package com.lulan.shincolle.gametest;

import com.lulan.shincolle.entity.ship.LegacyShipEntity;
import com.lulan.shincolle.entity.ship.ShipEntitySpecs;
import com.lulan.shincolle.registry.ModEntityTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.datafix.DataFixers;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.storage.EntityStorage;
import net.minecraft.world.level.entity.ChunkEntities;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

@GameTestHolder("shincolle")
@PrefixGameTestTemplate(false)
public final class LegacyKilledShipPersistenceGameTests {
    private LegacyKilledShipPersistenceGameTests() {
    }

    @GameTest(template = "gameplayparitygametests.empty", batch = "kill_region_reload")
    public static void killAndEntityRegionReloadNeverReviveShips(GameTestHelper helper) throws IOException {
        for (int egg : new int[] {53, 2053}) {
            for (int elapsed : egg == 53 ? new int[] {0, 5} : new int[] {0, 5, 399}) {
                var ship = createShip(helper, egg);
                helper.getLevel().addFreshEntity(ship);
                helper.getLevel().getServer().getCommands().performPrefixedCommand(
                        helper.getLevel().getServer().createCommandSourceStack().withSuppressedOutput(),
                        "kill " + ship.getStringUUID());
                helper.assertTrue(!ship.isAlive() && ship.getHealth() == 0, "actual /kill must leave zero health");
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
        return new EntityStorage(level, directory, DataFixers.getDataFixer(), false, Runnable::run);
    }
}
