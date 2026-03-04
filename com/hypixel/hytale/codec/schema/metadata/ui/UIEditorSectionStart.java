package com.hypixel.hytale.codec.schema.metadata.ui;

import com.hypixel.hytale.codec.schema.config.Schema;
import com.hypixel.hytale.codec.schema.metadata.Metadata;
import javax.annotation.Nonnull;

public class UIEditorSectionStart implements Metadata {
   private final String title;

   public UIEditorSectionStart(String title) {
      this.title = title;
   }

   @Override
   public void modify(@Nonnull Schema schema) {
      schema.getHytale().setUiSectionStart(this.title);
   }
}
