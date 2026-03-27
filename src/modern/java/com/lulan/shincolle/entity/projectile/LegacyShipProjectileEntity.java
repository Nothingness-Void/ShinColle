package com.lulan.shincolle.entity.projectile;

import com.lulan.shincolle.entity.ship.LegacyShipAttackKind;
import com.lulan.shincolle.entity.ship.LegacyShipEntity;
import com.lulan.shincolle.registry.ModEntityTypes;
import com.lulan.shincolle.registry.ModItems;
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

    private float damage;
    private int targetId = -1;
    private int lifeTicks = 80;
    private boolean missedShot;

    public LegacyShipProjectileEntity(EntityType<? extends LegacyShipProjectileEntity> entityType, Level level) {
        super(entityType, level);
    }

    public LegacyShipProjectileEntity(Level level, LegacyShipEntity owner) {
        super(ModEntityTypes.LEGACY_SHIP_PROJECTILE.get(), owner, level);
    }

    public static LegacyShipProjectileEntity create(Level level, LegacyShipEntity owner, LivingEntity target,
                                                    LegacyShipAttackKind attackKind, float damage, boolean missedShot) {
        LegacyShipProjectileEntity projectile = new LegacyShipProjectileEntity(level, owner);
        projectile.setAttackKind(attackKind);
        projectile.damage = damage;
        projectile.missedShot = missedShot;
        projectile.targetId = missedShot ? -1 : target.getId();
        projectile.lifeTicks = switch (attackKind) {
            case HEAVY -> 80;
            case AIR_LIGHT, AIR_HEAVY -> 100;
            default -> 60;
        };

        double launchY = owner.getY() + owner.getBbHeight() * switch (attackKind) {
            case HEAVY -> 0.7D;
            case AIR_LIGHT, AIR_HEAVY -> 0.9D;
            default -> 0.6D;
        };

        projectile.moveTo(owner.getX(), launchY, owner.getZ(), owner.getYRot(), owner.getXRot());
        Vec3 targetPos = projectile.resolveTargetPos(target, attackKind, missedShot);
        projectile.shoot(targetPos.x - projectile.getX(), targetPos.y - projectile.getY(), targetPos.z - projectile.getZ(),
                (float) projectile.baseSpeed(attackKind), 0.0F);
        return projectile;
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_ATTACK_KIND, LegacyShipAttackKind.HEAVY.ordinal());
    }

    @Override
    public void tick() {
        super.tick();

        if (this.tickCount > this.lifeTicks) {
            this.discard();
            return;
        }

        if (!this.level().isClientSide()) {
            this.updateAirGuidance();
        }
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        Entity hitEntity = result.getEntity();
        Entity owner = this.getOwner();

        if (this.level().isClientSide() || !(owner instanceof LegacyShipEntity ship) || !(hitEntity instanceof LivingEntity livingTarget)) {
            this.discard();
            return;
        }

        if (!ship.canEngage(livingTarget)) {
            this.discard();
            return;
        }

        boolean attacked = livingTarget.hurt(this.damageSources().thrown(this, ship), this.damage);
        if (attacked) {
            ship.setLastHurtMob(livingTarget);
            if (this.getAttackKind() == LegacyShipAttackKind.HEAVY || this.getAttackKind() == LegacyShipAttackKind.AIR_HEAVY) {
                double knockbackStrength = 0.4D;
                livingTarget.knockback(knockbackStrength, ship.getX() - livingTarget.getX(), ship.getZ() - livingTarget.getZ());
            }
        }

        this.discard();
    }

    @Override
    protected void onHit(HitResult result) {
        super.onHit(result);

        if (result.getType() != HitResult.Type.ENTITY) {
            this.discard();
        }
    }

    @Override
    protected boolean canHitEntity(Entity entity) {
        if (!super.canHitEntity(entity) || entity == this.getOwner() || !(entity instanceof LivingEntity)) {
            return false;
        }

        if (this.targetId >= 0) {
            return entity.getId() == this.targetId;
        }

        return !this.missedShot;
    }

    @Override
    protected Item getDefaultItem() {
        return switch (this.getAttackKind()) {
            case AIR_LIGHT, AIR_HEAVY -> ModItems.TOYAIRPLANE.get();
            case LIGHT -> ModItems.AMMO.get();
            case HEAVY, MELEE -> ModItems.AMMO1.get();
        };
    }

    @Override
    public ItemStack getItem() {
        return new ItemStack(this.getDefaultItem());
    }

    @Override
    protected float getGravity() {
        return 0.0F;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("AttackKind", this.getAttackKind().ordinal());
        tag.putFloat("Damage", this.damage);
        tag.putInt("TargetId", this.targetId);
        tag.putInt("LifeTicks", this.lifeTicks);
        tag.putBoolean("MissedShot", this.missedShot);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.setAttackKind(attackKindByOrdinal(tag.getInt("AttackKind")));
        this.damage = tag.getFloat("Damage");
        this.targetId = tag.getInt("TargetId");
        this.lifeTicks = tag.getInt("LifeTicks");
        this.missedShot = tag.getBoolean("MissedShot");
    }

    public LegacyShipAttackKind getAttackKind() {
        return attackKindByOrdinal(this.entityData.get(DATA_ATTACK_KIND));
    }

    private void setAttackKind(LegacyShipAttackKind attackKind) {
        this.entityData.set(DATA_ATTACK_KIND, attackKind.ordinal());
    }

    private void updateAirGuidance() {
        LegacyShipAttackKind attackKind = this.getAttackKind();
        if ((attackKind != LegacyShipAttackKind.AIR_LIGHT && attackKind != LegacyShipAttackKind.AIR_HEAVY) || this.missedShot) {
            return;
        }

        LivingEntity target = this.getIntendedTarget();
        if (target == null || !target.isAlive()) {
            return;
        }

        double speed = this.baseSpeed(attackKind);
        Vec3 desired = target.position().add(0.0D, target.getBbHeight() * 0.35D, 0.0D).subtract(this.position());
        if (desired.lengthSqr() < 1.0E-4D) {
            return;
        }

        Vec3 current = this.getDeltaMovement();
        Vec3 redirected = current.scale(0.82D).add(desired.normalize().scale(speed * 0.18D));
        if (redirected.lengthSqr() < 1.0E-6D) {
            redirected = desired.normalize().scale(speed);
        } else {
            redirected = redirected.normalize().scale(speed);
        }

        this.setDeltaMovement(redirected);
    }

    private @Nullable LivingEntity getIntendedTarget() {
        if (this.targetId < 0) {
            return null;
        }

        Entity entity = this.level().getEntity(this.targetId);
        return entity instanceof LivingEntity livingEntity ? livingEntity : null;
    }

    private Vec3 resolveTargetPos(LivingEntity target, LegacyShipAttackKind attackKind, boolean missedShot) {
        double targetX = target.getX();
        double targetY = target.getY() + target.getBbHeight() * 0.35D;
        double targetZ = target.getZ();

        if (missedShot) {
            targetX += this.random.nextDouble() * 10.0D - 5.0D;
            targetY += this.random.nextDouble() * 5.0D;
            targetZ += this.random.nextDouble() * 10.0D - 5.0D;
        }

        if (attackKind == LegacyShipAttackKind.HEAVY) {
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

    private double baseSpeed(LegacyShipAttackKind attackKind) {
        return switch (attackKind) {
            case HEAVY -> 0.52D;
            case AIR_LIGHT -> 0.42D;
            case AIR_HEAVY -> 0.38D;
            case LIGHT -> 0.75D;
            case MELEE -> 0.0D;
        };
    }

    private static LegacyShipAttackKind attackKindByOrdinal(int ordinal) {
        LegacyShipAttackKind[] values = LegacyShipAttackKind.values();
        return values[Mth.clamp(ordinal, 0, values.length - 1)];
    }
}
