package com.lulan.shincolle.item;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.component.CustomData;
import net.neoforged.neoforge.fluids.FluidStack;

public final class LegacyFluidData {
    private LegacyFluidData() {
    }

    public static FluidStack read(CompoundTag tag) {
        ResourceLocation id = ResourceLocation.tryParse(tag.getString("FluidName"));
        if (id == null || tag.getInt("Amount") <= 0) {
            return FluidStack.EMPTY;
        }
        return BuiltInRegistries.FLUID.getOptional(id).map(fluid -> {
            FluidStack stack = new FluidStack(fluid, tag.getInt("Amount"));
            if (tag.contains("Tag", 10)) {
                stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag.getCompound("Tag")));
            }
            return stack;
        }).orElse(FluidStack.EMPTY);
    }

    public static CompoundTag upgradeTank(HolderLookup.Provider registries, CompoundTag tag) {
        if (tag.getCompound("Fluid").contains("id", 8)) {
            return tag;
        }
        CompoundTag legacy = tag.contains("FluidName", 8) ? tag : tag.getCompound("Fluid");
        if (legacy.contains("FluidName", 8)) {
            FluidStack fluid = read(legacy);
            if (!fluid.isEmpty()) {
                tag.put("Fluid", fluid.save(registries));
            }
        }
        return tag;
    }
}
