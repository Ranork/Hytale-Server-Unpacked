package com.hypixel.hytale.builtin.hytalegenerator.framework.math;

import java.util.Random;

public class Probability {
   public static boolean of(double chance, long seed) {
      Random rand = new Random(seed);
      return rand.nextDouble() < chance;
   }
}
