package com.lulan.shincolle.sound;

public enum ShipSoundType {

    IDLE("ship-idle"),
    HIT("ship-hit"),
    HURT("ship-hurt"),
    DEAD("ship-death"),
    MARRY("ship-marry"),
    KNOCKBACK("ship-knockback"),
    PICKITEM("ship-item"),
    FEED("ship-feed"),
    TIMEKEEP00("ship-time0"),
    TIMEKEEP01("ship-time1"),
    TIMEKEEP02("ship-time2"),
    TIMEKEEP03("ship-time3"),
    TIMEKEEP04("ship-time4"),
    TIMEKEEP05("ship-time5"),
    TIMEKEEP06("ship-time6"),
    TIMEKEEP07("ship-time7"),
    TIMEKEEP08("ship-time8"),
    TIMEKEEP09("ship-time9"),
    TIMEKEEP10("ship-time10"),
    TIMEKEEP11("ship-time11"),
    TIMEKEEP12("ship-time12"),
    TIMEKEEP13("ship-time13"),
    TIMEKEEP14("ship-time14"),
    TIMEKEEP15("ship-time15"),
    TIMEKEEP16("ship-time16"),
    TIMEKEEP17("ship-time17"),
    TIMEKEEP18("ship-time18"),
    TIMEKEEP19("ship-time19"),
    TIMEKEEP20("ship-time20"),
    TIMEKEEP21("ship-time21"),
    TIMEKEEP22("ship-time22"),
    TIMEKEEP23("ship-time23");

    private final String registryName;

    ShipSoundType(String registryName) {
        this.registryName = registryName;
    }

    public String registryName() {
        return this.registryName;
    }
}
