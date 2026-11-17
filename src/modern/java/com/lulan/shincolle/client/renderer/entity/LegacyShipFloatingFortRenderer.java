package com.lulan.shincolle.client.renderer.entity;

import com.lulan.shincolle.ShinColle;
import com.lulan.shincolle.client.model.legacy.LegacyStaticModel;
import com.lulan.shincolle.entity.projectile.LegacyShipFloatingFortEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

/** Renders the legacy ModelFloatingFort and its original texture. */
public final class LegacyShipFloatingFortRenderer extends EntityRenderer<LegacyShipFloatingFortEntity> {

    private static final ResourceLocation MODEL_SOURCE = ResourceLocation.fromNamespaceAndPath(
            ShinColle.MOD_ID, "legacy_model_sources/modelfloatingfort.java");
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(
            ShinColle.MOD_ID, "textures/entity/entityfloatingfort.png");

    private final LegacyStaticModel model;

    public LegacyShipFloatingFortRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.model = LegacyStaticModel.load("ModelFloatingFort", MODEL_SOURCE);
        this.shadowRadius = 0.5F;
    }

    @Override
    public void render(LegacyShipFloatingFortEntity entity, float entityYaw, float partialTick,
                       PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();
        this.alignToFlight(entity, poseStack);
        VertexConsumer consumer = buffer.getBuffer(RenderType.entityTranslucent(TEXTURE));
        this.model.render(poseStack, consumer, packedLight, OverlayTexture.NO_OVERLAY);
        poseStack.popPose();
        super.render(entity, entityYaw, partialTick, poseStack, buffer, packedLight);
    }

    @Override
    public ResourceLocation getTextureLocation(LegacyShipFloatingFortEntity entity) {
        return TEXTURE;
    }

    private void alignToFlight(LegacyShipFloatingFortEntity entity, PoseStack poseStack) {
        Vec3 motion = entity.getDeltaMovement();
        if (motion.lengthSqr() < 1.0E-5D) {
            poseStack.mulPose(Axis.YP.rotationDegrees(entity.getYRot()));
            poseStack.mulPose(Axis.XP.rotationDegrees(-entity.getXRot()));
            return;
        }

        double horizontal = Math.sqrt(motion.x * motion.x + motion.z * motion.z);
        float yaw = (float) (Mth.atan2(motion.x, motion.z) * (180.0D / Math.PI));
        float pitch = (float) (-Mth.atan2(motion.y, horizontal) * (180.0D / Math.PI));
        poseStack.mulPose(Axis.YP.rotationDegrees(yaw));
        poseStack.mulPose(Axis.XP.rotationDegrees(pitch));
    }
}
