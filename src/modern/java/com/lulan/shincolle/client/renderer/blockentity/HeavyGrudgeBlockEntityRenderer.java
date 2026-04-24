package com.lulan.shincolle.client.renderer.blockentity;

import com.lulan.shincolle.ShinColle;
import com.lulan.shincolle.block.HeavyGrudgeBlock;
import com.lulan.shincolle.blockentity.HeavyGrudgeBlockEntity;
import com.lulan.shincolle.client.model.legacy.LegacyStaticModel;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.state.BlockState;

public class HeavyGrudgeBlockEntityRenderer implements BlockEntityRenderer<HeavyGrudgeBlockEntity> {

    private static final ResourceLocation BASE_MODEL_SOURCE = ResourceLocation.fromNamespaceAndPath(ShinColle.MOD_ID,
            "legacy_model_sources/modellargeshipyard.java");
    private static final ResourceLocation VORTEX_MODEL_SOURCE = ResourceLocation.fromNamespaceAndPath(ShinColle.MOD_ID,
            "legacy_model_sources/modelvortex.java");
    private static final ResourceLocation BASE_TEXTURE = ResourceLocation.fromNamespaceAndPath(ShinColle.MOD_ID,
            "textures/blocks/blocklargeshipyard.png");
    private static final ResourceLocation VORTEX_TEXTURE_OFF = ResourceLocation.fromNamespaceAndPath(ShinColle.MOD_ID,
            "textures/blocks/modelvortex.png");
    private static final ResourceLocation VORTEX_TEXTURE_ON = ResourceLocation.fromNamespaceAndPath(ShinColle.MOD_ID,
            "textures/blocks/modelvortexon.png");

    private final LegacyStaticModel baseModel;
    private final LegacyStaticModel vortexModel;

    public HeavyGrudgeBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
        this.baseModel = LegacyStaticModel.load("ModelLargeShipyard", BASE_MODEL_SOURCE);
        this.vortexModel = LegacyStaticModel.load("ModelVortex", VORTEX_MODEL_SOURCE);
    }

    @Override
    public void render(HeavyGrudgeBlockEntity blockEntity, float partialTick, PoseStack poseStack,
                       MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        if (!blockEntity.isStructureComplete()) {
            return;
        }

        VertexConsumer baseConsumer = bufferSource.getBuffer(RenderType.entityCutoutNoCull(BASE_TEXTURE));

        poseStack.pushPose();
        poseStack.translate(0.5D, -0.2D, 0.5D);
        poseStack.mulPose(Axis.ZP.rotationDegrees(180.0F));
        poseStack.scale(1.0F, 1.2F, 1.0F);
        this.baseModel.render(poseStack, baseConsumer, packedLight, packedOverlay);
        poseStack.popPose();

        Entity cameraEntity = Minecraft.getInstance().getCameraEntity();
        if (cameraEntity == null) {
            return;
        }

        double distX = blockEntity.getBlockPos().getX() + 0.5D - cameraEntity.getX();
        double distY = blockEntity.getBlockPos().getY() - 0.75D - cameraEntity.getY();
        double distZ = blockEntity.getBlockPos().getZ() + 0.5D - cameraEntity.getZ();
        double horizontalDistance = Math.sqrt(distX * distX + distZ * distZ);
        float pitch = (float) Math.toDegrees(Math.atan2(horizontalDistance, distY) + (Math.PI * 0.5D));
        float yaw = (float) Math.toDegrees(Math.atan2(distX, distZ));

        BlockState state = blockEntity.getBlockState();
        boolean active = state.hasProperty(HeavyGrudgeBlock.MBS) && state.getValue(HeavyGrudgeBlock.MBS) == 2;
        float spin = -cameraEntity.tickCount - partialTick;
        if (active) {
            spin *= 5.0F;
        }

        VertexConsumer vortexConsumer = bufferSource.getBuffer(RenderType.entityTranslucent(active
                ? VORTEX_TEXTURE_ON
                : VORTEX_TEXTURE_OFF));
        poseStack.pushPose();
        poseStack.translate(0.5D, 0.5D, 0.5D);
        poseStack.mulPose(Axis.YP.rotationDegrees(yaw));
        poseStack.mulPose(Axis.XP.rotationDegrees(pitch));
        poseStack.mulPose(Axis.ZP.rotationDegrees(spin));
        this.vortexModel.render(poseStack, vortexConsumer, packedLight, packedOverlay);
        poseStack.popPose();
    }
}
