package com.lulan.shincolle.client.renderer.entity;

import com.lulan.shincolle.ShinColle;
import com.lulan.shincolle.client.model.legacy.LegacyStaticModel;
import com.lulan.shincolle.entity.mount.LegacyMountEntity;
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

import java.util.HashMap;
import java.util.Map;

public class LegacyMountRenderer extends EntityRenderer<LegacyMountEntity> {

    private static final Map<Integer, String> MODEL_NAMES = Map.of(
            LegacyMountEntity.STYLE_AIRFIELD, "ModelMountAfH",
            LegacyMountEntity.STYLE_BATTLESHIP, "ModelMountBaH",
            LegacyMountEntity.STYLE_CARRIER, "ModelMountCaH",
            LegacyMountEntity.STYLE_CARRIER_WD, "ModelMountCaWD",
            LegacyMountEntity.STYLE_HARBOUR, "ModelMountHbH",
            LegacyMountEntity.STYLE_ISOLATED, "ModelMountIsH",
            LegacyMountEntity.STYLE_MIDWAY, "ModelMountMiH",
            LegacyMountEntity.STYLE_SUBMARINE, "ModelMountSuH");
    private static final Map<Integer, ResourceLocation> TEXTURES = Map.of(
            LegacyMountEntity.STYLE_AIRFIELD, texture("entitymountafh.png"),
            LegacyMountEntity.STYLE_BATTLESHIP, texture("entitymountbah.png"),
            LegacyMountEntity.STYLE_CARRIER, texture("entitymountcah.png"),
            LegacyMountEntity.STYLE_CARRIER_WD, texture("entitymountcawd.png"),
            LegacyMountEntity.STYLE_HARBOUR, texture("entitymounthbh.png"),
            LegacyMountEntity.STYLE_ISOLATED, texture("entitymountish.png"),
            LegacyMountEntity.STYLE_MIDWAY, texture("entitymountmih.png"),
            LegacyMountEntity.STYLE_SUBMARINE, texture("entitymountsuh.png"));

    private final Map<Integer, LegacyStaticModel> models = new HashMap<>();

    public LegacyMountRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.shadowRadius = 1.5F;
    }

    @Override
    public void render(LegacyMountEntity entity, float entityYaw, float partialTick,
                       PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();
        float yaw = Mth.rotLerp(partialTick, entity.yRotO, entity.getYRot());
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F - yaw));

        VertexConsumer vertexConsumer = buffer.getBuffer(RenderType.entityCutoutNoCull(this.getTextureLocation(entity)));
        this.modelFor(entity.getMountStyle()).render(poseStack, vertexConsumer, packedLight, OverlayTexture.NO_OVERLAY);
        poseStack.popPose();
        super.render(entity, entityYaw, partialTick, poseStack, buffer, packedLight);
    }

    @Override
    public ResourceLocation getTextureLocation(LegacyMountEntity entity) {
        return TEXTURES.getOrDefault(entity.getMountStyle(), TEXTURES.get(LegacyMountEntity.STYLE_BATTLESHIP));
    }

    @Override
    public boolean shouldRender(LegacyMountEntity entity, Frustum frustum, double camX, double camY, double camZ) {
        return super.shouldRender(entity, frustum, camX, camY, camZ);
    }

    private LegacyStaticModel modelFor(int style) {
        int resolvedStyle = MODEL_NAMES.containsKey(style) ? style : LegacyMountEntity.STYLE_BATTLESHIP;
        return this.models.computeIfAbsent(resolvedStyle,
                key -> LegacyStaticModel.load(MODEL_NAMES.get(key),
                        ResourceLocation.fromNamespaceAndPath(ShinColle.MOD_ID,
                                "legacy_model_sources/" + MODEL_NAMES.get(key).toLowerCase() + ".java")));
    }

    private static ResourceLocation texture(String path) {
        return ResourceLocation.fromNamespaceAndPath(ShinColle.MOD_ID, "textures/entity/" + path);
    }
}
