package com.lulan.shincolle.teitoku;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import net.neoforged.neoforge.attachment.IAttachmentSerializer;

import javax.annotation.Nullable;

public class TeitokuDataProvider implements IAttachmentSerializer<CompoundTag, TeitokuData> {

    @Override
    public CompoundTag write(TeitokuData data, HolderLookup.Provider registries) {
        return data.withRegistries(registries).saveToTag(new CompoundTag());
    }

    @Override
    public TeitokuData read(IAttachmentHolder holder, CompoundTag nbt, HolderLookup.Provider registries) {
        TeitokuData data = new TeitokuData().withRegistries(registries);
        data.loadFromTag(nbt);
        return data;
    }
}
