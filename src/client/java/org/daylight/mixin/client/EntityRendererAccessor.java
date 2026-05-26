package org.daylight.mixin.client;

import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(EntityRenderer.class)
public interface EntityRendererAccessor {
    @Invoker(value = "getShadowRadius", remap = false)
    float callGetShadowRadius(EntityRenderState state);

    @Invoker(value = "getShadowStrength", remap = false)
    float callGetShadowOpacity(EntityRenderState state);
}
