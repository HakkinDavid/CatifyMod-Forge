package org.daylight;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
import org.daylight.util.PlayerToCatReplacer;
import net.minecraft.world.entity.LivingEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class CatifyRuntimeEvents {

    @SubscribeEvent
    public static void onClientLogin(ClientPlayerNetworkEvent.LoggingIn event) {
        PlayerToCatReplacer.initWorld();
    }

    @SubscribeEvent
    public static void onClientLogout(ClientPlayerNetworkEvent.LoggingOut event) {
        PlayerToCatReplacer.cleanup();
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent.Pre event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return;

        // Make sure targetWorld is set
        PlayerToCatReplacer.initWorld();

        // 1. Add/Register all players loaded in the level
        for (net.minecraft.world.entity.player.Player player : mc.level.players()) {
            if (player instanceof AbstractClientPlayer clientPlayer) {
                PlayerToCatReplacer.replaceWithCat(clientPlayer);
            }
        }

        // 2. Clean up players that left (to avoid memory leak)
        List<UUID> toRemove = new ArrayList<>();
        for (UUID uuid : PlayerToCatReplacer.getDummyModelMap().keySet()) {
            if (mc.level.getPlayerByUUID(uuid) == null) {
                toRemove.add(uuid);
            }
        }

        for (UUID uuid : toRemove) {
            LivingEntity cat = PlayerToCatReplacer.getDummyModelMap().remove(uuid);
            if (cat != null) {
                cat.discard();
            }
        }
    }
}
