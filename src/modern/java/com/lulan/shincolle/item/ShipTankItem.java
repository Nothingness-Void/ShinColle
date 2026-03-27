package com.lulan.shincolle.item;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.fluids.FluidActionResult;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
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
    public @Nullable ICapabilityProvider initCapabilities(ItemStack stack, @Nullable net.minecraft.nbt.CompoundTag nbt) {
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
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        Player player = context.getPlayer();

        if (player == null) {
            return InteractionResult.PASS;
        }

        if (FluidUtil.interactWithFluidHandler(player, context.getHand(), level, context.getClickedPos(), context.getClickedFace())) {
            return InteractionResult.sidedSuccess(level.isClientSide());
        }

        ItemStack stack = context.getItemInHand();
        FluidActionResult pickupResult = FluidUtil.tryPickUpFluid(stack, player, level, context.getClickedPos(), context.getClickedFace());

        if (pickupResult.isSuccess()) {
            player.setItemInHand(context.getHand(), pickupResult.getResult());
            return InteractionResult.sidedSuccess(level.isClientSide());
        }

        BlockPos placePos = getPlacePos(context);
        Optional<FluidStack> containedFluid = FluidUtil.getFluidContained(stack);

        if (containedFluid.isEmpty() || containedFluid.get().isEmpty()) {
            return InteractionResult.PASS;
        }

        FluidActionResult placeResult = FluidUtil.tryPlaceFluid(player, level, context.getHand(), placePos, stack, containedFluid.get());

        if (placeResult.isSuccess()) {
            player.setItemInHand(context.getHand(), placeResult.getResult());
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
}
