package com.hypixel.hytale.builtin.hytalegenerator.rng;

import com.hypixel.hytale.math.util.FastRandom;
import java.util.function.Supplier;
import javax.annotation.Nonnull;

public class SeedBox {
   @Nonnull
   private final String key;

   public SeedBox(@Nonnull String key) {
      this.key = key;
   }

   public SeedBox(int key) {
      this.key = Integer.toString(key);
   }

   @Nonnull
   public SeedBox child(@Nonnull String childKey) {
      return new SeedBox(this.key + childKey);
   }

   @Nonnull
   public Supplier<Integer> createSupplier() {
      FastRandom rand = new FastRandom(this.key.hashCode());
      return () -> rand.nextInt();
   }

   @Nonnull
   @Override
   public String toString() {
      return "SeedBox{value='" + this.key + "'}";
   }
}
