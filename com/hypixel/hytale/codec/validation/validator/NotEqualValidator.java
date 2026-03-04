package com.hypixel.hytale.codec.validation.validator;

import com.hypixel.hytale.codec.schema.SchemaContext;
import com.hypixel.hytale.codec.schema.config.IntegerSchema;
import com.hypixel.hytale.codec.schema.config.NumberSchema;
import com.hypixel.hytale.codec.schema.config.Schema;
import com.hypixel.hytale.codec.schema.config.StringSchema;
import com.hypixel.hytale.codec.validation.ValidationResults;
import com.hypixel.hytale.codec.validation.Validator;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class NotEqualValidator<T extends Comparable<T>> implements Validator<T> {
   @Nonnull
   private final T value;

   public NotEqualValidator(@Nonnull T value) {
      this.value = value;
   }

   public void accept(@Nullable T o, @Nonnull ValidationResults results) {
      if (o != null && this.value.compareTo(o) == 0) {
         results.fail("Provided value can't be equal to " + this.value);
      }
   }

   @Override
   public void updateSchema(SchemaContext context, @Nonnull Schema target) {
      if (target.getAllOf() != null) {
         throw new IllegalArgumentException();
      } else {
         if (target instanceof StringSchema) {
            target.setAllOf(Schema.not(StringSchema.constant((String)this.value)));
         } else if (target instanceof IntegerSchema) {
            target.setAllOf(Schema.not(IntegerSchema.constant(((Number)this.value).intValue())));
         } else {
            if (!(target instanceof NumberSchema)) {
               throw new IllegalArgumentException();
            }

            target.setAllOf(Schema.not(NumberSchema.constant(((Number)this.value).doubleValue())));
         }
      }
   }
}
