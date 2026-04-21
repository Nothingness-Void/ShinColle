package com.lulan.shincolle.world;

import com.lulan.shincolle.ShinColle;
import com.lulan.shincolle.registry.ModItems;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;
import net.minecraftforge.event.LootTableLoadEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.List;

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
    private static final ResourceLocation VILLAGE_ARMORER = ResourceLocation.fromNamespaceAndPath("minecraft", "chests/village/village_armorer");
    private static final ResourceLocation VILLAGE_TOOLSMITH = ResourceLocation.fromNamespaceAndPath("minecraft", "chests/village/village_toolsmith");
    private static final ResourceLocation VILLAGE_WEAPONSMITH = ResourceLocation.fromNamespaceAndPath("minecraft", "chests/village/village_weaponsmith");
    private static final ResourceLocation SHIPWRECK_SUPPLY = ResourceLocation.fromNamespaceAndPath("minecraft", "chests/shipwreck_supply");
    private static final ResourceLocation SHIPWRECK_TREASURE = ResourceLocation.fromNamespaceAndPath("minecraft", "chests/shipwreck_treasure");
    private static final ResourceLocation UNDERWATER_RUIN_SMALL = ResourceLocation.fromNamespaceAndPath("minecraft", "chests/underwater_ruin_small");
    private static final ResourceLocation UNDERWATER_RUIN_BIG = ResourceLocation.fromNamespaceAndPath("minecraft", "chests/underwater_ruin_big");

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
                .name("shincolle_injected")
                .setRolls(UniformGenerator.between(1.0F, Math.max(1.0F, entries.size() / 2.0F + 1.0F)));

        for (LootEntryDef entry : entries) {
            LootItem.Builder<?> lootEntry = LootItem.lootTableItem(entry.item())
                    .setWeight(entry.weight())
                    .apply(SetItemCountFunction.setCount(UniformGenerator.between(entry.minCount(), entry.maxCount())));
            if (entry.chance() < 1.0F) {
                lootEntry.when(LootItemRandomChanceCondition.randomChance(entry.chance()));
            }
            pool.add(lootEntry);
        }

        event.getTable().addPool(pool.build());
    }

    private static List<LootEntryDef> resolveEntries(ResourceLocation tableId) {
        if (matches(tableId, BONUS_CHEST, IGLOO_CHEST, SHIPWRECK_SUPPLY, UNDERWATER_RUIN_SMALL)) {
            return starterSupplies();
        }
        if (matches(tableId, SIMPLE_DUNGEON, ABANDONED_MINESHAFT, DESERT_PYRAMID, JUNGLE_TEMPLE, SHIPWRECK_TREASURE, UNDERWATER_RUIN_BIG)) {
            return combatCache();
        }
        if (matches(tableId, VILLAGE_ARMORER, VILLAGE_TOOLSMITH, VILLAGE_WEAPONSMITH)) {
            return villageWorkshopCache();
        }
        if (matches(tableId, NETHER_BRIDGE, STRONGHOLD_LIBRARY, STRONGHOLD_CROSSING, STRONGHOLD_CORRIDOR)) {
            return rareExpeditionCache();
        }
        if (matches(tableId, END_CITY_TREASURE)) {
            return endCityCache();
        }
        return List.of();
    }

    private static boolean matches(ResourceLocation tableId, ResourceLocation... candidates) {
        for (ResourceLocation candidate : candidates) {
            if (candidate.equals(tableId)) {
                return true;
            }
        }
        return false;
    }

    private static List<LootEntryDef> starterSupplies() {
        return List.of(
                entry(ModItems.COMBATRATION.get(), 10, 1, 2, 0.80F),
                entry(ModItems.AMMO.get(), 8, 1, 3, 0.75F),
                entry(ModItems.GRUDGE.get(), 6, 1, 2, 0.50F),
                entry(ModItems.ABYSSNUGGET.get(), 5, 1, 3, 0.45F),
                entry(ModItems.ABYSSNUGGET1.get(), 4, 1, 2, 0.30F),
                entry(ModItems.BUCKETREPAIR.get(), 3, 1, 1, 0.22F),
                entry(ModItems.DESKITEMBOOK.get(), 1, 1, 1, 0.05F),
                entry(ModItems.RECIPEPAPER.get(), 1, 1, 1, 0.05F),
                entry(ModItems.OWNERPAPER.get(), 1, 1, 1, 0.04F),
                entry(ModItems.TARGETWRENCH.get(), 1, 1, 1, 0.06F),
                entry(ModItems.POINTERITEM.get(), 1, 1, 1, 0.06F));
    }

    private static List<LootEntryDef> combatCache() {
        return List.of(
                entry(ModItems.GRUDGE.get(), 12, 1, 4, 0.90F),
                entry(ModItems.AMMO.get(), 12, 1, 4, 0.90F),
                entry(ModItems.ABYSSMETAL.get(), 8, 1, 2, 0.60F),
                entry(ModItems.ABYSSMETAL1.get(), 4, 1, 2, 0.28F),
                entry(ModItems.COMBATRATION2.get(), 6, 1, 2, 0.40F),
                entry(ModItems.BUCKETREPAIR.get(), 4, 1, 1, 0.18F),
                entry(ModItems.TRAININGBOOK.get(), 2, 1, 1, 0.10F),
                entry(ModItems.EQUIPRADAR_DISPLAY_ITEMS.get(0).get(), 2, 1, 1, 0.08F),
                entry(ModItems.EQUIPDRUM_DISPLAY_ITEMS.get(0).get(), 2, 1, 1, 0.08F));
    }

    private static List<LootEntryDef> villageWorkshopCache() {
        return List.of(
                entry(ModItems.AMMO.get(), 10, 1, 3, 0.75F),
                entry(ModItems.ABYSSNUGGET.get(), 8, 1, 4, 0.50F),
                entry(ModItems.ABYSSMETAL.get(), 6, 1, 2, 0.28F),
                entry(ModItems.DESKITEMBOOK.get(), 2, 1, 1, 0.16F),
                entry(ModItems.DESKITEMRADAR.get(), 2, 1, 1, 0.12F),
                entry(ModItems.RECIPEPAPER.get(), 2, 1, 1, 0.12F),
                entry(ModItems.OWNERPAPER.get(), 2, 1, 1, 0.10F),
                entry(ModItems.TOYAIRPLANE.get(), 2, 1, 1, 0.10F),
                entry(ModItems.MODERNKIT.get(), 3, 1, 1, 0.12F),
                entry(ModItems.TRAININGBOOK.get(), 3, 1, 1, 0.12F),
                entry(ModItems.EQUIPTURBINE_DISPLAY_ITEMS.get(0).get(), 2, 1, 1, 0.10F),
                entry(ModItems.EQUIPRADAR_DISPLAY_ITEMS.get(1).get(), 2, 1, 1, 0.10F));
    }

    private static List<LootEntryDef> rareExpeditionCache() {
        return List.of(
                entry(ModItems.GRUDGE1.get(), 8, 1, 2, 0.55F),
                entry(ModItems.ABYSSMETAL.get(), 10, 1, 3, 0.75F),
                entry(ModItems.ABYSSMETAL1.get(), 8, 1, 3, 0.55F),
                entry(ModItems.COMBATRATION3.get(), 5, 1, 2, 0.30F),
                entry(ModItems.MODERNKIT.get(), 4, 1, 1, 0.20F),
                entry(ModItems.TRAININGBOOK.get(), 4, 1, 1, 0.20F),
                entry(ModItems.REPAIRGODDESS.get(), 2, 1, 1, 0.12F),
                entry(ModItems.EQUIPCATAPULT_DISPLAY_ITEMS.get(0).get(), 2, 1, 1, 0.10F),
                entry(ModItems.EQUIPTORPEDO_DISPLAY_ITEMS.get(0).get(), 2, 1, 1, 0.10F),
                entry(ModItems.SHIPSPAWNEGG_ITEMS.get(0).get(), 1, 1, 1, 0.04F));
    }

    private static List<LootEntryDef> endCityCache() {
        return List.of(
                entry(ModItems.GRUDGE1.get(), 10, 1, 3, 0.65F),
                entry(ModItems.ABYSSMETAL1.get(), 10, 1, 4, 0.85F),
                entry(ModItems.INSTANTCONMAT.get(), 6, 1, 2, 0.35F),
                entry(ModItems.REPAIRGODDESS.get(), 4, 1, 1, 0.20F),
                entry(ModItems.MARRIAGERING.get(), 2, 1, 1, 0.08F),
                entry(ModItems.SHIPTANK2.get(), 2, 1, 1, 0.06F),
                entry(ModItems.EQUIPCATAPULT_DISPLAY_ITEMS.get(Math.min(1, ModItems.EQUIPCATAPULT_DISPLAY_ITEMS.size() - 1)).get(), 3, 1, 1, 0.14F),
                entry(ModItems.EQUIPRADAR_DISPLAY_ITEMS.get(ModItems.EQUIPRADAR_DISPLAY_ITEMS.size() - 1).get(), 3, 1, 1, 0.14F),
                entry(ModItems.SHIPSPAWNEGG_ITEMS.get(1).get(), 1, 1, 1, 0.06F));
    }

    private static LootEntryDef entry(Item item, int weight, int minCount, int maxCount, float chance) {
        return new LootEntryDef(item, weight, minCount, maxCount, chance);
    }

    private record LootEntryDef(Item item, int weight, int minCount, int maxCount, float chance) {
    }
}
