package com.lulan.shincolle.menu;

import com.lulan.shincolle.network.ShipGuiButton;
import java.util.function.BiConsumer;

public final class LegacyShipInventoryControls {
    private static final int[][][] BUTTONS = {//gui 0: ship inventory
         //0: page0          1: page1          2: page2
         {{133,18,142,52},  {133,53,142,88},  {133,89,142,125},     //0: all
         //3: AI op0         4: AI op1         5: AI op2
          {173,131,237,143},{173,144,237,156},{173,157,237,169},
         //6: AI op3         7: AI op4         8: AI op5
          {173,170,237,182},{173,183,237,195},{173,196,237,208},
         //9: AI page0       10: AI page1      11: AI page2
          {239,131,245,142},{239,144,245,155},{239,157,245,168},
         //12: AI page3      13: AI page4      14: AI page5
          {239,170,245,181},{239,183,245,194},{239,196,245,208},
         //15: inv page0     16: inv page1     17: inv page2
          {61,18,70,52},    {61,53,70,88},    {61,89,70,125},
         //18: AI page6      19: AI page7      20: AI page8
          {246,131,253,142},{246,144,253,155},{246,157,253,168},
         //21: AI page9      22: AI page10     23: AI page11
          {246,170,253,181},{246,183,253,194},{246,196,253,208}},
         //0: attack
          {{73,18,132,40}},                                         //1: attribute
         //0: AI page2 bar0  1: AI page2 bar1  2: AI page3 bar2
          {{187,145,238,154},{187,169,238,178},{187,193,238,202}}   //2: AI control
        };
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

    public void click(int posX, int posY, int mouseKey)
    {


        //get click position
        this.xClick = posX;
        this.yClick = posY;
        this.mousePress = true;

        //check press bar
        if (this.showPageAI == 2)
        {
            switch(LegacyShipInventoryControls.buttonAt(2, xClick, yClick))
            {
            case 0:
                mousePressBar = 0;
                break;
            case 1:
                mousePressBar = 1;
                break;
            case 2:
                mousePressBar = 2;
                break;
            default:
                mousePressBar = -1;
                break;
            }
        }
        else if (this.showPageAI == 5)
        {
            switch(LegacyShipInventoryControls.buttonAt(2, xClick, yClick))
            {
            case 0:
                mousePressBar = 3;
                break;
            case 1:
                mousePressBar = 4;
                break;
            default:
                mousePressBar = -1;
                break;
            }
        }
        else
        {
            mousePressBar = -1;
        }

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
            case 1:     //page 1: can melee button
                this.switchPage1a[0] = this.entity.getStateFlag("UseMelee");
                this.send.accept(ShipGuiButton.MELEE, getInverseInt(this.switchPage1a[0]));
            break;
            case 3:     //page 3: change target AI
                this.switchPage3[0] = this.entity.getStateFlag("PassiveAI");
                this.send.accept(ShipGuiButton.TAR_AI, getInverseInt(this.switchPage3[0]));
            break;
            case 4:     //page 4: pick item AI
                this.switchPage4[0] = this.entity.getStateFlag("PickItem");
                this.send.accept(ShipGuiButton.PICKITEM_AI, getInverseInt(this.switchPage4[0]));
            break;
            case 6:     //page 6: show held item
                this.switchPage6[0] = this.entity.getStateFlag("ShowHeldItem");
                this.send.accept(ShipGuiButton.SHOW_HELD, getInverseInt(this.switchPage6[0]));
            break;
            case 7:     //page 7: change task
                int curTask = this.entity.getStateMinor("Task");
                int newTask = 0;

                if (xClick < 191)
                {
                    if (curTask != 1) newTask = 1;
                    this.send.accept(ShipGuiButton.TASK, newTask);
                }
                else if (xClick < 207)
                {
                    if (curTask != 2) newTask = 2;
                    this.send.accept(ShipGuiButton.TASK, newTask);
                }
                else if (xClick < 223)
                {
                    if (curTask != 3) newTask = 3;
                    this.send.accept(ShipGuiButton.TASK, newTask);
                }
                else
                {
                    if (curTask != 4) newTask = 4;
                    this.send.accept(ShipGuiButton.TASK, newTask);
                }
            break;
            }
        break;
        case 4: //AI operation 1
            switch (this.showPageAI)
            {
            case 1:     //page 1: use ammo light button
                this.switchPage1a[1] = this.entity.getStateFlag("UseAmmoLight");
                this.send.accept(ShipGuiButton.AMMO_LIGHT, getInverseInt(this.switchPage1a[1]));
            break;
            case 3:     //page 3: change onsight AI
                this.switchPage3[1] = this.entity.getStateFlag("OnSightChase");
                this.send.accept(ShipGuiButton.ON_SIGHT_AI, getInverseInt(this.switchPage3[1]));
            break;
            case 4:     //page 4: auto pump
                this.switchPage4[1] = this.entity.getStateFlag("AutoPump");
                this.send.accept(ShipGuiButton.AUTO_PUMP, getInverseInt(this.switchPage4[1]));
            break;
            case 7:     //page 7: change task
                int curTask = this.entity.getStateMinor("Task");
                int newTask = 0;

                if (xClick < 191)
                {
                    if (curTask != 1) newTask = 1;
                    this.send.accept(ShipGuiButton.TASK, newTask);
                }
                else if (xClick < 207)
                {
                    if (curTask != 2) newTask = 2;
                    this.send.accept(ShipGuiButton.TASK, newTask);
                }
                else if (xClick < 223)
                {
                    if (curTask != 3) newTask = 3;
                    this.send.accept(ShipGuiButton.TASK, newTask);
                }
                else
                {
                    if (curTask != 4) newTask = 4;
                    this.send.accept(ShipGuiButton.TASK, newTask);
                }
            break;
            case 8:     //page 8: change input side
                int clickid = 0;
                if (xClick < 185) { clickid = 0; }
                else if (xClick < 196) { clickid = 1; }
                else if (xClick < 207) { clickid = 2; }
                else if (xClick < 218) { clickid = 3; }
                else if (xClick < 229) { clickid = 4; }
                else { clickid = 5; }

                int newValue = this.entity.getStateMinor("TaskSide") ^ POW2[clickid];
                this.send.accept(ShipGuiButton.TASK_SIDE, newValue);
            break;
            }
        break;
        case 5: //AI operation 2
            switch (this.showPageAI)
            {
            case 1:     //page 1: use ammo heavy button
                this.switchPage1a[2] = this.entity.getStateFlag("UseAmmoHeavy");
                this.send.accept(ShipGuiButton.AMMO_HEAVY, getInverseInt(this.switchPage1a[2]));
            break;
            case 3:     //page 3: change PVP first AI
                this.switchPage3[2] = this.entity.getStateFlag("PVPFirst");
                this.send.accept(ShipGuiButton.PVP_AI, getInverseInt(this.switchPage3[2]));
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
            case 7:     //page 7: check metadata
                int newValue = this.entity.getStateMinor("TaskSide") ^ POW2[18];
                this.send.accept(ShipGuiButton.TASK_SIDE, newValue);
            break;
            }
        break;
        case 6: //AI operation 3
            switch (this.showPageAI)
            {
            case 1:     //page 1: use air light button
                this.switchPage1a[3] = this.entity.getStateFlag("UseAirLight");
                this.send.accept(ShipGuiButton.AIR_LIGHT, getInverseInt(this.switchPage1a[3]));
            break;
            case 3:     //page 3: change onsight AI
                this.switchPage3[3] = this.entity.getStateFlag("AntiAir");
                this.send.accept(ShipGuiButton.AA_AI, getInverseInt(this.switchPage3[3]));
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
            case 7:     //page 7: check ore dict
                int newValue = this.entity.getStateMinor("TaskSide") ^ POW2[19];
                this.send.accept(ShipGuiButton.TASK_SIDE, newValue);
            break;
            case 8:     //page 8: change output side
                int clickid = 6;
                if (xClick < 185) { clickid = 6; }
                else if (xClick < 196) { clickid = 7; }
                else if (xClick < 207) { clickid = 8; }
                else if (xClick < 218) { clickid = 9; }
                else if (xClick < 229) { clickid = 10; }
                else { clickid = 11; }

                int newValue2 = this.entity.getStateMinor("TaskSide") ^ POW2[clickid];
                this.send.accept(ShipGuiButton.TASK_SIDE, newValue2);
            break;
            }
        break;
        case 7: //AI operation 4
            switch (this.showPageAI)
            {
            case 1:     //page 1: use air heavy button
                this.switchPage1a[4] = this.entity.getStateFlag("UseAirHeavy");
                this.send.accept(ShipGuiButton.AIR_HEAVY, getInverseInt(this.switchPage1a[4]));
            break;
            case 3:     //page 3: change onsight AI
                this.switchPage3[4] = this.entity.getStateFlag("AntiSS");
                this.send.accept(ShipGuiButton.ASM_AI, getInverseInt(this.switchPage3[4]));
            break;
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
            case 7:     //page 7: check nbt tag
                int newValue = this.entity.getStateMinor("TaskSide") ^ POW2[20];
                this.send.accept(ShipGuiButton.TASK_SIDE, newValue);
            break;
            }
        break;
        case 8: //AI operation 5
            switch (this.showPageAI)
            {
            case 1:     //page 1: apply aura effect
                this.switchPage1a[5] = this.entity.getStateFlag("UseRingEffect");
                this.send.accept(ShipGuiButton.AURA_EFFECT, getInverseInt(this.switchPage1a[5]));
            break;
            case 3:     //page 3: timekeeper AI
                this.switchPage3[5] = this.entity.getStateFlag("TimeKeeper");
                this.send.accept(ShipGuiButton.TIMEKEEP_AI, getInverseInt(this.switchPage3[5]));
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
            case 8:     //page 8: change fuel side
                int clickid = 12;
                if (xClick < 185) { clickid = 12; }
                else if (xClick < 196) { clickid = 13; }
                else if (xClick < 207) { clickid = 14; }
                else if (xClick < 218) { clickid = 15; }
                else if (xClick < 229) { clickid = 16; }
                else { clickid = 17; }

                int newValue2 = this.entity.getStateMinor("TaskSide") ^ POW2[clickid];
                this.send.accept(ShipGuiButton.TASK_SIDE, newValue2);
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
            this.send.accept(ShipGuiButton.INV_PAGE, 0);
        break;
        case 16:    //inventory page 1
            if (this.entity.getInventoryPageSize() > 0)
            {
                this.send.accept(ShipGuiButton.INV_PAGE, 1);
            }
        break;
        case 17:    //inventory page 2
            if (this.entity.getInventoryPageSize() > 1)
            {
                this.send.accept(ShipGuiButton.INV_PAGE, 2);
            }
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

    }

    public void drag(int posX, int posY, int mouseKey)
    {


        //get click position
        xClick = posX;
        yClick = posY;

        barPos = xClick - 191;
        if (barPos > 42) barPos = 42;
        if (barPos < 0)  barPos = 0;

    }

    public void release(int posX, int posY, int state)
    {


        //get click position
        xClick = posX;
        yClick = posY;

        //get cliuck button
        int barvalue = 0;

        switch (mousePressBar)
        {
        case 0: //bar0: follow min
            barvalue = (int)(barPos / 42F * 30F + 1F);
            this.send.accept(ShipGuiButton.FOLLOW_MIN, barvalue);
            break;
        case 1: //bar1: follow max
            barvalue = (int)(barPos / 42F * 30F + 2F);
            this.send.accept(ShipGuiButton.FOLLOW_MAX, barvalue);
            break;
        case 2: //bar2: flee hp
            barvalue = (int)(barPos / 42F * 100F);
            this.send.accept(ShipGuiButton.FLEE_HP, barvalue);
            break;
        case 3: //bar3: wp stay time
            barvalue = (int)(barPos / (42F * 0.0625F));
            this.send.accept(ShipGuiButton.WP_STAY, barvalue);
            break;
        case 4: //bar4: auto use combat ration
            barvalue = (int)(barPos / (43F * 0.25F)) + 1;
            this.send.accept(ShipGuiButton.AUTO_CR, barvalue);
            break;
        }

        //reset flag
        mousePress = false;
        mousePressBar = -1;
    }

    private int getInverseInt(boolean par1)
    {
        return par1 ? 0 : 1;
    }
}
