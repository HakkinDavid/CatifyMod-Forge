package org.daylight.features;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.layers.EnergySwirlLayer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.model.animal.feline.AbstractFelineModel;
import net.minecraft.client.model.animal.feline.AdultCatModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.entity.state.CatRenderState;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.world.entity.Entity;
import net.minecraft.resources.Identifier;
import org.daylight.CustomCatState;
import org.daylight.IRenderableFeature;
import org.daylight.util.PlayerToCatReplacer;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Environment(EnvType.CLIENT)
public class CatChargeFeatureRenderer<S extends EntityRenderState>
        extends EnergySwirlLayer<CatRenderState, AbstractFelineModel<CatRenderState>> {
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
            RenderLayerParent<CatRenderState, AbstractFelineModel<CatRenderState>> context,
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

    private CatRenderState currentState = null;

    public static float globalProgress = 0;
    public static void moveGlobalTextureForward(float tickDelta) {
        globalProgress += tickDelta * 0.008f;
        if(globalProgress >= 8000f) {
            globalProgress = 0f;
        }
    }

    @Override
    public float xOffset(float partialAge) {
        if(true) return globalProgress;
        if(currentState instanceof CustomCatState customCatState) {
            UUID entityId = customCatState.catmodel$getCurrentEntityId();
            if (entityId == null) return 0f;
            Entity entity = PlayerToCatReplacer.findAsCat(entityId);
            if (entity == null) return 0f;

            CatChargeData data = getChargeData(entity);
            if (data.customDelta != null) {
                data.chargeProgress += data.customDelta * 0.008f;
            } else {
                data.chargeProgress += partialAge * 0.008f;
            }
            return data.chargeProgress;
        } return 0;
    }

    public void customRender(PoseStack matrices, SubmitNodeCollector queue, int light, CatRenderState state, float limbAngle, float limbDistance) {
        internalRender(matrices, queue, light, state, limbAngle, limbDistance);
    }

    private void internalRender(PoseStack matrices, SubmitNodeCollector queue, int light, CatRenderState state, float limbAngle, float limbDistance) {
        if(state instanceof CatRenderState catEntityRenderState && this instanceof IRenderableFeature<?> renderableFeature) {
            if (this.isPowered(catEntityRenderState)) {
                AbstractFelineModel<CatRenderState> entityModel = this.model();
                entityModel.setupAnim(catEntityRenderState);
                renderableFeature.catify$render(matrices, queue, light, state, limbAngle, limbDistance);
            }
        }
    }

    @Override
    public Identifier getTextureLocation() {
        return texture;
    }

    @Override
    protected AbstractFelineModel<CatRenderState> model() {
        return model;
    }
}
