package com.lulan.shincolle.registry;

import com.lulan.shincolle.ShinColle;
import com.lulan.shincolle.sound.ShipSoundType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import javax.annotation.Nullable;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

public final class ModSoundEvents {

    public static final DeferredRegister<SoundEvent> SOUND_EVENTS = DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, ShinColle.MOD_ID);

    private static final EnumMap<ShipSoundType, RegistryObject<SoundEvent>> SHIP_VOICES = registerShipVoices();
    private static final Map<Integer, EnumMap<ShipSoundType, RegistryObject<SoundEvent>>> SHIP_VOICE_VARIANTS = registerShipVoiceVariants();

    public static final RegistryObject<SoundEvent> SHIP_FIRELIGHT = register("ship-firelight");
    public static final RegistryObject<SoundEvent> SHIP_EXPLODE = register("ship-explode");
    public static final RegistryObject<SoundEvent> SHIP_FIREHEAVY = register("ship-fireheavy");
    public static final RegistryObject<SoundEvent> SHIP_AIRCRAFT = register("ship-aircraft");
    public static final RegistryObject<SoundEvent> SHIP_MACHINEGUN = register("ship-machinegun");
    public static final RegistryObject<SoundEvent> SHIP_LASER = register("ship-laser");
    public static final RegistryObject<SoundEvent> SHIP_KAITAI = register("ship-kaitai");
    public static final RegistryObject<SoundEvent> SHIP_AP_PHASE1 = register("ship-ap_phase1");
    public static final RegistryObject<SoundEvent> SHIP_AP_PHASE2 = register("ship-ap_phase2");
    public static final RegistryObject<SoundEvent> SHIP_AP_ATTACK = register("ship-ap_attack");
    public static final RegistryObject<SoundEvent> SHIP_WAKA_ATTACK = register("ship-waka_attack");
    public static final RegistryObject<SoundEvent> SHIP_WAKA_HURT = register("ship-waka_hurt");
    public static final RegistryObject<SoundEvent> SHIP_WAKA_IDLE = register("ship-waka_idle");
    public static final RegistryObject<SoundEvent> SHIP_WAKA_DEATH = register("ship-waka_death");
    public static final RegistryObject<SoundEvent> SHIP_GARURU = register("ship-garuru");
    public static final RegistryObject<SoundEvent> SHIP_YAMATO_READY = register("ship-yamato_ready");
    public static final RegistryObject<SoundEvent> SHIP_YAMATO_SHOT = register("ship-yamato_shot");
    public static final RegistryObject<SoundEvent> SHIP_LEVELUP = register("ship-levelup");
    public static final RegistryObject<SoundEvent> SHIP_BELL = register("ship-bell");
    public static final RegistryObject<SoundEvent> SHIP_JET = register("ship-jet");
    public static final RegistryObject<SoundEvent> SHIP_HITMETAL = register("ship-hitmetal");

    private ModSoundEvents() {
    }

    public static RegistryObject<SoundEvent> getShipVoice(ShipSoundType type) {
        return SHIP_VOICES.get(type);
    }

    @Nullable
    public static RegistryObject<SoundEvent> getShipVoiceVariant(int shipId, ShipSoundType type) {
        EnumMap<ShipSoundType, RegistryObject<SoundEvent>> variants = SHIP_VOICE_VARIANTS.get(shipId);
        return variants != null ? variants.get(type) : null;
    }

    private static EnumMap<ShipSoundType, RegistryObject<SoundEvent>> registerShipVoices() {
        EnumMap<ShipSoundType, RegistryObject<SoundEvent>> sounds = new EnumMap<>(ShipSoundType.class);

        for (ShipSoundType type : ShipSoundType.values()) {
            sounds.put(type, register(type.registryName()));
        }

        return sounds;
    }

    private static Map<Integer, EnumMap<ShipSoundType, RegistryObject<SoundEvent>>> registerShipVoiceVariants() {
        Map<Integer, EnumMap<ShipSoundType, RegistryObject<SoundEvent>>> variants = new HashMap<>();

        registerVariant(variants, 54, ShipSoundType.IDLE, "ship-idle-54");
        registerVariant(variants, 54, ShipSoundType.HURT, "ship-hurt-54");
        registerVariant(variants, 54, ShipSoundType.MARRY, "ship-marry-54");
        registerVariant(variants, 54, ShipSoundType.PICKITEM, "ship-item-54");

        registerVariant(variants, 56, ShipSoundType.IDLE, "ship-idle-56");
        registerVariant(variants, 56, ShipSoundType.HIT, "ship-hit-56");
        registerVariant(variants, 56, ShipSoundType.HURT, "ship-hurt-56");
        registerVariant(variants, 56, ShipSoundType.DEAD, "ship-death-56");
        registerVariant(variants, 56, ShipSoundType.PICKITEM, "ship-item-56");

        registerVariant(variants, 60, ShipSoundType.IDLE, "ship-idle-60");
        registerVariant(variants, 60, ShipSoundType.HIT, "ship-hit-60");

        registerVariant(variants, 62, ShipSoundType.HIT, "ship-hit-62");

        return variants;
    }

    private static void registerVariant(Map<Integer, EnumMap<ShipSoundType, RegistryObject<SoundEvent>>> variants,
                                        int shipId,
                                        ShipSoundType type,
                                        String registryName) {
        variants.computeIfAbsent(shipId, ignored -> new EnumMap<>(ShipSoundType.class))
                .put(type, register(registryName));
    }

    private static RegistryObject<SoundEvent> register(String name) {
        return SOUND_EVENTS.register(name,
                () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(ShinColle.MOD_ID, name)));
    }
}
