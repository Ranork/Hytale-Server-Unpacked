package com.hypixel.hytale.codec.validation.validator;

import com.hypixel.hytale.codec.validation.LegacyValidator;
import com.hypixel.hytale.codec.validation.ValidationResults;
import com.hypixel.hytale.codec.validation.Validator;
import java.util.List;
import javax.annotation.Nonnull;

public class ListValidator<T> implements LegacyValidator<List<T>> {
   private Validator<T> validator;

   public ListValidator(Validator<T> validator) {
      this.validator = validator;
   }

   public void accept(@Nonnull List<T> ts, ValidationResults results) {
      for (T t : ts) {
         this.validator.accept(t, results);
      }
   }
}
