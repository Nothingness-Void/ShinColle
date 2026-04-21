package com.lulan.shincolle.world;

import com.lulan.shincolle.ShinColle;
import com.lulan.shincolle.entity.ship.LegacyShipEntity;
import com.lulan.shincolle.entity.ship.ShipEntitySpec;
import com.lulan.shincolle.registry.ModEntityTypes;
import com.lulan.shincolle.teitoku.TeitokuData;
import com.lulan.shincolle.teitoku.TeitokuHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BiomeTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import javax.annotation.Nullable;

@Mod.EventBusSubscriber(modid = ShinColle.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class HostileEncounterSpawner {

    private static final int MOB_SPAWN_CHECK_MASK = 127;
    private static final int LEGACY_MOB_LIMIT = 50;
    private static final int LEGACY_MOB_ROLL = 10;
    private static final int LEGACY_MOB_GROUPS = 1;
    private static final int LEGACY_BOSS_COOLDOWN = 4800;
    private static final int LEGACY_BOSS_GROUPS = 2;
    private static final int LEGACY_ESCORT_GROUPS = 4;
    private static final int LEGACY_BOSS_NEARBY_LIMIT = 2;

    private HostileEncounterSpawner() {
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.player.level().isClientSide()) {
            return;
        }
        if (!(event.player instanceof ServerPlayer player)) {
            return;
        }

        ServerLevel level = player.serverLevel();
        if (level.getDifficulty() == Difficulty.PEACEFUL || player.isSpectator() || !player.isAlive()) {
            return;
        }

        TeitokuData teitokuData = TeitokuHelper.get(player).resolve().orElse(null);
        if ((player.tickCount & MOB_SPAWN_CHECK_MASK) == 0) {
            attemptMobEncounterSpawn(player, level, teitokuData);
        }
        attemptBossEncounterSpawn(player, level, teitokuData);
    }

    private static void attemptMobEncounterSpawn(ServerPlayer player, ServerLevel level, @Nullable TeitokuData teitokuData) {
        if (!canSpawnMobForPlayer(player, level, teitokuData)) {
            return;
        }
        if (countLoadedNonBossHostiles(level) > LEGACY_MOB_LIMIT) {
            return;
        }
        if (level.getRandom().nextInt(100) > LEGACY_MOB_ROLL) {
            return;
        }

        int groupsRemaining = LEGACY_MOB_GROUPS;
        int attemptsRemaining = 30 + groupsRemaining * 30;
        while (groupsRemaining > 0 && attemptsRemaining-- > 0) {
            BlockPos anchor = findMobSpawnAnchor(level, player);
            if (anchor == null) {
                continue;
            }

            HostileSpawnProfile profile = HostileEncounterTable.commonProfile(level.getRandom());
            LegacyShipEntity spawned = spawnEncounterAt(level, anchor, player, profile);
            if (spawned != null) {
                groupsRemaining--;
            }
        }
    }

    public static boolean canRollBossEncounter(@Nullable TeitokuData teitokuData) {
        return teitokuData != null && teitokuData.hasRing();
    }

    private static void attemptBossEncounterSpawn(ServerPlayer player, ServerLevel level, @Nullable TeitokuData teitokuData) {
        if (!canSpawnBossForPlayer(player, level, teitokuData) || teitokuData == null) {
            return;
        }

        teitokuData.setBossCooldown(LEGACY_BOSS_COOLDOWN);
        if (level.getRandom().nextInt(4) != 0) {
            TeitokuHelper.syncGameplayState(player);
            return;
        }

        for (int attempt = 0; attempt < 20; attempt++) {
            BlockPos anchor = findBossSpawnAnchor(level, player);
            if (anchor == null) {
                continue;
            }
            if (countNearbyBosses(level, anchor) >= LEGACY_BOSS_NEARBY_LIMIT) {
                continue;
            }

            boolean spawnedBoss = false;
            for (int i = 0; i < LEGACY_BOSS_GROUPS; i++) {
                spawnedBoss |= spawnEncounterAt(level, anchor, player, HostileEncounterTable.bossProfile(level.getRandom())) != null;
            }
            for (int i = 0; i < LEGACY_ESCORT_GROUPS; i++) {
                spawnEncounterAt(level, anchor, player, HostileEncounterTable.commonProfile(level.getRandom()));
            }

            if (spawnedBoss) {
                broadcastBossSpawn(level, anchor);
            }
            TeitokuHelper.syncGameplayState(player);
            return;
        }

        TeitokuHelper.syncGameplayState(player);
    }

    public static @Nullable LegacyShipEntity spawnEncounterAt(ServerLevel level,
                                                              BlockPos anchor,
                                                              @Nullable ServerPlayer targetPlayer,
                                                              HostileSpawnProfile profile) {
        RandomSource random = level.getRandom();
        ShipEntitySpec spec = com.lulan.shincolle.entity.ship.ShipEntitySpecs.getByEggMeta(profile.eggMeta());

        BlockPos spawnPos = anchor.offset(random.nextInt(3), 0, random.nextInt(3));
        LegacyShipEntity ship = new LegacyShipEntity(ModEntityTypes.LEGACY_SHIP.get(), level);
        ship.moveTo(spawnPos.getX() + 0.5D, spawnPos.getY() + 0.5D, spawnPos.getZ() + 0.5D, random.nextFloat() * 360.0F, 0.0F);
        ship.finalizeSpawn(level, level.getCurrentDifficultyAt(spawnPos), MobSpawnType.NATURAL, null, null);
        ship.applySpawnSpec(spec, null);
        ship.initializeHostileRuntime(true, profile.elite(), profile.boss());
        ship.setTarget(targetPlayer);
        if (!level.noCollision(ship, ship.getBoundingBox())) {
            ship.discard();
            return null;
        }
        level.addFreshEntity(ship);
        return ship;
    }

    private static boolean canSpawnMobForPlayer(ServerPlayer player, ServerLevel level, @Nullable TeitokuData teitokuData) {
        return isLegacyOceanBiome(level, player.blockPosition()) && canRollBossEncounter(teitokuData);
    }

    private static boolean canSpawnBossForPlayer(ServerPlayer player, ServerLevel level, @Nullable TeitokuData teitokuData) {
        return isLegacyOceanBiome(level, player.blockPosition())
                && canRollBossEncounter(teitokuData)
                && teitokuData != null
                && teitokuData.getBossCooldown() <= 0;
    }

    private static boolean isLegacyOceanBiome(ServerLevel level, BlockPos pos) {
        Holder<Biome> biome = level.getBiome(pos);
        return biome.is(BiomeTags.IS_OCEAN) || biome.is(BiomeTags.IS_DEEP_OCEAN) || biome.is(BiomeTags.IS_BEACH);
    }

    private static int countLoadedNonBossHostiles(ServerLevel level) {
        int count = 0;
        for (Entity entity : level.getAllEntities()) {
            if (entity instanceof LegacyShipEntity ship && ship.isHostileVariant() && !ship.isBossEncounter()) {
                count++;
            }
        }
        return count;
    }

    private static int countNearbyBosses(ServerLevel level, BlockPos anchor) {
        return level.getEntitiesOfClass(
                LegacyShipEntity.class,
                new AABB(anchor).inflate(48.0D),
                ship -> ship.isHostileVariant() && ship.isBossEncounter()).size();
    }

    private static void broadcastBossSpawn(ServerLevel level, BlockPos anchor) {
        String key = level.getRandom().nextInt(2) == 0 ? "chat.shincolle.bossspawn1" : "chat.shincolle.bossspawn2";
        Component message = Component.translatable(key)
                .append(Component.literal(" " + anchor.getX() + " " + anchor.getY() + " " + anchor.getZ()));
        if (level.getServer() != null) {
            level.getServer().getPlayerList().broadcastSystemMessage(message, false);
        }
    }

    private static @Nullable BlockPos findMobSpawnAnchor(ServerLevel level, ServerPlayer player) {
        return findSpawnAnchor(level, player, 20, 30, 60);
    }

    private static @Nullable BlockPos findBossSpawnAnchor(ServerLevel level, ServerPlayer player) {
        return findSpawnAnchor(level, player, 32, 32, 20);
    }

    private static @Nullable BlockPos findSpawnAnchor(ServerLevel level, ServerPlayer player, int minRadius, int spread, int attempts) {
        RandomSource random = level.getRandom();
        for (int attempt = 0; attempt < attempts; attempt++) {
            int offX = random.nextInt(spread) + minRadius;
            int offZ = random.nextInt(spread) + minRadius;
            int dx;
            int dz;
            switch (random.nextInt(4)) {
                case 1 -> {
                    dx = -offX;
                    dz = -offZ;
                }
                case 2 -> {
                    dx = offX;
                    dz = -offZ;
                }
                case 3 -> {
                    dx = -offX;
                    dz = offZ;
                }
                default -> {
                    dx = offX;
                    dz = offZ;
                }
            }

            int x = player.blockPosition().getX() + dx;
            int z = player.blockPosition().getZ() + dz;
            int y = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);
            BlockPos top = new BlockPos(x, y, z);
            if (level.getFluidState(top.below()).isSource()) {
                return top.below().immutable();
            }
            if (level.getFluidState(top).isSource()) {
                return top.immutable();
            }
        }

        return null;
    }
}
