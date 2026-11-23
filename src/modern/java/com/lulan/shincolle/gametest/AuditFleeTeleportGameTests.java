package com.lulan.shincolle.gametest;

import com.lulan.shincolle.config.ShinColleConfig;
import com.lulan.shincolle.entity.ship.LegacyShipEntity;
import com.lulan.shincolle.entity.ship.ShipEntitySpecs;
import com.lulan.shincolle.entity.ship.goal.LegacyShipFleeGoal;
import com.lulan.shincolle.entity.ship.path.ShipPathNavigate;
import com.lulan.shincolle.registry.ModEntityTypes;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder("shincolle")
@PrefixGameTestTemplate(false)
public final class AuditFleeTeleportGameTests {
    @GameTest(template = "legacyaircraftblockcollisiongametests.flight_space", batch = "audit_flee")
    public static void failedFleeUsesExactEscortPositionAndConfigGate(GameTestHelper helper) {
        var ship = new LegacyShipEntity(ModEntityTypes.LEGACY_SHIP.get(), helper.getLevel()) {
            private final ShipPathNavigate blocked = new ShipPathNavigate(this) {
                @Override public boolean tryMoveToEntityLiving(Entity target, double speed) { return false; }
            };
            @Override public ShipPathNavigate getShipNavigate() {
                return this.blocked == null ? super.getShipNavigate() : this.blocked;
            }
        };
        var escort = EntityType.ZOMBIE.create(helper.getLevel());
        escort.setNoAi(true);
        escort.setPos(helper.absoluteVec(new Vec3(20.25D, 5.0D, 5.75D)));
        helper.getLevel().addFreshEntity(escort);
        try {
            ship.applySpawnSpec(ShipEntitySpecs.getByEggMeta(2), null);
            ship.setPos(helper.absoluteVec(new Vec3(2, 5, 5)));
            ship.setGrudge(100);
            ship.setFleeHp(50);
            ship.setHealth(1.0F);
            ship.commandGuardEntity(escort.getUUID());
            LegacyShipFleeGoal goal = new LegacyShipFleeGoal(ship);
            helper.assertTrue(goal.canUse(), "low-health fueled ship resolves its escort");
            Vec3 original = ship.position();
            ShinColleConfig.setTestOverride("canTeleport", false);
            goal.start();
            goal.tick();
            helper.assertTrue(ship.position().equals(original), "disabled teleport must not move");
            ShinColleConfig.setTestOverride("canTeleport", true);
            goal.start();
            goal.tick();
            helper.assertTrue(ship.position().distanceToSqr(escort.position().add(0, 0.5D, 0)) < 0.00001D,
                    "failed flee teleports to escort X/Z and Y+0.5 even without supporting terrain");
        } finally {
            ShinColleConfig.clearTestOverride("canTeleport");
            ship.discard();
            escort.discard();
        }
        helper.succeed();
    }
}
