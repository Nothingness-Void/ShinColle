package com.lulan.shincolle.client.renderer.entity;

import com.lulan.shincolle.entity.other.LegacyShipFishingHookEntity;
import com.lulan.shincolle.entity.ship.LegacyShipEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

/** Port of RenderShipFishing's bobber quad and host tether. */
public final class LegacyShipFishingHookRenderer extends EntityRenderer<LegacyShipFishingHookEntity> {

    private static final ResourceLocation FISH_PARTICLES =
            ResourceLocation.withDefaultNamespace("textures/particle/particles.png");

    public LegacyShipFishingHookRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.shadowRadius = 0.0F;
    }

    @Override
    public void render(LegacyShipFishingHookEntity entity, float entityYaw, float partialTick,
                       PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        float bob = Mth.cos((entity.tickCount + partialTick) * 0.15F) * 0.05F - 0.25F;
        poseStack.pushPose();
        poseStack.translate(0.0D, bob + 0.25D, 0.0D);
        poseStack.mulPose(this.entityRenderDispatcher.cameraOrientation());
        poseStack.scale(0.5F, 0.5F, 0.5F);

        VertexConsumer quad = buffer.getBuffer(RenderType.entityCutoutNoCull(FISH_PARTICLES));
        PoseStack.Pose pose = poseStack.last();
        quad.vertex(pose.pose(), -0.5F, -0.5F, 0.0F)
                .color(255, 255, 255, 255).uv(0.0625F, 0.1875F)
                .overlayCoords(OverlayTexture.NO_OVERLAY).uv2(packedLight)
                .normal(pose.normal(), 0.0F, 1.0F, 0.0F).endVertex();
        quad.vertex(pose.pose(), 0.5F, -0.5F, 0.0F)
                .color(255, 255, 255, 255).uv(0.125F, 0.1875F)
                .overlayCoords(OverlayTexture.NO_OVERLAY).uv2(packedLight)
                .normal(pose.normal(), 0.0F, 1.0F, 0.0F).endVertex();
        quad.vertex(pose.pose(), 0.5F, 0.5F, 0.0F)
                .color(255, 255, 255, 255).uv(0.125F, 0.125F)
                .overlayCoords(OverlayTexture.NO_OVERLAY).uv2(packedLight)
                .normal(pose.normal(), 0.0F, 1.0F, 0.0F).endVertex();
        quad.vertex(pose.pose(), -0.5F, 0.5F, 0.0F)
                .color(255, 255, 255, 255).uv(0.0625F, 0.125F)
                .overlayCoords(OverlayTexture.NO_OVERLAY).uv2(packedLight)
                .normal(pose.normal(), 0.0F, 1.0F, 0.0F).endVertex();
        poseStack.popPose();

        LegacyShipEntity host = entity.getHostShip();
        if (host != null && !host.isRemoved()) {
            this.renderTether(entity, host, poseStack, buffer, partialTick, bob);
        }
        super.render(entity, entityYaw, partialTick, poseStack, buffer, packedLight);
    }

    private void renderTether(LegacyShipFishingHookEntity entity, LegacyShipEntity host,
                              PoseStack poseStack, MultiBufferSource buffer,
                              float partialTick, float bob) {
        double dx = host.getX() - entity.getX();
        double dy = host.getY() + host.getBbHeight() * 0.75D
                - (entity.getY() + bob + 0.25D);
        double dz = host.getZ() - entity.getZ();
        VertexConsumer line = buffer.getBuffer(RenderType.lines());
        PoseStack.Pose pose = poseStack.last();
        for (int segment = 0; segment <= 16; segment++) {
            float t = segment / 16.0F;
            float curve = t * t * 0.5F + t * 0.5F;
            line.vertex(pose.pose(), (float) (dx * t), (float) (dy * curve), (float) (dz * t))
                    .color(200, 200, 200, 255)
                    .normal(pose.normal(), 0.0F, 1.0F, 0.0F)
                    .endVertex();
        }
    }

    @Override
    public ResourceLocation getTextureLocation(LegacyShipFishingHookEntity entity) {
        return FISH_PARTICLES;
    }
}
