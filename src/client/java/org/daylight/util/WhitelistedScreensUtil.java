package org.daylight.util;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import org.daylight.config.ConfigHandler;
import org.daylight.replacements.FabricSetConfigValue;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

public class WhitelistedScreensUtil {
    private static final Set<Class<?>> whitelistedScreens = new HashSet<>();

    public static void initDefaultWhitelistedScreens() {
        ConfigHandler.whitelistedScreenNames.set(new ArrayList<>());
        whitelistedScreens.clear();

        whitelistedScreens.add(InventoryScreen.class);
        whitelistedScreens.add(CreativeModeInventoryScreen.class);

        Optional<Class<? extends Screen>> screen = findScreenClass("de.hysky.skyblocker.skyblock.item.SkyblockInventoryScreen");
        screen.ifPresent(whitelistedScreens::add);
    }

    public static Optional<Class<? extends Screen>> findScreenClass(String name) {
        try {
            return Optional.of(
                    Class.forName(name).asSubclass(Screen.class)
            );
        } catch (ClassNotFoundException | ClassCastException e) {
            return Optional.empty();
        }
    }

    public static void whitelistScreen(Screen screen) {
        if(whitelistedScreens.contains(screen.getClass())) return;
        whitelistedScreens.add(screen.getClass());

        serializeClassNames();
        ConfigHandler.CONFIG.save();
    }

    public static void unwhitelistScreen(Screen screen) {
        if(!whitelistedScreens.contains(screen.getClass())) return;
        whitelistedScreens.remove(screen.getClass());

        serializeClassNames();
        ConfigHandler.CONFIG.save();
    }

    public static boolean isWhitelisted(Screen screen) {
        return whitelistedScreens.contains(screen.getClass());
    }

    public static void deserializeClassNames() {
        whitelistedScreens.clear();
        ConfigHandler.whitelistedScreenNames.get().forEach(name -> {
            Optional<Class<? extends Screen>> screen = findScreenClass(name);
            screen.ifPresent(whitelistedScreens::add);
        });
    }

    public static void serializeClassNames() {
        ConfigHandler.whitelistedScreenNames.get().clear();
        whitelistedScreens.forEach(screen -> {
            ConfigHandler.whitelistedScreenNames.getCached().add(screen.getName());
        });
    }

    public static void toggleScreen(Screen screen) {
        if(screen == null) return;
        if(isWhitelisted(screen)) unwhitelistScreen(screen);
        else whitelistScreen(screen);
    }
}