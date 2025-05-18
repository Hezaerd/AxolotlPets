package com.hezaerd;

import com.hezaerd.item.ModItems;
import com.hezaerd.utils.Log;
import com.hezaerd.utils.Wisdom;
import net.fabricmc.api.ModInitializer;

public class Betteraxolotls implements ModInitializer {
	@Override
	public void onInitialize() {
		Wisdom.spread();

		ModItems.init();
		
		Log.i("BetterAxolotls successfully loaded!");
	}
}