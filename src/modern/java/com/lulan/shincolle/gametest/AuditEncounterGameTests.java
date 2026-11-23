package com.lulan.shincolle.gametest;

import com.lulan.shincolle.world.HostileEncounterSpawner;
import com.lulan.shincolle.world.HostileSpawnProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder("shincolle")
@PrefixGameTestTemplate(false)
public final class AuditEncounterGameTests {
    @GameTest(template = "legacyaircraftblockcollisiongametests.flight_space", batch = "audit_parity")
    public static void rolledEncountersKeepLegacyOffsetsWithoutCollisionVeto(GameTestHelper helper) {
        for (int x = 3; x < 9; x++) for (int z = 3; z < 9; z++) for (int y = 1; y < 6; y++) {
            helper.setBlock(x, y, z, Blocks.STONE);
        }
        BlockPos anchor = helper.absolutePos(new BlockPos(4, 2, 4));
        for (boolean common : new boolean[] {true, false}) {
            var ship = HostileEncounterSpawner.spawnEncounterAt(helper.getLevel(), anchor, null,
                    new HostileSpawnProfile(2053, 1, false, !common), common);
            helper.assertTrue(ship != null, "successful roll must spawn despite surrounding solids");
            try {
                helper.assertTrue(!helper.getLevel().noCollision(ship, ship.getBoundingBox()), "fixture must collide");
                double dx = ship.getX() - anchor.getX();
                double dz = ship.getZ() - anchor.getZ();
                helper.assertTrue(ship.getY() == anchor.getY() + 0.5D, "legacy Y+0.5");
                helper.assertTrue(common ? dx >= 0 && dx < 1 && dz >= 0 && dz < 1
                                : dx >= 0 && dx <= 2 && dx == Math.floor(dx)
                                  && dz >= 0 && dz <= 2 && dz == Math.floor(dz),
                        "common is within one block; boss/escort offsets are integers0..2 without extra0.5");
            } finally {
                ship.discard();
            }
        }
        helper.succeed();
    }
}
