package com.lulan.shincolle.teitoku;

import com.lulan.shincolle.morph.MorphProfile;
import com.lulan.shincolle.morph.MorphRuntimeState;
import com.lulan.shincolle.playerskill.PlayerSkillRuntimeState;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.util.Mth;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class TeitokuData {

    private static final String PLAYER_NAME_TAG = "PlayerName";
    private static final String LEGACY_CAPABILITY_TAG = "TeitokuExtProps";
    private static final String PLAYER_UID_TAG = "PlayerUID";
    private static final String HAS_RING_TAG = "HasRing";
    private static final String LEGACY_HAS_RING_TAG = "hasRing";
    private static final String RING_ACTIVE_TAG = "RingOn";
    private static final String RING_FLYING_TAG = "RingFly";
    private static final String MARRIAGE_NUM_TAG = "MarriageNum";
    private static final String BOSS_COOLDOWN_TAG = "BossCD";
    private static final String TEAM_COOLDOWN_TAG = "TeamCD";
    private static final String HAS_TEAM_TAG = "HasTeam";
    private static final String FORMATION_ID_TAG = "FormationId";
    private static final String COLLECTED_SHIPS_TAG = "ColleShip";
    private static final String COLLECTED_EQUIPMENT_TAG = "ColleEquip";
    private static final String TARGET_CLASSES_TAG = "CustomTargetClass";
    private static final String CURRENT_TEAM_ID_TAG = "CurrentTeamId";
    private static final String FORMATION_IDS_TAG = "FormationIds";
    private static final String LEGACY_FORMAT_IDS_TAG = "FormatID";
    private static final String TEAM_SHIP_UIDS_TAG = "TeamShipUids";
    private static final String TEAM_SHIP_SELECTED_TAG = "TeamShipSelected";
    private static final String TEAM_NAMES_TAG = "TeamNames";
    private static final String LEGACY_TEAM_LIST_PREFIX = "TeamList";
    private static final String LEGACY_SELECT_STATE_PREFIX = "SelectState";
    private static final String LEGACY_TEAM_NAME_PREFIX = "uname";
    private static final String MORPH_PROFILES_TAG = "MorphProfiles";
    private static final String MORPH_RUNTIME_TAG = "MorphRuntime";
    private static final String PLAYER_SKILL_RUNTIME_TAG = "PlayerSkillRuntime";

    public static final int DEFAULT_BOSS_COOLDOWN = 4800;
    public static final int DEFAULT_TEAM_COOLDOWN = 6000;
    public static final int DEFAULT_FORMATION_ID = 0;
    public static final int MAX_FORMATION_ID = 5;
    public static final int TEAM_COUNT = 9;
    public static final int TEAM_SIZE = 6;
    public static final int TEAM_SLOT_COUNT = TEAM_COUNT * TEAM_SIZE;

    private String playerName = "";
    private int playerUid = -1;
    private boolean hasRing;
    private boolean ringActive;
    private boolean ringFlying;
    private int marriageNum;
    private int bossCooldown = DEFAULT_BOSS_COOLDOWN;
    private int teamCooldown = DEFAULT_TEAM_COOLDOWN;
    private boolean hasTeam;
    private int currentTeamId;
    private int pointerSlotCursor;
    private final int[] formationIds = new int[TEAM_COUNT];
    private final int[] teamShipUids = new int[TEAM_SLOT_COUNT];
    private final boolean[] teamShipSelected = new boolean[TEAM_SLOT_COUNT];
    private final String[] teamNames = new String[TEAM_COUNT];
    private final List<Integer> collectedShips = new ArrayList<>();
    private final List<Integer> collectedEquipment = new ArrayList<>();
    private final List<String> targetClasses = new ArrayList<>();
    private final List<MorphProfile> morphProfiles = new ArrayList<>();
    private final MorphRuntimeState morphRuntimeState = new MorphRuntimeState();
    private final PlayerSkillRuntimeState playerSkillRuntimeState = new PlayerSkillRuntimeState();

    public TeitokuData() {
        Arrays.fill(this.formationIds, DEFAULT_FORMATION_ID);
        Arrays.fill(this.teamShipUids, -1);
        Arrays.fill(this.teamNames, "");
        this.morphRuntimeState.setSelectedClassId(0);
    }

    public CompoundTag saveToTag(CompoundTag tag) {
        tag.putString(PLAYER_NAME_TAG, this.playerName);
        tag.putInt(PLAYER_UID_TAG, this.playerUid);
        tag.putBoolean(HAS_RING_TAG, this.hasRing);
        tag.putBoolean(RING_ACTIVE_TAG, this.ringActive);
        tag.putBoolean(RING_FLYING_TAG, this.ringFlying);
        tag.putInt(MARRIAGE_NUM_TAG, this.marriageNum);
        tag.putInt(BOSS_COOLDOWN_TAG, this.bossCooldown);
        tag.putInt(TEAM_COOLDOWN_TAG, this.teamCooldown);
        tag.putBoolean(HAS_TEAM_TAG, this.hasTeam);
        tag.putInt(FORMATION_ID_TAG, this.getCurrentFormationId());
        tag.putIntArray(FORMATION_IDS_TAG, Arrays.copyOf(this.formationIds, this.formationIds.length));
        tag.putInt(CURRENT_TEAM_ID_TAG, this.currentTeamId);
        tag.putIntArray(TEAM_SHIP_UIDS_TAG, Arrays.copyOf(this.teamShipUids, this.teamShipUids.length));
        tag.putByteArray(TEAM_SHIP_SELECTED_TAG, toByteArray(this.teamShipSelected));
        tag.putIntArray(COLLECTED_SHIPS_TAG, this.collectedShips);
        tag.putIntArray(COLLECTED_EQUIPMENT_TAG, this.collectedEquipment);

        ListTag teamNameList = new ListTag();
        for (String teamName : this.teamNames) {
            teamNameList.add(StringTag.valueOf(teamName == null ? "" : teamName));
        }
        tag.put(TEAM_NAMES_TAG, teamNameList);

        ListTag targetClassList = new ListTag();
        for (String targetClass : this.targetClasses) {
            targetClassList.add(StringTag.valueOf(targetClass));
        }
        tag.put(TARGET_CLASSES_TAG, targetClassList);

        ListTag morphProfileList = new ListTag();
        for (MorphProfile morphProfile : this.morphProfiles) {
            morphProfileList.add(morphProfile.saveToTag(new CompoundTag()));
        }
        tag.put(MORPH_PROFILES_TAG, morphProfileList);
        tag.put(MORPH_RUNTIME_TAG, this.morphRuntimeState.saveToTag(new CompoundTag()));
        tag.put(PLAYER_SKILL_RUNTIME_TAG, this.playerSkillRuntimeState.saveToTag(new CompoundTag()));
        return tag;
    }

    public void loadFromTag(CompoundTag tag) {
        CompoundTag source = unwrapLegacyCapabilityTag(tag);
        this.playerName = source.getString(PLAYER_NAME_TAG);
        this.playerUid = source.contains(PLAYER_UID_TAG) ? source.getInt(PLAYER_UID_TAG) : -1;
        this.hasRing = source.contains(HAS_RING_TAG) ? source.getBoolean(HAS_RING_TAG) : source.getBoolean(LEGACY_HAS_RING_TAG);
        this.ringActive = source.getBoolean(RING_ACTIVE_TAG);
        this.ringFlying = source.getBoolean(RING_FLYING_TAG);
        this.marriageNum = Math.max(0, source.getInt(MARRIAGE_NUM_TAG));
        this.bossCooldown = source.contains(BOSS_COOLDOWN_TAG) ? source.getInt(BOSS_COOLDOWN_TAG) : DEFAULT_BOSS_COOLDOWN;
        this.teamCooldown = source.contains(TEAM_COOLDOWN_TAG) ? source.getInt(TEAM_COOLDOWN_TAG) : DEFAULT_TEAM_COOLDOWN;
        this.hasTeam = source.contains(HAS_TEAM_TAG) ? source.getBoolean(HAS_TEAM_TAG) : this.playerUid > 0;
        this.currentTeamId = normalizeTeamId(source.getInt(CURRENT_TEAM_ID_TAG));

        Arrays.fill(this.formationIds, DEFAULT_FORMATION_ID);
        int[] loadedFormationIds = source.getIntArray(FORMATION_IDS_TAG);
        if (loadedFormationIds.length == 0) {
            loadedFormationIds = source.getIntArray(LEGACY_FORMAT_IDS_TAG);
        }
        if (loadedFormationIds.length > 0) {
            for (int i = 0; i < TEAM_COUNT && i < loadedFormationIds.length; i++) {
                this.formationIds[i] = normalizeFormationId(loadedFormationIds[i]);
            }
        } else {
            int fallbackFormationId = source.contains(FORMATION_ID_TAG)
                    ? normalizeFormationId(source.getInt(FORMATION_ID_TAG))
                    : DEFAULT_FORMATION_ID;
            this.formationIds[this.currentTeamId] = fallbackFormationId;
        }

        Arrays.fill(this.teamShipUids, -1);
        int[] loadedShipUids = source.getIntArray(TEAM_SHIP_UIDS_TAG);
        if (loadedShipUids.length > 0) {
            for (int i = 0; i < TEAM_SLOT_COUNT && i < loadedShipUids.length; i++) {
                this.teamShipUids[i] = loadedShipUids[i] > 0 ? loadedShipUids[i] : -1;
            }
        } else {
            for (int teamId = 0; teamId < TEAM_COUNT; teamId++) {
                int[] legacyTeam = source.getIntArray(LEGACY_TEAM_LIST_PREFIX + teamId);
                for (int slot = 0; slot < TEAM_SIZE && slot < legacyTeam.length; slot++) {
                    this.teamShipUids[slotIndex(teamId, slot)] = legacyTeam[slot] > 0 ? legacyTeam[slot] : -1;
                }
            }
        }

        Arrays.fill(this.teamShipSelected, false);
        byte[] selected = source.getByteArray(TEAM_SHIP_SELECTED_TAG);
        if (selected.length > 0) {
            for (int i = 0; i < TEAM_SLOT_COUNT && i < selected.length; i++) {
                this.teamShipSelected[i] = selected[i] != 0;
            }
        } else {
            for (int teamId = 0; teamId < TEAM_COUNT; teamId++) {
                byte[] legacySelected = source.getByteArray(LEGACY_SELECT_STATE_PREFIX + teamId);
                for (int slot = 0; slot < TEAM_SIZE && slot < legacySelected.length; slot++) {
                    int index = slotIndex(teamId, slot);
                    this.teamShipSelected[index] = legacySelected[slot] != 0 && this.teamShipUids[index] > 0;
                }
            }
        }

        Arrays.fill(this.teamNames, "");
        ListTag teamNameList = source.getList(TEAM_NAMES_TAG, Tag.TAG_STRING);
        if (!teamNameList.isEmpty()) {
            for (int i = 0; i < TEAM_COUNT && i < teamNameList.size(); i++) {
                this.teamNames[i] = teamNameList.getString(i);
            }
        } else {
            for (int i = 0; i < TEAM_COUNT; i++) {
                this.teamNames[i] = source.getString(LEGACY_TEAM_NAME_PREFIX + i).trim();
            }
        }

        this.collectedShips.clear();
        for (int shipId : source.getIntArray(COLLECTED_SHIPS_TAG)) {
            this.addCollectedShip(shipId);
        }

        this.collectedEquipment.clear();
        for (int equipmentId : source.getIntArray(COLLECTED_EQUIPMENT_TAG)) {
            this.addCollectedEquipment(equipmentId);
        }

        this.targetClasses.clear();
        ListTag targetClassList = source.getList(TARGET_CLASSES_TAG, Tag.TAG_STRING);
        for (int index = 0; index < targetClassList.size(); index++) {
            String targetClass = targetClassList.getString(index);
            if (!targetClass.isBlank() && !this.targetClasses.contains(targetClass)) {
                this.targetClasses.add(targetClass);
            }
        }

        this.morphProfiles.clear();
        ListTag morphProfileList = source.getList(MORPH_PROFILES_TAG, Tag.TAG_COMPOUND);
        for (int index = 0; index < morphProfileList.size(); index++) {
            MorphProfile morphProfile = new MorphProfile();
            morphProfile.setDirtyCallback(() -> {
            });
            morphProfile.loadFromTag(morphProfileList.getCompound(index));
            morphProfile.setDirtyCallback(() -> {
            });
            if (morphProfile.getLegacyClassId() > 0) {
                this.morphProfiles.add(morphProfile);
            }
        }

        if (source.contains(MORPH_RUNTIME_TAG, Tag.TAG_COMPOUND)) {
            this.morphRuntimeState.loadFromTag(source.getCompound(MORPH_RUNTIME_TAG));
        } else {
            this.morphRuntimeState.setActive(false);
            this.morphRuntimeState.setSelectedClassId(this.morphProfiles.isEmpty() ? 0 : this.morphProfiles.get(0).getLegacyClassId());
        }
        if (source.contains(PLAYER_SKILL_RUNTIME_TAG, Tag.TAG_COMPOUND)) {
            this.playerSkillRuntimeState.loadFromTag(source.getCompound(PLAYER_SKILL_RUNTIME_TAG));
        } else {
            this.playerSkillRuntimeState.clear();
        }

        if (this.getSelectedMorphProfile() == null) {
            this.morphRuntimeState.setActive(false);
            this.morphRuntimeState.setSelectedClassId(this.morphProfiles.isEmpty() ? 0 : this.morphProfiles.get(0).getLegacyClassId());
        }
        this.bindMorphDirtyCallbacks();
    }

    private static CompoundTag unwrapLegacyCapabilityTag(CompoundTag tag) {
        if (tag.contains(LEGACY_CAPABILITY_TAG, Tag.TAG_COMPOUND)) {
            return tag.getCompound(LEGACY_CAPABILITY_TAG);
        }
        return tag;
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

    public boolean hasTeam() {
        return this.hasTeam;
    }

    public void setHasTeam(boolean hasTeam) {
        this.hasTeam = hasTeam;
    }

    public int getCurrentTeamId() {
        return this.currentTeamId;
    }

    public void setCurrentTeamId(int teamId) {
        this.currentTeamId = normalizeTeamId(teamId);
    }

    public int getPointerSlotCursor() {
        return Mth.clamp(this.pointerSlotCursor, 0, TEAM_SIZE - 1);
    }

    public void setPointerSlotCursor(int pointerSlotCursor) {
        this.pointerSlotCursor = Mth.clamp(pointerSlotCursor, 0, TEAM_SIZE - 1);
    }

    public int getCurrentFormationId() {
        return this.getFormationId(this.currentTeamId);
    }

    public int getFormationId() {
        return this.getCurrentFormationId();
    }

    public int getFormationId(int teamId) {
        return this.formationIds[normalizeTeamId(teamId)];
    }

    public int[] getFormationIdsCopy() {
        return Arrays.copyOf(this.formationIds, this.formationIds.length);
    }

    public void setFormationId(int formationId) {
        this.setFormationId(this.currentTeamId, formationId);
    }

    public void setFormationId(int teamId, int formationId) {
        this.formationIds[normalizeTeamId(teamId)] = normalizeFormationId(formationId);
    }

    public int cycleFormationId() {
        return this.cycleFormationId(this.currentTeamId);
    }

    public int cycleFormationId(int teamId) {
        int normalizedTeamId = normalizeTeamId(teamId);
        int next = (this.formationIds[normalizedTeamId] + 1) % (MAX_FORMATION_ID + 1);
        this.formationIds[normalizedTeamId] = next;
        return next;
    }

    public int getShipUid(int teamId, int slot) {
        return this.teamShipUids[slotIndex(teamId, slot)];
    }

    public void setShipUid(int teamId, int slot, int shipUid) {
        int index = slotIndex(teamId, slot);
        this.teamShipUids[index] = shipUid > 0 ? shipUid : -1;
        if (shipUid <= 0) {
            this.teamShipSelected[index] = false;
        }
    }

    public void removeShipUidEverywhere(int shipUid) {
        if (shipUid <= 0) {
            return;
        }

        for (int i = 0; i < TEAM_SLOT_COUNT; i++) {
            if (this.teamShipUids[i] == shipUid) {
                this.teamShipUids[i] = -1;
                this.teamShipSelected[i] = false;
            }
        }
    }

    public void assignCurrentTeamSlot(int slot, int shipUid) {
        this.setShipUid(this.currentTeamId, slot, shipUid);
    }

    public boolean isShipSelected(int teamId, int slot) {
        return this.teamShipSelected[slotIndex(teamId, slot)];
    }

    public void setShipSelected(int teamId, int slot, boolean selected) {
        int index = slotIndex(teamId, slot);
        this.teamShipSelected[index] = selected && this.teamShipUids[index] > 0;
    }

    public boolean setCurrentTeamSelection(int slot, boolean selected) {
        int index = slotIndex(this.currentTeamId, slot);
        if (this.teamShipUids[index] <= 0) {
            this.teamShipSelected[index] = false;
            return false;
        }

        this.teamShipSelected[index] = selected;
        return true;
    }

    public boolean toggleCurrentTeamSelect(int slot) {
        int index = slotIndex(this.currentTeamId, slot);
        if (this.teamShipUids[index] <= 0) {
            this.teamShipSelected[index] = false;
            return false;
        }

        this.teamShipSelected[index] = !this.teamShipSelected[index];
        return this.teamShipSelected[index];
    }

    public void clearCurrentTeamSelect() {
        int base = this.currentTeamId * TEAM_SIZE;
        for (int i = 0; i < TEAM_SIZE; i++) {
            this.teamShipSelected[base + i] = false;
        }
    }

    public void clearCurrentTeam() {
        int base = this.currentTeamId * TEAM_SIZE;
        for (int i = 0; i < TEAM_SIZE; i++) {
            this.teamShipUids[base + i] = -1;
            this.teamShipSelected[base + i] = false;
        }
    }

    public boolean swapCurrentTeamSlots(int fromSlot, int toSlot) {
        int from = slotIndex(this.currentTeamId, fromSlot);
        int to = slotIndex(this.currentTeamId, toSlot);
        if (from == to) {
            return false;
        }

        int uid = this.teamShipUids[from];
        this.teamShipUids[from] = this.teamShipUids[to];
        this.teamShipUids[to] = uid;

        boolean selected = this.teamShipSelected[from];
        this.teamShipSelected[from] = this.teamShipSelected[to];
        this.teamShipSelected[to] = selected;
        return true;
    }

    public int[] getTeamShipUids(int teamId) {
        int normalizedTeamId = normalizeTeamId(teamId);
        int[] values = new int[TEAM_SIZE];
        int base = normalizedTeamId * TEAM_SIZE;
        System.arraycopy(this.teamShipUids, base, values, 0, TEAM_SIZE);
        return values;
    }

    public boolean[] getTeamShipSelected(int teamId) {
        int normalizedTeamId = normalizeTeamId(teamId);
        boolean[] values = new boolean[TEAM_SIZE];
        int base = normalizedTeamId * TEAM_SIZE;
        System.arraycopy(this.teamShipSelected, base, values, 0, TEAM_SIZE);
        return values;
    }

    public List<Integer> getCurrentTeamShipUids(boolean selectedOnly) {
        List<Integer> values = new ArrayList<>(TEAM_SIZE);
        int base = this.currentTeamId * TEAM_SIZE;
        for (int i = 0; i < TEAM_SIZE; i++) {
            int uid = this.teamShipUids[base + i];
            if (uid <= 0) {
                continue;
            }
            if (selectedOnly && !this.teamShipSelected[base + i]) {
                continue;
            }
            values.add(uid);
        }
        return values;
    }

    public int findTeamIdByShipUid(int shipUid) {
        if (shipUid <= 0) {
            return -1;
        }

        for (int team = 0; team < TEAM_COUNT; team++) {
            int base = team * TEAM_SIZE;
            for (int slot = 0; slot < TEAM_SIZE; slot++) {
                if (this.teamShipUids[base + slot] == shipUid) {
                    return team;
                }
            }
        }

        return -1;
    }

    public int findSlotIndexByShipUid(int shipUid) {
        if (shipUid <= 0) {
            return -1;
        }

        for (int team = 0; team < TEAM_COUNT; team++) {
            int base = team * TEAM_SIZE;
            for (int slot = 0; slot < TEAM_SIZE; slot++) {
                if (this.teamShipUids[base + slot] == shipUid) {
                    return slot;
                }
            }
        }

        return -1;
    }

    public int countShipsInTeam(int teamId) {
        int normalizedTeamId = normalizeTeamId(teamId);
        int base = normalizedTeamId * TEAM_SIZE;
        int count = 0;

        for (int slot = 0; slot < TEAM_SIZE; slot++) {
            if (this.teamShipUids[base + slot] > 0) {
                count++;
            }
        }

        return count;
    }

    public String getTeamName(int teamId) {
        return this.teamNames[normalizeTeamId(teamId)];
    }

    public void setTeamName(int teamId, String teamName) {
        this.teamNames[normalizeTeamId(teamId)] = teamName == null ? "" : teamName;
    }

    public String[] getTeamNamesCopy() {
        return Arrays.copyOf(this.teamNames, this.teamNames.length);
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

    public int getTargetClassCount() {
        return this.targetClasses.size();
    }

    public boolean hasTargetClass(String targetClass) {
        String normalized = normalizeTargetClass(targetClass);
        return !normalized.isBlank() && this.targetClasses.contains(normalized);
    }

    public boolean addTargetClass(String targetClass) {
        String normalized = normalizeTargetClass(targetClass);
        if (normalized.isBlank() || this.targetClasses.contains(normalized)) {
            return false;
        }

        this.targetClasses.add(normalized);
        return true;
    }

    public boolean removeTargetClass(String targetClass) {
        String normalized = normalizeTargetClass(targetClass);
        if (normalized.isBlank()) {
            return false;
        }

        return this.targetClasses.remove(normalized);
    }

    public boolean toggleTargetClass(String targetClass) {
        return this.hasTargetClass(targetClass)
                ? !this.removeTargetClass(targetClass)
                : this.addTargetClass(targetClass);
    }

    public void setTargetClasses(List<String> targetClasses) {
        this.targetClasses.clear();
        if (targetClasses == null) {
            return;
        }

        for (String targetClass : targetClasses) {
            this.addTargetClass(targetClass);
        }
    }

    public List<MorphProfile> getMorphProfiles() {
        return List.copyOf(this.morphProfiles);
    }

    public int getMorphProfileCount() {
        return this.morphProfiles.size();
    }

    public MorphRuntimeState getMorphRuntimeState() {
        return this.morphRuntimeState;
    }

    public boolean hasActiveMorph() {
        return this.morphRuntimeState.isActive() && this.getSelectedMorphProfile() != null;
    }

    public PlayerSkillRuntimeState getPlayerSkillRuntimeState() {
        return this.playerSkillRuntimeState;
    }

    public boolean hasUnlockedMorph(int legacyClassId) {
        return this.findMorphProfile(legacyClassId) != null;
    }

    public boolean unlockMorph(int legacyClassId) {
        if (legacyClassId <= 0 || this.hasUnlockedMorph(legacyClassId)) {
            return false;
        }

        MorphProfile morphProfile = new MorphProfile(legacyClassId);
        this.installMorphProfile(morphProfile);
        if (this.morphRuntimeState.getSelectedClassId() <= 0) {
            this.morphRuntimeState.setSelectedClassId(legacyClassId);
        }
        return true;
    }

    public MorphProfile findMorphProfile(int legacyClassId) {
        if (legacyClassId <= 0) {
            return null;
        }

        for (MorphProfile morphProfile : this.morphProfiles) {
            if (morphProfile.getLegacyClassId() == legacyClassId) {
                return morphProfile;
            }
        }

        return null;
    }

    public MorphProfile getSelectedMorphProfile() {
        return this.findMorphProfile(this.morphRuntimeState.getSelectedClassId());
    }

    public boolean setSelectedMorphProfile(int legacyClassId) {
        if (legacyClassId <= 0) {
            this.morphRuntimeState.setSelectedClassId(0);
            this.morphRuntimeState.setActive(false);
            return true;
        }

        if (!this.hasUnlockedMorph(legacyClassId)) {
            return false;
        }

        this.morphRuntimeState.setSelectedClassId(legacyClassId);
        return true;
    }

    public boolean cycleMorphProfile(boolean forward) {
        if (this.morphProfiles.isEmpty()) {
            this.morphRuntimeState.setSelectedClassId(0);
            this.morphRuntimeState.setActive(false);
            return false;
        }

        int currentClassId = this.morphRuntimeState.getSelectedClassId();
        int currentIndex = -1;
        for (int i = 0; i < this.morphProfiles.size(); i++) {
            if (this.morphProfiles.get(i).getLegacyClassId() == currentClassId) {
                currentIndex = i;
                break;
            }
        }

        if (currentIndex < 0) {
            currentIndex = 0;
        } else {
            currentIndex = forward
                    ? (currentIndex + 1) % this.morphProfiles.size()
                    : (currentIndex - 1 + this.morphProfiles.size()) % this.morphProfiles.size();
        }

        this.morphRuntimeState.setSelectedClassId(this.morphProfiles.get(currentIndex).getLegacyClassId());
        return true;
    }

    public boolean setMorphActive(boolean active) {
        if (active && this.getSelectedMorphProfile() == null) {
            return false;
        }

        this.morphRuntimeState.setActive(active);
        return true;
    }

    public boolean tickMorphRuntime() {
        return this.morphRuntimeState.tick();
    }

    public void clearMorphSelectionIfMatches(int legacyClassId) {
        if (legacyClassId <= 0 || this.morphRuntimeState.getSelectedClassId() != legacyClassId) {
            return;
        }

        this.morphRuntimeState.setActive(false);
        this.morphRuntimeState.setSelectedClassId(this.morphProfiles.isEmpty() ? 0 : this.morphProfiles.get(0).getLegacyClassId());
    }

    private static String normalizeTargetClass(String targetClass) {
        return targetClass == null ? "" : targetClass.trim().toLowerCase(Locale.ROOT);
    }

    private void installMorphProfile(MorphProfile morphProfile) {
        morphProfile.setDirtyCallback(() -> {
        });
        this.morphProfiles.add(morphProfile);
        this.bindMorphDirtyCallbacks();
    }

    private void bindMorphDirtyCallbacks() {
        for (MorphProfile morphProfile : this.morphProfiles) {
            morphProfile.setDirtyCallback(() -> {
            });
        }
    }

    private static int normalizeFormationId(int formationId) {
        return Mth.clamp(formationId, DEFAULT_FORMATION_ID, MAX_FORMATION_ID);
    }

    private static int normalizeTeamId(int teamId) {
        return Mth.clamp(teamId, 0, TEAM_COUNT - 1);
    }

    private static int slotIndex(int teamId, int slot) {
        int normalizedTeamId = normalizeTeamId(teamId);
        int normalizedSlot = Mth.clamp(slot, 0, TEAM_SIZE - 1);
        return normalizedTeamId * TEAM_SIZE + normalizedSlot;
    }

    private static byte[] toByteArray(boolean[] values) {
        byte[] bytes = new byte[values.length];
        for (int i = 0; i < values.length; i++) {
            bytes[i] = values[i] ? (byte) 1 : (byte) 0;
        }
        return bytes;
    }
}
