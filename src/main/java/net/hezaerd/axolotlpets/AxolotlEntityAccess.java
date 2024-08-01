package net.hezaerd.axolotlpets;

import net.minecraft.entity.LivingEntity;
import net.minecraft.server.network.ServerPlayerEntity;

public interface AxolotlEntityAccess {
    LivingEntity axolotlpets$getOwner();

    boolean axolotlpets$mountOnto(ServerPlayerEntity player);
    boolean axolotlpets$isReadyToSitOnPlayer();

    boolean isTamed();

}
