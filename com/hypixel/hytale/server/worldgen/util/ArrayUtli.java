package com.hypixel.hytale.server.worldgen.util;

import java.util.Random;
import javax.annotation.Nonnull;

public class ArrayUtli {
   public static void shuffleArray(@Nonnull int[] ar, @Nonnull Random rnd) {
      for (int i = ar.length - 1; i > 0; i--) {
         int index = rnd.nextInt(i + 1);
         int a = ar[index];
         ar[index] = ar[i];
         ar[i] = a;
      }
   }

   public static <T> void shuffleArray(@Nonnull T[] ar, @Nonnull Random rnd) {
      for (int i = ar.length - 1; i > 0; i--) {
         int index = rnd.nextInt(i + 1);
         T a = ar[index];
         ar[index] = ar[i];
         ar[i] = a;
      }
   }
}
