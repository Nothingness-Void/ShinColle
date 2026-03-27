package com.lulan.shincolle.entity.ship.goal;

import com.lulan.shincolle.entity.ship.LegacyShipEntity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;

import java.util.EnumSet;

public class LegacyShipRangedAttackGoal extends Goal {

    private final LegacyShipEntity ship;
    private final double speedModifier;
    private int onSightTime;
    private int timeToRecalcPath;

    public LegacyShipRangedAttackGoal(LegacyShipEntity ship, double speedModifier) {
        this.ship = ship;
        this.speedModifier = speedModifier;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        LivingEntity target = this.ship.getTarget();
        return this.ship.canExecuteCombatGoal()
                && this.ship.canUseCompatRangedCombat()
                && target != null
                && this.ship.canEngage(target);
    }

    @Override
    public boolean canContinueToUse() {
        LivingEntity target = this.ship.getTarget();
        return this.ship.canExecuteCombatGoal()
                && this.ship.canUseCompatRangedCombat()
                && target != null
                && target.isAlive()
                && this.ship.canEngage(target);
    }

    @Override
    public void start() {
        this.onSightTime = 0;
        this.timeToRecalcPath = 0;
    }

    @Override
    public void stop() {
        this.onSightTime = 0;
        this.ship.getNavigation().stop();
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void tick() {
        LivingEntity target = this.ship.getTarget();
        if (target == null) {
            return;
        }

        double range = this.ship.getCompatAttackRange();
        double rangeSqr = range * range;
        double distanceSqr = this.ship.distanceToSqr(target);
        boolean onSight = this.ship.getSensing().hasLineOfSight(target);

        this.onSightTime = onSight ? this.onSightTime + 1 : 0;
        this.ship.getLookControl().setLookAt(target, 30.0F, 30.0F);

        if (--this.timeToRecalcPath <= 0) {
            this.timeToRecalcPath = this.adjustedTickDelay(16);

            if (distanceSqr <= rangeSqr && onSight) {
                this.ship.getNavigation().stop();
            } else {
                this.ship.getNavigation().moveTo(target, this.speedModifier);
            }
        }

        if (!onSight || distanceSqr > rangeSqr) {
            return;
        }

        if (this.onSightTime >= this.ship.getCompatAttackAimTime()) {
            this.ship.tryCompatCannonAttack(target);
        }

        this.ship.tryCompatAirAttack(target);
    }
}
