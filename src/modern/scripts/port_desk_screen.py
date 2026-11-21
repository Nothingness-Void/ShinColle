"""GuiDesk/GuiBook method bodies with modern API plumbing kept in DeskScreenSupport."""
from pathlib import Path
import re
import sys
import textwrap

root = Path(__file__).resolve().parents[3]
legacy = Path(sys.argv[1]) / 'src/main/java/com/lulan/shincolle'
old = (legacy / 'client/gui/GuiDesk.java').read_text()
ids = (legacy / 'reference/ID.java').read_text()
enums = (legacy / 'reference/Enums.java').read_text()

def block(source, start):
    brace = source.index('{', start)
    depth, end = 1, brace + 1
    while depth:
        depth += (source[end] == '{') - (source[end] == '}')
        end += 1
    return source[start:end]

def method(name, source=old):
    m = re.search(r'(?m)^\s*(?:public|private|protected) [^\n]*\b' + name + r'\(', source)
    return block(source, m.start()).strip()

def constant(group, name):
    body = block(ids, ids.index('class ' + group))
    return re.search(r'\b' + name + r'\s*=\s*(-?\d+)\s*;', body)[1]

def adapt(source):
    source = re.sub(r'/\*.*?\*/', '', source, flags=re.S)
    source = re.sub(r'//[^\n]*', '', source)
    source = re.sub(r'Enums.EnumColors.(\w+).getValue\(\)', lambda m: re.search(r'\b' + m[1] + r'\((\d+|0x[\da-fA-F]+)\)', enums)[1], source)
    source = re.sub(r'ID\.(\w+)\.(\w+)', lambda m: constant(m[1], m[2]), source)
    for a, b in [
        ('Minecraft.getMinecraft().getTextureManager().bindTexture(', 'bindTexture('),
        ('this.mc.player.closeScreen()', 'this.minecraft.player.closeContainer()'),
        ('this.tile.getPos()', 'this.menu.getDeskPos()'),
        ('MathHelper.', 'Mth.'), ('TextFormatting.', 'ChatFormatting.'), ('I18n.format(', 'I18n.get('),
        ('GuiHelper.getButton(3, ', 'LegacyDeskState.buttonAt('),
        ('SourceFactor.SRC_ALPHA', '1'), ('DestFactor.ONE_MINUS_SRC_ALPHA', '1'),
        ('EntityHelper.getMoraleLevel(', 'DeskScreenSupport.moraleLevel('),
        ('EntityHelper.getEntityByID(eid, 0, true)', 'this.player.level().getEntity(eid)'),
        ('CommonProxy.entityItemList', 'itemCoordinates()'),
        ('Values.N.Pow2', 'POW2'), ('Values.BookList', 'LegacyDeskBookData.BookList'),
        ('this.capa.getPlayerTeamDataMap()', 'this.capa.getPlayerTeamDataMap()'),
        ('.getTeamID()', '.getTeamId()'), ('.getTeamLeaderName()', '.getLeaderName()'),
        ('.getTeamAllyList()', '.getAllies()'), ('.getTeamBannedList()', '.getBanned()'),
        ('s2.getLevel()', 's2.getShipLevel()'), ('BasicEntityShip', 'LegacyShipEntity'),
        ('.getEntityId()', '.getId()'), ('EntityLiving)', 'LivingEntity)'),
    ]:
        source = source.replace(a, b)
    source = re.sub(r'([\w.]+)\.pos([XYZ])\b', lambda m: m[0] if m[1] == 'obj' else m[1] + '.get' + m[2] + '()', source)
    source = re.sub(r'(?m)^\s*LogHelper\.(?:debug|info)\(.*?\);\s*$', '', source)
    packet_map = {'Desk_Disband': ('DESK_DISBAND_TEAM', ''), 'Desk_Create': ('DESK_CREATE_TEAM', 'TeamName'),
                  'Desk_Rename': ('DESK_RENAME_TEAM', 'TeamName'), 'Desk_Break': ('DESK_REMOVE_ALLY', 'RelationTeamId'),
                  'Desk_Ally': ('DESK_ADD_ALLY', 'RelationTeamId'), 'Desk_Unban': ('DESK_REMOVE_BANNED', 'RelationTeamId'),
                  'Desk_Ban': ('DESK_ADD_BANNED', 'RelationTeamId'), 'SetTarClass': ('TARGET_CLASS_REMOVE', 'TargetClass'),
                  'OpenShipGUI': ('OPEN_SHIP_INVENTORY', 'ShipId')}
    def packet(m):
        command, key = packet_map[m[1]]
        return f'send(GameplayCommandType.{command}, "{key}", {m[2]});'
    source = re.sub(r'CommonProxy.channelG.sendToServer\(new C2SGUIPackets\(player, C2SGUIPackets.PID.(\w+), (.*?)\)\);', packet, source)
    source = '\n'.join(line.expandtabs(4).rstrip() for line in source.splitlines() if line.strip())
    return source

methods = []
for name in ['drawGuiContainerBackgroundLayer', 'drawGuiContainerForegroundLayer', 'drawRadarIcon',
             'drawMoraleIcon', 'drawRadarText', 'drawTeamPic', 'drawTeamText', 'drawTargetText',
             'handleClickTeamMain', 'handleClickTeamAlly', 'handleClickTeamCreate', 'handleClickTeamRename',
             'handleClickTeamBan', 'handleWheelMove', 'mouseClickMove', 'handleHoveringText', 'openShipGUI']:
    body = adapt(method(name))
    if name == 'mouseClickMove':
        body = re.sub(r'\s*super.mouseClickMove\(.*?\);', '', body)
    methods.append(body)

click = method('mouseClicked')
click = click.replace('protected void mouseClicked(int posX, int posY, int mouseKey) throws IOException',
                      'private void click(int posX, int posY, int mouseKey)')
click = click.replace('super.mouseClicked(posX, posY, mouseKey);', '')
click = click.replace('this.textField.mouseClicked(posX, posY, mouseKey);', 'this.textField.mouseClicked(posX, posY, mouseKey);')
click = click.replace('if (oldClick == this.listClicked[LISTCLICK_RADAR]) this.openShipGUI();',
    'if (LegacyDeskState.opensRadarInventory(listNum[0]+oldClick, listNum[0]+listClicked[0], shipList.size())) this.openShipGUI();')
methods.append(adapt(click))

ship_draw = method('drawShipModel')
prefix = ship_draw[:ship_draw.index('//tick time')]
prefix = adapt(prefix).replace('Values.ShipTypeIconMap.get((byte)this.shipType)', 'LegacyShipInventoryCatalog.typeIcon(this.shipType)')
prefix += '''
        int modelTicking = this.tickGUI % 3;
        if (modelTicking == 0) this.shipModel.advancePreview();
        renderModel(this.shipModel.ship, this.guiLeft + 72, this.guiTop + 110 + this.mScale * 1.1F,
                this.mScale, this.mRotateX, this.mRotateY, modelTicking * 0.33F, this.shipMount);
    } else {
        bindTexture(guiBook2);
        drawTexturedModalRect(guiLeft+20, guiTop+48, 0, 148, 87, 108);
    }
}'''
methods.append(prefix)

book_old = (legacy / 'client/gui/GuiBook.java').read_text()
book = block(book_old, book_old.index('public class GuiBook'))
book = book.replace('public class GuiBook', 'private final class GuiBook')
# Book is an instance helper so its GL and font calls share the screen pose.
book = book.replace('private static TextureManager tm = ClientProxy.getMineraft().getTextureManager();', '')
book = book.replace('private static RenderItem itemRender;', '')
book = book.replace('itemRender = Minecraft.getMinecraft().getRenderItem();', '')
book = book.replace('GuiDesk', 'DeskScreen').replace('FontRenderer', 'LegacyFont')
book = book.replace('private static DeskScreen gui;', '')
book = book.replace('gui = par1;', '')
book = book.replace('private static', 'private').replace('public static', 'public')
book = book.replace('tm.bindTexture(', 'bindTexture(')
book = book.replace('gui.drawTexturedModalRect(', 'drawTexturedModalRect(')
book = book.replace('gui.tickGUI', 'tickGUI')
book = book.replace('RenderHelper.enableGUIStandardItemLighting();', '').replace('RenderHelper.disableStandardItemLighting();', '')
book = book.replace('itemRender.renderItemIntoGUI(item, x, y);', 'graphics.renderItem(item, x, y);')
book = book.replace('Values.ItemIconMap.get((short)itemID)', 'LegacyDeskBookData.icon(itemID)')
book = book.replace('CalcHelper.stringConvNewlineToArray(str)', 'str.split("<BR>|<BR/>|<br>|<br/>")')
book = book.replace('new ResourceLocation(Reference.TEXTURES_GUI+"book/BookPic01.png")', 'texture("book/bookpic01")')
book = adapt(book)

fields = old[old.index('private static final ResourceLocation guiTexture'):old.index('public GuiDesk(')]
fields = fields[:fields.index('//object: ship entity')]
fields = fields.replace('private static final ResourceLocation', 'private static final ResourceLocation')
fields = re.sub(r'new ResourceLocation\(Reference.TEXTURES_GUI\+"([^".]+).png"\)', lambda m: 'texture("' + m[1].lower() + '")', fields)
fields = fields.replace('private TileEntityDesk tile;', '')
fields = fields.replace('EntityPlayer player;', '')
fields = fields.replace('CapaTeitoku capa;', '')
fields = fields.replace('GuiTextField textField;', 'DeskEditBox textField;')
fields = fields.replace('BasicEntityShip shipModel', 'DeskScreenSupport.Preview shipModel')
fields = fields.replace('BasicEntityMount shipMount', 'DeskScreenSupport.PreviewMount shipMount')
fields = adapt(fields)
strings = old[old.index('StrPos = I18n.format('):old.index('public void initGui()')]
strings = strings[:strings.rfind('}')]
strings = adapt(strings)

out = '''package com.lulan.shincolle.client.screen;

import com.lulan.shincolle.blockentity.DeskBlockEntity;
import com.lulan.shincolle.entity.ship.LegacyShipEntity;
import com.lulan.shincolle.menu.DeskMenu;
import com.lulan.shincolle.menu.LegacyDeskBookData;
import com.lulan.shincolle.menu.LegacyDeskState;
import com.lulan.shincolle.menu.LegacyShipInventoryCatalog;
import com.lulan.shincolle.network.GameplayCommandType;
import com.lulan.shincolle.team.TeamData;
import com.lulan.shincolle.teitoku.TeitokuHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DeskScreen extends DeskScreenSupport {
    ''' + fields + '''
    private final GuiBook GuiBook = new GuiBook();
    public DeskScreen(DeskMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.imageWidth = 320; this.imageHeight = 240;
        this.type = menu.getDeskType();
        this.GuiScale = 1.25F; this.GuiScaleInv = 0.8F;
        this.guiFunc = menu.getSelectedFunction();
        if (this.type == 0 && inventory.player.level().getBlockEntity(menu.getDeskPos()) instanceof DeskBlockEntity desk) {
            this.book_chapNum = desk.getBookChapter(); this.book_pageNum = desk.getBookPage();
            this.radar_zoomLv = desk.getRadarZoomIndex();
        }
        this.tempCD = CLICKCD;
        this.listNum = new int[5]; this.listClicked = new int[] {-1,-1,-1,-1,-1};
        this.shipList = new ArrayList<>(); this.itemList = new ArrayList<>();
        updateTargetClassList(); this.mScale = 30F; this.mRotateY = -30F;
        setShipModel(this.book_chapNum, this.book_pageNum);
        ''' + strings + '''
    }
    @Override protected void init() {
        super.init();
        this.guiLeft = this.leftPos; this.guiTop = this.topPos;
        this.textField = new DeskEditBox((int)((guiLeft+10)*GuiScale), (int)((guiTop+24)*GuiScale), 153, 12);
        requestRadar();
    }
    @Override public void render(GuiGraphics g, int mx, int my, float partial) {
        this.graphics = g; this.xMouse = mx; this.yMouse = my;
        renderBackground(g);
        com.mojang.blaze3d.systems.RenderSystem.enableBlend();
        com.mojang.blaze3d.systems.RenderSystem.defaultBlendFunc();
        g.setColor(1F, 1F, 1F, 1F);
        g.pose().pushPose(); g.pose().scale(GuiScale, GuiScale, 1F);
        drawGuiContainerBackgroundLayer(partial, mx, my);
        g.pose().pushPose(); g.pose().translate(guiLeft, guiTop, 0);
        drawGuiContainerForegroundLayer(mx, my);
        g.pose().popPose(); g.pose().popPose();
        this.textField.visible = this.teamState == TEAMSTATE_CREATE || this.teamState == TEAMSTATE_RENAME;
        if (this.textField.visible) this.textField.render(g, mx, my, partial);
        tickGUI++; if (tempCD > 0) tempCD--;
    }
    @Override protected void containerTick() {
        super.containerTick();
        if ((tickGUI & 63) == 0) requestRadar();
        textField.tick();
    }
    @Override public boolean mouseClicked(double x, double y, int button) {
        click((int)x, (int)y, button); return true;
    }
    @Override public boolean mouseDragged(double x, double y, int button, double dx, double dy) {
        mouseClickMove((int)x, (int)y, button, 0); return true;
    }
    @Override public boolean mouseScrolled(double x, double y, double delta) {
        handleWheelMove(delta > 0); return true;
    }
    @Override public boolean keyPressed(int key, int scan, int mods) {
        if (textField.visible && textField.isFocused() && key != 256) {
            textField.keyPressed(key, scan, mods); return true;
        }
        return super.keyPressed(key, scan, mods);
    }
    @Override public boolean charTyped(char c, int mods) {
        return textField.visible && textField.charTyped(c, mods);
    }
    private void setDeskFunction(int button) {
        if (button < 0 || button > 3) return;
        LegacyDeskState state = new LegacyDeskState(); state.function = guiFunc;
        state.selectFunction(button); guiFunc = state.function; syncTileEntityC2S();
    }
    private void updateDeskValue() {
        if (player.level().getBlockEntity(menu.getDeskPos()) instanceof DeskBlockEntity desk) {
            guiFunc = desk.getSelectedFunction(); book_chapNum = desk.getBookChapter();
            book_pageNum = desk.getBookPage(); radar_zoomLv = desk.getRadarZoomIndex();
        }
    }
    private void syncTileEntityC2S() {
        if (type == 0) syncDesk(guiFunc, radar_zoomLv, book_chapNum, book_pageNum);
    }
    private void updateTargetClassList() { this.tarList = new ArrayList<>(this.menu.getTargetClasses()); }
    private void getEntityByClick() {
        int selected = listNum[LISTCLICK_TARGET] + listClicked[LISTCLICK_TARGET];
        this.targetEntity = selected >= 0 && selected < tarList.size() ? createTarget(tarList.get(selected)) : null;
    }
    private void drawTargetModel() {
        if (targetEntity != null) renderModel(targetEntity, guiLeft+72, guiTop+136, mScale, mRotateX, mRotateY, 1F, null);
    }
    private void setShipModel(int chapter, int page) {
        this.shipMount = null;
        this.shipModel = preview(chapter, page);
        this.shipMaxStats = this.shipModel == null ? 0 : this.shipModel.modelButtons();
        if (shipModel != null) {
            this.shipType = shipModel.shipType(); this.shipClass = shipModel.ship.getShipClassId();
            this.iconXY = new int[][] {LegacyShipInventoryCatalog.typeIcon(shipType), LegacyShipInventoryCatalog.nameIcon(shipClass)};
        }
    }
    private void setShipMount() { this.shipMount = shipModel == null ? null : shipModel.mount(); }
''' + '\n\n'.join('    ' + m for m in methods) + '\n' + textwrap.indent(book, '    ') + '\n}\n'
out = '\n'.join(line.expandtabs(4).rstrip() for line in out.splitlines()) + '\n'
target = root / 'src/modern/java/com/lulan/shincolle/client/screen/DeskScreen.java'
if '--check' in sys.argv:
    assert target.read_text() == out
    print('GuiDesk / GuiBook method adaptation: mechanical parity passed')
else:
    target.write_text(out)
