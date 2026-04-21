package com.lulan.shincolle.playerskill;

import com.lulan.shincolle.entity.ship.LegacyShipEntity;
import com.lulan.shincolle.morph.MorphHostMode;
import com.lulan.shincolle.morph.MorphProfile;

import javax.annotation.Nullable;

public record PlayerSkillHostContext(
        MorphHostMode mode,
        @Nullable LegacyShipEntity ship,
        @Nullable MorphProfile profile) {

    private static final PlayerSkillHostContext NONE = new PlayerSkillHostContext(MorphHostMode.NONE, null, null);

    public static PlayerSkillHostContext none() {
        return NONE;
    }

    public boolean visible() {
        return this.mode != MorphHostMode.NONE && (this.ship != null || this.profile != null);
    }

    public int hostShipUid() {
        return this.ship != null ? Math.max(0, this.ship.getShipUid()) : 0;
    }

    public int hostClassId() {
        if (this.ship != null) {
            return Math.max(0, this.ship.getShipClassId());
        }
        return this.profile != null ? Math.max(0, this.profile.getLegacyClassId()) : 0;
    }
}
