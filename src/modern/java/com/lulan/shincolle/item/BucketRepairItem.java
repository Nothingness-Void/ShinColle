package com.lulan.shincolle.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.List;

public class BucketRepairItem extends Item {

    public BucketRepairItem(Properties properties) {
        super(properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("gui.shincolle.bucketrepair.effect").withStyle(ChatFormatting.AQUA));
        tooltip.add(Component.translatable("gui.shincolle.bucketrepair.use").withStyle(ChatFormatting.GRAY));
    }
}
