package com.lulan.shincolle.blockentity;

import com.lulan.shincolle.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.BlockCapability;
import java.util.Optional;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.items.IItemHandler;

import javax.annotation.Nullable;

public class PolymetalServantBlockEntity extends BlockEntity {

    private static final String MASTER_POS_TAG = "MasterPos";

    @Nullable
    private BlockPos masterPos;
    private int rescanTick;

    public PolymetalServantBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.POLYMETAL.get(), pos, state);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, net.minecraft.core.HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        com.lulan.shincolle.item.LegacyItemData.upgradeNested(registries, tag);
        this.masterPos = tag.contains(MASTER_POS_TAG) ? BlockPos.of(tag.getLong(MASTER_POS_TAG)) : null;
    }

    @Override
    protected void saveAdditional(CompoundTag tag, net.minecraft.core.HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        if (this.masterPos != null) {
            tag.putLong(MASTER_POS_TAG, this.masterPos.asLong());
        }
    }

    @Override
    public CompoundTag getUpdateTag(net.minecraft.core.HolderLookup.Provider registries) {
        return this.saveWithoutMetadata(this.level.registryAccess());
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    public <T> Optional<T> getCapability(BlockCapability<T, net.minecraft.core.Direction> capability) {
        return getCapability(capability, null);
    }

    public <T> Optional<T> getCapability(BlockCapability<T, net.minecraft.core.Direction> capability, @Nullable net.minecraft.core.Direction side) {
        if (capability == Capabilities.ItemHandler.BLOCK || capability == Capabilities.FluidHandler.BLOCK) {
            HeavyGrudgeBlockEntity master = this.getMaster();
            if (master == null || !master.isStructureComplete()) {
                return Optional.empty();
            }
            return master.getCapability(capability, side);
        }

        return Optional.empty();
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, PolymetalServantBlockEntity servant) {
        servant.tickServer();
    }

    private void tickServer() {
        this.rescanTick++;
        if (this.rescanTick < 20) {
            return;
        }

        this.rescanTick = 0;
        BlockPos resolved = this.resolveMasterPos();
        if (resolved == null ? this.masterPos != null : !resolved.equals(this.masterPos)) {
            this.masterPos = resolved;
            this.setChanged();
            if (this.level != null) {
                BlockState state = this.getBlockState();
                this.level.sendBlockUpdated(this.worldPosition, state, state, Block.UPDATE_ALL);
            }
        }
    }

    private @Nullable BlockPos resolveMasterPos() {
        if (this.level == null) {
            return null;
        }

        if (this.masterPos != null && this.level.getBlockEntity(this.masterPos) instanceof HeavyGrudgeBlockEntity heavy
                && heavy.isStructureComplete()
                && LargeShipyardStructureHelper.isServantPosition(this.masterPos, this.worldPosition)) {
            return this.masterPos;
        }

        return LargeShipyardStructureHelper.findMasterForServant(this.level, this.worldPosition);
    }

    public void setMasterPos(@Nullable BlockPos masterPos) {
        this.masterPos = masterPos == null ? null : masterPos.immutable();
        this.setChanged();
        if (this.level != null) {
            BlockState state = this.getBlockState();
            this.level.sendBlockUpdated(this.worldPosition, state, state, Block.UPDATE_ALL);
        }
    }

    public @Nullable HeavyGrudgeBlockEntity getMaster() {
        if (this.level == null) {
            return null;
        }

        BlockPos resolved = this.resolveMasterPos();
        if (resolved == null) {
            this.masterPos = null;
            return null;
        }

        this.masterPos = resolved;
        return this.level.getBlockEntity(resolved) instanceof HeavyGrudgeBlockEntity heavy ? heavy : null;
    }


}
