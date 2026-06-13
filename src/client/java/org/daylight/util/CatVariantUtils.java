package org.daylight.util;

import net.minecraft.world.entity.animal.feline.CatVariant;
import net.minecraft.resources.ResourceKey;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;

import java.util.Locale;

public class CatVariantUtils {
    public static String serializeVariant(ResourceKey<CatVariant> key) {
        return key.identifier().getPath().toUpperCase(Locale.ROOT);
    }

    public static ResourceKey<CatVariant> deserializeVariant(String name) {
        String path = name.toLowerCase(Locale.ROOT);
        return ResourceKey.create(Registries.CAT_VARIANT, Identifier.parse(path));
    }
}