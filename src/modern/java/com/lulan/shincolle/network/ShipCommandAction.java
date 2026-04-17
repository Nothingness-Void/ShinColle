package com.lulan.shincolle.network;

public enum ShipCommandAction {
    MOVE_TO_POS,
    GUARD_ENTITY,
    ATTACK_ENTITY,
    STOP_COMMAND,
    SET_AI_FLAGS,
    SET_FOLLOW_RANGE,
    TOGGLE_SIT,
    OPEN_SHIP_INVENTORY;

    public static ShipCommandAction fromOrdinal(int ordinal) {
        ShipCommandAction[] values = values();
        if (ordinal < 0 || ordinal >= values.length) {
            return STOP_COMMAND;
        }
        return values[ordinal];
    }
}
