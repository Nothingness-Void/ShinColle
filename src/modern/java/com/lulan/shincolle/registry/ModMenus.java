package com.lulan.shincolle.registry;

import com.lulan.shincolle.ShinColle;
import com.lulan.shincolle.menu.DeskReferenceMenu;
import com.lulan.shincolle.menu.RecipePaperMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModMenus {

    public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(ForgeRegistries.MENU_TYPES, ShinColle.MOD_ID);

    public static final RegistryObject<MenuType<DeskReferenceMenu>> DESK_REFERENCE = MENUS.register("desk_reference",
            () -> IForgeMenuType.create(DeskReferenceMenu::fromNetwork));
    public static final RegistryObject<MenuType<RecipePaperMenu>> RECIPE_PAPER = MENUS.register("recipe_paper",
            () -> IForgeMenuType.create(RecipePaperMenu::fromNetwork));

    private ModMenus() {
    }
}
