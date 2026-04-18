package com.lulan.shincolle.teitoku;

import com.lulan.shincolle.combat.WorldCombatRulesSavedData;
import com.lulan.shincolle.entity.ship.LegacyShipEntity;
import com.lulan.shincolle.network.ModNetwork;
import com.lulan.shincolle.team.TeamData;
import com.lulan.shincolle.team.TeamSavedData;
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
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.util.LazyOptional;

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

    public static final Capability<TeitokuData> TEITOKU_CAPABILITY =
            CapabilityManager.get(new CapabilityToken<>() {});
    private static final Map<Integer, TeamData> CLIENT_TEAM_DATA = new ConcurrentHashMap<>();
    private static final Map<Integer, ShipWorldCacheEntry> CLIENT_SHIP_CACHE = new ConcurrentHashMap<>();
    private static final Set<String> CLIENT_WORLD_UNATTACKABLE_CLASSES = ConcurrentHashMap.newKeySet();

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
            if (online.level().dimension().equals(sourcePlayer.level().dimension())) {
                syncGameplayState(online);
            }
        }
    }

    public static CompoundTag buildGameplayStateTag(ServerPlayer player) {
        return buildGameplayStateTag(player.serverLevel(), player);
    }

    public static CompoundTag buildGameplayStateTag(ServerLevel level, Player player) {
        CompoundTag payload = new CompoundTag();
        get(player).ifPresent(teitokuData -> payload.put(TEITOKU_PAYLOAD_TAG, teitokuData.saveToTag(new CompoundTag())));

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
        return payload;
    }

    public static void applyClientState(Player player, CompoundTag payload) {
        if (payload.contains(TEITOKU_PAYLOAD_TAG, Tag.TAG_COMPOUND)) {
            get(player).ifPresent(data -> data.loadFromTag(payload.getCompound(TEITOKU_PAYLOAD_TAG)));
        }

        CLIENT_TEAM_DATA.clear();
        ListTag teamList = payload.getList(TEAM_LIST_PAYLOAD_TAG, Tag.TAG_COMPOUND);
        for (int i = 0; i < teamList.size(); i++) {
            TeamData teamData = TeamData.fromTag(teamList.getCompound(i));
            if (teamData.getTeamId() > 0) {
                CLIENT_TEAM_DATA.put(teamData.getTeamId(), teamData);
            }
        }

        CLIENT_WORLD_UNATTACKABLE_CLASSES.clear();
        if (payload.contains(WORLD_RULES_PAYLOAD_TAG, Tag.TAG_COMPOUND)) {
            ListTag worldRules = payload.getCompound(WORLD_RULES_PAYLOAD_TAG).getList(WORLD_UNATTACKABLE_TAG, Tag.TAG_STRING);
            for (int i = 0; i < worldRules.size(); i++) {
                String targetClass = normalizeTargetClass(worldRules.getString(i));
                if (!targetClass.isBlank()) {
                    CLIENT_WORLD_UNATTACKABLE_CLASSES.add(targetClass);
                }
            }
        }

        CLIENT_SHIP_CACHE.clear();
        ListTag shipCache = payload.getList(SHIP_CACHE_PAYLOAD_TAG, Tag.TAG_COMPOUND);
        for (int i = 0; i < shipCache.size(); i++) {
            ShipWorldCacheEntry entry = ShipWorldCacheEntry.load(shipCache.getCompound(i));
            if (entry.shipUid() > 0) {
                CLIENT_SHIP_CACHE.put(entry.shipUid(), entry);
            }
        }
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
            teitokuData.setHasRing(true);
            teitokuData.setRingActive(active);
            if (!active) {
                teitokuData.setRingFlying(false);
            }
            sync(player);
        });
    }

    public static void incrementMarriageCount(ServerPlayer player) {
        get(player).ifPresent(teitokuData -> {
            teitokuData.setHasRing(true);
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

    public static int cycleFormationId(ServerPlayer player) {
        return get(player)
                .map(teitokuData -> {
                    int formationId = teitokuData.cycleFormationId();
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
            teitokuData.assignCurrentTeamSlot(slot, shipUid);
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

    public static void removeShipFromTeams(ServerPlayer player, int shipUid) {
        if (shipUid <= 0) {
            return;
        }
        removeShipFromAllOnlineTeams(player.server, shipUid);
    }

    public static void removeShipFromAllOnlineTeams(MinecraftServer server, int shipUid) {
        if (server == null || shipUid <= 0) {
            return;
        }

        for (ServerPlayer online : server.getPlayerList().getPlayers()) {
            get(online).ifPresent(teitokuData -> {
                teitokuData.removeShipUidEverywhere(shipUid);
                syncGameplayState(online);
            });
        }
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
                            && ship.getShipUid() == shipUid && ship.canCommanderEdit(player)) {
                        return ship;
                    }
                }
            }
        }

        List<LegacyShipEntity> nearby = player.level().getEntitiesOfClass(
                LegacyShipEntity.class,
                player.getBoundingBox().inflate(512.0D),
                ship -> ship.getShipUid() == shipUid && ship.canCommanderEdit(player));
        return nearby.isEmpty() ? null : nearby.get(0);
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
        if (normalized.isBlank()) {
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
        return get(player)
                .map(teitokuData -> {
                    int playerUid = teitokuData.getPlayerUid();
                    if (playerUid <= 0 || teitokuData.hasTeam() || teitokuData.getTeamCooldown() > 0) {
                        return false;
                    }

                    TeamSavedData.get(player.serverLevel()).createTeam(playerUid, player.getGameProfile().getName(), teamName);
                    teitokuData.setHasTeam(true);
                    teitokuData.setTeamCooldown(TeitokuData.DEFAULT_TEAM_COOLDOWN);
                    if (teamName != null && !teamName.isBlank()) {
                        teitokuData.setTeamName(teitokuData.getCurrentTeamId(), teamName.trim());
                    }
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
                    teitokuData.setTeamCooldown(TeitokuData.DEFAULT_TEAM_COOLDOWN);
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

        TeamData self = TeamSavedData.get(level).getTeam(ownerA);
        return self != null && self.isAlly(ownerB);
    }

    public static boolean isBanned(ServerLevel level, int ownerA, int ownerB) {
        if (ownerA <= 0 || ownerB <= 0 || ownerA == ownerB) {
            return false;
        }
        TeamData self = TeamSavedData.get(level).getTeam(ownerA);
        return self != null && self.isBanned(ownerB);
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
        ResourceLocation key = BuiltInRegistries.ENTITY_TYPE.getKey(target.getType());
        return key == null ? "" : normalizeTargetClass(key.toString());
    }

    private static String normalizeTargetClass(String targetClass) {
        return targetClass == null ? "" : targetClass.trim().toLowerCase(Locale.ROOT);
    }
}
