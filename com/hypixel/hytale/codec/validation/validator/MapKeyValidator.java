package com.hypixel.hytale.codec.validation.validator;

import com.hypixel.hytale.codec.schema.SchemaContext;
import com.hypixel.hytale.codec.schema.config.ObjectSchema;
import com.hypixel.hytale.codec.schema.config.Schema;
import com.hypixel.hytale.codec.schema.config.StringSchema;
import com.hypixel.hytale.codec.validation.ValidationResults;
import com.hypixel.hytale.codec.validation.Validator;
import java.util.Map;
import javax.annotation.Nonnull;

public class MapKeyValidator<K> implements Validator<Map<K, ?>> {
   private Validator<K> key;

   public MapKeyValidator(Validator<K> key) {
      this.key = key;
   }

   public Validator<K> getKeyValidator() {
      return this.key;
   }

   public void accept(@Nonnull Map<K, ?> map, ValidationResults results) {
      for (K k : map.keySet()) {
         this.key.accept(k, results);
      }
   }

   @Override
   public void updateSchema(SchemaContext context, Schema target) {
      if (target instanceof ObjectSchema obj) {
         StringSchema names = obj.getPropertyNames();
         if (names == null) {
            names = new StringSchema();
            obj.setPropertyNames(names);
         }

         this.key.updateSchema(context, names);
      } else {
         throw new IllegalArgumentException();
      }
   }
}
