package com.hypixel.hytale.codec.validation.validator;

import com.hypixel.hytale.codec.schema.SchemaContext;
import com.hypixel.hytale.codec.schema.config.ObjectSchema;
import com.hypixel.hytale.codec.schema.config.Schema;
import com.hypixel.hytale.codec.schema.config.StringSchema;
import com.hypixel.hytale.codec.validation.ValidationResults;
import com.hypixel.hytale.codec.validation.Validator;
import java.util.Map;
import javax.annotation.Nonnull;

public class RequiredMapKeysValidator<T> implements Validator<Map<T, ?>> {
   private final T[] array;

   public RequiredMapKeysValidator(T[] array) {
      this.array = array;
   }

   public void accept(@Nonnull Map<T, ?> map, @Nonnull ValidationResults results) {
      for (int i = 0; i < this.array.length; i++) {
         T obj = this.array[i];
         if (!map.containsKey(obj)) {
            results.fail(String.format("Key not found! %s", obj));
         }
      }
   }

   @Override
   public void updateSchema(SchemaContext context, Schema target) {
      ObjectSchema obj = (ObjectSchema)target;
      StringSchema keys = obj.getPropertyNames() != null ? obj.getPropertyNames() : new StringSchema();
      String[] keyValues = new String[this.array.length];

      for (int i = 0; i < this.array.length; i++) {
         keyValues[i] = this.array[i].toString();
      }

      keys.setEnum(keyValues);
      obj.setPropertyNames(keys);
   }
}
