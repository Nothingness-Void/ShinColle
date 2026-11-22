package com.lulan.shincolle.entity.mount;

import com.lulan.shincolle.entity.ship.ShipEntitySpec;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;

public final class LegacyMountControls {

    public static final int STYLE_AIRFIELD = 0;
    public static final int STYLE_BATTLESHIP = 1;
    public static final int STYLE_CARRIER = 2;
    public static final int STYLE_CARRIER_WD = 3;
    public static final int STYLE_HARBOUR = 4;
    public static final int STYLE_ISOLATED = 5;
    public static final int STYLE_MIDWAY = 6;
    public static final int STYLE_SUBMARINE = 7;

    public static final int KEY_FORWARD = 1;
    public static final int KEY_BACK = 2;
    public static final int KEY_LEFT = 4;
    public static final int KEY_RIGHT = 8;
    public static final int KEY_JUMP = 16;

    private LegacyMountControls() {
    }

    public static Vec3 resolveKeyMovement(Player rider, int keyMask, double speed, boolean jumpAllowed) {
        float forward = ((keyMask & KEY_FORWARD) != 0 ? 1.0F : 0.0F)
                - ((keyMask & KEY_BACK) != 0 ? 1.0F : 0.0F);
        if (forward < 0.0F) {
            forward *= 0.3F;
        }
        float strafe = (((keyMask & KEY_LEFT) != 0 ? 1.0F : 0.0F)
                - ((keyMask & KEY_RIGHT) != 0 ? 1.0F : 0.0F)) * 0.8F;
        float yawRadians = rider.getYRot() * ((float) Math.PI / 180.0F);
        double sin = Mth.sin(yawRadians);
        double cos = Mth.cos(yawRadians);
        double moveX = (double) (strafe * cos - forward * sin) * speed;
        double moveZ = (double) (forward * cos + strafe * sin) * speed;
        double moveY = jumpAllowed && (keyMask & KEY_JUMP) != 0 ? 0.42D : 0.0D;
        return new Vec3(moveX, moveY, moveZ);
    }
    static double resolveWaterSurfaceBuoyancy(Entity entity) {
        BlockPos origin = BlockPos.containing(entity.getX(), entity.getBoundingBox().minY, entity.getZ());
        Level level = entity.level();
        if (level.getFluidState(origin).isEmpty()) {
            return 0.0D;
        }

        double depth = 1.0D;
        boolean foundSurface = false;
        int maxY = level.getMaxBuildHeight();
        for (int y = origin.getY() + 1; y < maxY; y++) {
            BlockPos scanPos = new BlockPos(origin.getX(), y, origin.getZ());
            if (!level.getFluidState(scanPos).isEmpty()) {
                depth++;
                continue;
            }
            foundSurface = level.getBlockState(scanPos).isAir();
            break;
        }
        if (!foundSurface) {
            return 0.0D;
        }

        double shipDepth = depth - (entity.getY() - Mth.floor(entity.getY()));
        if (shipDepth <= 0.45D) {
            return 0.0D;
        }
        if (shipDepth > 4.0D) {
            return 0.025D;
        }
        if (shipDepth > 2.0D) {
            return 0.015D;
        }
        if (shipDepth > 1.3D) {
            return 0.007D;
        }
        return 0.003D;
    }
    public static int styleForSpec(@Nullable ShipEntitySpec spec) {
        if (spec == null) {
            return STYLE_BATTLESHIP;
        }

        return switch (spec.textureStem()) {
            case "EntityAirfieldHime" -> STYLE_AIRFIELD;
            case "EntityCarrierHime", "EntityCarrierAkagi", "EntityCarrierKaga", "EntityCarrierWo" -> STYLE_CARRIER;
            case "EntityCarrierWDemon" -> STYLE_CARRIER_WD;
            case "EntityHarbourHime" -> STYLE_HARBOUR;
            case "EntityIsolatedHime" -> STYLE_ISOLATED;
            case "EntityMidwayHime", "EntityNorthernHime" -> STYLE_MIDWAY;
            case "EntitySubmHime", "EntitySubmHimeNew" -> STYLE_SUBMARINE;
            default -> switch (spec.archetype()) {
                case SUBMARINE -> STYLE_SUBMARINE;
                case CARRIER -> STYLE_CARRIER;
                case INSTALLATION -> STYLE_AIRFIELD;
                case DESTROYER, CRUISER, BATTLESHIP, TRANSPORT, PRINCESS -> STYLE_BATTLESHIP;
            };
        };
    }
}
