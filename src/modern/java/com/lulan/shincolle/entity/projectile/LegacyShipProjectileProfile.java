package com.lulan.shincolle.entity.projectile;

import com.lulan.shincolle.network.GameplayParticleType;

public record LegacyShipProjectileProfile(
        LegacyShipProjectileVisual visual,
        LegacyShipProjectileMoveType moveType,
        double baseSpeed,
        float launchHeightFactor,
        float targetHeightFactor,
        int lifeTicks,
        float guidanceBlend,
        float launchArcLift,
        GameplayParticleType launchParticleType,
        GameplayParticleType trailParticleType,
        GameplayParticleType hitParticleType) {

    public static final LegacyShipProjectileProfile NONE = new LegacyShipProjectileProfile(
            LegacyShipProjectileVisual.NONE,
            LegacyShipProjectileMoveType.NONE,
            0.0D,
            0.6F,
            0.35F,
            40,
            0.0F,
            0.0F,
            GameplayParticleType.LAUNCH_SMOKE,
            GameplayParticleType.NONE,
            GameplayParticleType.HIT_EXPLOSION);

    public boolean isPresent() {
        return this.visual != LegacyShipProjectileVisual.NONE && this.moveType != LegacyShipProjectileMoveType.NONE;
    }
}
