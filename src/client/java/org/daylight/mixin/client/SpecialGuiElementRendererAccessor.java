package org.daylight.mixin.client;

import net.minecraft.client.gui.render.pip.PictureInPictureRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import org.daylight.IElementWVertexConsumerProvider;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(value = PictureInPictureRenderer.class, remap = false)
public class SpecialGuiElementRendererAccessor implements IElementWVertexConsumerProvider {
    @Shadow
    @Final
    protected MultiBufferSource.BufferSource bufferSource;

    public MultiBufferSource.BufferSource getVertexConsumers() {
        return this.bufferSource;
    }
}
