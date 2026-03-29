package com.lulan.shincolle.blockentity;

import com.lulan.shincolle.crafting.LargeShipyardRecipes;
import com.lulan.shincolle.crafting.ShipyardBuildTypes;
import com.lulan.shincolle.crafting.SmallShipyardRecipes;
import com.lulan.shincolle.menu.LargeShipyardMenu;
import com.lulan.shincolle.menu.LegacyCoreMenu;
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
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nullable;
import java.util.Arrays;

public class HeavyGrudgeBlockEntity extends BlockEntity implements LegacyCoreAccess, LargeShipyardAccess, RouteEnergyAccess, MenuProvider {

    private static final String OWNER_NAME_TAG = "OwnerName";
    private static final String OWNER_UUID_TAG = "OwnerUuid";
    private static final String OWNER_UID_TAG = "OwnerUID";

    private static final String MODE_TAG = "Mode";
    private static final String CHARGE_TAG = "Charge";
    private static final String CORE_FUEL_ITEMS_TAG = "FuelItems";

    private static final String SHIPYARD_ITEMS_TAG = "ShipyardInventory";
    private static final String SHIPYARD_TANK_TAG = "ShipyardTank";
    private static final String POWER_CONSUMED_TAG = "ConsumedPower";
    private static final String POWER_REMAINED_TAG = "RemainedPower";
    private static final String POWER_GOAL_TAG = "GoalPower";
    private static final String BUILD_TYPE_TAG = "BuildType";
    private static final String BUILD_RECORD_TAG = "BuildRecord";
    private static final String MATERIAL_STOCK_TAG = "MaterialStock";
    private static final String STRUCTURE_READY_TAG = "StructureReady";

    private static final int MODE_IDLE = 0;
    private static final int MODE_CHARGE = 1;
    private static final int MODE_DRAIN = 2;
    private static final int MAX_MODE = MODE_DRAIN;
    private static final int CORE_FUEL_SLOT_COUNT = 9;

    public static final int CORE_CHARGE_PER_TICK = 6;
    public static final int CORE_MAX_CHARGE = 20000;
    public static final int POWER_MAX = 1382400;
    public static final int BUILD_SPEED = 48;
    public static final int POWER_INSTANT = BUILD_SPEED * 1200;
    private static final int CORE_TRANSFER_PER_TICK = 96;
    private static final int LAVA_POWER_PER_BUCKET = 20000;
    private static final int LAVA_TRANSFER_AMOUNT = 1000;

    @Nullable
    private PlayerOwnerData owner;
    private final ItemStackHandler coreFuelItems = new ItemStackHandler(CORE_FUEL_SLOT_COUNT) {
        @Override
        protected void onContentsChanged(int slot) {
            HeavyGrudgeBlockEntity.this.markUpdated();
        }
    };
    private final ItemStackHandler shipyardItems = new ItemStackHandler(LargeShipyardRecipes.SLOT_COUNT) {
        @Override
        protected void onContentsChanged(int slot) {
            HeavyGrudgeBlockEntity.this.markUpdated();
        }
    };
    private final FluidTank lavaTank = new FluidTank(2000) {
        @Override
        protected void onContentsChanged() {
            HeavyGrudgeBlockEntity.this.markUpdated();
        }
    };
    private final ContainerData coreData = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> HeavyGrudgeBlockEntity.this.mode;
                case 1 -> HeavyGrudgeBlockEntity.this.storedCharge;
                case 2 -> CORE_MAX_CHARGE;
                case 3 -> HeavyGrudgeBlockEntity.this.structureComplete ? 1 : 0;
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
            switch (index) {
                case 0 -> HeavyGrudgeBlockEntity.this.mode = Mth.clamp(value, MODE_IDLE, MAX_MODE);
                case 1 -> HeavyGrudgeBlockEntity.this.storedCharge = Mth.clamp(value, 0, CORE_MAX_CHARGE);
                case 3 -> HeavyGrudgeBlockEntity.this.structureComplete = value != 0;
                default -> {
                }
            }
        }

        @Override
        public int getCount() {
            return 4;
        }
    };
    private final ContainerData shipyardData = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> HeavyGrudgeBlockEntity.this.buildType;
                case 1 -> HeavyGrudgeBlockEntity.this.powerConsumed;
                case 2 -> HeavyGrudgeBlockEntity.this.powerRemained;
                case 3 -> HeavyGrudgeBlockEntity.this.powerGoal;
                case 4 -> HeavyGrudgeBlockEntity.this.structureComplete ? 1 : 0;
                case 5 -> HeavyGrudgeBlockEntity.this.structureComplete ? 1 : 0;
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
            switch (index) {
                case 0 -> HeavyGrudgeBlockEntity.this.buildType = value;
                case 1 -> HeavyGrudgeBlockEntity.this.powerConsumed = value;
                case 2 -> HeavyGrudgeBlockEntity.this.powerRemained = value;
                case 3 -> HeavyGrudgeBlockEntity.this.powerGoal = value;
                case 4, 5 -> HeavyGrudgeBlockEntity.this.structureComplete = value != 0;
                default -> {
                }
            }
        }

        @Override
        public int getCount() {
            return 6;
        }
    };
    private LazyOptional<IItemHandler> coreItemHandler = LazyOptional.empty();
    private LazyOptional<IItemHandler> shipyardItemHandler = LazyOptional.empty();
    private LazyOptional<IFluidHandler> fluidHandler = LazyOptional.empty();
    private int mode;
    private int storedCharge;
    private int powerConsumed;
    private int powerRemained;
    private int powerGoal;
    private int buildType;
    private final int[] buildRecord = new int[4];
    private final int[] materialStock = new int[4];
    private boolean structureComplete;
    private int structureProbeTick;

    public HeavyGrudgeBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.GRUDGE_HEAVY.get(), pos, state);
    }

    @Override
    public void onLoad() {
        super.onLoad();
        this.coreItemHandler = LazyOptional.of(() -> this.coreFuelItems);
        this.shipyardItemHandler = LazyOptional.of(() -> this.shipyardItems);
        this.fluidHandler = LazyOptional.of(() -> this.lavaTank);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        this.coreItemHandler.invalidate();
        this.shipyardItemHandler.invalidate();
        this.fluidHandler.invalidate();
    }

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> capability, @Nullable net.minecraft.core.Direction side) {
        if (capability == ForgeCapabilities.ITEM_HANDLER) {
            return (this.structureComplete ? this.shipyardItemHandler : this.coreItemHandler).cast();
        }

        if (capability == ForgeCapabilities.FLUID_HANDLER && this.structureComplete) {
            return this.fluidHandler.cast();
        }

        return super.getCapability(capability, side);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        this.owner = PlayerOwnerData.load(tag, OWNER_UUID_TAG, OWNER_NAME_TAG, OWNER_UID_TAG);
        this.mode = Mth.clamp(tag.getInt(MODE_TAG), MODE_IDLE, MAX_MODE);
        this.storedCharge = Mth.clamp(tag.getInt(CHARGE_TAG), 0, CORE_MAX_CHARGE);
        if (tag.contains(CORE_FUEL_ITEMS_TAG)) {
            this.coreFuelItems.deserializeNBT(tag.getCompound(CORE_FUEL_ITEMS_TAG));
        }
        if (tag.contains(SHIPYARD_ITEMS_TAG)) {
            this.shipyardItems.deserializeNBT(tag.getCompound(SHIPYARD_ITEMS_TAG));
        }
        if (tag.contains(SHIPYARD_TANK_TAG)) {
            this.lavaTank.readFromNBT(tag.getCompound(SHIPYARD_TANK_TAG));
        }
        this.powerConsumed = tag.getInt(POWER_CONSUMED_TAG);
        this.powerRemained = tag.getInt(POWER_REMAINED_TAG);
        this.powerGoal = tag.getInt(POWER_GOAL_TAG);
        this.buildType = tag.getInt(BUILD_TYPE_TAG);
        this.structureComplete = tag.getBoolean(STRUCTURE_READY_TAG);

        int[] storedBuildRecord = tag.getIntArray(BUILD_RECORD_TAG);
        Arrays.fill(this.buildRecord, 0);
        System.arraycopy(storedBuildRecord, 0, this.buildRecord, 0, Math.min(this.buildRecord.length, storedBuildRecord.length));

        int[] storedMaterialStock = tag.getIntArray(MATERIAL_STOCK_TAG);
        Arrays.fill(this.materialStock, 0);
        System.arraycopy(storedMaterialStock, 0, this.materialStock, 0, Math.min(this.materialStock.length, storedMaterialStock.length));
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        if (this.owner != null) {
            this.owner.save(tag, OWNER_UUID_TAG, OWNER_NAME_TAG, OWNER_UID_TAG);
        }

        tag.putInt(MODE_TAG, this.mode);
        tag.putInt(CHARGE_TAG, this.storedCharge);
        tag.put(CORE_FUEL_ITEMS_TAG, this.coreFuelItems.serializeNBT());
        tag.put(SHIPYARD_ITEMS_TAG, this.shipyardItems.serializeNBT());
        tag.put(SHIPYARD_TANK_TAG, this.lavaTank.writeToNBT(new CompoundTag()));
        tag.putInt(POWER_CONSUMED_TAG, this.powerConsumed);
        tag.putInt(POWER_REMAINED_TAG, this.powerRemained);
        tag.putInt(POWER_GOAL_TAG, this.powerGoal);
        tag.putInt(BUILD_TYPE_TAG, this.buildType);
        tag.putIntArray(BUILD_RECORD_TAG, this.buildRecord);
        tag.putIntArray(MATERIAL_STOCK_TAG, this.materialStock);
        tag.putBoolean(STRUCTURE_READY_TAG, this.structureComplete);
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
        return this.structureComplete
                ? Component.translatable("block.shincolle.blocklargeshipyard")
                : Component.translatable("block.shincolle.blockgrudgeheavy");
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
        return this.structureComplete
                ? new LargeShipyardMenu(containerId, inventory, this)
                : new LegacyCoreMenu(containerId, inventory, this);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, HeavyGrudgeBlockEntity heavy) {
        heavy.tickServer();
    }

    private void tickServer() {
        boolean changed = this.updateStructureStatus();

        if (this.structureComplete) {
            changed |= this.tickStructuredShipyard();
        } else {
            changed |= this.tickStandaloneCore();
        }

        if (changed) {
            this.markUpdated();
        }
    }

    private boolean tickStandaloneCore() {
        boolean changed = false;

        if (this.mode != MODE_IDLE) {
            changed |= this.refuelCoreFromInventory();
        }

        if (this.mode == MODE_DRAIN && this.storedCharge >= CORE_CHARGE_PER_TICK) {
            this.storedCharge = Math.max(0, this.storedCharge - CORE_CHARGE_PER_TICK);
            changed = true;
        }

        return changed;
    }

    private boolean tickStructuredShipyard() {
        boolean changed = false;
        changed |= this.refuelCoreFromInventory();
        changed |= this.absorbGenericMaterials();
        changed |= this.refuelFromFuelSlot();
        changed |= this.refuelFromLavaTank();
        changed |= this.transferCoreCharge();
        changed |= this.calcPowerGoal();

        if (this.canBuild()) {
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

        return changed;
    }

    private boolean updateStructureStatus() {
        if (this.level == null) {
            return false;
        }

        this.structureProbeTick++;
        if (this.structureProbeTick < 20) {
            return false;
        }

        this.structureProbeTick = 0;
        boolean old = this.structureComplete;
        this.structureComplete = LargeShipyardStructureHelper.isValidMaster(this.level, this.worldPosition);

        if (!this.structureComplete && this.powerConsumed != 0) {
            this.powerConsumed = 0;
        }

        return old != this.structureComplete;
    }

    private boolean refuelCoreFromInventory() {
        if (this.storedCharge >= CORE_MAX_CHARGE) {
            return false;
        }

        for (int slot = 0; slot < this.coreFuelItems.getSlots(); slot++) {
            ItemStack stack = this.coreFuelItems.getStackInSlot(slot);
            int fuelValue = this.getCoreFuelValue(stack);
            if (fuelValue <= 0) {
                continue;
            }

            this.storedCharge = Math.min(CORE_MAX_CHARGE, this.storedCharge + fuelValue);
            stack.shrink(1);
            this.coreFuelItems.setStackInSlot(slot, stack.isEmpty() ? ItemStack.EMPTY : stack);
            return true;
        }

        return false;
    }

    private boolean absorbGenericMaterials() {
        boolean changed = false;

        for (int slot = 2; slot < this.shipyardItems.getSlots(); slot++) {
            ItemStack stack = this.shipyardItems.getStackInSlot(slot);
            int material = LargeShipyardRecipes.materialSlot(stack);
            if (material < 0) {
                continue;
            }

            this.materialStock[material] += stack.getCount();
            this.shipyardItems.setStackInSlot(slot, ItemStack.EMPTY);
            changed = true;
        }

        return changed;
    }

    private boolean refuelFromFuelSlot() {
        ItemStack fuelStack = this.shipyardItems.getStackInSlot(LargeShipyardRecipes.SLOT_FUEL);
        if (fuelStack.isEmpty() || fuelStack.is(ModItems.INSTANTCONMAT.get()) || this.powerRemained >= POWER_MAX) {
            return false;
        }

        int fuelValue = SmallShipyardRecipes.getFuelValue(fuelStack);
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
            this.shipyardItems.setStackInSlot(LargeShipyardRecipes.SLOT_FUEL, fuelStack.isEmpty() ? ItemStack.EMPTY : fuelStack);
        } else {
            this.shipyardItems.setStackInSlot(LargeShipyardRecipes.SLOT_FUEL, remainder.copy());
        }

        return true;
    }

    private boolean refuelFromLavaTank() {
        if (this.lavaTank.getFluidAmount() < LAVA_TRANSFER_AMOUNT || this.powerRemained >= POWER_MAX) {
            return false;
        }

        if (this.lavaTank.drain(LAVA_TRANSFER_AMOUNT, IFluidHandler.FluidAction.EXECUTE).isEmpty()) {
            return false;
        }

        this.powerRemained = Math.min(POWER_MAX, this.powerRemained + LAVA_POWER_PER_BUCKET);
        return true;
    }

    private boolean transferCoreCharge() {
        if (this.storedCharge <= 0 || this.powerRemained >= POWER_MAX) {
            return false;
        }

        int transferred = Math.min(CORE_TRANSFER_PER_TICK, Math.min(this.storedCharge, POWER_MAX - this.powerRemained));
        if (transferred <= 0) {
            return false;
        }

        this.storedCharge -= transferred;
        this.powerRemained += transferred;
        return true;
    }

    private boolean calcPowerGoal() {
        int oldGoal = this.powerGoal;
        if (this.buildType == ShipyardBuildTypes.NONE) {
            this.powerGoal = 0;
        } else {
            int[] materials = ShipyardBuildTypes.isLoopMode(this.buildType)
                    ? this.buildRecord
                    : Arrays.copyOf(this.materialStock, this.materialStock.length);
            this.powerGoal = LargeShipyardRecipes.calcGoalPower(materials);
        }
        return oldGoal != this.powerGoal;
    }

    private boolean consumeInstantConstruction() {
        for (int slot = 1; slot < this.shipyardItems.getSlots(); slot++) {
            ItemStack stack = this.shipyardItems.getStackInSlot(slot);
            if (!stack.is(ModItems.INSTANTCONMAT.get())) {
                continue;
            }

            stack.shrink(1);
            this.powerConsumed += POWER_INSTANT;
            this.shipyardItems.setStackInSlot(slot, stack.isEmpty() ? ItemStack.EMPTY : stack);
            return true;
        }

        return false;
    }

    private void finishBuild() {
        int[] materials;
        if (ShipyardBuildTypes.isLoopMode(this.buildType)) {
            materials = Arrays.copyOf(this.buildRecord, this.buildRecord.length);
            for (int i = 0; i < this.materialStock.length; i++) {
                this.materialStock[i] = Math.max(0, this.materialStock[i] - this.buildRecord[i]);
            }
        } else {
            materials = Arrays.copyOf(this.materialStock, this.materialStock.length);
            Arrays.fill(this.materialStock, 0);
        }

        RandomSource random = this.level != null ? this.level.getRandom() : RandomSource.create();
        ItemStack result = LargeShipyardRecipes.createBuildResult(this.buildType, materials, random);
        this.shipyardItems.setStackInSlot(LargeShipyardRecipes.SLOT_OUTPUT, result);
    }

    public void dropContents() {
        if (this.level == null || this.level.isClientSide()) {
            return;
        }

        for (int slot = 0; slot < this.coreFuelItems.getSlots(); slot++) {
            ItemStack stack = this.coreFuelItems.getStackInSlot(slot);
            if (!stack.isEmpty()) {
                Block.popResource(this.level, this.worldPosition, stack.copy());
                this.coreFuelItems.setStackInSlot(slot, ItemStack.EMPTY);
            }
        }

        for (int slot = 0; slot < this.shipyardItems.getSlots(); slot++) {
            ItemStack stack = this.shipyardItems.getStackInSlot(slot);
            if (!stack.isEmpty()) {
                Block.popResource(this.level, this.worldPosition, stack.copy());
                this.shipyardItems.setStackInSlot(slot, ItemStack.EMPTY);
            }
        }

        this.dropMaterialStock();
    }

    private void dropMaterialStock() {
        for (int i = 0; i < this.materialStock.length; i++) {
            Item item = switch (i) {
                case 0 -> ModItems.GRUDGE.get();
                case 1 -> ModItems.ABYSSMETAL.get();
                case 2 -> ModItems.AMMO.get();
                case 3 -> ModItems.ABYSSMETAL1.get();
                default -> null;
            };
            if (item == null) {
                continue;
            }

            int remaining = this.materialStock[i];
            while (remaining > 0) {
                int dropped = Math.min(64, remaining);
                Block.popResource(this.level, this.worldPosition, new ItemStack(item, dropped));
                remaining -= dropped;
            }
            this.materialStock[i] = 0;
        }
    }

    public void setOwner(Player player) {
        this.owner = PlayerOwnerData.of(player);
        this.markUpdated();
    }

    public boolean isStructureComplete() {
        return this.structureComplete;
    }

    @Override
    public int getRouteEnergyStored() {
        return this.structureComplete ? this.powerRemained : this.storedCharge;
    }

    @Override
    public int getRouteEnergyCapacity() {
        return this.structureComplete ? POWER_MAX : CORE_MAX_CHARGE;
    }

    @Override
    public boolean canEdit(Player player) {
        return this.owner == null || this.owner.canEdit(player);
    }

    @Override
    public Component getOwnerLabel() {
        return this.owner == null
                ? Component.translatable("gui.shincolle.waypoint.owner.unassigned")
                : this.owner.displayLabel();
    }

    @Override
    public Component getBlockLabel() {
        return Component.translatable("block.shincolle.blockgrudgeheavy");
    }

    @Override
    public ItemStackHandler getFuelItems() {
        return this.coreFuelItems;
    }

    @Override
    public ContainerData getCoreContainerData() {
        return this.coreData;
    }

    @Override
    public boolean isFuelItem(ItemStack stack) {
        return this.getCoreFuelValue(stack) > 0;
    }

    @Override
    public void cycleCoreMode() {
        this.mode = (this.mode + 1) % (MAX_MODE + 1);
        this.markUpdated();
    }

    @Override
    public boolean isVolCore() {
        return false;
    }

    @Override
    public ItemStackHandler getItems() {
        return this.shipyardItems;
    }

    @Override
    public ContainerData getContainerData() {
        return this.shipyardData;
    }

    @Override
    public int[] getMaterialAmounts() {
        return Arrays.copyOf(this.materialStock, this.materialStock.length);
    }

    @Override
    public void cycleShipMode() {
        this.setBuildType(ShipyardBuildTypes.cycleShipMode(this.buildType));
    }

    @Override
    public void cycleEquipMode() {
        this.setBuildType(ShipyardBuildTypes.cycleEquipMode(this.buildType));
    }

    @Override
    public int extractRouteEnergy(int amount, boolean simulate) {
        int available = this.getRouteEnergyStored();
        int extracted = Math.min(Math.max(0, amount), available);
        if (extracted <= 0) {
            return 0;
        }

        if (!simulate) {
            if (this.structureComplete) {
                this.powerRemained -= extracted;
            } else {
                this.storedCharge -= extracted;
            }
            this.markUpdated();
        }

        return extracted;
    }

    @Override
    public int receiveRouteEnergy(int amount, boolean simulate) {
        int accepted = Math.min(Math.max(0, amount), this.getRouteEnergyCapacity() - this.getRouteEnergyStored());
        if (accepted <= 0) {
            return 0;
        }

        if (!simulate) {
            if (this.structureComplete) {
                this.powerRemained += accepted;
            } else {
                this.storedCharge += accepted;
            }
            this.markUpdated();
        }

        return accepted;
    }

    @Override
    public boolean usesGenericInventory() {
        return true;
    }

    private void setBuildType(int buildType) {
        this.buildType = buildType;
        if (ShipyardBuildTypes.isLoopMode(buildType)) {
            System.arraycopy(this.materialStock, 0, this.buildRecord, 0, this.buildRecord.length);
        }

        if (!this.canBuild()) {
            this.powerConsumed = 0;
        }

        this.calcPowerGoal();
        this.markUpdated();
    }

    private boolean canBuild() {
        if (!this.structureComplete || this.powerGoal <= 0 || !this.shipyardItems.getStackInSlot(LargeShipyardRecipes.SLOT_OUTPUT).isEmpty()) {
            return false;
        }

        int[] materials = ShipyardBuildTypes.isLoopMode(this.buildType)
                ? this.buildRecord
                : this.materialStock;
        return LargeShipyardRecipes.canRecipeBuild(materials) && this.powerRemained > BUILD_SPEED;
    }

    private int getCoreFuelValue(ItemStack stack) {
        if (stack.isEmpty()) {
            return 0;
        }

        if (stack.is(ModBlocks.BLOCK_GRUDGE.get().asItem())) {
            return Math.max(CORE_CHARGE_PER_TICK * 270, CORE_MAX_CHARGE * 9 / 10);
        }
        if (stack.is(ModItems.GRUDGE1.get())) {
            return Math.max(CORE_CHARGE_PER_TICK * 60, CORE_MAX_CHARGE / 4);
        }
        if (stack.is(ModItems.GRUDGE.get())) {
            return Math.max(CORE_CHARGE_PER_TICK * 30, CORE_MAX_CHARGE / 10);
        }

        return 0;
    }

    private void markUpdated() {
        this.setChanged();

        if (this.level != null) {
            BlockState state = this.getBlockState();
            this.level.sendBlockUpdated(this.worldPosition, state, state, Block.UPDATE_ALL);
        }
    }
}
