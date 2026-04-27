package org.daylight;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
import org.daylight.util.PlayerToCatReplacer;

import java.util.concurrent.atomic.AtomicBoolean;

public final class CatifyRuntimeEvents {
    private static final AtomicBoolean checked = new AtomicBoolean(false);

    @SubscribeEvent
    public static void onClientLogin(ClientPlayerNetworkEvent.LoggingIn event) {
        PlayerToCatReplacer.initWorld();
        checked.set(false);
    }

    @SubscribeEvent
    public static void onClientLogout(ClientPlayerNetworkEvent.LoggingOut event) {
        PlayerToCatReplacer.cleanup();
        checked.set(false);
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent.Pre event) {
        if (checked.get()) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        PlayerToCatReplacer.replaceWithCat((AbstractClientPlayer) mc.player);
        checked.set(true);
    }
}

