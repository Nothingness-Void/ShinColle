package com.lulan.shincolle.entity.ship.goal;

import com.lulan.shincolle.entity.ship.LegacyShipAttackKind;
import com.lulan.shincolle.entity.ship.LegacyShipBehaviorCatalog;
import com.lulan.shincolle.entity.ship.LegacyShipEntity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;

import javax.annotation.Nullable;
import java.util.EnumSet;

/** Keeps legacy guard movement separate from the local attack target. */
public final class LegacyShipGuardGoal extends Goal {

    private final LegacyShipEntity ship;
    @Nullable
    private LivingEntity target;
    @Nullable
    private LivingEntity warmupTarget;
    private int onSightTime;
    private int warmupTicks;
    private int lightDelay = 20;
    private int heavyDelay = 20;
    private int airDelay = 20;
    private int maxLightDelay = 20;
    private int maxHeavyDelay = 40;
    private int maxAirDelay = 40;
    private boolean launchType;

    public LegacyShipGuardGoal(LegacyShipEntity ship) {
        this.ship = ship;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        return this.ship.canExecuteGuardGoal();
    }

    @Override
    public boolean canContinueToUse() {
        return this.ship.canExecuteGuardGoal();
    }

    @Override
    public void start() {
        this.target = null;
        this.warmupTarget = null;
        this.onSightTime = 0;
        this.warmupTicks = 20;
    }

    @Override
    public void stop() {
        this.target = null;
        this.warmupTarget = null;
        this.onSightTime = 0;
        this.warmupTicks = 0;
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void tick() {
        if (!this.ship.canExecuteGuardGoal()) {
            return;
        }

        // EntityAIShipGuarding attacks only while its path-following state is moving.
        if (!this.ship.isAiAutoTarget() || this.ship.getNavigation().isDone()) {
            return;
        }

        if ((this.ship.tickCount & 63) == 0) {
            this.maxLightDelay = this.ship.getCompatAttackMaxCooldown(LegacyShipAttackKind.LIGHT);
            this.maxHeavyDelay = this.ship.getCompatAttackMaxCooldown(LegacyShipAttackKind.HEAVY);
            // EntityAIShipGuarding uses the shared carrier (type 3) delay for both air channels.
            this.maxAirDelay = this.ship.getCompatAttackMaxCooldown(LegacyShipAttackKind.AIR_LIGHT);
        }
        this.lightDelay--;
        this.heavyDelay--;
        this.airDelay--;

        if ((this.ship.tickCount & 31) == 0) {
            this.target = LegacyShipBehaviorCatalog.findLegacyGuardTarget(this.ship);
            if (this.target != this.warmupTarget) {
                this.warmupTarget = this.target;
                this.onSightTime = 0;
                this.warmupTicks = 20;
            }
        }

        if (this.target == null
                || !this.target.isAlive()
                || !this.ship.canEngage(this.target)
                || !this.ship.isTargetInCompatRange(this.target, LegacyShipAttackKind.LIGHT)) {
            this.onSightTime = 0;
            return;
        }

        if (this.warmupTicks > 0) {
            this.warmupTicks--;
        }

        boolean onSight = this.ship.getSensing().hasLineOfSight(this.target);
        if (!onSight) {
            this.onSightTime = 0;
            return;
        }

        this.onSightTime++;
        this.ship.getLookControl().setLookAt(this.target, 30.0F, 30.0F);
        int aimTime = Math.max(20, this.ship.getCompatAttackAimTime());
        if (this.warmupTicks > 0 || this.onSightTime < aimTime) {
            return;
        }

        // EntityAIShipGuarding owns independent light/heavy/air delays and fires cannons first.
        if (this.lightDelay <= 0 && this.ship.performGuardCompatAttack(
                this.target, LegacyShipAttackKind.LIGHT)) {
            this.lightDelay = this.maxLightDelay;
        }
        if (this.heavyDelay <= 0 && this.ship.performGuardCompatAttack(
                this.target, LegacyShipAttackKind.HEAVY)) {
            this.heavyDelay = this.maxHeavyDelay;
        }

        if (this.ship.useAttackAirLight() || this.ship.useAttackAirHeavy()) {
            if (!this.ship.useAttackAirLight()) {
                this.launchType = false;
            }
            if (!this.ship.useAttackAirHeavy()) {
                this.launchType = true;
            }

            LegacyShipAttackKind airKind = this.launchType
                    ? LegacyShipAttackKind.AIR_LIGHT : LegacyShipAttackKind.AIR_HEAVY;
            if (this.airDelay <= 0 && this.ship.performGuardCompatAttack(this.target, airKind)) {
                this.airDelay = this.maxAirDelay;
            }
            this.launchType = !this.launchType;
        }
    }
}
