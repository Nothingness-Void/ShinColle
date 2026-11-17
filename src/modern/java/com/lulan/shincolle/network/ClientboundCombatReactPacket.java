package com.lulan.shincolle.network;

import com.lulan.shincolle.entity.projectile.LegacyShipProjectileVisual;
import com.lulan.shincolle.client.GameplayClientEffects;
import com.lulan.shincolle.entity.ship.LegacyShipAttackKind;
import com.lulan.shincolle.entity.ship.LegacyShipEntity;
import com.lulan.shincolle.entity.ship.LegacyShipRensouhouEntity;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record ClientboundCombatReactPacket(CombatReactType reactType, int attackerId, int targetId,
                                           LegacyShipAttackKind attackKind, LegacyShipProjectileVisual projectileVisual) {

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

    public static void handle(ClientboundCombatReactPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> GameplayClientEffects.handleCombatReact(packet));
        context.setPacketHandled(true);
    }
}

