package com.lulan.shincolle.menu;

import com.lulan.shincolle.network.ShipGuiButton;
import com.lulan.shincolle.network.GameplayCommandType;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public final class LegacyMorphInventoryControls {
    private static final int[] btCols = {189, 205, 222};
    private static final int[][][] SUPPLY_BUTTONS = {//gui 7: morph ship inventory
         {//0:+ammoL         1:+ammoH          2:grudge+
          {7,18,25,35},     {7,36,25,53},     {7,54,25,71}
         }
        };
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

public void click(int posX, int posY, int mouseKey)
    {


        //get click position
        this.xClick = posX;
        this.yClick = posY;

        //check button
        switch(LegacyShipInventoryControls.buttonAt(0, xClick, yClick))
        {
        case 0: //page 1 button
            this.showPage = 1;
        break;
        case 1: //page 2 button
            this.showPage = 2;
        break;
        case 2: //page 3 button
            this.showPage = 3;
        break;
        case 3: //AI operation 0
            switch (this.showPageAI)
            {
            case 1:     //page 1: state: no fuel
                this.switchPage1a[0] = this.entity.getStateFlag("NoFuel");
                this.send.accept(ShipGuiButton.NO_FUEL, getInverseInt(this.switchPage1a[0]));
            break;
            case 6:     //page 6: show held item
                this.switchPage6[0] = this.entity.getStateFlag("ShowHeldItem");
                this.send.accept(ShipGuiButton.SHOW_HELD, getInverseInt(this.switchPage6[0]));
            break;
            }
        break;
        case 4: //AI operation 1
            switch (this.showPageAI)
            {
            case 1:     //page 1: state: sit
                this.switchPage1a[1] = this.entity.isSitting();
                this.send.accept(ShipGuiButton.SIT, 0);
            break;
            }
        break;
        case 5: //AI operation 2
            switch (this.showPageAI)
            {
            case 1:     //page 1: state: emotion flag 1
                this.send.accept(ShipGuiButton.EMO_FLAG_1, this.entity.getRNG().nextInt(9));
            break;
            case 6:     //page 6: model display switch button 0~3
                if (this.xClick < btCols[0])
                {
                    this.send.accept((ShipGuiButton.MODEL_STATE_01 + 0), getInverseInt(this.switchPage6[1]));
                }
                else if (this.xClick < btCols[1])
                {
                    this.send.accept((ShipGuiButton.MODEL_STATE_01 + 1), getInverseInt(this.switchPage6[2]));
                }
                else if (this.xClick < btCols[2])
                {
                    this.send.accept((ShipGuiButton.MODEL_STATE_01 + 2), getInverseInt(this.switchPage6[3]));
                }
                else
                {
                    this.send.accept((ShipGuiButton.MODEL_STATE_01 + 3), getInverseInt(this.switchPage6[4]));
                }
            break;
            }
        break;
        case 6: //AI operation 3
            switch (this.showPageAI)
            {
            case 1:     //page 1: state: emotion flag 2
                this.send.accept(ShipGuiButton.EMO_FLAG_2, this.entity.getStateEmotion("Emotion4") > 0 ? 0 : 4);
            break;
            case 6:     //page 6: model display switch button 4~7
                if (this.xClick < btCols[0])
                {
                    this.send.accept((ShipGuiButton.MODEL_STATE_01 + 4), getInverseInt(this.switchPage6[5]));
                }
                else if (this.xClick < btCols[1])
                {
                    this.send.accept((ShipGuiButton.MODEL_STATE_01 + 5), getInverseInt(this.switchPage6[6]));
                }
                else if (this.xClick < btCols[2])
                {
                    this.send.accept((ShipGuiButton.MODEL_STATE_01 + 6), getInverseInt(this.switchPage6[7]));
                }
                else
                {
                    this.send.accept((ShipGuiButton.MODEL_STATE_01 + 7), getInverseInt(this.switchPage6[8]));
                }
            break;
            }
        break;
        case 7: //AI operation 4
            switch (this.showPageAI)
            {
            case 6:     //page 6: model display switch button 8~11
                if (this.xClick < btCols[0])
                {
                    this.send.accept((ShipGuiButton.MODEL_STATE_01 + 8), getInverseInt(this.switchPage6[9]));
                }
                else if (this.xClick < btCols[1])
                {
                    this.send.accept((ShipGuiButton.MODEL_STATE_01 + 9), getInverseInt(this.switchPage6[10]));
                }
                else if (this.xClick < btCols[2])
                {
                    this.send.accept((ShipGuiButton.MODEL_STATE_01 + 10), getInverseInt(this.switchPage6[11]));
                }
                else
                {
                    this.send.accept((ShipGuiButton.MODEL_STATE_01 + 11), getInverseInt(this.switchPage6[12]));
                }
            break;
            }
        break;
        case 8: //AI operation 5
            switch (this.showPageAI)
            {
            case 1:     //page 1: enable aura effect
                this.switchPage1a[5] = this.entity.getStateFlag("UseRingEffect");
                this.send.accept(ShipGuiButton.AURA_EFFECT, getInverseInt(this.switchPage1a[5]));
            break;
            case 6:     //page 6: model display switch button 12~15
                if (this.xClick < btCols[0])
                {
                    this.send.accept((ShipGuiButton.MODEL_STATE_01 + 12), getInverseInt(this.switchPage6[13]));
                }
                else if (this.xClick < btCols[1])
                {
                    this.send.accept((ShipGuiButton.MODEL_STATE_01 + 13), getInverseInt(this.switchPage6[14]));
                }
                else if (this.xClick < btCols[2])
                {
                    this.send.accept((ShipGuiButton.MODEL_STATE_01 + 14), getInverseInt(this.switchPage6[15]));
                }
                else
                {
                    this.send.accept((ShipGuiButton.MODEL_STATE_01 + 15), getInverseInt(this.switchPage6[16]));
                }
            break;
            }
        break;
        case 9:     //AI page 1
            this.showPageAI = 1;
        break;
        case 10:    //AI page 2
            this.showPageAI = 2;
        break;
        case 11:    //AI page 3
            this.showPageAI = 3;
        break;
        case 12:    //AI page 4
            this.showPageAI = 4;
        break;
        case 13:    //AI page 5
            this.showPageAI = 5;
        break;
        case 14:    //AI page 6
            this.showPageAI = 6;
        break;
        case 18:    //AI page 7
            this.showPageAI = 7;
        break;
        case 19:    //AI page 8
            this.showPageAI = 8;
        break;
        case 20:    //AI page 9
            this.showPageAI = 9;
        break;
        case 21:    //AI page 10
            this.showPageAI = 10;
        break;
        case 22:    //AI page 11
            this.showPageAI = 11;
        break;
        case 23:    //AI page 12
            this.showPageAI = 12;
        break;
        case 15:    //inventory page 0
        break;
        case 16:    //inventory page 1
        break;
        case 17:    //inventory page 2
        break;
        }//end all page switch

        if (this.showPage == 2)
        {   //page 2: damage display switch
            switch (LegacyShipInventoryControls.buttonAt(1, xClick, yClick))
            {
            case 0:
                if (this.showAttack == 1)
                {
                    this.showAttack = 2;
                }
                else
                {
                    this.showAttack = 1;
                }
            break;
            }
        }

        //button of morph ship inventory
        switch(supplyButtonAt(xClick, yClick))
        {
        case 0:  //add ammo light
            this.supply.accept(GameplayCommandType.MORPH_ADD_LIGHT_AMMO);
        break;
        case 1:  //add ammo heavy
            this.supply.accept(GameplayCommandType.MORPH_ADD_HEAVY_AMMO);
        break;
        case 2:  //add grudge
            this.supply.accept(GameplayCommandType.MORPH_ADD_GRUDGE);
        break;
        }

    }
private int getInverseInt(boolean par1)
    {
        return par1 ? 0 : 1;
    }
}
