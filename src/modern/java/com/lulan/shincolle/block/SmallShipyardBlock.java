package com.lulan.shincolle.block;

import com.lulan.shincolle.blockentity.SmallShipyardBlockEntity;
import com.lulan.shincolle.registry.ModBlockEntities;
import com.lulan.shincolle.registry.ModSoundEvents;
import com.lulan.shincolle.sound.ShinColleSoundHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.network.NetworkHooks;

import javax.annotation.Nullable;

public class SmallShipyardBlock extends BaseEntityBlock {

    public static final BooleanProperty ACTIVE = BooleanProperty.create("active");
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
    private static final double[][] SMOKE_POINTS = {
            {0.72D, 1.10D, 0.55D},
            {0.22D, 0.80D, 0.70D},
            {0.47D, 0.60D, 0.25D}
    };

    public SmallShipyardBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(ACTIVE, false)
                .setValue(FACING, Direction.NORTH));
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.ENTITYBLOCK_ANIMATED;
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState()
                .setValue(ACTIVE, false)
                .setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    public BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    public BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(ACTIVE, FACING);
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new SmallShipyardBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntityType) {
        return level.isClientSide() ? null : createTickerHelper(blockEntityType, ModBlockEntities.SMALL_SHIPYARD.get(), SmallShipyardBlockEntity::serverTick);
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        if (!level.isClientSide() && placer instanceof Player player && level.getBlockEntity(pos) instanceof SmallShipyardBlockEntity shipyard) {
            shipyard.setOwner(player);
        }
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (!(level.getBlockEntity(pos) instanceof SmallShipyardBlockEntity shipyard)) {
            return InteractionResult.PASS;
        }

        if (!level.isClientSide() && player instanceof ServerPlayer serverPlayer) {
            ShinColleSoundHelper.playForPlayer(level, player, ModSoundEvents.SHIP_BELL.get(), 0.7F,
                    ShinColleSoundHelper.variedPitch(player, 1.0F, 0.08F));
            NetworkHooks.openScreen(serverPlayer, shipyard, pos);

            if (!shipyard.canEdit(player)) {
                player.displayClientMessage(Component.translatable("chat.shincolle.shipyard.readonly"), true);
            }
        }

        return InteractionResult.sidedSuccess(level.isClientSide());
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (!state.getValue(ACTIVE)) {
            return;
        }

        Direction facing = state.getValue(FACING);
        int smokePattern = random.nextInt(3);
        spawnSmoke(level, pos, facing, smokePattern == 0 || smokePattern == 1 ? SMOKE_POINTS[0] : SMOKE_POINTS[1], 3);
        if (smokePattern == 0) {
            spawnSmoke(level, pos, facing, SMOKE_POINTS[2], 2);
        } else if (smokePattern == 1) {
            spawnSmoke(level, pos, facing, SMOKE_POINTS[1], 2);
        } else {
            spawnSmoke(level, pos, facing, SMOKE_POINTS[2], 2);
        }
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock()) && level.getBlockEntity(pos) instanceof SmallShipyardBlockEntity shipyard) {
            shipyard.dropContents();
        }

        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    @Override
    public boolean isPathfindable(BlockState state, BlockGetter level, BlockPos pos, net.minecraft.world.level.pathfinder.PathComputationType pathComputationType) {
        return false;
    }

    private static void spawnSmoke(Level level, BlockPos pos, Direction facing, double[] point, int amount) {
        double[] rotatedPoint = rotatePoint(facing, point);
        for (int i = 0; i < amount; i++) {
            level.addParticle(ParticleTypes.SMOKE, pos.getX() + rotatedPoint[0], pos.getY() + rotatedPoint[1] + i * 0.1D,
                    pos.getZ() + rotatedPoint[2], 0.0D, i * 0.005D, 0.0D);
        }
    }

    private static double[] rotatePoint(Direction facing, double[] point) {
        double offsetX = point[0] - 0.5D;
        double offsetZ = point[2] - 0.5D;

        return switch (facing) {
            case EAST -> new double[] {0.5D - offsetZ, point[1], 0.5D + offsetX};
            case SOUTH -> new double[] {0.5D - offsetX, point[1], 0.5D - offsetZ};
            case WEST -> new double[] {0.5D + offsetZ, point[1], 0.5D - offsetX};
            default -> point;
        };
    }
}
