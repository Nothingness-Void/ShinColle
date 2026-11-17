package com.lulan.shincolle.world;

import com.lulan.shincolle.blockentity.LightBlockEntity;
import com.lulan.shincolle.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;

/**
 * Dynamic light block placement helper mirroring 1.12.2 {@code BlockHelper.placeLightBlock}.
 */
public final class LightBlockHelper {

    private LightBlockHelper() {
    }

    public static boolean placeLightBlock(Level level, BlockPos center) {
        return placeLightBlock(level, center, LightBlockEntity.DEFAULT_LIFE_TICKS);
    }

    public static boolean placeLightBlock(Level level, BlockPos center, int lifeTicks) {
        if (level == null || level.isClientSide() || center == null) {
            return false;
        }

        if (refreshNearbyLightBlock(level, center, lifeTicks)) {
            return true;
        }

        for (int dy = 0; dy <= 1; dy++) {
            for (int dx = -1; dx <= 1; dx++) {
                for (int dz = -1; dz <= 1; dz++) {
                    BlockPos pos = center.offset(dx, dy, dz);
                    if (!level.isInWorldBounds(pos)) {
                        continue;
                    }
                    BlockState existing = level.getBlockState(pos);
                    if (existing.isAir()) {
                        level.setBlock(pos, ModBlocks.BLOCK_LIGHT_AIR.get().defaultBlockState(), 3);
                        if (level.getBlockEntity(pos) instanceof LightBlockEntity lightEntity) {
                            lightEntity.setRestoreAir();
                            lightEntity.setLifeTicks(lifeTicks);
                        }
                        return true;
                    }
                    if (existing.is(Blocks.WATER)) {
                        boolean source = existing.getFluidState().is(Fluids.WATER);
                        int fluidLevel = existing.hasProperty(LiquidBlock.LEVEL) ? existing.getValue(LiquidBlock.LEVEL) : 0;
                        level.setBlock(pos, ModBlocks.BLOCK_LIGHT_LIQUID.get().defaultBlockState(), 3);
                        if (level.getBlockEntity(pos) instanceof LightBlockEntity lightEntity) {
                            lightEntity.setRestoreWater(source, fluidLevel);
                            lightEntity.setLifeTicks(lifeTicks);
                        }
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public static boolean refreshNearbyLightBlock(Level level, BlockPos center, int lifeTicks) {
        for (int dy = -1; dy <= 2; dy++) {
            for (int dx = -1; dx <= 1; dx++) {
                for (int dz = -1; dz <= 1; dz++) {
                    BlockPos pos = center.offset(dx, dy, dz);
                    if (level.getBlockEntity(pos) instanceof LightBlockEntity lightEntity) {
                        lightEntity.resetLifetime(lifeTicks);
                        return true;
                    }
                }
            }
        }
        return false;
    }
}
