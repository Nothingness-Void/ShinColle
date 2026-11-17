package com.lulan.shincolle.client.renderer.entity;

import com.lulan.shincolle.ShinColle;
import com.lulan.shincolle.entity.projectile.LegacyShipStaticEffectEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

/** The legacy static field is rendered by its particle ring. */
public final class LegacyShipStaticEffectRenderer extends EntityRenderer<LegacyShipStaticEffectEntity> {

    private static final ResourceLocation INVISIBLE_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(ShinColle.MOD_ID,
                    "textures/entity/modelbasicentityitem.png");

    public LegacyShipStaticEffectRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.shadowRadius = 0.0F;
    }

    @Override
    public void render(LegacyShipStaticEffectEntity entity, float entityYaw, float partialTick,
                       PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        // EntityProjectileStatic had no model; its visual is emitted through the particle bus.
    }

    @Override
    public ResourceLocation getTextureLocation(LegacyShipStaticEffectEntity entity) {
        return INVISIBLE_TEXTURE;
    }
}
