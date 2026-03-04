package com.hypixel.hytale.codec.schema.metadata;

import com.hypixel.hytale.codec.schema.config.BooleanSchema;
import com.hypixel.hytale.codec.schema.config.IntegerSchema;
import com.hypixel.hytale.codec.schema.config.NumberSchema;
import com.hypixel.hytale.codec.schema.config.Schema;
import com.hypixel.hytale.codec.schema.config.StringSchema;

public class NoDefaultValue implements Metadata {
   public static final NoDefaultValue INSTANCE = new NoDefaultValue();

   private NoDefaultValue() {
   }

   @Override
   public void modify(Schema schema) {
      if (schema instanceof StringSchema) {
         ((StringSchema)schema).setDefault(null);
      } else if (schema instanceof IntegerSchema) {
         ((IntegerSchema)schema).setDefault(null);
      } else if (schema instanceof NumberSchema) {
         ((NumberSchema)schema).setDefault(null);
      } else if (schema instanceof BooleanSchema) {
         ((BooleanSchema)schema).setDefault(null);
      }
   }
}
