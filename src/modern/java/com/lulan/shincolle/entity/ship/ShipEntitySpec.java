package com.lulan.shincolle.entity.ship;

import com.lulan.shincolle.ShinColle;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.Locale;

public record ShipEntitySpec(int eggMeta, String translationKey, String textureStem, ShipArchetype archetype, boolean hostile) {

    public int legacyClassId() {
        return this.eggMeta - 2;
    }

    public Component displayName() {
        return Component.translatable(this.translationKey);
    }

    public ResourceLocation textureLocation() {
        return ResourceLocation.fromNamespaceAndPath(ShinColle.MOD_ID,
                "textures/entity/modern/" + this.textureStem.toLowerCase(Locale.ROOT) + ".png");
    }

    public ResourceLocation modelSourceLocation() {
        return ResourceLocation.fromNamespaceAndPath(ShinColle.MOD_ID,
                "legacy_model_sources/" + this.modelSourceStem().toLowerCase(Locale.ROOT) + ".java");
    }

    public String modelSourceStem() {
        return switch (this.textureStem) {
            case "EntityHeavyCruiserRi" -> "ModelHeavyCruiserRi";
            case "EntityHeavyCruiserNe" -> "ModelHeavyCruiserNe";
            case "EntityCAHime" -> "ModelCAHime";
            case "EntitySubmHimeNew" -> "ModelSSNH";
            default -> "Model" + this.textureStem.substring("Entity".length());
        };
    }
}
