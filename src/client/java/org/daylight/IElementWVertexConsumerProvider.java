package org.daylight;

import net.minecraft.client.renderer.MultiBufferSource;

public interface IElementWVertexConsumerProvider {
    MultiBufferSource.BufferSource getVertexConsumers();
}