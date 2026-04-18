package com.lulan.shincolle.network;

import com.lulan.shincolle.entity.projectile.LegacyShipProjectileVisual;
import com.lulan.shincolle.entity.ship.LegacyShipAttackKind;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

public final class CombatFxDispatcher {

    private CombatFxDispatcher() {
    }

    public static void sendCombatReact(LivingEntity attacker, LivingEntity target,
                                       CombatReactType reactType, LegacyShipAttackKind attackKind) {
        sendCombatReact(attacker, target, reactType, attackKind, LegacyShipProjectileVisual.NONE);
    }

    public static void sendCombatReact(LivingEntity attacker, LivingEntity target, CombatReactType reactType,
                                       LegacyShipAttackKind attackKind, LegacyShipProjectileVisual projectileVisual) {
        if (!(attacker.level() instanceof ServerLevel)) {
            return;
        }

        ClientboundCombatReactPacket packet = new ClientboundCombatReactPacket(
                reactType,
                attacker.getId(),
                target.getId(),
                attackKind,
                projectileVisual);
        ModNetwork.sendToTrackingAndSelf(attacker, packet);
        if (target != attacker) {
            ModNetwork.sendToTrackingAndSelf(target, packet);
        }
    }

    public static void sendParticle(Entity reference, GameplayParticleType particleType,
                                    double x, double y, double z) {
        sendParticle(reference, particleType, x, y, z, 0.0D, 0.0D, 0.0D);
    }

    public static void sendParticle(Entity reference, GameplayParticleType particleType,
                                    double x, double y, double z,
                                    double velocityX, double velocityY, double velocityZ) {
        if (!(reference.level() instanceof ServerLevel)) {
            return;
        }

        ClientboundSpawnParticlePacket packet = new ClientboundSpawnParticlePacket(
                particleType,
                x, y, z,
                velocityX, velocityY, velocityZ);
        ModNetwork.sendToTrackingAndSelf(reference, packet);
    }
}

