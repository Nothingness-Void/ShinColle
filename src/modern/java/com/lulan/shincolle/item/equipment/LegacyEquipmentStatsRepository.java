package com.lulan.shincolle.item.equipment;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import javax.annotation.Nullable;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public final class LegacyEquipmentStatsRepository {

    private static final String RESOURCE_PATH = "/data/shincolle/porting/legacy_equipment_stats.json";
    private static final LoadedData LOADED_DATA = load();

    private LegacyEquipmentStatsRepository() {
    }

    public static @Nullable float[] getMain(String key) {
        return LOADED_DATA.mainStats().get(key);
    }

    public static @Nullable MiscData getMisc(String key) {
        return LOADED_DATA.miscStats().get(key);
    }

    private static LoadedData load() {
        try (InputStream stream = LegacyEquipmentStatsRepository.class.getResourceAsStream(RESOURCE_PATH)) {
            if (stream == null) {
                throw new IllegalStateException("Missing legacy equipment stats resource: " + RESOURCE_PATH);
            }

            JsonObject root = JsonParser.parseReader(new InputStreamReader(stream, StandardCharsets.UTF_8)).getAsJsonObject();
            Map<String, float[]> mainStats = new HashMap<>();
            Map<String, MiscData> miscStats = new HashMap<>();

            readMainStats(root.getAsJsonObject("main"), mainStats);
            readMiscStats(root.getAsJsonObject("misc"), miscStats);

            return new LoadedData(Collections.unmodifiableMap(mainStats), Collections.unmodifiableMap(miscStats));
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to load legacy equipment stats resource", exception);
        }
    }

    private static void readMainStats(JsonObject rawMainStats, Map<String, float[]> output) {
        for (Map.Entry<String, JsonElement> entry : rawMainStats.entrySet()) {
            JsonArray array = entry.getValue().getAsJsonArray();
            float[] stats = new float[array.size()];

            for (int i = 0; i < array.size(); i++) {
                stats[i] = array.get(i).getAsFloat();
            }

            output.put(entry.getKey(), stats);
        }
    }

    private static void readMiscStats(JsonObject rawMiscStats, Map<String, MiscData> output) {
        for (Map.Entry<String, JsonElement> entry : rawMiscStats.entrySet()) {
            JsonArray array = entry.getValue().getAsJsonArray();
            output.put(entry.getKey(), new MiscData(
                    array.get(0).getAsInt(),
                    array.get(1).getAsString(),
                    array.get(2).getAsInt(),
                    array.get(3).getAsInt(),
                    array.get(4).getAsInt(),
                    array.get(5).getAsInt()));
        }
    }

    private record LoadedData(Map<String, float[]> mainStats, Map<String, MiscData> miscStats) {
    }

    public record MiscData(int carrierGroup, String rareType, int rareMean, int developNum, int developMaterial, int enchantType) {

        public boolean notForCarrier() {
            return this.carrierGroup == 1;
        }

        public boolean carrierOnly() {
            return this.carrierGroup == 3;
        }
    }
}
