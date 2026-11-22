package com.lulan.shincolle.client.model.legacy.compat;

public class BasicEntityMount extends Entity {
    public float prevLimbSwingAmount, limbSwingAmount, limbSwing;
    public BasicEntityMount(net.minecraft.world.entity.Entity modern) {
        super(modern);
        net.minecraft.world.entity.WalkAnimationState animation = null;
        if (modern instanceof net.minecraft.world.entity.LivingEntity living) animation = living.walkAnimation;
        if (modern instanceof com.lulan.shincolle.entity.mount.LegacyShipMountEntity mount) animation = mount.getLegacyModelWalk();
        if (animation != null) {
            prevLimbSwingAmount = animation.speed(0F);
            limbSwingAmount = animation.speed();
            limbSwing = animation.position();
        }
    }
}
