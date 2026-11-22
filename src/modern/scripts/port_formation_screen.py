"""Mechanical GuiFormation API adaptation; run with the 0aad501 checkout path."""
import pathlib
import re
import sys

root = pathlib.Path(__file__).resolve().parents[3]
old = (pathlib.Path(sys.argv[1]) / 'src/main/java/com/lulan/shincolle/client/gui/GuiFormation.java').read_text()


def method(name):
    start = re.search(r'(?m)^\s*(?:private|protected|public) [^\n]*\b' + name + r'\(', old).start()
    brace = old.index('{', start)
    depth = 1
    end = brace + 1
    while depth:
        depth += (old[end] == '{') - (old[end] == '}')
        end += 1
    return old[start:end].strip()


def adapt(source):
    source = source.replace('BasicEntityShip', 'ShipView').replace('AttrsAdv', 'ShipView')
    source = source.replace('private String getAttributeString(byte attrid)', 'private String getAttributeString(int attrid)')
    source = source.replace('ID.AttrsBase.', 'BaseAttr.').replace('ID.Attrs.', 'Attr.')
    source = source.replace('Attrs.AttrsLength', 'LegacyShipStatTables.ATTR_COUNT')
    source = source.replace('Attrs.getResetZeroValue()', 'new float[LegacyShipStatTables.ATTR_COUNT]')
    source = source.replace('BuffHelper.calcAttrsWithoutBuff(this.attrs, 4)', 'this.attrs.copyWithoutFormation()')
    source = source.replace('ConfigHandler.scaleShip', 'LegacyShipStatTables.SCALE_SHIP')
    source = source.replace('ConfigHandler.limitShipAttrs', 'LegacyShipStatTables.LIMIT_SHIP_ATTRS')
    source = source.replace('FormationHelper.getFormationBuffValue', 'this.menu.getFormationBuffValue')
    source = source.replace('getStateMinor(ID.M.FormatType)', 'getFormatType()')
    source = source.replace('getStateMinor(ID.M.FormatPos)', 'getFormatPos()')
    source = source.replace('getFormatIDCurrentTeam()', 'getCurrentFormationId()')
    source = source.replace('getCurrentTeamID()', 'getCurrentTeamId()').replace('getFormatID(', 'getFormationId(')
    source = source.replace('fontRendererObj.getStringWidth', 'font.width')
    source = source.replace('fontRendererObj.drawStringWithShadow', 'drawStringWithShadow')
    source = source.replace('fontRendererObj.drawString', 'drawString').replace('fontRendererObj', 'font')
    source = source.replace('TextFormatting.', 'ChatFormatting.').replace('I18n.format(', 'I18n.get(')
    source = source.replace('gui.shincolle:', 'gui.shincolle.')
    source = source.replace('Enums.EnumColors.WHITE.getValue()', '16777215')
    source = source.replace('Enums.EnumColors.YELLOW.getValue()', '16776960')
    source = source.replace('GlStateManager.pushMatrix()', 'graphics.pose().pushPose()')
    source = source.replace('GlStateManager.popMatrix()', 'graphics.pose().popPose()')
    source = source.replace('GlStateManager.scale(', 'graphics.pose().scale(')
    source = source.replace('Minecraft.getMinecraft().getTextureManager().bindTexture(guiNameIcon)', 'texture = guiNameIcon')
    source = source.replace('LogHelper.info(', 'LOGGER.warn(')
    source = re.sub(r'\b(listClicked|teamClicked|formatClicked|tickWaitSync|unitNameState)\b',
                    lambda m: {'listClicked': 'state.selectedRow', 'teamClicked': 'state.team',
                               'formatClicked': 'state.formation', 'tickWaitSync': 'state.waitTicks',
                               'unitNameState': 'state.namingTeam'}[m[0]], source)
    source = re.sub(r'\b(guiLeft|guiTop)\b', lambda m: {'guiLeft': 'leftPos', 'guiTop': 'topPos'}[m[0]], source)
    return source


header = '''package com.lulan.shincolle.client.screen;

import com.lulan.shincolle.ShinColle;
import com.lulan.shincolle.entity.ship.LegacyShipStatTables;
import com.lulan.shincolle.entity.ship.LegacyShipStatTables.Attr;
import com.lulan.shincolle.entity.ship.LegacyShipStatTables.BaseAttr;
import com.lulan.shincolle.formation.LegacyFormationScreenState;
import com.lulan.shincolle.menu.FormationMenu;
import com.lulan.shincolle.menu.FormationMenu.ShipView;
import com.lulan.shincolle.network.GameplayCommandHandler;
import com.lulan.shincolle.network.GameplayCommandType;
import com.lulan.shincolle.network.ModNetwork;
import com.lulan.shincolle.network.ServerboundGameplayCommandPacket;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.logging.LogUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;

public class FormationScreen extends AbstractContainerScreen<FormationMenu> {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final ResourceLocation guiFormat = ResourceLocation.fromNamespaceAndPath(ShinColle.MOD_ID, "textures/gui/guiformation.png");
    private static final ResourceLocation guiNameIcon = ResourceLocation.fromNamespaceAndPath(ShinColle.MOD_ID, "textures/gui/guinameicon0.png");
    private static final int[] barRows = {54, 69, 84, 99, 114, 129};
    private static final int[] barCols = {9, 52, 95};
    private static final int barLength = 20;
    private String attrHP, attrATKL, attrATKH, attrAIRL, attrAIRH, attrDEF, attrMOV,
            attrSPD, attrHIT, attrMISS, attrDODGE, attrCRI, attrDHIT, attrTHIT,
            attrAA, attrASM, attrXP, attrGRUDGE, attrAMMO, attrHPRES, attrKB,
            attrTotalFP, strPos, strErr01, strRadar, strNoSig;
    private String totalFPL, totalFPH, totalFPAL, totalFPAH, totalFPAA, totalFPASM, strUnitName;
    private int tickGUI, xMouse, yMouse, strLen;
    private final LegacyFormationScreenState state = new LegacyFormationScreenState();
    private final FormationMenu capa;
    private GuiGraphics graphics;
    private ResourceLocation texture;
    private EditBox textField;
    private ShipView attrs;
    private float[] unbuffedAttrs;
    private final int[][] spotPos = new int[2][6];
    private final int[][] spotPosFinal = new int[2][6];
    private final float[] buffBar = new float[LegacyShipStatTables.ATTR_COUNT];
    private final float[] buffBarFinal = new float[LegacyShipStatTables.ATTR_COUNT];
    private ShipView[] shipList = new ShipView[6];
    private final String[] shipName = new String[6];
    private final List<String> mouseoverList = new ArrayList<>();
    private int visualRevision = -1;

    public FormationScreen(FormationMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.capa = menu;
        this.imageWidth = 256;
        this.imageHeight = 192;
        for (int i = 0; i < 6; i++) {
            this.spotPos[0][i] = this.spotPos[1][i] = 25;
            this.spotPosFinal[0][i] = this.spotPosFinal[1][i] = 25;
        }
        this.state.team = menu.getCurrentTeamId();
        this.state.formation = menu.getCurrentFormationId();
        setShipList(this.state.team);
        setFormationSpotPos(this.state.formation);
        setFormationBuffBar(this.state.formation, this.state.selectedRow);
        setShipName();
'''
strings = old[old.index('\t\t//string\n') + len('\t\t//string\n'):old.index('\t\t//update string value')]
init_width = old[old.index('\t\t//get max string length'):old.index('    }', old.index('\t\t//get max string length'))]
ui = '''        updateString();
    }

    @Override
    protected void init() {
        super.init();
        this.textField = this.addWidget(new EditBox(this.font, this.leftPos + 100, this.topPos + 180,
                150, 12, Component.empty()));
        this.textField.setTextColor(16776960);
        this.textField.setTextColorUneditable(-1);
        this.textField.setMaxLength(250);
        this.textField.setEditable(false);
        this.textField.setVisible(false);
        this.state.namingTeam = -1;
'''
ui += adapt(init_width) + '''    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.graphics = graphics;
        if (this.menu.getVisualRevision() != this.visualRevision && this.state.waitTicks <= 0) {
            this.visualRevision = this.menu.getVisualRevision();
            setShipList(this.state.team);
            setShipName();
            setFormationBuffBar(this.state.formation, this.state.selectedRow);
            updateString();
        }
        this.renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
        this.xMouse = mouseX;
        this.yMouse = mouseY;
        this.tickGUI++;
        if (this.state.advanceGuiTick()) { setShipList(this.state.team); setShipName(); }
        this.textField.setEditable(this.state.namingTeam >= 0);
        this.textField.setVisible(this.state.namingTeam >= 0);
        if (this.state.namingTeam >= 0) this.textField.render(graphics, mouseX, mouseY, partialTick);
        if (this.tickGUI % 32 == 0) updateString();
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        if (this.state.waitTicks > 0) drawString(String.format("%.1f", this.state.waitTicks * 0.05F), 190, 171, 16776960);
        if (this.strUnitName != null) drawStringWithShadow(this.strUnitName, 100, 182, 16776960);
        drawFormationText();
        handleHoveringText();
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        RenderSystem.setShaderColor(1F, 1F, 1F, 1F);
        this.texture = guiFormat;
        drawTexturedModalRect(this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight);
        if (this.state.selectedRow > -1 && this.state.selectedRow < 6)
            drawTexturedModalRect(this.leftPos + 142, this.topPos + 5 + this.state.selectedRow * 27, 3, 192, 108, 27);
        drawTexturedModalRect(this.leftPos + 18 + this.state.team * 12, this.topPos + 167, 111 + this.state.team * 9, 207, 9, 11);
        drawTexturedModalRect(this.leftPos + 18 + this.state.formation * 18, this.topPos + 149, 111 + this.state.formation * 15, 192, 15, 15);
        drawFormationPosSpot();
        drawFormationBuffBar();
        this.texture = guiNameIcon;
        for (int i = 0; i < 6; i++) {
            if (this.shipList[i] != null)
                drawTexturedModalRect(this.leftPos + 145, this.topPos + 8 + i * 27,
                        LegacyFormationScreenState.moraleIconX(this.shipList[i].getMorale()), 240, 11, 11);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int mouseKey) {
        boolean handled = super.mouseClicked(mouseX, mouseY, mouseKey);
        int btn = LegacyFormationScreenState.buttonAt((int)mouseX - this.leftPos, (int)mouseY - this.topPos);
        if (btn >= 0 && btn <= 5) {
            this.state.formation = btn;
            setFormationSpotPos(btn);
            send(GameplayCommandType.SET_FORMATION, tag -> tag.putInt(GameplayCommandHandler.TAG_FORMATION_ID, this.state.formation));
        } else if (btn >= 6 && btn <= 14) {
            if (this.state.selectTeam(btn - 6)) {
                send(GameplayCommandType.SET_CURRENT_TEAM, tag -> tag.putInt(GameplayCommandHandler.TAG_TEAM_ID, this.state.team));
                setShipList(this.state.team);
                this.state.formation = this.menu.getFormationId(this.state.team);
                setFormationSpotPos(this.state.formation);
                setShipName();
            }
        } else if (btn >= 15 && btn <= 20) {
            if (this.state.clickRow(btn - 15)) openShipGUI();
        } else if (btn == 21 || btn == 22) {
            int from = this.state.selectedRow;
            int target = this.state.swapRow(btn == 22);
            if (target >= 0) send(GameplayCommandType.SWAP_TEAM_SLOT, tag -> {
                tag.putInt(GameplayCommandHandler.TAG_SLOT_FROM, from);
                tag.putInt(GameplayCommandHandler.TAG_SLOT_TO, target);
            });
        } else if (btn == 23) {
            if (this.state.namingTeam < 0) this.state.namingTeam = this.state.team;
            else renameTeam();
        }
        if (btn >= 0) { setFormationBuffBar(this.state.formation, this.state.selectedRow); updateString(); }
        return btn >= 0 || handled;
    }

    private void openShipGUI() {
        ShipView ship = this.shipList[this.state.selectedRow];
        if (ship == null) return;
        this.minecraft.player.closeContainer();
        send(GameplayCommandType.OPEN_SHIP_INVENTORY,
                tag -> tag.putInt(GameplayCommandHandler.TAG_SHIP_UID, ship.getUid()));
    }

    private void renameTeam() {
        send(GameplayCommandType.SET_UNIT_NAME, tag -> tag.putString(GameplayCommandHandler.TAG_TEAM_NAME, this.textField.getValue()));
        this.state.namingTeam = -1;
    }

    @Override
    public boolean keyPressed(int key, int scanCode, int modifiers) {
        if (key == GLFW.GLFW_KEY_ENTER || key == GLFW.GLFW_KEY_KP_ENTER) renameTeam();
        else if (key == GLFW.GLFW_KEY_ESCAPE) this.state.namingTeam = -1;
        return this.textField.keyPressed(key, scanCode, modifiers) || super.keyPressed(key, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char character, int modifiers) {
        return this.textField.charTyped(character, modifiers) || super.charTyped(character, modifiers);
    }

    @Override
    protected void containerTick() {
        super.containerTick();
        this.textField.tick();
    }

    private void setShipName() {
        for (int i = 0; i < 6; i++) this.shipName[i] = this.shipList[i] == null ? null : this.shipList[i].getName();
    }

    private void drawTexturedModalRect(int x, int y, int u, int v, int w, int h) {
        this.graphics.blit(this.texture, x, y, u, v, w, h);
    }
    private void drawString(String text, int x, int y, int color) {
        this.graphics.drawString(this.font, text, x, y, color, false);
    }
    private void drawStringWithShadow(String text, int x, int y, int color) {
        this.graphics.drawString(this.font, text, x, y, color, true);
    }
    private void drawHoveringText(List<String> lines, int x, int y, Font font) {
        this.graphics.pose().pushPose();
        this.graphics.pose().translate(-this.leftPos, -this.topPos, 0);
        this.graphics.renderComponentTooltip(font, lines.stream().map(Component::literal).map(c -> (Component)c).toList(),
                this.leftPos + x, this.topPos + y);
        this.graphics.pose().popPose();
    }
    private void send(GameplayCommandType type, java.util.function.Consumer<CompoundTag> fill) {
        CompoundTag payload = new CompoundTag();
        fill.accept(payload);
        ModNetwork.sendToServer(ServerboundGameplayCommandPacket.of(type, payload));
    }
'''
names = ['getAttributeString', 'updateString', 'handleHoveringText', 'drawFormationPosSpot',
         'drawFormationBuffBar', 'drawFormationText', 'setFormationSpotPos', 'setFormationBuffBar', 'setShipList']
body = '\n\n'.join(adapt(method(name)) for name in names)
source = header + adapt(strings) + ui + body + '\n}\n'
source = '\n'.join(line.expandtabs(4).rstrip() for line in source.splitlines()) + '\n'
target = root / 'src/modern/java/com/lulan/shincolle/client/screen/FormationScreen.java'
if '--check' in sys.argv[2:]:
    assert target.read_text() == source, 'FormationScreen differs from the mechanical API adaptation'
    print('GuiFormation mechanical methods (attributes/tooltips, text, spots, bars, ship ordering): 9/9')
else:
    target.write_text(source)
