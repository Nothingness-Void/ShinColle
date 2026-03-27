package com.lulan.shincolle.network;

import com.lulan.shincolle.ShinColle;
import com.lulan.shincolle.teitoku.TeitokuHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

public final class ModNetwork {

    private static final String PROTOCOL_VERSION = "1";
    private static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            ResourceLocation.fromNamespaceAndPath(ShinColle.MOD_ID, "main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals);

    private static boolean initialized;
    private static int packetId;

    private ModNetwork() {
    }

    public static void register() {
        if (initialized) {
            return;
        }

        CHANNEL.messageBuilder(ClientboundSyncTeitokuDataPacket.class, packetId++, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(ClientboundSyncTeitokuDataPacket::encode)
                .decoder(ClientboundSyncTeitokuDataPacket::decode)
                .consumerMainThread(ClientboundSyncTeitokuDataPacket::handle)
                .add();

        initialized = true;
    }

    public static void syncTeitoku(ServerPlayer player) {
        TeitokuHelper.get(player).ifPresent(teitokuData ->
                CHANNEL.send(PacketDistributor.PLAYER.with(() -> player),
                        new ClientboundSyncTeitokuDataPacket(teitokuData.saveToTag(new CompoundTag()))));
    }
}
