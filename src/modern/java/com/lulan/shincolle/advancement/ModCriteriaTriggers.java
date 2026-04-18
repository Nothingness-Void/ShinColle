package com.lulan.shincolle.advancement;

import net.minecraft.advancements.CriteriaTriggers;

public final class ModCriteriaTriggers {

    public static final FriendlyShipDeployedTrigger FRIENDLY_SHIP_DEPLOYED =
            CriteriaTriggers.register(new FriendlyShipDeployedTrigger());

    private ModCriteriaTriggers() {
    }

    public static void init() {
        // Static holder method to force trigger registration during mod bootstrap.
    }
}
