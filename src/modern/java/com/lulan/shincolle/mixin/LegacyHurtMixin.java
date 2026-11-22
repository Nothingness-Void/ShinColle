package com.lulan.shincolle.mixin;

import com.lulan.shincolle.event.LegacyLivingHurtEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.common.NeoForge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({LivingEntity.class, Player.class})
public abstract class LegacyHurtMixin {
    // NeoForge has no post-shield, pre-armor event equivalent to Forge 1.20.1 LivingHurtEvent.
    @Inject(method = "actuallyHurt", at = @At(value = "INVOKE", target = "Ljava/util/Stack;peek()Ljava/lang/Object;", ordinal = 0), cancellable = true)
    private void shincolle$beforeArmor(DamageSource source, float amount, CallbackInfo ci) {
        LivingEntity entity = (LivingEntity) (Object) this;
        LegacyLivingHurtEvent event = new LegacyLivingHurtEvent(entity, source, amount);
        NeoForge.EVENT_BUS.post(event);
        float damage = event.isCanceled() ? 0.0F : event.getAmount();
        ((LivingDamageAccess) entity).shincolle$damageContainers().peek().setNewDamage(damage);
        if (damage <= 0.0F) {
            ci.cancel();
        }
    }
}
