package com.lulan.shincolle.gametest;

import com.google.gson.JsonObject;
import com.lulan.shincolle.entity.ship.LegacyShipBehaviorCatalog;
import com.lulan.shincolle.entity.ship.LegacyShipSmoke;
import com.lulan.shincolle.entity.ship.ShipEntitySpecs;
import com.lulan.shincolle.registry.LegacySmokeOptions;
import com.lulan.shincolle.registry.ModParticleTypes;
import com.mojang.serialization.JsonOps;
import io.netty.buffer.Unpooled;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.util.RandomSource;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;
import net.minecraft.network.FriendlyByteBuf;

import java.util.List;

@GameTestHolder("shincolle")
@PrefixGameTestTemplate(false)
public final class LegacyShipSmokeGameTests {
    private LegacyShipSmokeGameTests() {
    }

    @GameTest(template = "gameplayparitygametests.empty", batch = "ship_smoke")
    public static void scaledSmokeKeepsScaleSeparateFromVelocityAndLegacySpacing(GameTestHelper helper) {
        for (float scale : new float[] {1.0F, 1.5F, 3.4F, 4.0F}) {
            List<LegacyShipSmoke.Emission> smoke = LegacyShipSmoke.scaled(2, 3, 4, scale, 0.05D);
            helper.assertTrue(smoke.size() == 3, "type 43 emits exactly three particles");
            for (int i = 0; i < smoke.size(); i++) {
                var point = smoke.get(i);
                helper.assertTrue(point.particle() instanceof LegacySmokeOptions options && options.scale() == scale,
                        "custom normal-smoke scale must not become large smoke or X speed");
                helper.assertTrue(point.x() == 2 && point.z() == 4 && close(point.y(), 3 + i * 0.1D),
                        "type 43 uses 0.1 vertical spacing without lateral offset");
                helper.assertTrue(point.velocityX() == 0 && point.velocityZ() == 0 && point.velocityY() == 0.05D,
                        "type 43 supplies only the requested vertical velocity");
            }
        }
        helper.succeed();
    }

    @GameTest(template = "gameplayparitygametests.empty", batch = "ship_smoke")
    public static void hpEffectsKeepLegacyTypesCountsAndClientTickGate(GameTestHelper helper) {
        int[] types = {0, 4, 5, 7};
        int[] counts = {0, 3, 6, 8};
        for (int state = 0; state <= 3; state++) {
            helper.assertTrue(LegacyShipSmoke.damageEffectType(state) == types[state],
                    "HP state must map to legacy particle helper type");
            helper.assertTrue(LegacyShipSmoke.damage(RandomSource.create(7), state, false, 1, 0, 0, 0).size() == counts[state],
                    "HP effects retain legacy particle count");
        }
        for (int state : new int[] {-1, 4, Integer.MAX_VALUE}) {
            helper.assertTrue(LegacyShipSmoke.damageEffectType(state) == 0
                    && LegacyShipSmoke.damage(RandomSource.create(7), state, false, 1, 0, 0, 0).isEmpty(),
                    "normal and unknown HP states have no effect");
        }
        for (int tick = 1; tick <= 64; tick++) {
            helper.assertTrue(LegacyShipSmoke.damageTick(true, tick) == (tick % 16 == 0),
                    "HP emission is every sixteen client ticks without a legacy alive-state restriction");
            helper.assertTrue(!LegacyShipSmoke.damageTick(false, tick), "no server HP emission");
        }
        helper.succeed();
    }

    @GameTest(template = "gameplayparitygametests.empty", batch = "ship_smoke")
    public static void damageOffsetsFlamesAndHostileSpreadMatchLegacy(GameTestHelper helper) {
        for (boolean hostile : new boolean[] {false, true}) {
            for (int state = 1; state <= 3; state++) {
                RandomSource oracle = RandomSource.create(71);
                var points = LegacyShipSmoke.damage(RandomSource.create(71), state, hostile, 2, 10, 20, 30);
                int stride = state == 1 ? 1 : 2;
                double spread = hostile ? 3.0D : 2.0D;
                for (int i = 0; i < points.size(); i += stride) {
                    double dx = oracle.nextFloat() * spread - spread / 2.0D;
                    double dy = oracle.nextFloat() * spread - spread / 2.0D;
                    double dz = oracle.nextFloat() * spread - spread / 2.0D;
                    var smoke = points.get(i);
                    helper.assertTrue(close(smoke.x(), 10 + dx) && close(smoke.y(), 20.7D + dy)
                            && close(smoke.z(), 30 + dz), "legacy random offsets, including hostile width*1.5");
                    helper.assertTrue(smoke.particle() == (state == 3 ? ParticleTypes.LARGE_SMOKE : ParticleTypes.SMOKE)
                            && smoke.velocityX() == 0 && smoke.velocityZ() == 0
                            && smoke.velocityY() == (state == 3 ? 0 : 0.05D), "legacy damage smoke type and velocity");
                    if (stride == 2) {
                        var flame = points.get(i + 1);
                        helper.assertTrue(flame.particle() == ParticleTypes.FLAME
                                && close(flame.x(), 10 + dz) && close(flame.y(), 20.7D + dy)
                                && close(flame.z(), 30 + dx) && flame.velocityX() == 0
                                && flame.velocityY() == 0.05D && flame.velocityZ() == 0,
                                "paired flame swaps X/Z offsets and rises at 0.05");
                    }
                }
            }
        }
        helper.succeed();
    }

    @GameTest(template = "gameplayparitygametests.empty", batch = "ship_smoke")
    public static void deathSmokeKeepsQualityCountsScaleAndOffsets(GameTestHelper helper) {
        int[] groups = {5, 3, 1};
        for (boolean hostile : new boolean[] {false, true}) {
            for (int quality = 0; quality < 3; quality++) {
                RandomSource oracle = RandomSource.create(98);
                var points = LegacyShipSmoke.death(RandomSource.create(98), hostile, 2, 2, quality, 10, 20, 30);
                helper.assertTrue(points.size() == groups[quality] * 3, "legacy death quality-dependent group count");
                for (int i = 0; i < points.size(); i += 3) {
                    double x = 10 - 2.4D + oracle.nextDouble() * 4.8D;
                    double y = 20 + (hostile ? 0.3D : 0.1D) + oracle.nextDouble() * 0.3D;
                    double z = 30 - 2.4D + oracle.nextDouble() * 4.8D;
                    for (int segment = 0; segment < 3; segment++) {
                        var point = points.get(i + segment);
                        helper.assertTrue(close(point.x(), x) && close(point.y(), y + segment * 0.1D)
                                && close(point.z(), z), "death smoke preserves width*1.2 and hostile/friendly Y");
                        helper.assertTrue(point.particle() instanceof LegacySmokeOptions options
                                && options.scale() == (hostile ? 3.0F : 1.5F)
                                && point.velocityX() == 0 && point.velocityY() == 0 && point.velocityZ() == 0,
                                "death smoke scale is not velocity");
                    }
                }
            }
        }
        helper.succeed();
    }

    @GameTest(template = "gameplayparitygametests.empty", batch = "ship_smoke")
    public static void registeredSmokeOptionsRoundTripAndRejectMalformedScale(GameTestHelper helper) throws Exception {
        var codec = LegacySmokeOptions.CODEC;
        var options = new LegacySmokeOptions(2.5F);
        var json = codec.encodeStart(JsonOps.INSTANCE, options).result().orElseThrow();
        helper.assertTrue(codec.parse(JsonOps.INSTANCE, json).result().orElseThrow().equals(options),
                "smoke scale survives codec round trip");
        helper.assertTrue(options.getType() == ModParticleTypes.LEGACY_SMOKE.get()
                && options.getType().getOverrideLimiter(), "type 43 uses registered smoke and bypasses vanilla setting suppression");
        helper.assertTrue(codec.parse(JsonOps.INSTANCE, new JsonObject()).error().isPresent(),
                "missing scale must not silently become X velocity");
        JsonObject malformed = new JsonObject();
        malformed.addProperty("scale", "not-a-number");
        helper.assertTrue(codec.parse(JsonOps.INSTANCE, malformed).error().isPresent(), "malformed scale is rejected");
        FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());
        try {
            options.writeToNetwork(buffer);
            helper.assertTrue(LegacySmokeOptions.DESERIALIZER.fromNetwork(options.getType(), buffer).equals(options),
                    "network scale round trip");
            var reader = new com.mojang.brigadier.StringReader(" 2.5");
            helper.assertTrue(LegacySmokeOptions.DESERIALIZER.fromCommand(options.getType(), reader).equals(options),
                    "command particle scale round trip");
        } finally {
            buffer.release();
        }
        helper.succeed();
    }

    @GameTest(template = "gameplayparitygametests.empty", batch = "ship_smoke")
    public static void hostileChimneysKeepPerShipLegacyParticleScales(GameTestHelper helper) {
        for (int meta : new int[] {2039, 2048, 2062, 2063, 2064, 2065}) {
            for (int scale = 0; scale <= 3; scale++) {
                var points = LegacyShipBehaviorCatalog.legacyEngineVisualPointsFor(ShipEntitySpecs.getByEggMeta(meta), scale);
                helper.assertTrue(points.size() == (meta == 2062 ? 2 : 1), "legacy hostile chimney count");
                for (var point : points) {
                    float expected = meta == 2063 ? 1.0F : 1.0F + scale;
                    helper.assertTrue(point.kind() == LegacyShipBehaviorCatalog.EngineVisualKind.SMOKE_CHIMNEY_L
                            && close(point.particleScale(), expected), "each hostile ship keeps its legacy type-43 scale");
                    if (meta == 2063) {
                        helper.assertTrue(point.xOffset() == -0.6F && point.yOffset() == 1.17F,
                                "Hiei callback uses literal anchors, not its unused scaled fields");
                    }
                    for (var emission : LegacyShipSmoke.scaled(0, 0, 0, point.particleScale(), 0)) {
                        helper.assertTrue(emission.particle() instanceof LegacySmokeOptions options
                                && options.scale() == expected && options.getType().getOverrideLimiter(),
                                "type-43 options retain the per-ship scale and direct-renderer limiter policy");
                    }
                }
            }
        }
        for (int scale : new int[] {-1, 4}) {
            var point = LegacyShipBehaviorCatalog.legacyEngineVisualPointsFor(
                    ShipEntitySpecs.getByEggMeta(2063), scale).get(0);
            helper.assertTrue(point.particleScale() == 1.0F && point.xOffset() == -0.6F
                    && point.yOffset() == 1.17F, "Hiei remains fixed even for an out-of-range encounter scale");
        }
        helper.succeed();
    }

    private static boolean close(double a, double b) {
        return Math.abs(a - b) < 0.000000001D;
    }

    @GameTest(template = "gameplayparitygametests.empty", batch = "ship_smoke")
    public static void friendlyDestroyerChimneysKeepOrdinarySmokeAndParticleLimiter(GameTestHelper helper) {
        for (int meta : new int[] {53, 54, 55, 56}) {
            for (int scale = 0; scale <= 3; scale++) {
                var points = LegacyShipBehaviorCatalog.legacyEngineVisualPointsFor(ShipEntitySpecs.getByEggMeta(meta), scale);
                helper.assertTrue(points.size() == 1
                        && points.get(0).kind() == LegacyShipBehaviorCatalog.EngineVisualKind.SMOKE_CHIMNEY_NORMAL,
                        "friendly destroyer uses legacy type 20, not custom type 43");
                helper.assertTrue(LegacyShipBehaviorCatalog.legacyEngineSmokePointsFor(
                        ShipEntitySpecs.getByEggMeta(meta), scale).isEmpty(), "ordinary smoke is not a type-43 point");
                var hostile = LegacyShipBehaviorCatalog.legacyEngineVisualPointsFor(
                        ShipEntitySpecs.getByEggMeta(meta + 2000), scale);
                helper.assertTrue(hostile.size() == 1
                        && hostile.get(0).kind() == LegacyShipBehaviorCatalog.EngineVisualKind.SMOKE_CHIMNEY_L
                        && close(hostile.get(0).particleScale(), 1.0F + scale * 0.8F),
                        "hostile destroyers retain scaled type 43");
            }
        }
        var smoke = LegacyShipSmoke.normal(2, 3, 4);
        helper.assertTrue(smoke.size() == 3, "type 20 has three ordinary smoke particles");
        for (int i = 0; i < smoke.size(); i++) {
            var point = smoke.get(i);
            helper.assertTrue(point.particle() == ParticleTypes.SMOKE
                    && !point.particle().getType().getOverrideLimiter(), "ordinary smoke respects particle settings");
            helper.assertTrue(point.x() == 2 && close(point.y(), 3 + i * 0.1D) && point.z() == 4
                    && point.velocityX() == 0 && point.velocityY() == 0 && point.velocityZ() == 0,
                    "type 20 retains legacy spacing and zero supplied velocity");
        }
        helper.assertTrue(LegacyShipBehaviorCatalog.legacyEngineVisualPointsFor(null, 0).isEmpty(),
                "missing spec has no engine effect");
        helper.succeed();
    }
}
