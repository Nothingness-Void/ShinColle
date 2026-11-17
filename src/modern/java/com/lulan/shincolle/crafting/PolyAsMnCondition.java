package com.lulan.shincolle.crafting;

import com.google.gson.JsonObject;
import com.lulan.shincolle.ShinColle;
import com.lulan.shincolle.config.ShinColleConfig;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.crafting.conditions.ICondition;
import net.minecraftforge.common.crafting.conditions.IConditionSerializer;

/**
 * 1.12.2 ConfigHandler.polyAsMn recipe condition parity (`shincolle:poly_as_mn`).
 */
public final class PolyAsMnCondition implements ICondition {

    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(ShinColle.MOD_ID, "poly_as_mn");
    public static final PolyAsMnCondition INSTANCE = new PolyAsMnCondition();

    private PolyAsMnCondition() {
    }

    @Override
    public ResourceLocation getID() {
        return ID;
    }

    @Override
    public boolean test(IContext context) {
        return ShinColleConfig.polyAsMn();
    }

    public enum Serializer implements IConditionSerializer<PolyAsMnCondition> {
        INSTANCE;

        @Override
        public void write(JsonObject json, PolyAsMnCondition value) {
        }

        @Override
        public PolyAsMnCondition read(JsonObject json) {
            return PolyAsMnCondition.INSTANCE;
        }

        @Override
        public ResourceLocation getID() {
            return ID;
        }
    }
}
