package com.lulan.shincolle.combat;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class WorldCombatRulesSavedData extends SavedData {

    private static final String DATA_NAME = "shincolle_world_combat_rules";
    private static final String UNATTACKABLE_CLASSES_TAG = "UnattackableClasses";

    private final Set<String> unattackableClasses = new LinkedHashSet<>();

    public static WorldCombatRulesSavedData get(ServerLevel level) {
        return level.getServer().overworld().getDataStorage().computeIfAbsent(
                WorldCombatRulesSavedData::load,
                WorldCombatRulesSavedData::new,
                DATA_NAME);
    }

    public static WorldCombatRulesSavedData load(CompoundTag tag) {
        WorldCombatRulesSavedData data = new WorldCombatRulesSavedData();
        ListTag list = tag.getList(UNATTACKABLE_CLASSES_TAG, Tag.TAG_STRING);
        for (int index = 0; index < list.size(); index++) {
            String normalized = normalize(list.getString(index));
            if (!normalized.isBlank()) {
                data.unattackableClasses.add(normalized);
            }
        }
        return data;
    }

    public boolean toggleUnattackable(String targetClass) {
        String normalized = normalize(targetClass);
        if (normalized.isBlank()) {
            return false;
        }

        boolean added;
        if (this.unattackableClasses.contains(normalized)) {
            this.unattackableClasses.remove(normalized);
            added = false;
        } else {
            this.unattackableClasses.add(normalized);
            added = true;
        }
        this.setDirty();
        return added;
    }

    public boolean isUnattackable(String targetClass) {
        String normalized = normalize(targetClass);
        return !normalized.isBlank() && this.unattackableClasses.contains(normalized);
    }

    public Collection<String> getAllUnattackableClasses() {
        return List.copyOf(this.unattackableClasses);
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        ListTag list = new ListTag();
        for (String targetClass : this.unattackableClasses) {
            list.add(StringTag.valueOf(targetClass));
        }
        tag.put(UNATTACKABLE_CLASSES_TAG, list);
        return tag;
    }

    private static String normalize(String targetClass) {
        return targetClass == null ? "" : targetClass.trim().toLowerCase(Locale.ROOT);
    }
}
