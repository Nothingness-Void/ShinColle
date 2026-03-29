package com.lulan.shincolle.client;

import com.lulan.shincolle.ShinColle;
import com.lulan.shincolle.entity.ship.LegacyShipAttackKind;
import com.lulan.shincolle.entity.ship.LegacyShipAttackProfile;
import com.lulan.shincolle.entity.ship.LegacyShipCombatHelper;
import com.lulan.shincolle.morph.MorphHelper;
import com.lulan.shincolle.morph.MorphProfile;
import com.lulan.shincolle.morph.MorphRuntimeState;
import com.lulan.shincolle.network.GameplayCommandHandler;
import com.lulan.shincolle.network.GameplayCommandType;
import com.lulan.shincolle.network.ModNetwork;
import com.lulan.shincolle.network.ServerboundGameplayCommandPacket;
import com.lulan.shincolle.registry.ModItems;
import com.lulan.shincolle.teitoku.TeitokuData;
import com.lulan.shincolle.teitoku.TeitokuHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
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

            while (PRIMARY_ATTACK.consumeClick()) {
                sendAttackCommand(resolvePrimaryAttack(player));
            }

            while (SECONDARY_ATTACK.consumeClick()) {
                sendAttackCommand(resolveSecondaryAttack(player));
            }

            while (SPECIAL_ATTACK.consumeClick()) {
                sendSpecialCommand();
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
            if (player == null) {
                return;
            }

            MorphProfile profile = MorphHelper.getActiveProfile(player);
            if (profile == null) {
                return;
            }

            MorphRuntimeState runtimeState = TeitokuHelper.get(player)
                    .map(TeitokuData::getMorphRuntimeState)
                    .orElse(null);
            if (runtimeState == null) {
                return;
            }

            GuiGraphics guiGraphics = event.getGuiGraphics();
            int left = 8;
            int top = event.getWindow().getGuiScaledHeight() - 58;

            guiGraphics.drawString(minecraft.font, profile.getSpec().displayName(), left, top, 0xF4E7C2, false);
            guiGraphics.drawString(minecraft.font,
                    Component.literal("L " + profile.getAmmoLight()
                            + "  H " + profile.getAmmoHeavy()
                            + "  G " + profile.getGrudge()),
                    left, top + 10, 0xF1F1F1, false);
            guiGraphics.drawString(minecraft.font,
                    Component.literal("P:" + runtimeState.getAttackCooldown(resolvePrimaryAttack(player))
                            + "  S:" + runtimeState.getAttackCooldown(resolveSecondaryAttack(player))
                            + (MorphHelper.hasSpecialSkill(profile) ? "  C:" + runtimeState.getSpecialCooldown() : "")),
                    left, top + 20, 0xD0D0D0, false);
            guiGraphics.drawString(minecraft.font,
                    Component.literal("[G] Morph  [Shift+G] Mount  [Z/X] Attack  [C] Skill"),
                    left, top + 30, 0xAFAFAF, false);
            guiGraphics.drawString(minecraft.font, MorphHelper.getSkillBarLabel(player), left, top + 40, 0x8FE3FF, false);
        }

        private static LegacyShipAttackKind resolvePrimaryAttack(LocalPlayer player) {
            MorphProfile profile = MorphHelper.getActiveProfile(player);
            if (profile == null) {
                return LegacyShipAttackKind.MELEE;
            }

            LivingEntity target = resolveCrosshairTarget();
            LegacyShipAttackProfile attackProfile = profile.buildAttackProfile();
            boolean flyingTarget = target != null && LegacyShipCombatHelper.isFlyingTarget(target);

            if (flyingTarget && attackProfile.airLight()) {
                return LegacyShipAttackKind.AIR_LIGHT;
            }
            if (attackProfile.light()) {
                return LegacyShipAttackKind.LIGHT;
            }
            if (attackProfile.heavy()) {
                return LegacyShipAttackKind.HEAVY;
            }
            if (attackProfile.airHeavy()) {
                return LegacyShipAttackKind.AIR_HEAVY;
            }
            if (attackProfile.airLight()) {
                return LegacyShipAttackKind.AIR_LIGHT;
            }
            return LegacyShipAttackKind.MELEE;
        }

        private static LegacyShipAttackKind resolveSecondaryAttack(LocalPlayer player) {
            MorphProfile profile = MorphHelper.getActiveProfile(player);
            if (profile == null) {
                return LegacyShipAttackKind.MELEE;
            }

            LivingEntity target = resolveCrosshairTarget();
            LegacyShipAttackProfile attackProfile = profile.buildAttackProfile();
            boolean flyingTarget = target != null && LegacyShipCombatHelper.isFlyingTarget(target);

            if (attackProfile.heavy()) {
                return LegacyShipAttackKind.HEAVY;
            }
            if (flyingTarget && attackProfile.airHeavy()) {
                return LegacyShipAttackKind.AIR_HEAVY;
            }
            if (attackProfile.airHeavy()) {
                return LegacyShipAttackKind.AIR_HEAVY;
            }
            if (attackProfile.light()) {
                return LegacyShipAttackKind.LIGHT;
            }
            if (attackProfile.airLight()) {
                return LegacyShipAttackKind.AIR_LIGHT;
            }
            return LegacyShipAttackKind.MELEE;
        }

        private static @org.jetbrains.annotations.Nullable LivingEntity resolveCrosshairTarget() {
            Minecraft minecraft = Minecraft.getInstance();
            Entity target = minecraft.crosshairPickEntity;
            return target instanceof LivingEntity living ? living : null;
        }

        private static void sendAttackCommand(LegacyShipAttackKind attackKind) {
            LivingEntity target = resolveCrosshairTarget();
            if (target == null) {
                return;
            }

            CompoundTag payload = new CompoundTag();
            payload.putInt(GameplayCommandHandler.TAG_TARGET_ID, target.getId());
            payload.putInt(GameplayCommandHandler.TAG_ATTACK_KIND, attackKind.ordinal());
            ModNetwork.sendToServer(ServerboundGameplayCommandPacket.of(GameplayCommandType.MORPH_CAST_ATTACK, payload));
        }

        private static void sendSpecialCommand() {
            CompoundTag payload = new CompoundTag();
            LivingEntity target = resolveCrosshairTarget();
            if (target != null) {
                payload.putInt(GameplayCommandHandler.TAG_TARGET_ID, target.getId());
            }
            ModNetwork.sendToServer(ServerboundGameplayCommandPacket.of(GameplayCommandType.MORPH_CAST_SPECIAL, payload));
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
