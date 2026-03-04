package com.hypixel.hytale.builtin.hytalegenerator.environmentproviders;

import com.hypixel.hytale.math.vector.Vector3i;
import javax.annotation.Nonnull;

public abstract class EnvironmentProvider {
   public abstract int getValue(@Nonnull EnvironmentProvider.Context var1);

   @Nonnull
   public static EnvironmentProvider noEnvironmentProvider() {
      return new ConstantEnvironmentProvider(0);
   }

   public static class Context {
      public Vector3i position;

      public Context(@Nonnull Vector3i position) {
         this.position = position;
      }

      public Context(@Nonnull EnvironmentProvider.Context other) {
         this.position = other.position;
      }
   }
}
