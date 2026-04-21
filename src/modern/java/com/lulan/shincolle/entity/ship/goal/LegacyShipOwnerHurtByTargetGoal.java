package com.lulan.shincolle.entity.ship.goal;

import com.lulan.shincolle.entity.ship.LegacyShipEntity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.target.TargetGoal;
import net.minecraft.world.entity.player.Player;

public class LegacyShipOwnerHurtByTargetGoal extends TargetGoal {

    private final LegacyShipEntity ship;
    private LivingEntity ownerLastHurtBy;
    private int timestamp;

    public LegacyShipOwnerHurtByTargetGoal(LegacyShipEntity ship) {
        super(ship, false);
        this.ship = ship;
    }

    @Override
    public boolean canUse() {
        Player owner = this.ship.getOwnerPlayer();

        if (owner == null || this.ship.isHostileVariant() || this.ship.isOrderedToSit()) {
            return false;
        }

        this.ownerLastHurtBy = owner.getLastHurtByMob();
        int ownerTimestamp = owner.getLastHurtByMobTimestamp();
        return ownerTimestamp != this.timestamp && this.ship.canEngage(this.ownerLastHurtBy);
    }

    @Override
    public void start() {
        this.mob.setTarget(this.ownerLastHurtBy);
        Player owner = this.ship.getOwnerPlayer();
        this.timestamp = owner == null ? 0 : owner.getLastHurtByMobTimestamp();
        super.start();
    }
}
