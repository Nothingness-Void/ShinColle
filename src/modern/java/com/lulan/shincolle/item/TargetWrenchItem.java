package com.lulan.shincolle.item;

import com.lulan.shincolle.registry.ModCapabilities;
import com.lulan.shincolle.blockentity.RouteNode;
import com.lulan.shincolle.config.ShinColleConfig;
import com.lulan.shincolle.entity.ship.LegacyShipEntity;
import com.lulan.shincolle.morph.MorphHelper;
import com.lulan.shincolle.network.GameplayCommandType;
import com.lulan.shincolle.network.ModNetwork;
import com.lulan.shincolle.network.ServerboundGameplayCommandPacket;
import com.lulan.shincolle.playerskill.PlayerSkillHostResolver;
import com.lulan.shincolle.registry.ModSoundEvents;
import com.lulan.shincolle.sound.ShinColleSoundHelper;
import com.lulan.shincolle.teitoku.TeitokuHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.Component;
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
import net.neoforged.neoforge.capabilities.Capabilities;

import javax.annotation.Nullable;
import java.util.List;

public class TargetWrenchItem extends Item {

    private StoredTarget selectedTarget;
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
        if (player == null || !player.isShiftKeyDown() || !context.getLevel().isClientSide()) {
            return InteractionResult.PASS;
        }

        Level level = context.getLevel();
        BlockPos clickedPos = context.getClickedPos();
        BlockPos first = this.selectTarget(level, player, clickedPos);
        if (first != null) {
            CompoundTag payload = new CompoundTag();
            payload.put("First", NbtUtils.writeBlockPos(first));
            payload.put("Second", NbtUtils.writeBlockPos(clickedPos));
            ModNetwork.sendToServer(ServerboundGameplayCommandPacket.of(GameplayCommandType.WRENCH_PAIR_TARGETS, payload));
        }
        return InteractionResult.FAIL;
    }

    public @Nullable BlockPos selectTarget(Level level, Player player, BlockPos clickedPos) {
        TargetType clickedType = TargetType.of(level.getBlockEntity(clickedPos));

        if (clickedType == TargetType.NONE) {
            this.selectedTarget = null;
            player.displayClientMessage(Component.translatable("chat.shincolle.wrench.wrongtile"), true);
            return null;
        }

        StoredTarget storedTarget = this.selectedTarget;
        if (storedTarget == null) {
            this.selectedTarget = new StoredTarget(clickedPos.immutable(), clickedType);
            player.displayClientMessage(Component.translatable("chat.shincolle.wrench.selected",
                    clickedType.getDisplayName(), posComponent(clickedPos, ChatFormatting.AQUA)), true);
            return null;
        }

        this.selectedTarget = null;

        if (storedTarget.pos().equals(clickedPos)) {
            player.displayClientMessage(Component.translatable("chat.shincolle.wrench.samepoint"), true);
            return null;
        }
        return storedTarget.pos();
    }

    public static void pairTargets(Level level, Player player, BlockPos first, BlockPos clickedPos) {
        if (first.equals(clickedPos) || !level.hasChunkAt(first) || !level.hasChunkAt(clickedPos)) return;
        BlockEntity storedBlockEntity = level.getBlockEntity(first);
        TargetType storedType = TargetType.of(storedBlockEntity);
        TargetType clickedType = TargetType.of(level.getBlockEntity(clickedPos));
        if (storedType == TargetType.NONE) {
            player.displayClientMessage(Component.translatable("chat.shincolle.wrench.wrongtile"), true);
            return;
        }

        if (storedType == TargetType.ROUTE_NODE && clickedType == TargetType.ROUTE_NODE) {
            pairRouteNodes(level, player, first, (RouteNode) storedBlockEntity, clickedPos,
                    level.getBlockEntity(clickedPos) instanceof RouteNode routeNode ? routeNode : null);
            return;
        }

        if (storedType == TargetType.ROUTE_NODE && clickedType == TargetType.CONTAINER) {
            pairRouteNodeAndContainer(level, player, first, (RouteNode) storedBlockEntity, clickedPos);
            return;
        }

        if (storedType == TargetType.CONTAINER && clickedType == TargetType.ROUTE_NODE) {
            pairRouteNodeAndContainer(level, player, clickedPos,
                    level.getBlockEntity(clickedPos) instanceof RouteNode routeNode ? routeNode : null,
                    first);
            return;
        }

        player.displayClientMessage(Component.translatable("chat.shincolle.wrench.wrongtile"), true);
    }

    @Override
    public boolean onLeftClickEntity(ItemStack stack, Player player, Entity entity) {
        if (!(player instanceof net.minecraft.server.level.ServerPlayer serverPlayer)
                || !(entity instanceof LegacyShipEntity ship)
                || ship.isHostileVariant()
                || !PlayerSkillHostResolver.sameOwner(serverPlayer, ship)) {
            return false;
        }

        boolean unlocked = MorphHelper.unlockMorph(serverPlayer, ship.getShipClassId());
        if (unlocked) {
            TeitokuHelper.get(serverPlayer).ifPresent(data -> {
                var profile = data.findMorphProfile(ship.getShipClassId());
                profile.setAmmoLight(0);
                profile.setAmmoHeavy(0);
                profile.setGrudge(0);
                profile.setMarried(true);
            });
        }
        TeitokuHelper.syncGameplayState(serverPlayer);
        serverPlayer.displayClientMessage(Component.translatable(unlocked
                ? "chat.shincolle.morph.unlocked"
                : "chat.shincolle.morph.already_acquired", ship.getName()), true);
        return true;
    }

    @Override
    public void appendHoverText(ItemStack stack, net.minecraft.world.item.Item.TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        Level level = context.level();
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

        if (!withinDistance(fromPos, toPos, ShinColleConfig.pairDistWp())) {
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

        if (!withinDistance(routeNodePos, containerPos, ShinColleConfig.pairDistChest())) {
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

    private Component describeSelection(ItemStack stack) {
        StoredTarget storedTarget = this.selectedTarget;
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

    private record StoredTarget(BlockPos pos, TargetType type) {
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
                    || (blockEntity != null && ModCapabilities.get(blockEntity, Capabilities.FluidHandler.BLOCK).isPresent())) {
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
