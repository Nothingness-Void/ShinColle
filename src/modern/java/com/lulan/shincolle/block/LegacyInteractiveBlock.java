package com.lulan.shincolle.block;

import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.BlockHitResult;

public class LegacyInteractiveBlock extends Block {

    private final String messageKey;

    public LegacyInteractiveBlock(Properties properties, String messageKey) {
        super(properties);
        this.messageKey = messageKey;
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (!level.isClientSide()) {
            player.displayClientMessage(Component.translatable(this.messageKey), true);
        }

        return InteractionResult.sidedSuccess(level.isClientSide());
    }

    @Override
    public boolean isPathfindable(BlockState state, net.minecraft.world.level.BlockGetter level, BlockPos pos, net.minecraft.world.level.pathfinder.PathComputationType pathComputationType) {
        return false;
    }
}
