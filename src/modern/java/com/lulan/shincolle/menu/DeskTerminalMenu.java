package com.lulan.shincolle.menu;

import com.lulan.shincolle.blockentity.DeskBlockEntity;
import com.lulan.shincolle.registry.ModMenus;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;

public class DeskTerminalMenu extends AbstractContainerMenu {

    private final BlockPos deskPos;
    private final int selectedFunction;

    public DeskTerminalMenu(int containerId, Inventory inventory, BlockPos deskPos, int selectedFunction) {
        super(ModMenus.DESK_TERMINAL.get(), containerId);
        this.deskPos = deskPos.immutable();
        this.selectedFunction = selectedFunction == DeskReferenceMenu.BOOK_VARIANT
                ? DeskReferenceMenu.BOOK_VARIANT
                : DeskReferenceMenu.RADAR_VARIANT;
    }

    public static DeskTerminalMenu fromNetwork(int containerId, Inventory inventory, FriendlyByteBuf buffer) {
        return new DeskTerminalMenu(containerId, inventory, buffer.readBlockPos(), buffer.readVarInt());
    }

    public BlockPos getDeskPos() {
        return this.deskPos;
    }

    public int getSelectedFunction() {
        return this.selectedFunction;
    }

    public Component getSelectedFunctionName() {
        return Component.translatable(this.selectedFunction == DeskReferenceMenu.BOOK_VARIANT
                ? "item.shincolle.deskitembook"
                : "item.shincolle.deskitemradar");
    }

    @Override
    public boolean stillValid(Player player) {
        return player.level().getBlockEntity(this.deskPos) instanceof DeskBlockEntity
                && player.distanceToSqr(this.deskPos.getX() + 0.5D, this.deskPos.getY() + 0.5D, this.deskPos.getZ() + 0.5D) <= 64.0D;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }
}
