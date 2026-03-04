package com.hypixel.hytale.codec.validation.validator;

import com.hypixel.hytale.codec.validation.LegacyValidator;
import com.hypixel.hytale.codec.validation.ValidationResults;
import javax.annotation.Nonnull;

public class DeprecatedValidator<T> implements LegacyValidator<T> {
   public static final DeprecatedValidator<?> INSTANCE = new DeprecatedValidator();

   private DeprecatedValidator() {
   }

   @Override
   public void accept(T t, @Nonnull ValidationResults results) {
      results.warn("This field is deprecated and will be removed in the future!");
   }
}
