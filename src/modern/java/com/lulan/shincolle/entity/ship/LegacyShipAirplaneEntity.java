package com.lulan.shincolle.entity.ship;

import com.lulan.shincolle.ShinColle;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

public class LegacyShipAirplaneEntity extends LegacyShipAircraftEntity {

    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(ShinColle.MOD_ID, "textures/entity/entityairplanezero.png");

    public LegacyShipAirplaneEntity(EntityType<? extends LegacyShipAirplaneEntity> entityType, Level level) {
        super(entityType, level);
    }

    @Override
    protected ResourceLocation getTextureLocation() {
        return TEXTURE;
    }
}
