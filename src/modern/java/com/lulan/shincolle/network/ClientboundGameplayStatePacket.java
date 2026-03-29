package com.lulan.shincolle.network;

import com.lulan.shincolle.teitoku.TeitokuHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record ClientboundGameplayStatePacket(CompoundTag payload) {

    public static void encode(ClientboundGameplayStatePacket packet, FriendlyByteBuf buffer) {
        buffer.writeNbt(packet.payload);
    }

    public static ClientboundGameplayStatePacket decode(FriendlyByteBuf buffer) {
        CompoundTag tag = buffer.readNbt();
        return new ClientboundGameplayStatePacket(tag == null ? new CompoundTag() : tag);
    }

    public static void handle(ClientboundGameplayStatePacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> applyClient(packet.payload));
        context.setPacketHandled(true);
    }

    @OnlyIn(Dist.CLIENT)
    private static void applyClient(CompoundTag payload) {
        Player player = Minecraft.getInstance().player;
        if (player == null) {
            return;
        }
        TeitokuHelper.applyClientState(player, payload);
    }
}

