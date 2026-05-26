package org.daylight;

import java.util.Locale;

public enum CatSize {
    SMALL(0.7F),
    NORMAL(1.0F),
    LARGE(1.35F);

    private final float scale;

    CatSize(float scale) {
        this.scale = scale;
    }

    public float scale() {
        return scale;
    }

    public String serializedName() {
        return name().toLowerCase(Locale.ROOT);
    }

    public static CatSize fromName(String name) {
        return CatSize.valueOf(name.toUpperCase(Locale.ROOT));
    }
}
