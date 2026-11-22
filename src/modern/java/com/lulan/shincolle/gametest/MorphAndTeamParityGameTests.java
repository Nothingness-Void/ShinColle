package com.lulan.shincolle.gametest;

import com.mojang.authlib.GameProfile;
import com.lulan.shincolle.ShinColle;
import com.lulan.shincolle.entity.ship.LegacyShipEntity;
import com.lulan.shincolle.entity.mount.LegacyShipMountEntity;
import com.lulan.shincolle.item.PointerCommandEvents;
import com.lulan.shincolle.item.PointerItem;
import com.lulan.shincolle.network.GameplayCommandHandler;
import com.lulan.shincolle.network.GameplayCommandType;
import com.lulan.shincolle.network.ServerboundShipCommandPacket;
import com.lulan.shincolle.network.ShipCommandService;
import com.lulan.shincolle.registry.ModBlocks;
import com.lulan.shincolle.registry.ModEntityTypes;
import com.lulan.shincolle.registry.ModItems;
import com.lulan.shincolle.teitoku.TeitokuData;
import com.lulan.shincolle.teitoku.TeitokuHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.util.UUID;

@GameTestHolder(ShinColle.MOD_ID)
@PrefixGameTestTemplate(false)
public final class MorphAndTeamParityGameTests {

    private static ServerPlayer player(GameTestHelper helper) {
        ServerPlayer player = FakePlayerFactory.get(helper.getLevel(), new GameProfile(UUID.randomUUID(), "ZeroTeamParity"));
        TeitokuHelper.get(player).orElseThrow().setPlayerUid(98401);
        return player;
    }

    private static LegacyShipEntity ship(GameTestHelper helper, ServerPlayer owner) {
        LegacyShipEntity ship = ModEntityTypes.LEGACY_SHIP.get().create(helper.getLevel());
        helper.assertTrue(ship != null, "ship can be created");
        ship.setVariantEggMeta(2);
        ship.setNoAi(true);
        ship.setNoGravity(true);
        BlockPos pos = helper.absolutePos(new BlockPos(2, 100, 2));
        ship.setPos(pos.getX(), pos.getY(), pos.getZ());
        ship.setOwner(owner.getUUID(), owner.getGameProfile().getName(), 98401);
        return ship;
    }

    @GameTest(template = "gameplayparitygametests.empty")
    public static void pointerClientOwnershipUsesSyncedUuidWithoutNumericOwnerCache(GameTestHelper helper) throws Exception {
        ServerPlayer player = player(helper);
        var data = TeitokuHelper.get(player).orElseThrow();
        data.clearCurrentTeam();
        var owned = ship(helper, player);
        owned.setVariantEggMeta(23);
        helper.getLevel().addFreshEntity(owned);
        owned.tickCount++;
        owned.tick();
        helper.assertTrue(data.addShipEntity(0, owned, false), "client snapshot requires a current-team ship");
        var mount = LegacyShipMountEntity.create(helper.getLevel(), owned);
        helper.assertTrue(mount != null, "client ownership requires a real mount host");
        helper.getLevel().addFreshEntity(mount);
        var ownerUid = LegacyShipEntity.class.getDeclaredField("ownerUid");
        ownerUid.setAccessible(true);
        int serverOwnerUid = owned.getOwnerUid();
        try {
            ownerUid.setInt(owned, 0);
            helper.assertTrue(owned.getOwnerUid() == 0 && owned.isOwnedBy(player),
                    "synced UUID must remain available without the server numeric owner cache");
            helper.assertTrue(PointerCommandEvents.consumesTargetClick(player, owned)
                            && PointerCommandEvents.consumesTargetClick(player, mount),
                    "normal client owned ship and mount clicks must consume with numeric owner zero");
            player.setShiftKeyDown(true);
            helper.assertTrue(PointerCommandEvents.consumesTargetClick(player, owned)
                            && PointerCommandEvents.consumesTargetClick(player, mount),
                    "client teammate sneak clicks must consume instead of switching mode");
            data.clearCurrentTeam();
            helper.assertTrue(!PointerCommandEvents.consumesTargetClick(player, owned),
                    "own non-team sneak clicks must retain legacy fall-through");
        } finally {
            ownerUid.setInt(owned, serverOwnerUid);
            player.setShiftKeyDown(false);
            data.clearCurrentTeam();
            mount.discard();
            owned.discard();
        }
        helper.succeed();
    }

    @GameTest(template = "gameplayparitygametests.empty")
    public static void pointerTargetConsumptionRetainsLegacyModifierFallthrough(GameTestHelper helper) {
        ServerPlayer player = player(helper);
        var data = TeitokuHelper.get(player).orElseThrow();
        data.clearCurrentTeam();
        var owned = ship(helper, player);
        helper.getLevel().addFreshEntity(owned);
        owned.tickCount++;
        owned.tick();
        var other = ship(helper, player);
        other.setOwner(UUID.randomUUID(), "Foreign", 98402);
        helper.getLevel().addFreshEntity(other);
        owned.setVariantEggMeta(23);
        var mount = LegacyShipMountEntity.create(helper.getLevel(), owned);
        helper.assertTrue(mount != null, "Airfield mount target must exist");
        helper.getLevel().addFreshEntity(mount);
        try {
            helper.assertTrue(PointerCommandEvents.consumesTargetClick(player, owned), "normal owned hit always consumes");
            helper.assertTrue(PointerCommandEvents.consumesTargetClick(player, mount), "normal owned mount hit consumes");
            helper.assertTrue(!PointerCommandEvents.consumesTargetClick(player, other), "foreign ship must fall through");
            helper.assertTrue(!PointerCommandEvents.consumesTargetClick(player, null), "empty ray must fall through");
            helper.assertTrue(PointerCommandEvents.consumesTargetClick(player, player), "non-ship targets retain SetTarClass consumption");
            player.setShiftKeyDown(true);
            player.setSprinting(true);
            helper.assertTrue(!PointerCommandEvents.consumesTargetClick(player, owned)
                            && !PointerCommandEvents.consumesTargetClick(player, mount),
                    "sneak+sprint on an own ship outside the team must reach clear-team logic");
            data.addShipEntity(0, owned, false);
            helper.assertTrue(PointerCommandEvents.consumesTargetClick(player, owned)
                            && PointerCommandEvents.consumesTargetClick(player, mount),
                    "sneak on a current-team ship or its mount consumes before clear-team logic");
            player.setSprinting(false);
            data.clearCurrentTeam();
            helper.assertTrue(!PointerCommandEvents.consumesTargetClick(player, owned),
                    "sneak on own non-team ship must reach mode-switch logic");
            helper.assertTrue(!PointerCommandEvents.consumesTargetClick(player, other), "sneaking foreign hits still fall through");
            helper.assertTrue(PointerCommandEvents.consumesTargetClick(player, player), "sneak non-ship still consumes for SetTarClass");
        } finally {
            player.setShiftKeyDown(false);
            player.setSprinting(false);
            mount.discard();
            owned.discard();
            other.discard();
        }
        helper.succeed();
    }

    @GameTest(template = "gameplayparitygametests.empty")
    public static void pointerLongRaySelectsOwnedTransportAndDispatchesToCrane(GameTestHelper helper) {
        ServerPlayer player = player(helper);
        LegacyShipEntity transport = ship(helper, player);
        transport.setVariantEggMeta(18);
        BlockPos origin = helper.absolutePos(new BlockPos(3, 150, 2));
        player.setPos(origin.getX() + 0.5D, origin.getY(), origin.getZ() + 0.5D);
        player.setYRot(0);
        player.setXRot(0);
        transport.setPos(player.getX(), player.getY(), player.getZ() + 20.0D);
        helper.getLevel().addFreshEntity(transport);
        transport.tickCount++;
        transport.tick();
        player.setXRot((float) Math.toDegrees(Math.atan2(
                player.getEyeY() - transport.getY() - transport.getBbHeight() * 0.5D, 20.0D)));
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(ModItems.POINTERITEM.get()));
        TeitokuData data = TeitokuHelper.get(player).orElseThrow();
        data.clearCurrentTeam();
        BlockPos crane = origin.offset(4, 0, 0);
        helper.getLevel().setBlockAndUpdate(crane, ModBlocks.BLOCK_CRANE.get().defaultBlockState());
        var order = ServerboundShipCommandPacket.guardBlock(0, -1, -1, crane);
        try {
            helper.assertTrue(!ShipCommandService.handle(player, order),
                    "an empty team must not dispatch a transport");
            var hit = PointerItem.pickCommandTarget(player);
            helper.assertTrue(hit != null && hit.getEntity() == transport && player.distanceToSqr(transport) > 9.0D,
                    "legacy long ray: eye=" + player.getEyePosition() + " look=" + player.getLookAngle()
                            + " box=" + transport.getBoundingBox() + " pick=" + transport.isPickable()
                            + " invuln=" + transport.isInvulnerable() + " tracked="
                            + (helper.getLevel().getEntity(transport.getId()) == transport) + " hit=" + hit);
            CompoundTag payload = new CompoundTag();
            payload.putInt(GameplayCommandHandler.TAG_TARGET_ID, hit.getEntity().getId());
            GameplayCommandHandler.handle(player, GameplayCommandType.POINTER_LEFT_CLICK, payload);
            helper.assertTrue(data.getShipUid(0, 0) == transport.getShipUid() && data.isShipSelected(0, 0),
                    "a single-mode long click must add and focus the owned transport on the server");
            helper.assertTrue(ShipCommandService.handle(player, order)
                            && crane.equals(transport.getRouteNodePos()),
                    "the selected transport must accept the actual crane route-node command");
            BlockPos wall = origin.offset(0, 1, 10);
            helper.getLevel().setBlockAndUpdate(wall, Blocks.STONE.defaultBlockState());
            helper.assertTrue(PointerItem.pickCommandTarget(player) == null,
                    "solid blocks must occlude the legacy long selection ray");
            helper.getLevel().setBlockAndUpdate(wall, Blocks.AIR.defaultBlockState());
            player.setYRot(90);
            helper.assertTrue(PointerItem.pickCommandTarget(player) == null, "an empty ray must not select a ship");
            data.clearCurrentTeam();
            payload.putInt(GameplayCommandHandler.TAG_TARGET_ID, -1);
            GameplayCommandHandler.handle(player, GameplayCommandType.POINTER_LEFT_CLICK, payload);
            transport.setOwner(UUID.randomUUID(), "Foreign", 98402);
            payload.putInt(GameplayCommandHandler.TAG_TARGET_ID, transport.getId());
            GameplayCommandHandler.handle(player, GameplayCommandType.POINTER_LEFT_CLICK, payload);
            helper.assertTrue(data.getShipUid(0, 0) <= 0, "missing and foreign ray targets must not join the fleet");
            transport.setOwner(player.getUUID(), player.getGameProfile().getName(), 98401);
            transport.setPos(player.getX(), player.getY(), player.getZ() + 70.0D);
            GameplayCommandHandler.handle(player, GameplayCommandType.POINTER_LEFT_CLICK, payload);
            helper.assertTrue(data.getShipUid(0, 0) <= 0, "a forged out-of-range ray target must not join the fleet");
            LegacyShipEntity host = ship(helper, player);
            host.setVariantEggMeta(23);
            host.setGrudge(50000);
            host.setPos(player.getX() + 5, player.getY(), player.getZ() + 20);
            helper.getLevel().addFreshEntity(host);
            host.tickCount++;
            host.tick();
            host.setModelState(1);
            var mount = LegacyShipMountEntity.create(helper.getLevel(), host);
            helper.assertTrue(mount != null, "owned Airfield Hime must create a pointer-targetable mount");
            helper.getLevel().addFreshEntity(mount);
            helper.assertTrue(host.startRiding(mount, true), "mount target must have its actual host passenger");
            try {
                payload.putInt(GameplayCommandHandler.TAG_TARGET_ID, mount.getId());
                GameplayCommandHandler.handle(player, GameplayCommandType.POINTER_LEFT_CLICK, payload);
                helper.assertTrue(data.getShipUid(0, 0) == host.getShipUid() && data.isShipSelected(0, 0),
                        "a pointer hit on an owned mount must focus its host ship");
            } finally {
                host.stopRiding();
                mount.discard();
                host.discard();
            }
            helper.succeed();
        } finally {
            transport.discard();
            helper.getLevel().setBlockAndUpdate(crane, Blocks.AIR.defaultBlockState());
        }
    }

    @GameTest(template = "gameplayparitygametests.empty")
    public static void zeroAcquisitionSelectionAndMigration(GameTestHelper helper) {
        ServerPlayer player = player(helper);
        TeitokuData data = TeitokuHelper.get(player).orElseThrow();
        LegacyShipEntity ship = ship(helper, player);
        helper.assertTrue(ship.getShipClassId() == 0, "Destroyer I has class zero");
        ItemStack wrench = new ItemStack(ModItems.TARGETWRENCH.get());
        helper.assertTrue(wrench.getItem().onLeftClickEntity(wrench, player, ship), "wrench acquires Destroyer I");
        helper.assertTrue(data.hasUnlockedMorph(0) && data.getSelectedMorphProfile() == null
                && data.getMorphRuntimeState().getSelectedClassId() == -1, "zero unlock does not auto-select");
        helper.assertTrue(!data.unlockMorph(0) && !data.unlockMorph(-1) && !data.setSelectedMorphProfile(999999),
                "duplicate, negative and unknown profiles are rejected");
        helper.assertTrue(data.setSelectedMorphProfile(0) && data.setMorphActive(true), "zero selects and activates");
        TeitokuData loaded = new TeitokuData();
        loaded.loadFromTag(data.saveToTag(new CompoundTag()));
        helper.assertTrue(loaded.getSelectedMorphProfile() != null && loaded.getSelectedMorphProfile().getLegacyClassId() == 0
                && loaded.getMorphRuntimeState().isActive(), "new save preserves active zero");
        loaded.setSelectedMorphProfile(-1);
        loaded.loadFromTag(loaded.saveToTag(new CompoundTag()));
        helper.assertTrue(loaded.getSelectedMorphProfile() == null && !loaded.getMorphRuntimeState().isActive(),
                "new save preserves no-selection with zero unlocked");
        CompoundTag old = data.saveToTag(new CompoundTag());
        old.getCompound("MorphRuntime").remove("SelectionVersion");
        loaded.loadFromTag(old);
        helper.assertTrue(loaded.getSelectedMorphProfile() == null && !loaded.getMorphRuntimeState().isActive(),
                "unversioned old zero stays empty, not implicitly activated");
        data.unlockMorph(56);
        data.setSelectedMorphProfile(56);
        old = data.saveToTag(new CompoundTag());
        old.getCompound("MorphRuntime").remove("SelectionVersion");
        loaded.loadFromTag(old);
        helper.assertTrue(loaded.getSelectedMorphProfile().getLegacyClassId() == 56, "old positive selection survives");
        ListTag malformed = new ListTag();
        malformed.add(new CompoundTag());
        old.put("MorphProfiles", malformed);
        old.remove("MorphRuntime");
        loaded.loadFromTag(old);
        helper.assertTrue(!loaded.hasUnlockedMorph(0) && loaded.getSelectedMorphProfile() == null,
                "missing class field cannot manufacture a zero profile");
        loaded.loadFromTag(new CompoundTag());
        helper.assertTrue(loaded.getMorphRuntimeState().getSelectedClassId() == -1, "empty old data stays empty");
        helper.succeed();
    }

    @GameTest(template = "gameplayparitygametests.empty")
    public static void currentTeamToggleHolesCursorAndOwnership(GameTestHelper helper) {
        ServerPlayer player = player(helper);
        TeitokuData data = TeitokuHelper.get(player).orElseThrow();
        LegacyShipEntity[] ships = new LegacyShipEntity[8];
        for (int i = 0; i < ships.length; i++) {
            ships[i] = ship(helper, player);
        }
        data.addShipEntity(5, ships[0], false);
        helper.assertTrue(data.getShipUid(0, 0) == ships[0].getShipUid() && data.getPointerSlotCursor() == 1,
                "add fills first hole, not requested slot");
        data.setCurrentTeamSelection(0, true);
        data.setCurrentTeamId(1);
        data.addShipEntity(0, ships[0], false);
        data.setCurrentTeamId(0);
        data.addShipEntity(0, ships[0], false);
        helper.assertTrue(data.getShipUid(0, 0) == -1 && !data.isShipSelected(0, 0) && data.getPointerSlotCursor() == 0
                && data.getShipUid(1, 0) == ships[0].getShipUid(), "toggle removes only current membership and moves cursor");
        for (int i = 0; i < 6; i++) {
            data.addShipEntity(0, ships[i], false);
            helper.assertTrue(data.getShipUid(0, i) == ships[i].getShipUid(), "first empty slots fill in order");
        }
        helper.assertTrue(data.getPointerSlotCursor() == 0, "six additions wrap cursor to zero");
        data.setCurrentTeamSelection(0, true);
        for (int i = 0; i < 6; i++) {
            data.addShipEntity(0, ships[(i + 6) % 7], false);
            helper.assertTrue(data.getPointerSlotCursor() == (i + 1) % 6, "full team cursor loops through six slots");
        }
        helper.assertTrue(data.getShipUid(0, 0) == ships[6].getShipUid() && !data.isShipSelected(0, 0),
                "full team replaces cursor slot and clears selection");
        ships[7].setOwner(UUID.randomUUID(), "OtherOwner", 98402);
        int first = data.getShipUid(0, 0);
        helper.assertTrue(!data.addShipEntity(0, ships[7], false) && data.getShipUid(0, 0) == first
                && data.getPointerSlotCursor() == 0, "foreign owner cannot change slots or cursor");
        data.addShipEntity(2, null, false);
        data.addShipEntity(0, ships[7], true);
        helper.assertTrue(data.getShipUid(0, 0) == ships[7].getShipUid() && data.getShipUid(0, 2) == -1,
                "null clears requested slot and forced sync bypasses owner validation");
        data.removeShipUidFromCurrentTeam(ships[0].getShipUid());
        helper.assertTrue(data.getShipUid(1, 0) == ships[0].getShipUid(), "current cleanup leaves other teams intact");
        data.setPointerSlotCursor(4);
        TeitokuData loaded = new TeitokuData();
        loaded.loadFromTag(data.saveToTag(new CompoundTag()));
        helper.assertTrue(loaded.getPointerSlotCursor() == 0 && loaded.getShipUid(1, 0) == ships[0].getShipUid(),
                "memberships persist but legacy runtime-only saveId does not");
        helper.succeed();
    }

    @GameTest(template = "gameplayparitygametests.empty")
    public static void pointerAndNetworkUseCurrentTeamSemantics(GameTestHelper helper) {
        ServerPlayer player = player(helper);
        TeitokuData data = TeitokuHelper.get(player).orElseThrow();
        LegacyShipEntity ship = ship(helper, player);
        LegacyShipEntity foreign = ship(helper, player);
        foreign.setOwner(UUID.randomUUID(), "Foreign", 98402);
        helper.getLevel().addFreshEntity(ship);
        helper.getLevel().addFreshEntity(foreign);
        try {
            ItemStack pointer = new ItemStack(ModItems.POINTERITEM.get());
            PointerItem.setMode(pointer, 1);
            player.setItemInHand(InteractionHand.MAIN_HAND, pointer);
            PointerCommandEvents.onAttackEntity(new AttackEntityEvent(player, ship));
            helper.assertTrue(data.getShipUid(0, 0) == ship.getShipUid(), "pointer adds own ship");
            PointerCommandEvents.onAttackEntity(new AttackEntityEvent(player, ship));
            helper.assertTrue(data.getShipUid(0, 0) == ship.getShipUid() && data.isShipSelected(0, 0),
                    "ordinary repeated click focuses, not membership toggle");
            data.setShipUid(1, 0, ship.getShipUid());
            player.setShiftKeyDown(true);
            PointerCommandEvents.onAttackEntity(new AttackEntityEvent(player, ship));
            player.setShiftKeyDown(false);
            helper.assertTrue(data.getShipUid(0, 0) == -1 && data.getShipUid(1, 0) == ship.getShipUid(),
                    "sneak click removes current membership only");
            CompoundTag payload = new CompoundTag();
            payload.putInt(GameplayCommandHandler.TAG_SHIP_ID, ship.getId());
            payload.putInt(GameplayCommandHandler.TAG_SLOT, 5);
            GameplayCommandHandler.handle(player, GameplayCommandType.ASSIGN_SHIP_SLOT, payload);
            helper.assertTrue(data.getShipUid(0, 0) == ship.getShipUid(), "network fills first hole, not supplied slot");
            payload.putInt(GameplayCommandHandler.TAG_SHIP_ID, foreign.getId());
            GameplayCommandHandler.handle(player, GameplayCommandType.ASSIGN_SHIP_SLOT, payload);
            helper.assertTrue(data.getShipUid(0, 0) == ship.getShipUid(), "foreign network entity cannot alter membership");
            payload.putInt(GameplayCommandHandler.TAG_SHIP_ID, ship.getId());
            GameplayCommandHandler.handle(player, GameplayCommandType.ASSIGN_SHIP_SLOT, payload);
            helper.assertTrue(data.getShipUid(0, 0) == -1 && data.getShipUid(1, 0) == ship.getShipUid(),
                    "network toggles only current team");
            data.setShipUid(0, 0, ship.getShipUid());
            payload.putInt(GameplayCommandHandler.TAG_SHIP_UID, -1);
            GameplayCommandHandler.handle(player, GameplayCommandType.ASSIGN_SHIP_SLOT, payload);
            helper.assertTrue(data.getShipUid(0, 0) == -1 && data.getShipUid(1, 0) == ship.getShipUid(),
                    "legacy empty AddTeam packet clears slot zero, not the supplied modern slot");
            data.setShipUid(0, 0, ship.getShipUid());
            ship.setOwner(UUID.randomUUID(), "Transferred", 98403);
            helper.assertTrue(data.getShipUid(0, 0) == ship.getShipUid() && data.getShipUid(1, 0) == ship.getShipUid(),
                    "owner setter does not eagerly erase all teams");
            TeitokuHelper.refreshPointerTeam(player, data, false);
            helper.assertTrue(data.getShipUid(0, 0) == -1 && data.getShipUid(1, 0) == ship.getShipUid(),
                    "periodic ownership refresh affects current team only");
        } finally {
            player.setShiftKeyDown(false);
            ship.discard();
            foreign.discard();
        }
        helper.succeed();
    }
}
