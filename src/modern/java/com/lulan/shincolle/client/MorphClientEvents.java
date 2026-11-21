package com.lulan.shincolle.client;

import com.lulan.shincolle.ShinColle;
import com.lulan.shincolle.config.ShinColleConfig;
import com.lulan.shincolle.entity.mount.LegacyMountControls;
import com.lulan.shincolle.entity.mount.LegacyShipMountEntity;
import com.lulan.shincolle.entity.ship.LegacyShipEntity;
import com.lulan.shincolle.item.OpToolItem;
import com.lulan.shincolle.network.GameplayCommandHandler;
import com.lulan.shincolle.network.GameplayCommandType;
import com.lulan.shincolle.network.ModNetwork;
import com.lulan.shincolle.network.ServerboundGameplayCommandPacket;
import com.lulan.shincolle.playerskill.PlayerSkillRuntimeState;
import com.lulan.shincolle.playerskill.PlayerSkillHostResolver;
import com.lulan.shincolle.registry.ModItems;
import com.lulan.shincolle.teitoku.TeitokuData;
import com.lulan.shincolle.teitoku.TeitokuHelper;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;

@Mod.EventBusSubscriber(modid = ShinColle.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class MorphClientEvents {

    private static final String KEY_CATEGORY = "key.categories.shincolle";

    private static final KeyMapping OPEN_MORPH = new KeyMapping("key.shincolle.morph.open", GLFW.GLFW_KEY_G, KEY_CATEGORY);
    private static final KeyMapping PRIMARY_ATTACK = new KeyMapping("key.shincolle.morph.primary", GLFW.GLFW_KEY_Z, KEY_CATEGORY);
    private static final KeyMapping SECONDARY_ATTACK = new KeyMapping("key.shincolle.morph.secondary", GLFW.GLFW_KEY_X, KEY_CATEGORY);
    private static final KeyMapping SPECIAL_ATTACK = new KeyMapping("key.shincolle.morph.special", GLFW.GLFW_KEY_C, KEY_CATEGORY);
    private static final KeyMapping OPTOOL_TOGGLE = new KeyMapping("key.shincolle.optool.toggle", GLFW.GLFW_KEY_KP_1, KEY_CATEGORY);
    private static final KeyMapping OPTOOL_LIST = new KeyMapping("key.shincolle.optool.list", GLFW.GLFW_KEY_KP_2, KEY_CATEGORY);

    private MorphClientEvents() {
    }

    @SubscribeEvent
    public static void onRegisterKeyMappings(RegisterKeyMappingsEvent event) {
        event.register(OPEN_MORPH);
        event.register(PRIMARY_ATTACK);
        event.register(SECONDARY_ATTACK);
        event.register(SPECIAL_ATTACK);
        event.register(OPTOOL_TOGGLE);
        event.register(OPTOOL_LIST);
    }

    @Mod.EventBusSubscriber(modid = ShinColle.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
    public static final class ForgeEvents {

        private static final ResourceLocation HUD = ResourceLocation.fromNamespaceAndPath(
                ShinColle.MOD_ID, "textures/gui/guihud.png");

        private ForgeEvents() {
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

            while (OPEN_MORPH.consumeClick()) {
                sendEmptyCommand(GameplayCommandType.OPEN_MORPH_SCREEN);
            }

            PlayerSkillRuntimeState runtimeState = TeitokuHelper.get(player)
                    .map(TeitokuData::getPlayerSkillRuntimeState)
                    .orElse(null);
            if (runtimeState != null && runtimeState.isVisible()) {
                handleHotbarSkillInput(player, minecraft, runtimeState);
            }

            while (PRIMARY_ATTACK.consumeClick()) {
                sendPlayerSkillCommand(1);
            }

            while (SECONDARY_ATTACK.consumeClick()) {
                sendPlayerSkillCommand(2);
            }

            while (SPECIAL_ATTACK.consumeClick()) {
                sendPlayerSkillCommand(5);
            }

            while (OPTOOL_TOGGLE.consumeClick()) {
                if (isHoldingOpTool(player)) {
                    sendOpToolToggle();
                }
            }

            while (OPTOOL_LIST.consumeClick()) {
                if (isHoldingOpTool(player)) {
                    sendEmptyCommand(GameplayCommandType.OPTOOL_PRINT_UNATTACKABLE);
                }
            }
        }

        @SubscribeEvent(receiveCanceled = true)
        public static void onRenderOverlay(RenderGuiOverlayEvent.Pre event) {
            if (!event.getOverlay().id().equals(VanillaGuiOverlay.HOTBAR.id())) return;
            Minecraft minecraft = Minecraft.getInstance();
            LocalPlayer player = minecraft.player;
            if (player == null) {
                return;
            }

            PlayerSkillRuntimeState runtimeState = TeitokuHelper.get(player)
                    .map(TeitokuData::getPlayerSkillRuntimeState)
                    .orElse(null);
            if (runtimeState == null || !runtimeState.isVisible()) {
                return;
            }

            GuiGraphics guiGraphics = event.getGuiGraphics();
            int width = event.getWindow().getGuiScaledWidth();
            int height = event.getWindow().getGuiScaledHeight();
            double[] position = ShinColleConfig.posHUD();
            int px = (int)(width * position[0]);
            int py = (int)(height * position[1]);
            var host = PlayerSkillHostResolver.resolve(player);
            int[] drawAirNum = new int[2];
            if (host.ship() != null) {
                switch (host.ship().getSpec().textureStem()) {
                    case "EntityBattleshipRe", "EntityCarrierWo", "EntityCarrierAkagi", "EntityCarrierKaga",
                            "EntityAirfieldHime", "EntityCarrierHime", "EntityHarbourHime", "EntityIsolatedHime",
                            "EntityMidwayHime", "EntityNorthernHime", "EntityCarrierWDemon" -> {
                        drawAirNum[0] = host.ship().getAircraftLight();
                        drawAirNum[1] = host.ship().getAircraftHeavy();
                    }
                    default -> {
                    }
                }
            }
            for (int slot = 0; slot < PlayerSkillRuntimeState.SLOT_COUNT; slot++) {
                if (!(slot == 4 ? host.profile() != null : runtimeState.isSlotEnabled(slot))) continue;
                int slotX = px - 40 + slot * 21;
                int cooldown = runtimeState.getSlotCooldown(slot);
                int maxCooldown = runtimeState.getSlotMaxCooldown(slot);
                int len = (int)((float)cooldown / (float)maxCooldown * 18F);
                RenderSystem.setShaderColor(1F, 1F, 1F, 1F);
                RenderSystem.enableBlend();
                RenderSystem.defaultBlendFunc();
                guiGraphics.blit(HUD, slotX, py, slot * 18, 0, 18, 18);
                guiGraphics.blit(HUD, slotX, py + 18 - len, 0, 36 - len, 18, len);
                if (len > 0) guiGraphics.drawString(minecraft.font,
                        String.format("%.1f", cooldown * 0.05F), slotX + 5, py + 18, 16776960, true);
                if (minecraft.options.keyHotbarSlots[slot].isDown())
                    guiGraphics.fill(slotX, py, slotX + 18, py + 18, 1073741823);
                if (slot == 2) guiGraphics.drawString(minecraft.font,
                        String.valueOf(drawAirNum[0]), slotX + 7, py - 8, 65344, true);
                else if (slot == 3) guiGraphics.drawString(minecraft.font,
                        String.valueOf(drawAirNum[1]), slotX + 7, py - 8, 65535, true);
            }
            RenderSystem.setShaderColor(1F, 1F, 1F, 1F);
            guiGraphics.blit(HUD, width / 2, height / 2 - 4, 0, 7, 1, 9);
            guiGraphics.blit(HUD, width / 2 - 4, height / 2, 7, 0, 9, 1);
            RenderSystem.setShaderColor(1F, 1F, 1F, 1F);
        }

        private static void handleHotbarSkillInput(LocalPlayer player, Minecraft minecraft, PlayerSkillRuntimeState runtimeState) {
            int selectedSlot = player.getInventory().selected;
            boolean usedSkillKey = false;

            for (int slot = 0; slot < PlayerSkillRuntimeState.SLOT_COUNT; slot++) {
                while (minecraft.options.keyHotbarSlots[slot].consumeClick()) {
                    sendPlayerSkillCommand(slot + 1);
                    usedSkillKey = true;
                }
            }

            if (usedSkillKey) {
                player.getInventory().selected = selectedSlot;
            }
        }

        private static LivingEntity resolveCrosshairTarget() {
            Minecraft minecraft = Minecraft.getInstance();
            if (minecraft.hitResult instanceof EntityHitResult entityHitResult
                    && entityHitResult.getEntity() instanceof LivingEntity living) {
                return living;
            }

            Entity target = minecraft.crosshairPickEntity;
            return target instanceof LivingEntity living ? living : null;
        }

        private static BlockPos resolveCrosshairBlockPos() {
            Minecraft minecraft = Minecraft.getInstance();
            if (minecraft.hitResult instanceof BlockHitResult blockHitResult && blockHitResult.getType() == HitResult.Type.BLOCK) {
                return blockHitResult.getBlockPos();
            }
            return null;
        }

        private static void sendPlayerSkillCommand(int slot) {
            CompoundTag payload = new CompoundTag();
            payload.putInt(GameplayCommandHandler.TAG_SKILL_SLOT, slot);

            LivingEntity target = resolveCrosshairTarget();
            if (target != null) {
                payload.putInt(GameplayCommandHandler.TAG_TARGET_ID, target.getId());
            } else {
                BlockPos blockPos = resolveCrosshairBlockPos();
                if (blockPos != null) {
                    payload.putInt(GameplayCommandHandler.TAG_X, blockPos.getX());
                    payload.putInt(GameplayCommandHandler.TAG_Y, blockPos.getY());
                    payload.putInt(GameplayCommandHandler.TAG_Z, blockPos.getZ());
                }
            }

            ModNetwork.sendToServer(ServerboundGameplayCommandPacket.of(GameplayCommandType.PLAYER_CAST_SKILL, payload));
        }

        private static void sendOpToolToggle() {
            CompoundTag payload = new CompoundTag();
            LivingEntity target = resolveCrosshairTarget();
            if (OpToolItem.isExcludedTarget(target)) {
                return;
            }
            if (target != null) {
                payload.putInt(GameplayCommandHandler.TAG_TARGET_ID, target.getId());
            }
            ModNetwork.sendToServer(ServerboundGameplayCommandPacket.of(GameplayCommandType.OPTOOL_TOGGLE_UNATTACKABLE, payload));
        }

        private static void sendEmptyCommand(GameplayCommandType type) {
            ModNetwork.sendToServer(ServerboundGameplayCommandPacket.of(type, new CompoundTag()));
        }

        private static int lastMountKeyMask;

        @SubscribeEvent
        public static void onClientTickMountControl(TickEvent.ClientTickEvent event) {
            if (event.phase != TickEvent.Phase.END) {
                return;
            }

            Minecraft minecraft = Minecraft.getInstance();
            LocalPlayer player = minecraft.player;
            if (player == null) {
                lastMountKeyMask = 0;
                return;
            }

            boolean ridingShipMount = player.getVehicle() instanceof LegacyShipMountEntity;
            boolean carryingOrRidingShip = ridingShipMount
                    || player.getVehicle() instanceof LegacyShipEntity
                    || player.getFirstPassenger() instanceof LegacyShipEntity;

            if (minecraft.screen == null && carryingOrRidingShip) {
                while (minecraft.options.keyInventory.consumeClick()) {
                    sendEmptyCommand(GameplayCommandType.OPEN_MOUNT_SHIP_INVENTORY);
                }
            }

            if (!ridingShipMount) {
                lastMountKeyMask = 0;
                return;
            }

            int keyMask = 0;
            if (minecraft.screen == null) {
                if (minecraft.options.keyUp.isDown()) {
                    keyMask |= LegacyMountControls.KEY_FORWARD;
                }
                if (minecraft.options.keyDown.isDown()) {
                    keyMask |= LegacyMountControls.KEY_BACK;
                }
                if (minecraft.options.keyLeft.isDown()) {
                    keyMask |= LegacyMountControls.KEY_LEFT;
                }
                if (minecraft.options.keyRight.isDown()) {
                    keyMask |= LegacyMountControls.KEY_RIGHT;
                }
                if (minecraft.options.keyJump.isDown()) {
                    keyMask |= LegacyMountControls.KEY_JUMP;
                }
            }

            if ((keyMask != 0 && (player.tickCount % 2 == 0 || keyMask != lastMountKeyMask))
                    || (keyMask == 0 && lastMountKeyMask != 0)) {
                CompoundTag payload = new CompoundTag();
                payload.putInt(GameplayCommandHandler.TAG_MODE, keyMask);
                ModNetwork.sendToServer(ServerboundGameplayCommandPacket.of(GameplayCommandType.MOUNT_MOVE, payload));
            }
            lastMountKeyMask = keyMask;
        }

        private static boolean isHoldingOpTool(LocalPlayer player) {
            return player.getMainHandItem().is(ModItems.OPTOOL.get()) || player.getOffhandItem().is(ModItems.OPTOOL.get());
        }
    }
}
