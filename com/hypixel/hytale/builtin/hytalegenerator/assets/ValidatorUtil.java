package com.hypixel.hytale.builtin.hytalegenerator.assets;

import com.hypixel.hytale.codec.schema.SchemaContext;
import com.hypixel.hytale.codec.schema.config.Schema;
import com.hypixel.hytale.codec.validation.ValidationResults;
import com.hypixel.hytale.codec.validation.Validator;
import javax.annotation.Nonnull;

public class ValidatorUtil {
   @Nonnull
   public static <T> Validator<String> validEnumValue(@Nonnull final T[] values) {
      return new Validator<String>() {
         public void accept(String providedValue, @Nonnull ValidationResults results) {
            for (T value : values) {
               if (value.toString().equals(providedValue)) {
                  return;
               }
            }

            results.fail("String not a valid enum value: " + providedValue);
         }

         @Override
         public void updateSchema(SchemaContext context, Schema target) {
         }
      };
   }
}
