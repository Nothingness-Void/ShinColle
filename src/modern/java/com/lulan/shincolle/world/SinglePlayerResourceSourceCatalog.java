package com.lulan.shincolle.world;

import com.lulan.shincolle.ShinColle;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Documents the deliberately small single-player resource loop used to close Phase 10.
 */
public final class SinglePlayerResourceSourceCatalog {

    public enum SourceKind {
        WORLDGEN,
        RECIPE,
        BLOCK_LOOT,
        CHEST_LOOT,
        HOSTILE_LOOT,
        ADVANCEMENT
    }

    public record ResourceSource(String resourceKey,
                                 SourceKind kind,
                                 ResourceLocation id,
                                 String packPath,
                                 String note) {
        public boolean hasPackResource() {
            return !this.packPath.isBlank();
        }
    }

    public record ResourceLoopStatus(String resourceKey, List<ResourceSource> sources) {
        public boolean discoverable() {
            return !this.sources.isEmpty();
        }
    }

    public static final String POLYMETAL = "polymetal";
    public static final String ABYSSMETAL = "abyssmetal";
    public static final String GRUDGE = "grudge";
    public static final String AMMO = "ammo";
    public static final String COMBAT_RATION = "combat_ration";
    public static final String FUEL = "fuel";
    public static final String SHIPYARD_BUILD = "shipyard_build";
    public static final String DESK_REFERENCE = "desk_reference";

    private static final Map<String, List<ResourceSource>> SOURCES = createSources();

    private SinglePlayerResourceSourceCatalog() {
    }

    public static List<String> requiredMainlineResources() {
        return List.copyOf(SOURCES.keySet());
    }

    public static List<ResourceSource> sourcesFor(String resourceKey) {
        return SOURCES.getOrDefault(resourceKey, List.of());
    }

    public static List<ResourceLoopStatus> mainlineLoopStatus() {
        List<ResourceLoopStatus> statuses = new ArrayList<>(SOURCES.size());
        SOURCES.forEach((key, sources) -> statuses.add(new ResourceLoopStatus(key, sources)));
        return List.copyOf(statuses);
    }

    public static List<String> deskReferenceLines() {
        List<String> lines = new ArrayList<>();
        for (ResourceLoopStatus status : mainlineLoopStatus()) {
            ResourceSource firstSource = status.sources().isEmpty() ? null : status.sources().get(0);
            lines.add(labelFor(status.resourceKey()) + ": " + (firstSource == null ? "not mapped" : firstSource.note()));
        }
        return List.copyOf(lines);
    }

    private static Map<String, List<ResourceSource>> createSources() {
        LinkedHashMap<String, List<ResourceSource>> sources = new LinkedHashMap<>();
        sources.put(POLYMETAL, List.of(
                data(POLYMETAL, SourceKind.WORLDGEN, "worldgen/configured_feature/polymetal_ore",
                        "data/shincolle/worldgen/configured_feature/polymetal_ore.json",
                        "mine polymetal ore in overworld stone"),
                data(POLYMETAL, SourceKind.WORLDGEN, "worldgen/configured_feature/polymetal_gravel",
                        "data/shincolle/worldgen/configured_feature/polymetal_gravel.json",
                        "collect polymetal gravel deposits near water"),
                data(POLYMETAL, SourceKind.BLOCK_LOOT, "loot_tables/blocks/blockpolymetalore",
                        "data/shincolle/loot_tables/blocks/blockpolymetalore.json",
                        "ore drops polymetallic nodules")));
        sources.put(ABYSSMETAL, List.of(
                data(ABYSSMETAL, SourceKind.RECIPE, "recipes/abyssmetal_from_iron_and_grudge",
                        "data/shincolle/recipes/abyssmetal_from_iron_and_grudge.json",
                        "craft abyssium ingots from iron and grudge"),
                code(ABYSSMETAL, SourceKind.HOSTILE_LOOT, "hostile_loot/abyssmetal",
                        "elite, carrier, battleship, and boss enemies can drop ingots"),
                code(ABYSSMETAL, SourceKind.CHEST_LOOT, "chest_loot/combat_cache",
                        "dungeon, mineshaft, desert temple, jungle temple, shipwreck, and ruin caches can contain ingots")));
        sources.put(GRUDGE, List.of(
                code(GRUDGE, SourceKind.HOSTILE_LOOT, "hostile_loot/grudge",
                        "common hostile ships always drop grudge"),
                code(GRUDGE, SourceKind.CHEST_LOOT, "chest_loot/starter_supplies",
                        "starter and ocean loot caches can contain grudge"),
                data(GRUDGE, SourceKind.RECIPE, "recipes/grudge_from_blockgrudge",
                        "data/shincolle/recipes/grudge_from_blockgrudge.json",
                        "stored grudge blocks unpack into grudge")));
        sources.put(AMMO, List.of(
                data(AMMO, SourceKind.RECIPE, "recipes/ammo",
                        "data/shincolle/recipes/ammo.json",
                        "craft light ammo from iron, gunpowder, and grudge"),
                data(AMMO, SourceKind.RECIPE, "recipes/ammo2",
                        "data/shincolle/recipes/ammo2.json",
                        "craft heavy ammo from light ammo and abyssium"),
                code(AMMO, SourceKind.HOSTILE_LOOT, "hostile_loot/ammo",
                        "common hostile ships always drop light ammo")));
        sources.put(COMBAT_RATION, List.of(
                data(COMBAT_RATION, SourceKind.RECIPE, "recipes/combatration",
                        "data/shincolle/recipes/combatration.json",
                        "craft rice-ball combat rations from wheat, bread, carrot, and grudge"),
                code(COMBAT_RATION, SourceKind.CHEST_LOOT, "chest_loot/starter_supplies",
                        "starter, shipwreck, and underwater ruin caches can contain rations")));
        sources.put(FUEL, List.of(
                data(FUEL, SourceKind.RECIPE, "recipes/blocksmallshipyard",
                        "data/shincolle/recipes/blocksmallshipyard.json",
                        "lava buckets fuel the first shipyard loop"),
                data(FUEL, SourceKind.RECIPE, "recipes/shiptank",
                        "data/shincolle/recipes/shiptank.json",
                        "ship tanks move lava into route and shipyard logistics")));
        sources.put(SHIPYARD_BUILD, List.of(
                data(SHIPYARD_BUILD, SourceKind.RECIPE, "recipes/blocksmallshipyard",
                        "data/shincolle/recipes/blocksmallshipyard.json",
                        "craft the small shipyard from grudge, obsidian, and lava"),
                code(SHIPYARD_BUILD, SourceKind.HOSTILE_LOOT, "hostile_loot/boss_bonus",
                        "boss encounters can drop instant construction material and recoverable ship eggs"),
                data(SHIPYARD_BUILD, SourceKind.ADVANCEMENT, "advancements/progression/build_small_shipyard",
                        "data/shincolle/advancements/progression/build_small_shipyard.json",
                        "progression tells the player when the small shipyard is ready")));
        sources.put(DESK_REFERENCE, List.of(
                data(DESK_REFERENCE, SourceKind.RECIPE, "recipes/deskitembook",
                        "data/shincolle/recipes/deskitembook.json",
                        "craft the desk logbook from grudge and a writable book"),
                data(DESK_REFERENCE, SourceKind.RECIPE, "recipes/deskitemradar",
                        "data/shincolle/recipes/deskitemradar.json",
                        "craft the portable radar from grudge and a compass"),
                data(DESK_REFERENCE, SourceKind.RECIPE, "recipes/blockdesk",
                        "data/shincolle/recipes/blockdesk.json",
                        "combine book, radar, wool, and obsidian into the desk")));
        return Collections.unmodifiableMap(sources);
    }

    private static ResourceSource data(String resourceKey, SourceKind kind, String idPath, String packPath, String note) {
        return new ResourceSource(resourceKey, kind, ResourceLocation.fromNamespaceAndPath(ShinColle.MOD_ID, idPath), packPath, note);
    }

    private static ResourceSource code(String resourceKey, SourceKind kind, String idPath, String note) {
        return new ResourceSource(resourceKey, kind, ResourceLocation.fromNamespaceAndPath(ShinColle.MOD_ID, idPath), "", note);
    }

    private static String labelFor(String resourceKey) {
        return switch (resourceKey) {
            case POLYMETAL -> "Polymetal";
            case ABYSSMETAL -> "Abyssmetal";
            case GRUDGE -> "Grudge";
            case AMMO -> "Ammo";
            case COMBAT_RATION -> "Combat ration";
            case FUEL -> "Fuel";
            case SHIPYARD_BUILD -> "Shipyard build";
            case DESK_REFERENCE -> "Desk reference";
            default -> resourceKey;
        };
    }
}
