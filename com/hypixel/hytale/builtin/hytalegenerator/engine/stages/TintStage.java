package com.hypixel.hytale.builtin.hytalegenerator.engine.stages;

import com.hypixel.hytale.builtin.hytalegenerator.GridUtils;
import com.hypixel.hytale.builtin.hytalegenerator.Registry;
import com.hypixel.hytale.builtin.hytalegenerator.biome.Biome;
import com.hypixel.hytale.builtin.hytalegenerator.bounds.Bounds3i;
import com.hypixel.hytale.builtin.hytalegenerator.engine.bufferbundle.BufferBundle;
import com.hypixel.hytale.builtin.hytalegenerator.engine.bufferbundle.buffers.CountedPixelBuffer;
import com.hypixel.hytale.builtin.hytalegenerator.engine.bufferbundle.buffers.SimplePixelBuffer;
import com.hypixel.hytale.builtin.hytalegenerator.engine.bufferbundle.buffers.type.BufferType;
import com.hypixel.hytale.builtin.hytalegenerator.engine.bufferbundle.buffers.type.ParametrizedBufferType;
import com.hypixel.hytale.builtin.hytalegenerator.engine.views.PixelBufferView;
import com.hypixel.hytale.builtin.hytalegenerator.tintproviders.TintProvider;
import com.hypixel.hytale.builtin.hytalegenerator.workerindexer.WorkerIndexer;
import com.hypixel.hytale.builtin.hytalegenerator.worldstructure.WorldStructure;
import com.hypixel.hytale.math.vector.Vector3i;
import java.util.List;
import java.util.Map;
import javax.annotation.Nonnull;

public class TintStage implements Stage {
   @Nonnull
   public static final Class<CountedPixelBuffer> biomeBufferClass = CountedPixelBuffer.class;
   @Nonnull
   public static final Class<Integer> biomeClass = Integer.class;
   @Nonnull
   public static final Class<SimplePixelBuffer> tintBufferClass = SimplePixelBuffer.class;
   @Nonnull
   public static final Class<Integer> tintClass = Integer.class;
   @Nonnull
   private final ParametrizedBufferType biomeInputBufferType;
   @Nonnull
   private final ParametrizedBufferType tintOutputBufferType;
   @Nonnull
   private final Bounds3i inputBounds_bufferGrid;
   @Nonnull
   private final String stageName;
   @Nonnull
   private final WorkerIndexer.Data<WorldStructure> worldStructure_workerData;

   public TintStage(
      @Nonnull String stageName,
      @Nonnull ParametrizedBufferType biomeInputBufferType,
      @Nonnull ParametrizedBufferType tintOutputBufferType,
      @Nonnull WorkerIndexer.Data<WorldStructure> worldStructure_workerData
   ) {
      assert biomeInputBufferType.isValidType(biomeBufferClass, biomeClass);

      assert tintOutputBufferType.isValidType(tintBufferClass, tintClass);

      this.biomeInputBufferType = biomeInputBufferType;
      this.tintOutputBufferType = tintOutputBufferType;
      this.stageName = stageName;
      this.worldStructure_workerData = worldStructure_workerData;
      this.inputBounds_bufferGrid = GridUtils.createUnitBounds3i(Vector3i.ZERO);
   }

   @Override
   public void run(@Nonnull Stage.Context context) {
      BufferBundle.Access.View biomeAccess = context.bufferAccess.get(this.biomeInputBufferType);
      PixelBufferView<Integer> biomeSpace = new PixelBufferView<>(biomeAccess, biomeClass);
      BufferBundle.Access.View tintAccess = context.bufferAccess.get(this.tintOutputBufferType);
      PixelBufferView<Integer> tintSpace = new PixelBufferView<>(tintAccess, tintClass);
      Bounds3i outputBounds_voxelGrid = tintSpace.getBounds();
      Registry<Biome> biomeRegistry = this.worldStructure_workerData.get(context.workerId).getBiomeRegistry();
      Vector3i position_voxelGrid = new Vector3i(outputBounds_voxelGrid.min);
      position_voxelGrid.setY(0);
      TintProvider.Context tintContext = new TintProvider.Context(position_voxelGrid, context.workerId);

      for (position_voxelGrid.x = outputBounds_voxelGrid.min.x; position_voxelGrid.x < outputBounds_voxelGrid.max.x; position_voxelGrid.x++) {
         for (position_voxelGrid.z = outputBounds_voxelGrid.min.z; position_voxelGrid.z < outputBounds_voxelGrid.max.z; position_voxelGrid.z++) {
            Integer biomeId = biomeSpace.get(position_voxelGrid.x, 0, position_voxelGrid.z);

            assert biomeId != null;

            Biome biome = biomeRegistry.getObject(biomeId);

            assert biome != null;

            TintProvider tintProvider = biome.getTintProvider();
            TintProvider.Result tintResult = tintProvider.getValue(tintContext);
            if (!tintResult.hasValue) {
               tintSpace.set(TintProvider.DEFAULT_TINT, position_voxelGrid);
            } else {
               tintSpace.set(tintResult.tint, position_voxelGrid);
            }
         }
      }
   }

   @Nonnull
   @Override
   public Map<BufferType, Bounds3i> getInputTypesAndBounds_bufferGrid() {
      return Map.of(this.biomeInputBufferType, this.inputBounds_bufferGrid);
   }

   @Nonnull
   @Override
   public List<BufferType> getOutputTypes() {
      return List.of(this.tintOutputBufferType);
   }

   @Nonnull
   @Override
   public String getName() {
      return this.stageName;
   }
}
