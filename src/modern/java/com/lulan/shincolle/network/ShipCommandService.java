package com.lulan.shincolle.network;

import com.lulan.shincolle.entity.ship.LegacyShipEntity;
import com.lulan.shincolle.formation.FormationRuntimeState;
import com.lulan.shincolle.menu.ShipInventoryMenu;
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

    public static boolean handle(ServerPlayer player, ServerboundShipCommandPacket packet) {
        boolean changed = handleInternal(player, packet);
        TeitokuHelper.syncGameplayState(player);
        return changed;
    }

    public static boolean handleForTesting(Player player, ServerboundShipCommandPacket packet) {
        return handleInternal(player, packet);
    }

    private static boolean handleInternal(Player player, ServerboundShipCommandPacket packet) {
        boolean changed = switch (packet.action()) {
            case MOVE_TO_POS -> moveToPos(player, packet.mode(), packet.shipId(), packet.shipUid(), packet.pos());
            case GUARD_ENTITY -> guardEntity(player, packet.mode(), packet.shipId(), packet.shipUid(), packet.targetId());
            case ATTACK_ENTITY -> attackEntity(player, packet.mode(), packet.shipId(), packet.shipUid(), packet.targetId());
            case STOP_COMMAND -> stop(player, packet.mode(), packet.shipId(), packet.shipUid());
            case SET_AI_FLAGS -> setAiFlags(player, packet.shipId(), packet.shipUid(), packet.value());
            case SET_FOLLOW_RANGE -> setFollowRange(player, packet.shipId(), packet.shipUid(), packet.value());
            case TOGGLE_SIT -> toggleSit(player, packet.mode(), packet.shipId(), packet.shipUid());
            case OPEN_SHIP_INVENTORY -> openShipInventory(player, packet.shipId(), packet.shipUid());
        };
        return changed;
    }

    public static boolean handleLegacy(Player player, GameplayCommandType type, CompoundTag payload) {
        return switch (type) {
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
            default -> false;
        };
    }

    private static int getShipUid(CompoundTag payload) {
        return payload.contains(GameplayCommandHandler.TAG_SHIP_UID)
                ? payload.getInt(GameplayCommandHandler.TAG_SHIP_UID)
                : ServerboundShipCommandPacket.NO_UID;
    }

    private static boolean toggleSit(Player player, int rawMode, int shipId, int shipUid) {
        int mode = Mth.clamp(rawMode, 0, 2);
        LegacyShipEntity anchor = resolveOwnedShip(player, shipId, shipUid);
        if (mode == 0) {
            if (anchor == null) {
                return false;
            }
            anchor.setOrderedToSit(!anchor.isOrderedToSit());
            return true;
        }

        List<LegacyShipEntity> ships = resolveCommandShips(player, mode, anchor);
        if (ships.isEmpty()) {
            return false;
        }

        boolean nextSit = anchor != null ? !anchor.isOrderedToSit() : !ships.get(0).isOrderedToSit();
        for (LegacyShipEntity ship : ships) {
            ship.setOrderedToSit(nextSit);
        }
        return true;
    }

    private static boolean moveToPos(Player player, int rawMode, int shipId, int shipUid, @Nullable BlockPos pos) {
        if (pos == null || player.distanceToSqr(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D) > MAX_MOVE_COMMAND_RANGE_SQR) {
            return false;
        }

        int mode = Mth.clamp(rawMode, 0, 2);
        LegacyShipEntity anchor = resolveOwnedShip(player, shipId, shipUid);
        boolean changed = false;
        for (LegacyShipEntity ship : resolveCommandShips(player, mode, anchor)) {
            FormationRuntimeState runtimeState = resolveFormationMoveState(player, ship, pos);
            ship.commandMoveTo(runtimeState.targetPos(), player.level().dimension().location().toString());
            ship.setOrderedToSit(false);
            changed = true;
        }
        return changed;
    }

    private static boolean guardEntity(Player player, int rawMode, int shipId, int shipUid, int targetId) {
        if (!(player.level().getEntity(targetId) instanceof LivingEntity target) || !target.isAlive()) {
            return false;
        }
        if (player.distanceToSqr(target) > MAX_TARGET_COMMAND_RANGE_SQR) {
            return false;
        }

        int mode = Mth.clamp(rawMode, 0, 2);
        LegacyShipEntity anchor = resolveOwnedShip(player, shipId, shipUid);
        boolean changed = false;
        for (LegacyShipEntity ship : resolveCommandShips(player, mode, anchor)) {
            ship.commandGuardEntity(target.getUUID());
            ship.setOrderedToSit(false);
            changed = true;
        }
        return changed;
    }

    private static boolean attackEntity(Player player, int rawMode, int shipId, int shipUid, int targetId) {
        if (!(player.level().getEntity(targetId) instanceof LivingEntity target) || !target.isAlive()) {
            return false;
        }
        if (player.distanceToSqr(target) > MAX_TARGET_COMMAND_RANGE_SQR) {
            return false;
        }

        int mode = Mth.clamp(rawMode, 0, 2);
        LegacyShipEntity anchor = resolveOwnedShip(player, shipId, shipUid);
        boolean changed = false;
        for (LegacyShipEntity ship : resolveCommandShips(player, mode, anchor)) {
            if (ship.canEngage(target)) {
                ship.setTarget(target);
                ship.clearCommandState();
                ship.setOrderedToSit(false);
                changed = true;
            }
        }
        return changed;
    }

    private static boolean stop(Player player, int rawMode, int shipId, int shipUid) {
        int mode = Mth.clamp(rawMode, 0, 2);
        LegacyShipEntity anchor = resolveOwnedShip(player, shipId, shipUid);
        boolean changed = false;
        for (LegacyShipEntity ship : resolveCommandShips(player, mode, anchor)) {
            ship.setTarget(null);
            ship.clearCommandState();
            ship.getNavigation().stop();
            changed = true;
        }
        return changed;
    }

    private static boolean setAiFlags(Player player, int shipId, int shipUid, int flags) {
        LegacyShipEntity ship = resolveOwnedShip(player, shipId, shipUid);
        if (ship == null) {
            return false;
        }

        ship.setAiAutoTarget((flags & GameplayCommandHandler.AI_FLAG_AUTO_TARGET) != 0);
        ship.setAiAllowPvp((flags & GameplayCommandHandler.AI_FLAG_ALLOW_PVP) != 0);
        ship.setAiAutoSupply((flags & GameplayCommandHandler.AI_FLAG_AUTO_SUPPLY) != 0);
        ship.setAiRespectRouteStay((flags & GameplayCommandHandler.AI_FLAG_ROUTE_STAY) != 0);
        return true;
    }

    private static boolean setFollowRange(Player player, int shipId, int shipUid, int followRange) {
        LegacyShipEntity ship = resolveOwnedShip(player, shipId, shipUid);
        if (ship == null) {
            return false;
        }

        ship.setAiFollowRange(followRange);
        return true;
    }

    private static boolean openShipInventory(Player player, int shipId, int shipUid) {
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return false;
        }
        LegacyShipEntity ship = resolveOwnedShip(player, shipId, shipUid);
        if (ship == null || !ship.canCommanderEdit(player)) {
            return false;
        }

        LegacyShipEntity target = ship;
        NetworkHooks.openScreen(serverPlayer,
                new SimpleMenuProvider(
                        (containerId, inventory, menuPlayer) -> new ShipInventoryMenu(containerId, inventory, target.getId()),
                        Component.translatable("gui.shincolle.ship_inventory.title", target.getName())),
                buffer -> buffer.writeVarInt(target.getId()));
        return true;
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

    private static FormationRuntimeState resolveFormationMoveState(Player player, LegacyShipEntity ship, BlockPos center) {
        if (ship.getShipUid() <= 0) {
            return new FormationRuntimeState(TeitokuHelper.getCurrentTeamId(player),
                    TeitokuData.DEFAULT_FORMATION_ID,
                    0,
                    center.immutable());
        }

        return TeitokuHelper.get(player)
                .map(teitokuData -> {
                    int teamId = teitokuData.findTeamIdByShipUid(ship.getShipUid());
                    int slot = teitokuData.findSlotIndexByShipUid(ship.getShipUid());
                    if (teamId < 0 || slot < 0 || teitokuData.countShipsInTeam(teamId) <= 4) {
                        return new FormationRuntimeState(TeitokuHelper.getCurrentTeamId(player),
                                TeitokuData.DEFAULT_FORMATION_ID,
                                slot < 0 ? 0 : slot,
                                center.immutable());
                    }

                    int formationId = teitokuData.getFormationId(teamId);
                    BlockPos formationPos = resolveFormationOffset(center, formationId, slot, player.getDirection());
                    return new FormationRuntimeState(teamId, formationId, slot, formationPos);
                })
                .orElse(new FormationRuntimeState(TeitokuHelper.getCurrentTeamId(player),
                        TeitokuData.DEFAULT_FORMATION_ID,
                        0,
                        center.immutable()));
    }

    private static BlockPos resolveFormationOffset(BlockPos center, int formationId, int slot, Direction forwardDirection) {
        int normalizedSlot = Mth.clamp(slot, 0, TeitokuData.TEAM_SIZE - 1);
        int[][] localOffsets = switch (Mth.clamp(formationId, TeitokuData.DEFAULT_FORMATION_ID, TeitokuData.MAX_FORMATION_ID)) {
            case 1 -> new int[][]{
                    { -4, 0 }, { -2, 0 }, { 0, 0 }, { 2, 0 }, { 4, 0 }, { 6, 0 }
            };
            case 2 -> new int[][]{
                    { -2, -1 }, { -2, 1 }, { 0, -1 }, { 0, 1 }, { 2, -1 }, { 2, 1 }
            };
            case 3 -> new int[][]{
                    { -3, 0 }, { -1, -2 }, { -1, 2 }, { 1, -1 }, { 1, 1 }, { 3, 0 }
            };
            case 4 -> new int[][]{
                    { -3, -3 }, { -2, -2 }, { -1, -1 }, { 0, 0 }, { 1, 1 }, { 2, 2 }
            };
            case 5 -> new int[][]{
                    { 0, -5 }, { 0, -3 }, { 0, -1 }, { 0, 1 }, { 0, 3 }, { 0, 5 }
            };
            default -> new int[][]{
                    { 0, 0 }, { 0, 0 }, { 0, 0 }, { 0, 0 }, { 0, 0 }, { 0, 0 }
            };
        };

        Direction forward = forwardDirection.getAxis().isVertical() ? Direction.NORTH : forwardDirection;
        Direction right = forward.getClockWise();
        int forwardOffset = localOffsets[normalizedSlot][0];
        int rightOffset = localOffsets[normalizedSlot][1];

        int x = center.getX() + forward.getStepX() * forwardOffset + right.getStepX() * rightOffset;
        int z = center.getZ() + forward.getStepZ() * forwardOffset + right.getStepZ() * rightOffset;
        return new BlockPos(x, center.getY(), z);
    }
}
