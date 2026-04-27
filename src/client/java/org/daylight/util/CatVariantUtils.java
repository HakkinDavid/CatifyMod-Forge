package org.daylight.util;

import java.util.Locale;

public class CatVariantUtils {
    public static String serializeVariant(String variantIdPath) {
        return variantIdPath.toUpperCase(Locale.ROOT);
    }

    public static String deserializeVariant(String name) {
        return name.toLowerCase(Locale.ROOT);
    }
}
