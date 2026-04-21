package com.lulan.shincolle.playerskill;

import com.lulan.shincolle.entity.mount.LegacyMountEntity;
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

        if (player.getVehicle() instanceof LegacyShipEntity ship
                && !ship.isHostileVariant()
                && ship.canCommanderEdit(player)) {
            return new PlayerSkillHostContext(MorphHostMode.RIDER, ship, null);
        }

        TeitokuData data = TeitokuHelper.get(player).resolve().orElse(null);
        MorphProfile profile = data != null && data.hasActiveMorph() ? data.getSelectedMorphProfile() : null;
        if (profile == null) {
            return PlayerSkillHostContext.none();
        }

        if (player.getVehicle() instanceof LegacyMountEntity mount && mount.isOwnedBy(player)) {
            return new PlayerSkillHostContext(MorphHostMode.MOUNT, null, profile);
        }

        return new PlayerSkillHostContext(MorphHostMode.MORPH, null, profile);
    }
}
