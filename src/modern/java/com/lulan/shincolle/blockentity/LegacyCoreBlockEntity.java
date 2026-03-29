package com.lulan.shincolle.blockentity;

import com.lulan.shincolle.entity.ship.LegacyShipEntity;
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
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nullable;

public class LegacyCoreBlockEntity extends BlockEntity implements LegacyCoreAccess, RouteEnergyAccess, MenuProvider {

    private static final String OWNER_NAME_TAG = "OwnerName";
    private static final String OWNER_UUID_TAG = "OwnerUuid";
    private static final String OWNER_UID_TAG = "OwnerUID";
    private static final String MODE_TAG = "Mode";
    private static final String CHARGE_TAG = "Charge";
    private static final String FUEL_ITEMS_TAG = "FuelItems";

    private static final int MODE_IDLE = 0;
    private static final int MODE_CHARGE = 1;
    private static final int MODE_DRAIN = 2;
    private static final int MAX_MODE = MODE_DRAIN;
    private static final int FUEL_SLOT_COUNT = 9;
    private static final double VOLCORE_AURA_RADIUS = 6.0D;
    private static final int VOLCORE_EFFECT_INTERVAL = 32;

    private final String blockNameKey;
    private final int chargePerTick;
    private final int maxCharge;
    private final ItemStackHandler fuelItems = new ItemStackHandler(FUEL_SLOT_COUNT) {
        @Override
        protected void onContentsChanged(int slot) {
            LegacyCoreBlockEntity.this.markUpdated();
        }
    };

    @Nullable
    private PlayerOwnerData owner;
    private int mode;
    private int storedCharge;
    private boolean volCoreWet;
    private LazyOptional<IItemHandler> itemHandler = LazyOptional.empty();
    private final ContainerData coreData = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> LegacyCoreBlockEntity.this.mode;
                case 1 -> LegacyCoreBlockEntity.this.storedCharge;
                case 2 -> LegacyCoreBlockEntity.this.maxCharge;
                case 3 -> LegacyCoreBlockEntity.this.volCoreWet ? 1 : 0;
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
            switch (index) {
                case 0 -> LegacyCoreBlockEntity.this.mode = Mth.clamp(value, MODE_IDLE, MAX_MODE);
                case 1 -> LegacyCoreBlockEntity.this.storedCharge = Mth.clamp(value, 0, LegacyCoreBlockEntity.this.maxCharge);
                case 3 -> LegacyCoreBlockEntity.this.volCoreWet = value != 0;
                default -> {
                }
            }
        }

        @Override
        public int getCount() {
            return 4;
        }
    };

    private LegacyCoreBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state, String blockNameKey, int chargePerTick, int maxCharge) {
        super(type, pos, state);
        this.blockNameKey = blockNameKey;
        this.chargePerTick = chargePerTick;
        this.maxCharge = maxCharge;
    }

    public static LegacyCoreBlockEntity newVolCore(BlockPos pos, BlockState state) {
        return new LegacyCoreBlockEntity(ModBlockEntities.VOL_CORE.get(), pos, state, "block.shincolle.blockvolcore", 8, 24000);
    }

    public static LegacyCoreBlockEntity newPolymetal(BlockPos pos, BlockState state) {
        return new LegacyCoreBlockEntity(ModBlockEntities.POLYMETAL.get(), pos, state, "block.shincolle.blockpolymetal", 5, 16000);
    }

    public static LegacyCoreBlockEntity newGrudgeHeavy(BlockPos pos, BlockState state) {
        return new LegacyCoreBlockEntity(ModBlockEntities.GRUDGE_HEAVY.get(), pos, state, "block.shincolle.blockgrudgeheavy", 6, 20000);
    }

    @Override
    public void onLoad() {
        super.onLoad();
        this.itemHandler = LazyOptional.of(() -> this.fuelItems);
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
        this.mode = Mth.clamp(tag.getInt(MODE_TAG), MODE_IDLE, MAX_MODE);
        this.storedCharge = Mth.clamp(tag.getInt(CHARGE_TAG), 0, this.maxCharge);
        if (tag.contains(FUEL_ITEMS_TAG)) {
            this.fuelItems.deserializeNBT(tag.getCompound(FUEL_ITEMS_TAG));
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        if (this.owner != null) {
            this.owner.save(tag, OWNER_UUID_TAG, OWNER_NAME_TAG, OWNER_UID_TAG);
        }
        tag.putInt(MODE_TAG, this.mode);
        tag.putInt(CHARGE_TAG, this.storedCharge);
        tag.put(FUEL_ITEMS_TAG, this.fuelItems.serializeNBT());
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
        return this.getBlockLabel();
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
        return new LegacyCoreMenu(containerId, inventory, this);
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

    public int cycleMode() {
        this.mode = (this.mode + 1) % (MAX_MODE + 1);
        this.markUpdated();
        return this.mode;
    }

    public Component getModeName() {
        return switch (this.mode) {
            case MODE_CHARGE -> Component.translatable("gui.shincolle.legacy_core.mode.charge");
            case MODE_DRAIN -> Component.translatable("gui.shincolle.legacy_core.mode.drain");
            default -> Component.translatable("gui.shincolle.legacy_core.mode.idle");
        };
    }

    public Component getBlockLabel() {
        return Component.translatable(this.blockNameKey);
    }

    public Component getChargeLabel() {
        return Component.literal(this.storedCharge + " / " + this.maxCharge);
    }

    public int getStoredCharge() {
        return this.storedCharge;
    }

    @Override
    public int getRouteEnergyStored() {
        return this.storedCharge;
    }

    @Override
    public int getRouteEnergyCapacity() {
        return this.maxCharge;
    }

    public boolean canProvideCharge() {
        return this.mode == MODE_DRAIN && this.storedCharge > 0;
    }

    @Override
    public int extractRouteEnergy(int amount, boolean simulate) {
        if (!this.canProvideCharge()) {
            return 0;
        }

        int extracted = Math.min(Math.max(0, amount), this.storedCharge);
        if (extracted > 0 && !simulate) {
            this.storedCharge -= extracted;
            this.markUpdated();
        }
        return extracted;
    }

    @Override
    public int receiveRouteEnergy(int amount, boolean simulate) {
        int accepted = Math.min(Math.max(0, amount), this.maxCharge - this.storedCharge);
        if (accepted > 0 && !simulate) {
            this.storedCharge += accepted;
            this.markUpdated();
        }
        return accepted;
    }

    public Component getFuelLabel() {
        return Component.literal(this.getFuelItemCount() + " / " + FUEL_SLOT_COUNT);
    }

    public int getFuelItemCount() {
        int total = 0;

        for (int slot = 0; slot < this.fuelItems.getSlots(); slot++) {
            total += this.fuelItems.getStackInSlot(slot).getCount();
        }

        return total;
    }

    @Override
    public boolean isVolCore() {
        return "block.shincolle.blockvolcore".equals(this.blockNameKey);
    }

    public boolean isFuelItem(net.minecraft.world.item.ItemStack stack) {
        return this.getFuelValue(stack) > 0;
    }

    @Override
    public ItemStackHandler getFuelItems() {
        return this.fuelItems;
    }

    @Override
    public ContainerData getCoreContainerData() {
        return this.coreData;
    }

    @Override
    public void cycleCoreMode() {
        this.cycleMode();
    }

    public int insertFuel(net.minecraft.world.item.ItemStack stack) {
        if (stack.isEmpty() || !this.isFuelItem(stack)) {
            return 0;
        }

        int inserted = 0;
        int remaining = stack.getCount();

        for (int slot = 0; slot < this.fuelItems.getSlots() && remaining > 0; slot++) {
            net.minecraft.world.item.ItemStack current = this.fuelItems.getStackInSlot(slot);
            if (!current.isEmpty() && (!net.minecraft.world.item.ItemStack.isSameItemSameTags(current, stack)
                    || current.getCount() >= current.getMaxStackSize())) {
                continue;
            }

            if (current.isEmpty()) {
                net.minecraft.world.item.ItemStack copy = stack.copy();
                copy.setCount(Math.min(copy.getMaxStackSize(), remaining));
                this.fuelItems.setStackInSlot(slot, copy);
                inserted += copy.getCount();
                remaining -= copy.getCount();
                continue;
            }

            int moved = Math.min(remaining, current.getMaxStackSize() - current.getCount());
            current.grow(moved);
            this.fuelItems.setStackInSlot(slot, current);
            inserted += moved;
            remaining -= moved;
        }

        return inserted;
    }

    public int consumeCharge(int request) {
        int amount = Math.min(Math.max(0, request), this.storedCharge);
        if (amount <= 0) {
            return 0;
        }

        this.storedCharge -= amount;
        this.markUpdated();
        return amount;
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, LegacyCoreBlockEntity core) {
        if (level.isClientSide()) {
            return;
        }

        boolean changed = false;
        if (core.isVolCore()) {
            boolean wet = core.hasNearbyFluid(level);
            if (wet != core.volCoreWet) {
                core.volCoreWet = wet;
                changed = true;
            }
        }

        if (core.mode != MODE_IDLE) {
            changed |= core.refuelFromInventory();
        }

        if (core.mode == MODE_DRAIN && core.storedCharge >= core.chargePerTick) {
            core.storedCharge = Math.max(0, core.storedCharge - core.chargePerTick);
            changed = true;

            if (core.isVolCore() && level.getGameTime() % VOLCORE_EFFECT_INTERVAL == 0) {
                changed |= core.tickVolCoreAura(level);
            }
        }

        if (changed) {
            core.markUpdated();
        }
    }

    public void dropContents() {
        if (this.level == null || this.level.isClientSide()) {
            return;
        }

        for (int slot = 0; slot < this.fuelItems.getSlots(); slot++) {
            net.minecraft.world.item.ItemStack stack = this.fuelItems.getStackInSlot(slot);
            if (!stack.isEmpty()) {
                Block.popResource(this.level, this.worldPosition, stack.copy());
                this.fuelItems.setStackInSlot(slot, net.minecraft.world.item.ItemStack.EMPTY);
            }
        }
    }

    private boolean refuelFromInventory() {
        if (this.storedCharge >= this.maxCharge) {
            return false;
        }

        for (int slot = 0; slot < this.fuelItems.getSlots(); slot++) {
            net.minecraft.world.item.ItemStack stack = this.fuelItems.getStackInSlot(slot);
            int fuelValue = this.getFuelValue(stack);
            if (fuelValue <= 0) {
                continue;
            }

            this.storedCharge = Math.min(this.maxCharge, this.storedCharge + fuelValue);
            stack.shrink(1);
            this.fuelItems.setStackInSlot(slot, stack.isEmpty() ? net.minecraft.world.item.ItemStack.EMPTY : stack);
            return true;
        }

        return false;
    }

    private boolean tickVolCoreAura(Level level) {
        AABB area = new AABB(this.worldPosition).inflate(VOLCORE_AURA_RADIUS);
        if (this.hasNearbyFluid(level)) {
            boolean affected = false;
            for (LegacyShipEntity ship : level.getEntitiesOfClass(LegacyShipEntity.class, area)) {
                if (!ship.isAlive() || ship.isHostileVariant() || !ship.isInWaterRainOrBubble()) {
                    continue;
                }

                if (ship.getHealth() < ship.getMaxHealth()) {
                    ship.heal(Math.max(4.0F, ship.getMaxHealth() * 0.01F + 4.0F));
                    affected = true;
                }

                ship.addMorale(80);
                affected = true;
            }

            return affected;
        }

        boolean affected = false;
        for (LivingEntity entity : level.getEntitiesOfClass(LivingEntity.class, area)) {
            if (entity instanceof LegacyShipEntity || entity instanceof Player || !entity.isAlive()) {
                continue;
            }

            entity.setSecondsOnFire(2);
            entity.hurt(level.damageSources().inFire(), 4.0F);
            affected = true;
        }

        return affected;
    }

    private boolean hasNearbyFluid(Level level) {
        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                for (int dz = -1; dz <= 1; dz++) {
                    if (!level.getFluidState(this.worldPosition.offset(dx, dy, dz)).isEmpty()) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    private int getFuelValue(net.minecraft.world.item.ItemStack stack) {
        if (stack.isEmpty()) {
            return 0;
        }

        if (stack.is(ModBlocks.BLOCK_GRUDGE.get().asItem())) {
            return Math.max(this.chargePerTick * 2700 / 10, this.maxCharge * 9 / 10);
        }
        if (stack.is(ModItems.GRUDGE1.get())) {
            return Math.max(this.chargePerTick * 600 / 10, this.maxCharge / 4);
        }
        if (stack.is(ModItems.GRUDGE.get())) {
            return Math.max(this.chargePerTick * 300, this.maxCharge / 10);
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
