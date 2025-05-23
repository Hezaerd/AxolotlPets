package com.hezaerd.mixin;

import com.hezaerd.accessor.AxolotlShoulderAccessor;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.passive.AxolotlEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AxolotlEntity.class)
public abstract class AxolotlShoulderMixin extends AnimalEntity implements AxolotlShoulderAccessor {
    @Unique private static final int READY_TO_SIT_COOLDOWN = 100;
    @Unique private int ticks;
    
    protected AxolotlShoulderMixin(EntityType<? extends AnimalEntity> entityType, World world) {
        super(entityType, world);
    }
    
    @Inject(method = "baseTick", at = @At("HEAD"))
    private void incrementTicks(CallbackInfo ci) {
        this.ticks++;
    }

    @Unique
    public boolean betteraxolotls$mountOnto(ServerPlayerEntity player) {
        NbtCompound nbtCompound = new NbtCompound();
        nbtCompound.putString("id", EntityType.getId(EntityType.AXOLOTL).toString());
        this.writeNbt(nbtCompound);
        if (player.addShoulderEntity(nbtCompound)) {
            this.discard();
            return true;
        } else {
            return false;
        }
    }

    @Unique
    public boolean betteraxolotls$isReadyToSitOnPlayer() {
        return this.ticks > READY_TO_SIT_COOLDOWN;
    }
}
