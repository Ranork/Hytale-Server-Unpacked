package com.hypixel.hytale.codec.validation.validator;

import com.hypixel.hytale.codec.schema.SchemaContext;
import com.hypixel.hytale.codec.schema.config.ArraySchema;
import com.hypixel.hytale.codec.schema.config.Schema;
import com.hypixel.hytale.codec.validation.ValidationResults;
import com.hypixel.hytale.codec.validation.Validator;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class NonEmptyDoubleArrayValidator implements Validator<double[]> {
   public static final NonEmptyDoubleArrayValidator INSTANCE = new NonEmptyDoubleArrayValidator();

   private NonEmptyDoubleArrayValidator() {
   }

   public void accept(@Nullable double[] doubles, @Nonnull ValidationResults results) {
      if (doubles == null || doubles.length == 0) {
         results.fail("Array can't be empty!");
      }
   }

   @Override
   public void updateSchema(SchemaContext context, Schema target) {
      ArraySchema arr = (ArraySchema)target;
      arr.setMinItems(1);
   }
}
