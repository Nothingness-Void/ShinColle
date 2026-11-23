package com.lulan.shincolle.registry;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.Codec;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;

import java.util.Locale;

public record LegacySmokeOptions(float scale) implements ParticleOptions {

    public static final Codec<LegacySmokeOptions> CODEC = Codec.FLOAT.fieldOf("scale")
            .xmap(LegacySmokeOptions::new, LegacySmokeOptions::scale).codec();
    public static final Deserializer<LegacySmokeOptions> DESERIALIZER = new Deserializer<>() {
        @Override
        public LegacySmokeOptions fromCommand(ParticleType<LegacySmokeOptions> type, StringReader reader)
                throws CommandSyntaxException {
            reader.expect(' ');
            return new LegacySmokeOptions(reader.readFloat());
        }

        @Override
        public LegacySmokeOptions fromNetwork(ParticleType<LegacySmokeOptions> type, FriendlyByteBuf buffer) {
            return new LegacySmokeOptions(buffer.readFloat());
        }
    };

    @Override
    public ParticleType<LegacySmokeOptions> getType() {
        return ModParticleTypes.LEGACY_SMOKE.get();
    }

    @Override
    public void writeToNetwork(FriendlyByteBuf buffer) {
        buffer.writeFloat(this.scale);
    }

    @Override
    public String writeToString() {
        return String.format(Locale.ROOT, "%s %s", BuiltInRegistries.PARTICLE_TYPE.getKey(this.getType()), this.scale);
    }
}
