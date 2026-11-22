package com.lulan.shincolle.config;

import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 1.12.2 ConfigHandler parity backed by ModConfigSpec (COMMON + CLIENT),
 * with safe pre-load fallbacks and deterministic GameTest overrides.
 */
public final class ShinColleConfig {

    public static final ModConfigSpec COMMON_SPEC;
    public static final ModConfigSpec CLIENT_SPEC;
    public static final Common COMMON;
    public static final Client CLIENT;

    private static final Map<String, Object> TEST_OVERRIDES = new ConcurrentHashMap<>();

    static {
        ModConfigSpec.Builder commonBuilder = new ModConfigSpec.Builder();
        COMMON = new Common(commonBuilder);
        COMMON_SPEC = commonBuilder.build();

        ModConfigSpec.Builder clientBuilder = new ModConfigSpec.Builder();
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

    public static void clearTestOverride(String key) {
        TEST_OVERRIDES.remove(key);
    }

    public static double[] shipScale() { return numericList("attrsScale", COMMON.attrsScale, 1, 1, 1, 1, 1, 1); }
    public static double[] hostileSmallMobScale() { return numericList("attrsHostileSmallMob", COMMON.attrsHostileSmallMob, 250, 25, 0.15, 0.7, 0.45, 12); }
    public static double[] hostileLargeMobScale() { return numericList("attrsHostileLargeMob", COMMON.attrsHostileLargeMob, 500, 50, 0.30, 0.9, 0.4, 15); }
    public static double[] hostileSmallBossScale() { return numericList("attrsHostileSmallBoss", COMMON.attrsHostileSmallBoss, 1600, 120, 0.5, 1.6, 0.38, 18); }
    public static double[] hostileLargeBossScale() { return numericList("attrsHostileLargeBoss", COMMON.attrsHostileLargeBoss, 3200, 240, 0.75, 2, 0.35, 22); }

    public static double[] tileShipyardSmall() { return numericList("tileShipyardSmall", COMMON.tileShipyardSmall, 460800, 48, 1); }
    public static double[] tileShipyardLarge() { return numericList("tileShipyardLarge", COMMON.tileShipyardLarge, 1382400, 48, 1); }
    public static double[] tileVolCore() { return numericList("tileVolCore", COMMON.tileVolCore, 9600, 16, 240); }
    public static double[] drumLiquid() { return numericList("drumLiquid", COMMON.drumLiquid, 40, 5); }
    public static int craneWorkExperience() { return (int) numericList("expGain", COMMON.expGain, 2, 4, 5, 8, 10, 1, 2)[6]; }
    public static double[] tileCrane() { return numericList("tileCrane", COMMON.tileCrane, 2048000, 160000000); }
    public static int shipTankCapacity(int tier) { return (int) numericList("itemShipTank", COMMON.itemShipTank, 32000, 128000, 512000, 2048000)[tier]; }

    private static double[] numericList(String key, ModConfigSpec.ConfigValue<List<? extends Number>> value, double... fallback) {
        Object loaded = TEST_OVERRIDES.get(key);
        if (loaded instanceof int[] ints && ints.length == fallback.length) {
            return Arrays.stream(ints).asDoubleStream().toArray();
        }
        if (loaded instanceof double[] doubles && doubles.length == fallback.length) {
            return doubles.clone();
        }
        if (loaded == null && COMMON_SPEC.isLoaded()) {
            loaded = value.get();
        }
        if (loaded instanceof List<?> list && list.size() == fallback.length) {
            double[] result = fallback.clone();
            for (int i = 0; i < result.length; i++) {
                if (list.get(i) instanceof Number number) result[i] = number.doubleValue();
            }
            return result;
        }
        return fallback;
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

    public static int itemGrudge() {
        return getInt("Item_Grudge", COMMON_SPEC, COMMON.itemGrudge, 300);
    }

    public static int itemLightAmmo() {
        return getInt("Item_LightAmmo", COMMON_SPEC, COMMON.itemLightAmmo, 30);
    }

    public static int itemHeavyAmmo() {
        return getInt("Item_HeavyAmmo", COMMON_SPEC, COMMON.itemHeavyAmmo, 15);
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

    public static int deathMaxTick() {
        return getInt("deathMaxTick", COMMON_SPEC, COMMON.deathMaxTick, 400);
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

    static int[] intArray(String key, ModConfigSpec.ConfigValue<List<? extends Number>> value, int[] fallback) {
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

    public static List<? extends Integer> shipTeleport() {
        List<? extends Integer> values = COMMON_SPEC.isLoaded() ? COMMON.shipTeleport.get() : List.of(200, 256);
        return values.size() == 2 ? values : List.of(200, 256);
    }

    public static boolean canTeleport() {
        return getBoolean("canTeleport", COMMON_SPEC, COMMON.canTeleport, true);
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

    public static double[] scaleHeldItem() {
        Object configured = TEST_OVERRIDES.get("scaleHeldItem");
        if (configured == null && CLIENT_SPEC.isLoaded()) {
            try {
                configured = CLIENT.scaleHeldItem.get();
            } catch (IllegalStateException ignored) {
            }
        }
        if (configured instanceof double[] values && values.length == 4) {
            return Arrays.copyOf(values, 4);
        }
        if (isHeldItemSettings(configured)) {
            List<?> values = (List<?>) configured;
            return new double[] {((Number)values.get(0)).doubleValue(), ((Number)values.get(1)).doubleValue(),
                    ((Number)values.get(2)).doubleValue(), ((Number)values.get(3)).doubleValue()};
        }
        return new double[] {2.5D, 0D, 0D, 0D};
    }

    public static double[] posHUD() {
        Object configured = TEST_OVERRIDES.get("Position_HUD");
        if (configured == null && CLIENT_SPEC.isLoaded()) {
            try {
                configured = CLIENT.posHUD.get();
            } catch (IllegalStateException ignored) {
            }
        }
        if (configured instanceof double[] values && values.length == 2) {
            return Arrays.copyOf(values, 2);
        }
        if (configured instanceof List<?> list && isHudPosition(list)) {
            return new double[] {((Number)list.get(0)).doubleValue(), ((Number)list.get(1)).doubleValue()};
        }
        return new double[] {0.5D, 0.6D};
    }

    private static boolean isHudPosition(Object value) {
        return value instanceof List<?> list && list.size() == 2 && list.stream().allMatch(Number.class::isInstance);
    }

    private static boolean isHeldItemSettings(Object value) {
        return value instanceof List<?> list && list.size() == 4 && list.stream().allMatch(Number.class::isInstance);
    }

    private static boolean getBoolean(String key, ModConfigSpec spec, ModConfigSpec.BooleanValue value, boolean fallback) {
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

    private static int getInt(String key, ModConfigSpec spec, ModConfigSpec.IntValue value, int fallback) {
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

    private static double getDouble(String key, ModConfigSpec spec, ModConfigSpec.DoubleValue value, double fallback) {
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

        public final ModConfigSpec.BooleanValue canTeleport;
        public final ModConfigSpec.ConfigValue<List<? extends Integer>> shipTeleport;
        public final ModConfigSpec.BooleanValue friendlyFire;
        public final ModConfigSpec.IntValue shipAttackPlayer;
        public final ModConfigSpec.BooleanValue mobAttackPlayer;
        public final ModConfigSpec.IntValue dmgSvS;
        public final ModConfigSpec.BooleanValue easyMode;
        public final ModConfigSpec.IntValue kaitaiAmountSmall;
        public final ModConfigSpec.IntValue kaitaiAmountLarge;
        public final ModConfigSpec.BooleanValue polyAsMn;
        public final ModConfigSpec.IntValue radarUpdate;
        public final ModConfigSpec.IntValue chunkloaderMode;
        public final ModConfigSpec.IntValue closeGuiDist;
        public final ModConfigSpec.IntValue bossCooldown;
        public final ModConfigSpec.IntValue teamCooldown;
        public final ModConfigSpec.IntValue shipNumPerPage;
        public final ModConfigSpec.IntValue pairDistChest;
        public final ModConfigSpec.IntValue pairDistWp;
        public final ModConfigSpec.IntValue spawnBossNum;
        public final ModConfigSpec.IntValue spawnMobNum;
        public final ModConfigSpec.IntValue despawnBoss;
        public final ModConfigSpec.IntValue despawnMinion;
        public final ModConfigSpec.IntValue baseCaressMorale;
        public final ModConfigSpec.IntValue itemGrudge;
        public final ModConfigSpec.IntValue itemLightAmmo;
        public final ModConfigSpec.IntValue itemHeavyAmmo;
        public final ModConfigSpec.IntValue modernLimit;
        public final ModConfigSpec.IntValue maxDmgOnPlayer;
        public final ModConfigSpec.ConfigValue<List<? extends Integer>> ringAbility;
        public final ModConfigSpec.DoubleValue dropGrudge;
        public final ModConfigSpec.IntValue despawnEgg;
        public final ModConfigSpec.IntValue deathMaxTick;
        public final ModConfigSpec.IntValue polyGravelBaseRate;
        public final ModConfigSpec.ConfigValue<List<? extends Boolean>> polyGravelBaseBlock;
        public final ModConfigSpec.BooleanValue checkRing;
        public final ModConfigSpec.ConfigValue<List<? extends Number>> mobSpawn;
        public final ModConfigSpec.BooleanValue enableMetamorphSkill;
        public final ModConfigSpec.DoubleValue expGainPlayerSkill;
        public final ModConfigSpec.DoubleValue morphHpRatio;
        public final ModConfigSpec.DoubleValue morphDmgTakenRatio;

        public final ModConfigSpec.ConfigValue<List<? extends Number>> attrsScale;
        public final ModConfigSpec.ConfigValue<List<? extends Number>> attrsHostileSmallMob;
        public final ModConfigSpec.ConfigValue<List<? extends Number>> attrsHostileLargeMob;
        public final ModConfigSpec.ConfigValue<List<? extends Number>> attrsHostileSmallBoss;
        public final ModConfigSpec.ConfigValue<List<? extends Number>> attrsHostileLargeBoss;
        public final ModConfigSpec.ConfigValue<List<? extends Number>> tileShipyardSmall;
        public final ModConfigSpec.ConfigValue<List<? extends Number>> tileShipyardLarge;
        public final ModConfigSpec.ConfigValue<List<? extends Number>> tileVolCore;
        public final ModConfigSpec.ConfigValue<List<? extends Number>> tileCrane;
        public final ModConfigSpec.ConfigValue<List<? extends Number>> drumLiquid;
        public final ModConfigSpec.ConfigValue<List<? extends Number>> expGain;
        public final ModConfigSpec.ConfigValue<List<? extends Number>> itemShipTank;

        private Common(ModConfigSpec.Builder builder) {
            builder.push("general");
            this.attrsScale = builder.defineList("Attrs_Scale", List.of(1D, 1D, 1D, 1D, 1D, 1D), entry -> entry instanceof Number);
            this.attrsHostileSmallMob = builder.defineList("Attrs_Hostile_SmallMob", List.of(250D, 25D, 0.15D, 0.7D, 0.45D, 12D), entry -> entry instanceof Number);
            this.attrsHostileLargeMob = builder.defineList("Attrs_Hostile_LargeMob", List.of(500D, 50D, 0.30D, 0.9D, 0.4D, 15D), entry -> entry instanceof Number);
            this.attrsHostileSmallBoss = builder.defineList("Attrs_Hostile_SmallBoss", List.of(1600D, 120D, 0.5D, 1.6D, 0.38D, 18D), entry -> entry instanceof Number);
            this.attrsHostileLargeBoss = builder.defineList("Attrs_Hostile_LargeBoss", List.of(3200D, 240D, 0.75D, 2D, 0.35D, 22D), entry -> entry instanceof Number);
            this.canTeleport = builder.define("Can_Teleport", true);
            this.shipTeleport = builder.defineList("ship_teleport", List.of(200, 256), value -> value instanceof Integer);
            this.drumLiquid = builder.defineList("Drum_Liquid", List.of(40, 5), entry -> entry instanceof Number);
            this.expGain = builder.defineList("Exp_Gain", List.of(2, 4, 5, 8, 10, 1, 2), entry -> entry instanceof Number);
            this.tileCrane = builder.defineList("Tile_Crane", List.of(2048000, 160000000), entry -> entry instanceof Number);
            this.itemShipTank = builder.defineList("Item_FluidDrum", List.of(32000, 128000, 512000, 2048000), entry -> entry instanceof Number);
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
            this.chunkloaderMode = builder
                    .comment("Chunk loader mode: 0: disable, 1: only 1 chunk each ship, 2: 3x3 chunks each ship")
                    .defineInRange("Mode_ChunkLoader", 2, 0, 2);
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
            this.deathMaxTick = builder
                    .comment("Ship death animation time (Death_Time)")
                    .defineInRange("Death_Time", 400, 0, 3600);
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
            this.itemGrudge = builder.defineInRange("Item_Grudge", 300, 1, 30000);
            this.itemLightAmmo = builder.defineInRange("Item_LightAmmo", 30, 1, 30000);
            this.itemHeavyAmmo = builder.defineInRange("Item_HeavyAmmo", 15, 1, 30000);
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
                    .comment("HP modify of player in morph, final HP = 20 + shipHP * Metamorph_HPRatio")
                    .defineInRange("Metamorph_HPRatio", 0.1D, 0.0D, 10.0D);
            this.morphDmgTakenRatio = builder
                    .comment("Damage taken ratio while morphed")
                    .defineInRange("Metamorph_DmgTakenRatio", 0.2D, 0.0D, 1.0D);
            builder.pop();
        }
    }

    public static final class Client {

        public final ModConfigSpec.BooleanValue canFlare;
        public final ModConfigSpec.BooleanValue canSearchlight;
        public final ModConfigSpec.BooleanValue timeKeeping;
        public final ModConfigSpec.BooleanValue useWakamoto;
        public final ModConfigSpec.BooleanValue showTag;
        public final ModConfigSpec.IntValue nameTagDist;
        public final ModConfigSpec.BooleanValue alwaysShowTeamParticle;
        public final ModConfigSpec.DoubleValue volumeTimekeep;
        public final ModConfigSpec.DoubleValue volumeShip;
        public final ModConfigSpec.DoubleValue volumeFire;
        public final ModConfigSpec.ConfigValue<List<? extends Double>> scaleHeldItem;
        public final ModConfigSpec.ConfigValue<List<? extends Double>> posHUD;

        private Client(ModConfigSpec.Builder builder) {
            builder.push("client");
            this.canFlare = builder.define("canFlare", true);
            this.canSearchlight = builder.define("canSearchlight", true);
            this.timeKeeping = builder.define("timeKeeping", true);
            this.useWakamoto = builder.define("useWakamoto", true);
            this.showTag = builder.define("showTag", true);
            this.nameTagDist = builder.defineInRange("nameTagDist", 16, 1, 64);
            this.alwaysShowTeamParticle = builder.define("alwaysShowTeamParticle", false);
            this.volumeTimekeep = builder.defineInRange("volumeTimekeep", 1.0D, 0.0D, 10.0D);
            this.volumeShip = builder.defineInRange("volumeShip", 1.0D, 0.0D, 10.0D);
            this.volumeFire = builder.defineInRange("volumeFire", 0.7D, 0.0D, 10.0D);
            this.scaleHeldItem = builder.define("scaleHeldItem", List.of(2.5D, 0D, 0D, 0D),
                    ShinColleConfig::isHeldItemSettings);
            this.posHUD = builder.define("Position_HUD", List.of(0.5D, 0.6D), ShinColleConfig::isHudPosition);
            builder.pop();
        }
    }
}
