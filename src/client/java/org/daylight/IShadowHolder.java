package org.daylight;

import net.minecraft.client.renderer.entity.state.CatRenderState;
import net.minecraft.client.renderer.entity.state.EntityRenderState;

public interface IShadowHolder {
    float getShadowRadiusAccessor(CatRenderState state);

    float getShadowOpacityAccessor(CatRenderState state);
}
