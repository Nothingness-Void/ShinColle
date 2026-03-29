package com.lulan.shincolle.blockentity;

import com.lulan.shincolle.block.LargeShipyardBlock;
import com.lulan.shincolle.crafting.LargeShipyardRecipes;
import com.lulan.shincolle.crafting.ShipyardBuildTypes;
import com.lulan.shincolle.menu.LargeShipyardMenu;
import com.lulan.shincolle.ownership.PlayerOwnerData;
import com.lulan.shincolle.registry.ModBlockEntities;
import com.lulan.shincolle.registry.ModBlocks;
import com.lulan.shincolle.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nullable;
import java.util.Arrays;

public class LargeShipyardBlockEntity extends BlockEntity implements LargeShipyardAccess, RouteEnergyAccess, MenuProvider {

    private static final String OWNER_NAME_TAG = "OwnerName";
    private static final String OWNER_UUID_TAG = "OwnerUuid";
    private static final String OWNER_UID_TAG = "OwnerUID";
    private static final String INVENTORY_TAG = "Inventory";
    private static final String POWER_CONSUMED_TAG = "ConsumedPower";
    private static final String POWER_REMAINED_TAG = "RemainedPower";
    private static final String POWER_GOAL_TAG = "GoalPower";
    private static final String BUILD_TYPE_TAG = "BuildType";
    private static final String BUILD_RECORD_TAG = "BuildRecord";
    private static final String STRUCTURE_READY_TAG = "StructureReady";
    private static final String CORE_LINKED_TAG = "CoreLinked";
    private static final String CORE_POS_TAG = "CorePos";

    public static final int POWER_MAX = 1382400;
    public static final int BUILD_SPEED = 48;
    public static final float FUEL_MAGNIFICATION = 1.0F;
    public static final int POWER_INSTANT = BUILD_SPEED * 1200;
    private static final int CORE_TRANSFER_PER_TICK = 96;

    @Nullable
    private PlayerOwnerData owner;
    private final ItemStackHandler items = new ItemStackHandler(LargeShipyardRecipes.SLOT_COUNT) {
        @Override
        protected void onContentsChanged(int slot) {
            LargeShipyardBlockEntity.this.setChanged();
        }
    };
    private final ContainerData dataAccess = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> LargeShipyardBlockEntity.this.buildType;
                case 1 -> LargeShipyardBlockEntity.this.powerConsumed;
                case 2 -> LargeShipyardBlockEntity.this.powerRemained;
                case 3 -> LargeShipyardBlockEntity.this.powerGoal;
                case 4 -> LargeShipyardBlockEntity.this.structureComplete ? 1 : 0;
                case 5 -> LargeShipyardBlockEntity.this.coreLinked ? 1 : 0;
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
            switch (index) {
                case 0 -> LargeShipyardBlockEntity.this.buildType = value;
                case 1 -> LargeShipyardBlockEntity.this.powerConsumed = value;
                case 2 -> LargeShipyardBlockEntity.this.powerRemained = value;
                case 3 -> LargeShipyardBlockEntity.this.powerGoal = value;
                case 4 -> LargeShipyardBlockEntity.this.structureComplete = value != 0;
                case 5 -> LargeShipyardBlockEntity.this.coreLinked = value != 0;
                default -> {
                }
            }
        }

        @Override
        public int getCount() {
            return 6;
        }
    };
    private LazyOptional<IItemHandler> itemHandler = LazyOptional.empty();
    private int powerConsumed;
    private int powerRemained;
    private int powerGoal;
    private int buildType;
    private final int[] buildRecord = new int[4];
    private boolean structureComplete;
    private boolean coreLinked;
    @Nullable
    private BlockPos linkedCorePos;
    private int structureProbeTick;

    public LargeShipyardBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.LARGE_SHIPYARD.get(), pos, state);
    }

    @Override
    public void onLoad() {
        super.onLoad();
        this.itemHandler = LazyOptional.of(() -> this.items);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        this.itemHandler.invalidate();
    }

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> capability, @Nullable net.minecraft.core.Direction side) {
        if (capability == ForgeCapabilities.ITEM_HANDLER) {
            return this.itemHandler.cast();
        }

        return super.getCapability(capability, side);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        this.owner = PlayerOwnerData.load(tag, OWNER_UUID_TAG, OWNER_NAME_TAG, OWNER_UID_TAG);
        if (tag.contains(INVENTORY_TAG)) {
            this.items.deserializeNBT(tag.getCompound(INVENTORY_TAG));
        }
        this.powerConsumed = tag.getInt(POWER_CONSUMED_TAG);
        this.powerRemained = tag.getInt(POWER_REMAINED_TAG);
        this.powerGoal = tag.getInt(POWER_GOAL_TAG);
        this.buildType = tag.getInt(BUILD_TYPE_TAG);
        this.structureComplete = tag.getBoolean(STRUCTURE_READY_TAG);
        this.coreLinked = tag.getBoolean(CORE_LINKED_TAG);
        this.linkedCorePos = tag.contains(CORE_POS_TAG) ? BlockPos.of(tag.getLong(CORE_POS_TAG)) : null;

        int[] storedRecord = tag.getIntArray(BUILD_RECORD_TAG);
        Arrays.fill(this.buildRecord, 0);
        System.arraycopy(storedRecord, 0, this.buildRecord, 0, Math.min(this.buildRecord.length, storedRecord.length));
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        if (this.owner != null) {
            this.owner.save(tag, OWNER_UUID_TAG, OWNER_NAME_TAG, OWNER_UID_TAG);
        }

        tag.put(INVENTORY_TAG, this.items.serializeNBT());
        tag.putInt(POWER_CONSUMED_TAG, this.powerConsumed);
        tag.putInt(POWER_REMAINED_TAG, this.powerRemained);
        tag.putInt(POWER_GOAL_TAG, this.powerGoal);
        tag.putInt(BUILD_TYPE_TAG, this.buildType);
        tag.putIntArray(BUILD_RECORD_TAG, this.buildRecord);
        tag.putBoolean(STRUCTURE_READY_TAG, this.structureComplete);
        tag.putBoolean(CORE_LINKED_TAG, this.coreLinked);
        if (this.linkedCorePos != null) {
            tag.putLong(CORE_POS_TAG, this.linkedCorePos.asLong());
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
    public Component getDisplayName() {
        return Component.translatable("block.shincolle.blocklargeshipyard");
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
        return new LargeShipyardMenu(containerId, inventory, this);
    }

    public ItemStackHandler getItems() {
        return this.items;
    }

    public ContainerData getContainerData() {
        return this.dataAccess;
    }

    public void setOwner(Player player) {
        this.owner = PlayerOwnerData.of(player);
        this.markUpdated();
    }

    public boolean canEdit(Player player) {
        return this.owner == null || this.owner.canEdit(player);
    }

    public Component getOwnerLabel() {
        return this.owner == null
                ? Component.translatable("gui.shincolle.waypoint.owner.unassigned")
                : this.owner.displayLabel();
    }

    public int getBuildType() {
        return this.buildType;
    }

    public int getPowerConsumed() {
        return this.powerConsumed;
    }

    public int getPowerRemained() {
        return this.powerRemained;
    }

    @Override
    public int getRouteEnergyStored() {
        return this.powerRemained;
    }

    @Override
    public int getRouteEnergyCapacity() {
        return POWER_MAX;
    }

    public int getPowerGoal() {
        return this.powerGoal;
    }

    public boolean isStructureComplete() {
        return this.structureComplete;
    }

    public boolean isCoreLinked() {
        return this.coreLinked;
    }

    public int[] getMaterialAmounts() {
        return LargeShipyardRecipes.getMaterialAmounts(this.items);
    }

    public boolean hasRemainedPower() {
        return this.powerRemained > BUILD_SPEED;
    }

    @Override
    public int extractRouteEnergy(int amount, boolean simulate) {
        int extracted = Math.min(Math.max(0, amount), this.powerRemained);
        if (extracted > 0 && !simulate) {
            this.powerRemained -= extracted;
            this.markUpdated();
        }
        return extracted;
    }

    @Override
    public int receiveRouteEnergy(int amount, boolean simulate) {
        int accepted = Math.min(Math.max(0, amount), POWER_MAX - this.powerRemained);
        if (accepted > 0 && !simulate) {
            this.powerRemained += accepted;
            this.markUpdated();
        }
        return accepted;
    }

    public int getPowerRemainingScaled(int height) {
        return Mth.floor((this.powerRemained * (double) height) / POWER_MAX);
    }

    public String getBuildTimeString() {
        if (this.powerGoal <= 0 || this.powerConsumed >= this.powerGoal) {
            return "00:00";
        }

        int remainingSeconds = Math.max(0, (int) (((this.powerGoal - this.powerConsumed) / (float) BUILD_SPEED) * 0.05F));
        int minutes = remainingSeconds / 60;
        int seconds = remainingSeconds % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }

    public void cycleShipMode() {
        this.setBuildType(ShipyardBuildTypes.cycleShipMode(this.buildType));
    }

    public void cycleEquipMode() {
        this.setBuildType(ShipyardBuildTypes.cycleEquipMode(this.buildType));
    }

    public void setBuildType(int buildType) {
        this.buildType = buildType;
        if (ShipyardBuildTypes.isLoopMode(buildType)) {
            int[] materials = LargeShipyardRecipes.getMaterialAmounts(this.items);
            System.arraycopy(materials, 0, this.buildRecord, 0, this.buildRecord.length);
        }

        if (!this.canBuild()) {
            this.powerConsumed = 0;
        }

        this.calcPowerGoal();
        this.markUpdated();
    }

    public boolean canBuild() {
        if (!this.structureComplete || this.powerGoal <= 0 || !this.items.getStackInSlot(LargeShipyardRecipes.SLOT_OUTPUT).isEmpty()) {
            return false;
        }

        if (ShipyardBuildTypes.isLoopMode(this.buildType)) {
            return LargeShipyardRecipes.canRecipeBuild(this.buildRecord)
                    && LargeShipyardRecipes.hasMaterialAmounts(this.items, this.buildRecord);
        }

        int[] materials = LargeShipyardRecipes.getMaterialAmounts(this.items);
        return LargeShipyardRecipes.canRecipeBuild(materials);
    }

    private void calcPowerGoal() {
        if (this.buildType == ShipyardBuildTypes.NONE) {
            this.powerGoal = 0;
            return;
        }

        int[] materials = ShipyardBuildTypes.isLoopMode(this.buildType)
                ? this.buildRecord
                : LargeShipyardRecipes.getMaterialAmounts(this.items);
        this.powerGoal = LargeShipyardRecipes.calcGoalPower(materials);
    }

    private boolean isBuilding() {
        return this.hasRemainedPower() && this.canBuild();
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, LargeShipyardBlockEntity shipyard) {
        shipyard.tickServer();
    }

    private void tickServer() {
        boolean changed = false;
        boolean activeBefore = this.getBlockState().getValue(LargeShipyardBlock.ACTIVE);

        changed |= this.updateStructureStatus();
        this.calcPowerGoal();
        changed |= this.refuelFromFuelSlot();
        changed |= this.transferLinkedCoreCharge();

        if (this.isBuilding()) {
            changed |= this.consumeInstantConstruction();
            this.powerRemained = Math.max(0, this.powerRemained - BUILD_SPEED);
            this.powerConsumed += BUILD_SPEED;
            changed = true;

            if (this.powerConsumed >= this.powerGoal) {
                this.finishBuild();
                this.powerConsumed = 0;
                this.powerGoal = 0;

                if (!ShipyardBuildTypes.isLoopMode(this.buildType)) {
                    this.buildType = ShipyardBuildTypes.NONE;
                } else {
                    this.calcPowerGoal();
                }

                changed = true;
            }
        }

        if (!this.canBuild() && this.powerConsumed != 0) {
            this.powerConsumed = 0;
            changed = true;
        }

        boolean activeNow = this.isBuilding();
        if (activeBefore != activeNow && this.level != null) {
            this.level.setBlock(this.worldPosition, this.getBlockState().setValue(LargeShipyardBlock.ACTIVE, activeNow), Block.UPDATE_ALL);
            changed = true;
        }

        if (changed) {
            this.setChanged();
        }
    }

    private boolean updateStructureStatus() {
        this.structureProbeTick++;
        if (this.structureProbeTick < 20 && this.linkedCorePos != null) {
            return false;
        }

        this.structureProbeTick = 0;
        boolean oldStructure = this.structureComplete;
        boolean oldCoreLinked = this.coreLinked;
        BlockPos oldCorePos = this.linkedCorePos;

        this.structureComplete = this.checkStructureRing();
        this.linkedCorePos = this.findLinkedCore();
        this.coreLinked = this.linkedCorePos != null;

        return oldStructure != this.structureComplete
                || oldCoreLinked != this.coreLinked
                || (oldCorePos == null ? this.linkedCorePos != null : !oldCorePos.equals(this.linkedCorePos));
    }

    private boolean checkStructureRing() {
        if (this.level == null) {
            return false;
        }

        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                if (dx == 0 && dz == 0) {
                    continue;
                }
                if (!this.level.getBlockState(this.worldPosition.offset(dx, 0, dz)).is(ModBlocks.BLOCK_GRUDGE_HEAVY_DECO.get())) {
                    return false;
                }
            }
        }

        return true;
    }

    private @Nullable BlockPos findLinkedCore() {
        if (this.level == null) {
            return null;
        }

        BlockPos bestPos = null;
        double bestDistance = Double.MAX_VALUE;

        for (int dx = -6; dx <= 6; dx++) {
            for (int dy = -2; dy <= 2; dy++) {
                for (int dz = -6; dz <= 6; dz++) {
                    BlockPos candidatePos = this.worldPosition.offset(dx, dy, dz);
                    if (!this.level.getBlockState(candidatePos).is(ModBlocks.BLOCK_GRUDGE_HEAVY.get())) {
                        continue;
                    }

                    if (!(this.level.getBlockEntity(candidatePos) instanceof LegacyCoreBlockEntity core) || !core.canProvideCharge()) {
                        continue;
                    }

                    double distance = candidatePos.distSqr(this.worldPosition);
                    if (distance < bestDistance) {
                        bestDistance = distance;
                        bestPos = candidatePos.immutable();
                    }
                }
            }
        }

        return bestPos;
    }

    private boolean transferLinkedCoreCharge() {
        if (!this.structureComplete || this.linkedCorePos == null || this.level == null || this.powerRemained >= POWER_MAX) {
            return false;
        }

        if (!(this.level.getBlockEntity(this.linkedCorePos) instanceof LegacyCoreBlockEntity core)) {
            this.linkedCorePos = null;
            this.coreLinked = false;
            return true;
        }

        if (!core.canProvideCharge()) {
            return false;
        }

        int request = Math.min(CORE_TRANSFER_PER_TICK, POWER_MAX - this.powerRemained);
        int transferred = core.consumeCharge(request);
        if (transferred <= 0) {
            return false;
        }

        this.powerRemained += transferred;
        return true;
    }

    private boolean refuelFromFuelSlot() {
        ItemStack fuelStack = this.items.getStackInSlot(LargeShipyardRecipes.SLOT_FUEL);
        if (fuelStack.isEmpty() || fuelStack.is(ModItems.INSTANTCONMAT.get()) || this.powerRemained >= POWER_MAX) {
            return false;
        }

        int fuelValue = Math.round(LargeShipyardRecipes.getFuelValue(fuelStack) * FUEL_MAGNIFICATION);
        if (fuelValue <= 0) {
            return false;
        }

        ItemStack remainder = fuelStack.hasCraftingRemainingItem() ? fuelStack.getCraftingRemainingItem() : ItemStack.EMPTY;
        if (!remainder.isEmpty() && fuelStack.getCount() > 1) {
            return false;
        }

        this.powerRemained = Math.min(POWER_MAX, this.powerRemained + fuelValue);
        if (remainder.isEmpty()) {
            fuelStack.shrink(1);
            if (fuelStack.isEmpty()) {
                this.items.setStackInSlot(LargeShipyardRecipes.SLOT_FUEL, ItemStack.EMPTY);
            }
        } else {
            this.items.setStackInSlot(LargeShipyardRecipes.SLOT_FUEL, remainder.copy());
        }

        return true;
    }

    private boolean consumeInstantConstruction() {
        ItemStack fuelStack = this.items.getStackInSlot(LargeShipyardRecipes.SLOT_FUEL);
        if (!fuelStack.is(ModItems.INSTANTCONMAT.get())) {
            return false;
        }

        fuelStack.shrink(1);
        this.powerConsumed += POWER_INSTANT;
        if (fuelStack.isEmpty()) {
            this.items.setStackInSlot(LargeShipyardRecipes.SLOT_FUEL, ItemStack.EMPTY);
        }

        return true;
    }

    private void finishBuild() {
        int[] materials;
        if (ShipyardBuildTypes.isLoopMode(this.buildType)) {
            materials = Arrays.copyOf(this.buildRecord, this.buildRecord.length);
            LargeShipyardRecipes.consumeMaterialAmounts(this.items, this.buildRecord);
        } else {
            materials = LargeShipyardRecipes.getMaterialAmounts(this.items);
            LargeShipyardRecipes.consumeAllMaterials(this.items);
        }

        RandomSource random = this.level != null ? this.level.getRandom() : RandomSource.create();
        ItemStack result = LargeShipyardRecipes.createBuildResult(this.buildType, materials, random);
        this.items.setStackInSlot(LargeShipyardRecipes.SLOT_OUTPUT, result);
    }

    public void dropContents() {
        if (this.level == null || this.level.isClientSide()) {
            return;
        }

        for (int slot = 0; slot < this.items.getSlots(); slot++) {
            ItemStack stack = this.items.getStackInSlot(slot);
            if (!stack.isEmpty()) {
                Block.popResource(this.level, this.worldPosition, stack.copy());
                this.items.setStackInSlot(slot, ItemStack.EMPTY);
            }
        }
    }

    private void markUpdated() {
        this.setChanged();

        if (this.level != null) {
            BlockState state = this.getBlockState();
            this.level.sendBlockUpdated(this.worldPosition, state, state, Block.UPDATE_ALL);
        }
    }
}
