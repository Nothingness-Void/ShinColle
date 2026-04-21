package com.lulan.shincolle.formation;

import com.lulan.shincolle.entity.ship.LegacyShipEntity;
import com.lulan.shincolle.teitoku.TeitokuData;
import com.lulan.shincolle.teitoku.TeitokuHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;

import javax.annotation.Nullable;

public final class LegacyFormationHelper {

    private LegacyFormationHelper() {
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
        int normalizedPos = Math.max(0, Math.min(TeitokuData.TEAM_SIZE - 1, formatPos));
        int[] pos = new int[]{flagshipPos.getX(), flagshipPos.getY(), flagshipPos.getZ()};
        if (normalizedPos == 0) {
            return flagshipPos.immutable();
        }

        boolean[] face = getFormationDirection(flagshipPos.getX(), flagshipPos.getZ(), oldX, oldZ);
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
