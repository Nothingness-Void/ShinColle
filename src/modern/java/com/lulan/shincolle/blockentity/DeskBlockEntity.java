package com.lulan.shincolle.blockentity;

import com.lulan.shincolle.menu.LegacyDeskBookData;
import com.lulan.shincolle.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;

public class DeskBlockEntity extends BlockEntity {

    private static final String SELECTED_FUNCTION_TAG = "SelectedFunction";
    private static final String RADAR_ZOOM_TAG = "RadarZoom";
    private static final String BOOK_CHAPTER_TAG = "BookChapter";
    private static final String BOOK_PAGE_TAG = "BookPage";

    private int selectedFunction;
    private int radarZoomIndex;
    private int bookChapter;
    private int bookPage;

    public DeskBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.DESK.get(), pos, state);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, net.minecraft.core.HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        com.lulan.shincolle.item.LegacyItemData.upgradeNested(registries, tag);
        this.selectedFunction = clampFunction(tag.getInt(SELECTED_FUNCTION_TAG));
        this.radarZoomIndex = Mth.clamp(tag.getInt(RADAR_ZOOM_TAG), 0, 2);
        this.bookChapter = Mth.clamp(tag.getInt(BOOK_CHAPTER_TAG), 0, 6);
        this.bookPage = Mth.clamp(tag.getInt(BOOK_PAGE_TAG), 0, LegacyDeskBookData.PAGE_LIMIT[this.bookChapter]);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, net.minecraft.core.HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt(SELECTED_FUNCTION_TAG, this.selectedFunction);
        tag.putInt(RADAR_ZOOM_TAG, this.radarZoomIndex);
        tag.putInt(BOOK_CHAPTER_TAG, this.bookChapter);
        tag.putInt(BOOK_PAGE_TAG, this.bookPage);
    }

    @Override
    public CompoundTag getUpdateTag(net.minecraft.core.HolderLookup.Provider registries) {
        CompoundTag tag = super.getUpdateTag(registries);
        this.saveAdditional(tag, registries);
        return tag;
    }

    @Override
    @Nullable
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    public int getSelectedFunction() {
        return this.selectedFunction;
    }

    public int getRadarZoomIndex() {
        return this.radarZoomIndex;
    }

    public int getBookChapter() {
        return this.bookChapter;
    }

    public int getBookPage() {
        return this.bookPage;
    }

    public void syncFunc(int selectedFunction, int radarZoomIndex, int bookChapter, int bookPage) {
        this.selectedFunction = clampFunction(selectedFunction);
        this.radarZoomIndex = Mth.clamp(radarZoomIndex, 0, 2);
        this.bookChapter = Mth.clamp(bookChapter, 0, 6);
        this.bookPage = Mth.clamp(bookPage, 0, LegacyDeskBookData.PAGE_LIMIT[this.bookChapter]);
        this.markUpdated();
    }

    public Component getSelectedFunctionName() {
        return Component.translatable(this.selectedFunction == 2
                ? "item.shincolle.deskitembook"
                : "item.shincolle.deskitemradar");
    }

    private void markUpdated() {
        this.setChanged();
        if (this.level != null) {
            BlockState state = this.getBlockState();
            this.level.sendBlockUpdated(this.worldPosition, state, state, Block.UPDATE_ALL);
        }
    }

    private static int clampFunction(int function) {
        return Mth.clamp(function, 0, 4);
    }
}
