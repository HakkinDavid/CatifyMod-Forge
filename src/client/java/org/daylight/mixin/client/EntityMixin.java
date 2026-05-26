package org.daylight.mixin.client;

import net.minecraft.world.entity.Entity;
import org.daylight.features.CatChargeFeatureRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Entity.class)
public class EntityMixin {
    @Inject(remap = false, method = "discard", at = @At("HEAD"))
    private void onDiscard(CallbackInfo ci) {
        CatChargeFeatureRenderer.CHARGE_DATA.remove(((Entity) (Object) this).getUUID());
    }
}
