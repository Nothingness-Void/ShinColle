package com.lulan.shincolle.entity.ship.path;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;

public final class LegacyShipObstaclePath {
    private @Nullable ShipPath path;
    private @Nullable BlockPos target;
    private int nextPathTick;

    public Vec3 resolve(Entity entity, Vec3 destination) {
        if (!entity.horizontalCollision && !entity.verticalCollision
                && !entity.level().getBlockCollisions(entity,
                entity.getBoundingBox().expandTowards(destination.subtract(entity.position()))).iterator().hasNext()) {
            this.path = null;
            this.target = null;
            return destination;
        }
        BlockPos target = BlockPos.containing(destination);
        if (this.path == null || !target.equals(this.target) || entity.tickCount >= this.nextPathTick) {
            this.path = new ShipPathFinder(entity.level(), true).findPath(
                    entity, target.getX(), target.getY(), target.getZ(), 70.0F);
            if (this.path != null && this.path.getCurrentPathLength() > 1) {
                this.path.incrementPathIndex();
            }
            this.target = target;
            this.nextPathTick = entity.tickCount + 16;
        }
        if (this.path != null) {
            while (!this.path.isFinished()
                    && entity.position().distanceToSqr(this.path.getPosition(entity)) < 0.25D) {
                this.path.incrementPathIndex();
            }
            if (!this.path.isFinished()) return this.path.getPosition(entity);
        }
        return destination;
    }
}
