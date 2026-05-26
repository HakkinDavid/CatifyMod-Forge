package org.daylight;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.ChannelBuilder;
import net.minecraftforge.network.SimpleChannel;
import net.minecraftforge.network.PacketDistributor;
import net.minecraft.resources.Identifier;
import org.daylight.network.CatVariantSyncPayload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Mod(CatifyMod.MOD_ID)
public class CatifyMod {
	public static final String MOD_ID = "catify";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	public static final Map<UUID, CatVariantSyncPayload> serverPlayerVariants = new HashMap<>();

	public static SimpleChannel INSTANCE;

	public CatifyMod() {
		// Initialize networking channel
		INSTANCE = ChannelBuilder.named(Identifier.fromNamespaceAndPath(MOD_ID, "network"))
			.networkProtocolVersion(1)
			.acceptedVersions((status, version) -> true)
			.simpleChannel();

		INSTANCE.messageBuilder(CatVariantSyncPayload.class)
			.codec(CatVariantSyncPayload.CODEC)
			.consumerMainThread((payload, context) -> {
				if (context.isServerSide()) {
					// Server side: cache and broadcast to other players
					serverPlayerVariants.put(payload.playerUuid(), payload);
					net.minecraft.server.level.ServerPlayer sender = context.getSender();
					if (sender != null && sender.level() instanceof net.minecraft.server.level.ServerLevel serverLevel) {
						for (net.minecraft.server.level.ServerPlayer other : serverLevel.getServer().getPlayerList().getPlayers()) {
							if (other != sender) {
								INSTANCE.send(payload, other.connection.getConnection());
							}
						}
					}
				} else {
					// Client side: save and apply
					CatVariantSyncPayload.playerVariants.put(payload.playerUuid(), payload);
					net.minecraft.world.entity.player.Player player = org.daylight.util.PlayerToCatReplacer.getPlayerById(payload.playerUuid());
					if (player != null) {
						org.daylight.util.PlayerToCatReplacer.setupAppearanceForPlayer(player, payload.variant(), payload.isVanilla(), payload.size());
					}
				}
			})
			.add();

		// Register ourselves on both event busses
		PlayerEvent.PlayerLoggedInEvent.BUS.addListener(CatifyMod::onPlayerLoggedIn);
	}

	public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
		if (event.getEntity() instanceof net.minecraft.server.level.ServerPlayer serverPlayer) {
			for (CatVariantSyncPayload payload : serverPlayerVariants.values()) {
				INSTANCE.send(payload, serverPlayer.connection.getConnection());
			}
		}
	}
}
