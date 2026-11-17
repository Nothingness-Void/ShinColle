package com.lulan.shincolle.entity.ship;

import com.lulan.shincolle.network.CombatFxDispatcher;
import com.lulan.shincolle.network.CombatReactType;
import com.lulan.shincolle.network.GameplayParticleType;
import com.lulan.shincolle.registry.ModEntityTypes;
import com.lulan.shincolle.config.ShinColleConfig;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;

/**
 * Modern counterpart of EntityRensouhou and EntityRensouhouMob.
 *
 * The old summon is a transient child of its ship.  The host and target IDs
 * intentionally remain the only links persisted, matching the old behavior
 * where a summon is retired when its host is no longer available.
 */
public class LegacyShipRensouhouEntity extends PathfinderMob {

    private static final String HOST_ID_TAG = "HostId";
    private static final String TARGET_ID_TAG = "TargetId";
    private static final String INTERNAL_AMMO_TAG = "InternalAmmo";
    private static final String LIFE_TICKS_TAG = "LifeTicks";
    private static final String ATTACK_COOLDOWN_TAG = "AttackCooldown";
    private static final String LARGE_MODEL_TAG = "LargeModel";
    private static final String HOSTILE_SCALE_LEVEL_TAG = "HostileScaleLevel";

    private static final int DEFAULT_INTERNAL_AMMO = 6;
    private static final int DEFAULT_LIFE_TICKS = 1200;

    private static final EntityDataAccessor<Integer> DATA_HOST_ID =
            SynchedEntityData.defineId(LegacyShipRensouhouEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_TARGET_ID =
            SynchedEntityData.defineId(LegacyShipRensouhouEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_INTERNAL_AMMO =
            SynchedEntityData.defineId(LegacyShipRensouhouEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> DATA_LARGE_MODEL =
            SynchedEntityData.defineId(LegacyShipRensouhouEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> DATA_HOSTILE_SCALE_LEVEL =
            SynchedEntityData.defineId(LegacyShipRensouhouEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> DATA_HOSTILE =
            SynchedEntityData.defineId(LegacyShipRensouhouEntity.class, EntityDataSerializers.BOOLEAN);

    private int lifeTicks = DEFAULT_LIFE_TICKS;
    private int attackCooldown;
    private boolean returnedResource;

    public LegacyShipRensouhouEntity(EntityType<? extends LegacyShipRensouhouEntity> entityType, Level level) {
        super(entityType, level);
        this.setNoGravity(true);
        this.setPersistenceRequired();
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 20.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.4D)
                .add(Attributes.ATTACK_DAMAGE, 2.0D)
                .add(Attributes.FOLLOW_RANGE, 64.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.5D);
    }

    public static @Nullable LegacyShipRensouhouEntity create(Level level, LegacyShipEntity host,
                                                              LivingEntity target) {
        LegacyShipRensouhouEntity summon = ModEntityTypes.LEGACY_RENSOUHOU.get().create(level);
        if (summon == null) {
            return null;
        }

        summon.setHostId(host.getId());
        summon.setTargetId(target.getId());
        summon.setHostileVariant(host.isHostileVariant());
        summon.setLargeModel(LegacyShipBehaviorCatalog.rensouhouUsesLargeModel(host));
        summon.setHostileScaleLevel(host.getLegacyHostileScaleLevel());
        summon.setInternalAmmo(DEFAULT_INTERNAL_AMMO);

        double x = host.getX() + (host.isHostileVariant()
                ? host.getRandom().nextDouble() * 6.0D - 3.0D
                : host.getRandom().nextDouble() * 3.0D - 1.5D);
        double y = host.getY() + (host.isHostileVariant() ? 0.5D : 0.0D);
        double z = host.getZ() + (host.isHostileVariant()
                ? host.getRandom().nextDouble() * 6.0D - 3.0D
                : host.getRandom().nextDouble() * 3.0D - 1.5D);
        summon.moveTo(x, y, z, host.getYRot(), host.getXRot());
        if (!level.noCollision(summon, summon.getBoundingBox())) {
            summon.moveTo(host.getX(), host.getY(), host.getZ(), host.getYRot(), host.getXRot());
        }

        summon.applyHostAttributes(host);
        return summon;
    }

    private void applyHostAttributes(LegacyShipEntity host) {
        double maxHealth = host.isHostileVariant()
                ? host.getMaxHealth() * 0.15D
                : host.getShipLevel() + host.getMaxHealth() * 0.2D;
        this.getAttribute(Attributes.MAX_HEALTH).setBaseValue(Math.max(1.0D, maxHealth));
        this.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(
                host.getLegacyStats().moveSpeed() * 0.2D + 0.4D);
        this.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(host.getLegacyStats().attackLight());
        this.getAttribute(Attributes.FOLLOW_RANGE).setBaseValue(64.0D);
        this.getAttribute(Attributes.KNOCKBACK_RESISTANCE).setBaseValue(0.5D);
        this.setHealth(this.getMaxHealth());
        this.refreshDimensions();
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_HOST_ID, -1);
        this.entityData.define(DATA_TARGET_ID, -1);
        this.entityData.define(DATA_INTERNAL_AMMO, DEFAULT_INTERNAL_AMMO);
        this.entityData.define(DATA_LARGE_MODEL, false);
        this.entityData.define(DATA_HOSTILE_SCALE_LEVEL, 0);
        this.entityData.define(DATA_HOSTILE, false);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.setHostId(tag.getInt(HOST_ID_TAG));
        this.setTargetId(tag.getInt(TARGET_ID_TAG));
        this.setInternalAmmo(tag.contains(INTERNAL_AMMO_TAG)
                ? tag.getInt(INTERNAL_AMMO_TAG) : DEFAULT_INTERNAL_AMMO);
        this.lifeTicks = Math.max(40, tag.contains(LIFE_TICKS_TAG)
                ? tag.getInt(LIFE_TICKS_TAG) : DEFAULT_LIFE_TICKS);
        this.attackCooldown = Math.max(0, tag.getInt(ATTACK_COOLDOWN_TAG));
        this.setLargeModel(tag.getBoolean(LARGE_MODEL_TAG));
        this.setHostileScaleLevel(tag.getInt(HOSTILE_SCALE_LEVEL_TAG));
        this.setHostileVariant(tag.getBoolean("Hostile"));
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt(HOST_ID_TAG, this.getHostId());
        tag.putInt(TARGET_ID_TAG, this.getTargetId());
        tag.putInt(INTERNAL_AMMO_TAG, this.getInternalAmmo());
        tag.putInt(LIFE_TICKS_TAG, this.lifeTicks);
        tag.putInt(ATTACK_COOLDOWN_TAG, this.attackCooldown);
        tag.putBoolean(LARGE_MODEL_TAG, this.isLargeModel());
        tag.putInt(HOSTILE_SCALE_LEVEL_TAG, this.getHostileScaleLevel());
        tag.putBoolean("Hostile", this.isHostileVariant());
    }

    @Override
    public void tick() {
        super.tick();

        if (this.tickCount > this.lifeTicks) {
            this.retire();
            return;
        }

        if (this.level().isClientSide()) {
            this.spawnMovementParticles();
            return;
        }

        LegacyShipEntity host = this.getHostShip();
        if (host == null || !host.isAlive()) {
            this.retire();
            return;
        }

        LivingEntity target = this.resolveTarget(host);
        if (target == null) {
            this.retire();
            return;
        }

        if (this.attackCooldown > 0) {
            this.attackCooldown--;
        }

        double strikeRange = Math.max(1.0D, host.getCompatAttackRange() + 1.0D);
        boolean onSight = this.getSensing().hasLineOfSight(target);
        if (this.attackCooldown <= 0
                && this.distanceToSqr(target) <= strikeRange * strikeRange
                && onSight) {
            this.setInternalAmmo(this.getInternalAmmo() - 1);
            this.performLightAttack(host, target);
            this.attackCooldown = LegacyShipCombatHelper.rensouhouAttackDelay(host);
            if (this.getInternalAmmo() <= 0) {
                this.retire();
                return;
            }
        }

        if (this.distanceToSqr(target) > strikeRange * strikeRange) {
            // EntityRensouhouMob hard-disables OnSightChase; only the friendly summon inherits it.
            if (!this.isHostileVariant() && host.isAiOnSightChase() && !onSight) {
                this.getNavigation().stop();
            } else if ((host.tickCount & 31) == 0) {
                this.getNavigation().moveTo(target, this.getAttributeValue(Attributes.MOVEMENT_SPEED));
            }
        } else {
            this.getNavigation().stop();
        }
        this.getLookControl().setLookAt(target, 30.0F, 30.0F);
    }

    private @Nullable LivingEntity resolveTarget(LegacyShipEntity host) {
        Entity current = this.level().getEntity(this.getTargetId());
        if (current instanceof LivingEntity living && living.isAlive() && host.canEngage(living)) {
            return living;
        }

        LivingEntity hostTarget = host.getTarget();
        if (hostTarget != null && hostTarget.isAlive() && host.canEngage(hostTarget)) {
            this.setTargetId(hostTarget.getId());
            return hostTarget;
        }
        return null;
    }

    private void performLightAttack(LegacyShipEntity host, LivingEntity target) {
        LegacyShipCombatHelper.AttackRoll roll = LegacyShipCombatHelper.rollAttack(
                host, target, LegacyShipAttackKind.LIGHT);
        host.setLastHurtMob(target);

        if (roll.miss()) {
            CombatFxDispatcher.sendCombatReact(this, target, CombatReactType.MISS,
                    LegacyShipAttackKind.LIGHT);
            CombatFxDispatcher.sendParticle(target, GameplayParticleType.TEXT_MISS,
                    target.getX(), target.getY() + target.getBbHeight() * 0.8D, target.getZ());
            return;
        }

        // EntityRensouhouMob marks hostile shots as projectile damage; the friendly summon uses mob damage.
        boolean attacked = target.hurt(this.isHostileVariant()
                ? this.damageSources().thrown(this, this)
                : this.damageSources().mobAttack(this), roll.damage());
        if (roll.crit()) {
            CombatFxDispatcher.sendCombatReact(this, target, CombatReactType.CRIT,
                    LegacyShipAttackKind.LIGHT);
            CombatFxDispatcher.sendParticle(target, GameplayParticleType.TEXT_CRIT,
                    target.getX(), target.getY() + target.getBbHeight() * 0.8D, target.getZ());
        } else if (roll.tripleHit()) {
            CombatFxDispatcher.sendCombatReact(this, target, CombatReactType.TRIPLE_HIT,
                    LegacyShipAttackKind.LIGHT);
            CombatFxDispatcher.sendParticle(target, GameplayParticleType.TEXT_TRIPLE,
                    target.getX(), target.getY() + target.getBbHeight() * 0.8D, target.getZ());
        } else if (roll.doubleHit()) {
            CombatFxDispatcher.sendCombatReact(this, target, CombatReactType.DOUBLE_HIT,
                    LegacyShipAttackKind.LIGHT);
            CombatFxDispatcher.sendParticle(target, GameplayParticleType.TEXT_DOUBLE,
                    target.getX(), target.getY() + target.getBbHeight() * 0.8D, target.getZ());
        }

        if (attacked) {
            host.setLastHurtMob(target);
            LegacyShipBehaviorCatalog.applyAttackEffect(host, target);
            CombatFxDispatcher.sendCombatReact(this, target, CombatReactType.HIT,
                    LegacyShipAttackKind.LIGHT);
            CombatFxDispatcher.sendParticle(target, GameplayParticleType.HIT_EXPLOSION,
                    target.getX(), target.getY() + target.getBbHeight() * 0.5D, target.getZ());
        }
    }

    private void spawnMovementParticles() {
        Vec3 motion = this.getDeltaMovement();
        if (motion.lengthSqr() > 1.0E-5D && this.tickCount % 3 == 0) {
            this.level().addParticle(ParticleTypes.SPLASH,
                    this.getX(), this.getY(), this.getZ(), -motion.x * 0.5D, 0.02D, -motion.z * 0.5D);
        }
    }

    private void retire() {
        if (!this.level().isClientSide()) {
            this.returnSummonResource();
        }
        this.discard();
    }

    private void returnSummonResource() {
        if (this.returnedResource) {
            return;
        }
        this.returnedResource = true;

        LegacyShipEntity host = this.getHostShip();
        if (host == null || host.isHostileVariant()) {
            return;
        }

        int recoveredAmmo = Math.max(0, this.getInternalAmmo() - 2);
        host.addShimakazeRensouhouCount(1);
        host.addLightAmmo(recoveredAmmo * LegacyShipBehaviorCatalog.ammoConsumptionFor(host.getSpec()));
    }

    @Override
    public EntityDimensions getDimensions(Pose pose) {
        if (this.isHostileVariant()) {
            return switch (this.getHostileScaleLevel()) {
                case 3 -> EntityDimensions.scalable(1.5F, 2.8F);
                case 2 -> EntityDimensions.scalable(1.1F, 2.1F);
                case 1 -> EntityDimensions.scalable(0.7F, 1.4F);
                default -> EntityDimensions.scalable(0.3F, 0.7F);
            };
        }
        return this.isLargeModel()
                ? EntityDimensions.scalable(0.5F, 1.4F)
                : EntityDimensions.scalable(0.3F, 0.7F);
    }

    @Override
    public boolean hurt(DamageSource damageSource, float amount) {
        if (!this.level().isClientSide()) {
            LegacyShipEntity host = this.getHostShip();
            if (host != null && !host.isHostileVariant()) {
                Entity attacker = damageSource.getEntity();
                if (attacker instanceof Player attackerPlayer) {
                    if (host.isOwnedBy(attackerPlayer)) {
                        return super.hurt(damageSource, amount);
                    }
                    if (!ShinColleConfig.friendlyFire()) {
                        return false;
                    }
                }
                if (attacker != null
                        && host.isAlliedTo(attacker)
                        && !(attacker instanceof Player player && host.isOwnedBy(player))) {
                    return false;
                }
            }
        }
        return super.hurt(damageSource, amount);
    }

    @Override
    public boolean killedEntity(ServerLevel level, LivingEntity entity) {
        LegacyShipEntity host = this.getHostShip();
        if (host != null) {
            host.incrementShipKills();
        }
        return super.killedEntity(level, entity);
    }

    @Override
    public boolean isAlliedTo(Entity entity) {
        LegacyShipEntity host = this.getHostShip();
        return host != null ? host.isAlliedTo(entity) : super.isAlliedTo(entity);
    }

    @Override
    public boolean canBreatheUnderwater() {
        return true;
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
    public boolean removeWhenFarAway(double distanceToClosestPlayer) {
        return false;
    }

    public int getHostId() {
        return this.entityData.get(DATA_HOST_ID);
    }

    public int getTargetId() {
        return this.entityData.get(DATA_TARGET_ID);
    }

    public int getInternalAmmo() {
        return this.entityData.get(DATA_INTERNAL_AMMO);
    }

    public void setInternalAmmo(int amount) {
        this.entityData.set(DATA_INTERNAL_AMMO, Math.max(0, amount));
    }

    public boolean isLargeModel() {
        return this.entityData.get(DATA_LARGE_MODEL);
    }

    public void setLargeModel(boolean largeModel) {
        this.entityData.set(DATA_LARGE_MODEL, largeModel);
        this.refreshDimensions();
    }

    public int getHostileScaleLevel() {
        return this.entityData.get(DATA_HOSTILE_SCALE_LEVEL);
    }

    public void setHostileScaleLevel(int scaleLevel) {
        this.entityData.set(DATA_HOSTILE_SCALE_LEVEL, Mth.clamp(scaleLevel, 0, 3));
        this.refreshDimensions();
    }

    public boolean isHostileVariant() {
        return this.entityData.get(DATA_HOSTILE);
    }

    private void setHostileVariant(boolean hostile) {
        this.entityData.set(DATA_HOSTILE, hostile);
    }

    private void setHostId(int hostId) {
        this.entityData.set(DATA_HOST_ID, hostId);
    }

    private void setTargetId(int targetId) {
        this.entityData.set(DATA_TARGET_ID, targetId);
    }

    private @Nullable LegacyShipEntity getHostShip() {
        Entity host = this.level().getEntity(this.getHostId());
        return host instanceof LegacyShipEntity ship ? ship : null;
    }
}
