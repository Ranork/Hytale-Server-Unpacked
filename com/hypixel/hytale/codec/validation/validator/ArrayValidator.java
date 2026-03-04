package com.hypixel.hytale.codec.validation.validator;

import com.hypixel.hytale.codec.schema.SchemaContext;
import com.hypixel.hytale.codec.schema.config.ArraySchema;
import com.hypixel.hytale.codec.schema.config.Schema;
import com.hypixel.hytale.codec.validation.LegacyValidator;
import com.hypixel.hytale.codec.validation.ValidationResults;
import com.hypixel.hytale.codec.validation.Validator;
import javax.annotation.Nullable;

public class ArrayValidator<T> implements Validator<T[]> {
   private Validator<T> validator;

   public ArrayValidator(Validator<T> validator) {
      this.validator = validator;
   }

   @Deprecated(forRemoval = true)
   public ArrayValidator(LegacyValidator<T> validator) {
      this.validator = validator;
   }

   public Validator<T> getValidator() {
      return this.validator;
   }

   public void accept(@Nullable T[] ts, ValidationResults results) {
      if (ts != null) {
         for (T t : ts) {
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
