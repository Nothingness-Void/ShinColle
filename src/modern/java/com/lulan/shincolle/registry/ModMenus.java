package com.lulan.shincolle.registry;

import com.lulan.shincolle.ShinColle;
import com.lulan.shincolle.menu.CraneTerminalMenu;
import com.lulan.shincolle.menu.DeskReferenceMenu;
import com.lulan.shincolle.menu.DeskTerminalMenu;
import com.lulan.shincolle.menu.RecipePaperMenu;
import com.lulan.shincolle.menu.ShipInventoryMenu;
import com.lulan.shincolle.menu.SmallShipyardMenu;
import com.lulan.shincolle.menu.WaypointTerminalMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModMenus {

    public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(ForgeRegistries.MENU_TYPES, ShinColle.MOD_ID);

    public static final RegistryObject<MenuType<CraneTerminalMenu>> CRANE_TERMINAL = MENUS.register("crane_terminal",
            () -> IForgeMenuType.create(CraneTerminalMenu::fromNetwork));
    public static final RegistryObject<MenuType<WaypointTerminalMenu>> WAYPOINT_TERMINAL = MENUS.register("waypoint_terminal",
            () -> IForgeMenuType.create(WaypointTerminalMenu::fromNetwork));
    public static final RegistryObject<MenuType<DeskReferenceMenu>> DESK_REFERENCE = MENUS.register("desk_reference",
            () -> IForgeMenuType.create(DeskReferenceMenu::fromNetwork));
    public static final RegistryObject<MenuType<DeskTerminalMenu>> DESK_TERMINAL = MENUS.register("desk_terminal",
            () -> IForgeMenuType.create(DeskTerminalMenu::fromNetwork));
    public static final RegistryObject<MenuType<SmallShipyardMenu>> SMALL_SHIPYARD = MENUS.register("small_shipyard",
            () -> IForgeMenuType.create(SmallShipyardMenu::fromNetwork));
    public static final RegistryObject<MenuType<RecipePaperMenu>> RECIPE_PAPER = MENUS.register("recipe_paper",
            () -> IForgeMenuType.create(RecipePaperMenu::fromNetwork));
    public static final RegistryObject<MenuType<ShipInventoryMenu>> SHIP_INVENTORY = MENUS.register("ship_inventory",
            () -> IForgeMenuType.create(ShipInventoryMenu::fromNetwork));

    private ModMenus() {
    }
}
