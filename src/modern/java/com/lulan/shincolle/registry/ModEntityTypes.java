package com.lulan.shincolle.registry;

import com.lulan.shincolle.ShinColle;
import com.lulan.shincolle.entity.projectile.LegacyShipProjectileEntity;
import com.lulan.shincolle.entity.ship.LegacyShipEntity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModEntityTypes {

    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, ShinColle.MOD_ID);

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

    private ModEntityTypes() {
    }
}
