package com.lulan.shincolle.network;

import com.lulan.shincolle.client.GameplayClientEffects;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record ClientboundSpawnParticlePacket(GameplayParticleType particleType,
                                             double x, double y, double z,
                                             double velocityX, double velocityY, double velocityZ) {

    public static void encode(ClientboundSpawnParticlePacket packet, FriendlyByteBuf buffer) {
        buffer.writeVarInt(packet.particleType.ordinal());
        buffer.writeDouble(packet.x);
        buffer.writeDouble(packet.y);
        buffer.writeDouble(packet.z);
        buffer.writeDouble(packet.velocityX);
        buffer.writeDouble(packet.velocityY);
        buffer.writeDouble(packet.velocityZ);
    }

    public static ClientboundSpawnParticlePacket decode(FriendlyByteBuf buffer) {
        GameplayParticleType type = GameplayParticleType.fromOrdinal(buffer.readVarInt());
        double x = buffer.readDouble();
        double y = buffer.readDouble();
        double z = buffer.readDouble();
        double velocityX = buffer.readDouble();
        double velocityY = buffer.readDouble();
        double velocityZ = buffer.readDouble();
        return new ClientboundSpawnParticlePacket(type, x, y, z, velocityX, velocityY, velocityZ);
    }

    public static void handle(ClientboundSpawnParticlePacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> GameplayClientEffects.handleParticle(packet));
        context.setPacketHandled(true);
    }
}

