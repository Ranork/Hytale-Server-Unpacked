package com.hypixel.hytale.codec.schema.metadata.ui;

import com.hypixel.hytale.codec.schema.config.Schema;
import com.hypixel.hytale.codec.schema.metadata.Metadata;
import javax.annotation.Nonnull;

public class UITypeIcon implements Metadata {
   private final String icon;

   public UITypeIcon(String icon) {
      this.icon = icon;
   }

   @Override
   public void modify(@Nonnull Schema schema) {
      schema.getHytale().setUiTypeIcon(this.icon);
   }
}
