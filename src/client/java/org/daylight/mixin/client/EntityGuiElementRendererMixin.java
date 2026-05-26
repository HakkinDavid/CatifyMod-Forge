package org.daylight.mixin.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.render.pip.PictureInPictureRenderer;
import net.minecraft.client.renderer.state.gui.pip.PictureInPictureRenderState;
import net.minecraft.client.renderer.state.gui.pip.GuiEntityRenderState;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.world.entity.EntityType;
import org.daylight.IElementWVertexConsumerProvider;
import org.daylight.config.ConfigHandler;
import org.daylight.util.StateStorage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = PictureInPictureRenderer.class, remap = false)
public class EntityGuiElementRendererMixin {

    @Inject(remap = false, 
            method = "renderToTexture(Lnet/minecraft/client/renderer/state/gui/pip/PictureInPictureRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;)V",
            at = @At("HEAD")
    )
    protected void renderStart(PictureInPictureRenderState state, PoseStack matrices, CallbackInfo ci) {
        if(Minecraft.getInstance().player == null) return;

        if (state instanceof GuiEntityRenderState guiEntityState && ConfigHandler.replacementActive.getCached() && guiEntityState.renderState().entityType == EntityType.PLAYER) {
            if ((Object) this instanceof IElementWVertexConsumerProvider
                    && (Object) guiEntityState.renderState() instanceof AvatarRenderState playerEntityRenderState) {
                StateStorage.inventoryRelativeHeadYaw = playerEntityRenderState.yRot;
                StateStorage.inventoryBodyYaw = playerEntityRenderState.bodyRot;
                StateStorage.inventoryPitch = playerEntityRenderState.xRot;
                StateStorage.currentlyRenderingUi = true;
            }
        }
    }

    @Inject(remap = false, 
            method = "renderToTexture(Lnet/minecraft/client/renderer/state/gui/pip/PictureInPictureRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;)V",
            at = @At("TAIL")
    )
    protected void renderEnd(PictureInPictureRenderState state, PoseStack matrices, CallbackInfo ci) {
        if (state instanceof GuiEntityRenderState) {
            StateStorage.currentlyRenderingUi = false;
        }
    }

    @Inject(remap = false, 
            method = "renderToTexture(Lnet/minecraft/client/renderer/state/gui/pip/PictureInPictureRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;)V",
            at = @At("TAIL")
    )
    protected void afterRender(PictureInPictureRenderState state, PoseStack matrices, CallbackInfo ci) {
        if (state instanceof GuiEntityRenderState) {
            StateStorage.inventoryRelativeHeadYaw = null;
            StateStorage.inventoryBodyYaw = null;
            StateStorage.inventoryPitch = null;
        }
    }
}
