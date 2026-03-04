package com.hypixel.hytale.builtin.hytalegenerator.newsystem;

import com.hypixel.hytale.builtin.hytalegenerator.VectorUtil;
import com.hypixel.hytale.builtin.hytalegenerator.bounds.Bounds3i;
import com.hypixel.hytale.builtin.hytalegenerator.framework.math.Calculator;
import com.hypixel.hytale.builtin.hytalegenerator.newsystem.bufferbundle.buffers.NVoxelBuffer;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3i;
import javax.annotation.Nonnull;

public class GridUtils {
   public static final int BUFFER_COUNT_IN_CHUNK_Y = 320 / NVoxelBuffer.SIZE.y;

   public static void toBufferGrid_fromVoxelGridOverlap(@Nonnull Bounds3i bounds_voxelGrid) {
      assert bounds_voxelGrid.isCorrect();

      VectorUtil.bitShiftRight(3, bounds_voxelGrid.min);
      if (bounds_voxelGrid.max.x % NVoxelBuffer.SIZE.x == 0) {
         bounds_voxelGrid.max.x--;
      }

      if (bounds_voxelGrid.max.y % NVoxelBuffer.SIZE.y == 0) {
         bounds_voxelGrid.max.y--;
      }

      if (bounds_voxelGrid.max.z % NVoxelBuffer.SIZE.z == 0) {
         bounds_voxelGrid.max.z--;
      }

      bounds_voxelGrid.max.x >>= 3;
      bounds_voxelGrid.max.y >>= 3;
      bounds_voxelGrid.max.z >>= 3;
   }

   @Nonnull
   public static Bounds3i createColumnBounds_voxelGrid(@Nonnull Vector3i position_bufferGrid, int minY_voxelSpace, int maxY_voxelSpace) {
      assert minY_voxelSpace <= maxY_voxelSpace;

      Vector3i min = position_bufferGrid.clone();
      VectorUtil.bitShiftLeft(3, min);
      Vector3i max = min.clone().add(NVoxelBuffer.SIZE);
      min.y = minY_voxelSpace;
      max.y = maxY_voxelSpace;
      return new Bounds3i(min, max);
   }

   @Nonnull
   public static Bounds3i createBufferBoundsInclusive_fromVoxelBounds(@Nonnull Bounds3i bounds_voxelGrid) {
      assert bounds_voxelGrid.isCorrect();

      Vector3i min = bounds_voxelGrid.min.clone();
      Vector3i max = bounds_voxelGrid.max.clone();
      min.x = Calculator.floor(min.x, NVoxelBuffer.SIZE.x);
      min.x >>= 3;
      min.y = Calculator.floor(min.y, NVoxelBuffer.SIZE.y);
      min.y >>= 3;
      min.z = Calculator.floor(min.z, NVoxelBuffer.SIZE.z);
      min.z >>= 3;
      int mod = Calculator.wrap(max.x, NVoxelBuffer.SIZE.x);
      max.x = Calculator.ceil(max.x, NVoxelBuffer.SIZE.x);
      max.x >>= 3;
      if (mod == 0) {
         max.x += 2;
      } else {
         max.x++;
      }

      mod = Calculator.wrap(max.y, NVoxelBuffer.SIZE.y);
      max.y = Calculator.ceil(max.y, NVoxelBuffer.SIZE.y);
      max.y >>= 3;
      if (mod == 0) {
         max.y += 2;
      } else {
         max.y++;
      }

      mod = Calculator.wrap(max.z, NVoxelBuffer.SIZE.z);
      max.z = Calculator.ceil(max.z, NVoxelBuffer.SIZE.z);
      max.z >>= 3;
      if (mod == 0) {
         max.z += 2;
      } else {
         max.z++;
      }

      min.dropHash();
      max.dropHash();
      return new Bounds3i(min, max);
   }

   @Nonnull
   public static Bounds3i createColumnBounds_bufferGrid(@Nonnull Vector3i position_bufferGrid, int minY_bufferGrid, int maxY_bufferGrid) {
      assert minY_bufferGrid <= maxY_bufferGrid;

      Vector3i min = position_bufferGrid.clone();
      Vector3i max = min.clone().add(Vector3i.ALL_ONES);
      min.y = minY_bufferGrid;
      max.y = maxY_bufferGrid;
      return new Bounds3i(min, max);
   }

   @Nonnull
   public static Bounds3i createChunkBounds_voxelGrid(int x_chunkGrid, int z_chunkGrid) {
      Vector3i min = new Vector3i(x_chunkGrid << 5, 0, z_chunkGrid << 5);
      Vector3i max = min.clone().add(32, 320, 32);
      return new Bounds3i(min, max);
   }

   @Nonnull
   public static Bounds3i createUnitBounds3i(@Nonnull Vector3i position) {
      return new Bounds3i(position, position.clone().add(Vector3i.ALL_ONES));
   }

   @Nonnull
   public static Bounds3i createBounds_fromRadius_originVoxelInclusive(int radius) {
      Vector3i min = new Vector3i(-radius, -radius, -radius);
      Vector3i max = new Vector3i(radius + 1, radius + 1, radius + 1);
      return new Bounds3i(min, max);
   }

   @Nonnull
   public static Bounds3i createBounds_fromVector_originVoxelInclusive(@Nonnull Vector3i range) {
      Vector3i min = new Vector3i(range).scale(-1);
      Vector3i max = new Vector3i(range).add(Vector3i.ALL_ONES);
      return new Bounds3i(min, max);
   }

   @Nonnull
   public static Bounds3i createChunkBounds_bufferGrid(int x_chunkGrid, int z_chunkGrid) {
      int bits = 2;
      Vector3i min = new Vector3i(x_chunkGrid << 2, 0, z_chunkGrid << 2);
      Vector3i max = new Vector3i(x_chunkGrid + 1 << 2, 40, z_chunkGrid + 1 << 2);
      return new Bounds3i(min, max);
   }

   public static void toVoxelGrid_fromBufferGrid(@Nonnull Bounds3i bounds_bufferGrid) {
      assert bounds_bufferGrid.isCorrect();

      VectorUtil.bitShiftLeft(3, bounds_bufferGrid.min);
      VectorUtil.bitShiftLeft(3, bounds_bufferGrid.max);
   }

   public static void toVoxelGrid_fromBufferGrid(@Nonnull Vector3i position_voxelGrid) {
      VectorUtil.bitShiftLeft(3, position_voxelGrid);
   }

   public static void toBufferGrid_fromVoxelGrid(@Nonnull Vector3i worldPosition_voxelGrid) {
      VectorUtil.bitShiftRight(3, worldPosition_voxelGrid);
   }

   public static int toBufferDistanceInclusive_fromVoxelDistance(int distance_voxelGrid) {
      int distance_bufferGrid = distance_voxelGrid >> 3;
      return Calculator.wrap(distance_voxelGrid, NVoxelBuffer.SIZE.x) == 0 ? distance_bufferGrid : distance_bufferGrid + 1;
   }

   @Nonnull
   public static Vector3i toIntegerGrid_fromDecimalGrid(@Nonnull Vector3d worldPosition_decimalGrid) {
      Vector3i position = new Vector3i();
      position.x = Calculator.toIntFloored(worldPosition_decimalGrid.x);
      position.y = Calculator.toIntFloored(worldPosition_decimalGrid.y);
      position.z = Calculator.toIntFloored(worldPosition_decimalGrid.z);
      return position;
   }

   public static void toVoxelGridInsideBuffer_fromWorldGrid(@Nonnull Vector3i worldPosition_voxelGrid) {
      worldPosition_voxelGrid.x = Calculator.wrap(worldPosition_voxelGrid.x, NVoxelBuffer.SIZE.x);
      worldPosition_voxelGrid.y = Calculator.wrap(worldPosition_voxelGrid.y, NVoxelBuffer.SIZE.y);
      worldPosition_voxelGrid.z = Calculator.wrap(worldPosition_voxelGrid.z, NVoxelBuffer.SIZE.z);
   }

   public static int toIndexFromPositionYXZ(@Nonnull Vector3i position, @Nonnull Bounds3i bounds) {
      assert bounds.contains(position);

      int x = position.x - bounds.min.x;
      int y = position.y - bounds.min.y;
      int z = position.z - bounds.min.z;
      int sizeX = bounds.max.x - bounds.min.x;
      int sizeY = bounds.max.y - bounds.min.y;
      return y + x * sizeY + z * sizeY * sizeX;
   }

   public static void setBoundsYToWorldHeight_bufferGrid(@Nonnull Bounds3i bounds_bufferGrid) {
      assert bounds_bufferGrid.isCorrect();

      bounds_bufferGrid.min.setY(0);
      bounds_bufferGrid.max.setY(40);
   }

   public static void setBoundsYToWorldHeight_voxelGrid(@Nonnull Bounds3i bounds_voxelGrid) {
      assert bounds_voxelGrid.isCorrect();

      bounds_voxelGrid.min.setY(0);
      bounds_voxelGrid.max.setY(320);
   }

   public static void toVoxelPosition_fromChunkPosition(@Nonnull Vector3i chunkPosition_voxelGrid) {
      chunkPosition_voxelGrid.x <<= 5;
      chunkPosition_voxelGrid.z <<= 5;
   }
}
