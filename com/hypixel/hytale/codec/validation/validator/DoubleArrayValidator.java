package com.hypixel.hytale.codec.validation.validator;

import com.hypixel.hytale.codec.schema.SchemaContext;
import com.hypixel.hytale.codec.schema.config.ArraySchema;
import com.hypixel.hytale.codec.schema.config.Schema;
import com.hypixel.hytale.codec.validation.ValidationResults;
import com.hypixel.hytale.codec.validation.Validator;
import javax.annotation.Nonnull;

public class DoubleArrayValidator implements Validator<double[]> {
   private Validator<Double> validator;

   public DoubleArrayValidator(Validator<Double> validator) {
      this.validator = validator;
   }

   public void accept(@Nonnull double[] ds, ValidationResults results) {
      for (double d : ds) {
         this.validator.accept(d, results);
      }
   }

   @Override
   public void updateSchema(SchemaContext context, Schema target) {
      if (!(target instanceof ArraySchema)) {
         throw new IllegalArgumentException();
      } else {
         Schema item = (Schema)((ArraySchema)target).getItems();
         this.validator.updateSchema(context, item);
      }
   }
}
