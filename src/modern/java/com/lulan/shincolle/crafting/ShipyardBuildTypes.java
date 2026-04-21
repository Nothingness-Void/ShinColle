package com.lulan.shincolle.crafting;

import net.minecraft.network.chat.Component;

public final class ShipyardBuildTypes {

    public static final int NONE = 0;
    public static final int SHIP = 1;
    public static final int EQUIP = 2;
    public static final int SHIP_LOOP = 3;
    public static final int EQUIP_LOOP = 4;

    private ShipyardBuildTypes() {
    }

    public static boolean isLoopMode(int buildType) {
        return buildType == SHIP_LOOP || buildType == EQUIP_LOOP;
    }

    public static boolean isShipMode(int buildType) {
        return buildType == SHIP || buildType == SHIP_LOOP;
    }

    public static boolean isEquipMode(int buildType) {
        return buildType == EQUIP || buildType == EQUIP_LOOP;
    }

    public static int cycleShipMode(int buildType) {
        return switch (buildType) {
            case SHIP -> SHIP_LOOP;
            case SHIP_LOOP -> NONE;
            default -> SHIP;
        };
    }

    public static int cycleEquipMode(int buildType) {
        return switch (buildType) {
            case EQUIP -> EQUIP_LOOP;
            case EQUIP_LOOP -> NONE;
            default -> EQUIP;
        };
    }

    public static Component label(int buildType) {
        return Component.translatable(labelKey(buildType));
    }

    public static String labelKey(int buildType) {
        return switch (buildType) {
            case SHIP -> "gui.shincolle.shipyard.mode.ship";
            case EQUIP -> "gui.shincolle.shipyard.mode.equip";
            case SHIP_LOOP -> "gui.shincolle.shipyard.mode.ship_loop";
            case EQUIP_LOOP -> "gui.shincolle.shipyard.mode.equip_loop";
            default -> "gui.shincolle.shipyard.mode.none";
        };
    }
}
