package com.lulan.shincolle.entity.ship;

import java.util.List;

public record BossPhaseProfile(List<BossActionType> actionCycle, int summonEggMeta, int baseActionCooldown, int baseSummonCooldown) {

    public static BossPhaseProfile forSpec(ShipEntitySpec spec) {
        int legacyClassId = spec.legacyClassId();
        if (spec.archetype() == ShipArchetype.INSTALLATION) {
            int summonEggMeta = legacyClassId == 21 ? 14 : 18;
            return new BossPhaseProfile(
                    List.of(BossActionType.AREA_BOMBARD, BossActionType.SUMMON_ESCORT, BossActionType.CANNON_BURST),
                    summonEggMeta,
                    40,
                    160);
        }

        if (legacyClassId == 30 || legacyClassId == 33 || legacyClassId == 49) {
            return new BossPhaseProfile(
                    List.of(BossActionType.AIR_ASSAULT, BossActionType.SUMMON_ESCORT, BossActionType.CANNON_BURST),
                    14,
                    34,
                    140);
        }

        if (spec.archetype() == ShipArchetype.PRINCESS) {
            int summonEggMeta = spec.archetype() == ShipArchetype.PRINCESS && legacyClassId == 44 ? 19 : 2;
            return new BossPhaseProfile(
                    List.of(BossActionType.CANNON_BURST, BossActionType.CHARGE, BossActionType.SUMMON_ESCORT),
                    summonEggMeta,
                    36,
                    180);
        }

        return new BossPhaseProfile(
                List.of(BossActionType.CANNON_BURST, BossActionType.CHARGE),
                2,
                54,
                220);
    }
}
