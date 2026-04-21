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

public record ClientboundSyncTeitokuDataPacket(CompoundTag data) {

    public static void encode(ClientboundSyncTeitokuDataPacket packet, FriendlyByteBuf buffer) {
        buffer.writeNbt(packet.data);
    }

    public static ClientboundSyncTeitokuDataPacket decode(FriendlyByteBuf buffer) {
        CompoundTag tag = buffer.readNbt();
        return new ClientboundSyncTeitokuDataPacket(tag == null ? new CompoundTag() : tag);
    }

    public static void handle(ClientboundSyncTeitokuDataPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> applyClient(packet.data));
        context.setPacketHandled(true);
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
