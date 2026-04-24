package com.lulan.shincolle.block;

import com.lulan.shincolle.blockentity.HeavyGrudgeBlockEntity;
import com.lulan.shincolle.registry.ModBlockEntities;
import com.lulan.shincolle.registry.ModSoundEvents;
import com.lulan.shincolle.sound.ShinColleSoundHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
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
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.network.NetworkHooks;

import javax.annotation.Nullable;

public class HeavyGrudgeBlock extends BaseEntityBlock {

    public static final IntegerProperty MBS = IntegerProperty.create("mbs", 0, 2);

    public HeavyGrudgeBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(MBS, 0));
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return state.getValue(MBS) > 0 ? RenderShape.ENTITYBLOCK_ANIMATED : RenderShape.MODEL;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(MBS);
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new HeavyGrudgeBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntityType) {
        return level.isClientSide() ? null : createTickerHelper(blockEntityType, ModBlockEntities.GRUDGE_HEAVY.get(), HeavyGrudgeBlockEntity::serverTick);
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        if (!level.isClientSide() && level.getBlockEntity(pos) instanceof HeavyGrudgeBlockEntity heavy) {
            level.setBlock(pos, state.setValue(MBS, 0), Block.UPDATE_ALL);
            if (placer instanceof Player player) {
                heavy.setOwner(player);
            }
            heavy.restoreFromPlacedStack(stack);
        }
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (!(level.getBlockEntity(pos) instanceof HeavyGrudgeBlockEntity heavy)) {
            return InteractionResult.PASS;
        }

        if (!level.isClientSide() && player instanceof ServerPlayer serverPlayer) {
            if (!heavy.isStructureComplete()) {
                if (player.isShiftKeyDown() || !heavy.tryAssembleStructure()) {
                    return InteractionResult.PASS;
                }
                ShinColleSoundHelper.playForPlayer(level, player, ModSoundEvents.SHIP_BELL.get(), 0.7F,
                        ShinColleSoundHelper.variedPitch(player, 1.0F, 0.08F));
                return InteractionResult.SUCCESS;
            }

            ShinColleSoundHelper.playForPlayer(level, player, ModSoundEvents.SHIP_BELL.get(), 0.7F,
                    ShinColleSoundHelper.variedPitch(player, 1.0F, 0.08F));
            NetworkHooks.openScreen(serverPlayer, heavy, pos);

            if (!heavy.canEdit(player)) {
                player.displayClientMessage(Component.translatable("chat.shincolle.shipyard.readonly"), true);
            }
        }

        return InteractionResult.sidedSuccess(level.isClientSide());
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (!(level.getBlockEntity(pos) instanceof HeavyGrudgeBlockEntity heavy) || !heavy.isStructureComplete()) {
            return;
        }

        if (random.nextInt(6) != 0) {
            return;
        }

        double x = pos.getX() + 0.35D + random.nextDouble() * 0.3D;
        double y = pos.getY() + 1.02D;
        double z = pos.getZ() + 0.35D + random.nextDouble() * 0.3D;
        level.addParticle(net.minecraft.core.particles.ParticleTypes.SMOKE, x, y, z, 0.0D, 0.03D, 0.0D);
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock()) && level.getBlockEntity(pos) instanceof HeavyGrudgeBlockEntity heavy) {
            heavy.dropContents();
            heavy.resetStructure(false);
        }

        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    @Override
    public boolean isPathfindable(BlockState state, BlockGetter level, BlockPos pos, net.minecraft.world.level.pathfinder.PathComputationType pathComputationType) {
        return false;
    }
}
