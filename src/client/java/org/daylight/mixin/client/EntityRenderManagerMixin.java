package org.daylight.mixin.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.Options;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.CatRenderer;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.client.renderer.entity.state.*;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.feline.Cat;
import net.minecraft.world.entity.player.Player;
import net.minecraft.util.Mth;
import com.mojang.math.Axis;
import net.minecraft.world.phys.Vec3;
import org.daylight.CatifyModClient;
import org.daylight.CustomCatTextureHolder;
import org.daylight.IFeatureManager;
import org.daylight.config.ConfigHandler;
import org.daylight.features.CatChargeFeatureRenderer;
import org.daylight.util.ModStateUtils;
import org.daylight.util.PlayerToCatReplacer;
import org.daylight.util.StateStorage;
import org.daylight.util.WhitelistedScreensUtil;
import org.joml.Quaternionf;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.UUID;

@Mixin(value = EntityRenderDispatcher.class, remap = false)
public abstract class EntityRenderManagerMixin {

    @Shadow
    public abstract <S extends EntityRenderState> EntityRenderer<?, ? super S> getRenderer(S state);

    @Shadow
    @Final
    public Options options;

    @Inject(remap = false, 
            method = "submit(Lnet/minecraft/client/renderer/entity/state/EntityRenderState;Lnet/minecraft/client/renderer/state/level/CameraRenderState;DDDLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;)V",
            at = @At("HEAD"),
            cancellable = true
    )
    private <S extends EntityRenderState> void onSubmit(S renderState, CameraRenderState cameraRenderState, double x, double y, double z, PoseStack matrices, SubmitNodeCollector collector, CallbackInfo ci) throws IllegalAccessException {
        if(!ConfigHandler.replacementActive.getCached()) return;

        Screen screen = Minecraft.getInstance().screen;
        if(StateStorage.currentlyRenderingUi && screen != null && !WhitelistedScreensUtil.isWhitelisted(screen)) return;

        if(renderState instanceof AvatarRenderState playerState) {
            if(StateStorage.currentStates.containsKey(playerState)) {
                UUID playerUuid = StateStorage.currentStates.get(playerState);
                if(playerUuid == null) return;
                Player player = PlayerToCatReplacer.getPlayerById(playerUuid);
                if(player == null) return;
                Cat cat = (Cat) PlayerToCatReplacer.getCatForPlayer(player);
                if(cat == null) return;

                CatRenderer catRenderer;
                CatRenderState catState;
                EntityRenderer<?, ? super S> originalRenderer = this.getRenderer(renderState);
                float tickDelta = Minecraft.getInstance().getDeltaTracker().getGameTimeDeltaTicks();

                ci.cancel();
                PlayerToCatReplacer.syncEntity2(player, cat);

                catRenderer = (CatRenderer) this.getRenderer(cat);
                catState = catRenderer.createRenderState(cat, tickDelta);

                if(playerState == null) playerState = (AvatarRenderState) getRenderer(player).createRenderState(player, tickDelta);
                if(ConfigHandler.catDamageVisible.getCached()) catState.hasRedOverlay = playerState.hasRedOverlay;
                else catState.hasRedOverlay = false;

                catState.lightCoords = playerState.lightCoords;

                if(StateStorage.inventoryRelativeHeadYaw != null) catState.yRot = StateStorage.inventoryRelativeHeadYaw;
                if(StateStorage.inventoryBodyYaw != null) catState.bodyRot = StateStorage.inventoryBodyYaw;
                if(StateStorage.inventoryPitch != null) catState.xRot = StateStorage.inventoryPitch;

                CatChargeFeatureRenderer.getChargeData(cat).chargeActive = false;

                try {
                    matrices.pushPose();
                    Vec3 vec3d = originalRenderer.getRenderOffset(renderState);
                    matrices.translate(x, y, z);
                    if(catRenderer instanceof CustomCatTextureHolder customCatTextureHolder) {
                        if(customCatTextureHolder.catModel$shouldUpdateCustomTexture()) {
                            catRenderer.createRenderState(cat, 0);
                        }
                    }
                    ci.cancel();

                    if(ModStateUtils.shouldRenderCat(player)) {
                        catRenderer.submit(catState, matrices, collector, cameraRenderState);
                    }

                } catch (ClassCastException e) {
                    CatifyModClient.LOGGER.error("The renderer is most likely not a EntityRenderer<Cat, CatRenderState>", e);
                } finally {
                    matrices.popPose();
                }

                if(ModStateUtils.shouldRenderCharge(player)) {
                    if(catRenderer instanceof IFeatureManager featureManager) {
                        if(catState == null) catState = (CatRenderState) catRenderer.createRenderState(cat, tickDelta);

                        matrices.pushPose();

                        try {
                            float bodyYaw = catState.bodyRot;
                            matrices.translate(x, y, z);
                            matrices.mulPose(Axis.YP.rotationDegrees(180.0F - bodyYaw));
                            matrices.scale(-1.0F, -1.0F, 1.0F);
                            matrices.translate(0.0f, -1.501f, 0.0f);

                            CatChargeFeatureRenderer.getChargeData(cat).chargeActive = true;

                            featureManager.getCatChargeFeatureRenderer().customRender(matrices, collector, catState.lightCoords, catState, catState.yRot, catState.xRot);
                        } catch (Throwable e) {
                            e.printStackTrace();
                        } finally {
                            matrices.popPose();
                        }
                    }
                }
            }
        }
    }

    @Shadow
    public abstract <T extends Entity> EntityRenderer getRenderer(T entity);
}