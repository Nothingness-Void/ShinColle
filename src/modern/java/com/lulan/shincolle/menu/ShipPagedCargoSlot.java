package com.lulan.shincolle.menu;

import com.lulan.shincolle.entity.ship.LegacyShipEntity;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import java.util.function.IntSupplier;

/** Legacy SlotShipInventory: cargo slot index is offset by {@code inventoryPage * 18}. */
public class ShipPagedCargoSlot extends Slot {

    private final int baseIndex;
    private final IntSupplier pageSupplier;

    public ShipPagedCargoSlot(Container container, int baseIndex, int x, int y, IntSupplier pageSupplier) {
        super(container, baseIndex, x, y);
        this.baseIndex = baseIndex;
        this.pageSupplier = pageSupplier;
    }

    private int pagedIndex() {
        int index = this.baseIndex + this.pageSupplier.getAsInt() * LegacyShipEntity.CARGO_SLOT_COUNT;
        return index < this.container.getContainerSize() ? index : this.baseIndex;
    }

    @Override
    public ItemStack getItem() {
        return this.container.getItem(this.pagedIndex());
    }

    @Override
    public void set(ItemStack stack) {
        this.container.setItem(this.pagedIndex(), stack);
        this.setChanged();
    }

    @Override
    public ItemStack remove(int amount) {
        return this.container.removeItem(this.pagedIndex(), amount);
    }

    @Override
    public int getContainerSlot() {
        return this.pagedIndex();
    }
}
