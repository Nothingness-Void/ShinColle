package com.lulan.shincolle.entity.ship;

import com.lulan.shincolle.registry.LegacySmokeOptions;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.List;

public final class LegacyShipSmoke {

    public record Emission(ParticleOptions particle, double x, double y, double z,
                           double velocityX, double velocityY, double velocityZ) {
        public void spawn(Level level) {
            level.addParticle(this.particle, this.x, this.y, this.z,
                    this.velocityX, this.velocityY, this.velocityZ);
        }
    }

    private LegacyShipSmoke() {
    }

    public static List<Emission> scaled(double x, double y, double z, float scale, double velocityY) {
        List<Emission> result = new ArrayList<>(3);
        LegacySmokeOptions particle = new LegacySmokeOptions(scale);
        for (int i = 0; i < 3; i++) {
            result.add(new Emission(particle, x, y + i * 0.1D, z, 0.0D, velocityY, 0.0D));
        }
        return result;
    }

    public static List<Emission> normal(double x, double y, double z) {
        List<Emission> result = new ArrayList<>(3);
        for (int i = 0; i < 3; i++) {
            result.add(new Emission(ParticleTypes.SMOKE, x, y + i * 0.1D, z, 0.0D, 0.0D, 0.0D));
        }
        return result;
    }

    public static int damageEffectType(int hpState) {
        return switch (hpState) {
            case 1 -> 4;
            case 2 -> 5;
            case 3 -> 7;
            default -> 0;
        };
    }

    public static boolean damageTick(boolean clientSide, int tick) {
        return clientSide && (tick & 15) == 0;
    }

    public static List<Emission> damage(RandomSource random, int hpState, boolean hostile,
                                        double width, double x, double y, double z) {
        int type = damageEffectType(hpState);
        if (type == 0) {
            return List.of();
        }
        double spread = hostile ? width * 1.5D : width;
        List<Emission> result = new ArrayList<>();
        int count = type == 7 ? 4 : 3;
        for (int i = 0; i < count; i++) {
            double dx = random.nextFloat() * spread - spread / 2.0D;
            double dy = random.nextFloat() * spread - spread / 2.0D;
            double dz = random.nextFloat() * spread - spread / 2.0D;
            result.add(new Emission(type == 7 ? ParticleTypes.LARGE_SMOKE : ParticleTypes.SMOKE,
                    x + dx, y + 0.7D + dy, z + dz, 0.0D, type == 7 ? 0.0D : 0.05D, 0.0D));
            if (type != 4) {
                result.add(new Emission(ParticleTypes.FLAME,
                        x + dz, y + 0.7D + dy, z + dx, 0.0D, 0.05D, 0.0D));
            }
        }
        return result;
    }

    public static List<Emission> death(RandomSource random, boolean hostile, int scaleLevel,
                                       double width, int particleSetting, double x, double y, double z) {
        int count = (int) ((3 - particleSetting) * 1.8F);
        double range = width * 1.2D;
        List<Emission> result = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            double px = x - range + random.nextDouble() * range * 2.0D;
            double py = y + (hostile ? 0.3D : 0.1D) + random.nextDouble() * 0.3D;
            double pz = z - range + random.nextDouble() * range * 2.0D;
            result.addAll(scaled(px, py, pz, hostile ? 1.0F + scaleLevel : 1.5F, 0.0D));
        }
        return result;
    }
}
