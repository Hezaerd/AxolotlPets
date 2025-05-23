package com.hezaerd.accessor;

import net.minecraft.server.network.ServerPlayerEntity;

public interface AxolotlShoulderAccessor {
    boolean betteraxolotls$mountOnto(ServerPlayerEntity player);

    boolean betteraxolotls$isReadyToSitOnPlayer();
}
