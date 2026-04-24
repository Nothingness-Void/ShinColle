package com.lulan.shincolle.menu;

import com.lulan.shincolle.entity.ship.LegacyShipEntity;
import com.lulan.shincolle.entity.ship.ShipEntitySpec;
import com.lulan.shincolle.entity.ship.ShipEntitySpecs;
import com.lulan.shincolle.teitoku.ShipWorldCacheEntry;
import com.lulan.shincolle.registry.ModMenus;
import com.lulan.shincolle.teitoku.TeitokuData;
import com.lulan.shincolle.teitoku.TeitokuHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.Locale;

public class FormationMenu extends AbstractContainerMenu {

    public record SlotSnapshot(
            int slot,
            int shipUid,
            boolean empty,
            boolean dead,
            boolean online,
            Component displayName,
            Component roleLabel,
            int shipLevel,
            String moraleText,
            String fuelText,
            String lightAmmoText,
            String heavyAmmoText,
            String grudgeText,
            Component modeLabel,
            Component marriageLabel
    ) {
        private static SlotSnapshot empty(int slot) {
            return new SlotSnapshot(
                    slot,
                    -1,
                    true,
                    false,
                    false,
                    Component.translatable("gui.shincolle.formation.slot.empty", slot + 1),
                    Component.translatable("gui.shincolle.ship_inventory.role.unknown"),
                    0,
                    "-",
                    "-",
                    "-",
                    "-",
                    "-",
                    Component.literal("-"),
                    Component.translatable("gui.shincolle.ship_inventory.marriage.no"));
        }
    }

    private final Inventory inventory;

    public FormationMenu(int containerId, Inventory inventory) {
        super(ModMenus.FORMATION.get(), containerId);
        this.inventory = inventory;
    }

    public static FormationMenu fromNetwork(int containerId, Inventory inventory, FriendlyByteBuf buffer) {
        return new FormationMenu(containerId, inventory);
    }

    public int getCurrentTeamId() {
        return this.withData(TeitokuData::getCurrentTeamId).orElse(0);
    }

    public int getCurrentFormationId() {
        return this.withData(TeitokuData::getCurrentFormationId).orElse(TeitokuData.DEFAULT_FORMATION_ID);
    }

    public String getCurrentTeamName() {
        return this.withData(data -> data.getTeamName(data.getCurrentTeamId())).orElse("");
    }

    public int getShipUid(int slot) {
        return this.withData(data -> data.getShipUid(data.getCurrentTeamId(), slot)).orElse(-1);
    }

    public boolean isSlotSelected(int slot) {
        return this.withData(data -> data.isShipSelected(data.getCurrentTeamId(), slot)).orElse(false);
    }

    public Component getSlotLabel(int slot) {
        int shipUid = this.getShipUid(slot);
        if (shipUid <= 0) {
            return Component.translatable("gui.shincolle.formation.slot.empty", slot + 1);
        }

        LegacyShipEntity ship = this.findShipByUid(shipUid);
        if (ship != null) {
            return Component.translatable("gui.shincolle.formation.slot.filled", slot + 1, ship.getName());
        }

        ShipWorldCacheEntry cached = TeitokuHelper.getClientShipCacheEntry(shipUid);
        if (cached != null) {
            Component name = cached.resolveDisplayName();
            if (cached.dead()) {
                name = name.copy().withStyle(ChatFormatting.DARK_GRAY);
            }
            return Component.translatable("gui.shincolle.formation.slot.filled", slot + 1, name);
        }

        return Component.translatable("gui.shincolle.formation.slot.uid", slot + 1, shipUid);
    }

    public int countCurrentTeamShips() {
        return this.withData(data -> data.countShipsInTeam(data.getCurrentTeamId())).orElse(0);
    }

    public SlotSnapshot getSlotSnapshot(int slot) {
        int shipUid = this.getShipUid(slot);
        if (shipUid <= 0) {
            return SlotSnapshot.empty(slot);
        }

        LegacyShipEntity ship = this.findShipByUid(shipUid);
        if (ship != null) {
            return new SlotSnapshot(
                    slot,
                    shipUid,
                    false,
                    false,
                    true,
                    ship.getName(),
                    Component.translatable("gui.shincolle.ship_inventory.role."
                            + ship.getSpec().archetype().name().toLowerCase(Locale.ROOT)),
                    ship.getShipLevel(),
                    ship.getMorale() + " / 16000",
                    ship.getShipFuelText(),
                    ship.getLightAmmoText(),
                    ship.getHeavyAmmoText(),
                    ship.getGrudgeText(),
                    ship.getEscortModeLabel(),
                    Component.translatable(ship.isMarried()
                            ? "gui.shincolle.ship_inventory.marriage.yes"
                            : "gui.shincolle.ship_inventory.marriage.no"));
        }

        ShipWorldCacheEntry cached = TeitokuHelper.getClientShipCacheEntry(shipUid);
        if (cached == null) {
            return new SlotSnapshot(
                    slot,
                    shipUid,
                    false,
                    false,
                    false,
                    Component.translatable("gui.shincolle.formation.slot.uid", slot + 1, shipUid),
                    Component.translatable("gui.shincolle.ship_inventory.role.unknown"),
                    0,
                    "-",
                    "-",
                    "-",
                    "-",
                    "-",
                    Component.literal("-"),
                    Component.translatable("gui.shincolle.ship_inventory.marriage.no"));
        }

        ShipEntitySpec spec = this.resolveCachedSpec(cached);
        Component roleLabel = spec == null
                ? Component.translatable("gui.shincolle.ship_inventory.role.unknown")
                : Component.translatable("gui.shincolle.ship_inventory.role."
                + spec.archetype().name().toLowerCase(Locale.ROOT));
        Component modeLabel = Component.translatable(cached.isOrderedToSit()
                ? "gui.shincolle.entity.mode.standby"
                : "gui.shincolle.entity.mode.follow");
        Component marriageLabel = Component.translatable(cached.isMarried()
                ? "gui.shincolle.ship_inventory.marriage.yes"
                : "gui.shincolle.ship_inventory.marriage.no");

        return new SlotSnapshot(
                slot,
                shipUid,
                false,
                cached.dead(),
                cached.online(),
                cached.resolveDisplayName(),
                roleLabel,
                cached.getShipLevel(),
                cached.getMorale() + " / 16000",
                cached.getShipFuel() + " / " + LegacyShipEntity.MAX_SHIP_FUEL,
                cached.getLightAmmo() + " / " + LegacyShipEntity.MAX_LIGHT_AMMO,
                cached.getHeavyAmmo() + " / " + LegacyShipEntity.MAX_HEAVY_AMMO,
                cached.getGrudge() + " / " + LegacyShipEntity.MAX_GRUDGE,
                modeLabel,
                marriageLabel);
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }

    private LegacyShipEntity findShipByUid(int shipUid) {
        for (LegacyShipEntity ship : this.inventory.player.level().getEntitiesOfClass(
                LegacyShipEntity.class,
                this.inventory.player.getBoundingBox().inflate(256.0D),
                candidate -> candidate.getShipUid() == shipUid && !candidate.isRemoved())) {
            if (ship.canCommanderEdit(this.inventory.player)) {
                return ship;
            }
        }
        return null;
    }

    private <T> Optional<T> withData(java.util.function.Function<TeitokuData, T> mapper) {
        TeitokuData data = TeitokuHelper.get(this.inventory.player).orElse(null);
        if (data == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(mapper.apply(data));
    }

    private @Nullable ShipEntitySpec resolveCachedSpec(@Nullable ShipWorldCacheEntry entry) {
        if (entry == null) {
            return null;
        }

        ShipEntitySpec spec = ShipEntitySpecs.findByEggMeta(entry.variantEggMeta());
        if (spec == null) {
            spec = ShipEntitySpecs.findByLegacyClassId(entry.legacyClassId());
        }
        return spec;
    }
}
