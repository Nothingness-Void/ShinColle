package com.lulan.shincolle.blockentity;

import com.lulan.shincolle.config.ShinColleConfig;
import com.lulan.shincolle.entity.ship.LegacyShipEntity;
import com.lulan.shincolle.entity.mount.LegacyShipMountEntity;
import com.lulan.shincolle.entity.ship.LegacyShipAircraftEntity;
import com.lulan.shincolle.entity.projectile.LegacyShipFloatingFortEntity;
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
import net.minecraft.world.entity.Entity;
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
import net.neoforged.neoforge.capabilities.BlockCapability;
import java.util.Optional;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;

import javax.annotation.Nullable;

public class LegacyCoreBlockEntity extends BlockEntity implements LegacyCoreAccess, MenuProvider {

    private static final String OWNER_NAME_TAG = "OwnerName";
    private static final String OWNER_UUID_TAG = "OwnerUuid";
    private static final String OWNER_UID_TAG = "OwnerUID";
    private static final String MODE_TAG = "Mode";
    private static final String CHARGE_TAG = "Charge";
    private static final String ACTIVE_TAG = "active";
    private static final String POWER_TAG = "power";
    private static final String FUEL_ITEMS_TAG = "FuelItems";

    private static final int MODE_IDLE = 0;
    private static final int MODE_ACTIVE = 1;
    private static final int MAX_MODE = MODE_ACTIVE;
    private static final int FUEL_SLOT_COUNT = 9;
    private static final double VOLCORE_AURA_RADIUS = 6.0D;
    private static final int VOLCORE_EFFECT_INTERVAL = 32;

    private final String blockNameKey;
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
    private int syncTick;
    private boolean canWork;
    private boolean volCoreWet;
    private final ContainerData coreData = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> LegacyCoreBlockEntity.this.mode;
                case 1 -> LegacyCoreBlockEntity.this.storedCharge;
                case 2 -> LegacyCoreBlockEntity.this.maxCharge();
                case 3 -> LegacyCoreBlockEntity.this.volCoreWet ? 1 : 0;
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
            switch (index) {
                case 0 -> LegacyCoreBlockEntity.this.mode = Mth.clamp(value, MODE_IDLE, MAX_MODE);
                case 1 -> LegacyCoreBlockEntity.this.storedCharge = Mth.clamp(value, 0, LegacyCoreBlockEntity.this.maxCharge());
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

    private LegacyCoreBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state, String blockNameKey) {
        super(type, pos, state);
        this.blockNameKey = blockNameKey;
    }

    public static LegacyCoreBlockEntity newVolCore(BlockPos pos, BlockState state) {
        return new LegacyCoreBlockEntity(ModBlockEntities.VOL_CORE.get(), pos, state, "block.shincolle.blockvolcore");
    }



    public <T> Optional<T> getCapability(BlockCapability<T, net.minecraft.core.Direction> capability) {
        return getCapability(capability, null);
    }

    public <T> Optional<T> getCapability(BlockCapability<T, net.minecraft.core.Direction> capability, @Nullable net.minecraft.core.Direction side) {
        if (capability == Capabilities.ItemHandler.BLOCK) {
            return Optional.of(capability.typeClass().cast(this.fuelItems));
        }

        return Optional.empty();
    }

    @Override
    protected void loadAdditional(CompoundTag tag, net.minecraft.core.HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        com.lulan.shincolle.item.LegacyItemData.upgradeNested(registries, tag);
        this.owner = PlayerOwnerData.load(tag, OWNER_UUID_TAG, OWNER_NAME_TAG, OWNER_UID_TAG);
        this.mode = tag.contains(ACTIVE_TAG)
                ? (tag.getBoolean(ACTIVE_TAG) ? MODE_ACTIVE : MODE_IDLE)
                : Mth.clamp(tag.getInt(MODE_TAG), MODE_IDLE, MAX_MODE);
        this.storedCharge = Mth.clamp(tag.contains(POWER_TAG) ? tag.getInt(POWER_TAG) : tag.getInt(CHARGE_TAG), 0, this.maxCharge());
        if (tag.contains(FUEL_ITEMS_TAG)) {
            this.fuelItems.deserializeNBT(registries, tag.getCompound(FUEL_ITEMS_TAG));
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag, net.minecraft.core.HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        if (this.owner != null) {
            this.owner.save(tag, OWNER_UUID_TAG, OWNER_NAME_TAG, OWNER_UID_TAG);
        }
        tag.putBoolean(ACTIVE_TAG, this.mode == MODE_ACTIVE);
        tag.putInt(POWER_TAG, this.storedCharge);
        tag.put(FUEL_ITEMS_TAG, this.fuelItems.serializeNBT(registries));
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

    public boolean canDestroy(Player player) {
        return player.isCreative() || player.hasPermissions(2)
                || (this.owner != null && (this.owner.uuid() != null ? this.owner.uuid().equals(player.getUUID())
                : this.owner.uid() == com.lulan.shincolle.teitoku.TeitokuHelper.getPlayerUid(player)));
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
        return this.mode == MODE_ACTIVE
                ? Component.translatable("gui.shincolle.legacy_core.mode.active")
                : Component.translatable("gui.shincolle.legacy_core.mode.idle");
    }

    public Component getBlockLabel() {
        return Component.translatable(this.blockNameKey);
    }

    public Component getChargeLabel() {
        return Component.literal(this.storedCharge + " / " + this.maxCharge());
    }

    public int getStoredCharge() {
        return this.storedCharge;
    }

    public boolean canProvideCharge() {
        return this.storedCharge > 0;
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
            if (!current.isEmpty() && (!net.minecraft.world.item.ItemStack.isSameItemSameComponents(current, stack)
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
        core.syncTick++;
        if (core.isVolCore()) {
            boolean wet = core.hasNearbyFluid(level);
            if (wet != core.volCoreWet) {
                core.volCoreWet = wet;
                changed = true;
            }
        }

        if (core.syncTick % 32 == 0) {
            changed |= core.refuelFromInventory();
        }

        if (core.syncTick % 16 == 0) {
            boolean canWork = core.storedCharge >= (int) ShinColleConfig.tileVolCore()[1];
            if (canWork != core.canWork) {
                core.canWork = canWork;
                changed = true;
            }
            if (core.mode == MODE_ACTIVE && core.canWork) {
                core.storedCharge = Math.max(0, core.storedCharge - (int) ShinColleConfig.tileVolCore()[1]);
                changed = true;

                if (core.isVolCore() && core.syncTick % VOLCORE_EFFECT_INTERVAL == 0) {
                    changed |= core.tickVolCoreAura(level);
                }
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
        if (this.storedCharge >= this.maxCharge()) {
            return false;
        }

        for (int slot = 0; slot < this.fuelItems.getSlots(); slot++) {
            net.minecraft.world.item.ItemStack stack = this.fuelItems.getStackInSlot(slot);
            int fuelValue = this.getFuelValue(stack);
            if (fuelValue <= 0 || fuelValue + this.storedCharge >= this.maxCharge()) {
                continue;
            }

            this.storedCharge += fuelValue;
            stack.shrink(1);
            this.fuelItems.setStackInSlot(slot, stack.isEmpty() ? net.minecraft.world.item.ItemStack.EMPTY : stack);
            return true;
        }

        return false;
    }

    private boolean tickVolCoreAura(Level level) {
        AABB area = AABB.ofSize(net.minecraft.world.phys.Vec3.atCenterOf(this.worldPosition),
                VOLCORE_AURA_RADIUS * 2, VOLCORE_AURA_RADIUS * 2, VOLCORE_AURA_RADIUS * 2);
        if (this.hasNearbyFluid(level)) {
            boolean affected = false;
            for (LegacyShipEntity ship : level.getEntitiesOfClass(LegacyShipEntity.class, area)) {
                BlockPos feet = new BlockPos(Mth.floor(ship.getX()), (int) ship.getBoundingBox().minY,
                        Mth.floor(ship.getZ()));
                if (ship.isHostileVariant() || !ship.isLegacyOutOfCombat()
                        || !(level.getBlockState(feet).liquid()
                        || level.getBlockState(feet).getBlock() instanceof net.minecraft.world.level.block.LiquidBlock)) {
                    continue;
                }

                if (ship.getHealth() < ship.getMaxHealth()) {
                    ship.heal(Math.max(4.0F, ship.getMaxHealth() * 0.01F + 4.0F));
                    affected = true;
                }

                if (ship.getMorale() < (int) (5100 * 1.8F)) {
                    ship.addMorale(80);
                    affected = true;
                }
            }

            return affected;
        }

        boolean affected = false;
        for (Entity candidate : level.getEntitiesOfClass(Entity.class, area,
                entity -> entity instanceof LivingEntity || entity instanceof LegacyShipMountEntity
                        || entity instanceof LegacyShipAircraftEntity || entity instanceof LegacyShipFloatingFortEntity)) {
            if (candidate instanceof LegacyShipEntity || candidate instanceof LegacyShipMountEntity
                    || candidate instanceof LegacyShipAircraftEntity || candidate instanceof LegacyShipFloatingFortEntity) {
                return affected;
            }
            LivingEntity entity = (LivingEntity) candidate;
            entity.igniteForSeconds(2);
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

    private int maxCharge() { return (int) ShinColleConfig.tileVolCore()[0]; }

    private int getFuelValue(net.minecraft.world.item.ItemStack stack) {
        if (stack.isEmpty()) {
            return 0;
        }

        // 1.12.2 TileEntityVolCore: FUELMAGN per grudge, FUELMAGN * 9 per grudge block.
        int fuelPerGrudge = (int) ShinColleConfig.tileVolCore()[2];
        if (stack.is(ModBlocks.BLOCK_GRUDGE.get().asItem())) {
            return fuelPerGrudge * 9;
        }
        if (stack.is(ModItems.GRUDGE1.get())) {
            return fuelPerGrudge;
        }
        if (stack.is(ModItems.GRUDGE.get())) {
            return fuelPerGrudge;
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
