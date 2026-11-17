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
        LegacyShipProjectileProfile lightProjectile,
        LegacyShipProjectileProfile heavyProjectile,
        LegacyShipProjectileProfile airLightProjectile,
        LegacyShipProjectileProfile airHeavyProjectile) {

    private static final LegacyShipProjectileProfile SUBMARINE_LIGHT_MISSILE = new LegacyShipProjectileProfile(
            LegacyShipProjectileVisual.MISSILE,
            LegacyShipProjectileMoveType.ARC,
            0.9D,
            1.0F,
            0.1F,
            140,
            0.0F,
            0.25F,
            GameplayParticleType.LAUNCH_SMOKE,
            GameplayParticleType.MISSILE_TRAIL,
            GameplayParticleType.MISSILE_IMPACT);
    private static final LegacyShipProjectileProfile SUBMARINE_RO500_LIGHT_MISSILE = new LegacyShipProjectileProfile(
            LegacyShipProjectileVisual.MISSILE,
            LegacyShipProjectileMoveType.ARC,
            1.0D,
            1.0F,
            0.1F,
            140,
            0.0F,
            0.25F,
            GameplayParticleType.LAUNCH_SMOKE,
            GameplayParticleType.MISSILE_TRAIL,
            GameplayParticleType.MISSILE_IMPACT);
    private static final LegacyShipProjectileProfile SUBMARINE_YO_LIGHT_MISSILE = new LegacyShipProjectileProfile(
            LegacyShipProjectileVisual.MISSILE,
            LegacyShipProjectileMoveType.ARC,
            0.8D,
            1.0F,
            0.1F,
            140,
            0.0F,
            0.25F,
            GameplayParticleType.LAUNCH_SMOKE,
            GameplayParticleType.MISSILE_TRAIL,
            GameplayParticleType.MISSILE_IMPACT);
    private static final LegacyShipProjectileProfile HOSTILE_SUBMARINE_LIGHT_MISSILE = new LegacyShipProjectileProfile(
            LegacyShipProjectileVisual.MISSILE,
            LegacyShipProjectileMoveType.ARC,
            0.8D,
            0.5F,
            0.1F,
            160,
            0.0F,
            0.25F,
            GameplayParticleType.LAUNCH_SMOKE,
            GameplayParticleType.MISSILE_TRAIL,
            GameplayParticleType.MISSILE_IMPACT);
    private static final LegacyShipProjectileProfile SUBMARINE_HIME_LIGHT_MISSILE = new LegacyShipProjectileProfile(
            LegacyShipProjectileVisual.MISSILE,
            LegacyShipProjectileMoveType.ARC,
            0.8D,
            0.6F,
            0.1F,
            160,
            0.0F,
            0.25F,
            GameplayParticleType.LAUNCH_SMOKE,
            GameplayParticleType.MISSILE_TRAIL,
            GameplayParticleType.MISSILE_IMPACT);

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
    private static final LegacyShipProjectileProfile CARRIER_HIME_MELEE_MISSILE = new LegacyShipProjectileProfile(
            LegacyShipProjectileVisual.MISSILE,
            LegacyShipProjectileMoveType.DIRECT,
            0.8D,
            0.0F,
            0.2F,
            100,
            0.0F,
            0.0F,
            GameplayParticleType.LAUNCH_SMOKE,
            GameplayParticleType.MISSILE_TRAIL,
            GameplayParticleType.MISSILE_IMPACT);
    private static final LegacyShipProjectileProfile BATTLESHIP_HIME_CLUSTER_MISSILE = new LegacyShipProjectileProfile(
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
    private static final LegacyShipProjectileProfile CLUSTER_SUB_MISSILE = new LegacyShipProjectileProfile(
            LegacyShipProjectileVisual.MISSILE,
            LegacyShipProjectileMoveType.DIRECT,
            0.5D,
            0.0F,
            0.1F,
            140,
            0.0F,
            0.0F,
            GameplayParticleType.LAUNCH_SMOKE,
            GameplayParticleType.MISSILE_TRAIL,
            GameplayParticleType.MISSILE_IMPACT);
    private static final LegacyShipProjectileProfile HARBOUR_HIME_RAILGUN = new LegacyShipProjectileProfile(
            LegacyShipProjectileVisual.MISSILE,
            LegacyShipProjectileMoveType.DIRECT,
            3.0D,
            0.3F,
            0.1F,
            160,
            0.0F,
            0.0F,
            GameplayParticleType.LAUNCH_SMOKE,
            GameplayParticleType.LEGACY_RAILGUN,
            GameplayParticleType.MISSILE_IMPACT);
    private static final LegacyShipProjectileProfile SHIMAKAZE_HEAVY_MISSILE = new LegacyShipProjectileProfile(
            LegacyShipProjectileVisual.MISSILE,
            LegacyShipProjectileMoveType.DIRECT,
            0.7D,
            0.7F,
            0.1F,
            160,
            0.0F,
            0.0F,
            GameplayParticleType.LAUNCH_SMOKE,
            GameplayParticleType.MISSILE_TRAIL,
            GameplayParticleType.MISSILE_IMPACT);
    private static final LegacyShipProjectileProfile BATTLESHIP_RU_HEAVY_MISSILE = new LegacyShipProjectileProfile(
            LegacyShipProjectileVisual.MISSILE,
            LegacyShipProjectileMoveType.ARC,
            0.5D,
            0.4F,
            0.1F,
            160,
            0.0F,
            0.35F,
            GameplayParticleType.LAUNCH_SMOKE,
            GameplayParticleType.MISSILE_TRAIL,
            GameplayParticleType.MISSILE_IMPACT);
    private static final LegacyShipProjectileProfile HOSTILE_SHIMAKAZE_HEAVY_MISSILE = new LegacyShipProjectileProfile(
            LegacyShipProjectileVisual.MISSILE,
            LegacyShipProjectileMoveType.DIRECT,
            0.5D,
            0.7F,
            0.1F,
            160,
            0.0F,
            0.0F,
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
            LegacyShipProjectileProfile.NONE,
            LegacyShipProjectileProfile.NONE);

    public static LegacyShipAttackProfile resolve(ShipEntitySpec spec) {
        String stem = spec.textureStem();
        boolean melee = true;
        boolean light = legacyLightAttackEnabled(spec);
        boolean heavy = legacyHeavyAttackEnabled(spec);
        boolean airLight = LEGACY_AIR_ATTACK_STEMS.contains(stem);
        boolean airHeavy = airLight;

        LegacyShipProjectileProfile lightProjectile = switch (stem) {
            case "EntitySubmU511" -> spec.hostile() ? HOSTILE_SUBMARINE_LIGHT_MISSILE : SUBMARINE_LIGHT_MISSILE;
            case "EntitySubmRo500" -> spec.hostile() ? HOSTILE_SUBMARINE_LIGHT_MISSILE : SUBMARINE_RO500_LIGHT_MISSILE;
            case "EntitySubmYo" -> SUBMARINE_YO_LIGHT_MISSILE;
            case "EntitySubmHime", "EntitySubmHimeNew" -> SUBMARINE_HIME_LIGHT_MISSILE;
            default -> LegacyShipProjectileProfile.NONE;
        };
        LegacyShipProjectileProfile heavyProjectile = heavy
                ? "EntityDestroyerShimakaze".equals(stem)
                    ? spec.hostile() ? HOSTILE_SHIMAKAZE_HEAVY_MISSILE : SHIMAKAZE_HEAVY_MISSILE
                    : "EntityBattleshipRu".equals(stem)
                        ? BATTLESHIP_RU_HEAVY_MISSILE
                        : "EntityBattleshipHime".equals(stem)
                            ? BATTLESHIP_HIME_CLUSTER_MISSILE
                            : "EntityHarbourHime".equals(stem)
                                ? HARBOUR_HIME_RAILGUN
                        : LEGACY_HEAVY_MISSILE
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

        if (spec.hostile() && spec.eggMeta() >= 2000
                && !LegacyShipBehaviorCatalog.usesLegacySpecialCarrierAirframe(spec)) {
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
                lightProjectile,
                heavyProjectile,
                airLightProjectile,
                airHeavyProjectile);
    }

    public static LegacyShipProjectileProfile legacyHeavyMissileProfile() {
        return LEGACY_HEAVY_MISSILE;
    }

    public static LegacyShipProjectileProfile carrierHimeMeleeMissileProfile() {
        return CARRIER_HIME_MELEE_MISSILE;
    }

    public static LegacyShipProjectileProfile clusterSubMissileProfile() {
        return CLUSTER_SUB_MISSILE;
    }

    /**
     * Mirrors the effective light attack channels exposed by the legacy classes.
     * The hostile Mob base overrides this query, so its registered variants keep
     * the default channel even when the friendly constructor disables it.
     */
    private static boolean legacyLightAttackEnabled(ShipEntitySpec spec) {
        return switch (spec.textureStem()) {
            case "EntityTransportWa", "EntityCarrierWo", "EntityCarrierHime" -> false;
            case "EntityCarrierKaga", "EntityCarrierAkagi" -> spec.hostile();
            default -> true;
        };
    }

    /**
     * Mirrors the effective heavy attack channels exposed by the legacy classes.
     * Battleship Ta leaves the raw flag inherited, but only implements the light
     * Rensouhou entry, so enabling a generic heavy path would add new behavior.
     */
    private static boolean legacyHeavyAttackEnabled(ShipEntitySpec spec) {
        return switch (spec.textureStem()) {
            case "EntityTransportWa", "EntityCarrierWo", "EntityCarrierHime",
                    "EntityCarrierWDemon", "EntityBattleshipTa" -> false;
            case "EntityCarrierKaga", "EntityCarrierAkagi" -> spec.hostile();
            default -> true;
        };
    }

    public boolean hasRangedAttack() {
        return this.light || this.heavy || this.airLight || this.airHeavy;
    }

    public boolean hasAirAttack() {
        return this.airLight || this.airHeavy;
    }

    public LegacyShipProjectileProfile projectileProfile(LegacyShipAttackKind attackKind) {
        return switch (attackKind) {
            case LIGHT -> this.lightProjectile;
            case HEAVY -> this.heavyProjectile;
            case AIR_LIGHT -> this.airLightProjectile;
            case AIR_HEAVY -> this.airHeavyProjectile;
            default -> LegacyShipProjectileProfile.NONE;
        };
    }
}
