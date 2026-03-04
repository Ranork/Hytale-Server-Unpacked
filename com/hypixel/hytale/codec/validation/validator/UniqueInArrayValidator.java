package com.hypixel.hytale.codec.validation.validator;

import com.hypixel.hytale.codec.schema.SchemaContext;
import com.hypixel.hytale.codec.schema.config.ArraySchema;
import com.hypixel.hytale.codec.schema.config.Schema;
import com.hypixel.hytale.codec.validation.ValidationResults;
import com.hypixel.hytale.codec.validation.Validator;
import java.util.Objects;
import javax.annotation.Nonnull;

public class UniqueInArrayValidator<T> implements Validator<T[]> {
   public static final UniqueInArrayValidator<?> INSTANCE = new UniqueInArrayValidator();

   private UniqueInArrayValidator() {
   }

   public void accept(@Nonnull T[] arr, @Nonnull ValidationResults results) {
      for (int i = 0; i < arr.length; i++) {
         T obj = arr[i];

         for (int j = i + 1; j < arr.length; j++) {
            T other = arr[j];
            if (Objects.equals(obj, other)) {
               results.fail(String.format("The two objects at index %s and %s are the same but must be unique! %s == %s", i, j, obj, other));
            }
         }
      }
   }

   @Override
   public void updateSchema(SchemaContext context, @Nonnull Schema target) {
      ((ArraySchema)target).setUniqueItems(true);
   }
}
