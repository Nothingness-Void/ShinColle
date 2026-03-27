package com.lulan.shincolle.client.model.legacy;

import com.lulan.shincolle.ShinColle;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.logging.LogUtils;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import org.slf4j.Logger;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public final class LegacyStaticModel {

    private static final Logger LOGGER = LogUtils.getLogger();
    private static final LegacyModelSourceParser SOURCE_PARSER = new LegacyModelSourceParser();
    private static final LegacyStaticModel FALLBACK = createFallback();

    private final ModelPart root;
    private final LegacyModelDefinition definition;

    private LegacyStaticModel(ModelPart root, LegacyModelDefinition definition) {
        this.root = root;
        this.definition = definition;
    }

    public static LegacyStaticModel load(String modelName, ResourceLocation sourceLocation) {
        try {
            Resource resource = Minecraft.getInstance().getResourceManager().getResource(sourceLocation)
                    .orElseThrow(() -> new IOException("Missing legacy model source " + sourceLocation));
            String source;

            try (var inputStream = resource.open()) {
                source = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
            }

            return bake(SOURCE_PARSER.parse(modelName, source));
        } catch (Exception exception) {
            LOGGER.warn("Failed to bake legacy static model {} from {}", modelName, sourceLocation, exception);
            return FALLBACK;
        }
    }

    public void render(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay) {
        poseStack.pushPose();

        for (LegacyModelDefinition.TransformOp transformOp : this.definition.transformOps()) {
            switch (transformOp.operation()) {
                case SCALE -> poseStack.scale(transformOp.x(), transformOp.y(), transformOp.z());
                case TRANSLATE -> poseStack.translate(transformOp.x(), transformOp.y(), transformOp.z());
                case ROTATE -> applyRotation(poseStack, transformOp);
                default -> {
                }
            }
        }

        this.root.render(poseStack, vertexConsumer, packedLight, packedOverlay, 1.0F, 1.0F, 1.0F, 1.0F);
        poseStack.popPose();
    }

    private static LegacyStaticModel bake(LegacyModelDefinition definition) {
        MeshDefinition meshDefinition = new MeshDefinition();
        PartDefinition rootDefinition = meshDefinition.getRoot();

        for (String rootPart : definition.rootParts()) {
            addPart(rootDefinition, definition, rootPart);
        }

        ModelPart bakedRoot = LayerDefinition.create(meshDefinition, definition.textureWidth(),
                definition.textureHeight()).bakeRoot();
        return new LegacyStaticModel(bakedRoot, definition);
    }

    private static void addPart(PartDefinition parentDefinition, LegacyModelDefinition definition, String partName) {
        LegacyModelDefinition.LegacyPart part = definition.parts().get(partName);

        if (part == null) {
            return;
        }

        CubeListBuilder cubeBuilder = CubeListBuilder.create();

        for (LegacyModelDefinition.LegacyCube cube : part.cubes()) {
            cubeBuilder.texOffs(cube.texU(), cube.texV()).mirror(cube.mirror());
            cubeBuilder.addBox(cube.x(), cube.y(), cube.z(), cube.width(), cube.height(), cube.depth(),
                    new CubeDeformation(cube.deformation()));
        }

        PartDefinition childDefinition = parentDefinition.addOrReplaceChild(partName, cubeBuilder,
                PartPose.offsetAndRotation(part.pivotX(), part.pivotY(), part.pivotZ(), part.rotateX(),
                        part.rotateY(), part.rotateZ()));

        for (String child : part.children()) {
            addPart(childDefinition, definition, child);
        }
    }

    private static LegacyStaticModel createFallback() {
        MeshDefinition meshDefinition = new MeshDefinition();
        PartDefinition rootDefinition = meshDefinition.getRoot();
        rootDefinition.addOrReplaceChild("fallback",
                CubeListBuilder.create()
                        .texOffs(0, 0)
                        .addBox(-8.0F, 0.0F, -8.0F, 16.0F, 16.0F, 16.0F, new CubeDeformation(0.0F)),
                PartPose.ZERO);

        LegacyModelDefinition fallbackDefinition = new LegacyModelDefinition(64, 64, java.util.List.of(),
                java.util.List.of("fallback"),
                java.util.Map.of("fallback", new LegacyModelDefinition.LegacyPart("fallback", 0.0F, 0.0F, 0.0F,
                        0.0F, 0.0F, 0.0F, java.util.List.of(), java.util.List.of())));
        return new LegacyStaticModel(LayerDefinition.create(meshDefinition, 64, 64).bakeRoot(), fallbackDefinition);
    }

    private static void applyRotation(PoseStack poseStack, LegacyModelDefinition.TransformOp transformOp) {
        if (transformOp.y() == 1.0F && transformOp.z() == 0.0F && transformOp.w() == 0.0F) {
            poseStack.mulPose(Axis.XP.rotationDegrees(transformOp.x()));
            return;
        }

        if (transformOp.z() == 1.0F && transformOp.y() == 0.0F && transformOp.w() == 0.0F) {
            poseStack.mulPose(Axis.YP.rotationDegrees(transformOp.x()));
            return;
        }

        if (transformOp.w() == 1.0F && transformOp.y() == 0.0F && transformOp.z() == 0.0F) {
            poseStack.mulPose(Axis.ZP.rotationDegrees(transformOp.x()));
        }
    }
}
