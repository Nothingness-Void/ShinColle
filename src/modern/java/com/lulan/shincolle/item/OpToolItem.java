package com.lulan.shincolle.item;

import com.lulan.shincolle.entity.mount.LegacyMountEntity;
import com.lulan.shincolle.entity.mount.LegacyShipMountEntity;
import com.lulan.shincolle.entity.ship.LegacyShipAircraftEntity;
import com.lulan.shincolle.entity.ship.LegacyShipEntity;
import com.lulan.shincolle.entity.ship.LegacyShipRensouhouEntity;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.List;

public class OpToolItem extends Item {

    public static boolean isExcludedTarget(Entity entity) {
        return entity instanceof LegacyShipEntity || entity instanceof LegacyMountEntity
                || entity instanceof LegacyShipMountEntity || entity instanceof LegacyShipAircraftEntity
                || entity instanceof LegacyShipRensouhouEntity;
    }

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

}
