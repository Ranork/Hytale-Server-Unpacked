package com.hypixel.hytale.codec.validation.validator;

import com.hypixel.hytale.codec.schema.SchemaContext;
import com.hypixel.hytale.codec.schema.config.ObjectSchema;
import com.hypixel.hytale.codec.schema.config.Schema;
import com.hypixel.hytale.codec.schema.config.StringSchema;
import com.hypixel.hytale.codec.validation.ValidationResults;
import com.hypixel.hytale.codec.validation.Validator;
import java.util.Map;
import java.util.Map.Entry;
import javax.annotation.Nonnull;

public class MapValidator<K, V> implements Validator<Map<K, V>> {
   private Validator<K> key;
   private Validator<V> value;

   public MapValidator(Validator<K> key, Validator<V> value) {
      this.key = key;
      this.value = value;
   }

   public void accept(@Nonnull Map<K, V> map, ValidationResults results) {
      for (Entry<K, V> entry : map.entrySet()) {
         this.key.accept(entry.getKey(), results);
         this.value.accept(entry.getValue(), results);
      }
   }

   @Override
   public void updateSchema(SchemaContext context, Schema target) {
      if (!(target instanceof ObjectSchema obj)) {
         throw new IllegalArgumentException();
      } else {
         if (obj.getProperties() != null) {
            for (Schema val : obj.getProperties().values()) {
               this.value.updateSchema(context, val);
            }
         }

         if (obj.getAdditionalProperties() instanceof Schema) {
            this.value.updateSchema(context, (Schema)obj.getAdditionalProperties());
         }

         StringSchema names = obj.getPropertyNames();
         if (names == null) {
            names = new StringSchema();
            obj.setPropertyNames(names);
         }

         this.key.updateSchema(context, names);
      }
   }
}
