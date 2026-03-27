package com.lulan.shincolle.entity.ship;

import java.util.Arrays;

public final class LegacyShipStatTables {

    public static final int ATTR_COUNT = 21;
    public static final int BASE_ATTR_COUNT = 12;
    public static final int BASE_GROWTH_COUNT = 6;

    public static final int MORALE_EXCITED_LOWER = 12000;
    public static final int MORALE_HAPPY_LOWER = 8000;
    public static final int MORALE_NORMAL_LOWER = 4000;
    public static final int MORALE_TIRED_LOWER = 1500;

    public static final double[] SCALE_SHIP = {1D, 1D, 1D, 1D, 1D, 1D};
    public static final double[] SCALE_MOB_SMALL = {250D, 25D, 0.15D, 0.7D, 0.45D, 12D};
    public static final double[] SCALE_MOB_LARGE = {500D, 50D, 0.30D, 0.9D, 0.4D, 15D};
    public static final double[] LIMIT_SHIP_ATTRS = {
            -1D, -1D, -1D, -1D, -1D,
            0.95D, 4D, 0.6D, 64D, 0.95D,
            0.95D, 0.95D, 0.95D, -1D, -1D,
            0.75D, -1D, -1D, -1D, -1D,
            1D
    };

    private static final float[] RESET_FORMATION = {
            0F, 1F, 1F, 1F, 1F,
            1F, 1F, 0F, 0F, 1F,
            1F, 1F, 1F, 1F, 1F,
            0F, 0F, 0F, 0F, 0F,
            0F
    };
    private static final float[] RESET_MORALE = {
            0F, 1F, 1F, 1F, 1F,
            0F, 1F, 0F, 0F, 1F,
            1F, 1F, 1F, 1F, 1F,
            0F, 0F, 0F, 0F, 0F,
            0F
    };
    private static final float[] HOSTILE_DEFAULT = {0.35F, 0.35F, 0.35F, 1F, 1.1F, 0.7F};

    private LegacyShipStatTables() {
    }

    public static float[] copyBaseStats(int legacyClassId) {
        float[] stats = switch (legacyClassId) {
            case 0 -> new float[] {20F, 3F, 0.05F, 1.0F, 0.5F, 6F, 0.3F, 0.25F, 0.11F, 0.5F, 1F, 0.4F};
            case 1 -> new float[] {22F, 4F, 0.06F, 1.0F, 0.5F, 6F, 0.32F, 0.28F, 0.12F, 0.5F, 1F, 0.4F};
            case 2 -> new float[] {24F, 3F, 0.07F, 1.0F, 0.5F, 6F, 0.34F, 0.25F, 0.13F, 0.5F, 1F, 0.4F};
            case 3 -> new float[] {28F, 4F, 0.09F, 1.0F, 0.5F, 6F, 0.36F, 0.28F, 0.15F, 0.5F, 1F, 0.4F};
            case 9 -> new float[] {58F, 14F, 0.18F, 1.0F, 0.42F, 9F, 0.48F, 0.4F, 0.21F, 0.56F, 0.84F, 0.5F};
            case 10 -> new float[] {62F, 15F, 0.19F, 1.0F, 0.42F, 9F, 0.5F, 0.42F, 0.22F, 0.56F, 0.84F, 0.5F};
            case 12 -> new float[] {85F, 25F, 0.21F, 1.0F, 0.36F, 16F, 0.65F, 0.6F, 0.23F, 0.6F, 0.72F, 0.6F};
            case 13 -> new float[] {95F, 30F, 0.30F, 1.0F, 0.32F, 12F, 0.85F, 0.65F, 0.27F, 0.63F, 0.66F, 0.5F};
            case 14 -> new float[] {84F, 19F, 0.23F, 1.2F, 0.42F, 10F, 0.65F, 0.55F, 0.24F, 0.7F, 0.84F, 0.5F};
            case 15 -> new float[] {120F, 27F, 0.25F, 1.1F, 0.36F, 12F, 0.8F, 0.65F, 0.25F, 0.63F, 0.72F, 0.5F};
            case 16 -> new float[] {90F, 3F, 0.10F, 1.0F, 0.3F, 8F, 0.7F, 0.25F, 0.16F, 0.35F, 0.6F, 0.3F};
            case 17 -> new float[] {40F, 28F, 0.09F, 0.8F, 0.3F, 5F, 0.35F, 0.67F, 0.14F, 0.7F, 0.6F, 0.3F};
            case 18 -> new float[] {36F, 30F, 0.10F, 0.8F, 0.3F, 5F, 0.33F, 0.7F, 0.16F, 0.7F, 0.6F, 0.3F};
            case 19 -> new float[] {34F, 38F, 0.12F, 0.8F, 0.28F, 5.5F, 0.3F, 0.8F, 0.18F, 0.7F, 0.6F, 0.3F};
            case 20 -> new float[] {225F, 13F, 0.34F, 0.9F, 0.22F, 24F, 1.3F, 0.4F, 0.29F, 0.6F, 0.44F, 0.8F};
            case 21 -> new float[] {240F, 16F, 0.32F, 1.0F, 0.3F, 26F, 1.2F, 0.45F, 0.28F, 0.6F, 0.6F, 0.8F};
            case 26 -> new float[] {220F, 42F, 0.40F, 1.0F, 0.4F, 16F, 1.0F, 0.8F, 0.32F, 0.73F, 0.8F, 0.6F};
            case 27 -> new float[] {180F, 40F, 0.28F, 1.0F, 0.45F, 22F, 0.85F, 0.75F, 0.26F, 0.62F, 0.85F, 0.7F};
            case 28 -> new float[] {90F, 22F, 0.20F, 1.0F, 0.52F, 12F, 0.55F, 0.5F, 0.22F, 0.6F, 1F, 0.5F};
            case 29 -> new float[] {260F, 14F, 0.36F, 0.8F, 0.2F, 24F, 1.35F, 0.4F, 0.3F, 0.6F, 0.4F, 0.8F};
            case 30 -> new float[] {350F, 22F, 0.45F, 0.8F, 0.25F, 30F, 1.5F, 0.5F, 0.34F, 0.6F, 0.4F, 0.8F};
            case 31 -> new float[] {210F, 13F, 0.30F, 0.8F, 0.32F, 22F, 1.15F, 0.35F, 0.27F, 0.6F, 0.64F, 0.8F};
            case 33 -> new float[] {190F, 45F, 0.40F, 1.0F, 0.42F, 25F, 1F, 0.95F, 0.32F, 0.75F, 0.84F, 0.8F};
            case 36 -> new float[] {38F, 11F, 0.12F, 1.0F, 0.6F, 9F, 0.35F, 0.4F, 0.16F, 0.55F, 1.2F, 0.46F};
            case 37 -> new float[] {135F, 40F, 0.26F, 1.0F, 0.32F, 14F, 0.85F, 0.8F, 0.25F, 0.63F, 0.64F, 0.6F};
            case 38 -> new float[] {28F, 30F, 0.07F, 0.8F, 0.3F, 10F, 0.3F, 0.7F, 0.13F, 0.7F, 0.6F, 0.4F};
            case 39 -> new float[] {32F, 32F, 0.10F, 0.8F, 0.3F, 11F, 0.33F, 0.75F, 0.16F, 0.7F, 0.6F, 0.4F};
            case 44 -> new float[] {75F, 45F, 0.15F, 1.0F, 0.3F, 7.5F, 0.5F, 0.9F, 0.2F, 0.7F, 0.6F, 0.4F};
            case 46 -> new float[] {150F, 55F, 0.36F, 1.0F, 0.3F, 20F, 1F, 1F, 0.3F, 0.7F, 0.6F, 0.7F};
            case 47 -> new float[] {70F, 22F, 0.21F, 1.0F, 0.34F, 16F, 0.65F, 0.6F, 0.23F, 0.6F, 0.72F, 0.6F};
            case 48 -> new float[] {75F, 22F, 0.20F, 1.0F, 0.32F, 16F, 0.65F, 0.6F, 0.23F, 0.6F, 0.72F, 0.6F};
            case 49 -> new float[] {180F, 35F, 0.32F, 1.0F, 0.45F, 14F, 0.85F, 0.77F, 0.29F, 0.65F, 0.9F, 0.6F};
            case 51 -> new float[] {32F, 9F, 0.09F, 1.0F, 0.5F, 11F, 0.32F, 0.38F, 0.12F, 0.5F, 1F, 0.5F};
            case 52 -> new float[] {40F, 7F, 0.11F, 1.0F, 0.5F, 10F, 0.38F, 0.36F, 0.14F, 0.5F, 1F, 0.48F};
            case 53 -> new float[] {30F, 5F, 0.09F, 1.0F, 0.5F, 9F, 0.3F, 0.32F, 0.12F, 0.5F, 1F, 0.46F};
            case 54 -> new float[] {30F, 5F, 0.09F, 1.0F, 0.5F, 9F, 0.3F, 0.32F, 0.12F, 0.5F, 1F, 0.46F};
            case 56 -> new float[] {42F, 13F, 0.16F, 1.0F, 0.42F, 8F, 0.4F, 0.4F, 0.2F, 0.6F, 0.9F, 0.4F};
            case 57 -> new float[] {42F, 13F, 0.16F, 1.0F, 0.42F, 8F, 0.4F, 0.4F, 0.2F, 0.6F, 0.9F, 0.4F};
            case 58 -> new float[] {62F, 15F, 0.18F, 1.0F, 0.42F, 9F, 0.5F, 0.42F, 0.22F, 0.56F, 0.84F, 0.5F};
            case 59 -> new float[] {62F, 15F, 0.18F, 1.0F, 0.42F, 9F, 0.5F, 0.42F, 0.22F, 0.56F, 0.84F, 0.5F};
            case 60 -> new float[] {90F, 28F, 0.36F, 1.0F, 0.42F, 12F, 0.7F, 0.6F, 0.24F, 0.6F, 0.84F, 0.55F};
            case 61 -> new float[] {90F, 28F, 0.36F, 1.0F, 0.42F, 12F, 0.7F, 0.6F, 0.24F, 0.6F, 0.84F, 0.55F};
            case 62 -> new float[] {90F, 28F, 0.36F, 1.0F, 0.42F, 12F, 0.7F, 0.6F, 0.24F, 0.6F, 0.84F, 0.55F};
            case 63 -> new float[] {90F, 28F, 0.36F, 1.0F, 0.42F, 12F, 0.7F, 0.6F, 0.24F, 0.6F, 0.84F, 0.55F};
            case 72 -> new float[] {55F, 34F, 0.10F, 1.0F, 0.4F, 5.5F, 0.4F, 0.75F, 0.16F, 0.7F, 0.7F, 0.4F};
            default -> null;
        };

        return stats != null ? stats : copyBaseStats(0);
    }

    public static float[] copyHostileModifier(int friendlyClassId) {
        float[] stats = switch (friendlyClassId) {
            case 36 -> new float[] {0.4F, 0.5F, 0.4F, 1.2F, 1.2F, 0.75F};
            case 37 -> new float[] {1.1F, 1.1F, 1.1F, 1F, 0.8F, 1.05F};
            case 38 -> new float[] {0.25F, 0.8F, 0.25F, 0.75F, 0.4F, 0.4F};
            case 39 -> new float[] {0.25F, 0.8F, 0.25F, 0.75F, 0.4F, 0.4F};
            case 46 -> new float[] {1.2F, 1.2F, 1.2F, 1.2F, 0.8F, 1.1F};
            case 47, 48 -> new float[] {0.8F, 0.8F, 0.8F, 0.75F, 0.8F, 1.2F};
            case 51, 52, 53, 54 -> new float[] {0.35F, 0.35F, 0.35F, 1F, 1.1F, 0.7F};
            case 56, 57 -> new float[] {0.55F, 0.6F, 0.5F, 1F, 0.9F, 0.8F};
            case 58, 59 -> new float[] {0.8F, 0.8F, 0.8F, 0.8F, 0.8F, 0.9F};
            case 60, 61, 62, 63 -> new float[] {1F, 1.05F, 1F, 1F, 1F, 1F};
            default -> HOSTILE_DEFAULT;
        };

        return Arrays.copyOf(stats, BASE_GROWTH_COUNT);
    }

    public static float[] copyMoraleStats(int moraleValue) {
        float[] stats;
        if (moraleValue > MORALE_EXCITED_LOWER) {
            stats = new float[] {0F, 1.25F, 1.25F, 1.25F, 1.25F, 0.2F, 1.4F, 0.15F, 4F, 1.2F, 1.2F, 1.2F, 1.5F, 1.5F, 1.5F, 0.25F, 0.5F, 0.5F, 0.5F, 0.5F, 0.25F};
        } else if (moraleValue > MORALE_HAPPY_LOWER) {
            stats = new float[] {0F, 1.1F, 1.1F, 1.1F, 1.1F, 0.1F, 1.2F, 0.08F, 2F, 1.1F, 1.1F, 1.1F, 1.25F, 1.25F, 1.25F, 0.12F, 0.25F, 0.25F, 0.25F, 0.25F, 0.15F};
        } else if (moraleValue <= MORALE_TIRED_LOWER) {
            stats = new float[] {0F, 0.75F, 0.75F, 0.75F, 0.75F, -0.2F, 0.6F, -0.15F, -4F, 0.8F, 0.8F, 0.8F, 0.5F, 0.5F, 0.5F, -0.25F, -0.5F, -0.5F, -0.5F, -0.5F, -0.2F};
        } else if (moraleValue <= MORALE_NORMAL_LOWER) {
            stats = new float[] {0F, 0.9F, 0.9F, 0.9F, 0.9F, -0.1F, 0.8F, -0.08F, -2F, 0.9F, 0.9F, 0.9F, 0.75F, 0.75F, 0.75F, -0.12F, -0.25F, -0.25F, -0.25F, -0.25F, -0.1F};
        } else {
            stats = RESET_MORALE;
        }

        return Arrays.copyOf(stats, ATTR_COUNT);
    }

    public static float[] copyResetFormation() {
        return Arrays.copyOf(RESET_FORMATION, ATTR_COUNT);
    }

    public static float[] copyResetMorale() {
        return Arrays.copyOf(RESET_MORALE, ATTR_COUNT);
    }

    public static double[] hostileScaleFor(ShipArchetype archetype) {
        return switch (archetype) {
            case DESTROYER, SUBMARINE -> SCALE_MOB_SMALL;
            default -> SCALE_MOB_LARGE;
        };
    }

    public static void clamp(float[] data) {
        for (int index = 0; index < ATTR_COUNT; index++) {
            double limit = LIMIT_SHIP_ATTRS[index];
            if (limit >= 0D && data[index] > limit) {
                data[index] = (float) limit;
            }
        }

        for (int index = 0; index < data.length; index++) {
            if (data[index] < 0F) {
                data[index] = 0F;
            }
        }

        if (data[Attr.HP] < 1F) {
            data[Attr.HP] = 1F;
        }
        if (data[Attr.ATK_L] < 1F) {
            data[Attr.ATK_L] = 1F;
        }
        if (data[Attr.ATK_H] < 1F) {
            data[Attr.ATK_H] = 1F;
        }
        if (data[Attr.ATK_AL] < 1F) {
            data[Attr.ATK_AL] = 1F;
        }
        if (data[Attr.ATK_AH] < 1F) {
            data[Attr.ATK_AH] = 1F;
        }
        if (data[Attr.HIT] < 1F) {
            data[Attr.HIT] = 1F;
        }
        if (data[Attr.SPD] < 0.1F) {
            data[Attr.SPD] = 0.1F;
        }
    }

    public static final class Attr {
        public static final int HP = 0;
        public static final int ATK_L = 1;
        public static final int ATK_H = 2;
        public static final int ATK_AL = 3;
        public static final int ATK_AH = 4;
        public static final int DEF = 5;
        public static final int SPD = 6;
        public static final int MOV = 7;
        public static final int HIT = 8;
        public static final int CRI = 9;
        public static final int DHIT = 10;
        public static final int THIT = 11;
        public static final int MISS = 12;
        public static final int AA = 13;
        public static final int ASM = 14;
        public static final int DODGE = 15;
        public static final int XP = 16;
        public static final int GRUDGE = 17;
        public static final int AMMO = 18;
        public static final int HPRES = 19;
        public static final int KB = 20;

        private Attr() {
        }
    }

    public static final class BaseAttr {
        public static final int HP = 0;
        public static final int ATK = 1;
        public static final int DEF = 2;
        public static final int SPD = 3;
        public static final int MOV = 4;
        public static final int HIT = 5;
        public static final int MOD_HP = 6;
        public static final int MOD_ATK = 7;
        public static final int MOD_DEF = 8;
        public static final int MOD_SPD = 9;
        public static final int MOD_MOV = 10;
        public static final int MOD_HIT = 11;

        private BaseAttr() {
        }
    }
}
