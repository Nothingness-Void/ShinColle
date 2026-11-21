package com.lulan.shincolle.gametest;

import com.mojang.authlib.GameProfile;
import com.lulan.shincolle.ShinColle;
import com.lulan.shincolle.config.ShinColleConfig;
import com.lulan.shincolle.entity.ship.LegacyShipEntity;
import com.lulan.shincolle.registry.ModEntityTypes;
import com.lulan.shincolle.registry.ModItems;
import com.lulan.shincolle.teitoku.TeitokuHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.ForcedChunksSavedData;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import net.neoforged.neoforge.event.entity.EntityEvent;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@GameTestHolder(ShinColle.MOD_ID)
@PrefixGameTestTemplate(false)
public final class ShipChunkLoaderGameTests {

    @GameTest(template = "gameplayparitygametests.empty")
    public static void compassTicketsRequireOnlineOwnerAndReleaseWithoutEquipment(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ServerPlayer owner = FakePlayerFactory.get(level, new GameProfile(UUID.randomUUID(), "ChunkOwner"));
        TeitokuHelper.get(owner).orElseThrow().setPlayerUid(98201);
        LegacyShipEntity ship = ModEntityTypes.LEGACY_SHIP.get().create(level);
        BlockPos pos = helper.absolutePos(new BlockPos(2, 2, 2));
        ship.setPos(pos.getX(), pos.getY(), pos.getZ());
        ship.setNoAi(true);
        ship.setOwner(owner.getUUID(), "ChunkOwner", 98201);
        int originalMode = ShinColleConfig.COMMON.chunkloaderMode.get();
        try {
            if (originalMode != 2) {
                ShinColleConfig.COMMON.chunkloaderMode.set(2);
            }
            level.addNewPlayer(owner);
            ship.updateChunkLoader();
            helper.assertTrue(forcedChunks(ship).isEmpty(), "no compass means no tickets");
            ship.getShipInventory().setItem(0, new ItemStack(ModItems.EQUIPCOMPASS.get()));
            ship.setOwner(owner.getUUID(), "ChunkOwner", 0);
            ship.updateChunkLoader();
            helper.assertTrue(forcedChunks(ship).isEmpty(), "invalid owner UID cannot acquire tickets");
            ship.setOwner(owner.getUUID(), "ChunkOwner", 98202);
            ship.updateChunkLoader();
            helper.assertTrue(forcedChunks(ship).isEmpty(), "an offline UID cannot acquire tickets");
            ship.setOwner(owner.getUUID(), "ChunkOwner", 98201);
            ship.updateChunkLoader();
            helper.assertTrue(forcedChunks(ship).equals(square(ship.chunkPosition())), "mode 2 forces a ticking 3x3 square");

            level.players().remove(owner);
            ship.updateChunkLoader();
            helper.assertTrue(forcedChunks(ship).size() == 9, "existing tickets survive owner logout as in legacy");

            ship.getShipInventory().setItem(0, ItemStack.EMPTY);
            ship.updateChunkLoader();
            helper.assertTrue(forcedChunks(ship).isEmpty(), "removing compass releases all tickets");
            ship.getShipInventory().setItem(0, new ItemStack(ModItems.EQUIPCOMPASS.get()));
            ship.updateChunkLoader();
            helper.assertTrue(forcedChunks(ship).isEmpty(), "after release an offline owner cannot reacquire tickets");
            level.players().add(owner);
            ship.updateChunkLoader();
            level.addFreshEntity(ship);
            ChunkPos next = new ChunkPos(ship.chunkPosition().x + 1, ship.chunkPosition().z);
            ship.setPos(pos.getX() + 16, pos.getY(), pos.getZ());
            helper.assertTrue(forcedChunks(ship).equals(square(next)), "entering a new chunk updates tickets immediately");
            ship.remove(Entity.RemovalReason.UNLOADED_TO_CHUNK);
            helper.assertTrue(forcedChunks(ship).isEmpty(), "unloading releases tickets");
        } finally {
            ship.clearChunkLoader();
            ship.discard();
            level.removePlayerImmediately(owner, Entity.RemovalReason.DISCARDED);
            if (originalMode != 2) {
                ShinColleConfig.COMMON.chunkloaderMode.set(originalMode);
            }
        }
        helper.succeed();
    }

    @GameTest(template = "gameplayparitygametests.empty")
    public static void wrongChunkLocationClearsRemovalAndDismounts(GameTestHelper helper) {
        LegacyShipEntity ship = ModEntityTypes.LEGACY_SHIP.get().create(helper.getLevel());
        Entity boat = EntityType.BOAT.create(helper.getLevel());
        ship.setPos(-0.5D, 64.0D, -16.5D);
        ship.startRiding(boat, true);
        long correct = SectionPos.asLong(-1, 4, -2);
        NeoForge.EVENT_BUS.post(new EntityEvent.EnteringSection(ship, correct, SectionPos.asLong(-1, 5, -2)));
        helper.assertTrue(ship.isPassenger(), "vertical section changes are not chunk transitions");
        NeoForge.EVENT_BUS.post(new EntityEvent.EnteringSection(ship, SectionPos.asLong(0, 4, -2), correct));
        helper.assertTrue(ship.isPassenger(), "correct negative chunk coordinates do not dismount");
        NeoForge.EVENT_BUS.post(new EntityEvent.EnteringSection(ship, correct, SectionPos.asLong(0, 4, -2)));
        helper.assertTrue(!ship.isPassenger(), "wrong location dismounts the ship");
        ship.remove(Entity.RemovalReason.DISCARDED);
        NeoForge.EVENT_BUS.post(new EntityEvent.EnteringSection(ship, correct, SectionPos.asLong(0, 4, -2)));
        helper.assertTrue(!ship.isRemoved(), "wrong location clears the removed flag");
        ship.discard();
        boat.discard();
        helper.succeed();
    }

    private static Set<Long> square(ChunkPos center) {
        Set<Long> chunks = new HashSet<>();
        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                chunks.add(ChunkPos.asLong(center.x + x, center.z + z));
            }
        }
        return chunks;
    }

    private static Set<Long> forcedChunks(LegacyShipEntity ship) {
        ForcedChunksSavedData data = ((ServerLevel) ship.level()).getDataStorage()
                .computeIfAbsent(ForcedChunksSavedData::load, ForcedChunksSavedData::new, "chunks");
        ListTag mods = data.save(new CompoundTag()).getList("ForgeForced", Tag.TAG_COMPOUND);
        Set<Long> result = new HashSet<>();
        for (int i = 0; i < mods.size(); i++) {
            CompoundTag mod = mods.getCompound(i);
            if (!ShinColle.MOD_ID.equals(mod.getString("Mod"))) {
                continue;
            }
            ListTag chunks = mod.getList("ModForced", Tag.TAG_COMPOUND);
            for (int j = 0; j < chunks.size(); j++) {
                CompoundTag chunk = chunks.getCompound(j);
                for (Tag owner : chunk.getList("TickingEntities", Tag.TAG_INT_ARRAY)) {
                    if (ship.getUUID().equals(NbtUtils.loadUUID(owner))) {
                        result.add(chunk.getLong("Chunk"));
                    }
                }
            }
        }
        return result;
    }
}
