package com.lulan.shincolle.entity.ship.goal;

import com.lulan.shincolle.entity.ship.LegacyShipEntity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.target.TargetGoal;
import net.minecraft.world.entity.player.Player;

import javax.annotation.Nullable;
import java.util.Comparator;
import java.util.List;

public class LegacyShipHostilePlayerTargetGoal extends TargetGoal {

    private final LegacyShipEntity ship;
    @Nullable
    private Player candidate;

    public LegacyShipHostilePlayerTargetGoal(LegacyShipEntity ship) {
        super(ship, false);
        this.ship = ship;
    }

    @Override
    public boolean canUse() {
        if (!this.ship.canExecuteCombatGoal()
                || !this.ship.isHostileVariant()
                || this.ship.tickCount % 8 != 0) {
            return false;
        }

        List<Player> candidates = this.ship.level().getEntitiesOfClass(
                Player.class,
                this.ship.getHostileTargetSearchArea(),
                this.ship::canEngage);
        if (candidates.isEmpty()) {
            return false;
        }

        candidates.sort(Comparator.comparingDouble(this.ship::distanceToSqr));
        this.candidate = candidates.size() > 2 ? candidates.get(this.ship.getRandom().nextInt(3)) : candidates.get(0);
        return true;
    }

    @Override
    public boolean canContinueToUse() {
        LivingEntity target = this.ship.getTarget();
        return this.ship.canExecuteCombatGoal()
                && this.ship.isHostileVariant()
                && target instanceof Player
                && target.isAlive()
                && this.ship.canEngage(target);
    }

    @Override
    public void start() {
        this.mob.setTarget(this.candidate);
        super.start();
    }

    @Override
    public void stop() {
        LivingEntity target = this.ship.getTarget();
        if (target instanceof Player
                && (!this.ship.canExecuteCombatGoal() || !this.ship.isHostileVariant() || !this.ship.canEngage(target))) {
            this.ship.setTarget(null);
        }
        this.candidate = null;
        super.stop();
    }
}
