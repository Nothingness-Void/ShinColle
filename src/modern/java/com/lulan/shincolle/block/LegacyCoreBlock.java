package com.lulan.shincolle.block;

import com.lulan.shincolle.blockentity.LegacyCoreBlockEntity;
import com.lulan.shincolle.registry.ModSoundEvents;
import com.lulan.shincolle.sound.ShinColleSoundHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
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
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.network.NetworkHooks;

import javax.annotation.Nullable;
import java.util.function.BiFunction;
import java.util.function.Supplier;

public class LegacyCoreBlock extends BaseEntityBlock {

    private final BiFunction<BlockPos, BlockState, LegacyCoreBlockEntity> blockEntityFactory;
    private final Supplier<BlockEntityType<LegacyCoreBlockEntity>> typeSupplier;

    public LegacyCoreBlock(Properties properties, BiFunction<BlockPos, BlockState, LegacyCoreBlockEntity> blockEntityFactory,
                           Supplier<BlockEntityType<LegacyCoreBlockEntity>> typeSupplier) {
        super(properties);
        this.blockEntityFactory = blockEntityFactory;
        this.typeSupplier = typeSupplier;
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return this.blockEntityFactory.apply(pos, state);
    }

    @Override
    public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntityType) {
        return level.isClientSide() ? null : createTickerHelper(blockEntityType, this.typeSupplier.get(), LegacyCoreBlockEntity::serverTick);
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        if (!level.isClientSide() && placer instanceof Player player && level.getBlockEntity(pos) instanceof LegacyCoreBlockEntity core) {
            core.setOwner(player);
        }
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (!(level.getBlockEntity(pos) instanceof LegacyCoreBlockEntity core)) {
            return InteractionResult.PASS;
        }

        if (!level.isClientSide()) {
            if (player.isShiftKeyDown()) {
                if (!core.canEdit(player)) {
                    player.displayClientMessage(Component.translatable("chat.shincolle.wrongowner"), true);
                    player.displayClientMessage(Component.translatable("chat.shincolle.legacy_core.readonly", core.getBlockLabel()), true);
                } else {
                    core.cycleMode();
                    ShinColleSoundHelper.playForPlayer(level, player, ModSoundEvents.SHIP_BELL.get(), 0.7F,
                            ShinColleSoundHelper.variedPitch(player, 1.0F, 0.08F));
                    player.displayClientMessage(Component.translatable("chat.shincolle.legacy_core.mode_changed",
                            core.getBlockLabel(), core.getModeName()), true);
                }
            } else {
                ShinColleSoundHelper.playForPlayer(level, player, ModSoundEvents.SHIP_BELL.get(), 0.7F,
                        ShinColleSoundHelper.variedPitch(player, 1.0F, 0.08F));
                if (player instanceof ServerPlayer serverPlayer) {
                    NetworkHooks.openScreen(serverPlayer, core, pos);
                }
                if (!core.canEdit(player)) {
                    player.displayClientMessage(Component.translatable("chat.shincolle.legacy_core.readonly", core.getBlockLabel()), true);
                }
            }
        }

        return InteractionResult.sidedSuccess(level.isClientSide());
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock()) && level.getBlockEntity(pos) instanceof LegacyCoreBlockEntity core) {
            core.dropContents();
        }

        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    @Override
    public boolean isPathfindable(BlockState state, BlockGetter level, BlockPos pos, net.minecraft.world.level.pathfinder.PathComputationType pathComputationType) {
        return false;
    }
}
