package com.lulan.shincolle.team;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;

import java.util.ArrayList;
import java.util.List;

public class TeamData {

    private static final String TEAM_ID_TAG = "TeamId";
    private static final String TEAM_NAME_TAG = "TeamName";
    private static final String LEADER_NAME_TAG = "LeaderName";
    private static final String ALLY_LIST_TAG = "Allies";
    private static final String BANNED_LIST_TAG = "Banned";

    private int teamId;
    private String teamName;
    private String leaderName;
    private final List<Integer> allies = new ArrayList<>();
    private final List<Integer> banned = new ArrayList<>();

    public TeamData() {
        this(0, "", "");
    }

    public TeamData(int teamId, String teamName, String leaderName) {
        this.teamId = Math.max(0, teamId);
        this.teamName = teamName == null ? "" : teamName;
        this.leaderName = leaderName == null ? "" : leaderName;
    }

    public CompoundTag saveToTag(CompoundTag tag) {
        tag.putInt(TEAM_ID_TAG, this.teamId);
        tag.putString(TEAM_NAME_TAG, this.teamName);
        tag.putString(LEADER_NAME_TAG, this.leaderName);
        tag.putIntArray(ALLY_LIST_TAG, this.allies);
        tag.putIntArray(BANNED_LIST_TAG, this.banned);
        return tag;
    }

    public void loadFromTag(CompoundTag tag) {
        this.teamId = Math.max(0, tag.getInt(TEAM_ID_TAG));
        this.teamName = tag.getString(TEAM_NAME_TAG);
        this.leaderName = tag.getString(LEADER_NAME_TAG);

        this.allies.clear();
        for (int id : tag.getIntArray(ALLY_LIST_TAG)) {
            this.addAlly(id);
        }

        this.banned.clear();
        for (int id : tag.getIntArray(BANNED_LIST_TAG)) {
            this.addBanned(id);
        }
    }

    public static TeamData fromTag(CompoundTag tag) {
        TeamData data = new TeamData();
        data.loadFromTag(tag);
        return data;
    }

    public int getTeamId() {
        return this.teamId;
    }

    public void setTeamId(int teamId) {
        this.teamId = Math.max(0, teamId);
    }

    public String getTeamName() {
        return this.teamName;
    }

    public void setTeamName(String teamName) {
        this.teamName = teamName == null ? "" : teamName;
    }

    public String getLeaderName() {
        return this.leaderName;
    }

    public void setLeaderName(String leaderName) {
        this.leaderName = leaderName == null ? "" : leaderName;
    }

    public List<Integer> getAllies() {
        return List.copyOf(this.allies);
    }

    public List<Integer> getBanned() {
        return List.copyOf(this.banned);
    }

    public boolean isAlly(int teamId) {
        return teamId > 0 && this.allies.contains(teamId);
    }

    public boolean isBanned(int teamId) {
        return teamId > 0 && this.banned.contains(teamId);
    }

    public boolean addAlly(int teamId) {
        if (teamId <= 0 || teamId == this.teamId || this.banned.contains(teamId) || this.allies.contains(teamId)) {
            return false;
        }

        this.allies.add(teamId);
        return true;
    }

    public boolean removeAlly(int teamId) {
        return this.allies.remove((Integer) teamId);
    }

    public boolean addBanned(int teamId) {
        if (teamId <= 0 || teamId == this.teamId || this.allies.contains(teamId) || this.banned.contains(teamId)) {
            return false;
        }

        this.banned.add(teamId);
        return true;
    }

    public boolean removeBanned(int teamId) {
        return this.banned.remove((Integer) teamId);
    }
}

