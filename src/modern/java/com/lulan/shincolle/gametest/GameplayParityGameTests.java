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
import com.lulan.shincolle.blockentity.WaypointBlockEntity;
import com.lulan.shincolle.crafting.LegacyShipConstructionHelper;
import com.lulan.shincolle.crafting.LargeShipyardRecipes;
import com.lulan.shincolle.crafting.ShipyardBuildTypes;
import com.lulan.shincolle.crafting.SmallShipyardRecipes;
import com.lulan.shincolle.entity.projectile.LegacyShipProjectileEntity;
import com.lulan.shincolle.entity.projectile.LegacyShipProjectileMoveType;
import com.lulan.shincolle.entity.projectile.LegacyShipProjectileVisual;
import com.lulan.shincolle.entity.mount.LegacyMountEntity;
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
import com.lulan.shincolle.network.GameplayCommandHandler;
import com.lulan.shincolle.network.GameplayCommandType;
import com.lulan.shincolle.network.ServerboundShipCommandPacket;
import com.lulan.shincolle.network.ShipCommandAction;
import com.lulan.shincolle.network.ShipCommandService;
import com.lulan.shincolle.menu.CraneTerminalMenu;
import com.lulan.shincolle.menu.LargeShipyardMenu;
import com.lulan.shincolle.menu.LegacyCoreMenu;
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
import com.lulan.shincolle.teitoku.TeitokuHelper;
import com.lulan.shincolle.teitoku.ShipCacheSavedData;
import com.lulan.shincolle.teitoku.ShipWorldCacheEntry;
import com.lulan.shincolle.world.HostileEncounterTable;
import com.lulan.shincolle.world.HostileEncounterSpawner;
import io.netty.buffer.Unpooled;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
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
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.IFluidHandler;
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
    public static void legacyRingPassivesApplyFirstMigratedShipEffects(GameTestHelper helper) {
        LegacyShipEntity submarine = ModEntityTypes.LEGACY_SHIP.get().create(helper.getLevel());
        LegacyShipEntity carrier = ModEntityTypes.LEGACY_SHIP.get().create(helper.getLevel());
        LegacyShipEntity escort = ModEntityTypes.LEGACY_SHIP.get().create(helper.getLevel());
        if (submarine == null || carrier == null || escort == null) {
            helper.fail("legacy ship entities should be creatable for ring passive tests");
            return;
        }

        submarine.setVariantEggMeta(40);
        submarine.setMarried(true);
        submarine.setShipLevel(60);
        submarine.setPos(1.0D, 2.0D, 1.0D);
        helper.getLevel().addFreshEntity(submarine);

        UUID ownerUuid = UUID.randomUUID();
        carrier.setVariantEggMeta(49);
        carrier.setOwner(ownerUuid, "PassiveTester", 91);
        carrier.setMarried(true);
        carrier.setShipLevel(85);
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
            helper.fail("U511/Ro500 migrated ring passive should apply invisibility to the ship");
            return;
        }

        if (!escort.hasEffect(MobEffects.JUMP) || escort.getEffect(MobEffects.JUMP).getAmplifier() != 1) {
            helper.fail("Kaga/Akagi migrated ring passive should apply jump boost to nearby allied ships");
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
        BlockPos shipyardPos = helper.absolutePos(new BlockPos(3, 2, 1));
        helper.getLevel().setBlock(cranePos, ModBlocks.BLOCK_CRANE.get().defaultBlockState(), Block.UPDATE_ALL);
        helper.getLevel().setBlock(shipyardPos, ModBlocks.BLOCK_LARGE_SHIPYARD.get().defaultBlockState(), Block.UPDATE_ALL);

        if (!(helper.getLevel().getBlockEntity(cranePos) instanceof CraneBlockEntity crane)) {
            helper.fail("crane block entity should exist for route transfer");
            return;
        }
        if (!(helper.getLevel().getBlockEntity(shipyardPos) instanceof LargeShipyardBlockEntity shipyard)) {
            helper.fail("large shipyard should expose route energy for crane transfer");
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
    public static void largeShipyardBuildsLargeShipEggFromCompleteRing(GameTestHelper helper) {
        BlockPos shipyardPos = helper.absolutePos(new BlockPos(4, 2, 4));
        helper.getLevel().setBlock(shipyardPos, ModBlocks.BLOCK_LARGE_SHIPYARD.get().defaultBlockState(), Block.UPDATE_ALL);
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                if (dx == 0 && dz == 0) {
                    continue;
                }
                helper.getLevel().setBlock(shipyardPos.offset(dx, 0, dz),
                        ModBlocks.BLOCK_GRUDGE_HEAVY_DECO.get().defaultBlockState(), Block.UPDATE_ALL);
            }
        }

        if (!(helper.getLevel().getBlockEntity(shipyardPos) instanceof LargeShipyardBlockEntity shipyard)) {
            helper.fail("large shipyard block entity should exist for build-loop test");
            return;
        }

        shipyard.getItems().setStackInSlot(LargeShipyardRecipes.SLOT_GRUDGE_A, new ItemStack(ModItems.GRUDGE.get(), 50));
        shipyard.getItems().setStackInSlot(LargeShipyardRecipes.SLOT_GRUDGE_B, new ItemStack(ModItems.GRUDGE.get(), 50));
        shipyard.getItems().setStackInSlot(LargeShipyardRecipes.SLOT_ABYSSIUM_A, new ItemStack(ModItems.ABYSSMETAL.get(), 50));
        shipyard.getItems().setStackInSlot(LargeShipyardRecipes.SLOT_ABYSSIUM_B, new ItemStack(ModItems.ABYSSMETAL.get(), 50));
        shipyard.getItems().setStackInSlot(LargeShipyardRecipes.SLOT_AMMO_A, new ItemStack(ModItems.AMMO.get(), 50));
        shipyard.getItems().setStackInSlot(LargeShipyardRecipes.SLOT_AMMO_B, new ItemStack(ModItems.AMMO.get(), 50));
        shipyard.getItems().setStackInSlot(LargeShipyardRecipes.SLOT_POLYMETAL_A, new ItemStack(ModItems.ABYSSMETAL1.get(), 50));
        shipyard.getItems().setStackInSlot(LargeShipyardRecipes.SLOT_POLYMETAL_B, new ItemStack(ModItems.ABYSSMETAL1.get(), 50));
        shipyard.getItems().setStackInSlot(LargeShipyardRecipes.SLOT_FUEL, new ItemStack(Items.LAVA_BUCKET));

        LargeShipyardBlockEntity.serverTick(helper.getLevel(), shipyardPos, helper.getLevel().getBlockState(shipyardPos), shipyard);
        if (!shipyard.isStructureComplete() || shipyard.getPowerRemained() <= 0) {
            helper.fail("complete ring and fuel slot should make the large shipyard ready with stored power");
            return;
        }

        shipyard.receiveRouteEnergy(LargeShipyardBlockEntity.POWER_MAX, false);
        shipyard.setBuildType(ShipyardBuildTypes.SHIP);
        if (shipyard.getPowerGoal() <= 0) {
            helper.fail("large shipyard should calculate a ship build goal from inserted materials");
            return;
        }

        shipyard.getContainerData().set(1, shipyard.getPowerGoal() - LargeShipyardBlockEntity.BUILD_SPEED);
        LargeShipyardBlockEntity.serverTick(helper.getLevel(), shipyardPos, helper.getLevel().getBlockState(shipyardPos), shipyard);
        ItemStack output = shipyard.getItems().getStackInSlot(LargeShipyardRecipes.SLOT_OUTPUT);
        if (!output.is(ModItems.SHIPSPAWNEGG_ITEMS.get(1).get())
                || shipyard.getBuildType() != ShipyardBuildTypes.NONE
                || LargeShipyardRecipes.getMaterialAmounts(shipyard.getItems())[0] != 0) {
            helper.fail("large shipyard should spend materials and output a large ship egg when the build completes");
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

        BlockPos shipyardPos = helper.absolutePos(new BlockPos(4, 2, 1));
        helper.getLevel().setBlock(shipyardPos, ModBlocks.BLOCK_LARGE_SHIPYARD.get().defaultBlockState(), Block.UPDATE_ALL);
        if (!(helper.getLevel().getBlockEntity(shipyardPos) instanceof LargeShipyardBlockEntity shipyard)) {
            helper.fail("large shipyard block entity should exist for menu data coverage");
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
        ship.setOwner(player);
        ship.setPos(2.5D, 2.0D, 2.5D);
        ship.getShipInventory().setItem(0, new ItemStack(ModItems.EQUIPDRUM_ITEMS.get(2).get()));
        helper.getLevel().addFreshEntity(ship);

        ShipInventoryMenu shipMenu = new ShipInventoryMenu(5, player.getInventory(), ship.getId());
        if (shipMenu.getAiFlags() != ship.getAiFlagsBitmask()
                || "- / -".equals(shipMenu.getRouteEnergyText())
                || "-".equals(shipMenu.getAttackText())
                || !shipMenu.clickMenuButton(player, ShipInventoryMenu.BUTTON_TOGGLE_MODE)
                || !ship.isOrderedToSit()) {
            helper.fail("ship inventory menu should expose combat/AI route data and mutate escort mode through the server button");
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
                ServerboundShipCommandPacket.openInventory(18, 108)
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
    public static void selectedAndCurrentTeamCommandsRespectFormationOffsets(GameTestHelper helper) {
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
            helper.fail("selected-team move command should affect selected ships");
            return;
        }
        if (ships[0].getCommandedPos() == null
                || ships[2].getCommandedPos() == null
                || ships[1].getCommandedPos() != null
                || ships[3].getCommandedPos() != null
                || ships[4].getCommandedPos() != null) {
            helper.fail("selected-team command should only affect selected slots");
            return;
        }
        if (selectedTarget.equals(ships[0].getCommandedPos())) {
            helper.fail("formation move should offset non-center slots when team size exceeds four");
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
    public static void registeredPhaseOneResourcesResolve(GameTestHelper helper) {
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
        WorldCombatRulesSavedData.get(helper.getLevel()).toggleUnattackable(worldRuleKey);

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
        assertMarriagePassive(helper, 40, LegacyShipBehaviorCatalog.MarriagePassive.SELF_AND_OWNER_INVISIBILITY);
        assertMarriagePassive(helper, 41, LegacyShipBehaviorCatalog.MarriagePassive.SELF_AND_OWNER_INVISIBILITY);
        assertMarriagePassive(helper, 49, LegacyShipBehaviorCatalog.MarriagePassive.ALLIED_JUMP_AURA);
        assertMarriagePassive(helper, 50, LegacyShipBehaviorCatalog.MarriagePassive.ALLIED_JUMP_AURA);
        assertMarriagePassive(helper, 53, LegacyShipBehaviorCatalog.MarriagePassive.OWNER_HASTE);
        assertMarriagePassive(helper, 54, LegacyShipBehaviorCatalog.MarriagePassive.OWNER_JUMP);
        assertMarriagePassive(helper, 55, LegacyShipBehaviorCatalog.MarriagePassive.OWNER_STRENGTH);
        assertMarriagePassive(helper, 56, LegacyShipBehaviorCatalog.MarriagePassive.OWNER_SPEED);
        assertMarriagePassive(helper, 58, LegacyShipBehaviorCatalog.MarriagePassive.NONE);
        assertMarriagePassive(helper, 2040, LegacyShipBehaviorCatalog.MarriagePassive.NONE);

        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void perShipBehaviorCatalogRoutesAttackProfiles(GameTestHelper helper) {
        LegacyShipAttackProfile transport = LegacyShipBehaviorCatalog.attackProfile(ShipEntitySpecs.getByEggMeta(18));
        LegacyShipAttackProfile tenryuu = LegacyShipBehaviorCatalog.attackProfile(ShipEntitySpecs.getByEggMeta(58));
        LegacyShipAttackProfile kaga = LegacyShipBehaviorCatalog.attackProfile(ShipEntitySpecs.getByEggMeta(49));
        LegacyShipAttackProfile airfield = LegacyShipBehaviorCatalog.attackProfile(ShipEntitySpecs.getByEggMeta(23));

        if (transport.hasRangedAttack()) {
            helper.fail("transport ships should stay melee-only through the behavior catalog");
            return;
        }
        if (!tenryuu.light() || !tenryuu.heavy() || tenryuu.hasAirAttack()) {
            helper.fail("cruiser behavior should expose light/heavy gun attacks without carrier air strikes");
            return;
        }
        if (!kaga.hasAirAttack() || kaga.light() || kaga.heavy()) {
            helper.fail("carrier behavior should expose air strikes without gun attacks");
            return;
        }
        if (!airfield.light() || !airfield.heavy() || !airfield.hasAirAttack()
                || airfield.projectileProfile(LegacyShipAttackKind.HEAVY).moveType() != LegacyShipProjectileMoveType.GUIDED) {
            helper.fail("installation behavior should keep mixed gun/air attacks and guided heavy fire");
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
