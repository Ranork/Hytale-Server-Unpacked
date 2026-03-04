package com.hypixel.hytale.builtin.hytalegenerator.vectorproviders;

import com.hypixel.hytale.math.vector.Vector3d;
import javax.annotation.Nonnull;

public class CacheVectorProvider extends VectorProvider {
   @Nonnull
   private final VectorProvider vectorProvider;
   @Nonnull
   private final CacheVectorProvider.Cache cache;

   public CacheVectorProvider(@Nonnull VectorProvider vectorProvider) {
      this.vectorProvider = vectorProvider;
      this.cache = new CacheVectorProvider.Cache();
   }

   @Override
   public void process(@Nonnull VectorProvider.Context context, @Nonnull Vector3d vector_out) {
      if (this.cache.position != null && this.cache.position.equals(context.position)) {
         vector_out.assign(this.cache.value);
      }

      if (this.cache.position == null) {
         this.cache.position = new Vector3d();
         this.cache.value = new Vector3d();
      }

      this.cache.position.assign(context.position);
      this.vectorProvider.process(context, this.cache.value);
      vector_out.assign(this.cache.value);
   }

   public static class Cache {
      Vector3d position;
      Vector3d value;
   }
}
