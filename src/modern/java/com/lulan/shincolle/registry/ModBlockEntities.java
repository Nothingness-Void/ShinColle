package com.lulan.shincolle.registry;

import com.lulan.shincolle.ShinColle;
import com.lulan.shincolle.blockentity.CraneBlockEntity;
import com.lulan.shincolle.blockentity.DeskBlockEntity;
import com.lulan.shincolle.blockentity.SmallShipyardBlockEntity;
import com.lulan.shincolle.blockentity.WaypointBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModBlockEntities {

    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, ShinColle.MOD_ID);

    public static final RegistryObject<BlockEntityType<CraneBlockEntity>> CRANE = BLOCK_ENTITIES.register("crane",
            () -> BlockEntityType.Builder.of(CraneBlockEntity::new, ModBlocks.BLOCK_CRANE.get()).build(null));
    public static final RegistryObject<BlockEntityType<DeskBlockEntity>> DESK = BLOCK_ENTITIES.register("desk",
            () -> BlockEntityType.Builder.of(DeskBlockEntity::new, ModBlocks.BLOCK_DESK.get()).build(null));
    public static final RegistryObject<BlockEntityType<SmallShipyardBlockEntity>> SMALL_SHIPYARD = BLOCK_ENTITIES.register("small_shipyard",
            () -> BlockEntityType.Builder.of(SmallShipyardBlockEntity::new, ModBlocks.BLOCK_SMALL_SHIPYARD.get()).build(null));
    public static final RegistryObject<BlockEntityType<WaypointBlockEntity>> WAYPOINT = BLOCK_ENTITIES.register("waypoint",
            () -> BlockEntityType.Builder.of(WaypointBlockEntity::new, ModBlocks.BLOCK_WAYPOINT.get()).build(null));

    private ModBlockEntities() {
    }
}
