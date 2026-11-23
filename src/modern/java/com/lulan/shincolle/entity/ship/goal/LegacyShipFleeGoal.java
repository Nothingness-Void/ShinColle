package com.lulan.shincolle.entity.ship.goal;

import com.lulan.shincolle.config.ShinColleConfig;
import com.lulan.shincolle.entity.ship.LegacyShipEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.EnumSet;
import java.util.UUID;

/**
 * Mirrors 1.12.2 EntityAIShipFlee: when HP falls below FleeHP%, clears target
 * and flees toward guarded entity or owner.
 */
public class LegacyShipFleeGoal extends Goal {

    private final LegacyShipEntity ship;
    private @Nullable LivingEntity escortTarget;
    private int findCooldown;

    public LegacyShipFleeGoal(LegacyShipEntity ship) {
        this.ship = ship;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK, Goal.Flag.JUMP));
    }

    @Override
    public boolean canUse() {
        if (this.ship.isHostileVariant() || this.ship.isOrderedToSit() || this.ship.isLeashed()) {
            return false;
        }

        int fleeHp = this.ship.getFleeHp();
        if (fleeHp <= 0 || this.ship.getMaxHealth() <= 0.0F || this.ship.getGrudge() <= 0) {
            return false;
        }

        if ((this.ship.getHealth() / this.ship.getMaxHealth()) > (fleeHp * 0.01F)) {
            return false;
        }

        LivingEntity resolved = this.resolveEscortTarget();
        if (resolved == null || !resolved.isAlive() || resolved.level() != this.ship.level()) {
            return false;
        }

        double distSq = this.ship.distanceToSqr(resolved);
        if (distSq > 6.0D && distSq < 3600.0D) {
            this.escortTarget = resolved;
            return true;
        }

        return false;
    }

    @Override
    public boolean canContinueToUse() {
        return this.canUse();
    }

    @Override
    public void start() {
        this.findCooldown = 0;
        this.ship.setTarget(null);
    }

    @Override
    public void stop() {
        this.escortTarget = null;
        this.ship.getShipNavigate().clearPathEntity();
    }

    @Override
    public void tick() {
        if (this.escortTarget == null) {
            return;
        }

        this.ship.getLookControl().setLookAt(this.escortTarget, 10.0F, this.ship.getMaxHeadXRot());

        if (--this.findCooldown > 0) {
            return;
        }

        this.findCooldown = 16;
        boolean canMove = this.ship.getShipNavigate().tryMoveToEntityLiving(this.escortTarget, 1.2D);
        if (!canMove && ShinColleConfig.canTeleport()) {
            double distSq = this.ship.distanceToSqr(this.escortTarget);
            if (distSq > 100.0D && this.ship.level() == this.escortTarget.level()) {
                LegacyShipTeleportHelper.applyTeleport(this.ship, distSq,
                        new Vec3(this.escortTarget.getX(), this.escortTarget.getY() + 0.5D, this.escortTarget.getZ()));
            }
        }
    }

    private @Nullable LivingEntity resolveEscortTarget() {
        UUID guardUuid = this.ship.getGuardEntityUuid();
        if (guardUuid != null && this.ship.level() instanceof ServerLevel serverLevel) {
            Entity guarded = serverLevel.getEntity(guardUuid);
            if (guarded instanceof LivingEntity livingGuarded) {
                return livingGuarded;
            }
        }
        return this.ship.getOwnerPlayer();
    }

}
