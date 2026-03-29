package com.lulan.shincolle.item;

import com.lulan.shincolle.entity.ship.LegacyShipEntity;
import com.lulan.shincolle.network.GameplayCommandHandler;
import com.lulan.shincolle.network.GameplayCommandType;
import com.lulan.shincolle.network.ModNetwork;
import com.lulan.shincolle.network.ServerboundGameplayCommandPacket;
import com.lulan.shincolle.registry.ModSoundEvents;
import com.lulan.shincolle.sound.ShipSoundType;
import com.lulan.shincolle.sound.ShinColleSoundHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import javax.annotation.Nullable;
import java.util.List;

public class PointerItem extends Item {

    private static final String MODE_TAG = "Mode";
    private static final int MAX_MODE = 3;

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
        int mode = getMode(stack);

        if (player.isShiftKeyDown() && player.isSprinting()) {
            if (mode == 2) {
                sendGameplayCommand(player, GameplayCommandType.CLEAR_CURRENT_TEAM, payload -> {
                });
                return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
            }

            int nextMode = (mode + 1) % (MAX_MODE + 1);
            setMode(stack, nextMode);

            if (!level.isClientSide()) {
                ShinColleSoundHelper.playForPlayer(level, player, ModSoundEvents.SHIP_BELL.get(), 0.7F,
                        ShinColleSoundHelper.variedPitch(player, 1.0F, 0.08F));
                player.displayClientMessage(Component.translatable("chat.shincolle.pointer.mode_changed", getModeName(nextMode)), true);
            }

            return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
        }

        if (player.isShiftKeyDown()) {
            sendGameplayCommand(player, mode == 2 ? GameplayCommandType.OPEN_DESK_SCREEN : GameplayCommandType.OPEN_FORMATION_SCREEN, payload -> {
                if (mode == 2) {
                    payload.putInt(GameplayCommandHandler.TAG_MODE, 1);
                }
            });
            return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
        }

        if (player.isSprinting() && mode != 2) {
            sendGameplayCommand(player, GameplayCommandType.STOP_COMMAND, payload -> {
                payload.putInt(GameplayCommandHandler.TAG_MODE, mode == 0 ? 1 : 2);
                payload.putInt(GameplayCommandHandler.TAG_SHIP_ID, -1);
            });
            return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
        }

        if (mode == 2 && player.isSprinting()) {
            sendGameplayCommand(player, GameplayCommandType.CYCLE_FORMATION, payload -> {
            });
            return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
        }

        ShinColleSoundHelper.playShipVoice(level, player, ShipSoundType.PICKITEM, 0.55F,
                ShinColleSoundHelper.variedPitch(player, 1.0F, 0.1F));

        if (level.isClientSide()) {
            switch (mode) {
                case 1 -> player.displayClientMessage(Component.translatable("chat.shincolle.pointer.command_group_hint"), true);
                case 2 -> player.displayClientMessage(Component.translatable("chat.shincolle.pointer.targetclass_hint"), true);
                case 3 -> player.displayClientMessage(Component.translatable("gui.shincolle.pointer.caress_note"), true);
                default -> player.displayClientMessage(Component.translatable("chat.shincolle.pointer.command_single_hint"), true);
            }
        }

        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Player player = context.getPlayer();
        if (player == null) {
            return InteractionResult.PASS;
        }

        ItemStack stack = context.getItemInHand();
        int mode = getMode(stack);
        if (mode == 3) {
            return InteractionResult.PASS;
        }

        if (player.isShiftKeyDown()) {
            sendGameplayCommand(player, mode == 2 ? GameplayCommandType.OPEN_DESK_SCREEN : GameplayCommandType.OPEN_FORMATION_SCREEN, payload -> {
                if (mode == 2) {
                    payload.putInt(GameplayCommandHandler.TAG_MODE, 1);
                }
            });
            return InteractionResult.sidedSuccess(player.level().isClientSide());
        }

        sendGameplayCommand(player, GameplayCommandType.MOVE_TO_POS, payload -> {
            payload.putInt(GameplayCommandHandler.TAG_MODE, mode == 0 ? 1 : 2);
            payload.putInt(GameplayCommandHandler.TAG_SHIP_ID, -1);
            payload.putInt(GameplayCommandHandler.TAG_X, context.getClickedPos().getX());
            payload.putInt(GameplayCommandHandler.TAG_Y, context.getClickedPos().getY());
            payload.putInt(GameplayCommandHandler.TAG_Z, context.getClickedPos().getZ());
        });
        return InteractionResult.sidedSuccess(player.level().isClientSide());
    }

    @Override
    public InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity interactionTarget, InteractionHand hand) {
        int mode = getMode(stack);

        if (mode == 3) {
            return InteractionResult.PASS;
        }

        if (player.isShiftKeyDown() && interactionTarget instanceof LegacyShipEntity ship && ship.canCommanderEdit(player)) {
            sendGameplayCommand(player, GameplayCommandType.OPEN_SHIP_INVENTORY,
                    payload -> payload.putInt(GameplayCommandHandler.TAG_SHIP_ID, ship.getId()));
            return InteractionResult.CONSUME;
        }

        if (mode == 2 && player.isShiftKeyDown()) {
            net.minecraft.resources.ResourceLocation targetKey =
                    net.minecraft.core.registries.BuiltInRegistries.ENTITY_TYPE.getKey(interactionTarget.getType());
            String targetClass = targetKey == null ? "" : targetKey.toString();
            if (targetClass.isBlank()) {
                player.displayClientMessage(Component.translatable("chat.shincolle.pointer.targetclass_invalid"), true);
                return InteractionResult.CONSUME;
            }

            sendGameplayCommand(player, GameplayCommandType.TOGGLE_TARGET_CLASS,
                    payload -> payload.putString(GameplayCommandHandler.TAG_TARGET_CLASS, targetClass));
            return InteractionResult.CONSUME;
        }

        if (mode <= 1
                && !player.isSprinting()
                && interactionTarget instanceof LegacyShipEntity ship
                && ship.canCommanderEdit(player)) {
            GameplayCommandType commandType = mode == 0
                    ? GameplayCommandType.TOGGLE_SIT_SINGLE
                    : GameplayCommandType.TOGGLE_SIT_GROUP;
            sendGameplayCommand(player, commandType, payload -> payload.putInt(GameplayCommandHandler.TAG_SHIP_ID, ship.getId()));
            return InteractionResult.CONSUME;
        }

        sendGameplayCommand(player,
                player.isSprinting() ? GameplayCommandType.GUARD_ENTITY : GameplayCommandType.ATTACK_ENTITY,
                payload -> {
                    payload.putInt(GameplayCommandHandler.TAG_MODE, mode == 0 ? 1 : 2);
                    payload.putInt(GameplayCommandHandler.TAG_SHIP_ID, -1);
                    payload.putInt(GameplayCommandHandler.TAG_TARGET_ID, interactionTarget.getId());
                });
        return InteractionResult.CONSUME;
    }

    private static void sendGameplayCommand(Player player, GameplayCommandType commandType, java.util.function.Consumer<CompoundTag> payloadBuilder) {
        CompoundTag payload = new CompoundTag();
        payloadBuilder.accept(payload);

        if (player.level().isClientSide()) {
            ModNetwork.sendToServer(ServerboundGameplayCommandPacket.of(commandType, payload));
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
        return getMode(stack);
    }

    public static int getMode(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag == null) {
            return 0;
        }

        int mode = tag.getInt(MODE_TAG);
        return mode >= 0 && mode <= MAX_MODE ? mode : 0;
    }

    private static void setMode(ItemStack stack, int mode) {
        stack.getOrCreateTag().putInt(MODE_TAG, mode);
    }

    private static Component getModeName(int mode) {
        return switch (mode) {
            case 1 -> Component.translatable("gui.shincolle.pointer1");
            case 2 -> Component.translatable("gui.shincolle.pointer2");
            case 3 -> Component.translatable("gui.shincolle.pointer.caress");
            default -> Component.translatable("gui.shincolle.pointer0");
        };
    }
}
