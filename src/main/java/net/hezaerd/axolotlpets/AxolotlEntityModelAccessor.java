package net.hezaerd.axolotlpets;

import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.util.math.MatrixStack;

public interface AxolotlEntityModelAccessor {
    void axolotlpets$poseOnShoulder(
        MatrixStack matrices,
        VertexConsumer vertexConsumer,
        int light,
        int overlay,
        float limbAngle,
        float limbDistance,
        float headYaw,
        float headPitch
    );
}
