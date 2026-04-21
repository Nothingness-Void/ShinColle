package com.lulan.shincolle.item;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.List;

public class OwnerPaperItem extends Item {

    private static final String OWNER_A_NAME = "OwnerAName";
    private static final String OWNER_A_UUID = "OwnerAUuid";
    private static final String OWNER_B_NAME = "OwnerBName";
    private static final String OWNER_B_UUID = "OwnerBUuid";
    private static final String NEXT_SLOT_IS_A = "NextSlotIsA";

    public OwnerPaperItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (player.isShiftKeyDown()) {
            return InteractionResultHolder.pass(stack);
        }

        if (!level.isClientSide()) {
            CompoundTag tag = stack.getOrCreateTag();
            boolean nextSlotIsA = !tag.contains(NEXT_SLOT_IS_A) || tag.getBoolean(NEXT_SLOT_IS_A);

            if (nextSlotIsA) {
                sign(tag, OWNER_A_NAME, OWNER_A_UUID, player);
            } else {
                sign(tag, OWNER_B_NAME, OWNER_B_UUID, player);
            }

            tag.putBoolean(NEXT_SLOT_IS_A, !nextSlotIsA);
            player.displayClientMessage(Component.translatable("chat.shincolle.ownerpaper.signed",
                    Component.translatable(nextSlotIsA ? "gui.shincolle.ownerpaper.slot_a" : "gui.shincolle.ownerpaper.slot_b")), true);
        }

        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        CompoundTag tag = stack.getTag();
        tooltip.add(buildSignerLine(tag, "gui.shincolle.ownerpaper.slot_a", OWNER_A_NAME, OWNER_A_UUID));
        tooltip.add(buildSignerLine(tag, "gui.shincolle.ownerpaper.slot_b", OWNER_B_NAME, OWNER_B_UUID));

        boolean nextSlotIsA = tag == null || !tag.contains(NEXT_SLOT_IS_A) || tag.getBoolean(NEXT_SLOT_IS_A);
        tooltip.add(Component.translatable("gui.shincolle.ownerpaper.next",
                Component.translatable(nextSlotIsA ? "gui.shincolle.ownerpaper.slot_a" : "gui.shincolle.ownerpaper.slot_b")).withStyle(ChatFormatting.GRAY));
    }

    private static void sign(CompoundTag tag, String nameKey, String uuidKey, Player player) {
        tag.putString(nameKey, player.getName().getString());
        tag.putString(uuidKey, player.getUUID().toString());
    }

    private static Component buildSignerLine(@Nullable CompoundTag tag, String slotKey, String nameKey, String uuidKey) {
        if (tag == null || !tag.contains(nameKey)) {
            return Component.translatable(slotKey)
                    .append(Component.literal(": ").withStyle(ChatFormatting.DARK_GRAY))
                    .append(Component.translatable("gui.shincolle.ownerpaper.empty").withStyle(ChatFormatting.DARK_GRAY));
        }

        String name = tag.getString(nameKey);
        String uuid = abbreviateUuid(tag.getString(uuidKey));

        return Component.translatable(slotKey)
                .append(Component.literal(": ").withStyle(ChatFormatting.DARK_GRAY))
                .append(Component.literal(name).withStyle(ChatFormatting.AQUA))
                .append(Component.literal(" [" + uuid + "]").withStyle(ChatFormatting.RED));
    }

    private static String abbreviateUuid(String uuid) {
        return uuid.length() <= 8 ? uuid : uuid.substring(0, 8);
    }
}
