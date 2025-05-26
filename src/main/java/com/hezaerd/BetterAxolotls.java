package com.hezaerd;

import com.hezaerd.utils.Log;
import com.hezaerd.utils.Wisdom;
import net.fabricmc.api.ModInitializer;

public class BetterAxolotls implements ModInitializer {
	@Override
	public void onInitialize() {
		Wisdom.spread();
		Log.i("BetterAxolotls successfully loaded!");
	}
}