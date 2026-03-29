package com.lulan.shincolle.item;

import com.lulan.shincolle.ShinColle;
import com.lulan.shincolle.entity.ship.LegacyShipEntity;
import com.lulan.shincolle.network.GameplayCommandHandler;
import com.lulan.shincolle.network.GameplayCommandType;
import com.lulan.shincolle.teitoku.TeitokuData;
import com.lulan.shincolle.teitoku.TeitokuHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = ShinColle.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class PointerCommandEvents {

    private PointerCommandEvents() {
    }

    @SubscribeEvent
    public static void onAttackEntity(AttackEntityEvent event) {
        Player player = event.getEntity();
        ItemStack pointer = player.getMainHandItem();
        if (!(pointer.getItem() instanceof PointerItem)) {
            return;
        }
        if (!(player instanceof ServerPlayer serverPlayer)) {
            event.setCanceled(true);
            return;
        }

        int mode = PointerItem.getMode(pointer);
        if (mode == 3) {
            return;
        }

        if (event.getTarget() instanceof LegacyShipEntity ship && ship.canCommanderEdit(serverPlayer)) {
            handleOwnedShipLeftClick(serverPlayer, ship);
            event.setCanceled(true);
            return;
        }

        if (mode == 2 && event.getTarget() instanceof net.minecraft.world.entity.LivingEntity livingEntity) {
            String targetClass = TeitokuHelper.resolveTargetClass(livingEntity);
            if (!targetClass.isBlank()) {
                dispatch(serverPlayer, GameplayCommandType.TOGGLE_TARGET_CLASS, payload ->
                        payload.putString(GameplayCommandHandler.TAG_TARGET_CLASS, targetClass));
            }
            event.setCanceled(true);
            return;
        }

        event.setCanceled(true);
    }

    private static void handleOwnedShipLeftClick(ServerPlayer player, LegacyShipEntity ship) {
        int shipUid = ship.getShipUid();
        if (shipUid <= 0) {
            return;
        }

        int currentSlot = TeitokuHelper.getCurrentTeamSlotOfShip(player, shipUid);
        if (player.isShiftKeyDown()) {
            if (currentSlot >= 0) {
                int clearSlot = currentSlot;
                dispatch(player, GameplayCommandType.ASSIGN_SHIP_SLOT, payload -> {
                    payload.putInt(GameplayCommandHandler.TAG_SLOT, clearSlot);
                    payload.putInt(GameplayCommandHandler.TAG_SHIP_UID, -1);
                    payload.putInt(GameplayCommandHandler.TAG_SHIP_ID, -1);
                });
                player.displayClientMessage(Component.translatable("chat.shincolle.formation.ship_removed"), true);
            }
            return;
        }

        if (currentSlot >= 0) {
            int selectSlot = currentSlot;
            dispatch(player, GameplayCommandType.TOGGLE_SHIP_SELECT, payload ->
                    payload.putInt(GameplayCommandHandler.TAG_SLOT, selectSlot));
            return;
        }

        int freeSlot = findFirstFreeSlot(player);
        if (freeSlot < 0) {
            player.displayClientMessage(Component.translatable("chat.shincolle.formation.team_full"), true);
            return;
        }

        dispatch(player, GameplayCommandType.ASSIGN_SHIP_SLOT, payload -> {
            payload.putInt(GameplayCommandHandler.TAG_SLOT, freeSlot);
            payload.putInt(GameplayCommandHandler.TAG_SHIP_ID, ship.getId());
            payload.putInt(GameplayCommandHandler.TAG_SHIP_UID, shipUid);
        });
        dispatch(player, GameplayCommandType.SET_SLOT_SELECTION, payload -> {
            payload.putInt(GameplayCommandHandler.TAG_SLOT, freeSlot);
            payload.putBoolean(GameplayCommandHandler.TAG_SELECTED, true);
        });
        player.displayClientMessage(Component.translatable("chat.shincolle.formation.ship_added"), true);
    }

    private static int findFirstFreeSlot(ServerPlayer player) {
        return TeitokuHelper.get(player)
                .map(data -> {
                    int teamId = data.getCurrentTeamId();
                    for (int slot = 0; slot < TeitokuData.TEAM_SIZE; slot++) {
                        if (data.getShipUid(teamId, slot) <= 0) {
                            return slot;
                        }
                    }
                    return -1;
                })
                .orElse(-1);
    }

    private static void dispatch(ServerPlayer player, GameplayCommandType type, java.util.function.Consumer<CompoundTag> payloadBuilder) {
        CompoundTag payload = new CompoundTag();
        payloadBuilder.accept(payload);
        GameplayCommandHandler.handle(player, type, payload);
    }
}
