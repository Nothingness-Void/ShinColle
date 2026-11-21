package com.lulan.shincolle;

import com.mojang.logging.LogUtils;
import com.lulan.shincolle.client.renderer.blockentity.DeskBlockEntityRenderer;
import com.lulan.shincolle.client.renderer.blockentity.HeavyGrudgeBlockEntityRenderer;
import com.lulan.shincolle.client.renderer.blockentity.SmallShipyardBlockEntityRenderer;
import com.lulan.shincolle.client.renderer.entity.LegacyShipAircraftRenderer;
import com.lulan.shincolle.client.renderer.entity.LegacyShipFloatingFortRenderer;
import com.lulan.shincolle.client.renderer.entity.LegacyShipProjectileRenderer;
import com.lulan.shincolle.client.renderer.entity.LegacyShipBeamRenderer;
import com.lulan.shincolle.client.renderer.entity.LegacyShipStaticEffectRenderer;
import com.lulan.shincolle.client.renderer.entity.LegacyShipMountRenderer;
import com.lulan.shincolle.client.renderer.entity.LegacyShipRenderer;
import com.lulan.shincolle.client.renderer.entity.LegacyShipRensouhouRenderer;
import com.lulan.shincolle.client.renderer.entity.LegacyShipFishingHookRenderer;
import com.lulan.shincolle.client.screen.CraneTerminalScreen;
import com.lulan.shincolle.client.screen.DeskScreen;
import com.lulan.shincolle.client.screen.FormationScreen;
import com.lulan.shincolle.client.screen.LargeShipyardScreen;
import com.lulan.shincolle.client.screen.LegacyCoreScreen;
import com.lulan.shincolle.client.screen.MorphInventoryScreen;
import com.lulan.shincolle.client.screen.RecipePaperScreen;
import com.lulan.shincolle.client.screen.ShipInventoryScreen;
import com.lulan.shincolle.client.screen.SmallShipyardScreen;
import com.lulan.shincolle.entity.ship.LegacyShipEntity;
import com.lulan.shincolle.entity.ship.LegacyShipRensouhouEntity;
import com.lulan.shincolle.entity.projectile.LegacyShipBeamEntity;
import com.lulan.shincolle.command.ShinColleCommands;
import com.lulan.shincolle.config.ShinColleConfig;
import com.lulan.shincolle.crafting.PolyAsMnCondition;
import com.lulan.shincolle.item.PointerItem;
import com.lulan.shincolle.network.ModNetwork;
import com.lulan.shincolle.registry.ModBlockEntities;
import com.lulan.shincolle.registry.ModBlocks;
import com.lulan.shincolle.registry.ModCreativeModeTabs;
import com.lulan.shincolle.registry.ModEntityTypes;
import com.lulan.shincolle.registry.ModFeatures;
import com.lulan.shincolle.registry.ModItems;
import com.lulan.shincolle.registry.ModMenus;
import com.lulan.shincolle.registry.ModSoundEvents;
import com.lulan.shincolle.teitoku.TeitokuEvents;
import com.lulan.shincolle.world.ShipChunkLoader;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.ModContainer;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.neoforged.neoforge.registries.RegisterEvent;
import org.slf4j.Logger;

@Mod(ShinColle.MOD_ID)
public class ShinColle {

    public static final String MOD_ID = "shincolle";

    private static final Logger LOGGER = LogUtils.getLogger();

    public ShinColle(IEventBus modEventBus, ModContainer context) {
        context.registerConfig(ModConfig.Type.COMMON, ShinColleConfig.COMMON_SPEC);
        context.registerConfig(ModConfig.Type.CLIENT, ShinColleConfig.CLIENT_SPEC);
        modEventBus.addListener(this::onCommonSetup);
        modEventBus.addListener(this::onEntityAttributeCreation);
        com.lulan.shincolle.teitoku.TeitokuHelper.ATTACHMENTS.register(modEventBus);
        com.lulan.shincolle.registry.ModDataComponents.COMPONENTS.register(modEventBus);
        modEventBus.addListener(ShipChunkLoader::register);
        modEventBus.addListener(com.lulan.shincolle.registry.ModCapabilities::register);
        PolyAsMnCondition.SERIALIZERS.register(modEventBus);
        ModBlockEntities.BLOCK_ENTITIES.register(modEventBus);
        ModBlocks.BLOCKS.register(modEventBus);
        ModEntityTypes.ENTITY_TYPES.register(modEventBus);
        ModFeatures.FEATURES.register(modEventBus);
        ModItems.ITEMS.register(modEventBus);
        ModMenus.MENUS.register(modEventBus);
        ModSoundEvents.SOUND_EVENTS.register(modEventBus);
        ModCreativeModeTabs.TABS.register(modEventBus);
        modEventBus.addListener(ModNetwork::register);
        NeoForge.EVENT_BUS.register(this);
        NeoForge.EVENT_BUS.addListener(ShinColleCommands::register);
    }

    private void onCommonSetup(final FMLCommonSetupEvent event) {
        LOGGER.info("Initializing ShinColle 1.20.1 port content batch");
    }

    private void onEntityAttributeCreation(EntityAttributeCreationEvent event) {
        event.put(ModEntityTypes.LEGACY_SHIP.get(), LegacyShipEntity.createAttributes().build());
        event.put(ModEntityTypes.LEGACY_RENSOUHOU.get(), LegacyShipRensouhouEntity.createAttributes().build());
    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        LOGGER.info("ShinColle 1.20.1 server starting");
    }

    @EventBusSubscriber(modid = MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents {

        @SubscribeEvent
        public static void registerScreens(net.neoforged.neoforge.client.event.RegisterMenuScreensEvent event) {
            event.register(ModMenus.CRANE_TERMINAL.get(), CraneTerminalScreen::new);
            event.register(ModMenus.RECIPE_PAPER.get(), RecipePaperScreen::new);
            event.register(ModMenus.DESK.get(), DeskScreen::new);
            event.register(ModMenus.FORMATION.get(), FormationScreen::new);
            event.register(ModMenus.LARGE_SHIPYARD.get(), LargeShipyardScreen::new);
            event.register(ModMenus.LEGACY_CORE.get(), LegacyCoreScreen::new);
            event.register(ModMenus.MORPH_INVENTORY.get(), MorphInventoryScreen::new);
            event.register(ModMenus.SMALL_SHIPYARD.get(), SmallShipyardScreen::new);
            event.register(ModMenus.SHIP_INVENTORY.get(), ShipInventoryScreen::new);
        }

        @SubscribeEvent
        @SuppressWarnings("removal")
        public static void onClientSetup(FMLClientSetupEvent event) {
            event.enqueueWork(() -> {
                BlockEntityRenderers.register(ModBlockEntities.DESK.get(), DeskBlockEntityRenderer::new);
                BlockEntityRenderers.register(ModBlockEntities.GRUDGE_HEAVY.get(), HeavyGrudgeBlockEntityRenderer::new);
                BlockEntityRenderers.register(ModBlockEntities.SMALL_SHIPYARD.get(), SmallShipyardBlockEntityRenderer::new);
                EntityRenderers.register(ModEntityTypes.LEGACY_SHIP.get(), LegacyShipRenderer::new);
                EntityRenderers.register(ModEntityTypes.RECOVERED_SHIP_ITEM.get(), net.minecraft.client.renderer.entity.ItemEntityRenderer::new);
                EntityRenderers.register(ModEntityTypes.LEGACY_SHIP_PROJECTILE.get(), LegacyShipProjectileRenderer::new);
                EntityRenderers.register(ModEntityTypes.LEGACY_SHIP_BEAM.get(), LegacyShipBeamRenderer::new);
                EntityRenderers.register(ModEntityTypes.LEGACY_SHIP_STATIC_EFFECT.get(), LegacyShipStaticEffectRenderer::new);
                EntityRenderers.register(ModEntityTypes.LEGACY_SHIP_FLOATING_FORT.get(), LegacyShipFloatingFortRenderer::new);
                EntityRenderers.register(ModEntityTypes.LEGACY_SHIP_AIRCRAFT.get(), LegacyShipAircraftRenderer::new);
                EntityRenderers.register(ModEntityTypes.LEGACY_SHIP_TAKOYAKI.get(), LegacyShipAircraftRenderer::new);
                EntityRenderers.register(ModEntityTypes.LEGACY_SHIP_MOUNT.get(), LegacyShipMountRenderer::new);
                EntityRenderers.register(ModEntityTypes.LEGACY_RENSOUHOU.get(), LegacyShipRensouhouRenderer::new);
                EntityRenderers.register(ModEntityTypes.LEGACY_SHIP_FISHING_HOOK.get(), LegacyShipFishingHookRenderer::new);
                ItemProperties.register(ModItems.POINTERITEM.get(), ResourceLocation.fromNamespaceAndPath(MOD_ID, "mode"),
                        (stack, level, entity, seed) -> PointerItem.getModelMode(stack));
                ItemBlockRenderTypes.setRenderLayer(ModBlocks.BLOCK_GRUDGE.get(), RenderType.translucent());
                ItemBlockRenderTypes.setRenderLayer(ModBlocks.BLOCK_GRUDGE_XP.get(), RenderType.translucent());
                ItemBlockRenderTypes.setRenderLayer(ModBlocks.BLOCK_CRANE.get(), RenderType.cutout());
                ItemBlockRenderTypes.setRenderLayer(ModBlocks.BLOCK_WAYPOINT.get(), RenderType.translucent());
            });
        }
    }
}
