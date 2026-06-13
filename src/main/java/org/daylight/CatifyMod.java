package org.daylight;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.level.ServerPlayer;
import org.daylight.network.CatVariantSyncPayload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CatifyMod implements ModInitializer {
	public static final String MOD_ID = "catify";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	public static final Map<UUID, CatVariantSyncPayload> serverPlayerVariants = new HashMap<>();

	@Override
	public void onInitialize() {
		// Register packet payload structures
		PayloadTypeRegistry.serverboundPlay().register(CatVariantSyncPayload.TYPE, CatVariantSyncPayload.CODEC);
		PayloadTypeRegistry.clientboundPlay().register(CatVariantSyncPayload.TYPE, CatVariantSyncPayload.CODEC);

		// Handle client variant sync payloads on the server
		ServerPlayNetworking.registerGlobalReceiver(CatVariantSyncPayload.TYPE, (payload, context) -> {
			serverPlayerVariants.put(payload.playerUuid(), payload);
			ServerPlayer sender = context.player();
			context.server().execute(() -> {
				for (ServerPlayer other : context.server().getPlayerList().getPlayers()) {
					if (other != sender) {
						ServerPlayNetworking.send(other, payload);
					}
				}
			});
		});

		// Synchronize cached variants to newly logged-in players
		ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
			for (CatVariantSyncPayload payload : serverPlayerVariants.values()) {
				ServerPlayNetworking.send(handler.player, payload);
			}
		});
	}
}