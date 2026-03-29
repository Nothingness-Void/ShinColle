package com.lulan.shincolle.network;

public enum GameplayParticleType {
    LAUNCH_SMOKE,
    HIT_EXPLOSION,
    AIR_TRAIL,
    TEXT_MISS,
    TEXT_CRIT,
    TEXT_DOUBLE,
    TEXT_TRIPLE,
    TEXT_DODGE;

    public static GameplayParticleType fromOrdinal(int ordinal) {
        GameplayParticleType[] values = values();
        if (ordinal < 0 || ordinal >= values.length) {
            return HIT_EXPLOSION;
        }
        return values[ordinal];
    }
}

