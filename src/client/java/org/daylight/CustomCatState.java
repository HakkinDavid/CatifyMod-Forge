package org.daylight;

import net.minecraft.world.entity.Entity;

import java.util.UUID;

public interface CustomCatState {
    float catmodel$getChargeProgress();
    void catmodel$setChargeProgress(float value);
//    Float catmodel$getCustomTimeDelta();
//    void catmodel$setCustomTimeDelta(Float value);
    boolean catmodel$getChargeActive();
    void catmodel$setChargeActive(boolean value);
    boolean catmodel$isMainSpecialCat();
    void catmodel$setAsMainSpecialCat(boolean value);
    UUID catmodel$getCurrentEntityId();
    void catmodel$setCurrentEntityId(UUID entity);
}