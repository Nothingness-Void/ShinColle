package com.lulan.shincolle.network;

import com.lulan.shincolle.teitoku.TeitokuHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;

import java.util.function.Supplier;

public record ClientboundSyncTeitokuDataPacket(CompoundTag data) implements CustomPacketPayload {

    public static final Type<ClientboundSyncTeitokuDataPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath("shincolle", "clientbound_sync_teitoku_data_packet"));
    public static final StreamCodec<FriendlyByteBuf, ClientboundSyncTeitokuDataPacket> STREAM_CODEC = StreamCodec.of(ClientboundSyncTeitokuDataPacket::encodeBuffer, ClientboundSyncTeitokuDataPacket::decode);

    private static void encodeBuffer(FriendlyByteBuf buffer, ClientboundSyncTeitokuDataPacket packet) {
        encode(packet, buffer);
    }

    @Override
    public Type<ClientboundSyncTeitokuDataPacket> type() {
        return TYPE;
    }

    public static void encode(ClientboundSyncTeitokuDataPacket packet, FriendlyByteBuf buffer) {
        buffer.writeNbt(packet.data);
    }

    public static ClientboundSyncTeitokuDataPacket decode(FriendlyByteBuf buffer) {
        CompoundTag tag = buffer.readNbt();
        return new ClientboundSyncTeitokuDataPacket(tag == null ? new CompoundTag() : tag);
    }

    public static void handle(ClientboundSyncTeitokuDataPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> applyClient(packet.data));
    }

    @OnlyIn(Dist.CLIENT)
    private static void applyClient(CompoundTag data) {
        Player player = Minecraft.getInstance().player;
        if (player == null) {
            return;
        }

        TeitokuHelper.get(player).ifPresent(teitokuData -> teitokuData.loadFromTag(data));
    }
}
