package com.lulan.shincolle.gametest;

import com.lulan.shincolle.entity.ship.LegacyShipEntity;
import com.lulan.shincolle.entity.ship.ShipEntitySpecs;
import com.lulan.shincolle.registry.ModEntityTypes;
import com.lulan.shincolle.item.LegacyPotionData;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.ThrownPotion;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder("shincolle")
@PrefixGameTestTemplate(false)
public final class AuditPotionHealingGameTests {
    @GameTest(template = "gameplayparitygametests.empty", batch = "audit_parity")
    public static void cloudWinsThenSplashThenOrdinaryHealing(GameTestHelper helper) {
        var ship = new LegacyShipEntity(ModEntityTypes.LEGACY_SHIP.get(), helper.getLevel());
        ship.applySpawnSpec(ShipEntitySpecs.getByEggMeta(2), null);
        ship.setPos(helper.absoluteVec(new Vec3(2, 2, 2)));
        var cloud = new AreaEffectCloud(helper.getLevel(), ship.getX(), ship.getY(), ship.getZ());
        cloud.setPotionContents(new net.minecraft.world.item.alchemy.PotionContents(Potions.REGENERATION));
        var potion = new ThrownPotion(EntityType.POTION, helper.getLevel());
        potion.setItem(LegacyPotionData.setPotion(new ItemStack(Items.SPLASH_POTION), Potions.STRONG_HEALING));
        potion.setPos(ship.position());
        helper.getLevel().addFreshEntity(cloud);
        helper.getLevel().addFreshEntity(potion);
        try {
            float percent = Math.max(1.0F, ship.getMaxHealth() * 0.01F);
            check(helper, ship, 2.0F + (percent + 3.0F) * 1.5F);
            cloud.discard();
            check(helper, ship, 2.0F + (percent + 6.0F) * 2.0F);
            potion.setItem(LegacyPotionData.setPotion(new ItemStack(Items.SPLASH_POTION), Potions.WATER));
            check(helper, ship, 2.0F);
            potion.discard();
            check(helper, ship, 2.0F);
        } finally {
            cloud.discard();
            potion.discard();
            ship.discard();
        }
        helper.succeed();
    }

    private static void check(GameTestHelper helper, LegacyShipEntity ship, float expected) {
        expected *= ship.getLegacyStats().healRate();
        ship.setHealth(1.0F);
        ship.heal(2.0F);
        helper.assertTrue(Math.abs(ship.getHealth() - Math.min(ship.getMaxHealth(), 1.0F + expected)) < 0.001F,
                "actual heal override must apply bonus then HPRES; actual=" + ship.getHealth() + ", heal=" + expected);
    }
}
