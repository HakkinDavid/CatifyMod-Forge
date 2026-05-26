package org.daylight.features;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.rendertype.RenderType;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.layers.EnergySwirlLayer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.model.animal.feline.AdultCatModel;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.entity.state.CatRenderState;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.world.entity.Entity;
import net.minecraft.resources.Identifier;
import org.daylight.CustomCatState;
import org.daylight.IRenderableFeature;
import org.daylight.config.Data;
import org.daylight.mixin.client.FeatureRendererAccessor;
import org.daylight.util.PlayerToCatReplacer;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@OnlyIn(Dist.CLIENT)
public class CatChargeFeatureRenderer<S extends EntityRenderState>
        extends EnergySwirlLayer<CatRenderState, AdultCatModel> {
    private final Identifier texture;
    private final AdultCatModel model;

    public static class CatChargeData {
        public boolean chargeActive = false;
        public float chargeProgress = 0f;
        public Float customDelta = null;
    }

    public static final Map<UUID, CatChargeData> CHARGE_DATA = new HashMap<>();

    public static CatChargeData getChargeData(Entity entity) {
        return CHARGE_DATA.computeIfAbsent(entity.getUUID(), k -> new CatChargeData());
    }

    public CatChargeFeatureRenderer(
            RenderLayerParent<CatRenderState, AdultCatModel> context,
            EntityModelSet loader,
            Identifier texture
    ) {
        super(context);
        this.texture = texture;
        this.model = new AdultCatModel(loader.bakeLayer(ModelLayers.CAT));
    }

    @Override
    protected boolean isPowered(CatRenderState state) {
        if(state instanceof CustomCatState customCatState) {
            UUID entityId = customCatState.catmodel$getCurrentEntityId();
            if (entityId == null) return false;
            Entity entity = PlayerToCatReplacer.findAsCat(entityId);
            if (entity == null) return false;

            CatChargeData data = getChargeData(entity);
            return data.chargeActive;
        }
        return false;
    }

    public static float globalProgress = 0;
    public static void moveGlobalTextureForward(float tickDelta) {
        globalProgress += tickDelta * 0.008f;
        if(globalProgress >= 8000f) {
            globalProgress = 0f;
        }
    }

    @Override
    protected float xOffset(float partialAge) {
        return globalProgress;
    }

    public void customRender(PoseStack matrices, SubmitNodeCollector queue, int light, CatRenderState state, float limbAngle, float limbDistance) {
        internalRender(matrices, queue, light, state, limbAngle, limbDistance);
    }

    private void internalRender(PoseStack matrices, SubmitNodeCollector queue, int light, CatRenderState state, float limbAngle, float limbDistance) {
        if(state instanceof CatRenderState catEntityRenderState && this instanceof IRenderableFeature<?> renderableFeature) {
            if (this.isPowered(catEntityRenderState)) {
                AdultCatModel entityModel = this.model();
                entityModel.setupAnim(catEntityRenderState);
                ((IRenderableFeature<CatRenderState>) renderableFeature).catify$render(matrices, queue, light, state, limbAngle, limbDistance);
            }
        }
    }

    @Override
    protected Identifier getTextureLocation() {
        return texture;
    }

    @Override
    protected AdultCatModel model() {
        return model;
    }
}
