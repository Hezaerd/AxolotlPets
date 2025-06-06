package com.hezaerd.render;

import com.google.common.collect.Maps;
import com.hezaerd.accessor.PlayerEntityRenderStateAccessor;
import com.hezaerd.utils.Log;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.model.*;
import net.minecraft.client.render.entity.state.AxolotlEntityRenderState;
import net.minecraft.client.render.entity.state.PlayerEntityRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.passive.AxolotlEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;

import java.util.Locale;
import java.util.Map;

public class ShoulderAxolotlFeatureRenderer extends FeatureRenderer<PlayerEntityRenderState, PlayerEntityModel> {
    private final AxolotlEntityModel model;
    private final AxolotlEntityRenderState axolotlState = new AxolotlEntityRenderState();

    private static final Map<AxolotlEntity.Variant, Identifier> AXOLOTL_TEXTURES =
            Util.make(Maps.newHashMap(), variants -> {
                for (AxolotlEntity.Variant variant : AxolotlEntity.Variant.values()) {
                    variants.put(variant, Identifier.ofVanilla(String.format(Locale.ROOT, "textures/entity/axolotl/axolotl_" + variant.getId() + ".png")));
                }
            });
    
    public ShoulderAxolotlFeatureRenderer(FeatureRendererContext<PlayerEntityRenderState, PlayerEntityModel> context, LoadedEntityModels loader) {
        super(context);
        this.model = new AxolotlEntityModel(loader.getModelPart(EntityModelLayers.AXOLOTL));
    }

    @Override
    public void render(MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, PlayerEntityRenderState state, float limbAngle, float limbDistance) {
        PlayerEntityRenderStateAccessor accessor = (PlayerEntityRenderStateAccessor)state;
        
        AxolotlEntity.Variant leftVariant = accessor.betteraxolotls$getLeftShoulderAxolotlVariant();
        if (leftVariant != null) {
            this.render(matrices, vertexConsumers, light, state, leftVariant, limbAngle, limbDistance, true);
        }
        
        AxolotlEntity.Variant rightVariant = accessor.betteraxolotls$getRightShoulderAxolotlVariant();
        if (rightVariant != null) {
            this.render(matrices, vertexConsumers, light, state, rightVariant, limbAngle, limbDistance, false);
        }
    }
    
    private void render(MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, PlayerEntityRenderState state, AxolotlEntity.Variant variant, float limbAngle, float limbDistance, boolean isLeft) {
        matrices.push();

        // Position the axolotl on the shoulder
        matrices.translate(isLeft ? 0.4D : -0.4D, state.isInSneakingPose ? -0.48D : -0.73D, 0.0D);

        // Scale down the axolotl since they're quite large
        matrices.scale(0.6f, 0.6f, 0.6f);

        // Set up the axolotl render state
        this.axolotlState.age = state.age;
        this.axolotlState.limbSwingAnimationProgress = state.limbSwingAnimationProgress;
        this.axolotlState.limbSwingAmplitude = state.limbSwingAmplitude;
        this.axolotlState.variant = variant;
        this.axolotlState.playingDeadValue = 0.0f;
        this.axolotlState.isMovingValue = 0.0f;
        this.axolotlState.inWaterValue = 0.0f;
        this.axolotlState.onGroundValue = 1.0f;
        
        this.model.setAngles(this.axolotlState);
        
        RenderLayer layer = this.model.getLayer(getAxolotlTexture(variant));
        this.model.render(matrices, vertexConsumers.getBuffer(layer), light, OverlayTexture.DEFAULT_UV);
        
        matrices.pop();
    }
    
    private static Identifier getAxolotlTexture(AxolotlEntity.Variant variant) {
        return AXOLOTL_TEXTURES.get(variant);
    }
}
