package com.lulan.shincolle.gametest;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.lulan.shincolle.ShinColle;
import com.lulan.shincolle.client.model.legacy.LegacyModelSourceParser;
import com.lulan.shincolle.registry.ModBlocks;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.level.block.RenderShape;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@GameTestHolder(ShinColle.MOD_ID)
@PrefixGameTestTemplate(false)
public final class LegacyBlockRenderGameTests {
    private LegacyBlockRenderGameTests() {}

    @GameTest(template = "gameplayparitygametests.empty")
    public static void deskRetainsNonOpaqueTesrShape(GameTestHelper helper) {
        var state = ModBlocks.BLOCK_DESK.get().defaultBlockState();
        helper.assertTrue(!state.canOcclude() && !state.isSolidRender(helper.getLevel(), helper.absolutePos(net.minecraft.core.BlockPos.ZERO)),
                "the non-full legacy desk must not occlude its own block-entity lighting");
        helper.assertTrue(state.getRenderShape() == RenderShape.ENTITYBLOCK_ANIMATED,
                "the desk must use its legacy Java model instead of a baked cube");
        helper.succeed();
    }

    @GameTest(template = "gameplayparitygametests.empty")
    public static void legacyBlockInventoryModelsAndTexturesExist(GameTestHelper helper) throws IOException {
        for (String model : new String[] {"blockcrane", "blockwaypoint"}) {
            var json = json("models/block/" + model + ".json");
            helper.assertTrue(json.get("parent").getAsString().equals("minecraft:block/block")
                            && !json.getAsJsonArray("elements").isEmpty(),
                    "custom baked blocks must inherit the 3D inventory transforms: " + model);
        }
        for (String model : new String[] {"blockdesk", "blocksmallshipyard"}) {
            helper.assertTrue(json("models/item/" + model + ".json").get("parent").getAsString()
                            .equals("shincolle:item/legacy_block_entity"),
                    "legacy TESR items must dispatch to their block entity renderer: " + model);
        }
        var item = json("models/item/legacy_block_entity.json");
        helper.assertTrue(item.get("parent").getAsString().equals("minecraft:builtin/entity")
                        && item.getAsJsonObject("display").has("gui")
                        && item.getAsJsonObject("display").has("firstperson_righthand"),
                "custom block entity items need both the renderer marker and display transforms");
        var parser = new LegacyModelSourceParser();
        var desk = parser.parse("ModelBlockDesk", resource("legacy_model_sources/modelblockdesk.java"));
        helper.assertTrue(desk.textureWidth() == 64 && desk.textureHeight() == 64
                        && desk.parts().size() == 5 && desk.rootParts().size() == 5,
                "the desk must retain all five legacy model parts and its texture atlas dimensions");
        var yard = parser.parse("ModelSmallShipyard", resource("legacy_model_sources/modelsmallshipyard.java"));
        helper.assertTrue(!yard.parts().isEmpty() && !yard.rootParts().isEmpty(),
                "the shipyard item must have a renderable legacy model");
        for (String texture : new String[] {"block/blockdesk", "block/blocksmallshipyardoff",
                "block/blocksmallshipyardon", "block/blockcraneside", "block/blockcranetop",
                "block/blockcranebtm", "block/blockwaypoint", "block/blockvolcore",
                "blocks/blocklargeshipyard", "blocks/modelvortex", "blocks/modelvortexon"}) {
            try (var stream = LegacyBlockRenderGameTests.class.getClassLoader()
                    .getResourceAsStream("assets/shincolle/textures/" + texture + ".png")) {
                helper.assertTrue(stream != null && stream.read() == 137,
                        "legacy block texture must exist at the canonical lowercase path: " + texture);
            }
        }
        helper.succeed();
    }

    private static JsonObject json(String path) throws IOException {
        return JsonParser.parseString(resource(path)).getAsJsonObject();
    }

    private static String resource(String path) throws IOException {
        try (var stream = LegacyBlockRenderGameTests.class.getClassLoader()
                .getResourceAsStream("assets/shincolle/" + path)) {
            if (stream == null) throw new IOException("Missing render resource: " + path);
            return new String(stream.readAllBytes(), StandardCharsets.UTF_8);
        }
    }
}
