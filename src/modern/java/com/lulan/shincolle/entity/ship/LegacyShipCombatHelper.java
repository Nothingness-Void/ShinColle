package com.lulan.shincolle.entity.ship;

import com.lulan.shincolle.config.ShinColleConfig;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.FlyingMob;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.WaterAnimal;
import net.minecraft.world.entity.player.Player;

public final class LegacyShipCombatHelper {

    private static final int[] BASE_ATTACK_SPEED = {40, 80, 120, 100, 100};
    private static final int[] FIXED_ATTACK_DELAY = {0, 20, 50, 35, 35};
    private static final int MAX_DAMAGE_ON_PLAYER = 59;
    private static final float[][] LEGACY_MOD_DAMAGE_DAY = {
            {0.5F, 0.5F, 0.5F, 0.5F, 1F, 0F, 0.75F},
            {1F, 1F, 1F, 1.25F, 1.5F, 0.5F, 0.75F},
            {1.25F, 1.25F, 1F, 1.5F, 2F, 0F, 0.5F},
            {1F, 1F, 1F, 1F, 1.25F, 1.25F, 1F},
            {0.5F, 0.5F, 0.5F, 0.5F, 1F, 2F, 1.5F},
            {1.5F, 1.5F, 1.25F, 1.25F, 1.5F, 1.5F, 0F},
            {1.5F, 1.5F, 1.5F, 1.75F, 2F, 0.5F, 2F}
    };
    private static final float[][] LEGACY_MOD_DAMAGE_NIGHT = {
            {0.25F, 0.25F, 0.25F, 0.25F, 0.5F, 0F, 0.5F},
            {0.75F, 0.75F, 0.75F, 1F, 1F, 0.25F, 0.5F},
            {1F, 1F, 0.75F, 1.25F, 1.75F, 0F, 0.25F},
            {0.75F, 0.75F, 0.75F, 0.75F, 1F, 1.25F, 0.75F},
            {0.5F, 0.5F, 0.5F, 0.5F, 0.75F, 1.75F, 1.25F},
            {1.5F, 1.5F, 1.25F, 1.25F, 1.5F, 1.5F, 0F},
            {0.5F, 0.5F, 0.5F, 0.75F, 0.75F, 0.25F, 1F}
    };

    private LegacyShipCombatHelper() {
    }

    public static int meleeAttackDelay(LegacyShipStats stats) {
        return attackDelay(stats, LegacyShipAttackKind.MELEE);
    }

    public static int attackDelay(LegacyShipEntity host, LegacyShipAttackKind attackKind) {
        return attackDelay(host.getLegacyStats(), host.getEquipmentBehaviorState(), attackKind);
    }

    public static int attackDelay(LegacyShipStats stats, LegacyShipAttackKind attackKind) {
        return attackDelay(stats, ShipEquipmentBehaviorState.EMPTY, attackKind);
    }

    public static int attackDelay(LegacyShipStats stats, ShipEquipmentBehaviorState behaviorState, LegacyShipAttackKind attackKind) {
        float attackSpeed = Math.max(0.01F, stats.attackSpeed());
        int baseDelay = (int) (BASE_ATTACK_SPEED[attackKind.delayType()] / attackSpeed) + FIXED_ATTACK_DELAY[attackKind.delayType()];
        return baseDelay;
    }

    /** Mirrors EntityRensouhou's copied attack-speed attribute. */
    public static int rensouhouAttackDelay(LegacyShipEntity host) {
        float attackSpeed = Math.max(0.01F, host.getLegacyStats().attackSpeed() * 0.75F - 0.25F);
        return (int) (BASE_ATTACK_SPEED[LegacyShipAttackKind.LIGHT.delayType()] / attackSpeed)
                + FIXED_ATTACK_DELAY[LegacyShipAttackKind.LIGHT.delayType()];
    }

    public static AttackRoll rollMeleeAttack(LegacyShipEntity host, LivingEntity target) {
        return rollAttack(host, target, LegacyShipAttackKind.MELEE);
    }

    public static AttackRoll rollAttack(LegacyShipEntity host, LivingEntity target, LegacyShipAttackKind attackKind) {
        return rollAttack(host, target, attackKind,
                getAttackDamage(host, target, attackKind),
                attackKind.canMiss(), attackKind.canCrit(),
                attackKind.canDoubleHit(), attackKind.canTripleHit(),
                attackKind.justLaunch());
    }

    /** Mirrors the legacy ground-target branch, which only rolls miss by distance. */
    public static boolean rollMissAtDistance(LegacyShipEntity host,
                                              LegacyShipAttackKind attackKind,
                                              float distance) {
        if (!attackKind.canMiss()) {
            return false;
        }

        LegacyShipStats stats = host.getLegacyStats();
        float miss = calcMissRate(host, stats, Math.max(0.0F, distance));
        return host.getRandom().nextFloat() <= miss;
    }

    /**
     * Preserves legacy direct attacks whose raw damage and roll flags are
     * independent from the modern projectile attack channel.
     */
    public static AttackRoll rollDirectAttack(LegacyShipEntity host, LivingEntity target, float damage) {
        return rollAttack(host, target, LegacyShipAttackKind.HEAVY, damage,
                true, true, true, true, false);
    }

    /**
     * Rolls a legacy direct follow-up using the fixed collision distance passed
     * by the old attack override instead of the host-to-target distance.
     */
    public static AttackRoll rollDirectAttack(LegacyShipEntity host, LivingEntity target,
                                              float damage, float collisionDistance) {
        return rollAttack(host, target, LegacyShipAttackKind.HEAVY, damage,
                true, true, true, true, false, Math.max(0.0F, collisionDistance));
    }

    /** Mirrors legacy direct attacks that explicitly disable double/triple hits. */
    public static AttackRoll rollNonMultiHitAttack(LegacyShipEntity host, LivingEntity target,
                                                    float damage, float collisionDistance) {
        return rollAttack(host, target, LegacyShipAttackKind.HEAVY, damage,
                true, true, false, false, false, Math.max(0.0F, collisionDistance));
    }

    /** Mirrors EntityAbyssMissile.onImpact's fixed-distance, non-multi-hit roll. */
    public static AttackRoll rollProjectileImpactAttack(LegacyShipEntity host,
                                                        LivingEntity target,
                                                        float damage) {
        return rollNonMultiHitAttack(host, target, damage, 1.0F);
    }

    private static AttackRoll rollAttack(LegacyShipEntity host, LivingEntity target,
                                         LegacyShipAttackKind attackKind, float damage,
                                         boolean canMiss, boolean canCrit,
                                         boolean canDoubleHit, boolean canTripleHit,
                                         boolean justLaunch) {
        return rollAttack(host, target, attackKind, damage,
                canMiss, canCrit, canDoubleHit, canTripleHit, justLaunch,
                host.distanceTo(target));
    }

    private static AttackRoll rollAttack(LegacyShipEntity host, LivingEntity target,
                                         LegacyShipAttackKind attackKind, float damage,
                                         boolean canMiss, boolean canCrit,
                                         boolean canDoubleHit, boolean canTripleHit,
                                         boolean justLaunch, float distance) {
        LegacyShipStats stats = host.getLegacyStats();
        float miss = canMiss
                ? calcMissRate(host, stats, distance)
                : 0F;
        float crit = canCrit ? stats.critical() + miss : miss;
        float doubleHit = canDoubleHit ? stats.doubleHit() + crit : crit;
        float tripleHit = canTripleHit ? stats.tripleHit() + doubleHit : doubleHit;
        float roll = host.getRandom().nextFloat();

        boolean isMiss = canMiss && roll <= miss;
        boolean isCrit = canCrit && !isMiss && roll <= crit;
        boolean isDoubleHit = canDoubleHit && !isMiss && !isCrit && roll <= doubleHit;
        boolean isTripleHit = canTripleHit && !isMiss && !isCrit && !isDoubleHit && roll <= tripleHit;

        if (isMiss && !justLaunch) {
            damage = 0F;
        } else if (isCrit) {
            damage *= 1.5F;
        } else if (isDoubleHit) {
            damage *= 2F;
        } else if (isTripleHit) {
            damage *= 3F;
        }

        if (target instanceof Player) {
            damage *= 0.25F;
            int maxDmg = ShinColleConfig.maxDmgOnPlayer();
            if (damage > maxDmg) {
                damage = maxDmg;
            }
        }

        return new AttackRoll(damage, isMiss, isCrit, isDoubleHit, isTripleHit);
    }

    private static float getAttackDamage(LegacyShipEntity host, LivingEntity target,
                                         LegacyShipAttackKind attackKind) {
        LegacyShipStats stats = host.getLegacyStats();
        return switch (attackKind) {
            case MELEE -> stats.meleeDamage();
            case LIGHT -> (LegacyShipBehaviorCatalog.normalizeLegacyClassId(host.getShipClassId()) == 18
                    && !host.isHostileVariant())
                    ? stats.attackLight()
                    : applyTargetDamageModifier(stats, target, stats.attackLight());
            case HEAVY -> stats.attackHeavy()
                    * LegacyShipBehaviorCatalog.heavyDamageMultiplierFor(
                            host.getShipClassId(), host.isHostileVariant());
            case AIR_LIGHT -> applyTargetDamageModifier(stats, target, stats.attackAirLight());
            case AIR_HEAVY -> stats.attackAirHeavy();
        };
    }

    public static float applyTargetDamageModifier(LegacyShipStats stats, LivingEntity target, float damage) {
        if (isFlyingTarget(target)) {
            damage += stats.antiAir();
        }

        if (isUnderseaTarget(target)) {
            damage += stats.antiSub();
        }

        return damage;
    }

    /** Mirrors the old IShipAttackBase#getDamageType mapping, including explicit hime exceptions. */
    public static int legacyDamageTypeFor(ShipEntitySpec spec) {
        if (spec == null) {
            return 0;
        }

        int classId = spec.legacyClassId();
        if (classId >= 2000) {
            classId -= 2000;
        }

        return switch (classId) {
            case 0, 1, 2, 3, 27, 36, 51, 52, 53, 54 -> 5; // destroyer
            case 9, 10, 49, 56, 57, 58, 59 -> 4; // cruiser
            case 12, 20, 33, 47, 48 -> 1; // carrier
            case 13, 14, 26, 37, 46, 60, 61, 62, 63 -> 3; // battleship
            case 15, 21, 28, 29, 30, 31 -> 2; // aviation / installation
            case 17, 18, 19, 38, 39, 44, 72 -> 6; // submarine
            case 16 -> 0; // EntityTransportWa inherits UNDEFINED
            default -> 0;
        };
    }

    /** EntityRensouhou uses DESTROYER while EntityRensouhouS uses BATTLESHIP. */
    public static int legacyDamageTypeForRensouhou(boolean largeModel) {
        return largeModel ? 3 : 5;
    }

    /**
     * Mirrors CombatHelper.modDamageByLight exactly. The legacy implementation
     * assigns its day/night table variables in the opposite order to the comments;
     * keep that runtime order for migration compatibility.
     */
    public static float modDamageByLight(float damage, int attackerType, int defenderType,
                                         float lightCoefficient) {
        if (attackerType <= 0 || defenderType <= 0
                || attackerType > LEGACY_MOD_DAMAGE_DAY.length
                || defenderType > LEGACY_MOD_DAMAGE_DAY[attackerType - 1].length) {
            return damage;
        }

        float clampedLight = Mth.clamp(lightCoefficient, 0F, 1F);
        float modDay = LEGACY_MOD_DAMAGE_NIGHT[attackerType - 1][defenderType - 1];
        float modNight = LEGACY_MOD_DAMAGE_DAY[attackerType - 1][defenderType - 1];
        return damage * (modNight + (modDay - modNight) * clampedLight);
    }

    /** Mirrors BuffHelper's target-light calculation and its un-clamped night-vision bonus. */
    public static float legacyLightCoefficientForRawBrightness(int rawBrightness, boolean nightVision) {
        float lightCoefficient = (rawBrightness - 2F) / 6F;
        lightCoefficient = Mth.clamp(lightCoefficient, 0F, 1F);
        if (nightVision) {
            lightCoefficient += 0.8F;
        }
        return lightCoefficient;
    }

    /** Applies the old ship-only light/type modifier at the modern ship damage intake entry. */
    public static float applyLegacyLightDamageModifier(LegacyShipEntity target,
                                                        DamageSource source,
                                                        float damage) {
        if (target == null || source == null) {
            return damage;
        }

        int attackerType = legacyDamageTypeForSource(source);
        int defenderType = legacyDamageTypeFor(target.getSpec());
        if (attackerType <= 0 || defenderType <= 0) {
            return damage;
        }

        Entity sourceEntity = source.getEntity();
        boolean nightVision = hasLegacyNightVision(sourceEntity, source.getDirectEntity());
        int rawBrightness = target.level().getMaxLocalRawBrightness(target.blockPosition());
        float lightCoefficient = legacyLightCoefficientForRawBrightness(rawBrightness, nightVision);
        return modDamageByLight(damage, attackerType, defenderType, lightCoefficient);
    }

    private static int legacyDamageTypeForSource(DamageSource source) {
        Entity directEntity = source.getDirectEntity();
        if (directEntity instanceof LegacyShipAircraftEntity) {
            return 7;
        }
        if (directEntity instanceof LegacyShipRensouhouEntity rensouhou) {
            return legacyDamageTypeForRensouhou(rensouhou.isLargeModel());
        }

        Entity sourceEntity = source.getEntity();
        if (sourceEntity instanceof LegacyShipRensouhouEntity rensouhou) {
            return legacyDamageTypeForRensouhou(rensouhou.isLargeModel());
        }
        if (sourceEntity instanceof LegacyShipEntity ship) {
            return legacyDamageTypeFor(ship.getSpec());
        }
        return 0;
    }

    private static boolean hasLegacyNightVision(Entity sourceEntity, Entity directEntity) {
        Entity effectEntity = sourceEntity;
        if (effectEntity instanceof LegacyShipRensouhouEntity rensouhou) {
            effectEntity = rensouhou.level().getEntity(rensouhou.getHostId());
        } else if (effectEntity instanceof LegacyShipAircraftEntity aircraft) {
            effectEntity = aircraft.level().getEntity(aircraft.getOwnerId());
        } else if (directEntity instanceof LegacyShipRensouhouEntity rensouhou) {
            effectEntity = rensouhou.level().getEntity(rensouhou.getHostId());
        } else if (directEntity instanceof LegacyShipAircraftEntity aircraft) {
            effectEntity = aircraft.level().getEntity(aircraft.getOwnerId());
        }

        return effectEntity instanceof LivingEntity living
                && living.hasEffect(MobEffects.NIGHT_VISION);
    }

    public static float applyDefenseReduction(RandomSource random, LegacyShipStats stats, float rawDamage) {
        float factor = 1F - stats.defense() + (random.nextFloat() * 0.5F - 0.25F);
        return rawDamage * Math.max(0F, factor);
    }

    public static boolean canDodge(LegacyShipEntity host, Entity attacker) {
        if (host == null || host.level().isClientSide()) {
            return false;
        }

        return host.getRandom().nextFloat() <= calculateDodgeChance(host, attacker);
    }

    public static float calculateDodgeChance(LegacyShipEntity host, Entity attacker) {
        float dodge = host.getLegacyStats().dodge();
        float invisibleLevel = LegacyShipBehaviorCatalog.invisibleLevelFor(host.getSpec());
        if (invisibleLevel > 0F && attacker != null) {
            float dodgeDistance = (1F - host.getShipLevel() * (1F + invisibleLevel) / 300F) * 64F;
            if (attacker.distanceToSqr(host) > dodgeDistance * dodgeDistance) {
                dodge += invisibleLevel;
            }
        }
        return dodge;
    }

    public static boolean isFlyingTarget(LivingEntity target) {
        return target instanceof FlyingMob
                || target.getType() == EntityType.ALLAY
                || target.getType() == EntityType.BAT
                || target.getType() == EntityType.BEE
                || target.getType() == EntityType.ENDER_DRAGON
                || target.getType() == EntityType.GHAST
                || target.getType() == EntityType.PARROT
                || target.getType() == EntityType.PHANTOM
                || target.getType() == EntityType.WITHER;
    }

    public static boolean isUnderseaTarget(LivingEntity target) {
        return target instanceof WaterAnimal
                || target.getType() == EntityType.AXOLOTL
                || target.getType() == EntityType.DROWNED
                || target.getType() == EntityType.ELDER_GUARDIAN
                || target.getType() == EntityType.GUARDIAN
                || target instanceof LegacyShipEntity ship && ship.getSpec().archetype() == ShipArchetype.SUBMARINE;
    }

    private static float calcMissRate(LegacyShipEntity host, LegacyShipStats stats, float distance) {
        float range = Math.max(1F, stats.attackRange());
        float miss;

        if (range <= 3F) {
            miss = 0.25F - 0.001F * host.getShipLevel();
        } else if (range <= 6F) {
            miss = 0.25F + 0.15F * (distance / range) - 0.001F * host.getShipLevel();
        } else {
            miss = 0.25F + 0.25F * (distance / range) - 0.001F * host.getShipLevel();
        }

        miss -= stats.missReduce();
        miss = Mth.clamp(miss, 0F, 0.5F);

        if (host.hasEffect(MobEffects.CONFUSION)) {
            miss += 0.4F;
        }

        return miss;
    }

    public record AttackRoll(float damage, boolean miss, boolean crit, boolean doubleHit, boolean tripleHit) {
    }
}
