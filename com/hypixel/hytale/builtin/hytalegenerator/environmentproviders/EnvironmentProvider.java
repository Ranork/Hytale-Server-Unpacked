package com.hypixel.hytale.builtin.hytalegenerator.environmentproviders;

import javax.annotation.Nonnull;
import org.joml.Vector3i;

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
