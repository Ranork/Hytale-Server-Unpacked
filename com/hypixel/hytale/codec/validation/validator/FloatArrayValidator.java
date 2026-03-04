package com.hypixel.hytale.codec.validation.validator;

import com.hypixel.hytale.codec.schema.SchemaContext;
import com.hypixel.hytale.codec.schema.config.ArraySchema;
import com.hypixel.hytale.codec.schema.config.Schema;
import com.hypixel.hytale.codec.validation.ValidationResults;
import com.hypixel.hytale.codec.validation.Validator;
import javax.annotation.Nullable;

public class FloatArrayValidator implements Validator<float[]> {
   private final Validator<Float> validator;

   public FloatArrayValidator(Validator<Float> validator) {
      this.validator = validator;
   }

   public void accept(@Nullable float[] floats, ValidationResults results) {
      if (floats != null) {
         for (float t : floats) {
            this.validator.accept(t, results);
         }
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
