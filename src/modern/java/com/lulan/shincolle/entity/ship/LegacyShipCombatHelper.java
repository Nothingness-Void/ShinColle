package com.lulan.shincolle.entity.ship;

import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffects;
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
    private static final int SHIP_VS_SHIP_DAMAGE_MODIFIER = 100;

    private LegacyShipCombatHelper() {
    }

    public static int meleeAttackDelay(LegacyShipStats stats) {
        return attackDelay(stats, LegacyShipAttackKind.MELEE);
    }

    public static int attackDelay(LegacyShipStats stats, LegacyShipAttackKind attackKind) {
        float attackSpeed = Math.max(0.01F, stats.attackSpeed());
        int baseDelay = (int) (BASE_ATTACK_SPEED[attackKind.delayType()] / attackSpeed) + FIXED_ATTACK_DELAY[attackKind.delayType()];
        int minimumDelay = attackKind == LegacyShipAttackKind.MELEE ? 4 : 6;
        return Math.max(minimumDelay, baseDelay);
    }

    public static AttackRoll rollMeleeAttack(LegacyShipEntity host, LivingEntity target) {
        return rollAttack(host, target, LegacyShipAttackKind.MELEE);
    }

    public static AttackRoll rollAttack(LegacyShipEntity host, LivingEntity target, LegacyShipAttackKind attackKind) {
        LegacyShipStats stats = host.getLegacyStats();
        float distance = host.distanceTo(target);
        float damage = getAttackDamage(stats, target, attackKind);
        float miss = attackKind.canMiss() ? calcMissRate(host, stats, distance) : 0F;
        float crit = attackKind.canCrit() ? stats.critical() + miss : miss;
        float doubleHit = attackKind.canDoubleHit() ? stats.doubleHit() + crit : crit;
        float tripleHit = attackKind.canTripleHit() ? stats.tripleHit() + doubleHit : doubleHit;
        float roll = host.getRandom().nextFloat();

        boolean isMiss = attackKind.canMiss() && roll <= miss;
        boolean isCrit = attackKind.canCrit() && !isMiss && roll <= crit;
        boolean isDoubleHit = attackKind.canDoubleHit() && !isMiss && !isCrit && roll <= doubleHit;
        boolean isTripleHit = attackKind.canTripleHit() && !isMiss && !isCrit && !isDoubleHit && roll <= tripleHit;

        if (isMiss && !attackKind.justLaunch()) {
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
            if (damage > MAX_DAMAGE_ON_PLAYER) {
                damage = MAX_DAMAGE_ON_PLAYER;
            }
        }

        if (target instanceof LegacyShipEntity) {
            damage *= SHIP_VS_SHIP_DAMAGE_MODIFIER / 100F;
        }

        return new AttackRoll(damage, isMiss, isCrit, isDoubleHit, isTripleHit);
    }

    private static float getAttackDamage(LegacyShipStats stats, LivingEntity target, LegacyShipAttackKind attackKind) {
        return switch (attackKind) {
            case MELEE -> stats.meleeDamage();
            case LIGHT -> applyTargetDamageModifier(stats, target, stats.attackLight());
            case HEAVY -> stats.attackHeavy();
            case AIR_LIGHT -> stats.attackAirLight();
            case AIR_HEAVY -> stats.attackAirHeavy();
        };
    }

    private static float applyTargetDamageModifier(LegacyShipStats stats, LivingEntity target, float damage) {
        if (isFlyingTarget(target)) {
            damage += stats.antiAir();
        }

        if (isUnderseaTarget(target)) {
            damage += stats.antiSub();
        }

        return damage;
    }

    public static float applyDefenseReduction(RandomSource random, LegacyShipStats stats, float rawDamage) {
        float factor = 1F - stats.defense() + (random.nextFloat() * 0.5F - 0.25F);
        return rawDamage * Math.max(0F, factor);
    }

    public static boolean canDodge(LegacyShipEntity host, Entity attacker) {
        LegacyShipStats stats = host.getLegacyStats();
        float dodge = stats.dodge();

        if (dodge <= 0F) {
            return false;
        }

        if (attacker != null && attacker.distanceToSqr(host) > stats.attackRange() * stats.attackRange()) {
            dodge += 0.05F;
        }

        return host.getRandom().nextFloat() <= Mth.clamp(dodge, 0F, 0.95F);
    }

    private static boolean isFlyingTarget(LivingEntity target) {
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

    private static boolean isUnderseaTarget(LivingEntity target) {
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
