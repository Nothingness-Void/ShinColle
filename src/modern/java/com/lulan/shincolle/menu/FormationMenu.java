package com.lulan.shincolle.menu;

import com.lulan.shincolle.entity.ship.LegacyShipEntity;
import com.lulan.shincolle.registry.ModMenus;
import com.lulan.shincolle.teitoku.TeitokuData;
import com.lulan.shincolle.teitoku.TeitokuHelper;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;

import java.util.Optional;

public class FormationMenu extends AbstractContainerMenu {

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

        return Component.translatable("gui.shincolle.formation.slot.uid", slot + 1, shipUid);
    }

    public int countCurrentTeamShips() {
        return this.withData(data -> data.countShipsInTeam(data.getCurrentTeamId())).orElse(0);
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
                candidate -> candidate.getShipUid() == shipUid)) {
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
}
