package com.lulan.shincolle.client.model.legacy.compat;

import com.lulan.shincolle.entity.mount.LegacyShipMountEntity;
import com.lulan.shincolle.entity.ship.LegacyShipEntity;
import net.minecraft.client.renderer.LevelRenderer;

public class Entity {
    public final net.minecraft.world.entity.Entity modern;
    public int ticksExisted;
    public double posX, posY, posZ;
    public Entity(net.minecraft.world.entity.Entity modern) {
        this.modern = modern;
        if (modern != null) {
            ticksExisted = modern.tickCount;
            posX = modern.getX(); posY = modern.getY(); posZ = modern.getZ();
        }
    }
    public static Entity wrap(net.minecraft.world.entity.Entity entity) {
        if (entity == null) return null;
        if (entity instanceof LegacyShipEntity ship) {
            if (ship.isHostileVariant()) return new BasicEntityShip(ship);
            return switch (ship.getSpec().textureStem()) {
                case "EntityDestroyerAkatsuki" -> new EntityDestroyerAkatsuki(ship);
                case "EntityDestroyerInazuma" -> new EntityDestroyerInazuma(ship);
                default -> new BasicEntityShip(ship);
            };
        }
        if (entity instanceof LegacyShipMountEntity) return new BasicEntityMount(entity);
        if (entity instanceof net.minecraft.world.entity.player.Player) return new EntityPlayer(entity);
        return new Entity(entity);
    }
    public Entity getRidingEntity() { return modern == null ? null : wrap(modern.getVehicle()); }
    public boolean isRiding() { return modern != null && modern.isPassenger(); }
    public boolean isSneaking() { return modern != null && modern.isShiftKeyDown(); }
    public boolean isSprinting() { return modern != null && modern.isSprinting(); }
    public int getBrightnessForRender() { return LevelRenderer.getLightColor(modern.level(), modern.blockPosition()); }
    public int getBrightnessForRender(float partialTick) { return getBrightnessForRender(); }
}
