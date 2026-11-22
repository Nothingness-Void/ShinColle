package com.lulan.shincolle.client.renderer.entity;

import com.lulan.shincolle.ShinColle;
import com.lulan.shincolle.client.model.legacy.LegacyStaticModel;
import com.lulan.shincolle.entity.mount.LegacyMountControls;
import com.lulan.shincolle.entity.mount.LegacyShipMountEntity;
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

/** Renders the original 1.12 model source for a hime ship mount. */
public final class LegacyShipMountRenderer extends EntityRenderer<LegacyShipMountEntity> {

    private static final Map<Integer, String> MODEL_NAMES = Map.of(
            LegacyMountControls.STYLE_AIRFIELD, "ModelMountAfH",
            LegacyMountControls.STYLE_BATTLESHIP, "ModelMountBaH",
            LegacyMountControls.STYLE_CARRIER, "ModelMountCaH",
            LegacyMountControls.STYLE_CARRIER_WD, "ModelMountCaWD",
            LegacyMountControls.STYLE_HARBOUR, "ModelMountHbH",
            LegacyMountControls.STYLE_ISOLATED, "ModelMountIsH",
            LegacyMountControls.STYLE_MIDWAY, "ModelMountMiH",
            LegacyMountControls.STYLE_SUBMARINE, "ModelMountSuH");
    private static final Map<Integer, ResourceLocation> TEXTURES = Map.of(
            LegacyMountControls.STYLE_AIRFIELD, texture("entitymountafh.png"),
            LegacyMountControls.STYLE_BATTLESHIP, texture("entitymountbah.png"),
            LegacyMountControls.STYLE_CARRIER, texture("entitymountcah.png"),
            LegacyMountControls.STYLE_CARRIER_WD, texture("entitymountcawd.png"),
            LegacyMountControls.STYLE_HARBOUR, texture("entitymounthbh.png"),
            LegacyMountControls.STYLE_ISOLATED, texture("entitymountish.png"),
            LegacyMountControls.STYLE_MIDWAY, texture("entitymountmih.png"),
            LegacyMountControls.STYLE_SUBMARINE, texture("entitymountsuh.png"));

    private final Map<Integer, LegacyStaticModel> models = new HashMap<>();

    public LegacyShipMountRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.shadowRadius = 1.5F;
    }

    @Override
    public void render(LegacyShipMountEntity entity, float entityYaw, float partialTick,
                       PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();
        float yaw = Mth.rotLerp(partialTick, entity.yRotO, entity.getYRot());
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F - yaw));
        poseStack.scale(-1.0F, -1.0F, 1.0F);
        poseStack.translate(0.0F, -1.501F, 0.0F);

        VertexConsumer vertexConsumer = buffer.getBuffer(RenderType.entityCutoutNoCull(
                this.getTextureLocation(entity)));
        this.modelFor(entity.getMountStyle()).render(
                poseStack, vertexConsumer, packedLight, OverlayTexture.NO_OVERLAY);
        poseStack.popPose();
        super.render(entity, entityYaw, partialTick, poseStack, buffer, packedLight);
    }

    @Override
    public ResourceLocation getTextureLocation(LegacyShipMountEntity entity) {
        return TEXTURES.getOrDefault(entity.getMountStyle(),
                TEXTURES.get(LegacyMountControls.STYLE_BATTLESHIP));
    }

    @Override
    public boolean shouldRender(LegacyShipMountEntity entity, Frustum frustum,
                                double camX, double camY, double camZ) {
        return super.shouldRender(entity, frustum, camX, camY, camZ);
    }

    private LegacyStaticModel modelFor(int style) {
        int resolvedStyle = MODEL_NAMES.containsKey(style)
                ? style : LegacyMountControls.STYLE_BATTLESHIP;
        return this.models.computeIfAbsent(resolvedStyle,
                key -> LegacyStaticModel.load(MODEL_NAMES.get(key),
                        ResourceLocation.fromNamespaceAndPath(ShinColle.MOD_ID,
                                "legacy_model_sources/" + MODEL_NAMES.get(key).toLowerCase() + ".java")));
    }

    private static ResourceLocation texture(String path) {
        return ResourceLocation.fromNamespaceAndPath(ShinColle.MOD_ID,
                "textures/entity/" + path);
    }
}
