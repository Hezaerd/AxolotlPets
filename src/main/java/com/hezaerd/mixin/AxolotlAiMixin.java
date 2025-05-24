package com.hezaerd.mixin;

import com.hezaerd.accessor.AxolotlAiAccessor;
import com.hezaerd.accessor.AxolotlTameableAccessor;
import com.hezaerd.entity.ai.goal.AxolotlFollowOwnerGoal;
import net.minecraft.block.BlockState;
import net.minecraft.block.LeavesBlock;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.pathing.LandPathNodeMaker;
import net.minecraft.entity.ai.pathing.PathNodeType;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.passive.AxolotlEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(AxolotlEntity.class)
public abstract class AxolotlAiMixin extends AnimalEntity implements AxolotlAiAccessor {
    
    @Unique private static final double TELEPORT_DISTANCE = 12.0D; // in blocks
    @Unique private static final double TELEPORT_DISTANCE_SQUARED = TELEPORT_DISTANCE * TELEPORT_DISTANCE;
    
    protected AxolotlAiMixin(EntityType<? extends AnimalEntity> entityType, World world) {
        super(entityType, world);
    }

    protected void initGoals() {
        this.goalSelector.add(6, new AxolotlFollowOwnerGoal((AxolotlEntity)(Object)this, 1.0D, 10.0F, 2.0F));
    }
    
    @Override
    public boolean betteraxolotls$cannotFollowOwner() {
        AxolotlTameableAccessor axolotlTameable = (AxolotlTameableAccessor) this;
        LivingEntity owner = axolotlTameable.betteraxolotls$getOwner();
        
        if (owner == null)
            return true;
        
        boolean isOwnerValid = !owner.isSpectator() && !owner.isDead();
        boolean isOwnerInWater = owner.isTouchingWater() || owner.isSubmergedInWater();
        return this.hasVehicle() || this.mightBeLeashed() || !isOwnerValid || !isOwnerInWater;
        
    }
    
    @Override
    public boolean betteraxolotls$shouldTryTeleportToOwner() {
        AxolotlTameableAccessor axolotlTameable = (AxolotlTameableAccessor) this;
        LivingEntity owner = axolotlTameable.betteraxolotls$getOwner();
        
        if (owner == null)
            return false;
        
        boolean isFarEnough = this.squaredDistanceTo(owner) >= TELEPORT_DISTANCE_SQUARED;
        boolean isOwnerValid = !owner.isSpectator() && !owner.isDead();
        boolean isOwnerInWater = owner.isTouchingWater() || owner.isSubmergedInWater();
        
        return isFarEnough && isOwnerValid && isOwnerInWater;
    }
    
    @Override
    public void betteraxolotls$tryTeleportToOwner() {
        AxolotlTameableAccessor axolotlTameable = (AxolotlTameableAccessor) this;
        LivingEntity owner = axolotlTameable.betteraxolotls$getOwner();
        if (owner != null) {
            this.tryTeleportNear(owner.getBlockPos());
        }
    }
    
    @Unique
    private void tryTeleportNear(BlockPos pos) {
        for (int i = 0; i < 10; i++) {
            int j = this.random.nextBetween(-3, 3);
            int k = this.random.nextBetween(-3, 3);
            if (Math.abs(j) >= 2 || Math.abs(k) >= 2) {
                int l = this.random.nextBetween(-1, 1);
                if (this.tryTeleportTo(pos.getX() + j, pos.getY() + l, pos.getZ() + k)) {
                    return;
                }
            }
        }
    }
    
    @Unique
    private boolean tryTeleportTo(int x, int y, int z) {
        if (!this.canTeleportTo(new BlockPos(x, y, z))) {
            return false;
        } else {
            this.refreshPositionAndAngles(x + 0.5, y, z + 0.5, this.getYaw(), this.getPitch());
            this.navigation.stop();
            return true;
        }
    }
    
    @Unique
    private boolean canTeleportTo(BlockPos pos) {
        PathNodeType pathNodeType = LandPathNodeMaker.getLandNodeType(this, pos);
        if (pathNodeType != PathNodeType.WATER) {
            return false;
        } else {
            BlockState blockState = this.getWorld().getBlockState(pos.down());
            if (!this.canTeleportOntoLeaves() && blockState.getBlock() instanceof LeavesBlock) {
                return false;
            } else {
                BlockPos blockPos = pos.subtract(this.getBlockPos());
                return this.getWorld().isSpaceEmpty(this, this.getBoundingBox().offset(blockPos));
            }
        }
    }
    
    @Unique
    private boolean canTeleportOntoLeaves() {
        return false;
    }
}
