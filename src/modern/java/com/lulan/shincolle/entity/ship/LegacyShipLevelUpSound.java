package com.lulan.shincolle.entity.ship;

import com.lulan.shincolle.registry.ModSoundEvents;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;

public record LegacyShipLevelUpSound(SoundEvent sound, float volume, float pitch) {
    public static LegacyShipLevelUpSound forRoll(int roll) {
        return new LegacyShipLevelUpSound(
                roll == 0 ? SoundEvents.PLAYER_LEVELUP : ModSoundEvents.SHIP_LEVELUP.get(), 0.75F, 1.0F);
    }
}
