package com.lulan.shincolle.world;

import com.lulan.shincolle.ShinColle;
import com.lulan.shincolle.entity.ship.LegacyShipEntity;
import com.lulan.shincolle.entity.ship.ShipEntitySpec;
import com.lulan.shincolle.entity.ship.ShipEntitySpecs;
import com.lulan.shincolle.teitoku.TeitokuHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BiomeTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import javax.annotation.Nullable;
import java.util.List;

@Mod.EventBusSubscriber(modid = ShinColle.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class HostileEncounterSpawner {

    private static final int SPAWN_CHECK_INTERVAL = 200;
    private static final int MAX_HOSTILES_PER_PLAYER = 8;
    private static final int MAX_BOSSES_PER_PLAYER = 1;

    private HostileEncounterSpawner() {
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.player.level().isClientSide()) {
            return;
        }
        if (!(event.player instanceof ServerPlayer player) || player.tickCount % SPAWN_CHECK_INTERVAL != 0) {
            return;
        }

        ServerLevel level = player.serverLevel();
        if (!canSpawnForPlayer(player, level)) {
            return;
        }

        attemptEncounterSpawn(player, level);
    }

    private static boolean canSpawnForPlayer(ServerPlayer player, ServerLevel level) {
        if (level.getDifficulty() == Difficulty.PEACEFUL || player.isSpectator() || !player.isAlive()) {
            return false;
        }
        if (!level.isNight() && level.getRandom().nextFloat() > 0.12F) {
            return false;
        }

        Holder<Biome> biome = level.getBiome(player.blockPosition());
        return biome.is(BiomeTags.IS_OCEAN) || biome.is(BiomeTags.IS_DEEP_OCEAN);
    }

    private static void attemptEncounterSpawn(ServerPlayer player, ServerLevel level) {
        List<LegacyShipEntity> nearby = level.getEntitiesOfClass(
                LegacyShipEntity.class,
                player.getBoundingBox().inflate(96.0D, 24.0D, 96.0D),
                ship -> ship.isHostileVariant());
        if (nearby.size() >= MAX_HOSTILES_PER_PLAYER) {
            return;
        }

        boolean bossNearby = nearby.stream().anyMatch(LegacyShipEntity::isBossEncounter);
        int bossCooldown = TeitokuHelper.get(player).map(data -> data.getBossCooldown()).orElse(0);
        boolean deepOcean = level.getBiome(player.blockPosition()).is(BiomeTags.IS_DEEP_OCEAN);
        HostileSpawnProfile profile = HostileEncounterTable.pick(level.getRandom(), level.getDifficulty(), deepOcean, !bossNearby && bossCooldown <= 0);
        if (profile.boss() && bossNearby) {
            return;
        }
        if (profile.boss() && bossCooldown > 0) {
            return;
        }

        BlockPos spawnCenter = findSpawnAnchor(level, player);
        if (spawnCenter == null) {
            return;
        }

        spawnEncounterGroup(level, spawnCenter, player, profile);
        if (profile.boss()) {
            TeitokuHelper.get(player).ifPresent(data -> data.setBossCooldown(20 * 60 * 8));
            TeitokuHelper.syncGameplayState(player);
        }
    }

    private static void spawnEncounterGroup(ServerLevel level, BlockPos anchor, ServerPlayer targetPlayer, HostileSpawnProfile profile) {
        RandomSource random = level.getRandom();
        ShipEntitySpec spec = ShipEntitySpecs.getByEggMeta(profile.eggMeta());
        int groupCount = profile.minGroup() >= profile.maxGroup()
                ? profile.minGroup()
                : random.nextInt(profile.maxGroup() - profile.minGroup() + 1) + profile.minGroup();
        if (profile.boss()) {
            groupCount = 1;
        }

        for (int index = 0; index < groupCount; index++) {
            BlockPos spawnPos = anchor.offset(random.nextInt(9) - 4, 0, random.nextInt(9) - 4);
            LegacyShipEntity ship = new LegacyShipEntity(com.lulan.shincolle.registry.ModEntityTypes.LEGACY_SHIP.get(), level);
            ship.moveTo(spawnPos.getX() + 0.5D, spawnPos.getY(), spawnPos.getZ() + 0.5D, random.nextFloat() * 360.0F, 0.0F);
            ship.finalizeSpawn(level, level.getCurrentDifficultyAt(spawnPos), MobSpawnType.NATURAL, null, null);
            ship.applySpawnSpec(spec, null);
            ship.initializeHostileRuntime(true, profile.elite(), profile.boss());
            ship.setTarget(targetPlayer);
            if (!level.noCollision(ship, ship.getBoundingBox())) {
                ship.discard();
                continue;
            }
            level.addFreshEntity(ship);
        }
    }

    private static @Nullable BlockPos findSpawnAnchor(ServerLevel level, ServerPlayer player) {
        RandomSource random = level.getRandom();
        for (int attempt = 0; attempt < 12; attempt++) {
            int radius = 28 + random.nextInt(32);
            int dx = random.nextBoolean() ? radius : -radius;
            int dz = random.nextInt(radius * 2 + 1) - radius;
            if (random.nextBoolean()) {
                int swap = dx;
                dx = dz;
                dz = swap;
            }

            int x = player.blockPosition().getX() + dx;
            int z = player.blockPosition().getZ() + dz;
            int y = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);
            BlockPos top = new BlockPos(x, y, z);
            if (!level.getFluidState(top.below()).isEmpty()) {
                return top.below().immutable();
            }
            if (!level.getFluidState(top).isEmpty()) {
                return top.immutable();
            }
        }

        return null;
    }
}
