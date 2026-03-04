package com.hypixel.hytale.builtin.hytalegenerator.newsystem.stages;

import com.hypixel.hytale.builtin.hytalegenerator.bounds.Bounds3i;
import com.hypixel.hytale.builtin.hytalegenerator.newsystem.bufferbundle.NBufferBundle;
import com.hypixel.hytale.builtin.hytalegenerator.newsystem.bufferbundle.buffers.type.NBufferType;
import com.hypixel.hytale.builtin.hytalegenerator.threadindexer.WorkerIndexer;
import java.util.List;
import java.util.Map;
import javax.annotation.Nonnull;

public interface NStage {
   void run(@Nonnull NStage.Context var1);

   @Nonnull
   Map<NBufferType, Bounds3i> getInputTypesAndBounds_bufferGrid();

   @Nonnull
   List<NBufferType> getOutputTypes();

   @Nonnull
   String getName();

   public static final class Context {
      @Nonnull
      public Map<NBufferType, NBufferBundle.Access.View> bufferAccess;
      @Nonnull
      public WorkerIndexer.Id workerId;

      public Context(@Nonnull Map<NBufferType, NBufferBundle.Access.View> bufferAccess, @Nonnull WorkerIndexer.Id workerId) {
         this.bufferAccess = bufferAccess;
         this.workerId = workerId;
      }
   }
}
