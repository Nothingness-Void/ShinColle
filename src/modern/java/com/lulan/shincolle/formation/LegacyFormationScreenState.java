package com.lulan.shincolle.formation;

public final class LegacyFormationScreenState {
    private static final int[][] BUTTONS = {
            {17,148,34,165}, {35,148,52,165}, {53,148,70,165},
            {71,148,88,165}, {89,148,106,165}, {107,148,124,165},
            {17,166,28,179}, {29,166,40,179}, {41,166,52,179},
            {53,166,64,179}, {65,166,76,179}, {77,166,88,179},
            {89,166,100,179}, {101,166,112,179}, {113,166,124,179},
            {142,5,250,32}, {142,33,250,59}, {142,60,250,86},
            {142,87,250,113}, {142,114,250,140}, {142,141,250,167},
            {159,170,189,180}, {203,170,233,180}, {46,180,94,192}
    };

    public int selectedRow;
    public int team;
    public int formation;
    public int waitTicks;
    public int namingTeam = -1;

    public static int buttonAt(int x, int y) {
        for (int i = 0; i < BUTTONS.length; i++) {
            int[] box = BUTTONS[i];
            if (x >= box[0] && y >= box[1] && x <= box[2] && y <= box[3]) return i;
        }
        return -1;
    }

    public static int moraleIconX(int morale) {
        if (morale > 5100) return 0;
        if (morale > 3900) return 11;
        if (morale > 2100) return 22;
        if (morale > 900) return 33;
        return 44;
    }

    public boolean clickRow(int row) {
        if (row < 0 || row >= 6) return false;
        int oldRow = this.selectedRow;
        this.selectedRow = row;
        return oldRow == row;
    }

    public boolean selectTeam(int team) {
        if (this.namingTeam >= 0 || team < 0 || team >= 9) return false;
        this.team = team;
        this.waitTicks = 60;
        return true;
    }

    public int swapRow(boolean up) {
        if (this.waitTicks > 0) return -1;
        this.selectedRow = (this.selectedRow + (up ? 5 : 1)) % 6;
        this.waitTicks = 40;
        return this.selectedRow;
    }

    // GuiFormation advances these GUI ticks from drawScreen, not world ticks.
    public boolean advanceGuiTick() {
        if (this.waitTicks > 0) {
            this.waitTicks--;
            return this.waitTicks == 1;
        }
        return false;
    }
}
