package com.lulan.shincolle.client;

import com.lulan.shincolle.client.particle.LegacyEmotionParticle;
import com.lulan.shincolle.client.particle.LegacyTextParticle;
import com.lulan.shincolle.client.particle.LegacyTeamMarkerParticle;
import com.lulan.shincolle.config.ShinColleConfig;
import com.lulan.shincolle.entity.projectile.LegacyShipProjectileVisual;
import com.lulan.shincolle.entity.ship.LegacyShipEntity;
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

    /** Spawns the entity-bound legacy ParticleTeam marker on the local client. */
    public static void spawnLegacyTeamMarker(Entity host, int legacyMarkerType) {
        Minecraft minecraft = Minecraft.getInstance();
        if (host == null || !(minecraft.level instanceof ClientLevel)) {
            return;
        }
        ClientLevel level = (ClientLevel) minecraft.level;
        minecraft.particleEngine.add(new LegacyTeamMarkerParticle(
                level, host, 1.0F, legacyMarkerType));
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

        Entity attacker = level.getEntity(packet.attackerId());
        if (minecraft.player != null && packet.allowsPlayerActionbar(minecraft.player, attacker)) {
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
            case LEGACY_SHIP_ANIMATION -> {
                if (level.getEntity(packet.entityId()) instanceof LegacyShipEntity ship) {
                    ship.setLegacyModelAttackTick(packet.variant() == 12 ? 100 : 50);
                }
            }
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
            case LEGACY_RAILGUN -> {
                spawnDirectedBurst(level, ParticleTypes.FLAME, packet.x(), packet.y(), packet.z(),
                        packet.velocityX(), packet.velocityY(), packet.velocityZ(), 4, 0.03D, 0.1D);
                spawnDirectedBurst(level, ParticleTypes.ELECTRIC_SPARK, packet.x(), packet.y(), packet.z(),
                        packet.velocityX(), packet.velocityY(), packet.velocityZ(), 3, 0.04D, 0.08D);
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
                if (ShinColleConfig.canFlare()) {
                    spawnBurst(level, ParticleTypes.END_ROD, packet.x(), packet.y(), packet.z(), 8, 0.2D);
                    spawnBurst(level, ParticleTypes.FLAME, packet.x(), packet.y(), packet.z(), 6, 0.16D);
                }
            }
            case SEARCHLIGHT_MARK -> {
                if (ShinColleConfig.canSearchlight()) {
                    spawnBurst(level, ParticleTypes.ELECTRIC_SPARK, packet.x(), packet.y(), packet.z(), 7, 0.18D);
                    spawnBurst(level, ParticleTypes.END_ROD, packet.x(), packet.y(), packet.z(), 3, 0.08D);
                }
            }
            case BEAM_TRAIL -> {
                spawnDirectedBurst(level, ParticleTypes.END_ROD, packet.x(), packet.y(), packet.z(),
                        packet.velocityX(), packet.velocityY(), packet.velocityZ(), 5, 0.06D, 0.12D);
                spawnDirectedBurst(level, ParticleTypes.ELECTRIC_SPARK, packet.x(), packet.y(), packet.z(),
                        packet.velocityX(), packet.velocityY(), packet.velocityZ(), 3, 0.04D, 0.08D);
            }
            case BEAM_IMPACT -> {
                spawnBurst(level, ParticleTypes.EXPLOSION, packet.x(), packet.y(), packet.z(), 5, 0.14D);
                spawnBurst(level, ParticleTypes.END_ROD, packet.x(), packet.y(), packet.z(), 10, 0.22D);
            }
            case BLACK_HOLE -> {
                spawnBurst(level, ParticleTypes.PORTAL, packet.x(), packet.y(), packet.z(), 18, 0.45D);
                spawnBurst(level, ParticleTypes.SMOKE, packet.x(), packet.y(), packet.z(), 8, 0.28D);
            }
            case LEGACY_CANNON -> spawnLegacyCannonSmoke(level, packet);
            case LEGACY_DOUBLE_CANNON -> spawnLegacyDoubleCannonSmoke(level, packet);
            case LEGACY_NORTHERN_CANNON -> spawnLegacyNorthernCannon(level, packet);
            case LEGACY_NORTHERN_HIT -> spawnLegacyNorthernHit(level, packet);
            case LEGACY_RE_LASER -> spawnLegacyReLaser(level, packet);
            // The hime heal branches used the legacy type-4 green/red sparkle
            // around the healed entity.
            case HEALING_SPARKLE -> spawnBurst(level, ParticleTypes.HAPPY_VILLAGER,
                    packet.x(), packet.y(), packet.z(), 8, 0.24D);
            case LEGACY_EMOTION -> spawnLegacyEmotion(level, packet);
            case LEGACY_DRIP_WATER -> level.addParticle(ParticleTypes.DRIPPING_WATER,
                    packet.x() + level.random.nextFloat() * 0.7D - 0.35D, packet.y(),
                    packet.z() + level.random.nextFloat() * 0.7D - 0.35D,
                    packet.velocityX(), packet.velocityY(), packet.velocityZ());
            case TEXT_MISS -> spawnLegacyText(level, packet, 0);
            case TEXT_CRIT -> spawnLegacyText(level, packet, 1);
            case TEXT_DOUBLE -> spawnLegacyText(level, packet, 2);
            case TEXT_TRIPLE -> spawnLegacyText(level, packet, 3);
            case TEXT_DODGE -> spawnLegacyText(level, packet, 4);
        }
    }

    private static void spawnLegacyEmotion(ClientLevel level, ClientboundSpawnParticlePacket packet) {
        Minecraft.getInstance().particleEngine.add(new LegacyEmotionParticle(level,
                level.getEntity(packet.entityId()), packet.x(), packet.y(), packet.z(),
                (float)packet.velocityX(), (int)packet.velocityY(), packet.variant()));
    }

    private static void spawnLegacyText(ClientLevel level, ClientboundSpawnParticlePacket packet, int type) {
        Entity host = level.getEntity(packet.entityId());
        Minecraft.getInstance().particleEngine.add(new LegacyTextParticle(level,
                host == null ? packet.x() : host.getX(),
                host == null ? packet.y() + packet.velocityY() : host.getY() + host.getBbHeight() * 1.3D,
                host == null ? packet.z() : host.getZ(), 1F, type));
    }

    /** Recreates the legacy type-5 six-stream large-smoke cannon burst. */
    private static void spawnLegacyCannonSmoke(ClientLevel level,
                                                ClientboundSpawnParticlePacket packet) {
        double width = packet.velocityX();
        double length = packet.velocityY();
        double height = packet.velocityZ();
        double yaw = Math.toRadians(packet.variant() / 1000.0D);
        double cos = Math.cos(yaw);
        double sin = Math.sin(yaw);

        // CalcHelper.rotateXZByAxis(length, width, yaw) returns [world-z, world-x].
        double firstZ = length * cos + width * sin;
        double firstX = width * cos - length * sin;
        double secondZ = length * cos - width * sin;
        double secondX = -width * cos - length * sin;
        double smokeVelocityZ = 0.25D * cos;
        double smokeVelocityX = -0.25D * sin;

        for (int index = 0; index < 24; index++) {
            double randomX = level.random.nextDouble() * 2.0D - 1.0D;
            double randomY = level.random.nextDouble() * 2.0D - 1.0D;
            double randomZ = level.random.nextDouble() * 2.0D - 1.0D;
            double randomSpeed = level.random.nextDouble() * 2.0D;

            addLegacyCannonSmoke(level, packet, firstX, firstZ,
                    randomX, randomY, randomZ, height, smokeVelocityX, smokeVelocityZ, randomSpeed);
            addLegacyCannonSmoke(level, packet, secondX, secondZ,
                    randomX, randomZ, randomY, height, smokeVelocityX, smokeVelocityZ, randomSpeed);
            addLegacyCannonSmoke(level, packet, firstX, firstZ,
                    randomY, randomX, randomZ, height, smokeVelocityX, smokeVelocityZ, randomSpeed);
            addLegacyCannonSmoke(level, packet, secondX, secondZ,
                    randomY, randomZ, randomX, height, smokeVelocityX, smokeVelocityZ, randomSpeed);
            addLegacyCannonSmoke(level, packet, firstX, firstZ,
                    randomZ, randomX, randomY, height, smokeVelocityX, smokeVelocityZ, randomSpeed);
            addLegacyCannonSmoke(level, packet, secondX, secondZ,
                    randomZ, randomY, randomX, height, smokeVelocityX, smokeVelocityZ, randomSpeed);
        }
    }

    private static void addLegacyCannonSmoke(ClientLevel level,
                                              ClientboundSpawnParticlePacket packet,
                                              double xOffset, double zOffset,
                                              double randomX, double randomY, double randomZ,
                                              double height, double velocityX, double velocityZ,
                                              double speed) {
        level.addParticle(ParticleTypes.LARGE_SMOKE,
                packet.x() + xOffset + randomX,
                packet.y() + height + randomY,
                packet.z() + zOffset + randomZ,
                velocityX * speed, 0.05D * speed, velocityZ * speed);
    }

    /** Recreates legacy particle type 19, the double large-smoke cannon stream. */
    private static void spawnLegacyDoubleCannonSmoke(ClientLevel level,
                                                      ClientboundSpawnParticlePacket packet) {
        double lookX = packet.velocityX();
        double spacing = packet.velocityY();
        double lookZ = packet.velocityZ();
        double yaw = -Math.atan2(lookX, lookZ);
        double firstZ = spacing * Math.sin(yaw);
        double firstX = spacing * Math.cos(yaw);
        double secondZ = -firstZ;
        double secondX = -firstX;

        for (int index = 0; index < 15; index++) {
            double randomY = level.random.nextFloat() - 0.5D;
            double firstSpeed = level.random.nextFloat();
            double secondSpeed = level.random.nextFloat();
            addLegacyDoubleCannonSmoke(level, packet, lookX, lookZ, firstX, firstZ,
                    index, 0.6D, randomY, firstSpeed);
            addLegacyDoubleCannonSmoke(level, packet, lookX, lookZ, secondX, secondZ,
                    index, 0.6D, randomY, secondSpeed);
            addLegacyDoubleCannonSmoke(level, packet, lookX, lookZ, firstX, firstZ,
                    index, 0.9D, randomY, secondSpeed);
            addLegacyDoubleCannonSmoke(level, packet, lookX, lookZ, secondX, secondZ,
                    index, 0.9D, randomY, firstSpeed);
        }
    }

    private static void addLegacyDoubleCannonSmoke(ClientLevel level,
                                                    ClientboundSpawnParticlePacket packet,
                                                    double lookX, double lookZ,
                                                    double xOffset, double zOffset,
                                                    int index, double height,
                                                    double randomY, double speed) {
        level.addParticle(ParticleTypes.LARGE_SMOKE,
                packet.x() + lookX - 0.5D + 0.05D * index + xOffset,
                packet.y() + height + randomY,
                packet.z() + lookZ - 0.5D + 0.05D * index + zOffset,
                lookX * 0.3D * speed, 0.05D * speed, lookZ * 0.3D * speed);
    }

    /** Recreates legacy particle type 31, the Northern Hime snow spray. */
    private static void spawnLegacyNorthernCannon(ClientLevel level,
                                                  ClientboundSpawnParticlePacket packet) {
        double lookX = packet.velocityX();
        double lookZ = packet.velocityZ();
        for (int index = 0; index < 22; index++) {
            double randomY = level.random.nextFloat() - 0.5D;
            double firstSpeed = level.random.nextFloat();
            double secondSpeed = level.random.nextFloat();
            addLegacyNorthernSnow(level, packet, lookX, lookZ, index, 0.7D, randomY, firstSpeed);
            addLegacyNorthernSnow(level, packet, lookX, lookZ, index, 0.9D, randomY, secondSpeed);
        }
    }

    private static void addLegacyNorthernSnow(ClientLevel level,
                                              ClientboundSpawnParticlePacket packet,
                                              double lookX, double lookZ,
                                              int index, double height,
                                              double randomY, double speed) {
        level.addParticle(ParticleTypes.ITEM_SNOWBALL,
                packet.x() + lookX - 0.5D + 0.05D * index,
                packet.y() + height + randomY,
                packet.z() + lookZ - 0.5D + 0.05D * index,
                lookX * 0.3D * speed, 0.05D * speed, lookZ * 0.3D * speed);
    }

    /** Recreates legacy particle type 30, the Northern Hime snow hit. */
    private static void spawnLegacyNorthernHit(ClientLevel level,
                                                ClientboundSpawnParticlePacket packet) {
        for (int index = 0; index < 15; index++) {
            double randomX = level.random.nextFloat() * 2.0D - 1.0D;
            double randomY = level.random.nextFloat() * 2.0D - 1.0D;
            double randomZ = level.random.nextFloat() * 2.0D - 1.0D;
            level.addParticle(ParticleTypes.ITEM_SNOWBALL,
                    packet.x() + randomX, packet.y() + 0.8D + randomY, packet.z() + randomZ,
                    0.0D, 0.5D, 0.0D);
        }
    }

    /** Modern directed equivalent of legacy type 6's adjustable purple laser. */
    private static void spawnLegacyReLaser(ClientLevel level,
                                           ClientboundSpawnParticlePacket packet) {
        double distance = packet.variant() / 1000.0D;
        if (distance <= 1.0E-4D) {
            return;
        }

        int count = Math.max(8, Math.min(64, (int) Math.ceil(distance * 5.0D)));
        for (int index = 0; index < count; index++) {
            double progress = (index + 0.5D) / count;
            double jitter = (level.random.nextDouble() - 0.5D) * 0.035D;
            level.addParticle(ParticleTypes.DRAGON_BREATH,
                    packet.x() + packet.velocityX() * distance * progress + jitter,
                    packet.y() + packet.velocityY() * distance * progress + jitter,
                    packet.z() + packet.velocityZ() * distance * progress + jitter,
                    packet.velocityX() * 0.02D,
                    packet.velocityY() * 0.02D,
                    packet.velocityZ() * 0.02D);
        }
    }

    private static void spawnCombatReact(ClientLevel level, Entity anchor, ClientboundCombatReactPacket packet) {
        double x = anchor.getX();
        double y = anchor.getY() + anchor.getBbHeight() * 0.75D;
        double z = anchor.getZ();

        switch (packet.reactType()) {
            case MISS, CRIT, DOUBLE_HIT, TRIPLE_HIT, DODGE -> {
            }
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
