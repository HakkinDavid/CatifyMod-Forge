package org.daylight;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.common.MinecraftForge;
import org.daylight.config.ConfigHandler;
import org.daylight.util.CatSkinManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Mod.EventBusSubscriber(modid = CatifyMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class CatifyModClient {
	public static final String MOD_ID = "catify";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@SubscribeEvent
	public static void onClientSetup(FMLClientSetupEvent event) {
        ModResources.init();
        ModCommands.register();
        ModKeyBindings.register();
        ConfigHandler.init();
        CatSkinManager.init();
        OwnResourceReloadListener.register();

        MinecraftForge.EVENT_BUS.register(CatifyRuntimeEvents.class);
	}
}
