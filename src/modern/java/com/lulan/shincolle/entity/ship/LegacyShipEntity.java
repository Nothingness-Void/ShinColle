package com.lulan.shincolle.entity.ship;

import com.lulan.shincolle.blockentity.CraneBlockEntity;
import com.lulan.shincolle.blockentity.RouteNode;
import com.lulan.shincolle.blockentity.WaypointBlockEntity;
import com.mojang.datafixers.util.Pair;
import com.lulan.shincolle.client.ClientShipVisibilityHelper;
import com.lulan.shincolle.config.ShinColleConfig;
import com.lulan.shincolle.world.LightBlockHelper;
import com.lulan.shincolle.entity.other.LegacyShipFishingHookEntity;
import com.lulan.shincolle.entity.ship.goal.LegacyShipFleeGoal;
import com.lulan.shincolle.entity.ship.path.ShipNavigationHost;
import com.lulan.shincolle.entity.ship.path.ShipPathNavigate;
import com.lulan.shincolle.entity.ship.path.ShipMoveHelper;
import com.lulan.shincolle.entity.ship.goal.LegacyShipFloatGoal;
import com.lulan.shincolle.entity.ship.goal.LegacyShipFollowOwnerGoal;
import com.lulan.shincolle.item.LegacyEntityItemMarker;
import com.lulan.shincolle.entity.ship.goal.LegacyShipGuardGoal;
import com.lulan.shincolle.entity.ship.goal.LegacyShipAttackOnCollideGoal;
import com.lulan.shincolle.entity.ship.goal.LegacyShipOpenDoorGoal;
import com.lulan.shincolle.entity.ship.goal.LegacyShipWanderGoal;
import com.lulan.shincolle.entity.ship.goal.LegacyShipPickItemGoal;
import com.lulan.shincolle.entity.ship.goal.LegacyShipRangedAttackGoal;
import com.lulan.shincolle.entity.ship.goal.LegacyShipRangeTargetGoal;
import com.lulan.shincolle.entity.projectile.LegacyShipBeamEntity;
import com.lulan.shincolle.entity.projectile.LegacyShipFloatingFortEntity;
import com.lulan.shincolle.entity.projectile.LegacyShipProjectileProfile;
import com.lulan.shincolle.entity.projectile.LegacyShipProjectileMoveType;
import com.lulan.shincolle.entity.projectile.LegacyShipProjectileVisual;
import com.lulan.shincolle.entity.projectile.LegacyShipProjectileEntity;
import com.lulan.shincolle.entity.mount.LegacyMountEntity;
import com.lulan.shincolle.entity.mount.LegacyShipMountEntity;
import com.lulan.shincolle.item.CombatRationItem;
import com.lulan.shincolle.item.LegacyShipSupportItem;
import com.lulan.shincolle.item.LegacyShipSpawnEggItem;
import com.lulan.shincolle.item.OwnerPaperItem;
import com.lulan.shincolle.item.PointerItem;
import com.lulan.shincolle.item.equipment.LegacyEquipmentFamily;
import com.lulan.shincolle.item.equipment.LegacyEquipmentItem;
import com.lulan.shincolle.combat.WorldCombatRulesSavedData;
import com.lulan.shincolle.menu.ShipInventoryMenu;
import com.lulan.shincolle.network.CombatFxDispatcher;
import com.lulan.shincolle.network.CombatReactType;
import com.lulan.shincolle.network.GameplayCommandHandler;
import com.lulan.shincolle.network.GameplayParticleType;
import com.lulan.shincolle.registry.ModItems;
import com.lulan.shincolle.registry.ModEntityTypes;
import com.lulan.shincolle.registry.ModBlocks;
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
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.DamageTypeTags;
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
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ExperienceOrb;
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
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.Slime;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ThrownPotion;
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
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.network.NetworkHooks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Predicate;

public class LegacyShipEntity extends PathfinderMob implements ShipNavigationHost {
    private final ShipPathNavigate shipNavigate = new ShipPathNavigate(this);
    private final ShipMoveHelper shipMoveHelper = new ShipMoveHelper(this, 60F);

    @Override
    public ShipPathNavigate getShipNavigate() {
        return this.shipNavigate;
    }

    @Override
    public ShipMoveHelper getShipMoveHelper() {
        return this.shipMoveHelper;
    }

    @Override
    public boolean canFly() {
        return !this.isHostileVariant() && this.hasEffect(MobEffects.LEVITATION);
    }

    @Override
    public boolean isShipJumping() {
        return this.jumping;
    }

    @Override
    public void aiStep() {
        if (!this.level().isClientSide()) {
            ShipNavigationHost.update(this, this.isOrderedToSit());
        }
        super.aiStep();
    }

    @Override
    public void travel(Vec3 input) {
        if (this.isEffectiveAi()) {
            ShipNavigationHost.travel(this, input);
        } else {
            super.travel(input);
        }
    }

    public enum CompatAttackFailureReason {
        NONE(""),
        TARGET_INVALID("chat.shincolle.skill.reason.target_invalid"),
        SKILL_UNAVAILABLE("chat.shincolle.skill.reason.skill_unavailable"),
        COOLDOWN("chat.shincolle.skill.reason.cooldown"),
        OUT_OF_RANGE("chat.shincolle.skill.reason.out_of_range"),
        NO_GRUDGE("chat.shincolle.skill.reason.no_grudge"),
        NO_LIGHT_AMMO("chat.shincolle.skill.reason.no_light_ammo"),
        NO_HEAVY_AMMO("chat.shincolle.skill.reason.no_heavy_ammo"),
        NO_AIRCRAFT("chat.shincolle.skill.reason.no_aircraft");

        private final String translationKey;

        CompatAttackFailureReason(String translationKey) {
            this.translationKey = translationKey;
        }

        public String translationKey() {
            return this.translationKey;
        }

        public Component message() {
            return this.translationKey.isEmpty() ? Component.empty() : Component.translatable(this.translationKey);
        }
    }

    private static final String VARIANT_EGG_META_TAG = "VariantEggMeta";
    private static final String OWNER_UUID_TAG = "OwnerUuid";
    private static final String OWNER_NAME_TAG = "OwnerName";
    private static final String OWNER_UID_TAG = "OwnerUID";
    private static final String ORDERED_TO_SIT_TAG = "OrderedToSit";
    private static final String SHIP_LEVEL_TAG = "ShipLevel";
    private static final String SHIP_EXPERIENCE_TAG = "ShipExperience";
    private static final String SHIP_MORALE_TAG = "ShipMorale";
    private static final String SHIP_LIGHT_AMMO_TAG = "ShipLightAmmo";
    private static final String SHIP_HEAVY_AMMO_TAG = "ShipHeavyAmmo";
    private static final String SHIP_AIRCRAFT_LIGHT_TAG = "ShipAircraftLight";
    private static final String SHIP_AIRCRAFT_HEAVY_TAG = "ShipAircraftHeavy";
    private static final String SHIP_GRUDGE_TAG = "ShipGrudge";
    private static final String LEGACY_LIGHT_AMMO_TAG = "NumAmmoLight";
    private static final String LEGACY_HEAVY_AMMO_TAG = "NumAmmoHeavy";
    private static final String LEGACY_AIRCRAFT_LIGHT_TAG = "NumAirL";
    private static final String LEGACY_AIRCRAFT_HEAVY_TAG = "NumAirH";
    private static final String LEGACY_GRUDGE_TAG = "NumGrudge";
    private static final String SHIP_MARRIED_TAG = "ShipMarried";
    private static final String WED_EFFECT_TAG = "WedEffect";
    private static final String MODERN_HEALTH_TAG = "ModernHealth";
    private static final String MODERN_ATTACK_TAG = "ModernAttack";
    private static final String MODERN_DEFENSE_TAG = "ModernDefense";
    private static final String MODERN_ATTACK_SPEED_TAG = "ModernAttackSpeed";
    private static final String MODERN_SPEED_TAG = "ModernSpeed";
    private static final String MODERN_RANGE_TAG = "ModernRange";
    private static final String SHIP_UID_TAG = "ShipUID";
    private static final String COMMAND_POS_TAG = "CommandPos";
    private static final String COMMAND_DIM_TAG = "CommandDim";
    private static final String GUARD_ENTITY_TAG = "GuardEntity";
    private static final String GUARD_BLOCK_POS_TAG = "GuardBlockPos";
    private static final String GUARD_BLOCK_DIM_TAG = "GuardBlockDim";
    private static final String ROUTE_NODE_TAG = "RouteNode";
    private static final String ROUTE_WAIT_TAG = "RouteWait";
    private static final String SHIP_WP_STAY_TAG = "ShipWpStay";
    private static final String AI_AUTO_TARGET_TAG = "AiAutoTarget";
    private static final String AI_ON_SIGHT_CHASE_TAG = "AiOnSightChase";
    private static final String AI_PVP_FIRST_TAG = "AiPvpFirst";
    private static final String AI_ANTI_AIR_TAG = "AiAntiAir";
    private static final String AI_ANTI_SUB_TAG = "AiAntiSub";
    private static final String AI_PICK_ITEM_TAG = "AiPickItem";
    private static final String FOLLOW_MIN_TAG = "FMin";
    private static final String FOLLOW_MAX_TAG = "FMax";
    private static final String SHIP_INVENTORY_TAG = "ShipInventory";
    private static final String SHIP_INVENTORY_SLOT_TAG = "Slot";
    private static final String HOSTILE_RUNTIME_TAG = "HostileRuntime";
    private static final String HOSTILE_SCALE_TAG = "scaleLV";
    private static final String LEGACY_EXTENDED_PROPERTIES_TAG = "ShipExtProps";
    private static final String LEGACY_MINOR_TAG = "Minor";
    private static final String LEGACY_OWNER_NAME_TAG = "Owner";
    private static final String LEGACY_LEVEL_TAG = "Level";
    private static final String LEGACY_EXPERIENCE_TAG = "Exp";
    private static final String LEGACY_LIGHT_AMMO_MINOR_TAG = "NumAmmoL";
    private static final String LEGACY_HEAVY_AMMO_MINOR_TAG = "NumAmmoH";
    private static final String LEGACY_GRUDGE_MINOR_TAG = "NumGrudge";
    private static final String LEGACY_GUARD_X_TAG = "GuardX";
    private static final String LEGACY_GUARD_Y_TAG = "GuardY";
    private static final String LEGACY_GUARD_Z_TAG = "GuardZ";
    private static final String LEGACY_GUARD_DIM_TAG = "GuardDim";
    private static final String LEGACY_GUARD_TYPE_TAG = "GuardType";
    private static final int LEGACY_GUARD_BLOCK_TYPE = 1;
    private static final String LEGACY_OWNER_UID_TAG = "PlayerUID";
    private static final String LEGACY_SHIP_UID_TAG = "ShipUID";
    private static final String LEGACY_MORALE_TAG = "Morale";
    private static final String LEGACY_WP_STAY_TAG = "WpStay";
    private static final String LEGACY_FLAGS_TAG = "ShipFlags";
    private static final String LEGACY_ON_SIGHT_CHASE_FLAG_TAG = "OnSight";
    private static final String LEGACY_PVP_FIRST_FLAG_TAG = "PVPFirst";
    private static final String LEGACY_ANTI_AIR_FLAG_TAG = "AA";
    private static final String LEGACY_ANTI_SUB_FLAG_TAG = "ASM";
    private static final String LEGACY_CAN_FLOAT_FLAG_TAG = "CanFloat";
    private static final String LEGACY_CAN_FOLLOW_FLAG_TAG = "CanFollow";
    private static final String LEGACY_CAN_DROP_FLAG_TAG = "CanDrop";
    private static final String LEGACY_HELD_ITEM_FLAG_TAG = "HeldItem";
    private static final String LEGACY_MARRIED_FLAG_TAG = "IsMarried";
    private static final String LEGACY_RING_EFFECT_FLAG_TAG = "WedEffect";
    private static final String LEGACY_PICK_ITEM_FLAG_TAG = "PickItem";
    private static final String LEGACY_TIME_KEEPER_FLAG_TAG = "TimeKeeper";
    private static final String SHIP_TIME_KEEPER_TAG = "ShipTimeKeeper";
    private static final String LEGACY_AUTO_PUMP_FLAG_TAG = "AutoPump";
    private static final String SHIP_AUTO_PUMP_TAG = "ShipAutoPump";
    private static final String LEGACY_COMBAT_RATION_LEVEL_TAG = "AutoCR";
    private static final String LEGACY_FLEE_HP_TAG = "FHP";
    private static final String LEGACY_FOOD_SATURATION_TAG = "Food";
    private static final String SHIP_CAN_FLOAT_TAG = "ShipCanFloat";
    private static final String SHIP_CAN_FOLLOW_TAG = "ShipCanFollow";
    private static final String SHIP_CAN_DROP_TAG = "ShipCanDrop";
    private static final String SHIP_SHOW_HELD_ITEM_TAG = "ShipShowHeldItem";
    private static final String LEGACY_TASK_TAG = "Task";
    private static final String LEGACY_TASK_SIDE_TAG = "Side";
    private static final String LEGACY_DISPLAY_TAG = "Display";
    private static final String LEGACY_MODEL_STATE_TAG = "State";
    private static final String LEGACY_HP_STATE_TAG = "HPState";
    private static final String LEGACY_EMOTION_TAG = "Emotion";
    private static final String LEGACY_EMOTION2_TAG = "Emotion2";
    private static final String LEGACY_EMOTION4_TAG = "Emotion4";
    private static final String LEGACY_PHASE_TAG = "Phase";
    private static final String SHIP_COMBAT_RATION_LEVEL_TAG = "ShipCombatRationLevel";
    private static final String SHIP_FLEE_HP_TAG = "ShipFleeHp";
    private static final String SHIP_FOOD_SATURATION_TAG = "ShipFoodSaturation";
    private static final String SHIP_TASK_TAG = "ShipTask";
    private static final String SHIP_TASK_SIDE_TAG = "ShipTaskSide";
    private static final String SHIP_TASK_TIME_TAG = "ShipTaskTime";
    private static final String SHIP_MODEL_STATE_TAG = "ShipModelState";
    private static final String SHIP_HP_STATE_TAG = "ShipHpState";
    private static final String SHIP_EMOTION_TAG = "ShipEmotion";
    private static final String SHIP_EMOTION2_TAG = "ShipEmotion2";
    private static final String SHIP_EMOTION4_TAG = "ShipEmotion4";
    private static final String SHIP_PHASE_TAG = "ShipPhase";
    private static final String SHIP_RENSOUHOU_COUNT_TAG = "ShipRensouhouCount";
    private static final String SHIP_SKILL_TIMER_TAG = "ShipSkillTimer";
    private static final String SHIP_SKILL_REMAINING_TAG = "ShipSkillRemaining";
    private static final String SHIP_SKILL_MOTION_X_TAG = "ShipSkillMotionX";
    private static final String SHIP_SKILL_MOTION_Y_TAG = "ShipSkillMotionY";
    private static final String SHIP_SKILL_MOTION_Z_TAG = "ShipSkillMotionZ";
    private static final String SHIP_SKILL_TARGET_TAG = "ShipSkillTarget";
    private static final String LEGACY_RENSOUHOU_COUNT_TAG = "NumRensouhou";
    private static final String LEGACY_USE_MELEE_FLAG_TAG = "Melee";
    private static final String LEGACY_USE_LIGHT_FLAG_TAG = "AmmoL";
    private static final String LEGACY_USE_HEAVY_FLAG_TAG = "AmmoH";
    private static final String LEGACY_USE_AIR_LIGHT_FLAG_TAG = "AirL";
    private static final String LEGACY_USE_AIR_HEAVY_FLAG_TAG = "AirH";
    private static final String LEGACY_PASSIVE_AI_FLAG_TAG = "PassiveAI";
    private static final String SHIP_USE_MELEE_TAG = "ShipUseMelee";
    private static final String SHIP_USE_LIGHT_TAG = "ShipUseAmmoLight";
    private static final String SHIP_USE_HEAVY_TAG = "ShipUseAmmoHeavy";
    private static final String SHIP_USE_AIR_LIGHT_TAG = "ShipUseAirLight";
    private static final String SHIP_USE_AIR_HEAVY_TAG = "ShipUseAirHeavy";

    private static final int DEFAULT_LEVEL = 1;
    private static final int HOSTILE_LEVEL = 150;
    private static final int DEFAULT_MORALE = 60;
    private static final int MAX_MORALE = 16000;
    private static final int DEFAULT_LIGHT_AMMO = 0;
    private static final int DEFAULT_HEAVY_AMMO = 0;
    private static final int DEFAULT_GRUDGE = 0;
    public static final int DEFAULT_FOLLOW_MAX = 12;
    public static final int DEFAULT_FOLLOW_MIN = 3;
    private static final boolean DEFAULT_AI_ON_SIGHT_CHASE = true;
    public static final boolean DEFAULT_AI_PVP_FIRST = true;
    public static final boolean DEFAULT_AI_ANTI_AIR = false;
    public static final boolean DEFAULT_AI_ANTI_SUB = false;
    public static final boolean DEFAULT_CAN_FLOAT_UP = false;
    public static final boolean DEFAULT_CAN_FOLLOW = false;
    public static final boolean DEFAULT_CAN_DROP_ITEM = true;
    public static final boolean DEFAULT_SHOW_HELD_ITEM = true;
    public static final int DEFAULT_COMBAT_RATION_LEVEL = 3;
    public static final int DEFAULT_FLEE_HP = 35;
    public static final int DEFAULT_TASK = -1;
    public static final int DEFAULT_TASK_SIDE = -1;
    public static final int DEFAULT_MODEL_STATE = 0;
    public static final int DEFAULT_HP_STATE = 0;
    public static final int DEFAULT_EMOTION = 0;
    public static final int DEFAULT_EMOTION2 = 0;
    public static final int DEFAULT_EMOTION4 = 0;
    public static final int DEFAULT_DISPLAY_PHASE = 0;
    public static final boolean DEFAULT_USE_MELEE = false;
    public static final boolean DEFAULT_USE_LIGHT = true;
    public static final boolean DEFAULT_USE_HEAVY = true;
    public static final boolean DEFAULT_USE_AIR_LIGHT = true;
    public static final boolean DEFAULT_USE_AIR_HEAVY = true;
    public static final int DEFAULT_WP_STAY_MODE = 0;
    public static final int MAX_WP_STAY_MODE = 16;
    public static final int MODERN_LIMIT = 3;
    private static final int MODERN_ATTRIBUTE_COUNT = 6;
    public static final int AUTO_PUMP_WATER_DEPTH = 12;
    private static final int AUTO_PUMP_LAVA_DEPTH = 8;
    private static final int AUTO_PUMP_BUCKET_AMOUNT = 1000;

    private static final EntityDataAccessor<Integer> DATA_VARIANT_EGG_META =
            SynchedEntityData.defineId(LegacyShipEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_HOSTILE_SCALE =
            SynchedEntityData.defineId(LegacyShipEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Optional<UUID>> DATA_OWNER_UUID =
            SynchedEntityData.defineId(LegacyShipEntity.class, EntityDataSerializers.OPTIONAL_UUID);
    private static final EntityDataAccessor<String> DATA_OWNER_NAME =
            SynchedEntityData.defineId(LegacyShipEntity.class, EntityDataSerializers.STRING);
    private static final EntityDataAccessor<Boolean> DATA_ORDERED_TO_SIT =
            SynchedEntityData.defineId(LegacyShipEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> DATA_LEVEL =
            SynchedEntityData.defineId(LegacyShipEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_EXPERIENCE =
            SynchedEntityData.defineId(LegacyShipEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_MORALE =
            SynchedEntityData.defineId(LegacyShipEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_COMBAT_RATION_LEVEL =
            SynchedEntityData.defineId(LegacyShipEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_FLEE_HP =
            SynchedEntityData.defineId(LegacyShipEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_FOOD_SATURATION =
            SynchedEntityData.defineId(LegacyShipEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_LIGHT_AMMO =
            SynchedEntityData.defineId(LegacyShipEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_HEAVY_AMMO =
            SynchedEntityData.defineId(LegacyShipEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_AIRCRAFT_LIGHT =
            SynchedEntityData.defineId(LegacyShipEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_AIRCRAFT_HEAVY =
            SynchedEntityData.defineId(LegacyShipEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_GRUDGE =
            SynchedEntityData.defineId(LegacyShipEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> DATA_MARRIED =
            SynchedEntityData.defineId(LegacyShipEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> DATA_RING_EFFECT =
            SynchedEntityData.defineId(LegacyShipEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> DATA_TIME_KEEPER =
            SynchedEntityData.defineId(LegacyShipEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> DATA_AUTO_PUMP =
            SynchedEntityData.defineId(LegacyShipEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> DATA_MODERNIZATION_TOTAL =
            SynchedEntityData.defineId(LegacyShipEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_FORMATION_ID =
            SynchedEntityData.defineId(LegacyShipEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_AI_FLAGS =
            SynchedEntityData.defineId(LegacyShipEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_INVENTORY_PAGE =
            SynchedEntityData.defineId(LegacyShipEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_FOLLOW_MIN =
            SynchedEntityData.defineId(LegacyShipEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_FOLLOW_MAX =
            SynchedEntityData.defineId(LegacyShipEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_WP_STAY_MODE =
            SynchedEntityData.defineId(LegacyShipEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_TASK =
            SynchedEntityData.defineId(LegacyShipEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_TASK_SIDE =
            SynchedEntityData.defineId(LegacyShipEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_MODEL_STATE =
            SynchedEntityData.defineId(LegacyShipEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_MODEL_SHIP_UID =
            SynchedEntityData.defineId(LegacyShipEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_HP_STATE =
            SynchedEntityData.defineId(LegacyShipEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_EMOTION =
            SynchedEntityData.defineId(LegacyShipEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_EMOTION2 =
            SynchedEntityData.defineId(LegacyShipEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_EMOTION4 =
            SynchedEntityData.defineId(LegacyShipEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_DISPLAY_PHASE =
            SynchedEntityData.defineId(LegacyShipEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_RENSOUHOU_COUNT =
            SynchedEntityData.defineId(LegacyShipEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> DATA_USE_MELEE =
            SynchedEntityData.defineId(LegacyShipEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> DATA_USE_LIGHT =
            SynchedEntityData.defineId(LegacyShipEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> DATA_USE_HEAVY =
            SynchedEntityData.defineId(LegacyShipEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> DATA_USE_AIR_LIGHT =
            SynchedEntityData.defineId(LegacyShipEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> DATA_USE_AIR_HEAVY =
            SynchedEntityData.defineId(LegacyShipEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> DATA_ON_SIGHT_CHASE =
            SynchedEntityData.defineId(LegacyShipEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> DATA_PVP_FIRST =
            SynchedEntityData.defineId(LegacyShipEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> DATA_ANTI_AIR =
            SynchedEntityData.defineId(LegacyShipEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> DATA_ANTI_SUB =
            SynchedEntityData.defineId(LegacyShipEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> DATA_CAN_FLOAT_UP =
            SynchedEntityData.defineId(LegacyShipEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> DATA_CAN_FOLLOW =
            SynchedEntityData.defineId(LegacyShipEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> DATA_SHOW_HELD_ITEM =
            SynchedEntityData.defineId(LegacyShipEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> DATA_LEGACY_RIDER_TYPE =
            SynchedEntityData.defineId(LegacyShipEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_LEGACY_RIDING_STATE =
            SynchedEntityData.defineId(LegacyShipEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> DATA_LEGACY_RAIDEN =
            SynchedEntityData.defineId(LegacyShipEntity.class, EntityDataSerializers.BOOLEAN);
    private static final String SHIP_KILLS_TAG = "ShipKills";
    private static final String LEGACY_KILLS_TAG = "Kills";
    private static final EntityDataAccessor<Integer> DATA_SHIP_KILLS =
            SynchedEntityData.defineId(LegacyShipEntity.class, EntityDataSerializers.INT);
    private static volatile boolean stopAi;

    public static final int EQUIPMENT_SLOT_COUNT = 6;
    public static final int CARGO_SLOT_COUNT = 18;
    public static final int SHIP_SLOT_COUNT = EQUIPMENT_SLOT_COUNT + CARGO_SLOT_COUNT;
    // Legacy CapaShipInventory: 6 equip + 18 cargo * 3 pages, pages unlocked by DrumState.
    public static final int INVENTORY_PAGE_COUNT = 3;
    public static final int SHIP_INVENTORY_SIZE = EQUIPMENT_SLOT_COUNT + CARGO_SLOT_COUNT * INVENTORY_PAGE_COUNT;
    private static final int LEGACY_TRANSPORT_WA_CLASS_ID = 16;
    // BasicEntityShip's task hands were the final two slots in the 24-slot handler.
    public static final int LEGACY_MAIN_HAND_SLOT = SHIP_SLOT_COUNT - 2;
    public static final int LEGACY_OFF_HAND_SLOT = SHIP_SLOT_COUNT - 1;

    private final SimpleContainer shipInventory = new SimpleContainer(SHIP_INVENTORY_SIZE) {
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
            0, 0, 0, 0, 0, 0,
            false,
            TeitokuData.DEFAULT_FORMATION_ID,
            ShipEquipmentProfile.EMPTY,
            List.of());
    private int modernHealthSteps;
    private int modernAttackSteps;
    private int modernDefenseSteps;
    private int modernAttackSpeedSteps;
    private int modernSpeedSteps;
    private int modernRangeSteps;
    private int meleeAttackCooldown;
    private int lightAttackCooldown;
    private int heavyAttackCooldown;
    private int airAttackCooldown;
    // Runtime-only equivalent of the legacy LastCombatTime state.
    private int legacyLastCombatTick;
    // Persistent equivalent of ID.T.TaskTime used by the mining task family.
    private int legacyTaskTime;
    private int legacySkillTimer;
    private int legacyHitHeight = -1;
    private int legacyModelFaceTick;
    private int legacyModelHeadTiltTick;
    private int legacyModelAttackTick;
    private int legacyModelAttackTick2;
    private int legacyModelCaressTicks;
    private boolean legacyModelHeadTilt;
    private final float[] legacyModelRotate = new float[3];
    private final Integer[] legacyModelEmotionOverrides = new Integer[8];
    private int legacyHitAngle;
    private int legacySkillRemaining;
    private Vec3 legacySkillMotion = Vec3.ZERO;
    private BlockPos legacySkillTarget = BlockPos.ZERO;
    private final Set<Integer> legacySkillDamagedTargetIds = new HashSet<>();
    private int legacyRePushTargetId = -1;
    private int legacyRePushTicks;
    private int legacyHimeRidingTargetId = -1;
    private int legacyHimeRidingTicks;
    private int maxAircraftLight;
    private int maxAircraftHeavy;
    private int aircraftRecoveryCooldown;
    private boolean aircraftProfileInitialized;
    private boolean loadingSaveData;
    private int shipItemVoiceCooldown;
    private int supplyEmoteDelay;
    private boolean fuelGoalsSuspended;
    private int goddessImmuneTicks;
    private int hurtSoundCooldown;
    private int cachedEffectSignature;
    private boolean cachedLegacyNight;
    // EntityAIShipCarrierAttack starts with its heavy aircraft channel.
    private boolean nextAirAttackLight;
    private boolean canDropItem = DEFAULT_CAN_DROP_ITEM;
    private int ownerUid;
    private int shipUid;
    @Nullable
    private BlockPos commandedPos;
    private String commandDimension = "";
    @Nullable
    private UUID guardEntityUuid;
    @Nullable
    private BlockPos guardedBlockPos;
    private String guardedBlockDimension = "";
    @Nullable
    private BlockPos routeNodePos;
    // GuardHandler.lastWaypoint was runtime-only in the legacy entity.
    @Nullable
    private BlockPos lastWaypointPos;
    private int routeWaitTicks;
    private int legacyGuardType;
    @Nullable
    private LegacyShipAttackOnCollideGoal meleeGoal;
    private boolean meleeGoalRegistered;
    @Nullable
    private LegacyShipWanderGoal wanderGoal;
    private int wanderPriority;
    private int craneState;
    private int craneTime;
    @Nullable
    private CraneBlockEntity workingCrane;
    private int waypointStayMode;
    private int routeTransferCooldown;
    // Legacy AttrNum.XP was runtime-only and was intentionally not serialized.
    private int autoPumpExperience;
    private double legacyLiquidDepth;
    // BasicEntityShip's previous position is runtime-only and intentionally
    // starts at the entity origin, matching the legacy constructor default.
    private double legacyPreviousX;
    private double legacyPreviousY;
    private double legacyPreviousZ;
    private static final ShipSoundType[] TIMEKEEPING_SOUNDS = {
            ShipSoundType.TIMEKEEP00, ShipSoundType.TIMEKEEP01, ShipSoundType.TIMEKEEP02,
            ShipSoundType.TIMEKEEP03, ShipSoundType.TIMEKEEP04, ShipSoundType.TIMEKEEP05,
            ShipSoundType.TIMEKEEP06, ShipSoundType.TIMEKEEP07, ShipSoundType.TIMEKEEP08,
            ShipSoundType.TIMEKEEP09, ShipSoundType.TIMEKEEP10, ShipSoundType.TIMEKEEP11,
            ShipSoundType.TIMEKEEP12, ShipSoundType.TIMEKEEP13, ShipSoundType.TIMEKEEP14,
            ShipSoundType.TIMEKEEP15, ShipSoundType.TIMEKEEP16, ShipSoundType.TIMEKEEP17,
            ShipSoundType.TIMEKEEP18, ShipSoundType.TIMEKEEP19, ShipSoundType.TIMEKEEP20,
            ShipSoundType.TIMEKEEP21, ShipSoundType.TIMEKEEP22, ShipSoundType.TIMEKEEP23
    };
    private boolean aiAutoTarget = true;
    private boolean aiOnSightChase = DEFAULT_AI_ON_SIGHT_CHASE;
    private boolean aiPvpFirst = DEFAULT_AI_PVP_FIRST;
    private boolean aiAntiAir = DEFAULT_AI_ANTI_AIR;
    private boolean aiAntiSub = DEFAULT_AI_ANTI_SUB;
    private boolean aiPickItem = true;
    private final HostileRuntimeState hostileRuntimeState = new HostileRuntimeState();
    @Nullable
    private LegacyShipFishingHookEntity fishingHook;
    @Nullable
    private ServerBossEvent bossEvent;

    public LegacyShipEntity(EntityType<? extends LegacyShipEntity> entityType, Level level) {
        super(entityType, level);
        this.noCulling = true;
        this.setPersistenceRequired();
        this.setCanPickUpLoot(false);
    }

    @Override
    public AABB getBoundingBoxForCulling() {
        return this.getBoundingBox().inflate(3.0D, 3.0D, 3.0D);
    }

    @Override
    public boolean isCurrentlyGlowing() {
        if (this.level().isClientSide()
                && this.isInvisible()
                && FMLEnvironment.dist.isClient()
                && ClientShipVisibilityHelper.isOwnedOrAlliedToClientPlayer(this)) {
            return true;
        }
        return super.isCurrentlyGlowing();
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
        this.entityData.define(DATA_HOSTILE_SCALE, 0);
        this.entityData.define(DATA_OWNER_UUID, Optional.empty());
        this.entityData.define(DATA_OWNER_NAME, "");
        this.entityData.define(DATA_ORDERED_TO_SIT, false);
        this.entityData.define(DATA_LEVEL, DEFAULT_LEVEL);
        this.entityData.define(DATA_EXPERIENCE, 0);
        this.entityData.define(DATA_MORALE, DEFAULT_MORALE);
        this.entityData.define(DATA_COMBAT_RATION_LEVEL, DEFAULT_COMBAT_RATION_LEVEL);
        this.entityData.define(DATA_FLEE_HP, DEFAULT_FLEE_HP);
        this.entityData.define(DATA_FOOD_SATURATION, 0);
        this.entityData.define(DATA_LIGHT_AMMO, DEFAULT_LIGHT_AMMO);
        this.entityData.define(DATA_HEAVY_AMMO, DEFAULT_HEAVY_AMMO);
        this.entityData.define(DATA_AIRCRAFT_LIGHT, 0);
        this.entityData.define(DATA_AIRCRAFT_HEAVY, 0);
        this.entityData.define(DATA_GRUDGE, DEFAULT_GRUDGE);
        this.entityData.define(DATA_MARRIED, false);
        this.entityData.define(DATA_RING_EFFECT, true);
        this.entityData.define(DATA_TIME_KEEPER, true);
        this.entityData.define(DATA_AUTO_PUMP, false);
        this.entityData.define(DATA_MODERNIZATION_TOTAL, 0);
        this.entityData.define(DATA_FORMATION_ID, TeitokuData.DEFAULT_FORMATION_ID);
        this.entityData.define(DATA_AI_FLAGS, GameplayCommandHandler.AI_FLAG_AUTO_TARGET
                | GameplayCommandHandler.AI_FLAG_PICK_ITEM);
        this.entityData.define(DATA_INVENTORY_PAGE, 0);
        this.entityData.define(DATA_FOLLOW_MIN, DEFAULT_FOLLOW_MIN);
        this.entityData.define(DATA_FOLLOW_MAX, DEFAULT_FOLLOW_MAX);
        this.entityData.define(DATA_WP_STAY_MODE, DEFAULT_WP_STAY_MODE);
        this.entityData.define(DATA_TASK, DEFAULT_TASK);
        this.entityData.define(DATA_TASK_SIDE, DEFAULT_TASK_SIDE);
        this.entityData.define(DATA_MODEL_STATE, DEFAULT_MODEL_STATE);
        this.entityData.define(DATA_MODEL_SHIP_UID, 0);
        this.entityData.define(DATA_HP_STATE, DEFAULT_HP_STATE);
        this.entityData.define(DATA_EMOTION, DEFAULT_EMOTION);
        this.entityData.define(DATA_EMOTION2, DEFAULT_EMOTION2);
        this.entityData.define(DATA_EMOTION4, DEFAULT_EMOTION4);
        this.entityData.define(DATA_DISPLAY_PHASE, DEFAULT_DISPLAY_PHASE);
        this.entityData.define(DATA_RENSOUHOU_COUNT, 0);
        this.entityData.define(DATA_USE_MELEE, DEFAULT_USE_MELEE);
        this.entityData.define(DATA_USE_LIGHT, DEFAULT_USE_LIGHT);
        this.entityData.define(DATA_USE_HEAVY, DEFAULT_USE_HEAVY);
        this.entityData.define(DATA_USE_AIR_LIGHT, DEFAULT_USE_AIR_LIGHT);
        this.entityData.define(DATA_USE_AIR_HEAVY, DEFAULT_USE_AIR_HEAVY);
        this.entityData.define(DATA_ON_SIGHT_CHASE, DEFAULT_AI_ON_SIGHT_CHASE);
        this.entityData.define(DATA_PVP_FIRST, DEFAULT_AI_PVP_FIRST);
        this.entityData.define(DATA_ANTI_AIR, DEFAULT_AI_ANTI_AIR);
        this.entityData.define(DATA_ANTI_SUB, DEFAULT_AI_ANTI_SUB);
        this.entityData.define(DATA_CAN_FLOAT_UP, DEFAULT_CAN_FLOAT_UP);
        this.entityData.define(DATA_CAN_FOLLOW, DEFAULT_CAN_FOLLOW);
        this.entityData.define(DATA_SHOW_HELD_ITEM, DEFAULT_SHOW_HELD_ITEM);
        this.entityData.define(DATA_LEGACY_RIDER_TYPE, 0);
        this.entityData.define(DATA_LEGACY_RIDING_STATE, 0);
        this.entityData.define(DATA_LEGACY_RAIDEN, false);
        this.entityData.define(DATA_SHIP_KILLS, 0);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new LegacyShipFloatGoal(this));
        this.goalSelector.addGoal(1, new LegacyShipFleeGoal(this));
        this.goalSelector.addGoal(1, new LegacyShipGuardGoal(this));
        this.goalSelector.addGoal(2, new LegacyShipRangedAttackGoal(this, 1.0D));
        this.meleeGoal = new LegacyShipAttackOnCollideGoal(this, 1.0D);
        this.goalSelector.addGoal(4, new LegacyShipFollowOwnerGoal(this));
        this.goalSelector.addGoal(5, new LegacyShipPickItemGoal(this, 1.0D));
        this.wanderGoal = new LegacyShipWanderGoal(this);
        this.wanderPriority = 24;
        this.goalSelector.addGoal(this.wanderPriority, this.wanderGoal);
        this.goalSelector.addGoal(25, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(26, new RandomLookAroundGoal(this));
        this.goalSelector.addGoal(8, new LegacyShipOpenDoorGoal(this, true));
        this.targetSelector.addGoal(0, new HurtByTargetGoal(this) {
            @Override
            public boolean canUse() {
                if (!super.canUse()) {
                    return false;
                }
                LivingEntity attacker = LegacyShipEntity.this.getLastHurtByMob();
                return LegacyShipEntity.this.isHostileVariant()
                        ? LegacyShipEntity.this.canEngage(attacker)
                        : LegacyShipEntity.this.canCommandOrRevengeEngage(attacker);
            }

            @Override
            public boolean canContinueToUse() {
                if (!super.canContinueToUse()) {
                    return false;
                }
                LivingEntity target = LegacyShipEntity.this.getTarget();
                return LegacyShipEntity.this.isHostileVariant()
                        ? LegacyShipEntity.this.canEngage(target)
                        : LegacyShipEntity.this.canCommandOrRevengeEngage(target);
            }
        });
        this.targetSelector.addGoal(3, new LegacyShipRangeTargetGoal(this));
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
        this.loadingSaveData = true;
        this.aircraftProfileInitialized = false;
        this.setVariantEggMeta(tag.getInt(VARIANT_EGG_META_TAG));
        this.setOrderedToSit(tag.getBoolean(ORDERED_TO_SIT_TAG));

        if (tag.hasUUID(OWNER_UUID_TAG)) {
            this.entityData.set(DATA_OWNER_UUID, Optional.of(tag.getUUID(OWNER_UUID_TAG)));
        } else {
            this.entityData.set(DATA_OWNER_UUID, Optional.empty());
        }

        this.entityData.set(DATA_OWNER_NAME, this.readOwnerName(tag));
        this.ownerUid = Math.max(0, getIntWithLegacyMinorFallback(
                tag, OWNER_UID_TAG, null, LEGACY_OWNER_UID_TAG, 0));
        this.entityData.set(DATA_LEVEL, Math.max(DEFAULT_LEVEL, getIntWithLegacyMinorFallback(
                tag, SHIP_LEVEL_TAG, null, LEGACY_LEVEL_TAG, DEFAULT_LEVEL)));
        this.setShipExperience(this.readShipExperience(tag));
        this.entityData.set(DATA_MORALE, Mth.clamp(getIntWithLegacyMinorFallback(
                tag, SHIP_MORALE_TAG, null, LEGACY_MORALE_TAG, DEFAULT_MORALE), 0, MAX_MORALE));
        this.setCombatRationLevel(getIntWithLegacyMinorFallback(
                tag, SHIP_COMBAT_RATION_LEVEL_TAG, null,
                LEGACY_COMBAT_RATION_LEVEL_TAG, DEFAULT_COMBAT_RATION_LEVEL));
        this.setFleeHp(getIntWithLegacyMinorFallback(
                tag, SHIP_FLEE_HP_TAG, null,
                LEGACY_FLEE_HP_TAG, DEFAULT_FLEE_HP));
        this.setFoodSaturation(getIntWithLegacyMinorFallback(
                tag, SHIP_FOOD_SATURATION_TAG, null,
                LEGACY_FOOD_SATURATION_TAG, 0));
        this.setTask(getIntWithLegacyMinorFallback(
                tag, SHIP_TASK_TAG, null, LEGACY_TASK_TAG, DEFAULT_TASK));
        this.setTaskSide(getIntWithLegacyMinorFallback(
                tag, SHIP_TASK_SIDE_TAG, null, LEGACY_TASK_SIDE_TAG, DEFAULT_TASK_SIDE));
        this.legacyTaskTime = Math.max(0, tag.getInt(SHIP_TASK_TIME_TAG));
        this.entityData.set(DATA_MODEL_STATE, getIntWithLegacyDisplayFallback(
                tag, SHIP_MODEL_STATE_TAG, LEGACY_MODEL_STATE_TAG, DEFAULT_MODEL_STATE));
        this.entityData.set(DATA_HP_STATE, Mth.clamp(getIntWithLegacyDisplayFallback(
                tag, SHIP_HP_STATE_TAG, LEGACY_HP_STATE_TAG, DEFAULT_HP_STATE), 0, 3));
        this.entityData.set(DATA_EMOTION, getIntWithLegacyDisplayFallback(
                tag, SHIP_EMOTION_TAG, LEGACY_EMOTION_TAG, DEFAULT_EMOTION));
        this.entityData.set(DATA_EMOTION2, getIntWithLegacyDisplayFallback(
                tag, SHIP_EMOTION2_TAG, LEGACY_EMOTION2_TAG, DEFAULT_EMOTION2));
        this.entityData.set(DATA_EMOTION4, getIntWithLegacyDisplayFallback(
                tag, SHIP_EMOTION4_TAG, LEGACY_EMOTION4_TAG, DEFAULT_EMOTION4));
        this.entityData.set(DATA_DISPLAY_PHASE, getIntWithLegacyDisplayFallback(
                tag, SHIP_PHASE_TAG, LEGACY_PHASE_TAG, DEFAULT_DISPLAY_PHASE));
        this.legacySkillTimer = Math.max(0, tag.getInt(SHIP_SKILL_TIMER_TAG));
        this.legacySkillRemaining = Math.max(0, tag.getInt(SHIP_SKILL_REMAINING_TAG));
        this.legacySkillMotion = new Vec3(
                tag.getDouble(SHIP_SKILL_MOTION_X_TAG),
                tag.getDouble(SHIP_SKILL_MOTION_Y_TAG),
                tag.getDouble(SHIP_SKILL_MOTION_Z_TAG));
        if (this.legacySkillMotion.lengthSqr() <= 1.0E-8D) {
            this.legacySkillMotion = Vec3.ZERO;
        }
        this.legacySkillTarget = tag.contains(SHIP_SKILL_TARGET_TAG, Tag.TAG_LONG)
                ? BlockPos.of(tag.getLong(SHIP_SKILL_TARGET_TAG)) : BlockPos.ZERO;
        this.legacySkillDamagedTargetIds.clear();
        if (tag.contains(SHIP_RENSOUHOU_COUNT_TAG, Tag.TAG_INT)) {
            this.setShimakazeRensouhouCount(tag.getInt(SHIP_RENSOUHOU_COUNT_TAG));
        } else if (tag.contains(LEGACY_RENSOUHOU_COUNT_TAG, Tag.TAG_INT)) {
            this.setShimakazeRensouhouCount(tag.getInt(LEGACY_RENSOUHOU_COUNT_TAG));
        }
        this.setUseAttackMelee(getBooleanWithLegacyFlagsFallback(
                tag, SHIP_USE_MELEE_TAG, LEGACY_USE_MELEE_FLAG_TAG, DEFAULT_USE_MELEE));
        this.setUseAttackLight(getBooleanWithLegacyFlagsFallback(
                tag, SHIP_USE_LIGHT_TAG, LEGACY_USE_LIGHT_FLAG_TAG, DEFAULT_USE_LIGHT));
        this.setUseAttackHeavy(getBooleanWithLegacyFlagsFallback(
                tag, SHIP_USE_HEAVY_TAG, LEGACY_USE_HEAVY_FLAG_TAG, DEFAULT_USE_HEAVY));
        this.setUseAttackAirLight(getBooleanWithLegacyFlagsFallback(
                tag, SHIP_USE_AIR_LIGHT_TAG, LEGACY_USE_AIR_LIGHT_FLAG_TAG, DEFAULT_USE_AIR_LIGHT));
        this.setUseAttackAirHeavy(getBooleanWithLegacyFlagsFallback(
                tag, SHIP_USE_AIR_HEAVY_TAG, LEGACY_USE_AIR_HEAVY_FLAG_TAG, DEFAULT_USE_AIR_HEAVY));
        this.setLightAmmo(getIntWithLegacyMinorFallback(
                tag, SHIP_LIGHT_AMMO_TAG, LEGACY_LIGHT_AMMO_TAG,
                LEGACY_LIGHT_AMMO_MINOR_TAG, DEFAULT_LIGHT_AMMO));
        this.setHeavyAmmo(getIntWithLegacyMinorFallback(
                tag, SHIP_HEAVY_AMMO_TAG, LEGACY_HEAVY_AMMO_TAG,
                LEGACY_HEAVY_AMMO_MINOR_TAG, DEFAULT_HEAVY_AMMO));
        this.setGrudge(getIntWithLegacyMinorFallback(
                tag, SHIP_GRUDGE_TAG, LEGACY_GRUDGE_TAG,
                LEGACY_GRUDGE_MINOR_TAG, DEFAULT_GRUDGE));
        CompoundTag legacyMinor = getLegacyMinorTag(tag);
        boolean hasAircraftState = tag.contains(SHIP_AIRCRAFT_LIGHT_TAG, Tag.TAG_INT)
                || tag.contains(SHIP_AIRCRAFT_HEAVY_TAG, Tag.TAG_INT)
                || tag.contains(LEGACY_AIRCRAFT_LIGHT_TAG, Tag.TAG_INT)
                || tag.contains(LEGACY_AIRCRAFT_HEAVY_TAG, Tag.TAG_INT)
                || legacyMinor != null && (legacyMinor.contains(LEGACY_AIRCRAFT_LIGHT_TAG, Tag.TAG_INT)
                || legacyMinor.contains(LEGACY_AIRCRAFT_HEAVY_TAG, Tag.TAG_INT));
        this.entityData.set(DATA_AIRCRAFT_LIGHT, Math.max(0, getIntWithLegacyMinorFallback(
                tag, SHIP_AIRCRAFT_LIGHT_TAG, LEGACY_AIRCRAFT_LIGHT_TAG,
                LEGACY_AIRCRAFT_LIGHT_TAG, 0)));
        this.entityData.set(DATA_AIRCRAFT_HEAVY, Math.max(0, getIntWithLegacyMinorFallback(
                tag, SHIP_AIRCRAFT_HEAVY_TAG, LEGACY_AIRCRAFT_HEAVY_TAG,
                LEGACY_AIRCRAFT_HEAVY_TAG, 0)));
        this.aircraftProfileInitialized = hasAircraftState;
        this.entityData.set(DATA_MARRIED, getBooleanWithLegacyFlagsFallback(
                tag, SHIP_MARRIED_TAG, LEGACY_MARRIED_FLAG_TAG, false));
        this.entityData.set(DATA_RING_EFFECT, getBooleanWithLegacyFlagsFallback(
                tag, WED_EFFECT_TAG, LEGACY_RING_EFFECT_FLAG_TAG, true));
        this.entityData.set(DATA_TIME_KEEPER, getBooleanWithLegacyFlagsFallback(
                tag, SHIP_TIME_KEEPER_TAG, LEGACY_TIME_KEEPER_FLAG_TAG, true));
        this.setAutoPump(getBooleanWithLegacyFlagsFallback(
                tag, SHIP_AUTO_PUMP_TAG, LEGACY_AUTO_PUMP_FLAG_TAG, false));
        this.modernHealthSteps = Mth.clamp(tag.getInt(MODERN_HEALTH_TAG), 0, 100);
        this.modernAttackSteps = Mth.clamp(tag.getInt(MODERN_ATTACK_TAG), 0, 100);
        this.modernDefenseSteps = Mth.clamp(tag.getInt(MODERN_DEFENSE_TAG), 0, 100);
        this.modernAttackSpeedSteps = Mth.clamp(tag.getInt(MODERN_ATTACK_SPEED_TAG), 0, 100);
        this.modernSpeedSteps = Mth.clamp(tag.getInt(MODERN_SPEED_TAG), 0, 100);
        this.modernRangeSteps = Mth.clamp(tag.getInt(MODERN_RANGE_TAG), 0, 100);
        this.setShipKills(getIntWithLegacyMinorFallback(tag, SHIP_KILLS_TAG, null, LEGACY_KILLS_TAG, 0));
        this.shipUid = Math.max(0, getIntWithLegacyMinorFallback(
                tag, SHIP_UID_TAG, null, LEGACY_SHIP_UID_TAG, 0));
        this.entityData.set(DATA_MODEL_SHIP_UID, this.shipUid);
        this.commandedPos = tag.contains(COMMAND_POS_TAG, Tag.TAG_LONG)
                ? BlockPos.of(tag.getLong(COMMAND_POS_TAG))
                : null;
        this.commandDimension = tag.getString(COMMAND_DIM_TAG);
        this.guardEntityUuid = tag.hasUUID(GUARD_ENTITY_TAG) ? tag.getUUID(GUARD_ENTITY_TAG) : null;
        this.guardedBlockPos = tag.contains(GUARD_BLOCK_POS_TAG, Tag.TAG_LONG)
                ? BlockPos.of(tag.getLong(GUARD_BLOCK_POS_TAG))
                : null;
        this.guardedBlockDimension = tag.getString(GUARD_BLOCK_DIM_TAG);
        if (!hasModernCommandState(tag)) {
            this.loadLegacyGuardBlock(tag);
        }
        this.routeNodePos = tag.contains(ROUTE_NODE_TAG, Tag.TAG_LONG)
                ? BlockPos.of(tag.getLong(ROUTE_NODE_TAG))
                : null;
        int guardType = this.guardEntityUuid != null ? 2 : this.hasActiveCommandState() ? 1 : 0;
        this.legacyGuardType = Mth.clamp(getIntWithLegacyMinorFallback(
                tag, LEGACY_GUARD_TYPE_TAG, null, LEGACY_GUARD_TYPE_TAG, guardType), 0, 2);
        this.routeWaitTicks = Math.max(0, tag.getInt(ROUTE_WAIT_TAG));
        this.craneState = tag.getInt("CraneState");
        this.craneTime = getIntWithLegacyMinorFallback(tag, "CraneTime", null, "Crane", 0);
        this.setWaypointStayMode(Mth.clamp(getIntWithLegacyMinorFallback(
                tag, SHIP_WP_STAY_TAG, null, LEGACY_WP_STAY_TAG, DEFAULT_WP_STAY_MODE),
                DEFAULT_WP_STAY_MODE, MAX_WP_STAY_MODE));
        this.aiAutoTarget = tag.contains(AI_AUTO_TARGET_TAG, Tag.TAG_BYTE)
                ? tag.getBoolean(AI_AUTO_TARGET_TAG)
                : !getBooleanWithLegacyFlagsFallback(
                        tag, AI_AUTO_TARGET_TAG, LEGACY_PASSIVE_AI_FLAG_TAG, false);
        this.aiOnSightChase = !this.isHostileVariant()
                && getBooleanWithLegacyFlagsFallback(
                        tag, AI_ON_SIGHT_CHASE_TAG, LEGACY_ON_SIGHT_CHASE_FLAG_TAG,
                        DEFAULT_AI_ON_SIGHT_CHASE);
        this.aiPvpFirst = getBooleanWithLegacyFlagsFallback(
                tag, AI_PVP_FIRST_TAG, LEGACY_PVP_FIRST_FLAG_TAG, DEFAULT_AI_PVP_FIRST);
        this.aiAntiAir = getBooleanWithLegacyFlagsFallback(
                tag, AI_ANTI_AIR_TAG, LEGACY_ANTI_AIR_FLAG_TAG, DEFAULT_AI_ANTI_AIR);
        this.aiAntiSub = getBooleanWithLegacyFlagsFallback(
                tag, AI_ANTI_SUB_TAG, LEGACY_ANTI_SUB_FLAG_TAG, DEFAULT_AI_ANTI_SUB);
        this.aiPickItem = getBooleanWithLegacyFlagsFallback(
                tag, AI_PICK_ITEM_TAG, LEGACY_PICK_ITEM_FLAG_TAG, true);
        int loadedAiFlags = this.encodeAiFlags();
        this.entityData.set(DATA_AI_FLAGS, loadedAiFlags);
        this.entityData.set(DATA_FOLLOW_MIN, getIntWithLegacyMinorFallback(
                tag, FOLLOW_MIN_TAG, null, FOLLOW_MIN_TAG, DEFAULT_FOLLOW_MIN));
        this.entityData.set(DATA_FOLLOW_MAX, getIntWithLegacyMinorFallback(
                tag, FOLLOW_MAX_TAG, null, FOLLOW_MAX_TAG, DEFAULT_FOLLOW_MAX));
        this.entityData.set(DATA_ON_SIGHT_CHASE, this.aiOnSightChase);
        this.entityData.set(DATA_PVP_FIRST, this.aiPvpFirst);
        this.entityData.set(DATA_ANTI_AIR, this.aiAntiAir);
        this.entityData.set(DATA_ANTI_SUB, this.aiAntiSub);
        this.entityData.set(DATA_CAN_FLOAT_UP, getBooleanWithLegacyFlagsFallback(
                tag, SHIP_CAN_FLOAT_TAG, LEGACY_CAN_FLOAT_FLAG_TAG, DEFAULT_CAN_FLOAT_UP));
        this.entityData.set(DATA_CAN_FOLLOW, getBooleanWithLegacyFlagsFallback(
                tag, SHIP_CAN_FOLLOW_TAG, LEGACY_CAN_FOLLOW_FLAG_TAG, DEFAULT_CAN_FOLLOW));
        this.entityData.set(DATA_SHOW_HELD_ITEM, getBooleanWithLegacyFlagsFallback(
                tag, SHIP_SHOW_HELD_ITEM_TAG, LEGACY_HELD_ITEM_FLAG_TAG, DEFAULT_SHOW_HELD_ITEM));
        this.canDropItem = getBooleanWithLegacyFlagsFallback(
                tag, SHIP_CAN_DROP_TAG, LEGACY_CAN_DROP_FLAG_TAG, DEFAULT_CAN_DROP_ITEM);
        this.applyAiData(loadedAiFlags);
        this.entityData.set(DATA_MODERNIZATION_TOTAL, this.getModernizationCount());
        this.loadShipInventory(tag);
        CompoundTag hostileTag = tag.getCompound(HOSTILE_RUNTIME_TAG);
        this.hostileRuntimeState.loadFromTag(hostileTag);
        // 旧的 1.20.1 存档用两个标志记录规模。
        int scale = tag.contains(HOSTILE_SCALE_TAG, Tag.TAG_ANY_NUMERIC)
                ? tag.getByte(HOSTILE_SCALE_TAG)
                : (hostileTag.getBoolean("Boss") ? 2 : 0) + (hostileTag.getBoolean("Elite") ? 1 : 0);
        this.entityData.set(DATA_HOSTILE_SCALE, this.isHostileVariant() ? Mth.clamp(scale, 0, 3) : 0);
        this.loadingSaveData = false;
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

        if (!this.isHostileVariant()) {
            tag.putInt(SHIP_LEVEL_TAG, this.getShipLevel());
            tag.putInt(SHIP_EXPERIENCE_TAG, this.getShipExperience());
            tag.putInt(SHIP_MORALE_TAG, this.getMorale());
        }
        tag.putInt(SHIP_COMBAT_RATION_LEVEL_TAG, this.getCombatRationLevel());
        tag.putInt(SHIP_FLEE_HP_TAG, this.getFleeHp());
        tag.putInt(SHIP_FOOD_SATURATION_TAG, this.getFoodSaturation());
        tag.putInt(SHIP_TASK_TAG, this.getTask());
        tag.putInt(SHIP_TASK_SIDE_TAG, this.getTaskSide());
        tag.putInt(SHIP_TASK_TIME_TAG, Math.max(0, this.legacyTaskTime));
        tag.putInt(SHIP_MODEL_STATE_TAG, this.getModelState());
        tag.putInt(SHIP_HP_STATE_TAG, this.getHpState());
        tag.putInt(SHIP_EMOTION_TAG, this.getEmotion());
        tag.putInt(SHIP_EMOTION2_TAG, this.getEmotion2());
        tag.putInt(SHIP_EMOTION4_TAG, this.getEmotion4());
        tag.putInt(SHIP_PHASE_TAG, this.getDisplayPhase());
        tag.putInt(SHIP_SKILL_TIMER_TAG, this.legacySkillTimer);
        tag.putInt(SHIP_SKILL_REMAINING_TAG, this.legacySkillRemaining);
        tag.putDouble(SHIP_SKILL_MOTION_X_TAG, this.legacySkillMotion.x);
        tag.putDouble(SHIP_SKILL_MOTION_Y_TAG, this.legacySkillMotion.y);
        tag.putDouble(SHIP_SKILL_MOTION_Z_TAG, this.legacySkillMotion.z);
        tag.putLong(SHIP_SKILL_TARGET_TAG, this.legacySkillTarget.asLong());
        tag.putInt(SHIP_RENSOUHOU_COUNT_TAG, this.getShimakazeRensouhouCount());
        tag.putBoolean(SHIP_USE_MELEE_TAG, this.useAttackMelee());
        tag.putBoolean(SHIP_USE_LIGHT_TAG, this.useAttackLight());
        tag.putBoolean(SHIP_USE_HEAVY_TAG, this.useAttackHeavy());
        tag.putBoolean(SHIP_USE_AIR_LIGHT_TAG, this.useAttackAirLight());
        tag.putBoolean(SHIP_USE_AIR_HEAVY_TAG, this.useAttackAirHeavy());
        tag.putInt(SHIP_LIGHT_AMMO_TAG, this.getLightAmmo());
        tag.putInt(SHIP_HEAVY_AMMO_TAG, this.getHeavyAmmo());
        tag.putInt(SHIP_AIRCRAFT_LIGHT_TAG, this.getAircraftLight());
        tag.putInt(SHIP_AIRCRAFT_HEAVY_TAG, this.getAircraftHeavy());
        tag.putInt(SHIP_GRUDGE_TAG, this.getGrudge());
        tag.putBoolean(SHIP_MARRIED_TAG, this.isMarried());
        tag.putBoolean(WED_EFFECT_TAG, this.isRingEffectEnabled());
        tag.putBoolean(SHIP_TIME_KEEPER_TAG, this.isTimeKeeper());
        tag.putBoolean(SHIP_AUTO_PUMP_TAG, this.isAutoPump());
        tag.putInt(MODERN_HEALTH_TAG, this.modernHealthSteps);
        tag.putInt(MODERN_ATTACK_TAG, this.modernAttackSteps);
        tag.putInt(MODERN_DEFENSE_TAG, this.modernDefenseSteps);
        tag.putInt(MODERN_ATTACK_SPEED_TAG, this.modernAttackSpeedSteps);
        tag.putInt(MODERN_SPEED_TAG, this.modernSpeedSteps);
        tag.putInt(MODERN_RANGE_TAG, this.modernRangeSteps);
        tag.putInt(SHIP_KILLS_TAG, this.getShipKills());
        if (this.shipUid > 0) {
            tag.putInt(SHIP_UID_TAG, this.shipUid);
        }
        if (this.commandedPos != null) {
            tag.putLong(COMMAND_POS_TAG, this.commandedPos.asLong());
        }
        if ((this.commandedPos != null || this.guardEntityUuid != null) && !this.commandDimension.isBlank()) {
            tag.putString(COMMAND_DIM_TAG, this.commandDimension);
        }
        tag.putInt(LEGACY_GUARD_TYPE_TAG, this.legacyGuardType);
        if (this.guardEntityUuid != null) {
            tag.putUUID(GUARD_ENTITY_TAG, this.guardEntityUuid);
        }
        if (this.guardedBlockPos != null) {
            tag.putLong(GUARD_BLOCK_POS_TAG, this.guardedBlockPos.asLong());
            if (!this.guardedBlockDimension.isBlank()) {
                tag.putString(GUARD_BLOCK_DIM_TAG, this.guardedBlockDimension);
            }
        }
        if (this.routeNodePos != null) {
            tag.putLong(ROUTE_NODE_TAG, this.routeNodePos.asLong());
            tag.putInt(ROUTE_WAIT_TAG, Math.max(0, this.routeWaitTicks));
        }
        tag.putInt(SHIP_WP_STAY_TAG, this.getWaypointStayMode());
        tag.putBoolean(AI_AUTO_TARGET_TAG, this.aiAutoTarget);
        tag.putBoolean(AI_ON_SIGHT_CHASE_TAG, this.isAiOnSightChase());
        tag.putBoolean(AI_PVP_FIRST_TAG, this.isAiPvpFirst());
        tag.putBoolean(AI_ANTI_AIR_TAG, this.isAiAntiAir());
        tag.putBoolean(AI_ANTI_SUB_TAG, this.isAiAntiSub());
        tag.putBoolean(AI_PICK_ITEM_TAG, this.aiPickItem);
        tag.putInt(FOLLOW_MIN_TAG, this.getFollowMin());
        tag.putInt(FOLLOW_MAX_TAG, this.getFollowMax());
        tag.putInt("CraneState", this.craneState);
        tag.putInt("CraneTime", this.craneTime);
        tag.putBoolean(SHIP_CAN_FLOAT_TAG, this.canFloatUp());
        tag.putBoolean(SHIP_CAN_FOLLOW_TAG, this.canFollow());
        tag.putBoolean(SHIP_CAN_DROP_TAG, this.canDropItem);
        tag.putBoolean(SHIP_SHOW_HELD_ITEM_TAG, this.canShowHeldItem());
        this.saveShipInventory(tag);
        if (this.isHostileVariant()) {
            tag.putByte(HOSTILE_SCALE_TAG, (byte) this.getLegacyHostileScaleLevel());
            tag.put(HOSTILE_RUNTIME_TAG, this.hostileRuntimeState.saveToTag(new CompoundTag()));
        }
    }

    @Override
    public void tick() {
        this.updateLegacyWaterState();
        if (!this.level().isClientSide()) {
            this.clearInvalidHostileTarget();
        }
        super.tick();

        if (this.level().isClientSide()) {
            if (this.legacyModelAttackTick > 0) this.legacyModelAttackTick--;
            if (this.legacyModelCaressTicks > 0 && --this.legacyModelCaressTicks == 0) {
                this.legacyModelEmotionOverrides[6] = 0;
            }
        }

        LegacyShipBehaviorCatalog.tickLegacyClientBehavior(this);

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
        if (!this.level().isClientSide() && this.shipItemVoiceCooldown > 0) {
            this.shipItemVoiceCooldown--;
        }
        if (!this.level().isClientSide() && this.goddessImmuneTicks > 0) {
            this.goddessImmuneTicks--;
        }
        if (!this.level().isClientSide() && this.hurtSoundCooldown > 0) {
            this.hurtSoundCooldown--;
        }

        // Legacy BasicEntityShip recalculates raw attributes on its 128-tick
        // refresh, including class-specific day/night bonuses.
        if ((this.tickCount & 127) == 0 && this.isLegacyNight() != this.cachedLegacyNight) {
            this.refreshFromVariant(true);
        }

        int effectSignature = this.computeActiveEffectSignature();
        if (effectSignature != this.cachedEffectSignature) {
            this.cachedEffectSignature = effectSignature;
            this.refreshFromVariant(true);
        }

        if (!this.level().isClientSide()) {
            if (this.supplyEmoteDelay > 0) {
                this.supplyEmoteDelay--;
            }
            this.ensureShipUid();
            if (this.workingCrane != null && (this.workingCrane.isRemoved()
                    || !this.workingCrane.getBlockPos().equals(this.routeNodePos))) {
                this.workingCrane.clearShipData(this);
                this.finishCraning();
            }
            if (this.craneState > 1) {
                this.craneTime++;
            }
            if ((this.tickCount % 20) == 0) {
                this.cacheShipState(false);
            }
            this.tickCommandState();
            if ((this.tickCount & 7) == 0) {
                LegacyShipBehaviorCatalog.tickLegacyTask(this);
            }
            this.tickEquipmentBehaviors();
            this.tickMarriageBond();
            this.tickLegacyRingPassives();
            this.tickAutoSupportItems();
            this.tickLegacyMovementAndIdleConsumption();
            this.updateFuelGoals();
            this.tickAutoPump();
            this.tickTimeKeeper();
            this.tickAircraftRecovery();
            if ((this.tickCount & 31) == 0 && this.isAlive()) {
                MobEffectInstance saturation = this.getEffect(MobEffects.SATURATION);
                if (saturation != null) {
                    int lv = Math.max(1, saturation.getAmplifier() + 1);
                    this.heal((this.getMaxHealth() * 0.01F + 2.0F) * (0.8F + lv * 0.2F));
                    this.addMorale(100 * lv);
                }
            }
            if ((this.tickCount & 63) == 0) {
                LegacyShipBehaviorCatalog.tickLegacyEmotionState(this);
            }
            this.tickHostileRuntime();
            LegacyShipBehaviorCatalog.tickLegacyShipMountBehavior(this);
            LegacyShipBehaviorCatalog.tickLegacyRiderBehavior(this);

            if (this.tickCount % 20 == 0) {
                int currentFormationId = this.resolveAppliedFormationId();
                int currentFormatSlot = currentFormationId > 0 ? this.resolveAppliedFormationSlot() : 0;
                if (currentFormationId != this.entityData.get(DATA_FORMATION_ID)
                        || currentFormatSlot != this.cachedFormationSlot) {
                    this.entityData.set(DATA_FORMATION_ID, currentFormationId);
                    this.refreshFromVariant(true);
                } else if (currentFormationId > 0) {
                    this.applyBaseValue(Attributes.MOVEMENT_SPEED, this.resolveEffectiveMoveSpeed(currentFormationId));
                }
            }
        }

        if (this.isOrderedToSit()) {
            this.getShipNavigate().clearPathEntity();
            this.setTarget(null);
        }
    }

    /** Mirrors BasicEntityShipCV's one-light/one-heavy recovery tick. */
    private void tickAircraftRecovery() {
        if (!this.attackProfile.hasAirAttack()
                || !LegacyShipBehaviorCatalog.managesAircraftInventory(this.getSpec())) {
            return;
        }
        if (this.aircraftRecoveryCooldown > 0) {
            this.aircraftRecoveryCooldown--;
            return;
        }

        this.aircraftRecoveryCooldown = LegacyShipBehaviorCatalog.aircraftRecoveryDelayFor(
                this.legacyStats.attackSpeed());
        this.setAircraftLight(this.getAircraftLight() + 1);
        this.setAircraftHeavy(this.getAircraftHeavy() + 1);
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> key) {
        super.onSyncedDataUpdated(key);

        if (this.legacyModelEmotionOverrides != null) {
            if (DATA_MODEL_STATE.equals(key)) this.legacyModelEmotionOverrides[0] = null;
            if (DATA_EMOTION.equals(key)) this.legacyModelEmotionOverrides[1] = null;
            if (DATA_EMOTION2.equals(key)) this.legacyModelEmotionOverrides[2] = null;
            if (DATA_DISPLAY_PHASE.equals(key)) this.legacyModelEmotionOverrides[5] = null;
            if (DATA_EMOTION4.equals(key)) this.legacyModelEmotionOverrides[7] = null;
        }

        if (DATA_VARIANT_EGG_META.equals(key)
                || DATA_HOSTILE_SCALE.equals(key)
                || DATA_LEVEL.equals(key)
                || DATA_MORALE.equals(key)
                || DATA_MARRIED.equals(key)
                || DATA_MODERNIZATION_TOTAL.equals(key)
                || DATA_FORMATION_ID.equals(key)) {
            this.refreshFromVariant(true);
        }

        if (DATA_AI_FLAGS.equals(key)) {
            this.applySyncedAiData();
        }

        if (DATA_ON_SIGHT_CHASE.equals(key)) {
            this.aiOnSightChase = this.entityData.get(DATA_ON_SIGHT_CHASE);
        }

        if (DATA_PVP_FIRST.equals(key)) {
            this.aiPvpFirst = this.entityData.get(DATA_PVP_FIRST);
        }
        if (DATA_ANTI_AIR.equals(key)) {
            this.aiAntiAir = this.entityData.get(DATA_ANTI_AIR);
        }
        if (DATA_ANTI_SUB.equals(key)) {
            this.aiAntiSub = this.entityData.get(DATA_ANTI_SUB);
        }
    }

    @Override
    public EntityDimensions getDimensions(Pose pose) {
        return LegacyShipBehaviorCatalog.dimensionsFor(this.getSpec(), this.getLegacyHostileScaleLevel());
    }

    @Override
    protected float getStandingEyeHeight(Pose pose, EntityDimensions dimensions) {
        return LegacyShipBehaviorCatalog.eyeHeightFor(this, dimensions);
    }

    @Override
    public double getPassengersRidingOffset() {
        return LegacyShipBehaviorCatalog.passengerRidingOffsetFor(this);
    }

    @Override
    public double getMyRidingOffset() {
        return LegacyShipBehaviorCatalog.myRidingOffsetFor(this);
    }

    @Override
    protected void positionRider(Entity passenger, MoveFunction moveFunction) {
        if (!LegacyShipBehaviorCatalog.positionLegacyRider(this, passenger, moveFunction)) {
            super.positionRider(passenger, moveFunction);
        }
    }

    @Override
    public boolean isPushable() {
        return !LegacyShipBehaviorCatalog.isLegacySkillMovementActive(this) && super.isPushable();
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
    public void playAmbientSound() {
        if (this.hasNoFuel() || this.random.nextInt(10) > 3) {
            return;
        }
        if (this.isMarried() && this.random.nextInt(5) == 0) {
            SoundEvent marrySound = ShinColleSoundHelper.getShipVoice(ShipSoundType.MARRY, this.getShipClassId());
            if (marrySound != null) {
                this.playSound(marrySound, this.getSoundVolume(), this.getVoicePitch());
                return;
            }
        }
        super.playAmbientSound();
    }

    @Override
    protected void playHurtSound(DamageSource source) {
        if (this.hurtSoundCooldown > 0) {
            return;
        }
        this.hurtSoundCooldown = 20 + this.random.nextInt(30);
        super.playHurtSound(source);
    }

    @Override
    protected @Nullable SoundEvent getAmbientSound() {
        if (LegacyShipBehaviorCatalog.shouldPlayRo500GaruruAmbient(
                this.getShipClassId(), this.random.nextInt(8))) {
            return ModSoundEvents.SHIP_GARURU.get();
        }
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
        return (float) ShinColleConfig.volumeShip();
    }

    @Override
    public SoundSource getSoundSource() {
        return this.isHostileVariant() ? SoundSource.HOSTILE : SoundSource.NEUTRAL;
    }

    @Override
    public boolean doHurtTarget(Entity target) {
        return this.performMeleeAttack(target, true);
    }

    public boolean performCollideAttack(LivingEntity target) {
        return this.performMeleeAttack(target, false);
    }

    private boolean performMeleeAttack(Entity target, boolean checkCooldown) {
        if (!(target instanceof LivingEntity livingTarget) || (checkCooldown && this.meleeAttackCooldown > 0)) {
            return false;
        }

        this.markLegacyCombat();

        this.emitLegacyModelAttack(LegacyShipAttackKind.MELEE);

        LegacyShipBehaviorCatalog.CombatHookResult hookResult =
                LegacyShipBehaviorCatalog.performCombatHook(this, livingTarget, LegacyShipAttackKind.MELEE);
        if (hookResult.handled()) {
            return true;
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
    public boolean causeFallDamage(float fallDistance, float multiplier, DamageSource source) {
        return false;
    }

    @Override
    public boolean hurt(DamageSource damageSource, float amount) {
        if (damageSource.is(DamageTypes.IN_WALL)
                || damageSource.is(DamageTypes.STARVE)
                || damageSource.is(DamageTypes.CACTUS)
                || damageSource.is(DamageTypes.FALL)) {
            return false;
        }
        if (damageSource.is(DamageTypes.FELL_OUT_OF_WORLD)) {
            if (!this.isHostileVariant()) {
                this.setOrderedToSit(false);
                this.stopRiding();
                this.teleportTo(this.getX(), 4.0D, this.getZ());
                this.setDeltaMovement(0.0D, 1.0D, 0.0D);
            } else {
                this.discard();
            }
            return false;
        }
        Entity directEntity = damageSource.getDirectEntity();
        boolean potionDamage = directEntity instanceof ThrownPotion || directEntity instanceof AreaEffectCloud;
        if (potionDamage) {
            List<MobEffectInstance> effects;
            if (directEntity instanceof ThrownPotion potion) {
                effects = PotionUtils.getMobEffects(potion.getItem());
            } else {
                CompoundTag cloudTag = new CompoundTag();
                directEntity.saveWithoutId(cloudTag);
                effects = PotionUtils.getAllEffects(cloudTag);
            }
            int potionLevel = 0;
            for (MobEffectInstance effect : effects) {
                if (effect.getEffect() == MobEffects.HARM) {
                    potionLevel = effect.getAmplifier() + 1;
                    break;
                }
            }
            amount += (Math.max(1F, this.getMaxHealth() * 0.01F) * 2F + 2F) * potionLevel;
        }
        if (!potionDamage && (damageSource.is(DamageTypes.MAGIC) || damageSource.is(DamageTypes.DRAGON_BREATH))) {
            if (amount < this.getMaxHealth() * 0.01F) {
                return false;
            }
            this.setEmotion(1);
            return super.hurt(damageSource, amount);
        }

        Entity attacker = directEntity != null ? directEntity : damageSource.getEntity();
        Entity hurtSourceEntity = damageSource.getEntity() != null ? damageSource.getEntity() : attacker;

        if (!this.level().isClientSide() && hurtSourceEntity instanceof Player playerAttacker) {
            if (!this.isHostileVariant() && this.isOwnedBy(playerAttacker)) {
                this.setOrderedToSit(false);
                this.setEmotion(1);
                return super.hurt(damageSource, amount);
            }
            if (!this.isHostileVariant()
                    && !ShinColleConfig.friendlyFire()) {
                return false;
            }
        }

        if (!this.level().isClientSide()
                && !damageSource.is(DamageTypeTags.BYPASSES_INVULNERABILITY)
                && this.goddessImmuneTicks > 0) {
            return false;
        }

        if (hurtSourceEntity == this) {
            this.setOrderedToSit(false);
            return false;
        }

        if (!this.level().isClientSide() && hurtSourceEntity instanceof LegacyShipEntity attackerShip) {
            if (!LegacyShipBehaviorCatalog.isLegacyFriendlyFireAllowed(attackerShip, this)) {
                return false;
            }
        }

        // Northern Hime and SSNH dismount before applying any damage.
        if (!this.level().isClientSide()
                && LegacyShipBehaviorCatalog.dismountsBeforeDamage(this.getSpec())
                && (this.getVehicle() instanceof LegacyShipEntity || this.getVehicle() instanceof Player)) {
            this.stopRiding();
        }

        if (!this.level().isClientSide()
                && hurtSourceEntity != null
                && LegacyShipCombatHelper.canDodge(this, hurtSourceEntity)) {
            if (hurtSourceEntity instanceof LivingEntity livingAttacker) {
                CombatFxDispatcher.sendCombatReact(livingAttacker, this, CombatReactType.DODGE, LegacyShipAttackKind.MELEE);
                CombatFxDispatcher.sendParticle(this, GameplayParticleType.TEXT_DODGE,
                        this.getX(), this.getY() + this.getBbHeight() * 0.8D, this.getZ());
            }
            return false;
        }

        if (!potionDamage) {
            amount = LegacyShipCombatHelper.applyDefenseReduction(this.random, this.legacyStats, amount);
        }
        LegacyShipEntity sourceShip = hurtSourceEntity instanceof LegacyShipEntity ship ? ship : null;
        if (hurtSourceEntity instanceof LegacyShipRensouhouEntity summon) {
            Entity host = this.level().getEntity(summon.getHostId());
            sourceShip = host instanceof LegacyShipEntity ship ? ship : null;
        } else if (hurtSourceEntity instanceof LegacyShipAircraftEntity aircraft) {
            Entity host = this.level().getEntity(aircraft.getOwnerId());
            sourceShip = host instanceof LegacyShipEntity ship ? ship : null;
        } else if (hurtSourceEntity instanceof LegacyShipFloatingFortEntity fort) {
            Entity host = this.level().getEntity(fort.getHostId());
            sourceShip = host instanceof LegacyShipEntity ship ? ship : null;
        } else if (hurtSourceEntity instanceof LegacyShipMountEntity mount) {
            sourceShip = mount.getHostShip();
        }
        if (!this.level().isClientSide() && (sourceShip != null && sourceShip.getOwnerUid() > 0
                || hurtSourceEntity instanceof LegacyMountEntity mount && mount.getOwnerUuid().isPresent())) {
            amount *= ShinColleConfig.dmgSvS() * 0.01F;
        }

        if (directEntity instanceof LegacyShipProjectileEntity) {
            MobEffectInstance resist = this.getEffect(MobEffects.DAMAGE_RESISTANCE);
            if (resist != null) {
                int lv = Math.min(4, resist.getAmplifier() + 1);
                amount *= (1.0F - lv * 0.20F);
            }
        } else if (directEntity instanceof LegacyShipEntity
                || directEntity instanceof LegacyShipAircraftEntity
                || directEntity instanceof LegacyShipRensouhouEntity
                || directEntity instanceof LegacyShipFloatingFortEntity
                || directEntity instanceof LegacyShipMountEntity
                || directEntity instanceof LegacyMountEntity
                || directEntity instanceof LegacyShipBeamEntity) {
            MobEffectInstance fireResist = this.getEffect(MobEffects.FIRE_RESISTANCE);
            if (fireResist != null) {
                int lv = Math.min(4, fireResist.getAmplifier() + 1);
                amount *= (1.0F - lv * 0.20F);
            }
        }

        amount = LegacyShipCombatHelper.applyLegacyLightDamageModifier(this, damageSource, amount);

        if (amount > 0.0F && amount < 1.0F) {
            amount = 1.0F;
        } else if (amount <= 0.0F) {
            amount = 0.0F;
        }

        if (!this.level().isClientSide()) {
            this.setOrderedToSit(false);
        }

        if (!this.level().isClientSide()
                && !this.isHostileVariant()
                && amount >= (this.getHealth() - 1.0F)
                && this.consumeFirstMatchingItem(ModItems.REPAIRGODDESS.get())) {
            this.setHealth(this.getMaxHealth());
            this.goddessImmuneTicks = 120;
            this.invulnerableTime = 120;
            this.lastHurt = Float.MAX_VALUE;
            CombatFxDispatcher.sendParticle(this, GameplayParticleType.FLARE_BURST,
                    this.getX(), this.getY() + this.getBbHeight() * 0.4D, this.getZ());
            return false;
        }

        if (!this.level().isClientSide()) {
            this.addMorale(-5);
            this.markLegacyCombat();
        }

        boolean hurt = super.hurt(damageSource, amount);
        if (hurt && !this.level().isClientSide()) {
            LegacyShipBehaviorCatalog.dismountRidersAfterDamage(this);
        }
        Entity sourceEntity = damageSource.getEntity();
        LegacyShipBehaviorCatalog.AttackEffectProfile reactionEffect =
                LegacyShipBehaviorCatalog.attackerReactionEffectFor(
                        this.getSpec(), this.getShipLevel(), this.getLegacyHostileScaleLevel());
        if (hurt
                && sourceEntity instanceof LivingEntity livingSource
                && sourceEntity != this.getOwnerPlayer()
                && !this.isAlliedTo(livingSource)
                && reactionEffect.isPresent()) {
            livingSource.addEffect(new MobEffectInstance(
                    reactionEffect.effect(),
                    reactionEffect.durationTicks(),
                    reactionEffect.amplifier(),
                    false,
                    false,
                    false));
        }
        return hurt;
    }

    @Override
    public void heal(float amount) {
        super.heal(amount * this.legacyStats.healRate());
    }

    @Override
    protected void dropCustomDeathLoot(DamageSource damageSource, int looting, boolean recentlyHit) {
        if (!this.isHostileVariant()) {
            if (!this.level().isClientSide()) {
                ItemStack recoveredEgg = ItemStack.EMPTY;
                if (this.canDropItem) {
                    // Legacy CanDropItem is consumed before the recovery stack serializes ship state.
                    this.canDropItem = false;
                    recoveredEgg = LegacyShipSpawnEggItem.createRecoveredShipStack(this);
                }
                this.ejectPassengers();
                this.getShipNavigate().clearPathEntity();
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
            this.getShipNavigate().clearPathEntity();
            this.setTarget(null);
            this.setNoAi(true);
            this.cacheShipState(true);
            this.dropHostileEgg();
            if (this.level() instanceof ServerLevel serverLevel && this.shipUid > 0) {
                TeitokuHelper.removeShipFromAllOnlineTeams(serverLevel.getServer(), this.shipUid);
            }
        }
    }

    @Override
    public int getExperienceReward() {
        if (!this.isHostileVariant()) {
            return super.getExperienceReward();
        }
        int scaleBonus = this.getLegacyHostileScaleLevel() * 30;
        return 10 + scaleBonus + this.random.nextInt(1 + scaleBonus);
    }

    @Override
    public void remove(Entity.RemovalReason removalReason) {
        LegacyShipFishingHookEntity hook = this.fishingHook;
        this.fishingHook = null;
        if (hook != null && !hook.isRemoved()) {
            hook.discard();
        }
        if (!this.level().isClientSide()) {
            boolean dead = removalReason == Entity.RemovalReason.KILLED
                    || removalReason == Entity.RemovalReason.DISCARDED
                    || !this.isAlive();
            this.cacheShipState(dead, false);
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
        if (LegacyShipBehaviorCatalog.handleBoredPlayerRiding(this, player, hand, stack)) {
            return this.level().isClientSide() ? InteractionResult.FAIL : InteractionResult.CONSUME;
        }
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
            this.playInteractionVoice(player, ShipSoundType.PICKITEM, 0.65F);
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
            this.playInteractionVoice(player, ShipSoundType.IDLE, 0.55F);
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
        this.entityData.set(DATA_COMBAT_RATION_LEVEL, DEFAULT_COMBAT_RATION_LEVEL);
        this.entityData.set(DATA_FOOD_SATURATION, 0);
        this.entityData.set(DATA_TASK, DEFAULT_TASK);
        this.entityData.set(DATA_TASK_SIDE, DEFAULT_TASK_SIDE);
        this.legacyTaskTime = 0;
        this.entityData.set(DATA_MODEL_STATE, DEFAULT_MODEL_STATE);
        this.entityData.set(DATA_HP_STATE, DEFAULT_HP_STATE);
        this.entityData.set(DATA_EMOTION, DEFAULT_EMOTION);
        this.entityData.set(DATA_EMOTION2, DEFAULT_EMOTION2);
        this.entityData.set(DATA_EMOTION4, DEFAULT_EMOTION4);
        this.entityData.set(DATA_DISPLAY_PHASE, DEFAULT_DISPLAY_PHASE);
        this.entityData.set(DATA_USE_MELEE, DEFAULT_USE_MELEE);
        this.entityData.set(DATA_USE_LIGHT, DEFAULT_USE_LIGHT);
        this.entityData.set(DATA_USE_HEAVY, DEFAULT_USE_HEAVY);
        this.entityData.set(DATA_USE_AIR_LIGHT, DEFAULT_USE_AIR_LIGHT);
        this.entityData.set(DATA_USE_AIR_HEAVY, DEFAULT_USE_AIR_HEAVY);
        this.entityData.set(DATA_MODEL_STATE, LegacyShipBehaviorCatalog.initialModelStateFor(this));
        this.aiOnSightChase = !spec.hostile();
        this.entityData.set(DATA_ON_SIGHT_CHASE, this.aiOnSightChase);
        this.aiPvpFirst = DEFAULT_AI_PVP_FIRST;
        this.aiAntiAir = DEFAULT_AI_ANTI_AIR;
        this.aiAntiSub = DEFAULT_AI_ANTI_SUB;
        this.entityData.set(DATA_PVP_FIRST, this.aiPvpFirst);
        this.entityData.set(DATA_ANTI_AIR, this.aiAntiAir);
        this.entityData.set(DATA_ANTI_SUB, this.aiAntiSub);
        this.entityData.set(DATA_CAN_FLOAT_UP, DEFAULT_CAN_FLOAT_UP);
        this.entityData.set(DATA_CAN_FOLLOW, DEFAULT_CAN_FOLLOW);
        this.entityData.set(DATA_SHOW_HELD_ITEM, DEFAULT_SHOW_HELD_ITEM);
        this.canDropItem = DEFAULT_CAN_DROP_ITEM;
        this.entityData.set(DATA_MARRIED, false);
        this.modernHealthSteps = 0;
        this.modernAttackSteps = 0;
        this.modernDefenseSteps = 0;
        this.modernAttackSpeedSteps = 0;
        this.modernSpeedSteps = 0;
        this.modernRangeSteps = 0;
        this.entityData.set(DATA_MODERNIZATION_TOTAL, 0);

        if (spec.hostile()) {
            this.clearOwner();
            this.setOrderedToSit(false);
        } else if (owner != null) {
            this.setOwner(owner);
        }

        this.initializeHostileRuntime(false, 0);
        this.refreshFromVariant(false);
    }

    public void restoreRecoveredDeployment(@Nullable Player fallbackOwner) {
        this.deathTime = 0;
        this.setNoAi(false);
        this.canDropItem = DEFAULT_CAN_DROP_ITEM;
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
        this.aircraftProfileInitialized = false;
        this.resetLegacyRePush();
        this.resetLegacyHimeRiding();
        ShipEntitySpec spec = ShipEntitySpecs.getByEggMeta(eggMeta);
        this.entityData.set(DATA_VARIANT_EGG_META, spec.eggMeta());
        this.entityData.set(DATA_RENSOUHOU_COUNT,
                spec.legacyClassId() == 36 || spec.legacyClassId() == 2036
                        ? (spec.hostile() ? 10 : 6) : 0);
        this.refreshFromVariant(false);
    }

    public int getShipClassId() {
        return this.getSpec().legacyClassId();
    }

    public boolean isHostileVariant() {
        return this.getSpec().hostile();
    }

    public int getLegacyRiderType() {
        return this.entityData.get(DATA_LEGACY_RIDER_TYPE);
    }

    public void setLegacyRiderType(int riderType) {
        this.entityData.set(DATA_LEGACY_RIDER_TYPE, Math.max(0, riderType));
    }

    public int getLegacyRidingState() {
        return this.entityData.get(DATA_LEGACY_RIDING_STATE);
    }

    public void setLegacyRidingState(int ridingState) {
        this.entityData.set(DATA_LEGACY_RIDING_STATE, Math.max(0, ridingState));
    }

    public boolean isLegacyRaiden() {
        return this.entityData.get(DATA_LEGACY_RAIDEN);
    }

    public void setLegacyRaiden(boolean raiden) {
        this.entityData.set(DATA_LEGACY_RAIDEN, raiden);
    }

    public int getLegacyFormationId() {
        return this.entityData.get(DATA_FORMATION_ID);
    }

    public boolean isBossEncounter() {
        return this.getLegacyHostileScaleLevel() > 1;
    }

    public int getLegacyHostileScaleLevel() {
        return this.isHostileVariant() ? this.entityData.get(DATA_HOSTILE_SCALE) : 0;
    }

    public boolean isOrderedToSit() {
        return this.entityData.get(DATA_ORDERED_TO_SIT);
    }

    public void setOrderedToSit(boolean orderedToSit) {
        this.entityData.set(DATA_ORDERED_TO_SIT, orderedToSit);

        if (orderedToSit) {
            this.getShipNavigate().clearPathEntity();
            this.getNavigation().stop();
        }
    }

    public int getShipLevel() {
        return this.isHostileVariant() ? HOSTILE_LEVEL : this.entityData.get(DATA_LEVEL);
    }

    public void setShipLevel(int targetLevel) {
        int clampedLevel = Mth.clamp(targetLevel, DEFAULT_LEVEL, this.getLevelCap());
        this.entityData.set(DATA_LEVEL, clampedLevel);
        this.refreshFromVariant(true);
    }

    public int getLevelCap() {
        return this.isMarried() ? 150 : 100;
    }

    public int getShipExperience() {
        return this.entityData.get(DATA_EXPERIENCE);
    }

    public void setShipExperience(int experience) {
        this.entityData.set(DATA_EXPERIENCE, Math.max(0, experience));
    }

    public int getShipExperienceNext() {
        return LegacyShipBehaviorCatalog.experienceToNext(this.getShipLevel());
    }

    /** Mirrors ShipStateHandler.addShipExp; callers must invoke this on the server. */
    public void addShipExperience(int experience) {
        if (this.level().isClientSide() || experience <= 0 || this.getShipLevel() >= this.getLevelCap()) {
            return;
        }

        int gainedExperience = Math.max(0, (int) (experience * this.legacyStats.get(LegacyShipStatTables.Attr.XP)));
        int currentExperience = this.getShipExperience() + gainedExperience;
        int currentLevel = this.getShipLevel();
        int levelCap = this.getLevelCap();

        while (currentLevel < levelCap) {
            int experienceNext = LegacyShipBehaviorCatalog.experienceToNext(currentLevel);
            if (currentExperience < experienceNext) {
                break;
            }

            currentExperience -= experienceNext;
            currentLevel++;
            this.playShipCombatSound(SoundEvents.PLAYER_LEVELUP, 0.7F, 1.0F);
            if (this.random.nextInt(4) == 0) {
                this.playShipCombatSound(ModSoundEvents.SHIP_LEVELUP.get(), 1.0F, 1.0F);
            }
        }

        this.setShipExperience(currentExperience);
        if (currentLevel != this.getShipLevel()) {
            this.entityData.set(DATA_LEVEL, currentLevel);
            this.refreshFromVariant(true);
            this.setHealth(this.getMaxHealth());
        }
    }

    public int getMorale() {
        return this.isHostileVariant() ? 0 : this.entityData.get(DATA_MORALE);
    }

    public void setMorale(int morale) {
        this.entityData.set(DATA_MORALE, Mth.clamp(morale, 0, MAX_MORALE));
        this.refreshFromVariant(true);
    }

    public void addMorale(int amount) {
        this.setMorale(this.getMorale() + amount);
    }

    /** Mirrors EntityHelper.checkShipOutOfCombat's strict 128-tick boundary. */
    public boolean isLegacyOutOfCombat() {
        return this.tickCount - this.legacyLastCombatTick > 128;
    }

    /** Records the legacy combat timestamp when an attack is accepted. */
    public void markLegacyCombat() {
        this.legacyLastCombatTick = this.tickCount;
    }

    public void setLegacyHitZone(int hitHeight, int hitAngle) {
        this.legacyHitHeight = Mth.clamp(hitHeight, -1, 100);
        this.legacyHitAngle = Mth.clamp(hitAngle, -180, 180);
    }

    public int getLegacyHitHeight() {
        return this.legacyHitHeight;
    }

    public int getLegacyModelEmotion(int id) {
        Integer local = this.legacyModelEmotionOverrides[id];
        if (local != null) return local;
        return switch (id) {
            case 0 -> this.getModelState();
            case 1 -> this.getEmotion();
            case 2 -> this.getEmotion2();
            case 5 -> this.getDisplayPhase();
            case 6 -> 0;
            case 7 -> this.getEmotion4();
            default -> throw new IllegalArgumentException("Unsupported model emotion " + id);
        };
    }

    public void setLegacyModelEmotion(int id, int value, boolean sync) {
        if (!sync || id == 6) {
            this.legacyModelEmotionOverrides[id] = value;
            return;
        }
        switch (id) {
            case 0 -> this.setModelState(value);
            case 1 -> this.setEmotion(value);
            case 2 -> this.setEmotion2(value);
            case 5 -> this.setDisplayPhase(value);
            case 7 -> this.setEmotion4(value);
            default -> throw new IllegalArgumentException("Unsupported model emotion " + id);
        }
    }

    public void startLegacyModelCaress() {
        this.legacyModelEmotionOverrides[6] = 1;
        this.legacyModelCaressTicks = 80;
    }

    public int getLegacyModelFaceTick() { return this.legacyModelFaceTick; }
    public void setLegacyModelFaceTick(int value) { this.legacyModelFaceTick = value; }
    public int getLegacyModelHeadTiltTick() { return this.legacyModelHeadTiltTick; }
    public void setLegacyModelHeadTiltTick(int value) { this.legacyModelHeadTiltTick = value; }
    public int getLegacyModelAttackTick() { return this.legacyModelAttackTick; }
    public void setLegacyModelAttackTick(int value) { this.legacyModelAttackTick = value; }
    public int getLegacyModelAttackTick2() { return this.legacyModelAttackTick2; }
    public void setLegacyModelAttackTick2(int value) { this.legacyModelAttackTick2 = value; }
    public boolean getLegacyModelHeadTilt() { return this.legacyModelHeadTilt; }
    public void setLegacyModelHeadTilt(boolean value) { this.legacyModelHeadTilt = value; }
    public float getLegacyModelRotate(int axis) { return this.legacyModelRotate[axis]; }
    public void setLegacyModelRotate(int axis, float value) { this.legacyModelRotate[axis] = value; }

    public int getLegacyHitAngle() {
        return this.legacyHitAngle;
    }

    public int getCombatRationLevel() {
        return this.entityData.get(DATA_COMBAT_RATION_LEVEL);
    }

    public void setCombatRationLevel(int level) {
        this.entityData.set(DATA_COMBAT_RATION_LEVEL, level);
        this.setPersistenceRequired();
    }

    public int getFleeHp() {
        return this.entityData.get(DATA_FLEE_HP);
    }

    public void setFleeHp(int fleeHp) {
        this.entityData.set(DATA_FLEE_HP, Mth.clamp(fleeHp, 0, 100));
        this.setPersistenceRequired();
    }

    public int getFoodSaturation() {
        return this.entityData.get(DATA_FOOD_SATURATION);
    }

    public void setFoodSaturation(int saturation) {
        this.entityData.set(DATA_FOOD_SATURATION, saturation);
        this.setPersistenceRequired();
    }

    public void addFoodSaturation(int amount) {
        this.setFoodSaturation(this.getFoodSaturation() + amount);
    }

    public int getFoodSaturationMax() {
        return LegacyShipBehaviorCatalog.foodSaturationMaxFor(this.getSpec());
    }

    public int getTask() {
        return this.entityData.get(DATA_TASK);
    }

    public void setTask(int task) {
        this.entityData.set(DATA_TASK, task);
        this.setPersistenceRequired();
    }

    /** Legacy TaskSide uses bits 0-17 for item sides and bits 18-20 for metadata checks. */
    public int getTaskSide() {
        return this.entityData.get(DATA_TASK_SIDE);
    }

    public void setTaskSide(int taskSide) {
        this.entityData.set(DATA_TASK_SIDE, taskSide);
        this.setPersistenceRequired();
    }

    public int getLegacyTaskTime() {
        return this.legacyTaskTime;
    }

    public void setLegacyTaskTime(int taskTime) {
        this.legacyTaskTime = Math.max(0, taskTime);
        this.setPersistenceRequired();
    }

    /** Returns the task main-hand slot used by BasicEntityShip, not the rider-facing hand. */
    public ItemStack getLegacyTaskMainHandItem() {
        return this.shipInventory.getItem(LEGACY_MAIN_HAND_SLOT);
    }

    /** Returns the task off-hand slot used by BasicEntityShip, not the rider-facing hand. */
    public ItemStack getLegacyTaskOffHandItem() {
        return this.shipInventory.getItem(LEGACY_OFF_HAND_SLOT);
    }

    public @Nullable LegacyShipFishingHookEntity getFishingHook() {
        return this.fishingHook;
    }

    public void setFishingHook(@Nullable LegacyShipFishingHookEntity fishingHook) {
        this.fishingHook = fishingHook;
    }

    public void clearFishingHook(LegacyShipFishingHookEntity fishingHook) {
        if (this.fishingHook == fishingHook) {
            this.fishingHook = null;
        }
    }

    /** Legacy Display.State is a model-part bitmask, not the vanilla entity Pose. */
    public int getModelState() {
        return this.entityData.get(DATA_MODEL_STATE);
    }

    public void setModelState(int modelState) {
        this.entityData.set(DATA_MODEL_STATE, modelState);
        this.setPersistenceRequired();
    }

    /** Legacy Display.HPState tracks normal, minor, moderate, and heavy damage. */
    public int getHpState() {
        return this.entityData.get(DATA_HP_STATE);
    }

    public void setHpState(int hpState) {
        this.entityData.set(DATA_HP_STATE, Mth.clamp(hpState, 0, 3));
        this.setPersistenceRequired();
    }

    /** Legacy Display.Emotion controls face emotion values consumed by the old model layer. */
    public int getEmotion() {
        return this.entityData.get(DATA_EMOTION);
    }

    public void setEmotion(int emotion) {
        this.entityData.set(DATA_EMOTION, emotion);
        this.setPersistenceRequired();
    }

    /** Legacy Display.Emotion2 stores the head-tilt completion state. */
    public int getEmotion2() {
        return this.entityData.get(DATA_EMOTION2);
    }

    public void setEmotion2(int emotion2) {
        this.entityData.set(DATA_EMOTION2, emotion2);
        this.setPersistenceRequired();
    }

    /** Legacy Display.Emotion4 controls the alternate sitting pose emotion. */
    public int getEmotion4() {
        return this.entityData.get(DATA_EMOTION4);
    }

    public void setEmotion4(int emotion4) {
        this.entityData.set(DATA_EMOTION4, emotion4);
        this.setPersistenceRequired();
    }

    /** 舰种专属动画与攻击的旧版 Display.Phase。 */
    public int getDisplayPhase() {
        return this.entityData.get(DATA_DISPLAY_PHASE);
    }

    public void setDisplayPhase(int phase) {
        this.entityData.set(DATA_DISPLAY_PHASE, phase);
        this.setPersistenceRequired();
    }

    public int getLegacySkillTimer() {
        return this.legacySkillTimer;
    }

    public void setLegacySkillTimer(int timer) {
        this.legacySkillTimer = Math.max(0, timer);
        this.setPersistenceRequired();
    }

    public int getLegacySkillRemaining() {
        return this.legacySkillRemaining;
    }

    public void setLegacySkillRemaining(int remaining) {
        this.legacySkillRemaining = Math.max(0, remaining);
        this.setPersistenceRequired();
    }

    public Vec3 getLegacySkillMotion() {
        return this.legacySkillMotion;
    }

    public void setLegacySkillMotion(Vec3 motion) {
        this.legacySkillMotion = motion == null ? Vec3.ZERO : motion;
        this.setPersistenceRequired();
    }

    public BlockPos getLegacySkillTarget() {
        return this.legacySkillTarget;
    }

    public void setLegacySkillTarget(BlockPos target) {
        this.legacySkillTarget = target == null ? BlockPos.ZERO : target.immutable();
        this.setPersistenceRequired();
    }

    public void resetLegacySkillState() {
        this.setDisplayPhase(DEFAULT_DISPLAY_PHASE);
        this.legacySkillTimer = 0;
        this.legacySkillRemaining = 0;
        this.legacySkillMotion = Vec3.ZERO;
        this.legacySkillTarget = BlockPos.ZERO;
        this.legacySkillDamagedTargetIds.clear();
        this.setDeltaMovement(Vec3.ZERO);
    }

    public boolean isLegacyRePushActive() {
        return this.legacyRePushTargetId >= 0;
    }

    public void beginLegacyRePush(@Nullable LivingEntity target) {
        this.legacyRePushTargetId = target == null ? -1 : target.getId();
        this.legacyRePushTicks = 0;
    }

    public @Nullable LivingEntity getLegacyRePushTarget() {
        if (this.legacyRePushTargetId < 0) {
            return null;
        }

        Entity entity = this.level().getEntity(this.legacyRePushTargetId);
        return entity instanceof LivingEntity living ? living : null;
    }

    public int getLegacyRePushTicks() {
        return this.legacyRePushTicks;
    }

    public void setLegacyRePushTicks(int ticks) {
        this.legacyRePushTicks = Math.max(0, ticks);
    }

    public void resetLegacyRePush() {
        this.legacyRePushTargetId = -1;
        this.legacyRePushTicks = 0;
    }

    public boolean isLegacyHimeRidingActive() {
        return this.legacyHimeRidingTargetId >= 0;
    }

    public void beginLegacyHimeRiding(@Nullable LivingEntity target) {
        this.legacyHimeRidingTargetId = target == null ? -1 : target.getId();
        this.legacyHimeRidingTicks = 0;
    }

    public @Nullable LivingEntity getLegacyHimeRidingTarget() {
        if (this.legacyHimeRidingTargetId < 0) {
            return null;
        }

        Entity entity = this.level().getEntity(this.legacyHimeRidingTargetId);
        return entity instanceof LivingEntity living ? living : null;
    }

    public int getLegacyHimeRidingTicks() {
        return this.legacyHimeRidingTicks;
    }

    public void setLegacyHimeRidingTicks(int ticks) {
        this.legacyHimeRidingTicks = Math.max(0, ticks);
    }

    public void resetLegacyHimeRiding() {
        this.legacyHimeRidingTargetId = -1;
        this.legacyHimeRidingTicks = 0;
    }

    boolean hasLegacySkillDamagedTarget(int entityId) {
        return this.legacySkillDamagedTargetIds.contains(entityId);
    }

    void markLegacySkillDamagedTarget(int entityId) {
        this.legacySkillDamagedTargetIds.add(entityId);
    }

    void clearLegacySkillDamagedTargets() {
        this.legacySkillDamagedTargetIds.clear();
    }

    public int getShimakazeRensouhouCount() {
        return this.entityData.get(DATA_RENSOUHOU_COUNT);
    }

    public void setShimakazeRensouhouCount(int count) {
        int maximum = this.isShimakazeVariant()
                ? (this.isHostileVariant() ? 10 : 6)
                : this.isBattleshipTaVariant() ? 6 : 0;
        this.entityData.set(DATA_RENSOUHOU_COUNT, Mth.clamp(count, 0, maximum));
        this.setPersistenceRequired();
    }

    public void addShimakazeRensouhouCount(int amount) {
        this.setShimakazeRensouhouCount(this.getShimakazeRensouhouCount() + amount);
    }

    /** Legacy ShipFlags.Melee controls whether the autonomous melee goal is installed. */
    public boolean useAttackMelee() {
        return this.entityData.get(DATA_USE_MELEE);
    }

    public void setUseAttackMelee(boolean enabled) {
        this.entityData.set(DATA_USE_MELEE, enabled);
        this.updateMeleeGoal();
        this.setPersistenceRequired();
    }

    private void updateMeleeGoal() {
        if (this.meleeGoal == null || this.level().isClientSide()) {
            return;
        }
        boolean enabled = this.useAttackMelee() && !this.isHostileVariant();
        if (enabled != this.meleeGoalRegistered) {
            if (enabled) {
                this.goalSelector.addGoal(15, this.meleeGoal);
            } else {
                this.goalSelector.removeGoal(this.meleeGoal);
            }
            this.meleeGoalRegistered = enabled;
        }
    }

    /** Legacy ShipFlags.AmmoL controls the autonomous light cannon channel. */
    public boolean useAttackLight() {
        return this.entityData.get(DATA_USE_LIGHT);
    }

    public void setUseAttackLight(boolean enabled) {
        this.entityData.set(DATA_USE_LIGHT, enabled);
        this.setPersistenceRequired();
    }

    /** Legacy ShipFlags.AmmoH controls the autonomous heavy cannon channel. */
    public boolean useAttackHeavy() {
        return this.entityData.get(DATA_USE_HEAVY);
    }

    public void setUseAttackHeavy(boolean enabled) {
        this.entityData.set(DATA_USE_HEAVY, enabled);
        this.setPersistenceRequired();
    }

    /** Legacy ShipFlags.AirL controls the autonomous light aircraft channel. */
    public boolean useAttackAirLight() {
        return this.entityData.get(DATA_USE_AIR_LIGHT);
    }

    public void setUseAttackAirLight(boolean enabled) {
        this.entityData.set(DATA_USE_AIR_LIGHT, enabled);
        this.setPersistenceRequired();
    }

    /** Legacy ShipFlags.AirH controls the autonomous heavy aircraft channel. */
    public boolean useAttackAirHeavy() {
        return this.entityData.get(DATA_USE_AIR_HEAVY);
    }

    public void setUseAttackAirHeavy(boolean enabled) {
        this.entityData.set(DATA_USE_AIR_HEAVY, enabled);
        this.setPersistenceRequired();
    }

    public boolean hasNoFuel() {
        return !this.isAlive() || (!this.isHostileVariant() && this.getGrudge() <= 0);
    }

    public int getLightAmmo() {
        return this.entityData.get(DATA_LIGHT_AMMO);
    }

    public void setLightAmmo(int ammo) {
        this.entityData.set(DATA_LIGHT_AMMO, Math.max(0, ammo));
    }

    public void addLightAmmo(int amount) {
        this.setLightAmmo(this.getLightAmmo() + amount);
    }

    public int getHeavyAmmo() {
        return this.entityData.get(DATA_HEAVY_AMMO);
    }

    public void setHeavyAmmo(int ammo) {
        this.entityData.set(DATA_HEAVY_AMMO, Math.max(0, ammo));
    }

    public void addHeavyAmmo(int amount) {
        this.setHeavyAmmo(this.getHeavyAmmo() + amount);
    }

    public int getAircraftLight() {
        return this.entityData.get(DATA_AIRCRAFT_LIGHT);
    }

    public int getAircraftHeavy() {
        return this.entityData.get(DATA_AIRCRAFT_HEAVY);
    }

    public int getMaxAircraftLight() {
        return this.maxAircraftLight;
    }

    public int getMaxAircraftHeavy() {
        return this.maxAircraftHeavy;
    }

    public void setAircraftLight(int amount) {
        int value = Math.max(0, amount);
        if (!this.level().isClientSide() && this.maxAircraftLight > 0) {
            value = Math.min(value, this.maxAircraftLight);
        }
        this.entityData.set(DATA_AIRCRAFT_LIGHT, value);
        this.setPersistenceRequired();
    }

    public void setAircraftHeavy(int amount) {
        int value = Math.max(0, amount);
        if (!this.level().isClientSide() && this.maxAircraftHeavy > 0) {
            value = Math.min(value, this.maxAircraftHeavy);
        }
        this.entityData.set(DATA_AIRCRAFT_HEAVY, value);
        this.setPersistenceRequired();
    }

    public boolean hasAircraftFor(LegacyShipAttackKind attackKind) {
        if (attackKind != LegacyShipAttackKind.AIR_LIGHT && attackKind != LegacyShipAttackKind.AIR_HEAVY) {
            return true;
        }
        if (!LegacyShipBehaviorCatalog.managesAircraftInventory(this.getSpec())) {
            return true;
        }
        return attackKind == LegacyShipAttackKind.AIR_LIGHT
                ? this.getAircraftLight() > 0
                : this.getAircraftHeavy() > 0;
    }

    /** Called when a launched aircraft reaches its legacy host return point. */
    void recoverAircraft(LegacyShipAttackKind attackKind) {
        this.recoverAircraft(attackKind, 0);
    }

    /** Mirrors BasicEntityAirplane.returnSummonResource's reserve-ammo refund. */
    void recoverAircraft(LegacyShipAttackKind attackKind, int remainingAircraftAmmo) {
        if (this.level().isClientSide() || !LegacyShipBehaviorCatalog.managesAircraftInventory(this.getSpec())) {
            return;
        }

        if (!this.isHostileVariant()) {
            int reserveAmmo = attackKind == LegacyShipAttackKind.AIR_LIGHT ? 3 : 1;
            int refundableAmmo = Math.max(0, remainingAircraftAmmo - reserveAmmo);
            int ammoRefund = refundableAmmo * LegacyShipBehaviorCatalog.ammoConsumptionFor(this.getSpec());
            if (attackKind == LegacyShipAttackKind.AIR_LIGHT) {
                this.addLightAmmo(ammoRefund);
            } else if (attackKind == LegacyShipAttackKind.AIR_HEAVY) {
                this.addHeavyAmmo(ammoRefund);
            }
        }

        if (attackKind == LegacyShipAttackKind.AIR_LIGHT) {
            this.setAircraftLight(this.getAircraftLight() + 1);
        } else if (attackKind == LegacyShipAttackKind.AIR_HEAVY) {
            this.setAircraftHeavy(this.getAircraftHeavy() + 1);
        }
    }

    public int getGrudge() {
        return this.entityData.get(DATA_GRUDGE);
    }

    public void setGrudge(int grudge) {
        this.entityData.set(DATA_GRUDGE, Math.max(0, grudge));
    }

    public void addGrudge(int amount) {
        this.setGrudge(this.getGrudge() + amount);
    }

    public String getLightAmmoText() {
        return Integer.toString(this.getLightAmmo());
    }

    public String getHeavyAmmoText() {
        return Integer.toString(this.getHeavyAmmo());
    }

    public String getGrudgeText() {
        return Integer.toString(this.getGrudge());
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

    public boolean isRingEffectEnabled() {
        return this.entityData.get(DATA_RING_EFFECT);
    }

    public void setRingEffectEnabled(boolean enabled) {
        this.entityData.set(DATA_RING_EFFECT, enabled);
        this.setPersistenceRequired();
        this.refreshFromVariant(true);
    }

    public boolean isTimeKeeper() {
        return this.entityData.get(DATA_TIME_KEEPER);
    }

    public void setTimeKeeper(boolean enabled) {
        this.entityData.set(DATA_TIME_KEEPER, enabled);
        this.setPersistenceRequired();
    }

    public boolean isAutoPump() {
        return this.entityData.get(DATA_AUTO_PUMP);
    }

    public void setAutoPump(boolean enabled) {
        this.entityData.set(DATA_AUTO_PUMP, enabled);
        this.setPersistenceRequired();
    }

    public int getModernizationCount() {
        return this.modernHealthSteps
                + this.modernAttackSteps
                + this.modernDefenseSteps
                + this.modernAttackSpeedSteps
                + this.modernSpeedSteps
                + this.modernRangeSteps;
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
        return this.level().isClientSide() ? this.entityData.get(DATA_MODEL_SHIP_UID) : this.shipUid;
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
        return ShipEquipmentProfile.canEquip(this.getSpec(), stack);
    }

    public boolean isAiAutoTarget() {
        return this.aiAutoTarget;
    }

    public void setAiAutoTarget(boolean aiAutoTarget) {
        this.aiAutoTarget = aiAutoTarget;
        this.entityData.set(DATA_AI_FLAGS, this.encodeAiFlags());
    }

    /** Legacy OnSightChase drops a friendly ship's target as soon as line of sight is lost. */
    public boolean isAiOnSightChase() {
        return !this.isHostileVariant() && this.aiOnSightChase;
    }

    public void setAiOnSightChase(boolean aiOnSightChase) {
        this.aiOnSightChase = !this.isHostileVariant() && aiOnSightChase;
        this.entityData.set(DATA_ON_SIGHT_CHASE, this.aiOnSightChase);
    }

    public boolean isAiPvpFirst() {
        return this.aiPvpFirst;
    }

    public void setAiPvpFirst(boolean aiPvpFirst) {
        this.aiPvpFirst = aiPvpFirst;
        this.entityData.set(DATA_PVP_FIRST, aiPvpFirst);
        this.setPersistenceRequired();
    }

    public boolean isAiAntiAir() {
        return this.aiAntiAir;
    }

    public void setAiAntiAir(boolean aiAntiAir) {
        this.aiAntiAir = aiAntiAir;
        this.entityData.set(DATA_ANTI_AIR, aiAntiAir);
        this.setPersistenceRequired();
    }

    public boolean isAiAntiSub() {
        return this.aiAntiSub;
    }

    public void setAiAntiSub(boolean aiAntiSub) {
        this.aiAntiSub = aiAntiSub;
        this.entityData.set(DATA_ANTI_SUB, aiAntiSub);
        this.setPersistenceRequired();
    }

    public boolean canFloatUp() {
        return this.entityData.get(DATA_CAN_FLOAT_UP);
    }

    public void setCanFloatUp(boolean canFloatUp) {
        this.entityData.set(DATA_CAN_FLOAT_UP, canFloatUp);
        this.setPersistenceRequired();
    }

    public boolean canFollow() {
        return this.entityData.get(DATA_CAN_FOLLOW);
    }

    public void setCanFollow(boolean canFollow) {
        this.entityData.set(DATA_CAN_FOLLOW, canFollow);
        this.setPersistenceRequired();
    }

    /** Legacy ShipFlags.HeldItem controls the client-side held-item display gate. */
    public boolean canShowHeldItem() {
        return this.entityData.get(DATA_SHOW_HELD_ITEM);
    }

    public void setShowHeldItem(boolean showHeldItem) {
        this.entityData.set(DATA_SHOW_HELD_ITEM, showHeldItem);
        this.setPersistenceRequired();
    }

    /** Legacy CanDropItem is server-side and prevents duplicate recovery/hostile egg drops. */
    public boolean canDropItem() {
        return this.canDropItem;
    }

    public void setCanDropItem(boolean canDropItem) {
        this.canDropItem = canDropItem;
        this.setPersistenceRequired();
    }

    public double getLegacyLiquidDepth() {
        return this.legacyLiquidDepth;
    }

    public double getLegacyFloatingDepth() {
        return LegacyShipBehaviorCatalog.floatingDepthFor(this.getSpec(), this.getLegacyHostileScaleLevel());
    }

    public boolean canUseLegacyFloatingGoal() {
        return this.canFloatUp()
                && this.legacyLiquidDepth > this.getLegacyFloatingDepth()
                && !this.isPassenger()
                && !this.isOrderedToSit()
                && !LegacyShipBehaviorCatalog.isLegacySkillMovementActive(this)
                && this.getShipNavigate().noPath()
                && !this.isAtLegacyGuardPosition();
    }

    public void applyLegacyFloatingMotion() {
        double lift = this.legacyLiquidDepth > 4.0D ? 0.025D
                : this.legacyLiquidDepth > 2.0D ? 0.015D
                : this.legacyLiquidDepth > 1.3D ? 0.007D
                : this.legacyLiquidDepth > 0.47D ? 0.003D
                : this.legacyLiquidDepth > 0.15D ? 0.0015D : 0.0D;
        if (lift > 0.0D) {
            this.setDeltaMovement(this.getDeltaMovement().add(0.0D, lift, 0.0D));
        }
    }

    public boolean isAiPickItem() {
        return this.aiPickItem;
    }

    public void setAiPickItem(boolean aiPickItem) {
        this.aiPickItem = aiPickItem;
        this.entityData.set(DATA_AI_FLAGS, this.encodeAiFlags());
    }

    /** Legacy CapaShipInventory.inventoryPage; not persisted, like the legacy capability field. */
    public int getInventoryPage() {
        return this.entityData.get(DATA_INVENTORY_PAGE);
    }

    public void setInventoryPage(int page) {
        this.entityData.set(DATA_INVENTORY_PAGE, page >= 0 && page < INVENTORY_PAGE_COUNT ? page : 0);
    }

    /** Legacy DrumState: +1 per drum equipment (EquipCalc misc[0]); EntityTransportWa forces 2. */
    public int getInventoryPageSize() {
        if (!this.isHostileVariant() && this.getSpec().legacyClassId() == LEGACY_TRANSPORT_WA_CLASS_ID) {
            return 2;
        }

        int drums = 0;
        for (int slot = 0; slot < EQUIPMENT_SLOT_COUNT; slot++) {
            if (this.shipInventory.getItem(slot).getItem() instanceof LegacyEquipmentItem equipment
                    && equipment.family() == LegacyEquipmentFamily.DRUM) {
                drums++;
            }
        }
        return drums;
    }

    /** Legacy CapaShipInventory.isSlotAvailable / getSizeInventoryPaged. */
    public int getInventorySizePaged() {
        return SHIP_SLOT_COUNT + Math.min(this.getInventoryPageSize(), INVENTORY_PAGE_COUNT - 1) * CARGO_SLOT_COUNT;
    }

    /** Legacy StateMinor FollowMin. */
    public int getFollowMin() {
        return this.entityData.get(DATA_FOLLOW_MIN);
    }

    public void setFollowMin(int followMin) {
        this.entityData.set(DATA_FOLLOW_MIN, followMin);
        this.setPersistenceRequired();
    }

    /** Legacy StateMinor FollowMax. */
    public int getFollowMax() {
        return this.entityData.get(DATA_FOLLOW_MAX);
    }

    public void setFollowMax(int followMax) {
        this.entityData.set(DATA_FOLLOW_MAX, followMax);
        this.setPersistenceRequired();
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

    private int cachedFormationSlot = 0;

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

    private int resolveAppliedFormationSlot() {
        if (this.isHostileVariant()) {
            return 0;
        }

        Player owner = this.getOwnerPlayer();
        if (owner == null) {
            return 0;
        }

        return TeitokuHelper.getFormationSlotForShip(owner, this.shipUid);
    }

    private double resolveEffectiveMoveSpeed(int formationId) {
        if (formationId <= 0 || this.isHostileVariant() || !(this.level() instanceof ServerLevel serverLevel)) {
            return this.legacyStats.moveSpeed();
        }

        Player owner = this.getOwnerPlayer();
        if (owner == null) {
            return this.legacyStats.moveSpeed();
        }

        int[] teamUids = TeitokuHelper.getTeamShipUidsForShip(owner, this.shipUid);
        if (teamUids.length == 0) {
            return this.legacyStats.moveSpeed();
        }

        float minMov = this.legacyStats.moveSpeed();
        for (Entity entity : serverLevel.getAllEntities()) {
            if (!(entity instanceof LegacyShipEntity ship) || !ship.isAlive() || ship.isHostileVariant()) {
                continue;
            }
            int uid = ship.getShipUid();
            if (uid <= 0) {
                continue;
            }
            for (int teamUid : teamUids) {
                if (uid == teamUid) {
                    minMov = Math.min(minMov, ship.getLegacyStats().moveSpeed());
                    break;
                }
            }
        }

        double formationMov = this.legacyStats.formationStat(LegacyShipStatTables.Attr.MOV)
                * LegacyShipStatTables.SCALE_SHIP[LegacyShipStatTables.BaseAttr.MOV];
        return Mth.clamp(minMov + formationMov, 0.0D,
                LegacyShipStatTables.LIMIT_SHIP_ATTRS[LegacyShipStatTables.Attr.MOV]);
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
        this.legacyGuardType = LEGACY_GUARD_BLOCK_TYPE;
        this.setCanFollow(false);
        this.guardEntityUuid = null;
        this.guardedBlockPos = null;
        this.guardedBlockDimension = "";
        this.setTarget(null);
        BlockEntity blockEntity = this.level().getBlockEntity(pos);
        this.routeNodePos = blockEntity instanceof RouteNode ? pos.immutable() : null;
        this.routeWaitTicks = 0;
        this.craneState = 0;
        this.routeTransferCooldown = 0;
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

    public @Nullable BlockPos getGuardedBlockPos() {
        return this.guardedBlockPos;
    }

    public String getGuardedBlockDimension() {
        return this.guardedBlockDimension;
    }

    public @Nullable BlockPos getRouteNodePos() {
        return this.routeNodePos;
    }

    public @Nullable BlockPos getLastWaypoint() {
        return this.lastWaypointPos;
    }

    public void setLastWaypoint(@Nullable BlockPos pos) {
        this.lastWaypointPos = pos == null ? null : pos.immutable();
    }

    public int getLegacyCraneState() {
        return this.craneState;
    }

    public int getLegacyFormationSlot() {
        return this.resolveAppliedFormationSlot();
    }

    public int getRouteWaitTicks() {
        return this.routeWaitTicks;
    }

    public int getWaypointStayMode() {
        return this.entityData.get(DATA_WP_STAY_MODE);
    }

    public void setWaypointStayMode(int waypointStayMode) {
        this.waypointStayMode = Mth.clamp(waypointStayMode, DEFAULT_WP_STAY_MODE, MAX_WP_STAY_MODE);
        this.entityData.set(DATA_WP_STAY_MODE, this.waypointStayMode);
    }

    public int getWaypointStayTicks() {
        return WaypointBlockEntity.stayModeToTicks(this.getWaypointStayMode());
    }

    public void commandGuardEntity(UUID entityUuid) {
        this.legacyGuardType = 2;
        this.setCanFollow(false);
        this.guardEntityUuid = entityUuid;
        this.guardedBlockPos = null;
        this.guardedBlockDimension = "";
        this.commandedPos = null;
        this.commandDimension = this.level().dimension().location().toString();
        this.routeNodePos = null;
        this.setTarget(null);
        this.routeWaitTicks = 0;
        this.routeTransferCooldown = 0;
    }

    public void commandGuardBlock(@Nullable BlockPos pos, String dimensionId) {
        if (pos == null) {
            this.clearCommandState();
            return;
        }

        this.guardedBlockPos = pos.immutable();
        this.legacyGuardType = LEGACY_GUARD_BLOCK_TYPE;
        this.guardedBlockDimension = dimensionId == null ? "" : dimensionId;
        this.setCanFollow(false);
        this.guardEntityUuid = null;
        this.commandedPos = null;
        this.commandDimension = "";
        BlockEntity blockEntity = this.level().getBlockEntity(pos);
        this.routeNodePos = blockEntity instanceof RouteNode ? pos.immutable() : null;
        this.setTarget(null);
        this.routeWaitTicks = 0;
        this.routeTransferCooldown = 0;
    }

    public boolean isGuardingBlockAt(BlockPos pos, String dimensionId) {
        return this.guardedBlockPos != null
                && this.guardedBlockPos.equals(pos)
                && (this.guardedBlockDimension.isBlank()
                ? dimensionId == null || dimensionId.isBlank()
                : this.guardedBlockDimension.equals(dimensionId));
    }

    public void clearCommandState() {
        this.craneState = 0;
        this.legacyGuardType = 0;
        this.commandedPos = null;
        this.commandDimension = "";
        this.guardEntityUuid = null;
        this.guardedBlockPos = null;
        this.guardedBlockDimension = "";
        this.routeNodePos = null;
        this.routeWaitTicks = 0;
        this.routeTransferCooldown = 0;
        this.setCanFollow(true);
    }

    public boolean canExecuteCombatGoal() {
        return this.canExecuteActiveGoal()
                && !this.hasExplicitMovementCommand()
                && !LegacyShipBehaviorCatalog.isLegacySkillMovementActive(this);
    }

    public boolean canUseCompatRangedCombat() {
        if (!this.attackProfile.hasRangedAttack()
                || (this.isPassenger() && (this.getVehicle() instanceof LegacyMountEntity
                || this.getVehicle() instanceof LegacyShipMountEntity))) {
            return false;
        }

        return (this.attackProfile.light() && this.useAttackLight()
                && this.hasRangedAttackResourcesForGoal(LegacyShipAttackKind.LIGHT))
                || (this.attackProfile.heavy() && this.useAttackHeavy()
                && this.hasRangedAttackResourcesForGoal(LegacyShipAttackKind.HEAVY))
                || (this.attackProfile.airLight() && this.useAttackAirLight()
                && this.hasRangedAttackResourcesForGoal(LegacyShipAttackKind.AIR_LIGHT))
                || (this.attackProfile.airHeavy() && this.useAttackAirHeavy()
                && this.hasRangedAttackResourcesForGoal(LegacyShipAttackKind.AIR_HEAVY));
    }

    public boolean isPassengerOfLegacyShipMount() {
        return this.isPassenger() && this.getVehicle() instanceof LegacyShipMountEntity;
    }

    private boolean hasRangedAttackResourcesForGoal(LegacyShipAttackKind attackKind) {
        if (this.isHostileVariant()) {
            return true;
        }
        if (!this.hasRequiredAttackResources(attackKind)) {
            return false;
        }

        int minimumAmmo = LegacyShipBehaviorCatalog.minimumAmmoForGoal(this.getSpec(), attackKind);
        return switch (attackKind) {
            case LIGHT, AIR_LIGHT -> this.getLightAmmo() >= minimumAmmo;
            case HEAVY, AIR_HEAVY -> this.getHeavyAmmo() >= minimumAmmo;
            case MELEE -> false;
        };
    }

    public int getCompatAttackAimTime() {
        return (int) (20F * (150 - this.getShipLevel()) / 150F) + 10;
    }

    public float getCompatAttackRange() {
        return this.legacyStats.attackRange();
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

    public boolean hasActiveCommandState() {
        return this.commandedPos != null
                || this.guardEntityUuid != null
                || this.guardedBlockPos != null
                || this.routeNodePos != null;
    }

    public boolean hasGuardCommand() {
        return this.guardEntityUuid != null || this.guardedBlockPos != null;
    }

    public int getLegacyGuardType() {
        return this.legacyGuardType;
    }

    public boolean canExecuteGuardGoal() {
        return !this.level().isClientSide()
                && !this.isOrderedToSit()
                && this.hasActiveCommandState()
                && !this.canFollow()
                && !this.isPassenger()
                && this.getLegacyCraneState() < 1
                && this.getGrudge() > 0;
    }

    public boolean tryCompatCannonAttack(LivingEntity target) {
        return this.tryCompatCannonAttack(target, true, true);
    }

    public boolean tryCompatCannonAttack(LivingEntity target, boolean allowLight, boolean allowHeavy) {
        boolean attacked = false;

        if (allowLight && this.attackProfile.light() && this.useAttackLight() && this.lightAttackCooldown <= 0) {
            attacked |= this.performCompatAttack(target, LegacyShipAttackKind.LIGHT);
        }

        if (allowHeavy && this.attackProfile.heavy() && this.useAttackHeavy() && this.heavyAttackCooldown <= 0) {
            attacked |= this.performCompatAttack(target, LegacyShipAttackKind.HEAVY);
        }

        return attacked;
    }

    public boolean tryCompatAirAttack(LivingEntity target) {
        if (!this.attackProfile.hasAirAttack() || this.airAttackCooldown > 0) {
            return false;
        }

        LegacyShipAttackKind preferred = this.nextAirAttackLight ? LegacyShipAttackKind.AIR_LIGHT : LegacyShipAttackKind.AIR_HEAVY;
        LegacyShipAttackKind alternate = this.nextAirAttackLight ? LegacyShipAttackKind.AIR_HEAVY : LegacyShipAttackKind.AIR_LIGHT;

        if (this.isAttackModeEnabled(preferred)
                && this.canUseCompatAttack(preferred)
                && this.performCompatAttack(target, preferred)) {
            this.nextAirAttackLight = !this.nextAirAttackLight;
            return true;
        }

        if (this.isAttackModeEnabled(alternate)
                && this.canUseCompatAttack(alternate)
                && this.performCompatAttack(target, alternate)) {
            this.nextAirAttackLight = !this.nextAirAttackLight;
            return true;
        }

        return false;
    }

    public boolean supportsCompatAttack(LegacyShipAttackKind attackKind) {
        return this.canUseCompatAttack(attackKind);
    }

    public CompatAttackFailureReason getCompatAttackFailureReason(@Nullable LivingEntity target,
                                                                  LegacyShipAttackKind attackKind) {
        if (target == null || !this.canAttackLegacyTarget(target)) {
            return CompatAttackFailureReason.TARGET_INVALID;
        }
        if (!this.canUseCompatAttack(attackKind)) {
            return CompatAttackFailureReason.SKILL_UNAVAILABLE;
        }
        if (this.getCompatAttackCooldown(attackKind) > 0) {
            return CompatAttackFailureReason.COOLDOWN;
        }
        if (!this.isTargetInCompatRange(target, attackKind)) {
            return CompatAttackFailureReason.OUT_OF_RANGE;
        }
        return this.getAttackResourceFailureReason(attackKind);
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
        if (this.getCompatAttackFailureReason(target, attackKind) != CompatAttackFailureReason.NONE) {
            return false;
        }
        return this.performCompatAttack(target, attackKind);
    }

    public CompatAttackFailureReason getCompatBlockAttackFailureReason(@Nullable BlockPos target,
                                                                        LegacyShipAttackKind attackKind) {
        if (target == null || this.isHostileVariant()) {
            return CompatAttackFailureReason.TARGET_INVALID;
        }
        if (!LegacyShipBehaviorCatalog.supportsBlockTargetAttack(this.getSpec(), attackKind)) {
            return CompatAttackFailureReason.SKILL_UNAVAILABLE;
        }
        if (!this.canUseCompatAttack(attackKind)) {
            return CompatAttackFailureReason.SKILL_UNAVAILABLE;
        }
        if (this.getCompatAttackCooldown(attackKind) > 0) {
            return CompatAttackFailureReason.COOLDOWN;
        }
        if (!this.isBlockTargetInCompatRange(target, attackKind)) {
            return CompatAttackFailureReason.OUT_OF_RANGE;
        }
        return this.getAttackResourceFailureReason(attackKind);
    }

    public boolean isBlockTargetInCompatRange(@Nullable BlockPos target, LegacyShipAttackKind attackKind) {
        if (target == null) {
            return false;
        }

        double maxRange = attackKind == LegacyShipAttackKind.MELEE ? 3.25D : this.getCompatAttackRange();
        return this.distanceToSqr(target.getX() + 0.5D, target.getY() + 0.5D, target.getZ() + 0.5D)
                <= maxRange * maxRange;
    }

    public boolean performPlayerCompatAttack(BlockPos target, LegacyShipAttackKind attackKind) {
        if (this.getCompatBlockAttackFailureReason(target, attackKind) != CompatAttackFailureReason.NONE) {
            return false;
        }
        this.markLegacyCombat();
        LegacyShipBehaviorCatalog.CombatHookResult hookResult =
                LegacyShipBehaviorCatalog.performBlockTargetCombatHook(this, target, attackKind, true);
        return hookResult.handled();
    }

    /** Executes one attack from the legacy guard AI without sharing ranged-AI cooldowns. */
    public boolean performGuardCompatAttack(LivingEntity target, LegacyShipAttackKind attackKind) {
        if (!this.isAttackModeEnabled(attackKind)) {
            return false;
        }
        return this.performCompatAttack(target, attackKind, false);
    }

    public boolean canEngage(@Nullable LivingEntity target) {
        return this.canSelectLegacyTarget(target);
    }

    private boolean canAttackLegacyTarget(@Nullable LivingEntity target) {
        return this.canCommandOrRevengeEngage(target);
    }

    /** Keeps the legacy skill replacement search radius separate from the normal target leash. */
    boolean canEngageIgnoringHostileTargetRange(@Nullable LivingEntity target) {
        return this.canSelectLegacyTarget(target);
    }

    public boolean canCommandOrRevengeEngage(@Nullable LivingEntity target) {
        if (target == null || !target.isAlive() || target == this || this.isAlliedTo(target)) {
            return false;
        }
        if (this.level() instanceof ServerLevel serverLevel
                && WorldCombatRulesSavedData.get(serverLevel).isUnattackable(TeitokuHelper.resolveTargetClass(target))) {
            return false;
        }
        if (target instanceof Player player) {
            return !(player.getAbilities().invulnerable || player.isCreative() || player.isSpectator() || this.isOwnedBy(player));
        }
        if (!LegacyShipBehaviorCatalog.canTargetLegacyInvisibleEntity(this, target)) {
            return false;
        }
        if (target instanceof LegacyShipEntity otherShip) {
            return !(this.isHostileVariant() && otherShip.isHostileVariant());
        }
        return true;
    }

    private boolean canSelectLegacyTarget(@Nullable LivingEntity target) {
        if (target == null || !target.isAlive() || target == this) {
            return false;
        }
        if (target instanceof Player player) {
            if (player.getAbilities().invulnerable || player.isCreative() || player.isSpectator()) {
                return false;
            }
            if (this.isHostileVariant()) {
                return ShinColleConfig.mobAttackPlayer();
            }
            boolean allowed = switch (ShinColleConfig.shipAttackPlayer()) {
                case 0 -> false;
                case 1 -> this.isLegacyBannedTarget(player);
                case 2 -> !this.isAlliedTo(player);
                case 3 -> !this.isOwnedBy(player);
                default -> false;
            };
            if (allowed) {
                return true;
            }
        }
        if (this.level() instanceof ServerLevel serverLevel
                && WorldCombatRulesSavedData.get(serverLevel).isUnattackable(TeitokuHelper.resolveTargetClass(target))) {
            return false;
        }
        if (!LegacyShipBehaviorCatalog.canTargetLegacyInvisibleEntity(this, target)) {
            return false;
        }
        if (this.isHostileVariant()) {
            if (target instanceof LegacyShipEntity ship) {
                return !ship.isHostileVariant();
            }
            if (target instanceof LegacyShipRensouhouEntity summon) {
                Entity host = this.level().getEntity(summon.getHostId());
                return host != null && !this.isAlliedTo(host);
            }
            return false;
        }
        if (this.isAiOnSightChase() && !this.getSensing().hasLineOfSight(target)) {
            return false;
        }
        if (LegacyShipBehaviorCatalog.isLegacyUnderseaTarget(target)) {
            return this.isAiAntiSub() && this.isLegacyBannedTarget(target);
        }
        if (this.isAiPvpFirst() && target instanceof LegacyShipEntity && this.isLegacyBannedTarget(target)) {
            return true;
        }
        return target instanceof Monster || target instanceof Slime
                || LegacyShipBehaviorCatalog.isExplicitCustomTarget(this, target);
    }

    private boolean isLegacyBannedTarget(LivingEntity target) {
        if (target instanceof LegacyShipEntity ship && ship.isHostileVariant()) {
            return true;
        }
        if (this.level() instanceof ServerLevel serverLevel) {
            int targetOwner = target instanceof Player player ? TeitokuHelper.getPlayerUid(player)
                    : target instanceof LegacyShipEntity ship ? ship.ownerUid : 0;
            return this.ownerUid > 0 && targetOwner > 0
                    && TeitokuHelper.isBanned(serverLevel, this.ownerUid, targetOwner);
        }
        return false;
    }

    public double getHostileTargetSearchRange() {
        int attackRange = (int) this.getCompatAttackRange();
        return attackRange < 2 ? Math.max(2, this.getFollowMax() + 2) : attackRange;
    }

    public AABB getHostileTargetSearchArea() {
        double targetRange = this.getHostileTargetSearchRange();
        return this.getBoundingBox().inflate(targetRange, targetRange * 0.75D, targetRange);
    }

    public boolean canAutoTargetEnemyMob(@Nullable LivingEntity target) {
        if (!this.aiAutoTarget || this.hasExplicitMovementCommand()) {
            return false;
        }

        if (!(target instanceof Mob mob) || !target.isAlive()
                || !(target instanceof Monster || target instanceof Slime) || !this.canEngage(target)) {
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
        this.initializeHostileRuntime(naturalSpawn, (boss ? 2 : 0) + (elite ? 1 : 0));
    }

    public void initializeHostileRuntime(boolean naturalSpawn, int scaleLevel) {
        this.hostileRuntimeState.reset(naturalSpawn);
        this.entityData.set(DATA_HOSTILE_SCALE, this.isHostileVariant() ? Mth.clamp(scaleLevel, 0, 3) : 0);
        if (this.isHostileVariant()) {
            this.setHealth(this.getMaxHealth());
        }
        if (!this.isBossEncounter() && this.bossEvent != null) {
            this.bossEvent.removeAllPlayers();
            this.bossEvent = null;
        }
    }

    public boolean addRandomModernization() {
        int modernLimit = Math.max(1, ShinColleConfig.modernLimit());
        if (this.getModernizationCount() >= modernLimit * MODERN_ATTRIBUTE_COUNT) {
            return false;
        }

        int selected = this.random.nextInt(MODERN_ATTRIBUTE_COUNT);
        if (this.getModernizationSteps(selected) >= modernLimit) {
            selected = -1;
            for (int i = 0; i < MODERN_ATTRIBUTE_COUNT; i++) {
                if (this.getModernizationSteps(i) < modernLimit) {
                    selected = i;
                    break;
                }
            }
        }
        if (selected < 0) {
            return false;
        }

        this.addModernizationStep(selected);

        this.entityData.set(DATA_MODERNIZATION_TOTAL, this.getModernizationCount());
        this.refreshFromVariant(true);
        return true;
    }

    public void setModernizationSteps(int hp, int atk, int def, int spd, int mov, int hit) {
        this.modernHealthSteps = Mth.clamp(hp, 0, 100);
        this.modernAttackSteps = Mth.clamp(atk, 0, 100);
        this.modernDefenseSteps = Mth.clamp(def, 0, 100);
        this.modernAttackSpeedSteps = Mth.clamp(spd, 0, 100);
        this.modernSpeedSteps = Mth.clamp(mov, 0, 100);
        this.modernRangeSteps = Mth.clamp(hit, 0, 100);
        this.entityData.set(DATA_MODERNIZATION_TOTAL, this.getModernizationCount());
        this.refreshFromVariant(true);
    }

    public int getShipKills() {
        return this.entityData.get(DATA_SHIP_KILLS);
    }

    public void setShipKills(int kills) {
        this.entityData.set(DATA_SHIP_KILLS, Math.max(0, kills));
    }

    public void incrementShipKills() {
        this.setShipKills(this.getShipKills() + 1);
    }

    @Override
    public boolean killedEntity(ServerLevel level, LivingEntity entity) {
        this.incrementShipKills();
        return super.killedEntity(level, entity);
    }

    public int getModernizationSteps(int index) {
        return switch (index) {
            case 0 -> this.modernHealthSteps;
            case 1 -> this.modernAttackSteps;
            case 2 -> this.modernDefenseSteps;
            case 3 -> this.modernAttackSpeedSteps;
            case 4 -> this.modernSpeedSteps;
            case 5 -> this.modernRangeSteps;
            default -> MODERN_LIMIT;
        };
    }

    private void addModernizationStep(int index) {
        switch (index) {
            case 0 -> this.modernHealthSteps++;
            case 1 -> this.modernAttackSteps++;
            case 2 -> this.modernDefenseSteps++;
            case 3 -> this.modernAttackSpeedSteps++;
            case 4 -> this.modernSpeedSteps++;
            case 5 -> this.modernRangeSteps++;
            default -> {
            }
        }
    }

    public int getRescueItemCount() {
        return this.countItemInInventory(ModItems.REPAIRGODDESS.get());
    }

    public boolean storeSingleItem(ItemStack sourceStack) {
        if (sourceStack.isEmpty()) {
            return false;
        }

        for (int slot = EQUIPMENT_SLOT_COUNT; slot < this.getInventorySizePaged(); slot++) {
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

    public boolean performKaitaiKill(Player player, ItemStack hammer) {
        if ((!this.canCommanderEdit(player) && !player.hasPermissions(2)) || this.level().isClientSide()) {
            return false;
        }

        this.hurt(player.damageSources().playerAttack(player), this.getMaxHealth() * 1.1F);

        if (!player.getAbilities().instabuild) {
            hammer.hurtAndBreak(1, player, p -> p.broadcastBreakEvent(InteractionHand.MAIN_HAND));
        }

        return true;
    }

    public boolean performKaitai(Player player, ItemStack hammer) {
        if ((!this.canCommanderEdit(player) && !player.hasPermissions(2)) || this.level().isClientSide()) {
            return false;
        }

        if (!player.getAbilities().instabuild) {
            hammer.hurtAndBreak(1, player, p -> p.broadcastBreakEvent(InteractionHand.MAIN_HAND));
        }

        if (!player.getAbilities().instabuild) {
            this.dropShipInventoryContents();
            for (ItemStack drop : LegacyShipBehaviorCatalog.createLegacyKaitaiDrops(
                    this.getSpec(), this.isLegacyRaiden(), this.random)) {
                this.spawnAtLocation(drop);
            }
            this.playSound(ModSoundEvents.SHIP_KAITAI.get(), 0.8F, this.getVoicePitch());
        }
        CombatFxDispatcher.sendLegacyEmotion(this, 8);
        for (LegacyShipEntity nearby : this.level().getEntitiesOfClass(LegacyShipEntity.class, this.getBoundingBox().inflate(10.0D))) {
            if (nearby != this && nearby.isAlive()) {
                CombatFxDispatcher.sendLegacyEmotion(nearby, 6);
            }
        }
        // Legacy Kaitei drops emitted via createLegacyKaitaiDrops above.
        if (this.level() instanceof ServerLevel serverLevel && this.shipUid > 0) {
            TeitokuHelper.removeShipFromAllOnlineTeams(serverLevel.getServer(), this.shipUid);
        }
        this.resetOwnershipBoundState();
        this.discard();
        return true;
    }

    private void refreshFromVariant(boolean preserveHealth) {
        ShipEntitySpec spec = this.getSpec();
        this.updateMeleeGoal();
        if (this.wanderGoal != null && !this.level().isClientSide()) {
            int priority = spec.hostile() ? 23 : 24;
            if (priority != this.wanderPriority) {
                this.goalSelector.removeGoal(this.wanderGoal);
                this.goalSelector.addGoal(priority, this.wanderGoal);
                this.wanderPriority = priority;
            }
        }
        this.setMaxUpStep(LegacyShipBehaviorCatalog.stepHeightFor(spec));
        this.attackProfile = LegacyShipBehaviorCatalog.attackProfile(spec);
        this.equipmentBehaviorState = LegacyShipBehaviorCatalog.equipmentBehaviorFor(
                spec,
                ShipEquipmentBehaviorState.fromInventory(this.shipInventory, spec.archetype()),
                this.isMarried(),
                this.isRingEffectEnabled());
        this.maxAircraftLight = LegacyShipBehaviorCatalog.aircraftLightCapacityFor(
                spec, this.getShipLevel(), this.countAircraftEquipment());
        this.maxAircraftHeavy = LegacyShipBehaviorCatalog.aircraftHeavyCapacityFor(
                spec, this.getShipLevel(), this.countAircraftEquipment());
        if (!this.loadingSaveData && !this.level().isClientSide()) {
            if (!this.attackProfile.hasAirAttack()) {
                this.entityData.set(DATA_AIRCRAFT_LIGHT, 0);
                this.entityData.set(DATA_AIRCRAFT_HEAVY, 0);
            } else if (!this.aircraftProfileInitialized) {
                this.entityData.set(DATA_AIRCRAFT_LIGHT, this.maxAircraftLight);
                this.entityData.set(DATA_AIRCRAFT_HEAVY, this.maxAircraftHeavy);
            } else {
                this.setAircraftLight(this.getAircraftLight());
                this.setAircraftHeavy(this.getAircraftHeavy());
            }
            this.aircraftProfileInitialized = true;
        }
        int formationId = this.entityData.get(DATA_FORMATION_ID);
        int formatPos = 0;
        if (!this.level().isClientSide()) {
            formationId = this.resolveAppliedFormationId();
            this.entityData.set(DATA_FORMATION_ID, formationId);
            formatPos = formationId > 0 ? this.resolveAppliedFormationSlot() : 0;
            this.cachedFormationSlot = formatPos;
        }
        this.legacyStats = LegacyShipStats.create(
                spec.legacyClassId(),
                spec.archetype(),
                spec.hostile(),
                this.getShipLevel(),
                this.getMorale(),
                this.modernHealthSteps,
                this.modernAttackSteps,
                this.modernDefenseSteps,
                this.modernAttackSpeedSteps,
                this.modernSpeedSteps,
                this.modernRangeSteps,
                this.isMarried(),
                formationId,
                formatPos,
                this.equipmentProfile,
                this.getActiveEffects(),
                this.isLegacyNight(),
                this.getLegacyHostileScaleLevel());
        this.cachedLegacyNight = this.isLegacyNight();
        this.cachedEffectSignature = this.computeActiveEffectSignature();

        this.refreshDimensions();
        this.applyBaseValue(Attributes.MAX_HEALTH, this.legacyStats.get(LegacyShipStatTables.Attr.HP));
        this.applyBaseValue(Attributes.MOVEMENT_SPEED, this.resolveEffectiveMoveSpeed(formationId));
        this.applyBaseValue(Attributes.ATTACK_DAMAGE, this.legacyStats.meleeDamage());
        this.applyBaseValue(Attributes.FOLLOW_RANGE, Math.max(28D,
                16D + this.legacyStats.attackRange() + this.equipmentBehaviorState.detectionRangeBonus()));
        this.applyBaseValue(Attributes.KNOCKBACK_RESISTANCE, this.legacyStats.knockbackResistance());

        float maxHealth = this.getMaxHealth();
        float currentHealth = preserveHealth ? this.clampPreservedHealth(maxHealth) : maxHealth;
        this.setHealth(currentHealth);
    }

    /** Matches 1.12 World.isDaytime for the normal 24000-tick day cycle. */
    private boolean isLegacyNight() {
        long timeOfDay = this.level().getDayTime() % 24000L;
        return timeOfDay >= 12000L;
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
        this.equipmentProfile = ShipEquipmentProfile.fromInventory(this.shipInventory, this.getSpec());
        this.equipmentBehaviorState = ShipEquipmentBehaviorState.fromInventory(this.shipInventory, this.getSpec().archetype());
        this.refreshFromVariant(preserveHealth);
    }

    private int countAircraftEquipment() {
        int count = 0;
        for (int slot = 0; slot < Math.min(EQUIPMENT_SLOT_COUNT, this.shipInventory.getContainerSize()); slot++) {
            ItemStack stack = this.shipInventory.getItem(slot);
            if (stack.getItem() instanceof LegacyEquipmentItem equipmentItem
                    && equipmentItem.family() == LegacyEquipmentFamily.AIRPLANE) {
                count++;
            }
        }
        return count;
    }

    private void spawnOwnerLockedRecoveryEgg(ItemStack recoveredEgg) {
        ItemEntity itemEntity = ModEntityTypes.RECOVERED_SHIP_ITEM.get().create(this.level());
        if (itemEntity == null) return;
        itemEntity.setPos(this.getX(), this.getY() + 0.5D, this.getZ());
        itemEntity.setItem(recoveredEgg);
        LegacyEntityItemMarker.mark(itemEntity);
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

    private boolean isAttackModeEnabled(LegacyShipAttackKind attackKind) {
        return switch (attackKind) {
            case MELEE -> this.useAttackMelee();
            case LIGHT -> this.useAttackLight();
            case HEAVY -> this.useAttackHeavy();
            case AIR_LIGHT -> this.useAttackAirLight();
            case AIR_HEAVY -> this.useAttackAirHeavy();
        };
    }

    private CompatAttackFailureReason getAttackResourceFailureReason(LegacyShipAttackKind attackKind) {
        if (this.isHostileVariant() || attackKind == LegacyShipAttackKind.MELEE) {
            return CompatAttackFailureReason.NONE;
        }

        if (this.hasNoFuel()) {
            return CompatAttackFailureReason.NO_GRUDGE;
        }
        if ((attackKind == LegacyShipAttackKind.AIR_LIGHT || attackKind == LegacyShipAttackKind.AIR_HEAVY)
                && !this.hasAircraftFor(attackKind)) {
            return CompatAttackFailureReason.NO_AIRCRAFT;
        }

        int ammoCost = this.getRequiredAttackAmmo(attackKind);
        return switch (attackKind) {
            case LIGHT, AIR_LIGHT -> this.getLightAmmo() >= ammoCost
                    ? CompatAttackFailureReason.NONE
                    : CompatAttackFailureReason.NO_LIGHT_AMMO;
            case HEAVY, AIR_HEAVY -> this.getHeavyAmmo() >= ammoCost
                    ? CompatAttackFailureReason.NONE
                    : CompatAttackFailureReason.NO_HEAVY_AMMO;
            case MELEE -> CompatAttackFailureReason.NONE;
        };
    }

    public boolean hasRequiredAttackResources(LegacyShipAttackKind attackKind) {
        if (this.isHostileVariant() || attackKind == LegacyShipAttackKind.MELEE) {
            return true;
        }

        if (this.hasNoFuel()) {
            return false;
        }
        if ((attackKind == LegacyShipAttackKind.AIR_LIGHT || attackKind == LegacyShipAttackKind.AIR_HEAVY)
                && !this.hasAircraftFor(attackKind)) {
            return false;
        }

        int ammoCost = this.getRequiredAttackAmmo(attackKind);
        return switch (attackKind) {
            case LIGHT, AIR_LIGHT -> this.getLightAmmo() >= ammoCost;
            case HEAVY, AIR_HEAVY -> this.getHeavyAmmo() >= ammoCost;
            case MELEE -> true;
        };
    }

    private boolean consumeAttackResources(LegacyShipAttackKind attackKind) {
        if (this.isHostileVariant() || attackKind == LegacyShipAttackKind.MELEE) {
            return true;
        }
        if (!this.hasRequiredAttackResources(attackKind)) {
            return false;
        }

        int ammoCost = this.getAttackAmmoCost(attackKind);
        switch (attackKind) {
            case LIGHT, AIR_LIGHT -> this.addLightAmmo(-ammoCost);
            case HEAVY, AIR_HEAVY -> this.addHeavyAmmo(-ammoCost);
            case MELEE -> {
            }
        }
        this.consumeLegacyGrudge(this.getAttackGrudgeCost(attackKind));
        this.addMorale(-LegacyShipBehaviorCatalog.moraleCostFor(attackKind));
        return true;
    }

    boolean consumeCatalogAttackResources(LegacyShipAttackKind attackKind,
                                          int moraleCost, int experience) {
        return consumeCatalogAttackResources(attackKind, 1, this.getAttackGrudgeCost(attackKind),
                moraleCost, experience);
    }

    boolean consumeCatalogAttackResources(LegacyShipAttackKind attackKind,
                                          int ammoMultiplier, int grudgeCost,
                                          int moraleCost, int experience) {
        if (this.isHostileVariant()) {
            return true;
        }
        int ammoCost = this.getAttackAmmoCost(attackKind) * Math.max(1, ammoMultiplier);
        if (this.hasNoFuel()
                || this.getAttackAmmo(attackKind) < ammoCost) {
            return false;
        }

        this.addAttackAmmo(attackKind, -ammoCost);
        this.consumeLegacyGrudge(grudgeCost);
        this.addMorale(-moraleCost);
        this.addShipExperience(experience);
        return true;
    }

    boolean consumeShimakazeLightAttackResources() {
        return this.consumeCatalogAttackResources(LegacyShipAttackKind.LIGHT, 4, 16, 1, 8);
    }

    boolean consumeShimakazeHeavyAttackResources() {
        return this.consumeCatalogAttackResources(LegacyShipAttackKind.HEAVY, 1, 8, 2, 5);
    }

    private int getAttackAmmo(LegacyShipAttackKind attackKind) {
        return switch (attackKind) {
            case LIGHT, AIR_LIGHT -> this.getLightAmmo();
            case HEAVY, AIR_HEAVY -> this.getHeavyAmmo();
            case MELEE -> Integer.MAX_VALUE;
        };
    }

    private void addAttackAmmo(LegacyShipAttackKind attackKind, int amount) {
        switch (attackKind) {
            case LIGHT, AIR_LIGHT -> this.addLightAmmo(amount);
            case HEAVY, AIR_HEAVY -> this.addHeavyAmmo(amount);
            case MELEE -> {
            }
        }
    }

    private int getAttackGrudgeCost(LegacyShipAttackKind attackKind) {
        if (attackKind == LegacyShipAttackKind.LIGHT
                && !this.isHostileVariant()
                && this.isLegacyServantVariant()) {
            return 16;
        }

        return switch (attackKind) {
            case LIGHT -> 4;
            case HEAVY -> 8;
            case AIR_LIGHT -> 6;
            case AIR_HEAVY -> 12;
            case MELEE -> 0;
        };
    }

    private int getAttackAmmoCost(LegacyShipAttackKind attackKind) {
        return LegacyShipBehaviorCatalog.attackAmmoCostFor(this.getSpec(), attackKind);
    }

    private int getRequiredAttackAmmo(LegacyShipAttackKind attackKind) {
        int multiplier = attackKind == LegacyShipAttackKind.LIGHT && this.isLegacyServantVariant() ? 4 : 1;
        return this.getAttackAmmoCost(attackKind) * multiplier;
    }

    public boolean isShimakazeVariant() {
        return this.getShipClassId() == 36 || this.getShipClassId() == 2036;
    }

    public boolean isBattleshipTaVariant() {
        return this.getShipClassId() == 14;
    }

    public boolean isLegacyServantVariant() {
        return this.isShimakazeVariant() || this.isBattleshipTaVariant();
    }

    private boolean performCompatAttack(LivingEntity target, LegacyShipAttackKind attackKind) {
        return this.performCompatAttack(target, attackKind, true);
    }

    private boolean performCompatAttack(LivingEntity target, LegacyShipAttackKind attackKind,
                                        boolean respectEntityCooldown) {
        if (!this.canAttackLegacyTarget(target) || !this.canUseCompatAttack(attackKind)
                || !this.hasRequiredAttackResources(attackKind)) {
            return false;
        }

        if (attackKind == LegacyShipAttackKind.LIGHT
                && this.isLegacyServantVariant()
                && this.getShimakazeRensouhouCount() <= 0) {
            return false;
        }

        this.markLegacyCombat();
        this.emitLegacyModelAttack(attackKind);

        LegacyShipBehaviorCatalog.CombatHookResult hookResult =
                LegacyShipBehaviorCatalog.performCombatHook(
                        this, target, attackKind, respectEntityCooldown);
        if (hookResult.handled()) {
            if (!hookResult.resourcesConsumed()) {
                this.consumeAttackResources(attackKind);
            }
            return true;
        }

        List<LegacyShipBehaviorCatalog.LegacyCannonAttackFeedback> cannonAttackFeedbacks =
                LegacyShipBehaviorCatalog.cannonAttackFeedbacksFor(
                        this.getSpec(), attackKind, this.getLegacyHostileScaleLevel());
        if (!cannonAttackFeedbacks.isEmpty()) {
            // The legacy packet carried the body yaw through the entity-bound particle handler.
            int encodedYaw = (int) (this.yBodyRot * 1000.0F);
            for (LegacyShipBehaviorCatalog.LegacyCannonAttackFeedback cannonAttackFeedback
                    : cannonAttackFeedbacks) {
                CombatFxDispatcher.sendParticle(this, GameplayParticleType.LEGACY_CANNON,
                        this.getX(), this.getY(), this.getZ(),
                        cannonAttackFeedback.width(), cannonAttackFeedback.length(),
                        cannonAttackFeedback.height(), encodedYaw);
            }
        }

        LegacyShipBehaviorCatalog.LegacyLightAttackParticleFeedback lightAttackParticleFeedback =
                LegacyShipBehaviorCatalog.lightAttackParticleFeedbackFor(this.getSpec(), attackKind);
        if (lightAttackParticleFeedback.isPresent()) {
            this.emitLegacyLightAttackFx(target, lightAttackParticleFeedback);
        }

        LegacyShipBehaviorCatalog.LegacyLightAttackFeedback lightAttackFeedback =
                LegacyShipBehaviorCatalog.lightAttackFeedbackFor(this.getSpec(), attackKind);
        if (lightAttackFeedback.isPresent()) {
            if (!lightAttackParticleFeedback.isPresent()) {
                this.emitLegacyLightAttackFx(target, lightAttackFeedback);
            }
            this.playLegacyLightAttackSound(lightAttackFeedback);
        }

        LegacyShipProjectileProfile projectileProfile = this.attackProfile.projectileProfile(attackKind);
        boolean projectileAttack = projectileProfile.isPresent();
        if (attackKind.justLaunch() && !projectileProfile.isPresent()) {
            return false;
        }
        if (attackKind != LegacyShipAttackKind.MELEE && this.equipmentBehaviorState.flareLevel() > 0) {
            this.emitIlluminationFx(target, true);
        }

        int delay = LegacyShipCombatHelper.attackDelay(this, attackKind);

        if (attackKind == LegacyShipAttackKind.AIR_LIGHT || attackKind == LegacyShipAttackKind.AIR_HEAVY) {
            boolean launched = this.launchCompatAircraft(target, attackKind, projectileProfile);
            if (launched) {
                if (respectEntityCooldown) {
                    this.airAttackCooldown = delay;
                }
                this.setLastHurtMob(target);
                this.playCompatAircraftLaunchSound();
                if (this.consumeAttackResources(attackKind)
                        && LegacyShipBehaviorCatalog.managesAircraftInventory(this.getSpec())) {
                    if (attackKind == LegacyShipAttackKind.AIR_LIGHT) {
                        this.setAircraftLight(this.getAircraftLight() - 1);
                    } else {
                        this.setAircraftHeavy(this.getAircraftHeavy() - 1);
                    }
                }
            }
            return launched;
        }

        LegacyShipCombatHelper.AttackRoll roll = LegacyShipCombatHelper.rollAttack(this, target, attackKind);
        if (roll.miss() && !attackKind.justLaunch() && !projectileAttack) {
            this.setLastHurtMob(target);
            this.emitAttackFx(target, attackKind, roll, true);
            if (respectEntityCooldown) {
                switch (attackKind) {
                    case LIGHT -> this.lightAttackCooldown = delay;
                    case HEAVY -> this.heavyAttackCooldown = delay;
                    case MELEE -> this.meleeAttackCooldown = delay;
                    default -> {
                    }
                }
            }
            this.consumeAttackResources(attackKind);
            return true;
        }

        if (attackKind.justLaunch() || projectileAttack) {
            boolean launched = this.launchCompatProjectile(target, attackKind, roll);
            if (launched) {
                if (respectEntityCooldown) {
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
                } else if (attackKind == LegacyShipAttackKind.HEAVY) {
                    this.playShipCombatSound(ModSoundEvents.SHIP_FIREHEAVY.get(), 0.75F, 0.85F + this.random.nextFloat() * 0.3F);
                } else if (attackKind == LegacyShipAttackKind.LIGHT) {
                    this.playShipCombatSound(ModSoundEvents.SHIP_FIRELIGHT.get(), 0.65F, 0.9F + this.random.nextFloat() * 0.2F);
                }
                this.setLastHurtMob(target);
                this.consumeAttackResources(attackKind);
            }
            return launched;
        }

        boolean attacked = target.hurt(this.damageSources().mobAttack(this), roll.damage());
        this.setLastHurtMob(target);
        if (attacked) {
            LegacyShipBehaviorCatalog.applyAttackEffect(this, target);
            if (attackKind == LegacyShipAttackKind.MELEE) {
                this.playShipCombatSound(ModSoundEvents.SHIP_HITMETAL.get(), 0.55F, 0.9F + this.random.nextFloat() * 0.2F);
            } else if (attackKind == LegacyShipAttackKind.LIGHT
                    && !lightAttackFeedback.isPresent()) {
                this.playShipCombatSound(ModSoundEvents.SHIP_FIRELIGHT.get(), 0.6F, 0.9F + this.random.nextFloat() * 0.2F);
            } else if (attackKind == LegacyShipAttackKind.HEAVY) {
                this.playShipCombatSound(ModSoundEvents.SHIP_FIREHEAVY.get(), 0.7F, 0.85F + this.random.nextFloat() * 0.3F);
            }
            LegacyShipBehaviorCatalog.applyLegacyPostAttackMotion(this, attackKind, target, roll.damage());
        }
        this.consumeAttackResources(attackKind);
        this.emitAttackFx(target, attackKind, roll, attacked);
        if (respectEntityCooldown) {
            switch (attackKind) {
                case LIGHT -> this.lightAttackCooldown = delay;
                case HEAVY -> this.heavyAttackCooldown = delay;
                case AIR_LIGHT, AIR_HEAVY -> this.airAttackCooldown = delay;
                case MELEE -> this.meleeAttackCooldown = delay;
            }
        }

        return true;
    }

    private void emitLegacyLightAttackFx(LivingEntity target,
                                         LegacyShipBehaviorCatalog.LegacyLightAttackFeedback feedback) {
        double sourceY = this.getY() + feedback.sourceHeight(this.isPassenger());
        Vec3 direction = target.position().add(0.0D, target.getBbHeight() * 0.5D, 0.0D)
                .subtract(this.getX(), sourceY, this.getZ());
        double distance = direction.length();
        if (distance <= 1.0E-4D) {
            return;
        }

        direction = direction.scale(1.0D / distance);
        CombatFxDispatcher.sendParticle(this, GameplayParticleType.BEAM_TRAIL,
                this.getX(), sourceY, this.getZ(), direction.x, direction.y, direction.z);
    }

    private void emitLegacyLightAttackFx(LivingEntity target,
                                         LegacyShipBehaviorCatalog.LegacyLightAttackParticleFeedback feedback) {
        Vec3 origin = this.position();
        Vec3 direction = target.position().subtract(origin);
        double distance = direction.length();
        if (distance <= 1.0E-4D) {
            return;
        }

        direction = direction.scale(1.0D / distance);
        double sourceY = this.getY() + feedback.sourceHeight(this.isPassenger());
        switch (feedback.particleType()) {
            case LEGACY_DOUBLE_CANNON -> CombatFxDispatcher.sendParticle(this,
                    GameplayParticleType.LEGACY_DOUBLE_CANNON,
                    this.getX(), sourceY, this.getZ(),
                    direction.x, feedback.parameter(), direction.z);
            case LEGACY_NORTHERN_CANNON -> CombatFxDispatcher.sendParticle(this,
                    GameplayParticleType.LEGACY_NORTHERN_CANNON,
                    this.getX(), sourceY, this.getZ(),
                    direction.x, direction.y, direction.z);
            case LEGACY_RE_LASER -> CombatFxDispatcher.sendParticle(this,
                    GameplayParticleType.LEGACY_RE_LASER,
                    this.getX(), sourceY, this.getZ(),
                    direction.x, direction.y, direction.z,
                    Mth.clamp((int) (distance * 1000.0D), 1, 32767));
            default -> {
            }
        }
    }

    private void playLegacyLightAttackSound(LegacyShipBehaviorCatalog.LegacyLightAttackFeedback feedback) {
        this.playShipCombatSound(ModSoundEvents.SHIP_LASER.get(), feedback.soundVolume(),
                this.getVoicePitch() * 0.85F);
        this.playLegacyAttackVoice();
    }

    public boolean performCatalogDirectAttack(LivingEntity target,
                                        LegacyShipCombatHelper.AttackRoll roll,
                                        boolean knockback, boolean applyAttackEffect,
                                        LegacyShipProjectileVisual projectileVisual,
                                        GameplayParticleType hitParticleType) {
        return performCatalogDirectAttack(target, roll, knockback, applyAttackEffect,
                projectileVisual, hitParticleType, false);
    }

    /** Allows legacy area attacks to resolve same-owner zero-damage collisions. */
    public boolean performLegacyAreaDirectAttack(LivingEntity target,
                                        LegacyShipCombatHelper.AttackRoll roll,
                                        boolean knockback, boolean applyAttackEffect,
                                        LegacyShipProjectileVisual projectileVisual,
                                        GameplayParticleType hitParticleType) {
        return performCatalogDirectAttack(target, roll, knockback, applyAttackEffect,
                projectileVisual, hitParticleType, true);
    }

    private boolean performCatalogDirectAttack(LivingEntity target,
                                        LegacyShipCombatHelper.AttackRoll roll,
                                        boolean knockback, boolean applyAttackEffect,
                                        LegacyShipProjectileVisual projectileVisual,
                                        GameplayParticleType hitParticleType,
                                        boolean legacyAreaAttack) {
        if (!legacyAreaAttack && !this.canAttackLegacyTarget(target)) {
            return false;
        }
        this.markLegacyCombat();
        if (roll.miss()) {
            this.emitAttackFx(target, LegacyShipAttackKind.HEAVY, roll, false,
                    projectileVisual, hitParticleType);
            return false;
        }

        boolean attacked = target.hurt(this.damageSources().mobAttack(this),
                Math.max(0.0F, roll.damage()));
        if (attacked) {
            this.setLastHurtMob(target);
            if (applyAttackEffect) {
                LegacyShipBehaviorCatalog.applyAttackEffect(this, target);
            }
            if (knockback) {
                target.knockback(0.32D, this.getX() - target.getX(), this.getZ() - target.getZ());
            }
            this.playShipCombatSound(ModSoundEvents.SHIP_FIREHEAVY.get(),
                    0.7F, 0.85F + this.random.nextFloat() * 0.3F);
        }
        this.emitAttackFx(target, LegacyShipAttackKind.HEAVY, roll, attacked,
                projectileVisual, hitParticleType);
        return attacked;
    }

    void setCatalogAttackCooldown(LegacyShipAttackKind attackKind, int delay) {
        int clampedDelay = Math.max(0, delay);
        switch (attackKind) {
            case LIGHT -> this.lightAttackCooldown = clampedDelay;
            case HEAVY -> this.heavyAttackCooldown = clampedDelay;
            case AIR_LIGHT, AIR_HEAVY -> this.airAttackCooldown = clampedDelay;
            case MELEE -> this.meleeAttackCooldown = clampedDelay;
        }
    }

    private void playCompatAircraftLaunchSound() {
        if (LegacyShipBehaviorCatalog.usesLegacyCarrierAircraftBowSound(this.getSpec())) {
            float pitch = 1.0F / (this.random.nextFloat() * 0.4F + 1.2F) + 0.5F;
            this.playShipCombatSound(SoundEvents.ARROW_SHOOT, 0.9F, pitch);
            if (this.getShipClassId() == 48) {
                this.playLegacyAttackVoice();
            }
            return;
        }

        this.playShipCombatSound(ModSoundEvents.SHIP_AIRCRAFT.get(),
                0.6F, 0.9F + this.random.nextFloat() * 0.2F);
    }

    private void playShipCombatSound(SoundEvent sound, float volume, float pitch) {
        this.level().playSound(null, this.blockPosition(), sound, this.getSoundSource(), volume, pitch);
    }

    /** Plays the legacy 20% attack voice used by special summon and barrage hooks. */
    public void playLegacyAttackVoice() {
        if (LegacyShipBehaviorCatalog.shouldPlayLegacyAttackVoice(this.random.nextInt(10))) {
            this.playShipCombatSound(ShinColleSoundHelper.getShipVoice(
                    ShipSoundType.HIT, this.getShipClassId()),
                    this.getSoundVolume(), this.getVoicePitch());
        }
    }

    /** Restores EntityBattleshipRu's phase-entry metal charge sound and optional voice. */
    public void playLegacyBattleshipRuChargeSound() {
        this.playShipCombatSound(ModSoundEvents.SHIP_HITMETAL.get(), 0.7F, 1.0F);
        this.playLegacyAttackVoice();
    }

    /** Restores EntityBattleshipRu's per-missile heavy cannon sound and optional voice. */
    public void playLegacyBattleshipRuBarrageShotSound() {
        this.playShipCombatSound(ModSoundEvents.SHIP_FIREHEAVY.get(),
                0.7F, this.getVoicePitch() * 0.85F);
        this.playLegacyAttackVoice();
    }

    public void playLegacyRePushSound() {
        this.playShipCombatSound(ShinColleSoundHelper.getShipVoice(
                ShipSoundType.KNOCKBACK, this.getShipClassId()),
                this.getSoundVolume(), this.getVoicePitch());
    }

    public static ShipSoundType timekeepingSoundTypeForHour(int hour) {
        return TIMEKEEPING_SOUNDS[Math.floorMod(hour, TIMEKEEPING_SOUNDS.length)];
    }

    private void tickTimeKeeper() {
        if (!this.isAlive() || !this.isTimeKeeper() || !ShinColleConfig.timeKeeping()) {
            return;
        }

        long worldTime = this.level().getDayTime();
        if (worldTime % 1000L != 0L) {
            return;
        }

        int hour = (int) ((worldTime / 1000L) % TIMEKEEPING_SOUNDS.length);
        SoundEvent sound = ShinColleSoundHelper.getShipVoice(
                timekeepingSoundTypeForHour(hour), this.getShipClassId());
        this.playShipCombatSound(sound, this.getSoundVolume() * ShinColleConfig.volumeTimekeep(),
                (this.random.nextFloat() - this.random.nextFloat()) * 0.1F + 1.0F);
    }

    public void playLegacyItemPickupVoice() {
        if (this.level().isClientSide() || this.shipItemVoiceCooldown > 0 || this.random.nextInt(2) != 0) {
            return;
        }

        this.shipItemVoiceCooldown = 40 + this.random.nextInt(10);
        this.playShipCombatSound(ShinColleSoundHelper.getShipVoice(ShipSoundType.PICKITEM, this.getShipClassId()),
                1.0F, 1.0F);
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
            GameplayParticleType resolvedHitParticle = LegacyShipBehaviorCatalog.lightAttackTargetParticleFor(
                    this.getSpec(), attackKind, hitParticleType);
            CombatFxDispatcher.sendParticle(target, resolvedHitParticle,
                    target.getX(),
                    resolvedHitParticle == GameplayParticleType.LEGACY_NORTHERN_HIT
                            ? target.getY()
                            : target.getY() + target.getBbHeight() * 0.5D,
                    target.getZ());
        }
    }

    public boolean performCompatAircraftStrike(LivingEntity target, LegacyShipAttackKind attackKind,
                                               LegacyShipProjectileVisual projectileVisual, GameplayParticleType hitParticleType,
                                               boolean flarePayload, boolean searchlightPayload) {
        return performCompatAircraftStrike(null, target, attackKind, projectileVisual, hitParticleType,
                flarePayload, searchlightPayload);
    }

    public boolean performCompatAircraftStrike(@Nullable Entity attackSource, LivingEntity target,
                                               LegacyShipAttackKind attackKind,
                                               LegacyShipProjectileVisual projectileVisual, GameplayParticleType hitParticleType,
                                               boolean flarePayload, boolean searchlightPayload) {
        if (!this.canAttackLegacyTarget(target) || (attackKind != LegacyShipAttackKind.AIR_LIGHT && attackKind != LegacyShipAttackKind.AIR_HEAVY)) {
            return false;
        }

        this.markLegacyCombat();

        if (attackKind == LegacyShipAttackKind.AIR_HEAVY) {
            Entity launchSource = attackSource == null ? this : attackSource;
            boolean missed = LegacyShipCombatHelper.rollMissAtDistance(this,
                    LegacyShipAttackKind.HEAVY, launchSource.distanceTo(target));
            LegacyShipProjectileProfile missileProfile = new LegacyShipProjectileProfile(
                    LegacyShipProjectileVisual.MISSILE, LegacyShipProjectileMoveType.DIRECT,
                    0.5D, 0F, 0.2F, 160, 0F, 0F,
                    GameplayParticleType.LAUNCH_SMOKE, GameplayParticleType.MISSILE_TRAIL,
                    GameplayParticleType.MISSILE_IMPACT);
            Vec3 launchOffset = launchSource.position().subtract(this.position()).add(0D, -0.8D, 0D);
            LegacyShipProjectileEntity missile = LegacyShipProjectileEntity.create(this.level(), this,
                    target, attackKind, this.legacyStats.attackAirHeavy(), missed, missileProfile,
                    null, 0.15D, true, 3.5D, launchOffset);
            this.level().addFreshEntity(missile);
            this.setLastHurtMob(target);
            return true;
        }

        LegacyShipCombatHelper.AttackRoll roll = LegacyShipCombatHelper.rollAttack(this, target, attackKind);
        if (roll.miss()) {
            this.setLastHurtMob(target);
            this.emitAttackFx(target, attackKind, roll, false, projectileVisual, hitParticleType);
            return true;
        }

        DamageSource damageSource = attackSource == null
                ? this.damageSources().mobAttack(this)
                : this.damageSources().thrown(attackSource, this);
        boolean attacked = target.hurt(damageSource, roll.damage());
        if (attacked) {
            this.setLastHurtMob(target);
            LegacyShipBehaviorCatalog.applyAttackEffect(this, target);
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

    private void emitLegacyModelAttack(LegacyShipAttackKind attackKind) {
        int particle = LegacyShipBehaviorCatalog.modelAttackAnimationParticleFor(
                this.getSpec(), attackKind, this.getDisplayPhase());
        if (particle >= 0) CombatFxDispatcher.sendLegacyAttackAnimation(this, particle);
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
        this.entityData.set(DATA_MODEL_SHIP_UID, this.shipUid);
    }

    private void cacheShipState(boolean dead) {
        this.cacheShipState(dead, !dead);
    }

    private void cacheShipState(boolean dead, boolean online) {
        this.ensureShipUid();
        TeitokuHelper.refreshShipCache(this, dead, online);
    }

    private void cleanupShipTeamSlotsIfNeeded() {
        if (this.shipUid <= 0 || !(this.level() instanceof ServerLevel serverLevel)) {
            return;
        }
        TeitokuHelper.removeShipFromAllOnlineTeams(serverLevel.getServer(), this.shipUid);
    }

    private int readShipExperience(CompoundTag tag) {
        if (tag.contains(SHIP_EXPERIENCE_TAG, Tag.TAG_INT)) {
            return tag.getInt(SHIP_EXPERIENCE_TAG);
        }
        if (tag.contains(LEGACY_EXPERIENCE_TAG, Tag.TAG_INT)) {
            return tag.getInt(LEGACY_EXPERIENCE_TAG);
        }
        CompoundTag minor = getLegacyMinorTag(tag);
        if (minor == null) {
            return 0;
        }

        return minor.contains(LEGACY_EXPERIENCE_TAG, Tag.TAG_INT)
                ? minor.getInt(LEGACY_EXPERIENCE_TAG)
                : 0;
    }

    private void tickHostileRuntime() {
        if (!(this.level() instanceof ServerLevel serverLevel) || !this.isHostileVariant()) {
            return;
        }

        this.clearInvalidHostileTarget();
        if (this.getTarget() == null) {
            LivingEntity target = LegacyShipBehaviorCatalog.findLegacyRangeTarget(this);
            if (target != null) {
                this.setTarget(target);
            }
        }

        if (this.hostileRuntimeState.isNaturalSpawn()) {
            int limit = this.getLegacyHostileScaleLevel() > 1
                    ? ShinColleConfig.despawnBoss()
                    : ShinColleConfig.despawnMinion();
            if (limit > -1 && this.tickCount > limit) {
                boolean playerWithin128 = serverLevel.getNearestPlayer(this, 128.0D) != null;
                if (!playerWithin128) {
                    this.discard();
                    return;
                }
                boolean playerWithin32 = serverLevel.getNearestPlayer(this, 32.0D) != null;
                if (playerWithin32) {
                    this.hostileRuntimeState.setDespawnTicks(0);
                } else {
                    int despawnTicks = this.hostileRuntimeState.getDespawnTicks() + 1;
                    this.hostileRuntimeState.setDespawnTicks(despawnTicks);
                    boolean playerWithin96 = serverLevel.getNearestPlayer(this, 96.0D) != null;
                    if ((!playerWithin96 && despawnTicks > 900)
                            || (despawnTicks > 600 && this.random.nextInt(800) == 0)) {
                        this.discard();
                        return;
                    }
                }
            }
        }

        if (this.isBossEncounter()) {
            this.ensureBossEvent();
            if (this.bossEvent != null) {
                this.bossEvent.setName(this.getName());
                this.bossEvent.setProgress(Mth.clamp(this.getHealth() / this.getMaxHealth(), 0.0F, 1.0F));
            }
        }
    }

    /**
     * Mirrors EntityAIShipRangeTarget.shouldContinueExecuting before modern
     * goal navigation gets a chance to move toward a stale hostile target.
     */
    private boolean clearInvalidHostileTarget() {
        if (!this.isHostileVariant()) {
            return false;
        }

        LivingEntity target = this.getTarget();
        double range = this.getHostileTargetSearchRange();
        if (target == null || (target.isAlive() && this.distanceToSqr(target) <= range * range && this.canEngage(target))) {
            return false;
        }

        this.setTarget(null);
        this.getShipNavigate().clearPathEntity();
        return true;
    }

    private void ensureBossEvent() {
        if (!this.isBossEncounter() || this.level().isClientSide()) {
            return;
        }

        if (this.bossEvent == null) {
            BossEvent.BossBarColor color = LegacyShipBehaviorCatalog.hostileBossBarColorFor(this.getSpec());
            this.bossEvent = new ServerBossEvent(this.getName(), color, BossEvent.BossBarOverlay.NOTCHED_10);
            this.bossEvent.setDarkenScreen(false);
        }
    }

    private void dropHostileEgg() {
        if (!this.canDropItem) {
            return;
        }
        this.canDropItem = false;
        boolean dropEgg = switch (this.getLegacyHostileScaleLevel()) {
            case 0 -> this.random.nextInt(5) == 0;
            case 1 -> this.random.nextInt(3) == 0;
            case 2 -> this.random.nextInt(10) > 0;
            default -> true;
        };
        if (dropEgg) {
            this.spawnAtLocation(this.resolveHostileEggDrop());
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
        if (this.isBossEncounter()) {
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
        if (this.isPassengerOfLegacyShipMount()) {
            this.getShipNavigate().clearPathEntity();
            return;
        }
        if (this.isOrderedToSit() || !(this.level() instanceof ServerLevel serverLevel)) {
            return;
        }

        if (this.canFollow() || this.isPassenger() || this.isLeashed()) {
            return;
        }

        if (this.routeNodePos != null) {
            String routeDimension = this.guardedBlockPos == null ? this.commandDimension : this.guardedBlockDimension;
            if (this.routeNodePos.getY() <= 0 || !this.dimensionMatches(routeDimension, serverLevel)) {
                this.clearCommandState();
                this.setTarget(null);
                this.getShipNavigate().clearPathEntity();
                return;
            }

            BlockEntity blockEntity = serverLevel.getBlockEntity(this.routeNodePos);
            boolean isCrane = blockEntity instanceof CraneBlockEntity;
            if (blockEntity instanceof WaypointBlockEntity
                    && this.getLegacyFormationId() > 0 && this.getLegacyFormationSlot() > 0) {
                return;
            }
            if (!isCrane && (this.tickCount & 15) != 0) {
                return;
            }
            double distSq = this.distanceToSqr(
                    this.routeNodePos.getX() + 0.5D,
                    this.routeNodePos.getY(),
                    this.routeNodePos.getZ() + 0.5D);
            if (isCrane && distSq >= 25.0D && this.getGrudge() > 0 && (this.tickCount & 15) == 0) {
                this.getShipNavigate().tryMoveToXYZ(
                        this.routeNodePos.getX() + 0.5D,
                        this.routeNodePos.getY() - 2.0D,
                        this.routeNodePos.getZ() + 0.5D,
                        1.0D);
            }

            if ((isCrane && distSq < 25.0D) || (!isCrane && distSq < 9.0D)) {
                if (blockEntity instanceof WaypointBlockEntity waypoint) {
                    if (this.handleWaypointRouteNode(serverLevel, waypoint)) {
                        return;
                    }
                    this.advanceAfterRouteNode(waypoint);
                } else if (blockEntity instanceof CraneBlockEntity) {
                    if (this.craneState == 0) {
                        this.craneState = 1;
                        this.craneTime = 20;
                        for (Entity passenger : this.getPassengers()) {
                            if (passenger instanceof LegacyShipEntity rider) {
                                rider.craneState = 1;
                                rider.stopRiding();
                            }
                        }
                    }
                    return;
                } else {
                    this.routeNodePos = null;
                }
            } else if (blockEntity instanceof WaypointBlockEntity && (this.tickCount & 127) == 0
                    && this.getGrudge() > 0) {
                this.getShipNavigate().tryMoveToXYZ(this.routeNodePos.getX() + 0.5D, this.routeNodePos.getY(),
                        this.routeNodePos.getZ() + 0.5D, 1.0D);
            }
            return;
        }
    }

    private void advanceRouteNode(@Nullable BlockPos nextWaypoint) {
        this.craneState = 0;
        String dimension = this.guardedBlockPos == null ? this.commandDimension : this.guardedBlockDimension;
        this.routeNodePos = nextWaypoint == null ? null : nextWaypoint.immutable();
        this.commandedPos = this.routeNodePos;
        this.guardedBlockPos = null;
        this.guardedBlockDimension = "";
        this.commandDimension = dimension;
        this.legacyGuardType = nextWaypoint == null ? 0 : LEGACY_GUARD_BLOCK_TYPE;
        if (nextWaypoint == null) {
            this.setCanFollow(true);
        } else if (this.getGrudge() > 0) {
            this.setFollowMin(2);
            this.getShipNavigate().tryMoveToXYZ(nextWaypoint.getX() + 0.5D, nextWaypoint.getY(), nextWaypoint.getZ() + 0.5D, 1.0D);
        }
        this.routeWaitTicks = 0;
        this.routeTransferCooldown = 0;
    }

    public int getCraneState() { return this.craneState; }
    public int getCraneTime() { return this.craneTime; }

    public boolean isDockedAtCrane(BlockPos pos) {
        return pos.equals(this.routeNodePos) && !this.canFollow() && !this.isOrderedToSit()
                && !this.isLeashed() && !this.isPassenger();
    }

    public void beginCraning(CraneBlockEntity crane) {
        this.craneState = 2;
        this.workingCrane = crane;
        BlockPos pos = crane.getBlockPos();
        this.getShipNavigate().tryMoveToXYZ(pos.getX() + 0.5D, pos.getY() - 2D, pos.getZ() + 0.5D, 0.5D);
    }

    public void setCraneState(int state) {
        this.craneState = state;
    }

    public void finishCraning() {
        this.craneState = 0;
        this.craneTime = 0;
        this.workingCrane = null;
    }

    public void finishCraning(CraneBlockEntity crane) {
        BlockPos currentNode = this.routeNodePos;
        this.finishCraning();
        this.advanceAfterRouteNode(crane);
        if (currentNode != null && currentNode.equals(this.lastWaypointPos)) {
            this.setFollowMin(2);
        }
    }

    private void advanceAfterRouteNode(RouteNode routeNode) {
        BlockPos currentNode = this.routeNodePos;
        if (currentNode == null) {
            return;
        }

        BlockPos nextWaypoint = routeNode.getNextWaypoint();
        if (nextWaypoint != null && nextWaypoint.equals(this.lastWaypointPos) && routeNode.getLastWaypoint() != null) {
            nextWaypoint = routeNode.getLastWaypoint();
        }

        this.lastWaypointPos = currentNode.immutable();
        if (nextWaypoint != null) {
            this.advanceRouteNode(nextWaypoint);
            return;
        }

        // The legacy helper keeps the current guard node when a route has no
        // outgoing link; it does not silently switch back to owner follow.
        this.commandedPos = currentNode.immutable();
        this.routeWaitTicks = 0;
        this.routeTransferCooldown = 0;
    }

    private void updateLegacyWaterState() {
        BlockPos origin = BlockPos.containing(this.getX(), this.getBoundingBox().minY, this.getZ());
        FluidState currentState = this.level().getFluidState(origin);
        boolean canFloat = this.canFloatUp();
        boolean updateCanFloat = true;

        if (currentState.isEmpty()) {
            this.legacyLiquidDepth = 0.0D;
            canFloat = false;
        } else {
            double depth = 1.0D;
            boolean foundSurface = false;
            for (int y = origin.getY() + 1; y < this.level().getMaxBuildHeight(); y++) {
                BlockPos scanPos = new BlockPos(origin.getX(), y, origin.getZ());
                if (!this.level().getFluidState(scanPos).isEmpty()) {
                    depth++;
                    continue;
                }

                canFloat = this.level().getBlockState(scanPos).isAir();
                foundSurface = true;
                break;
            }

            this.legacyLiquidDepth = depth - (this.getY() - (int) this.getY());
            updateCanFloat = foundSurface;
        }

        if (!this.level().isClientSide() && updateCanFloat && this.canFloatUp() != canFloat) {
            this.entityData.set(DATA_CAN_FLOAT_UP, canFloat);
        }
    }

    private boolean isAtLegacyGuardPosition() {
        if (this.level().getBlockState(this.blockPosition().above()).isAir()) {
            return false;
        }

        double minimumDistance = this.getFollowMin() + this.getBbWidth() * 0.5D;
        double minimumDistanceSqr = minimumDistance * minimumDistance;
        if (this.guardEntityUuid != null && this.level() instanceof ServerLevel serverLevel) {
            Entity guardedEntity = serverLevel.getEntity(this.guardEntityUuid);
            if (guardedEntity != null && this.distanceToSqr(guardedEntity) < minimumDistanceSqr) {
                return true;
            }
        }

        if (this.guardedBlockPos != null
                && (this.guardedBlockDimension.isBlank()
                || this.guardedBlockDimension.equals(this.level().dimension().location().toString()))
                && this.distanceToSqr(this.guardedBlockPos.getX(), this.guardedBlockPos.getY(),
                this.guardedBlockPos.getZ()) < minimumDistanceSqr
                && this.getY() >= this.guardedBlockPos.getY()) {
                return true;
        }

        if (this.canFollow()) {
            Player owner = this.getOwnerPlayer();
            if (owner != null) {
                double maximumDistance = this.getFollowMax() + this.getBbWidth() * 0.5D;
                return this.distanceToSqr(owner) < maximumDistance * maximumDistance;
            }
        }

        return false;
    }

    private boolean handleWaypointRouteNode(ServerLevel serverLevel, WaypointBlockEntity waypoint) {
        this.trySupplyAtRouteNode(serverLevel, waypoint.getPairedChest());

        return this.waitAtRouteNode(Math.max(this.getWaypointStayTicks(), waypoint.getStayTicks()), 16);
    }

    private boolean waitAtRouteNode(int waitTicks, int checkDelay) {
        if (waitTicks <= 0 || this.routeWaitTicks < 0) {
            return false;
        }

        if (this.routeWaitTicks == 0) {
            this.routeWaitTicks = waitTicks;
        }

        this.routeWaitTicks -= checkDelay;
        if (this.routeWaitTicks <= 0) {
            this.routeWaitTicks = -1;
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

    private int insertIntoShipCargo(ItemStack sourceStack, int maxItems) {
        if (sourceStack.isEmpty() || maxItems <= 0) {
            return 0;
        }

        int remaining = Math.min(maxItems, sourceStack.getCount());

        for (int slot = EQUIPMENT_SLOT_COUNT; slot < this.getInventorySizePaged() && remaining > 0; slot++) {
            ItemStack current = this.shipInventory.getItem(slot);
            if (current.isEmpty() || !ItemStack.isSameItemSameTags(current, sourceStack) || current.getCount() >= current.getMaxStackSize()) {
                continue;
            }

            int insert = Math.min(remaining, current.getMaxStackSize() - current.getCount());
            current.grow(insert);
            this.shipInventory.setItem(slot, current);
            remaining -= insert;
        }

        for (int slot = EQUIPMENT_SLOT_COUNT; slot < this.getInventorySizePaged() && remaining > 0; slot++) {
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

    private int getRouteTransferBudget() {
        return 8 + this.equipmentBehaviorState.transportTier() * 8;
    }

    private int getRouteTransferInterval() {
        return Math.max(6, 20 - this.equipmentBehaviorState.transportTier() * 3);
    }

    private @Nullable IFluidHandlerItem createRouteFluidHandler(ItemStack stack) {
        if (stack.isEmpty()) {
            return null;
        }

        return stack.copyWithCount(1).getCapability(ForgeCapabilities.FLUID_HANDLER_ITEM).resolve().orElse(null);
    }

    private void tickAutoPump() {
        if (!this.isAutoPump() || !this.isAlive() || this.isOrderedToSit() || this.hasNoFuel()) {
            return;
        }

        int delay = LegacyShipBehaviorCatalog.autoPumpDelayForLevel(this.getShipLevel());
        if (!this.hasAutoPumpEquipment()) {
            return;
        }

        if ((this.tickCount & delay) == 0) {
            BlockPos sourcePos = this.findAutoPumpSource();
            if (sourcePos != null) {
                Player owner = this.getOwnerPlayer();
                if (owner != null && owner.getAbilities().mayBuild && this.level().mayInteract(owner, sourcePos)) {
                    FluidState sourceState = this.level().getFluidState(sourcePos);
                    if (sourceState.isSource()) {
                        FluidStack source = new FluidStack(sourceState.getType(), AUTO_PUMP_BUCKET_AMOUNT);
                        int moved = this.fillAutoPumpContainers(source);
                        if (moved > 0) {
                            Fluid sourceFluid = sourceState.getType();
                            boolean infiniteSource = (sourceFluid == Fluids.WATER || sourceFluid == Fluids.LAVA)
                                    && this.hasInfiniteAutoPumpSource(sourcePos, sourceFluid,
                                    sourceFluid == Fluids.WATER ? AUTO_PUMP_WATER_DEPTH : AUTO_PUMP_LAVA_DEPTH);
                            if (!infiniteSource) {
                                this.level().setBlock(sourcePos, Blocks.AIR.defaultBlockState(), 11);
                                if (this.random.nextInt(3) == 0) {
                                    this.playShipCombatSound(SoundEvents.BUCKET_FILL, 0.5F,
                                            this.random.nextFloat() * 0.4F + 0.8F);
                                }
                            }

                            if (this.getSpec().archetype() == ShipArchetype.TRANSPORT) {
                                this.addShipExperience(LegacyShipBehaviorCatalog.pickupExperience());
                            }
                        }
                    }
                }
            }
        }

        if ((this.tickCount & 3) == 0) {
            this.tickAutoPumpExperience();
        }
    }

    private void tickAutoPumpExperience() {
        int bottleSlot = -1;
        for (int slot = EQUIPMENT_SLOT_COUNT; slot < this.getInventorySizePaged(); slot++) {
            if (this.shipInventory.getItem(slot).is(Items.GLASS_BOTTLE)) {
                bottleSlot = slot;
                break;
            }
        }
        if (bottleSlot < 0) {
            return;
        }

        List<ExperienceOrb> orbs = this.level().getEntitiesOfClass(
                ExperienceOrb.class, this.getBoundingBox().inflate(7.0D));
        for (ExperienceOrb orb : orbs) {
            double distanceSqr = this.distanceToSqr(orb);
            if (distanceSqr > 9.0D) {
                Vec3 pull = this.position().subtract(orb.position());
                double distance = pull.length();
                if (distance > 1.0E-4D) {
                    orb.setDeltaMovement(orb.getDeltaMovement().add(pull.scale(0.25D / distance)));
                    orb.hasImpulse = true;
                }
                continue;
            }

            this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                    SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 0.1F,
                    0.5F * ((this.random.nextFloat() - this.random.nextFloat()) * 0.7F + 1.8F));
            if (orb.getValue() > 0) {
                this.autoPumpExperience += orb.getValue();
            }
            orb.discard();
        }

        if (this.autoPumpExperience < 8) {
            return;
        }

        this.autoPumpExperience -= 8;
        ItemStack bottles = this.shipInventory.getItem(bottleSlot);
        bottles.shrink(1);
        this.shipInventory.setItem(bottleSlot, bottles.isEmpty() ? ItemStack.EMPTY : bottles);

        ItemStack experienceBottle = new ItemStack(Items.EXPERIENCE_BOTTLE);
        if (this.insertIntoShipCargo(experienceBottle, 1) < 1) {
            this.spawnAtLocation(experienceBottle);
        }
    }

    private boolean hasAutoPumpEquipment() {
        if (this.getSpec().archetype() == ShipArchetype.TRANSPORT && this.isMarried()) {
            return true;
        }

        for (int slot = 0; slot < Math.min(EQUIPMENT_SLOT_COUNT, this.shipInventory.getContainerSize()); slot++) {
            ItemStack stack = this.shipInventory.getItem(slot);
            if (stack.getItem() instanceof LegacyEquipmentItem equipment
                    && equipment.family() == LegacyEquipmentFamily.DRUM
                    && equipment.variantIndex() == 1) {
                return true;
            }
        }

        return false;
    }

    @Nullable
    private BlockPos findAutoPumpSource() {
        BlockPos origin = this.blockPosition();

        for (int y = 1; y > -3; y--) {
            for (int x = -3; x <= 3; x++) {
                for (int z = -3; z <= 3; z++) {
                    if (x == 0 && z == 0) {
                        continue;
                    }

                    BlockPos candidate = origin.offset(x, y, z);
                    if (this.level().getFluidState(candidate).isSource()) {
                        return candidate;
                    }
                }
            }
        }

        for (int y = 1; y > -2; y--) {
            BlockPos candidate = origin.offset(0, y, 0);
            if (this.level().getFluidState(candidate).isSource()) {
                return candidate;
            }
        }

        return null;
    }

    private int fillAutoPumpContainers(FluidStack source) {
        int moved = 0;
        FluidStack remaining = source.copy();

        for (int slot = EQUIPMENT_SLOT_COUNT;
             slot < this.getInventorySizePaged() && !remaining.isEmpty();
             slot++) {
            ItemStack stack = this.shipInventory.getItem(slot);
            if (stack.isEmpty() || stack.getCount() != 1) {
                continue;
            }

            IFluidHandlerItem handler = this.createRouteFluidHandler(stack);
            if (handler == null) {
                continue;
            }

            int accepted = handler.fill(remaining, IFluidHandler.FluidAction.EXECUTE);
            if (accepted <= 0) {
                continue;
            }

            this.shipInventory.setItem(slot, handler.getContainer());
            moved += accepted;
            remaining.shrink(accepted);
        }

        return moved;
    }

    private boolean hasInfiniteAutoPumpSource(BlockPos sourcePos, Fluid fluid, int depth) {
        for (int sample = 0; sample < 3; sample++) {
            int sampleX = sourcePos.getX() + this.random.nextInt(3) - 1;
            int sampleZ = sourcePos.getZ() + this.random.nextInt(3) - 1;

            for (int y = 0; y < depth; y++) {
                FluidState state = this.level().getFluidState(new BlockPos(sampleX, sourcePos.getY() - y, sampleZ));
                if (!this.isSameAutoPumpFluid(state, fluid)) {
                    return false;
                }
            }
        }

        return true;
    }

    private boolean isSameAutoPumpFluid(FluidState state, Fluid target) {
        Fluid candidate = state.getType();
        return !state.isEmpty()
                && (candidate.isSame(target)
                || target == Fluids.WATER && candidate == Fluids.FLOWING_WATER
                || target == Fluids.LAVA && candidate == Fluids.FLOWING_LAVA);
    }

    private void tickAutoSupportItems() {
        if (this.isHostileVariant() || !this.isAlive()) {
            return;
        }

        if ((this.tickCount & 31) == 0
                && (this.getMaxHealth() - this.getHealth()) > (this.getMaxHealth() * 0.1F + 5.0F)
                && this.consumeLegacySupplyItem(ModItems.BUCKETREPAIR.get())) {
            this.heal(this.getMaxHealth() * 0.08F + 15.0F);
            if (LegacyShipBehaviorCatalog.managesAircraftInventory(this.getSpec())) {
                this.setAircraftLight(this.getAircraftLight() + 1);
                this.setAircraftHeavy(this.getAircraftHeavy() + 1);
            }
        }

        if ((this.tickCount & 127) == 0
                && LegacyShipBehaviorCatalog.legacyMoraleIdFor(this.getMorale()) >= this.getCombatRationLevel()
                && this.getFoodSaturation() < this.getFoodSaturationMax()) {
            this.autoUseCombatRation();
        }

        if ((this.tickCount & 255) == 0) {
            if (this.getFoodSaturation() > 0) {
                this.setFoodSaturation(this.getFoodSaturation() - 1);
            }
            if (this.getHealth() < this.getMaxHealth()) {
                this.heal(this.getMaxHealth() * 0.03F + 1.0F);
            }
        }
    }

    private void autoUseCombatRation() {
        for (int slot = EQUIPMENT_SLOT_COUNT; slot < this.getInventorySizePaged(); slot++) {
            ItemStack stack = this.shipInventory.getItem(slot);
            if (!(stack.getItem() instanceof CombatRationItem ration)) {
                continue;
            }

            this.addMorale(ration.getMoraleValue());
            this.addGrudge((int) (ration.rollGrudge(this.random) * this.legacyStats.get(LegacyShipStatTables.Attr.GRUDGE)));
            this.addFoodSaturation(4);
            if (ration.clearsDebuffs()) {
                this.clearNegativeEffects();
            }
            stack.shrink(1);
            if (stack.isEmpty()) {
                this.shipInventory.setItem(slot, ItemStack.EMPTY);
            } else {
                this.shipInventory.setItem(slot, stack);
            }
            return;
        }
    }

    /** Mirrors BasicEntityShip.updateConsumeItem for registered BasicEntityShip classes. */
    private void tickLegacyMovementAndIdleConsumption() {
        if (!this.isAlive() || (this.tickCount & 127) != 0) {
            return;
        }

        int idleCost = LegacyShipBehaviorCatalog.legacyIdleGrudgeConsumptionFor(this.getSpec());
        if (idleCost < 0) {
            return;
        }

        this.reloadLegacyAmmoIfNeeded(false);
        this.reloadLegacyAmmoIfNeeded(true);

        double distanceX = this.getX() - this.legacyPreviousX;
        double distanceY = this.getY() - this.legacyPreviousY;
        double distanceZ = this.getZ() - this.legacyPreviousZ;
        int movement = (int) Math.sqrt(distanceX * distanceX + distanceY * distanceY + distanceZ * distanceZ);
        if (this.legacyPreviousY <= 0.0D) {
            movement = 0;
        }

        int moraleCost = Mth.clamp((int) (movement * 0.5F), 5, 50);
        this.addMorale(-moraleCost);
        int movementGrudge = movement * LegacyShipBehaviorCatalog.legacyMovementGrudgeCost();
        this.addShipExperience(LegacyShipBehaviorCatalog.legacyMovementExperienceFor(
                this.getSpec(), movementGrudge, this.tickCount));
        this.consumeLegacyGrudge(movementGrudge + idleCost);
        if (!this.hasNoFuel()) {
            this.updateLegacyMorale();
        }

        this.legacyPreviousX = this.getX();
        this.legacyPreviousY = this.getY();
        this.legacyPreviousZ = this.getZ();
    }

    /** Mirrors BasicEntityShip.updateMorale every 128 ticks when fueled. */
    private void updateLegacyMorale() {
        int m = this.getMorale();
        if (this.isLegacyOutOfCombat()) {
            if (m < LegacyShipStatTables.MORALE_NORMAL_LOWER + 1000) {
                this.addMorale(40);
            } else if (m > LegacyShipStatTables.MORALE_HAPPY_LOWER) {
                this.addMorale(-10);
            }
        } else {
            if (m < LegacyShipStatTables.MORALE_TIRED_LOWER) {
                this.addMorale(-11);
            } else if (m < LegacyShipStatTables.MORALE_NORMAL_LOWER) {
                this.addMorale(-7);
            } else if (m < LegacyShipStatTables.MORALE_HAPPY_LOWER) {
                this.addMorale(-5);
            } else {
                this.addMorale(-11);
            }
        }
    }

    /** Mirrors ShipStateHandler.decrAmmo(AMMO, 0) and its reserve threshold. */
    private void reloadLegacyAmmoIfNeeded(boolean heavy) {
        LegacyShipAttackKind attackKind = heavy ? LegacyShipAttackKind.HEAVY : LegacyShipAttackKind.LIGHT;
        int minimumAmmo = LegacyShipBehaviorCatalog.minimumAmmoForGoal(this.getSpec(), attackKind);
        int currentAmmo = heavy ? this.getHeavyAmmo() : this.getLightAmmo();
        if (currentAmmo >= minimumAmmo) {
            return;
        }

        Item normalItem = heavy ? ModItems.AMMO2.get() : ModItems.AMMO.get();
        Item containerItem = heavy ? ModItems.AMMO3.get() : ModItems.AMMO1.get();
        int ammoGain = heavy ? ShinColleConfig.itemHeavyAmmo() : ShinColleConfig.itemLightAmmo();
        if (this.consumeLegacySupplyItem(normalItem)) {
            this.reactEatItem();
        } else if (this.consumeLegacySupplyItem(containerItem)) {
            this.reactEatItem();
            ammoGain *= 9;
        } else {
            ammoGain = 0;
        }
        int scaledGain = (int) (ammoGain * this.legacyStats.get(LegacyShipStatTables.Attr.AMMO));
        if (heavy) {
            this.addHeavyAmmo(scaledGain);
        } else {
            this.addLightAmmo(scaledGain);
        }
        if ((heavy ? this.getHeavyAmmo() : this.getLightAmmo()) < minimumAmmo) {
            this.reactItemNotFound();
        }
    }

    private void consumeLegacyGrudge(int value) {
        MobEffectInstance hunger = this.getEffect(MobEffects.HUNGER);
        if (hunger != null && value > 0) {
            value *= 1 + 2 * (hunger.getAmplifier() + 1);
        }
        if (!this.hasNoFuel()) {
            this.addGrudge(-value);
        }
        if (this.getGrudge() <= 0) {
            int supply = ShinColleConfig.itemGrudge();
            if (this.consumeLegacySupplyItem(ModItems.GRUDGE.get())) {
                this.reactEatItem();
            } else if (this.consumeLegacySupplyItem(ModBlocks.BLOCK_GRUDGE.get().asItem())) {
                this.reactEatItem();
                supply *= 9;
            } else {
                supply = 0;
            }
            this.addGrudge((int) (supply * this.legacyStats.get(LegacyShipStatTables.Attr.GRUDGE)));
        }
        if (this.hasNoFuel() && !this.fuelGoalsSuspended) {
            this.setMorale(0);
        }
    }

    private void updateFuelGoals() {
        if (this.isHostileVariant() || !this.isAlive() || this.tickCount <= 10) {
            return;
        }

        if (this.tickCount == 11 && this.hasNoFuel()) {
            this.consumeLegacyGrudge(0);
        }
        boolean noFuel = this.hasNoFuel();
        if (this.fuelGoalsSuspended == noFuel) {
            return;
        }

        this.fuelGoalsSuspended = noFuel;
        this.setTarget(null);
        this.getShipNavigate().clearPathEntity();
        if (noFuel) {
            this.setMorale(0);
            this.goalSelector.getRunningGoals().forEach(goal -> goal.stop());
            this.targetSelector.getRunningGoals().forEach(goal -> goal.stop());
            this.goalSelector.removeAllGoals(goal -> true);
            this.targetSelector.removeAllGoals(goal -> true);
            if (this.supplyEmoteDelay <= 0) {
                this.supplyEmoteDelay = 20;
                CombatFxDispatcher.sendLegacyEmotion(this, switch (this.random.nextInt(7)) {
                    case 1 -> 0;
                    case 2 -> 32;
                    case 3 -> 2;
                    case 4 -> 12;
                    case 5 -> 5;
                    case 6 -> 20;
                    default -> 10;
                });
            }
        } else {
            this.registerGoals();
            if (this.supplyEmoteDelay <= 0) {
                this.supplyEmoteDelay = 40;
                CombatFxDispatcher.sendLegacyEmotion(this, switch (this.random.nextInt(5)) {
                    case 1 -> 31;
                    case 2 -> 32;
                    case 3 -> 7;
                    default -> 1;
                });
            }
        }
    }

    private void reactEatItem() {
        if (this.supplyEmoteDelay <= 0) {
            this.supplyEmoteDelay = 40;
            CombatFxDispatcher.sendLegacyEmotion(this, switch (this.random.nextInt(4)) {
                case 1 -> 29;
                case 2 -> 30;
                default -> 9;
            });
        }
    }

    private void reactItemNotFound() {
        if (this.supplyEmoteDelay <= 0) {
            this.supplyEmoteDelay = 20;
            CombatFxDispatcher.sendLegacyEmotion(this, switch (this.random.nextInt(7)) {
                case 1 -> 0;
                case 2 -> 2;
                case 3 -> 5;
                case 4 -> 20;
                default -> 32;
            });
        }
    }

    private boolean dimensionMatchesCommand(ServerLevel serverLevel) {
        return this.dimensionMatches(this.commandDimension, serverLevel);
    }

    private boolean dimensionMatches(String dimensionId, ServerLevel serverLevel) {
        if (dimensionId == null || dimensionId.isBlank()) {
            return true;
        }

        return dimensionId.equals(serverLevel.dimension().location().toString());
    }

    private static boolean hasModernCommandState(CompoundTag tag) {
        return tag.contains(COMMAND_POS_TAG, Tag.TAG_LONG)
                || tag.hasUUID(GUARD_ENTITY_TAG)
                || tag.contains(GUARD_BLOCK_POS_TAG, Tag.TAG_LONG)
                || tag.contains(ROUTE_NODE_TAG, Tag.TAG_LONG);
    }

    private void loadLegacyGuardBlock(CompoundTag tag) {
        CompoundTag minor = getLegacyMinorTag(tag);
        if (minor == null
                || !minor.contains(LEGACY_GUARD_TYPE_TAG, Tag.TAG_INT)
                || minor.getInt(LEGACY_GUARD_TYPE_TAG) < 0
                || minor.getInt(LEGACY_GUARD_TYPE_TAG) > LEGACY_GUARD_BLOCK_TYPE
                || !minor.contains(LEGACY_GUARD_X_TAG, Tag.TAG_INT)
                || !minor.contains(LEGACY_GUARD_Y_TAG, Tag.TAG_INT)
                || !minor.contains(LEGACY_GUARD_Z_TAG, Tag.TAG_INT)
                || !minor.contains(LEGACY_GUARD_DIM_TAG, Tag.TAG_INT)) {
            return;
        }

        this.guardedBlockPos = new BlockPos(
                minor.getInt(LEGACY_GUARD_X_TAG),
                minor.getInt(LEGACY_GUARD_Y_TAG),
                minor.getInt(LEGACY_GUARD_Z_TAG));
        this.guardedBlockDimension = LegacyShipBehaviorCatalog.modernDimensionIdForLegacy(
                minor.getInt(LEGACY_GUARD_DIM_TAG));
        if (this.guardedBlockDimension.isBlank()) {
            this.guardedBlockDimension = this.level().dimension().location().toString();
        }
    }

    private int encodeAiFlags() {
        int flags = 0;
        if (this.aiAutoTarget) {
            flags |= GameplayCommandHandler.AI_FLAG_AUTO_TARGET;
        }
        if (this.aiPickItem) {
            flags |= GameplayCommandHandler.AI_FLAG_PICK_ITEM;
        }
        return flags;
    }

    private void applySyncedAiData() {
        this.applyAiData(this.entityData.get(DATA_AI_FLAGS));
    }

    private void applyAiData(int flags) {
        this.aiAutoTarget = (flags & GameplayCommandHandler.AI_FLAG_AUTO_TARGET) != 0;
        this.aiPickItem = (flags & GameplayCommandHandler.AI_FLAG_PICK_ITEM) != 0;
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

    public static boolean isStopAi() {
        return stopAi;
    }

    public static void setStopAi(boolean value) {
        stopAi = value;
    }

    public static boolean toggleStopAi() {
        stopAi = !stopAi;
        return stopAi;
    }

    private boolean canExecuteActiveGoal() {
        return !stopAi && !this.isOrderedToSit() && !this.hasNoFuel()
                && (this.isHostileVariant() || this.getOwnerUuid().isPresent());
    }

    private boolean canExecuteTargetGoal() {
        return this.canExecuteActiveGoal() && !this.hasExplicitMovementCommand();
    }

    private boolean hasExplicitMovementCommand() {
        return this.commandedPos != null
                || this.guardEntityUuid != null
                || this.guardedBlockPos != null
                || this.routeNodePos != null;
    }

    private void tickEquipmentBehaviors() {
        if (!ShinColleConfig.canSearchlight()) {
            return;
        }

        boolean isAkatsukiMob = this.getVariantEggMeta() == 2053;
        boolean hasSearchlightEquip = this.equipmentBehaviorState.searchlightLevel() > 0;
        if (!hasSearchlightEquip && !isAkatsukiMob) {
            return;
        }

        boolean tickDue = isAkatsukiMob ? (this.tickCount & 15) == 0 : (this.tickCount % 20 == 0);
        if (!tickDue) {
            return;
        }

        if (this.level().getMaxLocalRawBrightness(this.blockPosition()) > 7) {
            return;
        }

        LightBlockHelper.placeLightBlock(this.level(), this.blockPosition().above());
        if (!hasSearchlightEquip) {
            return;
        }

        LivingEntity target = this.getTarget();
        int searchRange = this.equipmentBehaviorState.searchlightRange();
        if (target == null || !target.isAlive() || this.distanceToSqr(target) > searchRange * searchRange || !this.canEngage(target)) {
            target = this.findSearchlightTarget(searchRange);
        }

        if (target != null) {
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

    private void emitIlluminationFx(LivingEntity target, boolean flareSource) {
        if (this.level().isClientSide()) {
            return;
        }

        if ((flareSource && !ShinColleConfig.canFlare()) || (!flareSource && !ShinColleConfig.canSearchlight())) {
            return;
        }

        LightBlockHelper.placeLightBlock(this.level(), target.blockPosition().above());
        CombatFxDispatcher.sendParticle(target,
                flareSource ? GameplayParticleType.FLARE_BURST : GameplayParticleType.SEARCHLIGHT_MARK,
                target.getX(), target.getY() + target.getBbHeight() * 0.85D, target.getZ());
    }

    private void tickMarriageBond() {
        if ((this.tickCount & 127) != 0
                || !this.isMarried()
                || !this.isRingEffectEnabled()
                || this.getGrudge() <= 0) {
            return;
        }

        if (LegacyShipBehaviorCatalog.usesPerShipRingInvisibility(this.getSpec())) {
            return;
        }

        LegacyShipBehaviorCatalog.tickMarriagePassive(this);
    }

    private void tickLegacyRingPassives() {
        LegacyShipBehaviorCatalog.tickLegacyPerShipBehavior(this);
    }

    void applyCarrierRingAura(int durationTicks, int amplifier) {
        this.applyAlliedRingAura(MobEffects.JUMP, durationTicks, amplifier);
    }

    /** Legacy marriage auras use TeamHelper.checkSameOwner, not the ally relation. */
    void applyAlliedRingAura(MobEffect effect, int durationTicks, int amplifier) {
        this.applySameOwnerShipRingAura(effect, durationTicks, amplifier);
    }

    void applySameOwnerShipRingAura(MobEffect effect, int durationTicks, int amplifier) {
        Optional<UUID> ownerUuid = this.getOwnerUuid();
        if (ownerUuid.isEmpty()) {
            return;
        }

        for (LegacyShipEntity ship : this.level().getEntitiesOfClass(
                LegacyShipEntity.class, this.getBoundingBox().inflate(16.0D))) {
            if (!ship.isHostileVariant() && ownerUuid.equals(ship.getOwnerUuid())) {
                this.applyLegacyRingEffect(ship, effect, durationTicks, amplifier);
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
        this.getShipNavigate().clearPathEntity();
        this.setTarget(null);
        this.setOrderedToSit(false);
        this.modernHealthSteps = 0;
        this.modernAttackSteps = 0;
        this.modernDefenseSteps = 0;
        this.modernAttackSpeedSteps = 0;
        this.modernSpeedSteps = 0;
        this.modernRangeSteps = 0;
        this.entityData.set(DATA_MODERNIZATION_TOTAL, 0);
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

        if (stack.getItem() instanceof CombatRationItem
                || stack.getItem() instanceof LegacyShipSupportItem
                || stack.is(Items.POTION)
                || stack.getFoodProperties(this) != null) {
            if (!this.canAcceptFood()) {
                this.displayFoodSaturationFull(player);
                return InteractionResult.CONSUME;
            }

            if (stack.getItem() instanceof CombatRationItem ration) {
                this.feedCombatRation(player, hand, stack, ration);
                return InteractionResult.CONSUME;
            }

            if (this.feedLegacyItem(player, hand, stack)) {
                return InteractionResult.CONSUME;
            }

            return InteractionResult.PASS;
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
                this.playInteractionVoice(player, ShipSoundType.MARRY, 0.65F);
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
            this.consumeHeldItem(player, hand, 1);
            if (player instanceof ServerPlayer serverPlayer) {
                TeitokuHelper.incrementMarriageCount(serverPlayer);
            }
            this.playInteractionVoice(player, ShipSoundType.MARRY, 0.8F);
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

            if (!(this.level() instanceof ServerLevel serverLevel)) {
                return InteractionResult.CONSUME;
            }

            OwnerPaperItem.Signer targetSigner = OwnerPaperItem.resolveTransferTarget(serverLevel, stack, this.ownerUid).orElse(null);
            if (targetSigner == null) {
                player.displayClientMessage(Component.translatable("chat.shincolle.ship.ownerpaper_invalid"), true);
                return InteractionResult.CONSUME;
            }

            this.setOwner(targetSigner.uuid(), targetSigner.name(), targetSigner.uid());
            player.setItemInHand(hand, ItemStack.EMPTY);
            this.setEmotion(2);
            this.playInteractionVoice(player, ShipSoundType.MARRY, 0.6F);
            player.displayClientMessage(Component.translatable("chat.shincolle.ship.ownerpaper_transfer",
                    this.getName().copy().withStyle(ChatFormatting.AQUA),
                    Component.literal(targetSigner.name()).withStyle(ChatFormatting.GOLD)), true);
            return InteractionResult.CONSUME;
        }

        if (stack.is(ModItems.KAITAIHAMMER.get()) && player.isShiftKeyDown()) {
            if (!this.performKaitai(player, stack)) {
                this.displayOwnerLocked(player);
            }
            return InteractionResult.CONSUME;
        }

        if (stack.is(ModItems.POINTERITEM.get())) {
            if (PointerItem.getMode(stack) == 3 && !player.isShiftKeyDown()) {
                if (!this.canCommanderEdit(player)) {
                    this.displayOwnerLocked(player);
                    return InteractionResult.CONSUME;
                }

                int moraleGain = this.legacyHitHeight >= 75 ? 30 : ShinColleConfig.baseCaressMorale();
                this.addMorale(moraleGain);
                this.playInteractionVoice(player, ShipSoundType.FEED, 0.55F);
                player.displayClientMessage(Component.translatable("chat.shincolle.ship.caress",
                        this.getName().copy().withStyle(ChatFormatting.AQUA), this.getMorale()), true);
                return InteractionResult.CONSUME;
            }

            return InteractionResult.PASS;
        }

        return InteractionResult.PASS;
    }

    private boolean isShipInteractionItem(ItemStack stack) {
        return stack.getItem() instanceof CombatRationItem
                || stack.getItem() instanceof LegacyShipSupportItem
                || stack.is(Items.POTION)
                || stack.getFoodProperties(this) != null
                || stack.is(ModItems.BUCKETREPAIR.get())
                || stack.is(ModItems.MODERNKIT.get())
                || stack.is(ModItems.TRAININGBOOK.get())
                || stack.is(ModItems.MARRIAGERING.get())
                || stack.is(ModItems.OWNERPAPER.get())
                || stack.is(ModItems.KAITAIHAMMER.get())
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
        if (LegacyShipBehaviorCatalog.managesAircraftInventory(this.getSpec())) {
            this.setAircraftLight(this.getAircraftLight() + 1);
            this.setAircraftHeavy(this.getAircraftHeavy() + 1);
        }
        this.consumeHeldItem(player, hand, 1);
        this.playInteractionVoice(player, ShipSoundType.PICKITEM, 0.55F);
        player.displayClientMessage(Component.translatable("chat.shincolle.ship.repair",
                this.getName().copy().withStyle(ChatFormatting.AQUA), Math.round(this.getHealth()), Math.round(this.getMaxHealth())), true);
    }

    private void feedCombatRation(Player player, InteractionHand hand, ItemStack stack, CombatRationItem ration) {
        this.addMorale(ration.getMoraleValue());
        this.addGrudge((int) (ration.rollGrudge(this.random) * this.legacyStats.get(LegacyShipStatTables.Attr.GRUDGE)));
        this.addFoodSaturation(4);
        if (ration.clearsDebuffs()) {
            this.clearNegativeEffects();
        }

        this.consumeHeldItem(player, hand, 1);
        this.playInteractionVoice(player, ShipSoundType.FEED, 0.65F);
        player.displayClientMessage(Component.translatable("chat.shincolle.ship.feed",
                this.getName().copy().withStyle(ChatFormatting.LIGHT_PURPLE), this.getMorale()), true);
    }

    private void playInteractionVoice(Player player, ShipSoundType type, float volume) {
        ShinColleSoundHelper.playForPlayer(
                this.level(),
                player,
                ShinColleSoundHelper.getShipVoice(type, this.getShipClassId()),
                volume * ShinColleConfig.volumeShip(),
                ShinColleSoundHelper.variedPitch(player, 1.0F, 0.08F));
    }

    private boolean feedLegacyItem(Player player, InteractionHand hand, ItemStack stack) {
        int moraleGain = 0;
        int grudgeGain = 0;
        int lightAmmoGain = 0;
        int heavyAmmoGain = 0;
        boolean consumed = false;

        if (stack.is(ModItems.GRUDGE.get())) {
            moraleGain = 10 + this.random.nextInt(11);
            grudgeGain = 300 + this.random.nextInt(500);
            consumed = true;
        } else if (stack.is(ModItems.GRUDGE1.get())) {
            moraleGain = 10 + this.random.nextInt(11);
            grudgeGain = 300 + this.random.nextInt(500);
            consumed = true;
        } else if (stack.is(ModItems.AMMO.get())) {
            moraleGain = 5 + this.random.nextInt(6);
            lightAmmoGain = 30 + this.random.nextInt(10);
            consumed = true;
        } else if (stack.is(ModItems.AMMO1.get())) {
            moraleGain = 5 + this.random.nextInt(6);
            lightAmmoGain = 270 + this.random.nextInt(90);
            consumed = true;
        } else if (stack.is(ModItems.AMMO2.get())) {
            moraleGain = 5 + this.random.nextInt(6);
            heavyAmmoGain = 15 + this.random.nextInt(5);
            consumed = true;
        } else if (stack.is(ModItems.AMMO3.get())) {
            moraleGain = 5 + this.random.nextInt(6);
            heavyAmmoGain = 135 + this.random.nextInt(45);
            consumed = true;
        } else if (stack.is(ModItems.ABYSSMETAL.get())) {
            moraleGain = 30 + this.random.nextInt(31);
            this.heal(this.getMaxHealth() * 0.05F + 1.0F);
            consumed = true;
        } else if (stack.is(ModItems.ABYSSMETAL1.get())) {
            moraleGain = 30 + this.random.nextInt(31);
            if (this.attackProfile.hasAirAttack() && LegacyShipBehaviorCatalog.managesAircraftInventory(this.getSpec())
                    && this.random.nextInt(10) > 4) {
                this.setAircraftLight(this.getAircraftLight() + 1);
                this.setAircraftHeavy(this.getAircraftHeavy() + 1);
            }
            consumed = true;
        } else if (stack.is(ModItems.TOYAIRPLANE.get())) {
            moraleGain = 150 + this.random.nextInt(151);
            if (this.attackProfile.hasAirAttack() && LegacyShipBehaviorCatalog.managesAircraftInventory(this.getSpec())) {
                this.setAircraftLight(this.getAircraftLight() + this.random.nextInt(3) + 1);
                this.setAircraftHeavy(this.getAircraftHeavy() + this.random.nextInt(3) + 1);
            }
            consumed = true;
        } else if (stack.is(Items.POTION)) {
            moraleGain = -100;
            grudgeGain = 300;
            for (MobEffectInstance effect : PotionUtils.getMobEffects(stack)) {
                this.applySupportEffect(new MobEffectInstance(effect));
            }
            consumed = true;
        } else {
            FoodProperties foodProperties = stack.getFoodProperties(this);
            if (foodProperties == null) {
                return false;
            }

            float nutrition = Math.max(1.0F, foodProperties.getNutrition());
            moraleGain = Math.max(1, Math.round((nutrition + this.random.nextInt((int) nutrition + 5))
                    * foodProperties.getSaturationModifier() * 20.0F));
            grudgeGain = moraleGain;
            consumed = true;
        }

        if (!consumed) {
            return false;
        }

        this.addMorale(moraleGain);
        this.addGrudge((int) (grudgeGain * this.legacyStats.get(LegacyShipStatTables.Attr.GRUDGE)));
        this.addLightAmmo((int) (lightAmmoGain * this.legacyStats.get(LegacyShipStatTables.Attr.AMMO)));
        this.addHeavyAmmo((int) (heavyAmmoGain * this.legacyStats.get(LegacyShipStatTables.Attr.AMMO)));
        this.addFoodSaturation(1);
        this.consumeHeldItem(player, hand, 1);
        this.playInteractionVoice(player, ShipSoundType.FEED, 0.65F);
        player.displayClientMessage(Component.translatable("chat.shincolle.ship.feed",
                this.getName().copy().withStyle(ChatFormatting.LIGHT_PURPLE), this.getMorale()), true);
        return true;
    }

    private boolean canAcceptFood() {
        return this.getFoodSaturation() < this.getFoodSaturationMax();
    }

    private void displayFoodSaturationFull(Player player) {
        player.displayClientMessage(Component.translatable("chat.shincolle.ship.feed_full",
                this.getName().copy().withStyle(ChatFormatting.GRAY)), true);
    }

    private void applySupportEffect(MobEffectInstance effect) {
        MobEffect mobEffect = effect.getEffect();
        if (mobEffect.isInstantenous()) {
            mobEffect.applyInstantenousEffect(null, null, this, effect.getAmplifier(), 1.0D);
            return;
        }

        this.addEffect(new MobEffectInstance(effect));
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

    private void openInventory(ServerPlayer player) {
        NetworkHooks.openScreen(player,
                new SimpleMenuProvider(
                        (containerId, inventory, menuPlayer) -> new ShipInventoryMenu(containerId, inventory, this.getId(), this.getShipUid()),
                        Component.translatable("gui.shincolle.ship_inventory.title", this.getName())),
                buffer -> {
                    ShipInventoryMenu.writeOpeningData(buffer, this);
                });
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

    boolean consumeLegacySupplyItem(Item item) {
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

    private boolean consumeFirstMatchingItem(Item item) {
        return this.consumeLegacySupplyItem(item);
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

    private static int getIntWithLegacyMinorFallback(CompoundTag tag, String key, String fallbackKey,
                                                     String minorKey, int defaultValue) {
        if (tag.contains(key, Tag.TAG_INT)) {
            return tag.getInt(key);
        }

        if (fallbackKey != null && tag.contains(fallbackKey, Tag.TAG_INT)) {
            return tag.getInt(fallbackKey);
        }

        CompoundTag minor = getLegacyMinorTag(tag);
        return minor != null && minorKey != null && minor.contains(minorKey, Tag.TAG_INT)
                ? minor.getInt(minorKey)
                : defaultValue;
    }

    private static int getIntWithLegacyDisplayFallback(CompoundTag tag, String key, String displayKey,
                                                       int defaultValue) {
        if (tag.contains(key, Tag.TAG_INT)) {
            return tag.getInt(key);
        }

        CompoundTag display = getLegacyDisplayTag(tag);
        return display != null && display.contains(displayKey, Tag.TAG_INT)
                ? display.getInt(displayKey)
                : defaultValue;
    }

    private static String readOwnerName(CompoundTag tag) {
        if (tag.contains(OWNER_NAME_TAG, Tag.TAG_STRING)) {
            return tag.getString(OWNER_NAME_TAG);
        }

        CompoundTag extended = getLegacyExtendedPropertiesTag(tag);
        return extended != null && extended.contains(LEGACY_OWNER_NAME_TAG, Tag.TAG_STRING)
                ? extended.getString(LEGACY_OWNER_NAME_TAG)
                : "";
    }

    private static @Nullable CompoundTag getLegacyExtendedPropertiesTag(CompoundTag tag) {
        return tag.contains(LEGACY_EXTENDED_PROPERTIES_TAG, Tag.TAG_COMPOUND)
                ? tag.getCompound(LEGACY_EXTENDED_PROPERTIES_TAG)
                : null;
    }

    private static @Nullable CompoundTag getLegacyMinorTag(CompoundTag tag) {
        CompoundTag extended = getLegacyExtendedPropertiesTag(tag);
        return extended != null && extended.contains(LEGACY_MINOR_TAG, Tag.TAG_COMPOUND)
                ? extended.getCompound(LEGACY_MINOR_TAG)
                : null;
    }

    private static @Nullable CompoundTag getLegacyDisplayTag(CompoundTag tag) {
        if (tag.contains(LEGACY_DISPLAY_TAG, Tag.TAG_COMPOUND)) {
            return tag.getCompound(LEGACY_DISPLAY_TAG);
        }

        CompoundTag extended = getLegacyExtendedPropertiesTag(tag);
        return extended != null && extended.contains(LEGACY_DISPLAY_TAG, Tag.TAG_COMPOUND)
                ? extended.getCompound(LEGACY_DISPLAY_TAG)
                : null;
    }

    private static boolean getBooleanWithLegacyFlagsFallback(CompoundTag tag, String key,
                                                              String legacyKey, boolean defaultValue) {
        if (tag.contains(key, Tag.TAG_BYTE)) {
            return tag.getBoolean(key);
        }

        CompoundTag extended = getLegacyExtendedPropertiesTag(tag);
        if (extended == null || !extended.contains(LEGACY_FLAGS_TAG, Tag.TAG_COMPOUND)) {
            return defaultValue;
        }

        CompoundTag flags = extended.getCompound(LEGACY_FLAGS_TAG);
        return flags.contains(legacyKey, Tag.TAG_BYTE) ? flags.getBoolean(legacyKey) : defaultValue;
    }

}
