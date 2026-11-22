package com.lulan.shincolle.registry;

import com.lulan.shincolle.ShinColle;
import com.lulan.shincolle.block.CraneBlock;
import com.lulan.shincolle.block.DeskBlock;
import com.lulan.shincolle.block.HeavyGrudgeBlock;
import com.lulan.shincolle.block.LegacyCoreBlock;
import com.lulan.shincolle.block.PolymetalServantBlock;
import com.lulan.shincolle.block.SmallShipyardBlock;
import com.lulan.shincolle.block.TemporaryLightBlock;
import com.lulan.shincolle.block.WaypointBlock;
import com.lulan.shincolle.blockentity.LegacyCoreBlockEntity;
import com.lulan.shincolle.item.HeavyGrudgeBlockItem;
import com.lulan.shincolle.item.WaypointBlockItem;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DropExperienceBlock;
import net.minecraft.world.level.block.FallingBlock;
import net.minecraft.world.level.block.TransparentBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.neoforged.neoforge.registries.DeferredHolder;

import java.util.function.Function;
import java.util.function.Supplier;

public final class ModBlocks {

    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(BuiltInRegistries.BLOCK, ShinColle.MOD_ID);

    public static final DeferredHolder<Block, Block> BLOCK_ABYSSIUM = register("blockabyssium",
            () -> new Block(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.METAL)
                    .strength(3.0F)
                    .requiresCorrectToolForDrops()
                    .sound(SoundType.METAL)));

    public static final DeferredHolder<Block, Block> BLOCK_CRANE = register("blockcrane",
            () -> new CraneBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.METAL)
                    .strength(1.0F, 10.0F)
                    .requiresCorrectToolForDrops()
                    .sound(SoundType.METAL)
                    .noOcclusion()));

    public static final DeferredHolder<Block, Block> BLOCK_DESK = register("blockdesk",
            () -> new DeskBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.WOOD)
                    .strength(1.0F, 60.0F)
                    .requiresCorrectToolForDrops()
                    .sound(SoundType.WOOD)
                    .noOcclusion()));

    public static final DeferredHolder<Block, Block> BLOCK_FRAME = register("blockframe",
            () -> new Block(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.METAL)
                    .strength(0.2F, 40.0F)
                    .requiresCorrectToolForDrops()
                    .sound(SoundType.METAL)
                    .noOcclusion()));

    public static final DeferredHolder<Block, Block> BLOCK_GRUDGE = register("blockgrudge",
            () -> new TransparentBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_GRAY)
                    .strength(1.0F, 200.0F)
                    .lightLevel(state -> 15)
                    .sound(SoundType.SAND)
                    .noOcclusion()));

    public static final DeferredHolder<Block, Block> BLOCK_GRUDGE_XP = register("blockgrudgexp",
            () -> new TransparentBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_LIGHT_BLUE)
                    .strength(1.0F, 200.0F)
                    .lightLevel(state -> 15)
                    .sound(SoundType.SAND)
                    .noOcclusion()));

    public static final DeferredHolder<Block, Block> BLOCK_GRUDGE_HEAVY = register("blockgrudgeheavy",
            () -> new HeavyGrudgeBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_PURPLE)
                    .strength(3.0F, 600.0F)
                    .lightLevel(state -> 15)
                    .sound(SoundType.SAND)
                    .noOcclusion()),
            block -> new HeavyGrudgeBlockItem(block.get(), new Item.Properties()));

    public static final DeferredHolder<Block, Block> BLOCK_GRUDGE_HEAVY_DECO = register("blockgrudgeheavydeco",
            () -> new Block(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_PURPLE)
                    .strength(3.0F, 300.0F)
                    .lightLevel(state -> 15)
                    .sound(SoundType.SAND)));

    public static final DeferredHolder<Block, Block> BLOCK_LIGHT_AIR = register("blocklightair",
            () -> new TemporaryLightBlock(false, BlockBehaviour.Properties.of()
                    .mapColor(MapColor.NONE)
                    .replaceable()
                    .instabreak()
                    .lightLevel(state -> 15)
                    .noOcclusion()
                    .noCollission()
                    .noLootTable()));

    public static final DeferredHolder<Block, Block> BLOCK_LIGHT_LIQUID = register("blocklightliquid",
            () -> new TemporaryLightBlock(true, BlockBehaviour.Properties.of()
                    .mapColor(MapColor.WATER)
                    .instabreak()
                    .lightLevel(state -> 15)
                    .noOcclusion()
                    .noCollission()
                    .noLootTable()));

    public static final DeferredHolder<Block, Block> BLOCK_POLYMETAL = register("blockpolymetal",
            () -> new PolymetalServantBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.METAL)
                    .strength(3.0F)
                    .requiresCorrectToolForDrops()
                    .sound(SoundType.METAL)
                    .noOcclusion()));

    public static final DeferredHolder<Block, Block> BLOCK_POLYMETAL_ORE = register("blockpolymetalore",
            () -> new DropExperienceBlock(UniformInt.of(1, 4), BlockBehaviour.Properties.of()
                    .mapColor(MapColor.STONE)
                    .strength(3.0F)
                    .requiresCorrectToolForDrops()));

    public static final DeferredHolder<Block, Block> BLOCK_POLYMETAL_GRAVEL = register("blockpolymetalgravel",
            () -> new com.lulan.shincolle.block.LegacyFallingBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.SAND)
                    .strength(0.8F)
                    .sound(SoundType.SAND)));

    public static final DeferredHolder<Block, Block> BLOCK_SMALL_SHIPYARD = register("blocksmallshipyard",
            () -> new SmallShipyardBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_ORANGE)
                    .strength(10.0F, 1200.0F)
                    .requiresCorrectToolForDrops()
                    .lightLevel(state -> 4)
                    .sound(SoundType.STONE)
                    .noOcclusion()));

    public static final DeferredHolder<Block, Block> BLOCK_VOL_BLOCK = register("blockvolblock",
            () -> new Block(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_BLACK)
                    .strength(3.0F, 200.0F)
                    .lightLevel(state -> 15)
                    .sound(SoundType.SAND)));

    public static final DeferredHolder<Block, Block> BLOCK_VOL_CORE = register("blockvolcore",
            () -> new LegacyCoreBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_BLACK)
                    .strength(6.0F, 600.0F)
                    .requiresCorrectToolForDrops()
                    .lightLevel(state -> 15)
                    .sound(SoundType.SAND),
                    LegacyCoreBlockEntity::newVolCore,
                    ModBlockEntities.VOL_CORE));

    public static final DeferredHolder<Block, Block> BLOCK_WAYPOINT = register("blockwaypoint",
            () -> new WaypointBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_BLUE)
                    .strength(0.4F, 5.0F)
                    .lightLevel(state -> 10)
                    .sound(SoundType.GLASS)
                    .noOcclusion()),
            block -> new WaypointBlockItem(block.get(), new Item.Properties()));

    private ModBlocks() {
    }

    private static <T extends Block> DeferredHolder<Block, T> register(String name, Supplier<T> supplier) {
        DeferredHolder<Block, T> block = BLOCKS.register(name, supplier);
        ModItems.registerBlockItem(name, block);
        return block;
    }

    private static <T extends Block> DeferredHolder<Block, T> register(String name, Supplier<T> supplier, Function<DeferredHolder<Block, T>, ? extends Item> itemFactory) {
        DeferredHolder<Block, T> block = BLOCKS.register(name, supplier);
        ModItems.registerItem(name, () -> itemFactory.apply(block));
        return block;
    }
}
