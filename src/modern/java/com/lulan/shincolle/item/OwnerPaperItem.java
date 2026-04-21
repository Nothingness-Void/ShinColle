package com.lulan.shincolle.item;

import com.lulan.shincolle.teitoku.TeitokuHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class OwnerPaperItem extends Item {

    public static final String SIGN_NAME_A = "SignNameA";
    public static final String SIGN_NAME_B = "SignNameB";
    public static final String SIGN_ID_A = "SignIDA";
    public static final String SIGN_ID_B = "SignIDB";
    private static final String SIGN_POS = "signPos";

    public record Signer(int uid, UUID uuid, String name) {
    }

    public OwnerPaperItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (player.isShiftKeyDown() || !(player instanceof ServerPlayer serverPlayer)) {
            return InteractionResultHolder.pass(stack);
        }

        if (!level.isClientSide()) {
            int playerUid = TeitokuHelper.getPlayerUid(serverPlayer);
            if (playerUid <= 0) {
                return InteractionResultHolder.pass(stack);
            }

            CompoundTag tag = stack.getTag();
            if (tag == null) {
                tag = new CompoundTag();
                tag.putString(SIGN_NAME_A, player.getName().getString());
                tag.putString(SIGN_NAME_B, "");
                tag.putInt(SIGN_ID_A, playerUid);
                tag.putInt(SIGN_ID_B, -1);
                tag.putBoolean(SIGN_POS, false);
                stack.setTag(tag);
            } else if (tag.getBoolean(SIGN_POS)) {
                sign(tag, SIGN_NAME_A, SIGN_ID_A, serverPlayer, playerUid);
                tag.putBoolean(SIGN_POS, false);
            } else {
                sign(tag, SIGN_NAME_B, SIGN_ID_B, serverPlayer, playerUid);
                tag.putBoolean(SIGN_POS, true);
            }
        }

        return InteractionResultHolder.pass(stack);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        CompoundTag tag = stack.getTag();
        if (tag == null) {
            return;
        }

        tooltip.add(buildSignerLine(tag, SIGN_NAME_A, SIGN_ID_A));
        tooltip.add(buildSignerLine(tag, SIGN_NAME_B, SIGN_ID_B));
    }

    public static Optional<Signer> resolveTransferTarget(ServerLevel level, ItemStack stack, int currentOwnerUid) {
        CompoundTag tag = stack.getTag();
        if (tag == null || currentOwnerUid <= 0) {
            return Optional.empty();
        }

        int signerA = tag.getInt(SIGN_ID_A);
        int signerB = tag.getInt(SIGN_ID_B);
        if (signerA <= 0 || signerB <= 0) {
            return Optional.empty();
        }

        int targetUid;
        if (signerA == currentOwnerUid) {
            targetUid = signerB;
        } else if (signerB == currentOwnerUid) {
            targetUid = signerA;
        } else {
            return Optional.empty();
        }

        for (ServerPlayer onlinePlayer : level.getServer().getPlayerList().getPlayers()) {
            if (TeitokuHelper.getPlayerUid(onlinePlayer) == targetUid) {
                return Optional.of(new Signer(targetUid, onlinePlayer.getUUID(), onlinePlayer.getGameProfile().getName()));
            }
        }

        return Optional.empty();
    }

    private static void sign(CompoundTag tag, String nameKey, String idKey, ServerPlayer player, int uid) {
        tag.putString(nameKey, player.getGameProfile().getName());
        tag.putInt(idKey, uid);
    }

    private static Component buildSignerLine(CompoundTag tag, String nameKey, String idKey) {
        return Component.literal(String.valueOf(tag.getInt(idKey))).withStyle(ChatFormatting.RED)
                .append(Component.literal(" "))
                .append(Component.literal(tag.getString(nameKey)).withStyle(ChatFormatting.AQUA));
    }
}
