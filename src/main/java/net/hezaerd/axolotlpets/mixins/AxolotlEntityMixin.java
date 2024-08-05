package net.hezaerd.axolotlpets.mixins;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.hezaerd.axolotlpets.AxolotlEntityAccessor;
import net.hezaerd.axolotlpets.goals.AxolotlFollowOwnerGoal;
import net.hezaerd.axolotlpets.goals.AxolotlSitOnOwnerShoulderGoal;
import net.hezaerd.axolotlpets.item.ModItems;
import net.hezaerd.axolotlpets.utils.Log;
import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.entity.Bucketable;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.passive.AxolotlEntity;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.server.ServerConfigHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;
import java.util.UUID;

@Mixin(value = AxolotlEntity.class, priority = 1001)
public abstract class AxolotlEntityMixin extends AnimalEntity implements AxolotlEntityAccessor {
    @Unique
    private static final TrackedData<Byte> TAMEABLE_FLAGS = DataTracker.registerData(AxolotlEntity.class, TrackedDataHandlerRegistry.BYTE);
    @Unique
    private static final TrackedData<Optional<UUID>> OWNER_UUID = DataTracker.registerData(AxolotlEntity.class, TrackedDataHandlerRegistry.OPTIONAL_UUID);
    @Unique
    private boolean sitting;

    private static final int READY_TO_SIT_ON_SHOULDER_COOLDOWN = 100; // 5 seconds
    private int ticks;

    protected AxolotlEntityMixin(EntityType<? extends AnimalEntity> entityType, World world) {
        super(entityType, world);
    }

    @Inject(method = "initDataTracker", at = @At("TAIL"))
    protected void initDataTracker(CallbackInfo ci, @Local(argsOnly = true) DataTracker.Builder builder) {
        builder.add(TAMEABLE_FLAGS, (byte)0);
        builder.add(OWNER_UUID, Optional.empty());
    }

    protected void initGoals() {
        this.goalSelector.add(11, new AxolotlSitOnOwnerShoulderGoal((AxolotlEntity)(Object)this));
        this.goalSelector.add(12, new AxolotlFollowOwnerGoal((AxolotlEntity)(Object)this, 0.7, 10, 2, false));
    }

    @Inject(method = "writeCustomDataToNbt", at = @At("TAIL"))
    protected void writeCustomDataToNbt(NbtCompound nbt, CallbackInfo ci) {
        if (this.getOwnerUuid() != null) {
            nbt.putUuid("Owner", this.getOwnerUuid());
        }

        nbt.putBoolean("Sitting", this.sitting);
    }

    @Inject(method = "readCustomDataFromNbt", at = @At("TAIL"))
    protected void readCustomDataFromNbt(NbtCompound nbt, CallbackInfo ci) {
        UUID uUID;

        if (nbt.containsUuid("Owner")) {
            uUID = nbt.getUuid("Owner");
        } else {
            String ownerName = nbt.getString("Owner");
            uUID = ServerConfigHandler.getPlayerUuidByName(this.getServer(), ownerName);
        }

        if (uUID != null) {
            try {
                this.setOwnerUuid(uUID);
                this.setTamed(true);
            } catch (Throwable e) {
                this.setTamed(false);
                Log.w("Failed to get owner UUID: " + e.getMessage());
            }
        }

        this.sitting = nbt.getBoolean("Sitting");
        this.setSitting(this.sitting);
    }

    @Inject(method = "method_57305", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/passive/AxolotlEntity;getBrain()Lnet/minecraft/entity/ai/brain/Brain;", shift = At.Shift.BEFORE))
    public void addOwnerToBucketNbt(NbtCompound nbt, CallbackInfo ci) {
        if(this.getOwnerUuid() != null) nbt.putUuid("Owner", getOwnerUuid());
    }

    @Inject(method = "copyDataFromNbt", at = @At(value = "TAIL"))
    public void copyOwnerFromBucketNbt(NbtCompound nbt, CallbackInfo ci) {
        if(nbt.containsUuid("Owner")) {
            setOwnerUuid(nbt.getUuid("Owner"));
            setTamed(true);
        } else {
            setTamed(false);
        }
    }

    @Inject(method = "createChild", at = @At("TAIL"))
    private void addOwnerTagToChild(ServerWorld world, PassiveEntity entity, CallbackInfoReturnable<PassiveEntity> cir) {
        PassiveEntity child = cir.getReturnValue();
        if (this.isTamed())
            ((AxolotlEntityMixin)child).setOwnerUuid(this.getOwnerUuid());
    }

    @Unique
    public void setTamed(boolean tamed) {
        byte b = this.dataTracker.get(TAMEABLE_FLAGS);
        if (tamed) {
            this.dataTracker.set(TAMEABLE_FLAGS, (byte)(b | 4)); // 0000 0100
        } else {
            this.dataTracker.set(TAMEABLE_FLAGS, (byte)(b & -5)); // 1111 1011
        }

        this.onTamed(tamed);
    }

    @Unique
    public boolean isTamed() {
        return (this.dataTracker.get(TAMEABLE_FLAGS) & 4) != 0; // 0000 0100
    }

    @Unique
    protected void onTamed(boolean tamed) {
        Log.i("Axolotl " + this.getId() + " is now " + (tamed ? "tamed" : "wild"));
    }

    @Unique
    private void setSitting(boolean sitting) {
        byte b = this.dataTracker.get(TAMEABLE_FLAGS);
        if (sitting) {
            this.dataTracker.set(TAMEABLE_FLAGS, (byte)(b | 1)); // 0000 0001
            this.sitting = true;
        } else {
            this.dataTracker.set(TAMEABLE_FLAGS, (byte)(b & -2)); // 1111 1110
            this.sitting = false;
        }
    }

    @Unique
    public boolean isSitting() {
        return (this.dataTracker.get(TAMEABLE_FLAGS) & 1) != 0; // 0000 0001
    }

    @Unique
    @Nullable
    public UUID getOwnerUuid() {
        return (UUID)((Optional)this.dataTracker.get(OWNER_UUID)).orElse(null);
    }

    @Unique
    public void setOwnerUuid(@Nullable UUID uuid) {
        this.dataTracker.set(OWNER_UUID, Optional.ofNullable(uuid));
    }

    @Unique
    public void setOwner(PlayerEntity player) {
        this.setTamed(true);
        this.setOwnerUuid(player.getUuid());

        if (player instanceof ServerPlayerEntity) {
            Criteria.TAME_ANIMAL.trigger((ServerPlayerEntity)player, (AxolotlEntity)(Object)this);
        }
    }

    @Unique
    public @Nullable LivingEntity axolotlpets$getOwner() {
        UUID uUID = this.getOwnerUuid();
        if(this.getWorld() == null)
            return null;
        return uUID == null ? null : this.getWorld().getPlayerByUuid(uUID);
    }

    @Unique
    public boolean isOwner(PlayerEntity player) {
        return player == this.axolotlpets$getOwner();
    }

    @ModifyReturnValue(method = "cannotDespawn", at = @At("RETURN"))
    public boolean cannotDespawn(boolean original) {
        return original | this.isTamed(); // if tamed, cannot despawn
    }

    public void onDeath(DamageSource damageSource) {
        boolean showDeathMessages = this.getWorld().getGameRules().getBoolean(GameRules.SHOW_DEATH_MESSAGES);
        LivingEntity owner = this.axolotlpets$getOwner();
        if (!this.getWorld().isClient && showDeathMessages && owner instanceof ServerPlayerEntity) {
            owner.sendMessage(this.getDamageTracker().getDeathMessage());
        }
    }

    public boolean isBreedingItem(ItemStack itemStack) {
        return itemStack.isIn(ItemTags.AXOLOTL_FOOD) || itemStack.isOf(Items.TROPICAL_FISH);
    }

    @Unique
    public boolean isTamingItem(ItemStack itemStack) {
        return itemStack.isOf(ModItems.AXOLOTL_TREAT);
    }

    public ActionResult interactMob(PlayerEntity player, Hand hand) {
        Optional<ActionResult> result = Bucketable.tryBucket(player, hand, (AxolotlEntity)(Object)this);

        if(result.isPresent())
            return result.get();

        ItemStack itemStack = player.getStackInHand(hand);
        Item item = itemStack.getItem();


        if (this.getWorld().isClient && (!this.isBaby() || !this.isBreedingItem(itemStack))) {
            Log.i("CLIENT - Axolotl " + this.getId() + " is being interacted by " + player.getName().getString());
            boolean bl = this.isOwner(player) || this.isTamed() || (this.isTamingItem(itemStack) && !this.isTamed());
            return bl ? ActionResult.CONSUME : ActionResult.PASS;
        }

        Log.i("SERVER - Axolotl " + this.getId() + " is being interacted by " + player.getName().getString());

        if (this.isBreedingItem(itemStack)) {
            Log.i("SERVER - Axolotl " + this.getId() + " is being fed by " + player.getName().getString());
            Log.i("SERVER - Axolotl " + this.getId() + " is being fed with " + itemStack.getItem().getTranslationKey() + " | isBreedingItem: " + this.isBreedingItem(itemStack));

            if (itemStack.isOf(Items.TROPICAL_FISH_BUCKET)) {
                itemStack = new ItemStack(Items.BUCKET);
            } else {
                itemStack.decrementUnlessCreative(1, player);
            }

            if (this.getBreedingAge() == 0 && this.canEat()) {
                Log.i("SERVER - Axolotl " + this.getId() + " is in love mode");

                this.eat(player, player.getActiveHand(), itemStack);
                this.lovePlayer(player);
                return ActionResult.success(this.getWorld().isClient);
            }

            if (this.isBaby()) {
                Log.i("SERVER - Axolotl " + this.getId() + " growing up");

                this.eat(player, player.getActiveHand(), itemStack);
                this.growUp((int) ((float) (-this.getBreedingAge() / 20) * 0.1F), true);
                return ActionResult.success(this.getWorld().isClient);
            }

            if (this.isTamed() && this.getHealth() < this.getMaxHealth()) {
                Log.i("SERVER - Axolotl " + this.getId() + " is being fed to heal");
                this.eat(player, player.getActiveHand(), itemStack);
                this.heal(7.0F);
                return ActionResult.success(this.getWorld().isClient);
            }

            return ActionResult.PASS;
        }

        if (this.isTamingItem(itemStack) && !this.isTamed()) {
            itemStack.decrementUnlessCreative(1, player);
            boolean success = this.tryTame(player);
            this.tamingParticles(success);
            return ActionResult.SUCCESS;
        }

        if (itemStack.isEmpty() && this.isOwner(player)) {
            if(player.isSneaking()) {
                Log.i("SERVER - " + player.getName().getString() + " is picking up Axolotl " + this.getId());
                if(this.axolotlpets$mountOnto((ServerPlayerEntity) player)) {
                    Log.i("SERVER - " + player.getName().getString() + " successfully picked up Axolotl " + this.getId());
                    return ActionResult.SUCCESS;
                }
            } else {
                this.setSitting(!this.isSitting());
                Log.i("SERVER - Axolotl " + this.getId() + " is now " + (this.isSitting() ? "sitting" : "standing"));
                return ActionResult.SUCCESS;
            }
        }

        return super.interactMob(player, hand);
    }

    public void tick() {
        this.ticks++;
        super.tick();
    }

    @Unique
    public boolean tryTame(PlayerEntity player) {
        Log.i("Player " + player.getName().getString() + " is trying to tame Axolotl " + this.getId());

        if (this.random.nextInt(3) == 0) {
            this.setOwner(player);
            this.navigation.stop();
            this.setTarget((LivingEntity)null);
            this.getWorld().sendEntityStatus((AxolotlEntity)(Object)this, (byte)7);
            return true;
        } else {
            this.getWorld().sendEntityStatus((AxolotlEntity)(Object)this, (byte)6);
            return false;
        }
    }

    @Unique
    public void tamingParticles(boolean success) {
        ParticleEffect particleEffect = success ? ParticleTypes.HEART : ParticleTypes.SMOKE;
        for (int i = 0; i < 7; ++i) {
            double d = this.random.nextGaussian() * 0.02D;
            double e = this.random.nextGaussian() * 0.02D;
            double f = this.random.nextGaussian() * 0.02D;
            this.getWorld().addParticle(particleEffect, this.getParticleX(1.0D), this.getRandomBodyY() + 0.5D, this.getParticleZ(1.0D), d, e, f);
        }
    }

    @Override
    public boolean axolotlpets$mountOnto(ServerPlayerEntity player) {
        NbtCompound nbtCompound = new NbtCompound();
        nbtCompound.putString("id", this.getSavedEntityId());
        this.writeNbt(nbtCompound);
        if(player.addShoulderEntity(nbtCompound)) {
            Log.i("Axolotl " + this.getId() + " is now sitting on " + player.getName().getString());
            this.discard();
            return true;
        } else {
            Log.i("Axolotl " + this.getId() + " failed to sit on " + player.getName().getString());
            return false;
        }
    }

    @Override
    public boolean axolotlpets$isReadyToSitOnPlayer() {
        return this.ticks > READY_TO_SIT_ON_SHOULDER_COOLDOWN;
    }

    @Override
    public boolean axolotlpets$cannotFollowOwner() {
        LivingEntity owner = this.axolotlpets$getOwner();
        return this.isSitting() || this.hasVehicle() || this.mightBeLeashed() || owner != null && owner.isSpectator();
    }

    @Override
    public boolean axolotlpets$shouldTryTeleportToOwner() {
        LivingEntity owner = this.axolotlpets$getOwner();
        return owner != null && (this.squaredDistanceTo(owner) >= 144.0D); // 12 blocks
    }
}

