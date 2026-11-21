package com.lulan.shincolle.morph;

import com.lulan.shincolle.entity.ship.LegacyShipAttackKind;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntArrayTag;
import net.minecraft.util.Mth;

import java.util.Arrays;

public class MorphRuntimeState {

    public static final int NO_SELECTION = -1;

    private static final String ACTIVE_TAG = "Active";
    private static final String SELECTED_CLASS_ID_TAG = "SelectedClassId";
    private static final String SELECTION_VERSION_TAG = "SelectionVersion";
    private static final String HOST_MODE_TAG = "HostMode";
    private static final String ATTACK_COOLDOWNS_TAG = "AttackCooldowns";
    private static final String SPECIAL_COOLDOWN_TAG = "SpecialCooldown";

    private final int[] attackCooldowns = new int[LegacyShipAttackKind.values().length];
    private boolean active;
    private int selectedClassId = NO_SELECTION;
    private MorphHostMode hostMode = MorphHostMode.NONE;
    private int specialCooldown;
    private int immuneTicks;
    private int emotion;

    public int getEmotion() {
        return this.emotion;
    }

    public void setEmotion(int emotion) {
        this.emotion = emotion;
    }

    public int getImmuneTicks() {
        return this.immuneTicks;
    }

    public void setImmuneTicks(int ticks) {
        this.immuneTicks = ticks;
    }

    public CompoundTag saveToTag(CompoundTag tag) {
        tag.putInt("Emotion", this.emotion);
        tag.putBoolean(ACTIVE_TAG, this.active);
        tag.putInt(SELECTED_CLASS_ID_TAG, this.selectedClassId);
        tag.putInt(SELECTION_VERSION_TAG, 1);
        tag.putInt(HOST_MODE_TAG, this.hostMode.ordinal());
        tag.put(ATTACK_COOLDOWNS_TAG, new IntArrayTag(Arrays.copyOf(this.attackCooldowns, this.attackCooldowns.length)));
        tag.putInt(SPECIAL_COOLDOWN_TAG, this.specialCooldown);
        return tag;
    }

    public void loadFromTag(CompoundTag tag) {
        this.emotion = tag.getInt("Emotion");
        this.active = tag.getBoolean(ACTIVE_TAG);
        this.setSelectedClassId(tag.contains(SELECTED_CLASS_ID_TAG) ? tag.getInt(SELECTED_CLASS_ID_TAG) : NO_SELECTION);
        if (tag.getInt(SELECTION_VERSION_TAG) < 1 && this.selectedClassId == 0) {
            this.selectedClassId = NO_SELECTION;
        }
        this.hostMode = MorphHostMode.fromOrdinal(tag.getInt(HOST_MODE_TAG));
        Arrays.fill(this.attackCooldowns, 0);
        int[] storedCooldowns = tag.getIntArray(ATTACK_COOLDOWNS_TAG);
        System.arraycopy(storedCooldowns, 0, this.attackCooldowns, 0, Math.min(this.attackCooldowns.length, storedCooldowns.length));
        this.specialCooldown = Math.max(0, tag.getInt(SPECIAL_COOLDOWN_TAG));
    }

    public void copyFrom(MorphRuntimeState other) {
        this.loadFromTag(other.saveToTag(new CompoundTag()));
    }

    public boolean tick() {
        boolean changed = false;
        if (this.immuneTicks > 0) {
            this.immuneTicks--;
        }

        for (int i = 0; i < this.attackCooldowns.length; i++) {
            if (this.attackCooldowns[i] > 0) {
                this.attackCooldowns[i]--;
                changed = true;
            }
        }

        if (this.specialCooldown > 0) {
            this.specialCooldown--;
            changed = true;
        }

        return changed;
    }

    public boolean isActive() {
        return this.active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public int getSelectedClassId() {
        return this.selectedClassId;
    }

    public void setSelectedClassId(int selectedClassId) {
        this.selectedClassId = Math.max(NO_SELECTION, selectedClassId);
    }

    public MorphHostMode getHostMode() {
        return this.hostMode;
    }

    public void setHostMode(MorphHostMode hostMode) {
        this.hostMode = hostMode == null ? MorphHostMode.NONE : hostMode;
    }

    public int getAttackCooldown(LegacyShipAttackKind attackKind) {
        return this.attackCooldowns[attackKind.ordinal()];
    }

    public void setAttackCooldown(LegacyShipAttackKind attackKind, int ticks) {
        this.attackCooldowns[attackKind.ordinal()] = Mth.clamp(ticks, 0, 20 * 60);
    }

    public int[] getAttackCooldownsCopy() {
        return Arrays.copyOf(this.attackCooldowns, this.attackCooldowns.length);
    }

    public int getSpecialCooldown() {
        return this.specialCooldown;
    }

    public void setSpecialCooldown(int specialCooldown) {
        this.specialCooldown = Mth.clamp(specialCooldown, 0, 20 * 60);
    }
}
