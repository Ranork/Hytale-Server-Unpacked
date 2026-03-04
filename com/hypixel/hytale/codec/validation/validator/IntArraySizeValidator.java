package com.hypixel.hytale.codec.validation.validator;

import com.hypixel.hytale.codec.schema.SchemaContext;
import com.hypixel.hytale.codec.schema.config.ArraySchema;
import com.hypixel.hytale.codec.schema.config.Schema;
import com.hypixel.hytale.codec.validation.ValidationResults;
import com.hypixel.hytale.codec.validation.Validator;
import javax.annotation.Nonnull;

public class IntArraySizeValidator implements Validator<int[]> {
   private int size;

   public IntArraySizeValidator(int size) {
      this.size = size;
   }

   public void accept(@Nonnull int[] array, @Nonnull ValidationResults results) {
      if (array.length != this.size) {
         results.fail(String.format("Array size is invalid! Was %s, expected %s", array.length, this.size));
      }
   }

   @Override
   public void updateSchema(SchemaContext context, Schema target) {
      ArraySchema arr = (ArraySchema)target;
      arr.setMinItems(this.size);
      arr.setMaxItems(this.size);
   }
}
