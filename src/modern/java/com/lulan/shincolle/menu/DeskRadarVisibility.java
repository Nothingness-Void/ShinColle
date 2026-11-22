package com.lulan.shincolle.menu;

import net.minecraft.world.entity.Entity;
import javax.annotation.Nullable;

public final class DeskRadarVisibility {
    private DeskRadarVisibility() {
    }

    public static boolean isVisible(@Nullable Entity ship, int minimumHeight) {
        return ship != null && ship.getY() > minimumHeight;
    }
}
