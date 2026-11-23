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
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;
import java.util.UUID;

@GameTestHolder("shincolle")
@PrefixGameTestTemplate(false)
public final class AuditHostileDespawnGameTests {
    @GameTest(template = "gameplayparitygametests.empty", batch = "audit_parity")
    public static void routineTicksDoNotPersistUnnamedHostiles(GameTestHelper helper) {
        var player = FakePlayerFactory.get(helper.getLevel(), new GameProfile(UUID.randomUUID(), "AuditTick64"));
        player.setPos(helper.absoluteVec(new Vec3(2, 2, 2)));
        helper.getLevel().addNewPlayer(player);
        ShinColleConfig.setTestOverride("despawnMinion", 128);
        ShinColleConfig.setTestOverride("despawnBoss", 128);
        try {
            for (boolean natural : new boolean[] {false, true}) for (int scale : new int[] {0, 2}) {
                var ship = new LegacyShipEntity(ModEntityTypes.LEGACY_SHIP.get(), helper.getLevel());
                ship.applySpawnSpec(ShipEntitySpecs.getByEggMeta(2053), null);
                ship.initializeHostileRuntime(natural, scale);
                ship.setPos(player.position().add(10000, 0, 10000));
                ship.setHealth(ship.getMaxHealth() * 0.5F);
                ship.tickCount = 64;
                ship.tick();
                helper.assertTrue(ship.getHpState() == 2,
                        "actual tick64 must set the legacy MODERATE HP-state value2");
                CompoundTag saved = ship.saveWithoutId(new CompoundTag());
                helper.assertTrue(!saved.getCompound("HostileRuntime").getBoolean("PersistenceRequired"),
                        "routine tick64 HP/emotion updates must not request explicit hostile persistence");
                helper.assertTrue(ship.isPersistenceRequired(), "unified Mob native retention remains unchanged");
                var loaded = new LegacyShipEntity(ModEntityTypes.LEGACY_SHIP.get(), helper.getLevel());
                loaded.load(saved);
                for (var candidate : new LegacyShipEntity[] {ship, loaded}) {
                    helper.assertTrue(!candidate.hasCustomName()
                                    && candidate.distanceToSqr(helper.getLevel().getNearestPlayer(candidate, -1.0D)) > 16384.0D,
                            "unnamed hostile must have no player within128 blocks");
                    candidate.tickCount = 128;
                    candidate.tick();
                    helper.assertTrue(!candidate.isRemoved(), "exact age limit must still retain the hostile");
                    candidate.tickCount = 129;
                    candidate.tick();
                    helper.assertTrue(candidate.isRemoved(),
                            "unnamed natural/egg minion/boss must despawn after tick64, including after reload");
                }
                var named = new LegacyShipEntity(ModEntityTypes.LEGACY_SHIP.get(), helper.getLevel());
                named.applySpawnSpec(ShipEntitySpecs.getByEggMeta(2053), null);
                named.initializeHostileRuntime(natural, scale);
                named.setPos(player.position().add(10000, 0, 10000));
                ItemStack nameTag = new ItemStack(Items.NAME_TAG);
                nameTag.setHoverName(Component.literal("Audit named hostile"));
                nameTag.interactLivingEntity(player, named, InteractionHand.MAIN_HAND);
                helper.assertTrue(named.hasCustomName(), "actual name tag must apply its name");
                CompoundTag namedData = named.saveWithoutId(new CompoundTag());
                helper.assertTrue(namedData.getCompound("HostileRuntime").getBoolean("PersistenceRequired"),
                        "actual vanilla name-tag request must still set explicit persistence");
                loaded = new LegacyShipEntity(ModEntityTypes.LEGACY_SHIP.get(), helper.getLevel());
                loaded.load(namedData);
                for (var candidate : new LegacyShipEntity[] {named, loaded}) {
                    candidate.tickCount = 192;
                    candidate.tick();
                    helper.assertTrue(!candidate.isRemoved(), "name-tag persistence survives routine updates and reload");
                    candidate.discard();
                }
            }
        } finally {
            ShinColleConfig.clearTestOverride("despawnBoss");
            ShinColleConfig.clearTestOverride("despawnMinion");
            player.discard();
        }
        helper.succeed();
    }

    @GameTest(template = "gameplayparitygametests.empty", batch = "audit_parity")
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
            ShinColleConfig.setTestOverride("despawnMinion", 0);
            ship.tickCount = 100;
            ship.setPos(player.position().add(100, 0, 0));
            state.setDespawnTicks(900);
            ship.getRandom().setSeed(0);
            helper.assertTrue(net.minecraft.util.RandomSource.create(0).nextInt(800) != 0, "fixture must not roll despawn");
            LegacyShipHostileDespawn.tick(ship, state);
            helper.assertTrue(!ship.isRemoved(), "legacy has no unconditional96-block/900-idle rule");
            ship.setPos(player.position().add(10000, 0, 10000));
            CompoundTag modern = ship.saveWithoutId(new CompoundTag());
            for (int format = 0; format < 4; format++) {
                CompoundTag data = modern.copy();
                if (format == 1 || format == 2) {
                    data.remove("HostileRuntime");
                    data.putBoolean("PersistenceRequired", format == 2);
                } else if (format == 3) {
                    data.getCompound("HostileRuntime").remove("PersistenceRequired");
                }
                var loaded = new LegacyShipEntity(ModEntityTypes.LEGACY_SHIP.get(), helper.getLevel());
                loaded.load(data);
                loaded.tickCount = 100;
                tick.invoke(loaded);
                helper.assertTrue(loaded.isRemoved() == (format != 2),
                        "new/default, legacy false/true and pre-fix modern forced persistence migrate correctly; format=" + format);
                loaded.discard();
            }
            ship.discard();
        } finally {
            ShinColleConfig.clearTestOverride("despawnBoss");
            ShinColleConfig.clearTestOverride("despawnMinion");
            player.discard();
        }
        helper.succeed();
    }
}
