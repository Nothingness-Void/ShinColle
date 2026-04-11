package com.lulan.shincolle.entity.ship;

import com.lulan.shincolle.entity.projectile.LegacyShipProjectileProfile;
import com.lulan.shincolle.entity.projectile.LegacyShipProjectileVisual;
import com.lulan.shincolle.network.CombatFxDispatcher;
import com.lulan.shincolle.network.GameplayParticleType;
import com.lulan.shincolle.registry.ModEntityTypes;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MoverType;
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
    private static final int DEFAULT_MAX_LIFE_TICKS = 20 * 40;
    private static final int DEFAULT_INITIAL_STRAIGHT_TICKS = 18;

    private int maxLifeTicks = DEFAULT_MAX_LIFE_TICKS;
    private int attackCooldown;
    private int remainingAttacks = 1;
    private int initialStraightTicks = DEFAULT_INITIAL_STRAIGHT_TICKS;
    private float speedScale = 1.0F;
    private float turnBlend = 0.16F;
    private float targetHeightFactor = 0.55F;

    protected LegacyShipAircraftEntity(EntityType<? extends LegacyShipAircraftEntity> entityType, Level level) {
        super(entityType, level);
        this.noPhysics = true;
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
        aircraft.maxLifeTicks = Math.max(DEFAULT_MAX_LIFE_TICKS, profile.lifeTicks() * 8);
        aircraft.initialStraightTicks = attackKind == LegacyShipAttackKind.AIR_HEAVY ? 22 : DEFAULT_INITIAL_STRAIGHT_TICKS;
        aircraft.remainingAttacks = attackKind == LegacyShipAttackKind.AIR_HEAVY ? 1 : 2;
        aircraft.attackCooldown = attackKind == LegacyShipAttackKind.AIR_HEAVY ? 14 : 10;
        aircraft.speedScale = (float) owner.getEquipmentBehaviorState().projectileSpeedMultiplier(attackKind);
        aircraft.turnBlend = attackKind == LegacyShipAttackKind.AIR_HEAVY ? 0.12F : 0.18F;

        int effectFlags = 0;
        if (owner.getEquipmentBehaviorState().flareLevel() > 0) {
            effectFlags |= EFFECT_FLAG_FLARE;
        }
        if (owner.getEquipmentBehaviorState().searchlightLevel() > 0) {
            effectFlags |= EFFECT_FLAG_SEARCHLIGHT;
        }
        aircraft.setEffectFlags(effectFlags);

        double launchY = owner.getY() + owner.getBbHeight() * profile.launchHeightFactor();
        aircraft.moveTo(owner.getX(), launchY, owner.getZ(), owner.getYRot(), owner.getXRot());

        Vec3 launchTarget = target.position().add(0.0D, target.getBbHeight() * profile.targetHeightFactor(), 0.0D);
        Vec3 launchVector = launchTarget.subtract(aircraft.position());
        if (launchVector.lengthSqr() < 1.0E-4D) {
            launchVector = owner.getLookAngle();
        }
        launchVector = launchVector.normalize().scale(aircraft.baseSpeed()).add(0.0D, attackKind == LegacyShipAttackKind.AIR_HEAVY ? 0.14D : 0.08D, 0.0D);
        aircraft.setDeltaMovement(launchVector);
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
        this.maxLifeTicks = tag.contains(MAX_LIFE_TICKS_TAG) ? Math.max(40, tag.getInt(MAX_LIFE_TICKS_TAG)) : DEFAULT_MAX_LIFE_TICKS;
        this.attackCooldown = Math.max(0, tag.getInt(ATTACK_COOLDOWN_TAG));
        this.remainingAttacks = Math.max(0, tag.getInt(REMAINING_ATTACKS_TAG));
        this.initialStraightTicks = Math.max(0, tag.getInt(INITIAL_STRAIGHT_TICKS_TAG));
        this.speedScale = tag.contains(SPEED_SCALE_TAG) ? tag.getFloat(SPEED_SCALE_TAG) : 1.0F;
        this.turnBlend = tag.contains(TURN_BLEND_TAG) ? tag.getFloat(TURN_BLEND_TAG) : 0.16F;
        this.targetHeightFactor = tag.contains(TARGET_HEIGHT_FACTOR_TAG) ? tag.getFloat(TARGET_HEIGHT_FACTOR_TAG) : 0.55F;
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
    }

    @Override
    public void tick() {
        super.tick();

        if (this.tickCount > this.maxLifeTicks) {
            this.discard();
            return;
        }

        if (this.level().isClientSide()) {
            this.spawnTrailParticles();
            this.setPos(this.position().add(this.getDeltaMovement()));
            return;
        }

        LegacyShipEntity owner = this.getOwnerShip();
        if (owner == null || !owner.isAlive()) {
            this.discard();
            return;
        }

        LivingEntity target = this.resolveAttackTarget(owner);
        if (target == null && !this.isReturning()) {
            this.setReturning(true);
        }

        if (this.attackCooldown > 0) {
            this.attackCooldown--;
        }

        if (!this.isReturning() && target != null && this.canStrikeTarget(owner, target)) {
            boolean struck = owner.performCompatAircraftStrike(target, this.getAttackKind(), this.getProjectileVisual(),
                    this.getHitParticleType(), this.hasFlarePayload(), this.hasSearchlightPayload());
            if (struck) {
                this.remainingAttacks--;
                if (this.remainingAttacks <= 0 || this.getAttackKind() == LegacyShipAttackKind.AIR_HEAVY) {
                    this.setReturning(true);
                }
                this.attackCooldown = this.getAttackKind() == LegacyShipAttackKind.AIR_HEAVY ? 18 : 24;
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
        return this.isHeavyAirframe() ? 28.0D : 20.0D;
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

        if (owner.getTarget() != null && owner.getTarget().isAlive() && owner.canEngage(owner.getTarget())
                && this.hasLineOfSight(owner.getTarget())) {
            this.setTargetId(owner.getTarget().getId());
            return owner.getTarget();
        }

        if (this.tickCount % 8 != 0) {
            return null;
        }

        List<LivingEntity> nearbyTargets = this.level().getEntitiesOfClass(LivingEntity.class,
                this.getBoundingBox().inflate(this.getTargetingRange(), this.getTargetingRange() * 0.5D, this.getTargetingRange()),
                candidate -> candidate != null && candidate.isAlive() && candidate != owner && owner.canEngage(candidate) && this.hasLineOfSight(candidate));
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
        Vec3 desiredPos;
        if (this.isReturning()) {
            desiredPos = owner.position().add(0.0D, owner.getBbHeight() + 1.25D, 0.0D);
        } else if (target != null) {
            desiredPos = target.position().add(0.0D, target.getBbHeight() * this.targetHeightFactor, 0.0D);
        } else {
            desiredPos = owner.position().add(owner.getLookAngle().scale(6.0D)).add(0.0D, owner.getBbHeight() + 1.0D, 0.0D);
        }

        Vec3 desired = desiredPos.subtract(this.position());
        if (desired.lengthSqr() < 1.0E-6D) {
            return;
        }

        Vec3 current = this.getDeltaMovement();
        if (current.lengthSqr() < 1.0E-6D) {
            current = desired.normalize().scale(this.baseSpeed());
        }

        Vec3 blended;
        if (this.tickCount < this.initialStraightTicks && !this.isReturning()) {
            blended = current.add(0.0D, this.isHeavyAirframe() ? 0.015D : 0.01D, 0.0D);
        } else {
            blended = current.scale(1.0D - this.turnBlend).add(desired.normalize().scale(this.baseSpeed() * this.turnBlend));
        }

        if (blended.lengthSqr() < 1.0E-6D) {
            blended = desired.normalize().scale(this.baseSpeed());
        } else {
            blended = blended.normalize().scale(this.baseSpeed());
        }

        this.setDeltaMovement(blended);
        float yaw = (float) (Mth.atan2(blended.x, blended.z) * (180.0D / Math.PI));
        float pitch = (float) (-Mth.atan2(blended.y, Math.sqrt(blended.x * blended.x + blended.z * blended.z)) * (180.0D / Math.PI));
        this.setYRot(yaw);
        this.setXRot(pitch);
        this.yRotO = yaw;
        this.xRotO = pitch;
    }

    private void checkReturnCompletion(LegacyShipEntity owner) {
        if (!this.isReturning()) {
            return;
        }

        if (this.distanceToSqr(owner) <= (owner.getBbWidth() + 1.8F) * (owner.getBbWidth() + 1.8F)) {
            this.discard();
        }
    }

    private boolean hasLineOfSight(LivingEntity target) {
        Vec3 start = this.position().add(0.0D, this.getBbHeight() * 0.5D, 0.0D);
        Vec3 end = target.position().add(0.0D, target.getEyeHeight() * 0.9D, 0.0D);
        BlockHitResult hitResult = this.level().clip(new ClipContext(start, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this));
        return hitResult.getType() == HitResult.Type.MISS || hitResult.getBlockPos().closerThan(target.blockPosition(), 1.5D);
    }

    private void spawnTrailParticles() {
        GameplayParticleType particleType = this.getTrailParticleType();
        if (particleType == GameplayParticleType.NONE || this.tickCount < 2) {
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

    public float renderScale() {
        return this.getRenderScale();
    }
}
