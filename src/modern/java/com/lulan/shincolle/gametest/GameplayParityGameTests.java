package com.lulan.shincolle.gametest;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.authlib.GameProfile;
import com.lulan.shincolle.ShinColle;
import com.lulan.shincolle.advancement.ModCriteriaTriggers;
import com.lulan.shincolle.combat.WorldCombatRulesSavedData;
import com.lulan.shincolle.blockentity.CraneBlockEntity;
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
import com.lulan.shincolle.playerskill.PlayerSkillRuntimeState;
import com.lulan.shincolle.registry.ModBlocks;
import com.lulan.shincolle.registry.ModEntityTypes;
import com.lulan.shincolle.registry.ModItems;
import com.lulan.shincolle.registry.ModSoundEvents;
import com.lulan.shincolle.sound.ShinColleSoundHelper;
import com.lulan.shincolle.sound.ShipSoundType;
import com.lulan.shincolle.team.TeamSavedData;
import com.lulan.shincolle.teitoku.TeitokuData;
import com.lulan.shincolle.teitoku.TeitokuEvents;
import com.lulan.shincolle.teitoku.TeitokuHelper;
import com.lulan.shincolle.teitoku.ShipCacheSavedData;
import com.lulan.shincolle.teitoku.ShipWorldCacheEntry;
import com.lulan.shincolle.world.ChestLootInjector;
import com.lulan.shincolle.world.HostileEncounterTable;
import com.lulan.shincolle.world.HostileEncounterSpawner;
import io.netty.buffer.Unpooled;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.player.Player;
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

        if (!destroyerI.hostile() || kongou.hostile() || !kongouHostile.hostile() || !airfield.hostile()) {
            helper.fail("modern ship spec hostility flags should preserve abyssal, friendly, hostile-mirror, and installation semantics");
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
        ship.setShipFuel(1234);
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
                || !"1234 / 16000".equals(snapshot.fuelText())
                || !"987 / 5400".equals(snapshot.lightAmmoText())
                || !"654 / 2700".equals(snapshot.heavyAmmoText())
                || !"321 / 16000".equals(snapshot.grudgeText())
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
        if (ship == null || ally == null) {
            helper.fail("owned ships should be creatable for command failure reporting tests");
            return;
        }

        if (ShipCommandService.handleResultForTesting(player,
                ServerboundShipCommandPacket.moveTo(0, ship.getId(), ship.getShipUid(), new BlockPos(1000, 2, 1000)))
                != ShipCommandService.ShipCommandResult.MOVE_TARGET_TOO_FAR) {
            helper.fail("far move commands should report the move-target-too-far failure reason");
            return;
        }

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
        LegacyShipEntity otherOwner = ModEntityTypes.LEGACY_SHIP.get().create(helper.getLevel());
        LegacyShipEntity dead = createOwnedShip(helper, player, 60, 4.5D, 2.0D, 2.5D);
        if (owned == null || otherOwner == null || dead == null) {
            helper.fail("legacy ships should be creatable for command gate tests");
            return;
        }

        otherOwner.setVariantEggMeta(59);
        otherOwner.setPos(3.5D, 2.0D, 2.5D);
        helper.getLevel().addFreshEntity(otherOwner);
        otherOwner.setOwner(UUID.randomUUID(), "OtherAdmiral", 9999);
        dead.setHealth(0.0F);

        if (ShipCommandService.handleForTesting(player, ServerboundShipCommandPacket.moveTo(0, owned.getId(), owned.getShipUid(), new BlockPos(1000, 2, 1000)))
                || owned.getCommandedPos() != null) {
            helper.fail("far move commands should be ignored");
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
        for (String required : List.of("minecraft:stone", "minecraft:gravel", "minecraft:sand", "minecraft:dirt")) {
            if (!targetBlocks.contains(required)) {
                helper.fail("polymetal gravel target block list is missing 1.12 base block: " + required);
                return;
            }
        }
        if (targetBlocks.contains("minecraft:clay")) {
            helper.fail("polymetal gravel target block list must not include modern self-created clay replacement");
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
                {"data/forge/tags/items/foods/combat_ration.json", "shincolle:combatration5"}
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
        ship.setOrderedToSit(true);
        ship.setHealth(ship.getMaxHealth() * 0.5F);
        ship.tick();
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

        if (!transport.light() || !transport.heavy() || transport.hasAirAttack()) {
            helper.fail("transport ships should keep 1.12 default light/heavy attacks without CV air attacks");
            return;
        }
        if (!tenryuu.light() || !tenryuu.heavy() || tenryuu.hasAirAttack()) {
            helper.fail("cruiser behavior should expose light/heavy gun attacks without carrier air strikes");
            return;
        }
        if (!kaga.light() || !kaga.heavy() || !kaga.hasAirAttack()) {
            helper.fail("carrier behavior should keep 1.12 default gun attacks plus CV air strikes");
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
        assertCombatHook(helper, 60, LegacyShipBehaviorCatalog.CombatHook.HEAVY_CRUISER_BARRAGE);
        assertCombatHook(helper, 61, LegacyShipBehaviorCatalog.CombatHook.HEAVY_CRUISER_BARRAGE);
        assertCombatHook(helper, 62, LegacyShipBehaviorCatalog.CombatHook.KONGOU_COMBO);
        assertCombatHook(helper, 63, LegacyShipBehaviorCatalog.CombatHook.KONGOU_COMBO);
        assertCombatHook(helper, 64, LegacyShipBehaviorCatalog.CombatHook.KONGOU_COMBO);
        assertCombatHook(helper, 65, LegacyShipBehaviorCatalog.CombatHook.KONGOU_COMBO);
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
        target.setVariantEggMeta(23);
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

        ship.setShipFuel(0);
        ship.setGrudge(100);
        ship.setLightAmmo(100);
        if (ship.getCompatAttackFailureReason(target, LegacyShipAttackKind.LIGHT)
                != LegacyShipEntity.CompatAttackFailureReason.NO_FUEL) {
            helper.fail("empty fuel should report fuel feedback");
            return;
        }

        ship.setShipFuel(100);
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
        target.setVariantEggMeta(23);
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

        ship.setShipFuel(0);
        ship.setLightAmmo(0);
        ship.setHeavyAmmo(0);
        ship.setGrudge(0);
        if (ship.performPlayerCompatAttack(target, LegacyShipAttackKind.LIGHT)
                || ship.getCompatAttackCooldown(LegacyShipAttackKind.LIGHT) > 0) {
            helper.fail("friendly ranged combat should be gated by ship fuel, ammo, and grudge");
            return;
        }

        ship.setShipFuel(10);
        ship.setLightAmmo(10);
        ship.setGrudge(20);
        int fuelBefore = ship.getShipFuel();
        int lightAmmoBefore = ship.getLightAmmo();
        int grudgeBefore = ship.getGrudge();
        if (!ship.performPlayerCompatAttack(target, LegacyShipAttackKind.LIGHT)) {
            helper.fail("friendly ranged combat should resume after runtime supplies are restored");
            return;
        }
        if (ship.getShipFuel() != fuelBefore - 1
                || ship.getLightAmmo() >= lightAmmoBefore
                || ship.getGrudge() >= grudgeBefore) {
            helper.fail("friendly ranged combat should consume fuel, ammo, and grudge once per attack");
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
        if (loaded.getShipFuel() != ship.getShipFuel()
                || loaded.getLightAmmo() != ship.getLightAmmo()
                || loaded.getHeavyAmmo() != ship.getHeavyAmmo()
                || loaded.getGrudge() != ship.getGrudge()) {
            helper.fail("ship fuel, ammo, and grudge should persist through entity NBT");
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

        ship.setShipFuel(0);
        ship.setLightAmmo(0);
        ship.setHeavyAmmo(0);
        ship.setGrudge(0);
        ship.setMorale(0);
        int moraleBefore = ship.getMorale();

        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(ModItems.COMBATRATION.get()));
        if (ship.mobInteract(player, InteractionHand.MAIN_HAND) != InteractionResult.CONSUME
                || ship.getShipFuel() != 0
                || ship.getLightAmmo() != 0
                || ship.getHeavyAmmo() != 0
                || ship.getMorale() <= moraleBefore
                || ship.getGrudge() <= 0) {
            helper.fail("combat rations should restore morale and grudge without refueling or rearming ships");
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
                stack(ModItems.GRUDGE.get()), stack(Items.STICK));
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
                stack(Items.COOKED_BEEF), stack(ModItems.GRUDGE.get()), stack(Items.GOLDEN_CARROT),
                stack(Items.WHEAT), stack(Items.WHEAT), stack(Items.WHEAT));

        TransientCraftingContainer rawBeefInput = craftingGrid(
                stack(Items.WHEAT), stack(Items.WHEAT), stack(Items.WHEAT),
                stack(Items.BEEF), stack(ModItems.GRUDGE.get()), stack(Items.GOLDEN_CARROT),
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
        var profile = new com.lulan.shincolle.world.HostileSpawnProfile(23, 1, true, true);
        LegacyShipEntity spawned = HostileEncounterSpawner.spawnEncounterAt(helper.getLevel(), anchor, null, profile);
        if (spawned == null) {
            helper.fail("hostile encounter helper should spawn into an open game-test area");
            return;
        }

        if (spawned.getVariantEggMeta() != 23
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
        Player player = helper.makeMockPlayer();
        LegacyShipEntity boss = ModEntityTypes.LEGACY_SHIP.get().create(helper.getLevel());
        if (boss == null) {
            helper.fail("legacy ship entities should be creatable for hostile targeting tests");
            return;
        }

        boss.setVariantEggMeta(23);
        boss.initializeHostileRuntime(false, true, true);
        boss.setPos(5.0D, 2.0D, 5.0D);
        player.setPos(7.0D, 2.0D, 5.0D);

        player.getAbilities().invulnerable = true;
        player.getAbilities().instabuild = true;
        if (boss.canEngage(player)) {
            helper.fail("hostile bosses should ignore invulnerable creative or spectator players");
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

        ship.setVariantEggMeta(58);
        ship.setOwner(UUID.randomUUID(), "PickupTester");
        ship.setPos(1.0D, 2.0D, 1.0D);
        helper.getLevel().addFreshEntity(ship);

        ItemEntity grudgeDrop = new ItemEntity(helper.getLevel(), 2.1D, 2.0D, 1.1D, new ItemStack(ModItems.GRUDGE.get(), 3));
        helper.getLevel().addFreshEntity(grudgeDrop);

        LegacyShipPickItemGoal goal = new LegacyShipPickItemGoal(ship, 1.0D);
        if (!goal.canUse()) {
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
        if (pickupGoalCanUseWith(helper, 4, ship -> ship.setAiAutoSupply(false))) {
            helper.fail("pickup goal must respect the auto-supply AI flag");
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
        ship.setVariantEggMeta(58);
        ship.setOwner(UUID.randomUUID(), "PickupBoundary");
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
        ship.setPos(x, y, z);
        helper.getLevel().addFreshEntity(ship);
        ship.setOwner(player);
        return ship;
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
