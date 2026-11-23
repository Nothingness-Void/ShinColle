package com.lulan.shincolle.entity.ship;

import net.minecraft.nbt.CompoundTag;

public class HostileRuntimeState {

    private static final String NATURAL_SPAWN_TAG = "NaturalSpawn";
    private static final String DESPAWN_TICKS_TAG = "DespawnTicks";

    private boolean naturalSpawn;
    private int despawnTicks;
    private boolean persistenceRequired;

    public CompoundTag saveToTag(CompoundTag tag) {
        tag.putBoolean(NATURAL_SPAWN_TAG, this.naturalSpawn);
        tag.putInt(DESPAWN_TICKS_TAG, this.despawnTicks);
        tag.putBoolean("PersistenceRequired", this.persistenceRequired);
        return tag;
    }

    public void loadFromTag(CompoundTag tag) {
        this.naturalSpawn = tag.getBoolean(NATURAL_SPAWN_TAG);
        this.despawnTicks = 0;
        this.persistenceRequired = tag.getBoolean("PersistenceRequired");
    }

    public void reset(boolean naturalSpawn) {
        this.naturalSpawn = naturalSpawn;
        this.despawnTicks = 0;
        this.persistenceRequired = false;
    }

    public boolean isNaturalSpawn() {
        return this.naturalSpawn;
    }

    public boolean isPersistenceRequired() {
        return this.persistenceRequired;
    }

    public void setPersistenceRequired(boolean persistenceRequired) {
        this.persistenceRequired = persistenceRequired;
    }

    public int getDespawnTicks() {
        return this.despawnTicks;
    }

    public void setDespawnTicks(int despawnTicks) {
        this.despawnTicks = Math.max(0, despawnTicks);
    }

}
