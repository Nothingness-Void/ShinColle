package com.lulan.shincolle.gametest;

import com.lulan.shincolle.entity.ship.LegacyShipEntity;
import com.lulan.shincolle.entity.ship.LegacyShipStatTables;
import com.lulan.shincolle.entity.ship.ShipEntitySpec;
import com.lulan.shincolle.entity.ship.ShipEntitySpecs;
import com.lulan.shincolle.menu.LegacyShipInventoryCatalog;
import com.lulan.shincolle.menu.LegacyShipInventoryControls;
import com.lulan.shincolle.menu.ShipInventoryMenu;
import com.lulan.shincolle.menu.ShipInventoryPresentation;
import com.lulan.shincolle.network.ServerboundShipCommandPacket;
import com.lulan.shincolle.network.ShipCommandService;
import com.lulan.shincolle.network.ShipGuiButton;
import com.lulan.shincolle.registry.ModItems;
import io.netty.buffer.Unpooled;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.ArrayList;
import java.util.List;

public final class ShipInventoryParityGameTests {
    private ShipInventoryParityGameTests() {}

    public static void check(GameTestHelper helper, Player player, LegacyShipEntity ship) {
        CompoundTag original = new CompoundTag();
        ship.saveWithoutId(original);
        try {
            for (ShipEntitySpec spec : ShipEntitySpecs.values()) {
                if (spec.hostile()) continue;
                var metadata = LegacyShipInventoryCatalog.forShip(spec);
                helper.assertTrue(metadata != null && metadata.modelPosition().length == 4
                        && metadata.modelButtons() >= 0 && metadata.modelButtons() <= 16
                        && LegacyShipInventoryCatalog.typeIcon(metadata.shipType()) != null
                        && LegacyShipInventoryCatalog.nameIcon(spec.legacyClassId()) != null,
                        "every friendly legacy inventory needs its original icons, model position and switch count: " + spec);
            }

            ShipInventoryPresentation view = new ShipInventoryPresentation();
            view.apply(ShipInventoryPresentation.capture(null));
            helper.assertTrue(view.getHealth() == 0 && view.getInventoryPageSize() == 0
                    && view.formationTooltipValues().isEmpty(), "empty inventory presentation must not invent formation data");
            view.apply(ShipInventoryPresentation.capture(ship));
            List<int[]> packets = new ArrayList<>();
            LegacyShipInventoryControls controls = new LegacyShipInventoryControls(view,
                    (button, value) -> packets.add(new int[] {button, value}));
            helper.assertTrue(controls.showPage == 1 && controls.showPageAI == 1
                    && controls.showPageInv == 0 && controls.mousePressBar == -1,
                    "inventory must start on the original first detail/control/cargo pages");
            helper.assertTrue(LegacyShipInventoryControls.buttonAt(-1, 0, 0) == -1
                    && LegacyShipInventoryControls.buttonAt(0, 60, 18) == -1
                    && LegacyShipInventoryControls.buttonAt(0, 61, 18) == 15
                    && LegacyShipInventoryControls.buttonAt(0, 70, 52) == 15,
                    "legacy inventory hitboxes include both edges and reject outside coordinates");

            for (int capacity = 0; capacity <= 3; capacity++) {
                CompoundTag tag = ShipInventoryPresentation.capture(ship);
                tag.putInt("PageSize", capacity);
                view.apply(tag);
                packets.clear();
                for (int page = 0; page < 3; page++) {
                    int before = packets.size();
                    controls.click(65, 30 + page * 36, 0);
                    controls.release(65, 30 + page * 36, 0);
                    if (page == 0 || capacity >= page) {
                        helper.assertTrue(packets.size() == before + 1, "unlocked cargo tab must send a GUI button");
                        expect(helper, packets, ShipGuiButton.INV_PAGE, page);
                    } else helper.assertTrue(packets.size() == before, "locked cargo tab must not send a GUI button");
                }
            }
            view.apply(ShipInventoryPresentation.capture(ship));
            packets.clear();
            for (int page = 1; page <= 12; page++) {
                controls.click(page <= 6 ? 241 : 249, 134 + (page - 1) % 6 * 13, 0);
                helper.assertTrue(controls.showPageAI == page, "all twelve original control-page tabs must select directly");
            }
            for (int page = 9; page <= 12; page++) {
                controls.showPageAI = page;
                controls.click(180, 134, 0);
            }
            helper.assertTrue(packets.isEmpty(), "legacy reserved pages 9-12 must not acquire invented actions");

            int[] sliderButtons = {ShipGuiButton.FOLLOW_MIN, ShipGuiButton.FOLLOW_MAX,
                    ShipGuiButton.FLEE_HP, ShipGuiButton.WP_STAY, ShipGuiButton.AUTO_CR};
            for (int slider = 0; slider < sliderButtons.length; slider++) {
                for (int raw : new int[] {-20, 0, 21, 42, 80}) {
                    controls.showPageAI = slider < 3 ? 2 : 5;
                    int y = 149 + (slider < 3 ? slider : slider - 3) * 24;
                    controls.click(200, y, 0);
                    controls.drag(191 + raw, y, 0);
                    controls.release(191 + raw, y, 0);
                    float pos = Math.max(0, Math.min(42, raw));
                    int expected = switch (slider) {
                        case 0 -> (int)(pos / 42F * 30F + 1F);
                        case 1 -> (int)(pos / 42F * 30F + 2F);
                        case 2 -> (int)(pos / 42F * 100F);
                        case 3 -> (int)(pos / (42F * 0.0625F));
                        default -> (int)(pos / (43F * 0.25F)) + 1;
                    };
                    expect(helper, packets, sliderButtons[slider], expected);
                    helper.assertTrue(!controls.mousePress && controls.mousePressBar == -1,
                            "release must reset the legacy slider drag state");
                }
            }
            packets.clear();
            controls.click(0, 0, 0);
            controls.release(0, 0, 0);
            helper.assertTrue(packets.isEmpty(), "empty space and release without slider must send no command");

            String[][] flags = {{"UseMelee", "UseAmmoLight", "UseAmmoHeavy", "UseAirLight", "UseAirHeavy", "UseRingEffect"},
                    {"PassiveAI", "OnSightChase", "PVPFirst", "AntiAir", "AntiSS", "TimeKeeper"},
                    {"PickItem", "AutoPump"}, {"ShowHeldItem"}};
            int[][] toggleButtons = {{0, 1, 2, 3, 4, 9}, {8, 10, 11, 12, 13, 14}, {16, 20}, {18}};
            int[] pages = {1, 3, 4, 6};
            for (int group = 0; group < pages.length; group++) {
                controls.showPageAI = pages[group];
                for (int row = 0; row < flags[group].length; row++) {
                    controls.click(180, 134 + row * 13, 0);
                    expect(helper, packets, toggleButtons[group][row], view.getStateFlag(flags[group][row]) ? 0 : 1);
                    controls.release(180, 134 + row * 13, 0);
                }
            }

            controls.showPageAI = 6;
            int[] modelXs = {180, 195, 213, 230};
            for (int part = 0; part < 16; part++) {
                controls.click(modelXs[part % 4], 160 + part / 4 * 13, 0);
                expect(helper, packets, ShipGuiButton.MODEL_STATE_01 + part, 1);
            }
            controls.showPageAI = 7;
            for (int row = 0; row < 2; row++) {
                for (int task = 1; task <= 4; task++) {
                    controls.click(modelXs[task - 1], 134 + row * 13, 0);
                    expect(helper, packets, ShipGuiButton.TASK, view.getStateMinor("Task") == task ? 0 : task);
                }
            }
            for (int flag = 18; flag <= 20; flag++) {
                controls.click(180, 160 + (flag - 18) * 13, 0);
                expect(helper, packets, ShipGuiButton.TASK_SIDE, view.getStateMinor("TaskSide") ^ (1 << flag));
            }
            controls.showPageAI = 8;
            for (int face = 0; face < 18; face++) {
                controls.click(180 + face % 6 * 11, 147 + face / 6 * 26, 0);
                expect(helper, packets, ShipGuiButton.TASK_SIDE, view.getStateMinor("TaskSide") ^ (1 << face));
            }

            for (int slot = 0; slot < LegacyShipEntity.EQUIPMENT_SLOT_COUNT; slot++) ship.getShipInventory().setItem(slot, ItemStack.EMPTY);
            ship.getShipInventory().setItem(0, new ItemStack(ModItems.EQUIPDRUM_ITEMS.get(0).get()));
            ship.getShipInventory().setItem(1, new ItemStack(ModItems.EQUIPDRUM_ITEMS.get(0).get()));
            ShipInventoryMenu menu = new ShipInventoryMenu(33, player.getInventory(), ship.getId(), ship.getShipUid());
            helper.assertTrue(ship.getShipInventory().getContainerSize() == 60 && ship.getInventoryPageSize() == 2,
                    "two drums must unlock the existing 60-slot, three-page backend");
            for (int page = 0; page < 3; page++) {
                ship.getShipInventory().setItem(6 + page * 18, new ItemStack(Items.GOLD_INGOT, page + 1));
                helper.assertTrue(ShipCommandService.handleForTesting(player, ServerboundShipCommandPacket.guiButton(
                        ship.getId(), ship.getShipUid(), ShipGuiButton.INV_PAGE, page)), "legacy page GUI packet must dispatch");
                helper.assertTrue(menu.getInventoryPage() == page && menu.getSlot(6).getContainerSlot() == 6 + page * 18
                        && menu.getSlot(6).getItem().getCount() == page + 1, "server menu must expose the selected cargo page");
            }
            ShipCommandService.setEntityByGui(ship, ShipGuiButton.INV_PAGE, 3);
            helper.assertTrue(menu.getInventoryPage() == 0, "invalid legacy server page resets to zero");
            ShipCommandService.setEntityByGui(ship, ShipGuiButton.FOLLOW_MIN, 10);
            ShipCommandService.setEntityByGui(ship, ShipGuiButton.FOLLOW_MAX, 27);
            ShipCommandService.setEntityByGui(ship, ShipGuiButton.FLEE_HP, 63);
            ShipCommandService.setEntityByGui(ship, ShipGuiButton.TASK_SIDE, (1 << 20) | (1 << 17) | (1 << 6) | 1);
            int before = ship.getModelState();
            ShipCommandService.setEntityByGui(ship, ShipGuiButton.MODEL_STATE_16, 0);
            helper.assertTrue(ship.getFollowMin() == 10 && ship.getFollowMax() == 27 && ship.getFleeHp() == 63
                    && ship.getTaskSide() == ((1 << 20) | (1 << 17) | (1 << 6) | 1)
                    && ship.getModelState() == (before ^ (1 << 15)), "GUI button handler must preserve independent sliders, task masks and XOR visibility");

            CompoundTag snapshot = ShipInventoryPresentation.capture(ship);
            FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());
            try {
                ShipInventoryMenu.writeOpeningData(buffer, ship);
                ShipInventoryMenu opened = ShipInventoryMenu.fromNetwork(34, player.getInventory(), buffer);
                view = opened.getPresentation();
                helper.assertTrue(buffer.readableBytes() == 0 && opened.getShipUid() == ship.getShipUid(),
                        "both direct interaction and command opening data must include the complete menu snapshot");
                opened.removed(player);
            } finally { buffer.release(); }
            helper.assertTrue(view.getStateMinor("FollowMin") == 10 && view.getStateMinor("FollowMax") == 27
                    && view.getStateMinor("FleeHP") == 63, "menu snapshot must round-trip server GUI values");
            for (int attr = 0; attr < LegacyShipStatTables.ATTR_COUNT; attr++) {
                helper.assertTrue(Float.floatToIntBits(view.getAttrsFormation(attr)) == Float.floatToIntBits(ship.getLegacyStats().formationStat(attr)),
                        "inventory formation tooltip must receive the actual server formation attribute " + attr);
            }
            snapshot.getCompound("Minor").putInt("FormatType", 2);
            snapshot.getCompound("Minor").putInt("FormatPos", 4);
            int[] modifiers = new int[LegacyShipStatTables.ATTR_COUNT];
            for (int i = 0; i < modifiers.length; i++) modifiers[i] = Float.floatToIntBits(1.25F);
            modifiers[LegacyShipStatTables.Attr.HIT] = Float.floatToIntBits(2.5F);
            modifiers[LegacyShipStatTables.Attr.MOV] = Float.floatToIntBits(0.125F);
            snapshot.putIntArray("Formation", modifiers);
            view.apply(snapshot);
            List<String> tooltip = view.formationTooltipValues();
            helper.assertTrue(tooltip.size() == 17 && tooltip.get(0).equals("5")
                    && tooltip.get(1).equals("x 125 % / 125 %") && tooltip.get(3).equals("x 125 %")
                    && tooltip.get(4).equals("+ 2.5") && tooltip.get(11).equals("x 125 %")
                    && tooltip.get(12).equals("+ 125 %") && tooltip.get(16).equals("+ 0.13"),
                    "formation hover must retain the legacy one-based position, multipliers, additions and precision");
        } finally { ship.load(original); }
    }

    private static void expect(GameTestHelper helper, List<int[]> packets, int button, int value) {
        helper.assertTrue(!packets.isEmpty(), "legacy control must send a GUI button " + button);
        int[] packet = packets.get(packets.size() - 1);
        helper.assertTrue(packet[0] == button && packet[1] == value,
                "legacy GUI button mismatch: expected " + button + "/" + value + ", got " + packet[0] + "/" + packet[1]);
    }
}
