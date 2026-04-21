package com.lulan.shincolle.client;

import com.lulan.shincolle.ShinColle;
import com.lulan.shincolle.item.PointerItem;
import com.lulan.shincolle.network.GameplayCommandHandler;
import com.lulan.shincolle.network.GameplayCommandType;
import com.lulan.shincolle.network.ModNetwork;
import com.lulan.shincolle.network.ServerboundGameplayCommandPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = ShinColle.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class PointerClientEvents {

    private PointerClientEvents() {
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }

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
    public static void onLeftClickEmpty(PlayerInteractEvent.LeftClickEmpty event) {
        if (event.getEntity() instanceof LocalPlayer player) {
            handlePointerLeftClick(player);
        }
    }

    @SubscribeEvent
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
