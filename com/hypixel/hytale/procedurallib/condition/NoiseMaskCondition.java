package com.hypixel.hytale.procedurallib.condition;

import com.hypixel.hytale.procedurallib.property.NoiseProperty;
import javax.annotation.Nonnull;

public class NoiseMaskCondition implements ICoordinateCondition {
   protected final NoiseProperty noiseMask;
   protected final IDoubleCondition condition;

   public NoiseMaskCondition(NoiseProperty noiseMask, IDoubleCondition condition) {
      this.noiseMask = noiseMask;
      this.condition = condition;
   }

   @Override
   public boolean eval(int seed, int x, int y) {
      return this.condition.eval(this.noiseMask.get(seed, x, y));
   }

   @Override
   public boolean eval(int seed, int x, int y, int z) {
      return this.condition.eval(this.noiseMask.get(seed, x, y, z));
   }

   @Nonnull
   @Override
   public String toString() {
      return "NoiseMaskCondition{noiseMask=" + this.noiseMask + ", condition=" + this.condition + "}";
   }
}
