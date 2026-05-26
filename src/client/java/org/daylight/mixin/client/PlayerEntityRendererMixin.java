package org.daylight.mixin.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.world.entity.player.Player;
import net.minecraft.resources.Identifier;
import org.daylight.ModResources;
import org.daylight.config.ConfigHandler;
import org.daylight.config.Data;
import org.daylight.util.CatVariantUtils;
import org.daylight.util.PlayerToCatReplacer;
import org.daylight.util.StateStorage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AvatarRenderer.class)
public abstract class PlayerEntityRendererMixin {
    @Inject(remap = false, 
            method = "extractRenderState(Lnet/minecraft/world/entity/Avatar;Lnet/minecraft/client/renderer/entity/state/AvatarRenderState;F)V",
            at = @At("HEAD")
    )
    public void onExtractRenderState(net.minecraft.world.entity.Avatar player, AvatarRenderState state, float f, CallbackInfo ci) {
        if(player instanceof AbstractClientPlayer clientPlayer) {
            StateStorage.currentStates.put(state, clientPlayer.getUUID());
        }
    }

    @ModifyArg(remap = false, 
            method = "renderRightHand",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/entity/player/AvatarRenderer;renderHand(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;ILnet/minecraft/resources/Identifier;Lnet/minecraft/client/model/geom/ModelPart;Z)V"),
            index = 3
    )
    private Identifier replaceRightArmSkin(Identifier skinTexture) {
        if (ConfigHandler.catHandActive.getCached() && ConfigHandler.replacementActive.getCached() &&
                PlayerToCatReplacer.shouldReplace(getPlayer())) {
            return getHandTexture(skinTexture);
        }
        return skinTexture;
    }

    @ModifyArg(remap = false, 
            method = "renderLeftHand",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/entity/player/AvatarRenderer;renderHand(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;ILnet/minecraft/resources/Identifier;Lnet/minecraft/client/model/geom/ModelPart;Z)V"),
            index = 3
    )
    private Identifier replaceLeftArmSkin(Identifier skinTexture) {
        if (ConfigHandler.catHandActive.getCached() && ConfigHandler.replacementActive.getCached()
                && PlayerToCatReplacer.shouldReplace(getPlayer())) {
            return getHandTexture(skinTexture);
        }
        return skinTexture;
    }

    @Unique
    private Identifier getHandTexture(Identifier defaultValue) {
        if(!ConfigHandler.catVariantVanilla.getCached()) {
            if(Data.catHandTexture != null) return Data.catHandTexture;
            else return defaultValue;
        }
        return ModResources.CAT_HAND_BY_VARIANT.get(CatVariantUtils.deserializeVariant(ConfigHandler.catVariant.getCached()));
    }

    @Unique
    private AbstractClientPlayer getPlayer() {
        return Minecraft.getInstance().player;
    }
}
