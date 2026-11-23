package com.lulan.shincolle.gametest;

import com.lulan.shincolle.blockentity.LegacyCoreBlockEntity;
import com.lulan.shincolle.config.ShinColleConfig;
import com.lulan.shincolle.registry.ModBlocks;
import com.lulan.shincolle.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

@GameTestHolder("shincolle")
@PrefixGameTestTemplate(false)
public final class AuditVolCoreFuelGameTests {
    @GameTest(template = "gameplayparitygametests.empty", batch = "audit_parity")
    public static void fuelMustFitStrictlyAndNeverLoseOverflow(GameTestHelper helper) throws Exception {
        var core = LegacyCoreBlockEntity.newVolCore(BlockPos.ZERO, ModBlocks.BLOCK_VOL_CORE.get().defaultBlockState());
        var charge = LegacyCoreBlockEntity.class.getDeclaredField("storedCharge");
        charge.setAccessible(true);
        var refuel = LegacyCoreBlockEntity.class.getDeclaredMethod("refuelFromInventory");
        refuel.setAccessible(true);
        ShinColleConfig.setTestOverride("tileVolCore", new double[] {1000, 16, 240});
        try {
            for (int before : new int[] {761, 760, 759}) {
                charge.setInt(core, before);
                core.getFuelItems().setStackInSlot(0, new ItemStack(ModItems.GRUDGE.get(), 2));
                boolean changed = (boolean) refuel.invoke(core);
                helper.assertTrue(changed == (before == 759), "overflow and exact fill must reject");
                helper.assertTrue(core.getStoredCharge() == (before == 759 ? 999 : before), "whole fuel or no charge");
                helper.assertTrue(core.getFuelItems().getStackInSlot(0).getCount() == (before == 759 ? 1 : 2),
                        "rejected addition never consumes an item");
            }
            core.getFuelItems().setStackInSlot(0, ItemStack.EMPTY);
            helper.assertTrue(!(boolean) refuel.invoke(core), "empty input does nothing");
            core.getFuelItems().setStackInSlot(0, new ItemStack(Items.STICK));
            helper.assertTrue(!(boolean) refuel.invoke(core), "nonfuel input does nothing");
        } finally {
            ShinColleConfig.clearTestOverride("tileVolCore");
        }
        helper.succeed();
    }
}
