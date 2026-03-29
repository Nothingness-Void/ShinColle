package com.lulan.shincolle.client;

import com.lulan.shincolle.network.ClientboundCombatReactPacket;
import com.lulan.shincolle.network.ClientboundSpawnParticlePacket;
import com.lulan.shincolle.network.CombatReactType;
import com.lulan.shincolle.network.GameplayParticleType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
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

        Entity target = level.getEntity(packet.targetId());
        if (target != null) {
            double x = target.getX();
            double y = target.getY() + target.getBbHeight() * 0.75D;
            double z = target.getZ();
            switch (packet.reactType()) {
                case MISS -> spawnBurst(level, ParticleTypes.SMOKE, x, y, z, 6, 0.14D);
                case CRIT -> spawnBurst(level, ParticleTypes.CRIT, x, y, z, 10, 0.21D);
                case DOUBLE_HIT -> spawnBurst(level, ParticleTypes.SWEEP_ATTACK, x, y, z, 8, 0.16D);
                case TRIPLE_HIT -> spawnBurst(level, ParticleTypes.SWEEP_ATTACK, x, y, z, 12, 0.21D);
                case DODGE -> spawnBurst(level, ParticleTypes.CLOUD, x, y, z, 8, 0.18D);
                case LAUNCH -> spawnBurst(level, ParticleTypes.SMOKE, x, y, z, 10, 0.24D);
                case HIT -> spawnBurst(level, ParticleTypes.FLAME, x, y, z, 6, 0.12D);
            }
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
            case LAUNCH_SMOKE -> spawnBurst(level, ParticleTypes.SMOKE, packet.x(), packet.y(), packet.z(), 8, 0.2D);
            case HIT_EXPLOSION -> spawnBurst(level, ParticleTypes.EXPLOSION, packet.x(), packet.y(), packet.z(), 4, 0.12D);
            case AIR_TRAIL -> spawnBurst(level, ParticleTypes.CLOUD, packet.x(), packet.y(), packet.z(), 3, 0.06D);
            case TEXT_MISS -> spawnBurst(level, ParticleTypes.SMOKE, packet.x(), packet.y(), packet.z(), 4, 0.09D);
            case TEXT_CRIT -> spawnBurst(level, ParticleTypes.CRIT, packet.x(), packet.y(), packet.z(), 5, 0.1D);
            case TEXT_DOUBLE -> spawnBurst(level, ParticleTypes.SWEEP_ATTACK, packet.x(), packet.y(), packet.z(), 4, 0.08D);
            case TEXT_TRIPLE -> spawnBurst(level, ParticleTypes.SWEEP_ATTACK, packet.x(), packet.y(), packet.z(), 6, 0.12D);
            case TEXT_DODGE -> spawnBurst(level, ParticleTypes.CLOUD, packet.x(), packet.y(), packet.z(), 4, 0.08D);
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

    private static void spawnBurst(ClientLevel level, net.minecraft.core.particles.ParticleOptions particle,
                                   double x, double y, double z, int count, double spread) {
        for (int i = 0; i < count; i++) {
            double dx = (level.random.nextDouble() - 0.5D) * spread;
            double dy = (level.random.nextDouble() - 0.5D) * spread;
            double dz = (level.random.nextDouble() - 0.5D) * spread;
            level.addParticle(particle, x + dx, y + dy, z + dz, dx * 0.25D, dy * 0.25D, dz * 0.25D);
        }
    }
}

