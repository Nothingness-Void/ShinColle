package com.lulan.shincolle.entity.projectile;

import com.lulan.shincolle.entity.ship.LegacyShipAttackKind;
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
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ItemSupplier;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
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

    private static final int EFFECT_FLAG_FLARE = 1;
    private static final int EFFECT_FLAG_SEARCHLIGHT = 1 << 1;

    private float damage;
    private int targetId = -1;
    private int lifeTicks = 80;
    private boolean missedShot;
    private boolean resolvedImpact;
    private float speedScale = 1.0F;
    private float guidanceBlend = 0.18F;
    private float travelSpeed = 0.52F;
    private float targetHeightFactor = 0.35F;

    public LegacyShipProjectileEntity(EntityType<? extends LegacyShipProjectileEntity> entityType, Level level) {
        super(entityType, level);
    }

    public LegacyShipProjectileEntity(Level level, LegacyShipEntity owner) {
        super(ModEntityTypes.LEGACY_SHIP_PROJECTILE.get(), owner, level);
    }

    public static LegacyShipProjectileEntity create(Level level, LegacyShipEntity owner, LivingEntity target,
                                                    LegacyShipAttackKind attackKind, float damage, boolean missedShot) {
        LegacyShipProjectileEntity projectile = new LegacyShipProjectileEntity(level, owner);
        LegacyShipProjectileProfile profile = owner.getAttackProfile().projectileProfile(attackKind);
        if (!profile.isPresent()) {
            profile = fallbackProfile(attackKind);
        }

        projectile.setAttackKind(attackKind);
        projectile.setProjectileVisual(profile.visual());
        projectile.setMoveType(profile.moveType());
        projectile.setLaunchParticleType(profile.launchParticleType());
        projectile.setTrailParticleType(profile.trailParticleType());
        projectile.setHitParticleType(profile.hitParticleType());
        projectile.damage = damage;
        projectile.missedShot = missedShot;
        projectile.targetId = target.getId();
        projectile.targetHeightFactor = profile.targetHeightFactor();
        projectile.lifeTicks = profile.lifeTicks();
        projectile.travelSpeed = (float) profile.baseSpeed();

        ShipEquipmentBehaviorState behaviorState = owner.getEquipmentBehaviorState();
        projectile.speedScale = (float) behaviorState.projectileSpeedMultiplier(attackKind);
        projectile.guidanceBlend = Math.max(profile.guidanceBlend(),
                (float) (attackKind == LegacyShipAttackKind.AIR_LIGHT || attackKind == LegacyShipAttackKind.AIR_HEAVY
                        ? behaviorState.airGuidanceBlend()
                        : 0.0D));
        int effectFlags = 0;
        if (behaviorState.flareLevel() > 0) {
            effectFlags |= EFFECT_FLAG_FLARE;
        }
        if (behaviorState.searchlightLevel() > 0) {
            effectFlags |= EFFECT_FLAG_SEARCHLIGHT;
        }
        projectile.setEffectFlags(effectFlags);

        double launchY = owner.getY() + owner.getBbHeight() * profile.launchHeightFactor();
        projectile.moveTo(owner.getX(), launchY, owner.getZ(), owner.getYRot(), owner.getXRot());

        Vec3 targetPos = projectile.resolveTargetPos(target, profile, missedShot);
        Vec3 launchVector = targetPos.subtract(projectile.position());
        if (profile.moveType() == LegacyShipProjectileMoveType.ARC || profile.moveType() == LegacyShipProjectileMoveType.DROP) {
            launchVector = launchVector.add(0.0D, profile.launchArcLift(), 0.0D);
        }

        projectile.shoot(launchVector.x, launchVector.y, launchVector.z, projectile.baseSpeed(), 0.0F);
        return projectile;
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_ATTACK_KIND, LegacyShipAttackKind.HEAVY.ordinal());
        this.entityData.define(DATA_PROJECTILE_VISUAL, LegacyShipProjectileVisual.NONE.ordinal());
        this.entityData.define(DATA_MOVE_TYPE, LegacyShipProjectileMoveType.NONE.ordinal());
        this.entityData.define(DATA_LAUNCH_PARTICLE, GameplayParticleType.LAUNCH_SMOKE.ordinal());
        this.entityData.define(DATA_TRAIL_PARTICLE, GameplayParticleType.NONE.ordinal());
        this.entityData.define(DATA_HIT_PARTICLE, GameplayParticleType.HIT_EXPLOSION.ordinal());
        this.entityData.define(DATA_EFFECT_FLAGS, 0);
    }

    @Override
    public void tick() {
        super.tick();

        if (this.tickCount > this.lifeTicks) {
            if (!this.level().isClientSide()) {
                this.emitMissFx(this.position());
            }
            this.discard();
            return;
        }

        if (this.level().isClientSide()) {
            this.spawnTrailParticles();
            return;
        }

        this.updateFlightPath();
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        Entity hitEntity = result.getEntity();
        Entity owner = this.getOwner();

        if (this.level().isClientSide() || !(owner instanceof LegacyShipEntity ship) || !(hitEntity instanceof LivingEntity livingTarget)) {
            this.discard();
            return;
        }

        this.resolvedImpact = true;

        if (!ship.canEngage(livingTarget)) {
            this.discard();
            return;
        }

        boolean attacked = livingTarget.hurt(this.damageSources().thrown(this, ship), this.damage);
        Vec3 motion = this.getDeltaMovement();
        CombatFxDispatcher.sendParticle(livingTarget, this.getHitParticleType(),
                result.getLocation().x, result.getLocation().y, result.getLocation().z,
                motion.x, motion.y, motion.z);

        if (attacked) {
            ship.setLastHurtMob(livingTarget);
            CombatFxDispatcher.sendCombatReact(ship, livingTarget, CombatReactType.HIT,
                    this.getAttackKind(), this.getProjectileVisual());
            if (this.getAttackKind() == LegacyShipAttackKind.HEAVY || this.getAttackKind() == LegacyShipAttackKind.AIR_HEAVY) {
                double knockbackStrength = 0.4D;
                livingTarget.knockback(knockbackStrength, ship.getX() - livingTarget.getX(), ship.getZ() - livingTarget.getZ());
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
        super.onHit(result);

        if (result.getType() != HitResult.Type.ENTITY) {
            if (!this.level().isClientSide()) {
                this.emitMissFx(result.getLocation());
            }
            this.discard();
        }
    }

    @Override
    protected boolean canHitEntity(Entity entity) {
        if (!super.canHitEntity(entity) || entity == this.getOwner() || !(entity instanceof LivingEntity)) {
            return false;
        }

        if (this.missedShot) {
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
    protected float getGravity() {
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
        tag.putInt("LifeTicks", this.lifeTicks);
        tag.putBoolean("MissedShot", this.missedShot);
        tag.putFloat("SpeedScale", this.speedScale);
        tag.putFloat("GuidanceBlend", this.guidanceBlend);
        tag.putFloat("TravelSpeed", this.travelSpeed);
        tag.putFloat("TargetHeightFactor", this.targetHeightFactor);
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
        this.lifeTicks = tag.getInt("LifeTicks");
        this.missedShot = tag.getBoolean("MissedShot");
        this.speedScale = tag.contains("SpeedScale") ? tag.getFloat("SpeedScale") : 1.0F;
        this.guidanceBlend = tag.contains("GuidanceBlend") ? tag.getFloat("GuidanceBlend") : 0.18F;
        this.travelSpeed = tag.contains("TravelSpeed") ? tag.getFloat("TravelSpeed") : 0.52F;
        this.targetHeightFactor = tag.contains("TargetHeightFactor") ? tag.getFloat("TargetHeightFactor") : 0.35F;
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

    private void updateFlightPath() {
        switch (this.getMoveType()) {
            case GUIDED -> this.updateGuidedFlight();
            case DROP -> this.updateBombDropFlight();
            default -> {
            }
        }
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
        if (intendedTarget != null && intendedTarget.isAlive()) {
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

        if (missedShot) {
            double horizontalSpread = switch (profile.visual()) {
                case BOMB -> 7.0D;
                case AIRPLANE, TORPEDO -> 5.5D;
                default -> 5.0D;
            };
            targetX += this.random.nextDouble() * horizontalSpread * 2.0D - horizontalSpread;
            targetY += this.random.nextDouble() * 5.0D;
            targetZ += this.random.nextDouble() * horizontalSpread * 2.0D - horizontalSpread;
        }

        if (profile.moveType() == LegacyShipProjectileMoveType.ARC) {
            double distance = this.position().distanceTo(new Vec3(targetX, targetY, targetZ));
            if (distance < 6.0D) {
                Vec3 stretch = new Vec3(targetX - this.getX(), targetY - this.getY(), targetZ - this.getZ()).normalize().scale(6.0D - distance);
                targetX += stretch.x;
                targetY += stretch.y;
                targetZ += stretch.z;
            }
        }

        return new Vec3(targetX, targetY, targetZ);
    }

    private float baseSpeed() {
        return this.travelSpeed * this.speedScale;
    }

    private static LegacyShipProjectileProfile fallbackProfile(LegacyShipAttackKind attackKind) {
        return switch (attackKind) {
            case HEAVY -> new LegacyShipProjectileProfile(
                    LegacyShipProjectileVisual.MISSILE,
                    LegacyShipProjectileMoveType.ARC,
                    0.52D,
                    0.72F,
                    0.35F,
                    90,
                    0.0F,
                    0.18F,
                    GameplayParticleType.LAUNCH_SMOKE,
                    GameplayParticleType.MISSILE_TRAIL,
                    GameplayParticleType.MISSILE_IMPACT);
            case AIR_LIGHT -> new LegacyShipProjectileProfile(
                    LegacyShipProjectileVisual.AIRPLANE,
                    LegacyShipProjectileMoveType.GUIDED,
                    0.42D,
                    0.95F,
                    0.55F,
                    110,
                    0.22F,
                    0.08F,
                    GameplayParticleType.AIRCRAFT_LAUNCH,
                    GameplayParticleType.AIRCRAFT_TRAIL,
                    GameplayParticleType.AIRCRAFT_IMPACT);
            case AIR_HEAVY -> new LegacyShipProjectileProfile(
                    LegacyShipProjectileVisual.BOMB,
                    LegacyShipProjectileMoveType.DROP,
                    0.36D,
                    0.98F,
                    0.72F,
                    120,
                    0.08F,
                    0.18F,
                    GameplayParticleType.AIRCRAFT_LAUNCH,
                    GameplayParticleType.BOMB_DROP,
                    GameplayParticleType.BOMB_IMPACT);
            default -> LegacyShipProjectileProfile.NONE;
        };
    }

    private static LegacyShipAttackKind attackKindByOrdinal(int ordinal) {
        LegacyShipAttackKind[] values = LegacyShipAttackKind.values();
        return values[Mth.clamp(ordinal, 0, values.length - 1)];
    }
}
