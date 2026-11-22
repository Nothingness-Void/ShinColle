package com.lulan.shincolle.client.screen;

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
        attrHP = ChatFormatting.YELLOW+I18n.get("gui.shincolle.hp");
        attrATKL = ChatFormatting.RED+I18n.get("gui.shincolle.firepower1");
        attrATKH = ChatFormatting.GREEN+I18n.get("gui.shincolle.torpedo");
        attrAIRL = ChatFormatting.RED+I18n.get("gui.shincolle.airfirepower");
        attrAIRH = ChatFormatting.GREEN+I18n.get("gui.shincolle.airtorpedo");
        attrDEF = ChatFormatting.WHITE+I18n.get("gui.shincolle.armor");
        attrSPD = ChatFormatting.WHITE+I18n.get("gui.shincolle.attackspeed");
        attrMOV = ChatFormatting.GRAY+I18n.get("gui.shincolle.movespeed");
        attrHIT = ChatFormatting.LIGHT_PURPLE+I18n.get("gui.shincolle.range");
        attrMISS = ChatFormatting.RED+I18n.get("gui.shincolle.missreduce");
        attrDODGE = ChatFormatting.GOLD+I18n.get("gui.shincolle.dodge");
        attrCRI = ChatFormatting.AQUA+I18n.get("gui.shincolle.critical");
        attrDHIT = ChatFormatting.YELLOW+I18n.get("gui.shincolle.doublehit");
        attrTHIT = ChatFormatting.GOLD+I18n.get("gui.shincolle.triplehit");
        attrAA = ChatFormatting.YELLOW+I18n.get("gui.shincolle.antiair");
        attrASM = ChatFormatting.AQUA+I18n.get("gui.shincolle.antiss");
        attrXP = ChatFormatting.GREEN+I18n.get("gui.shincolle.equip.xp");
        attrGRUDGE = ChatFormatting.DARK_PURPLE+I18n.get("gui.shincolle.equip.grudge");
        attrAMMO = ChatFormatting.DARK_AQUA+I18n.get("gui.shincolle.equip.ammo");
        attrHPRES = ChatFormatting.DARK_GREEN+I18n.get("gui.shincolle.equip.hpres");
        attrKB = ChatFormatting.YELLOW+I18n.get("gui.shincolle.equip.kb");
        attrTotalFP = ChatFormatting.LIGHT_PURPLE+I18n.get("gui.shincolle.formation.totalfirepower");
        strPos = I18n.get("gui.shincolle.formation.position");
        strErr01 = I18n.get("gui.shincolle.formation.notenough");
        strRadar = I18n.get("gui.shincolle.radar.tname");
        strNoSig = ChatFormatting.DARK_RED+""+ChatFormatting.OBFUSCATED+I18n.get("gui.shincolle.formation.nosignal");

        updateString();
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
        //get max string length
        strLen = font.width(attrTotalFP);
        int temp = font.width(attrATKL);
        if(temp > strLen) strLen = temp;
        temp = font.width(attrATKH);
        if(temp > strLen) strLen = temp;
        temp = font.width(attrAIRL);
        if(temp > strLen) strLen = temp;
        temp = font.width(attrAIRH);
        if(temp > strLen) strLen = temp;
        temp = font.width(attrAA);
        if(temp > strLen) strLen = temp;
        temp = font.width(attrASM);
        if(temp > strLen) strLen = temp;
    }

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
private String getAttributeString(int attrid)
    {
        String str = "";
        String valueOrg = "";
        String valueBuff = "";

        //null check
        if (this.attrs == null) return str;

        /*  multiplication (add "%")
         *    ATK, DEF, SPD, CRI, DHIT, THIT, MISS, AA, ASM
         *  addition (without "%")
         *    HP, MOV, HIT, DODGE, XP, GRUDGE, AMMO, HPRES
         */
        switch (attrid)
        {
        case Attr.ATK_L:
        case Attr.ATK_H:
        case Attr.ATK_AL:
        case Attr.ATK_AH:
        case Attr.SPD:
        case Attr.AA:
        case Attr.ASM:
            str = String.format("%.0f", (this.attrs.getAttrsFormation(attrid) - 1F) * 100F) + "% : " + ChatFormatting.GRAY;
            valueOrg = String.format("%.1f", this.unbuffedAttrs[attrid]);
            valueBuff = String.format("%.1f", this.attrs.getAttrsBuffed(attrid));
            if (attrs.getAttrsFormation(attrid) > 1F) str = "+"+str;
        break;
        case Attr.DEF:
        case Attr.CRI:
        case Attr.DHIT:
        case Attr.THIT:
        case Attr.MISS:
            str = String.format("%.0f", (this.attrs.getAttrsFormation(attrid) - 1F) * 100F) + "% : " + ChatFormatting.GRAY;
            valueOrg = String.format("%.1f", this.unbuffedAttrs[attrid] * 100F) + "%";
            valueBuff = String.format("%.1f", this.attrs.getAttrsBuffed(attrid) * 100F) + "%";
            if (attrs.getAttrsFormation(attrid) > 1F) str = "+"+str;
        break;
        case Attr.HIT:
            str = String.format("%.2f", this.attrs.getAttrsFormation(attrid)) + " : " + ChatFormatting.GRAY;
            valueOrg = String.format("%.2f", this.unbuffedAttrs[attrid]);
            valueBuff = String.format("%.2f", this.attrs.getAttrsBuffed(attrid));
            if (attrs.getAttrsFormation(attrid) > 0F) str = "+"+str;
        break;
        case Attr.MOV:
            float mov = this.attrs.getMinMOV() + this.attrs.getAttrsFormation(attrid) * (float)LegacyShipStatTables.SCALE_SHIP[BaseAttr.MOV];
            if (mov > (float)LegacyShipStatTables.LIMIT_SHIP_ATTRS[Attr.MOV]) mov = (float)LegacyShipStatTables.LIMIT_SHIP_ATTRS[Attr.MOV];
            else if (mov < 0F) mov = 0F;

            str = String.format("%.2f", this.attrs.getAttrsFormation(attrid)) + " : " + ChatFormatting.GRAY;
            valueOrg = String.format("%.2f", this.unbuffedAttrs[attrid]);
            valueBuff = String.format("%.2f", mov);
            if (attrs.getAttrsFormation(attrid) > 0F) str = "+"+str;
        break;
        case Attr.DODGE:
            str = String.format("%.0f", this.attrs.getAttrsFormation(attrid) * 100F) + "% : " + ChatFormatting.GRAY;
            valueOrg = String.format("%.1f", this.unbuffedAttrs[attrid] * 100F) + "%";
            valueBuff = String.format("%.1f", this.attrs.getAttrsBuffed(attrid) * 100F) + "%";
            if (attrs.getAttrsFormation(attrid) > 0F) str = "+"+str;
        break;
        case Attr.GRUDGE:
        case Attr.HPRES:
            str = String.format("%.0f", this.attrs.getAttrsFormation(attrid) * 100F) + "% : " + ChatFormatting.GRAY;
            valueOrg = String.format("%.0f", (this.unbuffedAttrs[attrid] - 1F) * 100F) + "%";
            valueBuff = String.format("%.0f", (this.attrs.getAttrsBuffed(attrid) - 1F) * 100F) + "%";
            if (attrs.getAttrsFormation(attrid) > 0F) str = "+"+str;
        break;
        default:
            str = String.format("%.0f", this.attrs.getAttrsFormation(attrid) * 100F) + "% : " + ChatFormatting.GRAY;
            valueOrg = String.format("%.0f", this.unbuffedAttrs[attrid] * 100F) + "%";
            valueBuff = String.format("%.0f", this.attrs.getAttrsBuffed(attrid) * 100F) + "%";
            if (attrs.getAttrsFormation(attrid) > 0F) str = "+"+str;
        break;
        }

        //combine string
        str = str + valueOrg + ChatFormatting.WHITE + " -> " + ChatFormatting.YELLOW + valueBuff;

        return str;
    }

private void updateString()
    {
        //update formation attrs
        if (shipList[state.selectedRow] != null)
        {
            this.attrs = (ShipView) shipList[state.selectedRow].getAttrs();
            //calc attrs before formation buff
            this.unbuffedAttrs = this.attrs.copyWithoutFormation();
        }
        else
        {
            this.attrs = null;
            this.unbuffedAttrs = new float[LegacyShipStatTables.ATTR_COUNT];
        }

        float floatFPL = 0F;
        float floatFPH = 0F;
        float floatFPAL = 0F;
        float floatFPAH = 0F;
        float floatFPAA = 0F;
        float floatFPASM = 0F;

        //update attrs
        for (int i = 0; i < 6; i++)
        {
            if (this.shipList[i] != null)
            {
                floatFPL += this.shipList[i].getAttrs().getAttrsBuffed(Attr.ATK_L);
                floatFPH += this.shipList[i].getAttrs().getAttrsBuffed(Attr.ATK_H);
                floatFPAL += this.shipList[i].getAttrs().getAttrsBuffed(Attr.ATK_AL);
                floatFPAH += this.shipList[i].getAttrs().getAttrsBuffed(Attr.ATK_AH);
                floatFPAA += this.shipList[i].getAttrs().getAttrsBuffed(Attr.AA);
                floatFPASM += this.shipList[i].getAttrs().getAttrsBuffed(Attr.ASM);
            }
        }

        this.totalFPL = ChatFormatting.RED + String.format("%.1f",floatFPL);
        this.totalFPH = ChatFormatting.GREEN + String.format("%.1f",floatFPH);
        this.totalFPAL = ChatFormatting.RED + String.format("%.1f",floatFPAL);
        this.totalFPAH = ChatFormatting.GREEN + String.format("%.1f",floatFPAH);
        this.totalFPAA = ChatFormatting.YELLOW + String.format("%.1f",floatFPAA);
        this.totalFPASM = ChatFormatting.AQUA + String.format("%.1f",floatFPASM);

        //update unit name
        this.strUnitName = "\"" + this.capa.getUnitName(this.state.team) + "\"";
    }

private void handleHoveringText()
    {
        int mx = xMouse - leftPos;
        int my = yMouse - topPos;
        int len = 0;
        int range = 5;
        String str = null;

        //reset text
        mouseoverList.clear();

        //draw attributes
        if (shipList[state.selectedRow] != null && shipList[state.selectedRow].getFormatType() > 0 &&
           mx > 3 && mx < 138 && my > 43 && my < 145)
        {
            //column 1
            if (mx < 51)
            {
                if (my < this.barRows[0] + range)
                {
                    str = getAttributeString(Attr.ATK_L);
                    mouseoverList.add(str);
                }
                else if (my < this.barRows[1] + range)
                {
                    str = getAttributeString(Attr.ATK_H);
                    mouseoverList.add(str);
                }
                else if (my < this.barRows[2] + range)
                {
                    str = getAttributeString(Attr.ATK_AL);
                    mouseoverList.add(str);
                }
                else if (my < this.barRows[3] + range)
                {
                    str = getAttributeString(Attr.ATK_AH);
                    mouseoverList.add(str);
                }
                else if (my < this.barRows[4] + range)
                {
                    str = getAttributeString(Attr.SPD);
                    mouseoverList.add(str);
                }
                else if (my < this.barRows[5] + range)
                {
                    str = getAttributeString(Attr.HIT);
                    mouseoverList.add(str);
                }
            }
            //column 2
            else if (mx < 94)
            {
                if (my < this.barRows[0] + range)
                {
                    str = getAttributeString(Attr.CRI);
                    mouseoverList.add(str);
                }
                else if (my < this.barRows[1] + range)
                {
                    str = getAttributeString(Attr.DHIT);
                    mouseoverList.add(str);
                }
                else if (my < this.barRows[2] + range)
                {
                    str = getAttributeString(Attr.THIT);
                    mouseoverList.add(str);
                }
                else if (my < this.barRows[3] + range)
                {
                    str = getAttributeString(Attr.MISS);
                    mouseoverList.add(str);
                }
                else if (my < this.barRows[4] + range)
                {
                    str = getAttributeString(Attr.AA);
                    mouseoverList.add(str);
                }
                else if (my < this.barRows[5] + range)
                {
                    str = getAttributeString(Attr.ASM);
                    mouseoverList.add(str);
                }
            }
            //column 3
            else
            {
                if (my < this.barRows[0] + range)
                {
                    str = getAttributeString(Attr.DEF);
                    mouseoverList.add(str);
                }
                else if (my < this.barRows[1] + range)
                {
                    str = getAttributeString(Attr.DODGE);
                    mouseoverList.add(str);
                }
                else if (my < this.barRows[2] + range)
                {
                    str = getAttributeString(Attr.GRUDGE);
                    mouseoverList.add(str);
                }
                else if (my < this.barRows[3] + range)
                {
                    str = getAttributeString(Attr.HPRES);
                    mouseoverList.add(str);
                }
                else if (my < this.barRows[4] + range)
                {
                    str = getAttributeString(Attr.KB);
                    mouseoverList.add(str);
                }
                else if (my < this.barRows[5] + range)
                {
                    str = getAttributeString(Attr.MOV);
                    mouseoverList.add(str);
                }
            }

            this.drawHoveringText(mouseoverList, mx, my+10, this.font);
        }

        //draw total damage
        if (mx > 45 && mx < 138 && my > 3 && my < 43)
        {
            mouseoverList.clear();
            mouseoverList.add(attrTotalFP);
            mouseoverList.add(attrATKL);
            mouseoverList.add(attrATKH);
            mouseoverList.add(attrAIRL);
            mouseoverList.add(attrAIRH);
            mouseoverList.add(attrAA);
            mouseoverList.add(attrASM);

            this.drawHoveringText(mouseoverList, mx, my+10, this.font);

            mouseoverList.clear();
            mouseoverList.add("");
            mouseoverList.add(this.totalFPL);
            mouseoverList.add(this.totalFPH);
            mouseoverList.add(this.totalFPAL);
            mouseoverList.add(this.totalFPAH);
            mouseoverList.add(this.totalFPAA);
            mouseoverList.add(this.totalFPASM);

            this.drawHoveringText(mouseoverList, mx+strLen+6, my+10, this.font);
        }
    }

private void drawFormationPosSpot()
    {
        //calc spot position
        int dist = 0;
        for (int j = 0; j < 6; j++)
        {
            if (spotPosFinal[0][j] != spotPos[0][j])
            {
                dist = spotPosFinal[0][j] - spotPos[0][j];
                spotPos[0][j] = dist > 0 ? spotPos[0][j] + 1 : spotPos[0][j] - 1;
            }

            if (spotPosFinal[1][j] != spotPos[1][j])
            {
                dist = spotPosFinal[1][j] - spotPos[1][j];
                spotPos[1][j] = dist > 0 ? spotPos[1][j] + 1 : spotPos[1][j] - 1;
            }
        }

        //draw spot animation
        int spotIconY = 192;
        for (int i = 0; i < 6; i++)
        {
            //set spot color
            if (i == this.state.selectedRow)
            {
                spotIconY = 195;
            }
            else
            {
                spotIconY = 192;
            }

            drawTexturedModalRect(leftPos+spotPos[0][i], topPos+spotPos[1][i], 0, spotIconY, 3, 3);
        }
    }

private void drawFormationBuffBar()
    {
        //draw basic bar
        for (int i = 0; i < 3; i++)
        {
            for (int j = 0; j < 6; j++)
            {
                drawTexturedModalRect(leftPos+barCols[i], topPos+barRows[j], 0, 220, barLength, 4);
            }
        }

        //bar animation
        int len = 0;
        int bx = 0;     //bar pos x
        int by = 0;     //bar pos y
        int icony = 0;  //bar icon y
        int cols1 = barCols[0] + barLength;
        int cols2 = barCols[1] + barLength;
        int cols3 = barCols[2] + barLength;

        for (int i = 0; i < LegacyShipStatTables.ATTR_COUNT; i++)
        {
            //bar length--
            if ((int)this.buffBar[i] > (int)this.buffBarFinal[i])
            {
                this.buffBar[i] -= 0.3F;
            }
            //bar length++
            else if ((int)this.buffBar[i] < (int)this.buffBarFinal[i])
            {
                this.buffBar[i] += 0.3F;
            }

            //get draw position
            switch (i)
            {
            case Attr.HP:
            case Attr.XP:
            case Attr.AMMO:  //ignore case
                continue;
            case Attr.ATK_L: bx = cols1; by = barRows[0];
            break;
            case Attr.ATK_H: bx = cols1; by = barRows[1];
            break;
            case Attr.ATK_AL: bx = cols1; by = barRows[2];
            break;
            case Attr.ATK_AH: bx = cols1; by = barRows[3];
            break;
            case Attr.SPD: bx = cols1; by = barRows[4];
            break;
            case Attr.HIT: bx = cols1; by = barRows[5];
            break;

            case Attr.CRI: bx = cols2; by = barRows[0];
            break;
            case Attr.DHIT: bx = cols2; by = barRows[1];
            break;
            case Attr.THIT: bx = cols2; by = barRows[2];
            break;
            case Attr.MISS: bx = cols2; by = barRows[3];
            break;
            case Attr.AA: bx = cols2; by = barRows[4];
            break;
            case Attr.ASM: bx = cols2; by = barRows[5];
            break;

            case Attr.DEF: bx = cols3; by = barRows[0];
            break;
            case Attr.DODGE: bx = cols3; by = barRows[1];
            break;
            case Attr.GRUDGE: bx = cols3; by = barRows[2];
            break;
            case Attr.HPRES: bx = cols3; by = barRows[3];
            break;
            case Attr.KB: bx = cols3; by = barRows[4];
            break;
            case Attr.MOV: bx = cols3; by = barRows[5];
            break;
            }

            //draw red bar
            if (this.buffBar[i] > 0)
            {
                len = (int)this.buffBar[i];
                icony = 230;
                drawTexturedModalRect(leftPos+bx, topPos+by, 0, 230, len, 4);
            }
            //draw blue bar
            else if (this.buffBar[i] < 0)
            {
                len = (int)(-this.buffBar[i]);
                icony = 225;
                drawTexturedModalRect(leftPos+bx-len, topPos+by, 0, 225, len, 4);
            }
        }
    }

private void drawFormationText()
    {
        String str = null;
        int len = 0;

        //draw button text
        len = (int) (this.font.width(strRadar) * 0.5F);
        drawStringWithShadow(strRadar, 70-len, 182, 16776960);

        //draw ship name
        if (this.capa != null)
        {
            //icon setting
            graphics.pose().pushPose();
            graphics.pose().scale(0.75F, 0.75F, 0.75F);

            //draw ship list
            if (this.shipList != null)
            {
                int texty = 14;

                for (int i = 0; i < 6; i++)
                {
                    if (shipList[i] != null)
                    {
                        //draw name
                        drawString(shipName[i], 210, texty, 16777215);
                        texty += 14;

                        //draw pos
                        str = ChatFormatting.AQUA + "LV " + ChatFormatting.YELLOW +
                              shipList[i].getLevel() + "   " + ChatFormatting.GOLD +
                              (int)shipList[i].getHealth() + " / " + ChatFormatting.RED +
                              (int)shipList[i].getMaxHealth();
                        drawString(str, 195, texty, 0);
                        texty += 22;
                    }
                    else
                    {
                        str = strNoSig + ChatFormatting.GRAY + " UID: " + this.capa.getSID(this.capa.getCurrentTeamId(), i);
                        drawString(str, 195, texty, 0);
                        texty += 36;
                    }
                }
            }

            //draw formation name
            str = ChatFormatting.YELLOW+I18n.get("gui.shincolle.formation.format"+this.state.formation);
            len = (int) (font.width(str) * 0.5F);
            drawString(str, 115-len, 18, 16777215);

            str = ChatFormatting.LIGHT_PURPLE+strPos+" "+ChatFormatting.WHITE+(this.state.selectedRow+1);
            len = (int) (font.width(str) * 0.5F);
            drawString(str, 115-len, 30, 16777215);

            //draw attribute text
            drawStringWithShadow(attrATKL, 12, 60, 16777215);
            drawStringWithShadow(attrATKH, 12, 80, 16777215);
            drawStringWithShadow(attrAIRL, 12, 100, 16777215);
            drawStringWithShadow(attrAIRH, 12, 120, 16777215);
            drawStringWithShadow(attrSPD, 12, 140, 16777215);
            drawStringWithShadow(attrHIT, 12, 160, 16777215);
            drawStringWithShadow(attrCRI, 69, 60, 16777215);
            drawStringWithShadow(attrDHIT, 69, 80, 16777215);
            drawStringWithShadow(attrTHIT, 69, 100, 16777215);
            drawStringWithShadow(attrMISS, 69, 120, 16777215);
            drawStringWithShadow(attrAA, 69, 140, 16777215);
            drawStringWithShadow(attrASM, 69, 160, 16777215);
            drawStringWithShadow(attrDEF, 126, 60, 16777215);
            drawStringWithShadow(attrDODGE, 126, 80, 16777215);
            drawStringWithShadow(attrGRUDGE, 126, 100, 16777215);
            drawStringWithShadow(attrHPRES, 126, 120, 16777215);
            drawStringWithShadow(attrKB, 126, 140, 16777215);
            drawStringWithShadow(attrMOV, 126, 160, 16777215);

            graphics.pose().popPose();
        }//draw ship name
    }

private void setFormationSpotPos(int fid)
    {
        switch (fid)
        {
        case 1:   //line ahead
            spotPosFinal[0][0] = 25;  //pos 0
            spotPosFinal[1][0] = 9;
            spotPosFinal[0][1] = 25;  //pos 1
            spotPosFinal[1][1] = 15;
            spotPosFinal[0][2] = 25;  //pos 2
            spotPosFinal[1][2] = 21;
            spotPosFinal[0][3] = 25;  //pos 3
            spotPosFinal[1][3] = 27;
            spotPosFinal[0][4] = 25;  //pos 4
            spotPosFinal[1][4] = 33;
            spotPosFinal[0][5] = 25;  //pos 5
            spotPosFinal[1][5] = 39;
        break;
        case 2:   //double line
            spotPosFinal[0][0] = 21;  //pos 0
            spotPosFinal[1][0] = 25;
            spotPosFinal[0][1] = 29;  //pos 1
            spotPosFinal[1][1] = 25;
            spotPosFinal[0][2] = 21;  //pos 2
            spotPosFinal[1][2] = 16;
            spotPosFinal[0][3] = 29;  //pos 3
            spotPosFinal[1][3] = 16;
            spotPosFinal[0][4] = 21;  //pos 4
            spotPosFinal[1][4] = 34;
            spotPosFinal[0][5] = 29;  //pos 5
            spotPosFinal[1][5] = 34;
        break;
        case 3:   //diamond
            spotPosFinal[0][0] = 25;  //pos 0
            spotPosFinal[1][0] = 29;
            spotPosFinal[0][1] = 25;  //pos 1
            spotPosFinal[1][1] = 15;
            spotPosFinal[0][2] = 15;  //pos 2
            spotPosFinal[1][2] = 26;
            spotPosFinal[0][3] = 35;  //pos 3
            spotPosFinal[1][3] = 26;
            spotPosFinal[0][4] = 25;  //pos 4
            spotPosFinal[1][4] = 36;
            spotPosFinal[0][5] = 25;  //pos 5
            spotPosFinal[1][5] = 23;
        break;
        case 4:   //echelon
            spotPosFinal[0][0] = 40;  //pos 0
            spotPosFinal[1][0] = 9;
            spotPosFinal[0][1] = 34;  //pos 1
            spotPosFinal[1][1] = 15;
            spotPosFinal[0][2] = 28;  //pos 2
            spotPosFinal[1][2] = 21;
            spotPosFinal[0][3] = 22;  //pos 3
            spotPosFinal[1][3] = 27;
            spotPosFinal[0][4] = 16;  //pos 4
            spotPosFinal[1][4] = 33;
            spotPosFinal[0][5] = 10;  //pos 5
            spotPosFinal[1][5] = 39;
        break;
        case 5:   //line abreast
            spotPosFinal[0][0] = 40;  //pos 0
            spotPosFinal[1][0] = 25;
            spotPosFinal[0][1] = 34;  //pos 1
            spotPosFinal[1][1] = 25;
            spotPosFinal[0][2] = 28;  //pos 2
            spotPosFinal[1][2] = 25;
            spotPosFinal[0][3] = 22;  //pos 3
            spotPosFinal[1][3] = 25;
            spotPosFinal[0][4] = 16;  //pos 4
            spotPosFinal[1][4] = 25;
            spotPosFinal[0][5] = 10;  //pos 5
            spotPosFinal[1][5] = 25;
        break;
        default:  //no formation
            spotPosFinal[0][0] = 25;  //pos 0
            spotPosFinal[1][0] = 25;
            spotPosFinal[0][1] = 25;  //pos 1
            spotPosFinal[1][1] = 25;
            spotPosFinal[0][2] = 25;  //pos 2
            spotPosFinal[1][2] = 25;
            spotPosFinal[0][3] = 25;  //pos 3
            spotPosFinal[1][3] = 25;
            spotPosFinal[0][4] = 25;  //pos 4
            spotPosFinal[1][4] = 25;
            spotPosFinal[0][5] = 25;  //pos 5
            spotPosFinal[1][5] = 25;
        break;
        }//end switch formation spot
    }

private void setFormationBuffBar(int fid, int pos)
    {
        //get buff value
        float[] value = this.menu.getFormationBuffValue(fid, pos);
        float lenModify = 20F;

        //calc bar length, discard HP, XP, AMMO
        this.buffBarFinal[Attr.ATK_L] = (value[Attr.ATK_L] - 1F) * lenModify;
        this.buffBarFinal[Attr.ATK_H] = (value[Attr.ATK_H] - 1F) * lenModify;
        this.buffBarFinal[Attr.ATK_AL] = (value[Attr.ATK_AL] - 1F) * lenModify;
        this.buffBarFinal[Attr.ATK_AH] = (value[Attr.ATK_AH] - 1F) * lenModify;
        this.buffBarFinal[Attr.DEF] = (value[Attr.DEF] - 1F) * lenModify;
        this.buffBarFinal[Attr.SPD] = (value[Attr.SPD] - 1F) * lenModify;
        this.buffBarFinal[Attr.MOV] = value[Attr.MOV] / 0.5F * lenModify;
        this.buffBarFinal[Attr.HIT] = value[Attr.HIT] / 10F * lenModify;
        this.buffBarFinal[Attr.CRI] = (value[Attr.CRI] - 1F) * lenModify;
        this.buffBarFinal[Attr.DHIT] = (value[Attr.DHIT] - 1F) * lenModify;
        this.buffBarFinal[Attr.THIT] = (value[Attr.THIT] - 1F) * lenModify;
        this.buffBarFinal[Attr.MISS] = (value[Attr.MISS] - 1F) * lenModify;
        this.buffBarFinal[Attr.AA] = (value[Attr.AA] - 1F) * lenModify;
        this.buffBarFinal[Attr.ASM] = (value[Attr.ASM] - 1F) * lenModify;
        this.buffBarFinal[Attr.DODGE] = value[Attr.DODGE] * lenModify;
        this.buffBarFinal[Attr.GRUDGE] = value[Attr.GRUDGE] * lenModify;
        this.buffBarFinal[Attr.HPRES] = value[Attr.HPRES] * lenModify;
        this.buffBarFinal[Attr.KB] = value[Attr.KB] * lenModify;
    }

private void setShipList(int tid)
    {
        try
        {
            //has formation, set ship pos by formatPos
            if (this.capa.getFormationId(tid) > 0)
            {
                this.shipList = new ShipView[6];
                ShipView[] temp = this.capa.getShipEntityAll(this.state.team);

                if (temp != null)
                {
                    for (ShipView s : temp)
                    {
                        if (s != null) this.shipList[s.getFormatPos()] = s;
                    }
                }
            }
            //no formation
            else
            {
                this.shipList = this.capa.getShipEntityAll(this.state.team);
            }
        }
        catch (Exception e)
        {
            LOGGER.warn("EXCEPTION: formation GUI get ship fail: "+e);
            e.printStackTrace();
        }
    }
}
