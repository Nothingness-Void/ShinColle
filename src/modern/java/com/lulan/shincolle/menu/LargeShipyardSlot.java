package com.lulan.shincolle.menu;

import com.lulan.shincolle.crafting.LargeShipyardRecipes;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;

public class LargeShipyardSlot extends SlotItemHandler {

    private final int slotIndex;
    private final boolean genericInventory;

    public LargeShipyardSlot(IItemHandler itemHandler, int slotIndex, int xPosition, int yPosition) {
        this(itemHandler, slotIndex, xPosition, yPosition, false);
    }

    public LargeShipyardSlot(IItemHandler itemHandler, int slotIndex, int xPosition, int yPosition, boolean genericInventory) {
        super(itemHandler, slotIndex, xPosition, yPosition);
        this.slotIndex = slotIndex;
        this.genericInventory = genericInventory;
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
        if (this.slotIndex == LargeShipyardRecipes.SLOT_OUTPUT) {
            return false;
        }

        if (this.genericInventory) {
            return this.slotIndex != LargeShipyardRecipes.SLOT_OUTPUT;
        }

        if (this.slotIndex == LargeShipyardRecipes.SLOT_FUEL) {
            return LargeShipyardRecipes.isFuel(stack);
        }

        int slotMaterial = LargeShipyardRecipes.materialIndexForSlot(this.slotIndex);
        return slotMaterial >= 0 && LargeShipyardRecipes.materialSlot(stack) == slotMaterial;
    }
}
