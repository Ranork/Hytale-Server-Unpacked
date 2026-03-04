package com.hypixel.hytale.builtin.hytalegenerator.density.nodes;

import com.hypixel.hytale.builtin.hytalegenerator.density.Density;
import com.hypixel.hytale.builtin.hytalegenerator.framework.math.Normalizer;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class NormalizerDensity extends Density {
   private final double fromMin;
   private final double fromMax;
   private final double toMin;
   private final double toMax;
   @Nullable
   private Density input;

   public NormalizerDensity(double fromMin, double fromMax, double toMin, double toMax, Density input) {
      if (!(fromMin > fromMax) && !(toMin > toMax)) {
         this.fromMin = fromMin;
         this.fromMax = fromMax;
         this.toMin = toMin;
         this.toMax = toMax;
         this.input = input;
      } else {
         throw new IllegalArgumentException("min larger than max");
      }
   }

   @Override
   public double process(@Nonnull Density.Context context) {
      return Normalizer.normalize(this.fromMin, this.fromMax, this.toMin, this.toMax, this.input.process(context));
   }

   @Override
   public void setInputs(@Nonnull Density[] inputs) {
      if (inputs.length == 0) {
         this.input = null;
      }

      this.input = inputs[0];
   }
}
