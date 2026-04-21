package com.lulan.shincolle.crafting;

public final class LegacyBuildRollHelper {

    private static final double NORMAL_SCALE = 0.50132566D;
    private static final double STANDARD_DEVIATION = 0.2D;
    private static final double MEAN = 0.5D;
    private static final double MIN_PROBABILITY = 0.2D;

    private LegacyBuildRollHelper() {
    }

    public static float normalProbability(int meanDistance) {
        if (meanDistance < 0 || meanDistance >= 2000) {
            return (float) MIN_PROBABILITY;
        }

        double x = MEAN - meanDistance * 0.00025D;
        double scale = 1.0D / (STANDARD_DEVIATION * Math.sqrt(2.0D * Math.PI));
        double delta = x - MEAN;
        double exponent = -(delta * delta) / (2.0D * STANDARD_DEVIATION * STANDARD_DEVIATION);
        double probability = scale * Math.exp(exponent) * NORMAL_SCALE;
        return (float) Math.max(MIN_PROBABILITY, probability);
    }
}
