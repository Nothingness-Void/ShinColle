package com.lulan.shincolle.item;

import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.resources.RegistryOps;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionContents;

import java.util.ArrayList;
import java.util.List;

public final class LegacyPotionData {
    private LegacyPotionData() {
    }

    public static ItemStack setPotion(ItemStack stack, Holder<Potion> potion) {
        stack.set(DataComponents.POTION_CONTENTS, new PotionContents(potion));
        return stack;
    }

    public static List<MobEffectInstance> getMobEffects(ItemStack stack) {
        return effects(stack.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY));
    }

    public static List<MobEffectInstance> getAllEffects(Entity cloud) {
        CompoundTag saved = cloud.saveWithoutId(new CompoundTag());
        return effects(PotionContents.CODEC.parse(RegistryOps.create(NbtOps.INSTANCE, cloud.registryAccess()),
                saved.getCompound("potion_contents")).result().orElse(PotionContents.EMPTY));
    }

    private static List<MobEffectInstance> effects(PotionContents contents) {
        List<MobEffectInstance> effects = new ArrayList<>();
        contents.getAllEffects().forEach(effects::add);
        return effects;
    }
}
