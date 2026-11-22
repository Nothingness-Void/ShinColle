package com.lulan.shincolle.entity.projectile;

import com.lulan.shincolle.config.ShinColleConfig;
import com.lulan.shincolle.entity.ship.LegacyShipAttackKind;
import com.lulan.shincolle.entity.ship.LegacyShipBehaviorCatalog;
import com.lulan.shincolle.entity.ship.LegacyShipCombatHelper;
import com.lulan.shincolle.entity.ship.LegacyShipEntity;
import com.lulan.shincolle.network.CombatFxDispatcher;
import com.lulan.shincolle.network.GameplayParticleType;
import com.lulan.shincolle.registry.ModEntityTypes;
import com.lulan.shincolle.entity.ship.path.LegacyShipObstaclePath;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;

/** Modern counterpart of the legacy EntityFloatingFort summon. */
public final class LegacyShipFloatingFortEntity extends Entity {

    private static final String HOST_ID_TAG = "HostId";
    private static final String TARGET_ID_TAG = "TargetId";
    private static final String DAMAGE_TAG = "Damage";
    private static final String LIFE_TICKS_TAG = "LifeTicks";
    private static final String HEALTH_TAG = "Health";
    private static final String MAX_HEALTH_TAG = "MaxHealth";

    private static final EntityDataAccessor<Integer> DATA_HOST_ID =
            SynchedEntityData.defineId(LegacyShipFloatingFortEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_TARGET_ID =
            SynchedEntityData.defineId(LegacyShipFloatingFortEntity.class, EntityDataSerializers.INT);

    private static final int DEFAULT_LIFE_TICKS = 500;
    private static final double IMPACT_DISTANCE_SQR = 6.0D;
    private static final double IMPACT_RADIUS = 4.5D;

    private float damage;
    private int lifeTicks = DEFAULT_LIFE_TICKS;
    private float maxHp = 20.0F;
    private float hp = 20.0F;
    private boolean resolvedImpact;

    public LegacyShipFloatingFortEntity(EntityType<? extends LegacyShipFloatingFortEntity> entityType, Level level) {
        super(entityType, level);
        this.setNoGravity(true);
        this.noCulling = true;
    }

    public static @Nullable LegacyShipFloatingFortEntity create(Level level,
                                                                  LegacyShipEntity host,
                                                                  LivingEntity target,
                                                                  double launchY) {
        LegacyShipFloatingFortEntity fort = ModEntityTypes.LEGACY_SHIP_FLOATING_FORT.get().create(level);
        if (fort == null) {
            return null;
        }

        fort.setHostId(host.getId());
        fort.setTargetId(target.getId());
        fort.damage = host.getLegacyStats().attackHeavy();
        fort.maxHp = Math.max(1.0F, host.getMaxHealth() * 0.1F);
        fort.hp = fort.maxHp;
        fort.moveTo(host.getX(), launchY, host.getZ(), host.getYRot(), host.getXRot());
        fort.setDeltaMovement(Vec3.ZERO);
        return fort;
    }

    @Override
    protected void defineSynchedData() {
        this.entityData.define(DATA_HOST_ID, -1);
        this.entityData.define(DATA_TARGET_ID, -1);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        this.setHostId(tag.getInt(HOST_ID_TAG));
        this.setTargetId(tag.getInt(TARGET_ID_TAG));
        this.damage = tag.getFloat(DAMAGE_TAG);
        this.lifeTicks = Math.max(40, tag.contains(LIFE_TICKS_TAG)
                ? tag.getInt(LIFE_TICKS_TAG) : DEFAULT_LIFE_TICKS);
        this.maxHp = tag.contains(MAX_HEALTH_TAG) ? Math.max(1.0F, tag.getFloat(MAX_HEALTH_TAG)) : 20.0F;
        this.hp = tag.contains(HEALTH_TAG) ? Math.max(0.0F, tag.getFloat(HEALTH_TAG)) : this.maxHp;
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        tag.putInt(HOST_ID_TAG, this.getHostId());
        tag.putInt(TARGET_ID_TAG, this.getTargetId());
        tag.putFloat(DAMAGE_TAG, this.damage);
        tag.putInt(LIFE_TICKS_TAG, this.lifeTicks);
        tag.putFloat(HEALTH_TAG, this.hp);
        tag.putFloat(MAX_HEALTH_TAG, this.maxHp);
    }

    private final LegacyShipObstaclePath obstaclePath = new LegacyShipObstaclePath();

    @Override
    public void tick() {
        super.tick();

        if (this.tickCount >= this.lifeTicks) {
            this.resolveImpact();
            this.discard();
            return;
        }

        if (this.level().isClientSide()) {
            this.spawnTrailParticles();
            this.move(MoverType.SELF, this.getDeltaMovement());
            return;
        }

        LegacyShipEntity host = this.getHostShip();
        LivingEntity target = this.getTargetEntity();
        if (host == null || !host.isAlive() || target == null || !target.isAlive()
                || !host.canEngage(target)) {
            this.resolveImpact();
            this.discard();
            return;
        }

        Vec3 targetPosition = target.position().add(0.0D, 1.0D, 0.0D);
        Vec3 offset = targetPosition.subtract(this.position());
        double distanceSqr = offset.lengthSqr();
        if (distanceSqr <= IMPACT_DISTANCE_SQR) {
            this.resolveImpact();
            this.discard();
            return;
        }

        offset = this.obstaclePath.resolve(this, targetPosition).subtract(this.position());
        double speed = Math.max(0.18D, host.getLegacyStats().moveSpeed() * 0.1D + 0.3D);
        Vec3 movement = offset.normalize().scale(speed);
        this.setDeltaMovement(movement);
        this.setYRot((float) (Math.atan2(movement.x, movement.z) * (180.0D / Math.PI)));
        this.setXRot((float) (-Math.atan2(movement.y,
                Math.sqrt(movement.x * movement.x + movement.z * movement.z)) * (180.0D / Math.PI)));
        this.yRotO = this.getYRot();
        this.xRotO = this.getXRot();
        this.move(MoverType.SELF, movement);
    }

    private void resolveImpact() {
        if (this.resolvedImpact || this.level().isClientSide()) {
            return;
        }
        this.resolvedImpact = true;

        LegacyShipEntity host = this.getHostShip();
        if (host == null || !host.isAlive()) {
            return;
        }

        if (LegacyShipBehaviorCatalog.usesBlackHoleHeavyMissile(host, LegacyShipAttackKind.HEAVY)) {
            LegacyShipBehaviorCatalog.spawnBlackHoleEffect(host, this.position());
        }

        AABB impactBox = this.getBoundingBox().inflate(IMPACT_RADIUS);
        for (LivingEntity target : this.level().getEntitiesOfClass(
                LivingEntity.class,
                impactBox,
                candidate -> candidate != host
                        && candidate.isAlive()
                        && !candidate.isInvulnerable()
                        && host.canEngage(candidate))) {
            float targetDamage = LegacyShipCombatHelper.applyTargetDamageModifier(
                    host.getLegacyStats(), target, this.damage);
            host.performCatalogDirectAttack(
                    target,
                    // EntityFloatingFort.onImpact rolls each blast target at distance 1
                    // without the legacy double/triple-hit branches.
                    LegacyShipCombatHelper.rollProjectileImpactAttack(host, target, targetDamage),
                    false,
                    true,
                    LegacyShipProjectileVisual.BOMB,
                    GameplayParticleType.BOMB_IMPACT);
        }

        CombatFxDispatcher.sendParticle(this, GameplayParticleType.BOMB_IMPACT,
                this.getX(), this.getY(), this.getZ(),
                this.getDeltaMovement().x, this.getDeltaMovement().y, this.getDeltaMovement().z);
    }

    private void spawnTrailParticles() {
        if ((this.tickCount & 1) != 0) {
            return;
        }

        Vec3 motion = this.getDeltaMovement();
        this.level().addParticle(net.minecraft.core.particles.ParticleTypes.CLOUD,
                this.getX(), this.getY() + 0.2D, this.getZ(),
                -motion.x * 0.5D, 0.07D, -motion.z * 0.5D);
    }

    public int getHostId() {
        return this.entityData.get(DATA_HOST_ID);
    }

    public int getTargetId() {
        return this.entityData.get(DATA_TARGET_ID);
    }

    public int getLifeTicks() {
        return this.lifeTicks;
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

    private @Nullable LivingEntity getTargetEntity() {
        Entity target = this.level().getEntity(this.getTargetId());
        return target instanceof LivingEntity living ? living : null;
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
    public boolean isPickable() {
        return this.isAlive();
    }

    @Override
    public boolean hurt(DamageSource damageSource, float amount) {
        if (this.level().isClientSide()) {
            return false;
        }

        LegacyShipEntity host = this.getHostShip();
        if (host == null) {
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
            this.discard();
            return false;
        }

        Entity attacker = damageSource.getEntity();
        if (!host.isHostileVariant() && attacker instanceof Player attackerPlayer && host.isOwnedBy(attackerPlayer)) {
            return applyDamage(amount);
        }
        if (this.isInvulnerableTo(damageSource) || attacker == null || attacker.equals(this)) {
            return false;
        }

        if (!host.isHostileVariant()) {
            if (attacker instanceof Player && !ShinColleConfig.friendlyFire()) {
                return false;
            }
            if (host.isAlliedTo(attacker)
                    && !(attacker instanceof Player player && host.isOwnedBy(player))) {
                return false;
            }
        }

        if (this.random.nextFloat() <= host.getLegacyStats().dodge() + 0.3F) {
            return false;
        }

        float reducedAtk = amount;
        if (!damageSource.is(DamageTypeTags.BYPASSES_ARMOR)
                && !damageSource.is(DamageTypes.MAGIC)
                && !damageSource.is(DamageTypes.WITHER)
                && !damageSource.is(DamageTypes.DRAGON_BREATH)) {
            float def = host.getLegacyStats().defense() * 0.5F;
            float factor = 1.0F - def + (this.random.nextFloat() * 0.5F - 0.25F);
            reducedAtk = amount * Math.max(0.0F, factor);
        }
        if (reducedAtk < 1.0F && reducedAtk > 0.0F) {
            reducedAtk = 1.0F;
        } else if (reducedAtk <= 0.0F) {
            reducedAtk = 0.0F;
        }

        return applyDamage(reducedAtk);
    }

    private boolean applyDamage(float amount) {
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

    public float getSummonHealth() {
        return this.hp;
    }

    public float getSummonMaxHealth() {
        return this.maxHp;
    }
}
