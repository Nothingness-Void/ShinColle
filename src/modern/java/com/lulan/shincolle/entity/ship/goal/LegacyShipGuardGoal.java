package com.lulan.shincolle.entity.ship.goal;

import com.lulan.shincolle.config.ShinColleConfig;
import com.lulan.shincolle.entity.ship.LegacyShipAttackKind;
import com.lulan.shincolle.entity.ship.LegacyShipBehaviorCatalog;
import com.lulan.shincolle.entity.ship.LegacyShipEntity;
import com.lulan.shincolle.formation.LegacyFormationHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.EnumSet;

public final class LegacyShipGuardGoal extends Goal {

    private final LegacyShipEntity ship;
    @Nullable
    private LivingEntity target;
    private int onSightTime;
    private int aimTime = 20;
    private double attackRange = 1.0D;
    private double attackRangeSqr = 1.0D;
    private int findCooldown;
    private int checkTeleportTime;
    private int checkTeleportDistance;
    private double distanceSqr = 1.0D;
    private double minDistanceSqr;
    private double maxDistanceSqr;
    private Vec3 position = new Vec3(-1.0D, -1.0D, -1.0D);
    private Vec3 oldGuardPosition = new Vec3(-1.0D, -100.0D, -1.0D);
    private boolean moving;
    private int lightDelay = 20;
    private int heavyDelay = 20;
    private int airDelay = 20;
    private int maxLightDelay = 20;
    private int maxHeavyDelay = 40;
    private int maxAirDelay = 40;
    private boolean launchType;

    public LegacyShipGuardGoal(LegacyShipEntity ship) {
        this.ship = ship;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK, Goal.Flag.JUMP));
    }

    @Override
    public boolean canUse() {
        return this.ship.canExecuteGuardGoal() && this.checkGuardTarget();
    }

    @Override
    public boolean canContinueToUse() {
        if (this.ship.canExecuteGuardGoal()) {
            if (this.distanceSqr > this.minDistanceSqr) {
                return true;
            }
            return !this.ship.getNavigation().isDone() || this.canUse();
        }
        this.stop();
        return false;
    }

    @Override
    public void start() {
        this.findCooldown = 10;
        this.checkTeleportTime = 0;
        this.checkTeleportDistance = 0;
    }

    @Override
    public void stop() {
        this.moving = false;
        this.findCooldown = 10;
        this.ship.getNavigation().stop();
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void tick() {
        if (this.moving && this.ship.isAiAutoTarget() && this.ship.getLegacyGuardType() > 0) {
            this.tickAttack();
        }

        this.findCooldown--;
        Vec3 velocity = this.ship.getDeltaMovement();
        if (velocity.x * velocity.x < 0.0003D && velocity.z * velocity.z < 0.0003D) {
            this.checkTeleportTime++;
        }
        if (this.ship.tickCount % 8 == 0 && !this.checkGuardTarget()) {
            return;
        }
        if (this.distanceSqr <= this.minDistanceSqr) {
            this.moving = false;
            this.ship.getNavigation().stop();
        }
        if (this.findCooldown <= 0) {
            this.findCooldown = 32;
            this.moving = this.ship.getNavigation().moveTo(this.position.x, this.position.y, this.position.z, 1.0D);
        }
        this.ship.getLookControl().setLookAt(this.position.x, this.position.y, this.position.z,
                30.0F, this.ship.getMaxHeadXRot());
        if (!this.guardDimensionMatches()) {
            this.cancelGuard();
            return;
        }
        if (!ShinColleConfig.canTeleport()) {
            return;
        }
        if (this.distanceSqr > ShinColleConfig.shipTeleport().get(1)) {
            this.checkTeleportDistance++;
            if (this.checkTeleportDistance > ShinColleConfig.shipTeleport().get(0)) {
                this.checkTeleportDistance = 0;
                LegacyShipTeleportHelper.applyTeleport(this.ship, this.distanceSqr, this.position.add(0.0D, 0.75D, 0.0D));
                return;
            }
        }
        if (this.checkTeleportTime > ShinColleConfig.shipTeleport().get(0)) {
            this.checkTeleportTime = 0;
            LegacyShipTeleportHelper.applyTeleport(this.ship, this.distanceSqr, this.position.add(0.0D, 0.75D, 0.0D));
        }
    }

    private boolean checkGuardTarget() {
        if (!(this.ship.level() instanceof ServerLevel level)) {
            return false;
        }
        if (this.ship.getGuardEntityUuid() != null) {
            Entity guarded = level.getEntity(this.ship.getGuardEntityUuid());
            if (guarded == null || !guarded.isAlive() || guarded.level() != level) {
                this.cancelGuard();
                return false;
            }
            if (this.ship.getLegacyFormationId() > 0) {
                if (this.oldGuardPosition.distanceToSqr(guarded.position()) > 6.0D) {
                    this.position = LegacyFormationHelper.getFormationGuardingPos(this.ship, guarded,
                            this.oldGuardPosition.x, this.oldGuardPosition.z);
                    this.oldGuardPosition = guarded.position();
                    LegacyFormationHelper.sendFormationMarker(this.ship, this.position, 4);
                }
                if (this.ship.tickCount % 16 == 0) {
                    LegacyFormationHelper.sendFormationMarker(this.ship, this.position, 6);
                }
            } else {
                this.position = guarded.position();
            }
        } else {
            BlockPos guarded = this.ship.getGuardedBlockPos();
            if (guarded == null) {
                guarded = this.ship.getCommandedPos();
            }
            if (guarded == null) {
                this.cancelGuard();
                return false;
            }
            this.position = Vec3.atCenterOf(guarded);
        }
        if (this.position.y <= 0.0D || !this.guardDimensionMatches()) {
            this.cancelGuard();
            return false;
        }
        if (this.ship.getLegacyFormationId() > 0) {
            this.minDistanceSqr = this.ship.getLegacyGuardType() == 2 ? 5.0D : 4.0D;
            this.maxDistanceSqr = this.ship.getLegacyGuardType() == 2 ? 9.0D : 7.0D;
            if (this.ship.isAiPickItem()) {
                this.maxDistanceSqr = 64.0D;
            }
        } else {
            float min = this.ship.getFollowMin() + this.ship.getBbWidth() * 0.75F;
            float max = this.ship.getFollowMax() + this.ship.getBbWidth() * 0.75F;
            if (this.ship.isAiPickItem()) {
                max += 5.0D;
            }
            this.minDistanceSqr = min * min;
            this.maxDistanceSqr = max * max;
        }
        this.distanceSqr = this.position.distanceToSqr(this.ship.position());
        return this.distanceSqr > this.maxDistanceSqr;
    }

    private boolean guardDimensionMatches() {
        String dimension = this.ship.getGuardedBlockPos() == null
                ? this.ship.getCommandDimension() : this.ship.getGuardedBlockDimension();
        return dimension.isBlank() || dimension.equals(this.ship.level().dimension().location().toString());
    }

    private void cancelGuard() {
        this.ship.clearCommandState();
        this.ship.setTarget(null);
        this.stop();
    }

    private void tickAttack() {
        if ((this.ship.tickCount & 63) == 0) {
            this.attackRange = Math.max(1, (int) this.ship.getCompatAttackRange());
            this.attackRangeSqr = this.attackRange * this.attackRange;
            this.aimTime = this.ship.getCompatAttackAimTime();
            this.maxLightDelay = this.ship.getCompatAttackMaxCooldown(LegacyShipAttackKind.LIGHT);
            this.maxHeavyDelay = this.ship.getCompatAttackMaxCooldown(LegacyShipAttackKind.HEAVY);
            // EntityAIShipGuarding uses the shared carrier (type 3) delay for both air channels.
            this.maxAirDelay = this.ship.getCompatAttackMaxCooldown(LegacyShipAttackKind.AIR_LIGHT);
        }
        this.lightDelay--;
        this.heavyDelay--;
        this.airDelay--;

        if ((this.ship.tickCount & 31) == 0) {
            LivingEntity found = LegacyShipBehaviorCatalog.findLegacyGuardTarget(this.ship, this.attackRange);
            if (found != null) {
                this.target = found;
            }
            if (this.target != null && !this.target.isAlive()) {
                this.target = null;
            }
        }

        if (this.target == null) {
            this.onSightTime = 0;
            return;
        }

        boolean onSight = this.ship.getSensing().hasLineOfSight(this.target);
        if (!onSight) {
            this.onSightTime = 0;
            return;
        }

        this.onSightTime++;
        if (this.ship.distanceToSqr(this.target) > this.attackRangeSqr || this.onSightTime < this.aimTime) {
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
