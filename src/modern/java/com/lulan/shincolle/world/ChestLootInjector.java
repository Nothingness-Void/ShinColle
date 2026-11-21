package com.lulan.shincolle.world;

import com.lulan.shincolle.ShinColle;
import com.lulan.shincolle.config.LegacyLootConfig;
import com.lulan.shincolle.item.equipment.LegacyEquipmentFamily;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;
import net.neoforged.neoforge.event.LootTableLoadEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.common.EventBusSubscriber;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.Supplier;

@EventBusSubscriber(modid = ShinColle.MOD_ID, bus = EventBusSubscriber.Bus.GAME)
public final class ChestLootInjector {

    private static final ResourceLocation BONUS_CHEST = ResourceLocation.fromNamespaceAndPath("minecraft", "chests/spawn_bonus_chest");
    private static final ResourceLocation IGLOO_CHEST = ResourceLocation.fromNamespaceAndPath("minecraft", "chests/igloo_chest");
    private static final ResourceLocation SIMPLE_DUNGEON = ResourceLocation.fromNamespaceAndPath("minecraft", "chests/simple_dungeon");
    private static final ResourceLocation ABANDONED_MINESHAFT = ResourceLocation.fromNamespaceAndPath("minecraft", "chests/abandoned_mineshaft");
    private static final ResourceLocation DESERT_PYRAMID = ResourceLocation.fromNamespaceAndPath("minecraft", "chests/desert_pyramid");
    private static final ResourceLocation JUNGLE_TEMPLE = ResourceLocation.fromNamespaceAndPath("minecraft", "chests/jungle_temple");
    private static final ResourceLocation NETHER_BRIDGE = ResourceLocation.fromNamespaceAndPath("minecraft", "chests/nether_bridge");
    private static final ResourceLocation STRONGHOLD_LIBRARY = ResourceLocation.fromNamespaceAndPath("minecraft", "chests/stronghold_library");
    private static final ResourceLocation STRONGHOLD_CROSSING = ResourceLocation.fromNamespaceAndPath("minecraft", "chests/stronghold_crossing");
    private static final ResourceLocation STRONGHOLD_CORRIDOR = ResourceLocation.fromNamespaceAndPath("minecraft", "chests/stronghold_corridor");
    private static final ResourceLocation END_CITY_TREASURE = ResourceLocation.fromNamespaceAndPath("minecraft", "chests/end_city_treasure");
    private static final ResourceLocation VILLAGE_WEAPONSMITH = ResourceLocation.fromNamespaceAndPath("minecraft", "chests/village/village_weaponsmith");

    private ChestLootInjector() {
    }

    @SubscribeEvent
    public static void onLootTableLoad(LootTableLoadEvent event) {
        ResourceLocation tableId = event.getName();
        if (tableId == null) {
            return;
        }

        List<LootEntryDef> entries = resolveEntries(tableId);
        if (entries.isEmpty()) {
            return;
        }

        // 1.12.2 ChestLootTable: rolls 1 ~ N/2+1 (integer division, N = resolved ConfigLoot entries), bonus rolls 1.
        int maxRolls = entries.size() / 2 + 1;
        LootPool.Builder pool = LootPool.lootPool()
                .name("shincollePool")
                .setRolls(UniformGenerator.between(1.0F, maxRolls))
                .setBonusRolls(ConstantValue.exactly(1.0F));

        // 1.12 random meta was one weighted entry with SetMetadata(0~N); 1.20 splits metas into separate items,
        // so every weight is scaled by the lcm of variant counts to keep the same per-entry probability.
        int scale = 1;
        for (LootEntryDef entry : entries) {
            scale = lcm(scale, entry.items().size());
        }

        for (LootEntryDef entry : entries) {
            int variantWeight = entry.weight() * (scale / entry.items().size());
            for (Supplier<? extends ItemLike> item : entry.items()) {
                LootItem.Builder<?> lootEntry = LootItem.lootTableItem(item.get())
                        .setWeight(variantWeight)
                        .apply(SetItemCountFunction.setCount(UniformGenerator.between(entry.minCount(), entry.maxCount())))
                        .when(LootItemRandomChanceCondition.randomChance(entry.chance()));
                pool.add(lootEntry);
            }
        }

        event.getTable().addPool(pool.build());
    }

    public static List<String> legacySourceKeysForTesting(ResourceLocation tableId) {
        List<String> keys = new ArrayList<>();
        for (LootEntryDef entry : resolveEntries(tableId)) {
            keys.add(entry.legacySourceKey());
        }
        return List.copyOf(keys);
    }

    public static int concreteEntryCountForTesting(ResourceLocation tableId) {
        int count = 0;
        for (LootEntryDef entry : resolveEntries(tableId)) {
            count += entry.items().size();
        }
        return count;
    }

    public static int maxRollsForTesting(ResourceLocation tableId) {
        return resolveEntries(tableId).size() / 2 + 1;
    }

    private static List<LootEntryDef> resolveEntries(ResourceLocation tableId) {
        int chestId = legacyChestId(tableId);
        if (chestId < 0) {
            return List.of();
        }

        List<LootEntryDef> entries = new ArrayList<>();
        for (LegacyLootConfig.Entry entry : LegacyLootConfig.entries(chestId)) {
            List<Supplier<? extends ItemLike>> items = resolveLegacyItem(entry.itemName(), entry.itemMeta());
            if (items.isEmpty()) {
                continue;
            }
            entries.add(new LootEntryDef(entry.itemName() + ":" + entry.itemMeta(), items, entry.weight(),
                    entry.min(), entry.max(), entry.chance()));
        }
        return entries;
    }

    /** chest_ID of 1.12.2 ChestLootTable.editLoot; village blacksmith maps to the 1.20 weaponsmith table. */
    private static int legacyChestId(ResourceLocation tableId) {
        if (BONUS_CHEST.equals(tableId)) {
            return 0;
        }
        if (IGLOO_CHEST.equals(tableId)) {
            return 1;
        }
        if (SIMPLE_DUNGEON.equals(tableId)) {
            return 2;
        }
        if (VILLAGE_WEAPONSMITH.equals(tableId)) {
            return 3;
        }
        if (ABANDONED_MINESHAFT.equals(tableId)) {
            return 4;
        }
        if (DESERT_PYRAMID.equals(tableId)) {
            return 5;
        }
        if (JUNGLE_TEMPLE.equals(tableId)) {
            return 6;
        }
        if (NETHER_BRIDGE.equals(tableId)) {
            return 7;
        }
        if (STRONGHOLD_LIBRARY.equals(tableId) || STRONGHOLD_CROSSING.equals(tableId) || STRONGHOLD_CORRIDOR.equals(tableId)) {
            return 8;
        }
        if (END_CITY_TREASURE.equals(tableId)) {
            return 9;
        }
        return -1;
    }

    /**
     * 1.12.2 Item.getByNameOrId (ResourceLocation lower-cases the name) plus SetMetadata.
     * Unresolved names return an empty list and are skipped like the legacy null item.
     */
    private static List<Supplier<? extends ItemLike>> resolveLegacyItem(String itemName, int meta) {
        ResourceLocation id = ResourceLocation.tryParse(itemName.toLowerCase(Locale.ROOT));
        if (id == null) {
            return List.of();
        }

        if (!ShinColle.MOD_ID.equals(id.getNamespace())) {
            Item item = BuiltInRegistries.ITEM.get(id);
            return item == null || item == Items.AIR ? List.of() : List.of(() -> item);
        }

        String path = id.getPath();
        if ("shipspawnegg".equals(path)) {
            String eggPath = meta <= 0 ? "smallegg" : meta == 1 ? "largeegg" : "shipegg" + meta;
            Item egg = modItem(eggPath);
            return egg == null ? List.of() : List.of(() -> egg);
        }

        for (LegacyEquipmentFamily family : LegacyEquipmentFamily.values()) {
            if (family.registryName(0).equals(path)) {
                List<Supplier<? extends ItemLike>> variants = new ArrayList<>();
                if (meta == -1) {
                    for (int i = 0; i < family.variantCount(); i++) {
                        Item variant = modItem(family.registryName(i));
                        if (variant != null) {
                            variants.add(() -> variant);
                        }
                    }
                } else if (meta >= 0 && meta < family.variantCount()) {
                    Item variant = modItem(family.registryName(meta));
                    if (variant != null) {
                        variants.add(() -> variant);
                    }
                }
                return variants;
            }
        }

        Item base = modItem(path);
        if (base == null) {
            return List.of();
        }
        if (meta > 0) {
            Item variant = modItem(path + meta);
            return variant == null ? List.of(() -> base) : List.of(() -> variant);
        }
        if (meta == -1) {
            List<Supplier<? extends ItemLike>> variants = new ArrayList<>();
            variants.add(() -> base);
            for (int i = 1; ; i++) {
                Item variant = modItem(path + i);
                if (variant == null) {
                    break;
                }
                variants.add(() -> variant);
            }
            return variants;
        }
        return List.of(() -> base);
    }

    private static @Nullable Item modItem(String path) {
        Item item = BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath(ShinColle.MOD_ID, path));
        return item == null || item == Items.AIR ? null : item;
    }

    private static int lcm(int a, int b) {
        int x = a;
        int y = b;
        while (y != 0) {
            int t = x % y;
            x = y;
            y = t;
        }
        return a / x * b;
    }

    private record LootEntryDef(String legacySourceKey, List<Supplier<? extends ItemLike>> items, int weight,
                                int minCount, int maxCount, float chance) {
    }
}
