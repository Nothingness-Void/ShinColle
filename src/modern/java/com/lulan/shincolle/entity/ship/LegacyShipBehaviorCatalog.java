package com.lulan.shincolle.entity.ship;

import com.lulan.shincolle.blockentity.WaypointBlockEntity;
import com.lulan.shincolle.client.GameplayClientEffects;
import com.lulan.shincolle.config.ShinColleConfig;
import com.lulan.shincolle.crafting.LegacyItemMatchHelper;
import com.lulan.shincolle.entity.mount.LegacyMountEntity;
import com.lulan.shincolle.entity.mount.LegacyShipMountEntity;
import com.lulan.shincolle.entity.other.LegacyShipFishingHookEntity;
import com.lulan.shincolle.entity.projectile.LegacyShipProjectileEntity;
import com.lulan.shincolle.entity.projectile.LegacyShipBeamEntity;
import com.lulan.shincolle.entity.projectile.LegacyShipProjectileMoveType;
import com.lulan.shincolle.entity.projectile.LegacyShipProjectileProfile;
import com.lulan.shincolle.entity.projectile.LegacyShipProjectileVisual;
import com.lulan.shincolle.entity.projectile.LegacyShipStaticEffectEntity;
import com.lulan.shincolle.entity.projectile.LegacyShipFloatingFortEntity;
import com.lulan.shincolle.network.CombatFxDispatcher;
import com.lulan.shincolle.network.CombatReactType;
import com.lulan.shincolle.network.GameplayParticleType;
import com.lulan.shincolle.menu.RecipePaperMenu;
import com.lulan.shincolle.registry.ModBlocks;
import com.lulan.shincolle.registry.ModItems;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.TransientCraftingContainer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TieredItem;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SmeltingRecipe;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.BossEvent;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.ForgeHooks;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Central dispatch point for ported 1.12.2 per-ship behavior.
 */
public final class LegacyShipBehaviorCatalog {

    private static final int LEGACY_EMOTION_NORMAL = 0;
    private static final int LEGACY_EMOTION_TT = 2;
    private static final int LEGACY_EMOTION_BORED = 4;
    private static final int LEGACY_EMOTION_HUNGRY = 5;
    private static final int LEGACY_HP_NORMAL = 0;
    private static final int LEGACY_HP_MINOR = 1;
    private static final int LEGACY_HP_MODERATE = 2;
    private static final int LEGACY_HP_HEAVY = 3;

    public static final int LEGACY_TASK_UPDATE_INTERVAL = 8;
    public static final int LEGACY_FISHING_BASE_TICKS = 400;
    public static final int LEGACY_FISHING_RANDOM_TICKS = 600;
    public static final int LEGACY_FISHING_TIMEOUT_TICKS = 1000;
    public static final int LEGACY_FISHING_WATER_DEPTH = 3;

    public enum CombatHookResult {
        NOT_HANDLED(false, false),
        HANDLED(true, false),
        HANDLED_WITH_RESOURCES(true, true);

        private final boolean handled;
        private final boolean resourcesConsumed;

        CombatHookResult(boolean handled, boolean resourcesConsumed) {
            this.handled = handled;
            this.resourcesConsumed = resourcesConsumed;
        }

        public boolean handled() {
            return handled;
        }

        public boolean resourcesConsumed() {
            return resourcesConsumed;
        }
    }

    public enum AiPriority {
        DEATH_STOP_SIT,
        EXPLICIT_COMMAND,
        ACTIVE_COMBAT_TARGET,
        AUTO_SUPPLY,
        PICKUP,
        IDLE_FOLLOW
    }

    public enum MarriagePassive {
        NONE,
        SELF_AND_OWNER_INVISIBILITY,
        ALLIED_JUMP_AURA,
        ALLIED_STRENGTH_AURA,
        ALLIED_RESISTANCE_FIRE_AURA,
        ALLIED_HEALTH_BOOST_AURA,
        ALLIED_SATURATION_AURA,
        ALLIED_LUCK_AURA,
        ALLIED_NIGHT_VISION_AURA,
        OWNER_HASTE,
        OWNER_NIGHT_VISION,
        OWNER_JUMP,
        OWNER_STRENGTH,
        OWNER_SPEED,
        OWNER_RESISTANCE,
        SHIMAKAZE_OWNER_SPEED
    }

    public enum CombatHook {
        NONE,
        SHIMAKAZE_TORPEDO_BURST,
        NAGATO_HEAVY_STRIKE,
        YAMATO_BEAM_BARRAGE,
        TENRYUU_DASH,
        TATSUTA_CONTROL_AOE,
        HEAVY_CRUISER_BARRAGE,
        KONGOU_COMBO,
        BATTLESHIP_RU_BARRAGE,
        BATTLESHIP_TA_RENSOUHOU,
        SUBMARINE_HIME_DOUBLE_MISSILE,
        CARRIER_HIME_MELEE_MISSILE,
        NORTHERN_HIME_FLOATING_FORT
    }

    public record HostileLootProfile(boolean dropsAbyssMetal,
                                     boolean dropsAbyssMetal1,
                                     boolean dropsBossBonus,
                                     float eggDropChance) {
    }

    /** Snapshot of the legacy AttackEffectMap entry carried by a launched attack. */
    public record AttackEffectProfile(MobEffect effect, int amplifier, int durationTicks, int chancePercent) {
        public static final AttackEffectProfile NONE = new AttackEffectProfile(null, 0, 0, 0);

        public boolean isPresent() {
            return this.effect != null && this.durationTicks > 0 && this.chancePercent > 0;
        }
    }

    /** Legacy light-cannon attacker feedback, including its source height and sound volume. */
    public record LegacyLightAttackFeedback(double standingSourceHeight,
                                             double mountedSourceHeight,
                                             float soundVolume) {
        public static final LegacyLightAttackFeedback NONE =
                new LegacyLightAttackFeedback(0.0D, 0.0D, 0.0F);

        public boolean isPresent() {
            return this.soundVolume > 0.0F;
        }

        public double sourceHeight(boolean mounted) {
            return mounted ? this.mountedSourceHeight : this.standingSourceHeight;
        }
    }

    /** Legacy light-cannon particle family and its source-specific parameters. */
    public record LegacyLightAttackParticleFeedback(GameplayParticleType particleType,
                                                     double standingSourceHeight,
                                                     double mountedSourceHeight,
                                                     double parameter) {
        public static final LegacyLightAttackParticleFeedback NONE =
                new LegacyLightAttackParticleFeedback(GameplayParticleType.NONE, 0.0D, 0.0D, 0.0D);

        public boolean isPresent() {
            return this.particleType != GameplayParticleType.NONE;
        }

        public double sourceHeight(boolean mounted) {
            return mounted ? this.mountedSourceHeight : this.standingSourceHeight;
        }
    }

    /** Legacy type-5 custom large-smoke parameters used by hostile Kongou-class ships. */
    public record LegacyCannonAttackFeedback(double width, double length, double height) {
        public static final LegacyCannonAttackFeedback NONE =
                new LegacyCannonAttackFeedback(0.0D, 0.0D, 0.0D);

        public boolean isPresent() {
            return this.width > 0.0D && this.length > 0.0D && this.height > 0.0D;
        }
    }

    /** Legacy type-43 point before the client particle is spawned. */
    public record EngineSmokePoint(float xOffset, float yOffset, float particleScale) {
    }

    public enum EngineVisualKind {
        SMOKE_CHIMNEY_L,
        YAMATO_SPEED_BLUR
    }

    /** Keeps the legacy client-side trigger separate from the shared smoke point data. */
    public record EngineVisualPoint(float xOffset, float yOffset, float particleScale,
                                    EngineVisualKind kind, int requiredModelStateBit,
                                    boolean requiresActiveFriendlyState) {
    }

    public enum LegacyClientEffectKind {
        EMOTION,
        FLAME,
        SMOKE
    }

    /** Local point emitted by a legacy client-only effect before entity translation. */
    public record LegacyClientEffectPoint(float xOffset, float yOffset, float zOffset,
                                          float velocityX, float velocityY, float velocityZ,
                                          LegacyClientEffectKind kind, int legacyEmotionType) {
    }

    public record Behavior(int legacyClassId, boolean hostile, MarriagePassive marriagePassive, CombatHook combatHook) {
        public boolean hasMarriagePassive() {
            return this.marriagePassive != MarriagePassive.NONE;
        }

        public boolean hasCombatHook() {
            return this.combatHook != CombatHook.NONE;
        }
    }

    /** Exact seat and vehicle data carried by the audited legacy hime mounts. */
    public record ShipMountProfile(int style, float width, float height,
                                   double hostX, double hostY, double hostZ,
                                   double playerX, double playerY, double playerZ,
                                   boolean usesAircraft) {
    }

    private static final ShipMountProfile AIRFIELD_HIME_MOUNT = new ShipMountProfile(
            LegacyMountEntity.STYLE_AIRFIELD, 1.9F, 1.3F,
            0.59D, -0.25D, 0.0D, -0.85D, 1.0D, -1.12D, true);
    private static final ShipMountProfile BATTLESHIP_HIME_MOUNT = new ShipMountProfile(
            LegacyMountEntity.STYLE_BATTLESHIP, 1.9F, 3.1F,
            1.05D, 2.6D, 0.0D, 1.2D, 0.7D, -1.3D, false);
    private static final ShipMountProfile HARBOUR_HIME_MOUNT = new ShipMountProfile(
            LegacyMountEntity.STYLE_HARBOUR, 1.9F, 1.6F,
            0.0D, -0.29D, 0.0D, -1.5D, 1.06D, 0.44D, true);
    private static final ShipMountProfile ISOLATED_HIME_MOUNT = new ShipMountProfile(
            LegacyMountEntity.STYLE_ISOLATED, 1.6F, 2.2F,
            -0.32D, 1.0D, 0.0D, 0.61D, 0.28D, 0.0D, true);
    private static final ShipMountProfile MIDWAY_HIME_MOUNT = new ShipMountProfile(
            LegacyMountEntity.STYLE_MIDWAY, 2.5F, 2.9F,
            0.0D, 2.0D, 0.0D, -0.3D, 0.5D, 0.0D, true);
    private static final ShipMountProfile CARRIER_HIME_MOUNT = new ShipMountProfile(
            LegacyMountEntity.STYLE_CARRIER, 1.9F, 2.1F,
            0.0D, 1.12D, 0.0D, 0.14D, -0.39D, 0.0D, true);
    private static final ShipMountProfile CARRIER_WD_MOUNT = new ShipMountProfile(
            LegacyMountEntity.STYLE_CARRIER_WD, 1.9F, 2.1F,
            0.25D, 1.1D, 0.0D, 0.14D, -0.39D, 0.0D, true);
    private static final ShipMountProfile SUBMARINE_HIME_MOUNT = new ShipMountProfile(
            LegacyMountEntity.STYLE_SUBMARINE, 1.8F, 1.6F,
            -0.8D, 0.6D, 0.0D, 0.55D, 1.2D, 0.0D, false);

    private LegacyShipBehaviorCatalog() {
    }

    public static Behavior behaviorFor(ShipEntitySpec spec) {
        return behaviorFor(spec.legacyClassId(), spec.hostile());
    }

    public static Behavior behaviorFor(int legacyClassId, boolean hostile) {
        return new Behavior(
                legacyClassId,
                hostile,
                hostile ? MarriagePassive.NONE : marriagePassiveForFriendlyClassId(legacyClassId),
                entityCombatHookForClassId(legacyClassId));
    }

    public static @javax.annotation.Nullable ShipMountProfile shipMountProfileFor(
            @javax.annotation.Nullable ShipEntitySpec spec) {
        if (spec == null) {
            return null;
        }

        return switch (normalizeLegacyClassId(spec.legacyClassId())) {
            case 20 -> CARRIER_HIME_MOUNT;
            case 21 -> AIRFIELD_HIME_MOUNT;
            case 26 -> BATTLESHIP_HIME_MOUNT;
            case 28 -> HARBOUR_HIME_MOUNT;
            case 29 -> ISOLATED_HIME_MOUNT;
            case 30 -> MIDWAY_HIME_MOUNT;
            case 33 -> CARRIER_WD_MOUNT;
            case 44 -> SUBMARINE_HIME_MOUNT;
            default -> null;
        };
    }

    public static boolean hasLegacyShipMounts(@javax.annotation.Nullable ShipEntitySpec spec) {
        return shipMountProfileFor(spec) != null;
    }

    /** Mirrors BasicEntityShip.canSummonMounts: model-state bit 0 plus fuel. */
    public static boolean canSummonLegacyShipMounts(@javax.annotation.Nullable LegacyShipEntity ship) {
        return ship != null
                && hasLegacyShipMounts(ship.getSpec())
                && (ship.getModelState() & 1) != 0
                && !ship.hasNoFuel();
    }

    /**
     * Mirrors the two legacy Rensouhou spawn branches.  Battleship Ta uses
     * the inverse model-state bit from Shimakaze when selecting RensouhouS.
     */
    public static boolean rensouhouUsesLargeModel(@javax.annotation.Nullable LegacyShipEntity host) {
        if (host == null || host.isHostileVariant()) {
            return false;
        }

        boolean modelBitSet = (host.getModelState() & 1) != 0;
        return host.isBattleshipTaVariant() ? !modelBitSet : modelBitSet;
    }

    public static LegacyShipAttackProfile attackProfile(ShipEntitySpec spec) {
        return LegacyShipAttackProfile.resolve(spec);
    }

    /** Mirrors each legacy ship's getEquipType used by EquipCalc's slot filter. */
    public static int equipmentTypeFor(ShipEntitySpec spec) {
        if (spec == null) {
            return 1;
        }

        return switch (spec.textureStem()) {
            case "EntityCarrierWo", "EntityCarrierHime", "EntityCarrierWDemon",
                    "EntityCarrierKaga", "EntityCarrierAkagi" -> 3;
            case "EntityBattleshipRe", "EntityAirfieldHime", "EntityHarbourHime",
                    "EntityIsolatedHime", "EntityMidwayHime", "EntityNorthernHime" -> 2;
            default -> 1;
        };
    }

    /** Mirrors the hostile Mob constructors' initial model-state values. */
    public static int initialModelStateFor(LegacyShipEntity ship) {
        if (ship == null || !ship.isHostileVariant()) {
            return LegacyShipEntity.DEFAULT_MODEL_STATE;
        }

        return switch (normalizeLegacyClassId(ship.getShipClassId())) {
            case 37 -> 3; // EntityBattleshipNGTMob
            case 46 -> 15; // EntityBattleshipYMTMob
            case 47, 48 -> ship.getRandom().nextInt(128); // EntityCarrierKaga/AkagiMob
            case 58 -> ship.getRandom().nextInt(16); // EntityCAAtagoMob
            case 51 -> ship.getRandom().nextInt(16); // EntityDestroyerAkatsukiMob
            case 52 -> ship.getRandom().nextInt(32); // EntityDestroyerHibikiMob
            case 60 -> 7 + (ship.getRandom().nextBoolean() ? 8 : 0); // EntityBBKongouMob
            case 61, 62 -> 1 + (ship.getRandom().nextBoolean() ? 2 : 0); // Hiei/Haruna Mob
            case 63 -> 5 + (ship.getRandom().nextBoolean() ? 2 : 0); // EntityBBKirishimaMob
            case 53 -> ship.getRandom().nextInt(4); // EntityDestroyerIkazuchiMob
            case 54 -> ship.getRandom().nextInt(2); // EntityDestroyerInazumaMob
            case 56 -> ship.getRandom().nextInt(32); // EntityCLTenryuuMob
            case 57 -> ship.getRandom().nextInt(8); // EntityCLTatsutaMob
            case 36 -> ship.getRandom().nextInt(64); // EntityDestroyerShimakazeMob
            case 38, 39 -> ship.getRandom().nextInt(8); // U-511/Ro-500 Mob
            case 59 -> ship.getRandom().nextInt(16); // EntityCATakaoMob
            default -> LegacyShipEntity.DEFAULT_MODEL_STATE;
        };
    }

    /** Mirrors EntityAIShipRangeTarget's category order and attack-range box. */
    public static LivingEntity findLegacyRangeTarget(LegacyShipEntity ship) {
        return findLegacyRangeTarget(ship, ship.getHostileTargetSearchArea());
    }

    /** Mirrors EntityAIShipGuarding.findTarget's narrower moving-guard box. */
    public static LivingEntity findLegacyGuardTarget(LegacyShipEntity ship) {
        double range = Math.max(1.0D, (int) ship.getCompatAttackRange());
        return findLegacyGuardTarget(ship, range);
    }

    public static LivingEntity findLegacyGuardTarget(LegacyShipEntity ship, double range) {
        AABB guardArea = ship.getBoundingBox().inflate(range * 0.9D, range * 0.6D, range * 0.9D);
        return findLegacyRangeTarget(ship, guardArea);
    }

    private static LivingEntity findLegacyRangeTarget(LegacyShipEntity ship, AABB searchArea) {
        List<LivingEntity> nearby = ship.level().getEntitiesOfClass(
                LivingEntity.class,
                searchArea,
                candidate -> isSelectableLegacyRangeTarget(ship, candidate));

        if (ship.isHostileVariant()) {
            return chooseRangeTarget(ship, nearby, LegacyShipBehaviorCatalog::isHostileRangeTarget);
        }

        LivingEntity target = chooseRangeTarget(ship, nearby,
                candidate -> ship.isAiAntiAir()
                        && LegacyShipCombatHelper.isFlyingTarget(candidate)
                        && (!isPassiveFlyingAnimal(candidate) || isExplicitCustomTarget(ship, candidate)));
        if (target != null) {
            return target;
        }

        target = chooseRangeTarget(ship, nearby,
                candidate -> ship.isAiAntiSub() && isLegacyUnderseaTarget(candidate));
        if (target != null) {
            return target;
        }

        target = chooseRangeTarget(ship, nearby,
                candidate -> ship.isAiPvpFirst()
                        && candidate instanceof LegacyShipEntity other
                        && !isLegacyUnderseaTarget(other));
        if (target != null) {
            return target;
        }

        return chooseRangeTarget(ship, nearby, candidate -> {
            if (isExplicitCustomTarget(ship, candidate)) {
                return true;
            }
            if (!(candidate instanceof Enemy) || isPassiveFlyingAnimal(candidate)) {
                return false;
            }
            Player owner = ship.getOwnerPlayer();
            return owner == null || com.lulan.shincolle.teitoku.TeitokuHelper.isAutoTargetAllowed(owner, candidate);
        });
    }

    public static boolean isLegacyRangeTarget(LegacyShipEntity ship, LivingEntity target) {
        if (!isSelectableLegacyRangeTarget(ship, target)) {
            return false;
        }
        if (ship.isHostileVariant()) {
            return isHostileRangeTarget(target);
        }
        if ((ship.isAiAntiAir()
                && LegacyShipCombatHelper.isFlyingTarget(target)
                && (!isPassiveFlyingAnimal(target) || isExplicitCustomTarget(ship, target)))
                || (ship.isAiAntiSub() && isLegacyUnderseaTarget(target))
                || (ship.isAiPvpFirst() && target instanceof LegacyShipEntity other
                && !isLegacyUnderseaTarget(other))) {
            return true;
        }
        if (isExplicitCustomTarget(ship, target)) {
            return true;
        }
        if (!(target instanceof Enemy) || isPassiveFlyingAnimal(target)) {
            return false;
        }
        Player owner = ship.getOwnerPlayer();
        return owner == null || com.lulan.shincolle.teitoku.TeitokuHelper.isAutoTargetAllowed(owner, target);
    }

    /** Mirrors TargetHelper's flare/searchlight gate for invisible non-player targets. */
    public static boolean canTargetLegacyInvisibleEntity(LegacyShipEntity ship, LivingEntity target) {
        if (ship == null || target == null || !target.isInvisible()) {
            return true;
        }
        if (ship.isHostileVariant()) {
            return false;
        }
        ShipEquipmentBehaviorState equipment = ship.getEquipmentBehaviorState();
        return equipment.flareLevel() > 0 || equipment.searchlightLevel() > 0;
    }

    /** Modern LivingEntity equivalent of the legacy IShipInvisible target family. */
    public static boolean isLegacyUnderseaTarget(LivingEntity target) {
        return target instanceof LegacyShipEntity ship
                && ship.getSpec().archetype() == ShipArchetype.SUBMARINE;
    }

    private static boolean isSelectableLegacyRangeTarget(LegacyShipEntity ship, LivingEntity target) {
        if (target == null || target == ship || !target.isAlive()) {
            return false;
        }
        if (ship.isAiOnSightChase() && !ship.getSensing().hasLineOfSight(target)) {
            return false;
        }
        if (!ship.isHostileVariant() && isPassiveFlyingAnimal(target) && !isExplicitCustomTarget(ship, target)) {
            return false;
        }
        if (ship.canEngage(target)) {
            return true;
        }
        if (!ship.isHostileVariant()
                && !(target instanceof Player)
                && !(target instanceof LegacyShipEntity)
                && isExplicitCustomTarget(ship, target)
                && canTargetLegacyInvisibleEntity(ship, target)) {
            if (ship.level() instanceof ServerLevel serverLevel
                    && com.lulan.shincolle.combat.WorldCombatRulesSavedData.get(serverLevel)
                            .isUnattackable(com.lulan.shincolle.teitoku.TeitokuHelper.resolveTargetClass(target))) {
                return false;
            }
            return true;
        }
        return false;
    }

    private static boolean isPassiveFlyingAnimal(LivingEntity target) {
        if (target == null) {
            return false;
        }
        EntityType<?> type = target.getType();
        return type == EntityType.BAT
                || type == EntityType.BEE
                || type == EntityType.PARROT
                || type == EntityType.ALLAY;
    }

    private static boolean isExplicitCustomTarget(LegacyShipEntity ship, LivingEntity target) {
        Player owner = ship.getOwnerPlayer();
        if (owner == null || target == null) {
            return false;
        }
        String targetClass = com.lulan.shincolle.teitoku.TeitokuHelper.resolveTargetClass(target);
        return com.lulan.shincolle.teitoku.TeitokuHelper.get(owner)
                .map(data -> data.getTargetClassCount() > 0
                        && data.hasTargetClass(targetClass)
                        && com.lulan.shincolle.teitoku.TeitokuHelper.isAutoTargetAllowed(owner, target))
                .orElse(false);
    }

    private static boolean isHostileRangeTarget(LivingEntity target) {
        return target instanceof Player
                || target instanceof LegacyShipEntity ship && !ship.isHostileVariant();
    }

    /** Mirrors TargetHelper.SelectorForHostile for the modern LivingEntity target family. */
    private static boolean canUseLegacySkillReplacementTarget(LegacyShipEntity ship, LivingEntity target) {
        if (!ship.isHostileVariant()) {
            return ship.canEngage(target);
        }
        return isHostileRangeTarget(target) && ship.canEngageIgnoringHostileTargetRange(target);
    }

    private static LivingEntity chooseRangeTarget(LegacyShipEntity ship,
                                                  List<LivingEntity> nearby,
                                                  java.util.function.Predicate<LivingEntity> predicate) {
        List<LivingEntity> candidates = new ArrayList<>();
        for (LivingEntity candidate : nearby) {
            if (predicate.test(candidate)) {
                candidates.add(candidate);
            }
        }
        if (candidates.isEmpty()) {
            return null;
        }

        candidates.sort(Comparator.comparingDouble(ship::distanceToSqr));
        return candidates.size() > 2
                ? candidates.get(ship.getRandom().nextInt(3))
                : candidates.get(0);
    }

    /**
     * Returns the fixed 1.12.2 invisible level for entities that implemented
     * IShipInvisible. The modern entity uses one class for all ship variants,
     * so this keeps the old per-class values in the behavior catalog.
     */
    public static float invisibleLevelFor(ShipEntitySpec spec) {
        int normalizedClassId = normalizeLegacyClassId(spec.legacyClassId());
        return switch (normalizedClassId) {
            case 17, 18 -> 0.2F;
            case 19 -> 0.25F;
            case 38 -> 0.3F;
            case 39 -> 0.35F;
            case 44 -> 0.3F;
            case 72 -> 0.35F;
            default -> 0F;
        };
    }

    /** These seven submarine classes own their ring invisibility tick outside marriage state. */
    public static boolean usesPerShipRingInvisibility(ShipEntitySpec spec) {
        if (spec == null) {
            return false;
        }

        return switch (normalizeLegacyClassId(spec.legacyClassId())) {
            case 17, 18, 19, 38, 39, 44, 72 -> true;
            default -> false;
        };
    }

    /** Mirrors the legacy per-class depth at which a ship starts floating upward. */
    public static double floatingDepthFor(ShipEntitySpec spec) {
        return floatingDepthFor(spec, 0);
    }

    public static double floatingDepthFor(ShipEntitySpec spec, int hostileScaleLevel) {
        int normalizedClassId = normalizeLegacyClassId(spec.legacyClassId());
        if (normalizedClassId == 44 || normalizedClassId == 72) {
            return 1.0D;
        }

        if (spec.hostile()) {
            int scale = Mth.clamp(hostileScaleLevel, 0, 3);
            return switch (normalizedClassId) {
                case 38, 39 -> 1.1D + scale * 0.3D;
                default -> 0.3D + scale * 0.05D;
            };
        }

        return switch (normalizedClassId) {
            case 17, 18, 19 -> 1.0D;
            case 38, 39 -> 1.1D;
            default -> 0.32D;
        };
    }

    /** Mirrors friendly EntityDestroyerHibiki.calcShipAttributesAddRaw. */
    public static float rawDodgeBonusFor(int legacyClassId, boolean hostile, boolean night) {
        if (hostile) {
            return 0.0F;
        }

        return switch (normalizeLegacyClassId(legacyClassId)) {
            case 52 -> 0.3F;
            case 56, 57 -> night ? 0.15F : 0.0F;
            default -> 0.0F;
        };
    }

    /** Mirrors the light-cruiser night bonuses from the legacy classes. */
    public static float rawCriticalBonusFor(int legacyClassId, boolean hostile, boolean night) {
        int normalizedClassId = normalizeLegacyClassId(legacyClassId);
        // CA Hime is declared by the legacy class as BasicEntityShipSmall,
        // not as one of the separate hostile Mob classes, so its override
        // remains active for the hostile hime egg as well.
        if (hostile && normalizedClassId != 49) {
            return 0.0F;
        }

        return switch (normalizedClassId) {
            case 9 -> night ? 0.15F : 0.0F;
            case 10 -> night ? 0.3F : 0.0F;
            case 13 -> 0.05F;
            case 14 -> 0.1F;
            case 56, 57 -> night ? 0.15F : 0.0F;
            case 49 -> night ? 0.1F : 0.0F;
            default -> 0.0F;
        };
    }

    /** Mirrors the ordinary cruiser and battleship miss bonuses from the legacy raw attributes. */
    public static float rawMissBonusFor(int legacyClassId, boolean hostile, boolean night) {
        if (hostile) {
            return 0.0F;
        }

        return switch (normalizeLegacyClassId(legacyClassId)) {
            case 9 -> night ? 0.15F : 0.0F;
            case 14 -> 0.1F;
            default -> 0.0F;
        };
    }

    /** Mirrors the legacy double-hit additions for Ru and Re battleships. */
    public static float rawDoubleHitBonusFor(int legacyClassId, boolean hostile) {
        if (hostile) {
            return 0.0F;
        }

        return switch (normalizeLegacyClassId(legacyClassId)) {
            case 13 -> 0.05F;
            case 15 -> 0.1F;
            default -> 0.0F;
        };
    }

    /** Mirrors the legacy triple-hit additions for Ru, Re, and CA Hime. */
    public static float rawTripleHitBonusFor(int legacyClassId, boolean hostile, boolean night) {
        int normalizedClassId = normalizeLegacyClassId(legacyClassId);
        // See rawCriticalBonusFor: the old CA Hime class owns this override
        // even though its modern spawn spec is hostile.
        if (hostile && normalizedClassId != 49) {
            return 0.0F;
        }

        return switch (normalizedClassId) {
            case 13 -> 0.05F;
            case 15 -> 0.1F;
            case 49 -> night ? 0.2F : 0.0F;
            default -> 0.0F;
        };
    }

    /** Mirrors EntityCAAtago/EntityCATakao's attacker debuff on a successful hit. */
    public static boolean appliesAttackerSlowdown(int legacyClassId, boolean hostile) {
        int normalizedClassId = normalizeLegacyClassId(legacyClassId);
        return normalizedClassId == 58 || normalizedClassId == 59;
    }

    /** Mirrors the direct post-hit debuff in both the friendly and hostile CA classes. */
    public static AttackEffectProfile attackerReactionEffectFor(ShipEntitySpec spec,
                                                                 int level,
                                                                 int hostileScaleLevel) {
        int normalizedClassId = normalizeLegacyClassId(spec.legacyClassId());
        if (normalizedClassId != 58 && normalizedClassId != 59) {
            return AttackEffectProfile.NONE;
        }

        if (spec.hostile()) {
            int scale = Mth.clamp(hostileScaleLevel, 0, 3);
            return new AttackEffectProfile(MobEffects.MOVEMENT_SLOWDOWN,
                    scale / 3,
                    100 + scale * 50,
                    100);
        }

        int resolvedLevel = Math.max(1, level);
        return new AttackEffectProfile(MobEffects.MOVEMENT_SLOWDOWN,
                resolvedLevel / 100,
                100 + resolvedLevel,
                100);
    }

    /** Mirrors the explicit melee damage overrides in the legacy ship classes. */
    public static float meleeDamageMultiplierFor(int legacyClassId, boolean hostile) {
        return switch (normalizeLegacyClassId(legacyClassId)) {
            // These overrides belong to the friendly classes. Their separate
            // hostile Mob classes inherit BasicEntityShipHostile's 0.125x melee path.
            case 13 -> hostile ? 1.0F : 16.0F;
            case 37 -> hostile ? 1.0F : 24.0F;
            // Tenryuu/Tatsuta friendly classes use 2x melee damage, while their
            // hostile Mob classes explicitly return the full attackDamage value.
            case 56, 57 -> hostile ? 8.0F : 16.0F;
            // Legacy getAttackBaseDamage returns the full attackDamage for
            // these overrides, while the modern base melee path starts at
            // attackLight * 0.125F.
            case 20 -> 16.0F; // EntityCarrierHime: attackDamage * 2
            case 26 -> 24.0F; // EntityBattleshipHime: attackDamage * 3
            case 21, 27, 28, 29, 30, 31, 33, 72 -> 8.0F;
            case 53 -> hostile ? 1.0F : 8.0F;
            case 63 -> 12.0F; // EntityBBKirishima: attackDamage * 1.5
            default -> 1.0F;
        };
    }

    /** Mirrors EntityBattleshipHime/EntityHarbourHime/EntityNorthernHime heavy cannon damage overrides. */
    public static float heavyDamageMultiplierFor(int legacyClassId, boolean hostile) {
        int normalized = normalizeLegacyClassId(legacyClassId);
        return (normalized == 28 || normalized == 31) ? 0.75F : 1.0F;
    }

    /** Mirrors EntityDestroyerAkatsuki.calcShipAttributesAddEquip. */
    public static ShipEquipmentBehaviorState equipmentBehaviorFor(ShipEntitySpec spec,
                                                                   ShipEquipmentBehaviorState inventoryState,
                                                                   boolean married,
                                                                   boolean ringEffectEnabled) {
        if (spec.hostile()
                || normalizeLegacyClassId(spec.legacyClassId()) != 51
                || !married
                || !ringEffectEnabled) {
            return inventoryState;
        }

        return inventoryState.withSearchlightLevel(inventoryState.searchlightLevel() + 1);
    }

    /** Keeps the legacy per-class ammo multiplier instead of collapsing it to an archetype. */
    public static int ammoConsumptionFor(ShipEntitySpec spec) {
        return switch (normalizeLegacyClassId(spec.legacyClassId())) {
            case 0, 1, 2, 3, 16, 17, 18, 19, 27, 36, 38, 39, 44, 51, 52, 53, 54, 72 -> 1;
            case 9, 10, 49, 56, 57, 58, 59 -> 2;
            case 12, 20, 33, 47, 48, 50 -> 3;
            case 13, 14, 15, 21, 26, 28, 29, 30, 31, 37, 46, 60, 61, 62, 63 -> 4;
            default -> switch (spec.archetype()) {
                case DESTROYER, SUBMARINE, TRANSPORT -> 1;
                case CRUISER -> 2;
                case CARRIER -> 3;
                case BATTLESHIP, PRINCESS, INSTALLATION -> 4;
            };
        };
    }

    /**
     * Mirrors ConfigHandler.consumeGrudgeShipIdle for every actively
     * registered BasicEntityShip class. Old Hime classes inherit the common
     * base path; only the separate BasicEntityShipHostile Mob variants stay
     * outside it. Unknown or historical IDs keep the shared tick disabled.
     */
    public static int legacyIdleGrudgeConsumptionFor(ShipEntitySpec spec) {
        if (spec == null || spec.eggMeta() >= 2000) {
            return -1;
        }

        return switch (normalizeLegacyClassId(spec.legacyClassId())) {
            case 0, 1, 2, 3, 27, 36, 51, 52, 53, 54 -> 5;
            case 9, 10, 49, 58, 59 -> 8;
            case 12, 20, 33, 47, 48 -> 12;
            case 13, 14, 26, 37, 46, 60, 61, 62, 63 -> 15;
            case 15, 21, 28, 29, 30, 31 -> 14;
            case 16 -> 3;
            case 17, 18, 19, 38, 39, 44, 72 -> 4;
            case 56, 57 -> 7;
            default -> -1;
        };
    }

    /** Mirrors ConfigHandler.consumeGrudgeAction[ShipConsume.Move]. */
    public static int legacyMovementGrudgeCost() {
        return 3;
    }

    /** Mirrors BasicEntityShip's Transport Wa movement experience branch. */
    public static int legacyMovementExperienceFor(ShipEntitySpec spec,
                                                   int movementGrudge,
                                                   int tickCount) {
        if (spec == null || spec.eggMeta() >= 2000
                || normalizeLegacyClassId(spec.legacyClassId()) != 16
                || tickCount <= 200) {
            return 0;
        }

        return Math.max(0, (int) (Math.max(0, movementGrudge) * 0.2F));
    }

    /** Mirrors AttackHandler's direct ammo cost for each legacy attack channel. */
    public static int attackAmmoCostFor(ShipEntitySpec spec, LegacyShipAttackKind attackKind) {
        int baseCost = ammoConsumptionFor(spec);
        return switch (attackKind) {
            case AIR_LIGHT -> baseCost * 6;
            case AIR_HEAVY -> baseCost * 2;
            case MELEE, LIGHT, HEAVY -> baseCost;
        };
    }

    /** Mirrors CombatHelper.calcMissileMoveType for the standard submarine missile data. */
    public static LegacyShipProjectileMoveType legacySubmarineProjectileMoveTypeFor(
            ShipEntitySpec spec, double shipY, double liquidDepth, double targetY,
            LegacyShipProjectileMoveType configuredMoveType) {
        if (spec == null || configuredMoveType != LegacyShipProjectileMoveType.ARC
                || !usesLegacySubmarineMissileData(spec)) {
            return configuredMoveType;
        }

        if (liquidDepth > 2.0D) {
            return LegacyShipProjectileMoveType.DIRECT;
        }
        if (liquidDepth > 0.0D) {
            return targetY <= shipY || targetY - shipY < liquidDepth
                    ? LegacyShipProjectileMoveType.TORPEDO
                    : LegacyShipProjectileMoveType.ARC;
        }
        return LegacyShipProjectileMoveType.ARC;
    }

    private static boolean usesLegacySubmarineMissileData(ShipEntitySpec spec) {
        return switch (normalizeLegacyClassId(spec.legacyClassId())) {
            case 17, 18, 19, 38, 39, 44, 72 -> true;
            default -> false;
        };
    }

    /** Mirrors BasicEntityShipCV.calcShipAttributesAddRaw/AddEquip and the hostile CV override. */
    public static int aircraftLightCapacityFor(ShipEntitySpec spec, int level, int aircraftEquipmentCount) {
        if (!attackProfile(spec).hasAirAttack()) {
            return 0;
        }
        int normalizedClassId = normalizeLegacyClassId(spec.legacyClassId());
        if (spec.hostile() && (normalizedClassId == 47 || normalizedClassId == 48)) {
            return 10;
        }

        int resolvedLevel = Math.max(1, level);
        int capacity = 8 + resolvedLevel / 5 + Math.max(0, aircraftEquipmentCount) * 4;
        return capacity + switch (normalizedClassId) {
            case 12 -> Mth.floor(resolvedLevel * 0.25F);
            case 15 -> Mth.floor(resolvedLevel * 0.10F);
            case 47 -> Mth.floor(resolvedLevel * 0.40F);
            case 48 -> Mth.floor(resolvedLevel * 0.28F);
            default -> 0;
        };
    }

    /** Mirrors BasicEntityShipCV.calcShipAttributesAddRaw/AddEquip and the hostile CV override. */
    public static int aircraftHeavyCapacityFor(ShipEntitySpec spec, int level, int aircraftEquipmentCount) {
        if (!attackProfile(spec).hasAirAttack()) {
            return 0;
        }
        int normalizedClassId = normalizeLegacyClassId(spec.legacyClassId());
        if (spec.hostile() && (normalizedClassId == 47 || normalizedClassId == 48)) {
            return 10;
        }

        int resolvedLevel = Math.max(1, level);
        int capacity = 4 + resolvedLevel / 10 + Math.max(0, aircraftEquipmentCount) * 2;
        return capacity + switch (normalizedClassId) {
            case 12 -> Mth.floor(resolvedLevel * 0.15F);
            case 15 -> Mth.floor(resolvedLevel * 0.05F);
            case 47 -> Mth.floor(resolvedLevel * 0.20F);
            case 48 -> Mth.floor(resolvedLevel * 0.18F);
            default -> 0;
        };
    }

    /** Mirrors the launchHeight values set by the 1.12 carrier constructors. */
    public static float aircraftLaunchHeightFactorFor(ShipEntitySpec spec,
                                                       LegacyShipAttackKind attackKind) {
        int normalizedClassId = normalizeLegacyClassId(spec.legacyClassId());
        if (normalizedClassId == 47 || normalizedClassId == 48) {
            return 0.65F;
        }
        if (normalizedClassId == 21 || normalizedClassId == 28
                || normalizedClassId == 29 || normalizedClassId == 30) {
            return 0.7F;
        }
        if (normalizedClassId == 31) {
            return 1.0F;
        }
        if (normalizedClassId == 12) {
            return 0.9F;
        }
        if (normalizedClassId == 15) {
            return 0.8F;
        }
        if (normalizedClassId == 20) {
            return 0.9F;
        }
        if (normalizedClassId == 33) {
            return 1.2F;
        }

        LegacyShipProjectileProfile profile = attackProfile(spec).projectileProfile(attackKind);
        return profile.isPresent() ? profile.launchHeightFactor() : 0.6F;
    }

    /** Mirrors the friendly Kaga/Akagi applySoundAtAttacker air override. */
    public static boolean usesLegacyCarrierAircraftBowSound(ShipEntitySpec spec) {
        int normalizedClassId = normalizeLegacyClassId(spec.legacyClassId());
        return !spec.hostile() && (normalizedClassId == 47 || normalizedClassId == 48);
    }

    /** Mirrors the Kaga/Akagi getAttackAirplane override, including mob variants. */
    public static boolean usesLegacySpecialCarrierAirframe(@javax.annotation.Nullable ShipEntitySpec spec) {
        if (spec == null) {
            return false;
        }
        int normalizedClassId = normalizeLegacyClassId(spec.legacyClassId());
        return normalizedClassId == 47 || normalizedClassId == 48;
    }

    /** Mirrors EntityAirplaneZero/EntityAirplaneT's tick-six sound and silent trail override. */
    public static boolean aircraftVariantLaunchSoundAtTick(@javax.annotation.Nullable ShipEntitySpec spec,
                                                            int tickCount) {
        return tickCount == 6 && usesLegacySpecialCarrierAirframe(spec);
    }

    /** Mirrors the regular aircraft trail cadence and the Zero/T no-trail overrides. */
    public static boolean aircraftTrailParticleEnabledFor(@javax.annotation.Nullable ShipEntitySpec spec,
                                                          LegacyShipAttackKind attackKind,
                                                          int tickCount) {
        if (usesLegacySpecialCarrierAirframe(spec)) {
            return false;
        }
        return attackKind != LegacyShipAttackKind.AIR_HEAVY || (tickCount & 1) == 0;
    }

    /** Mirrors the legacy hostile BossInfo colors for the migrated ship classes. */
    public static BossEvent.BossBarColor hostileBossBarColorFor(ShipEntitySpec spec) {
        if (spec == null || !spec.hostile()) {
            return BossEvent.BossBarColor.RED;
        }

        return switch (normalizeLegacyClassId(spec.legacyClassId())) {
            case 36 -> BossEvent.BossBarColor.YELLOW;
            case 37 -> BossEvent.BossBarColor.WHITE;
            case 47 -> BossEvent.BossBarColor.BLUE;
            case 38 -> BossEvent.BossBarColor.BLUE;
            case 39 -> BossEvent.BossBarColor.YELLOW;
            case 46 -> BossEvent.BossBarColor.PINK;
            case 48, 50 -> BossEvent.BossBarColor.RED;
            case 51 -> BossEvent.BossBarColor.PURPLE;
            case 52 -> BossEvent.BossBarColor.WHITE;
            case 53, 54 -> BossEvent.BossBarColor.PINK;
            case 56, 57 -> BossEvent.BossBarColor.PURPLE;
            case 58, 59 -> BossEvent.BossBarColor.YELLOW;
            case 60, 61, 62, 63 -> BossEvent.BossBarColor.WHITE;
            default -> BossEvent.BossBarColor.RED;
        };
    }

    /** Mirrors EntityDestroyerShimakaze's friendly and hostile five-shot spread. */
    public static double shimakazeHeavyMissileSpreadFor(ShipEntitySpec spec, int hostileScaleLevel) {
        if (spec == null || !"EntityDestroyerShimakaze".equals(spec.textureStem())) {
            return 3.5D;
        }
        return spec.hostile() ? 3.0D + Mth.clamp(hostileScaleLevel, 0, 3) * 0.5D : 3.5D;
    }

    /** Mirrors the Shimakaze constructors' legacy hitbox sizes, including hostile scale variants. */
    public static EntityDimensions dimensionsFor(ShipEntitySpec spec, int hostileScaleLevel) {
        if (spec == null) {
            return ShipArchetype.DESTROYER.dimensions();
        }

        int normalizedClassId = normalizeLegacyClassId(spec.legacyClassId());
        EntityDimensions ordinaryDimensions = switch (normalizedClassId) {
            case 0, 1, 2 -> EntityDimensions.scalable(0.9F, 1.7F);
            case 3 -> EntityDimensions.scalable(0.9F, 1.9F);
            case 9 -> EntityDimensions.scalable(0.75F, 1.7F);
            case 10 -> EntityDimensions.scalable(0.6F, 1.3F);
            case 12 -> EntityDimensions.scalable(0.7F, 1.9F);
            case 13, 14 -> EntityDimensions.scalable(0.7F, 1.8F);
            case 15 -> EntityDimensions.scalable(0.6F, 1.55F);
            case 16 -> EntityDimensions.scalable(0.7F, 1.53F);
            case 17, 18, 19 -> EntityDimensions.scalable(0.6F, 1.8F);
            case 20, 21 -> EntityDimensions.scalable(0.7F, 1.9F);
            case 26 -> EntityDimensions.scalable(0.7F, 2.05F);
            case 27 -> EntityDimensions.scalable(0.6F, 1.55F);
            case 28 -> EntityDimensions.scalable(0.7F, 2.2F);
            case 29 -> EntityDimensions.scalable(0.6F, 1.6F);
            case 30 -> EntityDimensions.scalable(0.7F, 2.0F);
            case 31 -> EntityDimensions.scalable(0.5F, 0.9F);
            case 33 -> EntityDimensions.scalable(0.7F, 1.9F);
            case 44 -> EntityDimensions.scalable(0.7F, 1.85F);
            case 72 -> EntityDimensions.scalable(0.5F, 0.9F);
            case 49 -> EntityDimensions.scalable(0.7F, 1.2F);
            default -> null;
        };
        if (ordinaryDimensions != null) {
            return ordinaryDimensions;
        }

        if (normalizedClassId >= 51 && normalizedClassId <= 54) {
            if (!spec.hostile()) {
                return EntityDimensions.scalable(0.5F, 1.5F);
            }

            return switch (Mth.clamp(hostileScaleLevel, 0, 3)) {
                case 1 -> EntityDimensions.scalable(0.9F, 3.0F);
                case 2 -> EntityDimensions.scalable(1.3F, 4.5F);
                case 3 -> EntityDimensions.scalable(1.7F, 6.0F);
                default -> EntityDimensions.scalable(0.5F, 1.5F);
            };
        }

        if (normalizedClassId == 38 || normalizedClassId == 39) {
            if (!spec.hostile()) {
                return EntityDimensions.scalable(0.6F, 1.4F);
            }

            return switch (Mth.clamp(hostileScaleLevel, 0, 3)) {
                case 1 -> EntityDimensions.scalable(0.9F, 2.8F);
                case 2 -> EntityDimensions.scalable(1.2F, 4.2F);
                case 3 -> EntityDimensions.scalable(1.5F, 5.6F);
                default -> EntityDimensions.scalable(0.6F, 1.4F);
            };
        }

        if (normalizedClassId == 36) {
            if (!spec.hostile()) {
                return EntityDimensions.scalable(0.5F, 1.6F);
            }

            return switch (Mth.clamp(hostileScaleLevel, 0, 3)) {
                case 1 -> EntityDimensions.scalable(0.9F, 3.2F);
                case 2 -> EntityDimensions.scalable(1.3F, 4.8F);
                case 3 -> EntityDimensions.scalable(1.7F, 6.4F);
                default -> EntityDimensions.scalable(0.5F, 1.6F);
            };
        }

        if (normalizedClassId == 37) {
            if (!spec.hostile()) {
                return EntityDimensions.scalable(0.7F, 2.0F);
            }

            return switch (Mth.clamp(hostileScaleLevel, 0, 3)) {
                case 1 -> EntityDimensions.scalable(1.2F, 4.0F);
                case 2 -> EntityDimensions.scalable(1.7F, 6.0F);
                case 3 -> EntityDimensions.scalable(2.2F, 8.0F);
                default -> EntityDimensions.scalable(0.7F, 2.0F);
            };
        }

        if (normalizedClassId == 46) {
            if (!spec.hostile()) {
                return EntityDimensions.scalable(0.8F, 2.1F);
            }

            return switch (Mth.clamp(hostileScaleLevel, 0, 3)) {
                case 1 -> EntityDimensions.scalable(1.3F, 4.2F);
                case 2 -> EntityDimensions.scalable(1.8F, 6.3F);
                case 3 -> EntityDimensions.scalable(2.3F, 8.4F);
                default -> EntityDimensions.scalable(0.8F, 2.1F);
            };
        }

        if (normalizedClassId == 56 || normalizedClassId == 57) {
            if (!spec.hostile()) {
                return EntityDimensions.scalable(0.75F, 1.65F);
            }

            return switch (Mth.clamp(hostileScaleLevel, 0, 3)) {
                case 1 -> EntityDimensions.scalable(0.9F, 3.2F);
                case 2 -> EntityDimensions.scalable(1.3F, 4.8F);
                case 3 -> EntityDimensions.scalable(1.7F, 6.4F);
                default -> EntityDimensions.scalable(0.75F, 1.65F);
            };
        }

        if (normalizedClassId == 47 || normalizedClassId == 48) {
            if (!spec.hostile()) {
                return EntityDimensions.scalable(0.6F, 1.875F);
            }

            return switch (Mth.clamp(hostileScaleLevel, 0, 3)) {
                case 1 -> EntityDimensions.scalable(1.2F, 3.75F);
                case 2 -> EntityDimensions.scalable(1.8F, 5.625F);
                case 3 -> EntityDimensions.scalable(2.4F, 7.5F);
                default -> EntityDimensions.scalable(0.6F, 1.875F);
            };
        }

        if (normalizedClassId == 58 || normalizedClassId == 59) {
            if (!spec.hostile()) {
                return EntityDimensions.scalable(0.7F, 1.75F);
            }

            return switch (Mth.clamp(hostileScaleLevel, 0, 3)) {
                case 1 -> EntityDimensions.scalable(0.9F, 3.5F);
                case 2 -> EntityDimensions.scalable(1.3F, 5.25F);
                case 3 -> EntityDimensions.scalable(1.7F, 7.0F);
                default -> EntityDimensions.scalable(0.75F, 1.75F);
            };
        }

        if (normalizedClassId >= 60 && normalizedClassId <= 63) {
            if (!spec.hostile()) {
                return EntityDimensions.scalable(0.6F, 1.875F);
            }

            return switch (Mth.clamp(hostileScaleLevel, 0, 3)) {
                case 1 -> EntityDimensions.scalable(1.2F, 3.75F);
                case 2 -> EntityDimensions.scalable(1.8F, 5.625F);
                case 3 -> EntityDimensions.scalable(2.4F, 7.5F);
                default -> EntityDimensions.scalable(0.6F, 1.875F);
            };
        }

        return spec.archetype().dimensions();
    }

    /** Mirrors BasicEntityShip's default eye height and Shimakaze's fixed morph eye height. */
    public static float eyeHeightFor(LegacyShipEntity ship, EntityDimensions dimensions) {
        if (ship != null && !ship.isHostileVariant()) {
            int normalizedClassId = normalizeLegacyClassId(ship.getShipClassId());
            if (normalizedClassId == 52 || normalizedClassId == 53 || normalizedClassId == 54) {
                return 1.4F;
            }
            if (normalizedClassId == 36) {
                return 1.5F;
            }
        }
        return dimensions.height * 0.8F;
    }

    /** Mirrors BasicEntityShip's default and the audited per-ship mounted offsets. */
    public static double passengerRidingOffsetFor(LegacyShipEntity ship) {
        if (ship == null) {
            return 0.0D;
        }

        double height = ship.getBbHeight();
        int normalizedClassId = normalizeLegacyClassId(ship.getShipClassId());
        if (normalizedClassId == 0) {
            return ship.isOrderedToSit() ? height * 0.51D : height * 0.6D;
        }
        if (normalizedClassId == 1) {
            return ship.isOrderedToSit() ? height * 0.28D : height * 0.6D;
        }
        if (normalizedClassId == 2) {
            return ship.isOrderedToSit() ? height * 0.27D : height * 0.7D;
        }
        if (normalizedClassId == 3) {
            return ship.isOrderedToSit() ? height * 0.12D : height * 0.77D;
        }
        if (!ship.isHostileVariant() && (normalizedClassId == 51 || normalizedClassId == 52)) {
            if (ship.isOrderedToSit()) {
                return height * (ship.getEmotion() == 4 ? -0.07D : 0.26D);
            }
            return height * 0.64D;
        }
        if (!ship.isHostileVariant() && normalizedClassId == 53) {
            return ship.isOrderedToSit() ? height * 0.23D : height * 0.64D;
        }
        if (!ship.isHostileVariant() && normalizedClassId == 54) {
            if (ship.isOrderedToSit()) {
                return height * (ship.getEmotion() == 4 ? 0.23D : 0.44D);
            }
            return height * 0.64D;
        }
        if (ship.isHostileVariant() && (normalizedClassId == 53 || normalizedClassId == 54)) {
            return height * 0.47D;
        }
        if (normalizedClassId == 9) {
            return ship.isOrderedToSit()
                    ? height * (ship.getEmotion() == 4 ? 0.22D : 0.33D)
                    : height * 0.72D;
        }
        if (normalizedClassId == 10) {
            return ship.isOrderedToSit() ? 0.0D : height * 0.24D;
        }
        if (normalizedClassId == 12) {
            if ((ship.getModelState() & 1) != 0) {
                return ship.isOrderedToSit()
                        ? height * (ship.getEmotion() == 4 ? 0.2D : 0.43D)
                        : height * 1.21D;
            }
            return height * 0.68D;
        }
        if (normalizedClassId == 13) {
            if (!ship.isOrderedToSit()) {
                return height * 0.72D;
            }
            if ((ship.getModelState() & 1) != 0) {
                if (ship.getEmotion() == 4) {
                    return height * 0.51D;
                }
                if (ship.getEmotion4() == 4) {
                    return 0.0D;
                }
                return height * 0.55D;
            }
            return height * 0.45D;
        }
        if (normalizedClassId == 14) {
            return ship.isOrderedToSit()
                    ? (ship.getEmotion() == 4 ? 0.0D : height * 0.47D)
                    : height * 0.76D;
        }
        if (normalizedClassId == 15) {
            if (!ship.isOrderedToSit()) {
                return height * 0.55D;
            }
            return ship.getEmotion() == 4 ? height * 0.35D : 0.0D;
        }
        if (normalizedClassId == 16) {
            if (!ship.isOrderedToSit()) {
                return height * 0.64D;
            }
            return (ship.getModelState() & 2) != 0 ? height * 0.5D : 0.0D;
        }
        if (normalizedClassId == 17) {
            if (ship.isOrderedToSit()) {
                return height * (ship.getEmotion() == 4 ? 0.25D : 0.0D);
            }
            return height * 0.69D;
        }
        if (normalizedClassId == 18) {
            if ((ship.getModelState() & 1) != 0) {
                if (ship.isOrderedToSit()) {
                    return height * (ship.getEmotion() == 4 ? 0.55D : 0.42D);
                }
                return height * 0.58D;
            }
            if (ship.isOrderedToSit()) {
                return height * (ship.getEmotion() == 4 ? 0.48D : 0.0D);
            }
            return height * 0.69D;
        }
        if (normalizedClassId == 19) {
            if (ship.isOrderedToSit()) {
                return height * (ship.getEmotion() == 4 ? 0.48D : 0.0D);
            }
            return height * 0.69D;
        }
        if (!ship.isHostileVariant() && normalizedClassId == 38) {
            return ship.isOrderedToSit() ? 0.0D : height * 0.58D;
        }
        if (!ship.isHostileVariant() && normalizedClassId == 39) {
            if (ship.isOrderedToSit()) {
                return height * (ship.getEmotion() == 4 ? 0.05D : 0.14D);
            }
            return height * 0.6D;
        }
        if (normalizedClassId == 20) {
            if (ship.isOrderedToSit()) {
                return ship.getEmotion() == 4 ? 0.0D : height * 0.33D;
            }
            return height * 0.75D;
        }
        if (normalizedClassId == 21) {
            if (ship.isOrderedToSit()) {
                return height * (ship.getEmotion() == 4 ? 0.65D : 0.56D);
            }
            return height * 0.75D;
        }
        if (normalizedClassId == 26 || normalizedClassId == 27) {
            if (ship.isOrderedToSit()) {
                return ship.getEmotion() == 4 ? 0.0D : height * 0.62D;
            }
            return height * 0.76D;
        }
        if (normalizedClassId == 28 || normalizedClassId == 29 || normalizedClassId == 30) {
            if (ship.isOrderedToSit()) {
                return height * (ship.getEmotion() == 4 ? 0.2D : 0.52D);
            }
            return height * 0.76D;
        }
        if (normalizedClassId == 31) {
            if (ship.isOrderedToSit()) {
                return ship.getEmotion() == 4 ? 0.0D : height * 0.08D;
            }
            return height * 0.48D;
        }
        if (normalizedClassId == 33) {
            if (ship.isOrderedToSit()) {
                return height * (ship.getEmotion() == 4 ? 0.29D : 0.33D);
            }
            return height * 0.75D;
        }
        if (normalizedClassId == 44) {
            if (ship.isOrderedToSit()) {
                return ship.getEmotion() == 4 ? height * 0.25D : 0.0D;
            }
            return height * 0.69D;
        }
        if (normalizedClassId == 72) {
            if (ship.isOrderedToSit()) {
                return ship.getEmotion() == 4 ? 0.0D : height * 0.08D;
            }
            return height * 0.48D;
        }
        if (normalizedClassId == 49) {
            if (ship.isOrderedToSit()) {
                return ship.getEmotion() == 4 ? 0.0D : height * 0.62D;
            }
            return height * 0.76D;
        }
        if (!ship.isHostileVariant() && normalizedClassId == 36) {
            if (ship.isOrderedToSit()) {
                return height * (ship.getEmotion() == 4 ? -0.04D : 0.16D);
            }
            return height * 0.67D;
        }
        if (!ship.isHostileVariant() && normalizedClassId == 37) {
            if (ship.isOrderedToSit()) {
                if ((ship.getModelState() & 2) != 0) {
                    return height * 0.42D;
                }
                return ship.getEmotion() == 4 ? 0.0D : height * 0.35D;
            }
            return height * 0.75D;
        }
        if (!ship.isHostileVariant() && normalizedClassId == 46) {
            if (ship.isOrderedToSit()) {
                if ((ship.getModelState() & 1) != 0) {
                    return height * 0.5D;
                }
                return ship.getEmotion() == 4 ? height * 0.1D : height * 0.4D;
            }
            return height * 0.75D;
        }
        if (!ship.isHostileVariant() && normalizedClassId == 56) {
            if (ship.isOrderedToSit()) {
                return height * (ship.getEmotion() == 4 ? 0.2D : 0.3D);
            }
            return height * 0.7D;
        }
        if (!ship.isHostileVariant() && normalizedClassId == 57) {
            if (ship.isOrderedToSit()) {
                return height * (ship.getEmotion() == 4 ? 0.2D : 0.27D);
            }
            return height * 0.7D;
        }
        if (normalizedClassId == 47 || normalizedClassId == 48) {
            if (ship.isOrderedToSit()) {
                return height * (ship.getEmotion() == 4 ? 0.35D : 0.45D);
            }
            return height * 0.72D;
        }
        if (normalizedClassId == 58 || normalizedClassId == 59) {
            if (ship.isOrderedToSit()) {
                if ((ship.getModelState() & 1) != 0) {
                    return height * 0.42D;
                }
                return ship.getEmotion() == 4 ? 0.0D : height * 0.35D;
            }
            return height * 0.75D;
        }
        if (normalizedClassId >= 60 && normalizedClassId <= 63) {
            if (ship.isOrderedToSit()) {
                if ((ship.getModelState() & 2) != 0) {
                    return height * 0.42D;
                }
                return ship.getEmotion() == 4 ? 0.0D : height * 0.35D;
            }
            return height * 0.75D;
        }
        return height;
    }

    /** Mirrors the legacy getYOffset overrides used when a ship is a passenger. */
    public static double myRidingOffsetFor(LegacyShipEntity ship) {
        if (ship == null) {
            return 0.0D;
        }

        return switch (normalizeLegacyClassId(ship.getShipClassId())) {
            case 10 -> 0.3D;
            case 31, 72 -> 0.25D;
            default -> 0.0D;
        };
    }

    /**
     * Mirrors the custom six-destroyer updatePassenger formulas from the
     * legacy Akatsuki and Inazuma classes.
     */
    public static boolean positionLegacyRider(LegacyShipEntity host,
                                               Entity rider,
                                               Entity.MoveFunction moveFunction) {
        if (host == null || rider == null || rider.getVehicle() != host
                || !(rider instanceof LegacyShipEntity shipRider)) {
            return false;
        }

        int hostClassId = normalizeLegacyClassId(host.getShipClassId());
        int riderClassId = normalizeLegacyClassId(shipRider.getShipClassId());
        if (host.isHostileVariant() && shipRider.isHostileVariant()
                && hostClassId == 54 && riderClassId == 53) {
            moveFunction.accept(rider,
                    host.getX(),
                    host.getY() + host.getBbHeight() * 0.47D + rider.getMyRidingOffset(),
                    host.getZ());
            syncLegacyRiderRotation(host, rider);
            return true;
        }
        if (host.isHostileVariant() || shipRider.isHostileVariant()) {
            return false;
        }

        if (hostClassId == 51) {
            if (riderClassId == 54) {
                shipRider.setEmotion(host.getEmotion());
                moveLegacyRider(host, rider, moveFunction, -0.48D,
                        host.isOrderedToSit() ? -0.08D : 0.68D,
                        0.1D);
                syncLegacyRiderRotation(host, rider);
                return true;
            }
            if (riderClassId == 52) {
                moveLegacyRider(host, rider, moveFunction, -0.2D,
                        host.isOrderedToSit() ? 0.22D : 0.68D,
                        -0.45D);
                syncLegacyRiderRotation(host, rider);
                return true;
            }
            if (riderClassId == 53 && hasFriendlyPassengerOfClass(host, 54)) {
                LegacyShipEntity inazuma = firstFriendlyPassengerOfClass(host, 54);
                double emotionOffset = inazuma != null && inazuma.getEmotion() == 4 ? 0.5D : 0.6D;
                moveLegacyRider(host, rider, moveFunction, -0.68D,
                        (host.isOrderedToSit() ? -0.08D : 0.68D) + emotionOffset,
                        0.0D);
                syncLegacyRiderRotation(host, rider);
                return true;
            }
        } else if (hostClassId == 54 && riderClassId == 53) {
            moveLegacyRider(host, rider, moveFunction, -0.2D,
                    host.isOrderedToSit() ? 0.26D : 0.68D,
                    host.getEmotion() == 4 ? -0.6D : -0.45D);
            syncLegacyRiderRotation(host, rider);
            return true;
        }

        return false;
    }

    private static void moveLegacyRider(LegacyShipEntity host,
                                         Entity rider,
                                         Entity.MoveFunction moveFunction,
                                         double localZ,
                                         double yOffset,
                                         double yAdjustment) {
        double radians = host.getYRot() * (Math.PI / 180.0D);
        double cos = Math.cos(radians);
        double sin = Math.sin(radians);
        double rotatedZ = localZ * cos;
        double rotatedX = -localZ * sin;
        moveFunction.accept(rider,
                host.getX() + rotatedX,
                host.getY() + rider.getMyRidingOffset() + yOffset + yAdjustment,
                host.getZ() + rotatedZ);
    }

    private static void syncLegacyRiderRotation(LegacyShipEntity host, Entity rider) {
        rider.setYRot(host.getYRot());
        rider.yRotO = host.yRotO;
        if (rider instanceof LivingEntity living) {
            living.setYHeadRot(host.getYHeadRot());
            living.yHeadRotO = host.yHeadRotO;
            living.yBodyRot = host.yBodyRot;
            living.yBodyRotO = host.yBodyRotO;
        }
    }

    private static @javax.annotation.Nullable LegacyShipEntity firstFriendlyPassengerOfClass(
            LegacyShipEntity host, int classId) {
        for (Entity passenger : host.getPassengers()) {
            if (passenger instanceof LegacyShipEntity ship
                    && !ship.isHostileVariant()
                    && normalizeLegacyClassId(ship.getShipClassId()) == classId) {
                return ship;
            }
        }
        return null;
    }

    /** Mirrors MissileData.type=5 for married CA ships and scale-2+ CA abyssals. */
    public static boolean usesBlackHoleHeavyMissile(LegacyShipEntity ship,
                                                     LegacyShipAttackKind attackKind) {
        if (ship == null || attackKind != LegacyShipAttackKind.HEAVY) {
            return false;
        }

        int normalizedClassId = normalizeLegacyClassId(ship.getShipClassId());
        if (normalizedClassId == 29) {
            return true;
        }
        if (normalizedClassId != 58 && normalizedClassId != 59) {
            return false;
        }

        return ship.isHostileVariant()
                ? ship.getLegacyHostileScaleLevel() >= 2
                : ship.isMarried() && ship.isRingEffectEnabled();
    }

    /** Mirrors EntityBattleshipHime's missile data type 3 cluster payload. */
    public static boolean usesClusterHeavyMissile(LegacyShipEntity ship,
                                                   LegacyShipAttackKind attackKind) {
        return ship != null
                && attackKind == LegacyShipAttackKind.HEAVY
                && normalizeLegacyClassId(ship.getShipClassId()) == 26;
    }

    /** Spawns the legacy type-5 impact field at the exact missile impact position. */
    public static LegacyShipStaticEffectEntity spawnBlackHoleEffect(LegacyShipEntity ship,
                                                                      Vec3 impactPosition) {
        if (ship == null || impactPosition == null || ship.level().isClientSide()) {
            return null;
        }

        LegacyShipStaticEffectEntity effect = LegacyShipStaticEffectEntity.create(
                ship.level(), ship, impactPosition);
        if (effect != null) {
            ship.level().addFreshEntity(effect);
        }
        return effect;
    }

    /** BasicEntityShipHostileCV keeps Kaga/Akagi aircraft infinite via no-op setters. */
    public static boolean managesAircraftInventory(ShipEntitySpec spec) {
        int normalizedClassId = normalizeLegacyClassId(spec.legacyClassId());
        return !(spec.hostile() && (normalizedClassId == 47 || normalizedClassId == 48));
    }

    /** Mirrors ConfigHandler.CD_AirplaneRecovery's default and BasicEntityShipCV's speed scaling. */
    public static int aircraftRecoveryDelayFor(float attackSpeed) {
        int baseDelay = 3600;
        int scaledDelay = attackSpeed > 0.0F ? (int) (baseDelay / attackSpeed) : baseDelay;
        return Math.min(scaledDelay, baseDelay) + 20;
    }

    /** Mirrors the audited legacy ship AttackEffectMap initializers. */
    public static AttackEffectProfile attackEffectFor(ShipEntitySpec spec, int level, int hostileScaleLevel) {
        int normalizedClassId = normalizeLegacyClassId(spec.legacyClassId());
        int resolvedLevel = Math.max(1, level);
        if (spec.hostile()) {
            int scale = Mth.clamp(hostileScaleLevel, 0, 3);
            return switch (normalizedClassId) {
                case 26 -> new AttackEffectProfile(MobEffects.DIG_SLOWDOWN,
                        resolvedLevel / 70,
                        100 + resolvedLevel,
                        resolvedLevel);
                case 29 -> new AttackEffectProfile(MobEffects.BLINDNESS,
                        0,
                        100 + resolvedLevel,
                        resolvedLevel);
                case 15 -> new AttackEffectProfile(MobEffects.DIG_SLOWDOWN,
                        resolvedLevel / 100,
                        100 + resolvedLevel,
                        resolvedLevel);
                case 37 -> new AttackEffectProfile(MobEffects.POISON,
                        scale / 2,
                        60 + scale * 40,
                        25 + scale * 25);
                case 46 -> new AttackEffectProfile(MobEffects.DIG_SLOWDOWN,
                        (int) (scale / 1.5D),
                        100 + scale * 50,
                        25 + scale * 25);
                case 47, 48 -> new AttackEffectProfile(MobEffects.HUNGER,
                        scale / 3,
                        100 + scale * 50,
                        25 + scale * 25);
                case 58, 59 -> new AttackEffectProfile(MobEffects.MOVEMENT_SLOWDOWN,
                        scale / 2,
                        100 + scale * 50,
                        25 + scale * 25);
                case 60 -> new AttackEffectProfile(MobEffects.WEAKNESS,
                        (int) (scale / 1.5D),
                        80 + scale * 50,
                        25 + scale * 25);
                case 61 -> new AttackEffectProfile(MobEffects.POISON,
                        (int) (scale / 1.5D),
                        80 + scale * 50,
                        25 + scale * 25);
                case 62 -> new AttackEffectProfile(MobEffects.UNLUCK,
                        (int) (scale / 1.5D),
                        100 + scale * 50,
                        25 + scale * 25);
                case 63 -> new AttackEffectProfile(MobEffects.BLINDNESS,
                        (int) (scale / 1.5D),
                        40 + scale * 30,
                        25 + scale * 25);
                default -> AttackEffectProfile.NONE;
            };
        }

        return switch (normalizedClassId) {
            case 26 -> new AttackEffectProfile(MobEffects.DIG_SLOWDOWN,
                    resolvedLevel / 70,
                    100 + resolvedLevel,
                    resolvedLevel);
            case 29 -> new AttackEffectProfile(MobEffects.BLINDNESS,
                    0,
                    100 + resolvedLevel,
                    resolvedLevel);
            case 15 -> new AttackEffectProfile(MobEffects.DIG_SLOWDOWN,
                    resolvedLevel / 100,
                    100 + resolvedLevel,
                    resolvedLevel);
            case 37 -> new AttackEffectProfile(MobEffects.POISON,
                    resolvedLevel / 75,
                    60 + resolvedLevel,
                    resolvedLevel);
            case 46 -> new AttackEffectProfile(MobEffects.DIG_SLOWDOWN,
                    resolvedLevel / 70,
                    100 + resolvedLevel,
                    resolvedLevel);
            case 47, 48 -> new AttackEffectProfile(MobEffects.HUNGER,
                    resolvedLevel / 120,
                    100 + resolvedLevel,
                    resolvedLevel);
            case 58, 59 -> new AttackEffectProfile(MobEffects.MOVEMENT_SLOWDOWN,
                    resolvedLevel / 100,
                    100 + resolvedLevel,
                    resolvedLevel);
            case 60 -> new AttackEffectProfile(MobEffects.WEAKNESS,
                    resolvedLevel / 60,
                    100 + resolvedLevel,
                    resolvedLevel);
            case 61 -> new AttackEffectProfile(MobEffects.POISON,
                    resolvedLevel / 85,
                    80 + resolvedLevel,
                    resolvedLevel);
            case 62 -> new AttackEffectProfile(MobEffects.UNLUCK,
                    resolvedLevel / 95,
                    100 + resolvedLevel,
                    resolvedLevel);
            case 63 -> new AttackEffectProfile(MobEffects.BLINDNESS,
                    0,
                    50 + resolvedLevel,
                    resolvedLevel);
            default -> AttackEffectProfile.NONE;
        };
    }

    /** Applies the saved legacy effect only after a real hit, matching BuffHelper.applyBuffOnTarget. */
    public static void applyAttackEffect(LegacyShipEntity ship, LivingEntity target) {
        if (ship == null || target == null || target == ship || ship.isAlliedTo(target)) {
            return;
        }

        AttackEffectProfile profile = attackEffectFor(
                ship.getSpec(), ship.getShipLevel(), ship.getLegacyHostileScaleLevel());
        applyAttackEffectProfile(ship, target, profile);

        // EntityIsolatedHime adds poison to AttackEffectMap only while married
        // and the ring effect is enabled, alongside its unconditional blindness.
        if (normalizeLegacyClassId(ship.getShipClassId()) == 29
                && ship.isMarried() && ship.isRingEffectEnabled()) {
            applyAttackEffectProfile(ship, target, new AttackEffectProfile(
                    MobEffects.POISON, ship.getShipLevel() / 75,
                    80 + ship.getShipLevel(), ship.getShipLevel()));
        }
    }

    private static void applyAttackEffectProfile(LegacyShipEntity ship, LivingEntity target,
                                                 AttackEffectProfile profile) {
        if (profile == null || !profile.isPresent()
                || ship.getRandom().nextInt(100) >= Mth.clamp(profile.chancePercent(), 0, 100)) {
            return;
        }

        target.addEffect(new MobEffectInstance(
                profile.effect(), profile.durationTicks(), profile.amplifier(), false, true, true));
    }

    /** Mirrors EntityBattleshipRe.applyAttackPostMotion for a successful light hit. */
    public static int applyLegacyPostAttackMotion(LegacyShipEntity ship,
                                                  LegacyShipAttackKind attackKind,
                                                  LivingEntity primaryTarget,
                                                  float attackDamage) {
        if (ship == null || primaryTarget == null
                || normalizeLegacyClassId(ship.getShipClassId()) != 15
                || attackKind != LegacyShipAttackKind.LIGHT) {
            return 0;
        }

        int remaining = Mth.clamp((int) (ship.getShipLevel() * 0.05F), 0, 7);
        if (remaining <= 0) {
            return 0;
        }

        float rawDamage = Math.max(0.0F, attackDamage * 0.2F);
        int hitCount = 0;
        AABB impactBox = primaryTarget.getBoundingBox().inflate(3.5D, 3.5D, 3.5D);
        List<LivingEntity> candidates = ship.level().getEntitiesOfClass(
                LivingEntity.class,
                impactBox,
                candidate -> candidate != ship
                        && candidate != primaryTarget
                        && candidate.isAlive()
                        && !candidate.isInvulnerable()
                        && candidate.isPickable()
                        && !hasLegacySameOwner(ship, candidate));
        for (LivingEntity candidate : candidates) {
            if (remaining <= 0) {
                break;
            }

            remaining--;
            float damage = LegacyShipCombatHelper.applyTargetDamageModifier(
                    ship.getLegacyStats(), candidate, rawDamage);
            LegacyShipCombatHelper.AttackRoll roll = LegacyShipCombatHelper.rollDirectAttack(
                    ship, candidate, damage, 3.0F);
            boolean attacked = candidate.hurt(ship.damageSources().mobAttack(ship), roll.damage());
            if (!roll.miss() && attacked) {
                hitCount++;
            }
            CombatFxDispatcher.sendParticle(candidate, GameplayParticleType.BEAM_IMPACT,
                    candidate.getX(), candidate.getY() + candidate.getBbHeight() * 0.5D, candidate.getZ());
        }
        return hitCount;
    }

    /** Mirrors TeamHelper.checkSameOwner without widening it to the modern ally relation. */
    public static boolean hasLegacySameOwner(LegacyShipEntity ship, LivingEntity candidate) {
        if (ship.getOwnerUid() <= 0) {
            return ship.getOwnerUuid().isPresent()
                    && ((candidate instanceof Player player && ship.isOwnedBy(player))
                    || (candidate instanceof LegacyShipEntity otherShip
                    && !otherShip.isHostileVariant()
                    && ship.getOwnerUuid().equals(otherShip.getOwnerUuid())));
        }

        if (candidate instanceof Player player) {
            return ship.getOwnerUid() > 0
                    && ship.getOwnerUid() == com.lulan.shincolle.teitoku.TeitokuHelper.getPlayerUid(player);
        }
        return candidate instanceof LegacyShipEntity otherShip
                && !otherShip.isHostileVariant()
                && ship.getOwnerUid() > 0
                && ship.getOwnerUid() == otherShip.getOwnerUid();
    }

    /** Mirrors EntityHelper.isNotHost for beam and projectile collision candidates. */
    public static boolean isLegacyProjectileHostRelated(LegacyShipEntity host, Entity candidate) {
        if (host == null || candidate == null) {
            return true;
        }
        return candidate == host
                || candidate == host.getVehicle()
                || host.getPassengers().contains(candidate);
    }

    /** Mirrors TeamHelper.doFriendlyFire for projectiles, beams, and ship-vs-ship damage. */
    public static boolean isLegacyFriendlyFireAllowed(@javax.annotation.Nullable LegacyShipEntity attackerHost,
                                                      @javax.annotation.Nullable Entity target) {
        if (attackerHost == null || target == null) {
            return false;
        }
        if (target instanceof LivingEntity living && hasLegacySameOwner(attackerHost, living)) {
            return false;
        }
        if (attackerHost.isHostileVariant()
                && target instanceof LegacyShipEntity otherShip
                && otherShip.isHostileVariant()) {
            return false;
        }
        if (!ShinColleConfig.friendlyFire()) {
            if (!attackerHost.isHostileVariant() && target instanceof Player) {
                return false;
            }
            if (attackerHost.isAlliedTo(target)) {
                return false;
            }
        }
        return true;
    }

    /** Mirrors the legacy same-owner and hostile-team friendly-fire boundary. */
    public static boolean isLegacyProjectileDamageAllowed(LegacyShipEntity host, LivingEntity candidate) {
        return isLegacyFriendlyFireAllowed(host, candidate);
    }

    /** Mirrors 1.12.2 ShipCalc.getKaitaiItems per-class recycle drops. */
    public static List<ItemStack> createLegacyKaitaiDrops(ShipEntitySpec spec, boolean legacyRaiden, RandomSource random) {
        int classId = normalizeLegacyClassId(spec.legacyClassId());
        if (legacyRaiden
                || classId == 36 || classId == 38 || classId == 39
                || classId == 51 || classId == 52 || classId == 53 || classId == 54) {
            int base = ShinColleConfig.kaitaiAmountSmall();
            int extra = random.nextInt((int) (base * 0.25F) + 1);
            int count = base + extra;
            return List.of(
                    new ItemStack(ModItems.GRUDGE.get(), count),
                    new ItemStack(ModItems.ABYSSMETAL.get(), count),
                    new ItemStack(ModItems.AMMO.get(), count),
                    new ItemStack(ModItems.ABYSSMETAL1.get(), count));
        }

        return switch (classId) {
            case 0, 1, 2, 3, 9, 10, 16, 17, 18, 19 -> List.of(
                    new ItemStack(ModItems.GRUDGE.get(), 12 + random.nextInt(8)),
                    new ItemStack(ModItems.ABYSSMETAL.get(), 12 + random.nextInt(8)),
                    new ItemStack(ModItems.AMMO.get(), 12 + random.nextInt(8)),
                    new ItemStack(ModItems.ABYSSMETAL1.get(), 12 + random.nextInt(8)));
            case 12, 13, 14, 15, 20, 21, 26, 27, 28, 29, 30, 31, 33, 44, 49, 72 -> {
                if (ShinColleConfig.easyMode()) {
                    yield List.of(
                            new ItemStack(ModBlocks.BLOCK_GRUDGE.get(), 1),
                            new ItemStack(ModBlocks.BLOCK_ABYSSIUM.get(), 1),
                            new ItemStack(ModItems.AMMO1.get(), 1),
                            new ItemStack(ModBlocks.BLOCK_POLYMETAL.get(), 1));
                }
                yield List.of(
                        new ItemStack(ModBlocks.BLOCK_GRUDGE.get(), 10 + random.nextInt(3)),
                        new ItemStack(ModBlocks.BLOCK_ABYSSIUM.get(), 10 + random.nextInt(3)),
                        new ItemStack(ModItems.AMMO1.get(), 10 + random.nextInt(3)),
                        new ItemStack(ModBlocks.BLOCK_POLYMETAL.get(), 10 + random.nextInt(3)));
            }
            case 56, 57, 58, 59 -> {
                int base = ShinColleConfig.kaitaiAmountLarge();
                yield List.of(
                        new ItemStack(ModBlocks.BLOCK_GRUDGE.get(), base + random.nextInt((int) (base * 0.25F) + 1)),
                        new ItemStack(ModBlocks.BLOCK_ABYSSIUM.get(), base + random.nextInt((int) (base * 0.25F) + 1)),
                        new ItemStack(ModItems.AMMO1.get(), base + random.nextInt((int) (base * 0.25F) + 1)),
                        new ItemStack(ModBlocks.BLOCK_POLYMETAL.get(), base + random.nextInt((int) (base * 0.25F) + 1)));
            }
            case 37, 46, 47, 48, 60, 61, 62, 63 -> {
                int base = ShinColleConfig.kaitaiAmountLarge();
                yield List.of(
                        new ItemStack(ModBlocks.BLOCK_GRUDGE.get(), base + random.nextInt(base + 1)),
                        new ItemStack(ModBlocks.BLOCK_ABYSSIUM.get(), base + random.nextInt(base + 1)),
                        new ItemStack(ModItems.AMMO1.get(), base + random.nextInt(base + 1)),
                        new ItemStack(ModBlocks.BLOCK_POLYMETAL.get(), base + random.nextInt(base + 1)));
            }
            default -> List.of(
                    new ItemStack(ModItems.GRUDGE.get(), 1 + random.nextInt(3)),
                    new ItemStack(ModItems.ABYSSMETAL.get(), 1 + random.nextInt(2)));
        };
    }

    /** Mirrors EntitySubmHime's side offsets after CalcHelper.rotateXZByAxis. */
    public static List<Vec3> submarineHimeMissileLaunchOffsetsFor(float yawDegrees) {
        double radians = yawDegrees * (Math.PI / 180.0D);
        Vec3 firstSide = new Vec3(Math.cos(radians), 0.0D, Math.sin(radians));
        return List.of(firstSide, firstSide.scale(-1.0D));
    }

    /** Mirrors the old autonomous ranged-AI reserve check, including CV air-channel costs. */
    public static int minimumAmmoForGoal(ShipEntitySpec spec, LegacyShipAttackKind attackKind) {
        return switch (attackKind) {
            case AIR_LIGHT, AIR_HEAVY -> attackAmmoCostFor(spec, attackKind);
            case LIGHT, HEAVY -> attackAmmoCostFor(spec, attackKind) * 4;
            case MELEE -> 0;
        };
    }

    /** Mirrors the legacy classes that explicitly registered EntityAIShipPickItem. */
    public static boolean supportsItemPickup(ShipEntitySpec spec) {
        return switch (normalizeLegacyClassId(spec.legacyClassId())) {
            case 0, 1, 2, 3, 16, 17, 18, 19, 27, 36, 38, 39, 44, 51, 52, 53, 54, 72 -> true;
            default -> false;
        };
    }

    /** Returns the exact pickRangeBase passed by the legacy ship class. */
    public static int pickupRangeBaseFor(ShipEntitySpec spec) {
        return normalizeLegacyClassId(spec.legacyClassId()) == 16 ? 8 : 4;
    }

    /** Mirrors ConfigHandler.expGain[6] used by EntityAIShipPickItem. */
    public static int pickupExperience() {
        return 2;
    }

    /** Mirrors ShipStateHandler.calcExpNext with the legacy EXP_Modifier default. */
    public static int experienceToNext(int level) {
        return Math.max(1, (level + 1) * 20);
    }

    /** Mirrors TaskHelper.onUpdatePumping's level-based bitmask delay. */
    public static int autoPumpDelayForLevel(int level) {
        if (level >= 145) {
            return 3;
        }
        if (level >= 115) {
            return 7;
        }
        if (level >= 75) {
            return 15;
        }
        if (level >= 30) {
            return 31;
        }
        return 63;
    }

    /** Mirrors BasicEntityShip.onLivingUpdate's eight-tick TaskHelper dispatch cadence. */
    public static boolean isLegacyTaskUpdateTick(int tickCount) {
        return (tickCount & (LEGACY_TASK_UPDATE_INTERVAL - 1)) == 0;
    }

    /** Central task entry point, preserving TaskHelper's four task ids and eight-tick cadence. */
    public static void tickLegacyTask(LegacyShipEntity ship) {
        if (ship == null || ship.level().isClientSide() || ship.isHostileVariant()
                || !ship.isAlive() || ship.hasNoFuel()) {
            return;
        }

        switch (ship.getTask()) {
            case 1 -> tickLegacyCookingTask(ship);
            case 2 -> tickLegacyFishingTask(ship);
            case 3 -> tickLegacyMiningTask(ship);
            case 4 -> tickLegacyCraftingTask(ship);
            default -> { }
        }
    }

    /** Port of TaskHelper.onUpdateFishing, including its guard, water-depth, and hook gates. */
    private static void tickLegacyFishingTask(LegacyShipEntity ship) {
        ItemStack rod = ship.getLegacyTaskMainHandItem();
        if (!rod.is(Items.FISHING_ROD)) {
            return;
        }

        BlockPos guard = ship.getGuardedBlockPos();
        if (!isLegacyTaskGuard(ship, guard)) {
            return;
        }

        double guardX = guard.getX() + 0.5D;
        double guardZ = guard.getZ() + 0.5D;
        if (ship.distanceToSqr(guardX, guard.getY(), guardZ) > 10.0D) {
            ship.getNavigation().moveTo(guardX, guard.getY(), guardZ, 1.0D);
            return;
        }

        Vec3 motion = ship.getDeltaMovement();
        if (Math.abs(motion.x) > 0.1D || Math.abs(motion.z) > 0.1D || motion.y > 0.1D) {
            return;
        }

        BlockPos water = findLegacyFishingWater(ship);
        if (water == null) {
            return;
        }

        LegacyShipFishingHookEntity hook = ship.getFishingHook();
        if (hook == null || !hook.isAlive()) {
            ship.swing(InteractionHand.MAIN_HAND, true);
            hook = LegacyShipFishingHookEntity.create(ship.level(), ship);
            if (hook == null) {
                return;
            }

            hook.moveTo(water.getX() + 0.1D + ship.getRandom().nextDouble() * 0.8D,
                    water.getY() + 1.0D,
                    water.getZ() + 0.1D + ship.getRandom().nextDouble() * 0.8D,
                    0.0F, 0.0F);
            ship.level().addFreshEntity(hook);
            CombatFxDispatcher.sendLegacyEmotion(ship, switch (ship.getRandom().nextInt(4)) {
                case 1 -> 14;
                case 2 -> 7;
                case 3 -> 11;
                default -> 30;
            });
            return;
        }

        if (hook.tickCount > LEGACY_FISHING_BASE_TICKS
                + ship.getRandom().nextInt(LEGACY_FISHING_RANDOM_TICKS)) {
            generateLegacyFishingResult(ship, rod);
            hook.discard();
            ship.addShipExperience(20);
            ship.addGrudge(-30);
            ship.addMorale(300);
            CombatFxDispatcher.sendLegacyEmotion(ship, switch (ship.getRandom().nextInt(5)) {
                case 1 -> 1;
                case 2 -> 7;
                case 3 -> 16;
                case 4 -> 30;
                default -> 0;
            });
            ship.swing(InteractionHand.MAIN_HAND, true);
            return;
        }

        if (hook.isAlive() && hook.tickCount > LEGACY_FISHING_TIMEOUT_TICKS) {
            hook.discard();
        }
    }

    private record LegacyTaskFacility(BlockPos guard, BlockPos paired, Container container) {
    }

    private record LegacyMiningEntry(ItemStack result, int weight, int min, int max,
                                      int minimumShipLevel, int maximumY, int minimumToolLevel,
                                      float fortuneMultiplier) {
    }

    private static @javax.annotation.Nullable LegacyTaskFacility findLegacyTaskFacility(
            LegacyShipEntity ship, boolean furnaceRequired) {
        if (!(ship.level() instanceof ServerLevel serverLevel)) {
            return null;
        }

        BlockPos guard = ship.getGuardedBlockPos();
        if (!isLegacyTaskGuard(ship, guard)
                || !(serverLevel.getBlockEntity(guard) instanceof WaypointBlockEntity waypoint)) {
            return null;
        }

        BlockPos paired = waypoint.getPairedChest();
        if (paired == null || paired.getY() <= 0
                || ship.distanceToSqr(paired.getX(), paired.getY(), paired.getZ()) > 25.0D) {
            if (paired != null && paired.getY() > 0
                    && ship.distanceToSqr(paired.getX(), paired.getY(), paired.getZ()) > 25.0D) {
                ship.getNavigation().moveTo(guard.getX() + 0.5D, guard.getY(), guard.getZ() + 0.5D, 1.0D);
            }
            return null;
        }

        if (!(serverLevel.getBlockEntity(paired) instanceof Container container)
                || furnaceRequired && !(container instanceof AbstractFurnaceBlockEntity)) {
            return null;
        }

        return new LegacyTaskFacility(guard, paired, container);
    }

    private static boolean legacyTaskFlag(int taskSide, int bit) {
        return (taskSide & (1 << bit)) != 0;
    }

    private static int[] legacyFurnaceSlots(AbstractFurnaceBlockEntity furnace,
                                             ItemStack stack, int taskSide, int type) {
        Set<Integer> slots = new HashSet<>();
        int bitOffset = type * 6;

        for (int side = 0; side < 6; side++) {
            if (!legacyTaskFlag(taskSide, bitOffset + side)) {
                continue;
            }

            Direction direction = Direction.from3DDataValue(side);
            int[] faceSlots = furnace.getSlotsForFace(direction);
            for (int slot : faceSlots) {
                if (type == 1) {
                    // TaskHelper passes a null stack for output, so it accepts every configured face slot.
                    slots.add(slot);
                } else if (!stack.isEmpty()
                        && furnace.canPlaceItemThroughFace(slot, stack, direction)) {
                    slots.add(slot);
                }
            }
        }

        return slots.stream().mapToInt(Integer::intValue).sorted().toArray();
    }

    public static boolean matchesLegacyTaskItem(ItemStack target, ItemStack template,
                                                boolean checkMetadata, boolean checkOredict,
                                                boolean checkNbt) {
        return LegacyItemMatchHelper.matchesTargetItem(target, template, checkMetadata, checkOredict, checkNbt);
    }

    private static int findLegacyTaskCargoSlot(LegacyShipEntity ship, ItemStack template,
                                               boolean checkMetadata, boolean checkOredict,
                                               boolean checkNbt) {
        Container inventory = ship.getShipInventory();
        for (int slot = LegacyShipEntity.EQUIPMENT_SLOT_COUNT;
             slot < LegacyShipEntity.SHIP_SLOT_COUNT; slot++) {
            if (slot == LegacyShipEntity.LEGACY_MAIN_HAND_SLOT
                    || slot == LegacyShipEntity.LEGACY_OFF_HAND_SLOT) {
                continue;
            }

            if (matchesLegacyTaskItem(inventory.getItem(slot), template,
                    checkMetadata, checkOredict, checkNbt)) {
                return slot;
            }
        }
        return -1;
    }

    private static ItemStack takeOneLegacyTaskItem(Container source, ItemStack template,
                                                    boolean checkMetadata, boolean checkOredict,
                                                    boolean checkNbt) {
        for (int slot = 0; slot < source.getContainerSize(); slot++) {
            ItemStack current = source.getItem(slot);
            if (!matchesLegacyTaskItem(current, template, checkMetadata, checkOredict, checkNbt)) {
                continue;
            }

            ItemStack result = current.copy();
            result.setCount(1);
            current.shrink(1);
            source.setItem(slot, current.isEmpty() ? ItemStack.EMPTY : current);
            return result;
        }
        return ItemStack.EMPTY;
    }

    private static boolean moveLegacyTaskStackToFurnace(AbstractFurnaceBlockEntity furnace,
                                                        Container source, int sourceSlot,
                                                        int[] targetSlots) {
        if (sourceSlot < 0 || sourceSlot >= source.getContainerSize()) {
            return false;
        }

        ItemStack sourceStack = source.getItem(sourceSlot);
        if (sourceStack.isEmpty()) {
            return false;
        }

        boolean moved = false;
        for (int targetSlot : targetSlots) {
            if (sourceStack.isEmpty() || !furnace.canPlaceItem(targetSlot, sourceStack)) {
                continue;
            }

            ItemStack targetStack = furnace.getItem(targetSlot);
            if (!targetStack.isEmpty()
                    && !ItemStack.isSameItemSameTags(targetStack, sourceStack)) {
                continue;
            }

            int capacity = targetStack.isEmpty()
                    ? Math.min(sourceStack.getMaxStackSize(), furnace.getMaxStackSize())
                    : Math.min(targetStack.getMaxStackSize(), furnace.getMaxStackSize()) - targetStack.getCount();
            int amount = Math.min(sourceStack.getCount(), Math.max(0, capacity));
            if (amount <= 0) {
                continue;
            }

            if (targetStack.isEmpty()) {
                ItemStack inserted = sourceStack.copy();
                inserted.setCount(amount);
                furnace.setItem(targetSlot, inserted);
            } else {
                targetStack.grow(amount);
                furnace.setItem(targetSlot, targetStack);
            }
            sourceStack.shrink(amount);
            moved = true;
        }

        source.setItem(sourceSlot, sourceStack.isEmpty() ? ItemStack.EMPTY : sourceStack);
        return moved;
    }

    private static int insertLegacyTaskStack(Container target, ItemStack source,
                                             int startSlot, int endSlot) {
        if (source.isEmpty()) {
            return 0;
        }

        int moved = 0;
        int boundedStart = Math.max(0, startSlot);
        int boundedEnd = Math.min(target.getContainerSize(), endSlot);

        for (int slot = boundedStart; slot < boundedEnd && !source.isEmpty(); slot++) {
            ItemStack current = target.getItem(slot);
            if (current.isEmpty() || !ItemStack.isSameItemSameTags(current, source)
                    || !target.canPlaceItem(slot, source)
                    || current.getCount() >= Math.min(current.getMaxStackSize(), target.getMaxStackSize())) {
                continue;
            }

            int capacity = Math.min(current.getMaxStackSize(), target.getMaxStackSize()) - current.getCount();
            int amount = Math.min(source.getCount(), capacity);
            current.grow(amount);
            target.setItem(slot, current);
            source.shrink(amount);
            moved += amount;
        }

        for (int slot = boundedStart; slot < boundedEnd && !source.isEmpty(); slot++) {
            if (!target.getItem(slot).isEmpty() || !target.canPlaceItem(slot, source)) {
                continue;
            }

            int amount = Math.min(source.getCount(), Math.min(source.getMaxStackSize(), target.getMaxStackSize()));
            ItemStack inserted = source.copy();
            inserted.setCount(amount);
            target.setItem(slot, inserted);
            source.shrink(amount);
            moved += amount;
        }

        return moved;
    }

    private static int moveLegacyTaskStackToShip(LegacyShipEntity ship, ItemStack source) {
        return insertLegacyTaskStack(ship.getShipInventory(), source,
                LegacyShipEntity.EQUIPMENT_SLOT_COUNT, LegacyShipEntity.SHIP_SLOT_COUNT);
    }

    private static void dropLegacyTaskRemainder(LegacyShipEntity ship, ItemStack stack) {
        if (stack.isEmpty() || !(ship.level() instanceof ServerLevel serverLevel)) {
            return;
        }

        ItemEntity itemEntity = new ItemEntity(serverLevel, ship.getX(), ship.getY(), ship.getZ(), stack.copy());
        itemEntity.setDeltaMovement(serverLevel.random.nextGaussian() * 0.08D,
                serverLevel.random.nextGaussian() * 0.05D + 0.2D,
                serverLevel.random.nextGaussian() * 0.08D);
        serverLevel.addFreshEntity(itemEntity);
        stack.setCount(0);
    }

    private static TransientCraftingContainer createLegacyCraftingGrid(ItemStack[] inputs) {
        AbstractContainerMenu menu = new AbstractContainerMenu(MenuType.GENERIC_9x1, -1) {
            @Override
            public ItemStack quickMoveStack(Player player, int index) {
                return ItemStack.EMPTY;
            }

            @Override
            public boolean stillValid(Player player) {
                return true;
            }
        };
        TransientCraftingContainer grid = new TransientCraftingContainer(menu, 3, 3);
        for (int slot = 0; slot < RecipePaperMenu.CRAFT_SLOT_COUNT; slot++) {
            grid.setItem(slot, inputs[slot].copy());
        }
        return grid;
    }

    private static @javax.annotation.Nullable ItemStack[] readLegacyRecipePaper(ItemStack paper) {
        CompoundTag tag = paper.getTag();
        if (tag == null || !tag.contains(RecipePaperMenu.RECIPE_TAG, Tag.TAG_LIST)) {
            return null;
        }

        ItemStack[] inputs = new ItemStack[RecipePaperMenu.CRAFT_SLOT_COUNT];
        for (int slot = 0; slot < inputs.length; slot++) {
            inputs[slot] = ItemStack.EMPTY;
        }

        ListTag entries = tag.getList(RecipePaperMenu.RECIPE_TAG, Tag.TAG_COMPOUND);
        for (int index = 0; index < entries.size(); index++) {
            CompoundTag itemTag = entries.getCompound(index);
            int slot = itemTag.getInt(RecipePaperMenu.SLOT_KEY);
            if (slot >= 0 && slot < inputs.length) {
                inputs[slot] = ItemStack.of(itemTag);
            }
        }
        return inputs;
    }

    private static void tickLegacyCookingTask(LegacyShipEntity ship) {
        ItemStack mainStack = ship.getLegacyTaskMainHandItem();
        if (mainStack.isEmpty()) {
            return;
        }

        LegacyTaskFacility facility = findLegacyTaskFacility(ship, true);
        if (facility == null || !(facility.container() instanceof AbstractFurnaceBlockEntity furnace)) {
            return;
        }

        SimpleContainer input = new SimpleContainer(1);
        input.setItem(0, mainStack.copy());
        Optional<SmeltingRecipe> recipe = ship.level().getRecipeManager()
                .getRecipeFor(RecipeType.SMELTING, input, ship.level());
        if (recipe.isEmpty()) {
            return;
        }

        int taskSide = ship.getTaskSide();
        boolean checkMetadata = legacyTaskFlag(taskSide, 18);
        boolean checkOredict = legacyTaskFlag(taskSide, 19);
        boolean checkNbt = legacyTaskFlag(taskSide, 20);
        Container shipInventory = ship.getShipInventory();
        int targetSlot = findLegacyTaskCargoSlot(ship, mainStack,
                checkMetadata, checkOredict, checkNbt);
        ItemStack offStack = ship.getLegacyTaskOffHandItem();
        int fuelSlot = offStack.isEmpty() || ForgeHooks.getBurnTime(offStack, RecipeType.SMELTING) <= 0
                ? -1
                : findLegacyTaskCargoSlot(ship, offStack, checkMetadata, checkOredict, checkNbt);
        boolean swing = false;

        if (targetSlot >= 0 && moveLegacyTaskStackToFurnace(furnace, shipInventory, targetSlot,
                legacyFurnaceSlots(furnace, mainStack, taskSide, 0))) {
            swing = true;
        }
        if (fuelSlot >= 0 && moveLegacyTaskStackToFurnace(furnace, shipInventory, fuelSlot,
                legacyFurnaceSlots(furnace, offStack, taskSide, 2))) {
            swing = true;
        }

        ItemStack resultStack = recipe.get().getResultItem(ship.level().registryAccess());
        int[] outputSlots = legacyFurnaceSlots(furnace, resultStack, taskSide, 1);
        for (int outputSlot : outputSlots) {
            ItemStack output = furnace.getItem(outputSlot);
            if (!matchesLegacyTaskItem(output, resultStack, checkMetadata, checkOredict, checkNbt)) {
                continue;
            }

            int moved = moveLegacyTaskStackToShip(ship, output);
            furnace.setItem(outputSlot, output.isEmpty() ? ItemStack.EMPTY : output);
            if (moved <= 0) {
                break;
            }

            swing = true;
            ship.addShipExperience(2);
            ship.addGrudge(-3);
            ship.addMorale(100);
            float failChance = (150.0F - ship.getShipLevel()) / 150.0F * 0.2F + 0.05F;
            if (ship.getRandom().nextFloat() < failChance) {
                ItemStack charcoal = new ItemStack(Items.CHARCOAL);
                ItemEntity itemEntity = new ItemEntity(ship.level(),
                        facility.paired().getX() + 0.5D, facility.paired().getY() + 1.0D,
                        facility.paired().getZ() + 0.5D, charcoal);
                itemEntity.setDeltaMovement(ship.getRandom().nextGaussian() * 0.05D,
                        ship.getRandom().nextGaussian() * 0.05D + 0.2D,
                        ship.getRandom().nextGaussian() * 0.05D);
                ship.level().addFreshEntity(itemEntity);
                CombatFxDispatcher.sendLegacyEmotion(ship, 6);
            } else if (ship.getRandom().nextInt(7) == 0) {
                CombatFxDispatcher.sendLegacyEmotion(ship, switch (ship.getRandom().nextInt(5)) {
                    case 1 -> 1;
                    case 2 -> 7;
                    case 3 -> 16;
                    case 4 -> 30;
                    default -> 0;
                });
            }
            break;
        }

        if (swing) {
            ship.swing(InteractionHand.MAIN_HAND, true);
        }
    }

    private static void tickLegacyMiningTask(LegacyShipEntity ship) {
        ItemStack pickaxe = ship.getLegacyTaskMainHandItem();
        if (!pickaxe.is(ItemTags.PICKAXES)) {
            return;
        }

        Vec3 motion = ship.getDeltaMovement();
        if (Math.abs(motion.x) > 0.1D || Math.abs(motion.z) > 0.1D || motion.y > 0.1D) {
            return;
        }
        if ((ship.tickCount & 63) == 0) {
            ship.getNavigation().moveTo(ship.getX() + ship.getRandom().nextInt(9) - 4,
                    ship.getY() + ship.getRandom().nextInt(5) - 2,
                    ship.getZ() + ship.getRandom().nextInt(9) - 4, 1.0D);
            return;
        }

        if (ship.getRandom().nextInt(5) > 2) {
            ship.swing(InteractionHand.MAIN_HAND, true);
            if (ship.getRandom().nextInt(10) > 8) {
                CombatFxDispatcher.sendLegacyEmotion(ship, switch (ship.getRandom().nextInt(5)) {
                    case 2 -> 11;
                    case 3 -> 5;
                    case 4 -> 30;
                    default -> 0;
                });
            }
        }

        if ((ship.tickCount & 31) != 0
                || ship.tickCount - ship.getLegacyTaskTime()
                <= 100 + ship.getRandom().nextInt(200)) {
            return;
        }

        BlockPos origin = ship.blockPosition();
        int rockCount = 0;
        for (int dy = -3; dy < 5 && rockCount <= 120; dy++) {
            for (int dx = -3; dx < 4 && rockCount <= 120; dx++) {
                for (int dz = -3; dz < 4; dz++) {
                    if (isLegacyMiningRock(ship, origin.offset(dx, dy, dz))) {
                        rockCount++;
                    }
                }
            }
        }
        if (rockCount <= 120) {
            return;
        }

        generateLegacyMiningResult(ship, pickaxe);
        ship.addShipExperience(10);
        ship.addGrudge(-300);
        ship.addMorale(-200);
        CombatFxDispatcher.sendLegacyEmotion(ship, switch (ship.getRandom().nextInt(5)) {
            case 1 -> 11;
            case 2 -> 14;
            case 3 -> 4;
            case 4 -> 30;
            default -> 0;
        });
        ship.swing(InteractionHand.MAIN_HAND, true);
        ship.setLegacyTaskTime(ship.tickCount);
    }

    private static boolean isLegacyMiningRock(LegacyShipEntity ship, BlockPos pos) {
        var state = ship.level().getBlockState(pos);
        return state.is(BlockTags.MINEABLE_WITH_PICKAXE)
                || state.is(BlockTags.BASE_STONE_OVERWORLD)
                || state.is(BlockTags.BASE_STONE_NETHER)
                || state.is(BlockTags.STONE_ORE_REPLACEABLES)
                || state.is(BlockTags.DEEPSLATE_ORE_REPLACEABLES)
                || state.is(Blocks.COBBLESTONE)
                || state.is(Blocks.OBSIDIAN)
                || state.is(Blocks.END_STONE)
                || state.is(Blocks.NETHERRACK)
                || state.is(Blocks.NETHER_BRICKS)
                || state.is(Blocks.MAGMA_BLOCK);
    }

    private static List<LegacyMiningEntry> legacyMiningEntriesFor(LegacyShipEntity ship) {
        List<LegacyMiningEntry> entries = new ArrayList<>();
        entries.add(new LegacyMiningEntry(new ItemStack(Items.COBBLESTONE), 100, 1, 4, 1, 256, 0, 0.0F));

        if (ship.level().dimension() == net.minecraft.world.level.Level.NETHER) {
            entries.add(new LegacyMiningEntry(new ItemStack(Items.NETHERRACK), 4500, 1, 4, 1, 256, 0, 0.0F));
            entries.add(new LegacyMiningEntry(new ItemStack(Items.NETHER_BRICK), 1000, 1, 1, 1, 256, 0, 0.0F));
            entries.add(new LegacyMiningEntry(new ItemStack(Items.SOUL_SAND), 1000, 1, 1, 1, 256, 0, 0.0F));
            entries.add(new LegacyMiningEntry(new ItemStack(Items.GRAVEL), 1000, 1, 1, 1, 256, 0, 0.0F));
            entries.add(new LegacyMiningEntry(new ItemStack(Items.MAGMA_BLOCK), 500, 1, 1, 40, 256, 3, 0.0F));
            entries.add(new LegacyMiningEntry(new ItemStack(Items.FLINT), 500, 1, 1, 1, 256, 0, 0.0F));
            entries.add(new LegacyMiningEntry(new ItemStack(ModItems.MARRIAGERING.get()), 50, 1, 1, 1, 256, 3, 0.0F));
            entries.add(new LegacyMiningEntry(new ItemStack(Items.QUARTZ), 1000, 1, 3, 1, 256, 2, 1.50F));
            entries.add(new LegacyMiningEntry(new ItemStack(Items.GLOWSTONE_DUST), 500, 1, 2, 1, 256, 0, 1.0F));
            entries.add(new LegacyMiningEntry(new ItemStack(Items.GHAST_TEAR), 50, 1, 1, 90, 256, 3, 1.0F));
            entries.add(new LegacyMiningEntry(new ItemStack(Items.BLAZE_ROD), 80, 1, 1, 60, 256, 3, 1.0F));
        } else if (ship.level().dimension() == net.minecraft.world.level.Level.END) {
            entries.add(new LegacyMiningEntry(new ItemStack(Items.END_STONE), 4000, 1, 4, 1, 256, 0, 0.0F));
            entries.add(new LegacyMiningEntry(new ItemStack(Items.ENDER_PEARL), 200, 1, 1, 40, 256, 3, 1.0F));
            entries.add(new LegacyMiningEntry(new ItemStack(Items.CHORUS_FRUIT), 200, 1, 3, 60, 256, 3, 1.0F));
            entries.add(new LegacyMiningEntry(new ItemStack(ModItems.MARRIAGERING.get()), 25, 1, 1, 1, 256, 3, 0.0F));
        } else {
            entries.add(new LegacyMiningEntry(new ItemStack(Items.COBBLESTONE), 4000, 1, 4, 1, 256, 0, 0.0F));
            entries.add(new LegacyMiningEntry(new ItemStack(Items.GRANITE), 500, 1, 1, 1, 256, 0, 0.0F));
            entries.add(new LegacyMiningEntry(new ItemStack(Items.DIORITE), 500, 1, 1, 1, 256, 0, 0.0F));
            entries.add(new LegacyMiningEntry(new ItemStack(Items.ANDESITE), 500, 1, 1, 1, 256, 0, 0.0F));
            entries.add(new LegacyMiningEntry(new ItemStack(Items.DIRT), 500, 1, 1, 1, 256, 0, 0.0F));
            entries.add(new LegacyMiningEntry(new ItemStack(Items.SAND), 500, 1, 1, 1, 256, 0, 0.0F));
            entries.add(new LegacyMiningEntry(new ItemStack(Items.GRAVEL), 200, 1, 1, 1, 256, 0, 0.0F));
            entries.add(new LegacyMiningEntry(new ItemStack(Items.OBSIDIAN), 200, 1, 1, 1, 24, 3, 0.0F));
            entries.add(new LegacyMiningEntry(new ItemStack(Items.FLINT), 250, 1, 1, 1, 256, 0, 0.0F));
            entries.add(new LegacyMiningEntry(new ItemStack(Items.GUNPOWDER), 400, 1, 1, 40, 64, 0, 0.0F));
            entries.add(new LegacyMiningEntry(new ItemStack(Items.BONE), 400, 1, 1, 1, 64, 0, 0.0F));
            entries.add(new LegacyMiningEntry(new ItemStack(Items.COAL), 500, 1, 3, 1, 100, 1, 1.50F));
            entries.add(new LegacyMiningEntry(new ItemStack(Items.REDSTONE), 500, 1, 3, 20, 15, 2, 1.50F));
            entries.add(new LegacyMiningEntry(new ItemStack(Items.IRON_ORE), 350, 1, 2, 1, 64, 2, 1.0F));
            entries.add(new LegacyMiningEntry(new ItemStack(ModItems.ABYSSMETAL1.get()), 350, 1, 3, 1, 64, 2, 1.0F));
            entries.add(new LegacyMiningEntry(new ItemStack(Items.GOLD_ORE), 100, 1, 1, 30, 32, 2, 1.0F));
            entries.add(new LegacyMiningEntry(new ItemStack(Items.LAPIS_LAZULI), 200, 1, 3, 30, 30, 2, 1.50F));
            entries.add(new LegacyMiningEntry(new ItemStack(Items.DIAMOND), 50, 1, 1, 60, 16, 3, 1.0F));
            entries.add(new LegacyMiningEntry(new ItemStack(Items.EMERALD), 80, 1, 1, 40, 32, 3, 1.0F));
            entries.add(new LegacyMiningEntry(new ItemStack(ModItems.MARRIAGERING.get()), 25, 1, 1, 1, 16, 3, 0.0F));
        }
        return entries;
    }

    private static void generateLegacyMiningResult(LegacyShipEntity ship, ItemStack pickaxe) {
        int toolLevel = pickaxe.getItem() instanceof TieredItem tiered
                ? tiered.getTier().getLevel() : 0;
        List<LegacyMiningEntry> eligible = new ArrayList<>();
        int shipLevel = ship.getShipLevel();
        int shipY = Mth.floor(ship.getY());
        for (LegacyMiningEntry entry : legacyMiningEntriesFor(ship)) {
            if (shipLevel >= entry.minimumShipLevel()
                    && shipY <= entry.maximumY()
                    && toolLevel >= entry.minimumToolLevel()
                    && entry.weight() > 0) {
                eligible.add(entry);
            }
        }
        if (eligible.isEmpty()) {
            return;
        }

        int totalWeight = eligible.stream().mapToInt(LegacyMiningEntry::weight).sum();
        int roll = ship.getRandom().nextInt(totalWeight);
        LegacyMiningEntry selected = eligible.get(eligible.size() - 1);
        int cumulative = 0;
        for (LegacyMiningEntry entry : eligible) {
            cumulative += entry.weight();
            if (roll <= cumulative) {
                selected = entry;
                break;
            }
        }

        int count = selected.min();
        if (selected.max() > selected.min()) {
            count += ship.getRandom().nextInt(selected.max() - selected.min() + 1);
        }
        int fortune = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.BLOCK_FORTUNE, pickaxe);
        int luck = ship.hasEffect(MobEffects.LUCK)
                ? ship.getEffect(MobEffects.LUCK).getAmplifier() + 1 : 0;
        if (selected.fortuneMultiplier() > 0.0F) {
            count = (int) (count * (1.0F + (fortune + luck) * selected.fortuneMultiplier()));
        }

        ItemStack result = selected.result().copy();
        result.setCount(Math.max(1, count));
        moveLegacyTaskStackToShip(ship, result);
        dropLegacyTaskRemainder(ship, result);
    }

    private static void tickLegacyCraftingTask(LegacyShipEntity ship) {
        ItemStack paper = ship.getLegacyTaskMainHandItem();
        if (!paper.is(ModItems.RECIPEPAPER.get())) {
            return;
        }

        LegacyTaskFacility facility = findLegacyTaskFacility(ship, false);
        ItemStack[] inputs = readLegacyRecipePaper(paper);
        if (facility == null || inputs == null) {
            return;
        }

        TransientCraftingContainer recipeGrid = createLegacyCraftingGrid(inputs);
        Optional<CraftingRecipe> recipe = ship.level().getRecipeManager()
                .getRecipeFor(RecipeType.CRAFTING, recipeGrid, ship.level());
        if (recipe.isEmpty()) {
            return;
        }

        ItemStack expectedResult = recipe.get().assemble(recipeGrid, ship.level().registryAccess());
        if (expectedResult.isEmpty()) {
            return;
        }

        int taskSide = ship.getTaskSide();
        boolean checkMetadata = legacyTaskFlag(taskSide, 18);
        boolean checkOredict = legacyTaskFlag(taskSide, 19);
        boolean checkNbt = legacyTaskFlag(taskSide, 20);
        int maxCraft = ship.getShipLevel() / 20 + 1;
        boolean craftedAny = false;
        Container shipInventory = ship.getShipInventory();

        for (int attempt = 0; attempt < maxCraft; attempt++) {
            TransientCraftingContainer temporaryGrid = createLegacyCraftingGrid(inputs);
            for (int slot = 0; slot < RecipePaperMenu.CRAFT_SLOT_COUNT; slot++) {
                int materialSlot = 12 + slot;
                ItemStack material = shipInventory.getItem(materialSlot);
                if (material.isEmpty() && !inputs[slot].isEmpty()) {
                    material = takeOneLegacyTaskItem(facility.container(), inputs[slot],
                            checkMetadata, checkOredict, checkNbt);
                    shipInventory.setItem(materialSlot, material);
                }
                temporaryGrid.setItem(slot, material.copy());
            }

            ItemStack result = recipe.get().assemble(temporaryGrid, ship.level().registryAccess());
            if (result.isEmpty() || !matchesLegacyTaskItem(result, expectedResult,
                    checkMetadata, checkOredict, checkNbt)) {
                break;
            }

            insertLegacyTaskStack(facility.container(), result, 0, facility.container().getContainerSize());
            dropLegacyTaskRemainder(ship, result);
            for (int slot = 0; slot < RecipePaperMenu.CRAFT_SLOT_COUNT; slot++) {
                int materialSlot = 12 + slot;
                ItemStack material = shipInventory.getItem(materialSlot);
                if (!material.isEmpty()) {
                    material.shrink(1);
                    shipInventory.setItem(materialSlot, material.isEmpty() ? ItemStack.EMPTY : material);
                }
            }

            for (ItemStack remaining : recipe.get().getRemainingItems(temporaryGrid)) {
                if (remaining.isEmpty()) {
                    continue;
                }
                insertLegacyTaskStack(facility.container(), remaining, 0, facility.container().getContainerSize());
                dropLegacyTaskRemainder(ship, remaining);
            }
            craftedAny = true;
        }

        if (craftedAny) {
            ship.addShipExperience(1);
            ship.addGrudge(-2);
            ship.addMorale(-10);
            ship.swing(InteractionHand.MAIN_HAND, true);
            if (ship.getRandom().nextInt(5) == 0) {
                CombatFxDispatcher.sendLegacyEmotion(ship, switch (ship.getRandom().nextInt(5)) {
                    case 1 -> 2;
                    case 2 -> 7;
                    case 3 -> 13;
                    case 4 -> 30;
                    default -> 21;
                });
            }
        }
    }

    private static boolean isLegacyTaskGuard(LegacyShipEntity ship, @javax.annotation.Nullable BlockPos guard) {
        if (guard == null || guard.getY() <= 0) {
            return false;
        }

        String guardDimension = ship.getGuardedBlockDimension();
        return guardDimension == null || guardDimension.isBlank()
                || guardDimension.equals(ship.level().dimension().location().toString());
    }

    /** Mirrors BlockHelper.getNearbyLiquid(..., true, 5, 3) for vanilla water sources. */
    private static @javax.annotation.Nullable BlockPos findLegacyFishingWater(LegacyShipEntity ship) {
        BlockPos origin = ship.blockPosition();
        for (int y = 1; y > -3; y--) {
            int maxTry = (int) (0.5F * 5 * 5);
            for (int attempt = 0; attempt < maxTry; attempt++) {
                int x = ship.getRandom().nextInt(6) * 2 - 5;
                int z = ship.getRandom().nextInt(6) * 2 - 5;
                BlockPos candidate = origin.offset(x, y, z);
                if (isLegacyFishingWater(ship, candidate)) {
                    return candidate;
                }
            }

            for (int x = -5; x <= 5; x++) {
                for (int z = -5; z <= 5; z++) {
                    if (x == 0 && z == 0) {
                        continue;
                    }
                    BlockPos candidate = origin.offset(x, y, z);
                    if (isLegacyFishingWater(ship, candidate)) {
                        return candidate;
                    }
                }
            }
        }
        return null;
    }

    private static boolean isLegacyFishingWater(LegacyShipEntity ship, BlockPos pos) {
        for (int depth = 0; depth < LEGACY_FISHING_WATER_DEPTH; depth++) {
            FluidState fluid = ship.level().getFluidState(pos.below(depth));
            if (!fluid.is(FluidTags.WATER) || !fluid.isSource()) {
                return false;
            }
        }
        return true;
    }

    /** Uses the modern fishing loot table with the legacy luck sources and inventory fallback. */
    private static void generateLegacyFishingResult(LegacyShipEntity ship, ItemStack rod) {
        if (!(ship.level() instanceof ServerLevel serverLevel)) {
            return;
        }

        float luck = Math.max(
                EnchantmentHelper.getItemEnchantmentLevel(Enchantments.FISHING_LUCK, rod),
                EnchantmentHelper.getItemEnchantmentLevel(
                        Enchantments.FISHING_LUCK, ship.getLegacyTaskOffHandItem()));
        MobEffectInstance luckEffect = ship.getEffect(MobEffects.LUCK);
        if (luckEffect != null) {
            luck += luckEffect.getAmplifier() + 1;
        }
        luck += ship.getShipLevel() / 150.0F * 1.5F;
        LootTable table = serverLevel.getServer().getLootData().getLootTable(BuiltInLootTables.FISHING);
        LootParams params = new LootParams.Builder(serverLevel)
                .withParameter(LootContextParams.ORIGIN, ship.position())
                .withParameter(LootContextParams.THIS_ENTITY, ship)
                .withParameter(LootContextParams.TOOL, rod)
                .withLuck(luck)
                .create(LootContextParamSets.FISHING);

        for (ItemStack result : table.getRandomItems(params)) {
            ItemStack remaining = result.copy();
            while (!remaining.isEmpty()) {
                if (!ship.storeSingleItem(remaining)) {
                    ship.spawnAtLocation(remaining.copy());
                    break;
                }
                remaining.shrink(1);
            }
        }
    }

    /** Mirrors EntityHelper.getMoraleLevel's legacy IDs and strict boundaries. */
    public static int legacyMoraleIdFor(int morale) {
        if (morale > LegacyShipStatTables.MORALE_EXCITED_LOWER) {
            return 0;
        }
        if (morale > LegacyShipStatTables.MORALE_HAPPY_LOWER) {
            return 1;
        }
        if (morale > LegacyShipStatTables.MORALE_NORMAL_LOWER) {
            return 2;
        }
        if (morale > LegacyShipStatTables.MORALE_TIRED_LOWER) {
            return 3;
        }
        return 4;
    }

    /** Mirrors each legacy friendly class's ShipStateHandler FoodMax override. */
    public static int foodSaturationMaxFor(ShipEntitySpec spec) {
        return switch (normalizeLegacyClassId(spec.legacyClassId())) {
            case 9, 10 -> 14;
            case 12 -> 18;
            case 20 -> 24;
            case 21 -> 16;
            case 26 -> 30;
            case 28 -> 22;
            case 29 -> 18;
            case 30 -> 35;
            case 31 -> 16;
            case 33 -> 30;
            case 37 -> 20;
            case 46, 47 -> 30;
            case 48 -> 35;
            case 49 -> 17;
            case 56, 57 -> 12;
            case 58, 59 -> 16;
            case 60, 61, 62, 63 -> 19;
            default -> 10;
        };
    }

    /** Maps the vanilla 1.12 dimension ids used by the persisted GuardDim field. */
    public static String modernDimensionIdForLegacy(int dimensionId) {
        return switch (dimensionId) {
            case -1 -> "minecraft:the_nether";
            case 0 -> "minecraft:overworld";
            case 1 -> "minecraft:the_end";
            default -> "";
        };
    }

    public static int moraleCostFor(LegacyShipAttackKind attackKind) {
        return switch (attackKind) {
            case MELEE -> 2;
            case LIGHT -> 4;
            case HEAVY -> 6;
            case AIR_LIGHT -> 6;
            case AIR_HEAVY -> 8;
        };
    }

    /** Mirrors the pre-damage dismount overrides in Northern Hime and SSNH. */
    public static boolean dismountsBeforeDamage(ShipEntitySpec spec) {
        if (spec == null) {
            return false;
        }

        int normalizedClassId = normalizeLegacyClassId(spec.legacyClassId());
        return normalizedClassId == 31 || normalizedClassId == 72;
    }

    /** Mirrors the six-destroyer post-damage dismount overrides. */
    public static boolean dismountsRidersAfterDamage(ShipEntitySpec spec) {
        if (spec == null) {
            return false;
        }

        int normalizedClassId = normalizeLegacyClassId(spec.legacyClassId());
        return normalizedClassId >= 51 && normalizedClassId <= 54;
    }

    /**
     * Clears the six-destroyer formation through the same catalog-owned path as
     * gattai.  The 1.12 rider overrides cancel Akatsuki gattai from either the
     * host or a mounted Hibiki/Ikazuchi/Inazuma, and cancel Raiden from either
     * side of the Ikazuchi/Inazuma pair.
     */
    public static void dismountRidersAfterDamage(LegacyShipEntity ship) {
        if (ship == null || ship.isHostileVariant()
                || !dismountsRidersAfterDamage(ship.getSpec())) {
            return;
        }

        int normalizedClassId = normalizeLegacyClassId(ship.getShipClassId());
        boolean wasRaiden = ship.isLegacyRaiden();
        LegacyShipEntity sixDestroyerHost = normalizedClassId == 51
                ? ship : friendlyVehicleOfClass(ship, 51);
        if (sixDestroyerHost != null) {
            dismountSixDestroyerRiders(sixDestroyerHost);
        }

        if (wasRaiden && (normalizedClassId == 53 || normalizedClassId == 54)) {
            dismountRaidenPair(ship);
        }
    }

    /** Mirrors EntityBattleshipNGT's fixed blast radius and hostile scale extension. */
    public static double nagatoBlastRadiusFor(ShipEntitySpec spec, int hostileScaleLevel) {
        if (spec != null && spec.hostile()) {
            return 3.5D + Mth.clamp(hostileScaleLevel, 0, 3) * 0.5D;
        }
        return 3.5D;
    }

    /** Mirrors Northern Hime and SSNH's client ride request while sitting bored. */
    public static boolean handleBoredPlayerRiding(LegacyShipEntity ship,
                                                   Player player,
                                                   InteractionHand hand,
                                                   ItemStack stack) {
        if (ship == null || player == null || hand != InteractionHand.MAIN_HAND
                || !ship.isAlive() || !ship.isOrderedToSit() || ship.getEmotion() != 4
                || !supportsBoredPlayerRiding(ship.getSpec())
                || (stack != null && stack.is(ModItems.POINTERITEM.get()))) {
            return false;
        }

        if (!ship.level().isClientSide() && ship.isOwnedBy(player)) {
            ship.setOrderedToSit(false);
            ship.startRiding(player, true);
            ship.getNavigation().stop();
        }
        return true;
    }

    private static boolean supportsBoredPlayerRiding(ShipEntitySpec spec) {
        if (spec == null) {
            return false;
        }
        int normalizedClassId = normalizeLegacyClassId(spec.legacyClassId());
        return normalizedClassId == 31 || normalizedClassId == 72;
    }

    static int normalizeLegacyClassId(int legacyClassId) {
        return legacyClassId >= 2000 ? legacyClassId - 2000 : legacyClassId;
    }

    /** Mirrors the old ID.Morale.L_Excited * 1.5F gattai threshold (5100 * 1.5). */
    private static final int GATTAI_MORALE_THRESHOLD = 7650;

    /**
     * Keeps the destroyer rider state in the catalog so the modern entity does
     * not need a separate class for each 1.12 rider implementation.
     */
    public static void tickLegacyRiderBehavior(LegacyShipEntity ship) {
        if (ship == null || ship.level().isClientSide() || !ship.isAlive()
                || ((ship.tickCount & 31) != 0 && !ship.hasNoFuel())) {
            return;
        }

        switch (normalizeLegacyClassId(ship.getShipClassId())) {
            case 51 -> tickAkatsukiGattai(ship);
            case 52 -> tickHibikiGattaiState(ship);
            case 53 -> {
                if (ship.isHostileVariant()) {
                    tickHostileRaidenBehavior(ship);
                } else {
                    tickIkazuchiGattaiState(ship);
                }
            }
            case 54 -> {
                if (ship.isHostileVariant()) {
                    tickHostileRaidenBehavior(ship);
                } else {
                    tickInazumaGattaiState(ship);
                }
            }
            default -> {
            }
        }
    }

    /** Mirrors BasicEntityShip's 16/128-tick mount cancellation and summon gates. */
    public static void tickLegacyShipMountBehavior(LegacyShipEntity ship) {
        if (ship == null || ship.level().isClientSide() || !ship.isAlive()
                || !hasLegacyShipMounts(ship.getSpec())) {
            return;
        }

        if (!canSummonLegacyShipMounts(ship)) {
            if ((ship.tickCount & 15) == 0
                    && ship.getVehicle() instanceof LegacyShipMountEntity mount) {
                ship.stopRiding();
                mount.discard();
            }
            return;
        }

        if (ship.getVehicle() instanceof LegacyShipMountEntity) {
            return;
        }
        if (ship.isPassenger() || (ship.tickCount & 127) != 0) {
            return;
        }

        LegacyShipMountEntity mount = LegacyShipMountEntity.create(ship.level(), ship);
        if (mount == null) {
            return;
        }

        ship.level().addFreshEntity(mount);
        if (!ship.startRiding(mount, true)) {
            mount.discard();
        }
    }

    /** Mirrors the per-class client-side engine branches from the legacy entities. */
    public static void tickLegacyClientBehavior(LegacyShipEntity ship) {
        if (ship == null || !ship.level().isClientSide() || !ship.isAlive()
                || (ship.tickCount & 3) != 0) {
            return;
        }

        int normalizedClassId = normalizeLegacyClassId(ship.getShipClassId());
        if (normalizedClassId == 37) {
            tickBattleshipNagatoClientBehavior(ship);
        }
        if (normalizedClassId == 46) {
            tickBattleshipYamatoClientBehavior(ship);
        }
        if (!ship.isHostileVariant()) {
            switch (normalizedClassId) {
                case 12 -> tickCarrierWoClientBehavior(ship);
                case 13 -> tickBattleshipRuClientBehavior(ship);
                case 16 -> tickTransportWaClientBehavior(ship);
                case 18 -> tickSubmarineYoClientBehavior(ship);
                case 48 -> tickCarrierAkagiClientBehavior(ship);
                default -> {
                }
            }
        }

        double radians = (ship.yBodyRot % 360.0F) * (Math.PI / 180.0D);
        for (EngineVisualPoint point : legacyEngineVisualPointsFor(
                ship.getSpec(), ship.getLegacyHostileScaleLevel())) {
            if (point.requiredModelStateBit() >= 0
                    && (ship.getModelState() & (1 << point.requiredModelStateBit())) == 0) {
                continue;
            }
            if (point.requiresActiveFriendlyState()
                    && (ship.isOrderedToSit() || ship.hasNoFuel())) {
                continue;
            }

            if (!ship.isHostileVariant()
                    && !legacyEngineRiderStateAllowsSmoke(ship, normalizedClassId)) {
                continue;
            }

            float xOffset = point.xOffset();
            if (!ship.isHostileVariant() && normalizedClassId == 51 && ship.isPassenger()) {
                xOffset -= 0.2F;
            }
            double x = ship.getX() - xOffset * Math.sin(radians);
            double z = ship.getZ() + xOffset * Math.cos(radians);
            if (point.kind() == EngineVisualKind.SMOKE_CHIMNEY_L) {
                for (int segment = 0; segment < 3; segment++) {
                    ship.level().addParticle(ParticleTypes.LARGE_SMOKE,
                            x, ship.getY() + point.yOffset() + segment * 0.3D, z,
                            point.particleScale(), 0.0D, 0.0D);
                }
            } else if (point.kind() == EngineVisualKind.YAMATO_SPEED_BLUR) {
                spawnYamatoSpeedBlur(ship, x, ship.getY() + point.yOffset(), z);
            }
        }
    }

    /** Mirrors EntityCarrierWo's model-state, fuel, sitting, and head-tilt gates. */
    public static boolean shouldSpawnCarrierWoEyeEffect(int modelState, boolean noFuel,
                                                         boolean sitting, int emotion) {
        return (modelState & 1) != 0
                && !noFuel
                && (!sitting || emotion != 4);
    }

    /** Mirrors EntityNorthernHime's eight-tick type-28 emotion trigger. */
    public static boolean shouldSpawnNorthernHimeMouthEffect(int modelState, boolean noFuel,
                                                               int tickCount) {
        return (tickCount & 7) == 0
                && (modelState & (1 << 2)) != 0
                && !noFuel;
    }

    /** Mirrors EntityNorthernHime's sitting/riding type-28 particle height. */
    public static double northernHimeMouthYOffsetFor(boolean sitting, boolean passenger) {
        return sitting || passenger ? 0.9D : 1.1D;
    }

    /** Mirrors the two Carrier Wo eye positions after the legacy tilt/yaw/pitch transforms. */
    public static List<LegacyClientEffectPoint> carrierWoEyePointsFor(boolean sitting,
                                                                       int emotion2,
                                                                       float yaw,
                                                                       float pitch) {
        float[] left = sitting
                ? new float[] {-0.3F, 1.0F, -0.4F}
                : new float[] {0.55F, 1.0F, 0.2F};
        float[] right = sitting
                ? new float[] {-0.7F, 0.8F, 0.6F}
                : new float[] {-0.55F, 1.0F, 0.2F};

        if (emotion2 == 1 && !sitting) {
            float[] tiltedLeft = rotateLegacyXZByAxis(left[0], left[1], -0.24F);
            float[] tiltedRight = rotateLegacyXZByAxis(right[0], right[1], -0.24F);
            left[0] = tiltedLeft[0];
            left[1] = tiltedLeft[1];
            right[0] = tiltedRight[0];
            right[1] = tiltedRight[1];
        }

        left = rotateLegacyXYZByYawPitch(left[0], left[1], left[2], yaw, pitch);
        right = rotateLegacyXYZByYawPitch(right[0], right[1], right[2], yaw, pitch);
        return List.of(legacyEmotionPoint(left, 1.0F), legacyEmotionPoint(right, 1.0F));
    }

    /** Mirrors EntitySubmYo's model-state, fuel, and bored-sitting gates. */
    public static boolean shouldSpawnSubmarineYoEyeEffect(int modelState, boolean noFuel,
                                                           boolean sitting, int emotion) {
        return (modelState & 1) != 0
                && !noFuel
                && (!sitting || emotion != 4);
    }

    /** Mirrors EntitySubmYo's two body-yaw-only eye positions. */
    public static List<LegacyClientEffectPoint> submarineYoEyePointsFor(boolean sitting,
                                                                         float bodyYaw) {
        float[] left = sitting
                ? new float[] {0.35F, 1.5F, -0.5F}
                : new float[] {0.35F, 1.8F, -0.35F};
        float[] right = sitting
                ? new float[] {-0.35F, 1.5F, -0.5F}
                : new float[] {-0.35F, 1.8F, -0.35F};
        left = rotateLegacyXYZByYawPitch(left[0], left[1], left[2], bodyYaw, 0.0F);
        right = rotateLegacyXYZByYawPitch(right[0], right[1], right[2], bodyYaw, 0.0F);
        return List.of(legacyEmotionPoint(left, 0.5F), legacyEmotionPoint(right, 0.5F));
    }

    public static boolean shouldSpawnBattleshipRuFlameEffect(int modelState,
                                                               boolean sitting,
                                                               boolean passenger,
                                                               int tickCount) {
        return (tickCount & 15) == 0
                && (tickCount & 511) < 400
                && (modelState & (1 << 3)) != 0
                && !sitting
                && !passenger;
    }

    public static boolean shouldSpawnBattleshipRuBoredEffect(int modelState, int emotion,
                                                              int tickCount) {
        return (tickCount & 63) == 0
                && (tickCount & 511) > 400
                && emotion == 4
                && (modelState & 1) != 0;
    }

    public static boolean shouldSpawnTransportWaEmotion(int tickCount, int randomRoll) {
        return (tickCount & 127) == 0 && randomRoll == 0;
    }

    public static boolean shouldSpawnCarrierAkagiEmotion(int tickCount, int randomRoll,
                                                          boolean noFuel) {
        return (tickCount & 127) == 0 && randomRoll == 0 && !noFuel;
    }

    /** Mirrors EntitySubmRo500's one-in-eight garuru ambient sound branch. */
    public static boolean shouldPlayRo500GaruruAmbient(int legacyClassId, int randomRoll) {
        return normalizeLegacyClassId(legacyClassId) == 39 && randomRoll == 0;
    }

    public static int battleshipRuRingLuckAmplifierFor(int shipLevel) {
        return shipLevel / 140;
    }

    /** Mirrors EntityBattleshipNGT's client-side type-1 phase effect cadence. */
    public static boolean shouldSpawnBattleshipNagatoPhaseEffect(int phase, int tickCount) {
        return (tickCount & 7) == 0 && (phase == 1 || phase == 3);
    }

    public static float battleshipNagatoPhaseParticleScaleFor(boolean hostile, int scaleLevel) {
        return hostile ? 0.12F + 0.1F * Math.max(0, scaleLevel) : 0.12F;
    }

    /** Mirrors EntityBattleshipYMT's client-side type-4 railgun cadence. */
    public static boolean shouldSpawnBattleshipYamatoPhaseEffect(int phase, int tickCount) {
        return (tickCount & 15) == 0 && phase > 0;
    }

    public static float battleshipYamatoPhaseParticleScaleFor(boolean hostile, int scaleLevel) {
        return hostile ? 0.1F + Math.max(0, scaleLevel) : 0.1F;
    }

    /** Legacy ParticleTeam subtype used by the audited client-only state branches. */
    public static int legacyEntityMarkerTypeFor(int legacyClassId) {
        return switch (normalizeLegacyClassId(legacyClassId)) {
            case 37 -> 1;
            case 46 -> 4;
            case 12, 13 -> 17;
            default -> -1;
        };
    }

    /** Mirrors EntityBattleshipNGT's random emotion subtype selection. */
    public static int nagatoMoraleEmotionTypeFor(int randomRoll) {
        return switch (randomRoll) {
            case 1 -> 31;
            case 2 -> 1;
            case 3 -> 7;
            case 4 -> 16;
            default -> 29;
        };
    }

    /** Mirrors legacy custom light-cannon attacker hooks that are not ordinary beam feedback. */
    public static LegacyLightAttackParticleFeedback lightAttackParticleFeedbackFor(ShipEntitySpec spec,
                                                                                   LegacyShipAttackKind attackKind) {
        if (spec == null || attackKind != LegacyShipAttackKind.LIGHT) {
            return LegacyLightAttackParticleFeedback.NONE;
        }

        int classId = normalizeLegacyClassId(spec.legacyClassId());
        return switch (classId) {
            case 13 -> new LegacyLightAttackParticleFeedback(
                    GameplayParticleType.LEGACY_DOUBLE_CANNON, 0.0D, 0.0D, 0.7D);
            case 26 -> new LegacyLightAttackParticleFeedback(
                    GameplayParticleType.LEGACY_DOUBLE_CANNON, 0.3D, 0.3D, 1.0D);
            case 31 -> new LegacyLightAttackParticleFeedback(
                    GameplayParticleType.LEGACY_NORTHERN_CANNON, 0.0D, 0.0D, 0.0D);
            case 60, 61, 62, 63 -> !spec.hostile()
                    ? new LegacyLightAttackParticleFeedback(
                    GameplayParticleType.LEGACY_DOUBLE_CANNON, 0.3D, 0.3D, 1.0D)
                    : LegacyLightAttackParticleFeedback.NONE;
            case 15 -> new LegacyLightAttackParticleFeedback(
                    GameplayParticleType.LEGACY_RE_LASER, 1.7D, 1.45D, 0.0D);
            default -> LegacyLightAttackParticleFeedback.NONE;
        };
    }

    /** Mirrors EntityNorthernHime.applyParticleAtTarget's successful light-cannon hit effect. */
    public static GameplayParticleType lightAttackTargetParticleFor(ShipEntitySpec spec,
                                                                     LegacyShipAttackKind attackKind,
                                                                     GameplayParticleType fallback) {
        if (spec != null && attackKind == LegacyShipAttackKind.LIGHT
                && normalizeLegacyClassId(spec.legacyClassId()) == 31) {
            return GameplayParticleType.LEGACY_NORTHERN_HIT;
        }
        return fallback;
    }

    /** Returns every legacy type-5 smoke packet emitted by a light cannon. */
    public static List<LegacyCannonAttackFeedback> cannonAttackFeedbacksFor(ShipEntitySpec spec,
                                                                              LegacyShipAttackKind attackKind,
                                                                              int hostileScaleLevel) {
        if (spec == null || attackKind != LegacyShipAttackKind.LIGHT) {
            return List.of();
        }

        int scale = Mth.clamp(hostileScaleLevel, 0, 3);
        double factor = scale + 1.0D;
        return switch (normalizeLegacyClassId(spec.legacyClassId())) {
            case 37 -> spec.hostile()
                    ? List.of(new LegacyCannonAttackFeedback(
                    0.9D * factor, 1.0D * factor, 1.1D * factor))
                    : List.of(new LegacyCannonAttackFeedback(0.9D, 1.0D, 1.1D));
            case 46 -> spec.hostile()
                    ? List.of(
                    new LegacyCannonAttackFeedback(1.0D * factor, 1.0D * factor, 1.6D * factor),
                    new LegacyCannonAttackFeedback(0.9D * factor, 1.2D * factor, 1.0D * factor),
                    new LegacyCannonAttackFeedback(1.1D * factor, 1.1D * factor, 0.5D * factor))
                    : List.of(new LegacyCannonAttackFeedback(0.9D, 1.3D, 1.2D));
            case 60, 61, 62, 63 -> spec.hostile()
                    ? List.of(new LegacyCannonAttackFeedback(
                    0.9D * factor, 1.0D * factor, 1.1D * factor))
                    : List.of();
            default -> List.of();
        };
    }

    /** Keeps the existing single-feedback API for callers that only need the first packet. */
    public static LegacyLightAttackFeedback lightAttackFeedbackFor(ShipEntitySpec spec,
                                                                    LegacyShipAttackKind attackKind) {
        if (spec == null || attackKind != LegacyShipAttackKind.LIGHT) {
            return LegacyLightAttackFeedback.NONE;
        }

        return switch (normalizeLegacyClassId(spec.legacyClassId())) {
            case 15 -> new LegacyLightAttackFeedback(1.7D, 1.45D, 0.25F);
            case 33 -> new LegacyLightAttackFeedback(1.85D, 0.8D, 0.7F);
            default -> LegacyLightAttackFeedback.NONE;
        };
    }

    /** Mirrors the EntityBBKongou/Hiei/Haruna/Kirishima Mob type-5 light-cannon hook. */
    public static LegacyCannonAttackFeedback cannonAttackFeedbackFor(ShipEntitySpec spec,
                                                                       LegacyShipAttackKind attackKind,
                                                                       int hostileScaleLevel) {
        return cannonAttackFeedbacksFor(spec, attackKind, hostileScaleLevel).stream()
                .findFirst()
                .orElse(LegacyCannonAttackFeedback.NONE);
    }

    /** EntityBattleshipRe replaces the generic light-cannon feedback with its laser hook. */
    public static boolean usesBattleshipReLightAttackEffects(ShipEntitySpec spec,
                                                               LegacyShipAttackKind attackKind) {
        return spec != null
                && normalizeLegacyClassId(spec.legacyClassId()) == 15
                && lightAttackFeedbackFor(spec, attackKind).isPresent();
    }

    private static void tickBattleshipNagatoClientBehavior(LegacyShipEntity ship) {
        if (!shouldSpawnBattleshipNagatoPhaseEffect(ship.getDisplayPhase(), ship.tickCount)) {
            return;
        }
        GameplayClientEffects.spawnLegacyTeamMarker(
                ship, legacyEntityMarkerTypeFor(ship.getShipClassId()));
    }

    private static void tickBattleshipYamatoClientBehavior(LegacyShipEntity ship) {
        if (!shouldSpawnBattleshipYamatoPhaseEffect(ship.getDisplayPhase(), ship.tickCount)) {
            return;
        }
        GameplayClientEffects.spawnLegacyTeamMarker(
                ship, legacyEntityMarkerTypeFor(ship.getShipClassId()));
    }

    private static void tickCarrierWoClientBehavior(LegacyShipEntity ship) {
        if (shouldSpawnCarrierWoEyeEffect(ship.getModelState(), ship.hasNoFuel(),
                ship.isOrderedToSit(), ship.getEmotion())) {
            float yaw = ship.getYHeadRot() * ((float) Math.PI / 180.0F);
            float pitch = ship.getXRot() * ((float) Math.PI / 180.0F);
            for (LegacyClientEffectPoint point : carrierWoEyePointsFor(
                    ship.isOrderedToSit(), ship.getEmotion2(), yaw, pitch)) {
                spawnLegacyClientEffect(ship, point, 1.5D);
            }
        }

        if ((ship.tickCount & 15) == 0
                && (ship.getModelState() & (1 << 4)) != 0
                && !ship.isOrderedToSit()
                && !ship.isPassenger()) {
            GameplayClientEffects.spawnLegacyTeamMarker(
                    ship, legacyEntityMarkerTypeFor(ship.getShipClassId()));
        }
    }

    private static void tickBattleshipRuClientBehavior(LegacyShipEntity ship) {
        if (shouldSpawnBattleshipRuFlameEffect(ship.getModelState(), ship.isOrderedToSit(),
                ship.isPassenger(), ship.tickCount)) {
            GameplayClientEffects.spawnLegacyTeamMarker(
                    ship, legacyEntityMarkerTypeFor(ship.getShipClassId()));
        }

        if (shouldSpawnBattleshipRuBoredEffect(ship.getModelState(), ship.getEmotion(),
                ship.tickCount)) {
            float xOffset = ship.getRandom().nextInt(2) == 0 ? 0.0F : 2.0F;
            spawnLegacyBodyParticle(ship, 0.5F, 0.0F, xOffset,
                    LegacyClientEffectKind.SMOKE, 36);
        }
    }

    private static void tickTransportWaClientBehavior(LegacyShipEntity ship) {
        if (shouldSpawnTransportWaEmotion(ship.tickCount, ship.getRandom().nextInt(4))) {
            spawnLegacyEntityEmotion(ship, 2);
        }
    }

    private static void tickSubmarineYoClientBehavior(LegacyShipEntity ship) {
        if (!shouldSpawnSubmarineYoEyeEffect(ship.getModelState(), ship.hasNoFuel(),
                ship.isOrderedToSit(), ship.getEmotion())) {
            return;
        }

        float yaw = ship.yBodyRot * ((float) Math.PI / 180.0F);
        for (LegacyClientEffectPoint point : submarineYoEyePointsFor(ship.isOrderedToSit(), yaw)) {
            spawnLegacyClientEffect(ship, point, 0.0D);
        }
    }

    private static void tickCarrierAkagiClientBehavior(LegacyShipEntity ship) {
        if (!ship.isHostileVariant()
                && shouldSpawnCarrierAkagiEmotion(ship.tickCount, ship.getRandom().nextInt(4),
                        ship.hasNoFuel())) {
            spawnLegacyEntityEmotion(ship, 9);
        }
    }

    private static void spawnLegacyClientEffect(LegacyShipEntity ship,
                                                 LegacyClientEffectPoint point,
                                                 double yBase) {
        ship.level().addParticle(legacyClientParticle(point.kind(), point.legacyEmotionType()),
                ship.getX() + point.xOffset(),
                ship.getY() + yBase + point.yOffset(),
                ship.getZ() + point.zOffset(),
                point.velocityX(), point.velocityY(), point.velocityZ());
    }

    private static void spawnLegacyEntityEmotion(LegacyShipEntity ship, int emotionType) {
        double yOffset = ship.isOrderedToSit() ? ship.getBbHeight() * 0.4D
                : ship.getBbHeight() * 0.45D;
        ship.level().addParticle(legacyClientParticle(LegacyClientEffectKind.EMOTION, emotionType),
                ship.getX(), ship.getY() + yOffset, ship.getZ(), 0.0D, 0.0D, 0.0D);
    }

    private static void spawnLegacyBodyParticle(LegacyShipEntity ship, float zOffset,
                                                 float yOffset, float xOffset,
                                                 LegacyClientEffectKind kind,
                                                 int emotionType) {
        float radians = ship.yBodyRot * ((float) Math.PI / 180.0F);
        float[] rotated = rotateLegacyXZByAxis(zOffset, xOffset, radians);
        double x = ship.getX() + rotated[1];
        double z = ship.getZ() + rotated[0];
        if (kind == LegacyClientEffectKind.FLAME) {
            for (int segment = -1; segment <= 1; segment++) {
                ship.level().addParticle(ParticleTypes.FLAME, x,
                        ship.getY() + yOffset + segment * 0.1D, z, 0.0D, 0.0D, 0.0D);
            }
        } else {
            ship.level().addParticle(legacyClientParticle(kind, emotionType), x,
                    ship.getY() + yOffset, z, 0.0D, 0.05D, 0.0D);
        }
    }

    private static LegacyClientEffectPoint legacyEmotionPoint(float[] point, float velocityZ) {
        return new LegacyClientEffectPoint(point[0], point[1], point[2],
                0.0F, 0.05F, velocityZ, LegacyClientEffectKind.EMOTION, 16);
    }

    private static ParticleOptions legacyClientParticle(LegacyClientEffectKind kind, int emotionType) {
        if (kind == LegacyClientEffectKind.FLAME) {
            return ParticleTypes.FLAME;
        }
        if (kind == LegacyClientEffectKind.SMOKE || emotionType == 36) {
            return ParticleTypes.LARGE_SMOKE;
        }
        return switch (emotionType) {
            case 9 -> ParticleTypes.DRIPPING_WATER;
            case 16 -> ParticleTypes.HAPPY_VILLAGER;
            default -> ParticleTypes.SMOKE;
        };
    }

    private static float[] rotateLegacyXZByAxis(float z, float x, float radians) {
        float cos = Mth.cos(radians);
        float sin = Mth.sin(radians);
        return new float[] {z * cos + x * sin, x * cos - z * sin};
    }

    private static float[] rotateLegacyXYZByYawPitch(float x, float y, float z,
                                                      float yaw, float pitch) {
        float cosYaw = Mth.cos(yaw);
        float sinYaw = Mth.sin(yaw);
        float cosPitch = Mth.cos(-pitch);
        float sinPitch = Mth.sin(-pitch);
        float rotatedY = y * cosPitch + z * sinPitch;
        float rotatedZ = z * cosPitch - y * sinPitch;
        float rotatedX = x * cosYaw - rotatedZ * sinYaw;
        rotatedZ = rotatedZ * cosYaw + x * sinYaw;
        return new float[] {rotatedX, rotatedY, rotatedZ};
    }

    /** Mirrors the old per-class engine visual points assigned by constructors and client ticks. */
    public static List<EngineVisualPoint> legacyEngineVisualPointsFor(ShipEntitySpec spec,
                                                                        int hostileScaleLevel) {
        if (spec == null) {
            return List.of();
        }

        int normalizedClassId = normalizeLegacyClassId(spec.legacyClassId());
        if (normalizedClassId == 37) {
            if (!spec.hostile()) {
                return List.of(new EngineVisualPoint(-0.56F, 1.5F, 1.0F,
                        EngineVisualKind.SMOKE_CHIMNEY_L, 1, true));
            }

            int scale = Mth.clamp(hostileScaleLevel, 0, 3);
            float sizeMultiplier = 1.0F + scale;
            return List.of(new EngineVisualPoint(
                    -0.56F * sizeMultiplier,
                    1.5F * sizeMultiplier,
                    sizeMultiplier,
                    EngineVisualKind.SMOKE_CHIMNEY_L,
                    1,
                    false));
        }

        if (normalizedClassId == 46) {
            if (!spec.hostile()) {
                return List.of(new EngineVisualPoint(-0.63F, 1.65F, 1.0F,
                        EngineVisualKind.YAMATO_SPEED_BLUR, 0, true));
            }

            int scale = Mth.clamp(hostileScaleLevel, 0, 3);
            float sizeMultiplier = 1.0F + scale;
            return List.of(new EngineVisualPoint(
                    -0.63F * sizeMultiplier,
                    1.65F * sizeMultiplier,
                    sizeMultiplier,
                    EngineVisualKind.SMOKE_CHIMNEY_L,
                    -1,
                    false));
        }

        if (normalizedClassId >= 51 && normalizedClassId <= 54) {
            if (!spec.hostile()) {
                return List.of(new EngineVisualPoint(-0.42F, 1.4F, 1.0F,
                        EngineVisualKind.SMOKE_CHIMNEY_L, 0, true));
            }

            int scale = Mth.clamp(hostileScaleLevel, 0, 3);
            return List.of(new EngineVisualPoint(
                    hostileSixDestroyerSmokeX(scale),
                    hostileSixDestroyerSmokeY(scale),
                    1.0F + scale * 0.8F,
                    EngineVisualKind.SMOKE_CHIMNEY_L,
                    0,
                    false));
        }

        if (normalizedClassId < 60 || normalizedClassId > 63) {
            return List.of();
        }
        if (!spec.hostile()) {
            return switch (normalizedClassId) {
                case 60 -> List.of(
                        new EngineVisualPoint(-0.7F, 1.17F, 1.0F,
                                EngineVisualKind.SMOKE_CHIMNEY_L, 0, true),
                        new EngineVisualPoint(-0.45F, 1.32F, 1.0F,
                                EngineVisualKind.SMOKE_CHIMNEY_L, 0, true));
                case 61, 62, 63 -> List.of(new EngineVisualPoint(-0.6F, 1.17F, 1.0F,
                        EngineVisualKind.SMOKE_CHIMNEY_L, 0, true));
                default -> List.of();
            };
        }

        int scale = Mth.clamp(hostileScaleLevel, 0, 3);
        return switch (normalizedClassId) {
            case 60 -> List.of(
                    new EngineVisualPoint(hostileKongouSmokeX(scale, false),
                            hostileKongouSmokeY(scale, false), 1.0F + scale,
                            EngineVisualKind.SMOKE_CHIMNEY_L, 0, false),
                    new EngineVisualPoint(hostileKongouSmokeX(scale, true),
                            hostileKongouSmokeY(scale, true), 1.0F + scale,
                            EngineVisualKind.SMOKE_CHIMNEY_L, 0, false));
            case 61 -> List.of(new EngineVisualPoint(hostileHieiSmokeX(scale),
                    hostileHieiSmokeY(scale), 1.0F + scale,
                    EngineVisualKind.SMOKE_CHIMNEY_L, 0, false));
            case 62, 63 -> List.of(new EngineVisualPoint(hostileHieiSmokeX(scale),
                    hostileHieiSmokeY(scale), 1.0F + scale,
                    EngineVisualKind.SMOKE_CHIMNEY_L, 0, false));
            default -> List.of();
        };
    }

    /** Returns only the legacy type-43 points for callers that do not need other client visuals. */
    public static List<EngineSmokePoint> legacyEngineSmokePointsFor(ShipEntitySpec spec,
                                                                     int hostileScaleLevel) {
        List<EngineSmokePoint> points = new ArrayList<>();
        for (EngineVisualPoint point : legacyEngineVisualPointsFor(spec, hostileScaleLevel)) {
            if (point.kind() == EngineVisualKind.SMOKE_CHIMNEY_L) {
                points.add(new EngineSmokePoint(point.xOffset(), point.yOffset(), point.particleScale()));
            }
        }
        return points.isEmpty() ? List.of() : List.copyOf(points);
    }

    private static void spawnYamatoSpeedBlur(LegacyShipEntity ship, double x, double y, double z) {
        // The legacy type-20 ParticleLine is a client-only custom quad. Keep
        // its trigger and anchor, using the existing charged-beam primitives
        // as the smallest modern visual equivalent.
        ship.level().addParticle(ParticleTypes.END_ROD, x, y, z, 0.0D, 0.02D, 0.0D);
        ship.level().addParticle(ParticleTypes.ELECTRIC_SPARK, x, y, z, 0.0D, 0.0D, 0.0D);
    }

    private static float hostileKongouSmokeX(int scale, boolean secondPoint) {
        if (secondPoint) {
            return switch (scale) {
                case 1 -> -1.33F;
                case 2 -> -1.98F;
                case 3 -> -2.61F;
                default -> -0.45F;
            };
        }
        return switch (scale) {
            case 1 -> -0.82F;
            case 2 -> -1.25F;
            case 3 -> -1.64F;
            default -> -0.6F;
        };
    }

    private static float hostileSixDestroyerSmokeX(int scale) {
        return switch (scale) {
            case 1 -> -0.8F;
            case 2 -> -1.2F;
            case 3 -> -1.65F;
            default -> -0.42F;
        };
    }

    private static float hostileSixDestroyerSmokeY(int scale) {
        return switch (scale) {
            case 1 -> 2.7F;
            case 2 -> 4.1F;
            case 3 -> 5.3F;
            default -> 1.4F;
        };
    }

    private static boolean legacyEngineRiderStateAllowsSmoke(LegacyShipEntity ship,
                                                               int normalizedClassId) {
        return switch (normalizedClassId) {
            case 51 -> ship.getLegacyRiderType() < 1;
            case 52 -> ship.getLegacyRiderType() < 2;
            case 54 -> ship.getLegacyRiderType() < 4;
            default -> true;
        };
    }

    private static float hostileHieiSmokeX(int scale) {
        return switch (scale) {
            case 1 -> -1.16F;
            case 2 -> -1.76F;
            case 3 -> -2.36F;
            default -> -0.6F;
        };
    }

    private static float hostileHieiSmokeY(int scale) {
        return switch (scale) {
            case 1 -> 2.3F;
            case 2 -> 3.4F;
            case 3 -> 4.6F;
            default -> 1.17F;
        };
    }

    private static float hostileKongouSmokeY(int scale, boolean secondPoint) {
        if (secondPoint) {
            return switch (scale) {
                case 1 -> 2.21F;
                case 2 -> 3.18F;
                case 3 -> 4.27F;
                default -> 1.32F;
            };
        }
        return switch (scale) {
            case 1 -> 2.47F;
            case 2 -> 3.57F;
            case 3 -> 4.74F;
            default -> 1.17F;
        };
    }

    /** Mirrors BasicEntityShip's server-side 64-tick HP and emotion update. */
    public static void tickLegacyEmotionState(LegacyShipEntity ship) {
        if (ship == null || ship.level().isClientSide() || !ship.isAlive()) {
            return;
        }

        float maxHealth = ship.getMaxHealth();
        float healthRatio = maxHealth > 0.0F
                ? Mth.clamp(ship.getHealth() / maxHealth, 0.0F, 1.0F) : 0.0F;
        int hpState = healthRatio > 0.75F ? LEGACY_HP_NORMAL
                : healthRatio > 0.5F ? LEGACY_HP_MINOR
                : healthRatio > 0.25F ? LEGACY_HP_MODERATE : LEGACY_HP_HEAVY;
        ship.setHpState(hpState);

        if (ship.hasNoFuel()) {
            if (ship.getEmotion() != LEGACY_EMOTION_HUNGRY) {
                ship.setEmotion(LEGACY_EMOTION_HUNGRY);
            }
            return;
        }

        if (healthRatio < 0.35F) {
            if (ship.getEmotion() != LEGACY_EMOTION_TT) {
                ship.setEmotion(LEGACY_EMOTION_TT);
            }
            return;
        }

        boolean hostile = ship.isHostileVariant();
        if (ship.getEmotion() == LEGACY_EMOTION_NORMAL) {
            int boredRolls = hostile ? 4 : 3;
            if (ship.getRandom().nextInt(boredRolls) == 0) {
                ship.setEmotion(LEGACY_EMOTION_BORED);
            }
        } else {
            int normalRolls = hostile ? 2 : 4;
            if (ship.getRandom().nextInt(normalRolls) == 0) {
                ship.setEmotion(LEGACY_EMOTION_NORMAL);
            }
        }

        // EntityCAAtago resets Emotion4 every healthy update; its hostile mob
        // inherits the hostile base rule instead.
        if (!hostile && normalizeLegacyClassId(ship.getShipClassId()) == 58) {
            if (ship.getEmotion4() != LEGACY_EMOTION_NORMAL) {
                ship.setEmotion4(LEGACY_EMOTION_NORMAL);
            }
        } else if (ship.getEmotion4() == LEGACY_EMOTION_NORMAL) {
            if (ship.getRandom().nextInt(3) == 0) {
                ship.setEmotion4(LEGACY_EMOTION_BORED);
            }
        } else {
            int normalRolls = hostile ? 2 : 3;
            if (ship.getRandom().nextInt(normalRolls) == 0) {
                ship.setEmotion4(LEGACY_EMOTION_NORMAL);
            }
        }
    }

    /** Mirrors Shimakaze's 128-tick servant recovery outside the marriage gate. */
    public static void tickLegacyPerShipBehavior(LegacyShipEntity ship) {
        if (ship == null || ship.level().isClientSide() || !ship.isAlive()) {
            return;
        }

        int normalizedClassId = normalizeLegacyClassId(ship.getShipClassId());
        if ((normalizedClassId == 36 || normalizedClassId == 14) && (ship.tickCount & 127) == 0) {
            int maximum = normalizedClassId == 36 && ship.isHostileVariant() ? 10 : 6;
            if (ship.getShimakazeRensouhouCount() < maximum) {
                ship.addShimakazeRensouhouCount(1);
            }
        }

        if (!ship.isHostileVariant() && normalizedClassId == 37
                && (ship.tickCount & 127) == 0) {
            tickNagatoMoraleEvent(ship);
        }

        if (normalizedClassId == 56) {
            tickTenryuuSkill(ship);
        }

        if (normalizedClassId == 57) {
            tickTatsutaSkill(ship);
        }

        if (!ship.isHostileVariant() && (normalizedClassId == 9 || normalizedClassId == 10)) {
            tickHeavyCruiserNightBehavior(ship);
        }

        if (!ship.isHostileVariant() && normalizedClassId == 12) {
            tickCarrierWoBehavior(ship);
        }

        if (!ship.isHostileVariant() && normalizedClassId == 14) {
            tickBattleshipTaBehavior(ship);
        }

        if (!ship.isHostileVariant() && normalizedClassId == 16) {
            tickTransportWaBehavior(ship);
        }

        tickSubmarineInvisibilityBehavior(ship);

        if (normalizedClassId == 13) {
            tickBattleshipRuRingLuck(ship);
            tickBattleshipRuBarrage(ship);
        }

        if (normalizedClassId == 10 && !ship.isHostileVariant()) {
            tickLegacyPushBehavior(ship);
        }

        if (normalizedClassId == 15) {
            tickLegacyPushBehavior(ship);
        }

        if (normalizedClassId == 44 || normalizedClassId == 72) {
            tickSubmarineHimeBehavior(ship);
        }

        if (normalizedClassId == 20 || normalizedClassId == 21 || normalizedClassId == 26
                || normalizedClassId == 27 || normalizedClassId == 28 || normalizedClassId == 30
                || normalizedClassId == 31 || normalizedClassId == 33 || normalizedClassId == 49) {
            tickHimeBehavior(ship);
        }

        if (normalizedClassId == 49) {
            tickLegacyPushBehavior(ship);
        }
    }

    /** Mirrors the five legacy submarine classes' distinct friendly and hostile invisibility ticks. */
    private static void tickSubmarineInvisibilityBehavior(LegacyShipEntity ship) {
        if ((ship.tickCount & 127) != 0
                || !usesPerShipRingInvisibility(ship.getSpec())) {
            return;
        }
        int normalizedClassId = normalizeLegacyClassId(ship.getShipClassId());
        if (normalizedClassId == 44 || normalizedClassId == 72) {
            return;
        }

        if (ship.isHostileVariant()) {
            if (ship.getRandom().nextInt(2) != 0) {
                return;
            }

            int scaleLevel = Mth.clamp(ship.getLegacyHostileScaleLevel(), 0, 3);
            ship.applyLegacyRingEffect(ship, MobEffects.INVISIBILITY, 40 + scaleLevel * 10, 0);
            return;
        }

        if (!ship.isRingEffectEnabled() || ship.getGrudge() <= 0) {
            return;
        }

        int duration = 40 + ship.getShipLevel();
        ship.applyLegacyRingEffect(ship, MobEffects.INVISIBILITY, duration, 0);
        if (ship.isMarried()) {
            ship.applyOwnerRingEffect(MobEffects.INVISIBILITY, duration, 0);
        }
    }

    /** Mirrors EntitySubmHime and EntitySSNH's 1.12 ring aura and SSNH morale tick. */
    private static void tickSubmarineHimeBehavior(LegacyShipEntity ship) {
        if (!ship.isHostileVariant()
                && (ship.tickCount & 127) == 0
                && ship.isRingEffectEnabled()
                && ship.getGrudge() > 0) {
            int duration = 80 + ship.getShipLevel();
            ship.applyLegacyRingEffect(ship, MobEffects.INVISIBILITY, duration, 0);
            if (ship.isMarried()) {
                ship.applyOwnerRingEffect(MobEffects.INVISIBILITY, duration, 0);
            }
        }

        if (normalizeLegacyClassId(ship.getShipClassId()) != 72) {
            return;
        }

        if ((ship.tickCount & 63) == 0
                && ship.isPassenger()
                && ship.getMorale() < GATTAI_MORALE_THRESHOLD) {
            ship.addMorale(150);
        }

        if ((ship.tickCount & 255) == 0 && ship.getRandom().nextInt(3) == 0) {
            startNorthernHimeRidingSearch(ship);
        }
        tickNorthernHimeRiding(ship);
    }

    /** Mirrors the periodic abilities implemented directly by the legacy hime classes. */
    private static void tickHimeBehavior(LegacyShipEntity ship) {
        int normalizedClassId = normalizeLegacyClassId(ship.getShipClassId());

        if (normalizedClassId == 31) {
            if (shouldSpawnNorthernHimeMouthEffect(ship.getModelState(), ship.hasNoFuel(),
                    ship.tickCount)) {
                CombatFxDispatcher.sendParticle(ship, GameplayParticleType.LEGACY_EMOTION,
                        ship.getX(),
                        ship.getY() + northernHimeMouthYOffsetFor(
                                ship.isOrderedToSit(), ship.isPassenger()),
                        ship.getZ(), 0.0D, 0.0D, 0.0D, 28);
            }
            if ((ship.tickCount & 63) == 0) {
                if (ship.isPassenger() && ship.getMorale() < GATTAI_MORALE_THRESHOLD) {
                    ship.addMorale(150);
                }
                if (ship.getGrudge() > 0 && ship.getHealth() < ship.getMaxHealth()) {
                    ship.heal(ship.getMaxHealth() * 0.03F + 1.0F);
                }
                if (ship.isMarried() && ship.isRingEffectEnabled() && ship.getGrudge() > 25) {
                    healHimeAllies(ship, 8.0D, ship.getShipLevel() / 25 + 1, 0.98F, 25,
                            1.0F, 0.02F, 0.02F,
                            1.0F, 0.02F, 0.1F);
                }
            }
            if ((ship.tickCount & 255) == 0 && ship.getRandom().nextInt(3) == 0) {
                startNorthernHimeRidingSearch(ship);
            }
            tickNorthernHimeRiding(ship);
            return;
        }

        if ((ship.tickCount & 127) != 0) {
            return;
        }

        int shipLevel = ship.getShipLevel();
        switch (normalizedClassId) {
            case 20 -> {
                if (ship.isMarried() && ship.isRingEffectEnabled() && ship.getGrudge() > 0) {
                    ship.applySameOwnerShipRingAura(MobEffects.JUMP, 50 + shipLevel, shipLevel / 70);
                }
            }
            case 21 -> {
                if (ship.getGrudge() > 0 && ship.getHealth() < ship.getMaxHealth()) {
                    ship.heal(ship.getMaxHealth() * 0.06F + 1.0F);
                }
                if (ship.isMarried() && ship.isRingEffectEnabled() && ship.getGrudge() > 50) {
                    healHimeAllies(ship, 12.0D, shipLevel / 15 + 2, 0.96F, 50,
                            1.0F, 0.04F, 0.04F,
                            1.0F, 0.04F, 0.1F);
                }
            }
            case 26 -> {
                if (ship.isMarried() && ship.isRingEffectEnabled() && ship.getGrudge() > 0) {
                    ship.applySameOwnerShipRingAura(MobEffects.DAMAGE_RESISTANCE,
                            50 + shipLevel, shipLevel / 70);
                    ship.applySameOwnerShipRingAura(MobEffects.FIRE_RESISTANCE,
                            50 + shipLevel, shipLevel / 70);
                }
            }
            case 27 -> {
                if (ship.isMarried() && ship.isRingEffectEnabled() && ship.getGrudge() > 0) {
                    ship.applyOwnerRingEffect(MobEffects.MOVEMENT_SPEED, 80 + shipLevel, shipLevel / 45 + 1);
                    ship.applyOwnerRingEffect(MobEffects.DIG_SPEED, 80 + shipLevel, shipLevel / 30);
                }
            }
            case 28 -> {
                if (ship.getGrudge() > 0 && ship.getHealth() < ship.getMaxHealth()) {
                    ship.heal(ship.getMaxHealth() * 0.06F + 1.0F);
                }
                if (ship.isMarried() && ship.isRingEffectEnabled() && ship.getGrudge() > 50) {
                    healHimeAllies(ship, 12.0D, shipLevel / 50 + 1, 0.94F, 50,
                            4.0F, 0.06F, 0.06F,
                            4.0F, 0.06F, 0.2F);
                }
            }
            case 30 -> {
                if (ship.getGrudge() > 0 && ship.getHealth() < ship.getMaxHealth()) {
                    ship.heal(ship.getMaxHealth() * 0.09F + 1.0F);
                }
                if (ship.isMarried() && ship.isRingEffectEnabled() && ship.getGrudge() > 0) {
                    ship.applySameOwnerShipRingAura(MobEffects.ABSORPTION, 50 + shipLevel, shipLevel / 50);
                    ship.applyOwnerRingEffect(MobEffects.ABSORPTION, 50 + shipLevel, shipLevel / 50);
                }
            }
            case 33 -> {
                if (ship.isMarried() && ship.isRingEffectEnabled() && ship.getGrudge() > 0) {
                    ship.applySameOwnerShipRingAura(MobEffects.DIG_SPEED, 50 + shipLevel, shipLevel / 70);
                }
                ship.applyLegacyRingEffect(ship, MobEffects.NIGHT_VISION, 150, 0);
            }
            case 49 -> {
                if (!ship.level().isDay() && ship.isRingEffectEnabled()) {
                    ship.applyLegacyRingEffect(ship, MobEffects.MOVEMENT_SPEED, 150, shipLevel / 50);
                    ship.applyLegacyRingEffect(ship, MobEffects.JUMP, 150, shipLevel / 40);
                }
            }
            default -> {
            }
        }
    }

    private static void healHimeAllies(LegacyShipEntity ship, double radius, int maximumTargets,
                                       float healthThreshold, int grudgeCost,
                                       float playerBase, float playerMaxFactor, float playerLevelFactor,
                                       float shipBase, float shipMaxFactor, float shipLevelFactor) {
        int remaining = Math.max(0, maximumTargets);
        List<LivingEntity> candidates = ship.level().getEntitiesOfClass(
                LivingEntity.class,
                ship.getBoundingBox().inflate(radius),
                candidate -> candidate != ship
                        && candidate.isAlive()
                        && (candidate instanceof Player || candidate instanceof LegacyShipEntity)
                        && isLegacyHimeAlly(ship, candidate)
                        && candidate.getMaxHealth() > 0.0F
                        && candidate.getHealth() / candidate.getMaxHealth() < healthThreshold);

        for (LivingEntity target : candidates) {
            if (remaining <= 0) {
                break;
            }

            float before = target.getHealth();
            if (target instanceof Player) {
                target.heal(playerBase + target.getMaxHealth() * playerMaxFactor
                        + ship.getShipLevel() * playerLevelFactor);
            } else {
                target.heal(shipBase + target.getMaxHealth() * shipMaxFactor
                        + ship.getShipLevel() * shipLevelFactor);
            }

            if (target.getHealth() > before) {
                ship.addGrudge(-grudgeCost);
                remaining--;
                // EntityAirfieldHime, EntityHarbourHime, and EntityNorthernHime
                // send the legacy type-4 sparkle only after a heal succeeds.
                CombatFxDispatcher.sendParticle(ship, GameplayParticleType.HEALING_SPARKLE,
                        target.getX(), target.getY() + target.getBbHeight(), target.getZ());
            }
        }
    }

    private static boolean isLegacyHimeAlly(LegacyShipEntity ship, LivingEntity target) {
        return ship.isAlliedTo(target) || hasLegacySameOwner(ship, target);
    }

    private static void startNorthernHimeRidingSearch(LegacyShipEntity ship) {
        ship.resetLegacyHimeRiding();
        if (ship.isOrderedToSit() || ship.isLeashed() || ship.hasNoFuel()) {
            return;
        }

        if (ship.isPassenger()) {
            if (ship.getRandom().nextBoolean()) {
                ship.stopRiding();
            }
            return;
        }

        List<LivingEntity> candidates = ship.level().getEntitiesOfClass(
                LivingEntity.class,
                ship.getBoundingBox().inflate(6.0D, 4.0D, 6.0D),
                candidate -> candidate != ship
                        && (candidate instanceof LegacyShipEntity || candidate instanceof Player)
                        && !candidate.isPassenger()
                        && candidate.getPassengers().isEmpty()
                        && hasLegacySameOwner(ship, candidate));
        if (!candidates.isEmpty()) {
            ship.beginLegacyHimeRiding(candidates.get(ship.getRandom().nextInt(candidates.size())));
        }
    }

    private static void tickNorthernHimeRiding(LegacyShipEntity ship) {
        if (ship.getVehicle() != null && ship.getVehicle().isShiftKeyDown()) {
            ship.stopRiding();
        }
        if (!ship.isLegacyHimeRidingActive()) {
            return;
        }

        LivingEntity target = ship.getLegacyHimeRidingTarget();
        int ridingTicks = ship.getLegacyHimeRidingTicks() + 1;
        ship.setLegacyHimeRidingTicks(ridingTicks);
        if (ridingTicks > 200 || target == null || !target.isAlive()) {
            ship.resetLegacyHimeRiding();
            return;
        }

        double distance = ship.distanceTo(target);
        if (distance > 2.0D && ship.tickCount % 32 == 0) {
            ship.getNavigation().moveTo(target, 1.0D);
        }
        if (distance <= 2.0D && !target.isPassenger() && ship.getPassengers().isEmpty()
                && target.getPassengers().isEmpty()) {
            ship.startRiding(target, true);
            ship.getNavigation().stop();
            ship.resetLegacyHimeRiding();
        }
    }

    /** Mirrors EntityBattleshipRu's queued five-plus heavy missile barrage. */
    private static void tickBattleshipRuBarrage(LegacyShipEntity ship) {
        if (ship.getDisplayPhase() <= 0) {
            return;
        }

        int remaining = ship.getLegacySkillRemaining();
        if (remaining <= 0) {
            ship.resetLegacySkillState();
            return;
        }

        if ((ship.tickCount & 3) != 0) {
            return;
        }

        ship.setLegacySkillRemaining(remaining - 1);
        LivingEntity intendedTarget = ship.getTarget();
        BlockPos skillTarget = ship.getLegacySkillTarget();
        Vec3 shotTarget = new Vec3(
                skillTarget.getX() + ship.getRandom().nextFloat() * 8.0D - 4.0D,
                skillTarget.getY() + ship.getRandom().nextFloat() * 4.0D - 2.0D,
                skillTarget.getZ() + ship.getRandom().nextFloat() * 8.0D - 4.0D);
        LegacyShipProjectileProfile profile = ship.getAttackProfile().projectileProfile(
                LegacyShipAttackKind.HEAVY);
        LegacyShipProjectileEntity projectile = LegacyShipProjectileEntity.create(
                ship.level(), ship, intendedTarget, LegacyShipAttackKind.HEAVY,
                ship.getLegacyStats().attackHeavy() * 0.2F, false,
                profile, shotTarget, 0.15D, false, 3.5D);
        ship.level().addFreshEntity(projectile);
        ship.playLegacyBattleshipRuBarrageShotSound();
    }

    /** Mirrors EntityBattleshipRu's daytime ring Luck tick. */
    private static void tickBattleshipRuRingLuck(LegacyShipEntity ship) {
        if (!shouldApplyBattleshipRuRingLuck(ship.level().isDay(), ship.isRingEffectEnabled(), ship.tickCount)) {
            return;
        }

        ship.addEffect(new MobEffectInstance(MobEffects.LUCK, 150,
                battleshipRuRingLuckAmplifierFor(ship.getShipLevel()), false, false));
    }

    public static boolean shouldApplyBattleshipRuRingLuck(boolean daytime,
                                                          boolean ringEffectEnabled,
                                                          int tickCount) {
        return (tickCount & 127) == 0 && daytime && ringEffectEnabled;
    }

    /** Mirrors the legacy rand.nextInt(10) > 7 optional attack-voice gate. */
    public static boolean shouldPlayLegacyAttackVoice(int randomRoll) {
        return randomRoll > 7;
    }

    /** Mirrors EntityHeavyCruiserRi/Ne's 128-tick nighttime ring effects. */
    private static void tickHeavyCruiserNightBehavior(LegacyShipEntity ship) {
        if ((ship.tickCount & 127) != 0 || ship.level().isDay() || !ship.isRingEffectEnabled()) {
            return;
        }

        int shipLevel = ship.getShipLevel();
        ship.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 150, shipLevel / 70, false, false));
        ship.addEffect(new MobEffectInstance(MobEffects.JUMP, 150, shipLevel / 60, false, false));
    }

    /** Mirrors EntityCarrierWo's same-owner strength aura from onLivingUpdate. */
    private static void tickCarrierWoBehavior(LegacyShipEntity ship) {
        if ((ship.tickCount & 127) != 0
                || !ship.isMarried()
                || !ship.isRingEffectEnabled()
                || ship.getGrudge() <= 0) {
            return;
        }

        int shipLevel = ship.getShipLevel();
        ship.applySameOwnerShipRingAura(MobEffects.DAMAGE_BOOST, 30 + shipLevel, shipLevel / 80);
    }

    /** Mirrors EntityBattleshipTa.onLivingUpdate's same-owner speed aura. */
    private static void tickBattleshipTaBehavior(LegacyShipEntity ship) {
        if ((ship.tickCount & 127) != 0
                || !ship.isMarried()
                || !ship.isRingEffectEnabled()
                || ship.getGrudge() <= 0) {
            return;
        }

        int shipLevel = ship.getShipLevel();
        ship.applySameOwnerShipRingAura(MobEffects.MOVEMENT_SPEED, 50 + shipLevel, shipLevel / 80);
    }

    /** Mirrors EntityTransportWa's timed cargo use and same-owner resupply loop. */
    private static void tickTransportWaBehavior(LegacyShipEntity ship) {
        if ((ship.tickCount & 127) != 0) {
            return;
        }

        if (ship.getGrudge() <= 5400) {
            consumeTransportSupply(ship, ModItems.GRUDGE.get(), ModBlocks.BLOCK_GRUDGE.get().asItem(),
                    300, 2700, LegacyShipStatTables.Attr.GRUDGE, false);
        }
        if (ship.getLightAmmo() <= 540) {
            consumeTransportSupply(ship, ModItems.AMMO.get(), ModItems.AMMO1.get(),
                    30, 270, LegacyShipStatTables.Attr.AMMO, false);
        }
        if (ship.getHeavyAmmo() <= 270) {
            consumeTransportSupply(ship, ModItems.AMMO2.get(), ModItems.AMMO3.get(),
                    15, 135, LegacyShipStatTables.Attr.AMMO, true);
        }

        if ((ship.tickCount & 255) != 0 || ship.hasNoFuel()) {
            return;
        }

        int supplyCount = ship.getShipLevel() / 50 + 1;
        double range = 2.0D + ship.getCompatAttackRange() * 0.5D;
        List<LegacyShipEntity> nearby = ship.level().getEntitiesOfClass(
                LegacyShipEntity.class,
                ship.getBoundingBox().inflate(range),
                candidate -> candidate != ship
                        && !candidate.isHostileVariant()
                        && candidate.isAlive()
                        && hasSameLegacyOwner(ship, candidate));

        for (LegacyShipEntity target : nearby) {
            if (supplyCount <= 0) {
                break;
            }

            boolean supplied = false;
            if (ship.getGrudge() > 5400 && target.getGrudge() < 2700) {
                ship.addGrudge(-5400);
                target.addGrudge((int) (5400F * target.getLegacyStats().get(LegacyShipStatTables.Attr.GRUDGE)));
                supplied = true;
            }
            if (ship.getLightAmmo() >= 540 && target.getLightAmmo() < 270) {
                ship.addLightAmmo(-540);
                target.addLightAmmo((int) (540F * target.getLegacyStats().get(LegacyShipStatTables.Attr.AMMO)));
                supplied = true;
            }
            if (ship.getHeavyAmmo() >= 270 && target.getHeavyAmmo() < 135) {
                ship.addHeavyAmmo(-270);
                target.addHeavyAmmo((int) (270F * target.getLegacyStats().get(LegacyShipStatTables.Attr.AMMO)));
                supplied = true;
            }

            if (supplied) {
                ship.addShipExperience(40);
                supplyCount--;
            }
        }
    }

    private static void consumeTransportSupply(LegacyShipEntity ship,
                                                Item item,
                                                Item container,
                                                int itemValue,
                                                int containerValue,
                                                int statId,
                                                boolean heavyAmmo) {
        int value;
        if (ship.consumeLegacySupplyItem(item)) {
            value = itemValue;
        } else if (ship.consumeLegacySupplyItem(container)) {
            value = containerValue;
        } else {
            return;
        }

        int amount = (int) (value * ship.getLegacyStats().get(statId));
        if (statId == LegacyShipStatTables.Attr.GRUDGE) {
            ship.addGrudge(amount);
        } else if (heavyAmmo) {
            ship.addHeavyAmmo(amount);
        } else {
            ship.addLightAmmo(amount);
        }
    }

    /** Mirrors EntityHeavyCruiserNe and EntityBattleshipRe's periodic push target search and charge. */
    private static void tickLegacyPushBehavior(LegacyShipEntity ship) {
        if (ship.isLegacyRePushActive()) {
            LivingEntity target = ship.getLegacyRePushTarget();
            int pushTicks = ship.getLegacyRePushTicks() + 1;
            ship.setLegacyRePushTicks(pushTicks);
            if (pushTicks > 200 || target == null || !target.isAlive()) {
                ship.resetLegacyRePush();
                return;
            }

            if (ship.distanceTo(target) <= 2.5D) {
                double yaw = ship.getYRot() * (Math.PI / 180.0D);
                target.push(-Math.sin(yaw) * 0.5D, 0.5D, Math.cos(yaw) * 0.5D);
                ship.swing(InteractionHand.MAIN_HAND, true);
                ship.playLegacyRePushSound();
                ship.resetLegacyRePush();
            } else if (ship.tickCount % 32 == 0) {
                ship.getNavigation().moveTo(target, 1.0D);
            }
            return;
        }

        if ((ship.tickCount & 255) != 0
                || ship.getRandom().nextInt(5) != 0
                || ship.isOrderedToSit()
                || ship.isPassenger()
                || ship.hasNoFuel()
                || ship.isLeashed()) {
            return;
        }

        List<LivingEntity> candidates = ship.level().getEntitiesOfClass(
                LivingEntity.class,
                ship.getBoundingBox().inflate(12.0D, 6.0D, 12.0D),
                candidate -> candidate != ship
                        && candidate.isAlive()
                        && candidate.isPushable()
                        && candidate.isPickable());
        if (!candidates.isEmpty()) {
            ship.beginLegacyRePush(candidates.get(ship.getRandom().nextInt(candidates.size())));
        }
    }

    /** Mirrors EntityBattleshipNGT's 128-tick morale bonus for nearby destroyers and Northern Hime. */
    private static void tickNagatoMoraleEvent(LegacyShipEntity ship) {
        List<LegacyShipEntity> nearby = ship.level().getEntitiesOfClass(
                LegacyShipEntity.class,
                ship.getBoundingBox().inflate(16.0D, 12.0D, 16.0D),
                LegacyShipBehaviorCatalog::isNagatoMoraleTarget);
        if (nearby.isEmpty()) {
            return;
        }

        if (ship.getMorale() < GATTAI_MORALE_THRESHOLD) {
            ship.addMorale(150 * nearby.size());
        }

        if (ship.isOrderedToSit()
                || ship.isPassenger()
                || ship.hasNoFuel()
                || !ship.isLegacyOutOfCombat()
                || ship.hasActiveCommandState()
                || ship.getRandom().nextFloat() <= 0.5F) {
            return;
        }

        ship.getNavigation().moveTo(
                nearby.get(ship.getRandom().nextInt(nearby.size())), 1.0D);
        CombatFxDispatcher.sendLegacyEmotion(ship,
                nagatoMoraleEmotionTypeFor(ship.getRandom().nextInt(5)));
    }

    private static boolean isNagatoMoraleTarget(LegacyShipEntity candidate) {
        return candidate.getSpec().archetype() == ShipArchetype.DESTROYER
                || "EntityNorthernHime".equals(candidate.getSpec().textureStem());
    }

    /** Keeps the light-cruiser skill movement states outside generic AI movement. */
    public static boolean isLegacySkillMovementActive(LegacyShipEntity ship) {
        if (ship == null || ship.getDisplayPhase() <= 0) {
            return false;
        }

        int normalizedClassId = normalizeLegacyClassId(ship.getShipClassId());
        return normalizedClassId == 56 || normalizedClassId == 57;
    }

    /** Mirrors EntityDestroyerNi's two-block step height. */
    public static float stepHeightFor(ShipEntitySpec spec) {
        return spec != null && normalizeLegacyClassId(spec.legacyClassId()) == 3 ? 2.0F : 0.6F;
    }

    public static boolean isTenryuuSkillMovementActive(LegacyShipEntity ship) {
        return ship != null
                && normalizeLegacyClassId(ship.getShipClassId()) == 56
                && ship.getDisplayPhase() > 0;
    }

    public static boolean isTatsutaSkillMovementActive(LegacyShipEntity ship) {
        return ship != null
                && normalizeLegacyClassId(ship.getShipClassId()) == 57
                && ship.getDisplayPhase() > 0;
    }

    /** Mirrors EntityCLTenryuu and EntityCLTenryuuMob's phase/timer state machine. */
    private static void tickTenryuuSkill(LegacyShipEntity ship) {
        int phase = ship.getDisplayPhase();
        if (phase <= 0) {
            return;
        }

        if (ship.isOrderedToSit()) {
            ship.resetLegacySkillState();
            return;
        }

        // EntityCLTenryuu updates the collision window before its skill AI advances the phase.
        if (phase > 1) {
            updateTenryuuSkillEffect(ship, phase);
        }

        LivingEntity target = resolveTenryuuSkillTarget(ship);
        if (target == null) {
            ship.resetLegacySkillState();
            return;
        }

        phase = ship.getDisplayPhase();
        if (phase == 1) {
            if (ship.getLegacySkillRemaining() > 1) {
                updateTenryuuHorizontalAttack(ship, target);
            } else {
                updateTenryuuFinalAttack(ship, target);
            }
        } else if (ship.getLegacySkillTimer() <= 0) {
            if (phase == 2) {
                ship.setDisplayPhase(1);
            } else if (phase == 3) {
                ship.resetLegacySkillState();
                return;
            }
        }

        if (ship.getLegacySkillTimer() > 0) {
            ship.setLegacySkillTimer(ship.getLegacySkillTimer() - 1);
        }
    }

    private static @javax.annotation.Nullable LivingEntity resolveTenryuuSkillTarget(LegacyShipEntity ship) {
        LivingEntity target = ship.getTarget();
        double attackRange = ship.getCompatAttackRange();
        // The legacy helper retained an already selected living target without rechecking faction flags.
        if (target != null && target.isAlive()
                && ship.distanceToSqr(target) <= attackRange * attackRange) {
            return target;
        }

        if (ship.getLegacySkillRemaining() <= 0) {
            return null;
        }

        double searchRange = ship.isHostileVariant() ? 8.0D : 10.0D;
        List<LivingEntity> candidates = ship.level().getEntitiesOfClass(
                LivingEntity.class,
                ship.getBoundingBox().inflate(searchRange, searchRange, searchRange),
                candidate -> candidate != ship
                        && candidate.isAlive()
                        && !candidate.isInvulnerable()
                        && canUseLegacySkillReplacementTarget(ship, candidate));
        if (candidates.isEmpty()) {
            return null;
        }

        LivingEntity replacement = candidates.get(ship.getRandom().nextInt(candidates.size()));
        ship.setTarget(replacement);
        return replacement;
    }

    private static void updateTenryuuSkillEffect(LegacyShipEntity ship, int phase) {
        int timer = ship.getLegacySkillTimer();
        if (timer == 6) {
            ship.clearLegacySkillDamagedTargets();
            sendLaunchSmoke(ship);
        } else if (timer == 3) {
            Vec3 motion = ship.getLegacySkillMotion();
            CombatFxDispatcher.sendParticle(ship, GameplayParticleType.AIR_TRAIL,
                    ship.getX() + motion.x * 2.0D,
                    ship.getY() + ship.getBbHeight() * 0.4D + motion.y * 2.5D,
                    ship.getZ() + motion.z * 2.0D,
                    motion.x, motion.y, motion.z);
        }

        boolean inCollisionWindow = ship.isHostileVariant()
                ? timer <= 12 && timer >= 0
                : timer <= 7 && timer >= 0;
        if (inCollisionWindow) {
            ship.setDeltaMovement(ship.getLegacySkillMotion());
            damageTenryuuNearby(ship, phase);
        } else {
            ship.setDeltaMovement(Vec3.ZERO);
        }
    }

    private static void damageTenryuuNearby(LegacyShipEntity ship, int phase) {
        double horizontalRange = ship.isHostileVariant() ? 1.5D : 2.0D;
        float damageMultiplier;
        if (phase == 2) {
            damageMultiplier = ship.isHostileVariant() ? 0.4F : 0.3F;
        } else {
            damageMultiplier = ship.isHostileVariant() ? 1.0F : 1.2F;
        }

        float rawDamage = ship.getLegacyStats().attackHeavy() * damageMultiplier;
        for (LivingEntity target : ship.level().getEntitiesOfClass(
                LivingEntity.class,
                ship.getBoundingBox().inflate(horizontalRange, 1.5D, horizontalRange),
                candidate -> candidate != ship
                        && candidate.isAlive()
                        && !candidate.isInvulnerable()
                        && ship.canEngage(candidate))) {
            if (ship.hasLegacySkillDamagedTarget(target.getId())) {
                continue;
            }
            ship.markLegacySkillDamagedTarget(target.getId());

            float damage = LegacyShipCombatHelper.applyTargetDamageModifier(
                    ship.getLegacyStats(), target, rawDamage);
            boolean attacked = ship.performCatalogDirectAttack(
                    target,
                    LegacyShipCombatHelper.rollDirectAttack(ship, target, damage),
                    false,
                    false,
                    LegacyShipProjectileVisual.NONE,
                    GameplayParticleType.HIT_EXPLOSION);
            if (attacked && target.isPushable()) {
                double yaw = ship.getYRot() * (Math.PI / 180.0D);
                double horizontal = target instanceof LegacyShipEntity ? 0.02D : 0.05D;
                target.push(-Math.sin(yaw) * horizontal,
                        target instanceof LegacyShipEntity ? 0.2D : 0.4D,
                        Math.cos(yaw) * horizontal);
            }
        }
    }

    private static void updateTenryuuHorizontalAttack(LegacyShipEntity ship, LivingEntity target) {
        BlockPos pos = findLegacyRandomSafePosition(target);
        Vec3 destination = new Vec3(pos.getX() + 0.5D, pos.getY(), pos.getZ() + 0.5D);
        Vec3 direction = destination.subtract(target.position());
        Vec3 motion = direction.lengthSqr() <= 1.0E-4D
                ? Vec3.ZERO : direction.normalize().scale(-1.25D);
        ship.setLegacySkillMotion(motion);
        faceLegacyDirection(ship, motion);
        ship.getNavigation().stop();
        ship.moveTo(destination.x, destination.y, destination.z, ship.getYRot(), ship.getXRot());
        ship.setLegacySkillRemaining(ship.getLegacySkillRemaining() - 1);
        ship.setLegacySkillTimer(7);
        ship.setDisplayPhase(2);
    }

    private static void updateTenryuuFinalAttack(LegacyShipEntity ship, LivingEntity target) {
        BlockPos pos = findLegacyTopSafePosition(target);
        Vec3 destination = new Vec3(pos.getX() + 0.5D, pos.getY(), pos.getZ() + 0.5D);
        Vec3 motion = new Vec3(0.0D,
                -Math.abs(destination.y - target.getY()) * 0.25D,
                0.0D);
        ship.setLegacySkillMotion(motion);
        ship.getNavigation().stop();
        ship.moveTo(destination.x, destination.y, destination.z, ship.getYRot(), ship.getXRot());
        ship.setLegacySkillRemaining(ship.getLegacySkillRemaining() - 1);
        ship.setLegacySkillTimer(9);
        ship.setDisplayPhase(3);
    }

    /** Keeps the old BlockHelper's target-block acceptance and integer angle offsets. */
    private static BlockPos findLegacyRandomSafePosition(LivingEntity target) {
        BlockPos origin = target.blockPosition();
        int loops = 20;
        while (loops-- > 0) {
            double angle = target.getRandom().nextFloat() * (Math.PI * 2.0D);
            int zOffset = (int) (6.0D * Math.cos(angle));
            int xOffset = (int) (-6.0D * Math.sin(angle));
            BlockPos candidate = origin.offset(xOffset, 0, zOffset);
            if (isLegacySafeTargetBlock(target.level(), candidate)) {
                return candidate;
            }
        }
        return origin;
    }

    private static BlockPos findLegacyTopSafePosition(LivingEntity target) {
        BlockPos origin = target.blockPosition();
        int loops = (int) target.getBbHeight() + 7;
        while (loops-- > 0) {
            BlockPos candidate = origin.offset(0, loops, 0);
            if (isLegacySafeTargetBlock(target.level(), candidate)) {
                return candidate;
            }
        }
        return origin;
    }

    private static boolean isLegacySafeTargetBlock(Level level, BlockPos pos) {
        var state = level.getBlockState(pos);
        return state.isAir()
                || !state.getFluidState().isEmpty()
                || state.getCollisionShape(level, pos).isEmpty();
    }

    private static void tickTatsutaSkill(LegacyShipEntity ship) {
        int phase = ship.getDisplayPhase();
        if (phase <= 0) {
            return;
        }

        if (ship.isOrderedToSit()) {
            ship.resetLegacySkillState();
            return;
        }

        LivingEntity target = resolveTatsutaSkillTarget(ship);
        if (target == null) {
            ship.resetLegacySkillState();
            return;
        }

        if (ship.getLegacySkillTimer() <= 0) {
            if (phase == 3) {
                ship.resetLegacySkillState();
                return;
            }
            phase++;
            ship.setDisplayPhase(phase);
        }

        switch (phase) {
            case 1 -> updateTatsutaCharge(ship, target);
            case 2 -> updateTatsutaWave(ship);
            case 3 -> updateTatsutaFinalAttack(ship, target);
            default -> ship.resetLegacySkillState();
        }

        if (ship.getLegacySkillTimer() > 0) {
            ship.setLegacySkillTimer(ship.getLegacySkillTimer() - 1);
        }
    }

    private static @javax.annotation.Nullable LivingEntity resolveTatsutaSkillTarget(LegacyShipEntity ship) {
        LivingEntity target = ship.getTarget();
        double attackRange = ship.getCompatAttackRange();
        if (target != null && target.isAlive()
                && ship.distanceToSqr(target) <= attackRange * attackRange) {
            return target;
        }

        if (ship.getLegacySkillRemaining() <= 0) {
            return null;
        }

        double searchRange = ship.isHostileVariant() ? 13.0D : 16.0D;
        List<LivingEntity> candidates = ship.level().getEntitiesOfClass(
                LivingEntity.class,
                ship.getBoundingBox().inflate(searchRange, searchRange, searchRange),
                candidate -> candidate != ship
                        && candidate.isAlive()
                        && !candidate.isInvulnerable()
                        && canUseLegacySkillReplacementTarget(ship, candidate));
        if (candidates.isEmpty()) {
            return null;
        }

        LivingEntity replacement = candidates.get(ship.getRandom().nextInt(candidates.size()));
        ship.setTarget(replacement);
        return replacement;
    }

    private static void updateTatsutaCharge(LegacyShipEntity ship, LivingEntity target) {
        int timer = ship.getLegacySkillTimer();
        if (timer == 8) {
            Vec3 direction = target.position().subtract(ship.position());
            Vec3 motion = direction.scale(0.14D);
            ship.setLegacySkillMotion(motion);
            ship.setDeltaMovement(motion);
            faceLegacyDirection(ship, direction);
            CombatFxDispatcher.sendParticle(ship, GameplayParticleType.LAUNCH_SMOKE,
                    ship.getX(), ship.getY() + ship.getBbHeight() * 0.7D, ship.getZ());
        } else {
            ship.setDeltaMovement(ship.getLegacySkillMotion());
        }

        if (timer == 6) {
            Vec3 motion = ship.getLegacySkillMotion();
            CombatFxDispatcher.sendParticle(ship, GameplayParticleType.AIR_TRAIL,
                    ship.getX() + motion.x * 2.0D,
                    ship.getY() + motion.y * 2.0D + ship.getBbHeight() * 0.7D,
                    ship.getZ() + motion.z * 2.0D,
                    motion.x, motion.y, motion.z);
        }
    }

    private static void updateTatsutaWave(LegacyShipEntity ship) {
        int timer = ship.getLegacySkillTimer();
        if (timer <= 0) {
            ship.setLegacySkillMotion(new Vec3(0.0D,
                    0.3D + (ship.isHostileVariant() ? ship.getLegacyHostileScaleLevel() * 0.1D : 0.0D),
                    0.0D));
            ship.setLegacySkillTimer(25);
            CombatFxDispatcher.sendParticle(ship, GameplayParticleType.LAUNCH_SMOKE,
                    ship.getX(), ship.getY() + ship.getBbHeight() * 0.7D, ship.getZ());
            timer = 25;
        }

        ship.setDeltaMovement(ship.getLegacySkillMotion());
        damageTatsutaNearby(ship);

        if ((timer & 1) == 0) {
            Vec3 motion = ship.getLegacySkillMotion();
            CombatFxDispatcher.sendParticle(ship, GameplayParticleType.AIR_TRAIL,
                    ship.getX(), ship.getY() + ship.getBbHeight() * 0.7D, ship.getZ(),
                    motion.x, motion.y, motion.z);
        }

        if ((timer & 7) == 0) {
            ship.setLegacySkillRemaining(ship.getLegacySkillRemaining() - 1);
            ship.clearLegacySkillDamagedTargets();
            if (ship.getLegacySkillRemaining() <= 1) {
                ship.setLegacySkillTimer(0);
            }
        }
    }

    private static void damageTatsutaNearby(LegacyShipEntity ship) {
        float rawDamage = ship.getLegacyStats().attackHeavy() * 0.5F;
        for (LivingEntity target : ship.level().getEntitiesOfClass(
                LivingEntity.class,
                ship.getBoundingBox().inflate(4.0D, 3.0D, 4.0D),
                candidate -> candidate != ship
                        && candidate.isAlive()
                        && !candidate.isInvulnerable()
                        && ship.canEngage(candidate))) {
            if (ship.hasLegacySkillDamagedTarget(target.getId())) {
                continue;
            }
            ship.markLegacySkillDamagedTarget(target.getId());

            float damage = LegacyShipCombatHelper.applyTargetDamageModifier(
                    ship.getLegacyStats(), target, rawDamage);
            boolean attacked = ship.performCatalogDirectAttack(
                    target,
                    LegacyShipCombatHelper.rollDirectAttack(ship, target, damage),
                    false,
                    true,
                    LegacyShipProjectileVisual.NONE,
                    GameplayParticleType.HIT_EXPLOSION);
            if (attacked && target.isPushable()) {
                boolean isShip = target instanceof LegacyShipEntity;
                if (ship.isHostileVariant()) {
                    double yaw = ship.getYRot() * (Math.PI / 180.0D);
                    double horizontal = isShip ? 0.02D : 0.05D;
                    target.push(-Math.sin(yaw) * horizontal,
                            isShip ? 0.2D : 0.4D,
                            Math.cos(yaw) * horizontal);
                } else {
                    target.push(0.0D, isShip ? 0.25D : 0.5D, 0.0D);
                }
            }
        }
    }

    private static void updateTatsutaFinalAttack(LegacyShipEntity ship, LivingEntity target) {
        int timer = ship.getLegacySkillTimer();
        if (timer <= 0) {
            Vec3 direction = target.position().subtract(
                    ship.position().add(0.0D, 1.0D, 0.0D));
            ship.setLegacySkillMotion(direction.lengthSqr() <= 1.0E-4D
                    ? Vec3.ZERO : direction.normalize());
            ship.setLegacySkillRemaining(0);
            ship.setLegacySkillTimer(15);
            faceLegacyDirection(ship, direction);
            CombatFxDispatcher.sendParticle(ship, GameplayParticleType.LAUNCH_SMOKE,
                    ship.getX(), ship.getY() + ship.getBbHeight() * 0.7D, ship.getZ());
        }

        ship.setDeltaMovement(new Vec3(0.0D, 0.1D, 0.0D));
        if (timer == 6) {
            LegacyShipBeamEntity beam = LegacyShipBeamEntity.createGaeBolg(
                    ship.level(), ship, ship.getLegacySkillMotion(),
                    ship.getLegacyStats().attackHeavy() * 1.5F);
            if (beam != null) {
                ship.level().addFreshEntity(beam);
                CombatFxDispatcher.sendCombatReact(ship, target, CombatReactType.LAUNCH,
                        LegacyShipAttackKind.HEAVY, LegacyShipProjectileVisual.NONE);
            }
        }
        if (timer == 4) {
            Vec3 motion = ship.getLegacySkillMotion();
            CombatFxDispatcher.sendParticle(ship, GameplayParticleType.BEAM_TRAIL,
                    ship.getX() + motion.x * 10.0D,
                    ship.getY() + motion.y * 10.0D + ship.getBbHeight() * 0.7D,
                    ship.getZ() + motion.z * 10.0D,
                    motion.x, motion.y, motion.z);
        }
    }

    private static void faceLegacyDirection(LegacyShipEntity ship, Vec3 direction) {
        if (direction.lengthSqr() <= 1.0E-4D) {
            return;
        }
        float yaw = (float) (Mth.atan2(-direction.x, direction.z) * (180.0D / Math.PI));
        ship.setYRot(yaw);
        ship.setYHeadRot(yaw);
    }

    private static void tickAkatsukiGattai(LegacyShipEntity ship) {
        if (ship.isHostileVariant()) {
            return;
        }

        // EntityDestroyerAkatsuki.updateFuelState dismounts every rider as soon
        // as the legacy no-fuel flag is raised.
        if (ship.hasNoFuel()) {
            dismountSixDestroyerRiders(ship);
            return;
        }

        int riderType = sixDestroyerRiderMask(ship);
        if (!isValidSixDestroyerRiderMask(riderType)) {
            dismountSixDestroyerRiders(ship);
            return;
        }

        ship.setLegacyRiderType(riderType);
        ship.setLegacyRidingState((riderType & 1) != 0 ? 1 : 0);
        ship.setLegacyRaiden(false);

        if (riderType > 0) {
            addGattaiMorale(ship);
            for (Entity passenger : new ArrayList<>(ship.getPassengers())) {
                if (passenger instanceof LegacyShipEntity rider && !rider.isHostileVariant()) {
                    rider.setLegacyRiderType(riderType);
                    syncSixDestroyerRiderState(rider, riderType, false);
                    addGattaiMorale(rider);
                }
            }
        }

        if ((ship.tickCount & 127) == 0) {
            trySixDestroyerGattai(ship);
        }
    }

    private static void tickHibikiGattaiState(LegacyShipEntity ship) {
        if (ship.isHostileVariant()) {
            return;
        }

        LegacyShipEntity host = friendlyVehicleOfClass(ship, 51);
        if (ship.hasNoFuel()) {
            if (host != null) {
                dismountSixDestroyerRiders(host);
            }
            return;
        }

        int riderType = host == null ? 0 : host.getLegacyRiderType();
        ship.setLegacyRiderType(riderType);
        ship.setLegacyRaiden(false);
        syncSixDestroyerRiderState(ship, riderType, false);
    }

    private static void tickIkazuchiGattaiState(LegacyShipEntity ship) {
        if (ship.isHostileVariant()) {
            return;
        }

        LegacyShipEntity sixDestroyerHost = friendlyVehicleOfClass(ship, 51);
        if (ship.hasNoFuel()) {
            if (sixDestroyerHost != null) {
                dismountSixDestroyerRiders(sixDestroyerHost);
            }
            if (ship.isLegacyRaiden()) {
                dismountRaidenPair(ship);
            }
            return;
        }

        int riderType = sixDestroyerHost == null ? 0 : sixDestroyerHost.getLegacyRiderType();
        boolean raiden = friendlyVehicleOfClass(ship, 54) != null;
        ship.setLegacyRiderType(riderType);
        ship.setLegacyRaiden(raiden);
        syncSixDestroyerRiderState(ship, riderType, raiden);

        if (riderType == 0 && raiden) {
            addGattaiMorale(ship);
        }
        if ((ship.tickCount & 127) == 0) {
            tryRaidenGattai(ship, 54);
        }
    }

    /** Mirrors the hostile Mob-only Raiden pairing, which has no friendly gates. */
    private static void tickHostileRaidenBehavior(LegacyShipEntity ship) {
        int normalizedClassId = normalizeLegacyClassId(ship.getShipClassId());
        if (normalizedClassId == 53) {
            boolean raiden = hostileVehicleOfClass(ship, 54) != null;
            ship.setLegacyRaiden(raiden);
            ship.setLegacyRidingState(raiden ? 1 : 0);
            if ((ship.tickCount & 127) == 0) {
                tryHostileRaidenGattai(ship);
            }
        } else if (normalizedClassId == 54) {
            boolean raiden = hasHostilePassengerOfClass(ship, 53);
            ship.setLegacyRaiden(raiden);
            ship.setLegacyRidingState(raiden ? 2 : 0);
        }
    }

    private static void tickInazumaGattaiState(LegacyShipEntity ship) {
        if (ship.isHostileVariant()) {
            return;
        }

        LegacyShipEntity sixDestroyerHost = friendlyVehicleOfClass(ship, 51);
        if (ship.hasNoFuel()) {
            if (sixDestroyerHost != null) {
                dismountSixDestroyerRiders(sixDestroyerHost);
            }
            if (ship.isLegacyRaiden()) {
                dismountRaidenPair(ship);
            }
            return;
        }

        int riderType = sixDestroyerHost == null ? 0 : sixDestroyerHost.getLegacyRiderType();
        boolean raiden = hasFriendlyPassengerOfClass(ship, 53);
        ship.setLegacyRiderType(riderType);
        ship.setLegacyRaiden(raiden);
        syncSixDestroyerRiderState(ship, riderType, raiden);

        if (riderType == 0 && raiden) {
            addGattaiMorale(ship);
        }
        if ((ship.tickCount & 127) == 0) {
            tryRaidenGattai(ship, 53);
        }
    }

    private static int sixDestroyerRiderMask(LegacyShipEntity host) {
        int riderType = 0;
        for (Entity passenger : host.getPassengers()) {
            if (!(passenger instanceof LegacyShipEntity rider) || rider.isHostileVariant()) {
                continue;
            }

            switch (normalizeLegacyClassId(rider.getShipClassId())) {
                case 52 -> riderType |= 1;
                case 54 -> riderType |= 2;
                case 53 -> riderType |= 4;
                default -> {
                }
            }
        }
        return riderType;
    }

    private static boolean isValidSixDestroyerRiderMask(int riderType) {
        return riderType == 0 || riderType == 1 || riderType == 3 || riderType == 7;
    }

    private static void syncSixDestroyerRiderState(LegacyShipEntity ship, int riderType, boolean raiden) {
        switch (normalizeLegacyClassId(ship.getShipClassId())) {
            case 52 -> ship.setLegacyRidingState(riderType > 1 ? 2 : riderType == 1 ? 1 : 0);
            case 53 -> ship.setLegacyRidingState(riderType == 7 || raiden ? 2 : 0);
            case 54 -> ship.setLegacyRidingState(riderType == 7 ? 3 : raiden ? 2 : riderType == 3 ? 1 : 0);
            default -> {
            }
        }
    }

    private static void dismountSixDestroyerRiders(LegacyShipEntity host) {
        host.setLegacyRiderType(0);
        host.setLegacyRidingState(0);
        host.setLegacyRaiden(false);

        for (Entity passenger : new ArrayList<>(host.getPassengers())) {
            if (passenger instanceof LegacyShipEntity rider) {
                rider.setLegacyRiderType(0);
                rider.setLegacyRidingState(0);
                rider.setLegacyRaiden(false);
            }
            passenger.stopRiding();
        }
    }

    private static void dismountRaidenPair(LegacyShipEntity ship) {
        int normalizedClassId = normalizeLegacyClassId(ship.getShipClassId());
        if (normalizedClassId == 53) {
            LegacyShipEntity inazuma = friendlyVehicleOfClass(ship, 54);
            if (inazuma != null) {
                ship.setLegacyRaiden(false);
                ship.setLegacyRidingState(0);
                inazuma.setLegacyRaiden(false);
                inazuma.setLegacyRidingState(0);
                ship.stopRiding();
            }
            return;
        }

        if (normalizedClassId == 54) {
            for (Entity passenger : new ArrayList<>(ship.getPassengers())) {
                if (passenger instanceof LegacyShipEntity ikazuchi
                        && !ikazuchi.isHostileVariant()
                        && normalizeLegacyClassId(ikazuchi.getShipClassId()) == 53) {
                    ship.setLegacyRaiden(false);
                    ship.setLegacyRidingState(0);
                    ikazuchi.setLegacyRaiden(false);
                    ikazuchi.setLegacyRidingState(0);
                    ikazuchi.stopRiding();
                }
            }
        }
    }

    private static void addGattaiMorale(LegacyShipEntity ship) {
        if (ship.getMorale() < GATTAI_MORALE_THRESHOLD) {
            ship.addMorale(100);
        }
    }

    private static void trySixDestroyerGattai(LegacyShipEntity host) {
        if (host.isHostileVariant()
                || host.isPassenger()
                || host.isOrderedToSit()
                || host.hasNoFuel()
                || host.getLegacyRiderType() == 7
                || host.getHealth() <= host.getMaxHealth() * 0.5F
                || host.getLegacyFormationId() != 1) {
            return;
        }

        List<LegacyShipEntity> nearby = host.level().getEntitiesOfClass(
                LegacyShipEntity.class,
                host.getBoundingBox().inflate(6.0D, 5.0D, 6.0D),
                candidate -> isEligibleSixDestroyerMember(host, candidate));
        LegacyShipEntity hibiki = firstLegacyClass(nearby, 52);
        LegacyShipEntity inazuma = firstLegacyClass(nearby, 54);
        LegacyShipEntity ikazuchi = firstLegacyClass(nearby, 53);

        switch (host.getLegacyRiderType()) {
            case 0 -> {
                if (hibiki != null && hibiki.startRiding(host, true)) {
                    if (inazuma != null) {
                        inazuma.startRiding(host, true);
                    }
                    if (ikazuchi != null && inazuma != null) {
                        ikazuchi.startRiding(host, true);
                    }
                }
            }
            case 1 -> {
                if (inazuma != null && inazuma.startRiding(host, true) && ikazuchi != null) {
                    ikazuchi.startRiding(host, true);
                }
            }
            case 3 -> {
                if (ikazuchi != null) {
                    ikazuchi.startRiding(host, true);
                }
            }
            default -> {
            }
        }
    }

    private static boolean isEligibleSixDestroyerMember(LegacyShipEntity host, LegacyShipEntity candidate) {
        int classId = normalizeLegacyClassId(candidate.getShipClassId());
        return candidate != host
                && !candidate.isHostileVariant()
                && (classId == 52 || classId == 53 || classId == 54)
                && hasSameLegacyOwner(host, candidate)
                && candidate.isAlive()
                && !candidate.isPassenger()
                && !candidate.isOrderedToSit()
                && !candidate.hasNoFuel()
                && candidate.getLegacyRiderType() == 0
                && !candidate.isLegacyRaiden()
                && candidate.getLegacyFormationId() == 1;
    }

    private static void tryRaidenGattai(LegacyShipEntity ship, int partnerClassId) {
        if (ship.isHostileVariant()
                || ship.isPassenger()
                || ship.isOrderedToSit()
                || ship.hasNoFuel()
                || ship.getLegacyRiderType() > 0
                || ship.isLegacyRaiden()
                || ship.getHealth() <= ship.getMaxHealth() * 0.5F) {
            return;
        }

        List<LegacyShipEntity> nearby = ship.level().getEntitiesOfClass(
                LegacyShipEntity.class,
                ship.getBoundingBox().inflate(4.0D, 4.0D, 4.0D),
                candidate -> candidate != ship
                        && !candidate.isHostileVariant()
                        && normalizeLegacyClassId(candidate.getShipClassId()) == partnerClassId
                        && hasSameLegacyOwner(ship, candidate)
                        && candidate.isAlive()
                        && !candidate.isPassenger()
                        && !candidate.isOrderedToSit()
                        && !candidate.hasNoFuel()
                        && candidate.getLegacyRiderType() == 0
                        && !candidate.isLegacyRaiden());
        if (nearby.isEmpty()) {
            return;
        }

        LegacyShipEntity partner = nearby.get(0);
        if (ship.startRiding(partner, true)) {
            ship.setLegacyRaiden(true);
            partner.setLegacyRaiden(true);
            ship.setLegacyRidingState(2);
            partner.setLegacyRidingState(2);
        }
    }

    private static void tryHostileRaidenGattai(LegacyShipEntity ship) {
        if (ship.isLegacyRaiden()) {
            return;
        }

        List<LegacyShipEntity> nearby = ship.level().getEntitiesOfClass(
                LegacyShipEntity.class,
                ship.getBoundingBox().inflate(16.0D, 8.0D, 16.0D),
                candidate -> candidate != ship
                        && candidate.isHostileVariant()
                        && normalizeLegacyClassId(candidate.getShipClassId()) == 54
                        && candidate.isAlive()
                        && !candidate.isLegacyRaiden()
                        && candidate.getLegacyHostileScaleLevel() == ship.getLegacyHostileScaleLevel());
        if (nearby.isEmpty()) {
            return;
        }

        LegacyShipEntity partner = nearby.get(0);
        if (ship.startRiding(partner, true)) {
            ship.setLegacyRaiden(true);
            partner.setLegacyRaiden(true);
            ship.setLegacyRidingState(1);
            partner.setLegacyRidingState(2);
        }
    }

    private static LegacyShipEntity friendlyVehicleOfClass(LegacyShipEntity ship, int classId) {
        Entity vehicle = ship.getVehicle();
        if (vehicle instanceof LegacyShipEntity candidate
                && !candidate.isHostileVariant()
                && normalizeLegacyClassId(candidate.getShipClassId()) == classId) {
            return candidate;
        }
        return null;
    }

    private static LegacyShipEntity hostileVehicleOfClass(LegacyShipEntity ship, int classId) {
        Entity vehicle = ship.getVehicle();
        if (vehicle instanceof LegacyShipEntity candidate
                && candidate.isHostileVariant()
                && normalizeLegacyClassId(candidate.getShipClassId()) == classId) {
            return candidate;
        }
        return null;
    }

    private static boolean hasFriendlyPassengerOfClass(LegacyShipEntity ship, int classId) {
        for (Entity passenger : ship.getPassengers()) {
            if (passenger instanceof LegacyShipEntity candidate
                    && !candidate.isHostileVariant()
                    && normalizeLegacyClassId(candidate.getShipClassId()) == classId) {
                return true;
            }
        }
        return false;
    }

    private static boolean hasHostilePassengerOfClass(LegacyShipEntity ship, int classId) {
        for (Entity passenger : ship.getPassengers()) {
            if (passenger instanceof LegacyShipEntity candidate
                    && candidate.isHostileVariant()
                    && normalizeLegacyClassId(candidate.getShipClassId()) == classId) {
                return true;
            }
        }
        return false;
    }

    private static LegacyShipEntity firstLegacyClass(List<LegacyShipEntity> ships, int classId) {
        for (LegacyShipEntity ship : ships) {
            if (normalizeLegacyClassId(ship.getShipClassId()) == classId) {
                return ship;
            }
        }
        return null;
    }

    private static boolean hasSameLegacyOwner(LegacyShipEntity first, LegacyShipEntity second) {
        return first.getOwnerUuid().isPresent() && first.getOwnerUuid().equals(second.getOwnerUuid());
    }

    public static boolean hasCombatHook(ShipEntitySpec spec) {
        return behaviorFor(spec).hasCombatHook();
    }

    public static boolean shouldUseCombatHook(ShipEntitySpec spec, LegacyShipAttackKind attackKind) {
        Behavior behavior = behaviorFor(spec);
        if (!behavior.hasCombatHook()) {
            return false;
        }

        if (behavior.combatHook() == CombatHook.CARRIER_HIME_MELEE_MISSILE) {
            return attackKind == LegacyShipAttackKind.MELEE;
        }
        if (behavior.combatHook() == CombatHook.NORTHERN_HIME_FLOATING_FORT) {
            return attackKind == LegacyShipAttackKind.HEAVY;
        }
        if (behavior.combatHook() == CombatHook.BATTLESHIP_TA_RENSOUHOU) {
            // EntityBattleshipTa exposes only attackEntityWithAmmo; do not invent
            // a generic heavy entry for the inherited but unused legacy flag.
            return attackKind == LegacyShipAttackKind.LIGHT;
        }

        int normalizedClassId = normalizeLegacyClassId(spec.legacyClassId());
        return attackKind == LegacyShipAttackKind.HEAVY
                || attackKind == LegacyShipAttackKind.LIGHT
                && (normalizedClassId == 36 || normalizedClassId == 14
                || normalizedClassId == 44 || normalizedClassId == 72);
    }

    /** Mirrors the only legacy ship overrides that accepted a ground BlockPos heavy target. */
    public static boolean supportsBlockTargetAttack(ShipEntitySpec spec, LegacyShipAttackKind attackKind) {
        if (spec == null || attackKind != LegacyShipAttackKind.HEAVY) {
            return false;
        }

        return switch (normalizeLegacyClassId(spec.legacyClassId())) {
            case 13, 36 -> true;
            default -> false;
        };
    }

    public static List<AiPriority> singlePlayerAiPriorityOrder() {
        return List.of(
                AiPriority.DEATH_STOP_SIT,
                AiPriority.EXPLICIT_COMMAND,
                AiPriority.ACTIVE_COMBAT_TARGET,
                AiPriority.AUTO_SUPPLY,
                AiPriority.PICKUP,
                AiPriority.IDLE_FOLLOW);
    }

    public static CombatHookResult performCombatHook(LegacyShipEntity ship, LivingEntity target,
                                                     LegacyShipAttackKind attackKind) {
        return performCombatHook(ship, target, attackKind, true);
    }

    /**
     * Runs a legacy combat hook with an optional entity-level cooldown gate.
     * EntityAIShipGuarding owns its three channel delays, so that AI must not
     * consume or inspect the normal ranged-AI cooldown state.
     */
    public static CombatHookResult performCombatHook(LegacyShipEntity ship, LivingEntity target,
                                                     LegacyShipAttackKind attackKind,
                                                     boolean respectEntityCooldown) {
        Behavior behavior = behaviorFor(ship.getSpec());
        if (!shouldUseCombatHook(ship.getSpec(), attackKind)
                || !behavior.hasCombatHook()
                || respectEntityCooldown && ship.getCompatAttackCooldown(attackKind) > 0
                || !ship.canEngage(target)) {
            return CombatHookResult.NOT_HANDLED;
        }

        CombatHookResult result = switch (behavior.combatHook()) {
            case SHIMAKAZE_TORPEDO_BURST -> attackKind == LegacyShipAttackKind.LIGHT
                    ? performShimakazeSummon(ship, target)
                    : performShimakazeBurst(ship, target);
            case NAGATO_HEAVY_STRIKE -> performNagatoStrike(ship, target);
            case YAMATO_BEAM_BARRAGE -> performYamatoBeam(ship, target);
            case TENRYUU_DASH -> performTenryuuDash(ship, target);
            case TATSUTA_CONTROL_AOE -> performTatsutaControl(ship, target);
            case HEAVY_CRUISER_BARRAGE -> hookResult(performHeavyCruiserBarrage(ship, target));
            case KONGOU_COMBO -> hookResult(performKongouCombo(ship, target));
            case BATTLESHIP_RU_BARRAGE -> performBattleshipRuBarrage(ship, target);
            case BATTLESHIP_TA_RENSOUHOU -> performBattleshipTaSummon(ship, target);
            case SUBMARINE_HIME_DOUBLE_MISSILE -> performSubmarineHimeDoubleMissile(ship, target);
            case CARRIER_HIME_MELEE_MISSILE -> performCarrierHimeMeleeMissile(ship, target);
            case NORTHERN_HIME_FLOATING_FORT -> performNorthernHimeFloatingFort(ship, target);
            case NONE -> CombatHookResult.NOT_HANDLED;
        };

        if (result.handled() && respectEntityCooldown) {
            int baseDelay = LegacyShipCombatHelper.attackDelay(ship, attackKind);
            ship.setCatalogAttackCooldown(attackKind,
                    Math.max(8, Math.round(baseDelay * cooldownMultiplier(behavior.combatHook()))));
        }
        return result;
    }

    /** Runs the legacy heavy attack override that targets a fixed block position. */
    public static CombatHookResult performBlockTargetCombatHook(LegacyShipEntity ship,
                                                                BlockPos targetPosition,
                                                                LegacyShipAttackKind attackKind,
                                                                boolean respectEntityCooldown) {
        if (targetPosition == null
                || !supportsBlockTargetAttack(ship.getSpec(), attackKind)
                || respectEntityCooldown && ship.getCompatAttackCooldown(attackKind) > 0) {
            return CombatHookResult.NOT_HANDLED;
        }

        Behavior behavior = behaviorFor(ship.getSpec());
        CombatHookResult result = switch (behavior.combatHook()) {
            case SHIMAKAZE_TORPEDO_BURST -> performShimakazeBlockBurst(ship, targetPosition);
            case BATTLESHIP_RU_BARRAGE -> performBattleshipRuBlockBarrage(ship, targetPosition);
            default -> CombatHookResult.NOT_HANDLED;
        };

        if (result.handled() && respectEntityCooldown) {
            int baseDelay = LegacyShipCombatHelper.attackDelay(ship, attackKind);
            ship.setCatalogAttackCooldown(attackKind,
                    Math.max(8, Math.round(baseDelay * cooldownMultiplier(behavior.combatHook()))));
        }
        return result;
    }

    private static CombatHookResult hookResult(boolean handled) {
        return handled ? CombatHookResult.HANDLED : CombatHookResult.NOT_HANDLED;
    }

    public static BossPhaseProfile bossPhaseProfile(ShipEntitySpec spec) {
        int legacyClassId = spec.legacyClassId();
        if (spec.archetype() == ShipArchetype.INSTALLATION) {
            int summonEggMeta = legacyClassId == 21 ? 14 : 18;
            return new BossPhaseProfile(
                    List.of(BossActionType.AREA_BOMBARD, BossActionType.SUMMON_ESCORT, BossActionType.CANNON_BURST),
                    summonEggMeta,
                    40,
                    160);
        }

        if (legacyClassId == 30 || legacyClassId == 33 || legacyClassId == 49) {
            return new BossPhaseProfile(
                    List.of(BossActionType.AIR_ASSAULT, BossActionType.SUMMON_ESCORT, BossActionType.CANNON_BURST),
                    14,
                    34,
                    140);
        }

        if (spec.archetype() == ShipArchetype.PRINCESS) {
            int summonEggMeta = legacyClassId == 44 ? 19 : 2;
            return new BossPhaseProfile(
                    List.of(BossActionType.CANNON_BURST, BossActionType.CHARGE, BossActionType.SUMMON_ESCORT),
                    summonEggMeta,
                    36,
                    180);
        }

        return new BossPhaseProfile(
                List.of(BossActionType.CANNON_BURST, BossActionType.CHARGE),
                2,
                54,
                220);
    }

    public static HostileLootProfile hostileLootProfile(ShipEntitySpec spec, boolean elite, boolean boss) {
        boolean dropsAbyssMetal = elite
                || spec.archetype() == ShipArchetype.CARRIER
                || spec.archetype() == ShipArchetype.BATTLESHIP;
        boolean dropsAbyssMetal1 = spec.archetype() == ShipArchetype.INSTALLATION || boss;
        int scaleLevel = boss ? elite ? 3 : 2 : elite ? 1 : 0;
        float eggDropChance = switch (scaleLevel) {
            case 1 -> 0.33F;
            case 2 -> 0.9F;
            case 3 -> 1.0F;
            default -> 0.2F;
        };
        return new HostileLootProfile(dropsAbyssMetal, dropsAbyssMetal1, boss, eggDropChance);
    }

    public static int hostileSpawnLevel(ShipEntitySpec spec, boolean elite, boolean boss) {
        if (!spec.hostile()) {
            return 1;
        }

        int base = switch (spec.archetype()) {
            case DESTROYER, SUBMARINE -> 18;
            case TRANSPORT, CRUISER -> 24;
            case CARRIER, BATTLESHIP -> 32;
            case PRINCESS -> 48;
            case INSTALLATION -> 52;
        };
        if (elite) {
            base += 12;
        }
        if (boss) {
            base += 28;
        }
        if (spec.eggMeta() >= 2000) {
            base += 8;
        }
        return Math.max(1, Math.min(100, base));
    }

    public static int hostileSpawnMorale(boolean elite, boolean boss) {
        if (boss) {
            return 9000;
        }
        if (elite) {
            return 7000;
        }
        return 5200;
    }

    public static void tickMarriagePassive(LegacyShipEntity ship) {
        Behavior behavior = behaviorFor(ship.getSpec());
        if (!behavior.hasMarriagePassive()) {
            return;
        }

        int shipLevel = ship.getShipLevel();
        switch (behavior.marriagePassive()) {
            case SELF_AND_OWNER_INVISIBILITY -> {
                ship.applyLegacyRingEffect(ship, MobEffects.INVISIBILITY, 40 + shipLevel, 0);
                ship.applyOwnerRingEffect(MobEffects.INVISIBILITY, 40 + shipLevel, 0);
            }
            case ALLIED_JUMP_AURA -> ship.applyCarrierRingAura(50 + shipLevel, shipLevel / 85);
            case ALLIED_STRENGTH_AURA -> ship.applyAlliedRingAura(MobEffects.DAMAGE_BOOST, 50 + shipLevel, shipLevel / 70);
            case ALLIED_RESISTANCE_FIRE_AURA -> {
                int amplifier = shipLevel / 70;
                ship.applyAlliedRingAura(MobEffects.DAMAGE_RESISTANCE, 50 + shipLevel, amplifier);
                ship.applyAlliedRingAura(MobEffects.FIRE_RESISTANCE, 50 + shipLevel, amplifier);
            }
            case ALLIED_HEALTH_BOOST_AURA -> ship.applyAlliedRingAura(MobEffects.HEALTH_BOOST, 140 + shipLevel, shipLevel / 120);
            case ALLIED_SATURATION_AURA -> ship.applyAlliedRingAura(MobEffects.SATURATION, 100 + shipLevel * 2, shipLevel / 120);
            case ALLIED_LUCK_AURA -> ship.applyAlliedRingAura(MobEffects.LUCK, 100 + shipLevel, shipLevel / 120);
            case ALLIED_NIGHT_VISION_AURA -> ship.applyAlliedRingAura(MobEffects.NIGHT_VISION, 100 + shipLevel, 0);
            case OWNER_HASTE -> ship.applyOwnerRingEffect(MobEffects.DIG_SPEED, 80 + shipLevel, shipLevel / 30);
            case OWNER_NIGHT_VISION -> ship.applyOwnerRingEffect(MobEffects.NIGHT_VISION, 100 + shipLevel, 0);
            case OWNER_JUMP -> ship.applyOwnerRingEffect(MobEffects.JUMP, 80 + shipLevel, shipLevel / 45 + 1);
            case OWNER_STRENGTH -> ship.applyOwnerRingEffect(MobEffects.DAMAGE_BOOST, 80 + shipLevel, shipLevel / 50);
            case OWNER_SPEED -> ship.applyOwnerRingEffect(MobEffects.MOVEMENT_SPEED, 80 + shipLevel, shipLevel / 45);
            case OWNER_RESISTANCE -> ship.applyOwnerRingEffect(MobEffects.DAMAGE_RESISTANCE,
                    50 + shipLevel, shipLevel / 50);
            case SHIMAKAZE_OWNER_SPEED -> ship.applyOwnerRingEffect(MobEffects.MOVEMENT_SPEED,
                    80 + shipLevel, shipLevel / 35 + 1);
            case NONE -> {
            }
        }
    }

    public static CombatHook combatHookForLegacyClassId(int legacyClassId) {
        return morphCombatHookForClassId(legacyClassId);
    }

    public static int morphSpecialCooldown(CombatHook hook) {
        return switch (hook) {
            case SHIMAKAZE_TORPEDO_BURST -> 130;
            case NAGATO_HEAVY_STRIKE -> 170;
            case YAMATO_BEAM_BARRAGE -> 200;
            case TENRYUU_DASH -> 120;
            case TATSUTA_CONTROL_AOE -> 120;
            case HEAVY_CRUISER_BARRAGE -> 140;
            case KONGOU_COMBO -> 150;
            case BATTLESHIP_RU_BARRAGE -> 0;
            case BATTLESHIP_TA_RENSOUHOU -> 130;
            case SUBMARINE_HIME_DOUBLE_MISSILE -> 0;
            case CARRIER_HIME_MELEE_MISSILE -> 0;
            case NORTHERN_HIME_FLOATING_FORT -> 0;
            case NONE -> 0;
        };
    }

    private static MarriagePassive marriagePassiveForFriendlyClassId(int legacyClassId) {
        return switch (legacyClassId) {
            case 0 -> MarriagePassive.OWNER_SPEED;
            case 1 -> MarriagePassive.OWNER_HASTE;
            case 2 -> MarriagePassive.OWNER_JUMP;
            case 3 -> MarriagePassive.OWNER_STRENGTH;
            case 36 -> MarriagePassive.SHIMAKAZE_OWNER_SPEED;
            case 37 -> MarriagePassive.ALLIED_STRENGTH_AURA;
            case 17, 18, 19, 38, 39 -> MarriagePassive.SELF_AND_OWNER_INVISIBILITY;
            case 46 -> MarriagePassive.ALLIED_RESISTANCE_FIRE_AURA;
            case 47, 48 -> MarriagePassive.ALLIED_JUMP_AURA;
            case 51 -> MarriagePassive.OWNER_HASTE;
            case 52 -> MarriagePassive.OWNER_JUMP;
            case 53 -> MarriagePassive.OWNER_STRENGTH;
            case 54 -> MarriagePassive.OWNER_SPEED;
            case 15 -> MarriagePassive.OWNER_RESISTANCE;
            case 56, 57 -> MarriagePassive.OWNER_NIGHT_VISION;
            case 60 -> MarriagePassive.ALLIED_HEALTH_BOOST_AURA;
            case 61 -> MarriagePassive.ALLIED_SATURATION_AURA;
            case 62 -> MarriagePassive.ALLIED_LUCK_AURA;
            case 63 -> MarriagePassive.ALLIED_NIGHT_VISION_AURA;
            default -> MarriagePassive.NONE;
        };
    }

    /** The automatic entity attack hooks only cover classes with a legacy override. */
    private static CombatHook entityCombatHookForClassId(int legacyClassId) {
        int normalizedClassId = legacyClassId >= 2000 ? legacyClassId - 2000 : legacyClassId;
        return switch (normalizedClassId) {
            case 36 -> CombatHook.SHIMAKAZE_TORPEDO_BURST;
            case 37 -> CombatHook.NAGATO_HEAVY_STRIKE;
            case 46 -> CombatHook.YAMATO_BEAM_BARRAGE;
            case 56 -> CombatHook.TENRYUU_DASH;
            case 57 -> CombatHook.TATSUTA_CONTROL_AOE;
            case 13 -> CombatHook.BATTLESHIP_RU_BARRAGE;
            case 14 -> CombatHook.BATTLESHIP_TA_RENSOUHOU;
            case 44, 72 -> CombatHook.SUBMARINE_HIME_DOUBLE_MISSILE;
            case 20 -> CombatHook.CARRIER_HIME_MELEE_MISSILE;
            case 31 -> CombatHook.NORTHERN_HIME_FLOATING_FORT;
            default -> CombatHook.NONE;
        };
    }

    /** Keeps the established morph skill catalog separate from normal entity attacks. */
    private static CombatHook morphCombatHookForClassId(int legacyClassId) {
        int normalizedClassId = legacyClassId >= 2000 ? legacyClassId - 2000 : legacyClassId;
        return switch (normalizedClassId) {
            case 36 -> CombatHook.SHIMAKAZE_TORPEDO_BURST;
            case 37 -> CombatHook.NAGATO_HEAVY_STRIKE;
            case 46 -> CombatHook.YAMATO_BEAM_BARRAGE;
            case 56 -> CombatHook.TENRYUU_DASH;
            case 57 -> CombatHook.TATSUTA_CONTROL_AOE;
            case 58, 59 -> CombatHook.HEAVY_CRUISER_BARRAGE;
            case 60, 61, 62, 63 -> CombatHook.KONGOU_COMBO;
            default -> CombatHook.NONE;
        };
    }

    private static float cooldownMultiplier(CombatHook hook) {
        return switch (hook) {
            case SHIMAKAZE_TORPEDO_BURST -> 1.0F;
            case NAGATO_HEAVY_STRIKE -> 1.3F;
            case YAMATO_BEAM_BARRAGE -> 1.5F;
            case TENRYUU_DASH -> 0.9F;
            case TATSUTA_CONTROL_AOE -> 1.0F;
            case HEAVY_CRUISER_BARRAGE -> 1.1F;
            case KONGOU_COMBO -> 1.25F;
            case BATTLESHIP_RU_BARRAGE -> 1.0F;
            case BATTLESHIP_TA_RENSOUHOU -> 1.0F;
            case SUBMARINE_HIME_DOUBLE_MISSILE -> 1.0F;
            case CARRIER_HIME_MELEE_MISSILE -> 1.0F;
            case NORTHERN_HIME_FLOATING_FORT -> 1.0F;
            case NONE -> 1.0F;
        };
    }

    /** Mirrors both hime submarine classes' two side-launched light missiles. */
    private static CombatHookResult performSubmarineHimeDoubleMissile(LegacyShipEntity ship,
                                                                       LivingEntity primaryTarget) {
        if (!ship.consumeCatalogAttackResources(LegacyShipAttackKind.LIGHT, 1, 4, 1, 4)) {
            return CombatHookResult.HANDLED;
        }

        LegacyShipCombatHelper.AttackRoll roll = LegacyShipCombatHelper.rollAttack(
                ship, primaryTarget, LegacyShipAttackKind.LIGHT);
        // EntitySubmHime passes targetHeight=1F to the legacy summonMissile
        // hook, so the ordinary target point is always posY + 0.1F.
        Vec3 targetPosition = primaryTarget.position().add(0.0D, 0.1D, 0.0D);
        if (roll.miss()) {
            // The legacy miss branch replaces tarY with posY + random.nextFloat() * 5F;
            // it does not retain the ordinary +0.1F target-height offset.
            targetPosition = primaryTarget.position().add(
                    -5.0D + ship.getRandom().nextFloat() * 10.0D,
                    ship.getRandom().nextFloat() * 5.0D,
                    -5.0D + ship.getRandom().nextFloat() * 10.0D);
            CombatFxDispatcher.sendParticle(ship, GameplayParticleType.TEXT_MISS,
                    ship.getX(), ship.getY() + ship.getBbHeight() * 0.8D, ship.getZ());
        }

        float damage = ship.getLegacyStats().attackLight();
        LegacyShipProjectileProfile profile = ship.getAttackProfile().projectileProfile(
                LegacyShipAttackKind.LIGHT);
        for (Vec3 launchOffset : submarineHimeMissileLaunchOffsetsFor(ship.yBodyRot)) {
            LegacyShipProjectileEntity projectile = LegacyShipProjectileEntity.create(
                    ship.level(), ship, primaryTarget, LegacyShipAttackKind.LIGHT,
                    damage, roll.miss(), profile, targetPosition, 0.15D, false, 3.5D,
                    launchOffset);
            ship.level().addFreshEntity(projectile);
            CombatFxDispatcher.sendCombatReact(ship, primaryTarget, CombatReactType.LAUNCH,
                    LegacyShipAttackKind.LIGHT, projectile.getProjectileVisual());
        }

        ship.setLastHurtMob(primaryTarget);
        sendLaunchSmoke(ship);
        return CombatHookResult.HANDLED_WITH_RESOURCES;
    }

    /** Mirrors EntityCarrierHime.attackEntityAsMob's type-0 missile launch. */
    private static CombatHookResult performCarrierHimeMeleeMissile(LegacyShipEntity ship,
                                                                     LivingEntity target) {
        // EntityCarrierHime never calls the legacy attack-rate helper before launch;
        // type-0's miss/crit handling belongs to the missile impact path.
        LegacyShipCombatHelper.AttackRoll roll = new LegacyShipCombatHelper.AttackRoll(
                ship.getLegacyStats().meleeDamage(), false, false, false, false);
        LegacyShipProjectileProfile profile = LegacyShipAttackProfile.carrierHimeMeleeMissileProfile();
        Vec3 targetPosition = target.position().add(0.0D, target.getBbHeight() * 0.2D, 0.0D);
        LegacyShipProjectileEntity projectile = LegacyShipProjectileEntity.create(
                ship.level(), ship, target, LegacyShipAttackKind.MELEE,
                roll.damage(), roll.miss(), profile, targetPosition,
                0.0D, true, 3.5D, new Vec3(0.0D, 1.3D, 0.0D));
        ship.level().addFreshEntity(projectile);
        ship.addShipExperience(2);
        ship.setLastHurtMob(target);
        CombatFxDispatcher.sendCombatReact(ship, target, CombatReactType.LAUNCH,
                LegacyShipAttackKind.MELEE, projectile.getProjectileVisual());
        CombatFxDispatcher.sendParticle(ship, projectile.getLaunchParticleType(),
                projectile.getX(), projectile.getY(), projectile.getZ(),
                projectile.getDeltaMovement().x, projectile.getDeltaMovement().y,
                projectile.getDeltaMovement().z);
        return CombatHookResult.HANDLED_WITH_RESOURCES;
    }

    /** Mirrors EntityNorthernHime.attackEntityWithHeavyAmmo's FloatingFort summon. */
    private static CombatHookResult performNorthernHimeFloatingFort(LegacyShipEntity ship,
                                                                     LivingEntity target) {
        if (!ship.consumeCatalogAttackResources(LegacyShipAttackKind.HEAVY, 1, 8, 2, 5)) {
            return CombatHookResult.HANDLED;
        }

        double launchY = ship.getY() + ship.getBbHeight();
        if (ship.getLegacyLiquidDepth() > 0.0D) {
            launchY += 0.2D;
        }
        LegacyShipFloatingFortEntity floatingFort = LegacyShipFloatingFortEntity.create(
                ship.level(), ship, target, launchY);
        if (floatingFort == null) {
            return CombatHookResult.HANDLED_WITH_RESOURCES;
        }

        ship.level().addFreshEntity(floatingFort);
        ship.setLastHurtMob(target);
        CombatFxDispatcher.sendCombatReact(ship, target, CombatReactType.LAUNCH,
                LegacyShipAttackKind.HEAVY, LegacyShipProjectileVisual.BOMB);
        CombatFxDispatcher.sendParticle(ship, GameplayParticleType.BOMB_DROP,
                floatingFort.getX(), floatingFort.getY(), floatingFort.getZ());
        return CombatHookResult.HANDLED_WITH_RESOURCES;
    }

    private static CombatHookResult performShimakazeBurst(LegacyShipEntity ship, LivingEntity primaryTarget) {
        if (!ship.consumeShimakazeHeavyAttackResources()) {
            return CombatHookResult.HANDLED;
        }

        Vec3 targetPosition = primaryTarget.position();
        Vec3 direction = targetPosition.subtract(ship.position());
        double distance = direction.length();
        if (distance > 1.0E-4D && distance < 6.0D) {
            targetPosition = targetPosition.add(direction.scale((6.0D - distance) / distance));
        }
        targetPosition = targetPosition.add(0.0D, primaryTarget.getBbHeight() * 0.1D, 0.0D);

        boolean missed = LegacyShipCombatHelper.rollAttack(
                ship, primaryTarget, LegacyShipAttackKind.HEAVY).miss();
        if (missed) {
            targetPosition = targetPosition.add(
                    -5.0D + ship.getRandom().nextFloat() * 10.0D,
                    ship.getRandom().nextFloat() * 5.0D,
                    -5.0D + ship.getRandom().nextFloat() * 10.0D);
            CombatFxDispatcher.sendCombatReact(ship, primaryTarget, CombatReactType.MISS,
                    LegacyShipAttackKind.HEAVY, LegacyShipProjectileVisual.MISSILE);
            CombatFxDispatcher.sendParticle(primaryTarget, GameplayParticleType.TEXT_MISS,
                    primaryTarget.getX(), primaryTarget.getY() + primaryTarget.getBbHeight() * 0.8D,
                    primaryTarget.getZ());
        }

        float damage = ship.getLegacyStats().attackHeavy() * (ship.isHostileVariant() ? 0.9F : 0.3F);
        double knockback = ship.isHostileVariant() ? 0.08D : 0.15D;
        LegacyShipProjectileProfile profile = ship.getAttackProfile().projectileProfile(LegacyShipAttackKind.HEAVY);
        double spread = shimakazeHeavyMissileSpreadFor(ship.getSpec(), ship.getLegacyHostileScaleLevel());
        double[][] offsets = {{0.0D, 0.0D}, {spread, spread}, {spread, -spread},
                {-spread, spread}, {-spread, -spread}};
        for (double[] offset : offsets) {
            Vec3 shotTarget = targetPosition.add(offset[0], 0.0D, offset[1]);
            LegacyShipProjectileEntity projectile = LegacyShipProjectileEntity.create(
                    ship.level(), ship, primaryTarget, LegacyShipAttackKind.HEAVY,
                    damage, missed, profile, shotTarget, knockback, false);
            ship.level().addFreshEntity(projectile);
            CombatFxDispatcher.sendCombatReact(ship, primaryTarget, CombatReactType.LAUNCH,
                    LegacyShipAttackKind.HEAVY, projectile.getProjectileVisual());
        }

        ship.setLastHurtMob(primaryTarget);
        sendLaunchSmoke(ship);
        return CombatHookResult.HANDLED_WITH_RESOURCES;
    }

    /** Mirrors EntityDestroyerShimakaze.attackEntityWithHeavyAmmo(BlockPos). */
    private static CombatHookResult performShimakazeBlockBurst(LegacyShipEntity ship, BlockPos target) {
        if (!ship.consumeShimakazeHeavyAttackResources()) {
            return CombatHookResult.HANDLED;
        }

        BlockPos origin = ship.blockPosition();
        double deltaX = target.getX() - origin.getX();
        double deltaY = target.getY() - origin.getY();
        double deltaZ = target.getZ() - origin.getZ();
        double distance = Math.sqrt(deltaX * deltaX + deltaY * deltaY + deltaZ * deltaZ);
        double targetX = target.getX();
        double targetY = target.getY();
        double targetZ = target.getZ();
        if (distance > 1.0E-4D && distance < 6.0D) {
            double extension = (6.0D - distance) / distance;
            targetX += deltaX * extension;
            targetY += deltaY * extension;
            targetZ += deltaZ * extension;
        }

        boolean missed = LegacyShipCombatHelper.rollMissAtDistance(
                ship, LegacyShipAttackKind.HEAVY, (float) distance);
        if (missed) {
            targetX = targetX - 5.0D + ship.getRandom().nextFloat() * 10.0D;
            targetY = targetY + ship.getRandom().nextFloat() * 5.0D;
            targetZ = targetZ - 5.0D + ship.getRandom().nextFloat() * 10.0D;
            CombatFxDispatcher.sendParticle(ship, GameplayParticleType.TEXT_MISS,
                    ship.getX(), ship.getY() + ship.getBbHeight() * 0.8D, ship.getZ());
        }

        float damage = ship.getLegacyStats().attackHeavy() * 0.3F;
        LegacyShipProjectileProfile profile = ship.getAttackProfile().projectileProfile(
                LegacyShipAttackKind.HEAVY);
        double spread = shimakazeHeavyMissileSpreadFor(ship.getSpec(), ship.getLegacyHostileScaleLevel());
        double[][] offsets = {{0.0D, 0.0D}, {spread, spread}, {spread, -spread},
                {-spread, spread}, {-spread, -spread}};
        for (double[] offset : offsets) {
            Vec3 shotTarget = new Vec3(targetX + offset[0], targetY + 0.2D, targetZ + offset[1]);
            LegacyShipProjectileEntity projectile = LegacyShipProjectileEntity.create(
                    ship.level(), ship, null, LegacyShipAttackKind.HEAVY,
                    damage, missed, profile, shotTarget, 0.15D, false);
            ship.level().addFreshEntity(projectile);
        }

        sendLaunchSmoke(ship);
        return CombatHookResult.HANDLED_WITH_RESOURCES;
    }

    private static CombatHookResult performShimakazeSummon(LegacyShipEntity ship, LivingEntity target) {
        if (!ship.consumeShimakazeLightAttackResources()) {
            return CombatHookResult.HANDLED;
        }

        LegacyShipRensouhouEntity summon = LegacyShipRensouhouEntity.create(ship.level(), ship, target);
        if (summon == null) {
            return CombatHookResult.HANDLED_WITH_RESOURCES;
        }

        ship.setShimakazeRensouhouCount(ship.getShimakazeRensouhouCount() - 1);
        ship.level().addFreshEntity(summon);
        ship.playLegacyAttackVoice();
        sendLaunchSmoke(ship);
        return CombatHookResult.HANDLED_WITH_RESOURCES;
    }

    /** Mirrors EntityBattleshipTa.attackEntityWithAmmo and its six-shot servant pool. */
    private static CombatHookResult performBattleshipTaSummon(LegacyShipEntity ship, LivingEntity target) {
        if (ship.getShimakazeRensouhouCount() <= 0) {
            return CombatHookResult.HANDLED;
        }
        if (!ship.consumeCatalogAttackResources(LegacyShipAttackKind.LIGHT, 4, 16, 1, 8)) {
            return CombatHookResult.HANDLED;
        }

        LegacyShipRensouhouEntity summon = LegacyShipRensouhouEntity.create(ship.level(), ship, target);
        if (summon == null) {
            return CombatHookResult.HANDLED_WITH_RESOURCES;
        }

        ship.addShimakazeRensouhouCount(-1);
        ship.level().addFreshEntity(summon);
        ship.playLegacyAttackVoice();
        sendLaunchSmoke(ship);
        return CombatHookResult.HANDLED_WITH_RESOURCES;
    }

    /** Mirrors EntityBattleshipRu.attackEntityWithHeavyAmmo's charge and target lock. */
    private static CombatHookResult performBattleshipRuBarrage(LegacyShipEntity ship,
                                                                LivingEntity primaryTarget) {
        if (!ship.consumeCatalogAttackResources(LegacyShipAttackKind.HEAVY, 1, 8, 2, 5)) {
            return CombatHookResult.HANDLED;
        }

        boolean missed = LegacyShipCombatHelper.rollAttack(
                ship, primaryTarget, LegacyShipAttackKind.HEAVY).miss();
        float targetX = (float) primaryTarget.getX();
        float targetY = (float) primaryTarget.getY();
        float targetZ = (float) primaryTarget.getZ();
        if (missed) {
            targetX = targetX - 5.0F + ship.getRandom().nextFloat() * 10.0F;
            targetY = targetY + ship.getRandom().nextFloat() * 5.0F;
            targetZ = targetZ - 5.0F + ship.getRandom().nextFloat() * 10.0F;
            CombatFxDispatcher.sendParticle(ship, GameplayParticleType.TEXT_MISS,
                    ship.getX(), ship.getY() + ship.getBbHeight() * 0.8D, ship.getZ());
        }

        if (ship.getDisplayPhase() == 0) {
            ship.setDisplayPhase(1);
            ship.setLegacySkillRemaining(ship.getLegacySkillRemaining()
                    + 5 + (int) (ship.getShipLevel() * 0.035F));
            ship.setLegacySkillTarget(new BlockPos(
                    Mth.floor(targetX),
                    Mth.floor(targetY + primaryTarget.getBbHeight() * 0.35D),
                    Mth.floor(targetZ)));
            ship.setTarget(primaryTarget);
            ship.playLegacyBattleshipRuChargeSound();
            sendLaunchSmoke(ship);
        }

        return CombatHookResult.HANDLED_WITH_RESOURCES;
    }

    /** Mirrors EntityBattleshipRu.attackEntityWithHeavyAmmo(BlockPos). */
    private static CombatHookResult performBattleshipRuBlockBarrage(LegacyShipEntity ship,
                                                                     BlockPos target) {
        if (!ship.consumeCatalogAttackResources(LegacyShipAttackKind.HEAVY, 1, 8, 2, 5)) {
            return CombatHookResult.HANDLED;
        }

        BlockPos origin = ship.blockPosition();
        double deltaX = target.getX() - origin.getX();
        double deltaY = target.getY() - origin.getY();
        double deltaZ = target.getZ() - origin.getZ();
        double distance = Math.sqrt(deltaX * deltaX + deltaY * deltaY + deltaZ * deltaZ);
        float targetX = target.getX();
        float targetY = target.getY();
        float targetZ = target.getZ();
        if (LegacyShipCombatHelper.rollMissAtDistance(
                ship, LegacyShipAttackKind.HEAVY, (float) distance)) {
            targetX = targetX - 5.0F + ship.getRandom().nextFloat() * 10.0F;
            targetY = targetY + ship.getRandom().nextFloat() * 5.0F;
            targetZ = targetZ - 5.0F + ship.getRandom().nextFloat() * 10.0F;
            CombatFxDispatcher.sendParticle(ship, GameplayParticleType.TEXT_MISS,
                    ship.getX(), ship.getY() + ship.getBbHeight() * 0.8D, ship.getZ());
        }

        if (ship.getDisplayPhase() == 0) {
            ship.setDisplayPhase(1);
            ship.setLegacySkillRemaining(ship.getLegacySkillRemaining()
                    + 5 + (int) (ship.getShipLevel() * 0.035F));
            ship.setLegacySkillTarget(new BlockPos(
                    Mth.floor(targetX), Mth.floor(targetY + 0.5F), Mth.floor(targetZ)));
            ship.setTarget(null);
            ship.playLegacyBattleshipRuChargeSound();
            sendLaunchSmoke(ship);
        }

        return CombatHookResult.HANDLED_WITH_RESOURCES;
    }

    private static CombatHookResult performNagatoStrike(LegacyShipEntity ship,
                                                         LivingEntity primaryTarget) {
        if (!ship.consumeCatalogAttackResources(LegacyShipAttackKind.HEAVY, 2, 5)) {
            return CombatHookResult.HANDLED;
        }

        sendLaunchSmoke(ship);
        int nextPhase = ship.getDisplayPhase() + 1;
        if (nextPhase <= 3) {
            ship.setDisplayPhase(nextPhase);
            return CombatHookResult.HANDLED_WITH_RESOURCES;
        }

        float rawDamage = LegacyShipCombatHelper.applyTargetDamageModifier(
                ship.getLegacyStats(), primaryTarget, ship.getLegacyStats().attackHeavy()) * 4.0F;
        float collisionDistance = ship.distanceTo(primaryTarget);
        Vec3 direction = primaryTarget.position().subtract(ship.position());
        double distance = direction.length();
        if (distance > 1.0E-4D) {
            direction = direction.scale(1.0D / distance);
        } else {
            direction = Vec3.ZERO;
        }

        LegacyShipCombatHelper.AttackRoll primaryRoll = LegacyShipCombatHelper.rollNonMultiHitAttack(
                ship, primaryTarget, rawDamage, collisionDistance);
        ship.performCatalogDirectAttack(primaryTarget,
                primaryRoll,
                false, false, LegacyShipProjectileVisual.NONE,
                GameplayParticleType.HIT_EXPLOSION);

        ship.setDeltaMovement(Vec3.ZERO);
        ship.setPos(primaryTarget.getX() + direction.x * 2.0D,
                primaryTarget.getY(), primaryTarget.getZ() + direction.z * 2.0D);
        float areaDamage = primaryRoll.damage() * 0.5F;
        double blastRadius = nagatoBlastRadiusFor(ship.getSpec(), ship.getLegacyHostileScaleLevel());
        for (LivingEntity areaTarget : ship.level().getEntitiesOfClass(
                LivingEntity.class, ship.getBoundingBox().inflate(blastRadius),
                candidate -> candidate != ship && candidate.isAlive()
                        && !candidate.isInvulnerable() && candidate.isPickable()
                        && (ship.canEngage(candidate) || hasLegacySameOwner(ship, candidate)))) {
            float targetDamage = hasLegacySameOwner(ship, areaTarget) ? 0.0F : areaDamage;
            ship.performLegacyAreaDirectAttack(areaTarget,
                    LegacyShipCombatHelper.rollNonMultiHitAttack(ship, areaTarget,
                            targetDamage, collisionDistance),
                    false, false, LegacyShipProjectileVisual.NONE,
                    GameplayParticleType.HIT_EXPLOSION);
        }
        ship.setDisplayPhase(0);
        return CombatHookResult.HANDLED_WITH_RESOURCES;
    }

    private static CombatHookResult performYamatoBeam(LegacyShipEntity ship, LivingEntity primaryTarget) {
        if (!ship.consumeCatalogAttackResources(LegacyShipAttackKind.HEAVY, 2, 5)) {
            return CombatHookResult.HANDLED;
        }

        if (ship.getDisplayPhase() <= 0) {
            ship.setDisplayPhase(1);
            CombatFxDispatcher.sendParticle(ship, GameplayParticleType.SEARCHLIGHT_MARK,
                    ship.getX(), ship.getY() + ship.getBbHeight() * 0.8D, ship.getZ());
            sendLaunchSmoke(ship);
            return CombatHookResult.HANDLED_WITH_RESOURCES;
        }

        float beamDamage = LegacyShipCombatHelper.applyTargetDamageModifier(
                ship.getLegacyStats(), primaryTarget, ship.getLegacyStats().attackHeavy()) * 1.5F;
        LegacyShipBeamEntity beam = LegacyShipBeamEntity.create(
                ship.level(), ship, primaryTarget, beamDamage);
        if (beam == null) {
            return CombatHookResult.HANDLED_WITH_RESOURCES;
        }

        ship.level().addFreshEntity(beam);
        ship.setDisplayPhase(0);
        ship.setLastHurtMob(primaryTarget);
        CombatFxDispatcher.sendCombatReact(ship, primaryTarget, CombatReactType.LAUNCH,
                LegacyShipAttackKind.HEAVY, LegacyShipProjectileVisual.NONE);
        CombatFxDispatcher.sendParticle(ship, GameplayParticleType.BEAM_TRAIL,
                beam.getX(), beam.getY(), beam.getZ(),
                beam.getDeltaMovement().x, beam.getDeltaMovement().y, beam.getDeltaMovement().z);
        return CombatHookResult.HANDLED_WITH_RESOURCES;
    }

    private static CombatHookResult performTenryuuDash(LegacyShipEntity ship, LivingEntity primaryTarget) {
        if (!ship.consumeCatalogAttackResources(LegacyShipAttackKind.HEAVY, 2, 5)) {
            return CombatHookResult.HANDLED;
        }

        int phase = ship.getDisplayPhase();
        if (phase == 0) {
            ship.setDisplayPhase(-1);
            sendLaunchSmoke(ship);
        } else if (phase == -1) {
            ship.setDisplayPhase(1);
            ship.setLegacySkillTimer(0);
            ship.setLegacySkillRemaining(ship.isHostileVariant()
                    ? 2 + ship.getLegacyHostileScaleLevel() * 2
                    : 3 + (int) (ship.getShipLevel() * 0.03F));
            ship.setLegacySkillMotion(Vec3.ZERO);
            ship.clearLegacySkillDamagedTargets();
            ship.setTarget(primaryTarget);
            sendLaunchSmoke(ship);
        }

        return CombatHookResult.HANDLED_WITH_RESOURCES;
    }

    private static CombatHookResult performTatsutaControl(LegacyShipEntity ship, LivingEntity primaryTarget) {
        if (!ship.consumeCatalogAttackResources(LegacyShipAttackKind.HEAVY, 2, 5)) {
            return CombatHookResult.HANDLED;
        }

        int phase = ship.getDisplayPhase();
        if (phase == 0) {
            ship.setDisplayPhase(-1);
            sendLaunchSmoke(ship);
        } else if (phase == -1) {
            ship.setDisplayPhase(1);
            ship.setLegacySkillTimer(10);
            ship.setLegacySkillRemaining(ship.isHostileVariant()
                    ? 1 + ship.getLegacyHostileScaleLevel()
                    : 2 + (int) (ship.getShipLevel() * 0.015F));
            ship.setLegacySkillMotion(Vec3.ZERO);
            ship.clearLegacySkillDamagedTargets();
            ship.setTarget(primaryTarget);
            CombatFxDispatcher.sendParticle(ship, GameplayParticleType.SEARCHLIGHT_MARK,
                    ship.getX(), ship.getY() + ship.getBbHeight() * 0.8D, ship.getZ());
        }

        return CombatHookResult.HANDLED_WITH_RESOURCES;
    }

    private static boolean performHeavyCruiserBarrage(LegacyShipEntity ship, LivingEntity primaryTarget) {
        boolean acted = false;
        for (LivingEntity target : collectTargets(ship, primaryTarget, 5.5D, 4)) {
            boolean hit = ship.performCatalogSpecialAttack(target, LegacyShipAttackKind.HEAVY,
                    target == primaryTarget ? 1.45F : 1.05F,
                    true,
                    LegacyShipProjectileVisual.MISSILE,
                    GameplayParticleType.MISSILE_IMPACT);
            if (hit) {
                target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 40, 0, false, true, true));
                acted = true;
            }
        }
        sendLaunchSmoke(ship);
        return acted;
    }

    private static boolean performKongouCombo(LegacyShipEntity ship, LivingEntity primaryTarget) {
        List<LivingEntity> targets = collectTargets(ship, primaryTarget, 7.0D, 5);
        boolean acted = false;
        for (int index = 0; index < targets.size(); index++) {
            LivingEntity target = targets.get(index);
            acted |= ship.performCatalogSpecialAttack(target, LegacyShipAttackKind.HEAVY,
                    index == 0 ? 1.55F : 1.15F,
                    true,
                    LegacyShipProjectileVisual.MISSILE,
                    GameplayParticleType.MISSILE_IMPACT);
            acted |= ship.performCatalogSpecialAttack(target, LegacyShipAttackKind.LIGHT,
                    index == 0 ? 1.0F : 0.78F,
                    false,
                    LegacyShipProjectileVisual.NONE,
                    GameplayParticleType.HIT_EXPLOSION);
        }
        sendLaunchSmoke(ship);
        return acted;
    }

    private static List<LivingEntity> collectTargets(LegacyShipEntity ship, LivingEntity primaryTarget,
                                                     double radius, int maxTargets) {
        List<LivingEntity> targets = new ArrayList<>();
        targets.add(primaryTarget);

        AABB searchArea = primaryTarget.getBoundingBox().inflate(radius, radius * 0.5D, radius);
        for (LivingEntity candidate : ship.level().getEntitiesOfClass(LivingEntity.class, searchArea)) {
            if (candidate == primaryTarget || !ship.canEngage(candidate)) {
                continue;
            }
            targets.add(candidate);
        }

        targets.sort(Comparator.comparingDouble(ship::distanceToSqr));
        if (targets.size() > maxTargets) {
            return List.copyOf(targets.subList(0, maxTargets));
        }
        return List.copyOf(targets);
    }

    private static List<LivingEntity> collectLineTargets(LegacyShipEntity ship, LivingEntity primaryTarget,
                                                         double range, double radius, int maxTargets) {
        Vec3 origin = centerOf(ship);
        Vec3 primaryOffset = centerOf(primaryTarget).subtract(origin);
        if (primaryOffset.lengthSqr() <= 1.0E-4D) {
            return List.of(primaryTarget);
        }

        Vec3 direction = primaryOffset.normalize();
        List<ScoredTarget> scoredTargets = new ArrayList<>();
        AABB searchArea = new AABB(ship.blockPosition()).inflate(range, range * 0.5D, range);
        for (LivingEntity candidate : ship.level().getEntitiesOfClass(LivingEntity.class, searchArea)) {
            if (!ship.canEngage(candidate)) {
                continue;
            }

            Vec3 offset = centerOf(candidate).subtract(origin);
            double along = offset.dot(direction);
            if (along < 0.0D || along > range) {
                continue;
            }

            double lateralDistance = offset.subtract(direction.scale(along)).length();
            if (lateralDistance <= radius) {
                scoredTargets.add(new ScoredTarget(candidate, along));
            }
        }

        scoredTargets.sort(Comparator.comparingDouble(ScoredTarget::distanceAlong));
        List<LivingEntity> targets = new ArrayList<>();
        for (ScoredTarget scoredTarget : scoredTargets) {
            targets.add(scoredTarget.target());
            if (targets.size() >= maxTargets) {
                break;
            }
        }
        if (!targets.contains(primaryTarget)) {
            targets.add(0, primaryTarget);
        }
        return List.copyOf(targets);
    }

    private static Vec3 centerOf(LivingEntity entity) {
        return entity.position().add(0.0D, entity.getBbHeight() * 0.5D, 0.0D);
    }

    private static void sendLaunchSmoke(LegacyShipEntity ship) {
        CombatFxDispatcher.sendParticle(ship, GameplayParticleType.LAUNCH_SMOKE,
                ship.getX(), ship.getY() + ship.getBbHeight() * 0.7D, ship.getZ());
    }

    private record ScoredTarget(LivingEntity target, double distanceAlong) {
    }
}
