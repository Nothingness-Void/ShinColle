package com.lulan.shincolle.item;

import com.lulan.shincolle.blockentity.RouteEnergyAccess;
import com.lulan.shincolle.blockentity.RouteNode;
import com.lulan.shincolle.entity.ship.LegacyShipEntity;
import com.lulan.shincolle.morph.MorphHelper;
import com.lulan.shincolle.registry.ModSoundEvents;
import com.lulan.shincolle.sound.ShinColleSoundHelper;
import com.lulan.shincolle.teitoku.TeitokuHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.ForgeCapabilities;

import javax.annotation.Nullable;
import java.util.List;

public class TargetWrenchItem extends Item {

    private static final String TARGET_POS_TAG = "SelectedPos";
    private static final String TARGET_DIMENSION_TAG = "SelectedDimension";
    private static final String TARGET_TYPE_TAG = "SelectedType";
    private static final int WAYPOINT_PAIR_DISTANCE = 48;
    private static final int CONTAINER_PAIR_DISTANCE = 16;

    public TargetWrenchItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        return InteractionResultHolder.pass(player.getItemInHand(hand));
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Player player = context.getPlayer();
        if (player == null || !player.isShiftKeyDown()) {
            return InteractionResult.PASS;
        }

        Level level = context.getLevel();
        ItemStack stack = context.getItemInHand();
        BlockPos clickedPos = context.getClickedPos();
        TargetType clickedType = TargetType.of(level.getBlockEntity(clickedPos));

        if (clickedType == TargetType.NONE) {
            if (!level.isClientSide()) {
                clearSelection(stack);
                player.displayClientMessage(Component.translatable("chat.shincolle.wrench.wrongtile"), true);
            }

            return InteractionResult.sidedSuccess(level.isClientSide());
        }

        if (level.isClientSide()) {
            return InteractionResult.sidedSuccess(true);
        }

        StoredTarget storedTarget = getStoredTarget(stack);
        if (storedTarget == null) {
            storeTarget(stack, clickedPos, level.dimension().location(), clickedType);
            ShinColleSoundHelper.playForPlayer(level, player, ModSoundEvents.SHIP_BELL.get(), 0.55F,
                    ShinColleSoundHelper.variedPitch(player, 1.1F, 0.12F));
            player.displayClientMessage(Component.translatable("chat.shincolle.wrench.selected",
                    clickedType.getDisplayName(), posComponent(clickedPos, ChatFormatting.AQUA)), true);
            return InteractionResult.CONSUME;
        }

        clearSelection(stack);

        if (!storedTarget.dimension().equals(level.dimension().location())) {
            player.displayClientMessage(Component.translatable("chat.shincolle.wrench.cross_dimension"), true);
            return InteractionResult.CONSUME;
        }

        if (storedTarget.pos().equals(clickedPos)) {
            player.displayClientMessage(Component.translatable("chat.shincolle.wrench.samepoint"), true);
            return InteractionResult.CONSUME;
        }

        BlockEntity storedBlockEntity = level.getBlockEntity(storedTarget.pos());
        TargetType storedType = TargetType.of(storedBlockEntity);
        if (storedType == TargetType.NONE || storedType != storedTarget.type()) {
            player.displayClientMessage(Component.translatable("chat.shincolle.wrench.wrongtile"), true);
            return InteractionResult.CONSUME;
        }

        if (storedType == TargetType.ROUTE_NODE && clickedType == TargetType.ROUTE_NODE) {
            pairRouteNodes(level, player, storedTarget.pos(), (RouteNode) storedBlockEntity, clickedPos,
                    level.getBlockEntity(clickedPos) instanceof RouteNode routeNode ? routeNode : null);
            return InteractionResult.CONSUME;
        }

        if (storedType == TargetType.ROUTE_NODE && clickedType == TargetType.CONTAINER) {
            pairRouteNodeAndContainer(level, player, storedTarget.pos(), (RouteNode) storedBlockEntity, clickedPos);
            return InteractionResult.CONSUME;
        }

        if (storedType == TargetType.CONTAINER && clickedType == TargetType.ROUTE_NODE) {
            pairRouteNodeAndContainer(level, player, clickedPos,
                    level.getBlockEntity(clickedPos) instanceof RouteNode routeNode ? routeNode : null,
                    storedTarget.pos());
            return InteractionResult.CONSUME;
        }

        player.displayClientMessage(Component.translatable("chat.shincolle.wrench.wrongtile"), true);
        return InteractionResult.CONSUME;
    }

    @Override
    public boolean onLeftClickEntity(ItemStack stack, Player player, Entity entity) {
        if (!(player instanceof net.minecraft.server.level.ServerPlayer serverPlayer)
                || !(entity instanceof LegacyShipEntity ship)
                || ship.isHostileVariant()
                || !ship.canCommanderEdit(serverPlayer)) {
            return false;
        }

        boolean unlocked = MorphHelper.unlockMorph(serverPlayer, ship.getShipClassId());
        TeitokuHelper.get(serverPlayer).ifPresent(data -> data.setSelectedMorphProfile(ship.getShipClassId()));
        TeitokuHelper.syncGameplayState(serverPlayer);
        serverPlayer.displayClientMessage(Component.literal(unlocked
                ? "Unlocked morph: " + ship.getName().getString()
                : "Selected morph: " + ship.getName().getString()), true);
        return true;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("gui.shincolle.wrench.selection", describeSelection(stack)).withStyle(ChatFormatting.AQUA));
        tooltip.add(Component.translatable("gui.shincolle.wrench3").withStyle(ChatFormatting.GRAY));
    }

    private static void pairRouteNodes(Level level, Player player, BlockPos fromPos, RouteNode fromNode,
                                       BlockPos toPos, @Nullable RouteNode toNode) {
        if (toNode == null) {
            player.displayClientMessage(Component.translatable("chat.shincolle.wrench.wrongtile"), true);
            return;
        }

        if (!fromNode.canEdit(player)) {
            player.displayClientMessage(Component.translatable("chat.shincolle.wrongowner"), true);
            return;
        }

        if (!withinDistance(fromPos, toPos, WAYPOINT_PAIR_DISTANCE)) {
            player.displayClientMessage(Component.translatable("chat.shincolle.wrench.wptoofar"), true);
            return;
        }

        fromNode.setNextWaypoint(toPos);
        if (!fromPos.equals(toNode.getNextWaypoint())) {
            toNode.setLastWaypoint(fromPos);
        }

        ShinColleSoundHelper.playForPlayer(level, player, ModSoundEvents.SHIP_BELL.get(), 0.7F,
                ShinColleSoundHelper.variedPitch(player, 1.0F, 0.08F));
        player.displayClientMessage(Component.translatable("chat.shincolle.wrench.setwp")
                .append(Component.literal(" "))
                .append(posComponent(fromPos, ChatFormatting.GREEN))
                .append(Component.literal(" -> ").withStyle(ChatFormatting.GRAY))
                .append(posComponent(toPos, ChatFormatting.GOLD)), false);
    }

    private static void pairRouteNodeAndContainer(Level level, Player player, BlockPos routeNodePos, @Nullable RouteNode routeNode,
                                                  BlockPos containerPos) {
        if (routeNode == null) {
            player.displayClientMessage(Component.translatable("chat.shincolle.wrench.wrongtile"), true);
            return;
        }

        if (!routeNode.canEdit(player)) {
            player.displayClientMessage(Component.translatable("chat.shincolle.wrongowner"), true);
            return;
        }

        if (!withinDistance(routeNodePos, containerPos, CONTAINER_PAIR_DISTANCE)) {
            player.displayClientMessage(Component.translatable("chat.shincolle.wrench.toofar"), true);
            return;
        }

        routeNode.setPairedChest(containerPos);
        ShinColleSoundHelper.playForPlayer(level, player, ModSoundEvents.SHIP_BELL.get(), 0.7F,
                ShinColleSoundHelper.variedPitch(player, 1.0F, 0.08F));
        player.displayClientMessage(Component.translatable("chat.shincolle.wrench.setwp")
                .append(Component.literal(" "))
                .append(posComponent(routeNodePos, ChatFormatting.GREEN))
                .append(Component.literal(" & ").withStyle(ChatFormatting.GRAY))
                .append(posComponent(containerPos, ChatFormatting.GOLD)), false);
    }

    private static boolean withinDistance(BlockPos first, BlockPos second, int maxDistance) {
        return first.distSqr(second) <= (double) (maxDistance * maxDistance);
    }

    private static void storeTarget(ItemStack stack, BlockPos pos, ResourceLocation dimension, TargetType type) {
        CompoundTag tag = stack.getOrCreateTag();
        tag.put(TARGET_POS_TAG, NbtUtils.writeBlockPos(pos));
        tag.putString(TARGET_DIMENSION_TAG, dimension.toString());
        tag.putInt(TARGET_TYPE_TAG, type.id);
    }

    private static void clearSelection(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag == null) {
            return;
        }

        tag.remove(TARGET_POS_TAG);
        tag.remove(TARGET_DIMENSION_TAG);
        tag.remove(TARGET_TYPE_TAG);

        if (tag.isEmpty()) {
            stack.setTag(null);
        }
    }

    private static boolean hasSelection(ItemStack stack) {
        return getStoredTarget(stack) != null;
    }

    private static @Nullable StoredTarget getStoredTarget(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag == null || !tag.contains(TARGET_POS_TAG, Tag.TAG_COMPOUND) || !tag.contains(TARGET_DIMENSION_TAG, Tag.TAG_STRING)) {
            return null;
        }

        ResourceLocation dimension = ResourceLocation.tryParse(tag.getString(TARGET_DIMENSION_TAG));
        TargetType type = TargetType.fromId(tag.getInt(TARGET_TYPE_TAG));
        if (dimension == null || type == TargetType.NONE) {
            return null;
        }

        return new StoredTarget(NbtUtils.readBlockPos(tag.getCompound(TARGET_POS_TAG)), dimension, type);
    }

    private static Component describeSelection(ItemStack stack) {
        StoredTarget storedTarget = getStoredTarget(stack);
        if (storedTarget == null) {
            return Component.translatable("gui.shincolle.wrench.selection.none").withStyle(ChatFormatting.DARK_GRAY);
        }

        return Component.empty()
                .append(storedTarget.type().getDisplayName())
                .append(Component.literal(" @ ").withStyle(ChatFormatting.DARK_GRAY))
                .append(posComponent(storedTarget.pos(), ChatFormatting.GRAY));
    }

    private static Component posComponent(BlockPos pos, ChatFormatting color) {
        return Component.literal(pos.getX() + ", " + pos.getY() + ", " + pos.getZ()).withStyle(color);
    }

    private record StoredTarget(BlockPos pos, ResourceLocation dimension, TargetType type) {
    }

    private enum TargetType {
        NONE(0, "gui.shincolle.wrench.target.none"),
        ROUTE_NODE(1, "gui.shincolle.wrench.target.route"),
        CONTAINER(2, "gui.shincolle.wrench.target.chest");

        private final int id;
        private final String translationKey;

        TargetType(int id, String translationKey) {
            this.id = id;
            this.translationKey = translationKey;
        }

        public Component getDisplayName() {
            return Component.translatable(this.translationKey);
        }

        public static TargetType of(@Nullable BlockEntity blockEntity) {
            if (blockEntity instanceof RouteNode) {
                return ROUTE_NODE;
            }

            if (blockEntity instanceof Container
                    || blockEntity instanceof RouteEnergyAccess
                    || (blockEntity != null && blockEntity.getCapability(ForgeCapabilities.FLUID_HANDLER).isPresent())) {
                return CONTAINER;
            }

            return NONE;
        }

        public static TargetType fromId(int id) {
            for (TargetType type : values()) {
                if (type.id == id) {
                    return type;
                }
            }

            return NONE;
        }
    }
}
