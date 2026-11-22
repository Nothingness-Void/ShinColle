package com.lulan.shincolle.client;

import com.lulan.shincolle.block.WaypointBlock;
import com.lulan.shincolle.client.particle.LegacyWaypointMarkerParticle;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;

public final class WaypointClientEffects {
    private WaypointClientEffects() {}

    public static void spawnMarker(BlockPos pos) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null || minecraft.player == null
                || !WaypointBlock.showsMarker(minecraft.player.getMainHandItem())) {
            return;
        }
        minecraft.particleEngine.add(new LegacyWaypointMarkerParticle(minecraft.level,
                pos.getX() + 0.5D, pos.getY() - 0.25D, pos.getZ() + 0.5D));
    }
}
