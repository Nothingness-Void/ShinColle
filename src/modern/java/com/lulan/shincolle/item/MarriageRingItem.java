package com.lulan.shincolle.item;

import com.lulan.shincolle.sound.ShipSoundType;
import com.lulan.shincolle.sound.ShinColleSoundHelper;
import com.lulan.shincolle.teitoku.TeitokuHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.List;

public class MarriageRingItem extends Item {

    private static final String ACTIVE_TAG = "Active";

    public MarriageRingItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (!level.isClientSide()) {
            boolean active = !isActive(stack);
            stack.getOrCreateTag().putBoolean(ACTIVE_TAG, active);
            if (player instanceof ServerPlayer serverPlayer) {
                TeitokuHelper.markRingState(serverPlayer, active);
            }
            ShinColleSoundHelper.playShipVoice(level, player, active ? ShipSoundType.MARRY : ShipSoundType.PICKITEM,
                    active ? 0.8F : 0.55F,
                    ShinColleSoundHelper.variedPitch(player, 1.0F, 0.08F));
            player.displayClientMessage(Component.translatable(active
                    ? "chat.shincolle.marriagering.enabled"
                    : "chat.shincolle.marriagering.disabled"), true);
        }

        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return isActive(stack);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable(isActive(stack)
                ? "gui.shincolle.marriagering.active"
                : "gui.shincolle.marriagering.inactive").withStyle(ChatFormatting.AQUA));
        tooltip.add(Component.translatable("gui.shincolle.marriagering.use").withStyle(ChatFormatting.DARK_GRAY));
    }

    public static boolean isActive(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        return tag != null && tag.getBoolean(ACTIVE_TAG);
    }
}
