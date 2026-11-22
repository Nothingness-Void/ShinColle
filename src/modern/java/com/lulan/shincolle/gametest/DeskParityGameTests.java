package com.lulan.shincolle.gametest;

import com.lulan.shincolle.blockentity.DeskBlockEntity;
import com.lulan.shincolle.entity.ship.ShipEntitySpecs;
import com.lulan.shincolle.menu.DeskMenu;
import com.lulan.shincolle.menu.LegacyDeskBookData;
import com.lulan.shincolle.menu.LegacyDeskState;
import com.lulan.shincolle.menu.LegacyShipInventoryCatalog;
import com.lulan.shincolle.registry.ModBlocks;
import com.lulan.shincolle.registry.ModItems;
import com.lulan.shincolle.teitoku.TeitokuData;
import io.netty.buffer.Unpooled;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

import java.util.Arrays;
import java.util.stream.Stream;

public final class DeskParityGameTests {
    private DeskParityGameTests() {}

    public static void check(GameTestHelper helper, ServerPlayer player, DeskBlockEntity desk) {
        LegacyDeskState state = new LegacyDeskState();
        for (int tab = 0; tab < 4; tab++) {
            state.selectFunction(tab);
            helper.assertTrue(state.function == tab + 1, "desk tabs select their legacy function in-place");
            state.selectFunction(tab);
            helper.assertTrue(state.function == 0, "the active tab closes only its page");
        }
        state.selectFunction(0); state.selectFunction(3);
        helper.assertTrue(state.function == 4, "a different tab replaces the active page");
        state.selectFunction(-1);
        helper.assertTrue(state.function == 4 && LegacyDeskState.buttonAt(0, 3, 2) == 0
                && LegacyDeskState.buttonAt(0, 76, 18) == 3
                && LegacyDeskState.buttonAt(0, 20, 20) == -1
                && LegacyDeskState.buttonAt(-1, 0, 0) == -1, "legacy tab boundaries and empty/invalid hits");
        helper.assertTrue(Arrays.equals(LegacyDeskBookData.PAGE_LIMIT, new int[] {1,28,6,20,26,19,4}),
                "book has seven zero-indexed chapters with the original maximum pages");
        for (int chapter = 0; chapter < 7; chapter++) {
            state.bookClick(chapter + 2, 0);
            helper.assertTrue(state.chapter == chapter && state.page == 0, "chapters start at page zero");
            state.bookClick(1, 0);
            helper.assertTrue(state.page == 1, "left click advances one page");
            for (int repeat = 0; repeat < 4; repeat++) state.bookClick(1, 1);
            helper.assertTrue(state.page == LegacyDeskBookData.PAGE_LIMIT[chapter]
                    && state.chapter == chapter, "right click advances ten and clamps within the chapter");
            for (int repeat = 0; repeat < 4; repeat++) state.bookClick(0, 2);
            helper.assertTrue(state.page == 0 && state.chapter == chapter, "non-left click retreats ten and clamps at zero");
        }
        state.bookClick(9, 0);
        helper.assertTrue(state.chapter == 6 && state.page == 0, "invalid book buttons leave the page unchanged");
        helper.assertTrue(LegacyDeskState.buttonAt(2, 243, 34) == 2
                && LegacyDeskState.buttonAt(2, 256, 121) == 8
                && LegacyDeskState.buttonAt(1, 140, 23) == 1
                && LegacyDeskState.buttonAt(1, 252, 187) == 5
                && LegacyDeskState.buttonAt(3, 7, 61) == 9
                && LegacyDeskState.buttonAt(4, 252, 183) == 13, "old chapter, radar, team and target hit rectangles");
        helper.assertTrue(!LegacyDeskState.opensRadarInventory(-1, 0, 2)
                && LegacyDeskState.opensRadarInventory(0, 0, 2)
                && !LegacyDeskState.opensRadarInventory(0, 1, 2)
                && !LegacyDeskState.opensRadarInventory(-1, -1, 0)
                && !LegacyDeskState.opensRadarInventory(2, 2, 2),
                "radar rows open only a repeated valid selection, with no elapsed-time requirement");
        helper.assertTrue(LegacyDeskState.buttonAt(1, 69, 88) == -1
                && LegacyDeskState.buttonAt(1, 7, 24) == -1,
                "the legacy radar map has no click action; only list rows select/open ships");

        helper.assertTrue(LegacyDeskBookData.BookList.containsKey(1000)
                && LegacyDeskBookData.BookList.containsKey(1028)
                && LegacyDeskBookData.BookList.get(-1) == null
                && LegacyDeskBookData.icon(-1).isEmpty(), "original book content and empty/invalid content cases");
        for (var entries : LegacyDeskBookData.BookList.values()) for (int[] entry : entries) {
            if (entry[0] == 2) helper.assertTrue(!LegacyDeskBookData.icon(entry[4]).isEmpty(),
                    "every original book icon must resolve to a modern item: " + entry[4]);
        }
        Stream.concat(LegacyDeskBookData.ShipBookList.stream(), LegacyDeskBookData.EnemyBookList.stream()).forEach(id -> {
            var spec = ShipEntitySpecs.findByLegacyClassId(id);
            helper.assertTrue(spec != null && LegacyShipInventoryCatalog.forShip(spec) != null,
                    "each original ship-book entry has a renderable ship spec: " + id);
        });
        TeitokuData collected = new TeitokuData();
        helper.assertTrue(collected.getCollectedShips().isEmpty(), "an empty book has no unlocked ship models");
        collected.addCollectedShip(0); collected.addCollectedShip(0); collected.addCollectedShip(-1);
        CompoundTag saved = collected.saveToTag(new CompoundTag());
        collected = new TeitokuData(); collected.loadFromTag(saved);
        helper.assertTrue(collected.getCollectedShips().equals(java.util.List.of(0)),
                "the first legacy model (DDI class zero) can be collected and survives save/load");

        for (int type = 0; type <= 4; type++) {
            FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());
            try {
                DeskMenu.writeOpeningData(buffer, desk.getBlockPos(), type);
                DeskMenu opened = DeskMenu.fromNetwork(1, player.getInventory(), buffer);
                helper.assertTrue(opened.getDeskType() == type && opened.getDeskPos().equals(desk.getBlockPos()),
                        "desk and single-page modes use the same menu opening protocol");
            } finally { buffer.release(); }
        }
        CompoundTag original = desk.getUpdateTag();
        ItemStack held = player.getMainHandItem();
        boolean sneaking = player.isShiftKeyDown();
        int cooldown = com.lulan.shincolle.teitoku.TeitokuHelper.getTeamCooldown(player);
        try {
            for (int chapter = 0; chapter < 7; chapter++) {
                desk.syncFunc(2, 2, chapter, Integer.MAX_VALUE);
                DeskBlockEntity restored = new DeskBlockEntity(desk.getBlockPos(), desk.getBlockState());
                restored.load(desk.getUpdateTag());
                helper.assertTrue(restored.getBookChapter() == chapter
                        && restored.getBookPage() == LegacyDeskBookData.PAGE_LIMIT[chapter],
                        "all chapter bounds survive block-entity synchronization");
            }
            int function = desk.getSelectedFunction();
            player.setShiftKeyDown(true);
            var pos = desk.getBlockPos();
            BlockHitResult hit = new BlockHitResult(Vec3.atCenterOf(pos), Direction.UP, pos, false);
            helper.assertTrue(ModBlocks.BLOCK_DESK.get().use(desk.getBlockState(), player.level(), pos, player,
                    InteractionHand.MAIN_HAND, hit) == InteractionResult.PASS && desk.getSelectedFunction() == function,
                    "sneaking neither opens a terminal nor cycles a desk mode");
            player.setShiftKeyDown(false);
            ModBlocks.BLOCK_DESK.get().use(desk.getBlockState(), player.level(), pos, player, InteractionHand.MAIN_HAND, hit);
            helper.assertTrue(player.containerMenu instanceof DeskMenu opened && opened.getDeskType() == 0,
                    "ordinary desk use opens the unified four-tab mode");
            player.containerMenu.setData(0, 7);
            helper.assertTrue(((DeskMenu)player.containerMenu).getTeamCooldown() == 140,
                    "legacy desk cooldown synchronization converts whole seconds back to ticks");
            player.containerMenu.setData(0, 0);
            helper.assertTrue(((DeskMenu)player.containerMenu).getTeamCooldown() == 0,
                    "a completed team cooldown synchronizes as zero");
            player.closeContainer();
            player.setShiftKeyDown(true);
            player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(ModItems.DESKITEMRADAR.get()));
            ModItems.DESKITEMRADAR.get().use(player.level(), player, InteractionHand.MAIN_HAND);
            helper.assertTrue(player.containerMenu instanceof DeskMenu opened && opened.getDeskType() == 1,
                    "the radar item opens its legacy single-page mode, including while sneaking");
            player.closeContainer();
            player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(ModItems.DESKITEMBOOK.get()));
            ModItems.DESKITEMBOOK.get().use(player.level(), player, InteractionHand.MAIN_HAND);
            helper.assertTrue(player.containerMenu instanceof DeskMenu opened && opened.getDeskType() == 2,
                    "the book item opens its legacy single-page mode");
        } finally {
            player.closeContainer(); player.setItemInHand(InteractionHand.MAIN_HAND, held);
            player.setShiftKeyDown(sneaking); desk.load(original);
            com.lulan.shincolle.teitoku.TeitokuHelper.get(player).ifPresent(data -> data.setTeamCooldown(cooldown));
        }
    }
}
