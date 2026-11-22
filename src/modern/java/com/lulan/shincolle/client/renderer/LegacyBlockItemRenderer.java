package com.lulan.shincolle.client.renderer;

import com.lulan.shincolle.ShinColle;
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
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;

@EventBusSubscriber(modid = ShinColle.MOD_ID, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.MOD)
public final class LegacyBlockItemRenderer extends BlockEntityWithoutLevelRenderer {
    private final DeskBlockEntity desk = new DeskBlockEntity(BlockPos.ZERO, ModBlocks.BLOCK_DESK.get().defaultBlockState());
    private final SmallShipyardBlockEntity shipyard = new SmallShipyardBlockEntity(BlockPos.ZERO,
            ModBlocks.BLOCK_SMALL_SHIPYARD.get().defaultBlockState().setValue(SmallShipyardBlock.ACTIVE, true));

    private LegacyBlockItemRenderer() {
        super(Minecraft.getInstance().getBlockEntityRenderDispatcher(), Minecraft.getInstance().getEntityModels());
    }

    @SubscribeEvent
    public static void registerItemExtensions(RegisterClientExtensionsEvent event) {
        event.registerItem(new IClientItemExtensions() {
            private LegacyBlockItemRenderer renderer;

            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                if (renderer == null) renderer = new LegacyBlockItemRenderer();
                return renderer;
            }
        }, ModBlocks.BLOCK_DESK.get().asItem(), ModBlocks.BLOCK_SMALL_SHIPYARD.get().asItem());
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
