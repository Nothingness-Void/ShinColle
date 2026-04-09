package com.lulan.shincolle.entity.ship;

import com.lulan.shincolle.entity.projectile.LegacyShipProjectileMoveType;
import com.lulan.shincolle.entity.projectile.LegacyShipProjectileProfile;
import com.lulan.shincolle.entity.projectile.LegacyShipProjectileVisual;
import com.lulan.shincolle.network.GameplayParticleType;

public record LegacyShipAttackProfile(
        boolean melee,
        boolean light,
        boolean heavy,
        boolean airLight,
        boolean airHeavy,
        LegacyShipProjectileProfile heavyProjectile,
        LegacyShipProjectileProfile airLightProjectile,
        LegacyShipProjectileProfile airHeavyProjectile) {

    private static final LegacyShipProjectileProfile MISSILE_BARRAGE = new LegacyShipProjectileProfile(
            LegacyShipProjectileVisual.MISSILE,
            LegacyShipProjectileMoveType.ARC,
            0.52D,
            0.72F,
            0.35F,
            90,
            0.0F,
            0.18F,
            GameplayParticleType.LAUNCH_SMOKE,
            GameplayParticleType.MISSILE_TRAIL,
            GameplayParticleType.MISSILE_IMPACT);
    private static final LegacyShipProjectileProfile GUIDED_MISSILE_BARRAGE = new LegacyShipProjectileProfile(
            LegacyShipProjectileVisual.MISSILE,
            LegacyShipProjectileMoveType.GUIDED,
            0.48D,
            0.72F,
            0.4F,
            100,
            0.22F,
            0.08F,
            GameplayParticleType.LAUNCH_SMOKE,
            GameplayParticleType.MISSILE_TRAIL,
            GameplayParticleType.MISSILE_IMPACT);
    private static final LegacyShipProjectileProfile CARRIER_FIGHTER_SWEEP = new LegacyShipProjectileProfile(
            LegacyShipProjectileVisual.AIRPLANE,
            LegacyShipProjectileMoveType.GUIDED,
            0.44D,
            0.95F,
            0.55F,
            110,
            0.24F,
            0.08F,
            GameplayParticleType.AIRCRAFT_LAUNCH,
            GameplayParticleType.AIRCRAFT_TRAIL,
            GameplayParticleType.AIRCRAFT_IMPACT);
    private static final LegacyShipProjectileProfile CARRIER_TORPEDO_RUN = new LegacyShipProjectileProfile(
            LegacyShipProjectileVisual.TORPEDO,
            LegacyShipProjectileMoveType.DIRECT,
            0.4D,
            0.88F,
            0.22F,
            100,
            0.0F,
            0.0F,
            GameplayParticleType.AIRCRAFT_LAUNCH,
            GameplayParticleType.TORPEDO_WAKE,
            GameplayParticleType.TORPEDO_IMPACT);
    private static final LegacyShipProjectileProfile ABYSS_BOMBER_RUN = new LegacyShipProjectileProfile(
            LegacyShipProjectileVisual.BOMB,
            LegacyShipProjectileMoveType.DROP,
            0.34D,
            0.98F,
            0.72F,
            120,
            0.08F,
            0.18F,
            GameplayParticleType.AIRCRAFT_LAUNCH,
            GameplayParticleType.BOMB_DROP,
            GameplayParticleType.BOMB_IMPACT);

    public static final LegacyShipAttackProfile MELEE_ONLY = new LegacyShipAttackProfile(
            true, false, false, false, false,
            LegacyShipProjectileProfile.NONE,
            LegacyShipProjectileProfile.NONE,
            LegacyShipProjectileProfile.NONE);

    public static LegacyShipAttackProfile resolve(ShipEntitySpec spec) {
        String stem = spec.textureStem();
        boolean melee = true;
        boolean light;
        boolean heavy;
        boolean airLight;
        boolean airHeavy;

        switch (stem) {
            case "EntityTransportWa" -> {
                light = false;
                heavy = false;
                airLight = false;
                airHeavy = false;
            }
            case "EntityCarrierAkagi", "EntityCarrierKaga", "EntityCarrierWo", "EntityCarrierHime" -> {
                light = false;
                heavy = false;
                airLight = true;
                airHeavy = true;
            }
            case "EntityCarrierWDemon" -> {
                light = true;
                heavy = false;
                airLight = true;
                airHeavy = true;
            }
            case "EntityAirfieldHime", "EntityHarbourHime", "EntityIsolatedHime", "EntityMidwayHime", "EntityNorthernHime" -> {
                light = true;
                heavy = true;
                airLight = true;
                airHeavy = true;
            }
            default -> {
                switch (spec.archetype()) {
                    case CARRIER -> {
                        light = false;
                        heavy = false;
                        airLight = true;
                        airHeavy = true;
                    }
                    case TRANSPORT -> {
                        light = false;
                        heavy = false;
                        airLight = false;
                        airHeavy = false;
                    }
                    case INSTALLATION -> {
                        light = true;
                        heavy = true;
                        airLight = true;
                        airHeavy = true;
                    }
                    default -> {
                        light = true;
                        heavy = true;
                        airLight = false;
                        airHeavy = false;
                    }
                }
            }
        }

        LegacyShipProjectileProfile heavyProjectile = heavy
                ? (spec.archetype() == ShipArchetype.INSTALLATION ? GUIDED_MISSILE_BARRAGE : MISSILE_BARRAGE)
                : LegacyShipProjectileProfile.NONE;
        LegacyShipProjectileProfile airLightProjectile = airLight ? CARRIER_FIGHTER_SWEEP : LegacyShipProjectileProfile.NONE;
        LegacyShipProjectileProfile airHeavyProjectile = airHeavy
                ? switch (stem) {
                    case "EntityCarrierWo", "EntityCarrierHime", "EntityCarrierWDemon",
                            "EntityAirfieldHime", "EntityHarbourHime", "EntityIsolatedHime",
                            "EntityMidwayHime", "EntityNorthernHime" -> ABYSS_BOMBER_RUN;
                    default -> CARRIER_TORPEDO_RUN;
                }
                : LegacyShipProjectileProfile.NONE;

        if (spec.hostile() && spec.eggMeta() >= 2000) {
            if (heavy) {
                heavyProjectile = GUIDED_MISSILE_BARRAGE;
            }
            if (airHeavy) {
                airHeavyProjectile = ABYSS_BOMBER_RUN;
            }
        }

        return new LegacyShipAttackProfile(
                melee,
                light,
                heavy,
                airLight,
                airHeavy,
                heavyProjectile,
                airLightProjectile,
                airHeavyProjectile);
    }

    public boolean hasRangedAttack() {
        return this.light || this.heavy || this.airLight || this.airHeavy;
    }

    public boolean hasAirAttack() {
        return this.airLight || this.airHeavy;
    }

    public LegacyShipProjectileProfile projectileProfile(LegacyShipAttackKind attackKind) {
        return switch (attackKind) {
            case HEAVY -> this.heavyProjectile;
            case AIR_LIGHT -> this.airLightProjectile;
            case AIR_HEAVY -> this.airHeavyProjectile;
            default -> LegacyShipProjectileProfile.NONE;
        };
    }
}
