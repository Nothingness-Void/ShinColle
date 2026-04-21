package com.lulan.shincolle.menu;

import net.minecraft.world.entity.player.Player;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;

public class CraneTemplateSlot extends SlotItemHandler {

    public CraneTemplateSlot(IItemHandler itemHandler, int index, int xPosition, int yPosition) {
        super(itemHandler, index, xPosition, yPosition);
    }

    @Override
    public boolean mayPickup(Player player) {
        return false;
    }

    @Override
    public boolean mayPlace(net.minecraft.world.item.ItemStack stack) {
        return false;
    }
}
