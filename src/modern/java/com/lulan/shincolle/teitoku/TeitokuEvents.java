package com.lulan.shincolle.teitoku;

import com.lulan.shincolle.ShinColle;
import com.lulan.shincolle.entity.ship.LegacyShipEntity;
import com.lulan.shincolle.item.LegacyShipSpawnEggItem;
import com.lulan.shincolle.item.MarriageRingItem;
import net.minecraft.ChatFormatting;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BiomeTags;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerChangedDimensionEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerLoggedInEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerRespawnEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.items.IItemHandler;

import java.lang.reflect.Method;
import java.util.Optional;

@Mod.EventBusSubscriber(modid = ShinColle.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class TeitokuEvents {

    private static final ResourceLocation TEITOKU_ID =
            ResourceLocation.fromNamespaceAndPath(ShinColle.MOD_ID, "teitoku");

    private TeitokuEvents() {
    }

    public static void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.register(TeitokuData.class);
    }

    @SubscribeEvent
    public static void attachCapabilities(AttachCapabilitiesEvent<Entity> event) {
        if (event.getObject() instanceof Player) {
            event.addCapability(TEITOKU_ID, new TeitokuDataProvider());
        }
    }

    @SubscribeEvent
    public static void clonePlayer(PlayerEvent.Clone event) {
        event.getOriginal().reviveCaps();
        event.getOriginal().getCapability(TeitokuHelper.TEITOKU_CAPABILITY).ifPresent(oldData ->
                event.getEntity().getCapability(TeitokuHelper.TEITOKU_CAPABILITY).ifPresent(newData ->
                        newData.copyFrom(oldData)));
        event.getOriginal().invalidateCaps();
    }

    @SubscribeEvent
    public static void onPlayerLogin(PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
            TeitokuHelper.initializeAndSync(serverPlayer);
        }
    }

    @SubscribeEvent
    public static void onPlayerRespawn(PlayerRespawnEvent event) {
        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
            TeitokuHelper.initializeAndSync(serverPlayer);
        }
    }

    @SubscribeEvent
    public static void onPlayerChangedDimension(PlayerChangedDimensionEvent event) {
        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
            TeitokuHelper.initializeAndSync(serverPlayer);
        }
    }

    @SubscribeEvent
    public static void onRecoveredShipEggPickup(EntityItemPickupEvent event) {
        ItemEntity itemEntity = event.getItem();
        ItemStack stack = itemEntity.getItem();
        Player player = event.getEntity();
        if (!LegacyShipSpawnEggItem.isRecoveredShipStack(stack)
                || LegacyShipSpawnEggItem.canPlayerAccessRecoveredShip(stack, player)) {
            return;
        }

        event.setCanceled(true);
        itemEntity.setPickUpDelay(20);
        Component ownerName = Component.literal(LegacyShipSpawnEggItem.getRecoveredOwnerName(stack).orElse("?"))
                .withStyle(ChatFormatting.GOLD);
        player.displayClientMessage(Component.translatable("chat.shincolle.entity.owner_locked", ownerName), true);
    }

    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        LivingEntity target = event.getEntity();
        if (target.level().isClientSide()) {
            return;
        }
        Entity attackerSource = event.getSource().getEntity();
        Entity attacker = event.getSource().getDirectEntity() != null
                ? event.getSource().getDirectEntity()
                : attackerSource;
        if (attackerSource == null && attacker == null) {
            return;
        }

        double distSq = attackerSource != null ? target.distanceToSqr(attackerSource) : Double.MAX_VALUE;
        Entity revengeTarget = (distSq < 1024.0D && attackerSource != null) ? attackerSource : attacker;

        if (attackerSource instanceof Player playerAttacker) {
            propagatePlayerRevenge(playerAttacker, target, 32.0D);
        }

        if (target instanceof Player playerTarget) {
            if (revengeTarget instanceof LivingEntity livingRevenge) {
                propagatePlayerRevenge(playerTarget, livingRevenge, 32.0D);
            }
        } else if (target instanceof LegacyShipEntity hostileShip && hostileShip.isHostileVariant()) {
            if (attackerSource instanceof LegacyShipEntity attackerShip && attackerShip.isHostileVariant()) {
                return;
            }
            if (revengeTarget instanceof LivingEntity livingRevenge) {
                propagateHostileShipRevenge(hostileShip, livingRevenge, 64.0D);
            }
        }
    }

    private static void propagatePlayerRevenge(Player player, LivingEntity target, double range) {
        for (LegacyShipEntity ship : player.level().getEntitiesOfClass(
                LegacyShipEntity.class, player.getBoundingBox().inflate(range))) {
            if (!ship.isHostileVariant()
                    && ship != target
                    && ship.isOwnedBy(player)
                    && ship.canCommandOrRevengeEngage(target)) {
                ship.setLastHurtByMob(target);
            }
        }
    }

    private static void propagateHostileShipRevenge(LegacyShipEntity host, LivingEntity target, double range) {
        for (LegacyShipEntity ship : host.level().getEntitiesOfClass(
                LegacyShipEntity.class, host.getBoundingBox().inflate(range))) {
            if (ship.isHostileVariant()
                    && ship != target
                    && ship.canEngage(target)) {
                ship.setLastHurtByMob(target);
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.player.level().isClientSide()) {
            return;
        }
        if (!(event.player instanceof ServerPlayer serverPlayer)) {
            return;
        }

        int cooldown = TeitokuHelper.getTeamCooldown(serverPlayer);
        int bossCooldown = TeitokuHelper.get(serverPlayer).map(TeitokuData::getBossCooldown).orElse(0);
        boolean shouldSync = false;

        if (cooldown > 0) {
            int next = TeitokuHelper.tickCooldown(serverPlayer);
            shouldSync |= next == 0 || (serverPlayer.tickCount % 20) == 0;
        }

        if (bossCooldown > 0) {
            boolean legacyBossBiome = serverPlayer.serverLevel().getBiome(serverPlayer.blockPosition()).is(BiomeTags.IS_OCEAN)
                    || serverPlayer.serverLevel().getBiome(serverPlayer.blockPosition()).is(BiomeTags.IS_DEEP_OCEAN)
                    || serverPlayer.serverLevel().getBiome(serverPlayer.blockPosition()).is(BiomeTags.IS_BEACH);
            boolean hasRing = TeitokuHelper.get(serverPlayer).map(TeitokuData::hasRing).orElse(false);
            if (legacyBossBiome && hasRing) {
                int nextBoss = TeitokuHelper.tickBossCooldown(serverPlayer);
                shouldSync |= nextBoss == 0 || (serverPlayer.tickCount % 20) == 0;
            }
        }

        tickOptionalCuriosMarriageRing(serverPlayer);

        if (shouldSync) {
            TeitokuHelper.syncGameplayState(serverPlayer);
        }
    }

    private static void tickOptionalCuriosMarriageRing(ServerPlayer serverPlayer) {
        if (hasActiveVanillaMarriageRing(serverPlayer) || !ModList.get().isLoaded("curios")) {
            return;
        }
        try {
            Class<?> curiosApiClass = Class.forName("top.theillusivec4.curios.api.CuriosApi");
            Method getInventoryMethod = curiosApiClass.getMethod("getCuriosInventory", LivingEntity.class);
            Object lazyOptional = getInventoryMethod.invoke(null, serverPlayer);
            if (lazyOptional == null) {
                return;
            }
            Object resolvedObj = lazyOptional.getClass().getMethod("resolve").invoke(lazyOptional);
            if (!(resolvedObj instanceof Optional<?> resolved) || resolved.isEmpty()) {
                return;
            }
            Object curiosHandler = resolved.get();
            Object equippedObj = curiosHandler.getClass().getMethod("getEquippedCurios").invoke(curiosHandler);
            if (equippedObj instanceof IItemHandler itemHandler) {
                int slots = itemHandler.getSlots();
                for (int i = 0; i < slots; i++) {
                    ItemStack stack = itemHandler.getStackInSlot(i);
                    if (stack.getItem() instanceof MarriageRingItem && MarriageRingItem.isActive(stack)) {
                        MarriageRingItem.tickActiveRingEffects(serverPlayer, stack);
                        break;
                    }
                }
            }
        } catch (ReflectiveOperationException | LinkageError ignored) {
        }
    }

    private static boolean hasActiveVanillaMarriageRing(Player player) {
        Inventory inventory = player.getInventory();
        for (int i = 0; i < inventory.getContainerSize(); i++) {
            ItemStack stack = inventory.getItem(i);
            if (stack.getItem() instanceof MarriageRingItem && MarriageRingItem.isActive(stack)) {
                return true;
            }
        }
        return false;
    }
}
