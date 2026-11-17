package com.lulan.shincolle.teitoku;

import com.lulan.shincolle.entity.ship.LegacyShipEntity;
import com.lulan.shincolle.entity.ship.LegacyShipBehaviorCatalog;
import com.lulan.shincolle.entity.ship.ShipEntitySpec;
import com.lulan.shincolle.entity.ship.ShipEntitySpecs;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public record ShipWorldCacheEntry(
        int shipUid,
        int entityId,
        String dimensionId,
        int legacyClassId,
        int variantEggMeta,
        int ownerUid,
        String ownerName,
        boolean online,
        boolean dead,
        int posX,
        int posY,
        int posZ,
        CompoundTag entityTag
) {

    private static final String SHIP_UID_TAG = "ShipUID";
    private static final String ENTITY_ID_TAG = "EntityID";
    private static final String DIMENSION_TAG = "Dimension";
    private static final String LEGACY_CLASS_ID_TAG = "LegacyClassId";
    private static final String VARIANT_EGG_META_TAG = "VariantEggMeta";
    private static final String OWNER_UID_TAG = "OwnerUID";
    private static final String OWNER_NAME_TAG = "OwnerName";
    private static final String ONLINE_TAG = "Online";
    private static final String DEAD_TAG = "Dead";
    private static final String POS_X_TAG = "PosX";
    private static final String POS_Y_TAG = "PosY";
    private static final String POS_Z_TAG = "PosZ";
    private static final String ENTITY_TAG = "EntityTag";
    private static final String CUSTOM_NAME_TAG = "CustomName";
    private static final String ORDERED_TO_SIT_TAG = "OrderedToSit";
    private static final String SHIP_LEVEL_TAG = "ShipLevel";
    private static final String SHIP_MORALE_TAG = "ShipMorale";
    private static final String SHIP_FUEL_TAG = "ShipFuel";
    private static final String SHIP_LIGHT_AMMO_TAG = "ShipLightAmmo";
    private static final String SHIP_HEAVY_AMMO_TAG = "ShipHeavyAmmo";
    private static final String SHIP_GRUDGE_TAG = "ShipGrudge";
    private static final String SHIP_COMBAT_RATION_LEVEL_TAG = "ShipCombatRationLevel";
    private static final String SHIP_FOOD_SATURATION_TAG = "ShipFoodSaturation";
    private static final String LEGACY_LIGHT_AMMO_TAG = "NumAmmoLight";
    private static final String LEGACY_HEAVY_AMMO_TAG = "NumAmmoHeavy";
    private static final String LEGACY_GRUDGE_TAG = "NumGrudge";
    private static final String LEGACY_EXTENDED_PROPERTIES_TAG = "ShipExtProps";
    private static final String LEGACY_MINOR_TAG = "Minor";
    private static final String LEGACY_LEVEL_TAG = "Level";
    private static final String LEGACY_MORALE_TAG = "Morale";
    private static final String LEGACY_COMBAT_RATION_LEVEL_TAG = "AutoCR";
    private static final String LEGACY_FOOD_SATURATION_TAG = "Food";
    private static final String LEGACY_TASK_TAG = "Task";
    private static final String LEGACY_TASK_SIDE_TAG = "Side";
    private static final String LEGACY_DISPLAY_TAG = "Display";
    private static final String LEGACY_MODEL_STATE_TAG = "State";
    private static final String LEGACY_HP_STATE_TAG = "HPState";
    private static final String LEGACY_EMOTION_TAG = "Emotion";
    private static final String LEGACY_EMOTION2_TAG = "Emotion2";
    private static final String LEGACY_PHASE_TAG = "Phase";
    private static final String LEGACY_LIGHT_AMMO_MINOR_TAG = "NumAmmoL";
    private static final String LEGACY_HEAVY_AMMO_MINOR_TAG = "NumAmmoH";
    private static final String LEGACY_GRUDGE_MINOR_TAG = "NumGrudge";
    private static final String WED_EFFECT_TAG = "WedEffect";
    private static final String LEGACY_FLAGS_TAG = "ShipFlags";
    private static final String LEGACY_MARRIED_FLAG_TAG = "IsMarried";
    private static final String LEGACY_RING_EFFECT_FLAG_TAG = "WedEffect";
    private static final String LEGACY_PICK_ITEM_FLAG_TAG = "PickItem";
    private static final String LEGACY_TIME_KEEPER_FLAG_TAG = "TimeKeeper";
    private static final String LEGACY_AUTO_PUMP_FLAG_TAG = "AutoPump";
    private static final String LEGACY_CAN_FLOAT_FLAG_TAG = "CanFloat";
    private static final String LEGACY_CAN_FOLLOW_FLAG_TAG = "CanFollow";
    private static final String LEGACY_CAN_DROP_FLAG_TAG = "CanDrop";
    private static final String LEGACY_HELD_ITEM_FLAG_TAG = "HeldItem";
    private static final String LEGACY_USE_MELEE_FLAG_TAG = "Melee";
    private static final String LEGACY_USE_LIGHT_FLAG_TAG = "AmmoL";
    private static final String LEGACY_USE_HEAVY_FLAG_TAG = "AmmoH";
    private static final String LEGACY_USE_AIR_LIGHT_FLAG_TAG = "AirL";
    private static final String LEGACY_USE_AIR_HEAVY_FLAG_TAG = "AirH";
    private static final String LEGACY_PASSIVE_AI_FLAG_TAG = "PassiveAI";
    private static final String LEGACY_ON_SIGHT_CHASE_FLAG_TAG = "OnSight";
    private static final String LEGACY_PVP_FIRST_FLAG_TAG = "PVPFirst";
    private static final String LEGACY_ANTI_AIR_FLAG_TAG = "AA";
    private static final String LEGACY_ANTI_SUB_FLAG_TAG = "ASM";
    private static final String SHIP_MARRIED_TAG = "ShipMarried";
    private static final String AI_PICK_ITEM_TAG = "AiPickItem";
    private static final String SHIP_TIME_KEEPER_TAG = "ShipTimeKeeper";
    private static final String SHIP_AUTO_PUMP_TAG = "ShipAutoPump";
    private static final String SHIP_WP_STAY_TAG = "ShipWpStay";
    private static final String SHIP_TASK_TAG = "ShipTask";
    private static final String SHIP_TASK_SIDE_TAG = "ShipTaskSide";
    private static final String SHIP_MODEL_STATE_TAG = "ShipModelState";
    private static final String SHIP_HP_STATE_TAG = "ShipHpState";
    private static final String SHIP_EMOTION_TAG = "ShipEmotion";
    private static final String SHIP_EMOTION2_TAG = "ShipEmotion2";
    private static final String SHIP_PHASE_TAG = "ShipPhase";
    private static final String SHIP_USE_MELEE_TAG = "ShipUseMelee";
    private static final String SHIP_USE_LIGHT_TAG = "ShipUseAmmoLight";
    private static final String SHIP_USE_HEAVY_TAG = "ShipUseAmmoHeavy";
    private static final String SHIP_USE_AIR_LIGHT_TAG = "ShipUseAirLight";
    private static final String SHIP_USE_AIR_HEAVY_TAG = "ShipUseAirHeavy";
    private static final String AI_AUTO_TARGET_TAG = "AiAutoTarget";
    private static final String AI_ON_SIGHT_CHASE_TAG = "AiOnSightChase";
    private static final String AI_PVP_FIRST_TAG = "AiPvpFirst";
    private static final String AI_ANTI_AIR_TAG = "AiAntiAir";
    private static final String AI_ANTI_SUB_TAG = "AiAntiSub";
    private static final String SHIP_CAN_FLOAT_TAG = "ShipCanFloat";
    private static final String SHIP_CAN_FOLLOW_TAG = "ShipCanFollow";
    private static final String SHIP_CAN_DROP_TAG = "ShipCanDrop";
    private static final String SHIP_SHOW_HELD_ITEM_TAG = "ShipShowHeldItem";
    private static final String LEGACY_WP_STAY_TAG = "WpStay";
    private static final String COMMAND_POS_TAG = "CommandPos";
    private static final String GUARD_ENTITY_TAG = "GuardEntity";
    private static final String GUARD_BLOCK_POS_TAG = "GuardBlockPos";
    private static final String GUARD_BLOCK_DIM_TAG = "GuardBlockDim";
    private static final String ROUTE_NODE_TAG = "RouteNode";
    private static final String LEGACY_GUARD_X_TAG = "GuardX";
    private static final String LEGACY_GUARD_Y_TAG = "GuardY";
    private static final String LEGACY_GUARD_Z_TAG = "GuardZ";
    private static final String LEGACY_GUARD_DIM_TAG = "GuardDim";
    private static final String LEGACY_GUARD_TYPE_TAG = "GuardType";
    private static final int LEGACY_GUARD_BLOCK_TYPE = 1;

    public ShipWorldCacheEntry {
        dimensionId = dimensionId == null || dimensionId.isBlank()
                ? "minecraft:overworld"
                : dimensionId;
        ownerName = ownerName == null ? "" : ownerName;
        entityTag = entityTag == null ? new CompoundTag() : entityTag.copy();
    }

    public static ShipWorldCacheEntry fromShip(LegacyShipEntity ship, boolean dead) {
        return fromShip(ship, dead, !dead);
    }

    public static ShipWorldCacheEntry fromShip(LegacyShipEntity ship, boolean dead, boolean online) {
        ResourceLocation dimensionKey = ship.level().dimension().location();
        CompoundTag entityTag = ship.saveWithoutId(new CompoundTag());
        return new ShipWorldCacheEntry(
                ship.getShipUid(),
                ship.getId(),
                dimensionKey == null ? "minecraft:overworld" : dimensionKey.toString(),
                ship.getShipClassId(),
                ship.getVariantEggMeta(),
                ship.getOwnerUid(),
                ship.getOwnerName(),
                online && !dead,
                dead,
                (int) Math.floor(ship.getX()),
                (int) Math.floor(ship.getY()),
                (int) Math.floor(ship.getZ()),
                entityTag);
    }

    public CompoundTag saveToTag(CompoundTag tag) {
        tag.putInt(SHIP_UID_TAG, this.shipUid);
        tag.putInt(ENTITY_ID_TAG, this.entityId);
        tag.putString(DIMENSION_TAG, this.dimensionId);
        tag.putInt(LEGACY_CLASS_ID_TAG, this.legacyClassId);
        tag.putInt(VARIANT_EGG_META_TAG, this.variantEggMeta);
        tag.putInt(OWNER_UID_TAG, this.ownerUid);
        if (!this.ownerName.isBlank()) {
            tag.putString(OWNER_NAME_TAG, this.ownerName);
        }
        tag.putBoolean(ONLINE_TAG, this.online && !this.dead);
        tag.putBoolean(DEAD_TAG, this.dead);
        tag.putInt(POS_X_TAG, this.posX);
        tag.putInt(POS_Y_TAG, this.posY);
        tag.putInt(POS_Z_TAG, this.posZ);
        tag.put(ENTITY_TAG, this.entityTag.copy());
        return tag;
    }

    public static ShipWorldCacheEntry load(CompoundTag tag) {
        CompoundTag entityTag = tag.contains(ENTITY_TAG, Tag.TAG_COMPOUND)
                ? tag.getCompound(ENTITY_TAG).copy()
                : new CompoundTag();
        return new ShipWorldCacheEntry(
                Math.max(0, tag.getInt(SHIP_UID_TAG)),
                tag.getInt(ENTITY_ID_TAG),
                tag.getString(DIMENSION_TAG),
                tag.getInt(LEGACY_CLASS_ID_TAG),
                tag.contains(VARIANT_EGG_META_TAG) ? tag.getInt(VARIANT_EGG_META_TAG) : 0,
                Math.max(0, tag.getInt(OWNER_UID_TAG)),
                tag.getString(OWNER_NAME_TAG),
                tag.contains(ONLINE_TAG) && tag.getBoolean(ONLINE_TAG),
                tag.getBoolean(DEAD_TAG),
                tag.getInt(POS_X_TAG),
                tag.getInt(POS_Y_TAG),
                tag.getInt(POS_Z_TAG),
                entityTag);
    }

    public Component resolveDisplayName() {
        if (this.entityTag.contains(CUSTOM_NAME_TAG, Tag.TAG_STRING)) {
            String serialized = this.entityTag.getString(CUSTOM_NAME_TAG);
            if (!serialized.isBlank()) {
                try {
                    Component component = Component.Serializer.fromJson(serialized);
                    if (component != null) {
                        return component;
                    }
                } catch (Exception ignored) {
                    return Component.literal(serialized);
                }
            }
        }

        ShipEntitySpec spec = ShipEntitySpecs.findByLegacyClassId(this.legacyClassId);
        if (spec == null) {
            spec = ShipEntitySpecs.findByEggMeta(this.variantEggMeta);
        }
        return spec != null ? spec.displayName() : Component.literal("Ship UID " + this.shipUid);
    }

    public int getShipLevel() {
        return this.getIntWithFallback(SHIP_LEVEL_TAG, null, LEGACY_LEVEL_TAG);
    }

    public int getMorale() {
        return this.getIntWithFallback(SHIP_MORALE_TAG, null, LEGACY_MORALE_TAG);
    }

    public int getShipFuel() {
        return this.entityTag.getInt(SHIP_FUEL_TAG);
    }

    public int getLightAmmo() {
        return this.getIntWithFallback(SHIP_LIGHT_AMMO_TAG, LEGACY_LIGHT_AMMO_TAG, LEGACY_LIGHT_AMMO_MINOR_TAG);
    }

    public int getHeavyAmmo() {
        return this.getIntWithFallback(SHIP_HEAVY_AMMO_TAG, LEGACY_HEAVY_AMMO_TAG, LEGACY_HEAVY_AMMO_MINOR_TAG);
    }

    public int getGrudge() {
        return this.getIntWithFallback(SHIP_GRUDGE_TAG, LEGACY_GRUDGE_TAG, LEGACY_GRUDGE_MINOR_TAG);
    }

    public int getCombatRationLevel() {
        return this.getIntWithFallback(SHIP_COMBAT_RATION_LEVEL_TAG, null,
                LEGACY_COMBAT_RATION_LEVEL_TAG, LegacyShipEntity.DEFAULT_COMBAT_RATION_LEVEL);
    }

    public int getFoodSaturation() {
        return this.getIntWithFallback(SHIP_FOOD_SATURATION_TAG, null,
                LEGACY_FOOD_SATURATION_TAG, 0);
    }

    public int getTask() {
        return this.getIntWithFallback(SHIP_TASK_TAG, null, LEGACY_TASK_TAG, LegacyShipEntity.DEFAULT_TASK);
    }

    public int getTaskSide() {
        return this.getIntWithFallback(SHIP_TASK_SIDE_TAG, null, LEGACY_TASK_SIDE_TAG, LegacyShipEntity.DEFAULT_TASK_SIDE);
    }

    public int getModelState() {
        return this.getIntWithDisplayFallback(SHIP_MODEL_STATE_TAG, LEGACY_MODEL_STATE_TAG,
                LegacyShipEntity.DEFAULT_MODEL_STATE);
    }

    public int getHpState() {
        return this.getIntWithDisplayFallback(SHIP_HP_STATE_TAG, LEGACY_HP_STATE_TAG,
                LegacyShipEntity.DEFAULT_HP_STATE);
    }

    public int getEmotion() {
        return this.getIntWithDisplayFallback(SHIP_EMOTION_TAG, LEGACY_EMOTION_TAG,
                LegacyShipEntity.DEFAULT_EMOTION);
    }

    public int getEmotion2() {
        return this.getIntWithDisplayFallback(SHIP_EMOTION2_TAG, LEGACY_EMOTION2_TAG,
                LegacyShipEntity.DEFAULT_EMOTION2);
    }

    public int getDisplayPhase() {
        return this.getIntWithDisplayFallback(SHIP_PHASE_TAG, LEGACY_PHASE_TAG,
                LegacyShipEntity.DEFAULT_DISPLAY_PHASE);
    }

    public boolean isMarried() {
        return this.getBooleanWithLegacyFlagsFallback(SHIP_MARRIED_TAG, LEGACY_MARRIED_FLAG_TAG, false);
    }

    public boolean isRingEffectEnabled() {
        return this.getBooleanWithLegacyFlagsFallback(WED_EFFECT_TAG, LEGACY_RING_EFFECT_FLAG_TAG, true);
    }

    public boolean isAiPickItem() {
        return this.getBooleanWithLegacyFlagsFallback(AI_PICK_ITEM_TAG, LEGACY_PICK_ITEM_FLAG_TAG, true);
    }

    public boolean isTimeKeeper() {
        return this.getBooleanWithLegacyFlagsFallback(SHIP_TIME_KEEPER_TAG, LEGACY_TIME_KEEPER_FLAG_TAG, true);
    }

    public boolean isAutoPump() {
        return this.getBooleanWithLegacyFlagsFallback(SHIP_AUTO_PUMP_TAG, LEGACY_AUTO_PUMP_FLAG_TAG, false);
    }

    public boolean isCanFloatUp() {
        return this.getBooleanWithLegacyFlagsFallback(
                SHIP_CAN_FLOAT_TAG, LEGACY_CAN_FLOAT_FLAG_TAG, LegacyShipEntity.DEFAULT_CAN_FLOAT_UP);
    }

    public boolean canFollow() {
        return this.getBooleanWithLegacyFlagsFallback(
                SHIP_CAN_FOLLOW_TAG, LEGACY_CAN_FOLLOW_FLAG_TAG, LegacyShipEntity.DEFAULT_CAN_FOLLOW);
    }

    public boolean isCanDropItem() {
        return this.getBooleanWithLegacyFlagsFallback(
                SHIP_CAN_DROP_TAG, LEGACY_CAN_DROP_FLAG_TAG, LegacyShipEntity.DEFAULT_CAN_DROP_ITEM);
    }

    public boolean isShowHeldItem() {
        return this.getBooleanWithLegacyFlagsFallback(
                SHIP_SHOW_HELD_ITEM_TAG, LEGACY_HELD_ITEM_FLAG_TAG,
                LegacyShipEntity.DEFAULT_SHOW_HELD_ITEM);
    }

    /** Legacy PassiveAI disables the active range-target task. */
    public boolean isAiAutoTarget() {
        if (this.entityTag.contains(AI_AUTO_TARGET_TAG, Tag.TAG_BYTE)) {
            return this.entityTag.getBoolean(AI_AUTO_TARGET_TAG);
        }
        return !this.getBooleanWithLegacyFlagsFallback(
                LEGACY_PASSIVE_AI_FLAG_TAG, LEGACY_PASSIVE_AI_FLAG_TAG, false);
    }

    /** Legacy hostile ship classes override OnSightChase to false regardless of saved flags. */
    public boolean isAiOnSightChase() {
        if (this.isHostileVariant()) {
            return false;
        }
        return this.getBooleanWithLegacyFlagsFallback(
                AI_ON_SIGHT_CHASE_TAG, LEGACY_ON_SIGHT_CHASE_FLAG_TAG, true);
    }

    public boolean isAiPvpFirst() {
        return this.getBooleanWithLegacyFlagsFallback(
                AI_PVP_FIRST_TAG, LEGACY_PVP_FIRST_FLAG_TAG, LegacyShipEntity.DEFAULT_AI_PVP_FIRST);
    }

    public boolean isAiAntiAir() {
        return this.getBooleanWithLegacyFlagsFallback(
                AI_ANTI_AIR_TAG, LEGACY_ANTI_AIR_FLAG_TAG, LegacyShipEntity.DEFAULT_AI_ANTI_AIR);
    }

    public boolean isAiAntiSub() {
        return this.getBooleanWithLegacyFlagsFallback(
                AI_ANTI_SUB_TAG, LEGACY_ANTI_SUB_FLAG_TAG, LegacyShipEntity.DEFAULT_AI_ANTI_SUB);
    }

    public boolean useAttackMelee() {
        return this.getBooleanWithLegacyFlagsFallback(SHIP_USE_MELEE_TAG, LEGACY_USE_MELEE_FLAG_TAG,
                LegacyShipEntity.DEFAULT_USE_MELEE);
    }

    public boolean useAttackLight() {
        return this.getBooleanWithLegacyFlagsFallback(SHIP_USE_LIGHT_TAG, LEGACY_USE_LIGHT_FLAG_TAG,
                LegacyShipEntity.DEFAULT_USE_LIGHT);
    }

    public boolean useAttackHeavy() {
        return this.getBooleanWithLegacyFlagsFallback(SHIP_USE_HEAVY_TAG, LEGACY_USE_HEAVY_FLAG_TAG,
                LegacyShipEntity.DEFAULT_USE_HEAVY);
    }

    public boolean useAttackAirLight() {
        return this.getBooleanWithLegacyFlagsFallback(SHIP_USE_AIR_LIGHT_TAG, LEGACY_USE_AIR_LIGHT_FLAG_TAG,
                LegacyShipEntity.DEFAULT_USE_AIR_LIGHT);
    }

    public boolean useAttackAirHeavy() {
        return this.getBooleanWithLegacyFlagsFallback(SHIP_USE_AIR_HEAVY_TAG, LEGACY_USE_AIR_HEAVY_FLAG_TAG,
                LegacyShipEntity.DEFAULT_USE_AIR_HEAVY);
    }

    public int getWaypointStayMode() {
        return Math.max(LegacyShipEntity.DEFAULT_WP_STAY_MODE,
                Math.min(LegacyShipEntity.MAX_WP_STAY_MODE,
                        this.getIntWithFallback(SHIP_WP_STAY_TAG, null, LEGACY_WP_STAY_TAG)));
    }

    public boolean isOrderedToSit() {
        return this.entityTag.getBoolean(ORDERED_TO_SIT_TAG);
    }

    public boolean hasCommandState() {
        return this.entityTag.contains(COMMAND_POS_TAG, Tag.TAG_LONG)
                || this.entityTag.hasUUID(GUARD_ENTITY_TAG)
                || this.getGuardedBlockPos() != null
                || this.entityTag.contains(ROUTE_NODE_TAG, Tag.TAG_LONG);
    }

    private boolean isHostileVariant() {
        ShipEntitySpec spec = ShipEntitySpecs.findByEggMeta(this.variantEggMeta);
        if (spec == null) {
            spec = ShipEntitySpecs.findByLegacyClassId(this.legacyClassId);
        }
        return spec != null && spec.hostile();
    }

    public BlockPos getGuardedBlockPos() {
        if (this.entityTag.contains(GUARD_BLOCK_POS_TAG, Tag.TAG_LONG)) {
            return BlockPos.of(this.entityTag.getLong(GUARD_BLOCK_POS_TAG));
        }

        if (this.entityTag.contains(COMMAND_POS_TAG, Tag.TAG_LONG)
                || this.entityTag.hasUUID(GUARD_ENTITY_TAG)
                || this.entityTag.contains(ROUTE_NODE_TAG, Tag.TAG_LONG)) {
            return null;
        }

        CompoundTag minor = this.getLegacyMinorTag();
        if (minor == null
                || !minor.contains(LEGACY_GUARD_TYPE_TAG, Tag.TAG_INT)
                || minor.getInt(LEGACY_GUARD_TYPE_TAG) != LEGACY_GUARD_BLOCK_TYPE
                || !minor.contains(LEGACY_GUARD_X_TAG, Tag.TAG_INT)
                || !minor.contains(LEGACY_GUARD_Y_TAG, Tag.TAG_INT)
                || !minor.contains(LEGACY_GUARD_Z_TAG, Tag.TAG_INT)) {
            return null;
        }

        return new BlockPos(
                minor.getInt(LEGACY_GUARD_X_TAG),
                minor.getInt(LEGACY_GUARD_Y_TAG),
                minor.getInt(LEGACY_GUARD_Z_TAG));
    }

    public String getGuardedBlockDimension() {
        if (this.getGuardedBlockPos() == null) {
            return "";
        }
        if (this.entityTag.contains(GUARD_BLOCK_DIM_TAG, Tag.TAG_STRING)) {
            return this.entityTag.getString(GUARD_BLOCK_DIM_TAG);
        }

        CompoundTag minor = this.getLegacyMinorTag();
        return minor != null && minor.contains(LEGACY_GUARD_DIM_TAG, Tag.TAG_INT)
                ? LegacyShipBehaviorCatalog.modernDimensionIdForLegacy(minor.getInt(LEGACY_GUARD_DIM_TAG))
                : "";
    }

    public String getPositionText() {
        return this.posX + ", " + this.posY + ", " + this.posZ;
    }

    private int getIntWithFallback(String primaryTag, String legacyTag, String legacyMinorTag) {
        return this.getIntWithFallback(primaryTag, legacyTag, legacyMinorTag, 0);
    }

    private int getIntWithFallback(String primaryTag, String legacyTag, String legacyMinorTag, int defaultValue) {
        if (this.entityTag.contains(primaryTag, Tag.TAG_INT)) {
            return this.entityTag.getInt(primaryTag);
        }

        if (legacyTag != null && this.entityTag.contains(legacyTag, Tag.TAG_INT)) {
            return this.entityTag.getInt(legacyTag);
        }

        CompoundTag minor = this.getLegacyMinorTag();
        return minor != null && legacyMinorTag != null && minor.contains(legacyMinorTag, Tag.TAG_INT)
                ? minor.getInt(legacyMinorTag)
                : defaultValue;
    }

    private int getIntWithDisplayFallback(String primaryTag, String legacyDisplayTag, int defaultValue) {
        if (this.entityTag.contains(primaryTag, Tag.TAG_INT)) {
            return this.entityTag.getInt(primaryTag);
        }

        CompoundTag display = this.getLegacyDisplayTag();
        return display != null && display.contains(legacyDisplayTag, Tag.TAG_INT)
                ? display.getInt(legacyDisplayTag)
                : defaultValue;
    }

    private CompoundTag getLegacyMinorTag() {
        if (!this.entityTag.contains(LEGACY_EXTENDED_PROPERTIES_TAG, Tag.TAG_COMPOUND)) {
            return null;
        }

        CompoundTag extended = this.entityTag.getCompound(LEGACY_EXTENDED_PROPERTIES_TAG);
        return extended.contains(LEGACY_MINOR_TAG, Tag.TAG_COMPOUND)
                ? extended.getCompound(LEGACY_MINOR_TAG)
                : null;
    }

    private CompoundTag getLegacyDisplayTag() {
        if (this.entityTag.contains(LEGACY_DISPLAY_TAG, Tag.TAG_COMPOUND)) {
            return this.entityTag.getCompound(LEGACY_DISPLAY_TAG);
        }

        if (!this.entityTag.contains(LEGACY_EXTENDED_PROPERTIES_TAG, Tag.TAG_COMPOUND)) {
            return null;
        }

        CompoundTag extended = this.entityTag.getCompound(LEGACY_EXTENDED_PROPERTIES_TAG);
        return extended.contains(LEGACY_DISPLAY_TAG, Tag.TAG_COMPOUND)
                ? extended.getCompound(LEGACY_DISPLAY_TAG)
                : null;
    }

    private boolean getBooleanWithLegacyFlagsFallback(String primaryTag, String legacyTag, boolean defaultValue) {
        if (this.entityTag.contains(primaryTag, Tag.TAG_BYTE)) {
            return this.entityTag.getBoolean(primaryTag);
        }

        if (!this.entityTag.contains(LEGACY_EXTENDED_PROPERTIES_TAG, Tag.TAG_COMPOUND)) {
            return defaultValue;
        }

        CompoundTag extended = this.entityTag.getCompound(LEGACY_EXTENDED_PROPERTIES_TAG);
        if (!extended.contains(LEGACY_FLAGS_TAG, Tag.TAG_COMPOUND)) {
            return defaultValue;
        }

        CompoundTag flags = extended.getCompound(LEGACY_FLAGS_TAG);
        return flags.contains(legacyTag, Tag.TAG_BYTE) ? flags.getBoolean(legacyTag) : defaultValue;
    }
}
