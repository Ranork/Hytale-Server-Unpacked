package com.hypixel.hytale.codec.schema.metadata.ui;

import com.hypixel.hytale.codec.schema.config.Schema;
import com.hypixel.hytale.codec.schema.metadata.Metadata;
import javax.annotation.Nonnull;

public class UIEditorPreview implements Metadata {
   private final UIEditorPreview.PreviewType previewType;

   public UIEditorPreview(UIEditorPreview.PreviewType type) {
      this.previewType = type;
   }

   @Override
   public void modify(@Nonnull Schema schema) {
      schema.getHytale().setUiEditorPreview(this.previewType);
   }

   public static enum PreviewType {
      ITEM,
      MODEL,
      REVERB_EFFECT,
      EQUALIZER_EFFECT;
   }
}
