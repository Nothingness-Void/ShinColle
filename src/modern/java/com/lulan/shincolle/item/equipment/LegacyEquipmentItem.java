package com.lulan.shincolle.item.equipment;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Locale;

public class LegacyEquipmentItem extends Item {

    private static final String[] STAT_KEYS = {
            "gui.shincolle.hp",
            "gui.shincolle.firepower1",
            "gui.shincolle.torpedo",
            "gui.shincolle.airfirepower",
            "gui.shincolle.airtorpedo",
            "gui.shincolle.armor",
            "gui.shincolle.attackspeed",
            "gui.shincolle.movespeed",
            "gui.shincolle.range",
            "gui.shincolle.critical",
            "gui.shincolle.doublehit",
            "gui.shincolle.triplehit",
            "gui.shincolle.missreduce",
            "gui.shincolle.antiair",
            "gui.shincolle.antiss",
            "gui.shincolle.dodge",
            "gui.shincolle.equip.xp",
            "gui.shincolle.equip.grudge",
            "gui.shincolle.equip.ammo",
            "gui.shincolle.equip.hpres",
            "gui.shincolle.equip.kb"
    };
    private static final ChatFormatting[] STAT_COLORS = {
            ChatFormatting.RED,
            ChatFormatting.RED,
            ChatFormatting.GREEN,
            ChatFormatting.RED,
            ChatFormatting.GREEN,
            ChatFormatting.WHITE,
            ChatFormatting.WHITE,
            ChatFormatting.GRAY,
            ChatFormatting.LIGHT_PURPLE,
            ChatFormatting.AQUA,
            ChatFormatting.YELLOW,
            ChatFormatting.GOLD,
            ChatFormatting.RED,
            ChatFormatting.YELLOW,
            ChatFormatting.AQUA,
            ChatFormatting.GOLD,
            ChatFormatting.GREEN,
            ChatFormatting.DARK_PURPLE,
            ChatFormatting.DARK_AQUA,
            ChatFormatting.DARK_GREEN,
            ChatFormatting.DARK_RED
    };

    private final LegacyEquipmentFamily family;
    private final int variantIndex;

    public LegacyEquipmentItem(Properties properties, LegacyEquipmentFamily family, int variantIndex) {
        super(properties);
        this.family = family;
        this.variantIndex = variantIndex;
    }

    public LegacyEquipmentFamily family() {
        return this.family;
    }

    public int variantIndex() {
        return this.variantIndex;
    }

    public String definitionKey() {
        return this.family.definitionKey(this.variantIndex);
    }

    public @Nullable float[] getMainStats() {
        return LegacyEquipmentStatsRepository.getMain(this.definitionKey());
    }

    public @Nullable LegacyEquipmentStatsRepository.MiscData getMiscData() {
        return LegacyEquipmentStatsRepository.getMisc(this.definitionKey());
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.addAll(this.family.extraTooltipLines(this.variantIndex, stack));

        float[] stats = this.getMainStats();
        LegacyEquipmentStatsRepository.MiscData misc = this.getMiscData();

        if (stats != null) {
            appendStats(tooltip, stats);
        }

        if (misc != null) {
            appendBuildInfo(tooltip, misc);
        }
    }

    private static void appendStats(List<Component> tooltip, float[] stats) {
        for (int index = 0; index < Math.min(stats.length, STAT_KEYS.length); index++) {
            float value = stats[index];
            if (value == 0F) {
                continue;
            }

            tooltip.add(statComponent(index, value).withStyle(STAT_COLORS[index]));
        }
    }

    private static MutableComponent statComponent(int index, float value) {
        if (index == 16 || index == 17 || index == 18 || index == 19 || index == 20) {
            return Component.translatable(STAT_KEYS[index]).append(Component.literal(" " + formatPercent(value)));
        }

        String formattedValue = switch (index) {
            case 5 -> formatPercent(value);
            case 6, 7 -> format(value, 2);
            case 9, 10, 11, 12, 15 -> formatPercent(value);
            default -> format(value, 1);
        };

        return Component.literal(formattedValue + " ").append(Component.translatable(STAT_KEYS[index]));
    }

    private static void appendBuildInfo(List<Component> tooltip, LegacyEquipmentStatsRepository.MiscData misc) {
        MutableComponent enchantType = Component.translatable("gui.shincolle.equip.enchtype").append(Component.literal(" "));
        enchantType.append(enchantTypeLabel(misc.enchantType()));

        if (misc.notForCarrier()) {
            enchantType.append(Component.literal("  "));
            enchantType.append(Component.translatable("gui.shincolle.notforcarrier").withStyle(ChatFormatting.DARK_RED));
        } else if (misc.carrierOnly()) {
            enchantType.append(Component.literal("  "));
            enchantType.append(Component.translatable("gui.shincolle.carrieronly").withStyle(ChatFormatting.DARK_AQUA));
        }

        tooltip.add(enchantType);
        tooltip.add(Component.translatable(misc.developNum() > 400
                ? "gui.shincolle.shipyard.large"
                : "gui.shincolle.shipyard.small").withStyle(ChatFormatting.DARK_RED));

        MutableComponent materialLine = Component.translatable("gui.shincolle.equip.matstype").withStyle(ChatFormatting.DARK_PURPLE)
                .append(Component.literal(" (").withStyle(ChatFormatting.GRAY))
                .append(Component.translatable(materialKey(misc.developMaterial())).withStyle(ChatFormatting.GRAY))
                .append(Component.literal(") " + misc.developNum()).withStyle(ChatFormatting.GRAY))
                .append(Component.literal("  ").withStyle(ChatFormatting.GRAY))
                .append(Component.translatable("gui.shincolle.equip.matsrarelevel").withStyle(ChatFormatting.DARK_PURPLE))
                .append(Component.literal(" " + misc.rareMean()).withStyle(ChatFormatting.GRAY));
        tooltip.add(materialLine);
    }

    private static Component enchantTypeLabel(int enchantType) {
        return switch (enchantType) {
            case 1 -> Component.translatable("gui.shincolle.equip.enchtype1").withStyle(ChatFormatting.RED);
            case 2 -> Component.translatable("gui.shincolle.equip.enchtype0").withStyle(ChatFormatting.AQUA);
            case 3 -> Component.translatable("gui.shincolle.equip.enchtype2").withStyle(ChatFormatting.GRAY);
            default -> Component.translatable("gui.shincolle.equip.enchtype2").withStyle(ChatFormatting.GRAY);
        };
    }

    private static String materialKey(int developMaterial) {
        return switch (developMaterial) {
            case 1 -> "item.shincolle.abyssmetal";
            case 2 -> "item.shincolle.ammo";
            case 3 -> "item.shincolle.abyssmetal1";
            default -> "item.shincolle.grudge";
        };
    }

    private static String format(float value, int decimals) {
        return String.format(Locale.ROOT, "%." + decimals + "f", value);
    }

    private static String formatPercent(float value) {
        return String.format(Locale.ROOT, "%.0f%%", value * 100F);
    }
}
