package com.lulan.shincolle.crafting;

import com.lulan.shincolle.item.LegacyItemData;
import com.lulan.shincolle.config.ShinColleConfig;
import com.lulan.shincolle.registry.ModBlocks;
import com.lulan.shincolle.registry.ModItems;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Objects;

/**
 * Mirrors legacy 1.12.2 InventoryHelper#matchTargetItem semantics across Forge item tags, damage, and NBT.
 */
public final class LegacyItemMatchHelper {

    private static final TagKey<Item> TAG_DUSTS_MANGANESE =
            TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath("c", "dusts/manganese"));
    private static final TagKey<Item> TAG_INGOTS_MANGANESE =
            TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath("c", "ingots/manganese"));
    private static final TagKey<Item> TAG_NUGGETS_MANGANESE =
            TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath("c", "nuggets/manganese"));
    private static final TagKey<Item> TAG_ORES_MANGANESE =
            TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath("c", "ores/manganese"));
    private static final TagKey<Item> TAG_STORAGE_BLOCKS_MANGANESE =
            TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath("c", "storage_blocks/manganese"));
    private static final List<TagKey<Item>> MANGANESE_POLY_TAGS = List.of(
            TAG_DUSTS_MANGANESE,
            TAG_INGOTS_MANGANESE,
            TAG_NUGGETS_MANGANESE,
            TAG_ORES_MANGANESE,
            TAG_STORAGE_BLOCKS_MANGANESE);

    private LegacyItemMatchHelper() {
    }

    public static boolean matchesTag(ItemStack stack, TagKey<Item> tag) {
        if (stack.isEmpty() || tag == null) {
            return false;
        }
        if (stack.is(tag)) {
            return true;
        }
        if (!ShinColleConfig.polyAsMn()) {
            return false;
        }
        Item item = stack.getItem();
        if (item == ModItems.ABYSSMETAL1.get()) {
            return tag.equals(TAG_DUSTS_MANGANESE) || tag.equals(TAG_INGOTS_MANGANESE);
        }
        if (item == ModItems.ABYSSNUGGET1.get()) {
            return tag.equals(TAG_NUGGETS_MANGANESE);
        }
        if (item == ModBlocks.BLOCK_POLYMETAL_ORE.get().asItem()) {
            return tag.equals(TAG_ORES_MANGANESE);
        }
        if (item == ModBlocks.BLOCK_POLYMETAL.get().asItem() || item == ModBlocks.BLOCK_ABYSSIUM.get().asItem()) {
            return tag.equals(TAG_STORAGE_BLOCKS_MANGANESE);
        }
        return false;
    }

    public static boolean matchesTargetItem(ItemStack target, ItemStack template,
                                            boolean checkMetadata, boolean checkOredict,
                                            boolean checkNbt) {
        if (target.isEmpty() || template.isEmpty()) {
            return target.isEmpty() && template.isEmpty();
        }

        if (target.is(template.getItem())) {
            if (checkMetadata && target.getDamageValue() != template.getDamageValue()) {
                return false;
            }
            if (checkNbt && !matchesLegacyNbt(target, template)) {
                return false;
            }
            return true;
        }

        if (!checkOredict) {
            return false;
        }
        if (target.getTags().anyMatch(tag -> template.getTags().anyMatch(tag::equals))) {
            return true;
        }
        if (ShinColleConfig.polyAsMn()) {
            for (TagKey<Item> manganeseTag : MANGANESE_POLY_TAGS) {
                if (matchesTag(target, manganeseTag) && matchesTag(template, manganeseTag)) {
                    return true;
                }
            }
        }
        return false;
    }

    private static boolean matchesLegacyNbt(ItemStack target, ItemStack template) {
        // In 1.20.1, item damage lives inside ItemStack's "Damage" NBT key instead of metadata;
        // exclude it here so checkNbt stays independent of checkMetadata as in 1.12.2.
        CompoundTag targetTag = stripDamageTag(LegacyItemData.getTag(target));
        CompoundTag templateTag = stripDamageTag(LegacyItemData.getTag(template));
        return Objects.equals(targetTag, templateTag);
    }

    private static @Nullable CompoundTag stripDamageTag(@Nullable CompoundTag tag) {
        if (tag == null || tag.isEmpty()) {
            return null;
        }
        if (!tag.contains("Damage")) {
            return tag;
        }

        CompoundTag copy = tag.copy();
        copy.remove("Damage");
        return copy.isEmpty() ? null : copy;
    }
}
