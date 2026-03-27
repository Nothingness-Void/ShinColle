package com.lulan.shincolle.blockentity;

import com.lulan.shincolle.menu.DeskReferenceMenu;
import com.lulan.shincolle.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class DeskBlockEntity extends BlockEntity {

    private static final String SELECTED_FUNCTION_TAG = "SelectedFunction";

    private int selectedFunction = DeskReferenceMenu.RADAR_VARIANT;

    public DeskBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.DESK.get(), pos, state);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        this.selectedFunction = clampFunction(tag.getInt(SELECTED_FUNCTION_TAG));
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putInt(SELECTED_FUNCTION_TAG, this.selectedFunction);
    }

    public void cycleFunction() {
        this.selectedFunction = this.selectedFunction == DeskReferenceMenu.RADAR_VARIANT
                ? DeskReferenceMenu.BOOK_VARIANT
                : DeskReferenceMenu.RADAR_VARIANT;
        this.setChanged();
    }

    public int getSelectedFunction() {
        return this.selectedFunction;
    }

    public Component getSelectedFunctionName() {
        return Component.translatable(this.selectedFunction == DeskReferenceMenu.BOOK_VARIANT
                ? "item.shincolle.deskitembook"
                : "item.shincolle.deskitemradar");
    }

    private static int clampFunction(int function) {
        return function == DeskReferenceMenu.BOOK_VARIANT
                ? DeskReferenceMenu.BOOK_VARIANT
                : DeskReferenceMenu.RADAR_VARIANT;
    }
}
