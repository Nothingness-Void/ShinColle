package com.lulan.shincolle.morph;

import com.lulan.shincolle.entity.mount.LegacyMountEntity;
import com.lulan.shincolle.entity.ship.LegacyShipAttackKind;
import com.lulan.shincolle.entity.ship.LegacyShipAttackProfile;
import com.lulan.shincolle.entity.ship.LegacyShipCombatHelper;
import com.lulan.shincolle.entity.ship.LegacyShipEntity;
import com.lulan.shincolle.entity.ship.LegacyShipStatTables;
import com.lulan.shincolle.entity.ship.LegacyShipStats;
import com.lulan.shincolle.entity.ship.ShipEquipmentBehaviorState;
import com.lulan.shincolle.menu.MorphInventoryMenu;
import com.lulan.shincolle.network.CombatFxDispatcher;
import com.lulan.shincolle.network.CombatReactType;
import com.lulan.shincolle.network.GameplayParticleType;
import com.lulan.shincolle.teitoku.TeitokuData;
import com.lulan.shincolle.teitoku.TeitokuHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkHooks;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public final class MorphHelper {

    private static final UUID MAX_HEALTH_MODIFIER_ID = UUID.fromString("cc3950f8-72fe-4da1-9a57-45f3f2c9a001");
    private static final UUID MOVE_SPEED_MODIFIER_ID = UUID.fromString("cc3950f8-72fe-4da1-9a57-45f3f2c9a002");
    private static final UUID ATTACK_DAMAGE_MODIFIER_ID = UUID.fromString("cc3950f8-72fe-4da1-9a57-45f3f2c9a003");
    private static final UUID FOLLOW_RANGE_MODIFIER_ID = UUID.fromString("cc3950f8-72fe-4da1-9a57-45f3f2c9a004");
    private static final UUID KNOCKBACK_RESISTANCE_MODIFIER_ID = UUID.fromString("cc3950f8-72fe-4da1-9a57-45f3f2c9a005");

    private static final MorphCompatBridge COMPAT_BRIDGE = MorphCompatBridge.NOOP;

    private MorphHelper() {
    }

    public static @Nullable MorphProfile getSelectedProfile(Player player) {
        return TeitokuHelper.get(player)
                .map(TeitokuData::getSelectedMorphProfile)
                .orElse(null);
    }

    public static @Nullable MorphProfile getActiveProfile(Player player) {
        return TeitokuHelper.get(player)
                .map(data -> data.hasActiveMorph() ? data.getSelectedMorphProfile() : null)
                .orElse(null);
    }

    public static boolean unlockMorph(ServerPlayer player, int legacyClassId) {
        boolean unlocked = TeitokuHelper.get(player)
                .map(data -> {
                    boolean changed = data.unlockMorph(legacyClassId);
                    if (changed) {
                        data.setSelectedMorphProfile(legacyClassId);
                    }
                    return changed;
                })
                .orElse(false);
        if (unlocked) {
            TeitokuHelper.syncGameplayState(player);
        }
        return unlocked;
    }

    public static void openMorphScreen(ServerPlayer player) {
        NetworkHooks.openScreen(player,
                new SimpleMenuProvider(
                        (containerId, inventory, menuPlayer) -> new MorphInventoryMenu(containerId, inventory),
                        Component.translatable("gui.shincolle.morph_inventory.title")),
                buffer -> buffer.writeVarInt(resolveSelectedClassId(player)));
    }

    public static boolean cycleProfile(ServerPlayer player, boolean forward) {
        boolean changed = TeitokuHelper.get(player)
                .map(data -> data.cycleMorphProfile(forward))
                .orElse(false);
        if (changed) {
            TeitokuHelper.syncGameplayState(player);
        }
        return changed;
    }

    public static boolean toggleActive(ServerPlayer player) {
        return TeitokuHelper.get(player)
                .map(data -> {
                    boolean next = !data.hasActiveMorph();
                    if (!data.setMorphActive(next)) {
                        return false;
                    }
                    applyOrResetPlayerMorphState(player, data.hasActiveMorph() ? data.getSelectedMorphProfile() : null);
                    COMPAT_BRIDGE.syncActiveMorph(player, data.getSelectedMorphProfile(), data.hasActiveMorph());
                    TeitokuHelper.syncGameplayState(player);
                    return true;
                })
                .orElse(false);
    }

    public static boolean canUseMountHost(ServerPlayer player) {
        TeitokuData data = TeitokuHelper.get(player).resolve().orElse(null);
        return data != null && data.hasActiveMorph();
    }

    public static boolean toggleMount(ServerPlayer player) {
        if (!canUseMountHost(player)) {
            return false;
        }

        if (player.getVehicle() instanceof LegacyMountEntity mount && mount.isOwnedBy(player)) {
            player.stopRiding();
            mount.discard();
            return true;
        }

        if (!(player.level() instanceof ServerLevel serverLevel)) {
            return false;
        }

        LegacyMountEntity mount = LegacyMountEntity.create(serverLevel, player);
        if (mount == null) {
            return false;
        }

        if (!serverLevel.noCollision(mount, mount.getBoundingBox())) {
            return false;
        }

        serverLevel.addFreshEntity(mount);
        player.startRiding(mount, true);
        return true;
    }

    public static boolean tick(ServerPlayer player) {
        boolean[] changed = new boolean[] { false };
        TeitokuHelper.get(player).ifPresent(data -> {
            MorphRuntimeState runtimeState = data.getMorphRuntimeState();
            MorphHostMode nextMode = resolveHostMode(player, data);
            if (runtimeState.getHostMode() != nextMode) {
                runtimeState.setHostMode(nextMode);
                changed[0] = true;
            }

            if (data.tickMorphRuntime()) {
                changed[0] = true;
            }

            applyOrResetPlayerMorphState(player, data.hasActiveMorph() ? data.getSelectedMorphProfile() : null);
        });
        return changed[0];
    }

    public static boolean performCompatAttack(ServerPlayer player, LegacyShipAttackKind attackKind, int targetId) {
        MorphProfile profile = getActiveProfile(player);
        if (profile == null || !(player.level().getEntity(targetId) instanceof LivingEntity target)) {
            return false;
        }

        LegacyShipAttackProfile attackProfile = profile.buildAttackProfile();
        boolean allowed = switch (attackKind) {
            case MELEE -> attackProfile.melee();
            case LIGHT -> attackProfile.light();
            case HEAVY -> attackProfile.heavy();
            case AIR_LIGHT -> attackProfile.airLight();
            case AIR_HEAVY -> attackProfile.airHeavy();
        };
        if (!allowed || !canEngage(player, profile, target)) {
            return false;
        }

        TeitokuData data = TeitokuHelper.get(player).resolve().orElse(null);
        if (data == null) {
            return false;
        }

        MorphRuntimeState runtimeState = data.getMorphRuntimeState();
        if (runtimeState.getAttackCooldown(attackKind) > 0) {
            return false;
        }
        if (!consumeAttackCost(profile, attackKind)) {
            return false;
        }

        LegacyShipStats stats = profile.buildStats(player.getActiveEffects());
        if (attackKind != LegacyShipAttackKind.MELEE && profile.buildBehaviorState().flareLevel() > 0) {
            target.setGlowingTag(true);
        }

        LegacyShipCombatHelper.AttackRoll roll = rollMorphAttack(player, profile, stats, target, attackKind);
        int delay = LegacyShipCombatHelper.attackDelay(stats, profile.buildBehaviorState(), attackKind);
        runtimeState.setAttackCooldown(attackKind, delay);

        if (roll.miss()) {
            emitAttackFx(player, target, attackKind, roll, !attackKind.justLaunch());
            return true;
        }

        boolean attacked = target.hurt(player.damageSources().playerAttack(player), roll.damage());
        if (attacked) {
            player.setLastHurtMob(target);
        }
        emitAttackFx(player, target, attackKind, roll, attacked);
        TeitokuHelper.syncGameplayState(player);
        return attacked;
    }

    public static boolean performCompatSpecial(ServerPlayer player, int targetId) {
        MorphProfile profile = getActiveProfile(player);
        if (profile == null || !hasSpecialSkill(profile)) {
            return false;
        }

        TeitokuData data = TeitokuHelper.get(player).resolve().orElse(null);
        if (data == null) {
            return false;
        }

        MorphRuntimeState runtimeState = data.getMorphRuntimeState();
        if (runtimeState.getSpecialCooldown() > 0) {
            return false;
        }
        if (profile.getGrudge() < 4) {
            return false;
        }

        LivingEntity target = player.level().getEntity(targetId) instanceof LivingEntity living ? living : null;
        if (target == null || !canEngage(player, profile, target)) {
            target = findNearestMorphTarget(player, profile, 9.0D);
        }
        if (target == null) {
            return false;
        }

        int specialCooldown = 120;
        boolean triggered = switch (profile.getLegacyClassId()) {
            case 58, 2058 -> performTenryuuSpecial(player, profile, target);
            case 59, 2059 -> performTatsutaSpecial(player, profile, target);
            default -> false;
        };

        if (!triggered) {
            return false;
        }

        profile.addGrudge(-4);
        runtimeState.setSpecialCooldown(specialCooldown);
        TeitokuHelper.syncGameplayState(player);
        return true;
    }

    public static Component getHotbarLabel(Player player) {
        MorphProfile profile = getActiveProfile(player);
        if (profile == null) {
            return Component.empty();
        }

        return profile.getSpec().displayName().copy().append(Component.literal(" Lv" + profile.getLevel()));
    }

    public static Component getSkillBarLabel(Player player) {
        MorphProfile profile = getActiveProfile(player);
        if (profile == null) {
            return Component.empty();
        }

        LegacyShipAttackProfile attackProfile = profile.buildAttackProfile();
        String summary = (attackProfile.melee() ? "M" : "-")
                + (attackProfile.light() ? " L" : "")
                + (attackProfile.heavy() ? " H" : "")
                + (attackProfile.airLight() ? " AL" : "")
                + (attackProfile.airHeavy() ? " AH" : "");
        if (hasSpecialSkill(profile)) {
            summary += " SP";
        }
        return Component.literal(summary.trim());
    }

    public static boolean hasSpecialSkill(Player player) {
        return hasSpecialSkill(getActiveProfile(player));
    }

    public static boolean hasSpecialSkill(@Nullable MorphProfile profile) {
        if (profile == null) {
            return false;
        }

        return switch (profile.getLegacyClassId()) {
            case 58, 2058, 59, 2059 -> true;
            default -> false;
        };
    }

    private static boolean canEngage(Player player, MorphProfile profile, LivingEntity target) {
        if (target == null || !target.isAlive() || target == player || player.isAlliedTo(target)) {
            return false;
        }

        if (target instanceof Player targetPlayer) {
            return profile.isAiAllowPvp() && TeitokuHelper.getPlayerUid(player) > 0
                    && player.level() instanceof ServerLevel serverLevel
                    && TeitokuHelper.isBanned(serverLevel, TeitokuHelper.getPlayerUid(player), TeitokuHelper.getPlayerUid(targetPlayer));
        }

        if (target instanceof LegacyShipEntity ship) {
            if (profile.getSpec().hostile() != ship.isHostileVariant()) {
                return true;
            }
            if (profile.getSpec().hostile()) {
                return false;
            }
            return TeitokuHelper.getPlayerUid(player) > 0
                    && ship.getOwnerUid() > 0
                    && player.level() instanceof ServerLevel serverLevel
                    && TeitokuHelper.isBanned(serverLevel, TeitokuHelper.getPlayerUid(player), ship.getOwnerUid());
        }

        return target instanceof net.minecraft.world.entity.monster.Enemy;
    }

    private static LegacyShipCombatHelper.AttackRoll rollMorphAttack(ServerPlayer player, MorphProfile profile,
                                                                     LegacyShipStats stats, LivingEntity target,
                                                                     LegacyShipAttackKind attackKind) {
        float distance = player.distanceTo(target);
        ShipEquipmentBehaviorState behaviorState = profile.buildBehaviorState();
        boolean flyingTarget = LegacyShipCombatHelper.isFlyingTarget(target);
        boolean underseaTarget = LegacyShipCombatHelper.isUnderseaTarget(target);
        boolean darkCombat = player.level().getMaxLocalRawBrightness(player.blockPosition()) <= 7
                || player.level().getMaxLocalRawBrightness(target.blockPosition()) <= 7;
        boolean illuminated = target.isCurrentlyGlowing();

        float damage = switch (attackKind) {
            case MELEE -> stats.meleeDamage();
            case LIGHT -> adjustTargetDamage(stats, target, stats.attackLight());
            case HEAVY -> stats.attackHeavy();
            case AIR_LIGHT -> stats.attackAirLight();
            case AIR_HEAVY -> stats.attackAirHeavy();
        };

        float miss = attackKind.canMiss()
                ? calcMissRate(player, profile, stats, behaviorState, target, distance, flyingTarget, underseaTarget, darkCombat, illuminated)
                : 0.0F;
        float crit = attackKind.canCrit() ? stats.critical() + behaviorState.critBonus(illuminated) + miss : miss;
        float doubleHit = attackKind.canDoubleHit() ? stats.doubleHit() + crit : crit;
        float tripleHit = attackKind.canTripleHit() ? stats.tripleHit() + doubleHit : doubleHit;
        float roll = player.getRandom().nextFloat();

        boolean isMiss = attackKind.canMiss() && roll <= miss;
        boolean isCrit = attackKind.canCrit() && !isMiss && roll <= crit;
        boolean isDoubleHit = attackKind.canDoubleHit() && !isMiss && !isCrit && roll <= doubleHit;
        boolean isTripleHit = attackKind.canTripleHit() && !isMiss && !isCrit && !isDoubleHit && roll <= tripleHit;

        if (isMiss && !attackKind.justLaunch()) {
            damage = 0.0F;
        } else if (isCrit) {
            damage *= 1.5F;
        } else if (isDoubleHit) {
            damage *= 2.0F;
        } else if (isTripleHit) {
            damage *= 3.0F;
        }

        if (target instanceof Player) {
            damage = Math.min(59.0F, damage * 0.25F);
        } else if (target instanceof LegacyShipEntity) {
            damage *= 1.0F;
        }

        return new LegacyShipCombatHelper.AttackRoll(damage, isMiss, isCrit, isDoubleHit, isTripleHit);
    }

    private static float adjustTargetDamage(LegacyShipStats stats, LivingEntity target, float damage) {
        if (LegacyShipCombatHelper.isFlyingTarget(target)) {
            damage += stats.antiAir();
        }
        if (LegacyShipCombatHelper.isUnderseaTarget(target)) {
            damage += stats.antiSub();
        }
        return damage;
    }

    private static float calcMissRate(ServerPlayer player, MorphProfile profile, LegacyShipStats stats,
                                      ShipEquipmentBehaviorState behaviorState, LivingEntity target, float distance,
                                      boolean flyingTarget, boolean underseaTarget,
                                      boolean darkCombat, boolean illuminated) {
        float range = Math.max(1.0F, stats.attackRange());
        float miss;

        if (range <= 3.0F) {
            miss = 0.25F - 0.001F * profile.getLevel();
        } else if (range <= 6.0F) {
            miss = 0.25F + 0.15F * (distance / range) - 0.001F * profile.getLevel();
        } else {
            miss = 0.25F + 0.25F * (distance / range) - 0.001F * profile.getLevel();
        }

        miss -= stats.get(LegacyShipStatTables.Attr.MISS);
        miss -= behaviorState.rangedAccuracyBonus(flyingTarget, underseaTarget, darkCombat, illuminated);
        miss = Mth.clamp(miss, 0.0F, 0.5F);

        if (targetHasEffect(target, net.minecraft.world.effect.MobEffects.INVISIBILITY)) {
            miss += 0.08F;
        }

        return miss;
    }

    private static boolean targetHasEffect(LivingEntity target, net.minecraft.world.effect.MobEffect effect) {
        return target.hasEffect(effect);
    }

    private static MorphHostMode resolveHostMode(ServerPlayer player, TeitokuData data) {
        if (!data.hasActiveMorph()) {
            return MorphHostMode.NONE;
        }
        if (player.getVehicle() instanceof LegacyMountEntity) {
            return MorphHostMode.MOUNT;
        }
        if (player.getVehicle() instanceof LegacyShipEntity) {
            return MorphHostMode.RIDER;
        }
        return MorphHostMode.MORPH;
    }

    private static void applyOrResetPlayerMorphState(ServerPlayer player, @Nullable MorphProfile profile) {
        if (profile == null) {
            clearModifier(player, Attributes.MAX_HEALTH, MAX_HEALTH_MODIFIER_ID);
            clearModifier(player, Attributes.MOVEMENT_SPEED, MOVE_SPEED_MODIFIER_ID);
            clearModifier(player, Attributes.ATTACK_DAMAGE, ATTACK_DAMAGE_MODIFIER_ID);
            clearModifier(player, Attributes.FOLLOW_RANGE, FOLLOW_RANGE_MODIFIER_ID);
            clearModifier(player, Attributes.KNOCKBACK_RESISTANCE, KNOCKBACK_RESISTANCE_MODIFIER_ID);
            if (player.getHealth() > player.getMaxHealth()) {
                player.setHealth(player.getMaxHealth());
            }
            return;
        }

        LegacyShipStats stats = profile.buildStats(player.getActiveEffects());
        applyModifier(player, Attributes.MAX_HEALTH, MAX_HEALTH_MODIFIER_ID, "shincolle_morph_max_health",
                stats.get(LegacyShipStatTables.Attr.HP) - 20.0D);
        applyModifier(player, Attributes.MOVEMENT_SPEED, MOVE_SPEED_MODIFIER_ID, "shincolle_morph_move_speed",
                stats.moveSpeed() - 0.10000000149011612D);
        applyModifier(player, Attributes.ATTACK_DAMAGE, ATTACK_DAMAGE_MODIFIER_ID, "shincolle_morph_attack_damage",
                stats.meleeDamage() - 1.0D);
        applyModifier(player, Attributes.FOLLOW_RANGE, FOLLOW_RANGE_MODIFIER_ID, "shincolle_morph_follow_range",
                Math.max(0.0D, profile.getFollowRange() - 32.0D));
        applyModifier(player, Attributes.KNOCKBACK_RESISTANCE, KNOCKBACK_RESISTANCE_MODIFIER_ID, "shincolle_morph_knockback",
                stats.knockbackResistance());

        if (player.getHealth() > player.getMaxHealth()) {
            player.setHealth(player.getMaxHealth());
        }
    }

    private static void applyModifier(ServerPlayer player, Attribute attribute, UUID id, String name, double amount) {
        AttributeInstance instance = player.getAttribute(attribute);
        if (instance == null) {
            return;
        }

        AttributeModifier previous = instance.getModifier(id);
        if (previous != null) {
            instance.removeModifier(previous);
        }

        if (Math.abs(amount) < 1.0E-4D) {
            return;
        }

        instance.addTransientModifier(new AttributeModifier(id, name, amount, AttributeModifier.Operation.ADDITION));
    }

    private static void clearModifier(ServerPlayer player, Attribute attribute, UUID id) {
        AttributeInstance instance = player.getAttribute(attribute);
        if (instance == null) {
            return;
        }

        AttributeModifier previous = instance.getModifier(id);
        if (previous != null) {
            instance.removeModifier(previous);
        }
    }

    private static void emitAttackFx(ServerPlayer player, LivingEntity target, LegacyShipAttackKind attackKind,
                                     LegacyShipCombatHelper.AttackRoll roll, boolean attacked) {
        if (roll.miss()) {
            CombatFxDispatcher.sendCombatReact(player, target, CombatReactType.MISS, attackKind);
            CombatFxDispatcher.sendParticle(target, GameplayParticleType.TEXT_MISS,
                    target.getX(), target.getY() + target.getBbHeight() * 0.8D, target.getZ());
            return;
        }

        if (attackKind.justLaunch()) {
            CombatFxDispatcher.sendCombatReact(player, target, CombatReactType.LAUNCH, attackKind);
            CombatFxDispatcher.sendParticle(player, GameplayParticleType.LAUNCH_SMOKE,
                    player.getX(), player.getY() + player.getBbHeight() * 0.7D, player.getZ());
        }

        if (roll.crit()) {
            CombatFxDispatcher.sendCombatReact(player, target, CombatReactType.CRIT, attackKind);
            CombatFxDispatcher.sendParticle(target, GameplayParticleType.TEXT_CRIT,
                    target.getX(), target.getY() + target.getBbHeight() * 0.8D, target.getZ());
        } else if (roll.tripleHit()) {
            CombatFxDispatcher.sendCombatReact(player, target, CombatReactType.TRIPLE_HIT, attackKind);
            CombatFxDispatcher.sendParticle(target, GameplayParticleType.TEXT_TRIPLE,
                    target.getX(), target.getY() + target.getBbHeight() * 0.8D, target.getZ());
        } else if (roll.doubleHit()) {
            CombatFxDispatcher.sendCombatReact(player, target, CombatReactType.DOUBLE_HIT, attackKind);
            CombatFxDispatcher.sendParticle(target, GameplayParticleType.TEXT_DOUBLE,
                    target.getX(), target.getY() + target.getBbHeight() * 0.8D, target.getZ());
        }

        if (attacked) {
            CombatFxDispatcher.sendCombatReact(player, target, CombatReactType.HIT, attackKind);
            CombatFxDispatcher.sendParticle(target, GameplayParticleType.HIT_EXPLOSION,
                    target.getX(), target.getY() + target.getBbHeight() * 0.5D, target.getZ());
        }
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
            hit |= applySpecialAttack(player, profile, target, LegacyShipAttackKind.MELEE, 1.55F, false);
        }

        CombatFxDispatcher.sendParticle(player, GameplayParticleType.LAUNCH_SMOKE,
                player.getX(), player.getY() + player.getBbHeight() * 0.7D, player.getZ());
        return hit;
    }

    private static boolean performTatsutaSpecial(ServerPlayer player, MorphProfile profile, LivingEntity primaryTarget) {
        List<LivingEntity> targets = collectSpecialTargets(player, profile, primaryTarget, 4.5D, 8);
        boolean hit = false;
        for (LivingEntity target : targets) {
            boolean attacked = applySpecialAttack(player, profile, target, LegacyShipAttackKind.LIGHT, 1.35F, true);
            if (attacked) {
                target.addEffect(new MobEffectInstance(MobEffects.LEVITATION, 30, 0, false, true, true));
                hit = true;
            }
        }

        CombatFxDispatcher.sendParticle(primaryTarget, GameplayParticleType.HIT_EXPLOSION,
                primaryTarget.getX(), primaryTarget.getY() + primaryTarget.getBbHeight() * 0.5D, primaryTarget.getZ());
        return hit;
    }

    private static @Nullable LivingEntity findNearestMorphTarget(ServerPlayer player, MorphProfile profile, double range) {
        LivingEntity bestTarget = null;
        double bestDistance = range * range;

        for (LivingEntity candidate : player.level().getEntitiesOfClass(LivingEntity.class,
                new AABB(player.blockPosition()).inflate(range, 3.0D, range))) {
            if (!canEngage(player, profile, candidate)) {
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

    private static List<LivingEntity> collectSpecialTargets(ServerPlayer player, MorphProfile profile,
                                                            LivingEntity primaryTarget, double radius, int maxTargets) {
        List<LivingEntity> targets = new ArrayList<>();
        targets.add(primaryTarget);

        for (LivingEntity candidate : player.level().getEntitiesOfClass(LivingEntity.class,
                primaryTarget.getBoundingBox().inflate(radius, radius * 0.5D, radius))) {
            if (candidate == primaryTarget || !canEngage(player, profile, candidate)) {
                continue;
            }
            targets.add(candidate);
            if (targets.size() >= maxTargets) {
                break;
            }
        }

        return targets;
    }

    private static boolean applySpecialAttack(ServerPlayer player, MorphProfile profile, LivingEntity target,
                                              LegacyShipAttackKind attackKind, float damageMultiplier,
                                              boolean illuminate) {
        LegacyShipStats stats = profile.buildStats(player.getActiveEffects());
        LegacyShipCombatHelper.AttackRoll roll = rollMorphAttack(player, profile, stats, target, attackKind);
        float damage = roll.damage() * damageMultiplier;
        LegacyShipCombatHelper.AttackRoll adjustedRoll = new LegacyShipCombatHelper.AttackRoll(
                damage, roll.miss(), roll.crit(), roll.doubleHit(), roll.tripleHit());

        if (illuminate) {
            target.addEffect(new MobEffectInstance(MobEffects.GLOWING, 60, 0, false, true, true));
        }

        if (adjustedRoll.miss()) {
            emitAttackFx(player, target, attackKind, adjustedRoll, false);
            return false;
        }

        boolean attacked = target.hurt(player.damageSources().playerAttack(player), adjustedRoll.damage());
        if (attacked) {
            player.setLastHurtMob(target);
        }
        emitAttackFx(player, target, attackKind, adjustedRoll, attacked);
        return attacked;
    }

    private static boolean consumeAttackCost(MorphProfile profile, LegacyShipAttackKind attackKind) {
        return switch (attackKind) {
            case MELEE -> true;
            case LIGHT -> consumeAmmo(profile, 1, 0);
            case HEAVY -> consumeAmmo(profile, 0, 1);
            case AIR_LIGHT -> consumeAmmo(profile, 2, 0);
            case AIR_HEAVY -> consumeAmmo(profile, 0, 2);
        };
    }

    private static boolean consumeAmmo(MorphProfile profile, int lightAmmo, int heavyAmmo) {
        if (profile.getAmmoLight() < lightAmmo || profile.getAmmoHeavy() < heavyAmmo) {
            return false;
        }

        if (lightAmmo > 0) {
            profile.addAmmoLight(-lightAmmo);
        }
        if (heavyAmmo > 0) {
            profile.addAmmoHeavy(-heavyAmmo);
        }
        return true;
    }

    private static int resolveSelectedClassId(Player player) {
        return TeitokuHelper.get(player)
                .map(data -> data.getMorphRuntimeState().getSelectedClassId())
                .orElse(0);
    }
}
