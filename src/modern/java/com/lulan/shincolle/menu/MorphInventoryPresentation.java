package com.lulan.shincolle.menu;

import com.lulan.shincolle.entity.ship.LegacyShipStatTables;
import com.lulan.shincolle.entity.ship.LegacyShipBehaviorCatalog;
import com.lulan.shincolle.entity.ship.LegacyShipStats;
import com.lulan.shincolle.entity.ship.ShipEntitySpec;
import com.lulan.shincolle.entity.ship.ShipEntitySpecs;
import com.lulan.shincolle.morph.MorphProfile;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.util.RandomSource;

public final class MorphInventoryPresentation {
    private final ShipInventoryPresentation view = new ShipInventoryPresentation();
    private final RandomSource random = RandomSource.create();
    private MorphProfile profile;
    private Player player;

    public void refresh(Player player, MorphProfile profile) {
        this.player = player;
        this.profile = profile;
        CompoundTag tag = new CompoundTag();
        if (profile != null) {
            ShipEntitySpec metadataSpec = profile.getSpec().hostile()
                    ? ShipEntitySpecs.friendlyCounterpart(profile.getSpec()) : profile.getSpec();
            tag.putInt("Egg", metadataSpec.eggMeta());
            tag.putString("Name", Component.Serializer.toJson(profile.getSpec().displayName()));
            tag.putInt("Morale", profile.getMorale());
            tag.putFloat("Invisible", LegacyShipBehaviorCatalog.invisibleLevelFor(profile.getSpec()));
            tag.putBoolean("Nausea", player.hasEffect(MobEffects.CONFUSION));
            CompoundTag minor = new CompoundTag();
            minor.putInt("ShipLevel", profile.getLevel());
            minor.putInt("ExpCurrent", profile.getExperience());
            minor.putInt("ExpNext", (profile.getLevel() + 1) * 20);
            minor.putInt("NumAmmoLight", profile.getAmmoLight());
            minor.putInt("NumAmmoHeavy", profile.getAmmoHeavy());
            minor.putInt("NumGrudge", profile.getGrudge());
            tag.put("Minor", minor);
            int[] bonus = {profile.getModernHealthSteps(), profile.getModernAttackSteps(), profile.getModernDefenseSteps(),
                    profile.getModernAttackSpeedSteps(), profile.getModernSpeedSteps(), profile.getModernRangeSteps()};
            tag.putIntArray("Bonus", bonus);
            LegacyShipStats stats = profile.buildStats(player.getActiveEffects());
            putFloats(tag, "Buffed", stats.copyBuffed());
            float[] formation = new float[LegacyShipStatTables.ATTR_COUNT];
            float[] morale = new float[formation.length];
            for (int i = 0; i < formation.length; i++) {
                formation[i] = stats.formationStat(i);
                morale[i] = stats.moraleStat(i);
            }
            putFloats(tag, "Formation", formation);
            putFloats(tag, "MoraleValues", morale);
        }
        this.view.apply(tag);
    }

    private static void putFloats(CompoundTag tag, String key, float[] values) {
        int[] bits = new int[values.length];
        for (int i = 0; i < bits.length; i++) bits[i] = Float.floatToIntBits(values[i]);
        tag.putIntArray(key, bits);
    }

    public Player getPlayer() { return this.player; }
    public RandomSource getRNG() { return this.random; }
    public ShipEntitySpec getSpec() { return this.view.getSpec(); }
    public int getShipType() { return this.view.getShipType(); }
    public int getShipClass() { return this.view.getShipClass(); }
    public float[] getModelPos() { return this.view.getModelPos(); }
    public boolean isCarrier() { return this.view.isCarrier(); }
    public boolean isInvisibleShip() { return this.view.isInvisibleShip(); }
    public float getInvisibleLevel() { return this.view.getInvisibleLevel(); }
    public int getMorale() { return this.view.getMorale(); }
    public int getStateMinor(String key) { return this.view.getStateMinor(key); }
    public int getStateEmotion(String key) {
        if (this.profile == null) return 0;
        return switch (key) {
            case "State" -> this.profile.getModelState();
            case "Emotion" -> this.profile.getEmotion();
            case "Emotion4" -> this.profile.getEmotion4();
            default -> 0;
        };
    }
    public boolean getStateFlag(String key) {
        return switch (key) {
            case "NoFuel" -> this.profile != null && this.profile.isNoFuel();
            case "UseRingEffect" -> this.profile != null && this.profile.hasAuraEffect();
            case "ShowHeldItem" -> this.profile != null && this.profile.isShowHeldItem();
            case "IsMarried" -> this.profile != null && this.profile.isMarried();
            default -> this.view.getStateFlag(key);
        };
    }
    public boolean isSitting() { return this.profile != null && this.profile.isSitting(); }
    public String getCustomNameTag() { return this.view.getCustomNameTag(); }
    public int getAttrsBonus(int id) { return this.view.getAttrsBonus(id); }
    public float getAttrsBuffed(int id) { return this.view.getAttrsBuffed(id); }
    public float getAttrsFormation(int id) { return this.view.getAttrsFormation(id); }
    public float getAttrsMorale(int id) { return this.view.getAttrsMorale(id); }
    public float getAttackRange() { return this.view.getAttackRange(); }
    public float calcMissRate(float distance) { return this.view.calcMissRate(distance); }
    public int getNumAircraftLight() { return this.view.getNumAircraftLight(); }
    public int getNumAircraftHeavy() { return this.view.getNumAircraftHeavy(); }
}
