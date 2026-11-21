package com.lulan.shincolle.entity.ship.goal;

import com.lulan.shincolle.config.ShinColleConfig;
import com.lulan.shincolle.entity.ship.LegacyShipEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.Vec3;

public final class LegacyShipTeleportHelper {

    private LegacyShipTeleportHelper() {
    }

    public static boolean applyTeleport(LegacyShipEntity ship, double distanceSqr, Vec3 position) {
        if (!ShinColleConfig.canTeleport() || ship == null
                || !(ship.level() instanceof ServerLevel level)
                || !level.hasChunkAt(BlockPos.containing(position))) {
            return false;
        }
        if (distanceSqr > 1024.0D) {
            ship.stopRiding();
            ship.ejectPassengers();
        }
        ship.getShipNavigate().clearPathEntity();
        ship.teleportTo(position.x, position.y, position.z);
        return true;
    }
}
