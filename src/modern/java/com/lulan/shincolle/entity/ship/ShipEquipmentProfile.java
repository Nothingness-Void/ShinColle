package com.lulan.shincolle.entity.ship;

import com.lulan.shincolle.item.equipment.LegacyEquipmentFamily;
import com.lulan.shincolle.item.equipment.LegacyEquipmentItem;
import com.lulan.shincolle.item.equipment.LegacyEquipmentStatsRepository;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;

import javax.annotation.Nullable;
import java.util.Map;

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

    public static ShipEquipmentProfile fromInventory(Container inventory, ShipEntitySpec spec) {
        return fromInventory(inventory, LegacyShipBehaviorCatalog.equipmentTypeFor(spec));
    }

    public static ShipEquipmentProfile fromInventory(Container inventory, ShipArchetype archetype) {
        return fromInventory(inventory, equipmentTypeForArchetype(archetype));
    }

    private static ShipEquipmentProfile fromInventory(Container inventory, int equipmentType) {
        float[] totals = new float[STAT_COUNT];
        int equippedCount = 0;

        for (int slot = 0; slot < Math.min(LegacyShipEntity.EQUIPMENT_SLOT_COUNT, inventory.getContainerSize()); slot++) {
            ItemStack stack = inventory.getItem(slot);
            if (!(stack.getItem() instanceof LegacyEquipmentItem equipmentItem) || !canEquip(equipmentType, equipmentItem)) {
                continue;
            }

            float[] stats = equipmentItem.getMainStats();
            if (stats == null) {
                continue;
            }

            if (!stack.getEnchantments().isEmpty()) {
                LegacyEquipmentStatsRepository.MiscData miscData = equipmentItem.getMiscData();
                int itemEquipType = miscData != null ? miscData.equipmentType() : 0;
                stats = calcEquipStatWithEnchant(itemEquipType, stats, calcEnchantEffect(stack));
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
        return stack.getItem() instanceof LegacyEquipmentItem equipmentItem
                && canEquip(equipmentTypeForArchetype(archetype), equipmentItem);
    }

    public static boolean canEquip(ShipEntitySpec spec, ItemStack stack) {
        return stack.getItem() instanceof LegacyEquipmentItem equipmentItem
                && canEquip(LegacyShipBehaviorCatalog.equipmentTypeFor(spec), equipmentItem);
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

    private static boolean canEquip(int equipmentType, LegacyEquipmentItem equipmentItem) {
        LegacyEquipmentStatsRepository.MiscData miscData = equipmentItem.getMiscData();

        if (miscData != null) {
            // EquipCalc accepts type 2 on every ship and otherwise requires an exact type match.
            if (equipmentType != 2 && miscData.equipmentType() != 2
                    && equipmentType != miscData.equipmentType()) {
                return false;
            }
        }

        return switch (equipmentItem.family()) {
            case AIRPLANE, CATAPULT -> equipmentType == 2 || equipmentType == 3;
            default -> true;
        };
    }

    private static int equipmentTypeForArchetype(ShipArchetype archetype) {
        return archetype == ShipArchetype.CARRIER || archetype == ShipArchetype.PRINCESS ? 3 : 1;
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
        return firstRestrictionReason(equipmentTypeForArchetype(archetype), stack);
    }

    public @Nullable String firstRestrictionReason(ShipEntitySpec spec, ItemStack stack) {
        return firstRestrictionReason(LegacyShipBehaviorCatalog.equipmentTypeFor(spec), stack);
    }

    private @Nullable String firstRestrictionReason(int equipmentType, ItemStack stack) {
        if (!(stack.getItem() instanceof LegacyEquipmentItem equipmentItem)) {
            return null;
        }

        LegacyEquipmentFamily family = equipmentItem.family();
        LegacyEquipmentStatsRepository.MiscData miscData = equipmentItem.getMiscData();

        if ((family == LegacyEquipmentFamily.AIRPLANE || family == LegacyEquipmentFamily.CATAPULT)
                && equipmentType == 1) {
            return "gui.shincolle.ship_inventory.equip_restrict.carrier";
        }

        if (miscData != null && miscData.equipmentType() == 3 && equipmentType == 1) {
            return "gui.shincolle.ship_inventory.equip_restrict.carrier";
        }

        if (miscData != null && miscData.equipmentType() == 1 && equipmentType == 3) {
            return "gui.shincolle.ship_inventory.equip_restrict.no_carrier";
        }

        return null;
    }

    /** Mirrors EnchantHelper.calcEnchantEffect for 1.20.1 ItemStack enchantments. */
    private static float[] calcEnchantEffect(ItemStack stack) {
        float[] ench = new float[STAT_COUNT];
        var enchantments = stack.getEnchantments();

        for (var entry : enchantments.entrySet()) {
            var enchantment = entry.getKey();
            int lv = entry.getIntValue();
            if (enchantment == null || lv <= 0) {
                continue;
            }

            if (enchantment.is(Enchantments.BLAST_PROTECTION)) {
                ench[LegacyShipStatTables.Attr.HP] += 0.20F * lv;
                ench[LegacyShipStatTables.Attr.KB] += 0.1F * lv;
            } else if (enchantment.is(Enchantments.FIRE_PROTECTION)
                    || enchantment.is(Enchantments.PROJECTILE_PROTECTION)) {
                ench[LegacyShipStatTables.Attr.HP] += 0.15F * lv;
            } else if (enchantment.is(Enchantments.PROTECTION)) {
                ench[LegacyShipStatTables.Attr.HP] += 0.1F * lv;
            } else if (enchantment.is(Enchantments.SMITE)
                    || enchantment.is(Enchantments.BANE_OF_ARTHROPODS)) {
                ench[LegacyShipStatTables.Attr.ATK_L] += 0.16F * lv;
            } else if (enchantment.is(Enchantments.SHARPNESS)
                    || enchantment.is(Enchantments.POWER)) {
                ench[LegacyShipStatTables.Attr.ATK_L] += 0.08F * lv;
            } else if (enchantment.is(Enchantments.UNBREAKING)) {
                ench[LegacyShipStatTables.Attr.DEF] += 0.2F * lv;
            } else if (enchantment.is(Enchantments.EFFICIENCY)) {
                ench[LegacyShipStatTables.Attr.SPD] += 0.1F * lv;
            } else if (enchantment.is(Enchantments.AQUA_AFFINITY)
                    || enchantment.is(Enchantments.DEPTH_STRIDER)) {
                ench[LegacyShipStatTables.Attr.MOV] += 0.05F * lv;
                ench[LegacyShipStatTables.Attr.DODGE] += 0.25F * lv;
            } else if (enchantment.is(Enchantments.FEATHER_FALLING)) {
                ench[LegacyShipStatTables.Attr.MOV] += 0.1F * lv;
                ench[LegacyShipStatTables.Attr.KB] -= 0.1F * lv;
            } else if (enchantment.is(Enchantments.PUNCH)
                    || enchantment.is(Enchantments.KNOCKBACK)) {
                ench[LegacyShipStatTables.Attr.HIT] += 0.15F * lv;
                ench[LegacyShipStatTables.Attr.KB] += 0.05F * lv;
            } else if (enchantment.is(Enchantments.FROST_WALKER)) {
                ench[LegacyShipStatTables.Attr.CRI] += 0.25F * lv;
            } else if (enchantment.is(Enchantments.FIRE_ASPECT)
                    || enchantment.is(Enchantments.FLAME)) {
                ench[LegacyShipStatTables.Attr.DHIT] += 0.25F * lv;
                ench[LegacyShipStatTables.Attr.THIT] += 0.25F * lv;
            } else if (enchantment.is(Enchantments.LURE)) {
                ench[LegacyShipStatTables.Attr.MISS] += 0.25F * lv;
            } else if (enchantment.is(Enchantments.THORNS)) {
                ench[LegacyShipStatTables.Attr.AA] += 0.15F * lv;
            } else if (enchantment.is(Enchantments.RESPIRATION)) {
                ench[LegacyShipStatTables.Attr.ASM] += 0.15F * lv;
            } else if (enchantment.is(Enchantments.LOOTING)
                    || enchantment.is(Enchantments.FORTUNE)
                    || enchantment.is(Enchantments.LUCK_OF_THE_SEA)) {
                ench[LegacyShipStatTables.Attr.XP] += 0.25F * lv;
            } else if (enchantment.is(Enchantments.SILK_TOUCH)) {
                ench[LegacyShipStatTables.Attr.GRUDGE] += 0.25F * lv;
            } else if (enchantment.is(Enchantments.INFINITY)) {
                ench[LegacyShipStatTables.Attr.AMMO] += 0.25F * lv;
            } else if (enchantment.is(Enchantments.MENDING)) {
                ench[LegacyShipStatTables.Attr.HPRES] += 0.5F * lv;
            } else {
                for (int j = 0; j < ench.length; j++) {
                    ench[j] += 0.01F * lv;
                }
            }
        }

        return ench;
    }

    /** Mirrors EquipCalc.calcEquipStatWithEnchant. */
    private static float[] calcEquipStatWithEnchant(int equipType, float[] raw, float[] enchant) {
        float[] newstat = new float[STAT_COUNT];

        newstat[LegacyShipStatTables.Attr.HP] = raw[LegacyShipStatTables.Attr.HP] * (1F + enchant[LegacyShipStatTables.Attr.HP]);

        float atkMod = equipType == 1 ? 1F + enchant[LegacyShipStatTables.Attr.ATK_L] : 1F;
        newstat[LegacyShipStatTables.Attr.ATK_L] = raw[LegacyShipStatTables.Attr.ATK_L] * atkMod;
        newstat[LegacyShipStatTables.Attr.ATK_H] = raw[LegacyShipStatTables.Attr.ATK_H] * atkMod;
        newstat[LegacyShipStatTables.Attr.ATK_AL] = raw[LegacyShipStatTables.Attr.ATK_AL] * atkMod;
        newstat[LegacyShipStatTables.Attr.ATK_AH] = raw[LegacyShipStatTables.Attr.ATK_AH] * atkMod;

        float defMod = equipType == 2 ? 1F + enchant[LegacyShipStatTables.Attr.DEF] : 1F;
        newstat[LegacyShipStatTables.Attr.DEF] = raw[LegacyShipStatTables.Attr.DEF] * defMod;

        newstat[LegacyShipStatTables.Attr.SPD] = raw[LegacyShipStatTables.Attr.SPD] * (1F + enchant[LegacyShipStatTables.Attr.SPD]);

        float movMod = raw[LegacyShipStatTables.Attr.MOV] < 0F
                ? Math.max(0F, 1F - enchant[LegacyShipStatTables.Attr.MOV])
                : 1F + enchant[LegacyShipStatTables.Attr.MOV];
        newstat[LegacyShipStatTables.Attr.MOV] = raw[LegacyShipStatTables.Attr.MOV] * movMod;

        newstat[LegacyShipStatTables.Attr.HIT] = raw[LegacyShipStatTables.Attr.HIT] * (1F + enchant[LegacyShipStatTables.Attr.HIT]);
        newstat[LegacyShipStatTables.Attr.CRI] = raw[LegacyShipStatTables.Attr.CRI] * (1F + enchant[LegacyShipStatTables.Attr.CRI]);
        newstat[LegacyShipStatTables.Attr.DHIT] = raw[LegacyShipStatTables.Attr.DHIT] * (1F + enchant[LegacyShipStatTables.Attr.DHIT]);
        newstat[LegacyShipStatTables.Attr.THIT] = raw[LegacyShipStatTables.Attr.THIT] * (1F + enchant[LegacyShipStatTables.Attr.THIT]);
        newstat[LegacyShipStatTables.Attr.MISS] = raw[LegacyShipStatTables.Attr.MISS] * (1F + enchant[LegacyShipStatTables.Attr.MISS]);
        newstat[LegacyShipStatTables.Attr.AA] = raw[LegacyShipStatTables.Attr.AA] * (1F + enchant[LegacyShipStatTables.Attr.AA]);
        newstat[LegacyShipStatTables.Attr.ASM] = raw[LegacyShipStatTables.Attr.ASM] * (1F + enchant[LegacyShipStatTables.Attr.ASM]);

        float dodgeMod = raw[LegacyShipStatTables.Attr.DODGE] < 0F
                ? Math.max(0F, 1F - enchant[LegacyShipStatTables.Attr.DODGE])
                : 1F + enchant[LegacyShipStatTables.Attr.DODGE];
        newstat[LegacyShipStatTables.Attr.DODGE] = raw[LegacyShipStatTables.Attr.DODGE] * dodgeMod;

        newstat[LegacyShipStatTables.Attr.XP] = equipType == 1
                ? raw[LegacyShipStatTables.Attr.XP] + enchant[LegacyShipStatTables.Attr.XP]
                : raw[LegacyShipStatTables.Attr.XP];
        newstat[LegacyShipStatTables.Attr.GRUDGE] = equipType != 1
                ? raw[LegacyShipStatTables.Attr.GRUDGE] + enchant[LegacyShipStatTables.Attr.GRUDGE]
                : raw[LegacyShipStatTables.Attr.GRUDGE];
        newstat[LegacyShipStatTables.Attr.AMMO] = equipType == 1
                ? raw[LegacyShipStatTables.Attr.AMMO] + enchant[LegacyShipStatTables.Attr.AMMO]
                : raw[LegacyShipStatTables.Attr.AMMO];
        newstat[LegacyShipStatTables.Attr.HPRES] = equipType != 1
                ? raw[LegacyShipStatTables.Attr.HPRES] + enchant[LegacyShipStatTables.Attr.HPRES]
                : raw[LegacyShipStatTables.Attr.HPRES];
        newstat[LegacyShipStatTables.Attr.KB] = equipType != 1
                ? raw[LegacyShipStatTables.Attr.KB] + enchant[LegacyShipStatTables.Attr.KB]
                : raw[LegacyShipStatTables.Attr.KB];

        return newstat;
    }
}
