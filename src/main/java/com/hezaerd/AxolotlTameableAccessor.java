package com.hezaerd;

import net.minecraft.entity.LazyEntityReference;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.player.PlayerEntity;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public interface AxolotlTameableAccessor {
    boolean betteraxolotls$isTamed();
    boolean betteraxolotls$isTameable();
    void betteraxolotls$setTamed(boolean tamed, boolean updateAttributes);
    void betteraxolotls$setTamedBy(PlayerEntity player);
    void betteraxolotls$tryTame(PlayerEntity player);
    
    
    TrackedData<Optional<LazyEntityReference<LivingEntity>>> betteraxolotls$getOwnerReferenceData();
    @Nullable LazyEntityReference<LivingEntity> betteraxolotls$getOwnerReference();
    @Nullable LivingEntity betteraxolotls$getOwner();
    void betteraxolotls$setOwner(@Nullable LivingEntity owner);
    
    boolean betteraxolotls$isSitting();
    void betteraxolotls$setSitting(boolean sitting);
    boolean betteraxolotls$isInSittingPose();
    void betteraxolotls$setInSittingPose(boolean inSittingPose);
}