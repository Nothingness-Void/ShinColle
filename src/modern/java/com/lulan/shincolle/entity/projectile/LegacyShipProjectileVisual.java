package com.lulan.shincolle.entity.projectile;

import com.lulan.shincolle.ShinColle;
import net.minecraft.resources.ResourceLocation;

public enum LegacyShipProjectileVisual {
    NONE("textures/entity/modelbasicentityitem.png", 0.35F),
    MISSILE("textures/entity/entityabyssmissile.png", 0.65F),
    AIRPLANE("textures/entity/entityairplanezero.png", 0.62F),
    BOMB("textures/entity/entityaircraft.png", 0.45F),
    TORPEDO("textures/entity/modelbasicentityitem.png", 0.5F);

    private final ResourceLocation textureLocation;
    private final float renderScale;

    LegacyShipProjectileVisual(String texturePath, float renderScale) {
        this.textureLocation = ResourceLocation.fromNamespaceAndPath(ShinColle.MOD_ID, texturePath);
        this.renderScale = renderScale;
    }

    public ResourceLocation textureLocation() {
        return this.textureLocation;
    }

    public float renderScale() {
        return this.renderScale;
    }

    public static LegacyShipProjectileVisual byOrdinal(int ordinal) {
        LegacyShipProjectileVisual[] values = values();
        if (ordinal < 0 || ordinal >= values.length) {
            return NONE;
        }
        return values[ordinal];
    }
}
