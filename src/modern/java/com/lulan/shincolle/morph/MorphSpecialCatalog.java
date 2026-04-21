package com.lulan.shincolle.morph;

import com.lulan.shincolle.entity.projectile.LegacyShipProjectileVisual;
import com.lulan.shincolle.entity.ship.LegacyShipAttackKind;
import com.lulan.shincolle.entity.ship.LegacyShipBehaviorCatalog;
import com.lulan.shincolle.entity.ship.LegacyShipCombatHelper;
import com.lulan.shincolle.entity.ship.LegacyShipStats;
import com.lulan.shincolle.network.CombatFxDispatcher;
import com.lulan.shincolle.network.CombatReactType;
import com.lulan.shincolle.network.GameplayParticleType;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

final class MorphSpecialCatalog {

    private MorphSpecialCatalog() {
    }

    static boolean hasSpecial(@Nullable MorphProfile profile) {
        return resolve(profile) != null;
    }

    static int previewCooldown(@Nullable MorphProfile profile) {
        Behavior behavior = resolve(profile);
        return behavior == null ? 0 : behavior.cooldown();
    }

    static int cast(ServerPlayer player, MorphProfile profile, LivingEntity primaryTarget) {
        Behavior behavior = resolve(profile);
        if (behavior == null || !MorphHelper.canEngage(player, profile, primaryTarget)) {
            return 0;
        }
        return behavior.cast(player, profile, primaryTarget) ? behavior.cooldown() : 0;
    }

    static @Nullable LivingEntity findNearestTarget(ServerPlayer player, MorphProfile profile, double range) {
        LivingEntity bestTarget = null;
        double bestDistance = range * range;

        for (LivingEntity candidate : player.level().getEntitiesOfClass(LivingEntity.class,
                new AABB(player.blockPosition()).inflate(range, 3.0D, range))) {
            if (!MorphHelper.canEngage(player, profile, candidate)) {
                continue;
            }

            double distance = player.distanceToSqr(candidate);
            if (distance < bestDistance) {
                bestDistance = distance;
                bestTarget = candidate;
            }
        }

        return bestTarget;
    }

    private static @Nullable Behavior resolve(@Nullable MorphProfile profile) {
        if (profile == null) {
            return null;
        }

        LegacyShipBehaviorCatalog.CombatHook hook =
                LegacyShipBehaviorCatalog.combatHookForLegacyClassId(profile.getLegacyClassId());
        int cooldown = LegacyShipBehaviorCatalog.morphSpecialCooldown(hook);
        if (cooldown <= 0) {
            return null;
        }

        return switch (hook) {
            case SHIMAKAZE_TORPEDO_BURST -> new Behavior(cooldown, MorphSpecialCatalog::performShimakazeSpecial);
            case NAGATO_HEAVY_STRIKE -> new Behavior(cooldown, MorphSpecialCatalog::performNagatoSpecial);
            case YAMATO_BEAM_BARRAGE -> new Behavior(cooldown, MorphSpecialCatalog::performYamatoSpecial);
            case TENRYUU_DASH -> new Behavior(cooldown, MorphSpecialCatalog::performTenryuuSpecial);
            case TATSUTA_CONTROL_AOE -> new Behavior(cooldown, MorphSpecialCatalog::performTatsutaSpecial);
            case HEAVY_CRUISER_BARRAGE -> new Behavior(cooldown, MorphSpecialCatalog::performHeavyCruiserSpecial);
            case KONGOU_COMBO -> new Behavior(cooldown, MorphSpecialCatalog::performKongouClassSpecial);
            case NONE -> null;
        };
    }

    private static boolean performShimakazeSpecial(ServerPlayer player, MorphProfile profile, LivingEntity primaryTarget) {
        List<LivingEntity> targets = collectSpecialTargets(player, profile, primaryTarget, 6.0D, 5);
        boolean hit = false;
        for (int index = 0; index < 5; index++) {
            LivingEntity target = targets.get(Math.min(index, targets.size() - 1));
            sendTorpedoWake(player, target);
            hit |= applySpecialAttack(player, profile, target, LegacyShipAttackKind.HEAVY,
                    target == primaryTarget ? 0.5F : 0.38F, false,
                    GameplayParticleType.TORPEDO_IMPACT, LegacyShipProjectileVisual.TORPEDO);
        }

        CombatFxDispatcher.sendParticle(player, GameplayParticleType.LAUNCH_SMOKE,
                player.getX(), player.getY() + player.getBbHeight() * 0.7D, player.getZ());
        return hit;
    }

    private static boolean performNagatoSpecial(ServerPlayer player, MorphProfile profile, LivingEntity primaryTarget) {
        boolean hit = applySpecialAttack(player, profile, primaryTarget, LegacyShipAttackKind.HEAVY,
                2.0F, true, GameplayParticleType.HIT_EXPLOSION, LegacyShipProjectileVisual.MISSILE);

        List<LivingEntity> targets = collectSpecialTargets(player, profile, primaryTarget, 3.5D, 8);
        for (LivingEntity target : targets) {
            if (target == primaryTarget) {
                continue;
            }
            boolean attacked = applySpecialAttack(player, profile, target, LegacyShipAttackKind.HEAVY,
                    0.72F, false, GameplayParticleType.MISSILE_IMPACT, LegacyShipProjectileVisual.MISSILE);
            if (attacked) {
                target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 35, 0, false, true, true));
                hit = true;
            }
        }

        CombatFxDispatcher.sendParticle(player, GameplayParticleType.LAUNCH_SMOKE,
                player.getX(), player.getY() + player.getBbHeight() * 0.7D, player.getZ());
        CombatFxDispatcher.sendParticle(primaryTarget, GameplayParticleType.HIT_EXPLOSION,
                primaryTarget.getX(), primaryTarget.getY() + primaryTarget.getBbHeight() * 0.55D, primaryTarget.getZ());
        return hit;
    }

    private static boolean performYamatoSpecial(ServerPlayer player, MorphProfile profile, LivingEntity primaryTarget) {
        List<LivingEntity> targets = collectLineTargets(player, profile, primaryTarget, 18.0D, 1.7D, 6);
        boolean hit = false;
        for (LivingEntity target : targets) {
            CombatFxDispatcher.sendParticle(target, GameplayParticleType.SEARCHLIGHT_MARK,
                    target.getX(), target.getY() + target.getBbHeight() * 0.85D, target.getZ());
            hit |= applySpecialAttack(player, profile, target, LegacyShipAttackKind.HEAVY,
                    target == primaryTarget ? 2.25F : 1.1F, true,
                    GameplayParticleType.MISSILE_IMPACT, LegacyShipProjectileVisual.MISSILE);
        }

        CombatFxDispatcher.sendParticle(player, GameplayParticleType.LAUNCH_SMOKE,
                player.getX(), player.getY() + player.getBbHeight() * 0.7D, player.getZ());
        CombatFxDispatcher.sendParticle(primaryTarget, GameplayParticleType.SEARCHLIGHT_MARK,
                primaryTarget.getX(), primaryTarget.getY() + primaryTarget.getBbHeight(), primaryTarget.getZ());
        return hit;
    }

    private static boolean performTenryuuSpecial(ServerPlayer player, MorphProfile profile, LivingEntity primaryTarget) {
        Vec3 dash = primaryTarget.position().subtract(player.position());
        if (dash.lengthSqr() > 1.0E-4D) {
            Vec3 motion = dash.normalize().scale(0.9D);
            player.push(motion.x, 0.18D, motion.z);
            player.hurtMarked = true;
        }

        List<LivingEntity> targets = collectSpecialTargets(player, profile, primaryTarget, 3.5D, 6);
        boolean hit = false;
        for (LivingEntity target : targets) {
            hit |= applySpecialAttack(player, profile, target, LegacyShipAttackKind.MELEE,
                    1.55F, false, GameplayParticleType.HIT_EXPLOSION, LegacyShipProjectileVisual.NONE);
        }

        CombatFxDispatcher.sendParticle(player, GameplayParticleType.LAUNCH_SMOKE,
                player.getX(), player.getY() + player.getBbHeight() * 0.7D, player.getZ());
        return hit;
    }

    private static boolean performTatsutaSpecial(ServerPlayer player, MorphProfile profile, LivingEntity primaryTarget) {
        List<LivingEntity> targets = collectSpecialTargets(player, profile, primaryTarget, 4.5D, 8);
        boolean hit = false;
        for (LivingEntity target : targets) {
            boolean attacked = applySpecialAttack(player, profile, target, LegacyShipAttackKind.LIGHT,
                    1.35F, true, GameplayParticleType.HIT_EXPLOSION, LegacyShipProjectileVisual.NONE);
            if (attacked) {
                target.addEffect(new MobEffectInstance(MobEffects.LEVITATION, 30, 0, false, true, true));
                hit = true;
            }
        }

        CombatFxDispatcher.sendParticle(primaryTarget, GameplayParticleType.HIT_EXPLOSION,
                primaryTarget.getX(), primaryTarget.getY() + primaryTarget.getBbHeight() * 0.5D, primaryTarget.getZ());
        return hit;
    }

    private static boolean performHeavyCruiserSpecial(ServerPlayer player, MorphProfile profile, LivingEntity primaryTarget) {
        List<LivingEntity> targets = collectSpecialTargets(player, profile, primaryTarget, 5.5D, 4);
        boolean hit = false;
        for (LivingEntity target : targets) {
            boolean attacked = applySpecialAttack(player, profile, target, LegacyShipAttackKind.HEAVY,
                    target == primaryTarget ? 1.55F : 1.15F, true,
                    GameplayParticleType.MISSILE_IMPACT, LegacyShipProjectileVisual.MISSILE);
            if (attacked) {
                target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 40, 0, false, true, true));
                hit = true;
            }
        }

        CombatFxDispatcher.sendParticle(primaryTarget, GameplayParticleType.MISSILE_IMPACT,
                primaryTarget.getX(), primaryTarget.getY() + primaryTarget.getBbHeight() * 0.5D, primaryTarget.getZ());
        return hit;
    }

    private static boolean performKongouClassSpecial(ServerPlayer player, MorphProfile profile, LivingEntity primaryTarget) {
        List<LivingEntity> targets = collectSpecialTargets(player, profile, primaryTarget, 7.0D, 5);
        boolean hit = false;
        for (int index = 0; index < targets.size(); index++) {
            LivingEntity target = targets.get(index);
            boolean heavyHit = applySpecialAttack(player, profile, target, LegacyShipAttackKind.HEAVY,
                    index == 0 ? 1.7F : 1.25F, true,
                    GameplayParticleType.MISSILE_IMPACT, LegacyShipProjectileVisual.MISSILE);
            boolean lightHit = applySpecialAttack(player, profile, target, LegacyShipAttackKind.LIGHT,
                    index == 0 ? 1.2F : 0.95F, false,
                    GameplayParticleType.HIT_EXPLOSION, LegacyShipProjectileVisual.NONE);
            hit |= heavyHit || lightHit;
        }

        CombatFxDispatcher.sendParticle(player, GameplayParticleType.LAUNCH_SMOKE,
                player.getX(), player.getY() + player.getBbHeight() * 0.7D, player.getZ());
        CombatFxDispatcher.sendParticle(primaryTarget, GameplayParticleType.MISSILE_IMPACT,
                primaryTarget.getX(), primaryTarget.getY() + primaryTarget.getBbHeight() * 0.55D, primaryTarget.getZ());
        return hit;
    }

    private static List<LivingEntity> collectSpecialTargets(ServerPlayer player, MorphProfile profile,
                                                            LivingEntity primaryTarget, double radius, int maxTargets) {
        List<LivingEntity> targets = new ArrayList<>();
        targets.add(primaryTarget);

        for (LivingEntity candidate : player.level().getEntitiesOfClass(LivingEntity.class,
                primaryTarget.getBoundingBox().inflate(radius, radius * 0.5D, radius))) {
            if (candidate == primaryTarget || !MorphHelper.canEngage(player, profile, candidate)) {
                continue;
            }
            targets.add(candidate);
            if (targets.size() >= maxTargets) {
                break;
            }
        }

        return targets;
    }

    private static List<LivingEntity> collectLineTargets(ServerPlayer player, MorphProfile profile,
                                                         LivingEntity primaryTarget, double range,
                                                         double radius, int maxTargets) {
        Vec3 origin = centerOf(player);
        Vec3 primaryOffset = centerOf(primaryTarget).subtract(origin);
        if (primaryOffset.lengthSqr() <= 1.0E-4D) {
            return List.of(primaryTarget);
        }

        Vec3 direction = primaryOffset.normalize();
        List<ScoredTarget> scoredTargets = new ArrayList<>();
        AABB searchArea = new AABB(player.blockPosition()).inflate(range, range * 0.5D, range);
        for (LivingEntity candidate : player.level().getEntitiesOfClass(LivingEntity.class, searchArea)) {
            if (candidate == primaryTarget || !MorphHelper.canEngage(player, profile, candidate)) {
                continue;
            }

            Vec3 offset = centerOf(candidate).subtract(origin);
            double projection = offset.dot(direction);
            if (projection < 0.0D || projection > range) {
                continue;
            }

            double allowedRadius = radius + candidate.getBbWidth() * 0.5D;
            double perpendicularDistanceSqr = offset.subtract(direction.scale(projection)).lengthSqr();
            if (perpendicularDistanceSqr <= allowedRadius * allowedRadius) {
                scoredTargets.add(new ScoredTarget(candidate, projection));
            }
        }

        scoredTargets.sort(Comparator.comparingDouble(ScoredTarget::projection));
        List<LivingEntity> targets = new ArrayList<>();
        targets.add(primaryTarget);
        for (ScoredTarget scoredTarget : scoredTargets) {
            targets.add(scoredTarget.target());
            if (targets.size() >= maxTargets) {
                break;
            }
        }
        return targets;
    }

    private static boolean applySpecialAttack(ServerPlayer player, MorphProfile profile, LivingEntity target,
                                              LegacyShipAttackKind attackKind, float damageMultiplier,
                                              boolean illuminate, GameplayParticleType hitParticleType,
                                              LegacyShipProjectileVisual projectileVisual) {
        LegacyShipStats stats = profile.buildStats(player.getActiveEffects());
        LegacyShipCombatHelper.AttackRoll roll = MorphHelper.rollMorphAttack(player, profile, stats, target, attackKind);
        float damage = roll.damage() * damageMultiplier;
        LegacyShipCombatHelper.AttackRoll adjustedRoll = new LegacyShipCombatHelper.AttackRoll(
                damage, roll.miss(), roll.crit(), roll.doubleHit(), roll.tripleHit());

        if (illuminate) {
            target.addEffect(new MobEffectInstance(MobEffects.GLOWING, 60, 0, false, true, true));
        }

        if (adjustedRoll.miss()) {
            emitSpecialAttackFx(player, target, attackKind, adjustedRoll, false, hitParticleType, projectileVisual);
            return false;
        }

        boolean attacked = target.hurt(player.damageSources().playerAttack(player), adjustedRoll.damage());
        if (attacked) {
            player.setLastHurtMob(target);
        }
        emitSpecialAttackFx(player, target, attackKind, adjustedRoll, attacked, hitParticleType, projectileVisual);
        return attacked;
    }

    private static void emitSpecialAttackFx(ServerPlayer player, LivingEntity target, LegacyShipAttackKind attackKind,
                                            LegacyShipCombatHelper.AttackRoll roll, boolean attacked,
                                            GameplayParticleType hitParticleType,
                                            LegacyShipProjectileVisual projectileVisual) {
        if (roll.miss()) {
            CombatFxDispatcher.sendCombatReact(player, target, CombatReactType.MISS, attackKind, projectileVisual);
            CombatFxDispatcher.sendParticle(target, GameplayParticleType.TEXT_MISS,
                    target.getX(), target.getY() + target.getBbHeight() * 0.8D, target.getZ());
            return;
        }

        if (attackKind.justLaunch()) {
            CombatFxDispatcher.sendCombatReact(player, target, CombatReactType.LAUNCH, attackKind, projectileVisual);
            CombatFxDispatcher.sendParticle(player, GameplayParticleType.LAUNCH_SMOKE,
                    player.getX(), player.getY() + player.getBbHeight() * 0.7D, player.getZ());
        }

        if (roll.crit()) {
            CombatFxDispatcher.sendCombatReact(player, target, CombatReactType.CRIT, attackKind, projectileVisual);
            CombatFxDispatcher.sendParticle(target, GameplayParticleType.TEXT_CRIT,
                    target.getX(), target.getY() + target.getBbHeight() * 0.8D, target.getZ());
        } else if (roll.tripleHit()) {
            CombatFxDispatcher.sendCombatReact(player, target, CombatReactType.TRIPLE_HIT, attackKind, projectileVisual);
            CombatFxDispatcher.sendParticle(target, GameplayParticleType.TEXT_TRIPLE,
                    target.getX(), target.getY() + target.getBbHeight() * 0.8D, target.getZ());
        } else if (roll.doubleHit()) {
            CombatFxDispatcher.sendCombatReact(player, target, CombatReactType.DOUBLE_HIT, attackKind, projectileVisual);
            CombatFxDispatcher.sendParticle(target, GameplayParticleType.TEXT_DOUBLE,
                    target.getX(), target.getY() + target.getBbHeight() * 0.8D, target.getZ());
        }

        if (attacked) {
            CombatFxDispatcher.sendCombatReact(player, target, CombatReactType.HIT, attackKind, projectileVisual);
            if (hitParticleType != GameplayParticleType.NONE) {
                CombatFxDispatcher.sendParticle(target, hitParticleType,
                        target.getX(), target.getY() + target.getBbHeight() * 0.5D, target.getZ());
            }
        }
    }

    private static void sendTorpedoWake(ServerPlayer player, LivingEntity target) {
        Vec3 velocity = centerOf(target).subtract(centerOf(player));
        if (velocity.lengthSqr() > 1.0E-4D) {
            velocity = velocity.normalize().scale(0.18D);
        } else {
            velocity = Vec3.ZERO;
        }

        CombatFxDispatcher.sendParticle(player, GameplayParticleType.TORPEDO_WAKE,
                player.getX(), player.getY() + 0.2D, player.getZ(), velocity.x, 0.0D, velocity.z);
    }

    private static Vec3 centerOf(LivingEntity entity) {
        return entity.position().add(0.0D, entity.getBbHeight() * 0.5D, 0.0D);
    }

    @FunctionalInterface
    private interface CastAction {

        boolean cast(ServerPlayer player, MorphProfile profile, LivingEntity primaryTarget);
    }

    private record Behavior(int cooldown, CastAction castAction) {

        boolean cast(ServerPlayer player, MorphProfile profile, LivingEntity primaryTarget) {
            return this.castAction.cast(player, profile, primaryTarget);
        }
    }

    private record ScoredTarget(LivingEntity target, double projection) {
    }
}
