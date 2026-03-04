package com.hypixel.hytale.builtin.hytalegenerator.density.nodes;

import com.hypixel.hytale.builtin.hytalegenerator.density.Density;
import com.hypixel.hytale.math.vector.Vector3d;
import javax.annotation.Nonnull;

public class CacheDensity extends Density {
   @Nonnull
   private final CacheDensity.Cache cache;
   @Nonnull
   private Density input;

   public CacheDensity(@Nonnull Density input) {
      this.input = input;
      this.cache = new CacheDensity.Cache();
   }

   @Override
   public double process(@Nonnull Density.Context context) {
      if (this.cache.position != null
         && this.cache.position.x == context.position.x
         && this.cache.position.y == context.position.y
         && this.cache.position.z == context.position.z) {
         return this.cache.value;
      } else {
         if (this.cache.position == null) {
            this.cache.position = new Vector3d();
         }

         this.cache.position.assign(context.position);
         this.cache.value = this.input.process(context);
         return this.cache.value;
      }
   }

   @Override
   public void setInputs(@Nonnull Density[] inputs) {
      assert inputs.length != 0;

      assert inputs[0] != null;

      this.input = inputs[0];
   }

   private static class Cache {
      Vector3d position;
      double value;
   }
}
