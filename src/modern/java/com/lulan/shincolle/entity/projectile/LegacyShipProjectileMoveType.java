package com.lulan.shincolle.entity.projectile;

public enum LegacyShipProjectileMoveType {
    NONE,
    DIRECT,
    ARC,
    GUIDED,
    DROP;

    public static LegacyShipProjectileMoveType byOrdinal(int ordinal) {
        LegacyShipProjectileMoveType[] values = values();
        if (ordinal < 0 || ordinal >= values.length) {
            return NONE;
        }
        return values[ordinal];
    }
}
