package com.lulan.shincolle.network;

import com.lulan.shincolle.entity.projectile.LegacyShipProjectileVisual;
import com.lulan.shincolle.entity.ship.LegacyShipAttackKind;
import com.lulan.shincolle.entity.ship.LegacyShipEntity;
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
        sendParticle(reference, particleType, x, y, z, velocityX, velocityY, velocityZ, 0);
    }

    /** Sends a legacy particle family with its old integer subtype preserved. */
    public static void sendParticle(Entity reference, GameplayParticleType particleType,
                                    double x, double y, double z,
                                    double velocityX, double velocityY, double velocityZ,
                                    int variant) {
        if (!(reference.level() instanceof ServerLevel)) {
            return;
        }

        ClientboundSpawnParticlePacket packet = new ClientboundSpawnParticlePacket(
                particleType,
                x, y, z,
                velocityX, velocityY, velocityZ,
                variant, reference.getId());
        ModNetwork.sendToTrackingAndSelf(reference, packet);
    }

    /** Mirrors BasicEntityShip.applyParticleEmotion through the modern particle bus. */
    public static void sendLegacyEmotion(Entity reference, int legacyEmotionType) {
        float height = reference.getBbHeight() * (reference instanceof LegacyShipEntity ship
                && ship.isOrderedToSit() ? 0.4F : 0.45F);
        sendParticle(reference, GameplayParticleType.LEGACY_EMOTION,
                reference.getX(), reference.getY(), reference.getZ(),
                height, 0.0D, 0.0D, legacyEmotionType);
    }

    public static void sendLegacyAttackAnimation(Entity reference, int legacyParticleType) {
        sendParticle(reference, GameplayParticleType.LEGACY_SHIP_ANIMATION,
                reference.getX(), reference.getY(), reference.getZ(), 0D, 0D, 0D, legacyParticleType);
    }
}
