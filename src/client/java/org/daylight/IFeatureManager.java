package org.daylight;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import com.mojang.blaze3d.vertex.PoseStack;
import org.daylight.features.CatChargeFeatureRenderer;

import java.util.function.Predicate;

public interface IFeatureManager {
    boolean catmodel$addFeature(RenderLayer<?, ?> feature);
    void renderAllFeatures(LivingEntityRenderState livingEntityRenderState, PoseStack matrixStack, MultiBufferSource vertexConsumerProvider, int i, Predicate<RenderLayer<?, ?>> filter);
    CatChargeFeatureRenderer getCatChargeFeatureRenderer();
}
