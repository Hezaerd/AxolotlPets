package com.hezaerd.mixin;

import com.hezaerd.accessor.AxolotlShoulderAccessor;
import com.hezaerd.accessor.AxolotlTameableAccessor;
import net.minecraft.entity.Bucketable;
import net.minecraft.entity.EntityStatuses;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.passive.AxolotlEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import java.util.Optional;

@Mixin(value = AxolotlEntity.class, priority = 1500)
public abstract class AxolotlInteractionsMixin extends AnimalEntity {

    @Shadow public abstract boolean isBreedingItem(ItemStack stack);

    @Shadow protected abstract void eat(PlayerEntity player, Hand hand, ItemStack stack);

    protected AxolotlInteractionsMixin(EntityType<? extends AnimalEntity> entityType, World world) {
        super(entityType, world);
    }
    
    @Override
    public ActionResult interactMob(PlayerEntity player, Hand hand) {
        ItemStack itemStack = player.getStackInHand(hand);
        
        AxolotlTameableAccessor tameableAccessor = (AxolotlTameableAccessor) this;
        AxolotlShoulderAccessor shoulderAccessor = (AxolotlShoulderAccessor) this;

        // Taming related interactions
        if (tameableAccessor.betteraxolotls$isTamed()) {
            // Owner only interactions
            boolean isOwner = tameableAccessor.betteraxolotls$getOwner() == player;
            if (isOwner) {
                Optional<ActionResult> result = Bucketable.tryBucket(player, hand, (AxolotlEntity)(Object)this);
                if (result.isPresent()) {
                    return result.get();
                }
                
                // Empty-hand interactions
                if (itemStack.isEmpty() && shoulderAccessor.betteraxolotls$isReadyToSitOnPlayer() && !this.isBaby() && !player.isSubmergedInWater()) {
                    if (!this.getWorld().isClient) {
                        ServerPlayerEntity serverPlayer = (ServerPlayerEntity) player;
                        if (shoulderAccessor.betteraxolotls$mountOnto(serverPlayer)) {
                            SoundEvent soundEvent = this.isSubmergedInWater() ?
                                    SoundEvents.ENTITY_AXOLOTL_SPLASH :
                                    SoundEvents.ENTITY_BAT_TAKEOFF;
                            this.getWorld().playSound(
                                    null,
                                    this.getX(), this.getY(), this.getZ(),
                                    soundEvent,
                                    this.getSoundCategory(),
                                    1.0F,
                                    1.0F + (this.getWorld().random.nextFloat() - this.getWorld().random.nextFloat()) * 0.2F
                            );
                            return ActionResult.SUCCESS_SERVER;
                        } else {
                            return ActionResult.FAIL;
                        }
                    }
                }
            }
        } else {
            Optional<ActionResult> result = Bucketable.tryBucket(player, hand, (AxolotlEntity)(Object)this);
            if (result.isPresent()) {
                return result.get();
            }
            
            if (!this.getWorld().isClient && itemStack.isIn(ItemTags.AXOLOTL_FOOD) && tameableAccessor.betteraxolotls$isTameable()) {
                itemStack.decrementUnlessCreative(1, player);
                if (!this.isSilent()) {
                    SoundEvent soundEvent = this.isSubmergedInWater() ? 
                            SoundEvents.ENTITY_AXOLOTL_IDLE_WATER : 
                            SoundEvents.ENTITY_AXOLOTL_IDLE_AIR;
                    
                    this.getWorld().playSound(
                            null, 
                            this.getX(), this.getY(), this.getZ(),
                            soundEvent,
                            this.getSoundCategory(),
                            1.0F,
                            1.0F + (this.getWorld().random.nextFloat() - this.getWorld().random.nextFloat()) * 0.2F
                    );
                }
                tameableAccessor.betteraxolotls$tryTame(player);
                return ActionResult.SUCCESS_SERVER;
            }
        }
        
        // Breeding interaction
        if (this.isBreedingItem(itemStack)) {
            int breedingAge = this.getBreedingAge();
            if (!this.getWorld().isClient && breedingAge == 0 && this.canEat()) {
                this.eat(player, hand, itemStack);
                this.lovePlayer(player);
                this.playEatSound();
                return ActionResult.SUCCESS_SERVER;
            }
            
            if (this.isBaby()) {
                this.eat(player, hand, itemStack);
                this.growUp(toGrowUpAge(-breedingAge), true);
                this.playEatSound();
                return ActionResult.SUCCESS;
            }
            
            if (this.getWorld().isClient) {
                return ActionResult.CONSUME;
            }
        }
        
        return super.interactMob(player, hand);
    }
    
    @Override
    public void handleStatus(byte status) {
        if (status == EntityStatuses.ADD_POSITIVE_PLAYER_REACTION_PARTICLES) {
            this.showEmoteParticle(true);
        } else if (status == EntityStatuses.ADD_NEGATIVE_PLAYER_REACTION_PARTICLES) {
            this.showEmoteParticle(false);
        } else {
            super.handleStatus(status);
        }
    }
    
    @Unique
    private void showEmoteParticle(boolean positive) {
        ParticleEffect particleEffect = ParticleTypes.HEART;
        if (!positive) {
            particleEffect = ParticleTypes.SMOKE;
        }

        for (int i = 0; i < 7; i++) {
            double d = this.random.nextGaussian() * 0.02;
            double e = this.random.nextGaussian() * 0.02;
            double f = this.random.nextGaussian() * 0.02;
            this.getWorld().addParticleClient(particleEffect, this.getParticleX(1.0), this.getRandomBodyY() + 0.5, this.getParticleZ(1.0), d, e, f);
        }
    }
}
