package com.lulan.shincolle.registry;

import com.lulan.shincolle.ShinColle;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;

import java.util.List;

public final class ModCreativeModeTabs {

    public static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, ShinColle.MOD_ID);

    public static final RegistryObject<CreativeModeTab> SHINCOLLE = TABS.register("shincolle",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.shincolle"))
                    .icon(() -> new ItemStack(ModItems.GRUDGE.get()))
                    .displayItems((parameters, output) -> {
                        output.accept(ModItems.GRUDGE.get());
                        output.accept(ModItems.GRUDGE1.get());
                        output.accept(ModItems.AMMO.get());
                        output.accept(ModItems.AMMO1.get());
                        output.accept(ModItems.AMMO2.get());
                        output.accept(ModItems.AMMO3.get());
                        output.accept(ModItems.COMBATRATION.get());
                        output.accept(ModItems.COMBATRATION1.get());
                        output.accept(ModItems.COMBATRATION2.get());
                        output.accept(ModItems.COMBATRATION3.get());
                        output.accept(ModItems.COMBATRATION4.get());
                        output.accept(ModItems.COMBATRATION5.get());
                        output.accept(ModItems.ABYSSMETAL.get());
                        output.accept(ModItems.ABYSSMETAL1.get());
                        output.accept(ModItems.ABYSSNUGGET.get());
                        output.accept(ModItems.ABYSSNUGGET1.get());
                        output.accept(ModItems.INSTANTCONMAT.get());
                        output.accept(ModItems.RECIPEPAPER.get());
                        output.accept(ModItems.REPAIRGODDESS.get());
                        output.accept(ModItems.SHIPTANK.get());
                        output.accept(ModItems.SHIPTANK1.get());
                        output.accept(ModItems.SHIPTANK2.get());
                        output.accept(ModItems.SHIPTANK3.get());
                        output.accept(ModItems.TOYAIRPLANE.get());
                        acceptAll(output, ModItems.UTILITY_ITEMS);
                        acceptAll(output, ModItems.EQUIPAMMO_ITEMS);
                        acceptAll(output, ModItems.EQUIPAIRPLANE_ITEMS);
                        acceptAll(output, ModItems.EQUIPARMOR_ITEMS);
                        acceptAll(output, ModItems.EQUIPCANNON_ITEMS);
                        acceptAll(output, ModItems.EQUIPCATAPULT_ITEMS);
                        acceptAll(output, ModItems.EQUIPDRUM_ITEMS);
                        acceptAll(output, ModItems.EQUIPMACHINEGUN_ITEMS);
                        acceptAll(output, ModItems.EQUIPRADAR_ITEMS);
                        acceptAll(output, ModItems.EQUIPTORPEDO_ITEMS);
                        acceptAll(output, ModItems.EQUIPTURBINE_ITEMS);
                        acceptAll(output, ModItems.SPECIAL_EQUIPMENT_ITEMS);
                        acceptAll(output, ModItems.SHIPSPAWNEGG_ITEMS);
                        output.accept(ModBlocks.BLOCK_ABYSSIUM.get());
                        output.accept(ModBlocks.BLOCK_GRUDGE.get());
                        output.accept(ModBlocks.BLOCK_GRUDGE_XP.get());
                        output.accept(ModBlocks.BLOCK_GRUDGE_HEAVY.get());
                        output.accept(ModBlocks.BLOCK_GRUDGE_HEAVY_DECO.get());
                        output.accept(ModBlocks.BLOCK_CRANE.get());
                        output.accept(ModBlocks.BLOCK_DESK.get());
                        output.accept(ModBlocks.BLOCK_FRAME.get());
                        output.accept(ModBlocks.BLOCK_POLYMETAL.get());
                        output.accept(ModBlocks.BLOCK_POLYMETAL_ORE.get());
                        output.accept(ModBlocks.BLOCK_POLYMETAL_GRAVEL.get());
                        output.accept(ModBlocks.BLOCK_SMALL_SHIPYARD.get());
                        output.accept(ModBlocks.BLOCK_VOL_BLOCK.get());
                        output.accept(ModBlocks.BLOCK_VOL_CORE.get());
                        output.accept(ModBlocks.BLOCK_WAYPOINT.get());
                    })
                    .build());

    private ModCreativeModeTabs() {
    }

    private static void acceptAll(CreativeModeTab.Output output, List<RegistryObject<Item>> items) {
        for (RegistryObject<Item> item : items) {
            output.accept(item.get());
        }
    }
}
