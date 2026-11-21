package com.lulan.shincolle.registry;

import com.lulan.shincolle.ShinColle;
import com.lulan.shincolle.entity.other.RecoveredShipItemEntity;
import com.lulan.shincolle.entity.mount.LegacyMountEntity;
import com.lulan.shincolle.entity.mount.LegacyShipMountEntity;
import com.lulan.shincolle.entity.other.LegacyShipFishingHookEntity;
import com.lulan.shincolle.entity.projectile.LegacyShipProjectileEntity;
import com.lulan.shincolle.entity.projectile.LegacyShipBeamEntity;
import com.lulan.shincolle.entity.projectile.LegacyShipStaticEffectEntity;
import com.lulan.shincolle.entity.projectile.LegacyShipFloatingFortEntity;
import com.lulan.shincolle.entity.ship.LegacyShipAirplaneEntity;
import com.lulan.shincolle.entity.ship.LegacyShipTakoyakiEntity;
import com.lulan.shincolle.entity.ship.LegacyShipEntity;
import com.lulan.shincolle.entity.ship.LegacyShipRensouhouEntity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModEntityTypes {

    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, ShinColle.MOD_ID);

    public static final RegistryObject<EntityType<RecoveredShipItemEntity>> RECOVERED_SHIP_ITEM =
            ENTITY_TYPES.register("recovered_ship_item", () -> EntityType.Builder
                    .<RecoveredShipItemEntity>of(RecoveredShipItemEntity::new, MobCategory.MISC)
                    .sized(0.25F, 0.25F).fireImmune().clientTrackingRange(6).updateInterval(20)
                    .build("recovered_ship_item"));

    public static final RegistryObject<EntityType<LegacyShipEntity>> LEGACY_SHIP = ENTITY_TYPES.register("legacy_ship",
            () -> EntityType.Builder.of(LegacyShipEntity::new, MobCategory.CREATURE)
                    .sized(0.62F, 1.82F)
                    .clientTrackingRange(10)
                    .updateInterval(2)
                    .build("legacy_ship"));
    public static final RegistryObject<EntityType<LegacyShipProjectileEntity>> LEGACY_SHIP_PROJECTILE =
            ENTITY_TYPES.register("legacy_ship_projectile",
                    () -> EntityType.Builder.<LegacyShipProjectileEntity>of(LegacyShipProjectileEntity::new, MobCategory.MISC)
                            .sized(0.25F, 0.25F)
                            .clientTrackingRange(8)
                            .updateInterval(1)
                            .build("legacy_ship_projectile"));
    public static final RegistryObject<EntityType<LegacyShipBeamEntity>> LEGACY_SHIP_BEAM =
            ENTITY_TYPES.register("legacy_ship_beam",
                    () -> EntityType.Builder.<LegacyShipBeamEntity>of(LegacyShipBeamEntity::new, MobCategory.MISC)
                            .sized(1.0F, 1.0F)
                            .clientTrackingRange(10)
                            .updateInterval(1)
                            .build("legacy_ship_beam"));
    public static final RegistryObject<EntityType<LegacyShipStaticEffectEntity>> LEGACY_SHIP_STATIC_EFFECT =
            ENTITY_TYPES.register("legacy_ship_static_effect",
                    () -> EntityType.Builder.<LegacyShipStaticEffectEntity>of(LegacyShipStaticEffectEntity::new, MobCategory.MISC)
                            .sized(0.25F, 0.25F)
                            .clientTrackingRange(10)
                            .updateInterval(1)
                            .build("legacy_ship_static_effect"));
    public static final RegistryObject<EntityType<LegacyShipFloatingFortEntity>> LEGACY_SHIP_FLOATING_FORT =
            ENTITY_TYPES.register("legacy_ship_floating_fort",
                    () -> EntityType.Builder.<LegacyShipFloatingFortEntity>of(LegacyShipFloatingFortEntity::new, MobCategory.MISC)
                            .sized(0.5F, 0.5F)
                            .clientTrackingRange(10)
                            .updateInterval(1)
                            .build("legacy_ship_floating_fort"));
    public static final RegistryObject<EntityType<LegacyShipAirplaneEntity>> LEGACY_SHIP_AIRCRAFT =
            ENTITY_TYPES.register("legacy_ship_aircraft",
                    () -> EntityType.Builder.<LegacyShipAirplaneEntity>of(LegacyShipAirplaneEntity::new, MobCategory.MISC)
                            .sized(0.85F, 0.38F)
                            .clientTrackingRange(10)
                            .updateInterval(1)
                            .build("legacy_ship_aircraft"));
    public static final RegistryObject<EntityType<LegacyShipTakoyakiEntity>> LEGACY_SHIP_TAKOYAKI =
            ENTITY_TYPES.register("legacy_ship_takoyaki",
                    () -> EntityType.Builder.<LegacyShipTakoyakiEntity>of(LegacyShipTakoyakiEntity::new, MobCategory.MISC)
                            .sized(0.95F, 0.42F)
                            .clientTrackingRange(10)
                            .updateInterval(1)
                            .build("legacy_ship_takoyaki"));
    public static final RegistryObject<EntityType<LegacyMountEntity>> LEGACY_MOUNT =
            ENTITY_TYPES.register("legacy_mount",
                    () -> EntityType.Builder.<LegacyMountEntity>of(LegacyMountEntity::new, MobCategory.MISC)
                            .sized(0.95F, 0.95F)
                            .clientTrackingRange(8)
                            .updateInterval(1)
                            .build("legacy_mount"));
    public static final RegistryObject<EntityType<LegacyShipMountEntity>> LEGACY_SHIP_MOUNT =
            ENTITY_TYPES.register("legacy_ship_mount",
                    () -> EntityType.Builder.<LegacyShipMountEntity>of(LegacyShipMountEntity::new, MobCategory.MISC)
                            .sized(2.5F, 3.1F)
                            .clientTrackingRange(10)
                            .updateInterval(1)
                            .build("legacy_ship_mount"));
    public static final RegistryObject<EntityType<LegacyShipRensouhouEntity>> LEGACY_RENSOUHOU =
            ENTITY_TYPES.register("legacy_rensouhou",
                    () -> EntityType.Builder.<LegacyShipRensouhouEntity>of(LegacyShipRensouhouEntity::new, MobCategory.MISC)
                            .sized(0.3F, 0.7F)
                            .clientTrackingRange(10)
                            .updateInterval(1)
                            .build("legacy_rensouhou"));
    public static final RegistryObject<EntityType<LegacyShipFishingHookEntity>> LEGACY_SHIP_FISHING_HOOK =
            ENTITY_TYPES.register("legacy_ship_fishing_hook",
                    () -> EntityType.Builder.<LegacyShipFishingHookEntity>of(LegacyShipFishingHookEntity::new, MobCategory.MISC)
                            .sized(0.25F, 0.25F)
                            .clientTrackingRange(64)
                            .updateInterval(1)
                            .build("legacy_ship_fishing_hook"));

    private ModEntityTypes() {
    }
}
