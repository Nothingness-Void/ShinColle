package com.lulan.shincolle.item;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.item.ItemEntity;

/**
 * Marks item entities that 1.12.2 spawned as {@code BasicEntityItem} (ship eggs, boss eggs,
 * heavy grudge block drops) so {@code /shipcleardrop} keeps the legacy target set.
 */
public final class LegacyEntityItemMarker {

    private static final String TAG = "shincolle:BasicEntityItem";

    private LegacyEntityItemMarker() {
    }

    public static ItemEntity mark(ItemEntity itemEntity) {
        itemEntity.getPersistentData().putBoolean(TAG, true);
        return itemEntity;
    }

    public static boolean isMarked(ItemEntity itemEntity) {
        return itemEntity.getPersistentData().getBoolean(TAG);
    }

    public static void importForgeData(ItemEntity itemEntity, CompoundTag forgeData) {
        CompoundTag persistentData = itemEntity.getPersistentData();
        if (!persistentData.contains(TAG) && forgeData.contains(TAG, Tag.TAG_BYTE)) {
            persistentData.putBoolean(TAG, forgeData.getBoolean(TAG));
        }
    }
}
