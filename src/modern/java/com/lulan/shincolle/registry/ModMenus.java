package com.lulan.shincolle.registry;

import com.lulan.shincolle.ShinColle;
import com.lulan.shincolle.menu.CraneTerminalMenu;
import com.lulan.shincolle.menu.DeskReferenceMenu;
import com.lulan.shincolle.menu.DeskTerminalMenu;
import com.lulan.shincolle.menu.FormationMenu;
import com.lulan.shincolle.menu.LargeShipyardMenu;
import com.lulan.shincolle.menu.LegacyCoreMenu;
import com.lulan.shincolle.menu.MorphInventoryMenu;
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
    public static final RegistryObject<MenuType<FormationMenu>> FORMATION = MENUS.register("formation",
            () -> IForgeMenuType.create(FormationMenu::fromNetwork));
    public static final RegistryObject<MenuType<LargeShipyardMenu>> LARGE_SHIPYARD = MENUS.register("large_shipyard",
            () -> IForgeMenuType.create(LargeShipyardMenu::fromNetwork));
    public static final RegistryObject<MenuType<LegacyCoreMenu>> LEGACY_CORE = MENUS.register("legacy_core",
            () -> IForgeMenuType.create(LegacyCoreMenu::fromNetwork));
    public static final RegistryObject<MenuType<MorphInventoryMenu>> MORPH_INVENTORY = MENUS.register("morph_inventory",
            () -> IForgeMenuType.create(MorphInventoryMenu::fromNetwork));
    public static final RegistryObject<MenuType<SmallShipyardMenu>> SMALL_SHIPYARD = MENUS.register("small_shipyard",
            () -> IForgeMenuType.create(SmallShipyardMenu::fromNetwork));
    public static final RegistryObject<MenuType<RecipePaperMenu>> RECIPE_PAPER = MENUS.register("recipe_paper",
            () -> IForgeMenuType.create(RecipePaperMenu::fromNetwork));
    public static final RegistryObject<MenuType<ShipInventoryMenu>> SHIP_INVENTORY = MENUS.register("ship_inventory",
            () -> IForgeMenuType.create(ShipInventoryMenu::fromNetwork));

    private ModMenus() {
    }
}
