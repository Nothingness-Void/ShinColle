package com.lulan.shincolle.gametest;

import com.lulan.shincolle.config.ShinColleConfig;
import com.lulan.shincolle.entity.ship.HostileRuntimeState;
import com.lulan.shincolle.entity.ship.LegacyShipEntity;
import com.lulan.shincolle.entity.ship.LegacyShipHostileDespawn;
import com.lulan.shincolle.entity.ship.ShipEntitySpecs;
import com.lulan.shincolle.registry.ModEntityTypes;
import com.mojang.authlib.GameProfile;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;
import java.util.UUID;

@GameTestHolder("shincolle")
@PrefixGameTestTemplate(false)
public final class AuditHostileDespawnGameTests {
    @GameTest(template = "gameplayparitygametests.empty", batch = "audit_hostile_despawn")
    public static void eggAndNaturalShareAgeGateAndExplicitPersistence(GameTestHelper helper) throws Exception {
        var player = FakePlayerFactory.get(helper.getLevel(), new GameProfile(UUID.randomUUID(), "AuditDespawn"));
        player.setPos(helper.absoluteVec(new Vec3(2, 2, 2)));
        helper.getLevel().addNewPlayer(player);
        var tick = LegacyShipEntity.class.getDeclaredMethod("tickHostileRuntime");
        tick.setAccessible(true);
        try {
            for (boolean natural : new boolean[] {false, true}) for (int scale : new int[] {0, 2}) {
                var ship = new LegacyShipEntity(ModEntityTypes.LEGACY_SHIP.get(), helper.getLevel());
                ship.applySpawnSpec(ShipEntitySpecs.getByEggMeta(2053), null);
                ship.initializeHostileRuntime(natural, scale);
                ship.setPos(player.position().add(10000, 0, 10000));
                String key = scale > 1 ? "despawnBoss" : "despawnMinion";
                ShinColleConfig.setTestOverride(key, -1);
                ship.tickCount = 100;
                tick.invoke(ship);
                helper.assertTrue(!ship.isRemoved(), "negative setting disables despawn for either origin");
                ShinColleConfig.setTestOverride(key, 10);
                ship.tickCount = 10;
                tick.invoke(ship);
                helper.assertTrue(!ship.isRemoved(), "strict age gate excludes exact limit");
                ship.tickCount = 11;
                ship.setPersistenceRequired();
                tick.invoke(ship);
                helper.assertTrue(!ship.isRemoved(), "explicit vanilla persistence suppresses custom adapter too");
                CompoundTag saved = ship.saveWithoutId(new CompoundTag());
                var loaded = new LegacyShipEntity(ModEntityTypes.LEGACY_SHIP.get(), helper.getLevel());
                loaded.load(saved);
                loaded.tickCount = 11;
                tick.invoke(loaded);
                helper.assertTrue(!loaded.isRemoved(), "explicit persistence survives load");
                loaded.discard();
                ship.initializeHostileRuntime(natural, scale);
                tick.invoke(ship);
                helper.assertTrue(ship.isRemoved(), "default egg and natural hostiles despawn beyond128 after limit");
                ShinColleConfig.clearTestOverride(key);
            }
            var ship = new LegacyShipEntity(ModEntityTypes.LEGACY_SHIP.get(), helper.getLevel());
            ship.applySpawnSpec(ShipEntitySpecs.getByEggMeta(2053), null);
            ship.setPos(player.position().add(20, 0, 0));
            var state = new HostileRuntimeState();
            state.setDespawnTicks(900);
            LegacyShipHostileDespawn.tick(ship, state);
            helper.assertTrue(state.getDespawnTicks() == 0 && !ship.isRemoved(), "within32 resets idle");
            ship.discard();
        } finally {
            ShinColleConfig.clearTestOverride("despawnBoss");
            ShinColleConfig.clearTestOverride("despawnMinion");
            player.discard();
        }
        helper.succeed();
    }
}
