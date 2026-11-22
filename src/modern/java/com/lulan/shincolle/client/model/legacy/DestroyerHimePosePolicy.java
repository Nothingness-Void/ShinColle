package com.lulan.shincolle.client.model.legacy;

public final class DestroyerHimePosePolicy {
    private DestroyerHimePosePolicy() {}

    public static boolean showLegs(int modelState, boolean inWater) {
        return (modelState & (1 << 4)) != 0 || !inWater;
    }
}
