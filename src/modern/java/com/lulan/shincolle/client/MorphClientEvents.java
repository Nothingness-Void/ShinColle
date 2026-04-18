package com.lulan.shincolle.client;

import com.lulan.shincolle.ShinColle;
import com.lulan.shincolle.morph.MorphHelper;
import com.lulan.shincolle.morph.MorphHostMode;
import com.lulan.shincolle.network.GameplayCommandHandler;
import com.lulan.shincolle.network.GameplayCommandType;
import com.lulan.shincolle.network.ModNetwork;
import com.lulan.shincolle.network.ServerboundGameplayCommandPacket;
import com.lulan.shincolle.playerskill.PlayerSkillRuntimeState;
import com.lulan.shincolle.registry.ModItems;
import com.lulan.shincolle.teitoku.TeitokuData;
import com.lulan.shincolle.teitoku.TeitokuHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;

import java.util.Locale;

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

        private static final int SLOT_SIZE = 20;
        private static final int SLOT_GAP = 2;

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
                sendEmptyCommand(player.isShiftKeyDown()
                        ? GameplayCommandType.MORPH_TOGGLE_MOUNT
                        : GameplayCommandType.OPEN_MORPH_SCREEN);
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

        @SubscribeEvent
        public static void onRenderOverlay(RenderGuiOverlayEvent.Post event) {
            Minecraft minecraft = Minecraft.getInstance();
            LocalPlayer player = minecraft.player;
            if (player == null || minecraft.screen != null) {
                return;
            }

            PlayerSkillRuntimeState runtimeState = TeitokuHelper.get(player)
                    .map(TeitokuData::getPlayerSkillRuntimeState)
                    .orElse(null);
            if (runtimeState == null || !runtimeState.isVisible()) {
                return;
            }

            GuiGraphics guiGraphics = event.getGuiGraphics();
            int centerX = event.getWindow().getGuiScaledWidth() / 2;
            int baseY = event.getWindow().getGuiScaledHeight() - 78;
            int totalWidth = PlayerSkillRuntimeState.SLOT_COUNT * SLOT_SIZE + (PlayerSkillRuntimeState.SLOT_COUNT - 1) * SLOT_GAP;
            int left = centerX - totalWidth / 2;

            Component title = MorphHelper.getPlayerSkillTitle(player);
            Component resources = MorphHelper.getPlayerSkillResourceLine(player);
            Component mode = Component.literal(runtimeState.getHostMode().name());
            int titleWidth = minecraft.font.width(title);
            int resourceWidth = minecraft.font.width(resources);
            guiGraphics.drawString(minecraft.font, title, centerX - titleWidth / 2, baseY - 22, 0xF3E7BF, false);
            guiGraphics.drawString(minecraft.font, resources, centerX - resourceWidth / 2, baseY - 12, 0xCDD7DF, false);
            guiGraphics.drawString(minecraft.font, mode, left - 42, baseY + 6, hostModeColor(runtimeState.getHostMode()), false);

            for (int slot = 0; slot < PlayerSkillRuntimeState.SLOT_COUNT; slot++) {
                int slotX = left + slot * (SLOT_SIZE + SLOT_GAP);
                boolean enabled = runtimeState.isSlotEnabled(slot);
                int cooldown = runtimeState.getSlotCooldown(slot);
                int maxCooldown = runtimeState.getSlotMaxCooldown(slot);

                guiGraphics.fill(slotX, baseY, slotX + SLOT_SIZE, baseY + SLOT_SIZE, 0xCC11161A);
                guiGraphics.fill(slotX + 1, baseY + 1, slotX + SLOT_SIZE - 1, baseY + SLOT_SIZE - 1,
                        enabled ? 0xCC294048 : 0xAA22262B);
                guiGraphics.fill(slotX, baseY, slotX + SLOT_SIZE, baseY + 1, enabled ? 0xFFE4D4A2 : 0xFF555C60);
                guiGraphics.fill(slotX, baseY + SLOT_SIZE - 1, slotX + SLOT_SIZE, baseY + SLOT_SIZE, enabled ? 0xFF6B7B82 : 0xFF44484C);

                if (maxCooldown > 0 && cooldown > 0) {
                    int overlayHeight = Math.max(1, Math.round((cooldown / (float) maxCooldown) * (SLOT_SIZE - 2)));
                    guiGraphics.fill(slotX + 1, baseY + SLOT_SIZE - 1 - overlayHeight,
                            slotX + SLOT_SIZE - 1, baseY + SLOT_SIZE - 1, 0x99232A2F);
                }

                if (minecraft.options.keyHotbarSlots[slot].isDown() || (slot == 4 && SPECIAL_ATTACK.isDown())) {
                    guiGraphics.fill(slotX + 1, baseY + 1, slotX + SLOT_SIZE - 1, baseY + SLOT_SIZE - 1, 0x55FFF3C2);
                }

                guiGraphics.drawString(minecraft.font, String.valueOf(slot + 1), slotX + 2, baseY + 2, 0xFFF7E6AD, false);
                drawCentered(guiGraphics, getSlotLabel(player, runtimeState, slot), slotX + SLOT_SIZE / 2, baseY + 7,
                        enabled ? 0xFFF0F4F7 : 0xFF7B858D);
                if (cooldown > 0) {
                    String seconds = String.format(Locale.ROOT, "%.1f", cooldown / 20.0F);
                    drawCentered(guiGraphics, seconds, slotX + SLOT_SIZE / 2, baseY + 13, 0xFFECD990);
                }
            }

            guiGraphics.drawString(minecraft.font,
                    Component.literal("[1-5] Skills  [G] Morph  [Shift+G] Mount  [Z/X/C] Quick"),
                    centerX - 108, baseY + 24, 0x8FD7DDEA, false);
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

        private static void drawCentered(GuiGraphics guiGraphics, String text, int centerX, int y, int color) {
            Minecraft minecraft = Minecraft.getInstance();
            guiGraphics.drawString(minecraft.font, text, centerX - minecraft.font.width(text) / 2, y, color, false);
        }

        private static int hostModeColor(MorphHostMode mode) {
            return switch (mode) {
                case MORPH -> 0x8FE3FF;
                case MOUNT -> 0xA6F2C1;
                case RIDER -> 0xF3C88B;
                default -> 0x90979C;
            };
        }

        private static String getSlotLabel(LocalPlayer player, PlayerSkillRuntimeState runtimeState, int slot) {
            return switch (slot) {
                case 0 -> "L";
                case 1 -> "H";
                case 2 -> "AL";
                case 3 -> "AH";
                case 4 -> runtimeState.getHostMode() == MorphHostMode.RIDER
                        ? "--"
                        : (MorphHelper.hasSpecialSkill(player) ? "SP" : "M");
                default -> "-";
            };
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
            if (target != null) {
                payload.putInt(GameplayCommandHandler.TAG_TARGET_ID, target.getId());
            }
            ModNetwork.sendToServer(ServerboundGameplayCommandPacket.of(GameplayCommandType.OPTOOL_TOGGLE_UNATTACKABLE, payload));
        }

        private static void sendEmptyCommand(GameplayCommandType type) {
            ModNetwork.sendToServer(ServerboundGameplayCommandPacket.of(type, new CompoundTag()));
        }

        private static boolean isHoldingOpTool(LocalPlayer player) {
            return player.getMainHandItem().is(ModItems.OPTOOL.get()) || player.getOffhandItem().is(ModItems.OPTOOL.get());
        }
    }
}
