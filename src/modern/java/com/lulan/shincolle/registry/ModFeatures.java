package com.lulan.shincolle.registry;

import com.lulan.shincolle.ShinColle;
import com.lulan.shincolle.world.PolymetalGravelFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModFeatures {

    public static final DeferredRegister<Feature<?>> FEATURES = DeferredRegister.create(ForgeRegistries.FEATURES, ShinColle.MOD_ID);

    public static final RegistryObject<Feature<NoneFeatureConfiguration>> POLYMETAL_GRAVEL = FEATURES.register("polymetal_gravel",
            () -> new PolymetalGravelFeature(NoneFeatureConfiguration.CODEC));

    private ModFeatures() {
    }
}
