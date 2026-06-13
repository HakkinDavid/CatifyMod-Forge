package org.daylight;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.KeyMapping;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.Window;
import net.minecraft.resources.Identifier;
import org.daylight.util.WhitelistedScreensUtil;

public class ModKeyBindings {
    private static final KeyMapping.Category CATEGORY = KeyMapping.Category.register(Identifier.fromNamespaceAndPath(CatifyMod.MOD_ID, "main_category"));

    public static KeyMapping CHANGE_SCREEN_WHITELIST_STATE;

    private static boolean prevWhitelistDown = false;

    public static void register() {
        CHANGE_SCREEN_WHITELIST_STATE = KeyBindingHelper.registerKeyBinding(new KeyMapping(
                "key." + CatifyModClient.MOD_ID + ".change_screen_whitelist_state",
                InputConstants.Type.KEYSYM,
                InputConstants.UNKNOWN.getValue(),
                CATEGORY
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client == null || client.getWindow() == null) return;
            Window window = client.getWindow();

            InputConstants.Key whitelistScreenKey = KeyBindingHelper.getBoundKeyOf(CHANGE_SCREEN_WHITELIST_STATE);
            if (whitelistScreenKey.getType() == InputConstants.Type.KEYSYM) {
                if(whitelistScreenKey.getValue() != -1) {
                    boolean down = InputConstants.isKeyDown(window, whitelistScreenKey.getValue());

                    if (down && !prevWhitelistDown) {
                        WhitelistedScreensUtil.toggleScreen(Minecraft.getInstance().screen);
                    }
                    prevWhitelistDown = down;
                }
            }
        });
    }
}