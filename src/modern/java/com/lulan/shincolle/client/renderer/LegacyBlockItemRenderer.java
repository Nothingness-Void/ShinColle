package com.lulan.shincolle.client.renderer;

import com.lulan.shincolle.block.SmallShipyardBlock;
import com.lulan.shincolle.blockentity.DeskBlockEntity;
import com.lulan.shincolle.blockentity.SmallShipyardBlockEntity;
import com.lulan.shincolle.registry.ModBlocks;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

import net.minecraftforge.client.extensions.common.IClientItemExtensions;

import java.util.function.Consumer;

public final class LegacyBlockItemRenderer extends BlockEntityWithoutLevelRenderer {
    private final DeskBlockEntity desk = new DeskBlockEntity(BlockPos.ZERO, ModBlocks.BLOCK_DESK.get().defaultBlockState());
    private final SmallShipyardBlockEntity shipyard = new SmallShipyardBlockEntity(BlockPos.ZERO,
            ModBlocks.BLOCK_SMALL_SHIPYARD.get().defaultBlockState().setValue(SmallShipyardBlock.ACTIVE, true));

    private LegacyBlockItemRenderer() {
        super(Minecraft.getInstance().getBlockEntityRenderDispatcher(), Minecraft.getInstance().getEntityModels());
    }

    public static void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            private LegacyBlockItemRenderer renderer;

            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                if (renderer == null) renderer = new LegacyBlockItemRenderer();
                return renderer;
            }
        });
    }

    @Override
    public void renderByItem(ItemStack stack, ItemDisplayContext displayContext, PoseStack poses,
                             MultiBufferSource buffers, int light, int overlay) {
        var dispatcher = Minecraft.getInstance().getBlockEntityRenderDispatcher();
        if (stack.is(ModBlocks.BLOCK_DESK.get().asItem())) {
            dispatcher.renderItem(desk, poses, buffers, light, overlay);
        } else if (stack.is(ModBlocks.BLOCK_SMALL_SHIPYARD.get().asItem())) {
            dispatcher.renderItem(shipyard, poses, buffers, light, overlay);
        }
    }
}
