package com.lulan.shincolle.entity.mount;

import com.lulan.shincolle.morph.MorphHelper;
import com.lulan.shincolle.registry.ModEntityTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.UUID;

public class LegacyMountEntity extends Entity {

    private static final String OWNER_UUID_TAG = "OwnerUuid";
    private static final String OWNER_NAME_TAG = "OwnerName";
    private static final EntityDataAccessor<Optional<UUID>> DATA_OWNER_UUID =
            SynchedEntityData.defineId(LegacyMountEntity.class, EntityDataSerializers.OPTIONAL_UUID);
    private static final EntityDataAccessor<String> DATA_OWNER_NAME =
            SynchedEntityData.defineId(LegacyMountEntity.class, EntityDataSerializers.STRING);

    public LegacyMountEntity(EntityType<? extends LegacyMountEntity> entityType, Level level) {
        super(entityType, level);
        this.noPhysics = false;
    }

    public static LegacyMountEntity create(ServerLevel level, ServerPlayer owner) {
        LegacyMountEntity mount = ModEntityTypes.LEGACY_MOUNT.get().create(level);
        if (mount != null) {
            mount.setOwner(owner);
            mount.moveTo(owner.getX(), owner.getY(), owner.getZ(), owner.getYRot(), owner.getXRot());
        }
        return mount;
    }

    @Override
    protected void defineSynchedData() {
        this.entityData.define(DATA_OWNER_UUID, Optional.empty());
        this.entityData.define(DATA_OWNER_NAME, "");
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        if (tag.hasUUID(OWNER_UUID_TAG)) {
            this.entityData.set(DATA_OWNER_UUID, Optional.of(tag.getUUID(OWNER_UUID_TAG)));
        } else {
            this.entityData.set(DATA_OWNER_UUID, Optional.empty());
        }
        this.entityData.set(DATA_OWNER_NAME, tag.getString(OWNER_NAME_TAG));
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        this.getOwnerUuid().ifPresent(uuid -> tag.putUUID(OWNER_UUID_TAG, uuid));
        if (!this.getOwnerName().isBlank()) {
            tag.putString(OWNER_NAME_TAG, this.getOwnerName());
        }
    }

    @Override
    public void tick() {
        super.tick();

        if (this.level().isClientSide()) {
            return;
        }

        ServerPlayer owner = this.getOwnerPlayer();
        if (owner == null || !owner.isAlive() || !MorphHelper.canUseMountHost(owner)) {
            this.ejectPassengers();
            this.discard();
            return;
        }

        if (this.tickCount % 10 == 0 && !owner.isPassengerOfSameVehicle(this)) {
            this.discard();
            return;
        }

        Entity rider = this.getControllingPassenger();
        if (!(rider instanceof Player player)) {
            this.setDeltaMovement(this.getDeltaMovement().multiply(0.6D, 0.0D, 0.6D));
            return;
        }

        this.setYRot(player.getYRot());
        this.setXRot(player.getXRot() * 0.25F);
        this.yRotO = this.getYRot();
        this.setRot(this.getYRot(), this.getXRot());

        Vec3 movement = resolveMovement(player);
        this.setDeltaMovement(movement);
        this.move(MoverType.SELF, movement);
        if (!this.level().noCollision(this, this.getBoundingBox())) {
            this.setDeltaMovement(Vec3.ZERO);
        }
    }

    @Override
    public boolean canAddPassenger(Entity passenger) {
        return this.getPassengers().isEmpty() && passenger instanceof Player player && this.isOwnedBy(player);
    }

    @Override
    protected boolean canRide(Entity entity) {
        return false;
    }

    @Override
    public double getPassengersRidingOffset() {
        return 0.1D;
    }

    protected boolean couldAcceptPassenger() {
        return this.getPassengers().isEmpty();
    }

    @Override
    public boolean isInvisible() {
        return true;
    }

    @Override
    public boolean isPickable() {
        return false;
    }

    @Override
    public boolean displayFireAnimation() {
        return false;
    }

    @Override
    public boolean canCollideWith(Entity entity) {
        return false;
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    protected void positionRider(Entity passenger, MoveFunction moveFunction) {
        moveFunction.accept(passenger, this.getX(), this.getY() + this.getPassengersRidingOffset(), this.getZ());
    }

    @Override
    public LivingEntity getControllingPassenger() {
        return this.getFirstPassenger() instanceof LivingEntity living ? living : null;
    }

    @Override
    public boolean shouldRiderSit() {
        return false;
    }

    @Override
    public boolean canChangeDimensions() {
        return false;
    }

    @Override
    public Pose getPose() {
        return Pose.STANDING;
    }

    public void setOwner(ServerPlayer owner) {
        this.entityData.set(DATA_OWNER_UUID, Optional.of(owner.getUUID()));
        this.entityData.set(DATA_OWNER_NAME, owner.getGameProfile().getName());
    }

    public Optional<UUID> getOwnerUuid() {
        return this.entityData.get(DATA_OWNER_UUID);
    }

    public String getOwnerName() {
        return this.entityData.get(DATA_OWNER_NAME);
    }

    public boolean isOwnedBy(Player player) {
        return this.getOwnerUuid().map(player.getUUID()::equals).orElse(false);
    }

    public @Nullable ServerPlayer getOwnerPlayer() {
        if (!(this.level() instanceof ServerLevel serverLevel)) {
            return null;
        }

        return this.getOwnerUuid()
                .map(serverLevel::getPlayerByUUID)
                .filter(ServerPlayer.class::isInstance)
                .map(ServerPlayer.class::cast)
                .orElse(null);
    }

    private static Vec3 resolveMovement(Player rider) {
        float strafe = rider.xxa * 0.8F;
        float forward = rider.zza;
        if (forward < 0.0F) {
            forward *= 0.3F;
        }

        double speed = rider.isSprinting() ? 0.42D : 0.31D;
        float yawRadians = rider.getYRot() * ((float) Math.PI / 180.0F);
        double sin = Mth.sin(yawRadians);
        double cos = Mth.cos(yawRadians);
        double moveX = (double) (strafe * cos - forward * sin) * speed;
        double moveZ = (double) (forward * cos + strafe * sin) * speed;
        return new Vec3(moveX, 0.0D, moveZ);
    }
}
