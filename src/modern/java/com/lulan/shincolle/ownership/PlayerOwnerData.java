package com.lulan.shincolle.ownership;

import com.lulan.shincolle.team.TeamData;
import com.lulan.shincolle.teitoku.TeitokuHelper;
import com.lulan.shincolle.teitoku.TeitokuSavedData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

import javax.annotation.Nullable;
import java.util.Objects;
import java.util.UUID;

public record PlayerOwnerData(UUID uuid, String name, int uid) {

    public static PlayerOwnerData of(Player player) {
        return new PlayerOwnerData(player.getUUID(), player.getGameProfile().getName(), Math.max(0, TeitokuHelper.getPlayerUid(player)));
    }

    public static @Nullable PlayerOwnerData load(CompoundTag tag, String uuidKey, String nameKey) {
        return load(tag, uuidKey, nameKey, null);
    }

    public static @Nullable PlayerOwnerData load(CompoundTag tag, String uuidKey, String nameKey, @Nullable String uidKey) {
        if (!tag.hasUUID(uuidKey) && !tag.contains(nameKey)) {
            if (uidKey == null || !tag.contains(uidKey, Tag.TAG_INT)) {
                return null;
            }
        }

        UUID uuid = tag.hasUUID(uuidKey) ? tag.getUUID(uuidKey) : null;
        String name = tag.getString(nameKey);
        int uid = uidKey != null && tag.contains(uidKey, Tag.TAG_INT) ? Math.max(0, tag.getInt(uidKey)) : 0;
        if (uuid == null && name.isBlank() && uid <= 0) {
            return null;
        }

        return new PlayerOwnerData(uuid, name, uid);
    }

    public void save(CompoundTag tag, String uuidKey, String nameKey) {
        this.save(tag, uuidKey, nameKey, null);
    }

    public void save(CompoundTag tag, String uuidKey, String nameKey, @Nullable String uidKey) {
        if (this.uuid != null) {
            tag.putUUID(uuidKey, this.uuid);
        }

        if (!this.name.isBlank()) {
            tag.putString(nameKey, this.name);
        }

        if (uidKey != null && this.uid > 0) {
            tag.putInt(uidKey, this.uid);
        }
    }

    public boolean canEdit(Player player) {
        if (player == null) {
            return false;
        }
        if (player.hasPermissions(2)) {
            return true;
        }

        int ownerUid = this.resolveOwnerUid(player);
        if (this.uuid != null) {
            return Objects.equals(this.uuid, player.getUUID())
                    || (ownerUid > 0 && isAlliedOperator(player, ownerUid));
        }

        return this.uid <= 0
                || TeitokuHelper.getPlayerUid(player) == this.uid
                || (ownerUid > 0 && isAlliedOperator(player, ownerUid));
    }

    private int resolveOwnerUid(Player viewer) {
        if (this.uid > 0) {
            return this.uid;
        }
        if (this.uuid != null && viewer.level() instanceof ServerLevel serverLevel) {
            return TeitokuSavedData.get(serverLevel).getUid(this.uuid);
        }
        return 0;
    }

    private static boolean isAlliedOperator(Player player, int ownerUid) {
        int playerUid = TeitokuHelper.getPlayerUid(player);
        if (playerUid <= 0) {
            return false;
        }
        if (playerUid == ownerUid) {
            return true;
        }
        if (player.level() instanceof ServerLevel serverLevel) {
            return TeitokuHelper.isAlly(serverLevel, ownerUid, playerUid);
        }

        TeamData ownerTeam = TeitokuHelper.getClientTeamData().get(ownerUid);
        return ownerTeam != null && ownerTeam.isAlly(playerUid);
    }

    public Component displayLabel() {
        return this.name.isBlank()
                ? Component.translatable("gui.shincolle.waypoint.owner.unassigned")
                : Component.literal(this.name);
    }
}
