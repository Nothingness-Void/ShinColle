package com.lulan.shincolle.entity.ship;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;

public class HostileRuntimeState {

    private static final String NATURAL_SPAWN_TAG = "NaturalSpawn";
    private static final String ELITE_TAG = "Elite";
    private static final String BOSS_TAG = "Boss";
    private static final String INSTALLATION_BOSS_TAG = "InstallationBoss";
    private static final String PHASE_TAG = "BossPhase";
    private static final String ACTION_COOLDOWN_TAG = "ActionCooldown";
    private static final String SUMMON_COOLDOWN_TAG = "SummonCooldown";
    private static final String DESPAWN_TICKS_TAG = "DespawnTicks";
    private static final String ENGAGED_TICKS_TAG = "EngagedTicks";

    private boolean naturalSpawn;
    private boolean elite;
    private boolean boss;
    private boolean installationBoss;
    private int phase;
    private int actionCooldown;
    private int summonCooldown;
    private int despawnTicks;
    private int engagedTicks;

    public CompoundTag saveToTag(CompoundTag tag) {
        tag.putBoolean(NATURAL_SPAWN_TAG, this.naturalSpawn);
        tag.putBoolean(ELITE_TAG, this.elite);
        tag.putBoolean(BOSS_TAG, this.boss);
        tag.putBoolean(INSTALLATION_BOSS_TAG, this.installationBoss);
        tag.putInt(PHASE_TAG, this.phase);
        tag.putInt(ACTION_COOLDOWN_TAG, this.actionCooldown);
        tag.putInt(SUMMON_COOLDOWN_TAG, this.summonCooldown);
        tag.putInt(DESPAWN_TICKS_TAG, this.despawnTicks);
        tag.putInt(ENGAGED_TICKS_TAG, this.engagedTicks);
        return tag;
    }

    public void loadFromTag(CompoundTag tag) {
        this.naturalSpawn = tag.getBoolean(NATURAL_SPAWN_TAG);
        this.elite = tag.getBoolean(ELITE_TAG);
        this.boss = tag.getBoolean(BOSS_TAG);
        this.installationBoss = tag.getBoolean(INSTALLATION_BOSS_TAG);
        this.phase = Mth.clamp(tag.getInt(PHASE_TAG), 0, 3);
        this.actionCooldown = Math.max(0, tag.getInt(ACTION_COOLDOWN_TAG));
        this.summonCooldown = Math.max(0, tag.getInt(SUMMON_COOLDOWN_TAG));
        this.despawnTicks = Math.max(0, tag.getInt(DESPAWN_TICKS_TAG));
        this.engagedTicks = Math.max(0, tag.getInt(ENGAGED_TICKS_TAG));
    }

    public void reset(boolean naturalSpawn, boolean elite, boolean boss, boolean installationBoss) {
        this.naturalSpawn = naturalSpawn;
        this.elite = elite;
        this.boss = boss;
        this.installationBoss = installationBoss;
        this.phase = 0;
        this.actionCooldown = 0;
        this.summonCooldown = 0;
        this.despawnTicks = 0;
        this.engagedTicks = 0;
    }

    public boolean tick() {
        boolean changed = false;
        if (this.actionCooldown > 0) {
            this.actionCooldown--;
            changed = true;
        }
        if (this.summonCooldown > 0) {
            this.summonCooldown--;
            changed = true;
        }
        return changed;
    }

    public boolean isNaturalSpawn() {
        return this.naturalSpawn;
    }

    public boolean isElite() {
        return this.elite;
    }

    public boolean isBoss() {
        return this.boss;
    }

    public boolean isInstallationBoss() {
        return this.installationBoss;
    }

    public int getPhase() {
        return this.phase;
    }

    public void setPhase(int phase) {
        this.phase = Mth.clamp(phase, 0, 3);
    }

    public int getActionCooldown() {
        return this.actionCooldown;
    }

    public void setActionCooldown(int actionCooldown) {
        this.actionCooldown = Math.max(0, actionCooldown);
    }

    public int getSummonCooldown() {
        return this.summonCooldown;
    }

    public void setSummonCooldown(int summonCooldown) {
        this.summonCooldown = Math.max(0, summonCooldown);
    }

    public int getDespawnTicks() {
        return this.despawnTicks;
    }

    public void setDespawnTicks(int despawnTicks) {
        this.despawnTicks = Math.max(0, despawnTicks);
    }

    public int getEngagedTicks() {
        return this.engagedTicks;
    }

    public void setEngagedTicks(int engagedTicks) {
        this.engagedTicks = Math.max(0, engagedTicks);
    }
}
