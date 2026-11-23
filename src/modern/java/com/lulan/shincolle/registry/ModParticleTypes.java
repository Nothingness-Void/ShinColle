package com.lulan.shincolle.registry;

import com.lulan.shincolle.ShinColle;
import com.mojang.serialization.Codec;
import net.minecraft.core.particles.ParticleType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModParticleTypes {

    public static final DeferredRegister<ParticleType<?>> PARTICLE_TYPES =
            DeferredRegister.create(ForgeRegistries.PARTICLE_TYPES, ShinColle.MOD_ID);
    public static final RegistryObject<ParticleType<LegacySmokeOptions>> LEGACY_SMOKE =
            PARTICLE_TYPES.register("legacy_smoke", () -> new ParticleType<LegacySmokeOptions>(
                    true, LegacySmokeOptions.DESERIALIZER) {
                @Override
                public Codec<LegacySmokeOptions> codec() {
                    return LegacySmokeOptions.CODEC;
                }
            });

    private ModParticleTypes() {
    }
}
