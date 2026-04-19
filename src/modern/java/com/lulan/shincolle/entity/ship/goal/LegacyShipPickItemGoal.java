package com.lulan.shincolle.entity.ship.goal;

import com.lulan.shincolle.entity.ship.LegacyShipEntity;
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
        this.nextSearchTick = this.ship.tickCount + this.adjustedTickDelay(12);
        return this.targetItem != null;
    }

    @Override
    public boolean canContinueToUse() {
        return this.canRunPickupLogic()
                && this.targetItem != null
                && this.targetItem.isAlive()
                && this.hasCargoSpaceFor(this.targetItem.getItem());
    }

    @Override
    public void start() {
        this.timeToRecalcPath = 0;
    }

    @Override
    public void stop() {
        this.targetItem = null;
        if (this.ship.getTarget() == null) {
            this.ship.getNavigation().stop();
        }
    }

    @Override
    public void tick() {
        if (this.targetItem == null) {
            return;
        }

        if (--this.timeToRecalcPath <= 0) {
            this.timeToRecalcPath = this.adjustedTickDelay(10);
            this.ship.getNavigation().moveTo(this.targetItem, this.speedModifier);
        }

        if (this.ship.distanceToSqr(this.targetItem) <= PICKUP_REACH_SQR) {
            this.pickupTargetItem();
        }
    }

    private boolean canRunPickupLogic() {
        return this.ship.canExecuteCombatGoal()
                && !this.ship.isHostileVariant()
                && this.ship.isAiAutoSupply()
                && !this.ship.isOrderedToSit()
                && !this.ship.hasActiveCommandState()
                && this.ship.getTarget() == null
                && this.hasAnyCargoSpace();
    }

    private boolean hasAnyCargoSpace() {
        for (int slot = LegacyShipEntity.EQUIPMENT_SLOT_COUNT; slot < this.ship.getShipInventory().getContainerSize(); slot++) {
            ItemStack stack = this.ship.getShipInventory().getItem(slot);
            if (stack.isEmpty() || stack.getCount() < stack.getMaxStackSize()) {
                return true;
            }
        }

        return false;
    }

    private boolean hasCargoSpaceFor(ItemStack sourceStack) {
        for (int slot = LegacyShipEntity.EQUIPMENT_SLOT_COUNT; slot < this.ship.getShipInventory().getContainerSize(); slot++) {
            ItemStack current = this.ship.getShipInventory().getItem(slot);
            if (current.isEmpty()) {
                return true;
            }
            if (ItemStack.isSameItemSameTags(current, sourceStack) && current.getCount() < current.getMaxStackSize()) {
                return true;
            }
        }

        return false;
    }

    private @Nullable ItemEntity findNearestItem() {
        double range = Math.min(24.0D,
                Math.max(6.0D, this.ship.getAiFollowRange() * 0.75D + this.ship.getCompatAttackRange() * 0.35D));
        List<ItemEntity> nearbyItems = this.ship.level().getEntitiesOfClass(ItemEntity.class,
                this.ship.getBoundingBox().inflate(range, range * 0.5D + 1.0D, range),
                item -> item != null
                        && item.isAlive()
                        && !item.hasPickUpDelay()
                        && !item.getItem().isEmpty()
                        && this.hasCargoSpaceFor(item.getItem()));
        nearbyItems.sort(Comparator.comparingDouble(this.ship::distanceToSqr));
        return nearbyItems.isEmpty() ? null : nearbyItems.get(0);
    }

    private void pickupTargetItem() {
        if (this.targetItem == null) {
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
                SoundEvents.ITEM_PICKUP, this.ship.getSoundSource(), 0.2F,
                ((this.ship.getRandom().nextFloat() - this.ship.getRandom().nextFloat()) * 0.7F + 1.0F) * 2.0F);

        if (stack.isEmpty()) {
            this.targetItem.discard();
            this.targetItem = null;
        }
    }
}
