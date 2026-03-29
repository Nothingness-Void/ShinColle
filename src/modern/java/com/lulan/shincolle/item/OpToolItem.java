package com.lulan.shincolle.item;

import com.lulan.shincolle.combat.WorldCombatRulesSavedData;
import com.lulan.shincolle.teitoku.TeitokuHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class OpToolItem extends Item {

    public OpToolItem(Properties properties) {
        super(properties);
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return true;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("gui.shincolle.optool1").withStyle(ChatFormatting.RED));
        tooltip.add(Component.translatable("gui.shincolle.optool2").withStyle(ChatFormatting.AQUA));
        tooltip.add(Component.translatable("gui.shincolle.optool3").withStyle(ChatFormatting.GRAY));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide() && player instanceof ServerPlayer serverPlayer) {
            List<String> blocked = new ArrayList<>(WorldCombatRulesSavedData.get(serverPlayer.serverLevel()).getAllUnattackableClasses());
            if (blocked.isEmpty()) {
                player.displayClientMessage(Component.literal("World unattackable list is empty"), false);
            } else {
                player.displayClientMessage(Component.literal("World unattackable: " + String.join(", ", blocked)), false);
            }
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    @Override
    public InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity interactionTarget, InteractionHand usedHand) {
        if (player.level().isClientSide() || !(player instanceof ServerPlayer serverPlayer)) {
            return InteractionResult.sidedSuccess(player.level().isClientSide());
        }

        String targetClass = TeitokuHelper.resolveTargetClass(interactionTarget);
        boolean added = WorldCombatRulesSavedData.get(serverPlayer.serverLevel()).toggleUnattackable(targetClass);
        player.displayClientMessage(Component.literal((added ? "Blocked " : "Allowed ") + targetClass), true);
        return InteractionResult.CONSUME;
    }
}
