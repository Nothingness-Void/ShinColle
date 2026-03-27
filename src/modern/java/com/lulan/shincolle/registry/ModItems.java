package com.lulan.shincolle.registry;

import com.lulan.shincolle.ShinColle;
import com.lulan.shincolle.item.CombatRationItem;
import com.lulan.shincolle.item.DeskReferenceItem;
import com.lulan.shincolle.item.KaitaiHammerItem;
import com.lulan.shincolle.item.LegacyPlaceholderItem;
import com.lulan.shincolle.item.LegacyUseFeedbackItem;
import com.lulan.shincolle.item.MarriageRingItem;
import com.lulan.shincolle.item.OpToolItem;
import com.lulan.shincolle.item.OwnerPaperItem;
import com.lulan.shincolle.item.PointerItem;
import com.lulan.shincolle.item.RecipePaperItem;
import com.lulan.shincolle.item.RepairGoddessItem;
import com.lulan.shincolle.item.ShipTankItem;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.ArrayList;
import java.util.List;

public final class ModItems {

    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, ShinColle.MOD_ID);

    public static final RegistryObject<Item> ABYSSMETAL = register("abyssmetal");
    public static final RegistryObject<Item> ABYSSMETAL1 = register("abyssmetal1");
    public static final RegistryObject<Item> ABYSSNUGGET = register("abyssnugget");
    public static final RegistryObject<Item> ABYSSNUGGET1 = register("abyssnugget1");
    public static final RegistryObject<Item> AMMO = register("ammo");
    public static final RegistryObject<Item> AMMO1 = register("ammo1");
    public static final RegistryObject<Item> AMMO2 = register("ammo2");
    public static final RegistryObject<Item> AMMO3 = register("ammo3");
    public static final RegistryObject<Item> BUCKETREPAIR = ITEMS.register("bucketrepair",
            () -> new LegacyUseFeedbackItem(new Item.Properties().stacksTo(16),
                    null,
                    "gui.shincolle.bucketrepair.pending",
                    UseAnim.DRINK,
                    24));
    public static final RegistryObject<Item> COMBATRATION = combatRation("combatration", "gui.shincolle.combatration0", 1400, 900, 1800, 4, 0.35F);
    public static final RegistryObject<Item> COMBATRATION1 = combatRation("combatration1", "gui.shincolle.combatration1", 1800, 3600, 7200, 5, 0.4F);
    public static final RegistryObject<Item> COMBATRATION2 = combatRation("combatration2", "gui.shincolle.combatration2", 1600, 1200, 2400, 6, 0.55F);
    public static final RegistryObject<Item> COMBATRATION3 = combatRation("combatration3", "gui.shincolle.combatration3", 2000, 3900, 7800, 7, 0.65F);
    public static final RegistryObject<Item> COMBATRATION4 = combatRation("combatration4", "gui.shincolle.combatration4", 3000, 100, 200, 3, 0.25F);
    public static final RegistryObject<Item> COMBATRATION5 = combatRation("combatration5", "gui.shincolle.combatration5", 4000, 900, 1800, 4, 0.3F);
    public static final RegistryObject<Item> DESKITEMBOOK = ITEMS.register("deskitembook",
            () -> new DeskReferenceItem(new Item.Properties().stacksTo(1), 1, "item.shincolle.deskitembook"));
    public static final RegistryObject<Item> DESKITEMRADAR = ITEMS.register("deskitemradar",
            () -> new DeskReferenceItem(new Item.Properties().stacksTo(1), 0, "item.shincolle.deskitemradar"));
    public static final List<RegistryObject<Item>> EQUIPAIRPLANE_ITEMS = placeholderVariants("equipairplane", 22, "gui.shincolle.placeholder_equip");
    public static final List<RegistryObject<Item>> EQUIPAMMO_ITEMS = placeholderVariants("equipammo", 9, "gui.shincolle.placeholder_equip");
    public static final List<RegistryObject<Item>> EQUIPARMOR_ITEMS = placeholderVariants("equiparmor", 7, "gui.shincolle.placeholder_equip");
    public static final List<RegistryObject<Item>> EQUIPCANNON_ITEMS = placeholderVariants("equipcannon", 16, "gui.shincolle.placeholder_equip");
    public static final List<RegistryObject<Item>> EQUIPCATAPULT_ITEMS = placeholderVariants("equipcatapult", 4, "gui.shincolle.placeholder_equip");
    public static final RegistryObject<Item> EQUIPCOMPASS = placeholderItem("equipcompass", 1, "gui.shincolle.placeholder_equip");
    public static final List<RegistryObject<Item>> EQUIPDRUM_ITEMS = placeholderVariants("equipdrum", 3, "gui.shincolle.placeholder_equip");
    public static final RegistryObject<Item> EQUIPFLARE = placeholderItem("equipflare", 1, "gui.shincolle.placeholder_equip");
    public static final List<RegistryObject<Item>> EQUIPMACHINEGUN_ITEMS = placeholderVariants("equipmachinegun", 7, "gui.shincolle.placeholder_equip");
    public static final List<RegistryObject<Item>> EQUIPRADAR_ITEMS = placeholderVariants("equipradar", 9, "gui.shincolle.placeholder_equip");
    public static final RegistryObject<Item> EQUIPSEARCHLIGHT = placeholderItem("equipsearchlight", 1, "gui.shincolle.placeholder_equip");
    public static final List<RegistryObject<Item>> EQUIPTORPEDO_ITEMS = placeholderVariants("equiptorpedo", 7, "gui.shincolle.placeholder_equip");
    public static final List<RegistryObject<Item>> EQUIPTURBINE_ITEMS = placeholderVariants("equipturbine", 5, "gui.shincolle.placeholder_equip");
    public static final RegistryObject<Item> GRUDGE = register("grudge");
    public static final RegistryObject<Item> GRUDGE1 = register("grudge1");
    public static final RegistryObject<Item> INSTANTCONMAT = register("instantconmat");
    public static final RegistryObject<Item> KAITAIHAMMER = ITEMS.register("kaitaihammer",
            () -> new KaitaiHammerItem(new Item.Properties().stacksTo(1).durability(20)));
    public static final RegistryObject<Item> MARRIAGERING = ITEMS.register("marriagering",
            () -> new MarriageRingItem(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> MODERNKIT = ITEMS.register("modernkit",
            () -> new LegacyUseFeedbackItem(new Item.Properties().stacksTo(1),
                    "gui.shincolle.modernkit",
                    "gui.shincolle.modernkit.pending",
                    UseAnim.EAT,
                    80));
    public static final RegistryObject<Item> OWNERPAPER = ITEMS.register("ownerpaper",
            () -> new OwnerPaperItem(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> OPTOOL = ITEMS.register("optool",
            () -> new OpToolItem(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> POINTERITEM = ITEMS.register("pointeritem",
            () -> new PointerItem(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> RECIPEPAPER = ITEMS.register("recipepaper",
            () -> new RecipePaperItem(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> REPAIRGODDESS = ITEMS.register("repairgoddess",
            () -> new RepairGoddessItem(new Item.Properties().stacksTo(16)));
    public static final RegistryObject<Item> SHIPTANK = shipTank("shiptank", 32000);
    public static final RegistryObject<Item> SHIPTANK1 = shipTank("shiptank1", 128000);
    public static final RegistryObject<Item> SHIPTANK2 = shipTank("shiptank2", 512000);
    public static final RegistryObject<Item> SHIPTANK3 = shipTank("shiptank3", 2048000);
    public static final List<RegistryObject<Item>> SHIPSPAWNEGG_ITEMS = placeholderNamedItems(1, "gui.shincolle.placeholder_spawn_egg",
            "smallegg", "largeegg", "shipegg2", "shipegg3", "shipegg4", "shipegg5", "shipegg11", "shipegg12", "shipegg14",
            "shipegg15", "shipegg16", "shipegg17", "shipegg18", "shipegg19", "shipegg20", "shipegg21", "shipegg22", "shipegg23",
            "shipegg28", "shipegg29", "shipegg30", "shipegg31", "shipegg32", "shipegg33", "shipegg35", "shipegg38", "shipegg2038",
            "shipegg39", "shipegg2039", "shipegg40", "shipegg2040", "shipegg41", "shipegg2041", "shipegg46", "shipegg48",
            "shipegg2048", "shipegg49", "shipegg2049", "shipegg50", "shipegg2050", "shipegg51", "shipegg53", "shipegg2053",
            "shipegg54", "shipegg2054", "shipegg55", "shipegg2055", "shipegg56", "shipegg2056", "shipegg58", "shipegg2058",
            "shipegg59", "shipegg2059", "shipegg60", "shipegg2060", "shipegg61", "shipegg2061", "shipegg62", "shipegg2062",
            "shipegg63", "shipegg2063", "shipegg64", "shipegg2064", "shipegg65", "shipegg2065", "shipegg74");
    public static final RegistryObject<Item> TARGETWRENCH = placeholderItem("targetwrench", 1, "gui.shincolle.wrench3");
    public static final RegistryObject<Item> TOYAIRPLANE = register("toyairplane");
    public static final RegistryObject<Item> TRAININGBOOK = ITEMS.register("trainingbook",
            () -> new LegacyUseFeedbackItem(new Item.Properties().stacksTo(1),
                    "gui.shincolle.trainingbook",
                    "gui.shincolle.trainingbook.pending",
                    UseAnim.EAT,
                    80));

    public static final List<RegistryObject<Item>> UTILITY_ITEMS = List.of(
            BUCKETREPAIR, DESKITEMBOOK, DESKITEMRADAR, KAITAIHAMMER, MARRIAGERING, MODERNKIT,
            OWNERPAPER, OPTOOL, POINTERITEM, TARGETWRENCH, TRAININGBOOK);

    public static final List<RegistryObject<Item>> SPECIAL_EQUIPMENT_ITEMS = List.of(
            EQUIPCOMPASS, EQUIPFLARE, EQUIPSEARCHLIGHT);

    private ModItems() {
    }

    private static RegistryObject<Item> register(String name) {
        return ITEMS.register(name, () -> new Item(new Item.Properties()));
    }

    private static RegistryObject<Item> placeholderItem(String name, int stackSize, String tooltipKey) {
        return placeholderItem(name, stackSize, tooltipKey, false);
    }

    private static RegistryObject<Item> placeholderItem(String name, int stackSize, String tooltipKey, boolean foil) {
        return ITEMS.register(name, () -> new LegacyPlaceholderItem(new Item.Properties().stacksTo(stackSize), tooltipKey, foil));
    }

    private static List<RegistryObject<Item>> placeholderVariants(String baseName, int count, String tooltipKey) {
        List<RegistryObject<Item>> items = new ArrayList<>();

        for (int i = 0; i < count; i++) {
            items.add(placeholderItem(variantName(baseName, i), 1, tooltipKey));
        }

        return List.copyOf(items);
    }

    private static List<RegistryObject<Item>> placeholderNamedItems(int stackSize, String tooltipKey, String... names) {
        List<RegistryObject<Item>> items = new ArrayList<>();

        for (String name : names) {
            items.add(placeholderItem(name, stackSize, tooltipKey));
        }

        return List.copyOf(items);
    }

    private static RegistryObject<Item> combatRation(String name, String descriptionKey, int moraleValue, int fuelMin, int fuelMax, int nutrition, float saturation) {
        return ITEMS.register(name, () -> new CombatRationItem(
                new Item.Properties().stacksTo(16).food(CombatRationItem.rationFood(nutrition, saturation)),
                descriptionKey,
                moraleValue,
                fuelMin,
                fuelMax));
    }

    private static RegistryObject<Item> shipTank(String name, int capacity) {
        return ITEMS.register(name, () -> new ShipTankItem(new Item.Properties().stacksTo(1), capacity));
    }

    private static String variantName(String baseName, int index) {
        return index == 0 ? baseName : baseName + index;
    }

    public static <T extends Block> RegistryObject<Item> registerBlockItem(String name, RegistryObject<T> block) {
        return ITEMS.register(name, () -> new BlockItem(block.get(), new Item.Properties()));
    }
}
