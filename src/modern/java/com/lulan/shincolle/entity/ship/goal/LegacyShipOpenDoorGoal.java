package com.lulan.shincolle.entity.ship.goal;

import com.lulan.shincolle.entity.ship.LegacyShipEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import com.lulan.shincolle.entity.ship.path.ShipPathPoint;
import com.lulan.shincolle.entity.ship.path.ShipPath;

import java.util.ArrayList;
import java.util.List;

/**
 * Mirrors 1.12.2 EntityAIShipOpenDoor: opens wooden doors and fence gates
 * along the ship's path and optionally closes them after passing.
 */
public class LegacyShipOpenDoorGoal extends Goal {

    private final LegacyShipEntity ship;
    private final boolean closeDoor;
    private final double checkDistanceSq;
    private final List<BlockPos> doors = new ArrayList<>();
    private BlockPos pathPoint = BlockPos.ZERO;
    private boolean hasPassed;
    private float vecX;
    private float vecZ;
    private int delay;

    public LegacyShipOpenDoorGoal(LegacyShipEntity ship, boolean closeDoor) {
        this.ship = ship;
        this.closeDoor = closeDoor;
        float radius = ship.getBbWidth() + 1.0F;
        this.checkDistanceSq = radius * radius;
    }

    @Override
    public boolean canUse() {
        if (!this.ship.horizontalCollision) {
            return false;
        }

        ShipPath path = this.ship.getShipNavigate().getPath();
        if (path == null || path.isFinished()) {
            return false;
        }

        this.doors.clear();
        int limit = Math.min(path.getCurrentPathIndex() + 2, path.getCurrentPathLength());
        for (int i = 0; i < limit; i++) {
            ShipPathPoint node = path.getPathPointFromIndex(i);
            BlockPos pos = new BlockPos(node.xCoord, node.yCoord, node.zCoord);
            if (this.ship.distanceToSqr(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D) <= this.checkDistanceSq) {
                this.collectDoors(pos);
                this.pathPoint = pos;
            }
        }

        BlockPos selfPos = this.ship.blockPosition();
        this.collectDoors(selfPos);
        this.pathPoint = selfPos;

        return !this.doors.isEmpty();
    }

    @Override
    public boolean canContinueToUse() {
        return this.closeDoor && this.delay > 0 && !this.hasPassed;
    }

    @Override
    public void start() {
        this.delay = 30;
        this.hasPassed = false;
        this.setDoorsOpen(true);
        if (!this.pathPoint.equals(BlockPos.ZERO)) {
            this.vecX = this.pathPoint.getX() + 0.5F - (float) this.ship.getX();
            this.vecZ = this.pathPoint.getZ() + 0.5F - (float) this.ship.getZ();
        }
    }

    @Override
    public void stop() {
        if (this.closeDoor && !this.doors.isEmpty()) {
            this.setDoorsOpen(false);
        }
        this.doors.clear();
    }

    @Override
    public void tick() {
        this.delay--;
        if (this.delay <= 0) {
            float vx = this.pathPoint.getX() + 0.5F - (float) this.ship.getX();
            float vz = this.pathPoint.getZ() + 0.5F - (float) this.ship.getZ();
            if (this.vecX * vx + this.vecZ * vz < 0.0F) {
                this.hasPassed = true;
            }
        }
    }

    private void collectDoors(BlockPos center) {
        int range = Mth.floor(this.ship.getBbWidth() + 1.0F);
        int minY = center.getY();
        int maxY = minY + Mth.floor(this.ship.getBbHeight() + 1.0F);

        for (int ix = center.getX() - range; ix <= center.getX() + range; ix++) {
            for (int iz = center.getZ() - range; iz <= center.getZ() + range; iz++) {
                for (int iy = minY; iy <= maxY; iy++) {
                    BlockPos candidate = new BlockPos(ix, iy, iz);
                    BlockState state = this.ship.level().getBlockState(candidate);
                    Block block = state.getBlock();
                    if (block instanceof FenceGateBlock
                            || (iy != minY && block instanceof DoorBlock door && door.type().canOpenByHand())) {
                        if (!this.doors.contains(candidate)) {
                            this.doors.add(candidate);
                        }
                    }
                }
            }
        }
    }

    private void setDoorsOpen(boolean open) {
        for (BlockPos pos : this.doors) {
            BlockState state = this.ship.level().getBlockState(pos);
            Block block = state.getBlock();
            if (block instanceof DoorBlock door) {
                if (door.type().canOpenByHand() && state.getValue(DoorBlock.OPEN) != open) {
                    door.setOpen(this.ship, this.ship.level(), state, pos, open);
                }
            } else if (block instanceof FenceGateBlock) {
                boolean isOpen = state.getValue(FenceGateBlock.OPEN);
                if (isOpen != open) {
                    BlockState updated = state.setValue(FenceGateBlock.OPEN, open);
                    if (open) {
                        Direction facing = this.ship.getDirection();
                        if (updated.getValue(FenceGateBlock.FACING) == facing.getOpposite()) {
                            updated = updated.setValue(FenceGateBlock.FACING, facing);
                        }
                    }
                    this.ship.level().setBlock(pos, updated, 10);
                    this.ship.level().gameEvent(this.ship, open ? GameEvent.BLOCK_OPEN : GameEvent.BLOCK_CLOSE, pos);
                }
            }
        }
    }
}
