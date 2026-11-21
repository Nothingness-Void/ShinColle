package com.lulan.shincolle.block;

import com.lulan.shincolle.blockentity.WaypointBlockEntity;
import com.lulan.shincolle.registry.ModItems;
import com.lulan.shincolle.teitoku.TeitokuHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nullable;

public class WaypointBlock extends BaseEntityBlock {

    private static final VoxelShape SHAPE = Shapes.or(
            Block.box(4.0D, 0.0D, 4.0D, 12.0D, 2.0D, 12.0D),
            Shapes.or(
                    Block.box(2.0D, 2.0D, 6.0D, 14.0D, 4.0D, 10.0D),
                    Shapes.or(
                            Block.box(6.0D, 2.0D, 2.0D, 10.0D, 4.0D, 14.0D),
                            Shapes.or(
                                    Block.box(6.0D, 4.0D, 6.0D, 10.0D, 12.0D, 10.0D),
                                    Shapes.or(
                                            Block.box(4.0D, 12.0D, 4.0D, 12.0D, 14.0D, 12.0D),
                                            Block.box(7.0D, 14.0D, 7.0D, 9.0D, 16.0D, 9.0D))))));

    public WaypointBlock(Properties properties) {
        super(properties);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new WaypointBlockEntity(pos, state);
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);

        if (!level.isClientSide() && placer instanceof Player player && level.getBlockEntity(pos) instanceof WaypointBlockEntity waypoint) {
            waypoint.setOwner(player);
        }
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (!(level.getBlockEntity(pos) instanceof WaypointBlockEntity waypoint)) {
            return InteractionResult.PASS;
        }

        if (!level.isClientSide()) {
            level.sendBlockUpdated(pos, state, state, Block.UPDATE_CLIENTS);
            if (player != null && !player.isShiftKeyDown()
                    && player.getItemInHand(hand).is(ModItems.TARGETWRENCH.get())) {
                if (TeitokuHelper.getPlayerUid(player) == waypoint.getOwnerUid()) {
                    waypoint.cycleStayMode();
                    player.sendSystemMessage(Component.literal("Change waypoint stay time to: "
                            + WaypointBlockEntity.formatStayTicks(waypoint.getStayTicks())));
                    return InteractionResult.SUCCESS;
                }
                player.sendSystemMessage(Component.translatable("chat.shincolle.wrongowner")
                        .append(" " + waypoint.getOwnerUid()));
            }
        }

        return InteractionResult.PASS;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return Shapes.empty();
    }

    @Override
    public boolean isPathfindable(BlockState state, BlockGetter level, BlockPos pos, net.minecraft.world.level.pathfinder.PathComputationType pathComputationType) {
        return true;
    }

}
