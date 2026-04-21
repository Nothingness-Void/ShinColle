package com.lulan.shincolle.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.List;

public class KaitaiHammerItem extends Item {

    public KaitaiHammerItem(Properties properties) {
        super(properties);
    }

    @Override
    public boolean hasCraftingRemainingItem(ItemStack stack) {
        return true;
    }

    @Override
    public ItemStack getCraftingRemainingItem(ItemStack stack) {
        ItemStack remainder = stack.copy();
        remainder.setCount(1);
        remainder.setDamageValue(remainder.getDamageValue() + 1);
        return remainder.getDamageValue() >= remainder.getMaxDamage() ? ItemStack.EMPTY : remainder;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        int remainingUses = stack.getMaxDamage() - stack.getDamageValue();
        tooltip.add(Component.translatable("gui.shincolle.kaitaihammer.remaining", remainingUses).withStyle(ChatFormatting.GOLD));
        tooltip.add(Component.translatable("gui.shincolle.kaitaihammer.pending").withStyle(ChatFormatting.DARK_GRAY));
    }
}
