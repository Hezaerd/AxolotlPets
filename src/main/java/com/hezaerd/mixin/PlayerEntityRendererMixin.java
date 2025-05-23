package com.hezaerd.mixin;

import com.hezaerd.accessor.PlayerEntityRenderStateAccessor;
import com.hezaerd.render.ShoulderAxolotlFeatureRenderer;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.client.render.entity.state.PlayerEntityRenderState;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.passive.AxolotlEntity;
import net.minecraft.nbt.NbtCompound;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = PlayerEntityRenderer.class, priority = 1001)
public abstract class PlayerEntityRendererMixin extends LivingEntityRenderer<AbstractClientPlayerEntity, PlayerEntityRenderState, PlayerEntityModel> {
    protected PlayerEntityRendererMixin(EntityRendererFactory.Context ctx, PlayerEntityModel model, float shadowRadius) {
        super(ctx, model, shadowRadius);
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    private void betteraxolotls$PlayerEntityRendererMixin(EntityRendererFactory.Context ctx, boolean slim, CallbackInfo ci) {
        this.addFeature(new ShoulderAxolotlFeatureRenderer(this, ctx.getEntityModels()));
    }
    
    @Inject(method = "updateRenderState(Lnet/minecraft/client/network/AbstractClientPlayerEntity;Lnet/minecraft/client/render/entity/state/PlayerEntityRenderState;F)V", at = @At("TAIL"))
    private void updateAxolotlShoulderVariants(AbstractClientPlayerEntity player, PlayerEntityRenderState state, float tickDelta, CallbackInfo ci) {
        PlayerEntityRenderStateAccessor accessor = (PlayerEntityRenderStateAccessor)state;
        
        accessor.betteraxolotls$setLeftShoulderAxolotlVariant(getShoulderAxolotlVariant(player, true));
        accessor.betteraxolotls$setRightShoulderAxolotlVariant(getShoulderAxolotlVariant(player, false));
    }
    
    @Unique
    @Nullable
    private static AxolotlEntity.Variant getShoulderAxolotlVariant(AbstractClientPlayerEntity player, boolean left) {
        NbtCompound nbtCompound = left ? player.getShoulderEntityLeft() : player.getShoulderEntityRight();
        if (nbtCompound.isEmpty()) {
            return null;
        } else {
            EntityType<?> entityType = nbtCompound.get("id", EntityType.CODEC).orElse(null);
            return entityType == EntityType.AXOLOTL
                    ? nbtCompound.get("Variant", AxolotlEntity.Variant.CODEC).orElse(AxolotlEntity.Variant.LUCY)
                    : null;
        }
    }
}
