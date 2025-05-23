package com.hezaerd.mixin;

import com.hezaerd.accessor.AxolotlTameableAccessor;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.entity.EntityStatuses;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LazyEntityReference;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.passive.AxolotlEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Optional;

@Mixin(value = AxolotlEntity.class, priority = 1500)
public abstract class AxolotlTameableMixin extends AnimalEntity implements AxolotlTameableAccessor {
    @Shadow public abstract int getMaxAir();

    @Unique private static final TrackedData<Byte> TAMEABLE_FLAGS
            = DataTracker.registerData(AxolotlEntity.class, TrackedDataHandlerRegistry.BYTE);
    @Unique private static final TrackedData<Optional<LazyEntityReference<LivingEntity>>> OWNER_UUID
            = DataTracker.registerData(AxolotlEntity.class, TrackedDataHandlerRegistry.LAZY_ENTITY_REFERENCE);
    
    @Unique private boolean sitting;
    
    @Unique private final double untamedMaxHealth = 14.0D;
    @Unique private final double tamedMaxHealth = 40.0D;
    
    protected AxolotlTameableMixin(EntityType<? extends AnimalEntity> entityType, World world) {
        super(entityType, world);
    }
    
    @Inject(method = "initDataTracker", at = @At("TAIL"))
    protected void initDataTracker(CallbackInfo ci, @Local(argsOnly = true) DataTracker.Builder builder) {
        builder.add(TAMEABLE_FLAGS, (byte)0);
        builder.add(OWNER_UUID, Optional.empty());
    }

    @Override
    public boolean betteraxolotls$isTamed() {
        return (this.getDataTracker().get(TAMEABLE_FLAGS) & 4) != 0;
    }
    
    @Override 
    public boolean betteraxolotls$isTameable() {
        boolean isBaby = this.isBaby();
        boolean enoughAir = this.getAir() > this.getMaxAir() / 2;
        boolean isTamed = this.betteraxolotls$isTamed();
        boolean canEat = this.canEat();
        
        return !isBaby && enoughAir && !isTamed && canEat;
    }
    
    @Override
    public void betteraxolotls$tryTame(PlayerEntity player) {
        if (this.random.nextInt(5) == 0) {
            this.betteraxolotls$setTamedBy(player);
            this.navigation.stop();
            this.setTarget(null);
            this.betteraxolotls$setSitting(true);
            this.getWorld().sendEntityStatus((AxolotlEntity)(Object)this, EntityStatuses.ADD_POSITIVE_PLAYER_REACTION_PARTICLES);
        } else {
            this.getWorld().sendEntityStatus((AxolotlEntity)(Object)this, EntityStatuses.ADD_NEGATIVE_PLAYER_REACTION_PARTICLES);
        }
    }
    
    @Override 
    public void betteraxolotls$setTamed(boolean tamed, boolean updateAttributes) {
        byte b = this.dataTracker.get(TAMEABLE_FLAGS);
        if (tamed) {
            this.dataTracker.set(TAMEABLE_FLAGS, (byte)(b | 4));
        } else {
            this.dataTracker.set(TAMEABLE_FLAGS, (byte)(b & -5));
        }
        
        if (updateAttributes) {
            this.updateAttributesForTamed();
        }
    }
    
    @Override
    public void betteraxolotls$setTamedBy(PlayerEntity player) {
        this.betteraxolotls$setTamed(true, true);
        this.betteraxolotls$setOwner(player);
        if (player instanceof ServerPlayerEntity serverPlayerEntity) {
            Criteria.TAME_ANIMAL.trigger(serverPlayerEntity, (AxolotlEntity) (Object) this);
        }
    }
    
    @Unique
    private void updateAttributesForTamed() {
        if (this.betteraxolotls$isTamed()) {
            EntityAttributeInstance maxHealthInstance = this.getAttributeInstance(EntityAttributes.MAX_HEALTH);
            if (maxHealthInstance != null) {
                maxHealthInstance.setBaseValue(this.tamedMaxHealth);
                this.setHealth((float)this.tamedMaxHealth);                
            }
        } else {
            EntityAttributeInstance maxHealthInstance = this.getAttributeInstance(EntityAttributes.MAX_HEALTH);
            if (maxHealthInstance != null) 
                maxHealthInstance.setBaseValue(this.untamedMaxHealth);
        }
    }
    
    @Override
    public TrackedData<Optional<LazyEntityReference<LivingEntity>>> betteraxolotls$getOwnerReferenceData() {
        return OWNER_UUID;
    }
    
    @Override
    public LazyEntityReference<LivingEntity> betteraxolotls$getOwnerReference() {
        return this.getDataTracker().get(OWNER_UUID).orElse(null);
    }
    
    @Override
    public LivingEntity betteraxolotls$getOwner() {
        return LazyEntityReference.resolve(this.betteraxolotls$getOwnerReference(), this.getWorld(), LivingEntity.class);
    }
    
    @Override
    public void betteraxolotls$setOwner(@Nullable LivingEntity owner) {
        this.dataTracker.set(OWNER_UUID, Optional.ofNullable(owner).map(LazyEntityReference::new));
    }
    
    @Override
    public boolean betteraxolotls$isSitting() {
        return this.sitting;
    }
    
    @Override
    public boolean betteraxolotls$isInSittingPose() {
        return (this.dataTracker.get(TAMEABLE_FLAGS) & 1) != 0;
    }
    
    @Override
    public void betteraxolotls$setSitting(boolean sitting) {
        this.sitting = sitting;
    }
    
    @Override
    public void betteraxolotls$setInSittingPose(boolean inSittingPose) {
        byte b = this.dataTracker.get(TAMEABLE_FLAGS);
        if (inSittingPose) {
            this.dataTracker.set(TAMEABLE_FLAGS, (byte)(b | 1));
        } else {
            this.dataTracker.set(TAMEABLE_FLAGS, (byte)(b & -2));
        }
    }
}
