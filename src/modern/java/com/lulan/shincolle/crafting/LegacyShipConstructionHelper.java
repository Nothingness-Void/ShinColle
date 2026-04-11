package com.lulan.shincolle.crafting;

import com.lulan.shincolle.entity.ship.ShipEntitySpec;
import com.lulan.shincolle.entity.ship.ShipEntitySpecs;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;
import java.util.List;

public final class LegacyShipConstructionHelper {

    public static final String TAG_GRUDGE = "Grudge";
    public static final String TAG_ABYSSIUM = "Abyssium";
    public static final String TAG_AMMO = "Ammo";
    public static final String TAG_POLYMETAL = "Polymetal";

    private static final List<RollEntry> SMALL_ROLLS = List.of(
            new RollEntry(53, 80, 0),
            new RollEntry(54, 90, 0),
            new RollEntry(55, 100, 0),
            new RollEntry(56, 110, 0),
            new RollEntry(58, 120, 1),
            new RollEntry(59, 140, 1),
            new RollEntry(40, 160, 2),
            new RollEntry(41, 180, 2),
            new RollEntry(60, 220, 2),
            new RollEntry(61, 256, 2));

    private static final List<RollEntry> LARGE_ROLLS = List.of(
            new RollEntry(39, 650, 2),
            new RollEntry(62, 800, 2),
            new RollEntry(63, 900, 2),
            new RollEntry(64, 1000, 2),
            new RollEntry(65, 1100, 2),
            new RollEntry(49, 1400, 3),
            new RollEntry(50, 1500, 3),
            new RollEntry(48, 1800, 2),
            new RollEntry(60, 2000, 1),
            new RollEntry(61, 2200, 1));

    private LegacyShipConstructionHelper() {
    }

    public static boolean hasConstructionRecipe(ItemStack stack) {
        return hasConstructionRecipe(stack.getTag());
    }

    public static boolean hasConstructionRecipe(@Nullable CompoundTag tag) {
        return tag != null
                && tag.contains(TAG_GRUDGE)
                && tag.contains(TAG_ABYSSIUM)
                && tag.contains(TAG_AMMO)
                && tag.contains(TAG_POLYMETAL);
    }

    public static int[] readMaterialAmounts(ItemStack stack) {
        return readMaterialAmounts(stack.getTag());
    }

    public static int[] readMaterialAmounts(@Nullable CompoundTag tag) {
        if (tag == null) {
            return new int[]{0, 0, 0, 0};
        }

        return new int[]{
                tag.getInt(TAG_GRUDGE),
                tag.getInt(TAG_ABYSSIUM),
                tag.getInt(TAG_AMMO),
                tag.getInt(TAG_POLYMETAL)
        };
    }

    public static void writeMaterialAmounts(CompoundTag tag, int[] materials) {
        tag.putInt(TAG_GRUDGE, safeAt(materials, 0));
        tag.putInt(TAG_ABYSSIUM, safeAt(materials, 1));
        tag.putInt(TAG_AMMO, safeAt(materials, 2));
        tag.putInt(TAG_POLYMETAL, safeAt(materials, 3));
    }

    public static @Nullable ShipEntitySpec resolveConstructionEgg(ItemStack stack, String itemPath, RandomSource random) {
        CompoundTag tag = stack.getTag();
        if (!hasConstructionRecipe(tag)) {
            return null;
        }

        int[] materials = readMaterialAmounts(tag);
        if ("largeegg".equals(itemPath)) {
            return rollShipType(materials, LARGE_ROLLS, false, random);
        }

        if ("smallegg".equals(itemPath)) {
            return rollShipType(materials, SMALL_ROLLS, true, random);
        }

        return null;
    }

    private static ShipEntitySpec rollShipType(int[] materials, List<RollEntry> table, boolean smallBuild, RandomSource random) {
        int totalMaterials = materials[0] + materials[1] + materials[2] + materials[3];
        float[] probabilities = new float[table.size()];
        float totalProbability = 0.0F;

        for (int i = 0; i < table.size(); i++) {
            RollEntry entry = table.get(i);
            int mean = entry.favoredMaterial() >= 0 && entry.favoredMaterial() < materials.length
                    ? entry.mean() - materials[entry.favoredMaterial()]
                    : entry.mean();
            int meanDistance = Math.abs(totalMaterials - mean);
            if (smallBuild) {
                meanDistance = (int) (meanDistance * 15.625F);
            }

            probabilities[i] = LegacyBuildRollHelper.normalProbability(meanDistance);
            totalProbability += probabilities[i];
        }

        float randomValue = random.nextFloat() * totalProbability;
        float accumulated = 0.0125F;

        for (int i = 0; i < table.size(); i++) {
            accumulated += probabilities[i];
            if (accumulated > randomValue) {
                return ShipEntitySpecs.getByEggMeta(table.get(i).eggMeta());
            }
        }

        return smallBuild ? ShipEntitySpecs.randomConstructionSmall(random) : ShipEntitySpecs.randomConstructionLarge(random);
    }

    private static int safeAt(int[] values, int index) {
        return index >= 0 && index < values.length ? values[index] : 0;
    }

    private record RollEntry(int eggMeta, int mean, int favoredMaterial) {
    }
}
