package com.lulan.shincolle.item;

import com.lulan.shincolle.sound.ShipSoundType;
import com.lulan.shincolle.sound.ShinColleSoundHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.List;

public class CombatRationItem extends Item {

    private final String descriptionKey;
    private final int moraleValue;
    private final int fuelMin;
    private final int fuelMax;

    public CombatRationItem(Properties properties, String descriptionKey, int moraleValue, int fuelMin, int fuelMax) {
        super(properties);
        this.descriptionKey = descriptionKey;
        this.moraleValue = moraleValue;
        this.fuelMin = fuelMin;
        this.fuelMax = fuelMax;
    }

    public static FoodProperties rationFood(int nutrition, float saturation) {
        return new FoodProperties.Builder()
                .nutrition(nutrition)
                .saturationMod(saturation)
                .alwaysEat()
                .build();
    }

    public int getMoraleValue() {
        return this.moraleValue;
    }

    public int getFuelMin() {
        return this.fuelMin;
    }

    public int getFuelMax() {
        return this.fuelMax;
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity livingEntity) {
        ItemStack result = super.finishUsingItem(stack, level, livingEntity);

        if (!level.isClientSide() && livingEntity instanceof Player player) {
            ShinColleSoundHelper.playShipVoice(level, player, ShipSoundType.FEED, 0.65F,
                    ShinColleSoundHelper.variedPitch(player, 1.0F, 0.08F));
        }

        return result;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable(this.descriptionKey).withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.literal("+" + this.moraleValue + " ")
                .append(Component.translatable("gui.shincolle.combatration"))
                .withStyle(ChatFormatting.LIGHT_PURPLE));
        tooltip.add(Component.literal("+" + this.fuelMin + "~" + this.fuelMax + " ")
                .append(Component.translatable("item.shincolle.grudge"))
                .withStyle(ChatFormatting.RED));
        tooltip.add(Component.translatable("gui.shincolle.combatration.use").withStyle(ChatFormatting.GRAY));
    }
}
