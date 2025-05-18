package com.hezaerd.mixin;

import com.hezaerd.AxolotlTameableAccessor;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.passive.AxolotlEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(value = AxolotlEntity.class, priority = 1500)    
public abstract class AxolotlDeathMixin extends AnimalEntity {
    protected AxolotlDeathMixin(EntityType<? extends AnimalEntity> entityType, World world) {
        super(entityType, world);
    }

    @Override
    public void onDeath(DamageSource damageSource) {
        if (this.getWorld() instanceof ServerWorld serverWorld
                && serverWorld.getGameRules().getBoolean(GameRules.SHOW_DEATH_MESSAGES)
                && ((AxolotlTameableAccessor)this).betteraxolotls$getOwner() instanceof ServerPlayerEntity serverPlayerEntity) {
            serverPlayerEntity.sendMessage(this.getDamageTracker().getDeathMessage());
        }

        super.onDeath(damageSource);
    }
}
