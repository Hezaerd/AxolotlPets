package net.hezaerd.axolotlpets.item;

import net.minecraft.item.Item;

public class AxolotlTreat extends Item {
    public float chance;

    public AxolotlTreat(float chance) {
        super(new Item.Settings().maxCount(64));
    }
}
