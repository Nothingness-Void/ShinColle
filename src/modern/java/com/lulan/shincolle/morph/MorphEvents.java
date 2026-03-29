package com.lulan.shincolle.morph;

import com.lulan.shincolle.ShinColle;
import com.lulan.shincolle.entity.ship.LegacyShipEntity;
import com.lulan.shincolle.item.TargetWrenchItem;
import com.lulan.shincolle.teitoku.TeitokuData;
import com.lulan.shincolle.teitoku.TeitokuHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
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
    public static void onAttackEntity(AttackEntityEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer serverPlayer)
                || !(serverPlayer.getMainHandItem().getItem() instanceof TargetWrenchItem)
                || !(event.getTarget() instanceof LegacyShipEntity ship)
                || ship.isHostileVariant()
                || !ship.canCommanderEdit(serverPlayer)) {
            return;
        }

        event.setCanceled(true);
        boolean unlocked = MorphHelper.unlockMorph(serverPlayer, ship.getShipClassId());
        TeitokuHelper.get(serverPlayer).ifPresent(data -> data.setSelectedMorphProfile(ship.getShipClassId()));
        TeitokuHelper.syncGameplayState(serverPlayer);
        serverPlayer.displayClientMessage(Component.literal(unlocked
                ? "Unlocked morph: " + ship.getName().getString()
                : "Selected morph: " + ship.getName().getString()), true);
        MorphHelper.openMorphScreen(serverPlayer);
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

    @SubscribeEvent
    public static void onRightClickItem(PlayerInteractEvent.RightClickItem event) {
        if (event.getLevel().isClientSide() || !(event.getEntity() instanceof ServerPlayer serverPlayer)) {
            return;
        }

        if (MorphSupportHelper.handleImmediateUse(serverPlayer, event.getHand())) {
            event.setCanceled(true);
            event.setCancellationResult(InteractionResult.SUCCESS);
        }
    }

    @SubscribeEvent
    public static void onItemFinished(LivingEntityUseItemEvent.Finish event) {
        if (!(event.getEntity() instanceof ServerPlayer serverPlayer)) {
            return;
        }

        MorphSupportHelper.handleFinishedConsume(serverPlayer, event.getItem());
    }
}
