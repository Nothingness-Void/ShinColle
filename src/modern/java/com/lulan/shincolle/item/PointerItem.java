package com.lulan.shincolle.item;

import com.lulan.shincolle.entity.ship.LegacyShipEntity;
import com.lulan.shincolle.network.GameplayCommandHandler;
import com.lulan.shincolle.network.GameplayCommandType;
import com.lulan.shincolle.network.ModNetwork;
import com.lulan.shincolle.network.ServerboundGameplayCommandPacket;
import com.lulan.shincolle.network.ServerboundShipCommandPacket;
import com.lulan.shincolle.registry.ModItems;
import com.lulan.shincolle.teitoku.TeitokuHelper;
import com.lulan.shincolle.team.TeamData;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.List;

public class PointerItem extends Item {

    private static final String MODE_TAG = "Mode";
    private static final int MAX_MODE = 5;

    public PointerItem(Properties properties) {
        super(properties);
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return true;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (isCaressMode(getMode(stack))) {
            return InteractionResultHolder.pass(stack);
        }

        if (player.isShiftKeyDown()) {
            sendGameplayCommand(player, GameplayCommandType.OPEN_FORMATION_SCREEN, payload -> {
            });
            return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
        }

        return InteractionResultHolder.pass(stack);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Player player = context.getPlayer();
        if (player == null) {
            return InteractionResult.PASS;
        }

        int mode = getMode(context.getItemInHand());
        if (isCaressMode(mode)) {
            return InteractionResult.PASS;
        }

        if (player.isShiftKeyDown()) {
            sendGameplayCommand(player, GameplayCommandType.OPEN_FORMATION_SCREEN, payload -> {
            });
            return InteractionResult.sidedSuccess(player.level().isClientSide());
        }

        sendShipCommand(player, ServerboundShipCommandPacket.moveTo(
                getCommandMode(mode),
                ServerboundShipCommandPacket.NO_ENTITY,
                ServerboundShipCommandPacket.NO_UID,
                context.getClickedPos()));
        return InteractionResult.sidedSuccess(player.level().isClientSide());
    }

    @Override
    public InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity interactionTarget, InteractionHand hand) {
        int mode = getMode(stack);
        if (isCaressMode(mode)) {
            return InteractionResult.PASS;
        }

        int commandMode = getCommandMode(mode);

        if (player.isSprinting()) {
            sendShipCommand(player, ServerboundShipCommandPacket.guard(
                    commandMode,
                    ServerboundShipCommandPacket.NO_ENTITY,
                    ServerboundShipCommandPacket.NO_UID,
                    interactionTarget.getId()));
            return InteractionResult.CONSUME;
        }

        if (player.isShiftKeyDown() && interactionTarget instanceof LegacyShipEntity ship && ship.canCommanderEdit(player)) {
            sendShipCommand(player, ServerboundShipCommandPacket.openInventory(ship.getId(), ship.getShipUid()));
            return InteractionResult.CONSUME;
        }

        if (interactionTarget instanceof LegacyShipEntity ship && ship.canCommanderEdit(player)) {
            sendShipCommand(player, ServerboundShipCommandPacket.toggleSit(
                    commandMode,
                    ship.getId(),
                    ship.getShipUid()));
            return InteractionResult.CONSUME;
        }

        if (shouldMoveToEntity(player, interactionTarget)) {
            sendShipCommand(player, ServerboundShipCommandPacket.moveTo(
                    commandMode,
                    ServerboundShipCommandPacket.NO_ENTITY,
                    ServerboundShipCommandPacket.NO_UID,
                    interactionTarget.blockPosition()));
            return InteractionResult.CONSUME;
        }

        if (interactionTarget.isInvisible()) {
            return InteractionResult.PASS;
        }

        sendShipCommand(player, ServerboundShipCommandPacket.attack(
                commandMode,
                ServerboundShipCommandPacket.NO_ENTITY,
                ServerboundShipCommandPacket.NO_UID,
                interactionTarget.getId()));
        return InteractionResult.CONSUME;
    }

    private static boolean shouldMoveToEntity(Player player, LivingEntity target) {
        if (target.isInvisible()) {
            return true;
        }

        int playerUid = TeitokuHelper.getPlayerUid(player);
        if (target instanceof LegacyShipEntity ship) {
            return ship.canCommanderEdit(player) || isFriendlyTarget(playerUid, ship.getOwnerUid());
        }

        if (target instanceof Player otherPlayer) {
            return otherPlayer == player || isFriendlyTarget(playerUid, TeitokuHelper.getPlayerUid(otherPlayer));
        }

        return false;
    }

    private static boolean isFriendlyTarget(int sourceUid, int targetUid) {
        if (sourceUid <= 0 || targetUid <= 0) {
            return false;
        }
        if (sourceUid == targetUid) {
            return true;
        }

        TeamData teamData = TeitokuHelper.getClientTeamData().get(sourceUid);
        return teamData != null && teamData.isAlly(targetUid);
    }

    private static void sendGameplayCommand(Player player, GameplayCommandType commandType,
                                            java.util.function.Consumer<CompoundTag> payloadBuilder) {
        CompoundTag payload = new CompoundTag();
        payloadBuilder.accept(payload);

        if (player.level().isClientSide()) {
            ModNetwork.sendToServer(ServerboundGameplayCommandPacket.of(commandType, payload));
        }
    }

    private static void sendShipCommand(Player player, ServerboundShipCommandPacket packet) {
        if (player.level().isClientSide()) {
            ModNetwork.sendToServer(packet);
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("gui.shincolle.pointer.current_mode", getModeName(getMode(stack))).withStyle(ChatFormatting.AQUA));
        tooltip.add(Component.translatable("gui.shincolle.pointer3").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("gui.shincolle.pointer.targetclass_note").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("gui.shincolle.pointer.caress_note").withStyle(ChatFormatting.DARK_GRAY));
    }

    public static float getModelMode(ItemStack stack) {
        int mode = getMode(stack);
        return isCaressMode(mode) ? 3.0F : getCommandMode(mode);
    }

    public static int getMode(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag == null) {
            return 0;
        }

        int mode = tag.getInt(MODE_TAG);
        return mode >= 0 && mode <= MAX_MODE ? mode : 0;
    }

    public static int getCommandMode(int mode) {
        int commandMode = mode % 3;
        return commandMode < 0 ? commandMode + 3 : commandMode;
    }

    public static boolean isCaressMode(int mode) {
        return mode > 2;
    }

    public static int cycleCommandMode(int mode) {
        return switch (mode) {
            case 1, 4 -> 2;
            case 2, 5 -> 0;
            default -> 1;
        };
    }

    public static int toggleCaressMode(int mode) {
        return switch (mode) {
            case 1, 2 -> mode + 3;
            case 3, 4, 5 -> mode - 3;
            default -> 3;
        };
    }

    public static void setMode(ItemStack stack, int mode) {
        stack.getOrCreateTag().putInt(MODE_TAG, Math.max(0, Math.min(MAX_MODE, mode)));
    }

    public static Component getModeName(int mode) {
        if (isCaressMode(mode)) {
            return Component.translatable("gui.shincolle.pointer.caress");
        }

        return switch (getCommandMode(mode)) {
            case 1 -> Component.translatable("gui.shincolle.pointer1");
            case 2 -> Component.translatable("gui.shincolle.pointer2");
            default -> Component.translatable("gui.shincolle.pointer0");
        };
    }
}
