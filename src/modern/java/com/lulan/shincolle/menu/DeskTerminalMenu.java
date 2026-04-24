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
import java.util.Locale;
import java.util.Set;

public class DeskTerminalMenu extends AbstractContainerMenu {

    public enum TeamRelation {
        OWN("gui.shincolle.team.belong"),
        ALLIED("gui.shincolle.team.allied"),
        HOSTILE("gui.shincolle.team.hostile"),
        NEUTRAL("gui.shincolle.team.neutral");

        private final String translationKey;

        TeamRelation(String translationKey) {
            this.translationKey = translationKey;
        }

        public Component label() {
            return Component.translatable(this.translationKey);
        }
    }

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

    public TeamRelation getTeamRelation(int teamId) {
        if (teamId <= 0) {
            return TeamRelation.NEUTRAL;
        }
        if (teamId == this.getOwnTeamId()) {
            return TeamRelation.OWN;
        }

        TeamData ownTeam = TeitokuHelper.getClientTeamData().get(this.getOwnTeamId());
        if (ownTeam == null) {
            return TeamRelation.NEUTRAL;
        }
        if (ownTeam.isAlly(teamId)) {
            return TeamRelation.ALLIED;
        }
        if (ownTeam.isBanned(teamId)) {
            return TeamRelation.HOSTILE;
        }
        return TeamRelation.NEUTRAL;
    }

    public Component getTeamRelationLabel(int teamId) {
        return this.getTeamRelation(teamId).label();
    }

    public int getOwnAllyCount() {
        TeamData ownTeam = TeitokuHelper.getClientTeamData().get(this.getOwnTeamId());
        return ownTeam == null ? 0 : ownTeam.getAllies().size();
    }

    public int getOwnBannedCount() {
        TeamData ownTeam = TeitokuHelper.getClientTeamData().get(this.getOwnTeamId());
        return ownTeam == null ? 0 : ownTeam.getBanned().size();
    }

    public List<String> getWorldUnattackableClasses() {
        Set<String> classes = TeitokuHelper.getClientWorldUnattackableClasses();
        if (classes.isEmpty()) {
            return List.of();
        }

        List<String> sorted = new ArrayList<>(classes);
        sorted.sort(String::compareToIgnoreCase);
        return List.copyOf(sorted);
    }

    public String getCompactTeamRow(TeamData teamData) {
        String relation = this.getTeamRelationLabel(teamData.getTeamId()).getString();
        return "#" + teamData.getTeamId() + " "
                + relation + " "
                + teamData.getTeamName().trim();
    }

    public int getTeamListOffset() {
        List<TeamData> teams = this.getKnownTeams();
        if (teams.size() <= 8) {
            return 0;
        }

        int currentTeamId = this.getCurrentTeamId();
        int currentIndex = 0;
        for (int i = 0; i < teams.size(); i++) {
            if (teams.get(i).getTeamId() == currentTeamId) {
                currentIndex = i;
                break;
            }
        }

        return Math.max(0, Math.min(currentIndex - 3, teams.size() - 8));
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
