package com.lulan.shincolle.item.equipment;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public enum LegacyEquipmentFamily {
    AIRPLANE("equipairplane", keys(
            "AIR_T_LO:AIRCRAFT_TMK1",
            "AIR_T_LO:AIRCRAFT_TMK2",
            "AIR_T_LO:AIRCRAFT_TMK3",
            "AIR_T_HI:AIRCRAFT_TAVENGER",
            "AIR_F_LO:AIRCRAFT_FMK1",
            "AIR_F_LO:AIRCRAFT_FMK2",
            "AIR_F_LO:AIRCRAFT_FMK3",
            "AIR_F_HI:AIRCRAFT_FFLYFISH",
            "AIR_F_HI:AIRCRAFT_FHELLCAT",
            "AIR_B_LO:AIRCRAFT_BMK1",
            "AIR_B_LO:AIRCRAFT_BMK2",
            "AIR_B_HI:AIRCRAFT_BFLYFISH",
            "AIR_B_HI:AIRCRAFT_BHELL",
            "AIR_R_LO:AIRCRAFT_R",
            "AIR_R_HI:AIRCRAFT_RFLYFISH",
            "AIR_T_HI:AIRCRAFT_TAVENGERK",
            "AIR_F_HI:AIRCRAFT_FHELLCATK",
            "AIR_B_HI:AIRCRAFT_BHELLK",
            "AIR_F_HI:AIRCRAFT_FHELLCATB",
            "AIR_B_HI:AIRCRAFT_BLAND",
            "AIR_B_HI:AIRCRAFT_BLANDA",
            "AIR_F_HI:AIRCRAFT_FBC"),
            new int[]{0, 1, 2, 3, 15, 4, 5, 6, 7, 8, 18, 16, 21, 9, 10, 11, 12, 17, 19, 20, 13, 14}),
    AMMO("equipammo", keys(
            "AMMO_LO:AMMO_T91",
            "AMMO_HI:AMMO_T1",
            "AMMO_LO:AMMO_AA",
            "AMMO_HI:AMMO_T3",
            "AMMO_HI:AMMO_DU",
            "AMMO_HI:AMMO_G",
            "AMMO_HI:AMMO_AG",
            "AMMO_HI:AMMO_E",
            "AMMO_HI:AMMO_C")),
    ARMOR("equiparmor", keys(
            "ARMOR_LO:ARMOR",
            "ARMOR_HI:ARMOR_ENH",
            "ARMOR_LO:ARMOR_ATBS",
            "ARMOR_LO:ARMOR_ATBM",
            "ARMOR_HI:ARMOR_ATBL",
            "ARMOR_LO:ARMOR_ATBA",
            "ARMOR_HI:ARMOR_APB"),
            new int[]{0, 1, 5, 2, 3, 4, 6}),
    CANNON("equipcannon", keys(
            "CANNON_SI:CANNON_SINGLE_5",
            "CANNON_SI:CANNON_SINGLE_6",
            "CANNON_TW_LO:CANNON_TWIN_5",
            "CANNON_TW_LO:CANNON_TWIN_6",
            "CANNON_TW_LO:CANNON_TWIN_5DP",
            "CANNON_TW_LO:CANNON_TWIN_125",
            "CANNON_TW_HI:CANNON_TWIN_14",
            "CANNON_TW_HI:CANNON_TWIN_16",
            "CANNON_TW_HI:CANNON_TWIN_20",
            "CANNON_TR:CANNON_TRI_8",
            "CANNON_TR:CANNON_TRI_16",
            "CANNON_TR:CANNON_FG_15",
            "CANNON_SI:CANNON_CG_5",
            "CANNON_TW_LO:CANNON_TWIN_8",
            "CANNON_TR:CANNON_QUAD_15",
            "CANNON_TR:CANNON_TRI_12"),
            new int[]{0, 1, 12, 2, 3, 4, 13, 5, 6, 7, 8, 11, 9, 15, 10, 14}),
    CATAPULT("equipcatapult", keys(
            "CATAPULT_LO:CATAPULT_F",
            "CATAPULT_LO:CATAPULT_H",
            "CATAPULT_HI:CATAPULT_C",
            "CATAPULT_HI:CATAPULT_E")),
    COMPASS("equipcompass", keys("COMPASS_LO:COMPASS")),
    DRUM("equipdrum", keys(
            "DRUM_LO:DRUM",
            "DRUM_LO:DRUM_F",
            "DRUM_LO:DRUM_E")),
    FLARE("equipflare", keys("FLARE_LO:FLARE")),
    MACHINEGUN("equipmachinegun", keys(
            "GUN_LO:GUN_HA_3",
            "GUN_LO:GUN_HA_5",
            "GUN_LO:GUN_SINGLE_12",
            "GUN_LO:GUN_SINGLE_20",
            "GUN_HI:GUN_TWIN_40",
            "GUN_HI:GUN_QUAD_40",
            "GUN_HI:GUN_TWIN_4_CIC")),
    RADAR("equipradar", keys(
            "RADAR_LO:RADAR_AIRMK1",
            "RADAR_LO:RADAR_AIRMK2",
            "RADAR_LO:RADAR_SURMK1",
            "RADAR_LO:RADAR_SURMK2",
            "RADAR_LO:RADAR_SONAR",
            "RADAR_HI:RADAR_AIRABYSS",
            "RADAR_HI:RADAR_SURABYSS",
            "RADAR_HI:RADAR_SONARMK2",
            "RADAR_HI:RADAR_FCSCIC"),
            new int[]{0, 1, 5, 2, 3, 6, 4, 7, 8}),
    SEARCHLIGHT("equipsearchlight", keys("SEARCHLIGHT_LO:SEARCHLIGHT")),
    TORPEDO("equiptorpedo", keys(
            "TORPEDO_LO:TORPEDO_21MK1",
            "TORPEDO_LO:TORPEDO_21MK2",
            "TORPEDO_LO:TORPEDO_22MK1",
            "TORPEDO_HI:TORPEDO_CUTTLEFISH",
            "TORPEDO_HI:TORPEDO_HIGHSPEED",
            "TORPEDO_HI:TORPEDO_HIGHSPEED2",
            "TORPEDO_HI:TORPEDO_AMB")),
    TURBINE("equipturbine", keys(
            "TURBINE_LO:TURBINE",
            "TURBINE_LO:TURBINE_IMP",
            "TURBINE_HI:TURBINE_ENH",
            "TURBINE_HI:TURBINE_GE",
            "TURBINE_HI:TURBINE_GENEW"));

    private final String baseName;
    private final String[] definitionKeys;
    private final int[] displayOrder;

    LegacyEquipmentFamily(String baseName, String[] definitionKeys) {
        this(baseName, definitionKeys, null);
    }

    LegacyEquipmentFamily(String baseName, String[] definitionKeys, int[] displayOrder) {
        this.baseName = baseName;
        this.definitionKeys = definitionKeys;
        this.displayOrder = displayOrder == null ? createSequentialOrder(definitionKeys.length) : displayOrder;
    }

    public String registryName(int index) {
        return index == 0 ? this.baseName : this.baseName + index;
    }

    public int variantCount() {
        return this.definitionKeys.length;
    }

    public String definitionKey(int index) {
        return this.definitionKeys[index];
    }

    public int[] displayOrder() {
        return this.displayOrder.clone();
    }

    public int enchantability(int index) {
        String type = this.definitionKey(index).split(":", 2)[0];
        return switch (type) {
            case "CANNON_TW_LO" -> 12;
            case "CANNON_TW_HI" -> 18;
            case "CANNON_TR" -> 25;
            case "TORPEDO_LO" -> 16;
            case "TORPEDO_HI" -> 22;
            case "AIR_T_LO", "AIR_F_LO", "AIR_B_LO", "AIR_R_LO", "CATAPULT_LO", "TURBINE_LO" -> 18;
            case "AIR_T_HI", "AIR_F_HI", "AIR_B_HI", "AIR_R_HI", "CATAPULT_HI", "TURBINE_HI", "AMMO_HI" -> 25;
            case "RADAR_LO", "GUN_LO", "AMMO_LO" -> 12;
            case "RADAR_HI", "GUN_HI" -> 15;
            case "ARMOR_HI" -> 20;
            default -> 9;
        };
    }

    public List<Component> extraTooltipLines(int index, ItemStack stack) {
        List<Component> lines = new ArrayList<>();

        switch (this) {
            case COMPASS -> lines.add(Component.translatable("gui.shincolle.compass").withStyle(ChatFormatting.GRAY));
            case DRUM -> {
                if (index == 1) {
                    lines.add(Component.translatable("gui.shincolle.drum1").withStyle(ChatFormatting.GRAY));
                } else if (index == 2) {
                    lines.add(Component.translatable("gui.shincolle.drum2b").withStyle(ChatFormatting.GRAY));
                } else {
                    lines.add(Component.translatable("gui.shincolle.drum").withStyle(ChatFormatting.GRAY));
                }
            }
            case FLARE -> lines.add(Component.translatable("gui.shincolle.flare").withStyle(ChatFormatting.GRAY));
            case SEARCHLIGHT -> lines.add(Component.translatable("gui.shincolle.searchlight").withStyle(ChatFormatting.GRAY));
            case TORPEDO -> {
                int speedLevel = torpedoSpeedLevel(index);
                if (speedLevel > 0) {
                    lines.add(Component.translatable("gui.shincolle.equip.torpedospeed", speedLevel).withStyle(ChatFormatting.YELLOW));
                }
            }
            case AMMO -> {
                if (index == 5) {
                    lines.add(Component.translatable("gui.shincolle.equip.gravity").withStyle(ChatFormatting.YELLOW));
                } else if (index == 8) {
                    lines.add(Component.translatable("gui.shincolle.equip.cluster").withStyle(ChatFormatting.YELLOW));
                }
            }
            default -> {
            }
        }

        return lines;
    }

    private static int torpedoSpeedLevel(int index) {
        return switch (index) {
            case 3, 4 -> 1;
            case 5 -> 2;
            case 6 -> 3;
            default -> 0;
        };
    }

    private static int[] createSequentialOrder(int length) {
        int[] order = new int[length];

        for (int i = 0; i < length; i++) {
            order[i] = i;
        }

        return order;
    }

    private static String[] keys(String... values) {
        return values;
    }
}
