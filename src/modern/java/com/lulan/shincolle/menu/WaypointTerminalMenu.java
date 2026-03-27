package com.lulan.shincolle.menu;

import com.lulan.shincolle.blockentity.WaypointBlockEntity;
import com.lulan.shincolle.registry.ModMenus;
import com.lulan.shincolle.registry.ModSoundEvents;
import com.lulan.shincolle.sound.ShinColleSoundHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;

public class WaypointTerminalMenu extends AbstractContainerMenu {

    public static final int BUTTON_CYCLE_STAY = 0;
    public static final int BUTTON_CLEAR_NEXT = 1;
    public static final int BUTTON_CLEAR_LAST = 2;
    public static final int BUTTON_CLEAR_CHEST = 3;

    private final Inventory inventory;
    private final BlockPos waypointPos;

    public WaypointTerminalMenu(int containerId, Inventory inventory, BlockPos waypointPos) {
        super(ModMenus.WAYPOINT_TERMINAL.get(), containerId);
        this.inventory = inventory;
        this.waypointPos = waypointPos.immutable();
    }

    public static WaypointTerminalMenu fromNetwork(int containerId, Inventory inventory, FriendlyByteBuf buffer) {
        return new WaypointTerminalMenu(containerId, inventory, buffer.readBlockPos());
    }

    public BlockPos getWaypointPos() {
        return this.waypointPos;
    }

    public @Nullable WaypointBlockEntity getWaypoint() {
        return this.inventory.player.level().getBlockEntity(this.waypointPos) instanceof WaypointBlockEntity waypoint ? waypoint : null;
    }

    public boolean canEdit() {
        WaypointBlockEntity waypoint = this.getWaypoint();
        return waypoint != null && waypoint.canEdit(this.inventory.player);
    }

    public Component getOwnerLabel() {
        WaypointBlockEntity waypoint = this.getWaypoint();
        return waypoint != null ? waypoint.getOwnerLabel() : Component.translatable("gui.shincolle.waypoint.owner.unassigned");
    }

    public Component getStayLabel() {
        WaypointBlockEntity waypoint = this.getWaypoint();
        return waypoint != null ? waypoint.getStayLabel() : Component.translatable("gui.shincolle.waypoint.stay.off");
    }

    public @Nullable BlockPos getLastWaypoint() {
        WaypointBlockEntity waypoint = this.getWaypoint();
        return waypoint != null ? waypoint.getLastWaypoint() : null;
    }

    public @Nullable BlockPos getNextWaypoint() {
        WaypointBlockEntity waypoint = this.getWaypoint();
        return waypoint != null ? waypoint.getNextWaypoint() : null;
    }

    public @Nullable BlockPos getPairedChest() {
        WaypointBlockEntity waypoint = this.getWaypoint();
        return waypoint != null ? waypoint.getPairedChest() : null;
    }

    @Override
    public boolean clickMenuButton(Player player, int id) {
        if (!(player.level().getBlockEntity(this.waypointPos) instanceof WaypointBlockEntity waypoint)) {
            return false;
        }

        if (!waypoint.canEdit(player)) {
            player.displayClientMessage(Component.translatable("chat.shincolle.wrongowner"), true);
            return false;
        }

        switch (id) {
            case BUTTON_CYCLE_STAY -> waypoint.cycleStayMode();
            case BUTTON_CLEAR_NEXT -> waypoint.setNextWaypoint(null);
            case BUTTON_CLEAR_LAST -> waypoint.setLastWaypoint(null);
            case BUTTON_CLEAR_CHEST -> waypoint.setPairedChest(null);
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
        return player.level().getBlockEntity(this.waypointPos) instanceof WaypointBlockEntity
                && player.distanceToSqr(this.waypointPos.getX() + 0.5D, this.waypointPos.getY() + 0.5D, this.waypointPos.getZ() + 0.5D) <= 64.0D;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }
}
