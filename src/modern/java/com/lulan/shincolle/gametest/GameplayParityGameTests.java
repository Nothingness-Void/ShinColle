package com.lulan.shincolle.gametest;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.lulan.shincolle.ShinColle;
import com.lulan.shincolle.advancement.ModCriteriaTriggers;
import com.lulan.shincolle.combat.WorldCombatRulesSavedData;
import com.lulan.shincolle.blockentity.CraneBlockEntity;
import com.lulan.shincolle.blockentity.HeavyGrudgeBlockEntity;
import com.lulan.shincolle.blockentity.LargeShipyardStructureHelper;
import com.lulan.shincolle.blockentity.LargeShipyardBlockEntity;
import com.lulan.shincolle.blockentity.LegacyCoreBlockEntity;
import com.lulan.shincolle.blockentity.PolymetalServantBlockEntity;
import com.lulan.shincolle.crafting.LegacyShipConstructionHelper;
import com.lulan.shincolle.crafting.SmallShipyardRecipes;
import com.lulan.shincolle.entity.projectile.LegacyShipProjectileEntity;
import com.lulan.shincolle.entity.projectile.LegacyShipProjectileMoveType;
import com.lulan.shincolle.entity.projectile.LegacyShipProjectileVisual;
import com.lulan.shincolle.entity.mount.LegacyMountEntity;
import com.lulan.shincolle.entity.ship.LegacyShipAttackKind;
import com.lulan.shincolle.entity.ship.LegacyShipAttackProfile;
import com.lulan.shincolle.entity.ship.LegacyShipAircraftEntity;
import com.lulan.shincolle.entity.ship.LegacyShipEntity;
import com.lulan.shincolle.entity.ship.LegacyShipStats;
import com.lulan.shincolle.entity.ship.ShipEntitySpec;
import com.lulan.shincolle.entity.ship.ShipEntitySpecs;
import com.lulan.shincolle.entity.ship.ShipArchetype;
import com.lulan.shincolle.entity.ship.ShipEquipmentBehaviorState;
import com.lulan.shincolle.entity.ship.ShipEquipmentProfile;
import com.lulan.shincolle.entity.ship.goal.LegacyShipPickItemGoal;
import com.lulan.shincolle.morph.MorphHelper;
import com.lulan.shincolle.morph.MorphHostMode;
import com.lulan.shincolle.morph.MorphProfile;
import com.lulan.shincolle.playerskill.PlayerSkillRuntimeState;
import com.lulan.shincolle.registry.ModBlocks;
import com.lulan.shincolle.registry.ModEntityTypes;
import com.lulan.shincolle.registry.ModItems;
import com.lulan.shincolle.team.TeamSavedData;
import com.lulan.shincolle.teitoku.TeitokuData;
import com.lulan.shincolle.teitoku.ShipCacheSavedData;
import com.lulan.shincolle.teitoku.ShipWorldCacheEntry;
import com.lulan.shincolle.world.HostileEncounterTable;
import com.lulan.shincolle.world.HostileEncounterSpawner;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.TransientCraftingContainer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.Block;
import net.minecraft.network.chat.Component;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.common.capabilities.ForgeCapabilities;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.UUID;

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
            helper.fail("core route energy receive should fill stored charge");
            return;
        }
        if (core.extractRouteEnergy(400, true) != 0) {
            helper.fail("idle core should not export route energy until set to drain mode");
            return;
        }

        core.cycleCoreMode();
        core.cycleCoreMode();
        if (core.extractRouteEnergy(400, false) != 400 || core.getRouteEnergyStored() != 800) {
            helper.fail("drain mode should allow cores to export route energy");
            return;
        }

        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void shipyardRouteEnergyAccessUsesPowerPool(GameTestHelper helper) {
        LargeShipyardBlockEntity shipyard = new LargeShipyardBlockEntity(BlockPos.ZERO, ModBlocks.BLOCK_LARGE_SHIPYARD.get().defaultBlockState());
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
    public static void legacyCoreFuelChargesAndDischarges(GameTestHelper helper) {
        LegacyCoreBlockEntity core = LegacyCoreBlockEntity.newVolCore(BlockPos.ZERO, ModBlocks.BLOCK_VOL_CORE.get().defaultBlockState());
        ItemStack fuel = new ItemStack(ModItems.GRUDGE.get(), 1);
        int inserted = core.insertFuel(fuel);
        if (inserted != 1 || core.getFuelItemCount() != 1) {
            helper.fail("legacy core should accept grudge fuel into its internal storage");
            return;
        }

        core.cycleMode();
        LegacyCoreBlockEntity.serverTick(helper.getLevel(), BlockPos.ZERO, ModBlocks.BLOCK_VOL_CORE.get().defaultBlockState(), core);
        if (core.getStoredCharge() <= 0 || core.canProvideCharge()) {
            helper.fail("charge mode should convert fuel into stored charge without exposing discharge output");
            return;
        }

        int chargedAmount = core.getStoredCharge();
        core.cycleMode();
        if (!core.canProvideCharge()) {
            helper.fail("drain mode should expose stored charge for linked systems");
            return;
        }

        LegacyCoreBlockEntity.serverTick(helper.getLevel(), BlockPos.ZERO, ModBlocks.BLOCK_VOL_CORE.get().defaultBlockState(), core);
        if (core.getStoredCharge() >= chargedAmount) {
            helper.fail("drain mode should spend stored charge over time");
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
    public static void heavyGrudgeSwitchesFromCoreToShipyard(GameTestHelper helper) {
        BlockPos master = helper.absolutePos(new BlockPos(1, 4, 1));
        helper.getLevel().setBlock(master, ModBlocks.BLOCK_GRUDGE_HEAVY.get().defaultBlockState(), Block.UPDATE_ALL);

        if (!(helper.getLevel().getBlockEntity(master) instanceof HeavyGrudgeBlockEntity heavy)) {
            helper.fail("heavy grudge block entity should exist");
            return;
        }

        heavy.getFuelItems().setStackInSlot(0, new ItemStack(ModItems.GRUDGE.get()));
        heavy.cycleCoreMode();
        HeavyGrudgeBlockEntity.serverTick(helper.getLevel(), master, helper.getLevel().getBlockState(master), heavy);
        if (heavy.getCoreContainerData().get(1) <= 0 || heavy.isStructureComplete()) {
            helper.fail("unformed heavy grudge should work as a standalone charging core");
            return;
        }

        for (BlockPos servantPos : LargeShipyardStructureHelper.getServantPositions(master)) {
            helper.getLevel().setBlock(servantPos, ModBlocks.BLOCK_POLYMETAL.get().defaultBlockState(), Block.UPDATE_ALL);
        }

        for (int i = 0; i < 20; i++) {
            HeavyGrudgeBlockEntity.serverTick(helper.getLevel(), master, helper.getLevel().getBlockState(master), heavy);
        }

        if (!heavy.isStructureComplete()) {
            helper.fail("completed legacy structure should switch heavy grudge into shipyard master mode");
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

        for (int i = 0; i < 20; i++) {
            HeavyGrudgeBlockEntity.serverTick(helper.getLevel(), master, helper.getLevel().getBlockState(master), heavy);
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

        HeavyGrudgeBlockEntity.serverTick(helper.getLevel(), master, helper.getLevel().getBlockState(master), heavy);
        if (heavy.getMaterialAmounts()[3] != 8) {
            helper.fail("materials inserted through a servant block should be absorbed by the shipyard master");
            return;
        }

        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void langPlaceholdersDescribeLiveFeatures(GameTestHelper helper) {
        JsonObject lang = loadJsonResource("assets/shincolle/lang/en_us.json");
        if (lang == null) {
            helper.fail("expected en_us language resource to be present");
            return;
        }

        String[] keys = {
                "gui.shincolle.placeholder_equip",
                "gui.shincolle.placeholder.block.crane",
                "gui.shincolle.placeholder.block.desk",
                "gui.shincolle.placeholder_item",
                "gui.shincolle.placeholder_desk_item",
                "gui.shincolle.placeholder_command_item",
                "gui.shincolle.placeholder_spawn_egg"
        };
        for (String key : keys) {
            if (!lang.has(key)) {
                helper.fail("missing lang key: " + key);
                return;
            }
            String value = lang.get(key).getAsString().toLowerCase();
            if (value.contains("pending port") || value.contains("will return")) {
                helper.fail("lang key still reports unfinished behavior: " + key);
                return;
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
                || liveEntry.dead()
                || !"Cache Kongou".equals(liveEntry.resolveDisplayName().getString())) {
            helper.fail("live ship cache entries should preserve class, owner, and display name state");
            return;
        }

        data.updateFromShip(ship, true);
        CompoundTag saved = data.save(new CompoundTag());
        ShipCacheSavedData loaded = ShipCacheSavedData.load(saved);
        ShipWorldCacheEntry deadEntry = loaded.getShip(ship.getShipUid());

        if (deadEntry == null
                || !deadEntry.dead()
                || deadEntry.entityId() != ship.getId()
                || !deadEntry.dimensionId().equals(helper.getLevel().dimension().location().toString())
                || deadEntry.entityTag().isEmpty()) {
            helper.fail("ship cache save/load should preserve dead-state snapshots, entity id, dimension, and entity NBT");
            return;
        }

        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void projectileProfilesMatchShipRoles(GameTestHelper helper) {
        LegacyShipAttackProfile kongou = LegacyShipAttackProfile.resolve(ShipEntitySpecs.getByEggMeta(62));
        LegacyShipAttackProfile kongouHostileMirror = LegacyShipAttackProfile.resolve(ShipEntitySpecs.getByEggMeta(2062));
        LegacyShipAttackProfile akagi = LegacyShipAttackProfile.resolve(ShipEntitySpecs.getByEggMeta(50));
        LegacyShipAttackProfile wo = LegacyShipAttackProfile.resolve(ShipEntitySpecs.getByEggMeta(14));
        LegacyShipAttackProfile airfield = LegacyShipAttackProfile.resolve(ShipEntitySpecs.getByEggMeta(23));

        if (kongou.projectileProfile(LegacyShipAttackKind.HEAVY).visual() != LegacyShipProjectileVisual.MISSILE
                || kongou.projectileProfile(LegacyShipAttackKind.HEAVY).moveType() != LegacyShipProjectileMoveType.ARC) {
            helper.fail("friendly battleships should keep the legacy heavy missile barrage arc");
            return;
        }
        if (kongouHostileMirror.projectileProfile(LegacyShipAttackKind.HEAVY).moveType() != LegacyShipProjectileMoveType.GUIDED) {
            helper.fail("hostile mirror battleships should override the default heavy launch into guided barrages");
            return;
        }
        if (akagi.projectileProfile(LegacyShipAttackKind.AIR_HEAVY).visual() != LegacyShipProjectileVisual.TORPEDO) {
            helper.fail("friendly carriers should use torpedo-family visuals for heavy air strikes");
            return;
        }
        if (wo.projectileProfile(LegacyShipAttackKind.AIR_HEAVY).visual() != LegacyShipProjectileVisual.BOMB
                || airfield.projectileProfile(LegacyShipAttackKind.HEAVY).moveType() != LegacyShipProjectileMoveType.GUIDED) {
            helper.fail("abyssal carriers and installations should use bomber or guided installation overrides");
            return;
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

        owner.setVariantEggMeta(50);
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
                owner.getBoundingBox().inflate(20.0D));
        if (aircraft.isEmpty()) {
            helper.fail("air attacks should spawn aircraft entities into the world");
            return;
        }
        if (!helper.getLevel().getEntitiesOfClass(LegacyShipProjectileEntity.class,
                owner.getBoundingBox().inflate(20.0D),
                projectile -> projectile.getAttackKind() == LegacyShipAttackKind.AIR_HEAVY).isEmpty()) {
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
    public static void morphSpecialDispatchCoversCruiserAndKongouClasses(GameTestHelper helper) {
        if (!MorphHelper.hasSpecialSkill(new MorphProfile(58))
                || !MorphHelper.hasSpecialSkill(new MorphProfile(59))
                || !MorphHelper.hasSpecialSkill(new MorphProfile(60))
                || !MorphHelper.hasSpecialSkill(new MorphProfile(61))
                || !MorphHelper.hasSpecialSkill(new MorphProfile(62))
                || !MorphHelper.hasSpecialSkill(new MorphProfile(65))
                || MorphHelper.hasSpecialSkill(new MorphProfile(49))) {
            helper.fail("morph special dispatch should include Tenryuu, Tatsuta, Takao-class, and Kongou-class profiles without granting carriers a false special");
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
    public static void growthLoopBuildsSmallEggAndBossGate(GameTestHelper helper) {
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
        if (resolved == null || resolved.hostile()) {
            helper.fail("small shipyard eggs should resolve into friendly construction ships");
            return;
        }

        TeitokuData data = new TeitokuData();
        if (HostileEncounterSpawner.canRollBossEncounter(data)) {
            helper.fail("boss encounters should stay locked before the admiral deploys a ship");
            return;
        }
        data.addCollectedShip(2);
        if (!HostileEncounterSpawner.canRollBossEncounter(data)) {
            helper.fail("boss encounters should unlock after the first friendly ship is collected");
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

        if (smallEgg.hostile() || largeEgg.hostile()
                || abyssDestroyerCounterpart.hostile() || airfieldCounterpart.hostile()) {
            helper.fail("construction eggs and hostile loot counterparts should always resolve to friendly ship specs");
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
        assertCrafts(helper, "blocklargeshipyard", new ItemStack(ModBlocks.BLOCK_LARGE_SHIPYARD.get()),
                stack(ModBlocks.BLOCK_GRUDGE_HEAVY_DECO.get()), stack(ModBlocks.BLOCK_GRUDGE_HEAVY_DECO.get()), stack(ModBlocks.BLOCK_GRUDGE_HEAVY_DECO.get()),
                stack(ModBlocks.BLOCK_GRUDGE_HEAVY_DECO.get()), stack(ModBlocks.BLOCK_GRUDGE_HEAVY.get()), stack(ModBlocks.BLOCK_GRUDGE_HEAVY_DECO.get()),
                stack(ModBlocks.BLOCK_GRUDGE_HEAVY_DECO.get()), stack(ModBlocks.BLOCK_GRUDGE_HEAVY_DECO.get()), stack(ModBlocks.BLOCK_GRUDGE_HEAVY_DECO.get()));

        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void reachableShipVisualAssetsExist(GameTestHelper helper) {
        LinkedHashSet<ShipEntitySpec> reachableSpecs = new LinkedHashSet<>();
        reachableSpecs.addAll(ShipEntitySpecs.currentPlayableFriendlyRoster());
        reachableSpecs.addAll(HostileEncounterTable.reachableShipSpecs());

        for (ShipEntitySpec spec : reachableSpecs) {
            if (!resourceExists(spec.modelSourceLocation()) || !resourceExists(spec.textureLocation())) {
                helper.fail("reachable ship spec is missing a legacy model source or texture: egg="
                        + spec.eggMeta() + " stem=" + spec.textureStem());
                return;
            }
        }

        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void summonAndMountAssetsExist(GameTestHelper helper) {
        ResourceLocation[] requiredResources = {
                ResourceLocation.fromNamespaceAndPath(ShinColle.MOD_ID, "legacy_model_sources/modelairplanezero.java"),
                ResourceLocation.fromNamespaceAndPath(ShinColle.MOD_ID, "legacy_model_sources/modelairplanet.java"),
                ResourceLocation.fromNamespaceAndPath(ShinColle.MOD_ID, "legacy_model_sources/modeltakoyaki.java"),
                ResourceLocation.fromNamespaceAndPath(ShinColle.MOD_ID, "legacy_model_sources/modelmountafh.java"),
                ResourceLocation.fromNamespaceAndPath(ShinColle.MOD_ID, "legacy_model_sources/modelmountbah.java"),
                ResourceLocation.fromNamespaceAndPath(ShinColle.MOD_ID, "legacy_model_sources/modelmountcah.java"),
                ResourceLocation.fromNamespaceAndPath(ShinColle.MOD_ID, "legacy_model_sources/modelmountcawd.java"),
                ResourceLocation.fromNamespaceAndPath(ShinColle.MOD_ID, "legacy_model_sources/modelmounthbh.java"),
                ResourceLocation.fromNamespaceAndPath(ShinColle.MOD_ID, "legacy_model_sources/modelmountish.java"),
                ResourceLocation.fromNamespaceAndPath(ShinColle.MOD_ID, "legacy_model_sources/modelmountmih.java"),
                ResourceLocation.fromNamespaceAndPath(ShinColle.MOD_ID, "legacy_model_sources/modelmountsuh.java"),
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

        for (ResourceLocation resource : requiredResources) {
            if (!resourceExists(resource)) {
                helper.fail("expected summon or mount asset to exist: " + resource);
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
