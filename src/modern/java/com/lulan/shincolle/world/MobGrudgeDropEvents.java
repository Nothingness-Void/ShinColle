package com.lulan.shincolle.world;

import com.lulan.shincolle.ShinColle;
import com.lulan.shincolle.config.ShinColleConfig;
import com.lulan.shincolle.entity.ship.LegacyShipEntity;
import com.lulan.shincolle.registry.ModItems;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.AbstractGolem;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.monster.Slime;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.common.EventBusSubscriber;

/** 1.12.2 EventHandler.onDrop: IMob / slime / golem drop grudge by ConfigHandler.dropGrudge. */
@EventBusSubscriber(modid = ShinColle.MOD_ID, bus = EventBusSubscriber.Bus.GAME)
public final class MobGrudgeDropEvents {

    private MobGrudgeDropEvents() {
    }

    @SubscribeEvent
    public static void onLivingDrops(LivingDropsEvent event) {
        LivingEntity host = event.getEntity();
        Level level = host.level();
        if (level.isClientSide()) {
            return;
        }
        boolean legacyMob = host instanceof Enemy || host instanceof Slime || host instanceof AbstractGolem
                || (host instanceof LegacyShipEntity ship && ship.isHostileVariant());
        if (!legacyMob || !level.getGameRules().getBoolean(GameRules.RULE_DOMOBLOOT)) {
            return;
        }

        float dropGrudge = (float) ShinColleConfig.dropGrudge();
        int numGrudge = (int) dropGrudge;
        if (numGrudge > 0) {
            addDrop(event, host, numGrudge);
        } else if (level.random.nextFloat() <= dropGrudge) {
            addDrop(event, host, 1);
        }
        if (level.random.nextFloat() < dropGrudge - (float) numGrudge) {
            addDrop(event, host, 1);
        }
    }

    private static void addDrop(LivingDropsEvent event, LivingEntity host, int count) {
        event.getDrops().add(new ItemEntity(host.level(), host.getX(), host.getY(), host.getZ(),
                new ItemStack(ModItems.GRUDGE.get(), count)));
    }
}
