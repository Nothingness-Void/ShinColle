package com.lulan.shincolle.gametest;

import com.lulan.shincolle.ShinColle;
import com.lulan.shincolle.blockentity.HeavyGrudgeBlockEntity;
import com.lulan.shincolle.menu.LargeShipyardMenu;
import com.lulan.shincolle.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(ShinColle.MOD_ID)
@PrefixGameTestTemplate(false)
public final class LegacyShipyardGuiGameTests {
    private LegacyShipyardGuiGameTests() {}

    @GameTest(template = "gameplayparitygametests.empty")
    public static void largeShipyardShowsOnlyLegacyMaterialAndFuelErrors(GameTestHelper helper) {
        BlockPos pos = helper.absolutePos(new BlockPos(1, 2, 1));
        helper.getLevel().setBlockAndUpdate(pos, ModBlocks.BLOCK_GRUDGE_HEAVY.get().defaultBlockState());
        var shipyard = (HeavyGrudgeBlockEntity) helper.getLevel().getBlockEntity(pos);
        var menu = new LargeShipyardMenu(1, helper.makeMockPlayer(net.minecraft.world.level.GameType.CREATIVE).getInventory(), shipyard);
        var data = shipyard.getContainerData();
        for (int structure : new int[] {0, 1}) {
            data.set(4, structure);
            for (boolean occupied : new boolean[] {false, true}) {
                shipyard.getItems().setStackInSlot(0, occupied ? new ItemStack(Items.STONE) : ItemStack.EMPTY);
                for (int mode : new int[] {0, 1, 2}) {
                    data.set(0, mode);
                    for (int goal : new int[] {0, 1000}) {
                        data.set(3, goal);
                        for (int fuel : new int[] {0, HeavyGrudgeBlockEntity.buildSpeed(), HeavyGrudgeBlockEntity.buildSpeed() + 1}) {
                            data.set(2, fuel);
                            Component expected = goal <= 0 && mode != 0
                                    ? Component.translatable("gui.shincolle.shipyard.nomaterial")
                                    : fuel <= HeavyGrudgeBlockEntity.buildSpeed()
                                            ? Component.translatable("gui.shincolle.shipyard.nofuel") : Component.empty();
                            helper.assertTrue(menu.getFailureMessage().equals(expected),
                                    "only legacy material/fuel errors, in legacy priority, regardless of structure/output state");
                        }
                    }
                }
            }
        }
        helper.succeed();
    }
}
