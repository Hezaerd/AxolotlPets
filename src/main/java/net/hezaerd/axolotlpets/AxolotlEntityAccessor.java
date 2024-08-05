package net.hezaerd.axolotlpets;

import net.minecraft.entity.LivingEntity;
import net.minecraft.server.network.ServerPlayerEntity;

public interface AxolotlEntityAccessor {
    LivingEntity axolotlpets$getOwner();

    boolean axolotlpets$mountOnto(ServerPlayerEntity player);
    boolean axolotlpets$isReadyToSitOnPlayer();

    boolean axolotlpets$cannotFollowOwner();
    boolean axolotlpets$shouldTryTeleportToOwner();

    boolean isTamed();
}
