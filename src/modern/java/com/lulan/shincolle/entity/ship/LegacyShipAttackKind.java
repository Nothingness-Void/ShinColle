package com.lulan.shincolle.entity.ship;

public enum LegacyShipAttackKind {

    MELEE(0, true, true, true, true, false),
    LIGHT(1, true, true, true, true, false),
    HEAVY(2, true, false, false, false, true),
    AIR_LIGHT(3, false, false, false, false, true),
    AIR_HEAVY(4, false, false, false, false, true);

    private final int delayType;
    private final boolean canMiss;
    private final boolean canCrit;
    private final boolean canDoubleHit;
    private final boolean canTripleHit;
    private final boolean justLaunch;

    LegacyShipAttackKind(int delayType, boolean canMiss, boolean canCrit, boolean canDoubleHit, boolean canTripleHit, boolean justLaunch) {
        this.delayType = delayType;
        this.canMiss = canMiss;
        this.canCrit = canCrit;
        this.canDoubleHit = canDoubleHit;
        this.canTripleHit = canTripleHit;
        this.justLaunch = justLaunch;
    }

    public int delayType() {
        return this.delayType;
    }

    public boolean canMiss() {
        return this.canMiss;
    }

    public boolean canCrit() {
        return this.canCrit;
    }

    public boolean canDoubleHit() {
        return this.canDoubleHit;
    }

    public boolean canTripleHit() {
        return this.canTripleHit;
    }

    public boolean justLaunch() {
        return this.justLaunch;
    }

    public static LegacyShipAttackKind byOrdinal(int ordinal) {
        LegacyShipAttackKind[] values = values();
        if (ordinal < 0 || ordinal >= values.length) {
            return MELEE;
        }
        return values[ordinal];
    }
}
