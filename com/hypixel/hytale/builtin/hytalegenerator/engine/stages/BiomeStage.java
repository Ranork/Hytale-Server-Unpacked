package com.hypixel.hytale.builtin.hytalegenerator.engine.stages;

import com.hypixel.hytale.builtin.hytalegenerator.bounds.Bounds3i;
import com.hypixel.hytale.builtin.hytalegenerator.engine.bufferbundle.BufferBundle;
import com.hypixel.hytale.builtin.hytalegenerator.engine.bufferbundle.buffers.CountedPixelBuffer;
import com.hypixel.hytale.builtin.hytalegenerator.engine.bufferbundle.buffers.type.BufferType;
import com.hypixel.hytale.builtin.hytalegenerator.engine.bufferbundle.buffers.type.ParametrizedBufferType;
import com.hypixel.hytale.builtin.hytalegenerator.engine.views.PixelBufferView;
import com.hypixel.hytale.builtin.hytalegenerator.workerindexer.WorkerIndexer;
import com.hypixel.hytale.builtin.hytalegenerator.worldstructure.BiCarta;
import com.hypixel.hytale.builtin.hytalegenerator.worldstructure.WorldStructure;
import java.util.List;
import java.util.Map;
import javax.annotation.Nonnull;

public class BiomeStage implements Stage {
   @Nonnull
   public static final Class<CountedPixelBuffer> bufferClass = CountedPixelBuffer.class;
   @Nonnull
   public static final Class<Integer> biomeClass = Integer.class;
   @Nonnull
   private final ParametrizedBufferType biomeOutputBufferType;
   @Nonnull
   private final String stageName;
   @Nonnull
   private final WorkerIndexer.Data<WorldStructure> worldStructure_workerData;

   public BiomeStage(
      @Nonnull String stageName, @Nonnull ParametrizedBufferType biomeOutputBufferType, @Nonnull WorkerIndexer.Data<WorldStructure> worldStructure_workerData
   ) {
      this.stageName = stageName;
      this.biomeOutputBufferType = biomeOutputBufferType;
      this.worldStructure_workerData = worldStructure_workerData;
   }

   @Override
   public void run(@Nonnull Stage.Context context) {
      BufferBundle.Access.View biomeAccess = context.bufferAccess.get(this.biomeOutputBufferType);
      PixelBufferView<Integer> biomeSpace = new PixelBufferView<>(biomeAccess, biomeClass);
      BiCarta<Integer> biomeMap = this.worldStructure_workerData.get(context.workerId).getBiomeMap();
      Bounds3i bounds_voxelSpace = biomeSpace.getBounds();

      for (int x = bounds_voxelSpace.min.x; x < bounds_voxelSpace.max.x; x++) {
         for (int z = bounds_voxelSpace.min.z; z < bounds_voxelSpace.max.z; z++) {
            Integer biomeId = biomeMap.apply(x, z, context.workerId);
            biomeSpace.set(biomeId, x, 0, z);
         }
      }
   }

   @Nonnull
   @Override
   public Map<BufferType, Bounds3i> getInputTypesAndBounds_bufferGrid() {
      return Map.of();
   }

   @Nonnull
   @Override
   public List<BufferType> getOutputTypes() {
      return List.of(this.biomeOutputBufferType);
   }

   @Nonnull
   @Override
   public String getName() {
      return this.stageName;
   }
}
