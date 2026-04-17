package com.lulan.shincolle.entity.ship;

import net.minecraft.world.effect.MobEffects;

/**
 * Central dispatch point for legacy per-ship behavior that is visible in single-player.
 */
public final class LegacyShipBehaviorCatalog {

    public enum MarriagePassive {
        NONE,
        SELF_AND_OWNER_INVISIBILITY,
        ALLIED_JUMP_AURA,
        OWNER_HASTE,
        OWNER_JUMP,
        OWNER_STRENGTH,
        OWNER_SPEED
    }

    public record Behavior(int legacyClassId, boolean hostile, MarriagePassive marriagePassive) {
        public boolean hasMarriagePassive() {
            return this.marriagePassive != MarriagePassive.NONE;
        }
    }

    private LegacyShipBehaviorCatalog() {
    }

    public static Behavior behaviorFor(ShipEntitySpec spec) {
        return behaviorFor(spec.legacyClassId(), spec.hostile());
    }

    public static Behavior behaviorFor(int legacyClassId, boolean hostile) {
        return new Behavior(legacyClassId, hostile, hostile
                ? MarriagePassive.NONE
                : marriagePassiveForFriendlyClassId(legacyClassId));
    }

    public static LegacyShipAttackProfile attackProfile(ShipEntitySpec spec) {
        return LegacyShipAttackProfile.resolve(spec);
    }

    public static void tickMarriagePassive(LegacyShipEntity ship) {
        Behavior behavior = behaviorFor(ship.getSpec());
        if (!behavior.hasMarriagePassive()) {
            return;
        }

        int shipLevel = ship.getShipLevel();
        switch (behavior.marriagePassive()) {
            case SELF_AND_OWNER_INVISIBILITY -> {
                ship.applyLegacyRingEffect(ship, MobEffects.INVISIBILITY, 40 + shipLevel, 0);
                ship.applyOwnerRingEffect(MobEffects.INVISIBILITY, 40 + shipLevel, 0);
            }
            case ALLIED_JUMP_AURA -> ship.applyCarrierRingAura(50 + shipLevel, shipLevel / 85);
            case OWNER_HASTE -> ship.applyOwnerRingEffect(MobEffects.DIG_SPEED, 80 + shipLevel, shipLevel / 30);
            case OWNER_JUMP -> ship.applyOwnerRingEffect(MobEffects.JUMP, 80 + shipLevel, shipLevel / 45 + 1);
            case OWNER_STRENGTH -> ship.applyOwnerRingEffect(MobEffects.DAMAGE_BOOST, 80 + shipLevel, shipLevel / 50);
            case OWNER_SPEED -> ship.applyOwnerRingEffect(MobEffects.MOVEMENT_SPEED, 80 + shipLevel, shipLevel / 45);
            case NONE -> {
            }
        }
    }

    private static MarriagePassive marriagePassiveForFriendlyClassId(int legacyClassId) {
        return switch (legacyClassId) {
            case 38, 39 -> MarriagePassive.SELF_AND_OWNER_INVISIBILITY;
            case 47, 48 -> MarriagePassive.ALLIED_JUMP_AURA;
            case 51 -> MarriagePassive.OWNER_HASTE;
            case 52 -> MarriagePassive.OWNER_JUMP;
            case 53 -> MarriagePassive.OWNER_STRENGTH;
            case 54 -> MarriagePassive.OWNER_SPEED;
            default -> MarriagePassive.NONE;
        };
    }
}
