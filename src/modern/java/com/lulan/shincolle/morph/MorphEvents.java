package com.lulan.shincolle.morph;

import com.lulan.shincolle.ShinColle;
import com.lulan.shincolle.entity.ship.LegacyShipEntity;
import com.lulan.shincolle.teitoku.TeitokuData;
import com.lulan.shincolle.teitoku.TeitokuHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.common.EventBusSubscriber;

@EventBusSubscriber(modid = ShinColle.MOD_ID, bus = EventBusSubscriber.Bus.GAME)
public final class MorphEvents {

    private MorphEvents() {
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (event.getEntity().level().isClientSide()) {
            return;
        }
        if (!(event.getEntity() instanceof ServerPlayer serverPlayer)) {
            return;
        }

        if (MorphHelper.tick(serverPlayer) && (serverPlayer.tickCount % 5) == 0) {
            TeitokuHelper.syncGameplayState(serverPlayer);
        }
    }

    @SubscribeEvent
    public static void onDimensionChange(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (event.getEntity() instanceof ServerPlayer player && MorphHelper.getActiveProfile(player) != null) {
            MorphHelper.demorph(player);
        }
    }

    @SubscribeEvent
    public static void onHostileShipKilled(LivingDeathEvent event) {
        if (!(event.getEntity() instanceof LegacyShipEntity ship)
                || !ship.isHostileVariant()
                || !(event.getSource().getEntity() instanceof ServerPlayer serverPlayer)) {
            return;
        }

        if (MorphHelper.unlockMorph(serverPlayer, ship.getShipClassId())) {
            serverPlayer.displayClientMessage(Component.translatable("chat.shincolle.morph.unlocked_hostile", ship.getName()), true);
        }
    }
}
