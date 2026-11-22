package com.lulan.shincolle.config;

import com.lulan.shincolle.ShinColle;
import com.mojang.logging.LogUtils;
import net.neoforged.fml.loading.FMLPaths;
import org.slf4j.Logger;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** 1.12.2 ConfigLoot: config/shincolle/loottable.cfg, "chest_ID, item_name, item_meta, weight, chance%, min, max". */
public final class LegacyLootConfig {

    private static final Logger LOGGER = LogUtils.getLogger();
    public static final List<String> DEFAULT_CONTENT = List.of(
            "# Custom Loot Table",
            "#",
            "# format: chest_ID, item_name, item_meta, weight, chance%, min stack size, max stack size",
            "#",
            "# chest_ID: 0:Spawn Bonus Chest, 1:Igloo, 2:Dungeon, 3:Village Blacksmith, 4:Mineshaft,",
            "#           5:Pyramid, 6:Jungle Temple, 7:Nether Bridge, 8:Stronghold, 9:End City",
            "#",
            "# item_meta: -1:random meta, only for shincolle item, 0~N:specific meta",
            "#",
            "# chance%: 0~100",
            "#",
            "# 'rolls' will be random from 1 ~ N/2+1, N = #entries per chestID",
            "# 'bonus_rolls' is fixed at 1",
            "",
            "0,shincolle:Grudge,0,1,100,10,15",
            "0,shincolle:ShipSpawnEgg,2,2,100,1,1",
            "0,shincolle:Ammo,0,1,100,5,8",
            "1,shincolle:Grudge,0,1,100,5,8",
            "1,shincolle:ShipSpawnEgg,0,1,100,1,1",
            "1,shincolle:Ammo,0,1,100,2,3",
            "1,shincolle:InstantConMat,0,1,100,3,5",
            "2,shincolle:MarriageRing,0,4,70,1,1",
            "2,shincolle:TrainingBook,0,4,80,1,3",
            "2,shincolle:ShipSpawnEgg,0,3,100,1,1",
            "2,shincolle:ShipSpawnEgg,1,3,100,1,1",
            "2,shincolle:ShipSpawnEgg,17,1,80,1,1",
            "2,shincolle:ShipSpawnEgg,48,1,80,1,1",
            "3,shincolle:InstantConMat,0,20,100,10,20",
            "3,shincolle:BlockAbyssium,0,10,100,5,10",
            "3,shincolle:BlockPolymetal,0,10,100,5,10",
            "3,shincolle:ShipSpawnEgg,0,5,100,1,1",
            "4,shincolle:TrainingBook,0,6,80,1,3",
            "4,shincolle:MarriageRing,0,6,70,1,1",
            "4,shincolle:ShipSpawnEgg,0,3,100,1,1",
            "4,shincolle:ShipSpawnEgg,1,3,100,1,1",
            "4,shincolle:EquipCannon,-1,8,100,1,1",
            "4,shincolle:EquipAirplane,-1,8,100,1,1",
            "4,shincolle:Torpedo,-1,8,100,1,1",
            "5,shincolle:TrainingBook,0,6,80,1,3",
            "5,shincolle:MarriageRing,0,6,70,1,1",
            "5,shincolle:ShipSpawnEgg,0,3,100,1,1",
            "5,shincolle:ShipSpawnEgg,1,3,100,1,1",
            "5,shincolle:EquipCannon,-1,8,100,1,1",
            "5,shincolle:EquipAirplane,-1,8,100,1,1",
            "5,shincolle:Torpedo,-1,8,100,1,1",
            "6,shincolle:MarriageRing,0,4,70,1,1",
            "6,shincolle:ShipSpawnEgg,1,2,100,1,1",
            "6,shincolle:ShipSpawnEgg,17,1,80,1,1",
            "6,shincolle:ShipSpawnEgg,48,1,80,1,1",
            "7,shincolle:TrainingBook,0,4,80,1,3",
            "7,shincolle:InstantConMat,0,4,100,10,12",
            "7,shincolle:MarriageRing,0,4,70,1,1",
            "7,shincolle:BlockAbyssium,0,4,100,5,15",
            "7,shincolle:BlockPolymetal,0,4,100,5,15",
            "8,shincolle:TrainingBook,0,6,80,1,3",
            "8,shincolle:MarriageRing,0,6,70,1,1",
            "8,shincolle:ShipSpawnEgg,0,3,100,1,1",
            "8,shincolle:ShipSpawnEgg,1,3,100,1,1",
            "8,shincolle:EquipCannon,-1,8,100,1,1",
            "8,shincolle:EquipAirplane,-1,8,100,1,1",
            "8,shincolle:Torpedo,-1,8,100,1,1",
            "9,shincolle:TrainingBook,0,6,80,1,3",
            "9,shincolle:MarriageRing,0,6,70,1,1",
            "9,shincolle:ShipSpawnEgg,0,3,100,1,1",
            "9,shincolle:ShipSpawnEgg,1,3,100,1,1",
            "9,shincolle:EquipCannon,-1,8,100,1,1",
            "9,shincolle:EquipAirplane,-1,8,100,1,1",
            "9,shincolle:Torpedo,-1,8,100,1,1");

    private static Map<Integer, List<Entry>> lootMap;

    private LegacyLootConfig() {
    }

    public static synchronized List<Entry> entries(int chestId) {
        if (lootMap == null) {
            lootMap = load(FMLPaths.CONFIGDIR.get().resolve(ShinColle.MOD_ID).resolve("loottable.cfg"));
        }
        return lootMap.getOrDefault(chestId, List.of());
    }

    private static Map<Integer, List<Entry>> load(Path file) {
        List<String> lines = DEFAULT_CONTENT;
        try {
            if (!Files.exists(file)) {
                Files.createDirectories(file.getParent());
                Files.write(file, DEFAULT_CONTENT, StandardCharsets.UTF_8);
            }
            lines = Files.readAllLines(file, StandardCharsets.UTF_8);
        } catch (IOException e) {
            LOGGER.error("ERROR: load config file: {} fail: {}", file, e.toString());
        }
        return parse(lines);
    }

    /** 1.12.2 ConfigLoot.parse: whitespace removed, 7 comma-separated fields, bad lines discarded. */
    public static Map<Integer, List<Entry>> parse(List<String> lines) {
        Map<Integer, List<Entry>> map = new HashMap<>();
        for (int i = 0; i < 10; i++) {
            map.put(i, new ArrayList<>());
        }

        for (String line : lines) {
            if (isComment(line)) {
                continue;
            }
            String[] strs = line.replaceAll("\\s", "").split(",");
            if (strs.length != 7 || strs[1].isEmpty()) {
                continue;
            }
            try {
                int chestId = Integer.parseInt(strs[0]);
                int meta = Integer.parseInt(strs[2]);
                int weight = Integer.parseInt(strs[3]);
                int chance = Integer.parseInt(strs[4]);
                int min = Integer.parseInt(strs[5]);
                int max = Integer.parseInt(strs[6]);
                List<Entry> list = map.get(chestId);
                if (list != null) {
                    list.add(new Entry(strs[1], meta, weight, chance * 0.01F, min, max));
                }
            } catch (NumberFormatException ignored) {
            }
        }
        return map;
    }

    private static boolean isComment(String str) {
        for (int i = 0; i < str.length(); ++i) {
            if (!Character.isWhitespace(str.charAt(i))) {
                return str.charAt(i) == '#';
            }
        }
        return false;
    }

    public record Entry(String itemName, int itemMeta, int weight, float chance, int min, int max) {
    }
}
