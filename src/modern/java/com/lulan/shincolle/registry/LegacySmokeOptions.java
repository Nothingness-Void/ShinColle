package com.lulan.shincolle.registry;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record LegacySmokeOptions(float scale) implements ParticleOptions {

    public static final MapCodec<LegacySmokeOptions> CODEC = Codec.FLOAT.fieldOf("scale")
            .xmap(LegacySmokeOptions::new, LegacySmokeOptions::scale);
    public static final StreamCodec<RegistryFriendlyByteBuf, LegacySmokeOptions> STREAM_CODEC =
            StreamCodec.composite(ByteBufCodecs.FLOAT, LegacySmokeOptions::scale, LegacySmokeOptions::new);

    @Override
    public ParticleType<LegacySmokeOptions> getType() {
        return ModParticleTypes.LEGACY_SMOKE.get();
    }
}
