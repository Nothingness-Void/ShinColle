package com.lulan.shincolle.item;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.fluids.FluidActionResult;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import net.minecraftforge.fluids.capability.templates.FluidHandlerItemStack;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;

public class ShipTankItem extends Item {

    private final int capacity;

    public ShipTankItem(Properties properties, int capacity) {
        super(properties);
        this.capacity = capacity;
    }

    @Override
    public @Nullable ICapabilityProvider initCapabilities(ItemStack stack, @Nullable CompoundTag nbt) {
        return new FluidHandlerItemStack(stack, this.capacity) {
            @Override
            protected void setContainerToEmpty() {
                if (this.container.hasTag()) {
                    this.container.getTag().remove(FLUID_NBT_KEY);
                }
            }
        };
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        BlockHitResult hitResult = getPlayerPOVHitResult(level, player, ClipContext.Fluid.SOURCE_ONLY);
        if (hitResult.getType() != HitResult.Type.MISS) {
            return InteractionResultHolder.pass(stack);
        }

        BlockPos placePos = getBlockInFrontOfPlayer(player);
        if (!level.mayInteract(player, placePos) || !player.mayUseItemAt(placePos, Direction.UP, stack)) {
            return InteractionResultHolder.fail(stack);
        }

        if (!level.isClientSide() && !tryPlaceContainedLiquid(player, level, placePos, stack)) {
            return InteractionResultHolder.fail(stack);
        }

        return InteractionResultHolder.sidedSuccess(player.getItemInHand(hand), level.isClientSide());
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        Player player = context.getPlayer();
        if (player == null) {
            return InteractionResult.PASS;
        }

        ItemStack stack = context.getItemInHand();
        BlockPos clickedPos = context.getClickedPos();
        Direction clickedFace = context.getClickedFace();

        BlockEntity blockEntity = level.getBlockEntity(clickedPos);
        if (blockEntity != null) {
            IFluidHandler blockHandler = blockEntity.getCapability(ForgeCapabilities.FLUID_HANDLER, clickedFace)
                    .resolve()
                    .orElse(null);
            if (blockHandler != null && transferFluid(player, context.getHand(), stack, blockHandler)) {
                return InteractionResult.sidedSuccess(level.isClientSide());
            }
        }

        FluidActionResult pickupResult = FluidUtil.tryPickUpFluid(stack, player, level, clickedPos, clickedFace);
        if (pickupResult.isSuccess()) {
            player.setItemInHand(context.getHand(), pickupResult.getResult());
            return InteractionResult.sidedSuccess(level.isClientSide());
        }

        BlockPos placePos = getPlacePos(context);
        if (tryPlaceContainedLiquid(player, level, placePos, stack)) {
            return InteractionResult.sidedSuccess(level.isClientSide());
        }

        return InteractionResult.PASS;
    }

    @Override
    public boolean isBarVisible(ItemStack stack) {
        return FluidUtil.getFluidContained(stack).filter(fluid -> !fluid.isEmpty()).isPresent();
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        int amount = FluidUtil.getFluidContained(stack).map(FluidStack::getAmount).orElse(0);
        return Math.round(13.0F * amount / (float) this.capacity);
    }

    @Override
    public int getBarColor(ItemStack stack) {
        return 0x3AC5FF;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("gui.shincolle.shiptank").withStyle(ChatFormatting.GRAY));

        Optional<FluidStack> containedFluid = FluidUtil.getFluidContained(stack);
        String name = containedFluid.filter(fluid -> !fluid.isEmpty())
                .map(fluid -> fluid.getDisplayName().getString())
                .orElse("");
        int amount = containedFluid.map(FluidStack::getAmount).orElse(0);

        tooltip.add(Component.literal(name)
                .withStyle(ChatFormatting.AQUA)
                .append(Component.literal((name.isEmpty() ? "" : " ") + amount + " / " + this.capacity + " mB").withStyle(ChatFormatting.WHITE)));
    }

    private static BlockPos getPlacePos(UseOnContext context) {
        BlockPos clickedPos = context.getClickedPos();
        BlockState clickedState = context.getLevel().getBlockState(clickedPos);
        return clickedState.canBeReplaced(new BlockPlaceContext(context)) ? clickedPos : clickedPos.relative(context.getClickedFace());
    }

    private static boolean transferFluid(Player player, InteractionHand hand, ItemStack stack, IFluidHandler blockHandler) {
        IFluidHandlerItem itemHandler = stack.getCapability(ForgeCapabilities.FLUID_HANDLER_ITEM).resolve().orElse(null);
        if (itemHandler == null) {
            return false;
        }

        FluidStack transferred = player.isShiftKeyDown()
                ? FluidUtil.tryFluidTransfer(itemHandler, blockHandler, 1000, true)
                : FluidUtil.tryFluidTransfer(blockHandler, itemHandler, 1000, true);
        if (transferred == null || transferred.isEmpty()) {
            return false;
        }

        player.setItemInHand(hand, itemHandler.getContainer());
        return true;
    }

    private static boolean tryPlaceContainedLiquid(@Nullable Player player, Level level, BlockPos pos, ItemStack stack) {
        IFluidHandlerItem itemHandler = stack.getCapability(ForgeCapabilities.FLUID_HANDLER_ITEM).resolve().orElse(null);
        if (itemHandler == null) {
            return false;
        }

        FluidStack drained = itemHandler.drain(1000, IFluidHandler.FluidAction.SIMULATE);
        if (drained.isEmpty() || drained.getAmount() < 1000) {
            return false;
        }

        Fluid fluid = drained.getFluid();
        BlockState fluidState = fluid.defaultFluidState().createLegacyBlock();
        if (fluidState.isAir()) {
            return false;
        }

        BlockState existingState = level.getBlockState(pos);
        boolean replaceable = level.isEmptyBlock(pos) || existingState.canBeReplaced() || !existingState.getFluidState().isEmpty();
        if (!replaceable) {
            return false;
        }

        if (level.dimensionType().ultraWarm() && fluid == Fluids.WATER) {
            level.playSound(player, pos, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 0.5F,
                    2.6F + (level.random.nextFloat() - level.random.nextFloat()) * 0.8F);
            if (!level.isClientSide()) {
                for (int i = 0; i < 8; i++) {
                    level.addParticle(ParticleTypes.LARGE_SMOKE,
                            pos.getX() + level.random.nextDouble(),
                            pos.getY() + level.random.nextDouble(),
                            pos.getZ() + level.random.nextDouble(),
                            0.0D, 0.0D, 0.0D);
                }
            }

            itemHandler.drain(1000, IFluidHandler.FluidAction.EXECUTE);
            return true;
        }

        if (!level.isClientSide() && !level.isEmptyBlock(pos) && existingState.canBeReplaced() && existingState.getFluidState().isEmpty()) {
            level.destroyBlock(pos, true);
        }

        level.playSound(player, pos,
                fluid == Fluids.LAVA || fluid == Fluids.FLOWING_LAVA ? SoundEvents.BUCKET_EMPTY_LAVA : SoundEvents.BUCKET_EMPTY,
                SoundSource.BLOCKS, 1.0F, 1.0F);
        level.setBlock(pos, fluidState, 11);
        itemHandler.drain(1000, IFluidHandler.FluidAction.EXECUTE);
        level.updateNeighborsAt(pos, fluidState.getBlock());
        return true;
    }

    private static BlockPos getBlockInFrontOfPlayer(Player player) {
        float yaw = player.getYRot() % 360.0F;
        if (yaw > 180.0F) {
            yaw -= 360.0F;
        } else if (yaw < -180.0F) {
            yaw += 360.0F;
        }

        double x = player.getX();
        double y = player.getY() + 1.0D;
        double z = player.getZ();

        if (yaw <= -112.5F && yaw > -157.5F) {
            return BlockPos.containing(x + 1.0D, y, z - 1.0D);
        }
        if (yaw <= -67.5F && yaw > -112.5F) {
            return BlockPos.containing(x + 1.0D, y, z);
        }
        if (yaw <= -22.5F && yaw > -67.5F) {
            return BlockPos.containing(x + 1.0D, y, z + 1.0D);
        }
        if (yaw <= 22.5F && yaw > -22.5F) {
            return BlockPos.containing(x, y, z + 1.0D);
        }
        if (yaw <= 67.5F && yaw > 22.5F) {
            return BlockPos.containing(x - 1.0D, y, z + 1.0D);
        }
        if (yaw <= 112.5F && yaw > 67.5F) {
            return BlockPos.containing(x - 1.0D, y, z);
        }
        if (yaw <= 157.5F && yaw > 112.5F) {
            return BlockPos.containing(x - 1.0D, y, z - 1.0D);
        }

        return BlockPos.containing(x, y, z - 1.0D);
    }
}
