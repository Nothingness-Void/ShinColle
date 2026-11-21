package com.lulan.shincolle.gametest;

import com.mojang.authlib.GameProfile;
import com.lulan.shincolle.ShinColle;
import com.lulan.shincolle.config.ShinColleConfig;
import com.lulan.shincolle.entity.ship.LegacyShipEntity;
import com.lulan.shincolle.entity.ship.LegacyShipCombatHelper;
import com.lulan.shincolle.entity.ship.LegacyShipStatTables;
import com.lulan.shincolle.morph.MorphDamageHandler;
import com.lulan.shincolle.morph.MorphEvents;
import com.lulan.shincolle.morph.MorphHelper;
import com.lulan.shincolle.morph.MorphHostMode;
import com.lulan.shincolle.morph.MorphProfile;
import com.lulan.shincolle.playerskill.PlayerSkillHostResolver;
import com.lulan.shincolle.registry.ModEntityTypes;
import com.lulan.shincolle.registry.ModItems;
import com.lulan.shincolle.teitoku.TeitokuData;
import com.lulan.shincolle.teitoku.TeitokuHelper;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.projectile.ThrownPotion;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.UUID;

@GameTestHolder(ShinColle.MOD_ID)
@PrefixGameTestTemplate(false)
public final class MorphParityGameTests {

    private static ServerPlayer player(GameTestHelper helper) {
        return FakePlayerFactory.get(helper.getLevel(), new GameProfile(UUID.randomUUID(), "MorphParity"));
    }

    private static TeitokuData activate(ServerPlayer player) {
        TeitokuData data = TeitokuHelper.get(player).resolve().orElseThrow();
        data.unlockMorph(58);
        data.setSelectedMorphProfile(58);
        data.setMorphActive(true);
        return data;
    }

    @GameTest(template = "gameplayparitygametests.empty")
    public static void healthUsesLegacyRatioAndResetsOnLoginAndDimensionChange(GameTestHelper helper) {
        ServerPlayer player = player(helper);
        TeitokuData data = activate(player);
        data.getSelectedMorphProfile().setAmmoLight(0);
        data.getSelectedMorphProfile().setAmmoHeavy(0);
        data.getSelectedMorphProfile().setGrudge(0);
        double speed = player.getAttributeValue(Attributes.MOVEMENT_SPEED);
        double attack = player.getAttributeValue(Attributes.ATTACK_DAMAGE);
        try {
            ShinColleConfig.setTestOverride("morphHpRatio", 0.1D);
            player.tickCount = 31;
            MorphHelper.tick(player);
            float shipHp = data.getSelectedMorphProfile().buildStats(player.getActiveEffects()).get(LegacyShipStatTables.Attr.HP);
            helper.assertTrue(Math.abs(player.getMaxHealth() - (20D + shipHp * 0.1D)) < 0.001D, "legacy additive HP formula");
            helper.assertTrue(player.getAttributeValue(Attributes.MOVEMENT_SPEED) == speed
                    && player.getAttributeValue(Attributes.ATTACK_DAMAGE) == attack, "no invented movement or attack modifiers");
            helper.assertTrue(data.hasActiveMorph(), "empty resources do not force demorph");
            player.tickCount = 32;
            MorphHelper.tick(player);
            helper.assertTrue(!data.hasActiveMorph() && player.getMaxHealth() == 20F, "login tick 32 demorphs and resets HP");
            activate(player);
            player.tickCount = 33;
            MorphHelper.tick(player);
            MorphEvents.onDimensionChange(new PlayerEvent.PlayerChangedDimensionEvent(player, Level.OVERWORLD, Level.NETHER));
            helper.assertTrue(!data.hasActiveMorph() && player.getMaxHealth() == 20F, "dimension change demorphs");
        } finally {
            MorphHelper.demorph(player);
            ShinColleConfig.clearTestOverrides();
        }
        helper.succeed();
    }

    @GameTest(template = "gameplayparitygametests.empty")
    public static void morphDamagePreservesLegacyEarlyReturnsAndGoddessConsumption(GameTestHelper helper) {
        ServerPlayer player = player(helper);
        TeitokuData data = activate(player);
        Zombie attacker = new Zombie(EntityType.ZOMBIE, helper.getLevel());
        try {
            ShinColleConfig.setTestOverride("morphHpRatio", 0.1D);
            for (DamageSource source : new DamageSource[]{player.damageSources().inWall(), player.damageSources().cactus(), player.damageSources().fall()}) {
                helper.assertTrue(hurt(player, source, 10).getAmount() == 0F, "morph ignores wall/cactus/fall");
            }
            helper.assertTrue(hurt(player, player.damageSources().magic(), 10).getAmount() == 10F, "magic bypasses custom DEF");
            helper.assertTrue(hurt(player, player.damageSources().lava(), 10).getAmount() == 10F, "attackerless damage is unchanged");
            helper.assertTrue(hurt(player, player.damageSources().playerAttack(player), 10).getAmount() == 0F, "self damage is zero");
            ShinColleConfig.setTestOverride("friendlyFire", false);
            helper.assertTrue(hurt(player, player.damageSources().playerAttack(player(helper)), 10).getAmount() == 0F, "friendlyFire false blocks player damage");
            ThrownPotion potion = new ThrownPotion(helper.getLevel(), player);
            potion.setItem(PotionUtils.setPotion(new ItemStack(Items.SPLASH_POTION), Potions.HARMING));
            float hp = data.getSelectedMorphProfile().buildStats(player.getActiveEffects()).get(LegacyShipStatTables.Attr.HP);
            float expectedPotion = (Math.max(1F, hp * 0.01F) * 2F + 2F + 6F) * 0.1F;
            helper.assertTrue(Math.abs(hurt(player, player.damageSources().indirectMagic(potion, player), 6).getAmount() - expectedPotion) < 0.001F,
                    "potion scaling precedes even self-damage filtering");
            var stats = data.getSelectedMorphProfile().buildStats(player.getActiveEffects());
            long seed = 1;
            while (RandomSource.create(seed).nextFloat() <= stats.dodge()) {
                seed++;
            }
            RandomSource expectedRandom = RandomSource.create(seed);
            expectedRandom.nextFloat();
            float expected = Math.max(1F, 100F * (1F - stats.defense() + expectedRandom.nextFloat() * 0.5F - 0.25F));
            player.getRandom().setSeed(seed);
            helper.assertTrue(Math.abs(hurt(player, player.damageSources().mobAttack(attacker), 100).getAmount() - expected) < 0.001F, "legacy randomized DEF formula");
            LegacyShipEntity ship = ModEntityTypes.LEGACY_SHIP.get().create(helper.getLevel());
            ShinColleConfig.setTestOverride("morphDmgTakenRatio", 1D);
            float lightDamage = LegacyShipCombatHelper.modDamageByLight(expected,
                    LegacyShipCombatHelper.legacyDamageTypeFor(ship.getSpec()),
                    LegacyShipCombatHelper.legacyDamageTypeFor(data.getSelectedMorphProfile().getSpec()),
                    LegacyShipCombatHelper.legacyLightCoefficientForRawBrightness(
                            player.level().getMaxLocalRawBrightness(player.blockPosition()), false));
            player.getRandom().setSeed(seed);
            helper.assertTrue(Math.abs(hurt(player, player.damageSources().mobAttack(ship), 100).getAmount() - Math.max(1F, lightDamage)) < 0.001F, "light/type modifier");
            var missile = ModEntityTypes.LEGACY_SHIP_PROJECTILE.get().create(helper.getLevel());
            DamageSource missileSource = new DamageSource(player.damageSources().mobAttack(ship).typeHolder(), missile, ship);
            player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 200, 0));
            player.getRandom().setSeed(seed);
            helper.assertTrue(Math.abs(hurt(player, missileSource, 100).getAmount() - Math.max(1F, lightDamage * 0.8F)) < 0.001F, "missile resistance buff");
            player.removeEffect(MobEffects.DAMAGE_RESISTANCE);
            long dodgeSeed = 1;
            while (RandomSource.create(dodgeSeed).nextFloat() > stats.dodge()) {
                dodgeSeed++;
            }
            player.getRandom().setSeed(dodgeSeed);
            helper.assertTrue(hurt(player, player.damageSources().mobAttack(attacker), 10).isCanceled(), "legacy dodge cancels the event");
            ShinColleConfig.setTestOverride("morphDmgTakenRatio", 0D);
            player.getRandom().setSeed(seed);
            helper.assertTrue(hurt(player, player.damageSources().mobAttack(ship), 100).getAmount() == 0F, "ship damage uses morphDmgTakenRatio");
            player.getInventory().setItem(0, new ItemStack(ModItems.REPAIRGODDESS.get(), 2));
            player.setHealth(5F);
            player.getRandom().setSeed(seed);
            helper.assertTrue(hurt(player, player.damageSources().mobAttack(attacker), 10000).getAmount() == 0F
                    && player.getInventory().getItem(0).getCount() == 1 && player.getHealth() == player.getMaxHealth()
                    && player.invulnerableTime == 120 && data.getMorphRuntimeState().getImmuneTicks() == 120,
                    "type 8 consumes one goddess, heals fully, and grants 120 tick immunity");
        } finally {
            MorphHelper.demorph(player);
            helper.assertTrue(hurt(player, player.damageSources().fall(), 10).getAmount() == 10F, "inactive morph leaves damage unchanged");
            ShinColleConfig.clearTestOverrides();
        }
        helper.succeed();
    }

    @GameTest(template = "gameplayparitygametests.empty")
    public static void acquisitionAndSkillHostsUseOwnerNotCommanderAndLegacyOrder(GameTestHelper helper) {
        ServerPlayer owner = player(helper);
        TeitokuData data = TeitokuHelper.get(owner).resolve().orElseThrow();
        data.setPlayerUid(98301);
        LegacyShipEntity ship = ModEntityTypes.LEGACY_SHIP.get().create(helper.getLevel());
        ship.setVariantEggMeta(58);
        ship.setOwner(owner.getUUID(), "MorphParity", 98301);
        ship.setShipLevel(50);
        ModItems.TARGETWRENCH.get().onLeftClickEntity(new ItemStack(ModItems.TARGETWRENCH.get()), owner, ship);
        MorphProfile profile = data.findMorphProfile(ship.getShipClassId());
        helper.assertTrue(profile != null && profile.getLevel() == 1 && profile.getAmmoLight() == 0
                && profile.getAmmoHeavy() == 0 && profile.getGrudge() == 0 && profile.isMarried()
                && data.getSelectedMorphProfile() == null, "wrench acquires a reset profile without selecting");
        data.setSelectedMorphProfile(profile.getLegacyClassId());
        data.setMorphActive(true);
        owner.startRiding(ship, true);
        helper.assertTrue(PlayerSkillHostResolver.resolve(owner).mode() == MorphHostMode.MORPH, "riding a ship is not RIDER skill hosting");
        owner.stopRiding();
        ship.startRiding(owner, true);
        helper.assertTrue(PlayerSkillHostResolver.resolve(owner).mode() == MorphHostMode.RIDER, "first passenger takes precedence over morph");
        ship.setOwner(UUID.randomUUID(), "Other", 98302);
        helper.assertTrue(PlayerSkillHostResolver.resolve(owner).mode() == MorphHostMode.NONE, "foreign first passenger cannot fall through to morph");
        ship.stopRiding();
        ship.discard();
        helper.succeed();
    }

    private static LivingHurtEvent hurt(ServerPlayer player, DamageSource source, float amount) {
        LivingHurtEvent event = new LivingHurtEvent(player, source, amount);
        MorphDamageHandler.onHurt(event);
        return event;
    }
}
