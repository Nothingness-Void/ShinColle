package com.lulan.shincolle.world;

import com.lulan.shincolle.entity.ship.ShipEntitySpec;
import com.lulan.shincolle.entity.ship.ShipEntitySpecs;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

public final class HostileEncounterTable {

    private static final int[] COMMON_POOL = {2053, 2054, 2055, 2056, 2038, 2040, 2041};
    private static final int[] RARE_CRUISER_POOL = {2058, 2059, 2060, 2061};
    private static final int[] RARE_CARRIER_POOL = {2049, 2050};
    private static final int[] SUPER_RARE_KONGOU_POOL = {2062, 2063, 2064, 2065};
    private static final int[] REACHABLE_HOSTILE_POOL = {
            2038, 2039, 2040, 2041, 2048, 2049, 2050,
            2053, 2054, 2055, 2056, 2058, 2059, 2060, 2061, 2062, 2063, 2064, 2065
    };
    private static final List<HostileSpawnProfile> LEGACY_COMMON_PROFILES = createCommonProfiles();
    private static final List<HostileSpawnProfile> LEGACY_BOSS_PROFILES = createBossProfiles();

    private HostileEncounterTable() {
    }

    public static List<HostileSpawnProfile> commonProfiles() {
        return LEGACY_COMMON_PROFILES;
    }

    public static List<HostileSpawnProfile> bossProfiles() {
        return LEGACY_BOSS_PROFILES;
    }

    public static List<ShipEntitySpec> reachableShipSpecs() {
        LinkedHashSet<ShipEntitySpec> specs = new LinkedHashSet<>();
        for (int eggMeta : REACHABLE_HOSTILE_POOL) {
            specs.add(ShipEntitySpecs.getByEggMeta(eggMeta));
        }
        return List.copyOf(specs);
    }

    public static HostileSpawnProfile pick(RandomSource random, Difficulty difficulty, boolean deepOcean, boolean allowBoss) {
        if (difficulty == Difficulty.PEACEFUL) {
            return new HostileSpawnProfile(COMMON_POOL[0], 1, false, false);
        }

        if (allowBoss && random.nextInt(4) == 0) {
            return bossProfile(random);
        }

        return commonProfile(random);
    }

    public static HostileSpawnProfile commonProfile(RandomSource random) {
        return new HostileSpawnProfile(pickLegacyMobEggMeta(random), 1, random.nextInt(10) > 7, false);
    }

    public static HostileSpawnProfile bossProfile(RandomSource random) {
        return new HostileSpawnProfile(pickLegacyMobEggMeta(random), 1, random.nextInt(100) > 65, true);
    }

    private static int pickLegacyMobEggMeta(RandomSource random) {
        int tierRoll = random.nextInt(100);

        if (tierRoll > 75) {
            return switch (random.nextInt(3)) {
                case 1 -> 2048;
                case 2 -> pickFrom(random, SUPER_RARE_KONGOU_POOL);
                default -> 2039;
            };
        }

        if (tierRoll > 45) {
            return switch (random.nextInt(3)) {
                case 1, 2 -> pickFrom(random, RARE_CRUISER_POOL);
                default -> pickFrom(random, RARE_CARRIER_POOL);
            };
        }

        return pickFrom(random, COMMON_POOL);
    }

    private static int pickFrom(RandomSource random, int[] pool) {
        return pool[random.nextInt(pool.length)];
    }

    private static List<HostileSpawnProfile> createCommonProfiles() {
        ArrayList<HostileSpawnProfile> profiles = new ArrayList<>(REACHABLE_HOSTILE_POOL.length);
        for (int eggMeta : REACHABLE_HOSTILE_POOL) {
            profiles.add(new HostileSpawnProfile(eggMeta, 1, false, false));
        }
        return List.copyOf(profiles);
    }

    private static List<HostileSpawnProfile> createBossProfiles() {
        ArrayList<HostileSpawnProfile> profiles = new ArrayList<>(REACHABLE_HOSTILE_POOL.length);
        for (int eggMeta : REACHABLE_HOSTILE_POOL) {
            profiles.add(new HostileSpawnProfile(eggMeta, 1, true, true));
        }
        return List.copyOf(profiles);
    }
}
