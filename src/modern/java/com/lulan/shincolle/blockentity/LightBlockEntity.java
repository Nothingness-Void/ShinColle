package com.lulan.shincolle.blockentity;

import com.lulan.shincolle.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Temporary light block entity mirroring 1.12.2 {@code TileEntityLightBlock},
 * restoring air or source/flowing water when its lifetime expires.
 */
public class LightBlockEntity extends BlockEntity {

    public static final int DEFAULT_LIFE_TICKS = 120;
    private static final String LIFE_TAG = "Life";
    private static final String WATER_TYPE_TAG = "WaterType";
    private static final String FLUID_LEVEL_TAG = "FluidLevel";

    private int lifeTicks = DEFAULT_LIFE_TICKS;
    private int waterType;
    private int fluidLevel;

    public LightBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.LIGHT_BLOCK.get(), pos, state);
    }

    public int getLifeTicks() {
        return this.lifeTicks;
    }

    public void setLifeTicks(int lifeTicks) {
        this.lifeTicks = Math.max(1, lifeTicks);
        this.setChanged();
    }

    public void setRestoreAir() {
        this.waterType = 0;
        this.fluidLevel = 0;
        this.setChanged();
    }

    public void setRestoreWater(boolean source, int fluidLevel) {
        this.waterType = source ? 1 : 2;
        this.fluidLevel = Math.max(0, Math.min(15, fluidLevel));
        this.setChanged();
    }

    public void resetLifetime(int ticks) {
        this.lifeTicks = Math.max(1, ticks);
        this.setChanged();
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putInt(LIFE_TAG, this.lifeTicks);
        tag.putInt(WATER_TYPE_TAG, this.waterType);
        tag.putInt(FLUID_LEVEL_TAG, this.fluidLevel);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        this.lifeTicks = tag.contains(LIFE_TAG) ? Math.max(1, tag.getInt(LIFE_TAG)) : DEFAULT_LIFE_TICKS;
        this.waterType = tag.getInt(WATER_TYPE_TAG);
        this.fluidLevel = tag.getInt(FLUID_LEVEL_TAG);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, LightBlockEntity blockEntity) {
        if (level.isClientSide()) {
            return;
        }

        blockEntity.lifeTicks--;
        if (blockEntity.lifeTicks > 0) {
            return;
        }

        BlockState restoreState = switch (blockEntity.waterType) {
            case 1 -> Blocks.WATER.defaultBlockState();
            case 2 -> Blocks.WATER.defaultBlockState().setValue(LiquidBlock.LEVEL, blockEntity.fluidLevel);
            default -> Blocks.AIR.defaultBlockState();
        };
        level.setBlock(pos, restoreState, 3);
    }
}
