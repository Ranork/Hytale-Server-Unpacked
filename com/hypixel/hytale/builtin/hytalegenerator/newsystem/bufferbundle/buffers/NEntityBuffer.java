package com.hypixel.hytale.builtin.hytalegenerator.newsystem.bufferbundle.buffers;

import com.hypixel.hytale.builtin.hytalegenerator.newsystem.performanceinstruments.MemInstrument;
import com.hypixel.hytale.builtin.hytalegenerator.props.entity.EntityPlacementData;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class NEntityBuffer extends NBuffer {
   @Nullable
   private List<EntityPlacementData> entities = null;
   private boolean isReference = false;

   public void forEach(@Nonnull Consumer<EntityPlacementData> consumer) {
      if (this.entities != null) {
         for (EntityPlacementData entity : this.entities) {
            consumer.accept(entity);
         }
      }
   }

   public void addEntity(@Nonnull EntityPlacementData entityPlacementData) {
      if (this.entities == null) {
         this.entities = new ArrayList<>();
      }

      this.entities.add(entityPlacementData);
   }

   @Nonnull
   @Override
   public MemInstrument.Report getMemoryUsage() {
      long size_bytes = 1L;
      if (this.entities != null) {
         size_bytes += 24L + 8L * this.entities.size();

         for (EntityPlacementData entity : this.entities) {
            size_bytes += entity.getMemoryUsage().size_bytes();
         }
      }

      return new MemInstrument.Report(size_bytes);
   }

   public void copyFrom(@Nonnull NEntityBuffer sourceBuffer) {
      this.entities = sourceBuffer.entities;
      this.isReference = true;
   }
}
