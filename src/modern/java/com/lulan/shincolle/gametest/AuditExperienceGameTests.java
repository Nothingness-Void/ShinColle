package com.lulan.shincolle.gametest;

import com.lulan.shincolle.entity.ship.LegacyShipEntity;
import com.lulan.shincolle.entity.ship.LegacyShipStatTables;
import com.lulan.shincolle.entity.ship.ShipEntitySpecs;
import com.lulan.shincolle.registry.ModEntityTypes;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder("shincolle")
@PrefixGameTestTemplate(false)
public final class AuditExperienceGameTests {
    @GameTest(template = "gameplayparitygametests.empty", batch = "audit_parity")
    public static void oneAwardLevelsOnlyOnceAndKeepsCarry(GameTestHelper helper) {
        LegacyShipEntity ship = new LegacyShipEntity(ModEntityTypes.LEGACY_SHIP.get(), helper.getLevel());
        ship.applySpawnSpec(ShipEntitySpecs.getByEggMeta(2), null);
        ship.setShipLevel(1);
        ship.setHealth(1.0F);
        int firstGain = (int) (1000 * ship.getLegacyStats().get(LegacyShipStatTables.Attr.XP));
        ship.addShipExperience(1000);
        helper.assertTrue(ship.getShipLevel() == 2, "one award must advance exactly one level");
        helper.assertTrue(ship.getShipExperience() == firstGain - 40, "only threshold40 is consumed after XP multiplier");
        helper.assertTrue(ship.getHealth() == ship.getMaxHealth(), "level-up restores full health");
        int nextGain = (int) ship.getLegacyStats().get(LegacyShipStatTables.Attr.XP);
        ship.addShipExperience(1);
        helper.assertTrue(ship.getShipLevel() == 3 && ship.getShipExperience() == firstGain - 40 + nextGain - 60,
                "a later award spends exactly one more threshold60");
        ship.setShipExperience(0);
        ship.addShipExperience(0);
        helper.assertTrue(ship.getShipLevel() == 3 && ship.getShipExperience() == 0, "zero XP does not level");
        ship.setShipLevel(100);
        ship.addShipExperience(10000);
        helper.assertTrue(ship.getShipLevel() == 100 && ship.getShipExperience() == 0, "cap ignores XP");
        ship.discard();
        helper.succeed();
    }
}
