package com.lulan.shincolle.client.screen;

import com.lulan.shincolle.config.ShinColleConfig;
import com.lulan.shincolle.entity.ship.LegacyShipEntity;
import com.lulan.shincolle.entity.ship.LegacyShipStatTables.Attr;
import com.lulan.shincolle.entity.ship.LegacyShipStatTables.BaseAttr;
import com.lulan.shincolle.menu.LegacyShipInventoryCatalog;
import com.lulan.shincolle.menu.LegacyMorphInventoryControls;
import com.lulan.shincolle.menu.MorphInventoryMenu;
import com.lulan.shincolle.menu.MorphInventoryPresentation;
import com.lulan.shincolle.network.ModNetwork;
import com.lulan.shincolle.network.GameplayCommandType;
import com.lulan.shincolle.menu.LegacyShipInventoryControls;
import com.lulan.shincolle.network.ServerboundGameplayCommandPacket;
import com.lulan.shincolle.registry.ModEntityTypes;
import net.minecraft.nbt.CompoundTag;
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

public final class MorphInventoryScreen extends AbstractContainerScreen<MorphInventoryMenu> {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final ResourceLocation TEXTURE_BG = texture("guishipmorph");
    private static final ResourceLocation TEXTURE_ICON0 = texture("guinameicon0");
    private static final ResourceLocation TEXTURE_ICON1 = texture("guinameicon1");
    private static final ResourceLocation TEXTURE_ICON2 = texture("guinameicon2");
    private static final int[] POW2 = new int[32];
    static { for (int i = 0; i < 32; i++) POW2[i] = 1 << i; }
    private final MorphInventoryPresentation entity, attrs;
    private final LegacyMorphInventoryControls controls;
    private final LegacyShipEntity[] shipRiding = new LegacyShipEntity[3];
    private GuiGraphics graphics;
    private ResourceLocation boundTexture;
    private final FontBridge fontRendererObj = new FontBridge();
    private static String lvMark, hpMark, strAttrATK, strAttrAIR, strAttrDEF, strAttrSPD, strAttrMOV,
      strAttrHIT, strAttrCri, strAttrDHIT, strAttrTHIT, strAttrAA, strAttrASM, strAttrMiss,
      strAttrMissR, strAttrDodge, strAttrFPos, strAttrFormat, strAttrWedding,
      strAttrWedTrue, strAttrWedFalse, strMiKills, strMiExp, strMiAirL, strMiAirH, strMiAmmoL,
      strMiAmmoH, strMiGrudge, canMelee, canLATK, canHATK, canALATK, canAHATK, auraEffect,
      strTimeKeep, strNofuel,
      strAttrModern, strAttrXP, strAttrGrudge, strAttrAmmo, strAttrHPRES,
      strShowHeld, strAttrKB, strAttrHP, strAppear, strSit, strEmoFlag1, strEmoFlag2;
    private static int widthHoveringText1, widthHoveringText2, widthHoveringText3;

    private List<String> mouseoverList;
    private String titlename, shiplevel,
                   strATK, strAATK, strLATK, strHATK, strALATK, strAHATK, strDEF, strSPD, strMOV, strHIT,
                   Kills, Exp, Grudge, Owner, AmmoLight, AmmoHeavy, AirLight, AirHeavy, overText, marriage,
                   Formation;
    private int hpCurrent, hpMax, color,   pageIndicator, pageIndicatorAI,
                shipType, shipClass;
    private float xMouse, yMouse;
    private int[][] iconXY;  //icon array:  [ship type, ship name][file,x,y]


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

        //general string
        lvMark = I18n.get("gui.shincolle:level");
        hpMark = I18n.get("gui.shincolle:hp");

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
        strTimeKeep = I18n.get("gui.shincolle:ai.timekeeper");
        strShowHeld = I18n.get("gui.shincolle:showhelditem");
        strAppear = I18n.get("gui.shincolle:appearance");
        strNofuel = I18n.get("gui.shincolle:morph.nofuel");
        strSit = I18n.get("gui.shincolle:morph.sit");
        strEmoFlag1 = I18n.get("gui.shincolle:morph.emo1");
        strEmoFlag2 = I18n.get("gui.shincolle:morph.emo2");
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
            this.controls.switchPage1a[0] = this.entity.getStateFlag("NoFuel");
            this.controls.switchPage1a[1] = this.entity.isSitting();
            this.controls.switchPage1a[2] = this.entity.getStateEmotion("Emotion") > 0 ? true : false;
            this.controls.switchPage1a[3] = this.entity.getStateEmotion("Emotion4") > 0 ? true : false;
//            this.controls.switchPage1a[4] = this.entity.getStateFlag("UseAirHeavy");
            this.controls.switchPage1a[5] = this.entity.getStateFlag("UseRingEffect");

            //get button display flag
            this.controls.switchPage1b[0] = true;
            this.controls.switchPage1b[1] = true;
            this.controls.switchPage1b[2] = true;
            this.controls.switchPage1b[3] = true;
            this.controls.switchPage1b[4] = false;
            this.controls.switchPage1b[5] = true;

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
            break;
        }
        case 3:
        {   //page 3
            this.pageIndicator = 239;
            this.pageIndicatorAI = 157;
            break;
        }
        case 4:
        {   //page 4
            this.pageIndicator = 239;
            this.pageIndicatorAI = 170;
            break;
        }
        case 5:
        {   //page 5
            this.pageIndicator = 239;
            this.pageIndicatorAI = 183;
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

private void handleHoveringText()
    {
        String str, str2;
        int temp;

        //reset text
        mouseoverList.clear();

        //HP tooltip
        if (xMouse >= 145+leftPos && xMouse < 202+leftPos && yMouse >= 4+topPos && yMouse < 15+topPos)
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
//          else if (this.controls.showPage == 3)
//          {
//              //show text at FORMATION
//              if (yMouse >= 40+topPos && yMouse < 62+topPos && this.entity.getStateMinor("FormatType") >= 1)
//              {
//                  mouseoverList.add(ChatFormatting.LIGHT_PURPLE + strAttrFPos);
//                  mouseoverList.add(ChatFormatting.RED + strAttrATK);
//                  mouseoverList.add(ChatFormatting.RED + strAttrAIR);
//                  mouseoverList.add(ChatFormatting.WHITE + strAttrSPD);
//                  mouseoverList.add(ChatFormatting.LIGHT_PURPLE + strAttrHIT);
//                  mouseoverList.add(ChatFormatting.AQUA + strAttrCri);
//                  mouseoverList.add(ChatFormatting.YELLOW + strAttrDHIT);
//                  mouseoverList.add(ChatFormatting.GOLD + strAttrTHIT);
//                  mouseoverList.add(ChatFormatting.RED + strAttrMissR);
//                  mouseoverList.add(ChatFormatting.YELLOW + strAttrAA);
//                  mouseoverList.add(ChatFormatting.AQUA + strAttrASM);
//                  mouseoverList.add(ChatFormatting.WHITE + strAttrDEF);
//                  mouseoverList.add(ChatFormatting.GOLD + strAttrDodge);
//                  mouseoverList.add(ChatFormatting.DARK_PURPLE + strAttrGrudge);
//                  mouseoverList.add(ChatFormatting.DARK_GREEN + strAttrHPRES);
//                  mouseoverList.add(ChatFormatting.DARK_RED + strAttrKB);
//                  mouseoverList.add(ChatFormatting.GRAY + strAttrMOV);
//                  this.drawHoveringText(mouseoverList, 128, 35, this.fontRendererObj);
//
//                  //draw value
//                  mouseoverList.clear();
//
//                  overText = String.valueOf(this.entity.getStateMinor("FormatPos") + 1);
//                  mouseoverList.add(overText);
//
//                  str = String.format("%.0f", attrs.getAttrsFormation(Attr.ATK_L) * 100F) + " %";
//                  str2 = String.format("%.0f", attrs.getAttrsFormation(Attr.ATK_H) * 100F) + " %";
//                  overText = "x " + str + " / " + str2;
//                  mouseoverList.add(overText);
//
//                  str = String.format("%.0f", attrs.getAttrsFormation(Attr.ATK_AL) * 100F) + " %";
//                  str2 = String.format("%.0f", attrs.getAttrsFormation(Attr.ATK_AH) * 100F) + " %";
//                  overText = "x " + str + " / " + str2;
//                  mouseoverList.add(overText);
//
//                  overText = "x " + String.format("%.0f", attrs.getAttrsFormation(Attr.SPD) * 100F) + " %";
//                  mouseoverList.add(overText);
//                  overText = "+ " + String.format("%.1f", attrs.getAttrsFormation(Attr.HIT));
//                  mouseoverList.add(overText);
//                  overText = "x " + String.format("%.0f", attrs.getAttrsFormation(Attr.CRI) * 100F) + " %";
//                  mouseoverList.add(overText);
//                  overText = "x " + String.format("%.0f", attrs.getAttrsFormation(Attr.DHIT) * 100F) + " %";
//                  mouseoverList.add(overText);
//                  overText = "x " + String.format("%.0f", attrs.getAttrsFormation(Attr.THIT) * 100F) + " %";
//                  mouseoverList.add(overText);
//                  overText = "x " + String.format("%.0f", attrs.getAttrsFormation(Attr.MISS) * 100F) + " %";
//                  mouseoverList.add(overText);
//                  overText = "x " + String.format("%.0f", attrs.getAttrsFormation(Attr.AA) * 100F) + " %";
//                  mouseoverList.add(overText);
//                  overText = "x " + String.format("%.0f", attrs.getAttrsFormation(Attr.ASM) * 100F) + " %";
//                  mouseoverList.add(overText);
//                  overText = "x " + String.format("%.0f", attrs.getAttrsFormation(Attr.DEF) * 100F) + " %";
//                  mouseoverList.add(overText);
//                  overText = "+ " + String.format("%.0f", attrs.getAttrsFormation(Attr.DODGE) * 100F) + " %";
//                  mouseoverList.add(overText);
//                  overText = "+ " + String.format("%.0f", attrs.getAttrsFormation(Attr.GRUDGE) * 100F) + " %";
//                  mouseoverList.add(overText);
//                  overText = "+ " + String.format("%.0f", attrs.getAttrsFormation(Attr.HPRES) * 100F) + " %";
//                  mouseoverList.add(overText);
//                  overText = "+ " + String.format("%.0f", attrs.getAttrsFormation(Attr.KB) * 100F) + " %";
//                  mouseoverList.add(overText);
//                  overText = "+ " + String.format("%.2f", attrs.getAttrsFormation(Attr.MOV));
//                  mouseoverList.add(overText);
//
//                  this.drawHoveringText(mouseoverList, 134 + this.widthHoveringText3, 35, this.fontRendererObj);
//              }//end formation
//          }//end page 3
        }
    }

private void drawAttributes()
    {
        //draw hp, level
        shiplevel = String.valueOf(entity.getStateMinor("ShipLevel"));

        if (entity.getPlayer() != null)
        {
            hpCurrent = Mth.ceil(entity.getPlayer().getHealth());
            hpMax = Mth.ceil(entity.getPlayer().getMaxHealth());
        }

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
            this.fontRendererObj.drawString(strNofuel, 187, 133, 0);
            this.fontRendererObj.drawString(strSit, 187, 146, 0);
            this.fontRendererObj.drawString(strEmoFlag1, 187, 159, 0);
            this.fontRendererObj.drawString(strEmoFlag2, 187, 172, 0);
//          this.fontRendererObj.drawString(strASM, 187, 185, 0);
            this.fontRendererObj.drawString(auraEffect, 187, 198, 0);
        }
        break;
        case 6:     //AI page 6
        {   //AI page 6
            //draw string
            this.fontRendererObj.drawString(strShowHeld, 187, 133, 0);
            this.fontRendererObj.drawString(strAppear, 177, 146, 0);
        }
        break;
        }//end AI page switch
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
