package com.lulan.shincolle.entity.ship;

public record LegacyShipAttackProfile(boolean melee, boolean light, boolean heavy, boolean airLight, boolean airHeavy) {

    public static final LegacyShipAttackProfile MELEE_ONLY = new LegacyShipAttackProfile(true, false, false, false, false);

    public static LegacyShipAttackProfile resolve(ShipEntitySpec spec) {
        String stem = spec.textureStem();

        return switch (stem) {
            case "EntityTransportWa" -> MELEE_ONLY;
            case "EntityCarrierAkagi", "EntityCarrierKaga", "EntityCarrierWo", "EntityCarrierHime" ->
                    new LegacyShipAttackProfile(true, false, false, true, true);
            case "EntityCarrierWDemon" ->
                    new LegacyShipAttackProfile(true, true, false, true, true);
            case "EntityAirfieldHime", "EntityHarbourHime", "EntityIsolatedHime", "EntityMidwayHime", "EntityNorthernHime" ->
                    new LegacyShipAttackProfile(true, true, true, true, true);
            default -> switch (spec.archetype()) {
                case CARRIER -> new LegacyShipAttackProfile(true, false, false, true, true);
                case TRANSPORT -> MELEE_ONLY;
                case INSTALLATION -> new LegacyShipAttackProfile(true, true, true, true, true);
                default -> new LegacyShipAttackProfile(true, true, true, false, false);
            };
        };
    }

    public boolean hasRangedAttack() {
        return this.light || this.heavy || this.airLight || this.airHeavy;
    }

    public boolean hasAirAttack() {
        return this.airLight || this.airHeavy;
    }
}
