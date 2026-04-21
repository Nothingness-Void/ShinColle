package com.lulan.shincolle.formation;

import net.minecraft.core.BlockPos;

/**
 * Runtime formation slot resolution used by gameplay command handlers.
 */
public record FormationRuntimeState(int teamId, int formationId, int slotIndex, BlockPos targetPos) {
}

