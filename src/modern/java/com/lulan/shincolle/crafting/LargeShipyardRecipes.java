package com.lulan.shincolle.crafting;

import com.lulan.shincolle.item.LegacyItemData;
import com.lulan.shincolle.config.ShinColleConfig;
import com.lulan.shincolle.entity.ship.LegacyShipBehaviorCatalog;
import com.lulan.shincolle.item.LegacyShipSpawnEggItem;
import com.lulan.shincolle.item.equipment.LegacyEquipmentFamily;
import com.lulan.shincolle.item.equipment.LegacyEquipmentItem;
import com.lulan.shincolle.registry.ModItems;
import com.lulan.shincolle.registry.ModBlocks;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.ItemStackHandler;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.List;

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
            if (LegacyItemData.hasTag(stack)) {
                int[] mats = LegacyItemData.getTag(stack).getIntArray("mats");
                for (int i = 0; i < values.length && i < mats.length; i++) {
                    values[i] += mats[i];
                }
            }
            return values;
        }

        return new int[4];
    }

    /** 1.12.2 LargeRecipes.addMaterialStock: resource items, equipment recycle, ship egg kaitai and heavy grudge. */
    public static boolean addMaterialStock(int[] stock, ItemStack stack, RandomSource random) {
        if (stock == null || stock.length < 4 || stack.isEmpty()) {
            return false;
        }

        for (int i = 0; i < 4; i++) {
            if (stock[i] > MAX_STOCK) {
                return false;
            }
        }

        int[] values;
        if (stack.getItem() instanceof LegacyEquipmentItem equipment) {
            values = equipmentResourceValues(equipment.family(), equipment.variantIndex(), random);
        } else if (stack.getItem() instanceof LegacyShipSpawnEggItem) {
            values = shipEggKaitaiValues(stack, random);
            if (values == null) {
                return false;
            }
        } else {
            values = materialValues(stack);
            if (Arrays.stream(values).allMatch(value -> value == 0)) {
                return false;
            }
        }

        int multiplier = ShinColleConfig.easyMode() ? 10 : 1;
        for (int i = 0; i < 4; i++) {
            stock[i] += values[i] * multiplier;
        }
        return true;
    }

    public static int compressedOutputAmount() {
        return ShinColleConfig.easyMode() ? 90 : COMPRESSED_OUTPUT_AMOUNT;
    }

    public static int singleOutputAmount() {
        return ShinColleConfig.easyMode() ? 10 : SINGLE_OUTPUT_AMOUNT;
    }

    /** 1.12.2 ShipSpawnEgg branch: ShipCalc.getKaitaiItems(meta - 2), each item value times stack size. */
    private static @Nullable int[] shipEggKaitaiValues(ItemStack stack, RandomSource random) {
        String path = BuiltInRegistries.ITEM.getKey(stack.getItem()).getPath();
        List<ItemStack> drops;
        if ("smallegg".equals(path)) {
            drops = legacyEggKaitaiDrops(10, random);
        } else if ("largeegg".equals(path)) {
            drops = legacyEggKaitaiDrops(90, random);
        } else {
            int eggMeta;
            CompoundTag tag = LegacyItemData.getTag(stack);
            if (tag != null && tag.contains(LegacyShipSpawnEggItem.VARIANT_EGG_META_TAG)) {
                eggMeta = tag.getInt(LegacyShipSpawnEggItem.VARIANT_EGG_META_TAG);
            } else if (path.startsWith("shipegg")) {
                try {
                    eggMeta = Integer.parseInt(path.substring("shipegg".length()));
                } catch (NumberFormatException e) {
                    return null;
                }
            } else {
                return null;
            }
            drops = LegacyShipBehaviorCatalog.createLegacyShipyardKaitaiDrops(eggMeta - 2, random);
            if (drops == null) {
                return null;
            }
        }

        int[] total = new int[4];
        for (ItemStack drop : drops) {
            int[] values = materialValues(drop);
            for (int i = 0; i < 4; i++) {
                total[i] += values[i] * drop.getCount();
            }
        }
        return total;
    }

    private static List<ItemStack> legacyEggKaitaiDrops(int base, RandomSource random) {
        return List.of(
                new ItemStack(ModItems.GRUDGE.get(), base + random.nextInt(8)),
                new ItemStack(ModItems.ABYSSMETAL.get(), base + random.nextInt(8)),
                new ItemStack(ModItems.AMMO.get(), base + random.nextInt(8)),
                new ItemStack(ModItems.ABYSSMETAL1.get(), base + random.nextInt(8)));
    }

    /** 1.12.2 Equip*.getResourceValue, switched per item class on the legacy equip type. */
    public static int[] equipmentResourceValues(LegacyEquipmentFamily family, int variantIndex, RandomSource random) {
        String type = family.definitionKey(variantIndex).split(":", 2)[0];
        return switch (family) {
            case AIRPLANE -> switch (type) {
                case "AIR_T_LO", "AIR_F_LO", "AIR_B_LO" -> roll(random, 20, 80, 30, 100, 40, 120, 50, 150);
                case "AIR_T_HI", "AIR_F_HI", "AIR_B_HI" -> roll(random, 50, 130, 60, 170, 70, 210, 75, 230);
                case "AIR_R_LO" -> roll(random, 12, 3, 14, 5, 14, 5, 16, 11);
                case "AIR_R_HI" -> roll(random, 10, 40, 15, 50, 20, 60, 25, 80);
                default -> new int[4];
            };
            case AMMO -> switch (type) {
                case "AMMO_LO" -> roll(random, 3, 4, 4, 7, 5, 9, 2, 4);
                case "AMMO_HI" -> roll(random, 25, 35, 30, 45, 40, 70, 20, 40);
                default -> new int[4];
            };
            case ARMOR -> switch (type) {
                case "ARMOR_LO" -> roll(random, 3, 3, 4, 4, 2, 2, 2, 2);
                case "ARMOR_HI" -> roll(random, 15, 35, 20, 45, 10, 25, 5, 15);
                default -> new int[4];
            };
            case CANNON -> switch (type) {
                case "CANNON_SI" -> roll(random, 4, 5, 4, 5, 5, 11, 3, 3);
                case "CANNON_TW_LO" -> roll(random, 7, 10, 7, 10, 8, 16, 6, 6);
                case "CANNON_TW_HI" -> roll(random, 10, 50, 15, 70, 35, 90, 20, 80);
                case "CANNON_TR" -> roll(random, 60, 170, 70, 210, 80, 250, 50, 130);
                default -> new int[4];
            };
            case CATAPULT -> switch (type) {
                case "CATAPULT_LO" -> roll(random, 40, 120, 50, 150, 30, 80, 60, 180);
                case "CATAPULT_HI" -> roll(random, 70, 190, 85, 230, 55, 150, 90, 250);
                default -> new int[4];
            };
            case COMPASS -> "COMPASS_LO".equals(type) ? roll(random, 5, 5, 3, 4, 2, 2, 2, 2) : new int[4];
            case DRUM -> "DRUM_LO".equals(type) ? roll(random, 4, 5, 5, 9, 4, 4, 3, 3) : new int[4];
            case FLARE -> "FLARE_LO".equals(type) ? roll(random, 2, 2, 3, 3, 4, 4, 2, 2) : new int[4];
            case MACHINEGUN -> switch (type) {
                case "GUN_LO" -> roll(random, 3, 4, 4, 7, 5, 8, 2, 4);
                case "GUN_HI" -> roll(random, 20, 30, 25, 40, 30, 50, 15, 20);
                default -> new int[4];
            };
            case RADAR -> switch (type) {
                case "RADAR_LO" -> roll(random, 7, 12, 6, 10, 5, 9, 4, 7);
                case "RADAR_HI" -> roll(random, 40, 110, 35, 90, 30, 70, 25, 50);
                default -> new int[4];
            };
            case SEARCHLIGHT -> "SEARCHLIGHT_LO".equals(type) ? roll(random, 4, 4, 3, 3, 2, 2, 2, 2) : new int[4];
            case TORPEDO -> switch (type) {
                case "TORPEDO_LO" -> roll(random, 4, 8, 5, 8, 6, 12, 4, 5);
                case "TORPEDO_HI" -> roll(random, 20, 60, 25, 70, 30, 80, 15, 45);
                default -> new int[4];
            };
            case TURBINE -> switch (type) {
                case "TURBINE_LO" -> roll(random, 35, 90, 25, 80, 15, 45, 20, 60);
                case "TURBINE_HI" -> roll(random, 70, 200, 55, 170, 25, 90, 40, 130);
                default -> new int[4];
            };
        };
    }

    private static int[] roll(RandomSource random, int b0, int a0, int b1, int a1, int b2, int a2, int b3, int a3) {
        return new int[]{random.nextInt(b0) + a0, random.nextInt(b1) + a1, random.nextInt(b2) + a2, random.nextInt(b3) + a3};
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

            if (ItemStack.isSameItemSameComponents(current, output) && current.getCount() < current.getMaxStackSize()) {
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

    public static ItemStack createBuildResult(int buildType, int[] materialAmounts, RandomSource random, net.minecraft.core.HolderLookup.Provider registries) {
        if (ShipyardBuildTypes.isShipMode(buildType)) {
            return createShipEgg(materialAmounts);
        }

        if (ShipyardBuildTypes.isEquipMode(buildType)) {
            return LegacyEquipmentBuildHelper.buildLargeResult(materialAmounts, random, registries);
        }

        return ItemStack.EMPTY;
    }

    public static ItemStack createShipEgg(int[] materialAmounts) {
        ItemStack egg = new ItemStack(ModItems.SHIPSPAWNEGG_ITEMS.get(1).get());
        CompoundTag tag = new CompoundTag();
        LegacyShipConstructionHelper.writeMaterialAmounts(tag, materialAmounts);
        LegacyItemData.setTag(egg, tag);
        return egg;
    }
}
