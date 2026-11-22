package com.lulan.shincolle.menu;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class LegacyDeskBookData {
    public static final int[] PAGE_LIMIT = {1, 28, 6, 20, 26, 19, 4};
    public static final List<Integer> ShipBookList = List.of(0, 1, 2, 3, 9, 10, 12, 14, 15, 21, 26, 28, 31, 33, 16, 17, 18, 19, 20, 13, 27, 49, 29, 30, 44, 72);
    public static final List<Integer> EnemyBookList = List.of(36, 37, 38, 39, 46, 47, 48, 51, 52, 53, 54, 56, 57, 58, 59, 60, 61, 62, 63);
    public static final Map<Integer, List<int[]>> BookList = books();
    private LegacyDeskBookData() {}
    private static Map<Integer, List<int[]>> books() {
        Map<Integer, List<int[]>> data = new HashMap<>();
        data.put(0, Arrays.asList(   new int[] {0, 0, 0, 0},
                                new int[] {0, 1, 0, 0}));
        data.put(1, Arrays.asList(   new int[] {0, 0, 0, 0},
                                new int[] {0, 1, 0, 0}));
        data.put(1000, Arrays.asList(new int[] {0, 0, 0, 0},
                                new int[] {0, 1, 0, 0},
                                new int[] {1, 0, 0, 76, 0, 0, 0, 100, 56},
                                new int[] {2, 0, 13, -3, 1},
                                new int[] {2, 0, 43, -3, 2},
                                new int[] {2, 0, 73, -3, 3}));
        data.put(1001, Arrays.asList(new int[] {0, 0, 0, 0},
                                new int[] {0, 1, 0, 0},
                                new int[] {1, 0, 0, -6, 0, 100, 72, 100, 62},
                                new int[] {2, 0, 3, 17,  1},
                                new int[] {2, 0, 23, 17, 0},
                                new int[] {2, 0, 81, 17, 4}));
        data.put(1002, Arrays.asList(new int[] {0, 0, 0, 0},
                                new int[] {0, 1, 0, 0},
                                new int[] {1, 0, 0, -6, 0, 0, 56, 100, 56},
                                new int[] {1, 1, 0, 73, 0, 0, 112, 100, 59},
                                new int[] {2, 0, 5, 52,  9},
                                new int[] {2, 0, 30, 52, 6},
                                new int[] {2, 0, 55, 52, 7},
                                new int[] {2, 0, 80, 52, 8}));
        data.put(1003, Arrays.asList(new int[] {0, 0, 0, 0},
                                new int[] {0, 1, 0, 0},
                                new int[] {1, 0, 0, -6, 0, 100, 72, 100, 62},
                                new int[] {2, 0, 3,  -3, 0},
                                new int[] {2, 0, 23, -3, 0},
                                new int[] {2, 0, 43, -3, 0},
                                new int[] {2, 0, 3,  17, 0},
                                new int[] {2, 0, 23, 17, 1},
                                new int[] {2, 0, 43, 17, 0},
                                new int[] {2, 0, 3,  37, 0},
                                new int[] {2, 0, 23, 37, 10},
                                new int[] {2, 0, 43, 37, 0},
                                new int[] {2, 0, 81, 17, 12},
                                new int[] {2, 1, 3, 110, 12},
                                new int[] {2, 1, 28, 110,13},
                                new int[] {2, 1, 53, 110,14},
                                new int[] {2, 1, 78, 110,15}));
        data.put(1004, Arrays.asList(new int[] {0, 0, 0, 0},
                                new int[] {0, 1, 0, 0},
                                new int[] {1, 0, 0, -6, 0, 100, 72, 100, 62},
                                new int[] {2, 0, 3,  17, 17},
                                new int[] {2, 0, 23, 17, 1},
                                new int[] {2, 0, 81, 17, 16}));
        data.put(1005, Arrays.asList(new int[] {0, 0, 0, 0},
                                new int[] {0, 1, 0, 0},
                                new int[] {1, 0, 0, -6, 0, 100, 72, 100, 62},
                                new int[] {2, 0, 23, -3, 18},
                                new int[] {2, 0, 3,  17, 4},
                                new int[] {2, 0, 43, 17, 4},
                                new int[] {2, 0, 23, 37, 4},
                                new int[] {2, 0, 81, 17, 19}));
        data.put(1006, Arrays.asList(new int[] {0, 0, 0, 0},
                                new int[] {0, 1, 0, 0},
                                new int[] {1, 0, 0, -6, 0, 100, 72, 100, 62},
                                new int[] {2, 0, 3,  17, 20},
                                new int[] {2, 0, 23, 17, 1},
                                new int[] {2, 0, 81, 17, 21}));
        data.put(1007, Arrays.asList(new int[] {0, 0, 0, 0},
                                new int[] {0, 1, 0, 0},
                                new int[] {1, 0, 0, -6, 0, 100, 72, 100, 62},
                                new int[] {2, 0, 3,  -3, 4},
                                new int[] {2, 0, 23, -3, 4},
                                new int[] {2, 0, 43, -3, 4},
                                new int[] {2, 0, 3,  17, 4},
                                new int[] {2, 0, 23, 17, 4},
                                new int[] {2, 0, 43, 17, 4},
                                new int[] {2, 0, 23, 37, 22},
                                new int[] {2, 0, 81, 17, 23}));
        data.put(1008, Arrays.asList(new int[] {0, 0, 0, 0},
                                new int[] {0, 1, 0, 0},
                                new int[] {1, 0, 0, -6, 0, 100, 72, 100, 62},
                                new int[] {2, 0, 3,  -3, 23},
                                new int[] {2, 0, 23, -3, 54},
                                new int[] {2, 0, 43, -3, 78},
                                new int[] {2, 0, 3,  17, 78},
                                new int[] {2, 0, 23, 17, 78},
                                new int[] {2, 0, 43, 17, 78},
                                new int[] {2, 0, 81, 17, 24}));
        data.put(1009, Arrays.asList(new int[] {0, 0, 0, 0},
                                new int[] {0, 1, 0, 0},
                                new int[] {1, 0, 0, -6, 0, 100, 72, 100, 62},
                                new int[] {2, 0, 3,  17, 23},
                                new int[] {2, 0, 23, 17, 25},
                                new int[] {2, 0, 81, 17, 28}));
        data.put(1010, Arrays.asList(new int[] {0, 0, 0, 0},
                                new int[] {0, 1, 0, 0},
                                new int[] {1, 0, 0, -6, 0, 100, 72, 100, 62},
                                new int[] {2, 0, 3,  -3, 2},
                                new int[] {2, 0, 23, -3, 3},
                                new int[] {2, 0, 43, -3, 2},
                                new int[] {2, 0, 3,  17, 3},
                                new int[] {2, 0, 23, 17, 29},
                                new int[] {2, 0, 43, 17, 3},
                                new int[] {2, 0, 3,  37, 2},
                                new int[] {2, 0, 23, 37, 3},
                                new int[] {2, 0, 43, 37, 2},
                                new int[] {2, 0, 81, 17, 30}));
        data.put(1011, Arrays.asList(new int[] {0, 0, 0, 0},
                                new int[] {0, 1, 0, 0},
                                new int[] {1, 0, 0, -6, 0, 100, 72, 100, 62},
                                new int[] {2, 0, 3,  37, 6},
                                new int[] {2, 0, 23, 17, 6},
                                new int[] {2, 0, 43, -3, 2},
                                new int[] {2, 0, 81, 17, 31}));
        data.put(1012, Arrays.asList(new int[] {0, 0, 0, 0},
                                new int[] {0, 1, 0, 0}));
        data.put(1013, Arrays.asList(new int[] {0, 0, 0, 0},
                                new int[] {0, 1, 0, 0}));
        data.put(1014, Arrays.asList(new int[] {0, 0, 0, 0},
                                new int[] {0, 1, 0, 0},
                                new int[] {1, 0, 0, -6, 0, 100, 72, 100, 62},
                                new int[] {2, 0, 23, -3, 6},
                                new int[] {2, 0, 3,  17, 6},
                                new int[] {2, 0, 23, 17, 6},
                                new int[] {2, 0, 43, 17, 6},
                                new int[] {2, 0, 23, 37, 6},
                                new int[] {2, 0, 81, 17, 32}));
        data.put(1015, Arrays.asList(new int[] {0, 0, 0, 0},
                                new int[] {0, 1, 0, 0},
                                new int[] {1, 0, 0, -6, 0, 100, 72, 100, 62},
                                new int[] {2, 0, 3,  -3, 1},
                                new int[] {2, 0, 23, -3, 1},
                                new int[] {2, 0, 43, -3, 1},
                                new int[] {2, 0, 3,  17, 1},
                                new int[] {2, 0, 23, 17, 52},
                                new int[] {2, 0, 43, 17, 1},
                                new int[] {2, 0, 3,  37, 1},
                                new int[] {2, 0, 23, 37, 1},
                                new int[] {2, 0, 43, 37, 1},
                                new int[] {2, 0, 81, 17, 50}));
        data.put(1016, Arrays.asList(new int[] {0, 0, 0, 0},
                                new int[] {0, 1, 0, 0},
                                new int[] {1, 0, 0, -6, 0, 100, 72, 100, 62},
                                new int[] {2, 0, 3,  -3, 1},
                                new int[] {2, 0, 23, -3, 1},
                                new int[] {2, 0, 43, -3, 1},
                                new int[] {2, 0, 3,  17, 1},
                                new int[] {2, 0, 23, 17, 53},
                                new int[] {2, 0, 43, 17, 1},
                                new int[] {2, 0, 3,  37, 1},
                                new int[] {2, 0, 23, 37, 1},
                                new int[] {2, 0, 43, 37, 1},
                                new int[] {2, 0, 81, 17, 51}));
        data.put(1017, Arrays.asList(new int[] {0, 0, 0, 0},
                                new int[] {0, 1, 0, 0},
                                new int[] {1, 0, 0, -6, 0, 100, 72, 100, 62},
                                new int[] {2, 0, 3,  -3, 51},
                                new int[] {2, 0, 23, -3, 50},
                                new int[] {2, 0, 43, -3, 35},
                                new int[] {2, 0, 3,  17, 34},
                                new int[] {2, 0, 23, 17, 34},
                                new int[] {2, 0, 43, 17, 34},
                                new int[] {2, 0, 3,  37, 34},
                                new int[] {2, 0, 43, 37, 34},
                                new int[] {2, 0, 81, 17, 33}));
        data.put(1018, Arrays.asList(new int[] {0, 0, 0, 0},
                                new int[] {0, 1, 0, 0},
                                new int[] {1, 0, 0, -6, 0, 100, 72, 100, 62},
                                new int[] {2, 0, 3,  -3, 4},
                                new int[] {2, 0, 43, -3, 4},
                                new int[] {2, 0, 3,  17, 4},
                                new int[] {2, 0, 23, 17, 4},
                                new int[] {2, 0, 43, 17, 4},
                                new int[] {2, 0, 23, 37, 4},
                                new int[] {2, 0, 81, 17, 54}));
        data.put(1019, Arrays.asList(new int[] {0, 0, 0, 0},
                                new int[] {0, 1, 0, 0},
                                new int[] {1, 0, 0, -6, 0, 100, 72, 100, 62},
                                new int[] {2, 0, 3,  -3, 34},
                                new int[] {2, 0, 23, -3, 66},
                                new int[] {2, 0, 43, -3, 34},
                                new int[] {2, 0, 3,  17, 66},
                                new int[] {2, 0, 23, 17, 2},
                                new int[] {2, 0, 43, 17, 66},
                                new int[] {2, 0, 3,  37, 34},
                                new int[] {2, 0, 23, 37, 66},
                                new int[] {2, 0, 43, 37, 34},
                                new int[] {2, 0, 81, 17, 56}));
        data.put(1020, Arrays.asList(new int[] {0, 0, 0, 0},
                                new int[] {0, 1, 0, 0},
                                new int[] {1, 0, 0, -6, 0, 100, 72, 100, 62},
                                new int[] {2, 0, 3,  -3, 34},
                                new int[] {2, 0, 23, -3, 56},
                                new int[] {2, 0, 43, -3, 34},
                                new int[] {2, 0, 3,  17, 56},
                                new int[] {2, 0, 23, 17, 3},
                                new int[] {2, 0, 43, 17, 56},
                                new int[] {2, 0, 3,  37, 34},
                                new int[] {2, 0, 23, 37, 56},
                                new int[] {2, 0, 43, 37, 34},
                                new int[] {2, 0, 81, 17, 55}));
        data.put(1021, Arrays.asList(new int[] {0, 0, 0, 0},
                                new int[] {0, 1, 0, 0},
                                new int[] {1, 0, 0, -6, 0, 100, 72, 100, 62},
                                new int[] {2, 0, 3,  -3, 4},
                                new int[] {2, 0, 43, -3, 4},
                                new int[] {2, 0, 23, 17, 34},
                                new int[] {2, 0, 3,  37, 4},
                                new int[] {2, 0, 43, 37, 4},
                                new int[] {2, 0, 81, 17, 61}));
        data.put(1022, Arrays.asList(new int[] {0, 0, 0, 0},
                                new int[] {0, 1, 0, 0},
                                new int[] {1, 0, 0, -6, 0, 100, 72, 100, 62},
                                new int[] {2, 0, 23, 17, 1},
                                new int[] {2, 0, 23, 37, 22},
                                new int[] {2, 0, 81, 17, 62}));
        data.put(1023, Arrays.asList(new int[] {0, 0, 0, 0},
                                new int[] {0, 1, 0, 0},
                                new int[] {1, 0, 0, -6, 0, 100, 72, 100, 62},
                                new int[] {2, 0, 3,  -3, 4},
                                new int[] {2, 0, 23, -3, 4},
                                new int[] {2, 0, 43, -3, 4},
                                new int[] {2, 0, 3,  17, 4},
                                new int[] {2, 0, 23, 17, 2},
                                new int[] {2, 0, 43, 17, 4},
                                new int[] {2, 0, 3,  37, 4},
                                new int[] {2, 0, 23, 37, 64},
                                new int[] {2, 0, 43, 37, 4},
                                new int[] {2, 0, 81, 17, 63}));
        data.put(1024, Arrays.asList(new int[] {3, 4, 1},
                                new int[] {0, 0, 0, 0},
                                new int[] {0, 1, 0, 0},
                                new int[] {1, 0, 0, -6, 0, 100, 72, 100, 62},
                                new int[] {2, 0, 3,  -3, 23},
                                new int[] {2, 0, 23, -3, 24},
                                new int[] {2, 0, 43, -3, 52},
                                new int[] {2, 0, 3,  17, 78},
                                new int[] {2, 0, 23, 17, 78},
                                new int[] {2, 0, 43, 17, 78},
                                new int[] {2, 0, 3,  37, 78},
                                new int[] {2, 0, 81, 17, 65},
                                new int[] {1, 2, 0, -6, 0, 100, 72, 100, 62},
                                new int[] {2, 2, 3,  -3, 77},
                                new int[] {2, 2, 23, -3, 77},
                                new int[] {2, 2, 43, -3, 77},
                                new int[] {2, 2, 3,  17, 77},
                                new int[] {2, 2, 23, 17, 77},
                                new int[] {2, 2, 43, 17, 77},
                                new int[] {2, 2, 3,  37, 77},
                                new int[] {2, 2, 23, 37, 77},
                                new int[] {2, 2, 43, 37, 77},
                                new int[] {2, 2, 81, 17, 78},
                                new int[] {1, 4, 0, -6, 0, 100, 72, 100, 62},
                                new int[] {2, 4, 3,  -3, 79},
                                new int[] {2, 4, 23, -3, 79},
                                new int[] {2, 4, 43, -3, 79},
                                new int[] {2, 4, 3,  17, 79},
                                new int[] {2, 4, 23, 17, 1},
                                new int[] {2, 4, 43, 17, 79},
                                new int[] {2, 4, 3,  37, 79},
                                new int[] {2, 4, 23, 37, 79},
                                new int[] {2, 4, 43, 37, 79},
                                new int[] {2, 4, 81, 17, 77}));
        data.put(1025, Arrays.asList(new int[] {3, 6, 1},
                                new int[] {0, 0, 0, 0},
                                new int[] {0, 1, 0, 0},
                                new int[] {1, 0, 0, -6, 0, 100, 72, 100, 62},
                                new int[] {2, 0, 3,  -3, 6},
                                new int[] {2, 0, 23, -3, 71},
                                new int[] {2, 0, 43, -3, 6},
                                new int[] {2, 0, 3,  17, 6},
                                new int[] {2, 0, 23, 17, 71},
                                new int[] {2, 0, 43, 17, 6},
                                new int[] {2, 0, 3,  37, 6},
                                new int[] {2, 0, 23, 37, 71},
                                new int[] {2, 0, 43, 37, 6},
                                new int[] {2, 0, 81, 17, 67},
                                new int[] {1, 2, 0, -6, 0, 100, 72, 100, 62},
                                new int[] {2, 2, 3,  -3, 34},
                                new int[] {2, 2, 23, -3, 67},
                                new int[] {2, 2, 43, -3, 34},
                                new int[] {2, 2, 3,  17, 34},
                                new int[] {2, 2, 23, 17, 67},
                                new int[] {2, 2, 43, 17, 34},
                                new int[] {2, 2, 3,  37, 34},
                                new int[] {2, 2, 23, 37, 67},
                                new int[] {2, 2, 43, 37, 34},
                                new int[] {2, 2, 81, 17, 68},
                                new int[] {1, 4, 0, -6, 0, 100, 72, 100, 62},
                                new int[] {2, 4, 3,  -3, 5},
                                new int[] {2, 4, 23, -3, 68},
                                new int[] {2, 4, 43, -3, 5},
                                new int[] {2, 4, 3,  17, 5},
                                new int[] {2, 4, 23, 17, 68},
                                new int[] {2, 4, 43, 17, 5},
                                new int[] {2, 4, 3,  37, 5},
                                new int[] {2, 4, 23, 37, 68},
                                new int[] {2, 4, 43, 37, 5},
                                new int[] {2, 4, 81, 17, 69},
                                new int[] {1, 6, 0, -6, 0, 100, 72, 100, 62},
                                new int[] {2, 6, 3,  -3, 3},
                                new int[] {2, 6, 23, -3, 69},
                                new int[] {2, 6, 43, -3, 3},
                                new int[] {2, 6, 3,  17, 3},
                                new int[] {2, 6, 23, 17, 69},
                                new int[] {2, 6, 43, 17, 3},
                                new int[] {2, 6, 3,  37, 3},
                                new int[] {2, 6, 23, 37, 69},
                                new int[] {2, 6, 43, 37, 3},
                                new int[] {2, 6, 81, 17, 70}));
        data.put(1026, Arrays.asList(new int[] {0, 0, 0, 0},
                                new int[] {0, 1, 0, 0},
                                new int[] {1, 0, 0, -6, 0, 100, 72, 100, 62},
                                new int[] {2, 0, 3,  17, 1},
                                new int[] {2, 0, 23, 17, 20},
                                new int[] {2, 0, 43, 17, 72},
                                new int[] {2, 0, 81, 17, 73}));
        data.put(1027, Arrays.asList(new int[] {0, 0, 0, 0},
                                new int[] {0, 1, 0, 0},
                                new int[] {2, 0, 43, 17, 74}));
        data.put(1028, Arrays.asList(new int[] {0, 0, 0, 0},
                                new int[] {0, 1, 0, 0},
                                new int[] {1, 0, 0, -6, 0, 100, 72, 100, 62},
                                new int[] {2, 0, 3,  -3, 76},
                                new int[] {2, 0, 23, -3, 76},
                                new int[] {2, 0, 43, -3, 76},
                                new int[] {2, 0, 3,  17, 76},
                                new int[] {2, 0, 23, 17, 75},
                                new int[] {2, 0, 43, 17, 76},
                                new int[] {2, 0, 3,  37, 76},
                                new int[] {2, 0, 23, 37, 76},
                                new int[] {2, 0, 43, 37, 76},
                                new int[] {2, 0, 81, 17, 75}));
        data.put(2000, Arrays.asList(new int[] {0, 0, 0, 0},
                                new int[] {0, 1, 0, 0},
                                new int[] {1, 0, 0, -6, 0, 100, 72, 100, 62},
                                new int[] {1, 1, 0, -6, 0, 100, 134, 100, 46},
                                new int[] {2, 0, 3,  -3, 1},
                                new int[] {2, 0, 23, -3, 17},
                                new int[] {2, 0, 43, -3, 1},
                                new int[] {2, 0, 3,  17, 17},
                                new int[] {2, 0, 23, 17, 34},
                                new int[] {2, 0, 43, 17, 17},
                                new int[] {2, 0, 3,  37, 34},
                                new int[] {2, 0, 23, 37, 34},
                                new int[] {2, 0, 43, 37, 34},
                                new int[] {2, 0, 81, 17, 36}));
        data.put(2001, Arrays.asList(new int[] {0, 0, 0, 0},
                                new int[] {0, 1, 0, 0},
                                new int[] {1, 0, 25, -12, 0, 0, 230, 50, 26}));
        data.put(2002, Arrays.asList(new int[] {0, 0, 0, 0},
                                new int[] {0, 1, 0, 0},
                                new int[] {1, 0, 25, -12, 0, 50, 230, 50, 26}));
        data.put(2003, Arrays.asList(new int[] {0, 0, 0, 0},
                                new int[] {0, 1, 0, 0},
                                new int[] {1, 0, 0, 60, 0, 100, 180, 100, 65},
                                new int[] {1, 1, -7, -18, 0, 200, 0, 38, 38},
                                new int[] {1, 1, 31, -18, 0, 200, 38, 38, 38},
                                new int[] {1, 1, 69, -18, 0, 200, 76, 38, 38}));
        data.put(2004, Arrays.asList(new int[] {0, 0, 0, 0},
                                new int[] {0, 1, 0, 0}));
        data.put(3013, Arrays.asList(new int[] {0, 0, 0, 0},
                                new int[] {0, 1, 0, 0},
                                new int[] {1, 0, 0, 120, 0, 100, 245, 100, 11}));
        return Collections.unmodifiableMap(data);
    }
    public static ItemStack icon(int id) {
        String key = switch (id) {
            case 0 -> "minecraft:iron_ingot";
            case 1 -> "shincolle:grudge";
            case 2 -> "shincolle:blockgrudge";
            case 3 -> "shincolle:blockgrudgeheavy";
            case 4 -> "shincolle:abyssmetal";
            case 5 -> "shincolle:blockabyssium";
            case 6 -> "shincolle:abyssmetal1";
            case 9 -> "shincolle:blockpolymetalore";
            case 7 -> "shincolle:blockpolymetal";
            case 8 -> "shincolle:blockpolymetalgravel";
            case 10 -> "minecraft:gunpowder";
            case 11 -> "minecraft:blaze_powder";
            case 12 -> "shincolle:ammo";
            case 13 -> "shincolle:ammo1";
            case 14 -> "shincolle:ammo2";
            case 15 -> "shincolle:ammo3";
            case 16 -> "shincolle:bucketrepair";
            case 17 -> "minecraft:lava_bucket";
            case 18 -> "minecraft:nether_star";
            case 19 -> "shincolle:marriagering";
            case 20 -> "minecraft:paper";
            case 21 -> "shincolle:ownerpaper";
            case 22 -> "minecraft:stick";
            case 23 -> "shincolle:kaitaihammer";
            case 24 -> "shincolle:modernkit";
            case 25 -> "shincolle:smallegg";
            case 26 -> "shincolle:largeegg";
            case 27 -> "shincolle:shipegg2";
            case 28 -> "shincolle:instantconmat";
            case 29 -> "minecraft:diamond_block";
            case 30 -> "shincolle:repairgoddess";
            case 31 -> "shincolle:pointeritem";
            case 32 -> "shincolle:toyairplane";
            case 33 -> "shincolle:blockdesk";
            case 50 -> "shincolle:deskitembook";
            case 51 -> "shincolle:deskitemradar";
            case 52 -> "minecraft:writable_book";
            case 53 -> "minecraft:compass";
            case 34 -> "minecraft:obsidian";
            case 35 -> "minecraft:white_wool";
            case 36 -> "shincolle:blocksmallshipyard";
            case 54 -> "shincolle:targetwrench";
            case 55 -> "shincolle:blockvolcore";
            case 56 -> "shincolle:blockvolblock";
            case 61 -> "shincolle:blockframe";
            case 62 -> "shincolle:blockwaypoint";
            case 63 -> "shincolle:blockcrane";
            case 64 -> "minecraft:piston";
            case 65 -> "shincolle:trainingbook";
            case 66 -> "minecraft:magma_block";
            case 67 -> "shincolle:shiptank";
            case 68 -> "shincolle:shiptank1";
            case 69 -> "shincolle:shiptank2";
            case 70 -> "shincolle:shiptank3";
            case 71 -> "minecraft:cauldron";
            case 72 -> "minecraft:lapis_lazuli";
            case 73 -> "shincolle:recipepaper";
            case 74 -> "shincolle:optool";
            case 75 -> "shincolle:equipammo7";
            case 76 -> "minecraft:potion";
            case 77 -> "shincolle:grudge1";
            case 78 -> "shincolle:blockgrudgexp";
            case 79 -> "minecraft:experience_bottle";
            default -> null;
        };
        return key == null ? ItemStack.EMPTY : new ItemStack(BuiltInRegistries.ITEM.get(ResourceLocation.parse(key)));
    }
}
