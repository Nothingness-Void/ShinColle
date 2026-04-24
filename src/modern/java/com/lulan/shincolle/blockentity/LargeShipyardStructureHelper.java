package com.lulan.shincolle.blockentity;

import com.lulan.shincolle.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public final class LargeShipyardStructureHelper {

    private LargeShipyardStructureHelper() {
    }

    public static boolean isValidMaster(Level level, BlockPos masterPos) {
        if (level == null || !level.getBlockState(masterPos).is(ModBlocks.BLOCK_GRUDGE_HEAVY.get())) {
            return false;
        }

        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -2; dy <= 0; dy++) {
                for (int dz = -1; dz <= 1; dz++) {
                    BlockPos checkPos = masterPos.offset(dx, dy, dz);
                    BlockState state = level.getBlockState(checkPos);
                    boolean shouldBeMaster = dx == 0 && dy == 0 && dz == 0;
                    boolean shouldBeServant = isServantOffset(dx, dy, dz);

                    if (shouldBeMaster) {
                        if (!state.is(ModBlocks.BLOCK_GRUDGE_HEAVY.get())) {
                            return false;
                        }
                        continue;
                    }

                    if (shouldBeServant) {
                        if (!state.is(ModBlocks.BLOCK_POLYMETAL.get())) {
                            return false;
                        }
                        continue;
                    }

                    if (state.is(ModBlocks.BLOCK_GRUDGE_HEAVY.get()) || state.is(ModBlocks.BLOCK_POLYMETAL.get())) {
                        return false;
                    }
                }
            }
        }

        return true;
    }

    public static boolean isServantPosition(BlockPos masterPos, BlockPos candidatePos) {
        int dx = candidatePos.getX() - masterPos.getX();
        int dy = candidatePos.getY() - masterPos.getY();
        int dz = candidatePos.getZ() - masterPos.getZ();
        return isServantOffset(dx, dy, dz);
    }

    public static List<BlockPos> getServantPositions(BlockPos masterPos) {
        List<BlockPos> positions = new ArrayList<>(13);

        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                positions.add(masterPos.offset(dx, -2, dz));
            }
        }

        positions.add(masterPos.offset(-1, -1, -1));
        positions.add(masterPos.offset(-1, -1, 1));
        positions.add(masterPos.offset(1, -1, -1));
        positions.add(masterPos.offset(1, -1, 1));
        return List.copyOf(positions);
    }

    public static @Nullable BlockPos findMasterForServant(Level level, BlockPos servantPos) {
        if (level == null) {
            return null;
        }

        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = 0; dy <= 2; dy++) {
                for (int dz = -1; dz <= 1; dz++) {
                    BlockPos masterPos = servantPos.offset(dx, dy, dz);
                    if (!level.getBlockState(masterPos).is(ModBlocks.BLOCK_GRUDGE_HEAVY.get())) {
                        continue;
                    }
                    if (!isServantPosition(masterPos, servantPos)) {
                        continue;
                    }
                    if (level.getBlockEntity(masterPos) instanceof HeavyGrudgeBlockEntity heavy
                            && heavy.isStructureComplete()
                            && isValidMaster(level, masterPos)) {
                        return masterPos.immutable();
                    }
                }
            }
        }

        return null;
    }

    private static boolean isServantOffset(int dx, int dy, int dz) {
        if (dy == -2) {
            return Math.abs(dx) <= 1 && Math.abs(dz) <= 1;
        }

        if (dy == -1) {
            return Math.abs(dx) == 1 && Math.abs(dz) == 1;
        }

        return false;
    }
}
