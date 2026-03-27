package com.lulan.shincolle.entity.ship.goal;

import com.lulan.shincolle.entity.ship.LegacyShipEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.PathComputationType;

import javax.annotation.Nullable;
import java.util.EnumSet;

public class LegacyShipFollowOwnerGoal extends Goal {

    private final LegacyShipEntity ship;
    private final double speedModifier;
    private final float startDistance;
    private final float stopDistance;
    private final float teleportDistance;
    private @Nullable Player owner;
    private int timeToRecalcPath;

    public LegacyShipFollowOwnerGoal(LegacyShipEntity ship, double speedModifier, float startDistance,
                                     float stopDistance, float teleportDistance) {
        this.ship = ship;
        this.speedModifier = speedModifier;
        this.startDistance = startDistance;
        this.stopDistance = stopDistance;
        this.teleportDistance = teleportDistance;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        Player resolvedOwner = this.ship.getOwnerPlayer();

        if (resolvedOwner == null || this.ship.isHostileVariant() || this.ship.isOrderedToSit() || resolvedOwner.isSpectator()) {
            return false;
        }

        if (this.ship.distanceToSqr(resolvedOwner) < (double) (this.startDistance * this.startDistance)) {
            return false;
        }

        this.owner = resolvedOwner;
        return true;
    }

    @Override
    public boolean canContinueToUse() {
        if (this.owner == null || this.ship.isOrderedToSit() || this.ship.isHostileVariant()) {
            return false;
        }

        return this.owner.isAlive()
                && !this.ship.getNavigation().isDone()
                && this.ship.distanceToSqr(this.owner) > (double) (this.stopDistance * this.stopDistance);
    }

    @Override
    public void start() {
        this.timeToRecalcPath = 0;
    }

    @Override
    public void stop() {
        this.owner = null;
        this.ship.getNavigation().stop();
    }

    @Override
    public void tick() {
        if (this.owner == null) {
            return;
        }

        this.ship.getLookControl().setLookAt(this.owner, 10.0F, this.ship.getMaxHeadXRot());

        if (--this.timeToRecalcPath > 0) {
            return;
        }

        this.timeToRecalcPath = this.adjustedTickDelay(10);
        double distanceSqr = this.ship.distanceToSqr(this.owner);

        if (distanceSqr >= (double) (this.teleportDistance * this.teleportDistance) && this.tryTeleportNearOwner()) {
            return;
        }

        this.ship.getNavigation().moveTo(this.owner, this.speedModifier);
    }

    private boolean tryTeleportNearOwner() {
        if (this.owner == null) {
            return false;
        }

        BlockPos ownerPos = this.owner.blockPosition();

        for (int dx = -2; dx <= 2; dx++) {
            for (int dz = -2; dz <= 2; dz++) {
                if (Math.abs(dx) < 2 && Math.abs(dz) < 2) {
                    continue;
                }

                BlockPos targetPos = ownerPos.offset(dx, 0, dz);
                if (!this.canTeleportTo(targetPos)) {
                    continue;
                }

                this.ship.moveTo(targetPos.getX() + 0.5D, targetPos.getY(), targetPos.getZ() + 0.5D,
                        this.ship.getYRot(), this.ship.getXRot());
                this.ship.getNavigation().stop();
                return true;
            }
        }

        return false;
    }

    private boolean canTeleportTo(BlockPos pos) {
        BlockPos below = pos.below();
        BlockState floorState = this.ship.level().getBlockState(below);
        if (floorState.isAir() && !this.ship.level().getFluidState(below).is(FluidTags.WATER)) {
            return false;
        }

        if (!floorState.isPathfindable(this.ship.level(), below, PathComputationType.LAND)
                && !this.ship.level().getFluidState(below).is(FluidTags.WATER)) {
            return false;
        }

        return this.ship.level().noCollision(this.ship, this.ship.getBoundingBox().move(
                pos.getX() + 0.5D - this.ship.getX(),
                pos.getY() - this.ship.getY(),
                pos.getZ() + 0.5D - this.ship.getZ()));
    }
}
