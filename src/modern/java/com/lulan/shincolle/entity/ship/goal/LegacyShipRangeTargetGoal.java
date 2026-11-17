package com.lulan.shincolle.entity.ship.goal;

import com.lulan.shincolle.entity.ship.LegacyShipBehaviorCatalog;
import com.lulan.shincolle.entity.ship.LegacyShipEntity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.target.TargetGoal;

import javax.annotation.Nullable;

/** Uses the legacy eight-tick range scan and target-category priority. */
public final class LegacyShipRangeTargetGoal extends TargetGoal {

    private final LegacyShipEntity ship;
    @Nullable
    private LivingEntity candidate;

    public LegacyShipRangeTargetGoal(LegacyShipEntity ship) {
        super(ship, false);
        this.ship = ship;
    }

    @Override
    public boolean canUse() {
        if (!this.canRunAutomaticTargeting() || this.ship.tickCount % 8 != 0) {
            return false;
        }

        this.candidate = LegacyShipBehaviorCatalog.findLegacyRangeTarget(this.ship);
        return this.candidate != null;
    }

    @Override
    public boolean canContinueToUse() {
        LivingEntity target = this.ship.getTarget();
        double range = this.ship.getHostileTargetSearchRange();
        return this.canRunAutomaticTargeting()
                && target != null
                && target.isAlive()
                && this.ship.distanceToSqr(target) <= range * range
                && LegacyShipBehaviorCatalog.isLegacyRangeTarget(this.ship, target);
    }

    private boolean canRunAutomaticTargeting() {
        return this.ship.canExecuteCombatGoal()
                && (this.ship.isHostileVariant() || this.ship.isAiAutoTarget());
    }

    @Override
    public void start() {
        this.ship.setTarget(this.candidate);
        super.start();
    }

    @Override
    public void stop() {
        if (this.ship.getTarget() == this.candidate) {
            this.ship.setTarget(null);
        }
        this.candidate = null;
        super.stop();
    }
}
