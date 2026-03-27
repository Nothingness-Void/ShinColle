package com.lulan.shincolle.crafting;

import com.lulan.shincolle.registry.ModItems;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.items.ItemStackHandler;

import java.util.Optional;

public final class SmallShipyardRecipes {

    public static final int SLOT_GRUDGE = 0;
    public static final int SLOT_ABYSSIUM = 1;
    public static final int SLOT_AMMO = 2;
    public static final int SLOT_POLYMETAL = 3;
    public static final int SLOT_FUEL = 4;
    public static final int SLOT_OUTPUT = 5;
    public static final int SLOT_COUNT = 6;
    public static final int MIN_AMOUNT = 16;
    private static final int BASE_POWER = 57600;
    private static final int POWER_PER_MATERIAL = 2100;

    private SmallShipyardRecipes() {
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
            return SLOT_GRUDGE;
        }

        if (stack.is(ModItems.ABYSSMETAL.get())) {
            return SLOT_ABYSSIUM;
        }

        if (stack.is(ModItems.AMMO.get())) {
            return SLOT_AMMO;
        }

        if (stack.is(ModItems.ABYSSMETAL1.get())) {
            return SLOT_POLYMETAL;
        }

        return -1;
    }

    public static boolean isFuel(ItemStack stack) {
        return !stack.isEmpty() && (stack.is(ModItems.INSTANTCONMAT.get()) || getFuelValue(stack) > 0);
    }

    public static int getFuelValue(ItemStack stack) {
        if (stack.isEmpty()) {
            return 0;
        }

        int burnTime = ForgeHooks.getBurnTime(stack, RecipeType.SMELTING);
        if (burnTime > 0) {
            return burnTime;
        }

        Optional<FluidStack> containedFluid = FluidUtil.getFluidContained(stack);
        if (containedFluid.isPresent()) {
            FluidStack fluidStack = containedFluid.get();
            if (fluidStack.getFluid().isSame(Fluids.LAVA) && fluidStack.getAmount() == 1000) {
                return 20000;
            }
        }

        return 0;
    }

    public static int[] getMaterialAmounts(ItemStackHandler items) {
        int[] materialAmounts = new int[4];

        for (int i = 0; i < 4; i++) {
            ItemStack stack = items.getStackInSlot(i);
            materialAmounts[i] = stack.isEmpty() ? 0 : stack.getCount();
        }

        return materialAmounts;
    }

    public static ItemStack createBuildResult(int buildType, int[] materialAmounts, RandomSource random) {
        if (ShipyardBuildTypes.isShipMode(buildType)) {
            return createShipEgg(materialAmounts);
        }

        if (ShipyardBuildTypes.isEquipMode(buildType)) {
            return LegacyEquipmentBuildHelper.buildSmallResult(materialAmounts, random);
        }

        return ItemStack.EMPTY;
    }

    public static ItemStack createShipEgg(int[] materialAmounts) {
        ItemStack egg = new ItemStack(ModItems.SHIPSPAWNEGG_ITEMS.get(0).get());
        CompoundTag tag = new CompoundTag();
        LegacyShipConstructionHelper.writeMaterialAmounts(tag, materialAmounts);
        egg.setTag(tag);
        return egg;
    }
}
