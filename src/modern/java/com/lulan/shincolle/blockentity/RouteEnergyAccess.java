package com.lulan.shincolle.blockentity;

public interface RouteEnergyAccess {

    int getRouteEnergyStored();

    int getRouteEnergyCapacity();

    int extractRouteEnergy(int amount, boolean simulate);

    int receiveRouteEnergy(int amount, boolean simulate);

    default boolean hasRouteEnergy() {
        return this.getRouteEnergyStored() > 0;
    }

    default boolean canReceiveRouteEnergy() {
        return this.getRouteEnergyStored() < this.getRouteEnergyCapacity();
    }
}
