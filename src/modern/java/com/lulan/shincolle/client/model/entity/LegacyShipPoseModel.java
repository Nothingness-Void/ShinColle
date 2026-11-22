package com.lulan.shincolle.client.model.entity;

import com.lulan.shincolle.client.model.legacy.compat.Entity;
import com.lulan.shincolle.client.model.legacy.compat.LegacyRenderContext;
import com.lulan.shincolle.client.model.legacy.compat.ModelBase;
import com.lulan.shincolle.client.model.legacy.ship.LegacyShipModels;
import com.lulan.shincolle.client.model.legacy.ship.ModelMidwayHime;
import com.lulan.shincolle.client.model.legacy.ship.ModelTakoyaki;
import com.lulan.shincolle.entity.ship.LegacyShipEntity;
import com.lulan.shincolle.entity.ship.LegacyShipModelKind;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

import java.util.EnumMap;
import java.util.Map;

public class LegacyShipPoseModel extends EntityModel<LegacyShipEntity> {
    public LegacyShipPoseModel() {
        super(RenderType::entityTranslucent);
    }

    private final Map<LegacyShipModelKind, ModelBase> models = new EnumMap<>(LegacyShipModelKind.class);
    private final ModelTakoyaki takoyaki = new ModelTakoyaki();
    private LegacyShipEntity entity;
    private float limbSwing, limbSwingAmount, age, yaw, pitch;
    private MultiBufferSource buffers;

    public void setBuffers(MultiBufferSource buffers) { this.buffers = buffers; }

    public ModelBase getLegacyModel(LegacyShipEntity entity) {
        return models.computeIfAbsent(LegacyShipModelKind.forSpec(entity.getSpec()), LegacyShipModels::create);
    }

    @Override
    public void setupAnim(LegacyShipEntity entity, float limbSwing, float limbSwingAmount,
                          float age, float yaw, float pitch) {
        this.entity = entity;
        this.limbSwing = limbSwing;
        this.limbSwingAmount = limbSwingAmount;
        this.age = age;
        this.yaw = yaw;
        this.pitch = pitch;
    }

    @Override
    public void renderToBuffer(PoseStack poses, VertexConsumer vertices, int light, int overlay,
                               float red, float green, float blue, float alpha) {
        ModelBase model = getLegacyModel(entity);
        model.swingProgress = attackTime;
        model.isRiding = riding;
        model.isChild = young;
        Entity legacyEntity = Entity.wrap(entity);
        Matrix4f miscPose = model instanceof ModelMidwayHime ? new Matrix4f(poses.last().pose()) : null;
        Matrix3f miscNormal = model instanceof ModelMidwayHime ? new Matrix3f(poses.last().normal()) : null;
        LegacyRenderContext.begin(poses, vertices, light, overlay, red, green, blue, alpha);
        try {
            // RenderLivingBase applied the pose before render(), which also applies its own scaled pose.
            model.setRotationAngles(limbSwing, limbSwingAmount, age, yaw, pitch, 0.0625F, legacyEntity);
            poses.pushPose();
            try {
                model.render(legacyEntity, limbSwing, limbSwingAmount, age, yaw, pitch, 0.0625F);
            } finally {
                poses.popPose();
            }
        } finally {
            LegacyRenderContext.end();
        }
        if (model instanceof ModelMidwayHime midway && midway.shouldRenderMiscModel(0) && buffers != null) {
            Entity tako = midway.miscModelList.get(0).entity;
            poses.pushPose();
            try {
                poses.last().pose().set(miscPose);
                poses.last().normal().set(miscNormal);
                poses.translate(0D, 1.501D, 0D);
                poses.scale(-1F, -1F, 1F);
                poses.translate(tako.posX, tako.posY, tako.posZ);
                poses.scale(0.65F, 0.65F, 0.65F);
                poses.mulPose(Axis.XP.rotationDegrees(-30F));
                poses.scale(-1F, -1F, 1F);
                poses.translate(0D, -1.501D, 0D);
                VertexConsumer consumer = buffers.getBuffer(RenderType.entityTranslucent(
                        ResourceLocation.fromNamespaceAndPath("shincolle", "textures/entity/modern/entityaircrafttakoyaki.png")));
                LegacyRenderContext.begin(poses, consumer, light, overlay, red, green, blue, alpha);
                takoyaki.render(tako, limbSwing, limbSwingAmount, age, yaw, pitch, 0.0625F);
            } finally {
                LegacyRenderContext.end();
                poses.popPose();
            }
        }
    }
}
