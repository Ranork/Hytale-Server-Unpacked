package com.hypixel.hytale.builtin.hytalegenerator.engine.stages;

import com.hypixel.hytale.builtin.hytalegenerator.bounds.Bounds3i;
import com.hypixel.hytale.builtin.hytalegenerator.engine.bufferbundle.BufferBundle;
import com.hypixel.hytale.builtin.hytalegenerator.engine.bufferbundle.buffers.type.BufferType;
import com.hypixel.hytale.builtin.hytalegenerator.workerindexer.WorkerIndexer;
import java.util.List;
import java.util.Map;
import javax.annotation.Nonnull;

public interface Stage {
   void run(@Nonnull Stage.Context var1);

   @Nonnull
   Map<BufferType, Bounds3i> getInputTypesAndBounds_bufferGrid();

   @Nonnull
   List<BufferType> getOutputTypes();

   @Nonnull
   String getName();

   public static final class Context {
      @Nonnull
      public Map<BufferType, BufferBundle.Access.View> bufferAccess;
      @Nonnull
      public WorkerIndexer.Id workerId;

      public Context(@Nonnull Map<BufferType, BufferBundle.Access.View> bufferAccess, @Nonnull WorkerIndexer.Id workerId) {
         this.bufferAccess = bufferAccess;
         this.workerId = workerId;
      }
   }
}
