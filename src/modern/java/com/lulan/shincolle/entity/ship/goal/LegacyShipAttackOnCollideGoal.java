package com.lulan.shincolle.entity.ship.goal;

import com.lulan.shincolle.entity.ship.LegacyShipAttackKind;
import com.lulan.shincolle.entity.ship.LegacyShipEntity;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;

import javax.annotation.Nullable;
import java.util.EnumSet;

public final class LegacyShipAttackOnCollideGoal extends Goal {

    private final LegacyShipEntity ship;
    private final double speed;
    @Nullable
    private LivingEntity target;
    private int attackDelay = 20;
    private int maxAttackDelay = 20;

    public LegacyShipAttackOnCollideGoal(LegacyShipEntity ship, double speed) {
        this.ship = ship;
        this.speed = speed;
        this.setFlags(EnumSet.of(Flag.JUMP));
    }

    @Override
    public boolean canUse() {
        if (this.ship.isPassenger() || this.ship.isOrderedToSit()) {
            return false;
        }
        this.target = this.ship.getTarget();
        return this.target != null && this.target.isAlive();
    }

    @Override
    public boolean canContinueToUse() {
        return this.canUse();
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void tick() {
        if (this.target == null || !this.target.isAlive()) {
            return;
        }
        this.ship.getLookControl().setLookAt(this.target, 30.0F, 30.0F);
        double distance = this.ship.distanceToSqr(this.target.getX(), this.target.getBoundingBox().minY, this.target.getZ());
        double reach = this.ship.getBbWidth() * this.ship.getBbWidth() * 16.0F;
        if ((this.ship.tickCount & 31) == 0) {
            this.maxAttackDelay = this.ship.getCompatAttackMaxCooldown(LegacyShipAttackKind.MELEE);
            if (distance > reach) {
                this.ship.getNavigation().moveTo(this.target, this.speed);
            } else {
                this.ship.getNavigation().stop();
            }
        }
        if (distance <= reach && --this.attackDelay == 0) {
            this.attackDelay = this.maxAttackDelay;
            this.ship.swing(InteractionHand.MAIN_HAND);
            this.ship.performCollideAttack(this.target);
        }
    }
}
