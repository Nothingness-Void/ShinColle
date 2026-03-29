package com.lulan.shincolle.gametest;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.lulan.shincolle.ShinColle;
import com.lulan.shincolle.combat.WorldCombatRulesSavedData;
import com.lulan.shincolle.blockentity.CraneBlockEntity;
import com.lulan.shincolle.blockentity.HeavyGrudgeBlockEntity;
import com.lulan.shincolle.blockentity.LargeShipyardStructureHelper;
import com.lulan.shincolle.blockentity.LargeShipyardBlockEntity;
import com.lulan.shincolle.blockentity.LegacyCoreBlockEntity;
import com.lulan.shincolle.blockentity.PolymetalServantBlockEntity;
import com.lulan.shincolle.entity.ship.LegacyShipEntity;
import com.lulan.shincolle.entity.ship.LegacyShipStats;
import com.lulan.shincolle.entity.ship.ShipEntitySpec;
import com.lulan.shincolle.entity.ship.ShipEntitySpecs;
import com.lulan.shincolle.entity.ship.ShipArchetype;
import com.lulan.shincolle.entity.ship.ShipEquipmentBehaviorState;
import com.lulan.shincolle.entity.ship.ShipEquipmentProfile;
import com.lulan.shincolle.registry.ModBlocks;
import com.lulan.shincolle.registry.ModEntityTypes;
import com.lulan.shincolle.registry.ModItems;
import com.lulan.shincolle.team.TeamSavedData;
import com.lulan.shincolle.teitoku.TeitokuData;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.RandomSource;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.common.capabilities.ForgeCapabilities;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
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
        BlockPos master = new BlockPos(1, 4, 1);
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
        BlockPos master = new BlockPos(1, 4, 1);
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
        BlockPos master = new BlockPos(1, 4, 1);
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
}
