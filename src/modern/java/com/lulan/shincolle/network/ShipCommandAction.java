package com.lulan.shincolle.network;

public enum ShipCommandAction {
    MOVE_TO_POS,
    GUARD_ENTITY,
    ATTACK_ENTITY,
    STOP_COMMAND,
    SET_AI_FLAGS,
    SET_FOLLOW_RANGE,
    TOGGLE_SIT,
    OPEN_SHIP_INVENTORY,
    TOGGLE_RING_EFFECT,
    SET_WAYPOINT_STAY,
    SET_TIME_KEEPER,
    SET_AUTO_PUMP,
    SET_COMBAT_RATION_LEVEL,
    SET_FOLLOW_MIN;

    public static ShipCommandAction fromOrdinal(int ordinal) {
        ShipCommandAction[] values = values();
        if (ordinal < 0 || ordinal >= values.length) {
            return STOP_COMMAND;
        }
        return values[ordinal];
    }
}
