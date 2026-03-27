package com.lulan.shincolle.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.List;

public class LegacyUseFeedbackItem extends Item {

    @Nullable
    private final String descriptionKey;
    private final String pendingKey;
    private final UseAnim useAnimation;
    private final int useDuration;

    public LegacyUseFeedbackItem(Properties properties, @Nullable String descriptionKey, String pendingKey, UseAnim useAnimation, int useDuration) {
        super(properties);
        this.descriptionKey = descriptionKey;
        this.pendingKey = pendingKey;
        this.useAnimation = useAnimation;
        this.useDuration = useDuration;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        player.startUsingItem(hand);
        return InteractionResultHolder.consume(stack);
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return this.useAnimation;
    }

    @Override
    public int getUseDuration(ItemStack stack) {
        return this.useDuration;
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity livingEntity) {
        if (!level.isClientSide() && livingEntity instanceof Player player) {
            player.displayClientMessage(Component.translatable(this.pendingKey), true);
        }

        return stack;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        if (this.descriptionKey != null) {
            tooltip.add(Component.translatable(this.descriptionKey).withStyle(ChatFormatting.GOLD));
        }

        tooltip.add(Component.translatable(this.pendingKey).withStyle(ChatFormatting.DARK_GRAY));
    }
}
