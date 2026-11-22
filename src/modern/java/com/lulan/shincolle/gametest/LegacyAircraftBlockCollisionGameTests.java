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
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

@GameTestHolder("shincolle")
@PrefixGameTestTemplate(false)
public final class LegacyAircraftBlockCollisionGameTests {
    private LegacyAircraftBlockCollisionGameTests() {
    }

    @GameTest(template = "gameplayparitygametests.empty")
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
        for (int egg : new int[] {23, 30, 31, 32, 22, 28, 35, 46}) {
            host.setVariantEggMeta(egg);
            var mount = LegacyShipMountEntity.create(helper.getLevel(), host);
            helper.assertTrue(mount != null, "all eight Hime variants must create their mount: " + egg);
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
        StringBuilder trace = new StringBuilder();
        for (int tick = 0; tick < 160 && flyer.getZ() < origin.getZ() + 5; tick++) {
            flyer.tickCount++;
            Vec3 offset = navigation.resolve(flyer, destination).subtract(flyer.position());
            if (tick >= 144) trace.append(" ").append(flyer.position()).append(" -> ").append(offset);
            flyer.move(MoverType.SELF, offset.normalize().scale(Math.min(0.3D, offset.length())));
            helper.assertTrue(helper.getLevel().noCollision(flyer, flyer.getBoundingBox()),
                    "obstacle route must never enter glass: " + flyer.getType());
        }
        helper.assertTrue(flyer.getZ() >= origin.getZ() + 5,
                "finite wall must have a usable solid-aware route: " + flyer.getType() + trace);
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
