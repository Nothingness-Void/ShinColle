package com.lulan.shincolle.entity.ship;

import com.lulan.shincolle.blockentity.CraneBlockEntity;
import com.lulan.shincolle.blockentity.RouteEnergyAccess;
import com.lulan.shincolle.blockentity.RouteNode;
import com.lulan.shincolle.blockentity.WaypointBlockEntity;
import com.mojang.datafixers.util.Pair;
import com.lulan.shincolle.entity.ship.goal.LegacyShipFollowOwnerGoal;
import com.lulan.shincolle.entity.ship.goal.LegacyShipOwnerHurtByTargetGoal;
import com.lulan.shincolle.entity.ship.goal.LegacyShipOwnerHurtTargetGoal;
import com.lulan.shincolle.entity.ship.goal.LegacyShipPickItemGoal;
import com.lulan.shincolle.entity.ship.goal.LegacyShipRangedAttackGoal;
import com.lulan.shincolle.entity.projectile.LegacyShipProjectileProfile;
import com.lulan.shincolle.entity.projectile.LegacyShipProjectileVisual;
import com.lulan.shincolle.entity.projectile.LegacyShipProjectileEntity;
import com.lulan.shincolle.item.CombatRationItem;
import com.lulan.shincolle.item.LegacyShipSpawnEggItem;
import com.lulan.shincolle.item.OwnerPaperItem;
import com.lulan.shincolle.item.PointerItem;
import com.lulan.shincolle.item.equipment.LegacyEquipmentItem;
import com.lulan.shincolle.combat.WorldCombatRulesSavedData;
import com.lulan.shincolle.menu.ShipInventoryMenu;
import com.lulan.shincolle.network.CombatFxDispatcher;
import com.lulan.shincolle.network.CombatReactType;
import com.lulan.shincolle.network.GameplayCommandHandler;
import com.lulan.shincolle.network.GameplayParticleType;
import com.lulan.shincolle.registry.ModItems;
import com.lulan.shincolle.registry.ModSoundEvents;
import com.lulan.shincolle.sound.ShipSoundType;
import com.lulan.shincolle.sound.ShinColleSoundHelper;
import com.lulan.shincolle.teitoku.TeitokuData;
import com.lulan.shincolle.teitoku.TeitokuHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.BossEvent;
import net.minecraft.util.Mth;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffects;
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
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import net.minecraftforge.network.NetworkHooks;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Predicate;

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
    private static final String SHIP_UID_TAG = "ShipUID";
    private static final String COMMAND_POS_TAG = "CommandPos";
    private static final String COMMAND_DIM_TAG = "CommandDim";
    private static final String GUARD_ENTITY_TAG = "GuardEntity";
    private static final String ROUTE_NODE_TAG = "RouteNode";
    private static final String ROUTE_WAIT_TAG = "RouteWait";
    private static final String ROUTE_ENERGY_TAG = "RouteEnergy";
    private static final String AI_AUTO_TARGET_TAG = "AiAutoTarget";
    private static final String AI_ALLOW_PVP_TAG = "AiAllowPvp";
    private static final String AI_AUTO_SUPPLY_TAG = "AiAutoSupply";
    private static final String AI_FOLLOW_RANGE_TAG = "AiFollowRange";
    private static final String AI_ROUTE_STAY_TAG = "AiRouteStay";
    private static final String SHIP_INVENTORY_TAG = "ShipInventory";
    private static final String SHIP_INVENTORY_SLOT_TAG = "Slot";
    private static final String HOSTILE_RUNTIME_TAG = "HostileRuntime";

    private static final int DEFAULT_LEVEL = 1;
    private static final int DEFAULT_MORALE = 1600;
    private static final int MAX_MORALE = 16000;
    private static final int DEFAULT_AI_FOLLOW_RANGE = 14;
    private static final int MAX_MODERN_TOTAL_STEPS = 24;
    private static final int MAX_MODERN_STAT_STEPS = 12;
    private static final int ROUTE_FLUID_TRANSFER_UNIT = 250;
    private static final int ROUTE_ENERGY_TRANSFER_UNIT = 100;

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
    private static final EntityDataAccessor<Integer> DATA_FORMATION_ID =
            SynchedEntityData.defineId(LegacyShipEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_AI_FLAGS =
            SynchedEntityData.defineId(LegacyShipEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_AI_FOLLOW_RANGE =
            SynchedEntityData.defineId(LegacyShipEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_ROUTE_ENERGY =
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
    private ShipEquipmentBehaviorState equipmentBehaviorState = ShipEquipmentBehaviorState.EMPTY;
    private LegacyShipAttackProfile attackProfile = LegacyShipAttackProfile.MELEE_ONLY;
    private LegacyShipStats legacyStats = LegacyShipStats.create(
            ShipEntitySpecs.DEFAULT.legacyClassId(),
            ShipEntitySpecs.DEFAULT.archetype(),
            false,
            DEFAULT_LEVEL,
            DEFAULT_MORALE,
            0, 0, 0, 0,
            false,
            TeitokuData.DEFAULT_FORMATION_ID,
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
    private int shipUid;
    @Nullable
    private BlockPos commandedPos;
    private String commandDimension = "";
    @Nullable
    private UUID guardEntityUuid;
    @Nullable
    private BlockPos routeNodePos;
    private int routeWaitTicks;
    private int routeTransferCooldown;
    private boolean routePreferLoad = true;
    private boolean aiAutoTarget = true;
    private boolean aiAllowPvp;
    private boolean aiAutoSupply = true;
    private int aiFollowRange = DEFAULT_AI_FOLLOW_RANGE;
    private boolean aiRespectRouteStay = true;
    private final HostileRuntimeState hostileRuntimeState = new HostileRuntimeState();
    @Nullable
    private ServerBossEvent bossEvent;

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
        this.entityData.define(DATA_FORMATION_ID, TeitokuData.DEFAULT_FORMATION_ID);
        this.entityData.define(DATA_AI_FLAGS, GameplayCommandHandler.AI_FLAG_AUTO_TARGET
                | GameplayCommandHandler.AI_FLAG_AUTO_SUPPLY
                | GameplayCommandHandler.AI_FLAG_ROUTE_STAY);
        this.entityData.define(DATA_AI_FOLLOW_RANGE, DEFAULT_AI_FOLLOW_RANGE);
        this.entityData.define(DATA_ROUTE_ENERGY, 0);
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
        this.goalSelector.addGoal(4, new LegacyShipPickItemGoal(this, 1.0D));
        this.goalSelector.addGoal(5, new RandomStrollGoal(this, 0.9D) {
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
        this.shipUid = Math.max(0, tag.getInt(SHIP_UID_TAG));
        this.commandedPos = tag.contains(COMMAND_POS_TAG, Tag.TAG_LONG)
                ? BlockPos.of(tag.getLong(COMMAND_POS_TAG))
                : null;
        this.commandDimension = tag.getString(COMMAND_DIM_TAG);
        this.guardEntityUuid = tag.hasUUID(GUARD_ENTITY_TAG) ? tag.getUUID(GUARD_ENTITY_TAG) : null;
        this.routeNodePos = tag.contains(ROUTE_NODE_TAG, Tag.TAG_LONG)
                ? BlockPos.of(tag.getLong(ROUTE_NODE_TAG))
                : null;
        this.routeWaitTicks = Math.max(0, tag.getInt(ROUTE_WAIT_TAG));
        this.setRouteEnergyBuffer(tag.getInt(ROUTE_ENERGY_TAG));
        this.aiAutoTarget = !tag.contains(AI_AUTO_TARGET_TAG) || tag.getBoolean(AI_AUTO_TARGET_TAG);
        this.aiAllowPvp = tag.getBoolean(AI_ALLOW_PVP_TAG);
        this.aiAutoSupply = !tag.contains(AI_AUTO_SUPPLY_TAG) || tag.getBoolean(AI_AUTO_SUPPLY_TAG);
        this.aiFollowRange = tag.contains(AI_FOLLOW_RANGE_TAG)
                ? Mth.clamp(tag.getInt(AI_FOLLOW_RANGE_TAG), 4, 64)
                : DEFAULT_AI_FOLLOW_RANGE;
        this.aiRespectRouteStay = !tag.contains(AI_ROUTE_STAY_TAG) || tag.getBoolean(AI_ROUTE_STAY_TAG);
        int loadedAiFlags = this.encodeAiFlags();
        int loadedFollowRange = this.aiFollowRange;
        this.entityData.set(DATA_AI_FLAGS, loadedAiFlags);
        this.entityData.set(DATA_AI_FOLLOW_RANGE, loadedFollowRange);
        this.applyAiData(loadedAiFlags, loadedFollowRange);
        this.entityData.set(DATA_MODERNIZATION_TOTAL, this.getModernizationCount());
        this.loadShipInventory(tag);
        if (tag.contains(HOSTILE_RUNTIME_TAG, Tag.TAG_COMPOUND)) {
            this.hostileRuntimeState.loadFromTag(tag.getCompound(HOSTILE_RUNTIME_TAG));
        } else {
            this.initializeHostileRuntime(false, false, this.getSpec().archetype() == ShipArchetype.PRINCESS
                    || this.getSpec().archetype() == ShipArchetype.INSTALLATION);
        }
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
        if (this.shipUid > 0) {
            tag.putInt(SHIP_UID_TAG, this.shipUid);
        }
        if (this.commandedPos != null) {
            tag.putLong(COMMAND_POS_TAG, this.commandedPos.asLong());
            if (!this.commandDimension.isBlank()) {
                tag.putString(COMMAND_DIM_TAG, this.commandDimension);
            }
        }
        if (this.guardEntityUuid != null) {
            tag.putUUID(GUARD_ENTITY_TAG, this.guardEntityUuid);
        }
        if (this.routeNodePos != null) {
            tag.putLong(ROUTE_NODE_TAG, this.routeNodePos.asLong());
            tag.putInt(ROUTE_WAIT_TAG, Math.max(0, this.routeWaitTicks));
        }
        tag.putInt(ROUTE_ENERGY_TAG, this.getRouteEnergyBuffer());
        tag.putBoolean(AI_AUTO_TARGET_TAG, this.aiAutoTarget);
        tag.putBoolean(AI_ALLOW_PVP_TAG, this.aiAllowPvp);
        tag.putBoolean(AI_AUTO_SUPPLY_TAG, this.aiAutoSupply);
        tag.putInt(AI_FOLLOW_RANGE_TAG, this.aiFollowRange);
        tag.putBoolean(AI_ROUTE_STAY_TAG, this.aiRespectRouteStay);
        this.saveShipInventory(tag);
        tag.put(HOSTILE_RUNTIME_TAG, this.hostileRuntimeState.saveToTag(new CompoundTag()));
    }

    @Override
    public void tick() {
        super.tick();

        if (!this.level().isClientSide() && this.deathTime > 0 && this.getHealth() > 0.0F) {
            this.setHealth(0.0F);
        }
        if (this.isDeathLocked()) {
            return;
        }

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

        if (!this.level().isClientSide()) {
            this.ensureShipUid();
            if ((this.tickCount % 20) == 0) {
                this.cacheShipState(false);
            }
            this.tickCommandState();
            this.tickEquipmentBehaviors();
            this.tickMarriageBond();
            this.tickLegacyRingPassives();
            this.tickAutoSupportItems();
            this.tickHostileRuntime();

            if (this.tickCount % 20 == 0) {
                int currentFormationId = this.resolveAppliedFormationId();
                if (currentFormationId != this.entityData.get(DATA_FORMATION_ID)) {
                    this.entityData.set(DATA_FORMATION_ID, currentFormationId);
                    this.refreshFromVariant(true);
                }
            }
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
                || DATA_MODERNIZATION_TOTAL.equals(key)
                || DATA_FORMATION_ID.equals(key)) {
            this.refreshFromVariant(true);
        }

        if (DATA_AI_FLAGS.equals(key) || DATA_AI_FOLLOW_RANGE.equals(key)) {
            this.applySyncedAiData();
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
            if (ownerUuid.get().equals(player.getUUID())) {
                return true;
            }

            if (this.level() instanceof ServerLevel serverLevel) {
                int targetOwnerUid = TeitokuHelper.getPlayerUid(player);
                return this.ownerUid > 0 && targetOwnerUid > 0 && TeitokuHelper.isAlly(serverLevel, this.ownerUid, targetOwnerUid);
            }
            return false;
        }

        if (entity instanceof LegacyShipEntity otherShip) {
            if (otherShip.isHostileVariant()) {
                return false;
            }

            if (ownerUuid.equals(otherShip.getOwnerUuid())) {
                return true;
            }

            if (this.level() instanceof ServerLevel serverLevel) {
                return this.ownerUid > 0
                        && otherShip.ownerUid > 0
                        && TeitokuHelper.isAlly(serverLevel, this.ownerUid, otherShip.ownerUid);
            }
            return false;
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
    public SoundSource getSoundSource() {
        return this.isHostileVariant() ? SoundSource.HOSTILE : SoundSource.NEUTRAL;
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
            this.emitAttackFx(livingTarget, LegacyShipAttackKind.MELEE, roll, true);
            return true;
        }

        boolean attacked = target.hurt(this.damageSources().mobAttack(this), roll.damage());
        if (attacked) {
            this.setLastHurtMob(target);
        }
        this.emitAttackFx(livingTarget, LegacyShipAttackKind.MELEE, roll, attacked);

        return attacked;
    }

    @Override
    public boolean hurt(DamageSource damageSource, float amount) {
        Entity attacker = damageSource.getDirectEntity() != null ? damageSource.getDirectEntity() : damageSource.getEntity();

        if (!this.level().isClientSide()
                && attacker != null
                && attacker != this
                && LegacyShipCombatHelper.canDodge(this, attacker)) {
            if (attacker instanceof LivingEntity livingAttacker) {
                CombatFxDispatcher.sendCombatReact(livingAttacker, this, CombatReactType.DODGE, LegacyShipAttackKind.MELEE);
                CombatFxDispatcher.sendParticle(this, GameplayParticleType.TEXT_DODGE,
                        this.getX(), this.getY() + this.getBbHeight() * 0.8D, this.getZ());
            }
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
        if (!this.isHostileVariant()) {
            if (!this.level().isClientSide()) {
                ItemStack recoveredEgg = LegacyShipSpawnEggItem.createRecoveredShipStack(this);
                this.ejectPassengers();
                this.getNavigation().stop();
                this.setTarget(null);
                this.setNoAi(true);
                this.cacheShipState(true);
                if (!recoveredEgg.isEmpty()) {
                    this.spawnOwnerLockedRecoveryEgg(recoveredEgg);
                }
                if (this.level() instanceof ServerLevel serverLevel && this.shipUid > 0) {
                    TeitokuHelper.removeShipFromAllOnlineTeams(serverLevel.getServer(), this.shipUid);
                }
            }
            return;
        }

        if (!this.level().isClientSide()) {
            this.ejectPassengers();
            this.getNavigation().stop();
            this.setTarget(null);
            this.setNoAi(true);
            this.cacheShipState(true);
            super.dropCustomDeathLoot(damageSource, looting, recentlyHit);
            this.dropHostileLoot(looting);
            this.dropShipInventoryContents();
            if (this.level() instanceof ServerLevel serverLevel && this.shipUid > 0) {
                TeitokuHelper.removeShipFromAllOnlineTeams(serverLevel.getServer(), this.shipUid);
            }
        }
    }

    @Override
    public void remove(Entity.RemovalReason removalReason) {
        if (!this.level().isClientSide()) {
            boolean dead = removalReason == Entity.RemovalReason.KILLED
                    || removalReason == Entity.RemovalReason.DISCARDED
                    || !this.isAlive();
            this.cacheShipState(dead);
        }
        super.remove(removalReason);
    }

    @Override
    public float getVoicePitch() {
        return 1.0F + (this.random.nextFloat() - this.random.nextFloat()) * 0.05F;
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (this.isDeathLocked()) {
            return InteractionResult.PASS;
        }

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
            this.initializeHostileRuntime(false,
                    spec.archetype() == ShipArchetype.CARRIER || spec.archetype() == ShipArchetype.BATTLESHIP || spec.archetype() == ShipArchetype.CRUISER,
                    spec.archetype() == ShipArchetype.PRINCESS || spec.archetype() == ShipArchetype.INSTALLATION);
        } else if (owner != null) {
            this.setOwner(owner);
            this.initializeHostileRuntime(false, false, false);
        }

        this.refreshFromVariant(false);
    }

    public void restoreRecoveredDeployment(@Nullable Player fallbackOwner) {
        this.deathTime = 0;
        this.setNoAi(false);
        this.clearCommandState();

        if (this.getOwnerUuid().isEmpty() && fallbackOwner != null) {
            this.setOwner(fallbackOwner);
        }

        this.refreshFromVariant(true);
        this.setHealth(Math.max(1.0F, this.getMaxHealth() * 0.25F));
        this.cacheShipState(false);
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

    public boolean isBossEncounter() {
        return this.hostileRuntimeState.isBoss();
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

    public int getShipUid() {
        return this.shipUid;
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

    public ShipEquipmentBehaviorState getEquipmentBehaviorState() {
        return this.equipmentBehaviorState;
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

    public boolean isAiAutoTarget() {
        return this.aiAutoTarget;
    }

    public void setAiAutoTarget(boolean aiAutoTarget) {
        this.aiAutoTarget = aiAutoTarget;
        this.entityData.set(DATA_AI_FLAGS, this.encodeAiFlags());
    }

    public boolean isAiAllowPvp() {
        return this.aiAllowPvp;
    }

    public void setAiAllowPvp(boolean aiAllowPvp) {
        this.aiAllowPvp = aiAllowPvp;
        this.entityData.set(DATA_AI_FLAGS, this.encodeAiFlags());
    }

    public boolean isAiAutoSupply() {
        return this.aiAutoSupply;
    }

    public void setAiAutoSupply(boolean aiAutoSupply) {
        this.aiAutoSupply = aiAutoSupply;
        this.entityData.set(DATA_AI_FLAGS, this.encodeAiFlags());
    }

    public int getAiFollowRange() {
        return this.aiFollowRange;
    }

    public void setAiFollowRange(int aiFollowRange) {
        this.aiFollowRange = Mth.clamp(aiFollowRange, 4, 64);
        this.entityData.set(DATA_AI_FOLLOW_RANGE, this.aiFollowRange);
    }

    public boolean isAiRespectRouteStay() {
        return this.aiRespectRouteStay;
    }

    public void setAiRespectRouteStay(boolean aiRespectRouteStay) {
        this.aiRespectRouteStay = aiRespectRouteStay;
        this.entityData.set(DATA_AI_FLAGS, this.encodeAiFlags());
    }

    public int getAiFlagsBitmask() {
        return this.encodeAiFlags();
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

    private int resolveAppliedFormationId() {
        if (this.isHostileVariant()) {
            return TeitokuData.DEFAULT_FORMATION_ID;
        }

        Player owner = this.getOwnerPlayer();
        if (owner == null) {
            return TeitokuData.DEFAULT_FORMATION_ID;
        }

        return LegacyShipStatTables.normalizeFormationId(TeitokuHelper.getFormationIdForShip(owner, this.shipUid));
    }

    public boolean canCommanderEdit(Player player) {
        return !this.isDeathLocked() && !this.isHostileVariant() && this.isOwnedBy(player);
    }

    public void commandMoveTo(@Nullable BlockPos pos, String dimensionId) {
        if (pos == null) {
            this.clearCommandState();
            return;
        }

        this.commandedPos = pos.immutable();
        this.commandDimension = dimensionId == null ? "" : dimensionId;
        this.guardEntityUuid = null;
        this.setTarget(null);
        BlockEntity blockEntity = this.level().getBlockEntity(pos);
        this.routeNodePos = blockEntity instanceof RouteNode ? pos.immutable() : null;
        this.routeWaitTicks = 0;
        this.routeTransferCooldown = 0;
        this.routePreferLoad = true;
    }

    public @Nullable BlockPos getCommandedPos() {
        return this.commandedPos;
    }

    public String getCommandDimension() {
        return this.commandDimension;
    }

    public @Nullable UUID getGuardEntityUuid() {
        return this.guardEntityUuid;
    }

    public @Nullable BlockPos getRouteNodePos() {
        return this.routeNodePos;
    }

    public int getRouteWaitTicks() {
        return this.routeWaitTicks;
    }

    public void commandGuardEntity(UUID entityUuid) {
        this.guardEntityUuid = entityUuid;
        this.commandedPos = null;
        this.commandDimension = "";
        this.routeNodePos = null;
        this.setTarget(null);
        this.routeWaitTicks = 0;
        this.routeTransferCooldown = 0;
        this.routePreferLoad = true;
    }

    public void clearCommandState() {
        this.commandedPos = null;
        this.commandDimension = "";
        this.guardEntityUuid = null;
        this.routeNodePos = null;
        this.routeWaitTicks = 0;
        this.routeTransferCooldown = 0;
        this.routePreferLoad = true;
    }

    public boolean canExecuteCombatGoal() {
        return this.canExecuteActiveGoal();
    }

    public boolean canUseCompatRangedCombat() {
        return this.attackProfile.hasRangedAttack();
    }

    public int getCompatAttackAimTime() {
        int shipLevel = Math.min(150, this.getShipLevel());
        int baseAimTime = Mth.clamp((int) (20F * (150 - shipLevel) / 150F) + 10, 8, 30);
        return Math.max(6, baseAimTime - this.equipmentBehaviorState.aimTimeReductionTicks(LegacyShipAttackKind.LIGHT));
    }

    public float getCompatAttackRange() {
        float behaviorBonus = this.equipmentBehaviorState.surfaceRadarLevel() * 1.1F
                + this.equipmentBehaviorState.airRadarLevel() * 0.9F
                + this.equipmentBehaviorState.fcsLevel() * 1.2F
                + this.equipmentBehaviorState.searchlightLevel() * 0.4F;
        return Math.max(3.0F, this.legacyStats.attackRange() + behaviorBonus);
    }

    public double getFollowMovementSpeedModifier() {
        return this.equipmentBehaviorState.followSpeedMultiplier();
    }

    public double getCombatMovementSpeedModifier() {
        return this.equipmentBehaviorState.commandSpeedMultiplier();
    }

    public boolean shouldPreferAutonomousRoute() {
        return this.equipmentBehaviorState.autonomousRoute() && this.hasActiveCommandState();
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

    public boolean supportsCompatAttack(LegacyShipAttackKind attackKind) {
        return this.canUseCompatAttack(attackKind);
    }

    public int getCompatAttackCooldown(LegacyShipAttackKind attackKind) {
        return switch (attackKind) {
            case LIGHT -> this.lightAttackCooldown;
            case HEAVY -> this.heavyAttackCooldown;
            case AIR_LIGHT, AIR_HEAVY -> this.airAttackCooldown;
            case MELEE -> this.meleeAttackCooldown;
        };
    }

    public int getCompatAttackMaxCooldown(LegacyShipAttackKind attackKind) {
        return LegacyShipCombatHelper.attackDelay(this, attackKind);
    }

    public boolean isTargetInCompatRange(@Nullable LivingEntity target, LegacyShipAttackKind attackKind) {
        if (target == null) {
            return false;
        }

        double maxRange = attackKind == LegacyShipAttackKind.MELEE ? 3.25D : this.getCompatAttackRange();
        return this.distanceToSqr(target) <= maxRange * maxRange;
    }

    public boolean performPlayerCompatAttack(LivingEntity target, LegacyShipAttackKind attackKind) {
        return this.performCompatAttack(target, attackKind);
    }

    public boolean canEngage(@Nullable LivingEntity target) {
        if (target == null || !target.isAlive() || target == this || this.isAlliedTo(target)) {
            return false;
        }
        if (this.level() instanceof ServerLevel serverLevel
                && WorldCombatRulesSavedData.get(serverLevel).isUnattackable(TeitokuHelper.resolveTargetClass(target))) {
            return false;
        }

        Player owner = this.getOwnerPlayer();

        if (target instanceof Player player) {
            if (this.isHostileVariant()) {
                return true;
            }

            if (this.isOwnedBy(player)) {
                return false;
            }

            if (!this.aiAllowPvp) {
                return false;
            }

            if (this.level() instanceof ServerLevel serverLevel) {
                int targetOwnerUid = TeitokuHelper.getPlayerUid(player);
                return this.ownerUid > 0 && targetOwnerUid > 0 && TeitokuHelper.isBanned(serverLevel, this.ownerUid, targetOwnerUid);
            }
            return false;
        }

        if (target instanceof LegacyShipEntity otherShip) {
            if (this.isHostileVariant() != otherShip.isHostileVariant()) {
                return true;
            }

            if (this.isHostileVariant()) {
                return false;
            }

            if (this.level() instanceof ServerLevel serverLevel) {
                return this.ownerUid > 0
                        && otherShip.ownerUid > 0
                        && TeitokuHelper.isBanned(serverLevel, this.ownerUid, otherShip.ownerUid);
            }
            return false;
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
        if (!this.aiAutoTarget) {
            return false;
        }

        if (!(target instanceof Mob mob) || !target.isAlive() || !(target instanceof Enemy) || !this.canEngage(target)) {
            return false;
        }

        Player owner = this.getOwnerPlayer();
        if (owner != null && !TeitokuHelper.isAutoTargetAllowed(owner, mob)) {
            return false;
        }

        boolean flyingTarget = LegacyShipCombatHelper.isFlyingTarget(target);
        boolean underseaTarget = LegacyShipCombatHelper.isUnderseaTarget(target);
        double followRange = Math.max(16.0D, this.getAttributeValue(Attributes.FOLLOW_RANGE))
                + this.equipmentBehaviorState.detectionRangeBonusForTarget(flyingTarget, underseaTarget);
        double shipRangeSqr = followRange * followRange;
        if (this.distanceToSqr(mob) <= shipRangeSqr) {
            return true;
        }

        return owner != null && owner.distanceToSqr(mob) <= shipRangeSqr;
    }

    public void setOwner(Player player) {
        this.cleanupShipTeamSlotsIfNeeded();
        this.entityData.set(DATA_OWNER_UUID, Optional.of(player.getUUID()));
        this.entityData.set(DATA_OWNER_NAME, player.getGameProfile().getName());
        this.ownerUid = Math.max(0, TeitokuHelper.getPlayerUid(player));
        this.resetOwnershipBoundState();
        this.refreshFromVariant(true);
        this.cacheShipState(false);
        if (player instanceof ServerPlayer serverPlayer) {
            TeitokuHelper.syncGameplayState(serverPlayer);
        }
    }

    public void setOwner(UUID ownerUuid, String ownerName) {
        int resolvedOwnerUid = 0;
        if (!this.level().isClientSide() && this.level() instanceof ServerLevel serverLevel) {
            resolvedOwnerUid = TeitokuHelper.resolvePlayerUid(serverLevel, ownerUuid, ownerName);
        }
        this.setOwner(ownerUuid, ownerName, resolvedOwnerUid);
    }

    public void setOwner(UUID ownerUuid, String ownerName, int ownerUid) {
        this.cleanupShipTeamSlotsIfNeeded();
        this.entityData.set(DATA_OWNER_UUID, Optional.of(ownerUuid));
        this.entityData.set(DATA_OWNER_NAME, ownerName);
        this.ownerUid = Math.max(0, ownerUid);
        this.resetOwnershipBoundState();
        this.refreshFromVariant(true);
        this.cacheShipState(false);
        if (!this.level().isClientSide() && this.level() instanceof ServerLevel serverLevel) {
            ServerPlayer serverPlayer = serverLevel.getServer().getPlayerList().getPlayer(ownerUuid);
            if (serverPlayer != null) {
                TeitokuHelper.syncGameplayState(serverPlayer);
            }
        }
    }

    public void clearOwner() {
        ServerPlayer previousOwner = null;
        if (!this.level().isClientSide() && this.level() instanceof ServerLevel serverLevel) {
            previousOwner = this.getOwnerUuid()
                    .map(uuid -> serverLevel.getServer().getPlayerList().getPlayer(uuid))
                    .orElse(null);
        }
        this.cleanupShipTeamSlotsIfNeeded();
        this.entityData.set(DATA_OWNER_UUID, Optional.empty());
        this.entityData.set(DATA_OWNER_NAME, "");
        this.ownerUid = 0;
        this.resetOwnershipBoundState();
        this.refreshFromVariant(true);
        this.cacheShipState(false);
        if (previousOwner != null) {
            TeitokuHelper.syncGameplayState(previousOwner);
        }
    }

    public void initializeHostileRuntime(boolean naturalSpawn, boolean elite, boolean boss) {
        boolean installationBoss = boss && this.getSpec().archetype() == ShipArchetype.INSTALLATION;
        this.hostileRuntimeState.reset(naturalSpawn, elite, boss, installationBoss);
        if (!boss && this.bossEvent != null) {
            this.bossEvent.removeAllPlayers();
            this.bossEvent = null;
        }
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

    public int getRouteEnergyBuffer() {
        return this.entityData.get(DATA_ROUTE_ENERGY);
    }

    public int getRouteEnergyCapacity() {
        return this.getRouteTransferBudget() * 400;
    }

    public String getRouteEnergyText() {
        return this.getRouteEnergyBuffer() + " / " + this.getRouteEnergyCapacity();
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
        if (this.level() instanceof ServerLevel serverLevel && this.shipUid > 0) {
            TeitokuHelper.removeShipFromAllOnlineTeams(serverLevel.getServer(), this.shipUid);
        }
        this.resetOwnershipBoundState();
        this.playSound(ModSoundEvents.SHIP_KAITAI.get(), 0.8F, this.getVoicePitch());
        this.discard();
        return true;
    }

    private void refreshFromVariant(boolean preserveHealth) {
        ShipEntitySpec spec = this.getSpec();
        this.attackProfile = LegacyShipBehaviorCatalog.attackProfile(spec);
        int formationId = this.entityData.get(DATA_FORMATION_ID);
        if (!this.level().isClientSide()) {
            formationId = this.resolveAppliedFormationId();
            this.entityData.set(DATA_FORMATION_ID, formationId);
        }
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
                this.isMarried(),
                formationId,
                this.equipmentProfile,
                this.getActiveEffects());
        this.cachedEffectSignature = this.computeActiveEffectSignature();

        this.refreshDimensions();
        this.applyBaseValue(Attributes.MAX_HEALTH, this.legacyStats.get(LegacyShipStatTables.Attr.HP));
        this.applyBaseValue(Attributes.MOVEMENT_SPEED, this.legacyStats.moveSpeed());
        this.applyBaseValue(Attributes.ATTACK_DAMAGE, this.legacyStats.meleeDamage());
        this.applyBaseValue(Attributes.FOLLOW_RANGE, Math.max(28D,
                16D + this.legacyStats.attackRange() + this.equipmentBehaviorState.detectionRangeBonus()));
        this.applyBaseValue(Attributes.KNOCKBACK_RESISTANCE, this.legacyStats.knockbackResistance());

        float maxHealth = this.getMaxHealth();
        float currentHealth = preserveHealth ? this.clampPreservedHealth(maxHealth) : maxHealth;
        this.setHealth(currentHealth);
    }

    private float clampPreservedHealth(float maxHealth) {
        float currentHealth = this.getHealth();
        if (this.isDeathLocked() || currentHealth <= 0.0F) {
            return 0.0F;
        }
        return Mth.clamp(currentHealth, 1.0F, maxHealth);
    }

    private boolean isDeathLocked() {
        return this.isRemoved() || this.isDeadOrDying() || this.deathTime > 0;
    }

    private void refreshEquipmentProfile(boolean preserveHealth) {
        this.equipmentProfile = ShipEquipmentProfile.fromInventory(this.shipInventory, this.getSpec().archetype());
        this.equipmentBehaviorState = ShipEquipmentBehaviorState.fromInventory(this.shipInventory, this.getSpec().archetype());
        this.setRouteEnergyBuffer(this.getRouteEnergyBuffer());
        this.refreshFromVariant(preserveHealth);
    }

    private void spawnOwnerLockedRecoveryEgg(ItemStack recoveredEgg) {
        ItemEntity itemEntity = new ItemEntity(this.level(), this.getX(), this.getY() + 0.5D, this.getZ(), recoveredEgg);
        itemEntity.setPickUpDelay(10);
        this.getOwnerUuid().ifPresent(itemEntity::setTarget);
        this.level().addFreshEntity(itemEntity);

        Player owner = this.getOwnerPlayer();
        if (owner != null) {
            owner.displayClientMessage(Component.translatable("chat.shincolle.ship.recovery_egg",
                    this.getName().copy().withStyle(ChatFormatting.AQUA)), true);
        }
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

        LegacyShipProjectileProfile projectileProfile = this.attackProfile.projectileProfile(attackKind);
        if (attackKind.justLaunch() && !projectileProfile.isPresent()) {
            return false;
        }
        if (attackKind != LegacyShipAttackKind.MELEE && this.equipmentBehaviorState.flareLevel() > 0) {
            this.applyIllumination(target, this.equipmentBehaviorState.flareDurationTicks());
            this.emitIlluminationFx(target, true);
        }

        int delay = LegacyShipCombatHelper.attackDelay(this, attackKind);

        if (attackKind == LegacyShipAttackKind.AIR_LIGHT || attackKind == LegacyShipAttackKind.AIR_HEAVY) {
            boolean launched = this.launchCompatAircraft(target, attackKind, projectileProfile);
            if (launched) {
                this.airAttackCooldown = delay;
                this.setLastHurtMob(target);
                this.playShipCombatSound(ModSoundEvents.SHIP_AIRCRAFT.get(), 0.6F, 0.9F + this.random.nextFloat() * 0.2F);
            }
            return launched;
        }

        LegacyShipCombatHelper.AttackRoll roll = LegacyShipCombatHelper.rollAttack(this, target, attackKind);
        if (roll.miss() && !attackKind.justLaunch()) {
            this.setLastHurtMob(target);
            this.emitAttackFx(target, attackKind, roll, true);
            switch (attackKind) {
                case LIGHT -> this.lightAttackCooldown = delay;
                case HEAVY -> this.heavyAttackCooldown = delay;
                case MELEE -> this.meleeAttackCooldown = delay;
                default -> {
                }
            }
            return true;
        }

        if (attackKind.justLaunch()) {
            boolean launched = this.launchCompatProjectile(target, attackKind, roll);
            if (launched) {
                switch (attackKind) {
                    case HEAVY -> {
                        this.heavyAttackCooldown = delay;
                        this.playShipCombatSound(ModSoundEvents.SHIP_FIREHEAVY.get(), 0.75F, 0.85F + this.random.nextFloat() * 0.3F);
                    }
                    case LIGHT -> {
                        this.lightAttackCooldown = delay;
                        this.playShipCombatSound(ModSoundEvents.SHIP_FIRELIGHT.get(), 0.65F, 0.9F + this.random.nextFloat() * 0.2F);
                    }
                    case MELEE -> this.meleeAttackCooldown = delay;
                    case AIR_LIGHT, AIR_HEAVY -> this.airAttackCooldown = delay;
                }
                this.setLastHurtMob(target);
            }
            return launched;
        }

        boolean attacked = target.hurt(this.damageSources().mobAttack(this), roll.damage());
        if (attacked) {
            this.setLastHurtMob(target);
            if (attackKind == LegacyShipAttackKind.MELEE) {
                this.playShipCombatSound(ModSoundEvents.SHIP_HITMETAL.get(), 0.55F, 0.9F + this.random.nextFloat() * 0.2F);
            } else if (attackKind == LegacyShipAttackKind.LIGHT) {
                this.playShipCombatSound(ModSoundEvents.SHIP_FIRELIGHT.get(), 0.6F, 0.9F + this.random.nextFloat() * 0.2F);
            } else if (attackKind == LegacyShipAttackKind.HEAVY) {
                this.playShipCombatSound(ModSoundEvents.SHIP_FIREHEAVY.get(), 0.7F, 0.85F + this.random.nextFloat() * 0.3F);
            }
        }
        this.emitAttackFx(target, attackKind, roll, attacked);
        switch (attackKind) {
            case LIGHT -> this.lightAttackCooldown = delay;
            case HEAVY -> this.heavyAttackCooldown = delay;
            case AIR_LIGHT, AIR_HEAVY -> this.airAttackCooldown = delay;
            case MELEE -> this.meleeAttackCooldown = delay;
        }

        return attacked;
    }

    private void playShipCombatSound(SoundEvent sound, float volume, float pitch) {
        this.level().playSound(null, this.blockPosition(), sound, this.getSoundSource(), volume, pitch);
    }

    private void emitAttackFx(LivingEntity target, LegacyShipAttackKind attackKind, LegacyShipCombatHelper.AttackRoll roll, boolean attacked) {
        LegacyShipProjectileProfile profile = this.attackProfile.projectileProfile(attackKind);
        this.emitAttackFx(target, attackKind, roll, attacked, profile.visual(), profile.hitParticleType());
    }

    private void emitAttackFx(LivingEntity target, LegacyShipAttackKind attackKind, LegacyShipCombatHelper.AttackRoll roll,
                              boolean attacked, LegacyShipProjectileVisual projectileVisual, GameplayParticleType hitParticleType) {
        if (this.level().isClientSide()) {
            return;
        }

        if (roll.miss()) {
            CombatFxDispatcher.sendCombatReact(this, target, CombatReactType.MISS, attackKind, projectileVisual);
            CombatFxDispatcher.sendParticle(target, GameplayParticleType.TEXT_MISS,
                    target.getX(), target.getY() + target.getBbHeight() * 0.8D, target.getZ());
            return;
        }

        if (roll.crit()) {
            CombatFxDispatcher.sendCombatReact(this, target, CombatReactType.CRIT, attackKind, projectileVisual);
            CombatFxDispatcher.sendParticle(target, GameplayParticleType.TEXT_CRIT,
                    target.getX(), target.getY() + target.getBbHeight() * 0.8D, target.getZ());
        } else if (roll.tripleHit()) {
            CombatFxDispatcher.sendCombatReact(this, target, CombatReactType.TRIPLE_HIT, attackKind, projectileVisual);
            CombatFxDispatcher.sendParticle(target, GameplayParticleType.TEXT_TRIPLE,
                    target.getX(), target.getY() + target.getBbHeight() * 0.8D, target.getZ());
        } else if (roll.doubleHit()) {
            CombatFxDispatcher.sendCombatReact(this, target, CombatReactType.DOUBLE_HIT, attackKind, projectileVisual);
            CombatFxDispatcher.sendParticle(target, GameplayParticleType.TEXT_DOUBLE,
                    target.getX(), target.getY() + target.getBbHeight() * 0.8D, target.getZ());
        }

        if (attacked) {
            CombatFxDispatcher.sendCombatReact(this, target, CombatReactType.HIT, attackKind, projectileVisual);
            CombatFxDispatcher.sendParticle(target, hitParticleType,
                    target.getX(), target.getY() + target.getBbHeight() * 0.5D, target.getZ());
        }
    }

    public boolean performCompatAircraftStrike(LivingEntity target, LegacyShipAttackKind attackKind,
                                               LegacyShipProjectileVisual projectileVisual, GameplayParticleType hitParticleType,
                                               boolean flarePayload, boolean searchlightPayload) {
        if (!this.canEngage(target) || (attackKind != LegacyShipAttackKind.AIR_LIGHT && attackKind != LegacyShipAttackKind.AIR_HEAVY)) {
            return false;
        }

        LegacyShipCombatHelper.AttackRoll roll = LegacyShipCombatHelper.rollAttack(this, target, attackKind);
        if (roll.miss()) {
            this.setLastHurtMob(target);
            this.emitAttackFx(target, attackKind, roll, false, projectileVisual, hitParticleType);
            return true;
        }

        boolean attacked = target.hurt(this.damageSources().mobAttack(this), roll.damage());
        if (attacked) {
            this.setLastHurtMob(target);
            if (attackKind == LegacyShipAttackKind.AIR_HEAVY) {
                target.knockback(0.24D, this.getX() - target.getX(), this.getZ() - target.getZ());
            }
            if (flarePayload) {
                CombatFxDispatcher.sendParticle(target, GameplayParticleType.FLARE_BURST,
                        target.getX(), target.getY() + target.getBbHeight() * 0.75D, target.getZ());
            }
            if (searchlightPayload) {
                CombatFxDispatcher.sendParticle(target, GameplayParticleType.SEARCHLIGHT_MARK,
                        target.getX(), target.getY() + target.getBbHeight() * 0.85D, target.getZ());
            }
        }
        this.emitAttackFx(target, attackKind, roll, attacked, projectileVisual, hitParticleType);
        return true;
    }

    private boolean launchCompatProjectile(LivingEntity target, LegacyShipAttackKind attackKind, LegacyShipCombatHelper.AttackRoll roll) {
        if (this.level().isClientSide()) {
            return true;
        }

        LegacyShipProjectileEntity projectile = LegacyShipProjectileEntity.create(this.level(), this, target,
                attackKind, roll.damage(), roll.miss());
        this.level().addFreshEntity(projectile);
        CombatFxDispatcher.sendCombatReact(this, target, CombatReactType.LAUNCH, attackKind,
                projectile.getProjectileVisual());
        CombatFxDispatcher.sendParticle(this, projectile.getLaunchParticleType(),
                this.getX(), this.getY() + this.getBbHeight() * 0.7D, this.getZ());
        return true;
    }

    private boolean launchCompatAircraft(LivingEntity target, LegacyShipAttackKind attackKind, LegacyShipProjectileProfile projectileProfile) {
        if (this.level().isClientSide()) {
            return true;
        }

        LegacyShipAircraftEntity aircraft = LegacyShipAircraftEntity.create(this.level(), this, target, attackKind);
        if (aircraft == null) {
            return false;
        }

        this.level().addFreshEntity(aircraft);
        CombatFxDispatcher.sendCombatReact(this, target, CombatReactType.LAUNCH, attackKind, projectileProfile.visual());
        aircraft.playLaunchFx();
        return true;
    }

    private void applyBaseValue(Attribute attribute, double value) {
        AttributeInstance instance = this.getAttribute(attribute);

        if (instance != null) {
            instance.setBaseValue(value);
        }
    }

    private void ensureShipUid() {
        if (this.shipUid > 0 || !(this.level() instanceof ServerLevel serverLevel)) {
            return;
        }

        this.shipUid = TeitokuHelper.resolveShipUid(serverLevel, this.getUUID());
    }

    private void cacheShipState(boolean dead) {
        this.ensureShipUid();
        TeitokuHelper.refreshShipCache(this, dead);
    }

    private void cleanupShipTeamSlotsIfNeeded() {
        if (this.shipUid <= 0 || !(this.level() instanceof ServerLevel serverLevel)) {
            return;
        }
        TeitokuHelper.removeShipFromAllOnlineTeams(serverLevel.getServer(), this.shipUid);
    }

    private void tickHostileRuntime() {
        if (!(this.level() instanceof ServerLevel serverLevel) || !this.isHostileVariant()) {
            return;
        }

        this.hostileRuntimeState.tick();
        LivingEntity target = this.getTarget();
        if (target != null && (!target.isAlive() || !this.canEngage(target))) {
            this.setTarget(null);
            target = null;
        }

        if (target == null) {
            target = this.findBossTarget();
            if (target != null) {
                this.setTarget(target);
            }
        }

        if (this.hostileRuntimeState.isNaturalSpawn()) {
            boolean playerNearby = serverLevel.getNearestPlayer(this, 96.0D) != null;
            this.hostileRuntimeState.setDespawnTicks(playerNearby ? 0 : this.hostileRuntimeState.getDespawnTicks() + 1);
            if (this.hostileRuntimeState.getDespawnTicks() > 20 * 45) {
                this.discard();
                return;
            }
        }

        if (!this.hostileRuntimeState.isBoss()) {
            return;
        }

        this.ensureBossEvent();
        this.updateBossPhase();
        if (this.bossEvent != null) {
            this.bossEvent.setName(this.getName());
            this.bossEvent.setProgress(Mth.clamp(this.getHealth() / this.getMaxHealth(), 0.0F, 1.0F));
        }

        if (target == null) {
            return;
        }

        this.hostileRuntimeState.setEngagedTicks(this.hostileRuntimeState.getEngagedTicks() + 1);
        if (this.hostileRuntimeState.getActionCooldown() <= 0) {
            this.performBossAction(target);
        }
    }

    private void ensureBossEvent() {
        if (!this.hostileRuntimeState.isBoss() || this.level().isClientSide()) {
            return;
        }

        if (this.bossEvent == null) {
            BossEvent.BossBarColor color = this.hostileRuntimeState.isInstallationBoss()
                    ? BossEvent.BossBarColor.YELLOW
                    : BossEvent.BossBarColor.RED;
            this.bossEvent = new ServerBossEvent(this.getName(), color, BossEvent.BossBarOverlay.NOTCHED_10);
            this.bossEvent.setDarkenScreen(false);
        }
    }

    private void updateBossPhase() {
        float ratio = this.getHealth() / Math.max(1.0F, this.getMaxHealth());
        int phase = ratio <= 0.25F ? 3 : ratio <= 0.5F ? 2 : ratio <= 0.75F ? 1 : 0;
        if (phase == this.hostileRuntimeState.getPhase()) {
            return;
        }

        this.hostileRuntimeState.setPhase(phase);
        if (this.level() instanceof ServerLevel) {
            CombatFxDispatcher.sendParticle(this, GameplayParticleType.HIT_EXPLOSION,
                    this.getX(), this.getY() + this.getBbHeight() * 0.7D, this.getZ());
        }
    }

    private void performBossAction(LivingEntity target) {
        BossPhaseProfile phaseProfile = BossPhaseProfile.forSpec(this.getSpec());
        int actionIndex = (this.hostileRuntimeState.getPhase() + this.tickCount / 40) % phaseProfile.actionCycle().size();
        BossActionType actionType = phaseProfile.actionCycle().get(actionIndex);
        boolean acted = false;

        switch (actionType) {
            case AIR_ASSAULT -> {
                acted = this.tryCompatAirAttack(target) || this.tryCompatCannonAttack(target);
                this.applyIllumination(target, 40 + this.hostileRuntimeState.getPhase() * 20);
            }
            case CANNON_BURST -> {
                acted = this.tryCompatCannonAttack(target);
                if (!acted) {
                    acted = this.tryCompatAirAttack(target);
                }
            }
            case CHARGE -> {
                Vec3 dash = target.position().subtract(this.position());
                if (dash.lengthSqr() > 0.01D) {
                    Vec3 motion = dash.normalize().scale(0.55D + this.hostileRuntimeState.getPhase() * 0.08D);
                    this.push(motion.x, 0.08D, motion.z);
                    this.hurtMarked = true;
                }
                acted = this.doHurtTarget(target) || this.tryCompatCannonAttack(target);
            }
            case AREA_BOMBARD -> {
                acted = this.launchBossBombardment(target);
            }
            case SUMMON_ESCORT -> acted = this.summonBossEscort(phaseProfile.summonEggMeta());
        }

        if (!acted) {
            this.hostileRuntimeState.setActionCooldown(20);
            return;
        }

        int cooldown = Math.max(16, phaseProfile.baseActionCooldown() - this.hostileRuntimeState.getPhase() * 6);
        this.hostileRuntimeState.setActionCooldown(cooldown);
        if (actionType == BossActionType.SUMMON_ESCORT) {
            this.hostileRuntimeState.setSummonCooldown(Math.max(80, phaseProfile.baseSummonCooldown() - this.hostileRuntimeState.getPhase() * 20));
        }
    }

    private boolean launchBossBombardment(LivingEntity target) {
        if (this.level().isClientSide()) {
            return true;
        }

        int count = 1 + this.hostileRuntimeState.getPhase();
        boolean launched = false;
        for (int i = 0; i < count; i++) {
            LegacyShipCombatHelper.AttackRoll roll = LegacyShipCombatHelper.rollAttack(this, target, LegacyShipAttackKind.HEAVY);
            LegacyShipProjectileEntity projectile = LegacyShipProjectileEntity.create(this.level(), this, target,
                    LegacyShipAttackKind.HEAVY, roll.damage(), roll.miss());
            double offsetX = (this.random.nextDouble() - 0.5D) * 0.12D;
            double offsetZ = (this.random.nextDouble() - 0.5D) * 0.12D;
            projectile.setDeltaMovement(projectile.getDeltaMovement().add(offsetX, 0.0D, offsetZ));
            this.level().addFreshEntity(projectile);
            CombatFxDispatcher.sendCombatReact(this, target, CombatReactType.LAUNCH, LegacyShipAttackKind.HEAVY,
                    projectile.getProjectileVisual());
            CombatFxDispatcher.sendParticle(this, projectile.getLaunchParticleType(),
                    this.getX(), this.getY() + this.getBbHeight() * 0.7D, this.getZ());
            launched = true;
        }
        if (launched) {
            this.setLastHurtMob(target);
        }
        return launched;
    }

    private boolean summonBossEscort(int summonEggMeta) {
        if (!(this.level() instanceof ServerLevel serverLevel) || this.hostileRuntimeState.getSummonCooldown() > 0) {
            return false;
        }

        ShipEntitySpec summonSpec = ShipEntitySpecs.findByEggMeta(summonEggMeta);
        if (summonSpec == null || !summonSpec.hostile()) {
            return false;
        }

        int count = 1 + Math.min(2, this.hostileRuntimeState.getPhase());
        boolean summoned = false;
        for (int i = 0; i < count; i++) {
            LegacyShipEntity escort = new LegacyShipEntity((EntityType<? extends LegacyShipEntity>) this.getType(), serverLevel);
            if (escort == null) {
                continue;
            }

            double spawnX = this.getX() + (this.random.nextDouble() - 0.5D) * 6.0D;
            double spawnZ = this.getZ() + (this.random.nextDouble() - 0.5D) * 6.0D;
            escort.moveTo(spawnX, this.getY(), spawnZ, this.random.nextFloat() * 360.0F, 0.0F);
            escort.finalizeSpawn(serverLevel, serverLevel.getCurrentDifficultyAt(BlockPos.containing(spawnX, this.getY(), spawnZ)),
                    MobSpawnType.MOB_SUMMONED, null, null);
            escort.applySpawnSpec(summonSpec, null);
            escort.initializeHostileRuntime(true, true, false);
            if (this.getTarget() != null) {
                escort.setTarget(this.getTarget());
            }
            if (!serverLevel.noCollision(escort, escort.getBoundingBox())) {
                escort.discard();
                continue;
            }
            serverLevel.addFreshEntity(escort);
            summoned = true;
        }

        return summoned;
    }

    private @Nullable LivingEntity findBossTarget() {
        LivingEntity closest = null;
        double bestDistance = Double.MAX_VALUE;
        for (LivingEntity candidate : this.level().getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(40.0D, 12.0D, 40.0D))) {
            if (!this.canEngage(candidate)) {
                continue;
            }

            double distance = this.distanceToSqr(candidate);
            if (distance < bestDistance) {
                bestDistance = distance;
                closest = candidate;
            }
        }
        return closest;
    }

    private void dropHostileLoot(int looting) {
        if (!this.isHostileVariant() || this.level().isClientSide()) {
            return;
        }

        int grudgeCount = 1 + this.random.nextInt(2 + looting);
        int ammoCount = 1 + this.random.nextInt(2 + looting);
        this.spawnAtLocation(new ItemStack(ModItems.GRUDGE.get(), grudgeCount));
        this.spawnAtLocation(new ItemStack(ModItems.AMMO.get(), ammoCount));

        if (this.hostileRuntimeState.isElite() || this.getSpec().archetype() == ShipArchetype.CARRIER || this.getSpec().archetype() == ShipArchetype.BATTLESHIP) {
            this.spawnAtLocation(new ItemStack(ModItems.ABYSSMETAL.get(), 1 + this.random.nextInt(1 + looting)));
        }

        if (this.getSpec().archetype() == ShipArchetype.INSTALLATION || this.hostileRuntimeState.isBoss()) {
            this.spawnAtLocation(new ItemStack(ModItems.ABYSSMETAL1.get(), 1 + this.random.nextInt(2 + looting)));
        }

        if (this.hostileRuntimeState.isBoss()) {
            this.spawnAtLocation(new ItemStack(ModItems.GRUDGE1.get(), 1 + this.random.nextInt(2)));
            if (this.random.nextFloat() < 0.4F) {
                this.spawnAtLocation(new ItemStack(ModItems.INSTANTCONMAT.get()));
            }
        }

        float eggDropChance = this.hostileRuntimeState.isBoss() ? 0.9F : this.hostileRuntimeState.isElite() ? 0.33F : 0.2F;
        if (this.random.nextFloat() < eggDropChance) {
            ItemStack eggDrop = this.resolveHostileEggDrop();
            if (!eggDrop.isEmpty()) {
                this.spawnAtLocation(eggDrop);
            }
        }
    }

    private ItemStack resolveHostileEggDrop() {
        ShipEntitySpec friendlySpec = ShipEntitySpecs.friendlyCounterpart(this.getSpec());
        ResourceLocation itemId = ResourceLocation.fromNamespaceAndPath("shincolle", "shipegg" + friendlySpec.eggMeta());
        Item item = BuiltInRegistries.ITEM.get(itemId);
        if (item == Items.AIR) {
            return ItemStack.EMPTY;
        }
        return new ItemStack(item);
    }

    @Override
    public void startSeenByPlayer(ServerPlayer player) {
        super.startSeenByPlayer(player);
        if (this.hostileRuntimeState.isBoss()) {
            this.ensureBossEvent();
            if (this.bossEvent != null) {
                this.bossEvent.addPlayer(player);
            }
        }
    }

    @Override
    public void stopSeenByPlayer(ServerPlayer player) {
        super.stopSeenByPlayer(player);
        if (this.bossEvent != null) {
            this.bossEvent.removePlayer(player);
        }
    }

    private void tickCommandState() {
        if (this.isOrderedToSit() || !(this.level() instanceof ServerLevel serverLevel)) {
            return;
        }

        double commandSpeed = this.getCombatMovementSpeedModifier();

        if (this.guardEntityUuid != null) {
            Entity guarded = serverLevel.getEntity(this.guardEntityUuid);
            if (guarded instanceof LivingEntity living && living.isAlive()) {
                this.getNavigation().moveTo(living, commandSpeed);
                return;
            }
            this.guardEntityUuid = null;
        }

        if (this.routeNodePos != null) {
            if (!this.dimensionMatchesCommand(serverLevel)) {
                return;
            }

            this.getNavigation().moveTo(
                    this.routeNodePos.getX() + 0.5D,
                    this.routeNodePos.getY() + 0.1D,
                    this.routeNodePos.getZ() + 0.5D,
                    commandSpeed);

            if (this.distanceToSqr(this.routeNodePos.getX() + 0.5D, this.routeNodePos.getY() + 0.1D, this.routeNodePos.getZ() + 0.5D) <= 4.0D) {
                BlockEntity blockEntity = serverLevel.getBlockEntity(this.routeNodePos);
                if (blockEntity instanceof WaypointBlockEntity waypoint) {
                    if (this.handleWaypointRouteNode(serverLevel, waypoint)) {
                        return;
                    }
                    this.advanceRouteNode(waypoint.getNextWaypoint());
                } else if (blockEntity instanceof CraneBlockEntity crane) {
                    if (this.handleCraneRouteNode(serverLevel, crane)) {
                        return;
                    }
                    this.advanceRouteNode(crane.getNextWaypoint());
                } else {
                    this.routeNodePos = null;
                }
            }
            return;
        }

        if (this.commandedPos == null || !this.dimensionMatchesCommand(serverLevel)) {
            return;
        }

        this.getNavigation().moveTo(this.commandedPos.getX() + 0.5D, this.commandedPos.getY() + 0.1D, this.commandedPos.getZ() + 0.5D, commandSpeed);
        if (this.distanceToSqr(this.commandedPos.getX() + 0.5D, this.commandedPos.getY() + 0.1D, this.commandedPos.getZ() + 0.5D) <= 2.0D) {
            this.commandedPos = null;
            this.commandDimension = "";
        }
    }

    private void advanceRouteNode(@Nullable BlockPos nextWaypoint) {
        this.routeNodePos = nextWaypoint == null ? null : nextWaypoint.immutable();
        this.commandedPos = this.routeNodePos;
        this.routeWaitTicks = 0;
        this.routeTransferCooldown = 0;
        this.routePreferLoad = true;
    }

    private boolean handleWaypointRouteNode(ServerLevel serverLevel, WaypointBlockEntity waypoint) {
        this.trySupplyAtRouteNode(serverLevel, waypoint.getPairedChest());

        if (!this.aiRespectRouteStay) {
            return false;
        }

        return this.waitAtRouteNode(waypoint.getStayTicks());
    }

    private boolean handleCraneRouteNode(ServerLevel serverLevel, CraneBlockEntity crane) {
        Container pairedContainer = this.getPairedRouteContainer(serverLevel, crane.getPairedChest());
        IFluidHandler pairedFluidHandler = this.getPairedRouteFluidHandler(serverLevel, crane.getPairedChest());
        RouteEnergyAccess pairedEnergyAccess = this.getPairedRouteEnergyAccess(serverLevel, crane.getPairedChest());
        boolean canLoad = pairedContainer != null && crane.isLoadEnabled()
                && this.hasContainerToShipTransfer(pairedContainer, stack -> crane.matchesTransferFilter(stack, true));
        boolean canUnload = pairedContainer != null && crane.isUnloadEnabled()
                && this.hasShipToContainerTransfer(pairedContainer, stack -> crane.matchesTransferFilter(stack, false));
        boolean canLoadLiquid = crane.getLiquidMode() == 1
                && this.hasPairedFluidToShipTransfer(pairedFluidHandler, pairedContainer,
                stack -> crane.matchesTransferFilter(stack, true),
                this.getRouteFluidTransferBudget());
        boolean canUnloadLiquid = crane.getLiquidMode() == 2
                && this.hasShipFluidToPairedTransfer(pairedFluidHandler, pairedContainer,
                stack -> crane.matchesTransferFilter(stack, false),
                this.getRouteFluidTransferBudget());
        boolean canLoadEnergy = crane.getEnergyMode() == 1
                && this.hasPairedEnergyToShipTransfer(pairedEnergyAccess, this.getRouteEnergyTransferBudget());
        boolean canUnloadEnergy = crane.getEnergyMode() == 2
                && this.hasShipEnergyToPairedTransfer(pairedEnergyAccess, this.getRouteEnergyTransferBudget());
        boolean hasPendingWork = canLoad || canUnload;
        hasPendingWork = hasPendingWork || canLoadLiquid || canUnloadLiquid || canLoadEnergy || canUnloadEnergy;

        if (this.routeTransferCooldown > 0) {
            this.routeTransferCooldown--;
        }

        if (hasPendingWork && this.routeTransferCooldown <= 0) {
            int moved = 0;
            if ((canLoad || canLoadLiquid || canLoadEnergy)
                    && (!canUnload && !canUnloadLiquid && !canUnloadEnergy || this.routePreferLoad)) {
                if (canLoadEnergy) {
                    moved += this.transferPairedEnergyToShip(pairedEnergyAccess, this.getRouteEnergyTransferBudget());
                }
                if (canLoadLiquid) {
                    moved += this.transferPairedFluidToShip(pairedFluidHandler, pairedContainer,
                            stack -> crane.matchesTransferFilter(stack, true),
                            this.getRouteFluidTransferBudget());
                }
                if (moved <= 0 && canLoad) {
                    moved += this.transferContainerToShipCargo(pairedContainer,
                            stack -> crane.matchesTransferFilter(stack, true),
                            this.getRouteTransferBudget());
                }
            } else if (canUnload || canUnloadLiquid || canUnloadEnergy) {
                if (canUnloadEnergy) {
                    moved += this.transferShipEnergyToPaired(pairedEnergyAccess, this.getRouteEnergyTransferBudget());
                }
                if (canUnloadLiquid) {
                    moved += this.transferShipFluidToPaired(pairedFluidHandler, pairedContainer,
                            stack -> crane.matchesTransferFilter(stack, false),
                            this.getRouteFluidTransferBudget());
                }
                if (moved <= 0 && canUnload) {
                    moved += this.transferShipCargoToContainer(pairedContainer,
                            stack -> crane.matchesTransferFilter(stack, false),
                            this.getRouteTransferBudget());
                }
            }

            if (moved > 0) {
                this.routeTransferCooldown = this.getRouteTransferInterval();
                this.routePreferLoad = !this.routePreferLoad;
                canLoad = pairedContainer != null && crane.isLoadEnabled()
                        && this.hasContainerToShipTransfer(pairedContainer, stack -> crane.matchesTransferFilter(stack, true));
                canUnload = pairedContainer != null && crane.isUnloadEnabled()
                        && this.hasShipToContainerTransfer(pairedContainer, stack -> crane.matchesTransferFilter(stack, false));
                canLoadLiquid = crane.getLiquidMode() == 1
                        && this.hasPairedFluidToShipTransfer(pairedFluidHandler, pairedContainer,
                        stack -> crane.matchesTransferFilter(stack, true),
                        this.getRouteFluidTransferBudget());
                canUnloadLiquid = crane.getLiquidMode() == 2
                        && this.hasShipFluidToPairedTransfer(pairedFluidHandler, pairedContainer,
                        stack -> crane.matchesTransferFilter(stack, false),
                        this.getRouteFluidTransferBudget());
                canLoadEnergy = crane.getEnergyMode() == 1
                        && this.hasPairedEnergyToShipTransfer(pairedEnergyAccess, this.getRouteEnergyTransferBudget());
                canUnloadEnergy = crane.getEnergyMode() == 2
                        && this.hasShipEnergyToPairedTransfer(pairedEnergyAccess, this.getRouteEnergyTransferBudget());
                hasPendingWork = canLoad || canUnload || canLoadLiquid || canUnloadLiquid || canLoadEnergy || canUnloadEnergy;
            }
        }

        if (!this.aiRespectRouteStay) {
            return false;
        }

        if (crane.getWaitMode() <= 4) {
            return hasPendingWork;
        }

        return this.waitAtRouteNode(CraneBlockEntity.getWaitTime(crane.getWaitMode()));
    }

    private boolean waitAtRouteNode(int waitTicks) {
        if (waitTicks <= 0 || this.routeWaitTicks < 0) {
            return false;
        }

        if (this.routeWaitTicks == 0) {
            this.routeWaitTicks = waitTicks;
        }

        this.routeWaitTicks--;
        if (this.routeWaitTicks <= 0) {
            this.routeWaitTicks = -1;
            return false;
        }

        return true;
    }

    private void trySupplyAtRouteNode(ServerLevel serverLevel, @Nullable BlockPos pairedChestPos) {
        if (this.routeTransferCooldown > 0) {
            this.routeTransferCooldown--;
            return;
        }

        Container pairedContainer = this.getPairedRouteContainer(serverLevel, pairedChestPos);
        if (pairedContainer == null) {
            return;
        }

        int moved = this.transferContainerToShipCargo(pairedContainer, stack -> true, Math.max(4, this.getRouteTransferBudget() / 2));
        if (moved > 0) {
            this.routeTransferCooldown = this.getRouteTransferInterval();
        }
    }

    private @Nullable BlockEntity getPairedRouteBlockEntity(ServerLevel serverLevel, @Nullable BlockPos pairedChestPos) {
        if (pairedChestPos == null) {
            return null;
        }

        return serverLevel.getBlockEntity(pairedChestPos);
    }

    private @Nullable Container getPairedRouteContainer(ServerLevel serverLevel, @Nullable BlockPos pairedChestPos) {
        BlockEntity blockEntity = this.getPairedRouteBlockEntity(serverLevel, pairedChestPos);
        return blockEntity instanceof Container container ? container : null;
    }

    private @Nullable IFluidHandler getPairedRouteFluidHandler(ServerLevel serverLevel, @Nullable BlockPos pairedChestPos) {
        BlockEntity blockEntity = this.getPairedRouteBlockEntity(serverLevel, pairedChestPos);
        if (blockEntity == null) {
            return null;
        }

        return blockEntity.getCapability(ForgeCapabilities.FLUID_HANDLER).resolve().orElse(null);
    }

    private @Nullable RouteEnergyAccess getPairedRouteEnergyAccess(ServerLevel serverLevel, @Nullable BlockPos pairedChestPos) {
        BlockEntity blockEntity = this.getPairedRouteBlockEntity(serverLevel, pairedChestPos);
        return blockEntity instanceof RouteEnergyAccess access ? access : null;
    }

    private boolean hasContainerToShipTransfer(Container source, Predicate<ItemStack> predicate) {
        for (int slot = 0; slot < source.getContainerSize(); slot++) {
            ItemStack stack = source.getItem(slot);
            if (!stack.isEmpty() && predicate.test(stack) && this.canInsertIntoShipCargo(stack)) {
                return true;
            }
        }

        return false;
    }

    private boolean hasShipToContainerTransfer(Container target, Predicate<ItemStack> predicate) {
        for (int slot = CARGO_SLOT_COUNT > 0 ? EQUIPMENT_SLOT_COUNT : 0; slot < this.shipInventory.getContainerSize(); slot++) {
            ItemStack stack = this.shipInventory.getItem(slot);
            if (!stack.isEmpty() && predicate.test(stack) && this.canInsertIntoContainer(target, stack)) {
                return true;
            }
        }

        return false;
    }

    private boolean hasPairedFluidToShipTransfer(@Nullable IFluidHandler sourceHandler,
                                                 @Nullable Container sourceContainer,
                                                 Predicate<ItemStack> predicate,
                                                 int budget) {
        return (sourceHandler != null && this.hasBlockFluidToShipTransfer(sourceHandler, predicate, budget))
                || (sourceContainer != null && this.hasContainerFluidToShipTransfer(sourceContainer, predicate, budget));
    }

    private boolean hasShipFluidToPairedTransfer(@Nullable IFluidHandler targetHandler,
                                                 @Nullable Container targetContainer,
                                                 Predicate<ItemStack> predicate,
                                                 int budget) {
        return (targetHandler != null && this.hasShipFluidToBlockTransfer(targetHandler, predicate, budget))
                || (targetContainer != null && this.hasShipFluidToContainerTransfer(targetContainer, predicate, budget));
    }

    private int transferPairedFluidToShip(@Nullable IFluidHandler sourceHandler,
                                          @Nullable Container sourceContainer,
                                          Predicate<ItemStack> predicate,
                                          int budget) {
        int moved = 0;
        if (sourceHandler != null) {
            moved += this.transferBlockFluidToShip(sourceHandler, predicate, budget);
        }
        if (moved <= 0 && sourceContainer != null) {
            moved += this.transferContainerFluidToShip(sourceContainer, predicate, budget);
        }
        return moved;
    }

    private int transferShipFluidToPaired(@Nullable IFluidHandler targetHandler,
                                          @Nullable Container targetContainer,
                                          Predicate<ItemStack> predicate,
                                          int budget) {
        int moved = 0;
        if (targetHandler != null) {
            moved += this.transferShipFluidToBlock(targetHandler, predicate, budget);
        }
        if (moved <= 0 && targetContainer != null) {
            moved += this.transferShipFluidToContainer(targetContainer, predicate, budget);
        }
        return moved;
    }

    private boolean hasBlockFluidToShipTransfer(IFluidHandler source, Predicate<ItemStack> predicate, int budget) {
        if (budget <= 0) {
            return false;
        }

        FluidStack preview = source.drain(budget, IFluidHandler.FluidAction.SIMULATE);
        if (preview.isEmpty()) {
            return false;
        }

        for (int slot = EQUIPMENT_SLOT_COUNT; slot < this.shipInventory.getContainerSize(); slot++) {
            ItemStack tankStack = this.shipInventory.getItem(slot);
            if (!predicate.test(tankStack) || !this.isRouteFluidContainer(tankStack)) {
                continue;
            }

            IFluidHandlerItem tankHandler = this.createRouteFluidHandler(tankStack);
            if (tankHandler != null && tankHandler.fill(preview, IFluidHandler.FluidAction.SIMULATE) > 0) {
                return true;
            }
        }

        return false;
    }

    private boolean hasContainerFluidToShipTransfer(Container source, Predicate<ItemStack> predicate, int budget) {
        if (budget <= 0) {
            return false;
        }

        for (int sourceSlot = 0; sourceSlot < source.getContainerSize(); sourceSlot++) {
            ItemStack sourceStack = source.getItem(sourceSlot);
            if (!predicate.test(sourceStack) || !this.isRouteFluidContainer(sourceStack)) {
                continue;
            }

            IFluidHandlerItem sourceHandler = this.createRouteFluidHandler(sourceStack);
            if (sourceHandler == null) {
                continue;
            }

            FluidStack preview = sourceHandler.drain(budget, IFluidHandler.FluidAction.SIMULATE);
            if (preview.isEmpty()) {
                continue;
            }

            for (int shipSlot = EQUIPMENT_SLOT_COUNT; shipSlot < this.shipInventory.getContainerSize(); shipSlot++) {
                ItemStack tankStack = this.shipInventory.getItem(shipSlot);
                if (!this.isRouteFluidContainer(tankStack)) {
                    continue;
                }

                IFluidHandlerItem tankHandler = this.createRouteFluidHandler(tankStack);
                if (tankHandler != null && tankHandler.fill(preview, IFluidHandler.FluidAction.SIMULATE) > 0) {
                    return true;
                }
            }
        }

        return false;
    }

    private boolean hasShipFluidToBlockTransfer(IFluidHandler target, Predicate<ItemStack> predicate, int budget) {
        if (budget <= 0) {
            return false;
        }

        for (int shipSlot = EQUIPMENT_SLOT_COUNT; shipSlot < this.shipInventory.getContainerSize(); shipSlot++) {
            ItemStack tankStack = this.shipInventory.getItem(shipSlot);
            if (!predicate.test(tankStack) || !this.isRouteFluidContainer(tankStack)) {
                continue;
            }

            IFluidHandlerItem tankHandler = this.createRouteFluidHandler(tankStack);
            if (tankHandler == null) {
                continue;
            }

            FluidStack preview = tankHandler.drain(budget, IFluidHandler.FluidAction.SIMULATE);
            if (!preview.isEmpty() && target.fill(preview, IFluidHandler.FluidAction.SIMULATE) > 0) {
                return true;
            }
        }

        return false;
    }

    private boolean hasShipFluidToContainerTransfer(Container target, Predicate<ItemStack> predicate, int budget) {
        if (budget <= 0) {
            return false;
        }

        for (int shipSlot = EQUIPMENT_SLOT_COUNT; shipSlot < this.shipInventory.getContainerSize(); shipSlot++) {
            ItemStack sourceStack = this.shipInventory.getItem(shipSlot);
            if (!predicate.test(sourceStack) || !this.isRouteFluidContainer(sourceStack)) {
                continue;
            }

            IFluidHandlerItem sourceHandler = this.createRouteFluidHandler(sourceStack);
            if (sourceHandler == null) {
                continue;
            }

            FluidStack preview = sourceHandler.drain(budget, IFluidHandler.FluidAction.SIMULATE);
            if (preview.isEmpty()) {
                continue;
            }

            for (int targetSlot = 0; targetSlot < target.getContainerSize(); targetSlot++) {
                ItemStack targetStack = target.getItem(targetSlot);
                if (!this.isRouteFluidContainer(targetStack)) {
                    continue;
                }

                IFluidHandlerItem targetHandler = this.createRouteFluidHandler(targetStack);
                if (targetHandler != null && targetHandler.fill(preview, IFluidHandler.FluidAction.SIMULATE) > 0) {
                    return true;
                }
            }
        }

        return false;
    }

    private int transferContainerToShipCargo(Container source, Predicate<ItemStack> predicate, int budget) {
        int moved = 0;

        for (int slot = 0; slot < source.getContainerSize() && moved < budget; slot++) {
            ItemStack stack = source.getItem(slot);
            if (stack.isEmpty() || !predicate.test(stack)) {
                continue;
            }

            int inserted = this.insertIntoShipCargo(stack, budget - moved);
            if (inserted <= 0) {
                continue;
            }

            stack.shrink(inserted);
            source.setChanged();
            if (stack.isEmpty()) {
                source.setItem(slot, ItemStack.EMPTY);
            } else {
                source.setItem(slot, stack);
            }
            moved += inserted;
        }

        return moved;
    }

    private int transferShipCargoToContainer(Container target, Predicate<ItemStack> predicate, int budget) {
        int moved = 0;

        for (int slot = EQUIPMENT_SLOT_COUNT; slot < this.shipInventory.getContainerSize() && moved < budget; slot++) {
            ItemStack stack = this.shipInventory.getItem(slot);
            if (stack.isEmpty() || !predicate.test(stack)) {
                continue;
            }

            int inserted = this.insertIntoContainer(target, stack, budget - moved);
            if (inserted <= 0) {
                continue;
            }

            stack.shrink(inserted);
            if (stack.isEmpty()) {
                this.shipInventory.setItem(slot, ItemStack.EMPTY);
            } else {
                this.shipInventory.setItem(slot, stack);
            }
            moved += inserted;
        }

        return moved;
    }

    private int transferBlockFluidToShip(IFluidHandler source, Predicate<ItemStack> predicate, int budget) {
        int moved = 0;

        for (int shipSlot = EQUIPMENT_SLOT_COUNT; shipSlot < this.shipInventory.getContainerSize() && moved < budget; shipSlot++) {
            ItemStack tankStack = this.shipInventory.getItem(shipSlot);
            if (!predicate.test(tankStack) || !this.isRouteFluidContainer(tankStack)) {
                continue;
            }

            IFluidHandlerItem tankHandler = this.createRouteFluidHandler(tankStack);
            if (tankHandler == null) {
                continue;
            }

            FluidStack preview = source.drain(budget - moved, IFluidHandler.FluidAction.SIMULATE);
            if (preview.isEmpty()) {
                break;
            }

            int accepted = tankHandler.fill(preview, IFluidHandler.FluidAction.SIMULATE);
            if (accepted <= 0) {
                continue;
            }

            FluidStack extracted = source.drain(accepted, IFluidHandler.FluidAction.EXECUTE);
            if (extracted.isEmpty()) {
                continue;
            }

            int filled = tankHandler.fill(extracted, IFluidHandler.FluidAction.EXECUTE);
            if (filled <= 0) {
                continue;
            }

            this.shipInventory.setItem(shipSlot, tankHandler.getContainer());
            moved += filled;
        }

        return moved;
    }

    private int transferContainerFluidToShip(Container source, Predicate<ItemStack> predicate, int budget) {
        int moved = 0;

        for (int sourceSlot = 0; sourceSlot < source.getContainerSize() && moved < budget; sourceSlot++) {
            ItemStack sourceStack = source.getItem(sourceSlot);
            if (!predicate.test(sourceStack) || !this.isRouteFluidContainer(sourceStack)) {
                continue;
            }

            IFluidHandlerItem sourceHandler = this.createRouteFluidHandler(sourceStack);
            if (sourceHandler == null) {
                continue;
            }

            FluidStack preview = sourceHandler.drain(budget - moved, IFluidHandler.FluidAction.SIMULATE);
            if (preview.isEmpty()) {
                continue;
            }

            for (int shipSlot = EQUIPMENT_SLOT_COUNT; shipSlot < this.shipInventory.getContainerSize() && moved < budget; shipSlot++) {
                ItemStack tankStack = this.shipInventory.getItem(shipSlot);
                if (!this.isRouteFluidContainer(tankStack)) {
                    continue;
                }

                IFluidHandlerItem tankHandler = this.createRouteFluidHandler(tankStack);
                if (tankHandler == null) {
                    continue;
                }

                int accepted = tankHandler.fill(preview, IFluidHandler.FluidAction.SIMULATE);
                if (accepted <= 0) {
                    continue;
                }

                FluidStack extracted = sourceHandler.drain(accepted, IFluidHandler.FluidAction.EXECUTE);
                if (extracted.isEmpty()) {
                    continue;
                }

                int filled = tankHandler.fill(extracted, IFluidHandler.FluidAction.EXECUTE);
                if (filled <= 0) {
                    continue;
                }

                source.setItem(sourceSlot, sourceHandler.getContainer());
                source.setChanged();
                this.shipInventory.setItem(shipSlot, tankHandler.getContainer());
                moved += filled;
                break;
            }
        }

        return moved;
    }

    private int transferShipFluidToBlock(IFluidHandler target, Predicate<ItemStack> predicate, int budget) {
        int moved = 0;

        for (int shipSlot = EQUIPMENT_SLOT_COUNT; shipSlot < this.shipInventory.getContainerSize() && moved < budget; shipSlot++) {
            ItemStack tankStack = this.shipInventory.getItem(shipSlot);
            if (!predicate.test(tankStack) || !this.isRouteFluidContainer(tankStack)) {
                continue;
            }

            IFluidHandlerItem tankHandler = this.createRouteFluidHandler(tankStack);
            if (tankHandler == null) {
                continue;
            }

            FluidStack preview = tankHandler.drain(budget - moved, IFluidHandler.FluidAction.SIMULATE);
            if (preview.isEmpty()) {
                continue;
            }

            int accepted = target.fill(preview, IFluidHandler.FluidAction.SIMULATE);
            if (accepted <= 0) {
                continue;
            }

            FluidStack extracted = tankHandler.drain(accepted, IFluidHandler.FluidAction.EXECUTE);
            if (extracted.isEmpty()) {
                continue;
            }

            int filled = target.fill(extracted, IFluidHandler.FluidAction.EXECUTE);
            if (filled <= 0) {
                continue;
            }

            this.shipInventory.setItem(shipSlot, tankHandler.getContainer());
            moved += filled;
        }

        return moved;
    }

    private int transferShipFluidToContainer(Container target, Predicate<ItemStack> predicate, int budget) {
        int moved = 0;

        for (int shipSlot = EQUIPMENT_SLOT_COUNT; shipSlot < this.shipInventory.getContainerSize() && moved < budget; shipSlot++) {
            ItemStack sourceStack = this.shipInventory.getItem(shipSlot);
            if (!predicate.test(sourceStack) || !this.isRouteFluidContainer(sourceStack)) {
                continue;
            }

            IFluidHandlerItem sourceHandler = this.createRouteFluidHandler(sourceStack);
            if (sourceHandler == null) {
                continue;
            }

            FluidStack preview = sourceHandler.drain(budget - moved, IFluidHandler.FluidAction.SIMULATE);
            if (preview.isEmpty()) {
                continue;
            }

            for (int targetSlot = 0; targetSlot < target.getContainerSize() && moved < budget; targetSlot++) {
                ItemStack targetStack = target.getItem(targetSlot);
                if (!this.isRouteFluidContainer(targetStack)) {
                    continue;
                }

                IFluidHandlerItem targetHandler = this.createRouteFluidHandler(targetStack);
                if (targetHandler == null) {
                    continue;
                }

                int accepted = targetHandler.fill(preview, IFluidHandler.FluidAction.SIMULATE);
                if (accepted <= 0) {
                    continue;
                }

                FluidStack extracted = sourceHandler.drain(accepted, IFluidHandler.FluidAction.EXECUTE);
                if (extracted.isEmpty()) {
                    continue;
                }

                int filled = targetHandler.fill(extracted, IFluidHandler.FluidAction.EXECUTE);
                if (filled <= 0) {
                    continue;
                }

                this.shipInventory.setItem(shipSlot, sourceHandler.getContainer());
                target.setItem(targetSlot, targetHandler.getContainer());
                target.setChanged();
                moved += filled;
                break;
            }
        }

        return moved;
    }

    private boolean hasPairedEnergyToShipTransfer(@Nullable RouteEnergyAccess source, int budget) {
        return source != null
                && budget > 0
                && this.getRouteEnergyBuffer() < this.getRouteEnergyCapacity()
                && source.extractRouteEnergy(Math.min(budget, this.getRouteEnergyCapacity() - this.getRouteEnergyBuffer()), true) > 0;
    }

    private boolean hasShipEnergyToPairedTransfer(@Nullable RouteEnergyAccess target, int budget) {
        return target != null
                && budget > 0
                && this.getRouteEnergyBuffer() > 0
                && target.receiveRouteEnergy(Math.min(budget, this.getRouteEnergyBuffer()), true) > 0;
    }

    private int transferPairedEnergyToShip(@Nullable RouteEnergyAccess source, int budget) {
        if (source == null || budget <= 0) {
            return 0;
        }

        int request = Math.min(budget, this.getRouteEnergyCapacity() - this.getRouteEnergyBuffer());
        if (request <= 0) {
            return 0;
        }

        int extracted = source.extractRouteEnergy(request, false);
        if (extracted > 0) {
            this.setRouteEnergyBuffer(this.getRouteEnergyBuffer() + extracted);
        }
        return extracted;
    }

    private int transferShipEnergyToPaired(@Nullable RouteEnergyAccess target, int budget) {
        if (target == null || budget <= 0) {
            return 0;
        }

        int request = Math.min(budget, this.getRouteEnergyBuffer());
        if (request <= 0) {
            return 0;
        }

        int accepted = target.receiveRouteEnergy(request, false);
        if (accepted > 0) {
            this.setRouteEnergyBuffer(this.getRouteEnergyBuffer() - accepted);
        }
        return accepted;
    }

    private int insertIntoShipCargo(ItemStack sourceStack, int maxItems) {
        if (sourceStack.isEmpty() || maxItems <= 0) {
            return 0;
        }

        int remaining = Math.min(maxItems, sourceStack.getCount());

        for (int slot = EQUIPMENT_SLOT_COUNT; slot < this.shipInventory.getContainerSize() && remaining > 0; slot++) {
            ItemStack current = this.shipInventory.getItem(slot);
            if (current.isEmpty() || !ItemStack.isSameItemSameTags(current, sourceStack) || current.getCount() >= current.getMaxStackSize()) {
                continue;
            }

            int insert = Math.min(remaining, current.getMaxStackSize() - current.getCount());
            current.grow(insert);
            this.shipInventory.setItem(slot, current);
            remaining -= insert;
        }

        for (int slot = EQUIPMENT_SLOT_COUNT; slot < this.shipInventory.getContainerSize() && remaining > 0; slot++) {
            ItemStack current = this.shipInventory.getItem(slot);
            if (!current.isEmpty()) {
                continue;
            }

            ItemStack inserted = sourceStack.copy();
            inserted.setCount(remaining);
            this.shipInventory.setItem(slot, inserted);
            remaining = 0;
        }

        return Math.min(maxItems, sourceStack.getCount()) - remaining;
    }

    private int insertIntoContainer(Container target, ItemStack sourceStack, int maxItems) {
        if (sourceStack.isEmpty() || maxItems <= 0) {
            return 0;
        }

        int remaining = Math.min(maxItems, sourceStack.getCount());

        for (int slot = 0; slot < target.getContainerSize() && remaining > 0; slot++) {
            ItemStack current = target.getItem(slot);
            if (current.isEmpty() || !ItemStack.isSameItemSameTags(current, sourceStack) || current.getCount() >= current.getMaxStackSize()) {
                continue;
            }

            int insert = Math.min(remaining, current.getMaxStackSize() - current.getCount());
            current.grow(insert);
            target.setItem(slot, current);
            remaining -= insert;
        }

        for (int slot = 0; slot < target.getContainerSize() && remaining > 0; slot++) {
            ItemStack current = target.getItem(slot);
            if (!current.isEmpty()) {
                continue;
            }

            ItemStack inserted = sourceStack.copy();
            inserted.setCount(remaining);
            target.setItem(slot, inserted);
            remaining = 0;
        }

        if (remaining != Math.min(maxItems, sourceStack.getCount())) {
            target.setChanged();
        }

        return Math.min(maxItems, sourceStack.getCount()) - remaining;
    }

    private boolean canInsertIntoShipCargo(ItemStack sourceStack) {
        for (int slot = EQUIPMENT_SLOT_COUNT; slot < this.shipInventory.getContainerSize(); slot++) {
            ItemStack current = this.shipInventory.getItem(slot);
            if (current.isEmpty()) {
                return true;
            }

            if (ItemStack.isSameItemSameTags(current, sourceStack) && current.getCount() < current.getMaxStackSize()) {
                return true;
            }
        }

        return false;
    }

    private boolean canInsertIntoContainer(Container target, ItemStack sourceStack) {
        for (int slot = 0; slot < target.getContainerSize(); slot++) {
            ItemStack current = target.getItem(slot);
            if (current.isEmpty()) {
                return true;
            }

            if (ItemStack.isSameItemSameTags(current, sourceStack) && current.getCount() < current.getMaxStackSize()) {
                return true;
            }
        }

        return false;
    }

    private int getRouteTransferBudget() {
        return 8 + this.equipmentBehaviorState.transportTier() * 8;
    }

    private int getRouteFluidTransferBudget() {
        return this.getRouteTransferBudget() * ROUTE_FLUID_TRANSFER_UNIT;
    }

    private int getRouteEnergyTransferBudget() {
        return this.getRouteTransferBudget() * ROUTE_ENERGY_TRANSFER_UNIT;
    }

    private int getRouteTransferInterval() {
        return Math.max(6, 20 - this.equipmentBehaviorState.transportTier() * 3);
    }

    private boolean isRouteFluidContainer(ItemStack stack) {
        if (stack.isEmpty() || stack.getCount() != 1) {
            return false;
        }

        return this.createRouteFluidHandler(stack) != null;
    }

    private @Nullable IFluidHandlerItem createRouteFluidHandler(ItemStack stack) {
        if (stack.isEmpty()) {
            return null;
        }

        return stack.copyWithCount(1).getCapability(ForgeCapabilities.FLUID_HANDLER_ITEM).resolve().orElse(null);
    }

    private void tickAutoSupportItems() {
        if (!this.aiAutoSupply || this.isHostileVariant() || this.tickCount % this.equipmentBehaviorState.supportTickInterval() != 0) {
            return;
        }

        if (this.getHealth() < this.getMaxHealth() * 0.55F && this.consumeFirstMatchingItem(ModItems.BUCKETREPAIR.get())) {
            this.heal((float) ((this.getMaxHealth() * 0.08F + 6.0F) * this.getSupportEffectMultiplier()));
            return;
        }

        if (this.hasNegativeEffects() && this.tryUseSupportItemFromInventory(action -> action.clearsNegativeStates())) {
            return;
        }

        if (this.getHealth() < this.getMaxHealth() * 0.55F
                && this.tryUseSupportItemFromInventory(action -> action.healRatio() > 0.0F || !action.effects().isEmpty())) {
            return;
        }

        if (this.getMorale() >= 4200) {
            return;
        }

        for (int slot = 0; slot < this.shipInventory.getContainerSize(); slot++) {
            ItemStack stack = this.shipInventory.getItem(slot);
            if (!(stack.getItem() instanceof CombatRationItem ration)) {
                continue;
            }

            this.addMorale(this.scaleSupportMorale(ration.getMoraleValue()));
            this.heal((float) (Math.max(1.0F, this.getMaxHealth() * 0.01F) * this.getSupportEffectMultiplier()));
            stack.shrink(1);
            if (stack.isEmpty()) {
                this.shipInventory.setItem(slot, ItemStack.EMPTY);
            } else {
                this.shipInventory.setItem(slot, stack);
            }
            return;
        }

        this.tryUseSupportItemFromInventory(action -> action.moraleGain() > 0);
    }

    private boolean dimensionMatchesCommand(ServerLevel serverLevel) {
        if (this.commandDimension == null || this.commandDimension.isBlank()) {
            return true;
        }

        return this.commandDimension.equals(serverLevel.dimension().location().toString());
    }

    private int encodeAiFlags() {
        int flags = 0;
        if (this.aiAutoTarget) {
            flags |= GameplayCommandHandler.AI_FLAG_AUTO_TARGET;
        }
        if (this.aiAllowPvp) {
            flags |= GameplayCommandHandler.AI_FLAG_ALLOW_PVP;
        }
        if (this.aiAutoSupply) {
            flags |= GameplayCommandHandler.AI_FLAG_AUTO_SUPPLY;
        }
        if (this.aiRespectRouteStay) {
            flags |= GameplayCommandHandler.AI_FLAG_ROUTE_STAY;
        }
        return flags;
    }

    private void applySyncedAiData() {
        this.applyAiData(this.entityData.get(DATA_AI_FLAGS), this.entityData.get(DATA_AI_FOLLOW_RANGE));
    }

    private void applyAiData(int flags, int followRange) {
        this.aiAutoTarget = (flags & GameplayCommandHandler.AI_FLAG_AUTO_TARGET) != 0;
        this.aiAllowPvp = (flags & GameplayCommandHandler.AI_FLAG_ALLOW_PVP) != 0;
        this.aiAutoSupply = (flags & GameplayCommandHandler.AI_FLAG_AUTO_SUPPLY) != 0;
        this.aiRespectRouteStay = (flags & GameplayCommandHandler.AI_FLAG_ROUTE_STAY) != 0;
        this.aiFollowRange = Mth.clamp(followRange, 4, 64);
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

    private void setRouteEnergyBuffer(int amount) {
        this.entityData.set(DATA_ROUTE_ENERGY, Mth.clamp(amount, 0, this.getRouteEnergyCapacity()));
    }

    private boolean hasActiveCommandState() {
        return this.commandedPos != null || this.guardEntityUuid != null || this.routeNodePos != null;
    }

    private void tickEquipmentBehaviors() {
        if (this.equipmentBehaviorState.searchlightLevel() <= 0 || this.tickCount % 20 != 0) {
            return;
        }

        if (this.level().getMaxLocalRawBrightness(this.blockPosition()) > 7) {
            return;
        }

        LivingEntity target = this.getTarget();
        int searchRange = this.equipmentBehaviorState.searchlightRange();
        if (target == null || !target.isAlive() || this.distanceToSqr(target) > searchRange * searchRange || !this.canEngage(target)) {
            target = this.findSearchlightTarget(searchRange);
        }

        if (target != null) {
            this.applyIllumination(target, this.equipmentBehaviorState.searchlightDurationTicks());
            this.emitIlluminationFx(target, false);
        }
    }

    private @Nullable LivingEntity findSearchlightTarget(int range) {
        LivingEntity closest = null;
        double bestDistance = Double.MAX_VALUE;

        for (LivingEntity candidate : this.level().getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(range, 4.0D, range))) {
            if (!this.canEngage(candidate)) {
                continue;
            }

            double distance = this.distanceToSqr(candidate);
            if (distance < bestDistance) {
                bestDistance = distance;
                closest = candidate;
            }
        }

        return closest;
    }

    private void applyIllumination(LivingEntity target, int durationTicks) {
        target.addEffect(new MobEffectInstance(MobEffects.GLOWING, durationTicks, 0, false, true, true));
    }

    private void emitIlluminationFx(LivingEntity target, boolean flareSource) {
        if (this.level().isClientSide()) {
            return;
        }

        CombatFxDispatcher.sendParticle(target,
                flareSource ? GameplayParticleType.FLARE_BURST : GameplayParticleType.SEARCHLIGHT_MARK,
                target.getX(), target.getY() + target.getBbHeight() * 0.85D, target.getZ());
    }

    private void tickMarriageBond() {
        if (!this.isMarried() || this.tickCount % 200 != 0) {
            return;
        }

        if (this.getMorale() < 12000) {
            this.addMorale(24);
        }

        if (this.getHealth() < this.getMaxHealth()) {
            this.heal(Math.max(1.0F, this.getMaxHealth() * 0.01F));
        }
    }

    private void tickLegacyRingPassives() {
        if (!this.isMarried() || this.isHostileVariant() || this.tickCount % 128 != 0) {
            return;
        }

        LegacyShipBehaviorCatalog.tickMarriagePassive(this);
    }

    void applyCarrierRingAura(int durationTicks, int amplifier) {
        for (LegacyShipEntity ship : this.level().getEntitiesOfClass(LegacyShipEntity.class, this.getBoundingBox().inflate(16.0D))) {
            if (!ship.isHostileVariant() && (ship == this || this.isAlliedTo(ship))) {
                this.applyLegacyRingEffect(ship, MobEffects.JUMP, durationTicks, amplifier);
            }
        }
    }

    void applyOwnerRingEffect(MobEffect effect, int durationTicks, int amplifier) {
        Player owner = this.getOwnerPlayer();
        if (owner != null && this.distanceToSqr(owner) < 256.0D) {
            this.applyLegacyRingEffect(owner, effect, durationTicks, amplifier);
        }
    }

    void applyLegacyRingEffect(LivingEntity target, MobEffect effect, int durationTicks, int amplifier) {
        target.addEffect(new MobEffectInstance(effect, durationTicks, amplifier, false, false));
    }

    private double getSupportEffectMultiplier() {
        return this.equipmentBehaviorState.supportEffectMultiplier();
    }

    private int scaleSupportMorale(int baseValue) {
        if (baseValue == 0) {
            return 0;
        }

        if (baseValue < 0) {
            return baseValue;
        }

        double scaled = baseValue * this.getSupportEffectMultiplier();
        return Math.max(1, Mth.floor((float) scaled));
    }

    private void resetOwnershipBoundState() {
        this.clearCommandState();
        this.getNavigation().stop();
        this.setTarget(null);
        this.setOrderedToSit(false);
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
                return InteractionResult.CONSUME;
            }

            return InteractionResult.PASS;
        }

        if (this.tryApplySupportItem(player, hand, stack)) {
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
                || stack.is(ModItems.POINTERITEM.get())
                || this.resolveSupportItemAction(stack) != null;
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

        this.addMorale(this.scaleSupportMorale(ration.getMoraleValue()));
        this.heal((float) (Math.max(1.0F, this.getMaxHealth() * 0.025F) * this.getSupportEffectMultiplier()));

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

    private boolean tryApplySupportItem(Player player, InteractionHand hand, ItemStack stack) {
        ShipSupportAction action = this.resolveSupportItemAction(stack);
        if (action == null) {
            return false;
        }

        return this.applySupportItemAction(action,
                () -> this.consumeHeldItem(player, hand, 1),
                player);
    }

    private boolean tryUseSupportItemFromInventory(java.util.function.Predicate<ShipSupportAction> predicate) {
        for (int slot = 0; slot < this.shipInventory.getContainerSize(); slot++) {
            ItemStack stack = this.shipInventory.getItem(slot);
            ShipSupportAction action = this.resolveSupportItemAction(stack);
            if (action == null || !predicate.test(action)) {
                continue;
            }

            int resolvedSlot = slot;
            if (this.applySupportItemAction(action, () -> this.consumeInventorySlot(resolvedSlot), null)) {
                return true;
            }
        }

        return false;
    }

    private boolean applySupportItemAction(ShipSupportAction action, @Nullable Runnable consumeAction, @Nullable Player feedbackPlayer) {
        boolean canImproveMorale = action.moraleGain() > 0 && this.getMorale() < MAX_MORALE;
        boolean canReduceMorale = action.moraleGain() < 0;
        boolean canHeal = action.healRatio() > 0.0F && this.getHealth() < this.getMaxHealth();
        boolean canClearAll = action.clearAllEffects() && !this.getActiveEffects().isEmpty();
        boolean canClearNegative = action.clearNegativeEffects() && this.hasNegativeEffects();
        boolean canApplyEffects = !action.effects().isEmpty();

        if (!canImproveMorale && !canReduceMorale && !canHeal && !canClearAll && !canClearNegative && !canApplyEffects) {
            if (feedbackPlayer != null && action.moraleGain() > 0) {
                feedbackPlayer.displayClientMessage(Component.translatable("chat.shincolle.ship.feed_full",
                        this.getName().copy().withStyle(ChatFormatting.GRAY)), true);
            }
            return false;
        }

        if (action.clearAllEffects()) {
            this.removeAllEffects();
        } else if (action.clearNegativeEffects()) {
            this.clearNegativeEffects();
        }

        for (MobEffectInstance effect : action.effects()) {
            this.applySupportEffect(effect);
        }

        if (action.healRatio() > 0.0F && this.getHealth() < this.getMaxHealth()) {
            this.heal((float) Math.max(1.0F, this.getMaxHealth() * action.healRatio() * this.getSupportEffectMultiplier()));
        }

        if (action.moraleGain() != 0) {
            this.addMorale(this.scaleSupportMorale(action.moraleGain()));
        }

        if (consumeAction != null) {
            consumeAction.run();
        }

        if (feedbackPlayer != null) {
            ShinColleSoundHelper.playShipVoice(this.level(), feedbackPlayer, ShipSoundType.FEED, 0.62F,
                    ShinColleSoundHelper.variedPitch(feedbackPlayer, 1.0F, 0.08F));
            feedbackPlayer.displayClientMessage(Component.translatable("chat.shincolle.ship.feed",
                    this.getName().copy().withStyle(ChatFormatting.LIGHT_PURPLE), this.getMorale()), true);
        }

        return true;
    }

    private @Nullable ShipSupportAction resolveSupportItemAction(ItemStack stack) {
        if (stack.isEmpty()) {
            return null;
        }

        if (stack.is(ModItems.GRUDGE.get())) {
            return new ShipSupportAction(240, 0.0F, false, false,
                    List.of(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 20 * 25, 0)));
        }
        if (stack.is(ModItems.GRUDGE1.get())) {
            return new ShipSupportAction(420, 0.0F, false, false,
                    List.of(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 20 * 40, 1)));
        }
        if (stack.is(ModItems.AMMO.get())) {
            return new ShipSupportAction(140, 0.0F, false, false,
                    List.of(new MobEffectInstance(MobEffects.LUCK, 20 * 25, 0)));
        }
        if (stack.is(ModItems.AMMO1.get())) {
            return new ShipSupportAction(220, 0.0F, false, false,
                    List.of(new MobEffectInstance(MobEffects.LUCK, 20 * 40, 0)));
        }
        if (stack.is(ModItems.AMMO2.get())) {
            return new ShipSupportAction(180, 0.0F, false, false,
                    List.of(new MobEffectInstance(MobEffects.LUCK, 20 * 30, 1)));
        }
        if (stack.is(ModItems.AMMO3.get())) {
            return new ShipSupportAction(280, 0.0F, false, false,
                    List.of(new MobEffectInstance(MobEffects.LUCK, 20 * 50, 1)));
        }
        if (stack.is(ModItems.ABYSSMETAL.get())) {
            return new ShipSupportAction(180, 0.08F, false, false, List.of());
        }
        if (stack.is(ModItems.ABYSSMETAL1.get())) {
            return new ShipSupportAction(220, 0.04F, false, false,
                    List.of(new MobEffectInstance(MobEffects.ABSORPTION, 20 * 45, 0)));
        }
        if (stack.is(ModItems.TOYAIRPLANE.get())) {
            return new ShipSupportAction(360, 0.0F, false, false,
                    List.of(new MobEffectInstance(MobEffects.LUCK, 20 * 50, 1),
                            new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 20 * 40, 0)));
        }
        if (stack.is(Items.MILK_BUCKET)) {
            return new ShipSupportAction(80, 0.02F, true, false, List.of());
        }
        if (stack.is(Items.HONEY_BOTTLE)) {
            return new ShipSupportAction(120, 0.01F, false, true, List.of());
        }
        if (stack.is(Items.POTION) || stack.is(Items.SPLASH_POTION) || stack.is(Items.LINGERING_POTION)) {
            List<MobEffectInstance> effects = copyEffectList(PotionUtils.getMobEffects(stack));
            if (effects.isEmpty()) {
                return null;
            }

            int beneficialEffects = 0;
            int harmfulEffects = 0;
            for (MobEffectInstance effect : effects) {
                MobEffectCategory category = effect.getEffect().getCategory();
                if (category == MobEffectCategory.BENEFICIAL) {
                    beneficialEffects++;
                } else if (category == MobEffectCategory.HARMFUL) {
                    harmfulEffects++;
                }
            }

            int moraleGain = beneficialEffects * 80 - harmfulEffects * 60;
            return new ShipSupportAction(moraleGain, 0.0F, false, false, effects);
        }

        FoodProperties foodProperties = stack.getFoodProperties(this);
        if (foodProperties == null) {
            return null;
        }

        List<MobEffectInstance> foodEffects = new ArrayList<>();
        for (Pair<MobEffectInstance, Float> effectEntry : foodProperties.getEffects()) {
            if (this.random.nextFloat() <= effectEntry.getSecond()) {
                foodEffects.add(new MobEffectInstance(effectEntry.getFirst()));
            }
        }

        int moraleGain = Math.max(70, Math.round(foodProperties.getNutrition() * 18.0F
                + foodProperties.getSaturationModifier() * 180.0F));
        float healRatio = Mth.clamp(foodProperties.getNutrition() * 0.003F
                + foodProperties.getSaturationModifier() * 0.015F, 0.01F, 0.05F);
        return new ShipSupportAction(moraleGain, healRatio, false, false, foodEffects);
    }

    private void applySupportEffect(MobEffectInstance effect) {
        MobEffect mobEffect = effect.getEffect();
        if (mobEffect.isInstantenous()) {
            mobEffect.applyInstantenousEffect(null, null, this, effect.getAmplifier(), 1.0D);
            return;
        }

        this.addEffect(new MobEffectInstance(effect));
    }

    private boolean hasNegativeEffects() {
        for (MobEffectInstance effect : this.getActiveEffects()) {
            if (effect.getEffect().getCategory() == MobEffectCategory.HARMFUL) {
                return true;
            }
        }

        return false;
    }

    private void clearNegativeEffects() {
        List<MobEffect> harmfulEffects = new ArrayList<>();

        for (MobEffectInstance effect : this.getActiveEffects()) {
            if (effect.getEffect().getCategory() == MobEffectCategory.HARMFUL) {
                harmfulEffects.add(effect.getEffect());
            }
        }

        for (MobEffect effect : harmfulEffects) {
            this.removeEffect(effect);
        }
    }

    private static List<MobEffectInstance> copyEffectList(List<MobEffectInstance> effects) {
        List<MobEffectInstance> copiedEffects = new ArrayList<>(effects.size());

        for (MobEffectInstance effect : effects) {
            copiedEffects.add(new MobEffectInstance(effect));
        }

        return copiedEffects;
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

        ItemStack heldStack = player.getItemInHand(hand);
        if (heldStack.isEmpty()) {
            return;
        }

        ItemStack remainder = this.createUseRemainder(heldStack);
        if (heldStack.getCount() <= amount) {
            if (!remainder.isEmpty()) {
                player.setItemInHand(hand, remainder);
            } else {
                heldStack.shrink(amount);
                if (heldStack.isEmpty()) {
                    player.setItemInHand(hand, ItemStack.EMPTY);
                }
            }
            return;
        }

        heldStack.shrink(amount);
        if (!remainder.isEmpty() && !player.getInventory().add(remainder)) {
            player.drop(remainder, false);
        }
    }

    private void consumeInventorySlot(int slot) {
        ItemStack stack = this.shipInventory.getItem(slot);
        if (stack.isEmpty()) {
            return;
        }

        stack.shrink(1);
        if (stack.isEmpty()) {
            this.shipInventory.setItem(slot, ItemStack.EMPTY);
            return;
        }

        this.shipInventory.setItem(slot, stack);
    }

    private ItemStack createUseRemainder(ItemStack stack) {
        if (stack.is(Items.MILK_BUCKET)) {
            return new ItemStack(Items.BUCKET);
        }
        if (stack.is(Items.HONEY_BOTTLE)
                || stack.is(Items.POTION)
                || stack.is(Items.SPLASH_POTION)
                || stack.is(Items.LINGERING_POTION)) {
            return new ItemStack(Items.GLASS_BOTTLE);
        }
        if (stack.is(Items.MUSHROOM_STEW)
                || stack.is(Items.RABBIT_STEW)
                || stack.is(Items.BEETROOT_SOUP)
                || stack.is(Items.SUSPICIOUS_STEW)) {
            return new ItemStack(Items.BOWL);
        }

        return ItemStack.EMPTY;
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

    private record ShipSupportAction(
            int moraleGain,
            float healRatio,
            boolean clearAllEffects,
            boolean clearNegativeEffects,
            List<MobEffectInstance> effects) {

        private boolean clearsNegativeStates() {
            return this.clearAllEffects || this.clearNegativeEffects;
        }
    }
}
