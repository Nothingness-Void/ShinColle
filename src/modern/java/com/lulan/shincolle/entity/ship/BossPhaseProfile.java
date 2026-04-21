package com.lulan.shincolle.entity.ship;

import java.util.List;

public record BossPhaseProfile(List<BossActionType> actionCycle, int summonEggMeta, int baseActionCooldown, int baseSummonCooldown) {

    public static BossPhaseProfile forSpec(ShipEntitySpec spec) {
        return LegacyShipBehaviorCatalog.bossPhaseProfile(spec);
    }
}
