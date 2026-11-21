package com.lulan.shincolle.sound;

import com.lulan.shincolle.config.ShinColleConfig;
import com.lulan.shincolle.registry.ModSoundEvents;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.registries.DeferredHolder;

import javax.annotation.Nullable;
import java.util.EnumMap;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

public final class ShinColleSoundHelper {

    private static final Map<Integer, EnumMap<ShipSoundType, Float>> DEFAULT_CUSTOM_SOUND_RATES = Map.of(
            54, createRateMap(
                    ShipSoundType.IDLE, 0.25F,
                    ShipSoundType.HURT, 0.25F,
                    ShipSoundType.MARRY, 0.50F,
                    ShipSoundType.PICKITEM, 0.50F),
            56, createRateMap(
                    ShipSoundType.IDLE, 0.50F,
                    ShipSoundType.HIT, 0.50F,
                    ShipSoundType.HURT, 0.50F,
                    ShipSoundType.DEAD, 1.00F,
                    ShipSoundType.PICKITEM, 0.50F),
            60, createRateMap(
                    ShipSoundType.IDLE, 0.25F,
                    ShipSoundType.HIT, 0.50F),
            62, createRateMap(
                    ShipSoundType.HIT, 0.35F)
    );

    private ShinColleSoundHelper() {
    }

    public static SoundEvent getShipVoice(ShipSoundType type) {
        return ModSoundEvents.getShipVoice(type).get();
    }

    public static SoundEvent getShipVoice(ShipSoundType type, int shipId) {
        DeferredHolder<SoundEvent, SoundEvent> variant = ModSoundEvents.getShipVoiceVariant(shipId, type);
        if (variant != null) {
            float rate = getCustomSoundRate(shipId, type);
            if (rate > 0.0F && (rate >= 1.0F || ThreadLocalRandom.current().nextFloat() < rate)) {
                return variant.get();
            }
        }
        return ModSoundEvents.getShipVoice(type).get();
    }

    public static float getCustomSoundRate(int shipId, ShipSoundType type) {
        EnumMap<ShipSoundType, Float> rates = DEFAULT_CUSTOM_SOUND_RATES.get(shipId);
        if (rates == null) {
            return 1.0F;
        }
        return rates.getOrDefault(type, 0.0F);
    }

    public static void playForPlayer(Level level, @Nullable Player player, SoundEvent sound, float volume, float pitch) {
        if (player == null || level.isClientSide()) {
            return;
        }

        level.playSound(null, player.blockPosition(), sound, SoundSource.PLAYERS, volume, pitch);
    }

    public static void playShipVoice(Level level, @Nullable Player player, ShipSoundType type, float volume, float pitch) {
        playForPlayer(level, player, getShipVoice(type), volume * ShinColleConfig.volumeShip(), pitch);
    }

    public static void playShipVoice(Level level, @Nullable Player player, ShipSoundType type, int shipId,
                                     double x, double y, double z, float volume, float pitch) {
        if (level == null || level.isClientSide()) {
            return;
        }

        level.playSound(null, x, y, z, getShipVoice(type, shipId), SoundSource.PLAYERS,
                volume * ShinColleConfig.volumeShip(), pitch);
    }

    public static float variedPitch(Player player, float center, float spread) {
        return center + (player.getRandom().nextFloat() - player.getRandom().nextFloat()) * spread;
    }

    private static EnumMap<ShipSoundType, Float> createRateMap(Object... entries) {
        EnumMap<ShipSoundType, Float> map = new EnumMap<>(ShipSoundType.class);
        for (int i = 0; i + 1 < entries.length; i += 2) {
            map.put((ShipSoundType) entries[i], (Float) entries[i + 1]);
        }
        return map;
    }
}
