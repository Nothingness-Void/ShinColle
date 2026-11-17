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
    TEXT_DODGE,
    BEAM_TRAIL,
    BEAM_IMPACT,
    BLACK_HOLE,
    HEALING_SPARKLE,
    LEGACY_EMOTION,
    /** Legacy hostile Kongou-family light-cannon type-5 smoke. */
    LEGACY_CANNON,
    /** Legacy numeric type-19 double large-smoke cannon. */
    LEGACY_DOUBLE_CANNON,
    /** Legacy Northern Hime numeric type-31 snow spray. */
    LEGACY_NORTHERN_CANNON,
    /** Legacy Northern Hime numeric type-30 snow hit. */
    LEGACY_NORTHERN_HIT,
    /** Legacy Battleship Re numeric type-6 adjustable laser. */
    LEGACY_RE_LASER,
    /** Legacy AbyssMissile type-2 railgun head particle. */
    LEGACY_RAILGUN;

    public static GameplayParticleType fromOrdinal(int ordinal) {
        GameplayParticleType[] values = values();
        if (ordinal < 0 || ordinal >= values.length) {
            return HIT_EXPLOSION;
        }
        return values[ordinal];
    }
}

