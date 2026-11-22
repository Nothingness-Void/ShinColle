package com.lulan.shincolle.morph;

import com.lulan.shincolle.ShinColle;
import com.lulan.shincolle.config.ShinColleConfig;
import com.lulan.shincolle.entity.ship.LegacyShipBehaviorCatalog;
import com.lulan.shincolle.entity.ship.LegacyShipCombatHelper;
import com.lulan.shincolle.entity.ship.LegacyShipEntity;
import com.lulan.shincolle.entity.ship.LegacyShipStatTables;
import com.lulan.shincolle.entity.ship.LegacyShipStats;
import com.lulan.shincolle.entity.mount.LegacyShipMountEntity;
import com.lulan.shincolle.entity.projectile.LegacyShipProjectileEntity;
import com.lulan.shincolle.entity.projectile.LegacyShipBeamEntity;
import com.lulan.shincolle.entity.ship.LegacyShipAircraftEntity;
import com.lulan.shincolle.entity.ship.LegacyShipRensouhouEntity;
import com.lulan.shincolle.entity.projectile.LegacyShipFloatingFortEntity;
import com.lulan.shincolle.network.CombatFxDispatcher;
import com.lulan.shincolle.network.GameplayParticleType;
import com.lulan.shincolle.registry.ModItems;
import com.lulan.shincolle.teitoku.TeitokuHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ThrownPotion;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.List;

@Mod.EventBusSubscriber(modid = ShinColle.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class MorphDamageHandler {

    private MorphDamageHandler() {
    }

    @SubscribeEvent
    public static void onHurt(LivingHurtEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        MorphProfile profile = MorphHelper.getActiveProfile(player);
        if (profile == null) {
            return;
        }
        var source = event.getSource();
        if (source.is(DamageTypes.IN_WALL) || source.is(DamageTypes.CACTUS) || source.is(DamageTypes.FALL)) {
            event.setAmount(0.0F);
            return;
        }
        if (source.is(DamageTypes.MAGIC) || source.is(DamageTypes.DRAGON_BREATH) || source.is(DamageTypes.FELL_OUT_OF_WORLD)) {
            markHurt(player);
            return;
        }
        LegacyShipStats stats = profile.buildStats(player.getActiveEffects());
        Entity direct = source.getDirectEntity();
        if (direct instanceof ThrownPotion || direct instanceof AreaEffectCloud) {
            List<MobEffectInstance> effects = direct instanceof ThrownPotion potion
                    ? PotionUtils.getMobEffects(potion.getItem())
                    : PotionUtils.getAllEffects(direct.saveWithoutId(new CompoundTag()));
            int level = effects.stream().filter(effect -> effect.getEffect() == MobEffects.HARM)
                    .mapToInt(effect -> effect.getAmplifier() + 1).max().orElse(0);
            float potionDamage = (Math.max(1F, stats.get(LegacyShipStatTables.Attr.HP) * 0.01F) * 2F + 2F) * level
                    + event.getAmount();
            if (potionDamage > 0F) {
                markHurt(player);
                event.setAmount((float) (potionDamage * ShinColleConfig.morphHpRatio()));
                return;
            }
        }
        Entity attacker = source.getEntity();
        if (attacker == null) {
            return;
        }
        if (attacker == player || attacker instanceof Player && !ShinColleConfig.friendlyFire()) {
            event.setAmount(0F);
            return;
        }
        float dodge = stats.dodge();
        float invisible = LegacyShipBehaviorCatalog.invisibleLevelFor(profile.getSpec());
        float distance = (1F - profile.getLevel() * (1F + invisible) / 300F) * 64F;
        if (invisible > 0F && player.distanceToSqr(attacker) > distance * distance) {
            dodge += invisible;
        }
        if (player.getRandom().nextFloat() <= dodge) {
            event.setCanceled(true);
            CombatFxDispatcher.sendParticle(player, GameplayParticleType.TEXT_DODGE, player.getX(), player.getY(), player.getZ());
            return;
        }
        float damage = event.getAmount() * (1F - stats.defense() + (player.getRandom().nextFloat() * 0.5F - 0.25F));
        if (isShipAttack(attacker)) {
            damage *= ShinColleConfig.morphDmgTakenRatio();
        }
        MobEffectInstance resistance = player.getEffect(direct instanceof LegacyShipProjectileEntity
                ? MobEffects.DAMAGE_RESISTANCE : MobEffects.FIRE_RESISTANCE);
        if (resistance != null && isShipAttack(direct)) {
            damage *= 1F - Math.min(4, resistance.getAmplifier() + 1) * 0.2F;
        }
        if (isShipAttack(attacker)) {
            int light = player.level().getMaxLocalRawBrightness(player.blockPosition());
            boolean nightVision = LegacyShipCombatHelper.hasLegacyNightVision(attacker, direct);
            damage = LegacyShipCombatHelper.modDamageByLight(damage,
                    LegacyShipCombatHelper.legacyDamageTypeForSource(source),
                    LegacyShipCombatHelper.legacyDamageTypeFor(profile.getSpec()),
                    LegacyShipCombatHelper.legacyLightCoefficientForRawBrightness(light, nightVision));
        }
        damage = damage > 0F ? Math.max(1F, damage) : 0F;
        if (damage >= player.getHealth() - 1F) {
            for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) {
                ItemStack stack = player.getInventory().getItem(slot);
                if (stack.is(ModItems.REPAIRGODDESS.get())) {
                    stack.shrink(1);
                    player.getInventory().setChanged();
                    player.setHealth(player.getMaxHealth());
                    player.invulnerableTime = 120;
                    TeitokuHelper.get(player).resolve().orElseThrow().getMorphRuntimeState().setImmuneTicks(120);
                    CombatFxDispatcher.sendParticle(player, GameplayParticleType.REPAIR_GODDESS,
                            player.getX(), player.getY(), player.getZ(), 0D, 0.03D, 0D, player.getId());
                    event.setAmount(0F);
                    return;
                }
            }
        }
        markHurt(player);
        event.setAmount(damage);
    }

    private static void markHurt(ServerPlayer player) {
        TeitokuHelper.get(player).ifPresent(data -> data.getMorphRuntimeState().setEmotion(3));
        TeitokuHelper.syncGameplayState(player);
    }

    private static boolean isShipAttack(Entity entity) {
        return entity instanceof LegacyShipEntity || entity instanceof LegacyShipProjectileEntity
                || entity instanceof LegacyShipBeamEntity || entity instanceof LegacyShipAircraftEntity
                || entity instanceof LegacyShipRensouhouEntity || entity instanceof LegacyShipFloatingFortEntity
                || entity instanceof LegacyShipMountEntity;
    }
}
