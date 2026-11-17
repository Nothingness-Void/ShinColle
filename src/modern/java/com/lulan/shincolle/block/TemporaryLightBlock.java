package com.lulan.shincolle.block;

import com.lulan.shincolle.blockentity.LightBlockEntity;
import com.lulan.shincolle.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nullable;

/**
 * Dynamic invisible light block mirroring 1.12.2 {@code BlockLightAir} and {@code BlockLightLiquid}.
 */
public class TemporaryLightBlock extends Block implements EntityBlock {

    private final boolean liquid;

    public TemporaryLightBlock(boolean liquid, Properties properties) {
        super(properties);
        this.liquid = liquid;
    }

    public boolean isLiquidVariant() {
        return this.liquid;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new LightBlockEntity(pos, state);
    }

    @Override
    public @Nullable <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide() || type != ModBlockEntities.LIGHT_BLOCK.get()) {
            return null;
        }
        return (lvl, blockPos, blockState, be) -> {
            if (be instanceof LightBlockEntity lightBlock) {
                LightBlockEntity.serverTick(lvl, blockPos, blockState, lightBlock);
            }
        };
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.INVISIBLE;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return Shapes.empty();
    }

    @Override
    public FluidState getFluidState(BlockState state) {
        return this.liquid ? Fluids.WATER.getSource(false) : super.getFluidState(state);
    }
}
