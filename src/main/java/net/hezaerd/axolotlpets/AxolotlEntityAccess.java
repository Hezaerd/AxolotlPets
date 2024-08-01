package net.hezaerd.axolotlpets;

import net.minecraft.entity.LivingEntity;

public interface AxolotlEntityAccess {
    LivingEntity axolotlpets$getOwner();

    boolean isTamed();
}
