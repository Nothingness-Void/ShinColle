package com.lulan.shincolle.item;

import com.lulan.shincolle.entity.ship.LegacyShipEntity;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.List;

public class KaitaiHammerItem extends Item {

    public KaitaiHammerItem(Properties properties) {
        super(properties);
    }

    @Override
    public boolean hasCraftingRemainingItem(ItemStack stack) {
        return true;
    }

    @Override
    public ItemStack getCraftingRemainingItem(ItemStack stack) {
        ItemStack remainder = stack.copy();
        remainder.setCount(1);
        remainder.setDamageValue(remainder.getDamageValue() + 1);
        return remainder.getDamageValue() >= remainder.getMaxDamage() ? ItemStack.EMPTY : remainder;
    }

    @Override
    public boolean onLeftClickEntity(ItemStack stack, Player player, Entity entity) {
        if (!(entity instanceof LegacyShipEntity ship)) {
            return false;
        }

        if (player.level().isClientSide()) {
            return true;
        }

        if (!ship.performKaitai(player, stack)) {
            player.displayClientMessage(Component.translatable("chat.shincolle.entity.owner_locked",
                    Component.literal(ship.getOwnerName()).withStyle(ChatFormatting.GOLD)), true);
        }

        return true;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        int remainingUses = stack.getMaxDamage() - stack.getDamageValue();
        tooltip.add(Component.translatable("gui.shincolle.kaitaihammer.remaining", remainingUses).withStyle(ChatFormatting.GOLD));
        tooltip.add(Component.translatable("gui.shincolle.kaitaihammer.use").withStyle(ChatFormatting.DARK_GRAY));
    }
}
