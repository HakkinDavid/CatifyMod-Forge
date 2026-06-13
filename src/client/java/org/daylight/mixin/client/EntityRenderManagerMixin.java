package org.daylight.mixin.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.Options;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.CatRenderer;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.state.CatRenderState;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
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
import org.daylight.features.CatChargeFeatureRenderer;
import org.daylight.util.ModStateUtils;
import org.daylight.util.PlayerToCatReplacer;
import org.daylight.util.StateStorage;
import org.daylight.util.WhitelistedScreensUtil;
import org.daylight.config.ConfigHandler;
import org.joml.Quaternionf;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.UUID;

@Mixin(EntityRenderDispatcher.class)
public abstract class EntityRenderManagerMixin {
    @Shadow
    @Final
    public Options options;

    @Inject(
            method = "submit(Lnet/minecraft/client/renderer/entity/state/EntityRenderState;Lnet/minecraft/client/renderer/state/level/CameraRenderState;DDDLcom/mojang/blaze3d.vertex.PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;)V",
            at = @At("HEAD"),
            cancellable = true
    )
    private <S extends EntityRenderState> void onSubmit(
            S renderState,
            CameraRenderState cameraRenderState,
            double x, double y, double z,
            PoseStack matrices,
            SubmitNodeCollector submitNodeCollector,
            CallbackInfo ci
    ) throws IllegalAccessException {
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

                EntityRenderDispatcher dispatcher = (EntityRenderDispatcher)(Object)this;
                CatRenderer catRenderer;
                CatRenderState catState;
                EntityRenderer<?, ? super S> originalRenderer = dispatcher.getRenderer(renderState);
                float tickDelta = Minecraft.getInstance().getDeltaTracker().getGameTimeDeltaPartialTick(true);

                ci.cancel();
                PlayerToCatReplacer.syncEntity2(player, cat);

                catRenderer = (CatRenderer) dispatcher.getRenderer(cat);
                catState = catRenderer.createRenderState(cat, tickDelta);

                AvatarRenderState updatedPlayerState = (AvatarRenderState) dispatcher.getRenderer(player).createRenderState(player, tickDelta);
                if(ConfigHandler.catDamageVisible.getCached()) catState.hasRedOverlay = updatedPlayerState.hasRedOverlay;
                else catState.hasRedOverlay = false;

                catState.lightCoords = playerState.lightCoords;

                if(StateStorage.inventoryRelativeHeadYaw != null) catState.yRot = StateStorage.inventoryRelativeHeadYaw;
                if(StateStorage.inventoryBodyYaw != null) catState.bodyRot = StateStorage.inventoryBodyYaw;
                if(StateStorage.inventoryPitch != null) catState.xRot = StateStorage.inventoryPitch;

                CatChargeFeatureRenderer.getChargeData(cat).chargeActive = false;

                try {
                    matrices.pushPose();
                    Vec3 vec3d = originalRenderer.getRenderOffset(renderState);
                    matrices.translate(x + vec3d.x(), y + vec3d.y(), z + vec3d.z());
                    if(catRenderer instanceof CustomCatTextureHolder customCatTextureHolder) {
                        if(customCatTextureHolder.catModel$shouldUpdateCustomTexture()) {
                            catRenderer.createRenderState(cat, 0);
                        }
                    }

                    if(ModStateUtils.shouldRenderCat(player)) {
                        catRenderer.submit(catState, matrices, submitNodeCollector, cameraRenderState);
                    }

                    if (renderState.displayFireAnimation) {
                        submitNodeCollector.submitFlame(matrices, renderState, Mth.rotationAroundAxis(Mth.Y_AXIS, cameraRenderState.orientation, new Quaternionf()));
                    }

                    if (!renderState.shadowPieces.isEmpty()) {
                        submitNodeCollector.submitShadow(matrices, renderState.shadowRadius, renderState.shadowPieces);
                    }
                } catch (ClassCastException e) {
                    CatifyModClient.LOGGER.error("The renderer is most likely not a EntityRenderer<Cat, EntityRenderState>", e);
                } finally {
                    matrices.popPose();
                }

                if(ModStateUtils.shouldRenderCharge(player)) {
                    if(catRenderer instanceof IFeatureManager featureManager) {
                        if(catState == null) catState = catRenderer.createRenderState(cat, tickDelta);

                        matrices.pushPose();

                        try {
                            float bodyYaw = catState.bodyRot;
                            matrices.translate(x, y, z);
                            matrices.mulPose(Axis.YP.rotationDegrees(180.0F - bodyYaw));
                            matrices.scale(-1.0F, -1.0F, 1.0F);
                            matrices.translate(0.0f, -1.501f, 0.0f);

                            CatChargeFeatureRenderer.getChargeData(cat).chargeActive = true;

                            featureManager.getCatChargeFeatureRenderer().customRender(matrices, submitNodeCollector, catState.lightCoords, catState, catState.yRot, catState.xRot);
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
}