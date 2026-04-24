package com.lulan.shincolle.world;

import com.lulan.shincolle.ShinColle;
import com.lulan.shincolle.registry.ModBlocks;
import com.lulan.shincolle.registry.ModItems;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;
import net.minecraftforge.event.LootTableLoadEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.RegistryObject;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.function.Supplier;

@Mod.EventBusSubscriber(modid = ShinColle.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
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

        LootPool.Builder pool = LootPool.lootPool()
                .name("shincollePool")
                .setRolls(UniformGenerator.between(1.0F, entries.size() / 2.0F + 1.0F))
                .setBonusRolls(ConstantValue.exactly(1.0F));

        for (LootEntryDef entry : entries) {
            LootItem.Builder<?> lootEntry = LootItem.lootTableItem(entry.item().get())
                    .setWeight(entry.weight())
                    .apply(SetItemCountFunction.setCount(UniformGenerator.between(entry.minCount(), entry.maxCount())))
                    .when(LootItemRandomChanceCondition.randomChance(entry.chance()));
            pool.add(lootEntry);
        }

        event.getTable().addPool(pool.build());
    }

    public static List<String> legacySourceKeysForTesting(ResourceLocation tableId) {
        LinkedHashSet<String> keys = new LinkedHashSet<>();
        for (LootEntryDef entry : resolveEntries(tableId)) {
            keys.add(entry.legacySourceKey());
        }
        return List.copyOf(keys);
    }

    public static int concreteEntryCountForTesting(ResourceLocation tableId) {
        return resolveEntries(tableId).size();
    }

    private static List<LootEntryDef> resolveEntries(ResourceLocation tableId) {
        if (BONUS_CHEST.equals(tableId)) {
            return bonusChest();
        }
        if (IGLOO_CHEST.equals(tableId)) {
            return iglooChest();
        }
        if (SIMPLE_DUNGEON.equals(tableId)) {
            return simpleDungeon();
        }
        if (VILLAGE_WEAPONSMITH.equals(tableId)) {
            return villageBlacksmith();
        }
        if (ABANDONED_MINESHAFT.equals(tableId)) {
            return randomEquipmentChest();
        }
        if (DESERT_PYRAMID.equals(tableId)) {
            return randomEquipmentChest();
        }
        if (JUNGLE_TEMPLE.equals(tableId)) {
            return jungleTemple();
        }
        if (NETHER_BRIDGE.equals(tableId)) {
            return netherBridge();
        }
        if (STRONGHOLD_LIBRARY.equals(tableId) || STRONGHOLD_CROSSING.equals(tableId) || STRONGHOLD_CORRIDOR.equals(tableId)) {
            return randomEquipmentChest();
        }
        if (END_CITY_TREASURE.equals(tableId)) {
            return randomEquipmentChest();
        }
        return List.of();
    }

    private static List<LootEntryDef> bonusChest() {
        return List.of(
                entry("shincolle:Grudge:0", ModItems.GRUDGE::get, 1, 10, 15, 1.0F),
                entry("shincolle:ShipSpawnEgg:2", shipEgg(2), 2, 1, 1, 1.0F),
                entry("shincolle:Ammo:0", ModItems.AMMO::get, 1, 5, 8, 1.0F));
    }

    private static List<LootEntryDef> iglooChest() {
        return List.of(
                entry("shincolle:Grudge:0", ModItems.GRUDGE::get, 1, 5, 8, 1.0F),
                entry("shincolle:ShipSpawnEgg:0", shipEgg(0), 1, 1, 1, 1.0F),
                entry("shincolle:Ammo:0", ModItems.AMMO::get, 1, 2, 3, 1.0F),
                entry("shincolle:InstantConMat:0", ModItems.INSTANTCONMAT::get, 1, 3, 5, 1.0F));
    }

    private static List<LootEntryDef> simpleDungeon() {
        return List.of(
                entry("shincolle:MarriageRing:0", ModItems.MARRIAGERING::get, 4, 1, 1, 0.70F),
                entry("shincolle:TrainingBook:0", ModItems.TRAININGBOOK::get, 4, 1, 3, 0.80F),
                entry("shincolle:ShipSpawnEgg:0", shipEgg(0), 3, 1, 1, 1.0F),
                entry("shincolle:ShipSpawnEgg:1", shipEgg(1), 3, 1, 1, 1.0F),
                entry("shincolle:ShipSpawnEgg:17", shipEgg(17), 1, 1, 1, 0.80F),
                entry("shincolle:ShipSpawnEgg:48", shipEgg(48), 1, 1, 1, 0.80F));
    }

    private static List<LootEntryDef> villageBlacksmith() {
        return List.of(
                entry("shincolle:InstantConMat:0", ModItems.INSTANTCONMAT::get, 20, 10, 20, 1.0F),
                entry("shincolle:BlockAbyssium:0", ModBlocks.BLOCK_ABYSSIUM::get, 10, 5, 10, 1.0F),
                entry("shincolle:BlockPolymetal:0", ModBlocks.BLOCK_POLYMETAL::get, 10, 5, 10, 1.0F),
                entry("shincolle:ShipSpawnEgg:0", shipEgg(0), 5, 1, 1, 1.0F));
    }

    private static List<LootEntryDef> randomEquipmentChest() {
        List<LootEntryDef> entries = new ArrayList<>();
        entries.add(entry("shincolle:TrainingBook:0", ModItems.TRAININGBOOK::get, 6, 1, 3, 0.80F));
        entries.add(entry("shincolle:MarriageRing:0", ModItems.MARRIAGERING::get, 6, 1, 1, 0.70F));
        entries.add(entry("shincolle:ShipSpawnEgg:0", shipEgg(0), 3, 1, 1, 1.0F));
        entries.add(entry("shincolle:ShipSpawnEgg:1", shipEgg(1), 3, 1, 1, 1.0F));
        entries.addAll(legacyRandomMeta("shincolle:EquipCannon:-1", ModItems.EQUIPCANNON_DISPLAY_ITEMS, 8, 1, 1, 1.0F));
        entries.addAll(legacyRandomMeta("shincolle:EquipAirplane:-1", ModItems.EQUIPAIRPLANE_DISPLAY_ITEMS, 8, 1, 1, 1.0F));
        entries.addAll(legacyRandomMeta("shincolle:Torpedo:-1", ModItems.EQUIPTORPEDO_DISPLAY_ITEMS, 8, 1, 1, 1.0F));
        return List.copyOf(entries);
    }

    private static List<LootEntryDef> jungleTemple() {
        return List.of(
                entry("shincolle:MarriageRing:0", ModItems.MARRIAGERING::get, 4, 1, 1, 0.70F),
                entry("shincolle:ShipSpawnEgg:1", shipEgg(1), 2, 1, 1, 1.0F),
                entry("shincolle:ShipSpawnEgg:17", shipEgg(17), 1, 1, 1, 0.80F),
                entry("shincolle:ShipSpawnEgg:48", shipEgg(48), 1, 1, 1, 0.80F));
    }

    private static List<LootEntryDef> netherBridge() {
        return List.of(
                entry("shincolle:TrainingBook:0", ModItems.TRAININGBOOK::get, 4, 1, 3, 0.80F),
                entry("shincolle:InstantConMat:0", ModItems.INSTANTCONMAT::get, 4, 10, 12, 1.0F),
                entry("shincolle:MarriageRing:0", ModItems.MARRIAGERING::get, 4, 1, 1, 0.70F),
                entry("shincolle:BlockAbyssium:0", ModBlocks.BLOCK_ABYSSIUM::get, 4, 5, 15, 1.0F),
                entry("shincolle:BlockPolymetal:0", ModBlocks.BLOCK_POLYMETAL::get, 4, 5, 15, 1.0F));
    }

    private static List<LootEntryDef> legacyRandomMeta(String legacySourceKey, List<RegistryObject<Item>> items,
                                                       int weight, int minCount, int maxCount, float chance) {
        List<LootEntryDef> entries = new ArrayList<>();
        // 1.12 used one metadata item plus SetMetadata(-1); 1.20 stores those legacy metas as separate registered items.
        int splitWeight = Math.max(1, Math.round((float) weight / Math.max(1, items.size())));
        for (RegistryObject<Item> item : items) {
            entries.add(entry(legacySourceKey, item::get, splitWeight, minCount, maxCount, chance));
        }
        return entries;
    }

    private static Supplier<Item> shipEgg(int legacyMeta) {
        String itemPath = switch (legacyMeta) {
            case 0 -> "smallegg";
            case 1 -> "largeegg";
            default -> "shipegg" + legacyMeta;
        };

        for (RegistryObject<Item> item : ModItems.SHIPSPAWNEGG_ITEMS) {
            if (item.getId().getPath().equals(itemPath)) {
                return item::get;
            }
        }
        throw new IllegalStateException("Missing 1.12 ShipSpawnEgg meta mapping for " + legacyMeta);
    }

    private static LootEntryDef entry(String legacySourceKey, Supplier<? extends ItemLike> item,
                                      int weight, int minCount, int maxCount, float chance) {
        return new LootEntryDef(legacySourceKey, item, weight, minCount, maxCount, chance);
    }

    private record LootEntryDef(String legacySourceKey, Supplier<? extends ItemLike> item, int weight,
                                int minCount, int maxCount, float chance) {
    }
}
