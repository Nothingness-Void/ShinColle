package com.lulan.shincolle.client.renderer.entity;

import com.lulan.shincolle.ShinColle;
import com.lulan.shincolle.client.model.legacy.LegacyStaticModel;
import com.lulan.shincolle.entity.projectile.LegacyShipProjectileVisual;
import com.lulan.shincolle.entity.ship.LegacyShipAircraftEntity;
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

import java.util.EnumMap;
import java.util.Map;

public class LegacyShipAircraftRenderer extends EntityRenderer<LegacyShipAircraftEntity> {

    private static final ResourceLocation MODEL_AIRPLANE_ZERO = ResourceLocation.fromNamespaceAndPath(
            ShinColle.MOD_ID, "legacy_model_sources/modelairplanezero.java");
    private static final ResourceLocation MODEL_AIRPLANE_T = ResourceLocation.fromNamespaceAndPath(
            ShinColle.MOD_ID, "legacy_model_sources/modelairplanet.java");
    private static final ResourceLocation MODEL_TAKOYAKI = ResourceLocation.fromNamespaceAndPath(
            ShinColle.MOD_ID, "legacy_model_sources/modeltakoyaki.java");
    private static final ResourceLocation TEXTURE_AIRPLANE_ZERO = ResourceLocation.fromNamespaceAndPath(
            ShinColle.MOD_ID, "textures/entity/entityairplanezero.png");
    private static final ResourceLocation TEXTURE_AIRPLANE_T = ResourceLocation.fromNamespaceAndPath(
            ShinColle.MOD_ID, "textures/entity/entityairplanet.png");
    private static final ResourceLocation TEXTURE_TAKOYAKI = ResourceLocation.fromNamespaceAndPath(
            ShinColle.MOD_ID, "textures/entity/entityaircrafttakoyaki.png");

    private final Map<LegacyShipProjectileVisual, LegacyStaticModel> models =
            new EnumMap<>(LegacyShipProjectileVisual.class);

    public LegacyShipAircraftRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.models.put(LegacyShipProjectileVisual.AIRPLANE,
                LegacyStaticModel.load("ModelAirplaneZero", MODEL_AIRPLANE_ZERO));
        this.models.put(LegacyShipProjectileVisual.TORPEDO,
                LegacyStaticModel.load("ModelAirplaneT", MODEL_AIRPLANE_T));
        this.models.put(LegacyShipProjectileVisual.BOMB,
                LegacyStaticModel.load("ModelTakoyaki", MODEL_TAKOYAKI));
        this.shadowRadius = 0.5F;
    }

    @Override
    public void render(LegacyShipAircraftEntity entity, float entityYaw, float partialTick,
                       PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        LegacyShipProjectileVisual visual = entity.getProjectileVisual();
        this.shadowRadius = switch (visual) {
            case TORPEDO, BOMB -> 0.7F;
            default -> 0.5F;
        };

        poseStack.pushPose();
        this.alignToFlight(entity, poseStack);
        float renderScale = entity.renderScale();
        poseStack.scale(renderScale, renderScale, renderScale);

        VertexConsumer vertexConsumer = buffer.getBuffer(RenderType.entityCutoutNoCull(this.getTextureLocation(entity)));
        this.modelFor(visual).render(poseStack, vertexConsumer, packedLight, OverlayTexture.NO_OVERLAY);
        poseStack.popPose();
        super.render(entity, entityYaw, partialTick, poseStack, buffer, packedLight);
    }

    @Override
    public ResourceLocation getTextureLocation(LegacyShipAircraftEntity entity) {
        return textureFor(entity.getProjectileVisual());
    }

    private LegacyStaticModel modelFor(LegacyShipProjectileVisual visual) {
        return switch (visual) {
            case TORPEDO -> this.models.get(LegacyShipProjectileVisual.TORPEDO);
            case BOMB -> this.models.get(LegacyShipProjectileVisual.BOMB);
            default -> this.models.get(LegacyShipProjectileVisual.AIRPLANE);
        };
    }

    private static ResourceLocation textureFor(LegacyShipProjectileVisual visual) {
        return switch (visual) {
            case TORPEDO -> TEXTURE_AIRPLANE_T;
            case BOMB -> TEXTURE_TAKOYAKI;
            default -> TEXTURE_AIRPLANE_ZERO;
        };
    }

    private void alignToFlight(LegacyShipAircraftEntity entity, PoseStack poseStack) {
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
