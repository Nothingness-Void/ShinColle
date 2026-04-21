package com.lulan.shincolle.item;

import com.lulan.shincolle.morph.MorphHelper;
import com.lulan.shincolle.morph.MorphProfile;
import com.lulan.shincolle.sound.ShipSoundType;
import com.lulan.shincolle.sound.ShinColleSoundHelper;
import com.lulan.shincolle.teitoku.TeitokuHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
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

public class ModernKitItem extends Item {

    public ModernKitItem(Properties properties) {
        super(properties);
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
        return 80;
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity livingEntity) {
        if (!level.isClientSide() && livingEntity instanceof ServerPlayer player) {
            MorphProfile profile = MorphHelper.getActiveProfile(player);
            if (profile != null && profile.addRandomModernization(player.getRandom())) {
                if (!player.getAbilities().instabuild) {
                    stack.shrink(1);
                }

                TeitokuHelper.syncGameplayState(player);
                ShinColleSoundHelper.playShipVoice(level, player, ShipSoundType.MARRY, 0.65F,
                        ShinColleSoundHelper.variedPitch(player, 1.0F, 0.08F));
            }
        }

        return stack;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("gui.shincolle.modernkit").withStyle(ChatFormatting.GOLD));
        tooltip.add(Component.translatable("gui.shincolle.modernkit.effect").withStyle(ChatFormatting.AQUA));
    }
}
