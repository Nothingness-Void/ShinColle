package com.lulan.shincolle.blockentity;

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
import net.minecraftforge.items.ItemStackHandler;

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
    private static final int MAX_WAIT_MODE = 24;
    private static final int MAX_THREE_STATE_MODE = 2;
    public static final int FILTER_SLOT_COUNT = 18;

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
    private int waitMode;
    private int redstoneMode;
    private int liquidMode;
    private int energyMode;
    private int filterModeMask;
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
    public void load(CompoundTag tag) {
        super.load(tag);
        this.owner = PlayerOwnerData.load(tag, OWNER_UUID_TAG, OWNER_NAME_TAG, OWNER_UID_TAG);
        this.lastWaypoint = readOptionalPos(tag, LAST_WAYPOINT_TAG);
        this.nextWaypoint = readOptionalPos(tag, NEXT_WAYPOINT_TAG);
        this.pairedChest = readOptionalPos(tag, PAIRED_CHEST_TAG);
        this.loadEnabled = !tag.contains(LOAD_ENABLED_TAG) || tag.getBoolean(LOAD_ENABLED_TAG);
        this.unloadEnabled = !tag.contains(UNLOAD_ENABLED_TAG) || tag.getBoolean(UNLOAD_ENABLED_TAG);
        this.waitMode = Mth.clamp(tag.getInt(WAIT_MODE_TAG), 0, MAX_WAIT_MODE);
        this.redstoneMode = Mth.clamp(tag.getInt(REDSTONE_MODE_TAG), 0, MAX_THREE_STATE_MODE);
        this.liquidMode = Mth.clamp(tag.getInt(LIQUID_MODE_TAG), 0, MAX_THREE_STATE_MODE);
        this.energyMode = Mth.clamp(tag.getInt(ENERGY_MODE_TAG), 0, MAX_THREE_STATE_MODE);
        if (tag.contains(FILTER_ITEMS_TAG, Tag.TAG_COMPOUND)) {
            this.filterItems.deserializeNBT(tag.getCompound(FILTER_ITEMS_TAG));
        }
        this.filterModeMask = tag.getInt(FILTER_MODE_MASK_TAG);
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);

        if (this.owner != null) {
            this.owner.save(tag, OWNER_UUID_TAG, OWNER_NAME_TAG, OWNER_UID_TAG);
        }

        writeOptionalPos(tag, LAST_WAYPOINT_TAG, this.lastWaypoint);
        writeOptionalPos(tag, NEXT_WAYPOINT_TAG, this.nextWaypoint);
        writeOptionalPos(tag, PAIRED_CHEST_TAG, this.pairedChest);
        tag.putBoolean(LOAD_ENABLED_TAG, this.loadEnabled);
        tag.putBoolean(UNLOAD_ENABLED_TAG, this.unloadEnabled);
        tag.putInt(WAIT_MODE_TAG, this.waitMode);
        tag.putInt(REDSTONE_MODE_TAG, this.redstoneMode);
        tag.putInt(LIQUID_MODE_TAG, this.liquidMode);
        tag.putInt(ENERGY_MODE_TAG, this.energyMode);
        tag.put(FILTER_ITEMS_TAG, this.filterItems.serializeNBT());
        tag.putInt(FILTER_MODE_MASK_TAG, this.filterModeMask);
    }

    @Override
    public CompoundTag getUpdateTag() {
        return this.saveWithoutMetadata();
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    public void setOwner(Player player) {
        this.owner = PlayerOwnerData.of(player);
        this.markUpdated();
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

    public void cycleWaitMode() {
        this.waitMode = (this.waitMode + 1) % (MAX_WAIT_MODE + 1);
        this.markUpdated();
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
        if (!tag.contains(key, Tag.TAG_COMPOUND)) {
            return null;
        }

        return NbtUtils.readBlockPos(tag.getCompound(key));
    }

    private static void writeOptionalPos(CompoundTag tag, String key, @Nullable BlockPos pos) {
        if (pos != null) {
            tag.put(key, NbtUtils.writeBlockPos(pos));
        }
    }
}
