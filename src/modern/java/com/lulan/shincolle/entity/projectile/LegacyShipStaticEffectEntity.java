package com.lulan.shincolle.entity.projectile;

import com.lulan.shincolle.entity.ship.LegacyShipBehaviorCatalog;
import com.lulan.shincolle.entity.ship.LegacyShipEntity;
import com.lulan.shincolle.network.CombatFxDispatcher;
import com.lulan.shincolle.network.GameplayParticleType;
import com.lulan.shincolle.registry.ModEntityTypes;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;

/** Modern counterpart of EntityProjectileStatic's type-0 black-hole field. */
public final class LegacyShipStaticEffectEntity extends Entity {

    private static final String HOST_ID_TAG = "HostId";
    private static final String LIFE_TICKS_TAG = "LifeTicks";
    private static final String PULL_FORCE_TAG = "PullForce";
    private static final String RANGE_TAG = "Range";

    private static final EntityDataAccessor<Integer> DATA_HOST_ID =
            SynchedEntityData.defineId(LegacyShipStaticEffectEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_LIFE_TICKS =
            SynchedEntityData.defineId(LegacyShipStaticEffectEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Float> DATA_PULL_FORCE =
            SynchedEntityData.defineId(LegacyShipStaticEffectEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> DATA_RANGE =
            SynchedEntityData.defineId(LegacyShipStaticEffectEntity.class, EntityDataSerializers.FLOAT);

    public LegacyShipStaticEffectEntity(EntityType<? extends LegacyShipStaticEffectEntity> entityType, Level level) {
        super(entityType, level);
        this.noPhysics = true;
        this.setNoGravity(true);
        this.noCulling = true;
    }

    public static @Nullable LegacyShipStaticEffectEntity create(Level level,
                                                                  LegacyShipEntity host,
                                                                  Vec3 position) {
        if (host == null || position == null) {
            return null;
        }

        LegacyShipStaticEffectEntity effect = ModEntityTypes.LEGACY_SHIP_STATIC_EFFECT.get().create(level);
        if (effect == null) {
            return null;
        }

        int shipLevel = Math.max(1, host.getShipLevel());
        effect.setHostId(host.getId());
        effect.setLifeTicks((int) (20.0D + shipLevel * 0.125D));
        effect.setPullForce((float) (0.12D + shipLevel * 0.00075D));
        effect.setRange((float) (4.0D + shipLevel * 0.035D));
        effect.moveTo(position.x, position.y, position.z, 0.0F, 0.0F);
        return effect;
    }

    @Override
    protected void defineSynchedData(net.minecraft.network.syncher.SynchedEntityData.Builder builder) {
        builder.define(DATA_HOST_ID, -1);
        builder.define(DATA_LIFE_TICKS, 20);
        builder.define(DATA_PULL_FORCE, 0.12F);
        builder.define(DATA_RANGE, 4.0F);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        this.setHostId(tag.getInt(HOST_ID_TAG));
        this.setLifeTicks(tag.contains(LIFE_TICKS_TAG) ? tag.getInt(LIFE_TICKS_TAG) : 20);
        this.setPullForce(tag.contains(PULL_FORCE_TAG) ? tag.getFloat(PULL_FORCE_TAG) : 0.12F);
        this.setRange(tag.contains(RANGE_TAG) ? tag.getFloat(RANGE_TAG) : 4.0F);
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        tag.putInt(HOST_ID_TAG, this.getHostId());
        tag.putInt(LIFE_TICKS_TAG, this.getLifeTicks());
        tag.putFloat(PULL_FORCE_TAG, this.getPullForce());
        tag.putFloat(RANGE_TAG, this.getRange());
    }

    @Override
    public void tick() {
        super.tick();

        if (this.tickCount > this.getLifeTicks()) {
            this.discard();
            return;
        }

        if (this.level().isClientSide()) {
            this.spawnClientParticles();
            return;
        }

        LegacyShipEntity host = this.getHostShip();
        if (host == null || !host.isAlive()) {
            this.discard();
            return;
        }

        if (this.tickCount == 1) {
            CombatFxDispatcher.sendParticle(this, GameplayParticleType.BLACK_HOLE,
                    this.getX(), this.getY(), this.getZ());
        }
        if ((this.tickCount & 3) == 0) {
            this.pullEntities(host);
        }
    }

    private void pullEntities(LegacyShipEntity host) {
        AABB searchArea = this.getBoundingBox().inflate(this.getRange());
        for (Entity candidate : this.level().getEntitiesOfClass(Entity.class, searchArea,
                entity -> this.canPullEntity(entity, host))) {
            Vec3 offset = candidate.position().subtract(this.position());
            double distance = offset.length();
            if (distance <= 1.0D) {
                continue;
            }

            Vec3 pull = offset.scale(-this.getPullForce() / distance);
            candidate.setDeltaMovement(candidate.getDeltaMovement().add(pull));
            candidate.hasImpulse = true;
        }
    }

    private boolean canPullEntity(Entity candidate, LegacyShipEntity host) {
        return candidate != this
                && candidate != host
                && candidate != host.getVehicle()
                && !host.getPassengers().contains(candidate)
                && candidate.isPickable()
                && candidate.isPushable()
                && !candidate.isInvulnerable()
                && (!(candidate instanceof net.minecraft.world.entity.LivingEntity living) || living.isAlive())
                && (candidate instanceof net.minecraft.world.entity.LivingEntity livingCandidate
                        ? !LegacyShipBehaviorCatalog.hasLegacySameOwner(host, livingCandidate)
                        : !host.isAlliedTo(candidate));
    }

    private void spawnClientParticles() {
        if ((this.tickCount & 1) == 0) {
            double angle = this.tickCount * 0.45D;
            double radius = Math.min(1.3D, this.getRange() * 0.16D);
            this.level().addParticle(ParticleTypes.PORTAL,
                    this.getX() + Math.cos(angle) * radius,
                    this.getY() + 0.15D,
                    this.getZ() + Math.sin(angle) * radius,
                    -Math.sin(angle) * 0.04D, 0.02D, Math.cos(angle) * 0.04D);
        }
    }

    public int getHostId() {
        return this.entityData.get(DATA_HOST_ID);
    }

    public int getLifeTicks() {
        return this.entityData.get(DATA_LIFE_TICKS);
    }

    public float getPullForce() {
        return this.entityData.get(DATA_PULL_FORCE);
    }

    public float getRange() {
        return this.entityData.get(DATA_RANGE);
    }

    private void setHostId(int hostId) {
        this.entityData.set(DATA_HOST_ID, hostId);
    }

    private void setLifeTicks(int lifeTicks) {
        this.entityData.set(DATA_LIFE_TICKS, Math.max(1, lifeTicks));
    }

    private void setPullForce(float pullForce) {
        this.entityData.set(DATA_PULL_FORCE, Math.max(0.0F, pullForce));
    }

    private void setRange(float range) {
        this.entityData.set(DATA_RANGE, Math.max(0.0F, range));
    }

    private @Nullable LegacyShipEntity getHostShip() {
        Entity host = this.level().getEntity(this.getHostId());
        return host instanceof LegacyShipEntity ship ? ship : null;
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
