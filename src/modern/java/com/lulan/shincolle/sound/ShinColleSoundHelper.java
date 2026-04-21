package com.lulan.shincolle.sound;

import com.lulan.shincolle.registry.ModSoundEvents;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.RegistryObject;

import javax.annotation.Nullable;

public final class ShinColleSoundHelper {

    private ShinColleSoundHelper() {
    }

    public static SoundEvent getShipVoice(ShipSoundType type) {
        return ModSoundEvents.getShipVoice(type).get();
    }

    public static SoundEvent getShipVoice(ShipSoundType type, int shipId) {
        RegistryObject<SoundEvent> variant = ModSoundEvents.getShipVoiceVariant(shipId, type);
        return (variant != null ? variant : ModSoundEvents.getShipVoice(type)).get();
    }

    public static void playForPlayer(Level level, @Nullable Player player, SoundEvent sound, float volume, float pitch) {
        if (player == null || level.isClientSide()) {
            return;
        }

        level.playSound(null, player.blockPosition(), sound, SoundSource.PLAYERS, volume, pitch);
    }

    public static void playShipVoice(Level level, @Nullable Player player, ShipSoundType type, float volume, float pitch) {
        playForPlayer(level, player, getShipVoice(type), volume, pitch);
    }

    public static float variedPitch(Player player, float center, float spread) {
        return center + (player.getRandom().nextFloat() - player.getRandom().nextFloat()) * spread;
    }
}
