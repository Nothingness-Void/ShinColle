package com.lulan.shincolle.blockentity;

import com.lulan.shincolle.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nullable;

public class PolymetalServantBlockEntity extends BlockEntity implements RouteEnergyAccess {

    private static final String MASTER_POS_TAG = "MasterPos";

    @Nullable
    private BlockPos masterPos;
    private int rescanTick;

    public PolymetalServantBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.POLYMETAL.get(), pos, state);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        this.masterPos = tag.contains(MASTER_POS_TAG) ? BlockPos.of(tag.getLong(MASTER_POS_TAG)) : null;
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        if (this.masterPos != null) {
            tag.putLong(MASTER_POS_TAG, this.masterPos.asLong());
        }
    }

    @Override
    public CompoundTag getUpdateTag() {
        return this.saveWithoutMetadata();
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> capability, @Nullable net.minecraft.core.Direction side) {
        if (capability == ForgeCapabilities.ITEM_HANDLER || capability == ForgeCapabilities.FLUID_HANDLER) {
            HeavyGrudgeBlockEntity master = this.getMaster();
            if (master == null || !master.isStructureComplete()) {
                return LazyOptional.empty();
            }
            return master.getCapability(capability, side);
        }

        return super.getCapability(capability, side);
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

    @Override
    public int getRouteEnergyStored() {
        HeavyGrudgeBlockEntity master = this.getMaster();
        return master == null ? 0 : master.getRouteEnergyStored();
    }

    @Override
    public int getRouteEnergyCapacity() {
        HeavyGrudgeBlockEntity master = this.getMaster();
        return master == null ? 0 : master.getRouteEnergyCapacity();
    }

    @Override
    public int extractRouteEnergy(int amount, boolean simulate) {
        HeavyGrudgeBlockEntity master = this.getMaster();
        return master == null ? 0 : master.extractRouteEnergy(amount, simulate);
    }

    @Override
    public int receiveRouteEnergy(int amount, boolean simulate) {
        HeavyGrudgeBlockEntity master = this.getMaster();
        return master == null ? 0 : master.receiveRouteEnergy(amount, simulate);
    }
}
