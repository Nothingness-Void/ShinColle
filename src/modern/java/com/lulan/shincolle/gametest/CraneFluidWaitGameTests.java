package com.lulan.shincolle.gametest;

import com.lulan.shincolle.ShinColle;
import com.lulan.shincolle.blockentity.CraneBlockEntity;
import com.lulan.shincolle.entity.ship.LegacyShipEntity;
import com.lulan.shincolle.registry.ModBlocks;
import com.lulan.shincolle.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;

@GameTestHolder(ShinColle.MOD_ID)
@PrefixGameTestTemplate(false)
public final class CraneFluidWaitGameTests {
    private CraneFluidWaitGameTests() {}

    @GameTest(template = "gameplayparitygametests.empty")
    public static void emptyCraneTankDoesNotBypassLegacyContainerWaits(GameTestHelper helper) throws Exception {
        BlockPos pos = helper.absolutePos(new BlockPos(2, 2, 2));
        helper.getLevel().setBlock(pos, ModBlocks.BLOCK_CRANE.get().defaultBlockState(), Block.UPDATE_ALL);
        var crane = (CraneBlockEntity) helper.getLevel().getBlockEntity(pos);
        crane.onLoad();
        crane.toggleLoadEnabled();
        crane.toggleUnloadEnabled();
        crane.cycleLiquidMode();
        crane.cycleWaitMode();
        var ending = CraneBlockEntity.class.getDeclaredMethod("checkCraneEnding", LegacyShipEntity.class,
                IItemHandler.class, IItemHandler.class, boolean[].class);
        ending.setAccessible(true);
        var cargo = new ItemStackHandler(1);
        var chest = new ItemStackHandler(1);
        cargo.setStackInSlot(0, new ItemStack(Items.BUCKET));
        helper.assertTrue(!(boolean) ending.invoke(crane, null, chest, cargo, new boolean[4]),
                "UntilFull must wait for an empty bucket even when crane tank has no fluid");
        var partialTank = FluidUtil.getFluidHandler(new ItemStack(ModItems.SHIPTANK.get())).resolve().orElseThrow();
        partialTank.fill(new FluidStack(Fluids.WATER, 1000), IFluidHandler.FluidAction.EXECUTE);
        cargo.setStackInSlot(0, partialTank.getContainer());
        helper.assertTrue(!(boolean) ending.invoke(crane, null, chest, cargo, new boolean[4]),
                "null legacy target fluid is a wildcard for a partially filled ship tank");
        cargo.setStackInSlot(0, new ItemStack(Items.WATER_BUCKET));
        helper.assertTrue((boolean) ending.invoke(crane, null, chest, cargo, new boolean[4]),
                "UntilFull may finish when the bucket is actually full");
        crane.cycleLiquidMode();
        crane.cycleWaitMode();
        helper.assertTrue(!(boolean) ending.invoke(crane, null, chest, cargo, new boolean[4]),
                "UntilEmpty must wait on a water bucket when no transfer work occurred");
        cargo.setStackInSlot(0, new ItemStack(Items.BUCKET));
        helper.assertTrue((boolean) ending.invoke(crane, null, chest, cargo, new boolean[4]),
                "UntilEmpty finishes for an actually empty bucket");
        cargo.setStackInSlot(0, new ItemStack(Items.STONE));
        helper.assertTrue((boolean) ending.invoke(crane, null, chest, cargo, new boolean[4]),
                "non-container items must not block fluid-only waits");
        cargo.setStackInSlot(0, ItemStack.EMPTY);
        helper.assertTrue((boolean) ending.invoke(crane, null, chest, cargo, new boolean[4]),
                "empty inventories finish fluid-only waits");
        helper.succeed();
    }
}
