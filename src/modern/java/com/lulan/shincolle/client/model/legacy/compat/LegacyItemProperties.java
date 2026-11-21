package com.lulan.shincolle.client.model.legacy.compat;

import com.lulan.shincolle.item.KaitaiHammerItem;
import com.lulan.shincolle.item.OpToolItem;
import com.lulan.shincolle.item.PointerItem;
import com.lulan.shincolle.item.TargetWrenchItem;
import net.minecraft.world.item.BedItem;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.FishingRodItem;
import net.minecraft.world.item.FoodOnAStickItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemNameBlockItem;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TieredItem;
import net.minecraft.world.level.block.AbstractSkullBlock;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.SignBlock;

public final class LegacyItemProperties {
    private LegacyItemProperties() {}

    public static boolean isBlock(ItemStack stack) {
        if (!(stack.getItem() instanceof BlockItem block)) return false;
        if (block instanceof ItemNameBlockItem || block instanceof BedItem
                || block.getBlock() instanceof DoorBlock || block.getBlock() instanceof SignBlock
                || block.getBlock() instanceof AbstractSkullBlock) return false;
        Item item = stack.getItem();
        // These were ItemBlockSpecial, not ItemBlock, in 1.12.2.
        return item != Items.STRING && item != Items.SUGAR_CANE && item != Items.CAKE
                && item != Items.REPEATER && item != Items.COMPARATOR && item != Items.BREWING_STAND
                && item != Items.CAULDRON && item != Items.FLOWER_POT;
    }

    public static boolean isFull3D(ItemStack stack) {
        if (stack.isEmpty()) return false;
        Item item = stack.getItem();
        return item instanceof TieredItem || item instanceof FishingRodItem || item instanceof FoodOnAStickItem
                || item == Items.STICK || item == Items.BONE || item == Items.BLAZE_ROD
                || item instanceof TargetWrenchItem || item instanceof PointerItem
                || item instanceof KaitaiHammerItem || item instanceof OpToolItem;
    }
}
