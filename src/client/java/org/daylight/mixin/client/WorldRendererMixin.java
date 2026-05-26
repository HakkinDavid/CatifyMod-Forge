package org.daylight.mixin.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import org.daylight.features.CatChargeFeatureRenderer;
import org.daylight.util.StateStorage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = LevelRenderer.class, remap = false)
public class WorldRendererMixin {
    @Inject(
            method = "renderLevel",
            at = @At("HEAD")
    )
    private void renderLevelHead(CallbackInfo ci) {
        // Clear old mapped states
        StateStorage.currentStates.clear();

        // Tick global cat charge progress
        if (!Minecraft.getInstance().isPaused()) {
            CatChargeFeatureRenderer.moveGlobalTextureForward(Minecraft.getInstance().getDeltaTracker().getGameTimeDeltaTicks());
        }
    }
}
