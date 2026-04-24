package com.lulan.shincolle.teitoku;

import com.lulan.shincolle.entity.ship.LegacyShipEntity;
import com.lulan.shincolle.entity.ship.ShipEntitySpec;
import com.lulan.shincolle.entity.ship.ShipEntitySpecs;
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
    private static final String LEGACY_LIGHT_AMMO_TAG = "NumAmmoLight";
    private static final String LEGACY_HEAVY_AMMO_TAG = "NumAmmoHeavy";
    private static final String LEGACY_GRUDGE_TAG = "NumGrudge";
    private static final String SHIP_MARRIED_TAG = "ShipMarried";
    private static final String COMMAND_POS_TAG = "CommandPos";
    private static final String GUARD_ENTITY_TAG = "GuardEntity";
    private static final String ROUTE_NODE_TAG = "RouteNode";

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
        return this.entityTag.getInt(SHIP_LEVEL_TAG);
    }

    public int getMorale() {
        return this.entityTag.getInt(SHIP_MORALE_TAG);
    }

    public int getShipFuel() {
        return this.entityTag.getInt(SHIP_FUEL_TAG);
    }

    public int getLightAmmo() {
        return this.getIntWithFallback(SHIP_LIGHT_AMMO_TAG, LEGACY_LIGHT_AMMO_TAG);
    }

    public int getHeavyAmmo() {
        return this.getIntWithFallback(SHIP_HEAVY_AMMO_TAG, LEGACY_HEAVY_AMMO_TAG);
    }

    public int getGrudge() {
        return this.getIntWithFallback(SHIP_GRUDGE_TAG, LEGACY_GRUDGE_TAG);
    }

    public boolean isMarried() {
        return this.entityTag.getBoolean(SHIP_MARRIED_TAG);
    }

    public boolean isOrderedToSit() {
        return this.entityTag.getBoolean(ORDERED_TO_SIT_TAG);
    }

    public boolean hasCommandState() {
        return this.entityTag.contains(COMMAND_POS_TAG, Tag.TAG_LONG)
                || this.entityTag.hasUUID(GUARD_ENTITY_TAG)
                || this.entityTag.contains(ROUTE_NODE_TAG, Tag.TAG_LONG);
    }

    public String getPositionText() {
        return this.posX + ", " + this.posY + ", " + this.posZ;
    }

    private int getIntWithFallback(String primaryTag, String legacyTag) {
        if (this.entityTag.contains(primaryTag, Tag.TAG_INT)) {
            return this.entityTag.getInt(primaryTag);
        }

        return this.entityTag.contains(legacyTag, Tag.TAG_INT) ? this.entityTag.getInt(legacyTag) : 0;
    }
}
