package com.lulan.shincolle.entity.other;

import com.lulan.shincolle.entity.ship.LegacyShipEntity;
import com.lulan.shincolle.registry.ModEntityTypes;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;

/** Modern counterpart of EntityShipFishingHook, kept separate from vanilla player fishing. */
public final class LegacyShipFishingHookEntity extends Entity {

    private static final String HOST_ID_TAG = "HostId";
    private static final EntityDataAccessor<Integer> DATA_HOST_ID =
            SynchedEntityData.defineId(LegacyShipFishingHookEntity.class, EntityDataSerializers.INT);

    public LegacyShipFishingHookEntity(EntityType<? extends LegacyShipFishingHookEntity> entityType, Level level) {
        super(entityType, level);
        this.noPhysics = true;
        this.setNoGravity(true);
        this.noCulling = true;
    }

    public static @Nullable LegacyShipFishingHookEntity create(Level level, LegacyShipEntity host) {
        if (host == null || host.isHostileVariant()) {
            return null;
        }

        LegacyShipFishingHookEntity hook = ModEntityTypes.LEGACY_SHIP_FISHING_HOOK.get().create(level);
        if (hook == null) {
            return null;
        }

        hook.host = host;
        hook.setHostId(host.getId());
        host.setFishingHook(hook);
        return hook;
    }

    @Nullable
    private LegacyShipEntity host;

    @Override
    protected void defineSynchedData() {
        this.entityData.define(DATA_HOST_ID, -1);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        this.setHostId(tag.getInt(HOST_ID_TAG));
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        tag.putInt(HOST_ID_TAG, this.getHostId());
    }

    @Override
    public void tick() {
        super.tick();

        if (this.tickCount > 1000) {
            this.discard();
            return;
        }

        if (this.level().isClientSide()) {
            Vec3 motion = this.getDeltaMovement().scale(0.9D);
            this.setDeltaMovement(motion);
            this.setPos(this.getX(), this.getY(), this.getZ());
            if (this.tickCount == 4) {
                this.spawnSplashParticles();
            } else if ((this.tickCount & 63) == 0 && this.random.nextFloat() < 0.35F) {
                this.spawnSplashParticles();
            }
            return;
        }

        LegacyShipEntity ship = this.getHostShip();
        if (ship == null || ship.isHostileVariant() || !ship.isAlive()
                || (!ship.getLegacyTaskMainHandItem().is(Items.FISHING_ROD)
                && !ship.getLegacyTaskOffHandItem().is(Items.FISHING_ROD))
                || this.distanceToSqr(ship) > 1024.0D) {
            this.discard();
            return;
        }

        if (this.tickCount == 4) {
            this.playSound(SoundEvents.FISHING_BOBBER_SPLASH, 0.25F,
                    1.0F + (this.random.nextFloat() - this.random.nextFloat()) * 0.4F);
        }
    }

    private void spawnSplashParticles() {
        this.level().addParticle(ParticleTypes.SPLASH,
                this.getX(), this.getY() - 0.1D, this.getZ(),
                0.0D, 0.02D, 0.0D);
        this.level().addParticle(ParticleTypes.BUBBLE,
                this.getX(), this.getY() - 0.1D, this.getZ(),
                0.0D, 0.025D, 0.0D);
    }

    public int getHostId() {
        return this.entityData.get(DATA_HOST_ID);
    }

    private void setHostId(int hostId) {
        this.entityData.set(DATA_HOST_ID, hostId);
    }

    public @Nullable LegacyShipEntity getHostShip() {
        if (this.host != null && !this.host.isRemoved()) {
            return this.host;
        }

        Entity candidate = this.level().getEntity(this.getHostId());
        if (candidate instanceof LegacyShipEntity ship) {
            this.host = ship;
            return ship;
        }
        return null;
    }

    @Override
    public void remove(RemovalReason removalReason) {
        LegacyShipEntity ship = this.host;
        this.host = null;
        if (ship != null) {
            ship.clearFishingHook(this);
        }
        super.remove(removalReason);
    }

    @Override
    public boolean isInvulnerableTo(DamageSource source) {
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
    public boolean isPickable() {
        return false;
    }
}
