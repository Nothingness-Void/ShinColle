package com.lulan.shincolle.gametest;

import com.lulan.shincolle.blockentity.LegacyCoreBlockEntity;
import com.lulan.shincolle.entity.ship.LegacyShipEntity;
import com.lulan.shincolle.entity.ship.ShipEntitySpecs;
import com.lulan.shincolle.registry.ModBlocks;
import com.lulan.shincolle.registry.ModEntityTypes;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder("shincolle")
@PrefixGameTestTemplate(false)
public final class AuditVolCoreAuraGameTests {
    @GameTest(template = "legacyaircraftblockcollisiongametests.flight_space", batch = "audit_volcore_aura")
    public static void auraRequiresOutOfCombatLiquidFeetAndMoraleBelowThreshold(GameTestHelper helper) throws Exception {
        helper.setBlock(4, 2, 4, ModBlocks.BLOCK_VOL_CORE.get());
        helper.setBlock(5, 2, 4, Blocks.WATER);
        helper.setBlock(6, 2, 4, Blocks.WATER);
        var core = (LegacyCoreBlockEntity) helper.getBlockEntity(new net.minecraft.core.BlockPos(4, 2, 4));
        var apply = LegacyCoreBlockEntity.class.getDeclaredMethod("tickVolCoreAura", Level.class);
        apply.setAccessible(true);
        var ship = new LegacyShipEntity(ModEntityTypes.LEGACY_SHIP.get(), helper.getLevel());
        ship.applySpawnSpec(ShipEntitySpecs.getByEggMeta(2), null);
        ship.setNoAi(true);
        ship.setPos(helper.absoluteVec(new Vec3(6.5D, 2.1D, 4.5D)));
        helper.getLevel().addFreshEntity(ship);
        try {
            ship.tickCount = 200;
            ship.markLegacyCombat();
            ship.setHealth(1.0F);
            ship.setMorale(5000);
            apply.invoke(core, helper.getLevel());
            helper.assertTrue(ship.getHealth() == 1.0F && ship.getMorale() == 5000, "combat blocks both restoration effects");
            ship.tickCount += 129;
            apply.invoke(core, helper.getLevel());
            helper.assertTrue(ship.getHealth() > 1.0F && ship.getMorale() == 5080, "wet out-of-combat ship heals and gains80");
            int threshold = (int) (5100 * 1.8F);
            ship.setMorale(threshold);
            apply.invoke(core, helper.getLevel());
            helper.assertTrue(ship.getMorale() == threshold, "VolCore-specific morale ceiling");
            helper.setBlock(6, 2, 4, Blocks.AIR);
            ship.setHealth(1.0F);
            ship.setMorale(5000);
            apply.invoke(core, helper.getLevel());
            helper.assertTrue(ship.getHealth() == 1.0F && ship.getMorale() == 5000,
                    "fluid near the core does not make dry ship feet eligible");
        } finally {
            ship.discard();
        }
        helper.succeed();
    }
}
