package com.hypixel.hytale.codec.validation.validator;

import com.hypixel.hytale.codec.schema.SchemaContext;
import com.hypixel.hytale.codec.schema.config.ObjectSchema;
import com.hypixel.hytale.codec.schema.config.Schema;
import com.hypixel.hytale.codec.validation.ValidationResults;
import com.hypixel.hytale.codec.validation.Validator;
import java.util.Map;
import javax.annotation.Nonnull;

public class MapValueValidator<V> implements Validator<Map<?, V>> {
   private Validator<V> value;

   public MapValueValidator(Validator<V> value) {
      this.value = value;
   }

   public Validator<V> getValueValidator() {
      return this.value;
   }

   public void accept(@Nonnull Map<?, V> map, ValidationResults results) {
      for (V v : map.values()) {
         this.value.accept(v, results);
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
      }
   }
}
