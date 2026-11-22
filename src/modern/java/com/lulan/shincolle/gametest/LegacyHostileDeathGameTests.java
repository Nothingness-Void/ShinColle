package com.lulan.shincolle.gametest;

import com.lulan.shincolle.config.ShinColleConfig;
import com.lulan.shincolle.entity.ship.ShipEntitySpecs;
import com.lulan.shincolle.item.LegacyShipSpawnEggItem;
import com.lulan.shincolle.registry.ModEntityTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

@GameTestHolder("shincolle")
@PrefixGameTestTemplate(false)
public final class LegacyHostileDeathGameTests {
    private LegacyHostileDeathGameTests() {
    }

    @GameTest(template = "gameplayparitygametests.empty", batch = "guard_hostile_death")
    public static void hostileEggWaitsForDeathAnimationDeadlineAndRespectsDropGates(GameTestHelper helper) {
        var rule = helper.getLevel().getGameRules().getRule(GameRules.RULE_DOMOBLOOT);
        boolean oldLoot = rule.get();
        try {
            helper.assertTrue(ShinColleConfig.deathMaxTick() == 400, "legacy default death animation is 400 ticks");
            for (int deadline : new int[] {400, 4, 0}) {
                for (int gate = 0; gate < 3; gate++) {
                    ShinColleConfig.setTestOverride("deathMaxTick", deadline);
                    rule.set(gate != 2, helper.getLevel().getServer());
                    var ship = ModEntityTypes.LEGACY_SHIP.get().create(helper.getLevel());
                    helper.assertTrue(ship != null, "hostile death fixture must be creatable");
                    ship.applySpawnSpec(ShipEntitySpecs.getByEggMeta(2053), null);
                    ship.initializeHostileRuntime(false, 3);
                    ship.setCanDropItem(gate != 1);
                    ship.setNoGravity(true);
                    ship.setNoAi(true);
                    ship.moveTo(Vec3.atCenterOf(helper.absolutePos(new BlockPos(16, 160, 16))));
                    helper.getLevel().addFreshEntity(ship);
                    ship.setHealth(0.0F);
                    ship.die(ship.damageSources().genericKill());
                    java.util.function.Supplier<java.util.List<ItemEntity>> eggs = () -> helper.getLevel().getEntitiesOfClass(
                            ItemEntity.class, ship.getBoundingBox().inflate(2),
                            item -> item.getItem().getItem() instanceof LegacyShipSpawnEggItem);
                    try {
                        helper.assertTrue(eggs.get().isEmpty() && ship.deathTime == 0,
                                "lethal death dispatch without advancing ticks must not drop a hostile egg");
                        for (int tick = 1; tick < deadline; tick++) {
                            ship.tick();
                            helper.assertTrue(!ship.isRemoved() && eggs.get().isEmpty(),
                                    "hostile corpse and egg deadline must survive tick " + tick + "/" + deadline);
                        }
                        ship.tick();
                        int expected = deadline > 0 && gate == 0 ? 1 : 0;
                        helper.assertTrue(ship.isRemoved() && eggs.get().size() == expected,
                                "deadline must drop once only with CanDrop and doMobLoot; Death_Time=0 skips egg like legacy");
                        ship.tick();
                        helper.assertTrue(eggs.get().size() == expected, "later death updates must not duplicate the egg");
                    } finally {
                        eggs.get().forEach(ItemEntity::discard);
                        ship.discard();
                    }
                }
            }
            helper.succeed();
        } finally {
            rule.set(oldLoot, helper.getLevel().getServer());
            ShinColleConfig.clearTestOverride("deathMaxTick");
        }
    }
}
