package com.lulan.shincolle.morph;

import net.minecraft.server.level.ServerPlayer;

import javax.annotation.Nullable;

public interface MorphCompatBridge {

    MorphCompatBridge NOOP = new MorphCompatBridge() {
        @Override
        public String bridgeId() {
            return "none";
        }
    };

    default String bridgeId() {
        return "soft-noop";
    }

    default boolean isAvailable() {
        return false;
    }

    default String describeCapabilities() {
        return "sync=false,reset=false,attack_delegate=false";
    }

    default void syncActiveMorph(ServerPlayer player, @Nullable MorphProfile profile, boolean active) {
    }

    default void reset(ServerPlayer player) {
    }

    default boolean canDelegatePlayerSkill(ServerPlayer player) {
        return false;
    }
}
