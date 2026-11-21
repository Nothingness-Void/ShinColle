package com.lulan.shincolle.client.model.legacy.compat;

import net.minecraft.client.renderer.LightTexture;

public final class OpenGlHelper {
    public static final int lightmapTexUnit = 1;

    private OpenGlHelper() {}

    public static void setLightmapTextureCoords(int unit, float x, float y) {
        LegacyRenderContext.light = LightTexture.pack((int)x / 16, (int)y / 16);
    }
}
