package com.hypixel.hytale.builtin.hytalegenerator.datastructures.voxelspace;

import javax.annotation.Nonnull;

public class VoxelCoordinate {
   int x;
   int y;
   int z;

   public VoxelCoordinate(int x, int y, int z) {
      this.x = x;
      this.y = y;
      this.z = z;
   }

   @Override
   public boolean equals(Object other) {
      return !(other instanceof VoxelCoordinate otherVoxelCoordinate)
         ? false
         : this == otherVoxelCoordinate || this.x == otherVoxelCoordinate.x && this.y == otherVoxelCoordinate.y && this.z == otherVoxelCoordinate.z;
   }

   @Nonnull
   public VoxelCoordinate clone() {
      return new VoxelCoordinate(this.x, this.y, this.z);
   }

   @Nonnull
   @Override
   public String toString() {
      return "VoxelCoordinate{x=" + this.x + ", y=" + this.y + ", z=" + this.z + "}";
   }
}
