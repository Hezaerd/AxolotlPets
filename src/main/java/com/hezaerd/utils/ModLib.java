package com.hezaerd.utils;

import net.minecraft.util.Identifier;

public final class ModLib {
    public static final String MOD_ID = "betteraxolotls";

    public static Identifier id(String path) {
        return Identifier.of(MOD_ID, path);
    }
}
