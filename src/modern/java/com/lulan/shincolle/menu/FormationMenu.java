package com.lulan.shincolle.menu;

import com.lulan.shincolle.entity.ship.LegacyShipEntity;
import com.lulan.shincolle.entity.ship.LegacyShipStatTables;
import com.lulan.shincolle.entity.ship.LegacyShipStats;
import com.lulan.shincolle.network.ModNetwork;
import com.lulan.shincolle.registry.ModMenus;
import com.lulan.shincolle.teitoku.TeitokuData;
import com.lulan.shincolle.teitoku.TeitokuHelper;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;

import java.util.Optional;

public class FormationMenu extends AbstractContainerMenu {

    private final Inventory inventory;
    private CompoundTag visualData = new CompoundTag();
    private CompoundTag lastSentVisualData;
    private int visualRevision;

    public FormationMenu(int containerId, Inventory inventory) {
        super(ModMenus.FORMATION.get(), containerId);
        this.inventory = inventory;
    }

    public static FormationMenu fromNetwork(int containerId, Inventory inventory, FriendlyByteBuf buffer) {
        FormationMenu menu = new FormationMenu(containerId, inventory);
        CompoundTag tag = buffer.readNbt();
        if (tag != null) menu.applyVisualData(tag);
        return menu;
    }

    public void applyVisualData(CompoundTag tag) {
        this.visualData = tag.copy();
        this.visualRevision++;
    }

    public int getVisualRevision() { return this.visualRevision; }

    @Override
    public void broadcastChanges() {
        super.broadcastChanges();
        if (this.inventory.player instanceof ServerPlayer player) {
            CompoundTag next = captureVisualData(player);
            if (!next.equals(this.lastSentVisualData)) {
                this.lastSentVisualData = next.copy();
                CompoundTag payload = new CompoundTag();
                payload.putInt("FormationContainerId", this.containerId);
                payload.put("FormationVisualData", next);
                ModNetwork.syncGameplayState(player, payload);
            }
        }
    }

    public static CompoundTag captureVisualData(Player player) {
        CompoundTag result = new CompoundTag();
        TeitokuData data = TeitokuHelper.get(player).orElse(null);
        if (data == null) return result;
        result.putInt("Team", data.getCurrentTeamId());
        ListTag teams = new ListTag();
        for (int team = 0; team < TeitokuData.TEAM_COUNT; team++) {
            LegacyShipEntity[] ships = new LegacyShipEntity[6];
            float minMov = 10F;
            boolean found = false;
            for (int slot = 0; slot < 6; slot++) {
                ships[slot] = findShipByUid(player, data.getShipUid(team, slot));
                if (ships[slot] != null) {
                    found = true;
                    minMov = Math.min(minMov, ships[slot].getLegacyStats().moveSpeed());
                }
            }
            if (!found) minMov = 0F;
            ListTag list = new ListTag();
            for (int slot = 0; slot < 6; slot++) {
                LegacyShipEntity ship = ships[slot];
                if (ship == null) continue;
                LegacyShipStats stats = ship.getLegacyStats();
                CompoundTag entry = new CompoundTag();
                entry.putInt("Slot", slot);
                entry.putInt("Uid", ship.getShipUid());
                entry.putInt("EntityId", ship.getId());
                entry.putString("Name", Component.Serializer.toJson(ship.getName()));
                entry.putInt("Level", ship.getShipLevel());
                entry.putInt("Morale", ship.getMorale());
                entry.putFloat("Health", ship.getHealth());
                entry.putFloat("MaxHealth", ship.getMaxHealth());
                entry.putInt("FormatType", ship.getLegacyFormationId());
                entry.putInt("FormatPos", ship.getLegacyFormationSlot());
                entry.putFloat("MinMOV", minMov);
                putFloats(entry, "Buffed", stats.copyBuffed());
                putFloats(entry, "WithoutFormation", stats.copyWithoutFormation());
                float[] formation = new float[LegacyShipStatTables.ATTR_COUNT];
                for (int i = 0; i < formation.length; i++) formation[i] = stats.formationStat(i);
                putFloats(entry, "Formation", formation);
                list.add(entry);
            }
            CompoundTag teamData = new CompoundTag();
            teamData.put("Ships", list);
            teams.add(teamData);
        }
        result.put("Teams", teams);
        ListTag previews = new ListTag();
        for (int formation = 0; formation < 6; formation++) {
            for (int slot = 0; slot < 6; slot++) {
                CompoundTag entry = new CompoundTag();
                putFloats(entry, "Values", LegacyShipStatTables.copyFormationStats(formation, slot));
                previews.add(entry);
            }
        }
        result.put("Previews", previews);
        return result;
    }

    private static void putFloats(CompoundTag tag, String key, float[] values) {
        int[] bits = new int[values.length];
        for (int i = 0; i < bits.length; i++) bits[i] = Float.floatToIntBits(values[i]);
        tag.putIntArray(key, bits);
    }

    private static float[] readFloats(CompoundTag tag, String key) {
        int[] bits = tag.getIntArray(key);
        float[] values = new float[LegacyShipStatTables.ATTR_COUNT];
        if (bits.length == values.length) {
            for (int i = 0; i < values.length; i++) values[i] = Float.intBitsToFloat(bits[i]);
        }
        return values;
    }

    public float[] getFormationBuffValue(int formation, int slot) {
        ListTag previews = this.visualData.getList("Previews", Tag.TAG_COMPOUND);
        int index = formation * 6 + slot;
        return formation >= 0 && formation < 6 && slot >= 0 && slot < 6 && index < previews.size()
                ? readFloats(previews.getCompound(index), "Values") : LegacyShipStatTables.copyResetFormation();
    }

    public int getFormationId(int team) {
        return this.withData(data -> data.getFormationId(team)).orElse(0);
    }

    public String getUnitName(int team) {
        return this.withData(data -> data.getTeamName(team)).orElse("");
    }

    public int getSID(int team, int slot) {
        return this.withData(data -> data.getShipUid(team, slot)).orElse(-1);
    }

    public ShipView[] getShipEntityAll(int team) {
        ShipView[] ships = new ShipView[6];
        ListTag teams = this.visualData.getList("Teams", Tag.TAG_COMPOUND);
        if (team < 0 || team >= teams.size()) return ships;
        ListTag list = teams.getCompound(team).getList("Ships", Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            CompoundTag entry = list.getCompound(i);
            int slot = entry.getInt("Slot");
            if (slot < 0 || slot >= 6 || entry.getIntArray("Buffed").length != LegacyShipStatTables.ATTR_COUNT) continue;
            if (this.inventory.player.level().getEntity(entry.getInt("EntityId")) instanceof LegacyShipEntity entity
                    && entity.getShipUid() == entry.getInt("Uid") && !entity.isRemoved()) {
                ships[slot] = new ShipView(entry);
            }
        }
        return ships;
    }

    public static final class ShipView {
        private final CompoundTag data;
        private final float[] formation;
        private final float[] buffed;

        private ShipView(CompoundTag data) {
            this.data = data.copy();
            this.formation = readFloats(data, "Formation");
            this.buffed = readFloats(data, "Buffed");
        }

        public String getName() {
            Component name = Component.Serializer.fromJson(this.data.getString("Name"));
            return name == null ? "" : name.getString();
        }
        public int getUid() { return this.data.getInt("Uid"); }
        public int getEntityId() { return this.data.getInt("EntityId"); }
        public int getLevel() { return this.data.getInt("Level"); }
        public int getMorale() { return this.data.getInt("Morale"); }
        public float getHealth() { return this.data.getFloat("Health"); }
        public float getMaxHealth() { return this.data.getFloat("MaxHealth"); }
        public int getFormatType() { return this.data.getInt("FormatType"); }
        public int getFormatPos() { return this.data.getInt("FormatPos"); }
        public ShipView getAttrs() { return this; }
        public float getAttrsFormation(int id) { return this.formation[id]; }
        public float getAttrsBuffed(int id) { return this.buffed[id]; }
        public float getMinMOV() { return this.data.getFloat("MinMOV"); }
        public float[] copyWithoutFormation() { return readFloats(this.data, "WithoutFormation"); }
    }

    public int getCurrentTeamId() {
        return this.withData(TeitokuData::getCurrentTeamId).orElse(0);
    }

    public int getCurrentFormationId() {
        return this.withData(TeitokuData::getCurrentFormationId).orElse(TeitokuData.DEFAULT_FORMATION_ID);
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }

    private static LegacyShipEntity findShipByUid(Player player, int shipUid) {
        if (shipUid <= 0) return null;
        if (player.level() instanceof ServerLevel level) {
            for (Entity entity : level.getAllEntities()) {
                if (entity instanceof LegacyShipEntity ship && ship.getShipUid() == shipUid
                        && !ship.isRemoved() && ship.canCommanderEdit(player)) return ship;
            }
            return null;
        }
        for (LegacyShipEntity ship : player.level().getEntitiesOfClass(
                LegacyShipEntity.class,
                player.getBoundingBox().inflate(256.0D),
                candidate -> candidate.getShipUid() == shipUid && !candidate.isRemoved())) {
            if (ship.canCommanderEdit(player)) {
                return ship;
            }
        }
        return null;
    }

    private <T> Optional<T> withData(java.util.function.Function<TeitokuData, T> mapper) {
        TeitokuData data = TeitokuHelper.get(this.inventory.player).orElse(null);
        if (data == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(mapper.apply(data));
    }

}
