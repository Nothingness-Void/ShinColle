package com.lulan.shincolle.menu;

import com.lulan.shincolle.entity.ship.LegacyShipAttackProfile;
import com.lulan.shincolle.entity.ship.LegacyShipEntity;
import com.lulan.shincolle.entity.ship.LegacyShipStats;
import com.lulan.shincolle.entity.ship.ShipEquipmentBehaviorState;
import com.lulan.shincolle.entity.ship.ShipEquipmentProfile;
import com.lulan.shincolle.item.equipment.LegacyEquipmentItem;
import com.lulan.shincolle.morph.MorphProfile;
import com.lulan.shincolle.morph.MorphRuntimeState;
import com.lulan.shincolle.registry.ModMenus;
import com.lulan.shincolle.teitoku.TeitokuData;
import com.lulan.shincolle.teitoku.TeitokuHelper;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nullable;
import java.util.Locale;

public class MorphInventoryMenu extends AbstractContainerMenu {

    public static final int BUTTON_UNUSED = 0;

    private static final int EQUIPMENT_SLOT_START = 0;
    private static final int PLAYER_SLOT_START = EQUIPMENT_SLOT_START + LegacyShipEntity.EQUIPMENT_SLOT_COUNT;
    private static final int HOTBAR_SLOT_START = PLAYER_SLOT_START + 27;
    private static final int SLOT_END = HOTBAR_SLOT_START + 9;

    private final Inventory playerInventory;
    private final int selectedClassId;
    private final ItemStackHandler equipmentHandler;

    public MorphInventoryMenu(int containerId, Inventory playerInventory) {
        this(containerId, playerInventory, resolveSelectedClassId(playerInventory.player));
    }

    public MorphInventoryMenu(int containerId, Inventory playerInventory, int selectedClassId) {
        super(ModMenus.MORPH_INVENTORY.get(), containerId);
        this.playerInventory = playerInventory;
        this.selectedClassId = selectedClassId;
        MorphProfile profile = this.getProfile();
        this.equipmentHandler = profile != null ? profile.getEquipment() : new ItemStackHandler(LegacyShipEntity.EQUIPMENT_SLOT_COUNT);

        for (int slot = 0; slot < LegacyShipEntity.EQUIPMENT_SLOT_COUNT; slot++) {
            this.addSlot(new MorphEquipmentSlot(this.equipmentHandler, slot, 144, 18 + slot * 18, this::isValidMorphEquipment));
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

    public static MorphInventoryMenu fromNetwork(int containerId, Inventory inventory, FriendlyByteBuf buffer) {
        return new MorphInventoryMenu(containerId, inventory, buffer == null ? resolveSelectedClassId(inventory.player) : buffer.readVarInt());
    }

    public @Nullable MorphProfile getProfile() {
        return TeitokuHelper.get(this.playerInventory.player)
                .map(data -> data.findMorphProfile(this.selectedClassId))
                .orElse(null);
    }

    public int getSelectedClassId() {
        return this.selectedClassId;
    }

    public boolean hasProfile() {
        return this.getProfile() != null;
    }

    public boolean isActiveMorph() {
        return TeitokuHelper.get(this.playerInventory.player)
                .map(data -> data.getMorphRuntimeState().isActive() && data.getMorphRuntimeState().getSelectedClassId() == this.selectedClassId)
                .orElse(false);
    }

    public MorphRuntimeState getRuntimeState() {
        return TeitokuHelper.get(this.playerInventory.player)
                .map(TeitokuData::getMorphRuntimeState)
                .orElseGet(MorphRuntimeState::new);
    }

    public Component getTitleLabel() {
        MorphProfile profile = this.getProfile();
        return profile == null ? Component.translatable("gui.shincolle.morph_inventory.empty") : profile.getSpec().displayName();
    }

    public Component getHostModeLabel() {
        return Component.translatable("gui.shincolle.morph_inventory.host_mode." + this.getRuntimeState().getHostMode().name().toLowerCase(Locale.ROOT));
    }

    public Component getStatusLabel() {
        return Component.translatable(this.isActiveMorph()
                ? "gui.shincolle.morph_inventory.active"
                : "gui.shincolle.morph_inventory.inactive");
    }

    public int getProfileIndex() {
        return TeitokuHelper.get(this.playerInventory.player)
                .map(data -> {
                    int index = 0;
                    for (MorphProfile morphProfile : data.getMorphProfiles()) {
                        if (morphProfile.getLegacyClassId() == this.selectedClassId) {
                            return index;
                        }
                        index++;
                    }
                    return 0;
                })
                .orElse(0);
    }

    public int getProfileCount() {
        return TeitokuHelper.get(this.playerInventory.player)
                .map(TeitokuData::getMorphProfileCount)
                .orElse(0);
    }

    public boolean isValidMorphEquipment(ItemStack stack) {
        MorphProfile profile = this.getProfile();
        if (profile == null) {
            return stack.getItem() instanceof LegacyEquipmentItem;
        }

        return stack.isEmpty()
                || stack.is(Items.AIR)
                || ShipEquipmentProfile.canEquip(profile.getSpec().archetype(), stack);
    }

    public LegacyShipStats getLegacyStats() {
        MorphProfile profile = this.getProfile();
        return profile == null ? null : profile.buildStats(this.playerInventory.player.getActiveEffects());
    }

    public ShipEquipmentBehaviorState getBehaviorState() {
        MorphProfile profile = this.getProfile();
        return profile == null ? ShipEquipmentBehaviorState.EMPTY : profile.buildBehaviorState();
    }

    public LegacyShipAttackProfile getAttackProfile() {
        MorphProfile profile = this.getProfile();
        return profile == null ? LegacyShipAttackProfile.MELEE_ONLY : profile.buildAttackProfile();
    }

    public String getLevelText() {
        MorphProfile profile = this.getProfile();
        return profile == null ? "-" : Integer.toString(profile.getLevel());
    }

    public String getAttackText() {
        LegacyShipStats stats = this.getLegacyStats();
        return stats == null ? "-" : String.format(Locale.ROOT, "%.1f", stats.attackLight());
    }

    public String getAirAttackText() {
        LegacyShipStats stats = this.getLegacyStats();
        return stats == null ? "-" : String.format(Locale.ROOT, "%.1f", stats.attackAirLight());
    }

    public String getDefenseText() {
        LegacyShipStats stats = this.getLegacyStats();
        return stats == null ? "-" : String.format(Locale.ROOT, "%.2f", stats.defense());
    }

    public String getSpeedText() {
        LegacyShipStats stats = this.getLegacyStats();
        return stats == null ? "-" : String.format(Locale.ROOT, "%.2f", stats.attackSpeed());
    }

    public String getMoveText() {
        LegacyShipStats stats = this.getLegacyStats();
        return stats == null ? "-" : String.format(Locale.ROOT, "%.2f", stats.moveSpeed());
    }

    public String getRangeText() {
        LegacyShipStats stats = this.getLegacyStats();
        return stats == null ? "-" : String.format(Locale.ROOT, "%.1f", stats.attackRange());
    }

    public String getMoraleText() {
        MorphProfile profile = this.getProfile();
        return profile == null ? "-" : profile.getMorale() + " / " + MorphProfile.MAX_MORALE;
    }

    public String getAmmoLightText() {
        MorphProfile profile = this.getProfile();
        return profile == null ? "-" : Integer.toString(profile.getAmmoLight());
    }

    public String getAmmoHeavyText() {
        MorphProfile profile = this.getProfile();
        return profile == null ? "-" : Integer.toString(profile.getAmmoHeavy());
    }

    public String getGrudgeText() {
        MorphProfile profile = this.getProfile();
        return profile == null ? "-" : Integer.toString(profile.getGrudge());
    }

    public Component getMarriageLabel() {
        MorphProfile profile = this.getProfile();
        return Component.translatable(profile != null && profile.isMarried()
                ? "gui.shincolle.ship_inventory.marriage.yes"
                : "gui.shincolle.ship_inventory.marriage.no");
    }

    public int getModernizationCount() {
        MorphProfile profile = this.getProfile();
        return profile == null ? 0 : profile.getModernizationDisplayCount();
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

    @Override
    public boolean stillValid(Player player) {
        return true;
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

            if (this.isValidMorphEquipment(stackInSlot)) {
                moved = this.moveItemStackTo(stackInSlot, EQUIPMENT_SLOT_START, PLAYER_SLOT_START, false);
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
        if (player instanceof net.minecraft.server.level.ServerPlayer serverPlayer) {
            TeitokuHelper.syncGameplayState(serverPlayer);
        }
    }

    private static int resolveSelectedClassId(Player player) {
        return TeitokuHelper.get(player)
                .map(data -> data.getMorphRuntimeState().getSelectedClassId())
                .orElse(0);
    }
}
