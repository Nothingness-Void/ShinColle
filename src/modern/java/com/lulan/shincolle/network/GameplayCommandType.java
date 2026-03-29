package com.lulan.shincolle.network;

public enum GameplayCommandType {
    TOGGLE_SIT_SINGLE,
    TOGGLE_SIT_GROUP,
    CYCLE_FORMATION,
    SET_CURRENT_TEAM,
    ASSIGN_SHIP_SLOT,
    SWAP_TEAM_SLOT,
    TOGGLE_SHIP_SELECT,
    SET_SLOT_SELECTION,
    CLEAR_CURRENT_TEAM,
    MOVE_TO_POS,
    GUARD_ENTITY,
    ATTACK_ENTITY,
    STOP_COMMAND,
    TOGGLE_TARGET_CLASS,
    TARGET_CLASS_ADD,
    TARGET_CLASS_REMOVE,
    OPEN_SHIP_INVENTORY,
    OPEN_FORMATION_SCREEN,
    OPEN_DESK_SCREEN,
    OPEN_MORPH_SCREEN,
    SET_SHIP_AI_FLAGS,
    SET_SHIP_FOLLOW_RANGE,
    MORPH_CYCLE_PROFILE_PREV,
    MORPH_CYCLE_PROFILE_NEXT,
    MORPH_TOGGLE_ACTIVE,
    MORPH_TOGGLE_MOUNT,
    MORPH_CAST_ATTACK,
    MORPH_CAST_SPECIAL,
    DESK_CREATE_TEAM,
    DESK_DISBAND_TEAM,
    DESK_RENAME_TEAM,
    DESK_ADD_ALLY,
    DESK_REMOVE_ALLY,
    DESK_ADD_BANNED,
    DESK_REMOVE_BANNED,
    OPTOOL_TOGGLE_UNATTACKABLE,
    OPTOOL_PRINT_UNATTACKABLE;

    public static GameplayCommandType fromOrdinal(int ordinal) {
        GameplayCommandType[] values = values();
        if (ordinal < 0 || ordinal >= values.length) {
            return STOP_COMMAND;
        }
        return values[ordinal];
    }
}
