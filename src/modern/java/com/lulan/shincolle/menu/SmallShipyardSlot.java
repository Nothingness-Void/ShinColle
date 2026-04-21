package com.lulan.shincolle.menu;

import com.lulan.shincolle.crafting.SmallShipyardRecipes;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;

public class SmallShipyardSlot extends SlotItemHandler {

    private final int slotIndex;

    public SmallShipyardSlot(IItemHandler itemHandler, int slotIndex, int xPosition, int yPosition) {
        super(itemHandler, slotIndex, xPosition, yPosition);
        this.slotIndex = slotIndex;
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
        if (this.slotIndex == SmallShipyardRecipes.SLOT_OUTPUT) {
            return false;
        }

        if (this.slotIndex == SmallShipyardRecipes.SLOT_FUEL) {
            return SmallShipyardRecipes.isFuel(stack);
        }

        return SmallShipyardRecipes.materialSlot(stack) == this.slotIndex;
    }
}
