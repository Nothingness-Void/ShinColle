package com.lulan.shincolle.gametest;

import com.lulan.shincolle.entity.ship.LegacyShipEntity;
import com.lulan.shincolle.entity.ship.ShipEntitySpecs;
import com.lulan.shincolle.entity.ship.goal.LegacyShipFloatGoal;
import com.lulan.shincolle.network.ServerboundShipCommandPacket;
import com.lulan.shincolle.network.ShipCommandService;
import com.lulan.shincolle.registry.ModEntityTypes;
import com.lulan.shincolle.teitoku.TeitokuHelper;
import com.lulan.shincolle.teitoku.TeitokuSavedData;
import com.mojang.authlib.GameProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.UUID;

@GameTestHolder("shincolle")
@PrefixGameTestTemplate(false)
public final class PointerWaterMoveGameTests {
    private PointerWaterMoveGameTests() {}

    @GameTest(template = "legacyaircraftblockcollisiongametests.flight_space")
    public static void sixWaterShipsStartPointerMoveAndArrive(GameTestHelper helper) throws Exception {
        moveFleet(helper, true, false);
        helper.succeed();
    }

    @GameTest(template = "legacyaircraftblockcollisiongametests.flight_space")
    public static void sixLandShipsStartPointerMoveAndArrive(GameTestHelper helper) throws Exception {
        moveFleet(helper, false, false);
        helper.succeed();
    }

    @GameTest(template = "legacyaircraftblockcollisiongametests.flight_space")
    public static void legacyMutexOracleWaterFleetArrives(GameTestHelper helper) throws Exception {
        moveFleet(helper, true, true);
        helper.succeed();
    }

    @GameTest(template = "gameplayparitygametests.empty")
    public static void floatingMutexMatchesLegacyWithoutClaimingGuardMovement(GameTestHelper helper) {
        var ship = ModEntityTypes.LEGACY_SHIP.get().create(helper.getLevel());
        var floating = new LegacyShipFloatGoal(ship);
        helper.assertTrue(floating.getFlags().equals(EnumSet.of(Goal.Flag.TARGET)),
                "legacy floating mutex 8 must remain independent from guard mutex 7");
        helper.assertTrue(!floating.canUse(), "dry idle ships must not float");
        ship.setOrderedToSit(true);
        helper.assertTrue(!floating.canUse(), "sitting must not activate floating or movement");
        helper.succeed();
    }

    private static void moveFleet(GameTestHelper helper, boolean water, boolean legacyMutex) throws Exception {
        for (int x = 1; x < 25; x++) for (int z = 1; z < 23; z++) {
            helper.setBlock(x, 0, z, Blocks.STONE);
            for (int y = 1; y < 5; y++) helper.setBlock(x, y, z, water ? Blocks.WATER : Blocks.STONE);
        }
        var owner = FakePlayerFactory.get(helper.getLevel(), new GameProfile(UUID.randomUUID(), "WaterMove"));
        owner.setPos(helper.absoluteVec(new Vec3(12.5, 6, 2.5)));
        var data = TeitokuHelper.get(owner).orElseThrow(IllegalStateException::new);
        data.setPlayerUid(TeitokuSavedData.get(helper.getLevel()).getOrCreateUid(owner));
        data.setFormationId(0);
        LegacyShipEntity[] ships = new LegacyShipEntity[6];
        boolean[] started = new boolean[6];
        boolean[] arrived = new boolean[6];
        int[] eggs = {53, 54, 55, 56, 38, 39};
        var server = helper.getLevel().getServer();
        var clock = MinecraftServer.class.getDeclaredField("tickCount");
        clock.setAccessible(true);
        int originalClock = clock.getInt(server);
        try {
            for (int slot = 0; slot < ships.length; slot++) {
                var ship = new LegacyShipEntity(ModEntityTypes.LEGACY_SHIP.get(), helper.getLevel()) {
                    @Override
                    protected void registerGoals() {
                        super.registerGoals();
                        if (legacyMutex) this.goalSelector.getAvailableGoals().stream()
                                .filter(goal -> goal.getGoal() instanceof LegacyShipFloatGoal)
                                .forEach(goal -> goal.getGoal().setFlags(EnumSet.of(Goal.Flag.TARGET)));
                    }
                };
                ships[slot] = ship;
                ship.applySpawnSpec(ShipEntitySpecs.getByEggMeta(eggs[slot]), owner);
                ship.setAiPickItem(false);
                ship.setGrudge(1000);
                ship.setPos(helper.absoluteVec(new Vec3(3.5 + slot * 3, water ? 4.9 - slot * 0.3 : 5, 3.5)));
                helper.getLevel().addFreshEntity(ship);
                ship.tickCount = 41;
                ship.tick();
                data.assignCurrentTeamSlot(slot, ship.getShipUid());
            }
            BlockPos target = helper.absolutePos(new BlockPos(12, water ? 4 : 5, 20));
            BlockPos surface = helper.absolutePos(new BlockPos(12, 4, 20));
            double surfaceY = surface.getY() + (water
                    ? helper.getLevel().getFluidState(surface).getHeight(helper.getLevel(), surface) : 1.0D);
            Vec3 eye = owner.getEyePosition();
            Vec3 direction = new Vec3(surface.getX() + 0.5D, surfaceY - 0.001D, surface.getZ() + 0.5D)
                    .subtract(eye).normalize();
            var hit = helper.getLevel().clip(new ClipContext(eye, eye.add(direction.scale(64.0D)),
                    ClipContext.Block.OUTLINE, ClipContext.Fluid.ANY, owner));
            helper.assertTrue(hit.getType() == HitResult.Type.BLOCK, "Pointer ray must hit the fixture surface");
            BlockPos picked = hit.getBlockPos();
            if (helper.getLevel().getFluidState(picked).isEmpty()) picked = picked.relative(hit.getDirection());
            helper.assertTrue(picked.equals(target),
                    "water ray must retain the surface fluid block, while land is offset above the floor");
            helper.assertTrue(ShipCommandService.handleResultForTesting(owner,
                            ServerboundShipCommandPacket.moveTo(1, -1, -1, picked))
                            == ShipCommandService.ShipCommandResult.NO_COMMANDABLE_SHIPS,
                    "an empty selection must not silently issue a group move");
            helper.assertTrue(ShipCommandService.handleForTesting(owner,
                            ServerboundShipCommandPacket.moveTo(2, -1, -1, picked)),
                    "Pointer fleet move must dispatch successfully");
            for (var ship : ships) {
                helper.assertTrue(target.equals(ship.getCommandedPos()) && ship.canExecuteGuardGoal(),
                        "every selected slot must receive an executable target, not only the flagship");
            }
            for (int tick = 0; tick < 384; tick++) {
                // 1.20 Mob schedules goal starts by server tick plus entity ID.
                clock.setInt(server, originalClock + tick);
                for (int slot = 0; slot < ships.length; slot++) {
                    var ship = ships[slot];
                    ship.tickCount++;
                    ship.tick();
                    if (tick < 32 && !ship.getShipNavigate().noPath()) started[slot] = true;
                    double tolerance = ship.getFollowMax() + ship.getBbWidth() * 0.75D;
                    if (ship.position().distanceToSqr(Vec3.atCenterOf(target)) <= tolerance * tolerance) {
                        arrived[slot] = true;
                    }
                }
            }
            StringBuilder details = new StringBuilder();
            for (int slot = 0; slot < ships.length; slot++) {
                var ship = ships[slot];
                details.append(" slot=").append(slot).append(" pos=").append(ship.position())
                        .append(" depth=").append(ship.getLegacyLiquidDepth())
                        .append(" float=").append(ship.canUseLegacyFloatingGoal())
                        .append(" path=").append(!ship.getShipNavigate().noPath());
            }
            System.out.println("Pointer water=" + water + " legacyMutex=" + legacyMutex
                    + " started=" + Arrays.toString(started) + " arrived=" + Arrays.toString(arrived) + details);
            helper.assertTrue(Arrays.equals(arrived, new boolean[]{true, true, true, true, true, true}),
                    "all ships must reach legacy guard tolerance: water=" + water + " arrived=" + Arrays.toString(arrived));
            helper.assertTrue(Arrays.equals(started, new boolean[]{true, true, true, true, true, true}),
                    "floating must not monopolize movement before guard can create a path: water=" + water
                            + " started=" + Arrays.toString(started));
        } finally {
            clock.setInt(server, originalClock);
            for (var ship : ships) if (ship != null) ship.discard();
        }
    }
}
