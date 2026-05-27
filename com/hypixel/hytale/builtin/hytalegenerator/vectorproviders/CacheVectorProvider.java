package com.hypixel.hytale.builtin.hytalegenerator.vectorproviders;

import javax.annotation.Nonnull;
import org.joml.Vector3d;

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
         vector_out.set(this.cache.value);
      }

      if (this.cache.position == null) {
         this.cache.position = new Vector3d();
         this.cache.value = new Vector3d();
      }

      this.cache.position.set(context.position);
      this.vectorProvider.process(context, this.cache.value);
      vector_out.set(this.cache.value);
   }

   public static class Cache {
      Vector3d position;
      Vector3d value;
   }
}
