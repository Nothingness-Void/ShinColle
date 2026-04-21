package com.lulan.shincolle.network;

import com.lulan.shincolle.combat.WorldCombatRulesSavedData;
import com.lulan.shincolle.entity.ship.LegacyShipEntity;
import com.lulan.shincolle.formation.FormationRuntimeState;
import com.lulan.shincolle.menu.DeskReferenceMenu;
import com.lulan.shincolle.menu.DeskTerminalMenu;
import com.lulan.shincolle.menu.FormationMenu;
import com.lulan.shincolle.menu.ShipInventoryMenu;
import com.lulan.shincolle.morph.MorphHelper;
import com.lulan.shincolle.team.TeamSavedData;
import com.lulan.shincolle.teitoku.TeitokuData;
import com.lulan.shincolle.teitoku.TeitokuHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraftforge.network.NetworkHooks;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class GameplayCommandHandler {

    private static final double MAX_SHIP_COMMAND_RANGE_SQR = 256.0D * 256.0D;
    private static final double MAX_TARGET_COMMAND_RANGE_SQR = 256.0D * 256.0D;
    private static final double MAX_MOVE_COMMAND_RANGE_SQR = 320.0D * 320.0D;

    public static final String TAG_SHIP_ID = "ShipId";
    public static final String TAG_TARGET_ID = "TargetId";
    public static final String TAG_TEAM_ID = "TeamId";
    public static final String TAG_SLOT = "Slot";
    public static final String TAG_SLOT_FROM = "FromSlot";
    public static final String TAG_SLOT_TO = "ToSlot";
    public static final String TAG_SELECTED = "Selected";
    public static final String TAG_MODE = "Mode";
    public static final String TAG_X = "X";
    public static final String TAG_Y = "Y";
    public static final String TAG_Z = "Z";
    public static final String TAG_SHIP_UID = "ShipUid";
    public static final String TAG_TARGET_CLASS = "TargetClass";
    public static final String TAG_RELATION_TEAM_ID = "RelationTeamId";
    public static final String TAG_TEAM_NAME = "TeamName";
    public static final String TAG_AI_FLAGS = "AiFlags";
    public static final String TAG_FOLLOW_RANGE = "FollowRange";
    public static final String TAG_ATTACK_KIND = "AttackKind";
    public static final String TAG_SKILL_SLOT = "SkillSlot";

    public static final int AI_FLAG_AUTO_TARGET = 1;
    public static final int AI_FLAG_ALLOW_PVP = 1 << 1;
    public static final int AI_FLAG_AUTO_SUPPLY = 1 << 2;
    public static final int AI_FLAG_ROUTE_STAY = 1 << 3;

    private GameplayCommandHandler() {
    }

    public static void handle(ServerPlayer player, GameplayCommandType type, CompoundTag payload) {
        boolean broadcastTeamState = false;

        switch (type) {
            case SET_POINTER_MODE -> handleSetPointerMode(player, payload);
            case TOGGLE_SIT_SINGLE, TOGGLE_SIT_GROUP -> ShipCommandService.handleLegacy(player, type, payload);
            case CYCLE_FORMATION -> {
                TeitokuHelper.cycleFormationId(player);
                player.displayClientMessage(Component.translatable("chat.shincolle.pointer.formation_changed",
                        TeitokuHelper.getFormationLabel(player)), true);
            }
            case SET_CURRENT_TEAM -> {
                int teamId = Mth.clamp(payload.getInt(TAG_TEAM_ID), 0, TeitokuData.TEAM_COUNT - 1);
                TeitokuHelper.setCurrentTeamId(player, teamId);
                player.displayClientMessage(Component.translatable("chat.shincolle.team.current",
                        teamId + 1), true);
            }
            case ASSIGN_SHIP_SLOT -> handleAssignShip(player, payload);
            case SWAP_TEAM_SLOT -> handleSwapTeamSlots(player, payload);
            case TOGGLE_SHIP_SELECT -> handleToggleShipSelect(player, payload);
            case SET_SLOT_SELECTION -> handleSetSlotSelection(player, payload);
            case CLEAR_CURRENT_TEAM -> handleClearCurrentTeam(player);
            case MOVE_TO_POS, GUARD_ENTITY, ATTACK_ENTITY, STOP_COMMAND -> ShipCommandService.handleLegacy(player, type, payload);
            case TOGGLE_TARGET_CLASS -> {
                handleToggleTargetClass(player, payload);
                broadcastTeamState = true;
            }
            case TARGET_CLASS_ADD -> {
                handleTargetClassAddRemove(player, payload, true);
                broadcastTeamState = true;
            }
            case TARGET_CLASS_REMOVE -> {
                handleTargetClassAddRemove(player, payload, false);
                broadcastTeamState = true;
            }
            case OPEN_SHIP_INVENTORY -> ShipCommandService.handleLegacy(player, type, payload);
            case OPEN_FORMATION_SCREEN -> handleOpenFormationScreen(player);
            case OPEN_DESK_SCREEN -> handleOpenDeskScreen(player, payload);
            case OPEN_MORPH_SCREEN -> MorphHelper.openMorphScreen(player);
            case SET_SHIP_AI_FLAGS, SET_SHIP_FOLLOW_RANGE -> ShipCommandService.handleLegacy(player, type, payload);
            case MORPH_CYCLE_PROFILE_PREV -> handleMorphCycle(player, false);
            case MORPH_CYCLE_PROFILE_NEXT -> handleMorphCycle(player, true);
            case MORPH_TOGGLE_ACTIVE -> handleMorphToggle(player);
            case MORPH_TOGGLE_MOUNT -> handleMorphToggleMount(player);
            case PLAYER_CAST_SKILL -> handlePlayerCastSkill(player, payload);
            case MORPH_CAST_ATTACK -> handleMorphAttack(player, payload);
            case MORPH_CAST_SPECIAL -> handleMorphSpecial(player, payload);
            case DESK_CREATE_TEAM -> {
                handleDeskCreate(player, payload);
                broadcastTeamState = true;
            }
            case DESK_DISBAND_TEAM -> {
                handleDeskDisband(player);
                broadcastTeamState = true;
            }
            case DESK_RENAME_TEAM -> {
                handleDeskRename(player, payload);
                broadcastTeamState = true;
            }
            case DESK_ADD_ALLY -> {
                handleDeskAlly(player, payload, true);
                broadcastTeamState = true;
            }
            case DESK_REMOVE_ALLY -> {
                handleDeskAlly(player, payload, false);
                broadcastTeamState = true;
            }
            case DESK_ADD_BANNED -> {
                handleDeskBanned(player, payload, true);
                broadcastTeamState = true;
            }
            case DESK_REMOVE_BANNED -> {
                handleDeskBanned(player, payload, false);
                broadcastTeamState = true;
            }
            case OPTOOL_TOGGLE_UNATTACKABLE -> handleOpToolToggleUnattackable(player, payload);
            case OPTOOL_PRINT_UNATTACKABLE -> handleOpToolPrintUnattackable(player);
            default -> {
            }
        }

        if (broadcastTeamState) {
            TeitokuHelper.syncGameplayStateToRelevantPlayers(player);
        } else {
            TeitokuHelper.syncGameplayState(player);
        }
    }

    private static void handleAssignShip(ServerPlayer player, CompoundTag payload) {
        int slot = Mth.clamp(payload.getInt(TAG_SLOT), 0, TeitokuData.TEAM_SIZE - 1);
        boolean hasShipUid = payload.contains(TAG_SHIP_UID);
        int shipUid = hasShipUid ? payload.getInt(TAG_SHIP_UID) : 0;
        if (hasShipUid && shipUid <= 0) {
            TeitokuHelper.assignShipToCurrentTeam(player, slot, -1);
            return;
        }

        LegacyShipEntity ship = null;
        if (player.level().getEntity(payload.getInt(TAG_SHIP_ID)) instanceof LegacyShipEntity byId
                && byId.canCommanderEdit(player)) {
            ship = byId;
        }

        if (ship == null && shipUid > 0) {
            ship = TeitokuHelper.findOwnedShipByUid(player, shipUid);
        }

        if (ship != null) {
            shipUid = ship.getShipUid();
        }

        TeitokuHelper.assignShipToCurrentTeam(player, slot, shipUid);
    }

    private static void handleSwapTeamSlots(ServerPlayer player, CompoundTag payload) {
        int fromSlot = payload.contains(TAG_SLOT_FROM)
                ? payload.getInt(TAG_SLOT_FROM)
                : payload.getInt(TAG_SLOT);
        int toSlot = payload.contains(TAG_SLOT_TO)
                ? payload.getInt(TAG_SLOT_TO)
                : payload.getInt(TAG_TARGET_ID);
        TeitokuHelper.swapCurrentTeamSlots(player,
                Mth.clamp(fromSlot, 0, TeitokuData.TEAM_SIZE - 1),
                Mth.clamp(toSlot, 0, TeitokuData.TEAM_SIZE - 1));
    }

    private static void handleToggleShipSelect(ServerPlayer player, CompoundTag payload) {
        int slot = Mth.clamp(payload.getInt(TAG_SLOT), 0, TeitokuData.TEAM_SIZE - 1);
        TeitokuHelper.toggleCurrentTeamSelect(player, slot);
    }

    private static void handleSetSlotSelection(ServerPlayer player, CompoundTag payload) {
        int slot = Mth.clamp(payload.getInt(TAG_SLOT), 0, TeitokuData.TEAM_SIZE - 1);
        boolean selected = payload.getBoolean(TAG_SELECTED);
        TeitokuHelper.setCurrentTeamSelect(player, slot, selected);
    }

    private static void handleClearCurrentTeam(ServerPlayer player) {
        TeitokuHelper.clearCurrentTeam(player);
    }

    private static void handleSetPointerMode(ServerPlayer player, CompoundTag payload) {
        if (!(player.getMainHandItem().getItem() instanceof com.lulan.shincolle.item.PointerItem)) {
            return;
        }

        int mode = Mth.clamp(payload.getInt(TAG_MODE), 0, 5);
        com.lulan.shincolle.item.PointerItem.setMode(player.getMainHandItem(), mode);
        player.displayClientMessage(Component.translatable("chat.shincolle.pointer.mode_changed",
                com.lulan.shincolle.item.PointerItem.getModeName(mode)), true);
    }

    private static void handleToggleTargetClass(ServerPlayer player, CompoundTag payload) {
        String targetClass = payload.getString(TAG_TARGET_CLASS);
        if (targetClass == null || targetClass.isBlank()) {
            return;
        }
        TeitokuHelper.toggleTargetClass(player, sanitizeTargetClass(targetClass));
    }

    private static void handleTargetClassAddRemove(ServerPlayer player, CompoundTag payload, boolean add) {
        String targetClass = sanitizeTargetClass(payload.getString(TAG_TARGET_CLASS));
        if (targetClass.isBlank()) {
            return;
        }

        if (add) {
            TeitokuHelper.addTargetClass(player, targetClass);
        } else {
            TeitokuHelper.removeTargetClass(player, targetClass);
        }
    }

    private static void handleOpenFormationScreen(ServerPlayer player) {
        NetworkHooks.openScreen(player,
                new SimpleMenuProvider(
                        (containerId, inventory, menuPlayer) -> new FormationMenu(containerId, inventory),
                        Component.translatable("gui.shincolle.formation.title")));
    }

    private static void handleOpenDeskScreen(ServerPlayer player, CompoundTag payload) {
        int selectedFunction = Mth.clamp(payload.getInt(TAG_MODE),
                DeskReferenceMenu.RADAR_VARIANT,
                DeskReferenceMenu.BOOK_VARIANT);
        NetworkHooks.openScreen(player,
                new SimpleMenuProvider(
                        (containerId, inventory, menuPlayer) ->
                                new DeskTerminalMenu(containerId, inventory, player.blockPosition(), selectedFunction),
                        Component.translatable("block.shincolle.blockdesk")),
                buffer -> {
                    buffer.writeBlockPos(player.blockPosition());
                    buffer.writeVarInt(selectedFunction);
                });
    }

    private static void handleMorphCycle(ServerPlayer player, boolean forward) {
        if (MorphHelper.cycleProfile(player, forward)) {
            MorphHelper.openMorphScreen(player);
        }
    }

    private static void handleMorphToggle(ServerPlayer player) {
        if (MorphHelper.toggleActive(player) && player.containerMenu instanceof com.lulan.shincolle.menu.MorphInventoryMenu) {
            MorphHelper.openMorphScreen(player);
        }
    }

    private static void handleMorphToggleMount(ServerPlayer player) {
        if (MorphHelper.toggleMount(player)) {
            TeitokuHelper.syncGameplayState(player);
        }
    }

    private static void handlePlayerCastSkill(ServerPlayer player, CompoundTag payload) {
        BlockPos blockPos = payload.contains(TAG_X) && payload.contains(TAG_Y) && payload.contains(TAG_Z)
                ? new BlockPos(payload.getInt(TAG_X), payload.getInt(TAG_Y), payload.getInt(TAG_Z))
                : null;
        MorphHelper.performPlayerSkill(player,
                payload.contains(TAG_SKILL_SLOT) ? payload.getInt(TAG_SKILL_SLOT) : payload.getInt(TAG_SLOT),
                payload.contains(TAG_TARGET_ID) ? payload.getInt(TAG_TARGET_ID) : -1,
                blockPos);
    }

    private static void handleMorphAttack(ServerPlayer player, CompoundTag payload) {
        MorphHelper.performCompatAttack(player,
                com.lulan.shincolle.entity.ship.LegacyShipAttackKind.byOrdinal(payload.getInt(TAG_ATTACK_KIND)),
                payload.getInt(TAG_TARGET_ID));
    }

    private static void handleMorphSpecial(ServerPlayer player, CompoundTag payload) {
        MorphHelper.performCompatSpecial(player, payload.getInt(TAG_TARGET_ID));
    }

    private static void handleDeskCreate(ServerPlayer player, CompoundTag payload) {
        String teamName = sanitizeTeamName(payload.getString(TAG_TEAM_NAME));
        if (TeitokuHelper.createOwnTeam(player, teamName)) {
            player.displayClientMessage(Component.translatable("chat.shincolle.team.created"), true);
            return;
        }

        player.displayClientMessage(Component.translatable("chat.shincolle.team.action_denied"), true);
    }

    private static void handleDeskDisband(ServerPlayer player) {
        if (TeitokuHelper.disbandOwnTeam(player)) {
            player.displayClientMessage(Component.translatable("chat.shincolle.team.disbanded"), true);
            return;
        }

        player.displayClientMessage(Component.translatable("chat.shincolle.team.action_denied"), true);
    }

    private static void handleDeskRename(ServerPlayer player, CompoundTag payload) {
        String teamName = sanitizeTeamName(payload.getString(TAG_TEAM_NAME));
        if (teamName.isBlank()) {
            return;
        }
        TeitokuHelper.renameOwnTeam(player, teamName);
    }

    private static void handleDeskAlly(ServerPlayer player, CompoundTag payload, boolean add) {
        int relationTeamId = payload.getInt(TAG_RELATION_TEAM_ID);
        TeamSavedData teamData = TeamSavedData.get(player.serverLevel());
        int ownTeamId = TeitokuHelper.getPlayerUid(player);
        if (!TeitokuHelper.hasTeam(player) || relationTeamId <= 0 || ownTeamId <= 0 || relationTeamId == ownTeamId) {
            return;
        }

        boolean changed = add
                ? teamData.addAllyRelationship(ownTeamId, relationTeamId)
                : teamData.removeAllyRelationship(ownTeamId, relationTeamId);
        if (changed) {
            player.displayClientMessage(Component.translatable(add
                    ? "chat.shincolle.team.ally.added"
                    : "chat.shincolle.team.ally.removed", relationTeamId), true);
        }
    }

    private static void handleDeskBanned(ServerPlayer player, CompoundTag payload, boolean add) {
        int relationTeamId = payload.getInt(TAG_RELATION_TEAM_ID);
        TeamSavedData teamData = TeamSavedData.get(player.serverLevel());
        int ownTeamId = TeitokuHelper.getPlayerUid(player);
        if (!TeitokuHelper.hasTeam(player) || relationTeamId <= 0 || ownTeamId <= 0 || relationTeamId == ownTeamId) {
            return;
        }

        boolean changed = add
                ? teamData.addBannedRelationship(ownTeamId, relationTeamId)
                : teamData.removeBannedRelationship(ownTeamId, relationTeamId);
        if (changed) {
            player.displayClientMessage(Component.translatable(add
                    ? "chat.shincolle.team.banned.added"
                    : "chat.shincolle.team.banned.removed", relationTeamId), true);
        }
    }

    private static void handleOpToolToggleUnattackable(ServerPlayer player, CompoundTag payload) {
        String targetClass = sanitizeTargetClass(payload.getString(TAG_TARGET_CLASS));
        if (targetClass.isBlank() && player.level().getEntity(payload.getInt(TAG_TARGET_ID)) instanceof LivingEntity living) {
            targetClass = sanitizeTargetClass(TeitokuHelper.resolveTargetClass(living));
        }
        if (targetClass.isBlank()) {
            return;
        }

        boolean added = WorldCombatRulesSavedData.get(player.serverLevel()).toggleUnattackable(targetClass);
        player.displayClientMessage(Component.literal((added ? "Blocked " : "Allowed ") + targetClass), true);
    }

    private static void handleOpToolPrintUnattackable(ServerPlayer player) {
        List<String> classes = new ArrayList<>(WorldCombatRulesSavedData.get(player.serverLevel()).getAllUnattackableClasses());
        if (classes.isEmpty()) {
            player.displayClientMessage(Component.literal("World unattackable list is empty"), false);
            return;
        }

        player.displayClientMessage(Component.literal("World unattackable: " + String.join(", ", classes)), false);
    }

    private static String sanitizeTargetClass(String raw) {
        if (raw == null) {
            return "";
        }

        String normalized = raw.trim().toLowerCase(Locale.ROOT);
        if (normalized.length() > 96) {
            normalized = normalized.substring(0, 96);
        }
        return normalized;
    }

    private static String sanitizeTeamName(String raw) {
        if (raw == null) {
            return "";
        }

        String normalized = raw.trim();
        if (normalized.length() > 32) {
            normalized = normalized.substring(0, 32);
        }
        return normalized;
    }
}
