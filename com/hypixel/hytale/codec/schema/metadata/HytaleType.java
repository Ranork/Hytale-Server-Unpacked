package com.hypixel.hytale.codec.schema.metadata;

import com.hypixel.hytale.codec.schema.config.Schema;
import javax.annotation.Nonnull;

public class HytaleType implements Metadata {
   private final String type;

   public HytaleType(String type) {
      this.type = type;
   }

   @Override
   public void modify(@Nonnull Schema schema) {
      schema.getHytale().setType(this.type);
   }
}
