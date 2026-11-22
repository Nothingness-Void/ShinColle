package com.lulan.shincolle.entity.projectile;

import com.lulan.shincolle.entity.ship.LegacyShipAttackKind;
import com.lulan.shincolle.entity.ship.LegacyShipBehaviorCatalog;
import com.lulan.shincolle.entity.ship.LegacyShipCombatHelper;
import com.lulan.shincolle.entity.ship.LegacyShipEntity;
import com.lulan.shincolle.network.CombatFxDispatcher;
import com.lulan.shincolle.network.CombatReactType;
import com.lulan.shincolle.network.GameplayParticleType;
import com.lulan.shincolle.registry.ModEntityTypes;
import com.lulan.shincolle.registry.ModSoundEvents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.HashSet;
import java.util.Set;

/**
 * Modern counterpart of the legacy EntityProjectileBeam.
 *
 * The entity is deliberately invisible: the old renderer drew the beam head
 * with the particle bus, while the server entity performed one hit per target
 * as it travelled through its collision box.
 */
public final class LegacyShipBeamEntity extends Entity {

    private static final EntityDataAccessor<Integer> DATA_HOST_ID =
            SynchedEntityData.defineId(LegacyShipBeamEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_BEAM_TYPE =
            SynchedEntityData.defineId(LegacyShipBeamEntity.class, EntityDataSerializers.INT);

    private static final int NORMAL_BEAM_TYPE = 0;
    private static final int GAE_BOLG_BEAM_TYPE = 1;
    private static final int NORMAL_LIFE_TICKS = 31;
    private static final int GAE_BOLG_LIFE_TICKS = 8;
    private static final double NORMAL_TRAVEL_SPEED = 4.0D;
    private static final double GAE_BOLG_TRAVEL_SPEED = 3.0D;
    private static final double COLLISION_RADIUS = 1.5D;

    private final Set<Integer> damagedTargetIds = new HashSet<>();
    private Vec3 travelDirection = Vec3.ZERO;
    private float damage;

    public LegacyShipBeamEntity(EntityType<? extends LegacyShipBeamEntity> entityType, Level level) {
        super(entityType, level);
        this.noPhysics = true;
        this.setNoGravity(true);
        this.noCulling = true;
        this.setBoundingBox(this.getBoundingBox().inflate(0.5D));
    }

    public static LegacyShipBeamEntity create(Level level, LegacyShipEntity host, LivingEntity target,
                                               float damage) {
        LegacyShipBeamEntity beam = ModEntityTypes.LEGACY_SHIP_BEAM.get().create(level);
        if (beam == null) {
            return null;
        }

        Vec3 targetPosition = target.position();
        if (host.isHostileVariant()) {
            targetPosition = targetPosition.add(0.0D,
                    -host.getLegacyHostileScaleLevel() * 0.6D, 0.0D);
        }

        return create(level, host, targetPosition.subtract(host.position()), damage, NORMAL_BEAM_TYPE);
    }

    /** Mirrors EntityProjectileBeam type 1, the short Gae Bolg/Tatsuta beam. */
    public static LegacyShipBeamEntity createGaeBolg(Level level, LegacyShipEntity host,
                                                       Vec3 direction, float damage) {
        return create(level, host, direction, damage, GAE_BOLG_BEAM_TYPE);
    }

    private static LegacyShipBeamEntity create(Level level, LegacyShipEntity host,
                                                Vec3 direction, float damage, int beamType) {
        LegacyShipBeamEntity beam = ModEntityTypes.LEGACY_SHIP_BEAM.get().create(level);
        if (beam == null) {
            return null;
        }

        if (direction == null || direction.lengthSqr() <= 1.0E-4D) {
            direction = host.getLookAngle();
        }
        if (direction.lengthSqr() <= 1.0E-4D) {
            direction = new Vec3(0.0D, 0.0D, 1.0D);
        } else {
            direction = direction.normalize();
        }

        beam.setHostId(host.getId());
        beam.setBeamType(beamType);
        beam.damage = Math.max(0.0F, damage);
        beam.travelDirection = direction;
        boolean gaeBolg = beamType == GAE_BOLG_BEAM_TYPE;
        beam.moveTo(host.getX() + (gaeBolg ? 0.0D : direction.x),
                host.getY() + host.getBbHeight() * (gaeBolg ? 0.75D : 0.5D),
                host.getZ() + (gaeBolg ? 0.0D : direction.z),
                host.getYRot(), host.getXRot());
        beam.setDeltaMovement(direction.scale(beam.getTravelSpeed()));
        return beam;
    }

    @Override
    protected void defineSynchedData() {
        this.entityData.define(DATA_HOST_ID, -1);
        this.entityData.define(DATA_BEAM_TYPE, NORMAL_BEAM_TYPE);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        this.setHostId(tag.getInt("HostId"));
        this.setBeamType(tag.contains("BeamType") ? tag.getInt("BeamType") : NORMAL_BEAM_TYPE);
        this.damage = tag.getFloat("Damage");
        this.travelDirection = new Vec3(
                tag.getDouble("DirectionX"),
                tag.getDouble("DirectionY"),
                tag.getDouble("DirectionZ"));
        if (this.travelDirection.lengthSqr() > 1.0E-4D) {
            this.travelDirection = this.travelDirection.normalize();
            this.setDeltaMovement(this.travelDirection.scale(this.getTravelSpeed()));
        }
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        tag.putInt("HostId", this.getHostId());
        tag.putInt("BeamType", this.getBeamType());
        tag.putFloat("Damage", this.damage);
        tag.putDouble("DirectionX", this.travelDirection.x);
        tag.putDouble("DirectionY", this.travelDirection.y);
        tag.putDouble("DirectionZ", this.travelDirection.z);
    }

    @Override
    public void tick() {
        super.tick();

        if (this.tickCount > this.getLifeTicks()) {
            this.discard();
            return;
        }

        if (this.level().isClientSide()) {
            this.spawnBeamTrail();
            this.move(MoverType.SELF, this.getDeltaMovement());
            return;
        }

        LegacyShipEntity host = this.getHostShip();
        if (host == null || !host.isAlive()) {
            this.discard();
            return;
        }

        this.setDeltaMovement(this.travelDirection.scale(this.getTravelSpeed()));
        this.move(MoverType.SELF, this.getDeltaMovement());

        AABB impactBox = this.getBoundingBox().inflate(COLLISION_RADIUS);
        for (LivingEntity target : this.level().getEntitiesOfClass(
                LivingEntity.class, impactBox, candidate -> candidate != host
                        && candidate.isAlive()
                        && !candidate.isInvulnerable()
                        && candidate.isPickable()
                        && !LegacyShipBehaviorCatalog.isLegacyProjectileHostRelated(host, candidate)
                        && !this.damagedTargetIds.contains(candidate.getId()))) {
            this.damagedTargetIds.add(target.getId());
            this.applyImpact(host, target);
        }
    }

    private void applyImpact(LegacyShipEntity host, LivingEntity target) {
        this.playSound(ModSoundEvents.SHIP_EXPLODE.get(), 1.5F,
                0.7F / (this.random.nextFloat() * 0.4F + 0.8F));

        if (!LegacyShipBehaviorCatalog.isLegacyProjectileDamageAllowed(host, target)) {
            target.hurt(this.damageSources().indirectMagic(this, host), 0.0F);
            return;
        }

        float impactDamage = LegacyShipCombatHelper.applyTargetDamageModifier(
                host.getLegacyStats(), target, this.damage);
        LegacyShipCombatHelper.AttackRoll roll = LegacyShipCombatHelper.rollProjectileImpactAttack(
                host, target, impactDamage);
        if (roll.miss()) {
            CombatFxDispatcher.sendCombatReact(host, target, CombatReactType.MISS,
                    LegacyShipAttackKind.HEAVY, LegacyShipProjectileVisual.NONE);
            CombatFxDispatcher.sendParticle(target, GameplayParticleType.TEXT_MISS,
                    target.getX(), target.getY() + target.getBbHeight() * 0.8D, target.getZ());
            return;
        }

        if (roll.crit()) {
            CombatFxDispatcher.sendCombatReact(host, target, CombatReactType.CRIT,
                    LegacyShipAttackKind.HEAVY, LegacyShipProjectileVisual.NONE);
            CombatFxDispatcher.sendParticle(target, GameplayParticleType.TEXT_CRIT,
                    target.getX(), target.getY() + target.getBbHeight() * 1.3D, target.getZ());
        } else if (roll.tripleHit()) {
            CombatFxDispatcher.sendCombatReact(host, target, CombatReactType.TRIPLE_HIT,
                    LegacyShipAttackKind.HEAVY, LegacyShipProjectileVisual.NONE);
            CombatFxDispatcher.sendParticle(target, GameplayParticleType.TEXT_TRIPLE,
                    target.getX(), target.getY() + target.getBbHeight() * 1.3D, target.getZ());
        } else if (roll.doubleHit()) {
            CombatFxDispatcher.sendCombatReact(host, target, CombatReactType.DOUBLE_HIT,
                    LegacyShipAttackKind.HEAVY, LegacyShipProjectileVisual.NONE);
            CombatFxDispatcher.sendParticle(target, GameplayParticleType.TEXT_DOUBLE,
                    target.getX(), target.getY() + target.getBbHeight() * 1.3D, target.getZ());
        }

        boolean attacked = target.hurt(this.damageSources().indirectMagic(this, host), roll.damage());
        if (attacked) {
            host.setLastHurtMob(target);
            LegacyShipBehaviorCatalog.applyAttackEffect(host, target);
            CombatFxDispatcher.sendCombatReact(host, target, CombatReactType.HIT,
                    LegacyShipAttackKind.HEAVY, LegacyShipProjectileVisual.NONE);
            CombatFxDispatcher.sendParticle(target, GameplayParticleType.BEAM_IMPACT,
                    target.getX(), target.getY() + target.getBbHeight() * 0.5D, target.getZ());
        }
    }

    private void spawnBeamTrail() {
        Vec3 pos = this.position();
        this.level().addParticle(ParticleTypes.END_ROD,
                pos.x, pos.y, pos.z,
                this.travelDirection.x * 0.05D,
                this.travelDirection.y * 0.05D,
                this.travelDirection.z * 0.05D);
        if (this.tickCount % 2 == 0) {
            this.level().addParticle(ParticleTypes.ELECTRIC_SPARK,
                    pos.x, pos.y, pos.z, 0.0D, 0.0D, 0.0D);
        }
    }

    public int getHostId() {
        return this.entityData.get(DATA_HOST_ID);
    }

    public int getBeamType() {
        return this.entityData.get(DATA_BEAM_TYPE);
    }

    public int getLifeTicks() {
        return this.getBeamType() == GAE_BOLG_BEAM_TYPE
                ? GAE_BOLG_LIFE_TICKS : NORMAL_LIFE_TICKS;
    }

    public double getTravelSpeed() {
        return this.getBeamType() == GAE_BOLG_BEAM_TYPE
                ? GAE_BOLG_TRAVEL_SPEED : NORMAL_TRAVEL_SPEED;
    }

    public float getDamage() {
        return this.damage;
    }

    private void setHostId(int hostId) {
        this.entityData.set(DATA_HOST_ID, hostId);
    }

    private void setBeamType(int beamType) {
        this.entityData.set(DATA_BEAM_TYPE,
                beamType == GAE_BOLG_BEAM_TYPE ? GAE_BOLG_BEAM_TYPE : NORMAL_BEAM_TYPE);
    }

    private LegacyShipEntity getHostShip() {
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
