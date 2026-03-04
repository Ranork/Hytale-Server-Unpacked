package com.hypixel.hytale.builtin.hytalegenerator.newsystem.stages;

import com.hypixel.hytale.builtin.hytalegenerator.bounds.Bounds3i;
import com.hypixel.hytale.builtin.hytalegenerator.material.SolidMaterial;
import com.hypixel.hytale.builtin.hytalegenerator.newsystem.bufferbundle.NBufferBundle;
import com.hypixel.hytale.builtin.hytalegenerator.newsystem.bufferbundle.buffers.NVoxelBuffer;
import com.hypixel.hytale.builtin.hytalegenerator.newsystem.bufferbundle.buffers.type.NBufferType;
import com.hypixel.hytale.builtin.hytalegenerator.newsystem.bufferbundle.buffers.type.NParametrizedBufferType;
import com.hypixel.hytale.builtin.hytalegenerator.newsystem.views.NVoxelBufferView;
import com.hypixel.hytale.math.vector.Vector3i;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import javax.annotation.Nonnull;

public class NTestPropStage implements NStage {
   @Nonnull
   private static final Class<NVoxelBuffer> bufferClass = NVoxelBuffer.class;
   @Nonnull
   private static final Class<SolidMaterial> solidMaterialClass = SolidMaterial.class;
   private final int CONTEXT_DEPENDENCY_RANGE_BUFFER_GRID = 0;
   @Nonnull
   private final Bounds3i inputBounds_bufferGrid = new Bounds3i(new Vector3i(0, 0, 0), new Vector3i(1, 40, 1));
   @Nonnull
   private final NParametrizedBufferType inputBufferType;
   @Nonnull
   private final NParametrizedBufferType outputBufferType;
   @Nonnull
   private final SolidMaterial floorMaterial;
   @Nonnull
   private final SolidMaterial anchorMaterial;
   @Nonnull
   private final SolidMaterial propMaterial;

   public NTestPropStage(
      @Nonnull NBufferType inputBufferType,
      @Nonnull NBufferType outputBufferType,
      @Nonnull SolidMaterial floorMaterial,
      @Nonnull SolidMaterial anchorMaterial,
      @Nonnull SolidMaterial propMaterial
   ) {
      assert inputBufferType instanceof NParametrizedBufferType;

      assert outputBufferType instanceof NParametrizedBufferType;

      this.inputBufferType = (NParametrizedBufferType)inputBufferType;
      this.outputBufferType = (NParametrizedBufferType)outputBufferType;

      assert this.outputBufferType.isValidType(bufferClass, solidMaterialClass);

      this.floorMaterial = floorMaterial;
      this.anchorMaterial = anchorMaterial;
      this.propMaterial = propMaterial;
   }

   @Override
   public void run(@Nonnull NStage.Context context) {
      NBufferBundle.Access.View inputAccess = context.bufferAccess.get(this.inputBufferType);
      NVoxelBufferView<SolidMaterial> inputView = new NVoxelBufferView<>(inputAccess, solidMaterialClass);
      NBufferBundle.Access.View outputAccess = context.bufferAccess.get(this.outputBufferType);
      NVoxelBufferView<SolidMaterial> outputView = new NVoxelBufferView<>(outputAccess, solidMaterialClass);
      outputView.copyFrom(inputView);
      Vector3i scanPosition = new Vector3i(0, 316, 0);
      Random rand = new Random(Objects.hash(outputView.minX() * 1000, outputView.minZ()));
      scanPosition.setX(rand.nextInt(NVoxelBuffer.SIZE.x) + outputView.minX());
      scanPosition.setZ(rand.nextInt(NVoxelBuffer.SIZE.z) + outputView.minZ());

      for (; scanPosition.y >= 10; scanPosition.setY(scanPosition.y - 1)) {
         SolidMaterial floor = inputView.getContent(scanPosition.clone().add(0, -1, 0));
         SolidMaterial anchor = inputView.getContent(scanPosition);
         if (this.floorMaterial.equals(floor) && this.anchorMaterial.equals(anchor)) {
            this.placeProp(scanPosition, outputView);
         }
      }
   }

   private void placeProp(@Nonnull Vector3i position, @Nonnull NVoxelBufferView<SolidMaterial> view) {
      int height = 5;
      Vector3i placePosition = position.clone();

      for (int i = 0; i < 5; i++) {
         view.set(this.propMaterial, placePosition);
         placePosition.setY(placePosition.getY() + 1);
      }
   }

   @Nonnull
   @Override
   public Map<NBufferType, Bounds3i> getInputTypesAndBounds_bufferGrid() {
      return Map.of(this.inputBufferType, this.inputBounds_bufferGrid.clone());
   }

   @Nonnull
   @Override
   public List<NBufferType> getOutputTypes() {
      return List.of(this.outputBufferType);
   }

   @Nonnull
   @Override
   public String getName() {
      return "TestPropStage";
   }
}
