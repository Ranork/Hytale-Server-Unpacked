package com.hypixel.hytale.builtin.hytalegenerator.tintproviders;

import javax.annotation.Nonnull;

public class NoTintProvider extends TintProvider {
   @Nonnull
   @Override
   public TintProvider.Result getValue(@Nonnull TintProvider.Context context) {
      return TintProvider.Result.WITHOUT_VALUE;
   }
}
