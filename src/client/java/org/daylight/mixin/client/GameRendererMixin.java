package org.daylight.mixin.client;

import net.minecraft.client.renderer.GameRenderer;
import org.daylight.util.StateStorage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public class GameRendererMixin {
    @Inject(remap = false, 
            method = "render",
            at = @At("TAIL")
    )
    private void afterRender(CallbackInfo ci) {
        StateStorage.currentStates.clear();
        StateStorage.currentlyRenderingUi = false;
    }
}
