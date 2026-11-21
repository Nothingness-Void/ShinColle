package com.lulan.shincolle.client.renderer.entity;

import com.lulan.shincolle.client.model.entity.LegacyShipPoseModel;
import com.lulan.shincolle.entity.ship.LegacyShipEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.resources.ResourceLocation;

public class LegacyShipRenderer extends MobRenderer<LegacyShipEntity, LegacyShipPoseModel> {

    public LegacyShipRenderer(EntityRendererProvider.Context context) {
        super(context, new LegacyShipPoseModel(), 0.7F);
    }

    @Override
    public void render(LegacyShipEntity entity, float yaw, float partialTick, PoseStack poses,
                       MultiBufferSource buffers, int light) {
        this.model.setBuffers(buffers);
        try {
            super.render(entity, yaw, partialTick, poses, buffers, light);
        } finally {
            this.model.setBuffers(null);
        }
    }

    @Override
    public ResourceLocation getTextureLocation(LegacyShipEntity entity) {
        return LegacyShipRenderCatalog.forSpec(entity.getSpec()).textureLocation();
    }

    @Override
    protected void scale(LegacyShipEntity entity, PoseStack poseStack, float partialTickTime) {
        this.shadowRadius = LegacyShipRenderCatalog.forSpec(entity.getSpec()).shadowRadius()
                + entity.getLegacyHostileScaleLevel() * 0.4F;
    }
}
