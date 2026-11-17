package com.lulan.shincolle.entity.projectile;

import com.lulan.shincolle.entity.ship.LegacyShipAttackKind;
import com.lulan.shincolle.entity.ship.LegacyShipBehaviorCatalog;
import com.lulan.shincolle.entity.ship.LegacyShipCombatHelper;
import com.lulan.shincolle.entity.ship.LegacyShipEntity;
import com.lulan.shincolle.network.CombatFxDispatcher;
import com.lulan.shincolle.network.GameplayParticleType;
import com.lulan.shincolle.registry.ModEntityTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
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

    private static final EntityDataAccessor<Integer> DATA_HOST_ID =
            SynchedEntityData.defineId(LegacyShipFloatingFortEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_TARGET_ID =
            SynchedEntityData.defineId(LegacyShipFloatingFortEntity.class, EntityDataSerializers.INT);

    private static final int DEFAULT_LIFE_TICKS = 500;
    private static final double IMPACT_DISTANCE_SQR = 6.0D;
    private static final double IMPACT_RADIUS = 4.5D;

    private float damage;
    private int lifeTicks = DEFAULT_LIFE_TICKS;
    private boolean resolvedImpact;

    public LegacyShipFloatingFortEntity(EntityType<? extends LegacyShipFloatingFortEntity> entityType, Level level) {
        super(entityType, level);
        this.noPhysics = true;
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
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        tag.putInt(HOST_ID_TAG, this.getHostId());
        tag.putInt(TARGET_ID_TAG, this.getTargetId());
        tag.putFloat(DAMAGE_TAG, this.damage);
        tag.putInt(LIFE_TICKS_TAG, this.lifeTicks);
    }

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
        return false;
    }
}
