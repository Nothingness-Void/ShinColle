package com.lulan.shincolle.client.renderer.entity;

import com.lulan.shincolle.entity.projectile.LegacyShipProjectileEntity;
import com.lulan.shincolle.entity.projectile.LegacyShipProjectileVisual;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

public class LegacyShipProjectileRenderer extends EntityRenderer<LegacyShipProjectileEntity> {

    private final ProjectileModel missileModel = ProjectileModel.createMissile();
    private final ProjectileModel airplaneModel = ProjectileModel.createAirplane();
    private final ProjectileModel bombModel = ProjectileModel.createBomb();
    private final ProjectileModel torpedoModel = ProjectileModel.createTorpedo();

    public LegacyShipProjectileRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.shadowRadius = 0.1F;
    }

    @Override
    public void render(LegacyShipProjectileEntity entity, float entityYaw, float partialTick,
                       PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();
        this.alignToFlight(entity, poseStack);

        LegacyShipProjectileVisual visual = entity.getProjectileVisual();
        ProjectileModel model = this.modelFor(visual);
        poseStack.scale(visual.renderScale(), visual.renderScale(), visual.renderScale());

        VertexConsumer vertexConsumer = buffer.getBuffer(RenderType.entityCutoutNoCull(this.getTextureLocation(entity)));
        model.renderToBuffer(poseStack, vertexConsumer, packedLight, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
        poseStack.popPose();
        super.render(entity, entityYaw, partialTick, poseStack, buffer, packedLight);
    }

    @Override
    public ResourceLocation getTextureLocation(LegacyShipProjectileEntity entity) {
        return entity.getProjectileVisual().textureLocation();
    }

    private ProjectileModel modelFor(LegacyShipProjectileVisual visual) {
        return switch (visual) {
            case MISSILE -> this.missileModel;
            case AIRPLANE -> this.airplaneModel;
            case BOMB -> this.bombModel;
            case TORPEDO, NONE -> this.torpedoModel;
        };
    }

    private void alignToFlight(LegacyShipProjectileEntity entity, PoseStack poseStack) {
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

    private static final class ProjectileModel extends Model {

        private final ModelPart root;

        private ProjectileModel(ModelPart root) {
            super(RenderType::entityCutoutNoCull);
            this.root = root;
        }

        private static ProjectileModel createMissile() {
            MeshDefinition meshDefinition = new MeshDefinition();
            PartDefinition root = meshDefinition.getRoot();
            root.addOrReplaceChild("body",
                    CubeListBuilder.create()
                            .texOffs(0, 0)
                            .addBox(-2.0F, -2.0F, -6.0F, 4.0F, 4.0F, 12.0F, new CubeDeformation(0.0F))
                            .texOffs(0, 16)
                            .addBox(-1.0F, -1.0F, 6.0F, 2.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
                            .texOffs(16, 16)
                            .addBox(-0.5F, -4.0F, 5.5F, 1.0F, 8.0F, 3.0F, new CubeDeformation(0.0F))
                            .texOffs(0, 24)
                            .addBox(-4.0F, -0.5F, 5.5F, 8.0F, 1.0F, 3.0F, new CubeDeformation(0.0F)),
                    PartPose.ZERO);
            return new ProjectileModel(LayerDefinition.create(meshDefinition, 32, 32).bakeRoot());
        }

        private static ProjectileModel createAirplane() {
            MeshDefinition meshDefinition = new MeshDefinition();
            PartDefinition root = meshDefinition.getRoot();
            root.addOrReplaceChild("airplane",
                    CubeListBuilder.create()
                            .texOffs(0, 0)
                            .addBox(-1.5F, -1.5F, -6.0F, 3.0F, 3.0F, 12.0F, new CubeDeformation(0.0F))
                            .texOffs(0, 15)
                            .addBox(-7.0F, -0.5F, -1.0F, 14.0F, 1.0F, 4.0F, new CubeDeformation(0.0F))
                            .texOffs(0, 20)
                            .addBox(-1.0F, -3.0F, 4.0F, 2.0F, 6.0F, 2.0F, new CubeDeformation(0.0F))
                            .texOffs(20, 0)
                            .addBox(-2.0F, -2.0F, -8.0F, 4.0F, 4.0F, 2.0F, new CubeDeformation(0.0F)),
                    PartPose.ZERO);
            return new ProjectileModel(LayerDefinition.create(meshDefinition, 32, 32).bakeRoot());
        }

        private static ProjectileModel createBomb() {
            MeshDefinition meshDefinition = new MeshDefinition();
            PartDefinition root = meshDefinition.getRoot();
            root.addOrReplaceChild("bomb",
                    CubeListBuilder.create()
                            .texOffs(0, 0)
                            .addBox(-2.5F, -2.5F, -4.0F, 5.0F, 5.0F, 8.0F, new CubeDeformation(0.0F))
                            .texOffs(0, 13)
                            .addBox(-0.5F, -4.5F, 2.5F, 1.0F, 4.0F, 2.0F, new CubeDeformation(0.0F))
                            .texOffs(6, 13)
                            .addBox(-4.5F, -0.5F, 2.5F, 4.0F, 1.0F, 2.0F, new CubeDeformation(0.0F))
                            .texOffs(6, 16)
                            .addBox(0.5F, -0.5F, 2.5F, 4.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)),
                    PartPose.ZERO);
            return new ProjectileModel(LayerDefinition.create(meshDefinition, 32, 32).bakeRoot());
        }

        private static ProjectileModel createTorpedo() {
            MeshDefinition meshDefinition = new MeshDefinition();
            PartDefinition root = meshDefinition.getRoot();
            root.addOrReplaceChild("torpedo",
                    CubeListBuilder.create()
                            .texOffs(0, 0)
                            .addBox(-1.5F, -1.5F, -7.0F, 3.0F, 3.0F, 14.0F, new CubeDeformation(0.0F))
                            .texOffs(0, 17)
                            .addBox(-0.5F, -0.5F, 7.0F, 1.0F, 1.0F, 3.0F, new CubeDeformation(0.0F))
                            .texOffs(10, 17)
                            .addBox(-3.5F, -0.25F, 5.5F, 7.0F, 0.5F, 2.0F, new CubeDeformation(0.0F))
                            .texOffs(10, 20)
                            .addBox(-0.25F, -3.5F, 5.5F, 0.5F, 7.0F, 2.0F, new CubeDeformation(0.0F)),
                    PartPose.ZERO);
            return new ProjectileModel(LayerDefinition.create(meshDefinition, 32, 32).bakeRoot());
        }

        @Override
        public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay,
                                   float red, float green, float blue, float alpha) {
            this.root.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
        }
    }
}
