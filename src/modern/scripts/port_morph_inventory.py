"""GuiMorphInventory API adaptation, using the existing ship GUI render bridge."""
from pathlib import Path
import ast
import json
import re
import sys

root = Path(__file__).resolve().parents[3]
legacy_root = Path(sys.argv[1])
legacy = legacy_root / 'src/main/java/com/lulan/shincolle'
old = (legacy / 'client/gui/GuiMorphInventory.java').read_text()
ids = (legacy / 'reference/ID.java').read_text()
common = (root / 'src/modern/scripts/port_ship_inventory.py').read_text()
functions = {n.name: ast.get_source_segment(common, n) for n in ast.parse(common).body if isinstance(n, ast.FunctionDef)}
for name in ['block', 'method', 'constant', 'write']:
    exec(functions[name])
button_names = {'NoFuel': 'NO_FUEL', 'Sit': 'SIT', 'EmoFlag1': 'EMO_FLAG_1', 'EmoFlag2': 'EMO_FLAG_2',
                'AuraEffect': 'AURA_EFFECT', 'ShowHeld': 'SHOW_HELD'}
exec(functions['api'])
control_fields = ['showPage', 'showPageAI', 'showAttack', 'maxBtn', 'xClick', 'yClick',
                  'switchPage1a', 'switchPage1b', 'switchPage3', 'switchPage4', 'switchPage6']
exec(functions['screen_api'])

gui = (legacy / 'utility/GuiHelper.java').read_text()
buttons = block(gui, gui.index('{//gui 7:'))
controls = '''package com.lulan.shincolle.menu;

import com.lulan.shincolle.network.ShipGuiButton;
import com.lulan.shincolle.network.GameplayCommandType;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public final class LegacyMorphInventoryControls {
    private static final int[] btCols = {189, 205, 222};
    private static final int[][][] SUPPLY_BUTTONS = ''' + buttons + ''';
    private final MorphInventoryPresentation entity;
    private final BiConsumer<Integer, Integer> send;
    private final Consumer<GameplayCommandType> supply;
    public int showPage = 1, showPageAI = 1, showAttack = 1, maxBtn, xClick, yClick;
    public final boolean[] switchPage1a = new boolean[6], switchPage1b = new boolean[6],
            switchPage3 = new boolean[6], switchPage4 = new boolean[6], switchPage6 = new boolean[17];
    public LegacyMorphInventoryControls(MorphInventoryPresentation entity, BiConsumer<Integer, Integer> send,
                                       Consumer<GameplayCommandType> supply) {
        this.entity = entity; this.send = send; this.supply = supply;
        this.maxBtn = entity.getStateMinor("NumState");
    }
    public static int supplyButtonAt(int x, int y) {
        for (int i = 0; i < SUPPLY_BUTTONS[0].length; i++) {
            int[] b = SUPPLY_BUTTONS[0][i];
            if (x >= b[0] && y >= b[1] && x <= b[2] && y <= b[3]) return i;
        }
        return -1;
    }
'''
click = api(method('mouseClicked'))
click = re.sub(r'protected void mouseClicked[^\n]*', 'public void click(int posX, int posY, int mouseKey)', click)
click = re.sub(r'^\s*super.mouseClicked[^\n]*', '', click, flags=re.M)
click = click.replace(' - this.guiLeft', '').replace(' - this.guiTop', '')
click = click.replace('GuiHelper.getButton(0, 0, ', 'LegacyShipInventoryControls.buttonAt(0, ')
click = click.replace('GuiHelper.getButton(7, 0, ', 'supplyButtonAt(')
click = re.sub(r'CommonProxy.channelG.sendToServer\(new C2SGUIPackets\(this.invPlayer.player, C2SGUIPackets.PID.MorphBtn, (.*?)\)\);', r'this.send.accept(\1);', click)
for suffix, command in [('AddAmmoL', 'MORPH_ADD_LIGHT_AMMO'), ('AddAmmoH', 'MORPH_ADD_HEAVY_AMMO'), ('AddGrudge', 'MORPH_ADD_GRUDGE')]:
    click = click.replace('CommonProxy.channelG.sendToServer(new C2SGUIPackets(this.invPlayer.player, C2SGUIPackets.PID.MorphBtn2, ID.B.MorphInv_' + suffix + ', 0));',
                          'this.supply.accept(GameplayCommandType.' + command + ');')
controls += '\n' + click + '\n' + method('getInverseInt') + '\n}\n'
write('src/modern/java/com/lulan/shincolle/menu/LegacyMorphInventoryControls.java', controls)

ship = (root / 'src/modern/java/com/lulan/shincolle/client/screen/ShipInventoryScreen.java').read_text()
header = ship[:ship.index('    private static String lvMark')]
header = header.replace('ShipInventoryScreen', 'MorphInventoryScreen').replace('ShipInventoryMenu', 'MorphInventoryMenu')
header = header.replace('ShipInventoryPresentation', 'MorphInventoryPresentation').replace('LegacyShipInventoryControls', 'LegacyMorphInventoryControls')
header = header.replace('texture("guishipinventory")', 'texture("guishipmorph")')
header = header.replace('import com.lulan.shincolle.network.ServerboundShipCommandPacket;', '''import com.lulan.shincolle.network.GameplayCommandType;
import com.lulan.shincolle.menu.LegacyShipInventoryControls;
import com.lulan.shincolle.network.ServerboundGameplayCommandPacket;
import com.lulan.shincolle.registry.ModEntityTypes;
import net.minecraft.nbt.CompoundTag;''')
fields = old[old.index('\tprivate static String lvMark'):old.index('\n\tpublic GuiMorphInventory(')]
fields = fields.replace('private List mouseoverList;', 'private List<String> mouseoverList;')
for field in control_fields:
    fields = re.sub(r'\b' + field + r'\s*[,;]', '', fields)
fields = re.sub(r'private boolean\[\]\s*', '', fields)
fields = re.sub(r',\s*;', ';', fields)
fields = re.sub(r'(shipClass)\s*,\s*\n', r'\1;\n', fields)
screen = header + fields + '''
    public MorphInventoryScreen(MorphInventoryMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.imageWidth = 256; this.imageHeight = 214;
        this.entity = menu.getPresentation(); this.attrs = this.entity;
        this.controls = new LegacyMorphInventoryControls(this.entity, (button, value) -> {
            CompoundTag tag = new CompoundTag(); tag.putInt("Button", button); tag.putInt("Value", value);
            ModNetwork.sendToServer(new ServerboundGameplayCommandPacket(GameplayCommandType.MORPH_GUI_BUTTON, tag));
        }, command -> ModNetwork.sendToServer(new ServerboundGameplayCommandPacket(command, new CompoundTag())));
        this.mouseoverList = new ArrayList<>();
        this.shipType = this.entity.getShipType(); this.shipClass = this.entity.getShipClass();
        this.iconXY = new int[][] {LegacyShipInventoryCatalog.typeIcon(shipType), LegacyShipInventoryCatalog.nameIcon(shipClass)};
        if (menu.hasProfile()) {
            this.shipRiding[0] = ModEntityTypes.LEGACY_SHIP.get().create(inventory.player.level());
            if (this.shipRiding[0] != null) this.shipRiding[0].applySpawnSpec(menu.getProfile().getSpec(), null);
        }
'''
constructor = method('GuiMorphInventory')
screen += constructor[constructor.index('\n\t\t//general string'):constructor.rfind('\n')].replace('I18n.format(', 'I18n.get(') + '\n    }\n'
screen += method('initGui').replace('public void initGui()', '@Override\n    protected void init()').replace('super.initGui()', 'super.init()') + '\n'
for name in ['drawGuiContainerForegroundLayer', 'drawGuiContainerBackgroundLayer', 'handleHoveringText', 'drawAttributes']:
    body = screen_api(method(name))
    body = body.replace('GuiHelper.getButton(0, ', 'LegacyShipInventoryControls.buttonAt(')
    body = body.replace('GuiHelper.getButton(7, 0, ', 'LegacyMorphInventoryControls.supplyButtonAt(')
    body = body.replace('GuiHelper.getButton(7, ', 'LegacyMorphInventoryControls.supplyButtonAt(')
    body = body.replace('capa.player', 'entity.getPlayer()')
    if name == 'drawGuiContainerForegroundLayer':
        body = re.sub(r'protected void drawGuiContainerForegroundLayer[^\n]*', '@Override\n    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY)', body)
    if name == 'drawGuiContainerBackgroundLayer':
        body = body.replace('protected void drawGuiContainerBackgroundLayer(float par1,int par2, int par3)', '@Override\n    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY)')
    screen += '\n' + body + '\n'
screen += ship[ship.index('    private void drawEntityModel('):ship.index('    @Override\n    public void render(')]
screen += '''
    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.graphics = graphics;
        this.menu.getPresentation();
        updatePreview();
        renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);
        this.xMouse = mouseX; this.yMouse = mouseY;
        this.graphics = null;
    }
    private void updatePreview() {
        var profile = this.menu.getProfile();
        var ship = this.shipRiding[0];
        if (profile == null || ship == null) return;
        ship.setShipLevel(profile.getLevel()); ship.setMorale(profile.getMorale());
        ship.setModelState(profile.getModelState()); ship.setEmotion(profile.getEmotion()); ship.setEmotion4(profile.getEmotion4());
        ship.setShipFuel(profile.isNoFuel() ? 0 : 1); ship.setOrderedToSit(profile.isSitting());
        ship.setMarried(profile.isMarried()); ship.setShowHeldItem(profile.isShowHeldItem());
        ship.setRingEffectEnabled(profile.hasAuraEffect());
        ship.tickCount = this.menu.getPlayer().tickCount;
        for (int i = 0; i < 6; i++) ship.getShipInventory().setItem(i, profile.getEquipment().getStackInSlot(i).copy());
    }
    @Override
    protected void containerTick() {
        super.containerTick();
        if (!this.menu.hasProfile() || !this.menu.getPlayer().isAlive()) this.minecraft.player.closeContainer();
    }
    @Override
    public boolean mouseClicked(double x, double y, int button) {
        boolean handled = super.mouseClicked(x, y, button);
        this.controls.click((int)x - this.leftPos, (int)y - this.topPos, button);
        return handled || LegacyShipInventoryControls.buttonAt(0, (int)x - this.leftPos, (int)y - this.topPos) >= 0
                || LegacyMorphInventoryControls.supplyButtonAt((int)x - this.leftPos, (int)y - this.topPos) >= 0;
    }
'''
tail = ship[ship.index('    private static ResourceLocation texture('):]
screen += tail[:tail.rfind('}')]
screen += '\n}\n'
screen = screen.replace('GuiHelper.getBonusPointColor(', 'getBonusPointColor(').replace('GuiHelper.getDarkerColor(', 'getDarkerColor(')
write('src/modern/java/com/lulan/shincolle/client/screen/MorphInventoryScreen.java', screen)

texture = 'assets/shincolle/textures/gui/guishipmorph.png'
original = legacy_root / 'src/main/resources/assets/shincolle/textures/gui/GuiShipMorph.png'
target = root / 'src/modern/resources' / texture
if '--check' in sys.argv:
    assert target.read_bytes() == original.read_bytes()
    print('GuiShipMorph texture byte parity: passed')
else:
    target.write_bytes(original.read_bytes())
keys = set(re.findall(r'"(gui.shincolle:[^"]+)"', old))
english = dict(line.split('=', 1) for line in (legacy_root / 'src/main/resources/assets/shincolle/lang/en_US.lang').read_text().splitlines()
               if '=' in line and not line.startswith('#'))
for locale in ['en_us', 'zh_cn', 'zh_tw']:
    path = root / 'src/modern/resources/assets/shincolle/lang' / (locale + '.json')
    data = json.loads(path.read_text())
    obsolete = [key for key in data if key.startswith('gui.shincolle.morph_inventory.') and key != 'gui.shincolle.morph_inventory.title']
    if '--check' in sys.argv:
        assert not obsolete, obsolete
    else:
        for key in obsolete: del data[key]
    legacy_lang = legacy_root / 'src/main/resources/assets/shincolle/lang' / ({'en_us': 'en_US', 'zh_cn': 'zh_CN', 'zh_tw': 'zh_TW'}[locale] + '.lang')
    source = dict(english)
    source.update(line.split('=', 1) for line in legacy_lang.read_text().splitlines() if '=' in line and not line.startswith('#'))
    for key in keys:
        if key in source:
            if '--check' in sys.argv: assert data[key] == source[key], (locale, key)
            else: data[key] = source[key]
    if '--check' not in sys.argv:
        path.write_text(json.dumps(data, ensure_ascii=False, indent=2) + '\n')
