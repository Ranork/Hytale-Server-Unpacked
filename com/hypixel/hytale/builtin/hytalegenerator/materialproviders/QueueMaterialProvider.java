package com.hypixel.hytale.builtin.hytalegenerator.materialproviders;

import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class QueueMaterialProvider<V> extends MaterialProvider<V> {
   @Nonnull
   private final MaterialProvider<V>[] queue;

   public QueueMaterialProvider(@Nonnull List<MaterialProvider<V>> queue) {
      this.queue = new MaterialProvider[queue.size()];

      for (int i = 0; i < queue.size(); i++) {
         MaterialProvider<V> l = queue.get(i);
         if (l == null) {
            throw new IllegalArgumentException("null element in layers");
         }

         this.queue[i] = l;
      }
   }

   @Nullable
   @Override
   public V getVoxelTypeAt(@Nonnull MaterialProvider.Context context) {
      for (MaterialProvider<V> layer : this.queue) {
         V material = layer.getVoxelTypeAt(context);
         if (material != null) {
            return material;
         }
      }

      return null;
   }
}
