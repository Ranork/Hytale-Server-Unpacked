package com.hypixel.hytale.codec.schema.metadata;

import com.hypixel.hytale.codec.schema.config.Schema;
import javax.annotation.Nonnull;

public class VirtualPath implements Metadata {
   private final String path;

   public VirtualPath(String path) {
      this.path = path;
   }

   @Override
   public void modify(@Nonnull Schema schema) {
      schema.getHytale().setVirtualPath(this.path);
   }
}
