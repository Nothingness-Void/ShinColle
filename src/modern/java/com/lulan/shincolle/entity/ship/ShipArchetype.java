package com.lulan.shincolle.entity.ship;

import net.minecraft.world.entity.EntityDimensions;

public enum ShipArchetype {

    DESTROYER(0.62F, 1.82F, 24.0D, 0.30D, 4.0D, 20.0D, 0.96F, 0.45F, 0.18D, 0.030D, 0.00020D, 0.04D),
    CRUISER(0.68F, 1.92F, 34.0D, 0.28D, 5.0D, 24.0D, 1.02F, 0.5F, 0.24D, 0.038D, 0.00016D, 0.05D),
    BATTLESHIP(0.78F, 2.08F, 52.0D, 0.24D, 7.0D, 28.0D, 1.1F, 0.58F, 0.36D, 0.050D, 0.00010D, 0.06D),
    CARRIER(0.78F, 2.02F, 46.0D, 0.24D, 6.0D, 28.0D, 1.08F, 0.55F, 0.30D, 0.044D, 0.00010D, 0.06D),
    SUBMARINE(0.56F, 1.48F, 26.0D, 0.29D, 4.0D, 22.0D, 0.9F, 0.4F, 0.20D, 0.028D, 0.00018D, 0.04D),
    TRANSPORT(0.68F, 1.86F, 28.0D, 0.25D, 3.0D, 20.0D, 0.98F, 0.48F, 0.22D, 0.020D, 0.00012D, 0.05D),
    PRINCESS(0.88F, 2.22F, 72.0D, 0.23D, 8.0D, 32.0D, 1.18F, 0.7F, 0.42D, 0.052D, 0.00008D, 0.07D),
    INSTALLATION(1.18F, 2.72F, 110.0D, 0.18D, 10.0D, 36.0D, 1.34F, 0.9F, 0.60D, 0.065D, 0.00004D, 0.08D);

    private final EntityDimensions dimensions;
    private final double maxHealth;
    private final double movementSpeed;
    private final double attackDamage;
    private final double followRange;
    private final float renderScale;
    private final float shadowRadius;
    private final double healthGrowth;
    private final double attackGrowth;
    private final double speedGrowth;
    private final double rangeGrowth;

    ShipArchetype(float width, float height, double maxHealth, double movementSpeed, double attackDamage,
                  double followRange, float renderScale, float shadowRadius,
                  double healthGrowth, double attackGrowth, double speedGrowth, double rangeGrowth) {
        this.dimensions = EntityDimensions.scalable(width, height);
        this.maxHealth = maxHealth;
        this.movementSpeed = movementSpeed;
        this.attackDamage = attackDamage;
        this.followRange = followRange;
        this.renderScale = renderScale;
        this.shadowRadius = shadowRadius;
        this.healthGrowth = healthGrowth;
        this.attackGrowth = attackGrowth;
        this.speedGrowth = speedGrowth;
        this.rangeGrowth = rangeGrowth;
    }

    public EntityDimensions dimensions() {
        return this.dimensions;
    }

    public double maxHealth() {
        return this.maxHealth;
    }

    public double movementSpeed() {
        return this.movementSpeed;
    }

    public double attackDamage() {
        return this.attackDamage;
    }

    public double followRange() {
        return this.followRange;
    }

    public float renderScale() {
        return this.renderScale;
    }

    public float shadowRadius() {
        return this.shadowRadius;
    }

    public double healthGrowth() {
        return this.healthGrowth;
    }

    public double attackGrowth() {
        return this.attackGrowth;
    }

    public double speedGrowth() {
        return this.speedGrowth;
    }

    public double rangeGrowth() {
        return this.rangeGrowth;
    }
}
