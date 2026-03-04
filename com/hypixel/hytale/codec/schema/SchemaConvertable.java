package com.hypixel.hytale.codec.schema;

import com.hypixel.hytale.codec.schema.config.Schema;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public interface SchemaConvertable<T> {
   @Nonnull
   Schema toSchema(@Nonnull SchemaContext var1);

   @Nonnull
   default Schema toSchema(@Nonnull SchemaContext context, @Nullable T def) {
      return this.toSchema(context);
   }
}
