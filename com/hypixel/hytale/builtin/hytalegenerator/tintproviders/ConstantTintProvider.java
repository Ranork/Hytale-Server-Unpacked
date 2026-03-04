package com.hypixel.hytale.builtin.hytalegenerator.tintproviders;

import javax.annotation.Nonnull;

public class ConstantTintProvider extends TintProvider {
   @Nonnull
   private final TintProvider.Result result;

   public ConstantTintProvider(int value) {
      this.result = new TintProvider.Result(value);
   }

   @Nonnull
   @Override
   public TintProvider.Result getValue(@Nonnull TintProvider.Context context) {
      return this.result;
   }
}
