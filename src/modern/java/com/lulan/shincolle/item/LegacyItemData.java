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
}
