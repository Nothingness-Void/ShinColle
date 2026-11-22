package com.lulan.shincolle.client;

import com.lulan.shincolle.ShinColle;
import com.lulan.shincolle.entity.ship.LegacyShipEntity;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.EntityHitResult;
import com.lulan.shincolle.item.PointerItem;
import com.lulan.shincolle.item.PointerCommandEvents;
import com.lulan.shincolle.network.GameplayCommandHandler;
import com.lulan.shincolle.network.GameplayCommandType;
import com.lulan.shincolle.network.ModNetwork;
import com.lulan.shincolle.network.ServerboundGameplayCommandPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.common.EventBusSubscriber;

@EventBusSubscriber(modid = ShinColle.MOD_ID, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.GAME)
public final class PointerClientEvents {

    private PointerClientEvents() {
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {

        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer player = minecraft.player;
        if (player == null || minecraft.screen != null) {
            return;
        }

        ItemStack pointer = player.getMainHandItem();
        if (!(pointer.getItem() instanceof PointerItem)) {
            return;
        }

        while (minecraft.options.keyPlayerList.consumeClick()) {
            int nextMode = PointerItem.toggleCaressMode(PointerItem.getMode(pointer));
            PointerItem.setMode(pointer, nextMode);
            sendCommand(GameplayCommandType.SET_POINTER_MODE, payload ->
                    payload.putInt(GameplayCommandHandler.TAG_MODE, nextMode));
        }
    }

    @SubscribeEvent
    public static void onClientTickHitZone(ClientTickEvent.Post event) {

        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer player = minecraft.player;
        if (player == null || minecraft.screen != null || (player.tickCount & 3) != 0) {
            return;
        }

        ItemStack pointer = player.getMainHandItem();
        if (!(pointer.getItem() instanceof PointerItem) || !PointerItem.isCaressMode(PointerItem.getMode(pointer))) {
            return;
        }

        if (!(minecraft.hitResult instanceof EntityHitResult entityHit)
                || !(entityHit.getEntity() instanceof LegacyShipEntity ship)
                || ship.isHostileVariant()) {
            return;
        }

        double relY = Mth.clamp((entityHit.getLocation().y - ship.getY()) / Math.max(0.1D, ship.getBbHeight()), 0.0D, 1.0D);
        int hitHeight = Mth.clamp((int) Math.round(relY * 100.0D), 0, 100);
        float lookYaw = (float) (Math.toDegrees(Math.atan2(player.getZ() - ship.getZ(), player.getX() - ship.getX())) - 90.0D);
        int hitAngle = Math.round(Mth.wrapDegrees(lookYaw - ship.getYRot()));

        sendCommand(GameplayCommandType.SET_SHIP_HIT_ZONE, payload -> {
            payload.putInt(GameplayCommandHandler.TAG_SHIP_ID, ship.getId());
            payload.putInt(GameplayCommandHandler.TAG_HIT_HEIGHT, hitHeight);
            payload.putInt(GameplayCommandHandler.TAG_HIT_ANGLE, hitAngle);
        });
    }

    @SubscribeEvent
    public static void onCaressInteraction(PlayerInteractEvent.EntityInteract event) {
        if (!(event.getEntity() instanceof LocalPlayer player)
                || !(event.getTarget() instanceof LegacyShipEntity ship)
                || ship.isHostileVariant() || player.isShiftKeyDown()) {
            return;
        }
        ItemStack pointer = player.getItemInHand(event.getHand());
        if (!(pointer.getItem() instanceof PointerItem) || !PointerItem.isCaressMode(PointerItem.getMode(pointer))) {
            return;
        }
        if (Minecraft.getInstance().hitResult instanceof EntityHitResult hit && hit.getEntity() == ship) {
            double relY = Mth.clamp((hit.getLocation().y - ship.getY()) / Math.max(0.1D, ship.getBbHeight()), 0D, 1D);
            int hitHeight = Mth.clamp((int)Math.round(relY * 100D), 0, 100);
            if (hitHeight > (ship.isOrderedToSit() ? 29 : 58)) ship.startLegacyModelCaress();
        }
    }

    @SubscribeEvent
    public static void onLeftClickEmpty(PlayerInteractEvent.LeftClickEmpty event) {
        if (event.getEntity() instanceof LocalPlayer player) {
            handlePointerLeftClick(player);
        }
    }

    @SubscribeEvent
    public static void onAttackEntity(AttackEntityEvent event) {
        if (event.getEntity() instanceof LocalPlayer player
                && player.getMainHandItem().getItem() instanceof PointerItem
                && !PointerItem.isCaressMode(PointerItem.getMode(player.getMainHandItem()))
                && !PointerCommandEvents.consumesTargetClick(player, event.getTarget())) {
            handlePointerLeftClick(player);
        }
    }

    @SubscribeEvent(receiveCanceled = true)
    public static void onLeftClickBlock(PlayerInteractEvent.LeftClickBlock event) {
        if (!(event.getEntity() instanceof LocalPlayer player)) {
            return;
        }

        if (!(player.getMainHandItem().getItem() instanceof PointerItem)) {
            return;
        }

        event.setCanceled(true);
        handlePointerLeftClick(player);
    }

    private static void handlePointerLeftClick(LocalPlayer player) {
        ItemStack pointer = player.getMainHandItem();
        if (!(pointer.getItem() instanceof PointerItem)) {
            return;
        }

        int mode = PointerItem.getMode(pointer);
        if (!PointerItem.isCaressMode(mode)) {
            EntityHitResult hit = PointerItem.pickCommandTarget(player);
            if (hit != null && PointerCommandEvents.consumesTargetClick(player, hit.getEntity())) {
                sendCommand(GameplayCommandType.POINTER_LEFT_CLICK, payload ->
                        payload.putInt(GameplayCommandHandler.TAG_TARGET_ID, hit.getEntity().getId()));
                return;
            }
        }
        if (player.isShiftKeyDown()) {
            if (player.isSprinting()) {
                sendEmptyCommand(GameplayCommandType.CLEAR_CURRENT_TEAM);
                return;
            }

            int nextMode = PointerItem.cycleCommandMode(mode);
            PointerItem.setMode(pointer, nextMode);
            sendCommand(GameplayCommandType.SET_POINTER_MODE, payload ->
                    payload.putInt(GameplayCommandHandler.TAG_MODE, nextMode));
            return;
        }

        if (player.isSprinting() && mode == 2) {
            sendEmptyCommand(GameplayCommandType.CYCLE_FORMATION);
        }
    }

    private static void sendEmptyCommand(GameplayCommandType type) {
        ModNetwork.sendToServer(ServerboundGameplayCommandPacket.of(type, new CompoundTag()));
    }

    private static void sendCommand(GameplayCommandType type, java.util.function.Consumer<CompoundTag> payloadBuilder) {
        CompoundTag payload = new CompoundTag();
        payloadBuilder.accept(payload);
        ModNetwork.sendToServer(ServerboundGameplayCommandPacket.of(type, payload));
    }
}
