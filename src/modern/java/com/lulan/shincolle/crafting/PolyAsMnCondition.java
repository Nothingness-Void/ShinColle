package com.lulan.shincolle.crafting;

import com.mojang.serialization.MapCodec;
import com.lulan.shincolle.ShinColle;
import com.lulan.shincolle.config.ShinColleConfig;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.common.conditions.ICondition;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

/**
 * 1.12.2 ConfigHandler.polyAsMn recipe condition parity (`shincolle:poly_as_mn`).
 */
public final class PolyAsMnCondition implements ICondition {

    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(ShinColle.MOD_ID, "poly_as_mn");
    public static final PolyAsMnCondition INSTANCE = new PolyAsMnCondition();
    public static final MapCodec<PolyAsMnCondition> CODEC = MapCodec.unit(INSTANCE);
    public static final DeferredRegister<MapCodec<? extends ICondition>> SERIALIZERS =
            DeferredRegister.create(NeoForgeRegistries.Keys.CONDITION_CODECS, ShinColle.MOD_ID);

    static {
        SERIALIZERS.register("poly_as_mn", () -> CODEC);
    }

    private PolyAsMnCondition() {
    }

    @Override
    public MapCodec<PolyAsMnCondition> codec() {
        return CODEC;
    }

    @Override
    public boolean test(IContext context) {
        return ShinColleConfig.polyAsMn();
    }

}
