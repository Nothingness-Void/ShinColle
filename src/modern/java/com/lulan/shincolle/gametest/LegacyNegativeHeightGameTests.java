package com.lulan.shincolle.gametest;

import com.lulan.shincolle.entity.ship.LegacyShipEntity;
import com.lulan.shincolle.entity.ship.path.ShipNavigationHost;
import com.lulan.shincolle.registry.ModEntityTypes;
import com.lulan.shincolle.menu.DeskRadarVisibility;
import com.lulan.shincolle.teitoku.TeitokuEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.LinkedHashMap;
import java.util.Map;

@GameTestHolder("shincolle")
@PrefixGameTestTemplate(false)
public final class LegacyNegativeHeightGameTests {
    private LegacyNegativeHeightGameTests() {
    }

    @GameTest(template = "gameplayparitygametests.empty")
    public static void radarHeightGateUsesBuildMinimumAndRejectsMissingEntities(GameTestHelper helper) {
        int minimum = helper.getLevel().getMinBuildHeight();
        helper.assertTrue(!DeskRadarVisibility.isVisible(null, minimum), "missing entity is not a radar position");
        var ship = ModEntityTypes.LEGACY_SHIP.get().create(helper.getLevel());
        helper.assertTrue(ship != null, "radar fixture must exist");
        for (double y : new double[] {minimum - 0.1D, minimum, minimum + 0.1D, -60.1D, 0.0D, 65.0D,
                helper.getLevel().getMaxBuildHeight() + 1.0D}) {
            ship.setPos(0.5D, y, 0.5D);
            helper.assertTrue(DeskRadarVisibility.isVisible(ship, minimum) == (y > minimum),
                    "radar must preserve the strict legacy lower gate relative to build minimum; Y=" + y);
        }
        ship.discard();
        helper.succeed();
    }

    @GameTest(template = "gameplayparitygametests.empty")
    public static void waterDepthAndBuoyancyAreTranslationInvariantBelowZero(GameTestHelper helper) {
        for (int surface : new int[] {65, -60}) {
            for (double coordinate : new double[] {3200.5D, -3200.5D}) {
                for (double submersion : new double[] {0.1D, 0.8D}) {
                    BlockPos column = BlockPos.containing(coordinate, surface, coordinate);
                    Map<BlockPos, BlockState> original = new LinkedHashMap<>();
                    LegacyShipEntity ship = ModEntityTypes.LEGACY_SHIP.get().create(helper.getLevel());
                    helper.assertTrue(ship != null, "water fixture ship must exist");
                    try {
                        for (int dy = -3; dy <= 2; dy++) {
                            BlockPos pos = column.offset(0, dy, 0);
                            original.put(pos, helper.getLevel().getBlockState(pos));
                            helper.getLevel().setBlock(pos, dy < 0 ? Blocks.WATER.defaultBlockState()
                                    : Blocks.AIR.defaultBlockState(), Block.UPDATE_CLIENTS);
                        }
                        ship.setVariantEggMeta(53);
                        ship.setNoAi(true);
                        ship.setNoGravity(true);
                        ship.setPos(coordinate, surface - submersion, coordinate);
                        helper.getLevel().addFreshEntity(ship);
                        ship.tick();
                        String context = " surface=" + surface + " X/Z=" + coordinate + " depth=" + submersion;
                        helper.assertTrue(Math.abs(ship.getLegacyLiquidDepth() - submersion) < 1.0E-7D,
                                "real ship tick must sample the same fractional depth" + context);
                        helper.assertTrue(ship.canFloatUp() && ShipNavigationHost.isInLiquid(ship)
                                        && TeitokuEvents.isInLegacyLiquid(ship),
                                "surface air and both feet-block liquid gates must agree" + context);
                        ship.setDeltaMovement(Vec3.ZERO);
                        ship.applyLegacyFloatingMotion();
                        double expectedLift = submersion > 0.47D ? 0.003D : 0.0D;
                        helper.assertTrue(Math.abs(ship.getDeltaMovement().y - expectedLift) < 1.0E-9D,
                                "legacy buoyancy bands must be invariant under world-height translation" + context);
                        ship.setPos(coordinate, surface + 0.1D, coordinate);
                        ship.setDeltaMovement(Vec3.ZERO);
                        ship.tick();
                        helper.assertTrue(ship.getLegacyLiquidDepth() == 0.0D && !ship.canFloatUp()
                                        && !ShipNavigationHost.isInLiquid(ship) && !TeitokuEvents.isInLegacyLiquid(ship),
                                "dry negative and positive feet blocks must not sample the water below" + context);
                    } finally {
                        if (ship != null) ship.discard();
                        original.forEach((pos, state) -> helper.getLevel().setBlock(pos, state, Block.UPDATE_CLIENTS));
                    }
                }
            }
        }
        helper.succeed();
    }
}
