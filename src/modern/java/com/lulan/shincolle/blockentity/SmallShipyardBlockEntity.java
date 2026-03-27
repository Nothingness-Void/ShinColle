package com.lulan.shincolle.blockentity;

import com.lulan.shincolle.block.SmallShipyardBlock;
import com.lulan.shincolle.crafting.SmallShipyardRecipes;
import com.lulan.shincolle.crafting.ShipyardBuildTypes;
import com.lulan.shincolle.menu.SmallShipyardMenu;
import com.lulan.shincolle.ownership.PlayerOwnerData;
import com.lulan.shincolle.registry.ModBlockEntities;
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

public class SmallShipyardBlockEntity extends BlockEntity implements MenuProvider {

    private static final String OWNER_NAME_TAG = "OwnerName";
    private static final String OWNER_UUID_TAG = "OwnerUuid";
    private static final String OWNER_UID_TAG = "OwnerUID";
    private static final String INVENTORY_TAG = "Inventory";
    private static final String POWER_CONSUMED_TAG = "ConsumedPower";
    private static final String POWER_REMAINED_TAG = "RemainedPower";
    private static final String POWER_GOAL_TAG = "GoalPower";
    private static final String BUILD_TYPE_TAG = "BuildType";
    private static final String BUILD_RECORD_TAG = "BuildRecord";
    public static final int POWER_MAX = 460800;
    public static final int BUILD_SPEED = 48;
    public static final float FUEL_MAGNIFICATION = 1.0F;
    public static final int POWER_INSTANT = BUILD_SPEED * 2400;

    @Nullable
    private PlayerOwnerData owner;
    private final ItemStackHandler items = new ItemStackHandler(SmallShipyardRecipes.SLOT_COUNT) {
        @Override
        protected void onContentsChanged(int slot) {
            SmallShipyardBlockEntity.this.setChanged();
        }
    };
    private final ContainerData dataAccess = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> SmallShipyardBlockEntity.this.buildType;
                case 1 -> SmallShipyardBlockEntity.this.powerConsumed;
                case 2 -> SmallShipyardBlockEntity.this.powerRemained;
                case 3 -> SmallShipyardBlockEntity.this.powerGoal;
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
            switch (index) {
                case 0 -> SmallShipyardBlockEntity.this.buildType = value;
                case 1 -> SmallShipyardBlockEntity.this.powerConsumed = value;
                case 2 -> SmallShipyardBlockEntity.this.powerRemained = value;
                case 3 -> SmallShipyardBlockEntity.this.powerGoal = value;
                default -> {
                }
            }
        }

        @Override
        public int getCount() {
            return 4;
        }
    };
    private LazyOptional<IItemHandler> itemHandler = LazyOptional.empty();
    private int powerConsumed;
    private int powerRemained;
    private int powerGoal;
    private int buildType;
    private final int[] buildRecord = new int[4];

    public SmallShipyardBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.SMALL_SHIPYARD.get(), pos, state);
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
        return Component.translatable("block.shincolle.blocksmallshipyard");
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
        return new SmallShipyardMenu(containerId, inventory, this);
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

    public int getOwnerUid() {
        return this.owner == null ? 0 : this.owner.uid();
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

    public int getPowerGoal() {
        return this.powerGoal;
    }

    public boolean hasRemainedPower() {
        return this.powerRemained > BUILD_SPEED;
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
            int[] materials = SmallShipyardRecipes.getMaterialAmounts(this.items);
            System.arraycopy(materials, 0, this.buildRecord, 0, this.buildRecord.length);
        }

        if (!this.canBuild()) {
            this.powerConsumed = 0;
        }

        this.calcPowerGoal();
        this.markUpdated();
    }

    public boolean canBuild() {
        if (this.powerGoal <= 0 || !this.items.getStackInSlot(SmallShipyardRecipes.SLOT_OUTPUT).isEmpty()) {
            return false;
        }

        if (ShipyardBuildTypes.isLoopMode(this.buildType)) {
            if (!SmallShipyardRecipes.canRecipeBuild(this.buildRecord)) {
                return false;
            }

            for (int slot = 0; slot < 4; slot++) {
                ItemStack stack = this.items.getStackInSlot(slot);
                if (stack.isEmpty() || stack.getCount() < this.buildRecord[slot]) {
                    return false;
                }
            }
        }

        return ShipyardBuildTypes.isShipMode(this.buildType) || ShipyardBuildTypes.isEquipMode(this.buildType);
    }

    private void calcPowerGoal() {
        if (this.buildType == ShipyardBuildTypes.NONE) {
            this.powerGoal = 0;
            return;
        }

        int[] materials = ShipyardBuildTypes.isLoopMode(this.buildType)
                ? this.buildRecord
                : SmallShipyardRecipes.getMaterialAmounts(this.items);
        this.powerGoal = SmallShipyardRecipes.calcGoalPower(materials);
    }

    private boolean isBuilding() {
        return this.hasRemainedPower() && this.canBuild();
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, SmallShipyardBlockEntity shipyard) {
        shipyard.tickServer();
    }

    private void tickServer() {
        boolean changed = false;
        boolean activeBefore = this.getBlockState().getValue(SmallShipyardBlock.ACTIVE);

        this.calcPowerGoal();
        changed |= this.refuelFromFuelSlot();

        if (this.isBuilding()) {
            changed |= this.consumeInstantConstruction();
            this.powerRemained -= BUILD_SPEED;
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
            this.level.setBlock(this.worldPosition, this.getBlockState().setValue(SmallShipyardBlock.ACTIVE, activeNow), Block.UPDATE_ALL);
            changed = true;
        }

        if (changed) {
            this.setChanged();
        }
    }

    private boolean refuelFromFuelSlot() {
        ItemStack fuelStack = this.items.getStackInSlot(SmallShipyardRecipes.SLOT_FUEL);
        if (fuelStack.isEmpty() || fuelStack.is(ModItems.INSTANTCONMAT.get()) || this.powerRemained >= POWER_MAX) {
            return false;
        }

        int fuelValue = Math.round(SmallShipyardRecipes.getFuelValue(fuelStack) * FUEL_MAGNIFICATION);
        if (fuelValue <= 0 || fuelValue + this.powerRemained >= POWER_MAX) {
            return false;
        }

        ItemStack remainder = fuelStack.hasCraftingRemainingItem() ? fuelStack.getCraftingRemainingItem() : ItemStack.EMPTY;
        if (!remainder.isEmpty() && fuelStack.getCount() > 1) {
            return false;
        }

        this.powerRemained += fuelValue;
        if (remainder.isEmpty()) {
            fuelStack.shrink(1);
            if (fuelStack.isEmpty()) {
                this.items.setStackInSlot(SmallShipyardRecipes.SLOT_FUEL, ItemStack.EMPTY);
            }
        } else {
            this.items.setStackInSlot(SmallShipyardRecipes.SLOT_FUEL, remainder.copy());
        }

        return true;
    }

    private boolean consumeInstantConstruction() {
        ItemStack fuelStack = this.items.getStackInSlot(SmallShipyardRecipes.SLOT_FUEL);
        if (!fuelStack.is(ModItems.INSTANTCONMAT.get())) {
            return false;
        }

        fuelStack.shrink(1);
        this.powerConsumed += POWER_INSTANT;
        if (fuelStack.isEmpty()) {
            this.items.setStackInSlot(SmallShipyardRecipes.SLOT_FUEL, ItemStack.EMPTY);
        }

        return true;
    }

    private void finishBuild() {
        int[] materials;
        if (ShipyardBuildTypes.isLoopMode(this.buildType)) {
            materials = Arrays.copyOf(this.buildRecord, this.buildRecord.length);
            for (int slot = 0; slot < 4; slot++) {
                ItemStack stack = this.items.getStackInSlot(slot);
                stack.shrink(this.buildRecord[slot]);
                if (stack.isEmpty()) {
                    this.items.setStackInSlot(slot, ItemStack.EMPTY);
                }
            }
        } else {
            materials = SmallShipyardRecipes.getMaterialAmounts(this.items);
            for (int slot = 0; slot < 4; slot++) {
                this.items.setStackInSlot(slot, ItemStack.EMPTY);
            }
        }

        RandomSource random = this.level != null ? this.level.getRandom() : RandomSource.create();
        ItemStack result = SmallShipyardRecipes.createBuildResult(this.buildType, materials, random);
        this.items.setStackInSlot(SmallShipyardRecipes.SLOT_OUTPUT, result);
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
