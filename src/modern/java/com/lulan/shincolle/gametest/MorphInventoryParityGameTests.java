package com.lulan.shincolle.gametest;

import com.lulan.shincolle.menu.LegacyMorphInventoryControls;
import com.lulan.shincolle.menu.MorphInventoryMenu;
import com.lulan.shincolle.menu.MorphInventoryPresentation;
import com.lulan.shincolle.morph.MorphProfile;
import com.lulan.shincolle.network.GameplayCommandHandler;
import com.lulan.shincolle.network.GameplayCommandType;
import com.lulan.shincolle.network.ShipGuiButton;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;

import java.util.ArrayList;
import java.util.List;

public final class MorphInventoryParityGameTests {
    private MorphInventoryParityGameTests() {}

    public static void check(GameTestHelper helper, ServerPlayer player, MorphInventoryMenu menu) {
        MorphProfile profile = menu.getProfile();
        MorphInventoryPresentation view = menu.getPresentation();
        List<Integer> buttons = new ArrayList<>();
        List<GameplayCommandType> supplies = new ArrayList<>();
        LegacyMorphInventoryControls controls = new LegacyMorphInventoryControls(view, (button, value) -> {
            buttons.add(button);
            CompoundTag tag = new CompoundTag();
            tag.putInt("Button", button); tag.putInt("Value", value);
            GameplayCommandHandler.handle(player, GameplayCommandType.MORPH_GUI_BUTTON, tag);
        }, supplies::add);
        helper.assertTrue(controls.showPage == 1 && controls.showPageAI == 1, "legacy morph inventory starts on page one");
        controls.click(133, 53, 0);
        helper.assertTrue(controls.showPage == 2, "second old tab selects attributes");
        controls.click(133, 89, 1);
        helper.assertTrue(controls.showPage == 3, "third old tab selects details regardless of mouse button");
        controls.click(133, 18, 0);
        helper.assertTrue(controls.showPage == 1, "first old tab selects experience and supplies");
        controls.click(173, 131, 0);
        helper.assertTrue(profile.isNoFuel() && buttons.get(buttons.size() - 1) == ShipGuiButton.NO_FUEL,
                "no-fuel control sends the original button and explicit inverse flag");
        controls.click(173, 131, 1);
        helper.assertTrue(!profile.isNoFuel(), "repeated no-fuel clicks restore the flag, not the active morph state");
        controls.click(173, 144, 0);
        helper.assertTrue(profile.isSitting() && buttons.get(buttons.size() - 1) == ShipGuiButton.SIT,
                "sit sends the original toggle button");
        controls.click(173, 144, 0);
        helper.assertTrue(!profile.isSitting(), "sit toggles back without a payload-dependent state");
        view.getRNG().setSeed(10L);
        int expectedEmotion = RandomSource.create(10L).nextInt(9);
        controls.click(173, 157, 0); controls.click(173, 170, 0);
        helper.assertTrue(profile.getEmotion() == expectedEmotion && profile.getEmotion4() == 4,
                "the first emotion control uses the old nextInt(9); the second toggles its separate slot");
        controls.click(239, 196, 0);
        helper.assertTrue(controls.showPageAI == 6, "old model-controls page hot area");
        controls.click(174, 157, 0);
        helper.assertTrue(profile.getModelState() == 1, "first grouped model bit uses the original XOR semantics");
        controls.click(174, 157, 0);
        helper.assertTrue(profile.getModelState() == 0, "repeating the same model control toggles its bit back");
        int count = buttons.size();
        controls.click(-1, -1, 0); controls.click(8, 6, 0); controls.click(236, 6, 0); controls.click(170, 32, 0);
        helper.assertTrue(buttons.size() == count, "invalid space and removed modern profile/activation controls send nothing");
        controls.click(7, 18, 0); controls.click(25, 53, 0); controls.click(7, 54, 0);
        helper.assertTrue(supplies.equals(List.of(GameplayCommandType.MORPH_ADD_LIGHT_AMMO,
                GameplayCommandType.MORPH_ADD_HEAVY_AMMO, GameplayCommandType.MORPH_ADD_GRUDGE)),
                "old supply buttons retain GuiHelper's inclusive rectangles and existing handlers");
        helper.assertTrue(LegacyMorphInventoryControls.supplyButtonAt(6, 18) == -1
                && LegacyMorphInventoryControls.supplyButtonAt(25, 71) == 2
                && LegacyMorphInventoryControls.supplyButtonAt(26, 71) == -1, "supply hit-area edges");
        profile.applyGuiButton(ShipGuiButton.NO_FUEL, 1);
        profile.applyGuiButton(ShipGuiButton.SIT, 99);
        profile.applyGuiButton(ShipGuiButton.MODEL_STATE_16, 0);
        MorphProfile copy = profile.copy();
        helper.assertTrue(copy.isNoFuel() && copy.isSitting() && copy.getModelState() == 32768
                && copy.getEmotion() == expectedEmotion && copy.getEmotion4() == 4, "GUI state survives profile save/sync round trips");
        CompoundTag before = profile.saveToTag(new CompoundTag());
        helper.assertTrue(!profile.applyGuiButton(-1, 0) && !profile.applyGuiButton(999, 1)
                && before.equals(profile.saveToTag(new CompoundTag())), "unknown morph GUI buttons do not mutate data");
        MorphProfile empty = new MorphProfile();
        empty.loadFromTag(new CompoundTag());
        helper.assertTrue(!empty.isNoFuel() && !empty.isSitting() && empty.getModelState() == 0, "older/empty data has old default GUI flags");
        view.refresh(player, new MorphProfile(2047));
        helper.assertTrue(view.getShipClass() == 47 && view.getModelPos().length == 4,
                "hostile unlocks reuse explicit counterpart metadata without changing their combat profile");
        view.refresh(player, null);
        helper.assertTrue(view.getPlayer() == player && !view.isSitting() && !view.getStateFlag("NoFuel"), "empty morph view is safe");
        menu.getPresentation();
        helper.assertTrue(view.getPlayer() == player && view.getStateMinor("ShipLevel") == profile.getLevel()
                && view.getAttrsBuffed(0) == profile.buildStats(player.getActiveEffects()).copyBuffed()[0],
                "attributes use the selected profile and HP uses the actual player, not a temporary ship");
    }
}
