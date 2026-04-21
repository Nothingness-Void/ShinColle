package com.lulan.shincolle.item;

import com.lulan.shincolle.ShinColle;
import com.lulan.shincolle.entity.ship.LegacyShipEntity;
import com.lulan.shincolle.network.GameplayCommandHandler;
import com.lulan.shincolle.network.GameplayCommandType;
import com.lulan.shincolle.network.ModNetwork;
import com.lulan.shincolle.network.ServerboundGameplayCommandPacket;
import com.lulan.shincolle.teitoku.TeitokuData;
import com.lulan.shincolle.teitoku.TeitokuHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
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

        event.setCanceled(true);
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return;
        }

        int mode = PointerItem.getMode(pointer);
        int commandMode = PointerItem.getCommandMode(mode);

        if (event.getTarget() instanceof LegacyShipEntity ship && ship.canCommanderEdit(serverPlayer)) {
            handleOwnedShipLeftClick(serverPlayer, ship, commandMode);
            return;
        }

        String targetClass = event.getTarget().getClass().getSimpleName();
        if (targetClass != null && !targetClass.isBlank()) {
            dispatch(serverPlayer, GameplayCommandType.TOGGLE_TARGET_CLASS, payload ->
                    payload.putString(GameplayCommandHandler.TAG_TARGET_CLASS, targetClass));
        }
    }

    @SubscribeEvent
    public static void onLeftClickBlock(PlayerInteractEvent.LeftClickBlock event) {
        if (event.getEntity().getMainHandItem().getItem() instanceof PointerItem) {
            event.setCanceled(true);
        }
    }

    private static void handleOwnedShipLeftClick(ServerPlayer player, LegacyShipEntity ship, int commandMode) {
        int shipUid = ship.getShipUid();
        if (shipUid <= 0) {
            return;
        }

        TeitokuHelper.get(player).ifPresent(data -> {
            int currentTeamId = data.getCurrentTeamId();
            int currentSlot = findCurrentTeamSlot(data, shipUid);

            if (player.isShiftKeyDown()) {
                if (currentSlot < 0) {
                    return;
                }

                data.setShipUid(currentTeamId, currentSlot, -1);
                data.setCurrentTeamSelection(currentSlot, false);
                data.setPointerSlotCursor(currentSlot);

                if (commandMode == 0) {
                    focusFirstRemainingShip(data, currentSlot);
                }

                TeitokuHelper.syncGameplayState(player);
                return;
            }

            if (currentSlot >= 0) {
                if (commandMode == 0) {
                    data.clearCurrentTeamSelect();
                    data.setCurrentTeamSelection(currentSlot, true);
                } else if (commandMode == 1) {
                    data.toggleCurrentTeamSelect(currentSlot);
                }

                TeitokuHelper.syncGameplayState(player);
                return;
            }

            int slot = findAddSlot(data);
            data.setShipUid(currentTeamId, slot, shipUid);
            data.setCurrentTeamSelection(slot, false);
            data.setPointerSlotCursor((slot + 1) % TeitokuData.TEAM_SIZE);

            if (commandMode == 0) {
                data.clearCurrentTeamSelect();
                data.setCurrentTeamSelection(slot, true);
            }

            TeitokuHelper.syncGameplayState(player);
        });
    }

    private static int findCurrentTeamSlot(TeitokuData data, int shipUid) {
        int currentTeamId = data.getCurrentTeamId();
        for (int slot = 0; slot < TeitokuData.TEAM_SIZE; slot++) {
            if (data.getShipUid(currentTeamId, slot) == shipUid) {
                return slot;
            }
        }
        return -1;
    }

    private static void focusFirstRemainingShip(TeitokuData data, int removedSlot) {
        data.clearCurrentTeamSelect();
        int currentTeamId = data.getCurrentTeamId();
        for (int slot = 0; slot < TeitokuData.TEAM_SIZE; slot++) {
            if (slot != removedSlot && data.getShipUid(currentTeamId, slot) > 0) {
                data.setCurrentTeamSelection(slot, true);
                return;
            }
        }
    }

    private static int findAddSlot(TeitokuData data) {
        int currentTeamId = data.getCurrentTeamId();
        for (int slot = 0; slot < TeitokuData.TEAM_SIZE; slot++) {
            if (data.getShipUid(currentTeamId, slot) <= 0) {
                return slot;
            }
        }
        return data.getPointerSlotCursor();
    }

    private static void dispatch(ServerPlayer player, GameplayCommandType type, java.util.function.Consumer<CompoundTag> payloadBuilder) {
        CompoundTag payload = new CompoundTag();
        payloadBuilder.accept(payload);
        GameplayCommandHandler.handle(player, type, payload);
    }
}
