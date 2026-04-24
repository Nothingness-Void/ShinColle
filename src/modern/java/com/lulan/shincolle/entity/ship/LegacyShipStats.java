package com.lulan.shincolle.entity.ship;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;

import java.util.Arrays;
import java.util.Collection;

public final class LegacyShipStats {

    private final int legacyClassId;
    private final boolean hostileVariant;
    private final int level;
    private final float[] raw;
    private final float[] equip;
    private final float[] marriage;
    private final float[] morale;
    private final float[] potion;
    private final float[] formation;
    private final float[] buffed;

    private LegacyShipStats(int legacyClassId, boolean hostileVariant, int level,
                            float[] raw, float[] equip, float[] marriage, float[] morale,
                            float[] potion, float[] formation, float[] buffed) {
        this.legacyClassId = legacyClassId;
        this.hostileVariant = hostileVariant;
        this.level = level;
        this.raw = raw;
        this.equip = equip;
        this.marriage = marriage;
        this.morale = morale;
        this.potion = potion;
        this.formation = formation;
        this.buffed = buffed;
    }

    public static LegacyShipStats create(int legacyClassId, ShipArchetype archetype, boolean hostileVariant,
                                         int level, int moraleValue,
                                         int healthBonus, int attackBonus, int defenseBonus, int attackSpeedBonus,
                                         int moveBonus, int rangeBonus,
                                         boolean married,
                                         int formationId,
                                         ShipEquipmentProfile equipmentProfile,
                                         Collection<MobEffectInstance> activeEffects) {
        int resolvedLevel = Math.max(1, level);
        float[] raw = hostileVariant
                ? buildHostileRaw(legacyClassId, archetype)
                : buildFriendlyRaw(legacyClassId, resolvedLevel, healthBonus, attackBonus, defenseBonus, attackSpeedBonus, moveBonus, rangeBonus);
        float[] equip = equipmentProfile.toArray();
        float[] marriage = LegacyShipStatTables.copyMarriageStats(married);
        float[] morale = LegacyShipStatTables.copyMoraleStats(moraleValue);
        float[] potion = buildPotionStats(activeEffects);
        float[] formation = hostileVariant
                ? LegacyShipStatTables.copyResetFormation()
                : LegacyShipStatTables.copyFormationStats(formationId);
        float[] buffed = calcBuffed(raw, equip, marriage, morale, potion, formation);
        LegacyShipStatTables.clamp(buffed);

        return new LegacyShipStats(legacyClassId, hostileVariant, resolvedLevel,
                raw, equip, marriage, morale, potion, formation, buffed);
    }

    private static float[] buildFriendlyRaw(int legacyClassId, int level,
                                            int healthBonus, int attackBonus, int defenseBonus, int attackSpeedBonus,
                                            int moveBonus, int rangeBonus) {
        float[] raw = getResetRawValue();
        float[] base = LegacyShipStatTables.copyBaseStats(legacyClassId);

        raw[LegacyShipStatTables.Attr.HP] =
                (float) ((base[LegacyShipStatTables.BaseAttr.HP]
                        + (healthBonus + 1F) * level * base[LegacyShipStatTables.BaseAttr.MOD_HP])
                        * LegacyShipStatTables.SCALE_SHIP[LegacyShipStatTables.BaseAttr.HP]);
        raw[LegacyShipStatTables.Attr.DEF] =
                (float) ((base[LegacyShipStatTables.BaseAttr.DEF]
                        + (defenseBonus + 1F) * level * 0.00133F * base[LegacyShipStatTables.BaseAttr.MOD_DEF])
                        * LegacyShipStatTables.SCALE_SHIP[LegacyShipStatTables.BaseAttr.DEF]);
        raw[LegacyShipStatTables.Attr.SPD] =
                (float) ((base[LegacyShipStatTables.BaseAttr.SPD]
                        + (attackSpeedBonus + 1F) * level * 0.004F * base[LegacyShipStatTables.BaseAttr.MOD_SPD])
                        * LegacyShipStatTables.SCALE_SHIP[LegacyShipStatTables.BaseAttr.SPD]);
        raw[LegacyShipStatTables.Attr.MOV] =
                (float) ((base[LegacyShipStatTables.BaseAttr.MOV]
                        + (moveBonus + 1F) * level * 0.002F * base[LegacyShipStatTables.BaseAttr.MOD_MOV])
                        * LegacyShipStatTables.SCALE_SHIP[LegacyShipStatTables.BaseAttr.MOV]);
        raw[LegacyShipStatTables.Attr.HIT] =
                (float) ((base[LegacyShipStatTables.BaseAttr.HIT]
                        + (rangeBonus + 1F) * level * 0.02F * base[LegacyShipStatTables.BaseAttr.MOD_HIT])
                        * LegacyShipStatTables.SCALE_SHIP[LegacyShipStatTables.BaseAttr.HIT]);

        float baseAttack = base[LegacyShipStatTables.BaseAttr.ATK]
                + (attackBonus + 1F) * level * 0.133F * base[LegacyShipStatTables.BaseAttr.MOD_ATK];
        raw[LegacyShipStatTables.Attr.ATK_L] =
                (float) (baseAttack * LegacyShipStatTables.SCALE_SHIP[LegacyShipStatTables.BaseAttr.ATK]);
        raw[LegacyShipStatTables.Attr.ATK_H] =
                (float) (baseAttack * 3F * LegacyShipStatTables.SCALE_SHIP[LegacyShipStatTables.BaseAttr.ATK]);
        raw[LegacyShipStatTables.Attr.ATK_AL] =
                (float) (baseAttack * LegacyShipStatTables.SCALE_SHIP[LegacyShipStatTables.BaseAttr.ATK]);
        raw[LegacyShipStatTables.Attr.ATK_AH] =
                (float) (baseAttack * 3F * LegacyShipStatTables.SCALE_SHIP[LegacyShipStatTables.BaseAttr.ATK]);
        raw[LegacyShipStatTables.Attr.XP] = 1F;
        raw[LegacyShipStatTables.Attr.GRUDGE] = 1F;
        raw[LegacyShipStatTables.Attr.AMMO] = 1F;
        raw[LegacyShipStatTables.Attr.HPRES] = 1F;
        raw[LegacyShipStatTables.Attr.KB] = level * 0.005F;

        return raw;
    }

    private static float[] buildHostileRaw(int legacyClassId, ShipArchetype archetype) {
        float[] raw = getResetRawValue();
        int friendlyClassId = legacyClassId - 2000;
        float[] hostileModifier = LegacyShipStatTables.copyHostileModifier(friendlyClassId);
        double[] scale = LegacyShipStatTables.hostileScaleFor(archetype);
        float kb = archetype == ShipArchetype.DESTROYER || archetype == ShipArchetype.SUBMARINE ? 0.2F : 0.4F;

        raw[LegacyShipStatTables.Attr.HP] = (float) (scale[LegacyShipStatTables.BaseAttr.HP] * hostileModifier[LegacyShipStatTables.BaseAttr.HP]);
        raw[LegacyShipStatTables.Attr.ATK_L] = (float) (scale[LegacyShipStatTables.BaseAttr.ATK] * hostileModifier[LegacyShipStatTables.BaseAttr.ATK]);
        raw[LegacyShipStatTables.Attr.ATK_H] = raw[LegacyShipStatTables.Attr.ATK_L] * 3F;
        raw[LegacyShipStatTables.Attr.ATK_AL] = raw[LegacyShipStatTables.Attr.ATK_L];
        raw[LegacyShipStatTables.Attr.ATK_AH] = raw[LegacyShipStatTables.Attr.ATK_H];
        raw[LegacyShipStatTables.Attr.DEF] = (float) (scale[LegacyShipStatTables.BaseAttr.DEF] * hostileModifier[LegacyShipStatTables.BaseAttr.DEF]);
        raw[LegacyShipStatTables.Attr.SPD] = (float) (scale[LegacyShipStatTables.BaseAttr.SPD] * hostileModifier[LegacyShipStatTables.BaseAttr.SPD]);
        raw[LegacyShipStatTables.Attr.MOV] = (float) (scale[LegacyShipStatTables.BaseAttr.MOV] * hostileModifier[LegacyShipStatTables.BaseAttr.MOV]);
        raw[LegacyShipStatTables.Attr.HIT] = (float) (scale[LegacyShipStatTables.BaseAttr.HIT] * hostileModifier[LegacyShipStatTables.BaseAttr.HIT]);
        raw[LegacyShipStatTables.Attr.CRI] = 0.15F;
        raw[LegacyShipStatTables.Attr.DHIT] = 0.1F;
        raw[LegacyShipStatTables.Attr.THIT] = 0.1F;
        raw[LegacyShipStatTables.Attr.MISS] = 0F;
        raw[LegacyShipStatTables.Attr.AA] = 0F;
        raw[LegacyShipStatTables.Attr.ASM] = 0F;
        raw[LegacyShipStatTables.Attr.DODGE] = 0.15F;
        raw[LegacyShipStatTables.Attr.XP] = 1F;
        raw[LegacyShipStatTables.Attr.GRUDGE] = 1F;
        raw[LegacyShipStatTables.Attr.AMMO] = 1F;
        raw[LegacyShipStatTables.Attr.HPRES] = 1F;
        raw[LegacyShipStatTables.Attr.KB] = kb;

        return raw;
    }

    private static float[] buildPotionStats(Collection<MobEffectInstance> activeEffects) {
        float[] potion = new float[LegacyShipStatTables.ATTR_COUNT];

        for (MobEffectInstance effect : activeEffects) {
            int level = Math.max(1, Math.min(5, effect.getAmplifier() + 1));

            if (effect.getEffect() == MobEffects.MOVEMENT_SPEED) {
                potion[LegacyShipStatTables.Attr.MOV] += 0.08F * level;
            } else if (effect.getEffect() == MobEffects.MOVEMENT_SLOWDOWN) {
                potion[LegacyShipStatTables.Attr.MOV] += -0.15F * level;
                potion[LegacyShipStatTables.Attr.KB] += 0.15F * level;
            } else if (effect.getEffect() == MobEffects.DIG_SPEED) {
                potion[LegacyShipStatTables.Attr.SPD] += 0.6F * level;
            } else if (effect.getEffect() == MobEffects.DIG_SLOWDOWN) {
                potion[LegacyShipStatTables.Attr.SPD] += -0.6F * level;
            } else if (effect.getEffect() == MobEffects.DAMAGE_BOOST) {
                potion[LegacyShipStatTables.Attr.ATK_L] += 15F * level;
                potion[LegacyShipStatTables.Attr.ATK_H] += 15F * level;
                potion[LegacyShipStatTables.Attr.ATK_AL] += 15F * level;
                potion[LegacyShipStatTables.Attr.ATK_AH] += 15F * level;
                potion[LegacyShipStatTables.Attr.KB] += 0.15F * level;
            } else if (effect.getEffect() == MobEffects.JUMP) {
                potion[LegacyShipStatTables.Attr.HIT] += 2F * level;
            } else if (effect.getEffect() == MobEffects.WATER_BREATHING) {
                potion[LegacyShipStatTables.Attr.DODGE] += 0.15F * level;
                potion[LegacyShipStatTables.Attr.ASM] += 20F * level;
            } else if (effect.getEffect() == MobEffects.BLINDNESS) {
                potion[LegacyShipStatTables.Attr.HIT] -= 24F;
            } else if (effect.getEffect() == MobEffects.WEAKNESS) {
                potion[LegacyShipStatTables.Attr.ATK_L] += -15F * level;
                potion[LegacyShipStatTables.Attr.ATK_H] += -15F * level;
                potion[LegacyShipStatTables.Attr.ATK_AL] += -15F * level;
                potion[LegacyShipStatTables.Attr.ATK_AH] += -15F * level;
                potion[LegacyShipStatTables.Attr.KB] += -0.15F * level;
            } else if (effect.getEffect() == MobEffects.POISON) {
                potion[LegacyShipStatTables.Attr.DEF] += -0.25F * level;
                potion[LegacyShipStatTables.Attr.KB] += -0.1F * level;
            } else if (effect.getEffect() == MobEffects.HEALTH_BOOST) {
                potion[LegacyShipStatTables.Attr.HP] += 150F * level;
                potion[LegacyShipStatTables.Attr.HPRES] += 0.5F * level;
            } else if (effect.getEffect() == MobEffects.ABSORPTION) {
                potion[LegacyShipStatTables.Attr.HP] += 100F * level;
                potion[LegacyShipStatTables.Attr.DEF] += 0.2F * level;
            } else if (effect.getEffect() == MobEffects.SATURATION) {
                potion[LegacyShipStatTables.Attr.GRUDGE] += 0.5F * level;
                potion[LegacyShipStatTables.Attr.AMMO] += 0.5F * level;
            } else if (effect.getEffect() == MobEffects.LEVITATION) {
                potion[LegacyShipStatTables.Attr.DODGE] += 0.1F * level;
                potion[LegacyShipStatTables.Attr.AA] += 20F * level;
                potion[LegacyShipStatTables.Attr.KB] += -0.2F * level;
            } else if (effect.getEffect() == MobEffects.LUCK) {
                potion[LegacyShipStatTables.Attr.CRI] += 0.2F * level;
                potion[LegacyShipStatTables.Attr.DHIT] += 0.2F * level;
                potion[LegacyShipStatTables.Attr.THIT] += 0.2F * level;
            } else if (effect.getEffect() == MobEffects.UNLUCK) {
                potion[LegacyShipStatTables.Attr.CRI] += -0.3F * level;
                potion[LegacyShipStatTables.Attr.DHIT] += -0.3F * level;
                potion[LegacyShipStatTables.Attr.THIT] += -0.3F * level;
            }
        }

        return potion;
    }

    private static float[] calcBuffed(float[] raw, float[] equip, float[] marriage, float[] morale, float[] potion, float[] formation) {
        float[] buffed = new float[raw.length];

        int id = LegacyShipStatTables.Attr.HP;
        buffed[id] = raw[id] + equip[id] + marriage[id]
                + (morale[id] + potion[id] + formation[id]) * (float) LegacyShipStatTables.SCALE_SHIP[LegacyShipStatTables.BaseAttr.HP];
        id = LegacyShipStatTables.Attr.HIT;
        buffed[id] = raw[id] + equip[id] + marriage[id]
                + (morale[id] + potion[id] + formation[id]) * (float) LegacyShipStatTables.SCALE_SHIP[LegacyShipStatTables.BaseAttr.HIT];
        id = LegacyShipStatTables.Attr.DODGE;
        buffed[id] = raw[id] + equip[id] + marriage[id] + morale[id] + potion[id] + formation[id];
        id = LegacyShipStatTables.Attr.XP;
        buffed[id] = raw[id] + equip[id] + marriage[id] + morale[id] + potion[id] + formation[id];
        id = LegacyShipStatTables.Attr.GRUDGE;
        buffed[id] = raw[id] + equip[id] + marriage[id] + morale[id] + potion[id] + formation[id];
        id = LegacyShipStatTables.Attr.AMMO;
        buffed[id] = raw[id] + equip[id] + marriage[id] + morale[id] + potion[id] + formation[id];
        id = LegacyShipStatTables.Attr.HPRES;
        buffed[id] = raw[id] + equip[id] + marriage[id] + morale[id] + potion[id] + formation[id];
        id = LegacyShipStatTables.Attr.KB;
        buffed[id] = raw[id] + equip[id] + marriage[id] + morale[id] + potion[id] + formation[id];

        id = LegacyShipStatTables.Attr.MOV;
        buffed[id] = raw[id] + equip[id] + marriage[id]
                + (morale[id] + potion[id]) * (float) LegacyShipStatTables.SCALE_SHIP[LegacyShipStatTables.BaseAttr.MOV];

        id = LegacyShipStatTables.Attr.ATK_L;
        buffed[id] = (raw[id] + equip[id] + marriage[id] + potion[id] * (float) LegacyShipStatTables.SCALE_SHIP[LegacyShipStatTables.BaseAttr.ATK]) * morale[id] * formation[id];
        id = LegacyShipStatTables.Attr.ATK_H;
        buffed[id] = (raw[id] + equip[id] + marriage[id] + potion[id] * 3F * (float) LegacyShipStatTables.SCALE_SHIP[LegacyShipStatTables.BaseAttr.ATK]) * morale[id] * formation[id];
        id = LegacyShipStatTables.Attr.ATK_AL;
        buffed[id] = (raw[id] + equip[id] + marriage[id] + potion[id] * (float) LegacyShipStatTables.SCALE_SHIP[LegacyShipStatTables.BaseAttr.ATK]) * morale[id] * formation[id];
        id = LegacyShipStatTables.Attr.ATK_AH;
        buffed[id] = (raw[id] + equip[id] + marriage[id] + potion[id] * 3F * (float) LegacyShipStatTables.SCALE_SHIP[LegacyShipStatTables.BaseAttr.ATK]) * morale[id] * formation[id];
        id = LegacyShipStatTables.Attr.SPD;
        buffed[id] = (raw[id] + equip[id] + marriage[id] + potion[id] * (float) LegacyShipStatTables.SCALE_SHIP[LegacyShipStatTables.BaseAttr.SPD]) * morale[id] * formation[id];
        id = LegacyShipStatTables.Attr.CRI;
        buffed[id] = (raw[id] + equip[id] + marriage[id] + potion[id]) * morale[id] * formation[id];
        id = LegacyShipStatTables.Attr.DHIT;
        buffed[id] = (raw[id] + equip[id] + marriage[id] + potion[id]) * morale[id] * formation[id];
        id = LegacyShipStatTables.Attr.THIT;
        buffed[id] = (raw[id] + equip[id] + marriage[id] + potion[id]) * morale[id] * formation[id];
        id = LegacyShipStatTables.Attr.MISS;
        buffed[id] = (raw[id] + equip[id] + marriage[id] + potion[id]) * morale[id] * formation[id];
        id = LegacyShipStatTables.Attr.AA;
        buffed[id] = (raw[id] + equip[id] + marriage[id] + potion[id]) * morale[id] * formation[id];
        id = LegacyShipStatTables.Attr.ASM;
        buffed[id] = (raw[id] + equip[id] + marriage[id] + potion[id]) * morale[id] * formation[id];

        id = LegacyShipStatTables.Attr.DEF;
        buffed[id] = (raw[id] + equip[id] + marriage[id]
                + (morale[id] + potion[id]) * (float) LegacyShipStatTables.SCALE_SHIP[LegacyShipStatTables.BaseAttr.DEF]) * formation[id];

        return buffed;
    }

    private static float[] getResetRawValue() {
        return new float[] {
                4F, 0F, 0F, 0F, 0F,
                0F, 0.2F, 0F, 1F, 0F,
                0F, 0F, 0F, 0F, 0F,
                0F, 1F, 1F, 1F, 1F,
                0F
        };
    }

    public float get(int attrId) {
        return this.buffed[attrId];
    }

    public float attackLight() {
        return this.get(LegacyShipStatTables.Attr.ATK_L);
    }

    public float defense() {
        return this.get(LegacyShipStatTables.Attr.DEF);
    }

    public float attackHeavy() {
        return this.get(LegacyShipStatTables.Attr.ATK_H);
    }

    public float attackAirLight() {
        return this.get(LegacyShipStatTables.Attr.ATK_AL);
    }

    public float attackAirHeavy() {
        return this.get(LegacyShipStatTables.Attr.ATK_AH);
    }

    public float attackSpeed() {
        return this.get(LegacyShipStatTables.Attr.SPD);
    }

    public float moveSpeed() {
        return this.get(LegacyShipStatTables.Attr.MOV);
    }

    public float attackRange() {
        return this.get(LegacyShipStatTables.Attr.HIT);
    }

    public float dodge() {
        return this.get(LegacyShipStatTables.Attr.DODGE);
    }

    public float critical() {
        return this.get(LegacyShipStatTables.Attr.CRI);
    }

    public float doubleHit() {
        return this.get(LegacyShipStatTables.Attr.DHIT);
    }

    public float tripleHit() {
        return this.get(LegacyShipStatTables.Attr.THIT);
    }

    public float missReduce() {
        return this.get(LegacyShipStatTables.Attr.MISS);
    }

    public float antiAir() {
        return this.get(LegacyShipStatTables.Attr.AA);
    }

    public float antiSub() {
        return this.get(LegacyShipStatTables.Attr.ASM);
    }

    public float knockbackResistance() {
        return this.get(LegacyShipStatTables.Attr.KB);
    }

    public float healRate() {
        return this.get(LegacyShipStatTables.Attr.HPRES);
    }

    public float meleeDamage() {
        return Math.max(1F, this.attackLight() * 0.125F);
    }

    public float[] copyBuffed() {
        return Arrays.copyOf(this.buffed, this.buffed.length);
    }

    public float[] copyEquip() {
        return Arrays.copyOf(this.equip, this.equip.length);
    }
}
