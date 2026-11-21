package com.lulan.shincolle.crafting;

import com.lulan.shincolle.item.equipment.LegacyEquipmentFamily;
import com.lulan.shincolle.item.equipment.LegacyEquipmentStatsRepository;
import com.lulan.shincolle.registry.ModItems;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.neoforged.neoforge.registries.DeferredHolder;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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

    // 1.12.2 EnchantHelper.EnchantTable (legacy numeric enchant ids mapped to 1.20.1 registry objects).
    private static final List<List<net.minecraft.resources.ResourceKey<Enchantment>>> ENCHANT_TABLE = List.of(
            List.of(Enchantments.RESPIRATION, Enchantments.THORNS, Enchantments.FROST_WALKER, Enchantments.SHARPNESS,
                    Enchantments.SMITE, Enchantments.BANE_OF_ARTHROPODS, Enchantments.KNOCKBACK, Enchantments.FIRE_ASPECT,
                    Enchantments.LOOTING, Enchantments.EFFICIENCY, Enchantments.FORTUNE, Enchantments.POWER,
                    Enchantments.PUNCH, Enchantments.FLAME, Enchantments.INFINITY, Enchantments.LUCK_OF_THE_SEA),
            List.of(Enchantments.PROTECTION, Enchantments.FIRE_PROTECTION, Enchantments.BLAST_PROTECTION,
                    Enchantments.PROJECTILE_PROTECTION, Enchantments.SILK_TOUCH, Enchantments.UNBREAKING, Enchantments.MENDING),
            List.of(Enchantments.FEATHER_FALLING, Enchantments.AQUA_AFFINITY, Enchantments.DEPTH_STRIDER,
                    Enchantments.SILK_TOUCH, Enchantments.LURE, Enchantments.MENDING));

    private LegacyEquipmentBuildHelper() {
    }

    public static ItemStack buildSmallResult(int[] materialAmounts, RandomSource random, net.minecraft.core.HolderLookup.Provider registries) {
        int totalMaterials = materialAmounts[0] + materialAmounts[1] + materialAmounts[2] + materialAmounts[3];
        float equipRate = Math.min(totalMaterials / 128.0F, 1.0F);

        if (random.nextFloat() < equipRate) {
            String rareType = rollSmallType(materialAmounts, random);
            if (rareType == null) {
                return ItemStack.EMPTY;
            }
            return rollEquipmentOfType(rareType, totalMaterials, random, true, registries);
        }

        if (random.nextInt(2) == 0) {
            return new ItemStack(ModItems.AMMO1.get(), 11 + random.nextInt(11));
        }

        return new ItemStack(ModItems.AMMO3.get(), 2 + random.nextInt(2));
    }

    /** 1.12.2 LargeRecipes.getBuildResultEquip: always rolls equip type then equip instance, no ammo fallback. */
    public static ItemStack buildLargeResult(int[] materialAmounts, RandomSource random, net.minecraft.core.HolderLookup.Provider registries) {
        int totalMaterials = materialAmounts[0] + materialAmounts[1] + materialAmounts[2] + materialAmounts[3];
        String rareType = rollLargeType(materialAmounts, random);
        if (rareType == null) {
            return ItemStack.EMPTY;
        }
        return rollEquipmentOfType(rareType, totalMaterials, random, false, registries);
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

    private static ItemStack rollEquipmentOfType(String rareType, int totalMaterials, RandomSource random, boolean smallBuild, net.minecraft.core.HolderLookup.Provider registries) {
        // 1.12.2 EquipCalc.rollEquipsOfTheType only assigns totalMat for small builds; large builds keep 0.
        int rollMaterials = smallBuild ? (int) (totalMaterials * 15.625F) : 0;
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
                ItemStack stack = createEquipmentStack(candidate.family(), candidate.variantIndex());
                applyRandomEnchant(stack, enchantType(candidate.family()), enchantLevel(totalMaterials, smallBuild), random, registries);
                return stack;
            }
        }

        throw new IllegalStateException("Legacy equipment roll did not select from a non-empty 1.12 probability table: " + rareType);
    }

    /** 1.12.2 EquipCalc.rollEquipsOfTheType enchant level thresholds. */
    public static int enchantLevel(int totalMaterials, boolean smallBuild) {
        if (smallBuild) {
            return totalMaterials > 220 ? 3 : totalMaterials > 200 ? 2 : totalMaterials > 180 ? 1 : 0;
        }
        return totalMaterials > 3500 ? 3 : totalMaterials > 3000 ? 2 : totalMaterials > 2000 ? 1 : 0;
    }

    /** 1.12.2 EquipCalc.getItemStackFromId enchType: 0 weapon, 1 armor, 2 misc. */
    public static int enchantType(LegacyEquipmentFamily family) {
        return switch (family) {
            case CANNON, MACHINEGUN, TORPEDO, AIRPLANE, AMMO -> 0;
            case ARMOR -> 1;
            case RADAR, TURBINE, CATAPULT, DRUM, COMPASS, FLARE, SEARCHLIGHT -> 2;
        };
    }

    /** 1.12.2 EnchantHelper.applyRandomEnchantToEquip. */
    public static void applyRandomEnchant(ItemStack stack, int enchantType, int enchantLevel, RandomSource random, net.minecraft.core.HolderLookup.Provider registries) {
        if (stack.isEmpty() || enchantLevel <= 0) {
            return;
        }

        int roll = random.nextInt(10);
        int enchantCount = switch (enchantLevel) {
            case 1 -> roll > 5 ? 1 : 0;
            case 2 -> roll > 6 ? 2 : roll > 3 ? 1 : 0;
            case 3 -> roll > 7 ? 3 : roll > 4 ? 2 : roll > 1 ? 1 : 0;
            default -> 0;
        };
        if (enchantCount <= 0) {
            return;
        }

        List<net.minecraft.resources.ResourceKey<Enchantment>> table = ENCHANT_TABLE.get(enchantType);
        Map<net.minecraft.core.Holder<Enchantment>, Integer> enchantments = new LinkedHashMap<>();
        for (int i = 0; i < enchantCount; i++) {
            net.minecraft.core.Holder<Enchantment> enchantment = registries.lookupOrThrow(net.minecraft.core.registries.Registries.ENCHANTMENT).getOrThrow(table.get(random.nextInt(table.size())));
            Integer current = enchantments.get(enchantment);
            enchantments.put(enchantment, current == null ? 1 : Math.min(current + 1, enchantment.value().getMaxLevel()));
        }
        net.minecraft.world.item.enchantment.ItemEnchantments.Mutable updated =
                new net.minecraft.world.item.enchantment.ItemEnchantments.Mutable(net.minecraft.world.item.enchantment.ItemEnchantments.EMPTY);
        enchantments.forEach(updated::set);
        EnchantmentHelper.setEnchantments(stack, updated.toImmutable());
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

    private static ItemStack stackOf(DeferredHolder<net.minecraft.world.item.Item, net.minecraft.world.item.Item> item) {
        return new ItemStack(item.get());
    }

    private record TypeRollEntry(String rareType, int mean, int favoredMaterial) {
    }

    private record EquipmentCandidate(LegacyEquipmentFamily family, int variantIndex, float probability) {
    }
}
