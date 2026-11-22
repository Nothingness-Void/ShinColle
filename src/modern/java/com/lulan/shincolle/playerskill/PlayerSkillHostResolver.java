package com.lulan.shincolle.playerskill;

import com.lulan.shincolle.config.ShinColleConfig;
import com.lulan.shincolle.entity.mount.LegacyShipMountEntity;
import com.lulan.shincolle.entity.ship.LegacyShipEntity;
import com.lulan.shincolle.morph.MorphHostMode;
import com.lulan.shincolle.morph.MorphProfile;
import com.lulan.shincolle.teitoku.TeitokuData;
import com.lulan.shincolle.teitoku.TeitokuHelper;
import net.minecraft.world.entity.player.Player;

public final class PlayerSkillHostResolver {

    private PlayerSkillHostResolver() {
    }

    public static PlayerSkillHostContext resolve(Player player) {
        if (player == null) {
            return PlayerSkillHostContext.none();
        }

        if (player.getVehicle() instanceof LegacyShipMountEntity shipMount) {
            LegacyShipEntity host = shipMount.getHostShip();
            if (host != null && sameOwner(player, host)) {
                return new PlayerSkillHostContext(MorphHostMode.MOUNT, host, null);
            }
            return PlayerSkillHostContext.none();
        }

        if (player.getFirstPassenger() instanceof LegacyShipEntity passengerShip) {
            return sameOwner(player, passengerShip)
                    ? new PlayerSkillHostContext(MorphHostMode.RIDER, passengerShip, null)
                    : PlayerSkillHostContext.none();
        }

        TeitokuData data = TeitokuHelper.get(player).orElse(null);
        MorphProfile profile = data != null && data.hasActiveMorph() ? data.getSelectedMorphProfile() : null;
        if (profile != null && ShinColleConfig.enableMetamorphSkill()) {
            return new PlayerSkillHostContext(MorphHostMode.MORPH, null, profile);
        }

        return PlayerSkillHostContext.none();
    }

    public static boolean sameOwner(Player player, LegacyShipEntity ship) {
        int uid = TeitokuHelper.getPlayerUid(player);
        return (uid > 0 || uid < -1) && uid == ship.getOwnerUid();
    }
}
