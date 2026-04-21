package com.lulan.shincolle.network;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record ServerboundGameplayCommandPacket(GameplayCommandType type, CompoundTag payload) {

    public static ServerboundGameplayCommandPacket of(GameplayCommandType type, CompoundTag payload) {
        return new ServerboundGameplayCommandPacket(type, payload == null ? new CompoundTag() : payload.copy());
    }

    public static void encode(ServerboundGameplayCommandPacket packet, FriendlyByteBuf buffer) {
        buffer.writeVarInt(packet.type.ordinal());
        buffer.writeNbt(packet.payload);
    }

    public static ServerboundGameplayCommandPacket decode(FriendlyByteBuf buffer) {
        GameplayCommandType type = GameplayCommandType.fromOrdinal(buffer.readVarInt());
        CompoundTag payload = buffer.readNbt();
        return new ServerboundGameplayCommandPacket(type, payload == null ? new CompoundTag() : payload);
    }

    public static void handle(ServerboundGameplayCommandPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ServerPlayer sender = context.getSender();
            if (sender != null) {
                GameplayCommandHandler.handle(sender, packet.type, packet.payload);
            }
        });
        context.setPacketHandled(true);
    }
}

