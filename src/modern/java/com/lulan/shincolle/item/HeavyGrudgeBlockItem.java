package com.lulan.shincolle.item;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

import javax.annotation.Nullable;
import java.util.List;

public class HeavyGrudgeBlockItem extends BlockItem {

    public static final String MATS_TAG = "mats";
    public static final String FUEL_TAG = "fuel";

    public HeavyGrudgeBlockItem(Block block, Properties properties) {
        super(block, properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        CompoundTag tag = stack.getTag();
        if (tag == null || !tag.contains(MATS_TAG)) {
            return;
        }

        int[] mats = tag.getIntArray(MATS_TAG);
        tooltip.add(materialLine(mats, 0, "item.shincolle.grudge", ChatFormatting.WHITE));
        tooltip.add(materialLine(mats, 1, "item.shincolle.abyssmetal", ChatFormatting.RED));
        tooltip.add(materialLine(mats, 2, "item.shincolle.ammo", ChatFormatting.GREEN));
        tooltip.add(materialLine(mats, 3, "item.shincolle.abyssmetal1", ChatFormatting.AQUA));
    }

    private static Component materialLine(int[] mats, int index, String itemKey, ChatFormatting color) {
        int amount = index < mats.length ? mats[index] : 0;
        return Component.literal(amount + " ").append(Component.translatable(itemKey)).withStyle(color);
    }
}
