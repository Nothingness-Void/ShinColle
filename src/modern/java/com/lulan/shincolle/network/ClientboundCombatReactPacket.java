package com.lulan.shincolle.network;

import com.lulan.shincolle.entity.projectile.LegacyShipProjectileVisual;
import com.lulan.shincolle.client.GameplayClientEffects;
import com.lulan.shincolle.entity.ship.LegacyShipAttackKind;
import com.lulan.shincolle.entity.ship.LegacyShipEntity;
import com.lulan.shincolle.entity.ship.LegacyShipRensouhouEntity;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;

import java.util.function.Supplier;

public record ClientboundCombatReactPacket(CombatReactType reactType, int attackerId, int targetId,
                                           LegacyShipAttackKind attackKind, LegacyShipProjectileVisual projectileVisual) implements CustomPacketPayload {

    public static final Type<ClientboundCombatReactPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath("shincolle", "clientbound_combat_react_packet"));
    public static final StreamCodec<FriendlyByteBuf, ClientboundCombatReactPacket> STREAM_CODEC = StreamCodec.of(ClientboundCombatReactPacket::encodeBuffer, ClientboundCombatReactPacket::decode);

    private static void encodeBuffer(FriendlyByteBuf buffer, ClientboundCombatReactPacket packet) {
        encode(packet, buffer);
    }

    @Override
    public Type<ClientboundCombatReactPacket> type() {
        return TYPE;
    }

    public boolean involvesEntity(int entityId) {
        return entityId == this.attackerId || entityId == this.targetId;
    }

    public boolean allowsPlayerActionbar(Entity player, Entity attacker) {
        return this.involvesEntity(player.getId())
                && attacker != null
                && !(attacker instanceof LegacyShipEntity ship && ship.isHostileVariant())
                && !(attacker instanceof LegacyShipRensouhouEntity summon && summon.isHostileVariant());
    }

    public static void encode(ClientboundCombatReactPacket packet, FriendlyByteBuf buffer) {
        buffer.writeVarInt(packet.reactType.ordinal());
        buffer.writeVarInt(packet.attackerId);
        buffer.writeVarInt(packet.targetId);
        buffer.writeVarInt(packet.attackKind.ordinal());
        buffer.writeVarInt(packet.projectileVisual.ordinal());
    }

    public static ClientboundCombatReactPacket decode(FriendlyByteBuf buffer) {
        CombatReactType reactType = CombatReactType.fromOrdinal(buffer.readVarInt());
        int attackerId = buffer.readVarInt();
        int targetId = buffer.readVarInt();
        LegacyShipAttackKind attackKind = LegacyShipAttackKind.byOrdinal(buffer.readVarInt());
        LegacyShipProjectileVisual projectileVisual = LegacyShipProjectileVisual.byOrdinal(buffer.readVarInt());
        return new ClientboundCombatReactPacket(reactType, attackerId, targetId, attackKind, projectileVisual);
    }

    public static void handle(ClientboundCombatReactPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> GameplayClientEffects.handleCombatReact(packet));
    }
}

