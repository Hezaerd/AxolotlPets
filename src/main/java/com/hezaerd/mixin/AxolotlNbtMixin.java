package com.hezaerd.mixin;

import com.hezaerd.accessor.AxolotlTameableAccessor;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LazyEntityReference;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.passive.AxolotlEntity;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

@Mixin(value = AxolotlEntity.class, priority = 1500)
public abstract class AxolotlNbtMixin extends AnimalEntity {
    protected AxolotlNbtMixin(EntityType<? extends AnimalEntity> entityType, World world) {
        super(entityType, world);
    }
    
    @Inject(method = "writeCustomDataToNbt", at = @At("TAIL"))
    private void writeCustomDataToNbt(NbtCompound tag, CallbackInfo ci) {
        LazyEntityReference<LivingEntity> lazyEntityReference = ((AxolotlTameableAccessor)this).betteraxolotls$getOwnerReference();
        if (lazyEntityReference != null) {
            lazyEntityReference.writeNbt(tag, "Owner");
        }
    }
    
    @Inject(method = "readCustomDataFromNbt", at = @At("TAIL"))
    private void readCustomDataFromNbt(NbtCompound tag, CallbackInfo ci) {
        LazyEntityReference<LivingEntity> lazyEntityReference = LazyEntityReference.fromNbtOrPlayerName(tag, "Owner", this.getWorld());
        if (lazyEntityReference != null) {
            try {
                this.dataTracker.set(((AxolotlTameableAccessor)this).betteraxolotls$getOwnerReferenceData(), Optional.of(lazyEntityReference));
                ((AxolotlTameableAccessor)this).betteraxolotls$setTamed(true, false);
            }
            catch (IllegalArgumentException e) {
                ((AxolotlTameableAccessor)this).betteraxolotls$setTamed(false, true);
            }
        } else {
            this.dataTracker.set(((AxolotlTameableAccessor)this).betteraxolotls$getOwnerReferenceData(), Optional.empty());
            ((AxolotlTameableAccessor)this).betteraxolotls$setTamed(false, true);
        }
    }

    @Inject(method = "copyDataToStack", at = @At("TAIL"))
    public void addOwnerToBucketNbt(ItemStack stack, CallbackInfo ci) {
        NbtComponent.set(DataComponentTypes.BUCKET_ENTITY_DATA, stack, tag -> {
            LazyEntityReference<LivingEntity> lazyEntityReference = ((AxolotlTameableAccessor)this).betteraxolotls$getOwnerReference();
            if (lazyEntityReference != null) {
                lazyEntityReference.writeNbt(tag, "Owner");
            }
        });
    }
    
    @Inject(method = "copyDataFromNbt", at = @At("TAIL"))
    private void copyOwnerFromBucketNbt(NbtCompound tag, CallbackInfo ci) {
        LazyEntityReference<LivingEntity> ownerRef = LazyEntityReference.fromNbtOrPlayerName(tag, "Owner", this.getWorld());
        if (ownerRef != null) {
            this.dataTracker.set(
                    ((AxolotlTameableAccessor)this).betteraxolotls$getOwnerReferenceData(),
                    Optional.of(ownerRef)
            );
            ((AxolotlTameableAccessor)this).betteraxolotls$setTamed(true, false);
        } else {
            this.dataTracker.set(
                    ((AxolotlTameableAccessor)this).betteraxolotls$getOwnerReferenceData(),
                    Optional.empty()
            );
            ((AxolotlTameableAccessor)this).betteraxolotls$setTamed(false, true);
        }
    }
    
    @Inject(method = "createChild", at = @At("TAIL"))
    public void createChild(ServerWorld world, PassiveEntity entity, CallbackInfoReturnable<PassiveEntity> cir) {
        PassiveEntity child = cir.getReturnValue();
        if (((AxolotlTameableAccessor)this).betteraxolotls$isTamed()) {
            ((AxolotlTameableAccessor)child).betteraxolotls$setTamed(true, false);
            ((AxolotlTameableAccessor)child).betteraxolotls$setOwner(((AxolotlTameableAccessor)this).betteraxolotls$getOwner());
        }
    }
}
