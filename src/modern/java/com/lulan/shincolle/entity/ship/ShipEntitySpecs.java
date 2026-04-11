package com.lulan.shincolle.entity.ship;

import net.minecraft.util.RandomSource;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

public final class ShipEntitySpecs {

    private static final Map<Integer, ShipEntitySpec> SPECS = new LinkedHashMap<>();
    private static final int[] PRIMARY_EGG_POOL = {2, 3, 4, 5, 11, 12, 18, 19, 20, 21};
    private static final int[] ADVANCED_EGG_POOL = {14, 15, 16, 17, 22, 23, 28, 29, 30, 31, 32, 33, 35, 46, 51, 74};
    private static final int[] CONSTRUCTION_SMALL_EGG_POOL = {53, 54, 55, 56, 38, 58, 59, 60, 61, 40, 41};
    private static final int[] CONSTRUCTION_LARGE_EGG_POOL = {39, 48, 49, 50, 62, 63, 64, 65, 60, 61};
    private static final Map<Integer, Integer> FRIENDLY_COUNTERPARTS = new HashMap<>();

    public static final ShipEntitySpec DEFAULT = register(2, "EntityDestroyerI", ShipArchetype.DESTROYER, true);

    static {
        register(3, "EntityDestroyerRo", ShipArchetype.DESTROYER, true);
        register(4, "EntityDestroyerHa", ShipArchetype.DESTROYER, true);
        register(5, "EntityDestroyerNi", ShipArchetype.DESTROYER, true);
        register(11, "EntityHeavyCruiserRi", ShipArchetype.CRUISER, true);
        register(12, "EntityHeavyCruiserNe", ShipArchetype.CRUISER, true);
        register(14, "EntityCarrierWo", ShipArchetype.CARRIER, true);
        register(15, "EntityBattleshipRu", ShipArchetype.BATTLESHIP, true);
        register(16, "EntityBattleshipTa", ShipArchetype.BATTLESHIP, true);
        register(17, "EntityBattleshipRe", ShipArchetype.BATTLESHIP, true);
        register(18, "EntityTransportWa", ShipArchetype.TRANSPORT, true);
        register(19, "EntitySubmKa", ShipArchetype.SUBMARINE, true);
        register(20, "EntitySubmYo", ShipArchetype.SUBMARINE, true);
        register(21, "EntitySubmSo", ShipArchetype.SUBMARINE, true);
        register(22, "EntityCarrierHime", ShipArchetype.PRINCESS, true);
        register(23, "EntityAirfieldHime", ShipArchetype.INSTALLATION, true);
        register(28, "EntityBattleshipHime", ShipArchetype.PRINCESS, true);
        register(29, "EntityDestroyerHime", ShipArchetype.PRINCESS, true);
        register(30, "EntityHarbourHime", ShipArchetype.INSTALLATION, true);
        register(31, "EntityIsolatedHime", ShipArchetype.INSTALLATION, true);
        register(32, "EntityMidwayHime", ShipArchetype.PRINCESS, true);
        register(33, "EntityNorthernHime", ShipArchetype.PRINCESS, true);
        register(35, "EntityCarrierWDemon", ShipArchetype.PRINCESS, true);
        register(38, "EntityDestroyerShimakaze", ShipArchetype.DESTROYER, false);
        register(2038, "EntityDestroyerShimakaze", ShipArchetype.DESTROYER, true);
        register(39, "EntityBattleshipNagato", ShipArchetype.BATTLESHIP, false);
        register(2039, "EntityBattleshipNagato", ShipArchetype.BATTLESHIP, true);
        register(40, "EntitySubmU511", ShipArchetype.SUBMARINE, false);
        register(2040, "EntitySubmU511", ShipArchetype.SUBMARINE, true);
        register(41, "EntitySubmRo500", ShipArchetype.SUBMARINE, false);
        register(2041, "EntitySubmRo500", ShipArchetype.SUBMARINE, true);
        register(46, "EntitySubmHime", ShipArchetype.PRINCESS, true);
        register(48, "EntityBattleshipYamato", ShipArchetype.BATTLESHIP, false);
        register(2048, "EntityBattleshipYamato", ShipArchetype.BATTLESHIP, true);
        register(49, "EntityCarrierKaga", ShipArchetype.CARRIER, false);
        register(2049, "EntityCarrierKaga", ShipArchetype.CARRIER, true);
        register(50, "EntityCarrierAkagi", ShipArchetype.CARRIER, false);
        register(2050, "EntityCarrierAkagi", ShipArchetype.CARRIER, true);
        register(51, "EntityCAHime", ShipArchetype.PRINCESS, true);
        register(53, "EntityDestroyerAkatsuki", ShipArchetype.DESTROYER, false);
        register(2053, "EntityDestroyerAkatsuki", ShipArchetype.DESTROYER, true);
        register(54, "EntityDestroyerHibiki", ShipArchetype.DESTROYER, false);
        register(2054, "EntityDestroyerHibiki", ShipArchetype.DESTROYER, true);
        register(55, "EntityDestroyerIkazuchi", ShipArchetype.DESTROYER, false);
        register(2055, "EntityDestroyerIkazuchi", ShipArchetype.DESTROYER, true);
        register(56, "EntityDestroyerInazuma", ShipArchetype.DESTROYER, false);
        register(2056, "EntityDestroyerInazuma", ShipArchetype.DESTROYER, true);
        register(58, "EntityCruiserTenryuu", ShipArchetype.CRUISER, false);
        register(2058, "EntityCruiserTenryuu", ShipArchetype.CRUISER, true);
        register(59, "EntityCruiserTatsuta", ShipArchetype.CRUISER, false);
        register(2059, "EntityCruiserTatsuta", ShipArchetype.CRUISER, true);
        register(60, "EntityCruiserAtago", ShipArchetype.CRUISER, false);
        register(2060, "EntityCruiserAtago", ShipArchetype.CRUISER, true);
        register(61, "EntityCruiserTakao", ShipArchetype.CRUISER, false);
        register(2061, "EntityCruiserTakao", ShipArchetype.CRUISER, true);
        register(62, "EntityBBKongou", ShipArchetype.BATTLESHIP, false);
        register(2062, "EntityBBKongou", ShipArchetype.BATTLESHIP, true);
        register(63, "EntityBBHiei", ShipArchetype.BATTLESHIP, false);
        register(2063, "EntityBBHiei", ShipArchetype.BATTLESHIP, true);
        register(64, "EntityBBHaruna", ShipArchetype.BATTLESHIP, false);
        register(2064, "EntityBBHaruna", ShipArchetype.BATTLESHIP, true);
        register(65, "EntityBBKirishima", ShipArchetype.BATTLESHIP, false);
        register(2065, "EntityBBKirishima", ShipArchetype.BATTLESHIP, true);
        register(74, "EntitySubmHimeNew", ShipArchetype.PRINCESS, true);
        registerFriendlyCounterparts();
    }

    private ShipEntitySpecs() {
    }

    public static ShipEntitySpec getByEggMeta(int eggMeta) {
        return SPECS.getOrDefault(eggMeta, DEFAULT);
    }

    public static @Nullable ShipEntitySpec findByEggMeta(int eggMeta) {
        return SPECS.get(eggMeta);
    }

    public static @Nullable ShipEntitySpec findByLegacyClassId(int classId) {
        return SPECS.get(classId + 2);
    }

    public static ShipEntitySpec friendlyCounterpart(int eggMeta) {
        Integer friendlyMeta = FRIENDLY_COUNTERPARTS.get(eggMeta);
        if (friendlyMeta != null) {
            return getByEggMeta(friendlyMeta);
        }

        return fallbackFriendlySpec(getByEggMeta(eggMeta).archetype());
    }

    public static ShipEntitySpec friendlyCounterpart(ShipEntitySpec spec) {
        return friendlyCounterpart(spec.eggMeta());
    }

    public static ShipEntitySpec resolveEggItem(String itemPath, RandomSource random) {
        if ("smallegg".equals(itemPath)) {
            return randomConstructionSmall(random);
        }

        if ("largeegg".equals(itemPath)) {
            return randomConstructionLarge(random);
        }

        if (itemPath.startsWith("shipegg")) {
            try {
                return getByEggMeta(Integer.parseInt(itemPath.substring("shipegg".length())));
            } catch (NumberFormatException ignored) {
                return DEFAULT;
            }
        }

        return DEFAULT;
    }

    public static ShipEntitySpec randomPrimary(RandomSource random) {
        return getByEggMeta(PRIMARY_EGG_POOL[random.nextInt(PRIMARY_EGG_POOL.length)]);
    }

    public static ShipEntitySpec randomAdvanced(RandomSource random) {
        return getByEggMeta(ADVANCED_EGG_POOL[random.nextInt(ADVANCED_EGG_POOL.length)]);
    }

    public static ShipEntitySpec randomConstructionSmall(RandomSource random) {
        return getByEggMeta(CONSTRUCTION_SMALL_EGG_POOL[random.nextInt(CONSTRUCTION_SMALL_EGG_POOL.length)]);
    }

    public static ShipEntitySpec randomConstructionLarge(RandomSource random) {
        return getByEggMeta(CONSTRUCTION_LARGE_EGG_POOL[random.nextInt(CONSTRUCTION_LARGE_EGG_POOL.length)]);
    }

    public static List<ShipEntitySpec> primaryPool() {
        return resolvePool(PRIMARY_EGG_POOL);
    }

    public static List<ShipEntitySpec> advancedPool() {
        return resolvePool(ADVANCED_EGG_POOL);
    }

    public static List<ShipEntitySpec> constructionSmallPool() {
        return resolvePool(CONSTRUCTION_SMALL_EGG_POOL);
    }

    public static List<ShipEntitySpec> constructionLargePool() {
        return resolvePool(CONSTRUCTION_LARGE_EGG_POOL);
    }

    public static List<ShipEntitySpec> currentPlayableFriendlyRoster() {
        LinkedHashSet<ShipEntitySpec> roster = new LinkedHashSet<>();
        roster.addAll(constructionSmallPool());
        roster.addAll(constructionLargePool());
        return List.copyOf(roster);
    }

    public static Collection<ShipEntitySpec> values() {
        return Collections.unmodifiableCollection(SPECS.values());
    }

    private static ShipEntitySpec fallbackFriendlySpec(ShipArchetype archetype) {
        return switch (archetype) {
            case DESTROYER -> getByEggMeta(38);
            case CRUISER, TRANSPORT -> getByEggMeta(58);
            case SUBMARINE -> getByEggMeta(40);
            case CARRIER, INSTALLATION -> getByEggMeta(49);
            case BATTLESHIP, PRINCESS -> getByEggMeta(39);
        };
    }

    private static void registerFriendlyCounterparts() {
        mapCounterpart(2, 53);
        mapCounterpart(3, 54);
        mapCounterpart(4, 55);
        mapCounterpart(5, 56);
        mapCounterpart(11, 60);
        mapCounterpart(12, 61);
        mapCounterpart(14, 49);
        mapCounterpart(15, 39);
        mapCounterpart(16, 62);
        mapCounterpart(17, 48);
        mapCounterpart(18, 58);
        mapCounterpart(19, 40);
        mapCounterpart(20, 41);
        mapCounterpart(21, 40);
        mapCounterpart(22, 50);
        mapCounterpart(23, 49);
        mapCounterpart(28, 48);
        mapCounterpart(29, 38);
        mapCounterpart(30, 49);
        mapCounterpart(31, 50);
        mapCounterpart(32, 48);
        mapCounterpart(33, 39);
        mapCounterpart(35, 50);
        mapCounterpart(46, 41);
        mapCounterpart(51, 60);
        mapCounterpart(74, 40);

        mapCounterpart(2038, 38);
        mapCounterpart(2039, 39);
        mapCounterpart(2040, 40);
        mapCounterpart(2041, 41);
        mapCounterpart(2048, 48);
        mapCounterpart(2049, 49);
        mapCounterpart(2050, 50);
        mapCounterpart(2053, 53);
        mapCounterpart(2054, 54);
        mapCounterpart(2055, 55);
        mapCounterpart(2056, 56);
        mapCounterpart(2058, 58);
        mapCounterpart(2059, 59);
        mapCounterpart(2060, 60);
        mapCounterpart(2061, 61);
        mapCounterpart(2062, 62);
        mapCounterpart(2063, 63);
        mapCounterpart(2064, 64);
        mapCounterpart(2065, 65);
    }

    private static void mapCounterpart(int hostileEggMeta, int friendlyEggMeta) {
        FRIENDLY_COUNTERPARTS.put(hostileEggMeta, friendlyEggMeta);
    }

    private static List<ShipEntitySpec> resolvePool(int[] pool) {
        List<ShipEntitySpec> specs = new ArrayList<>(pool.length);
        for (int eggMeta : pool) {
            specs.add(getByEggMeta(eggMeta));
        }
        return List.copyOf(specs);
    }

    private static ShipEntitySpec register(int eggMeta, String textureStem, ShipArchetype archetype, boolean hostile) {
        ShipEntitySpec spec = new ShipEntitySpec(eggMeta, "item.shincolle.shipegg" + eggMeta, textureStem, archetype, hostile);
        ShipEntitySpec previous = SPECS.put(eggMeta, spec);

        if (previous != null) {
            throw new IllegalStateException("Duplicate legacy ship egg meta " + eggMeta);
        }

        return spec;
    }
}
