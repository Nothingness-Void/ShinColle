package com.lulan.shincolle.entity.ship.goal;

import com.lulan.shincolle.config.ShinColleConfig;
import com.lulan.shincolle.entity.ship.LegacyShipEntity;
import com.lulan.shincolle.formation.LegacyFormationHelper;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.EnumSet;

public class LegacyShipFollowOwnerGoal extends Goal {

    private final LegacyShipEntity ship;
    private @Nullable Player owner;
    private int findCooldown;
    private int checkTeleportTime;
    private int checkTeleportDistance;
    private double maxDistanceSqr;
    private double minDistanceSqr;
    private double distanceSqr = 1.0D;
    private Vec3 position;
    private Vec3 oldOwnerPosition;

    public LegacyShipFollowOwnerGoal(LegacyShipEntity ship) {
        this.ship = ship;
        this.position = ship.position();
        this.oldOwnerPosition = ship.position();
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK, Goal.Flag.JUMP));
    }

    @Override
    public boolean canUse() {
        if (this.canFollowOwner()) {
            Player resolvedOwner = this.ship.getOwnerPlayer();
            if (resolvedOwner != null) {
                this.owner = resolvedOwner;
                if (this.owner.level() != this.ship.level()) {
                    return false;
                }
                this.updateDistance();
                return this.distanceSqr > this.maxDistanceSqr;
            }
        }
        return false;
    }

    @Override
    public boolean canContinueToUse() {
        if (this.owner != null) {
            if (this.canFollowOwner()) {
                if (this.distanceSqr > this.minDistanceSqr) {
                    return true;
                }
                return !this.ship.getNavigation().isDone() || this.canUse();
            }
            this.stop();
        }
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
        this.owner = null;
        this.ship.getNavigation().stop();
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void tick() {
        this.findCooldown--;
        this.checkTeleportTime++;
        if (this.ship.tickCount % 32 == 0) {
            Player resolvedOwner = this.ship.getOwnerPlayer();
            if (resolvedOwner == null || resolvedOwner.level() != this.ship.level()) {
                this.stop();
                return;
            }
            this.owner = resolvedOwner;
            this.updateDistance();
        }
        if (this.owner == null) {
            return;
        }
        if (this.distanceSqr <= this.minDistanceSqr) {
            this.ship.getNavigation().stop();
        }
        if (this.findCooldown <= 0) {
            this.findCooldown = 32;
            this.ship.getNavigation().moveTo(this.position.x, this.position.y, this.position.z, 1.0D);
        }
        this.ship.getLookControl().setLookAt(this.owner, 20.0F, 40.0F);
        if (this.ship.level() == this.owner.level()) {
            if (!ShinColleConfig.canTeleport()) {
                return;
            }
            if (this.distanceSqr > ShinColleConfig.shipTeleport().get(1)) {
                this.checkTeleportDistance++;
                if (this.checkTeleportDistance > ShinColleConfig.shipTeleport().get(0)) {
                    this.checkTeleportDistance = 0;
                    LegacyShipTeleportHelper.applyTeleport(this.ship, this.distanceSqr,
                            this.owner.position().add(0.0D, 0.75D, 0.0D));
                    return;
                }
            }
            if (this.checkTeleportTime > ShinColleConfig.shipTeleport().get(0)) {
                this.checkTeleportTime = 0;
                LegacyShipTeleportHelper.applyTeleport(this.ship, this.distanceSqr,
                        this.owner.position().add(0.0D, 0.75D, 0.0D));
            }
        }
    }

    private void updateDistance() {
        if (this.ship.getLegacyFormationId() > 0) {
            this.minDistanceSqr = 4.0D;
            this.maxDistanceSqr = 7.0D;
            if (this.oldOwnerPosition.distanceToSqr(this.owner.position()) > 7.0D) {
                this.position = LegacyFormationHelper.getFormationGuardingPos(this.ship, this.owner,
                        this.oldOwnerPosition.x, this.oldOwnerPosition.z);
                this.oldOwnerPosition = this.owner.position();
                LegacyFormationHelper.sendFormationMarker(this.ship, this.position, 4);
            }
            if (this.ship.tickCount % 16 == 0) {
                LegacyFormationHelper.sendFormationMarker(this.ship, this.position, 6);
            }
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
            this.position = this.owner.position();
        }
        this.distanceSqr = this.position.distanceToSqr(this.ship.position());
    }

    private boolean canFollowOwner() {
        return !this.ship.isOrderedToSit()
                && !this.ship.isPassenger()
                && !this.ship.isLeashed()
                && this.ship.canFollow()
                && this.ship.getLegacyCraneState() < 1
                && this.ship.getGrudge() > 0;
    }
}
