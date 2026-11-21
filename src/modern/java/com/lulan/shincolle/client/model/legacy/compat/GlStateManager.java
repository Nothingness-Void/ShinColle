package com.lulan.shincolle.client.model.legacy.compat;

import org.joml.Quaternionf;

public final class GlStateManager {
    public enum SourceFactor { SRC_ALPHA, ONE }
    public enum DestFactor { ONE_MINUS_SRC_ALPHA, ONE }

    private GlStateManager() {}

    public static void pushMatrix() { LegacyRenderContext.poseStack.pushPose(); }
    public static void popMatrix() { LegacyRenderContext.poseStack.popPose(); }
    public static void translate(double x, double y, double z) { LegacyRenderContext.poseStack.translate(x, y, z); }
    public static void scale(double x, double y, double z) { LegacyRenderContext.poseStack.scale((float)x, (float)y, (float)z); }
    public static void rotate(float angle, float x, float y, float z) {
        LegacyRenderContext.poseStack.mulPose(new Quaternionf().rotationAxis(angle * ((float)Math.PI / 180F), x, y, z));
    }
    public static void color(float r, float g, float b, float a) {
        LegacyRenderContext.red = r;
        LegacyRenderContext.green = g;
        LegacyRenderContext.blue = b;
        LegacyRenderContext.alpha = a;
    }
    public static void color(float r, float g, float b) { color(r, g, b, 1F); }

    // Fixed-function state is provided by the modern RenderType (API adaptation).
    public static void enableLighting() {}
    public static void disableLighting() {}
    public static void enableBlend() {}
    public static void disableBlend() {}
    public static void enableCull() {}
    public static void disableCull() {}
    public static void pushAttrib() {}
    public static void popAttrib() {}
    public static void blendFunc(SourceFactor source, DestFactor destination) {}
}
