package net.hezaerd.axolotlpets;

import net.hezaerd.axolotlpets.utils.Log;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.model.AxolotlEntityModel;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.entity.model.EntityModelLoader;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.passive.AxolotlEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Identifier;

public class ShoulderAxolotlFeatureRenderer<T extends PlayerEntity> extends FeatureRenderer<T, PlayerEntityModel<T>> {
    private final AxolotlEntityModel model;

    public ShoulderAxolotlFeatureRenderer(FeatureRendererContext<T, PlayerEntityModel<T>> context, EntityModelLoader loader) {
        super(context);
        this.model = new AxolotlEntityModel(loader.getModelPart(EntityModelLayers.AXOLOTL));
    }

    @Override
    public void render(MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, T entity, float limbAngle, float limbDistance, float tickDelta, float animationProgress, float headYaw, float headPitch) {
        this.renderShoulderAxolotl(matrices, vertexConsumers, light, entity, limbAngle, limbDistance, headYaw, headPitch, true);
        this.renderShoulderAxolotl(matrices, vertexConsumers, light, entity, limbAngle, limbDistance, headYaw, headPitch, false);

    }

    private void renderShoulderAxolotl(
            MatrixStack matrices,
            VertexConsumerProvider vertexConsumers,
            int light,
            T player,
            float limbAngle,
            float limbDistance,
            float headYaw,
            float headPitch,
            boolean leftShoulder
    ) {
        NbtCompound nbtCompound = leftShoulder ? player.getShoulderEntityLeft() : player.getShoulderEntityRight();
        EntityType.get(nbtCompound.getString("id")).filter(type -> type == EntityType.AXOLOTL).ifPresent(type -> {

            Log.i("Rendering axolotl on shoulder" + (leftShoulder ? "left" : "right"));

            matrices.push();

            // Scale down the axolotl
            matrices.scale(0.8F, 0.8F, 0.8F);

            // Offset the axolotl to the shoulder
            matrices.translate(leftShoulder ? 0.4D : -0.4D, player.isInSneakingPose() ? -1.15D : -1.45D, 0.0D);


            // TODO: GET THE TEXTURE FROM AxolotlEntityRenderer
            AxolotlEntity.Variant variant = AxolotlEntity.Variant.byId(nbtCompound.getInt("Variant"));
            Identifier texture = Identifier.ofVanilla("textures/entity/axolotl/axolotl_lucy.png"); // TEMP: Use the lucy texture
            VertexConsumer vertexConsumer = vertexConsumers.getBuffer(this.model.getLayer(texture));

            // Render the axolotl
            ((AxolotlEntityModelAccessor)(Object)model).axolotlpets$poseOnShoulder(matrices, vertexConsumer, light, OverlayTexture.DEFAULT_UV, limbAngle, limbDistance, headYaw, headPitch);

            matrices.pop();
        });
    }
}
