package com.lulan.shincolle.morph;

import com.mojang.logging.LogUtils;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.fml.ModList;
import org.slf4j.Logger;

import javax.annotation.Nullable;

public final class MorphCompatBridgeLoader {

    private static final Logger LOGGER = LogUtils.getLogger();

    private MorphCompatBridgeLoader() {
    }

    public static MorphCompatBridge load() {
        boolean metamorphPresent = ModList.get().isLoaded("metamorph");
        MorphCompatBridge bridge = metamorphPresent
                ? new PlaceholderExternalBridge("metamorph")
                : MorphCompatBridge.NOOP;
        LOGGER.info("Morph compat bridge loaded: id={}, available={}, capabilities={}",
                bridge.bridgeId(), bridge.isAvailable(), bridge.describeCapabilities());
        return bridge;
    }

    private static final class PlaceholderExternalBridge implements MorphCompatBridge {

        private final String targetModId;

        private PlaceholderExternalBridge(String targetModId) {
            this.targetModId = targetModId;
        }

        @Override
        public String bridgeId() {
            return this.targetModId + "-soft-bridge";
        }

        @Override
        public String describeCapabilities() {
            return "mod_present=true,sync_hook=true,reset_hook=true,attack_delegate_hook=true";
        }

        @Override
        public void syncActiveMorph(ServerPlayer player, @Nullable MorphProfile profile, boolean active) {
        }

        @Override
        public void reset(ServerPlayer player) {
        }
    }
}
