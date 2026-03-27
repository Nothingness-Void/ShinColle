package com.lulan.shincolle.menu;

import com.lulan.shincolle.registry.ModMenus;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;

public class DeskReferenceMenu extends AbstractContainerMenu {

    public static final int RADAR_VARIANT = 0;
    public static final int BOOK_VARIANT = 1;

    private final int variant;

    public DeskReferenceMenu(int containerId, Inventory inventory, int variant) {
        super(ModMenus.DESK_REFERENCE.get(), containerId);
        this.variant = variant;
    }

    public static DeskReferenceMenu fromNetwork(int containerId, Inventory inventory, FriendlyByteBuf buffer) {
        return new DeskReferenceMenu(containerId, inventory, buffer.readVarInt());
    }

    public int getVariant() {
        return this.variant;
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }
}
