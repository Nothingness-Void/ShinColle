package com.lulan.shincolle.registry;

import com.lulan.shincolle.ShinColle;
import com.lulan.shincolle.menu.CraneTerminalMenu;
import com.lulan.shincolle.menu.DeskMenu;
import com.lulan.shincolle.menu.FormationMenu;
import com.lulan.shincolle.menu.LargeShipyardMenu;
import com.lulan.shincolle.menu.LegacyCoreMenu;
import com.lulan.shincolle.menu.MorphInventoryMenu;
import com.lulan.shincolle.menu.RecipePaperMenu;
import com.lulan.shincolle.menu.ShipInventoryMenu;
import com.lulan.shincolle.menu.SmallShipyardMenu;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.neoforged.neoforge.registries.DeferredHolder;

public final class ModMenus {

    public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(BuiltInRegistries.MENU, ShinColle.MOD_ID);

    public static final DeferredHolder<MenuType<?>, MenuType<CraneTerminalMenu>> CRANE_TERMINAL = MENUS.register("crane_terminal",
            () -> IMenuTypeExtension.create(CraneTerminalMenu::fromNetwork));
    public static final DeferredHolder<MenuType<?>, MenuType<DeskMenu>> DESK = MENUS.register("desk",
            () -> IMenuTypeExtension.create(DeskMenu::fromNetwork));
    public static final DeferredHolder<MenuType<?>, MenuType<FormationMenu>> FORMATION = MENUS.register("formation",
            () -> IMenuTypeExtension.create(FormationMenu::fromNetwork));
    public static final DeferredHolder<MenuType<?>, MenuType<LargeShipyardMenu>> LARGE_SHIPYARD = MENUS.register("large_shipyard",
            () -> IMenuTypeExtension.create(LargeShipyardMenu::fromNetwork));
    public static final DeferredHolder<MenuType<?>, MenuType<LegacyCoreMenu>> LEGACY_CORE = MENUS.register("legacy_core",
            () -> IMenuTypeExtension.create(LegacyCoreMenu::fromNetwork));
    public static final DeferredHolder<MenuType<?>, MenuType<MorphInventoryMenu>> MORPH_INVENTORY = MENUS.register("morph_inventory",
            () -> IMenuTypeExtension.create(MorphInventoryMenu::fromNetwork));
    public static final DeferredHolder<MenuType<?>, MenuType<SmallShipyardMenu>> SMALL_SHIPYARD = MENUS.register("small_shipyard",
            () -> IMenuTypeExtension.create(SmallShipyardMenu::fromNetwork));
    public static final DeferredHolder<MenuType<?>, MenuType<RecipePaperMenu>> RECIPE_PAPER = MENUS.register("recipe_paper",
            () -> IMenuTypeExtension.create(RecipePaperMenu::fromNetwork));
    public static final DeferredHolder<MenuType<?>, MenuType<ShipInventoryMenu>> SHIP_INVENTORY = MENUS.register("ship_inventory",
            () -> IMenuTypeExtension.create(ShipInventoryMenu::fromNetwork));

    private ModMenus() {
    }
}
