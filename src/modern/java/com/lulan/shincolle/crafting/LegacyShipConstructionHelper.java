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
            new RollEntry(2, 80, 0),
            new RollEntry(3, 90, 0),
            new RollEntry(4, 100, 0),
            new RollEntry(5, 110, 0),
            new RollEntry(18, 120, 1),
            new RollEntry(19, 140, 2),
            new RollEntry(20, 160, 2),
            new RollEntry(21, 180, 2),
            new RollEntry(11, 200, 2),
            new RollEntry(12, 256, 2));

    private static final List<RollEntry> LARGE_ROLLS = List.of(
            new RollEntry(29, 500, 0),
            new RollEntry(14, 650, 3),
            new RollEntry(16, 800, 2),
            new RollEntry(15, 800, 2),
            new RollEntry(51, 2000, 2),
            new RollEntry(33, 2600, 1),
            new RollEntry(74, 2600, 2),
            new RollEntry(31, 2700, 1),
            new RollEntry(30, 2800, 1),
            new RollEntry(23, 3000, 1),
            new RollEntry(22, 3000, 3),
            new RollEntry(46, 3500, 2),
            new RollEntry(17, 3800, 2),
            new RollEntry(28, 4600, 2),
            new RollEntry(32, 4800, 1),
            new RollEntry(35, 5000, 3));

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

        throw new IllegalStateException("Legacy ship construction roll did not select from a non-empty 1.12 probability table");
    }

    private static int safeAt(int[] values, int index) {
        return index >= 0 && index < values.length ? values[index] : 0;
    }

    private record RollEntry(int eggMeta, int mean, int favoredMaterial) {
    }
}
