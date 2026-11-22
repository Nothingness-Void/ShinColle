package com.lulan.shincolle.network;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nullable;
import java.util.function.Supplier;

public record ServerboundShipCommandPacket(ShipCommandAction action,
                                           int mode,
                                           int shipId,
                                           int shipUid,
                                           int targetId,
                                           @Nullable BlockPos pos,
                                           int value) implements CustomPacketPayload {

    public static final Type<ServerboundShipCommandPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath("shincolle", "serverbound_ship_command_packet"));
    public static final StreamCodec<FriendlyByteBuf, ServerboundShipCommandPacket> STREAM_CODEC = StreamCodec.of(ServerboundShipCommandPacket::encodeBuffer, ServerboundShipCommandPacket::decode);

    private static void encodeBuffer(FriendlyByteBuf buffer, ServerboundShipCommandPacket packet) {
        encode(packet, buffer);
    }

    @Override
    public Type<ServerboundShipCommandPacket> type() {
        return TYPE;
    }

    public static final int NO_ENTITY = -1;
    public static final int NO_UID = -1;

    public static ServerboundShipCommandPacket moveTo(int mode, int shipId, int shipUid, BlockPos pos) {
        return new ServerboundShipCommandPacket(ShipCommandAction.MOVE_TO_POS, mode, shipId, shipUid, NO_ENTITY, pos, 0);
    }

    public static ServerboundShipCommandPacket guardBlock(int mode, int shipId, int shipUid, BlockPos pos) {
        return new ServerboundShipCommandPacket(ShipCommandAction.MOVE_TO_POS, mode, shipId, shipUid, NO_ENTITY, pos, 1);
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

    /** Legacy C2SGUIPackets PID.ShipBtn: {@code button} is a {@link ShipGuiButton} id. */
    public static ServerboundShipCommandPacket guiButton(int shipId, int shipUid, int button, int value) {
        return new ServerboundShipCommandPacket(ShipCommandAction.SHIP_GUI_BUTTON, button, shipId, shipUid, NO_ENTITY, null, value);
    }

    public static ServerboundShipCommandPacket setWaypointStay(int shipId, int shipUid, int stayMode) {
        return new ServerboundShipCommandPacket(ShipCommandAction.SET_WAYPOINT_STAY, 0, shipId, shipUid, NO_ENTITY, null, stayMode);
    }

    public static ServerboundShipCommandPacket setTimeKeeper(int shipId, int shipUid, boolean enabled) {
        return new ServerboundShipCommandPacket(ShipCommandAction.SET_TIME_KEEPER, 0, shipId, shipUid, NO_ENTITY, null, enabled ? 1 : 0);
    }

    public static ServerboundShipCommandPacket setAutoPump(int shipId, int shipUid, boolean enabled) {
        return new ServerboundShipCommandPacket(ShipCommandAction.SET_AUTO_PUMP, 0, shipId, shipUid, NO_ENTITY, null, enabled ? 1 : 0);
    }

    public static ServerboundShipCommandPacket setCombatRationLevel(int shipId, int shipUid, int level) {
        return new ServerboundShipCommandPacket(ShipCommandAction.SET_COMBAT_RATION_LEVEL, 0,
                shipId, shipUid, NO_ENTITY, null, level);
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

    public static void handle(ServerboundShipCommandPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            ServerPlayer sender = (ServerPlayer) context.player();
            if (sender != null) {
                ShipCommandService.handle(sender, packet);
            }
        });
    }
}
