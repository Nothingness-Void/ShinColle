package com.lulan.shincolle.gametest;

import com.lulan.shincolle.config.ShinColleConfig;
import com.lulan.shincolle.item.LegacyItemData;
import com.lulan.shincolle.registry.ModItems;
import com.lulan.shincolle.teitoku.TeitokuEvents;
import com.lulan.shincolle.teitoku.TeitokuHelper;
import com.mojang.authlib.GameProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.common.damagesource.DamageContainer;
import net.minecraft.server.level.ClientInformation;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import net.neoforged.neoforge.items.IItemHandlerModifiable;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@GameTestHolder("shincolle")
@PrefixGameTestTemplate(false)
public final class LegacyCuriosRingGameTests {
    private LegacyCuriosRingGameTests() {
    }

    @GameTest(template = "gameplayparitygametests.empty")
    public static void ringTagAndOptionalCuriosInventoryPreserveAllRingAbilitiesAndLossCleanup(GameTestHelper helper) throws Exception {
        var ring = new ItemStack(ModItems.MARRIAGERING.get());
        LegacyItemData.update(ring, tag -> tag.putBoolean("isActive", true));
        helper.assertTrue(ring.is(TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath("curios", "ring"))),
                "normal Curios ring-slot tag must accept the marriage ring");
        if (!ModList.get().isLoaded("curios")) {
            helper.succeed();
            return;
        }
        var sink = FakePlayerFactory.get(helper.getLevel(), new GameProfile(UUID.randomUUID(), "CuriosRingSink"));
        var player = new ServerPlayer(helper.getLevel().getServer(), helper.getLevel(), new GameProfile(UUID.randomUUID(), "CuriosRing"), ClientInformation.createDefault());
        player.connection = sink.connection;
        Class<?> api = Class.forName("top.theillusivec4.curios.api.CuriosApi");
        Object optional = api.getMethod("getCuriosInventory", LivingEntity.class).invoke(null, player);
        Object resolved = optional instanceof Optional<?> ? optional : optional.getClass().getMethod("resolve").invoke(optional);
        if (((Map<?, ?>) api.getMethod("getEntitySlots", LivingEntity.class).invoke(null, player)).isEmpty()) {
            helper.assertTrue(((Optional<?>) resolved).isEmpty(), "Curios exposes no inventory without configured entity slots");
            helper.succeed();
            return;
        }
        Object inventory = ((Optional<?>) resolved).orElseThrow(() -> new AssertionError("Curios capability must attach to a real server player"));
        Class<?> handlerType = Class.forName("top.theillusivec4.curios.api.type.capability.ICuriosItemHandler");
        Map<?, ?> slots = (Map<?, ?>) handlerType.getMethod("getCurios").invoke(inventory);
        Object stacksHandler = slots.get("ring");
        helper.assertTrue(stacksHandler != null, "optional integration fixture requires a configured ring slot");
        var stacks = (IItemHandlerModifiable) stacksHandler.getClass().getMethod("getStacks").invoke(stacksHandler);
        ItemStack previous = stacks.getStackInSlot(0);
        var data = TeitokuHelper.get(player).orElseThrow();
        try {
            ShinColleConfig.setTestOverride("ringAbility", new int[] {0, 6, 30, 20, 12});
            player.getInventory().clearContent();
            stacks.setStackInSlot(0, ring);
            player.tickCount = 128;
            player.getAbilities().instabuild = false;
            player.getAbilities().mayfly = false;
            player.getAbilities().flying = false;
            data.setMarriageNum(12);
            TeitokuEvents.onRingScan(new PlayerTickEvent.Pre(player));
            helper.assertTrue(data.hasRing() && data.isRingActive(), "Curios-only ring must populate the same capability flags as inventory/Baubles");
            var wet = helper.absolutePos(new BlockPos(1, 3, 1));
            helper.getLevel().setBlockAndUpdate(wet, Blocks.WATER.defaultBlockState());
            player.setPos(wet.getX() + 0.5D, wet.getY() + 0.1D, wet.getZ() + 0.5D);
            player.setAirSupply(10);
            var tick = TeitokuEvents.class.getDeclaredMethod("tickOptionalCuriosMarriageRing", net.minecraft.server.level.ServerPlayer.class);
            tick.setAccessible(true);
            tick.invoke(null, player);
            helper.assertTrue(player.getAirSupply() == player.getMaxAirSupply(), "Curios-only ring water-breathing tick must execute");
            helper.assertTrue(player.getAbilities().flying && !player.getAbilities().mayfly, "Curios water flight must never grant double-jump permission");
            var fire = new LivingIncomingDamageEvent(player, new DamageContainer(player.damageSources().inFire(), 2.0F));
            TeitokuEvents.onLivingAttack(fire);
            helper.assertTrue(fire.isCanceled(), "Curios-only active ring must provide fire immunity");
            LegacyItemData.update(ring, tag -> tag.putBoolean("isActive", false));
            TeitokuEvents.onRingScan(new PlayerTickEvent.Pre(player));
            player.setAirSupply(10);
            tick.invoke(null, player);
            helper.assertTrue(!data.isRingActive() && !player.getAbilities().flying && !player.getAbilities().mayfly,
                    "disabling a Curios ring must revoke temporary flight");
            helper.assertTrue(player.getAirSupply() == player.getMaxAirSupply(), "legacy passive breathing must work even with the Curios ring toggled off");
            LegacyItemData.update(ring, tag -> tag.putBoolean("isActive", true));
            TeitokuEvents.onRingScan(new PlayerTickEvent.Pre(player));
            tick.invoke(null, player);
            helper.assertTrue(player.getAbilities().flying, "re-enabled Curios ring must resume water flight");
            stacks.setStackInSlot(0, ItemStack.EMPTY);
            TeitokuEvents.onRingScan(new PlayerTickEvent.Pre(player));
            helper.assertTrue(!data.hasRing() && !player.getAbilities().flying && !player.getAbilities().mayfly,
                    "removing the Curios ring must revoke temporary flight without leaving mayfly");
            tick.invoke(null, player);
            helper.assertTrue(!player.getAbilities().flying, "an empty Curios slot must not re-enable flight");
        } finally {
            stacks.setStackInSlot(0, previous);
            ShinColleConfig.clearTestOverrides();
            player.getInventory().clearContent();
        }
        helper.succeed();
    }
}
