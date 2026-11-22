package com.lulan.shincolle.client.model.legacy.compat;

import com.lulan.shincolle.entity.ship.LegacyShipEntity;
import com.lulan.shincolle.entity.mount.LegacyShipMountEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;

public class BasicEntityShip extends EntityLivingBase implements IShipEmotion, IShipFloating, IShipRiderType {
    public final LegacyShipEntity ship;
    public BasicEntityShip(LegacyShipEntity ship) { super(ship); this.ship = ship; }
    public int getStateEmotion(int id) { return ship.getLegacyModelEmotion(id); }
    public void setStateEmotion(int id, int value, boolean sync) { ship.setLegacyModelEmotion(id, value, sync); }
    public int getStateMinor(int id) {
        return switch (id) {
            case ID.M.Morale -> ship.getMorale();
            case ID.M.ShipUID -> ship.getShipUid();
            default -> throw new IllegalArgumentException("Unsupported model state " + id);
        };
    }
    public boolean getStateFlag(int id) {
        return switch (id) {
            case ID.F.NoFuel -> ship.hasNoFuel() || ship.deathTime > 0;
            case ID.F.HeadTilt -> ship.getLegacyModelHeadTilt();
            default -> throw new IllegalArgumentException("Unsupported model flag " + id);
        };
    }
    public void setStateFlag(int id, boolean value) {
        if (id != ID.F.HeadTilt) throw new IllegalArgumentException("Unsupported mutable model flag " + id);
        ship.setLegacyModelHeadTilt(value);
    }
    public int getFaceTick() { return ship.getLegacyModelFaceTick(); }
    public int getHeadTiltTick() { return ship.getLegacyModelHeadTiltTick(); }
    public int getAttackTick() { return ship.getLegacyModelAttackTick(); }
    public int getAttackTick2() { return ship.getLegacyModelAttackTick2(); }
    public void setFaceTick(int value) { ship.setLegacyModelFaceTick(value); }
    public void setHeadTiltTick(int value) { ship.setLegacyModelHeadTiltTick(value); }
    public void setAttackTick(int value) { ship.setLegacyModelAttackTick(value); }
    public void setAttackTick2(int value) { ship.setLegacyModelAttackTick2(value); }
    public int getTickExisted() { return ship.tickCount; }
    public float getSwingTime(float partialTick) { return ship.getAttackAnim(partialTick); }
    public boolean getIsRiding() { return ship.isPassenger(); }
    public boolean getIsSprinting() { return ship.isSprinting() || ship.walkAnimation.speed() > 0.9F; }
    public boolean getIsSitting() { return ship.isOrderedToSit(); }
    public boolean isSitting() { return getIsSitting(); }
    public boolean getIsSneaking() { return ship.isShiftKeyDown(); }
    public int getRidingState() { return ship.getLegacyRidingState(); }
    public int getRiderType() { return ship.getLegacyRiderType(); }
    public int getScaleLevel() { return ship.getLegacyHostileScaleLevel(); }
    public RandomSource getRand() { return ship.getRandom(); }
    public RandomSource getRNG() { return ship.getRandom(); }
    public double getShipDepth() { return ship.getLegacyLiquidDepth(); }
    public double getShipDepth(int type) {
        if (type == 2) return 0D;
        if (type == 1 && ship.getVehicle() instanceof LegacyShipEntity mount) return mount.getLegacyLiquidDepth();
        if (type == 1 && ship.getVehicle() instanceof LegacyShipMountEntity) {
            var mount = ship.getVehicle();
            BlockPos origin = mount.blockPosition();
            if (mount.level().getFluidState(origin).isEmpty()) return 0D;
            double depth = 1D;
            for (int y = origin.getY() + 1; y < mount.level().getMaxBuildHeight(); y++) {
                if (mount.level().getFluidState(new BlockPos(origin.getX(), y, origin.getZ())).isEmpty()) break;
                depth++;
            }
            return depth - (mount.getY() - Mth.floor(mount.getY()));
        }
        return getShipDepth();
    }
}
