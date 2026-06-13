package org.daylight;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLevelEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLevelEvents;
import net.minecraft.client.Minecraft;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.world.entity.player.Player;
import org.daylight.network.CatVariantSyncPayload;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.world.entity.LivingEntity;
import org.daylight.config.ConfigHandler;
import org.daylight.features.CatChargeFeatureRenderer;
import org.daylight.util.CatSkinManager;
import org.daylight.util.PlayerToCatReplacer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

@Environment(EnvType.CLIENT)
public class CatifyModClient implements ClientModInitializer {
	public static final String MOD_ID = "catify";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
	private static final Minecraft client = Minecraft.getInstance();
    AtomicBoolean checked = new AtomicBoolean(false);

	@Override
	public void onInitializeClient() {
        ModCommands.register();
        ModResources.init();
        ModKeyBindings.register();
        ConfigHandler.init();
        CatSkinManager.init();
        OwnResourceReloadListener.register();

        ClientPlayNetworking.registerGlobalReceiver(CatVariantSyncPayload.TYPE, (payload, context) -> {
            context.client().execute(() -> {
                CatVariantSyncPayload.playerVariants.put(payload.playerUuid(), payload);
                Player player = PlayerToCatReplacer.getPlayerById(payload.playerUuid());
                if (player != null) {
                    PlayerToCatReplacer.setupAppearanceForPlayer(player, payload.variant(), payload.isVanilla(), payload.size());
                }
            });
        });

		ClientLevelEvents.AFTER_CLIENT_LEVEL_CHANGE.register((client, level) -> {
			if (level != null) {
                PlayerToCatReplacer.initWorld();
                checked.set(false);
			}
		});

        ClientTickEvents.START_CLIENT_TICK.register(client -> {
            if (client.level == null) return;

            PlayerToCatReplacer.initWorld();

            // Replace all client players with cats
            for (Player player : client.level.players()) {
                if (player instanceof AbstractClientPlayer clientPlayer) {
                    PlayerToCatReplacer.replaceWithCat(clientPlayer);
                }
            }

            // Cleanup entities of players who left
            List<UUID> toRemove = new java.util.ArrayList<>();
            for (UUID uuid : PlayerToCatReplacer.getDummyModelMap().keySet()) {
                if (client.level.getPlayerByUUID(uuid) == null) {
                    toRemove.add(uuid);
                }
            }

            for (UUID uuid : toRemove) {
                LivingEntity cat = PlayerToCatReplacer.getDummyModelMap().remove(uuid);
                if (cat != null) {
                    cat.discard();
                }
            }
        });

		ServerLevelEvents.UNLOAD.register((server, level) -> {
			PlayerToCatReplacer.cleanup();
		});

		// Синхронизация каждый тик
//		ClientTickEvents.START_CLIENT_TICK.register(client -> {
//			if (client.player != null) {
//				PlayerToCatReplacer.syncEntities();
//			}
//		});

//        WorldRenderEvents.START.register(context -> {
//            if(!Minecraft.getInstance().isPaused()) {
//                CatChargeFeatureRenderer.moveGlobalTextureForward(context.tickCounter().getTickProgress(true));
//            }
//        });
	}
}