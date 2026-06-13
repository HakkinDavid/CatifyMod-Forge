package org.daylight;

import net.minecraft.resources.Identifier;

public interface CustomCatTextureHolder {
    Identifier catModel$getCustomTexture();
    void catModel$setCustomTexture(Identifier texture);
    boolean catModel$shouldUpdateCustomTexture();
    void catModel$requestCustomTextureUpdate();

//    float catmodel$getChargeProgress();
//
//    void catmodel$setChargeProgress(float value);
}