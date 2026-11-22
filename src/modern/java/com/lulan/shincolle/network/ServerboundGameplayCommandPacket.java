package com.lulan.shincolle.network;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;

import java.util.function.Supplier;

public record ServerboundGameplayCommandPacket(GameplayCommandType commandType, CompoundTag payload) implements CustomPacketPayload {

    public static final Type<ServerboundGameplayCommandPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath("shincolle", "serverbound_gameplay_command_packet"));
    public static final StreamCodec<FriendlyByteBuf, ServerboundGameplayCommandPacket> STREAM_CODEC = StreamCodec.of(ServerboundGameplayCommandPacket::encodeBuffer, ServerboundGameplayCommandPacket::decode);

    private static void encodeBuffer(FriendlyByteBuf buffer, ServerboundGameplayCommandPacket packet) {
        encode(packet, buffer);
    }

    @Override
    public Type<ServerboundGameplayCommandPacket> type() {
        return TYPE;
    }

    public static ServerboundGameplayCommandPacket of(GameplayCommandType type, CompoundTag payload) {
        return new ServerboundGameplayCommandPacket(type, payload == null ? new CompoundTag() : payload.copy());
    }

    public static void encode(ServerboundGameplayCommandPacket packet, FriendlyByteBuf buffer) {
        buffer.writeVarInt(packet.commandType.ordinal());
        buffer.writeNbt(packet.payload);
    }

    public static ServerboundGameplayCommandPacket decode(FriendlyByteBuf buffer) {
        GameplayCommandType type = GameplayCommandType.fromOrdinal(buffer.readVarInt());
        CompoundTag payload = buffer.readNbt();
        return new ServerboundGameplayCommandPacket(type, payload == null ? new CompoundTag() : payload);
    }

    public static void handle(ServerboundGameplayCommandPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            ServerPlayer sender = (ServerPlayer) context.player();
            if (sender != null) {
                GameplayCommandHandler.handle(sender, packet.commandType, packet.payload);
            }
        });
    }
}

