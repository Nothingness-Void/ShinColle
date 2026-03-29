package com.lulan.shincolle.client.renderer.entity;

import com.lulan.shincolle.entity.mount.LegacyMountEntity;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.client.renderer.texture.TextureAtlas;

public class LegacyMountRenderer extends EntityRenderer<LegacyMountEntity> {

    public LegacyMountRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public ResourceLocation getTextureLocation(LegacyMountEntity entity) {
        return TextureAtlas.LOCATION_BLOCKS;
    }

    @Override
    public boolean shouldRender(LegacyMountEntity entity, Frustum frustum, double camX, double camY, double camZ) {
        return false;
    }
}
