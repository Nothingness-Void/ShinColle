package com.lulan.shincolle.network;

import com.lulan.shincolle.client.GameplayClientEffects;
import com.lulan.shincolle.entity.ship.LegacyShipAttackKind;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record ClientboundCombatReactPacket(CombatReactType reactType, int attackerId, int targetId, LegacyShipAttackKind attackKind) {

    public static void encode(ClientboundCombatReactPacket packet, FriendlyByteBuf buffer) {
        buffer.writeVarInt(packet.reactType.ordinal());
        buffer.writeVarInt(packet.attackerId);
        buffer.writeVarInt(packet.targetId);
        buffer.writeVarInt(packet.attackKind.ordinal());
    }

    public static ClientboundCombatReactPacket decode(FriendlyByteBuf buffer) {
        CombatReactType reactType = CombatReactType.fromOrdinal(buffer.readVarInt());
        int attackerId = buffer.readVarInt();
        int targetId = buffer.readVarInt();
        LegacyShipAttackKind attackKind = LegacyShipAttackKind.byOrdinal(buffer.readVarInt());
        return new ClientboundCombatReactPacket(reactType, attackerId, targetId, attackKind);
    }

    public static void handle(ClientboundCombatReactPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> GameplayClientEffects.handleCombatReact(packet));
        context.setPacketHandled(true);
    }
}

