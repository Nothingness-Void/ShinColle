package com.lulan.shincolle.entity.ship.path;
import com.lulan.shincolle.entity.ship.path.ShipNavigationHost;
import net.minecraft.world.entity.Mob;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.ai.attributes.Attributes;
public class ShipMoveHelper
{
    private Mob entity;
    private ShipNavigationHost entityN;
    private double posX;
    private double posY;
    private double posZ;
    private double speed;
    private float rotateLimit;
    private int stuckTick;
    protected Action action = Action.WAIT;
    public ShipMoveHelper(Mob entity, float rotlimit)
    {
        this.entity = entity;
        this.entityN = (ShipNavigationHost) entity;
        this.posX = entity.getX();
        this.posY = entity.getY();
        this.posZ = entity.getZ();
        this.rotateLimit = rotlimit;
        this.stuckTick = 0;
    }
    public boolean isUpdating()
    {
        return this.action == Action.MOVE_TO;
    }
    public double getSpeed()
    {
        return this.speed;
    }
    public void setMoveTo(double x, double y, double z, double speed)
    {
        this.posX = x;
        this.posY = y;
        this.posZ = z;
        this.speed = speed;
        this.action = Action.MOVE_TO;
    }
    public void onUpdateMoveHelper()
    {
        this.entity.setZza(0F);
        if (this.action == Action.MOVE_TO)
        {
            this.action = Action.WAIT;
            double x1 = this.posX - this.entity.getX();
            double y1 = this.posY - this.entity.getY();
            double z1 = this.posZ - this.entity.getZ();
            double moveSq = x1 * x1 + y1 * y1 + z1 * z1;
            if (moveSq > 0.001D)
            {
                float f = (float)(Mth.atan2(z1, x1) * (180D / Math.PI)) - 90F;
                float moveSpeed = (float) this.entity.getAttributeValue(Attributes.MOVEMENT_SPEED);
                moveSpeed *= this.speed;
                this.entity.setYRot(this.limitAngle(this.entity.getYRot(), f, this.rotateLimit));
                if (entityN.canFly())
                {
                    if(y1 > 0.5D)
                    {
                        this.entity.setDeltaMovement(this.entity.getDeltaMovement().add(0, moveSpeed * 0.12D, 0));
                        moveSpeed *= 0.8F;
                    }
                    else if (y1 < -0.5D)
                    {
                        this.entity.setDeltaMovement(this.entity.getDeltaMovement().add(0, -moveSpeed * 0.16D, 0));
                        moveSpeed *= 0.92F;
                    }
                }
                else if (ShipNavigationHost.isInLiquid(this.entity))
                {
                    if (y1 > 1D)
                    {
                        this.entity.setDeltaMovement(this.entity.getDeltaMovement().add(0, moveSpeed * 0.2D, 0));
                        moveSpeed *= 0.5F;
                    }
                    else if (y1 > 0.35D)
                    {
                        this.entity.setDeltaMovement(this.entity.getDeltaMovement().add(0, moveSpeed * 0.1D, 0));
                        moveSpeed *= 0.5F;
                    }
                    else if (y1 < -1D)
                    {
                        this.entity.setDeltaMovement(this.entity.getDeltaMovement().add(0, -moveSpeed * 0.25D, 0));
                        moveSpeed *= 0.82F;
                    }
                }
                else if (y1 > this.entity.maxUpStep() && x1 * x1 + z1 * z1 < 1D)
                {
                    this.entity.getJumpControl().jump();
                }
                this.entity.setSpeed(moveSpeed);
            }
            else
            {
                this.entity.setZza(0F);
                return;
            }
        }
        else
        {
            this.entity.setZza(0F);
        }
    }
    private float limitAngle(float yaw, float degree, float limit)
    {
        float f = Mth.wrapDegrees(degree - yaw);
        if (f > limit)
        {
            f = limit;
        }
        if (f < -limit)
        {
            f = -limit;
        }
        float f1 = yaw + f;
        if (f1 < 0.0F)
        {
            f1 += 360.0F;
        }
        else if (f1 > 360.0F)
        {
            f1 -= 360.0F;
        }
        return f1;
    }
    public static enum Action
    {
        WAIT,
        MOVE_TO;
    }
}
