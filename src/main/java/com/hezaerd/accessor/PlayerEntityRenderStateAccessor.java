package com.hezaerd.accessor;

import net.minecraft.entity.passive.AxolotlEntity;
import org.jetbrains.annotations.Nullable;

public interface PlayerEntityRenderStateAccessor {
    @Nullable
    AxolotlEntity.Variant betteraxolotls$getLeftShoulderAxolotlVariant();

    void betteraxolotls$setLeftShoulderAxolotlVariant(@Nullable AxolotlEntity.Variant variant);

    @Nullable
    AxolotlEntity.Variant betteraxolotls$getRightShoulderAxolotlVariant();

    void betteraxolotls$setRightShoulderAxolotlVariant(@Nullable AxolotlEntity.Variant variant);
}
