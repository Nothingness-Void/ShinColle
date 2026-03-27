package com.lulan.shincolle.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.List;

public class LegacyPlaceholderItem extends Item {

    private final String tooltipKey;
    private final boolean foil;

    public LegacyPlaceholderItem(Properties properties, String tooltipKey) {
        this(properties, tooltipKey, false);
    }

    public LegacyPlaceholderItem(Properties properties, String tooltipKey, boolean foil) {
        super(properties);
        this.tooltipKey = tooltipKey;
        this.foil = foil;
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return this.foil;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable(this.tooltipKey).withStyle(ChatFormatting.GRAY));
    }
}
