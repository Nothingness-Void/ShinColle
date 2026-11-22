package com.lulan.shincolle.gametest;

import com.lulan.shincolle.config.ShinColleConfig;
import com.lulan.shincolle.entity.ship.LegacyShipStatTables;
import com.lulan.shincolle.entity.ship.LegacyShipStats;
import com.lulan.shincolle.entity.ship.ShipArchetype;
import com.lulan.shincolle.entity.ship.ShipEquipmentProfile;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.Arrays;
import java.util.List;

@GameTestHolder("shincolle")
@PrefixGameTestTemplate(false)
public final class LegacyAttributeScaleGameTests {
    private LegacyAttributeScaleGameTests() {
    }

    @GameTest(template = "gameplayparitygametests.empty")
    public static void attributeConfigChangesFriendlyAndEveryHostileScaleAndFallsBackOnMalformedLists(GameTestHelper helper) {
        try {
            helper.assertTrue(Arrays.equals(ShinColleConfig.shipScale(), LegacyShipStatTables.SCALE_SHIP), "friendly defaults match legacy");
            var friendly = stats(false, -1);
            ShinColleConfig.setTestOverride("attrsScale", new double[] {2, 1, 1, 1, 1, 1});
            helper.assertTrue(stats(false, -1).get(LegacyShipStatTables.Attr.HP)
                            > friendly.get(LegacyShipStatTables.Attr.HP),
                    "friendly Attrs_Scale HP must reach the computed stats");
            ShinColleConfig.clearTestOverride("attrsScale");
            String[] keys = {"attrsHostileSmallMob", "attrsHostileLargeMob", "attrsHostileSmallBoss", "attrsHostileLargeBoss"};
            double[][] defaults = {LegacyShipStatTables.SCALE_MOB_SMALL, LegacyShipStatTables.SCALE_MOB_LARGE,
                    LegacyShipStatTables.SCALE_BOSS_SMALL, LegacyShipStatTables.SCALE_BOSS_LARGE};
            for (int band = 0; band < keys.length; band++) {
                helper.assertTrue(Arrays.equals(LegacyShipStatTables.hostileScaleFor(ShipArchetype.DESTROYER, band), defaults[band]),
                        "hostile default six-value scale must match ConfigHandler: " + band);
                float baseline = stats(true, band).get(LegacyShipStatTables.Attr.HP);
                double[] changed = defaults[band].clone();
                changed[0] *= 2;
                ShinColleConfig.setTestOverride(keys[band], changed);
                helper.assertTrue(stats(true, band).get(LegacyShipStatTables.Attr.HP) > baseline,
                        "configured hostile HP must reach actual stat computation: " + band);
                ShinColleConfig.clearTestOverride(keys[band]);
            }
            for (Object malformed : List.of(List.of(), List.of(1, 2), List.of("invalid", 1, 1, 1, 1, 1))) {
                ShinColleConfig.setTestOverride("attrsScale", malformed);
                helper.assertTrue(Arrays.equals(ShinColleConfig.shipScale(), LegacyShipStatTables.SCALE_SHIP),
                        "empty, wrong-length and nonnumeric entries retain legacy defaults");
            }
        } finally {
            ShinColleConfig.clearTestOverrides();
        }
        helper.succeed();
    }

    private static LegacyShipStats stats(boolean hostile, int band) {
        return LegacyShipStats.create(0, ShipArchetype.DESTROYER, hostile, 1, 3000,
                0, 0, 0, 0, 0, 0, false, 0, ShipEquipmentProfile.EMPTY, List.of(), false, band);
    }
}
