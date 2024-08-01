package net.hezaerd.axolotlpets.item;

import net.fabricmc.fabric.api.item.v1.FabricItem;
import net.minecraft.component.type.FoodComponent;
import net.minecraft.item.Item;

public class AxolotlTreat extends Item {
    public float chance;

    public AxolotlTreat(float chance) {
        super(new Item.Settings().maxCount(64));
    }
}
