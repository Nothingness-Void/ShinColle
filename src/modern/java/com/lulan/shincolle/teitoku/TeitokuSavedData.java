package com.lulan.shincolle.teitoku;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class TeitokuSavedData extends SavedData {

    private static final String DATA_NAME = "shincolle_teitoku_registry";
    private static final String NEXT_PLAYER_UID_TAG = "NextPlayerUID";
    private static final String NEXT_SHIP_UID_TAG = "NextShipUID";
    private static final String PLAYERS_TAG = "Players";
    private static final String SHIPS_TAG = "Ships";
    private static final String PLAYER_UID_TAG = "PlayerUID";
    private static final String PLAYER_NAME_TAG = "PlayerName";
    private static final String PLAYER_UUID_TAG = "PlayerUUID";
    private static final String SHIP_UID_TAG = "ShipUID";
    private static final String SHIP_UUID_TAG = "ShipUUID";

    private final Map<UUID, Integer> playerUidMap = new HashMap<>();
    private final Map<UUID, String> playerNameMap = new HashMap<>();
    private final Map<UUID, Integer> shipUidMap = new HashMap<>();
    private int nextPlayerUid = 1;
    private int nextShipUid = 1;

    public static TeitokuSavedData get(ServerLevel level) {
        return level.getServer().overworld().getDataStorage().computeIfAbsent(
                TeitokuSavedData::load,
                TeitokuSavedData::new,
                DATA_NAME);
    }

    public static TeitokuSavedData load(CompoundTag tag) {
        TeitokuSavedData data = new TeitokuSavedData();
        data.nextPlayerUid = Math.max(1, tag.getInt(NEXT_PLAYER_UID_TAG));
        data.nextShipUid = Math.max(1, tag.getInt(NEXT_SHIP_UID_TAG));

        ListTag players = tag.getList(PLAYERS_TAG, Tag.TAG_COMPOUND);
        for (int index = 0; index < players.size(); index++) {
            CompoundTag playerTag = players.getCompound(index);
            if (!playerTag.hasUUID(PLAYER_UUID_TAG) || !playerTag.contains(PLAYER_UID_TAG)) {
                continue;
            }

            UUID uuid = playerTag.getUUID(PLAYER_UUID_TAG);
            int playerUid = playerTag.getInt(PLAYER_UID_TAG);
            String playerName = playerTag.getString(PLAYER_NAME_TAG);
            if (playerUid <= 0) {
                continue;
            }

            data.playerUidMap.put(uuid, playerUid);
            data.playerNameMap.put(uuid, playerName);
            data.nextPlayerUid = Math.max(data.nextPlayerUid, playerUid + 1);
        }

        ListTag ships = tag.getList(SHIPS_TAG, Tag.TAG_COMPOUND);
        for (int index = 0; index < ships.size(); index++) {
            CompoundTag shipTag = ships.getCompound(index);
            if (!shipTag.hasUUID(SHIP_UUID_TAG) || !shipTag.contains(SHIP_UID_TAG)) {
                continue;
            }

            UUID shipUuid = shipTag.getUUID(SHIP_UUID_TAG);
            int shipUid = shipTag.getInt(SHIP_UID_TAG);
            if (shipUid <= 0) {
                continue;
            }

            data.shipUidMap.put(shipUuid, shipUid);
            data.nextShipUid = Math.max(data.nextShipUid, shipUid + 1);
        }

        return data;
    }

    public int getOrCreateUid(ServerPlayer player) {
        return this.getOrCreateUid(player.getUUID(), player.getGameProfile().getName());
    }

    public int getOrCreateUid(UUID uuid, String playerName) {
        Integer existing = this.playerUidMap.get(uuid);

        if (existing != null) {
            if (!playerName.equals(this.playerNameMap.get(uuid))) {
                this.playerNameMap.put(uuid, playerName);
                this.setDirty();
            }
            return existing;
        }

        int assigned = this.nextPlayerUid++;
        this.playerUidMap.put(uuid, assigned);
        this.playerNameMap.put(uuid, playerName);
        this.setDirty();
        return assigned;
    }

    public int getOrCreateShipUid(UUID shipUuid) {
        Integer existing = this.shipUidMap.get(shipUuid);
        if (existing != null) {
            return existing;
        }

        int assigned = this.nextShipUid++;
        this.shipUidMap.put(shipUuid, assigned);
        this.setDirty();
        return assigned;
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        tag.putInt(NEXT_PLAYER_UID_TAG, this.nextPlayerUid);
        tag.putInt(NEXT_SHIP_UID_TAG, this.nextShipUid);

        ListTag players = new ListTag();
        for (Map.Entry<UUID, Integer> entry : this.playerUidMap.entrySet()) {
            CompoundTag playerTag = new CompoundTag();
            playerTag.putUUID(PLAYER_UUID_TAG, entry.getKey());
            playerTag.putInt(PLAYER_UID_TAG, entry.getValue());
            playerTag.putString(PLAYER_NAME_TAG, this.playerNameMap.getOrDefault(entry.getKey(), ""));
            players.add(playerTag);
        }

        tag.put(PLAYERS_TAG, players);

        ListTag ships = new ListTag();
        for (Map.Entry<UUID, Integer> entry : this.shipUidMap.entrySet()) {
            CompoundTag shipTag = new CompoundTag();
            shipTag.putUUID(SHIP_UUID_TAG, entry.getKey());
            shipTag.putInt(SHIP_UID_TAG, entry.getValue());
            ships.add(shipTag);
        }
        tag.put(SHIPS_TAG, ships);
        return tag;
    }
}
