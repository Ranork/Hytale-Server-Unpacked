package com.hypixel.hytale.codec.validation.validator;

import com.hypixel.hytale.codec.schema.SchemaContext;
import com.hypixel.hytale.codec.schema.config.ArraySchema;
import com.hypixel.hytale.codec.schema.config.Schema;
import com.hypixel.hytale.codec.validation.ValidationResults;
import com.hypixel.hytale.codec.validation.Validator;
import javax.annotation.Nonnull;

public class ArraySizeRangeValidator<T> implements Validator<T[]> {
   private int min;
   private int max;

   public ArraySizeRangeValidator(int min, int max) {
      this.min = min;
      this.max = max;
   }

   public void accept(@Nonnull T[] array, @Nonnull ValidationResults results) {
      if (array.length < this.min || array.length > this.max) {
         results.fail(String.format("Array size is invalid! Was %s, expected between %s and %s", array.length, this.min, this.max));
      }
   }

   @Override
   public void updateSchema(SchemaContext context, Schema target) {
      ArraySchema arr = (ArraySchema)target;
      arr.setMinItems(this.min);
      arr.setMaxItems(this.max);
   }
}
