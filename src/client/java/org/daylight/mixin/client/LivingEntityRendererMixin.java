package org.daylight.mixin.client;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.world.entity.LivingEntity;
import org.daylight.IFeatureManager;
import org.daylight.features.CatChargeFeatureRenderer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.List;
import java.util.function.Predicate;

@Mixin(value = LivingEntityRenderer.class, remap = false)
public abstract class LivingEntityRendererMixin<T extends LivingEntity, S extends LivingEntityRenderState, M extends EntityModel<S>> implements IFeatureManager {
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
    public void renderAllFeatures(LivingEntityRenderState livingEntityRenderState, PoseStack matrixStack, MultiBufferSource vertexConsumerProvider, int i, Predicate<RenderLayer<?, ?>> filter) {
        for(RenderLayer featureRenderer : layers) {
            // Unused but keep signature
        }
    }

    @Override
    public CatChargeFeatureRenderer getCatChargeFeatureRenderer() {
        for(RenderLayer featureRenderer : layers) {
            if(featureRenderer instanceof CatChargeFeatureRenderer) return (CatChargeFeatureRenderer) featureRenderer;
        }
        return null;
    }
}
