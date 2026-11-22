package com.lulan.shincolle.entity.ship.path;
public class ShipPathHeap
{
    private ShipPathPoint[] pathPoints = new ShipPathPoint[128];
    private int count;
    public ShipPathPoint[] getPathPoints()
    {
        return pathPoints;
    }
    public int getCount()
    {
        return count;
    }
    public ShipPathPoint addPoint(ShipPathPoint point)
    {
        if (point.index >= 0)
        {
            throw new IllegalStateException("OW KNOWS!");
        }
        else
        {
            if (this.count == this.pathPoints.length)
            {
                ShipPathPoint[] apathpoint = new ShipPathPoint[this.count << 1];
                System.arraycopy(this.pathPoints, 0, apathpoint, 0, this.count);
                this.pathPoints = apathpoint;
            }
            this.pathPoints[this.count] = point;
            point.index = this.count;
            this.sortToRoot(this.count++);
            return point;
        }
    }
    public void clearPath()
    {
        this.count = 0;
    }
    public ShipPathPoint dequeue()
    {
        ShipPathPoint pathpoint = this.pathPoints[0];
        this.pathPoints[0] = this.pathPoints[--this.count];
        this.pathPoints[this.count] = null;
        if (this.count > 0)
        {
            this.sortToLeaf(0);
        }
        pathpoint.index = -1;
        return pathpoint;
    }
    public void changeDistance(ShipPathPoint point, float dist)
    {
        float f1 = point.distanceToTarget;
        point.distanceToTarget = dist;
        if (dist < f1)
        {
            this.sortToRoot(point.index);
        }
        else
        {
            this.sortToLeaf(point.index);
        }
    }
    private void sortToRoot(int id)
    {
        ShipPathPoint fromNode = this.pathPoints[id];
        int j;
        for (float f = fromNode.distanceToTarget; id > 0; id = j)
        {
            j = id - 1 >> 1;
            ShipPathPoint parentNode = this.pathPoints[j];
            if (f >= parentNode.distanceToTarget)
            {
                break;
            }
            this.pathPoints[id] = parentNode;
            parentNode.index = id;
        }
        this.pathPoints[id] = fromNode;
        fromNode.index = id;
    }
    private void sortToLeaf(int id)
    {
        ShipPathPoint fromNode = this.pathPoints[id];
        float fromDist = fromNode.distanceToTarget;
        while (true)
        {
            int left = 1 + (id << 1);
            int right = left + 1;
            if (left >= this.count)
            {
                break;
            }
            ShipPathPoint leftNode = this.pathPoints[left];
            ShipPathPoint rightNode;
            float leftDist = leftNode.distanceToTarget;
            float rightDist;
            if (right >= this.count)
            {
                rightNode = null;
                rightDist = Float.POSITIVE_INFINITY;
            }
            else
            {
                rightNode = this.pathPoints[right];
                rightDist = rightNode.distanceToTarget;
            }
            if (leftDist < rightDist)
            {
                if (leftDist >= fromDist)
                {
                    break;
                }
                this.pathPoints[id] = leftNode;
                leftNode.index = id;
                id = left;
            }
            else
            {
                if (rightDist >= fromDist)
                {
                    break;
                }
                this.pathPoints[id] = rightNode;
                rightNode.index = id;
                id = right;
            }
        }
        this.pathPoints[id] = fromNode;
        fromNode.index = id;
    }
    public boolean isPathEmpty()
    {
        return this.count == 0;
    }
}
