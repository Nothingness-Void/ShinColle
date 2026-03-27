package com.lulan.shincolle;

import com.mojang.logging.LogUtils;
import com.lulan.shincolle.client.screen.DeskReferenceScreen;
import com.lulan.shincolle.client.screen.RecipePaperScreen;
import com.lulan.shincolle.item.PointerItem;
import com.lulan.shincolle.registry.ModBlocks;
import com.lulan.shincolle.registry.ModCreativeModeTabs;
import com.lulan.shincolle.registry.ModItems;
import com.lulan.shincolle.registry.ModMenus;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

@Mod(ShinColle.MOD_ID)
public class ShinColle {

    public static final String MOD_ID = "shincolle";

    private static final Logger LOGGER = LogUtils.getLogger();

    public ShinColle(FMLJavaModLoadingContext context) {
        IEventBus modEventBus = context.getModEventBus();
        modEventBus.addListener(this::onCommonSetup);
        ModBlocks.BLOCKS.register(modEventBus);
        ModItems.ITEMS.register(modEventBus);
        ModMenus.MENUS.register(modEventBus);
        ModCreativeModeTabs.TABS.register(modEventBus);
        MinecraftForge.EVENT_BUS.register(this);
    }

    private void onCommonSetup(final FMLCommonSetupEvent event) {
        LOGGER.info("Initializing ShinColle 1.20.1 port content batch");
    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        LOGGER.info("ShinColle 1.20.1 server starting");
    }

    @Mod.EventBusSubscriber(modid = MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents {

        @SubscribeEvent
        @SuppressWarnings("removal")
        public static void onClientSetup(FMLClientSetupEvent event) {
            event.enqueueWork(() -> {
                MenuScreens.register(ModMenus.RECIPE_PAPER.get(), RecipePaperScreen::new);
                MenuScreens.register(ModMenus.DESK_REFERENCE.get(), DeskReferenceScreen::new);
                ItemProperties.register(ModItems.POINTERITEM.get(), ResourceLocation.fromNamespaceAndPath(MOD_ID, "mode"),
                        (stack, level, entity, seed) -> PointerItem.getModelMode(stack));
                ItemBlockRenderTypes.setRenderLayer(ModBlocks.BLOCK_GRUDGE.get(), RenderType.translucent());
                ItemBlockRenderTypes.setRenderLayer(ModBlocks.BLOCK_GRUDGE_XP.get(), RenderType.translucent());
            });
        }
    }
}
