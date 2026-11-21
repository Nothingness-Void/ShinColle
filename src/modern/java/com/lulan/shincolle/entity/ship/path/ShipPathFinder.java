package com.lulan.shincolle.entity.ship.path;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import javax.annotation.Nullable;
import com.lulan.shincolle.entity.ship.path.ShipPathFinder.EnumPathType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.FenceBlock;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.WaterlilyBlock;
import net.minecraft.world.level.block.BaseRailBlock;
import net.minecraft.world.level.block.WallBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.Entity;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.BlockGetter;
public class ShipPathFinder
{
    public enum EnumPathType { BLOCKED, OPEN, FLUID, OPENABLE, FENCE }
    private BlockGetter world;
    private ShipPathHeap path = new ShipPathHeap();
    private Int2ObjectOpenHashMap<ShipPathPoint> pointMap = new Int2ObjectOpenHashMap<ShipPathPoint>();
    private boolean isPathingInAir;
    private boolean canEntityFly;
    public ShipPathFinder(BlockGetter world, boolean canFly)
    {
        this.world = world;
        this.isPathingInAir = false;
        this.canEntityFly = canFly;
    }
    @Nullable
    public ShipPath findPath(Entity fromEnt, Entity toEnt, float range)
    {
        return this.findPath(fromEnt, toEnt.getX(), toEnt.getBoundingBox().minY, toEnt.getZ(), range);
    }
    @Nullable
    public ShipPath findPath(Entity entity, int x, int y, int z, float range)
    {
        return this.findPath(entity, x + 0.5F, y + 0.5F, z + 0.5F, range);
    }
    @Nullable
    private ShipPath findPath(Entity entity, double x, double y, double z, float range)
    {
        this.path.clearPath();
        this.pointMap.clear();
        int i;
        i = Mth.floor(entity.getBoundingBox().minY + 0.5D);
        ShipPathPoint startpp = this.openPoint(Mth.floor(entity.getBoundingBox().minX), i, Mth.floor(entity.getBoundingBox().minZ));
        ShipPathPoint endpp = this.openPoint(Mth.floor(x - entity.getBbWidth() * 0.5F), Mth.floor(y), Mth.floor(z - entity.getBbWidth() * 0.5F));
        ShipPathPoint entitySize = new ShipPathPoint(Mth.floor(entity.getBbWidth() + 1F), Mth.floor(entity.getBbHeight() + 1F), Mth.floor(entity.getBbWidth() + 1F));
        ShipPath pathentity = this.findPath(entity, startpp, endpp, entitySize, range);
        return pathentity;
    }
    @Nullable
    private ShipPath findPath(Entity entity, ShipPathPoint startpp, ShipPathPoint endpp, ShipPathPoint entitySize, float range)
    {
        startpp.totalPathDistance = 0F;
        startpp.distanceToNext = startpp.distanceManhattan(endpp);
        startpp.distanceToTarget = startpp.distanceToNext;
        this.path.clearPath();
        this.path.addPoint(startpp);
        ShipPathPoint ppTemp = startpp;
        int findCount = 0;
        while (!this.path.isPathEmpty())
        {
            ShipPathPoint ppDequeue = this.path.dequeue();
            findCount++;
            if (ppDequeue.equals(endpp))
            {
                return this.createEntityPath(startpp, endpp);
            }
            if (findCount > 450)
            {
                break;
            }
            if (ppDequeue.distanceToSquared(endpp) < ppTemp.distanceToSquared(endpp))
            {
                ppTemp = ppDequeue;
            }
            ppDequeue.visited = true;
            ShipPathPoint[] findOption = this.findPathOptions(entity, ppDequeue, entitySize, endpp, range);
            for (ShipPathPoint pp : findOption)
            {
                float dist = ppDequeue.distanceManhattan(pp);
                pp.distanceFromOrigin = ppDequeue.distanceFromOrigin + dist;
                pp.cost = dist + pp.costMalus;
                float dist2 = ppDequeue.totalPathDistance + pp.cost;
                if (pp.distanceFromOrigin < range && (!pp.isAssigned() || dist2 < pp.totalPathDistance))
                {
                    pp.previous = ppDequeue;
                    pp.totalPathDistance = dist2;
                    pp.distanceToNext = pp.distanceManhattan(endpp) + pp.costMalus;
                    if (pp.isAssigned())
                    {
                        this.path.changeDistance(pp, pp.totalPathDistance + pp.distanceToNext);
                    }
                    else
                    {
                        pp.distanceToTarget = pp.totalPathDistance + pp.distanceToNext;
                        this.path.addPoint(pp);
                    }
                }
            }
        }
        if(ppTemp == startpp)
        {
            return null;
        }
        else
        {
            return this.createEntityPath(startpp, ppTemp);
        }
    }
    private ShipPathPoint[] findPathOptions(Entity entity, ShipPathPoint currentpp, ShipPathPoint entitySize, ShipPathPoint targetpp, float range)
    {
        EnumPathType type = getPathType(entity, currentpp.xCoord, currentpp.yCoord + 1, currentpp.zCoord, entitySize);
        int pathYOffset = 0;
        if (type == EnumPathType.FLUID || type == EnumPathType.OPEN)
        {
            pathYOffset = Mth.floor(Math.max(1F, entity.maxUpStep()));
        }
        ShipPathPoint[] pp = new ShipPathPoint[10];
        pp[0] = this.getSafePoint(entity, currentpp.xCoord, currentpp.yCoord - 1, currentpp.zCoord, entitySize, pathYOffset);
        pp[1] = null;
        pp[2] = null;
        pp[3] = null;
        pp[4] = null;
        pp[5] = null;
        pp[6] = this.getSafePoint(entity, currentpp.xCoord, currentpp.yCoord, currentpp.zCoord - 1, entitySize, pathYOffset);
        pp[7] = this.getSafePoint(entity, currentpp.xCoord, currentpp.yCoord, currentpp.zCoord + 1, entitySize, pathYOffset);
        pp[8] = this.getSafePoint(entity, currentpp.xCoord + 1, currentpp.yCoord, currentpp.zCoord, entitySize, pathYOffset);
        pp[9] = this.getSafePoint(entity, currentpp.xCoord - 1, currentpp.yCoord, currentpp.zCoord, entitySize, pathYOffset);
        boolean fn = pp[6] == null || pp[6].costMalus != 0F;
        boolean fs = pp[7] == null || pp[7].costMalus != 0F;
        boolean fe = pp[8] == null || pp[8].costMalus != 0F;
        boolean fw = pp[9] == null || pp[9].costMalus != 0F;
        if (pathYOffset > 0)
        {
            pp[1] = this.getSafePoint(entity, currentpp.xCoord, currentpp.yCoord + 1, currentpp.zCoord, entitySize, pathYOffset);
        }
        if (fn && fw)
        {
            pp[2] = this.getSafePoint(entity, currentpp.xCoord - 1, currentpp.yCoord, currentpp.zCoord - 1, entitySize, pathYOffset);
        }
        if (fn && fe)
        {
            pp[3] = this.getSafePoint(entity, currentpp.xCoord + 1, currentpp.yCoord, currentpp.zCoord - 1, entitySize, pathYOffset);
        }
        if (fs && fw)
        {
            pp[4] = this.getSafePoint(entity, currentpp.xCoord - 1, currentpp.yCoord, currentpp.zCoord + 1, entitySize, pathYOffset);
        }
        if (fs && fe)
        {
            pp[5] = this.getSafePoint(entity, currentpp.xCoord + 1, currentpp.yCoord, currentpp.zCoord + 1, entitySize, pathYOffset);
        }
        List<ShipPathPoint> temp = new ArrayList<ShipPathPoint>();
        int len = 0;
        for (ShipPathPoint spp : pp)
        {
            if(spp != null && !spp.visited && spp.distanceTo(targetpp) < range)
            {
                temp.add(spp);
                len++;
            }
        }
        ShipPathPoint[] ret = temp.toArray(new ShipPathPoint[len]);
        return ret;
    }
    private ShipPathPoint getSafePoint(Entity entity, int x, int y, int z, ShipPathPoint entitySize, int pathYOffset)
    {
        ShipPathPoint pp = null;
        EnumPathType pathCase = getPathType(entity, x, y, z, entitySize);
        if (pathCase == EnumPathType.FLUID || pathCase == EnumPathType.OPENABLE)
        {
            return openPoint(x, y, z);
        }
        else
        {
            if (pathCase == EnumPathType.OPEN)
            {
                pp = openPoint(x, y, z);
            }
            if (pp == null)
            {
                if (pathYOffset > 1 || (pathYOffset > 0 && pathCase != EnumPathType.FENCE))
                {
                    pp = getSafePoint(entity, x, y + 1, z, entitySize, pathYOffset - 1);
                    pathCase = getPathType(entity, x, y + 1, z, entitySize);
                }
            }
            if (pp != null)
            {
                if (this.canEntityFly)
                {
                    return pp;
                }
                int j1 = 0;
                while (y > 0)
                {
                    EnumPathType downCase = getPathType(entity, x, y - 1, z, entitySize);
                    if (downCase == EnumPathType.FLUID)
                    {
                        pp = this.openPoint(x, y - 1, z);
                        break;
                    }
                    if (downCase != EnumPathType.OPEN)
                    {
                        break;
                    }
                    if (j1++ > 64)
                    {
                        return null;
                    }
                    --y;
                    if (y > 0)
                    {
                        pp = this.openPoint(x, y, z);
                    }
                }
            }
            return pp;
        }
    }
    private final ShipPathPoint openPoint(int x, int y, int z)
    {
        int l = ShipPathPoint.makeHash(x, y, z);
        ShipPathPoint pathpoint = (ShipPathPoint) this.pointMap.get(l);
        if (pathpoint == null)
        {
            pathpoint = new ShipPathPoint(x, y, z);
            this.pointMap.put(l, pathpoint);
        }
        return pathpoint;
    }
    public EnumPathType getPathType(Entity entity, int x, int y, int z, ShipPathPoint entitySize)
    {
        return getPathType(entity, x, y, z, entitySize, this.isPathingInAir);
    }
    public static EnumPathType getPathType(Entity entity, int x, int y, int z, ShipPathPoint entitySize, boolean inAir)
    {
        EnumSet<EnumPathType> pathset = EnumSet.noneOf(EnumPathType.class);
        boolean pathInLiquid = false;
        int doorCount = 0;
        if (ShipNavigationHost.isLiquid(entity.level().getBlockState(new BlockPos(x, y, z))))
        {
            pathInLiquid = true;
        }
        for (int x1 = x; x1 < x + entitySize.xCoord; ++x1)
        {
            for (int y1 = y + entitySize.yCoord - 1; y1 >= y; --y1)
            {
                for (int z1 = z; z1 < z + entitySize.zCoord; ++z1)
                {
                    BlockPos pos = new BlockPos(x1, y1, z1);
                    BlockState state = entity.level().getBlockState(pos);
                    Block block = state.getBlock();
                    EnumPathType type = EnumPathType.OPEN;
                    if (!state.isAir())
                    {
                        if (block instanceof FenceBlock ||
                            block instanceof WallBlock)
                        {
                            if (y1 == y) return EnumPathType.FENCE;
                            return EnumPathType.BLOCKED;
                        }
                        if (block instanceof BaseRailBlock ||
                            block == com.lulan.shincolle.registry.ModBlocks.BLOCK_WAYPOINT.get())
                        {
                            type = EnumPathType.OPEN;
                        }
                        else if (block instanceof DoorBlock)
                        {
                            doorCount++;
                            if (!((DoorBlock) block).type().canOpenByHand() || doorCount > 4) return EnumPathType.BLOCKED;
                            type = EnumPathType.OPENABLE;
                        }
                        else if (block instanceof FenceGateBlock)
                        {
                            type = EnumPathType.OPENABLE;
                        }
                        else if (ShipNavigationHost.isLiquid(state))
                        {
                            type = EnumPathType.FLUID;
                        }
                        else
                        {
                            if (block instanceof WaterlilyBlock ||
                                !state.isPathfindable(net.minecraft.world.level.pathfinder.PathComputationType.LAND))
                            {
                                return EnumPathType.BLOCKED;
                            }
                        }
                    }
                    pathset.add(type);
                }
            }
        }
        return pathInLiquid ? EnumPathType.FLUID : EnumPathType.OPEN;
    }
    private ShipPath createEntityPath(ShipPathPoint startpp, ShipPathPoint endpp)
    {
        int i = 1;
        ShipPathPoint pathpoint2;
        for (pathpoint2 = endpp; pathpoint2.previous != null; pathpoint2 = pathpoint2.previous)
        {
            ++i;
        }
        ShipPathPoint[] pathtemp = new ShipPathPoint[i];
        pathpoint2 = endpp;
        --i;
        for (pathtemp[i] = endpp; pathpoint2.previous != null; pathtemp[i] = pathpoint2)
        {
            pathpoint2 = pathpoint2.previous;
            --i;
        }
        return new ShipPath(pathtemp);
    }
}
