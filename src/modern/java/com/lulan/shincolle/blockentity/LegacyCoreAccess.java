package com.lulan.shincolle.blockentity;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.ItemStackHandler;

public interface LegacyCoreAccess extends MenuProvider {

    BlockPos getBlockPos();

    ItemStackHandler getFuelItems();

    ContainerData getCoreContainerData();

    boolean canEdit(Player player);

    Component getOwnerLabel();

    Component getBlockLabel();

    boolean isFuelItem(ItemStack stack);

    void cycleCoreMode();

    boolean isVolCore();
}
