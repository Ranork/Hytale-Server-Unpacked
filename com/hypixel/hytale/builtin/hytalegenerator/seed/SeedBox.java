package com.hypixel.hytale.builtin.hytalegenerator.seed;

import java.util.Random;
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
      Random rand = new Random(this.key.hashCode());
      return () -> rand.nextInt();
   }

   @Nonnull
   @Override
   public String toString() {
      return "SeedBox{value='" + this.key + "'}";
   }
}
