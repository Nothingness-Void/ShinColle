package com.lulan.shincolle.gametest;

import com.lulan.shincolle.entity.ship.LegacyShipAircraftEntity;
import com.lulan.shincolle.entity.ship.LegacyShipAttackKind;
import com.lulan.shincolle.entity.mount.LegacyShipMountEntity;
import com.lulan.shincolle.entity.projectile.LegacyShipFloatingFortEntity;
import com.lulan.shincolle.entity.ship.path.LegacyShipObstaclePath;
import com.lulan.shincolle.entity.ship.path.ShipPathFinder;
import com.lulan.shincolle.registry.ModEntityTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder("shincolle")
@PrefixGameTestTemplate(false)
public final class LegacyAircraftBlockCollisionGameTests {
    private LegacyAircraftBlockCollisionGameTests() {
    }

    @GameTest(template = "legacyaircraftblockcollisiongametests.flight_space")
    public static void allEightMountsActuallyTickPastIntermediateWaypoints(GameTestHelper helper) {
        BlockPos origin = helper.absolutePos(new BlockPos(4, 5, 4));
        for (int x = -2; x <= 2; x++) {
            for (int y = -1; y <= 4; y++) {
                helper.getLevel().setBlockAndUpdate(origin.offset(x, y, 3), Blocks.GLASS.defaultBlockState());
            }
        }
        for (int egg : new int[] {23, 30, 31, 32, 22, 28, 35, 46}) {
            var host = ModEntityTypes.LEGACY_SHIP.get().create(helper.getLevel());
            helper.assertTrue(host != null, "mount host must exist");
            host.setVariantEggMeta(egg);
            host.setModelState(1);
            host.setGrudge(50000);
            host.setNoAi(true);
            host.setNoGravity(true);
            host.setPos(origin.getX() - 3, origin.getY(), origin.getZ() - 2);
            helper.getLevel().addFreshEntity(host);
            var mount = LegacyShipMountEntity.create(helper.getLevel(), host);
            helper.assertTrue(mount != null, "each Hime must have a real factory mount");
            helper.getLevel().addFreshEntity(mount);
            helper.assertTrue(host.startRiding(mount, true), "actual mount tick requires its real host passenger");
            Vec3 start = new Vec3(origin.getX() + 0.5D, origin.getY(), origin.getZ() - 2.5D);
            mount.setPos(start);
            BlockPos destination = BlockPos.containing(start.add(0, 0, 15));
            host.commandMoveTo(destination, helper.getLevel().dimension().location().toString());
            try {
                for (int tick = 0; tick < 200 && mount.getZ() < origin.getZ() + 5; tick++) {
                    mount.tickCount++;
                    mount.tick();
                    helper.assertTrue(!mount.isRemoved() && helper.getLevel().noCollision(mount, mount.getBoundingBox()),
                            "actual mount movement must stay valid and outside glass: " + egg);
                }
                helper.assertTrue(mount.getZ() >= origin.getZ() + 5,
                        "mount.tick must not apply final arrival radius to an intermediate node: " + egg + " " + mount.position());
                Vec3 open = start.add(15, 0, 0);
                mount.setPos(open);
                host.commandMoveTo(BlockPos.containing(open.add(0, 0, 3)), helper.getLevel().dimension().location().toString());
                mount.tickCount++;
                mount.tick();
                helper.assertTrue(mount.getZ() > open.z, "unobstructed movement must remain enabled");
                host.stopRiding();
                mount.tick();
                helper.assertTrue(mount.isRemoved(), "orphaned mounts must still be discarded");
            } finally {
                mount.discard();
                host.discard();
            }
        }
        helper.succeed();
    }

    @GameTest(template = "legacyaircraftblockcollisiongametests.flight_space")
    public static void floatingFortAndAllEightMountStylesKeepSolidsAndObstacleRoutes(GameTestHelper helper) {
        BlockPos origin = helper.absolutePos(new BlockPos(4, 5, 4));
        for (int x = -2; x <= 2; x++) {
            for (int y = -1; y <= 4; y++) {
                helper.getLevel().setBlockAndUpdate(origin.offset(x, y, 3), Blocks.GLASS.defaultBlockState());
            }
        }
        var host = ModEntityTypes.LEGACY_SHIP.get().create(helper.getLevel());
        var target = ModEntityTypes.LEGACY_SHIP.get().create(helper.getLevel());
        helper.assertTrue(host != null && target != null, "D3 host and target fixtures must exist");
        host.setPos(origin.getX() - 4, origin.getY() - 4, origin.getZ() - 4);
        target.setPos(origin.getX() - 4, origin.getY() - 4, origin.getZ() - 5);
        helper.getLevel().addFreshEntity(host);
        helper.getLevel().addFreshEntity(target);
        for (int egg : new int[] {23, 30, 31, 32, 22, 28, 35, 46}) {
            host.setVariantEggMeta(egg);
            var mount = LegacyShipMountEntity.create(helper.getLevel(), host);
            helper.assertTrue(mount != null, "all eight Hime variants must create their mount: " + egg);
            helper.assertTrue(mount.getHostShip() == host, "mount collision fixture must resolve its real host profile");
            assertSolidsAndRoute(helper, origin, mount);
            mount.discard();
        }
        host.setVariantEggMeta(33);
        var fort = LegacyShipFloatingFortEntity.create(helper.getLevel(), host, target, origin.getY());
        helper.assertTrue(fort != null, "Northern Hime floating-fort fixture must exist");
        assertSolidsAndRoute(helper, origin, fort);
        fort.discard();
        host.discard();
        target.discard();
        helper.succeed();
    }

    private static void assertSolidsAndRoute(GameTestHelper helper, BlockPos origin, Entity flyer) {
        helper.assertTrue(!flyer.noPhysics, "flying entities must preserve block physics: " + flyer.getType());
        Vec3 start = new Vec3(origin.getX() + 0.5D, origin.getY(), origin.getZ() - 2.5D);
        flyer.setPos(start);
        flyer.move(MoverType.SELF, new Vec3(0, 0, 0.25D));
        helper.assertTrue(flyer.getZ() > start.z, "open movement must still work");
        flyer.move(MoverType.SELF, new Vec3(0, 0, 9));
        helper.assertTrue(flyer.getBoundingBox().maxZ <= origin.getZ() + 3.0D + 1.0E-7D,
                "glass must stop the entire bounding box: " + flyer.getType());
        flyer.setPos(start);
        var navigation = new LegacyShipObstaclePath();
        Vec3 destination = start.add(0, 0, 12);
        helper.assertTrue(helper.getLevel().noCollision(flyer, flyer.getBoundingBox().move(destination.subtract(start))),
                "flight fixture must contain the entire destination box, without framework barriers");
        Vec3 lastOffset = Vec3.ZERO;
        for (int tick = 0; tick < 160 && flyer.getZ() < origin.getZ() + 5; tick++) {
            flyer.tickCount++;
            Vec3 offset = navigation.resolve(flyer, destination).subtract(flyer.position());
            lastOffset = offset;
            flyer.move(MoverType.SELF, offset.normalize().scale(Math.min(0.3D, offset.length())));
            helper.assertTrue(helper.getLevel().noCollision(flyer, flyer.getBoundingBox()),
                    "obstacle route must never enter glass: " + flyer.getType());
        }
        helper.assertTrue(flyer.getZ() >= origin.getZ() + 5,
                "finite wall must have a usable solid-aware route: " + flyer.getType() + " " + flyer.position() + " -> " + lastOffset);
        Vec3 open = new Vec3(origin.getX() + 15, origin.getY(), origin.getZ() - 2);
        flyer.setPos(open);
        flyer.horizontalCollision = false;
        flyer.verticalCollision = false;
        helper.assertTrue(new LegacyShipObstaclePath().resolve(flyer, open.add(1, 0, 0)).equals(open.add(1, 0, 0)),
                "an empty ray must retain the unmodified destination");
    }

    @GameTest(template = "gameplayparitygametests.empty")
    public static void lightAndHeavyAircraftKeepBlockCollisionAndLegacyAirPathObstacles(GameTestHelper helper) {
        BlockPos origin = helper.absolutePos(new BlockPos(3, 4, 3));
        var owner = ModEntityTypes.LEGACY_SHIP.get().create(helper.getLevel());
        var target = ModEntityTypes.LEGACY_SHIP.get().create(helper.getLevel());
        helper.assertTrue(owner != null && target != null, "aircraft owner and target must exist");
        owner.setVariantEggMeta(50);
        owner.setPos(origin.getX() + 0.5D, origin.getY(), origin.getZ() - 3.5D);
        target.setPos(origin.getX() + 0.5D, origin.getY(), origin.getZ() + 10.5D);
        helper.getLevel().addFreshEntity(owner);
        helper.getLevel().addFreshEntity(target);
        for (int x = -1; x <= 1; x++) {
            for (int y = -1; y <= 2; y++) {
                helper.getLevel().setBlockAndUpdate(origin.offset(x, y, 2), Blocks.GLASS.defaultBlockState());
            }
        }
        for (var kind : new LegacyShipAttackKind[] {LegacyShipAttackKind.AIR_LIGHT, LegacyShipAttackKind.AIR_HEAVY}) {
            var aircraft = LegacyShipAircraftEntity.create(helper.getLevel(), owner, target, kind);
            helper.assertTrue(aircraft != null && !aircraft.noPhysics && !aircraft.canCollideWith(owner),
                    "aircraft must disable entity collision only, not block physics: " + kind);
            aircraft.setPos(origin.getX() + 0.5D, origin.getY(), origin.getZ() + 0.5D);
            double startZ = aircraft.getZ();
            aircraft.move(MoverType.SELF, new Vec3(0.0D, 0.0D, 0.25D));
            helper.assertTrue(aircraft.getZ() > startZ, "open-air movement must remain enabled");
            aircraft.move(MoverType.SELF, new Vec3(0.0D, 0.0D, 4.0D));
            helper.assertTrue(aircraft.getBoundingBox().maxZ <= origin.getZ() + 2.0D + 1.0E-7D,
                    "glass wall must stop the swept aircraft bounding box: " + kind);
            var path = new ShipPathFinder(helper.getLevel(), true).findPath(aircraft,
                    origin.getX(), origin.getY(), origin.getZ() + 6, 32.0F);
            helper.assertTrue(path != null && path.getFinalPathPoint().zCoord > origin.getZ() + 2,
                    "legacy aerial pathfinder must find a path beyond a finite glass obstacle");
            for (int index = 0; index < path.getCurrentPathLength(); index++) {
                var point = path.getPathPointFromIndex(index);
                var pos = new BlockPos(point.xCoord, point.yCoord, point.zCoord);
                helper.assertTrue(!helper.getLevel().getBlockState(pos).is(Blocks.GLASS),
                        "aircraft route must not classify a solid glass cell as passable");
            }
            aircraft.discard();
        }
        owner.discard();
        target.discard();
        helper.succeed();
    }
}
