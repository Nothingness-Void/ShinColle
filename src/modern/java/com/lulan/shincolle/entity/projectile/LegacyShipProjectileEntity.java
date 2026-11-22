package com.lulan.shincolle.entity.projectile;

import java.util.List;

import com.lulan.shincolle.entity.ship.LegacyShipAttackKind;
import com.lulan.shincolle.entity.ship.LegacyShipAttackProfile;
import com.lulan.shincolle.entity.ship.LegacyShipBehaviorCatalog;
import com.lulan.shincolle.entity.ship.LegacyShipCombatHelper;
import com.lulan.shincolle.entity.ship.LegacyShipEntity;
import com.lulan.shincolle.entity.ship.ShipEquipmentBehaviorState;
import com.lulan.shincolle.network.CombatFxDispatcher;
import com.lulan.shincolle.network.CombatReactType;
import com.lulan.shincolle.network.GameplayParticleType;
import com.lulan.shincolle.registry.ModEntityTypes;
import com.lulan.shincolle.registry.ModItems;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ItemSupplier;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;

public class LegacyShipProjectileEntity extends ThrowableItemProjectile implements ItemSupplier {

    private static final EntityDataAccessor<Integer> DATA_ATTACK_KIND =
            SynchedEntityData.defineId(LegacyShipProjectileEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_PROJECTILE_VISUAL =
            SynchedEntityData.defineId(LegacyShipProjectileEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_MOVE_TYPE =
            SynchedEntityData.defineId(LegacyShipProjectileEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_LAUNCH_PARTICLE =
            SynchedEntityData.defineId(LegacyShipProjectileEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_TRAIL_PARTICLE =
            SynchedEntityData.defineId(LegacyShipProjectileEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_HIT_PARTICLE =
            SynchedEntityData.defineId(LegacyShipProjectileEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_EFFECT_FLAGS =
            SynchedEntityData.defineId(LegacyShipProjectileEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> DATA_LEGACY_MOTION_OVERRIDE =
            SynchedEntityData.defineId(LegacyShipProjectileEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Float> DATA_LEGACY_ARC_ACCELERATION_1 =
            SynchedEntityData.defineId(LegacyShipProjectileEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> DATA_LEGACY_ARC_ACCELERATION_2 =
            SynchedEntityData.defineId(LegacyShipProjectileEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Integer> DATA_LEGACY_ARC_RISE_TICKS =
            SynchedEntityData.defineId(LegacyShipProjectileEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> DATA_LEGACY_TORPEDO_STARTED =
            SynchedEntityData.defineId(LegacyShipProjectileEntity.class, EntityDataSerializers.BOOLEAN);

    private static final double LEGACY_ARC_ACCELERATION_LIMIT = 0.15D;

    private static final int EFFECT_FLAG_FLARE = 1;
    private static final int EFFECT_FLAG_SEARCHLIGHT = 1 << 1;

    private float damage;
    private int targetId = -1;
    private Vec3 legacyTargetPosition = Vec3.ZERO;
    private int lifeTicks = 80;
    private boolean missedShot;
    private boolean resolvedImpact;
    private boolean blackHoleOnImpact;
    private boolean clusterMissile;
    private boolean clusterSubMissile;
    private boolean impactEffectSpawned;
    private boolean reportMissFeedback = true;
    private double knockbackStrength = 0.15D;
    private float speedScale = 1.0F;
    private float guidanceBlend = 0.18F;
    private float travelSpeed = 0.52F;
    private float targetHeightFactor = 0.35F;
    private double impactRadius = 3.5D;
    private int torpedoStartMoveDelay;

    public LegacyShipProjectileEntity(EntityType<? extends LegacyShipProjectileEntity> entityType, Level level) {
        super(entityType, level);
    }

    public LegacyShipProjectileEntity(Level level, LegacyShipEntity owner) {
        super(ModEntityTypes.LEGACY_SHIP_PROJECTILE.get(), owner, level);
    }

    public static LegacyShipProjectileEntity create(Level level, LegacyShipEntity owner, LivingEntity target,
                                                    LegacyShipAttackKind attackKind, float damage, boolean missedShot) {
        return create(level, owner, target, attackKind, damage, missedShot,
                owner.getAttackProfile().projectileProfile(attackKind), null, 0.15D, true, 3.5D);
    }

    public static LegacyShipProjectileEntity create(Level level, LegacyShipEntity owner, LivingEntity target,
                                                    LegacyShipAttackKind attackKind, float damage, boolean missedShot,
                                                    LegacyShipProjectileProfile profile, @Nullable Vec3 targetPosition,
                                                    double knockbackStrength, boolean reportMissFeedback) {
        return create(level, owner, target, attackKind, damage, missedShot, profile, targetPosition,
                knockbackStrength, reportMissFeedback, 0.0D, Vec3.ZERO);
    }

    public static LegacyShipProjectileEntity create(Level level, LegacyShipEntity owner,
                                                    @Nullable LivingEntity target,
                                                     LegacyShipAttackKind attackKind, float damage, boolean missedShot,
                                                     LegacyShipProjectileProfile profile, @Nullable Vec3 targetPosition,
                                                     double knockbackStrength, boolean reportMissFeedback,
                                                     double impactRadius) {
        return create(level, owner, target, attackKind, damage, missedShot, profile, targetPosition,
                knockbackStrength, reportMissFeedback, impactRadius, Vec3.ZERO);
    }

    public static LegacyShipProjectileEntity create(Level level, LegacyShipEntity owner,
                                                     @Nullable LivingEntity target,
                                                     LegacyShipAttackKind attackKind, float damage, boolean missedShot,
                                                     LegacyShipProjectileProfile profile, @Nullable Vec3 targetPosition,
                                                     double knockbackStrength, boolean reportMissFeedback,
                                                     double impactRadius, @Nullable Vec3 launchOffset) {
        LegacyShipProjectileEntity projectile = new LegacyShipProjectileEntity(level, owner);
        if (!profile.isPresent()) {
            throw new IllegalStateException("Missing explicit legacy projectile profile for "
                    + owner.getSpec().textureStem() + " attack " + attackKind);
        }
        if (target == null && targetPosition == null) {
            throw new IllegalArgumentException("A legacy projectile needs a target or fixed target position");
        }

        projectile.setAttackKind(attackKind);
        projectile.setProjectileVisual(profile.visual());
        projectile.setLaunchParticleType(profile.launchParticleType());
        projectile.setTrailParticleType(profile.trailParticleType());
        projectile.setHitParticleType(profile.hitParticleType());
        projectile.damage = damage;
        projectile.missedShot = missedShot;
        projectile.blackHoleOnImpact = LegacyShipBehaviorCatalog.usesBlackHoleHeavyMissile(owner, attackKind);
        projectile.clusterMissile = LegacyShipBehaviorCatalog.usesClusterHeavyMissile(owner, attackKind);
        projectile.reportMissFeedback = reportMissFeedback;
        projectile.knockbackStrength = Math.max(0.0D, knockbackStrength);
        projectile.targetId = target == null ? -1 : target.getId();
        projectile.targetHeightFactor = profile.targetHeightFactor();
        projectile.lifeTicks = profile.lifeTicks();
        projectile.travelSpeed = (float) profile.baseSpeed();
        projectile.impactRadius = Math.max(0.0D, impactRadius);
        if (projectile.clusterMissile) {
            projectile.impactRadius = Math.max(projectile.impactRadius, 3.5D);
        }

        ShipEquipmentBehaviorState behaviorState = owner.getEquipmentBehaviorState();
        projectile.speedScale = 1.0F;
        projectile.travelSpeed = (float) behaviorState.legacyProjectileSpeed(profile.baseSpeed());
        projectile.guidanceBlend = profile.guidanceBlend();
        int effectFlags = 0;
        if (behaviorState.flareLevel() > 0) {
            effectFlags |= EFFECT_FLAG_FLARE;
        }
        if (behaviorState.searchlightLevel() > 0) {
            effectFlags |= EFFECT_FLAG_SEARCHLIGHT;
        }
        projectile.setEffectFlags(effectFlags);

        Vec3 resolvedLaunchOffset = launchOffset == null ? Vec3.ZERO : launchOffset;
        double launchY = owner.getY() + owner.getBbHeight() * profile.launchHeightFactor() + resolvedLaunchOffset.y;
        projectile.moveTo(owner.getX() + resolvedLaunchOffset.x, launchY,
                owner.getZ() + resolvedLaunchOffset.z, owner.getYRot(), owner.getXRot());

        Vec3 targetPos = targetPosition != null
                ? targetPosition
                : projectile.resolveTargetPos(target, profile, missedShot);
        projectile.legacyTargetPosition = targetPos;
        double targetYForMoveType = target == null ? targetPos.y : target.getY();
        LegacyShipProjectileMoveType moveType = LegacyShipBehaviorCatalog.legacySubmarineProjectileMoveTypeFor(
                owner.getSpec(), owner.getY(), owner.getLegacyLiquidDepth(), targetYForMoveType,
                profile.moveType());
        if (moveType == LegacyShipProjectileMoveType.TORPEDO
                && targetPos.distanceTo(projectile.position()) < 4.0D) {
            moveType = LegacyShipProjectileMoveType.DIRECT;
        }
        projectile.setMoveType(moveType);
        Vec3 launchVector = targetPos.subtract(projectile.position());
        if (moveType == LegacyShipProjectileMoveType.ARC || moveType == LegacyShipProjectileMoveType.DROP) {
            launchVector = launchVector.add(0.0D, profile.launchArcLift(), 0.0D);
        }

        if (moveType == LegacyShipProjectileMoveType.TORPEDO) {
            projectile.configureLegacyTorpedoMotion(targetPos);
        } else {
            projectile.shoot(launchVector.x, launchVector.y, launchVector.z, projectile.baseSpeed(), 0.0F);
        }
        if (moveType == LegacyShipProjectileMoveType.ARC) {
            projectile.configureLegacyArcMotion(targetPos, profile.launchArcLift());
        }
        return projectile;
    }

    @Override
    protected void defineSynchedData(net.minecraft.network.syncher.SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_ATTACK_KIND, LegacyShipAttackKind.HEAVY.ordinal());
        builder.define(DATA_PROJECTILE_VISUAL, LegacyShipProjectileVisual.NONE.ordinal());
        builder.define(DATA_MOVE_TYPE, LegacyShipProjectileMoveType.NONE.ordinal());
        builder.define(DATA_LAUNCH_PARTICLE, GameplayParticleType.LAUNCH_SMOKE.ordinal());
        builder.define(DATA_TRAIL_PARTICLE, GameplayParticleType.NONE.ordinal());
        builder.define(DATA_HIT_PARTICLE, GameplayParticleType.HIT_EXPLOSION.ordinal());
        builder.define(DATA_EFFECT_FLAGS, 0);
        builder.define(DATA_LEGACY_MOTION_OVERRIDE, false);
        builder.define(DATA_LEGACY_ARC_ACCELERATION_1, 0.0F);
        builder.define(DATA_LEGACY_ARC_ACCELERATION_2, 0.0F);
        builder.define(DATA_LEGACY_ARC_RISE_TICKS, 0);
        builder.define(DATA_LEGACY_TORPEDO_STARTED, false);
    }

    @Override
    public void tick() {
        Vec3 preTickMotion = this.getDeltaMovement();
        super.tick();

        if (!this.isRemoved()) {
            if (this.hasLegacyMotionOverride()) {
                this.advanceLegacyMotion(preTickMotion);
            } else {
                // Preserve exact pre-super.tick() velocity so ThrowableProjectile air/water drag does not alter legacy trajectories.
                this.setDeltaMovement(preTickMotion);
            }
        }

        if (this.tickCount > this.lifeTicks) {
            if (!this.level().isClientSide()) {
                if (this.impactRadius > 0.0D) {
                    this.resolveAreaImpact(this.position());
                } else {
                    if (this.getOwner() instanceof LegacyShipEntity ship) {
                        this.spawnImpactEffect(ship, this.position());
                    }
                    this.emitMissFx(this.position());
                }
            }
            this.discard();
            return;
        }

        if (!this.level().isClientSide() && this.clusterMissile) {
            // EntityAbyssMissile releases type-3 sub-missiles before its expanded collision check.
            this.spawnClusterSubMissile();
        }

        // EntityAbyssMissile checks its expanded entity box after movement and after five ticks.
        if (!this.level().isClientSide() && this.tickCount > 5 && !this.isRemoved()) {
            this.resolveLegacyContactAtCurrentPosition();
            if (this.isRemoved()) {
                return;
            }
        }

        if (this.level().isClientSide()) {
            this.spawnTrailParticles();
            return;
        }

        this.updateFlightPath();
    }

    private void resolveLegacyContactAtCurrentPosition() {
        if (this.resolvedImpact) {
            return;
        }

        Entity owner = this.getOwner();
        if (!(owner instanceof LegacyShipEntity ship)) {
            return;
        }

        AABB collisionBox = this.getBoundingBox().inflate(1.0D, 1.5D, 1.0D);
        for (LivingEntity candidate : this.level().getEntitiesOfClass(
                LivingEntity.class,
                collisionBox,
                entity -> this.isLegacyContactTarget(ship, entity))) {
            this.onHitEntity(new EntityHitResult(candidate, this.position()));
            return;
        }
    }

    private boolean isLegacyContactTarget(LegacyShipEntity ship, LivingEntity candidate) {
        if (candidate == ship || !candidate.isAlive() || !candidate.isPickable()) {
            return false;
        }
        if (candidate == ship.getVehicle() || ship.getPassengers().contains(candidate)) {
            return false;
        }
        return !LegacyShipBehaviorCatalog.hasLegacySameOwner(ship, candidate) && !ship.isAlliedTo(candidate);
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        if (this.clusterSubMissile && this.tickCount <= 5) {
            return;
        }
        Entity hitEntity = result.getEntity();
        Entity owner = this.getOwner();

        if (this.level().isClientSide() || !(owner instanceof LegacyShipEntity ship) || !(hitEntity instanceof LivingEntity livingTarget)) {
            this.discard();
            return;
        }

        if (this.impactRadius > 0.0D) {
            if (!this.isLegacyContactTarget(ship, livingTarget)) {
                this.discard();
                return;
            }
            this.resolveAreaImpact(result.getLocation());
            this.discard();
            return;
        }

        if (!ship.canEngage(livingTarget)) {
            this.discard();
            return;
        }

        this.resolvedImpact = true;
        this.spawnImpactEffect(ship, result.getLocation());
        boolean attacked = livingTarget.hurt(this.damageSources().thrown(this, ship), this.damage);
        Vec3 motion = this.getDeltaMovement();
        CombatFxDispatcher.sendParticle(livingTarget, this.getHitParticleType(),
                result.getLocation().x, result.getLocation().y, result.getLocation().z,
                motion.x, motion.y, motion.z);

        if (attacked) {
            ship.setLastHurtMob(livingTarget);
            LegacyShipBehaviorCatalog.applyAttackEffect(ship, livingTarget);
            CombatFxDispatcher.sendCombatReact(ship, livingTarget, CombatReactType.HIT,
                    this.getAttackKind(), this.getProjectileVisual());
            if (this.getAttackKind() == LegacyShipAttackKind.HEAVY || this.getAttackKind() == LegacyShipAttackKind.AIR_HEAVY) {
                livingTarget.knockback(this.knockbackStrength,
                        ship.getX() - livingTarget.getX(), ship.getZ() - livingTarget.getZ());
            }
            if (this.hasFlarePayload()) {
                CombatFxDispatcher.sendParticle(livingTarget, GameplayParticleType.FLARE_BURST,
                        livingTarget.getX(), livingTarget.getY() + livingTarget.getBbHeight() * 0.75D, livingTarget.getZ());
            }
            if (this.hasSearchlightPayload()) {
                CombatFxDispatcher.sendParticle(livingTarget, GameplayParticleType.SEARCHLIGHT_MARK,
                        livingTarget.getX(), livingTarget.getY() + livingTarget.getBbHeight() * 0.85D, livingTarget.getZ());
            }
        }

        this.discard();
    }

    @Override
    protected void onHit(HitResult result) {
        if (this.clusterSubMissile && this.tickCount <= 5) {
            return;
        }
        super.onHit(result);

        if (result.getType() != HitResult.Type.ENTITY) {
            if (!this.level().isClientSide()) {
                Entity owner = this.getOwner();
                if (owner instanceof LegacyShipEntity ship) {
                    if (this.impactRadius > 0.0D) {
                        this.resolveAreaImpact(result.getLocation());
                    } else {
                        this.spawnImpactEffect(ship, result.getLocation());
                        this.emitMissFx(result.getLocation());
                    }
                }
            }
            this.discard();
        }
    }

    @Override
    protected boolean canHitEntity(Entity entity) {
        if (!super.canHitEntity(entity) || entity == this.getOwner() || !(entity instanceof LivingEntity)) {
            return false;
        }

        if (this.clusterMissile) {
            return false;
        }

        Entity owner = this.getOwner();
        if (owner instanceof LegacyShipEntity ship) {
            return ship.canEngage((LivingEntity) entity);
        }

        return true;
    }

    @Override
    protected Item getDefaultItem() {
        if (this.entityData == null) {
            return ModItems.AMMO1.get();
        }
        return switch (this.getProjectileVisual()) {
            case AIRPLANE -> ModItems.TOYAIRPLANE.get();
            case BOMB -> ModItems.AMMO2.get();
            case TORPEDO -> ModItems.AMMO.get();
            case MISSILE -> ModItems.AMMO1.get();
            case NONE -> switch (this.getAttackKind()) {
                case AIR_LIGHT, AIR_HEAVY -> ModItems.TOYAIRPLANE.get();
                case LIGHT -> ModItems.AMMO.get();
                case HEAVY, MELEE -> ModItems.AMMO1.get();
            };
        };
    }

    @Override
    public ItemStack getItem() {
        return new ItemStack(this.getDefaultItem());
    }

    @Override
    protected double getDefaultGravity() {
        if (this.hasLegacyMotionOverride()) {
            return 0.0F;
        }

        return switch (this.getMoveType()) {
            case ARC -> 0.015F;
            case DROP -> 0.05F;
            default -> 0.0F;
        };
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("AttackKind", this.getAttackKind().ordinal());
        tag.putInt("ProjectileVisual", this.getProjectileVisual().ordinal());
        tag.putInt("MoveType", this.getMoveType().ordinal());
        tag.putInt("LaunchParticleType", this.getLaunchParticleType().ordinal());
        tag.putInt("TrailParticleType", this.getTrailParticleType().ordinal());
        tag.putInt("HitParticleType", this.getHitParticleType().ordinal());
        tag.putInt("EffectFlags", this.getEffectFlags());
        tag.putFloat("Damage", this.damage);
        tag.putInt("TargetId", this.targetId);
        tag.putDouble("LegacyTargetX", this.legacyTargetPosition.x);
        tag.putDouble("LegacyTargetY", this.legacyTargetPosition.y);
        tag.putDouble("LegacyTargetZ", this.legacyTargetPosition.z);
        tag.putInt("LifeTicks", this.lifeTicks);
        tag.putBoolean("MissedShot", this.missedShot);
        tag.putBoolean("BlackHoleOnImpact", this.blackHoleOnImpact);
        tag.putBoolean("ClusterMissile", this.clusterMissile);
        tag.putBoolean("ClusterSubMissile", this.clusterSubMissile);
        tag.putBoolean("ReportMissFeedback", this.reportMissFeedback);
        tag.putDouble("KnockbackStrength", this.knockbackStrength);
        tag.putFloat("SpeedScale", this.speedScale);
        tag.putFloat("GuidanceBlend", this.guidanceBlend);
        tag.putFloat("TravelSpeed", this.travelSpeed);
        tag.putFloat("TargetHeightFactor", this.targetHeightFactor);
        tag.putDouble("ImpactRadius", this.impactRadius);
        tag.putBoolean("LegacyMotionOverride", this.hasLegacyMotionOverride());
        tag.putDouble("LegacyArcAcceleration1", this.getLegacyArcAcceleration1());
        tag.putDouble("LegacyArcAcceleration2", this.getLegacyArcAcceleration2());
        tag.putInt("LegacyArcRiseTicks", this.getLegacyArcRiseTicks());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.setAttackKind(attackKindByOrdinal(tag.getInt("AttackKind")));
        this.setProjectileVisual(LegacyShipProjectileVisual.byOrdinal(tag.getInt("ProjectileVisual")));
        this.setMoveType(LegacyShipProjectileMoveType.byOrdinal(tag.getInt("MoveType")));
        this.setLaunchParticleType(GameplayParticleType.fromOrdinal(tag.getInt("LaunchParticleType")));
        this.setTrailParticleType(GameplayParticleType.fromOrdinal(tag.getInt("TrailParticleType")));
        this.setHitParticleType(GameplayParticleType.fromOrdinal(tag.getInt("HitParticleType")));
        this.setEffectFlags(tag.getInt("EffectFlags"));
        this.damage = tag.getFloat("Damage");
        this.targetId = tag.getInt("TargetId");
        this.legacyTargetPosition = tag.contains("LegacyTargetX")
                && tag.contains("LegacyTargetY")
                && tag.contains("LegacyTargetZ")
                ? new Vec3(tag.getDouble("LegacyTargetX"), tag.getDouble("LegacyTargetY"),
                        tag.getDouble("LegacyTargetZ"))
                : Vec3.ZERO;
        this.lifeTicks = tag.getInt("LifeTicks");
        this.missedShot = tag.getBoolean("MissedShot");
        this.blackHoleOnImpact = tag.getBoolean("BlackHoleOnImpact");
        this.clusterMissile = tag.getBoolean("ClusterMissile");
        this.clusterSubMissile = tag.getBoolean("ClusterSubMissile");
        this.reportMissFeedback = !tag.contains("ReportMissFeedback") || tag.getBoolean("ReportMissFeedback");
        this.knockbackStrength = tag.contains("KnockbackStrength")
                ? Math.max(0.0D, tag.getDouble("KnockbackStrength")) : 0.15D;
        this.speedScale = tag.contains("SpeedScale") ? tag.getFloat("SpeedScale") : 1.0F;
        this.guidanceBlend = tag.contains("GuidanceBlend") ? tag.getFloat("GuidanceBlend") : 0.18F;
        this.travelSpeed = tag.contains("TravelSpeed") ? tag.getFloat("TravelSpeed") : 0.52F;
        this.targetHeightFactor = tag.contains("TargetHeightFactor") ? tag.getFloat("TargetHeightFactor") : 0.35F;
        this.impactRadius = tag.contains("ImpactRadius") ? Math.max(0.0D, tag.getDouble("ImpactRadius")) : 3.5D;
        this.entityData.set(DATA_LEGACY_MOTION_OVERRIDE, tag.getBoolean("LegacyMotionOverride"));
        this.entityData.set(DATA_LEGACY_ARC_ACCELERATION_1,
                tag.contains("LegacyArcAcceleration1") ? (float) tag.getDouble("LegacyArcAcceleration1") : 0.0F);
        this.entityData.set(DATA_LEGACY_ARC_ACCELERATION_2,
                tag.contains("LegacyArcAcceleration2") ? (float) tag.getDouble("LegacyArcAcceleration2") : 0.0F);
        this.entityData.set(DATA_LEGACY_ARC_RISE_TICKS,
                tag.contains("LegacyArcRiseTicks") ? Math.max(0, tag.getInt("LegacyArcRiseTicks")) : 0);
    }

    public LegacyShipAttackKind getAttackKind() {
        return attackKindByOrdinal(this.entityData.get(DATA_ATTACK_KIND));
    }

    public LegacyShipProjectileVisual getProjectileVisual() {
        return LegacyShipProjectileVisual.byOrdinal(this.entityData.get(DATA_PROJECTILE_VISUAL));
    }

    public LegacyShipProjectileMoveType getMoveType() {
        return LegacyShipProjectileMoveType.byOrdinal(this.entityData.get(DATA_MOVE_TYPE));
    }

    public GameplayParticleType getLaunchParticleType() {
        return GameplayParticleType.fromOrdinal(this.entityData.get(DATA_LAUNCH_PARTICLE));
    }

    public GameplayParticleType getTrailParticleType() {
        return GameplayParticleType.fromOrdinal(this.entityData.get(DATA_TRAIL_PARTICLE));
    }

    public GameplayParticleType getHitParticleType() {
        return GameplayParticleType.fromOrdinal(this.entityData.get(DATA_HIT_PARTICLE));
    }

    public int getIntendedTargetId() {
        return this.targetId;
    }

    public Vec3 getLegacyTargetPosition() {
        return this.legacyTargetPosition;
    }

    public boolean isMissedShot() {
        return this.missedShot;
    }

    public boolean hasBlackHoleImpactEffect() {
        return this.blackHoleOnImpact;
    }

    public double getImpactRadius() {
        return this.impactRadius;
    }

    public double getKnockbackStrength() {
        return this.knockbackStrength;
    }

    public boolean hasFlarePayload() {
        return (this.getEffectFlags() & EFFECT_FLAG_FLARE) != 0;
    }

    public boolean hasSearchlightPayload() {
        return (this.getEffectFlags() & EFFECT_FLAG_SEARCHLIGHT) != 0;
    }

    private void setAttackKind(LegacyShipAttackKind attackKind) {
        this.entityData.set(DATA_ATTACK_KIND, attackKind.ordinal());
    }

    private void setProjectileVisual(LegacyShipProjectileVisual projectileVisual) {
        this.entityData.set(DATA_PROJECTILE_VISUAL, projectileVisual.ordinal());
    }

    private void setMoveType(LegacyShipProjectileMoveType moveType) {
        this.entityData.set(DATA_MOVE_TYPE, moveType.ordinal());
    }

    private void setLaunchParticleType(GameplayParticleType particleType) {
        this.entityData.set(DATA_LAUNCH_PARTICLE, particleType.ordinal());
    }

    private void setTrailParticleType(GameplayParticleType particleType) {
        this.entityData.set(DATA_TRAIL_PARTICLE, particleType.ordinal());
    }

    private void setHitParticleType(GameplayParticleType particleType) {
        this.entityData.set(DATA_HIT_PARTICLE, particleType.ordinal());
    }

    private int getEffectFlags() {
        return this.entityData.get(DATA_EFFECT_FLAGS);
    }

    private void setEffectFlags(int effectFlags) {
        this.entityData.set(DATA_EFFECT_FLAGS, effectFlags);
    }

    private boolean hasLegacyMotionOverride() {
        return this.entityData.get(DATA_LEGACY_MOTION_OVERRIDE);
    }

    private void setLegacyMotionOverride(boolean enabled) {
        this.entityData.set(DATA_LEGACY_MOTION_OVERRIDE, enabled);
    }

    private double getLegacyArcAcceleration1() {
        return this.entityData.get(DATA_LEGACY_ARC_ACCELERATION_1).doubleValue();
    }

    private double getLegacyArcAcceleration2() {
        return this.entityData.get(DATA_LEGACY_ARC_ACCELERATION_2).doubleValue();
    }

    private int getLegacyArcRiseTicks() {
        return this.entityData.get(DATA_LEGACY_ARC_RISE_TICKS);
    }

    private void setLegacyArcParameters(double acceleration1, double acceleration2, int riseTicks) {
        this.entityData.set(DATA_LEGACY_ARC_ACCELERATION_1, (float) acceleration1);
        this.entityData.set(DATA_LEGACY_ARC_ACCELERATION_2, (float) acceleration2);
        this.entityData.set(DATA_LEGACY_ARC_RISE_TICKS, Math.max(0, riseTicks));
    }

    private void configureLegacyArcMotion(Vec3 targetPosition, float arcHeightRatio) {
        this.setLegacyMotionOverride(true);
        this.setLegacyArcParameters(0.0D, 0.0D, 0);

        Vec3 distanceVector = targetPosition.subtract(this.position());
        double distance = distanceVector.length();
        double speed = this.baseSpeed();
        if (distance <= 1.0E-7D || speed <= 1.0E-7D) {
            this.setMoveType(LegacyShipProjectileMoveType.DIRECT);
            this.setDeltaMovement(Vec3.ZERO);
            return;
        }

        Vec3 directMotion = distanceVector.normalize().scale(speed);
        if (distance < 4.0D) {
            this.setMoveType(LegacyShipProjectileMoveType.DIRECT);
            this.setDeltaMovement(directMotion);
            return;
        }

        if (arcHeightRatio <= 0.0F) {
            this.setMoveType(LegacyShipProjectileMoveType.DIRECT);
            this.setDeltaMovement(directMotion);
            return;
        }

        double horizontalX = distanceVector.x;
        double horizontalZ = distanceVector.z;
        double horizontalDistance = Math.sqrt(horizontalX * horizontalX + horizontalZ * horizontalZ);
        if (horizontalDistance <= 4.0D) {
            // EntityAbyssMissile keeps moveType=1 here, but uses direct XYZ motion with zero acceleration.
            this.setDeltaMovement(directMotion);
            return;
        }

        horizontalX /= horizontalDistance;
        horizontalZ /= horizontalDistance;
        double travelTicks = horizontalDistance / speed;
        double additionalHeight = distance * arcHeightRatio;
        double verticalDistance = Math.abs(this.getY() - targetPosition.y);
        double heightRatio = Math.sqrt(additionalHeight / (additionalHeight + verticalDistance));
        int riseTicks;
        int fallTicks;
        double verticalSpeed;
        double acceleration1;
        double acceleration2;

        if (this.getY() - targetPosition.y < 1.0D) {
            heightRatio = Math.sqrt(additionalHeight / (additionalHeight + verticalDistance));
            riseTicks = Mth.floor(travelTicks / (1.0D + heightRatio));
            fallTicks = Mth.floor(travelTicks * heightRatio / (1.0D + heightRatio));
            if (riseTicks <= 0 || fallTicks <= 0) {
                this.setMoveType(LegacyShipProjectileMoveType.DIRECT);
                this.setDeltaMovement(directMotion);
                return;
            }
            verticalSpeed = 2.0D * (additionalHeight + verticalDistance) / riseTicks;
            acceleration1 = -verticalSpeed / riseTicks;
            acceleration2 = -2.0D * additionalHeight / (fallTicks * fallTicks);
        } else {
            heightRatio = Math.sqrt(additionalHeight / (additionalHeight + verticalDistance));
            riseTicks = Mth.floor(travelTicks * heightRatio / (1.0D + heightRatio));
            fallTicks = Mth.floor(travelTicks / (1.0D + heightRatio));
            if (riseTicks <= 0 || fallTicks <= 0) {
                this.setMoveType(LegacyShipProjectileMoveType.DIRECT);
                this.setDeltaMovement(directMotion);
                return;
            }
            acceleration1 = -2.0D * additionalHeight / (riseTicks * riseTicks);
            verticalSpeed = -acceleration1 * riseTicks;
            acceleration2 = -2.0D * (additionalHeight + verticalDistance) / (fallTicks * fallTicks);
        }

        if (Math.abs(acceleration1) > LEGACY_ARC_ACCELERATION_LIMIT
                || Math.abs(acceleration2) > LEGACY_ARC_ACCELERATION_LIMIT) {
            this.setMoveType(LegacyShipProjectileMoveType.DIRECT);
            this.setDeltaMovement(directMotion);
            return;
        }

        this.setDeltaMovement(horizontalX * speed, verticalSpeed, horizontalZ * speed);
        this.setLegacyArcParameters(acceleration1, acceleration2, riseTicks);
    }

    private void configureLegacyTorpedoMotion(Vec3 targetPosition) {
        Vec3 distanceVector = targetPosition.subtract(this.position());
        if (distanceVector.lengthSqr() <= 1.0E-7D) {
            this.setMoveType(LegacyShipProjectileMoveType.DIRECT);
            this.setDeltaMovement(Vec3.ZERO);
            this.setLegacyMotionOverride(false);
            return;
        }

        Vec3 direction = distanceVector.normalize();
        this.setLegacyMotionOverride(true);
        this.setLegacyArcParameters(-0.035D, 1.04D, 0);
        this.entityData.set(DATA_LEGACY_TORPEDO_STARTED, false);
        this.torpedoStartMoveDelay = 0;
        this.setDeltaMovement(direction.x * 0.6D, 0.1D, direction.z * 0.6D);
    }

    private void advanceLegacyMotion(Vec3 currentMotion) {
        if (this.getMoveType() == LegacyShipProjectileMoveType.TORPEDO) {
            boolean started = this.entityData.get(DATA_LEGACY_TORPEDO_STARTED);
            if (!this.level().isClientSide()) {
                if (started) {
                    if (this.torpedoStartMoveDelay >= 0) {
                        this.torpedoStartMoveDelay--;
                        if (this.torpedoStartMoveDelay < 0) {
                            Vec3 offset = this.legacyTargetPosition.subtract(this.position());
                            double dist = offset.length();
                            if (dist > 1.0E-7D) {
                                double vel0 = this.baseSpeed();
                                double vx = (offset.x / dist) * vel0 * 0.25D;
                                double vy = Math.min(0.003D, (offset.y / dist) * vel0 * 0.25D);
                                double vz = (offset.z / dist) * vel0 * 0.25D;
                                this.setDeltaMovement(vx, vy, vz);
                            } else {
                                this.setDeltaMovement(Vec3.ZERO);
                            }
                        } else {
                            this.setDeltaMovement(currentMotion);
                        }
                    } else if (currentMotion.lengthSqr() < 2.0D) {
                        this.setDeltaMovement(currentMotion.scale(this.getLegacyArcAcceleration2()));
                    } else {
                        this.setDeltaMovement(currentMotion);
                    }
                } else {
                    this.setDeltaMovement(
                            currentMotion.x * 0.85D,
                            currentMotion.y + this.getLegacyArcAcceleration1(),
                            currentMotion.z * 0.85D);
                    if (!this.level().getFluidState(this.blockPosition()).isEmpty()) {
                        this.entityData.set(DATA_LEGACY_TORPEDO_STARTED, true);
                        this.torpedoStartMoveDelay = 3;
                    }
                }
            } else {
                if (started) {
                    if (currentMotion.lengthSqr() < 2.0D) {
                        this.setDeltaMovement(currentMotion.scale(this.getLegacyArcAcceleration2()));
                    } else {
                        this.setDeltaMovement(currentMotion);
                    }
                } else {
                    this.setDeltaMovement(
                            currentMotion.x * 0.85D,
                            currentMotion.y + this.getLegacyArcAcceleration1(),
                            currentMotion.z * 0.85D);
                }
            }
            return;
        }

        double acceleration = this.tickCount <= this.getLegacyArcRiseTicks()
                ? this.getLegacyArcAcceleration1()
                : this.getLegacyArcAcceleration2();
        this.setDeltaMovement(currentMotion.x, currentMotion.y + acceleration, currentMotion.z);
    }

    private void spawnImpactEffect(LegacyShipEntity ship, Vec3 impactPosition) {
        if (!this.blackHoleOnImpact || this.impactEffectSpawned || this.level().isClientSide()) {
            return;
        }

        this.impactEffectSpawned = LegacyShipBehaviorCatalog.spawnBlackHoleEffect(
                ship, impactPosition) != null;
    }

    private void resolveAreaImpact(Vec3 impactPosition) {
        if (this.resolvedImpact || this.level().isClientSide()) {
            return;
        }

        Entity owner = this.getOwner();
        this.resolvedImpact = true;
        if (!(owner instanceof LegacyShipEntity ship)) {
            return;
        }

        this.spawnImpactEffect(ship, impactPosition);
        AABB impactBox = new AABB(impactPosition, impactPosition).inflate(this.impactRadius);
        List<LivingEntity> areaTargets = this.level().getEntitiesOfClass(
                LivingEntity.class,
                impactBox,
                candidate -> candidate != ship
                        && candidate.isAlive()
                        && !candidate.isInvulnerable()
                        && ship.canEngage(candidate));
        for (LivingEntity livingTarget : areaTargets) {
            float damage = LegacyShipCombatHelper.applyTargetDamageModifier(
                    ship.getLegacyStats(), livingTarget, this.damage);
            LegacyShipCombatHelper.AttackRoll roll = LegacyShipCombatHelper.rollProjectileImpactAttack(
                    ship, livingTarget, damage);
            if (roll.miss()) {
                continue;
            }
            boolean attacked = livingTarget.hurt(this.damageSources().explosion(this, ship), roll.damage());
            if (attacked) {
                ship.setLastHurtMob(livingTarget);
                LegacyShipBehaviorCatalog.applyAttackEffect(ship, livingTarget);
                livingTarget.knockback(this.knockbackStrength,
                        ship.getX() - livingTarget.getX(), ship.getZ() - livingTarget.getZ());
            }
        }

        Vec3 motion = this.getDeltaMovement();
        CombatFxDispatcher.sendParticle(ship, this.getHitParticleType(),
                impactPosition.x, impactPosition.y, impactPosition.z,
                motion.x, motion.y, motion.z);
    }

    private void updateFlightPath() {
        if (this.clusterSubMissile) {
            Vec3 motion = this.getDeltaMovement();
            this.setDeltaMovement(motion.x * 0.95D, motion.y - 0.06D, motion.z * 0.95D);
            return;
        }

        switch (this.getMoveType()) {
            case GUIDED -> this.updateGuidedFlight();
            case DROP -> this.updateBombDropFlight();
            default -> {
            }
        }
    }

    /** Mirrors EntityAbyssMissile's type-3 five-wave sub-missile release. */
    private void spawnClusterSubMissile() {
        if (this.tickCount <= 6 || this.tickCount >= 41
                || (this.tickCount & 7) != 0 || this.level().isClientSide()) {
            return;
        }

        Entity owner = this.getOwner();
        if (!(owner instanceof LegacyShipEntity ship)) {
            return;
        }

        LivingEntity target = this.getIntendedTarget();
        Vec3 motion = this.getDeltaMovement();
        Vec3 targetPosition = this.position().add(motion);
        LegacyShipProjectileEntity subMissile = create(
                this.level(), ship, target, LegacyShipAttackKind.HEAVY,
                this.damage * 0.5F, false,
                LegacyShipAttackProfile.clusterSubMissileProfile(), targetPosition,
                this.knockbackStrength, false, 3.5D);
        subMissile.clusterMissile = false;
        subMissile.clusterSubMissile = true;
        subMissile.moveTo(this.getX(),
                this.getY() - 0.65D - Math.abs(motion.y),
                this.getZ(), this.getYRot(), this.getXRot());
        subMissile.setDeltaMovement(motion);
        this.level().addFreshEntity(subMissile);
    }

    private void updateGuidedFlight() {
        if (this.missedShot) {
            return;
        }

        if (this.tickCount > 14) {
            return;
        }

        LivingEntity target = this.getIntendedTarget();
        if (target == null || !target.isAlive()) {
            return;
        }

        Entity owner = this.getOwner();
        if (owner instanceof LegacyShipEntity ship && !ship.getSensing().hasLineOfSight(target)) {
            return;
        }

        double speed = this.baseSpeed();
        Vec3 desired = target.position().add(0.0D, target.getBbHeight() * this.targetHeightFactor, 0.0D).subtract(this.position());
        if (desired.lengthSqr() < 1.0E-4D) {
            return;
        }

        Vec3 current = this.getDeltaMovement();
        Vec3 redirected = current.scale(1.0D - this.guidanceBlend).add(desired.normalize().scale(speed * this.guidanceBlend));
        if (redirected.lengthSqr() < 1.0E-6D) {
            redirected = desired.normalize().scale(speed);
        } else {
            redirected = redirected.normalize().scale(speed);
        }

        this.setDeltaMovement(redirected);
    }

    private void updateBombDropFlight() {
        // Legacy bomb drops keep their initial release vector after launch instead of steering into the target.
    }

    private void spawnTrailParticles() {
        if (this.clusterSubMissile && this.tickCount <= 4) {
            return;
        }
        GameplayParticleType particleType = this.getTrailParticleType();
        if (particleType == GameplayParticleType.NONE || this.tickCount < 2) {
            return;
        }

        Vec3 motion = this.getDeltaMovement();
        double x = this.getX() - motion.x * 0.25D;
        double y = this.getY() - motion.y * 0.25D;
        double z = this.getZ() - motion.z * 0.25D;

        switch (particleType) {
            case MISSILE_TRAIL -> {
                this.level().addParticle(ParticleTypes.SMOKE, x, y, z, -motion.x * 0.04D, -motion.y * 0.04D, -motion.z * 0.04D);
                this.level().addParticle(ParticleTypes.FLAME, x, y, z, -motion.x * 0.02D, -motion.y * 0.02D, -motion.z * 0.02D);
            }
            case LEGACY_RAILGUN -> {
                this.level().addParticle(ParticleTypes.FLAME, this.getX(), this.getY(), this.getZ(),
                        motion.x * 0.02D, motion.y * 0.02D, motion.z * 0.02D);
                this.level().addParticle(ParticleTypes.ELECTRIC_SPARK, this.getX(), this.getY(), this.getZ(),
                        motion.x * 0.04D, motion.y * 0.04D, motion.z * 0.04D);
            }
            case AIRCRAFT_TRAIL -> this.level().addParticle(ParticleTypes.CLOUD, x, y, z, -motion.x * 0.02D, 0.0D, -motion.z * 0.02D);
            case BOMB_DROP -> this.level().addParticle(ParticleTypes.SMOKE, x, y, z, 0.0D, -0.01D, 0.0D);
            case TORPEDO_WAKE -> {
                this.level().addParticle(ParticleTypes.BUBBLE, x, y, z, -motion.x * 0.03D, 0.01D, -motion.z * 0.03D);
                this.level().addParticle(ParticleTypes.SPLASH, x, y, z, 0.0D, 0.02D, 0.0D);
            }
            default -> {
            }
        }

        if (this.hasFlarePayload() && this.tickCount % 3 == 0) {
            this.level().addParticle(ParticleTypes.END_ROD, x, y, z, 0.0D, 0.01D, 0.0D);
        }
        if (this.hasSearchlightPayload() && this.tickCount % 4 == 0) {
            this.level().addParticle(ParticleTypes.ELECTRIC_SPARK, x, y, z, 0.0D, 0.0D, 0.0D);
        }
    }

    private void emitMissFx(Vec3 impactPos) {
        if (this.resolvedImpact || this.level().isClientSide()) {
            return;
        }

        this.resolvedImpact = true;
        Entity owner = this.getOwner();
        if (!(owner instanceof LegacyShipEntity ship)) {
            return;
        }

        LivingEntity intendedTarget = this.getIntendedTarget();
        if (this.reportMissFeedback && intendedTarget != null && intendedTarget.isAlive()) {
            CombatFxDispatcher.sendCombatReact(ship, intendedTarget, CombatReactType.MISS,
                    this.getAttackKind(), this.getProjectileVisual());
            CombatFxDispatcher.sendParticle(intendedTarget, GameplayParticleType.TEXT_MISS,
                    intendedTarget.getX(), intendedTarget.getY() + intendedTarget.getBbHeight() * 0.8D, intendedTarget.getZ());
        }

        Vec3 motion = this.getDeltaMovement();
        CombatFxDispatcher.sendParticle(this, this.getHitParticleType(),
                impactPos.x, impactPos.y, impactPos.z,
                motion.x, motion.y, motion.z);
    }

    private @Nullable LivingEntity getIntendedTarget() {
        if (this.targetId < 0) {
            return null;
        }

        Entity entity = this.level().getEntity(this.targetId);
        return entity instanceof LivingEntity livingEntity ? livingEntity : null;
    }

    private Vec3 resolveTargetPos(LivingEntity target, LegacyShipProjectileProfile profile, boolean missedShot) {
        double targetX = target.getX();
        double targetY = target.getY() + target.getBbHeight() * profile.targetHeightFactor();
        double targetZ = target.getZ();

        Vec3 targetPos = new Vec3(targetX, targetY, targetZ);
        return missedShot ? this.offsetMissTarget(targetPos) : targetPos;
    }

    private Vec3 offsetMissTarget(Vec3 targetPos) {
        return targetPos.add(-5F + this.random.nextFloat() * 10F,
                this.random.nextFloat() * 5F, -5F + this.random.nextFloat() * 10F);
    }

    private float baseSpeed() {
        return this.travelSpeed * this.speedScale;
    }

    @Override
    public boolean isPickable() {
        return this.isAlive();
    }

    @Override
    public boolean hurt(DamageSource damageSource, float amount) {
        if (this.level().isClientSide()) {
            return false;
        }

        if (this.getOwner() == null) {
            this.discard();
            return false;
        }

        if (damageSource.is(DamageTypes.IN_WALL)
                || damageSource.is(DamageTypes.STARVE)
                || damageSource.is(DamageTypes.CACTUS)
                || damageSource.is(DamageTypes.FALL)
                || damageSource.is(DamageTypes.LAVA)
                || damageSource.is(DamageTypes.IN_FIRE)
                || damageSource.is(DamageTypes.HOT_FLOOR)
                || damageSource.is(DamageTypes.ON_FIRE)
                || damageSource.is(DamageTypes.FALLING_BLOCK)) {
            return false;
        }

        if (damageSource.is(DamageTypes.MAGIC)
                || damageSource.is(DamageTypes.DRAGON_BREATH)
                || damageSource.is(DamageTypes.WITHER)
                || damageSource.is(DamageTypes.FELL_OUT_OF_WORLD)) {
            this.detonateOnIntercept();
            return true;
        }

        if (this.random.nextFloat() <= 0.5F) {
            return false;
        }

        if (this.isInvulnerableTo(damageSource)) {
            return false;
        }

        if (this.isAlive() && amount > 8.0F) {
            this.detonateOnIntercept();
            return true;
        }

        return super.hurt(damageSource, amount);
    }

    private void detonateOnIntercept() {
        if (!this.resolvedImpact && !this.level().isClientSide()) {
            if (this.impactRadius > 0.0D) {
                this.resolveAreaImpact(this.position());
            } else {
                if (this.getOwner() instanceof LegacyShipEntity ship) {
                    this.spawnImpactEffect(ship, this.position());
                }
                this.emitMissFx(this.position());
            }
        }
        this.discard();
    }

    private static LegacyShipAttackKind attackKindByOrdinal(int ordinal) {
        LegacyShipAttackKind[] values = LegacyShipAttackKind.values();
        return values[Mth.clamp(ordinal, 0, values.length - 1)];
    }
}
