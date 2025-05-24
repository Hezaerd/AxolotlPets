package com.hezaerd.entity.ai.goal;

import com.hezaerd.accessor.AxolotlAiAccessor;
import com.hezaerd.accessor.AxolotlTameableAccessor;
import com.hezaerd.utils.Log;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.ai.pathing.AmphibiousSwimNavigation;
import net.minecraft.entity.ai.pathing.EntityNavigation;
import net.minecraft.entity.ai.pathing.MobNavigation;
import net.minecraft.entity.ai.pathing.PathNodeType;
import net.minecraft.entity.passive.AxolotlEntity;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;

public class AxolotlFollowOwnerGoal extends Goal {
    private final AxolotlEntity axolotl;
    @Nullable private LivingEntity owner;
    private final double speed;
    private final EntityNavigation navigation;
    private int updateCountdownTicks;
    private final float maxDistance;
    private final float minDistance;
    private float oldWaterPathfindingPenalty;
    
    public AxolotlFollowOwnerGoal(AxolotlEntity axolotl, double speed, float minDistance, float maxDistance) {
        this.axolotl = axolotl;
        this.speed = speed;
        this.navigation = axolotl.getNavigation();
        this.minDistance = minDistance;
        this.maxDistance = maxDistance;
        this.setControls(EnumSet.of(Control.MOVE, Control.LOOK));
        if (!(axolotl.getNavigation() instanceof AmphibiousSwimNavigation)) {
            throw new IllegalArgumentException("Unsupported mob type for AxolotlFollowOwnerGoal");
        }
    }
    
    @Override
    public boolean canStart() {
        AxolotlTameableAccessor tameableAccessor = (AxolotlTameableAccessor) this.axolotl;
        AxolotlAiAccessor aiAccessor = (AxolotlAiAccessor) this.axolotl;
        LivingEntity owner = tameableAccessor.betteraxolotls$getOwner();
        if (owner == null) {
            return false;
        } else if (aiAccessor.betteraxolotls$cannotFollowOwner()) {
            return false;
        } else if (this.axolotl.squaredDistanceTo(owner) < this.minDistance * this.minDistance) {
            return false;
        } else {
            this.owner = owner;
            return true;
        }
    }
    
    @Override
    public boolean shouldContinue() {
        AxolotlAiAccessor aiAccessor = (AxolotlAiAccessor) this.axolotl;
        if (this.navigation.isIdle()) {
            return false;
        } else {
            return !aiAccessor.betteraxolotls$cannotFollowOwner() && !(this.axolotl.squaredDistanceTo(this.owner) <= this.maxDistance * this.maxDistance);
        }
    }

    @Override
    public void start() {
        this.updateCountdownTicks = 0;
        this.oldWaterPathfindingPenalty = this.axolotl.getPathfindingPenalty(PathNodeType.WATER);
        this.axolotl.setPathfindingPenalty(PathNodeType.WATER, 0.0F);
    }

    @Override
    public void stop() {
        this.owner = null;
        this.navigation.stop();
        this.axolotl.setPathfindingPenalty(PathNodeType.WATER, this.oldWaterPathfindingPenalty);
    }
    
    @Override
    public void tick() {
        AxolotlAiAccessor aiAccessor = (AxolotlAiAccessor) this.axolotl;
        boolean bl = aiAccessor.betteraxolotls$shouldTryTeleportToOwner();
        if (!bl) {
            this.axolotl.getLookControl().lookAt(this.owner, 10.0F, this.axolotl.getMaxLookPitchChange());
        }
        
        if (--this.updateCountdownTicks <= 0) {
            this.updateCountdownTicks = this.getTickCount(10);
            if (bl) {
                aiAccessor.betteraxolotls$tryTeleportToOwner();
            } else {
                Log.i("Axolotl: " + this.axolotl.getName().toString() + " is following owner: " + (this.owner != null ? this.owner.getName().toString() : "none"));
                this.navigation.startMovingTo(this.owner, this.speed);
            }
        }
    }
}
