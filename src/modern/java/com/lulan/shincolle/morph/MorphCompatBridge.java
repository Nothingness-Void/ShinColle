package com.lulan.shincolle.morph;

import net.minecraft.server.level.ServerPlayer;

public interface MorphCompatBridge {

    MorphCompatBridge NOOP = new MorphCompatBridge() {
    };

    default void syncActiveMorph(ServerPlayer player, MorphProfile profile, boolean active) {
    }
}
