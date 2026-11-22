package com.lulan.shincolle.entity;

import com.lulan.shincolle.entity.ship.LegacyShipEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.phys.Vec3;

public final class LegacyRiding {
    private LegacyRiding() {
    }

    public static double offset(Entity rider) {
        if (rider instanceof LegacyShipEntity ship) {
            return ship.getMyRidingOffset();
        }
        return rider instanceof Player ? -0.35D : 0.0D;
    }

    public static Vec3 vehicleAttachment(LegacyShipEntity rider, Entity vehicle) {
        // 1.21 adds a cart anchor that legacy getMountedYOffset() did not have.
        double cartAnchor = vehicle instanceof AbstractMinecart ? 0.1875D : 0.0D;
        return new Vec3(0.0D, cartAnchor - rider.getMyRidingOffset(), 0.0D);
    }
}
