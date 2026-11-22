package com.lulan.shincolle.formation;

import com.lulan.shincolle.entity.ship.LegacyShipEntity;
import com.lulan.shincolle.config.ShinColleConfig;
import com.lulan.shincolle.network.ClientboundSpawnParticlePacket;
import com.lulan.shincolle.network.GameplayParticleType;
import com.lulan.shincolle.network.ModNetwork;
import com.lulan.shincolle.registry.ModBlocks;
import com.lulan.shincolle.registry.ModItems;
import com.lulan.shincolle.teitoku.TeitokuData;
import com.lulan.shincolle.teitoku.TeitokuHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.Vec3;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;

import javax.annotation.Nullable;

public final class LegacyFormationHelper {

    private LegacyFormationHelper() {
    }

    public static Vec3 getFormationGuardingPos(LegacyShipEntity ship, Entity target, double oldX, double oldZ) {
        return getFormationGuardingPos(ship.getLegacyFormationId(), ship.getLegacyFormationSlot(), target, oldX, oldZ);
    }

    public static Vec3 getFormationGuardingPos(int formationId, int slot, Entity target, double oldX, double oldZ) {
        if (formationId <= 0) {
            return target.position();
        }
        if (slot < 0 || slot > 5) {
            slot = 0;
        }
        BlockPos center = new BlockPos(Mth.floor(target.getX()), Mth.floor(target.getY() + 0.5D), Mth.floor(target.getZ()));
        boolean[] direction = getFormationDirection(target.getX(), target.getZ(), oldX, oldZ);
        BlockPos position = calcFormationPos(formationId, slot, center, direction);
        BlockPos safePosition = getSafeBlockWithin5x5(target.level(), position);
        return safePosition == null ? target.position() : Vec3.atLowerCornerOf(safePosition);
    }

    @Nullable
    public static BlockPos getSafeBlockWithin5x5(Level level, BlockPos origin) {
        for (int ix = 0, x = 0; ix <= 6; ix++) {
            x += (ix & 1) == 0 ? -ix : ix;
            for (int iz = 0, z = 0; iz <= 6; iz++) {
                z += (iz & 1) == 0 ? -iz : iz;
                for (int iy = 0, y = 0; iy <= 8; iy++) {
                    y += (iy & 1) == 0 ? -iy : iy;
                    BlockPos candidate = origin.offset(x, y, z);
                    BlockState state = level.getBlockState(candidate);
                    if (state.isAir() || !state.getFluidState().isEmpty()
                            || state.is(ModBlocks.BLOCK_WAYPOINT.get())
                            || state.isPathfindable(level, candidate, PathComputationType.LAND)) {
                        for (int below = 0; below <= Math.abs(y); below++) {
                            BlockState floor = level.getBlockState(candidate.below(below + 1));
                            if (!floor.isAir() && !floor.is(Blocks.FIRE)
                                    && (!floor.getFluidState().isEmpty() || floor.blocksMotion())) {
                                return candidate.below(below);
                            }
                        }
                    }
                }
            }
        }
        return null;
    }

    public static void sendFormationMarker(LegacyShipEntity ship, Vec3 position, int marker) {
        Player owner = ship.getOwnerPlayer();
        if (owner instanceof ServerPlayer player && player.level() == ship.level()
                && (ShinColleConfig.alwaysShowTeamParticle()
                || player.getMainHandItem().is(ModItems.POINTERITEM.get())
                || player.getOffhandItem().is(ModItems.POINTERITEM.get()))) {
            ModNetwork.sendToPlayer(player, new ClientboundSpawnParticlePacket(GameplayParticleType.HEALING_SPARKLE,
                    position.x, position.y, position.z, 0.3D, marker, 0.0D));
        }
    }

    public static FormationRuntimeState resolveMoveState(Player player, LegacyShipEntity ship, BlockPos center,
                                                         @Nullable LegacyShipEntity flagship) {
        if (ship.getShipUid() <= 0) {
            return defaultState(player, center, 0);
        }

        return TeitokuHelper.get(player)
                .map(teitokuData -> {
                    int teamId = teitokuData.findTeamIdByShipUid(ship.getShipUid());
                    int slot = teitokuData.findSlotIndexByShipUid(ship.getShipUid());
                    if (teamId < 0 || slot < 0) {
                        return defaultState(player, center, Math.max(slot, 0));
                    }

                    int formationId = teitokuData.getFormationId(teamId);
                    if (formationId <= TeitokuData.DEFAULT_FORMATION_ID || teitokuData.countShipsInTeam(teamId) <= 4) {
                        return new FormationRuntimeState(teamId, TeitokuData.DEFAULT_FORMATION_ID, slot, center.immutable());
                    }

                    LegacyShipEntity anchor = flagship != null ? flagship : ship;
                    BlockPos targetPos = calcFormationPos(formationId, slot, center, anchor.getX(), anchor.getZ());
                    return new FormationRuntimeState(teamId, formationId, slot, targetPos);
                })
                .orElse(defaultState(player, center, 0));
    }

    public static @Nullable LegacyShipEntity resolveFlagship(java.util.List<LegacyShipEntity> ships) {
        for (LegacyShipEntity ship : ships) {
            if (ship != null) {
                return ship;
            }
        }
        return null;
    }

    private static FormationRuntimeState defaultState(Player player, BlockPos center, int slot) {
        return new FormationRuntimeState(TeitokuHelper.getCurrentTeamId(player),
                TeitokuData.DEFAULT_FORMATION_ID,
                slot,
                center.immutable());
    }

    private static BlockPos calcFormationPos(int formationId, int formatPos, BlockPos flagshipPos, double oldX, double oldZ) {
        return calcFormationPos(formationId, formatPos, flagshipPos,
                getFormationDirection(flagshipPos.getX(), flagshipPos.getZ(), oldX, oldZ));
    }

    private static BlockPos calcFormationPos(int formationId, int formatPos, BlockPos flagshipPos, boolean[] face) {
        int normalizedPos = Math.max(0, Math.min(TeitokuData.TEAM_SIZE - 1, formatPos));
        int[] pos = new int[]{flagshipPos.getX(), flagshipPos.getY(), flagshipPos.getZ()};
        if (normalizedPos == 0) {
            return flagshipPos.immutable();
        }

        switch (formationId) {
            case 1 -> {
                for (int i = 0; i < normalizedPos; i++) {
                    pos = nextLineAheadPos(face[0], face[1], pos[0], pos[1], pos[2]);
                }
            }
            case 2 -> pos = nextDoubleLinePos(face[0], face[1], normalizedPos, pos[0], pos[1], pos[2]);
            case 3 -> pos = nextDiamondPos(face[0], face[1], normalizedPos, pos[0], pos[1], pos[2]);
            case 4 -> {
                for (int i = 0; i < normalizedPos; i++) {
                    pos = nextEchelonPos(face[1], pos[0], pos[1], pos[2]);
                }
            }
            case 5 -> pos = nextLineAbreastPos(face[0], normalizedPos, pos[0], pos[1], pos[2]);
            default -> {
                return flagshipPos.immutable();
            }
        }

        return new BlockPos(pos[0], pos[1], pos[2]);
    }

    private static boolean[] getFormationDirection(double toX, double toZ, double fromX, double fromZ) {
        double dx = toX - fromX;
        double dz = toZ - fromZ;
        boolean alongX = Math.abs(dx) > Math.abs(dz);
        boolean facePositive = alongX ? dx >= 0.0D : dz >= 0.0D;
        return new boolean[]{alongX, facePositive};
    }

    private static int[] nextLineAheadPos(boolean alongX, boolean facePositive, int x, int y, int z) {
        if (alongX) {
            x += facePositive ? -3 : 3;
        } else {
            z += facePositive ? -3 : 3;
        }
        return new int[]{x, y, z};
    }

    private static int[] nextDoubleLinePos(boolean alongX, boolean facePositive, int formatPos, int x, int y, int z) {
        switch (formatPos) {
            case 1 -> {
                if (alongX) {
                    z += 3;
                } else {
                    x += 3;
                }
            }
            case 2 -> {
                if (alongX) {
                    x += facePositive ? 3 : -3;
                } else {
                    z += facePositive ? 3 : -3;
                }
            }
            case 3 -> {
                if (alongX) {
                    x += facePositive ? 3 : -3;
                    z += 3;
                } else {
                    x += 3;
                    z += facePositive ? 3 : -3;
                }
            }
            case 4 -> {
                if (alongX) {
                    x += facePositive ? -3 : 3;
                } else {
                    z += facePositive ? -3 : 3;
                }
            }
            case 5 -> {
                if (alongX) {
                    x += facePositive ? -3 : 3;
                    z += 3;
                } else {
                    x += 3;
                    z += facePositive ? -3 : 3;
                }
            }
            default -> {
            }
        }
        return new int[]{x, y, z};
    }

    private static int[] nextDiamondPos(boolean alongX, boolean facePositive, int formatPos, int x, int y, int z) {
        switch (formatPos) {
            case 1 -> {
                if (alongX) {
                    x += facePositive ? 5 : -5;
                } else {
                    z += facePositive ? 5 : -5;
                }
            }
            case 2 -> {
                if (alongX) {
                    x += facePositive ? 1 : -1;
                    z -= 4;
                } else {
                    x -= 4;
                    z += facePositive ? 1 : -1;
                }
            }
            case 3 -> {
                if (alongX) {
                    x += facePositive ? 1 : -1;
                    z += 4;
                } else {
                    x += 4;
                    z += facePositive ? 1 : -1;
                }
            }
            case 4 -> {
                if (alongX) {
                    x += facePositive ? -3 : 3;
                } else {
                    z += facePositive ? -3 : 3;
                }
            }
            case 5 -> {
                if (alongX) {
                    x += facePositive ? 2 : -2;
                } else {
                    z += facePositive ? 2 : -2;
                }
            }
            default -> {
            }
        }
        return new int[]{x, y, z};
    }

    private static int[] nextEchelonPos(boolean facePositive, int x, int y, int z) {
        x += facePositive ? -2 : 2;
        z += facePositive ? -2 : 2;
        return new int[]{x, y, z};
    }

    private static int[] nextLineAbreastPos(boolean alongX, int formatPos, int x, int y, int z) {
        switch (formatPos) {
            case 1 -> {
                if (alongX) {
                    z += 3;
                } else {
                    x += 3;
                }
            }
            case 2 -> {
                if (alongX) {
                    z -= 3;
                } else {
                    x -= 3;
                }
            }
            case 3 -> {
                if (alongX) {
                    z += 6;
                } else {
                    x += 6;
                }
            }
            case 4 -> {
                if (alongX) {
                    z -= 6;
                } else {
                    x -= 6;
                }
            }
            case 5 -> {
                if (alongX) {
                    z += 9;
                } else {
                    x += 9;
                }
            }
            default -> {
            }
        }
        return new int[]{x, y, z};
    }
}
