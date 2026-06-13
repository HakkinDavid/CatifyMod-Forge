package org.daylight.mixin.client;

import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.entity.state.CatRenderState;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import com.mojang.blaze3d.vertex.PoseStack;
import org.daylight.IRenderableFeature;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(RenderLayer.class)
public abstract class FeatureRendererAccessor<S extends EntityRenderState, M extends EntityModel<? super S>> implements IRenderableFeature<S> {
    @Shadow
    public abstract void submit(PoseStack matrices, SubmitNodeCollector queue, int light, S state, float limbAngle, float limbDistance);

    @Override
    public void catify$render(PoseStack matrices, SubmitNodeCollector queue, int light, CatRenderState state, float limbAngle, float limbDistance) {
        submit(matrices, queue, light, (S) state, limbAngle, limbDistance);
    }
}