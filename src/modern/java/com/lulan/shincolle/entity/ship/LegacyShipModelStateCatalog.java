package com.lulan.shincolle.entity.ship;

/**
 * Exact Display.State equipment visibility rules for the audited 1.12 Hime models.
 * Unknown parts remain visible because the old model leaves them untouched.
 */
public final class LegacyShipModelStateCatalog {

    private LegacyShipModelStateCatalog() {
    }

    public static boolean isPartVisible(String modelSourceStem, String partName, int modelState) {
        return switch (modelSourceStem) {
            case "ModelAirfieldHime" -> visibleForBit(partName, modelState, 1,
                    "EquipHand01", "EquipHand02");
            case "ModelMidwayHime" -> midwayPartVisible(partName, modelState);
            case "ModelNorthernHime" -> northernPartVisible(partName, modelState);
            case "ModelCarrierWDemon" -> visibleForBit(partName, modelState, 1,
                    "EquipBase");
            case "ModelBattleshipHime", "ModelHarbourHime" -> true;
            case "ModelSubmU511" -> visibleForBit(partName, modelState, 0,
                    "EquipBase")
                    && visibleForBit(partName, modelState, 1, "Hat01")
                    && visibleForBit(partName, modelState, 2, "Pipe");
            case "ModelSubmRo500" -> visibleForBit(partName, modelState, 0,
                    "EquipBase1")
                    && visibleForBit(partName, modelState, 1, "EquipBase2")
                    && visibleForBit(partName, modelState, 2, "FlowerBase");
            case "ModelSubmHime" -> visibleForBit(partName, modelState, 1, "Collar01");
            case "ModelSSNH" -> ssnhPartVisible(partName, modelState);
            case "ModelBBKongou" -> kongouPartVisible(partName, modelState);
            case "ModelBBHiei", "ModelBBHaruna" -> visibleForBit(partName, modelState, 0,
                    "EquipBase")
                    && visibleForBit(partName, modelState, 1, "EquipHeadBase");
            case "ModelBBKirishima" -> visibleForBit(partName, modelState, 0,
                    "EquipBase")
                    && visibleForBit(partName, modelState, 1, "EquipHeadBase")
                    && visibleForBit(partName, modelState, 2, "EquipGlass01");
            case "ModelBattleshipYamato" -> visibleForBit(partName, modelState, 0,
                    "EquipBaseBelt")
                    && visibleForBit(partName, modelState, 1, "EquipHeadBase")
                    && visibleForBit(partName, modelState, 2, "EquipU01")
                    && visibleForBit(partName, modelState, 3, "EquipLegR01", "EquipLegL01");
            case "ModelCarrierKaga" -> kagaPartVisible(partName, modelState);
            case "ModelCarrierAkagi" -> akagiPartVisible(partName, modelState);
            case "ModelCAHime" -> caHimePartVisible(partName, modelState);
            case "ModelDestroyerAkatsuki" -> akatsukiPartVisible(partName, modelState);
            case "ModelDestroyerHibiki" -> hibikiPartVisible(partName, modelState);
            case "ModelDestroyerIkazuchi" -> ikazuchiPartVisible(partName, modelState);
            case "ModelDestroyerInazuma" -> visibleForBit(partName, modelState, 0,
                    "EquipBase");
            case "ModelCruiserTenryuu" -> tenryuuPartVisible(partName, modelState);
            case "ModelCruiserTatsuta" -> tatsutaPartVisible(partName, modelState);
            case "ModelCruiserAtago", "ModelCruiserTakao" -> heavyCruiserPartVisible(partName, modelState);
            case "ModelDestroyerHime" -> destroyerPartVisible(partName, modelState);
            case "ModelDestroyerShimakaze" -> shimakazePartVisible(partName, modelState);
            case "ModelBattleshipNagato" -> nagatoPartVisible(partName, modelState);
            case "ModelIsolatedHime" -> isolatedPartVisible(partName, modelState);
            default -> true;
        };
    }

    private static boolean midwayPartVisible(String partName, int modelState) {
        if (isNamed(partName, "EquipSR01", "EquipSR01b", "EquipSR01c")) {
            return false;
        }
        return visibleForBit(partName, modelState, 1, "Collar01");
    }

    private static boolean northernPartVisible(String partName, int modelState) {
        return visibleForBit(partName, modelState, 0, "GlowEquipBase", "EquipBase")
                && visibleForBit(partName, modelState, 1, "SantaCloth01", "SantaHat01")
                && visibleForBit(partName, modelState, 2, "EquipUmbre01a")
                && visibleForBit(partName, modelState, 3, "ShoesL", "ShoesL2", "ShoesR");
    }

    private static boolean shimakazePartVisible(String partName, int modelState) {
        if (isNamed(partName, "EarBase")) {
            return hasBit(modelState, 3) || hasBit(modelState, 4) || hasBit(modelState, 5);
        }
        return visibleForBit(partName, modelState, 1, "EquipBase")
                && visibleForBit(partName, modelState, 2, "HairAnchor");
    }

    private static boolean nagatoPartVisible(String partName, int modelState) {
        return visibleForBit(partName, modelState, 0, "HeadEquip", "HeadEquip05")
                && visibleForBit(partName, modelState, 1, "EquipBase");
    }

    private static boolean destroyerPartVisible(String partName, int modelState) {
        return visibleForBit(partName, modelState, 0, "EquipBaseL", "EquipBaseR")
                && visibleForBit(partName, modelState, 1, "Hat01")
                && visibleForBit(partName, modelState, 2, "Cannon01")
                && visibleForBit(partName, modelState, 3, "BeltBase")
                && visibleForBit(partName, modelState, 4, "LegLeft01", "LegRight01")
                && visibleForBit(partName, modelState, 5, "ArmLeft02a", "ArmRight02a");
    }

    private static boolean isolatedPartVisible(String partName, int modelState) {
        if (isNamed(partName, "LegLeft02b", "LegRight02b")) {
            return hasBit(modelState, 6);
        }
        if (isNamed(partName, "LegLeft02a", "LegRight02a")) {
            return !hasBit(modelState, 6);
        }

        return visibleForBit(partName, modelState, 1, "HatBase", "GlowHatBase")
                && visibleForBit(partName, modelState, 2, "HeadH1", "HeadH4")
                && visibleForBit(partName, modelState, 3, "Cloth01a")
                && visibleForBit(partName, modelState, 4, "Cloth02a", "Cloth02b", "Cloth02c")
                && visibleForBit(partName, modelState, 5, "Cloth03a", "Cloth03b")
                && visibleForBit(partName, modelState, 7, "LegArmor01a", "LegArmor02a");
    }

    private static boolean ssnhPartVisible(String partName, int modelState) {
        if (isNamed(partName, "EquipTBase_2")) {
            return false;
        }

        return visibleForBit(partName, modelState, 0, "EquipHandRing")
                && visibleForBit(partName, modelState, 1, "RingBase")
                && visibleForBit(partName, modelState, 2, "EquipTBase");
    }

    private static boolean kongouPartVisible(String partName, int modelState) {
        if (isNamed(partName, "Ahoke00")) {
            return hasBit(modelState, 3);
        }
        if (isNamed(partName, "Ahoke01")) {
            return !hasBit(modelState, 3);
        }

        return visibleForBit(partName, modelState, 0, "EquipBase")
                && visibleForBit(partName, modelState, 1, "EquipHeadBase")
                && visibleForBit(partName, modelState, 2,
                "HairS01", "HairS02", "HairCBase", "HairCBaseB");
    }

    private static boolean kagaPartVisible(String partName, int modelState) {
        return visibleForBit(partName, modelState, 0, "EquipE01", "EquipGlove")
                && visibleForBit(partName, modelState, 1, "EquipABase")
                && visibleForBit(partName, modelState, 2, "EquipD01")
                && visibleForBit(partName, modelState, 3, "EquipC01")
                && visibleForBit(partName, modelState, 4, "EquipB01")
                && visibleForBit(partName, modelState, 5, "EquipS01")
                && visibleForBit(partName, modelState, 6, "Ear01", "Ear02", "Tail01")
                && visibleForBit(partName, modelState, 7, "EquipSL01", "EquipSR01");
    }

    private static boolean akagiPartVisible(String partName, int modelState) {
        return visibleForBit(partName, modelState, 0, "EquipE01", "EquipGlove")
                && visibleForBit(partName, modelState, 1, "EquipABase")
                && visibleForBit(partName, modelState, 2, "EquipD01")
                && visibleForBit(partName, modelState, 3, "EquipC01")
                && visibleForBit(partName, modelState, 4, "EquipB01")
                && visibleForBit(partName, modelState, 5, "EquipS01")
                && visibleForBit(partName, modelState, 6, "Ear01", "Ear02", "Tail01")
                && visibleForBit(partName, modelState, 7, "EquipSL01", "EquipSR01");
    }

    private static boolean caHimePartVisible(String partName, int modelState) {
        if (isNamed(partName, "TailBase")) {
            return hasBit(modelState, 0) || hasBit(modelState, 1);
        }

        boolean fh1 = hasBit(modelState, 2);
        boolean fh2 = hasBit(modelState, 3);
        boolean fh3 = hasBit(modelState, 4);
        if (fh2 || fh3 || (fh1 && fh2)) {
            return !isNamed(partName, "Hair01");
        }
        if (fh1) {
            return !isNamed(partName, "Hair01", "Horn01", "Horn02", "Ear01", "Ear02");
        }
        return !isNamed(partName, "HatBase");
    }

    private static boolean akatsukiPartVisible(String partName, int modelState) {
        return visibleForBit(partName, modelState, 0, "EquipBase")
                && visibleForBit(partName, modelState, 1, "HatBase")
                && visibleForBit(partName, modelState, 2, "EquipC12")
                && visibleForBit(partName, modelState, 3, "EquipTR03", "EquipTL03");
    }

    private static boolean hibikiPartVisible(String partName, int modelState) {
        if (isNamed(partName, "EquipBase")) {
            return hasBit(modelState, 0);
        }
        if (isNamed(partName, "EquipTL03")) {
            return hasBit(modelState, 1);
        }

        if (!isNamed(partName, "HatBase", "Hair02f1", "Hair01", "HatBase2")) {
            return true;
        }

        boolean fh1 = hasBit(modelState, 2);
        boolean fh2 = hasBit(modelState, 3);
        boolean fh3 = hasBit(modelState, 4);
        if (fh1) {
            return isNamed(partName, "HatBase", "Hair02f1", "Hair01");
        }
        if (fh2 && fh3) {
            return isNamed(partName, "HatBase2");
        }
        if (fh2 || fh3) {
            return isNamed(partName, "Hair01", "HatBase2");
        }
        return isNamed(partName, "Hair02f1", "Hair01");
    }

    private static boolean ikazuchiPartVisible(String partName, int modelState) {
        return visibleForBit(partName, modelState, 0, "EquipBase")
                && visibleForBit(partName, modelState, 1, "EquipHead01");
    }

    private static boolean tenryuuPartVisible(String partName, int modelState) {
        return visibleForBit(partName, modelState, 0, "Equip00", "GlowEquip00")
                && visibleForBit(partName, modelState, 1, "EarL01", "EarR01")
                && visibleForBit(partName, modelState, 2, "EquipSL00", "EquipSR01")
                && visibleForBit(partName, modelState, 3, "EyeMask")
                && visibleForBit(partName, modelState, 4, "ShoeL02", "ShoeR02");
    }

    private static boolean tatsutaPartVisible(String partName, int modelState) {
        return visibleForBit(partName, modelState, 0, "Equip00", "GlowEquip00")
                && visibleForBit(partName, modelState, 1, "CirBase")
                && visibleForBit(partName, modelState, 2, "EquipSL00");
    }

    private static boolean heavyCruiserPartVisible(String partName, int modelState) {
        return visibleForBit(partName, modelState, 0, "EquipBase")
                && visibleForBit(partName, modelState, 1, "EquipBag00")
                && visibleForBit(partName, modelState, 2, "Hat01")
                && visibleForBit(partName, modelState, 3,
                "ShoeL01", "ShoeR01", "ShoeL03", "ShoeL03_1");
    }

    private static boolean visibleForBit(String partName, int modelState, int bit,
                                          String... controlledParts) {
        return !isNamed(partName, controlledParts) || hasBit(modelState, bit);
    }

    private static boolean hasBit(int modelState, int bit) {
        return (modelState & (1 << bit)) != 0;
    }

    private static boolean isNamed(String partName, String... names) {
        for (String name : names) {
            if (name.equals(partName)) {
                return true;
            }
        }
        return false;
    }
}
