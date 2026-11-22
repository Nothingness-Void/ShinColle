package com.lulan.shincolle.gametest;

import com.lulan.shincolle.ShinColle;
import com.lulan.shincolle.blockentity.CraneBlockEntity;
import com.lulan.shincolle.crafting.LegacyItemMatchHelper;
import com.lulan.shincolle.registry.ModBlocks;
import com.lulan.shincolle.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.ArrayList;
import java.util.List;

@GameTestHolder(ShinColle.MOD_ID)
@PrefixGameTestTemplate(false)
public final class LegacyItemMatchGameTests {
    private LegacyItemMatchGameTests() {}

    @GameTest(template = "gameplayparitygametests.empty")
    public static void bucketContentsDoNotMatchThroughGenericContainerTags(GameTestHelper helper) {
        BlockPos pos = helper.absolutePos(new BlockPos(2, 2, 2));
        var crane = new CraneBlockEntity(pos, ModBlocks.BLOCK_CRANE.get().defaultBlockState());
        crane.setCheckOredict(true);
        crane.setFilter(0, new ItemStack(Items.WATER_BUCKET), false);
        crane.setFilter(9, new ItemStack(Items.WATER_BUCKET), false);
        for (String namespace : List.of("c", "forge")) {
            withSharedTag(Items.WATER_BUCKET, Items.LAVA_BUCKET, tag(namespace, "buckets"), () -> {
                helper.assertTrue(!LegacyItemMatchHelper.matchesTargetItem(
                                new ItemStack(Items.LAVA_BUCKET), new ItemStack(Items.WATER_BUCKET), false, true, false)
                                && !LegacyItemMatchHelper.matchesTargetItem(
                                new ItemStack(Items.WATER_BUCKET), new ItemStack(Items.LAVA_BUCKET), false, true, false),
                        "generic " + namespace + ":buckets must not equate different contents");
                helper.assertTrue(!crane.matchesTransferFilter(new ItemStack(Items.LAVA_BUCKET), true)
                                && !crane.matchesTransferFilter(new ItemStack(Items.LAVA_BUCKET), false),
                        "water-bucket load and unload filters must reject lava with ore matching on");
                helper.assertTrue(crane.matchesTransferFilter(new ItemStack(Items.WATER_BUCKET), true)
                                && crane.matchesTransferFilter(new ItemStack(Items.WATER_BUCKET), false),
                        "identical water buckets must still match");
            });
        }
        helper.assertTrue(!crane.matchesTransferFilter(new ItemStack(Items.BUCKET), true),
                "an empty bucket is not a water bucket");
        helper.succeed();
    }

    @GameTest(template = "gameplayparitygametests.empty")
    public static void specificIngotEquivalenceSurvivesGenericCategoryFiltering(GameTestHelper helper) {
        ItemStack alternateIngot = new ItemStack(ModItems.ABYSSMETAL.get());
        ItemStack iron = new ItemStack(Items.IRON_INGOT);
        for (String namespace : List.of("c", "forge")) {
            withSharedTag(alternateIngot.getItem(), iron.getItem(), tag(namespace, "ingots/iron"), () -> {
                helper.assertTrue(LegacyItemMatchHelper.matchesTargetItem(alternateIngot, iron, true, true, true)
                                && LegacyItemMatchHelper.matchesTargetItem(iron, alternateIngot, true, true, true),
                        "a specific shared ingot material must preserve legacy ore equivalence");
                helper.assertTrue(!LegacyItemMatchHelper.matchesTargetItem(alternateIngot, iron, false, false, false),
                        "different ingot items require the ore toggle");
            });
            withSharedTag(Items.IRON_INGOT, Items.GOLD_INGOT, tag(namespace, "ingots"), () ->
                    helper.assertTrue(!LegacyItemMatchHelper.matchesTargetItem(
                                    iron, new ItemStack(Items.GOLD_INGOT), false, true, false),
                            "generic ingots must not equate different materials"));
        }
        helper.assertTrue(LegacyItemMatchHelper.matchesTargetItem(ItemStack.EMPTY, ItemStack.EMPTY, true, true, true)
                        && !LegacyItemMatchHelper.matchesTargetItem(ItemStack.EMPTY, iron, false, true, false)
                        && !LegacyItemMatchHelper.matchesTargetItem(iron, ItemStack.EMPTY, false, true, false),
                "empty inputs retain their legacy matching semantics");
        helper.succeed();
    }

    private static TagKey<Item> tag(String namespace, String path) {
        return TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath(namespace, path));
    }

    private static void withSharedTag(Item first, Item second, TagKey<Item> tag, Runnable assertions) {
        var firstHolder = first.builtInRegistryHolder();
        var secondHolder = second.builtInRegistryHolder();
        var firstTags = firstHolder.tags().toList();
        var secondTags = secondHolder.tags().toList();
        var extendedFirst = new ArrayList<>(firstTags);
        var extendedSecond = new ArrayList<>(secondTags);
        extendedFirst.add(tag);
        extendedSecond.add(tag);
        try {
            firstHolder.bindTags(extendedFirst);
            secondHolder.bindTags(extendedSecond);
            assertions.run();
        } finally {
            firstHolder.bindTags(firstTags);
            secondHolder.bindTags(secondTags);
        }
    }
}
