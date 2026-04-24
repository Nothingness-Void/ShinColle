package com.lulan.shincolle.menu;

import com.lulan.shincolle.blockentity.LargeShipyardAccess;
import com.lulan.shincolle.blockentity.HeavyGrudgeBlockEntity;
import com.lulan.shincolle.crafting.LargeShipyardRecipes;
import com.lulan.shincolle.crafting.ShipyardBuildTypes;
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

public class LargeShipyardMenu extends AbstractContainerMenu {

    public static final int BUTTON_SHIP_MODE = 0;
    public static final int BUTTON_EQUIP_MODE = 1;
    public static final int BUTTON_INVENTORY_MODE = 2;
    public static final int BUTTON_SELECT_MATERIAL_BASE = 3;
    public static final int BUTTON_SELECT_MATERIAL_END = 6;
    public static final int BUTTON_ADJUST_MATERIAL_BASE = 7;
    public static final int BUTTON_ADJUST_MATERIAL_END = 14;

    private static final int TILE_SLOT_COUNT = LargeShipyardRecipes.SLOT_COUNT;
    private static final int PLAYER_SLOT_START = TILE_SLOT_COUNT;
    private static final int PLAYER_SLOT_END = PLAYER_SLOT_START + 36;
    private static final int HOTBAR_SLOT_START = PLAYER_SLOT_START + 27;

    private final Inventory inventory;
    private final BlockPos shipyardPos;
    private final ContainerData data;

    public LargeShipyardMenu(int containerId, Inventory inventory, LargeShipyardAccess shipyard) {
        this(containerId, inventory, shipyard.getBlockPos(), shipyard.getItems(), shipyard.getContainerData());
    }

    private LargeShipyardMenu(int containerId, Inventory inventory, BlockPos shipyardPos, ItemStackHandler items, ContainerData data) {
        super(ModMenus.LARGE_SHIPYARD.get(), containerId);
        this.inventory = inventory;
        this.shipyardPos = shipyardPos.immutable();
        this.data = data;
        checkContainerDataCount(data, 8);
        this.addDataSlots(data);

        boolean genericInventory = this.getShipyard() != null && this.getShipyard().usesGenericInventory();
        this.addSlot(new LargeShipyardSlot(items, LargeShipyardRecipes.SLOT_OUTPUT, 168, 51, genericInventory));
        this.addSlot(new LargeShipyardSlot(items, LargeShipyardRecipes.SLOT_FUEL, 9, 83, genericInventory));
        this.addSlot(new LargeShipyardSlot(items, LargeShipyardRecipes.SLOT_GRUDGE_A, 25, 116, genericInventory));
        this.addSlot(new LargeShipyardSlot(items, LargeShipyardRecipes.SLOT_GRUDGE_B, 43, 116, genericInventory));
        this.addSlot(new LargeShipyardSlot(items, LargeShipyardRecipes.SLOT_ABYSSIUM_A, 61, 116, genericInventory));
        this.addSlot(new LargeShipyardSlot(items, LargeShipyardRecipes.SLOT_ABYSSIUM_B, 79, 116, genericInventory));
        this.addSlot(new LargeShipyardSlot(items, LargeShipyardRecipes.SLOT_AMMO_A, 97, 116, genericInventory));
        this.addSlot(new LargeShipyardSlot(items, LargeShipyardRecipes.SLOT_AMMO_B, 115, 116, genericInventory));
        this.addSlot(new LargeShipyardSlot(items, LargeShipyardRecipes.SLOT_POLYMETAL_A, 133, 116, genericInventory));
        this.addSlot(new LargeShipyardSlot(items, LargeShipyardRecipes.SLOT_POLYMETAL_B, 151, 116, genericInventory));

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(new Slot(inventory, col + row * 9 + 9, 25 + col * 18, 141 + row * 18));
            }
        }

        for (int hotbarSlot = 0; hotbarSlot < 9; hotbarSlot++) {
            this.addSlot(new Slot(inventory, hotbarSlot, 24 + hotbarSlot * 18, 199));
        }
    }

    public static LargeShipyardMenu fromNetwork(int containerId, Inventory inventory, FriendlyByteBuf buffer) {
        BlockPos pos = buffer.readBlockPos();
        if (inventory.player.level().getBlockEntity(pos) instanceof LargeShipyardAccess shipyard) {
            return new LargeShipyardMenu(containerId, inventory, shipyard);
        }

        return new LargeShipyardMenu(containerId, inventory, pos, new ItemStackHandler(LargeShipyardRecipes.SLOT_COUNT), new SimpleContainerData(8));
    }

    public @Nullable LargeShipyardAccess getShipyard() {
        return this.inventory.player.level().getBlockEntity(this.shipyardPos) instanceof LargeShipyardAccess shipyard ? shipyard : null;
    }

    public boolean canEdit() {
        LargeShipyardAccess shipyard = this.getShipyard();
        return shipyard == null || shipyard.canEdit(this.inventory.player);
    }

    public Component getOwnerLabel() {
        LargeShipyardAccess shipyard = this.getShipyard();
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
        return this.getPowerRemained() > HeavyGrudgeBlockEntity.BUILD_SPEED;
    }

    public boolean hasBuildMode() {
        return this.getBuildType() != ShipyardBuildTypes.NONE;
    }

    public boolean isStructureComplete() {
        return this.data.get(4) != 0;
    }

    public boolean isCoreLinked() {
        return false;
    }

    public int getInvMode() {
        return this.data.get(6);
    }

    public int getSelectMat() {
        return this.data.get(7);
    }

    public boolean isOutputBlocked() {
        return this.slots.get(LargeShipyardRecipes.SLOT_OUTPUT).hasItem();
    }

    public Component getFailureMessage() {
        if (!this.isStructureComplete()) {
            return Component.translatable("gui.shincolle.shipyard.structure_missing");
        }
        if (!this.hasBuildMode()) {
            return Component.translatable("gui.shincolle.shipyard.select_mode");
        }
        if (this.isOutputBlocked()) {
            return Component.translatable("gui.shincolle.shipyard.output_locked");
        }
        if (this.getPowerGoal() <= 0) {
            return Component.translatable("gui.shincolle.shipyard.nomaterial");
        }
        if (!this.hasRemainedPower()) {
            return Component.translatable("gui.shincolle.shipyard.nofuel");
        }
        return Component.empty();
    }

    public Component getBuildTypeLabel() {
        return ShipyardBuildTypes.label(this.getBuildType());
    }

    public int getPowerRemainingScaled(int height) {
        return this.getPowerRemained() <= 0 ? 0 : (int) ((this.getPowerRemained() * (double) height) / HeavyGrudgeBlockEntity.POWER_MAX);
    }

    public String getBuildTimeString() {
        if (this.getPowerGoal() <= 0 || this.getPowerConsumed() >= this.getPowerGoal()) {
            return "00:00";
        }

        int remainingSeconds = Math.max(0, (int) (((this.getPowerGoal() - this.getPowerConsumed()) / (float) HeavyGrudgeBlockEntity.BUILD_SPEED) * 0.05F));
        int minutes = remainingSeconds / 60;
        int seconds = remainingSeconds % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }

    public int getMaterialAmount(int materialIndex) {
        LargeShipyardAccess shipyard = this.getShipyard();
        if (shipyard == null || materialIndex < 0 || materialIndex > 3) {
            return 0;
        }
        return shipyard.getMaterialAmounts()[materialIndex];
    }

    public int getBuildMaterialAmount(int materialIndex) {
        LargeShipyardAccess shipyard = this.getShipyard();
        if (shipyard == null || materialIndex < 0 || materialIndex > 3) {
            return 0;
        }
        return shipyard.getBuildMaterialAmountsView()[materialIndex];
    }

    @Override
    public boolean clickMenuButton(Player player, int id) {
        LargeShipyardAccess shipyard = this.getShipyard();
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
            case BUTTON_INVENTORY_MODE -> shipyard.cycleInventoryMode();
            default -> {
                if (id >= BUTTON_SELECT_MATERIAL_BASE && id <= BUTTON_SELECT_MATERIAL_END) {
                    shipyard.selectMaterial(id - BUTTON_SELECT_MATERIAL_BASE);
                } else if (id >= BUTTON_ADJUST_MATERIAL_BASE && id <= BUTTON_ADJUST_MATERIAL_END) {
                    shipyard.adjustBuildMaterial(shipyard.getSelectMat(), id - BUTTON_ADJUST_MATERIAL_BASE);
                } else {
                    return false;
                }
            }
        }

        ShinColleSoundHelper.playForPlayer(player.level(), player, ModSoundEvents.SHIP_BELL.get(), 0.65F,
                ShinColleSoundHelper.variedPitch(player, 1.0F, 0.08F));
        return true;
    }

    @Override
    public boolean stillValid(Player player) {
        return player.level().getBlockEntity(this.shipyardPos) instanceof LargeShipyardAccess
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

        if (index == LargeShipyardRecipes.SLOT_OUTPUT) {
            if (!this.moveItemStackTo(stack, PLAYER_SLOT_START, PLAYER_SLOT_END, true)) {
                return ItemStack.EMPTY;
            }
            slot.onQuickCraft(stack, copy);
        } else if (index < PLAYER_SLOT_START) {
            if (!this.moveItemStackTo(stack, PLAYER_SLOT_START, PLAYER_SLOT_END, false)) {
                return ItemStack.EMPTY;
            }
        } else {
            boolean moved = false;
            LargeShipyardAccess shipyard = this.getShipyard();
            if (shipyard != null && shipyard.usesGenericInventory()) {
                moved = this.moveItemStackTo(stack, LargeShipyardRecipes.SLOT_FUEL, TILE_SLOT_COUNT, false);
            } else {
                int material = LargeShipyardRecipes.materialSlot(stack);
                if (material >= 0) {
                    int[] range = LargeShipyardRecipes.slotRangeForMaterial(material);
                    moved = range[0] >= 0 && this.moveItemStackTo(stack, range[0], range[1], false);
                } else if (LargeShipyardRecipes.isFuel(stack)) {
                    moved = this.moveItemStackTo(stack, LargeShipyardRecipes.SLOT_FUEL, LargeShipyardRecipes.SLOT_FUEL + 1, false);
                }
            }

            if (!moved) {
                if (index < HOTBAR_SLOT_START) {
                    moved = this.moveItemStackTo(stack, HOTBAR_SLOT_START, PLAYER_SLOT_END, false);
                } else {
                    moved = this.moveItemStackTo(stack, PLAYER_SLOT_START, HOTBAR_SLOT_START, false);
                }
            }

            if (!moved) {
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
