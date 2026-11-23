package com.lulan.shincolle.gametest;

import com.lulan.shincolle.entity.ship.LegacyShipEntity;
import com.lulan.shincolle.entity.ship.ShipEntitySpecs;
import com.lulan.shincolle.registry.ModEntityTypes;
import com.lulan.shincolle.registry.ModItems;
import com.mojang.authlib.GameProfile;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import java.util.UUID;

@GameTestHolder("shincolle")
@PrefixGameTestTemplate(false)
public final class AuditTrainingGameTests {
    @GameTest(template = "gameplayparitygametests.empty", batch = "audit_training")
    public static void trainingHealsConsumesAtUnmarriedCapButStopsAt150(GameTestHelper helper) {
        var owner = FakePlayerFactory.get(helper.getLevel(), new GameProfile(UUID.randomUUID(), "AuditTraining"));
        var ship = new LegacyShipEntity(ModEntityTypes.LEGACY_SHIP.get(), helper.getLevel());
        ship.applySpawnSpec(ShipEntitySpecs.getByEggMeta(2), owner);
        owner.getAbilities().instabuild = false;
        owner.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(ModItems.TRAININGBOOK.get(), 3));
        try {
            ship.setShipLevel(99);
            for (int remaining : new int[] {2, 1}) {
                ship.setHealth(1.0F);
                ship.mobInteract(owner, InteractionHand.MAIN_HAND);
                helper.assertTrue(ship.getShipLevel() == 100 && ship.getHealth() == ship.getMaxHealth(),
                        "normal and capped unmarried training heals fully without exceeding100");
                helper.assertTrue(owner.getMainHandItem().getCount() == remaining,
                        "legacy consumes a book at100 because only150 blocks use");
            }
            ship.setMarried(true);
            ship.setShipLevel(150);
            ship.setHealth(1.0F);
            ship.mobInteract(owner, InteractionHand.MAIN_HAND);
            helper.assertTrue(owner.getMainHandItem().getCount() == 1 && ship.getHealth() == 1.0F,
                    "at150 book is not consumed and no heal occurs");
        } finally {
            owner.getInventory().clearContent();
            ship.discard();
        }
        helper.succeed();
    }
}
