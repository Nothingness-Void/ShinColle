package com.lulan.shincolle.client.renderer.entity;

import com.lulan.shincolle.client.model.entity.LegacyShipPoseModel;
import com.lulan.shincolle.client.model.legacy.compat.BasicEntityShip;
import com.lulan.shincolle.client.model.legacy.compat.Entity;
import com.lulan.shincolle.client.model.legacy.compat.EnumHandSide;
import com.lulan.shincolle.client.model.legacy.compat.GlStateManager;
import com.lulan.shincolle.client.model.legacy.compat.LegacyItemProperties;
import com.lulan.shincolle.client.model.legacy.compat.LegacyRenderContext;
import com.lulan.shincolle.client.model.legacy.compat.ModelRenderer;
import com.lulan.shincolle.client.model.legacy.ship.ShipModelBaseAdv;
import com.lulan.shincolle.config.ShinColleConfig;
import com.lulan.shincolle.entity.ship.LegacyShipEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public final class LayerShipHeldItem extends RenderLayer<LegacyShipEntity, LegacyShipPoseModel> {
    private final ItemInHandRenderer items;

    public LayerShipHeldItem(RenderLayerParent<LegacyShipEntity, LegacyShipPoseModel> parent, ItemInHandRenderer items) {
        super(parent);
        this.items = items;
    }

    @Override
    public void render(PoseStack poses, MultiBufferSource buffers, int light, LegacyShipEntity entity,
                       float swingTick, float swingAmount, float partialTicks, float tick, float yaw, float pitch) {
        if (!entity.canShowHeldItem()) return;
        if (!(getParentModel().getLegacyModel(entity) instanceof ShipModelBaseAdv model)) return;
        renderHeldItem(poses, buffers, light, entity, model, entity.getMainHandItem(),
                ItemDisplayContext.THIRD_PERSON_RIGHT_HAND, EnumHandSide.RIGHT);
        renderHeldItem(poses, buffers, light, entity, model, entity.getOffhandItem(),
                ItemDisplayContext.THIRD_PERSON_LEFT_HAND, EnumHandSide.LEFT);
    }

    private void renderHeldItem(PoseStack poses, MultiBufferSource buffers, int light, LegacyShipEntity entity,
                                ShipModelBaseAdv model, ItemStack stack, ItemDisplayContext type, EnumHandSide side) {
        if (stack.isEmpty()) return;
        LegacyRenderContext.begin(poses, null, light, OverlayTexture.NO_OVERLAY, 1F, 1F, 1F, 1F);
        poses.pushPose();
        try {
            if (entity.isShiftKeyDown()) GlStateManager.translate(0F, 0.2F, 0F);
            boolean isBlock = LegacyItemProperties.isBlock(stack);
            BasicEntityShip adapter = (BasicEntityShip) Entity.wrap(entity);
            ModelRenderer[] hand = model.getArmForSide(side);
            float[] offset = model.getHeldItemOffset(adapter, side, isBlock ? 1 : 0);
            float[] rotate = model.getHeldItemRotate(adapter, side, isBlock ? 1 : 0);
            float scale = model.getScale();
            if (hand == null) return;
            boolean left = side == EnumHandSide.LEFT;
            double[] settings = ShinColleConfig.scaleHeldItem();
            GlStateManager.translate((offset[0] + settings[1]) * (left ? -1F : 1F),
                    offset[1] + settings[2], offset[2] + settings[3]);
            for (ModelRenderer part : hand) part.postRender(0.0625F * scale);
            GlStateManager.scale(scale, scale, scale);
            GlStateManager.rotate(-90F + rotate[0], 1F, 0F, 0F);
            GlStateManager.rotate(180F + rotate[1], 0F, 1F, 0F);
            GlStateManager.rotate(rotate[2], 0F, 0F, 1F);
            GlStateManager.translate((float)(left ? -1 : 1) / 16F, 0.125F, -0.625F);
            if (LegacyItemProperties.isFull3D(stack)) {
                GlStateManager.scale(settings[0] * 0.5D, settings[0], settings[0]);
            } else if (isBlock) {
                GlStateManager.scale(settings[0] * 0.75D, settings[0] * 0.75D, settings[0] * 0.75D);
            } else {
                GlStateManager.scale(settings[0], settings[0], settings[0]);
            }
            items.renderItem(entity, stack, type, left, poses, buffers, light);
        } finally {
            poses.popPose();
            LegacyRenderContext.end();
        }
    }
}
