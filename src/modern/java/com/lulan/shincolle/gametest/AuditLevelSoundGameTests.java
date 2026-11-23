package com.lulan.shincolle.gametest;

import com.lulan.shincolle.entity.ship.LegacyShipLevelUpSound;
import com.lulan.shincolle.registry.ModSoundEvents;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder("shincolle")
@PrefixGameTestTemplate(false)
public final class AuditLevelSoundGameTests {
    @GameTest(template = "gameplayparitygametests.empty", batch = "audit_level_sound")
    public static void levelSoundIsExclusiveOneVanillaThreeShipRolls(GameTestHelper helper) {
        for (int roll = 0; roll < 4; roll++) {
            var sound = LegacyShipLevelUpSound.forRoll(roll);
            helper.assertTrue(sound.sound() == (roll == 0 ? SoundEvents.PLAYER_LEVELUP : ModSoundEvents.SHIP_LEVELUP.get()),
                    "legacy four-way roll chooses exactly one sound");
            helper.assertTrue(sound.volume() == 0.75F && sound.pitch() == 1.0F, "legacy volume/pitch");
        }
        helper.succeed();
    }
}
