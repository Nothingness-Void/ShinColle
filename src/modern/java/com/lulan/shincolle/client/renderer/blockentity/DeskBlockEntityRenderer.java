package com.lulan.shincolle.client.renderer.blockentity;

import com.lulan.shincolle.ShinColle;
import com.lulan.shincolle.block.DeskBlock;
import com.lulan.shincolle.blockentity.DeskBlockEntity;
import com.lulan.shincolle.client.model.legacy.LegacyStaticModel;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;

public class DeskBlockEntityRenderer implements BlockEntityRenderer<DeskBlockEntity> {

    private static final ResourceLocation MODEL_SOURCE = ResourceLocation.fromNamespaceAndPath(ShinColle.MOD_ID,
            "legacy_model_sources/modelblockdesk.java");
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(ShinColle.MOD_ID,
            "textures/block/blockdesk.png");

    private final LegacyStaticModel model;

    public DeskBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
        this.model = LegacyStaticModel.load("ModelBlockDesk", MODEL_SOURCE);
    }

    @Override
    public void render(DeskBlockEntity blockEntity, float partialTick, PoseStack poseStack,
                       MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        BlockState state = blockEntity.getBlockState();
        Direction facing = state.hasProperty(DeskBlock.FACING) ? state.getValue(DeskBlock.FACING) : Direction.NORTH;
        VertexConsumer vertexConsumer = bufferSource.getBuffer(RenderType.entityCutoutNoCull(TEXTURE));

        poseStack.pushPose();
        poseStack.translate(0.5D, 1.5D, 0.5D);
        poseStack.scale(1.0F, -1.0F, -1.0F);
        poseStack.mulPose(Axis.YP.rotationDegrees(rotationFor(facing)));
        this.model.render(poseStack, vertexConsumer, packedLight, packedOverlay);
        poseStack.popPose();
    }

    private static float rotationFor(Direction facing) {
        return switch (facing) {
            case EAST -> 90.0F;
            case SOUTH -> 180.0F;
            case WEST -> 270.0F;
            default -> 0.0F;
        };
    }
}
