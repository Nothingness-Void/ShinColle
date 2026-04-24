package com.lulan.shincolle.crafting;

import com.lulan.shincolle.registry.ModItems;
import com.lulan.shincolle.registry.ModBlocks;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.ItemStackHandler;

import java.util.Arrays;

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
    public static final int MAX_STOCK = 1000000;
    public static final int COMPRESSED_OUTPUT_AMOUNT = 9;
    public static final int SINGLE_OUTPUT_AMOUNT = 1;
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
        int[] values = materialValues(stack);
        if (values[0] > 0 && values[1] == 0 && values[2] == 0 && values[3] == 0) {
            return 0;
        }
        if (values[1] > 0 && values[0] == 0 && values[2] == 0 && values[3] == 0) {
            return 1;
        }
        if (values[2] > 0 && values[0] == 0 && values[1] == 0 && values[3] == 0) {
            return 2;
        }
        if (values[3] > 0 && values[0] == 0 && values[1] == 0 && values[2] == 0) {
            return 3;
        }
        return -1;
    }

    public static int[] materialValues(ItemStack stack) {
        if (stack.isEmpty()) {
            return new int[4];
        }

        if (stack.is(ModItems.GRUDGE.get()) || stack.is(ModItems.GRUDGE1.get())) {
            return new int[]{1, 0, 0, 0};
        }

        if (stack.is(ModItems.ABYSSMETAL.get())) {
            return new int[]{0, 1, 0, 0};
        }

        if (stack.is(ModItems.ABYSSMETAL1.get())) {
            return new int[]{0, 0, 0, 1};
        }

        if (stack.is(ModItems.AMMO.get())) {
            return new int[]{0, 0, 1, 0};
        }

        if (stack.is(ModItems.AMMO1.get())) {
            return new int[]{0, 0, 9, 0};
        }

        if (stack.is(ModItems.AMMO2.get())) {
            return new int[]{0, 0, 4, 0};
        }

        if (stack.is(ModItems.AMMO3.get())) {
            return new int[]{0, 0, 36, 0};
        }

        if (stack.is(ModBlocks.BLOCK_GRUDGE.get().asItem())) {
            return new int[]{9, 0, 0, 0};
        }

        if (stack.is(ModBlocks.BLOCK_ABYSSIUM.get().asItem())) {
            return new int[]{0, 9, 0, 0};
        }

        if (stack.is(ModBlocks.BLOCK_POLYMETAL.get().asItem())) {
            return new int[]{0, 0, 0, 9};
        }

        if (stack.is(ModBlocks.BLOCK_POLYMETAL_GRAVEL.get().asItem())) {
            return new int[]{0, 0, 0, 4};
        }

        if (stack.is(ModBlocks.BLOCK_GRUDGE_HEAVY_DECO.get().asItem())) {
            return new int[]{18, 0, 0, 0};
        }

        if (stack.is(ModBlocks.BLOCK_GRUDGE_HEAVY.get().asItem())) {
            int[] values = new int[]{81, 0, 0, 0};
            if (stack.hasTag()) {
                int[] mats = stack.getTag().getIntArray("mats");
                for (int i = 0; i < values.length && i < mats.length; i++) {
                    values[i] += mats[i];
                }
            }
            return values;
        }

        return new int[4];
    }

    public static boolean addMaterialStock(int[] stock, ItemStack stack) {
        if (stock == null || stock.length < 4 || stack.isEmpty()) {
            return false;
        }

        int[] values = materialValues(stack);
        if (Arrays.stream(values).allMatch(value -> value == 0)) {
            return false;
        }

        for (int i = 0; i < 4; i++) {
            if (stock[i] > MAX_STOCK || stock[i] + values[i] > MAX_STOCK) {
                return false;
            }
        }

        for (int i = 0; i < 4; i++) {
            stock[i] += values[i];
        }
        return true;
    }

    public static boolean outputMaterialToSlot(ItemStackHandler items, int material, boolean compress) {
        Item item = outputItem(material, compress);
        if (item == null) {
            return false;
        }

        ItemStack output = new ItemStack(item);
        for (int slot = SLOT_FUEL; slot < items.getSlots(); slot++) {
            ItemStack current = items.getStackInSlot(slot);
            if (current.isEmpty()) {
                items.setStackInSlot(slot, output);
                return true;
            }

            if (ItemStack.isSameItemSameTags(current, output) && current.getCount() < current.getMaxStackSize()) {
                current.grow(1);
                items.setStackInSlot(slot, current);
                return true;
            }
        }

        return false;
    }

    private static Item outputItem(int material, boolean compress) {
        if (compress) {
            return switch (material) {
                case 0 -> ModBlocks.BLOCK_GRUDGE.get().asItem();
                case 1 -> ModBlocks.BLOCK_ABYSSIUM.get().asItem();
                case 2 -> ModItems.AMMO1.get();
                case 3 -> ModBlocks.BLOCK_POLYMETAL.get().asItem();
                default -> null;
            };
        }

        return switch (material) {
            case 0 -> ModItems.GRUDGE.get();
            case 1 -> ModItems.ABYSSMETAL.get();
            case 2 -> ModItems.AMMO.get();
            case 3 -> ModItems.ABYSSMETAL1.get();
            default -> null;
        };
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
