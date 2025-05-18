package com.hezaerd.mixin;

import com.hezaerd.AxolotlTameableAccessor;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.entity.passive.AxolotlEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = AxolotlEntity.class, priority = 1500)
public abstract class AxolotlDespawnMixin {
    @ModifyReturnValue(method = "cannotDespawn", at = @At("RETURN"))
    public boolean cannotDespawn(boolean original) {
        return original | ((AxolotlTameableAccessor)this).betteraxolotls$isTamed();
    }
}
