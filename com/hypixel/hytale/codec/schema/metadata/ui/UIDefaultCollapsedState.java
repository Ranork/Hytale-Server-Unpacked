package com.hypixel.hytale.codec.schema.metadata.ui;

import com.hypixel.hytale.codec.schema.config.Schema;
import com.hypixel.hytale.codec.schema.metadata.Metadata;
import javax.annotation.Nonnull;

public class UIDefaultCollapsedState implements Metadata {
   public static final UIDefaultCollapsedState UNCOLLAPSED = new UIDefaultCollapsedState(false);
   private final boolean collapsedByDefault;

   private UIDefaultCollapsedState(boolean collapsedByDefault) {
      this.collapsedByDefault = collapsedByDefault;
   }

   @Override
   public void modify(@Nonnull Schema schema) {
      schema.getHytale().setUiCollapsedByDefault(this.collapsedByDefault);
   }
}
