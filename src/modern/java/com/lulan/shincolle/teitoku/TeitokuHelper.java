package com.lulan.shincolle.teitoku;

import com.lulan.shincolle.network.ModNetwork;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.util.LazyOptional;

public final class TeitokuHelper {

    public static final Capability<TeitokuData> TEITOKU_CAPABILITY =
            CapabilityManager.get(new CapabilityToken<>() {});

    private TeitokuHelper() {
    }

    public static LazyOptional<TeitokuData> get(Player player) {
        return player.getCapability(TEITOKU_CAPABILITY);
    }

    public static void initializeAndSync(ServerPlayer player) {
        TeitokuSavedData savedData = TeitokuSavedData.get(player.serverLevel());
        get(player).ifPresent(teitokuData -> {
            teitokuData.setPlayerName(player.getGameProfile().getName());
            teitokuData.setPlayerUid(savedData.getOrCreateUid(player));
            ModNetwork.syncTeitoku(player);
        });
    }

    public static void sync(ServerPlayer player) {
        ModNetwork.syncTeitoku(player);
    }

    public static int getPlayerUid(Player player) {
        return get(player)
                .map(TeitokuData::getPlayerUid)
                .orElse(0);
    }

    public static int resolvePlayerUid(ServerLevel level, java.util.UUID playerUuid, String playerName) {
        return TeitokuSavedData.get(level).getOrCreateUid(playerUuid, playerName);
    }

    public static void markRingState(ServerPlayer player, boolean active) {
        get(player).ifPresent(teitokuData -> {
            teitokuData.setHasRing(true);
            teitokuData.setRingActive(active);
            if (!active) {
                teitokuData.setRingFlying(false);
            }
            sync(player);
        });
    }

    public static void incrementMarriageCount(ServerPlayer player) {
        get(player).ifPresent(teitokuData -> {
            teitokuData.setHasRing(true);
            teitokuData.incrementMarriageNum();
            sync(player);
        });
    }

    public static void addCollectedShip(ServerPlayer player, int legacyClassId) {
        get(player).ifPresent(teitokuData -> {
            teitokuData.addCollectedShip(legacyClassId);
            sync(player);
        });
    }

    public static void addCollectedEquipment(ServerPlayer player, int equipmentId) {
        get(player).ifPresent(teitokuData -> {
            teitokuData.addCollectedEquipment(equipmentId);
            sync(player);
        });
    }
}
