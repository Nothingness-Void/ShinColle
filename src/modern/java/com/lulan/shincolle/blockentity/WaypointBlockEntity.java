package com.lulan.shincolle.blockentity;

import com.lulan.shincolle.registry.ModBlockEntities;
import com.lulan.shincolle.client.WaypointClientEffects;
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
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;

public class WaypointBlockEntity extends BlockEntity implements RouteNode {

    private static final String OWNER_NAME_TAG = "OwnerName";
    private static final String OWNER_UUID_TAG = "OwnerUuid";
    private static final String OWNER_UID_TAG = "OwnerUID";
    private static final String STAY_MODE_TAG = "StayMode";
    private static final String LAST_WAYPOINT_TAG = "LastWaypoint";
    private static final String NEXT_WAYPOINT_TAG = "NextWaypoint";
    private static final String PAIRED_CHEST_TAG = "PairedChest";
    private static final String LEGACY_OWNER_UID_TAG = "pid";
    private static final String LEGACY_STAY_MODE_TAG = "wpstay";
    private static final String LEGACY_LAST_WAYPOINT_TAG = "lastPos";
    private static final String LEGACY_NEXT_WAYPOINT_TAG = "nextPos";
    private static final String LEGACY_PAIRED_CHEST_TAG = "chestPos";
    private static final int MAX_STAY_MODE = 16;

    @Nullable
    private PlayerOwnerData owner;
    private int stayMode;
    private int clientTicks;
    @Nullable
    private BlockPos lastWaypoint;
    @Nullable
    private BlockPos nextWaypoint;
    @Nullable
    private BlockPos pairedChest;

    public WaypointBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.WAYPOINT.get(), pos, state);
    }

    public static void clientTick(Level level, BlockPos pos, BlockState state, WaypointBlockEntity tile) {
        if ((++tile.clientTicks & 7) == 0) {
            WaypointClientEffects.spawnMarker(pos);
        }
    }

    @Override
    protected void loadAdditional(CompoundTag tag, net.minecraft.core.HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        com.lulan.shincolle.item.LegacyItemData.upgradeNested(registries, tag);
        this.owner = PlayerOwnerData.load(tag, OWNER_UUID_TAG, OWNER_NAME_TAG, OWNER_UID_TAG);
        if (this.owner == null && tag.contains(LEGACY_OWNER_UID_TAG, Tag.TAG_INT)) {
            int legacyUid = Math.max(0, tag.getInt(LEGACY_OWNER_UID_TAG));
            if (legacyUid > 0) {
                this.owner = new PlayerOwnerData(null, "", legacyUid);
            }
        }

        this.stayMode = Mth.clamp(readInt(tag, STAY_MODE_TAG, LEGACY_STAY_MODE_TAG), 0, MAX_STAY_MODE);
        this.lastWaypoint = readOptionalPos(tag, LAST_WAYPOINT_TAG);
        if (this.lastWaypoint == null) {
            this.lastWaypoint = readLegacyPos(tag, LEGACY_LAST_WAYPOINT_TAG);
        }
        this.nextWaypoint = readOptionalPos(tag, NEXT_WAYPOINT_TAG);
        if (this.nextWaypoint == null) {
            this.nextWaypoint = readLegacyPos(tag, LEGACY_NEXT_WAYPOINT_TAG);
        }
        this.pairedChest = readOptionalPos(tag, PAIRED_CHEST_TAG);
        if (this.pairedChest == null) {
            this.pairedChest = readLegacyPos(tag, LEGACY_PAIRED_CHEST_TAG);
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag, net.minecraft.core.HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);

        if (this.owner != null) {
            this.owner.save(tag, OWNER_UUID_TAG, OWNER_NAME_TAG, OWNER_UID_TAG);
        }

        tag.putInt(STAY_MODE_TAG, this.stayMode);
        writeOptionalPos(tag, LAST_WAYPOINT_TAG, this.lastWaypoint);
        writeOptionalPos(tag, NEXT_WAYPOINT_TAG, this.nextWaypoint);
        writeOptionalPos(tag, PAIRED_CHEST_TAG, this.pairedChest);
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

    public int getOwnerUid() {
        return this.owner == null ? 0 : this.owner.uid();
    }

    @Override
    public Component getRouteNodeName() {
        return Component.translatable("block.shincolle.blockwaypoint");
    }

    public void cycleStayMode() {
        this.stayMode = (this.stayMode + 1) % (MAX_STAY_MODE + 1);
        this.markUpdated();
    }

    public int getStayMode() {
        return this.stayMode;
    }

    public int getStayTicks() {
        return stayModeToTicks(this.stayMode);
    }

    public Component getStayLabel() {
        int ticks = this.getStayTicks();
        return ticks <= 0
                ? Component.translatable("gui.shincolle.waypoint.stay.off")
                : Component.literal(formatStayTicks(ticks));
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

    public static int stayModeToTicks(int stayMode) {
        return switch (stayMode) {
            case 1, 2, 3, 4, 5 -> stayMode * 100;
            case 6, 7, 8, 9, 10 -> (stayMode - 5) * 1200;
            case 11, 12, 13, 14, 15, 16 -> (stayMode - 10) * 12000;
            default -> 0;
        };
    }

    public static String formatStayTicks(int ticks) {
        int seconds = Math.max(0, ticks / 20);
        if (seconds >= 60) {
            return seconds / 60 + "m";
        }

        return seconds + "s";
    }

    @Override
    public CompoundTag getUpdateTag(net.minecraft.core.HolderLookup.Provider registries) {
        CompoundTag tag = super.getUpdateTag(registries);
        this.saveAdditional(tag, registries);
        return tag;
    }

    @Override
    @Nullable
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
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

    private static int readInt(CompoundTag tag, String key, String legacyKey) {
        return tag.contains(key, Tag.TAG_INT) ? tag.getInt(key) : tag.getInt(legacyKey);
    }

    private static @Nullable BlockPos readLegacyPos(CompoundTag tag, String key) {
        int[] values = tag.getIntArray(key);
        if (values.length != 3 || values[1] <= 0) {
            return null;
        }

        return new BlockPos(values[0], values[1], values[2]);
    }

    private static void writeOptionalPos(CompoundTag tag, String key, @Nullable BlockPos pos) {
        if (pos != null) {
            tag.put(key, NbtUtils.writeBlockPos(pos));
        }
    }
}
