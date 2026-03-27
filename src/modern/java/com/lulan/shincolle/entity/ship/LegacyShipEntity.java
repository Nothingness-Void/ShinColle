package com.lulan.shincolle.entity.ship;

import com.lulan.shincolle.entity.ship.goal.LegacyShipFollowOwnerGoal;
import com.lulan.shincolle.entity.ship.goal.LegacyShipOwnerHurtByTargetGoal;
import com.lulan.shincolle.entity.ship.goal.LegacyShipOwnerHurtTargetGoal;
import com.lulan.shincolle.entity.ship.goal.LegacyShipRangedAttackGoal;
import com.lulan.shincolle.entity.projectile.LegacyShipProjectileEntity;
import com.lulan.shincolle.item.CombatRationItem;
import com.lulan.shincolle.item.OwnerPaperItem;
import com.lulan.shincolle.item.PointerItem;
import com.lulan.shincolle.item.equipment.LegacyEquipmentItem;
import com.lulan.shincolle.menu.ShipInventoryMenu;
import com.lulan.shincolle.registry.ModItems;
import com.lulan.shincolle.registry.ModSoundEvents;
import com.lulan.shincolle.sound.ShipSoundType;
import com.lulan.shincolle.sound.ShinColleSoundHelper;
import com.lulan.shincolle.teitoku.TeitokuHelper;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.Container;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleContainer;
import net.minecraftforge.network.NetworkHooks;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public class LegacyShipEntity extends PathfinderMob {

    private static final String VARIANT_EGG_META_TAG = "VariantEggMeta";
    private static final String OWNER_UUID_TAG = "OwnerUuid";
    private static final String OWNER_NAME_TAG = "OwnerName";
    private static final String OWNER_UID_TAG = "OwnerUID";
    private static final String ORDERED_TO_SIT_TAG = "OrderedToSit";
    private static final String SHIP_LEVEL_TAG = "ShipLevel";
    private static final String SHIP_MORALE_TAG = "ShipMorale";
    private static final String SHIP_MARRIED_TAG = "ShipMarried";
    private static final String MODERN_HEALTH_TAG = "ModernHealth";
    private static final String MODERN_ATTACK_TAG = "ModernAttack";
    private static final String MODERN_SPEED_TAG = "ModernSpeed";
    private static final String MODERN_RANGE_TAG = "ModernRange";
    private static final String SHIP_INVENTORY_TAG = "ShipInventory";
    private static final String SHIP_INVENTORY_SLOT_TAG = "Slot";

    private static final int DEFAULT_LEVEL = 1;
    private static final int DEFAULT_MORALE = 1600;
    private static final int MAX_MORALE = 16000;
    private static final int MAX_MODERN_TOTAL_STEPS = 24;
    private static final int MAX_MODERN_STAT_STEPS = 12;

    private static final EntityDataAccessor<Integer> DATA_VARIANT_EGG_META =
            SynchedEntityData.defineId(LegacyShipEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Optional<UUID>> DATA_OWNER_UUID =
            SynchedEntityData.defineId(LegacyShipEntity.class, EntityDataSerializers.OPTIONAL_UUID);
    private static final EntityDataAccessor<String> DATA_OWNER_NAME =
            SynchedEntityData.defineId(LegacyShipEntity.class, EntityDataSerializers.STRING);
    private static final EntityDataAccessor<Boolean> DATA_ORDERED_TO_SIT =
            SynchedEntityData.defineId(LegacyShipEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> DATA_LEVEL =
            SynchedEntityData.defineId(LegacyShipEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_MORALE =
            SynchedEntityData.defineId(LegacyShipEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> DATA_MARRIED =
            SynchedEntityData.defineId(LegacyShipEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> DATA_MODERNIZATION_TOTAL =
            SynchedEntityData.defineId(LegacyShipEntity.class, EntityDataSerializers.INT);

    public static final int EQUIPMENT_SLOT_COUNT = 6;
    public static final int CARGO_SLOT_COUNT = 18;
    public static final int SHIP_SLOT_COUNT = EQUIPMENT_SLOT_COUNT + CARGO_SLOT_COUNT;

    private final SimpleContainer shipInventory = new SimpleContainer(SHIP_SLOT_COUNT) {
        @Override
        public void setChanged() {
            super.setChanged();
            LegacyShipEntity.this.setPersistenceRequired();
            LegacyShipEntity.this.refreshEquipmentProfile(true);
        }
    };

    private ShipEquipmentProfile equipmentProfile = ShipEquipmentProfile.EMPTY;
    private LegacyShipAttackProfile attackProfile = LegacyShipAttackProfile.MELEE_ONLY;
    private LegacyShipStats legacyStats = LegacyShipStats.create(
            ShipEntitySpecs.DEFAULT.legacyClassId(),
            ShipEntitySpecs.DEFAULT.archetype(),
            false,
            DEFAULT_LEVEL,
            DEFAULT_MORALE,
            0, 0, 0, 0,
            ShipEquipmentProfile.EMPTY,
            List.of());
    private int modernHealthSteps;
    private int modernAttackSteps;
    private int modernSpeedSteps;
    private int modernRangeSteps;
    private int meleeAttackCooldown;
    private int lightAttackCooldown;
    private int heavyAttackCooldown;
    private int airAttackCooldown;
    private int cachedEffectSignature;
    private boolean nextAirAttackLight = true;
    private int ownerUid;

    public LegacyShipEntity(EntityType<? extends LegacyShipEntity> entityType, Level level) {
        super(entityType, level);
        this.setPersistenceRequired();
        this.setCanPickUpLoot(false);
    }

    public static AttributeSupplier.Builder createAttributes() {
        ShipArchetype defaults = ShipEntitySpecs.DEFAULT.archetype();
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, defaults.maxHealth())
                .add(Attributes.MOVEMENT_SPEED, defaults.movementSpeed())
                .add(Attributes.ATTACK_DAMAGE, defaults.attackDamage())
                .add(Attributes.FOLLOW_RANGE, defaults.followRange())
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.1D);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_VARIANT_EGG_META, ShipEntitySpecs.DEFAULT.eggMeta());
        this.entityData.define(DATA_OWNER_UUID, Optional.empty());
        this.entityData.define(DATA_OWNER_NAME, "");
        this.entityData.define(DATA_ORDERED_TO_SIT, false);
        this.entityData.define(DATA_LEVEL, DEFAULT_LEVEL);
        this.entityData.define(DATA_MORALE, DEFAULT_MORALE);
        this.entityData.define(DATA_MARRIED, false);
        this.entityData.define(DATA_MODERNIZATION_TOTAL, 0);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new LegacyShipRangedAttackGoal(this, 1.0D));
        this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.0D, true) {
            @Override
            public boolean canUse() {
                return LegacyShipEntity.this.canExecuteActiveGoal() && super.canUse();
            }

            @Override
            public boolean canContinueToUse() {
                return LegacyShipEntity.this.canExecuteActiveGoal() && super.canContinueToUse();
            }

            @Override
            protected double getAttackReachSqr(LivingEntity target) {
                double reach = switch (LegacyShipEntity.this.getSpec().archetype()) {
                    case DESTROYER, SUBMARINE -> 2.2D;
                    case CRUISER, TRANSPORT -> 2.6D;
                    case BATTLESHIP, CARRIER -> 2.9D;
                    case PRINCESS -> 3.2D;
                    case INSTALLATION -> 3.6D;
                };

                return reach * reach + target.getBbWidth();
            }
        });
        this.goalSelector.addGoal(3, new LegacyShipFollowOwnerGoal(this, 1.1D, 5.0F, 2.0F, 14.0F));
        this.goalSelector.addGoal(4, new RandomStrollGoal(this, 0.9D) {
            @Override
            public boolean canUse() {
                return !LegacyShipEntity.this.isOrderedToSit() && super.canUse();
            }

            @Override
            public boolean canContinueToUse() {
                return !LegacyShipEntity.this.isOrderedToSit() && super.canContinueToUse();
            }
        });
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(7, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(0, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(1, new LegacyShipOwnerHurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new LegacyShipOwnerHurtTargetGoal(this));
        this.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, Player.class, true) {
            @Override
            public boolean canUse() {
                return LegacyShipEntity.this.isHostileVariant() && LegacyShipEntity.this.canEngage(this.target);
            }

            @Override
            public boolean canContinueToUse() {
                return LegacyShipEntity.this.isHostileVariant() && super.canContinueToUse();
            }
        });
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, LegacyShipEntity.class, 5, true, false,
                target -> target != null && LegacyShipEntity.this.canEngage(target)) {
            @Override
            public boolean canUse() {
                return LegacyShipEntity.this.canExecuteActiveGoal() && super.canUse();
            }

            @Override
            public boolean canContinueToUse() {
                return LegacyShipEntity.this.canExecuteActiveGoal() && super.canContinueToUse();
            }
        });
        this.targetSelector.addGoal(4, new NearestAttackableTargetGoal<>(this, Mob.class, 8, true, false,
                target -> target != null && LegacyShipEntity.this.canAutoTargetEnemyMob(target)) {
            @Override
            public boolean canUse() {
                return LegacyShipEntity.this.canExecuteActiveGoal()
                        && !LegacyShipEntity.this.isHostileVariant()
                        && super.canUse();
            }

            @Override
            public boolean canContinueToUse() {
                return LegacyShipEntity.this.canExecuteActiveGoal()
                        && !LegacyShipEntity.this.isHostileVariant()
                        && super.canContinueToUse();
            }
        });
    }

    @Override
    public @Nullable SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty,
                                                  MobSpawnType spawnType, @Nullable SpawnGroupData spawnData,
                                                  @Nullable CompoundTag dataTag) {
        SpawnGroupData result = super.finalizeSpawn(level, difficulty, spawnType, spawnData, dataTag);
        this.refreshFromVariant(false);
        return result;
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.setVariantEggMeta(tag.getInt(VARIANT_EGG_META_TAG));
        this.setOrderedToSit(tag.getBoolean(ORDERED_TO_SIT_TAG));

        if (tag.hasUUID(OWNER_UUID_TAG)) {
            this.entityData.set(DATA_OWNER_UUID, Optional.of(tag.getUUID(OWNER_UUID_TAG)));
        } else {
            this.entityData.set(DATA_OWNER_UUID, Optional.empty());
        }

        this.entityData.set(DATA_OWNER_NAME, tag.getString(OWNER_NAME_TAG));
        this.ownerUid = Math.max(0, tag.getInt(OWNER_UID_TAG));
        this.entityData.set(DATA_LEVEL, Math.max(DEFAULT_LEVEL, tag.getInt(SHIP_LEVEL_TAG)));
        this.entityData.set(DATA_MORALE, Mth.clamp(tag.getInt(SHIP_MORALE_TAG), 0, MAX_MORALE));
        this.entityData.set(DATA_MARRIED, tag.getBoolean(SHIP_MARRIED_TAG));
        this.modernHealthSteps = Mth.clamp(tag.getInt(MODERN_HEALTH_TAG), 0, MAX_MODERN_STAT_STEPS);
        this.modernAttackSteps = Mth.clamp(tag.getInt(MODERN_ATTACK_TAG), 0, MAX_MODERN_STAT_STEPS);
        this.modernSpeedSteps = Mth.clamp(tag.getInt(MODERN_SPEED_TAG), 0, MAX_MODERN_STAT_STEPS);
        this.modernRangeSteps = Mth.clamp(tag.getInt(MODERN_RANGE_TAG), 0, MAX_MODERN_STAT_STEPS);
        this.entityData.set(DATA_MODERNIZATION_TOTAL, this.getModernizationCount());
        this.loadShipInventory(tag);
        this.refreshFromVariant(true);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt(VARIANT_EGG_META_TAG, this.getVariantEggMeta());
        tag.putBoolean(ORDERED_TO_SIT_TAG, this.isOrderedToSit());
        this.getOwnerUuid().ifPresent(uuid -> tag.putUUID(OWNER_UUID_TAG, uuid));

        if (!this.getOwnerName().isBlank()) {
            tag.putString(OWNER_NAME_TAG, this.getOwnerName());
        }
        if (this.ownerUid > 0) {
            tag.putInt(OWNER_UID_TAG, this.ownerUid);
        }

        tag.putInt(SHIP_LEVEL_TAG, this.getShipLevel());
        tag.putInt(SHIP_MORALE_TAG, this.getMorale());
        tag.putBoolean(SHIP_MARRIED_TAG, this.isMarried());
        tag.putInt(MODERN_HEALTH_TAG, this.modernHealthSteps);
        tag.putInt(MODERN_ATTACK_TAG, this.modernAttackSteps);
        tag.putInt(MODERN_SPEED_TAG, this.modernSpeedSteps);
        tag.putInt(MODERN_RANGE_TAG, this.modernRangeSteps);
        this.saveShipInventory(tag);
    }

    @Override
    public void tick() {
        super.tick();

        if (this.meleeAttackCooldown > 0) {
            this.meleeAttackCooldown--;
        }
        if (this.lightAttackCooldown > 0) {
            this.lightAttackCooldown--;
        }
        if (this.heavyAttackCooldown > 0) {
            this.heavyAttackCooldown--;
        }
        if (this.airAttackCooldown > 0) {
            this.airAttackCooldown--;
        }

        int effectSignature = this.computeActiveEffectSignature();
        if (effectSignature != this.cachedEffectSignature) {
            this.cachedEffectSignature = effectSignature;
            this.refreshFromVariant(true);
        }

        if (this.isOrderedToSit()) {
            this.getNavigation().stop();
            this.setTarget(null);
        }
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> key) {
        super.onSyncedDataUpdated(key);

        if (DATA_VARIANT_EGG_META.equals(key)
                || DATA_LEVEL.equals(key)
                || DATA_MORALE.equals(key)
                || DATA_MARRIED.equals(key)
                || DATA_MODERNIZATION_TOTAL.equals(key)) {
            this.refreshFromVariant(true);
        }
    }

    @Override
    public EntityDimensions getDimensions(Pose pose) {
        return this.getSpec().archetype().dimensions();
    }

    @Override
    public boolean canBreatheUnderwater() {
        return true;
    }

    @Override
    protected int decreaseAirSupply(int air) {
        return air;
    }

    @Override
    public boolean removeWhenFarAway(double distanceToClosestPlayer) {
        return false;
    }

    @Override
    public Component getName() {
        return this.hasCustomName() ? Objects.requireNonNull(this.getCustomName()) : this.getSpec().displayName();
    }

    @Override
    public boolean isAlliedTo(Entity entity) {
        if (super.isAlliedTo(entity)) {
            return true;
        }

        if (this.isHostileVariant()) {
            return false;
        }

        Optional<UUID> ownerUuid = this.getOwnerUuid();

        if (ownerUuid.isEmpty()) {
            return false;
        }

        if (entity instanceof Player player) {
            return ownerUuid.get().equals(player.getUUID());
        }

        if (entity instanceof LegacyShipEntity otherShip) {
            return !otherShip.isHostileVariant() && ownerUuid.equals(otherShip.getOwnerUuid());
        }

        return false;
    }

    @Override
    protected @Nullable SoundEvent getAmbientSound() {
        return ShinColleSoundHelper.getShipVoice(ShipSoundType.IDLE, this.getShipClassId());
    }

    @Override
    protected @Nullable SoundEvent getHurtSound(DamageSource damageSource) {
        return ShinColleSoundHelper.getShipVoice(ShipSoundType.HURT, this.getShipClassId());
    }

    @Override
    protected @Nullable SoundEvent getDeathSound() {
        return ShinColleSoundHelper.getShipVoice(ShipSoundType.DEAD, this.getShipClassId());
    }

    @Override
    protected float getSoundVolume() {
        return 0.55F;
    }

    @Override
    public boolean doHurtTarget(Entity target) {
        if (!(target instanceof LivingEntity livingTarget) || this.meleeAttackCooldown > 0) {
            return false;
        }

        LegacyShipCombatHelper.AttackRoll roll = LegacyShipCombatHelper.rollAttack(this, livingTarget, LegacyShipAttackKind.MELEE);
        this.meleeAttackCooldown = LegacyShipCombatHelper.attackDelay(this.legacyStats, LegacyShipAttackKind.MELEE);

        if (roll.miss()) {
            this.setLastHurtMob(target);
            return true;
        }

        boolean attacked = target.hurt(this.damageSources().mobAttack(this), roll.damage());
        if (attacked) {
            this.setLastHurtMob(target);
        }

        return attacked;
    }

    @Override
    public boolean hurt(DamageSource damageSource, float amount) {
        Entity attacker = damageSource.getDirectEntity() != null ? damageSource.getDirectEntity() : damageSource.getEntity();

        if (!this.level().isClientSide()
                && attacker != null
                && attacker != this
                && LegacyShipCombatHelper.canDodge(this, attacker)) {
            return false;
        }

        amount = LegacyShipCombatHelper.applyDefenseReduction(this.random, this.legacyStats, amount);

        if (!this.level().isClientSide()
                && !this.isHostileVariant()
                && amount >= this.getHealth()
                && this.consumeFirstMatchingItem(ModItems.REPAIRGODDESS.get())) {
            this.setHealth(Math.max(1.0F, this.getMaxHealth() * 0.4F));
            this.removeAllEffects();
            this.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 20 * 12, 1));
            this.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 20 * 20, 1));
            this.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 20 * 20, 0));
            this.level().broadcastEntityEvent(this, (byte) 35);

            Player owner = this.getOwnerPlayer();
            if (owner != null) {
                owner.displayClientMessage(Component.translatable("chat.shincolle.ship.repairgoddess.triggered",
                        this.getName().copy().withStyle(ChatFormatting.LIGHT_PURPLE)), true);
            }

            return false;
        }

        return super.hurt(damageSource, amount);
    }

    @Override
    public void heal(float amount) {
        super.heal(amount * this.legacyStats.healRate());
    }

    @Override
    protected void dropCustomDeathLoot(DamageSource damageSource, int looting, boolean recentlyHit) {
        super.dropCustomDeathLoot(damageSource, looting, recentlyHit);

        if (!this.level().isClientSide()) {
            this.dropShipInventoryContents();
        }
    }

    @Override
    public float getVoicePitch() {
        return 1.0F + (this.random.nextFloat() - this.random.nextFloat()) * 0.05F;
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!stack.isEmpty()) {
            InteractionResult itemResult = this.handleShipItemInteraction(player, hand, stack);
            return itemResult.consumesAction() ? itemResult : super.mobInteract(player, hand);
        }

        if (this.level().isClientSide()) {
            return InteractionResult.SUCCESS;
        }

        if (this.isHostileVariant()) {
            player.displayClientMessage(Component.translatable("chat.shincolle.entity.hostile",
                    this.getName().copy().withStyle(ChatFormatting.RED)), true);
            return InteractionResult.CONSUME;
        }

        if (this.getOwnerUuid().isEmpty()) {
            this.setOwner(player);
            ShinColleSoundHelper.playShipVoice(this.level(), player, ShipSoundType.PICKITEM, 0.65F,
                    ShinColleSoundHelper.variedPitch(player, 1.0F, 0.08F));
            player.displayClientMessage(Component.translatable("chat.shincolle.entity.claimed",
                    this.getName().copy().withStyle(ChatFormatting.AQUA)), true);
            return InteractionResult.CONSUME;
        }

        if (this.isOwnedBy(player) && player.isShiftKeyDown()) {
            if (player instanceof ServerPlayer serverPlayer) {
                ShinColleSoundHelper.playForPlayer(this.level(), player, ModSoundEvents.SHIP_BELL.get(), 0.65F,
                        ShinColleSoundHelper.variedPitch(player, 1.0F, 0.08F));
                this.openInventory(serverPlayer);
            }
            return InteractionResult.CONSUME;
        }

        if (this.isOwnedBy(player)) {
            this.setOrderedToSit(!this.isOrderedToSit());
            ShinColleSoundHelper.playShipVoice(this.level(), player, ShipSoundType.IDLE, 0.55F,
                    ShinColleSoundHelper.variedPitch(player, 1.0F, 0.08F));
            player.displayClientMessage(Component.translatable("chat.shincolle.entity.status",
                    this.getName().copy().withStyle(ChatFormatting.AQUA), this.getEscortModeLabel()), true);
            return InteractionResult.CONSUME;
        }

        this.displayOwnerLocked(player);
        return InteractionResult.CONSUME;
    }

    public void applySpawnSpec(ShipEntitySpec spec, @Nullable Player owner) {
        this.setVariantEggMeta(spec.eggMeta());
        this.entityData.set(DATA_LEVEL, DEFAULT_LEVEL);
        this.entityData.set(DATA_MORALE, DEFAULT_MORALE);
        this.entityData.set(DATA_MARRIED, false);
        this.modernHealthSteps = 0;
        this.modernAttackSteps = 0;
        this.modernSpeedSteps = 0;
        this.modernRangeSteps = 0;
        this.entityData.set(DATA_MODERNIZATION_TOTAL, 0);

        if (spec.hostile()) {
            this.clearOwner();
            this.setOrderedToSit(false);
        } else if (owner != null) {
            this.setOwner(owner);
        }

        this.refreshFromVariant(false);
    }

    public ShipEntitySpec getSpec() {
        return ShipEntitySpecs.getByEggMeta(this.getVariantEggMeta());
    }

    public int getVariantEggMeta() {
        return this.entityData.get(DATA_VARIANT_EGG_META);
    }

    public void setVariantEggMeta(int eggMeta) {
        this.entityData.set(DATA_VARIANT_EGG_META, ShipEntitySpecs.getByEggMeta(eggMeta).eggMeta());
        this.refreshFromVariant(false);
    }

    public int getShipClassId() {
        return this.getSpec().legacyClassId();
    }

    public boolean isHostileVariant() {
        return this.getSpec().hostile();
    }

    public boolean isOrderedToSit() {
        return this.entityData.get(DATA_ORDERED_TO_SIT);
    }

    public void setOrderedToSit(boolean orderedToSit) {
        this.entityData.set(DATA_ORDERED_TO_SIT, orderedToSit);

        if (orderedToSit) {
            this.getNavigation().stop();
        }
    }

    public int getShipLevel() {
        return this.entityData.get(DATA_LEVEL);
    }

    public void setShipLevel(int targetLevel) {
        int clampedLevel = Mth.clamp(targetLevel, DEFAULT_LEVEL, this.getLevelCap());
        this.entityData.set(DATA_LEVEL, clampedLevel);
        this.refreshFromVariant(true);
    }

    public int getLevelCap() {
        return this.isMarried() ? 150 : 100;
    }

    public int getMorale() {
        return this.entityData.get(DATA_MORALE);
    }

    public void setMorale(int morale) {
        this.entityData.set(DATA_MORALE, Mth.clamp(morale, 0, MAX_MORALE));
        this.refreshFromVariant(true);
    }

    public void addMorale(int amount) {
        this.setMorale(this.getMorale() + amount);
    }

    public boolean isMarried() {
        return this.entityData.get(DATA_MARRIED);
    }

    public void setMarried(boolean married) {
        this.entityData.set(DATA_MARRIED, married);

        if (!married && this.getShipLevel() > 100) {
            this.entityData.set(DATA_LEVEL, 100);
        }

        this.refreshFromVariant(true);
    }

    public int getModernizationCount() {
        return this.modernHealthSteps + this.modernAttackSteps + this.modernSpeedSteps + this.modernRangeSteps;
    }

    public int getModernizationDisplayCount() {
        return this.entityData.get(DATA_MODERNIZATION_TOTAL);
    }

    public int getMoraleTier() {
        int morale = this.getMorale();
        if (morale > LegacyShipStatTables.MORALE_EXCITED_LOWER) {
            return 4;
        }
        if (morale > LegacyShipStatTables.MORALE_HAPPY_LOWER) {
            return 3;
        }
        if (morale > LegacyShipStatTables.MORALE_NORMAL_LOWER) {
            return 2;
        }
        if (morale > LegacyShipStatTables.MORALE_TIRED_LOWER) {
            return 1;
        }
        return 0;
    }

    public Optional<UUID> getOwnerUuid() {
        return this.entityData.get(DATA_OWNER_UUID);
    }

    public int getOwnerUid() {
        return this.ownerUid;
    }

    public String getOwnerName() {
        return this.entityData.get(DATA_OWNER_NAME);
    }

    public boolean isOwnedBy(Player player) {
        return this.getOwnerUuid().map(player.getUUID()::equals).orElse(false);
    }

    public Container getShipInventory() {
        return this.shipInventory;
    }

    public ShipEquipmentProfile getEquipmentProfile() {
        return this.equipmentProfile;
    }

    public LegacyShipStats getLegacyStats() {
        return this.legacyStats;
    }

    public LegacyShipAttackProfile getAttackProfile() {
        return this.attackProfile;
    }

    public boolean isEquipmentSlot(int slot) {
        return slot >= 0 && slot < EQUIPMENT_SLOT_COUNT;
    }

    public boolean canEquip(ItemStack stack) {
        return ShipEquipmentProfile.canEquip(this.getSpec().archetype(), stack);
    }

    public Component getEscortModeLabel() {
        return Component.translatable(this.isOrderedToSit()
                ? "gui.shincolle.entity.mode.standby"
                : "gui.shincolle.entity.mode.follow");
    }

    public @Nullable Player getOwnerPlayer() {
        if (!(this.level() instanceof ServerLevel serverLevel)) {
            return null;
        }

        return this.getOwnerUuid().map(serverLevel::getPlayerByUUID).orElse(null);
    }

    public boolean canCommanderEdit(Player player) {
        return !this.isHostileVariant() && this.isOwnedBy(player);
    }

    public boolean canExecuteCombatGoal() {
        return this.canExecuteActiveGoal();
    }

    public boolean canUseCompatRangedCombat() {
        return this.attackProfile.hasRangedAttack();
    }

    public int getCompatAttackAimTime() {
        int shipLevel = Math.min(150, this.getShipLevel());
        return Mth.clamp((int) (20F * (150 - shipLevel) / 150F) + 10, 8, 30);
    }

    public float getCompatAttackRange() {
        return Math.max(3.0F, this.legacyStats.attackRange());
    }

    public boolean tryCompatCannonAttack(LivingEntity target) {
        boolean attacked = false;

        if (this.attackProfile.light() && this.lightAttackCooldown <= 0) {
            attacked |= this.performCompatAttack(target, LegacyShipAttackKind.LIGHT);
        }

        if (this.attackProfile.heavy() && this.heavyAttackCooldown <= 0) {
            attacked |= this.performCompatAttack(target, LegacyShipAttackKind.HEAVY);
        }

        return attacked;
    }

    public boolean tryCompatAirAttack(LivingEntity target) {
        if (!this.attackProfile.hasAirAttack() || this.airAttackCooldown > 0) {
            return false;
        }

        LegacyShipAttackKind preferred = this.nextAirAttackLight ? LegacyShipAttackKind.AIR_LIGHT : LegacyShipAttackKind.AIR_HEAVY;
        LegacyShipAttackKind fallback = this.nextAirAttackLight ? LegacyShipAttackKind.AIR_HEAVY : LegacyShipAttackKind.AIR_LIGHT;

        if (this.canUseCompatAttack(preferred) && this.performCompatAttack(target, preferred)) {
            this.nextAirAttackLight = !this.nextAirAttackLight;
            return true;
        }

        if (this.canUseCompatAttack(fallback) && this.performCompatAttack(target, fallback)) {
            this.nextAirAttackLight = !this.nextAirAttackLight;
            return true;
        }

        return false;
    }

    public boolean canEngage(@Nullable LivingEntity target) {
        if (target == null || !target.isAlive() || target == this || this.isAlliedTo(target)) {
            return false;
        }

        Player owner = this.getOwnerPlayer();

        if (target instanceof Player player) {
            return this.isHostileVariant() || !this.isOwnedBy(player);
        }

        if (target instanceof LegacyShipEntity otherShip) {
            return this.isHostileVariant() != otherShip.isHostileVariant();
        }

        if (this.isHostileVariant()) {
            return true;
        }

        if (target instanceof Enemy) {
            return true;
        }

        if (owner != null) {
            if (target == owner.getLastHurtMob() || target == owner.getLastHurtByMob()) {
                return true;
            }

            if (owner.equals(target.getLastHurtByMob())) {
                return true;
            }

            if (target instanceof Mob mob && mob.getTarget() == owner) {
                return true;
            }
        }

        return false;
    }

    public boolean canAutoTargetEnemyMob(@Nullable LivingEntity target) {
        if (!(target instanceof Mob mob) || !target.isAlive() || !(target instanceof Enemy) || !this.canEngage(target)) {
            return false;
        }

        double followRange = Math.max(16.0D, this.getAttributeValue(Attributes.FOLLOW_RANGE));
        double shipRangeSqr = followRange * followRange;
        if (this.distanceToSqr(mob) <= shipRangeSqr) {
            return true;
        }

        Player owner = this.getOwnerPlayer();
        return owner != null && owner.distanceToSqr(mob) <= shipRangeSqr;
    }

    public void setOwner(Player player) {
        this.entityData.set(DATA_OWNER_UUID, Optional.of(player.getUUID()));
        this.entityData.set(DATA_OWNER_NAME, player.getGameProfile().getName());
        this.ownerUid = Math.max(0, TeitokuHelper.getPlayerUid(player));
    }

    public void setOwner(UUID ownerUuid, String ownerName) {
        int resolvedOwnerUid = 0;
        if (!this.level().isClientSide() && this.level() instanceof ServerLevel serverLevel) {
            resolvedOwnerUid = TeitokuHelper.resolvePlayerUid(serverLevel, ownerUuid, ownerName);
        }
        this.setOwner(ownerUuid, ownerName, resolvedOwnerUid);
    }

    public void setOwner(UUID ownerUuid, String ownerName, int ownerUid) {
        this.entityData.set(DATA_OWNER_UUID, Optional.of(ownerUuid));
        this.entityData.set(DATA_OWNER_NAME, ownerName);
        this.ownerUid = Math.max(0, ownerUid);
    }

    public void clearOwner() {
        this.entityData.set(DATA_OWNER_UUID, Optional.empty());
        this.entityData.set(DATA_OWNER_NAME, "");
        this.ownerUid = 0;
    }

    public boolean addRandomModernization() {
        if (this.getModernizationCount() >= MAX_MODERN_TOTAL_STEPS) {
            return false;
        }

        List<Integer> available = new ArrayList<>(4);
        if (this.modernHealthSteps < MAX_MODERN_STAT_STEPS) {
            available.add(0);
        }
        if (this.modernAttackSteps < MAX_MODERN_STAT_STEPS) {
            available.add(1);
        }
        if (this.modernSpeedSteps < MAX_MODERN_STAT_STEPS) {
            available.add(2);
        }
        if (this.modernRangeSteps < MAX_MODERN_STAT_STEPS) {
            available.add(3);
        }

        if (available.isEmpty()) {
            return false;
        }

        switch (available.get(this.random.nextInt(available.size()))) {
            case 0 -> this.modernHealthSteps++;
            case 1 -> this.modernAttackSteps++;
            case 2 -> this.modernSpeedSteps++;
            default -> this.modernRangeSteps++;
        }

        this.entityData.set(DATA_MODERNIZATION_TOTAL, this.getModernizationCount());
        this.refreshFromVariant(true);
        return true;
    }

    public int getRescueItemCount() {
        return this.countItemInInventory(ModItems.REPAIRGODDESS.get());
    }

    public boolean storeSingleItem(ItemStack sourceStack) {
        if (sourceStack.isEmpty()) {
            return false;
        }

        for (int slot = EQUIPMENT_SLOT_COUNT; slot < SHIP_SLOT_COUNT; slot++) {
            ItemStack current = this.shipInventory.getItem(slot);
            if (current.isEmpty()) {
                ItemStack inserted = sourceStack.copy();
                inserted.setCount(1);
                this.shipInventory.setItem(slot, inserted);
                return true;
            }

            if (ItemStack.isSameItemSameTags(current, sourceStack) && current.getCount() < current.getMaxStackSize()) {
                current.grow(1);
                this.shipInventory.setItem(slot, current);
                return true;
            }
        }

        return false;
    }

    public boolean performKaitai(Player player, ItemStack hammer) {
        if (!this.canCommanderEdit(player) || this.level().isClientSide()) {
            return false;
        }

        if (!player.getAbilities().instabuild) {
            hammer.hurtAndBreak(1, player, p -> p.broadcastBreakEvent(InteractionHand.MAIN_HAND));
        }

        this.dropShipInventoryContents();
        this.spawnAtLocation(new ItemStack(ModItems.GRUDGE.get(), 1 + this.random.nextInt(3)));
        this.spawnAtLocation(new ItemStack(ModItems.ABYSSMETAL.get(), 1 + this.random.nextInt(2)));
        this.playSound(ModSoundEvents.SHIP_KAITAI.get(), 0.8F, this.getVoicePitch());
        this.discard();
        return true;
    }

    private void refreshFromVariant(boolean preserveHealth) {
        ShipEntitySpec spec = this.getSpec();
        this.attackProfile = LegacyShipAttackProfile.resolve(spec);
        this.legacyStats = LegacyShipStats.create(
                spec.legacyClassId(),
                spec.archetype(),
                spec.hostile(),
                this.getShipLevel(),
                this.getMorale(),
                this.modernHealthSteps,
                this.modernAttackSteps,
                this.modernSpeedSteps,
                this.modernRangeSteps,
                this.equipmentProfile,
                this.getActiveEffects());
        this.cachedEffectSignature = this.computeActiveEffectSignature();

        this.refreshDimensions();
        this.applyBaseValue(Attributes.MAX_HEALTH, this.legacyStats.get(LegacyShipStatTables.Attr.HP));
        this.applyBaseValue(Attributes.MOVEMENT_SPEED, this.legacyStats.moveSpeed());
        this.applyBaseValue(Attributes.ATTACK_DAMAGE, this.legacyStats.meleeDamage());
        this.applyBaseValue(Attributes.FOLLOW_RANGE, 64D);
        this.applyBaseValue(Attributes.KNOCKBACK_RESISTANCE, this.legacyStats.knockbackResistance());

        float maxHealth = this.getMaxHealth();
        float currentHealth = preserveHealth ? Mth.clamp(this.getHealth(), 1.0F, maxHealth) : maxHealth;
        this.setHealth(currentHealth);
    }

    private void refreshEquipmentProfile(boolean preserveHealth) {
        this.equipmentProfile = ShipEquipmentProfile.fromInventory(this.shipInventory, this.getSpec().archetype());
        this.refreshFromVariant(preserveHealth);
    }

    private boolean canUseCompatAttack(LegacyShipAttackKind attackKind) {
        return switch (attackKind) {
            case MELEE -> this.attackProfile.melee();
            case LIGHT -> this.attackProfile.light();
            case HEAVY -> this.attackProfile.heavy();
            case AIR_LIGHT -> this.attackProfile.airLight();
            case AIR_HEAVY -> this.attackProfile.airHeavy();
        };
    }

    private boolean performCompatAttack(LivingEntity target, LegacyShipAttackKind attackKind) {
        if (!this.canEngage(target) || !this.canUseCompatAttack(attackKind)) {
            return false;
        }

        LegacyShipCombatHelper.AttackRoll roll = LegacyShipCombatHelper.rollAttack(this, target, attackKind);
        int delay = LegacyShipCombatHelper.attackDelay(this.legacyStats, attackKind);

        switch (attackKind) {
            case LIGHT -> this.lightAttackCooldown = delay;
            case HEAVY -> this.heavyAttackCooldown = delay;
            case AIR_LIGHT, AIR_HEAVY -> this.airAttackCooldown = delay;
            case MELEE -> this.meleeAttackCooldown = delay;
        }

        if (roll.miss()) {
            if (!attackKind.justLaunch()) {
                this.setLastHurtMob(target);
                return true;
            }
        }

        if (attackKind.justLaunch()) {
            if (!this.level().isClientSide()) {
                this.level().addFreshEntity(LegacyShipProjectileEntity.create(this.level(), this, target, attackKind, roll.damage(), roll.miss()));
            }
            this.setLastHurtMob(target);
            return true;
        }

        boolean attacked = target.hurt(this.damageSources().mobAttack(this), roll.damage());
        if (attacked) {
            this.setLastHurtMob(target);
        }

        return attacked;
    }

    private void applyBaseValue(Attribute attribute, double value) {
        AttributeInstance instance = this.getAttribute(attribute);

        if (instance != null) {
            instance.setBaseValue(value);
        }
    }

    private int computeActiveEffectSignature() {
        int signature = 1;

        for (MobEffectInstance effect : this.getActiveEffects()) {
            signature = 31 * signature + BuiltInRegistries.MOB_EFFECT.getId(effect.getEffect());
            signature = 31 * signature + effect.getAmplifier();
            signature = 31 * signature + effect.getDuration();
        }

        return signature;
    }

    private boolean canExecuteActiveGoal() {
        return !this.isOrderedToSit() && (this.isHostileVariant() || this.getOwnerUuid().isPresent());
    }

    private InteractionResult handleShipItemInteraction(Player player, InteractionHand hand, ItemStack stack) {
        if (!this.isShipInteractionItem(stack)) {
            return InteractionResult.PASS;
        }

        if (this.level().isClientSide()) {
            return InteractionResult.SUCCESS;
        }

        if (this.isHostileVariant()) {
            player.displayClientMessage(Component.translatable("chat.shincolle.entity.hostile",
                    this.getName().copy().withStyle(ChatFormatting.RED)), true);
            return InteractionResult.CONSUME;
        }

        if (stack.getItem() instanceof CombatRationItem ration) {
            this.feedCombatRation(player, hand, stack, ration);
            return InteractionResult.CONSUME;
        }

        if (stack.is(ModItems.BUCKETREPAIR.get())) {
            this.repairFromBucket(player, hand, stack);
            return InteractionResult.CONSUME;
        }

        if (stack.is(ModItems.MODERNKIT.get())) {
            if (!this.canCommanderEdit(player)) {
                this.displayOwnerLocked(player);
                return InteractionResult.CONSUME;
            }

            if (this.addRandomModernization()) {
                this.consumeHeldItem(player, hand, 1);
                ShinColleSoundHelper.playShipVoice(this.level(), player, ShipSoundType.MARRY, 0.65F,
                        ShinColleSoundHelper.variedPitch(player, 1.0F, 0.08F));
                player.displayClientMessage(Component.translatable("chat.shincolle.ship.modernized",
                        this.getName().copy().withStyle(ChatFormatting.AQUA), this.getModernizationDisplayCount()), true);
            } else {
                player.displayClientMessage(Component.translatable("chat.shincolle.ship.modernized_cap",
                        this.getName().copy().withStyle(ChatFormatting.GRAY)), true);
            }

            return InteractionResult.CONSUME;
        }

        if (stack.is(ModItems.TRAININGBOOK.get())) {
            if (!this.canCommanderEdit(player)) {
                this.displayOwnerLocked(player);
                return InteractionResult.CONSUME;
            }

            if (this.getShipLevel() >= this.getLevelCap()) {
                player.displayClientMessage(Component.translatable("chat.shincolle.ship.training_cap",
                        this.getName().copy().withStyle(ChatFormatting.GRAY), this.getLevelCap()), true);
                return InteractionResult.CONSUME;
            }

            int levelGain = 5 + this.random.nextInt(6);
            this.setShipLevel(this.getShipLevel() + levelGain);
            this.consumeHeldItem(player, hand, 1);
            ShinColleSoundHelper.playForPlayer(this.level(), player, ModSoundEvents.SHIP_LEVELUP.get(), 0.75F,
                    ShinColleSoundHelper.variedPitch(player, 1.0F, 0.04F));
            player.displayClientMessage(Component.translatable("chat.shincolle.ship.training",
                    this.getName().copy().withStyle(ChatFormatting.YELLOW), this.getShipLevel()), true);
            return InteractionResult.CONSUME;
        }

        if (stack.is(ModItems.MARRIAGERING.get())) {
            if (!player.isShiftKeyDown()) {
                return InteractionResult.PASS;
            }

            if (!this.canCommanderEdit(player)) {
                this.displayOwnerLocked(player);
                return InteractionResult.CONSUME;
            }

            if (this.isMarried()) {
                player.displayClientMessage(Component.translatable("chat.shincolle.ship.married_already",
                        this.getName().copy().withStyle(ChatFormatting.LIGHT_PURPLE)), true);
                return InteractionResult.CONSUME;
            }

            this.setMarried(true);
            this.setMorale(MAX_MORALE);
            this.addRandomModernization();
            this.addRandomModernization();
            this.addRandomModernization();
            this.heal(this.getMaxHealth());
            this.consumeHeldItem(player, hand, 1);
            if (player instanceof ServerPlayer serverPlayer) {
                TeitokuHelper.incrementMarriageCount(serverPlayer);
                TeitokuHelper.addCollectedShip(serverPlayer, this.getShipClassId());
            }
            ShinColleSoundHelper.playShipVoice(this.level(), player, ShipSoundType.MARRY, 0.8F,
                    ShinColleSoundHelper.variedPitch(player, 1.0F, 0.06F));
            player.displayClientMessage(Component.translatable("chat.shincolle.ship.married",
                    this.getName().copy().withStyle(ChatFormatting.LIGHT_PURPLE)), true);
            return InteractionResult.CONSUME;
        }

        if (stack.is(ModItems.OWNERPAPER.get())) {
            if (!player.isShiftKeyDown()) {
                return InteractionResult.PASS;
            }

            if (!this.canCommanderEdit(player)) {
                this.displayOwnerLocked(player);
                return InteractionResult.CONSUME;
            }

            OwnerPaperItem.Signer targetSigner = OwnerPaperItem.resolveTransferTarget(stack, this.getOwnerUuid().orElse(null)).orElse(null);
            if (targetSigner == null) {
                player.displayClientMessage(Component.translatable("chat.shincolle.ship.ownerpaper_invalid"), true);
                return InteractionResult.CONSUME;
            }

            this.setOwner(targetSigner.uuid(), targetSigner.name());
            this.consumeHeldItem(player, hand, 1);
            ShinColleSoundHelper.playShipVoice(this.level(), player, ShipSoundType.PICKITEM, 0.6F,
                    ShinColleSoundHelper.variedPitch(player, 1.0F, 0.08F));
            player.displayClientMessage(Component.translatable("chat.shincolle.ship.ownerpaper_transfer",
                    this.getName().copy().withStyle(ChatFormatting.AQUA),
                    Component.literal(targetSigner.name()).withStyle(ChatFormatting.GOLD)), true);
            return InteractionResult.CONSUME;
        }

        if (stack.is(ModItems.REPAIRGODDESS.get())) {
            if (!this.canCommanderEdit(player)) {
                this.displayOwnerLocked(player);
                return InteractionResult.CONSUME;
            }

            if (this.storeSingleItem(stack)) {
                this.consumeHeldItem(player, hand, 1);
                player.displayClientMessage(Component.translatable("chat.shincolle.ship.repairgoddess_stowed",
                        this.getName().copy().withStyle(ChatFormatting.LIGHT_PURPLE), this.getRescueItemCount()), true);
            } else {
                player.displayClientMessage(Component.translatable("chat.shincolle.ship.inventory_full",
                        this.getName().copy().withStyle(ChatFormatting.GRAY)), true);
            }

            return InteractionResult.CONSUME;
        }

        if (stack.is(ModItems.POINTERITEM.get())) {
            if (PointerItem.getMode(stack) == 3) {
                if (!this.canCommanderEdit(player)) {
                    this.displayOwnerLocked(player);
                    return InteractionResult.CONSUME;
                }

                this.addMorale(120);
                this.heal(Math.max(1.0F, this.getMaxHealth() * 0.01F));
                ShinColleSoundHelper.playShipVoice(this.level(), player, ShipSoundType.FEED, 0.55F,
                        ShinColleSoundHelper.variedPitch(player, 1.0F, 0.08F));
                player.displayClientMessage(Component.translatable("chat.shincolle.ship.caress",
                        this.getName().copy().withStyle(ChatFormatting.AQUA), this.getMorale()), true);
            } else {
                player.displayClientMessage(Component.translatable("gui.shincolle.placeholder_command_item"), true);
            }

            return InteractionResult.CONSUME;
        }

        return InteractionResult.PASS;
    }

    private boolean isShipInteractionItem(ItemStack stack) {
        return stack.getItem() instanceof CombatRationItem
                || stack.is(ModItems.BUCKETREPAIR.get())
                || stack.is(ModItems.MODERNKIT.get())
                || stack.is(ModItems.TRAININGBOOK.get())
                || stack.is(ModItems.MARRIAGERING.get())
                || stack.is(ModItems.OWNERPAPER.get())
                || stack.is(ModItems.REPAIRGODDESS.get())
                || stack.is(ModItems.POINTERITEM.get());
    }

    private void repairFromBucket(Player player, InteractionHand hand, ItemStack stack) {
        if (this.getHealth() >= this.getMaxHealth()) {
            player.displayClientMessage(Component.translatable("chat.shincolle.ship.repair_full",
                    this.getName().copy().withStyle(ChatFormatting.GRAY)), true);
            return;
        }

        float healAmount = switch (this.getSpec().archetype()) {
            case DESTROYER, SUBMARINE -> this.getMaxHealth() * 0.10F + 5.0F;
            default -> this.getMaxHealth() * 0.05F + 10.0F;
        };

        this.heal(healAmount);
        this.consumeHeldItem(player, hand, 1);
        ShinColleSoundHelper.playShipVoice(this.level(), player, ShipSoundType.PICKITEM, 0.55F,
                ShinColleSoundHelper.variedPitch(player, 1.0F, 0.08F));
        player.displayClientMessage(Component.translatable("chat.shincolle.ship.repair",
                this.getName().copy().withStyle(ChatFormatting.AQUA), Math.round(this.getHealth()), Math.round(this.getMaxHealth())), true);
    }

    private void feedCombatRation(Player player, InteractionHand hand, ItemStack stack, CombatRationItem ration) {
        if (this.getMorale() >= MAX_MORALE) {
            player.displayClientMessage(Component.translatable("chat.shincolle.ship.feed_full",
                    this.getName().copy().withStyle(ChatFormatting.GRAY)), true);
            return;
        }

        this.addMorale(ration.getMoraleValue());
        this.heal(Math.max(1.0F, this.getMaxHealth() * 0.025F));

        String itemPath = BuiltInRegistries.ITEM.getKey(stack.getItem()).getPath();
        if ("combatration4".equals(itemPath) || "combatration5".equals(itemPath)) {
            this.removeAllEffects();
        }

        this.consumeHeldItem(player, hand, 1);
        ShinColleSoundHelper.playShipVoice(this.level(), player, ShipSoundType.FEED, 0.65F,
                ShinColleSoundHelper.variedPitch(player, 1.0F, 0.08F));
        player.displayClientMessage(Component.translatable("chat.shincolle.ship.feed",
                this.getName().copy().withStyle(ChatFormatting.LIGHT_PURPLE), this.getMorale()), true);
    }

    private void openInventory(ServerPlayer player) {
        NetworkHooks.openScreen(player,
                new SimpleMenuProvider(
                        (containerId, inventory, menuPlayer) -> new ShipInventoryMenu(containerId, inventory, this.getId()),
                        Component.translatable("gui.shincolle.ship_inventory.title", this.getName())),
                buffer -> buffer.writeVarInt(this.getId()));
    }

    private void displayOwnerLocked(Player player) {
        player.displayClientMessage(Component.translatable("chat.shincolle.entity.owner_locked",
                Component.literal(this.getOwnerName()).withStyle(ChatFormatting.GOLD)), true);
    }

    private void consumeHeldItem(Player player, InteractionHand hand, int amount) {
        if (player.getAbilities().instabuild) {
            return;
        }

        player.getItemInHand(hand).shrink(amount);
    }

    private int countItemInInventory(Item item) {
        int count = 0;

        for (int slot = 0; slot < this.shipInventory.getContainerSize(); slot++) {
            ItemStack stack = this.shipInventory.getItem(slot);
            if (stack.is(item)) {
                count += stack.getCount();
            }
        }

        return count;
    }

    private boolean consumeFirstMatchingItem(Item item) {
        for (int slot = 0; slot < this.shipInventory.getContainerSize(); slot++) {
            ItemStack stack = this.shipInventory.getItem(slot);
            if (!stack.is(item)) {
                continue;
            }

            stack.shrink(1);
            if (stack.isEmpty()) {
                this.shipInventory.setItem(slot, ItemStack.EMPTY);
            } else {
                this.shipInventory.setItem(slot, stack);
            }
            return true;
        }

        return false;
    }

    private void dropShipInventoryContents() {
        for (int slot = 0; slot < this.shipInventory.getContainerSize(); slot++) {
            ItemStack stack = this.shipInventory.getItem(slot);
            if (stack.isEmpty()) {
                continue;
            }

            this.spawnAtLocation(stack.copy());
            this.shipInventory.setItem(slot, ItemStack.EMPTY);
        }
    }

    private void loadShipInventory(CompoundTag tag) {
        this.shipInventory.clearContent();

        if (!tag.contains(SHIP_INVENTORY_TAG, Tag.TAG_LIST)) {
            this.refreshEquipmentProfile(false);
            return;
        }

        ListTag inventoryEntries = tag.getList(SHIP_INVENTORY_TAG, Tag.TAG_COMPOUND);
        for (int i = 0; i < inventoryEntries.size(); i++) {
            CompoundTag stackTag = inventoryEntries.getCompound(i);
            int slot = stackTag.getByte(SHIP_INVENTORY_SLOT_TAG) & 255;

            if (slot >= 0 && slot < this.shipInventory.getContainerSize()) {
                this.shipInventory.setItem(slot, ItemStack.of(stackTag));
            }
        }

        this.refreshEquipmentProfile(false);
    }

    private void saveShipInventory(CompoundTag tag) {
        ListTag inventoryEntries = new ListTag();

        for (int slot = 0; slot < this.shipInventory.getContainerSize(); slot++) {
            ItemStack stack = this.shipInventory.getItem(slot);
            if (stack.isEmpty()) {
                continue;
            }

            CompoundTag stackTag = new CompoundTag();
            stackTag.putByte(SHIP_INVENTORY_SLOT_TAG, (byte) slot);
            stack.save(stackTag);
            inventoryEntries.add(stackTag);
        }

        tag.put(SHIP_INVENTORY_TAG, inventoryEntries);
    }
}
