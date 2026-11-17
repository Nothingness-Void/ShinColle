package com.lulan.shincolle.client.renderer.entity;

import com.lulan.shincolle.ShinColle;
import com.lulan.shincolle.client.model.legacy.LegacyStaticModel;
import com.lulan.shincolle.entity.ship.LegacyShipRensouhouEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

public class LegacyShipRensouhouRenderer extends EntityRenderer<LegacyShipRensouhouEntity> {

    private static final ResourceLocation MODEL_RENSOUHOU = ResourceLocation.fromNamespaceAndPath(
            ShinColle.MOD_ID, "legacy_model_sources/modelrensouhou.java");
    private static final ResourceLocation MODEL_RENSOUHOU_S = ResourceLocation.fromNamespaceAndPath(
            ShinColle.MOD_ID, "legacy_model_sources/modelrensouhous.java");
    private static final ResourceLocation TEXTURE_RENSOUHOU = ResourceLocation.fromNamespaceAndPath(
            ShinColle.MOD_ID, "textures/entity/entityrensouhou.png");
    private static final ResourceLocation TEXTURE_RENSOUHOU_S = ResourceLocation.fromNamespaceAndPath(
            ShinColle.MOD_ID, "textures/entity/entityrensouhous.png");

    private final LegacyStaticModel normalModel;
    private final LegacyStaticModel largeModel;

    public LegacyShipRensouhouRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.normalModel = LegacyStaticModel.load("ModelRensouhou", MODEL_RENSOUHOU);
        this.largeModel = LegacyStaticModel.load("ModelRensouhouS", MODEL_RENSOUHOU_S);
        this.shadowRadius = 0.7F;
    }

    @Override
    public void render(LegacyShipRensouhouEntity entity, float entityYaw, float partialTick,
                       PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();
        float yaw = Mth.rotLerp(partialTick, entity.yRotO, entity.getYRot());
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F - yaw));

        boolean large = entity.isLargeModel() && !entity.isHostileVariant();
        if (large) {
            poseStack.scale(0.4F, 0.4F, 0.4F);
            poseStack.translate(0.0D, 0.75D, 0.0D);
        } else {
            float scale = normalScale(entity.getHostileScaleLevel());
            float offsetY = normalOffsetY(entity.getHostileScaleLevel());
            poseStack.scale(scale, scale, scale);
            poseStack.translate(0.0D, offsetY, 0.0D);
        }

        VertexConsumer vertexConsumer = buffer.getBuffer(RenderType.entityCutoutNoCull(this.getTextureLocation(entity)));
        (large ? this.largeModel : this.normalModel).renderRaw(
                poseStack, vertexConsumer, packedLight, OverlayTexture.NO_OVERLAY);
        poseStack.popPose();
        super.render(entity, entityYaw, partialTick, poseStack, buffer, packedLight);
    }

    @Override
    public ResourceLocation getTextureLocation(LegacyShipRensouhouEntity entity) {
        return entity.isLargeModel() && !entity.isHostileVariant()
                ? TEXTURE_RENSOUHOU_S : TEXTURE_RENSOUHOU;
    }

    @Override
    public boolean shouldRender(LegacyShipRensouhouEntity entity, Frustum frustum,
                                double camX, double camY, double camZ) {
        return super.shouldRender(entity, frustum, camX, camY, camZ);
    }

    private static float normalScale(int hostileScaleLevel) {
        return switch (Mth.clamp(hostileScaleLevel, 0, 3)) {
            case 3 -> 1.08F;
            case 2 -> 0.81F;
            case 1 -> 0.54F;
            default -> 0.27F;
        };
    }

    private static float normalOffsetY(int hostileScaleLevel) {
        return switch (Mth.clamp(hostileScaleLevel, 0, 3)) {
            case 3 -> -0.09F;
            case 2 -> 0.4F;
            case 1 -> 1.32F;
            default -> 4.09F;
        };
    }
}
