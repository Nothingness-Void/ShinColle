package com.lulan.shincolle.entity.ship.goal;

import com.lulan.shincolle.entity.ship.LegacyShipEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.EnumSet;

public class LegacyShipWanderGoal extends Goal {

    private final LegacyShipEntity ship;
    private Vec3 position = Vec3.ZERO;

    public LegacyShipWanderGoal(LegacyShipEntity ship) {
        this.ship = ship;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK, Flag.JUMP));
    }

    @Override
    public boolean canUse() {
        if (this.ship.isPassenger() || this.ship.isOrderedToSit() || this.ship.getLegacyCraneState() > 0
                || this.ship.getRandom().nextInt(180) != 0 || this.ship.getFishingHook() != null) {
            return false;
        }
        Vec3 target = this.findPosition(this.ship.isHostileVariant() ? 12 : 10, this.ship.isHostileVariant() ? 1 : 5);
        if (target == null) {
            return false;
        }
        this.position = target;
        return true;
    }

    protected @Nullable Vec3 findPosition(int horizontalRange, int verticalRange) {
        return DefaultRandomPos.getPos(this.ship, horizontalRange, verticalRange);
    }

    @Override
    public boolean canContinueToUse() {
        return !this.ship.getNavigation().isDone();
    }

    @Override
    public void start() {
        this.ship.getNavigation().moveTo(this.position.x, this.position.y, this.position.z, 0.8D);
    }
}
