package com.hypixel.hytale.builtin.hytalegenerator.density.nodes;

import com.hypixel.hytale.builtin.hytalegenerator.density.Density;
import com.hypixel.hytale.builtin.hytalegenerator.fields.noise.NoiseField;
import javax.annotation.Nonnull;

public class Noise3dDensity extends Density {
   @Nonnull
   private final NoiseField noise;

   public Noise3dDensity(@Nonnull NoiseField noise) {
      this.noise = noise;
   }

   @Override
   public double process(@Nonnull Density.Context context) {
      return this.noise.valueAt(context.position.x, context.position.y, context.position.z);
   }

   @Override
   public void setInputs(@Nonnull Density[] inputs) {
   }
}
