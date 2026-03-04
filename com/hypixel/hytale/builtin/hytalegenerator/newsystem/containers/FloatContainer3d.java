package com.hypixel.hytale.builtin.hytalegenerator.newsystem.containers;

import com.hypixel.hytale.builtin.hytalegenerator.bounds.Bounds3i;
import com.hypixel.hytale.builtin.hytalegenerator.newsystem.GridUtils;
import com.hypixel.hytale.math.vector.Vector3i;
import javax.annotation.Nonnull;

public class FloatContainer3d {
   @Nonnull
   private final Bounds3i bounds_voxelGrid;
   @Nonnull
   private final Vector3i size_voxelGrid;
   @Nonnull
   private final float[] data;
   private final float outOfBoundsValue;

   public FloatContainer3d(@Nonnull Bounds3i bounds_voxelGrid, float outOfBoundsValue) {
      this.bounds_voxelGrid = bounds_voxelGrid.clone();
      this.size_voxelGrid = bounds_voxelGrid.getSize();
      this.data = new float[this.size_voxelGrid.x * this.size_voxelGrid.y * this.size_voxelGrid.z];
      this.outOfBoundsValue = outOfBoundsValue;
   }

   public float get(@Nonnull Vector3i position_voxelGrid) {
      if (!this.bounds_voxelGrid.contains(position_voxelGrid)) {
         return this.outOfBoundsValue;
      } else {
         int index = GridUtils.toIndexFromPositionYXZ(position_voxelGrid, this.bounds_voxelGrid);
         return this.data[index];
      }
   }

   @Nonnull
   public Bounds3i getBounds_voxelGrid() {
      return this.bounds_voxelGrid;
   }

   public void set(@Nonnull Vector3i position_voxelGrid, float value) {
      assert this.bounds_voxelGrid.contains(position_voxelGrid);

      int index = GridUtils.toIndexFromPositionYXZ(position_voxelGrid, this.bounds_voxelGrid);
      this.data[index] = value;
   }

   public void moveMinTo(@Nonnull Vector3i min_voxelGrid) {
      Vector3i oldMin_voxelGrid = this.bounds_voxelGrid.min.clone().scale(-1);
      this.bounds_voxelGrid.offset(oldMin_voxelGrid);
      this.bounds_voxelGrid.offset(min_voxelGrid);
   }
}
