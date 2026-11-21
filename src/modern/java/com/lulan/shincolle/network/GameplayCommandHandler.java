package com.lulan.shincolle.network;

import com.lulan.shincolle.blockentity.DeskBlockEntity;
import com.lulan.shincolle.combat.WorldCombatRulesSavedData;
import com.lulan.shincolle.config.ShinColleConfig;
import com.lulan.shincolle.entity.mount.LegacyShipMountEntity;
import com.lulan.shincolle.entity.ship.LegacyShipEntity;
import com.lulan.shincolle.formation.FormationRuntimeState;
import com.lulan.shincolle.menu.DeskMenu;
import com.lulan.shincolle.item.OpToolItem;
import com.lulan.shincolle.item.TargetWrenchItem;
import net.minecraft.nbt.NbtUtils;
import com.lulan.shincolle.menu.FormationMenu;
import com.lulan.shincolle.menu.ShipInventoryMenu;
import com.lulan.shincolle.morph.MorphHelper;
import com.lulan.shincolle.morph.MorphProfile;
import com.lulan.shincolle.registry.ModBlocks;
import com.lulan.shincolle.registry.ModItems;
import com.lulan.shincolle.team.TeamSavedData;
import com.lulan.shincolle.teitoku.TeitokuData;
import com.lulan.shincolle.teitoku.TeitokuHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkHooks;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.Function;

public final class GameplayCommandHandler {

    private static final double MAX_SHIP_COMMAND_RANGE_SQR = 256.0D * 256.0D;
    private static final double MAX_TARGET_COMMAND_RANGE_SQR = 256.0D * 256.0D;
    private static final double MAX_MOVE_COMMAND_RANGE_SQR = 320.0D * 320.0D;
    private static final int LEGACY_MORPH_RESOURCE_CAP = 30000;
    private static final int LEGACY_BASE_LIGHT_AMMO = 30;
    private static final int LEGACY_BASE_HEAVY_AMMO = 15;
    private static final int LEGACY_BASE_GRUDGE = 300;
    private static final float LEGACY_EASY_MODE_MULTIPLIER = 10.0F;
    private static final boolean LEGACY_EASY_MODE_DEFAULT = false;

    public static final String TAG_SHIP_ID = "ShipId";
    public static final String TAG_TARGET_ID = "TargetId";
    public static final String TAG_TEAM_ID = "TeamId";
    public static final String TAG_SLOT = "Slot";
    public static final String TAG_SLOT_FROM = "FromSlot";
    public static final String TAG_SLOT_TO = "ToSlot";
    public static final String TAG_SELECTED = "Selected";
    public static final String TAG_MODE = "Mode";
    public static final String TAG_GUARD_TYPE = "GuardType";
    public static final String TAG_X = "X";
    public static final String TAG_Y = "Y";
    public static final String TAG_Z = "Z";
    public static final String TAG_SHIP_UID = "ShipUid";
    public static final String TAG_TARGET_CLASS = "TargetClass";
    public static final String TAG_RELATION_TEAM_ID = "RelationTeamId";
    public static final String TAG_TEAM_NAME = "TeamName";
    public static final String TAG_ATTACK_KIND = "AttackKind";
    public static final String TAG_SKILL_SLOT = "SkillSlot";
    public static final String TAG_FORMATION_ID = "FormationId";
    public static final String TAG_RADAR_ZOOM = "RadarZoom";
    public static final String TAG_BOOK_CHAPTER = "BookChapter";
    public static final String TAG_BOOK_PAGE = "BookPage";
    public static final String TAG_HIT_HEIGHT = "HitHeight";
    public static final String TAG_HIT_ANGLE = "HitAngle";

    // Entity-data packing of legacy !PassiveAI and PickItem; not a network payload.
    public static final int AI_FLAG_AUTO_TARGET = 1;
    public static final int AI_FLAG_PICK_ITEM = 1 << 4;

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
            case MORPH_GUI_BUTTON -> TeitokuHelper.get(player).ifPresent(data -> {
                MorphProfile profile = data.getSelectedMorphProfile();
                if (profile != null) profile.applyGuiButton(payload.getInt("Button"), payload.getInt("Value"));
            });
            case MORPH_ADD_LIGHT_AMMO -> handleMorphSupply(player, GameplayCommandType.MORPH_ADD_LIGHT_AMMO);
            case MORPH_ADD_HEAVY_AMMO -> handleMorphSupply(player, GameplayCommandType.MORPH_ADD_HEAVY_AMMO);
            case MORPH_ADD_GRUDGE -> handleMorphSupply(player, GameplayCommandType.MORPH_ADD_GRUDGE);
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
            case WRENCH_PAIR_TARGETS -> {
                if ((player.getMainHandItem().is(ModItems.TARGETWRENCH.get())
                        || player.getOffhandItem().is(ModItems.TARGETWRENCH.get()))
                        && payload.contains("First", 10) && payload.contains("Second", 10)) {
                    TargetWrenchItem.pairTargets(player.level(), player,
                            NbtUtils.readBlockPos(payload.getCompound("First")),
                            NbtUtils.readBlockPos(payload.getCompound("Second")));
                }
            }
            case SET_FORMATION -> TeitokuHelper.setFormationId(player, resolveFormationId(payload));
            case SET_UNIT_NAME -> TeitokuHelper.setUnitName(player,
                    payload.contains(TAG_TEAM_ID) ? payload.getInt(TAG_TEAM_ID) : -1,
                    sanitizeTeamName(payload.getString(TAG_TEAM_NAME)));
            case SYNC_DESK_FUNC -> handleSyncDeskFunc(player, payload);
            case OPEN_MOUNT_SHIP_INVENTORY -> handleOpenMountShipInventory(player);
            case MOUNT_MOVE -> handleMountMove(player, payload);
            case SET_SHIP_HIT_ZONE -> handleSetShipHitZone(player, payload);
            case REQUEST_DESK_RADAR -> {
                handleRequestDeskRadar(player);
                return;
            }
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
        boolean hasShipUid = payload.contains(TAG_SHIP_UID);
        int shipUid = hasShipUid ? payload.getInt(TAG_SHIP_UID) : 0;
        if (hasShipUid && shipUid <= 0) {
            TeitokuHelper.assignShipToCurrentTeam(player, 0, -1);
            return;
        }

        LegacyShipEntity ship = null;
        if (player.level().getEntity(payload.getInt(TAG_SHIP_ID)) instanceof LegacyShipEntity byId) {
            ship = byId;
        }

        if (ship == null && shipUid > 0) {
            ship = TeitokuHelper.findShipByUid(player, shipUid);
        }

        if (ship != null) {
            shipUid = ship.getShipUid();
        }

        if (ship != null) {
            TeitokuHelper.assignShipToCurrentTeam(player, 0, shipUid);
        } else {
            TeitokuHelper.assignShipToCurrentTeam(player, 0, -1);
        }
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

        int oldMode = com.lulan.shincolle.item.PointerItem.getMode(player.getMainHandItem());
        int mode = Mth.clamp(payload.getInt(TAG_MODE), 0, 5);
        com.lulan.shincolle.item.PointerItem.setMode(player.getMainHandItem(), mode);
        if (Math.abs(mode - oldMode) % 3 != 0
                && com.lulan.shincolle.item.PointerItem.getCommandMode(mode) == 0) {
            TeitokuHelper.focusFirstShipInCurrentTeam(player);
        }
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
                        Component.translatable("gui.shincolle.formation.title")),
                buffer -> buffer.writeNbt(FormationMenu.captureVisualData(player)));
    }

    private static void handleOpenDeskScreen(ServerPlayer player, CompoundTag payload) {
        int selectedFunction = Mth.clamp(payload.getInt(TAG_MODE),
                DeskMenu.RADAR_VARIANT,
                4);
        NetworkHooks.openScreen(player,
                new SimpleMenuProvider(
                        (containerId, inventory, menuPlayer) ->
                                new DeskMenu(containerId, inventory, player.blockPosition(), selectedFunction),
                        Component.translatable("block.shincolle.blockdesk")),
                buffer -> DeskMenu.writeOpeningData(buffer, player.blockPosition(), selectedFunction));
    }

    private static void handleMorphSupply(ServerPlayer player, GameplayCommandType commandType) {
        if (mutateSelectedMorphProfile(player, profile -> switch (commandType) {
            case MORPH_ADD_LIGHT_AMMO -> refillMorphAmmoLight(player, profile);
            case MORPH_ADD_HEAVY_AMMO -> refillMorphAmmoHeavy(player, profile);
            case MORPH_ADD_GRUDGE -> refillMorphGrudge(player, profile);
            default -> false;
        })) {
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

    private static boolean mutateSelectedMorphProfile(ServerPlayer player, Function<MorphProfile, Boolean> updater) {
        return TeitokuHelper.get(player)
                .map(data -> {
                    MorphProfile profile = data.getSelectedMorphProfile();
                    return profile != null && updater.apply(profile);
                })
                .orElse(false);
    }

    private static boolean refillMorphAmmoLight(ServerPlayer player, MorphProfile profile) {
        if (profile.getAmmoLight() >= LEGACY_MORPH_RESOURCE_CAP) {
            return false;
        }

        float baseValue;
        if (consumeInventoryItem(player.getInventory(), ModItems.AMMO.get())) {
            baseValue = LEGACY_BASE_LIGHT_AMMO;
        } else if (consumeInventoryItem(player.getInventory(), ModItems.AMMO1.get())) {
            baseValue = LEGACY_BASE_LIGHT_AMMO * 9.0F;
        } else {
            return false;
        }

        if (isLegacyEasyModeEnabled()) {
            baseValue *= LEGACY_EASY_MODE_MULTIPLIER;
        }

        profile.addAmmoLight((int) (baseValue * (1.0F + profile.buildEquipmentProfile().ammoBonus())));
        return true;
    }

    private static boolean refillMorphAmmoHeavy(ServerPlayer player, MorphProfile profile) {
        if (profile.getAmmoHeavy() >= LEGACY_MORPH_RESOURCE_CAP) {
            return false;
        }

        float baseValue;
        if (consumeInventoryItem(player.getInventory(), ModItems.AMMO2.get())) {
            baseValue = LEGACY_BASE_HEAVY_AMMO;
        } else if (consumeInventoryItem(player.getInventory(), ModItems.AMMO3.get())) {
            baseValue = LEGACY_BASE_HEAVY_AMMO * 9.0F;
        } else {
            return false;
        }

        if (isLegacyEasyModeEnabled()) {
            baseValue *= LEGACY_EASY_MODE_MULTIPLIER;
        }

        profile.addAmmoHeavy((int) (baseValue * (1.0F + profile.buildEquipmentProfile().ammoBonus())));
        return true;
    }

    private static boolean refillMorphGrudge(ServerPlayer player, MorphProfile profile) {
        if (profile.getGrudge() >= LEGACY_MORPH_RESOURCE_CAP) {
            return false;
        }

        float baseValue;
        if (consumeInventoryItem(player.getInventory(), ModItems.GRUDGE.get())) {
            baseValue = LEGACY_BASE_GRUDGE;
        } else if (consumeInventoryItem(player.getInventory(), ModBlocks.BLOCK_GRUDGE.get().asItem())) {
            baseValue = LEGACY_BASE_GRUDGE * 9.0F;
        } else {
            return false;
        }

        if (isLegacyEasyModeEnabled()) {
            baseValue *= LEGACY_EASY_MODE_MULTIPLIER;
        }

        profile.addGrudge((int) (baseValue * (1.0F + profile.buildEquipmentProfile().grudgeBonus())));
        return true;
    }

    private static boolean consumeInventoryItem(Inventory inventory, Item item) {
        if (inventory.player.getAbilities().instabuild) {
            return true;
        }

        for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
            ItemStack stack = inventory.getItem(slot);
            if (!stack.is(item)) {
                continue;
            }

            stack.shrink(1);
            if (stack.isEmpty()) {
                inventory.setItem(slot, ItemStack.EMPTY);
            } else {
                inventory.setChanged();
            }
            return true;
        }

        return false;
    }

    private static boolean isLegacyEasyModeEnabled() {
        return ShinColleConfig.easyMode();
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
        if (!player.hasPermissions(2)) {
            player.displayClientMessage(Component.translatable("chat.shincolle.optool.no_permission"), true);
            return;
        }
        net.minecraft.world.entity.Entity target = player.level().getEntity(payload.getInt(TAG_TARGET_ID));
        if (OpToolItem.isExcludedTarget(target)) {
            return;
        }
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

    private static int resolveFormationId(CompoundTag payload) {
        return payload.contains(TAG_FORMATION_ID)
                ? payload.getInt(TAG_FORMATION_ID)
                : payload.getInt(TAG_MODE);
    }

    private static void handleSyncDeskFunc(ServerPlayer player, CompoundTag payload) {
        BlockPos pos = new BlockPos(payload.getInt(TAG_X), payload.getInt(TAG_Y), payload.getInt(TAG_Z));
        if (!player.level().isLoaded(pos)
                || player.distanceToSqr(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D) > 64.0D) {
            return;
        }
        if (player.level().getBlockEntity(pos) instanceof DeskBlockEntity desk) {
            desk.syncFunc(
                    payload.getInt(TAG_MODE),
                    payload.getInt(TAG_RADAR_ZOOM),
                    payload.getInt(TAG_BOOK_CHAPTER),
                    payload.getInt(TAG_BOOK_PAGE));
        }
    }

    public static @javax.annotation.Nullable LegacyShipEntity resolveMountInventoryShip(ServerPlayer player) {
        if (player.getVehicle() instanceof LegacyShipMountEntity shipMount) {
            return shipMount.getHostShip();
        }
        if (player.getVehicle() instanceof LegacyShipEntity ship) {
            return ship;
        }
        if (player.getFirstPassenger() instanceof LegacyShipEntity passengerShip) {
            return passengerShip;
        }
        return null;
    }

    private static void handleOpenMountShipInventory(ServerPlayer player) {
        ShipCommandService.openShipInventoryForPlayer(player, resolveMountInventoryShip(player));
    }

    private static void handleMountMove(ServerPlayer player, CompoundTag payload) {
        int keyMask = payload.getInt(TAG_MODE) & 31;
        if (player.getVehicle() instanceof LegacyShipMountEntity shipMount) {
            LegacyShipEntity host = shipMount.getHostShip();
            if (host != null && !host.isHostileVariant() && host.isOwnedBy(player)) {
                shipMount.applyLegacyMountMove(keyMask);
            }
        }
    }

    private static void handleSetShipHitZone(ServerPlayer player, CompoundTag payload) {
        if (!(player.level().getEntity(payload.getInt(TAG_SHIP_ID)) instanceof LegacyShipEntity ship)) {
            return;
        }
        if (ship.isHostileVariant() || player.distanceToSqr(ship) > 64.0D) {
            return;
        }
        ship.setLegacyHitZone(payload.getInt(TAG_HIT_HEIGHT), payload.getInt(TAG_HIT_ANGLE));
    }

    private static void handleRequestDeskRadar(ServerPlayer player) {
        ModNetwork.syncGameplayState(player, TeitokuHelper.buildDeskRadarPayloadTag(player));
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
