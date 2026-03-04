package com.hypixel.hytale.builtin.hytalegenerator.framework.math;

public class RegionGrid {
   private int regionSizeX;
   private int regionSizeZ;

   public RegionGrid(int regionSizeX, int regionSizeZ) {
      this.regionSizeX = regionSizeX;
      this.regionSizeZ = regionSizeZ;
   }

   public int regionMinX(int chunkX) {
      return chunkX >= 0 ? chunkX / this.regionSizeX * this.regionSizeX : (chunkX - (this.regionSizeZ - 1)) / this.regionSizeX * this.regionSizeX;
   }

   public int regionMinZ(int chunkZ) {
      return chunkZ >= 0 ? chunkZ / this.regionSizeZ * this.regionSizeZ : (chunkZ - (this.regionSizeX - 1)) / this.regionSizeZ * this.regionSizeZ;
   }

   public int regionMaxX(int chunkX) {
      return this.regionMinX(chunkX) + this.regionSizeX;
   }

   public int regionMaxZ(int chunkZ) {
      return this.regionMinZ(chunkZ) + this.regionSizeZ;
   }
}
