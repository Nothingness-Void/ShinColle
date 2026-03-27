package com.lulan.shincolle.entity.ship;

import net.minecraft.util.RandomSource;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public final class ShipEntitySpecs {

    private static final Map<Integer, ShipEntitySpec> SPECS = new LinkedHashMap<>();
    private static final int[] PRIMARY_EGG_POOL = {2, 3, 4, 5, 11, 12, 18, 19, 20, 21};
    private static final int[] ADVANCED_EGG_POOL = {14, 15, 16, 17, 22, 23, 28, 29, 30, 31, 32, 33, 35, 46, 51, 74};

    public static final ShipEntitySpec DEFAULT = register(2, "EntityDestroyerI", ShipArchetype.DESTROYER);

    static {
        register(3, "EntityDestroyerRo", ShipArchetype.DESTROYER);
        register(4, "EntityDestroyerHa", ShipArchetype.DESTROYER);
        register(5, "EntityDestroyerNi", ShipArchetype.DESTROYER);
        register(11, "EntityHeavyCruiserRi", ShipArchetype.CRUISER);
        register(12, "EntityHeavyCruiserNe", ShipArchetype.CRUISER);
        register(14, "EntityCarrierWo", ShipArchetype.CARRIER);
        register(15, "EntityBattleshipRu", ShipArchetype.BATTLESHIP);
        register(16, "EntityBattleshipTa", ShipArchetype.BATTLESHIP);
        register(17, "EntityBattleshipRe", ShipArchetype.BATTLESHIP);
        register(18, "EntityTransportWa", ShipArchetype.TRANSPORT);
        register(19, "EntitySubmKa", ShipArchetype.SUBMARINE);
        register(20, "EntitySubmYo", ShipArchetype.SUBMARINE);
        register(21, "EntitySubmSo", ShipArchetype.SUBMARINE);
        register(22, "EntityCarrierHime", ShipArchetype.PRINCESS);
        register(23, "EntityAirfieldHime", ShipArchetype.INSTALLATION);
        register(28, "EntityBattleshipHime", ShipArchetype.PRINCESS);
        register(29, "EntityDestroyerHime", ShipArchetype.PRINCESS);
        register(30, "EntityHarbourHime", ShipArchetype.INSTALLATION);
        register(31, "EntityIsolatedHime", ShipArchetype.INSTALLATION);
        register(32, "EntityMidwayHime", ShipArchetype.PRINCESS);
        register(33, "EntityNorthernHime", ShipArchetype.PRINCESS);
        register(35, "EntityCarrierWDemon", ShipArchetype.PRINCESS);
        register(38, "EntityDestroyerShimakaze", ShipArchetype.DESTROYER);
        register(2038, "EntityDestroyerShimakaze", ShipArchetype.DESTROYER);
        register(39, "EntityBattleshipNagato", ShipArchetype.BATTLESHIP);
        register(2039, "EntityBattleshipNagato", ShipArchetype.BATTLESHIP);
        register(40, "EntitySubmU511", ShipArchetype.SUBMARINE);
        register(2040, "EntitySubmU511", ShipArchetype.SUBMARINE);
        register(41, "EntitySubmRo500", ShipArchetype.SUBMARINE);
        register(2041, "EntitySubmRo500", ShipArchetype.SUBMARINE);
        register(46, "EntitySubmHime", ShipArchetype.PRINCESS);
        register(48, "EntityBattleshipYamato", ShipArchetype.BATTLESHIP);
        register(2048, "EntityBattleshipYamato", ShipArchetype.BATTLESHIP);
        register(49, "EntityCarrierKaga", ShipArchetype.CARRIER);
        register(2049, "EntityCarrierKaga", ShipArchetype.CARRIER);
        register(50, "EntityCarrierAkagi", ShipArchetype.CARRIER);
        register(2050, "EntityCarrierAkagi", ShipArchetype.CARRIER);
        register(51, "EntityCAHime", ShipArchetype.PRINCESS);
        register(53, "EntityDestroyerAkatsuki", ShipArchetype.DESTROYER);
        register(2053, "EntityDestroyerAkatsuki", ShipArchetype.DESTROYER);
        register(54, "EntityDestroyerHibiki", ShipArchetype.DESTROYER);
        register(2054, "EntityDestroyerHibiki", ShipArchetype.DESTROYER);
        register(55, "EntityDestroyerIkazuchi", ShipArchetype.DESTROYER);
        register(2055, "EntityDestroyerIkazuchi", ShipArchetype.DESTROYER);
        register(56, "EntityDestroyerInazuma", ShipArchetype.DESTROYER);
        register(2056, "EntityDestroyerInazuma", ShipArchetype.DESTROYER);
        register(58, "EntityCruiserTenryuu", ShipArchetype.CRUISER);
        register(2058, "EntityCruiserTenryuu", ShipArchetype.CRUISER);
        register(59, "EntityCruiserTatsuta", ShipArchetype.CRUISER);
        register(2059, "EntityCruiserTatsuta", ShipArchetype.CRUISER);
        register(60, "EntityCruiserAtago", ShipArchetype.CRUISER);
        register(2060, "EntityCruiserAtago", ShipArchetype.CRUISER);
        register(61, "EntityCruiserTakao", ShipArchetype.CRUISER);
        register(2061, "EntityCruiserTakao", ShipArchetype.CRUISER);
        register(62, "EntityBBKongou", ShipArchetype.BATTLESHIP);
        register(2062, "EntityBBKongou", ShipArchetype.BATTLESHIP);
        register(63, "EntityBBHiei", ShipArchetype.BATTLESHIP);
        register(2063, "EntityBBHiei", ShipArchetype.BATTLESHIP);
        register(64, "EntityBBHaruna", ShipArchetype.BATTLESHIP);
        register(2064, "EntityBBHaruna", ShipArchetype.BATTLESHIP);
        register(65, "EntityBBKirishima", ShipArchetype.BATTLESHIP);
        register(2065, "EntityBBKirishima", ShipArchetype.BATTLESHIP);
        register(74, "EntitySubmHimeNew", ShipArchetype.PRINCESS);
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

    public static ShipEntitySpec resolveEggItem(String itemPath, RandomSource random) {
        if ("smallegg".equals(itemPath)) {
            return randomPrimary(random);
        }

        if ("largeegg".equals(itemPath)) {
            return randomAdvanced(random);
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

    public static Collection<ShipEntitySpec> values() {
        return Collections.unmodifiableCollection(SPECS.values());
    }

    private static ShipEntitySpec register(int eggMeta, String textureStem, ShipArchetype archetype) {
        ShipEntitySpec spec = new ShipEntitySpec(eggMeta, "item.shincolle.shipegg" + eggMeta, textureStem, archetype);
        ShipEntitySpec previous = SPECS.put(eggMeta, spec);

        if (previous != null) {
            throw new IllegalStateException("Duplicate legacy ship egg meta " + eggMeta);
        }

        return spec;
    }
}
