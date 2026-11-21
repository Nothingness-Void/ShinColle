package com.lulan.shincolle.world;

import com.lulan.shincolle.config.ShinColleConfig;
import com.lulan.shincolle.registry.ModBlocks;
import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

/**
 * 1.12.2 ShinColleWorldGen.generateSea + WorldGenPolyGravel, run once per ocean chunk.
 * getTopSolidOrLiquidBlock (seabed + 1) maps to the OCEAN_FLOOR_WG heightmap.
 */
public class PolymetalGravelFeature extends Feature<NoneFeatureConfiguration> {

    public PolymetalGravelFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        RandomSource rand = context.random();
        int chunkX = context.origin().getX() & ~15;
        int chunkZ = context.origin().getZ() & ~15;
        boolean[] replace = ShinColleConfig.polyGravelBaseBlock();
        int numberOfBlocks = 2 + rand.nextInt(2);
        boolean placed = false;

        for (int i = 0; i < ShinColleConfig.polyGravelBaseRate(); i++) {
            int x = chunkX + rand.nextInt(16);
            int z = chunkZ + rand.nextInt(16);
            int y = level.getHeight(Heightmap.Types.OCEAN_FLOOR_WG, x, z);
            placed |= generate(level, rand, new BlockPos(x, y, z), numberOfBlocks, replace);
        }
        return placed;
    }

    public static boolean generate(WorldGenLevel level, RandomSource rand, BlockPos pos, int numberOfBlocks, boolean[] replace) {
        boolean notFrozen = true;
        int x = pos.getX();
        int y = pos.getY();
        int z = pos.getZ();

        // frozen ocean: the top block is ice, search the seabed below (water depth >= 3)
        if (isIce(level.getBlockState(pos.below(1))) && isWater(level, pos.below(2))) {
            for (int newy = y - 3; newy > 3; newy--) {
                if (!isWater(level, new BlockPos(x, newy, z))) {
                    y = newy;
                    notFrozen = false;
                    break;
                }
            }
        }

        if (notFrozen && (!isWater(level, new BlockPos(x, y, z)) || y > 55)) {
            return false;
        }

        int l = rand.nextInt(numberOfBlocks - 1) + 1;
        int thickness = 1;
        BlockState gravel = ModBlocks.BLOCK_POLYMETAL_GRAVEL.get().defaultBlockState();
        for (int i1 = x - l; i1 <= x + l; ++i1) {
            for (int j1 = z - l; j1 <= z + l; ++j1) {
                int k1 = i1 - x;
                int l1 = j1 - z;
                if (k1 * k1 + l1 * l1 <= l * l) {
                    for (int i2 = y - thickness; i2 <= y + thickness; ++i2) {
                        BlockPos target = new BlockPos(i1, i2, j1);
                        if (isReplaceable(level.getBlockState(target), replace)) {
                            level.setBlock(target, gravel, 2);
                        }
                    }
                }
            }
        }
        return true;
    }

    /** 1.12 Blocks.STONE / GRAVEL / SAND / DIRT including their pre-flattening metadata variants. */
    public static boolean isReplaceable(BlockState state, boolean[] replace) {
        return (replace[0] && (state.is(Blocks.STONE) || state.is(Blocks.GRANITE) || state.is(Blocks.DIORITE) || state.is(Blocks.ANDESITE)))
                || (replace[1] && state.is(Blocks.GRAVEL))
                || (replace[2] && (state.is(Blocks.SAND) || state.is(Blocks.RED_SAND)))
                || (replace[3] && (state.is(Blocks.DIRT) || state.is(Blocks.COARSE_DIRT) || state.is(Blocks.PODZOL)));
    }

    private static boolean isIce(BlockState state) {
        return state.is(Blocks.ICE) || state.is(Blocks.FROSTED_ICE);
    }

    private static boolean isWater(WorldGenLevel level, BlockPos pos) {
        return level.getFluidState(pos).is(FluidTags.WATER);
    }
}
