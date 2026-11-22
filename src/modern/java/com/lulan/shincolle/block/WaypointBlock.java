package com.lulan.shincolle.block;

import com.lulan.shincolle.blockentity.WaypointBlockEntity;
import com.lulan.shincolle.item.PointerItem;
import com.lulan.shincolle.item.WaypointBlockItem;
import com.lulan.shincolle.registry.ModBlockEntities;
import com.lulan.shincolle.registry.ModItems;
import com.lulan.shincolle.teitoku.TeitokuHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
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
    public boolean onDestroyedByPlayer(BlockState state, Level level, BlockPos pos, Player player,
                                       boolean willHarvest, net.minecraft.world.level.material.FluidState fluid) {
        if (!(level.getBlockEntity(pos) instanceof WaypointBlockEntity tile) || !tile.canDestroy(player)) {
            return false;
        }
        if (!level.isClientSide()) {
            ItemStack stack = new ItemStack(this);
            if (!player.getInventory().add(stack)) {
                level.addFreshEntity(new ItemEntity(level, player.getX(), player.getY() + 0.5D, player.getZ(), stack));
            }
        }
        return super.onDestroyedByPlayer(state, level, pos, player, willHarvest, fluid);
    }

    @Override
    public void playerDestroy(Level level, Player player, BlockPos pos, BlockState state,
                              @Nullable BlockEntity blockEntity, ItemStack tool) {
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.INVISIBLE;
    }

    public static boolean showsMarker(ItemStack held) {
        return held.getItem() instanceof WaypointBlockItem || held.is(ModItems.TARGETWRENCH.get())
                || held.getItem() instanceof PointerItem && PointerItem.getMode(held) < 3;
    }

    @Override
    public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide() ? createTickerHelper(type, ModBlockEntities.WAYPOINT.get(), WaypointBlockEntity::clientTick) : null;
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
