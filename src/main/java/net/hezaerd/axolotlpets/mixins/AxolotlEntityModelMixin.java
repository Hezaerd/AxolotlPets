package net.hezaerd.axolotlpets.mixins;

import net.hezaerd.axolotlpets.AxolotlEntityModelAccessor;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.entity.model.AxolotlEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

@Mixin(AxolotlEntityModel.class)
public abstract class AxolotlEntityModelMixin implements AxolotlEntityModelAccessor {

    @Shadow @Final private ModelPart head;

    @Shadow protected abstract void setAngles(ModelPart part, float pitch, float yaw, float roll);

    @Shadow @Final private ModelPart body;

    @Unique
    public void axolotlpets$poseOnShoulder(
        MatrixStack matrices,
        VertexConsumer vertexConsumer,
        int light,
        int overlay,
        float limbAngle,
        float limbDistance,
        float headYaw,
        float headPitch
    ) {
        this.setAngles(this.head, headPitch * (float) (Math.PI / 180), headYaw * (float) (Math.PI / 180), 0.0F);
        this.body.render(matrices, vertexConsumer, light, overlay);
    }
}
