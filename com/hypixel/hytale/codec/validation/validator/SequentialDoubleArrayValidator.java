package com.hypixel.hytale.codec.validation.validator;

import com.hypixel.hytale.codec.schema.SchemaContext;
import com.hypixel.hytale.codec.schema.config.Schema;
import com.hypixel.hytale.codec.validation.ValidationResults;
import com.hypixel.hytale.codec.validation.Validator;
import javax.annotation.Nonnull;

public class SequentialDoubleArrayValidator implements Validator<double[]> {
   public static final SequentialDoubleArrayValidator NEQ_INSTANCE = new SequentialDoubleArrayValidator(false);
   public static final SequentialDoubleArrayValidator ALLOW_EQ_INSTANCE = new SequentialDoubleArrayValidator(true);
   private final boolean allowEquals;

   public SequentialDoubleArrayValidator(boolean allowEquals) {
      this.allowEquals = allowEquals;
   }

   public void accept(@Nonnull double[] doubles, @Nonnull ValidationResults results) {
      if (doubles.length > 1) {
         double last = doubles[0];

         for (int i = 1; i < doubles.length; i++) {
            double val = doubles[i];
            if (!this.allowEquals && last >= val || this.allowEquals && last > val) {
               results.fail(
                  String.format(
                     "Values must be sequential. %f at index %d is larger than %s %f at index %d", last, i - 1, this.allowEquals ? "" : "or equal to", val, i
                  )
               );
            }

            last = val;
         }
      }
   }

   @Override
   public void updateSchema(SchemaContext context, Schema target) {
   }
}
