package com.lulan.shincolle.registry;

import com.lulan.shincolle.ShinColle;
import com.lulan.shincolle.world.PolymetalGravelFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.neoforged.neoforge.registries.DeferredHolder;

public final class ModFeatures {

    public static final DeferredRegister<Feature<?>> FEATURES = DeferredRegister.create(BuiltInRegistries.FEATURE, ShinColle.MOD_ID);

    public static final DeferredHolder<Feature<?>, Feature<NoneFeatureConfiguration>> POLYMETAL_GRAVEL = FEATURES.register("polymetal_gravel",
            () -> new PolymetalGravelFeature(NoneFeatureConfiguration.CODEC));

    private ModFeatures() {
    }
}
