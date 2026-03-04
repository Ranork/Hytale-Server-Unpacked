package com.hypixel.hytale.codec.validation;

import com.hypixel.hytale.codec.ExtraInfo;
import com.hypixel.hytale.codec.schema.SchemaContext;
import com.hypixel.hytale.codec.schema.config.Schema;
import java.util.function.BiConsumer;
import javax.annotation.Nonnull;

public interface Validator<T> extends BiConsumer<T, ValidationResults> {
   void accept(T var1, ValidationResults var2);

   void updateSchema(SchemaContext var1, Schema var2);

   @Nonnull
   default LateValidator<T> late() {
      final Validator<T> current = this;
      return new LateValidator<T>() {
         @Override
         public void accept(T t, ValidationResults results) {
         }

         @Override
         public void acceptLate(T t, ValidationResults results, ExtraInfo extraInfo) {
            current.accept(t, results);
         }

         @Override
         public void updateSchema(SchemaContext context, Schema target) {
            current.updateSchema(context, target);
         }
      };
   }
}
