package com.lulan.shincolle.client.model.legacy.compat;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

public final class LegacyRenderContext {
    public static PoseStack poseStack;
    public static VertexConsumer vertices;
    public static int light;
    public static int overlay;
    public static float red, green, blue, alpha;

    private LegacyRenderContext() {}

    public static void begin(PoseStack poses, VertexConsumer consumer, int packedLight, int packedOverlay,
                             float r, float g, float b, float a) {
        poseStack = poses;
        vertices = consumer;
        light = packedLight;
        overlay = packedOverlay;
        red = r;
        green = g;
        blue = b;
        alpha = a;
    }

    public static void end() {
        poseStack = null;
        vertices = null;
    }
}
