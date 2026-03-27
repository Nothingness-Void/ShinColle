package com.lulan.shincolle.item;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.List;

public class PointerItem extends Item {

    private static final String MODE_TAG = "Mode";
    private static final int MAX_MODE = 2;

    public PointerItem(Properties properties) {
        super(properties);
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return true;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (player.isShiftKeyDown()) {
            int nextMode = (getMode(stack) + 1) % (MAX_MODE + 1);
            setMode(stack, nextMode);

            if (!level.isClientSide()) {
                player.displayClientMessage(Component.translatable("chat.shincolle.pointer.mode_changed", getModeName(nextMode)), true);
            }

            return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
        }

        if (level.isClientSide()) {
            player.displayClientMessage(Component.translatable("gui.shincolle.placeholder_command_item"), true);
        }

        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("gui.shincolle.pointer.current_mode", getModeName(getMode(stack))).withStyle(ChatFormatting.AQUA));
        tooltip.add(Component.translatable("gui.shincolle.pointer3").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("gui.shincolle.placeholder_command_item").withStyle(ChatFormatting.DARK_GRAY));
    }

    public static float getModelMode(ItemStack stack) {
        return getMode(stack);
    }

    private static int getMode(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag == null) {
            return 0;
        }

        int mode = tag.getInt(MODE_TAG);
        return mode >= 0 && mode <= MAX_MODE ? mode : 0;
    }

    private static void setMode(ItemStack stack, int mode) {
        stack.getOrCreateTag().putInt(MODE_TAG, mode);
    }

    private static Component getModeName(int mode) {
        return switch (mode) {
            case 1 -> Component.translatable("gui.shincolle.pointer1");
            case 2 -> Component.translatable("gui.shincolle.pointer2");
            default -> Component.translatable("gui.shincolle.pointer0");
        };
    }
}
