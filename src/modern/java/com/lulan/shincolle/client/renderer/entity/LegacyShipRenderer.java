package com.lulan.shincolle.client.renderer.entity;

import com.lulan.shincolle.client.model.entity.LegacyShipModel;
import com.lulan.shincolle.entity.ship.LegacyShipEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class LegacyShipRenderer extends MobRenderer<LegacyShipEntity, LegacyShipModel> {

    public LegacyShipRenderer(EntityRendererProvider.Context context) {
        super(context, new LegacyShipModel(), 0.45F);
    }

    @Override
    public ResourceLocation getTextureLocation(LegacyShipEntity entity) {
        return entity.getSpec().textureLocation();
    }

    @Override
    protected void scale(LegacyShipEntity entity, PoseStack poseStack, float partialTickTime) {
        this.shadowRadius = entity.getSpec().archetype().shadowRadius();
    }
}
