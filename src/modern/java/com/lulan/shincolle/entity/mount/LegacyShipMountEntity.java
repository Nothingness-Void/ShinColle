package com.lulan.shincolle.entity.mount;

import com.lulan.shincolle.entity.ship.LegacyShipBehaviorCatalog;
import com.lulan.shincolle.entity.ship.LegacyShipEntity;
import com.lulan.shincolle.entity.ship.ShipEntitySpec;
import com.lulan.shincolle.registry.ModEntityTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;

/** Modern vehicle counterpart of the audited legacy hime ship mounts. */
public final class LegacyShipMountEntity extends Entity {

    private static final String HOST_ID_TAG = "HostId";
    private static final String MOUNT_STYLE_TAG = "MountStyle";
    private static final EntityDataAccessor<Integer> DATA_HOST_ID =
            SynchedEntityData.defineId(LegacyShipMountEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_MOUNT_STYLE =
            SynchedEntityData.defineId(LegacyShipMountEntity.class, EntityDataSerializers.INT);

    private static final double TARGET_STOP_DISTANCE = 1.5D;
    private static final double COMMAND_STOP_DISTANCE = 0.75D;

    private int keyPressed;
    private int keyTick;

    public LegacyShipMountEntity(EntityType<? extends LegacyShipMountEntity> entityType, Level level) {
        super(entityType, level);
        this.noPhysics = true;
        this.setNoGravity(true);
        this.noCulling = true;
    }

    public static @Nullable LegacyShipMountEntity create(Level level, LegacyShipEntity host) {
        if (host == null) {
            return null;
        }

        LegacyShipBehaviorCatalog.ShipMountProfile profile =
                LegacyShipBehaviorCatalog.shipMountProfileFor(host.getSpec());
        if (profile == null) {
            return null;
        }

        LegacyShipMountEntity mount = ModEntityTypes.LEGACY_SHIP_MOUNT.get().create(level);
        if (mount == null) {
            return null;
        }

        mount.setHostId(host.getId());
        mount.setMountStyle(profile.style());
        mount.moveTo(host.getX(), host.getY(), host.getZ(), host.getYRot(), host.getXRot());
        mount.setDeltaMovement(Vec3.ZERO);
        return mount;
    }

    @Override
    protected void defineSynchedData() {
        this.entityData.define(DATA_HOST_ID, -1);
        this.entityData.define(DATA_MOUNT_STYLE, LegacyMountEntity.STYLE_BATTLESHIP);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        this.setHostId(tag.getInt(HOST_ID_TAG));
        this.setMountStyle(tag.contains(MOUNT_STYLE_TAG)
                ? tag.getInt(MOUNT_STYLE_TAG) : LegacyMountEntity.STYLE_BATTLESHIP);
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        tag.putInt(HOST_ID_TAG, this.getHostId());
        tag.putInt(MOUNT_STYLE_TAG, this.getMountStyle());
    }

    @Override
    public void tick() {
        super.tick();

        LegacyShipEntity host = this.getHostShip();
        if (this.level().isClientSide()) {
            this.spawnClientParticles();
            return;
        }

        if (host == null || !host.isAlive()
                || host.getVehicle() != this
                || !LegacyShipBehaviorCatalog.hasLegacyShipMounts(host.getSpec())
                || (host.getModelState() & 1) == 0) {
            if (host != null && host.getVehicle() == this) {
                host.stopRiding();
            }
            this.discard();
            return;
        }

        // BasicEntityMount delegates all ship state to its host.  Keep the host
        // navigator idle while this vehicle owns movement and attack timing.
        host.getShipNavigate().clearPathEntity();
        if (host.hasNoFuel() || host.isOrderedToSit() || LegacyShipEntity.isStopAi()) {
            this.setDeltaMovement(Vec3.ZERO);
            return;
        }

        Player steeringPlayer = this.resolveSteeringOwner(host);
        if (steeringPlayer != null && this.keyTick > 0) {
            this.keyTick--;
            this.setYRot(steeringPlayer.getYRot());
            this.yRotO = this.getYRot();
            double speed = Mth.clamp((0.4D + host.getLegacyStats().moveSpeed() * 0.2D)
                    * host.getCombatMovementSpeedModifier(), 0.2D, 0.8D);
            Vec3 movement = LegacyMountEntity.resolveKeyMovement(steeringPlayer, this.keyPressed, speed, true);
            if (movement.y == 0.0D) {
                double buoyancy = LegacyMountEntity.resolveWaterSurfaceBuoyancy(this);
                if (buoyancy > 0.0D) {
                    movement = movement.add(0.0D, buoyancy, 0.0D);
                }
            }
            this.setDeltaMovement(movement);
            this.move(MoverType.SELF, movement);
            return;
        }

        LivingEntity target = host.getTarget();
        if (target != null && target.isAlive() && host.canEngage(target)) {
            host.getLookControl().setLookAt(target, 30.0F, 30.0F);
            double range = Math.max(1.0D, host.getCompatAttackRange());
            if (this.distanceToSqr(target) > range * range) {
                this.moveToward(target.position().add(0.0D, target.getBbHeight() * 0.35D, 0.0D),
                        TARGET_STOP_DISTANCE, host);
            } else {
                if (target.getY() >= this.getY() - 0.5D) {
                    this.applyIdleWaterBuoyancy();
                } else {
                    this.setDeltaMovement(Vec3.ZERO);
                }
                if (host.getSensing().hasLineOfSight(target)) {
                    host.tryCompatCannonAttack(target);
                    if (this.usesAircraft()) {
                        host.tryCompatAirAttack(target);
                    }
                }
            }
            return;
        }

        Vec3 destination = this.resolveMovementTarget(host);
        if (destination == null) {
            this.applyIdleWaterBuoyancy();
            return;
        }

        this.moveToward(destination, COMMAND_STOP_DISTANCE, host);
    }

    private void applyIdleWaterBuoyancy() {
        double buoyancy = LegacyMountEntity.resolveWaterSurfaceBuoyancy(this);
        if (buoyancy > 0.0D) {
            Vec3 floatMotion = new Vec3(0.0D, buoyancy, 0.0D);
            this.setDeltaMovement(floatMotion);
            this.move(MoverType.SELF, floatMotion);
        } else {
            this.setDeltaMovement(Vec3.ZERO);
        }
    }

    private @Nullable Vec3 resolveMovementTarget(LegacyShipEntity host) {
        if (host.getGuardEntityUuid() != null) {
            Entity guarded = null;
            for (LivingEntity candidate : this.level().getEntitiesOfClass(
                    LivingEntity.class, this.getBoundingBox().inflate(64.0D))) {
                if (candidate.getUUID().equals(host.getGuardEntityUuid())) {
                    guarded = candidate;
                    break;
                }
            }
            if (guarded instanceof LivingEntity living && living.isAlive()) {
                return living.position();
            }
        }

        BlockPos position = host.getGuardedBlockPos();
        if (position == null) {
            position = host.getRouteNodePos();
        }
        if (position == null) {
            position = host.getCommandedPos();
        }
        if (position != null) {
            return Vec3.atCenterOf(position);
        }

        Player owner = host.getOwnerPlayer();
        if (owner != null && host.canFollow() && !host.hasActiveCommandState()) {
            return owner.position();
        }
        return null;
    }

    private void moveToward(Vec3 destination, double stopDistance, LegacyShipEntity host) {
        Vec3 offset = destination.subtract(this.position());
        double distance = offset.length();
        if (distance <= stopDistance) {
            this.setDeltaMovement(Vec3.ZERO);
            return;
        }

        double speed = (0.4D + host.getLegacyStats().moveSpeed() * 0.2D)
                * host.getCombatMovementSpeedModifier();
        speed = Mth.clamp(speed, 0.2D, 0.8D);
        Vec3 movement = offset.scale(Math.min(1.0D, speed / distance));
        this.setDeltaMovement(movement);
        this.setYRot((float) (Math.atan2(movement.x, movement.z) * (180.0D / Math.PI)));
        this.setXRot((float) (-Math.atan2(movement.y,
                Math.sqrt(movement.x * movement.x + movement.z * movement.z))
                * (180.0D / Math.PI)));
        this.yRotO = this.getYRot();
        this.xRotO = this.getXRot();
        this.move(MoverType.SELF, movement);
    }

    private void spawnClientParticles() {
        int style = this.getMountStyle();
        if (style == LegacyMountEntity.STYLE_AIRFIELD && (this.tickCount & 7) == 0) {
            this.spawnSmoke(-0.0D, 0.9D, -1.0D);
            this.spawnSmoke(-0.0D, 0.9D, -1.8D);
        } else if (style == LegacyMountEntity.STYLE_BATTLESHIP && (this.tickCount & 3) == 0) {
            this.level().addParticle(net.minecraft.core.particles.ParticleTypes.SMOKE,
                    this.getX(), this.getY() + 3.0D, this.getZ(), 0.0D, 0.03D, 0.0D);
        } else if (style == LegacyMountEntity.STYLE_HARBOUR && (this.tickCount & 15) == 0) {
            this.level().addParticle(net.minecraft.core.particles.ParticleTypes.CRIT,
                    this.getX(), this.getY() + 0.4D, this.getZ(), 0.0D, 0.05D, 0.0D);
        } else if (style == LegacyMountEntity.STYLE_ISOLATED && (this.tickCount & 7) == 0) {
            this.level().addParticle(net.minecraft.core.particles.ParticleTypes.DRIPPING_WATER,
                    this.getX() + 0.4D, this.getY() + 0.7D, this.getZ(), 0.0D, 0.0D, 0.0D);
        } else if (style == LegacyMountEntity.STYLE_MIDWAY && (this.tickCount & 7) == 0) {
            this.level().addParticle(net.minecraft.core.particles.ParticleTypes.DRIPPING_LAVA,
                    this.getX() + 0.8D, this.getY() + 0.85D, this.getZ(), 0.0D, 0.0D, 0.0D);
        }
    }

    private void spawnSmoke(double localX, double localY, double localZ) {
        double radians = this.getYRot() * (Math.PI / 180.0D);
        double worldZ = localZ * Math.cos(radians) + localX * Math.sin(radians);
        double worldX = localX * Math.cos(radians) - localZ * Math.sin(radians);
        this.level().addParticle(net.minecraft.core.particles.ParticleTypes.SMOKE,
                this.getX() + worldX, this.getY() + localY, this.getZ() + worldZ,
                0.0D, 0.1D, 0.0D);
    }

    private boolean usesAircraft() {
        LegacyShipBehaviorCatalog.ShipMountProfile profile =
                LegacyShipBehaviorCatalog.shipMountProfileFor(this.getHostShip() == null
                        ? null : this.getHostShip().getSpec());
        return profile != null && profile.usesAircraft();
    }

    @Override
    public boolean canAddPassenger(Entity passenger) {
        LegacyShipEntity host = this.getHostShip();
        if (passenger instanceof LegacyShipEntity ship) {
            return host != null && ship == host && this.getPassengers().stream()
                    .noneMatch(LegacyShipEntity.class::isInstance);
        }
        return passenger instanceof Player player
                && host != null
                && !host.isHostileVariant()
                && host.isOwnedBy(player)
                && this.getPassengers().stream().noneMatch(Player.class::isInstance);
    }

    @Override
    protected boolean canRide(Entity entity) {
        return false;
    }

    @Override
    protected void positionRider(Entity passenger, MoveFunction moveFunction) {
        if (passenger == null || passenger.getVehicle() != this) {
            return;
        }

        LegacyShipBehaviorCatalog.ShipMountProfile profile =
                LegacyShipBehaviorCatalog.shipMountProfileFor(this.getHostShip() == null
                        ? null : this.getHostShip().getSpec());
        if (profile == null) {
            super.positionRider(passenger, moveFunction);
            return;
        }

        double seatForward;
        double seatY;
        double seatLateral;
        if (passenger instanceof LegacyShipEntity) {
            seatForward = profile.hostX();
            seatY = profile.hostY();
            seatLateral = profile.hostZ();
            passenger.setYRot(this.getYRot());
            passenger.yRotO = this.yRotO;
            if (passenger instanceof LivingEntity living) {
                living.setYHeadRot(this.getYRot());
                living.yHeadRotO = this.yRotO;
                living.yBodyRot = this.getYRot();
                living.yBodyRotO = this.yRotO;
            }
        } else if (passenger instanceof Player) {
            seatForward = profile.playerX();
            seatY = profile.playerY();
            seatLateral = profile.playerZ();
        } else {
            seatForward = 0.0D;
            seatY = this.getPassengersRidingOffset();
            seatLateral = 0.0D;
        }

        double radians = this.getYRot() * (Math.PI / 180.0D);
        double cos = Math.cos(radians);
        double sin = Math.sin(radians);
        double worldZ = seatForward * cos + seatLateral * sin;
        double worldX = seatLateral * cos - seatForward * sin;
        moveFunction.accept(passenger,
                this.getX() + worldX,
                this.getY() + seatY + passenger.getMyRidingOffset(),
                this.getZ() + worldZ);
    }

    @Override
    public double getPassengersRidingOffset() {
        LegacyShipBehaviorCatalog.ShipMountProfile profile =
                LegacyShipBehaviorCatalog.shipMountProfileFor(this.getHostShip() == null
                        ? null : this.getHostShip().getSpec());
        return profile == null ? 0.0D : profile.height() * 0.5D;
    }

    @Override
    public EntityDimensions getDimensions(Pose pose) {
        LegacyShipBehaviorCatalog.ShipMountProfile profile =
                LegacyShipBehaviorCatalog.shipMountProfileFor(this.getHostShip() == null
                        ? null : this.getHostShip().getSpec());
        if (profile == null) {
            return super.getDimensions(pose);
        }
        return EntityDimensions.scalable(profile.width(), profile.height());
    }

    @Override
    public InteractionResult interact(Player player, InteractionHand hand) {
        LegacyShipEntity host = this.getHostShip();
        if (host == null || host.isHostileVariant() || !host.isOwnedBy(player)) {
            return InteractionResult.PASS;
        }
        if (this.level().isClientSide()) {
            return InteractionResult.SUCCESS;
        }
        if (player.isShiftKeyDown()) {
            return host.mobInteract(player, hand);
        }
        return player.startRiding(this, true)
                ? InteractionResult.CONSUME : InteractionResult.PASS;
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

    @Override
    public boolean canChangeDimensions() {
        return false;
    }

    public int getHostId() {
        return this.entityData.get(DATA_HOST_ID);
    }

    public int getMountStyle() {
        return this.entityData.get(DATA_MOUNT_STYLE);
    }

    public @Nullable LegacyShipEntity getHostShip() {
        Entity host = this.level().getEntity(this.getHostId());
        return host instanceof LegacyShipEntity ship ? ship : null;
    }

    public void applyLegacyMountMove(int keyMask) {
        this.keyPressed = keyMask;
        this.keyTick = keyMask != 0 ? 10 : 0;
    }

    public int getKeyPressed() {
        return this.keyPressed;
    }

    public int getKeyTick() {
        return this.keyTick;
    }

    private @Nullable Player resolveSteeringOwner(LegacyShipEntity host) {
        for (Entity passenger : this.getPassengers()) {
            if (passenger instanceof Player player && host.isOwnedBy(player)) {
                return player;
            }
        }
        return null;
    }

    private void setHostId(int hostId) {
        this.entityData.set(DATA_HOST_ID, hostId);
    }

    private void setMountStyle(int mountStyle) {
        this.entityData.set(DATA_MOUNT_STYLE, mountStyle);
        this.refreshDimensions();
    }
}
