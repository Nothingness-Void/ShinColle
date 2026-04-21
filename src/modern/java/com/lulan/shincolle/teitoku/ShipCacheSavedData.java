package com.lulan.shincolle.teitoku;

import com.lulan.shincolle.entity.ship.LegacyShipEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ShipCacheSavedData extends SavedData {

    private static final String DATA_NAME = "shincolle_ship_world_cache";
    private static final String SHIPS_TAG = "Ships";

    private final Map<Integer, ShipWorldCacheEntry> shipByUid = new HashMap<>();

    public static ShipCacheSavedData get(ServerLevel level) {
        return level.getServer().overworld().getDataStorage().computeIfAbsent(
                ShipCacheSavedData::load,
                ShipCacheSavedData::new,
                DATA_NAME);
    }

    public static ShipCacheSavedData load(CompoundTag tag) {
        ShipCacheSavedData data = new ShipCacheSavedData();
        ListTag list = tag.getList(SHIPS_TAG, Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            ShipWorldCacheEntry entry = ShipWorldCacheEntry.load(list.getCompound(i));
            if (entry.shipUid() > 0) {
                data.shipByUid.put(entry.shipUid(), entry);
            }
        }
        return data;
    }

    public void updateFromShip(LegacyShipEntity ship, boolean dead) {
        this.updateFromShip(ship, dead, !dead);
    }

    public void updateFromShip(LegacyShipEntity ship, boolean dead, boolean online) {
        if (ship == null || ship.getShipUid() <= 0) {
            return;
        }

        this.shipByUid.put(ship.getShipUid(), ShipWorldCacheEntry.fromShip(ship, dead, online));
        this.setDirty();
    }

    public @Nullable ShipWorldCacheEntry getShip(int shipUid) {
        return this.shipByUid.get(shipUid);
    }

    public List<ShipWorldCacheEntry> getShipsOwnedBy(int ownerUid) {
        List<ShipWorldCacheEntry> entries = new ArrayList<>();
        if (ownerUid <= 0) {
            return entries;
        }

        for (ShipWorldCacheEntry entry : this.shipByUid.values()) {
            if (entry.ownerUid() == ownerUid) {
                entries.add(entry);
            }
        }
        entries.sort(java.util.Comparator.comparingInt(ShipWorldCacheEntry::shipUid));
        return entries;
    }

    public Collection<ShipWorldCacheEntry> getAllShips() {
        return List.copyOf(this.shipByUid.values());
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        ListTag list = new ListTag();
        for (ShipWorldCacheEntry entry : this.shipByUid.values()) {
            if (entry.shipUid() <= 0) {
                continue;
            }
            list.add(entry.saveToTag(new CompoundTag()));
        }
        tag.put(SHIPS_TAG, list);
        return tag;
    }
}
