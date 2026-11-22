package com.lulan.shincolle.registry;

import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.capabilities.BlockCapability;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.ItemCapability;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import com.lulan.shincolle.item.ShipTankItem;

import java.util.Optional;

public final class ModCapabilities {

    private ModCapabilities() {
    }

    public static <T> Optional<T> get(BlockEntity blockEntity, BlockCapability<T, Direction> capability) {
        return get(blockEntity, capability, null);
    }

    public static <T> Optional<T> get(BlockEntity blockEntity, BlockCapability<T, Direction> capability, Direction side) {
        return blockEntity.getLevel() == null ? Optional.empty() : Optional.ofNullable(
                blockEntity.getLevel().getCapability(capability, blockEntity.getBlockPos(), side));
    }

    public static <T> Optional<T> get(ItemStack stack, ItemCapability<T, Void> capability) {
        return Optional.ofNullable(stack.getCapability(capability));
    }

    public static void register(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, ModBlockEntities.VOL_CORE.get(),
                (tile, side) -> tile.getCapability(Capabilities.ItemHandler.BLOCK, side).orElse(null));
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, ModBlockEntities.SMALL_SHIPYARD.get(),
                (tile, side) -> tile.getCapability(Capabilities.ItemHandler.BLOCK, side).orElse(null));
        event.registerBlockEntity(Capabilities.FluidHandler.BLOCK, ModBlockEntities.SMALL_SHIPYARD.get(),
                (tile, side) -> tile.getCapability(Capabilities.FluidHandler.BLOCK, side).orElse(null));
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, ModBlockEntities.GRUDGE_HEAVY.get(),
                (tile, side) -> tile.getCapability(Capabilities.ItemHandler.BLOCK, side).orElse(null));
        event.registerBlockEntity(Capabilities.FluidHandler.BLOCK, ModBlockEntities.GRUDGE_HEAVY.get(),
                (tile, side) -> tile.getCapability(Capabilities.FluidHandler.BLOCK, side).orElse(null));
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, ModBlockEntities.POLYMETAL.get(),
                (tile, side) -> tile.getCapability(Capabilities.ItemHandler.BLOCK, side).orElse(null));
        event.registerBlockEntity(Capabilities.FluidHandler.BLOCK, ModBlockEntities.POLYMETAL.get(),
                (tile, side) -> tile.getCapability(Capabilities.FluidHandler.BLOCK, side).orElse(null));
        event.registerBlockEntity(Capabilities.FluidHandler.BLOCK, ModBlockEntities.CRANE.get(),
                (tile, side) -> tile.getCapability(Capabilities.FluidHandler.BLOCK, side).orElse(null));
        event.registerItem(Capabilities.FluidHandler.ITEM,
                (stack, context) -> ((ShipTankItem) stack.getItem()).createFluidHandler(stack),
                ModItems.SHIPTANK.get(), ModItems.SHIPTANK1.get(), ModItems.SHIPTANK2.get(), ModItems.SHIPTANK3.get());
    }
}
