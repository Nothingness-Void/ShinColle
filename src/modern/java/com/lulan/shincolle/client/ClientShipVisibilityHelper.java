package com.lulan.shincolle.client;

import com.lulan.shincolle.entity.ship.LegacyShipBehaviorCatalog;
import com.lulan.shincolle.entity.ship.LegacyShipEntity;
import com.lulan.shincolle.teitoku.TeitokuData;
import com.lulan.shincolle.teitoku.TeitokuHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;

public final class ClientShipVisibilityHelper {

    private ClientShipVisibilityHelper() {
    }

    public static boolean isOwnedOrAlliedToClientPlayer(LegacyShipEntity ship) {
        Player player = Minecraft.getInstance().player;
        if (player == null || ship == null) {
            return false;
        }
        return ship.isOwnedBy(player)
                || LegacyShipBehaviorCatalog.hasLegacySameOwner(ship, player)
                || ship.isAlliedTo(player);
    }

    public static int getClientMarriageNum() {
        Player player = Minecraft.getInstance().player;
        if (player == null) {
            return -1;
        }
        return TeitokuHelper.get(player)
                .map(TeitokuData::getMarriageNum)
                .orElse(-1);
    }
}
