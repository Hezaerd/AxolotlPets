package net.hezaerd.axolotlpets;

import net.fabricmc.api.ModInitializer;
import net.hezaerd.axolotlpets.item.ModItems;
import net.hezaerd.axolotlpets.utils.Log;

public class AxolotlPets implements ModInitializer {

	@Override
	public void onInitialize() {
		Log.i("AxolotlPets is initializing...");

		ModItems.init();

		Log.i("AxolotlPets has been initialized!");
	}
}