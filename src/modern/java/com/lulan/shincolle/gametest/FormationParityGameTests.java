package com.lulan.shincolle.gametest;

import com.lulan.shincolle.entity.ship.LegacyShipEntity;
import com.lulan.shincolle.entity.ship.LegacyShipStatTables;
import com.lulan.shincolle.formation.LegacyFormationScreenState;
import com.lulan.shincolle.menu.FormationMenu;
import com.lulan.shincolle.registry.ModEntityTypes;
import com.lulan.shincolle.teitoku.TeitokuData;
import com.lulan.shincolle.teitoku.TeitokuHelper;
import com.lulan.shincolle.teitoku.TeitokuSavedData;
import com.mojang.authlib.GameProfile;
import io.netty.buffer.Unpooled;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.common.util.FakePlayerFactory;

import java.util.Arrays;
import java.util.UUID;

public final class FormationParityGameTests {
    private FormationParityGameTests() {}

    public static void formationClicksUseLegacyHotspotsAndGuiWaits(GameTestHelper helper) {
        LegacyFormationScreenState state = new LegacyFormationScreenState();
        for (int i = 0; i < 6; i++) {
            helper.assertTrue(LegacyFormationScreenState.buttonAt(17 + i * 18, 148) == i,
                    "formation hotbox start should be inclusive");
            helper.assertTrue(LegacyFormationScreenState.buttonAt(34 + i * 18, 165) == i,
                    "formation hotbox end should be inclusive");
        }
        for (int i = 0; i < 9; i++) {
            helper.assertTrue(LegacyFormationScreenState.buttonAt(17 + i * 12, 166) == i + 6,
                    "all nine fleet buttons should select directly");
        }
        int[] rows = {5, 33, 60, 87, 114, 141};
        for (int i = 0; i < 6; i++) {
            helper.assertTrue(LegacyFormationScreenState.buttonAt(142, rows[i]) == i + 15,
                    "legacy ship-row hitboxes should be retained");
        }
        helper.assertTrue(LegacyFormationScreenState.buttonAt(0, 0) == -1
                && LegacyFormationScreenState.buttonAt(251, 5) == -1, "outside clicks should do nothing");
        helper.assertTrue(LegacyFormationScreenState.buttonAt(159, 170) == 21
                && LegacyFormationScreenState.buttonAt(203, 170) == 22
                && LegacyFormationScreenState.buttonAt(46, 180) == 23, "reorder/name hotboxes should match GuiHelper");
        helper.assertTrue(!state.clickRow(3), "first click of a different row must only change the viewed row");
        for (int i = 0; i < 1000; i++) state.advanceGuiTick();
        helper.assertTrue(state.clickRow(3), "same-row click must open inventory regardless of elapsed time");
        helper.assertTrue(!state.clickRow(-1) && !state.selectTeam(9), "invalid hits must not alter UI state");
        helper.assertTrue(state.selectTeam(8) && state.waitTicks == 60, "fleet change must wait 60 GUI ticks");
        helper.assertTrue(state.swapRow(true) == -1 && state.selectedRow == 3,
                "reorder must be blocked during synchronization");
        for (int i = 0; i < 59; i++) {
            helper.assertTrue(state.advanceGuiTick() == (i == 58), "ship-list refresh must occur at remaining tick 1");
        }
        helper.assertTrue(state.waitTicks == 1 && state.swapRow(false) == -1, "last wait tick must still block reorder");
        state.advanceGuiTick();
        state.clickRow(0);
        helper.assertTrue(state.swapRow(true) == 5 && state.waitTicks == 40, "upward reorder must wrap and wait 40 ticks");
        for (int i = 0; i < 40; i++) state.advanceGuiTick();
        helper.assertTrue(state.swapRow(false) == 0, "downward reorder must wrap to row zero");
        state.namingTeam = 8;
        helper.assertTrue(!state.selectTeam(0) && state.team == 8, "fleet switching must stay blocked during naming");
        int[] morale = {5101, 5100, 3901, 3900, 2101, 2100, 901, 900, 0};
        int[] icons = {0, 11, 11, 22, 22, 33, 33, 44, 44};
        for (int i = 0; i < morale.length; i++) helper.assertTrue(
                LegacyFormationScreenState.moraleIconX(morale[i]) == icons[i], "morale icon thresholds must use strict legacy comparisons");
    }

    public static void formationMenuSyncsServerAttributesAndPreviewMatrix(GameTestHelper helper) {
        ServerPlayer player = FakePlayerFactory.get(helper.getLevel(), new GameProfile(UUID.randomUUID(), "FormationViews"));
        TeitokuData data = TeitokuHelper.get(player).orElseThrow(IllegalStateException::new);
        data.setPlayerUid(TeitokuSavedData.get(helper.getLevel()).getOrCreateUid(player));
        LegacyShipEntity[] ships = new LegacyShipEntity[6];
        for (int i = 0; i < ships.length; i++) {
            LegacyShipEntity ship = ModEntityTypes.LEGACY_SHIP.get().create(helper.getLevel());
            helper.assertTrue(ship != null, "formation fixture ship must be creatable");
            ship.setNoAi(true);
            ship.setPos(48.5D + i * 2, 2D, 48.5D);
            helper.getLevel().addFreshEntity(ship);
            ship.setOwner(player);
            ship.setShipLevel(i + 5);
            data.assignCurrentTeamSlot(i, ship.getShipUid());
            ships[i] = ship;
        }
        float minimum = 10F;
        for (LegacyShipEntity ship : ships) minimum = Math.min(minimum, ship.getLegacyStats().moveSpeed());
        FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());
        FormationMenu menu;
        try {
            buffer.writeNbt(FormationMenu.captureVisualData(player));
            menu = FormationMenu.fromNetwork(43, player.getInventory(), buffer);
        } finally {
            buffer.release();
        }
        FormationMenu.ShipView[] views = menu.getShipEntityAll(0);
        for (int i = 0; i < 6; i++) {
            FormationMenu.ShipView view = views[i];
            LegacyShipEntity ship = ships[i];
            helper.assertTrue(view != null && view.getUid() == ship.getShipUid()
                    && view.getEntityId() == ship.getId() && view.getLevel() == ship.getShipLevel(),
                    "ship identity must survive menu opening-data transport");
            helper.assertTrue(view.getMinMOV() == minimum && view.getMorale() == ship.getMorale()
                    && view.getHealth() == ship.getHealth() && view.getMaxHealth() == ship.getMaxHealth(),
                    "formation screen must receive actual morale, health and team minimum speed");
            helper.assertTrue(view.getFormatType() == ship.getLegacyFormationId()
                    && view.getFormatPos() == ship.getLegacyFormationSlot(), "actual applied formation state must be synchronized");
            helper.assertTrue(Arrays.equals(view.copyWithoutFormation(), ship.getLegacyStats().copyWithoutFormation()),
                    "unbuffed tooltip attributes must come from the server calculation");
            for (int attr = 0; attr < LegacyShipStatTables.ATTR_COUNT; attr++) {
                helper.assertTrue(view.getAttrsBuffed(attr) == ship.getLegacyStats().get(attr)
                        && view.getAttrsFormation(attr) == ship.getLegacyStats().formationStat(attr),
                        "all 21 buffed/formation attributes must retain float precision");
            }
        }
        for (int formation = 0; formation < 6; formation++) {
            for (int position = 0; position < 6; position++) helper.assertTrue(Arrays.equals(
                    menu.getFormationBuffValue(formation, position), LegacyShipStatTables.copyFormationStats(formation, position)),
                    "all 36 formation-position previews must use the server legacy table");
        }
        helper.assertTrue(Arrays.stream(menu.getShipEntityAll(8)).allMatch(view -> view == null), "empty fleets must have no signal");
        menu.applyVisualData(new CompoundTag());
        helper.assertTrue(Arrays.stream(menu.getShipEntityAll(0)).allMatch(view -> view == null)
                && Arrays.equals(menu.getFormationBuffValue(-1, 10), LegacyShipStatTables.copyResetFormation()),
                "missing/invalid menu data must not fabricate ships or bonuses");
        for (LegacyShipEntity ship : ships) ship.discard();
    }
}
