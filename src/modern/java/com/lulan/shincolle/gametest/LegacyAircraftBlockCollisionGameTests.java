package com.lulan.shincolle.gametest;

import com.lulan.shincolle.entity.ship.LegacyShipAircraftEntity;
import com.lulan.shincolle.entity.ship.LegacyShipAttackKind;
import com.lulan.shincolle.entity.ship.path.ShipPathFinder;
import com.lulan.shincolle.registry.ModEntityTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.MoverType;
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
