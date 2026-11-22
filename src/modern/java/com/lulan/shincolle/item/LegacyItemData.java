package com.lulan.shincolle.item;

import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

import javax.annotation.Nullable;
import java.util.function.Consumer;

public final class LegacyItemData {

    private LegacyItemData() {
    }

    public static boolean hasTag(ItemStack stack) {
        return stack.has(DataComponents.CUSTOM_DATA);
    }

    @Nullable
    public static CompoundTag getTag(ItemStack stack) {
        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        return data == null ? null : data.copyTag();
    }

    public static CompoundTag copyTag(ItemStack stack) {
        return stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
    }

    public static void setTag(ItemStack stack, @Nullable CompoundTag tag) {
        if (tag == null) {
            stack.remove(DataComponents.CUSTOM_DATA);
        } else {
            CustomData.set(DataComponents.CUSTOM_DATA, stack, tag);
        }
    }

    public static void update(ItemStack stack, Consumer<CompoundTag> update) {
        CustomData.update(DataComponents.CUSTOM_DATA, stack, update);
    }

    public static ItemStack parse(net.minecraft.core.HolderLookup.Provider registries, CompoundTag saved) {
        CompoundTag upgraded = saved.copy();
        upgradeNested(registries, upgraded);
        return ItemStack.parseOptional(registries, upgraded);
    }

    public static void save(ItemStack stack, net.minecraft.core.HolderLookup.Provider registries, CompoundTag target) {
        if (!stack.isEmpty()) {
            target.merge((CompoundTag) stack.save(registries));
        }
    }

    public static void importFluid(ItemStack stack) {
        CompoundTag custom = copyTag(stack);
        if (!(stack.getItem() instanceof ShipTankItem) || !custom.contains("Fluid", 10)
                || stack.has(com.lulan.shincolle.registry.ModDataComponents.FLUID_CONTENTS)) {
            return;
        }
        CompoundTag fluid = custom.getCompound("Fluid");
        net.neoforged.neoforge.fluids.FluidStack contents = LegacyFluidData.read(fluid);
        if (!contents.isEmpty()) {
            stack.set(com.lulan.shincolle.registry.ModDataComponents.FLUID_CONTENTS,
                    net.neoforged.neoforge.fluids.SimpleFluidContent.copyOf(contents));
            custom.remove("Fluid");
            setTag(stack, custom);
        }
    }

    public static void upgradeNested(net.minecraft.core.HolderLookup.Provider registries, net.minecraft.nbt.Tag tag) {
        if (tag instanceof CompoundTag compound) {
            if (compound.contains("id", 8) && compound.contains("Count", 99)) {
                CompoundTag upgraded = (CompoundTag) net.minecraft.util.datafix.DataFixers.getDataFixer().update(
                        net.minecraft.util.datafix.fixes.References.ITEM_STACK,
                        new com.mojang.serialization.Dynamic<>(net.minecraft.nbt.NbtOps.INSTANCE, compound.copy()),
                        3465, net.minecraft.SharedConstants.getCurrentVersion().getDataVersion().getVersion()).getValue();
                compound.remove("Count");
                compound.remove("tag");
                compound.merge(upgraded);
            }
            for (String key : java.util.Set.copyOf(compound.getAllKeys())) {
                upgradeNested(registries, compound.get(key));
            }
            net.minecraft.resources.ResourceLocation id = net.minecraft.resources.ResourceLocation.tryParse(compound.getString("id"));
            if (id != null && net.minecraft.core.registries.BuiltInRegistries.ITEM.getOptional(id).orElse(null) instanceof ShipTankItem
                    && compound.contains("count", 99)) {
                ItemStack stack = ItemStack.parseOptional(registries, compound);
                if (stack.getItem() instanceof ShipTankItem) {
                    importFluid(stack);
                    compound.put("components", ((CompoundTag) stack.save(registries)).getCompound("components"));
                }
            }
        } else if (tag instanceof net.minecraft.nbt.ListTag list) {
            for (net.minecraft.nbt.Tag child : list) {
                upgradeNested(registries, child);
            }
        }
    }
}
