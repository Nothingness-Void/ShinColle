package com.lulan.shincolle.item;

import com.lulan.shincolle.client.ClientShipVisibilityHelper;
import com.lulan.shincolle.entity.ship.LegacyShipBehaviorCatalog;
import com.lulan.shincolle.entity.ship.LegacyShipEntity;
import com.lulan.shincolle.sound.ShipSoundType;
import com.lulan.shincolle.sound.ShinColleSoundHelper;
import com.lulan.shincolle.teitoku.TeitokuData;
import com.lulan.shincolle.teitoku.TeitokuHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.fml.loading.FMLEnvironment;

import javax.annotation.Nullable;
import java.util.List;

public class MarriageRingItem extends Item {

    private static final String ACTIVE_TAG = "isActive";

    public MarriageRingItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (!level.isClientSide()) {
            boolean active = !isActive(stack);
            stack.getOrCreateTag().putBoolean(ACTIVE_TAG, active);
            if (player instanceof ServerPlayer serverPlayer) {
                TeitokuHelper.markRingState(serverPlayer, active);

                if (!active && !serverPlayer.getAbilities().instabuild) {
                    TeitokuHelper.get(serverPlayer).ifPresent(data -> {
                        if (data.isRingFlying()) {
                            serverPlayer.getAbilities().flying = false;
                            serverPlayer.getAbilities().mayfly = false;
                            data.setRingFlying(false);
                            serverPlayer.onUpdateAbilities();
                        }
                    });
                }
            }

            ShinColleSoundHelper.playShipVoice(level, player, active ? ShipSoundType.MARRY : ShipSoundType.PICKITEM,
                    active ? 0.8F : 0.55F,
                    ShinColleSoundHelper.variedPitch(player, 1.0F, 0.08F));
            player.displayClientMessage(Component.translatable(active
                    ? "chat.shincolle.marriagering.enabled"
                    : "chat.shincolle.marriagering.disabled"), true);
        }

        return InteractionResultHolder.pass(stack);
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return isActive(stack);
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean selected) {
        if (!(entity instanceof Player player)) {
            return;
        }

        // Passive water breathing applies whenever the ring is in inventory, even when inactive.
        TeitokuData data = TeitokuHelper.get(player).resolve().orElse(null);
        if (data != null) {
            int[] ringAbility = TeitokuHelper.getEffectiveRingAbility(level);
            int breatheThreshold = ringAbility.length > 0 ? ringAbility[0] : 0;
            if (breatheThreshold >= 0
                    && (player.tickCount & 127) == 0
                    && data.getMarriageNum() >= breatheThreshold
                    && player.getAirSupply() < player.getMaxAirSupply()) {
                player.setAirSupply(player.getMaxAirSupply());
            }
        }

        if (isActive(stack)) {
            tickActiveRingEffects(player, stack);
        }

        if (!level.isClientSide()
                && (player.tickCount & 63) == 0
                && (selected || player.getMainHandItem() == stack || player.getOffhandItem() == stack)
                && level instanceof ServerLevel serverLevel) {
            for (LegacyShipEntity ship : level.getEntitiesOfClass(
                    LegacyShipEntity.class, player.getBoundingBox().inflate(6.0D, 5.0D, 6.0D))) {
                if (ship.isAlive()
                        && !ship.hasNoFuel()
                        && LegacyShipBehaviorCatalog.hasLegacySameOwner(ship, player)) {
                    ship.setEmotion(2);
                    if (ship.getRandom().nextInt(5) == 0) {
                        serverLevel.sendParticles(ParticleTypes.HEART,
                                ship.getX(), ship.getY() + ship.getBbHeight() * 0.9D, ship.getZ(),
                                1, 0.25D, 0.2D, 0.25D, 0.02D);
                    }
                }
            }
        }
    }

    public static void tickActiveRingEffects(Player player, ItemStack stack) {
        if (player == null || stack == null || stack.isEmpty() || !isActive(stack)) {
            return;
        }

        TeitokuData data = TeitokuHelper.get(player).resolve().orElse(null);
        if (data == null) {
            return;
        }

        int[] ringAbility = TeitokuHelper.getEffectiveRingAbility(player.level());
        int breatheThreshold = ringAbility.length > 0 ? ringAbility[0] : 0;
        if (breatheThreshold >= 0
                && (player.tickCount & 127) == 0
                && data.getMarriageNum() >= breatheThreshold
                && player.getAirSupply() < player.getMaxAirSupply()) {
            player.setAirSupply(player.getMaxAirSupply());
        }

        int flyThreshold = ringAbility.length > 1 ? ringAbility[1] : 6;
        if (!(player instanceof ServerPlayer serverPlayer) || flyThreshold < 0 || data.getMarriageNum() < flyThreshold) {
            return;
        }

        boolean inLiquid = player.isInWaterOrBubble() || player.isInLava();
        if (inLiquid) {
            if (!player.getAbilities().instabuild) {
                serverPlayer.getAbilities().mayfly = true;
            }

            serverPlayer.getAbilities().flying = true;
            data.setRingFlying(true);
            serverPlayer.onUpdateAbilities();
            return;
        }

        if (!data.isRingFlying() || player.getAbilities().instabuild) {
            return;
        }

        serverPlayer.getAbilities().flying = false;
        serverPlayer.getAbilities().mayfly = false;
        data.setRingFlying(false);
        serverPlayer.onUpdateAbilities();
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        if (FMLEnvironment.dist.isClient()) {
            int marriageNum = ClientShipVisibilityHelper.getClientMarriageNum();
            if (marriageNum >= 0) {
                tooltip.add(Component.literal("Married: " + marriageNum).withStyle(ChatFormatting.AQUA));
            }
        }
        tooltip.add(Component.translatable(isActive(stack)
                ? "gui.shincolle.marriagering.active"
                : "gui.shincolle.marriagering.inactive").withStyle(ChatFormatting.AQUA));
        tooltip.add(Component.translatable("gui.shincolle.marriagering.use").withStyle(ChatFormatting.DARK_GRAY));
    }

    public static boolean isActive(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        return tag != null && tag.getBoolean(ACTIVE_TAG);
    }
}
