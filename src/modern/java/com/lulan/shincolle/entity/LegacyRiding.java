package com.lulan.shincolle.entity;

import com.lulan.shincolle.entity.ship.LegacyShipEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

public final class LegacyRiding {
    private LegacyRiding() {
    }

    public static double offset(Entity rider) {
        if (rider instanceof LegacyShipEntity ship) {
            return ship.getMyRidingOffset();
        }
        return rider instanceof Player ? -0.35D : 0.0D;
    }
}
