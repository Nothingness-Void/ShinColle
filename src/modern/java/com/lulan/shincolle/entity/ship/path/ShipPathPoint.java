package com.lulan.shincolle.entity.ship.path;
import net.minecraft.util.Mth;
public class ShipPathPoint
{
    public final int xCoord;
    public final int yCoord;
    public final int zCoord;
    private final int hash;
    public int index = -1;
    public float totalPathDistance;
    public float distanceToNext;
    public float distanceToTarget;
    public ShipPathPoint previous;
    public boolean visited;
    public float distanceFromOrigin = 0F;
    public float cost = 0F;
    public float costMalus = 0F;
    public ShipPathPoint(int x, int y, int z)
    {
        this.xCoord = x;
        this.yCoord = y;
        this.zCoord = z;
        this.hash = makeHash(x, y, z);
    }
    public static int makeHash(int x, int y, int z)
    {
        return y & 255 | (x & 32767) << 8 | (z & 32767) << 24 | (x < 0 ? Integer.MIN_VALUE : 0) | (z < 0 ? 32768 : 0);
    }
    public ShipPathPoint clonePoint(int x, int y, int z)
    {
        ShipPathPoint pathpoint = new ShipPathPoint(x, y, z);
        pathpoint.index = this.index;
        pathpoint.totalPathDistance = this.totalPathDistance;
        pathpoint.distanceToNext = this.distanceToNext;
        pathpoint.distanceToTarget = this.distanceToTarget;
        pathpoint.previous = this.previous;
        pathpoint.visited = this.visited;
        pathpoint.distanceFromOrigin = this.distanceFromOrigin;
        pathpoint.cost = this.cost;
        pathpoint.costMalus = this.costMalus;
        return pathpoint;
    }
    public float distanceTo(ShipPathPoint point)
    {
        float f = point.xCoord - this.xCoord;
        float f1 = point.yCoord - this.yCoord;
        float f2 = point.zCoord - this.zCoord;
        return Mth.sqrt(f * f + f1 * f1 + f2 * f2);
    }
    public float distanceToSquared(ShipPathPoint point)
    {
        float f = point.xCoord - this.xCoord;
        float f1 = point.yCoord - this.yCoord;
        float f2 = point.zCoord - this.zCoord;
        return f * f + f1 * f1 + f2 * f2;
    }
    public float distanceManhattan(ShipPathPoint point)
    {
        float f = (float)Math.abs(point.xCoord - this.xCoord);
        float f1 = (float)Math.abs(point.yCoord - this.yCoord);
        float f2 = (float)Math.abs(point.zCoord - this.zCoord);
        return f + f1 + f2;
    }
    @Override
    public boolean equals(Object obj)
    {
        if (!(obj instanceof ShipPathPoint))
        {
            return false;
        }
        else
        {
            ShipPathPoint pathpoint = (ShipPathPoint) obj;
            return this.hash == pathpoint.hash && this.xCoord == pathpoint.xCoord && this.yCoord == pathpoint.yCoord && this.zCoord == pathpoint.zCoord;
        }
    }
    @Override
    public int hashCode()
    {
        return this.hash;
    }
    public boolean isAssigned()
    {
        return this.index >= 0;
    }
    @Override
    public String toString()
    {
        return this.xCoord + ", " + this.yCoord + ", " + this.zCoord;
    }
}
