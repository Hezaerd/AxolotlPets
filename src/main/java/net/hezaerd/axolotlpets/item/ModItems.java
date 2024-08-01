package net.hezaerd.axolotlpets.item;

import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.hezaerd.axolotlpets.utils.LibMod;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;


public class ModItems {

    public static Item AXOLOTL_TREAT;

    public static void init() {
        AXOLOTL_TREAT = Registry.register(Registries.ITEM, LibMod.id("axolotl_treat"), new AxolotlTreat(0.3f));

        ItemGroupEvents.modifyEntriesEvent(ItemGroups.FOOD_AND_DRINK)
                .register((itemGroup) -> itemGroup.add(AXOLOTL_TREAT));
    }
}
