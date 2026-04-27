package org.daylight;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
import org.daylight.util.WhitelistedScreensUtil;

public class ModKeyBindings {
    public static KeyMapping CHANGE_SCREEN_WHITELIST_STATE;

    public static void register() {
        MinecraftForge.EVENT_BUS.register(ModKeyBindings.class);
    }

    @SubscribeEvent
    public static void onRegisterKeyMappings(RegisterKeyMappingsEvent event) {
        KeyMapping.Category category = KeyMapping.Category.register(
                Identifier.fromNamespaceAndPath(CatifyMod.MOD_ID, "main_category")
        );

        CHANGE_SCREEN_WHITELIST_STATE = new KeyMapping(
                "key." + CatifyModClient.MOD_ID + ".change_screen_whitelist_state",
                InputConstants.Type.KEYSYM,
                InputConstants.UNKNOWN.getValue(),
                category
        );

        event.register(CHANGE_SCREEN_WHITELIST_STATE);
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent.Post event) {
        if (CHANGE_SCREEN_WHITELIST_STATE == null) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        while (CHANGE_SCREEN_WHITELIST_STATE.consumeClick()) {
            WhitelistedScreensUtil.toggleScreen(mc.screen);
        }
    }
}
