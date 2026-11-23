package com.lulan.shincolle.gametest;

import com.google.gson.JsonObject;
import com.lulan.shincolle.entity.ship.LegacyShipSmoke;
import com.lulan.shincolle.registry.LegacySmokeOptions;
import com.lulan.shincolle.registry.ModParticleTypes;
import com.mojang.serialization.JsonOps;
import io.netty.buffer.Unpooled;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.util.RandomSource;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import net.minecraft.network.RegistryFriendlyByteBuf;

import java.util.List;

@GameTestHolder("shincolle")
@PrefixGameTestTemplate(false)
public final class LegacyShipSmokeGameTests {
    private LegacyShipSmokeGameTests() {
    }

    @GameTest(template = "gameplayparitygametests.empty")
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

    @GameTest(template = "gameplayparitygametests.empty")
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

    @GameTest(template = "gameplayparitygametests.empty")
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

    @GameTest(template = "gameplayparitygametests.empty")
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

    @GameTest(template = "gameplayparitygametests.empty")
    public static void registeredSmokeOptionsRoundTripAndRejectMalformedScale(GameTestHelper helper) throws Exception {
        var codec = LegacySmokeOptions.CODEC.codec();
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
        RegistryFriendlyByteBuf buffer = new RegistryFriendlyByteBuf(Unpooled.buffer(), helper.getLevel().registryAccess());
        try {
            LegacySmokeOptions.STREAM_CODEC.encode(buffer, options);
            helper.assertTrue(LegacySmokeOptions.STREAM_CODEC.decode(buffer).equals(options), "network scale round trip");
        } finally {
            buffer.release();
        }
        helper.succeed();
    }

    private static boolean close(double a, double b) {
        return Math.abs(a - b) < 0.000000001D;
    }
}
