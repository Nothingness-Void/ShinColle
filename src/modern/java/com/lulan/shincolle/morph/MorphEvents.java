package com.lulan.shincolle.morph;

import com.lulan.shincolle.ShinColle;
import com.lulan.shincolle.entity.ship.LegacyShipEntity;
import com.lulan.shincolle.teitoku.TeitokuData;
import com.lulan.shincolle.teitoku.TeitokuHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = ShinColle.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class MorphEvents {

    private MorphEvents() {
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.player.level().isClientSide()) {
            return;
        }
        if (!(event.player instanceof ServerPlayer serverPlayer)) {
            return;
        }

        if (MorphHelper.tick(serverPlayer) && (serverPlayer.tickCount % 5) == 0) {
            TeitokuHelper.syncGameplayState(serverPlayer);
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
            serverPlayer.displayClientMessage(Component.literal("Unlocked hostile morph: " + ship.getName().getString()), true);
        }
    }
}
