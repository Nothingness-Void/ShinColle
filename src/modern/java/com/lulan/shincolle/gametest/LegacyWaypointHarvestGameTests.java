package com.lulan.shincolle.gametest;

import com.lulan.shincolle.blockentity.WaypointBlockEntity;
import com.lulan.shincolle.registry.ModBlocks;
import com.mojang.authlib.GameProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.util.UUID;

@GameTestHolder("shincolle")
@PrefixGameTestTemplate(false)
public final class LegacyWaypointHarvestGameTests {
    private LegacyWaypointHarvestGameTests() {
    }

    @GameTest(template = "gameplayparitygametests.empty")
    public static void waypointHarvestReturnsToOwnerInventoryWithFullInventoryFallback(GameTestHelper helper) {
        var player = FakePlayerFactory.get(helper.getLevel(), new GameProfile(UUID.randomUUID(), "WaypointHarvest"));
        var intruder = FakePlayerFactory.get(helper.getLevel(), new GameProfile(UUID.randomUUID(), "WaypointIntruder"));
        var block = ModBlocks.BLOCK_WAYPOINT.get();
        BlockPos pos = helper.absolutePos(new BlockPos(3, 2, 3));
        player.setPos(pos.getX() + 0.5D, pos.getY(), pos.getZ() + 0.5D);
        for (int scenario = 0; scenario < 3; scenario++) {
            player.setGameMode(scenario == 2 ? GameType.CREATIVE : GameType.SURVIVAL);
            intruder.setGameMode(GameType.SURVIVAL);
            player.getInventory().clearContent();
            if (scenario == 1) {
                for (int slot = 0; slot < player.getInventory().items.size(); slot++) {
                    player.getInventory().setItem(slot, new ItemStack(Items.STONE, 64));
                }
            }
            helper.getLevel().setBlockAndUpdate(pos, block.defaultBlockState());
            var tile = (WaypointBlockEntity) helper.getLevel().getBlockEntity(pos);
            tile.setOwner(player);
            var state = helper.getLevel().getBlockState(pos);
            helper.assertTrue(!block.onDestroyedByPlayer(state, helper.getLevel(), pos, intruder, true, state.getFluidState())
                            && helper.getLevel().getBlockState(pos).is(block),
                    "unauthorized survival player must neither remove nor receive the waypoint");
            helper.assertTrue(block.onDestroyedByPlayer(state, helper.getLevel(), pos, player, true, state.getFluidState()),
                    "owner/creative waypoint removal must succeed");
            block.playerDestroy(helper.getLevel(), player, pos, state, tile, ItemStack.EMPTY);
            var drops = helper.getLevel().getEntitiesOfClass(ItemEntity.class,
                    player.getBoundingBox().inflate(3), item -> item.getItem().is(block.asItem()));
            int inventoryCount = player.getInventory().countItem(block.asItem());
            helper.assertTrue(inventoryCount == (scenario == 1 ? 0 : 1)
                            && drops.size() == (scenario == 1 ? 1 : 0),
                    "legacy removal returns one item even in creative, dropping only if inventory is full; no vanilla duplicate");
            drops.forEach(ItemEntity::discard);
        }
        player.getInventory().clearContent();
        helper.succeed();
    }
}
