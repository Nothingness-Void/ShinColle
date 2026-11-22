package com.lulan.shincolle.client;

import com.lulan.shincolle.ShinColle;
import com.lulan.shincolle.teitoku.TeitokuEvents;
import com.lulan.shincolle.teitoku.TeitokuHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.client.event.ViewportEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.common.EventBusSubscriber;

@EventBusSubscriber(modid = ShinColle.MOD_ID, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.GAME)
public final class RingClientEvents {

    private RingClientEvents() {
    }

    @SubscribeEvent(receiveCanceled = true)
    public static void onRenderFog(ViewportEvent.RenderFog event) {
        Entity camera = event.getCamera().getEntity();
        var player = Minecraft.getInstance().player;
        if (camera == null || player == null || !TeitokuEvents.isInLegacyLiquid(camera)) {
            return;
        }
        int cap = TeitokuHelper.getEffectiveRingAbility(player.level())[3];
        if (cap < 0) {
            return;
        }
        TeitokuHelper.get(player).ifPresent(data -> {
            if (data.hasRing() && data.isRingActive()) {
                float ratio = cap == 0 ? 0.0001F
                        : Math.max(0.0001F, (float) (cap - data.getMarriageNum()) / cap);
                event.setNearPlaneDistance(event.getNearPlaneDistance() / ratio);
                event.setFarPlaneDistance(event.getFarPlaneDistance() / ratio);
                event.setCanceled(true);
            }
        });
    }
}
