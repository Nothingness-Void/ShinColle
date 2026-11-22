package com.lulan.shincolle.network;

import com.lulan.shincolle.ShinColle;
import com.lulan.shincolle.teitoku.TeitokuHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.PacketDistributor;

public final class ModNetwork {

    private static final String PROTOCOL_VERSION = "2";

    private ModNetwork() {
    }

    public static void register(RegisterPayloadHandlersEvent event) {
        var registrar = event.registrar(PROTOCOL_VERSION);
        registrar.playToClient(ClientboundSyncTeitokuDataPacket.TYPE, ClientboundSyncTeitokuDataPacket.STREAM_CODEC, ClientboundSyncTeitokuDataPacket::handle);
        registrar.playToServer(ServerboundGameplayCommandPacket.TYPE, ServerboundGameplayCommandPacket.STREAM_CODEC, ServerboundGameplayCommandPacket::handle);
        registrar.playToServer(ServerboundShipCommandPacket.TYPE, ServerboundShipCommandPacket.STREAM_CODEC, ServerboundShipCommandPacket::handle);
        registrar.playToClient(ClientboundGameplayStatePacket.TYPE, ClientboundGameplayStatePacket.STREAM_CODEC, ClientboundGameplayStatePacket::handle);
        registrar.playToClient(ClientboundCombatReactPacket.TYPE, ClientboundCombatReactPacket.STREAM_CODEC, ClientboundCombatReactPacket::handle);
        registrar.playToClient(ClientboundSpawnParticlePacket.TYPE, ClientboundSpawnParticlePacket.STREAM_CODEC, ClientboundSpawnParticlePacket::handle);
    }

    public static void syncTeitoku(ServerPlayer player) {
        if (player.connection == null) {
            return;
        }
        TeitokuHelper.get(player).ifPresent(teitokuData ->
                PacketDistributor.sendToPlayer(player,
                        new ClientboundSyncTeitokuDataPacket(teitokuData.saveToTag(new CompoundTag()))));
    }

    public static void syncGameplayState(ServerPlayer player, CompoundTag payload) {
        if (player.connection == null) {
            return;
        }
        PacketDistributor.sendToPlayer(player, new ClientboundGameplayStatePacket(payload));
    }

    public static void sendToPlayer(ServerPlayer player, CustomPacketPayload packet) {
        if (player.connection != null) {
            PacketDistributor.sendToPlayer(player, packet);
        }
    }

    public static void sendToTrackingAndSelf(Entity entity, CustomPacketPayload packet) {
        PacketDistributor.sendToPlayersTrackingEntityAndSelf(entity, packet);
    }

    public static void sendToServer(CustomPacketPayload packet) {
        PacketDistributor.sendToServer(packet);
    }
}
