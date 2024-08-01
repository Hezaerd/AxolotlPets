package net.hezaerd.axolotlpets.utils;

import net.minecraft.util.Identifier;

public class LibMod {
    public static final String MOD_NAME = "AxolotlPets";
    public static final String MOD_ID = "axolotlpets";

    public static Identifier id(String path) {
        return Identifier.of(MOD_ID, path);
    }
}