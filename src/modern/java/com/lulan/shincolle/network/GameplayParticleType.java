package com.lulan.shincolle.network;

public enum GameplayParticleType {
    NONE,
    LAUNCH_SMOKE,
    HIT_EXPLOSION,
    AIR_TRAIL,
    MISSILE_TRAIL,
    MISSILE_IMPACT,
    AIRCRAFT_LAUNCH,
    AIRCRAFT_TRAIL,
    AIRCRAFT_IMPACT,
    BOMB_DROP,
    BOMB_IMPACT,
    TORPEDO_WAKE,
    TORPEDO_IMPACT,
    FLARE_BURST,
    SEARCHLIGHT_MARK,
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

