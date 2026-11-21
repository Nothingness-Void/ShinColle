package com.lulan.shincolle.client.screen;

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
    private static String lvMark, hpMark, strAttrATK, strAttrAIR, strAttrDEF, strAttrSPD, strAttrMOV,
      strAttrHIT, strAttrCri, strAttrDHIT, strAttrTHIT, strAttrAA, strAttrASM, strAttrMiss,
      strAttrMissR, strAttrDodge, strAttrFPos, strAttrFormat, strAttrWedding,
      strAttrWedTrue, strAttrWedFalse, strMiKills, strMiExp, strMiAirL, strMiAirH, strMiAmmoL,
      strMiAmmoH, strMiGrudge, canMelee, canLATK, canHATK, canALATK, canAHATK, auraEffect,
      followMin, followMax, fleeHP, tarAI, strOnSight, strPVP, strAA, strASM, strTimeKeep,
      strPick, strWpStay, strAttrModern, strAttrXP, strAttrGrudge, strAttrAmmo, strAttrHPRES,
      strShowHeld, strAutoCR, strAutoPump, strAttrKB, strAttrHP, strAppear, strMeta, strDict,
      strNbt, strInput, strOutput, strFuel, strCook, strFish, strMine, strCraft;
    private static String[] strMorale;
    private static int widthHoveringText1, widthHoveringText2, widthHoveringText3;

    private List<String> mouseoverList;
    private String titlename, shiplevel,
                   strATK, strAATK, strLATK, strHATK, strALATK, strAHATK, strDEF, strSPD, strMOV, strHIT,
                   Kills, Exp, Grudge, Owner, AmmoLight, AmmoHeavy, AirLight, AirHeavy, overText, marriage,
                   followMinValue, followMaxValue, fleeHPValue, barPosValue, Formation, strWpStayValue;
    private int hpCurrent, hpMax, color,   pageIndicator, pageIndicatorAI,
                fMinPos, fMaxPos, fleeHPPos,   shipType, shipClass,
                wpStayPos, autoCRPos;
    private float xMouse, yMouse;
            private int[][] iconXY;  //icon array:  [ship type, ship name][file,x,y]


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

        if ((this.shipClass == 53 || this.shipClass == 54)
                && this.entity.getRidingState() > 1 && this.shipRiding[0] != null) {
            this.shipType = 2;
            this.shipClass = 55;
            if (this.shipRiding[0].getVehicle() instanceof LegacyShipEntity mount
                    && mount.getSpec().legacyClassId() == 54) {
                this.shipRiding[2] = mount;
            } else if (!this.shipRiding[0].getPassengers().isEmpty()
                    && this.shipRiding[0].getPassengers().get(0) instanceof LegacyShipEntity rider
                    && rider.getSpec().legacyClassId() == 53) {
                this.shipRiding[1] = rider;
            }
        }

        this.iconXY = new int[][] {LegacyShipInventoryCatalog.typeIcon(this.shipType),
                LegacyShipInventoryCatalog.nameIcon(this.shipClass)};

        //general string
        lvMark = I18n.get("gui.shincolle:level");
        hpMark = I18n.get("gui.shincolle:hp");
        strMorale = new String[5];
        strMorale[0] = I18n.get("gui.shincolle:morale0");
        strMorale[1] = I18n.get("gui.shincolle:morale1");
        strMorale[2] = I18n.get("gui.shincolle:morale2");
        strMorale[3] = I18n.get("gui.shincolle:morale3");
        strMorale[4] = I18n.get("gui.shincolle:morale4");

        //attrs string
        strAttrHP = I18n.get("gui.shincolle:hp");
        strAttrATK = I18n.get("gui.shincolle:firepower1");
        strAttrAIR = I18n.get("gui.shincolle:firepower2");
        strAttrDEF = I18n.get("gui.shincolle:armor");
        strAttrSPD = I18n.get("gui.shincolle:attackspeed");
        strAttrMOV = I18n.get("gui.shincolle:movespeed");
        strAttrHIT = I18n.get("gui.shincolle:range");
        strAttrCri = I18n.get("gui.shincolle:critical");
        strAttrDHIT = I18n.get("gui.shincolle:doublehit");
        strAttrTHIT = I18n.get("gui.shincolle:triplehit");
        strAttrAA = I18n.get("gui.shincolle:antiair");
        strAttrASM = I18n.get("gui.shincolle:antiss");
        strAttrMiss = I18n.get("gui.shincolle:missrate");
        strAttrMissR = I18n.get("gui.shincolle:missreduce");
        strAttrDodge = I18n.get("gui.shincolle:dodge");
        strAttrFPos = I18n.get("gui.shincolle:formation.position");
        strAttrFormat = I18n.get("gui.shincolle:formation.formation");
        strAttrWedding = I18n.get("gui.shincolle:marriage");
        strAttrWedTrue = I18n.get("gui.shincolle:married");
        strAttrWedFalse = I18n.get("gui.shincolle:unmarried");
        strAttrModern = I18n.get("gui.shincolle:modernlevel");
        strAttrXP = I18n.get("gui.shincolle:equip.xp");
        strAttrGrudge = I18n.get("gui.shincolle:equip.grudge");
        strAttrAmmo = I18n.get("gui.shincolle:equip.ammo");
        strAttrHPRES = I18n.get("gui.shincolle:equip.hpres");
        strAttrKB = I18n.get("gui.shincolle:equip.kb");

        //minor string
        strMiKills = I18n.get("gui.shincolle:kills");
        strMiExp = I18n.get("gui.shincolle:exp");
        strMiAmmoL = I18n.get("gui.shincolle:ammolight");
        strMiAmmoH = I18n.get("gui.shincolle:ammoheavy");
        strMiGrudge = I18n.get("gui.shincolle:grudge");
        strMiAirL = I18n.get("gui.shincolle:airplanelight");
        strMiAirH = I18n.get("gui.shincolle:airplaneheavy");

        //AI string
        canMelee = I18n.get("gui.shincolle:canmelee");
        canLATK = I18n.get("gui.shincolle:canlightattack");
        canHATK = I18n.get("gui.shincolle:canheavyattack");
        canALATK = I18n.get("gui.shincolle:canairlightattack");
        canAHATK = I18n.get("gui.shincolle:canairheavyattack");
        auraEffect = I18n.get("gui.shincolle:auraeffect");
        followMin = I18n.get("gui.shincolle:followmin");
        followMax = I18n.get("gui.shincolle:followmax");
        fleeHP = I18n.get("gui.shincolle:fleehp");
        tarAI = I18n.get("gui.shincolle:targetAI");
        strOnSight = I18n.get("gui.shincolle:onsightAI");
        strPVP = I18n.get("gui.shincolle:ai.pvp");
        strAA = I18n.get("gui.shincolle:ai.aa");
        strASM = I18n.get("gui.shincolle:ai.asm");
        strTimeKeep = I18n.get("gui.shincolle:ai.timekeeper");
        strPick = I18n.get("gui.shincolle:ai.pickitem");
        strWpStay = I18n.get("gui.shincolle:ai.wpstay");
        strShowHeld = I18n.get("gui.shincolle:showhelditem");
        strAutoCR = I18n.get("gui.shincolle:autocombatration");
        strAutoPump = I18n.get("gui.shincolle:autopump");
        strAppear = I18n.get("gui.shincolle:appearance");
        strMeta = I18n.get("gui.shincolle:crane.usemeta");
        strDict = I18n.get("gui.shincolle:crane.useoredict");
        strNbt = I18n.get("gui.shincolle:crane.usenbt");
        strInput = I18n.get("gui.shincolle:ai.inputside");
        strOutput = I18n.get("gui.shincolle:ai.outputside");
        strFuel = I18n.get("gui.shincolle:ai.fuelside");
        strCook = I18n.get("gui.shincolle:ai.cooking");
        strFish = I18n.get("gui.shincolle:ai.fishing");
        strMine = I18n.get("gui.shincolle:ai.mining");
        strCraft = I18n.get("gui.shincolle:ai.crafting");
    }

    @Override
    protected void init()
    {
        super.init();

        //get max string width for hovering text drawing
        int temp = this.fontRendererObj.getStringWidth(strAttrATK);
        this.widthHoveringText1 = temp;
        temp = this.fontRendererObj.getStringWidth(strAttrAIR);
        if (temp > this.widthHoveringText1) this.widthHoveringText1 = temp;
        temp = this.fontRendererObj.getStringWidth(strAttrCri);
        if (temp > this.widthHoveringText1) this.widthHoveringText1 = temp;
        temp = this.fontRendererObj.getStringWidth(strAttrDHIT);
        if (temp > this.widthHoveringText1) this.widthHoveringText1 = temp;
        temp = this.fontRendererObj.getStringWidth(strAttrTHIT);
        if (temp > this.widthHoveringText1) this.widthHoveringText1 = temp;
        temp = this.fontRendererObj.getStringWidth(strAttrAA);
        if (temp > this.widthHoveringText1) this.widthHoveringText1 = temp;
        temp = this.fontRendererObj.getStringWidth(strAttrASM);
        if (temp > this.widthHoveringText1) this.widthHoveringText1 = temp;
        temp = this.fontRendererObj.getStringWidth(strAttrModern);
        if (temp > this.widthHoveringText1) this.widthHoveringText1 = temp;
        temp = this.fontRendererObj.getStringWidth(strAttrXP);
        if (temp > this.widthHoveringText1) this.widthHoveringText1 = temp;
        temp = this.fontRendererObj.getStringWidth(strAttrGrudge);
        if (temp > this.widthHoveringText1) this.widthHoveringText1 = temp;
        temp = this.fontRendererObj.getStringWidth(strAttrAmmo);
        if (temp > this.widthHoveringText1) this.widthHoveringText1 = temp;
        temp = this.fontRendererObj.getStringWidth(strAttrHPRES);
        if (temp > this.widthHoveringText1) this.widthHoveringText1 = temp;
        temp = this.fontRendererObj.getStringWidth(strAttrMiss);
        if (temp > this.widthHoveringText1) this.widthHoveringText1 = temp;
        temp = this.fontRendererObj.getStringWidth(strAttrDodge);
        if (temp > this.widthHoveringText1) this.widthHoveringText1 = temp;
        temp = this.fontRendererObj.getStringWidth(strAttrKB);
        if (temp > this.widthHoveringText1) this.widthHoveringText1 = temp;
        temp = this.fontRendererObj.getStringWidth(strAttrHP);
        if (temp > this.widthHoveringText1) this.widthHoveringText1 = temp;

        temp = this.fontRendererObj.getStringWidth(strAttrFPos);
        this.widthHoveringText3 = temp;
        temp = this.fontRendererObj.getStringWidth(strAttrATK);
        if (temp > this.widthHoveringText3) this.widthHoveringText3 = temp;
        temp = this.fontRendererObj.getStringWidth(strAttrAIR);
        if (temp > this.widthHoveringText3) this.widthHoveringText3 = temp;
        temp = this.fontRendererObj.getStringWidth(strAttrDEF);
        if (temp > this.widthHoveringText3) this.widthHoveringText3 = temp;
        temp = this.fontRendererObj.getStringWidth(strAttrDodge);
        if (temp > this.widthHoveringText3) this.widthHoveringText3 = temp;
        temp = this.fontRendererObj.getStringWidth(strAttrMissR);
        if (temp > this.widthHoveringText3) this.widthHoveringText3 = temp;
        temp = this.fontRendererObj.getStringWidth(strAttrCri);
        if (temp > this.widthHoveringText3) this.widthHoveringText3 = temp;
        temp = this.fontRendererObj.getStringWidth(strAttrDHIT);
        if (temp > this.widthHoveringText3) this.widthHoveringText3 = temp;
        temp = this.fontRendererObj.getStringWidth(strAttrTHIT);
        if (temp > this.widthHoveringText3) this.widthHoveringText3 = temp;
        temp = this.fontRendererObj.getStringWidth(strAttrAA);
        if (temp > this.widthHoveringText3) this.widthHoveringText3 = temp;
        temp = this.fontRendererObj.getStringWidth(strAttrASM);
        if (temp > this.widthHoveringText3) this.widthHoveringText3 = temp;
        temp = this.fontRendererObj.getStringWidth(strAttrMOV);
        if (temp > this.widthHoveringText3) this.widthHoveringText3 = temp;
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY)
    {
        //取得gui顯示名稱
        titlename = entity.getCustomNameTag();  //get type name from nbt

        //畫出字串 parm: string, x, y, color, (是否dropShadow)
        //draw entity name (title)
        this.fontRendererObj.drawString(titlename, 8, 6, 0);

        drawAttributes();

        handleHoveringText();
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY)
    {
        //reset color & draw background


        this.boundTexture = TEXTURE_BG;
        drawTexturedModalRect(leftPos, topPos, 0, 0, imageWidth, imageHeight);

        //draw banned inventory icon
        switch (this.entity.getInventoryPageSize())
        {
        case 2:
            break;
        case 1:
            drawTexturedModalRect(leftPos+62, topPos+90, 80, 214, 6, 34);
            break;
        default:
            drawTexturedModalRect(leftPos+62, topPos+54, 80, 214, 6, 34);
            drawTexturedModalRect(leftPos+62, topPos+90, 80, 214, 6, 34);
            break;
        }

        //draw inventory page indicator
        this.controls.showPageInv = this.entity.getInventoryPage();

        switch (this.controls.showPageInv)
        {
        case 1:  //page 1
            this.pageIndicator = 54;
            break;
        case 2:  //page 2
            this.pageIndicator = 90;
            break;
        default: //page 0
            this.pageIndicator = 18;
            break;
        }
        drawTexturedModalRect(leftPos+62, topPos+this.pageIndicator, 74, 214, 6, 34);

        //draw page indicator
        switch (this.controls.showPage)
        {
        case 1: //page 1
            this.pageIndicator = 18;
            break;
        case 2: //page 2
            this.pageIndicator = 54;
            break;
        case 3: //page 3
            this.pageIndicator = 90;
            break;
        }
        drawTexturedModalRect(leftPos+135, topPos+this.pageIndicator, 74, 214, 6, 34);

        //draw AI page indicator
        switch (this.controls.showPageAI)
        {
        case 1:
        {   //page 1
            this.pageIndicator = 239;
            this.pageIndicatorAI = 131;

            //get button value
            this.controls.switchPage1a[0] = this.entity.getStateFlag("UseMelee");
            this.controls.switchPage1a[1] = this.entity.getStateFlag("UseAmmoLight");
            this.controls.switchPage1a[2] = this.entity.getStateFlag("UseAmmoHeavy");
            this.controls.switchPage1a[3] = this.entity.getStateFlag("UseAirLight");
            this.controls.switchPage1a[4] = this.entity.getStateFlag("UseAirHeavy");
            this.controls.switchPage1a[5] = this.entity.getStateFlag("UseRingEffect");

            //get button display flag
            this.controls.switchPage1b[0] = true;
            this.controls.switchPage1b[1] = this.entity.getStateFlag("AtkType_Light");
            this.controls.switchPage1b[2] = this.entity.getStateFlag("AtkType_Heavy");
            this.controls.switchPage1b[3] = this.entity.getStateFlag("AtkType_AirLight");
            this.controls.switchPage1b[4] = this.entity.getStateFlag("AtkType_AirHeavy");
            this.controls.switchPage1b[5] = this.entity.getStateFlag("HaveRingEffect");

            //draw button
            int iconY = 131;

            for (int i = 0; i < 6; i++)
            {
                if (this.controls.switchPage1b[i])      //can draw button
                {
                    if (this.controls.switchPage1a[i])  //draw button
                    {
                        drawTexturedModalRect(leftPos + 174, topPos + iconY, 0, 214, 11, 11);
                    }
                    else
                    {
                        drawTexturedModalRect(leftPos + 174, topPos + iconY, 11, 214, 11, 11);
                    }
                }

                iconY += 13;
            }

            break;
        }
        case 2:
        {   //page 2
            this.pageIndicator = 239;
            this.pageIndicatorAI = 144;

            //get button value
            fMinPos = (int)(((entity.getStateMinor("FollowMin") - 1) / 30F) * 42F);
            fMaxPos = (int)(((entity.getStateMinor("FollowMax") - 2) / 30F) * 42F);
            fleeHPPos = (int)((entity.getStateMinor("FleeHP") / 100F) * 42F);

            //draw range bar
            drawTexturedModalRect(leftPos+191, topPos+148, 31, 214, 43, 3);
            drawTexturedModalRect(leftPos+191, topPos+172, 31, 214, 43, 3);
            drawTexturedModalRect(leftPos+191, topPos+196, 31, 214, 43, 3);

            //draw range indicator by mouse focus target
            switch (this.controls.mousePressBar)
            {
            case 0:
                drawTexturedModalRect(leftPos+187+this.controls.barPos, topPos+145, 22, 214, 9, 9);
                break;
            case 1:
                drawTexturedModalRect(leftPos+187+this.controls.barPos, topPos+169, 22, 214, 9, 9);
                break;
            case 2:
                drawTexturedModalRect(leftPos+187+this.controls.barPos, topPos+193, 22, 214, 9, 9);
                break;
            default:
                drawTexturedModalRect(leftPos+187+fMinPos, topPos+145, 22, 214, 9, 9);
                drawTexturedModalRect(leftPos+187+fMaxPos, topPos+169, 22, 214, 9, 9);
                drawTexturedModalRect(leftPos+187+fleeHPPos, topPos+193, 22, 214, 9, 9);
                break;
            }

            break;
        }
        case 3:
        {   //page 3
            this.pageIndicator = 239;
            this.pageIndicatorAI = 157;

            //get button value
            this.controls.switchPage3[0] = this.entity.getStateFlag("PassiveAI");
            this.controls.switchPage3[1] = this.entity.getStateFlag("OnSightChase");
            this.controls.switchPage3[2] = this.entity.getStateFlag("PVPFirst");
            this.controls.switchPage3[3] = this.entity.getStateFlag("AntiAir");
            this.controls.switchPage3[4] = this.entity.getStateFlag("AntiSS");
            this.controls.switchPage3[5] = this.entity.getStateFlag("TimeKeeper");

            //draw button
            int iconY = 131;

            for (boolean b : this.controls.switchPage3)
            {
                if (b)
                {
                    drawTexturedModalRect(leftPos + 174, topPos + iconY, 0, 214, 11, 11);
                }
                else
                {
                    drawTexturedModalRect(leftPos + 174, topPos + iconY, 11, 214, 11, 11);
                }

                iconY += 13;
            }

            break;
        }
        case 4:
        {   //page 4
            this.pageIndicator = 239;
            this.pageIndicatorAI = 170;

            //get button value
            this.controls.switchPage4[0] = this.entity.getStateFlag("PickItem");
            this.controls.switchPage4[1] = this.entity.getStateFlag("AutoPump");

            //draw button
            if (this.entity.getStateFlag("CanPickItem"))
            {
                if (this.controls.switchPage4[0])
                {
                    drawTexturedModalRect(leftPos+174, topPos+131, 0, 214, 11, 11);
                }
                else
                {
                    drawTexturedModalRect(leftPos+174, topPos+131, 11, 214, 11, 11);
                }
            }

            if (this.controls.switchPage4[1])
            {
                drawTexturedModalRect(leftPos+174, topPos+144, 0, 214, 11, 11);
            }
            else
            {
                drawTexturedModalRect(leftPos+174, topPos+144, 11, 214, 11, 11);
            }

            break;
        }
        case 5:
        {   //page 5
            this.pageIndicator = 239;
            this.pageIndicatorAI = 183;

            //get button value
            wpStayPos = (int)(entity.getStateMinor("WpStay") * 0.0625F * 42F);
            autoCRPos = (int)((entity.getStateMinor("UseCombatRation") - 1) * 14F);

            //draw range bar
            drawTexturedModalRect(leftPos+191, topPos+148, 31, 214, 43, 3);
            drawTexturedModalRect(leftPos+191, topPos+172, 31, 214, 43, 3);

            //draw range indicator by mouse focus target
            switch (this.controls.mousePressBar)
            {
            case 3:
                drawTexturedModalRect(leftPos+187+this.controls.barPos, topPos+145, 22, 214, 9, 9);
                break;
            case 4:
                drawTexturedModalRect(leftPos+187+this.controls.barPos, topPos+169, 22, 214, 9, 9);
                break;
            default:
                drawTexturedModalRect(leftPos+187+wpStayPos, topPos+145, 22, 214, 9, 9);
                drawTexturedModalRect(leftPos+187+autoCRPos, topPos+169, 22, 214, 9, 9);
                break;
            }

            break;
        }
        case 6:
        {   //page 6
            this.pageIndicator = 239;
            this.pageIndicatorAI = 196;

            //get button value
            this.controls.switchPage6[0] = this.entity.getStateFlag("ShowHeldItem");

            int modelstate = this.entity.getStateEmotion("State");
            int maxstate = this.entity.getStateMinor("NumState");

            for (int i = 0; i < maxstate; i++)
            {
                this.controls.switchPage6[i + 1] = (modelstate & POW2[i]) == POW2[i] ? true : false;
            }

            //draw button 0
            if (this.controls.switchPage6[0])
            {
                drawTexturedModalRect(leftPos + 174, topPos + 131, 0, 214, 11, 11);
            }
            else
            {
                drawTexturedModalRect(leftPos + 174, topPos + 131, 11, 214, 11, 11);
            }

            //draw button 1~16
            int numbtn = 0;

            for (int i = 0; i < 4; i++)
            {
                //check button number
                if (numbtn > this.controls.maxBtn) break;

                for (int j = 0; j < 4; j++)
                {
                    //check button number
                    if (++numbtn > this.controls.maxBtn) break;

                    if (this.controls.switchPage6[i * 4 + j + 1])
                    {
                        drawTexturedModalRect(leftPos + 176 + j * 16, topPos + 157 + i * 13, 0, 214, 11, 11);
                    }
                    else
                    {
                        drawTexturedModalRect(leftPos + 176 + j * 16, topPos + 157 + i * 13, 11, 214, 11, 11);
                    }
                }
            }

            break;
        }
        case 7:  //page 7
        {
            this.pageIndicator = 246;
            this.pageIndicatorAI = 131;

            //get button value
            int tside = this.entity.getStateMinor("TaskSide");

            //draw button: task icon
            drawTexturedModalRect(leftPos + 174, topPos + 136, 87, 214, 64, 16);
            drawTexturedModalRect(leftPos + 174, topPos + 138, 151, 237, 64, 16);

            switch (this.entity.getStateMinor("Task"))
            {
            case 1:  //cooking
                drawTexturedModalRect(leftPos + 174, topPos + 136, 87, 230, 16, 16);
            break;
            case 2:  //fishing
                drawTexturedModalRect(leftPos + 190, topPos + 136, 103, 230, 16, 16);
            break;
            case 3:  //mining
                drawTexturedModalRect(leftPos + 206, topPos + 136, 119, 230, 16, 16);
            break;
            case 4:  //crafting
                drawTexturedModalRect(leftPos + 222, topPos + 136, 135, 230, 16, 16);
            break;
            }

            //draw button: task setting
            if ((tside & POW2[18]) == POW2[18])
            {
                drawTexturedModalRect(leftPos + 177, topPos + 157, 0, 236, 11, 11);
            }
            else
            {
                drawTexturedModalRect(leftPos + 177, topPos + 157, 0, 225, 11, 11);
            }

            if ((tside & POW2[19]) == POW2[19])
            {
                drawTexturedModalRect(leftPos + 177, topPos + 170, 11, 236, 11, 11);
            }
            else
            {
                drawTexturedModalRect(leftPos + 177, topPos + 170, 11, 225, 11, 11);
            }

            if ((tside & POW2[20]) == POW2[20])
            {
                drawTexturedModalRect(leftPos + 177, topPos + 183, 22, 236, 11, 11);
            }
            else
            {
                drawTexturedModalRect(leftPos + 177, topPos + 183, 22, 225, 11, 11);
            }
        }
        break;
        case 8:  //page 8
        {
            this.pageIndicator = 246;
            this.pageIndicatorAI = 144;

            //get button value
            int tside = this.entity.getStateMinor("TaskSide");

            //draw button
            drawTexturedModalRect(leftPos + 173, topPos + 144, 151, 214, 66, 11);
            drawTexturedModalRect(leftPos + 173, topPos + 170, 151, 214, 66, 11);
            drawTexturedModalRect(leftPos + 173, topPos + 196, 151, 214, 66, 11);

            for (int i = 0; i < 18; i++)
            {
                if ((tside & POW2[i]) == POW2[i])
                {
                    int dx = i % 6 * 11;
                    int dy = i / 6 * 26;
                    drawTexturedModalRect(leftPos + 173 + dx, topPos + 144 + dy, 151 + dx, 225, 11, 11);
                }
            }
        }
        break;
        case 9:  //page 9
        {
            this.pageIndicator = 246;
            this.pageIndicatorAI = 157;
        }
        break;
        case 10:  //page 10
        {
            this.pageIndicator = 246;
            this.pageIndicatorAI = 170;
        }
        break;
        case 11:  //page 11
        {
            this.pageIndicator = 246;
            this.pageIndicatorAI = 183;
        }
        break;
        case 12:  //page 12
        {
            this.pageIndicator = 246;
            this.pageIndicatorAI = 196;
        }
        break;
        }//end AI page switch

        //draw AI page indicator
        drawTexturedModalRect(leftPos + this.pageIndicator, topPos + this.pageIndicatorAI, 74, 214, 6, 11);

        //draw task icon and task slot background
        if (this.controls.showPageInv == 0)
        {
            switch (this.entity.getStateMinor("Task"))
            {
            case 1:  //cooking
                drawTexturedModalRect(leftPos + 25, topPos + 107, 33, 225, 18, 18);
                drawTexturedModalRect(leftPos + 26, topPos + 109, 151, 236, 18, 18);
            break;
            case 2:  //fishing
                drawTexturedModalRect(leftPos + 25, topPos + 107, 33, 225, 18, 18);
                drawTexturedModalRect(leftPos + 26, topPos + 109, 167, 236, 18, 18);
            break;
            case 3:  //mining
                drawTexturedModalRect(leftPos + 25, topPos + 107, 33, 225, 18, 18);
                drawTexturedModalRect(leftPos + 26, topPos + 109, 183, 236, 18, 18);
            break;
            case 4:  //crafting
                drawTexturedModalRect(leftPos + 25, topPos + 107, 33, 225, 18, 18);
                drawTexturedModalRect(leftPos + 26, topPos + 109, 199, 236, 18, 18);

                for (int i = 0; i < 9; i++)
                {
                    if (this.menu.slots.get(i + 12).getItem().isEmpty())
                    {
                        drawTexturedModalRect(leftPos + 7 + (i % 3) * 18, topPos + 53 + (i / 3) * 18, 33, 225, 18, 18);
                    }
                    else
                    {
                        drawTexturedModalRect(leftPos + 7 + (i % 3) * 18, topPos + 53 + (i / 3) * 18, 51, 225, 18, 18);
                    }
                }
            break;
            }
        }

        try
        {
            //draw type icon
            this.boundTexture = TEXTURE_ICON0;

            if (entity.getStateMinor("ShipLevel") > 99)
            {
                drawTexturedModalRect(leftPos+165, topPos+18, 0, 0, 40, 42);
                drawTexturedModalRect(leftPos+167, topPos+22, this.iconXY[0][0], this.iconXY[0][1], 28, 28);
            }
            else
            {
                drawTexturedModalRect(leftPos+165, topPos+18, 0, 43, 30, 30);
                drawTexturedModalRect(leftPos+165, topPos+18, this.iconXY[0][0], this.iconXY[0][1], 28, 28);
            }

            //draw ship morale
            drawIconMorale();

            //draw name icon
            int offx = 0;
            int offy = 0;

            if (iconXY[1][0] < 100)
            {
                this.boundTexture = TEXTURE_ICON1;
                if (iconXY[1][0] == 4) offy = -10;
            }
            else
            {
                this.boundTexture = TEXTURE_ICON2;
                if (iconXY[1][0] == 6) offy = -10;
                else offy = 10;
            }

            drawTexturedModalRect(leftPos+176+offx, topPos+63+offy, this.iconXY[1][1], this.iconXY[1][2], 11, 59);
        }
        catch (Exception e)
        {
            LOGGER.info("Exception : ship GUI: get name icon fail "+e);
        }

        //draw entity model                                            leftPos + 200 - xMouse  topPos + 50 - yMouse
        drawEntityModel(leftPos+218, topPos+100, entity.getModelPos(), leftPos+215-xMouse, topPos+60-yMouse, this.shipRiding);

    }

    private void drawIconMorale()
    {
        int ix = moraleLevel(this.entity.getMorale()) * 11;
        drawTexturedModalRect(leftPos+239, topPos+18, ix, 240, 11, 11);

    }

    private void handleHoveringText()
    {
        String str, str2;
        int temp;

        //reset text
        mouseoverList.clear();

        //Morale tooltip
        if (xMouse >= 238+leftPos && xMouse < 251+leftPos && yMouse >= 17+topPos && yMouse < 30+topPos)
        {
            mouseoverList.add(this.strMorale[moraleLevel(this.entity.getMorale())]);
            this.drawHoveringText(mouseoverList, 120, 30, this.fontRendererObj);

            //draw value
            mouseoverList.clear();

            mouseoverList.add(ChatFormatting.RED + strAttrATK);
            mouseoverList.add(ChatFormatting.RED + strAttrAIR);
            mouseoverList.add(ChatFormatting.WHITE + strAttrSPD);
            mouseoverList.add(ChatFormatting.LIGHT_PURPLE + strAttrHIT);
            mouseoverList.add(ChatFormatting.AQUA + strAttrCri);
            mouseoverList.add(ChatFormatting.YELLOW + strAttrDHIT);
            mouseoverList.add(ChatFormatting.GOLD + strAttrTHIT);
            mouseoverList.add(ChatFormatting.RED + strAttrMissR);
            mouseoverList.add(ChatFormatting.YELLOW + strAttrAA);
            mouseoverList.add(ChatFormatting.AQUA + strAttrASM);
            mouseoverList.add(ChatFormatting.WHITE + strAttrDEF);
            mouseoverList.add(ChatFormatting.GOLD + strAttrDodge);
            mouseoverList.add(ChatFormatting.GREEN + strAttrXP);
            mouseoverList.add(ChatFormatting.DARK_PURPLE + strAttrGrudge);
            mouseoverList.add(ChatFormatting.DARK_AQUA + strAttrAmmo);
            mouseoverList.add(ChatFormatting.DARK_GREEN + strAttrHPRES);
            mouseoverList.add(ChatFormatting.DARK_RED + strAttrKB);
            mouseoverList.add(ChatFormatting.GRAY + strAttrMOV);
            this.drawHoveringText(mouseoverList, 120, 46, this.fontRendererObj);

            //draw value
            mouseoverList.clear();

            str = String.format("%.0f", attrs.getAttrsMorale(Attr.ATK_L) * 100F) + " %";
            str2 = String.format("%.0f", attrs.getAttrsMorale(Attr.ATK_H) * 100F) + " %";
            overText = "x " + str + " / " + str2;
            mouseoverList.add(overText);

            str = String.format("%.0f", attrs.getAttrsMorale(Attr.ATK_AL) * 100F) + " %";
            str2 = String.format("%.0f", attrs.getAttrsMorale(Attr.ATK_AH) * 100F) + " %";
            overText = "x " + str + " / " + str2;
            mouseoverList.add(overText);

            overText = "x " + String.format("%.0f", attrs.getAttrsMorale(Attr.SPD) * 100F) + " %";
            mouseoverList.add(overText);
            overText = "+ " + String.format("%.1f", attrs.getAttrsMorale(Attr.HIT));
            mouseoverList.add(overText);
            overText = "x " + String.format("%.0f", attrs.getAttrsMorale(Attr.CRI) * 100F) + " %";
            mouseoverList.add(overText);
            overText = "x " + String.format("%.0f", attrs.getAttrsMorale(Attr.DHIT) * 100F) + " %";
            mouseoverList.add(overText);
            overText = "x " + String.format("%.0f", attrs.getAttrsMorale(Attr.THIT) * 100F) + " %";
            mouseoverList.add(overText);
            overText = "x " + String.format("%.0f", attrs.getAttrsMorale(Attr.MISS) * 100F) + " %";
            mouseoverList.add(overText);
            overText = "x " + String.format("%.0f", attrs.getAttrsMorale(Attr.AA) * 100F) + " %";
            mouseoverList.add(overText);
            overText = "x " + String.format("%.0f", attrs.getAttrsMorale(Attr.ASM) * 100F) + " %";
            mouseoverList.add(overText);
            overText = "+ " + String.format("%.0f", attrs.getAttrsMorale(Attr.DEF) * 100F) + " %";
            mouseoverList.add(overText);
            overText = "+ " + String.format("%.0f", attrs.getAttrsMorale(Attr.DODGE) * 100F) + " %";
            mouseoverList.add(overText);
            overText = "+ " + String.format("%.0f", attrs.getAttrsMorale(Attr.XP) * 100F) + " %";
            mouseoverList.add(overText);
            overText = "+ " + String.format("%.0f", attrs.getAttrsMorale(Attr.GRUDGE) * 100F) + " %";
            mouseoverList.add(overText);
            overText = "+ " + String.format("%.0f", attrs.getAttrsMorale(Attr.AMMO) * 100F) + " %";
            mouseoverList.add(overText);
            overText = "+ " + String.format("%.0f", attrs.getAttrsMorale(Attr.HPRES) * 100F) + " %";
            mouseoverList.add(overText);
            overText = "+ " + String.format("%.0f", attrs.getAttrsMorale(Attr.KB) * 100F) + " %";
            mouseoverList.add(overText);
            overText = "+ " + String.format("%.2f", attrs.getAttrsMorale(Attr.MOV));
            mouseoverList.add(overText);

            this.drawHoveringText(mouseoverList, 126 + this.widthHoveringText3, 46, this.fontRendererObj);
        }
        //HP tooltip
        else if (xMouse >= 145+leftPos && xMouse < 202+leftPos && yMouse >= 4+topPos && yMouse < 15+topPos)
        {
            mouseoverList.add(strAttrModern+" "+attrs.getAttrsBonus(BaseAttr.HP));
            this.drawHoveringText(mouseoverList, 145, 32, this.fontRendererObj);
        }
        //Attrs tooltip
        else if (xMouse >= 73+leftPos && xMouse < 134+leftPos)
        {
            if (this.controls.showPage == 2)
            {
                //ATK tooltip
                if (yMouse >= 18+topPos && yMouse < 41+topPos)
                {
                    //draw attack text
                    mouseoverList.add(strAttrModern);
                    mouseoverList.add(ChatFormatting.RED + strAttrATK);
                    mouseoverList.add(ChatFormatting.RED + strAttrAIR);
                    mouseoverList.add(ChatFormatting.AQUA + strAttrCri);
                    mouseoverList.add(ChatFormatting.YELLOW + strAttrDHIT);
                    mouseoverList.add(ChatFormatting.GOLD + strAttrTHIT);
                    mouseoverList.add(ChatFormatting.YELLOW + strAttrAA);
                    mouseoverList.add(ChatFormatting.AQUA + strAttrASM);
                    mouseoverList.add(ChatFormatting.RED + strAttrMiss);
                    mouseoverList.add(ChatFormatting.GOLD + strAttrDodge);
                    mouseoverList.add(ChatFormatting.GREEN + strAttrXP);
                    mouseoverList.add(ChatFormatting.DARK_PURPLE + strAttrGrudge);
                    mouseoverList.add(ChatFormatting.DARK_AQUA + strAttrAmmo);
                    mouseoverList.add(ChatFormatting.DARK_GREEN + strAttrHPRES);
                    mouseoverList.add(ChatFormatting.DARK_RED + strAttrKB);
                    this.drawHoveringText(mouseoverList, 55, 57, this.fontRendererObj);

                    //draw attack value
                    mouseoverList.clear();
                    mouseoverList.add(String.valueOf(attrs.getAttrsBonus(BaseAttr.ATK)));
                    mouseoverList.add(strATK);
                    mouseoverList.add(strAATK);
                    overText = String.valueOf((int)(attrs.getAttrsBuffed(Attr.CRI) * 100F)) + " %";
                    mouseoverList.add(overText);
                    overText = String.valueOf((int)(attrs.getAttrsBuffed(Attr.DHIT) * 100F)) + " %";
                    mouseoverList.add(overText);
                    overText = String.valueOf((int)(attrs.getAttrsBuffed(Attr.THIT) * 100F)) + " %";
                    mouseoverList.add(overText);
                    overText = String.valueOf((int)(attrs.getAttrsBuffed(Attr.AA)));
                    mouseoverList.add(overText);
                    overText = String.valueOf((int)(attrs.getAttrsBuffed(Attr.ASM)));
                    mouseoverList.add(overText);

                    //calc miss
                    temp = (int) (this.entity.calcMissRate(0F) * 100F);
                    str = String.valueOf(temp);
                    temp = (int) (this.entity.calcMissRate(attrs.getAttackRange()) * 100F);
                    str2 = String.valueOf(temp);
                    overText = str + " ~ " + str2 + " %";
                    mouseoverList.add(overText);

                    //calc dodge for submarine
                    if (this.entity.isInvisibleShip())
                    {
                        float temp2 = attrs.getAttrsBuffed(Attr.DODGE) + this.entity.getInvisibleLevel();
                        if (temp2 > com.lulan.shincolle.entity.ship.LegacyShipStatTables.LIMIT_SHIP_ATTRS[Attr.DODGE]) temp2 = (float) com.lulan.shincolle.entity.ship.LegacyShipStatTables.LIMIT_SHIP_ATTRS[Attr.DODGE];
                        overText = String.format("%.0f", temp2 * 100F) + " %";
                    }
                    //calc dodge for normal ship
                    else
                    {
                        overText = String.format("%.0f", attrs.getAttrsBuffed(Attr.DODGE) * 100F) + " %";
                    }
                    mouseoverList.add(overText);

                    overText = String.valueOf((int)((attrs.getAttrsBuffed(Attr.XP) - 1F) * 100F)) + " %";
                    mouseoverList.add(overText);
                    overText = String.valueOf((int)((attrs.getAttrsBuffed(Attr.GRUDGE) - 1F) * 100F)) + " %";
                    mouseoverList.add(overText);
                    overText = String.valueOf((int)((attrs.getAttrsBuffed(Attr.AMMO) - 1F) * 100F)) + " %";
                    mouseoverList.add(overText);
                    overText = String.valueOf((int)((attrs.getAttrsBuffed(Attr.HPRES) - 1F) * 100F)) + " %";
                    mouseoverList.add(overText);
                    overText = String.valueOf((int)(attrs.getAttrsBuffed(Attr.KB) * 100F)) + " %";
                    mouseoverList.add(overText);
                    this.drawHoveringText(mouseoverList, 61 + this.widthHoveringText1, 57, this.fontRendererObj);
                }
                //DEF tooltip
                else if (yMouse >= 41+topPos && yMouse < 62+topPos)
                {
                    mouseoverList.add(strAttrModern+" "+attrs.getAttrsBonus(BaseAttr.DEF));
                    this.drawHoveringText(mouseoverList, 55, 78, this.fontRendererObj);
                }
                //SPD tooltip
                else if (yMouse >= 62+topPos && yMouse < 83+topPos)
                {
                    mouseoverList.add(strAttrModern+" "+attrs.getAttrsBonus(BaseAttr.SPD));
                    this.drawHoveringText(mouseoverList, 55, 99, this.fontRendererObj);
                }
                //MOV tooltip
                else if (yMouse >= 83+topPos && yMouse < 104+topPos)
                {
                    mouseoverList.add(strAttrModern+" "+attrs.getAttrsBonus(BaseAttr.MOV));
                    this.drawHoveringText(mouseoverList, 55, 120, this.fontRendererObj);
                }
                //RANGE tooltip
                else if (yMouse >= 104+topPos && yMouse < 126+topPos)
                {
                    mouseoverList.add(strAttrModern+" "+attrs.getAttrsBonus(BaseAttr.HIT));
                    this.drawHoveringText(mouseoverList, 55, 142, this.fontRendererObj);
                }
            }//end page 2
            else if (this.controls.showPage == 3)
            {
                //show text at FORMATION
                if (yMouse >= 40+topPos && yMouse < 62+topPos && this.entity.getStateMinor("FormatType") >= 1)
                {
                    mouseoverList.add(ChatFormatting.LIGHT_PURPLE + strAttrFPos);
                    mouseoverList.add(ChatFormatting.RED + strAttrATK);
                    mouseoverList.add(ChatFormatting.RED + strAttrAIR);
                    mouseoverList.add(ChatFormatting.WHITE + strAttrSPD);
                    mouseoverList.add(ChatFormatting.LIGHT_PURPLE + strAttrHIT);
                    mouseoverList.add(ChatFormatting.AQUA + strAttrCri);
                    mouseoverList.add(ChatFormatting.YELLOW + strAttrDHIT);
                    mouseoverList.add(ChatFormatting.GOLD + strAttrTHIT);
                    mouseoverList.add(ChatFormatting.RED + strAttrMissR);
                    mouseoverList.add(ChatFormatting.YELLOW + strAttrAA);
                    mouseoverList.add(ChatFormatting.AQUA + strAttrASM);
                    mouseoverList.add(ChatFormatting.WHITE + strAttrDEF);
                    mouseoverList.add(ChatFormatting.GOLD + strAttrDodge);
                    mouseoverList.add(ChatFormatting.DARK_PURPLE + strAttrGrudge);
                    mouseoverList.add(ChatFormatting.DARK_GREEN + strAttrHPRES);
                    mouseoverList.add(ChatFormatting.DARK_RED + strAttrKB);
                    mouseoverList.add(ChatFormatting.GRAY + strAttrMOV);
                    this.drawHoveringText(mouseoverList, 128, 35, this.fontRendererObj);

                    //draw value
                    mouseoverList.clear();

                    mouseoverList.addAll(this.entity.formationTooltipValues());

                    this.drawHoveringText(mouseoverList, 134 + this.widthHoveringText3, 35, this.fontRendererObj);
                }//end formation
            }//end page 3
        }
        //AI tooltip
        else if (xMouse >= 173+leftPos && xMouse < 239+leftPos && yMouse >= 131+topPos && yMouse < 207+topPos)
        {
            //AI page 7
            if (this.controls.showPageAI == 7)
            {
                mouseoverList.clear();

                if (yMouse <= 157+topPos)
                {
                    if (xMouse <= 189+leftPos)
                    {
                        mouseoverList.add(strCook);
                    }
                    else if (xMouse <= 205+leftPos)
                    {
                        mouseoverList.add(strFish);
                    }
                    else if (xMouse <= 221+leftPos)
                    {
                        mouseoverList.add(strMine);
                    }
                    else
                    {
                        mouseoverList.add(strCraft);
                    }

                    this.drawHoveringText(mouseoverList, 167, 170, this.fontRendererObj);
                }
                else if (yMouse <= 170+topPos)
                {
                    mouseoverList.add(strMeta);
                    this.drawHoveringText(mouseoverList, 145, 185, this.fontRendererObj);
                }
                else if (yMouse <= 183+topPos)
                {
                    mouseoverList.add(strDict);
                    this.drawHoveringText(mouseoverList, 145, 198, this.fontRendererObj);
                }
                else if (yMouse <= 196+topPos)
                {
                    mouseoverList.add(strNbt);
                    this.drawHoveringText(mouseoverList, 145, 211, this.fontRendererObj);
                }
            }//end AI page 7
        }
    }

    private void drawAttributes()
    {
        //draw hp, level
        shiplevel = String.valueOf(entity.getStateMinor("ShipLevel"));
        hpCurrent = Mth.ceil(entity.getHealth());
        hpMax = Mth.ceil(entity.getMaxHealth());
        color = 0;

        //draw lv/hp name
        this.fontRendererObj.drawStringWithShadow(lvMark, 231-this.fontRendererObj.getStringWidth(lvMark), 6, 65535);
        this.fontRendererObj.drawStringWithShadow(hpMark, 145-this.fontRendererObj.getStringWidth(hpMark), 6, 65535);

        //draw level: 150->gold other->white
        if (entity.getStateMinor("ShipLevel") < 150)
        {
            color = 16777215;  //white
        }
        else
        {
            color = 16766720;  //gold
        }
        this.fontRendererObj.drawStringWithShadow(shiplevel, imageWidth-6-this.fontRendererObj.getStringWidth(shiplevel), 6, color);

        //draw maxhp
        color = getBonusPointColor(attrs.getAttrsBonus(BaseAttr.HP));
        this.fontRendererObj.drawStringWithShadow("/"+String.valueOf(hpMax), 148 + this.fontRendererObj.getStringWidth(String.valueOf(hpCurrent)), 6, color);

        //draw current hp, if currHP < maxHP, use darker color
        if (hpCurrent < hpMax) color = getDarkerColor(color, 0.8F);
        this.fontRendererObj.drawStringWithShadow(String.valueOf(hpCurrent), 147, 6, color);

        //draw string in different page
        switch (this.controls.showPage)
        {
        case 2:
        {   //page 2: attribute page
            strLATK = String.format("%.1f", attrs.getAttrsBuffed(Attr.ATK_L));
            strHATK = String.format("%.1f", attrs.getAttrsBuffed(Attr.ATK_H));
            strATK = strLATK + " / " + strHATK;
            strALATK = String.format("%.1f", attrs.getAttrsBuffed(Attr.ATK_AL));
            strAHATK = String.format("%.1f", attrs.getAttrsBuffed(Attr.ATK_AH));
            strAATK = strALATK + " / " + strAHATK;
            strDEF = String.format("%.1f", attrs.getAttrsBuffed(Attr.DEF) * 100F)+"%";
            strSPD = String.format("%.2f", attrs.getAttrsBuffed(Attr.SPD));
            strMOV = String.format("%.2f", attrs.getAttrsBuffed(Attr.MOV));
            strHIT = String.format("%.1f", attrs.getAttrsBuffed(Attr.HIT));

            //draw ATK, DEF, SPD, MOV, HIT
            if (this.controls.showAttack == 1)
            {   //show cannon attack
                this.fontRendererObj.drawString(strAttrATK, 75, 20, 0);
                color = getBonusPointColor(attrs.getAttrsBonus(BaseAttr.ATK));
                this.fontRendererObj.drawStringWithShadow(strATK, 133-this.fontRendererObj.getStringWidth(strATK), 30, color);
            }
            else
            {   //show aircraft attack
                this.fontRendererObj.drawString(strAttrAIR, 75, 20, 0);
                color = getBonusPointColor(attrs.getAttrsBonus(BaseAttr.ATK));
                this.fontRendererObj.drawStringWithShadow(strAATK, 133-this.fontRendererObj.getStringWidth(strAATK), 30, color);
            }

            this.fontRendererObj.drawString(strAttrDEF, 75, 41, 0);
            this.fontRendererObj.drawString(strAttrSPD, 75, 62, 0);
            this.fontRendererObj.drawString(strAttrMOV, 75, 83, 0);
            this.fontRendererObj.drawString(strAttrHIT, 75, 104, 0);

            //draw armor
            color = getBonusPointColor(attrs.getAttrsBonus(BaseAttr.DEF));
            this.fontRendererObj.drawStringWithShadow(strDEF, 133-this.fontRendererObj.getStringWidth(strDEF), 51, color);

            //draw attack speed
            color = getBonusPointColor(attrs.getAttrsBonus(BaseAttr.SPD));
            this.fontRendererObj.drawStringWithShadow(strSPD, 133-this.fontRendererObj.getStringWidth(strSPD), 72, color);

            //draw movement speed
            color = getBonusPointColor(attrs.getAttrsBonus(BaseAttr.MOV));
            this.fontRendererObj.drawStringWithShadow(strMOV, 133-this.fontRendererObj.getStringWidth(strMOV), 93, color);

            //draw range
            color = getBonusPointColor(attrs.getAttrsBonus(BaseAttr.HIT));
            this.fontRendererObj.drawStringWithShadow(strHIT, 133-this.fontRendererObj.getStringWidth(strHIT), 114, color);
            break;
        }
        case 1:
        {   //page 1: exp, kills, L&H ammo, fuel
            //draw string
            this.fontRendererObj.drawString(strMiKills, 75, 20, 0);
            this.fontRendererObj.drawString(strMiExp, 75, 41, 0);
            this.fontRendererObj.drawString(strMiAmmoL, 75, 62, 0);
            this.fontRendererObj.drawString(strMiAmmoH, 75, 83, 0);
            this.fontRendererObj.drawString(strMiGrudge, 75, 104, 0);

            //draw value

            Exp = String.valueOf(this.entity.getStateMinor("ExpCurrent"))+"/"+String.valueOf(this.entity.getStateMinor("ExpNext"));
            Kills = String.valueOf(this.entity.getStateMinor("Kills"));
            AmmoLight = String.valueOf(this.entity.getStateMinor("NumAmmoLight"));
            AmmoHeavy = String.valueOf(this.entity.getStateMinor("NumAmmoHeavy"));
            Grudge = String.valueOf(this.entity.getStateMinor("NumGrudge"));

            this.fontRendererObj.drawStringWithShadow(Kills, 133-this.fontRendererObj.getStringWidth(Kills), 30, 16777215);
            this.fontRendererObj.drawStringWithShadow(Exp, 133-this.fontRendererObj.getStringWidth(Exp), 51, 16777215);
            this.fontRendererObj.drawStringWithShadow(AmmoLight, 133-this.fontRendererObj.getStringWidth(AmmoLight), 72, 16777215);
            this.fontRendererObj.drawStringWithShadow(AmmoHeavy, 133-this.fontRendererObj.getStringWidth(AmmoHeavy), 93, 16777215);
            this.fontRendererObj.drawStringWithShadow(Grudge, 133-this.fontRendererObj.getStringWidth(Grudge), 114, 16777215);

            break;
        }
        case 3:
        {   //page 3: light/heavy airplane, marriage
            //draw string
            this.fontRendererObj.drawString(strAttrWedding, 75, 20, 0);
            this.fontRendererObj.drawString(strAttrFormat, 75, 41, 0);

            //draw value
            //draw marriage
            if (this.entity.getStateFlag("IsMarried"))
            {
                marriage = strAttrWedTrue;
            }
            else
            {
                marriage = strAttrWedFalse;
            }

            //draw formation
            int ftype = this.entity.getStateMinor("FormatType");
            this.Formation = I18n.get("gui.shincolle:formation.format"+ftype);
            this.fontRendererObj.drawStringWithShadow(Formation, 133-this.fontRendererObj.getStringWidth(Formation), 51, 16777215);

            //大型艦, 顯示艦載機數量
            if (this.entity.isCarrier())
            {
                this.fontRendererObj.drawString(strMiAirL, 75, 83, 0);
                this.fontRendererObj.drawString(strMiAirH, 75, 104, 0);
                AirLight = String.valueOf(this.entity.getNumAircraftLight());
                AirHeavy = String.valueOf(this.entity.getNumAircraftHeavy());
                this.fontRendererObj.drawStringWithShadow(AirLight, 133-this.fontRendererObj.getStringWidth(AirLight), 93, 16776960);
                this.fontRendererObj.drawStringWithShadow(AirHeavy, 133-this.fontRendererObj.getStringWidth(AirHeavy), 114, 16776960);
            }

            this.fontRendererObj.drawStringWithShadow(marriage, 133-this.fontRendererObj.getStringWidth(marriage), 30, 16776960);

            break;
        }//end case 3
        }//end page switch

        //draw AI page
        switch (this.controls.showPageAI)
        {
        case 1:
        {   //AI page 1
            //draw string
            this.fontRendererObj.drawString(canMelee, 187, 133, 0);
            if (entity.getAttackType("AtkType_Light"))
            this.fontRendererObj.drawString(canLATK, 187, 146, 0);
            if (entity.getAttackType("AtkType_Heavy"))
            this.fontRendererObj.drawString(canHATK, 187, 159, 0);
            if (entity.getAttackType("AtkType_AirLight"))
            this.fontRendererObj.drawString(canALATK, 187, 172, 0);
            if (entity.getAttackType("AtkType_AirHeavy"))
            this.fontRendererObj.drawString(canAHATK, 187, 185, 0);
            if (entity.getAttackType("HaveRingEffect"))
            this.fontRendererObj.drawString(auraEffect, 187, 198, 0);
        }
        break;
        case 2:
        {   //AI page 2
            //draw string
            this.fontRendererObj.drawString(followMin, 174, 134, 0);
            this.fontRendererObj.drawString(followMax, 174, 158, 0);
            this.fontRendererObj.drawString(fleeHP, 174, 182, 0);

            //draw value
            followMinValue = String.valueOf(entity.getStateMinor("FollowMin"));
            followMaxValue = String.valueOf(entity.getStateMinor("FollowMax"));
            fleeHPValue = String.valueOf(entity.getStateMinor("FleeHP"));

            if (this.controls.mousePressBar == 0)
            {
                barPosValue = String.valueOf((int)(this.controls.barPos / 42F * 30F + 1F));
                this.fontRendererObj.drawStringWithShadow(barPosValue, 174, 145, 11141120);
            }
            else
            {
                this.fontRendererObj.drawStringWithShadow(followMinValue, 174, 145, 16776960);
            }

            if (this.controls.mousePressBar == 1)
            {
                barPosValue = String.valueOf((int)(this.controls.barPos / 42F * 30F + 2F));
                this.fontRendererObj.drawStringWithShadow(barPosValue, 174, 169, 16724787);
            }
            else
            {
                this.fontRendererObj.drawStringWithShadow(followMaxValue, 174, 169, 16776960);
            }

            if (this.controls.mousePressBar == 2)
            {
                barPosValue = String.valueOf((int)(this.controls.barPos / 42F * 100F));
                this.fontRendererObj.drawStringWithShadow(barPosValue, 174, 193, 16724787);
            }
            else
            {
                this.fontRendererObj.drawStringWithShadow(fleeHPValue, 174, 193, 16776960);
            }
        }
        break;
        case 3:
        {   //AI page 3
            //draw string
            this.fontRendererObj.drawString(tarAI, 187, 133, 0);
            this.fontRendererObj.drawString(strOnSight, 187, 146, 0);
            this.fontRendererObj.drawString(strPVP, 187, 159, 0);
            this.fontRendererObj.drawString(strAA, 187, 172, 0);
            this.fontRendererObj.drawString(strASM, 187, 185, 0);
            this.fontRendererObj.drawString(strTimeKeep, 187, 198, 0);
        }
        break;
        case 4:     //AI page 4
        {
            if (this.entity.getStateFlag("CanPickItem")) this.fontRendererObj.drawString(strPick, 187, 134, 0);
            this.fontRendererObj.drawString(strAutoPump, 187, 146, 0);
        }
        break;
        case 5:     //AI page 5
        {
            //draw string
            this.fontRendererObj.drawString(strWpStay, 174, 134, 0);
            this.fontRendererObj.drawString(strAutoCR, 174, 158, 0);

            //draw value
            strWpStayValue = tick2SecOrMin(WaypointBlockEntity.stayModeToTicks(entity.getStateMinor("WpStay")));
            int temp = entity.getStateMinor("UseCombatRation");

            if (this.controls.mousePressBar == 3)
            {
                barPosValue = tick2SecOrMin(WaypointBlockEntity.stayModeToTicks((int)(this.controls.barPos / (42F * 0.0625F))));
                this.fontRendererObj.drawStringWithShadow(barPosValue, 174, 145, 16724787);
            }
            else
            {
                this.fontRendererObj.drawStringWithShadow(strWpStayValue, 174, 145, 16776960);
            }

            if (this.controls.mousePressBar == 4)
            {
                int temp2 = (int)(this.controls.barPos / (43F * 0.25F));

                if (temp2 >= 0 && temp2 <= 3)
                {
                    barPosValue = this.strMorale[temp2+1];
                    this.fontRendererObj.drawStringWithShadow(barPosValue, 174, 169, 16724787);
                }
            }
            else
            {
                if (temp >= 0 && temp <= 4)
                {
                    this.fontRendererObj.drawStringWithShadow(this.strMorale[temp], 174, 169, 16776960);
                }
            }
        }
        break;
        case 6:     //AI page 6
        {   //AI page 6
            //draw string
            this.fontRendererObj.drawString(strShowHeld, 187, 133, 0);
            this.fontRendererObj.drawString(strAppear, 177, 146, 0);
        }
        break;
        case 7:     //AI page 7
        {
            //draw string
            this.fontRendererObj.drawString(" Metadata", 187, 159, 0);
            this.fontRendererObj.drawString(" Ore Dict", 187, 172, 0);
            this.fontRendererObj.drawString(" NBT Tag", 187, 185, 0);
        }
        break;
        case 8:     //AI page 8
        {
            //draw string
            this.fontRendererObj.drawString(strInput, 177, 133, 0);
            this.fontRendererObj.drawString(strOutput, 177, 159, 0);
            this.fontRendererObj.drawString(strFuel, 177, 185, 0);
        }
        break;
        case 9:     //AI page 9
        {
        }
        break;
        case 10:    //AI page 10
        {
        }
        break;
        case 11:    //AI page 11
        {
        }
        break;
        case 12:    //AI page 12
        {
        }
        break;
        }//end AI page switch
    }

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
                    if (mount.getEmotion() == 4) specialOffset = mount.isOrderedToSit() ? -0.34F : -0.57F;
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

    public static int moraleLevel(int m)
    {
        if (m > 5100)
        {  //excited
            return 0;
        }
        else if (m > 3900)
        {
            return 1;
        }
        else if (m > 2100)
        {
            return 2;
        }
        else if (m > 900)
        {
            return 3;
        }
        else
        {
            return 4;
        }
    }

    public static String tick2SecOrMin(int ticks)
    {
        int t = (int) (ticks * 0.05F);

        if (t >= 60)
        {
            t *= 0.016666667F;
            return String.valueOf(t) + "m";
        }
        else
        {
            return String.valueOf(t) + "s";
        }
    }

    public static int getBonusPointColor(int level)
    {
        /**
         * bonus color:
         *   white -> yellow -> red
         * = RGB255 -> RG255 -> R255
         * = 16711680 + 65280 + 255 -> 16711680 + 65280 -> 16711680
         *
         * 0~50% = blue--
         * 51~100% = green--
         */
        int max = ShinColleConfig.modernLimit();
        int color = 0;
        float flv = (float)level / (float)max - 0.5F;

        if (flv >= 0.5F)
        {
            return 16711680;
        }
        else if (flv >= 0F)
        {
            color = (int) (255F * (1F - flv * 2F)) * 256 + 16711680;
        }
        else
        {
            flv += 0.5F;
            color = (int) (255F * (1F - flv * 2F)) + 16776960;
        }

        return color;
    }

    public static int getDarkerColor(int color, float dark)
    {
        float b = (color & 255) * dark;
        color >>= 8;
        float g = (color & 255) * dark;
        color >>= 8;
        float r = color * dark;

        return ((int)r << 16) + ((int)g << 8) + (int)b;
    }

}
