package com.hypixel.hytale.builtin.hytalegenerator.newsystem.stages;

import com.hypixel.hytale.builtin.hytalegenerator.bounds.Bounds3i;
import com.hypixel.hytale.builtin.hytalegenerator.framework.interfaces.functions.BiCarta;
import com.hypixel.hytale.builtin.hytalegenerator.newsystem.bufferbundle.NBufferBundle;
import com.hypixel.hytale.builtin.hytalegenerator.newsystem.bufferbundle.buffers.NCountedPixelBuffer;
import com.hypixel.hytale.builtin.hytalegenerator.newsystem.bufferbundle.buffers.type.NBufferType;
import com.hypixel.hytale.builtin.hytalegenerator.newsystem.bufferbundle.buffers.type.NParametrizedBufferType;
import com.hypixel.hytale.builtin.hytalegenerator.newsystem.views.NPixelBufferView;
import com.hypixel.hytale.builtin.hytalegenerator.threadindexer.WorkerIndexer;
import com.hypixel.hytale.builtin.hytalegenerator.worldstructure.WorldStructure;
import java.util.List;
import java.util.Map;
import javax.annotation.Nonnull;

public class NBiomeStage implements NStage {
   @Nonnull
   public static final Class<NCountedPixelBuffer> bufferClass = NCountedPixelBuffer.class;
   @Nonnull
   public static final Class<Integer> biomeClass = Integer.class;
   @Nonnull
   private final NParametrizedBufferType biomeOutputBufferType;
   @Nonnull
   private final String stageName;
   @Nonnull
   private final WorkerIndexer.Data<WorldStructure> worldStructure_workerData;

   public NBiomeStage(
      @Nonnull String stageName, @Nonnull NParametrizedBufferType biomeOutputBufferType, @Nonnull WorkerIndexer.Data<WorldStructure> worldStructure_workerData
   ) {
      this.stageName = stageName;
      this.biomeOutputBufferType = biomeOutputBufferType;
      this.worldStructure_workerData = worldStructure_workerData;
   }

   @Override
   public void run(@Nonnull NStage.Context context) {
      NBufferBundle.Access.View biomeAccess = context.bufferAccess.get(this.biomeOutputBufferType);
      NPixelBufferView<Integer> biomeSpace = new NPixelBufferView<>(biomeAccess, biomeClass);
      BiCarta<Integer> biomeMap = this.worldStructure_workerData.get(context.workerId).getBiomeMap();

      for (int x = biomeSpace.minX(); x < biomeSpace.maxX(); x++) {
         for (int z = biomeSpace.minZ(); z < biomeSpace.maxZ(); z++) {
            Integer biomeId = biomeMap.apply(x, z, context.workerId);
            biomeSpace.set(biomeId, x, 0, z);
         }
      }
   }

   @Nonnull
   @Override
   public Map<NBufferType, Bounds3i> getInputTypesAndBounds_bufferGrid() {
      return Map.of();
   }

   @Nonnull
   @Override
   public List<NBufferType> getOutputTypes() {
      return List.of(this.biomeOutputBufferType);
   }

   @Nonnull
   @Override
   public String getName() {
      return this.stageName;
   }
}
