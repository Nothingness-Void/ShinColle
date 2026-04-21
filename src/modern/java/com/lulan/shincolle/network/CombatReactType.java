package com.lulan.shincolle.network;

public enum CombatReactType {
    MISS,
    CRIT,
    DOUBLE_HIT,
    TRIPLE_HIT,
    DODGE,
    LAUNCH,
    HIT;

    public static CombatReactType fromOrdinal(int ordinal) {
        CombatReactType[] values = values();
        if (ordinal < 0 || ordinal >= values.length) {
            return HIT;
        }
        return values[ordinal];
    }
}

