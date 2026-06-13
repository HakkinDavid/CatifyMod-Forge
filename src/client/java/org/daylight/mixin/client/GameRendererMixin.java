package org.daylight.mixin.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.DeltaTracker;
import org.daylight.features.CatChargeFeatureRenderer;
import org.daylight.util.StateStorage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public class GameRendererMixin {
    @Inject(
            method = "render",
            at = @At("HEAD")
    )
    private void beforeRender(DeltaTracker deltaTracker, boolean tick, CallbackInfo ci) {
        if(!Minecraft.getInstance().isPaused()) {
            CatChargeFeatureRenderer.moveGlobalTextureForward(deltaTracker.getGameTimeDeltaPartialTick(true));
        }
    }

    @Inject(
            method = "render",
            at = @At("TAIL")
    )
    private void afterRender(DeltaTracker deltaTracker, boolean tick, CallbackInfo ci) {
        StateStorage.currentStates.clear();
        StateStorage.currentlyRenderingUi = false;
    }
}