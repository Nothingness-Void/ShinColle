package com.lulan.shincolle.blockentity;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

import javax.annotation.Nullable;

public interface RouteNode {

    boolean canEdit(Player player);

    void setLastWaypoint(@Nullable BlockPos pos);

    @Nullable
    BlockPos getLastWaypoint();

    void setNextWaypoint(@Nullable BlockPos pos);

    @Nullable
    BlockPos getNextWaypoint();

    void setPairedChest(@Nullable BlockPos pos);

    @Nullable
    BlockPos getPairedChest();

    Component getRouteNodeName();
}
