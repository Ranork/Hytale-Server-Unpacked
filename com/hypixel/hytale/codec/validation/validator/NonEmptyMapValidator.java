package com.hypixel.hytale.codec.validation.validator;

import com.hypixel.hytale.codec.validation.ValidationResults;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class NonEmptyMapValidator<K, V> extends NonNullValidator<Map<K, V>> {
   public static final NonEmptyMapValidator<?, ?> INSTANCE = new NonEmptyMapValidator();

   private NonEmptyMapValidator() {
   }

   public void accept(@Nullable Map<K, V> t, @Nonnull ValidationResults results) {
      if (t == null || t.isEmpty()) {
         results.fail("Map can't be empty!");
      }
   }
}
