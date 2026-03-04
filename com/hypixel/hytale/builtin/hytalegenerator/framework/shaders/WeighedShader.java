package com.hypixel.hytale.builtin.hytalegenerator.framework.shaders;

import com.hypixel.hytale.builtin.hytalegenerator.datastructures.WeightedMap;
import com.hypixel.hytale.builtin.hytalegenerator.framework.math.SeedGenerator;
import java.util.Random;
import javax.annotation.Nonnull;

public class WeighedShader<T> implements Shader<T> {
   @Nonnull
   private final WeightedMap<Shader<T>> childrenWeightedMap = new WeightedMap<>(1);
   private SeedGenerator seedGenerator = new SeedGenerator(System.nanoTime());

   public WeighedShader(@Nonnull Shader<T> initialChild, double weight) {
      this.add(initialChild, weight);
   }

   @Nonnull
   public WeighedShader<T> add(@Nonnull Shader<T> child, double weight) {
      if (weight <= 0.0) {
         throw new IllegalArgumentException("invalid weight");
      } else {
         this.childrenWeightedMap.add(child, weight);
         return this;
      }
   }

   @Nonnull
   public WeighedShader<T> setSeed(long seed) {
      this.seedGenerator = new SeedGenerator(seed);
      return this;
   }

   @Override
   public T shade(T current, long seed) {
      Random r = new Random(seed);
      return this.childrenWeightedMap.pick(r).shade(current, seed);
   }

   @Override
   public T shade(T current, long seedA, long seedB) {
      return this.shade(current, this.seedGenerator.seedAt(seedA, seedB));
   }

   @Override
   public T shade(T current, long seedA, long seedB, long seedC) {
      return this.shade(current, this.seedGenerator.seedAt(seedA, seedB, seedC));
   }

   @Nonnull
   @Override
   public String toString() {
      return "WeighedShader{childrenWeighedMap=" + this.childrenWeightedMap + ", seedGenerator=" + this.seedGenerator + "}";
   }
}
