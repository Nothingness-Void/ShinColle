package com.lulan.shincolle.playerskill;

import com.lulan.shincolle.morph.MorphHostMode;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntArrayTag;
import net.minecraft.util.Mth;

import java.util.Arrays;

public class PlayerSkillRuntimeState {

    public static final int SLOT_COUNT = 5;

    private static final String VISIBLE_TAG = "Visible";
    private static final String HOST_MODE_TAG = "HostMode";
    private static final String SLOT_ENABLED_MASK_TAG = "SlotEnabledMask";
    private static final String SLOT_COOLDOWNS_TAG = "SlotCooldowns";
    private static final String SLOT_MAX_COOLDOWNS_TAG = "SlotMaxCooldowns";
    private static final String HOST_SHIP_UID_TAG = "HostShipUid";
    private static final String HOST_CLASS_ID_TAG = "HostClassId";

    private final int[] slotCooldowns = new int[SLOT_COUNT];
    private final int[] slotMaxCooldowns = new int[SLOT_COUNT];
    private boolean visible;
    private MorphHostMode hostMode = MorphHostMode.NONE;
    private int slotEnabledMask;
    private int hostShipUid;
    private int hostClassId;

    public CompoundTag saveToTag(CompoundTag tag) {
        tag.putBoolean(VISIBLE_TAG, this.visible);
        tag.putInt(HOST_MODE_TAG, this.hostMode.ordinal());
        tag.putInt(SLOT_ENABLED_MASK_TAG, this.slotEnabledMask);
        tag.put(SLOT_COOLDOWNS_TAG, new IntArrayTag(Arrays.copyOf(this.slotCooldowns, this.slotCooldowns.length)));
        tag.put(SLOT_MAX_COOLDOWNS_TAG, new IntArrayTag(Arrays.copyOf(this.slotMaxCooldowns, this.slotMaxCooldowns.length)));
        tag.putInt(HOST_SHIP_UID_TAG, this.hostShipUid);
        tag.putInt(HOST_CLASS_ID_TAG, this.hostClassId);
        return tag;
    }

    public void loadFromTag(CompoundTag tag) {
        this.visible = tag.getBoolean(VISIBLE_TAG);
        this.hostMode = MorphHostMode.fromOrdinal(tag.getInt(HOST_MODE_TAG));
        this.slotEnabledMask = tag.getInt(SLOT_ENABLED_MASK_TAG);
        Arrays.fill(this.slotCooldowns, 0);
        Arrays.fill(this.slotMaxCooldowns, 0);
        int[] storedCooldowns = tag.getIntArray(SLOT_COOLDOWNS_TAG);
        int[] storedMaxCooldowns = tag.getIntArray(SLOT_MAX_COOLDOWNS_TAG);
        System.arraycopy(storedCooldowns, 0, this.slotCooldowns, 0, Math.min(SLOT_COUNT, storedCooldowns.length));
        System.arraycopy(storedMaxCooldowns, 0, this.slotMaxCooldowns, 0, Math.min(SLOT_COUNT, storedMaxCooldowns.length));
        this.hostShipUid = Math.max(0, tag.getInt(HOST_SHIP_UID_TAG));
        this.hostClassId = Math.max(0, tag.getInt(HOST_CLASS_ID_TAG));
    }

    public void copyFrom(PlayerSkillRuntimeState other) {
        this.loadFromTag(other.saveToTag(new CompoundTag()));
    }

    public void clear() {
        this.visible = false;
        this.hostMode = MorphHostMode.NONE;
        this.slotEnabledMask = 0;
        this.hostShipUid = 0;
        this.hostClassId = 0;
        Arrays.fill(this.slotCooldowns, 0);
        Arrays.fill(this.slotMaxCooldowns, 0);
    }

    public boolean sameAs(PlayerSkillRuntimeState other) {
        if (other == null) {
            return false;
        }
        return this.visible == other.visible
                && this.hostMode == other.hostMode
                && this.slotEnabledMask == other.slotEnabledMask
                && this.hostShipUid == other.hostShipUid
                && this.hostClassId == other.hostClassId
                && Arrays.equals(this.slotCooldowns, other.slotCooldowns)
                && Arrays.equals(this.slotMaxCooldowns, other.slotMaxCooldowns);
    }

    public boolean isVisible() {
        return this.visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public MorphHostMode getHostMode() {
        return this.hostMode;
    }

    public void setHostMode(MorphHostMode hostMode) {
        this.hostMode = hostMode == null ? MorphHostMode.NONE : hostMode;
    }

    public int getSlotEnabledMask() {
        return this.slotEnabledMask;
    }

    public void setSlotEnabledMask(int slotEnabledMask) {
        this.slotEnabledMask = slotEnabledMask & ((1 << SLOT_COUNT) - 1);
    }

    public boolean isSlotEnabled(int slot) {
        if (slot < 0 || slot >= SLOT_COUNT) {
            return false;
        }
        return (this.slotEnabledMask & (1 << slot)) != 0;
    }

    public void setSlotEnabled(int slot, boolean enabled) {
        if (slot < 0 || slot >= SLOT_COUNT) {
            return;
        }
        if (enabled) {
            this.slotEnabledMask |= (1 << slot);
        } else {
            this.slotEnabledMask &= ~(1 << slot);
        }
    }

    public int getSlotCooldown(int slot) {
        if (slot < 0 || slot >= SLOT_COUNT) {
            return 0;
        }
        return this.slotCooldowns[slot];
    }

    public void setSlotCooldown(int slot, int ticks) {
        if (slot < 0 || slot >= SLOT_COUNT) {
            return;
        }
        this.slotCooldowns[slot] = Mth.clamp(ticks, 0, 20 * 60);
    }

    public int[] getSlotCooldownsCopy() {
        return Arrays.copyOf(this.slotCooldowns, this.slotCooldowns.length);
    }

    public int getSlotMaxCooldown(int slot) {
        if (slot < 0 || slot >= SLOT_COUNT) {
            return 0;
        }
        return this.slotMaxCooldowns[slot];
    }

    public void setSlotMaxCooldown(int slot, int ticks) {
        if (slot < 0 || slot >= SLOT_COUNT) {
            return;
        }
        this.slotMaxCooldowns[slot] = Mth.clamp(ticks, 0, 20 * 60);
    }

    public int[] getSlotMaxCooldownsCopy() {
        return Arrays.copyOf(this.slotMaxCooldowns, this.slotMaxCooldowns.length);
    }

    public int getHostShipUid() {
        return this.hostShipUid;
    }

    public void setHostShipUid(int hostShipUid) {
        this.hostShipUid = Math.max(0, hostShipUid);
    }

    public int getHostClassId() {
        return this.hostClassId;
    }

    public void setHostClassId(int hostClassId) {
        this.hostClassId = Math.max(0, hostClassId);
    }
}
