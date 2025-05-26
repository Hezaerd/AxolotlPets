package com.hezaerd.registry.tag;

import com.hezaerd.utils.ModLib;
import net.minecraft.item.Item;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;

public class ModItemTags {
    public static final TagKey<Item> AXOLOTL_POISONOUS_FOOD = of("axolotl_poisonous_food");

    private static TagKey<Item> of(String id) {
        return TagKey.of(RegistryKeys.ITEM, ModLib.id(id));
    }
}
