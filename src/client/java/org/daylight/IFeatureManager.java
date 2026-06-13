package org.daylight;

import net.minecraft.client.renderer.entity.layers.RenderLayer;
import org.daylight.features.CatChargeFeatureRenderer;

public interface IFeatureManager {
    boolean catmodel$addFeature(RenderLayer<?, ?> feature);
    CatChargeFeatureRenderer getCatChargeFeatureRenderer();
}