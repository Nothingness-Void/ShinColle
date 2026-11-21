package com.lulan.shincolle.menu;

import com.lulan.shincolle.blockentity.CraneBlockEntity;
import com.lulan.shincolle.entity.ship.LegacyShipEntity;
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
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
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
    public static final int BUTTON_TOGGLE_META = 6;
    public static final int BUTTON_TOGGLE_OREDICT = 7;
    public static final int BUTTON_TOGGLE_NBT = 8;
    public static final int BUTTON_POWER = 9;
    public static final int BUTTON_WAIT_MODE_DECR = 10;
    private static final int SLOT_FILTER_START = 0;
    private static final int SLOT_FILTER_END = SLOT_FILTER_START + CraneBlockEntity.FILTER_SLOT_COUNT;

    private final Inventory inventory;
    private final BlockPos cranePos;
    private final ContainerData craneData;

    public CraneTerminalMenu(int containerId, Inventory inventory, BlockPos cranePos) {
        this(containerId, inventory, cranePos, null);
    }

    public CraneTerminalMenu(int containerId, Inventory inventory, BlockPos cranePos, @Nullable ContainerData craneData) {
        super(ModMenus.CRANE_TERMINAL.get(), containerId);
        this.inventory = inventory;
        this.cranePos = cranePos.immutable();
        this.craneData = craneData != null ? craneData : new ContainerData() {
            @Override
            public int get(int index) {
                CraneBlockEntity crane = getCrane();
                LegacyShipEntity ship = crane == null ? null : crane.getShip();
                int value = ship == null ? 0 : index < 2 ? ship.getId() : ship.getCraneTime();
                return (value >>> ((index & 1) * 16)) & 0xFFFF;
            }

            @Override
            public void set(int index, int value) {
            }

            @Override
            public int getCount() {
                return 4;
            }
        };
        this.addDataSlots(this.craneData);

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
        return new CraneTerminalMenu(containerId, inventory, buffer.readBlockPos(), new SimpleContainerData(4));
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

    public int getShipId() {
        return (this.craneData.get(0) & 0xFFFF) | ((this.craneData.get(1) & 0xFFFF) << 16);
    }

    public @Nullable LegacyShipEntity getShip() {
        return this.inventory.player.level().getEntity(this.getShipId()) instanceof LegacyShipEntity ship ? ship : null;
    }

    public int getCraneTime() {
        return (this.craneData.get(2) & 0xFFFF) | ((this.craneData.get(3) & 0xFFFF) << 16);
    }

    public static String formatCraneTime(int ticks) {
        int seconds = (int) (ticks * 0.05F);
        return twoDigits(seconds / 3600) + ":" + twoDigits((seconds % 3600) / 60) + ":" + twoDigits(seconds % 60);
    }

    private static String twoDigits(int value) {
        return value < 10 ? "0" + value : Integer.toString(value);
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

    public boolean isCheckMetadata() {
        CraneBlockEntity crane = this.getCrane();
        return crane != null && crane.isCheckMetadata();
    }

    public boolean isCheckOredict() {
        CraneBlockEntity crane = this.getCrane();
        return crane != null && crane.isCheckOredict();
    }

    public boolean isCheckNbt() {
        CraneBlockEntity crane = this.getCrane();
        return crane != null && crane.isCheckNbt();
    }

    public Component getCheckMetadataStateLabel() {
        CraneBlockEntity crane = this.getCrane();
        return crane != null ? crane.getCheckMetadataStateLabel() : Component.translatable("gui.shincolle.crane.state.disabled");
    }

    public Component getCheckOredictStateLabel() {
        CraneBlockEntity crane = this.getCrane();
        return crane != null ? crane.getCheckOredictStateLabel() : Component.translatable("gui.shincolle.crane.state.disabled");
    }

    public Component getCheckNbtStateLabel() {
        CraneBlockEntity crane = this.getCrane();
        return crane != null ? crane.getCheckNbtStateLabel() : Component.translatable("gui.shincolle.crane.state.disabled");
    }

    public ItemStack getFilterStack(int slot) {
        CraneBlockEntity crane = this.getCrane();
        return crane != null ? crane.getFilterStack(slot) : ItemStack.EMPTY;
    }

    public boolean isFilterInverted(int slot) {
        CraneBlockEntity crane = this.getCrane();
        return crane != null && crane.isFilterInverted(slot);
    }

    public boolean isActive() {
        CraneBlockEntity crane = this.getCrane();
        return crane != null && crane.isActive();
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
            case BUTTON_WAIT_MODE_DECR -> crane.cycleWaitMode(false);
            case BUTTON_REDSTONE_MODE -> crane.cycleRedstoneMode();
            case BUTTON_LIQUID_MODE -> crane.cycleLiquidMode();
            case BUTTON_ENERGY_MODE -> crane.cycleEnergyMode();
            case BUTTON_TOGGLE_LOAD -> crane.toggleLoadEnabled();
            case BUTTON_TOGGLE_UNLOAD -> crane.toggleUnloadEnabled();
            case BUTTON_TOGGLE_META -> crane.toggleCheckMetadata();
            case BUTTON_TOGGLE_OREDICT -> crane.toggleCheckOredict();
            case BUTTON_TOGGLE_NBT -> crane.toggleCheckNbt();
            case BUTTON_POWER -> crane.toggleActive();
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
