package com.lulan.shincolle.entity.ship;

import com.lulan.shincolle.item.equipment.LegacyEquipmentFamily;
import com.lulan.shincolle.item.equipment.LegacyEquipmentItem;
import net.minecraft.util.Mth;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;

public record ShipEquipmentBehaviorState(
        int airRadarLevel,
        int surfaceRadarLevel,
        int sonarLevel,
        int fcsLevel,
        int flareLevel,
        int searchlightLevel,
        int catapultLevel,
        int turbineLevel,
        int torpedoSpeedLevel,
        boolean autonomousRoute,
        int transportTier) {

    public static final ShipEquipmentBehaviorState EMPTY = new ShipEquipmentBehaviorState(
            0, 0, 0, 0, 0, 0, 0, 0, 0, false, 0);

    public static ShipEquipmentBehaviorState fromInventory(Container inventory, ShipArchetype archetype) {
        int airRadar = 0;
        int surfaceRadar = 0;
        int sonar = 0;
        int fcs = 0;
        int flare = 0;
        int searchlight = 0;
        int catapult = 0;
        int turbine = 0;
        int torpedoSpeed = 0;
        boolean autonomousRoute = false;
        int transport = 0;

        for (int slot = 0; slot < Math.min(LegacyShipEntity.EQUIPMENT_SLOT_COUNT, inventory.getContainerSize()); slot++) {
            ItemStack stack = inventory.getItem(slot);
            if (!(stack.getItem() instanceof LegacyEquipmentItem equipmentItem)) {
                continue;
            }

            int index = equipmentItem.variantIndex();
            switch (equipmentItem.family()) {
                case RADAR -> {
                    switch (index) {
                        case 0, 1 -> airRadar += 1;
                        case 2, 3 -> surfaceRadar += 1;
                        case 4 -> sonar += 1;
                        case 5 -> airRadar += 2;
                        case 6 -> surfaceRadar += 2;
                        case 7 -> sonar += 2;
                        case 8 -> {
                            fcs += 2;
                            surfaceRadar += 1;
                        }
                        default -> {
                        }
                    }
                }
                case FLARE -> flare += 1;
                case SEARCHLIGHT -> searchlight += 1;
                case CATAPULT -> catapult += switch (index) {
                    case 2 -> 2;
                    case 3 -> 3;
                    default -> 1;
                };
                case TURBINE -> turbine += switch (index) {
                    case 1 -> 2;
                    case 2 -> 3;
                    case 3 -> 4;
                    case 4 -> 5;
                    default -> 1;
                };
                case TORPEDO -> torpedoSpeed = Math.max(torpedoSpeed, torpedoSpeedLevel(index));
                case COMPASS -> autonomousRoute = true;
                case DRUM -> transport += switch (index) {
                    case 1, 2 -> 2;
                    default -> 1;
                };
                default -> {
                }
            }
        }

        return new ShipEquipmentBehaviorState(
                airRadar,
                surfaceRadar,
                sonar,
                fcs,
                flare,
                searchlight,
                catapult,
                turbine,
                torpedoSpeed,
                autonomousRoute,
                transport);
    }

    public int detectionRangeBonus() {
        return Mth.clamp(this.airRadarLevel * 4 + this.surfaceRadarLevel * 4 + this.sonarLevel * 6 + this.fcsLevel * 5 + this.searchlightLevel * 2,
                0,
                48);
    }

    public int detectionRangeBonusForTarget(boolean flying, boolean undersea) {
        int bonus = this.detectionRangeBonus();
        if (flying) {
            bonus += this.airRadarLevel * 4;
        } else if (undersea) {
            bonus += this.sonarLevel * 6;
        } else {
            bonus += this.surfaceRadarLevel * 3 + this.fcsLevel * 2;
        }
        return Mth.clamp(bonus, 0, 64);
    }

    public float rangedAccuracyBonus(boolean flying, boolean undersea, boolean darkCombat, boolean illuminated) {
        float bonus;
        if (flying) {
            bonus = this.airRadarLevel * 0.025F;
        } else if (undersea) {
            bonus = this.sonarLevel * 0.03F;
        } else {
            bonus = this.surfaceRadarLevel * 0.02F + this.fcsLevel * 0.025F;
        }

        if (darkCombat) {
            bonus += this.searchlightLevel * 0.025F;
        }

        if (illuminated) {
            bonus += this.flareLevel * 0.03F + this.searchlightLevel * 0.02F;
        }

        return Mth.clamp(bonus, 0.0F, 0.24F);
    }

    public float critBonus(boolean illuminated) {
        float bonus = this.fcsLevel * 0.015F;
        if (illuminated) {
            bonus += this.flareLevel * 0.01F;
        }
        return Mth.clamp(bonus, 0.0F, 0.10F);
    }

    public float illuminatedDodgePenalty(boolean darkCombat) {
        float penalty = this.flareLevel * 0.02F + this.searchlightLevel * 0.015F;
        if (darkCombat) {
            penalty += this.searchlightLevel * 0.015F;
        }
        return Mth.clamp(penalty, 0.0F, 0.16F);
    }

    public int aimTimeReductionTicks(LegacyShipAttackKind attackKind) {
        int reduction = this.fcsLevel * 2;
        if (attackKind == LegacyShipAttackKind.AIR_LIGHT || attackKind == LegacyShipAttackKind.AIR_HEAVY) {
            reduction += this.catapultLevel * 2 + this.airRadarLevel;
        } else if (attackKind != LegacyShipAttackKind.MELEE) {
            reduction += this.surfaceRadarLevel;
        }
        return Mth.clamp(reduction, 0, 12);
    }

    public float attackDelayMultiplier(LegacyShipAttackKind attackKind) {
        float reduction = 0.0F;
        if (attackKind == LegacyShipAttackKind.AIR_LIGHT || attackKind == LegacyShipAttackKind.AIR_HEAVY) {
            reduction += this.catapultLevel * 0.06F + this.airRadarLevel * 0.015F;
        } else if (attackKind == LegacyShipAttackKind.LIGHT || attackKind == LegacyShipAttackKind.HEAVY) {
            reduction += this.fcsLevel * 0.025F + this.surfaceRadarLevel * 0.015F;
        }
        return Mth.clamp(1.0F - reduction, 0.65F, 1.0F);
    }

    public double followSpeedMultiplier() {
        return 1.0D + Mth.clamp(this.turbineLevel * 0.035D, 0.0D, 0.22D);
    }

    public double commandSpeedMultiplier() {
        return 1.0D + Mth.clamp(this.turbineLevel * 0.045D, 0.0D, 0.30D);
    }

    public double projectileSpeedMultiplier(LegacyShipAttackKind attackKind) {
        double multiplier = 1.0D;
        if (attackKind == LegacyShipAttackKind.LIGHT || attackKind == LegacyShipAttackKind.HEAVY) {
            multiplier += this.torpedoSpeedLevel * 0.05D + this.fcsLevel * 0.02D;
        } else if (attackKind == LegacyShipAttackKind.AIR_LIGHT || attackKind == LegacyShipAttackKind.AIR_HEAVY) {
            multiplier += this.catapultLevel * 0.04D + this.airRadarLevel * 0.015D;
        }
        return Mth.clamp(multiplier, 1.0D, 1.35D);
    }

    public double airGuidanceBlend() {
        return Mth.clamp(0.18D + this.catapultLevel * 0.035D + this.airRadarLevel * 0.02D, 0.18D, 0.42D);
    }

    public int flareDurationTicks() {
        return 40 + this.flareLevel * 20;
    }

    public int searchlightDurationTicks() {
        return 50 + this.searchlightLevel * 20;
    }

    public int searchlightRange() {
        return 6 + this.searchlightLevel * 4 + this.surfaceRadarLevel * 2;
    }

    public int supportTickInterval() {
        return Mth.clamp(40 - this.transportTier * 4, 16, 40);
    }

    public float supportEffectMultiplier() {
        return 1.0F + Mth.clamp(this.transportTier * 0.08F, 0.0F, 0.40F);
    }

    public static int torpedoSpeedLevel(int index) {
        return switch (index) {
            case 3, 4 -> 1;
            case 5 -> 2;
            case 6 -> 3;
            default -> 0;
        };
    }
}
