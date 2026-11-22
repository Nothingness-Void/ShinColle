package com.lulan.shincolle.menu;

import com.lulan.shincolle.entity.ship.LegacyShipBehaviorCatalog;
import com.lulan.shincolle.entity.ship.LegacyShipEntity;
import com.lulan.shincolle.entity.ship.LegacyShipStatTables;
import com.lulan.shincolle.entity.ship.LegacyShipStatTables.Attr;
import com.lulan.shincolle.entity.ship.LegacyShipStats;
import com.lulan.shincolle.entity.ship.ShipEntitySpec;
import com.lulan.shincolle.entity.ship.ShipEntitySpecs;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffects;

public final class ShipInventoryPresentation {
    private CompoundTag data = new CompoundTag();

    public void apply(CompoundTag data) { this.data = data.copy(); }

    public static CompoundTag capture(LegacyShipEntity ship) {
        CompoundTag tag = new CompoundTag();
        if (ship == null) return tag;
        tag.putInt("Egg", ship.getSpec().eggMeta());
        tag.putInt("Page", ship.getInventoryPage());
        tag.putInt("PageSize", ship.getInventoryPageSize());
        tag.putString("Name", Component.Serializer.toJson(ship.getName()));
        tag.putFloat("Health", ship.getHealth());
        tag.putFloat("MaxHealth", ship.getMaxHealth());
        tag.putInt("Morale", ship.getMorale());
        tag.putInt("State", ship.getModelState());
        tag.putInt("Emotion", ship.getEmotion());
        tag.putInt("Riding", ship.getLegacyRidingState());
        tag.putInt("AirLight", ship.getAircraftLight());
        tag.putInt("AirHeavy", ship.getAircraftHeavy());
        tag.putBoolean("Nausea", ship.hasEffect(MobEffects.CONFUSION));
        tag.putFloat("Invisible", LegacyShipBehaviorCatalog.invisibleLevelFor(ship.getSpec()));
        CompoundTag flags = new CompoundTag();
        flags.putBoolean("UseMelee", ship.useAttackMelee());
        flags.putBoolean("UseAmmoLight", ship.useAttackLight());
        flags.putBoolean("UseAmmoHeavy", ship.useAttackHeavy());
        flags.putBoolean("UseAirLight", ship.useAttackAirLight());
        flags.putBoolean("UseAirHeavy", ship.useAttackAirHeavy());
        flags.putBoolean("UseRingEffect", ship.isRingEffectEnabled());
        flags.putBoolean("IsMarried", ship.isMarried());
        flags.putBoolean("PassiveAI", !ship.isAiAutoTarget());
        flags.putBoolean("OnSightChase", ship.isAiOnSightChase());
        flags.putBoolean("PVPFirst", ship.isAiPvpFirst());
        flags.putBoolean("AntiAir", ship.isAiAntiAir());
        flags.putBoolean("AntiSS", ship.isAiAntiSub());
        flags.putBoolean("TimeKeeper", ship.isTimeKeeper());
        flags.putBoolean("PickItem", ship.isAiPickItem());
        flags.putBoolean("ShowHeldItem", ship.canShowHeldItem());
        flags.putBoolean("AutoPump", ship.isAutoPump());
        tag.put("Flags", flags);
        CompoundTag minor = new CompoundTag();
        minor.putInt("ShipLevel", ship.getShipLevel());
        minor.putInt("ExpCurrent", ship.getShipExperience());
        minor.putInt("ExpNext", ship.getShipExperienceNext());
        minor.putInt("Kills", ship.getShipKills());
        minor.putInt("NumAmmoLight", ship.getLightAmmo());
        minor.putInt("NumAmmoHeavy", ship.getHeavyAmmo());
        minor.putInt("NumGrudge", ship.getGrudge());
        minor.putInt("FollowMin", ship.getFollowMin());
        minor.putInt("FollowMax", ship.getFollowMax());
        minor.putInt("FleeHP", ship.getFleeHp());
        minor.putInt("FormatType", ship.getLegacyFormationId());
        minor.putInt("FormatPos", ship.getLegacyFormationSlot());
        minor.putInt("Task", ship.getTask());
        minor.putInt("TaskSide", ship.getTaskSide());
        minor.putInt("UseCombatRation", ship.getCombatRationLevel());
        minor.putInt("WpStay", ship.getWaypointStayMode());
        tag.put("Minor", minor);
        int[] bonus = new int[6];
        for (int i = 0; i < bonus.length; i++) bonus[i] = ship.getModernizationSteps(i);
        tag.putIntArray("Bonus", bonus);
        LegacyShipStats stats = ship.getLegacyStats();
        float[] formation = new float[LegacyShipStatTables.ATTR_COUNT];
        float[] morale = new float[formation.length];
        for (int i = 0; i < formation.length; i++) {
            formation[i] = stats.formationStat(i);
            morale[i] = stats.moraleStat(i);
        }
        putFloats(tag, "Buffed", stats.copyBuffed());
        putFloats(tag, "Formation", formation);
        putFloats(tag, "MoraleValues", morale);
        return tag;
    }

    private static void putFloats(CompoundTag tag, String key, float[] values) {
        int[] bits = new int[values.length];
        for (int i = 0; i < bits.length; i++) bits[i] = Float.floatToIntBits(values[i]);
        tag.putIntArray(key, bits);
    }

    private float attribute(String key, int index) {
        int[] bits = this.data.getIntArray(key);
        return index >= 0 && index < bits.length ? Float.intBitsToFloat(bits[index]) : 0F;
    }

    public ShipEntitySpec getSpec() { return this.data.contains("Egg") ? ShipEntitySpecs.getByEggMeta(this.data.getInt("Egg")) : ShipEntitySpecs.DEFAULT; }
    public int getShipType() { return LegacyShipInventoryCatalog.forShip(getSpec()).shipType(); }
    public int getShipClass() { return getSpec().legacyClassId(); }
    public boolean isCarrier() { return LegacyShipInventoryCatalog.forShip(getSpec()).carrier(); }
    public boolean isInvisibleShip() { return LegacyShipInventoryCatalog.forShip(getSpec()).invisible(); }
    public float[] getModelPos() { return LegacyShipInventoryCatalog.forShip(getSpec()).modelPosition().clone(); }
    public int getInventoryPage() { return this.data.getInt("Page"); }
    public int getInventoryPageSize() { return this.data.getInt("PageSize"); }
    public float getHealth() { return this.data.getFloat("Health"); }
    public float getMaxHealth() { return this.data.getFloat("MaxHealth"); }
    public int getMorale() { return this.data.getInt("Morale"); }
    public int getStateEmotion(String key) { return this.data.getInt(key); }
    public int getRidingState() { return this.data.getInt("Riding"); }
    public int getNumAircraftLight() { return this.data.getInt("AirLight"); }
    public int getNumAircraftHeavy() { return this.data.getInt("AirHeavy"); }
    public float getInvisibleLevel() { return this.data.getFloat("Invisible"); }
    public int getStateMinor(String key) {
        if (key.equals("NumState")) return LegacyShipInventoryCatalog.forShip(getSpec()).modelButtons();
        return this.data.getCompound("Minor").getInt(key);
    }
    public boolean getStateFlag(String key) {
        if (this.data.getCompound("Flags").contains(key)) return this.data.getCompound("Flags").getBoolean(key);
        return LegacyShipInventoryCatalog.capability(LegacyShipInventoryCatalog.forShip(getSpec()), key);
    }
    public boolean getAttackType(String key) { return getStateFlag(key); }
    public String getCustomNameTag() {
        if (!this.data.contains("Name")) return "";
        Component name = Component.Serializer.fromJson(this.data.getString("Name"));
        return name == null ? "" : name.getString();
    }
    public int getAttrsBonus(int id) {
        int[] values = this.data.getIntArray("Bonus");
        return id >= 0 && id < values.length ? values[id] : 0;
    }
    public float getAttrsBuffed(int id) { return attribute("Buffed", id); }
    public float getAttrsFormation(int id) { return attribute("Formation", id); }
    public float getAttrsMorale(int id) { return attribute("MoraleValues", id); }
    public float getAttackRange() { return getAttrsBuffed(LegacyShipStatTables.Attr.HIT); }
    public float calcMissRate(float distance) {
        float range = getAttackRange();
        float miss;
        if (range <= 3F) miss = 0.25F - 0.001F * getStateMinor("ShipLevel");
        else if (range <= 6F) miss = 0.25F + 0.15F * (distance / range) - 0.001F * getStateMinor("ShipLevel");
        else miss = 0.25F + 0.25F * (distance / range) - 0.001F * getStateMinor("ShipLevel");
        miss -= getAttrsBuffed(LegacyShipStatTables.Attr.MISS);
        if (miss > 0.5F) miss = 0.5F;
        else if (miss < 0F) miss = 0F;
        if (this.data.getBoolean("Nausea")) miss += 0.4F;
        return miss;
    }
    public java.util.List<String> formationTooltipValues() {
        java.util.List<String> values = new java.util.ArrayList<>();
        if (getStateMinor("FormatType") < 1) return values;
        String str, str2, overText;
        overText = String.valueOf(this.getStateMinor("FormatPos") + 1);
                    values.add(overText);

                    str = String.format("%.0f", this.getAttrsFormation(Attr.ATK_L) * 100F) + " %";
                    str2 = String.format("%.0f", this.getAttrsFormation(Attr.ATK_H) * 100F) + " %";
                    overText = "x " + str + " / " + str2;
                    values.add(overText);

                    str = String.format("%.0f", this.getAttrsFormation(Attr.ATK_AL) * 100F) + " %";
                    str2 = String.format("%.0f", this.getAttrsFormation(Attr.ATK_AH) * 100F) + " %";
                    overText = "x " + str + " / " + str2;
                    values.add(overText);

                    overText = "x " + String.format("%.0f", this.getAttrsFormation(Attr.SPD) * 100F) + " %";
                    values.add(overText);
                    overText = "+ " + String.format("%.1f", this.getAttrsFormation(Attr.HIT));
                    values.add(overText);
                    overText = "x " + String.format("%.0f", this.getAttrsFormation(Attr.CRI) * 100F) + " %";
                    values.add(overText);
                    overText = "x " + String.format("%.0f", this.getAttrsFormation(Attr.DHIT) * 100F) + " %";
                    values.add(overText);
                    overText = "x " + String.format("%.0f", this.getAttrsFormation(Attr.THIT) * 100F) + " %";
                    values.add(overText);
                    overText = "x " + String.format("%.0f", this.getAttrsFormation(Attr.MISS) * 100F) + " %";
                    values.add(overText);
                    overText = "x " + String.format("%.0f", this.getAttrsFormation(Attr.AA) * 100F) + " %";
                    values.add(overText);
                    overText = "x " + String.format("%.0f", this.getAttrsFormation(Attr.ASM) * 100F) + " %";
                    values.add(overText);
                    overText = "x " + String.format("%.0f", this.getAttrsFormation(Attr.DEF) * 100F) + " %";
                    values.add(overText);
                    overText = "+ " + String.format("%.0f", this.getAttrsFormation(Attr.DODGE) * 100F) + " %";
                    values.add(overText);
                    overText = "+ " + String.format("%.0f", this.getAttrsFormation(Attr.GRUDGE) * 100F) + " %";
                    values.add(overText);
                    overText = "+ " + String.format("%.0f", this.getAttrsFormation(Attr.HPRES) * 100F) + " %";
                    values.add(overText);
                    overText = "+ " + String.format("%.0f", this.getAttrsFormation(Attr.KB) * 100F) + " %";
                    values.add(overText);
                    overText = "+ " + String.format("%.2f", this.getAttrsFormation(Attr.MOV));
                    values.add(overText);
        return values;
    }
}
