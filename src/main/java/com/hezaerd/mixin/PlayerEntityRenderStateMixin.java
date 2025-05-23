package com.hezaerd.mixin;

import com.hezaerd.accessor.PlayerEntityRenderStateAccessor;
import net.minecraft.client.render.entity.state.PlayerEntityRenderState;
import net.minecraft.entity.passive.AxolotlEntity;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(PlayerEntityRenderState.class)
public class PlayerEntityRenderStateMixin implements PlayerEntityRenderStateAccessor {
    @Unique
    @Nullable
    public AxolotlEntity.Variant leftShoulderAxolotlVariant;
    
    @Unique
    @Nullable
    public AxolotlEntity.Variant rightShoulderAxolotlVariant;

    @Override
    public @Nullable AxolotlEntity.Variant betteraxolotls$getLeftShoulderAxolotlVariant() {
        return this.leftShoulderAxolotlVariant;
    }

    @Override
    public void betteraxolotls$setLeftShoulderAxolotlVariant(@Nullable AxolotlEntity.Variant variant) {
        this.leftShoulderAxolotlVariant = variant;
    }

    @Override
    public @Nullable AxolotlEntity.Variant betteraxolotls$getRightShoulderAxolotlVariant() {
        return this.rightShoulderAxolotlVariant;
    }

    @Override
    public void betteraxolotls$setRightShoulderAxolotlVariant(@Nullable AxolotlEntity.Variant variant) {
        this.rightShoulderAxolotlVariant = variant;
    }
}
