package com.lulan.shincolle.entity.ship.path;

import com.lulan.shincolle.registry.ModBlocks;
import com.lulan.shincolle.entity.ship.LegacyShipEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.Vec3;

public interface ShipNavigationHost {
    ShipPathNavigate getShipNavigate();

    ShipMoveHelper getShipMoveHelper();

    boolean canFly();

    boolean isShipJumping();

    default float getShipTravelSpeed() {
        Mob mob = (Mob) this;
        return mob instanceof LegacyShipEntity ship ? ship.getLegacyStats().moveSpeed()
                : (float) mob.getAttributeValue(Attributes.MOVEMENT_SPEED);
    }

    static boolean isLiquid(BlockState state) {
        return !state.getFluidState().isEmpty();
    }

    static boolean isInLiquid(Mob entity) {
        return isLiquid(entity.level().getBlockState(BlockPos.containing(
                entity.getX(), (int) entity.getBoundingBox().minY, entity.getZ())));
    }

    static boolean isFree(Mob entity) {
        BlockPos pos = BlockPos.containing(entity.getX(), (int) (entity.getBoundingBox().minY + 0.5D), entity.getZ());
        BlockState state = entity.level().getBlockState(pos);
        return state.isAir() || isLiquid(state) || state.is(ModBlocks.BLOCK_WAYPOINT.get())
                || state.isPathfindable(entity.level(), pos, PathComputationType.LAND);
    }

    static void update(Mob entity, boolean sitting) {
        ShipNavigationHost host = (ShipNavigationHost) entity;
        if (!host.getShipNavigate().noPath()) {
            if (!entity.getNavigation().isDone()) {
                entity.getNavigation().stop();
            }
            if (sitting || entity.isLeashed()) {
                host.getShipNavigate().clearPathEntity();
            }
            host.getShipNavigate().onUpdateNavigation();
            host.getShipMoveHelper().onUpdateMoveHelper();
            entity.travel(new Vec3(entity.xxa, 0.0D, entity.zza));
        }
        if (!entity.getNavigation().isDone() && isInLiquid(entity)) {
            entity.getNavigation().stop();
        }
    }

    static void travel(Mob entity, Vec3 input) {
        ShipNavigationHost host = (ShipNavigationHost) entity;
        float moveSpeed = host.getShipTravelSpeed();
        double oldY = entity.getY();
        if (!host.canFly() && !isInLiquid(entity)) {
            BlockPos floor = BlockPos.containing(entity.getX(), entity.getBoundingBox().minY - 1.0D, entity.getZ());
            float friction = entity.onGround() ? entity.level().getBlockState(floor).getFriction(entity.level(), floor, entity) * 0.91F : 0.91F;
            float acceleration = entity.onGround() ? moveSpeed * (0.16277136F / (friction * friction * friction)) : 0.02F;
            if (host.isShipJumping()) {
                entity.setDeltaMovement(entity.getDeltaMovement().add(0, moveSpeed * 0.1D, 0));
            }
            entity.moveRelative(acceleration, new Vec3(input.x, 0.0D, input.z));
            floor = BlockPos.containing(entity.getX(), entity.getBoundingBox().minY - 1.0D, entity.getZ());
            friction = entity.onGround() ? entity.level().getBlockState(floor).getFriction(entity.level(), floor, entity) * 0.91F : 0.91F;
            if (entity.onClimbable()) {
                Vec3 delta = entity.getDeltaMovement();
                entity.setDeltaMovement(net.minecraft.util.Mth.clamp(delta.x, -0.15D, 0.15D), Math.max(delta.y, -0.15D),
                        net.minecraft.util.Mth.clamp(delta.z, -0.15D, 0.15D));
                entity.fallDistance = 0;
            }
            entity.move(MoverType.SELF, entity.getDeltaMovement());
            Vec3 delta = entity.getDeltaMovement();
            double dy = entity.horizontalCollision && entity.onClimbable() ? 0.4D : delta.y;
            if (entity.hasEffect(net.minecraft.world.effect.MobEffects.LEVITATION)) {
                dy += (0.05D * (entity.getEffect(net.minecraft.world.effect.MobEffects.LEVITATION).getAmplifier() + 1) - dy) * 0.2D;
            } else if (!entity.isNoGravity()) {
                dy -= 0.08D;
            }
            entity.setDeltaMovement(delta.x * friction, dy * 0.98D, delta.z * friction);
            entity.calculateEntityAnimation(false);
            return;
        }
        entity.moveRelative(moveSpeed * 0.4F, new Vec3(input.x, 0.0D, input.z));
        entity.move(MoverType.SELF, entity.getDeltaMovement());
        double drag = ((ShipNavigationHost) entity).canFly() ? 0.91D : 0.8D;
        entity.setDeltaMovement(entity.getDeltaMovement().scale(drag));
        Vec3 motion = entity.getDeltaMovement();
        if (entity.horizontalCollision && entity.isFree(motion.x, motion.y + 0.6D - entity.getY() + oldY, motion.z)) {
            entity.setDeltaMovement(motion.x, 0.3D, motion.z);
        }
        entity.calculateEntityAnimation(false);
    }
}
