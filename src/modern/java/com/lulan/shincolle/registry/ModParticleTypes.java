package com.lulan.shincolle.registry;

import com.lulan.shincolle.ShinColle;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModParticleTypes {

    public static final DeferredRegister<ParticleType<?>> PARTICLE_TYPES =
            DeferredRegister.create(Registries.PARTICLE_TYPE, ShinColle.MOD_ID);
    public static final DeferredHolder<ParticleType<?>, ParticleType<LegacySmokeOptions>> LEGACY_SMOKE =
            PARTICLE_TYPES.register("legacy_smoke", () -> new ParticleType<LegacySmokeOptions>(true) {
                @Override
                public MapCodec<LegacySmokeOptions> codec() {
                    return LegacySmokeOptions.CODEC;
                }

                @Override
                public StreamCodec<? super RegistryFriendlyByteBuf, LegacySmokeOptions> streamCodec() {
                    return LegacySmokeOptions.STREAM_CODEC;
                }
            });

    private ModParticleTypes() {
    }
}
