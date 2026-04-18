package com.lulan.shincolle.client;

import com.lulan.shincolle.entity.projectile.LegacyShipProjectileVisual;
import com.lulan.shincolle.network.ClientboundCombatReactPacket;
import com.lulan.shincolle.network.ClientboundSpawnParticlePacket;
import com.lulan.shincolle.network.CombatReactType;
import com.lulan.shincolle.network.GameplayParticleType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;

public final class GameplayClientEffects {

    private GameplayClientEffects() {
    }

    public static void handleCombatReact(ClientboundCombatReactPacket packet) {
        Minecraft minecraft = Minecraft.getInstance();
        ClientLevel level = minecraft.level;
        if (level == null) {
            return;
        }

        Entity anchor = packet.reactType() == CombatReactType.LAUNCH
                ? level.getEntity(packet.attackerId())
                : level.getEntity(packet.targetId());
        if (anchor != null) {
            spawnCombatReact(level, anchor, packet);
        }

        if (minecraft.player != null) {
            minecraft.player.displayClientMessage(toMessage(packet.reactType()), true);
        }
    }

    public static void handleParticle(ClientboundSpawnParticlePacket packet) {
        Minecraft minecraft = Minecraft.getInstance();
        ClientLevel level = minecraft.level;
        if (level == null) {
            return;
        }

        switch (packet.particleType()) {
            case NONE -> {
            }
            case LAUNCH_SMOKE -> spawnBurst(level, ParticleTypes.SMOKE, packet.x(), packet.y(), packet.z(), 8, 0.2D);
            case HIT_EXPLOSION -> spawnBurst(level, ParticleTypes.EXPLOSION, packet.x(), packet.y(), packet.z(), 4, 0.12D);
            case AIR_TRAIL -> spawnBurst(level, ParticleTypes.CLOUD, packet.x(), packet.y(), packet.z(), 3, 0.06D);
            case MISSILE_TRAIL -> {
                spawnDirectedBurst(level, ParticleTypes.SMOKE, packet.x(), packet.y(), packet.z(),
                        packet.velocityX(), packet.velocityY(), packet.velocityZ(), 4, 0.04D, 0.15D);
                spawnDirectedBurst(level, ParticleTypes.FLAME, packet.x(), packet.y(), packet.z(),
                        packet.velocityX(), packet.velocityY(), packet.velocityZ(), 2, 0.03D, 0.08D);
            }
            case MISSILE_IMPACT -> {
                spawnBurst(level, ParticleTypes.EXPLOSION, packet.x(), packet.y(), packet.z(), 4, 0.1D);
                spawnBurst(level, ParticleTypes.FLAME, packet.x(), packet.y(), packet.z(), 8, 0.2D);
            }
            case AIRCRAFT_LAUNCH -> {
                spawnBurst(level, ParticleTypes.CLOUD, packet.x(), packet.y(), packet.z(), 7, 0.18D);
                spawnBurst(level, ParticleTypes.POOF, packet.x(), packet.y(), packet.z(), 4, 0.12D);
            }
            case AIRCRAFT_TRAIL -> spawnDirectedBurst(level, ParticleTypes.CLOUD, packet.x(), packet.y(), packet.z(),
                    packet.velocityX(), packet.velocityY(), packet.velocityZ(), 3, 0.05D, 0.08D);
            case AIRCRAFT_IMPACT -> {
                spawnBurst(level, ParticleTypes.EXPLOSION, packet.x(), packet.y(), packet.z(), 3, 0.08D);
                spawnBurst(level, ParticleTypes.CRIT, packet.x(), packet.y(), packet.z(), 8, 0.2D);
            }
            case BOMB_DROP -> spawnDirectedBurst(level, ParticleTypes.SMOKE, packet.x(), packet.y(), packet.z(),
                    packet.velocityX(), packet.velocityY(), packet.velocityZ(), 4, 0.05D, 0.05D);
            case BOMB_IMPACT -> {
                spawnBurst(level, ParticleTypes.EXPLOSION, packet.x(), packet.y(), packet.z(), 5, 0.1D);
                spawnBurst(level, ParticleTypes.SMOKE, packet.x(), packet.y(), packet.z(), 8, 0.16D);
            }
            case TORPEDO_WAKE -> {
                spawnDirectedBurst(level, ParticleTypes.BUBBLE, packet.x(), packet.y(), packet.z(),
                        packet.velocityX(), packet.velocityY(), packet.velocityZ(), 4, 0.04D, 0.08D);
                spawnBurst(level, ParticleTypes.SPLASH, packet.x(), packet.y(), packet.z(), 4, 0.12D);
            }
            case TORPEDO_IMPACT -> {
                spawnBurst(level, ParticleTypes.SPLASH, packet.x(), packet.y(), packet.z(), 8, 0.18D);
                spawnBurst(level, ParticleTypes.EXPLOSION, packet.x(), packet.y(), packet.z(), 3, 0.08D);
            }
            case FLARE_BURST -> {
                spawnBurst(level, ParticleTypes.END_ROD, packet.x(), packet.y(), packet.z(), 8, 0.2D);
                spawnBurst(level, ParticleTypes.FLAME, packet.x(), packet.y(), packet.z(), 6, 0.16D);
            }
            case SEARCHLIGHT_MARK -> {
                spawnBurst(level, ParticleTypes.ELECTRIC_SPARK, packet.x(), packet.y(), packet.z(), 7, 0.18D);
                spawnBurst(level, ParticleTypes.END_ROD, packet.x(), packet.y(), packet.z(), 3, 0.08D);
            }
            case TEXT_MISS -> spawnBurst(level, ParticleTypes.SMOKE, packet.x(), packet.y(), packet.z(), 4, 0.09D);
            case TEXT_CRIT -> spawnBurst(level, ParticleTypes.CRIT, packet.x(), packet.y(), packet.z(), 5, 0.1D);
            case TEXT_DOUBLE -> spawnBurst(level, ParticleTypes.SWEEP_ATTACK, packet.x(), packet.y(), packet.z(), 4, 0.08D);
            case TEXT_TRIPLE -> spawnBurst(level, ParticleTypes.SWEEP_ATTACK, packet.x(), packet.y(), packet.z(), 6, 0.12D);
            case TEXT_DODGE -> spawnBurst(level, ParticleTypes.CLOUD, packet.x(), packet.y(), packet.z(), 4, 0.08D);
        }
    }

    private static void spawnCombatReact(ClientLevel level, Entity anchor, ClientboundCombatReactPacket packet) {
        double x = anchor.getX();
        double y = anchor.getY() + anchor.getBbHeight() * 0.75D;
        double z = anchor.getZ();

        switch (packet.reactType()) {
            case MISS -> {
                spawnBurst(level, ParticleTypes.SMOKE, x, y, z, 6, 0.14D);
                if (packet.projectileVisual() == LegacyShipProjectileVisual.MISSILE
                        || packet.projectileVisual() == LegacyShipProjectileVisual.BOMB) {
                    spawnBurst(level, ParticleTypes.POOF, x, y, z, 3, 0.1D);
                }
            }
            case CRIT -> spawnBurst(level, ParticleTypes.CRIT, x, y, z, 10, 0.21D);
            case DOUBLE_HIT -> spawnBurst(level, ParticleTypes.SWEEP_ATTACK, x, y, z, 8, 0.16D);
            case TRIPLE_HIT -> spawnBurst(level, ParticleTypes.SWEEP_ATTACK, x, y, z, 12, 0.21D);
            case DODGE -> spawnBurst(level, ParticleTypes.CLOUD, x, y, z, 8, 0.18D);
            case LAUNCH -> spawnLaunchReact(level, x, y, z, packet.projectileVisual());
            case HIT -> spawnImpactReact(level, x, y, z, packet.projectileVisual());
        }
    }

    private static void spawnLaunchReact(ClientLevel level, double x, double y, double z,
                                         LegacyShipProjectileVisual projectileVisual) {
        switch (projectileVisual) {
            case MISSILE -> {
                spawnBurst(level, ParticleTypes.SMOKE, x, y, z, 9, 0.2D);
                spawnBurst(level, ParticleTypes.FLAME, x, y, z, 5, 0.16D);
            }
            case AIRPLANE, BOMB -> {
                spawnBurst(level, ParticleTypes.CLOUD, x, y, z, 8, 0.2D);
                spawnBurst(level, ParticleTypes.POOF, x, y, z, 4, 0.1D);
            }
            case TORPEDO -> {
                spawnBurst(level, ParticleTypes.CLOUD, x, y, z, 6, 0.18D);
                spawnBurst(level, ParticleTypes.SPLASH, x, y, z, 4, 0.12D);
            }
            case NONE -> spawnBurst(level, ParticleTypes.SMOKE, x, y, z, 10, 0.24D);
        }
    }

    private static void spawnImpactReact(ClientLevel level, double x, double y, double z,
                                         LegacyShipProjectileVisual projectileVisual) {
        switch (projectileVisual) {
            case MISSILE -> {
                spawnBurst(level, ParticleTypes.EXPLOSION, x, y, z, 4, 0.12D);
                spawnBurst(level, ParticleTypes.FLAME, x, y, z, 8, 0.2D);
            }
            case AIRPLANE -> {
                spawnBurst(level, ParticleTypes.EXPLOSION, x, y, z, 3, 0.08D);
                spawnBurst(level, ParticleTypes.CRIT, x, y, z, 8, 0.2D);
            }
            case BOMB -> {
                spawnBurst(level, ParticleTypes.EXPLOSION, x, y, z, 5, 0.12D);
                spawnBurst(level, ParticleTypes.SMOKE, x, y, z, 8, 0.18D);
            }
            case TORPEDO -> {
                spawnBurst(level, ParticleTypes.SPLASH, x, y, z, 8, 0.18D);
                spawnBurst(level, ParticleTypes.BUBBLE, x, y, z, 6, 0.14D);
            }
            case NONE -> spawnBurst(level, ParticleTypes.FLAME, x, y, z, 6, 0.12D);
        }
    }

    private static Component toMessage(CombatReactType reactType) {
        return switch (reactType) {
            case MISS -> Component.translatable("chat.shincolle.combat.miss");
            case CRIT -> Component.translatable("chat.shincolle.combat.crit");
            case DOUBLE_HIT -> Component.translatable("chat.shincolle.combat.double");
            case TRIPLE_HIT -> Component.translatable("chat.shincolle.combat.triple");
            case DODGE -> Component.translatable("chat.shincolle.combat.dodge");
            case LAUNCH -> Component.translatable("chat.shincolle.combat.launch");
            case HIT -> Component.translatable("chat.shincolle.combat.hit");
        };
    }

    private static void spawnBurst(ClientLevel level, ParticleOptions particle,
                                   double x, double y, double z, int count, double spread) {
        for (int i = 0; i < count; i++) {
            double dx = (level.random.nextDouble() - 0.5D) * spread;
            double dy = (level.random.nextDouble() - 0.5D) * spread;
            double dz = (level.random.nextDouble() - 0.5D) * spread;
            level.addParticle(particle, x + dx, y + dy, z + dz, dx * 0.25D, dy * 0.25D, dz * 0.25D);
        }
    }

    private static void spawnDirectedBurst(ClientLevel level, ParticleOptions particle,
                                           double x, double y, double z,
                                           double velocityX, double velocityY, double velocityZ,
                                           int count, double spread, double speedScale) {
        for (int i = 0; i < count; i++) {
            double dx = (level.random.nextDouble() - 0.5D) * spread;
            double dy = (level.random.nextDouble() - 0.5D) * spread;
            double dz = (level.random.nextDouble() - 0.5D) * spread;
            level.addParticle(particle,
                    x + dx, y + dy, z + dz,
                    velocityX * speedScale + dx * 0.2D,
                    velocityY * speedScale + dy * 0.2D,
                    velocityZ * speedScale + dz * 0.2D);
        }
    }
}
