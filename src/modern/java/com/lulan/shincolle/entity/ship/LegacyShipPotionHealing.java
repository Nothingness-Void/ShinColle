package com.lulan.shincolle.entity.ship;

import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.projectile.ThrownPotion;

public final class LegacyShipPotionHealing {
    private LegacyShipPotionHealing() {}

    public static float apply(LegacyShipEntity ship, float amount) {
        float percent = Math.max(1.0F, ship.getMaxHealth() * 0.01F);
        var area = ship.getBoundingBox().expandTowards(3.0D, 3.0D, 3.0D);
        for (AreaEffectCloud cloud : ship.level().getEntitiesOfClass(AreaEffectCloud.class, area)) {
            int level = healingLevel(PotionUtils.getAllEffects(cloud.saveWithoutId(new net.minecraft.nbt.CompoundTag())));
            if (level > 0) return amount + (percent + 3.0F) * (1.0F + level * 0.5F);
        }
        for (ThrownPotion potion : ship.level().getEntitiesOfClass(ThrownPotion.class, area)) {
            int level = healingLevel(PotionUtils.getMobEffects(potion.getItem()));
            if (level > 0) return amount + (percent + 6.0F) * (1.0F + level * 0.5F);
        }
        return amount;
    }

    private static int healingLevel(Iterable<MobEffectInstance> effects) {
        for (MobEffectInstance effect : effects) {
            if (effect.getEffect() == MobEffects.HEAL || effect.getEffect() == MobEffects.REGENERATION) {
                return effect.getAmplifier() + 1;
            }
        }
        return 0;
    }
}
