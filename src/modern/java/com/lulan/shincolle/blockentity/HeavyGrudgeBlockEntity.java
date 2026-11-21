package com.lulan.shincolle.blockentity;

import com.lulan.shincolle.item.LegacyItemData;
import com.lulan.shincolle.config.ShinColleConfig;
import com.lulan.shincolle.block.HeavyGrudgeBlock;
import com.lulan.shincolle.crafting.LargeShipyardRecipes;
import com.lulan.shincolle.crafting.ShipyardBuildTypes;
import com.lulan.shincolle.crafting.SmallShipyardRecipes;
import com.lulan.shincolle.item.HeavyGrudgeBlockItem;
import com.lulan.shincolle.item.LegacyEntityItemMarker;
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
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.capabilities.BlockCapability;
import java.util.Optional;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;

import javax.annotation.Nullable;
import java.util.Arrays;

public class HeavyGrudgeBlockEntity extends BlockEntity implements LargeShipyardAccess, MenuProvider {

    private static final String OWNER_NAME_TAG = "OwnerName";
    private static final String OWNER_UUID_TAG = "OwnerUuid";
    private static final String OWNER_UID_TAG = "OwnerUID";

    private static final String SHIPYARD_ITEMS_TAG = "ShipyardInventory";
    private static final String SHIPYARD_TANK_TAG = "ShipyardTank";
    private static final String POWER_CONSUMED_TAG = "powerConsumed";
    private static final String POWER_REMAINED_TAG = "powerRemained";
    private static final String POWER_GOAL_TAG = "powerGoal";
    private static final String BUILD_TYPE_TAG = "buildType";
    private static final String INV_MODE_TAG = "invMode";
    private static final String SELECT_MAT_TAG = "selectMat";
    private static final String MATS_BUILD_TAG = "matsBuild";
    private static final String MATS_STOCK_TAG = "matsStock";
    private static final String STRUCTURE_READY_TAG = "StructureReady";

    private static final String LEGACY_POWER_CONSUMED_TAG = "ConsumedPower";
    private static final String LEGACY_POWER_REMAINED_TAG = "RemainedPower";
    private static final String LEGACY_POWER_GOAL_TAG = "GoalPower";
    private static final String LEGACY_BUILD_TYPE_TAG = "BuildType";
    private static final String LEGACY_BUILD_RECORD_TAG = "BuildRecord";
    private static final String LEGACY_MATERIAL_STOCK_TAG = "MaterialStock";

    public static int powerMax() { return (int) ShinColleConfig.tileShipyardLarge()[0]; }
    public static int buildSpeed() { return (int) ShinColleConfig.tileShipyardLarge()[1]; }
    public static int powerInstant() { return buildSpeed() * 1200; }
    public static float fuelMagnification() { return (float) ShinColleConfig.tileShipyardLarge()[2]; }
    private static final int LAVA_TRANSFER_AMOUNT = 40;

    @Nullable
    private PlayerOwnerData owner;
    private final ItemStackHandler shipyardItems = new ItemStackHandler(LargeShipyardRecipes.SLOT_COUNT) {
        @Override
        protected void onContentsChanged(int slot) {
            HeavyGrudgeBlockEntity.this.markUpdated();
        }
    };
    private final FluidTank lavaTank = new FluidTank(2000) {
        @Override
        public boolean isFluidValid(FluidStack stack) {
            return stack.getFluid().isSame(Fluids.LAVA);
        }

        @Override
        protected void onContentsChanged() {
            HeavyGrudgeBlockEntity.this.markUpdated();
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
                case 5 -> 0;
                case 6 -> HeavyGrudgeBlockEntity.this.invMode;
                case 7 -> HeavyGrudgeBlockEntity.this.selectMat;
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
            switch (index) {
                case 0 -> HeavyGrudgeBlockEntity.this.buildType = value;
                case 1 -> HeavyGrudgeBlockEntity.this.powerConsumed = value;
                case 2 -> HeavyGrudgeBlockEntity.this.powerRemained = Mth.clamp(value, 0, powerMax());
                case 3 -> HeavyGrudgeBlockEntity.this.powerGoal = Math.max(0, value);
                case 4 -> HeavyGrudgeBlockEntity.this.structureComplete = value != 0;
                case 6 -> HeavyGrudgeBlockEntity.this.invMode = value == 0 ? 0 : 1;
                case 7 -> HeavyGrudgeBlockEntity.this.selectMat = Mth.clamp(value, 0, 3);
                default -> {
                }
            }
        }

        @Override
        public int getCount() {
            return 8;
        }
    };
    private int powerConsumed;
    private int powerRemained;
    private int powerGoal;
    private int buildType;
    private int invMode;
    private int selectMat;
    private final int[] matsBuild = new int[4];
    private final int[] matsStock = new int[4];
    private boolean structureComplete;

    public HeavyGrudgeBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.GRUDGE_HEAVY.get(), pos, state);
    }



    public <T> Optional<T> getCapability(BlockCapability<T, net.minecraft.core.Direction> capability) {
        return getCapability(capability, null);
    }

    public <T> Optional<T> getCapability(BlockCapability<T, net.minecraft.core.Direction> capability, @Nullable net.minecraft.core.Direction side) {
        if (!this.structureComplete) {
            return Optional.empty();
        }

        if (capability == Capabilities.ItemHandler.BLOCK) {
            return Optional.of(capability.typeClass().cast(this.shipyardItems));
        }

        if (capability == Capabilities.FluidHandler.BLOCK) {
            return Optional.of(capability.typeClass().cast(this.lavaTank));
        }

        return Optional.empty();
    }

    @Override
    protected void loadAdditional(CompoundTag tag, net.minecraft.core.HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        this.owner = PlayerOwnerData.load(tag, OWNER_UUID_TAG, OWNER_NAME_TAG, OWNER_UID_TAG);
        if (tag.contains(SHIPYARD_ITEMS_TAG)) {
            this.shipyardItems.deserializeNBT(registries, tag.getCompound(SHIPYARD_ITEMS_TAG));
        }
        if (tag.contains(SHIPYARD_TANK_TAG)) {
            this.lavaTank.readFromNBT(registries, tag.getCompound(SHIPYARD_TANK_TAG));
        }
        this.powerConsumed = getIntCompat(tag, POWER_CONSUMED_TAG, LEGACY_POWER_CONSUMED_TAG);
        this.powerRemained = Mth.clamp(getIntCompat(tag, POWER_REMAINED_TAG, LEGACY_POWER_REMAINED_TAG), 0, powerMax());
        this.powerGoal = Math.max(0, getIntCompat(tag, POWER_GOAL_TAG, LEGACY_POWER_GOAL_TAG));
        this.buildType = getIntCompat(tag, BUILD_TYPE_TAG, LEGACY_BUILD_TYPE_TAG);
        this.invMode = tag.getInt(INV_MODE_TAG) == 0 ? 0 : 1;
        this.selectMat = Mth.clamp(tag.getInt(SELECT_MAT_TAG), 0, 3);
        this.structureComplete = tag.getBoolean(STRUCTURE_READY_TAG);

        readIntArrayCompat(tag, this.matsBuild, MATS_BUILD_TAG, LEGACY_BUILD_RECORD_TAG);
        readIntArrayCompat(tag, this.matsStock, MATS_STOCK_TAG, LEGACY_MATERIAL_STOCK_TAG);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, net.minecraft.core.HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        if (this.owner != null) {
            this.owner.save(tag, OWNER_UUID_TAG, OWNER_NAME_TAG, OWNER_UID_TAG);
        }

        tag.put(SHIPYARD_ITEMS_TAG, this.shipyardItems.serializeNBT(registries));
        tag.put(SHIPYARD_TANK_TAG, this.lavaTank.writeToNBT(registries, new CompoundTag()));
        tag.putInt(POWER_CONSUMED_TAG, this.powerConsumed);
        tag.putInt(POWER_REMAINED_TAG, this.powerRemained);
        tag.putInt(POWER_GOAL_TAG, this.powerGoal);
        tag.putInt(BUILD_TYPE_TAG, this.buildType);
        tag.putInt(INV_MODE_TAG, this.invMode);
        tag.putInt(SELECT_MAT_TAG, this.selectMat);
        tag.putIntArray(MATS_BUILD_TAG, this.matsBuild);
        tag.putIntArray(MATS_STOCK_TAG, this.matsStock);
        tag.putBoolean(STRUCTURE_READY_TAG, this.structureComplete);
    }

    @Override
    public CompoundTag getUpdateTag(net.minecraft.core.HolderLookup.Provider registries) {
        return this.saveWithoutMetadata(this.level.registryAccess());
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public Component getDisplayName() {
        return this.structureComplete
                ? Component.translatable("gui.shincolle.shipyard.large")
                : Component.translatable("block.shincolle.blockgrudgeheavy");
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
        return this.structureComplete ? new LargeShipyardMenu(containerId, inventory, this) : null;
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, HeavyGrudgeBlockEntity heavy) {
        heavy.tickServer();
    }

    private void tickServer() {
        if (!this.structureComplete) {
            return;
        }

        if (this.level == null || !LargeShipyardStructureHelper.isValidMaster(this.level, this.worldPosition)) {
            this.resetStructure(true);
            return;
        }

        boolean changed = this.tickStructuredShipyard();
        changed |= this.updateActiveBlockState();
        if (changed) {
            this.markUpdated();
        }
    }

    private boolean tickStructuredShipyard() {
        boolean changed = this.invMode == 0 ? this.absorbGenericMaterials() : this.releaseGenericMaterials();
        changed |= this.refuelFromInventory();
        changed |= this.refuelFromLavaTank();
        changed |= this.calcPowerGoal();

        if (this.isBuilding()) {
            changed |= this.consumeInstantConstruction();
            this.powerRemained = Math.max(0, this.powerRemained - buildSpeed());
            this.powerConsumed += buildSpeed();
            changed = true;

            if (this.powerConsumed >= this.powerGoal) {
                this.finishBuild();
                this.powerConsumed = 0;
                this.powerGoal = 0;

                if (ShipyardBuildTypes.isLoopMode(this.buildType)) {
                    this.setRepeatBuild();
                } else {
                    this.buildType = ShipyardBuildTypes.NONE;
                    Arrays.fill(this.matsBuild, 0);
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

    public boolean tryAssembleStructure() {
        if (this.level == null || this.structureComplete || !LargeShipyardStructureHelper.isValidMaster(this.level, this.worldPosition)) {
            return false;
        }

        this.structureComplete = true;
        this.setStructureBlockStates(1, true);
        this.updateActiveBlockState();
        this.markUpdated();
        return true;
    }

    public void resetStructure(boolean includeMaster) {
        if (this.level == null) {
            this.structureComplete = false;
            return;
        }

        this.structureComplete = false;
        this.powerConsumed = 0;
        this.powerGoal = 0;
        this.setStructureBlockStates(0, includeMaster);
        this.markUpdated();
    }

    private void setStructureBlockStates(int mbs, boolean includeMaster) {
        if (this.level == null) {
            return;
        }

        if (includeMaster && this.level.getBlockState(this.worldPosition).is(ModBlocks.BLOCK_GRUDGE_HEAVY.get())) {
            this.level.setBlock(this.worldPosition, this.level.getBlockState(this.worldPosition).setValue(HeavyGrudgeBlock.MBS, mbs), Block.UPDATE_ALL);
        }

        for (BlockPos servantPos : LargeShipyardStructureHelper.getServantPositions(this.worldPosition)) {
            if (!this.level.getBlockState(servantPos).is(ModBlocks.BLOCK_POLYMETAL.get())) {
                continue;
            }
            this.level.setBlock(servantPos, this.level.getBlockState(servantPos).setValue(HeavyGrudgeBlock.MBS, mbs), Block.UPDATE_ALL);
            if (this.level.getBlockEntity(servantPos) instanceof PolymetalServantBlockEntity servant) {
                servant.setMasterPos(mbs > 0 ? this.worldPosition : null);
            }
        }
    }

    private boolean updateActiveBlockState() {
        if (this.level == null || !this.getBlockState().hasProperty(HeavyGrudgeBlock.MBS)) {
            return false;
        }

        int target = this.structureComplete ? (this.isBuilding() ? 2 : 1) : 0;
        if (this.getBlockState().getValue(HeavyGrudgeBlock.MBS) == target) {
            return false;
        }

        this.level.setBlock(this.worldPosition, this.getBlockState().setValue(HeavyGrudgeBlock.MBS, target), Block.UPDATE_ALL);
        return true;
    }

    private boolean absorbGenericMaterials() {
        for (int slot = LargeShipyardRecipes.SLOT_FUEL; slot < this.shipyardItems.getSlots(); slot++) {
            ItemStack stack = this.shipyardItems.getStackInSlot(slot);
            if (!LargeShipyardRecipes.addMaterialStock(this.matsStock, stack, this.level.random)) {
                continue;
            }

            stack.shrink(1);
            this.shipyardItems.setStackInSlot(slot, stack.isEmpty() ? ItemStack.EMPTY : stack);
            return true;
        }

        return false;
    }

    private boolean releaseGenericMaterials() {
        int material = Mth.clamp(this.selectMat, 0, 3);
        int compressedAmount = LargeShipyardRecipes.compressedOutputAmount();
        int singleAmount = LargeShipyardRecipes.singleOutputAmount();
        if (this.matsStock[material] >= compressedAmount
                && LargeShipyardRecipes.outputMaterialToSlot(this.shipyardItems, material, true)) {
            this.matsStock[material] -= compressedAmount;
            return true;
        }

        if (this.matsStock[material] >= singleAmount
                && LargeShipyardRecipes.outputMaterialToSlot(this.shipyardItems, material, false)) {
            this.matsStock[material] -= singleAmount;
            return true;
        }

        return false;
    }

    private boolean refuelFromInventory() {
        if (this.powerRemained >= powerMax()) {
            return false;
        }

        for (int slot = LargeShipyardRecipes.SLOT_FUEL; slot < this.shipyardItems.getSlots(); slot++) {
            ItemStack fuelStack = this.shipyardItems.getStackInSlot(slot);
            if (fuelStack.isEmpty() || fuelStack.is(ModItems.INSTANTCONMAT.get())) {
                continue;
            }

            var fuelUse = SmallShipyardRecipes.consumeFuelItem(fuelStack);
            if (fuelUse.isEmpty() || fuelUse.get().power() <= 0) {
                continue;
            }

            int fuelValue = (int) (fuelUse.get().power() * fuelMagnification());
            if (fuelValue <= 0 || fuelValue + this.powerRemained >= powerMax()) continue;
            this.powerRemained += fuelValue;
            this.shipyardItems.setStackInSlot(slot, fuelUse.get().remainder().copy());
            return true;
        }

        return false;
    }

    private boolean refuelFromLavaTank() {
        if (this.lavaTank.getFluidAmount() < LAVA_TRANSFER_AMOUNT || powerMax() - this.powerRemained < 800) {
            return false;
        }

        FluidStack drained = this.lavaTank.drain(new FluidStack(Fluids.LAVA, LAVA_TRANSFER_AMOUNT), IFluidHandler.FluidAction.EXECUTE);
        if (drained.isEmpty() || drained.getAmount() < LAVA_TRANSFER_AMOUNT) {
            return false;
        }

        this.powerRemained = this.powerRemained + (int) (800 * fuelMagnification());
        return true;
    }

    private boolean calcPowerGoal() {
        int oldGoal = this.powerGoal;
        this.powerGoal = this.buildType == ShipyardBuildTypes.NONE ? 0 : LargeShipyardRecipes.calcGoalPower(this.matsBuild);
        return oldGoal != this.powerGoal;
    }

    private boolean consumeInstantConstruction() {
        for (int slot = LargeShipyardRecipes.SLOT_FUEL; slot < this.shipyardItems.getSlots(); slot++) {
            ItemStack stack = this.shipyardItems.getStackInSlot(slot);
            if (!stack.is(ModItems.INSTANTCONMAT.get())) {
                continue;
            }

            stack.shrink(1);
            this.powerConsumed += powerInstant();
            this.shipyardItems.setStackInSlot(slot, stack.isEmpty() ? ItemStack.EMPTY : stack);
            return true;
        }

        return false;
    }

    private void finishBuild() {
        RandomSource random = this.level != null ? this.level.getRandom() : RandomSource.create();
        ItemStack result = LargeShipyardRecipes.createBuildResult(this.buildType, this.matsBuild, random);
        this.shipyardItems.setStackInSlot(LargeShipyardRecipes.SLOT_OUTPUT, result);
    }

    private void setRepeatBuild() {
        for (int i = 0; i < this.matsBuild.length; i++) {
            if (this.matsStock[i] >= this.matsBuild[i]) {
                this.matsStock[i] -= this.matsBuild[i];
            } else {
                this.matsBuild[i] = 0;
                this.buildType = ShipyardBuildTypes.NONE;
            }
        }
    }

    public void dropContents() {
        if (this.level == null || this.level.isClientSide()) {
            return;
        }

        for (int slot = 0; slot < this.shipyardItems.getSlots(); slot++) {
            ItemStack stack = this.shipyardItems.getStackInSlot(slot);
            if (!stack.isEmpty()) {
                Block.popResource(this.level, this.worldPosition, stack.copy());
                this.shipyardItems.setStackInSlot(slot, ItemStack.EMPTY);
            }
        }

        RandomSource random = this.level.getRandom();
        ItemEntity blockDrop = new ItemEntity(this.level,
                this.worldPosition.getX() + random.nextFloat() * 0.5F + 0.2F,
                this.worldPosition.getY() + random.nextFloat() * 0.5F + 0.2F,
                this.worldPosition.getZ() + random.nextFloat() * 0.5F + 0.2F,
                this.createDroppedBlockStack());
        blockDrop.setDefaultPickUpDelay();
        this.level.addFreshEntity(LegacyEntityItemMarker.mark(blockDrop));
    }

    public ItemStack createDroppedBlockStack() {
        ItemStack stack = new ItemStack(ModBlocks.BLOCK_GRUDGE_HEAVY.get());
        CompoundTag tag = new CompoundTag();
        int[] mats = new int[4];
        for (int i = 0; i < mats.length; i++) {
            mats[i] = this.matsBuild[i] + this.matsStock[i];
        }
        tag.putIntArray(HeavyGrudgeBlockItem.MATS_TAG, mats);
        tag.putInt(HeavyGrudgeBlockItem.FUEL_TAG, this.powerRemained);
        LegacyItemData.setTag(stack, tag);
        return stack;
    }

    public void restoreFromPlacedStack(ItemStack stack) {
        Arrays.fill(this.matsStock, 0);
        this.powerRemained = 0;
        if (!LegacyItemData.hasTag(stack)) {
            this.markUpdated();
            return;
        }

        CompoundTag tag = LegacyItemData.getTag(stack);
        int[] mats = tag.getIntArray(HeavyGrudgeBlockItem.MATS_TAG);
        System.arraycopy(mats, 0, this.matsStock, 0, Math.min(this.matsStock.length, mats.length));
        this.powerRemained = Mth.clamp(tag.getInt(HeavyGrudgeBlockItem.FUEL_TAG), 0, powerMax());
        this.markUpdated();
    }

    public void setOwner(Player player) {
        this.owner = PlayerOwnerData.of(player);
        this.markUpdated();
    }

    public boolean isStructureComplete() {
        return this.structureComplete;
    }

    public boolean isBuilding() {
        return this.structureComplete && this.powerRemained > buildSpeed() && this.canBuild();
    }

    public int getPowerRemained() {
        return this.powerRemained;
    }

    public int getPowerGoal() {
        return this.powerGoal;
    }

    public int getBuildType() {
        return this.buildType;
    }

    public int[] getBuildMaterialAmounts() {
        return Arrays.copyOf(this.matsBuild, this.matsBuild.length);
    }

    public void setMaterialStockForTest(int material, int amount) {
        this.matsStock[Mth.clamp(material, 0, 3)] = Mth.clamp(amount, 0, LargeShipyardRecipes.MAX_STOCK);
        this.markUpdated();
    }

    public void setBuildMaterialForTest(int material, int amount) {
        this.matsBuild[Mth.clamp(material, 0, 3)] = Mth.clamp(amount, 0, 1000);
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

    @Override
    public Component getOwnerLabel() {
        return this.owner == null
                ? Component.translatable("gui.shincolle.waypoint.owner.unassigned")
                : this.owner.displayLabel();
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
        return Arrays.copyOf(this.matsStock, this.matsStock.length);
    }

    @Override
    public int[] getBuildMaterialAmountsView() {
        return Arrays.copyOf(this.matsBuild, this.matsBuild.length);
    }

    @Override
    public int getInvMode() {
        return this.invMode;
    }

    @Override
    public int getSelectMat() {
        return this.selectMat;
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
    public void cycleInventoryMode() {
        this.invMode = this.invMode == 0 ? 1 : 0;
        this.markUpdated();
    }

    @Override
    public void selectMaterial(int material) {
        this.selectMat = Mth.clamp(material, 0, 3);
        this.markUpdated();
    }

    @Override
    public void adjustBuildMaterial(int material, int action) {
        int materialIndex = Mth.clamp(material, 0, 3);
        int amount = switch (action) {
            case 0, 4 -> 1000;
            case 1, 5 -> 100;
            case 2, 6 -> 10;
            case 3, 7 -> 1;
            default -> 0;
        };
        if (amount <= 0) {
            return;
        }

        if (action <= 3) {
            int moved = Math.min(amount, this.matsStock[materialIndex]);
            moved = Math.min(moved, 1000 - this.matsBuild[materialIndex]);
            this.matsStock[materialIndex] -= moved;
            this.matsBuild[materialIndex] += moved;
        } else {
            int moved = Math.min(amount, this.matsBuild[materialIndex]);
            this.matsBuild[materialIndex] -= moved;
            this.matsStock[materialIndex] = Math.min(LargeShipyardRecipes.MAX_STOCK, this.matsStock[materialIndex] + moved);
        }

        this.calcPowerGoal();
        this.markUpdated();
    }

    @Override
    public boolean usesGenericInventory() {
        return true;
    }

    private void setBuildType(int buildType) {
        this.buildType = Mth.clamp(buildType, ShipyardBuildTypes.NONE, ShipyardBuildTypes.EQUIP_LOOP);
        if (!this.canBuild()) {
            this.powerConsumed = 0;
        }

        this.calcPowerGoal();
        this.markUpdated();
    }

    private boolean canBuild() {
        return this.powerGoal > 0
                && this.shipyardItems.getStackInSlot(LargeShipyardRecipes.SLOT_OUTPUT).isEmpty();
    }

    private static int getIntCompat(CompoundTag tag, String currentKey, String legacyKey) {
        return tag.contains(currentKey) ? tag.getInt(currentKey) : tag.getInt(legacyKey);
    }

    private static void readIntArrayCompat(CompoundTag tag, int[] target, String currentKey, String legacyKey) {
        Arrays.fill(target, 0);
        int[] stored = tag.contains(currentKey) ? tag.getIntArray(currentKey) : tag.getIntArray(legacyKey);
        System.arraycopy(stored, 0, target, 0, Math.min(target.length, stored.length));
    }

    private void markUpdated() {
        this.setChanged();

        if (this.level != null) {
            BlockState state = this.getBlockState();
            this.level.sendBlockUpdated(this.worldPosition, state, state, Block.UPDATE_ALL);
        }
    }
}
