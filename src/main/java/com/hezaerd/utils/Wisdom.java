package com.hezaerd.utils;

import net.minecraft.util.Util;
import net.minecraft.util.math.random.Random;

public final class Wisdom {
    public static final Random CRYSTAL_BALL = Random.create();
    public static final String[] WISDOM_QUOTES = {
            "The only way to do great work is to love what you do.",
            "Success is not the key to happiness. Happiness is the key to success.",
            "Believe you can and you're halfway there.",
            "The future belongs to those who believe in the beauty of their dreams.",
            "Don't watch the clock; do what it does. Keep going.",
            "Success usually comes to those who are too busy to be looking for it.",
            "The only limit to our realization of tomorrow will be our doubts of today.",
            "The best way to predict the future is to create it.",
            "You miss 100% of the shots you don't take.",
            "Success is not in what you have, but who you are.",
    };

    public static void spread() {
        Log.i("Suddenly, a voice from the crystal ball whispers:");
        Log.i(Util.getRandom(WISDOM_QUOTES, CRYSTAL_BALL));
    }
}
