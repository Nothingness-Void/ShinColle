package com.lulan.shincolle.world;

import com.lulan.shincolle.ShinColle;
import com.lulan.shincolle.config.ShinColleConfig;
import com.lulan.shincolle.entity.ship.LegacyShipEntity;
import com.lulan.shincolle.teitoku.TeitokuHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;
import net.minecraftforge.common.world.ForgeChunkManager;
import net.minecraftforge.event.entity.EntityEvent;
import net.minecraftforge.event.entity.EntityLeaveLevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.HashSet;
import java.util.Set;

@Mod.EventBusSubscriber(modid = ShinColle.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class ShipChunkLoader {

    private final Set<ChunkPos> chunks = new HashSet<>();
    private ServerLevel ticketLevel;
    private boolean initialized;

    public static void register() {
        ForgeChunkManager.setForcedChunkLoadingCallback(ShinColle.MOD_ID, (level, tickets) -> {
            tickets.getEntityTickets().keySet().forEach(tickets::removeAllTickets);
            tickets.getBlockTickets().keySet().forEach(tickets::removeAllTickets);
        });
    }

    @SubscribeEvent
    public static void onEnteringSection(EntityEvent.EnteringSection event) {
        if (event.didChunkChange() && event.getEntity() instanceof LegacyShipEntity ship) {
            ship.onEnteringChunk(event.getNewPos().x(), event.getNewPos().z());
        }
    }

    @SubscribeEvent
    public static void onLeaveLevel(EntityLeaveLevelEvent event) {
        if (event.getEntity() instanceof LegacyShipEntity ship) {
            ship.clearChunkLoader();
        }
    }

    public void tick(LegacyShipEntity ship) {
        if (!this.initialized && ship.tickCount > 10 && (ship.tickCount & 7) == 0) {
            this.initialized = true;
            this.update(ship);
        } else if ((ship.tickCount & 127) == 0) {
            this.update(ship);
        }
    }

    public void update(LegacyShipEntity ship) {
        if (!(ship.level() instanceof ServerLevel level)) {
            return;
        }
        if (this.ticketLevel != null && this.ticketLevel != level) {
            this.clear(ship);
        }
        if (!ship.getEquipmentBehaviorState().autonomousRoute()) {
            this.clear(ship);
            return;
        }
        if (this.ticketLevel == null) {
            if (!ownerOnline(ship, level)) {
                return;
            }
            this.ticketLevel = level;
        }

        int mode = ShinColleConfig.COMMON.chunkloaderMode.get();
        Set<ChunkPos> nextChunks = new HashSet<>();
        ChunkPos center = ship.chunkPosition();
        if (mode > 0) {
            int radius = mode == 2 ? 1 : 0;
            for (int x = -radius; x <= radius; x++) {
                for (int z = -radius; z <= radius; z++) {
                    nextChunks.add(new ChunkPos(center.x + x, center.z + z));
                }
            }
        }
        for (ChunkPos chunk : Set.copyOf(this.chunks)) {
            if (!nextChunks.contains(chunk)) {
                ForgeChunkManager.forceChunk(level, ShinColle.MOD_ID, ship.getUUID(), chunk.x, chunk.z, false, true);
                this.chunks.remove(chunk);
            }
        }
        for (ChunkPos chunk : nextChunks) {
            if (!this.chunks.contains(chunk)
                    && ForgeChunkManager.forceChunk(level, ShinColle.MOD_ID, ship.getUUID(), chunk.x, chunk.z, true, true)) {
                this.chunks.add(chunk);
            }
        }
    }

    public void clear(LegacyShipEntity ship) {
        if (this.ticketLevel != null) {
            for (ChunkPos chunk : this.chunks) {
                ForgeChunkManager.forceChunk(this.ticketLevel, ShinColle.MOD_ID, ship.getUUID(), chunk.x, chunk.z, false, true);
            }
        }
        this.chunks.clear();
        this.ticketLevel = null;
    }

    private static boolean ownerOnline(LegacyShipEntity ship, ServerLevel level) {
        if (ship.getOwnerUid() > 0) {
            for (ServerLevel world : level.getServer().getAllLevels()) {
                for (ServerPlayer player : world.players()) {
                    if (TeitokuHelper.getPlayerUid(player) == ship.getOwnerUid()) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
}
