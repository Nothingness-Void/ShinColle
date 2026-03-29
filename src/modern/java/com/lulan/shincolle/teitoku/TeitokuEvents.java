package com.lulan.shincolle.teitoku;

import com.lulan.shincolle.ShinColle;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerChangedDimensionEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerLoggedInEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerRespawnEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = ShinColle.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class TeitokuEvents {

    private static final ResourceLocation TEITOKU_ID =
            ResourceLocation.fromNamespaceAndPath(ShinColle.MOD_ID, "teitoku");

    private TeitokuEvents() {
    }

    public static void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.register(TeitokuData.class);
    }

    @SubscribeEvent
    public static void attachCapabilities(AttachCapabilitiesEvent<Entity> event) {
        if (event.getObject() instanceof Player) {
            event.addCapability(TEITOKU_ID, new TeitokuDataProvider());
        }
    }

    @SubscribeEvent
    public static void clonePlayer(PlayerEvent.Clone event) {
        event.getOriginal().reviveCaps();
        event.getOriginal().getCapability(TeitokuHelper.TEITOKU_CAPABILITY).ifPresent(oldData ->
                event.getEntity().getCapability(TeitokuHelper.TEITOKU_CAPABILITY).ifPresent(newData ->
                        newData.copyFrom(oldData)));
        event.getOriginal().invalidateCaps();
    }

    @SubscribeEvent
    public static void onPlayerLogin(PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
            TeitokuHelper.initializeAndSync(serverPlayer);
        }
    }

    @SubscribeEvent
    public static void onPlayerRespawn(PlayerRespawnEvent event) {
        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
            TeitokuHelper.initializeAndSync(serverPlayer);
        }
    }

    @SubscribeEvent
    public static void onPlayerChangedDimension(PlayerChangedDimensionEvent event) {
        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
            TeitokuHelper.initializeAndSync(serverPlayer);
        }
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.player.level().isClientSide()) {
            return;
        }
        if (!(event.player instanceof ServerPlayer serverPlayer)) {
            return;
        }

        int cooldown = TeitokuHelper.getTeamCooldown(serverPlayer);
        int bossCooldown = TeitokuHelper.get(serverPlayer).map(TeitokuData::getBossCooldown).orElse(0);
        boolean shouldSync = false;

        if (cooldown > 0) {
            int next = TeitokuHelper.tickCooldown(serverPlayer);
            shouldSync |= next == 0 || (serverPlayer.tickCount % 20) == 0;
        }

        if (bossCooldown > 0) {
            int nextBoss = TeitokuHelper.tickBossCooldown(serverPlayer);
            shouldSync |= nextBoss == 0 || (serverPlayer.tickCount % 20) == 0;
        }

        if (shouldSync) {
            TeitokuHelper.syncGameplayState(serverPlayer);
        }
    }
}
