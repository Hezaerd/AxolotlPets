package com.hezaerd.mixin;

import com.hezaerd.accessor.AxolotlShoulderAccessor;
import com.hezaerd.accessor.AxolotlTameableAccessor;
import com.hezaerd.registry.tag.ModItemTags;
import net.minecraft.entity.Bucketable;
import net.minecraft.entity.EntityStatuses;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
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

        AxolotlTameableAccessor tameable = (AxolotlTameableAccessor) this;
        AxolotlShoulderAccessor shoulder = (AxolotlShoulderAccessor) this;

        // Poisonous food interaction
        if (tryPoisonousFood(player, hand, itemStack)) return ActionResult.SUCCESS_SERVER;

        // Tamed interactions
        if (tameable.betteraxolotls$isTamed()) {
            if (tameable.betteraxolotls$getOwner() == player) {
                Optional<ActionResult> bucketResult = tryBucket(player, hand);
                if (bucketResult.isPresent()) return bucketResult.get();

                if (tryShoulderMount(player, itemStack, shoulder)) return ActionResult.SUCCESS_SERVER;
            }
        } else {
            Optional<ActionResult> bucketResult = tryBucket(player, hand);
            if (bucketResult.isPresent()) return bucketResult.get();

            if (tryTame(player, itemStack, tameable)) return ActionResult.SUCCESS_SERVER;
        }

        // Breeding interaction
        if (tryBreed(player, hand, itemStack)) return ActionResult.SUCCESS_SERVER;

        return super.interactMob(player, hand);
    }

    @Unique
    private boolean tryPoisonousFood(PlayerEntity player, Hand hand, ItemStack itemStack) {
        if (!this.getWorld().isClient && itemStack.isIn(ModItemTags.AXOLOTL_POISONOUS_FOOD) && this.canEat()) {
            this.eat(player, hand, itemStack);
            this.addStatusEffect(new StatusEffectInstance(StatusEffects.POISON, 600));
            return true;
        }
        return false;
    }

    @Unique
    private Optional<ActionResult> tryBucket(PlayerEntity player, Hand hand) {
        return Bucketable.tryBucket(player, hand, (AxolotlEntity) (Object) this);
    }

    @Unique
    private boolean tryShoulderMount(PlayerEntity player, ItemStack itemStack, AxolotlShoulderAccessor shoulder) {
        if (itemStack.isEmpty() && shoulder.betteraxolotls$isReadyToSitOnPlayer() && !this.isBaby() && !player.isSubmergedInWater()) {
            if (!this.getWorld().isClient) {
                ServerPlayerEntity serverPlayer = (ServerPlayerEntity) player;
                if (shoulder.betteraxolotls$mountOnto(serverPlayer)) {
                    playMountSound();
                    return true;
                }
            }
        }
        return false;
    }

    @Unique
    private void playMountSound() {
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
    }

    @Unique
    private boolean tryTame(PlayerEntity player, ItemStack itemStack, AxolotlTameableAccessor tameable) {
        if (!this.getWorld().isClient && itemStack.isIn(ItemTags.AXOLOTL_FOOD) && tameable.betteraxolotls$isTameable()) {
            itemStack.decrementUnlessCreative(1, player);
            playTameSound();
            tameable.betteraxolotls$tryTame(player);
            return true;
        }
        return false;
    }

    @Unique
    private void playTameSound() {
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
    }

    @Unique
    private boolean tryBreed(PlayerEntity player, Hand hand, ItemStack itemStack) {
        if (this.isBreedingItem(itemStack)) {
            int breedingAge = this.getBreedingAge();
            if (!this.getWorld().isClient && breedingAge == 0 && this.canEat()) {
                this.eat(player, hand, itemStack);
                this.lovePlayer(player);
                this.playEatSound();
                return true;
            }
            if (this.isBaby()) {
                this.eat(player, hand, itemStack);
                this.growUp(toGrowUpAge(-breedingAge), true);
                this.playEatSound();
                return true;
            }
            return this.getWorld().isClient; // Will return CONSUME below
        }
        return false;
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
