package com.lulan.shincolle.teitoku;

import com.lulan.shincolle.blockentity.CraneBlockEntity;
import com.lulan.shincolle.blockentity.DeskBlockEntity;
import com.lulan.shincolle.blockentity.HeavyGrudgeBlockEntity;
import com.lulan.shincolle.blockentity.LegacyCoreBlockEntity;
import com.lulan.shincolle.blockentity.RouteNode;
import com.lulan.shincolle.blockentity.SmallShipyardBlockEntity;
import com.lulan.shincolle.blockentity.WaypointBlockEntity;
import com.lulan.shincolle.combat.WorldCombatRulesSavedData;
import com.lulan.shincolle.config.ShinColleConfig;
import com.lulan.shincolle.entity.ship.LegacyShipEntity;
import com.lulan.shincolle.playerskill.PlayerSkillHostResolver;
import com.lulan.shincolle.network.ModNetwork;
import com.lulan.shincolle.team.TeamData;
import com.lulan.shincolle.team.TeamSavedData;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.util.LazyOptional;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class TeitokuHelper {

    private static final String TEITOKU_PAYLOAD_TAG = "Teitoku";
    private static final String TEAM_LIST_PAYLOAD_TAG = "TeamList";
    private static final String WORLD_RULES_PAYLOAD_TAG = "WorldCombatRules";
    private static final String WORLD_UNATTACKABLE_TAG = "UnattackableClasses";
    private static final String SHIP_CACHE_PAYLOAD_TAG = "ShipCache";
    public static final String DESK_RADAR_PAYLOAD_TAG = "DeskRadarContacts";
    private static final String SYSTEM_CONFIG_PAYLOAD_TAG = "SystemConfig";

    public static final Capability<TeitokuData> TEITOKU_CAPABILITY =
            CapabilityManager.get(new CapabilityToken<>() {});
    private static final Map<Integer, TeamData> CLIENT_TEAM_DATA = new ConcurrentHashMap<>();
    private static final Map<Integer, ShipWorldCacheEntry> CLIENT_SHIP_CACHE = new ConcurrentHashMap<>();
    private static final Set<String> CLIENT_WORLD_UNATTACKABLE_CLASSES = ConcurrentHashMap.newKeySet();
    private static final List<DeskRadarContactSnapshot> CLIENT_DESK_RADAR_CONTACTS =
            new java.util.concurrent.CopyOnWriteArrayList<>();
    private static volatile @Nullable int[] clientRingAbility;
    private static volatile @Nullable Boolean clientEasyMode;
    private static volatile @Nullable Integer clientCloseGuiDist;
    private static volatile @Nullable Integer clientRadarUpdate;
    private static volatile @Nullable Boolean clientPolyAsMn;

    public record DeskRadarContactSnapshot(String kind, String name, String statusText,
                                           int x, int y, int z, int ownerUid, boolean hostile) {
    }

    private TeitokuHelper() {
    }

    public static LazyOptional<TeitokuData> get(Player player) {
        return player.getCapability(TEITOKU_CAPABILITY);
    }

    public static void initializeAndSync(ServerPlayer player) {
        TeitokuSavedData savedData = TeitokuSavedData.get(player.serverLevel());
        get(player).ifPresent(teitokuData -> {
            teitokuData.setPlayerName(player.getGameProfile().getName());
            teitokuData.setPlayerUid(savedData.getOrCreateUid(player));
            if (teitokuData.hasTeam() && teitokuData.getPlayerUid() > 0) {
                TeamSavedData.get(player.serverLevel()).getOrCreateTeam(teitokuData.getPlayerUid(), teitokuData.getPlayerName());
            }
            sync(player);
        });
    }

    public static void sync(ServerPlayer player) {
        ModNetwork.syncTeitoku(player);
        syncGameplayState(player);
    }

    public static void syncGameplayState(ServerPlayer player) {
        ModNetwork.syncGameplayState(player, buildGameplayStateTag(player));
    }

    public static void syncGameplayStateToRelevantPlayers(ServerPlayer sourcePlayer) {
        MinecraftServer server = sourcePlayer.server;
        if (server == null) {
            syncGameplayState(sourcePlayer);
            return;
        }

        for (ServerPlayer online : server.getPlayerList().getPlayers()) {
            syncGameplayState(online);
        }
    }

    public static CompoundTag buildGameplayStateTag(ServerPlayer player) {
        return buildGameplayStateTag(player.serverLevel(), player);
    }

    public static CompoundTag buildGameplayStateTag(ServerLevel level, Player player) {
        CompoundTag payload = new CompoundTag();
        get(player).ifPresent(teitokuData -> payload.put(TEITOKU_PAYLOAD_TAG, teitokuData.saveToTag(new CompoundTag())));
        get(player).ifPresent(teitokuData -> payload.putBoolean("ShipIdsInitialized", teitokuData.areShipIdsInitialized()));

        TeamSavedData teamSavedData = TeamSavedData.get(level);
        ListTag teamList = new ListTag();
        for (TeamData teamData : teamSavedData.getAllTeams()) {
            teamList.add(teamData.saveToTag(new CompoundTag()));
        }
        payload.put(TEAM_LIST_PAYLOAD_TAG, teamList);

        CompoundTag worldRulesTag = new CompoundTag();
        ListTag unattackable = new ListTag();
        for (String targetClass : WorldCombatRulesSavedData.get(level).getAllUnattackableClasses()) {
            unattackable.add(StringTag.valueOf(targetClass));
        }
        worldRulesTag.put(WORLD_UNATTACKABLE_TAG, unattackable);
        payload.put(WORLD_RULES_PAYLOAD_TAG, worldRulesTag);

        ListTag shipCache = new ListTag();
        int playerUid = getPlayerUid(player);
        for (ShipWorldCacheEntry entry : ShipCacheSavedData.get(level).getShipsOwnedBy(playerUid)) {
            shipCache.add(entry.saveToTag(new CompoundTag()));
        }
        payload.put(SHIP_CACHE_PAYLOAD_TAG, shipCache);

        CompoundTag sysTag = new CompoundTag();
        sysTag.putIntArray("RingAbility", ShinColleConfig.ringAbility());
        sysTag.putBoolean("EasyMode", ShinColleConfig.easyMode());
        sysTag.putInt("CloseGuiDist", ShinColleConfig.closeGuiDist());
        sysTag.putInt("RadarUpdate", ShinColleConfig.radarUpdateTicks());
        sysTag.putBoolean("PolyAsMn", ShinColleConfig.polyAsMn());
        payload.put(SYSTEM_CONFIG_PAYLOAD_TAG, sysTag);
        return payload;
    }

    public static void applyClientState(Player player, CompoundTag payload) {
        if (payload.contains(TEITOKU_PAYLOAD_TAG, Tag.TAG_COMPOUND)) {
            get(player).ifPresent(data -> data.loadFromTag(payload.getCompound(TEITOKU_PAYLOAD_TAG)));
            get(player).ifPresent(data -> data.setShipIdsInitialized(payload.getBoolean("ShipIdsInitialized")));
        }

        if (payload.contains(TEAM_LIST_PAYLOAD_TAG, Tag.TAG_LIST)) {
            CLIENT_TEAM_DATA.clear();
            ListTag teamList = payload.getList(TEAM_LIST_PAYLOAD_TAG, Tag.TAG_COMPOUND);
            for (int i = 0; i < teamList.size(); i++) {
                TeamData teamData = TeamData.fromTag(teamList.getCompound(i));
                if (teamData.getTeamId() > 0) {
                    CLIENT_TEAM_DATA.put(teamData.getTeamId(), teamData);
                }
            }
        }

        if (payload.contains(WORLD_RULES_PAYLOAD_TAG, Tag.TAG_COMPOUND)) {
            CLIENT_WORLD_UNATTACKABLE_CLASSES.clear();
            ListTag worldRules = payload.getCompound(WORLD_RULES_PAYLOAD_TAG).getList(WORLD_UNATTACKABLE_TAG, Tag.TAG_STRING);
            for (int i = 0; i < worldRules.size(); i++) {
                String targetClass = normalizeTargetClass(worldRules.getString(i));
                if (!targetClass.isBlank()) {
                    CLIENT_WORLD_UNATTACKABLE_CLASSES.add(targetClass);
                }
            }
        }

        if (payload.contains(SHIP_CACHE_PAYLOAD_TAG, Tag.TAG_LIST)) {
            CLIENT_SHIP_CACHE.clear();
            ListTag shipCache = payload.getList(SHIP_CACHE_PAYLOAD_TAG, Tag.TAG_COMPOUND);
            for (int i = 0; i < shipCache.size(); i++) {
                ShipWorldCacheEntry entry = ShipWorldCacheEntry.load(shipCache.getCompound(i));
                if (entry.shipUid() > 0) {
                    CLIENT_SHIP_CACHE.put(entry.shipUid(), entry);
                }
            }
        }

        if (payload.contains(DESK_RADAR_PAYLOAD_TAG, Tag.TAG_LIST)) {
            CLIENT_DESK_RADAR_CONTACTS.clear();
            ListTag radarList = payload.getList(DESK_RADAR_PAYLOAD_TAG, Tag.TAG_COMPOUND);
            for (int i = 0; i < radarList.size(); i++) {
                CompoundTag entryTag = radarList.getCompound(i);
                CLIENT_DESK_RADAR_CONTACTS.add(new DeskRadarContactSnapshot(
                        entryTag.getString("Kind"),
                        entryTag.getString("Name"),
                        entryTag.getString("Status"),
                        entryTag.getInt("X"),
                        entryTag.getInt("Y"),
                        entryTag.getInt("Z"),
                        entryTag.getInt("OwnerUid"),
                        entryTag.getBoolean("Hostile")));
            }
        }

        if (payload.contains(SYSTEM_CONFIG_PAYLOAD_TAG, Tag.TAG_COMPOUND)) {
            CompoundTag sysTag = payload.getCompound(SYSTEM_CONFIG_PAYLOAD_TAG);
            int[] ringAbility = sysTag.getIntArray("RingAbility");
            if (ringAbility.length >= 5) {
                clientRingAbility = Arrays.copyOf(ringAbility, 5);
            }
            if (sysTag.contains("EasyMode")) {
                clientEasyMode = sysTag.getBoolean("EasyMode");
            }
            if (sysTag.contains("CloseGuiDist", Tag.TAG_INT)) {
                clientCloseGuiDist = sysTag.getInt("CloseGuiDist");
            }
            if (sysTag.contains("RadarUpdate", Tag.TAG_INT)) {
                clientRadarUpdate = sysTag.getInt("RadarUpdate");
            }
            if (sysTag.contains("PolyAsMn")) {
                clientPolyAsMn = sysTag.getBoolean("PolyAsMn");
            }
        }
    }

    public static void clearClientSystemConfigCache() {
        clientRingAbility = null;
        clientEasyMode = null;
        clientCloseGuiDist = null;
        clientRadarUpdate = null;
        clientPolyAsMn = null;
    }

    public static int[] getEffectiveRingAbility(@Nullable Level level) {
        int[] cached = clientRingAbility;
        if (level != null && level.isClientSide() && cached != null) {
            return Arrays.copyOf(cached, 5);
        }
        return ShinColleConfig.ringAbility();
    }

    public static boolean isEffectiveEasyMode(@Nullable Level level) {
        Boolean cached = clientEasyMode;
        if (level != null && level.isClientSide() && cached != null) {
            return cached;
        }
        return ShinColleConfig.easyMode();
    }

    public static int getEffectiveCloseGuiDist(@Nullable Level level) {
        Integer cached = clientCloseGuiDist;
        if (level != null && level.isClientSide() && cached != null) {
            return cached;
        }
        return ShinColleConfig.closeGuiDist();
    }

    public static int getEffectiveRadarUpdateTicks(@Nullable Level level) {
        Integer cached = clientRadarUpdate;
        if (level != null && level.isClientSide() && cached != null) {
            return cached;
        }
        return ShinColleConfig.radarUpdateTicks();
    }

    public static boolean isEffectivePolyAsMn(@Nullable Level level) {
        Boolean cached = clientPolyAsMn;
        if (level != null && level.isClientSide() && cached != null) {
            return cached;
        }
        return ShinColleConfig.polyAsMn();
    }

    public static List<DeskRadarContactSnapshot> getClientDeskRadarContacts() {
        return List.copyOf(CLIENT_DESK_RADAR_CONTACTS);
    }

    public static CompoundTag buildDeskRadarPayloadTag(ServerPlayer player) {
        CompoundTag payload = new CompoundTag();
        ListTag contacts = new ListTag();
        if (player == null) {
            payload.put(DESK_RADAR_PAYLOAD_TAG, contacts);
            return payload;
        }

        ServerLevel level = player.serverLevel();
        double maxRadius = 256.0D;
        double maxRadiusSq = maxRadius * maxRadius;

        for (LegacyShipEntity ship : level.getEntitiesOfClass(
                LegacyShipEntity.class,
                player.getBoundingBox().inflate(maxRadius),
                candidate -> candidate.isAlive() && candidate.distanceToSqr(player) <= maxRadiusSq)) {
            CompoundTag entry = new CompoundTag();
            entry.putString("Kind", "SHIP");
            entry.putString("Name", ship.getName().getString());
            entry.putString("Status", Mth.floor(ship.getHealth()) + "/" + Mth.floor(ship.getMaxHealth()));
            entry.putInt("X", Mth.floor((float) ship.getX()));
            entry.putInt("Y", Mth.floor((float) ship.getY()));
            entry.putInt("Z", Mth.floor((float) ship.getZ()));
            entry.putInt("OwnerUid", ship.getOwnerUid());
            entry.putBoolean("Hostile", ship.isHostileVariant());
            contacts.add(entry);
        }

        for (ItemEntity item : level.getEntitiesOfClass(
                ItemEntity.class,
                player.getBoundingBox().inflate(maxRadius),
                candidate -> candidate.isAlive() && candidate.distanceToSqr(player) <= maxRadiusSq)) {
            ItemStack stack = item.getItem();
            CompoundTag entry = new CompoundTag();
            entry.putString("Kind", "ITEM");
            entry.putString("Name", stack.isEmpty() ? "Item" : stack.getHoverName().getString());
            entry.putString("Status", "Stack: " + Math.max(1, stack.getCount()));
            entry.putInt("X", Mth.floor((float) item.getX()));
            entry.putInt("Y", Mth.floor((float) item.getY()));
            entry.putInt("Z", Mth.floor((float) item.getZ()));
            entry.putInt("OwnerUid", 0);
            entry.putBoolean("Hostile", false);
            contacts.add(entry);
        }

        int playerUid = getPlayerUid(player);
        int radius = 256;
        int radiusSq = radius * radius;
        BlockPos playerPos = player.blockPosition();
        int centerChunkX = playerPos.getX() >> 4;
        int centerChunkZ = playerPos.getZ() >> 4;
        int chunkRadius = Mth.ceil(radius / 16.0F) + 1;

        for (int chunkX = centerChunkX - chunkRadius; chunkX <= centerChunkX + chunkRadius; chunkX++) {
            for (int chunkZ = centerChunkZ - chunkRadius; chunkZ <= centerChunkZ + chunkRadius; chunkZ++) {
                LevelChunk chunk = level.getChunkSource().getChunkNow(chunkX, chunkZ);
                if (chunk == null) {
                    continue;
                }
                for (BlockPos pos : chunk.getBlockEntitiesPos()) {
                    if (pos.distSqr(playerPos) > radiusSq) {
                        continue;
                    }
                    BlockEntity blockEntity = level.getBlockEntity(pos);
                    if (blockEntity == null) {
                        continue;
                    }

                    String kind;
                    String status;
                    if (blockEntity instanceof WaypointBlockEntity waypoint && waypoint.canEdit(player)) {
                        kind = "ROUTE";
                        status = waypoint.getPairedChest() == null ? "Route: idle" : "Route: supply linked";
                    } else if (blockEntity instanceof CraneBlockEntity crane && crane.canEdit(player)) {
                        kind = "ROUTE";
                        status = crane.getPairedChest() == null ? "Crane: idle" : "Crane: linked";
                    } else if (blockEntity instanceof HeavyGrudgeBlockEntity heavyGrudge && heavyGrudge.canEdit(player)) {
                        kind = "FACILITY";
                        status = heavyGrudge.isStructureComplete()
                                ? "Heavy Grudge: grand shipyard"
                                : "Heavy Grudge: unformed";
                    } else if (blockEntity instanceof LegacyCoreBlockEntity core && core.canEdit(player)) {
                        kind = "FACILITY";
                        status = core.isVolCore() ? "VolCore charge " + core.getStoredCharge() : "Core charge " + core.getStoredCharge();
                    } else if (blockEntity instanceof DeskBlockEntity) {
                        kind = "FACILITY";
                        status = "Desk terminal";
                    } else if (blockEntity instanceof SmallShipyardBlockEntity shipyard && shipyard.canEdit(player)) {
                        kind = "FACILITY";
                        status = "Small shipyard";
                    } else if (blockEntity instanceof RouteNode routeNode && routeNode.canEdit(player)) {
                        kind = "ROUTE";
                        status = routeNode.getRouteNodeName().getString();
                    } else {
                        continue;
                    }

                    CompoundTag entry = new CompoundTag();
                    entry.putString("Kind", kind);
                    entry.putString("Name", blockEntity.getBlockState().getBlock().getName().getString());
                    entry.putString("Status", status);
                    entry.putInt("X", pos.getX());
                    entry.putInt("Y", pos.getY());
                    entry.putInt("Z", pos.getZ());
                    entry.putInt("OwnerUid", playerUid);
                    entry.putBoolean("Hostile", false);
                    contacts.add(entry);
                }
            }
        }

        payload.put(DESK_RADAR_PAYLOAD_TAG, contacts);
        return payload;
    }

    public static Map<Integer, TeamData> getClientTeamData() {
        return Map.copyOf(CLIENT_TEAM_DATA);
    }

    public static Map<Integer, ShipWorldCacheEntry> getClientShipCache() {
        return Map.copyOf(CLIENT_SHIP_CACHE);
    }

    public static ShipWorldCacheEntry getClientShipCacheEntry(int shipUid) {
        return CLIENT_SHIP_CACHE.get(shipUid);
    }

    public static Set<String> getClientWorldUnattackableClasses() {
        return Set.copyOf(CLIENT_WORLD_UNATTACKABLE_CLASSES);
    }

    public static int getPlayerUid(Player player) {
        return get(player)
                .map(TeitokuData::getPlayerUid)
                .orElse(0);
    }

    public static boolean hasTeam(Player player) {
        return get(player)
                .map(TeitokuData::hasTeam)
                .orElse(false);
    }

    public static int resolvePlayerUid(ServerLevel level, UUID playerUuid, String playerName) {
        return TeitokuSavedData.get(level).getOrCreateUid(playerUuid, playerName);
    }

    public static int resolveShipUid(ServerLevel level, UUID shipUuid) {
        return TeitokuSavedData.get(level).getOrCreateShipUid(shipUuid);
    }

    public static void markRingState(ServerPlayer player, boolean active) {
        get(player).ifPresent(teitokuData -> {
            teitokuData.setRingActive(active);
            sync(player);
        });
    }

    public static void incrementMarriageCount(ServerPlayer player) {
        get(player).ifPresent(teitokuData -> {
            teitokuData.incrementMarriageNum();
            sync(player);
        });
    }

    public static void addCollectedShip(ServerPlayer player, int legacyClassId) {
        get(player).ifPresent(teitokuData -> {
            teitokuData.addCollectedShip(legacyClassId);
            sync(player);
        });
    }

    public static void addCollectedEquipment(ServerPlayer player, int equipmentId) {
        get(player).ifPresent(teitokuData -> {
            teitokuData.addCollectedEquipment(equipmentId);
            sync(player);
        });
    }

    public static int getTargetClassCount(Player player) {
        return get(player)
                .map(TeitokuData::getTargetClassCount)
                .orElse(0);
    }

    public static int getCurrentTeamId(Player player) {
        return get(player)
                .map(TeitokuData::getCurrentTeamId)
                .orElse(0);
    }

    public static int getTeamCooldown(Player player) {
        return get(player)
                .map(TeitokuData::getTeamCooldown)
                .orElse(0);
    }

    public static void setCurrentTeamId(ServerPlayer player, int teamId) {
        get(player).ifPresent(teitokuData -> {
            teitokuData.setCurrentTeamId(teamId);
            syncGameplayState(player);
        });
    }

    public static int getFormationId(Player player) {
        return get(player)
                .map(TeitokuData::getFormationId)
                .orElse(TeitokuData.DEFAULT_FORMATION_ID);
    }

    public static int getFormationIdForShip(Player player, int shipUid) {
        if (shipUid <= 0) {
            return TeitokuData.DEFAULT_FORMATION_ID;
        }

        return get(player)
                .map(teitokuData -> {
                    int teamId = teitokuData.findTeamIdByShipUid(shipUid);
                    if (teamId < 0) {
                        return TeitokuData.DEFAULT_FORMATION_ID;
                    }

                    // Keep the old rule: formation buffs only apply when the team has more than 4 ships.
                    if (teitokuData.countShipsInTeam(teamId) <= 4) {
                        return TeitokuData.DEFAULT_FORMATION_ID;
                    }

                    return teitokuData.getFormationId(teamId);
                })
                .orElse(TeitokuData.DEFAULT_FORMATION_ID);
    }

    public static int getFormationSlotForShip(Player player, int shipUid) {
        if (shipUid <= 0) {
            return 0;
        }

        return get(player)
                .map(teitokuData -> {
                    int teamId = teitokuData.findTeamIdByShipUid(shipUid);
                    if (teamId < 0 || teitokuData.countShipsInTeam(teamId) <= 4) {
                        return 0;
                    }

                    int[] shipUids = teitokuData.getTeamShipUids(teamId);
                    for (int slot = 0; slot < shipUids.length; slot++) {
                        if (shipUids[slot] == shipUid) {
                            return slot;
                        }
                    }
                    return 0;
                })
                .orElse(0);
    }

    public static int[] getTeamShipUidsForShip(Player player, int shipUid) {
        if (shipUid <= 0) {
            return new int[0];
        }

        return get(player)
                .map(teitokuData -> {
                    int teamId = teitokuData.findTeamIdByShipUid(shipUid);
                    if (teamId < 0 || teitokuData.countShipsInTeam(teamId) <= 4) {
                        return new int[0];
                    }
                    return teitokuData.getTeamShipUids(teamId);
                })
                .orElse(new int[0]);
    }

    public static int cycleFormationId(ServerPlayer player) {
        return get(player)
                .map(teitokuData -> {
                    int teamId = teitokuData.getCurrentTeamId();
                    int formationId;
                    if (teitokuData.countShipsInTeam(teamId) > 4) {
                        formationId = teitokuData.cycleFormationId(teamId);
                    } else {
                        teitokuData.setFormationId(teamId, TeitokuData.DEFAULT_FORMATION_ID);
                        formationId = TeitokuData.DEFAULT_FORMATION_ID;
                    }
                    syncGameplayState(player);
                    return formationId;
                })
                .orElse(TeitokuData.DEFAULT_FORMATION_ID);
    }

    public static net.minecraft.network.chat.Component getFormationLabel(Player player) {
        int formationId = getFormationId(player);
        return net.minecraft.network.chat.Component.translatable("gui.shincolle.formation.format" + formationId);
    }

    public static boolean toggleTargetClass(ServerPlayer player, String targetClass) {
        String normalized = normalizeTargetClass(targetClass);
        if (normalized.isBlank()) {
            return false;
        }

        return get(player)
                .map(teitokuData -> {
                    boolean added = teitokuData.toggleTargetClass(normalized);
                    sync(player);
                    return added;
                })
                .orElse(false);
    }

    public static boolean addTargetClass(ServerPlayer player, String targetClass) {
        String normalized = normalizeTargetClass(targetClass);
        if (normalized.isBlank()) {
            return false;
        }

        return get(player)
                .map(teitokuData -> {
                    boolean changed = teitokuData.addTargetClass(normalized);
                    if (changed) {
                        sync(player);
                    }
                    return changed;
                })
                .orElse(false);
    }

    public static boolean removeTargetClass(ServerPlayer player, String targetClass) {
        String normalized = normalizeTargetClass(targetClass);
        if (normalized.isBlank()) {
            return false;
        }

        return get(player)
                .map(teitokuData -> {
                    boolean changed = teitokuData.removeTargetClass(normalized);
                    if (changed) {
                        sync(player);
                    }
                    return changed;
                })
                .orElse(false);
    }

    public static void assignShipToCurrentTeam(ServerPlayer player, int slot, int shipUid) {
        get(player).ifPresent(teitokuData -> {
            LegacyShipEntity ship = shipUid > 0 ? findShipByUid(player, shipUid) : null;
            if (shipUid > 0 && ship == null) {
                return;
            }
            if (!teitokuData.addShipEntity(slot, ship, false)) {
                return;
            }
            teitokuData.setFormationId(teitokuData.getCurrentTeamId(), TeitokuData.DEFAULT_FORMATION_ID);
            syncGameplayState(player);
        });
    }

    public static boolean swapCurrentTeamSlots(ServerPlayer player, int fromSlot, int toSlot) {
        return get(player)
                .map(teitokuData -> {
                    boolean changed = teitokuData.swapCurrentTeamSlots(fromSlot, toSlot);
                    if (changed) {
                        syncGameplayState(player);
                    }
                    return changed;
                })
                .orElse(false);
    }

    public static void toggleCurrentTeamSelect(ServerPlayer player, int slot) {
        get(player).ifPresent(teitokuData -> {
            teitokuData.toggleCurrentTeamSelect(slot);
            syncGameplayState(player);
        });
    }

    public static boolean setCurrentTeamSelect(ServerPlayer player, int slot, boolean selected) {
        return get(player)
                .map(teitokuData -> {
                    boolean changed = teitokuData.setCurrentTeamSelection(slot, selected);
                    syncGameplayState(player);
                    return changed;
                })
                .orElse(false);
    }

    public static void clearCurrentTeam(ServerPlayer player) {
        get(player).ifPresent(teitokuData -> {
            teitokuData.clearCurrentTeam();
            syncGameplayState(player);
        });
    }

    public static void focusFirstShipInCurrentTeam(ServerPlayer player) {
        get(player).ifPresent(teitokuData -> {
            teitokuData.clearCurrentTeamSelect();
            int currentTeamId = teitokuData.getCurrentTeamId();
            for (int slot = 0; slot < TeitokuData.TEAM_SIZE; slot++) {
                if (teitokuData.getShipUid(currentTeamId, slot) > 0) {
                    teitokuData.setCurrentTeamSelection(slot, true);
                    break;
                }
            }
            syncGameplayState(player);
        });
    }

    public static int setFormationId(ServerPlayer player, int formationId) {
        return get(player)
                .map(teitokuData -> {
                    int teamId = teitokuData.getCurrentTeamId();
                    int resolved = teitokuData.countShipsInTeam(teamId) > 4
                            ? net.minecraft.util.Mth.clamp(formationId, TeitokuData.DEFAULT_FORMATION_ID, TeitokuData.MAX_FORMATION_ID)
                            : TeitokuData.DEFAULT_FORMATION_ID;
                    teitokuData.setFormationId(teamId, resolved);
                    syncGameplayState(player);
                    return resolved;
                })
                .orElse(TeitokuData.DEFAULT_FORMATION_ID);
    }

    public static void setUnitName(ServerPlayer player, int rawTeamId, String unitName) {
        String normalized = unitName == null ? "" : unitName.trim();
        if (normalized.length() > 32) {
            normalized = normalized.substring(0, 32);
        }

        String finalName = normalized;
        get(player).ifPresent(teitokuData -> {
            int teamId = rawTeamId >= 0 && rawTeamId < TeitokuData.TEAM_COUNT
                    ? rawTeamId
                    : teitokuData.getCurrentTeamId();
            teitokuData.setTeamName(teamId, finalName);
            syncGameplayState(player);
        });
    }

    public static void removeShipFromTeams(ServerPlayer player, int shipUid) {
        if (shipUid <= 0) {
            return;
        }
        get(player).ifPresent(data -> {
            data.removeShipUidFromCurrentTeam(shipUid);
            syncGameplayState(player);
        });
    }

    public static void collectCurrentTeamShips(Player player, boolean selectedOnly, List<LegacyShipEntity> output) {
        get(player).ifPresent(teitokuData -> {
            for (int shipUid : teitokuData.getCurrentTeamShipUids(selectedOnly)) {
                LegacyShipEntity ship = findOwnedShipByUid(player, shipUid);
                if (ship != null && !output.contains(ship)) {
                    output.add(ship);
                }
            }
        });
    }

    public static LegacyShipEntity findOwnedShipByUid(Player player, int shipUid) {
        LegacyShipEntity ship = findShipByUid(player, shipUid);
        return ship != null && ship.canCommanderEdit(player) ? ship : null;
    }

    public static LegacyShipEntity findShipByUid(Player player, int shipUid) {
        if (shipUid <= 0) {
            return null;
        }

        if (player instanceof ServerPlayer serverPlayer) {
            ShipWorldCacheEntry cached = ShipCacheSavedData.get(serverPlayer.serverLevel()).getShip(shipUid);
            if (cached != null && !cached.dead() && serverPlayer.server != null) {
                ResourceLocation dimensionId = ResourceLocation.tryParse(cached.dimensionId());
                if (dimensionId != null) {
                    ServerLevel cachedLevel = serverPlayer.server.getLevel(ResourceKey.create(Registries.DIMENSION, dimensionId));
                    if (cachedLevel != null && cachedLevel.getEntity(cached.entityId()) instanceof LegacyShipEntity ship
                            && ship.getShipUid() == shipUid) {
                        return ship;
                    }
                }
            }
        }

        List<LegacyShipEntity> nearby = player.level().getEntitiesOfClass(
                LegacyShipEntity.class,
                player.getBoundingBox().inflate(512.0D),
                ship -> ship.getShipUid() == shipUid);
        return nearby.isEmpty() ? null : nearby.get(0);
    }

    public static void refreshPointerTeam(ServerPlayer player, TeitokuData data, boolean initialize) {
        int firstTeam = initialize ? 0 : data.getCurrentTeamId();
        int lastTeam = initialize ? TeitokuData.TEAM_COUNT : firstTeam + 1;
        for (int team = firstTeam; team < lastTeam; team++) {
            for (int slot = 0; slot < TeitokuData.TEAM_SIZE; slot++) {
                int uid = data.getShipUid(team, slot);
                LegacyShipEntity ship = findShipByUid(player, uid);
                if (ship != null) {
                    refreshShipCache(ship, !ship.isAlive());
                    if (!initialize && !PlayerSkillHostResolver.sameOwner(player, ship)) {
                        data.setShipUid(team, slot, -1);
                        data.setShipSelected(team, slot, false);
                    }
                } else if (!initialize && uid <= 0) {
                    data.setShipUid(team, slot, -1);
                    data.setShipSelected(team, slot, false);
                }
            }
        }
    }

    public static void refreshShipCache(LegacyShipEntity ship, boolean dead) {
        refreshShipCache(ship, dead, !dead);
    }

    public static void refreshShipCache(LegacyShipEntity ship, boolean dead, boolean online) {
        if (ship == null || ship.getShipUid() <= 0 || !(ship.level() instanceof ServerLevel serverLevel)) {
            return;
        }

        ShipCacheSavedData.get(serverLevel).updateFromShip(ship, dead, online);
    }

    public static void renameOwnTeam(ServerPlayer player, String teamName) {
        String normalized = teamName == null ? "" : teamName.trim();
        if (normalized.length() < 2) {
            return;
        }

        get(player).ifPresent(teitokuData -> {
            int teamId = teitokuData.getPlayerUid();
            if (teamId <= 0 || !teitokuData.hasTeam()) {
                return;
            }

            TeamData teamData = TeamSavedData.get(player.serverLevel()).getOrCreateTeam(teamId, player.getGameProfile().getName());
            teamData.setTeamName(normalized);
            TeamSavedData.get(player.serverLevel()).setDirty();

            teitokuData.setTeamName(teitokuData.getCurrentTeamId(), normalized);
            syncGameplayState(player);
        });
    }

    public static boolean createOwnTeam(ServerPlayer player, String teamName) {
        String normalized = teamName == null ? "" : teamName.trim();
        if (normalized.length() < 2) {
            return false;
        }

        return get(player)
                .map(teitokuData -> {
                    int playerUid = teitokuData.getPlayerUid();
                    if (playerUid <= 0 || teitokuData.hasTeam() || teitokuData.getTeamCooldown() > 0) {
                        return false;
                    }

                    TeamSavedData.get(player.serverLevel()).createTeam(playerUid, player.getGameProfile().getName(), normalized);
                    teitokuData.setHasTeam(true);
                    teitokuData.setTeamCooldown(ShinColleConfig.teamCooldown());
                    teitokuData.setTeamName(teitokuData.getCurrentTeamId(), normalized);
                    syncGameplayState(player);
                    return true;
                })
                .orElse(false);
    }

    public static boolean disbandOwnTeam(ServerPlayer player) {
        return get(player)
                .map(teitokuData -> {
                    int playerUid = teitokuData.getPlayerUid();
                    if (playerUid <= 0 || !teitokuData.hasTeam() || teitokuData.getTeamCooldown() > 0) {
                        return false;
                    }

                    boolean removed = TeamSavedData.get(player.serverLevel()).removeTeam(playerUid);
                    if (!removed) {
                        return false;
                    }

                    teitokuData.setHasTeam(false);
                    teitokuData.setTeamCooldown(ShinColleConfig.teamCooldown());
                    syncGameplayState(player);
                    return true;
                })
                .orElse(false);
    }

    public static int tickCooldown(ServerPlayer player) {
        return get(player)
                .map(teitokuData -> {
                    if (teitokuData.getTeamCooldown() <= 0) {
                        return 0;
                    }

                    int next = teitokuData.getTeamCooldown() - 1;
                    teitokuData.setTeamCooldown(next);
                    return next;
                })
                .orElse(0);
    }

    public static int tickBossCooldown(ServerPlayer player) {
        return get(player)
                .map(teitokuData -> {
                    if (teitokuData.getBossCooldown() <= 0) {
                        return 0;
                    }

                    int next = teitokuData.getBossCooldown() - 1;
                    teitokuData.setBossCooldown(next);
                    return next;
                })
                .orElse(0);
    }

    public static int getCurrentTeamSlotOfShip(Player player, int shipUid) {
        if (shipUid <= 0) {
            return -1;
        }

        return get(player)
                .map(teitokuData -> {
                    int[] shipUids = teitokuData.getTeamShipUids(teitokuData.getCurrentTeamId());
                    for (int i = 0; i < shipUids.length; i++) {
                        if (shipUids[i] == shipUid) {
                            return i;
                        }
                    }
                    return -1;
                })
                .orElse(-1);
    }

    public static int getOwnerUid(Entity entity) {
        if (entity instanceof LegacyShipEntity ship) {
            return ship.getOwnerUid();
        }
        if (entity instanceof Player player) {
            return getPlayerUid(player);
        }
        return 0;
    }

    public static boolean isAlly(ServerLevel level, int ownerA, int ownerB) {
        if (ownerA <= 0 || ownerB <= 0) {
            return false;
        }
        if (ownerA == ownerB) {
            return true;
        }

        TeamSavedData teamData = TeamSavedData.get(level);
        TeamData self = teamData.getTeam(ownerA);
        TeamData target = teamData.getTeam(ownerB);
        return self != null && target != null && self.isAlly(ownerB);
    }

    public static boolean isBanned(ServerLevel level, int ownerA, int ownerB) {
        if (ownerA <= 0 || ownerB <= 0 || ownerA == ownerB) {
            return false;
        }
        TeamSavedData teamData = TeamSavedData.get(level);
        TeamData self = teamData.getTeam(ownerA);
        TeamData target = teamData.getTeam(ownerB);
        return self != null && target != null && self.isBanned(ownerB);
    }

    public static boolean isAutoTargetAllowed(Player owner, LivingEntity target) {
        String targetClass = resolveTargetClass(target);
        return get(owner)
                .map(teitokuData -> {
                    if (teitokuData.getTargetClassCount() <= 0) {
                        return true;
                    }
                    return teitokuData.hasTargetClass(targetClass);
                })
                .orElse(true);
    }

    public static String resolveTargetClass(LivingEntity target) {
        return target == null ? "" : normalizeTargetClass(target.getClass().getSimpleName());
    }

    private static String normalizeTargetClass(String targetClass) {
        return targetClass == null ? "" : targetClass.trim().toLowerCase(Locale.ROOT);
    }
}
