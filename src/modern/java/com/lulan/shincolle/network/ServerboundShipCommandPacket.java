package com.lulan.shincolle.network;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import javax.annotation.Nullable;
import java.util.function.Supplier;

public record ServerboundShipCommandPacket(ShipCommandAction action,
                                           int mode,
                                           int shipId,
                                           int shipUid,
                                           int targetId,
                                           @Nullable BlockPos pos,
                                           int value) {

    public static final int NO_ENTITY = -1;
    public static final int NO_UID = -1;

    public static ServerboundShipCommandPacket moveTo(int mode, int shipId, int shipUid, BlockPos pos) {
        return new ServerboundShipCommandPacket(ShipCommandAction.MOVE_TO_POS, mode, shipId, shipUid, NO_ENTITY, pos, 0);
    }

    public static ServerboundShipCommandPacket guard(int mode, int shipId, int shipUid, int targetId) {
        return new ServerboundShipCommandPacket(ShipCommandAction.GUARD_ENTITY, mode, shipId, shipUid, targetId, null, 0);
    }

    public static ServerboundShipCommandPacket attack(int mode, int shipId, int shipUid, int targetId) {
        return new ServerboundShipCommandPacket(ShipCommandAction.ATTACK_ENTITY, mode, shipId, shipUid, targetId, null, 0);
    }

    public static ServerboundShipCommandPacket stop(int mode, int shipId, int shipUid) {
        return new ServerboundShipCommandPacket(ShipCommandAction.STOP_COMMAND, mode, shipId, shipUid, NO_ENTITY, null, 0);
    }

    public static ServerboundShipCommandPacket toggleSit(int mode, int shipId, int shipUid) {
        return new ServerboundShipCommandPacket(ShipCommandAction.TOGGLE_SIT, mode, shipId, shipUid, NO_ENTITY, null, 0);
    }

    public static ServerboundShipCommandPacket openInventory(int shipId, int shipUid) {
        return new ServerboundShipCommandPacket(ShipCommandAction.OPEN_SHIP_INVENTORY, 0, shipId, shipUid, NO_ENTITY, null, 0);
    }

    public static ServerboundShipCommandPacket toggleRingEffect(int shipId, int shipUid) {
        return new ServerboundShipCommandPacket(ShipCommandAction.TOGGLE_RING_EFFECT, 0, shipId, shipUid, NO_ENTITY, null, 0);
    }

    public static ServerboundShipCommandPacket setAiFlags(int shipId, int shipUid, int flags) {
        return new ServerboundShipCommandPacket(ShipCommandAction.SET_AI_FLAGS, 0, shipId, shipUid, NO_ENTITY, null, flags);
    }

    public static ServerboundShipCommandPacket setFollowRange(int shipId, int shipUid, int followRange) {
        return new ServerboundShipCommandPacket(ShipCommandAction.SET_FOLLOW_RANGE, 0, shipId, shipUid, NO_ENTITY, null, followRange);
    }

    public static void encode(ServerboundShipCommandPacket packet, FriendlyByteBuf buffer) {
        buffer.writeVarInt(packet.action.ordinal());
        buffer.writeVarInt(packet.mode);
        buffer.writeVarInt(packet.shipId);
        buffer.writeVarInt(packet.shipUid);
        buffer.writeVarInt(packet.targetId);
        buffer.writeBoolean(packet.pos != null);
        if (packet.pos != null) {
            buffer.writeBlockPos(packet.pos);
        }
        buffer.writeVarInt(packet.value);
    }

    public static ServerboundShipCommandPacket decode(FriendlyByteBuf buffer) {
        ShipCommandAction action = ShipCommandAction.fromOrdinal(buffer.readVarInt());
        int mode = buffer.readVarInt();
        int shipId = buffer.readVarInt();
        int shipUid = buffer.readVarInt();
        int targetId = buffer.readVarInt();
        BlockPos pos = buffer.readBoolean() ? buffer.readBlockPos() : null;
        int value = buffer.readVarInt();
        return new ServerboundShipCommandPacket(action, mode, shipId, shipUid, targetId, pos, value);
    }

    public static void handle(ServerboundShipCommandPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ServerPlayer sender = context.getSender();
            if (sender != null) {
                ShipCommandService.handle(sender, packet);
            }
        });
        context.setPacketHandled(true);
    }
}
