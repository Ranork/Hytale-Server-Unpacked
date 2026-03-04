package com.hypixel.hytale.builtin.hytalegenerator.newsystem.stages;

import com.hypixel.hytale.builtin.hytalegenerator.bounds.Bounds3i;
import com.hypixel.hytale.builtin.hytalegenerator.material.SolidMaterial;
import com.hypixel.hytale.builtin.hytalegenerator.newsystem.bufferbundle.NBufferBundle;
import com.hypixel.hytale.builtin.hytalegenerator.newsystem.bufferbundle.buffers.NVoxelBuffer;
import com.hypixel.hytale.builtin.hytalegenerator.newsystem.bufferbundle.buffers.type.NBufferType;
import com.hypixel.hytale.builtin.hytalegenerator.newsystem.bufferbundle.buffers.type.NParametrizedBufferType;
import com.hypixel.hytale.builtin.hytalegenerator.newsystem.views.NVoxelBufferView;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.procedurallib.logic.SimplexNoise;
import java.util.List;
import java.util.Map;
import javax.annotation.Nonnull;

public class NTestTerrainStage implements NStage {
   @Nonnull
   private static final Class<NVoxelBuffer> bufferClass = NVoxelBuffer.class;
   @Nonnull
   private static final Class<SolidMaterial> solidMaterialClass = SolidMaterial.class;
   @Nonnull
   private final NParametrizedBufferType outputBufferType;
   @Nonnull
   private final SolidMaterial ground;
   @Nonnull
   private final SolidMaterial empty;

   public NTestTerrainStage(@Nonnull NBufferType outputBufferType, @Nonnull SolidMaterial groundMaterial, @Nonnull SolidMaterial emptyMaterial) {
      assert outputBufferType instanceof NParametrizedBufferType;

      this.outputBufferType = (NParametrizedBufferType)outputBufferType;

      assert this.outputBufferType.isValidType(bufferClass, solidMaterialClass);

      this.ground = groundMaterial;
      this.empty = emptyMaterial;
   }

   @Override
   public void run(@Nonnull NStage.Context context) {
      NBufferBundle.Access.View access = context.bufferAccess.get(this.outputBufferType);
      NVoxelBufferView<SolidMaterial> materialBuffer = new NVoxelBufferView<>(access, solidMaterialClass);
      SimplexNoise noise = SimplexNoise.INSTANCE;
      Vector3i position = new Vector3i();

      for (position.x = materialBuffer.minX(); position.x < materialBuffer.maxX(); position.x++) {
         for (position.z = materialBuffer.minZ(); position.z < materialBuffer.maxZ(); position.z++) {
            for (position.y = materialBuffer.minY(); position.y < materialBuffer.maxY(); position.y++) {
               Vector3d noisePosition = position.toVector3d();
               noisePosition.scale(0.05);
               double noiseValue = noise.get(1, 0, noisePosition.x, noisePosition.y, noisePosition.z);
               if (position.y >= 130 && (!(noiseValue > 0.0) || position.y >= 150)) {
                  materialBuffer.set(this.empty, position);
               } else {
                  materialBuffer.set(this.ground, position);
               }
            }
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
      return List.of(this.outputBufferType);
   }

   @Nonnull
   @Override
   public String getName() {
      return "TestTerrainStage";
   }
}
