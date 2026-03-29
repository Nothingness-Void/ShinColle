package com.lulan.shincolle.menu;

import com.lulan.shincolle.blockentity.DeskBlockEntity;
import com.lulan.shincolle.registry.ModMenus;
import com.lulan.shincolle.team.TeamData;
import com.lulan.shincolle.teitoku.TeitokuData;
import com.lulan.shincolle.teitoku.TeitokuHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class DeskTerminalMenu extends AbstractContainerMenu {

    private final Inventory inventory;
    private final BlockPos deskPos;
    private final int selectedFunction;

    public DeskTerminalMenu(int containerId, Inventory inventory, BlockPos deskPos, int selectedFunction) {
        super(ModMenus.DESK_TERMINAL.get(), containerId);
        this.inventory = inventory;
        this.deskPos = deskPos.immutable();
        this.selectedFunction = selectedFunction == DeskReferenceMenu.BOOK_VARIANT
                ? DeskReferenceMenu.BOOK_VARIANT
                : DeskReferenceMenu.RADAR_VARIANT;
    }

    public static DeskTerminalMenu fromNetwork(int containerId, Inventory inventory, FriendlyByteBuf buffer) {
        return new DeskTerminalMenu(containerId, inventory, buffer.readBlockPos(), buffer.readVarInt());
    }

    public BlockPos getDeskPos() {
        return this.deskPos;
    }

    public int getSelectedFunction() {
        return this.selectedFunction;
    }

    public Component getSelectedFunctionName() {
        return Component.translatable(this.selectedFunction == DeskReferenceMenu.BOOK_VARIANT
                ? "item.shincolle.deskitembook"
                : "item.shincolle.deskitemradar");
    }

    public int getCurrentTeamId() {
        return TeitokuHelper.get(this.inventory.player)
                .map(TeitokuData::getCurrentTeamId)
                .orElse(0);
    }

    public int getCurrentFormationId() {
        return TeitokuHelper.get(this.inventory.player)
                .map(TeitokuData::getFormationId)
                .orElse(0);
    }

    public Component getCurrentTeamLabel() {
        return Component.translatable("gui.shincolle.ship_inventory.team", this.getCurrentTeamId() + 1);
    }

    public Component getCurrentFormationLabel() {
        return Component.translatable("gui.shincolle.formation.format" + this.getCurrentFormationId());
    }

    public boolean hasOwnTeam() {
        return TeitokuHelper.hasTeam(this.inventory.player);
    }

    public int getTeamCooldown() {
        return TeitokuHelper.getTeamCooldown(this.inventory.player);
    }

    public int getOwnTeamId() {
        return TeitokuHelper.getPlayerUid(this.inventory.player);
    }

    public String getOwnTeamName() {
        TeamData own = TeitokuHelper.getClientTeamData().get(this.getOwnTeamId());
        return own == null ? "" : own.getTeamName();
    }

    public List<TeamData> getKnownTeams() {
        List<TeamData> teams = new ArrayList<>(TeitokuHelper.getClientTeamData().values());
        teams.sort(Comparator.comparingInt(TeamData::getTeamId));
        return teams;
    }

    public List<String> getTargetClasses() {
        return TeitokuHelper.get(this.inventory.player)
                .map(TeitokuData::getTargetClasses)
                .orElse(List.of());
    }

    @Override
    public boolean stillValid(Player player) {
        if (player.level().getBlockEntity(this.deskPos) instanceof DeskBlockEntity) {
            return player.distanceToSqr(this.deskPos.getX() + 0.5D, this.deskPos.getY() + 0.5D, this.deskPos.getZ() + 0.5D) <= 64.0D;
        }
        // Allow detached desk terminal sessions opened by command items.
        return true;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }
}
