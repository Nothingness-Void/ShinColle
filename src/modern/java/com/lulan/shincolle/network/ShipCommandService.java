package com.lulan.shincolle.network;

import com.lulan.shincolle.entity.ship.LegacyShipEntity;
import com.lulan.shincolle.formation.LegacyFormationHelper;
import com.lulan.shincolle.formation.FormationRuntimeState;
import com.lulan.shincolle.menu.ShipInventoryMenu;
import com.lulan.shincolle.teitoku.TeitokuData;
import com.lulan.shincolle.teitoku.TeitokuHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.network.NetworkHooks;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public final class ShipCommandService {

    private static final double MAX_SHIP_COMMAND_RANGE_SQR = 256.0D * 256.0D;
    private static final double MAX_TARGET_COMMAND_RANGE_SQR = 256.0D * 256.0D;
    private static final double MAX_MOVE_COMMAND_RANGE_SQR = 320.0D * 320.0D;

    private ShipCommandService() {
    }

    public enum ShipCommandResult {
        SUCCESS(null),
        NO_COMMANDABLE_SHIP("chat.shincolle.ship_command.reason.ship_unavailable"),
        NO_COMMANDABLE_SHIPS("chat.shincolle.ship_command.reason.no_commandable_ships"),
        MOVE_TARGET_TOO_FAR("chat.shincolle.ship_command.reason.move_target_too_far"),
        TARGET_INVALID("chat.shincolle.ship_command.reason.target_invalid"),
        TARGET_TOO_FAR("chat.shincolle.ship_command.reason.target_too_far"),
        TARGET_NOT_ENGAGEABLE("chat.shincolle.ship_command.reason.target_not_engageable");

        private final @Nullable String reasonKey;

        ShipCommandResult(@Nullable String reasonKey) {
            this.reasonKey = reasonKey;
        }

        public boolean successful() {
            return this == SUCCESS;
        }

        public @Nullable String reasonKey() {
            return this.reasonKey;
        }
    }

    public static boolean handle(ServerPlayer player, ServerboundShipCommandPacket packet) {
        ShipCommandResult result = handleInternal(player, packet);
        if (!result.successful()) {
            sendFailureFeedback(player, packet.action(), result);
        }
        TeitokuHelper.syncGameplayState(player);
        return result.successful();
    }

    public static boolean handleForTesting(Player player, ServerboundShipCommandPacket packet) {
        return handleResultForTesting(player, packet).successful();
    }

    public static ShipCommandResult handleResultForTesting(Player player, ServerboundShipCommandPacket packet) {
        return handleInternal(player, packet);
    }

    private static ShipCommandResult handleInternal(Player player, ServerboundShipCommandPacket packet) {
        return switch (packet.action()) {
            case MOVE_TO_POS -> moveToPos(player, packet.mode(), packet.shipId(), packet.shipUid(), packet.pos());
            case GUARD_ENTITY -> guardEntity(player, packet.mode(), packet.shipId(), packet.shipUid(), packet.targetId());
            case ATTACK_ENTITY -> attackEntity(player, packet.mode(), packet.shipId(), packet.shipUid(), packet.targetId());
            case STOP_COMMAND -> stop(player, packet.mode(), packet.shipId(), packet.shipUid());
            case SET_AI_FLAGS -> setAiFlags(player, packet.shipId(), packet.shipUid(), packet.value());
            case SET_FOLLOW_RANGE -> setFollowRange(player, packet.shipId(), packet.shipUid(), packet.value());
            case TOGGLE_SIT -> toggleSit(player, packet.mode(), packet.shipId(), packet.shipUid());
            case OPEN_SHIP_INVENTORY -> openShipInventory(player, packet.shipId(), packet.shipUid());
            case TOGGLE_RING_EFFECT -> toggleRingEffect(player, packet.shipId(), packet.shipUid());
        };
    }

    public static boolean handleLegacy(Player player, GameplayCommandType type, CompoundTag payload) {
        ShipCommandResult result = switch (type) {
            case TOGGLE_SIT_SINGLE -> toggleSit(player, 0, payload.getInt(GameplayCommandHandler.TAG_SHIP_ID), getShipUid(payload));
            case TOGGLE_SIT_GROUP -> toggleSit(player, 2, payload.getInt(GameplayCommandHandler.TAG_SHIP_ID), getShipUid(payload));
            case MOVE_TO_POS -> moveToPos(player,
                    Mth.clamp(payload.getInt(GameplayCommandHandler.TAG_MODE), 0, 2),
                    payload.getInt(GameplayCommandHandler.TAG_SHIP_ID),
                    getShipUid(payload),
                    new BlockPos(payload.getInt(GameplayCommandHandler.TAG_X),
                            payload.getInt(GameplayCommandHandler.TAG_Y),
                            payload.getInt(GameplayCommandHandler.TAG_Z)));
            case GUARD_ENTITY -> guardEntity(player,
                    Mth.clamp(payload.getInt(GameplayCommandHandler.TAG_MODE), 0, 2),
                    payload.getInt(GameplayCommandHandler.TAG_SHIP_ID),
                    getShipUid(payload),
                    payload.getInt(GameplayCommandHandler.TAG_TARGET_ID));
            case ATTACK_ENTITY -> attackEntity(player,
                    Mth.clamp(payload.getInt(GameplayCommandHandler.TAG_MODE), 0, 2),
                    payload.getInt(GameplayCommandHandler.TAG_SHIP_ID),
                    getShipUid(payload),
                    payload.getInt(GameplayCommandHandler.TAG_TARGET_ID));
            case STOP_COMMAND -> stop(player,
                    Mth.clamp(payload.getInt(GameplayCommandHandler.TAG_MODE), 0, 2),
                    payload.getInt(GameplayCommandHandler.TAG_SHIP_ID),
                    getShipUid(payload));
            case OPEN_SHIP_INVENTORY -> openShipInventory(player,
                    payload.getInt(GameplayCommandHandler.TAG_SHIP_ID),
                    getShipUid(payload));
            case SET_SHIP_AI_FLAGS -> setAiFlags(player,
                    payload.getInt(GameplayCommandHandler.TAG_SHIP_ID),
                    getShipUid(payload),
                    payload.getInt(GameplayCommandHandler.TAG_AI_FLAGS));
            case SET_SHIP_FOLLOW_RANGE -> setFollowRange(player,
                    payload.getInt(GameplayCommandHandler.TAG_SHIP_ID),
                    getShipUid(payload),
                    payload.getInt(GameplayCommandHandler.TAG_FOLLOW_RANGE));
            default -> null;
        };
        return result != null && result.successful();
    }

    private static int getShipUid(CompoundTag payload) {
        return payload.contains(GameplayCommandHandler.TAG_SHIP_UID)
                ? payload.getInt(GameplayCommandHandler.TAG_SHIP_UID)
                : ServerboundShipCommandPacket.NO_UID;
    }

    private static ShipCommandResult toggleSit(Player player, int rawMode, int shipId, int shipUid) {
        int mode = Mth.clamp(rawMode, 0, 2);
        LegacyShipEntity anchor = resolveOwnedShip(player, shipId, shipUid);
        if (mode == 0) {
            if (anchor == null) {
                return ShipCommandResult.NO_COMMANDABLE_SHIP;
            }
            anchor.setOrderedToSit(!anchor.isOrderedToSit());
            markShipDirty(anchor);
            return ShipCommandResult.SUCCESS;
        }

        List<LegacyShipEntity> ships = resolveCommandShips(player, mode, anchor);
        if (ships.isEmpty()) {
            return ShipCommandResult.NO_COMMANDABLE_SHIPS;
        }

        boolean nextSit = anchor != null ? !anchor.isOrderedToSit() : !ships.get(0).isOrderedToSit();
        for (LegacyShipEntity ship : ships) {
            ship.setOrderedToSit(nextSit);
            markShipDirty(ship);
        }
        return ShipCommandResult.SUCCESS;
    }

    private static ShipCommandResult moveToPos(Player player, int rawMode, int shipId, int shipUid, @Nullable BlockPos pos) {
        if (pos == null || player.distanceToSqr(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D) > MAX_MOVE_COMMAND_RANGE_SQR) {
            return ShipCommandResult.MOVE_TARGET_TOO_FAR;
        }

        int mode = Mth.clamp(rawMode, 0, 2);
        LegacyShipEntity anchor = resolveOwnedShip(player, shipId, shipUid);
        List<LegacyShipEntity> ships = resolveCommandShips(player, mode, anchor);
        if (ships.isEmpty()) {
            return mode == 0 ? ShipCommandResult.NO_COMMANDABLE_SHIP : ShipCommandResult.NO_COMMANDABLE_SHIPS;
        }
        if (!canExecuteLegacyMoveOrGuard(player, mode, ships)) {
            return ShipCommandResult.SUCCESS;
        }

        LegacyShipEntity flagship = mode == 2 ? LegacyFormationHelper.resolveFlagship(ships) : null;
        for (LegacyShipEntity ship : ships) {
            FormationRuntimeState runtimeState = LegacyFormationHelper.resolveMoveState(player, ship, pos, flagship);
            ship.commandMoveTo(runtimeState.targetPos(), player.level().dimension().location().toString());
            ship.setOrderedToSit(false);
            markShipDirty(ship);
        }
        return ShipCommandResult.SUCCESS;
    }

    private static ShipCommandResult guardEntity(Player player, int rawMode, int shipId, int shipUid, int targetId) {
        if (!(player.level().getEntity(targetId) instanceof LivingEntity target) || !target.isAlive()) {
            return ShipCommandResult.TARGET_INVALID;
        }
        if (player.distanceToSqr(target) > MAX_TARGET_COMMAND_RANGE_SQR) {
            return ShipCommandResult.TARGET_TOO_FAR;
        }

        int mode = Mth.clamp(rawMode, 0, 2);
        LegacyShipEntity anchor = resolveOwnedShip(player, shipId, shipUid);
        List<LegacyShipEntity> ships = resolveCommandShips(player, mode, anchor);
        if (ships.isEmpty()) {
            return mode == 0 ? ShipCommandResult.NO_COMMANDABLE_SHIP : ShipCommandResult.NO_COMMANDABLE_SHIPS;
        }
        if (!canExecuteLegacyMoveOrGuard(player, mode, ships)) {
            return ShipCommandResult.SUCCESS;
        }

        for (LegacyShipEntity ship : ships) {
            ship.commandGuardEntity(target.getUUID());
            ship.setOrderedToSit(false);
            markShipDirty(ship);
        }
        return ShipCommandResult.SUCCESS;
    }

    private static ShipCommandResult attackEntity(Player player, int rawMode, int shipId, int shipUid, int targetId) {
        if (!(player.level().getEntity(targetId) instanceof LivingEntity target) || !target.isAlive()) {
            return ShipCommandResult.TARGET_INVALID;
        }
        if (player.distanceToSqr(target) > MAX_TARGET_COMMAND_RANGE_SQR) {
            return ShipCommandResult.TARGET_TOO_FAR;
        }

        int mode = Mth.clamp(rawMode, 0, 2);
        LegacyShipEntity anchor = resolveOwnedShip(player, shipId, shipUid);
        List<LegacyShipEntity> ships = resolveCommandShips(player, mode, anchor);
        if (ships.isEmpty()) {
            return mode == 0 ? ShipCommandResult.NO_COMMANDABLE_SHIP : ShipCommandResult.NO_COMMANDABLE_SHIPS;
        }

        boolean changed = false;
        for (LegacyShipEntity ship : ships) {
            if (ship.canEngage(target)) {
                ship.setTarget(target);
                ship.clearCommandState();
                ship.setOrderedToSit(false);
                markShipDirty(ship);
                changed = true;
            }
        }
        return changed ? ShipCommandResult.SUCCESS : ShipCommandResult.TARGET_NOT_ENGAGEABLE;
    }

    private static ShipCommandResult stop(Player player, int rawMode, int shipId, int shipUid) {
        int mode = Mth.clamp(rawMode, 0, 2);
        LegacyShipEntity anchor = resolveOwnedShip(player, shipId, shipUid);
        List<LegacyShipEntity> ships = resolveCommandShips(player, mode, anchor);
        if (ships.isEmpty()) {
            return mode == 0 ? ShipCommandResult.NO_COMMANDABLE_SHIP : ShipCommandResult.NO_COMMANDABLE_SHIPS;
        }

        for (LegacyShipEntity ship : ships) {
            ship.setTarget(null);
            ship.clearCommandState();
            ship.getNavigation().stop();
            markShipDirty(ship);
        }
        return ShipCommandResult.SUCCESS;
    }

    private static ShipCommandResult setAiFlags(Player player, int shipId, int shipUid, int flags) {
        LegacyShipEntity ship = resolveOwnedShip(player, shipId, shipUid);
        if (ship == null) {
            return ShipCommandResult.NO_COMMANDABLE_SHIP;
        }

        ship.setAiAutoTarget((flags & GameplayCommandHandler.AI_FLAG_AUTO_TARGET) != 0);
        ship.setAiAllowPvp((flags & GameplayCommandHandler.AI_FLAG_ALLOW_PVP) != 0);
        ship.setAiAutoSupply((flags & GameplayCommandHandler.AI_FLAG_AUTO_SUPPLY) != 0);
        ship.setAiRespectRouteStay((flags & GameplayCommandHandler.AI_FLAG_ROUTE_STAY) != 0);
        markShipDirty(ship);
        return ShipCommandResult.SUCCESS;
    }

    private static ShipCommandResult setFollowRange(Player player, int shipId, int shipUid, int followRange) {
        LegacyShipEntity ship = resolveOwnedShip(player, shipId, shipUid);
        if (ship == null) {
            return ShipCommandResult.NO_COMMANDABLE_SHIP;
        }

        ship.setAiFollowRange(followRange);
        markShipDirty(ship);
        return ShipCommandResult.SUCCESS;
    }

    private static ShipCommandResult openShipInventory(Player player, int shipId, int shipUid) {
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return ShipCommandResult.NO_COMMANDABLE_SHIP;
        }
        LegacyShipEntity ship = resolveOwnedShip(player, shipId, shipUid);
        if (ship == null || !ship.canCommanderEdit(player)) {
            return ShipCommandResult.NO_COMMANDABLE_SHIP;
        }

        LegacyShipEntity target = ship;
        NetworkHooks.openScreen(serverPlayer,
                new SimpleMenuProvider(
                        (containerId, inventory, menuPlayer) -> new ShipInventoryMenu(containerId, inventory, target.getId(), target.getShipUid()),
                        Component.translatable("gui.shincolle.ship_inventory.title", target.getName())),
                buffer -> {
                    buffer.writeVarInt(target.getId());
                    buffer.writeVarInt(target.getShipUid());
                });
        return ShipCommandResult.SUCCESS;
    }

    private static ShipCommandResult toggleRingEffect(Player player, int shipId, int shipUid) {
        LegacyShipEntity ship = resolveOwnedShip(player, shipId, shipUid);
        if (ship == null) {
            return ShipCommandResult.NO_COMMANDABLE_SHIP;
        }

        ship.setRingEffectEnabled(!ship.isRingEffectEnabled());
        markShipDirty(ship);
        return ShipCommandResult.SUCCESS;
    }

    private static @Nullable LegacyShipEntity resolveOwnedShip(Player player, int entityId, int shipUid) {
        LegacyShipEntity ship = null;
        if (entityId >= 0 && player.level().getEntity(entityId) instanceof LegacyShipEntity byEntityId) {
            ship = byEntityId;
        }

        if (ship == null && shipUid > 0) {
            ship = TeitokuHelper.findOwnedShipByUid(player, shipUid);
        }

        return isCommandableFrom(player, ship) ? ship : null;
    }

    private static List<LegacyShipEntity> resolveCommandShips(Player player, int mode, @Nullable LegacyShipEntity anchor) {
        List<LegacyShipEntity> ships = new ArrayList<>();

        if (mode == 0 && anchor != null) {
            ships.add(anchor);
            return ships;
        }

        TeitokuHelper.collectCurrentTeamShips(player, mode == 1, ships);
        ships.removeIf(ship -> !isCommandableFrom(player, ship));
        if (ships.isEmpty() && anchor != null) {
            ships.add(anchor);
        }
        return ships;
    }

    private static boolean isCommandableFrom(Player commander, @Nullable LegacyShipEntity ship) {
        if (ship == null || ship.level() != commander.level() || !ship.canCommanderEdit(commander)) {
            return false;
        }

        return commander.distanceToSqr(ship) <= MAX_SHIP_COMMAND_RANGE_SQR;
    }

    private static void markShipDirty(@Nullable LegacyShipEntity ship) {
        if (ship == null) {
            return;
        }

        TeitokuHelper.refreshShipCache(ship, false);
    }

    private static boolean canExecuteLegacyMoveOrGuard(Player player, int mode, List<LegacyShipEntity> ships) {
        int formationId = TeitokuHelper.get(player)
                .map(data -> data.getFormationId(data.getCurrentTeamId()))
                .orElse(TeitokuData.DEFAULT_FORMATION_ID);
        if (mode < 2) {
            return formationId <= TeitokuData.DEFAULT_FORMATION_ID;
        }

        return formationId <= TeitokuData.DEFAULT_FORMATION_ID || ships.size() > 4;
    }

    private static void sendFailureFeedback(ServerPlayer player, ShipCommandAction action, ShipCommandResult result) {
        if (result.reasonKey() == null) {
            return;
        }

        player.displayClientMessage(Component.translatable(
                "chat.shincolle.ship_command.failed",
                Component.translatable(actionTranslationKey(action)),
                Component.translatable(result.reasonKey())), true);
    }

    private static String actionTranslationKey(ShipCommandAction action) {
        return switch (action) {
            case MOVE_TO_POS -> "chat.shincolle.ship_command.action.move";
            case GUARD_ENTITY -> "chat.shincolle.ship_command.action.guard";
            case ATTACK_ENTITY -> "chat.shincolle.ship_command.action.attack";
            case STOP_COMMAND -> "chat.shincolle.ship_command.action.stop";
            case SET_AI_FLAGS -> "chat.shincolle.ship_command.action.ai_flags";
            case SET_FOLLOW_RANGE -> "chat.shincolle.ship_command.action.follow_range";
            case TOGGLE_SIT -> "chat.shincolle.ship_command.action.toggle_sit";
            case OPEN_SHIP_INVENTORY -> "chat.shincolle.ship_command.action.open_inventory";
            case TOGGLE_RING_EFFECT -> "chat.shincolle.ship_command.action.toggle_ring_effect";
        };
    }

}
