package net.hezaerd.axolotlpets.goals;

import net.hezaerd.axolotlpets.AxolotlEntityAccessor;
import net.minecraft.block.BlockState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.ai.pathing.EntityNavigation;
import net.minecraft.entity.ai.pathing.LandPathNodeMaker;
import net.minecraft.entity.ai.pathing.PathNodeType;
import net.minecraft.entity.passive.AxolotlEntity;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldView;

import java.util.EnumSet;

public class AxolotlFollowOwnerGoal extends Goal {
    private final AxolotlEntity axolotl;
    private LivingEntity owner;
    private final WorldView world;
    private final double speed;
    private final EntityNavigation navigation;
    private int updateCountdownTicks;
    private final float maxDistance;
    private final float minDistance;
    private float oldWaterPathfindingPenalty;
    private final boolean leavesAllowed;

    public AxolotlFollowOwnerGoal(AxolotlEntity axolotl, double speed, float minDistance, float maxDistance, boolean leavesAllowed) {
        this.axolotl = axolotl;
        this.world = axolotl.getWorld();
        this.speed = speed;
        this.navigation = axolotl.getNavigation();
        this.minDistance = minDistance;
        this.maxDistance = maxDistance;
        this.leavesAllowed = leavesAllowed;
        this.setControls(EnumSet.of(Goal.Control.MOVE, Goal.Control.LOOK));
    }

    @Override
    public boolean canStart() {
        LivingEntity livingEntity = ((AxolotlEntityAccessor)this.axolotl).axolotlpets$getOwner();
        if (livingEntity == null) {
            return false;
        } else if (livingEntity.isSpectator()) {
            return false;
        } else if (this.axolotl.squaredDistanceTo(livingEntity) < (double) (this.minDistance * this.minDistance)) {
            return false;
        } else {
            this.owner = livingEntity;
            return true;
        }
    }

    public boolean shouldContinue() {
        if (this.navigation.isIdle()) {
            return false;
        } else if (!this.canFollow()) {
            return false;
        } else {
            return !(this.axolotl.squaredDistanceTo(this.owner) <= (double) (this.maxDistance * this.maxDistance));
        }
    }

    public boolean canFollow() {
        return !this.axolotl.hasVehicle() || !this.axolotl.isLeashed();
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
        this.axolotl.getLookControl().lookAt(this.owner, 10.0F, (float)this.axolotl.getMaxLookPitchChange());

        if (--this.updateCountdownTicks <= 0) {
            this.updateCountdownTicks = this.getTickCount(10);
            if (this.canFollow()) {
                if (this.axolotl.squaredDistanceTo(this.owner) >= 144.0D) { // 12 blocks
                    this.tryTeleport();
                } else {
                    this.navigation.startMovingTo(this.owner, this.speed);
                }
            }
        }
    }

    private int getRandomInt(int min, int max) {
        return this.axolotl.getRandom().nextInt(max - min + 1) + min;
    }

    private void tryTeleport() {
        BlockPos blockPos = this.owner.getBlockPos();

        for (int i = 0; i < 10; ++i) {
            int j = this.getRandomInt(-3, 3);
            int k = this.getRandomInt(-1, 1);
            int l = this.getRandomInt(-3, 3);
            boolean bl = this.tryTeleportTo(blockPos.getX() + j, blockPos.getY() + k, blockPos.getZ() + l);
            if (bl) {
                return;
            }
        }
    }

    private boolean tryTeleportTo(int x, int y, int z) {
        if (Math.abs((double) x - this.owner.getX()) < 2.0D && Math.abs((double) z - this.owner.getZ()) < 2.0D) {
            return false;
        } else if (!this.canTeleportTo(new BlockPos(x, y, z))) {
            return false;
        } else {
            this.axolotl.refreshPositionAndAngles((double) x + 0.5D, y, (double) z + 0.5D, this.axolotl.getYaw(), this.axolotl.getPitch());
            this.navigation.stop();
            return true;
        }
    }

    private boolean canTeleportTo(BlockPos pos) {
        PathNodeType pathNodeType = LandPathNodeMaker.getLandNodeType(this.axolotl, pos.mutableCopy());

        if (pathNodeType != PathNodeType.WATER) {
            return false;
        } else {
            BlockState blockState = this.world.getBlockState(pos.down());
            if (!this.leavesAllowed && blockState.isIn(BlockTags.LEAVES)) {
                return false;
            } else {
                BlockPos blockPos = pos.subtract(this.axolotl.getBlockPos());
                return this.world.isSpaceEmpty(this.axolotl, this.axolotl.getBoundingBox().offset(blockPos));
            }
        }
    }


}
