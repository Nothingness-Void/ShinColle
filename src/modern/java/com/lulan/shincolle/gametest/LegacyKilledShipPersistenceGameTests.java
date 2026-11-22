package com.lulan.shincolle.gametest;

import com.lulan.shincolle.entity.ship.LegacyShipEntity;
import com.lulan.shincolle.entity.ship.LegacyShipCombatHelper;
import com.lulan.shincolle.entity.ship.ShipEntitySpecs;
import com.lulan.shincolle.registry.ModEntityTypes;
import com.lulan.shincolle.teitoku.ShipCacheSavedData;
import com.lulan.shincolle.teitoku.TeitokuHelper;
import com.mojang.authlib.GameProfile;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.datafix.DataFixers;
import net.minecraft.util.RandomSource;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.storage.EntityStorage;
import net.minecraft.world.level.chunk.storage.SimpleRegionStorage;
import net.minecraft.world.level.chunk.storage.RegionStorageInfo;
import net.minecraft.world.level.entity.ChunkEntities;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;

@GameTestHolder("shincolle")
@PrefixGameTestTemplate(false)
public final class LegacyKilledShipPersistenceGameTests {
    private LegacyKilledShipPersistenceGameTests() {
    }

    @GameTest(template = "gameplayparitygametests.empty", batch = "kill_region_reload", timeoutTicks = 160)
    public static void unloadedNetherTransferRecoveryRetainsOnlyYoungerCopyAfterLegacyGrace(GameTestHelper helper) throws Exception {
        ServerLevel source = helper.getLevel();
        ServerLevel nether = source.getServer().getLevel(Level.NETHER);
        helper.assertTrue(nether != null, "recovery fixture requires the actual Nether");
        var ship = createShip(helper, 53);
        source.addFreshEntity(ship);
        ship.tickCount++;
        ship.tick();
        int uid = ship.getShipUid();
        var uuid = ship.getUUID();
        var cache = ShipCacheSavedData.get(source);
        cache.updateFromShip(ship, false, true);
        int x = 1000000 + uid * 32;
        int z = 1000000;
        ChunkPos chunk = new ChunkPos(BlockPos.containing(x, 100, z));
        helper.assertTrue(!nether.hasChunk(chunk.x, chunk.z), "destination must start unloaded, not merely be another dimension");
        var operator = FakePlayerFactory.get(source, new GameProfile(UUID.randomUUID(), "RecoveryOperator"));
        operator.setGameMode(GameType.CREATIVE);
        operator.setPos(ship.position());
        var commandSource = operator.createCommandSourceStack().withPermission(4);
        var destination = new java.util.concurrent.atomic.AtomicReference<LegacyShipEntity>();
        java.util.function.Consumer<EntityJoinLevelEvent> listener = event -> {
            if (event.getLevel() == nether && event.getEntity() instanceof LegacyShipEntity transferred
                    && transferred.getShipUid() == uid) destination.set(transferred);
        };
        MinecraftForge.EVENT_BUS.addListener(listener);
        try {
            source.getServer().getCommands().performPrefixedCommand(commandSource,
                    "execute in minecraft:the_nether run tp " + uuid + " " + x + " 100 " + z);
        } finally {
            MinecraftForge.EVENT_BUS.unregister(listener);
        }
        helper.assertTrue(ship.isRemoved(), "actual cross-dimension teleport must replace the source entity");
        helper.assertTrue(destination.get() != null, "teleport must join an actual destination ship with the original UID");
        helper.assertTrue(cache.getShip(uid).dimensionId().equals(source.dimension().location().toString()),
                "before destination ticks the legacy cache still points at the origin");
        source.getServer().getCommands().performPrefixedCommand(commandSource, "ship get " + uid);
        LegacyShipEntity recovered = null;
        for (Entity e : source.getAllEntities()) {
            if (e instanceof LegacyShipEntity candidate && candidate.getShipUid() == uid && candidate.isAlive()) recovered = candidate;
        }
        helper.assertTrue(recovered != null, "actual /ship get must reproduce the legacy pre-tick recovery window");
        LegacyShipEntity younger = recovered;
        nether.setChunkForced(chunk.x, chunk.z, true);
        nether.getChunk(chunk.x, chunk.z);
        helper.succeedWhen(() -> {
            var older = destination.get();
            helper.assertTrue(nether.getEntity(older.getId()) == older,
                        "forced destination must expose transferred id=" + older.getId() + " uuid=" + older.getUUID()
                                + " original=" + uuid + " removed=" + older.isRemoved() + " pos=" + older.position());
            try {
                helper.assertTrue(older.getShipUid() == uid && older != younger && younger.isAlive(),
                        "reproduction must contain two distinct living entities with the same UID");
                older.tickCount = 301;
                younger.tickCount = 201;
                cache.updateFromShip(younger, false, true);
                TeitokuHelper.refreshShipCache(older, false);
                helper.assertTrue(older.isRemoved() && younger.isAlive(),
                        "legacy guard removes the older duplicate once both ages exceed200");
                helper.assertTrue(cache.getShip(uid).entityId() == younger.getId() && !cache.getShip(uid).dead(),
                        "discard callback must not overwrite the surviving live cache with the removed copy");
            } finally {
                destination.get().discard();
                younger.discard();
                nether.setChunkForced(chunk.x, chunk.z, false);
            }
        });
    }

    @GameTest(template = "gameplayparitygametests.empty", batch = "kill_region_reload")
    public static void duplicateGuardRetainsGracePeriodAndDeadCopiesCannotDeleteLivingShips(GameTestHelper helper) {
        var first = createShip(helper, 53);
        var second = createShip(helper, 53);
        helper.getLevel().addFreshEntity(first);
        helper.getLevel().addFreshEntity(second);
        first.tickCount++;
        first.tick();
        second.tickCount++;
        second.tick();
        CompoundTag tag = new CompoundTag();
        second.saveWithoutId(tag);
        tag.putInt("ShipUID", first.getShipUid());
        second.readAdditionalSaveData(tag);
        var cache = ShipCacheSavedData.get(helper.getLevel());
        try {
            first.tickCount = 301;
            second.tickCount = 200;
            cache.updateFromShip(second, false, true);
            TeitokuHelper.refreshShipCache(first, false);
            helper.assertTrue(first.isAlive() && second.isAlive(), "exactly200 ticks must remain inside legacy grace period");
            second.tickCount = 201;
            second.setHealth(0);
            cache.updateFromShip(second, false, true);
            TeitokuHelper.refreshShipCache(first, false);
            helper.assertTrue(first.isAlive(), "a dead cached copy must never delete a living ship");
            second.setHealth(second.getMaxHealth());
            second.tickCount = 401;
            cache.updateFromShip(second, false, true);
            TeitokuHelper.refreshShipCache(first, false);
            helper.assertTrue(first.isAlive() && second.isRemoved(), "when cached copy is older, retain the refreshing younger ship");
        } finally {
            first.discard();
            second.discard();
        }
        helper.succeed();
    }

    @GameTest(template = "gameplayparitygametests.empty", batch = "kill_region_reload")
    public static void killAndEntityRegionReloadNeverReviveShips(GameTestHelper helper) throws IOException {
        for (int egg : new int[] {53, 2053}) {
            for (int elapsed : egg == 53 ? new int[] {0, 5} : new int[] {0, 5, 399}) {
                var ship = createShip(helper, egg);
                helper.getLevel().addFreshEntity(ship);
                ship.getRandom().setSeed(0L);
                helper.getLevel().getServer().getCommands().performPrefixedCommand(
                        helper.getLevel().getServer().createCommandSourceStack(),
                        "kill " + ship.getStringUUID());
                helper.assertTrue(!ship.isAlive() && ship.getHealth() == 0, "actual /kill must leave zero health: " + egg + "/" + elapsed
                        + " hp=" + ship.getHealth() + " tracked=" + (helper.getLevel().getEntity(ship.getUUID()) == ship));
                for (int tick = 0; tick < elapsed; tick++) ship.tick();
                helper.assertTrue(!ship.isRemoved(), "save fixture must be a pending corpse, not an already removed entity");
                int savedDeathTime = ship.deathTime;
                var reloaded = diskRoundTrip(helper, ship);
                helper.assertTrue(reloaded.getUUID().equals(ship.getUUID()) && !reloaded.isAlive()
                                && reloaded.getHealth() == 0 && reloaded.deathTime == savedDeathTime,
                        "entity-region reload must preserve corpse identity, zero health and death timer: " + egg + "/" + elapsed
                                + " hp=" + reloaded.getHealth() + " death=" + reloaded.deathTime + " uuid=" + reloaded.getUUID().equals(ship.getUUID()));
                while (!reloaded.isRemoved() && reloaded.deathTime < 400) {
                    reloaded.tick();
                    helper.assertTrue(!reloaded.isAlive(), "pending corpse must never revive");
                }
                helper.assertTrue(reloaded.isRemoved(), "reloaded corpse must finish removal at its current deadline");
                if (egg >= 2000) helper.assertTrue(reloaded.deathTime == 400, "scale3 removal deadline must remain400");
            }
        }
        helper.succeed();
    }

    @GameTest(template = "gameplayparitygametests.empty", batch = "kill_region_reload")
    public static void livingInjuredShipAndEmptyRegionRoundTrip(GameTestHelper helper) throws IOException {
        for (int level : new int[] {1, 75}) {
            var ship = createShip(helper, 53);
            ship.setShipLevel(level);
            ship.setHealth(ship.getMaxHealth() * 0.37F);
            float health = ship.getHealth();
            var loaded = diskRoundTrip(helper, ship);
            helper.assertTrue(loaded.isAlive() && Math.abs(loaded.getHealth() - health) < 0.001F,
                    "living damaged ships must retain health rather than revive or die during reload: " + health + " -> " + loaded.getHealth());
            loaded.discard();
        }
        helper.succeed();
    }

    @GameTest(template = "gameplayparitygametests.empty", batch = "kill_region_reload")
    public static void missingHealthKeepsSpawnDefaultsButDeathTimerStillLocksHealth(GameTestHelper helper) {
        var ship = createShip(helper, 53);
        CompoundTag tag = new CompoundTag();
        ship.save(tag);
        tag.remove("Health");
        var fresh = createShip(helper, 53);
        fresh.load(tag);
        helper.assertTrue(fresh.isAlive() && fresh.getHealth() == fresh.getMaxHealth(),
                "missing optional Health must retain normal spawn defaults");
        tag.putFloat("Health", 10.0F);
        tag.putShort("DeathTime", (short) 5);
        fresh.load(tag);
        helper.assertTrue(fresh.getHealth() == 0 && !fresh.isAlive(), "positive saved health cannot unlock a pending death");
        fresh.discard();
        ship.discard();
        helper.succeed();
    }

    private static LegacyShipEntity createShip(GameTestHelper helper, int egg) {
        var ship = ModEntityTypes.LEGACY_SHIP.get().create(helper.getLevel());
        helper.assertTrue(ship != null, "kill fixture must exist");
        ship.applySpawnSpec(ShipEntitySpecs.getByEggMeta(egg), null);
        if (egg >= 2000) ship.initializeHostileRuntime(false, 3);
        ship.setCanDropItem(false);
        ship.setNoAi(true);
        ship.setNoGravity(true);
        ship.setPos(Vec3.atCenterOf(helper.absolutePos(new BlockPos(2, 2, 2))));
        return ship;
    }

    @GameTest(template = "gameplayparitygametests.empty", batch = "kill_region_reload")
    public static void damageOverflowSaturatesWithoutChangingNormalOrZeroDamage(GameTestHelper helper) {
        var ship = createShip(helper, 53);
        var stats = ship.getLegacyStats();
        float factor = Math.max(0F, 1F - stats.defense() + (RandomSource.create(0L).nextFloat() * 0.5F - 0.25F));
        helper.assertTrue(LegacyShipCombatHelper.applyDefenseReduction(RandomSource.create(0L), stats, 16F) == 16F * factor,
                "ordinary damage arithmetic and random draw must stay exact");
        helper.assertTrue(LegacyShipCombatHelper.applyDefenseReduction(RandomSource.create(0L), stats, 0F) == 0F,
                "zero damage must remain zero");
        helper.assertTrue(LegacyShipCombatHelper.applyDefenseReduction(RandomSource.create(0L), stats, Float.MAX_VALUE) == Float.MAX_VALUE,
                "kill damage amplification must saturate to finite damage, not overflow into Neo damage-container NaN");
        ship.discard();
        helper.succeed();
    }

    private static LegacyShipEntity diskRoundTrip(GameTestHelper helper, LegacyShipEntity ship) throws IOException {
        Path directory = Files.createTempDirectory(helper.getLevel().getServer().getWorldPath(LevelResource.ROOT), "shincolle-kill-");
        ChunkPos chunk = ship.chunkPosition();
        try (var storage = storage(helper.getLevel(), directory)) {
            storage.storeEntities(new ChunkEntities<Entity>(chunk, List.of(ship)));
            storage.flush(true);
        }
        ship.remove(Entity.RemovalReason.UNLOADED_TO_CHUNK);
        LegacyShipEntity loaded;
        try (var storage = storage(helper.getLevel(), directory)) {
            var entities = storage.loadEntities(chunk).join().getEntities().toList();
            helper.assertTrue(entities.size() == 1 && entities.get(0) instanceof LegacyShipEntity,
                    "closed/reopened entity-region file must contain exactly one ship");
            loaded = (LegacyShipEntity) entities.get(0);
            storage.storeEntities(new ChunkEntities<Entity>(chunk, List.of()));
            storage.flush(true);
        }
        try (var storage = storage(helper.getLevel(), directory)) {
            helper.assertTrue(storage.loadEntities(chunk).join().isEmpty(), "persisted empty chunk must not respawn an old ship");
        }
        return loaded;
    }

    private static EntityStorage storage(ServerLevel level, Path directory) {
        return new EntityStorage(new SimpleRegionStorage(
                new RegionStorageInfo("shincolle-kill-test", level.dimension(), "entities"),
                directory, DataFixers.getDataFixer(), false, DataFixTypes.ENTITY_CHUNK), level, Runnable::run);
    }
}
