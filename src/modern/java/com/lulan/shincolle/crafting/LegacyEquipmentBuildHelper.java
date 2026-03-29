package com.lulan.shincolle.crafting;

import com.lulan.shincolle.item.equipment.LegacyEquipmentFamily;
import com.lulan.shincolle.item.equipment.LegacyEquipmentStatsRepository;
import com.lulan.shincolle.registry.ModItems;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.RegistryObject;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public final class LegacyEquipmentBuildHelper {

    private static final List<TypeRollEntry> SMALL_TYPE_ROLLS = List.of(
            new TypeRollEntry("ARMOR_LO", 80, 1),
            new TypeRollEntry("FLARE_LO", 80, 2),
            new TypeRollEntry("SEARCHLIGHT_LO", 80, 0),
            new TypeRollEntry("COMPASS_LO", 90, 0),
            new TypeRollEntry("GUN_LO", 100, 2),
            new TypeRollEntry("DRUM_LO", 120, 1),
            new TypeRollEntry("AMMO_LO", 120, 2),
            new TypeRollEntry("CANNON_SI", 128, 2),
            new TypeRollEntry("TORPEDO_LO", 160, 2),
            new TypeRollEntry("RADAR_LO", 200, 0),
            new TypeRollEntry("AIR_R_LO", 256, 3),
            new TypeRollEntry("CANNON_TW_LO", 320, 2));

    private static final List<TypeRollEntry> LARGE_TYPE_ROLLS = List.of(
            new TypeRollEntry("ARMOR_HI", 500, 1),
            new TypeRollEntry("GUN_HI", 800, 2),
            new TypeRollEntry("AMMO_HI", 1000, 2),
            new TypeRollEntry("AIR_R_HI", 1000, 3),
            new TypeRollEntry("TORPEDO_HI", 1200, 2),
            new TypeRollEntry("TURBINE_LO", 1400, 0),
            new TypeRollEntry("CANNON_TW_HI", 1600, 2),
            new TypeRollEntry("RADAR_HI", 2000, 0),
            new TypeRollEntry("AIR_T_LO", 2400, 3),
            new TypeRollEntry("AIR_F_LO", 2400, 3),
            new TypeRollEntry("AIR_B_LO", 2400, 3),
            new TypeRollEntry("CATAPULT_LO", 2800, 3),
            new TypeRollEntry("TURBINE_HI", 3200, 0),
            new TypeRollEntry("AIR_T_HI", 3800, 3),
            new TypeRollEntry("AIR_F_HI", 3800, 3),
            new TypeRollEntry("AIR_B_HI", 3800, 3),
            new TypeRollEntry("CANNON_TR", 4400, 2),
            new TypeRollEntry("CATAPULT_HI", 5000, 3));

    private LegacyEquipmentBuildHelper() {
    }

    public static ItemStack buildSmallResult(int[] materialAmounts, RandomSource random) {
        int totalMaterials = materialAmounts[0] + materialAmounts[1] + materialAmounts[2] + materialAmounts[3];
        float equipRate = Math.min(totalMaterials / 128.0F, 1.0F);

        if (random.nextFloat() < equipRate) {
            String rareType = rollSmallType(materialAmounts, random);
            if (rareType != null) {
                ItemStack equipment = rollEquipmentOfType(rareType, totalMaterials, random, true);
                if (!equipment.isEmpty()) {
                    return equipment;
                }
            }
        }

        if (random.nextBoolean()) {
            return new ItemStack(ModItems.AMMO1.get(), 11 + random.nextInt(11));
        }

        return new ItemStack(ModItems.AMMO3.get(), 2 + random.nextInt(2));
    }

    public static ItemStack buildLargeResult(int[] materialAmounts, RandomSource random) {
        int totalMaterials = materialAmounts[0] + materialAmounts[1] + materialAmounts[2] + materialAmounts[3];
        float equipRate = Math.min(totalMaterials / 2000.0F, 1.0F);

        if (random.nextFloat() < equipRate) {
            String rareType = rollLargeType(materialAmounts, random);
            if (rareType != null) {
                ItemStack equipment = rollEquipmentOfType(rareType, totalMaterials, random, false);
                if (!equipment.isEmpty()) {
                    return equipment;
                }
            }
        }

        if (random.nextBoolean()) {
            return new ItemStack(ModItems.AMMO2.get(), 8 + random.nextInt(9));
        }

        return new ItemStack(ModItems.AMMO3.get(), 3 + random.nextInt(3));
    }

    private static @Nullable String rollSmallType(int[] materialAmounts, RandomSource random) {
        float[] probabilities = new float[SMALL_TYPE_ROLLS.size()];
        float totalProbability = 0.0F;
        int totalMaterials = materialAmounts[0] + materialAmounts[1] + materialAmounts[2] + materialAmounts[3];

        for (int i = 0; i < SMALL_TYPE_ROLLS.size(); i++) {
            TypeRollEntry entry = SMALL_TYPE_ROLLS.get(i);
            int mean = entry.favoredMaterial() >= 0 && entry.favoredMaterial() < materialAmounts.length
                    ? entry.mean() - materialAmounts[entry.favoredMaterial()]
                    : entry.mean();
            int meanDistance = (int) (Math.abs(totalMaterials - mean) * 15.625F);
            probabilities[i] = LegacyBuildRollHelper.normalProbability(meanDistance);
            totalProbability += probabilities[i];
        }

        float randomValue = random.nextFloat() * totalProbability;
        float accumulated = 0.0125F;
        for (int i = 0; i < SMALL_TYPE_ROLLS.size(); i++) {
            accumulated += probabilities[i];
            if (accumulated > randomValue) {
                return SMALL_TYPE_ROLLS.get(i).rareType();
            }
        }

        return null;
    }

    private static @Nullable String rollLargeType(int[] materialAmounts, RandomSource random) {
        float[] probabilities = new float[LARGE_TYPE_ROLLS.size()];
        float totalProbability = 0.0F;
        int totalMaterials = materialAmounts[0] + materialAmounts[1] + materialAmounts[2] + materialAmounts[3];

        for (int i = 0; i < LARGE_TYPE_ROLLS.size(); i++) {
            TypeRollEntry entry = LARGE_TYPE_ROLLS.get(i);
            int mean = entry.favoredMaterial() >= 0 && entry.favoredMaterial() < materialAmounts.length
                    ? entry.mean() - materialAmounts[entry.favoredMaterial()]
                    : entry.mean();
            int meanDistance = Math.abs(totalMaterials - mean);
            probabilities[i] = LegacyBuildRollHelper.normalProbability(meanDistance);
            totalProbability += probabilities[i];
        }

        float randomValue = random.nextFloat() * totalProbability;
        float accumulated = 0.0125F;
        for (int i = 0; i < LARGE_TYPE_ROLLS.size(); i++) {
            accumulated += probabilities[i];
            if (accumulated > randomValue) {
                return LARGE_TYPE_ROLLS.get(i).rareType();
            }
        }

        return null;
    }

    private static ItemStack rollEquipmentOfType(String rareType, int totalMaterials, RandomSource random, boolean smallBuild) {
        int rollMaterials = smallBuild ? (int) (totalMaterials * 15.625F) : totalMaterials;
        List<EquipmentCandidate> candidates = new ArrayList<>();
        float totalProbability = 0.0F;

        for (LegacyEquipmentFamily family : LegacyEquipmentFamily.values()) {
            for (int index = 0; index < family.variantCount(); index++) {
                String definitionKey = family.definitionKey(index);
                LegacyEquipmentStatsRepository.MiscData miscData = LegacyEquipmentStatsRepository.getMisc(definitionKey);
                if (miscData == null || !rareType.equals(miscData.rareType())) {
                    continue;
                }

                int meanDistance = Math.abs(rollMaterials - miscData.rareMean());
                float probability = LegacyBuildRollHelper.normalProbability(meanDistance);
                totalProbability += probability;
                candidates.add(new EquipmentCandidate(family, index, probability));
            }
        }

        if (candidates.isEmpty()) {
            return ItemStack.EMPTY;
        }

        float randomValue = random.nextFloat() * totalProbability;
        float accumulated = 0.0125F;
        for (EquipmentCandidate candidate : candidates) {
            accumulated += candidate.probability();
            if (accumulated > randomValue) {
                return createEquipmentStack(candidate.family(), candidate.variantIndex());
            }
        }

        EquipmentCandidate fallback = candidates.get(0);
        return createEquipmentStack(fallback.family(), fallback.variantIndex());
    }

    private static ItemStack createEquipmentStack(LegacyEquipmentFamily family, int index) {
        return switch (family) {
            case AIRPLANE -> stackOf(ModItems.EQUIPAIRPLANE_ITEMS.get(index));
            case AMMO -> stackOf(ModItems.EQUIPAMMO_ITEMS.get(index));
            case ARMOR -> stackOf(ModItems.EQUIPARMOR_ITEMS.get(index));
            case CANNON -> stackOf(ModItems.EQUIPCANNON_ITEMS.get(index));
            case CATAPULT -> stackOf(ModItems.EQUIPCATAPULT_ITEMS.get(index));
            case COMPASS -> stackOf(ModItems.EQUIPCOMPASS);
            case DRUM -> stackOf(ModItems.EQUIPDRUM_ITEMS.get(index));
            case FLARE -> stackOf(ModItems.EQUIPFLARE);
            case MACHINEGUN -> stackOf(ModItems.EQUIPMACHINEGUN_ITEMS.get(index));
            case RADAR -> stackOf(ModItems.EQUIPRADAR_ITEMS.get(index));
            case SEARCHLIGHT -> stackOf(ModItems.EQUIPSEARCHLIGHT);
            case TORPEDO -> stackOf(ModItems.EQUIPTORPEDO_ITEMS.get(index));
            case TURBINE -> stackOf(ModItems.EQUIPTURBINE_ITEMS.get(index));
        };
    }

    private static ItemStack stackOf(RegistryObject<net.minecraft.world.item.Item> item) {
        return new ItemStack(item.get());
    }

    private record TypeRollEntry(String rareType, int mean, int favoredMaterial) {
    }

    private record EquipmentCandidate(LegacyEquipmentFamily family, int variantIndex, float probability) {
    }
}
