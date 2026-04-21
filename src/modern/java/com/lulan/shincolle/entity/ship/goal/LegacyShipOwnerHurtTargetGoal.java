package com.lulan.shincolle.entity.ship.goal;

import com.lulan.shincolle.entity.ship.LegacyShipEntity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.target.TargetGoal;
import net.minecraft.world.entity.player.Player;

public class LegacyShipOwnerHurtTargetGoal extends TargetGoal {

    private final LegacyShipEntity ship;
    private LivingEntity ownerLastHurt;
    private int timestamp;

    public LegacyShipOwnerHurtTargetGoal(LegacyShipEntity ship) {
        super(ship, false);
        this.ship = ship;
    }

    @Override
    public boolean canUse() {
        Player owner = this.ship.getOwnerPlayer();

        if (owner == null || this.ship.isHostileVariant() || this.ship.isOrderedToSit()) {
            return false;
        }

        this.ownerLastHurt = owner.getLastHurtMob();
        int ownerTimestamp = owner.getLastHurtMobTimestamp();
        return ownerTimestamp != this.timestamp && this.ship.canEngage(this.ownerLastHurt);
    }

    @Override
    public void start() {
        this.mob.setTarget(this.ownerLastHurt);
        Player owner = this.ship.getOwnerPlayer();
        this.timestamp = owner == null ? 0 : owner.getLastHurtMobTimestamp();
        super.start();
    }
}
