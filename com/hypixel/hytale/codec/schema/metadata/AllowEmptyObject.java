package com.hypixel.hytale.codec.schema.metadata;

import com.hypixel.hytale.codec.schema.config.Schema;
import javax.annotation.Nonnull;

public class AllowEmptyObject implements Metadata {
   public static final AllowEmptyObject INSTANCE = new AllowEmptyObject(true);
   private final boolean allowEmptyObject;

   public AllowEmptyObject(boolean allowEmptyObject) {
      this.allowEmptyObject = allowEmptyObject;
   }

   @Override
   public void modify(@Nonnull Schema schema) {
      schema.getHytale().setAllowEmptyObject(this.allowEmptyObject);
   }
}
