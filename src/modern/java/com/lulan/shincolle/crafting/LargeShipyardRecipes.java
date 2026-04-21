package com.lulan.shincolle.crafting;

import com.lulan.shincolle.registry.ModItems;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.ItemStackHandler;

public final class LargeShipyardRecipes {

    public static final int SLOT_OUTPUT = 0;
    public static final int SLOT_FUEL = 1;
    public static final int SLOT_GRUDGE_A = 2;
    public static final int SLOT_GRUDGE_B = 3;
    public static final int SLOT_ABYSSIUM_A = 4;
    public static final int SLOT_ABYSSIUM_B = 5;
    public static final int SLOT_AMMO_A = 6;
    public static final int SLOT_AMMO_B = 7;
    public static final int SLOT_POLYMETAL_A = 8;
    public static final int SLOT_POLYMETAL_B = 9;
    public static final int SLOT_COUNT = 10;

    public static final int MIN_AMOUNT = 100;
    private static final int BASE_POWER = 460800;
    private static final int POWER_PER_MATERIAL = 256;

    private static final int[][] MATERIAL_SLOT_GROUPS = {
            {SLOT_GRUDGE_A, SLOT_GRUDGE_B},
            {SLOT_ABYSSIUM_A, SLOT_ABYSSIUM_B},
            {SLOT_AMMO_A, SLOT_AMMO_B},
            {SLOT_POLYMETAL_A, SLOT_POLYMETAL_B}
    };

    private LargeShipyardRecipes() {
    }

    public static boolean canRecipeBuild(int[] materialAmounts) {
        return materialAmounts[0] >= MIN_AMOUNT
                && materialAmounts[1] >= MIN_AMOUNT
                && materialAmounts[2] >= MIN_AMOUNT
                && materialAmounts[3] >= MIN_AMOUNT;
    }

    public static int calcGoalPower(int[] materialAmounts) {
        if (!canRecipeBuild(materialAmounts)) {
            return 0;
        }

        int extraAmount = materialAmounts[0] + materialAmounts[1] + materialAmounts[2] + materialAmounts[3] - MIN_AMOUNT * 4;
        return BASE_POWER + POWER_PER_MATERIAL * extraAmount;
    }

    public static int materialSlot(ItemStack stack) {
        if (stack.isEmpty()) {
            return -1;
        }

        if (stack.is(ModItems.GRUDGE.get())) {
            return 0;
        }

        if (stack.is(ModItems.ABYSSMETAL.get())) {
            return 1;
        }

        if (stack.is(ModItems.AMMO.get())) {
            return 2;
        }

        if (stack.is(ModItems.ABYSSMETAL1.get())) {
            return 3;
        }

        return -1;
    }

    public static int materialIndexForSlot(int slot) {
        for (int material = 0; material < MATERIAL_SLOT_GROUPS.length; material++) {
            int[] group = MATERIAL_SLOT_GROUPS[material];
            if (slot == group[0] || slot == group[1]) {
                return material;
            }
        }
        return -1;
    }

    public static int[] slotRangeForMaterial(int materialIndex) {
        if (materialIndex < 0 || materialIndex >= MATERIAL_SLOT_GROUPS.length) {
            return new int[]{-1, -1};
        }

        int[] group = MATERIAL_SLOT_GROUPS[materialIndex];
        return new int[]{group[0], group[1] + 1};
    }

    public static boolean isFuel(ItemStack stack) {
        return SmallShipyardRecipes.isFuel(stack);
    }

    public static int getFuelValue(ItemStack stack) {
        return SmallShipyardRecipes.getFuelValue(stack);
    }

    public static int[] getMaterialAmounts(ItemStackHandler items) {
        int[] amounts = new int[4];
        for (int material = 0; material < MATERIAL_SLOT_GROUPS.length; material++) {
            int total = 0;
            for (int slot : MATERIAL_SLOT_GROUPS[material]) {
                ItemStack stack = items.getStackInSlot(slot);
                total += stack.isEmpty() ? 0 : stack.getCount();
            }
            amounts[material] = total;
        }
        return amounts;
    }

    public static boolean hasMaterialAmounts(ItemStackHandler items, int[] requiredAmounts) {
        int[] current = getMaterialAmounts(items);
        for (int i = 0; i < 4; i++) {
            if (requiredAmounts[i] > current[i]) {
                return false;
            }
        }
        return true;
    }

    public static void consumeAllMaterials(ItemStackHandler items) {
        for (int[] group : MATERIAL_SLOT_GROUPS) {
            for (int slot : group) {
                items.setStackInSlot(slot, ItemStack.EMPTY);
            }
        }
    }

    public static void consumeMaterialAmounts(ItemStackHandler items, int[] requiredAmounts) {
        for (int material = 0; material < MATERIAL_SLOT_GROUPS.length; material++) {
            int remaining = Math.max(0, requiredAmounts[material]);
            if (remaining == 0) {
                continue;
            }

            for (int slot : MATERIAL_SLOT_GROUPS[material]) {
                if (remaining <= 0) {
                    break;
                }

                ItemStack stack = items.getStackInSlot(slot);
                if (stack.isEmpty()) {
                    continue;
                }

                int consume = Math.min(remaining, stack.getCount());
                stack.shrink(consume);
                remaining -= consume;

                if (stack.isEmpty()) {
                    items.setStackInSlot(slot, ItemStack.EMPTY);
                } else {
                    items.setStackInSlot(slot, stack);
                }
            }
        }
    }

    public static ItemStack createBuildResult(int buildType, int[] materialAmounts, RandomSource random) {
        if (ShipyardBuildTypes.isShipMode(buildType)) {
            return createShipEgg(materialAmounts);
        }

        if (ShipyardBuildTypes.isEquipMode(buildType)) {
            return LegacyEquipmentBuildHelper.buildLargeResult(materialAmounts, random);
        }

        return ItemStack.EMPTY;
    }

    public static ItemStack createShipEgg(int[] materialAmounts) {
        ItemStack egg = new ItemStack(ModItems.SHIPSPAWNEGG_ITEMS.get(1).get());
        CompoundTag tag = new CompoundTag();
        LegacyShipConstructionHelper.writeMaterialAmounts(tag, materialAmounts);
        egg.setTag(tag);
        return egg;
    }
}
