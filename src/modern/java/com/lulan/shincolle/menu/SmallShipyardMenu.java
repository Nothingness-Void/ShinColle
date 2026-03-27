package com.lulan.shincolle.menu;

import com.lulan.shincolle.blockentity.SmallShipyardBlockEntity;
import com.lulan.shincolle.crafting.ShipyardBuildTypes;
import com.lulan.shincolle.crafting.SmallShipyardRecipes;
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

import javax.annotation.Nullable;

public class SmallShipyardMenu extends AbstractContainerMenu {

    public static final int BUTTON_SHIP_MODE = 0;
    public static final int BUTTON_EQUIP_MODE = 1;
    private static final int PLAYER_SLOT_START = 6;
    private static final int PLAYER_SLOT_END = 42;
    private static final int HOTBAR_SLOT_START = 33;

    private final Inventory inventory;
    private final BlockPos shipyardPos;
    private final ContainerData data;

    public SmallShipyardMenu(int containerId, Inventory inventory, SmallShipyardBlockEntity shipyard) {
        this(containerId, inventory, shipyard.getBlockPos(), shipyard.getItems(), shipyard.getContainerData());
    }

    private SmallShipyardMenu(int containerId, Inventory inventory, BlockPos shipyardPos, ItemStackHandler items, ContainerData data) {
        super(ModMenus.SMALL_SHIPYARD.get(), containerId);
        this.inventory = inventory;
        this.shipyardPos = shipyardPos.immutable();
        this.data = data;
        checkContainerDataCount(data, 4);
        this.addDataSlots(data);

        this.addSlot(new SmallShipyardSlot(items, SmallShipyardRecipes.SLOT_GRUDGE, 33, 29));
        this.addSlot(new SmallShipyardSlot(items, SmallShipyardRecipes.SLOT_ABYSSIUM, 53, 29));
        this.addSlot(new SmallShipyardSlot(items, SmallShipyardRecipes.SLOT_AMMO, 73, 29));
        this.addSlot(new SmallShipyardSlot(items, SmallShipyardRecipes.SLOT_POLYMETAL, 93, 29));
        this.addSlot(new SmallShipyardSlot(items, SmallShipyardRecipes.SLOT_FUEL, 8, 53));
        this.addSlot(new SmallShipyardSlot(items, SmallShipyardRecipes.SLOT_OUTPUT, 134, 44));

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(new Slot(inventory, col + row * 9 + 9, 8 + col * 18, 87 + row * 18));
            }
        }

        for (int hotbarSlot = 0; hotbarSlot < 9; hotbarSlot++) {
            this.addSlot(new Slot(inventory, hotbarSlot, 8 + hotbarSlot * 18, 145));
        }
    }

    public static SmallShipyardMenu fromNetwork(int containerId, Inventory inventory, FriendlyByteBuf buffer) {
        BlockPos pos = buffer.readBlockPos();
        if (inventory.player.level().getBlockEntity(pos) instanceof SmallShipyardBlockEntity shipyard) {
            return new SmallShipyardMenu(containerId, inventory, shipyard);
        }

        return new SmallShipyardMenu(containerId, inventory, pos, new ItemStackHandler(SmallShipyardRecipes.SLOT_COUNT), new SimpleContainerData(4));
    }

    public @Nullable SmallShipyardBlockEntity getShipyard() {
        return this.inventory.player.level().getBlockEntity(this.shipyardPos) instanceof SmallShipyardBlockEntity shipyard ? shipyard : null;
    }

    public boolean canEdit() {
        SmallShipyardBlockEntity shipyard = this.getShipyard();
        return shipyard == null || shipyard.canEdit(this.inventory.player);
    }

    public Component getOwnerLabel() {
        SmallShipyardBlockEntity shipyard = this.getShipyard();
        return shipyard == null
                ? Component.translatable("gui.shincolle.waypoint.owner.unassigned")
                : shipyard.getOwnerLabel();
    }

    public int getBuildType() {
        return this.data.get(0);
    }

    public int getPowerConsumed() {
        return this.data.get(1);
    }

    public int getPowerRemained() {
        return this.data.get(2);
    }

    public int getPowerGoal() {
        return this.data.get(3);
    }

    public boolean hasRemainedPower() {
        return this.getPowerRemained() > SmallShipyardBlockEntity.BUILD_SPEED;
    }

    public Component getBuildTypeLabel() {
        return ShipyardBuildTypes.label(this.getBuildType());
    }

    public int getPowerRemainingScaled(int height) {
        return this.getPowerRemained() <= 0 ? 0 : (int) ((this.getPowerRemained() * (double) height) / SmallShipyardBlockEntity.POWER_MAX);
    }

    public String getBuildTimeString() {
        if (this.getPowerGoal() <= 0 || this.getPowerConsumed() >= this.getPowerGoal()) {
            return "00:00";
        }

        int remainingSeconds = Math.max(0, (int) (((this.getPowerGoal() - this.getPowerConsumed()) / (float) SmallShipyardBlockEntity.BUILD_SPEED) * 0.05F));
        int minutes = remainingSeconds / 60;
        int seconds = remainingSeconds % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }

    @Override
    public boolean clickMenuButton(Player player, int id) {
        SmallShipyardBlockEntity shipyard = this.getShipyard();
        if (shipyard == null) {
            return false;
        }

        if (!shipyard.canEdit(player)) {
            player.displayClientMessage(Component.translatable("chat.shincolle.wrongowner"), true);
            return false;
        }

        switch (id) {
            case BUTTON_SHIP_MODE -> shipyard.cycleShipMode();
            case BUTTON_EQUIP_MODE -> shipyard.cycleEquipMode();
            default -> {
                return false;
            }
        }

        ShinColleSoundHelper.playForPlayer(player.level(), player, ModSoundEvents.SHIP_BELL.get(), 0.65F,
                ShinColleSoundHelper.variedPitch(player, 1.0F, 0.08F));
        return true;
    }

    @Override
    public boolean stillValid(Player player) {
        return player.level().getBlockEntity(this.shipyardPos) instanceof SmallShipyardBlockEntity
                && player.distanceToSqr(this.shipyardPos.getX() + 0.5D, this.shipyardPos.getY() + 0.5D, this.shipyardPos.getZ() + 0.5D) <= 64.0D;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = this.slots.get(index);
        if (slot == null || !slot.hasItem()) {
            return ItemStack.EMPTY;
        }

        ItemStack stack = slot.getItem();
        ItemStack copy = stack.copy();

        if (index == SmallShipyardRecipes.SLOT_OUTPUT) {
            if (!this.moveItemStackTo(stack, PLAYER_SLOT_START, PLAYER_SLOT_END, true)) {
                return ItemStack.EMPTY;
            }

            slot.onQuickCraft(stack, copy);
        } else if (index < PLAYER_SLOT_START) {
            if (!this.moveItemStackTo(stack, PLAYER_SLOT_START, PLAYER_SLOT_END, false)) {
                return ItemStack.EMPTY;
            }
        } else {
            int materialSlot = SmallShipyardRecipes.materialSlot(stack);
            if (materialSlot >= 0) {
                if (!this.moveItemStackTo(stack, materialSlot, materialSlot + 1, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (SmallShipyardRecipes.isFuel(stack)) {
                if (!this.moveItemStackTo(stack, SmallShipyardRecipes.SLOT_FUEL, SmallShipyardRecipes.SLOT_FUEL + 1, false)) {
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
