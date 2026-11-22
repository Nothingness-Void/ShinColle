package com.lulan.shincolle.network;

import com.lulan.shincolle.client.GameplayClientEffects;
import net.minecraft.network.FriendlyByteBuf;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;

import java.util.function.Supplier;

public record ClientboundSpawnParticlePacket(GameplayParticleType particleType,
                                             double x, double y, double z,
                                             double velocityX, double velocityY, double velocityZ,
                                             int variant, int entityId) implements CustomPacketPayload {

    public static final Type<ClientboundSpawnParticlePacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath("shincolle", "clientbound_spawn_particle_packet"));
    public static final StreamCodec<FriendlyByteBuf, ClientboundSpawnParticlePacket> STREAM_CODEC = StreamCodec.of(ClientboundSpawnParticlePacket::encodeBuffer, ClientboundSpawnParticlePacket::decode);

    private static void encodeBuffer(FriendlyByteBuf buffer, ClientboundSpawnParticlePacket packet) {
        encode(packet, buffer);
    }

    @Override
    public Type<ClientboundSpawnParticlePacket> type() {
        return TYPE;
    }

    public ClientboundSpawnParticlePacket(GameplayParticleType particleType,
                                           double x, double y, double z,
                                           double velocityX, double velocityY, double velocityZ, int variant) {
        this(particleType, x, y, z, velocityX, velocityY, velocityZ, variant, -1);
    }

    public ClientboundSpawnParticlePacket(GameplayParticleType particleType,
                                           double x, double y, double z,
                                           double velocityX, double velocityY, double velocityZ) {
        this(particleType, x, y, z, velocityX, velocityY, velocityZ, 0);
    }

    public static void encode(ClientboundSpawnParticlePacket packet, FriendlyByteBuf buffer) {
        buffer.writeVarInt(packet.particleType.ordinal());
        buffer.writeDouble(packet.x);
        buffer.writeDouble(packet.y);
        buffer.writeDouble(packet.z);
        buffer.writeDouble(packet.velocityX);
        buffer.writeDouble(packet.velocityY);
        buffer.writeDouble(packet.velocityZ);
        buffer.writeVarInt(packet.variant);
        buffer.writeVarInt(packet.entityId);
    }

    public static ClientboundSpawnParticlePacket decode(FriendlyByteBuf buffer) {
        GameplayParticleType type = GameplayParticleType.fromOrdinal(buffer.readVarInt());
        double x = buffer.readDouble();
        double y = buffer.readDouble();
        double z = buffer.readDouble();
        double velocityX = buffer.readDouble();
        double velocityY = buffer.readDouble();
        double velocityZ = buffer.readDouble();
        int variant = buffer.readVarInt();
        int entityId = buffer.readVarInt();
        return new ClientboundSpawnParticlePacket(type, x, y, z, velocityX, velocityY, velocityZ, variant, entityId);
    }

    public static void handle(ClientboundSpawnParticlePacket packet, IPayloadContext context) {
        context.enqueueWork(() -> GameplayClientEffects.handleParticle(packet));
    }
}
