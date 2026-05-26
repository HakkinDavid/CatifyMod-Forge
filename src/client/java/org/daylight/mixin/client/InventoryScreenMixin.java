package org.daylight.mixin.client;

import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.world.entity.LivingEntity;
import org.daylight.util.StateStorage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(InventoryScreen.class)
public class InventoryScreenMixin {
    @Inject(remap = false, 
            method = "extractRenderState(Lnet/minecraft/world/entity/LivingEntity;)Lnet/minecraft/client/renderer/entity/state/EntityRenderState;",
            at = @At("RETURN")
    )
    private static void captureState(
            LivingEntity entity,
            CallbackInfoReturnable<EntityRenderState> cir
    ) {
        EntityRenderState state = cir.getReturnValue();

        if (state instanceof AvatarRenderState) {
            StateStorage.currentStates.put(state, entity.getUUID());
        }
    }
}
