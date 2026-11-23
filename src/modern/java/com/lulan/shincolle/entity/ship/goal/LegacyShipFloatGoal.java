package com.lulan.shincolle.entity.ship.goal;

import com.lulan.shincolle.entity.ship.LegacyShipEntity;
import net.minecraft.world.entity.ai.goal.Goal;

import java.util.EnumSet;

public final class LegacyShipFloatGoal extends Goal {
    private final LegacyShipEntity ship;

    public LegacyShipFloatGoal(LegacyShipEntity ship) {
        this.ship = ship;
        this.setFlags(EnumSet.of(Goal.Flag.TARGET));
    }

    @Override
    public boolean canUse() {
        return this.ship.canUseLegacyFloatingGoal();
    }

    @Override
    public boolean canContinueToUse() {
        return this.ship.canUseLegacyFloatingGoal();
    }

    @Override
    public void tick() {
        this.ship.applyLegacyFloatingMotion();
    }
}
