package com.lulan.shincolle.item;

import com.lulan.shincolle.sound.ShipSoundType;
import com.lulan.shincolle.sound.ShinColleSoundHelper;
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
import java.util.UUID;

public class OwnerPaperItem extends Item {

    public static final String OWNER_A_NAME = "OwnerAName";
    public static final String OWNER_A_UUID = "OwnerAUuid";
    public static final String OWNER_B_NAME = "OwnerBName";
    public static final String OWNER_B_UUID = "OwnerBUuid";
    public static final String NEXT_SLOT_IS_A = "NextSlotIsA";

    public record Signer(UUID uuid, String name) {
    }

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
            ShinColleSoundHelper.playShipVoice(level, player, ShipSoundType.PICKITEM, 0.6F,
                    ShinColleSoundHelper.variedPitch(player, 1.0F, 0.08F));
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
        tooltip.add(Component.translatable("gui.shincolle.ownerpaper.use").withStyle(ChatFormatting.DARK_GRAY));
    }

    public static java.util.Optional<Signer> resolveTransferTarget(ItemStack stack, @Nullable UUID currentOwner) {
        CompoundTag tag = stack.getTag();
        if (tag == null || currentOwner == null) {
            return java.util.Optional.empty();
        }

        java.util.Optional<Signer> signerA = readSigner(tag, OWNER_A_NAME, OWNER_A_UUID);
        java.util.Optional<Signer> signerB = readSigner(tag, OWNER_B_NAME, OWNER_B_UUID);

        if (signerA.isPresent() && signerA.get().uuid().equals(currentOwner)) {
            return signerB;
        }

        if (signerB.isPresent() && signerB.get().uuid().equals(currentOwner)) {
            return signerA;
        }

        return java.util.Optional.empty();
    }

    private static void sign(CompoundTag tag, String nameKey, String uuidKey, Player player) {
        tag.putString(nameKey, player.getName().getString());
        tag.putString(uuidKey, player.getUUID().toString());
    }

    private static java.util.Optional<Signer> readSigner(CompoundTag tag, String nameKey, String uuidKey) {
        if (!tag.contains(nameKey) || !tag.contains(uuidKey)) {
            return java.util.Optional.empty();
        }

        String name = tag.getString(nameKey);
        String uuidText = tag.getString(uuidKey);
        if (name.isBlank() || uuidText.isBlank()) {
            return java.util.Optional.empty();
        }

        try {
            return java.util.Optional.of(new Signer(java.util.UUID.fromString(uuidText), name));
        } catch (IllegalArgumentException ignored) {
            return java.util.Optional.empty();
        }
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
