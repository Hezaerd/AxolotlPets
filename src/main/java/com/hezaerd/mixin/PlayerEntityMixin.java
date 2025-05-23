package com.hezaerd.mixin;

import com.hezaerd.utils.Log;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.passive.ParrotEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.sound.SoundEvents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerEntity.class)
public class PlayerEntityMixin {
    @Inject(method = "updateShoulderEntity", at = @At("HEAD"), cancellable = true)
    private void updateShoulderEntity(NbtCompound entityNbt, CallbackInfo ci) {
        PlayerEntity player = (PlayerEntity) (Object) this;

        if (entityNbt.isEmpty() || entityNbt.getBoolean("Silent", false)) {
            return;
        }

        if (player.getWorld().random.nextInt(200) == 0) {
            EntityType<?> entityType = entityNbt.get("id", EntityType.CODEC).orElse(null);
            
            if (entityType == EntityType.PARROT && !ParrotEntity.imitateNearbyMob(player.getWorld(), player)) {
                player.getWorld().playSound(
                        null, 
                        player.getX(), player.getY(), player.getZ(),
                        ParrotEntity.getRandomSound(player.getWorld(), player.getWorld().random),
                        player.getSoundCategory(), 
                        1.0F,
                        ParrotEntity.getSoundPitch(player.getWorld().random)
                );
                ci.cancel();
            }
            
            if (entityType == EntityType.AXOLOTL) {
                player.getWorld().playSound(
                        null,
                        player.getX(), player.getY(), player.getZ(),
                        SoundEvents.ENTITY_AXOLOTL_IDLE_AIR,
                        player.getSoundCategory(),
                        2.0F,
                        1.0F + (player.getWorld().random.nextFloat() - player.getWorld().random.nextFloat()) * 0.2F
                );
                ci.cancel();
            }
        }
        
        ci.cancel();
    }
}
