package com.lulan.shincolle.gametest;

import com.lulan.shincolle.entity.ship.LegacyShipEntity;
import com.lulan.shincolle.entity.ship.ShipEntitySpecs;
import com.lulan.shincolle.registry.ModEntityTypes;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder("shincolle")
@PrefixGameTestTemplate(false)
public final class AuditExperienceGameTests {
    @GameTest(template = "gameplayparitygametests.empty", batch = "audit_experience")
    public static void oneAwardLevelsOnlyOnceAndKeepsCarry(GameTestHelper helper) {
        LegacyShipEntity ship = new LegacyShipEntity(ModEntityTypes.LEGACY_SHIP.get(), helper.getLevel());
        ship.applySpawnSpec(ShipEntitySpecs.getByEggMeta(2), null);
        ship.setShipLevel(1);
        ship.setHealth(1.0F);
        ship.addShipExperience(1000);
        helper.assertTrue(ship.getShipLevel() == 2, "one award must advance exactly one level");
        helper.assertTrue(ship.getShipExperience() == 960, "only the level1 threshold40 is consumed");
        helper.assertTrue(ship.getHealth() == ship.getMaxHealth(), "level-up restores full health");
        ship.addShipExperience(1);
        helper.assertTrue(ship.getShipLevel() == 3 && ship.getShipExperience() == 901,
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
