package com.lulan.shincolle.item;

import com.lulan.shincolle.entity.ship.LegacyShipEntity;
import com.lulan.shincolle.morph.MorphHelper;
import com.lulan.shincolle.morph.MorphProfile;
import com.lulan.shincolle.sound.ShipSoundType;
import com.lulan.shincolle.sound.ShinColleSoundHelper;
import com.lulan.shincolle.teitoku.TeitokuHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class CombatRationItem extends Item {

    private final String descriptionKey;
    private final int moraleValue;
    private final int grudgeMin;
    private final int grudgeMax;
    private final boolean clearsDebuffs;

    public CombatRationItem(Properties properties, String descriptionKey, int moraleValue,
                            int grudgeMin, int grudgeMax, boolean clearsDebuffs) {
        super(properties);
        this.descriptionKey = descriptionKey;
        this.moraleValue = moraleValue;
        this.grudgeMin = grudgeMin;
        this.grudgeMax = grudgeMax;
        this.clearsDebuffs = clearsDebuffs;
    }

    public int getMoraleValue() {
        return this.moraleValue;
    }

    public int getGrudgeMin() {
        return this.grudgeMin;
    }

    public int getGrudgeMax() {
        return this.grudgeMax;
    }

    public int getFuelMin() {
        return this.grudgeMin;
    }

    public int getFuelMax() {
        return this.grudgeMax;
    }

    public boolean clearsDebuffs() {
        return this.clearsDebuffs;
    }

    public int rollGrudge(Player player) {
        return this.rollGrudge(player.getRandom());
    }

    public int rollGrudge(RandomSource random) {
        int min = Math.max(0, this.grudgeMin);
        int max = Math.max(min, this.grudgeMax);
        return min == max ? min : min + random.nextInt(max - min + 1);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (hand != InteractionHand.MAIN_HAND || MorphHelper.getActiveProfile(player) == null) {
            return InteractionResultHolder.fail(stack);
        }

        player.startUsingItem(hand);
        return InteractionResultHolder.consume(stack);
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.EAT;
    }

    @Override
    public int getUseDuration(ItemStack stack) {
        return 60;
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity livingEntity) {
        if (!level.isClientSide() && livingEntity instanceof ServerPlayer player) {
            MorphProfile profile = MorphHelper.getActiveProfile(player);
            if (profile != null) {
                profile.addMorale(this.moraleValue);
                profile.addGrudge(this.rollGrudge(player));
                if (this.clearsDebuffs) {
                    removeNegativeEffects(player);
                }

                TeitokuHelper.syncGameplayState(player);
                if (!player.getAbilities().instabuild) {
                    stack.shrink(1);
                }
            }

            ShinColleSoundHelper.playShipVoice(level, player, ShipSoundType.FEED, 0.65F,
                    ShinColleSoundHelper.variedPitch(player, 1.0F, 0.08F));
        }

        return stack;
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean selected) {
        if (level.isClientSide() || !(entity instanceof Player player) || !player.isUsingItem()
                || !ItemStack.isSameItemSameTags(player.getUseItem(), stack) || (player.tickCount & 15) != 0) {
            return;
        }

        for (LegacyShipEntity ship : level.getEntitiesOfClass(LegacyShipEntity.class,
                player.getBoundingBox().inflate(8.0D, 6.0D, 8.0D))) {
            if (ship.isHostileVariant() || !ship.canCommanderEdit(player) || ship.isOrderedToSit() || ship.isPassenger()) {
                continue;
            }

            if (player.distanceToSqr(ship) > 4.0D) {
                ship.getNavigation().moveTo(player, 0.75D);
            }

            ship.getLookControl().setLookAt(player.getX(), player.getEyeY(), player.getZ(), 50.0F, 50.0F);
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable(this.descriptionKey).withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.literal("+" + this.moraleValue + " ")
                .append(Component.translatable("gui.shincolle.combatration"))
                .withStyle(ChatFormatting.LIGHT_PURPLE));
        tooltip.add(Component.literal("+" + this.grudgeMin + "~" + this.grudgeMax + " ")
                .append(Component.translatable("item.shincolle.grudge"))
                .withStyle(ChatFormatting.RED));
        tooltip.add(Component.translatable("gui.shincolle.combatration.use").withStyle(ChatFormatting.GRAY));
    }

    private static void removeNegativeEffects(ServerPlayer player) {
        List<MobEffect> harmfulEffects = new ArrayList<>();

        for (MobEffectInstance effect : player.getActiveEffects()) {
            if (effect.getEffect().getCategory() == MobEffectCategory.HARMFUL) {
                harmfulEffects.add(effect.getEffect());
            }
        }

        for (MobEffect effect : harmfulEffects) {
            player.removeEffect(effect);
        }
    }
}
