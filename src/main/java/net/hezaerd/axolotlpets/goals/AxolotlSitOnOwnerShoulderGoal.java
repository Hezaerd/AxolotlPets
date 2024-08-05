package net.hezaerd.axolotlpets.goals;

import net.hezaerd.axolotlpets.AxolotlEntityAccess;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.passive.AxolotlEntity;
import net.minecraft.server.network.ServerPlayerEntity;

public class AxolotlSitOnOwnerShoulderGoal extends Goal {
    private final AxolotlEntity axolotl;
    private ServerPlayerEntity owner;
    private boolean mounted;

    public AxolotlSitOnOwnerShoulderGoal(AxolotlEntity axolotl) {
        this.axolotl = axolotl;
    }

    @Override
    public boolean canStart() {
        ServerPlayerEntity player = (ServerPlayerEntity)((AxolotlEntityAccess)this.axolotl).axolotlpets$getOwner();
        boolean bl = player != null
                && !player.isSpectator()
                && !player.getAbilities().flying
                && !player.inPowderSnow;

        return (bl && ((AxolotlEntityAccess)this.axolotl).axolotlpets$isReadyToSitOnPlayer());
    }

    @Override
    public boolean canStop() {
        return !this.mounted;
    }

    @Override
    public void start() {
        this.owner = (ServerPlayerEntity)((AxolotlEntityAccess)this.axolotl).axolotlpets$getOwner();
        this.mounted = false;
    }

    @Override
    public void tick() {
        if (!this.mounted && !this.axolotl.isLeashed()) {
            if (this.axolotl.getBoundingBox().intersects(this.owner.getBoundingBox())) {
                this.mounted = ((AxolotlEntityAccess)this.axolotl).axolotlpets$mountOnto(this.owner);
            }
        }
    }
}
