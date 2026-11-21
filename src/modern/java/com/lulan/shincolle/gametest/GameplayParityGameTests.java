package com.lulan.shincolle.gametest;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.authlib.GameProfile;
import com.lulan.shincolle.ShinColle;
import com.lulan.shincolle.advancement.ModCriteriaTriggers;
import com.lulan.shincolle.combat.WorldCombatRulesSavedData;
import com.lulan.shincolle.config.ShinColleConfig;
import com.lulan.shincolle.blockentity.CraneBlockEntity;
import com.lulan.shincolle.blockentity.DeskBlockEntity;
import com.lulan.shincolle.blockentity.HeavyGrudgeBlockEntity;
import com.lulan.shincolle.blockentity.LargeShipyardStructureHelper;
import com.lulan.shincolle.blockentity.LegacyCoreBlockEntity;
import com.lulan.shincolle.blockentity.PolymetalServantBlockEntity;
import com.lulan.shincolle.blockentity.WaypointBlockEntity;
import com.lulan.shincolle.client.model.legacy.LegacyModelDefinition;
import com.lulan.shincolle.client.model.legacy.LegacyModelSourceParser;
import com.lulan.shincolle.client.renderer.entity.LegacyShipRenderCatalog;
import com.lulan.shincolle.crafting.LegacyShipConstructionHelper;
import com.lulan.shincolle.crafting.LargeShipyardRecipes;
import com.lulan.shincolle.crafting.ShipyardBuildTypes;
import com.lulan.shincolle.crafting.SmallShipyardRecipes;
import com.lulan.shincolle.entity.projectile.LegacyShipProjectileEntity;
import com.lulan.shincolle.entity.projectile.LegacyShipProjectileMoveType;
import com.lulan.shincolle.entity.projectile.LegacyShipProjectileVisual;
import com.lulan.shincolle.entity.mount.LegacyMountEntity;
import com.lulan.shincolle.entity.ship.BossActionType;
import com.lulan.shincolle.entity.ship.BossPhaseProfile;
import com.lulan.shincolle.entity.ship.LegacyShipAttackKind;
import com.lulan.shincolle.entity.ship.LegacyShipAttackProfile;
import com.lulan.shincolle.entity.ship.LegacyShipBehaviorCatalog;
import com.lulan.shincolle.entity.ship.LegacyShipAircraftEntity;
import com.lulan.shincolle.entity.ship.LegacyShipEntity;
import com.lulan.shincolle.entity.ship.LegacyShipRensouhouEntity;
import com.lulan.shincolle.entity.ship.LegacyShipStats;
import com.lulan.shincolle.entity.ship.ShipEntitySpec;
import com.lulan.shincolle.entity.ship.ShipEntitySpecs;
import com.lulan.shincolle.entity.ship.ShipArchetype;
import com.lulan.shincolle.entity.ship.ShipEquipmentBehaviorState;
import com.lulan.shincolle.entity.ship.ShipEquipmentProfile;
import com.lulan.shincolle.entity.ship.goal.LegacyShipPickItemGoal;
import com.lulan.shincolle.item.LegacyShipSpawnEggItem;
import com.lulan.shincolle.morph.MorphHelper;
import com.lulan.shincolle.morph.MorphHostMode;
import com.lulan.shincolle.morph.MorphProfile;
import com.lulan.shincolle.network.ClientboundCombatReactPacket;
import com.lulan.shincolle.network.CombatReactType;
import com.lulan.shincolle.network.GameplayCommandHandler;
import com.lulan.shincolle.network.GameplayCommandType;
import com.lulan.shincolle.network.ServerboundGameplayCommandPacket;
import com.lulan.shincolle.network.ServerboundShipCommandPacket;
import com.lulan.shincolle.network.ShipCommandAction;
import com.lulan.shincolle.network.ShipCommandService;
import com.lulan.shincolle.menu.CraneTerminalMenu;
import com.lulan.shincolle.menu.DeskReferenceMenu;
import com.lulan.shincolle.menu.DeskTerminalMenu;
import com.lulan.shincolle.menu.FormationMenu;
import com.lulan.shincolle.menu.LargeShipyardMenu;
import com.lulan.shincolle.menu.LegacyCoreMenu;
import com.lulan.shincolle.menu.MorphInventoryMenu;
import com.lulan.shincolle.menu.ShipInventoryMenu;
import com.lulan.shincolle.menu.WaypointTerminalMenu;
import com.lulan.shincolle.ownership.PlayerOwnerData;
import com.lulan.shincolle.playerskill.PlayerSkillHostResolver;
import com.lulan.shincolle.playerskill.PlayerSkillRuntimeState;
import com.lulan.shincolle.registry.ModBlocks;
import com.lulan.shincolle.registry.ModEntityTypes;
import com.lulan.shincolle.registry.ModItems;
import com.lulan.shincolle.registry.ModSoundEvents;
import com.lulan.shincolle.sound.ShinColleSoundHelper;
import com.lulan.shincolle.sound.ShipSoundType;
import com.lulan.shincolle.team.TeamData;
import com.lulan.shincolle.team.TeamSavedData;
import com.lulan.shincolle.teitoku.TeitokuData;
import com.lulan.shincolle.teitoku.TeitokuEvents;
import com.lulan.shincolle.teitoku.TeitokuHelper;
import com.lulan.shincolle.teitoku.TeitokuSavedData;
import com.lulan.shincolle.teitoku.ShipCacheSavedData;
import com.lulan.shincolle.teitoku.ShipWorldCacheEntry;
import com.lulan.shincolle.world.ChestLootInjector;
import com.lulan.shincolle.world.HostileEncounterTable;
import com.lulan.shincolle.world.HostileEncounterSpawner;
import io.netty.buffer.Unpooled;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Cow;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.TransientCraftingContainer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.registries.ForgeRegistries;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.function.Consumer;

import com.lulan.shincolle.entity.mount.LegacyShipMountEntity;
import com.lulan.shincolle.entity.other.LegacyShipFishingHookEntity;
import com.lulan.shincolle.entity.projectile.LegacyShipBeamEntity;
import com.lulan.shincolle.entity.projectile.LegacyShipFloatingFortEntity;
import com.lulan.shincolle.entity.projectile.LegacyShipProjectileProfile;
import com.lulan.shincolle.entity.projectile.LegacyShipStaticEffectEntity;
import com.lulan.shincolle.entity.ship.LegacyShipCombatHelper;
import com.lulan.shincolle.entity.ship.LegacyShipModelStateCatalog;
import com.lulan.shincolle.entity.ship.LegacyShipModelTransformCatalog;
import com.lulan.shincolle.entity.ship.LegacyShipRensouhouEntity;
import com.lulan.shincolle.entity.ship.LegacyShipStatTables;
import com.lulan.shincolle.entity.ship.goal.LegacyShipFleeGoal;
import com.lulan.shincolle.entity.ship.goal.LegacyShipFollowOwnerGoal;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import com.lulan.shincolle.entity.ship.goal.LegacyShipGuardGoal;
import com.lulan.shincolle.entity.ship.goal.LegacyShipRangeTargetGoal;
import com.lulan.shincolle.entity.ship.goal.LegacyShipRangedAttackGoal;
import com.lulan.shincolle.blockentity.LightBlockEntity;
import com.lulan.shincolle.crafting.LegacyItemMatchHelper;
import com.lulan.shincolle.network.ClientboundSpawnParticlePacket;
import com.lulan.shincolle.network.GameplayParticleType;
import com.lulan.shincolle.world.LightBlockHelper;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.BossEvent;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.Node;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.common.crafting.conditions.ICondition;

@GameTestHolder(ShinColle.MOD_ID)
public final class GameplayParityGameTests {

    private GameplayParityGameTests() {
    }

    @GameTest(template = "empty")
    public static void teamRelationshipDirectionality(GameTestHelper helper) {
        TeamSavedData data = new TeamSavedData();
        data.createTeam(1, "A", "A");
        data.createTeam(2, "B", "B");

        if (!data.addAllyRelationship(1, 2)) {
            helper.fail("ally add should succeed");
            return;
        }
        if (!data.getTeam(1).isAlly(2) || data.getTeam(2).isAlly(1)) {
            helper.fail("ally add must be unilateral");
            return;
        }

        if (!data.removeAllyRelationship(1, 2)) {
            helper.fail("ally remove should succeed");
            return;
        }
        if (data.getTeam(1).isAlly(2) || data.getTeam(2).isAlly(1)) {
            helper.fail("ally remove must clear both sides");
            return;
        }

        if (!data.addBannedRelationship(1, 2)) {
            helper.fail("ban add should succeed");
            return;
        }
        if (!data.getTeam(1).isBanned(2) || !data.getTeam(2).isBanned(1)) {
            helper.fail("ban add must apply to both sides");
            return;
        }

        if (!data.removeBannedRelationship(1, 2)) {
            helper.fail("ban remove should succeed");
            return;
        }
        if (data.getTeam(1).isBanned(2) || !data.getTeam(2).isBanned(1)) {
            helper.fail("ban remove must be unilateral");
            return;
        }

        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void teitokuSlotSwapAndClear(GameTestHelper helper) {
        TeitokuData data = new TeitokuData();
        data.setCurrentTeamId(0);
        data.assignCurrentTeamSlot(0, 101);
        data.assignCurrentTeamSlot(1, 202);
        data.setCurrentTeamSelection(0, true);
        data.setCurrentTeamSelection(1, false);

        if (!data.swapCurrentTeamSlots(0, 1)) {
            helper.fail("swap should succeed");
            return;
        }
        if (data.getShipUid(0, 0) != 202 || data.getShipUid(0, 1) != 101) {
            helper.fail("ship uid should swap");
            return;
        }
        if (!data.isShipSelected(0, 1) || data.isShipSelected(0, 0)) {
            helper.fail("selection state should swap");
            return;
        }

        data.clearCurrentTeam();
        if (data.countShipsInTeam(0) != 0 || data.getCurrentTeamShipUids(false).size() != 0) {
            helper.fail("team clear should remove all slots");
            return;
        }

        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void teitokuTargetClassAddRemove(GameTestHelper helper) {
        TeitokuData data = new TeitokuData();
        data.addTargetClass("minecraft:zombie");
        data.addTargetClass("minecraft:skeleton");
        data.addTargetClass("minecraft:zombie");

        if (data.getTargetClassCount() != 2) {
            helper.fail("duplicate target class should be ignored");
            return;
        }
        if (!data.removeTargetClass("minecraft:zombie") || data.hasTargetClass("minecraft:zombie")) {
            helper.fail("target class remove should work");
            return;
        }

        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void teitokuLoadsLegacyCapaTeamFormationTags(GameTestHelper helper) {
        CompoundTag legacy = new CompoundTag();
        legacy.putBoolean("hasRing", true);
        legacy.putBoolean("RingOn", true);
        legacy.putBoolean("RingFly", true);
        legacy.putInt("PlayerUID", 42);
        legacy.putIntArray("FormatID", new int[]{0, 2, 4, 5, 1, 3, 0, 0, 0});
        legacy.putIntArray("TeamList1", new int[]{101, 0, 202, -1, 303, 0});
        legacy.putByteArray("SelectState1", new byte[]{1, 1, 1, 0, 1, 0});
        legacy.putString("uname1", "Legacy Team");
        ListTag legacyTargetClasses = new ListTag();
        legacyTargetClasses.add(StringTag.valueOf("minecraft:zombie"));
        legacyTargetClasses.add(StringTag.valueOf("minecraft:skeleton"));
        legacy.put("CustomTargetClass", legacyTargetClasses);
        CompoundTag wrappedLegacy = new CompoundTag();
        wrappedLegacy.put("TeitokuExtProps", legacy);

        TeitokuData loaded = new TeitokuData();
        loaded.setCurrentTeamId(1);
        loaded.loadFromTag(wrappedLegacy);

        if (!loaded.hasRing() || !loaded.isRingActive() || !loaded.isRingFlying()) {
            helper.fail("legacy CapaTeitoku ring booleans should load from old tag names");
            return;
        }
        if (loaded.getPlayerUid() != 42 || loaded.getFormationId(1) != 2 || loaded.getFormationId(3) != 5) {
            helper.fail("legacy CapaTeitoku player UID and FormatID array should load");
            return;
        }
        int[] team = loaded.getTeamShipUids(1);
        boolean[] selected = loaded.getTeamShipSelected(1);
        if (team[0] != 101 || team[1] != -1 || team[2] != 202 || team[4] != 303) {
            helper.fail("legacy TeamListN values should load and normalize empty slots");
            return;
        }
        if (!selected[0] || selected[1] || !selected[2] || !selected[4]) {
            helper.fail("legacy SelectStateN values should load only for occupied ship UID slots");
            return;
        }
        if (!"Legacy Team".equals(loaded.getTeamName(1))) {
            helper.fail("legacy unameN team name should load");
            return;
        }
        if (!loaded.hasTargetClass("minecraft:zombie") || !loaded.hasTargetClass("minecraft:skeleton")) {
            helper.fail("wrapped legacy CustomTargetClass list should load");
            return;
        }

        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void deskTerminalMenuProjectsTeamRelationsAndWorldRules(GameTestHelper helper) {
        Player player = helper.makeMockPlayer();
        int playerUid = 8121;
        String worldRuleKey = "shincolle:test_desk_world_rule";

        if (!configureTeitoku(helper, player, data -> {
            data.setPlayerUid(playerUid);
            data.setPlayerName(player.getGameProfile().getName());
            data.setHasTeam(true);
            data.setCurrentTeamId(0);
            data.addTargetClass("minecraft:zombie");
            data.addTargetClass("minecraft:skeleton");
        })) {
            return;
        }

        TeamSavedData teamData = TeamSavedData.get(helper.getLevel());
        teamData.createTeam(playerUid, player.getGameProfile().getName(), "Desk Fleet");
        teamData.createTeam(200, "Bravo", "Bravo");
        teamData.createTeam(300, "Charlie", "Charlie");
        teamData.createTeam(400, "Delta", "Delta");
        teamData.addAllyRelationship(playerUid, 200);
        teamData.addBannedRelationship(playerUid, 300);
        WorldCombatRulesSavedData worldRules = WorldCombatRulesSavedData.get(helper.getLevel());
        if (!worldRules.isUnattackable(worldRuleKey)) {
            worldRules.toggleUnattackable(worldRuleKey);
        }

        CompoundTag payload = TeitokuHelper.buildGameplayStateTag(helper.getLevel(), player);
        TeitokuHelper.applyClientState(player, payload);

        DeskTerminalMenu menu = new DeskTerminalMenu(0, player.getInventory(), BlockPos.ZERO, DeskReferenceMenu.BOOK_VARIANT);
        if (menu.getOwnAllyCount() != 1
                || menu.getOwnBannedCount() != 1
                || menu.getTeamRelation(200) != DeskTerminalMenu.TeamRelation.ALLIED
                || menu.getTeamRelation(300) != DeskTerminalMenu.TeamRelation.HOSTILE
                || menu.getTeamRelation(400) != DeskTerminalMenu.TeamRelation.NEUTRAL
                || menu.getTeamRelation(playerUid) != DeskTerminalMenu.TeamRelation.OWN) {
            helper.fail("desk terminal menu should project legacy own/ally/hostile/neutral team relations");
            return;
        }

        if (!menu.getTargetClasses().contains("minecraft:zombie")
                || !menu.getWorldUnattackableClasses().contains(worldRuleKey)) {
            helper.fail("desk terminal menu should expose synced target-class and world-rule lists");
            return;
        }

        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void hostileSpecFlagsMatchLegacySemantics(GameTestHelper helper) {
        ShipEntitySpec destroyerI = ShipEntitySpecs.getByEggMeta(2);
        ShipEntitySpec kongou = ShipEntitySpecs.getByEggMeta(62);
        ShipEntitySpec kongouHostile = ShipEntitySpecs.getByEggMeta(2062);
        ShipEntitySpec airfield = ShipEntitySpecs.getByEggMeta(23);
        ShipEntitySpec yamatoHostile = ShipEntitySpecs.getByEggMeta(2048);

        if (destroyerI.hostile() || kongou.hostile() || !kongouHostile.hostile()
                || airfield.hostile() || !yamatoHostile.hostile()) {
            helper.fail("modern ship spec hostility flags should preserve 1.12 friendly and >2000 hostile-mob semantics");
            return;
        }

        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void shipEntitySpecsResolveLegacyClassIds(GameTestHelper helper) {
        int[][] expectedMappings = {
                {36, 38},
                {37, 39},
                {38, 40},
                {39, 41},
                {46, 48},
                {47, 49},
                {48, 50},
                {51, 53},
                {52, 54},
                {53, 55},
                {54, 56},
                {56, 58},
                {57, 59},
                {58, 60},
                {59, 61},
                {60, 62},
                {61, 63},
                {62, 64},
                {63, 65}
        };

        for (int[] mapping : expectedMappings) {
            int legacyClassId = mapping[0];
            int expectedEggMeta = mapping[1];
            ShipEntitySpec spec = ShipEntitySpecs.findByLegacyClassId(legacyClassId);
            ShipEntitySpec mirrorSpec = ShipEntitySpecs.findByLegacyClassId(legacyClassId + 2000);
            if (spec == null || spec.eggMeta() != expectedEggMeta
                    || mirrorSpec == null || mirrorSpec.eggMeta() != expectedEggMeta + 2000) {
                helper.fail("legacy class id lookup mismatch for class " + legacyClassId
                        + ": expected egg " + expectedEggMeta + " and mirror " + (expectedEggMeta + 2000));
                return;
            }
        }

        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void hostileEncounterTableOnlyReturnsHostileProfiles(GameTestHelper helper) {
        RandomSource random = RandomSource.create(3412L);
        for (int i = 0; i < 64; i++) {
            var profile = com.lulan.shincolle.world.HostileEncounterTable.pick(random, net.minecraft.world.Difficulty.HARD, i % 2 == 0, true);
            ShipEntitySpec spec = ShipEntitySpecs.getByEggMeta(profile.eggMeta());
            if (!spec.hostile()) {
                helper.fail("encounter table should never choose a friendly ship spec");
                return;
            }
        }

        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void equipmentBehaviorMapping(GameTestHelper helper) {
        SimpleContainer carrierInventory = new SimpleContainer(LegacyShipEntity.SHIP_SLOT_COUNT);
        carrierInventory.setItem(0, new ItemStack(ModItems.EQUIPRADAR_ITEMS.get(8).get()));
        carrierInventory.setItem(1, new ItemStack(ModItems.EQUIPSEARCHLIGHT.get()));
        carrierInventory.setItem(2, new ItemStack(ModItems.EQUIPFLARE.get()));
        carrierInventory.setItem(3, new ItemStack(ModItems.EQUIPCATAPULT_ITEMS.get(3).get()));
        carrierInventory.setItem(4, new ItemStack(ModItems.EQUIPTURBINE_ITEMS.get(4).get()));
        carrierInventory.setItem(5, new ItemStack(ModItems.EQUIPTORPEDO_ITEMS.get(6).get()));

        ShipEquipmentBehaviorState carrierState = ShipEquipmentBehaviorState.fromInventory(carrierInventory, ShipArchetype.CARRIER);
        if (carrierState.fcsLevel() < 2
                || carrierState.surfaceRadarLevel() < 1
                || carrierState.searchlightLevel() != 1
                || carrierState.flareLevel() != 1
                || carrierState.catapultLevel() != 3
                || carrierState.turbineLevel() != 5
                || carrierState.torpedoSpeedLevel() != 3) {
            helper.fail("carrier equipment behavior state should map representative special gear");
            return;
        }

        SimpleContainer routeInventory = new SimpleContainer(LegacyShipEntity.SHIP_SLOT_COUNT);
        routeInventory.setItem(0, new ItemStack(ModItems.EQUIPCOMPASS.get()));
        routeInventory.setItem(1, new ItemStack(ModItems.EQUIPDRUM_ITEMS.get(2).get()));
        routeInventory.setItem(2, new ItemStack(ModItems.EQUIPRADAR_ITEMS.get(7).get()));

        ShipEquipmentBehaviorState routeState = ShipEquipmentBehaviorState.fromInventory(routeInventory, ShipArchetype.DESTROYER);
        if (!routeState.autonomousRoute() || routeState.transportTier() < 2 || routeState.sonarLevel() < 2) {
            helper.fail("compass, drum, and sonar should map into autonomous route, transport, and sonar behavior");
            return;
        }

        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void marriageBonusStatsApply(GameTestHelper helper) {
        LegacyShipStats unmarried = LegacyShipStats.create(
                0,
                ShipArchetype.DESTROYER,
                false,
                50,
                6000,
                0,
                0,
                0,
                0,
                0,
                0,
                false,
                TeitokuData.DEFAULT_FORMATION_ID,
                ShipEquipmentProfile.EMPTY,
                java.util.List.of());
        LegacyShipStats married = LegacyShipStats.create(
                0,
                ShipArchetype.DESTROYER,
                false,
                50,
                6000,
                0,
                0,
                0,
                0,
                0,
                0,
                true,
                TeitokuData.DEFAULT_FORMATION_ID,
                ShipEquipmentProfile.EMPTY,
                java.util.List.of());

        if (married.get(0) <= unmarried.get(0)
                || married.defense() <= unmarried.defense()
                || married.dodge() <= unmarried.dodge()
                || married.healRate() <= unmarried.healRate()) {
            helper.fail("married stats should provide a persistent survivability bonus");
            return;
        }

        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void marriageLevelCapAndReset(GameTestHelper helper) {
        LegacyShipEntity ship = ModEntityTypes.LEGACY_SHIP.get().create(helper.getLevel());
        if (ship == null) {
            helper.fail("legacy ship entity should be creatable in tests");
            return;
        }

        ship.setShipLevel(120);
        if (ship.getShipLevel() != 100 || ship.getLevelCap() != 100) {
            helper.fail("unmarried ship level should clamp to the 100 cap");
            return;
        }

        ship.setMarried(true);
        ship.setShipLevel(120);
        if (ship.getShipLevel() != 120 || ship.getLevelCap() != 150) {
            helper.fail("married ship should be able to grow beyond level 100 up to 150");
            return;
        }

        ship.setMarried(false);
        if (ship.getShipLevel() != 100 || ship.getLevelCap() != 100) {
            helper.fail("removing marriage should clamp the ship back to the normal level cap");
            return;
        }

        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void marriedShipTicksApplyLegacyRingAuras(GameTestHelper helper) {
        LegacyShipEntity submarine = ModEntityTypes.LEGACY_SHIP.get().create(helper.getLevel());
        LegacyShipEntity carrier = ModEntityTypes.LEGACY_SHIP.get().create(helper.getLevel());
        LegacyShipEntity escort = ModEntityTypes.LEGACY_SHIP.get().create(helper.getLevel());
        if (submarine == null || carrier == null || escort == null) {
            helper.fail("legacy ship entities should be creatable for ring passive tests");
            return;
        }

        UUID ownerUuid = UUID.randomUUID();
        submarine.setVariantEggMeta(40);
        submarine.setOwner(ownerUuid, "PassiveTester", 91);
        submarine.setMarried(true);
        submarine.setShipLevel(60);
        submarine.setGrudge(200);
        submarine.setPos(1.0D, 2.0D, 1.0D);
        helper.getLevel().addFreshEntity(submarine);

        carrier.setVariantEggMeta(49);
        carrier.setOwner(ownerUuid, "PassiveTester", 91);
        carrier.setMarried(true);
        carrier.setShipLevel(85);
        carrier.setGrudge(200);
        carrier.setPos(3.0D, 2.0D, 1.0D);
        helper.getLevel().addFreshEntity(carrier);

        escort.setVariantEggMeta(58);
        escort.setOwner(ownerUuid, "PassiveTester", 91);
        escort.setPos(4.0D, 2.0D, 1.0D);
        helper.getLevel().addFreshEntity(escort);

        for (int i = 0; i < 128; i++) {
            submarine.tick();
            carrier.tick();
        }

        if (!submarine.hasEffect(MobEffects.INVISIBILITY)) {
            helper.fail("married U511/Ro500 variants should keep the legacy self invisibility aura");
            return;
        }

        if (!escort.hasEffect(MobEffects.JUMP)) {
            helper.fail("married Kaga/Akagi variants should keep the legacy jump aura for allied ships");
            return;
        }

        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void shipRingEffectToggleSuppressesLegacyMarriagePassiveAndPersists(GameTestHelper helper) {
        Player player = helper.makeMockPlayer();
        player.setPos(2.0D, 2.0D, 2.0D);
        if (!configureTeitoku(helper, player, data -> data.setPlayerUid(8104))) {
            return;
        }

        LegacyShipEntity ship = createOwnedShip(helper, player, 40, 2.5D, 2.0D, 2.5D);
        if (ship == null) {
            return;
        }

        ship.setMarried(true);
        ship.setShipLevel(90);
        ship.setGrudge(200);
        ShipInventoryMenu menu = new ShipInventoryMenu(7, player.getInventory(), ship.getId(), ship.getShipUid());
        if (!menu.isRingEffectEnabled()) {
            helper.fail("legacy marriage aura switch should default to enabled");
            return;
        }

        if (!ShipCommandService.handleForTesting(player, ServerboundShipCommandPacket.toggleRingEffect(ship.getId(), ship.getShipUid()))
                || ship.isRingEffectEnabled()) {
            helper.fail("ship command toggle should disable the legacy marriage aura switch");
            return;
        }

        CompoundTag saved = new CompoundTag();
        ship.saveWithoutId(saved);
        if (!saved.contains("WedEffect") || saved.getBoolean("WedEffect")) {
            helper.fail("ship save data should persist the legacy WedEffect flag when the aura switch is disabled");
            return;
        }

        for (int i = 0; i < 128; i++) {
            ship.tick();
        }
        if (ship.hasEffect(MobEffects.INVISIBILITY)) {
            helper.fail("disabled marriage aura switch should suppress legacy self aura effects");
            return;
        }

        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void clearOwnerResetsCommandState(GameTestHelper helper) {
        LegacyShipEntity ship = ModEntityTypes.LEGACY_SHIP.get().create(helper.getLevel());
        if (ship == null) {
            helper.fail("legacy ship entity should be creatable in tests");
            return;
        }

        ship.setOwner(UUID.randomUUID(), "TestAdmiral", 77);
        ship.commandMoveTo(new BlockPos(2, 2, 2), helper.getLevel().dimension().location().toString());
        ship.setOrderedToSit(true);

        CompoundTag before = new CompoundTag();
        ship.saveWithoutId(before);
        if (!before.contains("OwnerUuid") || !before.contains("CommandPos") || !before.getBoolean("OrderedToSit")) {
            helper.fail("precondition failed: ship should save owner, command state, and sit state before clearing");
            return;
        }

        ship.clearOwner();

        CompoundTag after = new CompoundTag();
        ship.saveWithoutId(after);
        if (after.contains("OwnerUuid")
                || after.contains("CommandPos")
                || after.contains("GuardEntity")
                || after.contains("RouteNode")
                || after.getBoolean("OrderedToSit")) {
            helper.fail("clearing owner should wipe ownership-bound command and sit state");
            return;
        }

        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void morphProfilesPersistAndCycle(GameTestHelper helper) {
        TeitokuData data = new TeitokuData();
        if (!data.unlockMorph(58) || !data.unlockMorph(2058)) {
            helper.fail("morph unlock should register unique profiles");
            return;
        }

        data.getMorphRuntimeState().setActive(true);
        data.getMorphRuntimeState().setSpecialCooldown(80);
        if (!data.cycleMorphProfile(true)) {
            helper.fail("cycling unlocked morph profiles should succeed");
            return;
        }

        int selected = data.getMorphRuntimeState().getSelectedClassId();
        if (selected != 2058 || !data.hasActiveMorph()) {
            helper.fail("cycling morph profiles should advance selection while keeping the runtime active");
            return;
        }

        CompoundTag saved = data.saveToTag(new CompoundTag());
        TeitokuData loaded = new TeitokuData();
        loaded.loadFromTag(saved);

        if (loaded.getMorphProfileCount() != 2
                || loaded.getMorphRuntimeState().getSelectedClassId() != 2058
                || !loaded.hasActiveMorph()
                || loaded.findMorphProfile(58) == null
                || loaded.getMorphRuntimeState().getSpecialCooldown() != 80) {
            helper.fail("morph profiles and runtime state should persist through teitoku save data");
            return;
        }

        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void inactiveMorphRightClickFallsThroughWithoutError(GameTestHelper helper) {
        Player player = helper.makeMockPlayer();
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(ModItems.SHIPSPAWNEGG_ITEMS.get(20).get()));

        if (MorphHelper.getSelectedProfile(player) != null || MorphHelper.getActiveProfile(player) != null) {
            helper.fail("fresh player should expose no selected or active morph profile to item right-click handlers");
            return;
        }

        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void friendlyShipDeathDropsOwnerLockedRecoveryEgg(GameTestHelper helper) {
        Player player = helper.makeMockPlayer();
        LegacyShipEntity ship = ModEntityTypes.LEGACY_SHIP.get().create(helper.getLevel());
        if (ship == null) {
            helper.fail("legacy ship entity should be creatable for death-state coverage");
            return;
        }

        ship.setPos(1.5D, 2.0D, 1.5D);
        helper.getLevel().addFreshEntity(ship);
        ship.applySpawnSpec(ShipEntitySpecs.getByEggMeta(60), player);
        for (ItemEntity itemEntity : helper.getLevel().getEntitiesOfClass(ItemEntity.class, ship.getBoundingBox().inflate(8.0D))) {
            itemEntity.discard();
        }
        ship.getShipInventory().setItem(LegacyShipEntity.EQUIPMENT_SLOT_COUNT, new ItemStack(Items.APPLE, 3));

        if (LegacyShipSpawnEggItem.createRecoveredShipStack(ship).isEmpty()) {
            helper.fail("friendly ship recovery stack should resolve to the matching shipegg item");
            return;
        }

        if (!ship.hurt(ship.damageSources().generic(), 10000.0F)) {
            helper.fail("lethal test damage should be accepted by the ship");
            return;
        }
        if (!ship.isDeadOrDying() || ship.getHealth() > 0.0F) {
            helper.fail("lethal friendly damage should leave the ship in the vanilla death path before recovery egg pickup");
            return;
        }

        ship.setMorale(ship.getMorale() + 1);
        if (!ship.isDeadOrDying() || ship.getHealth() > 0.0F) {
            helper.fail("variant refresh must not revive a dead ship after inventory drops");
            return;
        }
        if (ship.mobInteract(player, InteractionHand.MAIN_HAND) != InteractionResult.PASS) {
            helper.fail("dead or dying ships must not open inventory or accept command interactions");
            return;
        }

        ItemEntity recoveryEgg = null;
        boolean droppedCargo = false;
        StringBuilder nearbyItems = new StringBuilder();
        for (ItemEntity itemEntity : helper.getLevel().getEntitiesOfClass(ItemEntity.class, ship.getBoundingBox().inflate(4.0D))) {
            ItemStack stack = itemEntity.getItem();
            if (!nearbyItems.isEmpty()) {
                nearbyItems.append(", ");
            }
            nearbyItems.append(stack.getItem()).append(" x").append(stack.getCount()).append(" tag=").append(stack.hasTag());
            if (LegacyShipSpawnEggItem.isRecoveredShipStack(stack)) {
                recoveryEgg = itemEntity;
            }
            if (stack.is(Items.APPLE)) {
                droppedCargo = true;
            }
        }
        if (droppedCargo) {
            helper.fail("friendly ship death must keep ship inventory inside the recovered egg instead of dropping cargo; nearby=" + nearbyItems);
            return;
        }
        if (recoveryEgg == null) {
            helper.fail("friendly ship death should spawn a recovered ship egg item");
            return;
        }
        CompoundTag itemEntityData = recoveryEgg.saveWithoutId(new CompoundTag());
        boolean ownerLocked = itemEntityData.hasUUID("Target") && player.getUUID().equals(itemEntityData.getUUID("Target"));
        ownerLocked = ownerLocked || itemEntityData.hasUUID("Owner") && player.getUUID().equals(itemEntityData.getUUID("Owner"));
        if (!ownerLocked) {
            helper.fail("recovered ship egg item should be owner-locked");
            return;
        }

        CompoundTag recoveredData = LegacyShipSpawnEggItem.getRecoveredShipData(recoveryEgg.getItem());
        LegacyShipEntity restored = ModEntityTypes.LEGACY_SHIP.get().create(helper.getLevel());
        if (restored == null || recoveredData == null) {
            helper.fail("recovered egg should carry restorable ship data");
            return;
        }
        restored.readAdditionalSaveData(recoveredData);
        restored.restoreRecoveredDeployment(player);
        if (!restored.isOwnedBy(player)
                || restored.getVariantEggMeta() != 60
                || !restored.getShipInventory().getItem(LegacyShipEntity.EQUIPMENT_SLOT_COUNT).is(Items.APPLE)
                || restored.getHealth() <= 0.0F) {
            helper.fail("recovered egg should restore owner, variant, cargo, and living health on redeploy");
            return;
        }

        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void recoveredShipEggPickupAndRedeployRequireLegacyOwner(GameTestHelper helper) {
        ServerPlayer owner = FakePlayerFactory.get(helper.getLevel(),
                new GameProfile(UUID.randomUUID(), "RecoveredOwner"));
        ServerPlayer intruder = FakePlayerFactory.get(helper.getLevel(),
                new GameProfile(UUID.randomUUID(), "RecoveredIntruder"));
        LegacyShipEntity ship = ModEntityTypes.LEGACY_SHIP.get().create(helper.getLevel());
        if (ship == null) {
            helper.fail("legacy ship entity should be creatable for recovered-owner gate coverage");
            return;
        }

        ship.setVariantEggMeta(60);
        ship.finalizeSpawn(helper.getLevel(), helper.getLevel().getCurrentDifficultyAt(BlockPos.ZERO),
                net.minecraft.world.entity.MobSpawnType.SPAWN_EGG, null, null);
        ship.setOwner(owner.getUUID(), owner.getGameProfile().getName(), 7101);
        ItemStack recoveredStack = LegacyShipSpawnEggItem.createRecoveredShipStack(ship);
        if (recoveredStack.isEmpty()) {
            helper.fail("recovered ship stack should resolve to the matching shipegg item");
            return;
        }

        if (!LegacyShipSpawnEggItem.canPlayerAccessRecoveredShip(recoveredStack, owner)) {
            helper.fail("recovered ship owner should pass the restored legacy owner gate");
            return;
        }
        if (LegacyShipSpawnEggItem.canPlayerAccessRecoveredShip(recoveredStack, intruder)) {
            helper.fail("non-owner should fail the restored recovered-egg owner gate");
            return;
        }

        ItemEntity droppedEgg = new ItemEntity(helper.getLevel(), 1.5D, 2.0D, 1.5D, recoveredStack.copy());
        EntityItemPickupEvent blockedPickup = new EntityItemPickupEvent(intruder, droppedEgg);
        TeitokuEvents.onRecoveredShipEggPickup(blockedPickup);
        if (!blockedPickup.isCanceled()) {
            helper.fail("non-owner pickup event should be canceled for recovered ship eggs");
            return;
        }

        EntityItemPickupEvent ownerPickup = new EntityItemPickupEvent(owner, droppedEgg);
        TeitokuEvents.onRecoveredShipEggPickup(ownerPickup);
        if (ownerPickup.isCanceled()) {
            helper.fail("owner pickup event should remain allowed for recovered ship eggs");
            return;
        }

        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void worldCombatRulesNormalizeAndToggle(GameTestHelper helper) {
        WorldCombatRulesSavedData data = new WorldCombatRulesSavedData();
        if (!data.toggleUnattackable(" Minecraft:Villager ")) {
            helper.fail("toggling a new class should add it to the world shield");
            return;
        }
        if (!data.isUnattackable("minecraft:villager")) {
            helper.fail("world shield lookups should normalize entity class strings");
            return;
        }
        if (data.toggleUnattackable("minecraft:villager")) {
            helper.fail("toggling the same class again should remove it");
            return;
        }
        if (data.isUnattackable("minecraft:villager")) {
            helper.fail("removed world shield classes should no longer match");
            return;
        }

        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void routeEnergyCapacityTracksTransportTier(GameTestHelper helper) {
        LegacyShipEntity ship = ModEntityTypes.LEGACY_SHIP.get().create(helper.getLevel());
        if (ship == null) {
            helper.fail("legacy ship entity should be creatable in tests");
            return;
        }

        int baseCapacity = ship.getRouteEnergyCapacity();
        ship.getShipInventory().setItem(0, new ItemStack(ModItems.EQUIPDRUM_ITEMS.get(2).get()));
        if (ship.getRouteEnergyCapacity() <= baseCapacity) {
            helper.fail("transport equipment should increase route energy capacity alongside logistics throughput");
            return;
        }

        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void legacyCoreRouteEnergyAccessHonorsMode(GameTestHelper helper) {
        LegacyCoreBlockEntity core = LegacyCoreBlockEntity.newVolCore(BlockPos.ZERO, ModBlocks.BLOCK_VOL_CORE.get().defaultBlockState());
        if (core.receiveRouteEnergy(1200, false) != 1200 || core.getRouteEnergyStored() != 1200) {
            helper.fail("volcore route energy receive should fill the legacy power pool");
            return;
        }
        if (core.extractRouteEnergy(400, true) != 400 || core.getRouteEnergyStored() != 1200) {
            helper.fail("volcore route energy simulation should read from the legacy power pool without spending it");
            return;
        }
        if (core.extractRouteEnergy(400, false) != 400 || core.getRouteEnergyStored() != 800) {
            helper.fail("volcore route energy extraction should spend legacy stored power without a player discharge mode");
            return;
        }

        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void shipyardRouteEnergyAccessUsesPowerPool(GameTestHelper helper) {
        HeavyGrudgeBlockEntity shipyard = new HeavyGrudgeBlockEntity(BlockPos.ZERO, ModBlocks.BLOCK_GRUDGE_HEAVY.get().defaultBlockState());
        shipyard.getContainerData().set(4, 1);
        shipyard.getContainerData().set(2, 2000);

        if (shipyard.extractRouteEnergy(500, false) != 500 || shipyard.getRouteEnergyStored() != 1500) {
            helper.fail("legacy large shipyard should export route energy from its power pool");
            return;
        }
        if (shipyard.receiveRouteEnergy(250, false) != 250 || shipyard.getRouteEnergyStored() != 1750) {
            helper.fail("legacy large shipyard should accept route energy back into its power pool");
            return;
        }

        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void shipyardFuelConsumesShipTankLavaByBucket(GameTestHelper helper) {
        ItemStack tank = new ItemStack(ModItems.SHIPTANK.get());
        var handler = FluidUtil.getFluidHandler(tank).resolve();
        if (handler.isEmpty()) {
            helper.fail("ship tank should expose a fluid handler for lava fuel tests");
            return;
        }

        int filled = handler.get().fill(new FluidStack(Fluids.LAVA, 2000), IFluidHandler.FluidAction.EXECUTE);
        ItemStack filledTank = handler.get().getContainer();
        if (filled != 2000 || SmallShipyardRecipes.getFuelValue(filledTank) != 20000) {
            helper.fail("ship tank with at least one bucket of lava should be accepted as shipyard fuel");
            return;
        }

        var firstUse = SmallShipyardRecipes.consumeFuelItem(filledTank);
        if (firstUse.isEmpty() || firstUse.get().power() != 20000
                || !firstUse.get().remainder().is(ModItems.SHIPTANK.get())
                || FluidUtil.getFluidContained(firstUse.get().remainder()).map(FluidStack::getAmount).orElse(0) != 1000) {
            helper.fail("shipyard fuel should drain one lava bucket from the tank and keep the tank item");
            return;
        }

        var secondUse = SmallShipyardRecipes.consumeFuelItem(firstUse.get().remainder());
        if (secondUse.isEmpty() || secondUse.get().power() != 20000
                || FluidUtil.getFluidContained(secondUse.get().remainder()).map(FluidStack::getAmount).orElse(0) != 0) {
            helper.fail("second ship tank fuel use should drain the remaining lava bucket");
            return;
        }

        var lavaBucketUse = SmallShipyardRecipes.consumeFuelItem(new ItemStack(Items.LAVA_BUCKET));
        if (lavaBucketUse.isEmpty() || lavaBucketUse.get().power() != 20000
                || !lavaBucketUse.get().remainder().is(Items.BUCKET)) {
            helper.fail("lava bucket fuel should still return an empty bucket");
            return;
        }

        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void craneFilterMatching(GameTestHelper helper) {
        CraneBlockEntity crane = new CraneBlockEntity(BlockPos.ZERO, ModBlocks.BLOCK_CRANE.get().defaultBlockState());
        crane.setFilter(0, new ItemStack(Items.APPLE), false);
        crane.setFilter(1, new ItemStack(Items.POISONOUS_POTATO), true);
        crane.setFilter(9, new ItemStack(Items.COBBLESTONE), false);

        if (!crane.matchesTransferFilter(new ItemStack(Items.APPLE), true)) {
            helper.fail("loading filters should allow explicitly listed items");
            return;
        }

        if (crane.matchesTransferFilter(new ItemStack(Items.POISONOUS_POTATO), true)) {
            helper.fail("loading filters should reject inverted matches");
            return;
        }

        if (crane.matchesTransferFilter(new ItemStack(Items.BREAD), true)) {
            helper.fail("loading filters should reject unrelated items when a positive filter exists");
            return;
        }

        if (!crane.matchesTransferFilter(new ItemStack(Items.COBBLESTONE), false)
                || crane.matchesTransferFilter(new ItemStack(Items.DIRT), false)) {
            helper.fail("unloading filters should use the unload row independently");
            return;
        }

        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void craneRouteTransfersEnergyWithoutItemContainer(GameTestHelper helper) {
        BlockPos cranePos = helper.absolutePos(new BlockPos(1, 2, 1));
        BlockPos shipyardPos = helper.absolutePos(new BlockPos(3, 4, 1));
        helper.getLevel().setBlock(cranePos, ModBlocks.BLOCK_CRANE.get().defaultBlockState(), Block.UPDATE_ALL);
        helper.getLevel().setBlock(shipyardPos, ModBlocks.BLOCK_GRUDGE_HEAVY.get().defaultBlockState(), Block.UPDATE_ALL);
        for (BlockPos servantPos : LargeShipyardStructureHelper.getServantPositions(shipyardPos)) {
            helper.getLevel().setBlock(servantPos, ModBlocks.BLOCK_POLYMETAL.get().defaultBlockState(), Block.UPDATE_ALL);
        }

        if (!(helper.getLevel().getBlockEntity(cranePos) instanceof CraneBlockEntity crane)) {
            helper.fail("crane block entity should exist for route transfer");
            return;
        }
        if (!(helper.getLevel().getBlockEntity(shipyardPos) instanceof HeavyGrudgeBlockEntity shipyard) || !shipyard.tryAssembleStructure()) {
            helper.fail("formed heavy grudge multiblock should expose route energy for crane transfer");
            return;
        }

        crane.setPairedChest(shipyardPos);
        crane.cycleEnergyMode();
        shipyard.getContainerData().set(2, 1600);

        LegacyShipEntity ship = ModEntityTypes.LEGACY_SHIP.get().create(helper.getLevel());
        if (ship == null) {
            helper.fail("legacy ship entity should be creatable in tests");
            return;
        }

        ship.setPos(cranePos.getX() + 0.5D, cranePos.getY() + 0.1D, cranePos.getZ() + 0.5D);
        helper.getLevel().addFreshEntity(ship);
        ship.commandMoveTo(cranePos, "");

        for (int i = 0; i < 3; i++) {
            ship.tick();
        }

        if (ship.getRouteEnergyBuffer() <= 0 || shipyard.getRouteEnergyStored() >= 1600) {
            helper.fail("crane route should transfer energy from a route-energy block even when no paired item container exists");
            return;
        }

        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void craneRouteTransfersShipTankLavaFromChestToShip(GameTestHelper helper) {
        BlockPos cranePos = helper.absolutePos(new BlockPos(1, 2, 1));
        BlockPos chestPos = helper.absolutePos(new BlockPos(3, 2, 1));
        helper.getLevel().setBlock(cranePos, ModBlocks.BLOCK_CRANE.get().defaultBlockState(), Block.UPDATE_ALL);
        helper.getLevel().setBlock(chestPos, Blocks.CHEST.defaultBlockState(), Block.UPDATE_ALL);

        if (!(helper.getLevel().getBlockEntity(cranePos) instanceof CraneBlockEntity crane)) {
            helper.fail("crane block entity should exist for route fluid transfer");
            return;
        }
        if (!(helper.getLevel().getBlockEntity(chestPos) instanceof Container chest)) {
            helper.fail("paired chest should expose a container for route fluid transfer");
            return;
        }

        ItemStack sourceTank = new ItemStack(ModItems.SHIPTANK.get());
        var sourceHandler = FluidUtil.getFluidHandler(sourceTank).resolve();
        if (sourceHandler.isEmpty()) {
            helper.fail("ship tank should expose a fluid handler for crane transfer tests");
            return;
        }

        int filled = sourceHandler.get().fill(new FluidStack(Fluids.LAVA, 2000), IFluidHandler.FluidAction.EXECUTE);
        if (filled != 2000) {
            helper.fail("source ship tank should accept lava for crane transfer tests");
            return;
        }

        chest.setItem(0, sourceHandler.get().getContainer());
        crane.setPairedChest(chestPos);
        crane.cycleLiquidMode();

        LegacyShipEntity ship = ModEntityTypes.LEGACY_SHIP.get().create(helper.getLevel());
        if (ship == null) {
            helper.fail("legacy ship entity should be creatable in tests");
            return;
        }

        int shipTankSlot = LegacyShipEntity.EQUIPMENT_SLOT_COUNT;
        ship.getShipInventory().setItem(shipTankSlot, new ItemStack(ModItems.SHIPTANK.get()));
        ship.setPos(cranePos.getX() + 0.5D, cranePos.getY() + 0.1D, cranePos.getZ() + 0.5D);
        helper.getLevel().addFreshEntity(ship);
        ship.commandMoveTo(cranePos, "");

        for (int i = 0; i < 3; i++) {
            ship.tick();
        }

        int shipTankAmount = FluidUtil.getFluidContained(ship.getShipInventory().getItem(shipTankSlot))
                .map(FluidStack::getAmount)
                .orElse(0);
        int chestTankAmount = FluidUtil.getFluidContained(chest.getItem(0))
                .map(FluidStack::getAmount)
                .orElse(0);
        if (shipTankAmount <= 0 || chestTankAmount >= 2000) {
            helper.fail("crane route should move lava from a paired chest tank into a ship cargo tank");
            return;
        }

        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void craneRouteTransfersFilteredItemsBetweenChestAndShip(GameTestHelper helper) {
        BlockPos cranePos = helper.absolutePos(new BlockPos(1, 2, 1));
        BlockPos chestPos = helper.absolutePos(new BlockPos(3, 2, 1));
        helper.getLevel().setBlock(cranePos, ModBlocks.BLOCK_CRANE.get().defaultBlockState(), Block.UPDATE_ALL);
        helper.getLevel().setBlock(chestPos, Blocks.CHEST.defaultBlockState(), Block.UPDATE_ALL);

        if (!(helper.getLevel().getBlockEntity(cranePos) instanceof CraneBlockEntity crane)) {
            helper.fail("crane block entity should exist for item route transfer");
            return;
        }
        if (!(helper.getLevel().getBlockEntity(chestPos) instanceof Container chest)) {
            helper.fail("paired chest should expose a container for item route transfer");
            return;
        }

        crane.setPairedChest(chestPos);
        crane.setFilter(0, new ItemStack(Items.APPLE), false);
        chest.setItem(0, new ItemStack(Items.APPLE, 6));
        chest.setItem(1, new ItemStack(Items.BREAD, 6));

        LegacyShipEntity ship = ModEntityTypes.LEGACY_SHIP.get().create(helper.getLevel());
        if (ship == null) {
            helper.fail("legacy ship entity should be creatable in item route tests");
            return;
        }

        ship.setPos(cranePos.getX() + 0.5D, cranePos.getY() + 0.1D, cranePos.getZ() + 0.5D);
        helper.getLevel().addFreshEntity(ship);
        ship.commandMoveTo(cranePos, "");
        for (int i = 0; i < 3; i++) {
            ship.tick();
        }

        if (countItems(ship.getShipInventory(), Items.APPLE) <= 0
                || countItems(chest, Items.APPLE) >= 6
                || countItems(chest, Items.BREAD) != 6) {
            helper.fail("crane load row should move only matching chest items into ship cargo");
            return;
        }

        clearContainer(chest);
        crane.toggleLoadEnabled();
        crane.setFilter(9, new ItemStack(Items.COBBLESTONE), false);
        ship.getShipInventory().setItem(LegacyShipEntity.EQUIPMENT_SLOT_COUNT + 1, new ItemStack(Items.COBBLESTONE, 5));
        ship.getShipInventory().setItem(LegacyShipEntity.EQUIPMENT_SLOT_COUNT + 2, new ItemStack(Items.DIRT, 5));
        ship.commandMoveTo(cranePos, "");
        for (int i = 0; i < 3; i++) {
            ship.tick();
        }

        if (countItems(chest, Items.COBBLESTONE) <= 0
                || countItems(chest, Items.DIRT) != 0
                || countItems(ship.getShipInventory(), Items.DIRT) != 5) {
            helper.fail("crane unload row should move only matching ship cargo into the paired chest");
            return;
        }

        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void waypointRouteWaitsOnceThenAdvances(GameTestHelper helper) {
        BlockPos waypointPos = helper.absolutePos(new BlockPos(1, 2, 1));
        BlockPos nextWaypointPos = helper.absolutePos(new BlockPos(24, 2, 1));
        helper.getLevel().setBlock(waypointPos, ModBlocks.BLOCK_WAYPOINT.get().defaultBlockState(), Block.UPDATE_ALL);
        helper.getLevel().setBlock(nextWaypointPos, ModBlocks.BLOCK_WAYPOINT.get().defaultBlockState(), Block.UPDATE_ALL);
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                helper.getLevel().setBlock(waypointPos.offset(dx, -1, dz), Blocks.STONE.defaultBlockState(), Block.UPDATE_ALL);
            }
        }

        if (!(helper.getLevel().getBlockEntity(waypointPos) instanceof WaypointBlockEntity waypoint)) {
            helper.fail("waypoint block entity should exist for route wait test");
            return;
        }

        waypoint.setNextWaypoint(nextWaypointPos);
        waypoint.cycleStayMode();

        LegacyShipEntity ship = ModEntityTypes.LEGACY_SHIP.get().create(helper.getLevel());
        if (ship == null) {
            helper.fail("legacy ship entity should be creatable for waypoint route tests");
            return;
        }

        ship.setGrudge(100);
        ship.setNoAi(true);
        ship.setNoGravity(true);
        ship.setPos(waypointPos.getX() + 0.5D, waypointPos.getY() + 0.1D, waypointPos.getZ() + 0.5D);
        helper.getLevel().addFreshEntity(ship);
        ship.commandMoveTo(waypointPos, "");
        ship.tick();
        if (!waypointPos.equals(ship.getRouteNodePos())) {
            helper.fail("ship should keep the current waypoint while route stay ticks are active");
            return;
        }

        for (int i = 0; i < WaypointBlockEntity.stayModeToTicks(1); i++) {
            ship.tick();
        }

        if (!nextWaypointPos.equals(ship.getRouteNodePos())) {
            helper.fail("ship should advance to the next route node after waiting once at the waypoint; current="
                    + ship.getRouteNodePos() + ", expected=" + nextWaypointPos + ", shipPos=" + ship.blockPosition());
            return;
        }

        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void shipRouteAndGuardCommandsClearCombatTarget(GameTestHelper helper) {
        Player player = helper.makeMockPlayer();
        LegacyShipEntity ship = createOwnedShip(helper, player, 58, 2.5D, 2.0D, 2.5D);
        if (ship == null) {
            return;
        }

        Zombie target = new Zombie(EntityType.ZOMBIE, helper.getLevel());
        target.setPos(4.5D, 2.0D, 2.5D);
        helper.getLevel().addFreshEntity(target);

        BlockPos waypointPos = helper.absolutePos(new BlockPos(1, 2, 1));
        helper.getLevel().setBlock(waypointPos, ModBlocks.BLOCK_WAYPOINT.get().defaultBlockState(), Block.UPDATE_ALL);
        ship.setTarget(target);
        ship.commandMoveTo(waypointPos, helper.getLevel().dimension().location().toString());
        if (ship.getTarget() != null || !waypointPos.equals(ship.getRouteNodePos())) {
            helper.fail("move/route commands should clear combat target while preserving route state");
            return;
        }

        ship.setTarget(target);
        ship.commandGuardEntity(target.getUUID());
        if (ship.getTarget() != null || ship.getRouteNodePos() != null
                || !target.getUUID().equals(ship.getGuardEntityUuid())) {
            helper.fail("guard commands should clear combat target and route state while keeping guard UUID");
            return;
        }

        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void legacyCoreFuelChargesAndDischarges(GameTestHelper helper) {
        LegacyCoreBlockEntity core = LegacyCoreBlockEntity.newVolCore(BlockPos.ZERO, ModBlocks.BLOCK_VOL_CORE.get().defaultBlockState());
        ItemStack fuel = new ItemStack(ModItems.GRUDGE.get(), 1);
        int inserted = core.insertFuel(fuel);
        if (inserted != 1 || core.getFuelItemCount() != 1) {
            helper.fail("legacy core should accept grudge fuel into its internal storage");
            return;
        }

        core.cycleMode();
        for (int i = 0; i < 32; i++) {
            LegacyCoreBlockEntity.serverTick(helper.getLevel(), BlockPos.ZERO, ModBlocks.BLOCK_VOL_CORE.get().defaultBlockState(), core);
        }
        if (core.getStoredCharge() <= 0 || !core.canProvideCharge()) {
            helper.fail("active volcore should convert grudge fuel into the legacy power pool");
            return;
        }

        int chargedAmount = core.getStoredCharge();
        for (int i = 0; i < 16; i++) {
            LegacyCoreBlockEntity.serverTick(helper.getLevel(), BlockPos.ZERO, ModBlocks.BLOCK_VOL_CORE.get().defaultBlockState(), core);
        }
        if (core.getStoredCharge() >= chargedAmount) {
            helper.fail("active volcore should spend stored power over time");
            return;
        }

        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void legacyLargeShipyardPatternMatches(GameTestHelper helper) {
        BlockPos master = helper.absolutePos(new BlockPos(1, 4, 1));
        helper.getLevel().setBlock(master, ModBlocks.BLOCK_GRUDGE_HEAVY.get().defaultBlockState(), Block.UPDATE_ALL);
        for (BlockPos servantPos : LargeShipyardStructureHelper.getServantPositions(master)) {
            helper.getLevel().setBlock(servantPos, ModBlocks.BLOCK_POLYMETAL.get().defaultBlockState(), Block.UPDATE_ALL);
        }

        if (!LargeShipyardStructureHelper.isValidMaster(helper.getLevel(), master)) {
            helper.fail("legacy 3x3x3 Heavy Grudge + Polymetal structure should be recognized");
            return;
        }

        helper.getLevel().setBlock(master.below(), ModBlocks.BLOCK_POLYMETAL.get().defaultBlockState(), Block.UPDATE_ALL);
        if (LargeShipyardStructureHelper.isValidMaster(helper.getLevel(), master)) {
            helper.fail("extra polymetal in the middle layer should invalidate the legacy structure");
            return;
        }

        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void largeShipyardBuildsLargeShipEggFromCompleteRing(GameTestHelper helper) {
        BlockPos shipyardPos = helper.absolutePos(new BlockPos(4, 4, 4));
        helper.getLevel().setBlock(shipyardPos, ModBlocks.BLOCK_GRUDGE_HEAVY.get().defaultBlockState(), Block.UPDATE_ALL);
        for (BlockPos servantPos : LargeShipyardStructureHelper.getServantPositions(shipyardPos)) {
            helper.getLevel().setBlock(servantPos, ModBlocks.BLOCK_POLYMETAL.get().defaultBlockState(), Block.UPDATE_ALL);
        }

        if (!(helper.getLevel().getBlockEntity(shipyardPos) instanceof HeavyGrudgeBlockEntity shipyard) || !shipyard.tryAssembleStructure()) {
            helper.fail("heavy grudge multiblock master should exist for build-loop test");
            return;
        }

        for (int material = 0; material < 4; material++) {
            shipyard.setBuildMaterialForTest(material, LargeShipyardRecipes.MIN_AMOUNT);
        }
        shipyard.getItems().setStackInSlot(LargeShipyardRecipes.SLOT_FUEL, new ItemStack(Items.LAVA_BUCKET));

        HeavyGrudgeBlockEntity.serverTick(helper.getLevel(), shipyardPos, helper.getLevel().getBlockState(shipyardPos), shipyard);
        if (!shipyard.isStructureComplete() || shipyard.getPowerRemained() <= 0) {
            helper.fail("formed structure and fuel slot should make the large shipyard ready with stored power");
            return;
        }

        shipyard.receiveRouteEnergy(HeavyGrudgeBlockEntity.POWER_MAX, false);
        shipyard.getContainerData().set(0, ShipyardBuildTypes.SHIP);
        HeavyGrudgeBlockEntity.serverTick(helper.getLevel(), shipyardPos, helper.getLevel().getBlockState(shipyardPos), shipyard);
        if (shipyard.getPowerGoal() <= 0) {
            helper.fail("large shipyard should calculate a ship build goal from selected build materials");
            return;
        }

        shipyard.getContainerData().set(1, shipyard.getPowerGoal() - HeavyGrudgeBlockEntity.BUILD_SPEED);
        HeavyGrudgeBlockEntity.serverTick(helper.getLevel(), shipyardPos, helper.getLevel().getBlockState(shipyardPos), shipyard);
        ItemStack output = shipyard.getItems().getStackInSlot(LargeShipyardRecipes.SLOT_OUTPUT);
        if (!output.is(ModItems.SHIPSPAWNEGG_ITEMS.get(1).get())
                || shipyard.getBuildType() != ShipyardBuildTypes.NONE
                || shipyard.getBuildMaterialAmountsView()[0] != 0) {
            helper.fail("large shipyard should spend selected build materials and output a large ship egg when the build completes");
            return;
        }

        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void phaseFourMenusMutateServerStateAndExposeData(GameTestHelper helper) {
        Player player = helper.makeMockPlayer();
        player.setPos(2.0D, 2.0D, 2.0D);

        BlockPos cranePos = helper.absolutePos(new BlockPos(1, 2, 1));
        helper.getLevel().setBlock(cranePos, ModBlocks.BLOCK_CRANE.get().defaultBlockState(), Block.UPDATE_ALL);
        if (!(helper.getLevel().getBlockEntity(cranePos) instanceof CraneBlockEntity crane)) {
            helper.fail("crane block entity should exist for menu button coverage");
            return;
        }
        CraneTerminalMenu craneMenu = new CraneTerminalMenu(1, player.getInventory(), cranePos);
        if (!craneMenu.clickMenuButton(player, CraneTerminalMenu.BUTTON_TOGGLE_LOAD)
                || crane.isLoadEnabled()
                || craneMenu.isLoadEnabled()) {
            helper.fail("crane menu button should mutate and expose load state");
            return;
        }

        BlockPos waypointPos = helper.absolutePos(new BlockPos(2, 2, 1));
        helper.getLevel().setBlock(waypointPos, ModBlocks.BLOCK_WAYPOINT.get().defaultBlockState(), Block.UPDATE_ALL);
        if (!(helper.getLevel().getBlockEntity(waypointPos) instanceof WaypointBlockEntity waypoint)) {
            helper.fail("waypoint block entity should exist for menu button coverage");
            return;
        }
        WaypointTerminalMenu waypointMenu = new WaypointTerminalMenu(2, player.getInventory(), waypointPos);
        if (!waypointMenu.clickMenuButton(player, WaypointTerminalMenu.BUTTON_CYCLE_STAY)
                || waypoint.getStayMode() != 1
                || !waypointMenu.getStayLabel().getString().equals(waypoint.getStayLabel().getString())) {
            helper.fail("waypoint menu button should mutate and expose stay state");
            return;
        }

        BlockPos corePos = helper.absolutePos(new BlockPos(3, 2, 1));
        helper.getLevel().setBlock(corePos, ModBlocks.BLOCK_VOL_CORE.get().defaultBlockState(), Block.UPDATE_ALL);
        if (!(helper.getLevel().getBlockEntity(corePos) instanceof LegacyCoreBlockEntity core)) {
            helper.fail("legacy core block entity should exist for menu data coverage");
            return;
        }
        LegacyCoreMenu coreMenu = new LegacyCoreMenu(3, player.getInventory(), core);
        if (!coreMenu.clickMenuButton(player, LegacyCoreMenu.BUTTON_CYCLE_MODE)
                || core.getCoreContainerData().get(0) != 1
                || coreMenu.getMode() != 1) {
            helper.fail("legacy core menu button and ContainerData should reflect mode changes");
            return;
        }

        BlockPos shipyardPos = helper.absolutePos(new BlockPos(4, 4, 1));
        helper.getLevel().setBlock(shipyardPos, ModBlocks.BLOCK_GRUDGE_HEAVY.get().defaultBlockState(), Block.UPDATE_ALL);
        for (BlockPos servantPos : LargeShipyardStructureHelper.getServantPositions(shipyardPos)) {
            helper.getLevel().setBlock(servantPos, ModBlocks.BLOCK_POLYMETAL.get().defaultBlockState(), Block.UPDATE_ALL);
        }
        if (!(helper.getLevel().getBlockEntity(shipyardPos) instanceof HeavyGrudgeBlockEntity shipyard) || !shipyard.tryAssembleStructure()) {
            helper.fail("formed heavy grudge shipyard should exist for menu data coverage");
            return;
        }
        LargeShipyardMenu shipyardMenu = new LargeShipyardMenu(4, player.getInventory(), shipyard);
        if (!shipyardMenu.clickMenuButton(player, LargeShipyardMenu.BUTTON_SHIP_MODE)
                || shipyard.getBuildType() != ShipyardBuildTypes.SHIP
                || shipyardMenu.getBuildType() != ShipyardBuildTypes.SHIP) {
            helper.fail("large shipyard menu button and ContainerData should reflect build mode changes");
            return;
        }

        LegacyShipEntity ship = ModEntityTypes.LEGACY_SHIP.get().create(helper.getLevel());
        if (ship == null) {
            helper.fail("legacy ship entity should be creatable for ship inventory menu coverage");
            return;
        }
        ship.setVariantEggMeta(58);
        if (!configureTeitoku(helper, player, data -> {
            data.setPlayerUid(8122);
            data.setPlayerName(player.getGameProfile().getName());
            data.setHasTeam(true);
        })) {
            return;
        }
        ship.setPos(2.5D, 2.0D, 2.5D);
        ship.getShipInventory().setItem(0, new ItemStack(ModItems.EQUIPDRUM_ITEMS.get(2).get()));
        helper.getLevel().addFreshEntity(ship);
        ship.setOwner(player);

        ShipInventoryMenu shipMenu = new ShipInventoryMenu(5, player.getInventory(), ship.getId());
        if (shipMenu.getAiFlags() != ship.getAiFlagsBitmask()
                || "- / -".equals(shipMenu.getRouteEnergyText())
                || "-".equals(shipMenu.getAttackText())
                || !shipMenu.clickMenuButton(player, ShipInventoryMenu.BUTTON_TOGGLE_MODE)
                || !ship.isOrderedToSit()) {
            helper.fail("ship inventory menu should expose combat/AI route data and mutate escort mode through the server button");
            return;
        }

        if (!configureTeitoku(helper, player, data -> data.assignCurrentTeamSlot(0, ship.getShipUid()))) {
            return;
        }

        CompoundTag formationPayload = TeitokuHelper.buildGameplayStateTag(helper.getLevel(), player);
        TeitokuHelper.applyClientState(player, formationPayload);
        ship.setShipLevel(77);
        ship.setMorale(4321);
        ship.setLightAmmo(987);
        ship.setHeavyAmmo(654);
        ship.setGrudge(321);
        ship.setMarried(true);
        ship.setOrderedToSit(true);
        ship.remove(Entity.RemovalReason.UNLOADED_TO_CHUNK);

        CompoundTag offlinePayload = TeitokuHelper.buildGameplayStateTag(helper.getLevel(), player);
        TeitokuHelper.applyClientState(player, offlinePayload);
        FormationMenu formationMenu = new FormationMenu(6, player.getInventory());
        FormationMenu.SlotSnapshot snapshot = formationMenu.getSlotSnapshot(0);
        if (snapshot.empty()
                || snapshot.shipUid() != ship.getShipUid()
                || snapshot.shipLevel() != 77
                || !"4321 / 16000".equals(snapshot.moraleText())
                || !"987".equals(snapshot.lightAmmoText())
                || !"654".equals(snapshot.heavyAmmoText())
                || !"321".equals(snapshot.grudgeText())
                || snapshot.online()
                || snapshot.dead()
                || !snapshot.marriageLabel().getString().equals(Component.translatable("gui.shincolle.ship_inventory.marriage.yes").getString())
                || !snapshot.modeLabel().getString().equals(Component.translatable("gui.shincolle.entity.mode.standby").getString())) {
            helper.fail("formation menu should project legacy ship summary fields from offline ship-cache data");
            return;
        }

        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void shipCommandPacketRoundTripsTypedFields(GameTestHelper helper) {
        ServerboundShipCommandPacket[] packets = {
                ServerboundShipCommandPacket.moveTo(2, 11, 101, new BlockPos(4, 5, 6)),
                ServerboundShipCommandPacket.guard(1, 12, 102, 44),
                ServerboundShipCommandPacket.attack(1, 13, 103, 45),
                ServerboundShipCommandPacket.stop(2, 14, 104),
                ServerboundShipCommandPacket.setAiFlags(15, 105,
                        GameplayCommandHandler.AI_FLAG_AUTO_TARGET | GameplayCommandHandler.AI_FLAG_AUTO_SUPPLY),
                ServerboundShipCommandPacket.setFollowRange(16, 106, 42),
                ServerboundShipCommandPacket.toggleSit(0, 17, 107),
                ServerboundShipCommandPacket.openInventory(18, 108),
                ServerboundShipCommandPacket.toggleRingEffect(19, 109)
        };

        for (ServerboundShipCommandPacket packet : packets) {
            FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());
            ServerboundShipCommandPacket.encode(packet, buffer);
            ServerboundShipCommandPacket decoded = ServerboundShipCommandPacket.decode(buffer);
            if (decoded.action() != packet.action()
                    || decoded.mode() != packet.mode()
                    || decoded.shipId() != packet.shipId()
                    || decoded.shipUid() != packet.shipUid()
                    || decoded.targetId() != packet.targetId()
                    || decoded.value() != packet.value()
                    || (decoded.pos() == null ? packet.pos() != null : !decoded.pos().equals(packet.pos()))) {
                helper.fail("typed ship command packet should round-trip all fields for " + packet.action());
                return;
            }
        }

        if (ShipCommandAction.fromOrdinal(999) != ShipCommandAction.STOP_COMMAND) {
            helper.fail("invalid ship command action ordinal should decode to STOP_COMMAND");
            return;
        }

        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void shipCommandServiceReportsFailureReasons(GameTestHelper helper) {
        Player player = helper.makeMockPlayer();
        if (!configureTeitoku(helper, player, data -> data.setPlayerUid(8109))) {
            return;
        }
        player.setPos(2.0D, 2.0D, 2.0D);

        LegacyShipEntity ship = createOwnedShip(helper, player, 58, 2.5D, 2.0D, 2.5D);
        LegacyShipEntity ally = createOwnedShip(helper, player, 59, 4.5D, 2.0D, 2.5D);
        LegacyShipEntity farShip = createOwnedShip(helper, player, 60, 70.0D, 2.0D, 2.5D);
        if (ship == null || ally == null || farShip == null) {
            helper.fail("owned ships should be creatable for command failure reporting tests");
            return;
        }

        if (ShipCommandService.handleResultForTesting(player,
                ServerboundShipCommandPacket.moveTo(0, farShip.getId(), farShip.getShipUid(), new BlockPos(6, 2, 6)))
                != ShipCommandService.ShipCommandResult.NO_COMMANDABLE_SHIP) {
            farShip.discard();
            helper.fail("commands to owned ships >= 64 blocks away should report no-commandable-ship");
            return;
        }
        farShip.discard();

        if (ShipCommandService.handleResultForTesting(player,
                ServerboundShipCommandPacket.moveTo(0, ship.getId(), ship.getShipUid(), new BlockPos(1000, 2, 1000)))
                != ShipCommandService.ShipCommandResult.SUCCESS) {
            helper.fail("near ships in 1.12 should still accept move commands to distant target coordinates");
            return;
        }
        ship.clearCommandState();

        if (ShipCommandService.handleResultForTesting(player,
                ServerboundShipCommandPacket.guard(0, ship.getId(), ship.getShipUid(), 999999))
                != ShipCommandService.ShipCommandResult.TARGET_INVALID) {
            helper.fail("invalid guard targets should report the invalid-target failure reason");
            return;
        }

        if (ShipCommandService.handleResultForTesting(player,
                ServerboundShipCommandPacket.attack(0, ship.getId(), ship.getShipUid(), ally.getId()))
                != ShipCommandService.ShipCommandResult.TARGET_NOT_ENGAGEABLE) {
            helper.fail("attacking an allied target should report the not-engageable failure reason");
            return;
        }

        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void legacyAndTypedShipCommandsShareServerBehavior(GameTestHelper helper) {
        Player player = helper.makeMockPlayer();
        if (!configureTeitoku(helper, player, data -> data.setPlayerUid(8101))) {
            return;
        }
        player.setPos(2.0D, 2.0D, 2.0D);

        LegacyShipEntity legacyShip = createOwnedShip(helper, player, 58, 2.5D, 2.0D, 2.5D);
        LegacyShipEntity typedShip = createOwnedShip(helper, player, 59, 3.5D, 2.0D, 2.5D);
        if (legacyShip == null || typedShip == null) {
            return;
        }

        BlockPos moveTarget = new BlockPos(6, 2, 6);
        CompoundTag legacyMove = new CompoundTag();
        legacyMove.putInt(GameplayCommandHandler.TAG_MODE, 0);
        legacyMove.putInt(GameplayCommandHandler.TAG_SHIP_ID, legacyShip.getId());
        legacyMove.putInt(GameplayCommandHandler.TAG_X, moveTarget.getX());
        legacyMove.putInt(GameplayCommandHandler.TAG_Y, moveTarget.getY());
        legacyMove.putInt(GameplayCommandHandler.TAG_Z, moveTarget.getZ());
        if (!ShipCommandService.handleLegacy(player, GameplayCommandType.MOVE_TO_POS, legacyMove)
                || !ShipCommandService.handleForTesting(player, ServerboundShipCommandPacket.moveTo(0, typedShip.getId(), typedShip.getShipUid(), moveTarget))
                || !moveTarget.equals(legacyShip.getCommandedPos())
                || !moveTarget.equals(typedShip.getCommandedPos())) {
            helper.fail("legacy and typed move commands should write the same command position");
            return;
        }

        int flags = GameplayCommandHandler.AI_FLAG_AUTO_TARGET | GameplayCommandHandler.AI_FLAG_ROUTE_STAY;
        CompoundTag legacyFlags = new CompoundTag();
        legacyFlags.putInt(GameplayCommandHandler.TAG_SHIP_ID, legacyShip.getId());
        legacyFlags.putInt(GameplayCommandHandler.TAG_AI_FLAGS, flags);
        if (!ShipCommandService.handleLegacy(player, GameplayCommandType.SET_SHIP_AI_FLAGS, legacyFlags)
                || !ShipCommandService.handleForTesting(player, ServerboundShipCommandPacket.setAiFlags(typedShip.getId(), typedShip.getShipUid(), flags))
                || legacyShip.getAiFlagsBitmask() != typedShip.getAiFlagsBitmask()) {
            helper.fail("legacy and typed AI flag commands should share behavior");
            return;
        }

        CompoundTag legacyFollow = new CompoundTag();
        legacyFollow.putInt(GameplayCommandHandler.TAG_SHIP_ID, legacyShip.getId());
        legacyFollow.putInt(GameplayCommandHandler.TAG_FOLLOW_RANGE, 28);
        if (!ShipCommandService.handleLegacy(player, GameplayCommandType.SET_SHIP_FOLLOW_RANGE, legacyFollow)
                || !ShipCommandService.handleForTesting(player, ServerboundShipCommandPacket.setFollowRange(typedShip.getId(), typedShip.getShipUid(), 28))
                || legacyShip.getAiFollowRange() != typedShip.getAiFollowRange()) {
            helper.fail("legacy and typed follow-range commands should share behavior");
            return;
        }

        CompoundTag legacySit = new CompoundTag();
        legacySit.putInt(GameplayCommandHandler.TAG_SHIP_ID, legacyShip.getId());
        legacySit.putInt(GameplayCommandHandler.TAG_SHIP_UID, legacyShip.getShipUid());
        if (!ShipCommandService.handleLegacy(player, GameplayCommandType.TOGGLE_SIT_SINGLE, legacySit)
                || !ShipCommandService.handleForTesting(player, ServerboundShipCommandPacket.toggleSit(0, typedShip.getId(), typedShip.getShipUid()))
                || !legacyShip.isOrderedToSit()
                || !typedShip.isOrderedToSit()) {
            helper.fail("legacy and typed toggle-sit commands should share behavior");
            return;
        }

        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void shipCommandServiceRejectsUnauthorizedFarAndDeadShips(GameTestHelper helper) {
        Player player = helper.makeMockPlayer();
        if (!configureTeitoku(helper, player, data -> data.setPlayerUid(8102))) {
            return;
        }
        player.setPos(2.0D, 2.0D, 2.0D);

        LegacyShipEntity owned = createOwnedShip(helper, player, 58, 2.5D, 2.0D, 2.5D);
        LegacyShipEntity farOwned = createOwnedShip(helper, player, 61, 68.0D, 2.0D, 2.5D);
        LegacyShipEntity otherOwner = ModEntityTypes.LEGACY_SHIP.get().create(helper.getLevel());
        LegacyShipEntity dead = createOwnedShip(helper, player, 60, 4.5D, 2.0D, 2.5D);
        if (owned == null || farOwned == null || otherOwner == null || dead == null) {
            helper.fail("legacy ships should be creatable for command gate tests");
            return;
        }

        otherOwner.setVariantEggMeta(59);
        otherOwner.setPos(3.5D, 2.0D, 2.5D);
        helper.getLevel().addFreshEntity(otherOwner);
        otherOwner.setOwner(UUID.randomUUID(), "OtherAdmiral", 9999);
        dead.setHealth(0.0F);

        if (ShipCommandService.handleForTesting(player, ServerboundShipCommandPacket.moveTo(0, farOwned.getId(), farOwned.getShipUid(), new BlockPos(6, 2, 6)))
                || farOwned.getCommandedPos() != null) {
            helper.fail("commands to owned ships >= 64 blocks away should be ignored");
            return;
        }
        farOwned.discard();

        if (!ShipCommandService.handleForTesting(player, ServerboundShipCommandPacket.moveTo(0, owned.getId(), owned.getShipUid(), new BlockPos(1000, 2, 1000)))
                || !new BlockPos(1000, 2, 1000).equals(owned.getCommandedPos())) {
            helper.fail("ships within 64 blocks should accept distant move coordinates matching 1.12 FormationHelper");
            return;
        }
        if (ShipCommandService.handleForTesting(player, ServerboundShipCommandPacket.moveTo(0, otherOwner.getId(), otherOwner.getShipUid(), new BlockPos(6, 2, 6)))
                || otherOwner.getCommandedPos() != null) {
            helper.fail("non-owner ship commands should be ignored");
            return;
        }
        if (ShipCommandService.handleForTesting(player, ServerboundShipCommandPacket.moveTo(0, dead.getId(), dead.getShipUid(), new BlockPos(6, 2, 6)))
                || dead.getCommandedPos() != null) {
            helper.fail("dead or dying ships should reject commands");
            return;
        }

        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void singleShipCommandLoopMovesGuardsAttacksAndStops(GameTestHelper helper) {
        Player player = helper.makeMockPlayer();
        if (!configureTeitoku(helper, player, data -> data.setPlayerUid(8103))) {
            return;
        }
        player.setPos(2.0D, 2.0D, 2.0D);

        LegacyShipEntity ship = createOwnedShip(helper, player, 58, 2.5D, 2.0D, 2.5D);
        Zombie target = EntityType.ZOMBIE.create(helper.getLevel());
        if (ship == null || target == null) {
            helper.fail("ship and target should be creatable for command loop test");
            return;
        }
        target.setPos(5.0D, 2.0D, 5.0D);
        helper.getLevel().addFreshEntity(target);

        ship.setOrderedToSit(true);
        BlockPos moveTarget = new BlockPos(6, 2, 6);
        if (!ShipCommandService.handleForTesting(player, ServerboundShipCommandPacket.moveTo(0, ship.getId(), ship.getShipUid(), moveTarget))
                || !moveTarget.equals(ship.getCommandedPos())
                || ship.isOrderedToSit()) {
            helper.fail("move command should set command position and leave standby");
            return;
        }

        ship.setOrderedToSit(true);
        if (!ShipCommandService.handleForTesting(player, ServerboundShipCommandPacket.guard(0, ship.getId(), ship.getShipUid(), target.getId()))
                || !target.getUUID().equals(ship.getGuardEntityUuid())
                || ship.isOrderedToSit()) {
            helper.fail("guard command should write target UUID and leave standby");
            return;
        }

        if (!ShipCommandService.handleForTesting(player, ServerboundShipCommandPacket.attack(0, ship.getId(), ship.getShipUid(), target.getId()))
                || ship.getTarget() != target
                || ship.getGuardEntityUuid() != null) {
            helper.fail("attack command should set combat target and clear command state");
            return;
        }

        ship.commandMoveTo(moveTarget, helper.getLevel().dimension().location().toString());
        if (!ShipCommandService.handleForTesting(player, ServerboundShipCommandPacket.stop(0, ship.getId(), ship.getShipUid()))
                || ship.getTarget() != null
                || ship.getCommandedPos() != null
                || ship.getGuardEntityUuid() != null) {
            helper.fail("stop command should clear target and command state");
            return;
        }

        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void formationModeCommandsGateGroupMovesAndOffsetFollowers(GameTestHelper helper) {
        Player player = helper.makeMockPlayer();
        if (!configureTeitoku(helper, player, data -> {
            data.setPlayerUid(8104);
            data.setCurrentTeamId(0);
            data.setFormationId(0, 1);
        })) {
            return;
        }
        player.setPos(2.0D, 2.0D, 2.0D);

        LegacyShipEntity[] ships = new LegacyShipEntity[5];
        for (int index = 0; index < ships.length; index++) {
            ships[index] = createOwnedShip(helper, player, 58 + index, 2.5D + index, 2.0D, 2.5D);
            if (ships[index] == null || ships[index].getShipUid() <= 0) {
                helper.fail("owned team ship should have a ship UID");
                return;
            }
        }

        if (!configureTeitoku(helper, player, data -> {
            data.setPlayerUid(8104);
            data.setCurrentTeamId(0);
            data.setFormationId(0, 1);
            for (int index = 0; index < ships.length; index++) {
                data.assignCurrentTeamSlot(index, ships[index].getShipUid());
            }
            data.setCurrentTeamSelection(0, true);
            data.setCurrentTeamSelection(2, true);
        })) {
            return;
        }

        BlockPos selectedTarget = new BlockPos(10, 2, 10);
        if (!ShipCommandService.handleForTesting(player, ServerboundShipCommandPacket.moveTo(1,
                ServerboundShipCommandPacket.NO_ENTITY,
                ServerboundShipCommandPacket.NO_UID,
                selectedTarget))) {
            helper.fail("legacy move command should still pass through the selected-team handler");
            return;
        }
        if (ships[0].getCommandedPos() != null
                || ships[1].getCommandedPos() != null
                || ships[2].getCommandedPos() != null
                || ships[3].getCommandedPos() != null
                || ships[4].getCommandedPos() != null) {
            helper.fail("selected-team move commands should stay blocked while the current team is in an active formation");
            return;
        }

        BlockPos currentTeamTarget = new BlockPos(14, 2, 14);
        if (!ShipCommandService.handleForTesting(player, ServerboundShipCommandPacket.moveTo(2,
                ServerboundShipCommandPacket.NO_ENTITY,
                ServerboundShipCommandPacket.NO_UID,
                currentTeamTarget))) {
            helper.fail("current-team move command should affect the whole current team");
            return;
        }
        for (LegacyShipEntity ship : ships) {
            if (ship.getCommandedPos() == null) {
                helper.fail("current-team command should write a command position to every team ship");
                return;
            }
        }
        if (!currentTeamTarget.equals(ships[0].getCommandedPos())) {
            helper.fail("formation flagship should keep the requested center position");
            return;
        }
        if (currentTeamTarget.equals(ships[1].getCommandedPos())) {
            helper.fail("formation followers should use old-position offsets instead of stacking on the flagship");
            return;
        }

        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void commandRuntimeAndTeamStatePersistThroughNbt(GameTestHelper helper) {
        TeitokuData data = new TeitokuData();
        data.setPlayerUid(9001);
        data.setCurrentTeamId(3);
        data.setFormationId(3, 4);
        data.assignCurrentTeamSlot(0, 111);
        data.assignCurrentTeamSlot(1, 222);
        data.setCurrentTeamSelection(1, true);
        data.setTeamName(3, "Sortie");

        TeitokuData loadedData = new TeitokuData();
        loadedData.loadFromTag(data.saveToTag(new CompoundTag()));
        if (loadedData.getPlayerUid() != 9001
                || loadedData.getCurrentTeamId() != 3
                || loadedData.getFormationId(3) != 4
                || loadedData.getShipUid(3, 0) != 111
                || loadedData.getShipUid(3, 1) != 222
                || !loadedData.isShipSelected(3, 1)
                || !"Sortie".equals(loadedData.getTeamName(3))) {
            helper.fail("Teitoku team, formation, and selection state should persist through NBT");
            return;
        }

        LegacyShipEntity movingShip = ModEntityTypes.LEGACY_SHIP.get().create(helper.getLevel());
        LegacyShipEntity guardShip = ModEntityTypes.LEGACY_SHIP.get().create(helper.getLevel());
        if (movingShip == null || guardShip == null) {
            helper.fail("ships should be creatable for runtime persistence tests");
            return;
        }

        UUID owner = UUID.randomUUID();
        UUID guardTarget = UUID.randomUUID();
        BlockPos commandTarget = new BlockPos(8, 2, 8);
        movingShip.setVariantEggMeta(58);
        movingShip.setOwner(owner, "PersistAdmiral", 9001);
        movingShip.setOrderedToSit(true);
        movingShip.setAiAutoTarget(false);
        movingShip.setAiAllowPvp(true);
        movingShip.setAiAutoSupply(false);
        movingShip.setAiRespectRouteStay(false);
        movingShip.setAiFollowRange(36);
        movingShip.commandMoveTo(commandTarget, "minecraft:overworld");

        CompoundTag movingTag = new CompoundTag();
        movingShip.addAdditionalSaveData(movingTag);
        LegacyShipEntity loadedMovingShip = ModEntityTypes.LEGACY_SHIP.get().create(helper.getLevel());
        if (loadedMovingShip == null) {
            helper.fail("loaded moving ship should be creatable");
            return;
        }
        loadedMovingShip.readAdditionalSaveData(movingTag);
        if (!commandTarget.equals(loadedMovingShip.getCommandedPos())
                || !"minecraft:overworld".equals(loadedMovingShip.getCommandDimension())
                || !loadedMovingShip.isOrderedToSit()
                || loadedMovingShip.getAiFollowRange() != 36
                || loadedMovingShip.isAiAutoTarget()
                || !loadedMovingShip.isAiAllowPvp()
                || loadedMovingShip.isAiAutoSupply()
                || loadedMovingShip.isAiRespectRouteStay()) {
            helper.fail("ship command, standby, and AI runtime state should persist through NBT: pos="
                    + loadedMovingShip.getCommandedPos()
                    + " dim=" + loadedMovingShip.getCommandDimension()
                    + " sit=" + loadedMovingShip.isOrderedToSit()
                    + " flags=" + loadedMovingShip.getAiFlagsBitmask()
                    + " follow=" + loadedMovingShip.getAiFollowRange()
                    + " autoTarget=" + loadedMovingShip.isAiAutoTarget()
                    + " allowPvp=" + loadedMovingShip.isAiAllowPvp()
                    + " autoSupply=" + loadedMovingShip.isAiAutoSupply()
                    + " routeStay=" + loadedMovingShip.isAiRespectRouteStay());
            return;
        }

        guardShip.setVariantEggMeta(59);
        guardShip.setOwner(owner, "PersistAdmiral", 9001);
        guardShip.commandGuardEntity(guardTarget);
        CompoundTag guardTag = new CompoundTag();
        guardShip.addAdditionalSaveData(guardTag);
        LegacyShipEntity loadedGuardShip = ModEntityTypes.LEGACY_SHIP.get().create(helper.getLevel());
        if (loadedGuardShip == null) {
            helper.fail("loaded guard ship should be creatable");
            return;
        }
        loadedGuardShip.readAdditionalSaveData(guardTag);
        if (!guardTarget.equals(loadedGuardShip.getGuardEntityUuid())) {
            helper.fail("ship guard target should persist through NBT");
            return;
        }

        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void heavyGrudgeSwitchesFromCoreToShipyard(GameTestHelper helper) {
        HeavyGrudgeBlockEntity dropSource = new HeavyGrudgeBlockEntity(BlockPos.ZERO, ModBlocks.BLOCK_GRUDGE_HEAVY.get().defaultBlockState());
        dropSource.setMaterialStockForTest(0, 12);
        dropSource.setBuildMaterialForTest(0, 7);
        dropSource.setMaterialStockForTest(1, 3);
        dropSource.getContainerData().set(2, 456);
        ItemStack dropped = dropSource.createDroppedBlockStack();
        if (!dropped.is(ModBlocks.BLOCK_GRUDGE_HEAVY.get().asItem())
                || !dropped.hasTag()
                || dropped.getTag().getIntArray("mats")[0] != 19
                || dropped.getTag().getIntArray("mats")[1] != 3
                || dropped.getTag().getInt("fuel") != 456) {
            helper.fail("heavy grudge block break should preserve matsBuild + matsStock and fuel in item NBT");
            return;
        }

        HeavyGrudgeBlockEntity restored = new HeavyGrudgeBlockEntity(BlockPos.ZERO, ModBlocks.BLOCK_GRUDGE_HEAVY.get().defaultBlockState());
        restored.restoreFromPlacedStack(dropped);
        if (restored.getMaterialAmounts()[0] != 19
                || restored.getMaterialAmounts()[1] != 3
                || restored.getPowerRemained() != 456) {
            helper.fail("placing a tagged heavy grudge block should restore material stock and fuel");
            return;
        }

        BlockPos master = helper.absolutePos(new BlockPos(1, 4, 1));
        helper.getLevel().setBlock(master, ModBlocks.BLOCK_GRUDGE_HEAVY.get().defaultBlockState(), Block.UPDATE_ALL);

        if (!(helper.getLevel().getBlockEntity(master) instanceof HeavyGrudgeBlockEntity heavy)) {
            helper.fail("heavy grudge block entity should exist");
            return;
        }

        if (heavy.receiveRouteEnergy(1000, false) != 0
                || heavy.extractRouteEnergy(1000, false) != 0
                || heavy.getCapability(ForgeCapabilities.ITEM_HANDLER).isPresent()) {
            helper.fail("unformed heavy grudge must not act as an independent energy core or expose machine inventory");
            return;
        }

        HeavyGrudgeBlockEntity.serverTick(helper.getLevel(), master, helper.getLevel().getBlockState(master), heavy);
        if (heavy.isStructureComplete()) {
            helper.fail("unformed heavy grudge should not auto-form or open standalone behavior without player assembly");
            return;
        }

        for (BlockPos servantPos : LargeShipyardStructureHelper.getServantPositions(master)) {
            helper.getLevel().setBlock(servantPos, ModBlocks.BLOCK_POLYMETAL.get().defaultBlockState(), Block.UPDATE_ALL);
        }

        if (!heavy.tryAssembleStructure() || !heavy.isStructureComplete()
                || helper.getLevel().getBlockState(master).getValue(com.lulan.shincolle.block.HeavyGrudgeBlock.MBS) != 1) {
            helper.fail("right-click assembly should switch a valid legacy structure into shipyard master mode");
            return;
        }

        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void polymetalServantProxiesMasterInventory(GameTestHelper helper) {
        BlockPos master = helper.absolutePos(new BlockPos(1, 4, 1));
        helper.getLevel().setBlock(master, ModBlocks.BLOCK_GRUDGE_HEAVY.get().defaultBlockState(), Block.UPDATE_ALL);
        for (BlockPos servantPos : LargeShipyardStructureHelper.getServantPositions(master)) {
            helper.getLevel().setBlock(servantPos, ModBlocks.BLOCK_POLYMETAL.get().defaultBlockState(), Block.UPDATE_ALL);
        }

        if (!(helper.getLevel().getBlockEntity(master) instanceof HeavyGrudgeBlockEntity heavy)) {
            helper.fail("heavy grudge block entity should exist");
            return;
        }

        if (!heavy.tryAssembleStructure()) {
            helper.fail("valid heavy grudge structure should assemble before servant proxy checks");
            return;
        }

        BlockPos servantPos = LargeShipyardStructureHelper.getServantPositions(master).get(0);
        if (!(helper.getLevel().getBlockEntity(servantPos) instanceof PolymetalServantBlockEntity servant)) {
            helper.fail("polymetal servant block entity should exist");
            return;
        }

        IItemHandler itemHandler = servant.getCapability(ForgeCapabilities.ITEM_HANDLER).orElse(null);
        if (itemHandler == null) {
            helper.fail("formed polymetal servant should expose the master inventory capability");
            return;
        }

        ItemStack remainder = itemHandler.insertItem(2, new ItemStack(ModItems.ABYSSMETAL1.get(), 8), false);
        if (!remainder.isEmpty()) {
            helper.fail("servant inventory proxy should accept polymetal materials");
            return;
        }

        for (int i = 0; i < 8; i++) {
            HeavyGrudgeBlockEntity.serverTick(helper.getLevel(), master, helper.getLevel().getBlockState(master), heavy);
        }
        if (heavy.getMaterialAmounts()[3] != 8) {
            helper.fail("materials inserted through a servant block should be absorbed by the shipyard master");
            return;
        }

        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void simplifiedChineseLangCoversModernKeys(GameTestHelper helper) {
        JsonObject english = loadJsonResource("assets/shincolle/lang/en_us.json");
        JsonObject simplifiedChinese = loadJsonResource("assets/shincolle/lang/zh_cn.json");
        if (english == null || simplifiedChinese == null) {
            helper.fail("expected en_us and zh_cn language resources to be present");
            return;
        }

        for (String key : english.keySet()) {
            if (!simplifiedChinese.has(key)) {
                helper.fail("zh_cn language resource is missing key: " + key);
                return;
            }
            if (simplifiedChinese.get(key).getAsString().isBlank()) {
                helper.fail("zh_cn language resource has blank value for key: " + key);
                return;
            }
        }

        String[] representativeLocalizedKeys = {
                "block.shincolle.blockabyssium",
                "item.shincolle.abyssmetal",
                "item.shincolle.shipegg58",
                "gui.shincolle.ship_inventory.owner",
                "chat.shincolle.spawn_egg.deployed",
                "advancement.shincolle.progression.root.title"
        };
        for (String key : representativeLocalizedKeys) {
            if (english.get(key).getAsString().equals(simplifiedChinese.get(key).getAsString())) {
                helper.fail("zh_cn language key still matches English: " + key);
                return;
            }
        }

        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void playerFacingLangAvoidsMigrationPlaceholderWording(GameTestHelper helper) {
        String[] languagePaths = {
                "assets/shincolle/lang/en_us.json",
                "assets/shincolle/lang/zh_cn.json",
                "assets/shincolle/lang/zh_tw.json"
        };
        String[] bannedFragments = {
                "placeholder",
                "pending",
                "later logistics migration",
                "ported legacy equipment",
                "legacy marriage aura",
                "已迁移",
                "已遷移",
                "旧版婚戒",
                "舊版婚戒"
        };

        for (String languagePath : languagePaths) {
            JsonObject lang = loadJsonResource(languagePath);
            if (lang == null) {
                helper.fail("expected language resource to be present: " + languagePath);
                return;
            }

            for (String key : lang.keySet()) {
                String value = lang.get(key).getAsString();
                String lowered = value.toLowerCase(Locale.ROOT);
                for (String bannedFragment : bannedFragments) {
                    if (lowered.contains(bannedFragment.toLowerCase(Locale.ROOT))) {
                        helper.fail("player-facing language resource still contains migration placeholder wording: "
                                + languagePath + " :: " + key + " -> " + value);
                        return;
                    }
                }
            }
        }

        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void soundsJsonOnlyReferencesPackagedOgg(GameTestHelper helper) {
        JsonObject sounds = loadJsonResource("assets/shincolle/sounds.json");
        if (sounds == null) {
            helper.fail("expected sounds.json resource to be present");
            return;
        }

        for (String eventKey : sounds.keySet()) {
            JsonObject definition = sounds.getAsJsonObject(eventKey);
            if (!definition.has("sounds")) {
                continue;
            }
            for (JsonElement soundElement : definition.getAsJsonArray("sounds")) {
                String soundName = soundElement.isJsonObject()
                        ? soundElement.getAsJsonObject().get("name").getAsString()
                        : soundElement.getAsString();
                String relative = soundName.contains(":") ? soundName.substring(soundName.indexOf(':') + 1) : soundName;
                String resourcePath = "assets/shincolle/sounds/" + relative + ".ogg";
                try (InputStream ignored = GameplayParityGameTests.class.getClassLoader().getResourceAsStream(resourcePath)) {
                    if (ignored == null) {
                        helper.fail("missing packaged sound: " + resourcePath);
                        return;
                    }
                } catch (Exception exception) {
                    helper.fail("failed to read packaged sound: " + resourcePath + " (" + exception.getMessage() + ")");
                    return;
                }
            }
        }

        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void chestLootInjectorUsesLegacyConfigLootDefaults(GameTestHelper helper) {
        List<String> bonusChest = ChestLootInjector.legacySourceKeysForTesting(
                ResourceLocation.fromNamespaceAndPath("minecraft", "chests/spawn_bonus_chest"));
        if (!bonusChest.equals(List.of(
                "shincolle:Grudge:0",
                "shincolle:ShipSpawnEgg:2",
                "shincolle:Ammo:0"))) {
            helper.fail("spawn bonus chest loot should match the 1.12 ConfigLoot defaults");
            return;
        }

        List<String> villageBlacksmith = ChestLootInjector.legacySourceKeysForTesting(
                ResourceLocation.fromNamespaceAndPath("minecraft", "chests/village/village_weaponsmith"));
        if (!villageBlacksmith.equals(List.of(
                "shincolle:InstantConMat:0",
                "shincolle:BlockAbyssium:0",
                "shincolle:BlockPolymetal:0",
                "shincolle:ShipSpawnEgg:0"))) {
            helper.fail("1.20 village weaponsmith should carry the old village blacksmith ShinColle loot entries");
            return;
        }

        ResourceLocation[] modernOnlyTables = {
                ResourceLocation.fromNamespaceAndPath("minecraft", "chests/village/village_armorer"),
                ResourceLocation.fromNamespaceAndPath("minecraft", "chests/village/village_toolsmith"),
                ResourceLocation.fromNamespaceAndPath("minecraft", "chests/shipwreck_supply"),
                ResourceLocation.fromNamespaceAndPath("minecraft", "chests/shipwreck_treasure"),
                ResourceLocation.fromNamespaceAndPath("minecraft", "chests/underwater_ruin_small"),
                ResourceLocation.fromNamespaceAndPath("minecraft", "chests/underwater_ruin_big")
        };
        for (ResourceLocation table : modernOnlyTables) {
            if (!ChestLootInjector.legacySourceKeysForTesting(table).isEmpty()) {
                helper.fail("modern-only chest table should not receive self-created ShinColle loot: " + table);
                return;
            }
        }

        ResourceLocation mineshaft = ResourceLocation.fromNamespaceAndPath("minecraft", "chests/abandoned_mineshaft");
        List<String> legacyRandomMetaSource = List.of(
                "shincolle:TrainingBook:0",
                "shincolle:MarriageRing:0",
                "shincolle:ShipSpawnEgg:0",
                "shincolle:ShipSpawnEgg:1",
                "shincolle:EquipCannon:-1",
                "shincolle:EquipAirplane:-1",
                "shincolle:Torpedo:-1");
        if (!ChestLootInjector.legacySourceKeysForTesting(mineshaft).equals(legacyRandomMetaSource)) {
            helper.fail("mineshaft loot should keep the 1.12 random-meta equipment source entries");
            return;
        }
        int expectedConcreteEntries = 4
                + ModItems.EQUIPCANNON_DISPLAY_ITEMS.size()
                + ModItems.EQUIPAIRPLANE_DISPLAY_ITEMS.size()
                + ModItems.EQUIPTORPEDO_DISPLAY_ITEMS.size();
        if (ChestLootInjector.concreteEntryCountForTesting(mineshaft) != expectedConcreteEntries) {
            helper.fail("1.12 random metadata equipment should fan out to all modern 1.20 item variants");
            return;
        }

        if (!ChestLootInjector.legacySourceKeysForTesting(ResourceLocation.fromNamespaceAndPath(
                "minecraft", "chests/stronghold_corridor")).equals(legacyRandomMetaSource)
                || !ChestLootInjector.legacySourceKeysForTesting(ResourceLocation.fromNamespaceAndPath(
                "minecraft", "chests/end_city_treasure")).equals(legacyRandomMetaSource)) {
            helper.fail("stronghold and end city chests should share the 1.12 ConfigLoot equipment entries");
            return;
        }

        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void worldgenResourcesKeepLegacyPolymetalPlacement(GameTestHelper helper) {
        JsonObject orePlaced = loadJsonResource("data/shincolle/worldgen/placed_feature/polymetal_ore.json");
        JsonObject oceanBonusPlaced = loadJsonResource("data/shincolle/worldgen/placed_feature/polymetal_ore_ocean_bonus.json");
        JsonObject oceanBonusModifier = loadJsonResource("data/shincolle/forge/biome_modifier/add_polymetal_ore_ocean_bonus.json");
        JsonObject gravelConfigured = loadJsonResource("data/shincolle/worldgen/configured_feature/polymetal_gravel.json");
        if (orePlaced == null || oceanBonusPlaced == null || oceanBonusModifier == null || gravelConfigured == null) {
            helper.fail("expected polymetal worldgen resources to be packaged");
            return;
        }

        JsonObject oreCount = placementByType(orePlaced, "minecraft:count");
        JsonObject oceanBonusCount = placementByType(oceanBonusPlaced, "minecraft:count");
        if (oreCount == null || oreCount.get("count").getAsInt() != 7) {
            helper.fail("base polymetal ore count should match 1.12 ConfigHandler.polyOreBaseRate");
            return;
        }
        if (oceanBonusCount == null || oceanBonusCount.get("count").getAsInt() != 14) {
            helper.fail("ocean polymetal ore bonus should add two extra 1.12 base counts for ocean x3 behavior");
            return;
        }
        if (!"#minecraft:is_ocean".equals(oceanBonusModifier.get("biomes").getAsString())) {
            helper.fail("polymetal ore ocean bonus must only target 1.12 ocean biome semantics");
            return;
        }

        JsonObject config = gravelConfigured.getAsJsonObject("config");
        JsonObject radius = config.getAsJsonObject("radius").getAsJsonObject("value");
        if (config.get("half_height").getAsInt() != 1
                || radius.get("min_inclusive").getAsInt() != 1
                || radius.get("max_inclusive").getAsInt() != 2) {
            helper.fail("polymetal gravel disk should keep the 1.12 WorldGenPolyGravel radius and thickness");
            return;
        }

        String targetBlocks = config.getAsJsonObject("target").getAsJsonArray("blocks").toString();
        for (String required : List.of("minecraft:stone", "minecraft:gravel")) {
            if (!targetBlocks.contains(required)) {
                helper.fail("polymetal gravel target block list is missing 1.12 base block: " + required);
                return;
            }
        }
        if (targetBlocks.contains("minecraft:sand")
                || targetBlocks.contains("minecraft:dirt")
                || targetBlocks.contains("minecraft:clay")) {
            helper.fail("polymetal gravel target block list must only match 1.12 ConfigHandler.polyGravelBaseBlock defaults {true, true, false, false}");
            return;
        }

        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void forgeTagsMirrorLegacyOreDictionaryDefaults(GameTestHelper helper) {
        String[][] expectedTagValues = {
                {"data/forge/tags/items/ingots/abyssium.json", "shincolle:abyssmetal"},
                {"data/forge/tags/items/nuggets/abyssium.json", "shincolle:abyssnugget"},
                {"data/forge/tags/items/storage_blocks/abyssium.json", "shincolle:blockabyssium"},
                {"data/forge/tags/blocks/storage_blocks/abyssium.json", "shincolle:blockabyssium"},
                {"data/forge/tags/items/ingots/polymetal.json", "shincolle:abyssmetal1"},
                {"data/forge/tags/items/nuggets/polymetal.json", "shincolle:abyssnugget1"},
                {"data/forge/tags/items/ores/polymetal.json", "shincolle:blockpolymetalore"},
                {"data/forge/tags/blocks/ores/polymetal.json", "shincolle:blockpolymetalore"},
                {"data/forge/tags/items/storage_blocks/polymetal.json", "shincolle:blockpolymetal"},
                {"data/forge/tags/blocks/storage_blocks/polymetal.json", "shincolle:blockpolymetal"},
                {"data/forge/tags/items/grudge.json", "shincolle:grudge"},
                {"data/forge/tags/items/grudge.json", "shincolle:grudge1"},
                {"data/forge/tags/items/storage_blocks/grudge.json", "shincolle:blockgrudge"},
                {"data/forge/tags/blocks/storage_blocks/grudge.json", "shincolle:blockgrudge"},
                {"data/forge/tags/items/storage_blocks/heavy_grudge.json", "shincolle:blockgrudgeheavy"},
                {"data/forge/tags/blocks/storage_blocks/heavy_grudge.json", "shincolle:blockgrudgeheavy"},
                {"data/forge/tags/items/foods/combat_ration.json", "shincolle:combatration"},
                {"data/forge/tags/items/foods/combat_ration.json", "shincolle:combatration1"},
                {"data/forge/tags/items/foods/combat_ration.json", "shincolle:combatration2"},
                {"data/forge/tags/items/foods/combat_ration.json", "shincolle:combatration3"},
                {"data/forge/tags/items/foods/combat_ration.json", "shincolle:combatration4"},
                {"data/forge/tags/items/foods/combat_ration.json", "shincolle:combatration5"},
                {"data/forge/tags/items/paper.json", "minecraft:paper"}
        };

        for (String[] expectation : expectedTagValues) {
            if (!tagResourceContains(expectation[0], expectation[1])) {
                helper.fail("missing 1.12 OreDictionary tag mapping: " + expectation[0] + " -> " + expectation[1]);
                return;
            }
        }

        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void registeredPhaseOneResourcesResolve(GameTestHelper helper) {
        ResourceLocation removedLargeShipyardId = ResourceLocation.fromNamespaceAndPath(ShinColle.MOD_ID, "blocklarge" + "shipyard");
        if (ForgeRegistries.BLOCKS.containsKey(removedLargeShipyardId)
                || ForgeRegistries.ITEMS.containsKey(removedLargeShipyardId)
                || ForgeRegistries.BLOCK_ENTITY_TYPES.containsKey(ResourceLocation.fromNamespaceAndPath(ShinColle.MOD_ID, "large_shipyard"))) {
            helper.fail("strict 1.12 parity should not register standalone " + removedLargeShipyardId);
            return;
        }

        String removedLargeShipyardPath = "blocklarge" + "shipyard";
        String[] removedLargeShipyardResources = {
                "assets/shincolle/blockstates/" + removedLargeShipyardPath + ".json",
                "assets/shincolle/models/block/" + removedLargeShipyardPath + ".json",
                "assets/shincolle/models/block/" + removedLargeShipyardPath + "_on.json",
                "assets/shincolle/models/item/" + removedLargeShipyardPath + ".json",
                "data/shincolle/loot_tables/blocks/" + removedLargeShipyardPath + ".json",
                "data/shincolle/recipes/" + removedLargeShipyardPath + ".json"
        };
        for (String resource : removedLargeShipyardResources) {
            if (loadJsonResource(resource) != null) {
                helper.fail("strict 1.12 parity should not package standalone large shipyard resource: " + resource);
                return;
            }
        }

        List<ResourceLocation> noDropBlockLootTables = List.of(
                ModBlocks.BLOCK_GRUDGE_HEAVY.getId(),
                ModBlocks.BLOCK_LIGHT_AIR.getId(),
                ModBlocks.BLOCK_LIGHT_LIQUID.getId());

        for (var blockRegistration : ModBlocks.BLOCKS.getEntries()) {
            ResourceLocation id = blockRegistration.getId();
            if (!id.getPath().equals(id.getPath().toLowerCase(java.util.Locale.ROOT))) {
                helper.fail("registered block id must use a lowercase resource path: " + id);
                return;
            }

            String blockStatePath = "assets/" + id.getNamespace() + "/blockstates/" + id.getPath() + ".json";
            JsonObject blockState = loadJsonResource(blockStatePath);
            if (blockState == null) {
                helper.fail("registered block is missing blockstate json: " + blockStatePath);
                return;
            }
            if (!referencedModelsExist(helper, blockState, blockStatePath)) {
                return;
            }

            ResourceLocation itemModel = ResourceLocation.fromNamespaceAndPath(id.getNamespace(), "models/item/" + id.getPath() + ".json");
            if (!resourceExists(itemModel)) {
                helper.fail("registered block item is missing item model: " + itemModel);
                return;
            }

            String blockLootPath = "data/" + id.getNamespace() + "/loot_tables/blocks/" + id.getPath() + ".json";
            JsonObject blockLootTable = loadJsonResource(blockLootPath);
            if (noDropBlockLootTables.contains(id)) {
                if (blockLootTable != null) {
                    helper.fail("special no-drop block should stay without a standard block loot table: " + id);
                    return;
                }
            } else if (blockRegistration.get().asItem() != Items.AIR) {
                if (blockLootTable == null) {
                    helper.fail("registered block is missing block loot table json: " + blockLootPath);
                    return;
                }
                String expectedLootItem = id.equals(ModBlocks.BLOCK_POLYMETAL_ORE.getId())
                        ? ModItems.ABYSSMETAL1.getId().toString()
                        : id.toString();
                if (!blockLootTable.has("type")
                        || !"minecraft:block".equals(blockLootTable.get("type").getAsString())
                        || !jsonContainsString(blockLootTable, expectedLootItem)) {
                    helper.fail("registered block loot table should be a minecraft:block table with the expected legacy drop: "
                            + id + " -> " + expectedLootItem);
                    return;
                }
            }
        }

        for (var itemRegistration : ModItems.ITEMS.getEntries()) {
            ResourceLocation id = itemRegistration.getId();
            if (!id.getPath().equals(id.getPath().toLowerCase(java.util.Locale.ROOT))) {
                helper.fail("registered item id must use a lowercase resource path: " + id);
                return;
            }

            ResourceLocation itemModel = ResourceLocation.fromNamespaceAndPath(id.getNamespace(), "models/item/" + id.getPath() + ".json");
            if (!resourceExists(itemModel)) {
                helper.fail("registered item is missing item model: " + itemModel);
                return;
            }
        }

        ResourceLocation[] menuTextures = {
                ResourceLocation.fromNamespaceAndPath(ShinColle.MOD_ID, "textures/gui/gui_crane.png"),
                ResourceLocation.fromNamespaceAndPath(ShinColle.MOD_ID, "textures/gui/gui_large_shipyard.png"),
                ResourceLocation.fromNamespaceAndPath(ShinColle.MOD_ID, "textures/gui/gui_ship_inventory.png"),
                ResourceLocation.fromNamespaceAndPath(ShinColle.MOD_ID, "textures/gui/gui_small_shipyard.png"),
                ResourceLocation.fromNamespaceAndPath(ShinColle.MOD_ID, "textures/gui/gui_vol_core.png"),
                ResourceLocation.fromNamespaceAndPath(ShinColle.MOD_ID, "textures/gui/recipe_paper.png")
        };
        for (ResourceLocation texture : menuTextures) {
            if (!resourceExists(texture)) {
                helper.fail("phase 4 menu texture should be packaged at lowercase runtime path: " + texture);
                return;
            }
        }

        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void shipSoundLayerResolvesFriendlyAndHostileEvents(GameTestHelper helper) {
        JsonObject sounds = loadJsonResource("assets/shincolle/sounds.json");
        if (sounds == null) {
            helper.fail("expected sounds.json resource to be present for sound layer coverage");
            return;
        }

        LegacyShipEntity friendly = ModEntityTypes.LEGACY_SHIP.get().create(helper.getLevel());
        LegacyShipEntity hostile = ModEntityTypes.LEGACY_SHIP.get().create(helper.getLevel());
        if (friendly == null || hostile == null) {
            helper.fail("legacy ship entities should be creatable for sound source tests");
            return;
        }

        friendly.setVariantEggMeta(58);
        hostile.setVariantEggMeta(2058);
        if (friendly.getSoundSource() != SoundSource.NEUTRAL || hostile.getSoundSource() != SoundSource.HOSTILE) {
            helper.fail("friendly ships should use NEUTRAL sounds and hostile mirrors should use HOSTILE sounds");
            return;
        }

        ShipSoundType[] voiceTypes = {ShipSoundType.IDLE, ShipSoundType.HURT, ShipSoundType.DEAD, ShipSoundType.HIT};
        for (ShipSoundType type : voiceTypes) {
            if (!soundEventIsBackedBySoundsJson(helper, sounds,
                    ShinColleSoundHelper.getShipVoice(type, friendly.getShipClassId()),
                    "friendly " + type.registryName())) {
                return;
            }
            if (!soundEventIsBackedBySoundsJson(helper, sounds,
                    ShinColleSoundHelper.getShipVoice(type, hostile.getShipClassId()),
                    "hostile " + type.registryName())) {
                return;
            }
        }

        SoundEvent[] combatSounds = {
                ModSoundEvents.SHIP_FIRELIGHT.get(),
                ModSoundEvents.SHIP_FIREHEAVY.get(),
                ModSoundEvents.SHIP_AIRCRAFT.get(),
                ModSoundEvents.SHIP_HITMETAL.get()
        };
        for (SoundEvent sound : combatSounds) {
            if (!soundEventIsBackedBySoundsJson(helper, sounds, sound, "combat " + sound.getLocation())) {
                return;
            }
        }

        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void shipInventoryMenuReadsCachedStateByShipUid(GameTestHelper helper) {
        Player player = helper.makeMockPlayer();
        if (!configureTeitoku(helper, player, data -> {
            data.setPlayerUid(8110);
            data.setCurrentTeamId(0);
        })) {
            return;
        }
        player.setPos(2.0D, 2.0D, 2.0D);

        LegacyShipEntity ship = createOwnedShip(helper, player, 58, 2.5D, 2.0D, 2.5D);
        if (ship == null) {
            helper.fail("owned ship should be creatable for ship inventory cache fallback tests");
            return;
        }

        ship.setShipLevel(33);
        ship.setMorale(8200);
        ship.setMarried(true);
        ship.setNoGravity(true);
        ship.setOrderedToSit(true);
        ship.setHealth(ship.getMaxHealth() * 0.5F);
        ship.tick();
        ship.setOrderedToSit(true);
        TeitokuHelper.refreshShipCache(ship, false);
        int shipUid = ship.getShipUid();
        if (shipUid <= 0) {
            helper.fail("cached ship fallback requires a persistent ship UID");
            return;
        }

        ship.remove(Entity.RemovalReason.UNLOADED_TO_CHUNK);

        ShipInventoryMenu menu = new ShipInventoryMenu(6, player.getInventory(), ServerboundShipCommandPacket.NO_ENTITY, shipUid);
        if (menu.getShip() != null) {
            helper.fail("cache fallback menu should not resolve a live ship after unload");
            return;
        }
        if (menu.getShipUid() != shipUid) {
            helper.fail("cache fallback menu should preserve ship UID");
            return;
        }
        if (!menu.canEdit()) {
            helper.fail("cache fallback menu should keep owner edit access through ship UID state");
            return;
        }
        if (!player.getGameProfile().getName().equals(menu.getOwnerLabel().getString())) {
            helper.fail("cache fallback menu should preserve owner label");
            return;
        }
        if (!menu.getModeLabel().getString().equals(Component.translatable("gui.shincolle.entity.mode.standby").getString())) {
            helper.fail("cache fallback menu should preserve standby/follow mode");
            return;
        }
        if (!menu.getRoleLabel().getString().equals(Component.translatable("gui.shincolle.ship_inventory.role.cruiser").getString())) {
            helper.fail("cache fallback menu should preserve role label");
            return;
        }
        if (menu.getShipLevel() != 33) {
            helper.fail("cache fallback menu should preserve ship level");
            return;
        }
        if (!menu.getMarriageLabel().getString().equals(Component.translatable("gui.shincolle.ship_inventory.marriage.yes").getString())) {
            helper.fail("cache fallback menu should preserve marriage label");
            return;
        }
        if ("- / -".equals(menu.getHealthText())) {
            helper.fail("cache fallback menu should preserve health text");
            return;
        }
        if ("-".equals(menu.getAttackText())) {
            helper.fail("cache fallback menu should preserve cached combat stats");
            return;
        }
        if ("- / -".equals(menu.getRouteEnergyText())) {
            helper.fail("cache fallback menu should preserve route energy text");
            return;
        }
        if (menu.getAiFollowRange() != ship.getAiFollowRange()) {
            helper.fail("cache fallback menu should preserve AI follow range");
            return;
        }

        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void shipCommandsRefreshDetachedInventoryCacheState(GameTestHelper helper) {
        Player player = helper.makeMockPlayer();
        if (!configureTeitoku(helper, player, data -> {
            data.setPlayerUid(8111);
            data.setCurrentTeamId(0);
        })) {
            return;
        }
        player.setPos(2.0D, 2.0D, 2.0D);

        LegacyShipEntity ship = createOwnedShip(helper, player, 58, 2.5D, 2.0D, 2.5D);
        if (ship == null) {
            helper.fail("owned ship should be creatable for detached ship cache refresh tests");
            return;
        }

        TeitokuHelper.refreshShipCache(ship, false);
        int shipUid = ship.getShipUid();
        int nextFlags = GameplayCommandHandler.AI_FLAG_ALLOW_PVP | GameplayCommandHandler.AI_FLAG_ROUTE_STAY;

        if (!ShipCommandService.handleForTesting(player, ServerboundShipCommandPacket.toggleSit(0, ship.getId(), shipUid))
                || !ShipCommandService.handleForTesting(player, ServerboundShipCommandPacket.setAiFlags(ship.getId(), shipUid, nextFlags))
                || !ShipCommandService.handleForTesting(player, ServerboundShipCommandPacket.setFollowRange(ship.getId(), shipUid, 30))) {
            helper.fail("ship commands should succeed before detached cache refresh validation");
            return;
        }

        ship.remove(Entity.RemovalReason.UNLOADED_TO_CHUNK);

        ShipInventoryMenu menu = new ShipInventoryMenu(7, player.getInventory(), ServerboundShipCommandPacket.NO_ENTITY, shipUid);
        if (!menu.getModeLabel().getString().equals(Component.translatable("gui.shincolle.entity.mode.standby").getString())) {
            helper.fail("detached ship inventory cache should refresh standby state after toggle-sit commands");
            return;
        }
        if (menu.getAiFlags() != nextFlags) {
            helper.fail("detached ship inventory cache should refresh AI flag state after ship commands");
            return;
        }
        if (menu.getAiFollowRange() != 30) {
            helper.fail("detached ship inventory cache should refresh follow range after ship commands");
            return;
        }

        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void shipWorldCachePreservesLegacySnapshotFields(GameTestHelper helper) {
        LegacyShipEntity ship = ModEntityTypes.LEGACY_SHIP.get().create(helper.getLevel());
        if (ship == null) {
            helper.fail("legacy ship entity should be creatable for cache tests");
            return;
        }

        ship.setVariantEggMeta(62);
        ship.setOwner(UUID.randomUUID(), "CacheAdmiral", 77);
        ship.setCustomName(Component.literal("Cache Kongou"));
        ship.setPos(12.8D, 5.2D, -3.7D);
        helper.getLevel().addFreshEntity(ship);
        ship.tick();

        ShipCacheSavedData data = new ShipCacheSavedData();
        data.updateFromShip(ship, false);
        ShipWorldCacheEntry liveEntry = data.getShip(ship.getShipUid());

        if (liveEntry == null
                || liveEntry.legacyClassId() != ship.getShipClassId()
                || liveEntry.variantEggMeta() != 62
                || liveEntry.ownerUid() != 77
                || !liveEntry.online()
                || liveEntry.dead()
                || !"Cache Kongou".equals(liveEntry.resolveDisplayName().getString())) {
            helper.fail("live ship cache entries should preserve class, owner, display name, and online-state metadata");
            return;
        }

        data.updateFromShip(ship, true);
        CompoundTag saved = data.save(new CompoundTag());
        ShipCacheSavedData loaded = ShipCacheSavedData.load(saved);
        ShipWorldCacheEntry deadEntry = loaded.getShip(ship.getShipUid());

        if (deadEntry == null
                || deadEntry.online()
                || !deadEntry.dead()
                || deadEntry.entityId() != ship.getId()
                || !deadEntry.dimensionId().equals(helper.getLevel().dimension().location().toString())
                || deadEntry.entityTag().isEmpty()) {
            helper.fail("ship cache save/load should preserve dead/offline snapshots, entity id, dimension, and entity NBT");
            return;
        }

        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void shipWorldCacheMarksUnloadedShipsOffline(GameTestHelper helper) {
        Player player = helper.makeMockPlayer();
        if (!configureTeitoku(helper, player, data -> data.setPlayerUid(8112))) {
            return;
        }

        LegacyShipEntity ship = createOwnedShip(helper, player, 58, 4.5D, 2.0D, 4.5D);
        if (ship == null) {
            helper.fail("owned ship should be creatable for offline cache tests");
            return;
        }

        ship.tick();
        int shipUid = ship.getShipUid();
        if (shipUid <= 0) {
            helper.fail("offline cache tests require a persistent ship UID");
            return;
        }

        ship.remove(Entity.RemovalReason.UNLOADED_TO_CHUNK);
        ShipWorldCacheEntry unloadedEntry = ShipCacheSavedData.get(helper.getLevel()).getShip(shipUid);
        if (unloadedEntry == null
                || unloadedEntry.dead()
                || unloadedEntry.online()
                || unloadedEntry.entityTag().isEmpty()) {
            helper.fail("unloaded ships should remain cached as offline, not dead, with their NBT snapshot intact");
            return;
        }

        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void gameplayStatePayloadSyncsOfflineShipCacheAndRuntimeSlices(GameTestHelper helper) {
        Player source = helper.makeMockPlayer();
        Player clientMirror = helper.makeMockPlayer();
        String worldRuleKey = "shincolle:test_phase6_world_rule";

        int playerUid = 8113;
        if (!configureTeitoku(helper, source, data -> {
            data.setPlayerUid(playerUid);
            data.setPlayerName(source.getGameProfile().getName());
            data.setHasTeam(true);
            data.setCurrentTeamId(0);
            data.addTargetClass("minecraft:zombie");
        }) || !configureTeitoku(helper, clientMirror, TeitokuData::clearCurrentTeam)) {
            return;
        }

        TeamSavedData.get(helper.getLevel()).createTeam(playerUid, source.getGameProfile().getName(), "Phase6Fleet");
        WorldCombatRulesSavedData worldRules = WorldCombatRulesSavedData.get(helper.getLevel());
        if (!worldRules.isUnattackable(worldRuleKey)) {
            worldRules.toggleUnattackable(worldRuleKey);
        }

        LegacyShipEntity ship = createOwnedShip(helper, source, 58, 6.5D, 2.0D, 6.5D);
        if (ship == null) {
            helper.fail("owned ship should be creatable for gameplay state payload tests");
            return;
        }

        int shipUid = ship.getShipUid();
        if (shipUid <= 0) {
            helper.fail("gameplay state payload tests require a persistent ship UID");
            return;
        }

        if (!configureTeitoku(helper, source, data -> {
            data.assignCurrentTeamSlot(0, shipUid);
            PlayerSkillRuntimeState runtimeState = data.getPlayerSkillRuntimeState();
            runtimeState.setVisible(true);
            runtimeState.setHostMode(MorphHostMode.MOUNT);
            runtimeState.setSlotEnabled(0, true);
            runtimeState.setSlotEnabled(1, true);
            runtimeState.setSlotCooldown(0, 40);
            runtimeState.setSlotMaxCooldown(0, 80);
            runtimeState.setHostShipUid(shipUid);
            runtimeState.setHostClassId(ship.getShipClassId());
        })) {
            return;
        }

        ship.remove(Entity.RemovalReason.UNLOADED_TO_CHUNK);
        CompoundTag payload = TeitokuHelper.buildGameplayStateTag(helper.getLevel(), source);
        TeitokuHelper.applyClientState(clientMirror, payload);

        boolean[] clientTeitokuSynced = { false };
        TeitokuHelper.get(clientMirror).ifPresent(data -> {
            clientTeitokuSynced[0] = data.getPlayerUid() == playerUid
                    && data.hasTargetClass("minecraft:zombie")
                    && data.getCurrentTeamId() == 0
                    && data.getShipUid(0, 0) == shipUid
                    && data.getPlayerSkillRuntimeState().isVisible()
                    && data.getPlayerSkillRuntimeState().getHostMode() == MorphHostMode.MOUNT
                    && data.getPlayerSkillRuntimeState().getHostShipUid() == shipUid
                    && data.getPlayerSkillRuntimeState().getHostClassId() == ship.getShipClassId()
                    && data.getPlayerSkillRuntimeState().getSlotCooldown(0) == 40
                    && data.getPlayerSkillRuntimeState().getSlotMaxCooldown(0) == 80
                    && data.getPlayerSkillRuntimeState().isSlotEnabled(0)
                    && data.getPlayerSkillRuntimeState().isSlotEnabled(1);
        });

        if (!clientTeitokuSynced[0]) {
            helper.fail("gameplay state payload should restore teitoku target/team/player-skill runtime slices on the client mirror");
            return;
        }

        if (!TeitokuHelper.getClientTeamData().containsKey(playerUid)
                || !"Phase6Fleet".equals(TeitokuHelper.getClientTeamData().get(playerUid).getTeamName())) {
            helper.fail("gameplay state payload should carry team world data for detached UI mirrors");
            return;
        }

        if (!TeitokuHelper.getClientWorldUnattackableClasses().contains(worldRuleKey)) {
            helper.fail("gameplay state payload should carry world unattackable target classes");
            return;
        }

        ShipWorldCacheEntry cachedEntry = TeitokuHelper.getClientShipCacheEntry(shipUid);
        if (cachedEntry == null
                || cachedEntry.ownerUid() != playerUid
                || cachedEntry.dead()
                || cachedEntry.online()) {
            helper.fail("gameplay state payload should carry offline ship-cache entries for detached ship UI fallback");
            return;
        }

        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void perShipBehaviorCatalogKeepsLegacyMarriagePassiveDispatch(GameTestHelper helper) {
        assertMarriagePassive(helper, 39, LegacyShipBehaviorCatalog.MarriagePassive.ALLIED_STRENGTH_AURA);
        assertMarriagePassive(helper, 40, LegacyShipBehaviorCatalog.MarriagePassive.SELF_AND_OWNER_INVISIBILITY);
        assertMarriagePassive(helper, 41, LegacyShipBehaviorCatalog.MarriagePassive.SELF_AND_OWNER_INVISIBILITY);
        assertMarriagePassive(helper, 48, LegacyShipBehaviorCatalog.MarriagePassive.ALLIED_RESISTANCE_FIRE_AURA);
        assertMarriagePassive(helper, 49, LegacyShipBehaviorCatalog.MarriagePassive.ALLIED_JUMP_AURA);
        assertMarriagePassive(helper, 50, LegacyShipBehaviorCatalog.MarriagePassive.ALLIED_JUMP_AURA);
        assertMarriagePassive(helper, 53, LegacyShipBehaviorCatalog.MarriagePassive.OWNER_HASTE);
        assertMarriagePassive(helper, 54, LegacyShipBehaviorCatalog.MarriagePassive.OWNER_JUMP);
        assertMarriagePassive(helper, 55, LegacyShipBehaviorCatalog.MarriagePassive.OWNER_STRENGTH);
        assertMarriagePassive(helper, 56, LegacyShipBehaviorCatalog.MarriagePassive.OWNER_SPEED);
        assertMarriagePassive(helper, 58, LegacyShipBehaviorCatalog.MarriagePassive.OWNER_NIGHT_VISION);
        assertMarriagePassive(helper, 59, LegacyShipBehaviorCatalog.MarriagePassive.OWNER_NIGHT_VISION);
        assertMarriagePassive(helper, 60, LegacyShipBehaviorCatalog.MarriagePassive.NONE);
        assertMarriagePassive(helper, 61, LegacyShipBehaviorCatalog.MarriagePassive.NONE);
        assertMarriagePassive(helper, 62, LegacyShipBehaviorCatalog.MarriagePassive.ALLIED_HEALTH_BOOST_AURA);
        assertMarriagePassive(helper, 63, LegacyShipBehaviorCatalog.MarriagePassive.ALLIED_SATURATION_AURA);
        assertMarriagePassive(helper, 64, LegacyShipBehaviorCatalog.MarriagePassive.ALLIED_LUCK_AURA);
        assertMarriagePassive(helper, 65, LegacyShipBehaviorCatalog.MarriagePassive.ALLIED_NIGHT_VISION_AURA);
        assertMarriagePassive(helper, 2040, LegacyShipBehaviorCatalog.MarriagePassive.NONE);
        assertMarriagePassive(helper, 2039, LegacyShipBehaviorCatalog.MarriagePassive.NONE);

        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void perShipBehaviorCatalogRoutesAttackProfiles(GameTestHelper helper) {
        LegacyShipAttackProfile transport = LegacyShipBehaviorCatalog.attackProfile(ShipEntitySpecs.getByEggMeta(18));
        LegacyShipAttackProfile tenryuu = LegacyShipBehaviorCatalog.attackProfile(ShipEntitySpecs.getByEggMeta(58));
        LegacyShipAttackProfile kaga = LegacyShipBehaviorCatalog.attackProfile(ShipEntitySpecs.getByEggMeta(49));
        LegacyShipAttackProfile airfield = LegacyShipBehaviorCatalog.attackProfile(ShipEntitySpecs.getByEggMeta(23));

        if (transport.light() || transport.heavy() || transport.hasAirAttack()) {
            helper.fail("transport ships should keep 1.12 disabled gun and air attack flags");
            return;
        }
        if (!tenryuu.light() || !tenryuu.heavy() || tenryuu.hasAirAttack()) {
            helper.fail("cruiser behavior should expose light/heavy gun attacks without carrier air strikes");
            return;
        }
        if (kaga.light() || kaga.heavy() || !kaga.hasAirAttack()) {
            helper.fail("carrier behavior should keep 1.12 disabled gun flags and active CV air strikes");
            return;
        }
        if (!airfield.light() || !airfield.heavy() || !airfield.hasAirAttack()
                || airfield.projectileProfile(LegacyShipAttackKind.HEAVY).moveType() != LegacyShipProjectileMoveType.ARC
                || airfield.projectileProfile(LegacyShipAttackKind.HEAVY).baseSpeed() != 0.5D
                || airfield.projectileProfile(LegacyShipAttackKind.HEAVY).targetHeightFactor() != 0.1F
                || airfield.projectileProfile(LegacyShipAttackKind.HEAVY).lifeTicks() != 160) {
            helper.fail("installation behavior should keep mixed gun/air attacks and 1.12 heavy missile data");
            return;
        }

        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void perShipBehaviorCatalogRoutesSpecialCombatHooks(GameTestHelper helper) {
        assertCombatHook(helper, 38, LegacyShipBehaviorCatalog.CombatHook.SHIMAKAZE_TORPEDO_BURST);
        assertCombatHook(helper, 2038, LegacyShipBehaviorCatalog.CombatHook.SHIMAKAZE_TORPEDO_BURST);
        assertCombatHook(helper, 39, LegacyShipBehaviorCatalog.CombatHook.NAGATO_HEAVY_STRIKE);
        assertCombatHook(helper, 2039, LegacyShipBehaviorCatalog.CombatHook.NAGATO_HEAVY_STRIKE);
        assertCombatHook(helper, 48, LegacyShipBehaviorCatalog.CombatHook.YAMATO_BEAM_BARRAGE);
        assertCombatHook(helper, 2048, LegacyShipBehaviorCatalog.CombatHook.YAMATO_BEAM_BARRAGE);
        assertCombatHook(helper, 58, LegacyShipBehaviorCatalog.CombatHook.TENRYUU_DASH);
        assertCombatHook(helper, 2058, LegacyShipBehaviorCatalog.CombatHook.TENRYUU_DASH);
        assertCombatHook(helper, 59, LegacyShipBehaviorCatalog.CombatHook.TATSUTA_CONTROL_AOE);
        assertCombatHook(helper, 60, LegacyShipBehaviorCatalog.CombatHook.NONE);
        assertCombatHook(helper, 61, LegacyShipBehaviorCatalog.CombatHook.NONE);
        assertCombatHook(helper, 62, LegacyShipBehaviorCatalog.CombatHook.NONE);
        assertCombatHook(helper, 63, LegacyShipBehaviorCatalog.CombatHook.NONE);
        assertCombatHook(helper, 64, LegacyShipBehaviorCatalog.CombatHook.NONE);
        assertCombatHook(helper, 65, LegacyShipBehaviorCatalog.CombatHook.NONE);
        assertCombatHook(helper, 15, LegacyShipBehaviorCatalog.CombatHook.BATTLESHIP_RU_BARRAGE);
        assertCombatHook(helper, 16, LegacyShipBehaviorCatalog.CombatHook.BATTLESHIP_TA_RENSOUHOU);
        assertCombatHook(helper, 22, LegacyShipBehaviorCatalog.CombatHook.CARRIER_HIME_MELEE_MISSILE);
        assertCombatHook(helper, 33, LegacyShipBehaviorCatalog.CombatHook.NORTHERN_HIME_FLOATING_FORT);
        assertCombatHook(helper, 46, LegacyShipBehaviorCatalog.CombatHook.SUBMARINE_HIME_DOUBLE_MISSILE);
        assertCombatHook(helper, 49, LegacyShipBehaviorCatalog.CombatHook.NONE);
        assertCombatHook(helper, 50, LegacyShipBehaviorCatalog.CombatHook.NONE);

        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void specialCombatHooksApplyHeavyCooldowns(GameTestHelper helper) {
        int[] specialEggs = {38, 2038, 39, 2039, 48, 2048, 58, 2058, 59, 60, 61, 62, 63, 64, 65};
        for (int index = 0; index < specialEggs.length; index++) {
            LegacyShipEntity ship = ModEntityTypes.LEGACY_SHIP.get().create(helper.getLevel());
            Zombie target = EntityType.ZOMBIE.create(helper.getLevel());
            if (ship == null || target == null) {
                helper.fail("legacy ship and target should be creatable for special combat hook tests");
                return;
            }

            ship.setVariantEggMeta(specialEggs[index]);
            supplyShipForTest(ship);
            ship.setOwner(UUID.randomUUID(), "SpecialHook" + specialEggs[index]);
            ship.setShipLevel(80);
            BlockPos shipPos = helper.absolutePos(new BlockPos(1, 2, 1));
            BlockPos targetPos = helper.absolutePos(new BlockPos(3, 2, 1));
            ship.setPos(shipPos.getX() + 0.5D, shipPos.getY(), shipPos.getZ() + 0.5D);
            target.setPos(targetPos.getX() + 0.5D, targetPos.getY(), targetPos.getZ() + 0.5D);
            helper.getLevel().addFreshEntity(ship);
            helper.getLevel().addFreshEntity(target);

            boolean handled = ship.performPlayerCompatAttack(target, LegacyShipAttackKind.HEAVY);
            boolean cooledDown = ship.getCompatAttackCooldown(LegacyShipAttackKind.HEAVY) > 0;
            ship.discard();
            target.discard();

            if (!handled) {
                helper.fail("special combat hook should handle heavy attack for eggMeta " + specialEggs[index]);
                return;
            }
            if (!cooledDown) {
                helper.fail("special combat hook should set heavy cooldown for eggMeta " + specialEggs[index]);
                return;
            }
        }

        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void specialCombatHooksPreserveHeavyMisses(GameTestHelper helper) {
        LegacyShipEntity ship = ModEntityTypes.LEGACY_SHIP.get().create(helper.getLevel());
        Zombie target = EntityType.ZOMBIE.create(helper.getLevel());
        if (ship == null || target == null) {
            helper.fail("legacy ship and target should be creatable for special combat miss tests");
            return;
        }

        ship.setVariantEggMeta(39);
        supplyShipForTest(ship);
        ship.setOwner(UUID.randomUUID(), "SpecialHookMiss");
        ship.setShipLevel(1);
        BlockPos shipPos = helper.absolutePos(new BlockPos(1, 2, 1));
        BlockPos targetPos = helper.absolutePos(new BlockPos(11, 2, 1));
        ship.setPos(shipPos.getX() + 0.5D, shipPos.getY(), shipPos.getZ() + 0.5D);
        target.setPos(targetPos.getX() + 0.5D, targetPos.getY(), targetPos.getZ() + 0.5D);
        helper.getLevel().addFreshEntity(ship);
        helper.getLevel().addFreshEntity(target);
        ship.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 100, 0, false, false, false));
        target.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, 100, 0, false, false, false));
        ship.getRandom().setSeed(1L);

        float healthBefore = target.getHealth();
        boolean handled = ship.performPlayerCompatAttack(target, LegacyShipAttackKind.HEAVY);
        boolean cooledDown = ship.getCompatAttackCooldown(LegacyShipAttackKind.HEAVY) > 0;
        float healthAfter = target.getHealth();
        ship.discard();
        target.discard();

        if (!handled) {
            helper.fail("special combat hook should handle a forced heavy miss");
            return;
        }
        if (!cooledDown) {
            helper.fail("special combat hook should still set heavy cooldown after a miss");
            return;
        }
        if (healthAfter != healthBefore) {
            helper.fail("special heavy miss must not deal direct damage: before=" + healthBefore + ", after=" + healthAfter);
            return;
        }

        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void behaviorCatalogOwnsBossAndLootProfiles(GameTestHelper helper) {
        BossPhaseProfile airfield = LegacyShipBehaviorCatalog.bossPhaseProfile(ShipEntitySpecs.getByEggMeta(23));
        BossPhaseProfile carrierDemon = LegacyShipBehaviorCatalog.bossPhaseProfile(ShipEntitySpecs.getByEggMeta(35));
        BossPhaseProfile battleshipHime = LegacyShipBehaviorCatalog.bossPhaseProfile(ShipEntitySpecs.getByEggMeta(28));
        LegacyShipBehaviorCatalog.HostileLootProfile bossLoot = LegacyShipBehaviorCatalog.hostileLootProfile(
                ShipEntitySpecs.getByEggMeta(23), true, true);
        LegacyShipBehaviorCatalog.HostileLootProfile commonLoot = LegacyShipBehaviorCatalog.hostileLootProfile(
                ShipEntitySpecs.getByEggMeta(2), false, false);

        if (!airfield.actionCycle().equals(List.of(BossActionType.AREA_BOMBARD, BossActionType.SUMMON_ESCORT, BossActionType.CANNON_BURST))
                || airfield.summonEggMeta() != 14
                || airfield.baseActionCooldown() != 40
                || airfield.baseSummonCooldown() != 160) {
            helper.fail("installation boss profile should live in the behavior catalog with its legacy action cycle");
            return;
        }
        if (!carrierDemon.actionCycle().equals(List.of(BossActionType.AIR_ASSAULT, BossActionType.SUMMON_ESCORT, BossActionType.CANNON_BURST))
                || carrierDemon.summonEggMeta() != 14) {
            helper.fail("air boss profile should route through catalog metadata");
            return;
        }
        if (!battleshipHime.actionCycle().equals(List.of(BossActionType.CANNON_BURST, BossActionType.CHARGE, BossActionType.SUMMON_ESCORT))
                || battleshipHime.summonEggMeta() != 2) {
            helper.fail("princess boss profile should route through catalog metadata");
            return;
        }
        if (!bossLoot.dropsAbyssMetal() || !bossLoot.dropsAbyssMetal1() || !bossLoot.dropsBossBonus()
                || bossLoot.eggDropChance() < 0.89F) {
            helper.fail("boss loot profile should keep advanced drops and high recovered egg chance");
            return;
        }
        if (commonLoot.dropsAbyssMetal() || commonLoot.dropsAbyssMetal1() || commonLoot.dropsBossBonus()
                || commonLoot.eggDropChance() > 0.21F) {
            helper.fail("common hostile loot profile should stay lightweight");
            return;
        }

        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void shipCompatAttackFailureReasonsCoverSinglePlayerFeedback(GameTestHelper helper) {
        LegacyShipEntity ship = ModEntityTypes.LEGACY_SHIP.get().create(helper.getLevel());
        LegacyShipEntity target = ModEntityTypes.LEGACY_SHIP.get().create(helper.getLevel());
        if (ship == null || target == null) {
            helper.fail("legacy ships should be creatable for attack feedback tests");
            return;
        }

        ship.setVariantEggMeta(58);
        ship.setOwner(UUID.randomUUID(), "AttackFeedback");
        ship.setShipLevel(25);
        target.setVariantEggMeta(2049);
        target.initializeHostileRuntime(false, true, true);
        BlockPos shipPos = helper.absolutePos(new BlockPos(1, 2, 1));
        BlockPos targetPos = helper.absolutePos(new BlockPos(3, 2, 1));
        ship.setPos(shipPos.getX() + 0.5D, shipPos.getY(), shipPos.getZ() + 0.5D);
        target.setPos(targetPos.getX() + 0.5D, targetPos.getY(), targetPos.getZ() + 0.5D);
        helper.getLevel().addFreshEntity(ship);
        helper.getLevel().addFreshEntity(target);

        if (ship.getCompatAttackFailureReason(null, LegacyShipAttackKind.LIGHT)
                != LegacyShipEntity.CompatAttackFailureReason.TARGET_INVALID) {
            helper.fail("missing target should report target-invalid feedback");
            return;
        }
        if (ship.getCompatAttackFailureReason(target, LegacyShipAttackKind.AIR_HEAVY)
                != LegacyShipEntity.CompatAttackFailureReason.SKILL_UNAVAILABLE) {
            helper.fail("unsupported attack kind should report skill-unavailable feedback");
            return;
        }

        target.setPos(shipPos.getX() + 40.5D, shipPos.getY(), shipPos.getZ() + 0.5D);
        if (ship.getCompatAttackFailureReason(target, LegacyShipAttackKind.LIGHT)
                != LegacyShipEntity.CompatAttackFailureReason.OUT_OF_RANGE) {
            helper.fail("distant target should report out-of-range feedback");
            return;
        }
        target.setPos(targetPos.getX() + 0.5D, targetPos.getY(), targetPos.getZ() + 0.5D);

        ship.setLightAmmo(100);
        ship.setGrudge(0);
        if (ship.getCompatAttackFailureReason(target, LegacyShipAttackKind.LIGHT)
                != LegacyShipEntity.CompatAttackFailureReason.NO_GRUDGE) {
            helper.fail("empty grudge should report grudge feedback");
            return;
        }

        ship.setGrudge(100);
        ship.setLightAmmo(0);
        if (ship.getCompatAttackFailureReason(target, LegacyShipAttackKind.LIGHT)
                != LegacyShipEntity.CompatAttackFailureReason.NO_LIGHT_AMMO) {
            helper.fail("empty light ammo should report light-ammo feedback");
            return;
        }

        ship.setLightAmmo(100);
        target.setHealth(target.getMaxHealth());
        boolean attacked = ship.performPlayerCompatAttack(target, LegacyShipAttackKind.LIGHT);
        LegacyShipEntity.CompatAttackFailureReason reloadReason =
                ship.getCompatAttackFailureReason(target, LegacyShipAttackKind.LIGHT);
        if (!attacked || reloadReason != LegacyShipEntity.CompatAttackFailureReason.COOLDOWN) {
            helper.fail("successful player-facing attack should report cooldown while reloading: attacked="
                    + attacked + ", reason=" + reloadReason + ", cooldown="
                    + ship.getCompatAttackCooldown(LegacyShipAttackKind.LIGHT) + ", targetAlive=" + target.isAlive());
            return;
        }

        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void projectileProfilesMatchShipRoles(GameTestHelper helper) {
        LegacyShipAttackProfile kongou = LegacyShipBehaviorCatalog.attackProfile(ShipEntitySpecs.getByEggMeta(62));
        LegacyShipAttackProfile kongouHostileMirror = LegacyShipBehaviorCatalog.attackProfile(ShipEntitySpecs.getByEggMeta(2062));
        LegacyShipAttackProfile akagi = LegacyShipBehaviorCatalog.attackProfile(ShipEntitySpecs.getByEggMeta(50));
        LegacyShipAttackProfile wo = LegacyShipBehaviorCatalog.attackProfile(ShipEntitySpecs.getByEggMeta(14));
        LegacyShipAttackProfile airfield = LegacyShipBehaviorCatalog.attackProfile(ShipEntitySpecs.getByEggMeta(23));

        if (kongou.projectileProfile(LegacyShipAttackKind.HEAVY).visual() != LegacyShipProjectileVisual.MISSILE
                || kongou.projectileProfile(LegacyShipAttackKind.HEAVY).moveType() != LegacyShipProjectileMoveType.ARC) {
            helper.fail("friendly battleships should keep the legacy heavy missile barrage arc");
            return;
        }
        if (kongouHostileMirror.projectileProfile(LegacyShipAttackKind.HEAVY).moveType() != LegacyShipProjectileMoveType.ARC) {
            helper.fail("hostile mirror battleships should keep the same 1.12 heavy missile movement as friendly mirrors");
            return;
        }
        if (akagi.projectileProfile(LegacyShipAttackKind.AIR_HEAVY).visual() != LegacyShipProjectileVisual.TORPEDO) {
            helper.fail("friendly carriers should use torpedo-family visuals for heavy air strikes");
            return;
        }
        if (wo.projectileProfile(LegacyShipAttackKind.AIR_HEAVY).visual() != LegacyShipProjectileVisual.BOMB
                || airfield.projectileProfile(LegacyShipAttackKind.HEAVY).moveType() != LegacyShipProjectileMoveType.ARC) {
            helper.fail("abyssal CV air strikes should use bomber visuals while heavy missiles keep 1.12 arc movement");
            return;
        }

        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void registeredProjectileProfilesAreExplicit(GameTestHelper helper) {
        for (ShipEntitySpec spec : ShipEntitySpecs.values()) {
            LegacyShipAttackProfile profile = LegacyShipBehaviorCatalog.attackProfile(spec);
            if (profile.heavy()
                    && !profile.projectileProfile(LegacyShipAttackKind.HEAVY).isPresent()) {
                helper.fail("registered heavy attack must not rely on projectile fallback: egg=" + spec.eggMeta());
                return;
            }
            if (profile.airLight()
                    && !profile.projectileProfile(LegacyShipAttackKind.AIR_LIGHT).isPresent()) {
                helper.fail("registered air-light attack must not rely on projectile fallback: egg=" + spec.eggMeta());
                return;
            }
            if (profile.airHeavy()
                    && !profile.projectileProfile(LegacyShipAttackKind.AIR_HEAVY).isPresent()) {
                helper.fail("registered air-heavy attack must not rely on projectile fallback: egg=" + spec.eggMeta());
                return;
            }
        }

        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void projectileEntitySaveLoadKeepsFlavor(GameTestHelper helper) {
        LegacyShipEntity owner = ModEntityTypes.LEGACY_SHIP.get().create(helper.getLevel());
        LegacyShipEntity target = ModEntityTypes.LEGACY_SHIP.get().create(helper.getLevel());
        if (owner == null || target == null) {
            helper.fail("legacy ship entities should be creatable for projectile tests");
            return;
        }

        owner.setVariantEggMeta(62);
        target.setVariantEggMeta(23);
        owner.setPos(1.0D, 2.0D, 1.0D);
        target.setPos(10.0D, 2.0D, 1.0D);
        helper.getLevel().addFreshEntity(owner);
        helper.getLevel().addFreshEntity(target);

        owner.getShipInventory().setItem(0, new ItemStack(ModItems.EQUIPFLARE.get()));
        owner.getShipInventory().setItem(1, new ItemStack(ModItems.EQUIPSEARCHLIGHT.get()));
        owner.getShipInventory().setItem(2, new ItemStack(ModItems.EQUIPCATAPULT_ITEMS.get(3).get()));

        LegacyShipProjectileEntity projectile = LegacyShipProjectileEntity.create(helper.getLevel(), owner, target,
                LegacyShipAttackKind.HEAVY, 12.0F, false);
        CompoundTag tag = new CompoundTag();
        projectile.addAdditionalSaveData(tag);

        LegacyShipProjectileEntity loaded = new LegacyShipProjectileEntity(ModEntityTypes.LEGACY_SHIP_PROJECTILE.get(), helper.getLevel());
        loaded.readAdditionalSaveData(tag);

        if (loaded.getAttackKind() != LegacyShipAttackKind.HEAVY
                || loaded.getProjectileVisual() != LegacyShipProjectileVisual.MISSILE
                || loaded.getMoveType() != LegacyShipProjectileMoveType.ARC
                || loaded.getIntendedTargetId() != target.getId()
                || !loaded.hasFlarePayload()
                || !loaded.hasSearchlightPayload()) {
            helper.fail("projectile save/load should preserve legacy launch flavor, target, and illumination flags");
            return;
        }

        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void airAttacksLaunchAircraftEntities(GameTestHelper helper) {
        LegacyShipEntity owner = ModEntityTypes.LEGACY_SHIP.get().create(helper.getLevel());
        LegacyShipEntity target = ModEntityTypes.LEGACY_SHIP.get().create(helper.getLevel());
        if (owner == null || target == null) {
            helper.fail("legacy ship entities should be creatable for aircraft launch tests");
            return;
        }

        owner.setVariantEggMeta(50);
        supplyShipForTest(owner);
        target.setVariantEggMeta(2049);
        owner.setPos(1.0D, 2.0D, 1.0D);
        target.setPos(8.0D, 2.0D, 1.0D);
        helper.getLevel().addFreshEntity(owner);
        helper.getLevel().addFreshEntity(target);

        if (!owner.performPlayerCompatAttack(target, LegacyShipAttackKind.AIR_HEAVY)) {
            helper.fail("carrier air attacks should launch an aircraft entity");
            return;
        }

        List<LegacyShipAircraftEntity> aircraft = helper.getLevel().getEntitiesOfClass(LegacyShipAircraftEntity.class,
                owner.getBoundingBox().inflate(20.0D),
                aircraftEntity -> aircraftEntity.getOwnerId() == owner.getId()
                        && aircraftEntity.getTargetId() == target.getId());
        if (aircraft.isEmpty()) {
            helper.fail("air attacks should spawn aircraft entities into the world");
            return;
        }
        if (!helper.getLevel().getEntitiesOfClass(LegacyShipProjectileEntity.class,
                owner.getBoundingBox().inflate(20.0D),
                projectile -> projectile.getOwner() == owner
                        && projectile.getAttackKind() == LegacyShipAttackKind.AIR_HEAVY).isEmpty()) {
            helper.fail("air attacks should no longer spawn heavy air projectiles");
            return;
        }

        LegacyShipAircraftEntity launched = aircraft.get(0);
        if (launched.getAttackKind() != LegacyShipAttackKind.AIR_HEAVY || launched.getTargetId() != target.getId()) {
            helper.fail("launched aircraft should keep the requested air attack kind and target");
            return;
        }

        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void shipRuntimeSuppliesGatePersistAndRestoreLegacySupportValues(GameTestHelper helper) {
        Player player = helper.makeMockPlayer();
        LegacyShipEntity ship = ModEntityTypes.LEGACY_SHIP.get().create(helper.getLevel());
        Zombie target = EntityType.ZOMBIE.create(helper.getLevel());
        if (ship == null || target == null) {
            helper.fail("legacy ship and zombie should be creatable for ship supply tests");
            return;
        }

        ship.setVariantEggMeta(53);
        ship.setOwner(player);
        ship.setPos(1.0D, 2.0D, 1.0D);
        target.setPos(3.0D, 2.0D, 1.0D);
        helper.getLevel().addFreshEntity(ship);
        helper.getLevel().addFreshEntity(target);

        ship.setLightAmmo(0);
        ship.setHeavyAmmo(0);
        ship.setGrudge(0);
        if (ship.performPlayerCompatAttack(target, LegacyShipAttackKind.LIGHT)
                || ship.getCompatAttackCooldown(LegacyShipAttackKind.LIGHT) > 0) {
            helper.fail("friendly ranged combat should be gated by ship ammo and grudge");
            return;
        }

        ship.setLightAmmo(10);
        ship.setGrudge(20);
        int lightAmmoBefore = ship.getLightAmmo();
        int grudgeBefore = ship.getGrudge();
        if (!ship.performPlayerCompatAttack(target, LegacyShipAttackKind.LIGHT)) {
            helper.fail("friendly ranged combat should resume after runtime supplies are restored");
            return;
        }
        if (ship.getLightAmmo() >= lightAmmoBefore
                || ship.getGrudge() >= grudgeBefore) {
            helper.fail("friendly ranged combat should consume ammo and grudge once per attack");
            return;
        }

        CompoundTag saved = new CompoundTag();
        ship.addAdditionalSaveData(saved);
        LegacyShipEntity loaded = ModEntityTypes.LEGACY_SHIP.get().create(helper.getLevel());
        if (loaded == null) {
            helper.fail("legacy ship should be creatable for ship supply save/load tests");
            return;
        }
        loaded.readAdditionalSaveData(saved);
        if (loaded.getLightAmmo() != ship.getLightAmmo()
                || loaded.getHeavyAmmo() != ship.getHeavyAmmo()
                || loaded.getGrudge() != ship.getGrudge()) {
            helper.fail("ship ammo and grudge should persist through entity NBT");
            return;
        }

        CompoundTag legacyTag = new CompoundTag();
        legacyTag.putInt("VariantEggMeta", 53);
        legacyTag.putInt("NumAmmoLight", 7);
        legacyTag.putInt("NumAmmoHeavy", 9);
        legacyTag.putInt("NumGrudge", 11);
        LegacyShipEntity migrated = ModEntityTypes.LEGACY_SHIP.get().create(helper.getLevel());
        if (migrated == null) {
            helper.fail("legacy ship should be creatable for legacy supply migration tests");
            return;
        }
        migrated.readAdditionalSaveData(legacyTag);
        if (migrated.getLightAmmo() != 7 || migrated.getHeavyAmmo() != 9 || migrated.getGrudge() != 11) {
            helper.fail("modern ship runtime supplies should migrate legacy ammo and grudge tags");
            return;
        }

        ship.setLightAmmo(0);
        ship.setHeavyAmmo(0);
        ship.setGrudge(0);
        ship.setMorale(0);
        int moraleBefore = ship.getMorale();

        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(ModItems.COMBATRATION.get()));
        if (ship.mobInteract(player, InteractionHand.MAIN_HAND) != InteractionResult.CONSUME
                || ship.getLightAmmo() != 0
                || ship.getHeavyAmmo() != 0
                || ship.getMorale() <= moraleBefore
                || ship.getGrudge() <= 0) {
            helper.fail("combat rations should restore morale and grudge without rearming ships");
            return;
        }
        int grudgeAfterRation = ship.getGrudge();

        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(ModItems.GRUDGE.get()));
        if (ship.mobInteract(player, InteractionHand.MAIN_HAND) != InteractionResult.CONSUME
                || ship.getGrudge() <= grudgeAfterRation) {
            helper.fail("grudge support items should restore ship grudge runtime state");
            return;
        }

        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(ModItems.AMMO.get()));
        if (ship.mobInteract(player, InteractionHand.MAIN_HAND) != InteractionResult.CONSUME
                || ship.getLightAmmo() <= 0) {
            helper.fail("light ammo support items should reload ship light ammo runtime state");
            return;
        }

        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(ModItems.AMMO2.get()));
        if (ship.mobInteract(player, InteractionHand.MAIN_HAND) != InteractionResult.CONSUME
                || ship.getHeavyAmmo() <= 0) {
            helper.fail("heavy ammo support items should reload ship heavy ammo runtime state");
            return;
        }

        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void playerSkillRuntimeStateSaveLoadKeepsFiveSlotSnapshot(GameTestHelper helper) {
        PlayerSkillRuntimeState runtimeState = new PlayerSkillRuntimeState();
        runtimeState.setVisible(true);
        runtimeState.setHostMode(MorphHostMode.MOUNT);
        runtimeState.setHostShipUid(345);
        runtimeState.setHostClassId(62);
        runtimeState.setSlotEnabled(0, true);
        runtimeState.setSlotEnabled(1, true);
        runtimeState.setSlotEnabled(4, true);
        runtimeState.setSlotCooldown(0, 17);
        runtimeState.setSlotCooldown(4, 90);
        runtimeState.setSlotMaxCooldown(0, 45);
        runtimeState.setSlotMaxCooldown(4, 150);

        CompoundTag tag = runtimeState.saveToTag(new CompoundTag());
        PlayerSkillRuntimeState loaded = new PlayerSkillRuntimeState();
        loaded.loadFromTag(tag);

        if (!loaded.isVisible()
                || loaded.getHostMode() != MorphHostMode.MOUNT
                || loaded.getHostShipUid() != 345
                || loaded.getHostClassId() != 62
                || !loaded.isSlotEnabled(0)
                || loaded.isSlotEnabled(3)
                || loaded.getSlotCooldown(0) != 17
                || loaded.getSlotCooldown(4) != 90
                || loaded.getSlotMaxCooldown(4) != 150) {
            helper.fail("player skill runtime state should preserve host mode, enabled mask, and five-slot cooldown snapshots");
            return;
        }

        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void morphSpecialDispatchUsesLegacyClassIds(GameTestHelper helper) {
        int[] specialClassIds = {36, 37, 46, 56, 57, 58, 59, 60, 61, 62, 63};
        for (int classId : specialClassIds) {
            if (!MorphHelper.hasSpecialSkill(new MorphProfile(classId))
                    || !MorphHelper.hasSpecialSkill(new MorphProfile(classId + 2000))) {
                helper.fail("morph special dispatch should include legacy class id " + classId + " and its hostile mirror");
                return;
            }
        }

        int[] nonSpecialClassIds = {38, 39, 47, 48, 51, 52, 53, 54};
        for (int classId : nonSpecialClassIds) {
            if (MorphHelper.hasSpecialSkill(new MorphProfile(classId))
                    || MorphHelper.hasSpecialSkill(new MorphProfile(classId + 2000))) {
                helper.fail("morph special dispatch should not grant a first-batch special to legacy class id " + classId);
                return;
            }
        }

        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void morphSpecialPreviewCooldownUsesBehaviorCatalog(GameTestHelper helper) {
        int[][] cooldownCases = {
                {56, 120},
                {57, 120},
                {58, 140},
                {59, 140},
                {60, 150},
                {63, 150},
                {36, 130},
                {37, 170},
                {46, 200}
        };

        for (int[] cooldownCase : cooldownCases) {
            int classId = cooldownCase[0];
            int expectedCooldown = cooldownCase[1];
            int actualCooldown = MorphHelper.getSpecialPreviewCooldown(new MorphProfile(classId));
            int mirrorCooldown = MorphHelper.getSpecialPreviewCooldown(new MorphProfile(classId + 2000));
            if (actualCooldown != expectedCooldown || mirrorCooldown != expectedCooldown) {
                helper.fail("morph special cooldown preview mismatch for class " + classId
                        + ": expected " + expectedCooldown + ", got " + actualCooldown
                        + " / mirror " + mirrorCooldown);
                return;
            }
        }

        if (MorphHelper.getSpecialPreviewCooldown(new MorphProfile(47)) != 0
                || MorphHelper.getSpecialPreviewCooldown(new MorphProfile(2047)) != 0) {
            helper.fail("non-special morph classes should preview zero special cooldown");
            return;
        }

        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void repairGoddessCargoRescueRestoresLegacyRuntimeAndTooltip(GameTestHelper helper) {
        Player player = helper.makeMockPlayer();
        LegacyShipEntity ship = createOwnedShip(helper, player, 58, 2.5D, 2.0D, 2.5D);
        if (ship == null) {
            return;
        }

        ship.getShipInventory().setItem(LegacyShipEntity.EQUIPMENT_SLOT_COUNT, new ItemStack(ModItems.REPAIRGODDESS.get()));
        ship.addEffect(new MobEffectInstance(MobEffects.POISON, 200, 0, false, false, false));
        ship.setHealth(Math.min(5.0F, ship.getMaxHealth()));

        if (ship.hurt(ship.damageSources().generic(), ship.getMaxHealth() + 40.0F)) {
            helper.fail("repair goddess rescue should cancel lethal damage instead of entering the death path");
            return;
        }

        if (!ship.isAlive() || ship.isDeadOrDying() || Math.abs(ship.getHealth() - ship.getMaxHealth()) > 0.01F) {
            helper.fail("repair goddess rescue should restore the ship to full legacy health");
            return;
        }
        if (!ship.hasEffect(MobEffects.POISON)) {
            helper.fail("repair goddess rescue should not clear unrelated effects");
            return;
        }
        if (ship.hasEffect(MobEffects.REGENERATION)
                || ship.hasEffect(MobEffects.ABSORPTION)
                || ship.hasEffect(MobEffects.FIRE_RESISTANCE)) {
            helper.fail("repair goddess rescue should not inject modern potion effects");
            return;
        }
        if (!ship.getShipInventory().getItem(LegacyShipEntity.EQUIPMENT_SLOT_COUNT).isEmpty()) {
            helper.fail("repair goddess rescue should consume one stored repair goddess from cargo");
            return;
        }

        float rescuedHealth = ship.getHealth();
        ship.hurt(ship.damageSources().generic(), 3.0F);
        if (ship.getHealth() < rescuedHealth - 0.01F) {
            helper.fail("repair goddess rescue should apply the legacy invulnerability window after triggering");
            return;
        }

        ItemStack goddess = new ItemStack(ModItems.REPAIRGODDESS.get());
        List<Component> tooltip = new ArrayList<>();
        goddess.getItem().appendHoverText(goddess, helper.getLevel(), tooltip, TooltipFlag.Default.NORMAL);
        if (!goddess.getItem().isFoil(goddess)) {
            helper.fail("repair goddess item should retain the legacy foil effect");
            return;
        }
        if (tooltip.size() != 1
                || !tooltip.get(0).getString().equals(Component.translatable("gui.shincolle.repairgoddess").getString())) {
            helper.fail("repair goddess tooltip should contain only the legacy rescue description");
            return;
        }

        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void morphInventoryLegacySupportButtonsMutateSelectedProfile(GameTestHelper helper) {
        ServerPlayer serverPlayer = makeServerBackedPlayer(helper, "MorphCommandCoverage");
        Player player = serverPlayer;

        if (!configureTeitoku(helper, player, data -> {
            data.setPlayerUid(8610);
            data.unlockMorph(58);
            data.setSelectedMorphProfile(58);
            MorphProfile profile = data.getSelectedMorphProfile();
            if (profile != null) {
                profile.setLevel(3);
                profile.setExperience(40);
                profile.setAmmoLight(0);
                profile.setAmmoHeavy(0);
                profile.setGrudge(0);
                profile.setAuraEffect(false);
                profile.setShowHeldItem(false);
            }
        })) {
            return;
        }

        player.getInventory().add(new ItemStack(ModItems.AMMO.get()));
        player.getInventory().add(new ItemStack(ModItems.AMMO2.get()));
        player.getInventory().add(new ItemStack(ModBlocks.BLOCK_GRUDGE.get()));

        GameplayCommandHandler.handle(serverPlayer, GameplayCommandType.MORPH_ADD_LIGHT_AMMO, new CompoundTag());
        GameplayCommandHandler.handle(serverPlayer, GameplayCommandType.MORPH_ADD_HEAVY_AMMO, new CompoundTag());
        GameplayCommandHandler.handle(serverPlayer, GameplayCommandType.MORPH_ADD_GRUDGE, new CompoundTag());
        GameplayCommandHandler.handle(serverPlayer, GameplayCommandType.MORPH_TOGGLE_AURA_EFFECT, new CompoundTag());
        GameplayCommandHandler.handle(serverPlayer, GameplayCommandType.MORPH_TOGGLE_SHOW_HELD, new CompoundTag());

        MorphProfile profile = TeitokuHelper.get(player)
                .map(TeitokuData::getSelectedMorphProfile)
                .orElse(null);
        if (profile == null) {
            helper.fail("selected morph profile should remain available after morph inventory commands");
            return;
        }

        if (profile.getAmmoLight() != 30
                || profile.getAmmoHeavy() != 15
                || profile.getGrudge() != 2700
                || !profile.hasAuraEffect()
                || !profile.isShowHeldItem()) {
            helper.fail("legacy morph inventory support buttons should refill supplies and toggle profile flags");
            return;
        }

        if (player.getInventory().countItem(ModItems.AMMO.get()) != 0
                || player.getInventory().countItem(ModItems.AMMO2.get()) != 0
                || player.getInventory().countItem(ModBlocks.BLOCK_GRUDGE.get().asItem()) != 0) {
            helper.fail("legacy morph supply buttons should consume the matching inventory items");
            return;
        }

        MorphInventoryMenu menu = new MorphInventoryMenu(9, player.getInventory(), 58);
        if (!"40 / 80".equals(menu.getExperienceText())
                || !"30".equals(menu.getAmmoLightText())
                || !"15".equals(menu.getAmmoHeavyText())
                || !"2700".equals(menu.getGrudgeText())
                || !menu.isAuraEffectEnabled()
                || !menu.isShowHeldItemEnabled()
                || !"ON".equals(menu.getAuraStateLabel().getString())
                || !"ON".equals(menu.getShowHeldStateLabel().getString())) {
            helper.fail("morph inventory menu should expose the restored legacy experience and toggle readouts");
            return;
        }

        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void growthEntryRecipesResolveAndCraft(GameTestHelper helper) {
        assertCrafts(helper, "deskitembook", new ItemStack(ModItems.DESKITEMBOOK.get()),
                stack(ModItems.GRUDGE.get()), stack(ModItems.GRUDGE.get()), stack(ModItems.GRUDGE.get()),
                stack(ModItems.GRUDGE.get()), stack(Items.WRITABLE_BOOK), stack(ModItems.GRUDGE.get()),
                stack(ModItems.GRUDGE.get()), stack(ModItems.GRUDGE.get()), stack(ModItems.GRUDGE.get()));
        assertCrafts(helper, "deskitemradar", new ItemStack(ModItems.DESKITEMRADAR.get()),
                stack(ModItems.GRUDGE.get()), stack(ModItems.GRUDGE.get()), stack(ModItems.GRUDGE.get()),
                stack(ModItems.GRUDGE.get()), stack(Items.COMPASS), stack(ModItems.GRUDGE.get()),
                stack(ModItems.GRUDGE.get()), stack(ModItems.GRUDGE.get()), stack(ModItems.GRUDGE.get()));
        assertCrafts(helper, "blockdesk", new ItemStack(ModBlocks.BLOCK_DESK.get()),
                stack(ModItems.DESKITEMRADAR.get()), stack(ModItems.DESKITEMBOOK.get()), stack(Items.WHITE_WOOL),
                stack(Items.OBSIDIAN), stack(Items.OBSIDIAN), stack(Items.OBSIDIAN),
                stack(Items.OBSIDIAN), ItemStack.EMPTY, stack(Items.OBSIDIAN));
        assertCrafts(helper, "blocksmallshipyard", new ItemStack(ModBlocks.BLOCK_SMALL_SHIPYARD.get()),
                stack(ModItems.GRUDGE.get()), stack(Items.LAVA_BUCKET), stack(ModItems.GRUDGE.get()),
                stack(Items.LAVA_BUCKET), stack(Items.OBSIDIAN), stack(Items.LAVA_BUCKET),
                stack(Items.OBSIDIAN), stack(Items.OBSIDIAN), stack(Items.OBSIDIAN));
        assertCrafts(helper, "pointeritem", new ItemStack(ModItems.POINTERITEM.get()),
                ItemStack.EMPTY, ItemStack.EMPTY, stack(ModBlocks.BLOCK_GRUDGE.get()),
                ItemStack.EMPTY, stack(ModItems.ABYSSMETAL1.get()), ItemStack.EMPTY,
                stack(ModItems.ABYSSMETAL1.get()), ItemStack.EMPTY, ItemStack.EMPTY);
        assertCrafts(helper, "targetwrench", new ItemStack(ModItems.TARGETWRENCH.get()),
                stack(ModItems.ABYSSMETAL.get()), ItemStack.EMPTY, stack(ModItems.ABYSSMETAL.get()),
                stack(ModItems.ABYSSMETAL.get()), stack(ModItems.ABYSSMETAL.get()), stack(ModItems.ABYSSMETAL.get()),
                ItemStack.EMPTY, stack(ModItems.ABYSSMETAL.get()), ItemStack.EMPTY);
        assertCrafts(helper, "blockwaypoint", new ItemStack(ModBlocks.BLOCK_WAYPOINT.get(), 16),
                stack(ModItems.GRUDGE.get()), ItemStack.EMPTY, ItemStack.EMPTY,
                stack(Items.STICK), ItemStack.EMPTY, ItemStack.EMPTY,
                ItemStack.EMPTY, ItemStack.EMPTY, ItemStack.EMPTY);
        assertCrafts(helper, "ownerpaper", new ItemStack(ModItems.OWNERPAPER.get()),
                stack(ModItems.GRUDGE.get()), stack(Items.PAPER));
        assertCrafts(helper, "recipepaper", new ItemStack(ModItems.RECIPEPAPER.get()),
                stack(ModItems.GRUDGE.get()), stack(Items.PAPER), stack(Items.LAPIS_LAZULI));
        assertCrafts(helper, "bucketrepair", new ItemStack(ModItems.BUCKETREPAIR.get()),
                stack(Items.LAVA_BUCKET), stack(ModItems.GRUDGE.get()));

        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void combatRationCookedMeatTagOnlyAcceptsCookedInputs(GameTestHelper helper) {
        assertCrafts(helper, "combatration2", new ItemStack(ModItems.COMBATRATION2.get()),
                stack(Items.WHEAT), stack(Items.WHEAT), stack(Items.WHEAT),
                stack(Items.COOKED_BEEF), stack(Items.GOLDEN_CARROT), stack(ModItems.GRUDGE.get()),
                stack(Items.WHEAT), stack(Items.WHEAT), stack(Items.WHEAT));

        TransientCraftingContainer rawBeefInput = craftingGrid(
                stack(Items.WHEAT), stack(Items.WHEAT), stack(Items.WHEAT),
                stack(Items.BEEF), stack(Items.GOLDEN_CARROT), stack(ModItems.GRUDGE.get()),
                stack(Items.WHEAT), stack(Items.WHEAT), stack(Items.WHEAT));
        if (helper.getLevel().getRecipeManager().getRecipeFor(RecipeType.CRAFTING, rawBeefInput, helper.getLevel()).isPresent()) {
            helper.fail("combatration2 should reject raw meat inputs");
            return;
        }

        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void smallShipyardEggFlowUsesLegacyBossRingGate(GameTestHelper helper) {
        int[] materials = {SmallShipyardRecipes.MIN_AMOUNT, SmallShipyardRecipes.MIN_AMOUNT,
                SmallShipyardRecipes.MIN_AMOUNT, SmallShipyardRecipes.MIN_AMOUNT};
        if (!SmallShipyardRecipes.canRecipeBuild(materials)) {
            helper.fail("minimum growth-loop materials should satisfy small shipyard requirements");
            return;
        }

        ItemStack egg = SmallShipyardRecipes.createShipEgg(materials);
        if (!egg.is(ModItems.SHIPSPAWNEGG_ITEMS.get(0).get())) {
            helper.fail("small shipyard should output the small ship egg");
            return;
        }
        if (!LegacyShipConstructionHelper.hasConstructionRecipe(egg)) {
            helper.fail("small shipyard egg should carry construction material tags");
            return;
        }
        int[] stored = LegacyShipConstructionHelper.readMaterialAmounts(egg);
        for (int i = 0; i < materials.length; i++) {
            if (stored[i] != materials[i]) {
                helper.fail("small egg should preserve construction material amounts");
                return;
            }
        }
        ShipEntitySpec resolved = LegacyShipConstructionHelper.resolveConstructionEgg(egg, "smallegg", RandomSource.create(24L));
        if (resolved == null || !ShipEntitySpecs.primaryPool().contains(resolved)) {
            helper.fail("small shipyard eggs should resolve through the 1.12 ShipCalc.EquipSmall table");
            return;
        }

        TeitokuData data = new TeitokuData();
        if (HostileEncounterSpawner.canRollBossEncounter(data)) {
            helper.fail("boss encounters should stay locked until the admiral has a ring");
            return;
        }
        data.setHasRing(true);
        if (!HostileEncounterSpawner.canRollBossEncounter(data)) {
            helper.fail("boss encounters should unlock when the admiral has the ring");
            return;
        }

        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void shipCommandSupportBossAndCombatFlowUsesLegacyBossState(GameTestHelper helper) {
        Player player = helper.makeMockPlayer();
        player.setPos(2.0D, 2.0D, 2.0D);
        if (!configureTeitoku(helper, player, data -> {
            data.setPlayerUid(8601);
            data.setCurrentTeamId(0);
            data.setBossCooldown(0);
            data.setHasRing(true);
        })) {
            return;
        }

        LegacyShipEntity ship = createOwnedShip(helper, player, 62, 2.5D, 2.0D, 2.5D);
        if (ship == null) {
            return;
        }
        ship.setShipLevel(35);
        ship.setMorale(1200);

        if (!configureTeitoku(helper, player, data -> {
            data.assignCurrentTeamSlot(0, ship.getShipUid());
            data.setCurrentTeamSelection(0, true);
        })) {
            return;
        }

        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(ModItems.COMBATRATION.get()));
        int moraleBeforeRation = ship.getMorale();
        if (ship.mobInteract(player, InteractionHand.MAIN_HAND) != InteractionResult.CONSUME
                || ship.getMorale() <= moraleBeforeRation) {
            helper.fail("owned ship should accept support items before sortie commands");
            return;
        }

        int aiFlags = GameplayCommandHandler.AI_FLAG_AUTO_TARGET
                | GameplayCommandHandler.AI_FLAG_AUTO_SUPPLY
                | GameplayCommandHandler.AI_FLAG_ROUTE_STAY;
        if (!ShipCommandService.handleForTesting(player, ServerboundShipCommandPacket.setAiFlags(ship.getId(), ship.getShipUid(), aiFlags))
                || !ShipCommandService.handleForTesting(player, ServerboundShipCommandPacket.setFollowRange(ship.getId(), ship.getShipUid(), 24))
                || ship.getAiFlagsBitmask() != aiFlags
                || ship.getAiFollowRange() != 24) {
            helper.fail("ship inventory command controls should update AI flags and follow range");
            return;
        }

        BlockPos moveTarget = new BlockPos(6, 2, 6);
        if (!ShipCommandService.handleForTesting(player, ServerboundShipCommandPacket.moveTo(0, ship.getId(), ship.getShipUid(), moveTarget))
                || !moveTarget.equals(ship.getCommandedPos())
                || ship.isOrderedToSit()) {
            helper.fail("sortie flow should accept typed move commands");
            return;
        }

        TeitokuData data = new TeitokuData();
        data.setHasRing(true);
        data.setBossCooldown(0);
        if (!HostileEncounterSpawner.canRollBossEncounter(data)) {
            helper.fail("boss gate should open for admirals with a ring");
            return;
        }

        LegacyShipEntity boss = HostileEncounterSpawner.spawnEncounterAt(helper.getLevel(),
                helper.absolutePos(new BlockPos(5, 2, 5)),
                null,
                new com.lulan.shincolle.world.HostileSpawnProfile(23, 1, true, true));
        if (boss == null) {
            helper.fail("legacy hostile helper should be able to spawn a hostile boss encounter");
            return;
        }
        boss.setPos(5.0D, 2.0D, 5.0D);

        if (!ShipCommandService.handleForTesting(player, ServerboundShipCommandPacket.attack(0, ship.getId(), ship.getShipUid(), boss.getId()))
                || ship.getTarget() != boss
                || ship.getCommandedPos() != null) {
            helper.fail("attack command should target hostile boss encounters and clear move orders");
            return;
        }

        if (!ship.performPlayerCompatAttack(boss, LegacyShipAttackKind.HEAVY)
                || ship.getCompatAttackCooldown(LegacyShipAttackKind.HEAVY) <= 0) {
            helper.fail("boss combat should dispatch heavy combat hooks and set cooldowns");
            return;
        }

        if (!ShipCommandService.handleForTesting(player, ServerboundShipCommandPacket.stop(0, ship.getId(), ship.getShipUid()))
                || ship.getTarget() != null
                || ship.getCommandedPos() != null
                || ship.getGuardEntityUuid() != null) {
            helper.fail("stop command should clear boss combat and navigation state");
            return;
        }

        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void friendlyCounterpartsResolveForConstructionAndLoot(GameTestHelper helper) {
        ShipEntitySpec smallEgg = ShipEntitySpecs.resolveEggItem("smallegg", RandomSource.create(7L));
        ShipEntitySpec largeEgg = ShipEntitySpecs.resolveEggItem("largeegg", RandomSource.create(17L));
        ShipEntitySpec abyssDestroyerCounterpart = ShipEntitySpecs.friendlyCounterpart(2);
        ShipEntitySpec airfieldCounterpart = ShipEntitySpecs.friendlyCounterpart(23);

        if (!ShipEntitySpecs.primaryPool().contains(smallEgg)
                || !ShipEntitySpecs.advancedPool().contains(largeEgg)
                || abyssDestroyerCounterpart.hostile()
                || airfieldCounterpart.hostile()) {
            helper.fail("random eggs must use 1.12 small/large tables, and hostile loot counterparts must resolve to friendly specs");
            return;
        }

        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void hostileEncounterTableHonorsBossFlag(GameTestHelper helper) {
        RandomSource noBossRandom = RandomSource.create(911L);
        for (int i = 0; i < 64; i++) {
            var profile = com.lulan.shincolle.world.HostileEncounterTable.pick(
                    noBossRandom, net.minecraft.world.Difficulty.HARD, true, false);
            if (profile.boss()) {
                helper.fail("encounter table should not produce boss profiles when boss rolls are disabled");
                return;
            }
        }

        RandomSource bossRandom = RandomSource.create(37L);
        boolean foundBoss = false;
        for (int i = 0; i < 128; i++) {
            if (com.lulan.shincolle.world.HostileEncounterTable.pick(
                    bossRandom, net.minecraft.world.Difficulty.HARD, true, true).boss()) {
                foundBoss = true;
                break;
            }
        }
        if (!foundBoss) {
            helper.fail("encounter table should still be able to produce boss profiles after the growth gate unlocks");
            return;
        }

        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void hostileEncounterSpawnInitializesRuntime(GameTestHelper helper) {
        BlockPos anchor = helper.absolutePos(new BlockPos(5, 2, 5));
        var profile = new com.lulan.shincolle.world.HostileSpawnProfile(2049, 1, true, true);
        LegacyShipEntity spawned = HostileEncounterSpawner.spawnEncounterAt(helper.getLevel(), anchor, null, profile);
        if (spawned == null) {
            helper.fail("hostile encounter helper should spawn into an open game-test area");
            return;
        }

        if (spawned.getVariantEggMeta() != 2049
                || !spawned.isHostileVariant()
                || !spawned.isBossEncounter()
                || spawned.getSoundSource() != SoundSource.HOSTILE
                || !spawned.getAttackProfile().hasAirAttack()
                || spawned.getShipLevel() != LegacyShipBehaviorCatalog.hostileSpawnLevel(spawned.getSpec(), true, true)
                || spawned.getMorale() != LegacyShipBehaviorCatalog.hostileSpawnMorale(true, true)
                || spawned.getHealth() < spawned.getMaxHealth()) {
            helper.fail("hostile encounter spawn should initialize hostile boss runtime, sound source, stats, and attack profile");
            return;
        }

        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void hostileBossUsesLegacyPlayerTargetRules(GameTestHelper helper) {
        Player creativePlayer = helper.makeMockPlayer();
        ServerPlayer player = makeServerBackedPlayer(helper, "HostileBossTarget");
        LegacyShipEntity boss = ModEntityTypes.LEGACY_SHIP.get().create(helper.getLevel());
        if (boss == null) {
            helper.fail("legacy ship entities should be creatable for hostile targeting tests");
            return;
        }

        boss.setVariantEggMeta(2049);
        boss.initializeHostileRuntime(false, true, true);
        boss.setPos(5.0D, 2.0D, 5.0D);
        creativePlayer.setPos(7.0D, 2.0D, 5.0D);
        player.setPos(7.0D, 2.0D, 5.0D);

        if (boss.canEngage(creativePlayer)) {
            helper.fail("hostile bosses should ignore creative or spectator players");
            return;
        }

        player.getAbilities().invulnerable = true;
        player.getAbilities().instabuild = true;
        if (boss.canEngage(player)) {
            helper.fail("hostile bosses should ignore invulnerable players");
            return;
        }

        player.getAbilities().invulnerable = false;
        player.getAbilities().instabuild = false;
        if (!boss.canEngage(player)) {
            helper.fail("hostile bosses should still engage nearby vulnerable players");
            return;
        }

        double legacyRange = boss.getCompatAttackRange();
        player.setPos(5.0D + legacyRange + 1.0D, 2.0D, 5.0D);
        if (boss.canEngage(player)) {
            helper.fail("hostile bosses should use legacy attack range as the player target leash");
            return;
        }

        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void combatReactActionbarScopeRequiresParticipant(GameTestHelper helper) {
        ClientboundCombatReactPacket packet = new ClientboundCombatReactPacket(
                CombatReactType.LAUNCH, 10, 20, LegacyShipAttackKind.AIR_HEAVY, LegacyShipProjectileVisual.AIRPLANE);
        if (!packet.involvesEntity(10) || !packet.involvesEntity(20) || packet.involvesEntity(30)) {
            helper.fail("combat reaction actionbar eligibility should be limited to attacker and target entity ids");
            return;
        }

        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void progressionAdvancementsLoadAndFriendlyShipTriggerAwards(GameTestHelper helper) {
        Advancement deployAdvancement = helper.getLevel().getServer().getAdvancements()
                .getAdvancement(ResourceLocation.fromNamespaceAndPath(ShinColle.MOD_ID, "progression/deploy_first_ship"));
        Advancement bossAdvancement = helper.getLevel().getServer().getAdvancements()
                .getAdvancement(ResourceLocation.fromNamespaceAndPath(ShinColle.MOD_ID, "progression/ocean_boss_encounter"));
        if (deployAdvancement == null || bossAdvancement == null) {
            helper.fail("expected progression advancements to load into the server advancement manager");
            return;
        }

        var trigger = CriteriaTriggers.getCriterion(ModCriteriaTriggers.FRIENDLY_SHIP_DEPLOYED.getId());
        if (trigger == null || !trigger.getId().equals(ModCriteriaTriggers.FRIENDLY_SHIP_DEPLOYED.getId())) {
            helper.fail("friendly ship deployment trigger should be registered in the vanilla criterion registry");
            return;
        }

        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void mountStyleResolutionMatchesLegacyPrincessFamilies(GameTestHelper helper) {
        if (LegacyMountEntity.styleForSpec(ShipEntitySpecs.getByEggMeta(23)) != LegacyMountEntity.STYLE_AIRFIELD
                || LegacyMountEntity.styleForSpec(ShipEntitySpecs.getByEggMeta(35)) != LegacyMountEntity.STYLE_CARRIER_WD
                || LegacyMountEntity.styleForSpec(ShipEntitySpecs.getByEggMeta(32)) != LegacyMountEntity.STYLE_MIDWAY
                || LegacyMountEntity.styleForSpec(ShipEntitySpecs.getByEggMeta(40)) != LegacyMountEntity.STYLE_SUBMARINE
                || LegacyMountEntity.styleForSpec(ShipEntitySpecs.getByEggMeta(62)) != LegacyMountEntity.STYLE_BATTLESHIP) {
            helper.fail("mount host rendering should map legacy ship families to the original mount model groups");
            return;
        }

        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void lateGameRecipesResolveAndCraft(GameTestHelper helper) {
        assertCrafts(helper, "kaitaihammer", new ItemStack(ModItems.KAITAIHAMMER.get()),
                stack(ModItems.ABYSSMETAL.get()), stack(ModItems.ABYSSMETAL.get()), stack(ModItems.ABYSSMETAL.get()),
                ItemStack.EMPTY, stack(ModItems.ABYSSMETAL.get()), ItemStack.EMPTY,
                ItemStack.EMPTY, stack(ModItems.ABYSSMETAL.get()), ItemStack.EMPTY);
        assertCrafts(helper, "instantconmat", new ItemStack(ModItems.INSTANTCONMAT.get()),
                ItemStack.EMPTY, stack(ModItems.ABYSSMETAL.get()), ItemStack.EMPTY,
                stack(ModItems.ABYSSMETAL.get()), stack(ModBlocks.BLOCK_GRUDGE.get()), stack(ModItems.ABYSSMETAL.get()),
                ItemStack.EMPTY, stack(ModItems.ABYSSMETAL.get()), ItemStack.EMPTY);
        assertCrafts(helper, "instantconmat8", new ItemStack(ModItems.INSTANTCONMAT.get(), 8),
                stack(ModItems.SHIPSPAWNEGG_ITEMS.get(0).get()), stack(ModItems.KAITAIHAMMER.get()));
        assertCrafts(helper, "instantconmat64", new ItemStack(ModItems.INSTANTCONMAT.get(), 64),
                stack(ModItems.SHIPSPAWNEGG_ITEMS.get(1).get()), stack(ModItems.KAITAIHAMMER.get()));
        String removedLargeShipyardRecipePath = "data/shincolle/recipes/blocklarge" + "shipyard.json";
        if (loadJsonResource(removedLargeShipyardRecipePath) != null) {
            helper.fail("strict 1.12 parity must not expose a standalone blocklargeshipyard recipe");
            return;
        }

        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void registeredShipVisualAssetsExist(GameTestHelper helper) {
        if (LegacyShipRenderCatalog.entries().size() != ShipEntitySpecs.values().size()) {
            helper.fail("legacy ship render catalog should cover every registered ship spec");
            return;
        }

        for (ShipEntitySpec spec : ShipEntitySpecs.values()) {
            LegacyShipRenderCatalog.RenderEntry renderEntry = LegacyShipRenderCatalog.forSpec(spec);
            if (!resourceExists(renderEntry.modelSourceLocation()) || !resourceExists(renderEntry.textureLocation())) {
                helper.fail("registered ship spec is missing a legacy model source or texture: egg="
                        + spec.eggMeta() + " stem=" + spec.textureStem());
                return;
            }
        }

        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void registeredShipLegacyModelSourcesParse(GameTestHelper helper) {
        LegacyModelSourceParser parser = new LegacyModelSourceParser();
        for (ShipEntitySpec spec : ShipEntitySpecs.values()) {
            LegacyShipRenderCatalog.RenderEntry renderEntry = LegacyShipRenderCatalog.forSpec(spec);
            String source = readTextResource(renderEntry.modelSourceLocation());
            if (source == null) {
                helper.fail("registered ship model source should be packaged: egg="
                        + spec.eggMeta() + " stem=" + renderEntry.modelSourceStem());
                return;
            }

            LegacyModelDefinition definition;
            try {
                definition = parser.parse(renderEntry.modelSourceStem(), source);
            } catch (Exception exception) {
                helper.fail("registered ship model source should parse without fallback: egg="
                        + spec.eggMeta() + " stem=" + renderEntry.modelSourceStem()
                        + " error=" + exception.getClass().getSimpleName());
                return;
            }

            boolean hasRenderableCube = definition.parts().values().stream()
                    .anyMatch(part -> !part.cubes().isEmpty());
            if (definition.rootParts().isEmpty() || definition.parts().isEmpty() || !hasRenderableCube) {
                helper.fail("registered ship model source parsed to an empty model: egg="
                        + spec.eggMeta() + " stem=" + renderEntry.modelSourceStem());
                return;
            }
        }

        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void summonAndMountAssetsExist(GameTestHelper helper) {
        StaticLegacyModelAsset[] requiredModels = {
                new StaticLegacyModelAsset("ModelAirplaneZero", ResourceLocation.fromNamespaceAndPath(ShinColle.MOD_ID, "legacy_model_sources/modelairplanezero.java")),
                new StaticLegacyModelAsset("ModelAirplaneT", ResourceLocation.fromNamespaceAndPath(ShinColle.MOD_ID, "legacy_model_sources/modelairplanet.java")),
                new StaticLegacyModelAsset("ModelTakoyaki", ResourceLocation.fromNamespaceAndPath(ShinColle.MOD_ID, "legacy_model_sources/modeltakoyaki.java")),
                new StaticLegacyModelAsset("ModelMountAfH", ResourceLocation.fromNamespaceAndPath(ShinColle.MOD_ID, "legacy_model_sources/modelmountafh.java")),
                new StaticLegacyModelAsset("ModelMountBaH", ResourceLocation.fromNamespaceAndPath(ShinColle.MOD_ID, "legacy_model_sources/modelmountbah.java")),
                new StaticLegacyModelAsset("ModelMountCaH", ResourceLocation.fromNamespaceAndPath(ShinColle.MOD_ID, "legacy_model_sources/modelmountcah.java")),
                new StaticLegacyModelAsset("ModelMountCaWD", ResourceLocation.fromNamespaceAndPath(ShinColle.MOD_ID, "legacy_model_sources/modelmountcawd.java")),
                new StaticLegacyModelAsset("ModelMountHbH", ResourceLocation.fromNamespaceAndPath(ShinColle.MOD_ID, "legacy_model_sources/modelmounthbh.java")),
                new StaticLegacyModelAsset("ModelMountIsH", ResourceLocation.fromNamespaceAndPath(ShinColle.MOD_ID, "legacy_model_sources/modelmountish.java")),
                new StaticLegacyModelAsset("ModelMountMiH", ResourceLocation.fromNamespaceAndPath(ShinColle.MOD_ID, "legacy_model_sources/modelmountmih.java")),
                new StaticLegacyModelAsset("ModelMountSuH", ResourceLocation.fromNamespaceAndPath(ShinColle.MOD_ID, "legacy_model_sources/modelmountsuh.java"))
        };
        ResourceLocation[] requiredTextures = {
                ResourceLocation.fromNamespaceAndPath(ShinColle.MOD_ID, "textures/entity/entityairplanezero.png"),
                ResourceLocation.fromNamespaceAndPath(ShinColle.MOD_ID, "textures/entity/entityairplanet.png"),
                ResourceLocation.fromNamespaceAndPath(ShinColle.MOD_ID, "textures/entity/entityaircrafttakoyaki.png"),
                ResourceLocation.fromNamespaceAndPath(ShinColle.MOD_ID, "textures/entity/entitymountafh.png"),
                ResourceLocation.fromNamespaceAndPath(ShinColle.MOD_ID, "textures/entity/entitymountbah.png"),
                ResourceLocation.fromNamespaceAndPath(ShinColle.MOD_ID, "textures/entity/entitymountcah.png"),
                ResourceLocation.fromNamespaceAndPath(ShinColle.MOD_ID, "textures/entity/entitymountcawd.png"),
                ResourceLocation.fromNamespaceAndPath(ShinColle.MOD_ID, "textures/entity/entitymounthbh.png"),
                ResourceLocation.fromNamespaceAndPath(ShinColle.MOD_ID, "textures/entity/entitymountish.png"),
                ResourceLocation.fromNamespaceAndPath(ShinColle.MOD_ID, "textures/entity/entitymountmih.png"),
                ResourceLocation.fromNamespaceAndPath(ShinColle.MOD_ID, "textures/entity/entitymountsuh.png")
        };

        LegacyModelSourceParser parser = new LegacyModelSourceParser();
        for (StaticLegacyModelAsset model : requiredModels) {
            String source = readTextResource(model.sourceLocation());
            if (source == null) {
                helper.fail("expected summon or mount model source to exist: " + model.sourceLocation());
                return;
            }

            LegacyModelDefinition definition;
            try {
                definition = parser.parse(model.modelName(), source);
            } catch (Exception exception) {
                helper.fail("summon or mount model source should parse without fallback: "
                        + model.sourceLocation() + " error=" + exception.getClass().getSimpleName());
                return;
            }

            boolean hasRenderableCube = definition.parts().values().stream()
                    .anyMatch(part -> !part.cubes().isEmpty());
            if (definition.rootParts().isEmpty() || definition.parts().isEmpty() || !hasRenderableCube) {
                helper.fail("summon or mount model source parsed to an empty model: " + model.sourceLocation());
                return;
            }
        }

        for (ResourceLocation texture : requiredTextures) {
            if (!resourceExists(texture)) {
                helper.fail("expected summon or mount texture to exist: " + texture);
                return;
            }
        }

        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void shipSpecResolutionRejectsUnmappedFallbacks(GameTestHelper helper) {
        try {
            ShipEntitySpecs.getByEggMeta(-999);
            helper.fail("unknown legacy ship egg meta should not fall back to a default spec");
            return;
        } catch (IllegalArgumentException expected) {
        }

        try {
            ShipEntitySpecs.resolveEggItem("shipegg9999", RandomSource.create());
            helper.fail("unknown shipegg item path should not fall back to a default spec");
            return;
        } catch (IllegalArgumentException expected) {
        }

        try {
            ShipEntitySpecs.resolveEggItem("modern_generated_egg", RandomSource.create());
            helper.fail("unknown ship egg item path should not fall back to a default spec");
            return;
        } catch (IllegalArgumentException expected) {
        }

        for (ShipEntitySpec spec : ShipEntitySpecs.values()) {
            if (!spec.hostile()) {
                continue;
            }

            ShipEntitySpec counterpart;
            try {
                counterpart = ShipEntitySpecs.friendlyCounterpart(spec);
            } catch (IllegalArgumentException exception) {
                helper.fail("hostile legacy ship egg meta lacks explicit 1.12 counterpart: " + spec.eggMeta());
                return;
            }

            if (counterpart.hostile()) {
                helper.fail("explicit 1.12 counterpart must resolve to a friendly ship: hostile="
                        + spec.eggMeta() + ", counterpart=" + counterpart.eggMeta());
                return;
            }
        }

        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void shipyardRendererAssetsExist(GameTestHelper helper) {
        StaticLegacyModelAsset[] requiredModels = {
                new StaticLegacyModelAsset("ModelLargeShipyard",
                        ResourceLocation.fromNamespaceAndPath(ShinColle.MOD_ID, "legacy_model_sources/modellargeshipyard.java")),
                new StaticLegacyModelAsset("ModelVortex",
                        ResourceLocation.fromNamespaceAndPath(ShinColle.MOD_ID, "legacy_model_sources/modelvortex.java"))
        };
        ResourceLocation[] requiredTextures = {
                ResourceLocation.fromNamespaceAndPath(ShinColle.MOD_ID, "textures/blocks/blocklargeshipyard.png"),
                ResourceLocation.fromNamespaceAndPath(ShinColle.MOD_ID, "textures/blocks/modelvortex.png"),
                ResourceLocation.fromNamespaceAndPath(ShinColle.MOD_ID, "textures/blocks/modelvortexon.png")
        };

        LegacyModelSourceParser parser = new LegacyModelSourceParser();
        for (StaticLegacyModelAsset model : requiredModels) {
            String source = readTextResource(model.sourceLocation());
            if (source == null) {
                helper.fail("expected large shipyard renderer model source to exist: " + model.sourceLocation());
                return;
            }

            LegacyModelDefinition definition;
            try {
                definition = parser.parse(model.modelName(), source);
            } catch (Exception exception) {
                helper.fail("large shipyard renderer model source should parse without fallback: "
                        + model.sourceLocation() + " error=" + exception.getClass().getSimpleName());
                return;
            }

            boolean hasRenderableCube = definition.parts().values().stream()
                    .anyMatch(part -> !part.cubes().isEmpty());
            if (definition.rootParts().isEmpty() || definition.parts().isEmpty() || !hasRenderableCube) {
                helper.fail("large shipyard renderer model source parsed to an empty model: " + model.sourceLocation());
                return;
            }
        }

        for (ResourceLocation texture : requiredTextures) {
            if (!resourceExists(texture)) {
                helper.fail("expected large shipyard renderer texture to exist: " + texture);
                return;
            }
        }

        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void shipPickupGoalStoresNearbyDrops(GameTestHelper helper) {
        LegacyShipEntity ship = ModEntityTypes.LEGACY_SHIP.get().create(helper.getLevel());
        if (ship == null) {
            helper.fail("legacy ship should be creatable for pickup goal tests");
            return;
        }

        BlockPos shipPos = helper.absolutePos(new BlockPos(2, 2, 2));
        ship.setVariantEggMeta(53);
        ship.setGrudge(100);
        ship.setOwner(UUID.randomUUID(), "PickupTester");
        ship.setPos(shipPos.getX() + 0.5D, shipPos.getY(), shipPos.getZ() + 0.5D);
        helper.getLevel().addFreshEntity(ship);

        ItemEntity grudgeDrop = new ItemEntity(helper.getLevel(),
                ship.getX() + 1.1D,
                ship.getY(),
                ship.getZ() + 0.1D,
                new ItemStack(ModItems.GRUDGE.get(), 3));
        grudgeDrop.setNoPickUpDelay();
        helper.getLevel().addFreshEntity(grudgeDrop);

        LegacyShipPickItemGoal goal = new LegacyShipPickItemGoal(ship, 1.0D);
        if (!goal.canUse()) {
            grudgeDrop.discard();
            ship.discard();
            helper.fail("pickup goal should activate when a nearby drop fits in cargo");
            return;
        }

        goal.start();
        for (int i = 0; i < 6; i++) {
            goal.tick();
        }

        boolean stored = false;
        for (int slot = LegacyShipEntity.EQUIPMENT_SLOT_COUNT; slot < ship.getShipInventory().getContainerSize(); slot++) {
            if (ship.getShipInventory().getItem(slot).is(ModItems.GRUDGE.get())) {
                stored = true;
                break;
            }
        }
        grudgeDrop.discard();
        ship.discard();

        if (!stored) {
            helper.fail("pickup goal should move nearby drops into ship cargo");
            return;
        }

        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void shipPickupGoalYieldsToOrdersCombatAndSupplyFlag(GameTestHelper helper) {
        if (!pickupGoalCanUseWith(helper, 0, ship -> {
        })) {
            helper.fail("pickup goal should still activate for an idle friendly ship with auto-supply enabled");
            return;
        }
        if (pickupGoalCanUseWith(helper, 1, ship -> ship.commandMoveTo(new BlockPos(4, 2, 4),
                helper.getLevel().dimension().location().toString()))) {
            helper.fail("pickup goal must not interrupt an active move or route command");
            return;
        }
        if (pickupGoalCanUseWith(helper, 2, ship -> ship.commandGuardEntity(UUID.randomUUID()))) {
            helper.fail("pickup goal must not interrupt an active guard command");
            return;
        }
        if (pickupGoalCanUseWith(helper, 3, ship -> ship.setOrderedToSit(true))) {
            helper.fail("pickup goal must not run while the ship is standing by");
            return;
        }
        if (pickupGoalCanUseWith(helper, 4, ship -> ship.setAiPickItem(false))) {
            helper.fail("pickup goal must respect the pick-item AI flag");
            return;
        }
        if (pickupGoalCanUseWith(helper, 5, ship -> {
            Zombie target = EntityType.ZOMBIE.create(helper.getLevel());
            if (target != null) {
                target.setPos(ship.getX() + 2.0D, ship.getY(), ship.getZ());
                helper.getLevel().addFreshEntity(target);
                ship.setTarget(target);
            }
        })) {
            helper.fail("pickup goal must not interrupt active combat targeting");
            return;
        }

        helper.succeed();
    }

    private static boolean referencedModelsExist(GameTestHelper helper, JsonElement element, String ownerPath) {
        if (element == null || element.isJsonNull()) {
            return true;
        }

        if (element.isJsonObject()) {
            JsonObject object = element.getAsJsonObject();
            if (object.has("model")) {
                String modelName = object.get("model").getAsString();
                ResourceLocation modelId = modelName.contains(":")
                        ? ResourceLocation.tryParse(modelName)
                        : ResourceLocation.fromNamespaceAndPath(ShinColle.MOD_ID, modelName);
                if (modelId == null) {
                    helper.fail("invalid model reference in " + ownerPath + ": " + modelName);
                    return false;
                }

                if (ShinColle.MOD_ID.equals(modelId.getNamespace())) {
                    ResourceLocation modelResource = ResourceLocation.fromNamespaceAndPath(
                            modelId.getNamespace(), "models/" + modelId.getPath() + ".json");
                    if (!resourceExists(modelResource)) {
                        helper.fail("missing model referenced by " + ownerPath + ": " + modelResource);
                        return false;
                    }
                }
            }

            for (String key : object.keySet()) {
                if (!referencedModelsExist(helper, object.get(key), ownerPath)) {
                    return false;
                }
            }
            return true;
        }

        if (element.isJsonArray()) {
            for (JsonElement child : element.getAsJsonArray()) {
                if (!referencedModelsExist(helper, child, ownerPath)) {
                    return false;
                }
            }
        }

        return true;
    }

    private static boolean soundEventIsBackedBySoundsJson(GameTestHelper helper, JsonObject sounds, SoundEvent sound, String label) {
        if (sound == null) {
            helper.fail("expected sound event to resolve: " + label);
            return false;
        }

        ResourceLocation id = sound.getLocation();
        if (!ShinColle.MOD_ID.equals(id.getNamespace()) || !sounds.has(id.getPath())) {
            helper.fail("sound event should be registered and backed by sounds.json: " + label + " -> " + id);
            return false;
        }

        return true;
    }

    private static void assertMarriagePassive(GameTestHelper helper,
                                              int eggMeta,
                                              LegacyShipBehaviorCatalog.MarriagePassive expectedPassive) {
        ShipEntitySpec spec = ShipEntitySpecs.getByEggMeta(eggMeta);
        LegacyShipBehaviorCatalog.Behavior behavior = LegacyShipBehaviorCatalog.behaviorFor(spec);
        if (behavior.marriagePassive() != expectedPassive) {
            helper.fail("unexpected marriage passive for eggMeta " + eggMeta
                    + " / classId " + spec.legacyClassId()
                    + ": expected " + expectedPassive + " but got " + behavior.marriagePassive());
        }
    }

    private static void assertCombatHook(GameTestHelper helper,
                                         int eggMeta,
                                         LegacyShipBehaviorCatalog.CombatHook expectedHook) {
        ShipEntitySpec spec = ShipEntitySpecs.getByEggMeta(eggMeta);
        LegacyShipBehaviorCatalog.Behavior behavior = LegacyShipBehaviorCatalog.behaviorFor(spec);
        if (behavior.combatHook() != expectedHook) {
            helper.fail("unexpected combat hook for eggMeta " + eggMeta
                    + " / classId " + spec.legacyClassId()
                    + ": expected " + expectedHook + " but got " + behavior.combatHook());
        }
    }

    private static boolean pickupGoalCanUseWith(GameTestHelper helper, int index, Consumer<LegacyShipEntity> setup) {
        LegacyShipEntity ship = ModEntityTypes.LEGACY_SHIP.get().create(helper.getLevel());
        if (ship == null) {
            helper.fail("legacy ship should be creatable for pickup boundary tests");
            return false;
        }

        BlockPos shipPos = helper.absolutePos(new BlockPos(1 + index, 2, 1));
        ship.setVariantEggMeta(53);
        ship.setOwner(UUID.randomUUID(), "PickupBoundary");
        ship.setGrudge(100);
        ship.setPos(shipPos.getX() + 0.5D, shipPos.getY(), shipPos.getZ() + 0.5D);
        helper.getLevel().addFreshEntity(ship);

        setup.accept(ship);

        ItemEntity grudgeDrop = new ItemEntity(helper.getLevel(),
                ship.getX() + 1.0D,
                ship.getY(),
                ship.getZ() + 0.1D,
                new ItemStack(ModItems.GRUDGE.get(), 1));
        helper.getLevel().addFreshEntity(grudgeDrop);
        boolean canUse = new LegacyShipPickItemGoal(ship, 1.0D).canUse();
        for (Zombie zombie : helper.getLevel().getEntitiesOfClass(Zombie.class, ship.getBoundingBox().inflate(6.0D))) {
            zombie.discard();
        }
        grudgeDrop.discard();
        ship.discard();
        return canUse;
    }

    private static int countItems(Container container, net.minecraft.world.level.ItemLike item) {
        int count = 0;
        for (int slot = 0; slot < container.getContainerSize(); slot++) {
            ItemStack stack = container.getItem(slot);
            if (stack.is(item.asItem())) {
                count += stack.getCount();
            }
        }
        return count;
    }

    private static void clearContainer(Container container) {
        for (int slot = 0; slot < container.getContainerSize(); slot++) {
            container.setItem(slot, ItemStack.EMPTY);
        }
        container.setChanged();
    }

    private static boolean configureTeitoku(GameTestHelper helper,
                                            Player player,
                                            java.util.function.Consumer<TeitokuData> consumer) {
        boolean[] configured = { false };
        TeitokuHelper.get(player).ifPresent(data -> {
            consumer.accept(data);
            configured[0] = true;
        });
        if (!configured[0]) {
            helper.fail("mock player should expose Teitoku capability for command service tests");
        }
        return configured[0];
    }

    private static ServerPlayer makeServerBackedPlayer(GameTestHelper helper, String name) {
        return FakePlayerFactory.get(helper.getLevel(), new GameProfile(UUID.randomUUID(), name));
    }

    private static LegacyShipEntity createOwnedShip(GameTestHelper helper,
                                                    Player player,
                                                    int eggMeta,
                                                    double x,
                                                    double y,
                                                    double z) {
        LegacyShipEntity ship = ModEntityTypes.LEGACY_SHIP.get().create(helper.getLevel());
        if (ship == null) {
            helper.fail("legacy ship should be creatable for command service tests");
            return null;
        }

        ship.setVariantEggMeta(eggMeta);
        supplyShipForTest(ship);
        ship.setPos(x, y, z);
        helper.getLevel().addFreshEntity(ship);
        ship.setOwner(player);
        return ship;
    }

    private static void supplyShipForTest(LegacyShipEntity ship) {
        ship.setGrudge(1000);
        ship.setLightAmmo(1000);
        ship.setHeavyAmmo(1000);
    }

    private static boolean jsonContainsString(JsonElement element, String expected) {
        if (element == null || element.isJsonNull()) {
            return false;
        }
        if (element.isJsonPrimitive() && element.getAsJsonPrimitive().isString()) {
            return expected.equals(element.getAsString());
        }
        if (element.isJsonObject()) {
            JsonObject object = element.getAsJsonObject();
            for (String key : object.keySet()) {
                if (jsonContainsString(object.get(key), expected)) {
                    return true;
                }
            }
            return false;
        }
        if (element.isJsonArray()) {
            for (JsonElement child : element.getAsJsonArray()) {
                if (jsonContainsString(child, expected)) {
                    return true;
                }
            }
        }
        return false;
    }

    private static JsonObject placementByType(JsonObject placedFeature, String type) {
        for (JsonElement placementElement : placedFeature.getAsJsonArray("placement")) {
            JsonObject placement = placementElement.getAsJsonObject();
            if (type.equals(placement.get("type").getAsString())) {
                return placement;
            }
        }
        return null;
    }

    private static boolean tagResourceContains(String resourcePath, String expectedValue) {
        JsonObject tag = loadJsonResource(resourcePath);
        if (tag == null || !tag.has("values")) {
            return false;
        }
        for (JsonElement value : tag.getAsJsonArray("values")) {
            if (expectedValue.equals(value.getAsString())) {
                return true;
            }
        }
        return false;
    }

    private static JsonObject loadJsonResource(String resourcePath) {
        try (InputStream stream = GameplayParityGameTests.class.getClassLoader().getResourceAsStream(resourcePath)) {
            if (stream == null) {
                return null;
            }
            try (InputStreamReader reader = new InputStreamReader(stream, StandardCharsets.UTF_8)) {
                return JsonParser.parseReader(reader).getAsJsonObject();
            }
        } catch (Exception exception) {
            return null;
        }
    }

    private static boolean resourceExists(ResourceLocation resourceLocation) {
        try (InputStream stream = GameplayParityGameTests.class.getClassLoader()
                .getResourceAsStream("assets/" + resourceLocation.getNamespace() + "/" + resourceLocation.getPath())) {
            return stream != null;
        } catch (Exception exception) {
            return false;
        }
    }

    private static boolean packResourceExists(String packPath) {
        try (InputStream stream = GameplayParityGameTests.class.getClassLoader().getResourceAsStream(packPath)) {
            return stream != null;
        } catch (Exception exception) {
            return false;
        }
    }

    private static String readTextResource(ResourceLocation resourceLocation) {
        try (InputStream stream = GameplayParityGameTests.class.getClassLoader()
                .getResourceAsStream("assets/" + resourceLocation.getNamespace() + "/" + resourceLocation.getPath())) {
            if (stream == null) {
                return null;
            }
            return new String(stream.readAllBytes(), StandardCharsets.UTF_8);
        } catch (Exception exception) {
            return null;
        }
    }

    private record StaticLegacyModelAsset(String modelName, ResourceLocation sourceLocation) {
    }

    private static void assertCrafts(GameTestHelper helper, String recipePath, ItemStack expected, ItemStack... inputs) {
        TransientCraftingContainer grid = craftingGrid(inputs);
        ResourceLocation recipeId = ResourceLocation.fromNamespaceAndPath(ShinColle.MOD_ID, recipePath);
        var recipe = helper.getLevel().getRecipeManager().getRecipeFor(RecipeType.CRAFTING, grid, helper.getLevel());
        if (recipe.isEmpty()) {
            helper.fail("expected recipe to resolve: " + recipeId);
            return;
        }
        if (!recipe.get().getId().equals(recipeId)) {
            helper.fail("resolved wrong recipe for " + recipeId + ": " + recipe.get().getId());
            return;
        }

        ItemStack result = recipe.get().assemble(grid, helper.getLevel().registryAccess());
        if (!ItemStack.isSameItemSameTags(result, expected) || result.getCount() != expected.getCount()) {
            helper.fail("recipe " + recipeId + " returned " + result + " instead of " + expected);
        }
    }

    @GameTest(template = "empty")
    public static void b5OreTagResourcesAndConditionalRecipesParity(GameTestHelper helper) {
        if (!tagResourceContains("data/forge/tags/items/paper.json", "minecraft:paper")) {
            helper.fail("forge paper tag resource should expose minecraft:paper");
            return;
        }

        String[] emptyCompatTagPaths = {
                "data/forge/tags/items/flour.json",
                "data/forge/tags/items/cooked_rice.json",
                "data/forge/tags/items/ingots/tin.json",
                "data/forge/tags/items/ingots/bronze.json",
                "data/forge/tags/items/ingots/lead.json",
                "data/forge/tags/items/ingots/steel.json",
                "data/forge/tags/items/ingots/silver.json",
                "data/forge/tags/items/ingots/uranium.json"
        };
        for (String tagPath : emptyCompatTagPaths) {
            JsonObject json = loadJsonResource(tagPath);
            if (json == null || !json.has("values") || !json.get("values").isJsonArray()
                    || json.getAsJsonArray("values").size() != 0) {
                helper.fail("cross-mod compat tag declaration should exist with empty defaults: " + tagPath);
                return;
            }
        }

        if (!new ItemStack(Items.IRON_INGOT).is(itemTag("forge", "ingots/iron"))
                || !new ItemStack(Items.GOLD_INGOT).is(itemTag("forge", "ingots/gold"))
                || !new ItemStack(Items.COPPER_INGOT).is(itemTag("forge", "ingots/copper"))
                || !new ItemStack(Items.DIAMOND).is(itemTag("forge", "gems/diamond"))
                || !new ItemStack(Items.LAPIS_LAZULI).is(itemTag("forge", "gems/lapis"))
                || !new ItemStack(Items.BLUE_DYE).is(itemTag("forge", "dyes/blue"))
                || !new ItemStack(Items.GUNPOWDER).is(itemTag("forge", "gunpowder"))
                || !new ItemStack(Items.OBSIDIAN).is(itemTag("forge", "obsidian"))
                || !new ItemStack(Items.STICK).is(itemTag("forge", "rods/wooden"))
                || !new ItemStack(Items.WHEAT).is(itemTag("forge", "crops/wheat"))
                || !new ItemStack(Items.NETHER_STAR).is(itemTag("forge", "nether_stars"))
                || !new ItemStack(Items.DIAMOND_BLOCK).is(itemTag("forge", "storage_blocks/diamond"))
                || !new ItemStack(Items.PAPER).is(itemTag("forge", "paper"))
                || !new ItemStack(ModItems.GRUDGE.get()).is(itemTag("forge", "grudge"))
                || !new ItemStack(ModItems.GRUDGE1.get()).is(itemTag("forge", "grudge"))
                || new ItemStack(ModItems.ABYSSMETAL1.get()).is(itemTag("forge", "ingots/manganese"))) {
            helper.fail("runtime Forge item tags should match 1.12 OreDictionary defaults including polyAsMn=false");
            return;
        }

        String[] conditionalRecipeIds = {
                "ammo_from_tin", "ammo_from_bronze", "ammo_from_lead",
                "ammo_from_steel", "ammo_from_silver", "ammo_from_uranium",
                "ammo2_from_tin", "ammo2_from_bronze", "ammo2_from_lead",
                "ammo2_from_steel", "ammo2_from_silver", "ammo2_from_uranium",
                "combatration2_rice"
        };
        for (String recipePath : conditionalRecipeIds) {
            ResourceLocation id = ResourceLocation.fromNamespaceAndPath(ShinColle.MOD_ID, recipePath);
            if (helper.getLevel().getRecipeManager().byKey(id).isPresent()) {
                helper.fail("conditional cross-mod recipe should stay gated off when its Forge tag is empty: " + id);
                return;
            }
        }

        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void b5LegacyOreTagRecipesParity(GameTestHelper helper) {
        assertCrafts(helper, "abyssmetal_from_iron_and_grudge", new ItemStack(ModItems.ABYSSMETAL.get()),
                stack(Items.IRON_INGOT), stack(ModItems.GRUDGE.get()));

        assertCrafts(helper, "ammo", new ItemStack(ModItems.AMMO.get(), 16),
                stack(Items.IRON_INGOT), stack(Items.IRON_INGOT), stack(Items.IRON_INGOT),
                stack(Items.IRON_INGOT), stack(ModItems.GRUDGE.get()), stack(Items.IRON_INGOT),
                stack(Items.IRON_INGOT), stack(Items.GUNPOWDER), stack(Items.IRON_INGOT));
        assertCrafts(helper, "ammo_from_copper", new ItemStack(ModItems.AMMO.get(), 8),
                stack(Items.COPPER_INGOT), stack(Items.COPPER_INGOT), stack(Items.COPPER_INGOT),
                stack(Items.COPPER_INGOT), stack(ModItems.GRUDGE.get()), stack(Items.COPPER_INGOT),
                stack(Items.COPPER_INGOT), stack(Items.GUNPOWDER), stack(Items.COPPER_INGOT));
        assertCrafts(helper, "ammo_from_abyssium", new ItemStack(ModItems.AMMO.get(), 24),
                stack(ModItems.ABYSSMETAL.get()), stack(ModItems.ABYSSMETAL.get()), stack(ModItems.ABYSSMETAL.get()),
                stack(ModItems.ABYSSMETAL.get()), stack(ModItems.GRUDGE.get()), stack(ModItems.ABYSSMETAL.get()),
                stack(ModItems.ABYSSMETAL.get()), stack(Items.GUNPOWDER), stack(ModItems.ABYSSMETAL.get()));
        assertCrafts(helper, "ammo_from_gold", new ItemStack(ModItems.AMMO.get(), 48),
                stack(Items.GOLD_INGOT), stack(Items.GOLD_INGOT), stack(Items.GOLD_INGOT),
                stack(Items.GOLD_INGOT), stack(ModItems.GRUDGE.get()), stack(Items.GOLD_INGOT),
                stack(Items.GOLD_INGOT), stack(Items.GUNPOWDER), stack(Items.GOLD_INGOT));
        assertCrafts(helper, "ammo_from_diamond", new ItemStack(ModItems.AMMO.get(), 64),
                stack(Items.DIAMOND), stack(Items.DIAMOND), stack(Items.DIAMOND),
                stack(Items.DIAMOND), stack(ModItems.GRUDGE.get()), stack(Items.DIAMOND),
                stack(Items.DIAMOND), stack(Items.GUNPOWDER), stack(Items.DIAMOND));

        assertCrafts(helper, "ammo1", new ItemStack(ModItems.AMMO1.get()),
                stack(ModItems.AMMO.get()), stack(ModItems.AMMO.get()), stack(ModItems.AMMO.get()),
                stack(ModItems.AMMO.get()), stack(ModItems.AMMO.get()), stack(ModItems.AMMO.get()),
                stack(ModItems.AMMO.get()), stack(ModItems.AMMO.get()), stack(ModItems.AMMO.get()));
        assertCrafts(helper, "ammo_from_ammo1", new ItemStack(ModItems.AMMO.get(), 9),
                stack(ModItems.AMMO1.get()));

        assertCrafts(helper, "ammo2", new ItemStack(ModItems.AMMO2.get(), 4),
                stack(Items.IRON_INGOT), stack(Items.IRON_INGOT), stack(Items.IRON_INGOT),
                stack(Items.IRON_INGOT), stack(ModItems.GRUDGE.get()), stack(Items.IRON_INGOT),
                stack(Items.IRON_INGOT), stack(Items.BLAZE_POWDER), stack(Items.IRON_INGOT));
        assertCrafts(helper, "ammo2_from_copper", new ItemStack(ModItems.AMMO2.get(), 2),
                stack(Items.COPPER_INGOT), stack(Items.COPPER_INGOT), stack(Items.COPPER_INGOT),
                stack(Items.COPPER_INGOT), stack(ModItems.GRUDGE.get()), stack(Items.COPPER_INGOT),
                stack(Items.COPPER_INGOT), stack(Items.BLAZE_POWDER), stack(Items.COPPER_INGOT));
        assertCrafts(helper, "ammo2_from_abyssium", new ItemStack(ModItems.AMMO2.get(), 6),
                stack(ModItems.ABYSSMETAL.get()), stack(ModItems.ABYSSMETAL.get()), stack(ModItems.ABYSSMETAL.get()),
                stack(ModItems.ABYSSMETAL.get()), stack(ModItems.GRUDGE.get()), stack(ModItems.ABYSSMETAL.get()),
                stack(ModItems.ABYSSMETAL.get()), stack(Items.BLAZE_POWDER), stack(ModItems.ABYSSMETAL.get()));
        assertCrafts(helper, "ammo2_from_gold", new ItemStack(ModItems.AMMO2.get(), 12),
                stack(Items.GOLD_INGOT), stack(Items.GOLD_INGOT), stack(Items.GOLD_INGOT),
                stack(Items.GOLD_INGOT), stack(ModItems.GRUDGE.get()), stack(Items.GOLD_INGOT),
                stack(Items.GOLD_INGOT), stack(Items.BLAZE_POWDER), stack(Items.GOLD_INGOT));
        assertCrafts(helper, "ammo2_from_diamond", new ItemStack(ModItems.AMMO2.get(), 16),
                stack(Items.DIAMOND), stack(Items.DIAMOND), stack(Items.DIAMOND),
                stack(Items.DIAMOND), stack(ModItems.GRUDGE.get()), stack(Items.DIAMOND),
                stack(Items.DIAMOND), stack(Items.BLAZE_POWDER), stack(Items.DIAMOND));

        assertCrafts(helper, "ammo3", new ItemStack(ModItems.AMMO3.get()),
                stack(ModItems.AMMO2.get()), stack(ModItems.AMMO2.get()), stack(ModItems.AMMO2.get()),
                stack(ModItems.AMMO2.get()), stack(ModItems.AMMO2.get()), stack(ModItems.AMMO2.get()),
                stack(ModItems.AMMO2.get()), stack(ModItems.AMMO2.get()), stack(ModItems.AMMO2.get()));
        assertCrafts(helper, "ammo2_from_ammo3", new ItemStack(ModItems.AMMO2.get(), 9),
                stack(ModItems.AMMO3.get()));

        assertCrafts(helper, "blockframe", new ItemStack(ModBlocks.BLOCK_FRAME.get(), 16),
                stack(ModItems.ABYSSMETAL.get()), ItemStack.EMPTY, stack(ModItems.ABYSSMETAL.get()),
                ItemStack.EMPTY, stack(Items.OBSIDIAN), ItemStack.EMPTY,
                stack(ModItems.ABYSSMETAL.get()), ItemStack.EMPTY, stack(ModItems.ABYSSMETAL.get()));
        assertCrafts(helper, "blockvolblock", new ItemStack(ModBlocks.BLOCK_VOL_BLOCK.get()),
                stack(Items.MAGMA_BLOCK), stack(Items.OBSIDIAN), stack(Items.MAGMA_BLOCK),
                stack(Items.OBSIDIAN), stack(ModBlocks.BLOCK_GRUDGE.get()), stack(Items.OBSIDIAN),
                stack(Items.MAGMA_BLOCK), stack(Items.OBSIDIAN), stack(Items.MAGMA_BLOCK));
        assertCrafts(helper, "blockvolblock_alt", new ItemStack(ModBlocks.BLOCK_VOL_BLOCK.get()),
                stack(Items.OBSIDIAN), stack(Items.MAGMA_BLOCK), stack(Items.OBSIDIAN),
                stack(Items.MAGMA_BLOCK), stack(ModBlocks.BLOCK_GRUDGE.get()), stack(Items.MAGMA_BLOCK),
                stack(Items.OBSIDIAN), stack(Items.MAGMA_BLOCK), stack(Items.OBSIDIAN));
        assertCrafts(helper, "blockvolcore", new ItemStack(ModBlocks.BLOCK_VOL_CORE.get()),
                stack(ModBlocks.BLOCK_VOL_BLOCK.get()), stack(Items.OBSIDIAN), stack(ModBlocks.BLOCK_VOL_BLOCK.get()),
                stack(Items.OBSIDIAN), stack(ModBlocks.BLOCK_GRUDGE_HEAVY.get()), stack(Items.OBSIDIAN),
                stack(ModBlocks.BLOCK_VOL_BLOCK.get()), stack(Items.OBSIDIAN), stack(ModBlocks.BLOCK_VOL_BLOCK.get()));
        assertCrafts(helper, "blockvolcore_alt", new ItemStack(ModBlocks.BLOCK_VOL_CORE.get()),
                stack(Items.OBSIDIAN), stack(ModBlocks.BLOCK_VOL_BLOCK.get()), stack(Items.OBSIDIAN),
                stack(ModBlocks.BLOCK_VOL_BLOCK.get()), stack(ModBlocks.BLOCK_GRUDGE_HEAVY.get()), stack(ModBlocks.BLOCK_VOL_BLOCK.get()),
                stack(Items.OBSIDIAN), stack(ModBlocks.BLOCK_VOL_BLOCK.get()), stack(Items.OBSIDIAN));

        TransientCraftingContainer horizontalWaypoint = craftingGrid(
                stack(ModItems.GRUDGE.get()), stack(Items.STICK));
        if (helper.getLevel().getRecipeManager().getRecipeFor(RecipeType.CRAFTING, horizontalWaypoint, helper.getLevel()).isPresent()) {
            helper.fail("blockwaypoint should require the 1.12 vertical shaped pattern");
            return;
        }

        assertCrafts(helper, "combatration", new ItemStack(ModItems.COMBATRATION.get()),
                stack(Items.BREAD), stack(Items.GOLDEN_CARROT), stack(ModItems.GRUDGE.get()),
                stack(Items.WHEAT), stack(Items.WHEAT), stack(Items.WHEAT));
        TransientCraftingContainer threeRowRation = craftingGrid(
                stack(Items.WHEAT), stack(Items.WHEAT), stack(Items.WHEAT),
                stack(Items.BREAD), stack(ModItems.GRUDGE.get()), stack(Items.GOLDEN_CARROT),
                stack(Items.WHEAT), stack(Items.WHEAT), stack(Items.WHEAT));
        if (helper.getLevel().getRecipeManager().getRecipeFor(RecipeType.CRAFTING, threeRowRation, helper.getLevel()).isPresent()) {
            helper.fail("combatration should require the 1.12 two-row pattern");
            return;
        }

        assertCrafts(helper, "combatration2", new ItemStack(ModItems.COMBATRATION2.get()),
                stack(Items.WHEAT), stack(Items.WHEAT), stack(Items.WHEAT),
                stack(Items.COD), stack(Items.GOLDEN_CARROT), stack(ModItems.GRUDGE.get()),
                stack(Items.WHEAT), stack(Items.WHEAT), stack(Items.WHEAT));
        assertCrafts(helper, "combatration2", new ItemStack(ModItems.COMBATRATION2.get()),
                stack(Items.WHEAT), stack(Items.WHEAT), stack(Items.WHEAT),
                stack(Items.TROPICAL_FISH), stack(Items.GOLDEN_CARROT), stack(ModItems.GRUDGE.get()),
                stack(Items.WHEAT), stack(Items.WHEAT), stack(Items.WHEAT));

        assertCrafts(helper, "marriagering", new ItemStack(ModItems.MARRIAGERING.get()),
                ItemStack.EMPTY, stack(Items.NETHER_STAR), ItemStack.EMPTY,
                stack(ModItems.ABYSSNUGGET.get()), ItemStack.EMPTY, stack(ModItems.ABYSSNUGGET.get()),
                ItemStack.EMPTY, stack(ModItems.ABYSSNUGGET.get()), ItemStack.EMPTY);
        TransientCraftingContainer ingotMarriageRing = craftingGrid(
                ItemStack.EMPTY, stack(Items.NETHER_STAR), ItemStack.EMPTY,
                stack(ModItems.ABYSSMETAL.get()), ItemStack.EMPTY, stack(ModItems.ABYSSMETAL.get()),
                ItemStack.EMPTY, stack(ModItems.ABYSSMETAL.get()), ItemStack.EMPTY);
        if (helper.getLevel().getRecipeManager().getRecipeFor(RecipeType.CRAFTING, ingotMarriageRing, helper.getLevel()).isPresent()) {
            helper.fail("marriagering should require AbyssNugget instead of AbyssMetal ingots");
            return;
        }

        assertCrafts(helper, "recipepaper", new ItemStack(ModItems.RECIPEPAPER.get()),
                stack(ModItems.GRUDGE.get()), stack(Items.PAPER), stack(Items.BLUE_DYE));
        assertCrafts(helper, "repairgoddess", new ItemStack(ModItems.REPAIRGODDESS.get()),
                stack(ModBlocks.BLOCK_GRUDGE_HEAVY.get()), stack(ModBlocks.BLOCK_GRUDGE.get()), stack(ModBlocks.BLOCK_GRUDGE_HEAVY.get()),
                stack(ModBlocks.BLOCK_GRUDGE.get()), stack(Items.DIAMOND_BLOCK), stack(ModBlocks.BLOCK_GRUDGE.get()),
                stack(ModBlocks.BLOCK_GRUDGE_HEAVY.get()), stack(ModBlocks.BLOCK_GRUDGE.get()), stack(ModBlocks.BLOCK_GRUDGE_HEAVY.get()));
        assertCrafts(helper, "repairgoddess_alt", new ItemStack(ModItems.REPAIRGODDESS.get()),
                stack(ModBlocks.BLOCK_GRUDGE.get()), stack(ModBlocks.BLOCK_GRUDGE_HEAVY.get()), stack(ModBlocks.BLOCK_GRUDGE.get()),
                stack(ModBlocks.BLOCK_GRUDGE_HEAVY.get()), stack(Items.DIAMOND_BLOCK), stack(ModBlocks.BLOCK_GRUDGE_HEAVY.get()),
                stack(ModBlocks.BLOCK_GRUDGE.get()), stack(ModBlocks.BLOCK_GRUDGE_HEAVY.get()), stack(ModBlocks.BLOCK_GRUDGE.get()));
        assertCrafts(helper, "shiptank1", new ItemStack(ModItems.SHIPTANK1.get()),
                stack(Items.OBSIDIAN), stack(ModItems.SHIPTANK.get()), stack(Items.OBSIDIAN),
                stack(Items.OBSIDIAN), stack(ModItems.SHIPTANK.get()), stack(Items.OBSIDIAN),
                stack(Items.OBSIDIAN), stack(ModItems.SHIPTANK.get()), stack(Items.OBSIDIAN));

        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void b5CraneAndTaskOreTagFilterMatchingParity(GameTestHelper helper) {
        Player player = helper.makeMockPlayer();
        BlockPos cranePos = helper.absolutePos(new BlockPos(1, 2, 1));
        player.setPos(cranePos.getX() + 0.5D, cranePos.getY() + 0.5D, cranePos.getZ() + 0.5D);
        helper.getLevel().setBlock(cranePos, ModBlocks.BLOCK_CRANE.get().defaultBlockState(), Block.UPDATE_ALL);
        if (!(helper.getLevel().getBlockEntity(cranePos) instanceof CraneBlockEntity crane)) {
            helper.fail("crane block entity should exist for B5 filter toggle coverage");
            return;
        }

        if (crane.isCheckMetadata() || crane.isCheckOredict() || crane.isCheckNbt()) {
            helper.fail("crane checkMetadata/checkOredict/checkNbt should default to false");
            return;
        }

        CraneTerminalMenu menu = new CraneTerminalMenu(1, player.getInventory(), cranePos);
        if (!menu.clickMenuButton(player, CraneTerminalMenu.BUTTON_TOGGLE_META)
                || !menu.clickMenuButton(player, CraneTerminalMenu.BUTTON_TOGGLE_OREDICT)
                || !menu.clickMenuButton(player, CraneTerminalMenu.BUTTON_TOGGLE_NBT)
                || !crane.isCheckMetadata() || !menu.isCheckMetadata()
                || !crane.isCheckOredict() || !menu.isCheckOredict()
                || !crane.isCheckNbt() || !menu.isCheckNbt()) {
            helper.fail("crane menu buttons 6..8 should mutate and expose meta/dict/nbt flags");
            return;
        }

        CompoundTag saved = crane.saveWithFullMetadata();
        if (!saved.getBoolean("meta") || !saved.getBoolean("dict") || !saved.getBoolean("nbt")) {
            helper.fail("crane should persist legacy meta/dict/nbt NBT keys");
            return;
        }

        CraneBlockEntity reloaded = new CraneBlockEntity(cranePos, ModBlocks.BLOCK_CRANE.get().defaultBlockState());
        reloaded.load(saved);
        if (!reloaded.isCheckMetadata() || !reloaded.isCheckOredict() || !reloaded.isCheckNbt()) {
            helper.fail("crane should restore meta/dict/nbt flags from NBT");
            return;
        }

        crane.setCheckMetadata(false);
        crane.setCheckOredict(false);
        crane.setCheckNbt(false);
        crane.setFilter(0, new ItemStack(ModItems.GRUDGE.get()), false);
        crane.setFilter(9, new ItemStack(Items.SPRUCE_PLANKS), false);
        if (crane.matchesTransferFilter(new ItemStack(ModItems.GRUDGE1.get()), true)
                || crane.matchesTransferFilter(new ItemStack(Items.OAK_PLANKS), false)) {
            helper.fail("crane should not match tag siblings when checkOredict is false");
            return;
        }

        crane.setCheckOredict(true);
        if (!crane.matchesTransferFilter(new ItemStack(ModItems.GRUDGE1.get()), true)
                || !crane.matchesTransferFilter(new ItemStack(Items.OAK_PLANKS), false)
                || crane.matchesTransferFilter(new ItemStack(Items.IRON_INGOT), false)) {
            helper.fail("crane should match Forge item tag siblings when checkOredict is true");
            return;
        }

        ItemStack undamagedSword = new ItemStack(Items.IRON_SWORD);
        ItemStack damagedSword = new ItemStack(Items.IRON_SWORD);
        damagedSword.setDamageValue(15);
        if (!LegacyShipBehaviorCatalog.matchesLegacyTaskItem(damagedSword, undamagedSword, false, false, false)
                || LegacyShipBehaviorCatalog.matchesLegacyTaskItem(damagedSword, undamagedSword, true, false, false)
                || !LegacyShipBehaviorCatalog.matchesLegacyTaskItem(damagedSword, undamagedSword, false, false, true)
                || LegacyShipBehaviorCatalog.matchesLegacyTaskItem(damagedSword, undamagedSword, true, false, true)) {
            helper.fail("task item matching should keep checkNbt independent of damage and checkMetadata sensitive to damage");
            return;
        }

        damagedSword.getOrCreateTag().putInt("CustomTag", 1);
        if (LegacyShipBehaviorCatalog.matchesLegacyTaskItem(damagedSword, undamagedSword, false, false, true)) {
            helper.fail("task item matching should reject mismatched non-damage NBT when checkNbt is true");
            return;
        }

        undamagedSword.getOrCreateTag().putInt("CustomTag", 1);
        if (!LegacyShipBehaviorCatalog.matchesLegacyTaskItem(damagedSword, undamagedSword, false, false, true)
                || !LegacyShipBehaviorCatalog.matchesLegacyTaskItem(new ItemStack(ModItems.GRUDGE1.get()), new ItemStack(ModItems.GRUDGE.get()), false, true, false)
                || LegacyShipBehaviorCatalog.matchesLegacyTaskItem(new ItemStack(ModItems.GRUDGE1.get()), new ItemStack(ModItems.GRUDGE.get()), false, false, false)) {
            helper.fail("task item matching should preserve checkNbt and checkOredict tag semantics");
            return;
        }

        helper.succeed();
    }


    // --------------------------------------------------------------------------------
    // Historical Regression Backfill (182 Tests from SHIP-01 through SHIP-ATTACK-PROFILE-AUDIT)
    // --------------------------------------------------------------------------------

    @GameTest(template = "empty")
    public static void fiveSubmarineProjectileVariantsKeepLegacyMoveAndSpeedRules(GameTestHelper helper) {
        int[] eggMetas = {19, 20, 21, 40, 41};
        for (int index = 0; index < eggMetas.length; index++) {
            ShipEntitySpec spec = ShipEntitySpecs.getByEggMeta(eggMetas[index]);
            LegacyShipAttackProfile profile = LegacyShipBehaviorCatalog.attackProfile(spec);
            boolean hasLegacyLightOverride = index == 1 || index >= 3;
            if (profile.projectileProfile(LegacyShipAttackKind.LIGHT).isPresent() != hasLegacyLightOverride) {
                helper.fail("submarine light projectile profile should follow the legacy override boundary: egg="
                        + eggMetas[index]);
                return;
            }

            if (LegacyShipBehaviorCatalog.legacySubmarineProjectileMoveTypeFor(
                    spec, 10.0D, 3.0D, 9.0D, LegacyShipProjectileMoveType.ARC)
                    != LegacyShipProjectileMoveType.DIRECT
                    || LegacyShipBehaviorCatalog.legacySubmarineProjectileMoveTypeFor(
                    spec, 10.0D, 1.0D, 9.0D, LegacyShipProjectileMoveType.ARC)
                    != LegacyShipProjectileMoveType.TORPEDO
                || LegacyShipBehaviorCatalog.legacySubmarineProjectileMoveTypeFor(
                    spec, 10.0D, 1.0D, 12.0D, LegacyShipProjectileMoveType.ARC)
                    != LegacyShipProjectileMoveType.ARC
                || LegacyShipBehaviorCatalog.legacySubmarineProjectileMoveTypeFor(
                    spec, 10.0D, 0.0D, 9.0D, LegacyShipProjectileMoveType.ARC)
                    != LegacyShipProjectileMoveType.ARC) {
                helper.fail("submarine projectile movement should mirror CombatHelper depth selection: egg="
                        + eggMetas[index]);
                return;
            }
        }

        SimpleContainer inventory = new SimpleContainer(LegacyShipEntity.SHIP_SLOT_COUNT);
        inventory.setItem(0, new ItemStack(ModItems.EQUIPTORPEDO_ITEMS.get(6).get()));
        ShipEquipmentBehaviorState state = ShipEquipmentBehaviorState.fromInventory(
                inventory, ShipArchetype.SUBMARINE);
        if (state.torpedoSpeedLevel() != 3
                || Math.abs(state.legacyProjectileSpeed(0.8D) - 0.875D) > 1.0E-7D) {
            helper.fail("submarine missile equipment should add the legacy 0.025-per-level speed bonus");
            return;
        }

        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void himeSubmarineProjectilePathUsesLegacyDepthSelection(GameTestHelper helper) {
        int[] eggMetas = {46, 74};
        for (int eggMeta : eggMetas) {
            ShipEntitySpec spec = ShipEntitySpecs.getByEggMeta(eggMeta);
            if (spec == null || !LegacyShipBehaviorCatalog.attackProfile(spec)
                    .projectileProfile(LegacyShipAttackKind.LIGHT).isPresent()) {
                helper.fail("hime submarine should retain its legacy light missile profile: egg=" + eggMeta);
                return;
            }

            if (LegacyShipBehaviorCatalog.legacySubmarineProjectileMoveTypeFor(
                    spec, 10.0D, 3.0D, 9.0D, LegacyShipProjectileMoveType.ARC)
                    != LegacyShipProjectileMoveType.DIRECT
                    || LegacyShipBehaviorCatalog.legacySubmarineProjectileMoveTypeFor(
                    spec, 10.0D, 1.0D, 9.0D, LegacyShipProjectileMoveType.ARC)
                    != LegacyShipProjectileMoveType.TORPEDO
                || LegacyShipBehaviorCatalog.legacySubmarineProjectileMoveTypeFor(
                    spec, 10.0D, 1.0D, 12.0D, LegacyShipProjectileMoveType.ARC)
                    != LegacyShipProjectileMoveType.ARC
                || LegacyShipBehaviorCatalog.legacySubmarineProjectileMoveTypeFor(
                    spec, 10.0D, 0.0D, 9.0D, LegacyShipProjectileMoveType.ARC)
                    != LegacyShipProjectileMoveType.ARC) {
                helper.fail("hime submarine projectile movement should mirror CombatHelper depth selection: egg="
                        + eggMeta);
                return;
            }
        }

        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void waypointRouteBacktracksUsingLegacyLastWaypoint(GameTestHelper helper) {
        BlockPos waypointPos = helper.absolutePos(new BlockPos(1, 2, 1));
        BlockPos backWaypointPos = helper.absolutePos(new BlockPos(12, 2, 1));
        BlockPos nextWaypointPos = helper.absolutePos(new BlockPos(24, 2, 1));
        helper.getLevel().setBlock(waypointPos, ModBlocks.BLOCK_WAYPOINT.get().defaultBlockState(), Block.UPDATE_ALL);
        helper.getLevel().setBlock(backWaypointPos, ModBlocks.BLOCK_WAYPOINT.get().defaultBlockState(), Block.UPDATE_ALL);
        helper.getLevel().setBlock(nextWaypointPos, ModBlocks.BLOCK_WAYPOINT.get().defaultBlockState(), Block.UPDATE_ALL);

        if (!(helper.getLevel().getBlockEntity(waypointPos) instanceof WaypointBlockEntity waypoint)) {
            helper.fail("waypoint block entity should exist for route backtrack test");
            return;
        }

        waypoint.setLastWaypoint(backWaypointPos);
        waypoint.setNextWaypoint(nextWaypointPos);

        LegacyShipEntity ship = ModEntityTypes.LEGACY_SHIP.get().create(helper.getLevel());
        if (ship == null) {
            helper.fail("legacy ship entity should be creatable for route backtrack tests");
            return;
        }

        ship.setNoGravity(true);
        ship.setNoAi(true);
        ship.setInvulnerable(true);
        ship.setLastWaypoint(nextWaypointPos);
        ship.setPos(waypointPos.getX() + 0.5D, waypointPos.getY() + 0.1D, waypointPos.getZ() + 0.5D);
        helper.getLevel().addFreshEntity(ship);
        ship.commandMoveTo(waypointPos, "");
        ship.tick();

        if (!backWaypointPos.equals(ship.getRouteNodePos())
                || !waypointPos.equals(ship.getLastWaypoint())
                || ship.canFollow()) {
            helper.fail("route should reverse to the waypoint's legacy last link when next repeats the ship's last waypoint");
            return;
        }

        waypoint.setLastWaypoint(null);
        waypoint.setNextWaypoint(null);
        ship.commandMoveTo(waypointPos, "");
        ship.tick();
        if (!waypointPos.equals(ship.getRouteNodePos()) || ship.canFollow()) {
            helper.fail("a route without an outgoing waypoint should keep its current guard node");
            return;
        }

        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void legacyWaypointNbtMigratesOwnerAndRouteLinks(GameTestHelper helper) {
        BlockPos lastPos = new BlockPos(4, 2, 4);
        BlockPos nextPos = new BlockPos(8, 2, 4);
        BlockPos chestPos = new BlockPos(6, 2, 8);
        CompoundTag legacyTag = new CompoundTag();
        legacyTag.putInt("wpstay", 6);
        legacyTag.putInt("pid", 8121);
        legacyTag.putIntArray("lastPos", new int[]{lastPos.getX(), lastPos.getY(), lastPos.getZ()});
        legacyTag.putIntArray("nextPos", new int[]{nextPos.getX(), nextPos.getY(), nextPos.getZ()});
        legacyTag.putIntArray("chestPos", new int[]{chestPos.getX(), chestPos.getY(), chestPos.getZ()});

        WaypointBlockEntity waypoint = new WaypointBlockEntity(
                helper.absolutePos(new BlockPos(1, 2, 1)),
                ModBlocks.BLOCK_WAYPOINT.get().defaultBlockState());
        waypoint.load(legacyTag);

        if (waypoint.getStayMode() != 6
                || waypoint.getOwnerUid() != 8121
                || !lastPos.equals(waypoint.getLastWaypoint())
                || !nextPos.equals(waypoint.getNextWaypoint())
                || !chestPos.equals(waypoint.getPairedChest())) {
            helper.fail("legacy waypoint NBT should migrate pid, wpstay, and all three route positions");
            return;
        }

        Player matchingOwner = helper.makeMockPlayer();
        Player otherPlayer = helper.makeMockPlayer();
        if (!configureTeitoku(helper, matchingOwner, data -> data.setPlayerUid(8121))
                || !configureTeitoku(helper, otherPlayer, data -> data.setPlayerUid(8122))
                || !waypoint.canEdit(matchingOwner)
                || waypoint.canEdit(otherPlayer)) {
            helper.fail("legacy waypoint pid ownership should remain restricted to the matching Teitoku UID");
            return;
        }

        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void shipCanDropMigratesAndSuppressesDuplicateRecoveryEgg(GameTestHelper helper) {
        Player player = helper.makeMockPlayer();
        LegacyShipEntity migrated = ModEntityTypes.LEGACY_SHIP.get().create(helper.getLevel());
        if (migrated == null) {
            helper.fail("legacy ship should be creatable for CanDrop migration coverage");
            return;
        }

        CompoundTag legacyTag = new CompoundTag();
        legacyTag.putInt("VariantEggMeta", 60);
        CompoundTag legacyExtended = new CompoundTag();
        CompoundTag legacyFlags = new CompoundTag();
        legacyFlags.putBoolean("CanDrop", false);
        legacyExtended.put("ShipFlags", legacyFlags);
        legacyTag.put("ShipExtProps", legacyExtended);
        migrated.readAdditionalSaveData(legacyTag);
        if (migrated.canDropItem()) {
            helper.fail("legacy ShipFlags.CanDrop=false should migrate to the modern server state");
            return;
        }

        CompoundTag saved = new CompoundTag();
        migrated.addAdditionalSaveData(saved);
        if (saved.getBoolean("ShipCanDrop")) {
            helper.fail("modern save should preserve a consumed CanDropItem gate");
            return;
        }

        ShipWorldCacheEntry cached = ShipWorldCacheEntry.fromShip(migrated, false);
        cached.entityTag().remove("ShipCanDrop");
        cached.entityTag().put("ShipExtProps", legacyExtended.copy());
        if (cached.isCanDropItem()) {
            helper.fail("detached ship cache should preserve legacy CanDrop=false");
            return;
        }

        CompoundTag modernWins = legacyTag.copy();
        modernWins.putBoolean("ShipCanDrop", true);
        LegacyShipEntity modern = ModEntityTypes.LEGACY_SHIP.get().create(helper.getLevel());
        if (modern == null) {
            helper.fail("modern precedence ship should be creatable");
            return;
        }
        modern.readAdditionalSaveData(modernWins);
        if (!modern.canDropItem()) {
            helper.fail("modern ShipCanDrop should take precedence over legacy CanDrop");
            return;
        }

        migrated.setPos(1.5D, 2.0D, 1.5D);
        helper.getLevel().addFreshEntity(migrated);
        migrated.applySpawnSpec(ShipEntitySpecs.getByEggMeta(60), player);
        for (ItemEntity itemEntity : helper.getLevel().getEntitiesOfClass(
                ItemEntity.class, migrated.getBoundingBox().inflate(8.0D))) {
            itemEntity.discard();
        }
        migrated.setCanDropItem(false);
        if (!migrated.hurt(migrated.damageSources().generic(), 10000.0F)) {
            helper.fail("lethal test damage should be accepted by the CanDrop gate ship");
            return;
        }

        for (ItemEntity itemEntity : helper.getLevel().getEntitiesOfClass(
                ItemEntity.class, migrated.getBoundingBox().inflate(4.0D))) {
            if (LegacyShipSpawnEggItem.isRecoveredShipStack(itemEntity.getItem())) {
                helper.fail("CanDropItem=false should suppress the recovery egg");
                return;
            }
        }

        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void invalidGuardTargetRestoresIdleState(GameTestHelper helper) {
        Player player = helper.makeMockPlayer();
        LegacyShipEntity ship = createOwnedShip(helper, player, 58, 2.5D, 2.0D, 2.5D);
        Zombie target = new Zombie(EntityType.ZOMBIE, helper.getLevel());
        if (ship == null) {
            return;
        }

        target.setPos(4.5D, 2.0D, 2.5D);
        helper.getLevel().addFreshEntity(target);
        ship.commandGuardEntity(UUID.randomUUID());
        ship.setTarget(target);
        ship.tick();

        if (ship.getGuardEntityUuid() != null || ship.getTarget() != null || !ship.getNavigation().isDone()) {
            helper.fail("dead or missing guard targets should clear guard state, combat target, and navigation");
            return;
        }

        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void explicitMoveCommandWinsOverAutonomousCombat(GameTestHelper helper) {
        Player player = helper.makeMockPlayer();
        LegacyShipEntity ship = createOwnedShip(helper, player, 58, 2.5D, 2.0D, 2.5D);
        Zombie target = new Zombie(EntityType.ZOMBIE, helper.getLevel());
        if (ship == null) {
            return;
        }

        target.setPos(4.5D, 2.0D, 2.5D);
        helper.getLevel().addFreshEntity(target);
        BlockPos moveTarget = helper.absolutePos(new BlockPos(8, 2, 8));
        ship.commandMoveTo(moveTarget, helper.getLevel().dimension().location().toString());

        if (ship.canExecuteCombatGoal() || ship.canAutoTargetEnemyMob(target)) {
            helper.fail("explicit move commands should keep autonomous combat from taking the navigation slot");
            return;
        }

        ship.clearCommandState();
        ship.commandGuardEntity(UUID.randomUUID());
        if (ship.canExecuteCombatGoal() || ship.canAutoTargetEnemyMob(target)) {
            helper.fail("explicit guard commands should keep autonomous combat from taking the navigation slot");
            return;
        }

        ship.clearCommandState();
        ship.commandGuardBlock(new BlockPos(8, 2, 8), helper.getLevel().dimension().location().toString());
        if (ship.canExecuteCombatGoal() || ship.canAutoTargetEnemyMob(target)) {
            helper.fail("explicit block-guard commands should keep autonomous combat from taking the navigation slot");
            return;
        }

        ship.clearCommandState();
        if (!ship.canExecuteCombatGoal() || !ship.canAutoTargetEnemyMob(target)) {
            helper.fail("autonomous combat should resume after the explicit move command is cleared");
            return;
        }

        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void passiveAiStopsAutomaticRangeTargeting(GameTestHelper helper) {
        Player player = helper.makeMockPlayer();
        LegacyShipEntity ship = createOwnedShip(helper, player, 58, 2.5D, 2.0D, 2.5D);
        Zombie target = new Zombie(EntityType.ZOMBIE, helper.getLevel());
        if (ship == null) {
            return;
        }

        target.setNoAi(true);
        target.setPos(4.0D, 2.0D, 2.5D);
        helper.getLevel().addFreshEntity(target);
        ship.setAiOnSightChase(false);
        ship.setAiAutoTarget(false);
        ship.tickCount = 8;

        LegacyShipRangeTargetGoal goal = new LegacyShipRangeTargetGoal(ship);
        if (goal.canUse()) {
            helper.fail("legacy PassiveAI should keep the automatic range target goal disabled");
            return;
        }

        ship.setAiAutoTarget(true);
        if (!goal.canUse()) {
            helper.fail("re-enabling automatic targeting should restore the legacy range target goal");
            return;
        }

        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void guardGoalKeepsLocalCombatInsideGuardCommand(GameTestHelper helper) {
        Player player = helper.makeMockPlayer();
        BlockPos origin = helper.absolutePos(new BlockPos(1, 2, 1));
        LegacyShipEntity ship = createOwnedShip(helper, player, 53,
                origin.getX() + 0.5D, origin.getY(), origin.getZ() + 0.5D);
        Zombie target = new Zombie(EntityType.ZOMBIE, helper.getLevel());
        if (ship == null) {
            return;
        }

        target.setNoAi(true);
        target.setPos(origin.getX() + 2.0D, origin.getY(), origin.getZ() + 0.5D);
        helper.getLevel().addFreshEntity(target);
        ship.setUseAttackLight(true);
        ship.setUseAttackHeavy(false);
        ship.setUseAttackAirLight(false);
        ship.setUseAttackAirHeavy(false);
        BlockPos guardPos = origin.offset(8, 0, 8);
        ship.commandGuardBlock(guardPos, helper.getLevel().dimension().location().toString());

        LegacyShipGuardGoal goal = new LegacyShipGuardGoal(ship);
        if (!goal.canUse() || ship.canExecuteCombatGoal()) {
            helper.fail("guard commands should own navigation while exposing only the local guard combat goal");
            return;
        }
        ship.setCanFollow(true);
        if (goal.canUse()) {
            helper.fail("legacy guard AI should yield while CanFollow is enabled");
            return;
        }
        ship.setCanFollow(false);

        int ammoBefore = ship.getLightAmmo();
        goal.start();
        int guardAimTicks = Math.max(20, ship.getCompatAttackAimTime());

        ship.getNavigation().stop();
        for (int tick = 32; tick < 32 + guardAimTicks; tick++) {
            ship.tickCount = tick;
            goal.tick();
        }
        if (ship.getLightAmmo() != ammoBefore) {
            helper.fail("guard combat should not fire after the ship has reached its guard position");
            return;
        }

        ship.setAiAutoTarget(false);
        if (!goal.canUse()) {
            helper.fail("PassiveAI should keep the legacy guard movement goal active");
            return;
        }
        if (!setSyntheticGuardPath(ship, origin, guardPos)) {
            helper.fail("guard command should expose an active navigation path while returning to its position");
            return;
        }
        goal.stop();
        goal.start();
        for (int tick = 64; tick < 64 + guardAimTicks; tick++) {
            ship.tickCount = tick;
            goal.tick();
        }
        if (ship.getLightAmmo() != ammoBefore) {
            helper.fail("PassiveAI should suppress guard combat without cancelling guard movement");
            return;
        }

        ship.setAiAutoTarget(true);
        if (!setSyntheticGuardPath(ship, origin, guardPos)) {
            helper.fail("guard command should restart its navigation path for active guard combat");
            return;
        }
        goal.stop();
        goal.start();
        for (int tick = 96; tick < 96 + guardAimTicks; tick++) {
            ship.tickCount = tick;
            goal.tick();
        }
        if (ship.getLightAmmo() >= ammoBefore) {
            helper.fail("guard combat should fire at a nearby target without converting the guard into pursuit");
            return;
        }

        ship.clearCommandState();
        if (goal.canContinueToUse()) {
            helper.fail("clearing a guard command should stop the dedicated guard goal");
            return;
        }

        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void guardGoalKeepsIndependentCannonDelay(GameTestHelper helper) {
        Player player = helper.makeMockPlayer();
        BlockPos origin = helper.absolutePos(new BlockPos(1, 2, 1));
        LegacyShipEntity ship = createOwnedShip(helper, player, 53,
                origin.getX() + 0.5D, origin.getY(), origin.getZ() + 0.5D);
        Zombie target = EntityType.ZOMBIE.create(helper.getLevel());
        if (ship == null || target == null) {
            helper.fail("legacy guard cannon delay entities should be creatable");
            return;
        }

        target.setNoAi(true);
        target.setPos(origin.getX() + 2.0D, origin.getY(), origin.getZ() + 0.5D);
        helper.getLevel().addFreshEntity(target);
        ship.setNoAi(true);
        ship.setUseAttackLight(true);
        ship.setUseAttackHeavy(false);
        ship.setUseAttackAirLight(false);
        ship.setUseAttackAirHeavy(false);
        ship.setLightAmmo(40);
        ship.setGrudge(100);

        if (!ship.performPlayerCompatAttack(target, LegacyShipAttackKind.LIGHT)) {
            helper.fail("the setup cannon attack should establish the shared ranged cooldown");
            return;
        }
        int ammoAfterSetup = ship.getLightAmmo();
        int sharedCooldown = ship.getCompatAttackCooldown(LegacyShipAttackKind.LIGHT);
        if (sharedCooldown <= 0) {
            helper.fail("the setup cannon attack should leave a normal ranged cooldown");
            return;
        }

        BlockPos guardPos = origin.offset(8, 0, 8);
        ship.commandGuardBlock(guardPos, helper.getLevel().dimension().location().toString());
        LegacyShipGuardGoal goal = new LegacyShipGuardGoal(ship);
        if (!goal.canUse() || !setSyntheticGuardPath(ship, origin, guardPos)) {
            helper.fail("guard command should expose an active path for independent cannon delay testing");
            return;
        }

        goal.start();
        int guardAimTicks = Math.max(20, ship.getCompatAttackAimTime());
        for (int tick = 0; tick < guardAimTicks; tick++) {
            ship.tickCount = tick;
            goal.tick();
        }

        if (ship.getLightAmmo() != ammoAfterSetup - 1) {
            helper.fail("guard cannon should fire after its own delay and legacy aim time even while normal ranged cooldown is active");
            return;
        }
        if (ship.getCompatAttackCooldown(LegacyShipAttackKind.LIGHT) != sharedCooldown) {
            helper.fail("guard cannon should not consume or rewrite the normal ranged cooldown");
            return;
        }

        ship.discard();
        target.discard();
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void guardGoalKeepsIndependentAircraftDelay(GameTestHelper helper) {
        Player player = helper.makeMockPlayer();
        BlockPos origin = helper.absolutePos(new BlockPos(1, 2, 1));
        LegacyShipEntity carrier = createOwnedShip(helper, player, 49,
                origin.getX() + 0.5D, origin.getY(), origin.getZ() + 0.5D);
        Zombie target = EntityType.ZOMBIE.create(helper.getLevel());
        if (carrier == null || target == null) {
            helper.fail("legacy guard aircraft delay entities should be creatable");
            return;
        }

        target.setNoAi(true);
        target.setPos(origin.getX() + 6.5D, origin.getY(), origin.getZ() + 0.5D);
        helper.getLevel().addFreshEntity(target);
        carrier.setNoAi(true);
        carrier.setUseAttackMelee(false);
        carrier.setUseAttackLight(false);
        carrier.setUseAttackHeavy(false);
        carrier.setUseAttackAirLight(false);
        carrier.setUseAttackAirHeavy(true);
        carrier.setLightAmmo(100);
        carrier.setHeavyAmmo(100);
        carrier.setGrudge(100);
        carrier.setAircraftHeavy(Math.max(2, carrier.getMaxAircraftHeavy()));

        if (!carrier.performPlayerCompatAttack(target, LegacyShipAttackKind.AIR_HEAVY)) {
            helper.fail("the setup aircraft attack should establish the shared aircraft cooldown");
            return;
        }
        int launchedBeforeGuard = helper.getLevel().getEntitiesOfClass(
                LegacyShipAircraftEntity.class,
                carrier.getBoundingBox().inflate(20.0D),
                aircraft -> aircraft.getOwnerId() == carrier.getId()).size();
        int sharedCooldown = carrier.getCompatAttackCooldown(LegacyShipAttackKind.AIR_HEAVY);
        if (launchedBeforeGuard != 1 || sharedCooldown <= 0) {
            helper.fail("the setup aircraft attack should launch one aircraft and leave a shared cooldown");
            return;
        }

        BlockPos guardPos = origin.offset(8, 0, 8);
        carrier.commandGuardBlock(guardPos, helper.getLevel().dimension().location().toString());
        LegacyShipGuardGoal goal = new LegacyShipGuardGoal(carrier);
        if (!goal.canUse() || !setSyntheticGuardPath(carrier, origin, guardPos)) {
            helper.fail("guard command should expose an active path for independent aircraft delay testing");
            return;
        }

        goal.start();
        int guardAimTicks = Math.max(20, carrier.getCompatAttackAimTime());
        for (int tick = 0; tick < guardAimTicks; tick++) {
            carrier.tickCount = tick;
            goal.tick();
        }

        int launchedAfterGuard = helper.getLevel().getEntitiesOfClass(
                LegacyShipAircraftEntity.class,
                carrier.getBoundingBox().inflate(20.0D),
                aircraft -> aircraft.getOwnerId() == carrier.getId()).size();
        if (launchedAfterGuard != launchedBeforeGuard + 1) {
            helper.fail("guard aircraft should launch after its own delay and legacy aim time even while normal aircraft cooldown is active");
            return;
        }
        if (carrier.getCompatAttackCooldown(LegacyShipAttackKind.AIR_HEAVY) != sharedCooldown) {
            helper.fail("guard aircraft should not consume or rewrite the normal aircraft cooldown");
            return;
        }

        carrier.discard();
        target.discard();
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void ship02CommandBoundariesCoverFiveLegacyVariants(GameTestHelper helper) {
        Player player = helper.makeMockPlayer();
        if (!configureTeitoku(helper, player, data -> data.setPlayerUid(8122))) {
            return;
        }
        player.setPos(2.0D, 2.0D, 2.0D);

        int[] eggMetas = {12, 14, 15, 16, 17};
        LegacyShipEntity[] ships = new LegacyShipEntity[eggMetas.length];
        for (int index = 0; index < eggMetas.length; index++) {
            ships[index] = createOwnedShip(helper, player, eggMetas[index], 3.5D + index * 2.0D, 2.0D, 2.5D);
            if (ships[index] == null) {
                return;
            }

            BlockPos distantMoveTarget = helper.absolutePos(new BlockPos(1000 + index, 2, 1000 + index));
            if (ShipCommandService.handleResultForTesting(player,
                    ServerboundShipCommandPacket.moveTo(0, ships[index].getId(), ships[index].getShipUid(), distantMoveTarget))
                    != ShipCommandService.ShipCommandResult.SUCCESS
                    || !distantMoveTarget.equals(ships[index].getCommandedPos())) {
                helper.fail("legacy variant egg meta " + eggMetas[index]
                        + " should accept an old-style move coordinate when the ship is within commander range");
                return;
            }
            ships[index].clearCommandState();
        }

        ships[0].setPos(66.5D, 2.0D, 2.5D);
        if (ShipCommandService.handleResultForTesting(player,
                ServerboundShipCommandPacket.moveTo(0, ships[0].getId(), ships[0].getShipUid(), new BlockPos(6, 2, 6)))
                != ShipCommandService.ShipCommandResult.NO_COMMANDABLE_SHIP
                || ships[0].getCommandedPos() != null) {
            helper.fail("a ship at 64 or more blocks from its commander should reject the command like 1.12");
            return;
        }

        Zombie distantTarget = EntityType.ZOMBIE.create(helper.getLevel());
        if (distantTarget == null) {
            helper.fail("zombie should be creatable for distant target boundary test");
            return;
        }
        distantTarget.setPos(2.5D, 260.5D, 2.5D);
        helper.getLevel().addFreshEntity(distantTarget);
        ships[1].clearCommandState();
        if (ShipCommandService.handleResultForTesting(player,
                ServerboundShipCommandPacket.guard(0, ships[1].getId(), ships[1].getShipUid(), distantTarget.getId()))
                != ShipCommandService.ShipCommandResult.SUCCESS
                || !distantTarget.getUUID().equals(ships[1].getGuardEntityUuid())) {
            helper.fail("legacy guard commands should not add a modern player-to-target distance cap");
            return;
        }

        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void carrierAircraftSoundKeepsLegacyFriendlyOverrides(GameTestHelper helper) {
        ShipEntitySpec kaga = ShipEntitySpecs.getByEggMeta(49);
        ShipEntitySpec akagi = ShipEntitySpecs.getByEggMeta(50);
        ShipEntitySpec hostileKaga = ShipEntitySpecs.getByEggMeta(2049);
        ShipEntitySpec hostileAkagi = ShipEntitySpecs.getByEggMeta(2050);
        ShipEntitySpec wo = ShipEntitySpecs.getByEggMeta(14);

        if (!LegacyShipBehaviorCatalog.usesLegacyCarrierAircraftBowSound(kaga)
                || !LegacyShipBehaviorCatalog.usesLegacyCarrierAircraftBowSound(akagi)
                || LegacyShipBehaviorCatalog.usesLegacyCarrierAircraftBowSound(hostileKaga)
                || LegacyShipBehaviorCatalog.usesLegacyCarrierAircraftBowSound(hostileAkagi)
                || LegacyShipBehaviorCatalog.usesLegacyCarrierAircraftBowSound(wo)) {
            helper.fail("only friendly Kaga and Akagi should use the legacy bow launch sound");
            return;
        }

        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void shipWorldCacheReadsLegacyNestedRuntimeScalars(GameTestHelper helper) {
        CompoundTag entityTag = new CompoundTag();
        CompoundTag extended = new CompoundTag();
        CompoundTag minor = new CompoundTag();
        minor.putInt("Level", 7);
        minor.putInt("Morale", 4321);
        minor.putInt("NumAmmoL", 17);
        minor.putInt("NumAmmoH", 19);
        minor.putInt("NumGrudge", 23);
        extended.put("Minor", minor);
        entityTag.put("ShipExtProps", extended);

        ShipWorldCacheEntry entry = new ShipWorldCacheEntry(
                31, 17, "minecraft:overworld", 53, 53, 29, "LegacyOwner",
                false, false, 1, 2, 3, entityTag);
        if (entry.getShipLevel() != 7
                || entry.getMorale() != 4321
                || entry.getLightAmmo() != 17
                || entry.getHeavyAmmo() != 19
                || entry.getGrudge() != 23) {
            helper.fail("detached ship cache should read legacy ShipExtProps.Minor runtime scalars");
            return;
        }

        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void baseDestroyersApplyLegacyOwnerMarriageAuras(GameTestHelper helper) {
        ServerPlayer owner = makeServerBackedPlayer(helper, "BaseDestroyerAuraTester");
        helper.getLevel().players().add(owner);
        owner.setPos(2.0D, 2.0D, 2.0D);

        int[] eggMetas = {2, 3, 4, 5};
        MobEffect[] effects = {
                MobEffects.MOVEMENT_SPEED,
                MobEffects.DIG_SPEED,
                MobEffects.JUMP,
                MobEffects.DAMAGE_BOOST
        };
        int[] amplifiers = {2, 3, 3, 1};
        int level = 90;

        for (int index = 0; index < eggMetas.length; index++) {
            LegacyShipEntity ship = ModEntityTypes.LEGACY_SHIP.get().create(helper.getLevel());
            if (ship == null) {
                helper.fail("legacy ship entity should be creatable for base destroyer marriage aura tests");
                return;
            }

            ship.setVariantEggMeta(eggMetas[index]);
            ship.setOwner(owner);
            ship.setMarried(true);
            ship.setRingEffectEnabled(true);
            ship.setShipLevel(level);
            ship.setGrudge(200);
            ship.setPos(3.0D + index, 2.0D, 2.0D);
            helper.getLevel().addFreshEntity(ship);

            if (ship.isHostileVariant()) {
                helper.fail("base destroyer egg " + eggMetas[index] + " must remain a friendly 1.12 entity");
                return;
            }

            LegacyShipBehaviorCatalog.tickMarriagePassive(ship);
            MobEffectInstance applied = owner.getEffect(effects[index]);
            if (applied == null
                    || applied.getDuration() != 80 + level
                    || applied.getAmplifier() != amplifiers[index]) {
                helper.fail("base destroyer egg " + eggMetas[index]
                        + " should apply its legacy owner aura at the original duration and amplifier");
                return;
            }
            ship.discard();
        }

        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void carrierWoKeepsLegacyFriendlyAuraAndAircraftCaps(GameTestHelper helper) {
        ServerPlayer owner = makeServerBackedPlayer(helper, "CarrierWoAuraTester");
        helper.getLevel().players().add(owner);

        LegacyShipEntity carrier = ModEntityTypes.LEGACY_SHIP.get().create(helper.getLevel());
        LegacyShipEntity escort = ModEntityTypes.LEGACY_SHIP.get().create(helper.getLevel());
        LegacyShipEntity otherOwner = ModEntityTypes.LEGACY_SHIP.get().create(helper.getLevel());
        if (carrier == null || escort == null || otherOwner == null) {
            helper.fail("carrier Wo and escort ships should be creatable for legacy aura tests");
            return;
        }

        String ownerName = owner.getGameProfile().getName();
        carrier.setVariantEggMeta(14);
        carrier.setOwner(owner.getUUID(), ownerName, 91);
        carrier.setMarried(true);
        carrier.setRingEffectEnabled(true);
        carrier.setShipLevel(80);
        carrier.setGrudge(200);
        carrier.setPos(2.0D, 2.0D, 2.0D);

        escort.setVariantEggMeta(2);
        escort.setOwner(owner.getUUID(), ownerName, 91);
        escort.setPos(5.0D, 2.0D, 2.0D);

        otherOwner.setVariantEggMeta(2);
        otherOwner.setOwner(UUID.randomUUID(), "OtherCarrierOwner", 999);
        otherOwner.setPos(6.0D, 2.0D, 2.0D);

        helper.getLevel().addFreshEntity(carrier);
        helper.getLevel().addFreshEntity(escort);
        helper.getLevel().addFreshEntity(otherOwner);

        if (carrier.isHostileVariant()
                || carrier.getMaxAircraftLight() != LegacyShipBehaviorCatalog.aircraftLightCapacityFor(
                        carrier.getSpec(), 80, 0)
                || carrier.getMaxAircraftHeavy() != LegacyShipBehaviorCatalog.aircraftHeavyCapacityFor(
                        carrier.getSpec(), 80, 0)
                || carrier.getMaxAircraftLight() != 44
                || carrier.getMaxAircraftHeavy() != 24) {
            helper.fail("Carrier Wo should remain friendly and keep its legacy level-based aircraft capacity additions");
            return;
        }

        carrier.tickCount = 128;
        LegacyShipBehaviorCatalog.tickLegacyPerShipBehavior(carrier);
        MobEffectInstance carrierStrength = carrier.getEffect(MobEffects.DAMAGE_BOOST);
        MobEffectInstance escortStrength = escort.getEffect(MobEffects.DAMAGE_BOOST);
        if (carrierStrength == null || escortStrength == null
                || carrierStrength.getDuration() != 110
                || escortStrength.getDuration() != 110
                || carrierStrength.getAmplifier() != 1
                || escortStrength.getAmplifier() != 1
                || otherOwner.hasEffect(MobEffects.DAMAGE_BOOST)) {
            helper.fail("Carrier Wo should apply its legacy strength aura only to same-owner ships within range");
            return;
        }

        carrier.removeEffect(MobEffects.DAMAGE_BOOST);
        escort.removeEffect(MobEffects.DAMAGE_BOOST);
        carrier.setRingEffectEnabled(false);
        carrier.tickCount = 256;
        LegacyShipBehaviorCatalog.tickLegacyPerShipBehavior(carrier);
        if (carrier.hasEffect(MobEffects.DAMAGE_BOOST) || escort.hasEffect(MobEffects.DAMAGE_BOOST)) {
            helper.fail("Carrier Wo ring toggle should suppress its legacy strength aura");
            return;
        }

        carrier.discard();
        escort.discard();
        otherOwner.discard();
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void battleshipTaKeepsLegacySameOwnerSpeedAura(GameTestHelper helper) {
        LegacyShipEntity battleshipTa = ModEntityTypes.LEGACY_SHIP.get().create(helper.getLevel());
        LegacyShipEntity escort = ModEntityTypes.LEGACY_SHIP.get().create(helper.getLevel());
        LegacyShipEntity otherOwner = ModEntityTypes.LEGACY_SHIP.get().create(helper.getLevel());
        if (battleshipTa == null || escort == null || otherOwner == null) {
            helper.fail("Battleship Ta and escort ships should be creatable for legacy aura tests");
            return;
        }

        UUID ownerUuid = UUID.randomUUID();
        battleshipTa.setVariantEggMeta(16);
        battleshipTa.setOwner(ownerUuid, "BattleshipTaAuraTester", 92);
        battleshipTa.setMarried(true);
        battleshipTa.setRingEffectEnabled(true);
        battleshipTa.setShipLevel(80);
        battleshipTa.setGrudge(200);
        battleshipTa.setPos(2.0D, 2.0D, 2.0D);

        escort.setVariantEggMeta(2);
        escort.setOwner(ownerUuid, "BattleshipTaAuraTester", 92);
        escort.setPos(5.0D, 2.0D, 2.0D);

        otherOwner.setVariantEggMeta(2);
        otherOwner.setOwner(UUID.randomUUID(), "OtherBattleshipTaOwner", 993);
        otherOwner.setPos(6.0D, 2.0D, 2.0D);

        helper.getLevel().addFreshEntity(battleshipTa);
        helper.getLevel().addFreshEntity(escort);
        helper.getLevel().addFreshEntity(otherOwner);

        battleshipTa.tickCount = 128;
        LegacyShipBehaviorCatalog.tickLegacyPerShipBehavior(battleshipTa);
        MobEffectInstance taSpeed = battleshipTa.getEffect(MobEffects.MOVEMENT_SPEED);
        MobEffectInstance escortSpeed = escort.getEffect(MobEffects.MOVEMENT_SPEED);
        if (taSpeed == null || escortSpeed == null
                || taSpeed.getDuration() != 130
                || escortSpeed.getDuration() != 130
                || taSpeed.getAmplifier() != 1
                || escortSpeed.getAmplifier() != 1
                || otherOwner.hasEffect(MobEffects.MOVEMENT_SPEED)) {
            helper.fail("Battleship Ta should apply its legacy speed aura only to same-owner ships within range");
            return;
        }

        battleshipTa.removeEffect(MobEffects.MOVEMENT_SPEED);
        escort.removeEffect(MobEffects.MOVEMENT_SPEED);
        battleshipTa.setRingEffectEnabled(false);
        battleshipTa.tickCount = 256;
        LegacyShipBehaviorCatalog.tickLegacyPerShipBehavior(battleshipTa);
        if (battleshipTa.hasEffect(MobEffects.MOVEMENT_SPEED)
                || escort.hasEffect(MobEffects.MOVEMENT_SPEED)) {
            helper.fail("Battleship Ta ring toggle should suppress its legacy speed aura");
            return;
        }

        battleshipTa.discard();
        escort.discard();
        otherOwner.discard();
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void legacyMarriageAurasStayOwnerScoped(GameTestHelper helper) {
        LegacyShipEntity nagato = ModEntityTypes.LEGACY_SHIP.get().create(helper.getLevel());
        LegacyShipEntity sameOwner = ModEntityTypes.LEGACY_SHIP.get().create(helper.getLevel());
        LegacyShipEntity alliedOwner = ModEntityTypes.LEGACY_SHIP.get().create(helper.getLevel());
        if (nagato == null || sameOwner == null || alliedOwner == null) {
            helper.fail("Nagato and escort ships should be creatable for owner-scoped aura tests");
            return;
        }

        int ownerTeam = 18201;
        int allyTeam = 18202;
        TeamSavedData teams = TeamSavedData.get(helper.getLevel());
        teams.createTeam(ownerTeam, "OwnerScopedAura", "Owner Scoped Aura");
        teams.createTeam(allyTeam, "AllyScopedAura", "Ally Scoped Aura");
        teams.addAllyRelationship(ownerTeam, allyTeam);
        if (teams.getTeam(ownerTeam) == null || !teams.getTeam(ownerTeam).isAlly(allyTeam)) {
            helper.fail("owner-scoped aura test should expose a legacy ally relation");
            return;
        }

        UUID ownerUuid = UUID.randomUUID();
        UUID allyUuid = UUID.randomUUID();
        nagato.setVariantEggMeta(39);
        nagato.setOwner(ownerUuid, "OwnerScopedAura", ownerTeam);
        nagato.setMarried(true);
        nagato.setRingEffectEnabled(true);
        nagato.setShipLevel(70);
        nagato.setGrudge(200);
        nagato.setPos(2.0D, 2.0D, 2.0D);

        sameOwner.setVariantEggMeta(2);
        sameOwner.setOwner(ownerUuid, "OwnerScopedAura", ownerTeam);
        sameOwner.setPos(5.0D, 2.0D, 2.0D);

        alliedOwner.setVariantEggMeta(2);
        alliedOwner.setOwner(allyUuid, "AllyScopedAura", allyTeam);
        alliedOwner.setPos(6.0D, 2.0D, 2.0D);

        helper.getLevel().addFreshEntity(nagato);
        helper.getLevel().addFreshEntity(sameOwner);
        helper.getLevel().addFreshEntity(alliedOwner);

        if (!nagato.isAlliedTo(alliedOwner)) {
            helper.fail("test setup should expose the configured ally relation to modern entity diplomacy");
            return;
        }

        LegacyShipBehaviorCatalog.tickMarriagePassive(nagato);
        MobEffectInstance nagatoStrength = nagato.getEffect(MobEffects.DAMAGE_BOOST);
        MobEffectInstance sameOwnerStrength = sameOwner.getEffect(MobEffects.DAMAGE_BOOST);
        if (nagatoStrength == null || sameOwnerStrength == null
                || nagatoStrength.getDuration() != 120
                || sameOwnerStrength.getDuration() != 120
                || nagatoStrength.getAmplifier() != 1
                || sameOwnerStrength.getAmplifier() != 1
                || alliedOwner.hasEffect(MobEffects.DAMAGE_BOOST)) {
            helper.fail("legacy marriage strength aura should affect same-owner ships but exclude allied owners");
            return;
        }

        nagato.discard();
        sameOwner.discard();
        alliedOwner.discard();
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void heavyCruisersKeepLegacyNightEffectsAndNePush(GameTestHelper helper) {
        LegacyShipEntity ri = ModEntityTypes.LEGACY_SHIP.get().create(helper.getLevel());
        LegacyShipEntity ne = ModEntityTypes.LEGACY_SHIP.get().create(helper.getLevel());
        Zombie target = EntityType.ZOMBIE.create(helper.getLevel());
        if (ri == null || ne == null || target == null) {
            helper.fail("heavy cruisers and push target should be creatable for legacy behavior tests");
            return;
        }

        helper.getLevel().setDayTime(18000L);
        ri.setVariantEggMeta(11);
        ne.setVariantEggMeta(12);
        ri.setMorale(3000);
        ne.setMorale(3000);
        ri.setGrudge(100);
        ne.setGrudge(100);
        ri.setShipLevel(90);
        ne.setShipLevel(90);
        ri.setRingEffectEnabled(true);
        ne.setRingEffectEnabled(true);
        helper.getLevel().setDayTime(18000L);
        helper.getLevel().updateSkyBrightness();
        ri.setPos(1.5D, 2.0D, 1.5D);
        ne.setPos(8.5D, 2.0D, 1.5D);
        helper.getLevel().addFreshEntity(ri);
        helper.getLevel().addFreshEntity(ne);

        if (ri.isHostileVariant() || ne.isHostileVariant()) {
            helper.fail("heavy cruiser direct eggs must remain friendly BasicEntityShip variants");
            return;
        }

        LegacyShipStats riDay = LegacyShipStats.create(
                9, ShipArchetype.CRUISER, false, 90, 3000,
                0, 0, 0, 0, 0, 0, false,
                0, ShipEquipmentProfile.EMPTY, List.of(), false);
        LegacyShipStats riNight = LegacyShipStats.create(
                9, ShipArchetype.CRUISER, false, 90, 3000,
                0, 0, 0, 0, 0, 0, false,
                0, ShipEquipmentProfile.EMPTY, List.of(), true);
        LegacyShipStats neDay = LegacyShipStats.create(
                10, ShipArchetype.CRUISER, false, 90, 3000,
                0, 0, 0, 0, 0, 0, false,
                0, ShipEquipmentProfile.EMPTY, List.of(), false);
        LegacyShipStats neNight = LegacyShipStats.create(
                10, ShipArchetype.CRUISER, false, 90, 3000,
                0, 0, 0, 0, 0, 0, false,
                0, ShipEquipmentProfile.EMPTY, List.of(), true);
        if (Math.abs(riNight.critical() - riDay.critical() - 0.15F) > 0.0001F
                || Math.abs(riNight.missReduce() - riDay.missReduce() - 0.15F) > 0.0001F
                || Math.abs(neNight.critical() - neDay.critical() - 0.3F) > 0.0001F
                || ri.getLegacyStats().critical() <= riDay.critical()
                || ne.getLegacyStats().critical() <= neDay.critical()) {
            helper.fail("Ri/Ne should keep the legacy night critical and miss bonuses");
            return;
        }

        ri.tickCount = 128;
        ne.tickCount = 128;
        LegacyShipBehaviorCatalog.tickLegacyPerShipBehavior(ri);
        LegacyShipBehaviorCatalog.tickLegacyPerShipBehavior(ne);
        MobEffectInstance riSpeed = ri.getEffect(MobEffects.MOVEMENT_SPEED);
        MobEffectInstance riJump = ri.getEffect(MobEffects.JUMP);
        MobEffectInstance neSpeed = ne.getEffect(MobEffects.MOVEMENT_SPEED);
        MobEffectInstance neJump = ne.getEffect(MobEffects.JUMP);
        if (riSpeed == null || riJump == null || neSpeed == null || neJump == null
                || riSpeed.getDuration() != 150 || riJump.getDuration() != 150
                || neSpeed.getDuration() != 150 || neJump.getDuration() != 150
                || riSpeed.getAmplifier() != 1 || riJump.getAmplifier() != 1
                || neSpeed.getAmplifier() != 1 || neJump.getAmplifier() != 1) {
            helper.fail("Ri/Ne should apply the legacy 150-tick nighttime ring effects");
            return;
        }

        target.setNoAi(true);
        target.setPos(ne.getX(), ne.getY(), ne.getZ() + 2.0D);
        helper.getLevel().addFreshEntity(target);
        ne.beginLegacyRePush(target);
        ne.tickCount = 257;
        target.setDeltaMovement(Vec3.ZERO);
        LegacyShipBehaviorCatalog.tickLegacyPerShipBehavior(ne);
        Vec3 motion = target.getDeltaMovement();
        if (ne.isLegacyRePushActive() || motion.y < 0.49D || Math.abs(motion.z) < 0.49D) {
            helper.fail("Ne should push its selected target with the legacy velocity");
            return;
        }

        ri.discard();
        ne.discard();
        target.discard();
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void firstDestroyersAndRiKeepLegacyConstructorHooks(GameTestHelper helper) {
        int[] destroyerEggMetas = {2, 3, 4, 5};
        int[] destroyerClassIds = {0, 1, 2, 3};
        double[] sittingFactors = {0.51D, 0.28D, 0.27D, 0.12D};
        double[] standingFactors = {0.6D, 0.6D, 0.7D, 0.77D};

        for (int index = 0; index < destroyerEggMetas.length; index++) {
            LegacyShipEntity ship = ModEntityTypes.LEGACY_SHIP.get().create(helper.getLevel());
            if (ship == null) {
                helper.fail("legacy destroyer should be creatable for constructor hook coverage");
                return;
            }

            ship.setVariantEggMeta(destroyerEggMetas[index]);
            LegacyShipAttackProfile profile = ship.getAttackProfile();
            if (ship.getShipClassId() != destroyerClassIds[index]
                    || !profile.light() || !profile.heavy() || profile.hasAirAttack()
                    || !LegacyShipBehaviorCatalog.supportsItemPickup(ship.getSpec())
                    || LegacyShipBehaviorCatalog.pickupRangeBaseFor(ship.getSpec()) != 4
                    || LegacyShipBehaviorCatalog.ammoConsumptionFor(ship.getSpec()) != 1) {
                helper.fail("legacy destroyer " + destroyerEggMetas[index]
                        + " should keep its cannon, pickup, and ammunition hooks");
                return;
            }

            ship.setOrderedToSit(true);
            double sittingOffset = ship.getPassengersRidingOffset();
            ship.setOrderedToSit(false);
            double standingOffset = ship.getPassengersRidingOffset();
            if (Math.abs(sittingOffset - ship.getBbHeight() * sittingFactors[index]) > 1.0E-6D
                    || Math.abs(standingOffset - ship.getBbHeight() * standingFactors[index]) > 1.0E-6D) {
                helper.fail("legacy destroyer " + destroyerEggMetas[index]
                        + " should keep its exact mounted offsets");
                return;
            }
            ship.discard();
        }

        LegacyShipEntity ri = ModEntityTypes.LEGACY_SHIP.get().create(helper.getLevel());
        if (ri == null) {
            helper.fail("EntityCARi should be creatable for constructor hook coverage");
            return;
        }

        ri.setVariantEggMeta(11);
        LegacyShipAttackProfile riProfile = ri.getAttackProfile();
        if (ri.getShipClassId() != 9
                || !riProfile.light() || !riProfile.heavy() || riProfile.hasAirAttack()
                || LegacyShipBehaviorCatalog.supportsItemPickup(ri.getSpec())
                || LegacyShipBehaviorCatalog.ammoConsumptionFor(ri.getSpec()) != 2
                || ri.getFoodSaturationMax() != 14) {
            helper.fail("EntityCARi should keep the legacy heavy-cruiser equipment and supply hooks");
            return;
        }

        ri.setOrderedToSit(true);
        ri.setEmotion(0);
        double riSittingOffset = ri.getPassengersRidingOffset();
        ri.setEmotion(4);
        double riBoredOffset = ri.getPassengersRidingOffset();
        ri.setOrderedToSit(false);
        double riStandingOffset = ri.getPassengersRidingOffset();
        if (Math.abs(riSittingOffset - ri.getBbHeight() * 0.33D) > 1.0E-6D
                || Math.abs(riBoredOffset - ri.getBbHeight() * 0.22D) > 1.0E-6D
                || Math.abs(riStandingOffset - ri.getBbHeight() * 0.72D) > 1.0E-6D) {
            helper.fail("EntityCARi should keep its legacy normal, bored, and standing mounted offsets");
            return;
        }

        ri.discard();
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void firstDestroyersAndRiKeepLegacyCombatCadenceAndSaveState(GameTestHelper helper) {
        int[] eggMetas = {2, 3, 4, 5, 11};
        int[] classIds = {0, 1, 2, 3, 9};
        ServerPlayer owner = makeServerBackedPlayer(helper, "Ship01CombatState");

        for (int index = 0; index < eggMetas.length; index++) {
            LegacyShipEntity ship = createOwnedShip(helper, owner, eggMetas[index],
                    2.5D + index * 3.0D, 2.0D, 2.5D);
            if (ship == null) {
                return;
            }

            float attackSpeed = Math.max(0.01F, ship.getLegacyStats().attackSpeed());
            int expectedLightDelay = (int) (80F / attackSpeed);
            int expectedHeavyDelay = (int) (120F / attackSpeed) + 50;
            if (ship.getShipClassId() != classIds[index]
                    || ship.getCompatAttackMaxCooldown(LegacyShipAttackKind.LIGHT) != expectedLightDelay + 20
                    || ship.getCompatAttackMaxCooldown(LegacyShipAttackKind.HEAVY) != expectedHeavyDelay
                    || LegacyShipBehaviorCatalog.legacyIdleGrudgeConsumptionFor(ship.getSpec())
                    != (index == 4 ? 8 : 5)) {
                helper.fail("SHIP-01 cadence or idle grudge mapping diverged for class " + classIds[index]
                        + ": actualClass=" + ship.getShipClassId()
                        + ", light=" + ship.getCompatAttackMaxCooldown(LegacyShipAttackKind.LIGHT)
                        + "/" + (expectedLightDelay + 20)
                        + ", heavy=" + ship.getCompatAttackMaxCooldown(LegacyShipAttackKind.HEAVY)
                        + "/" + expectedHeavyDelay
                        + ", idle=" + LegacyShipBehaviorCatalog.legacyIdleGrudgeConsumptionFor(ship.getSpec()));
                return;
            }

            ship.setNoAi(true);
            ship.setLightAmmo(100);
            ship.setHeavyAmmo(100);
            ship.setGrudge(100);
            ship.setMorale(5000);
            Zombie target = EntityType.ZOMBIE.create(helper.getLevel());
            if (target == null) {
                helper.fail("SHIP-01 should create a combat target");
                return;
            }
            target.setNoAi(true);
            target.setPos(ship.getX(), ship.getY(), ship.getZ() + 2.0D);
            helper.getLevel().addFreshEntity(target);

            int ammoBefore = ship.getLightAmmo();
            if (!ship.performPlayerCompatAttack(target, LegacyShipAttackKind.LIGHT)) {
                helper.fail("class " + classIds[index] + " should accept a valid light attack");
                return;
            }

            int expectedAmmoCost = LegacyShipBehaviorCatalog.attackAmmoCostFor(
                    ship.getSpec(), LegacyShipAttackKind.LIGHT);
            if (ship.getLightAmmo() != ammoBefore - expectedAmmoCost
                    || ship.getGrudge() != 96
                    || ship.getMorale() != 4996
                    || ship.getCompatAttackCooldown(LegacyShipAttackKind.LIGHT)
                    != ship.getCompatAttackMaxCooldown(LegacyShipAttackKind.LIGHT)) {
                helper.fail("class " + classIds[index] + " should keep legacy light attack resource costs");
                return;
            }

            CompoundTag saved = new CompoundTag();
            ship.addAdditionalSaveData(saved);
            LegacyShipEntity loaded = ModEntityTypes.LEGACY_SHIP.get().create(helper.getLevel());
            if (loaded == null) {
                helper.fail("SHIP-01 should create a save-state restore target");
                return;
            }
            loaded.readAdditionalSaveData(saved);
            if (loaded.getShipClassId() != classIds[index]
                    || loaded.getLightAmmo() != ammoBefore - expectedAmmoCost
                    || loaded.getGrudge() != 96
                    || loaded.getMorale() != 4996) {
                helper.fail("class " + classIds[index] + " should restore combat resources from NBT");
                return;
            }

            loaded.discard();
            target.discard();
            ship.discard();
        }

        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void firstDestroyersAndRiKeepLegacyIdleSupplyAccounting(GameTestHelper helper) {
        ServerPlayer owner = makeServerBackedPlayer(helper, "Ship01IdleSupply");
        LegacyShipEntity destroyer = createOwnedShip(helper, owner, 2, 2.5D, 2.0D, 2.5D);
        if (destroyer == null) {
            return;
        }

        destroyer.setNoAi(true);
        destroyer.setNoGravity(true);
        destroyer.setInvulnerable(true);
        destroyer.setAiAutoSupply(false);
        destroyer.setLightAmmo(0);
        destroyer.setHeavyAmmo(0);
        destroyer.setGrudge(1000);
        destroyer.setMorale(3900);
        destroyer.getShipInventory().setItem(LegacyShipEntity.EQUIPMENT_SLOT_COUNT,
                new ItemStack(ModItems.AMMO.get()));
        destroyer.getShipInventory().setItem(LegacyShipEntity.EQUIPMENT_SLOT_COUNT + 1,
                new ItemStack(ModItems.AMMO2.get()));

        destroyer.tickCount = 128;
        destroyer.tick();
        if (destroyer.getLightAmmo() != 30
                || destroyer.getHeavyAmmo() != 15
                || destroyer.getGrudge() != 995
                || !destroyer.getShipInventory().getItem(LegacyShipEntity.EQUIPMENT_SLOT_COUNT).isEmpty()
                || !destroyer.getShipInventory().getItem(LegacyShipEntity.EQUIPMENT_SLOT_COUNT + 1).isEmpty()) {
            helper.fail("destroyer should reload one light and one heavy supply and pay idle grudge");
            return;
        }

        destroyer.setPos(destroyer.getX() + 4.0D, destroyer.getY(), destroyer.getZ());
        destroyer.tickCount = 256;
        destroyer.tick();
        if (destroyer.getGrudge() != 978 || destroyer.getMorale() != 3875) {
            helper.fail("destroyer should pay legacy movement grudge and cumulative clamped morale cost: grudge="
                    + destroyer.getGrudge() + ", morale=" + destroyer.getMorale());
            return;
        }

        LegacyShipEntity ri = createOwnedShip(helper, owner, 11, 8.5D, 2.0D, 2.5D);
        if (ri == null) {
            return;
        }
        ri.setNoAi(true);
        ri.setAiAutoSupply(false);
        ri.setLightAmmo(0);
        ri.setHeavyAmmo(0);
        ri.setGrudge(1000);
        ri.setMorale(3900);
        ri.getShipInventory().setItem(LegacyShipEntity.EQUIPMENT_SLOT_COUNT,
                new ItemStack(ModItems.AMMO.get()));
        ri.getShipInventory().setItem(LegacyShipEntity.EQUIPMENT_SLOT_COUNT + 1,
                new ItemStack(ModItems.AMMO2.get()));
        ri.tickCount = 128;
        ri.tick();
        if (ri.getLightAmmo() != 30
                || ri.getHeavyAmmo() != 15
                || ri.getGrudge() != 992) {
            helper.fail("Ri should keep its legacy heavy-cruiser idle grudge and supply costs");
            return;
        }

        LegacyShipEntity normalGrudge = createOwnedShip(helper, owner, 2, 2.5D, 2.0D, 5.5D);
        if (normalGrudge == null) {
            return;
        }
        normalGrudge.setNoAi(true);
        normalGrudge.setAiAutoSupply(false);
        normalGrudge.setLightAmmo(100);
        normalGrudge.setHeavyAmmo(100);
        normalGrudge.setGrudge(0);
        normalGrudge.getShipInventory().setItem(LegacyShipEntity.EQUIPMENT_SLOT_COUNT,
                new ItemStack(ModItems.GRUDGE.get()));
        int expectedNormalGrudge = (int) (300F
                * normalGrudge.getLegacyStats().get(LegacyShipStatTables.Attr.GRUDGE));
        normalGrudge.tickCount = 128;
        normalGrudge.tick();
        if (normalGrudge.getGrudge() != expectedNormalGrudge
                || !normalGrudge.getShipInventory().getItem(LegacyShipEntity.EQUIPMENT_SLOT_COUNT).isEmpty()) {
            helper.fail("legacy idle grudge recovery must consume one normal grudge item");
            return;
        }

        LegacyShipEntity blockGrudge = createOwnedShip(helper, owner, 2, 8.5D, 2.0D, 5.5D);
        if (blockGrudge == null) {
            return;
        }
        blockGrudge.setNoAi(true);
        blockGrudge.setAiAutoSupply(false);
        blockGrudge.setLightAmmo(100);
        blockGrudge.setHeavyAmmo(100);
        blockGrudge.setGrudge(0);
        blockGrudge.getShipInventory().setItem(LegacyShipEntity.EQUIPMENT_SLOT_COUNT,
                new ItemStack(ModBlocks.BLOCK_GRUDGE.get()));
        int expectedBlockGrudge = (int) (2700F
                * blockGrudge.getLegacyStats().get(LegacyShipStatTables.Attr.GRUDGE));
        blockGrudge.tickCount = 128;
        blockGrudge.tick();
        if (blockGrudge.getGrudge() != expectedBlockGrudge
                || !blockGrudge.getShipInventory().getItem(LegacyShipEntity.EQUIPMENT_SLOT_COUNT).isEmpty()) {
            helper.fail("legacy idle grudge recovery must consume one grudge block item");
            return;
        }

        ri.discard();
        destroyer.discard();
        normalGrudge.discard();
        blockGrudge.discard();
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void shipCoreRestoresLegacyNoFuelAndConfiguredSupplies(GameTestHelper helper) {
        LegacyShipEntity ship = ModEntityTypes.LEGACY_SHIP.get().create(helper.getLevel());
        if (ship == null) {
            helper.fail("无法创建舰娘");
            return;
        }
        ship.setVariantEggMeta(2);
        ship.setNoAi(true);
        if (ship.getGrudge() != 0 || ship.getLightAmmo() != 0 || ship.getHeavyAmmo() != 0
                || ship.getMorale() != 60) {
            helper.fail("新舰娘必须从零怨念、零弹药和 60 士气开始");
            return;
        }
        ship.setGrudge(40_000);
        ship.setLightAmmo(10_000);
        ship.setHeavyAmmo(10_000);
        if (ship.getGrudge() != 40_000 || ship.getLightAmmo() != 10_000 || ship.getHeavyAmmo() != 10_000) {
            helper.fail("舰娘怨念和弹药不得受移植版上限限制");
            return;
        }
        ship.addEffect(new MobEffectInstance(MobEffects.HUNGER, 100, 1));
        ship.tickCount = 128;
        ship.tick();
        if (ship.getGrudge() != 39_975) {
            helper.fail("二级饥饿必须使待机怨念消耗变为五倍");
            return;
        }
        ship.setGrudge(1);
        Zombie target = EntityType.ZOMBIE.create(helper.getLevel());
        if (target == null) {
            helper.fail("无法创建断粮前的攻击目标");
            return;
        }
        ship.setTarget(target);
        ship.tickCount = 256;
        ship.tick();
        if (!ship.hasNoFuel() || ship.getMorale() != 0 || ship.getTarget() != null) {
            helper.fail("怨念耗尽后必须断粮并清除士气及目标");
            return;
        }
        ship.setLightAmmo(0);
        ship.setHeavyAmmo(0);
        int cargo = LegacyShipEntity.EQUIPMENT_SLOT_COUNT;
        ship.getShipInventory().setItem(cargo, new ItemStack(ModBlocks.BLOCK_GRUDGE.get(), 2));
        ship.getShipInventory().setItem(cargo + 1, new ItemStack(ModItems.AMMO.get(), 2));
        ship.getShipInventory().setItem(cargo + 2, new ItemStack(ModItems.AMMO3.get(), 2));
        try {
            ShinColleConfig.setTestOverride("Item_Grudge", 400);
            ShinColleConfig.setTestOverride("Item_LightAmmo", 40);
            ShinColleConfig.setTestOverride("Item_HeavyAmmo", 20);
            int grudge = (int) (3600 * ship.getLegacyStats().get(LegacyShipStatTables.Attr.GRUDGE));
            int light = (int) (40 * ship.getLegacyStats().get(LegacyShipStatTables.Attr.AMMO));
            int heavy = (int) (180 * ship.getLegacyStats().get(LegacyShipStatTables.Attr.AMMO));
            ship.tickCount = 384;
            ship.tick();
            if (ship.hasNoFuel() || ship.getGrudge() != grudge || ship.getLightAmmo() != light
                    || ship.getHeavyAmmo() != heavy
                    || ship.getShipInventory().getItem(cargo).getCount() != 1
                    || ship.getShipInventory().getItem(cargo + 1).getCount() != 1
                    || ship.getShipInventory().getItem(cargo + 2).getCount() != 1) {
                helper.fail("补给必须按配置补充怨念与弹药，各消耗一件，并解除断粮状态");
                return;
            }
        } finally {
            ShinColleConfig.clearTestOverrides();
            ship.discard();
        }
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void nextFiveShipClassesKeepLegacyEquipmentBoundaries(GameTestHelper helper) {
        ShipEntitySpec[] specs = {
                ShipEntitySpecs.getByEggMeta(12),
                ShipEntitySpecs.getByEggMeta(14),
                ShipEntitySpecs.getByEggMeta(16),
                ShipEntitySpecs.getByEggMeta(17),
                ShipEntitySpecs.getByEggMeta(18)
        };
        int[] expectedEquipmentTypes = {1, 3, 1, 2, 1};
        ItemStack cannon = new ItemStack(ModItems.EQUIPCANNON_ITEMS.get(0).get());
        ItemStack radar = new ItemStack(ModItems.EQUIPRADAR_ITEMS.get(0).get());
        ItemStack airplane = new ItemStack(ModItems.EQUIPAIRPLANE_ITEMS.get(0).get());

        for (int index = 0; index < specs.length; index++) {
            ShipEntitySpec spec = specs[index];
            if (LegacyShipBehaviorCatalog.equipmentTypeFor(spec) != expectedEquipmentTypes[index]
                    || !ShipEquipmentProfile.canEquip(spec, radar)) {
                helper.fail("legacy equipment type or misc equipment acceptance changed for egg meta "
                        + spec.eggMeta());
                return;
            }

            boolean cannonAllowed = ShipEquipmentProfile.canEquip(spec, cannon);
            boolean airplaneAllowed = ShipEquipmentProfile.canEquip(spec, airplane);
            boolean expectedCannon = expectedEquipmentTypes[index] != 3;
            boolean expectedAirplane = expectedEquipmentTypes[index] != 1;
            if (cannonAllowed != expectedCannon || airplaneAllowed != expectedAirplane) {
                helper.fail("legacy cannon/aircraft equipment boundary changed for egg meta "
                        + spec.eggMeta());
                return;
            }
        }

        SimpleContainer reInventory = new SimpleContainer(LegacyShipEntity.SHIP_SLOT_COUNT);
        reInventory.setItem(0, airplane);
        ShipEquipmentProfile reProfile = ShipEquipmentProfile.fromInventory(
                reInventory, ShipEntitySpecs.getByEggMeta(17));
        if (reProfile.equippedCount() != 1 || reProfile.airFirepower() <= 0.0F) {
            helper.fail("Battleship Re should apply legacy aircraft equipment through equip type 2");
            return;
        }

        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void thirdFiveHimeClassesKeepLegacyBehaviorBoundaries(GameTestHelper helper) {
        int[] eggMetas = {22, 23, 28, 29, 30};
        int[] classIds = {20, 21, 26, 27, 28};
        int[] equipmentTypes = {3, 2, 1, 1, 2};
        int[] foodMaxima = {24, 16, 30, 10, 22};
        boolean[][] attackChannels = {
                {false, false, true}, {true, true, true}, {true, true, false},
                {true, true, false}, {true, true, true}
        };
        float[] meleeMultipliers = {16.0F, 8.0F, 24.0F, 8.0F, 8.0F};

        for (int index = 0; index < eggMetas.length; index++) {
            ShipEntitySpec spec = ShipEntitySpecs.getByEggMeta(eggMetas[index]);
            LegacyShipAttackProfile profile = LegacyShipBehaviorCatalog.attackProfile(spec);
            boolean pickup = LegacyShipBehaviorCatalog.supportsItemPickup(spec);
            if (spec.legacyClassId() != classIds[index]
                    || LegacyShipBehaviorCatalog.equipmentTypeFor(spec) != equipmentTypes[index]
                    || LegacyShipBehaviorCatalog.foodSaturationMaxFor(spec) != foodMaxima[index]
                    || profile.light() != attackChannels[index][0]
                    || profile.heavy() != attackChannels[index][1]
                    || profile.hasAirAttack() != attackChannels[index][2]
                    || pickup != (classIds[index] == 27)
                    || (pickup && LegacyShipBehaviorCatalog.pickupRangeBaseFor(spec) != 4)
                    || LegacyShipBehaviorCatalog.meleeDamageMultiplierFor(classIds[index], true)
                    != meleeMultipliers[index]) {
                helper.fail("hime egg " + eggMetas[index]
                        + " should keep its legacy equipment, attack, supply, pickup, and melee boundaries");
                return;
            }
        }

        ShipEntitySpec battleshipHime = ShipEntitySpecs.getByEggMeta(28);
        ShipEntitySpec harbourHime = ShipEntitySpecs.getByEggMeta(30);
        if (!LegacyShipBehaviorCatalog.attackProfile(battleshipHime)
                .projectileProfile(LegacyShipAttackKind.HEAVY).isPresent()
                || !LegacyShipBehaviorCatalog.attackProfile(harbourHime)
                .projectileProfile(LegacyShipAttackKind.HEAVY).isPresent()
                || LegacyShipBehaviorCatalog.attackProfile(battleshipHime)
                .projectileProfile(LegacyShipAttackKind.HEAVY).moveType()
                != LegacyShipProjectileMoveType.ARC
                || LegacyShipBehaviorCatalog.attackProfile(harbourHime)
                .projectileProfile(LegacyShipAttackKind.HEAVY).moveType()
                != LegacyShipProjectileMoveType.DIRECT
                || LegacyShipBehaviorCatalog.heavyDamageMultiplierFor(28, true) != 0.75F) {
            helper.fail("Battleship Hime and Harbour Hime should keep their legacy heavy projectile boundaries");
            return;
        }

        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void fourthFiveShipClassesKeepLegacyBehaviorBoundaries(GameTestHelper helper) {
        int[] eggMetas = {31, 32, 46, 49, 50};
        int[] classIds = {29, 30, 44, 47, 48};
        int[] equipmentTypes = {2, 2, 1, 3, 3};
        int[] foodMaxima = {18, 35, 10, 30, 35};
        float[][] dimensions = {
                {0.6F, 1.6F}, {0.7F, 2.0F}, {0.7F, 1.85F},
                {0.6F, 1.875F}, {0.6F, 1.875F}
        };
        boolean[][] attackChannels = {
                {true, true, true}, {true, true, true}, {true, true, false},
                {false, false, true}, {false, false, true}
        };
        float[] launchFactors = {0.7F, 0.7F, 0.0F, 0.65F, 0.65F};

        for (int index = 0; index < eggMetas.length; index++) {
            ShipEntitySpec spec = ShipEntitySpecs.getByEggMeta(eggMetas[index]);
            LegacyShipAttackProfile profile = LegacyShipBehaviorCatalog.attackProfile(spec);
            EntityDimensions entityDimensions = LegacyShipBehaviorCatalog.dimensionsFor(spec, 0);
            if (spec.legacyClassId() != classIds[index]
                    || LegacyShipBehaviorCatalog.equipmentTypeFor(spec) != equipmentTypes[index]
                    || LegacyShipBehaviorCatalog.foodSaturationMaxFor(spec) != foodMaxima[index]
                    || Math.abs(entityDimensions.width - dimensions[index][0]) > 1.0E-6F
                    || Math.abs(entityDimensions.height - dimensions[index][1]) > 1.0E-6F
                    || profile.light() != attackChannels[index][0]
                    || profile.heavy() != attackChannels[index][1]
                    || profile.hasAirAttack() != attackChannels[index][2]
                    || profile.hasAirAttack()
                    && Math.abs(LegacyShipBehaviorCatalog.aircraftLaunchHeightFactorFor(
                    spec, LegacyShipAttackKind.AIR_LIGHT) - launchFactors[index]) > 1.0E-6F) {
                helper.fail("fourth-batch ship egg " + eggMetas[index]
                        + " should keep its legacy class, equipment, geometry, attack, and launch boundaries");
                return;
            }
        }

        ShipEntitySpec submarineHime = ShipEntitySpecs.getByEggMeta(46);
        if (!LegacyShipBehaviorCatalog.supportsItemPickup(submarineHime)
                || LegacyShipBehaviorCatalog.pickupRangeBaseFor(submarineHime) != 4
                || LegacyShipBehaviorCatalog.ammoConsumptionFor(submarineHime) != 1
                || LegacyShipBehaviorCatalog.attackAmmoCostFor(submarineHime, LegacyShipAttackKind.LIGHT) != 1
                || LegacyShipBehaviorCatalog.attackAmmoCostFor(submarineHime, LegacyShipAttackKind.HEAVY) != 1) {
            helper.fail("Submarine Hime should keep its legacy pickup and ammunition boundaries");
            return;
        }

        ShipEntitySpec hostileKaga = ShipEntitySpecs.getByEggMeta(2049);
        ShipEntitySpec hostileAkagi = ShipEntitySpecs.getByEggMeta(2050);
        if (LegacyShipBehaviorCatalog.aircraftLightCapacityFor(hostileKaga, 50, 0) != 10
                || LegacyShipBehaviorCatalog.aircraftHeavyCapacityFor(hostileKaga, 50, 0) != 10
                || LegacyShipBehaviorCatalog.aircraftLightCapacityFor(hostileAkagi, 50, 0) != 10
                || LegacyShipBehaviorCatalog.aircraftHeavyCapacityFor(hostileAkagi, 50, 0) != 10
                || LegacyShipBehaviorCatalog.managesAircraftInventory(hostileKaga)
                || LegacyShipBehaviorCatalog.managesAircraftInventory(hostileAkagi)) {
            helper.fail("hostile Kaga and Akagi should keep the legacy fixed ten-aircraft boss profile");
            return;
        }

        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void fifthFiveShipClassesKeepLegacyBehaviorBoundaries(GameTestHelper helper) {
        int[] eggMetas = {51, 62, 63, 64, 65};
        int[] classIds = {49, 60, 61, 62, 63};
        int[] equipmentTypes = {1, 1, 1, 1, 1};
        int[] foodMaxima = {17, 19, 19, 19, 19};
        for (int index = 0; index < eggMetas.length; index++) {
            ShipEntitySpec spec = ShipEntitySpecs.getByEggMeta(eggMetas[index]);
            if (spec.legacyClassId() != classIds[index]
                    || LegacyShipBehaviorCatalog.equipmentTypeFor(spec) != equipmentTypes[index]
                    || LegacyShipBehaviorCatalog.foodSaturationMaxFor(spec) != foodMaxima[index]) {
                helper.fail("fifth-batch ship egg " + eggMetas[index]
                        + " should keep its legacy class, equipment, and FoodMax boundaries");
                return;
            }
        }

        ShipEntitySpec caHime = ShipEntitySpecs.getByEggMeta(51);
        if (!LegacyShipBehaviorCatalog.legacyEngineSmokePointsFor(caHime, 0).isEmpty()) {
            helper.fail("CA Hime should keep the legacy absence of the Kongou-family engine smoke branch");
            return;
        }

        ShipEntitySpec friendlyKongou = ShipEntitySpecs.getByEggMeta(62);
        List<LegacyShipBehaviorCatalog.EngineSmokePoint> kongouSmoke =
                LegacyShipBehaviorCatalog.legacyEngineSmokePointsFor(friendlyKongou, 0);
        if (kongouSmoke.size() != 2
                || !engineSmokePointMatches(kongouSmoke.get(0), -0.7F, 1.17F, 1.0F)
                || !engineSmokePointMatches(kongouSmoke.get(1), -0.45F, 1.32F, 1.0F)) {
            helper.fail("friendly Kongou should keep both legacy engine smoke points");
            return;
        }

        ShipEntitySpec hostileKongou = ShipEntitySpecs.getByEggMeta(2062);
        List<LegacyShipBehaviorCatalog.EngineSmokePoint> hostileKongouSmoke =
                LegacyShipBehaviorCatalog.legacyEngineSmokePointsFor(hostileKongou, 3);
        if (hostileKongouSmoke.size() != 2
                || !engineSmokePointMatches(hostileKongouSmoke.get(0), -1.64F, 4.74F, 4.0F)
                || !engineSmokePointMatches(hostileKongouSmoke.get(1), -2.61F, 4.27F, 4.0F)) {
            helper.fail("hostile Kongou scale three should keep both legacy scaled smoke points");
            return;
        }

        ShipEntitySpec hostileHiei = ShipEntitySpecs.getByEggMeta(2063);
        List<LegacyShipBehaviorCatalog.EngineSmokePoint> hostileHieiSmoke =
                LegacyShipBehaviorCatalog.legacyEngineSmokePointsFor(hostileHiei, 2);
        if (hostileHieiSmoke.size() != 1
                || !engineSmokePointMatches(hostileHieiSmoke.get(0), -1.76F, 3.4F, 3.0F)) {
            helper.fail("hostile Hiei should keep its legacy scaled x, y, and particle scale");
            return;
        }

        ShipEntitySpec hostileHaruna = ShipEntitySpecs.getByEggMeta(2064);
        ShipEntitySpec hostileKirishima = ShipEntitySpecs.getByEggMeta(2065);
        List<LegacyShipBehaviorCatalog.EngineSmokePoint> hostileHarunaSmoke =
                LegacyShipBehaviorCatalog.legacyEngineSmokePointsFor(hostileHaruna, 1);
        List<LegacyShipBehaviorCatalog.EngineSmokePoint> hostileKirishimaSmoke =
                LegacyShipBehaviorCatalog.legacyEngineSmokePointsFor(hostileKirishima, 1);
        if (hostileHarunaSmoke.size() != 1
                || hostileKirishimaSmoke.size() != 1
                || !engineSmokePointMatches(hostileHarunaSmoke.get(0), -1.16F, 2.3F, 2.0F)
                || !engineSmokePointMatches(hostileKirishimaSmoke.get(0), -1.16F, 2.3F, 2.0F)) {
            helper.fail("hostile Haruna and Kirishima should keep their legacy scaled smoke points");
            return;
        }

        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void kongouFamilyFiveShipCombatHooksKeepLegacyCannonFeedback(GameTestHelper helper) {
        int[] hostileEggMetas = {2049, 2062, 2063, 2064, 2065};
        for (int eggMeta : hostileEggMetas) {
            ShipEntitySpec spec = ShipEntitySpecs.getByEggMeta(eggMeta);
            LegacyShipBehaviorCatalog.LegacyCannonAttackFeedback feedback =
                    LegacyShipBehaviorCatalog.cannonAttackFeedbackFor(
                            spec, LegacyShipAttackKind.LIGHT, 2);
            boolean kongouFamily = eggMeta != 2049;
            if (feedback.isPresent() != kongouFamily) {
                helper.fail("hostile Kongou-family cannon feedback should exclude Kaga and include the four battleships");
                return;
            }
            if (kongouFamily
                    && (Math.abs(feedback.width() - 2.7D) > 1.0E-6D
                    || Math.abs(feedback.length() - 3.0D) > 1.0E-6D
                    || Math.abs(feedback.height() - 3.3D) > 1.0E-6D)) {
                helper.fail("hostile Kongou-family type-5 smoke should preserve the legacy scale-two parameters");
                return;
            }
            if (LegacyShipBehaviorCatalog.cannonAttackFeedbackFor(
                    spec, LegacyShipAttackKind.HEAVY, 2).isPresent()) {
                helper.fail("legacy type-5 cannon feedback should only be emitted for light cannon attacks");
                return;
            }
        }

        ShipEntitySpec friendlyKongou = ShipEntitySpecs.getByEggMeta(62);
        if (LegacyShipBehaviorCatalog.cannonAttackFeedbackFor(
                friendlyKongou, LegacyShipAttackKind.LIGHT, 2).isPresent()) {
            helper.fail("friendly Kongou should retain the hostile-only type-5 cannon override");
            return;
        }

        ClientboundSpawnParticlePacket packet = new ClientboundSpawnParticlePacket(
                GameplayParticleType.LEGACY_CANNON, 1.0D, 2.0D, 3.0D,
                2.7D, 3.0D, 3.3D, 90000);
        FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());
        ClientboundSpawnParticlePacket.encode(packet, buffer);
        ClientboundSpawnParticlePacket decoded = ClientboundSpawnParticlePacket.decode(buffer);
        if (decoded.particleType() != GameplayParticleType.LEGACY_CANNON
                || decoded.variant() != 90000
                || Math.abs(decoded.velocityX() - 2.7D) > 1.0E-6D
                || Math.abs(decoded.velocityY() - 3.0D) > 1.0E-6D
                || Math.abs(decoded.velocityZ() - 3.3D) > 1.0E-6D) {
            helper.fail("legacy type-5 cannon feedback should preserve smoke parameters and encoded body yaw");
            return;
        }

        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void legacyLightCannonParticleFamiliesKeepShipSpecificHooks(GameTestHelper helper) {
        int[] doubleCannonEggMetas = {15, 28, 62};
        double[] spacing = {0.7D, 1.0D, 1.0D};
        double[] sourceHeights = {0.0D, 0.3D, 0.3D};
        for (int index = 0; index < doubleCannonEggMetas.length; index++) {
            LegacyShipBehaviorCatalog.LegacyLightAttackParticleFeedback feedback =
                    LegacyShipBehaviorCatalog.lightAttackParticleFeedbackFor(
                            ShipEntitySpecs.getByEggMeta(doubleCannonEggMetas[index]),
                            LegacyShipAttackKind.LIGHT);
            if (!feedback.isPresent()
                    || feedback.particleType() != GameplayParticleType.LEGACY_DOUBLE_CANNON
                    || Math.abs(feedback.parameter() - spacing[index]) > 1.0E-6D
                    || Math.abs(feedback.sourceHeight(false) - sourceHeights[index]) > 1.0E-6D
                    || Math.abs(feedback.sourceHeight(true) - sourceHeights[index]) > 1.0E-6D) {
                helper.fail("Ru, Battleship Hime, and friendly Kongou-class ships should keep legacy type-19 cannon hooks");
                return;
            }
        }

        LegacyShipBehaviorCatalog.LegacyLightAttackParticleFeedback northern =
                LegacyShipBehaviorCatalog.lightAttackParticleFeedbackFor(
                        ShipEntitySpecs.getByEggMeta(33), LegacyShipAttackKind.LIGHT);
        if (!northern.isPresent()
                || northern.particleType() != GameplayParticleType.LEGACY_NORTHERN_CANNON
                || LegacyShipBehaviorCatalog.lightAttackTargetParticleFor(
                ShipEntitySpecs.getByEggMeta(33), LegacyShipAttackKind.LIGHT,
                GameplayParticleType.HIT_EXPLOSION) != GameplayParticleType.LEGACY_NORTHERN_HIT
                || LegacyShipBehaviorCatalog.lightAttackTargetParticleFor(
                ShipEntitySpecs.getByEggMeta(33), LegacyShipAttackKind.HEAVY,
                GameplayParticleType.HIT_EXPLOSION) != GameplayParticleType.HIT_EXPLOSION) {
            helper.fail("Northern Hime should keep the legacy type-31 attacker and type-30 hit particle boundaries");
            return;
        }

        LegacyShipBehaviorCatalog.LegacyLightAttackParticleFeedback battleshipRe =
                LegacyShipBehaviorCatalog.lightAttackParticleFeedbackFor(
                        ShipEntitySpecs.getByEggMeta(17), LegacyShipAttackKind.LIGHT);
        if (!battleshipRe.isPresent()
                || battleshipRe.particleType() != GameplayParticleType.LEGACY_RE_LASER
                || battleshipRe.sourceHeight(false) != 1.7D
                || battleshipRe.sourceHeight(true) != 1.45D) {
            helper.fail("Battleship Re should keep its legacy type-6 adjustable laser source heights");
            return;
        }

        if (LegacyShipBehaviorCatalog.legacyEntityMarkerTypeFor(37) != 1
                || LegacyShipBehaviorCatalog.legacyEntityMarkerTypeFor(46) != 4
                || LegacyShipBehaviorCatalog.legacyEntityMarkerTypeFor(12) != 17
                || LegacyShipBehaviorCatalog.legacyEntityMarkerTypeFor(13) != 17
                || LegacyShipBehaviorCatalog.legacyEntityMarkerTypeFor(58) != -1) {
            helper.fail("legacy NGT, YMT, Wo, and Ru client branches should keep ParticleTeam marker subtypes");
            return;
        }

        List<LegacyShipBehaviorCatalog.LegacyCannonAttackFeedback> yamatoFriendly =
                LegacyShipBehaviorCatalog.cannonAttackFeedbacksFor(
                        ShipEntitySpecs.getByEggMeta(48), LegacyShipAttackKind.LIGHT, 0);
        List<LegacyShipBehaviorCatalog.LegacyCannonAttackFeedback> yamatoHostile =
                LegacyShipBehaviorCatalog.cannonAttackFeedbacksFor(
                        ShipEntitySpecs.getByEggMeta(2048), LegacyShipAttackKind.LIGHT, 2);
        List<LegacyShipBehaviorCatalog.LegacyCannonAttackFeedback> nagatoFriendly =
                LegacyShipBehaviorCatalog.cannonAttackFeedbacksFor(
                        ShipEntitySpecs.getByEggMeta(39), LegacyShipAttackKind.LIGHT, 0);
        List<LegacyShipBehaviorCatalog.LegacyCannonAttackFeedback> nagatoHostile =
                LegacyShipBehaviorCatalog.cannonAttackFeedbacksFor(
                        ShipEntitySpecs.getByEggMeta(2039), LegacyShipAttackKind.LIGHT, 2);
        if (yamatoFriendly.size() != 1
                || !cannonFeedbackMatches(yamatoFriendly.get(0), 0.9D, 1.3D, 1.2D)
                || yamatoHostile.size() != 3
                || !cannonFeedbackMatches(yamatoHostile.get(0), 3.0D, 3.0D, 4.8D)
                || !cannonFeedbackMatches(yamatoHostile.get(1), 2.7D, 3.6D, 3.0D)
                || !cannonFeedbackMatches(yamatoHostile.get(2), 3.3D, 3.3D, 1.5D)
                || nagatoFriendly.size() != 1
                || !cannonFeedbackMatches(nagatoFriendly.get(0), 0.9D, 1.0D, 1.1D)
                || nagatoHostile.size() != 1
                || !cannonFeedbackMatches(nagatoHostile.get(0), 2.7D, 3.0D, 3.3D)) {
            helper.fail("friendly and hostile Yamato and Nagato should keep their legacy type-5 smoke packet families");
            return;
        }

        for (GameplayParticleType type : new GameplayParticleType[] {
                GameplayParticleType.LEGACY_DOUBLE_CANNON,
                GameplayParticleType.LEGACY_NORTHERN_CANNON,
                GameplayParticleType.LEGACY_NORTHERN_HIT,
                GameplayParticleType.LEGACY_RE_LASER}) {
            ClientboundSpawnParticlePacket packet = new ClientboundSpawnParticlePacket(
                    type, 1.0D, 2.0D, 3.0D, 0.1D, 0.2D, 0.3D, 1234);
            FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());
            ClientboundSpawnParticlePacket.encode(packet, buffer);
            ClientboundSpawnParticlePacket decoded = ClientboundSpawnParticlePacket.decode(buffer);
            if (decoded.particleType() != type || decoded.variant() != 1234) {
                helper.fail("legacy light-cannon particle type should survive packet encoding: " + type);
                return;
            }
        }

        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void sixthFiveShipClassesKeepLegacyEngineBranches(GameTestHelper helper) {
        int[] eggMetas = {38, 39, 48, 60, 61};
        int[] classIds = {36, 37, 46, 58, 59};
        for (int index = 0; index < eggMetas.length; index++) {
            ShipEntitySpec spec = ShipEntitySpecs.getByEggMeta(eggMetas[index]);
            if (spec.legacyClassId() != classIds[index]
                    || LegacyShipBehaviorCatalog.equipmentTypeFor(spec) != 1) {
                helper.fail("sixth-batch ship egg " + eggMetas[index]
                        + " should keep its legacy class and type-1 equipment boundary");
                return;
            }
        }

        ShipEntitySpec friendlyShimakaze = ShipEntitySpecs.getByEggMeta(38);
        ShipEntitySpec hostileShimakaze = ShipEntitySpecs.getByEggMeta(2038);
        ShipEntitySpec friendlyAtago = ShipEntitySpecs.getByEggMeta(60);
        ShipEntitySpec hostileTakao = ShipEntitySpecs.getByEggMeta(2061);
        if (!LegacyShipBehaviorCatalog.legacyEngineVisualPointsFor(friendlyShimakaze, 0).isEmpty()
                || !LegacyShipBehaviorCatalog.legacyEngineVisualPointsFor(hostileShimakaze, 3).isEmpty()
                || !LegacyShipBehaviorCatalog.legacyEngineVisualPointsFor(friendlyAtago, 0).isEmpty()
                || !LegacyShipBehaviorCatalog.legacyEngineVisualPointsFor(hostileTakao, 3).isEmpty()) {
            helper.fail("Shimakaze, Atago, and Takao should keep the legacy absence of a dedicated engine branch");
            return;
        }

        ShipEntitySpec friendlyNagato = ShipEntitySpecs.getByEggMeta(39);
        List<LegacyShipBehaviorCatalog.EngineVisualPoint> nagatoFriendlyVisuals =
                LegacyShipBehaviorCatalog.legacyEngineVisualPointsFor(friendlyNagato, 0);
        if (nagatoFriendlyVisuals.size() != 1
                || nagatoFriendlyVisuals.get(0).kind() != LegacyShipBehaviorCatalog.EngineVisualKind.SMOKE_CHIMNEY_L
                || nagatoFriendlyVisuals.get(0).requiredModelStateBit() != 1
                || !nagatoFriendlyVisuals.get(0).requiresActiveFriendlyState()
                || !engineVisualPointMatches(nagatoFriendlyVisuals.get(0), -0.56F, 1.5F, 1.0F)) {
            helper.fail("friendly Nagato should keep the legacy bit-1, fuel-gated engine smoke branch");
            return;
        }

        ShipEntitySpec hostileNagato = ShipEntitySpecs.getByEggMeta(2039);
        List<LegacyShipBehaviorCatalog.EngineVisualPoint> nagatoHostileVisuals =
                LegacyShipBehaviorCatalog.legacyEngineVisualPointsFor(hostileNagato, 3);
        if (nagatoHostileVisuals.size() != 1
                || nagatoHostileVisuals.get(0).requiredModelStateBit() != 1
                || nagatoHostileVisuals.get(0).requiresActiveFriendlyState()
                || !engineVisualPointMatches(nagatoHostileVisuals.get(0), -2.24F, 6.0F, 4.0F)) {
            helper.fail("hostile Nagato scale three should keep the legacy scaled bit-1 smoke branch");
            return;
        }

        ShipEntitySpec friendlyYamato = ShipEntitySpecs.getByEggMeta(48);
        List<LegacyShipBehaviorCatalog.EngineVisualPoint> yamatoFriendlyVisuals =
                LegacyShipBehaviorCatalog.legacyEngineVisualPointsFor(friendlyYamato, 0);
        if (yamatoFriendlyVisuals.size() != 1
                || yamatoFriendlyVisuals.get(0).kind() != LegacyShipBehaviorCatalog.EngineVisualKind.YAMATO_SPEED_BLUR
                || yamatoFriendlyVisuals.get(0).requiredModelStateBit() != 0
                || !yamatoFriendlyVisuals.get(0).requiresActiveFriendlyState()
                || !engineVisualPointMatches(yamatoFriendlyVisuals.get(0), -0.63F, 1.65F, 1.0F)) {
            helper.fail("friendly Yamato should keep the legacy bit-0, fuel-gated type-20 visual branch");
            return;
        }

        ShipEntitySpec hostileYamato = ShipEntitySpecs.getByEggMeta(2048);
        List<LegacyShipBehaviorCatalog.EngineVisualPoint> yamatoHostileVisuals =
                LegacyShipBehaviorCatalog.legacyEngineVisualPointsFor(hostileYamato, 3);
        if (yamatoHostileVisuals.size() != 1
                || yamatoHostileVisuals.get(0).kind() != LegacyShipBehaviorCatalog.EngineVisualKind.SMOKE_CHIMNEY_L
                || yamatoHostileVisuals.get(0).requiredModelStateBit() != -1
                || yamatoHostileVisuals.get(0).requiresActiveFriendlyState()
                || !engineVisualPointMatches(yamatoHostileVisuals.get(0), -2.52F, 6.6F, 4.0F)) {
            helper.fail("hostile Yamato scale three should keep the always-on scaled smoke branch");
            return;
        }

        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void seventhFiveHostileShipsKeepLegacyScaleBranches(GameTestHelper helper) {
        int[] hostileEggMetas = {2038, 2039, 2040, 2041, 2048};
        double[] expectedHealthScales = {250D, 500D, 1600D, 3200D};
        double[] expectedRangeScales = {12D, 15D, 18D, 22D};

        for (int hostileEggMeta : hostileEggMetas) {
            LegacyShipEntity ship = ModEntityTypes.LEGACY_SHIP.get().create(helper.getLevel());
            if (ship == null) {
                helper.fail("hostile ship should be creatable for scale-level migration coverage");
                return;
            }
            ship.setVariantEggMeta(hostileEggMeta);
            ShipEntitySpec spec = ship.getSpec();
            float referenceHealthRatio = 0F;
            float previousRange = -1F;

            for (int scaleLevel = 0; scaleLevel <= 3; scaleLevel++) {
                boolean boss = scaleLevel >= 2;
                boolean elite = scaleLevel == 1 || scaleLevel == 3;
                ship.initializeHostileRuntime(false, elite, boss);
                if (ship.getLegacyHostileScaleLevel() != scaleLevel) {
                    helper.fail("hostile " + hostileEggMeta + " should preserve legacy scale level " + scaleLevel);
                    return;
                }

                double[] expectedScale = LegacyShipStatTables.hostileScaleFor(spec.archetype(), scaleLevel);
                float healthRatio = ship.getLegacyStats().get(LegacyShipStatTables.Attr.HP)
                        / (float) expectedHealthScales[scaleLevel];
                if (Math.abs(expectedScale[LegacyShipStatTables.BaseAttr.HP] - expectedHealthScales[scaleLevel]) > 1.0E-6D
                        || Math.abs(expectedScale[LegacyShipStatTables.BaseAttr.HIT] - expectedRangeScales[scaleLevel]) > 1.0E-6D
                        || (scaleLevel > 0 && Math.abs(healthRatio - referenceHealthRatio) > 1.0E-4F)
                        || (scaleLevel > 0 && ship.getLegacyStats().attackRange() <= previousRange)) {
                    helper.fail("hostile " + hostileEggMeta + " should apply the legacy scale table to stats");
                    return;
                }
                double expectedFloatingDepth = hostileEggMeta == 2040 || hostileEggMeta == 2041
                        ? 1.1D + scaleLevel * 0.3D
                        : 0.3D + scaleLevel * 0.05D;
                if (Math.abs(ship.getLegacyFloatingDepth() - expectedFloatingDepth) > 1.0E-6D) {
                    helper.fail("hostile " + hostileEggMeta + " should apply the legacy scale to floating depth");
                    return;
                }
                if (scaleLevel == 0) {
                    referenceHealthRatio = healthRatio;
                }
                previousRange = ship.getLegacyStats().attackRange();
            }
            ship.discard();
        }

        LegacyShipBehaviorCatalog.HostileLootProfile bossLargeLoot =
                LegacyShipBehaviorCatalog.hostileLootProfile(ShipEntitySpecs.getByEggMeta(2038), true, true);
        if (bossLargeLoot.eggDropChance() != 1.0F) {
            helper.fail("legacy scale-three hostile bosses should always drop their recovered egg");
            return;
        }

        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void eighthFiveShipHooksKeepLegacyHostileDestroyerParity(GameTestHelper helper) {
        int[] friendlyEggMetas = {53, 54, 55, 56};
        int[] hostileEggMetas = {2053, 2054, 2055, 2056};
        float[] expectedX = {-0.42F, -0.8F, -1.2F, -1.65F};
        float[] expectedY = {1.4F, 2.7F, 4.1F, 5.3F};
        float[] expectedParticleScale = {1.0F, 1.8F, 2.6F, 3.4F};

        for (int index = 0; index < friendlyEggMetas.length; index++) {
            ShipEntitySpec spec = ShipEntitySpecs.getByEggMeta(friendlyEggMetas[index]);
            List<LegacyShipBehaviorCatalog.EngineVisualPoint> points =
                    LegacyShipBehaviorCatalog.legacyEngineVisualPointsFor(spec, 0);
            if (points.size() != 1
                    || points.get(0).kind() != LegacyShipBehaviorCatalog.EngineVisualKind.SMOKE_CHIMNEY_L
                    || points.get(0).requiredModelStateBit() != 0
                    || !points.get(0).requiresActiveFriendlyState()
                    || !engineVisualPointMatches(points.get(0), -0.42F, 1.4F, 1.0F)) {
                helper.fail("friendly six destroyer " + friendlyEggMetas[index]
                        + " should keep the legacy active bit-0 smoke point");
                return;
            }
        }

        for (int index = 0; index < hostileEggMetas.length; index++) {
            ShipEntitySpec spec = ShipEntitySpecs.getByEggMeta(hostileEggMetas[index]);
            for (int scale = 0; scale <= 3; scale++) {
                List<LegacyShipBehaviorCatalog.EngineVisualPoint> points =
                        LegacyShipBehaviorCatalog.legacyEngineVisualPointsFor(spec, scale);
                if (points.size() != 1
                        || points.get(0).kind() != LegacyShipBehaviorCatalog.EngineVisualKind.SMOKE_CHIMNEY_L
                        || points.get(0).requiredModelStateBit() != 0
                        || points.get(0).requiresActiveFriendlyState()
                        || !engineVisualPointMatches(points.get(0),
                        expectedX[scale], expectedY[scale], expectedParticleScale[scale])) {
                    helper.fail("hostile six destroyer " + hostileEggMetas[index]
                            + " should keep the legacy scale-three smoke branches");
                    return;
                }
            }
        }

        if (LegacyShipBehaviorCatalog.hostileBossBarColorFor(ShipEntitySpecs.getByEggMeta(2049))
                        != BossEvent.BossBarColor.BLUE
                || LegacyShipBehaviorCatalog.hostileBossBarColorFor(ShipEntitySpecs.getByEggMeta(2053))
                        != BossEvent.BossBarColor.PURPLE
                || LegacyShipBehaviorCatalog.hostileBossBarColorFor(ShipEntitySpecs.getByEggMeta(2054))
                        != BossEvent.BossBarColor.WHITE
                || LegacyShipBehaviorCatalog.hostileBossBarColorFor(ShipEntitySpecs.getByEggMeta(2055))
                        != BossEvent.BossBarColor.PINK
                || LegacyShipBehaviorCatalog.hostileBossBarColorFor(ShipEntitySpecs.getByEggMeta(2056))
                        != BossEvent.BossBarColor.PINK) {
            helper.fail("hostile Kaga and six destroyers should keep their legacy boss bar colors");
            return;
        }

        LegacyShipEntity ikazuchi = ModEntityTypes.LEGACY_SHIP.get().create(helper.getLevel());
        LegacyShipEntity inazuma = ModEntityTypes.LEGACY_SHIP.get().create(helper.getLevel());
        if (ikazuchi == null || inazuma == null) {
            helper.fail("hostile Ikazuchi and Inazuma should be creatable for Raiden pairing tests");
            return;
        }

        ikazuchi.setVariantEggMeta(2055);
        inazuma.setVariantEggMeta(2056);
        // Use the large hostile scale to keep this range query isolated from
        // neighboring GameTest structures that may contain ordinary escorts.
        ikazuchi.initializeHostileRuntime(false, true, true);
        inazuma.initializeHostileRuntime(false, true, true);
        ikazuchi.setNoAi(true);
        inazuma.setNoAi(true);
        ikazuchi.setNoGravity(true);
        inazuma.setNoGravity(true);
        BlockPos ikazuchiPos = helper.absolutePos(new BlockPos(8, 4, 8));
        BlockPos inazumaPos = helper.absolutePos(new BlockPos(9, 4, 8));
        ikazuchi.setPos(ikazuchiPos.getX() + 0.5D, ikazuchiPos.getY(), ikazuchiPos.getZ() + 0.5D);
        inazuma.setPos(inazumaPos.getX() + 0.5D, inazumaPos.getY(), inazumaPos.getZ() + 0.5D);
        helper.getLevel().addFreshEntity(ikazuchi);
        helper.getLevel().addFreshEntity(inazuma);

        ikazuchi.tickCount = 128;
        LegacyShipBehaviorCatalog.tickLegacyRiderBehavior(ikazuchi);
        inazuma.tickCount = 128;
        LegacyShipBehaviorCatalog.tickLegacyRiderBehavior(inazuma);
        if (ikazuchi.getVehicle() != inazuma
                || !ikazuchi.isLegacyRaiden()
                || !inazuma.isLegacyRaiden()
                || ikazuchi.getLegacyRidingState() != 1
                || inazuma.getLegacyRidingState() != 2) {
            helper.fail("hostile Ikazuchi should form the legacy Raiden pair without friendly gates: "
                    + "ikazuchiClass=" + ikazuchi.getShipClassId()
                    + ", inazumaClass=" + inazuma.getShipClassId()
                    + ", ikazuchiHostile=" + ikazuchi.isHostileVariant()
                    + ", inazumaHostile=" + inazuma.isHostileVariant()
                    + ", ikazuchiScale=" + ikazuchi.getLegacyHostileScaleLevel()
                    + ", inazumaScale=" + inazuma.getLegacyHostileScaleLevel()
                    + ", ikazuchiVehicle=" + (ikazuchi.getVehicle() == null ? "null" : ikazuchi.getVehicle().getClass().getSimpleName())
                    + ", inazumaPassengers=" + inazuma.getPassengers().size()
                    + ", ikazuchiPassengers=" + ikazuchi.getPassengers().size());
            return;
        }

        BlockPos liftedInazumaPos = helper.absolutePos(new BlockPos(12, 5, 12));
        inazuma.setPos(liftedInazumaPos.getX() + 0.5D, liftedInazumaPos.getY(), liftedInazumaPos.getZ() + 0.5D);
        ikazuchi.rideTick();
        double expectedRiderY = inazuma.getY() + inazuma.getBbHeight() * 0.47D
                + ikazuchi.getMyRidingOffset();
        if (Math.abs(ikazuchi.getX() - inazuma.getX()) > 1.0E-6D
                || Math.abs(ikazuchi.getY() - expectedRiderY) > 1.0E-6D
                || Math.abs(ikazuchi.getZ() - inazuma.getZ()) > 1.0E-6D) {
            helper.fail("hostile Inazuma should use the legacy direct Raiden passenger position");
            return;
        }

        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void ninthFiveShipClientHooksKeepLegacyConditions(GameTestHelper helper) {
        List<LegacyShipBehaviorCatalog.LegacyClientEffectPoint> carrierWoStanding =
                LegacyShipBehaviorCatalog.carrierWoEyePointsFor(false, 0, 0.0F, 0.0F);
        if (carrierWoStanding.size() != 2
                || !clientEffectPointMatches(carrierWoStanding.get(0), 0.55F, 1.0F, 0.2F,
                0.0F, 0.05F, 1.0F, LegacyShipBehaviorCatalog.LegacyClientEffectKind.EMOTION, 16)
                || !clientEffectPointMatches(carrierWoStanding.get(1), -0.55F, 1.0F, 0.2F,
                0.0F, 0.05F, 1.0F, LegacyShipBehaviorCatalog.LegacyClientEffectKind.EMOTION, 16)) {
            helper.fail("Carrier Wo should preserve the legacy standing eye points and motion");
            return;
        }

        List<LegacyShipBehaviorCatalog.LegacyClientEffectPoint> carrierWoSitting =
                LegacyShipBehaviorCatalog.carrierWoEyePointsFor(true, 0, 0.0F, 0.0F);
        if (carrierWoSitting.size() != 2
                || !clientEffectPointMatches(carrierWoSitting.get(0), -0.3F, 1.0F, -0.4F,
                0.0F, 0.05F, 1.0F, LegacyShipBehaviorCatalog.LegacyClientEffectKind.EMOTION, 16)
                || !clientEffectPointMatches(carrierWoSitting.get(1), -0.7F, 0.8F, 0.6F,
                0.0F, 0.05F, 1.0F, LegacyShipBehaviorCatalog.LegacyClientEffectKind.EMOTION, 16)) {
            helper.fail("Carrier Wo should preserve the legacy sitting eye points");
            return;
        }

        List<LegacyShipBehaviorCatalog.LegacyClientEffectPoint> tiltedCarrierWo =
                LegacyShipBehaviorCatalog.carrierWoEyePointsFor(false, 1, 0.0F, 0.0F);
        if (Math.abs(tiltedCarrierWo.get(0).xOffset() - 0.2965F) > 1.0E-3F
                || Math.abs(tiltedCarrierWo.get(0).yOffset() - 1.1021F) > 1.0E-3F) {
            helper.fail("Carrier Wo should apply the legacy -0.24 head tilt before yaw and pitch");
            return;
        }

        List<LegacyShipBehaviorCatalog.LegacyClientEffectPoint> submarineStanding =
                LegacyShipBehaviorCatalog.submarineYoEyePointsFor(false, 0.0F);
        List<LegacyShipBehaviorCatalog.LegacyClientEffectPoint> submarineSitting =
                LegacyShipBehaviorCatalog.submarineYoEyePointsFor(true, 0.0F);
        if (submarineStanding.size() != 2
                || !clientEffectPointMatches(submarineStanding.get(0), 0.35F, 1.8F, -0.35F,
                0.0F, 0.05F, 0.5F, LegacyShipBehaviorCatalog.LegacyClientEffectKind.EMOTION, 16)
                || !clientEffectPointMatches(submarineStanding.get(1), -0.35F, 1.8F, -0.35F,
                0.0F, 0.05F, 0.5F, LegacyShipBehaviorCatalog.LegacyClientEffectKind.EMOTION, 16)
                || !clientEffectPointMatches(submarineSitting.get(0), 0.35F, 1.5F, -0.5F,
                0.0F, 0.05F, 0.5F, LegacyShipBehaviorCatalog.LegacyClientEffectKind.EMOTION, 16)) {
            helper.fail("Submarine Yo should preserve the legacy standing and sitting eye points");
            return;
        }

        if (!LegacyShipBehaviorCatalog.shouldSpawnCarrierWoEyeEffect(1, false, false, 0)
                || LegacyShipBehaviorCatalog.shouldSpawnCarrierWoEyeEffect(0, false, false, 0)
                || LegacyShipBehaviorCatalog.shouldSpawnCarrierWoEyeEffect(1, true, false, 0)
                || LegacyShipBehaviorCatalog.shouldSpawnCarrierWoEyeEffect(1, false, true, 4)
                || !LegacyShipBehaviorCatalog.shouldSpawnSubmarineYoEyeEffect(1, false, true, 0)
                || LegacyShipBehaviorCatalog.shouldSpawnSubmarineYoEyeEffect(1, false, true, 4)) {
            helper.fail("Carrier Wo and Submarine Yo should preserve their legacy eye effect gates");
            return;
        }

        if (!LegacyShipBehaviorCatalog.shouldSpawnBattleshipRuFlameEffect(8, false, false, 16)
                || LegacyShipBehaviorCatalog.shouldSpawnBattleshipRuFlameEffect(8, false, false, 400)
                || !LegacyShipBehaviorCatalog.shouldSpawnBattleshipRuBoredEffect(1, 4, 448)
                || LegacyShipBehaviorCatalog.shouldSpawnBattleshipRuBoredEffect(1, 4, 384)
                || !LegacyShipBehaviorCatalog.shouldSpawnTransportWaEmotion(128, 0)
                || LegacyShipBehaviorCatalog.shouldSpawnTransportWaEmotion(128, 1)
                || !LegacyShipBehaviorCatalog.shouldSpawnCarrierAkagiEmotion(128, 0, false)
                || LegacyShipBehaviorCatalog.shouldSpawnCarrierAkagiEmotion(128, 0, true)) {
            helper.fail("Ru, Transport Wa, and Carrier Akagi should preserve legacy client tick gates");
            return;
        }

        if (LegacyShipBehaviorCatalog.battleshipRuRingLuckAmplifierFor(139) != 0
                || LegacyShipBehaviorCatalog.battleshipRuRingLuckAmplifierFor(140) != 1
                || LegacyShipBehaviorCatalog.battleshipRuRingLuckAmplifierFor(280) != 2) {
            helper.fail("Battleship Ru should preserve the legacy level-divided Luck amplifier");
            return;
        }

        if (!LegacyShipBehaviorCatalog.shouldApplyBattleshipRuRingLuck(true, true, 128)
                || LegacyShipBehaviorCatalog.shouldApplyBattleshipRuRingLuck(false, true, 128)
                || LegacyShipBehaviorCatalog.shouldApplyBattleshipRuRingLuck(true, false, 128)
                || LegacyShipBehaviorCatalog.shouldApplyBattleshipRuRingLuck(true, true, 64)) {
            helper.fail("Battleship Ru should preserve the legacy daytime ring Luck tick gate");
            return;
        }

        if (LegacyShipBehaviorCatalog.shouldPlayLegacyAttackVoice(7)
                || !LegacyShipBehaviorCatalog.shouldPlayLegacyAttackVoice(8)
                || !LegacyShipBehaviorCatalog.shouldPlayLegacyAttackVoice(9)) {
            helper.fail("special summon and barrage voices should preserve the legacy 20% random gate");
            return;
        }

        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void tenthFiveShipHooksKeepLegacyConditions(GameTestHelper helper) {
        int[] eggMetas = {12, 17, 39, 48, 51};
        int[] classIds = {10, 15, 37, 46, 49};
        for (int index = 0; index < eggMetas.length; index++) {
            ShipEntitySpec spec = ShipEntitySpecs.getByEggMeta(eggMetas[index]);
            if (spec.legacyClassId() != classIds[index]) {
                helper.fail("tenth-batch egg " + eggMetas[index]
                        + " should preserve its legacy class id");
                return;
            }
        }

        if (!LegacyShipBehaviorCatalog.shouldSpawnBattleshipNagatoPhaseEffect(1, 8)
                || !LegacyShipBehaviorCatalog.shouldSpawnBattleshipNagatoPhaseEffect(3, 16)
                || LegacyShipBehaviorCatalog.shouldSpawnBattleshipNagatoPhaseEffect(2, 8)
                || LegacyShipBehaviorCatalog.shouldSpawnBattleshipNagatoPhaseEffect(1, 4)
                || Math.abs(LegacyShipBehaviorCatalog.battleshipNagatoPhaseParticleScaleFor(
                false, 3) - 0.12F) > 1.0E-6F
                || Math.abs(LegacyShipBehaviorCatalog.battleshipNagatoPhaseParticleScaleFor(
                true, 3) - 0.42F) > 1.0E-6F) {
            helper.fail("Nagato should preserve the legacy phase-1/3 client cadence and scale");
            return;
        }

        if (!LegacyShipBehaviorCatalog.shouldSpawnBattleshipYamatoPhaseEffect(1, 16)
                || !LegacyShipBehaviorCatalog.shouldSpawnBattleshipYamatoPhaseEffect(3, 32)
                || LegacyShipBehaviorCatalog.shouldSpawnBattleshipYamatoPhaseEffect(0, 16)
                || LegacyShipBehaviorCatalog.shouldSpawnBattleshipYamatoPhaseEffect(1, 12)
                || Math.abs(LegacyShipBehaviorCatalog.battleshipYamatoPhaseParticleScaleFor(
                false, 3) - 0.1F) > 1.0E-6F
                || Math.abs(LegacyShipBehaviorCatalog.battleshipYamatoPhaseParticleScaleFor(
                true, 3) - 3.1F) > 1.0E-6F) {
            helper.fail("Yamato should preserve the legacy phase client cadence and scale");
            return;
        }

        if (LegacyShipBehaviorCatalog.nagatoMoraleEmotionTypeFor(0) != 29
                || LegacyShipBehaviorCatalog.nagatoMoraleEmotionTypeFor(1) != 31
                || LegacyShipBehaviorCatalog.nagatoMoraleEmotionTypeFor(2) != 1
                || LegacyShipBehaviorCatalog.nagatoMoraleEmotionTypeFor(3) != 7
                || LegacyShipBehaviorCatalog.nagatoMoraleEmotionTypeFor(4) != 16
                || !LegacyShipBehaviorCatalog.usesBattleshipReLightAttackEffects(
                ShipEntitySpecs.getByEggMeta(17), LegacyShipAttackKind.LIGHT)
                || LegacyShipBehaviorCatalog.usesBattleshipReLightAttackEffects(
                ShipEntitySpecs.getByEggMeta(17), LegacyShipAttackKind.HEAVY)) {
            helper.fail("Nagato morale emotions and Battleship Re light feedback should keep legacy dispatch");
            return;
        }

        if (LegacyShipBehaviorCatalog.rawCriticalBonusFor(10, false, true) != 0.3F
                || LegacyShipBehaviorCatalog.rawCriticalBonusFor(49, true, true) != 0.1F
                || LegacyShipBehaviorCatalog.rawTripleHitBonusFor(49, true, true) != 0.2F) {
            helper.fail("CA Ne and CA Hime should preserve their legacy night combat bonuses");
            return;
        }

        ClientboundSpawnParticlePacket packet = new ClientboundSpawnParticlePacket(
                GameplayParticleType.LEGACY_EMOTION, 1.0D, 2.0D, 3.0D,
                0.0D, 0.0D, 0.0D, 31);
        FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());
        ClientboundSpawnParticlePacket.encode(packet, buffer);
        ClientboundSpawnParticlePacket decoded = ClientboundSpawnParticlePacket.decode(buffer);
        if (decoded.particleType() != GameplayParticleType.LEGACY_EMOTION
                || decoded.variant() != 31
                || decoded.x() != 1.0D
                || decoded.y() != 2.0D
                || decoded.z() != 3.0D) {
            helper.fail("legacy emotion particle packets should preserve their subtype and position");
            return;
        }

        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void currentFiveShipHooksKeepLegacyEntryBoundaries(GameTestHelper helper) {
        int[] eggMetas = {33, 35, 74, 51, 53};
        int[] classIds = {31, 33, 72, 49, 51};
        boolean[] hostile = {false, false, false, false, false};
        ShipArchetype[] archetypes = {
                ShipArchetype.PRINCESS,
                ShipArchetype.CARRIER,
                ShipArchetype.PRINCESS,
                ShipArchetype.PRINCESS,
                ShipArchetype.DESTROYER
        };

        for (int index = 0; index < eggMetas.length; index++) {
            ShipEntitySpec spec = ShipEntitySpecs.getByEggMeta(eggMetas[index]);
            if (spec.legacyClassId() != classIds[index]
                    || spec.hostile() != hostile[index]
                    || spec.archetype() != archetypes[index]) {
                helper.fail("current five ship entries should preserve legacy class, side, and archetype");
                return;
            }
        }

        ShipEntitySpec northern = ShipEntitySpecs.getByEggMeta(33);
        if (LegacyShipBehaviorCatalog.behaviorFor(northern).combatHook()
                        != LegacyShipBehaviorCatalog.CombatHook.NORTHERN_HIME_FLOATING_FORT
                || !LegacyShipBehaviorCatalog.shouldSpawnNorthernHimeMouthEffect(4, false, 8)
                || LegacyShipBehaviorCatalog.shouldSpawnNorthernHimeMouthEffect(4, false, 9)
                || LegacyShipBehaviorCatalog.shouldSpawnNorthernHimeMouthEffect(0, false, 8)
                || LegacyShipBehaviorCatalog.shouldSpawnNorthernHimeMouthEffect(4, true, 8)
                || Math.abs(LegacyShipBehaviorCatalog.northernHimeMouthYOffsetFor(true, false) - 0.9D)
                > 1.0E-6D
                || Math.abs(LegacyShipBehaviorCatalog.northernHimeMouthYOffsetFor(false, true) - 0.9D)
                > 1.0E-6D
                || Math.abs(LegacyShipBehaviorCatalog.northernHimeMouthYOffsetFor(false, false) - 1.1D)
                > 1.0E-6D) {
            helper.fail("Northern Hime should preserve the legacy type-28 cadence, state, fuel, and height gates");
            return;
        }

        ShipEntitySpec carrierWd = ShipEntitySpecs.getByEggMeta(35);
        LegacyShipAttackProfile carrierWdProfile = LegacyShipBehaviorCatalog.attackProfile(carrierWd);
        if (!carrierWdProfile.light()
                || carrierWdProfile.heavy()
                || !carrierWdProfile.hasAirAttack()
                || Math.abs(LegacyShipBehaviorCatalog.aircraftLaunchHeightFactorFor(
                carrierWd, LegacyShipAttackKind.AIR_LIGHT) - 1.2F) > 1.0E-6F) {
            helper.fail("Carrier WD should preserve its legacy light/air-only attack and 1.2 launch height");
            return;
        }

        LegacyShipBehaviorCatalog.LegacyLightAttackFeedback battleshipReLight =
                LegacyShipBehaviorCatalog.lightAttackFeedbackFor(
                        ShipEntitySpecs.getByEggMeta(17), LegacyShipAttackKind.LIGHT);
        LegacyShipBehaviorCatalog.LegacyLightAttackFeedback carrierWdLight =
                LegacyShipBehaviorCatalog.lightAttackFeedbackFor(carrierWd, LegacyShipAttackKind.LIGHT);
        if (!battleshipReLight.isPresent()
                || battleshipReLight.sourceHeight(false) != 1.7D
                || battleshipReLight.sourceHeight(true) != 1.45D
                || battleshipReLight.soundVolume() != 0.25F
                || !carrierWdLight.isPresent()
                || carrierWdLight.sourceHeight(false) != 1.85D
                || carrierWdLight.sourceHeight(true) != 0.8D
                || carrierWdLight.soundVolume() != 0.7F
                || LegacyShipBehaviorCatalog.lightAttackFeedbackFor(
                ShipEntitySpecs.getByEggMeta(14), LegacyShipAttackKind.LIGHT).isPresent()
                || LegacyShipBehaviorCatalog.lightAttackFeedbackFor(
                ShipEntitySpecs.getByEggMeta(49), LegacyShipAttackKind.LIGHT).isPresent()
                || LegacyShipBehaviorCatalog.lightAttackFeedbackFor(
                ShipEntitySpecs.getByEggMeta(50), LegacyShipAttackKind.LIGHT).isPresent()) {
            helper.fail("the five aviation/battleship entries should preserve their legacy light feedback boundaries");
            return;
        }

        ShipEntitySpec ssnh = ShipEntitySpecs.getByEggMeta(74);
        if (LegacyShipBehaviorCatalog.behaviorFor(ssnh).combatHook()
                        != LegacyShipBehaviorCatalog.CombatHook.SUBMARINE_HIME_DOUBLE_MISSILE
                || !LegacyShipBehaviorCatalog.dismountsBeforeDamage(ssnh)) {
            helper.fail("SSNH should preserve the legacy double-missile and pre-damage dismount hooks");
            return;
        }

        if (LegacyShipBehaviorCatalog.foodSaturationMaxFor(ShipEntitySpecs.getByEggMeta(51)) != 17
                || LegacyShipBehaviorCatalog.dismountsRidersAfterDamage(
                ShipEntitySpecs.getByEggMeta(51))
                || !LegacyShipBehaviorCatalog.dismountsRidersAfterDamage(
                ShipEntitySpecs.getByEggMeta(54))) {
            helper.fail("CA Hime and Hibiki should preserve their distinct legacy runtime boundaries");
            return;
        }

        if (!LegacyShipBehaviorCatalog.dismountsRidersAfterDamage(
                ShipEntitySpecs.getByEggMeta(53))
                || !LegacyShipBehaviorCatalog.dismountsRidersAfterDamage(
                ShipEntitySpecs.getByEggMeta(55))
                || !LegacyShipBehaviorCatalog.dismountsRidersAfterDamage(
                ShipEntitySpecs.getByEggMeta(56))) {
            helper.fail("the six-destroyer formation should keep every legacy post-damage dismount override");
            return;
        }

        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void hostileConstructorModelStatesKeepLegacyDefaults(GameTestHelper helper) {
        int[] hostileEggMetas = {2039, 2048, 2049, 2050, 2060};
        int[] fixedStates = {3, 15, -1, -1, -1};
        int[] randomBounds = {0, 0, 128, 128, 16};

        for (int index = 0; index < hostileEggMetas.length; index++) {
            LegacyShipEntity ship = ModEntityTypes.LEGACY_SHIP.get().create(helper.getLevel());
            if (ship == null) {
                helper.fail("hostile constructor-state ships should be creatable");
                return;
            }

            ship.applySpawnSpec(ShipEntitySpecs.getByEggMeta(hostileEggMetas[index]), null);
            int state = ship.getModelState();
            if (fixedStates[index] >= 0
                    ? state != fixedStates[index]
                    : state < 0 || state >= randomBounds[index]) {
                helper.fail("hostile constructor model state should preserve the legacy Mob initializer: egg="
                        + hostileEggMetas[index] + ", state=" + state);
                return;
            }
        }

        LegacyShipEntity friendly = ModEntityTypes.LEGACY_SHIP.get().create(helper.getLevel());
        if (friendly == null) {
            helper.fail("friendly constructor-state ship should be creatable");
            return;
        }
        friendly.applySpawnSpec(ShipEntitySpecs.getByEggMeta(49), null);
        if (friendly.getModelState() != LegacyShipEntity.DEFAULT_MODEL_STATE) {
            helper.fail("friendly carrier should retain the base legacy constructor model state");
            return;
        }

        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void hostileDestroyerAndKongouModelStatesKeepLegacyDefaults(GameTestHelper helper) {
        int[] hostileEggMetas = {2053, 2054, 2062, 2063, 2064};
        int[] upperBounds = {16, 32, 16, 4, 4};
        int[] minimumStates = {0, 0, 7, 1, 1};

        for (int index = 0; index < hostileEggMetas.length; index++) {
            LegacyShipEntity ship = ModEntityTypes.LEGACY_SHIP.get().create(helper.getLevel());
            if (ship == null) {
                helper.fail("hostile destroyer/battleship state ships should be creatable");
                return;
            }

            ship.applySpawnSpec(ShipEntitySpecs.getByEggMeta(hostileEggMetas[index]), null);
            int state = ship.getModelState();
            if (state < minimumStates[index] || state >= upperBounds[index]
                    || (hostileEggMetas[index] == 2062 && state != 7 && state != 15)
                    || ((hostileEggMetas[index] == 2063 || hostileEggMetas[index] == 2064)
                    && state != 1 && state != 3)) {
                helper.fail("hostile constructor model state should preserve the legacy Mob initializer: egg="
                        + hostileEggMetas[index] + ", state=" + state);
                return;
            }
        }

        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void hostileRaidenAndLightCruiserModelStatesKeepLegacyDefaults(GameTestHelper helper) {
        int[] hostileEggMetas = {2065, 2055, 2056, 2058, 2059};
        int[] upperBounds = {8, 4, 2, 32, 8};
        int[] minimumStates = {5, 0, 0, 0, 0};

        for (int index = 0; index < hostileEggMetas.length; index++) {
            LegacyShipEntity ship = ModEntityTypes.LEGACY_SHIP.get().create(helper.getLevel());
            if (ship == null) {
                helper.fail("hostile raiden/light cruiser state ships should be creatable");
                return;
            }

            ship.applySpawnSpec(ShipEntitySpecs.getByEggMeta(hostileEggMetas[index]), null);
            int state = ship.getModelState();
            if (state < minimumStates[index] || state >= upperBounds[index]
                    || (hostileEggMetas[index] == 2065 && state != 5 && state != 7)) {
                helper.fail("hostile constructor model state should preserve the legacy Mob initializer: egg="
                        + hostileEggMetas[index] + ", state=" + state);
                return;
            }
        }

        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void hostileShimakazeSubmarineAndTakaoModelStatesKeepLegacyDefaults(GameTestHelper helper) {
        int[] hostileEggMetas = {2038, 2040, 2041, 2061};
        int[] upperBounds = {64, 8, 8, 16};

        for (int index = 0; index < hostileEggMetas.length; index++) {
            LegacyShipEntity ship = ModEntityTypes.LEGACY_SHIP.get().create(helper.getLevel());
            if (ship == null) {
                helper.fail("hostile Shimakaze/submarine/Takao state ships should be creatable");
                return;
            }

            ship.applySpawnSpec(ShipEntitySpecs.getByEggMeta(hostileEggMetas[index]), null);
            int state = ship.getModelState();
            if (state < 0 || state >= upperBounds[index]) {
                helper.fail("hostile constructor model state should preserve the legacy Mob initializer: egg="
                        + hostileEggMetas[index] + ", state=" + state);
                return;
            }
        }

        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void eleventhFiveSubmarineClassesKeepLegacyBoundaries(GameTestHelper helper) {
        int[] friendlyEggMetas = {19, 20, 21, 40, 41};
        int[] expectedClassIds = {17, 18, 19, 38, 39};
        int[] legacyModelStates = {0, 1, 0, 0, 0};
        double[] standingOffsets = {0.69D, 0.58D, 0.69D, 0.58D, 0.6D};
        double[] normalSittingOffsets = {0.0D, 0.42D, 0.0D, 0.0D, 0.14D};
        double[] boredSittingOffsets = {0.25D, 0.55D, 0.48D, 0.0D, 0.05D};

        for (int index = 0; index < friendlyEggMetas.length; index++) {
            ShipEntitySpec spec = ShipEntitySpecs.getByEggMeta(friendlyEggMetas[index]);
            EntityDimensions dimensions = LegacyShipBehaviorCatalog.dimensionsFor(spec, 0);
            if (spec.hostile()
                    || spec.legacyClassId() != expectedClassIds[index]
                    || Math.abs(dimensions.width - 0.6F) > 1.0E-6F
                    || Math.abs(dimensions.height - (index < 3 ? 1.8F : 1.4F)) > 1.0E-6F
                    || !LegacyShipBehaviorCatalog.supportsItemPickup(spec)) {
                helper.fail("the five friendly submarine classes should preserve their legacy role and dimensions");
                return;
            }

            LegacyShipEntity ship = ModEntityTypes.LEGACY_SHIP.get().create(helper.getLevel());
            if (ship == null) {
                helper.fail("the five friendly submarine classes should be creatable");
                return;
            }
            ship.setVariantEggMeta(friendlyEggMetas[index]);
            ship.setModelState(legacyModelStates[index]);
            ship.setOrderedToSit(false);
            if (Math.abs(ship.getPassengersRidingOffset()
                    - ship.getBbHeight() * standingOffsets[index]) > 1.0E-6D) {
                helper.fail("submarine standing riding offset should preserve the legacy class override: egg="
                        + friendlyEggMetas[index]);
                return;
            }

            ship.setOrderedToSit(true);
            ship.setEmotion(0);
            if (Math.abs(ship.getPassengersRidingOffset()
                    - ship.getBbHeight() * normalSittingOffsets[index]) > 1.0E-6D) {
                helper.fail("submarine normal sitting offset should preserve the legacy class override: egg="
                        + friendlyEggMetas[index]);
                return;
            }

            ship.setEmotion(4);
            if (Math.abs(ship.getPassengersRidingOffset()
                    - ship.getBbHeight() * boredSittingOffsets[index]) > 1.0E-6D) {
                helper.fail("submarine bored sitting offset should preserve the legacy class override: egg="
                        + friendlyEggMetas[index]);
                return;
            }
            ship.discard();
        }

        float[] expectedWidths = {0.6F, 0.9F, 1.2F, 1.5F};
        float[] expectedHeights = {1.4F, 2.8F, 4.2F, 5.6F};
        for (int scale = 0; scale < expectedWidths.length; scale++) {
            for (int eggMeta : new int[] {2040, 2041}) {
                EntityDimensions dimensions = LegacyShipBehaviorCatalog.dimensionsFor(
                        ShipEntitySpecs.getByEggMeta(eggMeta), scale);
                if (Math.abs(dimensions.width - expectedWidths[scale]) > 1.0E-6F
                        || Math.abs(dimensions.height - expectedHeights[scale]) > 1.0E-6F) {
                    helper.fail("hostile U-511/Ro-500 scale dimensions should match the legacy four bands: egg="
                            + eggMeta + ", scale=" + scale);
                    return;
                }
            }
        }

        if (LegacyShipBehaviorCatalog.hostileBossBarColorFor(ShipEntitySpecs.getByEggMeta(2040))
                        != BossEvent.BossBarColor.BLUE
                || LegacyShipBehaviorCatalog.hostileBossBarColorFor(ShipEntitySpecs.getByEggMeta(2041))
                        != BossEvent.BossBarColor.YELLOW
                || !LegacyShipBehaviorCatalog.shouldPlayRo500GaruruAmbient(39, 0)
                || LegacyShipBehaviorCatalog.shouldPlayRo500GaruruAmbient(39, 1)
                || LegacyShipBehaviorCatalog.shouldPlayRo500GaruruAmbient(38, 0)) {
            helper.fail("U-511/Ro-500 should preserve hostile boss colors and Ro-500 ambient sound gating");
            return;
        }

        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void twelfthFiveShipClassesKeepLegacyHitboxBoundaries(GameTestHelper helper) {
        int[] friendlyEggMetas = {53, 54, 55, 56};
        int[] expectedClassIds = {51, 52, 53, 54};
        BossEvent.BossBarColor[] expectedBossColors = {
                BossEvent.BossBarColor.PURPLE,
                BossEvent.BossBarColor.WHITE,
                BossEvent.BossBarColor.PINK,
                BossEvent.BossBarColor.PINK
        };

        for (int index = 0; index < friendlyEggMetas.length; index++) {
            ShipEntitySpec spec = ShipEntitySpecs.getByEggMeta(friendlyEggMetas[index]);
            EntityDimensions dimensions = LegacyShipBehaviorCatalog.dimensionsFor(spec, 0);
            if (spec.hostile()
                    || spec.legacyClassId() != expectedClassIds[index]
                    || Math.abs(dimensions.width - 0.5F) > 1.0E-6F
                    || Math.abs(dimensions.height - 1.5F) > 1.0E-6F) {
                helper.fail("the four friendly six-destroyer variants should keep the legacy 0.5 x 1.5 hitbox");
                return;
            }

            ShipEntitySpec hostileSpec = ShipEntitySpecs.getByEggMeta(2000 + friendlyEggMetas[index]);
            if (!hostileSpec.hostile()
                    || hostileSpec.legacyClassId() != 2000 + expectedClassIds[index]
                    || LegacyShipBehaviorCatalog.hostileBossBarColorFor(hostileSpec) != expectedBossColors[index]) {
                helper.fail("the four hostile six-destroyer variants should preserve their legacy class and boss color");
                return;
            }
        }

        float[] expectedWidths = {0.5F, 0.9F, 1.3F, 1.7F};
        float[] expectedHeights = {1.5F, 3.0F, 4.5F, 6.0F};
        for (int scale = 0; scale < expectedWidths.length; scale++) {
            for (int eggMeta : new int[] {2053, 2054, 2055, 2056}) {
                EntityDimensions dimensions = LegacyShipBehaviorCatalog.dimensionsFor(
                        ShipEntitySpecs.getByEggMeta(eggMeta), scale);
                if (Math.abs(dimensions.width - expectedWidths[scale]) > 1.0E-6F
                        || Math.abs(dimensions.height - expectedHeights[scale]) > 1.0E-6F) {
                    helper.fail("hostile six-destroyer scale dimensions should match the legacy four bands: egg="
                            + eggMeta + ", scale=" + scale);
                    return;
                }
            }
        }

        ShipEntitySpec carrierWd = ShipEntitySpecs.getByEggMeta(35);
        LegacyShipAttackProfile carrierProfile = LegacyShipBehaviorCatalog.attackProfile(carrierWd);
        EntityDimensions carrierDimensions = LegacyShipBehaviorCatalog.dimensionsFor(carrierWd, 0);
        if (carrierWd.hostile()
                || carrierWd.legacyClassId() != 33
                || Math.abs(carrierDimensions.width - 0.7F) > 1.0E-6F
                || Math.abs(carrierDimensions.height - 1.9F) > 1.0E-6F
                || LegacyShipBehaviorCatalog.equipmentTypeFor(carrierWd) != 3
                || LegacyShipBehaviorCatalog.foodSaturationMaxFor(carrierWd) != 30
                || carrierProfile.heavy()
                || !carrierProfile.hasAirAttack()
                || !LegacyShipBehaviorCatalog.managesAircraftInventory(carrierWd)
                || Math.abs(LegacyShipBehaviorCatalog.aircraftLaunchHeightFactorFor(
                        carrierWd, LegacyShipAttackKind.AIR_LIGHT) - 1.2F) > 1.0E-6F) {
            helper.fail("Carrier WD should preserve the legacy class 33 carrier, supply, attack, inventory, and launch boundaries");
            return;
        }

        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void remainingFriendlyShipHooksMatchLegacyOverrides(GameTestHelper helper) {
        if (LegacyShipBehaviorCatalog.rawDodgeBonusFor(57, false, true) != 0.15F
                || LegacyShipBehaviorCatalog.rawCriticalBonusFor(57, false, true) != 0.15F
                || LegacyShipBehaviorCatalog.rawDodgeBonusFor(57, false, false) != 0.0F
                || LegacyShipBehaviorCatalog.rawCriticalBonusFor(57, true, true) != 0.0F) {
            helper.fail("Tatsuta should only receive the legacy night dodge and critical bonuses");
            return;
        }
        if (!LegacyShipBehaviorCatalog.appliesAttackerSlowdown(58, false)
                || !LegacyShipBehaviorCatalog.appliesAttackerSlowdown(59, false)
                || !LegacyShipBehaviorCatalog.appliesAttackerSlowdown(58, true)
                || !LegacyShipBehaviorCatalog.appliesAttackerSlowdown(59, true)
                || LegacyShipBehaviorCatalog.appliesAttackerSlowdown(60, false)) {
            helper.fail("only Atago and Takao, friendly or hostile, should apply the legacy attacker slowdown");
            return;
        }

        LegacyShipStats haruna = LegacyShipStats.create(
                62, ShipArchetype.BATTLESHIP, false, 50, 6000,
                0, 0, 0, 0, 0, 0, false,
                TeitokuData.DEFAULT_FORMATION_ID, ShipEquipmentProfile.EMPTY, List.of());
        LegacyShipStats kirishima = LegacyShipStats.create(
                63, ShipArchetype.BATTLESHIP, false, 50, 6000,
                0, 0, 0, 0, 0, 0, false,
                TeitokuData.DEFAULT_FORMATION_ID, ShipEquipmentProfile.EMPTY, List.of());
        LegacyShipStats hostileHaruna = LegacyShipStats.create(
                62, ShipArchetype.BATTLESHIP, true, 50, 6000,
                0, 0, 0, 0, 0, 0, false,
                TeitokuData.DEFAULT_FORMATION_ID, ShipEquipmentProfile.EMPTY, List.of());
        LegacyShipStats hostileKirishima = LegacyShipStats.create(
                63, ShipArchetype.BATTLESHIP, true, 50, 6000,
                0, 0, 0, 0, 0, 0, false,
                TeitokuData.DEFAULT_FORMATION_ID, ShipEquipmentProfile.EMPTY, List.of());
        LegacyShipStats ikazuchi = LegacyShipStats.create(
                53, ShipArchetype.DESTROYER, false, 50, 6000,
                0, 0, 0, 0, 0, 0, false,
                TeitokuData.DEFAULT_FORMATION_ID, ShipEquipmentProfile.EMPTY, List.of());
        LegacyShipStats hostileIkazuchi = LegacyShipStats.create(
                53, ShipArchetype.DESTROYER, true, 50, 6000,
                0, 0, 0, 0, 0, 0, false,
                TeitokuData.DEFAULT_FORMATION_ID, ShipEquipmentProfile.EMPTY, List.of());
        if (Math.abs(kirishima.meleeDamage() - haruna.meleeDamage() * 12.0F) > 0.001F
                || Math.abs(hostileKirishima.meleeDamage() - hostileHaruna.meleeDamage() * 12.0F) > 0.001F
                || LegacyShipBehaviorCatalog.meleeDamageMultiplierFor(63, false) != 12.0F
                || LegacyShipBehaviorCatalog.meleeDamageMultiplierFor(2063, true) != 12.0F
                || Math.abs(ikazuchi.meleeDamage() - ikazuchi.attackLight()) > 0.001F
                || Math.abs(hostileIkazuchi.meleeDamage() - hostileIkazuchi.attackLight() * 0.125F) > 0.001F
                || LegacyShipBehaviorCatalog.meleeDamageMultiplierFor(53, false) != 8.0F
                || LegacyShipBehaviorCatalog.meleeDamageMultiplierFor(2053, true) != 1.0F) {
            helper.fail("Kirishima and Ikazuchi should preserve each legacy class's melee damage override");
            return;
        }

        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void legacyEmotionTickUpdatesFiveShipFamilies(GameTestHelper helper) {
        int[] eggMetas = {60, 61, 62, 63, 64};
        float[] healthRatios = {1.0F, 0.75F, 0.5F, 0.25F, 1.0F};
        int[] expectedHpStates = {0, 1, 2, 3, 0};

        for (int index = 0; index < eggMetas.length; index++) {
            LegacyShipEntity ship = ModEntityTypes.LEGACY_SHIP.get().create(helper.getLevel());
            if (ship == null) {
                helper.fail("Atago, Takao, and Kongou-class ships should be creatable for emotion tests");
                return;
            }

            ship.setVariantEggMeta(eggMetas[index]);
            ship.setNoAi(true);
            ship.setPos(2.0D + index * 2.0D, 2.0D, 2.0D);
            ship.setHealth(ship.getMaxHealth() * healthRatios[index]);
            ship.setGrudge(index == 4 ? 0 : 1000);
            ship.setEmotion(LegacyShipEntity.DEFAULT_EMOTION);
            ship.setEmotion4(index == 0 ? 4 : LegacyShipEntity.DEFAULT_EMOTION4);
            helper.getLevel().addFreshEntity(ship);

            LegacyShipBehaviorCatalog.tickLegacyEmotionState(ship);
            if (ship.getHpState() != expectedHpStates[index]) {
                helper.fail("legacy HPState mismatch for egg=" + eggMetas[index]
                        + ": got=" + ship.getHpState() + ", expected=" + expectedHpStates[index]);
                return;
            }
            if (index == 0 && ship.getEmotion4() != LegacyShipEntity.DEFAULT_EMOTION) {
                helper.fail("friendly Atago should reset Emotion4 to NORMAL on a healthy update");
                return;
            }
            if (index == 3 && ship.getEmotion() != 2) {
                helper.fail("healthy-fuel-independent low HP should set Emotion to T_T");
                return;
            }
            if (index == 4 && ship.getEmotion() != 5) {
                helper.fail("zero fuel should set Emotion to HUNGRY");
                return;
            }
        }

        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void himeMeleeDamageOverridesMatchLegacyClasses(GameTestHelper helper) {
        int[] classIds = {20, 21, 26, 27, 28};
        float[] expectedRatiosToAttackLight = {2.0F, 1.0F, 3.0F, 1.0F, 1.0F};
        for (int index = 0; index < classIds.length; index++) {
            int classId = classIds[index];
            LegacyShipStats stats = LegacyShipStats.create(
                    classId, ShipArchetype.PRINCESS, true, 50, 6000,
                    0, 0, 0, 0, 0, 0, false,
                    TeitokuData.DEFAULT_FORMATION_ID, ShipEquipmentProfile.EMPTY, List.of());
            if (LegacyShipBehaviorCatalog.meleeDamageMultiplierFor(classId, true)
                    != expectedRatiosToAttackLight[index] * 8.0F
                    || Math.abs(stats.meleeDamage() - stats.attackLight() * expectedRatiosToAttackLight[index])
                    > 0.001F) {
                helper.fail("legacy hime class " + classId
                        + " should preserve its legacy attackDamage melee ratio");
                return;
            }
        }

        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void northernCarrierWdAndSsnHimeMeleeDamageMatchLegacyClasses(GameTestHelper helper) {
        int[] classIds = {31, 33, 72};
        boolean[] hostileVariants = {true, false, true};
        ShipArchetype[] archetypes = {
                ShipArchetype.PRINCESS, ShipArchetype.CARRIER, ShipArchetype.PRINCESS
        };
        for (int index = 0; index < classIds.length; index++) {
            LegacyShipStats stats = LegacyShipStats.create(
                    classIds[index], archetypes[index], hostileVariants[index], 50, 6000,
                    0, 0, 0, 0, 0, 0, false,
                    TeitokuData.DEFAULT_FORMATION_ID, ShipEquipmentProfile.EMPTY, List.of());
            if (LegacyShipBehaviorCatalog.meleeDamageMultiplierFor(
                    classIds[index], hostileVariants[index]) != 8.0F
                    || Math.abs(stats.meleeDamage() - stats.attackLight()) > 0.001F) {
                helper.fail("legacy hime class " + classIds[index]
                        + " should use full attackDamage for default melee damage");
                return;
            }
        }

        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void battleshipAndLightCruiserMeleeOverridesMatchLegacyClasses(GameTestHelper helper) {
        int[] classIds = {13, 37, 37, 56, 56, 57, 57};
        boolean[] hostileVariants = {false, false, true, false, true, false, true};
        ShipArchetype[] archetypes = {
                ShipArchetype.BATTLESHIP, ShipArchetype.BATTLESHIP, ShipArchetype.BATTLESHIP,
                ShipArchetype.CRUISER, ShipArchetype.CRUISER,
                ShipArchetype.CRUISER, ShipArchetype.CRUISER
        };
        float[] expectedMultipliers = {16.0F, 24.0F, 1.0F, 16.0F, 8.0F, 16.0F, 8.0F};

        for (int index = 0; index < classIds.length; index++) {
            LegacyShipStats stats = LegacyShipStats.create(
                    classIds[index], archetypes[index], hostileVariants[index], 50, 6000,
                    0, 0, 0, 0, 0, 0, false,
                    TeitokuData.DEFAULT_FORMATION_ID, ShipEquipmentProfile.EMPTY, List.of());
            if (LegacyShipBehaviorCatalog.meleeDamageMultiplierFor(
                    classIds[index], hostileVariants[index]) != expectedMultipliers[index]
                    || Math.abs(stats.meleeDamage() - stats.attackLight() * expectedMultipliers[index] / 8.0F)
                    > 0.001F) {
                helper.fail("legacy class " + classIds[index] + " hostile=" + hostileVariants[index]
                        + " should preserve its explicit melee damage override");
                return;
            }
        }

        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void transportWaKeepsLegacyCargoAndFleetSupplyRules(GameTestHelper helper) {
        UUID owner = UUID.randomUUID();
        LegacyShipEntity transport = ModEntityTypes.LEGACY_SHIP.get().create(helper.getLevel());
        LegacyShipEntity target = ModEntityTypes.LEGACY_SHIP.get().create(helper.getLevel());
        LegacyShipEntity otherOwner = ModEntityTypes.LEGACY_SHIP.get().create(helper.getLevel());
        if (transport == null || target == null || otherOwner == null) {
            helper.fail("Transport Wa and escorts should be creatable for cargo supply tests");
            return;
        }

        transport.setVariantEggMeta(18);
        transport.setOwner(owner, "TransportWaTester", 91);
        transport.setShipLevel(50);
        transport.setGrudge(5000);
        transport.setLightAmmo(500);
        transport.setHeavyAmmo(250);
        transport.getShipInventory().setItem(LegacyShipEntity.EQUIPMENT_SLOT_COUNT,
                new ItemStack(ModItems.GRUDGE.get()));
        transport.getShipInventory().setItem(LegacyShipEntity.EQUIPMENT_SLOT_COUNT + 1,
                new ItemStack(ModItems.AMMO.get()));
        transport.getShipInventory().setItem(LegacyShipEntity.EQUIPMENT_SLOT_COUNT + 2,
                new ItemStack(ModItems.AMMO2.get()));
        helper.getLevel().addFreshEntity(transport);

        transport.tickCount = 128;
        LegacyShipBehaviorCatalog.tickLegacyPerShipBehavior(transport);
        int transportGrudgeGain = (int) (300F * transport.getLegacyStats().get(LegacyShipStatTables.Attr.GRUDGE));
        int transportLightAmmoGain = (int) (30F * transport.getLegacyStats().get(LegacyShipStatTables.Attr.AMMO));
        int transportHeavyAmmoGain = (int) (15F * transport.getLegacyStats().get(LegacyShipStatTables.Attr.AMMO));
        if (transport.getGrudge() != 5000 + transportGrudgeGain
                || transport.getLightAmmo() != 500 + transportLightAmmoGain
                || transport.getHeavyAmmo() != 250 + transportHeavyAmmoGain
                || !transport.getShipInventory().getItem(LegacyShipEntity.EQUIPMENT_SLOT_COUNT).isEmpty()
                || !transport.getShipInventory().getItem(LegacyShipEntity.EQUIPMENT_SLOT_COUNT + 1).isEmpty()
                || !transport.getShipInventory().getItem(LegacyShipEntity.EQUIPMENT_SLOT_COUNT + 2).isEmpty()) {
            helper.fail("Transport Wa should consume one legacy grudge, light-ammo, and heavy-ammo item at 128 ticks");
            return;
        }

        target.setVariantEggMeta(2);
        target.setOwner(owner, "TransportWaTester", 91);
        target.setGrudge(2600);
        target.setLightAmmo(260);
        target.setHeavyAmmo(134);
        target.setPos(transport.getX() + 3.0D, transport.getY(), transport.getZ());
        helper.getLevel().addFreshEntity(target);

        otherOwner.setVariantEggMeta(2);
        otherOwner.setOwner(UUID.randomUUID(), "OtherTransportOwner", 92);
        otherOwner.setGrudge(2600);
        otherOwner.setLightAmmo(260);
        otherOwner.setHeavyAmmo(134);
        otherOwner.setPos(transport.getX() + 3.5D, transport.getY(), transport.getZ());
        helper.getLevel().addFreshEntity(otherOwner);

        transport.setGrudge(6000);
        transport.setLightAmmo(540);
        transport.setHeavyAmmo(270);
        int experienceBefore = transport.getShipExperience();
        transport.tickCount = 256;
        LegacyShipBehaviorCatalog.tickLegacyPerShipBehavior(transport);
        int expectedTargetGrudge = 2600
                + (int) (5400F * target.getLegacyStats().get(LegacyShipStatTables.Attr.GRUDGE));
        int expectedTargetLightAmmo = 260
                + (int) (540F * target.getLegacyStats().get(LegacyShipStatTables.Attr.AMMO));
        int expectedTargetHeavyAmmo = 134
                + (int) (270F * target.getLegacyStats().get(LegacyShipStatTables.Attr.AMMO));
        int expectedSupplyExperience = experienceBefore
                + (int) (40F * transport.getLegacyStats().get(LegacyShipStatTables.Attr.XP));
        if (transport.getGrudge() != 600
                || transport.getLightAmmo() != 0
                || transport.getHeavyAmmo() != 0
                || target.getGrudge() != expectedTargetGrudge
                || target.getLightAmmo() != expectedTargetLightAmmo
                || target.getHeavyAmmo() != expectedTargetHeavyAmmo
                || otherOwner.getGrudge() != 2600
                || transport.getShipExperience() != expectedSupplyExperience) {
            helper.fail("Transport Wa should supply one same-owner escort with all deficient resources and preserve owner isolation");
            return;
        }

        target.setGrudge(2600);
        target.setLightAmmo(260);
        target.setHeavyAmmo(134);
        transport.setGrudge(6000);
        transport.setLightAmmo(540);
        transport.setHeavyAmmo(270);
        transport.setGrudge(0);
        transport.tickCount = 512;
        LegacyShipBehaviorCatalog.tickLegacyPerShipBehavior(transport);
        if (target.getGrudge() != 2600 || target.getLightAmmo() != 260 || target.getHeavyAmmo() != 134
                || transport.getGrudge() != 0 || transport.getLightAmmo() != 540 || transport.getHeavyAmmo() != 270) {
            helper.fail("Transport Wa should not supply a fleet while out of fuel");
            return;
        }

        transport.discard();
        target.discard();
        otherOwner.discard();
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void allActiveShipClassesKeepLegacyIdleSupplyCosts(GameTestHelper helper) {
        int[] eggMetas = {
                2, 3, 4, 5, 11, 12, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23,
                28, 29, 30, 31, 32, 33, 35, 38, 39, 40, 41, 46, 48, 49, 50, 51,
                53, 54, 55, 56, 58, 59, 60, 61, 62, 63, 64, 65, 74
        };
        int[] expectedIdleCosts = {
                5, 5, 5, 5, 8, 8, 12, 15, 15, 14, 3, 4, 4, 4, 12, 14,
                15, 5, 14, 14, 14, 14, 12, 5, 15, 4, 4, 4, 15, 12, 12, 8,
                5, 5, 5, 5, 7, 7, 8, 8, 15, 15, 15, 15, 4
        };
        if (eggMetas.length != 45 || expectedIdleCosts.length != eggMetas.length) {
            helper.fail("the active BasicEntityShip idle-supply audit must contain all 45 registered classes");
            return;
        }

        for (int index = 0; index < eggMetas.length; index++) {
            ShipEntitySpec spec = ShipEntitySpecs.getByEggMeta(eggMetas[index]);
            int actual = LegacyShipBehaviorCatalog.legacyIdleGrudgeConsumptionFor(spec);
            if (spec.legacyClassId() != eggMetas[index] - 2
                    || actual != expectedIdleCosts[index]) {
                helper.fail("legacy idle grudge mapping diverged for egg meta " + eggMetas[index]
                        + ": class=" + spec.legacyClassId() + ", actual=" + actual
                        + ", expected=" + expectedIdleCosts[index]);
                return;
            }
        }

        if (LegacyShipBehaviorCatalog.legacyIdleGrudgeConsumptionFor(
                ShipEntitySpecs.getByEggMeta(2038)) >= 0
                || LegacyShipBehaviorCatalog.legacyMovementExperienceFor(
                ShipEntitySpecs.getByEggMeta(18), 15, 200) != 0
                || LegacyShipBehaviorCatalog.legacyMovementExperienceFor(
                ShipEntitySpecs.getByEggMeta(18), 15, 201) != 3) {
            helper.fail("hostile exclusion or Transport Wa movement experience diverged from legacy");
            return;
        }

        LegacyShipEntity ship = ModEntityTypes.LEGACY_SHIP.get().create(helper.getLevel());
        if (ship == null) {
            helper.fail("a previously uncovered BasicEntityShip class must be creatable for the shared tick regression");
            return;
        }
        ship.setVariantEggMeta(28);
        ship.setNoAi(true);
        ship.setNoGravity(true);
        ship.setGrudge(1000);
        ship.setLightAmmo(100);
        ship.setHeavyAmmo(100);
        ship.setMorale(10000);
        BlockPos shipPos = helper.absolutePos(new BlockPos(2, 2, 2));
        ship.setPos(shipPos.getX() + 0.5D, shipPos.getY(), shipPos.getZ() + 0.5D);
        helper.getLevel().addFreshEntity(ship);
        ship.tickCount = 128;
        ship.tick();
        if (ship.getGrudge() != 985 || ship.getMorale() != 9984) {
            helper.fail("a previously uncovered Battleship Hime must execute the shared 128-tick idle path: grudge="
                    + ship.getGrudge() + ", morale=" + ship.getMorale());
            ship.discard();
            return;
        }
        ship.discard();
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void ship03TransportSubmarinesAndCarrierHimeKeepLegacyCombatBoundaries(GameTestHelper helper) {
        int[] eggMetas = {18, 19, 20, 21, 22};
        int[] classIds = {16, 17, 18, 19, 20};
        int[] idleGrudgeCosts = {3, 4, 4, 4, 12};
        int[] ammoCosts = {1, 1, 1, 1, 3};
        boolean[][] attackChannels = {
                {false, false, false}, {true, true, false}, {true, true, false},
                {true, true, false}, {false, false, true}
        };

        for (int index = 0; index < eggMetas.length; index++) {
            ShipEntitySpec spec = ShipEntitySpecs.getByEggMeta(eggMetas[index]);
            LegacyShipAttackProfile profile = LegacyShipBehaviorCatalog.attackProfile(spec);
            boolean expectedPickup = index < 4;
            int expectedPickupRange = index == 0 ? 8 : 4;
            if (spec.legacyClassId() != classIds[index]
                    || profile.light() != attackChannels[index][0]
                    || profile.heavy() != attackChannels[index][1]
                    || profile.hasAirAttack() != attackChannels[index][2]
                    || LegacyShipBehaviorCatalog.ammoConsumptionFor(spec) != ammoCosts[index]
                    || LegacyShipBehaviorCatalog.legacyIdleGrudgeConsumptionFor(spec)
                    != idleGrudgeCosts[index]
                    || LegacyShipBehaviorCatalog.supportsItemPickup(spec) != expectedPickup
                    || expectedPickup
                    && LegacyShipBehaviorCatalog.pickupRangeBaseFor(spec) != expectedPickupRange) {
                helper.fail("SHIP-03 attack, resource, idle, or pickup boundary changed for egg meta "
                        + eggMetas[index]);
                return;
            }
        }

        ShipEntitySpec submarineYo = ShipEntitySpecs.getByEggMeta(20);
        if (!LegacyShipBehaviorCatalog.attackProfile(submarineYo)
                .projectileProfile(LegacyShipAttackKind.LIGHT).isPresent()) {
            helper.fail("Submarine Yo should keep its legacy light missile launch path");
            return;
        }

        ShipEntitySpec carrierHime = ShipEntitySpecs.getByEggMeta(22);
        if (carrierHime.hostile()
                || LegacyShipBehaviorCatalog.equipmentTypeFor(carrierHime) != 3
                || LegacyShipBehaviorCatalog.foodSaturationMaxFor(carrierHime) != 24
                || !LegacyShipBehaviorCatalog.managesAircraftInventory(carrierHime)
                || LegacyShipBehaviorCatalog.aircraftLightCapacityFor(carrierHime, 50, 0) != 18
                || LegacyShipBehaviorCatalog.aircraftHeavyCapacityFor(carrierHime, 50, 0) != 9
                || Math.abs(LegacyShipBehaviorCatalog.aircraftLaunchHeightFactorFor(
                carrierHime, LegacyShipAttackKind.AIR_LIGHT) - 0.9F) > 1.0E-6F
                || Math.abs(LegacyShipBehaviorCatalog.aircraftLaunchHeightFactorFor(
                carrierHime, LegacyShipAttackKind.AIR_HEAVY) - 0.9F) > 1.0E-6F) {
            helper.fail("Carrier Hime should keep the legacy finite-aircraft and 0.9 launch-height rules");
            return;
        }

        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void ordinaryDeepSeaConstructorsKeepLegacyDimensionsAndOffsets(GameTestHelper helper) {
        int[] eggMetas = {2, 3, 4, 5, 11, 12, 14, 15, 16, 17, 18};
        float[] widths = {0.9F, 0.9F, 0.9F, 0.9F, 0.75F, 0.6F, 0.7F, 0.7F, 0.7F, 0.6F, 0.7F};
        float[] heights = {1.7F, 1.7F, 1.7F, 1.9F, 1.7F, 1.3F, 1.9F, 1.8F, 1.8F, 1.55F, 1.53F};

        for (int index = 0; index < eggMetas.length; index++) {
            ShipEntitySpec spec = ShipEntitySpecs.getByEggMeta(eggMetas[index]);
            EntityDimensions dimensions = LegacyShipBehaviorCatalog.dimensionsFor(spec, 3);
            if (Math.abs(dimensions.width - widths[index]) > 1.0E-6F
                    || Math.abs(dimensions.height - heights[index]) > 1.0E-6F) {
                helper.fail("ordinary deep-sea constructor dimensions changed for egg meta " + eggMetas[index]);
                return;
            }
        }

        LegacyShipEntity destroyerNi = ModEntityTypes.LEGACY_SHIP.get().create(helper.getLevel());
        LegacyShipEntity carrierWo = ModEntityTypes.LEGACY_SHIP.get().create(helper.getLevel());
        LegacyShipEntity battleshipRu = ModEntityTypes.LEGACY_SHIP.get().create(helper.getLevel());
        LegacyShipEntity battleshipRe = ModEntityTypes.LEGACY_SHIP.get().create(helper.getLevel());
        LegacyShipEntity transportWa = ModEntityTypes.LEGACY_SHIP.get().create(helper.getLevel());
        if (destroyerNi == null || carrierWo == null || battleshipRu == null
                || battleshipRe == null || transportWa == null) {
            helper.fail("ordinary deep-sea variants should be creatable for constructor compatibility");
            return;
        }

        destroyerNi.setVariantEggMeta(5);
        if (Math.abs(destroyerNi.getBbWidth() - 0.9F) > 1.0E-6F
                || Math.abs(destroyerNi.getBbHeight() - 1.9F) > 1.0E-6F
                || Math.abs(destroyerNi.maxUpStep() - 2.0F) > 1.0E-6F) {
            helper.fail("Destroyer Ni should keep its 1.12 size and two-block step height");
            return;
        }

        carrierWo.setVariantEggMeta(14);
        carrierWo.setModelState(1);
        carrierWo.setEmotion(0);
        if (Math.abs(carrierWo.getPassengersRidingOffset() - carrierWo.getBbHeight() * 1.21D) > 1.0E-6D) {
            helper.fail("Carrier Wo standing offset should keep the legacy equipped-model factor");
            return;
        }
        carrierWo.setOrderedToSit(true);
        if (Math.abs(carrierWo.getPassengersRidingOffset() - carrierWo.getBbHeight() * 0.43D) > 1.0E-6D) {
            helper.fail("Carrier Wo sitting offset should keep the legacy equipped-model factor");
            return;
        }
        carrierWo.setModelState(0);
        if (Math.abs(carrierWo.getPassengersRidingOffset() - carrierWo.getBbHeight() * 0.68D) > 1.0E-6D) {
            helper.fail("Carrier Wo unequipped sitting offset should keep the legacy factor");
            return;
        }

        battleshipRu.setVariantEggMeta(15);
        battleshipRu.setModelState(1);
        battleshipRu.setEmotion(0);
        battleshipRu.setEmotion4(4);
        battleshipRu.setOrderedToSit(true);
        if (Math.abs(battleshipRu.getPassengersRidingOffset()) > 1.0E-6D) {
            helper.fail("Battleship Ru should honor the legacy alternate bored sitting pose");
            return;
        }

        battleshipRe.setVariantEggMeta(17);
        battleshipRe.setModelState(1);
        battleshipRe.setEmotion(4);
        battleshipRe.setOrderedToSit(true);
        if (Math.abs(battleshipRe.getPassengersRidingOffset() - battleshipRe.getBbHeight() * 0.35D) > 1.0E-6D) {
            helper.fail("Battleship Re should keep the legacy bored sitting offset");
            return;
        }

        transportWa.setVariantEggMeta(18);
        transportWa.setModelState(2);
        transportWa.setOrderedToSit(true);
        if (Math.abs(transportWa.getPassengersRidingOffset() - transportWa.getBbHeight() * 0.5D) > 1.0E-6D) {
            helper.fail("Transport Wa should keep the legacy equipped sitting offset");
            return;
        }
        transportWa.setOrderedToSit(false);
        if (Math.abs(transportWa.getPassengersRidingOffset() - transportWa.getBbHeight() * 0.64D) > 1.0E-6D) {
            helper.fail("Transport Wa should keep the legacy standing offset");
            return;
        }

        if (LegacyShipBehaviorCatalog.rawCriticalBonusFor(9, false, true) != 0.15F
                || LegacyShipBehaviorCatalog.rawCriticalBonusFor(10, false, true) != 0.3F
                || LegacyShipBehaviorCatalog.rawCriticalBonusFor(13, false, false) != 0.05F
                || LegacyShipBehaviorCatalog.rawCriticalBonusFor(14, false, false) != 0.1F
                || LegacyShipBehaviorCatalog.rawMissBonusFor(9, false, true) != 0.15F
                || LegacyShipBehaviorCatalog.rawMissBonusFor(14, false, false) != 0.1F
                || LegacyShipBehaviorCatalog.rawDoubleHitBonusFor(13, false) != 0.05F
                || LegacyShipBehaviorCatalog.rawDoubleHitBonusFor(15, false) != 0.1F
                || LegacyShipBehaviorCatalog.rawTripleHitBonusFor(13, false, false) != 0.05F
                || LegacyShipBehaviorCatalog.rawTripleHitBonusFor(15, false, false) != 0.1F) {
            helper.fail("ordinary cruiser and battleship raw attribute overrides should match legacy values");
            return;
        }

        if (LegacyShipBehaviorCatalog.aircraftLaunchHeightFactorFor(
                ShipEntitySpecs.getByEggMeta(14), LegacyShipAttackKind.AIR_LIGHT) != 0.9F
                || LegacyShipBehaviorCatalog.aircraftLaunchHeightFactorFor(
                ShipEntitySpecs.getByEggMeta(17), LegacyShipAttackKind.AIR_LIGHT) != 0.8F) {
            helper.fail("Carrier Wo and Battleship Re should keep their legacy aircraft launch heights");
            return;
        }

        destroyerNi.discard();
        carrierWo.discard();
        battleshipRu.discard();
        battleshipRe.discard();
        transportWa.discard();
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void shimakazeNagatoYamatoRidingOffsetsMatchLegacyClasses(GameTestHelper helper) {
        int[] eggMetas = {38, 39, 48};
        double[] standingFactors = {0.67D, 0.75D, 0.75D};
        int[] equippedModelBits = {0, 2, 1};
        double[] equippedFactors = {0.16D, 0.42D, 0.5D};
        double[] normalSittingFactors = {0.16D, 0.35D, 0.4D};
        double[] boredSittingFactors = {-0.04D, 0.0D, 0.1D};

        for (int index = 0; index < eggMetas.length; index++) {
            LegacyShipEntity ship = ModEntityTypes.LEGACY_SHIP.get().create(helper.getLevel());
            if (ship == null) {
                helper.fail("Shimakaze, Nagato, and Yamato should be creatable for riding offset tests");
                return;
            }

            ship.setVariantEggMeta(eggMetas[index]);
            double height = ship.getBbHeight();
            if (Math.abs(ship.getPassengersRidingOffset() - height * standingFactors[index]) > 1.0E-6D) {
                helper.fail("standing riding offset should preserve the legacy factor: egg=" + eggMetas[index]);
                return;
            }

            ship.setOrderedToSit(true);
            ship.setModelState(equippedModelBits[index]);
            ship.setEmotion(0);
            if (Math.abs(ship.getPassengersRidingOffset() - height * equippedFactors[index]) > 1.0E-6D) {
                helper.fail("equipped sitting riding offset should preserve the legacy factor: egg=" + eggMetas[index]);
                return;
            }

            ship.setModelState(0);
            if (index == 0) {
                ship.setEmotion(0);
            }
            if (Math.abs(ship.getPassengersRidingOffset() - height * normalSittingFactors[index]) > 1.0E-6D) {
                helper.fail("normal sitting riding offset should preserve the legacy factor: egg=" + eggMetas[index]);
                return;
            }

            ship.setEmotion(4);
            if (Math.abs(ship.getPassengersRidingOffset() - height * boredSittingFactors[index]) > 1.0E-6D) {
                helper.fail("bored sitting riding offset should preserve the legacy factor: egg=" + eggMetas[index]);
                return;
            }
            ship.discard();
        }

        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void nagatoMoraleEventMatchesLegacyNearbyTargets(GameTestHelper helper) {
        LegacyShipEntity nagato = ModEntityTypes.LEGACY_SHIP.get().create(helper.getLevel());
        LegacyShipEntity destroyer = ModEntityTypes.LEGACY_SHIP.get().create(helper.getLevel());
        LegacyShipEntity northern = ModEntityTypes.LEGACY_SHIP.get().create(helper.getLevel());
        if (nagato == null || destroyer == null || northern == null) {
            helper.fail("Nagato morale event entities should be creatable");
            return;
        }

        BlockPos origin = helper.absolutePos(new BlockPos(8, 200, 8));
        nagato.setVariantEggMeta(39);
        nagato.setGrudge(1000);
        nagato.setMorale(7000);
        nagato.setNoGravity(true);
        nagato.setNoAi(true);
        nagato.setPos(origin.getX() + 0.5D, origin.getY(), origin.getZ() + 0.5D);
        destroyer.setVariantEggMeta(53);
        destroyer.setNoGravity(true);
        destroyer.setNoAi(true);
        destroyer.setPos(origin.getX() + 4.5D, origin.getY(), origin.getZ() + 0.5D);
        northern.setVariantEggMeta(33);
        northern.setNoGravity(true);
        northern.setNoAi(true);
        northern.setPos(origin.getX() + 0.5D, origin.getY(), origin.getZ() + 4.5D);
        helper.getLevel().addFreshEntity(nagato);
        helper.getLevel().addFreshEntity(destroyer);
        helper.getLevel().addFreshEntity(northern);

        nagato.tickCount = 128;
        nagato.tick();
        int actualMorale = nagato.getMorale();
        if (actualMorale < 7284 || (actualMorale - 6984) % 300 != 0) {
            helper.fail("Nagato should apply the legacy idle morale cost before its nearby-target bonus"
                    + "; actualMorale=" + actualMorale);
            return;
        }

        nagato.setMorale(7600);
        nagato.tickCount = 256;
        nagato.tick();
        int actualMorale2 = nagato.getMorale();
        if (actualMorale2 < 7885 || (actualMorale2 - 7585) % 300 != 0) {
            helper.fail("Nagato morale event should retain the legacy below-threshold gate; actualMorale2="
                    + actualMorale2);
            return;
        }

        nagato.discard();
        destroyer.discard();
        northern.discard();
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void nagatoMoraleEventKeepsLegacyOutOfCombatNavigationGate(GameTestHelper helper) {
        LegacyShipEntity nagato = ModEntityTypes.LEGACY_SHIP.get().create(helper.getLevel());
        LegacyShipEntity destroyer = ModEntityTypes.LEGACY_SHIP.get().create(helper.getLevel());
        if (nagato == null || destroyer == null) {
            helper.fail("Nagato navigation entities should be creatable");
            return;
        }

        BlockPos origin = helper.absolutePos(new BlockPos(8, 2, 8));
        for (int x = -2; x <= 6; x++) {
            for (int z = -1; z <= 1; z++) {
                helper.getLevel().setBlock(origin.offset(x, -1, z), Blocks.STONE.defaultBlockState(), Block.UPDATE_ALL);
            }
        }
        nagato.setVariantEggMeta(39);
        nagato.setGrudge(1000);
        nagato.setMorale(0);
        // PathNavigation refuses to create a path for a modern mob with NoAI;
        // the legacy test only needed to suppress autonomous goals.
        nagato.setNoAi(false);
        nagato.setOnGround(true);
        nagato.setPos(origin.getX() + 0.5D, origin.getY(), origin.getZ() + 0.5D);
        destroyer.setVariantEggMeta(53);
        destroyer.setNoAi(false);
        destroyer.setOnGround(true);
        destroyer.setPos(origin.getX() + 4.5D, origin.getY(), origin.getZ() + 0.5D);
        helper.getLevel().addFreshEntity(nagato);
        helper.getLevel().addFreshEntity(destroyer);

        boolean movedToNearbyTarget = false;
        for (long seed = 0L; seed < 1024L && !movedToNearbyTarget; seed++) {
            nagato.getNavigation().stop();
            nagato.getRandom().setSeed(seed);
            nagato.tickCount = 256;
            LegacyShipBehaviorCatalog.tickLegacyPerShipBehavior(nagato);
            movedToNearbyTarget = !nagato.getNavigation().isDone()
                    && nagato.getNavigation().getTargetPos() != null;
        }
        if (!movedToNearbyTarget) {
            helper.fail("Nagato should sometimes navigate toward a nearby destroyer after 128 ticks out of combat");
            return;
        }

        nagato.getNavigation().stop();
        nagato.markLegacyCombat();
        nagato.tickCount = 384;
        LegacyShipBehaviorCatalog.tickLegacyPerShipBehavior(nagato);
        if (!nagato.getNavigation().isDone()) {
            helper.fail("Nagato should not run the morale navigation event while within the legacy combat window");
            return;
        }

        nagato.clearCommandState();
        nagato.getNavigation().stop();
        nagato.tickCount = 512;
        nagato.commandMoveTo(origin.offset(8, 0, 0), helper.getLevel().dimension().location().toString());
        nagato.getRandom().setSeed(0L);
        LegacyShipBehaviorCatalog.tickLegacyPerShipBehavior(nagato);
        if (!nagato.getNavigation().isDone()) {
            helper.fail("explicit movement commands should retain navigation priority over Nagato's ambient morale event");
            return;
        }

        nagato.discard();
        destroyer.discard();
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void kongouClassRidingOffsetsMatchLegacyClasses(GameTestHelper helper) {
        int[] eggMetas = {62, 63, 64, 65};
        for (int eggMeta : eggMetas) {
            LegacyShipEntity ship = ModEntityTypes.LEGACY_SHIP.get().create(helper.getLevel());
            if (ship == null) {
                helper.fail("Kongou-class ships should be creatable for riding offset tests");
                return;
            }

            ship.setVariantEggMeta(eggMeta);
            double height = ship.getBbHeight();
            if (Math.abs(ship.getPassengersRidingOffset() - height * 0.75D) > 1.0E-6D) {
                helper.fail("standing Kongou-class ships should keep the legacy 0.75 riding factor: egg=" + eggMeta);
                return;
            }

            ship.setOrderedToSit(true);
            ship.setModelState(2);
            if (Math.abs(ship.getPassengersRidingOffset() - height * 0.42D) > 1.0E-6D) {
                helper.fail("head-equipped sitting Kongou-class ships should keep the legacy 0.42 riding factor: egg=" + eggMeta);
                return;
            }

            ship.setModelState(0);
            ship.setEmotion(0);
            if (Math.abs(ship.getPassengersRidingOffset() - height * 0.35D) > 1.0E-6D) {
                helper.fail("normal sitting Kongou-class ships should keep the legacy 0.35 riding factor: egg=" + eggMeta);
                return;
            }

            ship.setEmotion(4);
            if (ship.getPassengersRidingOffset() != 0.0D) {
                helper.fail("bored sitting Kongou-class ships should use the legacy zero riding offset: egg=" + eggMeta);
                return;
            }
        }

        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void kongouClassMarriageAurasMatchLegacyClasses(GameTestHelper helper) {
        int[] eggMetas = {62, 63, 64, 65};
        MobEffect[] effects = {
                MobEffects.HEALTH_BOOST,
                MobEffects.SATURATION,
                MobEffects.LUCK,
                MobEffects.NIGHT_VISION
        };
        int[] durations = {260, 340, 220, 220};
        int[] amplifiers = {1, 1, 1, 0};
        UUID ownerUuid = UUID.randomUUID();

        for (int index = 0; index < eggMetas.length; index++) {
            LegacyShipEntity ship = ModEntityTypes.LEGACY_SHIP.get().create(helper.getLevel());
            if (ship == null) {
                helper.fail("Kongou-class ships should be creatable for marriage aura tests");
                return;
            }

            ship.setVariantEggMeta(eggMetas[index]);
            ship.setOwner(ownerUuid, "KongouAuraTester");
            ship.setMarried(true);
            ship.setRingEffectEnabled(true);
            ship.setShipLevel(120);
            ship.setGrudge(200);
            ship.setPos(2.5D + index * 2.0D, 2.0D, 2.5D);
            helper.getLevel().addFreshEntity(ship);
            LegacyShipBehaviorCatalog.tickMarriagePassive(ship);

            MobEffectInstance effect = ship.getEffect(effects[index]);
            if (effect == null || effect.getDuration() != durations[index]
                    || effect.getAmplifier() != amplifiers[index]) {
                helper.fail("Kongou-class marriage aura should preserve the legacy duration and amplifier: egg="
                        + eggMetas[index]);
                return;
            }
        }

        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void atagoAndTakaoReactionAndMountBehaviorMatchLegacyClasses(GameTestHelper helper) {
        LegacyShipEntity atago = ModEntityTypes.LEGACY_SHIP.get().create(helper.getLevel());
        LegacyShipEntity takao = ModEntityTypes.LEGACY_SHIP.get().create(helper.getLevel());
        if (atago == null || takao == null) {
            helper.fail("Atago and Takao should be creatable for legacy behavior tests");
            return;
        }

        atago.setVariantEggMeta(60);
        takao.setVariantEggMeta(61);
        atago.setShipLevel(100);
        takao.setShipLevel(100);

        if (Math.abs(atago.getPassengersRidingOffset() - atago.getBbHeight() * 0.75D) > 1.0E-6D
                || Math.abs(takao.getPassengersRidingOffset() - takao.getBbHeight() * 0.75D) > 1.0E-6D) {
            helper.fail("standing Atago and Takao should keep the legacy 0.75 riding factor");
            return;
        }

        atago.setOrderedToSit(true);
        takao.setOrderedToSit(true);
        atago.setModelState(1);
        takao.setModelState(1);
        if (Math.abs(atago.getPassengersRidingOffset() - atago.getBbHeight() * 0.42D) > 1.0E-6D
                || Math.abs(takao.getPassengersRidingOffset() - takao.getBbHeight() * 0.42D) > 1.0E-6D) {
            helper.fail("cannon-equipped sitting Atago and Takao should keep the legacy 0.42 riding factor");
            return;
        }

        atago.setModelState(0);
        takao.setModelState(0);
        atago.setEmotion(0);
        takao.setEmotion(0);
        if (Math.abs(atago.getPassengersRidingOffset() - atago.getBbHeight() * 0.35D) > 1.0E-6D
                || Math.abs(takao.getPassengersRidingOffset() - takao.getBbHeight() * 0.35D) > 1.0E-6D) {
            helper.fail("normal sitting Atago and Takao should keep the legacy 0.35 riding factor");
            return;
        }

        atago.setEmotion(4);
        takao.setEmotion(4);
        if (atago.getPassengersRidingOffset() != 0.0D || takao.getPassengersRidingOffset() != 0.0D) {
            helper.fail("bored sitting Atago and Takao should use the legacy zero riding offset");
            return;
        }

        LegacyShipBehaviorCatalog.AttackEffectProfile friendly =
                LegacyShipBehaviorCatalog.attackerReactionEffectFor(
                        ShipEntitySpecs.getByEggMeta(60), 100, 0);
        LegacyShipBehaviorCatalog.AttackEffectProfile hostile =
                LegacyShipBehaviorCatalog.attackerReactionEffectFor(
                        ShipEntitySpecs.getByEggMeta(2060), 1, 2);
        if (friendly.effect() != MobEffects.MOVEMENT_SLOWDOWN
                || friendly.amplifier() != 1
                || friendly.durationTicks() != 200
                || friendly.chancePercent() != 100
                || hostile.effect() != MobEffects.MOVEMENT_SLOWDOWN
                || hostile.amplifier() != 0
                || hostile.durationTicks() != 200
                || hostile.chancePercent() != 100) {
            helper.fail("Atago and Takao attacker reactions should preserve the legacy friendly and hostile formulas");
            return;
        }

        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void heavyCruiserTypeFiveMissilesSpawnLegacyStaticFields(GameTestHelper helper) {
        LegacyShipEntity friendly = ModEntityTypes.LEGACY_SHIP.get().create(helper.getLevel());
        LegacyShipEntity hostile = ModEntityTypes.LEGACY_SHIP.get().create(helper.getLevel());
        Zombie target = EntityType.ZOMBIE.create(helper.getLevel());
        if (friendly == null || hostile == null || target == null) {
            helper.fail("heavy cruiser and target should be creatable for type-five missile tests");
            return;
        }

        friendly.setVariantEggMeta(60);
        friendly.setShipLevel(80);
        friendly.setMarried(true);
        friendly.setRingEffectEnabled(true);
        friendly.setOwner(UUID.randomUUID(), "AtagoTypeFive");
        friendly.setPos(1.5D, 2.0D, 1.5D);
        hostile.setVariantEggMeta(2060);
        hostile.initializeHostileRuntime(false, true, true);
        hostile.setPos(1.5D, 2.0D, 9.5D);
        target.setNoAi(true);
        target.setPos(7.5D, 2.0D, 1.5D);
        helper.getLevel().addFreshEntity(friendly);
        helper.getLevel().addFreshEntity(hostile);
        helper.getLevel().addFreshEntity(target);

        LegacyShipProjectileEntity marriedMissile = LegacyShipProjectileEntity.create(
                helper.getLevel(), friendly, target, LegacyShipAttackKind.HEAVY, 4.0F, false);
        friendly.setRingEffectEnabled(false);
        LegacyShipProjectileEntity ordinaryMissile = LegacyShipProjectileEntity.create(
                helper.getLevel(), friendly, target, LegacyShipAttackKind.HEAVY, 4.0F, false);
        if (!marriedMissile.hasBlackHoleImpactEffect() || ordinaryMissile.hasBlackHoleImpactEffect()
                || !LegacyShipBehaviorCatalog.usesBlackHoleHeavyMissile(hostile, LegacyShipAttackKind.HEAVY)) {
            helper.fail("legacy type-five missile selection should follow marriage and hostile scale gates");
            return;
        }

        LegacyShipStaticEffectEntity effect = LegacyShipStaticEffectEntity.create(
                helper.getLevel(), friendly, new Vec3(4.5D, 2.0D, 1.5D));
        if (effect == null) {
            helper.fail("legacy type-five missile should create a static impact field");
            return;
        }
        helper.getLevel().addFreshEntity(effect);
        target.setDeltaMovement(Vec3.ZERO);
        effect.tick();
        effect.tick();
        effect.tick();
        effect.tick();

        if (effect.getLifeTicks() != 30
                || Math.abs(effect.getPullForce() - 0.18F) > 1.0E-5F
                || Math.abs(effect.getRange() - 6.8F) > 1.0E-5F
                || effect.getHostId() != friendly.getId()
                || target.getDeltaMovement().x >= 0.0D) {
            helper.fail("legacy black-hole field should preserve level-scaled lifetime, range, and pull timing");
            return;
        }

        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void heavyProjectileMissesRemainProjectileOnly(GameTestHelper helper) {
        LegacyShipEntity ship = ModEntityTypes.LEGACY_SHIP.get().create(helper.getLevel());
        Zombie target = EntityType.ZOMBIE.create(helper.getLevel());
        if (ship == null || target == null) {
            helper.fail("legacy ship and target should be creatable for special combat miss tests");
            return;
        }

        ship.setVariantEggMeta(40);
        supplyShipForTest(ship);
        ship.setOwner(UUID.randomUUID(), "SpecialHookMiss");
        ship.setShipLevel(1);
        BlockPos shipPos = helper.absolutePos(new BlockPos(1, 2, 1));
        BlockPos targetPos = helper.absolutePos(new BlockPos(3, 2, 1));
        ship.setPos(shipPos.getX() + 0.5D, shipPos.getY(), shipPos.getZ() + 0.5D);
        target.setPos(targetPos.getX() + 0.5D, targetPos.getY(), targetPos.getZ() + 0.5D);
        target.setNoAi(true);
        target.setInvulnerable(true);
        helper.getLevel().addFreshEntity(ship);
        helper.getLevel().addFreshEntity(target);
        ship.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 100, 0, false, false, false));
        target.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, 100, 0, false, false, false));
        long missSeed = -1L;
        for (long seed = 0L; seed < 1024L; seed++) {
            ship.getRandom().setSeed(seed);
            if (LegacyShipCombatHelper.rollAttack(ship, target, LegacyShipAttackKind.HEAVY).miss()) {
                missSeed = seed;
                break;
            }
        }
        if (missSeed < 0L) {
            helper.fail("generic heavy attack should have a deterministic miss seed");
            return;
        }
        ship.getRandom().setSeed(missSeed);

        float healthBefore = target.getHealth();
        boolean handled = ship.performPlayerCompatAttack(target, LegacyShipAttackKind.HEAVY);
        boolean cooledDown = ship.getCompatAttackCooldown(LegacyShipAttackKind.HEAVY) > 0;
        float healthAfter = target.getHealth();
        var failureReason = ship.getCompatAttackFailureReason(target, LegacyShipAttackKind.HEAVY);
        List<LegacyShipProjectileEntity> projectiles = helper.getLevel().getEntitiesOfClass(
                LegacyShipProjectileEntity.class,
                ship.getBoundingBox().inflate(20.0D),
                projectile -> projectile.getOwner() == ship
                        && projectile.getAttackKind() == LegacyShipAttackKind.HEAVY);
        ship.discard();
        target.discard();

        if (!handled) {
            helper.fail("generic heavy attack should handle a forced heavy miss: failure="
                    + failureReason);
            return;
        }
        if (!cooledDown) {
            helper.fail("generic heavy attack should still set heavy cooldown after a miss");
            return;
        }
        if (healthAfter != healthBefore) {
            helper.fail("heavy projectile miss must not deal direct damage: before=" + healthBefore + ", after=" + healthAfter);
            return;
        }
        if (projectiles.size() != 1 || !projectiles.get(0).isMissedShot()) {
            helper.fail("heavy miss should launch one projectile marked as missed");
            return;
        }

        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void catalogHeavyMissesRemainProjectileOnly(GameTestHelper helper) {
        LegacyShipEntity ship = ModEntityTypes.LEGACY_SHIP.get().create(helper.getLevel());
        Zombie target = EntityType.ZOMBIE.create(helper.getLevel());
        if (ship == null || target == null) {
            helper.fail("legacy ship and target should be creatable for catalog miss tests");
            return;
        }

        // EntityCAAtago/EntityCATakao route heavy fire through their legacy
        // barrage helper, which must keep projectile miss semantics.
        ship.setVariantEggMeta(60);
        ship.setOwner(UUID.randomUUID(), "CatalogHeavyMiss");
        ship.setShipLevel(1);
        ship.setHeavyAmmo(100);
        ship.setGrudge(100);
        ship.setNoAi(true);
        BlockPos shipPos = helper.absolutePos(new BlockPos(1, 2, 1));
        BlockPos targetPos = helper.absolutePos(new BlockPos(3, 2, 1));
        ship.setPos(shipPos.getX() + 0.5D, shipPos.getY(), shipPos.getZ() + 0.5D);
        target.setPos(targetPos.getX() + 0.5D, targetPos.getY(), targetPos.getZ() + 0.5D);
        target.setNoAi(true);
        helper.getLevel().addFreshEntity(ship);
        helper.getLevel().addFreshEntity(target);
        ship.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 100, 0, false, false, false));
        target.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, 100, 0, false, false, false));

        long missSeed = -1L;
        for (long seed = 0L; seed < 1024L; seed++) {
            ship.getRandom().setSeed(seed);
            if (LegacyShipCombatHelper.rollAttack(ship, target, LegacyShipAttackKind.HEAVY).miss()) {
                missSeed = seed;
                break;
            }
        }
        if (missSeed < 0L) {
            helper.fail("catalog heavy barrage should have a deterministic miss seed");
            return;
        }
        ship.getRandom().setSeed(missSeed);

        float healthBefore = target.getHealth();
        int ammoBefore = ship.getHeavyAmmo();
        boolean handled = ship.performPlayerCompatAttack(target, LegacyShipAttackKind.HEAVY);
        List<LegacyShipProjectileEntity> projectiles = helper.getLevel().getEntitiesOfClass(
                LegacyShipProjectileEntity.class,
                ship.getBoundingBox().inflate(20.0D),
                projectile -> projectile.getOwner() == ship
                        && projectile.getAttackKind() == LegacyShipAttackKind.HEAVY);
        float healthAfter = target.getHealth();
        ship.discard();
        target.discard();

        if (!handled || ship.getCompatAttackCooldown(LegacyShipAttackKind.HEAVY) <= 0) {
            helper.fail("catalog heavy miss should still handle the attack and set cooldown");
            return;
        }
        if (healthAfter != healthBefore) {
            helper.fail("catalog heavy miss must not deal direct damage: before="
                    + healthBefore + ", after=" + healthAfter);
            return;
        }
        if (ship.getHeavyAmmo() >= ammoBefore
                || projectiles.size() != 1
                || !projectiles.get(0).isMissedShot()) {
            helper.fail("catalog heavy miss should launch one missed projectile and consume heavy ammo"
                    + "; ammoBefore=" + ammoBefore
                    + ", ammoAfter=" + ship.getHeavyAmmo()
                    + ", projectiles=" + projectiles.size()
                    + ", missed=" + (projectiles.size() == 1 && projectiles.get(0).isMissedShot()));
            return;
        }
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void nagatoHeavyStrikeUsesLegacyFourPhaseCycle(GameTestHelper helper) {
        LegacyShipEntity ship = ModEntityTypes.LEGACY_SHIP.get().create(helper.getLevel());
        Zombie target = EntityType.ZOMBIE.create(helper.getLevel());
        if (ship == null || target == null) {
            helper.fail("legacy ship and target should be creatable for Nagato phase tests");
            return;
        }

        ship.setVariantEggMeta(39);
        supplyShipForTest(ship);
        ship.setOwner(UUID.randomUUID(), "NagatoPhase");
        ship.setShipLevel(80);
        ship.tickCount = 1;
        ship.setNoAi(true);
        ship.setNoGravity(true);
        BlockPos shipPos = helper.absolutePos(new BlockPos(1, 2, 1));
        BlockPos targetPos = helper.absolutePos(new BlockPos(3, 2, 1));
        ship.setPos(shipPos.getX() + 0.5D, shipPos.getY(), shipPos.getZ() + 0.5D);
        target.setPos(targetPos.getX() + 0.5D, targetPos.getY(), targetPos.getZ() + 0.5D);
        target.setNoAi(true);
        target.setInvulnerable(true);
        helper.getLevel().addFreshEntity(ship);
        helper.getLevel().addFreshEntity(target);

        for (int phase = 0; phase < 4; phase++) {
            int guard = 0;
            while (ship.getCompatAttackCooldown(LegacyShipAttackKind.HEAVY) > 0 && guard++ < 2000) {
                ship.tick();
            }
            if (ship.getCompatAttackCooldown(LegacyShipAttackKind.HEAVY) > 0
                    || !ship.performPlayerCompatAttack(target, LegacyShipAttackKind.HEAVY)) {
                helper.fail("Nagato heavy phase " + phase + " should be handled after cooldown, remaining="
                        + ship.getCompatAttackCooldown(LegacyShipAttackKind.HEAVY)
                        + ", failure=" + ship.getCompatAttackFailureReason(target, LegacyShipAttackKind.HEAVY));
                return;
            }
            int expectedPhase = phase == 3 ? 0 : phase + 1;
            if (ship.getDisplayPhase() != expectedPhase) {
                helper.fail("Nagato phase should be " + expectedPhase
                        + " after attack " + phase + ", got " + ship.getDisplayPhase());
                return;
            }
        }

        List<LegacyShipProjectileEntity> projectiles = helper.getLevel().getEntitiesOfClass(
                LegacyShipProjectileEntity.class,
                ship.getBoundingBox().inflate(20.0D),
                projectile -> projectile.getOwner() == ship
                        && projectile.getAttackKind() == LegacyShipAttackKind.HEAVY);
        ship.discard();
        target.discard();

        if (!projectiles.isEmpty()) {
            helper.fail("Nagato legacy heavy strike must not create projectile entities");
            return;
        }
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void nagatoHeavyStrikeUsesLegacyBlastProfile(GameTestHelper helper) {
        ShipEntitySpec friendly = ShipEntitySpecs.getByEggMeta(39);
        ShipEntitySpec hostile = ShipEntitySpecs.getByEggMeta(2039);
        if (friendly == null || hostile == null) {
            helper.fail("friendly and hostile Nagato specs should be registered");
            return;
        }

        double[] expectedHostileRadii = {3.5D, 4.0D, 4.5D, 5.0D};
        for (int scale = 0; scale < expectedHostileRadii.length; scale++) {
            if (Math.abs(LegacyShipBehaviorCatalog.nagatoBlastRadiusFor(friendly, scale) - 3.5D) > 1.0E-6D
                    || Math.abs(LegacyShipBehaviorCatalog.nagatoBlastRadiusFor(hostile, scale)
                    - expectedHostileRadii[scale]) > 1.0E-6D) {
                helper.fail("Nagato blast radius should preserve friendly fixed radius and hostile scale extension");
                return;
            }
        }

        LegacyShipStats stats = LegacyShipStats.create(
                37, ShipArchetype.BATTLESHIP, false, 80, 4000,
                0, 0, 0, 0, 0, 0, false,
                TeitokuData.DEFAULT_FORMATION_ID,
                new ShipEquipmentProfile(
                        0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F,
                        0.0F, 0.0F, 0.0F, 0.0F, 4.0F, 5.0F, 0.0F, 0.0F, 0.0F,
                        0.0F, 0.0F, 0.0F, 1),
                List.of());
        LivingEntity airTarget = EntityType.BAT.create(helper.getLevel());
        LivingEntity seaTarget = EntityType.DROWNED.create(helper.getLevel());
        if (airTarget == null || seaTarget == null) {
            helper.fail("air and undersea targets should be creatable for Nagato damage profile tests");
            return;
        }

        float baseDamage = 10.0F;
        LivingEntity normalTarget = EntityType.ZOMBIE.create(helper.getLevel());
        float airDamage = LegacyShipCombatHelper.applyTargetDamageModifier(stats, airTarget, baseDamage);
        float seaDamage = LegacyShipCombatHelper.applyTargetDamageModifier(stats, seaTarget, baseDamage);
        float normalDamage = normalTarget == null
                ? -1.0F
                : LegacyShipCombatHelper.applyTargetDamageModifier(stats, normalTarget, baseDamage);
        if (Math.abs(airDamage - (baseDamage + stats.antiAir())) > 1.0E-6F
                || Math.abs(seaDamage - (baseDamage + stats.antiSub())) > 1.0E-6F
                || Math.abs(normalDamage - baseDamage) > 1.0E-6F) {
            helper.fail("Nagato heavy damage should apply legacy AA/ASM additions only to matching target types"
                    + ": air=" + airDamage + ", sea=" + seaDamage + ", normal=" + normalDamage
                    + ", aa=" + stats.antiAir() + ", asm=" + stats.antiSub());
            return;
        }

        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void yamatoHeavyAttackUsesLegacyTwoPhaseBeam(GameTestHelper helper) {
        LegacyShipEntity ship = ModEntityTypes.LEGACY_SHIP.get().create(helper.getLevel());
        Zombie target = EntityType.ZOMBIE.create(helper.getLevel());
        if (ship == null || target == null) {
            helper.fail("legacy ship and target should be creatable for Yamato beam tests");
            return;
        }

        ship.setVariantEggMeta(48);
        ship.setOwner(UUID.randomUUID(), "YamatoBeam");
        ship.setShipLevel(80);
        ship.setHeavyAmmo(100);
        ship.setGrudge(100);
        ship.setNoAi(true);
        BlockPos shipPos = helper.absolutePos(new BlockPos(1, 2, 1));
        BlockPos targetPos = helper.absolutePos(new BlockPos(8, 2, 1));
        ship.setPos(shipPos.getX() + 0.5D, shipPos.getY(), shipPos.getZ() + 0.5D);
        target.setPos(targetPos.getX() + 0.5D, targetPos.getY(), targetPos.getZ() + 0.5D);
        target.setNoAi(true);
        target.setInvulnerable(true);
        helper.getLevel().addFreshEntity(ship);
        helper.getLevel().addFreshEntity(target);

        int ammoBefore = ship.getHeavyAmmo();
        int grudgeBefore = ship.getGrudge();
        int moraleBefore = ship.getMorale();
        float healthBefore = target.getHealth();
        if (!ship.performPlayerCompatAttack(target, LegacyShipAttackKind.HEAVY)
                || ship.getDisplayPhase() != 1) {
            helper.fail("Yamato first heavy attack should only enter the legacy charge phase");
            return;
        }
        if (ship.getHeavyAmmo() != ammoBefore - 4
                || ship.getGrudge() != grudgeBefore - 8
                || ship.getMorale() != moraleBefore - 2) {
            helper.fail("Yamato charge should consume one legacy battleship heavy-ammo unit");
            return;
        }

        // Direct GameTest ticks do not pass through ServerLevel's tickCount increment.
        ship.tickCount = 1;
        int guard = 0;
        while (ship.getCompatAttackCooldown(LegacyShipAttackKind.HEAVY) > 0 && guard++ < 2000) {
            ship.tick();
        }
        if (ship.getCompatAttackCooldown(LegacyShipAttackKind.HEAVY) > 0
                || !ship.performPlayerCompatAttack(target, LegacyShipAttackKind.HEAVY)) {
            helper.fail("Yamato second heavy attack should launch the legacy beam");
            return;
        }

        List<LegacyShipBeamEntity> beams = helper.getLevel().getEntitiesOfClass(
                LegacyShipBeamEntity.class,
                ship.getBoundingBox().inflate(24.0D),
                beam -> beam.getHostId() == ship.getId());
        if (beams.size() != 1 || beams.get(0).getBeamType() != 0
                || beams.get(0).getLifeTicks() != 31
                || beams.get(0).getTravelSpeed() != 4.0D
                || Math.abs(beams.get(0).getDamage()
                - ship.getLegacyStats().attackHeavy() * 1.5F) > 1.0E-6F
                || ship.getDisplayPhase() != 0
                || target.getHealth() != healthBefore
                || ship.getHeavyAmmo() != ammoBefore - 8
                || ship.getGrudge() != grudgeBefore - 16
                || ship.getMorale() != moraleBefore - 4) {
            helper.fail("Yamato second heavy attack should create one beam without direct damage");
            return;
        }

        ship.discard();
        target.discard();
        for (LegacyShipBeamEntity beam : beams) {
            beam.discard();
        }
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void tatsutaHeavyAttackRunsLegacySkillPhases(GameTestHelper helper) {
        LegacyShipEntity ship = ModEntityTypes.LEGACY_SHIP.get().create(helper.getLevel());
        Zombie target = EntityType.ZOMBIE.create(helper.getLevel());
        if (ship == null || target == null) {
            helper.fail("legacy ship and target should be creatable for Tatsuta skill tests");
            return;
        }

        ship.setVariantEggMeta(59);
        ship.setOwner(UUID.randomUUID(), "TatsutaSkill");
        ship.setShipLevel(80);
        ship.setHeavyAmmo(100);
        ship.setGrudge(100);
        ship.setTarget(target);
        ship.setNoAi(true);
        ship.setNoGravity(true);
        ship.setInvulnerable(true);
        ship.setAiAutoTarget(false);
        BlockPos shipPos = helper.absolutePos(new BlockPos(1, 2, 1));
        BlockPos targetPos = helper.absolutePos(new BlockPos(5, 2, 1));
        ship.setPos(shipPos.getX() + 0.5D, shipPos.getY(), shipPos.getZ() + 0.5D);
        target.setPos(targetPos.getX() + 0.5D, targetPos.getY(), targetPos.getZ() + 0.5D);
        target.setNoAi(true);
        target.setInvulnerable(true);
        helper.getLevel().addFreshEntity(ship);
        helper.getLevel().addFreshEntity(target);

        int ammoBefore = ship.getHeavyAmmo();
        int grudgeBefore = ship.getGrudge();
        int moraleBefore = ship.getMorale();
        int experienceBefore = ship.getShipExperience();
        int experienceGain = Math.max(0, (int) (5F * ship.getLegacyStats().get(LegacyShipStatTables.Attr.XP)));
        int ammoCost = LegacyShipBehaviorCatalog.attackAmmoCostFor(
                ship.getSpec(), LegacyShipAttackKind.HEAVY);

        if (!ship.performPlayerCompatAttack(target, LegacyShipAttackKind.HEAVY)
                || ship.getDisplayPhase() != -1
                || ship.getHeavyAmmo() != ammoBefore - ammoCost
                || ship.getGrudge() != grudgeBefore - 8
                || ship.getMorale() != moraleBefore - 2
                || ship.getShipExperience() != experienceBefore + experienceGain) {
            helper.fail("Tatsuta first heavy attack should enter the legacy charge pre-phase and consume resources");
            return;
        }

        ship.setTarget(null);
        // Keep the direct cooldown loop away from the shared 128-tick supply boundary.
        ship.tickCount = 1;
        int guard = 0;
        while (ship.getCompatAttackCooldown(LegacyShipAttackKind.HEAVY) > 0 && guard++ < 2000) {
            ship.tick();
        }
        ship.setTarget(target);
        boolean secondAttack = ship.performPlayerCompatAttack(target, LegacyShipAttackKind.HEAVY);
        if (ship.getCompatAttackCooldown(LegacyShipAttackKind.HEAVY) <= 0
                || !secondAttack
                || ship.getDisplayPhase() != 1
                || ship.getLegacySkillTimer() != 10
                || ship.getLegacySkillRemaining() != 3
                || ship.isPushable()) {
            helper.fail("Tatsuta second heavy attack should enter phase 1 with the legacy level-scaled sweep count"
                    + "; acted=" + secondAttack
                    + ", phase=" + ship.getDisplayPhase()
                    + ", timer=" + ship.getLegacySkillTimer()
                    + ", remaining=" + ship.getLegacySkillRemaining()
                    + ", cooldown=" + ship.getCompatAttackCooldown(LegacyShipAttackKind.HEAVY)
                    + ", pushable=" + ship.isPushable()
                    + ", guard=" + guard);
            return;
        }

        if (ship.getHeavyAmmo() != ammoBefore - ammoCost * 2
                || ship.getGrudge() != grudgeBefore - 16
                || ship.getMorale() != moraleBefore - 4
                || ship.getShipExperience() != experienceBefore + experienceGain * 2) {
            helper.fail("Tatsuta skill phase entry should consume one legacy heavy attack per invocation");
            return;
        }

        List<LegacyShipBeamEntity> beams = List.of();
        for (int ticks = 0; ticks < 100; ticks++) {
            ship.tick();
            beams = helper.getLevel().getEntitiesOfClass(
                    LegacyShipBeamEntity.class,
                    ship.getBoundingBox().inflate(24.0D),
                    beam -> beam.getHostId() == ship.getId() && beam.getBeamType() == 1);
            if (!beams.isEmpty()) {
                break;
            }
        }

        if (beams.size() != 1 || beams.get(0).getLifeTicks() != 8
                || beams.get(0).getTravelSpeed() != 3.0D
                || ship.getDisplayPhase() != 3
                || ship.getLegacySkillRemaining() != 0) {
            helper.fail("Tatsuta phase 3 should launch exactly one legacy beam after the horizontal sweeps");
            return;
        }

        for (int ticks = 0; ticks < 20 && ship.getDisplayPhase() != 0; ticks++) {
            ship.tick();
        }

        if (ship.getDisplayPhase() != 0 || !ship.isPushable()) {
            helper.fail("Tatsuta should leave the legacy skill state after the final beam window");
            return;
        }
        ship.discard();
        target.discard();
        for (LegacyShipBeamEntity beam : beams) {
            beam.discard();
        }
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void shimakazeLightAttackSummonsLegacyRensouhou(GameTestHelper helper) {
        LegacyShipEntity ship = ModEntityTypes.LEGACY_SHIP.get().create(helper.getLevel());
        Zombie target = EntityType.ZOMBIE.create(helper.getLevel());
        if (ship == null || target == null) {
            helper.fail("legacy ship and target should be creatable for Shimakaze summon tests");
            return;
        }

        ship.setVariantEggMeta(38);
        ship.setOwner(UUID.randomUUID(), "ShimakazeSummon");
        ship.setShimakazeRensouhouCount(6);
        ship.setLightAmmo(100);
        ship.setGrudge(100);
        BlockPos shipPos = helper.absolutePos(new BlockPos(1, 2, 1));
        BlockPos targetPos = helper.absolutePos(new BlockPos(3, 2, 1));
        ship.setPos(shipPos.getX() + 0.5D, shipPos.getY(), shipPos.getZ() + 0.5D);
        target.setPos(targetPos.getX() + 0.5D, targetPos.getY(), targetPos.getZ() + 0.5D);
        target.setNoAi(true);
        target.setInvulnerable(true);
        helper.getLevel().addFreshEntity(ship);
        helper.getLevel().addFreshEntity(target);

        int ammoBefore = ship.getLightAmmo();
        int grudgeBefore = ship.getGrudge();
        int moraleBefore = ship.getMorale();
        float healthBefore = target.getHealth();
        if (!ship.performPlayerCompatAttack(target, LegacyShipAttackKind.LIGHT)) {
            helper.fail("Shimakaze light attack should summon a Rensouhou");
            return;
        }

        List<LegacyShipRensouhouEntity> summons = helper.getLevel().getEntitiesOfClass(
                LegacyShipRensouhouEntity.class,
                ship.getBoundingBox().inflate(8.0D),
                summon -> summon.getHostId() == ship.getId());
        if (summons.size() != 1 || summons.get(0).getInternalAmmo() != 6
                || summons.get(0).getTargetId() != target.getId()) {
            helper.fail("Shimakaze light attack should create one six-shot linked Rensouhou");
            return;
        }
        if (ship.getShimakazeRensouhouCount() != 5
                || ship.getLightAmmo() != ammoBefore - 4
                || ship.getGrudge() != grudgeBefore - 16
                || ship.getMorale() != moraleBefore - 1
                || target.getHealth() != healthBefore
                || ship.getCompatAttackCooldown(LegacyShipAttackKind.LIGHT) <= 0) {
            helper.fail("Shimakaze light attack should preserve legacy servant, resource, and cooldown costs");
            return;
        }

        ship.discard();
        target.discard();
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void battleshipTaLightAttackSummonsLegacyRensouhou(GameTestHelper helper) {
        LegacyShipEntity ship = ModEntityTypes.LEGACY_SHIP.get().create(helper.getLevel());
        Zombie target = EntityType.ZOMBIE.create(helper.getLevel());
        if (ship == null || target == null) {
            helper.fail("legacy ship and target should be creatable for Battleship Ta summon tests");
            return;
        }

        ship.setVariantEggMeta(16);
        supplyShipForTest(ship);
        ship.setShimakazeRensouhouCount(99);
        if (ship.getShimakazeRensouhouCount() != 6) {
            helper.fail("Battleship Ta should keep the legacy six-servant cap");
            return;
        }
        ship.setShimakazeRensouhouCount(0);
        ship.tickCount = 128;
        BlockPos shipPos = helper.absolutePos(new BlockPos(1, 2, 1));
        BlockPos targetPos = helper.absolutePos(new BlockPos(3, 2, 1));
        ship.setPos(shipPos.getX() + 0.5D, shipPos.getY(), shipPos.getZ() + 0.5D);
        target.setPos(targetPos.getX() + 0.5D, targetPos.getY(), targetPos.getZ() + 0.5D);
        target.setNoAi(true);
        target.setInvulnerable(true);
        helper.getLevel().addFreshEntity(ship);
        helper.getLevel().addFreshEntity(target);

        LegacyShipBehaviorCatalog.tickLegacyPerShipBehavior(ship);
        if (ship.getShimakazeRensouhouCount() != 1) {
            helper.fail("Battleship Ta should recover one Rensouhou every 128 ticks");
            return;
        }
        ship.setShimakazeRensouhouCount(6);
        int ammoBefore = ship.getLightAmmo();
        int grudgeBefore = ship.getGrudge();
        int moraleBefore = ship.getMorale();
        int experienceBefore = ship.getShipExperience();
        float healthBefore = target.getHealth();
        if (!ship.performPlayerCompatAttack(target, LegacyShipAttackKind.LIGHT)) {
            helper.fail("Battleship Ta light attack should summon a Rensouhou");
            return;
        }

        List<LegacyShipRensouhouEntity> summons = helper.getLevel().getEntitiesOfClass(
                LegacyShipRensouhouEntity.class,
                ship.getBoundingBox().inflate(8.0D),
                summon -> summon.getHostId() == ship.getId());
        if (summons.size() != 1 || summons.get(0).getInternalAmmo() != 6
                || summons.get(0).getTargetId() != target.getId()
                || summons.get(0).isHostileVariant() || !summons.get(0).isLargeModel()) {
            helper.fail("Battleship Ta should create one friendly six-shot RensouhouS");
            return;
        }
        int expectedAmmo = ammoBefore
                - LegacyShipBehaviorCatalog.attackAmmoCostFor(ship.getSpec(), LegacyShipAttackKind.LIGHT) * 4;
        int expectedGrudge = grudgeBefore - 16;
        int expectedMorale = moraleBefore - 1;
        int expectedExperience = experienceBefore
                + (int) (8 * ship.getLegacyStats().get(LegacyShipStatTables.Attr.XP));
        if (ship.getShimakazeRensouhouCount() != 5
                || ship.getLightAmmo() != expectedAmmo
                || ship.getGrudge() != expectedGrudge
                || ship.getMorale() != expectedMorale
                || ship.getShipExperience() != expectedExperience
                || target.getHealth() != healthBefore
                || ship.getCompatAttackCooldown(LegacyShipAttackKind.LIGHT) <= 0) {
            helper.fail("friendly Battleship Ta should preserve legacy servant and resource costs");
            return;
        }

        ship.discard();
        target.discard();
        for (LegacyShipRensouhouEntity summon : summons) {
            summon.discard();
        }
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void rensouhouModelVariantMirrorsLegacyHostState(GameTestHelper helper) {
        LegacyShipEntity ship = ModEntityTypes.LEGACY_SHIP.get().create(helper.getLevel());
        if (ship == null) {
            helper.fail("legacy ship should be creatable for Rensouhou model-state coverage");
            return;
        }

        ship.setVariantEggMeta(38);
        ship.setModelState(0);
        if (LegacyShipBehaviorCatalog.rensouhouUsesLargeModel(ship)) {
            helper.fail("Shimakaze model state 0 should select the ordinary Rensouhou");
            return;
        }
        ship.setModelState(1);
        if (!LegacyShipBehaviorCatalog.rensouhouUsesLargeModel(ship)) {
            helper.fail("Shimakaze model state bit 0 should select RensouhouS");
            return;
        }

        ship.setVariantEggMeta(16);
        ship.setModelState(0);
        if (!LegacyShipBehaviorCatalog.rensouhouUsesLargeModel(ship)) {
            helper.fail("Battleship Ta model state 0 should select RensouhouS");
            return;
        }
        ship.setModelState(1);
        if (LegacyShipBehaviorCatalog.rensouhouUsesLargeModel(ship)) {
            helper.fail("Battleship Ta model state bit 0 should select the ordinary Rensouhou");
            return;
        }

        ship.discard();
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void rensouhouKeepsLegacyLineOfSightAttackGate(GameTestHelper helper) {
        LegacyShipEntity ship = ModEntityTypes.LEGACY_SHIP.get().create(helper.getLevel());
        Zombie target = EntityType.ZOMBIE.create(helper.getLevel());
        if (ship == null || target == null) {
            helper.fail("legacy ship and target should be creatable for Rensouhou sight tests");
            return;
        }

        ship.setVariantEggMeta(38);
        ship.setOwner(UUID.randomUUID(), "RensouhouSight");
        ship.setLightAmmo(100);
        ship.setGrudge(100);
        ship.setNoAi(true);
        ship.setAiOnSightChase(false);
        BlockPos shipPos = helper.absolutePos(new BlockPos(1, 2, 1));
        BlockPos targetPos = helper.absolutePos(new BlockPos(5, 2, 1));
        ship.setPos(shipPos.getX() + 0.5D, shipPos.getY(), shipPos.getZ() + 0.5D);
        target.setPos(targetPos.getX() + 0.5D, targetPos.getY(), targetPos.getZ() + 0.5D);
        target.setNoAi(true);
        target.setInvulnerable(true);
        helper.getLevel().addFreshEntity(ship);
        helper.getLevel().addFreshEntity(target);

        LegacyShipRensouhouEntity summon = LegacyShipRensouhouEntity.create(
                helper.getLevel(), ship, target);
        if (summon == null) {
            helper.fail("Rensouhou should be creatable for line-of-sight tests");
            return;
        }
        summon.setPos(ship.getX() + 0.5D, ship.getY(), ship.getZ());
        helper.getLevel().addFreshEntity(summon);

        BlockPos wall = helper.absolutePos(new BlockPos(3, 2, 1));
        helper.getLevel().setBlock(wall, Blocks.STONE.defaultBlockState(), 3);
        int ammoBefore = summon.getInternalAmmo();
        summon.tick();
        if (summon.getInternalAmmo() != ammoBefore) {
            helper.fail("Rensouhou must not attack through a wall when OnSightChase is disabled");
            return;
        }

        helper.getLevel().removeBlock(wall, false);
        summon.tick();
        if (summon.getInternalAmmo() != ammoBefore - 1) {
            helper.fail("Rensouhou should attack after its legacy line of sight is restored");
            return;
        }

        ship.discard();
        target.discard();
        summon.discard();
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void rensouhouOnSightChaseStopsBlockedPursuit(GameTestHelper helper) {
        LegacyShipEntity ship = ModEntityTypes.LEGACY_SHIP.get().create(helper.getLevel());
        Zombie target = EntityType.ZOMBIE.create(helper.getLevel());
        if (ship == null || target == null) {
            helper.fail("legacy ship and target should be creatable for Rensouhou pursuit tests");
            return;
        }

        ship.setVariantEggMeta(38);
        ship.setOwner(UUID.randomUUID(), "RensouhouPursuit");
        ship.setLightAmmo(100);
        ship.setGrudge(100);
        ship.setNoAi(true);
        ship.setAiOnSightChase(true);
        ship.setPos(helper.absolutePos(new BlockPos(1, 2, 1)).getX() + 0.5D, 2.0D,
                helper.absolutePos(new BlockPos(1, 2, 1)).getZ() + 0.5D);
        target.setPos(helper.absolutePos(new BlockPos(12, 2, 1)).getX() + 0.5D, 2.0D,
                helper.absolutePos(new BlockPos(12, 2, 1)).getZ() + 0.5D);
        target.setNoAi(true);
        target.setInvulnerable(true);
        helper.getLevel().addFreshEntity(ship);
        helper.getLevel().addFreshEntity(target);

        LegacyShipRensouhouEntity summon = LegacyShipRensouhouEntity.create(
                helper.getLevel(), ship, target);
        if (summon == null) {
            helper.fail("Rensouhou should be creatable for pursuit tests");
            return;
        }
        summon.setPos(ship.getX() + 0.5D, ship.getY(), ship.getZ());
        helper.getLevel().addFreshEntity(summon);

        BlockPos wall = helper.absolutePos(new BlockPos(6, 2, 1));
        helper.getLevel().setBlock(wall, Blocks.STONE.defaultBlockState(), 3);
        summon.tick();
        if (!summon.getNavigation().isDone() || summon.getInternalAmmo() != 6) {
            helper.fail("Rensouhou should stop pursuit and hold fire while OnSightChase loses its target");
            return;
        }

        helper.getLevel().removeBlock(wall, false);
        ship.discard();
        target.discard();
        summon.discard();
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void battleshipRuHeavyAttackQueuesLegacyMissileBarrage(GameTestHelper helper) {
        LegacyShipEntity ship = ModEntityTypes.LEGACY_SHIP.get().create(helper.getLevel());
        Zombie target = EntityType.ZOMBIE.create(helper.getLevel());
        if (ship == null || target == null) {
            helper.fail("legacy ship and target should be creatable for Battleship Ru barrage tests");
            return;
        }

        ship.setVariantEggMeta(15);
        supplyShipForTest(ship);
        ship.setNoAi(true);
        BlockPos shipPos = helper.absolutePos(new BlockPos(1, 2, 1));
        BlockPos targetPos = helper.absolutePos(new BlockPos(7, 2, 1));
        ship.setPos(shipPos.getX() + 0.5D, shipPos.getY(), shipPos.getZ() + 0.5D);
        target.setPos(targetPos.getX() + 0.5D, targetPos.getY(), targetPos.getZ() + 0.5D);
        target.setNoAi(true);
        target.setInvulnerable(true);
        helper.getLevel().addFreshEntity(ship);
        helper.getLevel().addFreshEntity(target);

        int heavyAmmoBefore = ship.getHeavyAmmo();
        int grudgeBefore = ship.getGrudge();
        int moraleBefore = ship.getMorale();
        int experienceBefore = ship.getShipExperience();
        int expectedHeavyAmmo = heavyAmmoBefore
                - LegacyShipBehaviorCatalog.attackAmmoCostFor(ship.getSpec(), LegacyShipAttackKind.HEAVY);
        int expectedGrudge = grudgeBefore - 8;
        int expectedMorale = moraleBefore - 2;
        int expectedExperience = experienceBefore
                + (int) (5 * ship.getLegacyStats().get(LegacyShipStatTables.Attr.XP));
        if (!ship.performPlayerCompatAttack(target, LegacyShipAttackKind.HEAVY)) {
            helper.fail("Battleship Ru heavy attack should enter its staged missile barrage");
            return;
        }

        int expectedShots = 5 + (int) (ship.getShipLevel() * 0.035F);
        if (ship.getDisplayPhase() != 1
                || ship.getLegacySkillRemaining() != expectedShots
                || ship.getLegacySkillTarget().equals(BlockPos.ZERO)
                || ship.getCompatAttackCooldown(LegacyShipAttackKind.HEAVY) <= 0
                || ship.getHeavyAmmo() != expectedHeavyAmmo
                || ship.getGrudge() != expectedGrudge
                || ship.getMorale() != expectedMorale
                || ship.getShipExperience() != expectedExperience) {
            helper.fail("friendly Battleship Ru should lock the target and preserve legacy resource costs");
            return;
        }

        List<LegacyShipProjectileEntity> projectiles = helper.getLevel().getEntitiesOfClass(
                LegacyShipProjectileEntity.class,
                ship.getBoundingBox().inflate(24.0D),
                projectile -> projectile.getOwner() == ship);
        if (!projectiles.isEmpty()) {
            helper.fail("Battleship Ru should not launch the queued missiles during the initial attack call");
            return;
        }

        ship.tickCount = 4;
        LegacyShipBehaviorCatalog.tickLegacyPerShipBehavior(ship);
        projectiles = helper.getLevel().getEntitiesOfClass(
                LegacyShipProjectileEntity.class,
                ship.getBoundingBox().inflate(24.0D),
                projectile -> projectile.getOwner() == ship);
        if (projectiles.size() != 1 || ship.getLegacySkillRemaining() != expectedShots - 1) {
            helper.fail("Battleship Ru should launch exactly one queued missile every four ticks");
            return;
        }

        LegacyShipProjectileEntity projectile = projectiles.get(0);
        LegacyShipProjectileProfile projectileProfile = ship.getAttackProfile()
                .projectileProfile(LegacyShipAttackKind.HEAVY);
        Vec3 legacyTargetPosition = projectile.getLegacyTargetPosition();
        Vec3 distanceVector = legacyTargetPosition.subtract(projectile.position());
        double distance = distanceVector.length();
        double speed = projectileProfile.baseSpeed()
                * ship.getEquipmentBehaviorState().projectileSpeedMultiplier(LegacyShipAttackKind.HEAVY);
        Vec3 expectedMotion = distance <= 1.0E-7D || speed <= 1.0E-7D
                ? Vec3.ZERO : distanceVector.normalize().scale(speed);
        LegacyShipProjectileMoveType expectedMoveType = projectileProfile.moveType();
        if (distance <= 1.0E-7D || speed <= 1.0E-7D || distance < 4.0D
                || projectileProfile.launchArcLift() <= 0.0F) {
            expectedMoveType = LegacyShipProjectileMoveType.DIRECT;
        } else {
            double horizontalDistance = Math.sqrt(distanceVector.x * distanceVector.x
                    + distanceVector.z * distanceVector.z);
            if (horizontalDistance > 4.0D) {
                double horizontalX = distanceVector.x / horizontalDistance;
                double horizontalZ = distanceVector.z / horizontalDistance;
                double travelTicks = horizontalDistance / speed;
                double additionalHeight = distance * projectileProfile.launchArcLift();
                double verticalDistance = Math.abs(projectile.getY() - legacyTargetPosition.y);
                double heightRatio = Math.sqrt(additionalHeight / (additionalHeight + verticalDistance));
                int riseTicks;
                int fallTicks;
                double verticalSpeed;
                double acceleration1;
                double acceleration2;
                if (projectile.getY() - legacyTargetPosition.y < 1.0D) {
                    riseTicks = (int) Math.floor(travelTicks / (1.0D + heightRatio));
                    fallTicks = (int) Math.floor(travelTicks * heightRatio / (1.0D + heightRatio));
                    verticalSpeed = 2.0D * (additionalHeight + verticalDistance) / riseTicks;
                    acceleration1 = -verticalSpeed / riseTicks;
                    acceleration2 = -2.0D * additionalHeight / (fallTicks * fallTicks);
                } else {
                    riseTicks = (int) Math.floor(travelTicks * heightRatio / (1.0D + heightRatio));
                    fallTicks = (int) Math.floor(travelTicks / (1.0D + heightRatio));
                    acceleration1 = -2.0D * additionalHeight / (riseTicks * riseTicks);
                    verticalSpeed = -acceleration1 * riseTicks;
                    acceleration2 = -2.0D * (additionalHeight + verticalDistance)
                            / (fallTicks * fallTicks);
                }
                if (riseTicks <= 0 || fallTicks <= 0
                        || Math.abs(acceleration1) > 0.15D || Math.abs(acceleration2) > 0.15D) {
                    expectedMoveType = LegacyShipProjectileMoveType.DIRECT;
                } else {
                    expectedMotion = new Vec3(horizontalX * speed, verticalSpeed, horizontalZ * speed);
                }
            }
        }
        Vec3 initialMotion = projectile.getDeltaMovement();
        if (projectile.getProjectileVisual() != LegacyShipProjectileVisual.MISSILE
                || projectile.getMoveType() != expectedMoveType
                || projectile.getIntendedTargetId() != target.getId()
                || projectile.getImpactRadius() != 3.5D
                || Math.abs(projectile.getY() - (ship.getY() + ship.getBbHeight() * 0.4D)) > 1.0E-6D
                || initialMotion.distanceToSqr(expectedMotion) > 1.0E-10D
                || projectile.isMissedShot()) {
            helper.fail("Battleship Ru should preserve the legacy target scatter, missile branch, and launch formula");
            return;
        }

        ship.tickCount = 8;
        LegacyShipBehaviorCatalog.tickLegacyPerShipBehavior(ship);
        int secondLaunchCount = helper.getLevel().getEntitiesOfClass(
                LegacyShipProjectileEntity.class,
                ship.getBoundingBox().inflate(24.0D),
                candidate -> candidate.getOwner() == ship).size();
        if (secondLaunchCount != 2 || ship.getLegacySkillRemaining() != expectedShots - 2) {
            helper.fail("Battleship Ru should keep the four-tick launch cadence");
            return;
        }

        ship.setLegacySkillRemaining(0);
        ship.tickCount = 16;
        LegacyShipBehaviorCatalog.tickLegacyPerShipBehavior(ship);
        if (ship.getDisplayPhase() != 0 || !ship.getLegacySkillTarget().equals(BlockPos.ZERO)) {
            helper.fail("Battleship Ru should clear its staged barrage state after the final missile");
            return;
        }

        ship.discard();
        target.discard();
        for (LegacyShipProjectileEntity queued : projectiles) {
            queued.discard();
        }
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void battleshipRuBlockHeavyAttackUsesLegacyGroundTarget(GameTestHelper helper) {
        LegacyShipEntity ship = ModEntityTypes.LEGACY_SHIP.get().create(helper.getLevel());
        if (ship == null) {
            helper.fail("legacy ship should be creatable for Battleship Ru block-target tests");
            return;
        }

        ship.setVariantEggMeta(15);
        ship.setOwner(UUID.randomUUID(), "BattleshipRuBlock");
        ship.setNoAi(true);
        ship.setLightAmmo(100);
        ship.setHeavyAmmo(100);
        ship.setGrudge(100);
        BlockPos shipPos = helper.absolutePos(new BlockPos(1, 2, 1));
        BlockPos targetPos = helper.absolutePos(new BlockPos(7, 2, 1));
        ship.setPos(shipPos.getX() + 0.5D, shipPos.getY(), shipPos.getZ() + 0.5D);
        helper.getLevel().addFreshEntity(ship);

        int heavyAmmoBefore = ship.getHeavyAmmo();
        int grudgeBefore = ship.getGrudge();
        int moraleBefore = ship.getMorale();
        int experienceBefore = ship.getShipExperience();
        int expectedHeavyAmmo = heavyAmmoBefore
                - LegacyShipBehaviorCatalog.attackAmmoCostFor(ship.getSpec(), LegacyShipAttackKind.HEAVY);
        int expectedExperience = experienceBefore
                + (int) (5 * ship.getLegacyStats().get(LegacyShipStatTables.Attr.XP));
        if (!LegacyShipBehaviorCatalog.supportsBlockTargetAttack(
                ShipEntitySpecs.getByEggMeta(15), LegacyShipAttackKind.HEAVY)
                || LegacyShipBehaviorCatalog.supportsBlockTargetAttack(
                ShipEntitySpecs.getByEggMeta(35), LegacyShipAttackKind.HEAVY)) {
            helper.fail("only legacy Ru and Shimakaze overrides should accept a heavy block target");
            return;
        }
        if (!ship.performPlayerCompatAttack(targetPos, LegacyShipAttackKind.HEAVY)) {
            helper.fail("Battleship Ru block heavy attack should enter its staged missile barrage");
            return;
        }

        int expectedShots = 5 + (int) (ship.getShipLevel() * 0.035F);
        if (ship.getDisplayPhase() != 1
                || ship.getLegacySkillRemaining() != expectedShots
                || ship.getLegacySkillTarget().equals(BlockPos.ZERO)
                || ship.getTarget() != null
                 || ship.getHeavyAmmo() != expectedHeavyAmmo
                 || ship.getGrudge() != grudgeBefore - 8
                 || ship.getMorale() != moraleBefore - 2
                 || ship.getShipExperience() != expectedExperience
                 || ship.getCompatAttackCooldown(LegacyShipAttackKind.HEAVY) <= 0) {
            helper.fail("Ru ground-target heavy attack should preserve the legacy charge state and costs");
            return;
        }

        List<LegacyShipProjectileEntity> projectiles = helper.getLevel().getEntitiesOfClass(
                LegacyShipProjectileEntity.class,
                ship.getBoundingBox().inflate(24.0D),
                projectile -> projectile.getOwner() == ship);
        if (!projectiles.isEmpty()) {
            helper.fail("Ru ground-target heavy attack should queue missiles instead of launching immediately");
            return;
        }

        ship.tickCount = 4;
        LegacyShipBehaviorCatalog.tickLegacyPerShipBehavior(ship);
        projectiles = helper.getLevel().getEntitiesOfClass(
                LegacyShipProjectileEntity.class,
                ship.getBoundingBox().inflate(24.0D),
                projectile -> projectile.getOwner() == ship);
        if (projectiles.size() != 1
                || projectiles.get(0).getIntendedTargetId() != -1
                || projectiles.get(0).getImpactRadius() != 3.5D
                || ship.getLegacySkillRemaining() != expectedShots - 1) {
            helper.fail("Ru ground-target barrage should emit an area missile without an entity target");
            return;
        }

        ship.discard();
        for (LegacyShipProjectileEntity projectile : projectiles) {
            projectile.discard();
        }
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void battleshipRuMissileAppliesLegacyAreaImpact(GameTestHelper helper) {
        LegacyShipEntity ship = ModEntityTypes.LEGACY_SHIP.get().create(helper.getLevel());
        Zombie primaryTarget = EntityType.ZOMBIE.create(helper.getLevel());
        Zombie nearbyTarget = EntityType.ZOMBIE.create(helper.getLevel());
        if (ship == null || primaryTarget == null || nearbyTarget == null) {
            helper.fail("legacy ship and targets should be creatable for Battleship Ru impact tests");
            return;
        }

        ship.setVariantEggMeta(15);
        ship.setNoAi(true);
        BlockPos shipPos = helper.absolutePos(new BlockPos(1, 2, 1));
        BlockPos primaryPos = helper.absolutePos(new BlockPos(4, 2, 1));
        BlockPos nearbyPos = helper.absolutePos(new BlockPos(4, 2, 2));
        ship.setPos(shipPos.getX() + 0.5D, shipPos.getY(), shipPos.getZ() + 0.5D);
        primaryTarget.setNoAi(true);
        primaryTarget.setPos(primaryPos.getX() + 0.5D, primaryPos.getY(), primaryPos.getZ() + 0.5D);
        nearbyTarget.setNoAi(true);
        nearbyTarget.setPos(nearbyPos.getX() + 0.5D, nearbyPos.getY(), nearbyPos.getZ() + 0.5D);
        helper.getLevel().addFreshEntity(ship);
        helper.getLevel().addFreshEntity(primaryTarget);
        helper.getLevel().addFreshEntity(nearbyTarget);

        float primaryHealthBefore = primaryTarget.getHealth();
        float nearbyHealthBefore = nearbyTarget.getHealth();
        boolean projectileRemoved = false;
        boolean damaged = false;
        for (int attempt = 0; attempt < 16 && !damaged; attempt++) {
            LegacyShipProjectileEntity projectile = LegacyShipProjectileEntity.create(
                    helper.getLevel(), ship, primaryTarget, LegacyShipAttackKind.HEAVY,
                    ship.getLegacyStats().attackHeavy() * 0.2F, false,
                    ship.getAttackProfile().projectileProfile(LegacyShipAttackKind.HEAVY),
                    primaryTarget.position(), 0.15D, false, 3.5D);
            projectile.setPos(primaryTarget.getX() - 0.5D, primaryTarget.getY() + 0.5D, primaryTarget.getZ());
            projectile.setDeltaMovement(new Vec3(0.5D, 0.0D, 0.0D));
            projectile.tickCount = 6;
            helper.getLevel().addFreshEntity(projectile);
            projectile.tick();
            projectileRemoved |= projectile.isRemoved();
            damaged = primaryTarget.getHealth() < primaryHealthBefore
                    || nearbyTarget.getHealth() < nearbyHealthBefore;
        }

        if (!projectileRemoved || !damaged) {
            helper.fail("Battleship Ru missile collision should resolve one legacy area impact");
            return;
        }

        ship.discard();
        primaryTarget.discard();
        nearbyTarget.discard();
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void battleshipRePushesLegacyNearbyTarget(GameTestHelper helper) {
        LegacyShipEntity ship = ModEntityTypes.LEGACY_SHIP.get().create(helper.getLevel());
        Zombie target = EntityType.ZOMBIE.create(helper.getLevel());
        if (ship == null || target == null) {
            helper.fail("legacy ship and target should be creatable for Battleship Re push tests");
            return;
        }

        ship.setVariantEggMeta(17);
        ship.setNoAi(true);
        ship.setGrudge(100);
        ship.setPos(1.5D, 2.0D, 1.5D);
        ship.setYRot(0.0F);
        target.setNoAi(true);
        target.setPos(6.5D, 2.0D, 1.5D);
        helper.getLevel().addFreshEntity(ship);
        helper.getLevel().addFreshEntity(target);

        // The legacy trigger includes a 1/5 random gate. Seed-independent tests
        // enter the selected-target state directly and exercise the same charge path.
        ship.beginLegacyRePush(target);
        if (!ship.isLegacyRePushActive() || ship.getLegacyRePushTarget() != target) {
            helper.fail("Battleship Re should retain its selected push target");
            return;
        }

        target.setPos(ship.getX(), ship.getY(), ship.getZ() + 2.0D);
        target.setDeltaMovement(Vec3.ZERO);
        ship.tickCount = 257;
        LegacyShipBehaviorCatalog.tickLegacyPerShipBehavior(ship);
        Vec3 motion = target.getDeltaMovement();
        if (ship.isLegacyRePushActive()
                || motion.y < 0.49D
                || Math.abs(motion.z) < 0.49D) {
            helper.fail("Battleship Re should push its selected target with the legacy velocity");
            return;
        }

        ship.discard();
        target.discard();
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void battleshipReLightAttackAddsLegacyNearbyHits(GameTestHelper helper) {
        LegacyShipEntity ship = ModEntityTypes.LEGACY_SHIP.get().create(helper.getLevel());
        Zombie primaryTarget = EntityType.ZOMBIE.create(helper.getLevel());
        List<Zombie> nearbyTargets = new ArrayList<>();
        for (int index = 0; index < 7; index++) {
            Zombie nearbyTarget = EntityType.ZOMBIE.create(helper.getLevel());
            if (nearbyTarget == null) {
                helper.fail("nearby zombie should be creatable for Battleship Re follow-up tests");
                return;
            }
            nearbyTargets.add(nearbyTarget);
        }
        if (ship == null || primaryTarget == null) {
            helper.fail("legacy ship and primary target should be creatable for Battleship Re follow-up tests");
            return;
        }

        ship.setVariantEggMeta(17);
        ship.setNoAi(true);
        ship.setShipLevel(100);
        BlockPos shipPos = helper.absolutePos(new BlockPos(1, 2, 1));
        BlockPos primaryPos = helper.absolutePos(new BlockPos(4, 2, 1));
        ship.setPos(shipPos.getX() + 0.5D, shipPos.getY(), shipPos.getZ() + 0.5D);
        primaryTarget.setNoAi(true);
        primaryTarget.setPos(primaryPos.getX() + 0.5D, primaryPos.getY(), primaryPos.getZ() + 0.5D);
        helper.getLevel().addFreshEntity(ship);
        helper.getLevel().addFreshEntity(primaryTarget);
        for (int index = 0; index < nearbyTargets.size(); index++) {
            Zombie nearbyTarget = nearbyTargets.get(index);
            nearbyTarget.setNoAi(true);
            nearbyTarget.setPos(primaryTarget.getX(), primaryTarget.getY(), primaryTarget.getZ() + (index - 3) * 0.75D);
            helper.getLevel().addFreshEntity(nearbyTarget);
        }

        LegacyShipBehaviorCatalog.AttackEffectProfile effect = LegacyShipBehaviorCatalog.attackEffectFor(
                ShipEntitySpecs.getByEggMeta(17), 100, 0);
        if (effect.effect() != MobEffects.DIG_SLOWDOWN
                || effect.amplifier() != 1
                || effect.durationTicks() != 200
                || effect.chancePercent() != 100) {
            helper.fail("Battleship Re should preserve the legacy mining-fatigue attack effect");
            return;
        }

        int hitCount = 0;
        for (int attempt = 0; attempt < 16 && hitCount == 0; attempt++) {
            hitCount = LegacyShipBehaviorCatalog.applyLegacyPostAttackMotion(
                    ship, LegacyShipAttackKind.LIGHT, primaryTarget, 20.0F);
        }
        if (hitCount <= 0 || hitCount > 7) {
            helper.fail("Battleship Re light hit should resolve a capped nearby follow-up attack");
            return;
        }

        ship.discard();
        primaryTarget.discard();
        for (Zombie nearbyTarget : nearbyTargets) {
            nearbyTarget.discard();
        }
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void shimakazeHeavyAttackLaunchesFiveLegacyMissiles(GameTestHelper helper) {
        LegacyShipEntity ship = ModEntityTypes.LEGACY_SHIP.get().create(helper.getLevel());
        Zombie target = EntityType.ZOMBIE.create(helper.getLevel());
        if (ship == null || target == null) {
            helper.fail("legacy ship and target should be creatable for Shimakaze heavy tests");
            return;
        }

        ship.setVariantEggMeta(38);
        ship.setOwner(UUID.randomUUID(), "ShimakazeHeavy");
        ship.setLightAmmo(100);
        ship.setHeavyAmmo(100);
        ship.setGrudge(100);
        BlockPos shipPos = helper.absolutePos(new BlockPos(1, 2, 1));
        BlockPos targetPos = helper.absolutePos(new BlockPos(3, 2, 1));
        ship.setPos(shipPos.getX() + 0.5D, shipPos.getY(), shipPos.getZ() + 0.5D);
        target.setPos(targetPos.getX() + 0.5D, targetPos.getY(), targetPos.getZ() + 0.5D);
        target.setNoAi(true);
        target.setInvulnerable(true);
        helper.getLevel().addFreshEntity(ship);
        helper.getLevel().addFreshEntity(target);

        int heavyAmmoBefore = ship.getHeavyAmmo();
        int grudgeBefore = ship.getGrudge();
        int moraleBefore = ship.getMorale();
        float healthBefore = target.getHealth();
        if (!ship.performPlayerCompatAttack(target, LegacyShipAttackKind.HEAVY)) {
            helper.fail("Shimakaze heavy attack should launch its five-missile burst");
            return;
        }

        List<LegacyShipProjectileEntity> projectiles = helper.getLevel().getEntitiesOfClass(
                LegacyShipProjectileEntity.class,
                ship.getBoundingBox().inflate(24.0D),
                projectile -> projectile.getOwner() == ship
                        && projectile.getAttackKind() == LegacyShipAttackKind.HEAVY);
        if (projectiles.size() != 5) {
            helper.fail("Shimakaze heavy attack should launch exactly five missiles, got " + projectiles.size());
            return;
        }
        for (LegacyShipProjectileEntity projectile : projectiles) {
            if (projectile.getProjectileVisual() != LegacyShipProjectileVisual.MISSILE
                    || projectile.getMoveType() != LegacyShipProjectileMoveType.DIRECT
                    || projectile.getIntendedTargetId() != target.getId()
                    || Math.abs(projectile.getY() - (ship.getY() + ship.getBbHeight() * 0.7D)) > 1.0E-6D
                    || Math.abs(projectile.getDeltaMovement().length() - 0.7D) > 1.0E-5D) {
                helper.fail("Shimakaze heavy burst should use the legacy missile profile and target");
                return;
            }
        }
        if (ship.getHeavyAmmo() != heavyAmmoBefore - 1
                || ship.getGrudge() != grudgeBefore - 8
                || ship.getMorale() != moraleBefore - 2
                || target.getHealth() != healthBefore
                || ship.getCompatAttackCooldown(LegacyShipAttackKind.HEAVY) <= 0) {
            helper.fail("Shimakaze heavy attack should preserve legacy resource and projectile-only behavior");
            return;
        }

        ship.discard();
        target.discard();
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void shimakazeBlockHeavyAttackLaunchesFiveLegacyMissiles(GameTestHelper helper) {
        LegacyShipEntity ship = ModEntityTypes.LEGACY_SHIP.get().create(helper.getLevel());
        if (ship == null) {
            helper.fail("legacy ship should be creatable for Shimakaze block-target tests");
            return;
        }

        ship.setVariantEggMeta(38);
        ship.setOwner(UUID.randomUUID(), "ShimakazeBlock");
        ship.setNoAi(true);
        ship.setLightAmmo(100);
        ship.setHeavyAmmo(100);
        ship.setGrudge(100);
        BlockPos shipPos = helper.absolutePos(new BlockPos(1, 2, 1));
        BlockPos targetPos = helper.absolutePos(new BlockPos(3, 2, 1));
        ship.setPos(shipPos.getX() + 0.5D, shipPos.getY(), shipPos.getZ() + 0.5D);
        helper.getLevel().addFreshEntity(ship);

        int heavyAmmoBefore = ship.getHeavyAmmo();
        int grudgeBefore = ship.getGrudge();
        int moraleBefore = ship.getMorale();
        List<LegacyShipProjectileEntity> projectilesBefore = helper.getLevel().getEntitiesOfClass(
                LegacyShipProjectileEntity.class,
                ship.getBoundingBox().inflate(24.0D),
                projectile -> projectile.getOwner() == ship);
        if (!projectilesBefore.isEmpty()
                || !ship.performPlayerCompatAttack(targetPos, LegacyShipAttackKind.HEAVY)) {
            helper.fail("Shimakaze block heavy attack should launch its five-missile burst");
            return;
        }

        List<LegacyShipProjectileEntity> projectiles = helper.getLevel().getEntitiesOfClass(
                LegacyShipProjectileEntity.class,
                ship.getBoundingBox().inflate(24.0D),
                projectile -> projectile.getOwner() == ship
                        && projectile.getAttackKind() == LegacyShipAttackKind.HEAVY);
        if (projectiles.size() != 5) {
            helper.fail("Shimakaze block heavy attack should launch exactly five missiles, got " + projectiles.size());
            return;
        }
        for (LegacyShipProjectileEntity projectile : projectiles) {
            if (projectile.getProjectileVisual() != LegacyShipProjectileVisual.MISSILE
                    || projectile.getMoveType() != LegacyShipProjectileMoveType.DIRECT
                    || projectile.getIntendedTargetId() != -1
                    || Math.abs(projectile.getY() - (ship.getY() + ship.getBbHeight() * 0.7D)) > 1.0E-6D) {
                helper.fail("Shimakaze ground-target missiles should preserve the legacy direct profile");
                return;
            }
        }
        if (ship.getHeavyAmmo() != heavyAmmoBefore - 1
                || ship.getGrudge() != grudgeBefore - 8
                || ship.getMorale() != moraleBefore - 2
                || ship.getCompatAttackCooldown(LegacyShipAttackKind.HEAVY) <= 0) {
            helper.fail("Shimakaze ground-target heavy attack should preserve legacy resource costs");
            return;
        }

        ship.discard();
        for (LegacyShipProjectileEntity projectile : projectiles) {
            projectile.discard();
        }
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void shimakazeHeavyMissileSpreadMatchesLegacyVariants(GameTestHelper helper) {
        ShipEntitySpec friendly = ShipEntitySpecs.getByEggMeta(38);
        ShipEntitySpec hostile = ShipEntitySpecs.getByEggMeta(2038);
        if (Math.abs(LegacyShipBehaviorCatalog.shimakazeHeavyMissileSpreadFor(friendly, 0) - 3.5D) > 1.0E-6D
                || Math.abs(LegacyShipBehaviorCatalog.shimakazeHeavyMissileSpreadFor(hostile, 0) - 3.0D) > 1.0E-6D
                || Math.abs(LegacyShipBehaviorCatalog.shimakazeHeavyMissileSpreadFor(hostile, 3) - 4.5D) > 1.0E-6D
                || LegacyShipBehaviorCatalog.attackProfile(friendly)
                    .projectileProfile(LegacyShipAttackKind.HEAVY).baseSpeed() != 0.7D
                || LegacyShipBehaviorCatalog.attackProfile(friendly)
                    .projectileProfile(LegacyShipAttackKind.HEAVY).launchHeightFactor() != 0.7F
                || LegacyShipBehaviorCatalog.attackProfile(friendly)
                    .projectileProfile(LegacyShipAttackKind.HEAVY).moveType() != LegacyShipProjectileMoveType.DIRECT
                || LegacyShipBehaviorCatalog.attackProfile(hostile)
                    .projectileProfile(LegacyShipAttackKind.HEAVY).baseSpeed() != 0.5D
                || LegacyShipBehaviorCatalog.attackProfile(hostile)
                    .projectileProfile(LegacyShipAttackKind.HEAVY).launchHeightFactor() != 0.7F) {
            helper.fail("Shimakaze heavy missile speed, launch height, and spread should match legacy variants");
            return;
        }

        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void shimakazeConstructorsKeepLegacyDimensionsAndEyeHeight(GameTestHelper helper) {
        LegacyShipEntity friendly = ModEntityTypes.LEGACY_SHIP.get().create(helper.getLevel());
        LegacyShipEntity hostile = ModEntityTypes.LEGACY_SHIP.get().create(helper.getLevel());
        if (friendly == null || hostile == null) {
            helper.fail("Shimakaze variants should be creatable for constructor compatibility");
            return;
        }

        friendly.setVariantEggMeta(38);
        if (Math.abs(friendly.getBbWidth() - 0.5F) > 1.0E-6F
                || Math.abs(friendly.getBbHeight() - 1.6F) > 1.0E-6F
                || Math.abs(friendly.getEyeHeight() - 1.5F) > 1.0E-6F) {
            helper.fail("friendly Shimakaze should keep the legacy constructor size and eye height");
            return;
        }

        float[] hostileWidths = {0.5F, 0.9F, 1.3F, 1.7F};
        float[] hostileHeights = {1.6F, 3.2F, 4.8F, 6.4F};
        for (int scale = 0; scale < hostileWidths.length; scale++) {
            EntityDimensions dimensions = LegacyShipBehaviorCatalog.dimensionsFor(
                    ShipEntitySpecs.getByEggMeta(2038), scale);
            if (Math.abs(dimensions.width - hostileWidths[scale]) > 1.0E-6F
                    || Math.abs(dimensions.height - hostileHeights[scale]) > 1.0E-6F) {
                helper.fail("hostile Shimakaze scale should keep the legacy constructor dimensions: scale=" + scale);
                return;
            }
        }

        hostile.setVariantEggMeta(2038);
        if (Math.abs(hostile.getBbWidth() - 0.5F) > 1.0E-6F
                || Math.abs(hostile.getBbHeight() - 1.6F) > 1.0E-6F
                || Math.abs(hostile.getEyeHeight() - hostile.getBbHeight() * 0.8F) > 1.0E-6F) {
            helper.fail("hostile Shimakaze should keep the legacy default eye-height rule");
            return;
        }

        friendly.discard();
        hostile.discard();
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void nagatoConstructorsKeepLegacyDimensions(GameTestHelper helper) {
        LegacyShipEntity friendly = ModEntityTypes.LEGACY_SHIP.get().create(helper.getLevel());
        LegacyShipEntity hostile = ModEntityTypes.LEGACY_SHIP.get().create(helper.getLevel());
        if (friendly == null || hostile == null) {
            helper.fail("Nagato variants should be creatable for constructor compatibility");
            return;
        }

        friendly.setVariantEggMeta(39);
        if (Math.abs(friendly.getBbWidth() - 0.7F) > 1.0E-6F
                || Math.abs(friendly.getBbHeight() - 2.0F) > 1.0E-6F
                || Math.abs(friendly.getEyeHeight() - 1.6F) > 1.0E-6F) {
            helper.fail("friendly Nagato should keep the legacy constructor size and eye height");
            return;
        }

        float[] hostileWidths = {0.7F, 1.2F, 1.7F, 2.2F};
        float[] hostileHeights = {2.0F, 4.0F, 6.0F, 8.0F};
        for (int scale = 0; scale < hostileWidths.length; scale++) {
            EntityDimensions dimensions = LegacyShipBehaviorCatalog.dimensionsFor(
                    ShipEntitySpecs.getByEggMeta(2039), scale);
            if (Math.abs(dimensions.width - hostileWidths[scale]) > 1.0E-6F
                    || Math.abs(dimensions.height - hostileHeights[scale]) > 1.0E-6F) {
                helper.fail("hostile Nagato scale should keep the legacy constructor dimensions: scale=" + scale);
                return;
            }
        }

        hostile.setVariantEggMeta(2039);
        if (Math.abs(hostile.getBbWidth() - 0.7F) > 1.0E-6F
                || Math.abs(hostile.getBbHeight() - 2.0F) > 1.0E-6F
                || Math.abs(hostile.getEyeHeight() - 1.6F) > 1.0E-6F) {
            helper.fail("hostile Nagato should keep the legacy base constructor dimensions");
            return;
        }

        friendly.discard();
        hostile.discard();
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void yamatoConstructorsKeepLegacyDimensions(GameTestHelper helper) {
        LegacyShipEntity friendly = ModEntityTypes.LEGACY_SHIP.get().create(helper.getLevel());
        LegacyShipEntity hostile = ModEntityTypes.LEGACY_SHIP.get().create(helper.getLevel());
        if (friendly == null || hostile == null) {
            helper.fail("Yamato variants should be creatable for constructor compatibility");
            return;
        }

        friendly.setVariantEggMeta(48);
        if (Math.abs(friendly.getBbWidth() - 0.8F) > 1.0E-6F
                || Math.abs(friendly.getBbHeight() - 2.1F) > 1.0E-6F
                || Math.abs(friendly.getEyeHeight() - 1.68F) > 1.0E-6F) {
            helper.fail("friendly Yamato should keep the legacy constructor size and eye height");
            return;
        }

        float[] hostileWidths = {0.8F, 1.3F, 1.8F, 2.3F};
        float[] hostileHeights = {2.1F, 4.2F, 6.3F, 8.4F};
        for (int scale = 0; scale < hostileWidths.length; scale++) {
            EntityDimensions dimensions = LegacyShipBehaviorCatalog.dimensionsFor(
                    ShipEntitySpecs.getByEggMeta(2048), scale);
            if (Math.abs(dimensions.width - hostileWidths[scale]) > 1.0E-6F
                    || Math.abs(dimensions.height - hostileHeights[scale]) > 1.0E-6F) {
                helper.fail("hostile Yamato scale should keep the legacy constructor dimensions: scale=" + scale);
                return;
            }
        }

        hostile.setVariantEggMeta(2048);
        if (Math.abs(hostile.getBbWidth() - 0.8F) > 1.0E-6F
                || Math.abs(hostile.getBbHeight() - 2.1F) > 1.0E-6F
                || Math.abs(hostile.getEyeHeight() - 1.68F) > 1.0E-6F) {
            helper.fail("hostile Yamato should keep the legacy base constructor dimensions");
            return;
        }

        friendly.discard();
        hostile.discard();
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void kagaAndAkagiConstructorsKeepLegacyDimensions(GameTestHelper helper) {
        LegacyShipEntity kaga = ModEntityTypes.LEGACY_SHIP.get().create(helper.getLevel());
        LegacyShipEntity akagi = ModEntityTypes.LEGACY_SHIP.get().create(helper.getLevel());
        if (kaga == null || akagi == null) {
            helper.fail("Kaga and Akagi should be creatable for constructor compatibility");
            return;
        }

        kaga.setVariantEggMeta(49);
        akagi.setVariantEggMeta(50);
        if (Math.abs(kaga.getBbWidth() - 0.6F) > 1.0E-6F
                || Math.abs(kaga.getBbHeight() - 1.875F) > 1.0E-6F
                || Math.abs(kaga.getEyeHeight() - 1.5F) > 1.0E-6F
                || Math.abs(akagi.getBbWidth() - 0.6F) > 1.0E-6F
                || Math.abs(akagi.getBbHeight() - 1.875F) > 1.0E-6F
                || Math.abs(akagi.getEyeHeight() - 1.5F) > 1.0E-6F) {
            helper.fail("friendly Kaga and Akagi should keep the legacy constructor size and eye height");
            return;
        }

        float[] hostileWidths = {0.6F, 1.2F, 1.8F, 2.4F};
        float[] hostileHeights = {1.875F, 3.75F, 5.625F, 7.5F};
        for (int scale = 0; scale < hostileWidths.length; scale++) {
            EntityDimensions dimensions = LegacyShipBehaviorCatalog.dimensionsFor(
                    ShipEntitySpecs.getByEggMeta(2049), scale);
            if (Math.abs(dimensions.width - hostileWidths[scale]) > 1.0E-6F
                    || Math.abs(dimensions.height - hostileHeights[scale]) > 1.0E-6F) {
                helper.fail("hostile Kaga scale should keep the legacy constructor dimensions: scale=" + scale);
                return;
            }
        }

        kaga.discard();
        akagi.discard();
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void atagoAndTakaoConstructorsKeepLegacyDimensions(GameTestHelper helper) {
        LegacyShipEntity atago = ModEntityTypes.LEGACY_SHIP.get().create(helper.getLevel());
        LegacyShipEntity takao = ModEntityTypes.LEGACY_SHIP.get().create(helper.getLevel());
        if (atago == null || takao == null) {
            helper.fail("Atago and Takao should be creatable for constructor compatibility");
            return;
        }

        atago.setVariantEggMeta(60);
        takao.setVariantEggMeta(61);
        if (Math.abs(atago.getBbWidth() - 0.7F) > 1.0E-6F
                || Math.abs(atago.getBbHeight() - 1.75F) > 1.0E-6F
                || Math.abs(atago.getEyeHeight() - 1.4F) > 1.0E-6F
                || Math.abs(takao.getBbWidth() - 0.7F) > 1.0E-6F
                || Math.abs(takao.getBbHeight() - 1.75F) > 1.0E-6F
                || Math.abs(takao.getEyeHeight() - 1.4F) > 1.0E-6F) {
            helper.fail("friendly Atago and Takao should keep the legacy constructor size and eye height");
            return;
        }

        float[] hostileWidths = {0.75F, 0.9F, 1.3F, 1.7F};
        float[] hostileHeights = {1.75F, 3.5F, 5.25F, 7.0F};
        int[] hostileEggMetas = {2060, 2061};
        for (int hostileEggMeta : hostileEggMetas) {
            for (int scale = 0; scale < hostileWidths.length; scale++) {
                EntityDimensions dimensions = LegacyShipBehaviorCatalog.dimensionsFor(
                        ShipEntitySpecs.getByEggMeta(hostileEggMeta), scale);
                if (Math.abs(dimensions.width - hostileWidths[scale]) > 1.0E-6F
                        || Math.abs(dimensions.height - hostileHeights[scale]) > 1.0E-6F) {
                    helper.fail("hostile Atago/Takao scale should keep the legacy constructor dimensions: eggMeta="
                            + hostileEggMeta + ", scale=" + scale);
                    return;
                }
            }
        }

        atago.setVariantEggMeta(2060);
        takao.setVariantEggMeta(2061);
        if (Math.abs(atago.getBbWidth() - 0.75F) > 1.0E-6F
                || Math.abs(atago.getBbHeight() - 1.75F) > 1.0E-6F
                || Math.abs(takao.getBbWidth() - 0.75F) > 1.0E-6F
                || Math.abs(takao.getBbHeight() - 1.75F) > 1.0E-6F) {
            helper.fail("hostile Atago and Takao should keep the legacy base constructor dimensions");
            return;
        }

        atago.discard();
        takao.discard();
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void kongouClassConstructorsKeepLegacyDimensions(GameTestHelper helper) {
        int[] friendlyEggMetas = {62, 63, 64, 65};
        for (int eggMeta : friendlyEggMetas) {
            LegacyShipEntity ship = ModEntityTypes.LEGACY_SHIP.get().create(helper.getLevel());
            if (ship == null) {
                helper.fail("Kongou-class ships should be creatable for constructor compatibility");
                return;
            }

            ship.setVariantEggMeta(eggMeta);
            if (Math.abs(ship.getBbWidth() - 0.6F) > 1.0E-6F
                    || Math.abs(ship.getBbHeight() - 1.875F) > 1.0E-6F
                    || Math.abs(ship.getEyeHeight() - 1.5F) > 1.0E-6F) {
                helper.fail("friendly Kongou-class ships should keep the legacy constructor size and eye height: eggMeta="
                        + eggMeta);
                return;
            }
            ship.discard();
        }

        float[] hostileWidths = {0.6F, 1.2F, 1.8F, 2.4F};
        float[] hostileHeights = {1.875F, 3.75F, 5.625F, 7.5F};
        int[] hostileEggMetas = {2062, 2063, 2064, 2065};
        for (int hostileEggMeta : hostileEggMetas) {
            for (int scale = 0; scale < hostileWidths.length; scale++) {
                EntityDimensions dimensions = LegacyShipBehaviorCatalog.dimensionsFor(
                        ShipEntitySpecs.getByEggMeta(hostileEggMeta), scale);
                if (Math.abs(dimensions.width - hostileWidths[scale]) > 1.0E-6F
                        || Math.abs(dimensions.height - hostileHeights[scale]) > 1.0E-6F) {
                    helper.fail("hostile Kongou-class scale should keep the legacy constructor dimensions: eggMeta="
                            + hostileEggMeta + ", scale=" + scale);
                    return;
                }
            }
        }

        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void tenryuuAndTatsutaConstructorsKeepLegacyDimensions(GameTestHelper helper) {
        int[] friendlyEggMetas = {58, 59};
        for (int eggMeta : friendlyEggMetas) {
            LegacyShipEntity ship = ModEntityTypes.LEGACY_SHIP.get().create(helper.getLevel());
            if (ship == null) {
                helper.fail("Tenryuu and Tatsuta should be creatable for constructor compatibility");
                return;
            }

            ship.setVariantEggMeta(eggMeta);
            if (Math.abs(ship.getBbWidth() - 0.75F) > 1.0E-6F
                    || Math.abs(ship.getBbHeight() - 1.65F) > 1.0E-6F
                    || Math.abs(ship.getEyeHeight() - 1.32F) > 1.0E-6F) {
                helper.fail("friendly Tenryuu/Tatsuta should keep the legacy constructor size and eye height: eggMeta="
                        + eggMeta);
                return;
            }
            ship.discard();
        }

        LegacyShipEntity tenryuu = ModEntityTypes.LEGACY_SHIP.get().create(helper.getLevel());
        if (tenryuu == null) {
            helper.fail("Tenryuu should be creatable for the legacy riding offset test");
            return;
        }
        tenryuu.setVariantEggMeta(58);
        if (Math.abs(tenryuu.getPassengersRidingOffset() - tenryuu.getBbHeight() * 0.7D) > 1.0E-6D) {
            helper.fail("standing Tenryuu should keep the legacy 0.7 riding factor");
            return;
        }
        tenryuu.setOrderedToSit(true);
        tenryuu.setEmotion(0);
        if (Math.abs(tenryuu.getPassengersRidingOffset() - tenryuu.getBbHeight() * 0.3D) > 1.0E-6D) {
            helper.fail("normal sitting Tenryuu should keep the legacy 0.3 riding factor");
            return;
        }
        tenryuu.setEmotion(4);
        if (Math.abs(tenryuu.getPassengersRidingOffset() - tenryuu.getBbHeight() * 0.2D) > 1.0E-6D) {
            helper.fail("bored sitting Tenryuu should keep the legacy 0.2 riding factor");
            return;
        }
        tenryuu.discard();

        LegacyShipEntity tatsuta = ModEntityTypes.LEGACY_SHIP.get().create(helper.getLevel());
        if (tatsuta == null) {
            helper.fail("Tatsuta should be creatable for the legacy riding offset test");
            return;
        }
        tatsuta.setVariantEggMeta(59);
        if (Math.abs(tatsuta.getPassengersRidingOffset() - tatsuta.getBbHeight() * 0.7D) > 1.0E-6D) {
            helper.fail("standing Tatsuta should keep the legacy 0.7 riding factor");
            return;
        }
        tatsuta.setOrderedToSit(true);
        tatsuta.setEmotion(0);
        if (Math.abs(tatsuta.getPassengersRidingOffset() - tatsuta.getBbHeight() * 0.27D) > 1.0E-6D) {
            helper.fail("normal sitting Tatsuta should keep the legacy 0.27 riding factor");
            return;
        }
        tatsuta.setEmotion(4);
        if (Math.abs(tatsuta.getPassengersRidingOffset() - tatsuta.getBbHeight() * 0.2D) > 1.0E-6D) {
            helper.fail("bored sitting Tatsuta should keep the legacy 0.2 riding factor");
            return;
        }
        tatsuta.discard();

        float[] hostileWidths = {0.75F, 0.9F, 1.3F, 1.7F};
        float[] hostileHeights = {1.65F, 3.2F, 4.8F, 6.4F};
        int[] hostileEggMetas = {2058, 2059};
        for (int hostileEggMeta : hostileEggMetas) {
            for (int scale = 0; scale < hostileWidths.length; scale++) {
                EntityDimensions dimensions = LegacyShipBehaviorCatalog.dimensionsFor(
                        ShipEntitySpecs.getByEggMeta(hostileEggMeta), scale);
                if (Math.abs(dimensions.width - hostileWidths[scale]) > 1.0E-6F
                        || Math.abs(dimensions.height - hostileHeights[scale]) > 1.0E-6F) {
                    helper.fail("hostile Tenryuu/Tatsuta scale should keep the legacy constructor dimensions: eggMeta="
                            + hostileEggMeta + ", scale=" + scale);
                    return;
                }
            }
        }

        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void tenryuuHeavyAttackRunsLegacySkillPhases(GameTestHelper helper) {
        LegacyShipEntity ship = ModEntityTypes.LEGACY_SHIP.get().create(helper.getLevel());
        Zombie target = EntityType.ZOMBIE.create(helper.getLevel());
        if (ship == null || target == null) {
            helper.fail("legacy ship and target should be creatable for Tenryuu skill tests");
            return;
        }

        ship.setVariantEggMeta(58);
        ship.setOwner(UUID.randomUUID(), "TenryuuSkill");
        ship.setShipLevel(80);
        ship.setMorale(3000);
        ship.setHeavyAmmo(100);
        ship.setGrudge(100);
        ship.setTarget(target);
        ship.setNoAi(true);
        ship.setInvulnerable(true);
        BlockPos shipPos = helper.absolutePos(new BlockPos(1, 2, 1));
        BlockPos targetPos = helper.absolutePos(new BlockPos(5, 2, 1));
        ship.setPos(shipPos.getX() + 0.5D, shipPos.getY(), shipPos.getZ() + 0.5D);
        target.setPos(targetPos.getX() + 0.5D, targetPos.getY(), targetPos.getZ() + 0.5D);
        target.setNoAi(true);
        target.setInvulnerable(true);
        helper.getLevel().addFreshEntity(ship);
        helper.getLevel().addFreshEntity(target);

        int ammoBefore = ship.getHeavyAmmo();
        int grudgeBefore = ship.getGrudge();
        int moraleBefore = ship.getMorale();
        int experienceBefore = ship.getShipExperience();
        int experienceGain = Math.max(0, (int) (5F * ship.getLegacyStats().get(LegacyShipStatTables.Attr.XP)));
        int ammoCost = LegacyShipBehaviorCatalog.attackAmmoCostFor(
                ship.getSpec(), LegacyShipAttackKind.HEAVY);

        if (!ship.performPlayerCompatAttack(target, LegacyShipAttackKind.HEAVY)
                || ship.getDisplayPhase() != -1
                || ship.getHeavyAmmo() != ammoBefore - ammoCost
                || ship.getGrudge() != grudgeBefore - 8
                || ship.getMorale() != moraleBefore - 2
                || ship.getShipExperience() != experienceBefore + experienceGain) {
            helper.fail("Tenryuu first heavy attack should enter the legacy charge pre-phase and consume resources");
            return;
        }

        // Direct GameTest ticks do not pass through ServerLevel's tickCount increment.
        ship.tickCount = 1;
        int guard = 0;
        while (ship.getCompatAttackCooldown(LegacyShipAttackKind.HEAVY) > 0 && guard++ < 2000) {
            ship.tick();
        }
        int expectedSweeps = 3 + (int) (ship.getShipLevel() * 0.03F);
        if (ship.getCompatAttackCooldown(LegacyShipAttackKind.HEAVY) > 0
                || !ship.performPlayerCompatAttack(target, LegacyShipAttackKind.HEAVY)
                || ship.getDisplayPhase() != 1
                || ship.getLegacySkillTimer() != 0
                || ship.getLegacySkillRemaining() != expectedSweeps
                || ship.isPushable()) {
            helper.fail("Tenryuu second heavy attack should enter phase 1 with the legacy level-scaled sweep count"
                    + " (phase=" + ship.getDisplayPhase()
                    + ", cooldown=" + ship.getCompatAttackCooldown(LegacyShipAttackKind.HEAVY)
                    + ", remaining=" + ship.getLegacySkillRemaining()
                    + ", expected=" + expectedSweeps
                    + ", target=" + ship.getTarget()
                    + ", shipPos=" + ship.position()
                    + ", targetPos=" + target.position()
                    + ", pushable=" + ship.isPushable()
                    + ", alive=" + ship.isAlive()
                    + ", deadOrDying=" + ship.isDeadOrDying()
                    + ", removed=" + ship.isRemoved()
                    + ", health=" + ship.getHealth()
                    + ", maxHealth=" + ship.getMaxHealth()
                    + ", tickCount=" + ship.tickCount
                    + ", delta=" + ship.getDeltaMovement() + ")");
            return;
        }

        ship.tick();
        if (ship.getDisplayPhase() != 2
                || ship.getLegacySkillTimer() != 6
                || ship.getLegacySkillRemaining() != expectedSweeps - 1
                || ship.isPushable()) {
            helper.fail("Tenryuu first skill sweep should enter phase 2 with the legacy seven-tick window");
            return;
        }

        guard = 0;
        while (ship.getDisplayPhase() != 3 && guard++ < 400) {
            ship.tick();
        }
        if (ship.getDisplayPhase() != 3
                || ship.getLegacySkillRemaining() != 0
                || ship.getLegacySkillTimer() != 8
                || ship.isPushable()) {
            helper.fail("Tenryuu final strike should enter phase 3 after the legacy horizontal sweeps");
            return;
        }

        guard = 0;
        while (ship.getDisplayPhase() != 0 && guard++ < 32) {
            ship.tick();
        }
        if (ship.getDisplayPhase() != 0 || !ship.isPushable()
                || ship.getHeavyAmmo() != ammoBefore - ammoCost * 2
                || ship.getGrudge() != grudgeBefore - 16
                || ship.getMorale() != moraleBefore - 4
                || ship.getShipExperience() != experienceBefore + experienceGain * 2) {
            helper.fail("Tenryuu skill should restore the legacy movable state after its final window");
            return;
        }

        ship.discard();
        target.discard();
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void hostileTatsutaSkillKeepsLegacyFreeAttackEntry(GameTestHelper helper) {
        LegacyShipEntity ship = ModEntityTypes.LEGACY_SHIP.get().create(helper.getLevel());
        Zombie target = EntityType.ZOMBIE.create(helper.getLevel());
        if (ship == null || target == null) {
            helper.fail("hostile Tatsuta and target should be creatable for skill entry tests");
            return;
        }

        ship.setVariantEggMeta(2059);
        ship.initializeHostileRuntime(false, true, true);
        ship.setTarget(target);
        ship.setNoAi(true);
        BlockPos shipPos = helper.absolutePos(new BlockPos(1, 2, 1));
        BlockPos targetPos = helper.absolutePos(new BlockPos(5, 2, 1));
        ship.setPos(shipPos.getX() + 0.5D, shipPos.getY(), shipPos.getZ() + 0.5D);
        target.setPos(targetPos.getX() + 0.5D, targetPos.getY(), targetPos.getZ() + 0.5D);
        target.setNoAi(true);
        target.setInvulnerable(true);
        helper.getLevel().addFreshEntity(ship);
        helper.getLevel().addFreshEntity(target);

        int ammoBefore = ship.getHeavyAmmo();
        int grudgeBefore = ship.getGrudge();
        int moraleBefore = ship.getMorale();
        int experienceBefore = ship.getShipExperience();
        if (!ship.performPlayerCompatAttack(target, LegacyShipAttackKind.HEAVY)
                || ship.getDisplayPhase() != -1
                || ship.getHeavyAmmo() != ammoBefore
                || ship.getGrudge() != grudgeBefore
                || ship.getMorale() != moraleBefore
                || ship.getShipExperience() != experienceBefore) {
            helper.fail("hostile Tatsuta charge should not consume friendly attack resources");
            return;
        }

        int guard = 0;
        while (ship.getCompatAttackCooldown(LegacyShipAttackKind.HEAVY) > 0 && guard++ < 2000) {
            ship.tick();
        }
        if (ship.getCompatAttackCooldown(LegacyShipAttackKind.HEAVY) > 0
                || !ship.performPlayerCompatAttack(target, LegacyShipAttackKind.HEAVY)
                || ship.getDisplayPhase() != 1
                || ship.getLegacySkillTimer() != 10
                || ship.getLegacySkillRemaining() != 4
                || ship.getHeavyAmmo() != ammoBefore
                || ship.getGrudge() != grudgeBefore
                || ship.getMorale() != moraleBefore
                || ship.getShipExperience() != experienceBefore) {
            helper.fail("hostile Tatsuta should enter the scale-based legacy skill without resource cost");
            return;
        }

        ship.discard();
        target.discard();
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void shimakazeRensouhouStateKeepsLegacyLimitsAndNbt(GameTestHelper helper) {
        LegacyShipEntity ship = ModEntityTypes.LEGACY_SHIP.get().create(helper.getLevel());
        LegacyShipEntity loaded = ModEntityTypes.LEGACY_SHIP.get().create(helper.getLevel());
        LegacyShipEntity hostile = ModEntityTypes.LEGACY_SHIP.get().create(helper.getLevel());
        if (ship == null || loaded == null || hostile == null) {
            helper.fail("legacy ships should be creatable for Shimakaze state tests");
            return;
        }

        ship.setVariantEggMeta(38);
        ship.setShimakazeRensouhouCount(2);
        CompoundTag saved = new CompoundTag();
        ship.addAdditionalSaveData(saved);
        loaded.readAdditionalSaveData(saved);
        if (loaded.getShimakazeRensouhouCount() != 2) {
            helper.fail("Shimakaze servant count should persist through modern NBT");
            return;
        }

        CompoundTag legacy = saved.copy();
        legacy.remove("ShipRensouhouCount");
        legacy.putInt("NumRensouhou", 4);
        LegacyShipEntity legacyLoaded = ModEntityTypes.LEGACY_SHIP.get().create(helper.getLevel());
        if (legacyLoaded == null) {
            helper.fail("legacy ship should be creatable for Shimakaze legacy NBT tests");
            return;
        }
        legacyLoaded.readAdditionalSaveData(legacy);
        if (legacyLoaded.getShimakazeRensouhouCount() != 4) {
            helper.fail("Shimakaze servant count should migrate the legacy NumRensouhou field");
            return;
        }

        hostile.setVariantEggMeta(2038);
        hostile.setShimakazeRensouhouCount(99);
        if (hostile.getShimakazeRensouhouCount() != 10) {
            helper.fail("hostile Shimakaze should keep the legacy ten-servant cap");
            return;
        }
        hostile.setShimakazeRensouhouCount(9);
        hostile.tickCount = 128;
        helper.getLevel().addFreshEntity(hostile);
        hostile.tick();
        if (hostile.getShimakazeRensouhouCount() != 10) {
            helper.fail("hostile Shimakaze should recover one servant every 128 ticks");
            return;
        }

        ship.discard();
        loaded.discard();
        hostile.discard();
        legacyLoaded.discard();
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void legacySubmarineVariantsKeepFriendlyRoleAndRingBehavior(GameTestHelper helper) {
        int[] eggMetas = {19, 20, 21};
        int[] classIds = {17, 18, 19};
        float[] invisibleLevels = {0.2F, 0.2F, 0.25F};
        boolean[] equippedModels = {false, true, false};
        double[] standingOffsets = {0.69D, 0.58D, 0.69D};
        double[] sittingOffsets = {0.0D, 0.42D, 0.0D};
        double[] boredSittingOffsets = {0.25D, 0.55D, 0.48D};
        LegacyShipEntity[] ships = new LegacyShipEntity[eggMetas.length];

        for (int index = 0; index < eggMetas.length; index++) {
            ShipEntitySpec spec = ShipEntitySpecs.getByEggMeta(eggMetas[index]);
            if (spec.hostile()
                    || spec.legacyClassId() != classIds[index]
                    || !ShipEntitySpecs.primaryPool().contains(spec)
                    || LegacyShipBehaviorCatalog.invisibleLevelFor(spec) != invisibleLevels[index]
                    || LegacyShipBehaviorCatalog.floatingDepthFor(spec) != 1.0D
                    || Math.abs(LegacyShipBehaviorCatalog.dimensionsFor(spec, 0).width - 0.6F) > 1.0E-6F
                    || Math.abs(LegacyShipBehaviorCatalog.dimensionsFor(spec, 0).height - 1.8F) > 1.0E-6F
                    || !LegacyShipBehaviorCatalog.supportsItemPickup(spec)) {
                helper.fail("legacy submarine egg " + eggMetas[index]
                        + " should remain a friendly small-build ship with its 1.12 behavior values");
                return;
            }

            LegacyShipEntity ship = ModEntityTypes.LEGACY_SHIP.get().create(helper.getLevel());
            if (ship == null) {
                helper.fail("legacy submarine entity should be creatable for friendly variant tests");
                return;
            }
            ship.setVariantEggMeta(eggMetas[index]);
            ship.setOwner(UUID.randomUUID(), "SubmarinePassive", 1);
            ship.setMarried(true);
            ship.setShipLevel(60);
            ship.setGrudge(200);
            ship.setNoAi(true);
            ship.setNoGravity(true);
            ship.setPos(1.5D + index * 2.0D, 2.0D, 1.5D);
            helper.getLevel().addFreshEntity(ship);
            ships[index] = ship;
        }

        for (int index = 0; index < ships.length; index++) {
            LegacyShipEntity ship = ships[index];
            ship.setModelState(equippedModels[index] ? 1 : 0);
            ship.setOrderedToSit(false);
            if (Math.abs(ship.getPassengersRidingOffset() - ship.getBbHeight() * standingOffsets[index]) > 1.0E-6D) {
                helper.fail("submarine egg " + eggMetas[index] + " should keep its legacy standing riding offset");
                return;
            }

            ship.setOrderedToSit(true);
            ship.setEmotion(0);
            if (Math.abs(ship.getPassengersRidingOffset() - ship.getBbHeight() * sittingOffsets[index]) > 1.0E-6D) {
                helper.fail("submarine egg " + eggMetas[index] + " should keep its legacy normal sitting offset");
                return;
            }

            ship.setEmotion(4);
            if (Math.abs(ship.getPassengersRidingOffset() - ship.getBbHeight() * boredSittingOffsets[index]) > 1.0E-6D) {
                helper.fail("submarine egg " + eggMetas[index] + " should keep its legacy bored sitting offset");
                return;
            }
        }

        for (LegacyShipEntity ship : ships) {
            ship.tickCount = 128;
            ship.tick();
            if (ship.isHostileVariant() || !ship.hasEffect(MobEffects.INVISIBILITY)) {
                helper.fail("friendly Ka/Yo/So variants should apply the legacy self invisibility ring passive"
                        + " (egg=" + ship.getVariantEggMeta()
                        + ", passive=" + LegacyShipBehaviorCatalog.behaviorFor(ship.getSpec()).marriagePassive()
                        + ", married=" + ship.isMarried()
                        + ", ring=" + ship.isRingEffectEnabled()
                        + ", grudge=" + ship.getGrudge()
                        + ", tick=" + ship.tickCount + ")");
                return;
            }
        }

        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void fiveSubmarineClassesKeepLegacyInvisibilityBoundaries(GameTestHelper helper) {
        int[] friendlyEggMetas = {19, 20, 21, 40, 41};
        int[] expectedClassIds = {17, 18, 19, 38, 39};
        LegacyShipEntity[] friendlyShips = new LegacyShipEntity[friendlyEggMetas.length];

        for (int index = 0; index < friendlyEggMetas.length; index++) {
            ShipEntitySpec spec = ShipEntitySpecs.getByEggMeta(friendlyEggMetas[index]);
            if (spec.hostile()
                    || spec.legacyClassId() != expectedClassIds[index]
                    || !LegacyShipBehaviorCatalog.usesPerShipRingInvisibility(spec)) {
                helper.fail("friendly submarine egg " + friendlyEggMetas[index]
                        + " should use the legacy per-ship invisibility path");
                return;
            }

            LegacyShipEntity ship = ModEntityTypes.LEGACY_SHIP.get().create(helper.getLevel());
            if (ship == null) {
                helper.fail("friendly submarine should be creatable for invisibility boundary tests");
                return;
            }

            ship.setVariantEggMeta(friendlyEggMetas[index]);
            ship.setShipLevel(60);
            ship.setGrudge(200);
            ship.setRingEffectEnabled(true);
            ship.setNoAi(true);
            ship.setPos(1.5D + index * 1.5D, 2.0D, 1.5D);
            helper.getLevel().addFreshEntity(ship);
            friendlyShips[index] = ship;
        }

        for (LegacyShipEntity ship : friendlyShips) {
            ship.tickCount = 128;
            LegacyShipBehaviorCatalog.tickLegacyPerShipBehavior(ship);
            MobEffectInstance invisibility = ship.getEffect(MobEffects.INVISIBILITY);
            if (invisibility == null || invisibility.getDuration() != 100) {
                helper.fail("unmarried submarine should keep its legacy self invisibility duration");
                return;
            }
        }

        ServerPlayer owner = makeServerBackedPlayer(helper, "SubmarineInvisibilityOwner");
        helper.getLevel().players().add(owner);
        owner.setPos(2.0D, 2.0D, 2.0D);
        LegacyShipEntity marriedU511 = friendlyShips[3];
        marriedU511.setOwner(owner);
        marriedU511.setMarried(true);
        marriedU511.removeEffect(MobEffects.INVISIBILITY);
        owner.removeEffect(MobEffects.INVISIBILITY);
        marriedU511.tickCount = 256;
        LegacyShipBehaviorCatalog.tickLegacyPerShipBehavior(marriedU511);
        MobEffectInstance ownerInvisibility = owner.getEffect(MobEffects.INVISIBILITY);
        if (marriedU511.getEffect(MobEffects.INVISIBILITY) == null
                || ownerInvisibility == null
                || ownerInvisibility.getDuration() != 100) {
            helper.fail("married submarine should keep self invisibility and apply it to a nearby owner");
            return;
        }

        LegacyShipEntity gated = friendlyShips[0];
        gated.removeEffect(MobEffects.INVISIBILITY);
        gated.setRingEffectEnabled(false);
        gated.tickCount = 384;
        LegacyShipBehaviorCatalog.tickLegacyPerShipBehavior(gated);
        if (gated.hasEffect(MobEffects.INVISIBILITY)) {
            helper.fail("disabling the ring effect should suppress friendly submarine invisibility");
            return;
        }

        int[] hostileEggMetas = {2040, 2041};
        for (int eggMeta : hostileEggMetas) {
            LegacyShipEntity hostile = ModEntityTypes.LEGACY_SHIP.get().create(helper.getLevel());
            if (hostile == null) {
                helper.fail("hostile submarine should be creatable for invisibility boundary tests");
                return;
            }

            hostile.setVariantEggMeta(eggMeta);
            hostile.initializeHostileRuntime(false, false, true);
            hostile.setNoAi(true);
            hostile.setPos(10.0D + eggMeta - 2040, 2.0D, 1.5D);
            helper.getLevel().addFreshEntity(hostile);

            boolean applied = false;
            for (long seed = 0L; seed < 1024L; seed++) {
                hostile.removeEffect(MobEffects.INVISIBILITY);
                hostile.getRandom().setSeed(seed << 32);
                hostile.tickCount = 128;
                LegacyShipBehaviorCatalog.tickLegacyPerShipBehavior(hostile);
                MobEffectInstance invisibility = hostile.getEffect(MobEffects.INVISIBILITY);
                if (invisibility != null) {
                    applied = true;
                    if (invisibility.getDuration() != 60) {
                        helper.fail("boss submarine invisibility should use 40 + scaleLevel * 10 ticks");
                        return;
                    }
                    break;
                }
            }

            if (!applied) {
                helper.fail("hostile submarine should retain the legacy 50 percent invisibility roll");
                return;
            }
        }

        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void shipRangedGoalUsesLegacyPreflightRules(GameTestHelper helper) {
        LegacyShipEntity ship = ModEntityTypes.LEGACY_SHIP.get().create(helper.getLevel());
        Zombie target = EntityType.ZOMBIE.create(helper.getLevel());
        if (ship == null || target == null) {
            helper.fail("legacy ship and zombie should be creatable for ranged goal tests");
            return;
        }

        ship.setVariantEggMeta(53);
        ship.setOwner(UUID.randomUUID(), "RangedGoalTester");
        ship.setPos(1.5D, 2.0D, 1.5D);
        target.setPos(3.5D, 2.0D, 1.5D);
        helper.getLevel().addFreshEntity(ship);
        helper.getLevel().addFreshEntity(target);
        ship.setTarget(target);
        ship.setLightAmmo(20);
        ship.setHeavyAmmo(20);
        ship.setGrudge(20);

        LegacyShipRangedAttackGoal goal = new LegacyShipRangedAttackGoal(ship, 1.0D);
        if (!goal.canUse()) {
            helper.fail("ranged goal should activate with four legacy light shots available");
            return;
        }

        ship.setGrudge(0);
        if (goal.canUse()) {
            helper.fail("ranged goal must stop when legacy fuel is empty");
            return;
        }

        ship.setGrudge(20);
        ship.setLightAmmo(3);
        ship.setHeavyAmmo(3);
        if (goal.canUse()) {
            helper.fail("ranged goal must require four times the legacy class ammo cost before starting");
            return;
        }

        ship.setLightAmmo(20);
        ship.setHeavyAmmo(20);
        if (!goal.canUse()) {
            helper.fail("ranged goal should resume when either legacy cannon ammo reserve is restored");
            return;
        }

        ship.setOrderedToSit(true);
        if (goal.canUse()) {
            helper.fail("ranged goal must stop while the ship is ordered to sit");
            return;
        }

        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void shipRangedGoalHonorsLegacyInitialCannonDelays(GameTestHelper helper) {
        LegacyShipEntity ship = ModEntityTypes.LEGACY_SHIP.get().create(helper.getLevel());
        Zombie target = EntityType.ZOMBIE.create(helper.getLevel());
        if (ship == null || target == null) {
            helper.fail("legacy ship and zombie should be creatable for ranged warmup tests");
            return;
        }

        BlockPos origin = helper.absolutePos(new BlockPos(1, 2, 1));
        ship.setVariantEggMeta(53);
        ship.setOwner(UUID.randomUUID(), "RangedWarmupTester");
        ship.setShipLevel(150);
        ship.setNoAi(true);
        ship.setPos(origin.getX() + 0.5D, origin.getY(), origin.getZ() + 0.5D);
        target.setNoAi(true);
        target.setPos(origin.getX() + 2.5D, origin.getY(), origin.getZ() + 0.5D);
        helper.getLevel().addFreshEntity(ship);
        helper.getLevel().addFreshEntity(target);

        ship.setTarget(target);
        ship.setLightAmmo(20);
        ship.setHeavyAmmo(20);
        ship.setGrudge(20);
        ship.setUseAttackMelee(false);
        ship.setUseAttackLight(true);
        ship.setUseAttackHeavy(false);
        ship.setUseAttackAirLight(false);
        ship.setUseAttackAirHeavy(false);

        LegacyShipRangedAttackGoal goal = new LegacyShipRangedAttackGoal(ship, 1.0D);
        if (!goal.canUse()) {
            helper.fail("ranged goal should be available before the legacy cannon warmup");
            return;
        }

        goal.start();
        for (int tick = 0; tick < 19; tick++) {
            goal.tick();
        }
        if (ship.getLightAmmo() != 20 || ship.getCompatAttackCooldown(LegacyShipAttackKind.LIGHT) != 0) {
            helper.fail("legacy light cannon should wait 20 ticks before its first attack");
            return;
        }

        goal.tick();
        if (ship.getLightAmmo() != 19
                || ship.getCompatAttackCooldown(LegacyShipAttackKind.LIGHT) <= 0) {
            helper.fail("legacy light cannon should enter its attack cooldown on the twentieth tick");
            return;
        }

        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void carrierGoalHonorsLegacyInitialAirDelayAndOrder(GameTestHelper helper) {
        LegacyShipEntity carrier = ModEntityTypes.LEGACY_SHIP.get().create(helper.getLevel());
        Zombie target = EntityType.ZOMBIE.create(helper.getLevel());
        if (carrier == null || target == null) {
            helper.fail("carrier and zombie should be creatable for air warmup tests");
            return;
        }

        BlockPos origin = helper.absolutePos(new BlockPos(1, 2, 1));
        carrier.setVariantEggMeta(49);
        carrier.setOwner(UUID.randomUUID(), "CarrierWarmupTester");
        carrier.setNoAi(true);
        carrier.setPos(origin.getX() + 0.5D, origin.getY(), origin.getZ() + 0.5D);
        target.setNoAi(true);
        target.setPos(origin.getX() + 6.5D, origin.getY(), origin.getZ() + 0.5D);
        helper.getLevel().addFreshEntity(carrier);
        helper.getLevel().addFreshEntity(target);

        carrier.setTarget(target);
        carrier.setLightAmmo(18);
        carrier.setHeavyAmmo(6);
        carrier.setGrudge(20);
        carrier.setUseAttackMelee(false);
        carrier.setUseAttackLight(false);
        carrier.setUseAttackHeavy(false);
        carrier.setUseAttackAirLight(false);
        carrier.setUseAttackAirHeavy(true);

        LegacyShipRangedAttackGoal goal = new LegacyShipRangedAttackGoal(carrier, 1.0D);
        if (!goal.canUse()) {
            helper.fail("carrier air goal should be available before the legacy launch warmup");
            return;
        }

        goal.start();
        for (int tick = 0; tick < 19; tick++) {
            goal.tick();
        }
        if (!helper.getLevel().getEntitiesOfClass(LegacyShipAircraftEntity.class,
                carrier.getBoundingBox().inflate(20.0D),
                aircraft -> aircraft.getOwnerId() == carrier.getId()).isEmpty()) {
            helper.fail("carrier aircraft should wait for the legacy 20-tick launch delay");
            return;
        }

        goal.tick();
        List<LegacyShipAircraftEntity> aircraft = helper.getLevel().getEntitiesOfClass(
                LegacyShipAircraftEntity.class,
                carrier.getBoundingBox().inflate(20.0D),
                launched -> launched.getOwnerId() == carrier.getId()
                        && launched.getTargetId() == target.getId());
        if (aircraft.isEmpty() || aircraft.get(0).getAttackKind() != LegacyShipAttackKind.AIR_HEAVY) {
            helper.fail("carrier first air launch should be the legacy heavy-air channel");
            return;
        }

        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void legacyAttackCostsFollowShipClassAndMoraleRules(GameTestHelper helper) {
        int[][] expectedAmmoCosts = {
                {2, 1}, {3, 1}, {4, 1}, {5, 1}, {18, 1}, {19, 1}, {20, 1}, {21, 1},
                {29, 1}, {38, 1}, {40, 1}, {41, 1}, {46, 1}, {53, 1}, {54, 1}, {55, 1}, {56, 1}, {74, 1},
                {2038, 1}, {2040, 1}, {2041, 1}, {2053, 1}, {2054, 1}, {2055, 1}, {2056, 1},
                {11, 2}, {12, 2}, {51, 2}, {58, 2}, {59, 2}, {60, 2}, {61, 2},
                {2058, 2}, {2059, 2}, {2060, 2}, {2061, 2},
                {14, 3}, {22, 3}, {35, 3}, {49, 3}, {50, 3}, {2049, 3}, {2050, 3},
                {15, 4}, {16, 4}, {17, 4}, {23, 4}, {28, 4}, {30, 4}, {31, 4}, {32, 4}, {33, 4},
                {39, 4}, {48, 4}, {62, 4}, {63, 4}, {64, 4}, {65, 4}, {2039, 4}, {2048, 4},
                {2062, 4}, {2063, 4}, {2064, 4}, {2065, 4}
        };
        for (int[] expected : expectedAmmoCosts) {
            ShipEntitySpec spec = ShipEntitySpecs.getByEggMeta(expected[0]);
            int actual = LegacyShipBehaviorCatalog.ammoConsumptionFor(spec);
            if (actual != expected[1]) {
                helper.fail("legacy ammo consumption mismatch: egg=" + expected[0]
                        + ", expected=" + expected[1] + ", actual=" + actual);
                return;
            }
        }

        if (LegacyShipBehaviorCatalog.moraleCostFor(LegacyShipAttackKind.MELEE) != 2
                || LegacyShipBehaviorCatalog.moraleCostFor(LegacyShipAttackKind.LIGHT) != 4
                || LegacyShipBehaviorCatalog.moraleCostFor(LegacyShipAttackKind.HEAVY) != 6
                || LegacyShipBehaviorCatalog.moraleCostFor(LegacyShipAttackKind.AIR_LIGHT) != 6
                || LegacyShipBehaviorCatalog.moraleCostFor(LegacyShipAttackKind.AIR_HEAVY) != 8) {
            helper.fail("legacy attack morale costs should match the direct AttackHandler paths");
            return;
        }

        Player player = helper.makeMockPlayer();
        LegacyShipEntity ship = ModEntityTypes.LEGACY_SHIP.get().create(helper.getLevel());
        Zombie target = EntityType.ZOMBIE.create(helper.getLevel());
        if (ship == null || target == null) {
            helper.fail("legacy ship and zombie should be creatable for attack cost tests");
            return;
        }

        ship.setVariantEggMeta(53);
        ship.setOwner(player);
        ship.setPos(1.5D, 2.0D, 1.5D);
        target.setPos(3.5D, 2.0D, 1.5D);
        helper.getLevel().addFreshEntity(ship);
        helper.getLevel().addFreshEntity(target);
        ship.setLightAmmo(20);
        ship.setGrudge(20);
        ship.setMorale(5000);

        int ammoBefore = ship.getLightAmmo();
        int grudgeBefore = ship.getGrudge();
        int moraleBefore = ship.getMorale();
        if (!ship.performPlayerCompatAttack(target, LegacyShipAttackKind.LIGHT)) {
            helper.fail("friendly light attack should execute for attack cost regression");
            return;
        }
        if (ship.getLightAmmo() != ammoBefore - 1
                || ship.getGrudge() != grudgeBefore - 4
                || ship.getMorale() != moraleBefore - 4) {
            helper.fail("friendly light attack should consume legacy class ammo, grudge and morale exactly once");
            return;
        }

        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void carrierAirAmmoThresholdAndConsumptionFollowLegacyCvRules(GameTestHelper helper) {
        ShipEntitySpec kagaSpec = ShipEntitySpecs.getByEggMeta(49);
        if (LegacyShipBehaviorCatalog.attackAmmoCostFor(kagaSpec, LegacyShipAttackKind.AIR_LIGHT) != 18
                || LegacyShipBehaviorCatalog.attackAmmoCostFor(kagaSpec, LegacyShipAttackKind.AIR_HEAVY) != 6
                || LegacyShipBehaviorCatalog.minimumAmmoForGoal(kagaSpec, LegacyShipAttackKind.AIR_LIGHT) != 18
                || LegacyShipBehaviorCatalog.minimumAmmoForGoal(kagaSpec, LegacyShipAttackKind.AIR_HEAVY) != 6) {
            helper.fail("carrier air ammo should follow legacy 6x light and 2x heavy CV costs");
            return;
        }

        Player player = helper.makeMockPlayer();
        LegacyShipEntity carrier = ModEntityTypes.LEGACY_SHIP.get().create(helper.getLevel());
        Zombie target = EntityType.ZOMBIE.create(helper.getLevel());
        if (carrier == null || target == null) {
            helper.fail("carrier and zombie should be creatable for CV ammo tests");
            return;
        }

        BlockPos origin = helper.absolutePos(new BlockPos(1, 2, 1));
        carrier.setVariantEggMeta(49);
        carrier.setOwner(player);
        carrier.setNoAi(true);
        carrier.setPos(origin.getX() + 0.5D, origin.getY(), origin.getZ() + 0.5D);
        target.setNoAi(true);
        target.setPos(origin.getX() + 6.5D, origin.getY(), origin.getZ() + 0.5D);
        helper.getLevel().addFreshEntity(carrier);
        helper.getLevel().addFreshEntity(target);

        carrier.setLightAmmo(18);
        carrier.setHeavyAmmo(6);
        carrier.setGrudge(20);
        carrier.setMorale(5000);
        carrier.setUseAttackMelee(false);
        carrier.setUseAttackLight(false);
        carrier.setUseAttackHeavy(false);
        carrier.setUseAttackAirLight(false);
        carrier.setUseAttackAirHeavy(true);

        if (!carrier.canUseCompatRangedCombat()) {
            helper.fail("carrier heavy-air combat should be available at the legacy 2x ammo threshold");
            return;
        }
        carrier.setHeavyAmmo(5);
        if (carrier.canUseCompatRangedCombat()) {
            helper.fail("carrier heavy-air combat should stop below the legacy 2x ammo threshold");
            return;
        }
        carrier.setHeavyAmmo(6);

        int heavyAmmoBefore = carrier.getHeavyAmmo();
        int grudgeBefore = carrier.getGrudge();
        int moraleBefore = carrier.getMorale();
        if (!carrier.performPlayerCompatAttack(target, LegacyShipAttackKind.AIR_HEAVY)) {
            helper.fail("carrier heavy-air attack should launch with six heavy ammo");
            return;
        }
        if (carrier.getHeavyAmmo() != heavyAmmoBefore - 6
                || carrier.getGrudge() != grudgeBefore - 12
                || carrier.getMorale() != moraleBefore - 8) {
            helper.fail("carrier heavy-air launch should consume legacy ammo, grudge and morale exactly once");
            return;
        }

        if (helper.getLevel().getEntitiesOfClass(LegacyShipAircraftEntity.class,
                carrier.getBoundingBox().inflate(20.0D),
                aircraft -> aircraft.getOwnerId() == carrier.getId()
                        && aircraft.getTargetId() == target.getId()).isEmpty()) {
            helper.fail("carrier heavy-air launch should create an aircraft for the requested target");
            return;
        }

        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void carrierAircraftCapacityAndRuntimeFollowLegacyCvRules(GameTestHelper helper) {
        ShipEntitySpec kaga = ShipEntitySpecs.getByEggMeta(49);
        ShipEntitySpec akagi = ShipEntitySpecs.getByEggMeta(50);
        ShipEntitySpec hostileKaga = ShipEntitySpecs.getByEggMeta(2049);
        ShipEntitySpec carrierWo = ShipEntitySpecs.getByEggMeta(14);
        if (LegacyShipBehaviorCatalog.aircraftLightCapacityFor(kaga, 50, 0) != 38
                || LegacyShipBehaviorCatalog.aircraftHeavyCapacityFor(kaga, 50, 0) != 19
                || LegacyShipBehaviorCatalog.aircraftLightCapacityFor(akagi, 50, 0) != 32
                || LegacyShipBehaviorCatalog.aircraftHeavyCapacityFor(akagi, 50, 0) != 18
                || LegacyShipBehaviorCatalog.aircraftLightCapacityFor(kaga, 50, 2) != 46
                || LegacyShipBehaviorCatalog.aircraftHeavyCapacityFor(kaga, 50, 2) != 23
                || LegacyShipBehaviorCatalog.aircraftLightCapacityFor(hostileKaga, 50, 0) != 10
                || LegacyShipBehaviorCatalog.aircraftHeavyCapacityFor(hostileKaga, 50, 0) != 10
                || LegacyShipBehaviorCatalog.aircraftLightCapacityFor(carrierWo, 50, 0) != 30
                || LegacyShipBehaviorCatalog.aircraftHeavyCapacityFor(carrierWo, 50, 0) != 16
                || LegacyShipBehaviorCatalog.aircraftRecoveryDelayFor(1.0F) != 3620) {
            helper.fail("carrier aircraft capacity and recovery delay should match legacy CV formulas");
            return;
        }

        LegacyShipEntity carrier = ModEntityTypes.LEGACY_SHIP.get().create(helper.getLevel());
        Zombie target = EntityType.ZOMBIE.create(helper.getLevel());
        if (carrier == null || target == null) {
            helper.fail("carrier and zombie should be creatable for aircraft runtime tests");
            return;
        }

        carrier.setShipLevel(50);
        carrier.setVariantEggMeta(49);
        carrier.setNoAi(true);
        carrier.setPos(1.0D, 2.0D, 1.0D);
        target.setPos(7.0D, 2.0D, 1.0D);
        helper.getLevel().addFreshEntity(carrier);
        helper.getLevel().addFreshEntity(target);
        if (carrier.getMaxAircraftLight() != 38
                || carrier.getMaxAircraftHeavy() != 19
                || carrier.getAircraftLight() != 38
                || carrier.getAircraftHeavy() != 19) {
            helper.fail("new Kaga entities should initialize legacy aircraft reserves at their calculated caps");
            return;
        }

        carrier.setHeavyAmmo(6);
        carrier.setGrudge(20);
        carrier.setMorale(5000);
        carrier.setUseAttackMelee(false);
        carrier.setUseAttackLight(false);
        carrier.setUseAttackHeavy(false);
        carrier.setUseAttackAirLight(false);
        carrier.setUseAttackAirHeavy(true);
        carrier.setAircraftHeavy(1);
        int aircraftBefore = carrier.getAircraftHeavy();
        if (!carrier.performPlayerCompatAttack(target, LegacyShipAttackKind.AIR_HEAVY)
                || carrier.getAircraftHeavy() != aircraftBefore - 1) {
            helper.fail("heavy aircraft launch should consume exactly one legacy heavy aircraft");
            return;
        }

        CompoundTag saved = new CompoundTag();
        carrier.setAircraftLight(7);
        carrier.setAircraftHeavy(5);
        carrier.addAdditionalSaveData(saved);
        LegacyShipEntity loaded = ModEntityTypes.LEGACY_SHIP.get().create(helper.getLevel());
        if (loaded == null) {
            helper.fail("carrier should be creatable for aircraft save/load tests");
            return;
        }
        loaded.readAdditionalSaveData(saved);
        if (loaded.getAircraftLight() != 7 || loaded.getAircraftHeavy() != 5) {
            helper.fail("modern aircraft reserves should persist through entity NBT");
            return;
        }

        CompoundTag legacyTag = new CompoundTag();
        legacyTag.putInt("VariantEggMeta", 49);
        CompoundTag legacyExtended = new CompoundTag();
        CompoundTag legacyMinor = new CompoundTag();
        legacyMinor.putInt("NumAirL", 3);
        legacyMinor.putInt("NumAirH", 2);
        legacyExtended.put("Minor", legacyMinor);
        legacyTag.put("ShipExtProps", legacyExtended);
        LegacyShipEntity migrated = ModEntityTypes.LEGACY_SHIP.get().create(helper.getLevel());
        if (migrated == null) {
            helper.fail("carrier should be creatable for legacy aircraft migration tests");
            return;
        }
        migrated.readAdditionalSaveData(legacyTag);
        if (migrated.getAircraftLight() != 3 || migrated.getAircraftHeavy() != 2) {
            helper.fail("legacy NumAirL and NumAirH should migrate into modern aircraft reserves");
            return;
        }

        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void carrierLaunchAndPassengerOffsetsFollowLegacyConstructors(GameTestHelper helper) {
        LegacyShipEntity kaga = ModEntityTypes.LEGACY_SHIP.get().create(helper.getLevel());
        LegacyShipEntity akagi = ModEntityTypes.LEGACY_SHIP.get().create(helper.getLevel());
        LegacyShipEntity wo = ModEntityTypes.LEGACY_SHIP.get().create(helper.getLevel());
        LegacyShipEntity hostileKaga = ModEntityTypes.LEGACY_SHIP.get().create(helper.getLevel());
        LegacyShipEntity destroyer = ModEntityTypes.LEGACY_SHIP.get().create(helper.getLevel());
        Zombie target = EntityType.ZOMBIE.create(helper.getLevel());
        if (kaga == null || akagi == null || wo == null || hostileKaga == null || destroyer == null || target == null) {
            helper.fail("carrier and target entities should be creatable for constructor parity tests");
            return;
        }

        kaga.setVariantEggMeta(49);
        kaga.setPos(2.0D, 2.0D, 2.0D);
        akagi.setVariantEggMeta(50);
        akagi.setPos(4.0D, 2.0D, 2.0D);
        wo.setVariantEggMeta(14);
        wo.setPos(6.0D, 2.0D, 2.0D);
        hostileKaga.setVariantEggMeta(2049);
        hostileKaga.setPos(8.0D, 2.0D, 2.0D);
        hostileKaga.initializeHostileRuntime(false, true, true);
        destroyer.setVariantEggMeta(38);
        destroyer.setPos(10.0D, 2.0D, 2.0D);
        target.setNoAi(true);
        target.setPos(14.0D, 2.0D, 2.0D);
        helper.getLevel().addFreshEntity(kaga);
        helper.getLevel().addFreshEntity(akagi);
        helper.getLevel().addFreshEntity(wo);
        helper.getLevel().addFreshEntity(hostileKaga);
        helper.getLevel().addFreshEntity(destroyer);
        helper.getLevel().addFreshEntity(target);

        if (LegacyShipBehaviorCatalog.aircraftLaunchHeightFactorFor(kaga.getSpec(), LegacyShipAttackKind.AIR_LIGHT) != 0.65F
                || LegacyShipBehaviorCatalog.aircraftLaunchHeightFactorFor(kaga.getSpec(), LegacyShipAttackKind.AIR_HEAVY) != 0.65F
                || LegacyShipBehaviorCatalog.aircraftLaunchHeightFactorFor(akagi.getSpec(), LegacyShipAttackKind.AIR_LIGHT) != 0.65F
                || LegacyShipBehaviorCatalog.aircraftLaunchHeightFactorFor(akagi.getSpec(), LegacyShipAttackKind.AIR_HEAVY) != 0.65F) {
            helper.fail("Kaga and Akagi aircraft should retain the legacy 0.65 launch-height factor");
            return;
        }

        LegacyShipAircraftEntity kagaLight = LegacyShipAircraftEntity.create(
                helper.getLevel(), kaga, target, LegacyShipAttackKind.AIR_LIGHT);
        LegacyShipAircraftEntity akagiHeavy = LegacyShipAircraftEntity.create(
                helper.getLevel(), akagi, target, LegacyShipAttackKind.AIR_HEAVY);
        LegacyShipAircraftEntity woLight = LegacyShipAircraftEntity.create(
                helper.getLevel(), wo, target, LegacyShipAttackKind.AIR_LIGHT);
        LegacyShipAircraftEntity woHeavy = LegacyShipAircraftEntity.create(
                helper.getLevel(), wo, target, LegacyShipAttackKind.AIR_HEAVY);
        LegacyShipAircraftEntity hostileKagaLight = LegacyShipAircraftEntity.create(
                helper.getLevel(), hostileKaga, target, LegacyShipAttackKind.AIR_LIGHT);
        if (kagaLight == null || akagiHeavy == null || woLight == null || woHeavy == null || hostileKagaLight == null
                || Math.abs(kagaLight.getY() - (kaga.getY() + kaga.getBbHeight() * 0.65D)) > 1.0E-6D
                || Math.abs(akagiHeavy.getY() - (akagi.getY() + akagi.getBbHeight() * 0.65D)) > 1.0E-6D
                || Math.abs(woLight.getBbWidth() - 0.5F) > 1.0E-6F
                || Math.abs(woHeavy.getBbWidth() - 0.6F) > 1.0E-6F
                || Math.abs(kagaLight.getBbWidth() - 0.5F) > 1.0E-6F
                || Math.abs(akagiHeavy.getBbWidth() - 0.5F) > 1.0E-6F
                || Math.abs(hostileKagaLight.getBbWidth() - 2.0F) > 1.0E-6F) {
            helper.fail("carrier aircraft should launch from the legacy height for both air channels");
            return;
        }

        if (Math.abs(kaga.getPassengersRidingOffset() - kaga.getBbHeight() * 0.72D) > 1.0E-6D
                || Math.abs(akagi.getPassengersRidingOffset() - akagi.getBbHeight() * 0.72D) > 1.0E-6D
                || Math.abs(destroyer.getPassengersRidingOffset() - destroyer.getBbHeight() * 0.67D) > 1.0E-6D) {
            helper.fail("standing carrier and ordinary ship riding offsets should match the legacy constructors");
            return;
        }

        kaga.setOrderedToSit(true);
        akagi.setOrderedToSit(true);
        if (Math.abs(kaga.getPassengersRidingOffset() - kaga.getBbHeight() * 0.45D) > 1.0E-6D
                || Math.abs(akagi.getPassengersRidingOffset() - akagi.getBbHeight() * 0.45D) > 1.0E-6D) {
            helper.fail("sitting carrier riding offsets should retain the legacy 0.45 factor");
            return;
        }

        kaga.setEmotion(4);
        akagi.setEmotion(4);
        if (Math.abs(kaga.getPassengersRidingOffset() - kaga.getBbHeight() * 0.35D) > 1.0E-6D
                || Math.abs(akagi.getPassengersRidingOffset() - akagi.getBbHeight() * 0.35D) > 1.0E-6D) {
            helper.fail("bored sitting carriers should retain the legacy 0.35 riding factor");
            return;
        }

        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void carrierAttackEffectsFollowLegacyCvRules(GameTestHelper helper) {
        LegacyShipBehaviorCatalog.AttackEffectProfile kaga = LegacyShipBehaviorCatalog.attackEffectFor(
                ShipEntitySpecs.getByEggMeta(49), 120, 0);
        LegacyShipBehaviorCatalog.AttackEffectProfile akagi = LegacyShipBehaviorCatalog.attackEffectFor(
                ShipEntitySpecs.getByEggMeta(50), 120, 0);
        LegacyShipBehaviorCatalog.AttackEffectProfile hostileKaga = LegacyShipBehaviorCatalog.attackEffectFor(
                ShipEntitySpecs.getByEggMeta(2049), 1, 2);

        if (kaga.effect() != MobEffects.HUNGER
                || kaga.amplifier() != 1
                || kaga.durationTicks() != 220
                || kaga.chancePercent() != 120
                || akagi.effect() != MobEffects.HUNGER
                || akagi.amplifier() != 1
                || akagi.durationTicks() != 220
                || hostileKaga.effect() != MobEffects.HUNGER
                || hostileKaga.amplifier() != 0
                || hostileKaga.durationTicks() != 200
                || hostileKaga.chancePercent() != 75) {
            helper.fail("Kaga and Akagi attack effects should preserve the legacy friendly and hostile formulas");
            return;
        }

        LegacyShipEntity carrier = ModEntityTypes.LEGACY_SHIP.get().create(helper.getLevel());
        if (carrier == null) {
            helper.fail("carrier should be creatable for attack effect tests");
            return;
        }

        carrier.setVariantEggMeta(49);
        carrier.setShipLevel(120);
        carrier.setPos(1.0D, 2.0D, 1.0D);
        helper.getLevel().addFreshEntity(carrier);

        boolean effectApplied = false;
        for (int attempt = 0; attempt < 32 && !effectApplied; attempt++) {
            Zombie target = EntityType.ZOMBIE.create(helper.getLevel());
            if (target == null) {
                helper.fail("zombie should be creatable for attack effect tests");
                return;
            }
            target.getAttribute(Attributes.MAX_HEALTH).setBaseValue(10000.0D);
            target.setHealth(target.getMaxHealth());
            target.setPos(2.0D, 2.0D, 1.0D);
            helper.getLevel().addFreshEntity(target);
            carrier.performCompatAircraftStrike(target, LegacyShipAttackKind.AIR_LIGHT,
                    LegacyShipProjectileVisual.AIRPLANE,
                    com.lulan.shincolle.network.GameplayParticleType.AIRCRAFT_IMPACT,
                    false, false);
            effectApplied = target.hasEffect(MobEffects.HUNGER);
            if (!effectApplied) {
                target.discard();
            }
        }

        if (!effectApplied) {
            helper.fail("carrier aircraft hits should apply the legacy hunger attack effect");
            return;
        }

        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void remainingShipAttackEffectsFollowLegacyClassMaps(GameTestHelper helper) {
        if (!attackEffectMatches(39, 75, 0, MobEffects.POISON, 1, 135, 75)
                || !attackEffectMatches(48, 70, 0, MobEffects.DIG_SLOWDOWN, 1, 170, 70)
                || !attackEffectMatches(60, 100, 0, MobEffects.MOVEMENT_SLOWDOWN, 1, 200, 100)
                || !attackEffectMatches(61, 100, 0, MobEffects.MOVEMENT_SLOWDOWN, 1, 200, 100)
                || !attackEffectMatches(62, 60, 0, MobEffects.WEAKNESS, 1, 160, 60)
                || !attackEffectMatches(63, 85, 0, MobEffects.POISON, 1, 165, 85)
                || !attackEffectMatches(64, 95, 0, MobEffects.UNLUCK, 1, 195, 95)
                || !attackEffectMatches(65, 50, 0, MobEffects.BLINDNESS, 0, 100, 50)
                || !attackEffectMissing(38, 120, 0)) {
            helper.fail("friendly ship attack effects should preserve the legacy class formulas");
            return;
        }

        if (!attackEffectMatches(2039, 1, 3, MobEffects.POISON, 1, 180, 100)
                || !attackEffectMatches(2048, 1, 3, MobEffects.DIG_SLOWDOWN, 2, 250, 100)
                || !attackEffectMissing(2059, 1, 3)
                || !attackEffectMatches(2060, 1, 3, MobEffects.MOVEMENT_SLOWDOWN, 1, 250, 100)
                || !attackEffectMatches(2062, 1, 3, MobEffects.WEAKNESS, 2, 230, 100)
                || !attackEffectMatches(2063, 1, 3, MobEffects.POISON, 2, 230, 100)
                || !attackEffectMatches(2064, 1, 3, MobEffects.UNLUCK, 2, 250, 100)
                || !attackEffectMatches(2065, 1, 3, MobEffects.BLINDNESS, 2, 130, 100)) {
            helper.fail("hostile ship attack effects should preserve the legacy mob formulas");
            return;
        }

        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void lightCruiserBossBarsAndEffectsFollowLegacyClasses(GameTestHelper helper) {
        if (LegacyShipBehaviorCatalog.hostileBossBarColorFor(ShipEntitySpecs.getByEggMeta(2058))
                        != BossEvent.BossBarColor.PURPLE
                || LegacyShipBehaviorCatalog.hostileBossBarColorFor(ShipEntitySpecs.getByEggMeta(2059))
                        != BossEvent.BossBarColor.PURPLE) {
            helper.fail("hostile Tenryuu/Tatsuta should keep the legacy purple BossBar color");
            return;
        }

        if (!attackEffectMissing(58, 100, 0)
                || !attackEffectMissing(59, 100, 0)
                || !attackEffectMissing(2058, 1, 3)
                || !attackEffectMissing(2059, 1, 3)) {
            helper.fail("Tenryuu and Tatsuta should not gain an attack effect absent from their legacy classes");
            return;
        }

        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void registeredHostileBossBarsFollowLegacyMobClasses(GameTestHelper helper) {
        int[] hostileEggMetas = {
                2038, 2039, 2040, 2041, 2048, 2049, 2050, 2053, 2054,
                2055, 2056, 2058, 2059, 2060, 2061, 2062, 2063, 2064, 2065
        };
        BossEvent.BossBarColor[] expectedColors = {
                BossEvent.BossBarColor.YELLOW, BossEvent.BossBarColor.WHITE,
                BossEvent.BossBarColor.BLUE, BossEvent.BossBarColor.YELLOW,
                BossEvent.BossBarColor.PINK, BossEvent.BossBarColor.BLUE,
                BossEvent.BossBarColor.RED, BossEvent.BossBarColor.PURPLE,
                BossEvent.BossBarColor.WHITE, BossEvent.BossBarColor.PINK,
                BossEvent.BossBarColor.PINK, BossEvent.BossBarColor.PURPLE,
                BossEvent.BossBarColor.PURPLE, BossEvent.BossBarColor.YELLOW,
                BossEvent.BossBarColor.YELLOW, BossEvent.BossBarColor.WHITE,
                BossEvent.BossBarColor.WHITE, BossEvent.BossBarColor.WHITE,
                BossEvent.BossBarColor.WHITE
        };

        for (int index = 0; index < hostileEggMetas.length; index++) {
            if (LegacyShipBehaviorCatalog.hostileBossBarColorFor(
                    ShipEntitySpecs.getByEggMeta(hostileEggMetas[index])) != expectedColors[index]) {
                helper.fail("hostile egg " + hostileEggMetas[index]
                        + " should preserve its legacy BossInfo color");
                return;
            }
        }

        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void legacyArcProjectileUsesLegacyMotionPhases(GameTestHelper helper) {
        LegacyShipEntity ship = ModEntityTypes.LEGACY_SHIP.get().create(helper.getLevel());
        Zombie farTarget = EntityType.ZOMBIE.create(helper.getLevel());
        Zombie nearTarget = EntityType.ZOMBIE.create(helper.getLevel());
        if (ship == null || farTarget == null || nearTarget == null) {
            helper.fail("submarine and targets should be creatable for legacy arc motion tests");
            return;
        }

        ship.setVariantEggMeta(40);
        ship.setNoAi(true);
        ship.setPos(1.5D, 2.0D, 1.5D);
        farTarget.setNoAi(true);
        farTarget.setPos(26.5D, 2.0D, 1.5D);
        nearTarget.setNoAi(true);
        nearTarget.setPos(4.0D, 2.0D, 1.5D);
        helper.getLevel().addFreshEntity(ship);
        helper.getLevel().addFreshEntity(farTarget);
        helper.getLevel().addFreshEntity(nearTarget);

        LegacyShipProjectileProfile profile = ship.getAttackProfile()
                .projectileProfile(LegacyShipAttackKind.LIGHT);
        LegacyShipProjectileEntity farProjectile = LegacyShipProjectileEntity.create(
                helper.getLevel(), ship, farTarget, LegacyShipAttackKind.LIGHT,
                4.0F, false, profile, null, 0.4D, false);
        Vec3 farMotion = farProjectile.getDeltaMovement();
        double launchY = ship.getY() + ship.getBbHeight() * profile.launchHeightFactor();
        double targetY = farTarget.getY() + farTarget.getBbHeight() * profile.targetHeightFactor();
        double dx = farTarget.getX() - ship.getX();
        double dz = farTarget.getZ() - ship.getZ();
        double horizontalDistance = Math.sqrt(dx * dx + dz * dz);
        double distance = Math.sqrt(horizontalDistance * horizontalDistance
                + (targetY - launchY) * (targetY - launchY));
        double speed = profile.baseSpeed()
                * ship.getEquipmentBehaviorState().projectileSpeedMultiplier(LegacyShipAttackKind.LIGHT);
        double additionalHeight = distance * profile.launchArcLift();
        double verticalDistance = Math.abs(launchY - targetY);
        double heightRatio = Math.sqrt(additionalHeight / (additionalHeight + verticalDistance));
        int riseTicks = (int) Math.floor((horizontalDistance / speed) * heightRatio / (1.0D + heightRatio));
        double expectedAcceleration = -2.0D * additionalHeight / (riseTicks * riseTicks);
        double expectedVerticalSpeed = -expectedAcceleration * riseTicks;

        if (farProjectile.getMoveType() != LegacyShipProjectileMoveType.ARC
                || Math.abs(farMotion.x - speed) > 1.0E-6D
                || Math.abs(farMotion.z) > 1.0E-6D
                || Math.abs(farMotion.y - expectedVerticalSpeed) > 1.0E-6D) {
            helper.fail("far submarine missile should use the legacy two-phase arc initial velocity");
            return;
        }

        LegacyShipProjectileEntity nearProjectile = LegacyShipProjectileEntity.create(
                helper.getLevel(), ship, nearTarget, LegacyShipAttackKind.LIGHT,
                4.0F, false, profile, null, 0.4D, false);
        if (nearProjectile.getMoveType() != LegacyShipProjectileMoveType.DIRECT
                || Math.abs(nearProjectile.getDeltaMovement().length() - speed) > 1.0E-6D) {
            helper.fail("submarine missile within four blocks should switch to legacy direct motion");
            return;
        }

        farProjectile.discard();
        nearProjectile.discard();
        ship.discard();
        farTarget.discard();
        nearTarget.discard();
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void ro500LightAttackLaunchesLegacyMissile(GameTestHelper helper) {
        LegacyShipEntity ship = ModEntityTypes.LEGACY_SHIP.get().create(helper.getLevel());
        Zombie target = EntityType.ZOMBIE.create(helper.getLevel());
        if (ship == null || target == null) {
            helper.fail("Ro500 and zombie should be creatable for submarine light attack tests");
            return;
        }

        ship.setVariantEggMeta(41);
        ship.setNoAi(true);
        ship.setPos(1.5D, 2.0D, 1.5D);
        target.setNoAi(true);
        target.setPos(7.5D, 2.0D, 1.5D);
        helper.getLevel().addFreshEntity(ship);
        helper.getLevel().addFreshEntity(target);
        ship.setLightAmmo(20);
        ship.setGrudge(20);
        ship.setMorale(5000);
        ship.setUseAttackMelee(false);
        ship.setUseAttackLight(true);
        ship.setUseAttackHeavy(false);
        ship.setUseAttackAirLight(false);
        ship.setUseAttackAirHeavy(false);

        float healthBefore = target.getHealth();
        if (!ship.performPlayerCompatAttack(target, LegacyShipAttackKind.LIGHT)) {
            helper.fail("Ro500 light attack should launch with the legacy light ammo path");
            return;
        }

        List<LegacyShipProjectileEntity> projectiles = helper.getLevel().getEntitiesOfClass(
                LegacyShipProjectileEntity.class,
                ship.getBoundingBox().inflate(20.0D),
                projectile -> projectile.getOwner() == ship
                        && projectile.getAttackKind() == LegacyShipAttackKind.LIGHT);
        if (projectiles.size() != 1) {
            helper.fail("Ro500 light attack should create exactly one missile projectile");
            return;
        }
        LegacyShipProjectileEntity projectile = projectiles.get(0);
        // The legacy depth selector starts at ARC, then the constructor falls
        // back to DIRECT when this launch's computed acceleration exceeds 0.15.
        LegacyShipProjectileMoveType expectedMoveType = LegacyShipProjectileMoveType.DIRECT;
        if (projectile.getProjectileVisual() != LegacyShipProjectileVisual.MISSILE
                || projectile.getMoveType() != expectedMoveType
                || projectile.getIntendedTargetId() != target.getId()
                || projectile.getImpactRadius() != 3.5D
                || projectile.getKnockbackStrength() != 0.15D) {
            helper.fail("Ro500 missile mismatch: expectedMove=" + expectedMoveType
                    + ", actualMove=" + projectile.getMoveType()
                    + ", depth=" + ship.getLegacyLiquidDepth()
                    + ", radius=" + projectile.getImpactRadius()
                    + ", knockback=" + projectile.getKnockbackStrength());
            return;
        }
        if (target.getHealth() != healthBefore) {
            helper.fail("Ro500 light attack should not deal direct damage before its missile impacts");
            return;
        }
        if (ship.getLightAmmo() != 19 || ship.getGrudge() != 16
                || ship.getMorale() != 4996 || ship.getCompatAttackCooldown(LegacyShipAttackKind.LIGHT) <= 0) {
            helper.fail("Ro500 missile launch should consume the legacy light attack resources exactly once");
            return;
        }

        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void submarineHimeKeepsLegacyGeometryAndRingBehavior(GameTestHelper helper) {
        LegacyShipEntity submarineHime = ModEntityTypes.LEGACY_SHIP.get().create(helper.getLevel());
        LegacyShipEntity ssnh = ModEntityTypes.LEGACY_SHIP.get().create(helper.getLevel());
        if (submarineHime == null || ssnh == null) {
            helper.fail("hime submarines should be creatable for geometry tests");
            return;
        }

        submarineHime.setVariantEggMeta(46);
        submarineHime.setPos(2.5D, 2.0D, 2.5D);
        submarineHime.setNoAi(true);
        submarineHime.setRingEffectEnabled(true);
        submarineHime.setGrudge(100);
        helper.getLevel().addFreshEntity(submarineHime);

        ssnh.setVariantEggMeta(74);
        ssnh.setPos(6.5D, 2.0D, 2.5D);
        ssnh.setNoAi(true);
        ssnh.setRingEffectEnabled(true);
        ssnh.setGrudge(100);
        helper.getLevel().addFreshEntity(ssnh);

        EntityDimensions submarineDimensions = LegacyShipBehaviorCatalog.dimensionsFor(
                submarineHime.getSpec(), submarineHime.getLegacyHostileScaleLevel());
        EntityDimensions ssnhDimensions = LegacyShipBehaviorCatalog.dimensionsFor(
                ssnh.getSpec(), ssnh.getLegacyHostileScaleLevel());
        if (Math.abs(submarineDimensions.width - 0.7F) > 1.0E-6F
                || Math.abs(submarineDimensions.height - 1.85F) > 1.0E-6F
                || Math.abs(ssnhDimensions.width - 0.5F) > 1.0E-6F
                || Math.abs(ssnhDimensions.height - 0.9F) > 1.0E-6F) {
            helper.fail("hime submarine dimensions should mirror EntitySubmHime and EntitySSNH");
            return;
        }

        submarineHime.setOrderedToSit(true);
        submarineHime.setEmotion(4);
        if (Math.abs(submarineHime.getPassengersRidingOffset() - submarineHime.getBbHeight() * 0.25D) > 1.0E-6D) {
            helper.fail("EntitySubmHime bored sitting offset should remain 25 percent of height");
            return;
        }
        submarineHime.setEmotion(0);
        if (submarineHime.getPassengersRidingOffset() != 0.0D) {
            helper.fail("EntitySubmHime non-bored sitting offset should remain zero");
            return;
        }
        submarineHime.setOrderedToSit(false);
        if (Math.abs(submarineHime.getPassengersRidingOffset() - submarineHime.getBbHeight() * 0.69D) > 1.0E-6D) {
            helper.fail("EntitySubmHime standing offset should remain 69 percent of height");
            return;
        }

        ssnh.setOrderedToSit(true);
        ssnh.setEmotion(4);
        if (ssnh.getPassengersRidingOffset() != 0.0D) {
            helper.fail("EntitySSNH bored sitting offset should remain zero");
            return;
        }
        ssnh.setEmotion(0);
        if (Math.abs(ssnh.getPassengersRidingOffset() - ssnh.getBbHeight() * 0.08D) > 1.0E-6D) {
            helper.fail("EntitySSNH non-bored sitting offset should remain 8 percent of height");
            return;
        }
        ssnh.setOrderedToSit(false);
        if (Math.abs(ssnh.getPassengersRidingOffset() - ssnh.getBbHeight() * 0.48D) > 1.0E-6D) {
            helper.fail("EntitySSNH standing offset should remain 48 percent of height");
            return;
        }

        submarineHime.tickCount = 128;
        ssnh.tickCount = 128;
        LegacyShipBehaviorCatalog.tickLegacyPerShipBehavior(submarineHime);
        LegacyShipBehaviorCatalog.tickLegacyPerShipBehavior(ssnh);
        if (!submarineHime.hasEffect(MobEffects.INVISIBILITY)
                || !ssnh.hasEffect(MobEffects.INVISIBILITY)) {
            helper.fail("hime submarines should apply the legacy ring invisibility tick");
            return;
        }

        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void himeRosterKeepsLegacyGeometryEffectsAndSupportBehavior(GameTestHelper helper) {
        int[] eggMetas = {22, 23, 28, 29, 30, 31, 32, 33, 35, 51};
        float[][] dimensions = {
                {0.7F, 1.9F}, {0.7F, 1.9F}, {0.7F, 2.05F}, {0.6F, 1.55F},
                {0.7F, 2.2F}, {0.6F, 1.6F}, {0.7F, 2.0F}, {0.5F, 0.9F},
                {0.7F, 1.9F}, {0.7F, 1.2F}
        };
        double[] standingFactors = {0.75D, 0.75D, 0.76D, 0.76D, 0.76D,
                0.76D, 0.76D, 0.48D, 0.75D, 0.76D};
        double[] sittingFactors = {0.33D, 0.56D, 0.62D, 0.62D, 0.52D,
                0.52D, 0.52D, 0.08D, 0.33D, 0.62D};

        for (int index = 0; index < eggMetas.length; index++) {
            ShipEntitySpec spec = ShipEntitySpecs.getByEggMeta(eggMetas[index]);
            EntityDimensions actual = LegacyShipBehaviorCatalog.dimensionsFor(spec, 3);
            if (Math.abs(actual.width - dimensions[index][0]) > 1.0E-6F
                    || Math.abs(actual.height - dimensions[index][1]) > 1.0E-6F) {
                helper.fail("hime egg " + eggMetas[index] + " should keep its fixed legacy dimensions");
                return;
            }

            LegacyShipEntity ship = ModEntityTypes.LEGACY_SHIP.get().create(helper.getLevel());
            if (ship == null) {
                helper.fail("hime ship should be creatable for geometry coverage");
                return;
            }
            ship.setVariantEggMeta(eggMetas[index]);
            ship.setOrderedToSit(false);
            if (Math.abs(ship.getPassengersRidingOffset() - ship.getBbHeight() * standingFactors[index]) > 1.0E-6D) {
                helper.fail("hime egg " + eggMetas[index] + " should keep its legacy standing riding offset");
                return;
            }
            ship.setOrderedToSit(true);
            ship.setEmotion(0);
            if (Math.abs(ship.getPassengersRidingOffset() - ship.getBbHeight() * sittingFactors[index]) > 1.0E-6D) {
                helper.fail("hime egg " + eggMetas[index] + " should keep its legacy sitting riding offset");
                return;
            }
            ship.setEmotion(4);
            double boredOffset = (eggMetas[index] == 23 || eggMetas[index] == 35)
                    ? ship.getBbHeight() * (eggMetas[index] == 23 ? 0.65D : 0.29D)
                    : (eggMetas[index] == 30 || eggMetas[index] == 31 || eggMetas[index] == 32)
                    ? ship.getBbHeight() * 0.2D
                    : 0.0D;
            if (Math.abs(ship.getPassengersRidingOffset() - boredOffset) > 1.0E-6D) {
                helper.fail("hime egg " + eggMetas[index] + " should keep its legacy bored riding offset");
                return;
            }
            ship.discard();
        }

        if (!attackEffectMatches(28, 70, 3, MobEffects.DIG_SLOWDOWN, 1, 170, 70)
                || !attackEffectMatches(31, 75, 3, MobEffects.BLINDNESS, 0, 175, 75)
                || LegacyShipBehaviorCatalog.rawCriticalBonusFor(49, false, true) != 0.1F
                || LegacyShipBehaviorCatalog.rawTripleHitBonusFor(49, false, false) != 0.0F
                || LegacyShipBehaviorCatalog.rawTripleHitBonusFor(49, false, true) != 0.2F) {
            helper.fail("hime attack effects and CA Hime night raw attributes should match legacy values");
            return;
        }

        ServerPlayer owner = makeServerBackedPlayer(helper, "HimeSupportTester");
        helper.getLevel().players().add(owner);
        owner.setPos(2.0D, 2.0D, 2.0D);

        LegacyShipEntity carrierHime = ModEntityTypes.LEGACY_SHIP.get().create(helper.getLevel());
        LegacyShipEntity escort = ModEntityTypes.LEGACY_SHIP.get().create(helper.getLevel());
        LegacyShipEntity airfieldHime = ModEntityTypes.LEGACY_SHIP.get().create(helper.getLevel());
        LegacyShipEntity woundedEscort = ModEntityTypes.LEGACY_SHIP.get().create(helper.getLevel());
        if (carrierHime == null || escort == null || airfieldHime == null || woundedEscort == null) {
            helper.fail("hime support entities should be creatable");
            return;
        }

        carrierHime.setVariantEggMeta(22);
        carrierHime.setOwner(owner);
        carrierHime.setMarried(true);
        carrierHime.setRingEffectEnabled(true);
        carrierHime.setShipLevel(70);
        carrierHime.setHealth(carrierHime.getMaxHealth());
        carrierHime.setGrudge(100);
        carrierHime.setPos(2.0D, 2.0D, 2.0D);
        escort.setVariantEggMeta(2);
        escort.setOwner(owner);
        escort.setHealth(escort.getMaxHealth());
        escort.setPos(4.0D, 2.0D, 2.0D);
        airfieldHime.setVariantEggMeta(23);
        airfieldHime.setOwner(owner);
        airfieldHime.setMarried(true);
        airfieldHime.setRingEffectEnabled(true);
        airfieldHime.setShipLevel(30);
        airfieldHime.setGrudge(200);
        airfieldHime.setPos(8.0D, 2.0D, 2.0D);
        woundedEscort.setVariantEggMeta(2);
        woundedEscort.setOwner(owner);
        woundedEscort.setPos(10.0D, 2.0D, 2.0D);
        woundedEscort.setHealth(woundedEscort.getMaxHealth() * 0.5F);
        helper.getLevel().addFreshEntity(carrierHime);
        helper.getLevel().addFreshEntity(escort);
        helper.getLevel().addFreshEntity(airfieldHime);
        helper.getLevel().addFreshEntity(woundedEscort);

        carrierHime.tickCount = 128;
        LegacyShipBehaviorCatalog.tickLegacyPerShipBehavior(carrierHime);
        MobEffectInstance jump = escort.getEffect(MobEffects.JUMP);
        if (jump == null || jump.getDuration() != 120 || jump.getAmplifier() != 1) {
            helper.fail("Carrier Hime should apply its same-owner jump aura with legacy values");
            return;
        }

        float woundedHealth = woundedEscort.getHealth();
        airfieldHime.tickCount = 128;
        LegacyShipBehaviorCatalog.tickLegacyPerShipBehavior(airfieldHime);
        if (woundedEscort.getHealth() <= woundedHealth || airfieldHime.getGrudge() != 150) {
            helper.fail("Airfield Hime should heal one same-owner ship and consume exactly 50 grudge: health="
                    + woundedEscort.getHealth() + "/" + woundedHealth + ", grudge=" + airfieldHime.getGrudge());
            return;
        }

        carrierHime.discard();
        escort.discard();
        airfieldHime.discard();
        woundedEscort.discard();
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void himePassiveEffectsMatchLegacyApplicationPaths(GameTestHelper helper) {
        ServerPlayer owner = makeServerBackedPlayer(helper, "HimeEffectPathTester");
        helper.getLevel().players().add(owner);
        owner.setPos(2.0D, 2.0D, 2.0D);

        LegacyShipEntity battleshipHime = ModEntityTypes.LEGACY_SHIP.get().create(helper.getLevel());
        LegacyShipEntity escort = ModEntityTypes.LEGACY_SHIP.get().create(helper.getLevel());
        LegacyShipEntity isolatedHime = ModEntityTypes.LEGACY_SHIP.get().create(helper.getLevel());
        Player target = helper.makeMockPlayer();
        if (battleshipHime == null || escort == null || isolatedHime == null) {
            helper.fail("hime passive effect entities should be creatable");
            return;
        }

        battleshipHime.setVariantEggMeta(28);
        battleshipHime.setOwner(owner);
        battleshipHime.setMarried(true);
        battleshipHime.setRingEffectEnabled(true);
        battleshipHime.setShipLevel(70);
        battleshipHime.setGrudge(1);
        battleshipHime.setNoAi(true);
        battleshipHime.setPos(2.0D, 2.0D, 2.0D);

        escort.setVariantEggMeta(2);
        escort.setOwner(owner);
        escort.setNoAi(true);
        escort.setPos(4.0D, 2.0D, 2.0D);

        helper.getLevel().addFreshEntity(battleshipHime);
        helper.getLevel().addFreshEntity(escort);
        battleshipHime.tickCount = 128;
        LegacyShipBehaviorCatalog.tickLegacyPerShipBehavior(battleshipHime);
        MobEffectInstance resistance = escort.getEffect(MobEffects.DAMAGE_RESISTANCE);
        MobEffectInstance fireResistance = escort.getEffect(MobEffects.FIRE_RESISTANCE);
        if (resistance == null || fireResistance == null
                || resistance.getDuration() != 120 || fireResistance.getDuration() != 120
                || resistance.getAmplifier() != 1 || fireResistance.getAmplifier() != 1) {
            helper.fail("Battleship Hime should apply its legacy resistance and fire resistance aura");
            return;
        }

        isolatedHime.setVariantEggMeta(31);
        isolatedHime.setShipLevel(100);
        isolatedHime.setNoAi(true);
        isolatedHime.setPos(7.0D, 2.0D, 2.0D);
        target.setPos(8.0D, 2.0D, 2.0D);
        helper.getLevel().addFreshEntity(isolatedHime);

        LegacyShipBehaviorCatalog.applyAttackEffect(isolatedHime, target);
        MobEffectInstance blindness = target.getEffect(MobEffects.BLINDNESS);
        if (blindness == null || blindness.getDuration() != 200 || blindness.getAmplifier() != 0
                || target.hasEffect(MobEffects.POISON)) {
            helper.fail("Isolated Hime should apply unconditional blindness without marriage poison");
            return;
        }

        isolatedHime.setMarried(true);
        isolatedHime.setRingEffectEnabled(true);
        target.removeAllEffects();
        LegacyShipBehaviorCatalog.applyAttackEffect(isolatedHime, target);
        blindness = target.getEffect(MobEffects.BLINDNESS);
        MobEffectInstance poison = target.getEffect(MobEffects.POISON);
        if (blindness == null || poison == null
                || blindness.getDuration() != 200 || blindness.getAmplifier() != 0
                || poison.getDuration() != 180 || poison.getAmplifier() != 1) {
            helper.fail("married Isolated Hime should add its legacy poison attack effect");
            return;
        }

        battleshipHime.discard();
        escort.discard();
        isolatedHime.discard();
        target.discard();
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void himeHealingKeepsLegacyFormulasAndFeedbackGate(GameTestHelper helper) {
        Player owner = helper.makeMockPlayer();
        if (!helper.getLevel().addFreshEntity(owner)) {
            helper.fail("mock player should be indexed in the test level for hime healing queries");
            return;
        }
        if (!configureTeitoku(helper, owner, data -> data.setPlayerUid(8124))) {
            return;
        }
        owner.setPos(100.0D, 2.0D, 100.0D);

        LegacyShipEntity harbour = ModEntityTypes.LEGACY_SHIP.get().create(helper.getLevel());
        LegacyShipEntity escort = ModEntityTypes.LEGACY_SHIP.get().create(helper.getLevel());
        if (harbour == null || escort == null) {
            helper.fail("Harbour Hime and its same-owner target should be creatable for healing tests");
            return;
        }

        harbour.setVariantEggMeta(30);
        harbour.setOwner(owner);
        harbour.setMarried(true);
        harbour.setRingEffectEnabled(true);
        harbour.setShipLevel(100);
        harbour.setGrudge(100);
        harbour.setPos(2.0D, 2.0D, 2.0D);

        escort.setVariantEggMeta(2);
        escort.setOwner(owner);
        escort.setPos(4.0D, 2.0D, 2.0D);
        escort.setHealth(1.0F);

        float harbourHealthBefore = harbour.getMaxHealth() - 20.0F;
        harbour.setHealth(harbourHealthBefore);
        float escortHealthBefore = escort.getHealth();
        helper.getLevel().addFreshEntity(harbour);
        helper.getLevel().addFreshEntity(escort);

        harbour.tickCount = 128;
        LegacyShipBehaviorCatalog.tickLegacyPerShipBehavior(harbour);

        float expectedHarbourHealth = Math.min(harbour.getMaxHealth(),
                harbourHealthBefore + (harbour.getMaxHealth() * 0.06F + 1.0F)
                        * harbour.getLegacyStats().healRate());
        float expectedEscortHealth = Math.min(escort.getMaxHealth(),
                escortHealthBefore + (4.0F + escort.getMaxHealth() * 0.06F
                        + harbour.getShipLevel() * 0.2F) * escort.getLegacyStats().healRate());
        if (Math.abs(harbour.getHealth() - expectedHarbourHealth) > 0.001F
                || Math.abs(escort.getHealth() - expectedEscortHealth) > 0.001F
                || harbour.getGrudge() != 50) {
            helper.fail("Harbour Hime should keep its legacy self/ship heal formulas and one 50-grudge cost");
            return;
        }

        harbour.discard();
        escort.discard();
        owner.setPos(2.0D, 2.0D, 2.0D);

        LegacyShipEntity northern = ModEntityTypes.LEGACY_SHIP.get().create(helper.getLevel());
        if (northern == null) {
            helper.fail("Northern Hime should be creatable for player healing tests");
            return;
        }
        northern.setVariantEggMeta(33);
        northern.setOwner(owner);
        northern.setMarried(true);
        northern.setRingEffectEnabled(true);
        northern.setShipLevel(25);
        northern.setGrudge(100);
        northern.setPos(4.0D, 2.0D, 2.0D);
        owner.setHealth(10.0F);
        helper.getLevel().addFreshEntity(northern);

        northern.tickCount = 64;
        LegacyShipBehaviorCatalog.tickLegacyPerShipBehavior(northern);
        float expectedOwnerHealth = Math.min(owner.getMaxHealth(), 10.0F
                + 1.0F + owner.getMaxHealth() * 0.02F + northern.getShipLevel() * 0.02F);
        if (Math.abs(owner.getHealth() - expectedOwnerHealth) > 0.001F
                || northern.getGrudge() != 75) {
            helper.fail("Northern Hime should keep its legacy player heal formula and one 25-grudge cost");
            return;
        }

        northern.discard();
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void submarineHimeLightAttackLaunchesDoubleMissile(GameTestHelper helper) {
        LegacyShipEntity ship = ModEntityTypes.LEGACY_SHIP.get().create(helper.getLevel());
        Zombie target = EntityType.ZOMBIE.create(helper.getLevel());
        if (ship == null || target == null) {
            helper.fail("hime submarine and zombie should be creatable for double missile tests");
            return;
        }

        ship.setVariantEggMeta(46);
        ship.setShipLevel(50);
        ship.setLightAmmo(1000);
        ship.setHeavyAmmo(1000);
        ship.setGrudge(1000);
        ship.setNoAi(true);
        ship.setPos(1.5D, 2.0D, 1.5D);
        target.setNoAi(true);
        target.setPos(4.5D, 2.0D, 1.5D);
        helper.getLevel().addFreshEntity(ship);
        helper.getLevel().addFreshEntity(target);

        float healthBefore = target.getHealth();
        if (!ship.performPlayerCompatAttack(target, LegacyShipAttackKind.LIGHT)) {
            helper.fail("hime submarine light attack should launch through its legacy hook: reason="
                    + ship.getCompatAttackFailureReason(target, LegacyShipAttackKind.LIGHT));
            return;
        }

        List<LegacyShipProjectileEntity> projectiles = helper.getLevel().getEntitiesOfClass(
                LegacyShipProjectileEntity.class,
                ship.getBoundingBox().inflate(20.0D),
                projectile -> projectile.getOwner() == ship
                        && projectile.getAttackKind() == LegacyShipAttackKind.LIGHT);
        if (projectiles.size() != 2) {
            helper.fail("hime submarine light attack should create exactly two missile projectiles");
            return;
        }
        for (LegacyShipProjectileEntity projectile : projectiles) {
            // EntityAbyssMissile keeps ARC for short horizontal launches, even when
            // its constructor replaces the arc acceleration with direct motion.
            LegacyShipProjectileMoveType expectedMoveType = expectedLegacyArcMissileMove(
                    ship, projectile);
            if (projectile.getProjectileVisual() != LegacyShipProjectileVisual.MISSILE
                    || projectile.getMoveType() != expectedMoveType
                    || projectile.getIntendedTargetId() != target.getId()
                    || Math.abs(projectile.getY() - (ship.getY() + ship.getBbHeight() * 0.6D)) > 1.0E-6D
                    || projectile.getImpactRadius() != 3.5D
                    || projectile.getKnockbackStrength() != 0.15D) {
                helper.fail("hime submarine missile mismatch: visual=" + projectile.getProjectileVisual()
                        + ", move=" + projectile.getMoveType()
                        + ", expectedMove=" + expectedMoveType
                        + ", depth=" + ship.getLegacyLiquidDepth()
                        + ", targetId=" + projectile.getIntendedTargetId() + "/" + target.getId()
                        + ", y=" + projectile.getY() + "/" + (ship.getY() + ship.getBbHeight() * 0.6D)
                        + ", position=" + projectile.position()
                        + ", target=" + projectile.getLegacyTargetPosition()
                        + ", motion=" + projectile.getDeltaMovement());
                return;
            }
            if (!projectile.isMissedShot()
                    && (Math.abs(projectile.getLegacyTargetPosition().x - target.getX()) > 1.0E-6D
                    || Math.abs(projectile.getLegacyTargetPosition().y - (target.getY() + 0.1D)) > 1.0E-6D
                    || Math.abs(projectile.getLegacyTargetPosition().z - target.getZ()) > 1.0E-6D)) {
                helper.fail("hime submarine missiles should use the legacy targetHeight=1 target point");
                return;
            }
        }
        if (Math.abs(projectiles.get(0).getX() - projectiles.get(1).getX()) < 1.9D
                || target.getHealth() != healthBefore
                || ship.getCompatAttackCooldown(LegacyShipAttackKind.LIGHT) <= 0) {
            helper.fail("hime submarine missiles should launch from opposite sides without direct damage");
            return;
        }

        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void ssnhLightAttackLaunchesDoubleMissile(GameTestHelper helper) {
        LegacyShipEntity ship = ModEntityTypes.LEGACY_SHIP.get().create(helper.getLevel());
        Zombie target = EntityType.ZOMBIE.create(helper.getLevel());
        if (ship == null || target == null) {
            helper.fail("SSNH and zombie should be creatable for double missile tests");
            return;
        }

        ship.setVariantEggMeta(74);
        ship.setShipLevel(50);
        ship.setMorale(3000);
        ship.setLightAmmo(1000);
        ship.setHeavyAmmo(1000);
        ship.setGrudge(1000);
        ship.setNoAi(true);
        ship.setNoGravity(true);
        ship.setPos(1.5D, 2.0D, 1.5D);
        helper.getLevel().setBlock(BlockPos.containing(ship.getX(), ship.getY(), ship.getZ()),
                Blocks.STONE.defaultBlockState(), Block.UPDATE_ALL);
        target.setNoAi(true);
        target.setPos(4.5D, 2.0D, 1.5D);
        helper.getLevel().addFreshEntity(ship);
        helper.getLevel().addFreshEntity(target);

        float healthBefore = target.getHealth();
        if (!ship.performPlayerCompatAttack(target, LegacyShipAttackKind.LIGHT)) {
            helper.fail("SSNH light attack should launch through its legacy hook: reason="
                    + ship.getCompatAttackFailureReason(target, LegacyShipAttackKind.LIGHT));
            return;
        }

        List<LegacyShipProjectileEntity> projectiles = helper.getLevel().getEntitiesOfClass(
                LegacyShipProjectileEntity.class,
                ship.getBoundingBox().inflate(20.0D),
                projectile -> projectile.getOwner() == ship
                        && projectile.getAttackKind() == LegacyShipAttackKind.LIGHT);
        if (projectiles.size() != 2) {
            helper.fail("SSNH light attack should create exactly two missile projectiles");
            return;
        }
        for (LegacyShipProjectileEntity projectile : projectiles) {
            LegacyShipProjectileMoveType expectedMoveType = expectedLegacyArcMissileMove(
                    ship, projectile);
            if (projectile.getProjectileVisual() != LegacyShipProjectileVisual.MISSILE
                    || projectile.getMoveType() != expectedMoveType
                    || projectile.getIntendedTargetId() != target.getId()
                    || Math.abs(projectile.getY() - (ship.getY() + ship.getBbHeight() * 0.6D)) > 1.0E-6D
                    || projectile.getImpactRadius() != 3.5D
                    || projectile.getKnockbackStrength() != 0.15D) {
                helper.fail("SSNH missile should preserve the legacy launch profile: visual="
                        + projectile.getProjectileVisual() + ", move=" + projectile.getMoveType()
                        + ", expectedMove=" + expectedMoveType
                        + ", depth=" + ship.getLegacyLiquidDepth()
                        + ", targetId=" + projectile.getIntendedTargetId() + "/" + target.getId()
                        + ", y=" + projectile.getY() + "/" + (ship.getY() + ship.getBbHeight() * 0.6D));
                return;
            }
            if (!projectile.isMissedShot()
                    && (Math.abs(projectile.getLegacyTargetPosition().x - target.getX()) > 1.0E-6D
                    || Math.abs(projectile.getLegacyTargetPosition().y - (target.getY() + 0.1D)) > 1.0E-6D
                    || Math.abs(projectile.getLegacyTargetPosition().z - target.getZ()) > 1.0E-6D)) {
                helper.fail("SSNH missiles should use the legacy targetHeight=1 target point");
                return;
            }
        }
        if (Math.abs(projectiles.get(0).getX() - projectiles.get(1).getX()) < 1.9D
                || target.getHealth() != healthBefore
                || ship.getCompatAttackCooldown(LegacyShipAttackKind.LIGHT) <= 0) {
            helper.fail("SSNH missiles should launch from opposite sides without direct damage");
            return;
        }

        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void carrierHimeKeepsLegacyMeleeMissileLaunch(GameTestHelper helper) {
        LegacyShipEntity ship = ModEntityTypes.LEGACY_SHIP.get().create(helper.getLevel());
        Zombie target = EntityType.ZOMBIE.create(helper.getLevel());
        if (ship == null || target == null) {
            helper.fail("Carrier Hime and zombie should be creatable for melee missile tests");
            return;
        }

        ship.setVariantEggMeta(22);
        ship.setNoAi(true);
        ship.setPos(1.5D, 2.0D, 1.5D);
        target.setNoAi(true);
        target.setPos(4.0D, 2.0D, 1.5D);
        helper.getLevel().addFreshEntity(ship);
        helper.getLevel().addFreshEntity(target);

        float healthBefore = target.getHealth();
        if (!ship.performPlayerCompatAttack(target, LegacyShipAttackKind.MELEE)) {
            helper.fail("Carrier Hime should route melee through its legacy missile hook");
            return;
        }

        List<LegacyShipProjectileEntity> projectiles = helper.getLevel().getEntitiesOfClass(
                LegacyShipProjectileEntity.class,
                ship.getBoundingBox().inflate(12.0D),
                projectile -> projectile.getOwner() == ship
                        && projectile.getAttackKind() == LegacyShipAttackKind.MELEE);
        if (projectiles.size() != 1) {
            helper.fail("Carrier Hime should launch exactly one melee missile");
            return;
        }

        LegacyShipProjectileEntity projectile = projectiles.get(0);
        if (projectile.getProjectileVisual() != LegacyShipProjectileVisual.MISSILE
                || projectile.getMoveType() != LegacyShipProjectileMoveType.DIRECT
                || projectile.getIntendedTargetId() != target.getId()
                || Math.abs(projectile.getY() - (ship.getY() + 1.3D)) > 1.0E-6D
                || projectile.getImpactRadius() != 3.5D
                || projectile.getKnockbackStrength() != 0.0D
                || projectile.isMissedShot()
                || target.getHealth() != healthBefore
                || ship.getCompatAttackCooldown(LegacyShipAttackKind.MELEE) <= 0) {
            helper.fail("Carrier Hime melee missile should preserve the legacy launch height and delayed impact");
            return;
        }

        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void battleshipHimeReleasesLegacyClusterMissiles(GameTestHelper helper) {
        LegacyShipEntity ship = ModEntityTypes.LEGACY_SHIP.get().create(helper.getLevel());
        Zombie target = EntityType.ZOMBIE.create(helper.getLevel());
        if (ship == null || target == null) {
            helper.fail("Battleship Hime and zombie should be creatable for cluster missile tests");
            return;
        }

        ship.setVariantEggMeta(28);
        ship.setNoAi(true);
        ship.setPos(1.5D, 2.0D, 1.5D);
        target.setNoAi(true);
        target.setPos(40.0D, 2.0D, 1.5D);
        helper.getLevel().addFreshEntity(ship);
        helper.getLevel().addFreshEntity(target);

        LegacyShipProjectileEntity mainMissile = LegacyShipProjectileEntity.create(
                helper.getLevel(), ship, target, LegacyShipAttackKind.HEAVY,
                ship.getLegacyStats().attackHeavy(), false,
                ship.getAttackProfile().projectileProfile(LegacyShipAttackKind.HEAVY),
                target.position(), 0.4D, false);
        helper.getLevel().addFreshEntity(mainMissile);
        if (mainMissile.getImpactRadius() != 3.5D
                || mainMissile.getMoveType() != LegacyShipProjectileMoveType.ARC) {
            helper.fail("Battleship Hime main missile should preserve the legacy cluster arc and explosion radius");
            return;
        }

        mainMissile.tickCount = 8;
        mainMissile.tick();
        List<LegacyShipProjectileEntity> clusterMissiles = helper.getLevel().getEntitiesOfClass(
                LegacyShipProjectileEntity.class,
                ship.getBoundingBox().inflate(48.0D),
                projectile -> projectile.getOwner() == ship && projectile != mainMissile);
        if (clusterMissiles.size() != 1
                || clusterMissiles.get(0).getMoveType() != LegacyShipProjectileMoveType.DIRECT
                || clusterMissiles.get(0).getImpactRadius() != 3.5D
                || clusterMissiles.get(0).getIntendedTargetId() != target.getId()) {
            helper.fail("Battleship Hime should release one direct half-damage sub-missile at tick 8");
            return;
        }

        mainMissile.tickCount = 16;
        mainMissile.tick();
        int secondWaveCount = helper.getLevel().getEntitiesOfClass(
                LegacyShipProjectileEntity.class,
                ship.getBoundingBox().inflate(48.0D),
                projectile -> projectile.getOwner() == ship && projectile != mainMissile).size();
        if (secondWaveCount != 2) {
            helper.fail("Battleship Hime should keep the eight-tick cluster release cadence");
            return;
        }

        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void northernHimeSummonsLegacyFloatingFort(GameTestHelper helper) {
        LegacyShipEntity ship = ModEntityTypes.LEGACY_SHIP.get().create(helper.getLevel());
        Zombie target = EntityType.ZOMBIE.create(helper.getLevel());
        if (ship == null || target == null) {
            helper.fail("Northern Hime and zombie should be creatable for FloatingFort tests");
            return;
        }

        ship.setVariantEggMeta(33);
        supplyShipForTest(ship);
        ship.setNoAi(true);
        ship.setPos(1.5D, 2.0D, 1.5D);
        target.setNoAi(true);
        target.setPos(6.5D, 2.0D, 1.5D);
        helper.getLevel().addFreshEntity(ship);
        helper.getLevel().addFreshEntity(target);

        float healthBefore = target.getHealth();
        if (!ship.performPlayerCompatAttack(target, LegacyShipAttackKind.HEAVY)) {
            helper.fail("Northern Hime should route heavy attack through its FloatingFort hook");
            return;
        }

        List<LegacyShipFloatingFortEntity> forts = helper.getLevel().getEntitiesOfClass(
                LegacyShipFloatingFortEntity.class,
                ship.getBoundingBox().inflate(12.0D),
                fort -> fort.getHostId() == ship.getId() && fort.getTargetId() == target.getId());
        if (forts.size() != 1) {
            helper.fail("Northern Hime should summon exactly one FloatingFort for a heavy attack");
            return;
        }

        LegacyShipFloatingFortEntity fort = forts.get(0);
        double expectedLaunchY = ship.getY() + ship.getBbHeight()
                + (ship.getLegacyLiquidDepth() > 0.0D ? 0.2D : 0.0D);
        if (fort.getLifeTicks() != 500
                || Math.abs(fort.getY() - expectedLaunchY) > 1.0E-6D
                || target.getHealth() != healthBefore
                || ship.getCompatAttackCooldown(LegacyShipAttackKind.HEAVY) <= 0) {
            helper.fail("Northern Hime FloatingFort should preserve the legacy lifetime, launch height, and delayed impact");
            return;
        }

        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void northernHimeDismountsWhenDamaged(GameTestHelper helper) {
        LegacyShipEntity northern = ModEntityTypes.LEGACY_SHIP.get().create(helper.getLevel());
        LegacyShipEntity vehicle = ModEntityTypes.LEGACY_SHIP.get().create(helper.getLevel());
        if (northern == null || vehicle == null) {
            helper.fail("Northern Hime and a ship vehicle should be creatable for dismount tests");
            return;
        }

        northern.setVariantEggMeta(33);
        northern.setNoAi(true);
        northern.setPos(1.5D, 2.0D, 1.5D);
        vehicle.setVariantEggMeta(53);
        vehicle.setNoAi(true);
        vehicle.setPos(1.5D, 2.0D, 1.5D);
        helper.getLevel().addFreshEntity(northern);
        helper.getLevel().addFreshEntity(vehicle);

        if (!northern.startRiding(vehicle, true) || !northern.isPassenger()) {
            helper.fail("Northern Hime should be mountable on a ship before damage");
            return;
        }

        northern.hurt(northern.damageSources().generic(), 1.0F);
        if (northern.isPassenger()) {
            helper.fail("Northern Hime should dismount before applying incoming damage");
            return;
        }

        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void ssnhDismountsWhenDamaged(GameTestHelper helper) {
        LegacyShipEntity ssnh = ModEntityTypes.LEGACY_SHIP.get().create(helper.getLevel());
        LegacyShipEntity vehicle = ModEntityTypes.LEGACY_SHIP.get().create(helper.getLevel());
        if (ssnh == null || vehicle == null) {
            helper.fail("SSNH and a ship vehicle should be creatable for dismount tests");
            return;
        }

        ssnh.setVariantEggMeta(74);
        ssnh.setNoAi(true);
        ssnh.setPos(1.5D, 2.0D, 1.5D);
        vehicle.setVariantEggMeta(53);
        vehicle.setNoAi(true);
        vehicle.setPos(1.5D, 2.0D, 1.5D);
        helper.getLevel().addFreshEntity(ssnh);
        helper.getLevel().addFreshEntity(vehicle);

        if (!LegacyShipBehaviorCatalog.dismountsBeforeDamage(ssnh.getSpec())
                || !LegacyShipBehaviorCatalog.dismountsBeforeDamage(
                ShipEntitySpecs.getByEggMeta(74))
                || LegacyShipBehaviorCatalog.dismountsBeforeDamage(
                ShipEntitySpecs.getByEggMeta(22))) {
            helper.fail("only Northern Hime and SSNH should keep the legacy damage dismount override");
            return;
        }

        if (!ssnh.startRiding(vehicle, true) || !ssnh.isPassenger()) {
            helper.fail("SSNH should be mountable on a ship before damage");
            return;
        }

        ssnh.hurt(ssnh.damageSources().generic(), 1.0F);
        if (ssnh.isPassenger()) {
            helper.fail("SSNH should dismount before applying incoming damage");
            return;
        }

        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void northernHimeAndSsnhAllowLegacyBoredPlayerRiding(GameTestHelper helper) {
        ServerPlayer owner = makeServerBackedPlayer(helper, "HimeRider");
        owner.setPos(1.5D, 2.0D, 1.5D);
        helper.getLevel().players().add(owner);

        LegacyShipEntity northern = ModEntityTypes.LEGACY_SHIP.get().create(helper.getLevel());
        LegacyShipEntity ssnh = ModEntityTypes.LEGACY_SHIP.get().create(helper.getLevel());
        if (northern == null || ssnh == null) {
            helper.fail("Northern Hime and SSNH should be creatable for bored ride tests");
            return;
        }

        northern.setVariantEggMeta(33);
        northern.setOwner(owner);
        northern.setOrderedToSit(true);
        northern.setEmotion(4);
        northern.setPos(1.5D, 2.0D, 1.5D);
        helper.getLevel().addFreshEntity(northern);

        if (northern.mobInteract(owner, InteractionHand.MAIN_HAND) != InteractionResult.CONSUME
                || !northern.isPassenger() || northern.getVehicle() != owner
                || northern.isOrderedToSit()) {
            helper.fail("Northern Hime should preserve the legacy bored player riding request");
            return;
        }
        northern.discard();

        ssnh.setVariantEggMeta(74);
        ssnh.setOwner(owner);
        ssnh.setOrderedToSit(true);
        ssnh.setEmotion(4);
        ssnh.setPos(1.5D, 2.0D, 1.5D);
        helper.getLevel().addFreshEntity(ssnh);

        if (ssnh.mobInteract(owner, InteractionHand.MAIN_HAND) != InteractionResult.CONSUME
                || !ssnh.isPassenger() || ssnh.getVehicle() != owner
                || ssnh.isOrderedToSit()) {
            helper.fail("SSNH should preserve the legacy bored player riding request");
            return;
        }

        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void isolatedHimeKeepsLegacyBlackHoleHeavyImpact(GameTestHelper helper) {
        LegacyShipEntity ship = ModEntityTypes.LEGACY_SHIP.get().create(helper.getLevel());
        Zombie target = EntityType.ZOMBIE.create(helper.getLevel());
        if (ship == null || target == null) {
            helper.fail("Isolated Hime and zombie should be creatable for black hole tests");
            return;
        }

        ship.setVariantEggMeta(31);
        ship.setNoAi(true);
        ship.setPos(1.5D, 2.0D, 1.5D);
        target.setNoAi(true);
        target.setPos(8.0D, 2.0D, 1.5D);
        helper.getLevel().addFreshEntity(ship);
        helper.getLevel().addFreshEntity(target);

        LegacyShipProjectileEntity projectile = LegacyShipProjectileEntity.create(
                helper.getLevel(), ship, target, LegacyShipAttackKind.HEAVY,
                ship.getLegacyStats().attackHeavy(), false,
                ship.getAttackProfile().projectileProfile(LegacyShipAttackKind.HEAVY),
                target.position(), 0.4D, false);
        if (!LegacyShipBehaviorCatalog.usesBlackHoleHeavyMissile(ship, LegacyShipAttackKind.HEAVY)
                || !projectile.hasBlackHoleImpactEffect()
                || projectile.getProjectileVisual() != LegacyShipProjectileVisual.MISSILE) {
            helper.fail("Isolated Hime heavy missiles should preserve the legacy type-5 black hole payload");
            return;
        }

        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void hibikiKeepsLegacyDodgeBonus(GameTestHelper helper) {
        LegacyShipStats hibiki = LegacyShipStats.create(
                52,
                ShipArchetype.DESTROYER,
                false,
                1,
                3000,
                0, 0, 0, 0, 0, 0,
                false,
                0,
                ShipEquipmentProfile.EMPTY,
                List.of());
        LegacyShipStats hostileHibiki = LegacyShipStats.create(
                2052,
                ShipArchetype.DESTROYER,
                true,
                1,
                3000,
                0, 0, 0, 0, 0, 0,
                false,
                0,
                ShipEquipmentProfile.EMPTY,
                List.of());
        if (Math.abs(hibiki.dodge() - 0.30F) > 0.0001F) {
            helper.fail("friendly Hibiki should retain the legacy raw dodge bonus");
            return;
        }
        if (Math.abs(hostileHibiki.dodge() - 0.15F) > 0.0001F) {
            helper.fail("hostile Hibiki should not inherit the friendly raw dodge bonus");
            return;
        }

        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void tenryuuKeepsLegacyNightCombatBonuses(GameTestHelper helper) {
        LegacyShipStats day = LegacyShipStats.create(
                56,
                ShipArchetype.CRUISER,
                false,
                1,
                3000,
                0, 0, 0, 0, 0, 0,
                false,
                0,
                ShipEquipmentProfile.EMPTY,
                List.of(),
                false);
        LegacyShipStats night = LegacyShipStats.create(
                56,
                ShipArchetype.CRUISER,
                false,
                1,
                3000,
                0, 0, 0, 0, 0, 0,
                false,
                0,
                ShipEquipmentProfile.EMPTY,
                List.of(),
                true);
        LegacyShipStats hostileNight = LegacyShipStats.create(
                2058,
                ShipArchetype.CRUISER,
                true,
                1,
                3000,
                0, 0, 0, 0, 0, 0,
                false,
                0,
                ShipEquipmentProfile.EMPTY,
                List.of(),
                true);

        if (Math.abs(night.critical() - day.critical() - 0.15F) > 0.0001F
                || Math.abs(night.dodge() - day.dodge() - 0.15F) > 0.0001F) {
            helper.fail("friendly Tenryuu should gain the legacy night critical and dodge bonuses");
            return;
        }
        if (Math.abs(hostileNight.critical() - 0.15F) > 0.0001F
                || Math.abs(hostileNight.dodge() - 0.15F) > 0.0001F) {
            helper.fail("hostile Tenryuu should keep hostile base stats without the friendly night bonus");
            return;
        }

        LegacyShipEntity entity = ModEntityTypes.LEGACY_SHIP.get().create(helper.getLevel());
        if (entity == null) {
            helper.fail("Tenryuu entity should be creatable for world night refresh tests");
            return;
        }
        helper.getLevel().setDayTime(6000L);
        entity.setMorale(3000);
        entity.setVariantEggMeta(58);
        float entityDayCritical = entity.getLegacyStats().critical();
        float entityDayDodge = entity.getLegacyStats().dodge();
        helper.getLevel().setDayTime(18000L);
        entity.setVariantEggMeta(58);
        if (Math.abs(entity.getLegacyStats().critical() - entityDayCritical - 0.15F) > 0.0001F
                || Math.abs(entity.getLegacyStats().dodge() - entityDayDodge - 0.15F) > 0.0001F) {
            helper.fail("Tenryuu entity refresh should apply the world night context");
            return;
        }

        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void caHimeRefreshesNightStatsOnLegacyCadence(GameTestHelper helper) {
        LegacyShipEntity entity = ModEntityTypes.LEGACY_SHIP.get().create(helper.getLevel());
        if (entity == null) {
            helper.fail("CA Hime should be creatable for day/night stat refresh tests");
            return;
        }

        helper.getLevel().setDayTime(6000L);
        entity.setNoAi(true);
        entity.setGrudge(1000);
        entity.setMorale(3800);
        entity.setVariantEggMeta(51);
        float dayCritical = entity.getLegacyStats().critical();
        float dayTripleHit = entity.getLegacyStats().tripleHit();
        helper.getLevel().addFreshEntity(entity);

        helper.getLevel().setDayTime(18000L);
        entity.tickCount = 128;
        entity.tick();
        if (Math.abs(entity.getLegacyStats().critical() - dayCritical - 0.1F) > 0.0001F
                || Math.abs(entity.getLegacyStats().tripleHit() - dayTripleHit - 0.2F) > 0.0001F) {
            helper.fail("CA Hime should apply its legacy night critical and triple-hit bonuses at the 128-tick refresh");
            return;
        }

        helper.getLevel().setDayTime(6000L);
        entity.tickCount = 256;
        entity.tick();
        if (Math.abs(entity.getLegacyStats().critical() - dayCritical) > 0.0001F
                || Math.abs(entity.getLegacyStats().tripleHit() - dayTripleHit) > 0.0001F) {
            helper.fail("CA Hime should remove its legacy night bonuses when the world returns to daytime");
            return;
        }

        entity.discard();
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void akatsukiKeepsLegacyRingSearchlightOverride(GameTestHelper helper) {
        LegacyShipEntity friendly = ModEntityTypes.LEGACY_SHIP.get().create(helper.getLevel());
        LegacyShipEntity hostile = ModEntityTypes.LEGACY_SHIP.get().create(helper.getLevel());
        if (friendly == null || hostile == null) {
            helper.fail("Akatsuki variants should be creatable for searchlight override tests");
            return;
        }

        friendly.setVariantEggMeta(53);
        friendly.setMarried(true);
        friendly.setRingEffectEnabled(true);
        if (friendly.getEquipmentBehaviorState().searchlightLevel() != 1) {
            helper.fail("married friendly Akatsuki should gain the legacy ring searchlight level");
            return;
        }

        friendly.setRingEffectEnabled(false);
        if (friendly.getEquipmentBehaviorState().searchlightLevel() != 0) {
            helper.fail("disabling the ring effect should remove Akatsuki's derived searchlight level");
            return;
        }

        friendly.setRingEffectEnabled(true);
        friendly.setMarried(false);
        if (friendly.getEquipmentBehaviorState().searchlightLevel() != 0) {
            helper.fail("unmarried Akatsuki should not retain the derived searchlight level");
            return;
        }

        hostile.setVariantEggMeta(2053);
        hostile.setMarried(true);
        hostile.setRingEffectEnabled(true);
        if (hostile.getEquipmentBehaviorState().searchlightLevel() != 0
                || LegacyShipBehaviorCatalog.behaviorFor(hostile.getSpec()).marriagePassive()
                != LegacyShipBehaviorCatalog.MarriagePassive.NONE) {
            helper.fail("hostile Akatsuki should not inherit the friendly ring searchlight override");
            return;
        }

        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void sixDestroyerRiderStateFollowsLegacyMasks(GameTestHelper helper) {
        LegacyShipEntity akatsuki = ModEntityTypes.LEGACY_SHIP.get().create(helper.getLevel());
        LegacyShipEntity hibiki = ModEntityTypes.LEGACY_SHIP.get().create(helper.getLevel());
        LegacyShipEntity inazuma = ModEntityTypes.LEGACY_SHIP.get().create(helper.getLevel());
        LegacyShipEntity ikazuchi = ModEntityTypes.LEGACY_SHIP.get().create(helper.getLevel());
        if (akatsuki == null || hibiki == null || inazuma == null || ikazuchi == null) {
            helper.fail("six destroyer variants should be creatable for rider state tests");
            return;
        }

        UUID ownerUuid = UUID.randomUUID();
        LegacyShipEntity[] ships = {akatsuki, hibiki, inazuma, ikazuchi};
        int[] eggMetas = {53, 54, 56, 55};
        for (int index = 0; index < ships.length; index++) {
            LegacyShipEntity ship = ships[index];
            ship.setVariantEggMeta(eggMetas[index]);
            ship.setOwner(ownerUuid, "GattaiTester", 91);
            ship.setGrudge(100);
            ship.setNoAi(true);
            ship.setPos(2.5D + index, 2.0D, 2.5D);
            helper.getLevel().addFreshEntity(ship);
        }

        if (!hibiki.startRiding(akatsuki, true)
                || !inazuma.startRiding(akatsuki, true)
                || !ikazuchi.startRiding(akatsuki, true)) {
            helper.fail("six destroyer riders should be able to mount Akatsuki");
            return;
        }

        akatsuki.tickCount = 32;
        LegacyShipBehaviorCatalog.tickLegacyRiderBehavior(akatsuki);
        if (akatsuki.getLegacyRiderType() != 7
                || akatsuki.getLegacyRidingState() != 1
                || hibiki.getLegacyRiderType() != 7
                || hibiki.getLegacyRidingState() != 2
                || inazuma.getLegacyRiderType() != 7
                || inazuma.getLegacyRidingState() != 3
                || ikazuchi.getLegacyRiderType() != 7
                || ikazuchi.getLegacyRidingState() != 2) {
            helper.fail("six destroyer rider states should mirror legacy masks 7/1/2/3/2");
            return;
        }

        hibiki.stopRiding();
        inazuma.stopRiding();
        ikazuchi.stopRiding();
        if (!inazuma.startRiding(akatsuki, true) || !ikazuchi.startRiding(akatsuki, true)) {
            helper.fail("the invalid six destroyer mask setup should be mountable");
            return;
        }
        akatsuki.tickCount = 64;
        LegacyShipBehaviorCatalog.tickLegacyRiderBehavior(akatsuki);
        if (!akatsuki.getPassengers().isEmpty()
                || akatsuki.getLegacyRiderType() != 0
                || inazuma.getLegacyRidingState() != 0
                || ikazuchi.getLegacyRidingState() != 0) {
            helper.fail("legacy invalid rider masks should dismount every six destroyer rider");
            return;
        }

        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void akatsukiDismountsRidersOnLegacyFuelOrDamage(GameTestHelper helper) {
        LegacyShipEntity akatsuki = ModEntityTypes.LEGACY_SHIP.get().create(helper.getLevel());
        LegacyShipEntity hibiki = ModEntityTypes.LEGACY_SHIP.get().create(helper.getLevel());
        if (akatsuki == null || hibiki == null) {
            helper.fail("Akatsuki and Hibiki should be creatable for dismount regression tests");
            return;
        }

        UUID ownerUuid = UUID.randomUUID();
        akatsuki.setVariantEggMeta(53);
        akatsuki.setOwner(ownerUuid, "AkatsukiTester", 92);
        akatsuki.setGrudge(100);
        akatsuki.setNoAi(true);
        akatsuki.setPos(2.5D, 2.0D, 2.5D);
        hibiki.setVariantEggMeta(54);
        hibiki.setOwner(ownerUuid, "AkatsukiTester", 92);
        hibiki.setGrudge(100);
        hibiki.setNoAi(true);
        hibiki.setPos(2.5D, 2.0D, 2.5D);
        helper.getLevel().addFreshEntity(akatsuki);
        helper.getLevel().addFreshEntity(hibiki);

        if (!hibiki.startRiding(akatsuki, true)) {
            helper.fail("Hibiki should be able to mount Akatsuki before the fuel boundary test");
            return;
        }

        akatsuki.setGrudge(0);
        akatsuki.tickCount = 32;
        LegacyShipBehaviorCatalog.tickLegacyRiderBehavior(akatsuki);
        if (!akatsuki.getPassengers().isEmpty()
                || hibiki.isPassenger()
                || akatsuki.getLegacyRiderType() != 0
                || hibiki.getLegacyRiderType() != 0
                || hibiki.getLegacyRidingState() != 0) {
            helper.fail("Akatsuki should dismount and clear every rider when legacy fuel reaches zero");
            return;
        }

        akatsuki.setGrudge(100);
        akatsuki.setHealth(akatsuki.getMaxHealth());
        if (!hibiki.startRiding(akatsuki, true)) {
            helper.fail("Hibiki should be able to remount Akatsuki before the damage boundary test");
            return;
        }
        if (!akatsuki.hurt(akatsuki.damageSources().generic(), 1.0F)) {
            helper.fail("Akatsuki should accept the small generic damage used by the legacy dismount test");
            return;
        }
        if (!akatsuki.getPassengers().isEmpty()
                || hibiki.isPassenger()
                || akatsuki.getLegacyRiderType() != 0
                || hibiki.getLegacyRiderType() != 0
                || hibiki.getLegacyRidingState() != 0) {
            helper.fail("Akatsuki should dismount and clear every rider after accepted legacy damage");
            return;
        }

        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void sixDestroyerRidersBreakFormationAfterDamage(GameTestHelper helper) {
        LegacyShipEntity[] ships = new LegacyShipEntity[11];
        int[] eggMetas = {53, 54, 53, 55, 56, 53, 56, 56, 55, 56, 55};
        UUID ownerUuid = UUID.randomUUID();
        BlockPos base = helper.absolutePos(new BlockPos(1, 32, 1));
        for (int index = 0; index < ships.length; index++) {
            ships[index] = ModEntityTypes.LEGACY_SHIP.get().create(helper.getLevel());
            if (ships[index] == null) {
                helper.fail("six destroyer damage paths should be creatable");
                return;
            }

            ships[index].setVariantEggMeta(eggMetas[index]);
            ships[index].setOwner(ownerUuid, "DamageFormationTester", 93);
            ships[index].setGrudge(100);
            ships[index].setNoAi(true);
            ships[index].setNoGravity(true);
            ships[index].setPos(base.getX() + 0.5D + index * 2.0D,
                    base.getY(), base.getZ() + 0.5D);
            helper.getLevel().addFreshEntity(ships[index]);
        }

        // Akatsuki's own damage path clears its complete passenger list.
        if (!ships[1].startRiding(ships[0], true)
                || !ships[0].hurt(ships[0].damageSources().generic(), 1.0F)
                || !ships[0].getPassengers().isEmpty()
                || ships[1].isPassenger()
                || ships[0].getLegacyRiderType() != 0
                || ships[1].getLegacyRidingState() != 0) {
            helper.fail("Akatsuki damage should cancel the complete six-destroyer formation");
            return;
        }

        // Ikazuchi damage while mounted on Akatsuki cancels six-destroyer gattai.
        if (!ships[3].startRiding(ships[2], true)
                || !ships[4].startRiding(ships[2], true)
                || !ships[3].hurt(ships[3].damageSources().generic(), 1.0F)
                || !ships[2].getPassengers().isEmpty()
                || ships[3].isPassenger()
                || ships[4].isPassenger()
                || ships[2].getLegacyRiderType() != 0
                || ships[3].getLegacyRidingState() != 0
                || ships[4].getLegacyRidingState() != 0) {
            helper.fail("Ikazuchi damage should cancel the Akatsuki six-destroyer formation");
            return;
        }

        // Inazuma has the same Akatsuki rider-side damage override.
        if (!ships[6].startRiding(ships[5], true)
                || !ships[6].hurt(ships[6].damageSources().generic(), 1.0F)
                || !ships[5].getPassengers().isEmpty()
                || ships[6].isPassenger()
                || ships[5].getLegacyRiderType() != 0
                || ships[6].getLegacyRidingState() != 0) {
            helper.fail("Inazuma damage should cancel the Akatsuki six-destroyer formation");
            return;
        }

        // Ikazuchi damage from the Raiden rider side clears both flags.
        if (!ships[8].startRiding(ships[7], true)) {
            helper.fail("Ikazuchi should be able to mount Inazuma for the Raiden damage path");
            return;
        }
        ships[7].setLegacyRaiden(true);
        ships[7].setLegacyRidingState(2);
        ships[8].setLegacyRaiden(true);
        ships[8].setLegacyRidingState(2);
        if (!ships[8].hurt(ships[8].damageSources().generic(), 1.0F)
                || ships[8].isPassenger()
                || ships[7].isLegacyRaiden()
                || ships[8].isLegacyRaiden()
                || ships[7].getLegacyRidingState() != 0
                || ships[8].getLegacyRidingState() != 0) {
            helper.fail("Ikazuchi damage should cancel both sides of the Raiden pair");
            return;
        }

        // Inazuma damage from the Raiden host side also clears its Ikazuchi rider.
        if (!ships[10].startRiding(ships[9], true)) {
            helper.fail("Ikazuchi should be able to mount the second Inazuma for the host damage path");
            return;
        }
        ships[9].setLegacyRaiden(true);
        ships[9].setLegacyRidingState(2);
        ships[10].setLegacyRaiden(true);
        ships[10].setLegacyRidingState(2);
        if (!ships[9].hurt(ships[9].damageSources().generic(), 1.0F)
                || !ships[9].getPassengers().isEmpty()
                || ships[10].isPassenger()
                || ships[9].isLegacyRaiden()
                || ships[10].isLegacyRaiden()
                || ships[9].getLegacyRidingState() != 0
                || ships[10].getLegacyRidingState() != 0) {
            helper.fail("Inazuma damage should cancel both sides of the Raiden pair");
            return;
        }

        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void sixDestroyerMountedOffsetsMatchLegacyClasses(GameTestHelper helper) {
        int[] friendlyEggMetas = {53, 54, 55, 56};
        double[] standingFactors = {0.64D, 0.64D, 0.64D, 0.64D};
        double[] sittingFactors = {0.26D, 0.26D, 0.23D, 0.44D};
        double[] boredFactors = {-0.07D, -0.07D, 0.23D, 0.23D};

        for (int index = 0; index < friendlyEggMetas.length; index++) {
            LegacyShipEntity ship = ModEntityTypes.LEGACY_SHIP.get().create(helper.getLevel());
            if (ship == null) {
                helper.fail("six destroyer variants should be creatable for mounted offset tests");
                return;
            }

            ship.setVariantEggMeta(friendlyEggMetas[index]);
            ship.setEmotion(0);
            double height = ship.getBbHeight();
            if (Math.abs(ship.getPassengersRidingOffset() - height * standingFactors[index]) > 1.0E-6D) {
                helper.fail("friendly six destroyer standing offset should preserve legacy factor for egg "
                        + friendlyEggMetas[index]);
                return;
            }

            ship.setOrderedToSit(true);
            if (Math.abs(ship.getPassengersRidingOffset() - height * sittingFactors[index]) > 1.0E-6D) {
                helper.fail("friendly six destroyer sitting offset should preserve legacy factor for egg "
                        + friendlyEggMetas[index]);
                return;
            }

            ship.setEmotion(4);
            if (Math.abs(ship.getPassengersRidingOffset() - height * boredFactors[index]) > 1.0E-6D) {
                helper.fail("friendly six destroyer bored offset should preserve legacy factor for egg "
                        + friendlyEggMetas[index]);
                return;
            }
            ship.discard();
        }

        for (int hostileEggMeta : new int[]{2055, 2056}) {
            LegacyShipEntity ship = ModEntityTypes.LEGACY_SHIP.get().create(helper.getLevel());
            if (ship == null) {
                helper.fail("hostile six destroyer variants should be creatable for mounted offset tests");
                return;
            }
            ship.setVariantEggMeta(hostileEggMeta);
            if (Math.abs(ship.getPassengersRidingOffset() - ship.getBbHeight() * 0.47D) > 1.0E-6D) {
                helper.fail("hostile Ikazuchi/Inazuma offset should preserve the legacy mob factor for egg "
                        + hostileEggMeta);
                return;
            }
            ship.discard();
        }

        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void legacyPassengerRidingOffsetsMatchLegacyOverrides(GameTestHelper helper) {
        int[] eggMetas = {12, 33, 74, 53};
        double[] expectedOffsets = {0.3D, 0.25D, 0.25D, 0.0D};
        for (int index = 0; index < eggMetas.length; index++) {
            LegacyShipEntity ship = ModEntityTypes.LEGACY_SHIP.get().create(helper.getLevel());
            if (ship == null) {
                helper.fail("legacy ship should be creatable for passenger offset tests");
                return;
            }
            ship.setVariantEggMeta(eggMetas[index]);
            if (Math.abs(ship.getMyRidingOffset() - expectedOffsets[index]) > 1.0E-6D) {
                helper.fail("legacy getYOffset override should map to getMyRidingOffset for egg "
                        + eggMetas[index]);
                return;
            }
            ship.discard();
        }

        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void sixDestroyerRidersUseLegacyPassengerPositions(GameTestHelper helper) {
        LegacyShipEntity akatsuki = ModEntityTypes.LEGACY_SHIP.get().create(helper.getLevel());
        LegacyShipEntity hibiki = ModEntityTypes.LEGACY_SHIP.get().create(helper.getLevel());
        LegacyShipEntity inazuma = ModEntityTypes.LEGACY_SHIP.get().create(helper.getLevel());
        LegacyShipEntity ikazuchi = ModEntityTypes.LEGACY_SHIP.get().create(helper.getLevel());
        LegacyShipEntity raidenHost = ModEntityTypes.LEGACY_SHIP.get().create(helper.getLevel());
        LegacyShipEntity raidenRider = ModEntityTypes.LEGACY_SHIP.get().create(helper.getLevel());
        if (akatsuki == null || hibiki == null || inazuma == null || ikazuchi == null
                || raidenHost == null || raidenRider == null) {
            helper.fail("six destroyer variants should be creatable for passenger position tests");
            return;
        }

        LegacyShipEntity[] ships = {akatsuki, hibiki, inazuma, ikazuchi, raidenHost, raidenRider};
        int[] eggMetas = {53, 54, 56, 55, 56, 55};
        BlockPos base = helper.absolutePos(new BlockPos(1, 32, 1));
        double hostX = base.getX() + 0.5D;
        double hostY = base.getY();
        double hostZ = base.getZ() + 0.5D;
        for (LegacyShipEntity ship : ships) {
            ship.setNoAi(true);
            ship.setNoGravity(true);
            ship.setPos(hostX, hostY, hostZ);
            helper.getLevel().addFreshEntity(ship);
        }
        for (int index = 0; index < ships.length; index++) {
            ships[index].setVariantEggMeta(eggMetas[index]);
        }

        akatsuki.setEmotion(4);
        akatsuki.setYRot(90.0F);
        akatsuki.setYHeadRot(90.0F);
        boolean hibikiMounted = hibiki.startRiding(akatsuki, true);
        boolean inazumaMounted = inazuma.startRiding(akatsuki, true);
        boolean ikazuchiMounted = ikazuchi.startRiding(akatsuki, true);
        if (!hibikiMounted || !inazumaMounted || !ikazuchiMounted) {
            helper.fail("legacy six destroyer riders should mount Akatsuki: results="
                    + hibikiMounted + "/" + inazumaMounted + "/" + ikazuchiMounted
                    + ", passengers=" + akatsuki.getPassengers().size());
            return;
        }

        akatsuki.positionRider(hibiki);
        akatsuki.positionRider(inazuma);
        akatsuki.positionRider(ikazuchi);
        if (inazuma.getEmotion() != 4
                || Math.abs(hibiki.getX() - (hostX + 0.2D)) > 1.0E-6D
                || Math.abs(hibiki.getY() - (hostY + 0.23D)) > 1.0E-6D
                || Math.abs(hibiki.getZ() - hostZ) > 1.0E-6D
                || Math.abs(inazuma.getX() - (hostX + 0.48D)) > 1.0E-6D
                || Math.abs(inazuma.getY() - (hostY + 0.78D)) > 1.0E-6D
                || Math.abs(inazuma.getZ() - hostZ) > 1.0E-6D
                || Math.abs(ikazuchi.getX() - (hostX + 0.68D)) > 1.0E-6D
                || Math.abs(ikazuchi.getY() - (hostY + 1.18D)) > 1.0E-6D
                || Math.abs(ikazuchi.getZ() - hostZ) > 1.0E-6D) {
            helper.fail("Akatsuki riders should use the legacy rotated offsets and emotion-dependent height: "
                    + "hibiki=" + hibiki.position() + ", inazuma=" + inazuma.position()
                    + ", ikazuchi=" + ikazuchi.position()
                    + ", emotions=" + hibiki.getEmotion() + "/" + inazuma.getEmotion() + "/" + ikazuchi.getEmotion());
            return;
        }
        if (Math.abs(hibiki.getYRot() - 90.0F) > 1.0E-6F
                || Math.abs(inazuma.getYRot() - 90.0F) > 1.0E-6F
                || Math.abs(ikazuchi.getYRot() - 90.0F) > 1.0E-6F) {
            helper.fail("six destroyer riders should mirror the legacy host rotation");
            return;
        }

        if (!raidenRider.startRiding(raidenHost, true)) {
            helper.fail("Ikazuchi should be able to mount Inazuma for Raiden position tests");
            return;
        }
        raidenHost.setEmotion(0);
        raidenHost.setYRot(90.0F);
        raidenHost.setYHeadRot(90.0F);
        raidenHost.positionRider(raidenRider);
        if (Math.abs(raidenRider.getX() - (hostX + 0.2D)) > 1.0E-6D
                || Math.abs(raidenRider.getY() - (hostY + 0.23D)) > 1.0E-6D
                || Math.abs(raidenRider.getZ() - hostZ) > 1.0E-6D) {
            helper.fail("Inazuma Raiden rider should use the legacy rotated offset");
            return;
        }

        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void sixDestroyerEyeHeightsMatchLegacyClasses(GameTestHelper helper) {
        for (int eggMeta : new int[]{54, 55, 56}) {
            LegacyShipEntity ship = ModEntityTypes.LEGACY_SHIP.get().create(helper.getLevel());
            if (ship == null) {
                helper.fail("six destroyer variants should be creatable for eye-height tests");
                return;
            }

            ship.setVariantEggMeta(eggMeta);
            if (Math.abs(ship.getEyeHeight() - 1.4F) > 1.0E-6F) {
                helper.fail("friendly Hibiki/Ikazuchi/Inazuma should preserve the legacy 1.4 eye height for egg "
                        + eggMeta);
                return;
            }
            ship.discard();
        }

        for (int eggMeta : new int[]{2054, 2055, 2056}) {
            LegacyShipEntity ship = ModEntityTypes.LEGACY_SHIP.get().create(helper.getLevel());
            if (ship == null) {
                helper.fail("hostile six destroyer variants should be creatable for eye-height tests");
                return;
            }

            ship.setVariantEggMeta(eggMeta);
            if (Math.abs(ship.getEyeHeight() - ship.getBbHeight() * 0.8F) > 1.0E-6F) {
                helper.fail("hostile six destroyer variants should keep the hostile base eye height for egg "
                        + eggMeta);
                return;
            }
            ship.discard();
        }

        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void friendlyIkazuchiAndInazumaKeepLegacyRaidenPairing(GameTestHelper helper) {
        LegacyShipEntity ikazuchi = ModEntityTypes.LEGACY_SHIP.get().create(helper.getLevel());
        LegacyShipEntity inazuma = ModEntityTypes.LEGACY_SHIP.get().create(helper.getLevel());
        LegacyShipEntity lowHealthIkazuchi = ModEntityTypes.LEGACY_SHIP.get().create(helper.getLevel());
        LegacyShipEntity lowHealthInazuma = ModEntityTypes.LEGACY_SHIP.get().create(helper.getLevel());
        if (ikazuchi == null || inazuma == null || lowHealthIkazuchi == null || lowHealthInazuma == null) {
            helper.fail("Raiden destroyer variants should be creatable for pairing tests");
            return;
        }

        UUID ownerUuid = UUID.randomUUID();
        LegacyShipEntity[] ships = {ikazuchi, inazuma, lowHealthIkazuchi, lowHealthInazuma};
        int[] eggMetas = {55, 56, 55, 56};
        for (int index = 0; index < ships.length; index++) {
            LegacyShipEntity ship = ships[index];
            ship.setVariantEggMeta(eggMetas[index]);
            ship.setOwner(ownerUuid, "RaidenTester", 92);
            ship.setGrudge(100);
            ship.setNoAi(true);
            ship.setPos(index < 2 ? 2.5D + index : 20.5D + index, 2.0D, 2.5D);
            helper.getLevel().addFreshEntity(ship);
        }
        lowHealthIkazuchi.setHealth(lowHealthIkazuchi.getMaxHealth() * 0.5F);

        ikazuchi.tickCount = 128;
        LegacyShipBehaviorCatalog.tickLegacyRiderBehavior(ikazuchi);
        if (ikazuchi.getVehicle() != inazuma
                || !ikazuchi.isLegacyRaiden()
                || !inazuma.isLegacyRaiden()
                || ikazuchi.getLegacyRidingState() != 2
                || inazuma.getLegacyRidingState() != 2) {
            helper.fail("friendly Ikazuchi should form the legacy Raiden pair with Inazuma");
            return;
        }

        lowHealthIkazuchi.tickCount = 128;
        LegacyShipBehaviorCatalog.tickLegacyRiderBehavior(lowHealthIkazuchi);
        if (lowHealthIkazuchi.isPassenger() || lowHealthIkazuchi.isLegacyRaiden()
                || lowHealthInazuma.isLegacyRaiden()) {
            helper.fail("Raiden pairing should require health strictly above 50 percent");
            return;
        }

        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void u511LightAttackLaunchesLegacyMissile(GameTestHelper helper) {
        LegacyShipEntity ship = ModEntityTypes.LEGACY_SHIP.get().create(helper.getLevel());
        Zombie target = EntityType.ZOMBIE.create(helper.getLevel());
        if (ship == null || target == null) {
            helper.fail("U511 and zombie should be creatable for submarine light attack tests");
            return;
        }

        ship.setVariantEggMeta(40);
        ship.setNoAi(true);
        ship.setPos(1.5D, 2.0D, 1.5D);
        target.setNoAi(true);
        target.setPos(4.5D, 2.0D, 1.5D);
        helper.getLevel().addFreshEntity(ship);
        helper.getLevel().addFreshEntity(target);
        ship.setLightAmmo(20);
        ship.setGrudge(20);
        ship.setMorale(5000);
        ship.setUseAttackMelee(false);
        ship.setUseAttackLight(true);
        ship.setUseAttackHeavy(false);
        ship.setUseAttackAirLight(false);
        ship.setUseAttackAirHeavy(false);

        float healthBefore = target.getHealth();
        if (!ship.performPlayerCompatAttack(target, LegacyShipAttackKind.LIGHT)) {
            helper.fail("U511 light attack should launch with the legacy light ammo path");
            return;
        }

        List<LegacyShipProjectileEntity> projectiles = helper.getLevel().getEntitiesOfClass(
                LegacyShipProjectileEntity.class,
                ship.getBoundingBox().inflate(20.0D),
                projectile -> projectile.getOwner() == ship
                        && projectile.getAttackKind() == LegacyShipAttackKind.LIGHT);
        if (projectiles.size() != 1) {
            helper.fail("U511 light attack should create exactly one missile projectile");
            return;
        }
        LegacyShipProjectileEntity projectile = projectiles.get(0);
        LegacyShipProjectileMoveType expectedMoveType = expectedLegacyArcMissileMove(ship, projectile);
        if (projectile.getProjectileVisual() != LegacyShipProjectileVisual.MISSILE
                || projectile.getMoveType() != expectedMoveType
                || projectile.getIntendedTargetId() != target.getId()
                || projectile.getImpactRadius() != 3.5D
                || projectile.getKnockbackStrength() != 0.15D) {
            helper.fail("U511 missile mismatch: expectedMove=" + expectedMoveType
                    + ", actualMove=" + projectile.getMoveType()
                    + ", depth=" + ship.getLegacyLiquidDepth()
                    + ", radius=" + projectile.getImpactRadius()
                    + ", knockback=" + projectile.getKnockbackStrength());
            return;
        }
        if (target.getHealth() != healthBefore) {
            helper.fail("U511 light attack should not deal direct damage before its missile impacts");
            return;
        }
        if (ship.getLightAmmo() != 19 || ship.getGrudge() != 16
                || ship.getMorale() != 4996 || ship.getCompatAttackCooldown(LegacyShipAttackKind.LIGHT) <= 0) {
            helper.fail("U511 missile launch should consume the legacy light attack resources exactly once");
            return;
        }

        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void registeredAttackChannelsMirrorLegacySources(GameTestHelper helper) {
        int[] friendlyEggMetas = {
                2, 3, 4, 5, 11, 12, 14, 15, 16, 17, 18, 19, 20, 21,
                22, 23, 28, 29, 30, 31, 32, 33, 35, 38, 39, 40, 41,
                46, 48, 49, 50, 51, 53, 54, 55, 56, 58, 59, 60, 61,
                62, 63, 64, 65, 74
        };
        java.util.Set<Integer> noLight = java.util.Set.of(14, 18, 22, 49, 50);
        java.util.Set<Integer> noHeavy = java.util.Set.of(14, 16, 18, 22, 35, 49, 50);
        java.util.Set<Integer> air = java.util.Set.of(14, 17, 22, 23, 30, 31, 32, 33, 35, 49, 50);

        if (ShipEntitySpecs.values().size() != 64) {
            helper.fail("attack-channel audit expects all 64 registered variants");
            return;
        }

        for (int eggMeta : friendlyEggMetas) {
            ShipEntitySpec spec = ShipEntitySpecs.getByEggMeta(eggMeta);
            LegacyShipAttackProfile profile = LegacyShipBehaviorCatalog.attackProfile(spec);
            boolean expectedAir = air.contains(eggMeta);
            if (profile.light() == noLight.contains(eggMeta)
                    || profile.heavy() == noHeavy.contains(eggMeta)
                    || profile.airLight() != expectedAir
                    || profile.airHeavy() != expectedAir) {
                helper.fail("legacy attack channel mismatch for egg=" + eggMeta
                        + "; light=" + profile.light()
                        + "; heavy=" + profile.heavy()
                        + "; airLight=" + profile.airLight()
                        + "; airHeavy=" + profile.airHeavy());
                return;
            }

            for (LegacyShipAttackKind attackKind : LegacyShipAttackKind.values()) {
                boolean hookUsesChannel = LegacyShipBehaviorCatalog.shouldUseCombatHook(spec, attackKind);
                boolean channelEnabled = switch (attackKind) {
                    case MELEE -> profile.melee();
                    case LIGHT -> profile.light();
                    case HEAVY -> profile.heavy();
                    case AIR_LIGHT -> profile.airLight();
                    case AIR_HEAVY -> profile.airHeavy();
                };
                if (hookUsesChannel && !channelEnabled) {
                    helper.fail("special hook selected a disabled attack channel for egg="
                            + eggMeta + "; attack=" + attackKind);
                    return;
                }
            }
        }

        for (ShipEntitySpec spec : ShipEntitySpecs.values()) {
            if (!spec.hostile() || spec.eggMeta() < 2000) {
                continue;
            }
            ShipEntitySpec friendly = ShipEntitySpecs.findByEggMeta(spec.eggMeta() - 2000);
            if (friendly == null) {
                helper.fail("hostile attack profile has no registered friendly counterpart: egg=" + spec.eggMeta());
                return;
            }
            LegacyShipAttackProfile profile = LegacyShipBehaviorCatalog.attackProfile(spec);
            boolean expectedAir = air.contains(friendly.eggMeta());
            if (!profile.light() || !profile.heavy()
                    || profile.airLight() != expectedAir
                    || profile.airHeavy() != expectedAir) {
                helper.fail("hostile override attack channel mismatch for egg=" + spec.eggMeta()
                        + "; light=" + profile.light()
                        + "; heavy=" + profile.heavy()
                        + "; air=" + profile.airLight() + "/" + profile.airHeavy());
                return;
            }
        }

        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void singlePlayerAiPriorityContractMatchesLegacyBoundaries(GameTestHelper helper) {
        List<LegacyShipBehaviorCatalog.AiPriority> expected = List.of(
                LegacyShipBehaviorCatalog.AiPriority.DEATH_STOP_SIT,
                LegacyShipBehaviorCatalog.AiPriority.EXPLICIT_COMMAND,
                LegacyShipBehaviorCatalog.AiPriority.ACTIVE_COMBAT_TARGET,
                LegacyShipBehaviorCatalog.AiPriority.AUTO_SUPPLY,
                LegacyShipBehaviorCatalog.AiPriority.PICKUP,
                LegacyShipBehaviorCatalog.AiPriority.IDLE_FOLLOW);
        if (!expected.equals(LegacyShipBehaviorCatalog.singlePlayerAiPriorityOrder())) {
            helper.fail("single-player AI priority must retain the documented legacy order");
            return;
        }

        Player player = helper.makeMockPlayer();
        LegacyShipEntity ship = createOwnedShip(helper, player, 58, 2.5D, 2.0D, 2.5D);
        Zombie target = new Zombie(EntityType.ZOMBIE, helper.getLevel());
        if (ship == null) {
            helper.fail("legacy ship should be creatable for the AI priority contract");
            return;
        }

        target.setNoAi(true);
        target.setPos(4.0D, 2.0D, 2.5D);
        helper.getLevel().addFreshEntity(target);
        ship.setOrderedToSit(true);
        if (ship.canExecuteCombatGoal()) {
            helper.fail("stop/sit must suppress active combat goals");
            return;
        }

        ship.setOrderedToSit(false);
        ship.commandMoveTo(helper.absolutePos(new BlockPos(8, 2, 8)),
                helper.getLevel().dimension().location().toString());
        if (ship.canExecuteCombatGoal() || ship.canAutoTargetEnemyMob(target)) {
            helper.fail("explicit commands must suppress autonomous combat targeting");
            return;
        }

        ship.clearCommandState();
        if (!ship.canExecuteCombatGoal() || !ship.canAutoTargetEnemyMob(target)) {
            helper.fail("autonomous combat must resume after the explicit command is cleared");
            return;
        }

        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void hostileRangeUsesLegacyFollowFallbackForLowAttackRange(GameTestHelper helper) {
        LegacyShipEntity ship = ModEntityTypes.LEGACY_SHIP.get().create(helper.getLevel());
        if (ship == null) {
            helper.fail("legacy hostile ships should be creatable for the low attack range fallback test");
            return;
        }

        ship.setVariantEggMeta(2058);
        ship.initializeHostileRuntime(false, false, false);
        ship.setNoAi(true);
        ship.setPos(helper.absolutePos(new BlockPos(2, 2, 2)).getX() + 0.5D, 2.0D,
                helper.absolutePos(new BlockPos(2, 2, 2)).getZ() + 0.5D);
        helper.getLevel().addFreshEntity(ship);
        ship.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 200, 0, false, false, false));
        ship.tick();
        int attackRange = (int) ship.getCompatAttackRange();
        if (attackRange >= 2 || ship.getHostileTargetSearchRange() != 14.0D) {
            helper.fail("low hostile attack ranges should use the legacy FollowMax + 2 effective fallback"
                    + ": attackRange=" + attackRange
                    + ", followRange=" + ship.getAiFollowRange()
                    + ", searchRange=" + ship.getHostileTargetSearchRange());
            return;
        }

        ship.discard();
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void hostileLightCruiserSkillReplacementKeepsLegacySearchRadius(GameTestHelper helper) {
        int[] hostileEggMetas = {2058, 2059};
        double[] legacySearchRanges = {8.0D, 13.0D};
        for (int index = 0; index < hostileEggMetas.length; index++) {
            LegacyShipEntity ship = ModEntityTypes.LEGACY_SHIP.get().create(helper.getLevel());
            LegacyShipEntity candidate = ModEntityTypes.LEGACY_SHIP.get().create(helper.getLevel());
            Zombie staleTarget = EntityType.ZOMBIE.create(helper.getLevel());
            if (ship == null || candidate == null || staleTarget == null) {
                helper.fail("hostile light cruiser skill target fixtures should be creatable");
                return;
            }

            ship.setVariantEggMeta(hostileEggMetas[index]);
            ship.initializeHostileRuntime(false, false, false);
            ship.setNoAi(true);
            ship.setNoGravity(true);
            BlockPos shipPos = helper.absolutePos(new BlockPos(2 + index * 8, 2, 2));
            ship.setPos(shipPos.getX() + 0.5D, shipPos.getY(), shipPos.getZ() + 0.5D);

            double attackRange = ship.getCompatAttackRange();
            double searchRange = legacySearchRanges[index];
            double candidateDistance = searchRange - 1.0D;

            candidate.setVariantEggMeta(58);
            candidate.setGrudge(100);
            candidate.setNoAi(true);
            candidate.setNoGravity(true);
            candidate.setPos(ship.getX() + candidateDistance, ship.getY(), ship.getZ());

            staleTarget.setNoAi(true);
            staleTarget.setPos(ship.getX() + Math.max(attackRange, searchRange) + 4.0D, ship.getY(), ship.getZ());
            helper.getLevel().addFreshEntity(ship);
            helper.getLevel().addFreshEntity(candidate);

            ship.setTarget(staleTarget);
            ship.setDisplayPhase(1);
            ship.setLegacySkillRemaining(2);
            ship.setLegacySkillTimer(10);
            LegacyShipBehaviorCatalog.tickLegacyPerShipBehavior(ship);
            if (ship.getTarget() != candidate) {
                helper.fail("hostile light cruiser skill should replace a stale target within the legacy search radius"
                        + ": egg=" + hostileEggMetas[index]
                        + ", target=" + ship.getTarget()
                        + ", candidateDistance=" + candidateDistance
                        + ", searchRange=" + searchRange);
                return;
            }

            ship.discard();
            candidate.discard();
        }

        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void hostileBossDropsInvalidTargetBeforeNavigation(GameTestHelper helper) {
        Player creativePlayer = helper.makeMockPlayer();
        LegacyShipEntity creativeBoss = ModEntityTypes.LEGACY_SHIP.get().create(helper.getLevel());
        LegacyShipEntity distantBoss = ModEntityTypes.LEGACY_SHIP.get().create(helper.getLevel());
        Player distantPlayer = helper.makeMockSurvivalPlayer();
        if (creativeBoss == null || distantBoss == null) {
            helper.fail("hostile bosses should be creatable for stale target navigation tests");
            return;
        }
        distantPlayer.setPos(1000.0D, 2.0D, 1000.0D);

        creativeBoss.setVariantEggMeta(2048);
        creativeBoss.initializeHostileRuntime(false, true, true);
        creativeBoss.setNoAi(true);
        creativeBoss.setPos(5.5D, 2.0D, 5.5D);
        helper.getLevel().addFreshEntity(creativeBoss);
        creativePlayer.setPos(7.5D, 2.0D, 5.5D);
        creativePlayer.getAbilities().invulnerable = true;
        creativePlayer.getAbilities().instabuild = true;
        creativeBoss.setTarget(creativePlayer);
        if (!setSyntheticGuardPath(creativeBoss, new BlockPos(5, 2, 5), new BlockPos(12, 2, 5))) {
            helper.fail("creative target navigation should be active before the hostile target gate runs");
            return;
        }
        creativeBoss.tick();
        if (creativeBoss.getTarget() == creativePlayer || !creativeBoss.getNavigation().isDone()) {
            helper.fail("hostile bosses should clear creative targets and stop navigation before AI movement"
                    + ": hostile=" + creativeBoss.isHostileVariant()
                    + ", canEngage=" + creativeBoss.canEngage(creativePlayer)
                    + ", target=" + creativeBoss.getTarget()
                    + ", navigationDone=" + creativeBoss.getNavigation().isDone());
            return;
        }

        distantBoss.setVariantEggMeta(2048);
        distantBoss.initializeHostileRuntime(false, true, true);
        distantBoss.setNoAi(true);
        distantBoss.setPos(5.5D, 2.0D, 5.5D);
        helper.getLevel().addFreshEntity(distantBoss);
        double range = distantBoss.getHostileTargetSearchRange();
        distantPlayer.setPos(5.5D + range + 2.0D, 2.0D, 5.5D);
        distantBoss.setTarget(distantPlayer);
        if (!setSyntheticGuardPath(distantBoss, new BlockPos(5, 2, 5), new BlockPos(12, 2, 5))) {
            helper.fail("out-of-range target navigation should be active before the hostile target gate runs");
            return;
        }
        distantBoss.tick();
        if (distantBoss.getTarget() == distantPlayer || !distantBoss.getNavigation().isDone()) {
            helper.fail("hostile bosses should clear out-of-range targets and stop navigation before AI movement");
            return;
        }

        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void fiveHimeBossesKeepLegacyInvisibleTargetBoundary(GameTestHelper helper) {
        Player player = helper.makeMockSurvivalPlayer();
        LegacyShipEntity friendlyTarget = ModEntityTypes.LEGACY_SHIP.get().create(helper.getLevel());
        LegacyShipEntity hostileTarget = ModEntityTypes.LEGACY_SHIP.get().create(helper.getLevel());
        if (friendlyTarget == null || hostileTarget == null) {
            helper.fail("legacy hime target boundary entities should be creatable");
            return;
        }

        friendlyTarget.setVariantEggMeta(2);
        hostileTarget.setVariantEggMeta(2049);
        friendlyTarget.setPos(5.0D, 2.0D, 5.0D);
        hostileTarget.setPos(5.0D, 2.0D, 5.0D);
        friendlyTarget.setInvisible(true);
        player.setPos(7.0D, 2.0D, 5.0D);

        for (int eggMeta : new int[] {2048, 2049, 2050, 2062, 2063}) {
            LegacyShipEntity boss = ModEntityTypes.LEGACY_SHIP.get().create(helper.getLevel());
            if (boss == null) {
                helper.fail("legacy hime boss should be creatable: egg=" + eggMeta);
                return;
            }

            boss.setVariantEggMeta(eggMeta);
            boss.initializeHostileRuntime(false, true, true);
            boss.setPos(5.0D, 2.0D, 5.0D);
            if (boss.canEngage(friendlyTarget)) {
                helper.fail("hostile hime bosses should ignore invisible non-player targets: egg=" + eggMeta);
                return;
            }
            friendlyTarget.setInvisible(false);
            if (!boss.canEngage(friendlyTarget)) {
                helper.fail("hostile hime bosses should engage visible friendly ships: egg=" + eggMeta);
                return;
            }
            if (boss.canEngage(hostileTarget)) {
                helper.fail("hostile hime bosses should not target hostile ships: egg=" + eggMeta);
                return;
            }
            if (!boss.canEngage(player)) {
                helper.fail("hostile hime bosses should engage nearby vulnerable players: egg=" + eggMeta);
                return;
            }
            player.setPos(5.0D + boss.getHostileTargetSearchRange() + 1.0D, 2.0D, 5.0D);
            if (boss.canEngage(player)) {
                helper.fail("hostile hime bosses should drop players outside the legacy range: egg=" + eggMeta);
                return;
            }
            player.setPos(7.0D, 2.0D, 5.0D);
            friendlyTarget.setInvisible(true);
        }

        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void hostileCombatReactDoesNotAllowPlayerActionbar(GameTestHelper helper) {
        Player player = helper.makeMockPlayer();
        LegacyShipEntity boss = ModEntityTypes.LEGACY_SHIP.get().create(helper.getLevel());
        if (boss == null) {
            helper.fail("legacy ship entities should be creatable for combat feedback tests");
            return;
        }

        boss.setVariantEggMeta(2049);
        boss.initializeHostileRuntime(false, true, true);
        for (CombatReactType reactType : new CombatReactType[] {CombatReactType.LAUNCH, CombatReactType.HIT}) {
            ClientboundCombatReactPacket packet = new ClientboundCombatReactPacket(
                    reactType, boss.getId(), player.getId(),
                    LegacyShipAttackKind.AIR_HEAVY, LegacyShipProjectileVisual.AIRPLANE);
            if (packet.allowsPlayerActionbar(player, boss)) {
                helper.fail("hostile ship aircraft reactions should not show an actionbar message to the player");
                return;
            }
            if (packet.allowsPlayerActionbar(player, null)) {
                helper.fail("combat reactions without a resolved attacker should not show an actionbar message");
                return;
            }
        }

        LegacyShipRensouhouEntity hostileSummon = LegacyShipRensouhouEntity.create(helper.getLevel(), boss, player);
        if (hostileSummon == null) {
            helper.fail("hostile ship summons should be creatable for combat feedback tests");
            return;
        }
        ClientboundCombatReactPacket summonPacket = new ClientboundCombatReactPacket(
                CombatReactType.HIT, hostileSummon.getId(), player.getId(),
                LegacyShipAttackKind.LIGHT, LegacyShipProjectileVisual.NONE);
        if (summonPacket.allowsPlayerActionbar(player, hostileSummon)) {
            helper.fail("hostile ship summon reactions should not show an actionbar message to the player");
            return;
        }

        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void legacyInvisibleDodgeUsesShipSpecificDistanceRule(GameTestHelper helper) {
        LegacyShipEntity submarine = ModEntityTypes.LEGACY_SHIP.get().create(helper.getLevel());
        LegacyShipEntity ordinaryShip = ModEntityTypes.LEGACY_SHIP.get().create(helper.getLevel());
        Zombie attacker = EntityType.ZOMBIE.create(helper.getLevel());
        if (submarine == null || ordinaryShip == null || attacker == null) {
            helper.fail("legacy ship and attacker should be creatable for invisible dodge tests");
            return;
        }

        submarine.setVariantEggMeta(40);
        submarine.setShipLevel(60);
        submarine.setPos(0.0D, 2.0D, 0.0D);
        attacker.setPos(0.0D, 2.0D, 0.0D);
        float invisibleLevel = LegacyShipBehaviorCatalog.invisibleLevelFor(submarine.getSpec());
        float dodgeDistance = (1F - submarine.getShipLevel() * (1F + invisibleLevel) / 300F) * 64F;
        float nearChance = LegacyShipCombatHelper.calculateDodgeChance(submarine, attacker);

        attacker.setPos(dodgeDistance + 1.0D, 2.0D, 0.0D);
        float farChance = LegacyShipCombatHelper.calculateDodgeChance(submarine, attacker);
        if (Math.abs(nearChance - submarine.getLegacyStats().dodge()) > 0.0001F
                || Math.abs(farChance - (nearChance + invisibleLevel)) > 0.0001F) {
            helper.fail("submarine dodge should add its legacy invisible level only beyond the dynamic distance");
            return;
        }

        ordinaryShip.setVariantEggMeta(2058);
        ordinaryShip.setShipLevel(60);
        ordinaryShip.setPos(0.0D, 2.0D, 0.0D);
        float ordinaryBaseChance = ordinaryShip.getLegacyStats().dodge();
        float ordinaryFarChance = LegacyShipCombatHelper.calculateDodgeChance(ordinaryShip, attacker);
        if (Math.abs(ordinaryFarChance - ordinaryBaseChance) > 0.0001F) {
            helper.fail("non-IShipInvisible ships should not receive the submarine distance dodge bonus");
            return;
        }

        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void shipPickupGoalYieldsToOrdersCombatAndIndependentAiFlags(GameTestHelper helper) {
        if (!pickupGoalCanUseWith(helper, 0, ship -> {
        })) {
            helper.fail("pickup goal should still activate for an idle friendly ship with auto-supply enabled");
            return;
        }
        if (pickupGoalCanUseWith(helper, 1, ship -> ship.commandMoveTo(new BlockPos(4, 2, 4),
                helper.getLevel().dimension().location().toString()))) {
            helper.fail("pickup goal must not interrupt an active move or route command");
            return;
        }
        if (pickupGoalCanUseWith(helper, 2, ship -> ship.commandGuardEntity(UUID.randomUUID()))) {
            helper.fail("pickup goal must not interrupt an active guard command");
            return;
        }
        if (pickupGoalCanUseWith(helper, 3, ship -> ship.setOrderedToSit(true))) {
            helper.fail("pickup goal must not run while the ship is standing by");
            return;
        }
        if (!pickupGoalCanUseWith(helper, 4, ship -> ship.setAiAutoSupply(false))) {
            helper.fail("pickup goal should remain independent from the combat-ration AI flag");
            return;
        }
        if (pickupGoalCanUseWith(helper, 5, ship -> ship.setAiPickItem(false))) {
            helper.fail("pickup goal must respect the legacy PickItem AI flag");
            return;
        }
        if (pickupGoalCanUseWith(helper, 6, ship -> {
            Zombie target = EntityType.ZOMBIE.create(helper.getLevel());
            if (target != null) {
                target.setPos(ship.getX() + 2.0D, ship.getY(), ship.getZ());
                helper.getLevel().addFreshEntity(target);
                ship.setTarget(target);
            }
        })) {
            helper.fail("pickup goal must not interrupt active combat targeting");
            return;
        }

        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void ship04HimeLegacySourcesAndProjectileFamilies(GameTestHelper helper) {
        int[] eggMetas = {23, 28, 29, 30, 31};
        String[] modelStems = {
                "ModelAirfieldHime", "ModelBattleshipHime", "ModelDestroyerHime",
                "ModelHarbourHime", "ModelIsolatedHime"
        };
        int[] textureWidths = {128, 128, 128, 128, 256};
        int[] textureHeights = {128, 128, 128, 128, 128};
        LegacyModelSourceParser parser = new LegacyModelSourceParser();

        for (int index = 0; index < eggMetas.length; index++) {
            ShipEntitySpec spec = ShipEntitySpecs.getByEggMeta(eggMetas[index]);
            LegacyShipRenderCatalog.RenderEntry renderEntry = LegacyShipRenderCatalog.forSpec(spec);
            if (!modelStems[index].equals(renderEntry.modelSourceStem())) {
                helper.fail("Hime render lookup should preserve the exact legacy model source: egg=" + eggMetas[index]);
                return;
            }

            String source = readTextResource(renderEntry.modelSourceLocation());
            if (source == null) {
                helper.fail("Hime legacy model source should be packaged: egg=" + eggMetas[index]);
                return;
            }

            LegacyModelDefinition definition;
            try {
                definition = parser.parse(renderEntry.modelSourceStem(), source);
            } catch (Exception exception) {
                helper.fail("Hime legacy model source should parse without fallback: egg=" + eggMetas[index]);
                return;
            }

            if (definition.textureWidth() != textureWidths[index]
                    || definition.textureHeight() != textureHeights[index]
                    || definition.rootParts().isEmpty()
                    || definition.parts().isEmpty()) {
                helper.fail("Hime legacy model metadata or parsed parts changed: egg=" + eggMetas[index]);
                return;
            }
        }

        LegacyShipAttackProfile airfieldProfile = LegacyShipBehaviorCatalog.attackProfile(
                ShipEntitySpecs.getByEggMeta(23));
        LegacyShipAttackProfile harbourProfile = LegacyShipBehaviorCatalog.attackProfile(
                ShipEntitySpecs.getByEggMeta(30));
        LegacyShipAttackProfile battleshipProfile = LegacyShipBehaviorCatalog.attackProfile(
                ShipEntitySpecs.getByEggMeta(28));
        LegacyShipAttackProfile isolatedProfile = LegacyShipBehaviorCatalog.attackProfile(
                ShipEntitySpecs.getByEggMeta(31));
        if (!airfieldProfile.hasAirAttack()
                || airfieldProfile.projectileProfile(LegacyShipAttackKind.AIR_LIGHT).visual()
                        != LegacyShipProjectileVisual.AIRPLANE
                || airfieldProfile.projectileProfile(LegacyShipAttackKind.AIR_HEAVY).visual()
                        != LegacyShipProjectileVisual.BOMB
                || Math.abs(LegacyShipBehaviorCatalog.aircraftLaunchHeightFactorFor(
                        ShipEntitySpecs.getByEggMeta(23), LegacyShipAttackKind.AIR_LIGHT) - 0.7F) > 0.001F
                || Math.abs(LegacyShipBehaviorCatalog.aircraftLaunchHeightFactorFor(
                        ShipEntitySpecs.getByEggMeta(30), LegacyShipAttackKind.AIR_HEAVY) - 0.7F) > 0.001F) {
            helper.fail("Airfield and Harbour Hime aircraft should preserve the legacy CV launch family and height");
            return;
        }
        if (battleshipProfile.projectileProfile(LegacyShipAttackKind.HEAVY).visual()
                != LegacyShipProjectileVisual.MISSILE
                || !battleshipProfile.projectileProfile(LegacyShipAttackKind.HEAVY).isPresent()) {
            helper.fail("Battleship Hime should preserve the legacy AbyssMissile projectile family");
            return;
        }
        if (harbourProfile.projectileProfile(LegacyShipAttackKind.HEAVY).moveType()
                != LegacyShipProjectileMoveType.DIRECT
                || harbourProfile.projectileProfile(LegacyShipAttackKind.HEAVY).baseSpeed() != 3.0D
                || harbourProfile.projectileProfile(LegacyShipAttackKind.HEAVY).trailParticleType()
                        != GameplayParticleType.LEGACY_RAILGUN) {
            helper.fail("Harbour Hime should preserve the legacy direct railgun and type-2 particle family");
            return;
        }
        if (isolatedProfile.projectileProfile(LegacyShipAttackKind.HEAVY).visual()
                != LegacyShipProjectileVisual.MISSILE) {
            helper.fail("Isolated Hime should preserve the legacy missile-rendered black-hole attack");
            return;
        }

        LegacyShipEntity battleship = ModEntityTypes.LEGACY_SHIP.get().create(helper.getLevel());
        LegacyShipEntity isolated = ModEntityTypes.LEGACY_SHIP.get().create(helper.getLevel());
        if (battleship == null || isolated == null) {
            helper.fail("selected Hime specs should create modern legacy ship entities");
            return;
        }
        battleship.applySpawnSpec(ShipEntitySpecs.getByEggMeta(28), null);
        isolated.applySpawnSpec(ShipEntitySpecs.getByEggMeta(31), null);
        if (!LegacyShipBehaviorCatalog.usesClusterHeavyMissile(battleship, LegacyShipAttackKind.HEAVY)
                || !LegacyShipBehaviorCatalog.usesBlackHoleHeavyMissile(isolated, LegacyShipAttackKind.HEAVY)) {
            helper.fail("Battleship Hime and Isolated Hime should retain their legacy heavy payload markers");
            return;
        }

        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void ship06SubmarineYamatoKagaLegacyModelsKeepExactSourceAndStateRules(GameTestHelper helper) {
        int[] eggMetas = {40, 41, 46, 74, 48, 49};
        String[] modelStems = {
                "ModelSubmU511", "ModelSubmRo500", "ModelSubmHime", "ModelSSNH",
                "ModelBattleshipYamato", "ModelCarrierKaga"
        };
        LegacyModelSourceParser parser = new LegacyModelSourceParser();

        for (int index = 0; index < eggMetas.length; index++) {
            ShipEntitySpec spec = ShipEntitySpecs.getByEggMeta(eggMetas[index]);
            LegacyShipRenderCatalog.RenderEntry renderEntry = LegacyShipRenderCatalog.forSpec(spec);
            if (!modelStems[index].equals(renderEntry.modelSourceStem())
                    || !resourceExists(renderEntry.modelSourceLocation())
                    || !resourceExists(renderEntry.textureLocation())) {
                helper.fail("SHIP-06 selected ship should keep its exact legacy model source and texture: egg="
                        + eggMetas[index]);
                return;
            }

            try {
                LegacyModelDefinition definition = parser.parse(renderEntry.modelSourceStem(),
                        readTextResource(renderEntry.modelSourceLocation()));
                if (definition.rootParts().isEmpty() || definition.parts().isEmpty()
                        || definition.parts().values().stream().noneMatch(part -> !part.cubes().isEmpty())) {
                    helper.fail("SHIP-06 selected model source should bake to a non-empty model: "
                            + renderEntry.modelSourceStem());
                    return;
                }
            } catch (Exception exception) {
                helper.fail("SHIP-06 selected model source should parse without fallback: "
                        + renderEntry.modelSourceStem() + " error=" + exception.getClass().getSimpleName());
                return;
            }
        }

        if (LegacyShipModelStateCatalog.isPartVisible("ModelSubmU511", "EquipBase", 0)
                || !LegacyShipModelStateCatalog.isPartVisible("ModelSubmU511", "EquipBase", 1 << 0)
                || LegacyShipModelStateCatalog.isPartVisible("ModelSubmU511", "Hat01", 0)
                || !LegacyShipModelStateCatalog.isPartVisible("ModelSubmU511", "Hat01", 1 << 1)
                || LegacyShipModelStateCatalog.isPartVisible("ModelSubmU511", "Pipe", 0)
                || !LegacyShipModelStateCatalog.isPartVisible("ModelSubmU511", "Pipe", 1 << 2)
                || LegacyShipModelStateCatalog.isPartVisible("ModelSubmRo500", "EquipBase1", 0)
                || !LegacyShipModelStateCatalog.isPartVisible("ModelSubmRo500", "EquipBase1", 1 << 0)
                || LegacyShipModelStateCatalog.isPartVisible("ModelSubmRo500", "EquipBase2", 0)
                || !LegacyShipModelStateCatalog.isPartVisible("ModelSubmRo500", "EquipBase2", 1 << 1)
                || LegacyShipModelStateCatalog.isPartVisible("ModelSubmRo500", "FlowerBase", 0)
                || !LegacyShipModelStateCatalog.isPartVisible("ModelSubmRo500", "FlowerBase", 1 << 2)
                || LegacyShipModelStateCatalog.isPartVisible("ModelSubmHime", "Collar01", 0)
                || !LegacyShipModelStateCatalog.isPartVisible("ModelSubmHime", "Collar01", 1 << 1)
                || LegacyShipModelStateCatalog.isPartVisible("ModelSSNH", "EquipHandRing", 0)
                || !LegacyShipModelStateCatalog.isPartVisible("ModelSSNH", "EquipHandRing", 1 << 0)
                || LegacyShipModelStateCatalog.isPartVisible("ModelSSNH", "RingBase", 0)
                || !LegacyShipModelStateCatalog.isPartVisible("ModelSSNH", "RingBase", 1 << 1)
                || LegacyShipModelStateCatalog.isPartVisible("ModelSSNH", "EquipTBase", 0)
                || !LegacyShipModelStateCatalog.isPartVisible("ModelSSNH", "EquipTBase", 1 << 2)
                || LegacyShipModelStateCatalog.isPartVisible("ModelSSNH", "EquipTBase_2", 1 << 7)
                || LegacyShipModelStateCatalog.isPartVisible("ModelBattleshipYamato", "EquipBaseBelt", 0)
                || !LegacyShipModelStateCatalog.isPartVisible("ModelBattleshipYamato", "EquipBaseBelt", 1 << 0)
                || LegacyShipModelStateCatalog.isPartVisible("ModelBattleshipYamato", "EquipHeadBase", 0)
                || !LegacyShipModelStateCatalog.isPartVisible("ModelBattleshipYamato", "EquipHeadBase", 1 << 1)
                || LegacyShipModelStateCatalog.isPartVisible("ModelBattleshipYamato", "EquipU01", 0)
                || !LegacyShipModelStateCatalog.isPartVisible("ModelBattleshipYamato", "EquipU01", 1 << 2)
                || LegacyShipModelStateCatalog.isPartVisible("ModelBattleshipYamato", "EquipLegR01", 0)
                || !LegacyShipModelStateCatalog.isPartVisible("ModelBattleshipYamato", "EquipLegR01", 1 << 3)
                || LegacyShipModelStateCatalog.isPartVisible("ModelBattleshipYamato", "EquipLegL01", 0)
                || !LegacyShipModelStateCatalog.isPartVisible("ModelBattleshipYamato", "EquipLegL01", 1 << 3)
                || LegacyShipModelStateCatalog.isPartVisible("ModelCarrierKaga", "EquipE01", 0)
                || !LegacyShipModelStateCatalog.isPartVisible("ModelCarrierKaga", "EquipE01", 1 << 0)
                || LegacyShipModelStateCatalog.isPartVisible("ModelCarrierKaga", "EquipGlove", 0)
                || !LegacyShipModelStateCatalog.isPartVisible("ModelCarrierKaga", "EquipGlove", 1 << 0)
                || LegacyShipModelStateCatalog.isPartVisible("ModelCarrierKaga", "EquipABase", 0)
                || !LegacyShipModelStateCatalog.isPartVisible("ModelCarrierKaga", "EquipABase", 1 << 1)
                || LegacyShipModelStateCatalog.isPartVisible("ModelCarrierKaga", "EquipD01", 0)
                || !LegacyShipModelStateCatalog.isPartVisible("ModelCarrierKaga", "EquipD01", 1 << 2)
                || LegacyShipModelStateCatalog.isPartVisible("ModelCarrierKaga", "EquipC01", 0)
                || !LegacyShipModelStateCatalog.isPartVisible("ModelCarrierKaga", "EquipC01", 1 << 3)
                || LegacyShipModelStateCatalog.isPartVisible("ModelCarrierKaga", "EquipB01", 0)
                || !LegacyShipModelStateCatalog.isPartVisible("ModelCarrierKaga", "EquipB01", 1 << 4)
                || LegacyShipModelStateCatalog.isPartVisible("ModelCarrierKaga", "EquipS01", 0)
                || !LegacyShipModelStateCatalog.isPartVisible("ModelCarrierKaga", "EquipS01", 1 << 5)
                || LegacyShipModelStateCatalog.isPartVisible("ModelCarrierKaga", "Ear01", 0)
                || !LegacyShipModelStateCatalog.isPartVisible("ModelCarrierKaga", "Ear01", 1 << 6)
                || LegacyShipModelStateCatalog.isPartVisible("ModelCarrierKaga", "Ear02", 0)
                || !LegacyShipModelStateCatalog.isPartVisible("ModelCarrierKaga", "Ear02", 1 << 6)
                || LegacyShipModelStateCatalog.isPartVisible("ModelCarrierKaga", "Tail01", 0)
                || !LegacyShipModelStateCatalog.isPartVisible("ModelCarrierKaga", "Tail01", 1 << 6)
                || LegacyShipModelStateCatalog.isPartVisible("ModelCarrierKaga", "EquipSL01", 0)
                || !LegacyShipModelStateCatalog.isPartVisible("ModelCarrierKaga", "EquipSL01", 1 << 7)
                || LegacyShipModelStateCatalog.isPartVisible("ModelCarrierKaga", "EquipSR01", 0)
                || !LegacyShipModelStateCatalog.isPartVisible("ModelCarrierKaga", "EquipSR01", 1 << 7)) {
            helper.fail("SHIP-06 model visibility should preserve the selected legacy showEquip state bits");
            return;
        }

        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void ship06SelectedModelScaleBranchesMatchLegacySource(GameTestHelper helper) {
        String[] modelStems = {"ModelSubmU511", "ModelSubmRo500", "ModelBattleshipYamato", "ModelCarrierKaga"};
        float[][] expected = {
                {0.36F, 0.72F, 1.08F, 1.44F},
                {0.36F, 0.72F, 1.08F, 1.44F},
                {0.5F, 1.0F, 1.5F, 2.0F},
                {0.46F, 0.92F, 1.38F, 1.84F}
        };
        float[][] offsets = {
                {2.86F, 0.66F, -0.06F, -0.45F},
                {2.86F, 0.66F, -0.06F, -0.45F},
                {1.53F, 0.02F, -0.48F, -0.73F},
                {1.81F, 0.16F, -0.37F, -0.63F}
        };

        for (int modelIndex = 0; modelIndex < modelStems.length; modelIndex++) {
            for (int scaleLevel = 0; scaleLevel < 4; scaleLevel++) {
                LegacyShipModelTransformCatalog.RenderTransform transform =
                        LegacyShipModelTransformCatalog.forModel(modelStems[modelIndex], scaleLevel).orElse(null);
                float scale = expected[modelIndex][scaleLevel];
                float expectedY = modelIndex < 2 ? scale * 0.95F : scale;
                if (transform == null
                        || Math.abs(transform.scaleX() - scale) > 1.0E-6F
                        || Math.abs(transform.scaleY() - expectedY) > 1.0E-6F
                        || Math.abs(transform.scaleZ() - scale) > 1.0E-6F
                        || Math.abs(transform.offsetY() - offsets[modelIndex][scaleLevel]) > 1.0E-6F) {
                    helper.fail("SHIP-06 model scale branch differs from the legacy source: "
                            + modelStems[modelIndex] + " level=" + scaleLevel);
                    return;
                }
            }
        }

        if (LegacyShipModelTransformCatalog.forModel("ModelSubmHime", 0).isPresent()
                || LegacyShipModelTransformCatalog.forModel("ModelSSNH", 0).isPresent()) {
            helper.fail("SHIP-06 constant-scale Hime models must not receive a dynamic scale branch");
            return;
        }

        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void ship06SubmarineHimeAndBeamProjectileBoundariesMatchLegacy(GameTestHelper helper) {
        List<Vec3> yawZero = LegacyShipBehaviorCatalog.submarineHimeMissileLaunchOffsetsFor(0.0F);
        List<Vec3> yawNinety = LegacyShipBehaviorCatalog.submarineHimeMissileLaunchOffsetsFor(90.0F);
        if (yawZero.size() != 2 || Math.abs(yawZero.get(0).x - 1.0D) > 1.0E-6D
                || Math.abs(yawZero.get(0).z) > 1.0E-6D
                || Math.abs(yawZero.get(1).x + 1.0D) > 1.0E-6D
                || Math.abs(yawNinety.get(0).x) > 1.0E-6D
                || Math.abs(yawNinety.get(0).z - 1.0D) > 1.0E-6D) {
            helper.fail("SHIP-06 submarine Hime missile offsets should preserve CalcHelper.rotateXZByAxis");
            return;
        }

        LegacyShipEntity host = ModEntityTypes.LEGACY_SHIP.get().create(helper.getLevel());
        LegacyShipEntity sameOwner = ModEntityTypes.LEGACY_SHIP.get().create(helper.getLevel());
        LegacyShipEntity hostileHost = ModEntityTypes.LEGACY_SHIP.get().create(helper.getLevel());
        LegacyShipEntity hostileTarget = ModEntityTypes.LEGACY_SHIP.get().create(helper.getLevel());
        Zombie ordinaryTarget = new Zombie(EntityType.ZOMBIE, helper.getLevel());
        if (host == null || sameOwner == null || hostileHost == null || hostileTarget == null) {
            helper.fail("SHIP-06 projectile boundary entities should be creatable");
            return;
        }

        UUID ownerUuid = UUID.randomUUID();
        host.setVariantEggMeta(40);
        host.setOwner(ownerUuid, "Ship06Owner", 6006);
        sameOwner.setVariantEggMeta(41);
        sameOwner.setOwner(ownerUuid, "Ship06Owner", 6006);
        hostileHost.setVariantEggMeta(2041);
        hostileTarget.setVariantEggMeta(2040);
        helper.getLevel().addFreshEntity(host);
        helper.getLevel().addFreshEntity(sameOwner);
        helper.getLevel().addFreshEntity(hostileHost);
        helper.getLevel().addFreshEntity(hostileTarget);
        helper.getLevel().addFreshEntity(ordinaryTarget);
        if (LegacyShipBehaviorCatalog.isLegacyProjectileDamageAllowed(host, sameOwner)
                || LegacyShipBehaviorCatalog.isLegacyProjectileDamageAllowed(hostileHost, hostileTarget)
                || !LegacyShipBehaviorCatalog.isLegacyProjectileDamageAllowed(host, ordinaryTarget)) {
            helper.fail("SHIP-06 projectile damage should preserve owner and hostile-team boundaries");
            return;
        }

        Zombie passenger = new Zombie(EntityType.ZOMBIE, helper.getLevel());
        helper.getLevel().addFreshEntity(passenger);
        if (!passenger.startRiding(host, true)
                || !LegacyShipBehaviorCatalog.isLegacyProjectileHostRelated(host, passenger)
                || !LegacyShipBehaviorCatalog.isLegacyProjectileHostRelated(host, host)) {
            helper.fail("SHIP-06 projectile collision should exclude the host and its direct passengers");
            return;
        }

        host.discard();
        sameOwner.discard();
        hostileHost.discard();
        hostileTarget.discard();
        ordinaryTarget.discard();
        passenger.discard();
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void ship04HimeModelStateUsesLegacyShowEquipBits(GameTestHelper helper) {
        if (LegacyShipModelStateCatalog.isPartVisible("ModelAirfieldHime", "EquipHand01", 0)
                || !LegacyShipModelStateCatalog.isPartVisible("ModelAirfieldHime", "EquipHand01", 1 << 1)
                || LegacyShipModelStateCatalog.isPartVisible("ModelDestroyerHime", "Cannon01", 1 << 1)
                || !LegacyShipModelStateCatalog.isPartVisible("ModelDestroyerHime", "Cannon01", 1 << 2)
                || LegacyShipModelStateCatalog.isPartVisible("ModelIsolatedHime", "LegLeft02b", 0)
                || !LegacyShipModelStateCatalog.isPartVisible("ModelIsolatedHime", "LegLeft02b", 1 << 6)
                || !LegacyShipModelStateCatalog.isPartVisible("ModelIsolatedHime", "LegLeft02a", 0)
                || LegacyShipModelStateCatalog.isPartVisible("ModelIsolatedHime", "LegLeft02a", 1 << 6)
                || !LegacyShipModelStateCatalog.isPartVisible("ModelBattleshipHime", "BodyMain", 0)
                || !LegacyShipModelStateCatalog.isPartVisible("ModelHarbourHime", "BodyMain", 0)
                || !LegacyShipModelStateCatalog.isPartVisible("ModelIsolatedHime", "UnknownPart", 0)) {
            helper.fail("selected Hime model visibility should preserve the legacy Display.State bit rules");
            return;
        }

        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void ship05SpecialModelStateUsesLegacyShowEquipBits(GameTestHelper helper) {
        if (LegacyShipModelStateCatalog.isPartVisible("ModelMidwayHime", "EquipSR01", 0)
                || LegacyShipModelStateCatalog.isPartVisible("ModelMidwayHime", "EquipSR01c", 1 << 1)
                || LegacyShipModelStateCatalog.isPartVisible("ModelMidwayHime", "Collar01", 0)
                || !LegacyShipModelStateCatalog.isPartVisible("ModelMidwayHime", "Collar01", 1 << 1)
                || LegacyShipModelStateCatalog.isPartVisible("ModelNorthernHime", "GlowEquipBase", 0)
                || !LegacyShipModelStateCatalog.isPartVisible("ModelNorthernHime", "GlowEquipBase", 1 << 0)
                || LegacyShipModelStateCatalog.isPartVisible("ModelNorthernHime", "SantaCloth01", 0)
                || !LegacyShipModelStateCatalog.isPartVisible("ModelNorthernHime", "SantaCloth01", 1 << 1)
                || LegacyShipModelStateCatalog.isPartVisible("ModelNorthernHime", "EquipUmbre01a", 0)
                || !LegacyShipModelStateCatalog.isPartVisible("ModelNorthernHime", "EquipUmbre01a", 1 << 2)
                || LegacyShipModelStateCatalog.isPartVisible("ModelNorthernHime", "ShoesL", 0)
                || !LegacyShipModelStateCatalog.isPartVisible("ModelNorthernHime", "ShoesL", 1 << 3)
                || LegacyShipModelStateCatalog.isPartVisible("ModelCarrierWDemon", "EquipBase", 0)
                || !LegacyShipModelStateCatalog.isPartVisible("ModelCarrierWDemon", "EquipBase", 1 << 1)
                || LegacyShipModelStateCatalog.isPartVisible("ModelDestroyerShimakaze", "EquipBase", 0)
                || !LegacyShipModelStateCatalog.isPartVisible("ModelDestroyerShimakaze", "EquipBase", 1 << 1)
                || LegacyShipModelStateCatalog.isPartVisible("ModelDestroyerShimakaze", "HairAnchor", 0)
                || !LegacyShipModelStateCatalog.isPartVisible("ModelDestroyerShimakaze", "HairAnchor", 1 << 2)
                || LegacyShipModelStateCatalog.isPartVisible("ModelDestroyerShimakaze", "EarBase", 0)
                || !LegacyShipModelStateCatalog.isPartVisible("ModelDestroyerShimakaze", "EarBase", 1 << 3)
                || LegacyShipModelStateCatalog.isPartVisible("ModelBattleshipNagato", "HeadEquip", 0)
                || !LegacyShipModelStateCatalog.isPartVisible("ModelBattleshipNagato", "HeadEquip", 1 << 0)
                || LegacyShipModelStateCatalog.isPartVisible("ModelBattleshipNagato", "EquipBase", 0)
                || !LegacyShipModelStateCatalog.isPartVisible("ModelBattleshipNagato", "EquipBase", 1 << 1)
                || !LegacyShipModelStateCatalog.isPartVisible("ModelNorthernHime", "UnknownPart", 0)) {
            helper.fail("SHIP-05 model visibility should preserve each legacy showEquip state bit");
            return;
        }

        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void ship05SpecialRuntimeCatalogKeepsLegacyBoundaries(GameTestHelper helper) {
        ShipEntitySpec midway = ShipEntitySpecs.getByEggMeta(32);
        ShipEntitySpec northern = ShipEntitySpecs.getByEggMeta(33);
        ShipEntitySpec carrierWd = ShipEntitySpecs.getByEggMeta(35);
        ShipEntitySpec shimakaze = ShipEntitySpecs.getByEggMeta(38);
        ShipEntitySpec hostileShimakaze = ShipEntitySpecs.getByEggMeta(2038);
        ShipEntitySpec nagato = ShipEntitySpecs.getByEggMeta(39);
        ShipEntitySpec hostileNagato = ShipEntitySpecs.getByEggMeta(2039);

        if (midway.legacyClassId() != 30 || !"ModelMidwayHime".equals(midway.modelSourceStem())
                || northern.legacyClassId() != 31 || !"ModelNorthernHime".equals(northern.modelSourceStem())
                || carrierWd.legacyClassId() != 33 || !"ModelCarrierWDemon".equals(carrierWd.modelSourceStem())
                || shimakaze.legacyClassId() != 36 || !"ModelDestroyerShimakaze".equals(shimakaze.modelSourceStem())
                || nagato.legacyClassId() != 37 || !"ModelBattleshipNagato".equals(nagato.modelSourceStem())) {
            helper.fail("SHIP-05 specs should retain the exact legacy class and model source mapping");
            return;
        }

        LegacyShipAttackProfile midwayProfile = LegacyShipBehaviorCatalog.attackProfile(midway);
        LegacyShipAttackProfile northernProfile = LegacyShipBehaviorCatalog.attackProfile(northern);
        LegacyShipAttackProfile carrierProfile = LegacyShipBehaviorCatalog.attackProfile(carrierWd);
        if (!midwayProfile.hasAirAttack()
                || midwayProfile.projectileProfile(LegacyShipAttackKind.AIR_HEAVY).visual()
                != LegacyShipProjectileVisual.BOMB
                || Math.abs(LegacyShipBehaviorCatalog.aircraftLaunchHeightFactorFor(
                midway, LegacyShipAttackKind.AIR_LIGHT) - 0.7F) > 1.0E-6F
                || !northernProfile.hasAirAttack()
                || Math.abs(LegacyShipBehaviorCatalog.aircraftLaunchHeightFactorFor(
                northern, LegacyShipAttackKind.AIR_LIGHT) - 1.0F) > 1.0E-6F
                || Math.abs(LegacyShipBehaviorCatalog.aircraftLaunchHeightFactorFor(
                northern, LegacyShipAttackKind.AIR_HEAVY) - 1.0F) > 1.0E-6F
                || !carrierProfile.light() || carrierProfile.heavy() || !carrierProfile.hasAirAttack()
                || Math.abs(LegacyShipBehaviorCatalog.aircraftLaunchHeightFactorFor(
                carrierWd, LegacyShipAttackKind.AIR_LIGHT) - 1.2F) > 1.0E-6F) {
            helper.fail("SHIP-05 carrier launch profiles should preserve the legacy attack channels and heights");
            return;
        }

        if (LegacyShipBehaviorCatalog.equipmentTypeFor(midway) != 2
                || LegacyShipBehaviorCatalog.equipmentTypeFor(northern) != 2
                || LegacyShipBehaviorCatalog.equipmentTypeFor(carrierWd) != 3
                || LegacyShipBehaviorCatalog.foodSaturationMaxFor(midway) != 35
                || LegacyShipBehaviorCatalog.foodSaturationMaxFor(northern) != 16
                || LegacyShipBehaviorCatalog.foodSaturationMaxFor(carrierWd) != 30
                || !LegacyShipBehaviorCatalog.dismountsBeforeDamage(northern)
                || LegacyShipBehaviorCatalog.hostileBossBarColorFor(hostileShimakaze)
                != BossEvent.BossBarColor.YELLOW
                || LegacyShipBehaviorCatalog.hostileBossBarColorFor(hostileNagato)
                != BossEvent.BossBarColor.WHITE) {
            helper.fail("SHIP-05 runtime state should preserve equipment, food, dismount, and boss boundaries");
            return;
        }

        if (LegacyShipBehaviorCatalog.behaviorFor(northern).combatHook()
                        != LegacyShipBehaviorCatalog.CombatHook.NORTHERN_HIME_FLOATING_FORT
                || !LegacyShipBehaviorCatalog.shouldUseCombatHook(northern, LegacyShipAttackKind.HEAVY)
                || LegacyShipBehaviorCatalog.shouldUseCombatHook(northern, LegacyShipAttackKind.LIGHT)
                || LegacyShipBehaviorCatalog.behaviorFor(shimakaze).combatHook()
                        != LegacyShipBehaviorCatalog.CombatHook.SHIMAKAZE_TORPEDO_BURST
                || !LegacyShipBehaviorCatalog.shouldUseCombatHook(shimakaze, LegacyShipAttackKind.LIGHT)
                || !LegacyShipBehaviorCatalog.shouldUseCombatHook(shimakaze, LegacyShipAttackKind.HEAVY)
                || LegacyShipBehaviorCatalog.behaviorFor(nagato).combatHook()
                        != LegacyShipBehaviorCatalog.CombatHook.NAGATO_HEAVY_STRIKE
                || !LegacyShipBehaviorCatalog.shouldUseCombatHook(nagato, LegacyShipAttackKind.HEAVY)
                || LegacyShipBehaviorCatalog.shouldUseCombatHook(nagato, LegacyShipAttackKind.LIGHT)) {
            helper.fail("SHIP-05 special combat hooks should remain limited to their legacy attack entries");
            return;
        }

        LegacyShipEntity hostileShip = ModEntityTypes.LEGACY_SHIP.get().create(helper.getLevel());
        if (hostileShip == null) {
            helper.fail("hostile Shimakaze should be creatable for the servant chase boundary");
            return;
        }
        hostileShip.setVariantEggMeta(2038);
        hostileShip.setAiOnSightChase(true);
        if (hostileShip.isAiOnSightChase()) {
            helper.fail("hostile Shimakaze and its Rensouhou must keep the legacy OnSightChase=false boundary");
            return;
        }
        hostileShip.discard();

        if (!LegacyShipBehaviorCatalog.shouldSpawnNorthernHimeMouthEffect(4, false, 8)
                || LegacyShipBehaviorCatalog.shouldSpawnNorthernHimeMouthEffect(4, false, 9)
                || LegacyShipBehaviorCatalog.shouldSpawnNorthernHimeMouthEffect(4, true, 8)) {
            helper.fail("Northern Hime mouth feedback should keep its legacy model, fuel, and eight-tick gates");
            return;
        }

        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void legacyPickupRegistrationAndRangeMatch(GameTestHelper helper) {
        int[] pickupClasses = {0, 1, 2, 3, 16, 17, 18, 19, 27, 36, 38, 39, 44, 51, 52, 53, 54, 72};
        for (ShipEntitySpec spec : ShipEntitySpecs.values()) {
            boolean expected = false;
            for (int pickupClass : pickupClasses) {
                if (spec.legacyClassId() == pickupClass || spec.legacyClassId() - 2000 == pickupClass) {
                    expected = true;
                    break;
                }
            }
            if (LegacyShipBehaviorCatalog.supportsItemPickup(spec) != expected) {
                helper.fail("item pickup registration mismatch for legacy class " + spec.legacyClassId());
                return;
            }
            if (expected) {
                int expectedBase = (spec.legacyClassId() == 16 || spec.legacyClassId() == 2016) ? 8 : 4;
                if (LegacyShipBehaviorCatalog.pickupRangeBaseFor(spec) != expectedBase) {
                    helper.fail("item pickup range base mismatch for legacy class " + spec.legacyClassId());
                    return;
                }
            }
        }

        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void himeShipMountProfilesMatchLegacySeatAndAttackFamilies(GameTestHelper helper) {
        int[] eggMetas = {22, 23, 28, 30, 31, 32, 35, 46};
        int[] styles = {
                LegacyMountEntity.STYLE_CARRIER,
                LegacyMountEntity.STYLE_AIRFIELD,
                LegacyMountEntity.STYLE_BATTLESHIP,
                LegacyMountEntity.STYLE_HARBOUR,
                LegacyMountEntity.STYLE_ISOLATED,
                LegacyMountEntity.STYLE_MIDWAY,
                LegacyMountEntity.STYLE_CARRIER_WD,
                LegacyMountEntity.STYLE_SUBMARINE};
        boolean[] aircraft = {true, true, false, true, true, true, true, false};

        for (int index = 0; index < eggMetas.length; index++) {
            LegacyShipBehaviorCatalog.ShipMountProfile profile =
                    LegacyShipBehaviorCatalog.shipMountProfileFor(ShipEntitySpecs.getByEggMeta(eggMetas[index]));
            if (profile == null || profile.style() != styles[index]
                    || profile.usesAircraft() != aircraft[index]) {
                helper.fail("legacy hime mount profile mismatch for egg meta " + eggMetas[index]);
                return;
            }
        }

        LegacyShipBehaviorCatalog.ShipMountProfile airfield =
                LegacyShipBehaviorCatalog.shipMountProfileFor(ShipEntitySpecs.getByEggMeta(23));
        LegacyShipBehaviorCatalog.ShipMountProfile battleship =
                LegacyShipBehaviorCatalog.shipMountProfileFor(ShipEntitySpecs.getByEggMeta(28));
        LegacyShipBehaviorCatalog.ShipMountProfile carrier =
                LegacyShipBehaviorCatalog.shipMountProfileFor(ShipEntitySpecs.getByEggMeta(22));
        LegacyShipBehaviorCatalog.ShipMountProfile carrierWd =
                LegacyShipBehaviorCatalog.shipMountProfileFor(ShipEntitySpecs.getByEggMeta(35));
        LegacyShipBehaviorCatalog.ShipMountProfile submarine =
                LegacyShipBehaviorCatalog.shipMountProfileFor(ShipEntitySpecs.getByEggMeta(46));
        if (Math.abs(carrier.hostY() - 1.12D) > 0.0001D
                || Math.abs(carrier.playerY() + 0.39D) > 0.0001D
                || Math.abs(carrierWd.hostX() - 0.25D) > 0.0001D
                || Math.abs(carrierWd.playerY() + 0.39D) > 0.0001D
                || Math.abs(submarine.hostX() + 0.8D) > 0.0001D
                || Math.abs(submarine.playerY() - 1.2D) > 0.0001D
                || Math.abs(airfield.hostX() - 0.59D) > 0.0001D
                || Math.abs(airfield.hostY() + 0.25D) > 0.0001D
                || Math.abs(airfield.playerZ() + 1.12D) > 0.0001D
                || Math.abs(battleship.width() - 1.9F) > 0.0001F
                || Math.abs(battleship.height() - 3.1F) > 0.0001F
                || Math.abs(submarine.width() - 1.8F) > 0.0001F
                || Math.abs(submarine.height() - 1.6F) > 0.0001F) {
            helper.fail("legacy hime mount profiles should preserve the original seat and size constants");
            return;
        }

        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void himeShipMountLifecycleMirrorsLegacyFuelAndStateGate(GameTestHelper helper) {
        int[] eggMetas = {22, 35, 46};
        int[] styles = {
                LegacyMountEntity.STYLE_CARRIER,
                LegacyMountEntity.STYLE_CARRIER_WD,
                LegacyMountEntity.STYLE_SUBMARINE};
        LegacyShipEntity[] ships = new LegacyShipEntity[eggMetas.length];
        LegacyShipMountEntity[] mounts = new LegacyShipMountEntity[eggMetas.length];

        for (int index = 0; index < eggMetas.length; index++) {
            LegacyShipEntity ship = ModEntityTypes.LEGACY_SHIP.get().create(helper.getLevel());
            if (ship == null) {
                helper.fail("legacy ship should be creatable for hime mount lifecycle coverage");
                return;
            }

            ship.setVariantEggMeta(eggMetas[index]);
            ship.setModelState(1);
            ship.setGrudge(1000);
            ship.setPos(2.5D + index * 4.0D, 2.0D, 2.5D);
            helper.getLevel().addFreshEntity(ship);
            ship.tickCount = 128;
            LegacyShipBehaviorCatalog.tickLegacyShipMountBehavior(ship);

            if (!(ship.getVehicle() instanceof LegacyShipMountEntity mount)
                    || mount.getMountStyle() != styles[index]
                    || !mount.getPassengers().contains(ship)) {
                helper.fail("legacy hime should summon the matching old mount when state bit 0 and fuel are available for egg meta "
                        + eggMetas[index]);
                return;
            }
            ships[index] = ship;
            mounts[index] = mount;
        }

        for (int index = 0; index < ships.length; index++) {
            ships[index].setGrudge(0);
            ships[index].tickCount = 144;
            LegacyShipBehaviorCatalog.tickLegacyShipMountBehavior(ships[index]);
            if (ships[index].isPassenger() || !mounts[index].isRemoved()) {
                helper.fail("legacy hime mount should dismount and retire when fuel reaches zero for egg meta "
                        + eggMetas[index]);
                return;
            }
        }

        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void shipPickupGoalStopsForLegacyFuelAndRidingState(GameTestHelper helper) {
        if (pickupGoalCanUseWith(helper, 6, ship -> ship.setGrudge(0))) {
            helper.fail("pickup goal must stop when the legacy ship has no fuel");
            return;
        }

        LegacyShipEntity ship = ModEntityTypes.LEGACY_SHIP.get().create(helper.getLevel());
        if (ship == null) {
            helper.fail("legacy ship should be creatable for riding pickup coverage");
            return;
        }
        ship.setVariantEggMeta(53);
        ship.setOwner(UUID.randomUUID(), "PickupRiding");
        ship.setPos(2.5D, 2.0D, 2.5D);
        helper.getLevel().addFreshEntity(ship);
        LegacyMountEntity mount = ModEntityTypes.LEGACY_MOUNT.get().create(helper.getLevel());
        if (mount == null) {
            helper.fail("legacy mount should be creatable for riding pickup coverage");
            return;
        }
        mount.setPos(ship.getX(), ship.getY(), ship.getZ());
        helper.getLevel().addFreshEntity(mount);
        ship.startRiding(mount, true);
        ItemEntity grudgeDrop = new ItemEntity(helper.getLevel(), ship.getX() + 1.0D, ship.getY(), ship.getZ(),
                new ItemStack(ModItems.GRUDGE.get(), 1));
        helper.getLevel().addFreshEntity(grudgeDrop);
        if (new LegacyShipPickItemGoal(ship, 1.0D).canUse()) {
            helper.fail("pickup goal must stop while the ship is riding a mount");
            return;
        }

        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void shipPickupGoalUsesLegacySelectionAndCooldown(GameTestHelper helper) {
        LegacyShipEntity ship = ModEntityTypes.LEGACY_SHIP.get().create(helper.getLevel());
        if (ship == null) {
            helper.fail("legacy ship should be creatable for pickup cadence coverage");
            return;
        }
        ship.setVariantEggMeta(53);
        ship.setGrudge(100);
        ship.setOwner(UUID.randomUUID(), "PickupCadence");
        BlockPos shipPos = helper.absolutePos(new BlockPos(1, 2, 1));
        ship.setPos(shipPos.getX() + 0.5D, shipPos.getY(), shipPos.getZ() + 0.5D);
        helper.getLevel().addFreshEntity(ship);

        int lastCargoSlot = ship.getShipInventory().getContainerSize() - 1;
        for (int slot = LegacyShipEntity.EQUIPMENT_SLOT_COUNT; slot < lastCargoSlot; slot++) {
            ship.getShipInventory().setItem(slot, new ItemStack(Items.COBBLESTONE, 64));
        }
        ship.getShipInventory().setItem(LegacyShipEntity.EQUIPMENT_SLOT_COUNT, new ItemStack(Items.APPLE, 63));
        ship.getShipInventory().setItem(lastCargoSlot, new ItemStack(Items.COBBLESTONE, 64));
        ItemEntity appleDrop = new ItemEntity(helper.getLevel(), ship.getX(), ship.getY(), ship.getZ(),
                new ItemStack(Items.APPLE, 3));
        appleDrop.setPickUpDelay(1);
        helper.getLevel().addFreshEntity(appleDrop);

        LegacyShipPickItemGoal goal = new LegacyShipPickItemGoal(ship, 1.0D);
        if (goal.canUse()) {
            helper.fail("pickup goal should require a genuinely empty legacy cargo slot, not partial stack space");
            return;
        }
        ship.getShipInventory().setItem(lastCargoSlot, ItemStack.EMPTY);
        if (!goal.canUse()) {
            helper.fail("pickup goal should select a nearby item when an empty cargo slot exists");
            return;
        }
        goal.start();
        goal.tick();
        if (appleDrop.getItem().getCount() != 3
                || ship.getShipInventory().getItem(LegacyShipEntity.EQUIPMENT_SLOT_COUNT).getCount() != 63) {
            helper.fail("pickup delay should prevent the first legacy pickup attempt");
            return;
        }

        appleDrop.setPickUpDelay(0);
        goal.tick();
        if (appleDrop.getItem().getCount() != 3) {
            helper.fail("legacy pickup cooldown should prevent an immediate second pickup attempt");
            return;
        }
        for (int tick = 0; tick < 20; tick++) {
            goal.tick();
        }
        if (appleDrop.isAlive()
                || ship.getShipInventory().getItem(LegacyShipEntity.EQUIPMENT_SLOT_COUNT).getCount() != 64
                || ship.getShipInventory().getItem(lastCargoSlot).getCount() != 2) {
            helper.fail("legacy pickup cooldown should eventually allow the full drop to use the matching stack and empty slot");
            return;
        }

        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void shipPickupGoalSelectsDelayedDropsLikeLegacy(GameTestHelper helper) {
        LegacyShipEntity ship = ModEntityTypes.LEGACY_SHIP.get().create(helper.getLevel());
        if (ship == null) {
            helper.fail("legacy ship should be creatable for delayed pickup coverage");
            return;
        }
        ship.setVariantEggMeta(53);
        ship.setGrudge(100);
        ship.setOwner(UUID.randomUUID(), "PickupDelay");
        BlockPos shipPos = helper.absolutePos(new BlockPos(1, 2, 1));
        ship.setPos(shipPos.getX() + 0.5D, shipPos.getY(), shipPos.getZ() + 0.5D);
        helper.getLevel().addFreshEntity(ship);

        ItemEntity delayedDrop = new ItemEntity(helper.getLevel(), ship.getX(), ship.getY(), ship.getZ(),
                new ItemStack(ModItems.GRUDGE.get(), 1));
        delayedDrop.setPickUpDelay(20);
        helper.getLevel().addFreshEntity(delayedDrop);

        LegacyShipPickItemGoal goal = new LegacyShipPickItemGoal(ship, 1.0D);
        if (!goal.canUse()) {
            helper.fail("legacy pickup scan should select a delayed item and defer the pickup check");
            return;
        }
        goal.start();
        goal.tick();
        if (!delayedDrop.isAlive() || delayedDrop.getItem().isEmpty()) {
            helper.fail("delayed item should remain until its legacy pickup delay expires");
            return;
        }

        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void shipPickupAppliesLegacyExperienceAndPersistsIt(GameTestHelper helper) {
        LegacyShipEntity ship = ModEntityTypes.LEGACY_SHIP.get().create(helper.getLevel());
        if (ship == null) {
            helper.fail("legacy ship should be creatable for pickup feedback coverage");
            return;
        }

        ship.setVariantEggMeta(53);
        ship.setGrudge(100);
        ship.setOwner(UUID.randomUUID(), "PickupExperience");
        BlockPos pickupPos = helper.absolutePos(new BlockPos(1, 2, 1));
        ship.setPos(pickupPos.getX() + 0.5D, pickupPos.getY(), pickupPos.getZ() + 0.5D);
        helper.getLevel().addFreshEntity(ship);
        ItemEntity drop = new ItemEntity(helper.getLevel(), ship.getX(), ship.getY(), ship.getZ(),
                new ItemStack(ModItems.GRUDGE.get(), 1));
        drop.setPickUpDelay(0);
        helper.getLevel().addFreshEntity(drop);

        LegacyShipPickItemGoal goal = new LegacyShipPickItemGoal(ship, 1.0D);
        if (!goal.canUse()) {
            helper.fail("legacy pickup goal should find an immediately collectible item");
            return;
        }
        goal.start();
        goal.tick();
        int expectedPickupExperience = (int) (LegacyShipBehaviorCatalog.pickupExperience()
                * ship.getLegacyStats().get(LegacyShipStatTables.Attr.XP));
        if (ship.getShipExperience() != expectedPickupExperience) {
            helper.fail("legacy item pickup should grant the configured pickup experience once: actual="
                    + ship.getShipExperience() + ", expected=" + expectedPickupExperience
                    + ", xpMultiplier=" + ship.getLegacyStats().get(LegacyShipStatTables.Attr.XP)
                    + ", level=" + ship.getShipLevel() + ", dropAlive=" + drop.isAlive()
                    + ", dropCount=" + drop.getItem().getCount()
                    + ", distance=" + ship.distanceToSqr(drop)
                    + ", delayed=" + drop.hasPickUpDelay()
                    + ", canContinue=" + goal.canContinueToUse()
                    + ", cargo=" + countItems(ship.getShipInventory(), ModItems.GRUDGE.get()));
            return;
        }

        CompoundTag saved = new CompoundTag();
        ship.addAdditionalSaveData(saved);
        if (saved.getInt("ShipExperience") != ship.getShipExperience()) {
            helper.fail("ship experience should be saved in modern entity data");
            return;
        }

        LegacyShipEntity loaded = ModEntityTypes.LEGACY_SHIP.get().create(helper.getLevel());
        if (loaded == null) {
            helper.fail("legacy ship should be creatable for experience save/load coverage");
            return;
        }
        loaded.readAdditionalSaveData(saved);
        if (loaded.getShipExperience() != ship.getShipExperience()) {
            helper.fail("ship experience should survive entity save/load");
            return;
        }

        CompoundTag legacyTag = new CompoundTag();
        legacyTag.putInt("VariantEggMeta", 53);
        CompoundTag extended = new CompoundTag();
        CompoundTag minor = new CompoundTag();
        minor.putInt("Exp", 17);
        extended.put("Minor", minor);
        legacyTag.put("ShipExtProps", extended);
        LegacyShipEntity migrated = ModEntityTypes.LEGACY_SHIP.get().create(helper.getLevel());
        if (migrated == null) {
            helper.fail("legacy ship should be creatable for legacy experience migration coverage");
            return;
        }
        migrated.readAdditionalSaveData(legacyTag);
        if (migrated.getShipExperience() != 17) {
            helper.fail("legacy ShipExtProps.Minor.Exp should migrate to modern ship experience");
            return;
        }

        ship.setMorale(4001);
        ship.setShipExperience(ship.getShipExperienceNext() - 1);
        int levelBefore = ship.getShipLevel();
        ship.addShipExperience(1);
        if (ship.getShipLevel() != levelBefore + 1 || ship.getShipExperience() != 0) {
            helper.fail("ship experience should use the legacy next-level threshold and carry over after level-up");
            return;
        }

        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void shipRuntimeStateMigratesLegacyMinorScalarsAndDefaults(GameTestHelper helper) {
        LegacyShipEntity migrated = ModEntityTypes.LEGACY_SHIP.get().create(helper.getLevel());
        if (migrated == null) {
            helper.fail("legacy ship should be creatable for nested runtime state migration coverage");
            return;
        }

        CompoundTag legacyTag = new CompoundTag();
        legacyTag.putInt("VariantEggMeta", 53);
        CompoundTag extended = new CompoundTag();
        CompoundTag minor = new CompoundTag();
        minor.putInt("Level", 7);
        minor.putInt("Exp", 13);
        minor.putInt("Morale", 4321);
        minor.putInt("NumAmmoL", 17);
        minor.putInt("NumAmmoH", 19);
        minor.putInt("NumGrudge", 23);
        minor.putInt("WpStay", 6);
          minor.putInt("PlayerUID", 29);
          minor.putInt("ShipUID", 31);
          extended.put("Minor", minor);
          CompoundTag legacyFlags = new CompoundTag();
         legacyFlags.putBoolean("IsMarried", true);
         legacyFlags.putBoolean("WedEffect", false);
          legacyFlags.putBoolean("PickItem", false);
          legacyFlags.putBoolean("TimeKeeper", false);
          legacyFlags.putBoolean("AutoPump", true);
          extended.put("ShipFlags", legacyFlags);
          extended.putString("Owner", "LegacyOwner");
        legacyTag.put("ShipExtProps", extended);
        migrated.readAdditionalSaveData(legacyTag);
        if (migrated.getShipLevel() != 7
                || migrated.getShipExperience() != 13
                || migrated.getMorale() != 4321
                || migrated.getLightAmmo() != 17
                || migrated.getHeavyAmmo() != 19
                || migrated.getGrudge() != 23
                || migrated.getWaypointStayMode() != 6
                || migrated.getOwnerUid() != 29
                || migrated.getShipUid() != 31
                 || !migrated.isMarried()
                  || migrated.isRingEffectEnabled()
                  || migrated.isAiPickItem()
                  || migrated.isTimeKeeper()
                  || !migrated.isAutoPump()
                  || !"LegacyOwner".equals(migrated.getOwnerName())) {
            helper.fail("legacy ShipExtProps.Minor runtime scalars should migrate without changing their values");
            return;
        }

        CompoundTag precedenceTag = new CompoundTag();
        precedenceTag.putInt("VariantEggMeta", 53);
        precedenceTag.putInt("ShipLevel", 8);
        precedenceTag.putInt("ShipExperience", 37);
        precedenceTag.putInt("ShipMorale", 8765);
        precedenceTag.putInt("ShipLightAmmo", 41);
        precedenceTag.putInt("ShipHeavyAmmo", 43);
        precedenceTag.putInt("ShipGrudge", 47);
        precedenceTag.putInt("ShipWpStay", 3);
        precedenceTag.putInt("OwnerUID", 53);
        precedenceTag.putInt("ShipUID", 59);
         precedenceTag.putBoolean("ShipMarried", false);
         precedenceTag.putBoolean("WedEffect", true);
          precedenceTag.putBoolean("AiPickItem", true);
          precedenceTag.putBoolean("ShipTimeKeeper", true);
          precedenceTag.putBoolean("ShipAutoPump", false);
        precedenceTag.putString("OwnerName", "ModernOwner");
        CompoundTag precedenceExtended = new CompoundTag();
        CompoundTag precedenceMinor = new CompoundTag();
        precedenceMinor.putInt("Level", 1);
        precedenceMinor.putInt("Exp", 2);
        precedenceMinor.putInt("Morale", 3);
        precedenceMinor.putInt("NumAmmoL", 5);
        precedenceMinor.putInt("NumAmmoH", 7);
        precedenceMinor.putInt("NumGrudge", 11);
        precedenceMinor.putInt("WpStay", 9);
        precedenceMinor.putInt("PlayerUID", 13);
        precedenceMinor.putInt("ShipUID", 17);
        precedenceExtended.put("Minor", precedenceMinor);
        CompoundTag precedenceFlags = new CompoundTag();
         precedenceFlags.putBoolean("IsMarried", true);
         precedenceFlags.putBoolean("WedEffect", false);
          precedenceFlags.putBoolean("PickItem", false);
          precedenceFlags.putBoolean("TimeKeeper", false);
          precedenceFlags.putBoolean("AutoPump", true);
        precedenceExtended.put("ShipFlags", precedenceFlags);
        precedenceExtended.putString("Owner", "LegacyOwnerShouldLose");
        precedenceTag.put("ShipExtProps", precedenceExtended);
        LegacyShipEntity precedence = ModEntityTypes.LEGACY_SHIP.get().create(helper.getLevel());
        if (precedence == null) {
            helper.fail("legacy ship should be creatable for runtime state precedence coverage");
            return;
        }
        precedence.readAdditionalSaveData(precedenceTag);
        if (precedence.getShipLevel() != 8
                || precedence.getShipExperience() != 37
                || precedence.getMorale() != 8765
                || precedence.getLightAmmo() != 41
                || precedence.getHeavyAmmo() != 43
                || precedence.getGrudge() != 47
                || precedence.getWaypointStayMode() != 3
                || precedence.getOwnerUid() != 53
                || precedence.getShipUid() != 59
                 || precedence.isMarried()
                 || !precedence.isRingEffectEnabled()
                  || !precedence.isAiPickItem()
                  || !precedence.isTimeKeeper()
                  || precedence.isAutoPump()
                  || !"ModernOwner".equals(precedence.getOwnerName())) {
            helper.fail("modern runtime fields should take precedence over legacy nested values");
            return;
        }

        LegacyShipEntity defaults = ModEntityTypes.LEGACY_SHIP.get().create(helper.getLevel());
        if (defaults == null) {
            helper.fail("legacy ship should be creatable for runtime state defaults coverage");
            return;
        }
        CompoundTag defaultsTag = new CompoundTag();
        defaultsTag.putInt("VariantEggMeta", 53);
        defaults.readAdditionalSaveData(defaultsTag);
        if (defaults.getMorale() != 60 || defaults.getGrudge() != 0 || defaults.getLightAmmo() != 0
                || defaults.getHeavyAmmo() != 0 || !defaults.isAiPickItem()
                || !defaults.isTimeKeeper()
                || defaults.isAutoPump()
                || defaults.getWaypointStayMode() != LegacyShipEntity.DEFAULT_WP_STAY_MODE) {
            helper.fail("缺少现代和旧版字段时，应使用旧版初始资源、士气及行为设置");
            return;
        }

        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void autoPumpKeepsLegacyLevelDelayBands(GameTestHelper helper) {
        if (LegacyShipBehaviorCatalog.autoPumpDelayForLevel(1) != 63
                || LegacyShipBehaviorCatalog.autoPumpDelayForLevel(29) != 63
                || LegacyShipBehaviorCatalog.autoPumpDelayForLevel(30) != 31
                || LegacyShipBehaviorCatalog.autoPumpDelayForLevel(74) != 31
                || LegacyShipBehaviorCatalog.autoPumpDelayForLevel(75) != 15
                || LegacyShipBehaviorCatalog.autoPumpDelayForLevel(114) != 15
                || LegacyShipBehaviorCatalog.autoPumpDelayForLevel(115) != 7
                || LegacyShipBehaviorCatalog.autoPumpDelayForLevel(144) != 7
                || LegacyShipBehaviorCatalog.autoPumpDelayForLevel(145) != 3) {
            helper.fail("auto pump should preserve the legacy level-based bitmask delays");
            return;
        }

        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void legacyMoraleAndFoodSaturationCatalogMatches(GameTestHelper helper) {
        int[][] moraleBoundaries = {
                {5101, 0}, {5100, 1}, {3901, 1}, {3900, 2},
                {2101, 2}, {2100, 3}, {901, 3}, {900, 4}, {0, 4}
        };
        for (int[] boundary : moraleBoundaries) {
            if (LegacyShipBehaviorCatalog.legacyMoraleIdFor(boundary[0]) != boundary[1]) {
                helper.fail("legacy morale ID boundary mismatch at " + boundary[0]);
                return;
            }
        }

        int[][] foodMaxima = {
                {9, 14}, {10, 14}, {12, 18}, {20, 24}, {21, 16}, {26, 30},
                {27, 10}, {28, 22}, {29, 18}, {30, 35}, {31, 16}, {33, 30}, {37, 20},
                {46, 30}, {47, 30}, {48, 35}, {49, 17}, {56, 12}, {57, 12},
                {58, 16}, {59, 16}, {60, 19}, {61, 19}, {62, 19}, {63, 19}
        };
        for (int[] mapping : foodMaxima) {
            ShipEntitySpec spec = ShipEntitySpecs.findByLegacyClassId(mapping[0]);
            if (spec == null || LegacyShipBehaviorCatalog.foodSaturationMaxFor(spec) != mapping[1]) {
                helper.fail("legacy FoodMax mapping mismatch for class " + mapping[0]);
                return;
            }
        }

        ShipEntitySpec defaultSpec = ShipEntitySpecs.findByLegacyClassId(36);
        if (defaultSpec == null || LegacyShipBehaviorCatalog.foodSaturationMaxFor(defaultSpec) != 10) {
            helper.fail("unoverridden legacy classes should retain FoodMax 10");
            return;
        }

        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void shipAutoCombatRationMigratesAndKeepsLegacyCadence(GameTestHelper helper) {
        LegacyShipEntity migrated = ModEntityTypes.LEGACY_SHIP.get().create(helper.getLevel());
        if (migrated == null) {
            helper.fail("legacy ship should be creatable for combat ration state migration");
            return;
        }

        CompoundTag legacyTag = new CompoundTag();
        legacyTag.putInt("VariantEggMeta", 62);
        CompoundTag extended = new CompoundTag();
        CompoundTag minor = new CompoundTag();
        minor.putInt("AutoCR", 2);
        minor.putInt("Food", 7);
        extended.put("Minor", minor);
        legacyTag.put("ShipExtProps", extended);
        migrated.readAdditionalSaveData(legacyTag);
        if (migrated.getCombatRationLevel() != 2
                || migrated.getFoodSaturation() != 7
                || migrated.getFoodSaturationMax() != 19) {
            helper.fail("legacy AutoCR, Food, and class FoodMax should migrate together");
            return;
        }

        CompoundTag saved = new CompoundTag();
        migrated.addAdditionalSaveData(saved);
        if (saved.getInt("ShipCombatRationLevel") != 2 || saved.getInt("ShipFoodSaturation") != 7) {
            helper.fail("combat ration level and food saturation should save under modern tags");
            return;
        }

        CompoundTag precedenceTag = new CompoundTag();
        precedenceTag.putInt("VariantEggMeta", 62);
        precedenceTag.putInt("ShipCombatRationLevel", 4);
        precedenceTag.putInt("ShipFoodSaturation", 3);
        CompoundTag precedenceExtended = new CompoundTag();
        CompoundTag precedenceMinor = new CompoundTag();
        precedenceMinor.putInt("AutoCR", 1);
        precedenceMinor.putInt("Food", 9);
        precedenceExtended.put("Minor", precedenceMinor);
        precedenceTag.put("ShipExtProps", precedenceExtended);
        LegacyShipEntity precedence = ModEntityTypes.LEGACY_SHIP.get().create(helper.getLevel());
        if (precedence == null) {
            helper.fail("legacy ship should be creatable for modern combat ration precedence");
            return;
        }
        precedence.readAdditionalSaveData(precedenceTag);
        if (precedence.getCombatRationLevel() != 4 || precedence.getFoodSaturation() != 3) {
            helper.fail("modern combat ration state should take precedence over legacy Minor values");
            return;
        }

        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void shipTaskStateMigratesThroughEntityAndCache(GameTestHelper helper) {
        LegacyShipEntity migrated = ModEntityTypes.LEGACY_SHIP.get().create(helper.getLevel());
        if (migrated == null) {
            helper.fail("legacy ship should be creatable for task state migration");
            return;
        }

        int legacyTaskSide = (1 << 2) | (1 << 18) | (1 << 19) | (1 << 20);
        CompoundTag legacyTag = new CompoundTag();
        legacyTag.putInt("VariantEggMeta", 62);
        CompoundTag legacyExtended = new CompoundTag();
        CompoundTag legacyMinor = new CompoundTag();
        legacyMinor.putInt("Task", 3);
        legacyMinor.putInt("Side", legacyTaskSide);
        legacyExtended.put("Minor", legacyMinor);
        legacyTag.put("ShipExtProps", legacyExtended);
        migrated.readAdditionalSaveData(legacyTag);

        if (migrated.getTask() != 3 || migrated.getTaskSide() != legacyTaskSide) {
            helper.fail("legacy Task and Side should migrate from ShipExtProps.Minor");
            return;
        }

        CompoundTag saved = new CompoundTag();
        migrated.addAdditionalSaveData(saved);
        if (saved.getInt("ShipTask") != 3 || saved.getInt("ShipTaskSide") != legacyTaskSide) {
            helper.fail("task state should save under modern entity tags");
            return;
        }

        ShipWorldCacheEntry cached = ShipWorldCacheEntry.fromShip(migrated, false);
        cached.entityTag().remove("ShipTask");
        cached.entityTag().remove("ShipTaskSide");
        cached.entityTag().put("ShipExtProps", legacyExtended.copy());
        if (cached.getTask() != 3 || cached.getTaskSide() != legacyTaskSide) {
            helper.fail("detached ship cache should read legacy Task and Side values");
            return;
        }

        cached.entityTag().putInt("ShipTask", 4);
        cached.entityTag().putInt("ShipTaskSide", 7);
        if (cached.getTask() != 4 || cached.getTaskSide() != 7) {
            helper.fail("modern task state should take precedence over legacy Minor values");
            return;
        }

        LegacyShipEntity defaults = ModEntityTypes.LEGACY_SHIP.get().create(helper.getLevel());
        if (defaults == null || defaults.getTask() != LegacyShipEntity.DEFAULT_TASK
                || defaults.getTaskSide() != LegacyShipEntity.DEFAULT_TASK_SIDE) {
            helper.fail("new ships should retain legacy Task and Side defaults");
            return;
        }

        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void legacyTaskRuntimeKeepsOneTwelveCadenceAndTaskHands(GameTestHelper helper) {
        LegacyShipEntity ship = ModEntityTypes.LEGACY_SHIP.get().create(helper.getLevel());
        if (ship == null) {
            helper.fail("legacy ship should be creatable for task runtime state");
            return;
        }

        if (!LegacyShipBehaviorCatalog.isLegacyTaskUpdateTick(0)
                || LegacyShipBehaviorCatalog.isLegacyTaskUpdateTick(7)
                || !LegacyShipBehaviorCatalog.isLegacyTaskUpdateTick(8)) {
            helper.fail("TaskHelper should dispatch on every eighth tick");
            return;
        }

        ItemStack rod = new ItemStack(Items.FISHING_ROD);
        ItemStack fuel = new ItemStack(Items.COAL);
        ship.getShipInventory().setItem(LegacyShipEntity.LEGACY_MAIN_HAND_SLOT, rod);
        ship.getShipInventory().setItem(LegacyShipEntity.LEGACY_OFF_HAND_SLOT, fuel);
        ship.setLegacyTaskTime(1234);
        if (!ship.getLegacyTaskMainHandItem().is(Items.FISHING_ROD)
                || !ship.getLegacyTaskOffHandItem().is(Items.COAL)
                || ship.getLegacyTaskTime() != 1234) {
            helper.fail("task hands and TaskTime should use the legacy 22/23 slots");
            return;
        }

        CompoundTag saved = new CompoundTag();
        ship.addAdditionalSaveData(saved);
        LegacyShipEntity restored = ModEntityTypes.LEGACY_SHIP.get().create(helper.getLevel());
        if (restored == null) {
            helper.fail("legacy ship should be creatable for TaskTime restore");
            return;
        }
        restored.readAdditionalSaveData(saved);
        if (restored.getLegacyTaskTime() != 1234
                || !restored.getLegacyTaskMainHandItem().is(Items.FISHING_ROD)
                || !restored.getLegacyTaskOffHandItem().is(Items.COAL)) {
            helper.fail("TaskTime and task hand contents should survive entity save/load");
            return;
        }

        LegacyShipFishingHookEntity hook = LegacyShipFishingHookEntity.create(helper.getLevel(), ship);
        if (hook == null || ship.getFishingHook() != hook || hook.getHostId() != ship.getId()) {
            helper.fail("fishing hook should register its friendly ship host");
            return;
        }
        hook.discard();
        if (ship.getFishingHook() != null) {
            helper.fail("discarding a fishing hook should clear the ship reference");
            return;
        }

        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void legacyFishingTaskRequiresGuardAndThreeDeepWater(GameTestHelper helper) {
        LegacyShipEntity ship = ModEntityTypes.LEGACY_SHIP.get().create(helper.getLevel());
        if (ship == null) {
            helper.fail("legacy ship should be creatable for fishing task behavior");
            return;
        }

        BlockPos guard = new BlockPos(8, 2, 8);
        helper.getLevel().setBlock(guard, ModBlocks.BLOCK_WAYPOINT.get().defaultBlockState(), Block.UPDATE_ALL);
        helper.getLevel().setBlock(guard.offset(2, 1, 0), Blocks.WATER.defaultBlockState(), Block.UPDATE_ALL);
        helper.getLevel().setBlock(guard.offset(2, 0, 0), Blocks.WATER.defaultBlockState(), Block.UPDATE_ALL);
        helper.getLevel().setBlock(guard.offset(2, -1, 0), Blocks.WATER.defaultBlockState(), Block.UPDATE_ALL);
        ship.commandGuardBlock(guard, helper.getLevel().dimension().location().toString());
        ship.setTask(2);
        ship.setGrudge(100);
        ship.getShipInventory().setItem(LegacyShipEntity.LEGACY_MAIN_HAND_SLOT,
                new ItemStack(Items.FISHING_ROD));
        ship.moveTo(guard.getX() + 0.5D, guard.getY(), guard.getZ() + 0.5D, 0.0F, 0.0F);
        ship.setDeltaMovement(Vec3.ZERO);

        LegacyShipBehaviorCatalog.tickLegacyTask(ship);
        LegacyShipFishingHookEntity hook = ship.getFishingHook();
        if (hook == null || !hook.getHostShip().equals(ship)) {
            helper.fail("fishing task should launch a hook only at a guarded three-deep water source");
            return;
        }

        hook.discard();
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void legacyCookingTaskMovesFurnaceItemsAndAppliesRewards(GameTestHelper helper) {
        LegacyShipEntity ship = ModEntityTypes.LEGACY_SHIP.get().create(helper.getLevel());
        if (ship == null) {
            helper.fail("legacy ship should be creatable for cooking task behavior");
            return;
        }

        BlockPos guard = new BlockPos(8, 2, 8);
        BlockPos furnacePos = guard.offset(2, 0, 0);
        helper.getLevel().setBlock(guard, ModBlocks.BLOCK_WAYPOINT.get().defaultBlockState(), Block.UPDATE_ALL);
        helper.getLevel().setBlock(furnacePos, Blocks.FURNACE.defaultBlockState(), Block.UPDATE_ALL);
        if (!(helper.getLevel().getBlockEntity(guard) instanceof WaypointBlockEntity waypoint)
                || !(helper.getLevel().getBlockEntity(furnacePos) instanceof AbstractFurnaceBlockEntity furnace)) {
            helper.fail("cooking task should use a waypoint paired with a furnace block entity");
            return;
        }
        waypoint.setPairedChest(furnacePos);

        ship.commandGuardBlock(guard, helper.getLevel().dimension().location().toString());
        ship.setTask(1);
        ship.setGrudge(100);
        ship.setMorale(1000);
        ship.moveTo(furnacePos.getX() + 0.5D, furnacePos.getY(), furnacePos.getZ() + 0.5D, 0.0F, 0.0F);
        ship.setDeltaMovement(Vec3.ZERO);
        ship.getShipInventory().setItem(6, new ItemStack(Items.BEEF, 2));
        ship.getShipInventory().setItem(7, new ItemStack(Items.COAL));
        ship.getShipInventory().setItem(LegacyShipEntity.LEGACY_MAIN_HAND_SLOT, new ItemStack(Items.BEEF));
        ship.getShipInventory().setItem(LegacyShipEntity.LEGACY_OFF_HAND_SLOT, new ItemStack(Items.COAL));
        furnace.setItem(2, new ItemStack(Items.COOKED_BEEF));

        LegacyShipBehaviorCatalog.tickLegacyTask(ship);

        boolean cookedMoved = false;
        for (int slot = LegacyShipEntity.EQUIPMENT_SLOT_COUNT;
             slot < LegacyShipEntity.SHIP_SLOT_COUNT; slot++) {
            if (ship.getShipInventory().getItem(slot).is(Items.COOKED_BEEF)) {
                cookedMoved = true;
                break;
            }
        }
        if (!cookedMoved || !furnace.getItem(0).is(Items.BEEF) || !furnace.getItem(1).is(Items.COAL)
                || furnace.getItem(2).is(Items.COOKED_BEEF)
                || ship.getGrudge() != 97 || ship.getMorale() != 1100) {
            helper.fail("cooking task should transfer input, fuel, and output while applying legacy rewards");
            return;
        }

        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void legacyMiningTaskUsesRockThresholdAndTaskTime(GameTestHelper helper) {
        LegacyShipEntity ship = ModEntityTypes.LEGACY_SHIP.get().create(helper.getLevel());
        if (ship == null) {
            helper.fail("legacy ship should be creatable for mining task behavior");
            return;
        }

        BlockPos origin = new BlockPos(8, 8, 8);
        for (int dy = -3; dy < 0; dy++) {
            for (int dx = -3; dx <= 3; dx++) {
                for (int dz = -3; dz <= 3; dz++) {
                    helper.getLevel().setBlock(origin.offset(dx, dy, dz), Blocks.STONE.defaultBlockState(), Block.UPDATE_ALL);
                }
            }
        }

        ship.setTask(3);
        ship.setGrudge(1000);
        ship.setMorale(5000);
        ship.setLegacyTaskTime(0);
        ship.getShipInventory().setItem(LegacyShipEntity.LEGACY_MAIN_HAND_SLOT,
                new ItemStack(Items.WOODEN_PICKAXE));
        ship.moveTo(origin.getX() + 0.5D, origin.getY(), origin.getZ() + 0.5D, 0.0F, 0.0F);
        ship.setDeltaMovement(Vec3.ZERO);
        ship.tickCount = 1056;

        LegacyShipBehaviorCatalog.tickLegacyTask(ship);

        boolean hasMiningResult = false;
        for (int slot = LegacyShipEntity.EQUIPMENT_SLOT_COUNT;
             slot < LegacyShipEntity.SHIP_SLOT_COUNT; slot++) {
            if (!ship.getShipInventory().getItem(slot).isEmpty()) {
                hasMiningResult = true;
                break;
            }
        }
        if (!hasMiningResult || ship.getLegacyTaskTime() != 1056
                || ship.getGrudge() != 700 || ship.getMorale() != 4800) {
            helper.fail("mining task should require rock density, update TaskTime, and apply legacy rewards");
            return;
        }

        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void legacyCraftingTaskUsesRecipePaperAndTaskMaterialSlots(GameTestHelper helper) {
        LegacyShipEntity ship = ModEntityTypes.LEGACY_SHIP.get().create(helper.getLevel());
        if (ship == null) {
            helper.fail("legacy ship should be creatable for crafting task behavior");
            return;
        }

        BlockPos guard = new BlockPos(8, 2, 8);
        BlockPos chestPos = guard.offset(2, 0, 0);
        helper.getLevel().setBlock(guard, ModBlocks.BLOCK_WAYPOINT.get().defaultBlockState(), Block.UPDATE_ALL);
        helper.getLevel().setBlock(chestPos, Blocks.CHEST.defaultBlockState(), Block.UPDATE_ALL);
        if (!(helper.getLevel().getBlockEntity(guard) instanceof WaypointBlockEntity waypoint)
                || !(helper.getLevel().getBlockEntity(chestPos) instanceof Container chest)) {
            helper.fail("crafting task should use a waypoint paired with an inventory");
            return;
        }
        waypoint.setPairedChest(chestPos);
        chest.setItem(0, new ItemStack(Items.COAL));
        chest.setItem(1, new ItemStack(Items.STICK));

        CompoundTag paperTag = new CompoundTag();
        ListTag recipeEntries = new ListTag();
        CompoundTag coalEntry = new CompoundTag();
        coalEntry.putInt("Slot", 1);
        new ItemStack(Items.COAL).save(coalEntry);
        recipeEntries.add(coalEntry);
        CompoundTag stickEntry = new CompoundTag();
        stickEntry.putInt("Slot", 4);
        new ItemStack(Items.STICK).save(stickEntry);
        recipeEntries.add(stickEntry);
        paperTag.put("Recipe", recipeEntries);
        ItemStack paper = new ItemStack(ModItems.RECIPEPAPER.get());
        paper.setTag(paperTag);

        ship.commandGuardBlock(guard, helper.getLevel().dimension().location().toString());
        ship.setTask(4);
        ship.setGrudge(100);
        ship.setMorale(1000);
        ship.moveTo(chestPos.getX() + 0.5D, chestPos.getY(), chestPos.getZ() + 0.5D, 0.0F, 0.0F);
        ship.setDeltaMovement(Vec3.ZERO);
        ship.getShipInventory().setItem(LegacyShipEntity.LEGACY_MAIN_HAND_SLOT, paper);

        LegacyShipBehaviorCatalog.tickLegacyTask(ship);

        boolean torchCreated = false;
        for (int slot = 0; slot < chest.getContainerSize(); slot++) {
            if (chest.getItem(slot).is(Items.TORCH)) {
                torchCreated = true;
                break;
            }
        }
        if (!torchCreated || countItems(chest, Items.COAL) != 0 || countItems(chest, Items.STICK) != 0
                || !ship.getShipInventory().getItem(13).isEmpty()
                || !ship.getShipInventory().getItem(16).isEmpty()
                || ship.getGrudge() != 98 || ship.getMorale() != 990) {
            helper.fail("crafting task should consume chest materials through slots 12-20 and return the recipe result");
            return;
        }

        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void shipGuardBlockStateMigratesThroughEntityAndCache(GameTestHelper helper) {
        LegacyShipEntity migrated = ModEntityTypes.LEGACY_SHIP.get().create(helper.getLevel());
        if (migrated == null) {
            helper.fail("legacy ship should be creatable for guard block state migration");
            return;
        }

        BlockPos guardPos = new BlockPos(7, 2, 9);
        CompoundTag legacyTag = new CompoundTag();
        legacyTag.putInt("VariantEggMeta", 62);
        CompoundTag legacyExtended = new CompoundTag();
        CompoundTag legacyMinor = new CompoundTag();
        legacyMinor.putInt("GuardX", guardPos.getX());
        legacyMinor.putInt("GuardY", guardPos.getY());
        legacyMinor.putInt("GuardZ", guardPos.getZ());
        legacyMinor.putInt("GuardDim", 0);
        legacyMinor.putInt("GuardID", -1);
        legacyMinor.putInt("GuardType", 1);
        legacyExtended.put("Minor", legacyMinor);
        legacyTag.put("ShipExtProps", legacyExtended);
        migrated.readAdditionalSaveData(legacyTag);

        if (!guardPos.equals(migrated.getGuardedBlockPos())
                || !"minecraft:overworld".equals(migrated.getGuardedBlockDimension())
                || migrated.getCommandedPos() != null
                || !migrated.hasActiveCommandState()) {
            helper.fail("legacy GuardX/Y/Z/Dim/Type should become a persistent modern block guard");
            return;
        }

        CompoundTag saved = new CompoundTag();
        migrated.addAdditionalSaveData(saved);
        if (BlockPos.of(saved.getLong("GuardBlockPos")).equals(guardPos) == false
                || !"minecraft:overworld".equals(saved.getString("GuardBlockDim"))) {
            helper.fail("block guard state should save under modern guard tags");
            return;
        }

        ShipWorldCacheEntry cached = ShipWorldCacheEntry.fromShip(migrated, false);
        cached.entityTag().remove("GuardBlockPos");
        cached.entityTag().remove("GuardBlockDim");
        cached.entityTag().put("ShipExtProps", legacyExtended.copy());
        if (!guardPos.equals(cached.getGuardedBlockPos())
                || !"minecraft:overworld".equals(cached.getGuardedBlockDimension())
                || !cached.hasCommandState()) {
            helper.fail("detached ship cache should read legacy block guard state");
            return;
        }

        CompoundTag precedenceTag = legacyTag.copy();
        BlockPos movePos = new BlockPos(12, 2, 12);
        precedenceTag.putLong("CommandPos", movePos.asLong());
        LegacyShipEntity precedence = ModEntityTypes.LEGACY_SHIP.get().create(helper.getLevel());
        if (precedence == null) {
            helper.fail("legacy ship should be creatable for guard state precedence");
            return;
        }
        precedence.readAdditionalSaveData(precedenceTag);
        if (!movePos.equals(precedence.getCommandedPos()) || precedence.getGuardedBlockPos() != null) {
            helper.fail("modern command state should take precedence over legacy Guard fields");
            return;
        }

        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void shipDisplayStateMigratesThroughEntityAndCache(GameTestHelper helper) {
        LegacyShipEntity migrated = ModEntityTypes.LEGACY_SHIP.get().create(helper.getLevel());
        if (migrated == null) {
            helper.fail("legacy ship should be creatable for display state migration");
            return;
        }

        CompoundTag legacyTag = new CompoundTag();
        legacyTag.putInt("VariantEggMeta", 62);
        CompoundTag legacyExtended = new CompoundTag();
        CompoundTag legacyDisplay = new CompoundTag();
        legacyDisplay.putInt("State", 0x2A);
        legacyDisplay.putInt("HPState", 2);
        legacyDisplay.putInt("Emotion", 7);
        legacyDisplay.putInt("Emotion2", 1);
        legacyDisplay.putInt("Phase", -1);
        legacyExtended.put("Display", legacyDisplay);
        legacyTag.put("ShipExtProps", legacyExtended);
        migrated.readAdditionalSaveData(legacyTag);

        if (migrated.getModelState() != 0x2A
                || migrated.getHpState() != 2
                || migrated.getEmotion() != 7
                || migrated.getEmotion2() != 1
                || migrated.getDisplayPhase() != -1) {
            helper.fail("legacy ShipExtProps.Display values should migrate without clamping or remapping");
            return;
        }

        CompoundTag saved = new CompoundTag();
        migrated.addAdditionalSaveData(saved);
        if (saved.getInt("ShipModelState") != 0x2A
                || saved.getInt("ShipHpState") != 2
                || saved.getInt("ShipEmotion") != 7
                || saved.getInt("ShipEmotion2") != 1
                || saved.getInt("ShipPhase") != -1) {
            helper.fail("display state should save under modern entity tags");
            return;
        }

        ShipWorldCacheEntry cached = ShipWorldCacheEntry.fromShip(migrated, false);
        cached.entityTag().remove("ShipModelState");
        cached.entityTag().remove("ShipHpState");
        cached.entityTag().remove("ShipEmotion");
        cached.entityTag().remove("ShipEmotion2");
        cached.entityTag().remove("ShipPhase");
        cached.entityTag().put("ShipExtProps", legacyExtended.copy());
        if (cached.getModelState() != 0x2A
                || cached.getHpState() != 2
                || cached.getEmotion() != 7
                || cached.getEmotion2() != 1
                || cached.getDisplayPhase() != -1) {
            helper.fail("detached ship cache should read legacy Display values");
            return;
        }

        CompoundTag precedenceTag = legacyTag.copy();
        precedenceTag.putInt("ShipModelState", 3);
        precedenceTag.putInt("ShipHpState", 1);
        precedenceTag.putInt("ShipEmotion", 2);
        precedenceTag.putInt("ShipEmotion2", 0);
        precedenceTag.putInt("ShipPhase", 2);
        LegacyShipEntity precedence = ModEntityTypes.LEGACY_SHIP.get().create(helper.getLevel());
        if (precedence == null) {
            helper.fail("legacy ship should be creatable for display state precedence");
            return;
        }
        precedence.readAdditionalSaveData(precedenceTag);
        if (precedence.getModelState() != 3
                || precedence.getHpState() != 1
                || precedence.getEmotion() != 2
                || precedence.getEmotion2() != 0
                || precedence.getDisplayPhase() != 2) {
            helper.fail("modern display state should take precedence over legacy Display values");
            return;
        }

        LegacyShipEntity defaults = ModEntityTypes.LEGACY_SHIP.get().create(helper.getLevel());
        if (defaults == null
                || defaults.getModelState() != LegacyShipEntity.DEFAULT_MODEL_STATE
                || defaults.getHpState() != LegacyShipEntity.DEFAULT_HP_STATE
                || defaults.getEmotion() != LegacyShipEntity.DEFAULT_EMOTION
                || defaults.getEmotion2() != LegacyShipEntity.DEFAULT_EMOTION2
                || defaults.getDisplayPhase() != LegacyShipEntity.DEFAULT_DISPLAY_PHASE) {
            helper.fail("new ships should retain legacy display state defaults");
            return;
        }

        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void shipAttackModeFlagsMigrateAndGateAutonomousCombat(GameTestHelper helper) {
        LegacyShipEntity migrated = ModEntityTypes.LEGACY_SHIP.get().create(helper.getLevel());
        if (migrated == null) {
            helper.fail("legacy ship should be creatable for attack mode migration");
            return;
        }

        CompoundTag legacyTag = new CompoundTag();
        legacyTag.putInt("VariantEggMeta", 53);
        CompoundTag legacyExtended = new CompoundTag();
        CompoundTag legacyFlags = new CompoundTag();
        legacyFlags.putBoolean("Melee", true);
        legacyFlags.putBoolean("AmmoL", false);
        legacyFlags.putBoolean("AmmoH", true);
        legacyFlags.putBoolean("AirL", false);
        legacyFlags.putBoolean("AirH", true);
        legacyExtended.put("ShipFlags", legacyFlags);
        legacyTag.put("ShipExtProps", legacyExtended);
        migrated.readAdditionalSaveData(legacyTag);
        if (!migrated.useAttackMelee()
                || migrated.useAttackLight()
                || !migrated.useAttackHeavy()
                || migrated.useAttackAirLight()
                || !migrated.useAttackAirHeavy()) {
            helper.fail("legacy ShipFlags attack channels should migrate without changing their values");
            return;
        }

        CompoundTag saved = new CompoundTag();
        migrated.addAdditionalSaveData(saved);
        if (!saved.getBoolean("ShipUseMelee")
                || saved.getBoolean("ShipUseAmmoLight")
                || !saved.getBoolean("ShipUseAmmoHeavy")
                || saved.getBoolean("ShipUseAirLight")
                || !saved.getBoolean("ShipUseAirHeavy")) {
            helper.fail("modern save data should persist all attack mode switches");
            return;
        }

        ShipWorldCacheEntry cached = ShipWorldCacheEntry.fromShip(migrated, false);
        cached.entityTag().remove("ShipUseMelee");
        cached.entityTag().remove("ShipUseAmmoLight");
        cached.entityTag().remove("ShipUseAmmoHeavy");
        cached.entityTag().remove("ShipUseAirLight");
        cached.entityTag().remove("ShipUseAirHeavy");
        cached.entityTag().put("ShipExtProps", legacyExtended.copy());
        if (!cached.useAttackMelee()
                || cached.useAttackLight()
                || !cached.useAttackHeavy()
                || cached.useAttackAirLight()
                || !cached.useAttackAirHeavy()) {
            helper.fail("detached ship cache should read legacy attack mode switches");
            return;
        }

        LegacyShipEntity defaultShip = ModEntityTypes.LEGACY_SHIP.get().create(helper.getLevel());
        if (defaultShip == null
                || defaultShip.useAttackMelee()
                || !defaultShip.useAttackLight()
                || !defaultShip.useAttackHeavy()
                || !defaultShip.useAttackAirLight()
                || !defaultShip.useAttackAirHeavy()) {
            helper.fail("new ships should retain legacy attack mode defaults");
            return;
        }

        migrated.setUseAttackMelee(false);
        supplyShipForTest(migrated);
        migrated.setUseAttackHeavy(false);
        migrated.setUseAttackAirHeavy(false);
        migrated.setUseAttackLight(true);
        if (!migrated.canUseCompatRangedCombat()) {
            helper.fail("an enabled ranged attack channel should keep autonomous combat available");
            return;
        }
        migrated.setUseAttackLight(false);
        if (migrated.canUseCompatRangedCombat()) {
            helper.fail("disabling every ranged attack channel should stop autonomous ranged combat");
            return;
        }

        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void passiveAiMigratesToModernAutoTargetState(GameTestHelper helper) {
        LegacyShipEntity passive = ModEntityTypes.LEGACY_SHIP.get().create(helper.getLevel());
        LegacyShipEntity active = ModEntityTypes.LEGACY_SHIP.get().create(helper.getLevel());
        if (passive == null || active == null) {
            helper.fail("ships should be creatable for PassiveAI migration");
            return;
        }

        CompoundTag passiveLegacy = new CompoundTag();
        passiveLegacy.putInt("VariantEggMeta", 53);
        CompoundTag passiveExtended = new CompoundTag();
        CompoundTag passiveFlags = new CompoundTag();
        passiveFlags.putBoolean("PassiveAI", true);
        passiveExtended.put("ShipFlags", passiveFlags);
        passiveLegacy.put("ShipExtProps", passiveExtended);
        passive.readAdditionalSaveData(passiveLegacy);
        if (passive.isAiAutoTarget()) {
            helper.fail("legacy PassiveAI should disable modern automatic enemy targeting");
            return;
        }

        CompoundTag saved = new CompoundTag();
        passive.addAdditionalSaveData(saved);
        if (saved.getBoolean("AiAutoTarget")) {
            helper.fail("modern save should persist the migrated passive targeting state");
            return;
        }

        ShipWorldCacheEntry cached = ShipWorldCacheEntry.fromShip(passive, false);
        cached.entityTag().remove("AiAutoTarget");
        cached.entityTag().put("ShipExtProps", passiveExtended.copy());
        if (cached.isAiAutoTarget()) {
            helper.fail("detached ship cache should preserve legacy PassiveAI semantics");
            return;
        }

        CompoundTag modernWins = passiveLegacy.copy();
        modernWins.putBoolean("AiAutoTarget", true);
        active.readAdditionalSaveData(modernWins);
        if (!active.isAiAutoTarget()) {
            helper.fail("modern AiAutoTarget should take precedence over legacy PassiveAI");
            return;
        }

        LegacyShipEntity defaults = ModEntityTypes.LEGACY_SHIP.get().create(helper.getLevel());
        if (defaults == null || !defaults.isAiAutoTarget()) {
            helper.fail("new ships should retain the legacy active-targeting default");
            return;
        }

        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void onSightChaseMigratesAndDropsLostSightTargets(GameTestHelper helper) {
        LegacyShipEntity migrated = ModEntityTypes.LEGACY_SHIP.get().create(helper.getLevel());
        LegacyShipEntity hostile = ModEntityTypes.LEGACY_SHIP.get().create(helper.getLevel());
        Zombie target = EntityType.ZOMBIE.create(helper.getLevel());
        if (migrated == null || hostile == null || target == null) {
            helper.fail("ships and zombie should be creatable for OnSightChase migration");
            return;
        }

        CompoundTag legacyTag = new CompoundTag();
        legacyTag.putInt("VariantEggMeta", 53);
        CompoundTag legacyExtended = new CompoundTag();
        CompoundTag legacyFlags = new CompoundTag();
        legacyFlags.putBoolean("OnSight", false);
        legacyExtended.put("ShipFlags", legacyFlags);
        legacyTag.put("ShipExtProps", legacyExtended);
        migrated.readAdditionalSaveData(legacyTag);
        if (migrated.isAiOnSightChase()) {
            helper.fail("legacy OnSight=false should disable the modern sight-chase rule");
            return;
        }

        CompoundTag saved = new CompoundTag();
        migrated.addAdditionalSaveData(saved);
        if (saved.getBoolean("AiOnSightChase")) {
            helper.fail("modern save should persist the migrated OnSight=false state");
            return;
        }

        ShipWorldCacheEntry cached = ShipWorldCacheEntry.fromShip(migrated, false);
        cached.entityTag().remove("AiOnSightChase");
        cached.entityTag().put("ShipExtProps", legacyExtended.copy());
        if (cached.isAiOnSightChase()) {
            helper.fail("detached ship cache should preserve legacy OnSight=false");
            return;
        }

        CompoundTag modernWins = legacyTag.copy();
        modernWins.putBoolean("AiOnSightChase", true);
        LegacyShipEntity modern = ModEntityTypes.LEGACY_SHIP.get().create(helper.getLevel());
        if (modern == null) {
            helper.fail("modern precedence ship should be creatable");
            return;
        }
        modern.readAdditionalSaveData(modernWins);
        if (!modern.isAiOnSightChase()) {
            helper.fail("modern AiOnSightChase should take precedence over legacy OnSight");
            return;
        }

        hostile.setVariantEggMeta(2049);
        hostile.setAiOnSightChase(true);
        if (hostile.isAiOnSightChase()) {
            helper.fail("hostile ship classes should retain the legacy OnSightChase=false override");
            return;
        }

        migrated.setVariantEggMeta(53);
        migrated.setOwner(UUID.randomUUID(), "OnSightTester");
        migrated.setPos(1.5D, 2.0D, 1.5D);
        target.setPos(5.5D, 2.0D, 1.5D);
        helper.getLevel().addFreshEntity(migrated);
        helper.getLevel().addFreshEntity(target);
        for (int y = 1; y <= 4; y++) {
            helper.getLevel().setBlock(new BlockPos(3, y, 1), Blocks.STONE.defaultBlockState(), Block.UPDATE_ALL);
        }
        migrated.setLightAmmo(20);
        migrated.setHeavyAmmo(20);
        migrated.setGrudge(20);
        migrated.setAiOnSightChase(true);
        migrated.setTarget(target);
        LegacyShipRangedAttackGoal goal = new LegacyShipRangedAttackGoal(migrated, 1.0D);
        if (migrated.getSensing().hasLineOfSight(target) || !goal.canUse()) {
            helper.fail("OnSightChase behavior test should start with a blocked but valid target");
            return;
        }
        goal.start();
        goal.tick();
        if (migrated.getTarget() != null) {
            helper.fail("OnSightChase should clear a friendly ship target after line of sight is lost");
            return;
        }

        migrated.setAiOnSightChase(false);
        migrated.setTarget(target);
        goal.start();
        goal.tick();
        if (migrated.getTarget() != target) {
            helper.fail("disabled OnSightChase should allow the friendly ship to retain a blocked target");
            return;
        }

        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void shipTargetClassFlagsMigrateAndCache(GameTestHelper helper) {
        LegacyShipEntity migrated = ModEntityTypes.LEGACY_SHIP.get().create(helper.getLevel());
        if (migrated == null) {
            helper.fail("legacy ship should be creatable for target-class flag migration");
            return;
        }

        CompoundTag legacyTag = new CompoundTag();
        legacyTag.putInt("VariantEggMeta", 53);
        CompoundTag legacyExtended = new CompoundTag();
        CompoundTag legacyFlags = new CompoundTag();
        legacyFlags.putBoolean("PVPFirst", false);
        legacyFlags.putBoolean("AA", true);
        legacyFlags.putBoolean("ASM", true);
        legacyExtended.put("ShipFlags", legacyFlags);
        legacyTag.put("ShipExtProps", legacyExtended);
        migrated.readAdditionalSaveData(legacyTag);
        if (migrated.isAiPvpFirst() || !migrated.isAiAntiAir() || !migrated.isAiAntiSub()) {
            helper.fail("legacy PVPFirst, AA, and ASM flags should migrate to modern state");
            return;
        }

        CompoundTag saved = new CompoundTag();
        migrated.addAdditionalSaveData(saved);
        if (!saved.contains("AiPvpFirst") || saved.getBoolean("AiPvpFirst")
                || !saved.contains("AiAntiAir") || !saved.getBoolean("AiAntiAir")
                || !saved.contains("AiAntiSub") || !saved.getBoolean("AiAntiSub")) {
            helper.fail("modern save should persist migrated target-class flags");
            return;
        }

        ShipWorldCacheEntry cached = ShipWorldCacheEntry.fromShip(migrated, false);
        cached.entityTag().remove("AiPvpFirst");
        cached.entityTag().remove("AiAntiAir");
        cached.entityTag().remove("AiAntiSub");
        cached.entityTag().put("ShipExtProps", legacyExtended.copy());
        if (cached.isAiPvpFirst() || !cached.isAiAntiAir() || !cached.isAiAntiSub()) {
            helper.fail("detached ship cache should preserve legacy target-class flags");
            return;
        }

        CompoundTag modernWins = legacyTag.copy();
        modernWins.putBoolean("AiPvpFirst", true);
        modernWins.putBoolean("AiAntiAir", false);
        modernWins.putBoolean("AiAntiSub", false);
        LegacyShipEntity modern = ModEntityTypes.LEGACY_SHIP.get().create(helper.getLevel());
        if (modern == null) {
            helper.fail("modern precedence ship should be creatable");
            return;
        }
        modern.readAdditionalSaveData(modernWins);
        if (!modern.isAiPvpFirst() || modern.isAiAntiAir() || modern.isAiAntiSub()) {
            helper.fail("modern target-class flags should take precedence over legacy flags");
            return;
        }

        LegacyShipEntity defaults = ModEntityTypes.LEGACY_SHIP.get().create(helper.getLevel());
        if (defaults == null || !defaults.isAiPvpFirst()
                || defaults.isAiAntiAir() || defaults.isAiAntiSub()) {
            helper.fail("new ships should retain legacy target-class flag defaults");
            return;
        }

        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void shipRangeTargetUsesLegacyCategoryPriority(GameTestHelper helper) {
        LegacyShipEntity ship = ModEntityTypes.LEGACY_SHIP.get().create(helper.getLevel());
        LegacyShipEntity hostileSubmarine = ModEntityTypes.LEGACY_SHIP.get().create(helper.getLevel());
        LegacyShipEntity hostileSurfaceShip = ModEntityTypes.LEGACY_SHIP.get().create(helper.getLevel());
        LivingEntity normalTarget = EntityType.CREEPER.create(helper.getLevel());
        LivingEntity airTarget = EntityType.PHANTOM.create(helper.getLevel());
        if (ship == null || hostileSubmarine == null || hostileSurfaceShip == null
                || normalTarget == null || airTarget == null) {
            helper.fail("legacy range target test entities should be creatable");
            return;
        }

        BlockPos origin = helper.absolutePos(new BlockPos(1, 2, 1));
        double originX = origin.getX() + 0.5D;
        double originY = origin.getY();
        double originZ = origin.getZ() + 0.5D;
        ship.setVariantEggMeta(53);
        ship.setPos(originX, originY, originZ);
        hostileSubmarine.setVariantEggMeta(2040);
        hostileSubmarine.setPos(originX + 3.0D, originY, originZ);
        hostileSurfaceShip.setVariantEggMeta(2053);
        hostileSurfaceShip.setPos(originX + 4.0D, originY, originZ);
        normalTarget.setPos(originX + 2.0D, originY, originZ);
        airTarget.setPos(originX + 1.0D, originY, originZ);
        if (normalTarget instanceof Mob normalMob) {
            normalMob.setNoAi(true);
        }
        if (airTarget instanceof Mob airMob) {
            airMob.setNoAi(true);
        }
        ship.setNoAi(true);
        hostileSubmarine.setNoAi(true);
        hostileSurfaceShip.setNoAi(true);
        helper.getLevel().addFreshEntity(ship);
        helper.getLevel().addFreshEntity(hostileSubmarine);
        helper.getLevel().addFreshEntity(hostileSurfaceShip);
        helper.getLevel().addFreshEntity(normalTarget);
        helper.getLevel().addFreshEntity(airTarget);

        ship.setAiAntiAir(true);
        ship.setAiAntiSub(true);
        ship.setAiPvpFirst(true);
        LivingEntity antiAirTarget = LegacyShipBehaviorCatalog.findLegacyRangeTarget(ship);
        if (antiAirTarget == null || !LegacyShipCombatHelper.isFlyingTarget(antiAirTarget)) {
            helper.fail("AntiAir should have the first legacy automatic target priority");
            return;
        }

        ship.setAiAntiAir(false);
        LivingEntity antiSubTarget = LegacyShipBehaviorCatalog.findLegacyRangeTarget(ship);
        if (antiSubTarget == null || !LegacyShipBehaviorCatalog.isLegacyUnderseaTarget(antiSubTarget)) {
            helper.fail("AntiSS should be selected before PVPFirst and normal targets");
            return;
        }

        ship.setAiAntiSub(false);
        LivingEntity pvpTarget = LegacyShipBehaviorCatalog.findLegacyRangeTarget(ship);
        if (!(pvpTarget instanceof LegacyShipEntity selectedShip)
                || selectedShip.isHostileVariant() == ship.isHostileVariant()
                || LegacyShipBehaviorCatalog.isLegacyUnderseaTarget(pvpTarget)) {
            helper.fail("PVPFirst should select hostile ships before normal targets");
            return;
        }

        ship.setAiPvpFirst(false);
        LivingEntity normalTargetSelection = LegacyShipBehaviorCatalog.findLegacyRangeTarget(ship);
        if (normalTargetSelection == null
                || !(normalTargetSelection instanceof Enemy)
                || LegacyShipBehaviorCatalog.isLegacyUnderseaTarget(normalTargetSelection)) {
            helper.fail("normal target selection should remain after disabled special categories");
            return;
        }

        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void guardTargetUsesLegacyGuardingBounds(GameTestHelper helper) {
        Player player = helper.makeMockPlayer();
        BlockPos origin = helper.absolutePos(new BlockPos(1, 2, 1));
        LegacyShipEntity ship = createOwnedShip(helper, player, 53,
                origin.getX() + 0.5D, origin.getY(), origin.getZ() + 0.5D);
        Zombie target = new Zombie(EntityType.ZOMBIE, helper.getLevel());
        if (ship == null) {
            return;
        }

        double range = Math.max(1.0D, (int) ship.getCompatAttackRange());
        target.setNoAi(true);
        double targetMinOffset = target.getBoundingBox().minY - target.getY();
        double targetY = ship.getBoundingBox().maxY + range * 0.6D + 0.25D - targetMinOffset;
        target.setPos(origin.getX() + 0.5D, targetY, origin.getZ() + 0.5D);
        helper.getLevel().addFreshEntity(target);

        if (LegacyShipBehaviorCatalog.findLegacyRangeTarget(ship) != target) {
            helper.fail("the regular legacy target box should include a target at 0.7 times range vertically");
            return;
        }
        if (LegacyShipBehaviorCatalog.findLegacyGuardTarget(ship) != null) {
            helper.fail("the moving guard target box should stop at 0.6 times range vertically");
            return;
        }

        target.setPos(origin.getX() + 0.5D, origin.getY(), origin.getZ() + 0.5D);
        if (LegacyShipBehaviorCatalog.findLegacyGuardTarget(ship) != target) {
            helper.fail("the moving guard target box should include an in-range target");
            return;
        }

        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void shipShowHeldItemUsesLegacyFlagAndCache(GameTestHelper helper) {
        LegacyShipEntity migrated = ModEntityTypes.LEGACY_SHIP.get().create(helper.getLevel());
        if (migrated == null) {
            helper.fail("legacy ship should be creatable for ShowHeldItem migration");
            return;
        }

        CompoundTag legacyTag = new CompoundTag();
        legacyTag.putInt("VariantEggMeta", 53);
        CompoundTag legacyExtended = new CompoundTag();
        CompoundTag legacyFlags = new CompoundTag();
        legacyFlags.putBoolean("HeldItem", false);
        legacyExtended.put("ShipFlags", legacyFlags);
        legacyTag.put("ShipExtProps", legacyExtended);
        migrated.readAdditionalSaveData(legacyTag);
        if (migrated.canShowHeldItem()) {
            helper.fail("legacy ShipFlags.HeldItem=false should disable held-item display");
            return;
        }

        CompoundTag saved = new CompoundTag();
        migrated.addAdditionalSaveData(saved);
        if (!saved.contains("ShipShowHeldItem") || saved.getBoolean("ShipShowHeldItem")) {
            helper.fail("modern save should persist the migrated held-item display gate");
            return;
        }

        ShipWorldCacheEntry cached = ShipWorldCacheEntry.fromShip(migrated, false);
        cached.entityTag().remove("ShipShowHeldItem");
        cached.entityTag().put("ShipExtProps", legacyExtended.copy());
        if (cached.isShowHeldItem()) {
            helper.fail("detached ship cache should preserve legacy HeldItem=false");
            return;
        }

        CompoundTag modernWins = legacyTag.copy();
        modernWins.putBoolean("ShipShowHeldItem", true);
        LegacyShipEntity modern = ModEntityTypes.LEGACY_SHIP.get().create(helper.getLevel());
        if (modern == null) {
            helper.fail("modern precedence ship should be creatable");
            return;
        }
        modern.readAdditionalSaveData(modernWins);
        if (!modern.canShowHeldItem()) {
            helper.fail("modern ShipShowHeldItem should take precedence over legacy HeldItem");
            return;
        }

        LegacyShipEntity defaults = ModEntityTypes.LEGACY_SHIP.get().create(helper.getLevel());
        if (defaults == null || !defaults.canShowHeldItem()) {
            helper.fail("new ships should retain the legacy held-item display default");
            return;
        }

        migrated.setShowHeldItem(true);
        if (!migrated.canShowHeldItem()) {
            helper.fail("the modern held-item display setter should update the synced gate");
            return;
        }

        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void shipCanFloatStateAndLegacyDepthRules(GameTestHelper helper) {
        LegacyShipEntity migrated = ModEntityTypes.LEGACY_SHIP.get().create(helper.getLevel());
        if (migrated == null) {
            helper.fail("legacy ship should be creatable for CanFloatUp migration");
            return;
        }

        CompoundTag legacyTag = new CompoundTag();
        legacyTag.putInt("VariantEggMeta", 53);
        CompoundTag legacyExtended = new CompoundTag();
        CompoundTag legacyFlags = new CompoundTag();
        legacyFlags.putBoolean("CanFloat", true);
        legacyExtended.put("ShipFlags", legacyFlags);
        legacyTag.put("ShipExtProps", legacyExtended);
        migrated.readAdditionalSaveData(legacyTag);
        if (!migrated.canFloatUp()) {
            helper.fail("legacy ShipFlags.CanFloat should migrate to the modern state");
            return;
        }

        CompoundTag saved = new CompoundTag();
        migrated.addAdditionalSaveData(saved);
        if (!saved.getBoolean("ShipCanFloat")) {
            helper.fail("modern save should persist CanFloatUp");
            return;
        }

        ShipWorldCacheEntry cached = ShipWorldCacheEntry.fromShip(migrated, false);
        cached.entityTag().remove("ShipCanFloat");
        cached.entityTag().put("ShipExtProps", legacyExtended.copy());
        if (!cached.isCanFloatUp()) {
            helper.fail("detached ship cache should preserve legacy CanFloat");
            return;
        }

        CompoundTag modernWins = legacyTag.copy();
        modernWins.putBoolean("ShipCanFloat", false);
        LegacyShipEntity modern = ModEntityTypes.LEGACY_SHIP.get().create(helper.getLevel());
        if (modern == null) {
            helper.fail("modern precedence ship should be creatable");
            return;
        }
        modern.readAdditionalSaveData(modernWins);
        if (modern.canFloatUp()) {
            helper.fail("modern ShipCanFloat should take precedence over legacy CanFloat");
            return;
        }

        LegacyShipEntity waterShip = ModEntityTypes.LEGACY_SHIP.get().create(helper.getLevel());
        if (waterShip == null) {
            helper.fail("water test ship should be creatable");
            return;
        }
        waterShip.setVariantEggMeta(53);
        waterShip.setNoGravity(true);
        waterShip.setOrderedToSit(true);
        waterShip.setPos(4.5D, 200.0D, 4.5D);
        helper.getLevel().addFreshEntity(waterShip);
        for (int y = 200; y <= 204; y++) {
            helper.getLevel().setBlock(new BlockPos(4, y, 4), Blocks.WATER.defaultBlockState(), Block.UPDATE_ALL);
        }
        waterShip.tick();
        if (!waterShip.canFloatUp()
                || waterShip.getLegacyLiquidDepth() <= waterShip.getLegacyFloatingDepth()) {
            BlockPos sampledPos = BlockPos.containing(waterShip.getX(), waterShip.getBoundingBox().minY,
                    waterShip.getZ());
            helper.fail("liquid column should enable legacy floating: canFloat=" + waterShip.canFloatUp()
                    + ", depth=" + waterShip.getLegacyLiquidDepth()
                    + ", sampled=" + sampledPos
                    + ", fluidEmpty=" + helper.getLevel().getFluidState(sampledPos).isEmpty());
            return;
        }

        waterShip.setOrderedToSit(false);
        waterShip.setDeltaMovement(0.0D, 0.0D, 0.0D);
        waterShip.applyLegacyFloatingMotion();
        if (waterShip.getDeltaMovement().y <= 0.0D) {
            helper.fail("legacy floating depth bands should add upward motion");
            return;
        }

        waterShip.setOrderedToSit(true);
        if (waterShip.canUseLegacyFloatingGoal()) {
            helper.fail("ordered-to-sit ships must not run the legacy floating goal");
            return;
        }
        waterShip.setOrderedToSit(false);
        waterShip.commandGuardBlock(new BlockPos(4, 200, 4), "minecraft:overworld");
        if (waterShip.canUseLegacyFloatingGoal()) {
            helper.fail("ships at a guarded block position must not float upward");
            return;
        }

        if (LegacyShipBehaviorCatalog.floatingDepthFor(ShipEntitySpecs.getByEggMeta(53)) != 0.32D
                || LegacyShipBehaviorCatalog.floatingDepthFor(ShipEntitySpecs.getByEggMeta(40)) != 1.1D
                || LegacyShipBehaviorCatalog.floatingDepthFor(ShipEntitySpecs.getByEggMeta(46)) != 1.0D
                || LegacyShipBehaviorCatalog.floatingDepthFor(ShipEntitySpecs.getByEggMeta(2040)) != 1.1D) {
            helper.fail("floating depth should retain legacy class overrides");
            return;
        }

        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void autoCombatRationUsesLegacyTickAndFoodRules(GameTestHelper helper) {
        ServerPlayer owner = makeServerBackedPlayer(helper, "AutoCombatRation");
        helper.getLevel().players().add(owner);
        owner.setPos(4.5D, 20.0D, 4.5D);
        LegacyShipEntity ship = createOwnedShip(helper, owner, 58, 4.5D, 20.0D, 4.5D);
        if (ship == null) {
            return;
        }

        ship.setNoGravity(true);
        ship.setAiAutoSupply(true);
        ship.setCombatRationLevel(2);
        ship.setMorale(3900);
        ship.setFoodSaturation(0);
        int rationSlot = LegacyShipEntity.EQUIPMENT_SLOT_COUNT;
        ship.getShipInventory().setItem(rationSlot, new ItemStack(ModItems.COMBATRATION.get()));
        int moraleBefore = ship.getMorale();

        ship.tickCount = 127;
        ship.tick();
        if (!ship.getShipInventory().getItem(rationSlot).is(ModItems.COMBATRATION.get())
                || ship.getFoodSaturation() != 0 || ship.getMorale() != moraleBefore) {
            helper.fail("auto combat ration should wait for the legacy 128-tick boundary");
            return;
        }

        ship.tickCount = 128;
        ship.tick();
        if (!ship.getShipInventory().getItem(rationSlot).isEmpty()
                || ship.getFoodSaturation() != 4
                || ship.getMorale() <= moraleBefore) {
            helper.fail("auto combat ration should consume one cargo ration and add four food saturation");
            return;
        }

        ship.setFoodSaturation(ship.getFoodSaturationMax());
        ship.getShipInventory().setItem(rationSlot, new ItemStack(ModItems.COMBATRATION.get()));
        ship.tickCount = 384;
        ship.tick();
        if (!ship.getShipInventory().getItem(rationSlot).is(ModItems.COMBATRATION.get())) {
            helper.fail("auto combat ration should not consume food while saturation is full");
            return;
        }

        ship.setFoodSaturation(1);
        ship.setCombatRationLevel(3);
        ship.setMorale(3900);
        ship.tickCount = 512;
        ship.tick();
        if (ship.getFoodSaturation() != 0
                || !ship.getShipInventory().getItem(rationSlot).is(ModItems.COMBATRATION.get())) {
            helper.fail("food should decay at 256 ticks while a below-threshold ration remains unused");
            return;
        }

        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void shipFeedingUsesLegacyFoodSaturationCap(GameTestHelper helper) {
        Player player = helper.makeMockPlayer();
        LegacyShipEntity ship = createOwnedShip(helper, player, 58, 2.5D, 2.0D, 2.5D);
        if (ship == null) {
            return;
        }

        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(ModItems.COMBATRATION.get()));
        if (ship.mobInteract(player, InteractionHand.MAIN_HAND) != InteractionResult.CONSUME
                || ship.getFoodSaturation() != 4) {
            helper.fail("manual combat ration feeding should add the legacy four saturation points");
            return;
        }

        int maxFood = ship.getFoodSaturationMax();
        ship.setFoodSaturation(maxFood);
        ItemStack fullStack = new ItemStack(ModItems.COMBATRATION.get());
        player.setItemInHand(InteractionHand.MAIN_HAND, fullStack);
        if (ship.mobInteract(player, InteractionHand.MAIN_HAND) != InteractionResult.CONSUME
                || ship.getFoodSaturation() != maxFood
                || player.getItemInHand(InteractionHand.MAIN_HAND).getCount() != 1) {
            helper.fail("full legacy food saturation should reject feeding without consuming the item");
            return;
        }

        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void autoPumpUsesFluidDrumAndConsumesIsolatedSource(GameTestHelper helper) {
        ServerPlayer owner = makeServerBackedPlayer(helper, "AutoPumpSource");
        helper.getLevel().players().add(owner);
        owner.getAbilities().mayBuild = true;
        owner.setPos(4.5D, 20.0D, 4.5D);

        LegacyShipEntity ship = createOwnedShip(helper, owner, 58, 4.5D, 20.0D, 4.5D);
        if (ship == null) {
            return;
        }

        ship.setNoGravity(true);
        ship.setAutoPump(true);
        ship.getShipInventory().setItem(0, new ItemStack(ModItems.EQUIPDRUM_ITEMS.get(1).get()));
        int tankSlot = LegacyShipEntity.EQUIPMENT_SLOT_COUNT;
        ship.getShipInventory().setItem(tankSlot, new ItemStack(ModItems.SHIPTANK.get()));
        BlockPos sourcePos = ship.blockPosition().east();
        helper.getLevel().setBlock(sourcePos, Blocks.WATER.defaultBlockState(), Block.UPDATE_ALL);
        ship.tickCount = 64;
        ship.tick();

        int tankAmount = FluidUtil.getFluidContained(ship.getShipInventory().getItem(tankSlot))
                .filter(fluid -> fluid.getFluid().isSame(Fluids.WATER))
                .map(FluidStack::getAmount)
                .orElse(0);
        if (tankAmount < 1000 || !helper.getLevel().getFluidState(sourcePos).isEmpty()) {
            helper.fail("legacy fluid drum should fill a cargo tank and consume an isolated source block");
            return;
        }

        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void autoPumpPreservesDeepWaterAndRejectsBaseDrum(GameTestHelper helper) {
        ServerPlayer owner = makeServerBackedPlayer(helper, "AutoPumpDepth");
        helper.getLevel().players().add(owner);
        owner.getAbilities().mayBuild = true;
        owner.setPos(4.5D, 20.0D, 4.5D);

        LegacyShipEntity wrongDrumShip = createOwnedShip(helper, owner, 58, 4.5D, 20.0D, 4.5D);
        if (wrongDrumShip == null) {
            return;
        }
        wrongDrumShip.setNoGravity(true);
        wrongDrumShip.setAutoPump(true);
        wrongDrumShip.getShipInventory().setItem(0, new ItemStack(ModItems.EQUIPDRUM_ITEMS.get(0).get()));
        int wrongTankSlot = LegacyShipEntity.EQUIPMENT_SLOT_COUNT;
        wrongDrumShip.getShipInventory().setItem(wrongTankSlot, new ItemStack(ModItems.SHIPTANK.get()));
        BlockPos wrongSourcePos = wrongDrumShip.blockPosition().east();
        helper.getLevel().setBlock(wrongSourcePos, Blocks.WATER.defaultBlockState(), Block.UPDATE_ALL);
        wrongDrumShip.tickCount = 64;
        wrongDrumShip.tick();
        if (FluidUtil.getFluidContained(wrongDrumShip.getShipInventory().getItem(wrongTankSlot)).isPresent()
                || helper.getLevel().getFluidState(wrongSourcePos).isEmpty()) {
            helper.fail("the base legacy drum must not substitute for the fluid drum");
            return;
        }

        LegacyShipEntity deepShip = createOwnedShip(helper, owner, 58, 14.5D, 20.0D, 4.5D);
        if (deepShip == null) {
            return;
        }
        deepShip.setNoGravity(true);
        deepShip.setAutoPump(true);
        deepShip.getShipInventory().setItem(0, new ItemStack(ModItems.EQUIPDRUM_ITEMS.get(1).get()));
        int deepTankSlot = LegacyShipEntity.EQUIPMENT_SLOT_COUNT;
        deepShip.getShipInventory().setItem(deepTankSlot, new ItemStack(ModItems.SHIPTANK.get()));
        BlockPos deepSourcePos = deepShip.blockPosition().east();
        for (int y = 0; y > -LegacyShipEntity.AUTO_PUMP_WATER_DEPTH; y--) {
            for (int x = -1; x <= 1; x++) {
                for (int z = -1; z <= 1; z++) {
                    helper.getLevel().setBlock(deepSourcePos.offset(x, y, z),
                            Blocks.WATER.defaultBlockState(), Block.UPDATE_ALL);
                }
            }
        }
        deepShip.tickCount = 64;
        deepShip.tick();

        if (FluidUtil.getFluidContained(deepShip.getShipInventory().getItem(deepTankSlot))
                .map(FluidStack::getAmount).orElse(0) < 1000
                || helper.getLevel().getFluidState(deepSourcePos).isEmpty()) {
            helper.fail("a water source meeting the legacy infinite depth should remain after pumping");
            return;
        }

        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void autoPumpCollectsExperienceOrbsIntoBottles(GameTestHelper helper) {
        ServerPlayer owner = makeServerBackedPlayer(helper, "AutoPumpExperience");
        helper.getLevel().players().add(owner);
        owner.getAbilities().mayBuild = true;
        owner.setPos(4.5D, 20.0D, 4.5D);

        LegacyShipEntity ship = createOwnedShip(helper, owner, 58, 4.5D, 20.0D, 4.5D);
        if (ship == null) {
            return;
        }

        ship.setNoGravity(true);
        ship.setAutoPump(true);
        ship.getShipInventory().setItem(0, new ItemStack(ModItems.EQUIPDRUM_ITEMS.get(1).get()));
        int bottleSlot = LegacyShipEntity.EQUIPMENT_SLOT_COUNT;
        ship.getShipInventory().setItem(bottleSlot, new ItemStack(Items.GLASS_BOTTLE, 2));
        ExperienceOrb orb = new ExperienceOrb(helper.getLevel(), ship.getX(), ship.getY(), ship.getZ(), 8);
        helper.getLevel().addFreshEntity(orb);

        ship.tickCount = 4;
        ship.tick();

        int experienceBottleCount = 0;
        int glassBottleCount = 0;
        for (int slot = LegacyShipEntity.EQUIPMENT_SLOT_COUNT;
             slot < ship.getShipInventory().getContainerSize(); slot++) {
            ItemStack stack = ship.getShipInventory().getItem(slot);
            if (stack.is(Items.EXPERIENCE_BOTTLE)) {
                experienceBottleCount += stack.getCount();
            } else if (stack.is(Items.GLASS_BOTTLE)) {
                glassBottleCount += stack.getCount();
            }
        }

        if (!orb.isRemoved() || experienceBottleCount != 1 || glassBottleCount != 1) {
            helper.fail("legacy auto pump should collect 8 XP and convert one glass bottle");
            return;
        }

        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void autoPumpPullsNearbyExperienceOrbs(GameTestHelper helper) {
        ServerPlayer owner = makeServerBackedPlayer(helper, "AutoPumpExperiencePull");
        helper.getLevel().players().add(owner);
        owner.getAbilities().mayBuild = true;
        owner.setPos(4.5D, 20.0D, 4.5D);

        LegacyShipEntity ship = createOwnedShip(helper, owner, 58, 4.5D, 20.0D, 4.5D);
        if (ship == null) {
            return;
        }

        ship.setNoGravity(true);
        ship.setAutoPump(true);
        ship.getShipInventory().setItem(0, new ItemStack(ModItems.EQUIPDRUM_ITEMS.get(1).get()));
        int bottleSlot = LegacyShipEntity.EQUIPMENT_SLOT_COUNT;
        ship.getShipInventory().setItem(bottleSlot, new ItemStack(Items.GLASS_BOTTLE));
        ExperienceOrb orb = new ExperienceOrb(helper.getLevel(), ship.getX() + 4.0D, ship.getY(), ship.getZ(), 3);
        helper.getLevel().addFreshEntity(orb);

        ship.tickCount = 4;
        ship.tick();

        if (orb.isRemoved() || orb.getDeltaMovement().x >= 0.0D
                || !ship.getShipInventory().getItem(bottleSlot).is(Items.GLASS_BOTTLE)) {
            helper.fail("legacy auto pump should pull nearby XP orbs without collecting them too early");
            return;
        }

        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void shipFollowGoalYieldsToExplicitOrders(GameTestHelper helper) {
        ServerPlayer player = makeServerBackedPlayer(helper, "FollowGoal");
        helper.getLevel().players().add(player);
        player.setPos(1.5D, 2.0D, 1.5D);
        LegacyShipEntity ship = ModEntityTypes.LEGACY_SHIP.get().create(helper.getLevel());
        if (ship == null) {
            helper.fail("legacy ship should be creatable for follow goal tests");
            return;
        }

        ship.setVariantEggMeta(58);
        ship.setPos(20.5D, 2.0D, 1.5D);
        helper.getLevel().addFreshEntity(ship);
        ship.setOwner(player.getUUID(), player.getGameProfile().getName(), 0);
        ship.setGrudge(1);
        ship.setCanFollow(true);

        LegacyShipFollowOwnerGoal goal = new LegacyShipFollowOwnerGoal(ship, 1.1D, 5.0F, 2.0F, 14.0F);
        if (!goal.canUse()) {
            helper.fail("follow goal should activate for a distant idle friendly ship");
            return;
        }

        ship.commandMoveTo(new BlockPos(24, 2, 1), helper.getLevel().dimension().location().toString());
        if (goal.canUse()) {
            helper.fail("follow goal must yield to an explicit move or route command");
            return;
        }

        ship.clearCommandState();
        if (!goal.canUse()) {
            helper.fail("follow goal should resume after an explicit move or route command is cleared");
            return;
        }

        ship.commandGuardEntity(UUID.randomUUID());
        if (goal.canUse()) {
            helper.fail("follow goal must yield to an explicit guard command");
            return;
        }

        ship.clearCommandState();
        ship.setGrudge(0);
        if (goal.canUse()) {
            helper.fail("follow goal must stop when the ship has no grudge to spend");
            return;
        }

        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void shipCanFollowMigratesAndSwitchesWithCommands(GameTestHelper helper) {
        LegacyShipEntity migrated = ModEntityTypes.LEGACY_SHIP.get().create(helper.getLevel());
        if (migrated == null) {
            helper.fail("legacy ship should be creatable for CanFollow migration");
            return;
        }

        CompoundTag legacyTag = new CompoundTag();
        legacyTag.putInt("VariantEggMeta", 53);
        CompoundTag legacyExtended = new CompoundTag();
        CompoundTag legacyFlags = new CompoundTag();
        legacyFlags.putBoolean("CanFollow", true);
        legacyExtended.put("ShipFlags", legacyFlags);
        legacyTag.put("ShipExtProps", legacyExtended);
        migrated.readAdditionalSaveData(legacyTag);
        if (!migrated.canFollow()) {
            helper.fail("legacy ShipFlags.CanFollow should migrate to the modern state");
            return;
        }

        CompoundTag saved = new CompoundTag();
        migrated.addAdditionalSaveData(saved);
        if (!saved.getBoolean("ShipCanFollow")) {
            helper.fail("modern save should persist CanFollow");
            return;
        }

        ShipWorldCacheEntry cached = ShipWorldCacheEntry.fromShip(migrated, false);
        cached.entityTag().remove("ShipCanFollow");
        cached.entityTag().put("ShipExtProps", legacyExtended.copy());
        if (!cached.canFollow()) {
            helper.fail("detached ship cache should preserve legacy CanFollow");
            return;
        }

        CompoundTag modernWins = legacyTag.copy();
        modernWins.putBoolean("ShipCanFollow", false);
        LegacyShipEntity modern = ModEntityTypes.LEGACY_SHIP.get().create(helper.getLevel());
        if (modern == null) {
            helper.fail("modern precedence ship should be creatable");
            return;
        }
        modern.readAdditionalSaveData(modernWins);
        if (modern.canFollow()) {
            helper.fail("modern ShipCanFollow should take precedence over legacy CanFollow");
            return;
        }

        migrated.setCanFollow(true);
        migrated.commandMoveTo(new BlockPos(8, 2, 8), "minecraft:overworld");
        if (migrated.canFollow()) {
            helper.fail("move commands should disable legacy owner follow");
            return;
        }
        migrated.clearCommandState();
        if (!migrated.canFollow()) {
            helper.fail("clearing a move command should restore legacy owner follow");
            return;
        }

        migrated.commandGuardEntity(UUID.randomUUID());
        if (migrated.canFollow()) {
            helper.fail("guard-entity commands should disable legacy owner follow");
            return;
        }
        migrated.clearCommandState();
        migrated.commandGuardBlock(new BlockPos(8, 2, 8), "minecraft:overworld");
        if (migrated.canFollow()) {
            helper.fail("guard-block commands should disable legacy owner follow");
            return;
        }
        migrated.clearCommandState();
        if (!migrated.canFollow()) {
            helper.fail("clearing a guard command should restore legacy owner follow");
            return;
        }

        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void ship07SelectedModelsBakeAndKeepLegacyShowEquipBranches(GameTestHelper helper) {
        int[] eggMetas = {50, 51, 53, 54, 55};
        String[] modelStems = {
                "ModelCarrierAkagi", "ModelCAHime", "ModelDestroyerAkatsuki",
                "ModelDestroyerHibiki", "ModelDestroyerIkazuchi"
        };
        LegacyModelSourceParser parser = new LegacyModelSourceParser();

        for (int index = 0; index < eggMetas.length; index++) {
            ShipEntitySpec spec = ShipEntitySpecs.getByEggMeta(eggMetas[index]);
            LegacyShipRenderCatalog.RenderEntry renderEntry = LegacyShipRenderCatalog.forSpec(spec);
            if (!modelStems[index].equals(renderEntry.modelSourceStem())
                    || !resourceExists(renderEntry.modelSourceLocation())
                    || !resourceExists(renderEntry.textureLocation())) {
                helper.fail("SHIP-07 selected ship should keep its exact legacy model source and texture: egg="
                        + eggMetas[index]);
                return;
            }

            try {
                LegacyModelDefinition definition = parser.parse(renderEntry.modelSourceStem(),
                        readTextResource(renderEntry.modelSourceLocation()));
                if (definition.rootParts().isEmpty() || definition.parts().isEmpty()
                        || definition.parts().values().stream().noneMatch(part -> !part.cubes().isEmpty())) {
                    helper.fail("SHIP-07 selected model source should bake to a non-empty model: "
                            + renderEntry.modelSourceStem());
                    return;
                }
            } catch (Exception exception) {
                helper.fail("SHIP-07 selected model source should parse without fallback: "
                        + renderEntry.modelSourceStem() + " error=" + exception.getClass().getSimpleName());
                return;
            }
        }

        if (LegacyShipModelStateCatalog.isPartVisible("ModelCarrierAkagi", "EquipE01", 0)
                || !LegacyShipModelStateCatalog.isPartVisible("ModelCarrierAkagi", "EquipE01", 1 << 0)
                || LegacyShipModelStateCatalog.isPartVisible("ModelCarrierAkagi", "EquipSL01", 0)
                || !LegacyShipModelStateCatalog.isPartVisible("ModelCarrierAkagi", "EquipSL01", 1 << 7)
                || LegacyShipModelStateCatalog.isPartVisible("ModelCAHime", "TailBase", 0)
                || !LegacyShipModelStateCatalog.isPartVisible("ModelCAHime", "TailBase", 1 << 0)
                || !LegacyShipModelStateCatalog.isPartVisible("ModelCAHime", "TailBase", 1 << 1)
                || LegacyShipModelStateCatalog.isPartVisible("ModelCAHime", "HatBase", 0)
                || !LegacyShipModelStateCatalog.isPartVisible("ModelCAHime", "Hair01", 0)
                || !LegacyShipModelStateCatalog.isPartVisible("ModelCAHime", "Horn01", 0)
                || !LegacyShipModelStateCatalog.isPartVisible("ModelCAHime", "HatBase", 1 << 2)
                || LegacyShipModelStateCatalog.isPartVisible("ModelCAHime", "Hair01", 1 << 2)
                || LegacyShipModelStateCatalog.isPartVisible("ModelCAHime", "Horn01", 1 << 2)
                || !LegacyShipModelStateCatalog.isPartVisible("ModelCAHime", "HatBase", 1 << 3)
                || !LegacyShipModelStateCatalog.isPartVisible("ModelCAHime", "Horn01", 1 << 3)
                || LegacyShipModelStateCatalog.isPartVisible("ModelCAHime", "Hair01", 1 << 3)
                || LegacyShipModelStateCatalog.isPartVisible("ModelDestroyerAkatsuki", "EquipBase", 0)
                || !LegacyShipModelStateCatalog.isPartVisible("ModelDestroyerAkatsuki", "EquipBase", 1 << 0)
                || LegacyShipModelStateCatalog.isPartVisible("ModelDestroyerAkatsuki", "EquipTR03", 0)
                || !LegacyShipModelStateCatalog.isPartVisible("ModelDestroyerAkatsuki", "EquipTR03", 1 << 3)
                || LegacyShipModelStateCatalog.isPartVisible("ModelDestroyerHibiki", "HatBase", 0)
                || !LegacyShipModelStateCatalog.isPartVisible("ModelDestroyerHibiki", "Hair02f1", 0)
                || !LegacyShipModelStateCatalog.isPartVisible("ModelDestroyerHibiki", "Hair01", 0)
                || LegacyShipModelStateCatalog.isPartVisible("ModelDestroyerHibiki", "HatBase2", 0)
                || !LegacyShipModelStateCatalog.isPartVisible("ModelDestroyerHibiki", "HatBase", 1 << 2)
                || !LegacyShipModelStateCatalog.isPartVisible("ModelDestroyerHibiki", "Hair02f1", 1 << 2)
                || LegacyShipModelStateCatalog.isPartVisible("ModelDestroyerHibiki", "HatBase2", 1 << 2)
                || LegacyShipModelStateCatalog.isPartVisible("ModelDestroyerHibiki", "HatBase", 1 << 3)
                || !LegacyShipModelStateCatalog.isPartVisible("ModelDestroyerHibiki", "Hair01", 1 << 3)
                || !LegacyShipModelStateCatalog.isPartVisible("ModelDestroyerHibiki", "HatBase2", 1 << 3)
                || LegacyShipModelStateCatalog.isPartVisible("ModelDestroyerHibiki", "Hair01", (1 << 3) | (1 << 4))
                || !LegacyShipModelStateCatalog.isPartVisible("ModelDestroyerHibiki", "HatBase2", (1 << 3) | (1 << 4))
                || LegacyShipModelStateCatalog.isPartVisible("ModelDestroyerIkazuchi", "EquipBase", 0)
                || !LegacyShipModelStateCatalog.isPartVisible("ModelDestroyerIkazuchi", "EquipBase", 1 << 0)
                || LegacyShipModelStateCatalog.isPartVisible("ModelDestroyerIkazuchi", "EquipHead01", 0)
                || !LegacyShipModelStateCatalog.isPartVisible("ModelDestroyerIkazuchi", "EquipHead01", 1 << 1)) {
            helper.fail("SHIP-07 model visibility should preserve each selected legacy showEquip branch");
            return;
        }

        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void ship07DestroyerRiderFuelBoundaryMatchesLegacyOverrides(GameTestHelper helper) {
        LegacyShipEntity akatsuki = ModEntityTypes.LEGACY_SHIP.get().create(helper.getLevel());
        LegacyShipEntity hibiki = ModEntityTypes.LEGACY_SHIP.get().create(helper.getLevel());
        if (akatsuki == null || hibiki == null) {
            helper.fail("Akatsuki and Hibiki should be creatable for the rider fuel boundary");
            return;
        }

        UUID ownerUuid = UUID.randomUUID();
        akatsuki.setVariantEggMeta(53);
        akatsuki.setOwner(ownerUuid, "Ship07Fuel", 7007);
        akatsuki.setGrudge(100);
        akatsuki.setPos(2.5D, 2.0D, 2.5D);
        hibiki.setVariantEggMeta(54);
        hibiki.setOwner(ownerUuid, "Ship07Fuel", 7007);
        hibiki.setGrudge(0);
        hibiki.setPos(2.5D, 2.0D, 2.5D);
        helper.getLevel().addFreshEntity(akatsuki);
        helper.getLevel().addFreshEntity(hibiki);
        if (!hibiki.startRiding(akatsuki, true)) {
            helper.fail("Hibiki should mount Akatsuki before the rider-side fuel boundary");
            return;
        }
        hibiki.setLegacyRiderType(1);
        hibiki.setLegacyRidingState(1);
        hibiki.tickCount = 32;
        LegacyShipBehaviorCatalog.tickLegacyRiderBehavior(hibiki);
        if (hibiki.isPassenger() || !akatsuki.getPassengers().isEmpty()
                || hibiki.getLegacyRiderType() != 0 || hibiki.getLegacyRidingState() != 0) {
            helper.fail("Hibiki no-fuel state should invoke Akatsuki's legacy dismountAllRider override");
            return;
        }
        akatsuki.discard();
        hibiki.discard();

        LegacyShipEntity inazuma = ModEntityTypes.LEGACY_SHIP.get().create(helper.getLevel());
        LegacyShipEntity ikazuchi = ModEntityTypes.LEGACY_SHIP.get().create(helper.getLevel());
        if (inazuma == null || ikazuchi == null) {
            helper.fail("Inazuma and Ikazuchi should be creatable for the Raiden fuel boundary");
            return;
        }
        inazuma.setVariantEggMeta(56);
        inazuma.setOwner(ownerUuid, "Ship07Fuel", 7007);
        inazuma.setGrudge(100);
        inazuma.setPos(6.5D, 2.0D, 2.5D);
        ikazuchi.setVariantEggMeta(55);
        ikazuchi.setOwner(ownerUuid, "Ship07Fuel", 7007);
        ikazuchi.setGrudge(0);
        ikazuchi.setPos(6.5D, 2.0D, 2.5D);
        helper.getLevel().addFreshEntity(inazuma);
        helper.getLevel().addFreshEntity(ikazuchi);
        if (!ikazuchi.startRiding(inazuma, true)) {
            helper.fail("Ikazuchi should mount Inazuma before the Raiden fuel boundary");
            return;
        }
        ikazuchi.setLegacyRaiden(true);
        inazuma.setLegacyRaiden(true);
        ikazuchi.setLegacyRidingState(1);
        inazuma.setLegacyRidingState(2);
        ikazuchi.tickCount = 32;
        LegacyShipBehaviorCatalog.tickLegacyRiderBehavior(ikazuchi);
        if (ikazuchi.isPassenger() || !inazuma.getPassengers().isEmpty()
                || ikazuchi.isLegacyRaiden() || inazuma.isLegacyRaiden()
                || ikazuchi.getLegacyRidingState() != 0 || inazuma.getLegacyRidingState() != 0) {
            helper.fail("Ikazuchi no-fuel state should break the legacy Raiden pair");
            return;
        }
        inazuma.discard();
        ikazuchi.discard();

        LegacyShipEntity secondInazuma = ModEntityTypes.LEGACY_SHIP.get().create(helper.getLevel());
        LegacyShipEntity secondIkazuchi = ModEntityTypes.LEGACY_SHIP.get().create(helper.getLevel());
        if (secondInazuma == null || secondIkazuchi == null) {
            helper.fail("second Raiden pair should be creatable for the host-side fuel boundary");
            return;
        }
        secondInazuma.setVariantEggMeta(56);
        secondInazuma.setOwner(ownerUuid, "Ship07Fuel", 7007);
        secondInazuma.setGrudge(0);
        secondInazuma.setPos(10.5D, 2.0D, 2.5D);
        secondIkazuchi.setVariantEggMeta(55);
        secondIkazuchi.setOwner(ownerUuid, "Ship07Fuel", 7007);
        secondIkazuchi.setGrudge(100);
        secondIkazuchi.setPos(10.5D, 2.0D, 2.5D);
        helper.getLevel().addFreshEntity(secondInazuma);
        helper.getLevel().addFreshEntity(secondIkazuchi);
        if (!secondIkazuchi.startRiding(secondInazuma, true)) {
            helper.fail("Ikazuchi should mount the second Inazuma before the inverse fuel boundary");
            return;
        }
        secondInazuma.setLegacyRaiden(true);
        secondIkazuchi.setLegacyRaiden(true);
        secondInazuma.setLegacyRidingState(2);
        secondIkazuchi.setLegacyRidingState(1);
        secondInazuma.tickCount = 32;
        LegacyShipBehaviorCatalog.tickLegacyRiderBehavior(secondInazuma);
        if (secondIkazuchi.isPassenger() || !secondInazuma.getPassengers().isEmpty()
                || secondInazuma.isLegacyRaiden() || secondIkazuchi.isLegacyRaiden()
                || secondInazuma.getLegacyRidingState() != 0
                || secondIkazuchi.getLegacyRidingState() != 0) {
            helper.fail("Inazuma no-fuel state should break the legacy Raiden pair from the host side");
            return;
        }

        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void ship08SelectedModelsBakeAndKeepLegacyShowEquipBranches(GameTestHelper helper) {
        int[] eggMetas = {56, 58, 59, 60, 61};
        String[] modelStems = {
                "ModelDestroyerInazuma", "ModelCruiserTenryuu", "ModelCruiserTatsuta",
                "ModelCruiserAtago", "ModelCruiserTakao"
        };
        LegacyModelSourceParser parser = new LegacyModelSourceParser();

        for (int index = 0; index < eggMetas.length; index++) {
            ShipEntitySpec spec = ShipEntitySpecs.getByEggMeta(eggMetas[index]);
            LegacyShipRenderCatalog.RenderEntry renderEntry = LegacyShipRenderCatalog.forSpec(spec);
            if (!modelStems[index].equals(renderEntry.modelSourceStem())
                    || !resourceExists(renderEntry.modelSourceLocation())
                    || !resourceExists(renderEntry.textureLocation())) {
                helper.fail("SHIP-08 selected ship should keep its exact legacy model source and texture: egg="
                        + eggMetas[index]);
                return;
            }

            try {
                LegacyModelDefinition definition = parser.parse(renderEntry.modelSourceStem(),
                        readTextResource(renderEntry.modelSourceLocation()));
                if (definition.rootParts().isEmpty() || definition.parts().isEmpty()
                        || definition.parts().values().stream().noneMatch(part -> !part.cubes().isEmpty())) {
                    helper.fail("SHIP-08 selected model source should bake to a non-empty model: "
                            + renderEntry.modelSourceStem());
                    return;
                }
            } catch (Exception exception) {
                helper.fail("SHIP-08 selected model source should parse without fallback: "
                        + renderEntry.modelSourceStem() + " error=" + exception.getClass().getSimpleName());
                return;
            }
        }

        if (LegacyShipModelStateCatalog.isPartVisible("ModelDestroyerInazuma", "EquipBase", 0)
                || !LegacyShipModelStateCatalog.isPartVisible("ModelDestroyerInazuma", "EquipBase", 1)
                || LegacyShipModelStateCatalog.isPartVisible("ModelCruiserTenryuu", "Equip00", 0)
                || !LegacyShipModelStateCatalog.isPartVisible("ModelCruiserTenryuu", "Equip00", 1)
                || LegacyShipModelStateCatalog.isPartVisible("ModelCruiserTenryuu", "EyeMask", 0)
                || !LegacyShipModelStateCatalog.isPartVisible("ModelCruiserTenryuu", "EyeMask", 1 << 3)
                || LegacyShipModelStateCatalog.isPartVisible("ModelCruiserTatsuta", "CirBase", 0)
                || !LegacyShipModelStateCatalog.isPartVisible("ModelCruiserTatsuta", "CirBase", 1 << 1)
                || LegacyShipModelStateCatalog.isPartVisible("ModelCruiserTatsuta", "EquipSL00", 0)
                || !LegacyShipModelStateCatalog.isPartVisible("ModelCruiserTatsuta", "EquipSL00", 1 << 2)
                || LegacyShipModelStateCatalog.isPartVisible("ModelCruiserAtago", "Hat01", 0)
                || !LegacyShipModelStateCatalog.isPartVisible("ModelCruiserAtago", "Hat01", 1 << 2)
                || LegacyShipModelStateCatalog.isPartVisible("ModelCruiserTakao", "ShoeL03", 0)
                || !LegacyShipModelStateCatalog.isPartVisible("ModelCruiserTakao", "ShoeL03", 1 << 3)) {
            helper.fail("SHIP-08 model visibility should preserve each selected legacy showEquip branch");
            return;
        }

        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void ship09KongouFamilyModelsAndRuntimeBoundariesMatchLegacy(GameTestHelper helper) {
        int[] eggMetas = {62, 63, 64, 65, 74};
        String[] modelStems = {
                "ModelBBKongou", "ModelBBHiei", "ModelBBHaruna", "ModelBBKirishima", "ModelSSNH"
        };
        LegacyModelSourceParser parser = new LegacyModelSourceParser();

        for (int index = 0; index < eggMetas.length; index++) {
            ShipEntitySpec spec = ShipEntitySpecs.getByEggMeta(eggMetas[index]);
            LegacyShipRenderCatalog.RenderEntry renderEntry = LegacyShipRenderCatalog.forSpec(spec);
            if (!modelStems[index].equals(renderEntry.modelSourceStem())
                    || !resourceExists(renderEntry.modelSourceLocation())
                    || !resourceExists(renderEntry.textureLocation())) {
                helper.fail("SHIP-09 selected ship should keep its exact legacy model source and texture: egg="
                        + eggMetas[index]);
                return;
            }

            try {
                LegacyModelDefinition definition = parser.parse(renderEntry.modelSourceStem(),
                        readTextResource(renderEntry.modelSourceLocation()));
                if (definition.rootParts().isEmpty() || definition.parts().isEmpty()
                        || definition.parts().values().stream().noneMatch(part -> !part.cubes().isEmpty())) {
                    helper.fail("SHIP-09 selected model source should bake to a non-empty model: "
                            + renderEntry.modelSourceStem());
                    return;
                }
            } catch (Exception exception) {
                helper.fail("SHIP-09 selected model source should parse without fallback: "
                        + renderEntry.modelSourceStem() + " error=" + exception.getClass().getSimpleName());
                return;
            }
        }

        if (LegacyShipModelStateCatalog.isPartVisible("ModelBBKongou", "EquipBase", 0)
                || !LegacyShipModelStateCatalog.isPartVisible("ModelBBKongou", "EquipBase", 1)
                || LegacyShipModelStateCatalog.isPartVisible("ModelBBKongou", "EquipHeadBase", 0)
                || !LegacyShipModelStateCatalog.isPartVisible("ModelBBKongou", "EquipHeadBase", 1 << 1)
                || LegacyShipModelStateCatalog.isPartVisible("ModelBBKongou", "HairS01", 0)
                || !LegacyShipModelStateCatalog.isPartVisible("ModelBBKongou", "HairS01", 1 << 2)
                || LegacyShipModelStateCatalog.isPartVisible("ModelBBKongou", "Ahoke00", 0)
                || !LegacyShipModelStateCatalog.isPartVisible("ModelBBKongou", "Ahoke00", 1 << 3)
                || LegacyShipModelStateCatalog.isPartVisible("ModelBBKongou", "Ahoke01", 1 << 3)
                || !LegacyShipModelStateCatalog.isPartVisible("ModelBBHiei", "EquipBase", 1 << 0)
                || LegacyShipModelStateCatalog.isPartVisible("ModelBBHiei", "EquipBase", 0)
                || !LegacyShipModelStateCatalog.isPartVisible("ModelBBHaruna", "EquipHeadBase", 1 << 1)
                || LegacyShipModelStateCatalog.isPartVisible("ModelBBHaruna", "EquipHeadBase", 0)
                || LegacyShipModelStateCatalog.isPartVisible("ModelBBKirishima", "EquipGlass01", 0)
                || !LegacyShipModelStateCatalog.isPartVisible("ModelBBKirishima", "EquipGlass01", 1 << 2)
                || LegacyShipModelStateCatalog.isPartVisible("ModelSSNH", "EquipTBase", 0)
                || !LegacyShipModelStateCatalog.isPartVisible("ModelSSNH", "EquipTBase", 1 << 2)
                || LegacyShipModelStateCatalog.isPartVisible("ModelSSNH", "EquipTBase_2", 1 << 2)) {
            helper.fail("SHIP-09 model visibility should preserve the legacy showEquip branches");
            return;
        }

        assertMarriagePassive(helper, 62, LegacyShipBehaviorCatalog.MarriagePassive.ALLIED_HEALTH_BOOST_AURA);
        assertMarriagePassive(helper, 63, LegacyShipBehaviorCatalog.MarriagePassive.ALLIED_SATURATION_AURA);
        assertMarriagePassive(helper, 64, LegacyShipBehaviorCatalog.MarriagePassive.ALLIED_LUCK_AURA);
        assertMarriagePassive(helper, 65, LegacyShipBehaviorCatalog.MarriagePassive.ALLIED_NIGHT_VISION_AURA);

        for (int eggMeta : new int[] {62, 63, 64, 65}) {
            ShipEntitySpec friendly = ShipEntitySpecs.getByEggMeta(eggMeta);
            ShipEntitySpec hostile = ShipEntitySpecs.getByEggMeta(eggMeta + 2000);
            EntityDimensions friendlyDimensions = LegacyShipBehaviorCatalog.dimensionsFor(friendly, 0);
            EntityDimensions hostileDimensions = LegacyShipBehaviorCatalog.dimensionsFor(hostile, 3);
            LegacyShipAttackProfile profile = LegacyShipBehaviorCatalog.attackProfile(friendly);
            if (Math.abs(friendlyDimensions.width - 0.6F) > 1.0E-6F
                    || Math.abs(friendlyDimensions.height - 1.875F) > 1.0E-6F
                    || Math.abs(hostileDimensions.width - 2.4F) > 1.0E-6F
                    || Math.abs(hostileDimensions.height - 7.5F) > 1.0E-6F
                    || profile.hasAirAttack()
                    || !profile.heavy()
                    || !profile.projectileProfile(LegacyShipAttackKind.HEAVY).isPresent()) {
                helper.fail("SHIP-09 battleships should preserve legacy dimensions and non-air heavy missile profiles: egg="
                        + eggMeta);
                return;
            }
        }

        LegacyShipEntity ssnh = ModEntityTypes.LEGACY_SHIP.get().create(helper.getLevel());
        if (ssnh == null) {
            helper.fail("SHIP-09 SSNH should be creatable for its runtime boundary checks");
            return;
        }
        ssnh.setVariantEggMeta(74);
        if (LegacyShipBehaviorCatalog.floatingDepthFor(ShipEntitySpecs.getByEggMeta(74)) != 1.0D
                || Math.abs(LegacyShipBehaviorCatalog.myRidingOffsetFor(ssnh) - 0.25D) > 1.0E-6D
                || LegacyShipBehaviorCatalog.behaviorFor(ssnh.getSpec()).combatHook()
                != LegacyShipBehaviorCatalog.CombatHook.SUBMARINE_HIME_DOUBLE_MISSILE) {
            helper.fail("SHIP-09 SSNH should preserve legacy depth, riding offset, and double-missile hook");
            return;
        }

        ssnh.discard();
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void allShipDamageTypesAndLightModifiersMirrorLegacy(GameTestHelper helper) {
        int[][] expectedTypes = {
                {2, 5}, {3, 5}, {4, 5}, {5, 5}, {11, 4}, {12, 4},
                {14, 1}, {15, 3}, {16, 3}, {17, 2}, {18, 0}, {19, 6},
                {20, 6}, {21, 6}, {22, 1}, {23, 2}, {28, 3}, {29, 5},
                {30, 2}, {31, 2}, {32, 2}, {33, 2}, {35, 1}, {38, 5},
                {39, 3}, {40, 6}, {41, 6}, {46, 6}, {48, 3}, {49, 1},
                {50, 1}, {51, 4}, {53, 5}, {54, 5}, {55, 5}, {56, 5},
                {58, 4}, {59, 4}, {60, 4}, {61, 4}, {62, 3}, {63, 3},
                {64, 3}, {65, 3}, {74, 6}
        };

        for (int[] expected : expectedTypes) {
            ShipEntitySpec spec = ShipEntitySpecs.getByEggMeta(expected[0]);
            if (LegacyShipCombatHelper.legacyDamageTypeFor(spec) != expected[1]) {
                helper.fail("legacy damage type mismatch for egg meta " + expected[0]
                        + ": expected " + expected[1]
                        + " but got " + LegacyShipCombatHelper.legacyDamageTypeFor(spec));
                return;
            }
        }

        if (LegacyShipCombatHelper.legacyDamageTypeForRensouhou(false) != 5
                || LegacyShipCombatHelper.legacyDamageTypeForRensouhou(true) != 3
                || Math.abs(LegacyShipCombatHelper.legacyLightCoefficientForRawBrightness(2, false)) > 1.0E-6F
                || Math.abs(LegacyShipCombatHelper.legacyLightCoefficientForRawBrightness(8, false) - 1F) > 1.0E-6F
                || Math.abs(LegacyShipCombatHelper.legacyLightCoefficientForRawBrightness(5, true) - 1.3F) > 1.0E-6F
                || Math.abs(LegacyShipCombatHelper.modDamageByLight(100F, 1, 5, 0F) - 100F) > 1.0E-6F
                || Math.abs(LegacyShipCombatHelper.modDamageByLight(100F, 1, 5, 1F) - 50F) > 1.0E-6F
                || Math.abs(LegacyShipCombatHelper.modDamageByLight(100F, 0, 5, 0.5F) - 100F) > 1.0E-6F) {
            helper.fail("legacy light modifier should preserve type tables, clamps, and summon source types");
            return;
        }

        helper.succeed();
    }

    // --------------------------------------------------------------------------------
    // Historical Regression Backfill Helpers (8 Methods)
    // --------------------------------------------------------------------------------

    private static LegacyShipProjectileMoveType expectedLegacyArcMissileMove(
            LegacyShipEntity ship, LegacyShipProjectileEntity projectile) {
        LegacyShipProjectileProfile profile = ship.getAttackProfile()
                .projectileProfile(LegacyShipAttackKind.LIGHT);
        Vec3 distanceVector = projectile.getLegacyTargetPosition().subtract(projectile.position());
        double distance = distanceVector.length();
        double speed = ship.getEquipmentBehaviorState().legacyProjectileSpeed(profile.baseSpeed());
        if (profile.moveType() != LegacyShipProjectileMoveType.ARC
                || distance <= 1.0E-7D || speed <= 1.0E-7D || distance < 4.0D
                || profile.launchArcLift() <= 0.0F) {
            return profile.moveType() == LegacyShipProjectileMoveType.ARC
                    ? LegacyShipProjectileMoveType.DIRECT : profile.moveType();
        }

        double horizontalDistance = Math.sqrt(distanceVector.x * distanceVector.x
                + distanceVector.z * distanceVector.z);
        if (horizontalDistance <= 4.0D) {
            return LegacyShipProjectileMoveType.ARC;
        }

        double travelTicks = horizontalDistance / speed;
        double additionalHeight = distance * profile.launchArcLift();
        double verticalDistance = Math.abs(projectile.getY() - projectile.getLegacyTargetPosition().y);
        double heightRatio = Math.sqrt(additionalHeight / (additionalHeight + verticalDistance));
        int riseTicks;
        int fallTicks;
        double acceleration1;
        double acceleration2;
        if (projectile.getY() - projectile.getLegacyTargetPosition().y < 1.0D) {
            riseTicks = (int) Math.floor(travelTicks / (1.0D + heightRatio));
            fallTicks = (int) Math.floor(travelTicks * heightRatio / (1.0D + heightRatio));
            double verticalSpeed = 2.0D * (additionalHeight + verticalDistance) / riseTicks;
            acceleration1 = -verticalSpeed / riseTicks;
            acceleration2 = -2.0D * additionalHeight / (fallTicks * fallTicks);
        } else {
            riseTicks = (int) Math.floor(travelTicks * heightRatio / (1.0D + heightRatio));
            fallTicks = (int) Math.floor(travelTicks / (1.0D + heightRatio));
            acceleration1 = -2.0D * additionalHeight / (riseTicks * riseTicks);
            acceleration2 = -2.0D * (additionalHeight + verticalDistance)
                    / (fallTicks * fallTicks);
        }
        if (riseTicks <= 0 || fallTicks <= 0
                || Math.abs(acceleration1) > 0.15D || Math.abs(acceleration2) > 0.15D) {
            return LegacyShipProjectileMoveType.DIRECT;
        }
        return LegacyShipProjectileMoveType.ARC;
    }

    private static boolean attackEffectMatches(int eggMeta,
                                               int level,
                                               int hostileScaleLevel,
                                               MobEffect expectedEffect,
                                               int expectedAmplifier,
                                               int expectedDuration,
                                               int expectedChance) {
        LegacyShipBehaviorCatalog.AttackEffectProfile actual = LegacyShipBehaviorCatalog.attackEffectFor(
                ShipEntitySpecs.getByEggMeta(eggMeta), level, hostileScaleLevel);
        return actual.effect() == expectedEffect
                && actual.amplifier() == expectedAmplifier
                && actual.durationTicks() == expectedDuration
                && actual.chancePercent() == expectedChance;
    }

    private static boolean attackEffectMissing(int eggMeta, int level, int hostileScaleLevel) {
        return !LegacyShipBehaviorCatalog.attackEffectFor(
                ShipEntitySpecs.getByEggMeta(eggMeta), level, hostileScaleLevel).isPresent();
    }

    private static boolean setSyntheticGuardPath(LegacyShipEntity ship, BlockPos origin, BlockPos guardPos) {
        Path path = new Path(List.of(
                new Node(origin.getX(), origin.getY(), origin.getZ()),
                new Node(guardPos.getX(), guardPos.getY(), guardPos.getZ())), guardPos, true);
        return ship.getNavigation().moveTo(path, 1.0D) && !ship.getNavigation().isDone();
    }

    private static boolean engineSmokePointMatches(LegacyShipBehaviorCatalog.EngineSmokePoint point,
                                                    float xOffset, float yOffset, float particleScale) {
        return Math.abs(point.xOffset() - xOffset) <= 1.0E-6F
                && Math.abs(point.yOffset() - yOffset) <= 1.0E-6F
                && Math.abs(point.particleScale() - particleScale) <= 1.0E-6F;
    }

    private static boolean cannonFeedbackMatches(
            LegacyShipBehaviorCatalog.LegacyCannonAttackFeedback feedback,
            double width, double length, double height) {
        return Math.abs(feedback.width() - width) <= 1.0E-6D
                && Math.abs(feedback.length() - length) <= 1.0E-6D
                && Math.abs(feedback.height() - height) <= 1.0E-6D;
    }

    private static boolean engineVisualPointMatches(LegacyShipBehaviorCatalog.EngineVisualPoint point,
                                                     float xOffset, float yOffset, float particleScale) {
        return Math.abs(point.xOffset() - xOffset) <= 1.0E-6F
                && Math.abs(point.yOffset() - yOffset) <= 1.0E-6F
                && Math.abs(point.particleScale() - particleScale) <= 1.0E-6F;
    }

    private static boolean clientEffectPointMatches(
            LegacyShipBehaviorCatalog.LegacyClientEffectPoint point,
            float xOffset, float yOffset, float zOffset,
            float velocityX, float velocityY, float velocityZ,
            LegacyShipBehaviorCatalog.LegacyClientEffectKind kind,
            int legacyEmotionType) {
        return Math.abs(point.xOffset() - xOffset) <= 1.0E-4F
                && Math.abs(point.yOffset() - yOffset) <= 1.0E-4F
                && Math.abs(point.zOffset() - zOffset) <= 1.0E-4F
                && Math.abs(point.velocityX() - velocityX) <= 1.0E-4F
                && Math.abs(point.velocityY() - velocityY) <= 1.0E-4F
                && Math.abs(point.velocityZ() - velocityZ) <= 1.0E-4F
                && point.kind() == kind
                && point.legacyEmotionType() == legacyEmotionType;
    }

    @GameTest(template = "empty")
    public static void blockHarvestToolTagsAndBeaconBaseMirrorLegacy(GameTestHelper helper) {
        Block[] pickaxeLevelZeroBlocks = {
                ModBlocks.BLOCK_ABYSSIUM.get(),
                ModBlocks.BLOCK_CRANE.get(),
                ModBlocks.BLOCK_DESK.get(),
                ModBlocks.BLOCK_FRAME.get(),
                ModBlocks.BLOCK_POLYMETAL.get(),
                ModBlocks.BLOCK_VOL_BLOCK.get(),
                ModBlocks.BLOCK_VOL_CORE.get()
        };
        ItemStack woodPick = new ItemStack(Items.WOODEN_PICKAXE);
        ItemStack stonePick = new ItemStack(Items.STONE_PICKAXE);
        ItemStack ironPick = new ItemStack(Items.IRON_PICKAXE);
        ItemStack diamondPick = new ItemStack(Items.DIAMOND_PICKAXE);

        for (Block block : pickaxeLevelZeroBlocks) {
            BlockState state = block.defaultBlockState();
            if (!state.is(BlockTags.MINEABLE_WITH_PICKAXE) || !woodPick.isCorrectToolForDrops(state)) {
                helper.fail("pickaxe level-0 block should allow wooden pickaxe drops: " + block.getDescriptionId());
                return;
            }
        }

        BlockState oreState = ModBlocks.BLOCK_POLYMETAL_ORE.get().defaultBlockState();
        if (!oreState.is(BlockTags.MINEABLE_WITH_PICKAXE)
                || !oreState.is(BlockTags.NEEDS_STONE_TOOL)
                || woodPick.isCorrectToolForDrops(oreState)
                || !stonePick.isCorrectToolForDrops(oreState)) {
            helper.fail("blockpolymetalore should match 1.12 pickaxe level 1 (stone tool)");
            return;
        }

        BlockState shipyardState = ModBlocks.BLOCK_SMALL_SHIPYARD.get().defaultBlockState();
        if (!shipyardState.is(BlockTags.MINEABLE_WITH_PICKAXE)
                || !shipyardState.is(BlockTags.NEEDS_DIAMOND_TOOL)
                || ironPick.isCorrectToolForDrops(shipyardState)
                || !diamondPick.isCorrectToolForDrops(shipyardState)) {
            helper.fail("blocksmallshipyard should match 1.12 pickaxe level 3 (diamond tool)");
            return;
        }

        if (ModBlocks.BLOCK_WAYPOINT.get().defaultBlockState().is(BlockTags.MINEABLE_WITH_PICKAXE)) {
            helper.fail("blockwaypoint should match 1.12 harvest level -1 (no pickaxe requirement)");
            return;
        }

        Block[] shovelBlocks = {
                ModBlocks.BLOCK_GRUDGE.get(),
                ModBlocks.BLOCK_GRUDGE_HEAVY.get(),
                ModBlocks.BLOCK_GRUDGE_HEAVY_DECO.get(),
                ModBlocks.BLOCK_GRUDGE_XP.get(),
                ModBlocks.BLOCK_POLYMETAL_GRAVEL.get()
        };
        for (Block block : shovelBlocks) {
            if (!block.defaultBlockState().is(BlockTags.MINEABLE_WITH_SHOVEL)) {
                helper.fail("shovel block should match 1.12 mineable/shovel: " + block.getDescriptionId());
                return;
            }
        }

        Block[] beaconBlocks = {
                ModBlocks.BLOCK_ABYSSIUM.get(),
                ModBlocks.BLOCK_GRUDGE.get(),
                ModBlocks.BLOCK_GRUDGE_HEAVY.get(),
                ModBlocks.BLOCK_GRUDGE_HEAVY_DECO.get(),
                ModBlocks.BLOCK_GRUDGE_XP.get(),
                ModBlocks.BLOCK_POLYMETAL.get(),
                ModBlocks.BLOCK_VOL_BLOCK.get()
        };
        for (Block block : beaconBlocks) {
            if (!block.defaultBlockState().is(BlockTags.BEACON_BASE_BLOCKS)) {
                helper.fail("1.12 isBeaconBase block should be in minecraft:beacon_base_blocks: " + block.getDescriptionId());
                return;
            }
        }

        Player player = helper.makeMockPlayer();
        BlockPos pos = helper.absolutePos(new BlockPos(1, 2, 1));
        List<ItemStack> oreDrops = Block.getDrops(oreState, helper.getLevel(), pos, null, player, stonePick);
        if (oreDrops.size() != 1
                || !oreDrops.get(0).is(ModItems.ABYSSMETAL1.get())
                || oreDrops.get(0).getCount() < 1
                || oreDrops.get(0).getCount() > 2) {
            helper.fail("blockpolymetalore loot table should drop 1..2 polymetal ingots");
            return;
        }

        List<ItemStack> gravelDrops = Block.getDrops(
                ModBlocks.BLOCK_POLYMETAL_GRAVEL.get().defaultBlockState(),
                helper.getLevel(),
                pos,
                null,
                player,
                new ItemStack(Items.STONE_SHOVEL));
        if (gravelDrops.size() != 1
                || !gravelDrops.get(0).is(ModBlocks.BLOCK_POLYMETAL_GRAVEL.get().asItem())) {
            helper.fail("blockpolymetalgravel loot table should drop itself");
            return;
        }

        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void b2PacketCodecRoundTripsEncodeAndDecodeCleanly(GameTestHelper helper) {
        CompoundTag formationTag = new CompoundTag();
        formationTag.putInt(GameplayCommandHandler.TAG_FORMATION_ID, 4);

        CompoundTag unitNameTag = new CompoundTag();
        unitNameTag.putInt(GameplayCommandHandler.TAG_TEAM_ID, 2);
        unitNameTag.putString(GameplayCommandHandler.TAG_TEAM_NAME, "Kantai 3");

        CompoundTag deskTag = new CompoundTag();
        deskTag.putInt(GameplayCommandHandler.TAG_X, 12);
        deskTag.putInt(GameplayCommandHandler.TAG_Y, 64);
        deskTag.putInt(GameplayCommandHandler.TAG_Z, -8);
        deskTag.putInt(GameplayCommandHandler.TAG_MODE, DeskReferenceMenu.BOOK_VARIANT);
        deskTag.putInt(GameplayCommandHandler.TAG_RADAR_ZOOM, 2);
        deskTag.putInt(GameplayCommandHandler.TAG_BOOK_CHAPTER, 3);
        deskTag.putInt(GameplayCommandHandler.TAG_BOOK_PAGE, 5);

        CompoundTag mountMoveTag = new CompoundTag();
        mountMoveTag.putInt(GameplayCommandHandler.TAG_MODE, LegacyMountEntity.KEY_FORWARD | LegacyMountEntity.KEY_JUMP);

        CompoundTag hitZoneTag = new CompoundTag();
        hitZoneTag.putInt(GameplayCommandHandler.TAG_SHIP_ID, 77);
        hitZoneTag.putInt(GameplayCommandHandler.TAG_HIT_HEIGHT, 88);
        hitZoneTag.putInt(GameplayCommandHandler.TAG_HIT_ANGLE, -45);

        ServerboundGameplayCommandPacket[] packets = {
                ServerboundGameplayCommandPacket.of(GameplayCommandType.SET_FORMATION, formationTag),
                ServerboundGameplayCommandPacket.of(GameplayCommandType.SET_UNIT_NAME, unitNameTag),
                ServerboundGameplayCommandPacket.of(GameplayCommandType.SYNC_DESK_FUNC, deskTag),
                ServerboundGameplayCommandPacket.of(GameplayCommandType.OPEN_MOUNT_SHIP_INVENTORY, new CompoundTag()),
                ServerboundGameplayCommandPacket.of(GameplayCommandType.MOUNT_MOVE, mountMoveTag),
                ServerboundGameplayCommandPacket.of(GameplayCommandType.SET_SHIP_HIT_ZONE, hitZoneTag)
        };

        for (ServerboundGameplayCommandPacket packet : packets) {
            FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());
            ServerboundGameplayCommandPacket.encode(packet, buffer);
            ServerboundGameplayCommandPacket decoded = ServerboundGameplayCommandPacket.decode(buffer);
            if (decoded.type() != packet.type() || !decoded.payload().equals(packet.payload())) {
                helper.fail("gameplay command packet should round-trip type and payload for " + packet.type());
                return;
            }
        }

        if (GameplayCommandType.fromOrdinal(-1) != GameplayCommandType.STOP_COMMAND
                || GameplayCommandType.fromOrdinal(999) != GameplayCommandType.STOP_COMMAND) {
            helper.fail("out-of-range GameplayCommandType ordinals should safely fall back to STOP_COMMAND");
            return;
        }

        ServerboundShipCommandPacket rationPacket = ServerboundShipCommandPacket.setCombatRationLevel(21, 201, 4);
        FriendlyByteBuf shipBuffer = new FriendlyByteBuf(Unpooled.buffer());
        ServerboundShipCommandPacket.encode(rationPacket, shipBuffer);
        ServerboundShipCommandPacket decodedShip = ServerboundShipCommandPacket.decode(shipBuffer);
        if (decodedShip.action() != ShipCommandAction.SET_COMBAT_RATION_LEVEL
                || decodedShip.shipId() != 21
                || decodedShip.shipUid() != 201
                || decodedShip.value() != 4) {
            helper.fail("ship command packet round-trip should preserve SET_COMBAT_RATION_LEVEL fields");
            return;
        }

        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void b2WaypointAndDeskBlockEntitySyncUpdatePackets(GameTestHelper helper) {
        ServerPlayer player = FakePlayerFactory.get(helper.getLevel(),
                new GameProfile(UUID.randomUUID(), "B2SyncOwner"));
        BlockPos waypointPos = helper.absolutePos(new BlockPos(1, 2, 1));
        BlockPos deskPos = helper.absolutePos(new BlockPos(2, 2, 1));
        helper.getLevel().setBlock(waypointPos, ModBlocks.BLOCK_WAYPOINT.get().defaultBlockState(), 3);
        helper.getLevel().setBlock(deskPos, ModBlocks.BLOCK_DESK.get().defaultBlockState(), 3);

        if (!(helper.getLevel().getBlockEntity(waypointPos) instanceof WaypointBlockEntity waypoint)
                || !(helper.getLevel().getBlockEntity(deskPos) instanceof DeskBlockEntity desk)) {
            helper.fail("waypoint and desk block entities should be instantiated cleanly");
            return;
        }

        waypoint.setOwner(player);
        waypoint.cycleStayMode();
        waypoint.setNextWaypoint(waypointPos.east(3));
        waypoint.setPairedChest(waypointPos.south(2));
        if (waypoint.getUpdatePacket() == null) {
            helper.fail("WaypointBlockEntity should expose a non-null ClientboundBlockEntityDataPacket");
            return;
        }

        WaypointBlockEntity syncedWaypoint = new WaypointBlockEntity(waypointPos, ModBlocks.BLOCK_WAYPOINT.get().defaultBlockState());
        syncedWaypoint.load(waypoint.getUpdateTag());
        if (syncedWaypoint.getStayMode() != 1
                || !waypointPos.east(3).equals(syncedWaypoint.getNextWaypoint())
                || !waypointPos.south(2).equals(syncedWaypoint.getPairedChest())
                || syncedWaypoint.getOwnerUid() != waypoint.getOwnerUid()) {
            helper.fail("WaypointBlockEntity update tag should preserve owner, stay mode, and links");
            return;
        }

        player.setPos(deskPos.getX() + 0.5D, deskPos.getY(), deskPos.getZ() + 0.5D);
        CompoundTag deskSyncPayload = new CompoundTag();
        deskSyncPayload.putInt(GameplayCommandHandler.TAG_X, deskPos.getX());
        deskSyncPayload.putInt(GameplayCommandHandler.TAG_Y, deskPos.getY());
        deskSyncPayload.putInt(GameplayCommandHandler.TAG_Z, deskPos.getZ());
        deskSyncPayload.putInt(GameplayCommandHandler.TAG_MODE, DeskReferenceMenu.BOOK_VARIANT);
        deskSyncPayload.putInt(GameplayCommandHandler.TAG_RADAR_ZOOM, 2);
        deskSyncPayload.putInt(GameplayCommandHandler.TAG_BOOK_CHAPTER, 3);
        deskSyncPayload.putInt(GameplayCommandHandler.TAG_BOOK_PAGE, 7);
        GameplayCommandHandler.handle(player, GameplayCommandType.SYNC_DESK_FUNC, deskSyncPayload);

        if (desk.getUpdatePacket() == null
                || desk.getSelectedFunction() != DeskReferenceMenu.BOOK_VARIANT
                || desk.getRadarZoomIndex() != 2
                || desk.getBookChapter() != 3
                || desk.getBookPage() != 7) {
            helper.fail("SYNC_DESK_FUNC should update DeskBlockEntity function, zoom, chapter, and page");
            return;
        }

        DeskBlockEntity syncedDesk = new DeskBlockEntity(deskPos, ModBlocks.BLOCK_DESK.get().defaultBlockState());
        syncedDesk.load(desk.getUpdateTag());
        if (syncedDesk.getSelectedFunction() != DeskReferenceMenu.BOOK_VARIANT
                || syncedDesk.getRadarZoomIndex() != 2
                || syncedDesk.getBookChapter() != 3
                || syncedDesk.getBookPage() != 7) {
            helper.fail("DeskBlockEntity update tag should round-trip synced desk fields");
            return;
        }

        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void b2PointerModeFormationAndUnitNameHandlers(GameTestHelper helper) {
        ServerPlayer player = FakePlayerFactory.get(helper.getLevel(),
                new GameProfile(UUID.randomUUID(), "B2CommandTester"));
        ItemStack pointer = new ItemStack(ModItems.POINTERITEM.get());
        com.lulan.shincolle.item.PointerItem.setMode(pointer, 1);
        player.setItemInHand(InteractionHand.MAIN_HAND, pointer);

        TeitokuData data = TeitokuHelper.get(player).resolve().orElse(null);
        if (data == null) {
            helper.fail("TeitokuData capability should be attached to FakePlayer");
            return;
        }

        data.setHasTeam(false);
        data.setCurrentTeamId(0);
        data.setShipUid(0, 2, 101);
        data.setShipUid(0, 4, 102);
        data.setCurrentTeamSelection(4, true);

        CompoundTag switchToCaressSingle = new CompoundTag();
        switchToCaressSingle.putInt(GameplayCommandHandler.TAG_MODE, 3);
        GameplayCommandHandler.handle(player, GameplayCommandType.SET_POINTER_MODE, switchToCaressSingle);
        if (!data.isShipSelected(0, 2) || data.isShipSelected(0, 4)) {
            helper.fail("cross-group pointer switch into single command mode should focus first ship in fleet");
            return;
        }

        data.clearCurrentTeamSelect();
        data.setCurrentTeamSelection(4, true);
        CompoundTag toggleCaressOff = new CompoundTag();
        toggleCaressOff.putInt(GameplayCommandHandler.TAG_MODE, 0);
        GameplayCommandHandler.handle(player, GameplayCommandType.SET_POINTER_MODE, toggleCaressOff);
        if (!data.isShipSelected(0, 4)) {
            helper.fail("toggling caress mode off within single mode (%3 == 0) should preserve ship selection");
            return;
        }

        CompoundTag groupMode = new CompoundTag();
        groupMode.putInt(GameplayCommandHandler.TAG_MODE, 1);
        GameplayCommandHandler.handle(player, GameplayCommandType.SET_POINTER_MODE, groupMode);
        CompoundTag singleMode = new CompoundTag();
        singleMode.putInt(GameplayCommandHandler.TAG_MODE, 0);
        GameplayCommandHandler.handle(player, GameplayCommandType.SET_POINTER_MODE, singleMode);
        if (!data.isShipSelected(0, 2) || data.isShipSelected(0, 4)) {
            helper.fail("switching from Group mode to Single mode should reset selection to the first ship");
            return;
        }

        CompoundTag formationThree = new CompoundTag();
        formationThree.putInt(GameplayCommandHandler.TAG_FORMATION_ID, 3);
        GameplayCommandHandler.handle(player, GameplayCommandType.SET_FORMATION, formationThree);
        if (data.getFormationId(0) != TeitokuData.DEFAULT_FORMATION_ID) {
            helper.fail("SET_FORMATION should keep formation 0 when current fleet has <= 4 ships");
            return;
        }

        data.setShipUid(0, 0, 103);
        data.setShipUid(0, 1, 104);
        data.setShipUid(0, 3, 105);
        GameplayCommandHandler.handle(player, GameplayCommandType.SET_FORMATION, formationThree);
        if (data.getFormationId(0) != 3) {
            helper.fail("SET_FORMATION should apply formation id 3 once current fleet has > 4 ships");
            return;
        }

        CompoundTag unitNameDefaultTeam = new CompoundTag();
        unitNameDefaultTeam.putString(GameplayCommandHandler.TAG_TEAM_NAME, "  DesDiv 6  ");
        GameplayCommandHandler.handle(player, GameplayCommandType.SET_UNIT_NAME, unitNameDefaultTeam);

        CompoundTag unitNameSlotOne = new CompoundTag();
        unitNameSlotOne.putInt(GameplayCommandHandler.TAG_TEAM_ID, 1);
        unitNameSlotOne.putString(GameplayCommandHandler.TAG_TEAM_NAME, "SubRon 2");
        GameplayCommandHandler.handle(player, GameplayCommandType.SET_UNIT_NAME, unitNameSlotOne);

        if (!"DesDiv 6".equals(data.getTeamName(0)) || !"SubRon 2".equals(data.getTeamName(1))) {
            helper.fail("SET_UNIT_NAME should persist per-fleet names independently of admiral faction creation");
            return;
        }

        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void b2OpToolToggleRequiresOpPermission(GameTestHelper helper) {
        ServerPlayer nonOp = FakePlayerFactory.get(helper.getLevel(),
                new GameProfile(UUID.randomUUID(), "B2NonOpPlayer"));
        ServerPlayer opPlayer = new FakePlayer(helper.getLevel(),
                new GameProfile(UUID.randomUUID(), "B2OpPlayer")) {
            @Override
            public boolean hasPermissions(int permissionLevel) {
                return permissionLevel <= 2;
            }
        };

        String ruleTarget = "shincolle:b2_op_gate_test_target";
        WorldCombatRulesSavedData rules = WorldCombatRulesSavedData.get(helper.getLevel());
        if (rules.isUnattackable(ruleTarget)) {
            rules.toggleUnattackable(ruleTarget);
        }

        CompoundTag payload = new CompoundTag();
        payload.putString(GameplayCommandHandler.TAG_TARGET_CLASS, ruleTarget);
        GameplayCommandHandler.handle(nonOp, GameplayCommandType.OPTOOL_TOGGLE_UNATTACKABLE, payload);
        if (rules.isUnattackable(ruleTarget)) {
            helper.fail("non-OP player should be blocked by the restored level-2 permission gate");
            return;
        }

        GameplayCommandHandler.handle(opPlayer, GameplayCommandType.OPTOOL_TOGGLE_UNATTACKABLE, payload);
        if (!rules.isUnattackable(ruleTarget)) {
            helper.fail("OP level 2+ player should be able to toggle unattackable rules");
            return;
        }

        GameplayCommandHandler.handle(opPlayer, GameplayCommandType.OPTOOL_TOGGLE_UNATTACKABLE, payload);
        if (rules.isUnattackable(ruleTarget)) {
            helper.fail("OP level 2+ player should be able to remove unattackable rules cleanly");
            return;
        }

        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void b2MountMoveOpenInventorySkillAndHitZoneParity(GameTestHelper helper) {
        ServerPlayer owner = FakePlayerFactory.get(helper.getLevel(),
                new GameProfile(UUID.randomUUID(), "B2MountOwner"));
        ServerPlayer intruder = FakePlayerFactory.get(helper.getLevel(),
                new GameProfile(UUID.randomUUID(), "B2MountIntruder"));

        LegacyShipEntity ship = ModEntityTypes.LEGACY_SHIP.get().create(helper.getLevel());
        if (ship == null) {
            helper.fail("legacy ship should be creatable for mount and hit-zone tests");
            return;
        }

        ship.setVariantEggMeta(35);
        ship.finalizeSpawn(helper.getLevel(), helper.getLevel().getCurrentDifficultyAt(BlockPos.ZERO),
                net.minecraft.world.entity.MobSpawnType.SPAWN_EGG, null, null);
        ship.setOwner(owner);
        ship.setModelState(1);
        ship.setGrudge(1000);
        ship.setMorale(4000);
        BlockPos origin = helper.absolutePos(new BlockPos(2, 2, 2));
        ship.setPos(origin.getX() + 0.5D, origin.getY(), origin.getZ() + 0.5D);
        helper.getLevel().addFreshEntity(ship);
        ship.tickCount = 128;
        LegacyShipBehaviorCatalog.tickLegacyShipMountBehavior(ship);

        if (!(ship.getVehicle() instanceof LegacyShipMountEntity shipMount)) {
            helper.fail("CarrierWDemon with model bit 0 and fuel should summon LegacyShipMountEntity");
            return;
        }

        owner.setYRot(0.0F);
        owner.startRiding(shipMount, true);
        if (PlayerSkillHostResolver.resolve(owner).mode() != MorphHostMode.MOUNT
                || PlayerSkillHostResolver.resolve(owner).ship() != ship) {
            helper.fail("PlayerSkillHostResolver should resolve LegacyShipMountEntity to MorphHostMode.MOUNT with host ship");
            return;
        }

        MorphHelper.tick(owner);
        PlayerSkillRuntimeState skillState = MorphHelper.getPlayerSkillRuntimeState(owner);
        if (!skillState.isVisible() || skillState.getHostMode() != MorphHostMode.MOUNT) {
            helper.fail("MorphHelper.tick should publish visible MOUNT skill HUD state when riding ship mount");
            return;
        }

        if (GameplayCommandHandler.resolveMountInventoryShip(owner) != ship
                || !ShipCommandService.openShipInventoryForPlayer(owner, ship)
                || ShipCommandService.openShipInventoryForPlayer(intruder, ship)) {
            helper.fail("mount inventory resolver should allow owning rider and block non-owner");
            return;
        }
        GameplayCommandHandler.handle(owner, GameplayCommandType.OPEN_MOUNT_SHIP_INVENTORY, new CompoundTag());

        net.minecraft.world.phys.Vec3 beforePos = shipMount.position();
        CompoundTag intruderMove = new CompoundTag();
        intruderMove.putInt(GameplayCommandHandler.TAG_MODE, 0);
        CompoundTag ownerMove = new CompoundTag();
        ownerMove.putInt(GameplayCommandHandler.TAG_MODE, LegacyMountEntity.KEY_FORWARD | LegacyMountEntity.KEY_JUMP);
        GameplayCommandHandler.handle(owner, GameplayCommandType.MOUNT_MOVE, ownerMove);
        GameplayCommandHandler.handle(intruder, GameplayCommandType.MOUNT_MOVE, intruderMove);
        if (shipMount.getKeyPressed() != (LegacyMountEntity.KEY_FORWARD | LegacyMountEntity.KEY_JUMP)
                || shipMount.getKeyTick() != 10) {
            helper.fail("MOUNT_MOVE should store owner steering bitmask and resist non-owner interference");
            return;
        }

        shipMount.tick();
        if (shipMount.position().y <= beforePos.y + 0.1D
                || shipMount.position().z <= beforePos.z + 0.05D) {
            helper.fail("LegacyShipMountEntity tick should apply WASD+Jump steering along player yaw");
            return;
        }

        net.minecraft.world.phys.Vec3 morphMountVec = LegacyMountEntity.resolveKeyMovement(
                owner, LegacyMountEntity.KEY_FORWARD | LegacyMountEntity.KEY_JUMP, 0.3D, true);
        if (morphMountVec.y <= 0.4D || morphMountVec.z <= 0.2D) {
            helper.fail("LegacyMountEntity.resolveKeyMovement should compute forward and vertical jump motion");
            return;
        }

        owner.stopRiding();
        ship.stopRiding();
        shipMount.discard();
        ship.startRiding(owner, true);
        if (PlayerSkillHostResolver.resolve(owner).mode() != MorphHostMode.RIDER
                || PlayerSkillHostResolver.resolve(owner).ship() != ship
                || GameplayCommandHandler.resolveMountInventoryShip(owner) != ship) {
            helper.fail("carrying an owned ship passenger should resolve RIDER skills and mount inventory");
            return;
        }
        ship.stopRiding();

        owner.setPos(ship.getX() + 1.0D, ship.getY(), ship.getZ());
        ItemStack caressPointer = new ItemStack(ModItems.POINTERITEM.get());
        com.lulan.shincolle.item.PointerItem.setMode(caressPointer, 3);
        owner.setItemInHand(InteractionHand.MAIN_HAND, caressPointer);

        CompoundTag headHit = new CompoundTag();
        headHit.putInt(GameplayCommandHandler.TAG_SHIP_ID, ship.getId());
        headHit.putInt(GameplayCommandHandler.TAG_HIT_HEIGHT, 85);
        headHit.putInt(GameplayCommandHandler.TAG_HIT_ANGLE, 15);
        GameplayCommandHandler.handle(owner, GameplayCommandType.SET_SHIP_HIT_ZONE, headHit);
        if (ship.getLegacyHitHeight() != 85 || ship.getLegacyHitAngle() != 15) {
            helper.fail("SET_SHIP_HIT_ZONE should store hit height and angle on LegacyShipEntity");
            return;
        }

        int beforeHeadMorale = ship.getMorale();
        ship.mobInteract(owner, InteractionHand.MAIN_HAND);
        if (ship.getMorale() - beforeHeadMorale != 30) {
            helper.fail("caress in head zone (hitHeight >= 75) should grant +30 morale bonus");
            return;
        }

        CompoundTag bodyHit = new CompoundTag();
        bodyHit.putInt(GameplayCommandHandler.TAG_SHIP_ID, ship.getId());
        bodyHit.putInt(GameplayCommandHandler.TAG_HIT_HEIGHT, 20);
        bodyHit.putInt(GameplayCommandHandler.TAG_HIT_ANGLE, 0);
        GameplayCommandHandler.handle(owner, GameplayCommandType.SET_SHIP_HIT_ZONE, bodyHit);
        int beforeBodyMorale = ship.getMorale();
        ship.mobInteract(owner, InteractionHand.MAIN_HAND);
        if (ship.getMorale() - beforeBodyMorale != 20) {
            helper.fail("caress outside head zone should keep standard +20 morale");
            return;
        }

        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void b3TeamLifecycleCooldownAndNameValidation(GameTestHelper helper) {
        TeitokuData freshData = new TeitokuData();
        if (freshData.getTeamCooldown() != TeitokuData.INITIAL_TEAM_COOLDOWN) {
            helper.fail("fresh TeitokuData should default teamCooldown to INITIAL_TEAM_COOLDOWN (20)");
            return;
        }
        freshData.setTeamCooldown(999);
        freshData.loadFromTag(new CompoundTag());
        if (freshData.getTeamCooldown() != TeitokuData.INITIAL_TEAM_COOLDOWN) {
            helper.fail("loading a TeitokuData tag without TeamCD should default teamCooldown to INITIAL_TEAM_COOLDOWN (20)");
            return;
        }

        ServerPlayer leader = FakePlayerFactory.get(helper.getLevel(),
                new GameProfile(UUID.randomUUID(), "B3LifecycleLeader"));
        TeitokuData leaderData = TeitokuHelper.get(leader).resolve().orElse(null);
        if (leaderData == null || leaderData.getTeamCooldown() != TeitokuData.INITIAL_TEAM_COOLDOWN) {
            helper.fail("newly attached player TeitokuData should start with initial 20-tick team cooldown");
            return;
        }

        TeitokuSavedData uidRegistry = TeitokuSavedData.get(helper.getLevel());
        int leaderUid = uidRegistry.getOrCreateUid(leader);
        if (leaderUid <= 0
                || uidRegistry.getUid(leader.getUUID()) != leaderUid
                || uidRegistry.getUid(UUID.randomUUID()) != 0) {
            helper.fail("TeitokuSavedData.getUid should return assigned UID for known UUID and 0 for unknown UUID");
            return;
        }
        leaderData.setPlayerUid(leaderUid);
        leaderData.setPlayerName(leader.getGameProfile().getName());
        leaderData.setHasTeam(false);

        if (TeitokuHelper.createOwnTeam(leader, "Alpha Fleet") || leaderData.hasTeam()) {
            helper.fail("team creation should be gated while initial teamCooldown > 0");
            return;
        }

        leaderData.setTeamCooldown(0);
        if (TeitokuHelper.createOwnTeam(leader, "")
                || TeitokuHelper.createOwnTeam(leader, "A")
                || TeitokuHelper.createOwnTeam(leader, " X ")
                || leaderData.hasTeam()) {
            helper.fail("team creation should reject blank and 1-character normalized team names");
            return;
        }

        TeamSavedData teams = TeamSavedData.get(helper.getLevel());
        try {
            if (!TeitokuHelper.createOwnTeam(leader, "Alpha Fleet")
                    || !leaderData.hasTeam()
                    || leaderData.getTeamCooldown() != TeitokuData.DEFAULT_TEAM_COOLDOWN
                    || !teams.hasTeam(leaderUid)
                    || !"Alpha Fleet".equals(teams.getTeam(leaderUid).getTeamName())) {
                helper.fail("valid 2+ char createOwnTeam should register team, set hasTeam, and reset cooldown to 6000");
                return;
            }

            TeitokuHelper.renameOwnTeam(leader, "Z");
            if (!"Alpha Fleet".equals(teams.getTeam(leaderUid).getTeamName())) {
                helper.fail("renameOwnTeam should ignore normalized names shorter than 2 characters");
                return;
            }

            TeitokuHelper.renameOwnTeam(leader, "Beta Fleet");
            if (!"Beta Fleet".equals(teams.getTeam(leaderUid).getTeamName())) {
                helper.fail("renameOwnTeam should update team name when normalized length >= 2");
                return;
            }

            if (TeitokuHelper.disbandOwnTeam(leader) || !leaderData.hasTeam() || !teams.hasTeam(leaderUid)) {
                helper.fail("disbandOwnTeam should remain gated while teamCooldown > 0");
                return;
            }

            leaderData.setTeamCooldown(0);
            if (!TeitokuHelper.disbandOwnTeam(leader)
                    || leaderData.hasTeam()
                    || teams.hasTeam(leaderUid)
                    || leaderData.getTeamCooldown() != TeitokuData.DEFAULT_TEAM_COOLDOWN) {
                helper.fail("disbandOwnTeam after cooldown expires should remove team, clear hasTeam, and set 6000 cooldown");
                return;
            }

            int staleId = 99701;
            int allyWatcherId = 99702;
            int banWatcherId = 99703;
            int replacementId = 99704;
            try {
                teams.createTeam(staleId, "B3StaleLeader", "Old Fleet");
                teams.createTeam(allyWatcherId, "B3AllyWatcher", "Ally Watcher");
                teams.createTeam(banWatcherId, "B3BanWatcher", "Ban Watcher");
                teams.addAllyRelationship(allyWatcherId, staleId);
                teams.addBannedRelationship(banWatcherId, staleId);

                teams.createTeam(replacementId, "B3StaleLeader", "Fresh Fleet");
                if (teams.hasTeam(staleId)
                        || !teams.hasTeam(replacementId)
                        || !"Fresh Fleet".equals(teams.getTeam(replacementId).getTeamName())) {
                    helper.fail("TeamSavedData.createTeam should remove stale teams led by the same leader name");
                    return;
                }
                TeamData allyWatcher = teams.getTeam(allyWatcherId);
                TeamData banWatcher = teams.getTeam(banWatcherId);
                if (allyWatcher == null || allyWatcher.isAlly(staleId)
                        || banWatcher == null || banWatcher.isBanned(staleId)) {
                    helper.fail("stale leader team cleanup should scrub ally and banned references to the removed team ID");
                    return;
                }
            } finally {
                teams.removeTeam(staleId);
                teams.removeTeam(replacementId);
                teams.removeTeam(allyWatcherId);
                teams.removeTeam(banWatcherId);
            }
        } finally {
            teams.removeTeam(leaderUid);
        }

        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void b3TeamAllyBannedExistenceAndBlockOwnershipPermissions(GameTestHelper helper) {
        TeamSavedData teams = TeamSavedData.get(helper.getLevel());
        int teamA = 99801;
        int targetAlly = 99802;
        int targetBanned = 99803;

        ServerPlayer owner = FakePlayerFactory.get(helper.getLevel(),
                new GameProfile(UUID.randomUUID(), "B3BlockOwner"));
        ServerPlayer ally = FakePlayerFactory.get(helper.getLevel(),
                new GameProfile(UUID.randomUUID(), "B3BlockAlly"));
        ServerPlayer neutral = FakePlayerFactory.get(helper.getLevel(),
                new GameProfile(UUID.randomUUID(), "B3BlockNeutral"));
        ServerPlayer opPlayer = new FakePlayer(helper.getLevel(),
                new GameProfile(UUID.randomUUID(), "B3BlockOp")) {
            @Override
            public boolean hasPermissions(int permissionLevel) {
                return permissionLevel <= 2;
            }
        };

        TeitokuSavedData uidRegistry = TeitokuSavedData.get(helper.getLevel());
        int ownerUid = uidRegistry.getOrCreateUid(owner);
        int allyUid = uidRegistry.getOrCreateUid(ally);
        int neutralUid = uidRegistry.getOrCreateUid(neutral);

        TeitokuHelper.get(owner).ifPresent(d -> {
            d.setPlayerUid(ownerUid);
            d.setPlayerName(owner.getGameProfile().getName());
        });
        TeitokuHelper.get(ally).ifPresent(d -> {
            d.setPlayerUid(allyUid);
            d.setPlayerName(ally.getGameProfile().getName());
        });
        TeitokuHelper.get(neutral).ifPresent(d -> {
            d.setPlayerUid(neutralUid);
            d.setPlayerName(neutral.getGameProfile().getName());
        });

        try {
            TeamData probeTeam = teams.createTeam(teamA, "B3ProbeLeader", "Probe Fleet");
            probeTeam.addAlly(targetAlly);
            probeTeam.addBanned(targetBanned);
            if (TeitokuHelper.isAlly(helper.getLevel(), teamA, targetAlly)
                    || TeitokuHelper.isBanned(helper.getLevel(), teamA, targetBanned)) {
                helper.fail("isAlly and isBanned must require both teams to exist when team IDs differ");
                return;
            }

            teams.createTeam(targetAlly, "B3AllyLeader", "Ally Fleet");
            teams.createTeam(targetBanned, "B3BannedLeader", "Banned Fleet");
            if (!TeitokuHelper.isAlly(helper.getLevel(), teamA, targetAlly)
                    || !TeitokuHelper.isBanned(helper.getLevel(), teamA, targetBanned)) {
                helper.fail("isAlly and isBanned should evaluate true once both active teams exist");
                return;
            }

            teams.createTeam(ownerUid, owner.getGameProfile().getName(), "Owner Fleet");
            teams.createTeam(allyUid, ally.getGameProfile().getName(), "Ally Fleet");
            teams.createTeam(neutralUid, neutral.getGameProfile().getName(), "Neutral Fleet");
            teams.addAllyRelationship(ownerUid, allyUid);

            BlockPos waypointPos = helper.absolutePos(new BlockPos(1, 2, 1));
            helper.getLevel().setBlock(waypointPos, ModBlocks.BLOCK_WAYPOINT.get().defaultBlockState(), 3);
            if (!(helper.getLevel().getBlockEntity(waypointPos) instanceof WaypointBlockEntity waypoint)) {
                helper.fail("WaypointBlockEntity should instantiate cleanly for B3 ownership permission checks");
                return;
            }

            waypoint.setOwner(owner);
            if (!waypoint.canEdit(owner)
                    || !waypoint.canEdit(ally)
                    || !waypoint.canEdit(opPlayer)
                    || waypoint.canEdit(neutral)) {
                helper.fail("WaypointBlockEntity.canEdit should allow owner, allied team, and level-2+ OP while rejecting neutral players");
                return;
            }

            PlayerOwnerData zeroUidOwner = new PlayerOwnerData(owner.getUUID(), owner.getGameProfile().getName(), 0);
            if (!zeroUidOwner.canEdit(ally)
                    || !zeroUidOwner.canEdit(opPlayer)
                    || zeroUidOwner.canEdit(neutral)) {
                helper.fail("PlayerOwnerData with uid=0 should resolve owner UID from TeitokuSavedData for ally checks while enforcing OP/non-ally rules");
                return;
            }
        } finally {
            teams.removeTeam(teamA);
            teams.removeTeam(targetAlly);
            teams.removeTeam(targetBanned);
            teams.removeTeam(ownerUid);
            teams.removeTeam(allyUid);
            teams.removeTeam(neutralUid);
        }

        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void b3Part2FriendlyFirePvpModesAndCommandRevengeTargeting(GameTestHelper helper) {
        ServerPlayer owner = makeServerBackedPlayer(helper, "B3P2Owner");
        ServerPlayer ally = makeServerBackedPlayer(helper, "B3P2Ally");
        ServerPlayer banned = makeServerBackedPlayer(helper, "B3P2Banned");
        ServerPlayer neutral = makeServerBackedPlayer(helper, "B3P2Neutral");

        TeitokuSavedData uidRegistry = TeitokuSavedData.get(helper.getLevel());
        int ownerUid = uidRegistry.getOrCreateUid(owner);
        int allyUid = uidRegistry.getOrCreateUid(ally);
        int bannedUid = uidRegistry.getOrCreateUid(banned);
        int neutralUid = uidRegistry.getOrCreateUid(neutral);

        TeitokuHelper.get(owner).ifPresent(d -> {
            d.setPlayerUid(ownerUid);
            d.setPlayerName(owner.getGameProfile().getName());
        });
        TeitokuHelper.get(ally).ifPresent(d -> {
            d.setPlayerUid(allyUid);
            d.setPlayerName(ally.getGameProfile().getName());
        });
        TeitokuHelper.get(banned).ifPresent(d -> {
            d.setPlayerUid(bannedUid);
            d.setPlayerName(banned.getGameProfile().getName());
        });
        TeitokuHelper.get(neutral).ifPresent(d -> {
            d.setPlayerUid(neutralUid);
            d.setPlayerName(neutral.getGameProfile().getName());
        });

        TeamSavedData teams = TeamSavedData.get(helper.getLevel());
        teams.createTeam(ownerUid, owner.getGameProfile().getName(), "Owner Fleet");
        teams.createTeam(allyUid, ally.getGameProfile().getName(), "Ally Fleet");
        teams.createTeam(bannedUid, banned.getGameProfile().getName(), "Banned Fleet");
        teams.createTeam(neutralUid, neutral.getGameProfile().getName(), "Neutral Fleet");
        teams.addAllyRelationship(ownerUid, allyUid);
        teams.addBannedRelationship(ownerUid, bannedUid);

        BlockPos anchor = helper.absolutePos(new BlockPos(2, 2, 2));
        owner.moveTo(anchor.getX() + 0.5D, anchor.getY(), anchor.getZ() + 0.5D);
        neutral.moveTo(anchor.getX() + 3.5D, anchor.getY(), anchor.getZ() + 0.5D);
        ally.moveTo(anchor.getX() + 1.5D, anchor.getY(), anchor.getZ() + 2.5D);
        banned.moveTo(anchor.getX() + 3.5D, anchor.getY(), anchor.getZ() + 1.5D);
        LegacyShipEntity ownerShip = createOwnedShip(helper, owner, 2, anchor.getX() + 0.5D, anchor.getY(), anchor.getZ() + 0.5D);
        LegacyShipEntity bannedShip = createOwnedShip(helper, banned, 62, anchor.getX() + 2.5D, anchor.getY(), anchor.getZ() + 0.5D);
        LegacyShipEntity allyShip = createOwnedShip(helper, ally, 2, anchor.getX() + 0.5D, anchor.getY(), anchor.getZ() + 2.5D);
        LegacyShipEntity hostileShip = ModEntityTypes.LEGACY_SHIP.get().create(helper.getLevel());
        if (ownerShip == null || bannedShip == null || allyShip == null || hostileShip == null) {
            helper.fail("ships should instantiate cleanly for B3 Part 2 combat tests");
            return;
        }
        hostileShip.setVariantEggMeta(2062);
        hostileShip.setPos(anchor.getX() + 4.5D, anchor.getY(), anchor.getZ() + 0.5D);
        helper.getLevel().addFreshEntity(hostileShip);

        Zombie dummyEnemy = EntityType.ZOMBIE.create(helper.getLevel());
        Cow neutralCow = EntityType.COW.create(helper.getLevel());
        if (dummyEnemy == null || neutralCow == null) {
            helper.fail("target entities should instantiate cleanly for B3 Part 2 tests");
            return;
        }
        dummyEnemy.setPos(anchor.getX() + 6.5D, anchor.getY(), anchor.getZ() + 2.5D);
        neutralCow.setPos(anchor.getX() + 2.5D, anchor.getY(), anchor.getZ() + 2.5D);
        helper.getLevel().addFreshEntity(dummyEnemy);
        helper.getLevel().addFreshEntity(neutralCow);

        LegacyShipRensouhouEntity rensouhou = LegacyShipRensouhouEntity.create(helper.getLevel(), ownerShip, dummyEnemy);
        if (rensouhou == null) {
            helper.fail("LegacyShipRensouhouEntity should instantiate for summon hurt guard checks");
            return;
        }
        helper.getLevel().addFreshEntity(rensouhou);

        try {
            ShinColleConfig.setTestOverride("friendlyFire", false);
            ownerShip.setHealth(ownerShip.getMaxHealth());
            rensouhou.setHealth(rensouhou.getMaxHealth());
            if (ownerShip.hurt(helper.getLevel().damageSources().playerAttack(neutral), 6.0F)
                    || rensouhou.hurt(helper.getLevel().damageSources().playerAttack(neutral), 6.0F)) {
                helper.fail("friendlyFire=false should block non-owner player damage on ships and summons");
                return;
            }
            if (!ownerShip.hurt(helper.getLevel().damageSources().playerAttack(owner), 6.0F)
                    || !rensouhou.hurt(helper.getLevel().damageSources().playerAttack(owner), 6.0F)) {
                helper.fail("friendlyFire=false should still allow owner player hits on ships and summons");
                return;
            }

            ShinColleConfig.setTestOverride("friendlyFire", true);
            bannedShip.setShipLevel(120);
            bannedShip.invulnerableTime = 0;
            bannedShip.setHealth(bannedShip.getMaxHealth());
            float hpBeforeFull = bannedShip.getHealth();
            ShinColleConfig.setTestOverride("dmgSvS", 100);
            bannedShip.hurt(helper.getLevel().damageSources().mobAttack(ownerShip), 50.0F);
            float damageAt100 = hpBeforeFull - bannedShip.getHealth();

            bannedShip.invulnerableTime = 0;
            bannedShip.setHealth(bannedShip.getMaxHealth());
            float hpBeforeScaled = bannedShip.getHealth();
            ShinColleConfig.setTestOverride("dmgSvS", 20);
            bannedShip.hurt(helper.getLevel().damageSources().mobAttack(ownerShip), 50.0F);
            float damageAt20 = hpBeforeScaled - bannedShip.getHealth();
            if (damageAt100 <= 5.0F || damageAt20 <= 0.0F || damageAt20 >= damageAt100 * 0.5F) {
                helper.fail("dmgSvS=20 should scale friendly ship-vs-ship damage to ~20% vs dmgSvS=100 (at100="
                        + damageAt100 + ", at20=" + damageAt20 + ")");
                return;
            }

            ShinColleConfig.setTestOverride("shipAttackPlayer", 0);
            if (ownerShip.canEngage(banned) || ownerShip.canEngage(neutral)
                    || ownerShip.canEngage(ally) || ownerShip.canEngage(owner)) {
                helper.fail("shipAttackPlayer=0 should prevent friendly ships from targeting any player");
                return;
            }
            ShinColleConfig.setTestOverride("shipAttackPlayer", 1);
            if (!ownerShip.canEngage(banned) || ownerShip.canEngage(neutral)
                    || ownerShip.canEngage(ally) || ownerShip.canEngage(owner)) {
                helper.fail("shipAttackPlayer=1 should target only banned-team players");
                return;
            }
            ShinColleConfig.setTestOverride("shipAttackPlayer", 2);
            if (!ownerShip.canEngage(banned) || !ownerShip.canEngage(neutral)
                    || ownerShip.canEngage(ally) || ownerShip.canEngage(owner)) {
                helper.fail("shipAttackPlayer=2 should target banned and neutral players while excluding allies and owner");
                return;
            }
            ShinColleConfig.setTestOverride("shipAttackPlayer", 3);
            if (!ownerShip.canEngage(banned) || !ownerShip.canEngage(neutral)
                    || !ownerShip.canEngage(ally) || ownerShip.canEngage(owner)) {
                helper.fail("shipAttackPlayer=3 should target all non-owner players including allies");
                return;
            }

            ShinColleConfig.setTestOverride("mobAttackPlayer", false);
            if (hostileShip.canEngage(neutral)) {
                helper.fail("mobAttackPlayer=false should stop hostile ships from auto-engaging players");
                return;
            }
            ShinColleConfig.setTestOverride("mobAttackPlayer", true);
            if (!hostileShip.canEngage(neutral)) {
                helper.fail("mobAttackPlayer=true should allow hostile ships to engage non-creative players");
                return;
            }

            if (ownerShip.canEngage(neutralCow) || !ownerShip.canCommandOrRevengeEngage(neutralCow)) {
                helper.fail("neutral mob should fail passive canEngage while passing canCommandOrRevengeEngage");
                return;
            }
            if (ShipCommandService.handleResultForTesting(owner,
                    ServerboundShipCommandPacket.attack(0, ownerShip.getId(), ownerShip.getShipUid(), neutralCow.getId()))
                    != ShipCommandService.ShipCommandResult.SUCCESS
                    || ownerShip.getTarget() != neutralCow) {
                helper.fail("manual pointer attack command should engage neutral targets allowed by canCommandOrRevengeEngage");
                return;
            }
            if (ShipCommandService.handleResultForTesting(owner,
                    ServerboundShipCommandPacket.attack(0, ownerShip.getId(), ownerShip.getShipUid(), allyShip.getId()))
                    != ShipCommandService.ShipCommandResult.TARGET_NOT_ENGAGEABLE) {
                helper.fail("manual pointer attack command must still reject allied ships");
                return;
            }
        } finally {
            ShinColleConfig.clearTestOverrides();
            teams.removeTeam(ownerUid);
            teams.removeTeam(allyUid);
            teams.removeTeam(bannedUid);
            teams.removeTeam(neutralUid);
        }

        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void b3Part2OwnerPaperKaitaiHammerAndDeskRadarSync(GameTestHelper helper) {
        ServerPlayer ownerA = makeServerBackedPlayer(helper, "B3P2OwnerA");
        ServerPlayer ownerB = makeServerBackedPlayer(helper, "B3P2OwnerB");

        TeitokuSavedData uidRegistry = TeitokuSavedData.get(helper.getLevel());
        int ownerAUid = uidRegistry.getOrCreateUid(ownerA);
        int ownerBUid = uidRegistry.getOrCreateUid(ownerB);

        TeitokuHelper.get(ownerA).ifPresent(d -> {
            d.setPlayerUid(ownerAUid);
            d.setPlayerName(ownerA.getGameProfile().getName());
        });
        TeitokuHelper.get(ownerB).ifPresent(d -> {
            d.setPlayerUid(ownerBUid);
            d.setPlayerName(ownerB.getGameProfile().getName());
        });

        BlockPos anchor = helper.absolutePos(new BlockPos(2, 2, 2));
        ownerA.moveTo(anchor.getX() + 0.5D, anchor.getY(), anchor.getZ() + 0.5D);
        ownerB.moveTo(anchor.getX() + 1.5D, anchor.getY(), anchor.getZ() + 0.5D);
        LegacyShipEntity transferShip = createOwnedShip(helper, ownerA, 2, anchor.getX() + 0.5D, anchor.getY(), anchor.getZ() + 0.5D);
        LegacyShipEntity bystanderShip = createOwnedShip(helper, ownerB, 2, anchor.getX() + 2.5D, anchor.getY(), anchor.getZ() + 0.5D);
        LegacyShipEntity dismantleShip = createOwnedShip(helper, ownerB, 2, anchor.getX() + 0.5D, anchor.getY(), anchor.getZ() + 2.5D);
        LegacyShipEntity killShip = createOwnedShip(helper, ownerB, 2, anchor.getX() + 2.5D, anchor.getY(), anchor.getZ() + 2.5D);
        if (transferShip == null || bystanderShip == null || dismantleShip == null || killShip == null) {
            helper.fail("ships should instantiate cleanly for OwnerPaper, KaitaiHammer, and radar tests");
            return;
        }

        boolean addedToLevelPlayers = false;
        List<ServerPlayer> levelPlayers = helper.getLevel().players();
        try {
            ItemStack paper = new ItemStack(ModItems.OWNERPAPER.get());
            ownerA.setItemInHand(InteractionHand.MAIN_HAND, paper);
            ModItems.OWNERPAPER.get().use(helper.getLevel(), ownerA, InteractionHand.MAIN_HAND);
            ownerB.setItemInHand(InteractionHand.MAIN_HAND, paper);
            ModItems.OWNERPAPER.get().use(helper.getLevel(), ownerB, InteractionHand.MAIN_HAND);

            if (!levelPlayers.contains(ownerB)) {
                levelPlayers.add(ownerB);
                addedToLevelPlayers = true;
            }

            ownerA.setShiftKeyDown(true);
            ownerA.setItemInHand(InteractionHand.MAIN_HAND, paper);
            transferShip.mobInteract(ownerA, InteractionHand.MAIN_HAND);
            ownerA.setShiftKeyDown(false);
            if (!transferShip.isOwnedBy(ownerB) || transferShip.isOwnedBy(ownerA) || transferShip.getEmotion() != 2) {
                helper.fail("OwnerPaper transfer should assign new owner and trigger T_T emotion (2)");
                return;
            }

            ShinColleConfig.setTestOverride("kaitaiAmountSmall", 20);
            ShinColleConfig.setTestOverride("kaitaiAmountLarge", 20);
            ShinColleConfig.setTestOverride("easyMode", false);

            ItemStack hammer = new ItemStack(ModItems.KAITAIHAMMER.get());
            ownerB.setShiftKeyDown(true);
            ownerB.setItemInHand(InteractionHand.MAIN_HAND, hammer);
            dismantleShip.mobInteract(ownerB, InteractionHand.MAIN_HAND);
            ownerA.setShiftKeyDown(false);
            ownerB.setShiftKeyDown(false);
            if (dismantleShip.isAlive() || !bystanderShip.isAlive()) {
                helper.fail("KaitaiHammer sneak-right-click should dismantle the target ship without harming bystanders");
                return;
            }

            List<ItemStack> kaitaiDrops = helper.getLevel()
                    .getEntitiesOfClass(ItemEntity.class, dismantleShip.getBoundingBox().inflate(4.0D))
                    .stream()
                    .map(ItemEntity::getItem)
                    .toList();
            if (kaitaiDrops.stream().noneMatch(stack -> stack.is(ModItems.GRUDGE.get()))
                    || kaitaiDrops.stream().noneMatch(stack -> stack.is(ModItems.ABYSSMETAL.get()))
                    || kaitaiDrops.stream().noneMatch(stack -> stack.is(ModItems.AMMO.get()))
                    || kaitaiDrops.stream().noneMatch(stack -> stack.is(ModItems.ABYSSMETAL1.get()))) {
                helper.fail("KaitaiHammer dismantle should drop all 4 legacy material stacks");
                return;
            }

            List<ItemStack> normalLargeDrops = LegacyShipBehaviorCatalog.createLegacyKaitaiDrops(
                    ShipEntitySpecs.getByEggMeta(14), false, RandomSource.create(42L));
            ShinColleConfig.setTestOverride("easyMode", true);
            List<ItemStack> easyLargeDrops = LegacyShipBehaviorCatalog.createLegacyKaitaiDrops(
                    ShipEntitySpecs.getByEggMeta(14), false, RandomSource.create(42L));
            if (normalLargeDrops.size() != 4 || easyLargeDrops.size() != 4
                    || easyLargeDrops.get(0).getCount() >= normalLargeDrops.get(0).getCount()) {
                helper.fail("easyMode should reduce Carrier Wo/large abyssal KaitaiHammer material yield");
                return;
            }

            ModItems.KAITAIHAMMER.get().onLeftClickEntity(hammer, ownerB, killShip);
            if (killShip.isAlive()) {
                helper.fail("KaitaiHammer left-click should perform one-hit kill recovery");
                return;
            }
            boolean hasRecoveredEgg = helper.getLevel()
                    .getEntitiesOfClass(ItemEntity.class, killShip.getBoundingBox().inflate(4.0D))
                    .stream()
                    .anyMatch(item -> LegacyShipSpawnEggItem.isRecoveredShipStack(item.getItem()));
            if (!hasRecoveredEgg) {
                helper.fail("KaitaiHammer left-click kill should drop a recovered ship spawn egg");
                return;
            }

            BlockPos waypointPos = helper.absolutePos(new BlockPos(3, 2, 3));
            helper.getLevel().setBlock(waypointPos, ModBlocks.BLOCK_WAYPOINT.get().defaultBlockState(), 3);
            if (helper.getLevel().getBlockEntity(waypointPos) instanceof WaypointBlockEntity waypoint) {
                waypoint.setOwner(ownerA);
            }
            ItemEntity radarItem = new ItemEntity(helper.getLevel(),
                    ownerA.getX() + 1.0D, ownerA.getY(), ownerA.getZ() + 1.0D,
                    new ItemStack(ModItems.GRUDGE.get(), 3));
            helper.getLevel().addFreshEntity(radarItem);

            CompoundTag seedTeamPayload = new CompoundTag();
            ListTag seedTeams = new ListTag();
            TeamData seedTeam = new TeamData(ownerAUid, ownerA.getGameProfile().getName(), "Radar Seed Fleet");
            CompoundTag seedTeamTag = new CompoundTag();
            seedTeam.saveToTag(seedTeamTag);
            seedTeams.add(seedTeamTag);
            seedTeamPayload.put("TeamList", seedTeams);
            TeitokuHelper.applyClientState(ownerA, seedTeamPayload);

            CompoundTag radarPayload = TeitokuHelper.buildDeskRadarPayloadTag(ownerA);
            TeitokuHelper.applyClientState(ownerA, radarPayload);
            List<TeitokuHelper.DeskRadarContactSnapshot> syncedContacts = TeitokuHelper.getClientDeskRadarContacts();
            boolean hasShip = syncedContacts.stream().anyMatch(c -> "SHIP".equals(c.kind()));
            boolean hasItem = syncedContacts.stream().anyMatch(c -> "ITEM".equals(c.kind()));
            boolean hasRoute = syncedContacts.stream().anyMatch(c -> "ROUTE".equals(c.kind()));
            if (!hasShip || !hasItem || !hasRoute || TeitokuHelper.getClientTeamData().isEmpty()) {
                helper.fail("Desk radar sync should populate ship, item, and facility contacts without clearing team cache");
                return;
            }
        } finally {
            if (addedToLevelPlayers) {
                levelPlayers.remove(ownerB);
            }
            ownerA.setShiftKeyDown(false);
            ownerB.setShiftKeyDown(false);
            ShinColleConfig.clearTestOverrides();
        }

        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void b6SlashShipCommandsParity(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ServerPlayer ownerA = makeServerBackedPlayer(helper, "B6CmdOwnerA");
        ServerPlayer ownerB = makeServerBackedPlayer(helper, "B6CmdOwnerB");
        List<ServerPlayer> levelPlayers = level.players();
        boolean addedA = !levelPlayers.contains(ownerA) && levelPlayers.add(ownerA);
        boolean addedB = !levelPlayers.contains(ownerB) && levelPlayers.add(ownerB);

        LegacyShipEntity kongou = null;
        LegacyShipEntity reloaded = null;
        LegacyShipEntity hostile = null;
        ItemEntity drop = null;
        try {
            int uidA = TeitokuSavedData.get(level).getOrCreateUid(ownerA);
            int uidB = TeitokuSavedData.get(level).getOrCreateUid(ownerB);
            if (!configureTeitoku(helper, ownerA, d -> {
                d.setPlayerUid(uidA);
                d.setPlayerName(ownerA.getGameProfile().getName());
            }) || !configureTeitoku(helper, ownerB, d -> {
                d.setPlayerUid(uidB);
                d.setPlayerName(ownerB.getGameProfile().getName());
            })) {
                return;
            }

            BlockPos origin = helper.absolutePos(new BlockPos(2, 2, 2));
            ownerA.setPos(origin.getX() + 0.5D, origin.getY(), origin.getZ() + 0.5D);
            ownerA.setYRot(0.0F);
            ownerA.setYHeadRot(0.0F);
            ownerA.yRotO = 0.0F;
            ownerA.setXRot(0.0F);
            ownerA.xRotO = 0.0F;

            kongou = createOwnedShip(helper, ownerA, 62, origin.getX() + 0.5D, origin.getY(), origin.getZ() + 4.5D);
            if (kongou == null || kongou.getShipUid() <= 0) {
                helper.fail("owned Kongou should spawn with a positive ShipUID");
                return;
            }
            int shipUid = kongou.getShipUid();
            ShipCacheSavedData.get(level).updateFromShip(kongou, false, true);

            Commands commands = level.getServer().getCommands();
            CommandSourceStack opSource = ownerA.createCommandSourceStack()
                    .withPermission(4)
                    .withRotation(new Vec2(0.0F, 0.0F));

            if (commands.performPrefixedCommand(opSource, "shipinfo") <= 0
                    || commands.performPrefixedCommand(opSource, "shipemotes lol") <= 0
                    || commands.performPrefixedCommand(opSource, "shipupdateowneruid " + ownerA.getGameProfile().getName()) <= 0) {
                helper.fail("/shipinfo, /shipemotes, and /shipupdateowneruid should succeed on aimed owned ship");
                return;
            }

            if (commands.performPrefixedCommand(opSource, "shipattrs 50 10 10 10 10 10 10") <= 0
                    || kongou.getShipLevel() != 50
                    || kongou.getModernizationDisplayCount() != 60) {
                helper.fail("/shipattrs should set level 50 and 10 modernization steps across all 6 stats");
                return;
            }

            CompoundTag savedTag = new CompoundTag();
            kongou.addAdditionalSaveData(savedTag);
            reloaded = ModEntityTypes.LEGACY_SHIP.get().create(level);
            if (reloaded == null) {
                helper.fail("failed to allocate reloaded ship for NBT clamp check");
                return;
            }
            reloaded.setVariantEggMeta(62);
            reloaded.readAdditionalSaveData(savedTag);
            if (reloaded.getModernizationDisplayCount() != 60) {
                helper.fail("modernization steps > 3 from /shipattrs should persist across NBT save/load");
                return;
            }
            reloaded.discard();
            reloaded = null;

            boolean initialStopAi = LegacyShipEntity.isStopAi();
            if (commands.performPrefixedCommand(opSource, "shipstopai") <= 0
                    || LegacyShipEntity.isStopAi() == initialStopAi
                    || commands.performPrefixedCommand(opSource, "shipstopai") <= 0
                    || LegacyShipEntity.isStopAi() != initialStopAi) {
                helper.fail("/shipstopai should toggle global AI freeze flag on and off");
                return;
            }

            if (commands.performPrefixedCommand(opSource, "shipchangeowner " + ownerB.getGameProfile().getName()) <= 0
                    || kongou.getOwnerUid() != uidB
                    || !ownerB.getUUID().equals(kongou.getOwnerUuid().orElse(null))) {
                helper.fail("/shipchangeowner should transfer both owner UUID and owner UID");
                return;
            }

            kongou.setPos(origin.getX() + 0.5D, origin.getY(), origin.getZ() + 10.5D);
            if (commands.performPrefixedCommand(opSource, "ship list") <= 0
                    || commands.performPrefixedCommand(opSource, "ship get " + shipUid) <= 0
                    || kongou.distanceToSqr(ownerA) > 4.0D
                    || commands.performPrefixedCommand(opSource, "ship del " + shipUid) <= 0
                    || ShipCacheSavedData.get(level).getShip(shipUid) != null) {
                helper.fail("/ship list|get|del should list cached ship, recall it to sender, and delete cache entry");
                return;
            }

            drop = new ItemEntity(level, origin.getX() + 1.5D, origin.getY() + 0.5D, origin.getZ() + 1.5D, new ItemStack(Items.APPLE));
            level.addFreshEntity(drop);
            if (commands.performPrefixedCommand(opSource, "shipcleardrop 16") <= 0 || drop.isAlive()) {
                helper.fail("/shipcleardrop should remove nearby dropped ItemEntities");
                return;
            }

            hostile = ModEntityTypes.LEGACY_SHIP.get().create(level);
            if (hostile == null) {
                helper.fail("failed to allocate hostile target for /shipkill test");
                return;
            }
            hostile.setVariantEggMeta(2);
            hostile.setPos(origin.getX() + 3.5D, origin.getY(), origin.getZ() + 3.5D);
            level.addFreshEntity(hostile);
            int cmdClassId = (hostile.isHostileVariant() ? 2000 : 0) + hostile.getShipClassId() + 2;
            if (commands.performPrefixedCommand(opSource, "shipkill " + cmdClassId + " 16") <= 0
                    || hostile.isAlive()
                    || !kongou.isAlive()) {
                helper.fail("/shipkill should remove matching hostile ships while sparing Kongou");
                return;
            }
        } finally {
            LegacyShipEntity.setStopAi(false);
            ShinColleConfig.clearTestOverrides();
            if (addedA) {
                levelPlayers.remove(ownerA);
            }
            if (addedB) {
                levelPlayers.remove(ownerB);
            }
            if (kongou != null) {
                kongou.discard();
            }
            if (reloaded != null) {
                reloaded.discard();
            }
            if (hostile != null) {
                hostile.discard();
            }
            if (drop != null) {
                drop.discard();
            }
        }

        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void b6DynamicLightBlocksSearchlightAndFlareDecay(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ServerPlayer owner = makeServerBackedPlayer(helper, "B6LightOwner");
        LegacyShipEntity ship = null;
        LegacyShipEntity ship2 = null;
        Zombie target = null;
        Zombie target2 = null;
        BlockPos waterPos = helper.absolutePos(new BlockPos(8, 3, 2));
        try {
            BlockPos airPos = helper.absolutePos(new BlockPos(2, 3, 2));
            clearLightRegion(level, airPos);
            clearLightRegion(level, waterPos);
            level.setBlock(airPos, Blocks.AIR.defaultBlockState(), 3);
            for (int dy = 0; dy <= 1; dy++) {
                for (int dx = -1; dx <= 1; dx++) {
                    for (int dz = -1; dz <= 1; dz++) {
                        level.setBlock(waterPos.offset(dx, dy, dz), Blocks.WATER.defaultBlockState(), 2);
                    }
                }
            }

            if (!LightBlockHelper.placeLightBlock(level, airPos)) {
                helper.fail("LightBlockHelper should place light air block in open air");
                return;
            }
            BlockPos placedAirPos = findPlacedLightPos(level, airPos);
            if (placedAirPos == null
                    || !level.getBlockState(placedAirPos).is(ModBlocks.BLOCK_LIGHT_AIR.get())
                    || level.getBlockState(placedAirPos).getLightEmission(level, placedAirPos) != 15
                    || !(level.getBlockEntity(placedAirPos) instanceof LightBlockEntity airBe)) {
                helper.fail("placed BLOCK_LIGHT_AIR should emit light 15 and carry a LightBlockEntity");
                return;
            }

            if (!LightBlockHelper.placeLightBlock(level, waterPos)) {
                helper.fail("LightBlockHelper should place light liquid block in water");
                return;
            }
            BlockPos placedWaterPos = findPlacedLightPos(level, waterPos);
            if (placedWaterPos == null
                    || !level.getBlockState(placedWaterPos).is(ModBlocks.BLOCK_LIGHT_LIQUID.get())
                    || !level.getFluidState(placedWaterPos).is(Fluids.WATER)
                    || !(level.getBlockEntity(placedWaterPos) instanceof LightBlockEntity waterBe)) {
                helper.fail("placed BLOCK_LIGHT_LIQUID should preserve water fluid state and carry a LightBlockEntity");
                return;
            }

            for (int tick = 0; tick < LightBlockEntity.DEFAULT_LIFE_TICKS; tick++) {
                LightBlockEntity.serverTick(level, placedAirPos, level.getBlockState(placedAirPos), airBe);
                LightBlockEntity.serverTick(level, placedWaterPos, level.getBlockState(placedWaterPos), waterBe);
            }
            if (!level.getBlockState(placedAirPos).isAir()
                    || !level.getBlockState(placedWaterPos).is(Blocks.WATER)) {
                helper.fail("expired light blocks should restore air and water respectively");
                return;
            }

            BlockPos shipAnchor = helper.absolutePos(new BlockPos(3, 3, 6));
            BlockPos targetAnchor = helper.absolutePos(new BlockPos(6, 3, 6));
            clearLightRegion(level, shipAnchor.above());
            clearLightRegion(level, targetAnchor.above());

            ship = createOwnedShip(helper, owner, 62, shipAnchor.getX() + 0.5D, shipAnchor.getY(), shipAnchor.getZ() + 0.5D);
            target = EntityType.ZOMBIE.create(level);
            if (ship == null || target == null) {
                helper.fail("ship and zombie target should spawn for flare config gate check");
                return;
            }
            target.setPos(targetAnchor.getX() + 0.5D, targetAnchor.getY(), targetAnchor.getZ() + 0.5D);
            level.addFreshEntity(target);
            ship.getShipInventory().setItem(0, new ItemStack(ModItems.EQUIPFLARE.get()));
            ship.setLightAmmo(200);

            ShinColleConfig.setTestOverride("canFlare", false);
            ShinColleConfig.setTestOverride("canSearchlight", false);
            if (!ship.performPlayerCompatAttack(target, LegacyShipAttackKind.LIGHT)) {
                helper.fail("flare-gated light attack should still execute compat attack");
                return;
            }
            if (findPlacedLightPos(level, targetAnchor.above()) != null) {
                helper.fail("canFlare=false should suppress dynamic light block placement during flare attack");
                return;
            }

            BlockPos ship2Anchor = helper.absolutePos(new BlockPos(3, 3, 10));
            BlockPos target2Anchor = helper.absolutePos(new BlockPos(6, 3, 10));
            clearLightRegion(level, ship2Anchor.above());
            clearLightRegion(level, target2Anchor.above());
            ship2 = createOwnedShip(helper, owner, 62, ship2Anchor.getX() + 0.5D, ship2Anchor.getY(), ship2Anchor.getZ() + 0.5D);
            target2 = EntityType.ZOMBIE.create(level);
            if (ship2 == null || target2 == null) {
                helper.fail("second ship and target should spawn for canFlare=true test");
                return;
            }
            target2.setPos(target2Anchor.getX() + 0.5D, target2Anchor.getY(), target2Anchor.getZ() + 0.5D);
            level.addFreshEntity(target2);
            ship2.getShipInventory().setItem(0, new ItemStack(ModItems.EQUIPFLARE.get()));
            ship2.setLightAmmo(200);

            ShinColleConfig.setTestOverride("canFlare", true);
            if (!ship2.performPlayerCompatAttack(target2, LegacyShipAttackKind.LIGHT)) {
                helper.fail("flare-enabled light attack should execute compat attack");
                return;
            }
            if (findPlacedLightPos(level, target2Anchor.above()) == null) {
                helper.fail("canFlare=true should place dynamic light block at illuminated target");
                return;
            }
        } finally {
            ShinColleConfig.clearTestOverrides();
            for (int dy = 0; dy <= 1; dy++) {
                for (int dx = -1; dx <= 1; dx++) {
                    for (int dz = -1; dz <= 1; dz++) {
                        level.setBlock(waterPos.offset(dx, dy, dz), Blocks.AIR.defaultBlockState(), 2);
                    }
                }
            }
            if (ship != null) {
                ship.discard();
            }
            if (ship2 != null) {
                ship2.discard();
            }
            if (target != null) {
                target.discard();
            }
            if (target2 != null) {
                target2.discard();
            }
        }

        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void b6SystemConfigPolyAsMnShipKillsAndRaidenLabel(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ServerPlayer owner = makeServerBackedPlayer(helper, "B6SysOwner");
        LegacyShipEntity ikazuchi = null;
        LegacyShipRensouhouEntity rensouhou = null;
        Cow cowA = null;
        Cow cowB = null;
        try {
            ShinColleConfig.setTestOverride("ringAbility", new int[] {1, 4, 25, 15, 10});
            ShinColleConfig.setTestOverride("easyMode", true);
            ShinColleConfig.setTestOverride("closeGuiDist", 16);
            ShinColleConfig.setTestOverride("radarUpdate", 80);
            ShinColleConfig.setTestOverride("polyAsMn", true);

            CompoundTag syncPayload = TeitokuHelper.buildGameplayStateTag(owner);
            CompoundTag sysTag = syncPayload.getCompound("SystemConfig");
            if (!syncPayload.contains("SystemConfig", Tag.TAG_COMPOUND)
                    || !sysTag.getBoolean("EasyMode")
                    || sysTag.getInt("CloseGuiDist") != 16
                    || sysTag.getInt("RadarUpdate") != 80
                    || !sysTag.getBoolean("PolyAsMn")
                    || sysTag.getIntArray("RingAbility").length < 5
                    || sysTag.getIntArray("RingAbility")[1] != 4) {
                helper.fail("TeitokuHelper gameplay sync must serialize SystemConfig fields");
                return;
            }
            TeitokuHelper.applyClientState(owner, syncPayload);

            BlockPos deskPos = helper.absolutePos(new BlockPos(2, 2, 2));
            level.setBlock(deskPos, ModBlocks.BLOCK_DESK.get().defaultBlockState(), 3);
            DeskTerminalMenu deskMenu = new DeskTerminalMenu(0, owner.getInventory(), deskPos, DeskReferenceMenu.BOOK_VARIANT);
            owner.setPos(deskPos.getX() + 2.5D, deskPos.getY(), deskPos.getZ() + 0.5D);
            if (!deskMenu.stillValid(owner)) {
                helper.fail("DeskTerminalMenu should stay valid within configured closeGuiDist=16");
                return;
            }
            owner.setPos(deskPos.getX() + 5.5D, deskPos.getY(), deskPos.getZ() + 0.5D);
            if (deskMenu.stillValid(owner)) {
                helper.fail("DeskTerminalMenu should invalidate beyond configured closeGuiDist=16");
                return;
            }

            JsonObject condJson = new JsonObject();
            condJson.addProperty("type", "shincolle:poly_as_mn");
            ICondition polyCondition = CraftingHelper.getCondition(condJson);

            ShinColleConfig.setTestOverride("polyAsMn", false);
            if (polyCondition.test(ICondition.IContext.EMPTY)
                    || LegacyItemMatchHelper.matchesTag(new ItemStack(ModItems.ABYSSMETAL1.get()), itemTag("forge", "ingots/manganese"))) {
                helper.fail("PolyAsMnCondition and matchesTag should be false when polyAsMn=false");
                return;
            }

            ShinColleConfig.setTestOverride("polyAsMn", true);
            if (!polyCondition.test(ICondition.IContext.EMPTY)
                    || !LegacyItemMatchHelper.matchesTag(new ItemStack(ModItems.ABYSSMETAL1.get()), itemTag("forge", "ingots/manganese"))
                    || !LegacyItemMatchHelper.matchesTag(new ItemStack(ModItems.ABYSSMETAL1.get()), itemTag("forge", "dusts/manganese"))
                    || new ItemStack(ModItems.ABYSSMETAL1.get()).is(itemTag("forge", "ingots/manganese"))) {
                helper.fail("polyAsMn=true should enable condition and runtime tag match without altering static tags");
                return;
            }

            ikazuchi = createOwnedShip(helper, owner, 55, deskPos.getX() + 1.5D, deskPos.getY() + 1.0D, deskPos.getZ() + 2.5D);
            cowA = EntityType.COW.create(level);
            cowB = EntityType.COW.create(level);
            if (ikazuchi == null || cowA == null || cowB == null) {
                helper.fail("Ikazuchi and dummy targets should spawn for ShipKills and Raiden tests");
                return;
            }
            level.addFreshEntity(cowA);
            level.addFreshEntity(cowB);
            ikazuchi.killedEntity(level, cowA);
            rensouhou = LegacyShipRensouhouEntity.create(level, ikazuchi, cowB);
            if (rensouhou == null) {
                helper.fail("Rensouhou summon should spawn to verify host ShipKills attribution");
                return;
            }
            level.addFreshEntity(rensouhou);
            rensouhou.killedEntity(level, cowB);

            ShipInventoryMenu shipMenu = new ShipInventoryMenu(11, owner.getInventory(), ikazuchi.getId(), ikazuchi.getShipUid());
            if (ikazuchi.getShipKills() != 2 || shipMenu.getShipKills() != 2) {
                helper.fail("direct ship kills and Rensouhou kills should both increment ShipKills and menu count");
                return;
            }

            ikazuchi.setLegacyRaiden(true);
            Component roleLabel = shipMenu.getRoleLabel();
            if (!(roleLabel.getContents() instanceof TranslatableContents tc)
                    || !"gui.shincolle.ship_inventory.role.raiden".equals(tc.getKey())) {
                helper.fail("Raiden ship should resolve gui.shincolle.ship_inventory.role.raiden role label");
                return;
            }
        } finally {
            ShinColleConfig.clearTestOverrides();
            TeitokuHelper.clearClientSystemConfigCache();
            if (ikazuchi != null) {
                ikazuchi.discard();
            }
            if (rensouhou != null) {
                rensouhou.discard();
            }
            if (cowA != null) {
                cowA.discard();
            }
            if (cowB != null) {
                cowB.discard();
            }
        }

        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void abyssalHimeBaseSpecsAreFriendlyTamedPickupAndAuraEnabled(GameTestHelper helper) {
        int[] himeEggMetas = {22, 23, 28, 29, 30, 31, 32, 33, 46, 51, 74};
        for (int eggMeta : himeEggMetas) {
            ShipEntitySpec spec = ShipEntitySpecs.getByEggMeta(eggMeta);
            if (spec.hostile()) {
                helper.fail("Abyssal Hime base spec eggMeta=" + eggMeta + " must have hostile == false in 1.12.2");
                return;
            }
        }

        for (ShipEntitySpec spec : ShipEntitySpecs.values()) {
            boolean expectedHostile = spec.eggMeta() > 2000;
            if (spec.hostile() != expectedHostile) {
                helper.fail("ShipEntitySpec hostility boundary mismatch for eggMeta=" + spec.eggMeta()
                        + ": expected=" + expectedHostile + ", actual=" + spec.hostile());
                return;
            }
        }

        ServerPlayer owner = makeServerBackedPlayer(helper, "HimeHatchOwner");
        ServerLevel level = helper.getLevel();
        BlockPos groundPos = helper.absolutePos(new BlockPos(4, 1, 4));
        helper.setBlock(new BlockPos(4, 1, 4), Blocks.STONE.defaultBlockState());

        ItemStack shipEgg23 = new ItemStack(ForgeRegistries.ITEMS.getValue(
                ResourceLocation.fromNamespaceAndPath(ShinColle.MOD_ID, "shipegg23")));
        owner.setItemInHand(InteractionHand.MAIN_HAND, shipEgg23);
        InteractionResult hatch23 = shipEgg23.useOn(new UseOnContext(
                owner, InteractionHand.MAIN_HAND,
                new BlockHitResult(Vec3.atCenterOf(groundPos), Direction.UP, groundPos, false)));
        if (!hatch23.consumesAction()) {
            helper.fail("shipegg23 should hatch on solid ground");
            return;
        }

        BlockPos largePos = helper.absolutePos(new BlockPos(7, 1, 4));
        helper.setBlock(new BlockPos(7, 1, 4), Blocks.STONE.defaultBlockState());
        ItemStack largeEgg = new ItemStack(ForgeRegistries.ITEMS.getValue(
                ResourceLocation.fromNamespaceAndPath(ShinColle.MOD_ID, "largeegg")));
        owner.setItemInHand(InteractionHand.MAIN_HAND, largeEgg);
        InteractionResult hatchLarge = largeEgg.useOn(new UseOnContext(
                owner, InteractionHand.MAIN_HAND,
                new BlockHitResult(Vec3.atCenterOf(largePos), Direction.UP, largePos, false)));
        if (!hatchLarge.consumesAction()) {
            helper.fail("largeegg should hatch on solid ground");
            return;
        }

        List<LegacyShipEntity> hatched = level.getEntitiesOfClass(
                LegacyShipEntity.class,
                new net.minecraft.world.phys.AABB(groundPos).inflate(16.0D),
                ship -> ship.isOwnedBy(owner));
        if (hatched.size() < 2 || hatched.stream().anyMatch(LegacyShipEntity::isHostileVariant)) {
            helper.fail("shipegg23 and largeegg should hatch tamed friendly ships owned by player");
            hatched.forEach(Entity::discard);
            return;
        }
        hatched.forEach(Entity::discard);

        if (!LegacyShipBehaviorCatalog.supportsItemPickup(ShipEntitySpecs.getByEggMeta(29))) {
            helper.fail("DestroyerHime (29) should allow item pickup");
            return;
        }

        LegacyShipEntity carrierHime = createOwnedShip(helper, owner, 22, 4.5D, 2.0D, 4.5D);
        LegacyShipEntity battleshipHime = createOwnedShip(helper, owner, 28, 5.5D, 2.0D, 4.5D);
        LegacyShipEntity airfieldAlly = createOwnedShip(helper, owner, 23, 6.5D, 2.0D, 4.5D);
        if (carrierHime == null || battleshipHime == null || airfieldAlly == null) {
            return;
        }
        carrierHime.setNoAi(true);
        battleshipHime.setNoAi(true);
        airfieldAlly.setNoAi(true);
        carrierHime.setMarried(true);
        carrierHime.setGrudge(500);
        carrierHime.setRingEffectEnabled(true);
        battleshipHime.setMarried(true);
        battleshipHime.setGrudge(500);
        battleshipHime.setRingEffectEnabled(true);

        carrierHime.tickCount = 128;
        carrierHime.tick();
        battleshipHime.tickCount = 128;
        battleshipHime.tick();
        if (!airfieldAlly.hasEffect(MobEffects.JUMP) || !airfieldAlly.hasEffect(MobEffects.DAMAGE_RESISTANCE)) {
            helper.fail("CarrierHime and BattleshipHime ring auras should apply Jump and Resistance to Hime allies");
            return;
        }

        carrierHime.discard();
        battleshipHime.discard();
        airfieldAlly.discard();
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void submarineHimeStealthAndShooterDistanceDodgeMatchLegacy(GameTestHelper helper) {
        ServerPlayer owner = makeServerBackedPlayer(helper, "SubmHimeStealth");
        for (int eggMeta : new int[]{46, 74}) {
            LegacyShipEntity sub = createOwnedShip(helper, owner, eggMeta, 4.5D, 2.0D, 4.5D);
            if (sub == null) {
                return;
            }
            sub.setNoAi(true);
            sub.setShipLevel(50);

            sub.setRingEffectEnabled(false);
            sub.setGrudge(100);
            sub.tickCount = 128;
            sub.tick();
            if (sub.hasEffect(MobEffects.INVISIBILITY)) {
                helper.fail("eggMeta=" + eggMeta + " must not gain stealth when ringEffect=false");
                sub.discard();
                return;
            }

            sub.setRingEffectEnabled(true);
            sub.setGrudge(0);
            sub.tickCount = 128;
            sub.tick();
            if (sub.hasEffect(MobEffects.INVISIBILITY)) {
                helper.fail("eggMeta=" + eggMeta + " must not gain stealth when grudge=0");
                sub.discard();
                return;
            }

            sub.setGrudge(100);
            sub.tickCount = 128;
            sub.tick();
            MobEffectInstance invis = sub.getEffect(MobEffects.INVISIBILITY);
            if (invis == null || invis.getDuration() != 80 + 50) {
                helper.fail("eggMeta=" + eggMeta + " should gain 80 + shipLevel invisibility when ringEffect && grudge>0");
                sub.discard();
                return;
            }
            sub.discard();
        }

        LegacyShipEntity submHime = createOwnedShip(helper, owner, 46, 4.5D, 2.0D, 4.5D);
        if (submHime == null) {
            return;
        }
        submHime.setNoAi(true);
        submHime.setShipLevel(50);

        Zombie farShooter = EntityType.ZOMBIE.create(helper.getLevel());
        if (farShooter == null) {
            helper.fail("far shooter should be creatable");
            submHime.discard();
            return;
        }
        BlockPos origin = submHime.blockPosition();
        farShooter.setPos(origin.getX() + 70.0D, origin.getY(), origin.getZ());
        helper.getLevel().addFreshEntity(farShooter);

        Arrow pointBlankArrow = new Arrow(helper.getLevel(), farShooter);
        pointBlankArrow.setPos(submHime.getX() + 0.5D, submHime.getY() + 0.5D, submHime.getZ());
        helper.getLevel().addFreshEntity(pointBlankArrow);

        float shooterDodge = LegacyShipCombatHelper.calculateDodgeChance(submHime, farShooter);
        float arrowDodge = LegacyShipCombatHelper.calculateDodgeChance(submHime, pointBlankArrow);
        float baseDodge = submHime.getLegacyStats().dodge();
        float expectedInvisible = LegacyShipBehaviorCatalog.invisibleLevelFor(submHime.getSpec());
        if (expectedInvisible <= 0.0F
                || Math.abs(shooterDodge - (baseDodge + expectedInvisible)) > 1.0E-5F
                || Math.abs(arrowDodge - baseDodge) > 1.0E-5F) {
            helper.fail("submarine invisibleLevel dodge should use shooter distance rather than point-blank projectile distance");
            farShooter.discard();
            pointBlankArrow.discard();
            submHime.discard();
            return;
        }

        farShooter.discard();
        pointBlankArrow.discard();
        submHime.discard();
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void baseShipLifecycleRepairMoraleRegenAndFleeGoalMatchLegacy(GameTestHelper helper) {
        ServerPlayer owner = makeServerBackedPlayer(helper, "LifecycleOwner");
        helper.getLevel().players().add(owner);
        BlockPos carrierPos = helper.absolutePos(new BlockPos(4, 2, 4));
        LegacyShipEntity carrier = createOwnedShip(helper, owner, 35,
                carrierPos.getX() + 0.5D, carrierPos.getY(), carrierPos.getZ() + 0.5D);
        if (carrier == null) {
            return;
        }
        carrier.setNoAi(true);
        carrier.setNoGravity(true);
        carrier.setAiAutoSupply(false);
        carrier.setGrudge(1000);
        carrier.setAircraftLight(0);
        carrier.setAircraftHeavy(0);
        carrier.getShipInventory().setItem(LegacyShipEntity.EQUIPMENT_SLOT_COUNT,
                new ItemStack(ModItems.BUCKETREPAIR.get()));
        float maxHp = carrier.getMaxHealth();
        carrier.setHealth(Math.max(1.0F, maxHp * 0.5F));
        float hpBeforeBucket = carrier.getHealth();

        carrier.tickCount = 32;
        carrier.tick();
        if (carrier.getHealth() <= hpBeforeBucket
                || carrier.getAircraftLight() < 1
                || carrier.getAircraftHeavy() < 1
                || !carrier.getShipInventory().getItem(LegacyShipEntity.EQUIPMENT_SLOT_COUNT).isEmpty()) {
            helper.fail("32-tick auto-bucket repair should consume bucket, heal HP, and restore +1 carrier aircraft: hp="
                    + carrier.getHealth() + "/" + hpBeforeBucket
                    + ", light=" + carrier.getAircraftLight()
                    + ", heavy=" + carrier.getAircraftHeavy()
                    + ", bucketEmpty=" + carrier.getShipInventory().getItem(LegacyShipEntity.EQUIPMENT_SLOT_COUNT).isEmpty());
            carrier.discard();
            return;
        }

        carrier.setInvulnerable(true);
        carrier.setMorale(2500);
        carrier.tickCount = 384;
        carrier.tick();
        if (carrier.getMorale() != 2535) {
            helper.fail("128-tick updateMorale should recover +40 below 3100 (expected 2535, got " + carrier.getMorale() + ")");
            carrier.discard();
            return;
        }

        carrier.setMorale(4000);
        carrier.tickCount = 384;
        carrier.tick();
        carrier.setInvulnerable(false);
        if (carrier.getMorale() != 3985) {
            helper.fail("128-tick updateMorale should decay -10 above 3900 (got " + carrier.getMorale() + ")");
            carrier.discard();
            return;
        }

        carrier.setHealth(Math.max(1.0F, maxHp * 0.5F));
        float hpBeforeRegen = carrier.getHealth();
        carrier.tickCount = 256;
        carrier.tick();
        float expectedRegen = Math.min(maxHp,
                hpBeforeRegen + (maxHp * 0.03F + 1.0F) * carrier.getLegacyStats().healRate());
        if (Math.abs(carrier.getHealth() - expectedRegen) > 0.05F) {
            helper.fail("256-tick passive regen should heal 0.03 * maxHP + 1: hp="
                    + carrier.getHealth() + ", expected=" + expectedRegen + ", before=" + hpBeforeRegen);
            carrier.discard();
            return;
        }

        owner.setPos(carrier.getX() + 10.0D, carrier.getY(), carrier.getZ());
        carrier.setFleeHp(50);
        carrier.setGrudge(100);
        carrier.setHealth(Math.max(1.0F, maxHp * 0.4F));
        Zombie target = EntityType.ZOMBIE.create(helper.getLevel());
        if (target == null) {
            helper.fail("dummy target should be creatable");
            carrier.discard();
            return;
        }
        target.setPos(carrier.getX() - 3.0D, carrier.getY(), carrier.getZ());
        helper.getLevel().addFreshEntity(target);
        carrier.setTarget(target);

        LegacyShipFleeGoal fleeGoal = new LegacyShipFleeGoal(carrier);
        if (!fleeGoal.canUse()) {
            helper.fail("LegacyShipFleeGoal should activate when HP <= fleeHp% and owner is within flee range");
            target.discard();
            carrier.discard();
            return;
        }
        fleeGoal.start();
        if (carrier.getTarget() != null) {
            helper.fail("LegacyShipFleeGoal.start should clear combat target");
            target.discard();
            carrier.discard();
            return;
        }
        carrier.setHealth(maxHp * 0.8F);
        if (fleeGoal.canUse()) {
            helper.fail("LegacyShipFleeGoal should not activate when HP > fleeHp%");
            target.discard();
            carrier.discard();
            return;
        }

        target.discard();
        carrier.discard();
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void shipSpecificHooksMountOrientationAndModelCatalogsMatchLegacy(GameTestHelper helper) {
        ServerPlayer owner = makeServerBackedPlayer(helper, "SpecificHooksOwner");
        ServerLevel level = helper.getLevel();

        if (Math.abs(LegacyShipBehaviorCatalog.heavyDamageMultiplierFor(31, false) - 0.75F) > 1.0E-6F
                || !LegacyShipBehaviorCatalog.dismountsBeforeDamage(ShipEntitySpecs.getByEggMeta(33))
                || !LegacyShipBehaviorCatalog.dismountsBeforeDamage(ShipEntitySpecs.getByEggMeta(74))) {
            helper.fail("NorthernHime should keep 0.75F heavy multiplier and class 31/72 sneak-dismount rules");
            return;
        }

        LegacyShipEntity battleshipRe = createOwnedShip(helper, owner, 17, 3.5D, 2.0D, 3.5D);
        LegacyShipEntity transportWa = createOwnedShip(helper, owner, 18, 5.5D, 2.0D, 3.5D);
        if (battleshipRe == null || transportWa == null) {
            return;
        }
        double reHeight = battleshipRe.getBbHeight();
        double waHeight = transportWa.getBbHeight();
        battleshipRe.setOrderedToSit(false);
        transportWa.setOrderedToSit(false);
        if (Math.abs(battleshipRe.getPassengersRidingOffset() - reHeight * 0.55D) > 1.0E-5D
                || Math.abs(transportWa.getPassengersRidingOffset() - waHeight * 0.64D) > 1.0E-5D) {
            helper.fail("BattleshipRe and TransportWa standing riding offsets should match 1.12");
            battleshipRe.discard();
            transportWa.discard();
            return;
        }
        battleshipRe.setOrderedToSit(true);
        battleshipRe.setEmotion(4);
        transportWa.setOrderedToSit(true);
        transportWa.setModelState(2);
        if (Math.abs(battleshipRe.getPassengersRidingOffset() - reHeight * 0.35D) > 1.0E-5D
                || Math.abs(transportWa.getPassengersRidingOffset() - waHeight * 0.5D) > 1.0E-5D) {
            helper.fail("BattleshipRe and TransportWa sitting riding offsets should match 1.12");
            battleshipRe.discard();
            transportWa.discard();
            return;
        }
        battleshipRe.discard();
        transportWa.discard();

        LegacyShipEntity submYo = createOwnedShip(helper, owner, 20, 4.5D, 2.0D, 4.5D);
        if (submYo == null) {
            return;
        }
        submYo.getShipInventory().setItem(0, new ItemStack(ModItems.EQUIPMACHINEGUN_ITEMS.get(0).get()));
        submYo.setNoAi(true);
        submYo.tickCount = 128;
        submYo.tick();
        var phantom = EntityType.PHANTOM.create(level);
        if (phantom == null) {
            helper.fail("phantom target should be creatable");
            submYo.discard();
            return;
        }
        phantom.setPos(submYo.getX() + 2.0D, submYo.getY() + 1.0D, submYo.getZ());
        level.addFreshEntity(phantom);
        float rawLight = submYo.getLegacyStats().attackLight();
        float aaBonus = submYo.getLegacyStats().antiAir();
        LegacyShipCombatHelper.AttackRoll roll = LegacyShipCombatHelper.rollAttack(
                submYo, phantom, LegacyShipAttackKind.LIGHT);
        for (int attempt = 0; attempt < 20 && roll.miss(); attempt++) {
            roll = LegacyShipCombatHelper.rollAttack(submYo, phantom, LegacyShipAttackKind.LIGHT);
        }
        float mult = roll.tripleHit() ? 3.0F : (roll.doubleHit() ? 2.0F : (roll.crit() ? 1.5F : 1.0F));
        if (aaBonus <= 0.0F || roll.miss() || Math.abs(roll.damage() - rawLight * mult) > 1.0E-3F) {
            helper.fail("SubmYo light attack should use raw attackLight without AA/ASM bonus: aaBonus="
                    + aaBonus + ", miss=" + roll.miss() + ", damage=" + roll.damage()
                    + ", expected=" + (rawLight * mult));
            phantom.discard();
            submYo.discard();
            return;
        }
        phantom.discard();
        submYo.discard();

        LegacyShipEntity friendlyTatsuta = createOwnedShip(helper, owner, 59, 4.5D, 2.0D, 4.5D);
        LegacyShipEntity hostileTatsuta = ModEntityTypes.LEGACY_SHIP.get().create(level);
        Zombie mobA = EntityType.ZOMBIE.create(level);
        Zombie mobB = EntityType.ZOMBIE.create(level);
        if (friendlyTatsuta == null || hostileTatsuta == null || mobA == null || mobB == null) {
            helper.fail("Tatsuta knockback test entities should be creatable");
            return;
        }
        mobA.setPos(friendlyTatsuta.getX() + 1.0D, friendlyTatsuta.getY(), friendlyTatsuta.getZ());
        var mobAMaxHp = mobA.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.MAX_HEALTH);
        if (mobAMaxHp != null) {
            mobAMaxHp.setBaseValue(500.0D);
        }
        mobA.setHealth(500.0F);
        level.addFreshEntity(mobA);
        friendlyTatsuta.setNoAi(true);
        friendlyTatsuta.setShipLevel(100);
        friendlyTatsuta.setHeavyAmmo(1000);
        friendlyTatsuta.setGrudge(1000);
        Vec3 friendlyKb = Vec3.ZERO;
        for (int attempt = 0; attempt < 15 && friendlyKb.equals(Vec3.ZERO); attempt++) {
            friendlyTatsuta.resetLegacySkillState();
            friendlyTatsuta.setTarget(mobA);
            friendlyTatsuta.setDisplayPhase(2);
            friendlyTatsuta.setLegacySkillRemaining(2);
            friendlyTatsuta.setLegacySkillTimer(8);
            friendlyTatsuta.tick();
            friendlyKb = mobA.getDeltaMovement();
        }
        if (Math.abs(friendlyKb.z) > 1.0E-5D || friendlyKb.y <= 0.4D) {
            helper.fail("friendly Tatsuta wave should apply vertical-only knockback to mobs: kb=" + friendlyKb);
            mobA.discard();
            friendlyTatsuta.discard();
            return;
        }
        mobA.discard();
        friendlyTatsuta.discard();

        mobB.discard();
        LegacyShipEntity friendlyTargetShip = ModEntityTypes.LEGACY_SHIP.get().create(level);
        if (friendlyTargetShip == null) {
            return;
        }
        BlockPos hostilePos = helper.absolutePos(new BlockPos(4, 2, 4));
        friendlyTargetShip.setVariantEggMeta(62);
        friendlyTargetShip.setOwner(owner);
        friendlyTargetShip.setNoGravity(true);
        friendlyTargetShip.setPos(hostilePos.getX() + 1.5D, hostilePos.getY(), hostilePos.getZ() + 0.5D);
        level.addFreshEntity(friendlyTargetShip);
        var targetShipMaxHp = friendlyTargetShip.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.MAX_HEALTH);
        if (targetShipMaxHp != null) {
            targetShipMaxHp.setBaseValue(5000.0D);
        }
        friendlyTargetShip.setHealth(5000.0F);
        friendlyTargetShip.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 200, 3));

        hostileTatsuta.setVariantEggMeta(2059);
        hostileTatsuta.initializeHostileRuntime(false, false, false);
        hostileTatsuta.setShipLevel(1);
        hostileTatsuta.setNoAi(true);
        hostileTatsuta.setNoGravity(true);
        hostileTatsuta.setPos(hostilePos.getX() + 0.5D, hostilePos.getY(), hostilePos.getZ() + 0.5D);
        hostileTatsuta.setYRot(90.0F);
        level.addFreshEntity(hostileTatsuta);
        Vec3 hostileKb = Vec3.ZERO;
        for (int attempt = 0; attempt < 15 && Math.abs(hostileKb.x) <= 1.0E-4D; attempt++) {
            friendlyTargetShip.setHealth(5000.0F);
            hostileTatsuta.resetLegacySkillState();
            hostileTatsuta.setTarget(friendlyTargetShip);
            hostileTatsuta.setDisplayPhase(2);
            hostileTatsuta.setLegacySkillRemaining(2);
            hostileTatsuta.setLegacySkillTimer(8);
            hostileTatsuta.tick();
            hostileKb = friendlyTargetShip.getDeltaMovement();
        }
        if (Math.abs(hostileKb.x) <= 0.01D || hostileKb.y <= 0.15D) {
            helper.fail("hostile Tatsuta wave should apply horizontal + vertical knockback to mobs: kb=" + hostileKb
                    + ", pushable=" + friendlyTargetShip.isPushable()
                    + ", alive=" + friendlyTargetShip.isAlive()
                    + ", hp=" + friendlyTargetShip.getHealth() + "/" + friendlyTargetShip.getMaxHealth()
                    + ", climb=" + friendlyTargetShip.onClimbable());
            friendlyTargetShip.discard();
            hostileTatsuta.discard();
            return;
        }
        friendlyTargetShip.discard();
        hostileTatsuta.discard();

        LegacyShipEntity mountHost = createOwnedShip(helper, owner, 23, 8.5D, 2.0D, 8.5D);
        if (mountHost == null) {
            return;
        }
        mountHost.setModelState(1);
        mountHost.setGrudge(1000);
        mountHost.tickCount = 128;
        LegacyShipBehaviorCatalog.tickLegacyShipMountBehavior(mountHost);
        if (!(mountHost.getVehicle() instanceof LegacyShipMountEntity mount)) {
            helper.fail("LegacyShipMountEntity should be creatable");
            mountHost.discard();
            return;
        }
        mount.setYRot(90.0F);
        mountHost.rideTick();
        double hostX90 = mountHost.getX();
        double hostZ90 = mountHost.getZ();
        mount.setYRot(0.0F);
        mountHost.rideTick();
        double hostX0 = mountHost.getX();
        double hostZ0 = mountHost.getZ();
        if (Math.abs(hostX90 - hostX0) < 1.0E-3D && Math.abs(hostZ90 - hostZ0) < 1.0E-3D) {
            helper.fail("LegacyShipMountEntity dual-seat positionRider should rotate X/Z offsets with mount yaw: host90=("
                    + hostX90 + "," + hostZ90 + "), host0=(" + hostX0 + "," + hostZ0 + ")");
            mountHost.discard();
            mount.discard();
            return;
        }
        mountHost.discard();
        mount.discard();

        String[] scaleModels = {
                "ModelSubmU511", "ModelSubmRo500", "ModelBattleshipYamato", "ModelBattleshipNagato",
                "ModelBBKongou", "ModelBBHiei", "ModelBBHaruna", "ModelBBKirishima",
                "ModelCarrierKaga", "ModelCarrierAkagi", "ModelCruiserAtago", "ModelCruiserTakao",
                "ModelCruiserTenryuu", "ModelCruiserTatsuta", "ModelDestroyerShimakaze",
                "ModelDestroyerAkatsuki", "ModelDestroyerHibiki", "ModelDestroyerIkazuchi", "ModelDestroyerInazuma"
        };
        for (String stem : scaleModels) {
            for (int scaleLevel = 0; scaleLevel <= 3; scaleLevel++) {
                if (LegacyShipModelTransformCatalog.forModel(stem, scaleLevel).isEmpty()) {
                    helper.fail("LegacyShipModelTransformCatalog missing transform for " + stem + " scaleLevel=" + scaleLevel);
                    return;
                }
            }
        }

        if (LegacyShipModelStateCatalog.isPartVisible("ModelCarrierHime", "GlowBodyMain2", 0)
                || !LegacyShipModelStateCatalog.isPartVisible("ModelCarrierHime", "GlowBodyMain2", 2)
                || LegacyShipModelStateCatalog.isPartVisible("ModelCarrierHime", "GlowArmLeft01", 0)
                || LegacyShipModelStateCatalog.isPartVisible("ModelSubmHime", "EquipTBase", 15)
                || LegacyShipModelStateCatalog.isPartVisible("ModelSubmHime", "Collar01", 0)
                || !LegacyShipModelStateCatalog.isPartVisible("ModelSubmHime", "GlowEquipBase", 4)
                || LegacyShipModelStateCatalog.isPartVisible("ModelDestroyerI", "PKisaragi00", 0)
                || !LegacyShipModelStateCatalog.isPartVisible("ModelDestroyerI", "PKisaragi00", 1)
                || LegacyShipModelStateCatalog.isPartVisible("ModelDestroyerRo", "k00", 0)
                || LegacyShipModelStateCatalog.isPartVisible("ModelSubmU511", "Hat01", 0)
                || !LegacyShipModelStateCatalog.isPartVisible("ModelBattleshipHime", "AnyPart", 0)) {
            helper.fail("LegacyShipModelStateCatalog should enforce ModelCarrierHime, ModelSubmHime, and Abyssal base model rules");
            return;
        }

        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void round2HurtImmunitiesVoidRescueGoddessAndResistanceMatchLegacy(GameTestHelper helper) {
        ServerPlayer owner = makeServerBackedPlayer(helper, "Round2HurtOwner");
        helper.getLevel().players().add(owner);
        ServerLevel level = helper.getLevel();
        LegacyShipEntity ship = createOwnedShip(helper, owner, 62, 4.5D, 2.0D, 4.5D);
        if (ship == null) {
            return;
        }
        ship.setNoAi(true);
        float maxHp = ship.getMaxHealth();

        if (ship.hurt(level.damageSources().fall(), 10.0F)
                || ship.hurt(level.damageSources().inWall(), 10.0F)
                || ship.hurt(level.damageSources().cactus(), 10.0F)
                || ship.hurt(level.damageSources().starve(), 10.0F)) {
            helper.fail("ships should be immune to FALL, IN_WALL, CACTUS, and STARVE damage");
            ship.discard();
            return;
        }

        if (ship.hurt(level.damageSources().fellOutOfWorld(), 50.0F)
                || Math.abs(ship.getY() - 4.0D) > 1.0E-4D
                || Math.abs(ship.getDeltaMovement().y - 1.0D) > 1.0E-4D) {
            helper.fail("friendly ships should rescue from FELL_OUT_OF_WORLD to Y=4 with motionY=1");
            ship.discard();
            return;
        }

        float tinyMagic = Math.max(0.01F, maxHp * 0.005F);
        if (ship.hurt(level.damageSources().magic(), tinyMagic)) {
            helper.fail("MAGIC damage below 1% maxHP should be ignored");
            ship.discard();
            return;
        }

        ship.setHealth(10.0F);
        ship.getShipInventory().setItem(LegacyShipEntity.EQUIPMENT_SLOT_COUNT,
                new ItemStack(ModItems.REPAIRGODDESS.get()));
        ship.hurt(level.damageSources().generic(), 1000.0F);
        if (ship.getHealth() < maxHp - 1.0F
                || !ship.getShipInventory().getItem(LegacyShipEntity.EQUIPMENT_SLOT_COUNT).isEmpty()
                || ship.hurt(level.damageSources().generic(), 10.0F)) {
            helper.fail("Repair Goddess should consume item, restore HP, and grant 120t immunity");
            ship.discard();
            return;
        }

        for (int i = 0; i < 121; i++) {
            ship.tick();
        }
        ship.setHealth(maxHp);
        ship.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 200, 1));
        Arrow arrow = EntityType.ARROW.create(level);
        if (arrow != null) {
            arrow.setPos(ship.getX(), ship.getY(), ship.getZ());
            level.addFreshEntity(arrow);
            float hpBefore = ship.getHealth();
            ship.hurt(level.damageSources().arrow(arrow, arrow), 20.0F);
            float taken = hpBefore - ship.getHealth();
            arrow.discard();
            if (taken > 13.0F) {
                helper.fail("DAMAGE_RESISTANCE lv2 should mitigate projectile damage by 40% (taken=" + taken + ")");
                ship.discard();
                return;
            }
        }

        if (ShinColleConfig.dmgSvS() <= 0) {
            helper.fail("ShinColleConfig.dmgSvS() should be positive");
            ship.discard();
            return;
        }

        ship.discard();
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void round2SlotFormationBuffsMinMovAndEnchantScalingMatchLegacy(GameTestHelper helper) {
        ServerPlayer owner = makeServerBackedPlayer(helper, "Round2FormOwner");
        helper.getLevel().players().add(owner);

        float[] lineSlot0 = LegacyShipStatTables.copyFormationStats(1, 0);
        float[] lineSlot5 = LegacyShipStatTables.copyFormationStats(1, 5);
        if (Math.abs(lineSlot0[LegacyShipStatTables.Attr.ATK_L] - lineSlot5[LegacyShipStatTables.Attr.ATK_L]) < 1.0E-4F) {
            helper.fail("Line Ahead slot 0 and slot 5 should have distinct formation buffs");
            return;
        }

        LegacyShipEntity[] fleet = new LegacyShipEntity[5];
        int[] eggMetas = {62, 2, 2, 2, 2};
        for (int i = 0; i < 5; i++) {
            fleet[i] = createOwnedShip(helper, owner, eggMetas[i], 2.5D + i, 2.0D, 2.5D);
            if (fleet[i] == null) {
                return;
            }
            fleet[i].setNoAi(true);
            fleet[i].tick();
            TeitokuHelper.assignShipToCurrentTeam(owner, i, fleet[i].getShipUid());
        }
        TeitokuHelper.cycleFormationId(owner);
        for (LegacyShipEntity s : fleet) {
            s.tickCount = 20;
            s.tick();
        }
        double expectedMinMov = Math.min(fleet[0].getLegacyStats().moveSpeed(), fleet[1].getLegacyStats().moveSpeed())
                + fleet[0].getLegacyStats().formationStat(LegacyShipStatTables.Attr.MOV)
                * LegacyShipStatTables.SCALE_SHIP[LegacyShipStatTables.BaseAttr.MOV];
        double actualSpeed = fleet[0].getAttributeBaseValue(Attributes.MOVEMENT_SPEED);
        if (Math.abs(actualSpeed - expectedMinMov) > 1.0E-4D) {
            helper.fail("formation movement speed should sync to MinMOV + formation[MOV]: expected="
                    + expectedMinMov + ", actual=" + actualSpeed);
            for (LegacyShipEntity s : fleet) {
                s.discard();
            }
            return;
        }

        ItemStack plainCannon = new ItemStack(ModItems.EQUIPCANNON_ITEMS.get(0).get());
        ItemStack enchantedCannon = plainCannon.copy();
        enchantedCannon.enchant(net.minecraft.world.item.enchantment.Enchantments.SHARPNESS, 3);
        fleet[0].getShipInventory().setItem(0, plainCannon);
        float plainAtk = ShipEquipmentProfile.fromInventory(fleet[0].getShipInventory(), fleet[0].getSpec())
                .stat(LegacyShipStatTables.Attr.ATK_L);
        fleet[0].getShipInventory().setItem(0, enchantedCannon);
        float enchAtk = ShipEquipmentProfile.fromInventory(fleet[0].getShipInventory(), fleet[0].getSpec())
                .stat(LegacyShipStatTables.Attr.ATK_L);
        if (enchAtk <= plainAtk) {
            helper.fail("equipment enchantments should scale equipment stats: plain=" + plainAtk + ", ench=" + enchAtk);
            for (LegacyShipEntity s : fleet) {
                s.discard();
            }
            return;
        }

        for (LegacyShipEntity s : fleet) {
            s.discard();
        }
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void round2FishingCraneAndPointerTargetingMatchLegacy(GameTestHelper helper) {
        ServerPlayer owner = makeServerBackedPlayer(helper, "Round2UtilityOwner");
        helper.getLevel().players().add(owner);
        ServerLevel level = helper.getLevel();

        BlockPos guardRel = new BlockPos(4, 1, 4);
        BlockPos guardAbs = helper.absolutePos(guardRel);
        helper.setBlock(guardRel, Blocks.STONE.defaultBlockState());
        helper.setBlock(new BlockPos(4, 1, 5), Blocks.WATER.defaultBlockState());

        LegacyShipEntity fisher = createOwnedShip(helper, owner, 2, 4.5D, 2.0D, 4.5D);
        if (fisher == null) {
            return;
        }
        fisher.setNoAi(true);
        fisher.setShipLevel(50);
        fisher.setGrudge(500);
        fisher.commandGuardBlock(guardAbs, level.dimension().location().toString());
        if (!guardAbs.equals(fisher.getGuardedBlockPos())) {
            helper.fail("fishing task should allow guarding non-waypoint blocks");
            fisher.discard();
            return;
        }
        fisher.addEffect(new MobEffectInstance(MobEffects.LUCK, 200, 2));
        fisher.tickCount = 8;
        LegacyShipBehaviorCatalog.tickLegacyTask(fisher);
        fisher.discard();

        BlockPos craneRel = new BlockPos(6, 1, 4);
        helper.setBlock(craneRel, ModBlocks.BLOCK_CRANE.get().defaultBlockState());
        if (helper.getBlockEntity(craneRel) instanceof CraneBlockEntity crane) {
            if (!crane.isCraneEnabled()) {
                helper.fail("crane should default to enabled");
                return;
            }
            int mode0 = crane.getWaitMode();
            crane.cycleWaitMode();
            if (crane.getWaitMode() == mode0) {
                helper.fail("crane cycleWaitMode should advance WaitMode");
                return;
            }
        } else {
            helper.fail("CraneBlockEntity should exist");
            return;
        }

        LegacyShipEntity otherShip = ModEntityTypes.LEGACY_SHIP.get().create(level);
        if (otherShip == null) {
            helper.fail("otherShip should be creatable");
            return;
        }
        otherShip.setVariantEggMeta(2);
        otherShip.setOwner(UUID.randomUUID(), "OtherAdmiral");
        otherShip.setPos(guardAbs.getX() + 0.5D, guardAbs.getY() + 1.0D, guardAbs.getZ() + 0.5D);
        level.addFreshEntity(otherShip);

        ItemStack pointer = new ItemStack(ModItems.POINTERITEM.get());
        owner.setItemInHand(InteractionHand.MAIN_HAND, pointer);
        if (!ShinColleConfig.friendlyFire()) {
            pointer.interactLivingEntity(owner, otherShip, InteractionHand.MAIN_HAND);
        }

        BlockPos farWaterRel = new BlockPos(4, 1, 4);
        helper.setBlock(farWaterRel, Blocks.WATER.defaultBlockState());
        owner.setPos(guardAbs.getX() + 0.5D, guardAbs.getY() + 2.0D, guardAbs.getZ() - 10.0D);
        owner.setXRot(10.0F);
        owner.setYRot(0.0F);
        pointer.use(level, owner, InteractionHand.MAIN_HAND);

        otherShip.discard();
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void round2AircraftCooldownShootableTorpedoAndRensouhouMatchLegacy(GameTestHelper helper) {
        ServerPlayer owner = makeServerBackedPlayer(helper, "Round2ProjOwner");
        helper.getLevel().players().add(owner);
        ServerLevel level = helper.getLevel();

        LegacyShipEntity carrier = createOwnedShip(helper, owner, 50, 4.5D, 2.0D, 4.5D);
        LegacyShipEntity enemy = ModEntityTypes.LEGACY_SHIP.get().create(level);
        if (carrier == null || enemy == null) {
            helper.fail("carrier and enemy should be creatable");
            return;
        }
        enemy.setVariantEggMeta(2038);
        enemy.initializeHostileRuntime(false, false, false);
        enemy.setPos(carrier.getX() + 4.0D, carrier.getY(), carrier.getZ());
        level.addFreshEntity(enemy);

        LegacyShipAircraftEntity aircraft = LegacyShipAircraftEntity.create(
                level, carrier, enemy, LegacyShipAttackKind.AIR_LIGHT);
        if (aircraft == null || !aircraft.isPickable() || aircraft.getAircraftMaxHealth() <= 0.0F) {
            helper.fail("LegacyShipAircraftEntity should be shootable with positive maxHealth");
            carrier.discard();
            enemy.discard();
            return;
        }
        level.addFreshEntity(aircraft);
        float acHpBefore = aircraft.getAircraftHealth();
        for (int attempt = 0; attempt < 20 && aircraft.isAlive(); attempt++) {
            aircraft.hurt(level.damageSources().mobAttack(enemy), acHpBefore + 100.0F);
        }
        if (aircraft.getAircraftHealth() >= acHpBefore && aircraft.isAlive()) {
            helper.fail("LegacyShipAircraftEntity should take fatal generic damage when shootable");
            aircraft.discard();
            carrier.discard();
            enemy.discard();
            return;
        }
        aircraft.discard();

        BlockPos waterRel = new BlockPos(4, 1, 6);
        helper.setBlock(waterRel, Blocks.WATER.defaultBlockState());
        BlockPos waterAbs = helper.absolutePos(waterRel);
        LegacyShipProjectileEntity torpedo = LegacyShipProjectileEntity.create(
                level, enemy, carrier, LegacyShipAttackKind.HEAVY, 10.0F, false);
        if (torpedo == null) {
            helper.fail("torpedo projectile should be creatable");
            carrier.discard();
            enemy.discard();
            return;
        }
        torpedo.setPos(waterAbs.getX() + 0.5D, waterAbs.getY() + 0.5D, waterAbs.getZ() + 0.5D);
        level.addFreshEntity(torpedo);
        for (int t = 0; t < 6; t++) {
            torpedo.tick();
        }
        torpedo.discard();

        LegacyShipEntity battleship = createOwnedShip(helper, owner, 62, 4.5D, 2.0D, 4.5D);
        if (battleship != null) {
            LegacyShipProjectileEntity blackHoleMissile = LegacyShipProjectileEntity.create(
                    level, battleship, enemy, LegacyShipAttackKind.HEAVY, 10.0F, true);
            if (blackHoleMissile != null) {
                blackHoleMissile.setPos(waterAbs.getX() + 0.5D, waterAbs.getY() + 2.0D, waterAbs.getZ() + 0.5D);
                level.addFreshEntity(blackHoleMissile);
                blackHoleMissile.discard();
            }
            battleship.discard();
        }

        LegacyShipRensouhouEntity hostileRensouhou = LegacyShipRensouhouEntity.create(level, enemy, carrier);
        if (hostileRensouhou == null) {
            helper.fail("hostile Rensouhou should be creatable");
            carrier.discard();
            enemy.discard();
            return;
        }
        level.addFreshEntity(hostileRensouhou);
        hostileRensouhou.hurt(level.damageSources().generic(), 5.0F);
        hostileRensouhou.discard();

        carrier.discard();
        enemy.discard();
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void round2DeathRecoveryEggXpCostAndHeldCombatRationMatchLegacy(GameTestHelper helper) {
        ServerPlayer owner = makeServerBackedPlayer(helper, "Round2EggOwner");
        helper.getLevel().players().add(owner);
        ServerLevel level = helper.getLevel();

        LegacyShipEntity ship = createOwnedShip(helper, owner, 2, 4.5D, 2.0D, 4.5D);
        if (ship == null) {
            return;
        }
        ship.setShipLevel(15);
        ship.setShipExperience(250);
        ItemStack recovered = LegacyShipSpawnEggItem.createRecoveredShipStack(ship);
        ship.discard();

        CompoundTag shipData = LegacyShipSpawnEggItem.getRecoveredShipData(recovered);
        if (shipData == null || shipData.getInt("ShipLevel") != 14 || shipData.getInt("ShipExperience") != 0) {
            helper.fail("death recovery egg should set level - 1 (14) and reset XP to 0");
            return;
        }

        BlockPos groundRel = new BlockPos(4, 1, 4);
        helper.setBlock(groundRel, Blocks.STONE.defaultBlockState());
        BlockPos groundAbs = helper.absolutePos(groundRel);
        owner.gameMode.changeGameModeForPlayer(net.minecraft.world.level.GameType.SURVIVAL);
        owner.experienceLevel = 1;
        owner.setItemInHand(InteractionHand.MAIN_HAND, recovered);
        InteractionResult failRes = recovered.useOn(new UseOnContext(
                owner, InteractionHand.MAIN_HAND,
                new BlockHitResult(Vec3.atCenterOf(groundAbs), Direction.UP, groundAbs, false)));
        if (failRes.consumesAction()) {
            helper.fail("survival spawn of recovered level 14 ship should fail when player XP level < 4");
            return;
        }

        owner.experienceLevel = 10;
        InteractionResult okRes = recovered.useOn(new UseOnContext(
                owner, InteractionHand.MAIN_HAND,
                new BlockHitResult(Vec3.atCenterOf(groundAbs), Direction.UP, groundAbs, false)));
        if (!okRes.consumesAction() || owner.experienceLevel != 6) {
            helper.fail("survival spawn of recovered level 14 ship should deduct 14/3 = 4 XP levels (got "
                    + owner.experienceLevel + ")");
            return;
        }

        LegacyShipEntity spawned = level.getEntitiesOfClass(
                LegacyShipEntity.class,
                new net.minecraft.world.phys.AABB(groundAbs).inflate(6.0D),
                s -> s.isOwnedBy(owner)).stream().findFirst().orElse(null);
        if (spawned == null) {
            helper.fail("recovered ship should have spawned");
            return;
        }
        spawned.setGrudge(100);
        ItemStack ration = new ItemStack(ModItems.COMBATRATION.get());
        owner.setItemInHand(InteractionHand.MAIN_HAND, ration);
        owner.setPos(spawned.getX() + 4.0D, spawned.getY(), spawned.getZ());
        for (int t = 0; t <= 16; t++) {
            owner.tickCount = t;
            ration.inventoryTick(level, owner, owner.getInventory().selected, true);
        }
        if (spawned.getEmotion() != 3) {
            helper.fail("held CombatRation should attract owned ship and set emotion=3 (got "
                    + spawned.getEmotion() + ")");
            spawned.discard();
            return;
        }

        spawned.discard();
        helper.succeed();
    }

    private static void clearLightRegion(ServerLevel level, BlockPos center) {
        for (int dy = -1; dy <= 2; dy++) {
            for (int dx = -1; dx <= 1; dx++) {
                for (int dz = -1; dz <= 1; dz++) {
                    BlockPos pos = center.offset(dx, dy, dz);
                    if (level.getBlockState(pos).is(ModBlocks.BLOCK_LIGHT_AIR.get())
                            || level.getBlockState(pos).is(ModBlocks.BLOCK_LIGHT_LIQUID.get())) {
                        level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
                    }
                }
            }
        }
    }

    private static BlockPos findPlacedLightPos(ServerLevel level, BlockPos center) {
        for (int dy = 0; dy <= 1; dy++) {
            for (int dx = -1; dx <= 1; dx++) {
                for (int dz = -1; dz <= 1; dz++) {
                    BlockPos pos = center.offset(dx, dy, dz);
                    if (level.getBlockState(pos).is(ModBlocks.BLOCK_LIGHT_AIR.get())
                            || level.getBlockState(pos).is(ModBlocks.BLOCK_LIGHT_LIQUID.get())) {
                        return pos;
                    }
                }
            }
        }
        return null;
    }

    private static TagKey<Item> itemTag(String namespace, String path) {
        return TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath(namespace, path));
    }

    private static TransientCraftingContainer craftingGrid(ItemStack... inputs) {
        TransientCraftingContainer grid = new TransientCraftingContainer(new DummyCraftingMenu(), 3, 3);
        for (int slot = 0; slot < Math.min(9, inputs.length); slot++) {
            grid.setItem(slot, inputs[slot].copy());
        }
        return grid;
    }

    private static ItemStack stack(net.minecraft.world.level.ItemLike item) {
        return new ItemStack(item);
    }

    private static final class DummyCraftingMenu extends AbstractContainerMenu {

        private DummyCraftingMenu() {
            super(MenuType.GENERIC_9x1, -1);
        }

        @Override
        public ItemStack quickMoveStack(Player player, int index) {
            return ItemStack.EMPTY;
        }

        @Override
        public boolean stillValid(Player player) {
            return true;
        }
    }
}
