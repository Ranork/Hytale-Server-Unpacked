package com.hypixel.hytale.codec.validation.validator;

import com.hypixel.hytale.codec.schema.SchemaContext;
import com.hypixel.hytale.codec.schema.config.ArraySchema;
import com.hypixel.hytale.codec.schema.config.Schema;
import com.hypixel.hytale.codec.validation.ValidationResults;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class NonEmptyArrayValidator<T> extends NonNullValidator<T[]> {
   public static final NonEmptyArrayValidator<?> INSTANCE = new NonEmptyArrayValidator();

   private NonEmptyArrayValidator() {
   }

   public void accept(@Nullable T[] t, @Nonnull ValidationResults results) {
      if (t == null || t.length == 0) {
         results.fail("Array can't be empty!");
      }
   }

   @Override
   public void updateSchema(SchemaContext context, Schema target) {
      ArraySchema arr = (ArraySchema)target;
      arr.setMinItems(1);
   }
}
