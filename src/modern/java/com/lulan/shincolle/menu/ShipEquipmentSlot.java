package com.lulan.shincolle.menu;

import net.minecraft.world.Container;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import java.util.function.Predicate;

public class ShipEquipmentSlot extends Slot {

    private final Predicate<ItemStack> validator;

    public ShipEquipmentSlot(Container container, int index, int x, int y, Predicate<ItemStack> validator) {
        super(container, index, x, y);
        this.validator = validator;
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
        return this.validator.test(stack);
    }

    @Override
    public int getMaxStackSize() {
        return 1;
    }
}
