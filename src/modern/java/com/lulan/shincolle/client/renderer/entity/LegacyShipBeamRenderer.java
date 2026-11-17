package com.lulan.shincolle.client.renderer.entity;

import com.lulan.shincolle.ShinColle;
import com.lulan.shincolle.entity.projectile.LegacyShipBeamEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

/** The legacy beam is rendered by its travelling particle head. */
public final class LegacyShipBeamRenderer extends EntityRenderer<LegacyShipBeamEntity> {

    private static final ResourceLocation INVISIBLE_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(ShinColle.MOD_ID,
                    "textures/entity/modelbasicentityitem.png");

    public LegacyShipBeamRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.shadowRadius = 0.0F;
    }

    @Override
    public void render(LegacyShipBeamEntity entity, float entityYaw, float partialTick,
                       PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        // EntityProjectileBeam had no model; its head particle is emitted by the entity on the client.
    }

    @Override
    public ResourceLocation getTextureLocation(LegacyShipBeamEntity entity) {
        return INVISIBLE_TEXTURE;
    }
}
