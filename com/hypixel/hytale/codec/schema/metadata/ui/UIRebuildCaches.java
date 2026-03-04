package com.hypixel.hytale.codec.schema.metadata.ui;

import com.hypixel.hytale.codec.schema.config.Schema;
import com.hypixel.hytale.codec.schema.metadata.Metadata;
import javax.annotation.Nonnull;

public class UIRebuildCaches implements Metadata {
   private final UIRebuildCaches.ClientCache[] caches;
   private final boolean appliesToChildProperties;

   public UIRebuildCaches(UIRebuildCaches.ClientCache... caches) {
      this(true, caches);
   }

   public UIRebuildCaches(boolean appliesToChildProperties, UIRebuildCaches.ClientCache... caches) {
      this.caches = caches;
      this.appliesToChildProperties = appliesToChildProperties;
   }

   @Override
   public void modify(@Nonnull Schema schema) {
      schema.getHytale().setUiRebuildCaches(this.caches);
      schema.getHytale().setUiRebuildCachesForChildProperties(this.appliesToChildProperties);
   }

   public static enum ClientCache {
      BLOCK_TEXTURES,
      MODELS,
      MODEL_TEXTURES,
      MAP_GEOMETRY,
      ITEM_ICONS;
   }
}
