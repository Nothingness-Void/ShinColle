package com.lulan.shincolle.menu;

import com.lulan.shincolle.entity.ship.LegacyShipEntity;
import com.lulan.shincolle.entity.ship.ShipEquipmentBehaviorState;
import com.lulan.shincolle.item.equipment.LegacyEquipmentItem;
import com.lulan.shincolle.registry.ModItems;
import com.lulan.shincolle.registry.ModMenus;
import com.lulan.shincolle.sound.ShipSoundType;
import com.lulan.shincolle.sound.ShinColleSoundHelper;
import com.lulan.shincolle.teitoku.TeitokuData;
import com.lulan.shincolle.teitoku.TeitokuHelper;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;

public class ShipInventoryMenu extends AbstractContainerMenu {

    public static final int BUTTON_TOGGLE_MODE = 0;

    private static final int EQUIPMENT_SLOT_START = 0;
    private static final int CARGO_SLOT_START = EQUIPMENT_SLOT_START + LegacyShipEntity.EQUIPMENT_SLOT_COUNT;
    private static final int PLAYER_SLOT_START = LegacyShipEntity.SHIP_SLOT_COUNT;
    private static final int HOTBAR_SLOT_START = PLAYER_SLOT_START + 27;
    private static final int SLOT_END = HOTBAR_SLOT_START + 9;

    private final Inventory playerInventory;
    private final int shipId;
    private final Container shipInventory;

    private @Nullable LegacyShipEntity ship;

    public ShipInventoryMenu(int containerId, Inventory playerInventory, int shipId) {
        super(ModMenus.SHIP_INVENTORY.get(), containerId);
        this.playerInventory = playerInventory;
        this.shipId = shipId;
        this.ship = resolveShip(playerInventory, shipId);
        this.shipInventory = this.ship != null ? this.ship.getShipInventory() : new SimpleContainer(LegacyShipEntity.SHIP_SLOT_COUNT);

        checkContainerSize(this.shipInventory, LegacyShipEntity.SHIP_SLOT_COUNT);
        this.shipInventory.startOpen(playerInventory.player);

        for (int slot = 0; slot < LegacyShipEntity.EQUIPMENT_SLOT_COUNT; slot++) {
            this.addSlot(new ShipEquipmentSlot(this.shipInventory, slot, 144, 18 + slot * 18, this::isValidShipEquipment));
        }

        for (int row = 0; row < 6; row++) {
            for (int col = 0; col < 3; col++) {
                int slot = CARGO_SLOT_START + row * 3 + col;
                this.addSlot(new Slot(this.shipInventory, slot, 8 + col * 18, 18 + row * 18));
            }
        }

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(new Slot(playerInventory, col + row * 9 + 9, 8 + col * 18, 132 + row * 18));
            }
        }

        for (int col = 0; col < 9; col++) {
            this.addSlot(new Slot(playerInventory, col, 8 + col * 18, 190));
        }
    }

    public static ShipInventoryMenu fromNetwork(int containerId, Inventory inventory, FriendlyByteBuf buffer) {
        return new ShipInventoryMenu(containerId, inventory, buffer.readVarInt());
    }

    public @Nullable LegacyShipEntity getShip() {
        if (this.ship == null || !this.ship.isAlive()) {
            this.ship = resolveShip(this.playerInventory, this.shipId);
        }

        return this.ship;
    }

    public int getShipId() {
        return this.shipId;
    }

    public boolean canEdit() {
        LegacyShipEntity ship = this.getShip();
        return ship != null && ship.isOwnedBy(this.playerInventory.player) && !ship.isHostileVariant();
    }

    public boolean isValidShipEquipment(ItemStack stack) {
        LegacyShipEntity ship = this.getShip();
        if (ship == null) {
            return stack.getItem() instanceof LegacyEquipmentItem;
        }

        return ship.canEquip(stack);
    }

    public Component getOwnerLabel() {
        LegacyShipEntity ship = this.getShip();
        String ownerName = ship != null ? ship.getOwnerName() : "";

        if (ownerName.isBlank()) {
            return Component.translatable("gui.shincolle.waypoint.owner.unassigned");
        }

        return Component.literal(ownerName);
    }

    public Component getModeLabel() {
        LegacyShipEntity ship = this.getShip();
        return ship != null ? ship.getEscortModeLabel() : Component.translatable("gui.shincolle.entity.mode.follow");
    }

    public float getHealthRatio() {
        LegacyShipEntity ship = this.getShip();
        if (ship == null || ship.getMaxHealth() <= 0F) {
            return 0F;
        }

        return ship.getHealth() / ship.getMaxHealth();
    }

    public String getHealthText() {
        LegacyShipEntity ship = this.getShip();
        if (ship == null) {
            return "- / -";
        }

        return Math.round(ship.getHealth()) + " / " + Math.round(ship.getMaxHealth());
    }

    public String getAttackText() {
        LegacyShipEntity ship = this.getShip();
        return ship != null ? String.format(java.util.Locale.ROOT, "%.1f", ship.getLegacyStats().attackLight()) : "-";
    }

    public String getSpeedText() {
        LegacyShipEntity ship = this.getShip();
        return ship != null ? String.format(java.util.Locale.ROOT, "%.2f", ship.getLegacyStats().attackSpeed()) : "-";
    }

    public String getRangeText() {
        LegacyShipEntity ship = this.getShip();
        return ship != null ? String.format(java.util.Locale.ROOT, "%.1f", ship.getLegacyStats().attackRange()) : "-";
    }

    public Component getRoleLabel() {
        LegacyShipEntity ship = this.getShip();
        if (ship == null) {
            return Component.translatable("gui.shincolle.ship_inventory.role.unknown");
        }

        return Component.translatable("gui.shincolle.ship_inventory.role." + ship.getSpec().archetype().name().toLowerCase(java.util.Locale.ROOT));
    }

    public int getShipLevel() {
        LegacyShipEntity ship = this.getShip();
        return ship != null ? ship.getShipLevel() : 0;
    }

    public Component getMoraleLabel() {
        LegacyShipEntity ship = this.getShip();
        if (ship == null) {
            return Component.literal("-");
        }

        return Component.translatable("gui.shincolle.ship_inventory.morale." + ship.getMoraleTier());
    }

    public String getMoraleText() {
        LegacyShipEntity ship = this.getShip();
        return ship != null ? ship.getMorale() + " / 16000" : "-";
    }

    public Component getMarriageLabel() {
        LegacyShipEntity ship = this.getShip();
        if (ship == null) {
            return Component.translatable("gui.shincolle.ship_inventory.marriage.no");
        }

        return Component.translatable(ship.isMarried()
                ? "gui.shincolle.ship_inventory.marriage.yes"
                : "gui.shincolle.ship_inventory.marriage.no");
    }

    public int getModernizationCount() {
        LegacyShipEntity ship = this.getShip();
        return ship != null ? ship.getModernizationDisplayCount() : 0;
    }

    public int getRescueCount() {
        int count = 0;

        for (int slot = 0; slot < this.shipInventory.getContainerSize(); slot++) {
            ItemStack stack = this.shipInventory.getItem(slot);
            if (stack.is(ModItems.REPAIRGODDESS.get())) {
                count += stack.getCount();
            }
        }

        return count;
    }

    public ShipEquipmentBehaviorState getBehaviorState() {
        LegacyShipEntity ship = this.getShip();
        return ship == null ? ShipEquipmentBehaviorState.EMPTY : ship.getEquipmentBehaviorState();
    }

    public Component getSensorBehaviorLabel() {
        ShipEquipmentBehaviorState behaviorState = this.getBehaviorState();
        return Component.translatable("gui.shincolle.ship_inventory.behavior.sensor",
                behaviorState.airRadarLevel(),
                behaviorState.surfaceRadarLevel(),
                behaviorState.sonarLevel(),
                behaviorState.fcsLevel());
    }

    public Component getUtilityBehaviorLabel() {
        ShipEquipmentBehaviorState behaviorState = this.getBehaviorState();
        return Component.translatable("gui.shincolle.ship_inventory.behavior.utility",
                behaviorState.flareLevel(),
                behaviorState.searchlightLevel(),
                behaviorState.catapultLevel(),
                behaviorState.turbineLevel(),
                behaviorState.transportTier());
    }

    public Component getRouteBehaviorLabel() {
        ShipEquipmentBehaviorState behaviorState = this.getBehaviorState();
        return Component.translatable("gui.shincolle.ship_inventory.behavior.route",
                Component.translatable(behaviorState.autonomousRoute()
                        ? "gui.shincolle.ship_inventory.behavior.on"
                        : "gui.shincolle.ship_inventory.behavior.off"));
    }

    public Component getTorpedoBehaviorLabel() {
        return Component.translatable("gui.shincolle.ship_inventory.behavior.torpedo",
                this.getBehaviorState().torpedoSpeedLevel());
    }

    public Component getMarriageBonusLabel() {
        LegacyShipEntity ship = this.getShip();
        if (ship == null || !ship.isMarried()) {
            return Component.translatable("gui.shincolle.ship_inventory.marriage.bonus.none");
        }

        return Component.translatable("gui.shincolle.ship_inventory.marriage.bonus.active");
    }

    public int getAiFlags() {
        LegacyShipEntity ship = this.getShip();
        return ship == null ? 0 : ship.getAiFlagsBitmask();
    }

    public int getAiFollowRange() {
        LegacyShipEntity ship = this.getShip();
        return ship == null ? 14 : ship.getAiFollowRange();
    }

    public String getRouteEnergyText() {
        LegacyShipEntity ship = this.getShip();
        return ship == null ? "- / -" : ship.getRouteEnergyText();
    }

    public int getCurrentTeamId() {
        return TeitokuHelper.get(this.playerInventory.player)
                .map(TeitokuData::getCurrentTeamId)
                .orElse(0);
    }

    public int getCurrentFormationId() {
        return TeitokuHelper.get(this.playerInventory.player)
                .map(TeitokuData::getFormationId)
                .orElse(TeitokuData.DEFAULT_FORMATION_ID);
    }

    public Component getCurrentTeamLabel() {
        return Component.translatable("gui.shincolle.ship_inventory.team", this.getCurrentTeamId() + 1);
    }

    public Component getCurrentTeamShortLabel() {
        return Component.literal("T" + (this.getCurrentTeamId() + 1));
    }

    public Component getCurrentFormationLabel() {
        return Component.translatable("gui.shincolle.formation.format" + this.getCurrentFormationId());
    }

    public Component getCurrentFormationShortLabel() {
        return Component.literal(switch (this.getCurrentFormationId()) {
            case 1 -> "Ahead";
            case 2 -> "Double";
            case 3 -> "Diamond";
            case 4 -> "Echelon";
            case 5 -> "Abreast";
            default -> "None";
        });
    }

    public int getCargoUsedSlots() {
        int used = 0;

        for (int slot = CARGO_SLOT_START; slot < LegacyShipEntity.SHIP_SLOT_COUNT; slot++) {
            if (!this.shipInventory.getItem(slot).isEmpty()) {
                used++;
            }
        }

        return used;
    }

    public int getEquipmentUsedSlots() {
        int used = 0;

        for (int slot = 0; slot < LegacyShipEntity.EQUIPMENT_SLOT_COUNT; slot++) {
            if (!this.shipInventory.getItem(slot).isEmpty()) {
                used++;
            }
        }

        return used;
    }

    @Override
    public boolean clickMenuButton(Player player, int id) {
        if (id != BUTTON_TOGGLE_MODE) {
            return false;
        }

        LegacyShipEntity ship = this.getShip();
        if (ship == null || !ship.isOwnedBy(player)) {
            return false;
        }

        ship.setOrderedToSit(!ship.isOrderedToSit());
        ShinColleSoundHelper.playShipVoice(player.level(), player, ShipSoundType.IDLE, 0.55F,
                ShinColleSoundHelper.variedPitch(player, 1.0F, 0.08F));
        player.displayClientMessage(Component.translatable("chat.shincolle.entity.status",
                ship.getName().copy().withStyle(net.minecraft.ChatFormatting.AQUA), ship.getEscortModeLabel()), true);
        return true;
    }

    @Override
    public boolean stillValid(Player player) {
        LegacyShipEntity ship = this.getShip();
        return ship != null
                && ship.isAlive()
                && ship.isOwnedBy(player)
                && player.distanceToSqr(ship) <= 64.0D;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = this.slots.get(index);
        if (!slot.hasItem()) {
            return ItemStack.EMPTY;
        }

        ItemStack stackInSlot = slot.getItem();
        ItemStack copy = stackInSlot.copy();
        boolean moved;

        if (index < PLAYER_SLOT_START) {
            moved = this.moveItemStackTo(stackInSlot, PLAYER_SLOT_START, SLOT_END, true);
        } else {
            moved = false;

            LegacyShipEntity ship = this.getShip();
            if (ship != null && ship.canEquip(stackInSlot)) {
                moved = this.moveItemStackTo(stackInSlot, EQUIPMENT_SLOT_START, CARGO_SLOT_START, false);
            }

            if (!moved) {
                moved = this.moveItemStackTo(stackInSlot, CARGO_SLOT_START, PLAYER_SLOT_START, false);
            }

            if (!moved && index < HOTBAR_SLOT_START) {
                moved = this.moveItemStackTo(stackInSlot, HOTBAR_SLOT_START, SLOT_END, false);
            }

            if (!moved && index >= HOTBAR_SLOT_START) {
                moved = this.moveItemStackTo(stackInSlot, PLAYER_SLOT_START, HOTBAR_SLOT_START, false);
            }
        }

        if (!moved) {
            return ItemStack.EMPTY;
        }

        if (stackInSlot.isEmpty()) {
            slot.set(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }

        slot.onTake(player, stackInSlot);
        return copy;
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        this.shipInventory.stopOpen(player);
    }
    private static @Nullable LegacyShipEntity resolveShip(Inventory inventory, int shipId) {
        return inventory.player.level().getEntity(shipId) instanceof LegacyShipEntity ship ? ship : null;
    }
}
