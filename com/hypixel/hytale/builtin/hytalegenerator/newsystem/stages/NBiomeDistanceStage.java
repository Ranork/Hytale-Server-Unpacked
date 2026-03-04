package com.hypixel.hytale.builtin.hytalegenerator.newsystem.stages;

import com.hypixel.hytale.builtin.hytalegenerator.bounds.Bounds3i;
import com.hypixel.hytale.builtin.hytalegenerator.framework.math.Calculator;
import com.hypixel.hytale.builtin.hytalegenerator.newsystem.GridUtils;
import com.hypixel.hytale.builtin.hytalegenerator.newsystem.bufferbundle.NBufferBundle;
import com.hypixel.hytale.builtin.hytalegenerator.newsystem.bufferbundle.buffers.NCountedPixelBuffer;
import com.hypixel.hytale.builtin.hytalegenerator.newsystem.bufferbundle.buffers.NPixelBuffer;
import com.hypixel.hytale.builtin.hytalegenerator.newsystem.bufferbundle.buffers.NSimplePixelBuffer;
import com.hypixel.hytale.builtin.hytalegenerator.newsystem.bufferbundle.buffers.type.NBufferType;
import com.hypixel.hytale.builtin.hytalegenerator.newsystem.bufferbundle.buffers.type.NParametrizedBufferType;
import com.hypixel.hytale.builtin.hytalegenerator.newsystem.views.NPixelBufferView;
import com.hypixel.hytale.math.vector.Vector3i;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class NBiomeDistanceStage implements NStage {
   private static final double ORIGIN_REACH = 1.0;
   private static final double BUFFER_DIAGONAL_VOXEL_GRID = Math.sqrt(NPixelBuffer.SIZE.x * NPixelBuffer.SIZE.x + NPixelBuffer.SIZE.z * NPixelBuffer.SIZE.z);
   public static final double DEFAULT_DISTANCE_TO_BIOME_EDGE = Double.MAX_VALUE;
   @Nonnull
   public static final Class<NCountedPixelBuffer> biomeBufferClass = NCountedPixelBuffer.class;
   @Nonnull
   public static final Class<Integer> biomeClass = Integer.class;
   @Nonnull
   public static final Class<NSimplePixelBuffer> biomeDistanceBufferClass = NSimplePixelBuffer.class;
   @Nonnull
   public static final Class<NBiomeDistanceStage.BiomeDistanceEntries> biomeDistanceClass = NBiomeDistanceStage.BiomeDistanceEntries.class;
   @Nonnull
   private final NParametrizedBufferType biomeInputBufferType;
   @Nonnull
   private final NParametrizedBufferType biomeDistanceOutputBufferType;
   @Nonnull
   private final String stageName;
   private final double maxDistance_voxelGrid;
   private final int maxDistance_bufferGrid;
   @Nonnull
   private final Bounds3i inputBounds_bufferGrid;

   public NBiomeDistanceStage(
      @Nonnull String stageName,
      @Nonnull NParametrizedBufferType biomeInputBufferType,
      @Nonnull NParametrizedBufferType biomeDistanceOutputBufferType,
      double maxDistance_voxelGrid
   ) {
      assert maxDistance_voxelGrid >= 0.0;

      this.stageName = stageName;
      this.biomeInputBufferType = biomeInputBufferType;
      this.biomeDistanceOutputBufferType = biomeDistanceOutputBufferType;
      this.maxDistance_voxelGrid = maxDistance_voxelGrid;
      this.maxDistance_bufferGrid = GridUtils.toBufferDistanceInclusive_fromVoxelDistance((int)Math.ceil(maxDistance_voxelGrid));
      Bounds3i inputBounds_voxelGrid = GridUtils.createBounds_fromRadius_originVoxelInclusive((int)Math.ceil(maxDistance_voxelGrid));
      Bounds3i bufferColumnBounds_voxelGrid = GridUtils.createColumnBounds_voxelGrid(new Vector3i(), 0, 1);
      inputBounds_voxelGrid.stack(bufferColumnBounds_voxelGrid);
      this.inputBounds_bufferGrid = GridUtils.createBufferBoundsInclusive_fromVoxelBounds(inputBounds_voxelGrid);
      GridUtils.setBoundsYToWorldHeight_bufferGrid(this.inputBounds_bufferGrid);
   }

   @Override
   public void run(@Nonnull NStage.Context context) {
      NBufferBundle.Access.View biomeAccess = context.bufferAccess.get(this.biomeInputBufferType);
      NPixelBufferView<Integer> biomeSpace = new NPixelBufferView<>(biomeAccess, biomeClass);
      NBufferBundle.Access.View biomeDistanceAccess = context.bufferAccess.get(this.biomeDistanceOutputBufferType);
      NPixelBufferView<NBiomeDistanceStage.BiomeDistanceEntries> biomeDistanceSpace = new NPixelBufferView<>(biomeDistanceAccess, biomeDistanceClass);
      Vector3i position_voxelGrid = new Vector3i();

      for (position_voxelGrid.x = biomeDistanceSpace.minX(); position_voxelGrid.x < biomeDistanceSpace.maxX(); position_voxelGrid.x++) {
         for (position_voxelGrid.z = biomeDistanceSpace.minZ(); position_voxelGrid.z < biomeDistanceSpace.maxZ(); position_voxelGrid.z++) {
            NBiomeDistanceStage.BiomeDistanceEntries distanceEntries = this.createDistanceTracker(biomeAccess, biomeSpace, position_voxelGrid);
            biomeDistanceSpace.set(distanceEntries, position_voxelGrid);
         }
      }
   }

   @Nonnull
   private NBiomeDistanceStage.BiomeDistanceEntries createDistanceTracker(
      @Nonnull NBufferBundle.Access.View biomeAccess, @Nonnull NPixelBufferView<Integer> biomeSpace, @Nonnull Vector3i targetPosition_voxelGrid
   ) {
      NBiomeDistanceStage.BiomeDistanceCounter counter = new NBiomeDistanceStage.BiomeDistanceCounter();
      Vector3i position_bufferGrid = new Vector3i();
      Bounds3i scanBounds_voxelGrid = GridUtils.createBounds_fromRadius_originVoxelInclusive((int)Math.ceil(this.maxDistance_voxelGrid));
      scanBounds_voxelGrid.offset(targetPosition_voxelGrid);
      Bounds3i scanBounds_bufferGrid = GridUtils.createBufferBoundsInclusive_fromVoxelBounds(scanBounds_voxelGrid);

      for (position_bufferGrid.x = scanBounds_bufferGrid.min.x; position_bufferGrid.x < scanBounds_bufferGrid.max.x; position_bufferGrid.x++) {
         for (position_bufferGrid.z = scanBounds_bufferGrid.min.z; position_bufferGrid.z < scanBounds_bufferGrid.max.z; position_bufferGrid.z++) {
            double distanceToBuffer_voxelGrid = distanceToBuffer_voxelGrid(targetPosition_voxelGrid, position_bufferGrid);
            distanceToBuffer_voxelGrid = Math.max(distanceToBuffer_voxelGrid - 1.0, 0.0);
            if (!(distanceToBuffer_voxelGrid > this.maxDistance_voxelGrid)) {
               NCountedPixelBuffer<Integer> biomeBuffer = (NCountedPixelBuffer<Integer>)biomeAccess.getBuffer(position_bufferGrid).buffer();
               List<Integer> uniqueBiomeIds = biomeBuffer.getUniqueEntries();

               assert !uniqueBiomeIds.isEmpty();

               if (!allBiomesAreCountedAndFarther(counter, uniqueBiomeIds, distanceToBuffer_voxelGrid)) {
                  if (uniqueBiomeIds.size() == 1) {
                     if (!(distanceToBuffer_voxelGrid > this.maxDistance_voxelGrid)) {
                        counter.accountFor(uniqueBiomeIds.getFirst(), distanceToBuffer_voxelGrid);
                     }
                  } else {
                     Bounds3i bufferBounds_voxelGrid = GridUtils.createColumnBounds_voxelGrid(position_bufferGrid, 0, 1);
                     Vector3i columnPosition_voxelGrid = new Vector3i();

                     for (columnPosition_voxelGrid.x = bufferBounds_voxelGrid.min.x;
                        columnPosition_voxelGrid.x < bufferBounds_voxelGrid.max.x;
                        columnPosition_voxelGrid.x++
                     ) {
                        for (columnPosition_voxelGrid.z = bufferBounds_voxelGrid.min.z;
                           columnPosition_voxelGrid.z < bufferBounds_voxelGrid.max.z;
                           columnPosition_voxelGrid.z++
                        ) {
                           double distanceToColumn_voxelGrid = Calculator.distance(
                              columnPosition_voxelGrid.x, columnPosition_voxelGrid.z, targetPosition_voxelGrid.x, targetPosition_voxelGrid.z
                           );
                           distanceToColumn_voxelGrid = Math.max(distanceToColumn_voxelGrid - 1.0, 0.0);
                           if (!(distanceToColumn_voxelGrid > this.maxDistance_voxelGrid)) {
                              Integer biomeId = biomeSpace.getContent(columnPosition_voxelGrid);

                              assert biomeId != null;

                              counter.accountFor(biomeId, distanceToColumn_voxelGrid);
                           }
                        }
                     }
                  }
               }
            }
         }
      }

      return new NBiomeDistanceStage.BiomeDistanceEntries(counter.entries);
   }

   @Nonnull
   @Override
   public Map<NBufferType, Bounds3i> getInputTypesAndBounds_bufferGrid() {
      return Map.of(this.biomeInputBufferType, this.inputBounds_bufferGrid);
   }

   @Nonnull
   @Override
   public List<NBufferType> getOutputTypes() {
      return List.of(this.biomeDistanceOutputBufferType);
   }

   @Nonnull
   @Override
   public String getName() {
      return this.stageName;
   }

   public static double distanceToBuffer_voxelGrid(@Nonnull Vector3i position_voxelGrid, @Nonnull Vector3i position_bufferGrid) {
      assert (double)position_voxelGrid.y == 0.0;

      assert (double)position_bufferGrid.y == 0.0;

      Vector3i bufferAtPosition_bufferGrid = position_voxelGrid.clone();
      GridUtils.toBufferGrid_fromVoxelGrid(bufferAtPosition_bufferGrid);
      if (bufferAtPosition_bufferGrid.x == position_bufferGrid.x && bufferAtPosition_bufferGrid.z == position_bufferGrid.z) {
         return 0.0;
      } else {
         int cornerShift = NCountedPixelBuffer.SIZE_VOXEL_GRID.x - 1;
         Vector3i corner00 = position_bufferGrid.clone();
         GridUtils.toVoxelGrid_fromBufferGrid(corner00);
         Vector3i corner01 = new Vector3i(corner00);
         corner01.z += cornerShift;
         if (position_voxelGrid.x >= corner00.x && position_voxelGrid.x <= corner00.x + cornerShift) {
            return Math.min(Math.abs(position_voxelGrid.z - corner00.z), Math.abs(position_voxelGrid.z - corner01.z));
         } else {
            Vector3i corner10 = new Vector3i(corner00);
            corner10.x += cornerShift;
            if (position_voxelGrid.z >= corner00.z && position_voxelGrid.z <= corner00.z + cornerShift) {
               return Math.min(Math.abs(position_voxelGrid.x - corner00.x), Math.abs(position_voxelGrid.x - corner10.x));
            } else if (position_voxelGrid.x < corner00.x && position_voxelGrid.z < corner00.z) {
               return position_voxelGrid.distanceTo(corner00);
            } else if (position_voxelGrid.x < corner01.x && position_voxelGrid.z > corner01.z) {
               return position_voxelGrid.distanceTo(corner01);
            } else if (position_voxelGrid.x > corner10.x && position_voxelGrid.z < corner10.z) {
               return position_voxelGrid.distanceTo(corner10);
            } else {
               Vector3i corner11 = new Vector3i(corner10.x, 0, corner01.z);
               return position_voxelGrid.distanceTo(corner11);
            }
         }
      }
   }

   private static boolean allBiomesAreCountedAndFarther(
      @Nonnull NBiomeDistanceStage.BiomeDistanceCounter counter, @Nonnull List<Integer> uniqueBiomes, double distanceToBuffer_voxelGrid
   ) {
      for (Integer biomeId : uniqueBiomes) {
         if (counter.isCloserThanCounted(biomeId, distanceToBuffer_voxelGrid)) {
            return false;
         }
      }

      return true;
   }

   private static class BiomeDistanceCounter {
      @Nonnull
      final List<NBiomeDistanceStage.BiomeDistanceEntry> entries = new ArrayList<>(3);
      @Nullable
      NBiomeDistanceStage.BiomeDistanceEntry cachedEntry = null;

      BiomeDistanceCounter() {
      }

      boolean isCloserThanCounted(int biomeId, double distance_voxelGrid) {
         for (NBiomeDistanceStage.BiomeDistanceEntry entry : this.entries) {
            if (entry.biomeId == biomeId) {
               return distance_voxelGrid < entry.distance_voxelGrid;
            }
         }

         return true;
      }

      void accountFor(int biomeId, double distance_voxelGrid) {
         if (this.cachedEntry != null && this.cachedEntry.biomeId == biomeId) {
            if (!(this.cachedEntry.distance_voxelGrid <= distance_voxelGrid)) {
               this.cachedEntry.distance_voxelGrid = distance_voxelGrid;
            }
         } else {
            for (NBiomeDistanceStage.BiomeDistanceEntry entry : this.entries) {
               if (entry.biomeId == biomeId) {
                  this.cachedEntry = entry;
                  if (entry.distance_voxelGrid <= distance_voxelGrid) {
                     return;
                  }

                  entry.distance_voxelGrid = distance_voxelGrid;
                  return;
               }
            }

            NBiomeDistanceStage.BiomeDistanceEntry entryx = new NBiomeDistanceStage.BiomeDistanceEntry();
            entryx.biomeId = biomeId;
            entryx.distance_voxelGrid = distance_voxelGrid;
            this.entries.add(entryx);
            this.cachedEntry = entryx;
         }
      }
   }

   public static class BiomeDistanceEntries {
      @Nonnull
      public final List<NBiomeDistanceStage.BiomeDistanceEntry> entries;

      public BiomeDistanceEntries(@Nonnull List<NBiomeDistanceStage.BiomeDistanceEntry> entries) {
         this.entries = entries;
      }

      public double distanceToClosestOtherBiome(int thisBiomeId) {
         double smallestDistance = Double.MAX_VALUE;

         for (NBiomeDistanceStage.BiomeDistanceEntry entry : this.entries) {
            if (entry.biomeId != thisBiomeId) {
               smallestDistance = Math.min(smallestDistance, entry.distance_voxelGrid);
            }
         }

         return smallestDistance;
      }
   }

   public static class BiomeDistanceEntry {
      public int biomeId;
      public double distance_voxelGrid;
   }
}
