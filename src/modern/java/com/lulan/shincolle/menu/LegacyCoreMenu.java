package com.lulan.shincolle.menu;

import com.lulan.shincolle.blockentity.LegacyCoreAccess;
import com.lulan.shincolle.registry.ModMenus;
import com.lulan.shincolle.registry.ModSoundEvents;
import com.lulan.shincolle.sound.ShinColleSoundHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.SlotItemHandler;

import javax.annotation.Nullable;

public class LegacyCoreMenu extends AbstractContainerMenu {

    public static final int BUTTON_CYCLE_MODE = 0;
    private static final int CORE_SLOT_COUNT = 9;
    private static final int PLAYER_SLOT_START = CORE_SLOT_COUNT;
    private static final int PLAYER_SLOT_END = PLAYER_SLOT_START + 36;
    private static final int HOTBAR_SLOT_START = PLAYER_SLOT_START + 27;

    private final Inventory inventory;
    private final BlockPos corePos;
    private final ContainerData data;

    public LegacyCoreMenu(int containerId, Inventory inventory, LegacyCoreAccess core) {
        this(containerId, inventory, core.getBlockPos(), core.getFuelItems(), core.getCoreContainerData());
    }

    private LegacyCoreMenu(int containerId, Inventory inventory, BlockPos corePos, ItemStackHandler items, ContainerData data) {
        super(ModMenus.LEGACY_CORE.get(), containerId);
        this.inventory = inventory;
        this.corePos = corePos.immutable();
        this.data = data;
        checkContainerDataCount(data, 4);
        this.addDataSlots(data);

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                int slotIndex = col + row * 3;
                this.addSlot(new SlotItemHandler(items, slotIndex, 18 + col * 18, 22 + row * 18) {
                    @Override
                    public boolean mayPlace(ItemStack stack) {
                        LegacyCoreAccess core = LegacyCoreMenu.this.getCore();
                        return core != null && core.isFuelItem(stack);
                    }
                });
            }
        }

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(new Slot(inventory, col + row * 9 + 9, 8 + col * 18, 92 + row * 18));
            }
        }

        for (int hotbarSlot = 0; hotbarSlot < 9; hotbarSlot++) {
            this.addSlot(new Slot(inventory, hotbarSlot, 8 + hotbarSlot * 18, 150));
        }
    }

    public static LegacyCoreMenu fromNetwork(int containerId, Inventory inventory, FriendlyByteBuf buffer) {
        BlockPos pos = buffer.readBlockPos();
        if (inventory.player.level().getBlockEntity(pos) instanceof LegacyCoreAccess core) {
            return new LegacyCoreMenu(containerId, inventory, core);
        }

        return new LegacyCoreMenu(containerId, inventory, pos, new ItemStackHandler(CORE_SLOT_COUNT), new SimpleContainerData(4));
    }

    public @Nullable LegacyCoreAccess getCore() {
        return this.inventory.player.level().getBlockEntity(this.corePos) instanceof LegacyCoreAccess core ? core : null;
    }

    public boolean canEdit() {
        LegacyCoreAccess core = this.getCore();
        return core == null || core.canEdit(this.inventory.player);
    }

    public Component getOwnerLabel() {
        LegacyCoreAccess core = this.getCore();
        return core == null ? Component.translatable("gui.shincolle.waypoint.owner.unassigned") : core.getOwnerLabel();
    }

    public Component getBlockLabel() {
        LegacyCoreAccess core = this.getCore();
        return core == null ? Component.empty() : core.getBlockLabel();
    }

    public boolean isVolCore() {
        LegacyCoreAccess core = this.getCore();
        return core != null && core.isVolCore();
    }

    public int getMode() {
        return this.data.get(0);
    }

    public int getCharge() {
        return this.data.get(1);
    }

    public int getMaxCharge() {
        return this.data.get(2);
    }

    public boolean hasNearbyFluid() {
        return this.data.get(3) != 0;
    }

    public Component getModeLabel() {
        return this.getMode() == 1
                ? Component.translatable("gui.shincolle.legacy_core.mode.active")
                : Component.translatable("gui.shincolle.legacy_core.mode.idle");
    }

    public int getChargeScaled(int height) {
        return this.getMaxCharge() <= 0 ? 0 : (int) ((this.getCharge() * (double) height) / this.getMaxCharge());
    }

    @Override
    public boolean clickMenuButton(Player player, int id) {
        if (id != BUTTON_CYCLE_MODE) {
            return false;
        }

        LegacyCoreAccess core = this.getCore();
        if (core == null) {
            return false;
        }

        if (!core.canEdit(player)) {
            player.displayClientMessage(Component.translatable("chat.shincolle.wrongowner"), true);
            player.displayClientMessage(Component.translatable("chat.shincolle.legacy_core.readonly", core.getBlockLabel()), true);
            return false;
        }

        core.cycleCoreMode();
        ShinColleSoundHelper.playForPlayer(player.level(), player, ModSoundEvents.SHIP_BELL.get(), 0.7F,
                ShinColleSoundHelper.variedPitch(player, 1.0F, 0.08F));
        return true;
    }

    @Override
    public boolean stillValid(Player player) {
        return player.level().getBlockEntity(this.corePos) instanceof LegacyCoreAccess
                && player.distanceToSqr(this.corePos.getX() + 0.5D, this.corePos.getY() + 0.5D, this.corePos.getZ() + 0.5D) <= 64.0D;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = this.slots.get(index);
        if (slot == null || !slot.hasItem()) {
            return ItemStack.EMPTY;
        }

        ItemStack stack = slot.getItem();
        ItemStack copy = stack.copy();

        if (index < PLAYER_SLOT_START) {
            if (!this.moveItemStackTo(stack, PLAYER_SLOT_START, PLAYER_SLOT_END, false)) {
                return ItemStack.EMPTY;
            }
        } else {
            LegacyCoreAccess core = this.getCore();
            if (core != null && core.isFuelItem(stack)) {
                if (!this.moveItemStackTo(stack, 0, CORE_SLOT_COUNT, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (index < HOTBAR_SLOT_START) {
                if (!this.moveItemStackTo(stack, HOTBAR_SLOT_START, PLAYER_SLOT_END, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.moveItemStackTo(stack, PLAYER_SLOT_START, HOTBAR_SLOT_START, false)) {
                return ItemStack.EMPTY;
            }
        }

        if (stack.isEmpty()) {
            slot.set(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }

        if (stack.getCount() == copy.getCount()) {
            return ItemStack.EMPTY;
        }

        slot.onTake(player, stack);
        return copy;
    }
}
