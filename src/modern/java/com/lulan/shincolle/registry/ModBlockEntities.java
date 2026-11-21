package com.lulan.shincolle.registry;

import com.lulan.shincolle.ShinColle;
import com.lulan.shincolle.blockentity.CraneBlockEntity;
import com.lulan.shincolle.blockentity.DeskBlockEntity;
import com.lulan.shincolle.blockentity.HeavyGrudgeBlockEntity;
import com.lulan.shincolle.blockentity.LegacyCoreBlockEntity;
import com.lulan.shincolle.blockentity.LightBlockEntity;
import com.lulan.shincolle.blockentity.PolymetalServantBlockEntity;
import com.lulan.shincolle.blockentity.SmallShipyardBlockEntity;
import com.lulan.shincolle.blockentity.WaypointBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.neoforged.neoforge.registries.DeferredHolder;

public final class ModBlockEntities {

    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(BuiltInRegistries.BLOCK_ENTITY_TYPE, ShinColle.MOD_ID);

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<CraneBlockEntity>> CRANE = BLOCK_ENTITIES.register("crane",
            () -> BlockEntityType.Builder.of(CraneBlockEntity::new, ModBlocks.BLOCK_CRANE.get()).build(null));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<DeskBlockEntity>> DESK = BLOCK_ENTITIES.register("desk",
            () -> BlockEntityType.Builder.of(DeskBlockEntity::new, ModBlocks.BLOCK_DESK.get()).build(null));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<HeavyGrudgeBlockEntity>> GRUDGE_HEAVY = BLOCK_ENTITIES.register("grudge_heavy",
            () -> BlockEntityType.Builder.of(HeavyGrudgeBlockEntity::new, ModBlocks.BLOCK_GRUDGE_HEAVY.get()).build(null));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<PolymetalServantBlockEntity>> POLYMETAL = BLOCK_ENTITIES.register("polymetal",
            () -> BlockEntityType.Builder.of(PolymetalServantBlockEntity::new, ModBlocks.BLOCK_POLYMETAL.get()).build(null));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<SmallShipyardBlockEntity>> SMALL_SHIPYARD = BLOCK_ENTITIES.register("small_shipyard",
            () -> BlockEntityType.Builder.of(SmallShipyardBlockEntity::new, ModBlocks.BLOCK_SMALL_SHIPYARD.get()).build(null));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<LegacyCoreBlockEntity>> VOL_CORE = BLOCK_ENTITIES.register("vol_core",
            () -> BlockEntityType.Builder.of(LegacyCoreBlockEntity::newVolCore, ModBlocks.BLOCK_VOL_CORE.get()).build(null));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<WaypointBlockEntity>> WAYPOINT = BLOCK_ENTITIES.register("waypoint",
            () -> BlockEntityType.Builder.of(WaypointBlockEntity::new, ModBlocks.BLOCK_WAYPOINT.get()).build(null));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<LightBlockEntity>> LIGHT_BLOCK = BLOCK_ENTITIES.register("light_block",
            () -> BlockEntityType.Builder.of(LightBlockEntity::new,
                    ModBlocks.BLOCK_LIGHT_AIR.get(),
                    ModBlocks.BLOCK_LIGHT_LIQUID.get()).build(null));

    private ModBlockEntities() {
    }
}
