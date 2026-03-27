package com.lulan.shincolle.entity.ship;

import com.lulan.shincolle.item.equipment.LegacyEquipmentFamily;
import com.lulan.shincolle.item.equipment.LegacyEquipmentItem;
import com.lulan.shincolle.item.equipment.LegacyEquipmentStatsRepository;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;

public record ShipEquipmentProfile(
        float hp,
        float firepower,
        float torpedo,
        float airFirepower,
        float airTorpedo,
        float armor,
        float attackSpeed,
        float movementSpeed,
        float range,
        float critical,
        float doubleHit,
        float tripleHit,
        float missReduce,
        float antiAir,
        float antiSub,
        float dodge,
        float expBonus,
        float grudgeBonus,
        float ammoBonus,
        float hpResist,
        float knockbackBonus,
        int equippedCount) {

    private static final int STAT_COUNT = 21;
    public static final ShipEquipmentProfile EMPTY = new ShipEquipmentProfile(
            0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F,
            0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0);

    public static ShipEquipmentProfile fromInventory(Container inventory, ShipArchetype archetype) {
        float[] totals = new float[STAT_COUNT];
        int equippedCount = 0;

        for (int slot = 0; slot < Math.min(LegacyShipEntity.EQUIPMENT_SLOT_COUNT, inventory.getContainerSize()); slot++) {
            ItemStack stack = inventory.getItem(slot);
            if (!(stack.getItem() instanceof LegacyEquipmentItem equipmentItem) || !canEquip(archetype, stack)) {
                continue;
            }

            float[] stats = equipmentItem.getMainStats();
            if (stats == null) {
                continue;
            }

            equippedCount++;
            for (int index = 0; index < Math.min(stats.length, totals.length); index++) {
                totals[index] += stats[index];
            }
        }

        return new ShipEquipmentProfile(
                totals[0], totals[1], totals[2], totals[3], totals[4], totals[5], totals[6], totals[7], totals[8],
                totals[9], totals[10], totals[11], totals[12], totals[13], totals[14], totals[15], totals[16],
                totals[17], totals[18], totals[19], totals[20], equippedCount);
    }

    public static boolean canEquip(ShipArchetype archetype, ItemStack stack) {
        return stack.getItem() instanceof LegacyEquipmentItem equipmentItem && canEquip(archetype, equipmentItem);
    }

    public float stat(int index) {
        return switch (index) {
            case 0 -> this.hp;
            case 1 -> this.firepower;
            case 2 -> this.torpedo;
            case 3 -> this.airFirepower;
            case 4 -> this.airTorpedo;
            case 5 -> this.armor;
            case 6 -> this.attackSpeed;
            case 7 -> this.movementSpeed;
            case 8 -> this.range;
            case 9 -> this.critical;
            case 10 -> this.doubleHit;
            case 11 -> this.tripleHit;
            case 12 -> this.missReduce;
            case 13 -> this.antiAir;
            case 14 -> this.antiSub;
            case 15 -> this.dodge;
            case 16 -> this.expBonus;
            case 17 -> this.grudgeBonus;
            case 18 -> this.ammoBonus;
            case 19 -> this.hpResist;
            case 20 -> this.knockbackBonus;
            default -> 0F;
        };
    }

    public float[] toArray() {
        float[] values = new float[STAT_COUNT];
        for (int index = 0; index < values.length; index++) {
            values[index] = this.stat(index);
        }
        return values;
    }

    private static boolean canEquip(ShipArchetype archetype, LegacyEquipmentItem equipmentItem) {
        LegacyEquipmentStatsRepository.MiscData miscData = equipmentItem.getMiscData();

        if (miscData != null) {
            if (miscData.carrierOnly() && archetype != ShipArchetype.CARRIER && archetype != ShipArchetype.PRINCESS) {
                return false;
            }

            if (miscData.notForCarrier() && (archetype == ShipArchetype.CARRIER || archetype == ShipArchetype.PRINCESS)) {
                return false;
            }
        }

        return switch (equipmentItem.family()) {
            case AIRPLANE, CATAPULT -> archetype == ShipArchetype.CARRIER || archetype == ShipArchetype.PRINCESS;
            default -> true;
        };
    }

    public double maxHealthBonus() {
        return this.hp;
    }

    public double movementSpeedBonus() {
        // Legacy equipment speed values were authored against the old movement model and are too large
        // when applied 1:1 to 1.20's mob movement attribute.
        return this.movementSpeed * 0.12D;
    }

    public double followRangeBonus() {
        return this.range;
    }

    public double knockbackResistanceBonus() {
        return Mth.clamp(this.knockbackBonus + this.armor * 0.5F, 0.0F, 0.8F);
    }

    public double attackDamageBonus(ShipArchetype archetype) {
        double damageBonus = this.firepower;

        damageBonus += switch (archetype) {
            case DESTROYER, SUBMARINE -> this.torpedo * 0.85D + this.airTorpedo * 0.15D;
            case CRUISER, TRANSPORT -> this.torpedo * 0.45D + this.airFirepower * 0.15D;
            case BATTLESHIP, INSTALLATION -> this.torpedo * 0.20D + this.airFirepower * 0.10D;
            case CARRIER -> this.airFirepower * 0.90D + this.airTorpedo * 0.45D;
            case PRINCESS -> this.torpedo * 0.35D + this.airFirepower * 0.55D + this.airTorpedo * 0.25D;
        };

        damageBonus += this.antiSub * 0.20D;
        return damageBonus;
    }

    public float damageReductionFactor() {
        return Mth.clamp(this.armor + this.hpResist * 0.6F, 0.0F, 0.65F);
    }

    public float dodgeChance() {
        return Mth.clamp(this.dodge, 0.0F, 0.45F);
    }

    public float attackDamageMultiplier(RandomSource random) {
        float damageMultiplier = 1.0F;

        if (this.tripleHit > 0.0F && random.nextFloat() < this.tripleHit) {
            damageMultiplier += 1.15F;
        } else if (this.doubleHit > 0.0F && random.nextFloat() < this.doubleHit) {
            damageMultiplier += 0.65F;
        }

        if (this.critical > 0.0F && random.nextFloat() < this.critical) {
            damageMultiplier *= 1.45F;
        }

        return damageMultiplier;
    }

    public boolean tryDodge(RandomSource random) {
        return this.dodgeChance() > 0.0F && random.nextFloat() < this.dodgeChance();
    }

    public @Nullable String firstRestrictionReason(ShipArchetype archetype, ItemStack stack) {
        if (!(stack.getItem() instanceof LegacyEquipmentItem equipmentItem)) {
            return null;
        }

        LegacyEquipmentFamily family = equipmentItem.family();
        LegacyEquipmentStatsRepository.MiscData miscData = equipmentItem.getMiscData();

        if ((family == LegacyEquipmentFamily.AIRPLANE || family == LegacyEquipmentFamily.CATAPULT)
                && archetype != ShipArchetype.CARRIER && archetype != ShipArchetype.PRINCESS) {
            return "gui.shincolle.ship_inventory.equip_restrict.carrier";
        }

        if (miscData != null && miscData.carrierOnly()
                && archetype != ShipArchetype.CARRIER && archetype != ShipArchetype.PRINCESS) {
            return "gui.shincolle.ship_inventory.equip_restrict.carrier";
        }

        if (miscData != null && miscData.notForCarrier()
                && (archetype == ShipArchetype.CARRIER || archetype == ShipArchetype.PRINCESS)) {
            return "gui.shincolle.ship_inventory.equip_restrict.no_carrier";
        }

        return null;
    }
}
