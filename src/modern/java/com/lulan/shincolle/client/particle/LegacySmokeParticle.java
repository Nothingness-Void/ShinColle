package com.lulan.shincolle.client.particle;

import com.lulan.shincolle.registry.LegacySmokeOptions;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SmokeParticle;
import net.minecraft.client.particle.SpriteSet;

public final class LegacySmokeParticle extends SmokeParticle {

    private LegacySmokeParticle(ClientLevel level, double x, double y, double z,
                                double velocityX, double velocityY, double velocityZ,
                                float scale, SpriteSet sprites) {
        super(level, x, y, z, velocityX, velocityY, velocityZ, scale, sprites);
    }

    public record Provider(SpriteSet sprites) implements ParticleProvider<LegacySmokeOptions> {
        @Override
        public Particle createParticle(LegacySmokeOptions options, ClientLevel level,
                                       double x, double y, double z,
                                       double velocityX, double velocityY, double velocityZ) {
            return new LegacySmokeParticle(level, x, y, z,
                    velocityX, velocityY, velocityZ, options.scale(), this.sprites);
        }
    }
}
