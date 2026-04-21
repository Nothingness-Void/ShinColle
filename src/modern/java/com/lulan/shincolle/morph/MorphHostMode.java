package com.lulan.shincolle.morph;

public enum MorphHostMode {
    NONE,
    MORPH,
    RIDER,
    MOUNT;

    public static MorphHostMode fromOrdinal(int ordinal) {
        MorphHostMode[] values = values();
        if (ordinal < 0 || ordinal >= values.length) {
            return NONE;
        }
        return values[ordinal];
    }
}
