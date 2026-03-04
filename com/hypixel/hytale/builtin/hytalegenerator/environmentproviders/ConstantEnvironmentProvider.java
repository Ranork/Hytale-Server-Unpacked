package com.hypixel.hytale.builtin.hytalegenerator.environmentproviders;

import javax.annotation.Nonnull;

public class ConstantEnvironmentProvider extends EnvironmentProvider {
   private final int value;

   public ConstantEnvironmentProvider(int value) {
      this.value = value;
   }

   @Override
   public int getValue(@Nonnull EnvironmentProvider.Context context) {
      return this.value;
   }
}
