package com.hypixel.hytale.codec.validation.validator;

import com.hypixel.hytale.codec.schema.SchemaContext;
import com.hypixel.hytale.codec.schema.config.IntegerSchema;
import com.hypixel.hytale.codec.schema.config.NumberSchema;
import com.hypixel.hytale.codec.schema.config.Schema;
import com.hypixel.hytale.codec.validation.ValidationResults;
import com.hypixel.hytale.codec.validation.Validator;
import com.hypixel.hytale.logger.HytaleLogger;
import java.util.logging.Level;

public class RangeRefValidator<T extends Comparable<T>> implements Validator<T> {
   private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
   private final String minPointer;
   private final String maxPointer;
   private final boolean inclusive;

   public RangeRefValidator(String minPointer, String maxPointer, boolean inclusive) {
      this.minPointer = minPointer;
      this.maxPointer = maxPointer;
      this.inclusive = inclusive;
   }

   public void accept(T t, ValidationResults results) {
   }

   @Override
   public void updateSchema(SchemaContext context, Schema target) {
      if (!(target instanceof NumberSchema) && !(target instanceof IntegerSchema)) {
         LOGGER.at(Level.WARNING).log("Can't handle: " + target.getHytale().getType() + " as a range");
      } else {
         if (target instanceof IntegerSchema i) {
            if (this.minPointer != null) {
               if (this.inclusive) {
                  i.setMinimum(Schema.data(this.minPointer));
               } else {
                  i.setExclusiveMinimum(Schema.data(this.minPointer));
               }
            }

            if (this.maxPointer != null) {
               if (this.inclusive) {
                  i.setMaximum(Schema.data(this.maxPointer));
               } else {
                  i.setExclusiveMaximum(Schema.data(this.maxPointer));
               }
            }
         } else {
            NumberSchema i = (NumberSchema)target;
            if (this.minPointer != null) {
               if (this.inclusive) {
                  i.setMinimum(Schema.data(this.minPointer));
               } else {
                  i.setExclusiveMinimum(Schema.data(this.minPointer));
               }
            }

            if (this.maxPointer != null) {
               if (this.inclusive) {
                  i.setMaximum(Schema.data(this.maxPointer));
               } else {
                  i.setExclusiveMaximum(Schema.data(this.maxPointer));
               }
            }
         }
      }
   }
}
