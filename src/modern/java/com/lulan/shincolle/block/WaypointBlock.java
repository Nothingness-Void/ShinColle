package com.lulan.shincolle.block;

import com.lulan.shincolle.blockentity.WaypointBlockEntity;
import com.lulan.shincolle.menu.WaypointTerminalMenu;
import com.lulan.shincolle.registry.ModItems;
import com.lulan.shincolle.registry.ModSoundEvents;
import com.lulan.shincolle.sound.ShinColleSoundHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleMenuProvider;
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
import net.minecraftforge.network.NetworkHooks;

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

        ItemStack stack = player.getItemInHand(hand);
        if (player.isShiftKeyDown()) {
            return InteractionResult.PASS;
        }

        if (!level.isClientSide()) {
            if (stack.is(ModItems.TARGETWRENCH.get())) {
                if (!waypoint.canEdit(player)) {
                    player.displayClientMessage(Component.translatable("chat.shincolle.wrongowner"), true);
                    return InteractionResult.CONSUME;
                }

                waypoint.cycleStayMode();
                ShinColleSoundHelper.playForPlayer(level, player, ModSoundEvents.SHIP_BELL.get(), 0.7F,
                        ShinColleSoundHelper.variedPitch(player, 1.0F, 0.08F));
                player.displayClientMessage(Component.translatable("chat.shincolle.waypoint.stay_changed", waypoint.getStayLabel()), true);
                player.displayClientMessage(labelWithPos("chat.shincolle.wrench.wplast", waypoint.getLastWaypoint(), ChatFormatting.LIGHT_PURPLE), false);
                player.displayClientMessage(labelWithPos("chat.shincolle.wrench.wpnext", waypoint.getNextWaypoint(), ChatFormatting.AQUA), false);
                player.displayClientMessage(labelWithPos("chat.shincolle.waypoint.paired_chest", waypoint.getPairedChest(), ChatFormatting.GOLD), false);
            } else if (player instanceof ServerPlayer serverPlayer) {
                ShinColleSoundHelper.playForPlayer(level, player, ModSoundEvents.SHIP_BELL.get(), 0.7F,
                        ShinColleSoundHelper.variedPitch(player, 1.0F, 0.08F));
                NetworkHooks.openScreen(serverPlayer,
                        new SimpleMenuProvider(
                                (containerId, inventory, ignoredPlayer) -> new WaypointTerminalMenu(containerId, inventory, pos),
                                Component.translatable("block.shincolle.blockwaypoint")),
                        buffer -> buffer.writeBlockPos(pos));

                if (!waypoint.canEdit(player)) {
                    player.displayClientMessage(Component.translatable("chat.shincolle.waypoint.readonly"), true);
                }
            }
        }

        return InteractionResult.sidedSuccess(level.isClientSide());
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

    private static Component labelWithPos(String key, @Nullable BlockPos pos, ChatFormatting color) {
        return Component.translatable(key)
                .append(Component.literal(" "))
                .append(formatPos(pos, color));
    }

    private static Component formatPos(@Nullable BlockPos pos, ChatFormatting color) {
        if (pos == null) {
            return Component.translatable("gui.shincolle.waypoint.none").withStyle(ChatFormatting.DARK_GRAY);
        }

        return Component.literal(pos.getX() + ", " + pos.getY() + ", " + pos.getZ()).withStyle(color);
    }
}
