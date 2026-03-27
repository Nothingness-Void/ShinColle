package com.lulan.shincolle.menu;

import com.lulan.shincolle.blockentity.CraneBlockEntity;
import com.lulan.shincolle.registry.ModMenus;
import com.lulan.shincolle.registry.ModSoundEvents;
import com.lulan.shincolle.sound.ShinColleSoundHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nullable;

public class CraneTerminalMenu extends AbstractContainerMenu {

    public static final int BUTTON_WAIT_MODE = 0;
    public static final int BUTTON_REDSTONE_MODE = 1;
    public static final int BUTTON_LIQUID_MODE = 2;
    public static final int BUTTON_ENERGY_MODE = 3;
    public static final int BUTTON_TOGGLE_LOAD = 4;
    public static final int BUTTON_TOGGLE_UNLOAD = 5;
    private static final int SLOT_FILTER_START = 0;
    private static final int SLOT_FILTER_END = SLOT_FILTER_START + CraneBlockEntity.FILTER_SLOT_COUNT;

    private final Inventory inventory;
    private final BlockPos cranePos;

    public CraneTerminalMenu(int containerId, Inventory inventory, BlockPos cranePos) {
        super(ModMenus.CRANE_TERMINAL.get(), containerId);
        this.inventory = inventory;
        this.cranePos = cranePos.immutable();

        ItemStackHandler filterItems = this.getCrane() != null ? this.getCrane().getFilterItems() : new ItemStackHandler(CraneBlockEntity.FILTER_SLOT_COUNT);
        for (int i = 0; i < 9; i++) {
            this.addSlot(new CraneTemplateSlot(filterItems, i, 8 + i * 18, 65));
        }

        for (int i = 0; i < 9; i++) {
            this.addSlot(new CraneTemplateSlot(filterItems, i + 9, 8 + i * 18, 96));
        }

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(new Slot(inventory, col + row * 9 + 9, 8 + col * 18, 119 + row * 18));
            }
        }

        for (int hotbarSlot = 0; hotbarSlot < 9; hotbarSlot++) {
            this.addSlot(new Slot(inventory, hotbarSlot, 8 + hotbarSlot * 18, 177));
        }
    }

    public static CraneTerminalMenu fromNetwork(int containerId, Inventory inventory, FriendlyByteBuf buffer) {
        return new CraneTerminalMenu(containerId, inventory, buffer.readBlockPos());
    }

    public BlockPos getCranePos() {
        return this.cranePos;
    }

    public @Nullable CraneBlockEntity getCrane() {
        return this.inventory.player.level().getBlockEntity(this.cranePos) instanceof CraneBlockEntity crane ? crane : null;
    }

    public boolean canEdit() {
        CraneBlockEntity crane = this.getCrane();
        return crane != null && crane.canEdit(this.inventory.player);
    }

    public Component getOwnerLabel() {
        CraneBlockEntity crane = this.getCrane();
        return crane != null ? crane.getOwnerLabel() : Component.translatable("gui.shincolle.waypoint.owner.unassigned");
    }

    public Component getWaitModeLabel() {
        CraneBlockEntity crane = this.getCrane();
        return crane != null ? crane.getWaitModeLabel() : Component.translatable("gui.shincolle.crane.nowait");
    }

    public Component getRedstoneModeLabel() {
        CraneBlockEntity crane = this.getCrane();
        return crane != null ? crane.getRedstoneModeLabel() : Component.translatable("gui.shincolle.crane.red0");
    }

    public Component getLiquidModeLabel() {
        CraneBlockEntity crane = this.getCrane();
        return crane != null ? crane.getLiquidModeLabel() : Component.translatable("gui.shincolle.crane.liquid0");
    }

    public Component getEnergyModeLabel() {
        CraneBlockEntity crane = this.getCrane();
        return crane != null ? crane.getEnergyModeLabel() : Component.translatable("gui.shincolle.crane.energy0");
    }

    public Component getLoadStateLabel() {
        CraneBlockEntity crane = this.getCrane();
        return crane != null ? crane.getLoadStateLabel() : Component.translatable("gui.shincolle.crane.state.enabled");
    }

    public Component getUnloadStateLabel() {
        CraneBlockEntity crane = this.getCrane();
        return crane != null ? crane.getUnloadStateLabel() : Component.translatable("gui.shincolle.crane.state.enabled");
    }

    public boolean isLoadEnabled() {
        CraneBlockEntity crane = this.getCrane();
        return crane == null || crane.isLoadEnabled();
    }

    public boolean isUnloadEnabled() {
        CraneBlockEntity crane = this.getCrane();
        return crane == null || crane.isUnloadEnabled();
    }

    public @Nullable BlockPos getLastWaypoint() {
        CraneBlockEntity crane = this.getCrane();
        return crane != null ? crane.getLastWaypoint() : null;
    }

    public @Nullable BlockPos getNextWaypoint() {
        CraneBlockEntity crane = this.getCrane();
        return crane != null ? crane.getNextWaypoint() : null;
    }

    public @Nullable BlockPos getPairedChest() {
        CraneBlockEntity crane = this.getCrane();
        return crane != null ? crane.getPairedChest() : null;
    }

    public ItemStack getFilterStack(int slot) {
        CraneBlockEntity crane = this.getCrane();
        return crane != null ? crane.getFilterStack(slot) : ItemStack.EMPTY;
    }

    public boolean isFilterInverted(int slot) {
        CraneBlockEntity crane = this.getCrane();
        return crane != null && crane.isFilterInverted(slot);
    }

    @Override
    public boolean clickMenuButton(Player player, int id) {
        if (!(player.level().getBlockEntity(this.cranePos) instanceof CraneBlockEntity crane)) {
            return false;
        }

        if (!crane.canEdit(player)) {
            player.displayClientMessage(Component.translatable("chat.shincolle.wrongowner"), true);
            return false;
        }

        switch (id) {
            case BUTTON_WAIT_MODE -> crane.cycleWaitMode();
            case BUTTON_REDSTONE_MODE -> crane.cycleRedstoneMode();
            case BUTTON_LIQUID_MODE -> crane.cycleLiquidMode();
            case BUTTON_ENERGY_MODE -> crane.cycleEnergyMode();
            case BUTTON_TOGGLE_LOAD -> crane.toggleLoadEnabled();
            case BUTTON_TOGGLE_UNLOAD -> crane.toggleUnloadEnabled();
            default -> {
                return false;
            }
        }

        ShinColleSoundHelper.playForPlayer(player.level(), player, ModSoundEvents.SHIP_BELL.get(), 0.65F,
                ShinColleSoundHelper.variedPitch(player, 1.0F, 0.08F));
        return true;
    }

    @Override
    public void clicked(int slotId, int dragType, ClickType clickType, Player player) {
        if (slotId >= SLOT_FILTER_START && slotId < SLOT_FILTER_END) {
            CraneBlockEntity crane = this.getCrane();
            if (crane == null || !crane.canEdit(player)) {
                return;
            }

            ItemStack carried = this.getCarried();
            if (carried.isEmpty()) {
                crane.clearFilter(slotId);
                this.broadcastChanges();
                return;
            }

            ItemStack copy = carried.copy();
            if (dragType == 1) {
                copy.setCount(1);
                crane.setFilter(slotId, copy, true);
            } else {
                ItemStack oldStack = crane.getFilterStack(slotId);
                int count = copy.getCount();
                if (!oldStack.isEmpty() && ItemStack.isSameItemSameTags(copy, oldStack)) {
                    count += oldStack.getCount();
                }

                copy.setCount(Math.min(copy.getMaxStackSize(), count));
                crane.setFilter(slotId, copy, false);
            }

            this.broadcastChanges();
            return;
        }

        super.clicked(slotId, dragType, clickType, player);
    }

    @Override
    public boolean stillValid(Player player) {
        return player.level().getBlockEntity(this.cranePos) instanceof CraneBlockEntity
                && player.distanceToSqr(this.cranePos.getX() + 0.5D, this.cranePos.getY() + 0.5D, this.cranePos.getZ() + 0.5D) <= 64.0D;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }
}
