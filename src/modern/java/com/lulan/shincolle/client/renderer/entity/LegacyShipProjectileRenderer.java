package com.lulan.shincolle.client.renderer.entity;

import com.lulan.shincolle.entity.projectile.LegacyShipProjectileEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;

public class LegacyShipProjectileRenderer extends ThrownItemRenderer<LegacyShipProjectileEntity> {

    public LegacyShipProjectileRenderer(EntityRendererProvider.Context context) {
        super(context, 1.0F, true);
    }
}
