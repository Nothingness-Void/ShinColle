package com.lulan.shincolle.entity.ship;

import com.lulan.shincolle.entity.projectile.LegacyShipProjectileVisual;
import com.lulan.shincolle.network.CombatFxDispatcher;
import com.lulan.shincolle.network.GameplayParticleType;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Central dispatch point for ported 1.12.2 per-ship behavior.
 */
public final class LegacyShipBehaviorCatalog {

    public enum AiPriority {
        DEATH_STOP_SIT,
        EXPLICIT_COMMAND,
        ACTIVE_COMBAT_TARGET,
        AUTO_SUPPLY,
        PICKUP,
        IDLE_FOLLOW
    }

    public enum MarriagePassive {
        NONE,
        SELF_AND_OWNER_INVISIBILITY,
        ALLIED_JUMP_AURA,
        OWNER_HASTE,
        OWNER_JUMP,
        OWNER_STRENGTH,
        OWNER_SPEED
    }

    public enum CombatHook {
        NONE,
        SHIMAKAZE_TORPEDO_BURST,
        NAGATO_HEAVY_STRIKE,
        YAMATO_BEAM_BARRAGE,
        TENRYUU_DASH,
        TATSUTA_CONTROL_AOE,
        HEAVY_CRUISER_BARRAGE,
        KONGOU_COMBO
    }

    public record HostileLootProfile(boolean dropsAbyssMetal,
                                     boolean dropsAbyssMetal1,
                                     boolean dropsBossBonus,
                                     float eggDropChance) {
    }

    public record Behavior(int legacyClassId, boolean hostile, MarriagePassive marriagePassive, CombatHook combatHook) {
        public boolean hasMarriagePassive() {
            return this.marriagePassive != MarriagePassive.NONE;
        }

        public boolean hasCombatHook() {
            return this.combatHook != CombatHook.NONE;
        }
    }

    private LegacyShipBehaviorCatalog() {
    }

    public static Behavior behaviorFor(ShipEntitySpec spec) {
        return behaviorFor(spec.legacyClassId(), spec.hostile());
    }

    public static Behavior behaviorFor(int legacyClassId, boolean hostile) {
        return new Behavior(
                legacyClassId,
                hostile,
                hostile ? MarriagePassive.NONE : marriagePassiveForFriendlyClassId(legacyClassId),
                combatHookForClassId(legacyClassId));
    }

    public static LegacyShipAttackProfile attackProfile(ShipEntitySpec spec) {
        return LegacyShipAttackProfile.resolve(spec);
    }

    public static boolean hasCombatHook(ShipEntitySpec spec) {
        return behaviorFor(spec).hasCombatHook();
    }

    public static boolean shouldUseCombatHook(ShipEntitySpec spec, LegacyShipAttackKind attackKind) {
        return attackKind == LegacyShipAttackKind.HEAVY && hasCombatHook(spec);
    }

    public static List<AiPriority> singlePlayerAiPriorityOrder() {
        return List.of(
                AiPriority.DEATH_STOP_SIT,
                AiPriority.EXPLICIT_COMMAND,
                AiPriority.ACTIVE_COMBAT_TARGET,
                AiPriority.AUTO_SUPPLY,
                AiPriority.PICKUP,
                AiPriority.IDLE_FOLLOW);
    }

    public static boolean performCombatHook(LegacyShipEntity ship, LivingEntity target, LegacyShipAttackKind attackKind) {
        Behavior behavior = behaviorFor(ship.getSpec());
        if (attackKind != LegacyShipAttackKind.HEAVY
                || !behavior.hasCombatHook()
                || ship.getCompatAttackCooldown(LegacyShipAttackKind.HEAVY) > 0
                || !ship.canEngage(target)) {
            return false;
        }

        boolean acted = switch (behavior.combatHook()) {
            case SHIMAKAZE_TORPEDO_BURST -> performShimakazeBurst(ship, target);
            case NAGATO_HEAVY_STRIKE -> performNagatoStrike(ship, target);
            case YAMATO_BEAM_BARRAGE -> performYamatoBeam(ship, target);
            case TENRYUU_DASH -> performTenryuuDash(ship, target);
            case TATSUTA_CONTROL_AOE -> performTatsutaControl(ship, target);
            case HEAVY_CRUISER_BARRAGE -> performHeavyCruiserBarrage(ship, target);
            case KONGOU_COMBO -> performKongouCombo(ship, target);
            case NONE -> false;
        };

        if (acted) {
            int baseDelay = LegacyShipCombatHelper.attackDelay(ship, LegacyShipAttackKind.HEAVY);
            ship.setCatalogAttackCooldown(LegacyShipAttackKind.HEAVY,
                    Math.max(8, Math.round(baseDelay * cooldownMultiplier(behavior.combatHook()))));
        }
        return acted;
    }

    public static BossPhaseProfile bossPhaseProfile(ShipEntitySpec spec) {
        int legacyClassId = spec.legacyClassId();
        if (spec.archetype() == ShipArchetype.INSTALLATION) {
            int summonEggMeta = legacyClassId == 21 ? 14 : 18;
            return new BossPhaseProfile(
                    List.of(BossActionType.AREA_BOMBARD, BossActionType.SUMMON_ESCORT, BossActionType.CANNON_BURST),
                    summonEggMeta,
                    40,
                    160);
        }

        if (legacyClassId == 30 || legacyClassId == 33 || legacyClassId == 49) {
            return new BossPhaseProfile(
                    List.of(BossActionType.AIR_ASSAULT, BossActionType.SUMMON_ESCORT, BossActionType.CANNON_BURST),
                    14,
                    34,
                    140);
        }

        if (spec.archetype() == ShipArchetype.PRINCESS) {
            int summonEggMeta = legacyClassId == 44 ? 19 : 2;
            return new BossPhaseProfile(
                    List.of(BossActionType.CANNON_BURST, BossActionType.CHARGE, BossActionType.SUMMON_ESCORT),
                    summonEggMeta,
                    36,
                    180);
        }

        return new BossPhaseProfile(
                List.of(BossActionType.CANNON_BURST, BossActionType.CHARGE),
                2,
                54,
                220);
    }

    public static HostileLootProfile hostileLootProfile(ShipEntitySpec spec, boolean elite, boolean boss) {
        boolean dropsAbyssMetal = elite
                || spec.archetype() == ShipArchetype.CARRIER
                || spec.archetype() == ShipArchetype.BATTLESHIP;
        boolean dropsAbyssMetal1 = spec.archetype() == ShipArchetype.INSTALLATION || boss;
        float eggDropChance = boss ? 0.9F : elite ? 0.33F : 0.2F;
        return new HostileLootProfile(dropsAbyssMetal, dropsAbyssMetal1, boss, eggDropChance);
    }

    public static int hostileSpawnLevel(ShipEntitySpec spec, boolean elite, boolean boss) {
        if (!spec.hostile()) {
            return 1;
        }

        int base = switch (spec.archetype()) {
            case DESTROYER, SUBMARINE -> 18;
            case TRANSPORT, CRUISER -> 24;
            case CARRIER, BATTLESHIP -> 32;
            case PRINCESS -> 48;
            case INSTALLATION -> 52;
        };
        if (elite) {
            base += 12;
        }
        if (boss) {
            base += 28;
        }
        if (spec.eggMeta() >= 2000) {
            base += 8;
        }
        return Math.max(1, Math.min(100, base));
    }

    public static int hostileSpawnMorale(boolean elite, boolean boss) {
        if (boss) {
            return 9000;
        }
        if (elite) {
            return 7000;
        }
        return 5200;
    }

    public static void tickMarriagePassive(LegacyShipEntity ship) {
        Behavior behavior = behaviorFor(ship.getSpec());
        if (!behavior.hasMarriagePassive()) {
            return;
        }

        int shipLevel = ship.getShipLevel();
        switch (behavior.marriagePassive()) {
            case SELF_AND_OWNER_INVISIBILITY -> {
                ship.applyLegacyRingEffect(ship, MobEffects.INVISIBILITY, 40 + shipLevel, 0);
                ship.applyOwnerRingEffect(MobEffects.INVISIBILITY, 40 + shipLevel, 0);
            }
            case ALLIED_JUMP_AURA -> ship.applyCarrierRingAura(50 + shipLevel, shipLevel / 85);
            case OWNER_HASTE -> ship.applyOwnerRingEffect(MobEffects.DIG_SPEED, 80 + shipLevel, shipLevel / 30);
            case OWNER_JUMP -> ship.applyOwnerRingEffect(MobEffects.JUMP, 80 + shipLevel, shipLevel / 45 + 1);
            case OWNER_STRENGTH -> ship.applyOwnerRingEffect(MobEffects.DAMAGE_BOOST, 80 + shipLevel, shipLevel / 50);
            case OWNER_SPEED -> ship.applyOwnerRingEffect(MobEffects.MOVEMENT_SPEED, 80 + shipLevel, shipLevel / 45);
            case NONE -> {
            }
        }
    }

    public static CombatHook combatHookForLegacyClassId(int legacyClassId) {
        return combatHookForClassId(legacyClassId);
    }

    public static int morphSpecialCooldown(CombatHook hook) {
        return switch (hook) {
            case SHIMAKAZE_TORPEDO_BURST -> 130;
            case NAGATO_HEAVY_STRIKE -> 170;
            case YAMATO_BEAM_BARRAGE -> 200;
            case TENRYUU_DASH -> 120;
            case TATSUTA_CONTROL_AOE -> 120;
            case HEAVY_CRUISER_BARRAGE -> 140;
            case KONGOU_COMBO -> 150;
            case NONE -> 0;
        };
    }

    private static MarriagePassive marriagePassiveForFriendlyClassId(int legacyClassId) {
        return switch (legacyClassId) {
            case 38, 39 -> MarriagePassive.SELF_AND_OWNER_INVISIBILITY;
            case 47, 48 -> MarriagePassive.ALLIED_JUMP_AURA;
            case 51 -> MarriagePassive.OWNER_HASTE;
            case 52 -> MarriagePassive.OWNER_JUMP;
            case 53 -> MarriagePassive.OWNER_STRENGTH;
            case 54 -> MarriagePassive.OWNER_SPEED;
            default -> MarriagePassive.NONE;
        };
    }

    private static CombatHook combatHookForClassId(int legacyClassId) {
        int normalizedClassId = legacyClassId >= 2000 ? legacyClassId - 2000 : legacyClassId;
        return switch (normalizedClassId) {
            case 36 -> CombatHook.SHIMAKAZE_TORPEDO_BURST;
            case 37 -> CombatHook.NAGATO_HEAVY_STRIKE;
            case 46 -> CombatHook.YAMATO_BEAM_BARRAGE;
            case 56 -> CombatHook.TENRYUU_DASH;
            case 57 -> CombatHook.TATSUTA_CONTROL_AOE;
            case 58, 59 -> CombatHook.HEAVY_CRUISER_BARRAGE;
            case 60, 61, 62, 63 -> CombatHook.KONGOU_COMBO;
            default -> CombatHook.NONE;
        };
    }

    private static float cooldownMultiplier(CombatHook hook) {
        return switch (hook) {
            case SHIMAKAZE_TORPEDO_BURST -> 1.2F;
            case NAGATO_HEAVY_STRIKE -> 1.3F;
            case YAMATO_BEAM_BARRAGE -> 1.5F;
            case TENRYUU_DASH -> 0.9F;
            case TATSUTA_CONTROL_AOE -> 1.0F;
            case HEAVY_CRUISER_BARRAGE -> 1.1F;
            case KONGOU_COMBO -> 1.25F;
            case NONE -> 1.0F;
        };
    }

    private static boolean performShimakazeBurst(LegacyShipEntity ship, LivingEntity primaryTarget) {
        List<LivingEntity> targets = collectTargets(ship, primaryTarget, 6.0D, 5);
        boolean acted = false;
        for (int index = 0; index < 5; index++) {
            LivingEntity target = targets.get(Math.min(index, targets.size() - 1));
            CombatFxDispatcher.sendParticle(ship, GameplayParticleType.TORPEDO_WAKE,
                    target.getX(), target.getY() + 0.08D, target.getZ());
            acted |= ship.performCatalogSpecialAttack(target, LegacyShipAttackKind.HEAVY,
                    target == primaryTarget ? 0.42F : 0.32F,
                    false,
                    LegacyShipProjectileVisual.TORPEDO,
                    GameplayParticleType.TORPEDO_IMPACT);
        }
        sendLaunchSmoke(ship);
        return acted;
    }

    private static boolean performNagatoStrike(LegacyShipEntity ship, LivingEntity primaryTarget) {
        boolean acted = ship.performCatalogSpecialAttack(primaryTarget, LegacyShipAttackKind.HEAVY,
                1.85F,
                true,
                LegacyShipProjectileVisual.MISSILE,
                GameplayParticleType.HIT_EXPLOSION);
        CombatFxDispatcher.sendParticle(primaryTarget, GameplayParticleType.HIT_EXPLOSION,
                primaryTarget.getX(), primaryTarget.getY() + primaryTarget.getBbHeight() * 0.5D, primaryTarget.getZ());
        sendLaunchSmoke(ship);
        return acted;
    }

    private static boolean performYamatoBeam(LegacyShipEntity ship, LivingEntity primaryTarget) {
        List<LivingEntity> targets = collectLineTargets(ship, primaryTarget, 18.0D, 1.8D, 6);
        boolean acted = false;
        for (LivingEntity target : targets) {
            CombatFxDispatcher.sendParticle(target, GameplayParticleType.SEARCHLIGHT_MARK,
                    target.getX(), target.getY() + target.getBbHeight() * 0.85D, target.getZ());
            acted |= ship.performCatalogSpecialAttack(target, LegacyShipAttackKind.HEAVY,
                    target == primaryTarget ? 2.05F : 1.05F,
                    true,
                    LegacyShipProjectileVisual.MISSILE,
                    GameplayParticleType.MISSILE_IMPACT);
        }
        sendLaunchSmoke(ship);
        return acted;
    }

    private static boolean performTenryuuDash(LegacyShipEntity ship, LivingEntity primaryTarget) {
        Vec3 dash = primaryTarget.position().subtract(ship.position());
        if (dash.lengthSqr() > 1.0E-4D) {
            Vec3 motion = dash.normalize().scale(0.65D);
            ship.push(motion.x, 0.12D, motion.z);
            ship.hurtMarked = true;
        }

        boolean acted = false;
        for (LivingEntity target : collectTargets(ship, primaryTarget, 3.5D, 6)) {
            acted |= ship.performCatalogSpecialAttack(target, LegacyShipAttackKind.MELEE,
                    1.35F,
                    false,
                    LegacyShipProjectileVisual.NONE,
                    GameplayParticleType.HIT_EXPLOSION);
        }
        sendLaunchSmoke(ship);
        return acted;
    }

    private static boolean performTatsutaControl(LegacyShipEntity ship, LivingEntity primaryTarget) {
        boolean acted = false;
        for (LivingEntity target : collectTargets(ship, primaryTarget, 4.5D, 8)) {
            boolean hit = ship.performCatalogSpecialAttack(target, LegacyShipAttackKind.LIGHT,
                    1.18F,
                    true,
                    LegacyShipProjectileVisual.NONE,
                    GameplayParticleType.HIT_EXPLOSION);
            if (hit) {
                target.addEffect(new MobEffectInstance(MobEffects.LEVITATION, 30, 0, false, true, true));
                acted = true;
            }
        }
        CombatFxDispatcher.sendParticle(primaryTarget, GameplayParticleType.HIT_EXPLOSION,
                primaryTarget.getX(), primaryTarget.getY() + primaryTarget.getBbHeight() * 0.5D, primaryTarget.getZ());
        return acted;
    }

    private static boolean performHeavyCruiserBarrage(LegacyShipEntity ship, LivingEntity primaryTarget) {
        boolean acted = false;
        for (LivingEntity target : collectTargets(ship, primaryTarget, 5.5D, 4)) {
            boolean hit = ship.performCatalogSpecialAttack(target, LegacyShipAttackKind.HEAVY,
                    target == primaryTarget ? 1.45F : 1.05F,
                    true,
                    LegacyShipProjectileVisual.MISSILE,
                    GameplayParticleType.MISSILE_IMPACT);
            if (hit) {
                target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 40, 0, false, true, true));
                acted = true;
            }
        }
        sendLaunchSmoke(ship);
        return acted;
    }

    private static boolean performKongouCombo(LegacyShipEntity ship, LivingEntity primaryTarget) {
        List<LivingEntity> targets = collectTargets(ship, primaryTarget, 7.0D, 5);
        boolean acted = false;
        for (int index = 0; index < targets.size(); index++) {
            LivingEntity target = targets.get(index);
            acted |= ship.performCatalogSpecialAttack(target, LegacyShipAttackKind.HEAVY,
                    index == 0 ? 1.55F : 1.15F,
                    true,
                    LegacyShipProjectileVisual.MISSILE,
                    GameplayParticleType.MISSILE_IMPACT);
            acted |= ship.performCatalogSpecialAttack(target, LegacyShipAttackKind.LIGHT,
                    index == 0 ? 1.0F : 0.78F,
                    false,
                    LegacyShipProjectileVisual.NONE,
                    GameplayParticleType.HIT_EXPLOSION);
        }
        sendLaunchSmoke(ship);
        return acted;
    }

    private static List<LivingEntity> collectTargets(LegacyShipEntity ship, LivingEntity primaryTarget,
                                                     double radius, int maxTargets) {
        List<LivingEntity> targets = new ArrayList<>();
        targets.add(primaryTarget);

        AABB searchArea = primaryTarget.getBoundingBox().inflate(radius, radius * 0.5D, radius);
        for (LivingEntity candidate : ship.level().getEntitiesOfClass(LivingEntity.class, searchArea)) {
            if (candidate == primaryTarget || !ship.canEngage(candidate)) {
                continue;
            }
            targets.add(candidate);
        }

        targets.sort(Comparator.comparingDouble(ship::distanceToSqr));
        if (targets.size() > maxTargets) {
            return List.copyOf(targets.subList(0, maxTargets));
        }
        return List.copyOf(targets);
    }

    private static List<LivingEntity> collectLineTargets(LegacyShipEntity ship, LivingEntity primaryTarget,
                                                         double range, double radius, int maxTargets) {
        Vec3 origin = centerOf(ship);
        Vec3 primaryOffset = centerOf(primaryTarget).subtract(origin);
        if (primaryOffset.lengthSqr() <= 1.0E-4D) {
            return List.of(primaryTarget);
        }

        Vec3 direction = primaryOffset.normalize();
        List<ScoredTarget> scoredTargets = new ArrayList<>();
        AABB searchArea = new AABB(ship.blockPosition()).inflate(range, range * 0.5D, range);
        for (LivingEntity candidate : ship.level().getEntitiesOfClass(LivingEntity.class, searchArea)) {
            if (!ship.canEngage(candidate)) {
                continue;
            }

            Vec3 offset = centerOf(candidate).subtract(origin);
            double along = offset.dot(direction);
            if (along < 0.0D || along > range) {
                continue;
            }

            double lateralDistance = offset.subtract(direction.scale(along)).length();
            if (lateralDistance <= radius) {
                scoredTargets.add(new ScoredTarget(candidate, along));
            }
        }

        scoredTargets.sort(Comparator.comparingDouble(ScoredTarget::distanceAlong));
        List<LivingEntity> targets = new ArrayList<>();
        for (ScoredTarget scoredTarget : scoredTargets) {
            targets.add(scoredTarget.target());
            if (targets.size() >= maxTargets) {
                break;
            }
        }
        if (!targets.contains(primaryTarget)) {
            targets.add(0, primaryTarget);
        }
        return List.copyOf(targets);
    }

    private static Vec3 centerOf(LivingEntity entity) {
        return entity.position().add(0.0D, entity.getBbHeight() * 0.5D, 0.0D);
    }

    private static void sendLaunchSmoke(LegacyShipEntity ship) {
        CombatFxDispatcher.sendParticle(ship, GameplayParticleType.LAUNCH_SMOKE,
                ship.getX(), ship.getY() + ship.getBbHeight() * 0.7D, ship.getZ());
    }

    private record ScoredTarget(LivingEntity target, double distanceAlong) {
    }
}
