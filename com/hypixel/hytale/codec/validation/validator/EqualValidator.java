package com.hypixel.hytale.codec.validation.validator;

import com.hypixel.hytale.codec.schema.SchemaContext;
import com.hypixel.hytale.codec.schema.config.IntegerSchema;
import com.hypixel.hytale.codec.schema.config.NumberSchema;
import com.hypixel.hytale.codec.schema.config.Schema;
import com.hypixel.hytale.codec.schema.config.StringSchema;
import com.hypixel.hytale.codec.validation.ValidationResults;
import com.hypixel.hytale.codec.validation.Validator;
import com.hypixel.hytale.logger.HytaleLogger;
import java.util.logging.Level;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class EqualValidator<T extends Comparable<T>> implements Validator<T> {
   private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
   @Nonnull
   private final T value;

   public EqualValidator(@Nonnull T value) {
      this.value = value;
   }

   public void accept(@Nullable T o, @Nonnull ValidationResults results) {
      if (o != null && this.value.compareTo(o) != 0) {
         results.fail("Provided value must be equal to " + this.value);
      }
   }

   @Override
   public void updateSchema(SchemaContext context, @Nonnull Schema target) {
      if (target.getAllOf() != null) {
         throw new IllegalArgumentException();
      } else {
         if (target instanceof StringSchema && this.value instanceof String) {
            ((StringSchema)target).setConst((String)this.value);
         } else if (target instanceof IntegerSchema && this.value instanceof Number) {
            ((IntegerSchema)target).setConst(((Number)this.value).intValue());
         } else if (target instanceof NumberSchema && this.value instanceof Number) {
            ((NumberSchema)target).setConst(((Number)this.value).doubleValue());
         } else {
            LOGGER.at(Level.WARNING).log("Cannot compare " + this.value.getClass() + " with " + target.getClass());
         }
      }
   }
}
