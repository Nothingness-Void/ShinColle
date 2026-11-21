package com.lulan.shincolle.blockentity;

import com.lulan.shincolle.registry.ModCapabilities;
import com.lulan.shincolle.entity.ship.LegacyShipEntity;
import com.lulan.shincolle.entity.ship.ShipArchetype;
import com.lulan.shincolle.item.equipment.LegacyEquipmentItem;
import com.lulan.shincolle.item.equipment.LegacyEquipmentFamily;
import com.lulan.shincolle.network.CombatFxDispatcher;
import com.lulan.shincolle.network.GameplayParticleType;
import com.lulan.shincolle.registry.ModSoundEvents;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidUtil;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import net.neoforged.neoforge.items.wrapper.InvWrapper;
import net.neoforged.neoforge.items.wrapper.RangedWrapper;
import com.lulan.shincolle.config.ShinColleConfig;
import net.minecraft.core.Direction;
import net.neoforged.neoforge.capabilities.BlockCapability;
import java.util.Optional;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import com.lulan.shincolle.crafting.LegacyItemMatchHelper;
import com.lulan.shincolle.registry.ModBlockEntities;
import com.lulan.shincolle.ownership.PlayerOwnerData;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.ItemStackHandler;

import javax.annotation.Nullable;
import java.util.Locale;

public class CraneBlockEntity extends BlockEntity implements RouteNode {

    private static final String OWNER_NAME_TAG = "OwnerName";
    private static final String OWNER_UUID_TAG = "OwnerUuid";
    private static final String OWNER_UID_TAG = "OwnerUID";
    private static final String LAST_WAYPOINT_TAG = "LastWaypoint";
    private static final String NEXT_WAYPOINT_TAG = "NextWaypoint";
    private static final String PAIRED_CHEST_TAG = "PairedChest";
    private static final String LOAD_ENABLED_TAG = "LoadEnabled";
    private static final String UNLOAD_ENABLED_TAG = "UnloadEnabled";
    private static final String WAIT_MODE_TAG = "WaitMode";
    private static final String REDSTONE_MODE_TAG = "RedstoneMode";
    private static final String LIQUID_MODE_TAG = "LiquidMode";
    private static final String ENERGY_MODE_TAG = "EnergyMode";
    private static final String FILTER_ITEMS_TAG = "FilterItems";
    private static final String FILTER_MODE_MASK_TAG = "FilterModeMask";
    private static final String CHECK_METADATA_TAG = "meta";
    private static final String CHECK_OREDICT_TAG = "dict";
    private static final String CHECK_NBT_TAG = "nbt";
    private static final int MAX_WAIT_MODE = 24;
    private static final int MAX_THREE_STATE_MODE = 2;
    public static final int FILTER_SLOT_COUNT = 18;

    private final FluidTank tank = new FluidTank((int) ShinColleConfig.tileCrane()[0]) {
        @Override
        public int getCapacity() { return (int) ShinColleConfig.tileCrane()[0]; }
        @Override
        protected void onContentsChanged() { CraneBlockEntity.this.setChanged(); }
    };



    public <T> Optional<T> getCapability(BlockCapability<T, net.minecraft.core.Direction> capability) {
        return getCapability(capability, null);
    }

    public <T> Optional<T> getCapability(BlockCapability<T, net.minecraft.core.Direction> capability, @Nullable Direction side) {
        if (capability == Capabilities.FluidHandler.BLOCK && side != Direction.DOWN) return Optional.of(capability.typeClass().cast(this.tank));
        return Optional.empty();
    }

    @Nullable
    private PlayerOwnerData owner;
    @Nullable
    private BlockPos lastWaypoint;
    @Nullable
    private BlockPos nextWaypoint;
    @Nullable
    private BlockPos pairedChest;
    private boolean loadEnabled = true;
    private boolean unloadEnabled = true;
    private boolean active = true;
    private boolean checkMetadata;
    private boolean checkOredict;
    private boolean checkNbt;
    private int waitMode;
    private int redstoneMode;
    private int liquidMode;
    private int energyMode;
    private int filterModeMask;
    private int redstoneTicks;
    @Nullable
    private LegacyShipEntity ship;
    private final ItemStackHandler filterItems = new ItemStackHandler(FILTER_SLOT_COUNT) {
        @Override
        protected void onContentsChanged(int slot) {
            CraneBlockEntity.this.markUpdated();
        }
    };

    public CraneBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.CRANE.get(), pos, state);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, net.minecraft.core.HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        com.lulan.shincolle.item.LegacyItemData.upgradeNested(registries, tag);
        this.tank.readFromNBT(registries, com.lulan.shincolle.item.LegacyFluidData.upgradeTank(registries, tag));
        this.owner = PlayerOwnerData.load(tag, OWNER_UUID_TAG, OWNER_NAME_TAG, OWNER_UID_TAG);
        this.lastWaypoint = readOptionalPos(tag, LAST_WAYPOINT_TAG);
        this.nextWaypoint = readOptionalPos(tag, NEXT_WAYPOINT_TAG);
        this.pairedChest = readOptionalPos(tag, PAIRED_CHEST_TAG);
        this.loadEnabled = !tag.contains(LOAD_ENABLED_TAG) || tag.getBoolean(LOAD_ENABLED_TAG);
        this.unloadEnabled = !tag.contains(UNLOAD_ENABLED_TAG) || tag.getBoolean(UNLOAD_ENABLED_TAG);
        this.active = !tag.contains("active") || tag.getBoolean("active");
        this.waitMode = Mth.clamp(tag.getInt(WAIT_MODE_TAG), 0, MAX_WAIT_MODE);
        this.redstoneMode = Mth.clamp(tag.getInt(REDSTONE_MODE_TAG), 0, MAX_THREE_STATE_MODE);
        this.liquidMode = Mth.clamp(tag.getInt(LIQUID_MODE_TAG), 0, MAX_THREE_STATE_MODE);
        this.energyMode = Mth.clamp(tag.getInt(ENERGY_MODE_TAG), 0, MAX_THREE_STATE_MODE);
        if (tag.contains(FILTER_ITEMS_TAG, Tag.TAG_COMPOUND)) {
            this.filterItems.deserializeNBT(registries, tag.getCompound(FILTER_ITEMS_TAG));
        }
        this.filterModeMask = tag.getInt(FILTER_MODE_MASK_TAG);
        this.checkMetadata = tag.getBoolean(CHECK_METADATA_TAG);
        this.checkOredict = tag.getBoolean(CHECK_OREDICT_TAG);
        this.checkNbt = tag.getBoolean(CHECK_NBT_TAG);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, net.minecraft.core.HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        this.tank.writeToNBT(registries, tag);

        if (this.owner != null) {
            this.owner.save(tag, OWNER_UUID_TAG, OWNER_NAME_TAG, OWNER_UID_TAG);
        }

        writeOptionalPos(tag, LAST_WAYPOINT_TAG, this.lastWaypoint);
        writeOptionalPos(tag, NEXT_WAYPOINT_TAG, this.nextWaypoint);
        writeOptionalPos(tag, PAIRED_CHEST_TAG, this.pairedChest);
        tag.putBoolean(LOAD_ENABLED_TAG, this.loadEnabled);
        tag.putBoolean(UNLOAD_ENABLED_TAG, this.unloadEnabled);
        tag.putBoolean("active", this.active);
        tag.putInt(WAIT_MODE_TAG, this.waitMode);
        tag.putInt(REDSTONE_MODE_TAG, this.redstoneMode);
        tag.putInt(LIQUID_MODE_TAG, this.liquidMode);
        tag.putInt(ENERGY_MODE_TAG, this.energyMode);
        tag.put(FILTER_ITEMS_TAG, this.filterItems.serializeNBT(registries));
        tag.putInt(FILTER_MODE_MASK_TAG, this.filterModeMask);
        tag.putBoolean(CHECK_METADATA_TAG, this.checkMetadata);
        tag.putBoolean(CHECK_OREDICT_TAG, this.checkOredict);
        tag.putBoolean(CHECK_NBT_TAG, this.checkNbt);
    }

    @Override
    public CompoundTag getUpdateTag(net.minecraft.core.HolderLookup.Provider registries) {
        return this.saveWithoutMetadata(this.level.registryAccess());
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    public void setOwner(Player player) {
        this.owner = PlayerOwnerData.of(player);
        this.markUpdated();
    }

    public boolean canDestroy(Player player) {
        return player.isCreative() || player.hasPermissions(2)
                || (this.owner != null && (this.owner.uuid() != null ? this.owner.uuid().equals(player.getUUID())
                : this.owner.uid() == com.lulan.shincolle.teitoku.TeitokuHelper.getPlayerUid(player)));
    }

    @Override
    public boolean canEdit(Player player) {
        return this.owner == null || this.owner.canEdit(player);
    }

    public Component getOwnerLabel() {
        return this.owner == null
                ? Component.translatable("gui.shincolle.waypoint.owner.unassigned")
                : this.owner.displayLabel();
    }

    public int getOwnerUid() {
        return this.owner == null ? 0 : this.owner.uid();
    }

    @Override
    public Component getRouteNodeName() {
        return Component.translatable("block.shincolle.blockcrane");
    }

    @Override
    public void setLastWaypoint(@Nullable BlockPos pos) {
        this.lastWaypoint = pos;
        this.markUpdated();
    }

    @Override
    public @Nullable BlockPos getLastWaypoint() {
        return this.lastWaypoint;
    }

    @Override
    public void setNextWaypoint(@Nullable BlockPos pos) {
        this.nextWaypoint = pos;
        this.markUpdated();
    }

    @Override
    public @Nullable BlockPos getNextWaypoint() {
        return this.nextWaypoint;
    }

    @Override
    public void setPairedChest(@Nullable BlockPos pos) {
        this.pairedChest = pos;
        this.markUpdated();
    }

    @Override
    public @Nullable BlockPos getPairedChest() {
        return this.pairedChest;
    }

    public boolean isLoadEnabled() {
        return this.loadEnabled;
    }

    public boolean isUnloadEnabled() {
        return this.unloadEnabled;
    }

    public int getWaitMode() {
        return this.waitMode;
    }

    public int getRedstoneMode() {
        return this.redstoneMode;
    }

    public int getLiquidMode() {
        return this.liquidMode;
    }

    public int getEnergyMode() {
        return this.energyMode;
    }

    public void toggleLoadEnabled() {
        this.loadEnabled = !this.loadEnabled;
        this.markUpdated();
    }

    public void toggleUnloadEnabled() {
        this.unloadEnabled = !this.unloadEnabled;
        this.markUpdated();
    }

    public boolean isCheckMetadata() {
        return this.checkMetadata;
    }

    public void setCheckMetadata(boolean checkMetadata) {
        this.checkMetadata = checkMetadata;
        this.markUpdated();
    }

    public void toggleCheckMetadata() {
        this.checkMetadata = !this.checkMetadata;
        this.markUpdated();
    }

    public boolean isCheckOredict() {
        return this.checkOredict;
    }

    public void setCheckOredict(boolean checkOredict) {
        this.checkOredict = checkOredict;
        this.markUpdated();
    }

    public void toggleCheckOredict() {
        this.checkOredict = !this.checkOredict;
        this.markUpdated();
    }

    public boolean isCheckNbt() {
        return this.checkNbt;
    }

    public void setCheckNbt(boolean checkNbt) {
        this.checkNbt = checkNbt;
        this.markUpdated();
    }

    public void toggleCheckNbt() {
        this.checkNbt = !this.checkNbt;
        this.markUpdated();
    }

    public Component getCheckMetadataStateLabel() {
        return enabledLabel(this.checkMetadata);
    }

    public Component getCheckOredictStateLabel() {
        return enabledLabel(this.checkOredict);
    }

    public Component getCheckNbtStateLabel() {
        return enabledLabel(this.checkNbt);
    }

    public boolean isActive() {
        return this.active;
    }

    public boolean isCraneEnabled() {
        return this.active;
    }

    public void toggleActive() {
        this.active = !this.active;
        this.markUpdated();
    }

    public int getRedstoneTicks() {
        return this.redstoneTicks;
    }

    public void emitRedstoneSignal(int ticks) {
        if (ticks <= 0) {
            return;
        }
        this.redstoneTicks = ticks;
        if (this.level != null) {
            this.level.updateNeighborsAt(this.worldPosition, this.getBlockState().getBlock());
        }
    }

    public static void serverTick(net.minecraft.world.level.Level level, BlockPos pos, BlockState state, CraneBlockEntity crane) {
        crane.getShip();
        if (crane.redstoneTicks > 0) {
            crane.redstoneTicks--;
            if (crane.redstoneTicks <= 0) level.updateNeighborsAt(pos, state.getBlock());
        }
        crane.tickWork();
    }


    private int workTicks;
    private boolean hadPairedInventory;

    private void tickWork() {
        this.workTicks++;
        if (!this.active || this.pairedChest == null || this.workTicks <= 64 || (this.workTicks & 15) != 0) return;
        IItemHandler chest = this.getPairedInventory();
        if (chest == null && this.hadPairedInventory) {
            this.setPairedChest(null);
            this.hadPairedInventory = false;
            return;
        }
        this.hadPairedInventory = chest != null;
        LegacyShipEntity ship = this.findCraningShip();
        if (ship != null && this.redstoneMode == 1) this.emitRedstoneSignal(18);
        if (chest != null) this.applyPreLiquidTransfer(chest);
        if (chest == null || ship == null || ship.getCraneState() != 2) return;

        IItemHandler cargo = new RangedWrapper(new InvWrapper(ship.getShipInventory()),
                LegacyShipEntity.EQUIPMENT_SLOT_COUNT, ship.getInventorySizePaged());
        boolean[] work = new boolean[4];
        if (this.loadEnabled) work[0] = this.applyItemTransfer(chest, cargo, true);
        if (this.unloadEnabled) work[1] = this.applyItemTransfer(cargo, chest, false);
        int rate = this.liquidTransferRate(ship);
        if (this.liquidMode != 0 && rate > 0) work[2] = this.applyLiquidTransfer(cargo, rate);
        // Legacy EU work requires IC2; no IC2 integration is present in this port.
        if (ship.getSpec().archetype() == ShipArchetype.TRANSPORT) {
            for (boolean ignored : work) ship.addShipExperience(ShinColleConfig.craneWorkExperience());
        }
        CombatFxDispatcher.sendParticle(ship, GameplayParticleType.CRANING_SPARKLE,
                ship.getX(), ship.getY() + ship.getBbHeight() * 0.4D, ship.getZ(), 0D, 0.1D, 0D);
        if (this.checkCraneEnding(ship, chest, cargo, work)) {
            if (this.redstoneMode == 2) this.emitRedstoneSignal(2);
            ship.finishCraning(this);
            this.ship = null;
            ship.playSound(ModSoundEvents.SHIP_BELL.get(), ShinColleConfig.volumeShip() * 1.5F,
                    ship.getRandom().nextFloat() * 0.3F + 1F);
        }
    }

    private @Nullable IItemHandler getPairedInventory() {
        if (this.level == null || this.pairedChest == null || !this.level.hasChunkAt(this.pairedChest)) return null;
        BlockEntity tile = this.level.getBlockEntity(this.pairedChest);
        if (tile == null) return null;
        if (tile instanceof net.minecraft.world.level.block.entity.ChestBlockEntity
                && tile.getBlockState().getBlock() instanceof net.minecraft.world.level.block.ChestBlock block) {
            net.minecraft.world.Container inventory = net.minecraft.world.level.block.ChestBlock.getContainer(
                    block, tile.getBlockState(), this.level, this.pairedChest, true);
            return inventory == null ? null : new InvWrapper(inventory);
        }
        IItemHandler handler = ModCapabilities.get(tile, Capabilities.ItemHandler.BLOCK).orElse(null);
        if (handler != null) return handler;
        return tile instanceof net.minecraft.world.Container inventory ? new InvWrapper(inventory) : null;
    }

    private @Nullable LegacyShipEntity findCraningShip() {
        var ships = this.level.getEntitiesOfClass(LegacyShipEntity.class,
                new net.minecraft.world.phys.AABB(this.worldPosition.getX() - 6D, this.worldPosition.getY() - 12D,
                        this.worldPosition.getZ() - 6D, this.worldPosition.getX() + 6D,
                        this.worldPosition.getY(), this.worldPosition.getZ() + 6D),
                ship -> ship.isAlive() && ship.isDockedAtCrane(this.worldPosition));
        for (int state = 2; state >= 1; state--) {
            for (LegacyShipEntity ship : ships) {
                if (ship.getCraneState() == state) {
                    this.setShipData(ship);
                    return ship;
                }
            }
        }
        if (this.ship != null) this.clearShipData(this.ship);
        return null;
    }

    private int liquidTransferRate(LegacyShipEntity ship) {
        int pumps = ship.getSpec().archetype() == ShipArchetype.TRANSPORT && ship.isMarried() ? 1 : 0;
        int enchantments = 0;
        for (int slot = 0; slot < Math.min(6, ship.getShipInventory().getContainerSize()); slot++) {
            ItemStack stack = ship.getShipInventory().getItem(slot);
            if (stack.getItem() instanceof LegacyEquipmentItem equipment
                    && equipment.family() == LegacyEquipmentFamily.DRUM && equipment.variantIndex() == 1) {
                pumps++;
                for (var enchantment : stack.getEnchantments().entrySet()) {
                    enchantments += enchantment.getIntValue();
                }
            }
        }
        double[] rates = ShinColleConfig.drumLiquid();
        return ((int) rates[0] * pumps + (int) rates[1] * enchantments) * 16
                * ((int) (ship.getShipLevel() * 0.1F) + 1);
    }

    private boolean matches(ItemStack stack, ItemStack filter) {
        return LegacyItemMatchHelper.matchesTargetItem(stack, filter,
                this.checkMetadata, this.checkOredict, this.checkNbt);
    }

    private int countMatchingStacks(IItemHandler inventory, ItemStack filter) {
        int count = 0;
        for (int slot = 0; slot < inventory.getSlots(); slot++) {
            if (this.matches(inventory.getStackInSlot(slot), filter)) count++;
        }
        return count;
    }

    private boolean applyItemTransfer(IItemHandler source, IItemHandler target, boolean loading) {
        int start = loading ? 0 : 9;
        boolean hasPositive = false;
        for (int i = start; i < start + 9; i++) {
            ItemStack filter = this.getFilterStack(i);
            if (filter.isEmpty() || this.isFilterInverted(i)) continue;
            hasPositive = true;
            if (this.waitMode == 3 && this.countMatchingStacks(target, filter) >= filter.getCount()) continue;
            if (this.waitMode == 4 && this.countMatchingStacks(source, filter) <= filter.getCount()) continue;
            for (int slot = 0; slot < source.getSlots(); slot++) {
                if (this.matches(source.getStackInSlot(slot), filter)) {
                    if (moveStack(source, target, slot)) return true;
                    break;
                }
            }
        }
        if (!hasPositive) {
            for (int slot = 0; slot < source.getSlots(); slot++) {
                if (this.matchesTransferFilter(source.getStackInSlot(slot), loading)) {
                    return moveStack(source, target, slot);
                }
            }
        }
        return false;
    }

    private static boolean moveStack(IItemHandler source, IItemHandler target, int slot) {
        ItemStack available = source.extractItem(slot, source.getStackInSlot(slot).getCount(), true);
        ItemStack remaining = ItemHandlerHelper.insertItemStacked(target, available, true);
        int amount = available.getCount() - remaining.getCount();
        if (amount <= 0) return false;
        ItemStack extracted = source.extractItem(slot, amount, false);
        ItemStack rejected = ItemHandlerHelper.insertItemStacked(target, extracted, false);
        if (!rejected.isEmpty()) source.insertItem(slot, rejected, false);
        return extracted.getCount() > rejected.getCount();
    }

    private void applyPreLiquidTransfer(IItemHandler chest) {
        if (this.liquidMode == 0) return;
        for (int slot = 0; slot < chest.getSlots(); slot++) {
            if (this.transferFluidContainer(chest, slot, this.liquidMode == 1, this.liquidMode == 1 ? 64000 : 16000) > 0) break;
        }
    }

    private int transferFluidContainer(IItemHandler inventory, int slot, boolean toCrane, int limit) {
        ItemStack stack = inventory.getStackInSlot(slot);
        if (stack.isEmpty() || stack.getCount() != 1) return 0;
        var optional = FluidUtil.getFluidHandler(stack.copy());
        if (optional.isEmpty()) return 0;
        var container = optional.get();
        FluidTank preview = new FluidTank(this.tank.getCapacity());
        preview.setFluid(this.tank.getFluid().copy());
        FluidStack moved = toCrane
                ? FluidUtil.tryFluidTransfer(preview, container, limit, true)
                : FluidUtil.tryFluidTransfer(container, preview, limit, true);
        ItemStack result = container.getContainer();
        if (moved.isEmpty() || !inventory.isItemValid(slot, result)
                || inventory.extractItem(slot, 1, true).isEmpty()) return 0;
        ItemStack original = inventory.extractItem(slot, 1, false);
        if (original.isEmpty()) return 0;
        ItemStack rejected = inventory.insertItem(slot, result, false);
        if (!rejected.isEmpty()) {
            inventory.insertItem(slot, original, false);
            return 0;
        }
        if (toCrane) this.tank.fill(moved, IFluidHandler.FluidAction.EXECUTE);
        else this.tank.drain(moved, IFluidHandler.FluidAction.EXECUTE);
        return moved.getAmount();
    }

    private boolean applyLiquidTransfer(IItemHandler cargo, int rate) {
        int remaining = rate;
        for (int slot = 0; slot < cargo.getSlots() && remaining > 0; slot++) {
            remaining -= this.transferFluidContainer(cargo, slot, this.liquidMode == 2, remaining);
        }
        return remaining != rate;
    }

    private boolean checkCraneEnding(LegacyShipEntity ship, IItemHandler chest, IItemHandler cargo, boolean[] work) {
        return switch (this.waitMode) {
            case 0 -> !work[0] && !work[1] && !work[2] && !work[3];
            case 1 -> (!this.loadEnabled || inventoryFull(cargo)) && (!this.unloadEnabled || inventoryFull(chest))
                    && (this.liquidMode != 1 || this.fluidContainersDone(cargo, true))
                    && (this.liquidMode != 2 || this.fluidContainersDone(chest, true));
            case 2 -> (!this.loadEnabled || this.inventoryEmpty(chest, true))
                    && (!this.unloadEnabled || this.inventoryEmpty(cargo, false))
                    && (this.liquidMode != 1 || this.fluidContainersDone(chest, false))
                    && (this.liquidMode != 2 || this.fluidContainersDone(cargo, false));
            case 3 -> !work[2] && !work[3] && (!this.loadEnabled || this.inventoryAmount(cargo, true, true))
                    && (!this.unloadEnabled || this.inventoryAmount(chest, false, true));
            case 4 -> !work[2] && !work[3] && (!this.loadEnabled || this.inventoryAmount(chest, true, false))
                    && (!this.unloadEnabled || this.inventoryAmount(cargo, false, false));
            default -> ship.getCraneTime() >= getWaitTime(this.waitMode);
        };
    }

    private static boolean inventoryFull(IItemHandler inventory) {
        for (int slot = 0; slot < inventory.getSlots(); slot++) {
            if (inventory.getStackInSlot(slot).isEmpty()) return false;
        }
        return true;
    }

    private boolean inventoryEmpty(IItemHandler inventory, boolean loading) {
        int start = loading ? 0 : 9;
        boolean hasFilter = false;
        for (int i = start; i < start + 9; i++) {
            ItemStack filter = this.getFilterStack(i);
            if (filter.isEmpty()) continue;
            hasFilter = true;
            if (!this.isFilterInverted(i) && this.countMatchingStacks(inventory, filter) > 0) return false;
        }
        if (hasFilter) return true;
        for (int slot = 0; slot < inventory.getSlots(); slot++) {
            if (!inventory.getStackInSlot(slot).isEmpty()) return false;
        }
        return true;
    }

    private boolean inventoryAmount(IItemHandler inventory, boolean loading, boolean excess) {
        int start = loading ? 0 : 9;
        for (int i = start; i < start + 9; i++) {
            ItemStack filter = this.getFilterStack(i);
            if (filter.isEmpty() || this.isFilterInverted(i)) continue;
            int stacks = this.countMatchingStacks(inventory, filter);
            if (excess ? stacks < filter.getCount() : stacks > filter.getCount()) return false;
        }
        return true;
    }

    private boolean fluidContainersDone(IItemHandler inventory, boolean full) {
        if (this.tank.isEmpty()) return true;
        for (int slot = 0; slot < inventory.getSlots(); slot++) {
            var handler = FluidUtil.getFluidHandler(inventory.getStackInSlot(slot));
            if (handler.isEmpty()) continue;
            for (int i = 0; i < handler.get().getTanks(); i++) {
                FluidStack fluid = handler.get().getFluidInTank(i);
                if (full ? fluid.isEmpty() || (fluid.isFluidEqual(this.tank.getFluid())
                        && handler.get().fill(this.tank.getFluid(), IFluidHandler.FluidAction.SIMULATE) > 0)
                        : !handler.get().drain(Integer.MAX_VALUE, IFluidHandler.FluidAction.SIMULATE).isEmpty()) return false;
            }
        }
        return true;
    }

    public void cycleWaitMode() {
        this.cycleWaitMode(true);
    }

    public void cycleWaitMode(boolean forward) {
        this.waitMode = Mth.clamp(this.waitMode + (forward ? 1 : -1), 0, MAX_WAIT_MODE);
        this.markUpdated();
    }

    public @Nullable LegacyShipEntity getShip() {
        if (this.ship != null && (!this.ship.isAlive() || this.ship.isRemoved()
                || !this.ship.isDockedAtCrane(this.worldPosition) || this.isRemoved())) {
            this.clearShipData(this.ship);
        }
        return this.ship;
    }

    public void setShipData(LegacyShipEntity ship) {
        this.ship = ship;
        ship.beginCraning(this);
    }

    public void clearShipData(LegacyShipEntity ship) {
        if (this.ship == ship) {
            this.ship = null;
            ship.finishCraning();
        }
    }

    public void cycleRedstoneMode() {
        this.redstoneMode = (this.redstoneMode + 1) % (MAX_THREE_STATE_MODE + 1);
        this.markUpdated();
    }

    public void cycleLiquidMode() {
        this.liquidMode = (this.liquidMode + 1) % (MAX_THREE_STATE_MODE + 1);
        this.markUpdated();
    }

    public void cycleEnergyMode() {
        this.energyMode = (this.energyMode + 1) % (MAX_THREE_STATE_MODE + 1);
        this.markUpdated();
    }

    public Component getWaitModeLabel() {
        return getWaitModeLabel(this.waitMode);
    }

    public Component getRedstoneModeLabel() {
        return switch (this.redstoneMode) {
            case 1 -> Component.translatable("gui.shincolle.crane.red1");
            case 2 -> Component.translatable("gui.shincolle.crane.red2");
            default -> Component.translatable("gui.shincolle.crane.red0");
        };
    }

    public Component getLiquidModeLabel() {
        return switch (this.liquidMode) {
            case 1 -> Component.translatable("gui.shincolle.crane.liquid1");
            case 2 -> Component.translatable("gui.shincolle.crane.liquid2");
            default -> Component.translatable("gui.shincolle.crane.liquid0");
        };
    }

    public Component getEnergyModeLabel() {
        return switch (this.energyMode) {
            case 1 -> Component.translatable("gui.shincolle.crane.energy1");
            case 2 -> Component.translatable("gui.shincolle.crane.energy2");
            default -> Component.translatable("gui.shincolle.crane.energy0");
        };
    }

    public Component getLoadStateLabel() {
        return enabledLabel(this.loadEnabled);
    }

    public Component getUnloadStateLabel() {
        return enabledLabel(this.unloadEnabled);
    }

    public ItemStackHandler getFilterItems() {
        return this.filterItems;
    }

    public ItemStack getFilterStack(int slot) {
        return this.filterItems.getStackInSlot(slot);
    }

    public boolean isFilterInverted(int slot) {
        return ((this.filterModeMask >> slot) & 1) == 1;
    }

    public boolean matchesTransferFilter(ItemStack candidate, boolean loading) {
        if (candidate.isEmpty()) return false;
        int start = loading ? 0 : 9;
        boolean hasPositive = false;
        boolean matchesPositive = false;
        boolean excluded = false;
        for (int slot = start; slot < start + 9; slot++) {
            ItemStack filter = this.getFilterStack(slot);
            if (filter.isEmpty()) continue;
            boolean matches = this.matches(candidate, filter);
            if (this.isFilterInverted(slot)) {
                excluded |= matches;
            } else {
                hasPositive = true;
                matchesPositive |= matches;
            }
        }
        return hasPositive ? matchesPositive : !excluded;
    }

    public void setFilter(int slot, ItemStack stack, boolean inverted) {
        if (slot < 0 || slot >= FILTER_SLOT_COUNT) {
            return;
        }

        ItemStack copy = stack.copy();
        if (copy.isEmpty()) {
            this.clearFilter(slot);
            return;
        }

        copy.setCount(Math.min(copy.getCount(), copy.getMaxStackSize()));
        this.filterItems.setStackInSlot(slot, copy);
        this.setFilterInverted(slot, inverted);
    }

    public void clearFilter(int slot) {
        if (slot < 0 || slot >= FILTER_SLOT_COUNT) {
            return;
        }

        this.filterItems.setStackInSlot(slot, ItemStack.EMPTY);
        this.setFilterInverted(slot, false);
    }

    private void setFilterInverted(int slot, boolean inverted) {
        if (inverted) {
            this.filterModeMask |= (1 << slot);
        } else {
            this.filterModeMask &= ~(1 << slot);
        }

        this.markUpdated();
    }

    public static Component getWaitModeLabel(int waitMode) {
        return switch (waitMode) {
            case 1 -> Component.translatable("gui.shincolle.crane.untilfull");
            case 2 -> Component.translatable("gui.shincolle.crane.untilempty");
            case 3 -> Component.translatable("gui.shincolle.crane.excess");
            case 4 -> Component.translatable("gui.shincolle.crane.remain");
            case 5, 6, 7, 8, 9 -> Component.translatable("gui.shincolle.crane.waitsec",
                    String.format(Locale.ROOT, "%.1f", getWaitTime(waitMode) * 0.05F));
            case 10, 11, 12, 13, 14 -> Component.translatable("gui.shincolle.crane.waitsec",
                    Integer.toString(getWaitTime(waitMode) / 20));
            case 15, 16, 17, 18, 19, 20, 21, 22, 23, 24 -> Component.translatable("gui.shincolle.crane.waitmin",
                    Integer.toString(getWaitTime(waitMode) / 1200));
            default -> Component.translatable("gui.shincolle.crane.nowait");
        };
    }

    public static int getWaitTime(int mode) {
        return switch (mode) {
            case 5, 6, 7, 8, 9 -> (mode - 4) * 16;
            case 10, 11, 12, 13, 14 -> (mode - 9) * 100;
            case 15, 16, 17, 18, 19 -> (mode - 14) * 1200;
            case 20, 21, 22, 23, 24 -> (mode - 19) * 12000;
            default -> 0;
        };
    }

    private static Component enabledLabel(boolean enabled) {
        return Component.translatable(enabled ? "gui.shincolle.crane.state.enabled" : "gui.shincolle.crane.state.disabled");
    }

    private void markUpdated() {
        this.setChanged();

        if (this.level != null) {
            BlockState state = this.getBlockState();
            this.level.sendBlockUpdated(this.worldPosition, state, state, Block.UPDATE_ALL);
        }
    }

    private static @Nullable BlockPos readOptionalPos(CompoundTag tag, String key) {
        if (tag.contains(key, Tag.TAG_COMPOUND)) {
            CompoundTag pos = tag.getCompound(key);
            return new BlockPos(pos.getInt("X"), pos.getInt("Y"), pos.getInt("Z"));
        }
        return NbtUtils.readBlockPos(tag, key).orElse(null);
    }

    private static void writeOptionalPos(CompoundTag tag, String key, @Nullable BlockPos pos) {
        if (pos != null) {
            tag.put(key, NbtUtils.writeBlockPos(pos));
        }
    }
}
