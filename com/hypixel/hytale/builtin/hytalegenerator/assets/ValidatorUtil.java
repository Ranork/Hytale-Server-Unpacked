package com.hypixel.hytale.builtin.hytalegenerator.assets;

import com.hypixel.hytale.codec.validation.LegacyValidator;
import com.hypixel.hytale.codec.validation.ValidationResults;
import javax.annotation.Nonnull;

public class ValidatorUtil {
   @Nonnull
   public static <T> LegacyValidator<String> validEnumValue(@Nonnull final T[] values) {
      return new LegacyValidator<String>() {
         public void accept(String providedValue, @Nonnull ValidationResults results) {
            for (T value : values) {
               if (value.toString().equals(providedValue)) {
                  return;
               }
            }

            results.fail("String not a valid enum value: " + providedValue);
         }
      };
   }
}
