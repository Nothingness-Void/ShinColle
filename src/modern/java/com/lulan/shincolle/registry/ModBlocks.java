package com.lulan.shincolle.registry;

import com.lulan.shincolle.ShinColle;
import com.lulan.shincolle.block.LegacyInteractiveBlock;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DropExperienceBlock;
import net.minecraft.world.level.block.FallingBlock;
import net.minecraft.world.level.block.GlassBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.function.Supplier;

public final class ModBlocks {

    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, ShinColle.MOD_ID);

    public static final RegistryObject<Block> BLOCK_ABYSSIUM = register("blockabyssium",
            () -> new Block(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.METAL)
                    .strength(3.0F)
                    .requiresCorrectToolForDrops()
                    .sound(SoundType.METAL)));

    public static final RegistryObject<Block> BLOCK_CRANE = register("blockcrane",
            () -> new LegacyInteractiveBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.METAL)
                    .strength(1.0F, 10.0F)
                    .requiresCorrectToolForDrops()
                    .sound(SoundType.METAL)
                    .noOcclusion(), "gui.shincolle.placeholder.block.crane"));

    public static final RegistryObject<Block> BLOCK_DESK = register("blockdesk",
            () -> new LegacyInteractiveBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.WOOD)
                    .strength(1.0F, 60.0F)
                    .requiresCorrectToolForDrops()
                    .sound(SoundType.WOOD)
                    .noOcclusion(), "gui.shincolle.placeholder.block.desk"));

    public static final RegistryObject<Block> BLOCK_FRAME = register("blockframe",
            () -> new Block(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.METAL)
                    .strength(0.2F, 40.0F)
                    .requiresCorrectToolForDrops()
                    .sound(SoundType.METAL)
                    .noOcclusion()));

    public static final RegistryObject<Block> BLOCK_GRUDGE = register("blockgrudge",
            () -> new GlassBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_GRAY)
                    .strength(1.0F, 200.0F)
                    .lightLevel(state -> 15)
                    .sound(SoundType.SAND)
                    .noOcclusion()));

    public static final RegistryObject<Block> BLOCK_GRUDGE_XP = register("blockgrudgexp",
            () -> new GlassBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_LIGHT_BLUE)
                    .strength(1.0F, 200.0F)
                    .lightLevel(state -> 15)
                    .sound(SoundType.SAND)
                    .noOcclusion()));

    public static final RegistryObject<Block> BLOCK_GRUDGE_HEAVY = register("blockgrudgeheavy",
            () -> new LegacyInteractiveBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_PURPLE)
                    .strength(3.0F, 600.0F)
                    .lightLevel(state -> 15)
                    .sound(SoundType.SAND), "gui.shincolle.placeholder.block.grudgeheavy"));

    public static final RegistryObject<Block> BLOCK_GRUDGE_HEAVY_DECO = register("blockgrudgeheavydeco",
            () -> new Block(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_PURPLE)
                    .strength(3.0F, 300.0F)
                    .lightLevel(state -> 15)
                    .sound(SoundType.SAND)));

    public static final RegistryObject<Block> BLOCK_LIGHT_AIR = register("blocklightair",
            () -> new Block(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.NONE)
                    .replaceable()
                    .instabreak()
                    .noOcclusion()
                    .noCollission()));

    public static final RegistryObject<Block> BLOCK_LIGHT_LIQUID = register("blocklightliquid",
            () -> new Block(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.WATER)
                    .replaceable()
                    .instabreak()
                    .noOcclusion()
                    .noCollission()));

    public static final RegistryObject<Block> BLOCK_POLYMETAL = register("blockpolymetal",
            () -> new LegacyInteractiveBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.METAL)
                    .strength(3.0F)
                    .requiresCorrectToolForDrops()
                    .sound(SoundType.METAL), "gui.shincolle.placeholder.block.polymetal"));

    public static final RegistryObject<Block> BLOCK_POLYMETAL_ORE = register("blockpolymetalore",
            () -> new DropExperienceBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.STONE)
                    .strength(3.0F)
                    .requiresCorrectToolForDrops(), UniformInt.of(1, 4)));

    public static final RegistryObject<Block> BLOCK_POLYMETAL_GRAVEL = register("blockpolymetalgravel",
            () -> new FallingBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.SAND)
                    .strength(0.8F)
                    .sound(SoundType.SAND)));

    public static final RegistryObject<Block> BLOCK_SMALL_SHIPYARD = register("blocksmallshipyard",
            () -> new LegacyInteractiveBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_ORANGE)
                    .strength(10.0F, 1200.0F)
                    .requiresCorrectToolForDrops()
                    .lightLevel(state -> 4)
                    .sound(SoundType.STONE)
                    .noOcclusion(), "gui.shincolle.placeholder.block.shipyard"));

    public static final RegistryObject<Block> BLOCK_VOL_BLOCK = register("blockvolblock",
            () -> new Block(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_BLACK)
                    .strength(3.0F, 200.0F)
                    .lightLevel(state -> 15)
                    .sound(SoundType.SAND)));

    public static final RegistryObject<Block> BLOCK_VOL_CORE = register("blockvolcore",
            () -> new LegacyInteractiveBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_BLACK)
                    .strength(6.0F, 600.0F)
                    .requiresCorrectToolForDrops()
                    .lightLevel(state -> 15)
                    .sound(SoundType.SAND), "gui.shincolle.placeholder.block.volcore"));

    public static final RegistryObject<Block> BLOCK_WAYPOINT = register("blockwaypoint",
            () -> new LegacyInteractiveBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_BLUE)
                    .strength(0.4F, 5.0F)
                    .lightLevel(state -> 10)
                    .sound(SoundType.GLASS)
                    .noOcclusion(), "gui.shincolle.placeholder.block.waypoint"));

    private ModBlocks() {
    }

    private static <T extends Block> RegistryObject<T> register(String name, Supplier<T> supplier) {
        RegistryObject<T> block = BLOCKS.register(name, supplier);
        ModItems.registerBlockItem(name, block);
        return block;
    }
}
