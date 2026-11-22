"""Mechanical ship-inventory GUI adaptation from the complete 0aad501 tree."""
from pathlib import Path
import re
import sys

root = Path(__file__).resolve().parents[3]
legacy = Path(sys.argv[1]) / 'src/main/java/com/lulan/shincolle'
old = (legacy / 'client/gui/GuiShipInventory.java').read_text()
ids = (legacy / 'reference/ID.java').read_text()


def block(source, start):
    brace = source.index('{', start)
    depth, end = 1, brace + 1
    while depth:
        depth += (source[end] == '{') - (source[end] == '}')
        end += 1
    return source[start:end]


def method(name, source=old):
    match = re.search(r'(?m)^\s*(?:public|private|protected) [^\n]*\b' + name + r'\(', source)
    return block(source, match.start()).strip()


def constant(group, name):
    part = block(ids, ids.index('class ' + group + '\n'))
    value = re.search(r'\b' + name + r'\s*=\s*(-?\d+)\s*;', part)
    assert value, (group, name)
    return int(value[1])


def write(relative, source):
    path = root / relative
    source = '\n'.join(line.expandtabs(4).rstrip() for line in source.splitlines()) + '\n'
    if '--check' in sys.argv:
        assert path.read_text() == source, f'{path.name} differs from the mechanical port'
        print(path.name + ': mechanical parity passed')
    else:
        path.write_text(source)


sources = {p.stem: p.read_text() for p in (legacy / 'entity').rglob('*.java')}
base_minor = [int(v.strip()) for v in re.search(r'this.StateMinor = new int\[\] \{([^}]+)\}', sources['BasicEntityShip'])[1].split(',')]
caps = ['AtkType_Light', 'AtkType_Heavy', 'AtkType_AirLight', 'AtkType_AirHeavy', 'HaveRingEffect', 'CanPickItem']


def metadata(name):
    original_name = name
    chain = []
    while name in sources:
        chain.insert(0, sources[name])
        parent = re.search(r'class\s+\w+\s+extends\s+(\w+)', sources[name])
        name = parent[1] if parent else ''
    combined = '\n'.join(chain)
    ship_type = re.findall(r'setStateMinor\(ID.M.ShipType,\s*ID.ShipType.(\w+)\)', combined)
    state_count = re.findall(r'setStateMinor\(ID.M.NumState,\s*(\d+)\)', combined)
    model = re.findall(r'ModelPos\s*=\s*new float\[\]\s*\{([^}]+)\}', combined)
    flags = []
    for cap in caps:
        default = cap != 'CanPickItem'
        changes = re.findall(r'StateFlag\[ID.F.' + cap + r'\]\s*=\s*(true|false)', combined)
        changes += re.findall(r'setStateFlag\(ID.F.' + cap + r',\s*(true|false)\)', combined)
        flags.append(changes[-1] == 'true' if changes else default)
    assert model, original_name
    return (constant('ShipType', ship_type[-1]) if ship_type else 0,
            int(state_count[-1]) if state_count else base_minor[constant('M', 'NumState')],
            'class BasicEntityShipCV ' in combined,
            bool(re.search(r'implements[^\{]*\bIShipInvisible\b', combined)),
            model[-1].strip(), flags)


specs = (root / 'src/modern/java/com/lulan/shincolle/entity/ship/ShipEntitySpecs.java').read_text()
rows = re.findall(r'register\((\d+), "([^"]+)", ShipArchetype.\w+, (true|false)\)', specs)
entries = []
registry = {name: clazz for clazz, name in re.findall(r'createEntity\((\w+).class, "([^"]+)"', (legacy/'init/ModEntity.java').read_text())}
classes_by_id = {}
for clazz, source in sources.items():
    if clazz.endswith('Mob'): continue
    declared = re.search(r'setStateMinor\(ID.M.ShipClass,\s*ID.ShipClass.(\w+)\)', source)
    if declared:
        classes_by_id[constant('ShipClass', declared[1])] = clazz
for egg, stem, hostile in rows:
    if hostile == 'true':
        continue
    key = stem + ('Mob' if hostile == 'true' else '')
    aliases = {'EntitySubmHimeNew': 'EntitySSNH', 'EntityCarrierWDemon': 'EntityCarrierWD'}
    name = aliases.get(key, registry.get(key, key))
    if name not in sources: name = classes_by_id[int(egg) - 2]
    assert name in sources, name
    typ, count, carrier, invisible, pos, flags = metadata(name)
    entries.append(f'            Map.entry({egg}, new Metadata({typ}, {count}, {str(carrier).lower()}, {str(invisible).lower()}, new float[] {{{pos}}}, new boolean[] {{{", ".join(str(f).lower() for f in flags)}}}))')
values = (legacy / 'reference/Values.java').read_text()


def icons(map_name, group):
    part = block(values, values.index(' ' + map_name + ' ='))
    return ',\n'.join('            Map.entry(' + str(constant(group, name)) + ', new int[] {' + vector + '})'
                      for name, vector in re.findall(r'(?m)^\s*put\((?:\(int\))?ID.' + group + r'.(\w+),\s*new int\[\] \{([^}]+)\}', part))


catalog = '''package com.lulan.shincolle.menu;

import com.lulan.shincolle.entity.ship.ShipEntitySpec;
import java.util.Map;

public final class LegacyShipInventoryCatalog {
    public record Metadata(int shipType, int modelButtons, boolean carrier, boolean invisible,
                           float[] modelPosition, boolean[] capabilities) {}
    private static final String[] CAPABILITIES = {"AtkType_Light", "AtkType_Heavy", "AtkType_AirLight", "AtkType_AirHeavy", "HaveRingEffect", "CanPickItem"};
    private static final Map<Integer, Metadata> SHIPS = Map.ofEntries(
'''+',\n'.join(entries)+''');
    private static final Map<Integer, int[]> TYPE_ICONS = Map.ofEntries(
'''+icons('ShipTypeIconMap', 'ShipType')+''');
    private static final Map<Integer, int[]> NAME_ICONS = Map.ofEntries(
'''+icons('ShipNameIconMap', 'ShipClass')+''');
    private LegacyShipInventoryCatalog() {}
    public static Metadata forShip(ShipEntitySpec spec) { return SHIPS.get(spec.eggMeta()); }
    public static int[] typeIcon(int type) { return TYPE_ICONS.get(type); }
    public static int[] nameIcon(int shipClass) { return NAME_ICONS.get(shipClass); }
    public static boolean capability(Metadata data, String flag) {
        for (int i = 0; i < CAPABILITIES.length; i++) if (CAPABILITIES[i].equals(flag)) return data.capabilities()[i];
        throw new IllegalArgumentException("Unknown capability " + flag);
    }
}
'''
write('src/modern/java/com/lulan/shincolle/menu/LegacyShipInventoryCatalog.java', catalog)

button_names = {'Melee':'MELEE','AmmoLight':'AMMO_LIGHT','AmmoHeavy':'AMMO_HEAVY',
                'AirLight':'AIR_LIGHT','AirHeavy':'AIR_HEAVY','FollowMin':'FOLLOW_MIN','FollowMax':'FOLLOW_MAX',
                'FleeHP':'FLEE_HP','TarAI':'TAR_AI','AuraEffect':'AURA_EFFECT','OnSightAI':'ON_SIGHT_AI',
                'PVPAI':'PVP_AI','AAAI':'AA_AI','ASMAI':'ASM_AI','TIMEKEEPAI':'TIMEKEEP_AI',
                'InvPage':'INV_PAGE','PickitemAI':'PICKITEM_AI','WpStay':'WP_STAY','ShowHeld':'SHOW_HELD',
                'AutoCR':'AUTO_CR','AutoPump':'AUTO_PUMP','Task':'TASK','TaskSide':'TASK_SIDE'}


def api(source):
    source = re.sub(r'\bID\.([FMS])\.(\w+)', lambda m: '"'+m[2]+'"', source)
    source = source.replace('ID.AttrsBase.', 'BaseAttr.').replace('ID.Attrs.', 'Attr.')
    source = re.sub(r'ID.ShipClass.(\w+)', lambda m:str(constant('ShipClass', m[1])), source)
    source = re.sub(r'ID.ShipType.(\w+)', lambda m:str(constant('ShipType', m[1])), source)
    source = re.sub(r'ID.Emotion.(\w+)', lambda m:str(constant('Emotion', m[1])), source)
    def button(m):
        if m[1].startswith('ModelState'): return '(ShipGuiButton.MODEL_STATE_01 + '+str(int(m[1][-2:])-1)+')'
        return 'ShipGuiButton.'+button_names[m[1]]
    source = re.sub(r'ID.B.ShipInv_(\w+)', button, source)
    source = source.replace('Values.N.Pow2[', 'POW2[')
    source = source.replace('GuiHelper.getButton(ID.Gui.SHIPINVENTORY, ', 'LegacyShipInventoryControls.buttonAt(')
    source = re.sub(r'CommonProxy.channelG.sendToServer\(new C2SGUIPackets\(this.entity, C2SGUIPackets.PID.ShipBtn, (.*?)\)\);', r'this.send.accept(\1);', source)
    return source


control_fields = ['showPage','showPageAI','showPageInv','showAttack','mousePress','mousePressBar','maxBtn',
                  'switchPage1a','switchPage1b','switchPage3','switchPage4','switchPage6','xClick','yClick','barPos']
buttons = block((legacy/'utility/GuiHelper.java').read_text(), (legacy/'utility/GuiHelper.java').read_text().index('{//gui 0:'))
controls = '''package com.lulan.shincolle.menu;

import com.lulan.shincolle.network.ShipGuiButton;
import java.util.function.BiConsumer;

public final class LegacyShipInventoryControls {
    private static final int[][][] BUTTONS = '''+buttons+''';
    private static final int[] POW2 = new int[32];
    static { for (int i = 0; i < 32; i++) POW2[i] = 1 << i; }
    private static final int[] btCols = {189, 205, 222};
    private final ShipInventoryPresentation entity;
    private final BiConsumer<Integer, Integer> send;
    public int showPage = 1, showPageAI = 1, showPageInv, showAttack = 1;
    public int mousePressBar = -1, maxBtn, xClick, yClick, barPos;
    public boolean mousePress;
    public final boolean[] switchPage1a = new boolean[6], switchPage1b = new boolean[6],
            switchPage3 = new boolean[6], switchPage4 = new boolean[6], switchPage6 = new boolean[17];
    public LegacyShipInventoryControls(ShipInventoryPresentation entity, BiConsumer<Integer, Integer> send) {
        this.entity = entity;
        this.send = send;
        this.maxBtn = entity.getStateMinor("NumState");
    }
    public static int buttonAt(int page, int x, int y) {
        if (page < 0 || page >= BUTTONS.length) return -1;
        for (int i = 0; i < BUTTONS[page].length; i++) {
            int[] b = BUTTONS[page][i];
            if (x >= b[0] && y >= b[1] && x <= b[2] && y <= b[3]) return i;
        }
        return -1;
    }
'''
for name, signature in [('mouseClicked','public void click(int posX, int posY, int mouseKey)'),
                        ('mouseClickMove','public void drag(int posX, int posY, int mouseKey)'),
                        ('mouseReleased','public void release(int posX, int posY, int state)'),
                        ('getInverseInt','private int getInverseInt(boolean par1)')]:
    body = api(method(name))
    body = signature + body[body.index('\n'):]
    body = re.sub(r'^\s*super\.\w+\([^\n]*\);', '', body, flags=re.M)
    body = body.replace(' - this.guiLeft', '').replace(' - this.guiTop', '')
    controls += '\n    ' + body + '\n'
controls += '}\n'
write('src/modern/java/com/lulan/shincolle/menu/LegacyShipInventoryControls.java', controls)

screen_header = '''package com.lulan.shincolle.client.screen;

import com.lulan.shincolle.config.ShinColleConfig;
import com.lulan.shincolle.entity.ship.LegacyShipEntity;
import com.lulan.shincolle.entity.ship.LegacyShipStatTables.Attr;
import com.lulan.shincolle.entity.ship.LegacyShipStatTables.BaseAttr;
import com.lulan.shincolle.menu.LegacyShipInventoryCatalog;
import com.lulan.shincolle.menu.LegacyShipInventoryControls;
import com.lulan.shincolle.menu.ShipInventoryMenu;
import com.lulan.shincolle.menu.ShipInventoryPresentation;
import com.lulan.shincolle.network.ModNetwork;
import com.lulan.shincolle.network.ServerboundShipCommandPacket;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.LightTexture;
import com.mojang.blaze3d.platform.Lighting;
import net.minecraft.client.Minecraft;
import com.mojang.blaze3d.systems.RenderSystem;
import com.lulan.shincolle.blockentity.WaypointBlockEntity;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import org.joml.Quaternionf;
import org.slf4j.Logger;
import com.mojang.logging.LogUtils;
import java.util.ArrayList;
import java.util.List;

public final class ShipInventoryScreen extends AbstractContainerScreen<ShipInventoryMenu> {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final ResourceLocation TEXTURE_BG = texture("guishipinventory");
    private static final ResourceLocation TEXTURE_ICON0 = texture("guinameicon0");
    private static final ResourceLocation TEXTURE_ICON1 = texture("guinameicon1");
    private static final ResourceLocation TEXTURE_ICON2 = texture("guinameicon2");
    private static final int[] POW2 = new int[32];
    static { for (int i = 0; i < 32; i++) POW2[i] = 1 << i; }
    private final ShipInventoryPresentation entity, attrs;
    private final LegacyShipInventoryControls controls;
    private final LegacyShipEntity[] shipRiding = new LegacyShipEntity[3];
    private GuiGraphics graphics;
    private ResourceLocation boundTexture;
    private final FontBridge fontRendererObj = new FontBridge();
'''
fields = old[old.index('\tprivate static String lvMark'):old.index('\n\tpublic GuiShipInventory(')]
fields = fields.replace('private List mouseoverList;', 'private List<String> mouseoverList;')
for field in control_fields:
    fields = re.sub(r'\b'+field+r'\s*[,;]', '', fields)
# Empty declarations and a trailing comma after removing shared controller fields.
fields = re.sub(r'private boolean\s*\n', '', fields)
fields = re.sub(r'private boolean\[\]\s*\n', '', fields)
fields = fields.replace('yMouse;', 'yMouse;')
fields = re.sub(r',\s*;', ';', fields)
fields = re.sub(r'(autoCRPos)\s*,\s*\n', r'\1;\n', fields)
fields = fields.replace('            private int[][]', '    private int[][]')
screen = screen_header + fields
screen += '''
    public ShipInventoryScreen(ShipInventoryMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.imageWidth = 256;
        this.imageHeight = 214;
        this.entity = menu.getPresentation();
        this.attrs = this.entity;
        this.controls = new LegacyShipInventoryControls(this.entity, (button, value) -> {
            if (this.menu.canEdit()) ModNetwork.sendToServer(ServerboundShipCommandPacket.guiButton(
                    this.menu.getShipId(), this.menu.getShipUid(), button, value));
        });
        this.mouseoverList = new ArrayList<>();
        this.shipType = this.entity.getShipType();
        this.shipClass = this.entity.getShipClass();
        this.shipRiding[0] = menu.getShip();
'''
constructor = method('GuiShipInventory')
special = f'''
        if ((this.shipClass == {constant('ShipClass','DDIkazuchi')} || this.shipClass == {constant('ShipClass','DDInazuma')})
                && this.entity.getRidingState() > 1 && this.shipRiding[0] != null) {{
            this.shipType = {constant('ShipType','HEAVY_CRUISER')};
            this.shipClass = {constant('ShipClass','Raiden')};
            if (this.shipRiding[0].getVehicle() instanceof LegacyShipEntity mount
                    && mount.getSpec().legacyClassId() == {constant('ShipClass','DDInazuma')}) {{
                this.shipRiding[2] = mount;
            }} else if (!this.shipRiding[0].getPassengers().isEmpty()
                    && this.shipRiding[0].getPassengers().get(0) instanceof LegacyShipEntity rider
                    && rider.getSpec().legacyClassId() == {constant('ShipClass','DDIkazuchi')}) {{
                this.shipRiding[1] = rider;
            }}
        }}
'''
screen += special
screen += '''
        this.iconXY = new int[][] {LegacyShipInventoryCatalog.typeIcon(this.shipType),
                LegacyShipInventoryCatalog.nameIcon(this.shipClass)};
'''
init_strings = constructor[constructor.index('\n\t\t//general string'):constructor.rfind('\n')]
screen += init_strings.replace('I18n.format(', 'I18n.get(') + '\n    }\n'
init = method('initGui').replace('public void initGui()', '@Override\n    protected void init()').replace('super.initGui()', 'super.init()')
screen += '\n    '+init+'\n'


def screen_api(body):
    body = api(body)
    for field in control_fields:
        body = re.sub(r'(?<!\w)(?:this\.)?'+field+r'\b', 'this.controls.'+field, body)
    body = body.replace('I18n.format(', 'I18n.get(')
    body = body.replace('MathHelper.', 'Mth.').replace('TextFormatting.', 'ChatFormatting.')
    colors = dict(re.findall(r'\b(\w+)\((\d+)\)', (legacy/'reference/Enums.java').read_text()))
    body = re.sub(r'Enums.EnumColors.(\w+).getValue\(\)', lambda m:colors[m[1]], body)
    body = body.replace('guiLeft', 'leftPos').replace('guiTop', 'topPos').replace('xSize', 'imageWidth').replace('ySize', 'imageHeight')
    body = body.replace('this.entity.getCapaShipInventory().getInventoryPage()', 'this.entity.getInventoryPage()')
    body = re.sub(r'^\s*CapaShipInventory inv = this.entity.getCapaShipInventory\(\);', '', body, flags=re.M)
    body = body.replace('inv.getStackInSlot(i + 12) == null', 'this.menu.slots.get(i + 12).getItem().isEmpty()')
    body = body.replace('((BasicEntityShipCV)this.entity)', 'this.entity')
    body = body.replace('this.entity instanceof BasicEntityShipCV', 'this.entity.isCarrier()')
    body = body.replace('this.entity instanceof IShipInvisible', 'this.entity.isInvisibleShip()')
    body = body.replace('((IShipInvisible)this.entity)', 'this.entity')
    body = body.replace('CombatHelper.calcMissRate(this.entity, ', 'this.entity.calcMissRate(')
    body = body.replace('EntityHelper.getMoraleLevel(', 'moraleLevel(')
    body = body.replace('CalcHelper.tick2SecOrMin(', 'tick2SecOrMin(')
    body = body.replace('entity.wpStayTime2Ticks(', 'WaypointBlockEntity.stayModeToTicks(')
    body = body.replace('ConfigHandler.limitShipAttrs[Attr.DODGE]', 'com.lulan.shincolle.entity.ship.LegacyShipStatTables.LIMIT_SHIP_ATTRS[Attr.DODGE]')
    body = re.sub(r'^\s*entity.setExpNext\(\);[^\n]*', '', body, flags=re.M)
    body = re.sub(r'Minecraft.getMinecraft\(\).getTextureManager\(\).bindTexture\(([^)]+)\);', r'this.boundTexture = \1;', body)
    body = re.sub(r'^\s*GlStateManager\.\w+\([^\n]*\);', '', body, flags=re.M)
    body = body.replace('drawString(titlename, 8, 6, 4210752)', 'drawString(titlename, 8, 6, 4210752)')
    body = body.replace('LogHelper.info(', 'LOGGER.info(')
    return body


for name in ['drawGuiContainerForegroundLayer','drawGuiContainerBackgroundLayer','drawIconMorale','handleHoveringText','drawAttributes']:
    body = screen_api(method(name))
    if name == 'drawGuiContainerForegroundLayer':
        body = re.sub(r'protected void drawGuiContainerForegroundLayer[^\n]*',
                      '@Override\n    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY)', body)
    if name == 'drawGuiContainerBackgroundLayer':
        body = body.replace('protected void drawGuiContainerBackgroundLayer(float par1,int par2, int par3)',
                            '@Override\n    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY)')
    screen += '\n    '+body+'\n'
screen += '''
    @Override
    protected void containerTick() {
        super.containerTick();
        LegacyShipEntity ship = this.menu.getShip();
        if (ship == null || !ship.isAlive() || ship.distanceTo(this.minecraft.player) > ShinColleConfig.closeGuiDist()) {
            this.minecraft.player.closeContainer();
        }
    }

    private void drawEntityModel(int x, int y, float[] modelPos, float yaw, float pitch, LegacyShipEntity[] ships) {
        if (ships[0] == null) return;
        var dispatcher = Minecraft.getInstance().getEntityRenderDispatcher();
        PoseStack pose = this.graphics.pose();
        this.graphics.flush();
        pose.pushPose();
        pose.translate(x + modelPos[0], y + modelPos[1], 50F + modelPos[2]);
        pose.scale(-modelPos[3], modelPos[3], modelPos[3]);
        pose.mulPose(Axis.ZP.rotationDegrees(180F));
        Lighting.setupForEntityInInventory();
        pose.mulPose(Axis.XP.rotationDegrees(-((float)Math.atan(pitch / 40F)) * 20F));
        for (LegacyShipEntity ship : ships) {
            if (ship == null) continue;
            ship.yBodyRot = (float)Math.atan(yaw / 40F) * 20F;
            ship.setYRot((float)Math.atan(yaw / 40F) * 40F);
            ship.setXRot(-((float)Math.atan(pitch / 40F)) * 20F);
            ship.yHeadRot = ship.getYRot();
            ship.yHeadRotO = ship.getYRot();
        }
        Quaternionf camera = new Quaternionf(dispatcher.cameraOrientation());
        dispatcher.overrideCameraOrientation(Axis.YP.rotationDegrees(180F));
        dispatcher.setRenderShadow(false);
        try {
            LegacyShipEntity mount = ships[2] != null ? ships[2] : ships[0];
            LegacyShipEntity rider = ships[1] != null ? ships[1] : ships[0];
            if (ships[1] != null || ships[2] != null) {
                float specialOffset = 0F;
                if ((ships[1] != null && rider.getSpec().legacyClassId() == 53)
                        || (ships[2] != null && mount.getSpec().legacyClassId() == 54)) {
                    if (mount.getEmotion() == 0) specialOffset = mount.isOrderedToSit() ? -0.34F : -0.57F;
                    else specialOffset = mount.isOrderedToSit() ? -0.55F : -0.45F;
                }
                float angle = (ships[0].yBodyRot % 360) / 57.2957F;
                float z = -0.2F * Mth.cos(angle), dx = 0.2F * Mth.sin(angle);
                pose.pushPose();
                pose.translate(dx, mount.getBbHeight() + specialOffset, z);
                renderPreviewShip(rider, pose);
                pose.popPose();
                renderPreviewShip(mount, pose);
            } else renderPreviewShip(ships[0], pose);
            this.graphics.flush();
        } finally {
            dispatcher.setRenderShadow(true);
            dispatcher.overrideCameraOrientation(camera);
            pose.popPose();
            Lighting.setupFor3DItems();
        }
    }

    private void renderPreviewShip(LegacyShipEntity ship, PoseStack pose) {
        RenderSystem.runAsFancy(() -> Minecraft.getInstance().getEntityRenderDispatcher().render(
                ship, 0D, 0D, 0D, 0F, 1F, pose, this.graphics.bufferSource(), LightTexture.FULL_BRIGHT));
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.graphics = graphics;
        renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);
        this.xMouse = mouseX;
        this.yMouse = mouseY;
        this.graphics = null;
    }

    @Override
    public boolean mouseClicked(double x, double y, int button) {
        boolean handled = super.mouseClicked(x, y, button);
        if (this.menu.canEdit()) this.controls.click((int)x - this.leftPos, (int)y - this.topPos, button);
        return handled || LegacyShipInventoryControls.buttonAt(0, (int)x - this.leftPos, (int)y - this.topPos) >= 0;
    }

    @Override
    public boolean mouseDragged(double x, double y, int button, double dx, double dy) {
        if (this.controls.mousePress) {
            this.controls.drag((int)x - this.leftPos, (int)y - this.topPos, button);
            return true;
        }
        return super.mouseDragged(x, y, button, dx, dy);
    }

    @Override
    public boolean mouseReleased(double x, double y, int button) {
        this.controls.release((int)x - this.leftPos, (int)y - this.topPos, button);
        return super.mouseReleased(x, y, button);
    }

    private static ResourceLocation texture(String name) {
        return ResourceLocation.fromNamespaceAndPath("shincolle", "textures/gui/" + name + ".png");
    }
    private void drawTexturedModalRect(int x, int y, int u, int v, int width, int height) {
        this.graphics.blit(this.boundTexture, x, y, u, v, width, height);
    }
    private void drawHoveringText(List<String> lines, int x, int y, FontBridge font) {
        this.graphics.renderComponentTooltip(this.font, lines.stream().map(Component::literal).map(c -> (Component)c).toList(), x, y);
    }
    private final class FontBridge {
        int getStringWidth(String text) { return Minecraft.getInstance().font.width(text); }
        void drawString(String text, int x, int y, int color) { graphics.drawString(font, text, x, y, color, false); }
        void drawStringWithShadow(String text, float x, float y, int color) { graphics.drawString(font, text, (int)x, (int)y, color, true); }
    }
'''
for name, source in [('getMoraleLevel', (legacy/'utility/EntityHelper.java').read_text()),
                     ('tick2SecOrMin',(legacy/'utility/CalcHelper.java').read_text())]:
    body = method(name, source)
    body = re.sub(r'ID.Morale.(\w+)', lambda m:str(constant('Morale',m[1])), body)
    screen += '\n    '+body.replace('getMoraleLevel(', 'moraleLevel(')+'\n'
for name in ['getBonusPointColor','getDarkerColor']:
    body = method(name, (legacy/'utility/GuiHelper.java').read_text())
    body = body.replace('ConfigHandler.modernLimit', 'ShinColleConfig.modernLimit()')
    screen += '\n    '+body+'\n'
screen = screen.replace('GuiHelper.getBonusPointColor(', 'getBonusPointColor(').replace('GuiHelper.getDarkerColor(', 'getDarkerColor(')
screen += '\n}\n'
screen = screen.replace('mount.getEmotion() == 0', 'mount.getEmotion() == '+str(constant('Emotion','BORED')))

formation_values = screen[screen.index('overText = String.valueOf(this.entity.getStateMinor("FormatPos")'):]
formation_values = formation_values[:formation_values.index('this.drawHoveringText')].rstrip()
screen = screen.replace(formation_values, 'mouseoverList.addAll(this.entity.formationTooltipValues());')
formation_method = '''public java.util.List<String> formationTooltipValues() {
        java.util.List<String> values = new java.util.ArrayList<>();
        if (getStateMinor("FormatType") < 1) return values;
        String str, str2, overText;
        '''+formation_values.replace('mouseoverList', 'values').replace('this.entity.', 'this.').replace('attrs.', 'this.')+'''
        return values;
    }'''
presentation_path = root/'src/modern/java/com/lulan/shincolle/menu/ShipInventoryPresentation.java'
presentation = presentation_path.read_text()
if 'public java.util.List<String> formationTooltipValues()' in presentation:
    previous = block(presentation, presentation.index('public java.util.List<String> formationTooltipValues()'))
    presentation = presentation.replace(previous, formation_method)
else:
    presentation = presentation.rstrip()[:-1]+'    '+formation_method+'\n}\n'
write('src/modern/java/com/lulan/shincolle/menu/ShipInventoryPresentation.java', presentation)
write('src/modern/java/com/lulan/shincolle/client/screen/ShipInventoryScreen.java', screen)

import json
import shutil

resource_root = root/'src/modern/resources/assets/shincolle'
legacy_resources = legacy.parents[3]/'resources/assets/shincolle'
for stem in ['GuiShipInventory', 'GuiNameIcon0', 'GuiNameIcon1', 'GuiNameIcon2']:
    origin = legacy_resources/'textures/gui'/f'{stem}.png'
    target = resource_root/'textures/gui'/f'{stem.lower()}.png'
    if '--check' in sys.argv:
        assert origin.read_bytes() == target.read_bytes()
    else:
        shutil.copyfile(origin, target)

used_keys = set(re.findall(r'I18n.format\("([^"]+)"\)', old))
used_keys |= {'gui.shincolle:formation.format'+str(i) for i in range(7)}
java = '\n'.join(p.read_text() for p in (root/'src/modern/java').rglob('*.java'))
live_keys = re.findall(r'"(gui.shincolle.ship_inventory.[^"]+)"', java)
for modern_locale, legacy_locale in [('en_us','en_US'), ('zh_cn','zh_CN'), ('zh_tw','zh_TW')]:
    lang_path = resource_root/'lang'/f'{modern_locale}.json'
    original = lang_path.read_text()
    language = json.loads(original)
    legacy_lang = dict(line.split('=', 1) for line in (legacy_resources/'lang'/f'{legacy_locale}.lang').read_text().splitlines() if '=' in line and not line.startswith('#'))
    missing = {key: legacy_lang[key] for key in sorted(used_keys) if key in legacy_lang and key not in language}
    obsolete = [key for key in language if key.startswith('gui.shincolle.ship_inventory.') and not any(key.startswith(live) for live in live_keys)]
    result = original
    for key in obsolete:
        result = re.sub(r'(?m)^\s*'+re.escape(json.dumps(key))+r':[^\n]+\n', '', result)
    if missing:
        result = result.rstrip()[:-1].rstrip().rstrip(',')+',\n'
        result += ',\n'.join('  '+json.dumps(k, ensure_ascii=False)+': '+json.dumps(v, ensure_ascii=False) for k,v in missing.items())+'\n}\n'
    json.loads(result)
    if '--check' in sys.argv:
        assert not missing and not obsolete, modern_locale
    else:
        lang_path.write_text(result)
