package com.hypixel.hytale.builtin.hytalegenerator.density.nodes;

import com.hypixel.hytale.builtin.hytalegenerator.density.Density;
import com.hypixel.hytale.builtin.hytalegenerator.fields.noise.NoiseField;
import javax.annotation.Nonnull;

public class Noise2dDensity extends Density {
   private NoiseField noise;

   public Noise2dDensity(@Nonnull NoiseField noise) {
      this.noise = noise;
   }

   @Override
   public double process(@Nonnull Density.Context context) {
      return this.noise.valueAt(context.position.x, context.position.z);
   }

   @Override
   public void setInputs(@Nonnull Density[] inputs) {
   }
}
