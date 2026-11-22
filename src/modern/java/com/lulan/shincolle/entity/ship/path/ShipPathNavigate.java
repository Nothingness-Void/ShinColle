package com.lulan.shincolle.entity.ship.path;
import javax.annotation.Nullable;
import com.lulan.shincolle.entity.ship.path.ShipNavigationHost;
import net.minecraft.world.level.PathNavigationRegion;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.phys.AABB;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.Level;
public class ShipPathNavigate
{
    private static int UpdatePathDelay = 20;
    private Mob host;
    private ShipNavigationHost hostShip;
    private Level world;
    @Nullable
    private ShipPath currentPath;
    private double speed;
    private int pathTicks;
    private int ticksAtLastPos;
    private Vec3 lastPosCheck = Vec3.ZERO;
    private Vec3 lastPosStuck = Vec3.ZERO;
    private long timeoutTimer = 0L;
    private long lastTimeoutCheck = 0L;
    private double timeoutLimit;
    private float maxDistanceToWaypoint = 0.5F;
    private int hostCeilWeight, hostCeilHight;
    private long lastTimeUpdated;
    private BlockPos targetPos;
    public ShipPathNavigate(Mob entity)
    {
        this.host = entity;
        this.hostShip = (ShipNavigationHost) entity;
        this.world = entity.level();
        this.maxDistanceToWaypoint = (float) Math.max(this.host.getBbWidth() * 0.75D, 0.75D);
        this.hostCeilWeight = Mth.ceil(this.host.getBbWidth());
        this.hostCeilHight = Mth.ceil(this.host.getBbHeight());
    }
    public void setSpeed(double par1)
    {
        this.speed = par1;
    }
    public float getPathSearchRange()
    {
        if (host instanceof ShipNavigationHost)
        {
            return 70F;
        }
        else
        {
            return 48F;
        }
    }
    public boolean tryMoveToXYZ(double x, double y, double z, double speed)
    {
        ShipPath path = this.getPathToXYZ(Mth.floor(x), Mth.floor(y), Mth.floor(z));
        return this.setPath(path, speed);
    }
    public ShipPath getPathToXYZ(double x, double y, double z)
    {
        return !this.canNavigate() ? null : this.getShipPathToXYZ(this.host, Mth.floor(x), Mth.floor(y), Mth.floor(z), this.getPathSearchRange(), this.hostShip.canFly());
    }
    public ShipPath getShipPathToXYZ(Entity entity, int x, int y, int z, float range, boolean canFly)
    {
        BlockPos pos = new BlockPos(x, y, z);
        if (this.currentPath != null && !this.currentPath.isFinished() && pos.equals(this.targetPos))
        {
            return this.currentPath;
        }
        this.targetPos = pos;
        this.world.getProfiler().push("pathfind");
        BlockPos hostPos = entity.blockPosition();
        int i = (int)(range + 8.0F);
        PathNavigationRegion chunkcache = new PathNavigationRegion(this.world, hostPos.offset(-i, -i, -i), hostPos.offset(i, i, i));
        ShipPath pathentity = (new ShipPathFinder(chunkcache, canFly)).findPath(entity, x, y, z, range);
        this.world.getProfiler().pop();
        return pathentity;
    }
    public ShipPath getPathToEntityLiving(Entity entity)
    {
        return !this.canNavigate() ? null : this.getPathEntityToEntity(this.host, entity, this.getPathSearchRange(), this.hostShip.canFly());
    }
    public ShipPath getPathEntityToEntity(Entity entity, Entity targetEntity, float range, boolean canFly)
    {
        BlockPos pos = targetEntity.blockPosition();
        if (this.currentPath != null && !this.currentPath.isFinished() && pos.equals(this.targetPos))
        {
            return this.currentPath;
        }
        this.targetPos = pos;
        this.world.getProfiler().push("pathfind");
        BlockPos hostPos = (entity.blockPosition()).above();
        int i = (int)(range + 16.0F);
        PathNavigationRegion chunkcache = new PathNavigationRegion(this.world, hostPos.offset(-i, -i, -i), hostPos.offset(i, i, i));
        ShipPath pathentity = (new ShipPathFinder(chunkcache, canFly)).findPath(entity, targetEntity, range);
        this.world.getProfiler().pop();
        return pathentity;
    }
    public boolean tryMoveToEntityLiving(Entity entity, double speed)
    {
        ShipPath pathentity = this.getPathToEntityLiving(entity);
        return pathentity != null ? this.setPath(pathentity, speed) : false;
    }
    public boolean setPath(ShipPath pathEntity, double speed)
    {
        if (pathEntity == null)
        {
            this.currentPath = null;
            return false;
        }
        else
        {
            if (pathEntity.getCurrentPathLength() == 0)
            {
                return false;
            }
            else
            {
                this.currentPath = pathEntity;
                this.speed = speed;
                Vec3 vec3 = this.getEntityPosition();
                this.ticksAtLastPos = this.pathTicks;
                this.lastPosCheck = vec3;
                return true;
            }
        }
    }
    public ShipPath getPath()
    {
        return this.currentPath;
    }
    public void onUpdateNavigation()
    {
        if (host.tickCount > 40)
        {
            ++this.pathTicks;
            if (!this.noPath())
            {
                if (this.canNavigate())
                {
                    this.pathFollow();
                }
                if (!this.noPath())
                {
                    Vec3 vec3 = this.currentPath.getPosition(this.host);
                    if (vec3 != null)
                    {
                        BlockPos blockPos = (BlockPos.containing(vec3)).below();
                        net.minecraft.world.phys.shapes.VoxelShape shape = this.world.getBlockState(blockPos).getShape(this.world, blockPos);
                        AABB blockAABB = shape.isEmpty() ? new AABB(0, 0, 0, 1, 1, 1) : shape.bounds();
                        vec3 = vec3.subtract(0.0D, 1.0D - blockAABB.maxY, 0.0D);
                        this.hostShip.getShipMoveHelper().setMoveTo(vec3.x, vec3.y + 0.1D, vec3.z, this.speed);
                    }
                }
            }
        }
    }
    private void pathFollow()
    {
        Vec3 hostPos = this.getEntityPosition();
        int i = this.currentPath.getCurrentPathLength();
        for (int j = this.currentPath.getCurrentPathIndex(); j < this.currentPath.getCurrentPathLength(); ++j)
        {
            if ((double)this.currentPath.getPathPointFromIndex(j).yCoord != Math.floor(hostPos.y))
            {
                i = j;
                break;
            }
        }
        Vec3 nowPos = this.currentPath.getCurrentPos();
        if (Mth.abs((float)(this.host.getX() - nowPos.x - 0.5D)) < this.maxDistanceToWaypoint && Mth.abs((float)(this.host.getZ() - nowPos.z - 0.5D)) < this.maxDistanceToWaypoint)
        {
            this.currentPath.setCurrentPathIndex(this.currentPath.getCurrentPathIndex() + 1);
        }
        for (int j1 = i - 1; j1 >= this.currentPath.getCurrentPathIndex(); --j1)
        {
            if (this.isDirectPathBetweenPoints(hostPos, this.currentPath.getVectorFromIndex(this.host, j1), this.hostCeilWeight, this.hostCeilHight, this.hostCeilWeight))
            {
                this.currentPath.setCurrentPathIndex(j1);
                break;
            }
        }
        this.checkForStuck(hostPos);
    }
    protected void checkForStuck(Vec3 pos)
    {
        int checkTick = this.pathTicks - this.ticksAtLastPos;
        if (checkTick % 32 == 0)
        {
            boolean isStuck = false;
            double dist = pos.distanceToSqr(this.lastPosCheck);
            if(dist < 1D)
            {
                isStuck = true;
                if (!currentPath.isFinished())
                {
                    float dx = (float) (currentPath.getVectorFromIndex(this.host, currentPath.getCurrentPathIndex()).x - host.getX());
                    float dz = (float) (currentPath.getVectorFromIndex(this.host, currentPath.getCurrentPathIndex()).z - host.getZ());
                    double targetX = 0D;
                    double targetZ = 0D;
                    targetX = dx > 0.1F ? 1D : dx < -0.1F ? -1D : 0D;
                    targetZ = dz > 0.1F ? 1D : dz < -0.1F ? -1D : 0D;
                    this.host.setDeltaMovement(this.speed * 0.5D * targetX, this.host.getDeltaMovement().y, this.speed * 0.5D * targetZ);
                    if (host.getRandom().nextInt(2) == 0)
                    {
                        host.getJumpControl().jump();
                        float speed = (float) host.getSpeed() * 0.35F;
                        this.host.setDeltaMovement(this.host.getDeltaMovement().add(dx > 0.2F ? speed : dx < 0.2F ? -speed : 0, 0,
                                dz > 0.2F ? speed : dz < 0.2F ? -speed : 0));
                    }
                }
            }
            if (checkTick > 100)
            {
                if(isStuck)
                {
                    this.clearPathEntity();
                }
                this.ticksAtLastPos = this.pathTicks;
            }
            this.lastPosCheck = pos;
        }
        if (this.currentPath != null && !this.currentPath.isFinished())
        {
            Vec3 vec3d = this.currentPath.getCurrentPos();
            if (!vec3d.equals(this.lastPosStuck))
            {
                this.lastPosStuck = vec3d;
                double d0 = pos.distanceTo(this.lastPosStuck);
                this.timeoutLimit = this.host.getSpeed() > 0F ? d0 / (double)this.host.getSpeed() * 1000D : 0D;
            }
            else
            {
                this.timeoutTimer += System.currentTimeMillis() - this.lastTimeoutCheck;
            }
            if (this.timeoutLimit > 0D && (double)this.timeoutTimer > this.timeoutLimit * 3D)
            {
                this.lastPosStuck = Vec3.ZERO;
                this.timeoutTimer = 0L;
                this.timeoutLimit = 0D;
                this.clearPathEntity();
            }
            this.lastTimeoutCheck = System.currentTimeMillis();
        }
    }
    public boolean noPath()
    {
        return this.currentPath == null || this.currentPath.isFinished();
    }
    public void clearPathEntity()
    {
        this.currentPath = null;
    }
    private Vec3 getEntityPosition()
    {
        return new Vec3(this.host.getX(), this.host.getY() + 0.5D, this.host.getZ());
    }
    private double getPathableYPos()
    {
        if (this.hostShip.canFly())
        {
            return this.host.getY();
        }
        else
        {
             int i = Mth.floor(this.host.getY());
             BlockState block = this.world.getBlockState(new BlockPos(Mth.floor(this.host.getX()), i, Mth.floor(this.host.getZ())));
             int j = 0;
             do
             {
                 if (!block.isAir())
                 {
                     if (ShipNavigationHost.isLiquid(block))
                     {
                         return i + 0.4D;
                     }
                     else
                     {
                         return i;
                     }
                 }
                 ++i;
                 block = this.world.getBlockState(new BlockPos(Mth.floor(this.host.getX()), i, Mth.floor(this.host.getZ())));
                 ++j;
             }
             while (j <= 24);
             return Mth.floor(this.host.getY());
        }
    }
    private boolean canNavigate()
    {
        return !host.isPassenger() && (this.hostShip.canFly() || this.host.onGround() || ShipNavigationHost.isFree(host));
    }
    private boolean isInLiquid()
    {
        return ShipNavigationHost.isInLiquid(host);
    }
    private boolean isDirectPathBetweenPoints(Vec3 pos1, Vec3 pos2, int sizeX, int sizeY, int sizeZ)
    {
        int x1 = Mth.floor(pos1.x);
        int y1 = Mth.floor(pos1.y);
        int z1 = Mth.floor(pos1.z);
        double xOffset = pos2.x - pos1.x;
        double yOffset = pos2.x - pos1.x;
        double zOffset = pos2.z - pos1.z;
        double offsetSq = xOffset * xOffset + zOffset * zOffset + yOffset * yOffset;
        if (offsetSq < 1.0E-8D)
        {
            return false;
        }
        else
        {
            double offsetVec = 1D / Math.sqrt(offsetSq);
            xOffset = xOffset * offsetVec;
            yOffset = yOffset * offsetVec;
            zOffset = zOffset * offsetVec;
            sizeX = sizeX + 2;
            sizeY = sizeY + 1;
            sizeZ = sizeZ + 2;
            if (!this.isSafeToStandAt(x1, y1, z1, sizeX, sizeY, sizeZ, pos1, xOffset, zOffset))
            {
                return false;
            }
            else
            {
                sizeX = sizeX - 2;
                sizeY = sizeY - 1;
                sizeZ = sizeZ - 2;
                double unitX = 1D / Math.abs(xOffset);
                double unitY = 1D / Math.abs(yOffset);
                double unitZ = 1D / Math.abs(zOffset);
                double proX = (double)x1 - pos1.x;
                double proY = (double)y1 - pos1.y;
                double proZ = (double)z1 - pos1.z;
                if (xOffset >= 0D) ++proX;
                if (yOffset >= 0D) ++proY;
                if (zOffset >= 0D) ++proZ;
                proX = proX / xOffset;
                proY = proY / yOffset;
                proZ = proZ / zOffset;
                int dirX = xOffset < 0D ? -1 : 1;
                int dirY = yOffset < 0D ? -1 : 1;
                int dirZ = zOffset < 0D ? -1 : 1;
                int x2 = Mth.floor(pos2.x);
                int y2 = Mth.floor(pos2.y);
                int z2 = Mth.floor(pos2.z);
                int xIntOffset = x2 - x1;
                int yIntOffset = y2 - y1;
                int zIntOffset = z2 - z1;
                while (xIntOffset * dirX > 0 || yIntOffset * dirY > 0 || zIntOffset * dirZ > 0)
                {
                    switch (proX <= proY ? (proX <= proZ ? 1 : 3) : (proY <= proZ ? 2 : 3))
                    {
                    case 1:
                        proX += unitX;
                        x1 += dirX;
                        xIntOffset = x2 - x1;
                    break;
                    case 2:
                        proY += unitY;
                        y1 += dirY;
                        yIntOffset = y2 - y1;
                    break;
                    case 3:
                        proZ += unitZ;
                        z1 += dirZ;
                        zIntOffset = z2 - z1;
                    break;
                    }
                    if (!this.isSafeToStandAt(x1, y1, z1, sizeX, sizeY, sizeZ, pos1, xOffset, zOffset))
                    {
                        return false;
                    }
                }
                return true;
            }
        }
    }
    private boolean isSafeToStandAt(int xOffset, int yOffset, int zOffset, int xSize, int ySize, int zSize, Vec3 orgPos, double vecX, double vecZ)
    {
        if (this.hostShip.canFly()) return true;
        int xSize2 = xOffset - xSize / 2;
        int zSize2 = zOffset - zSize / 2;
        if (!this.isPositionClear(xSize2, yOffset, zSize2, xSize, ySize, zSize, orgPos, vecX, vecZ))
        {
            return false;
        }
        else
        {
            for (int x1 = xSize2; x1 < xSize2 + xSize; ++x1)
            {
                for (int z1 = zSize2; z1 < zSize2 + zSize; ++z1)
                {
                    double x2 = x1 + 0.5D - orgPos.x;
                    double z2 = z1 + 0.5D - orgPos.z;
                    if (x2 * vecX + z2 * vecZ >= 0D)
                    {
                        BlockState block = this.world.getBlockState(new BlockPos(x1, yOffset - 1, z1));
                        if(block.isAir())
                        {
                            return false;
                        }
                    }
                }
            }
            return true;
        }
    }
    private boolean isPositionClear(int xOffset, int yOffset, int zOffset, int xSize, int ySize, int zSize, Vec3 orgPos, double vecX, double vecZ)
    {
        for (BlockPos blockpos : BlockPos.betweenClosed(new BlockPos(xOffset, yOffset, zOffset), new BlockPos(xOffset + xSize - 1, yOffset + ySize - 1, zOffset + zSize - 1)))
        {
            double d0 = (double)blockpos.getX() + 0.5D - orgPos.x;
            double d1 = (double)blockpos.getZ() + 0.5D - orgPos.z;
            if (d0 * vecX + d1 * vecZ >= 0D)
            {
                Block block = this.world.getBlockState(blockpos).getBlock();
                if (!this.world.getBlockState(blockpos).isPathfindable(net.minecraft.world.level.pathfinder.PathComputationType.LAND))
                {
                    return false;
                }
            }
        }
        return true;
    }
}
