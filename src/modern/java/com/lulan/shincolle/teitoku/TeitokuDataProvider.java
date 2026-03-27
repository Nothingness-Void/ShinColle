package com.lulan.shincolle.teitoku;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;

import javax.annotation.Nullable;

public class TeitokuDataProvider implements ICapabilityProvider, INBTSerializable<CompoundTag> {

    private final TeitokuData teitokuData = new TeitokuData();
    private final LazyOptional<TeitokuData> optional = LazyOptional.of(() -> this.teitokuData);

    @Override
    public <T> LazyOptional<T> getCapability(net.minecraftforge.common.capabilities.Capability<T> capability,
                                             @Nullable Direction side) {
        return capability == TeitokuHelper.TEITOKU_CAPABILITY ? this.optional.cast() : LazyOptional.empty();
    }

    @Override
    public CompoundTag serializeNBT() {
        return this.teitokuData.saveToTag(new CompoundTag());
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        this.teitokuData.loadFromTag(nbt);
    }
}
