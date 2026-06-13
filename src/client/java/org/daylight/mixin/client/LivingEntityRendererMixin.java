package org.daylight.mixin.client;

import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.world.entity.LivingEntity;
import org.daylight.IFeatureManager;
import org.daylight.features.CatChargeFeatureRenderer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.List;

@Mixin(LivingEntityRenderer.class)
public abstract class LivingEntityRendererMixin<T extends LivingEntity, S extends LivingEntityRenderState, M extends EntityModel<? super S>> implements IFeatureManager {
    @Shadow
    protected abstract boolean addLayer(RenderLayer<S, M> feature);

    @Shadow
    @Final
    protected List<RenderLayer<S, M>> layers;

    @Override
    public boolean catmodel$addFeature(RenderLayer<?, ?> feature) {
        return addLayer((RenderLayer<S, M>) feature);
    }

    @Override
    public CatChargeFeatureRenderer getCatChargeFeatureRenderer() {
        for(RenderLayer<S, M> featureRenderer : layers) {
            if(featureRenderer instanceof CatChargeFeatureRenderer) return (CatChargeFeatureRenderer) featureRenderer;
        }
        return null;
    }
}