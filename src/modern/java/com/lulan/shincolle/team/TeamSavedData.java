package com.lulan.shincolle.team;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class TeamSavedData extends SavedData {

    private static final String DATA_NAME = "shincolle_team_registry";
    private static final String TEAMS_TAG = "Teams";

    private final Map<Integer, TeamData> teamById = new HashMap<>();

    public static TeamSavedData get(ServerLevel level) {
        return level.getServer().overworld().getDataStorage().computeIfAbsent(
                TeamSavedData::load,
                TeamSavedData::new,
                DATA_NAME);
    }

    public static TeamSavedData load(CompoundTag tag) {
        TeamSavedData data = new TeamSavedData();
        ListTag list = tag.getList(TEAMS_TAG, Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            TeamData team = TeamData.fromTag(list.getCompound(i));
            if (team.getTeamId() > 0) {
                data.teamById.put(team.getTeamId(), team);
            }
        }
        return data;
    }

    public TeamData getOrCreateTeam(int teamId, String leaderName) {
        if (teamId <= 0) {
            return new TeamData();
        }

        TeamData existing = this.teamById.get(teamId);
        if (existing != null) {
            if (existing.getLeaderName().isBlank() && leaderName != null && !leaderName.isBlank()) {
                existing.setLeaderName(leaderName);
                if (existing.getTeamName().isBlank()) {
                    existing.setTeamName(defaultTeamName(leaderName));
                }
                this.setDirty();
            }
            return existing;
        }

        String safeLeader = leaderName == null ? "" : leaderName;
        TeamData created = new TeamData(teamId, defaultTeamName(safeLeader), safeLeader);
        this.teamById.put(teamId, created);
        this.setDirty();
        return created;
    }

    public TeamData createTeam(int teamId, String leaderName, String teamName) {
        if (teamId <= 0) {
            return new TeamData();
        }

        TeamData team = this.getOrCreateTeam(teamId, leaderName);
        if (teamName != null && !teamName.isBlank()) {
            team.setTeamName(teamName.trim());
            this.setDirty();
        }
        return team;
    }

    public boolean removeTeam(int teamId) {
        if (teamId <= 0 || !this.teamById.containsKey(teamId)) {
            return false;
        }

        this.teamById.remove(teamId);
        for (TeamData other : this.teamById.values()) {
            other.removeAlly(teamId);
            other.removeBanned(teamId);
        }
        this.setDirty();
        return true;
    }

    public boolean hasTeam(int teamId) {
        return teamId > 0 && this.teamById.containsKey(teamId);
    }

    public TeamData getTeam(int teamId) {
        return this.teamById.get(teamId);
    }

    public Collection<TeamData> getAllTeams() {
        return this.teamById.values();
    }

    public boolean addAllyRelationship(int selfTeamId, int targetTeamId) {
        TeamData self = this.teamById.get(selfTeamId);
        if (self == null || !this.teamById.containsKey(targetTeamId)) {
            return false;
        }

        boolean changed = self.addAlly(targetTeamId);
        if (changed) {
            this.setDirty();
        }
        return changed;
    }

    public boolean removeAllyRelationship(int selfTeamId, int targetTeamId) {
        TeamData self = this.teamById.get(selfTeamId);
        TeamData target = this.teamById.get(targetTeamId);
        if (self == null || target == null) {
            return false;
        }

        boolean changedA = self.removeAlly(targetTeamId);
        boolean changedB = target.removeAlly(selfTeamId);
        if (changedA || changedB) {
            this.setDirty();
        }
        return changedA || changedB;
    }

    public boolean addBannedRelationship(int selfTeamId, int targetTeamId) {
        TeamData self = this.teamById.get(selfTeamId);
        TeamData target = this.teamById.get(targetTeamId);
        if (self == null || target == null) {
            return false;
        }

        boolean changedA = self.addBanned(targetTeamId);
        boolean changedB = target.addBanned(selfTeamId);
        if (changedA || changedB) {
            self.removeAlly(targetTeamId);
            target.removeAlly(selfTeamId);
            this.setDirty();
        }
        return changedA || changedB;
    }

    public boolean removeBannedRelationship(int selfTeamId, int targetTeamId) {
        TeamData self = this.teamById.get(selfTeamId);
        if (self == null) {
            return false;
        }

        boolean changed = self.removeBanned(targetTeamId);
        if (changed) {
            this.setDirty();
        }
        return changed;
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        ListTag list = new ListTag();
        for (TeamData teamData : this.teamById.values()) {
            if (teamData.getTeamId() <= 0) {
                continue;
            }

            list.add(teamData.saveToTag(new CompoundTag()));
        }
        tag.put(TEAMS_TAG, list);
        return tag;
    }

    private static String defaultTeamName(String leaderName) {
        if (leaderName == null || leaderName.isBlank()) {
            return "Fleet";
        }
        return leaderName + "'s Fleet";
    }
}
