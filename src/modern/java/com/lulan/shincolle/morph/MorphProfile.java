package com.lulan.shincolle.morph;

import com.lulan.shincolle.entity.ship.LegacyShipAttackProfile;
import com.lulan.shincolle.entity.ship.LegacyShipEntity;
import com.lulan.shincolle.entity.ship.LegacyShipStats;
import com.lulan.shincolle.entity.ship.ShipEntitySpec;
import com.lulan.shincolle.entity.ship.ShipEntitySpecs;
import com.lulan.shincolle.entity.ship.ShipEquipmentBehaviorState;
import com.lulan.shincolle.entity.ship.ShipEquipmentProfile;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.world.SimpleContainer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraftforge.items.ItemStackHandler;

import java.util.Collection;

public class MorphProfile {

    public static final int DEFAULT_LEVEL = 1;
    public static final int DEFAULT_MORALE = 1600;
    public static final int MAX_MORALE = 16000;
    public static final int MAX_MODERN_TOTAL_STEPS = 24;
    public static final int MAX_MODERN_STAT_STEPS = 12;

    private static final String LEGACY_CLASS_ID_TAG = "LegacyClassId";
    private static final String LEVEL_TAG = "Level";
    private static final String EXPERIENCE_TAG = "Experience";
    private static final String MORALE_TAG = "Morale";
    private static final String AMMO_LIGHT_TAG = "AmmoLight";
    private static final String AMMO_HEAVY_TAG = "AmmoHeavy";
    private static final String GRUDGE_TAG = "Grudge";
    private static final String MARRIED_TAG = "Married";
    private static final String MODERN_HEALTH_TAG = "ModernHealth";
    private static final String MODERN_ATTACK_TAG = "ModernAttack";
    private static final String MODERN_SPEED_TAG = "ModernSpeed";
    private static final String MODERN_RANGE_TAG = "ModernRange";
    private static final String SHOW_HELD_TAG = "ShowHeldItem";
    private static final String AURA_EFFECT_TAG = "AuraEffect";
    private static final String AI_AUTO_TARGET_TAG = "AiAutoTarget";
    private static final String AI_ALLOW_PVP_TAG = "AiAllowPvp";
    private static final String AI_AUTO_SUPPLY_TAG = "AiAutoSupply";
    private static final String AI_ROUTE_STAY_TAG = "AiRouteStay";
    private static final String FOLLOW_RANGE_TAG = "FollowRange";
    private static final String EQUIPMENT_TAG = "Equipment";

    private final ItemStackHandler equipment = new ItemStackHandler(LegacyShipEntity.EQUIPMENT_SLOT_COUNT) {
        @Override
        protected void onContentsChanged(int slot) {
            MorphProfile.this.markDirty();
        }
    };

    private int legacyClassId;
    private int level = DEFAULT_LEVEL;
    private int experience;
    private int morale = DEFAULT_MORALE;
    private int ammoLight = 40;
    private int ammoHeavy = 20;
    private int grudge = 12;
    private boolean married;
    private int modernHealthSteps;
    private int modernAttackSteps;
    private int modernSpeedSteps;
    private int modernRangeSteps;
    private boolean showHeldItem;
    private boolean auraEffect = true;
    private boolean aiAutoTarget = true;
    private boolean aiAllowPvp = true;
    private boolean aiAutoSupply = true;
    private boolean aiRouteStay;
    private int followRange = 14;
    private Runnable dirtyCallback = () -> {
    };

    public MorphProfile() {
    }

    public MorphProfile(int legacyClassId) {
        this.legacyClassId = legacyClassId;
    }

    public void setDirtyCallback(Runnable dirtyCallback) {
        this.dirtyCallback = dirtyCallback == null ? () -> {
        } : dirtyCallback;
    }

    public CompoundTag saveToTag(CompoundTag tag) {
        tag.putInt(LEGACY_CLASS_ID_TAG, this.legacyClassId);
        tag.putInt(LEVEL_TAG, this.level);
        tag.putInt(EXPERIENCE_TAG, this.experience);
        tag.putInt(MORALE_TAG, this.morale);
        tag.putInt(AMMO_LIGHT_TAG, this.ammoLight);
        tag.putInt(AMMO_HEAVY_TAG, this.ammoHeavy);
        tag.putInt(GRUDGE_TAG, this.grudge);
        tag.putBoolean(MARRIED_TAG, this.married);
        tag.putInt(MODERN_HEALTH_TAG, this.modernHealthSteps);
        tag.putInt(MODERN_ATTACK_TAG, this.modernAttackSteps);
        tag.putInt(MODERN_SPEED_TAG, this.modernSpeedSteps);
        tag.putInt(MODERN_RANGE_TAG, this.modernRangeSteps);
        tag.putBoolean(SHOW_HELD_TAG, this.showHeldItem);
        tag.putBoolean(AURA_EFFECT_TAG, this.auraEffect);
        tag.putBoolean(AI_AUTO_TARGET_TAG, this.aiAutoTarget);
        tag.putBoolean(AI_ALLOW_PVP_TAG, this.aiAllowPvp);
        tag.putBoolean(AI_AUTO_SUPPLY_TAG, this.aiAutoSupply);
        tag.putBoolean(AI_ROUTE_STAY_TAG, this.aiRouteStay);
        tag.putInt(FOLLOW_RANGE_TAG, this.followRange);
        tag.put(EQUIPMENT_TAG, this.equipment.serializeNBT());
        return tag;
    }

    public void loadFromTag(CompoundTag tag) {
        this.legacyClassId = tag.getInt(LEGACY_CLASS_ID_TAG);
        this.level = Mth.clamp(tag.getInt(LEVEL_TAG), DEFAULT_LEVEL, this.getLevelCap());
        this.experience = Math.max(0, tag.getInt(EXPERIENCE_TAG));
        this.morale = Mth.clamp(tag.getInt(MORALE_TAG), 0, MAX_MORALE);
        this.ammoLight = Math.max(0, tag.getInt(AMMO_LIGHT_TAG));
        this.ammoHeavy = Math.max(0, tag.getInt(AMMO_HEAVY_TAG));
        this.grudge = Math.max(0, tag.getInt(GRUDGE_TAG));
        this.married = tag.getBoolean(MARRIED_TAG);
        this.modernHealthSteps = Mth.clamp(tag.getInt(MODERN_HEALTH_TAG), 0, MAX_MODERN_STAT_STEPS);
        this.modernAttackSteps = Mth.clamp(tag.getInt(MODERN_ATTACK_TAG), 0, MAX_MODERN_STAT_STEPS);
        this.modernSpeedSteps = Mth.clamp(tag.getInt(MODERN_SPEED_TAG), 0, MAX_MODERN_STAT_STEPS);
        this.modernRangeSteps = Mth.clamp(tag.getInt(MODERN_RANGE_TAG), 0, MAX_MODERN_STAT_STEPS);
        this.showHeldItem = tag.getBoolean(SHOW_HELD_TAG);
        this.auraEffect = !tag.contains(AURA_EFFECT_TAG) || tag.getBoolean(AURA_EFFECT_TAG);
        this.aiAutoTarget = !tag.contains(AI_AUTO_TARGET_TAG) || tag.getBoolean(AI_AUTO_TARGET_TAG);
        this.aiAllowPvp = !tag.contains(AI_ALLOW_PVP_TAG) || tag.getBoolean(AI_ALLOW_PVP_TAG);
        this.aiAutoSupply = !tag.contains(AI_AUTO_SUPPLY_TAG) || tag.getBoolean(AI_AUTO_SUPPLY_TAG);
        this.aiRouteStay = tag.getBoolean(AI_ROUTE_STAY_TAG);
        this.followRange = Mth.clamp(tag.getInt(FOLLOW_RANGE_TAG), 4, 64);
        if (tag.contains(EQUIPMENT_TAG)) {
            this.equipment.deserializeNBT(tag.getCompound(EQUIPMENT_TAG));
        }
    }

    public MorphProfile copy() {
        MorphProfile copy = new MorphProfile();
        copy.loadFromTag(this.saveToTag(new CompoundTag()));
        return copy;
    }

    public void markDirty() {
        this.dirtyCallback.run();
    }

    public boolean addRandomModernization(RandomSource random) {
        if (this.getModernizationCount() >= MAX_MODERN_TOTAL_STEPS) {
            return false;
        }

        int[] weights = new int[] {
                this.modernHealthSteps < MAX_MODERN_STAT_STEPS ? 1 : 0,
                this.modernAttackSteps < MAX_MODERN_STAT_STEPS ? 1 : 0,
                this.modernSpeedSteps < MAX_MODERN_STAT_STEPS ? 1 : 0,
                this.modernRangeSteps < MAX_MODERN_STAT_STEPS ? 1 : 0
        };
        int total = weights[0] + weights[1] + weights[2] + weights[3];
        if (total <= 0) {
            return false;
        }

        int roll = random.nextInt(total);
        for (int i = 0; i < weights.length; i++) {
            if (roll < weights[i]) {
                switch (i) {
                    case 0 -> this.modernHealthSteps++;
                    case 1 -> this.modernAttackSteps++;
                    case 2 -> this.modernSpeedSteps++;
                    default -> this.modernRangeSteps++;
                }
                this.markDirty();
                return true;
            }
            roll -= weights[i];
        }

        return false;
    }

    public ShipEntitySpec getSpec() {
        ShipEntitySpec spec = ShipEntitySpecs.findByLegacyClassId(this.legacyClassId);
        return spec == null ? ShipEntitySpecs.DEFAULT : spec;
    }

    public ShipEquipmentProfile buildEquipmentProfile() {
        return ShipEquipmentProfile.fromInventory(this.createEquipmentContainerCopy(), this.getSpec().archetype());
    }

    public ShipEquipmentBehaviorState buildBehaviorState() {
        return ShipEquipmentBehaviorState.fromInventory(this.createEquipmentContainerCopy(), this.getSpec().archetype());
    }

    public LegacyShipAttackProfile buildAttackProfile() {
        return LegacyShipAttackProfile.resolve(this.getSpec());
    }

    public LegacyShipStats buildStats(Collection<MobEffectInstance> activeEffects) {
        return LegacyShipStats.create(
                this.getSpec().legacyClassId(),
                this.getSpec().archetype(),
                this.getSpec().hostile(),
                this.level,
                this.morale,
                this.modernHealthSteps,
                this.modernAttackSteps,
                this.modernSpeedSteps,
                this.modernRangeSteps,
                this.married,
                0,
                this.buildEquipmentProfile(),
                activeEffects);
    }

    public int getLevelCap() {
        return this.married ? 150 : 100;
    }

    public int getModernizationCount() {
        return this.modernHealthSteps + this.modernAttackSteps + this.modernSpeedSteps + this.modernRangeSteps;
    }

    public int getLegacyClassId() {
        return this.legacyClassId;
    }

    public void setLegacyClassId(int legacyClassId) {
        this.legacyClassId = legacyClassId;
        this.markDirty();
    }

    public int getLevel() {
        return this.level;
    }

    public void setLevel(int level) {
        this.level = Mth.clamp(level, DEFAULT_LEVEL, this.getLevelCap());
        this.markDirty();
    }

    public int getExperience() {
        return this.experience;
    }

    public void setExperience(int experience) {
        this.experience = Math.max(0, experience);
        this.markDirty();
    }

    public int getMorale() {
        return this.morale;
    }

    public void setMorale(int morale) {
        this.morale = Mth.clamp(morale, 0, MAX_MORALE);
        this.markDirty();
    }

    public void addMorale(int amount) {
        this.setMorale(this.morale + amount);
    }

    public int getAmmoLight() {
        return this.ammoLight;
    }

    public void setAmmoLight(int ammoLight) {
        this.ammoLight = Math.max(0, ammoLight);
        this.markDirty();
    }

    public void addAmmoLight(int amount) {
        this.setAmmoLight(this.ammoLight + amount);
    }

    public int getAmmoHeavy() {
        return this.ammoHeavy;
    }

    public void setAmmoHeavy(int ammoHeavy) {
        this.ammoHeavy = Math.max(0, ammoHeavy);
        this.markDirty();
    }

    public void addAmmoHeavy(int amount) {
        this.setAmmoHeavy(this.ammoHeavy + amount);
    }

    public int getGrudge() {
        return this.grudge;
    }

    public void setGrudge(int grudge) {
        this.grudge = Math.max(0, grudge);
        this.markDirty();
    }

    public void addGrudge(int amount) {
        this.setGrudge(this.grudge + amount);
    }

    public boolean isMarried() {
        return this.married;
    }

    public void setMarried(boolean married) {
        this.married = married;
        if (!married && this.level > 100) {
            this.level = 100;
        }
        this.markDirty();
    }

    public int getModernizationDisplayCount() {
        return this.getModernizationCount();
    }

    public int getModernHealthSteps() {
        return this.modernHealthSteps;
    }

    public int getModernAttackSteps() {
        return this.modernAttackSteps;
    }

    public int getModernSpeedSteps() {
        return this.modernSpeedSteps;
    }

    public int getModernRangeSteps() {
        return this.modernRangeSteps;
    }

    public boolean isShowHeldItem() {
        return this.showHeldItem;
    }

    public void setShowHeldItem(boolean showHeldItem) {
        this.showHeldItem = showHeldItem;
        this.markDirty();
    }

    public boolean hasAuraEffect() {
        return this.auraEffect;
    }

    public void setAuraEffect(boolean auraEffect) {
        this.auraEffect = auraEffect;
        this.markDirty();
    }

    public boolean isAiAutoTarget() {
        return this.aiAutoTarget;
    }

    public void setAiAutoTarget(boolean aiAutoTarget) {
        this.aiAutoTarget = aiAutoTarget;
        this.markDirty();
    }

    public boolean isAiAllowPvp() {
        return this.aiAllowPvp;
    }

    public void setAiAllowPvp(boolean aiAllowPvp) {
        this.aiAllowPvp = aiAllowPvp;
        this.markDirty();
    }

    public boolean isAiAutoSupply() {
        return this.aiAutoSupply;
    }

    public void setAiAutoSupply(boolean aiAutoSupply) {
        this.aiAutoSupply = aiAutoSupply;
        this.markDirty();
    }

    public boolean isAiRouteStay() {
        return this.aiRouteStay;
    }

    public void setAiRouteStay(boolean aiRouteStay) {
        this.aiRouteStay = aiRouteStay;
        this.markDirty();
    }

    public int getFollowRange() {
        return this.followRange;
    }

    public void setFollowRange(int followRange) {
        this.followRange = Mth.clamp(followRange, 4, 64);
        this.markDirty();
    }

    public ItemStackHandler getEquipment() {
        return this.equipment;
    }

    private SimpleContainer createEquipmentContainerCopy() {
        SimpleContainer container = new SimpleContainer(LegacyShipEntity.EQUIPMENT_SLOT_COUNT);
        for (int slot = 0; slot < LegacyShipEntity.EQUIPMENT_SLOT_COUNT; slot++) {
            container.setItem(slot, this.equipment.getStackInSlot(slot).copy());
        }
        return container;
    }
}
