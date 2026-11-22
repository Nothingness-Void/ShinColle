package com.lulan.shincolle.entity.ship.goal;

import com.lulan.shincolle.entity.ship.LegacyShipBehaviorCatalog;
import com.lulan.shincolle.entity.ship.LegacyShipEntity;
import com.lulan.shincolle.config.ShinColleConfig;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;

public class LegacyShipPickItemGoal extends Goal {

    private static final double PICKUP_REACH_SQR = 9.0D;

    private final LegacyShipEntity ship;
    private final double speedModifier;
    @Nullable
    private ItemEntity targetItem;
    private int nextSearchTick;
    private int timeToRecalcPath;
    private int pickupCooldown;

    public LegacyShipPickItemGoal(LegacyShipEntity ship, double speedModifier) {
        this.ship = ship;
        this.speedModifier = speedModifier;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        if (!this.canRunPickupLogic()) {
            return false;
        }
        if (this.ship.tickCount < this.nextSearchTick) {
            return false;
        }

        this.targetItem = this.findNearestItem();
        this.nextSearchTick = this.ship.tickCount + this.adjustedTickDelay(16);
        return this.targetItem != null;
    }

    @Override
    public boolean canContinueToUse() {
        return this.canRunPickupLogic()
                && this.targetItem != null
                && this.targetItem.isAlive();
    }

    @Override
    public void start() {
        this.timeToRecalcPath = 0;
    }

    @Override
    public void stop() {
        this.targetItem = null;
        if (this.ship.getTarget() == null) {
            this.ship.getShipNavigate().clearPathEntity();
        }
    }

    @Override
    public void tick() {
        if (this.targetItem == null) {
            return;
        }

        if (--this.timeToRecalcPath <= 0) {
            this.timeToRecalcPath = this.adjustedTickDelay(16);
            this.ship.getShipNavigate().tryMoveToEntityLiving(this.targetItem, this.speedModifier);
        }

        if (this.pickupCooldown > 0) {
            this.pickupCooldown--;
        }

        if (this.pickupCooldown <= 0 && this.ship.distanceToSqr(this.targetItem) < PICKUP_REACH_SQR) {
            this.pickupTargetItem();
        }
    }

    private boolean canRunPickupLogic() {
        return !this.ship.isHostileVariant()
                && LegacyShipBehaviorCatalog.supportsItemPickup(this.ship.getSpec())
                && this.ship.isAiPickItem()
                && !this.ship.isOrderedToSit()
                && !this.ship.isPassenger()
                && !this.ship.hasNoFuel()
                && this.ship.getLegacyCraneState() < 1
                && this.ship.getFishingHook() == null
                && this.hasAnyCargoSpace();
    }

    private boolean hasAnyCargoSpace() {
        for (int slot = LegacyShipEntity.EQUIPMENT_SLOT_COUNT; slot < this.ship.getInventorySizePaged(); slot++) {
            ItemStack stack = this.ship.getShipInventory().getItem(slot);
            if (stack.isEmpty()) {
                return true;
            }
        }

        return false;
    }

    private @Nullable ItemEntity findNearestItem() {
        double pickupRangeBase = LegacyShipBehaviorCatalog.pickupRangeBaseFor(this.ship.getSpec());
        double range = Math.min(pickupRangeBase + this.ship.getFollowMax(),
                pickupRangeBase + this.ship.getCompatAttackRange() * 0.5D);
        List<ItemEntity> nearbyItems = this.ship.level().getEntitiesOfClass(ItemEntity.class,
                this.ship.getBoundingBox().inflate(range, range * 0.5D + 1.0D, range),
                item -> item != null
                        && item.isAlive()
                        && !item.getItem().isEmpty());
        nearbyItems.sort(Comparator.comparingDouble(this.ship::distanceToSqr));
        return nearbyItems.isEmpty() ? null : nearbyItems.get(0);
    }

    private void pickupTargetItem() {
        if (this.targetItem == null) {
            return;
        }

        this.pickupCooldown = this.getPickupCooldown();
        if (this.targetItem.hasPickUpDelay()) {
            this.ship.getShipNavigate().clearPathEntity();
            return;
        }

        ItemStack stack = this.targetItem.getItem();
        if (stack.isEmpty()) {
            this.targetItem.discard();
            this.targetItem = null;
            return;
        }

        int moved = 0;
        int originalCount = stack.getCount();
        while (moved < originalCount && this.ship.storeSingleItem(stack)) {
            moved++;
        }

        if (moved <= 0) {
            return;
        }

        stack.shrink(moved);
        this.ship.take(this.targetItem, moved);
        this.ship.level().playSound(null, this.ship.getX(), this.ship.getY(), this.ship.getZ(),
                SoundEvents.ITEM_PICKUP, this.ship.getSoundSource(), (float) ShinColleConfig.volumeShip(),
                ((this.ship.getRandom().nextFloat() - this.ship.getRandom().nextFloat()) * 0.7F + 1.0F) * 2.0F);
        this.ship.playLegacyItemPickupVoice();
        this.ship.addShipExperience(LegacyShipBehaviorCatalog.pickupExperience());

        if (stack.isEmpty()) {
            this.targetItem.discard();
            this.targetItem = null;
        }
    }

    private int getPickupCooldown() {
        float attackSpeed = Math.max(1.0F, this.ship.getLegacyStats().attackSpeed());
        return Math.max(0, (int) (10.0F / attackSpeed));
    }
}
