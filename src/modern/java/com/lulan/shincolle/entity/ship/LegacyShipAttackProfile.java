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

    private static final LegacyShipProjectileProfile LEGACY_HEAVY_MISSILE = new LegacyShipProjectileProfile(
            LegacyShipProjectileVisual.MISSILE,
            LegacyShipProjectileMoveType.ARC,
            0.5D,
            0.5F,
            0.1F,
            160,
            0.0F,
            0.25F,
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

    private static final java.util.Set<String> LEGACY_AIR_ATTACK_STEMS = java.util.Set.of(
            "EntityCarrierWo",
            "EntityBattleshipRe",
            "EntityCarrierHime",
            "EntityAirfieldHime",
            "EntityHarbourHime",
            "EntityIsolatedHime",
            "EntityMidwayHime",
            "EntityNorthernHime",
            "EntityCarrierWDemon",
            "EntityCarrierKaga",
            "EntityCarrierAkagi");

    public static final LegacyShipAttackProfile MELEE_ONLY = new LegacyShipAttackProfile(
            true, false, false, false, false,
            LegacyShipProjectileProfile.NONE,
            LegacyShipProjectileProfile.NONE,
            LegacyShipProjectileProfile.NONE);

    public static LegacyShipAttackProfile resolve(ShipEntitySpec spec) {
        String stem = spec.textureStem();
        boolean melee = true;
        boolean light = true;
        boolean heavy = true;
        boolean airLight = LEGACY_AIR_ATTACK_STEMS.contains(stem);
        boolean airHeavy = airLight;

        LegacyShipProjectileProfile heavyProjectile = heavy ? LEGACY_HEAVY_MISSILE : LegacyShipProjectileProfile.NONE;
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
