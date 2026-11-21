package com.lulan.shincolle.menu;

public final class LegacyDeskState {
    private static final int[][][] BUTTONS = {
         {{3,2,19,18}, {22,2,38,18}, {41,2,57,18}, {60,2,76,18}},
         {{7,158,55,170},   {140,23,252,54},  {140,55,252,86},
          {140,87,252,118}, {140,119,252,150},{140,151,252,187},
          {7,172,55,184}},
         {{0,25,122,193},   {123,25,240,193}, {243,34,256,45},
          {243,46,256,59},  {243,60,256,71},  {243,72,256,82},
          {243,83,256,96},  {243,97,256,109}, {243,110,256,121}},
         {{7,158,55,170},   {140,23,252,54},  {140,55,252,86},
          {140,87,252,118}, {140,119,252,150},{140,151,252,187},
          {7,172,55,184},   {86,158,134,170}, {86,172,135,184},
          {7,61,134,91},    {7,92,134,122},   {7,123,134,153}},
         {{7,158,55,170},   {140,23,252,37},  {140,38,252,49},
          {140,50,252,61},  {140,62,252,73},  {140,74,252,85},
          {140,86,252,97},  {140,98,252,109}, {140,110,252,121},
          {140,122,252,133},{140,134,252,145},{140,146,252,157},
          {140,158,252,169},{140,170,252,183}},
         {{18,45,110,157},  {22,158,30,166},  {33,158,41,166},
          {22,169,30,177},  {33,169,41,177},  {44,159,52,167},
          {44,168,52,176},  {53,159,61,167},  {53,168,61,176},
          {62,159,70,167},  {62,168,70,176},  {71,159,79,167},
          {71,168,79,176},  {80,159,88,167},  {80,168,88,176},
          {89,159,97,167},  {89,168,97,176},  {98,159,106,167},
          {98,168,106,176}, {107,159,115,167},{107,168,115,176}}
        };
    public int function, chapter, page, zoom;
    public static int buttonAt(int panel, int x, int y) {
        if (panel < 0 || panel >= BUTTONS.length) return -1;
        for (int i = 0; i < BUTTONS[panel].length; i++) {
            int[] b = BUTTONS[panel][i];
            if (x >= b[0] && x <= b[2] && y >= b[1] && y <= b[3]) return i;
        }
        return -1;
    }
    public void selectFunction(int button) {
        if (button >= 0 && button < 4) function = function == button + 1 ? 0 : button + 1;
    }
    public void bookClick(int button, int mouseButton) {
        if (button < 0 || button > 8) return;
        if (button > 1) { chapter = button - 2; page = 0; }
        else {
            int step = mouseButton == 0 ? 1 : 10;
            page = Math.max(0, Math.min(LegacyDeskBookData.PAGE_LIMIT[chapter], page + (button == 0 ? -step : step)));
        }
    }
    public static boolean opensRadarInventory(int selected, int clicked, int count) {
        return clicked >= 0 && clicked < count && selected == clicked;
    }
    public static int radarPointOffset(int index, int count) {
        return Math.min(Math.max(0, index), Math.max(0, count - 5));
    }
}
