package com.hypixel.hytale.codec.validation.validator;

import com.hypixel.hytale.codec.schema.SchemaContext;
import com.hypixel.hytale.codec.schema.config.Schema;
import com.hypixel.hytale.codec.validation.ValidationResults;
import com.hypixel.hytale.codec.validation.Validator;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class NonNullValidator<T> implements Validator<T> {
   public static final NonNullValidator<?> INSTANCE = new NonNullValidator();

   protected NonNullValidator() {
   }

   @Override
   public void accept(@Nullable T t, @Nonnull ValidationResults results) {
      if (t == null) {
         results.fail("Can't be null!");
      }
   }

   @Override
   public void updateSchema(SchemaContext context, Schema target) {
   }
}
