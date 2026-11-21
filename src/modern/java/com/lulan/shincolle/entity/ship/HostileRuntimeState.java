package com.lulan.shincolle.entity.ship;

import net.minecraft.nbt.CompoundTag;

public class HostileRuntimeState {

    private static final String NATURAL_SPAWN_TAG = "NaturalSpawn";
    private static final String DESPAWN_TICKS_TAG = "DespawnTicks";

    private boolean naturalSpawn;
    private int despawnTicks;

    public CompoundTag saveToTag(CompoundTag tag) {
        tag.putBoolean(NATURAL_SPAWN_TAG, this.naturalSpawn);
        tag.putInt(DESPAWN_TICKS_TAG, this.despawnTicks);
        return tag;
    }

    public void loadFromTag(CompoundTag tag) {
        this.naturalSpawn = tag.getBoolean(NATURAL_SPAWN_TAG);
        this.despawnTicks = Math.max(0, tag.getInt(DESPAWN_TICKS_TAG));
    }

    public void reset(boolean naturalSpawn) {
        this.naturalSpawn = naturalSpawn;
        this.despawnTicks = 0;
    }

    public boolean isNaturalSpawn() {
        return this.naturalSpawn;
    }

    public int getDespawnTicks() {
        return this.despawnTicks;
    }

    public void setDespawnTicks(int despawnTicks) {
        this.despawnTicks = Math.max(0, despawnTicks);
    }

}
