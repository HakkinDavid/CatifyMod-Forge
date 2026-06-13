package org.daylight;

import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.state.CatRenderState;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import com.mojang.blaze3d.vertex.PoseStack;

public interface IRenderableFeature<S extends EntityRenderState> {
    void catify$render(PoseStack matrices, SubmitNodeCollector queue, int light, CatRenderState state, float limbAngle, float limbDistance);
}