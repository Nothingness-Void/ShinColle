package com.lulan.shincolle.gametest;

import com.lulan.shincolle.blockentity.LegacyCoreBlockEntity;
import com.lulan.shincolle.entity.ship.LegacyShipEntity;
import com.lulan.shincolle.registry.ModBlocks;
import com.lulan.shincolle.registry.ModEntityTypes;
import com.mojang.authlib.GameProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import java.util.UUID;

@GameTestHolder("shincolle")
@PrefixGameTestTemplate(false)
public final class AuditVolCoreDamageGameTests {
    @GameTest(template = "legacyaircraftblockcollisiongametests.flight_space", batch = "audit_volcore_damage")
    public static void dryCoreBurnsPlayersAndStopsEntireLoopAtShip(GameTestHelper helper) throws Exception {
        helper.setBlock(4, 2, 4, ModBlocks.BLOCK_VOL_CORE.get());
        var core = (LegacyCoreBlockEntity) helper.getBlockEntity(new BlockPos(4, 2, 4));
        var apply = LegacyCoreBlockEntity.class.getDeclaredMethod("tickVolCoreAura", Level.class);
        apply.setAccessible(true);
        var player = FakePlayerFactory.get(helper.getLevel(), new GameProfile(UUID.randomUUID(), "AuditCoreFire"));
        player.setPos(helper.absoluteVec(new Vec3(5, 2, 4)));
        helper.getLevel().addNewPlayer(player);
        player.getAbilities().instabuild = false;
        player.getAbilities().invulnerable = false;
        player.invulnerableTime = 0;
        player.setHealth(20.0F);
        try {
            apply.invoke(core, helper.getLevel());
            helper.assertTrue(player.getHealth() == 16.0F && player.isOnFire(), "dry aura burns survival players for4");
        } finally {
            player.discard();
        }
        var ship = new LegacyShipEntity(ModEntityTypes.LEGACY_SHIP.get(), helper.getLevel());
        ship.setPos(helper.absoluteVec(new Vec3(5, 2, 4)));
        var target = EntityType.ZOMBIE.create(helper.getLevel());
        target.setPos(ship.position());
        helper.getLevel().addFreshEntity(ship);
        helper.getLevel().addFreshEntity(target);
        try {
            var order = helper.getLevel().getEntitiesOfClass(Entity.class, new AABB(core.getBlockPos()).inflate(6));
            helper.assertTrue(order.indexOf(ship) < order.indexOf(target), "fixture orders ship before ordinary target");
            float before = target.getHealth();
            apply.invoke(core, helper.getLevel());
            helper.assertTrue(target.getHealth() == before && !target.isOnFire(),
                    "first ship returns from whole aura, not merely continue");
        } finally {
            ship.discard();
            target.discard();
        }
        helper.succeed();
    }
}
