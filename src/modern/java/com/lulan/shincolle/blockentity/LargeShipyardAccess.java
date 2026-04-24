package com.lulan.shincolle.blockentity;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerData;
import net.minecraftforge.items.ItemStackHandler;

public interface LargeShipyardAccess extends MenuProvider {

    BlockPos getBlockPos();

    ItemStackHandler getItems();

    ContainerData getContainerData();

    boolean canEdit(Player player);

    Component getOwnerLabel();

    int[] getMaterialAmounts();

    int[] getBuildMaterialAmountsView();

    int getInvMode();

    int getSelectMat();

    void cycleShipMode();

    void cycleEquipMode();

    void cycleInventoryMode();

    void selectMaterial(int material);

    void adjustBuildMaterial(int material, int action);

    default boolean usesGenericInventory() {
        return false;
    }
}
