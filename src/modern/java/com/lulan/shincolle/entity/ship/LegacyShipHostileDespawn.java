package com.lulan.shincolle.entity.ship;

import com.lulan.shincolle.config.ShinColleConfig;
import net.minecraft.world.entity.player.Player;

public final class LegacyShipHostileDespawn {
    private LegacyShipHostileDespawn() {}

    public static boolean tick(LegacyShipEntity ship, HostileRuntimeState state) {
        if (!ship.isAlive() || ship.isNoAi()) return false;
        if (state.isPersistenceRequired()) {
            state.setDespawnTicks(0);
            return false;
        }
        int idle = state.getDespawnTicks() + 1;
        state.setDespawnTicks(idle);
        Player closest = ship.level().getNearestPlayer(ship, -1.0D);
        if (closest == null) return false;
        double distance = ship.distanceToSqr(closest);
        int limit = ship.getLegacyHostileScaleLevel() > 1
                ? ShinColleConfig.despawnBoss() : ShinColleConfig.despawnMinion();
        boolean canDespawn = limit > -1 && ship.tickCount > limit;
        if (canDespawn && distance > 16384.0D) {
            ship.discard();
            return true;
        }
        if (idle > 600 && ship.getRandom().nextInt(800) == 0 && distance > 1024.0D && canDespawn) {
            ship.discard();
            return true;
        } else if (distance < 1024.0D) {
            state.setDespawnTicks(0);
        }
        return false;
    }
}
