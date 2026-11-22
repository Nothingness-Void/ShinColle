package com.lulan.shincolle.entity.ship;

import com.lulan.shincolle.entity.projectile.LegacyShipProjectileProfile;
import com.lulan.shincolle.entity.projectile.LegacyShipProjectileVisual;
import com.lulan.shincolle.entity.ship.path.ShipPath;
import com.lulan.shincolle.entity.ship.path.ShipPathFinder;
import com.lulan.shincolle.network.CombatFxDispatcher;
import com.lulan.shincolle.network.GameplayParticleType;
import com.lulan.shincolle.registry.ModEntityTypes;
import com.lulan.shincolle.registry.ModSoundEvents;
import com.lulan.shincolle.config.ShinColleConfig;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.Comparator;
import java.util.List;

public abstract class LegacyShipAircraftEntity extends Entity {

    private static final String ATTACK_KIND_TAG = "AttackKind";
    private static final String PROJECTILE_VISUAL_TAG = "ProjectileVisual";
    private static final String LAUNCH_PARTICLE_TAG = "LaunchParticleType";
    private static final String TRAIL_PARTICLE_TAG = "TrailParticleType";
    private static final String HIT_PARTICLE_TAG = "HitParticleType";
    private static final String EFFECT_FLAGS_TAG = "EffectFlags";
    private static final String OWNER_ID_TAG = "OwnerId";
    private static final String TARGET_ID_TAG = "TargetId";
    private static final String MAX_LIFE_TICKS_TAG = "MaxLifeTicks";
    private static final String ATTACK_COOLDOWN_TAG = "AttackCooldown";
    private static final String REMAINING_ATTACKS_TAG = "RemainingAttacks";
    private static final String INITIAL_STRAIGHT_TICKS_TAG = "InitialStraightTicks";
    private static final String RETURNING_TAG = "Returning";
    private static final String SPEED_SCALE_TAG = "SpeedScale";
    private static final String TURN_BLEND_TAG = "TurnBlend";
    private static final String TARGET_HEIGHT_FACTOR_TAG = "TargetHeightFactor";
    private static final String HEALTH_TAG = "Health";
    private static final String MAX_HEALTH_TAG = "MaxHealth";

    private static final EntityDataAccessor<Integer> DATA_ATTACK_KIND =
            SynchedEntityData.defineId(LegacyShipAircraftEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_PROJECTILE_VISUAL =
            SynchedEntityData.defineId(LegacyShipAircraftEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_LAUNCH_PARTICLE =
            SynchedEntityData.defineId(LegacyShipAircraftEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_TRAIL_PARTICLE =
            SynchedEntityData.defineId(LegacyShipAircraftEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_HIT_PARTICLE =
            SynchedEntityData.defineId(LegacyShipAircraftEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_EFFECT_FLAGS =
            SynchedEntityData.defineId(LegacyShipAircraftEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_OWNER_ID =
            SynchedEntityData.defineId(LegacyShipAircraftEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_TARGET_ID =
            SynchedEntityData.defineId(LegacyShipAircraftEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> DATA_RETURNING =
            SynchedEntityData.defineId(LegacyShipAircraftEntity.class, EntityDataSerializers.BOOLEAN);

    private static final int EFFECT_FLAG_FLARE = 1;
    private static final int EFFECT_FLAG_SEARCHLIGHT = 1 << 1;
    // BasicEntityAirplane returns at tick 1200 and BasicEntitySummon expires at 1800.
    private static final int LEGACY_RETURN_TICKS = 20 * 60;
    private static final int DEFAULT_MAX_LIFE_TICKS = 20 * 90;
    private static final int DEFAULT_INITIAL_STRAIGHT_TICKS = 34;
    private static final int LEGACY_LIGHT_AIRCRAFT_AMMO = 9;
    private static final int LEGACY_HEAVY_AIRCRAFT_AMMO = 3;
    private static final double LEGACY_INITIAL_HORIZONTAL_SPEED = 0.375D;
    private static final double LEGACY_INITIAL_VERTICAL_SPEED = 0.1D;

    private int maxLifeTicks = DEFAULT_MAX_LIFE_TICKS;
    private int attackCooldown;
    private int remainingAttacks = 1;
    private int initialStraightTicks = DEFAULT_INITIAL_STRAIGHT_TICKS;
    private float speedScale = 1.0F;
    private float turnBlend = 0.16F;
    private float targetHeightFactor = 0.55F;
    private float maxHp = 20.0F;
    private float hp = 20.0F;
    private boolean returnedToOwner;
    private @Nullable ShipPath obstaclePath;
    private @Nullable BlockPos obstacleTarget;
    private int nextObstaclePathTick;

    protected LegacyShipAircraftEntity(EntityType<? extends LegacyShipAircraftEntity> entityType, Level level) {
        super(entityType, level);
        this.setNoGravity(true);
    }

    public static LegacyShipAircraftEntity create(Level level, LegacyShipEntity owner, LivingEntity target, LegacyShipAttackKind attackKind) {
        LegacyShipProjectileProfile profile = owner.getAttackProfile().projectileProfile(attackKind);
        LegacyShipAircraftEntity aircraft = attackKind == LegacyShipAttackKind.AIR_HEAVY
                ? ModEntityTypes.LEGACY_SHIP_TAKOYAKI.get().create(level)
                : ModEntityTypes.LEGACY_SHIP_AIRCRAFT.get().create(level);
        if (aircraft == null) {
            return null;
        }

        aircraft.setAttackKind(attackKind);
        aircraft.setProjectileVisual(profile.visual());
        aircraft.setLaunchParticleType(profile.launchParticleType());
        aircraft.setTrailParticleType(profile.trailParticleType());
        aircraft.setHitParticleType(profile.hitParticleType());
        aircraft.setOwnerId(owner.getId());
        aircraft.setTargetId(target.getId());
        aircraft.targetHeightFactor = profile.targetHeightFactor();
        aircraft.maxLifeTicks = DEFAULT_MAX_LIFE_TICKS;
        aircraft.initialStraightTicks = DEFAULT_INITIAL_STRAIGHT_TICKS;
        aircraft.remainingAttacks = attackKind == LegacyShipAttackKind.AIR_HEAVY
                ? LEGACY_HEAVY_AIRCRAFT_AMMO : LEGACY_LIGHT_AIRCRAFT_AMMO;
        aircraft.attackCooldown = 0;
        aircraft.speedScale = 1.0F;
        aircraft.turnBlend = attackKind == LegacyShipAttackKind.AIR_HEAVY ? 0.12F : 0.18F;
        aircraft.maxHp = Math.max(1.0F, owner.getShipLevel()
                + owner.getMaxHealth() * (aircraft.isHeavyAirframe() ? 0.15F : 0.1F));
        aircraft.hp = aircraft.maxHp;

        int effectFlags = 0;
        if (owner.getEquipmentBehaviorState().flareLevel() > 0) {
            effectFlags |= EFFECT_FLAG_FLARE;
        }
        if (owner.getEquipmentBehaviorState().searchlightLevel() > 0) {
            effectFlags |= EFFECT_FLAG_SEARCHLIGHT;
        }
        aircraft.setEffectFlags(effectFlags);
        aircraft.refreshDimensions();

        double launchY = owner.getY() + owner.getBbHeight()
                * LegacyShipBehaviorCatalog.aircraftLaunchHeightFactorFor(owner.getSpec(), attackKind);
        aircraft.moveTo(owner.getX(), launchY, owner.getZ(), owner.getYRot(), owner.getXRot());

        aircraft.setDeltaMovement(legacyInitialStraightMovement(
                aircraft.position(), target, owner.getLookAngle()));
        return aircraft;
    }

    @Override
    protected void defineSynchedData() {
        this.entityData.define(DATA_ATTACK_KIND, LegacyShipAttackKind.AIR_LIGHT.ordinal());
        this.entityData.define(DATA_PROJECTILE_VISUAL, LegacyShipProjectileVisual.AIRPLANE.ordinal());
        this.entityData.define(DATA_LAUNCH_PARTICLE, GameplayParticleType.AIRCRAFT_LAUNCH.ordinal());
        this.entityData.define(DATA_TRAIL_PARTICLE, GameplayParticleType.AIRCRAFT_TRAIL.ordinal());
        this.entityData.define(DATA_HIT_PARTICLE, GameplayParticleType.AIRCRAFT_IMPACT.ordinal());
        this.entityData.define(DATA_EFFECT_FLAGS, 0);
        this.entityData.define(DATA_OWNER_ID, -1);
        this.entityData.define(DATA_TARGET_ID, -1);
        this.entityData.define(DATA_RETURNING, false);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        this.setAttackKind(LegacyShipAttackKind.byOrdinal(tag.getInt(ATTACK_KIND_TAG)));
        this.setProjectileVisual(LegacyShipProjectileVisual.byOrdinal(tag.getInt(PROJECTILE_VISUAL_TAG)));
        this.setLaunchParticleType(GameplayParticleType.fromOrdinal(tag.getInt(LAUNCH_PARTICLE_TAG)));
        this.setTrailParticleType(GameplayParticleType.fromOrdinal(tag.getInt(TRAIL_PARTICLE_TAG)));
        this.setHitParticleType(GameplayParticleType.fromOrdinal(tag.getInt(HIT_PARTICLE_TAG)));
        this.setEffectFlags(tag.getInt(EFFECT_FLAGS_TAG));
        this.setOwnerId(tag.getInt(OWNER_ID_TAG));
        this.setTargetId(tag.getInt(TARGET_ID_TAG));
        this.setReturning(tag.getBoolean(RETURNING_TAG));
        this.maxLifeTicks = tag.contains(MAX_LIFE_TICKS_TAG)
                ? Math.max(DEFAULT_MAX_LIFE_TICKS, tag.getInt(MAX_LIFE_TICKS_TAG))
                : DEFAULT_MAX_LIFE_TICKS;
        this.attackCooldown = Math.max(0, tag.getInt(ATTACK_COOLDOWN_TAG));
        int defaultRemainingAttacks = this.getAttackKind() == LegacyShipAttackKind.AIR_HEAVY
                ? LEGACY_HEAVY_AIRCRAFT_AMMO : LEGACY_LIGHT_AIRCRAFT_AMMO;
        this.remainingAttacks = tag.contains(REMAINING_ATTACKS_TAG)
                ? Math.max(0, tag.getInt(REMAINING_ATTACKS_TAG)) : defaultRemainingAttacks;
        this.initialStraightTicks = tag.contains(INITIAL_STRAIGHT_TICKS_TAG)
                ? Math.max(DEFAULT_INITIAL_STRAIGHT_TICKS, tag.getInt(INITIAL_STRAIGHT_TICKS_TAG))
                : DEFAULT_INITIAL_STRAIGHT_TICKS;
        this.speedScale = tag.contains(SPEED_SCALE_TAG) ? tag.getFloat(SPEED_SCALE_TAG) : 1.0F;
        this.turnBlend = tag.contains(TURN_BLEND_TAG) ? tag.getFloat(TURN_BLEND_TAG) : 0.16F;
        this.targetHeightFactor = tag.contains(TARGET_HEIGHT_FACTOR_TAG) ? tag.getFloat(TARGET_HEIGHT_FACTOR_TAG) : 0.55F;
        this.maxHp = tag.contains(MAX_HEALTH_TAG) ? Math.max(1.0F, tag.getFloat(MAX_HEALTH_TAG)) : 20.0F;
        this.hp = tag.contains(HEALTH_TAG) ? Math.max(0.0F, tag.getFloat(HEALTH_TAG)) : this.maxHp;
        this.refreshDimensions();
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        tag.putInt(ATTACK_KIND_TAG, this.getAttackKind().ordinal());
        tag.putInt(PROJECTILE_VISUAL_TAG, this.getProjectileVisual().ordinal());
        tag.putInt(LAUNCH_PARTICLE_TAG, this.getLaunchParticleType().ordinal());
        tag.putInt(TRAIL_PARTICLE_TAG, this.getTrailParticleType().ordinal());
        tag.putInt(HIT_PARTICLE_TAG, this.getHitParticleType().ordinal());
        tag.putInt(EFFECT_FLAGS_TAG, this.getEffectFlags());
        tag.putInt(OWNER_ID_TAG, this.getOwnerId());
        tag.putInt(TARGET_ID_TAG, this.getTargetId());
        tag.putInt(MAX_LIFE_TICKS_TAG, this.maxLifeTicks);
        tag.putInt(ATTACK_COOLDOWN_TAG, this.attackCooldown);
        tag.putInt(REMAINING_ATTACKS_TAG, this.remainingAttacks);
        tag.putInt(INITIAL_STRAIGHT_TICKS_TAG, this.initialStraightTicks);
        tag.putBoolean(RETURNING_TAG, this.isReturning());
        tag.putFloat(SPEED_SCALE_TAG, this.speedScale);
        tag.putFloat(TURN_BLEND_TAG, this.turnBlend);
        tag.putFloat(TARGET_HEIGHT_FACTOR_TAG, this.targetHeightFactor);
        tag.putFloat(HEALTH_TAG, this.hp);
        tag.putFloat(MAX_HEALTH_TAG, this.maxHp);
    }

    @Override
    public void tick() {
        super.tick();

        if (!this.isReturning() && this.tickCount >= LEGACY_RETURN_TICKS) {
            this.setTargetId(-1);
            this.setReturning(true);
        }

        if (this.tickCount > this.maxLifeTicks) {
            LegacyShipEntity owner = this.getOwnerShip();
            if (owner != null) {
                this.returnAircraftToOwner(owner);
            }
            this.discard();
            return;
        }

        if (this.level().isClientSide()) {
            this.spawnTrailParticles();
            this.move(MoverType.SELF, this.getDeltaMovement());
            return;
        }

        LegacyShipEntity owner = this.getOwnerShip();
        if (owner == null || !owner.isAlive()) {
            if (owner != null) {
                this.returnAircraftToOwner(owner);
            }
            this.discard();
            return;
        }

        if (LegacyShipBehaviorCatalog.aircraftVariantLaunchSoundAtTick(owner.getSpec(), this.tickCount)) {
            float pitch = 0.7F / (this.random.nextFloat() * 0.4F + 0.8F);
            this.level().playSound(null, this.blockPosition(), ModSoundEvents.SHIP_AIRCRAFT.get(),
                    owner.getSoundSource(), 0.4F, pitch);
        }

        LivingEntity target = this.resolveAttackTarget(owner);
        if (target == null && !this.isReturning()) {
            this.setReturning(true);
        }

        if (this.attackCooldown > 0) {
            this.attackCooldown--;
        }

        if (!this.isReturning() && target != null && this.canStrikeTarget(owner, target)) {
            boolean struck = owner.performCompatAircraftStrike(this, target, this.getAttackKind(),
                    this.getProjectileVisual(), this.getHitParticleType(), this.hasFlarePayload(),
                    this.hasSearchlightPayload());
            if (struck) {
                this.remainingAttacks--;
                if (this.remainingAttacks <= 0) {
                    this.setReturning(true);
                }
                // EntityAIShipAircraftAttack uses 100 / aircraft.SPD + 35 with aircraft SPD = ship.SPD * (2.5F or 3.0F).
                float aircraftSpd = Math.max(0.01F, owner.getLegacyStats().attackSpeed()
                        * (this.isHeavyAirframe() ? 2.5F : 3.0F));
                this.attackCooldown = Math.max(1, (int) (100.0F / aircraftSpd) + 35);
            }
        }

        this.updateMotion(owner, target);
        this.move(MoverType.SELF, this.getDeltaMovement());
        this.checkReturnCompletion(owner);
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    public boolean canCollideWith(Entity entity) {
        return false;
    }

    @Override
    public boolean displayFireAnimation() {
        return false;
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> key) {
        super.onSyncedDataUpdated(key);
        if (key == DATA_ATTACK_KIND || key == DATA_PROJECTILE_VISUAL || key == DATA_OWNER_ID) {
            this.refreshDimensions();
        }
    }

    @Override
    public EntityDimensions getDimensions(Pose pose) {
        float baseSize = this.getProjectileVisual() == LegacyShipProjectileVisual.BOMB
                && !this.usesLegacySpecialCarrierAirframe() ? 0.6F : 0.5F;
        float scale = this.legacyHostileAirframeScale();
        return EntityDimensions.scalable(baseSize * scale, baseSize * scale);
    }

    public LegacyShipAttackKind getAttackKind() {
        return LegacyShipAttackKind.byOrdinal(this.entityData.get(DATA_ATTACK_KIND));
    }

    public LegacyShipProjectileVisual getProjectileVisual() {
        return LegacyShipProjectileVisual.byOrdinal(this.entityData.get(DATA_PROJECTILE_VISUAL));
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

    public boolean hasFlarePayload() {
        return (this.getEffectFlags() & EFFECT_FLAG_FLARE) != 0;
    }

    public boolean hasSearchlightPayload() {
        return (this.getEffectFlags() & EFFECT_FLAG_SEARCHLIGHT) != 0;
    }

    public boolean isReturning() {
        return this.entityData.get(DATA_RETURNING);
    }

    public int getOwnerId() {
        return this.entityData.get(DATA_OWNER_ID);
    }

    public int getTargetId() {
        return this.entityData.get(DATA_TARGET_ID);
    }

    public boolean usesLegacySpecialCarrierAirframe() {
        LegacyShipEntity owner = this.getOwnerShip();
        return owner != null && LegacyShipBehaviorCatalog.usesLegacySpecialCarrierAirframe(owner.getSpec());
    }

    protected abstract net.minecraft.resources.ResourceLocation getTextureLocation();

    protected float getRenderScale() {
        return this.getAttackKind() == LegacyShipAttackKind.AIR_HEAVY ? 0.72F : 0.64F;
    }

    protected boolean isHeavyAirframe() {
        return this.getAttackKind() == LegacyShipAttackKind.AIR_HEAVY;
    }

    protected double getStrikeRange() {
        return this.isHeavyAirframe() ? 16.0D : 6.0D;
    }

    protected double getTargetingRange() {
        return 24.0D;
    }

    private float legacyHostileAirframeScale() {
        LegacyShipEntity owner = this.getOwnerShip();
        if (owner == null || !owner.isHostileVariant()) {
            return 1.0F;
        }

        return switch (Mth.clamp(owner.getLegacyHostileScaleLevel(), 0, 3)) {
            case 1 -> 2.0F;
            case 2 -> 3.0F;
            case 3 -> 4.0F;
            default -> 1.0F;
        };
    }

    private void setAttackKind(LegacyShipAttackKind attackKind) {
        this.entityData.set(DATA_ATTACK_KIND, attackKind.ordinal());
    }

    private void setProjectileVisual(LegacyShipProjectileVisual visual) {
        this.entityData.set(DATA_PROJECTILE_VISUAL, visual.ordinal());
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

    private void setEffectFlags(int flags) {
        this.entityData.set(DATA_EFFECT_FLAGS, flags);
    }

    private void setOwnerId(int ownerId) {
        this.entityData.set(DATA_OWNER_ID, ownerId);
        this.refreshDimensions();
    }

    private void setTargetId(int targetId) {
        this.entityData.set(DATA_TARGET_ID, targetId);
    }

    private void setReturning(boolean returning) {
        this.entityData.set(DATA_RETURNING, returning);
    }

    private @Nullable LegacyShipEntity getOwnerShip() {
        Entity entity = this.level().getEntity(this.getOwnerId());
        return entity instanceof LegacyShipEntity ship ? ship : null;
    }

    private @Nullable LivingEntity getCurrentTarget() {
        Entity entity = this.level().getEntity(this.getTargetId());
        return entity instanceof LivingEntity living ? living : null;
    }

    private @Nullable LivingEntity resolveAttackTarget(LegacyShipEntity owner) {
        LivingEntity target = this.getCurrentTarget();
        if (target != null && target.isAlive() && owner.canEngage(target)) {
            return target;
        }

        if (this.tickCount < this.initialStraightTicks) {
            return null;
        }

        if (owner.getTarget() != null && owner.getTarget().isAlive() && owner.canEngage(owner.getTarget())) {
            this.setTargetId(owner.getTarget().getId());
            return owner.getTarget();
        }

        if (this.tickCount % 16 != 0) {
            return null;
        }

        List<LivingEntity> nearbyTargets = this.level().getEntitiesOfClass(LivingEntity.class,
                this.getBoundingBox().inflate(this.getTargetingRange(), this.getTargetingRange() * 0.5D, this.getTargetingRange()),
                candidate -> candidate != null && candidate.isAlive() && candidate != owner && owner.canEngage(candidate));
        nearbyTargets.sort(Comparator.comparingDouble(this::distanceToSqr));
        if (nearbyTargets.isEmpty()) {
            return null;
        }

        LivingEntity newTarget = nearbyTargets.get(0);
        this.setTargetId(newTarget.getId());
        return newTarget;
    }

    private boolean canStrikeTarget(LegacyShipEntity owner, LivingEntity target) {
        if (this.attackCooldown > 0 || this.tickCount < this.initialStraightTicks || this.isReturning()) {
            return false;
        }
        if (!target.isAlive() || !owner.canEngage(target) || !this.hasLineOfSight(target)) {
            return false;
        }
        double strikeRange = this.getStrikeRange();
        return this.distanceToSqr(target) <= strikeRange * strikeRange;
    }

    private void updateMotion(LegacyShipEntity owner, @Nullable LivingEntity target) {
        if (this.tickCount < this.initialStraightTicks && !this.isReturning()) {
            if (target != null) {
                Vec3 initialMovement = legacyInitialStraightMovement(
                        this.position(), target, this.getDeltaMovement());
                this.setDeltaMovement(initialMovement);
                this.updateRotation(initialMovement);
            }
            return;
        }

        Vec3 desiredPos;
        if (this.isReturning()) {
            desiredPos = owner.position().add(0.0D, owner.getBbHeight() + 1.0D, 0.0D);
        } else if (target != null) {
            desiredPos = target.position().add(0.0D, target.getBbHeight() * this.targetHeightFactor, 0.0D);
        } else {
            desiredPos = owner.position().add(owner.getLookAngle().scale(6.0D)).add(0.0D, owner.getBbHeight() + 1.0D, 0.0D);
        }

        Vec3 desired = this.resolveObstacleWaypoint(desiredPos).subtract(this.position());
        if (desired.lengthSqr() < 1.0E-6D) {
            return;
        }

        Vec3 current = this.getDeltaMovement();
        if (current.lengthSqr() < 1.0E-6D) {
            current = desired.normalize().scale(this.baseSpeed());
        }

        Vec3 blended = current.scale(1.0D - this.turnBlend)
                .add(desired.normalize().scale(this.baseSpeed() * this.turnBlend));

        if (blended.lengthSqr() < 1.0E-6D) {
            blended = desired.normalize().scale(this.baseSpeed());
        } else {
            blended = blended.normalize().scale(this.baseSpeed());
        }

        this.setDeltaMovement(blended);
        this.updateRotation(blended);
    }

    private Vec3 resolveObstacleWaypoint(Vec3 destination) {
        if (this.level().clip(new ClipContext(this.position(), destination, ClipContext.Block.COLLIDER,
                ClipContext.Fluid.NONE, this)).getType() == HitResult.Type.MISS) {
            this.obstaclePath = null;
            this.obstacleTarget = null;
            return destination;
        }
        BlockPos target = BlockPos.containing(destination);
        if (this.obstaclePath == null || !target.equals(this.obstacleTarget) || this.tickCount >= this.nextObstaclePathTick) {
            this.obstaclePath = new ShipPathFinder(this.level(), true).findPath(
                    this, target.getX(), target.getY(), target.getZ(), 32.0F);
            this.obstacleTarget = target;
            this.nextObstaclePathTick = this.tickCount + 16;
        }
        if (this.obstaclePath != null) {
            while (!this.obstaclePath.isFinished()
                    && this.position().distanceToSqr(this.obstaclePath.getPosition(this)) < 0.25D) {
                this.obstaclePath.incrementPathIndex();
            }
            if (!this.obstaclePath.isFinished()) return this.obstaclePath.getPosition(this);
        }
        return destination;
    }

    private static Vec3 legacyInitialStraightMovement(Vec3 origin, LivingEntity target, Vec3 fallback) {
        double distX = target.getX() - origin.x;
        double distZ = target.getZ() - origin.z;
        double horizontalDistance = Math.sqrt(distX * distX + distZ * distZ);
        if (horizontalDistance < 1.0E-6D) {
            distX = fallback.x;
            distZ = fallback.z;
            horizontalDistance = Math.sqrt(distX * distX + distZ * distZ);
        }
        if (horizontalDistance < 1.0E-6D) {
            distX = 0.0D;
            distZ = 1.0D;
            horizontalDistance = 1.0D;
        }

        return new Vec3(
                distX / horizontalDistance * LEGACY_INITIAL_HORIZONTAL_SPEED,
                LEGACY_INITIAL_VERTICAL_SPEED,
                distZ / horizontalDistance * LEGACY_INITIAL_HORIZONTAL_SPEED);
    }

    private void updateRotation(Vec3 movement) {
        float yaw = (float) (Mth.atan2(movement.x, movement.z) * (180.0D / Math.PI));
        float pitch = (float) (-Mth.atan2(movement.y, Math.sqrt(movement.x * movement.x + movement.z * movement.z)) * (180.0D / Math.PI));
        this.setYRot(yaw);
        this.setXRot(pitch);
        this.yRotO = yaw;
        this.xRotO = pitch;
    }

    private void checkReturnCompletion(LegacyShipEntity owner) {
        if (!this.isReturning()) {
            return;
        }

        double returnRange = owner.getBbHeight() + 2.0D;
        if (this.distanceToSqr(owner) <= returnRange * returnRange) {
            this.returnAircraftToOwner(owner);
            this.discard();
        }
    }

    private void returnAircraftToOwner(LegacyShipEntity owner) {
        if (this.returnedToOwner) {
            return;
        }
        this.returnedToOwner = true;
        owner.recoverAircraft(this.getAttackKind(), this.remainingAttacks);
    }

    private boolean hasLineOfSight(LivingEntity target) {
        Vec3 start = this.position().add(0.0D, this.getBbHeight() * 0.5D, 0.0D);
        Vec3 end = target.position().add(0.0D, target.getEyeHeight() * 0.9D, 0.0D);
        BlockHitResult hitResult = this.level().clip(new ClipContext(start, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this));
        return hitResult.getType() == HitResult.Type.MISS || hitResult.getBlockPos().closerThan(target.blockPosition(), 1.5D);
    }

    private void spawnTrailParticles() {
        GameplayParticleType particleType = this.getTrailParticleType();
        LegacyShipEntity owner = this.getOwnerShip();
        if (particleType == GameplayParticleType.NONE || this.tickCount < 2
                || !LegacyShipBehaviorCatalog.aircraftTrailParticleEnabledFor(
                owner == null ? null : owner.getSpec(), this.getAttackKind(), this.tickCount)) {
            return;
        }

        Vec3 motion = this.getDeltaMovement();
        double x = this.getX() - motion.x * 0.25D;
        double y = this.getY() - motion.y * 0.25D;
        double z = this.getZ() - motion.z * 0.25D;

        switch (particleType) {
            case AIRCRAFT_TRAIL -> this.level().addParticle(ParticleTypes.CLOUD, x, y, z, -motion.x * 0.02D, 0.0D, -motion.z * 0.02D);
            case BOMB_DROP -> this.level().addParticle(ParticleTypes.SMOKE, x, y, z, 0.0D, -0.01D, 0.0D);
            case TORPEDO_WAKE -> {
                this.level().addParticle(ParticleTypes.BUBBLE, x, y, z, -motion.x * 0.03D, 0.01D, -motion.z * 0.03D);
                this.level().addParticle(ParticleTypes.SPLASH, x, y, z, 0.0D, 0.02D, 0.0D);
            }
            default -> this.level().addParticle(ParticleTypes.CLOUD, x, y, z, 0.0D, 0.0D, 0.0D);
        }

        if (this.hasFlarePayload() && this.tickCount % 3 == 0) {
            this.level().addParticle(ParticleTypes.END_ROD, x, y, z, 0.0D, 0.01D, 0.0D);
        }
        if (this.hasSearchlightPayload() && this.tickCount % 4 == 0) {
            this.level().addParticle(ParticleTypes.ELECTRIC_SPARK, x, y, z, 0.0D, 0.0D, 0.0D);
        }
    }

    protected double baseSpeed() {
        LegacyShipEntity owner = this.getOwnerShip();
        if (owner != null) {
            double ownerMov = owner.getLegacyStats().moveSpeed();
            double legacySpeed = this.isHeavyAirframe()
                    ? (ownerMov * 0.1D + 0.23D)
                    : (ownerMov * 0.2D + 0.3D);
            return Math.max(0.1D, legacySpeed * this.speedScale);
        }
        return Math.max(0.24D, (this.isHeavyAirframe() ? 0.4D : 0.44D) * this.speedScale);
    }

    public void playLaunchFx() {
        if (!(this.level() instanceof ServerLevel serverLevel)) {
            return;
        }
        CombatFxDispatcher.sendParticle(this, this.getLaunchParticleType(),
                this.getX(), this.getY(), this.getZ(),
                this.getDeltaMovement().x, this.getDeltaMovement().y, this.getDeltaMovement().z);
    }

    public net.minecraft.resources.ResourceLocation textureLocation() {
        return this.getTextureLocation();
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

        LegacyShipEntity owner = this.getOwnerShip();
        if (owner == null) {
            this.discard();
            return false;
        }

        if (damageSource.is(DamageTypes.IN_WALL)
                || damageSource.is(DamageTypes.STARVE)
                || damageSource.is(DamageTypes.CACTUS)
                || damageSource.is(DamageTypes.FALL)) {
            return false;
        }
        if (damageSource.is(DamageTypes.FELL_OUT_OF_WORLD)) {
            this.returnAircraftToOwner(owner);
            this.discard();
            return false;
        }

        Entity attacker = damageSource.getEntity();
        if (!owner.isHostileVariant() && attacker instanceof Player attackerPlayer && owner.isOwnedBy(attackerPlayer)) {
            return this.applyAircraftDamage(amount);
        }
        if (this.isInvulnerableTo(damageSource) || attacker == null || attacker.equals(this)) {
            return false;
        }

        if (!owner.isHostileVariant()) {
            if (attacker instanceof Player && !ShinColleConfig.friendlyFire()) {
                return false;
            }
            if (owner.isAlliedTo(attacker)
                    && !(attacker instanceof Player player && owner.isOwnedBy(player))) {
                return false;
            }
        }

        // EntityAirplane has a 33% chance to dodge heavy hits (> 50% max HP).
        if (!this.isHeavyAirframe() && amount > this.maxHp * 0.5F && this.random.nextInt(3) == 0) {
            return false;
        }

        float dodgeChance = owner.getLegacyStats().dodge() + (this.isHeavyAirframe() ? 0.2F : 0.3F);
        if (this.random.nextFloat() <= dodgeChance) {
            return false;
        }

        float reducedAtk = amount;
        if (!damageSource.is(DamageTypeTags.BYPASSES_ARMOR)
                && !damageSource.is(DamageTypes.MAGIC)
                && !damageSource.is(DamageTypes.WITHER)
                && !damageSource.is(DamageTypes.DRAGON_BREATH)) {
            float def = owner.getLegacyStats().defense() * 0.5F;
            float factor = 1.0F - def + (this.random.nextFloat() * 0.5F - 0.25F);
            reducedAtk = amount * Math.max(0.0F, factor);
        }
        if (reducedAtk < 1.0F && reducedAtk > 0.0F) {
            reducedAtk = 1.0F;
        } else if (reducedAtk <= 0.0F) {
            reducedAtk = 0.0F;
        }

        return this.applyAircraftDamage(reducedAtk);
    }

    private boolean applyAircraftDamage(float amount) {
        if (amount <= 0.0F || !this.isAlive()) {
            return false;
        }
        this.hp -= amount;
        if (this.hp <= 0.0F) {
            this.hp = 0.0F;
            this.discard();
        }
        return true;
    }

    public float getAircraftHealth() {
        return this.hp;
    }

    public float getAircraftMaxHealth() {
        return this.maxHp;
    }

    @Override
    public boolean killedEntity(ServerLevel level, LivingEntity entity) {
        LegacyShipEntity owner = this.getOwnerShip();
        if (owner != null) {
            owner.incrementShipKills();
        }
        return super.killedEntity(level, entity);
    }

    public float renderScale() {
        return this.getRenderScale();
    }
}
