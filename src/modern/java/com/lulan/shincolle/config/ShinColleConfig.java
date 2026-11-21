package com.lulan.shincolle.config;

import net.minecraftforge.common.ForgeConfigSpec;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 1.12.2 ConfigHandler parity backed by ForgeConfigSpec (COMMON + CLIENT),
 * with safe pre-load fallbacks and deterministic GameTest overrides.
 */
public final class ShinColleConfig {

    public static final ForgeConfigSpec COMMON_SPEC;
    public static final ForgeConfigSpec CLIENT_SPEC;
    public static final Common COMMON;
    public static final Client CLIENT;

    private static final Map<String, Object> TEST_OVERRIDES = new ConcurrentHashMap<>();

    static {
        ForgeConfigSpec.Builder commonBuilder = new ForgeConfigSpec.Builder();
        COMMON = new Common(commonBuilder);
        COMMON_SPEC = commonBuilder.build();

        ForgeConfigSpec.Builder clientBuilder = new ForgeConfigSpec.Builder();
        CLIENT = new Client(clientBuilder);
        CLIENT_SPEC = clientBuilder.build();
    }

    private ShinColleConfig() {
    }

    public static void setTestOverride(String key, Object value) {
        if (key == null || value == null) {
            return;
        }
        TEST_OVERRIDES.put(key, value);
    }

    public static void clearTestOverrides() {
        TEST_OVERRIDES.clear();
    }

    public static boolean friendlyFire() {
        return getBoolean("friendlyFire", COMMON_SPEC, COMMON.friendlyFire, true);
    }

    public static int shipAttackPlayer() {
        return getInt("shipAttackPlayer", COMMON_SPEC, COMMON.shipAttackPlayer, 0);
    }

    public static boolean mobAttackPlayer() {
        return getBoolean("mobAttackPlayer", COMMON_SPEC, COMMON.mobAttackPlayer, true);
    }

    public static int dmgSvS() {
        return getInt("dmgSvS", COMMON_SPEC, COMMON.dmgSvS, 100);
    }

    public static boolean easyMode() {
        return getBoolean("easyMode", COMMON_SPEC, COMMON.easyMode, false);
    }

    public static int kaitaiAmountSmall() {
        return getInt("kaitaiAmountSmall", COMMON_SPEC, COMMON.kaitaiAmountSmall, 20);
    }

    public static int kaitaiAmountLarge() {
        return getInt("kaitaiAmountLarge", COMMON_SPEC, COMMON.kaitaiAmountLarge, 20);
    }

    public static boolean polyAsMn() {
        return getBoolean("polyAsMn", COMMON_SPEC, COMMON.polyAsMn, false);
    }

    public static int radarUpdateTicks() {
        return getInt("radarUpdate", COMMON_SPEC, COMMON.radarUpdate, 64);
    }

    public static int closeGuiDist() {
        return getInt("closeGuiDist", COMMON_SPEC, COMMON.closeGuiDist, 64);
    }

    public static int bossCooldown() {
        return getInt("bossCooldown", COMMON_SPEC, COMMON.bossCooldown, 4800);
    }

    public static int teamCooldown() {
        return getInt("teamCooldown", COMMON_SPEC, COMMON.teamCooldown, 6000);
    }

    public static int shipNumPerPage() {
        return getInt("shipNumPerPage", COMMON_SPEC, COMMON.shipNumPerPage, 5);
    }

    public static int pairDistChest() {
        return getInt("pairDistChest", COMMON_SPEC, COMMON.pairDistChest, 16);
    }

    public static int pairDistWp() {
        return getInt("pairDistWp", COMMON_SPEC, COMMON.pairDistWp, 48);
    }

    public static int spawnBossNum() {
        return getInt("spawnBossNum", COMMON_SPEC, COMMON.spawnBossNum, 2);
    }

    public static int spawnMobNum() {
        return getInt("spawnMobNum", COMMON_SPEC, COMMON.spawnMobNum, 4);
    }

    public static int despawnBoss() {
        return getInt("despawnBoss", COMMON_SPEC, COMMON.despawnBoss, 12000);
    }

    public static int despawnMinion() {
        return getInt("despawnMinion", COMMON_SPEC, COMMON.despawnMinion, 600);
    }

    public static int baseCaressMorale() {
        return getInt("baseCaressMorale", COMMON_SPEC, COMMON.baseCaressMorale, 20);
    }

    public static int modernLimit() {
        return getInt("modernLimit", COMMON_SPEC, COMMON.modernLimit, 3);
    }

    public static int maxDmgOnPlayer() {
        return getInt("maxDmgOnPlayer", COMMON_SPEC, COMMON.maxDmgOnPlayer, 59);
    }

    /** 1.12.2 DropRate_Grudge. */
    public static double dropGrudge() {
        return getDouble("dropGrudge", COMMON_SPEC, COMMON.dropGrudge, 1.0D);
    }

    /** 1.12.2 Despawn_Egg, -1 = do NOT despawn. */
    public static int despawnEgg() {
        return getInt("despawnEgg", COMMON_SPEC, COMMON.despawnEgg, 12000);
    }

    /** 1.12.2 Tile_SmallShipyard: max fuel storage, build speed, fuel magnification. */
    public static double[] tileShipyardSmall() {
        return doubleArray("tileShipyardSmall", COMMON.tileShipyardSmall, new double[]{460800D, 48D, 1D});
    }

    /** 1.12.2 Tile_LargeShipyard: max fuel storage, build speed, fuel magnification. */
    public static double[] tileShipyardLarge() {
        return doubleArray("tileShipyardLarge", COMMON.tileShipyardLarge, new double[]{1382400D, 48D, 1D});
    }

    /** 1.12.2 Tile_VolCore: max fuel storage, fuel consume speed, fuel value per grudge item. */
    public static double[] tileVolCore() {
        return doubleArray("tileVolCore", COMMON.tileVolCore, new double[]{9600D, 16D, 240D});
    }

    /** 1.12.2 Polymetal_Gravel: polymetallic gravel clusters in one chunk. */
    public static int polyGravelBaseRate() {
        return getInt("polyGravelBaseRate", COMMON_SPEC, COMMON.polyGravelBaseRate, 4);
    }

    /** 1.12.2 Polymetal_Gravel_Replace: stone, gravel, sand, dirt. */
    public static boolean[] polyGravelBaseBlock() {
        boolean[] fallback = new boolean[]{true, true, false, false};
        Object override = TEST_OVERRIDES.get("polyGravelBaseBlock");
        if (override instanceof boolean[] bools && bools.length >= 4) {
            return Arrays.copyOf(bools, 4);
        }
        List<? extends Boolean> loaded = null;
        if (COMMON_SPEC.isLoaded()) {
            try {
                loaded = COMMON.polyGravelBaseBlock.get();
            } catch (IllegalStateException ignored) {
            }
        }
        if (loaded == null || loaded.size() < 4) {
            return fallback;
        }
        boolean[] values = new boolean[4];
        for (int i = 0; i < 4; i++) {
            values[i] = loaded.get(i);
        }
        return values;
    }

    public static boolean checkRing() {
        return getBoolean("checkRing", COMMON_SPEC, COMMON.checkRing, true);
    }

    /** 1.12.2 Limit_MobSpawnNumber: [max mob count, spawn chance %, groups, min per group, max per group]. */
    public static int[] mobSpawn() {
        return intArray("mobSpawn", COMMON.mobSpawn, new int[]{50, 10, 1, 1, 1});
    }

    static int[] intArray(String key, ForgeConfigSpec.ConfigValue<List<? extends Number>> value, int[] fallback) {
        Object override = TEST_OVERRIDES.get(key);
        if (override instanceof int[] ints && ints.length >= fallback.length) {
            return Arrays.copyOf(ints, fallback.length);
        }
        List<? extends Number> loaded = null;
        if (COMMON_SPEC.isLoaded()) {
            try {
                loaded = value.get();
            } catch (IllegalStateException ignored) {
            }
        }
        if (loaded == null || loaded.size() < fallback.length) {
            return fallback.clone();
        }
        int[] values = new int[fallback.length];
        for (int i = 0; i < values.length; i++) {
            values[i] = loaded.get(i).intValue();
        }
        return values;
    }

    static double[] doubleArray(String key, ForgeConfigSpec.ConfigValue<List<? extends Number>> value, double[] fallback) {
        Object override = TEST_OVERRIDES.get(key);
        if (override instanceof double[] doubles && doubles.length >= fallback.length) {
            return Arrays.copyOf(doubles, fallback.length);
        }
        List<? extends Number> loaded = null;
        if (COMMON_SPEC.isLoaded()) {
            try {
                loaded = value.get();
            } catch (IllegalStateException ignored) {
            }
        }
        if (loaded == null || loaded.size() < fallback.length) {
            return fallback.clone();
        }
        double[] values = new double[fallback.length];
        for (int i = 0; i < values.length; i++) {
            values[i] = loaded.get(i).doubleValue();
        }
        return values;
    }

    public static int[] ringAbility() {
        Object override = TEST_OVERRIDES.get("ringAbility");
        if (override instanceof int[] ints && ints.length >= 5) {
            return Arrays.copyOf(ints, 5);
        }
        if (override instanceof List<?> list && list.size() >= 5) {
            int[] values = new int[5];
            for (int i = 0; i < 5; i++) {
                values[i] = list.get(i) instanceof Number num ? num.intValue() : 0;
            }
            return values;
        }
        if (COMMON_SPEC.isLoaded()) {
            try {
                List<? extends Integer> loaded = COMMON.ringAbility.get();
                if (loaded != null && loaded.size() >= 5) {
                    return new int[] {
                            loaded.get(0),
                            loaded.get(1),
                            loaded.get(2),
                            loaded.get(3),
                            loaded.get(4)
                    };
                }
            } catch (IllegalStateException ignored) {
            }
        }
        return new int[] {0, 6, 30, 20, 12};
    }

    public static boolean enableMetamorphSkill() {
        return getBoolean("enableMetamorphSkill", COMMON_SPEC, COMMON.enableMetamorphSkill, true);
    }

    public static double expGainPlayerSkill() {
        return getDouble("expGainPlayerSkill", COMMON_SPEC, COMMON.expGainPlayerSkill, 6.0D);
    }

    public static double morphHpRatio() {
        return getDouble("morphHpRatio", COMMON_SPEC, COMMON.morphHpRatio, 0.1D);
    }

    public static double morphDmgTakenRatio() {
        return getDouble("morphDmgTakenRatio", COMMON_SPEC, COMMON.morphDmgTakenRatio, 0.2D);
    }

    public static boolean canFlare() {
        return getBoolean("canFlare", CLIENT_SPEC, CLIENT.canFlare, true);
    }

    public static boolean canSearchlight() {
        return getBoolean("canSearchlight", CLIENT_SPEC, CLIENT.canSearchlight, true);
    }

    public static boolean canTeleport() {
        return getBoolean("canTeleport", CLIENT_SPEC, CLIENT.canTeleport, true);
    }

    public static boolean timeKeeping() {
        return getBoolean("timeKeeping", CLIENT_SPEC, CLIENT.timeKeeping, true);
    }

    public static boolean useWakamoto() {
        return getBoolean("useWakamoto", CLIENT_SPEC, CLIENT.useWakamoto, true);
    }

    public static boolean showTag() {
        return getBoolean("showTag", CLIENT_SPEC, CLIENT.showTag, true);
    }

    public static int nameTagDist() {
        return getInt("nameTagDist", CLIENT_SPEC, CLIENT.nameTagDist, 16);
    }

    public static boolean alwaysShowTeamParticle() {
        return getBoolean("alwaysShowTeamParticle", CLIENT_SPEC, CLIENT.alwaysShowTeamParticle, false);
    }

    public static float volumeTimekeep() {
        return (float) getDouble("volumeTimekeep", CLIENT_SPEC, CLIENT.volumeTimekeep, 1.0D);
    }

    public static float volumeShip() {
        return (float) getDouble("volumeShip", CLIENT_SPEC, CLIENT.volumeShip, 1.0D);
    }

    public static float volumeFire() {
        return (float) getDouble("volumeFire", CLIENT_SPEC, CLIENT.volumeFire, 0.7D);
    }

    private static boolean getBoolean(String key, ForgeConfigSpec spec, ForgeConfigSpec.BooleanValue value, boolean fallback) {
        Object override = TEST_OVERRIDES.get(key);
        if (override instanceof Boolean bool) {
            return bool;
        }
        if (spec.isLoaded()) {
            try {
                return value.get();
            } catch (IllegalStateException ignored) {
            }
        }
        return fallback;
    }

    private static int getInt(String key, ForgeConfigSpec spec, ForgeConfigSpec.IntValue value, int fallback) {
        Object override = TEST_OVERRIDES.get(key);
        if (override instanceof Number num) {
            return num.intValue();
        }
        if (spec.isLoaded()) {
            try {
                return value.get();
            } catch (IllegalStateException ignored) {
            }
        }
        return fallback;
    }

    private static double getDouble(String key, ForgeConfigSpec spec, ForgeConfigSpec.DoubleValue value, double fallback) {
        Object override = TEST_OVERRIDES.get(key);
        if (override instanceof Number num) {
            return num.doubleValue();
        }
        if (spec.isLoaded()) {
            try {
                return value.get();
            } catch (IllegalStateException ignored) {
            }
        }
        return fallback;
    }

    public static final class Common {

        public final ForgeConfigSpec.BooleanValue friendlyFire;
        public final ForgeConfigSpec.IntValue shipAttackPlayer;
        public final ForgeConfigSpec.BooleanValue mobAttackPlayer;
        public final ForgeConfigSpec.IntValue dmgSvS;
        public final ForgeConfigSpec.BooleanValue easyMode;
        public final ForgeConfigSpec.IntValue kaitaiAmountSmall;
        public final ForgeConfigSpec.IntValue kaitaiAmountLarge;
        public final ForgeConfigSpec.BooleanValue polyAsMn;
        public final ForgeConfigSpec.IntValue radarUpdate;
        public final ForgeConfigSpec.IntValue closeGuiDist;
        public final ForgeConfigSpec.IntValue bossCooldown;
        public final ForgeConfigSpec.IntValue teamCooldown;
        public final ForgeConfigSpec.IntValue shipNumPerPage;
        public final ForgeConfigSpec.IntValue pairDistChest;
        public final ForgeConfigSpec.IntValue pairDistWp;
        public final ForgeConfigSpec.IntValue spawnBossNum;
        public final ForgeConfigSpec.IntValue spawnMobNum;
        public final ForgeConfigSpec.IntValue despawnBoss;
        public final ForgeConfigSpec.IntValue despawnMinion;
        public final ForgeConfigSpec.IntValue baseCaressMorale;
        public final ForgeConfigSpec.IntValue modernLimit;
        public final ForgeConfigSpec.IntValue maxDmgOnPlayer;
        public final ForgeConfigSpec.ConfigValue<List<? extends Integer>> ringAbility;
        public final ForgeConfigSpec.DoubleValue dropGrudge;
        public final ForgeConfigSpec.IntValue despawnEgg;
        public final ForgeConfigSpec.ConfigValue<List<? extends Number>> tileShipyardSmall;
        public final ForgeConfigSpec.ConfigValue<List<? extends Number>> tileShipyardLarge;
        public final ForgeConfigSpec.ConfigValue<List<? extends Number>> tileVolCore;
        public final ForgeConfigSpec.IntValue polyGravelBaseRate;
        public final ForgeConfigSpec.ConfigValue<List<? extends Boolean>> polyGravelBaseBlock;
        public final ForgeConfigSpec.BooleanValue checkRing;
        public final ForgeConfigSpec.ConfigValue<List<? extends Number>> mobSpawn;
        public final ForgeConfigSpec.BooleanValue enableMetamorphSkill;
        public final ForgeConfigSpec.DoubleValue expGainPlayerSkill;
        public final ForgeConfigSpec.DoubleValue morphHpRatio;
        public final ForgeConfigSpec.DoubleValue morphDmgTakenRatio;

        private Common(ForgeConfigSpec.Builder builder) {
            builder.push("general");
            this.friendlyFire = builder
                    .comment("false: disable damage done by player (except owner) and ally friendly-fire")
                    .define("friendlyFire", true);
            this.shipAttackPlayer = builder
                    .comment("0: never auto-attack player, 1: banned team only, 2: hostile and neutral players (!isAlly), 3: all except owner")
                    .defineInRange("shipAttackPlayer", 0, 0, 3);
            this.mobAttackPlayer = builder
                    .comment("true: hostile ships attack players automatically")
                    .define("mobAttackPlayer", true);
            this.dmgSvS = builder
                    .comment("Ship vs Ship damage modifier percentage (100 = 100%)")
                    .defineInRange("dmgSvS", 100, 0, 1000);
            this.easyMode = builder
                    .comment("Reduce ship resource/ammo cost by 10x and reduce large ship kaitai yield")
                    .define("easyMode", false);
            this.kaitaiAmountSmall = builder
                    .comment("Recycle amount by Kaitai Hammer for small kanmusu ships")
                    .defineInRange("kaitaiAmountSmall", 20, 0, 1000);
            this.kaitaiAmountLarge = builder
                    .comment("Recycle amount by Kaitai Hammer for large kanmusu ships")
                    .defineInRange("kaitaiAmountLarge", 20, 0, 1000);
            this.polyAsMn = builder
                    .comment("true: treat Polymetallic Nodules / Ore / Block as Manganese in tag matching and conditional recipes")
                    .define("polyAsMn", false);
            this.radarUpdate = builder
                    .comment("Radar update interval (ticks) in Admiral's Desk GUI")
                    .defineInRange("radarUpdate", 64, 20, 6000);
            this.closeGuiDist = builder
                    .comment("Auto-close GUI distance squared threshold")
                    .defineInRange("closeGuiDist", 64, 1, 256);
            this.bossCooldown = builder
                    .comment("Natural boss spawn cooldown in ticks")
                    .defineInRange("bossCooldown", 4800, 20, 1728000);
            this.teamCooldown = builder
                    .comment("Create/Disband Team cooldown in ticks")
                    .defineInRange("teamCooldown", 6000, 20, 1728000);
            this.shipNumPerPage = builder
                    .comment("Ships per page for /ship list")
                    .defineInRange("shipNumPerPage", 5, 1, 5000);
            this.pairDistChest = builder
                    .comment("Max pairing distance between waypoint and chest")
                    .defineInRange("pairDistChest", 16, 0, 64);
            this.pairDistWp = builder
                    .comment("Max pairing distance between waypoints")
                    .defineInRange("pairDistWp", 48, 0, 64);
            this.spawnBossNum = builder
                    .comment("Large hostile ship (boss) group count per spawn")
                    .defineInRange("spawnBossNum", 2, 1, 10);
            this.spawnMobNum = builder
                    .comment("Small hostile ship group count per spawn")
                    .defineInRange("spawnMobNum", 4, 1, 10);
            this.despawnBoss = builder
                    .comment("Despawn time of boss ship, -1 = do NOT despawn")
                    .defineInRange("despawnBoss", 12000, -1, 1728000);
            this.despawnMinion = builder
                    .comment("Despawn time of nonboss ship, -1 = do NOT despawn")
                    .defineInRange("despawnMinion", 600, -1, 1728000);
            this.despawnEgg = builder
                    .comment("Despawn time of spawn egg of ship mob, -1 = do NOT despawn (Despawn_Egg)")
                    .defineInRange("Despawn_Egg", 12000, -1, 1728000);
            this.dropGrudge = builder
                    .comment("Grudge drop rate (ex: 0.5 = 50% drop 1 grudge, 5.5 = drop 5 grudge + 50% drop 1 grudge) (DropRate_Grudge)")
                    .defineInRange("DropRate_Grudge", 1.0D, 0.0D, 64.0D);
            this.tileShipyardSmall = builder
                    .comment("Small shipyard: max fuel storage, build speed, fuel magnification (Tile_SmallShipyard)")
                    .defineList("Tile_SmallShipyard", List.of(460800.0D, 48.0D, 1.0D), entry -> entry instanceof Number);
            this.tileShipyardLarge = builder
                    .comment("Large shipyard: max fuel storage, build speed, fuel magnification (Tile_LargeShipyard)")
                    .defineList("Tile_LargeShipyard", List.of(1382400.0D, 48.0D, 1.0D), entry -> entry instanceof Number);
            this.tileVolCore = builder
                    .comment("Volcano Core: max fuel storage, fuel consume speed, fuel value per grudge item (Tile_VolCore)")
                    .defineList("Tile_VolCore", List.of(9600.0D, 16.0D, 240.0D), entry -> entry instanceof Number);
            this.checkRing = builder
                    .comment("Spawn mob ship only if player has marriage ring (Check_Ring)")
                    .define("Check_Ring", true);
            this.mobSpawn = builder
                    .comment("Mob ship spawn: [max mob in all worlds, spawn chance (0~100), groups, min per group, max per group] (Limit_MobSpawnNumber)")
                    .defineList("Limit_MobSpawnNumber", List.of(50, 10, 1, 1, 1), entry -> entry instanceof Number);
            builder.pop();

            builder.push("world_gen");
            this.polyGravelBaseRate = builder
                    .comment("Polymetallic Gravel clusters in one chunk (Polymetal_Gravel)")
                    .defineInRange("Polymetal_Gravel", 4, 0, 100);
            this.polyGravelBaseBlock = builder
                    .comment("PolyGravel replaced block: stone, gravel, sand, dirt (Polymetal_Gravel_Replace)")
                    .defineList("Polymetal_Gravel_Replace", List.of(true, true, false, false), entry -> entry instanceof Boolean);
            builder.pop();

            builder.push("ship_setting");
            this.baseCaressMorale = builder
                    .comment("Base morale gained per 4-tick caress tick")
                    .defineInRange("baseCaressMorale", 20, 1, 5000);
            this.modernLimit = builder
                    .comment("Max upgrade level by Modernization Toolkit")
                    .defineInRange("modernLimit", 3, 3, 100);
            this.maxDmgOnPlayer = builder
                    .comment("Max damage a ship attack can deal to a player target")
                    .defineInRange("maxDmgOnPlayer", 59, 1, 10000);
            this.ringAbility = builder
                    .comment("Marriage count thresholds for [water_breath, water_fly, dig_boost, liquid_fog, fire_immunity]")
                    .defineList("ringAbility", List.of(0, 6, 30, 20, 12), entry -> entry instanceof Integer);
            builder.pop();

            builder.push("inter_mod");
            this.enableMetamorphSkill = builder
                    .comment("Enable morph skill usage and grudge drain")
                    .define("enableMetamorphSkill", true);
            this.expGainPlayerSkill = builder
                    .comment("Exp multiplier for player morph skills")
                    .defineInRange("expGainPlayerSkill", 6.0D, 0.0D, 1000.0D);
            this.morphHpRatio = builder
                    .comment("HP multiplier of morphed player")
                    .defineInRange("morphHpRatio", 0.1D, 0.0D, 10.0D);
            this.morphDmgTakenRatio = builder
                    .comment("Damage taken ratio while morphed")
                    .defineInRange("morphDmgTakenRatio", 0.2D, 0.0D, 1.0D);
            builder.pop();
        }
    }

    public static final class Client {

        public final ForgeConfigSpec.BooleanValue canFlare;
        public final ForgeConfigSpec.BooleanValue canSearchlight;
        public final ForgeConfigSpec.BooleanValue canTeleport;
        public final ForgeConfigSpec.BooleanValue timeKeeping;
        public final ForgeConfigSpec.BooleanValue useWakamoto;
        public final ForgeConfigSpec.BooleanValue showTag;
        public final ForgeConfigSpec.IntValue nameTagDist;
        public final ForgeConfigSpec.BooleanValue alwaysShowTeamParticle;
        public final ForgeConfigSpec.DoubleValue volumeTimekeep;
        public final ForgeConfigSpec.DoubleValue volumeShip;
        public final ForgeConfigSpec.DoubleValue volumeFire;

        private Client(ForgeConfigSpec.Builder builder) {
            builder.push("client");
            this.canFlare = builder.define("canFlare", true);
            this.canSearchlight = builder.define("canSearchlight", true);
            this.canTeleport = builder.define("canTeleport", true);
            this.timeKeeping = builder.define("timeKeeping", true);
            this.useWakamoto = builder.define("useWakamoto", true);
            this.showTag = builder.define("showTag", true);
            this.nameTagDist = builder.defineInRange("nameTagDist", 16, 1, 64);
            this.alwaysShowTeamParticle = builder.define("alwaysShowTeamParticle", false);
            this.volumeTimekeep = builder.defineInRange("volumeTimekeep", 1.0D, 0.0D, 10.0D);
            this.volumeShip = builder.defineInRange("volumeShip", 1.0D, 0.0D, 10.0D);
            this.volumeFire = builder.defineInRange("volumeFire", 0.7D, 0.0D, 10.0D);
            builder.pop();
        }
    }
}
