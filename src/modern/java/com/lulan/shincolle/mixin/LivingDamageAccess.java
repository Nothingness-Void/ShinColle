package com.lulan.shincolle.mixin;

import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.common.damagesource.DamageContainer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Stack;

@Mixin(LivingEntity.class)
public interface LivingDamageAccess {
    @Accessor("damageContainers")
    Stack<DamageContainer> shincolle$damageContainers();
}
