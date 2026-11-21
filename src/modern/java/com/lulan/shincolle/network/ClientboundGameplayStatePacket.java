package com.lulan.shincolle.network;

import com.lulan.shincolle.teitoku.TeitokuHelper;
import com.lulan.shincolle.menu.FormationMenu;
import com.lulan.shincolle.menu.ShipInventoryMenu;
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

public record ClientboundGameplayStatePacket(CompoundTag payload) implements CustomPacketPayload {

    public static final Type<ClientboundGameplayStatePacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath("shincolle", "clientbound_gameplay_state_packet"));
    public static final StreamCodec<FriendlyByteBuf, ClientboundGameplayStatePacket> STREAM_CODEC = StreamCodec.of(ClientboundGameplayStatePacket::encodeBuffer, ClientboundGameplayStatePacket::decode);

    private static void encodeBuffer(FriendlyByteBuf buffer, ClientboundGameplayStatePacket packet) {
        encode(packet, buffer);
    }

    @Override
    public Type<ClientboundGameplayStatePacket> type() {
        return TYPE;
    }

    public static void encode(ClientboundGameplayStatePacket packet, FriendlyByteBuf buffer) {
        buffer.writeNbt(packet.payload);
    }

    public static ClientboundGameplayStatePacket decode(FriendlyByteBuf buffer) {
        CompoundTag tag = buffer.readNbt();
        return new ClientboundGameplayStatePacket(tag == null ? new CompoundTag() : tag);
    }

    public static void handle(ClientboundGameplayStatePacket packet, IPayloadContext context) {
        context.enqueueWork(() -> applyClient(packet.payload));
    }

    @OnlyIn(Dist.CLIENT)
    private static void applyClient(CompoundTag payload) {
        Player player = Minecraft.getInstance().player;
        if (player == null) {
            return;
        }
        TeitokuHelper.applyClientState(player, payload);
        if (player.containerMenu instanceof FormationMenu menu
                && payload.contains("FormationVisualData")
                && payload.getInt("FormationContainerId") == menu.containerId) {
            menu.applyVisualData(payload.getCompound("FormationVisualData"));
        }
        if (player.containerMenu instanceof ShipInventoryMenu menu
                && payload.contains("ShipInventoryPresentation")
                && payload.getInt("ShipInventoryContainerId") == menu.containerId) {
            menu.applyPresentation(payload.getCompound("ShipInventoryPresentation"));
        }
    }
}
