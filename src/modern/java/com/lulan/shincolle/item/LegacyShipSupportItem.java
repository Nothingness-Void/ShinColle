package com.lulan.shincolle.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.List;

public class LegacyShipSupportItem extends Item {

    private final String descriptionKey;
    private final String useKey;

    public LegacyShipSupportItem(Properties properties, String descriptionKey, String useKey) {
        super(properties);
        this.descriptionKey = descriptionKey;
        this.useKey = useKey;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable(this.descriptionKey).withStyle(ChatFormatting.AQUA));
        tooltip.add(Component.translatable(this.useKey).withStyle(ChatFormatting.GRAY));
    }
}
