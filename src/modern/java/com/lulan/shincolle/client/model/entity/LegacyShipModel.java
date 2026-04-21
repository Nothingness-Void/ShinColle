package com.lulan.shincolle.client.model.entity;

import com.lulan.shincolle.ShinColle;
import com.lulan.shincolle.client.model.legacy.LegacyModelDefinition;
import com.lulan.shincolle.client.model.legacy.LegacyModelSourceParser;
import com.lulan.shincolle.entity.ship.LegacyShipEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.logging.LogUtils;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.util.Mth;
import org.slf4j.Logger;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class LegacyShipModel extends EntityModel<LegacyShipEntity> {

    private static final Logger LOGGER = LogUtils.getLogger();
    private static final LegacyModelSourceParser SOURCE_PARSER = new LegacyModelSourceParser();
    private static final BakedLegacyModel FALLBACK_MODEL = BakedLegacyModel.createFallback();

    private final Map<String, BakedLegacyModel> bakedModels = new LinkedHashMap<>();
    private BakedLegacyModel currentModel = FALLBACK_MODEL;

    public LegacyShipModel() {
        super(RenderType::entityTranslucent);
    }

    @Override
    public void setupAnim(LegacyShipEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks,
                          float netHeadYaw, float headPitch) {
        this.currentModel = this.bakedModels.computeIfAbsent(entity.getSpec().modelSourceStem(), this::loadModel);
        this.currentModel.resetPose();
        this.currentModel.applyHeadRotation(netHeadYaw, headPitch);
        this.currentModel.applyWalk(limbSwing, limbSwingAmount);
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay,
                               float red, float green, float blue, float alpha) {
        this.currentModel.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
    }

    private BakedLegacyModel loadModel(String modelSourceStem) {
        ResourceLocation sourceLocation = ResourceLocation.fromNamespaceAndPath(ShinColle.MOD_ID,
                "legacy_model_sources/" + modelSourceStem.toLowerCase(Locale.ROOT) + ".java");

        try {
            Resource resource = Minecraft.getInstance().getResourceManager().getResource(sourceLocation)
                    .orElseThrow(() -> new IOException("Missing legacy model source " + sourceLocation));
            String source;

            try (var inputStream = resource.open()) {
                source = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
            }

            LegacyModelDefinition definition = SOURCE_PARSER.parse(modelSourceStem, source);
            return BakedLegacyModel.bake(definition);
        } catch (Exception exception) {
            LOGGER.warn("Failed to bake legacy ship model {}, falling back to simple placeholder", modelSourceStem,
                    exception);
            return FALLBACK_MODEL;
        }
    }

    private static final class BakedLegacyModel {
        private static final float DEG_TO_RAD = (float) (Math.PI / 180.0D);

        private final ModelPart root;
        private final Map<String, ModelPart> parts;
        private final Map<String, LegacyModelDefinition.LegacyPart> definitions;
        private final List<String> headParts;
        private final List<String> neckParts;
        private final List<String> leftArmParts;
        private final List<String> rightArmParts;
        private final List<String> leftLegParts;
        private final List<String> rightLegParts;
        private final List<String> faceParts;
        private final List<String> mouthParts;
        private final List<String> flushParts;
        private final List<LegacyModelDefinition.TransformOp> transformOps;

        private BakedLegacyModel(ModelPart root, Map<String, ModelPart> parts,
                                 Map<String, LegacyModelDefinition.LegacyPart> definitions,
                                 List<LegacyModelDefinition.TransformOp> transformOps) {
            this.root = root;
            this.parts = parts;
            this.definitions = definitions;
            this.transformOps = transformOps;
            this.headParts = findNamedParts(parts, "Head", "GlowHead", "PHead");
            this.neckParts = findNamedParts(parts, "Neck", "GlowNeck", "PNeck", "Neck02", "Neck03");
            this.leftArmParts = findMatchingParts(parts, "ArmLeft01", "ArmLeft", "ArmL", "PalmLeft");
            this.rightArmParts = findMatchingParts(parts, "ArmRight01", "ArmRight", "ArmR", "PalmRight");
            this.leftLegParts = findMatchingParts(parts, "LegLeft01", "LegLeft", "LegL");
            this.rightLegParts = findMatchingParts(parts, "LegRight01", "LegRight", "LegR");
            this.faceParts = findNamedParts(parts, "Face0", "Face1", "Face2", "Face3", "Face4");
            this.mouthParts = findNamedParts(parts, "Mouth0", "Mouth1", "Mouth2");
            this.flushParts = findNamedParts(parts, "Flush0", "Flush1");
        }

        private static BakedLegacyModel bake(LegacyModelDefinition definition) {
            MeshDefinition meshDefinition = new MeshDefinition();
            PartDefinition rootDefinition = meshDefinition.getRoot();

            for (String rootPart : definition.rootParts()) {
                addPart(rootDefinition, definition, rootPart);
            }

            ModelPart bakedRoot = LayerDefinition.create(meshDefinition, definition.textureWidth(),
                    definition.textureHeight()).bakeRoot();
            Map<String, ModelPart> bakedParts = new LinkedHashMap<>();

            for (String rootPart : definition.rootParts()) {
                collectParts(bakedRoot.getChild(rootPart), definition, rootPart, bakedParts);
            }

            return new BakedLegacyModel(bakedRoot, bakedParts, definition.parts(), definition.transformOps());
        }

        private static BakedLegacyModel createFallback() {
            MeshDefinition meshDefinition = new MeshDefinition();
            PartDefinition rootDefinition = meshDefinition.getRoot();
            CubeListBuilder bodyBuilder = CubeListBuilder.create()
                    .texOffs(0, 0)
                    .addBox(-4.0F, -12.0F, -2.0F, 8.0F, 12.0F, 4.0F, new CubeDeformation(0.0F))
                    .texOffs(24, 0)
                    .addBox(-4.0F, -20.0F, -4.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.0F));
            rootDefinition.addOrReplaceChild("fallback", bodyBuilder, PartPose.ZERO);
            ModelPart bakedRoot = LayerDefinition.create(meshDefinition, 64, 64).bakeRoot();
            Map<String, ModelPart> bakedParts = Map.of("fallback", bakedRoot.getChild("fallback"));
            Map<String, LegacyModelDefinition.LegacyPart> definitions = Map.of("fallback",
                    new LegacyModelDefinition.LegacyPart("fallback", 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F,
                            List.of(), List.of()));
            return new BakedLegacyModel(bakedRoot, bakedParts, definitions, List.of());
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
                    PartPose.offsetAndRotation(part.pivotX(), part.pivotY(), part.pivotZ(),
                            part.rotateX(), part.rotateY(), part.rotateZ()));

            for (String child : part.children()) {
                addPart(childDefinition, definition, child);
            }
        }

        private static void collectParts(ModelPart bakedPart, LegacyModelDefinition definition, String partName,
                                         Map<String, ModelPart> bakedParts) {
            bakedParts.put(partName, bakedPart);

            LegacyModelDefinition.LegacyPart definitionPart = definition.parts().get(partName);

            if (definitionPart == null) {
                return;
            }

            for (String child : definitionPart.children()) {
                if (bakedPart.hasChild(child)) {
                    collectParts(bakedPart.getChild(child), definition, child, bakedParts);
                }
            }
        }

        private static List<String> findNamedParts(Map<String, ModelPart> parts, String... names) {
            List<String> found = new ArrayList<>();

            for (String name : names) {
                if (parts.containsKey(name)) {
                    found.add(name);
                }
            }

            return found;
        }

        private static List<String> findMatchingParts(Map<String, ModelPart> parts, String... prefixes) {
            List<String> found = new ArrayList<>();

            for (String candidate : parts.keySet()) {
                for (String prefix : prefixes) {
                    if (candidate.startsWith(prefix)) {
                        found.add(candidate);
                        break;
                    }
                }
            }

            return found;
        }

        private void resetPose() {
            for (Map.Entry<String, ModelPart> entry : this.parts.entrySet()) {
                LegacyModelDefinition.LegacyPart definitionPart = this.definitions.get(entry.getKey());

                if (definitionPart == null) {
                    continue;
                }

                ModelPart part = entry.getValue();
                part.x = definitionPart.pivotX();
                part.y = definitionPart.pivotY();
                part.z = definitionPart.pivotZ();
                part.xRot = definitionPart.rotateX();
                part.yRot = definitionPart.rotateY();
                part.zRot = definitionPart.rotateZ();
                part.visible = true;
            }

            applyDefaultOverlayVisibility(this.faceParts, "Face0");
            applyDefaultOverlayVisibility(this.mouthParts, "Mouth0");

            for (String partName : this.flushParts) {
                ModelPart part = this.parts.get(partName);

                if (part != null) {
                    part.visible = false;
                }
            }
        }

        private void applyHeadRotation(float netHeadYaw, float headPitch) {
            float yaw = netHeadYaw * DEG_TO_RAD * 0.65F;
            float pitch = headPitch * DEG_TO_RAD * 0.55F;

            for (String partName : this.headParts) {
                ModelPart part = this.parts.get(partName);

                if (part != null) {
                    part.yRot += yaw;
                    part.xRot += pitch;
                }
            }

            for (String partName : this.neckParts) {
                ModelPart part = this.parts.get(partName);

                if (part != null) {
                    part.yRot += yaw * 0.5F;
                    part.xRot += pitch * 0.35F;
                }
            }
        }

        private void applyWalk(float limbSwing, float limbSwingAmount) {
            float walkStrength = Mth.clamp(limbSwingAmount, 0.0F, 1.0F);
            float armSwing = Mth.cos(limbSwing * 0.6662F) * 0.32F * walkStrength;
            float legSwing = Mth.cos(limbSwing * 0.6662F) * 0.45F * walkStrength;

            addXRotation(this.leftArmParts, -armSwing);
            addXRotation(this.rightArmParts, armSwing);
            addXRotation(this.leftLegParts, legSwing);
            addXRotation(this.rightLegParts, -legSwing);
        }

        private void addXRotation(List<String> partNames, float amount) {
            for (String partName : partNames) {
                ModelPart part = this.parts.get(partName);

                if (part != null) {
                    part.xRot += amount;
                }
            }
        }

        private void applyDefaultOverlayVisibility(List<String> partNames, String preferredVisiblePart) {
            if (partNames.isEmpty()) {
                return;
            }

            String visiblePart = this.parts.containsKey(preferredVisiblePart) ? preferredVisiblePart : partNames.get(0);

            for (String partName : partNames) {
                ModelPart part = this.parts.get(partName);

                if (part != null) {
                    part.visible = visiblePart.equals(partName);
                }
            }
        }

        private void render(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay,
                            float red, float green, float blue, float alpha) {
            poseStack.pushPose();

            for (LegacyModelDefinition.TransformOp transformOp : this.transformOps) {
                switch (transformOp.operation()) {
                    case SCALE -> poseStack.scale(transformOp.x(), transformOp.y(), transformOp.z());
                    case TRANSLATE -> poseStack.translate(transformOp.x(), transformOp.y(), transformOp.z());
                    case ROTATE -> applyRotation(poseStack, transformOp);
                    default -> {
                    }
                }
            }

            this.root.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
            poseStack.popPose();
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
}
