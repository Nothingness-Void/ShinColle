package com.lulan.shincolle.morph;

import com.lulan.shincolle.item.CombatRationItem;
import com.lulan.shincolle.item.LegacyShipSupportItem;
import com.lulan.shincolle.registry.ModItems;
import com.lulan.shincolle.teitoku.TeitokuHelper;
import com.mojang.datafixers.util.Pair;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionUtils;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public final class MorphSupportHelper {

    private MorphSupportHelper() {
    }

    public static boolean handleImmediateUse(ServerPlayer player, InteractionHand hand) {
        MorphProfile profile = MorphHelper.getActiveProfile(player);
        if (profile == null) {
            return false;
        }

        ItemStack stack = player.getItemInHand(hand);
        if (stack.isEmpty()) {
            return false;
        }

        if (stack.getItem() instanceof LegacyShipSupportItem) {
            MorphSupportAction action = resolveSupportAction(stack, player);
            if (action == null) {
                return false;
            }

            applySupportAction(player, profile, action, true);
            replenishCombatResources(profile, stack);
            consumeHeldItem(player, hand, 1);
            syncAndNotify(player, profile);
            return true;
        }

        if (stack.is(ModItems.BUCKETREPAIR.get())) {
            if (player.getHealth() >= player.getMaxHealth()) {
                return false;
            }

            player.heal(player.getMaxHealth());
            consumeHeldItem(player, hand, 1);
            syncAndNotify(player, profile);
            return true;
        }

        if (stack.is(ModItems.MODERNKIT.get())) {
            if (!profile.addRandomModernization(player.getRandom())) {
                return false;
            }

            consumeHeldItem(player, hand, 1);
            syncAndNotify(player, profile);
            return true;
        }

        if (stack.is(ModItems.TRAININGBOOK.get())) {
            if (profile.getLevel() >= profile.getLevelCap()) {
                return false;
            }

            profile.setLevel(profile.getLevel() + 1);
            profile.setExperience(0);
            consumeHeldItem(player, hand, 1);
            syncAndNotify(player, profile);
            return true;
        }

        if (stack.is(ModItems.MARRIAGERING.get())) {
            if (!player.isShiftKeyDown() || profile.isMarried()) {
                return false;
            }

            profile.setMarried(true);
            profile.setMorale(MorphProfile.MAX_MORALE);
            for (int step = 0; step < 3; step++) {
                profile.addRandomModernization(player.getRandom());
            }
            player.heal(player.getMaxHealth());
            consumeHeldItem(player, hand, 1);
            TeitokuHelper.incrementMarriageCount(player);
            TeitokuHelper.addCollectedShip(player, profile.getLegacyClassId());
            syncAndNotify(player, profile);
            return true;
        }

        return false;
    }

    public static boolean handleFinishedConsume(ServerPlayer player, ItemStack consumedStack) {
        MorphProfile profile = MorphHelper.getActiveProfile(player);
        if (profile == null || consumedStack.isEmpty()) {
            return false;
        }

        if (consumedStack.getItem() instanceof CombatRationItem ration) {
            profile.addMorale(scaleSupportMorale(profile, ration.getMoraleValue()));
            player.heal((float) Math.max(1.0F, player.getMaxHealth() * 0.025F * supportMultiplier(profile)));

            String itemPath = BuiltInRegistries.ITEM.getKey(consumedStack.getItem()).getPath();
            if ("combatration4".equals(itemPath) || "combatration5".equals(itemPath)) {
                player.removeAllEffects();
            }

            syncAndNotify(player, profile);
            return true;
        }

        MorphSupportAction action = resolveSupportAction(consumedStack, player);
        if (action == null) {
            return false;
        }

        applySupportAction(player, profile, action, false);
        syncAndNotify(player, profile);
        return true;
    }

    private static void applySupportAction(ServerPlayer player, MorphProfile profile,
                                           MorphSupportAction action, boolean applyEffects) {
        if (action.clearsNegativeStates()) {
            clearNegativeEffects(player);
        }

        if (action.removesPoison()) {
            player.removeEffect(net.minecraft.world.effect.MobEffects.POISON);
        }

        if (applyEffects) {
            for (MobEffectInstance effect : action.effects()) {
                applyEffect(player, effect);
            }
        }

        if (action.healRatio() > 0.0F) {
            player.heal((float) Math.max(1.0F, player.getMaxHealth() * action.healRatio() * supportMultiplier(profile)));
        }

        if (action.moraleGain() != 0) {
            profile.addMorale(scaleSupportMorale(profile, action.moraleGain()));
        }
    }

    private static void replenishCombatResources(MorphProfile profile, ItemStack stack) {
        if (stack.is(ModItems.GRUDGE.get())) {
            profile.addGrudge(4);
            return;
        }
        if (stack.is(ModItems.GRUDGE1.get())) {
            profile.addGrudge(8);
            return;
        }
        if (stack.is(ModItems.AMMO.get())) {
            profile.addAmmoLight(4);
            return;
        }
        if (stack.is(ModItems.AMMO1.get())) {
            profile.addAmmoLight(8);
            return;
        }
        if (stack.is(ModItems.AMMO2.get())) {
            profile.addAmmoHeavy(4);
            return;
        }
        if (stack.is(ModItems.AMMO3.get())) {
            profile.addAmmoHeavy(8);
            return;
        }
        if (stack.is(ModItems.TOYAIRPLANE.get())) {
            profile.addAmmoLight(6);
            profile.addAmmoHeavy(3);
        }
    }

    private static double supportMultiplier(MorphProfile profile) {
        return profile.buildBehaviorState().supportEffectMultiplier();
    }

    private static int scaleSupportMorale(MorphProfile profile, int baseValue) {
        if (baseValue == 0) {
            return 0;
        }
        if (baseValue < 0) {
            return baseValue;
        }

        return Math.max(1, Mth.floor((float) (baseValue * supportMultiplier(profile))));
    }

    private static void clearNegativeEffects(ServerPlayer player) {
        List<MobEffect> effectsToRemove = new ArrayList<>();
        for (MobEffectInstance effect : player.getActiveEffects()) {
            if (effect.getEffect().getCategory() == MobEffectCategory.HARMFUL) {
                effectsToRemove.add(effect.getEffect());
            }
        }

        for (MobEffect effect : effectsToRemove) {
            player.removeEffect(effect);
        }
    }

    private static void applyEffect(ServerPlayer player, MobEffectInstance effect) {
        MobEffect mobEffect = effect.getEffect();
        if (mobEffect.isInstantenous()) {
            mobEffect.applyInstantenousEffect(player, player, player, effect.getAmplifier(), 1.0D);
            return;
        }

        player.addEffect(new MobEffectInstance(effect));
    }

    private static void consumeHeldItem(ServerPlayer player, InteractionHand hand, int amount) {
        if (player.getAbilities().instabuild) {
            return;
        }

        ItemStack heldStack = player.getItemInHand(hand);
        if (heldStack.isEmpty()) {
            return;
        }

        heldStack.shrink(amount);
        if (heldStack.isEmpty()) {
            player.setItemInHand(hand, ItemStack.EMPTY);
        }
    }

    private static void syncAndNotify(ServerPlayer player, MorphProfile profile) {
        TeitokuHelper.syncGameplayState(player);
        player.displayClientMessage(Component.literal("Morph ")
                .append(profile.getSpec().displayName().copy().withStyle(ChatFormatting.LIGHT_PURPLE))
                .append(Component.literal(" updated")), true);
    }

    private static @Nullable MorphSupportAction resolveSupportAction(ItemStack stack, ServerPlayer player) {
        if (stack.isEmpty()) {
            return null;
        }

        if (stack.is(ModItems.GRUDGE.get())) {
            return new MorphSupportAction(240, 0.0F, false, false,
                    List.of(new MobEffectInstance(net.minecraft.world.effect.MobEffects.DAMAGE_BOOST, 20 * 25, 0)));
        }
        if (stack.is(ModItems.GRUDGE1.get())) {
            return new MorphSupportAction(420, 0.0F, false, false,
                    List.of(new MobEffectInstance(net.minecraft.world.effect.MobEffects.DAMAGE_BOOST, 20 * 40, 1)));
        }
        if (stack.is(ModItems.AMMO.get())) {
            return new MorphSupportAction(140, 0.0F, false, false,
                    List.of(new MobEffectInstance(net.minecraft.world.effect.MobEffects.LUCK, 20 * 25, 0)));
        }
        if (stack.is(ModItems.AMMO1.get())) {
            return new MorphSupportAction(220, 0.0F, false, false,
                    List.of(new MobEffectInstance(net.minecraft.world.effect.MobEffects.LUCK, 20 * 40, 0)));
        }
        if (stack.is(ModItems.AMMO2.get())) {
            return new MorphSupportAction(180, 0.0F, false, false,
                    List.of(new MobEffectInstance(net.minecraft.world.effect.MobEffects.LUCK, 20 * 30, 1)));
        }
        if (stack.is(ModItems.AMMO3.get())) {
            return new MorphSupportAction(280, 0.0F, false, false,
                    List.of(new MobEffectInstance(net.minecraft.world.effect.MobEffects.LUCK, 20 * 50, 1)));
        }
        if (stack.is(ModItems.ABYSSMETAL.get())) {
            return new MorphSupportAction(180, 0.08F, false, false, List.of());
        }
        if (stack.is(ModItems.ABYSSMETAL1.get())) {
            return new MorphSupportAction(220, 0.04F, false, false,
                    List.of(new MobEffectInstance(net.minecraft.world.effect.MobEffects.ABSORPTION, 20 * 45, 0)));
        }
        if (stack.is(ModItems.TOYAIRPLANE.get())) {
            return new MorphSupportAction(360, 0.0F, false, false,
                    List.of(new MobEffectInstance(net.minecraft.world.effect.MobEffects.LUCK, 20 * 50, 1),
                            new MobEffectInstance(net.minecraft.world.effect.MobEffects.MOVEMENT_SPEED, 20 * 40, 0)));
        }
        if (stack.is(Items.MILK_BUCKET)) {
            return new MorphSupportAction(80, 0.02F, true, false, List.of());
        }
        if (stack.is(Items.HONEY_BOTTLE)) {
            return new MorphSupportAction(120, 0.01F, false, true, List.of());
        }
        if (stack.is(Items.POTION) || stack.is(Items.SPLASH_POTION) || stack.is(Items.LINGERING_POTION)) {
            List<MobEffectInstance> effects = copyEffectList(PotionUtils.getMobEffects(stack));
            if (effects.isEmpty()) {
                return null;
            }

            int beneficialEffects = 0;
            int harmfulEffects = 0;
            for (MobEffectInstance effect : effects) {
                MobEffectCategory category = effect.getEffect().getCategory();
                if (category == MobEffectCategory.BENEFICIAL) {
                    beneficialEffects++;
                } else if (category == MobEffectCategory.HARMFUL) {
                    harmfulEffects++;
                }
            }

            int moraleGain = beneficialEffects * 80 - harmfulEffects * 60;
            return new MorphSupportAction(moraleGain, 0.0F, false, false, effects);
        }

        FoodProperties foodProperties = stack.getFoodProperties(player);
        if (foodProperties == null) {
            return null;
        }

        List<MobEffectInstance> foodEffects = new ArrayList<>();
        for (Pair<MobEffectInstance, Float> effectEntry : foodProperties.getEffects()) {
            if (player.getRandom().nextFloat() <= effectEntry.getSecond()) {
                foodEffects.add(new MobEffectInstance(effectEntry.getFirst()));
            }
        }

        int moraleGain = Math.max(70, Math.round(foodProperties.getNutrition() * 18.0F
                + foodProperties.getSaturationModifier() * 180.0F));
        float healRatio = Mth.clamp(foodProperties.getNutrition() * 0.003F
                + foodProperties.getSaturationModifier() * 0.015F, 0.01F, 0.05F);
        return new MorphSupportAction(moraleGain, healRatio, false, false, foodEffects);
    }

    private static List<MobEffectInstance> copyEffectList(List<MobEffectInstance> effects) {
        List<MobEffectInstance> copied = new ArrayList<>(effects.size());
        for (MobEffectInstance effect : effects) {
            copied.add(new MobEffectInstance(effect));
        }
        return copied;
    }

    private record MorphSupportAction(int moraleGain, float healRatio, boolean clearsNegativeStates,
                                      boolean removesPoison, List<MobEffectInstance> effects) {
    }
}
