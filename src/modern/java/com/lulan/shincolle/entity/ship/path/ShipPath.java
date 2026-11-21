package com.lulan.shincolle.entity.ship.path;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
public class ShipPath
{
    private final ShipPathPoint[] points;
    private int currentPathIndex;
    private int pathLength;
    public ShipPath(ShipPathPoint[] pathpoint)
    {
        this.points = pathpoint;
        this.pathLength = pathpoint.length;
    }
    public void incrementPathIndex()
    {
        ++this.currentPathIndex;
    }
    public boolean isFinished()
    {
        return this.currentPathIndex >= this.pathLength;
    }
    public ShipPathPoint getFinalPathPoint()
    {
        return this.pathLength > 0 ? this.points[this.pathLength - 1] : null;
    }
    public ShipPathPoint getPathPointFromIndex(int i)
    {
        return this.points[i];
    }
    public int getCurrentPathLength()
    {
        return this.pathLength;
    }
    public void setCurrentPathLength(int i)
    {
        this.pathLength = i;
    }
    public int getCurrentPathIndex()
    {
        return this.currentPathIndex;
    }
    public void setCurrentPathIndex(int i)
    {
        this.currentPathIndex = i;
    }
    public Vec3 getVectorFromIndex(Entity entity, int i)
    {
        if (i >= points.length) i = points.length - 1;
        double d0 = this.points[i].xCoord + ((int)(entity.getBbWidth() + 1.0F)) * 0.5D;
        double d1 = this.points[i].yCoord;
        double d2 = this.points[i].zCoord + ((int)(entity.getBbWidth() + 1.0F)) * 0.5D;
        return new Vec3(d0, d1, d2);
    }
    public Vec3 getPosition(Entity entity)
    {
        return this.getVectorFromIndex(entity, this.currentPathIndex);
    }
    public Vec3 getCurrentPos()
    {
        ShipPathPoint pathpoint = this.points[this.currentPathIndex];
        return new Vec3((double)pathpoint.xCoord, (double)pathpoint.yCoord, (double)pathpoint.zCoord);
    }
    public boolean isSamePath(ShipPath path)
    {
        if (path == null)
        {
            return false;
        }
        else if (path.points.length != this.points.length)
        {
            return false;
        }
        else
        {
            for (int i = 0; i < this.points.length; ++i)
            {
                if (this.points[i].xCoord != path.points[i].xCoord || this.points[i].yCoord != path.points[i].yCoord || this.points[i].zCoord != path.points[i].zCoord)
                {
                    return false;
                }
            }
            return true;
        }
    }
    public boolean isDestinationSame(Vec3 vec)
    {
        ShipPathPoint pathpoint = this.getFinalPathPoint();
        return pathpoint == null ? false : pathpoint.xCoord == (int)vec.x && pathpoint.zCoord == (int)vec.z;
    }
}
