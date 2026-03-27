package com.lulan.shincolle.teitoku;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;

import java.util.ArrayList;
import java.util.List;

public class TeitokuData {

    private static final String PLAYER_NAME_TAG = "PlayerName";
    private static final String PLAYER_UID_TAG = "PlayerUID";
    private static final String HAS_RING_TAG = "HasRing";
    private static final String RING_ACTIVE_TAG = "RingOn";
    private static final String RING_FLYING_TAG = "RingFly";
    private static final String MARRIAGE_NUM_TAG = "MarriageNum";
    private static final String BOSS_COOLDOWN_TAG = "BossCD";
    private static final String TEAM_COOLDOWN_TAG = "TeamCD";
    private static final String COLLECTED_SHIPS_TAG = "ColleShip";
    private static final String COLLECTED_EQUIPMENT_TAG = "ColleEquip";
    private static final String TARGET_CLASSES_TAG = "CustomTargetClass";

    public static final int DEFAULT_BOSS_COOLDOWN = 4800;
    public static final int DEFAULT_TEAM_COOLDOWN = 6000;

    private String playerName = "";
    private int playerUid = -1;
    private boolean hasRing;
    private boolean ringActive;
    private boolean ringFlying;
    private int marriageNum;
    private int bossCooldown = DEFAULT_BOSS_COOLDOWN;
    private int teamCooldown = DEFAULT_TEAM_COOLDOWN;
    private final List<Integer> collectedShips = new ArrayList<>();
    private final List<Integer> collectedEquipment = new ArrayList<>();
    private final List<String> targetClasses = new ArrayList<>();

    public CompoundTag saveToTag(CompoundTag tag) {
        tag.putString(PLAYER_NAME_TAG, this.playerName);
        tag.putInt(PLAYER_UID_TAG, this.playerUid);
        tag.putBoolean(HAS_RING_TAG, this.hasRing);
        tag.putBoolean(RING_ACTIVE_TAG, this.ringActive);
        tag.putBoolean(RING_FLYING_TAG, this.ringFlying);
        tag.putInt(MARRIAGE_NUM_TAG, this.marriageNum);
        tag.putInt(BOSS_COOLDOWN_TAG, this.bossCooldown);
        tag.putInt(TEAM_COOLDOWN_TAG, this.teamCooldown);
        tag.putIntArray(COLLECTED_SHIPS_TAG, this.collectedShips);
        tag.putIntArray(COLLECTED_EQUIPMENT_TAG, this.collectedEquipment);

        ListTag targetClassList = new ListTag();
        for (String targetClass : this.targetClasses) {
            targetClassList.add(StringTag.valueOf(targetClass));
        }
        tag.put(TARGET_CLASSES_TAG, targetClassList);
        return tag;
    }

    public void loadFromTag(CompoundTag tag) {
        this.playerName = tag.getString(PLAYER_NAME_TAG);
        this.playerUid = tag.contains(PLAYER_UID_TAG) ? tag.getInt(PLAYER_UID_TAG) : -1;
        this.hasRing = tag.getBoolean(HAS_RING_TAG);
        this.ringActive = tag.getBoolean(RING_ACTIVE_TAG);
        this.ringFlying = tag.getBoolean(RING_FLYING_TAG);
        this.marriageNum = Math.max(0, tag.getInt(MARRIAGE_NUM_TAG));
        this.bossCooldown = tag.contains(BOSS_COOLDOWN_TAG) ? tag.getInt(BOSS_COOLDOWN_TAG) : DEFAULT_BOSS_COOLDOWN;
        this.teamCooldown = tag.contains(TEAM_COOLDOWN_TAG) ? tag.getInt(TEAM_COOLDOWN_TAG) : DEFAULT_TEAM_COOLDOWN;

        this.collectedShips.clear();
        for (int shipId : tag.getIntArray(COLLECTED_SHIPS_TAG)) {
            this.addCollectedShip(shipId);
        }

        this.collectedEquipment.clear();
        for (int equipmentId : tag.getIntArray(COLLECTED_EQUIPMENT_TAG)) {
            this.addCollectedEquipment(equipmentId);
        }

        this.targetClasses.clear();
        ListTag targetClassList = tag.getList(TARGET_CLASSES_TAG, Tag.TAG_STRING);
        for (int index = 0; index < targetClassList.size(); index++) {
            String targetClass = targetClassList.getString(index);
            if (!targetClass.isBlank() && !this.targetClasses.contains(targetClass)) {
                this.targetClasses.add(targetClass);
            }
        }
    }

    public void copyFrom(TeitokuData other) {
        this.loadFromTag(other.saveToTag(new CompoundTag()));
    }

    public String getPlayerName() {
        return this.playerName;
    }

    public void setPlayerName(String playerName) {
        this.playerName = playerName == null ? "" : playerName;
    }

    public int getPlayerUid() {
        return this.playerUid;
    }

    public void setPlayerUid(int playerUid) {
        this.playerUid = playerUid;
    }

    public boolean hasRing() {
        return this.hasRing;
    }

    public void setHasRing(boolean hasRing) {
        this.hasRing = hasRing;
    }

    public boolean isRingActive() {
        return this.ringActive;
    }

    public void setRingActive(boolean ringActive) {
        this.ringActive = ringActive;
    }

    public boolean isRingFlying() {
        return this.ringFlying;
    }

    public void setRingFlying(boolean ringFlying) {
        this.ringFlying = ringFlying;
    }

    public int getMarriageNum() {
        return this.marriageNum;
    }

    public void setMarriageNum(int marriageNum) {
        this.marriageNum = Math.max(0, marriageNum);
    }

    public void incrementMarriageNum() {
        this.marriageNum++;
    }

    public int getBossCooldown() {
        return this.bossCooldown;
    }

    public void setBossCooldown(int bossCooldown) {
        this.bossCooldown = Math.max(0, bossCooldown);
    }

    public int getTeamCooldown() {
        return this.teamCooldown;
    }

    public void setTeamCooldown(int teamCooldown) {
        this.teamCooldown = Math.max(0, teamCooldown);
    }

    public List<Integer> getCollectedShips() {
        return List.copyOf(this.collectedShips);
    }

    public void addCollectedShip(int shipId) {
        if (shipId > 0 && !this.collectedShips.contains(shipId)) {
            this.collectedShips.add(shipId);
        }
    }

    public List<Integer> getCollectedEquipment() {
        return List.copyOf(this.collectedEquipment);
    }

    public void addCollectedEquipment(int equipmentId) {
        if (equipmentId >= 0 && !this.collectedEquipment.contains(equipmentId)) {
            this.collectedEquipment.add(equipmentId);
        }
    }

    public List<String> getTargetClasses() {
        return List.copyOf(this.targetClasses);
    }

    public void setTargetClasses(List<String> targetClasses) {
        this.targetClasses.clear();
        if (targetClasses == null) {
            return;
        }

        for (String targetClass : targetClasses) {
            if (targetClass != null && !targetClass.isBlank() && !this.targetClasses.contains(targetClass)) {
                this.targetClasses.add(targetClass);
            }
        }
    }
}
