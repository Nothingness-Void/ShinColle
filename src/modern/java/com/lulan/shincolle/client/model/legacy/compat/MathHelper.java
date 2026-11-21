package com.lulan.shincolle.client.model.legacy.compat;

import net.minecraft.util.Mth;

public final class MathHelper {
    private MathHelper() {}
    public static float sin(float value) { return Mth.sin(value); }
    public static float cos(float value) { return Mth.cos(value); }
    public static float sqrt(float value) { return Mth.sqrt(value); }
    public static float sqrt(double value) { return (float)Math.sqrt(value); }
}
