package com.lulan.shincolle.registry;

import com.lulan.shincolle.ShinColle;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.neoforged.neoforge.fluids.SimpleFluidContent;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public final class ModDataComponents {

    public static final DeferredRegister<DataComponentType<?>> COMPONENTS =
            DeferredRegister.create(Registries.DATA_COMPONENT_TYPE, ShinColle.MOD_ID);
    public static final Supplier<DataComponentType<SimpleFluidContent>> FLUID_CONTENTS = COMPONENTS.register("fluid_contents",
            () -> DataComponentType.<SimpleFluidContent>builder().persistent(SimpleFluidContent.CODEC)
                    .networkSynchronized(SimpleFluidContent.STREAM_CODEC).build());

    private ModDataComponents() {
    }
}
