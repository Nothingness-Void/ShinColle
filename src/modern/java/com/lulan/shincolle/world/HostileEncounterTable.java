package com.lulan.shincolle.world;

import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;

import java.util.List;

public final class HostileEncounterTable {

    private static final List<HostileSpawnProfile> OCEAN_COMMON = List.of(
            new HostileSpawnProfile(2, 12, 2, 4, false, false),
            new HostileSpawnProfile(3, 10, 2, 4, false, false),
            new HostileSpawnProfile(4, 10, 2, 4, false, false),
            new HostileSpawnProfile(5, 10, 2, 4, false, false),
            new HostileSpawnProfile(18, 7, 1, 2, false, false),
            new HostileSpawnProfile(11, 6, 1, 2, false, false),
            new HostileSpawnProfile(12, 6, 1, 2, false, false),
            new HostileSpawnProfile(19, 5, 1, 2, false, false),
            new HostileSpawnProfile(20, 5, 1, 2, false, false),
            new HostileSpawnProfile(21, 5, 1, 2, false, false),
            new HostileSpawnProfile(14, 4, 1, 2, true, false),
            new HostileSpawnProfile(15, 4, 1, 2, true, false),
            new HostileSpawnProfile(16, 4, 1, 2, true, false),
            new HostileSpawnProfile(17, 4, 1, 2, true, false));
    private static final List<HostileSpawnProfile> OCEAN_BOSSES = List.of(
            new HostileSpawnProfile(22, 8, 1, 1, true, true),
            new HostileSpawnProfile(23, 6, 1, 1, true, true),
            new HostileSpawnProfile(28, 7, 1, 1, true, true),
            new HostileSpawnProfile(30, 6, 1, 1, true, true),
            new HostileSpawnProfile(31, 5, 1, 1, true, true),
            new HostileSpawnProfile(32, 4, 1, 1, true, true),
            new HostileSpawnProfile(33, 4, 1, 1, true, true),
            new HostileSpawnProfile(35, 3, 1, 1, true, true),
            new HostileSpawnProfile(46, 4, 1, 1, true, true),
            new HostileSpawnProfile(51, 4, 1, 1, true, true),
            new HostileSpawnProfile(74, 3, 1, 1, true, true));

    private HostileEncounterTable() {
    }

    public static HostileSpawnProfile pick(RandomSource random, Difficulty difficulty, boolean deepOcean, boolean allowBoss) {
        if (allowBoss && difficulty != Difficulty.PEACEFUL && random.nextFloat() < (deepOcean ? 0.45F : 0.28F)) {
            return weightedPick(random, OCEAN_BOSSES);
        }

        return weightedPick(random, OCEAN_COMMON);
    }

    private static HostileSpawnProfile weightedPick(RandomSource random, List<HostileSpawnProfile> entries) {
        int totalWeight = 0;
        for (HostileSpawnProfile entry : entries) {
            totalWeight += Math.max(1, entry.weight());
        }

        int roll = random.nextInt(Math.max(1, totalWeight));
        for (HostileSpawnProfile entry : entries) {
            roll -= Math.max(1, entry.weight());
            if (roll < 0) {
                return entry;
            }
        }

        return entries.get(0);
    }
}
